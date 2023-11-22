/**
 *  Copyright 2016-2022 Jörg Prante <joergprante@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache License 2.0</a>
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.xbib.marc.xml;

import org.xbib.marc.MarcField;
import org.xbib.marc.MarcListener;
import org.xbib.marc.MarcRecord;
import org.xbib.marc.MarcRecordListener;
import org.xbib.marc.label.RecordLabel;
import org.xbib.marc.transformer.value.MarcValueTransformers;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.util.XMLEventConsumer;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

/**
 * This MarcXchange Writer writes MarcXchange events to a StaX XML output stream or XML event consumer.
 * Default output format is MarcXchange.
 */
@SuppressWarnings("this-escape")
public class MarcXchangeWriter extends MarcContentHandler implements Flushable, Closeable {

    private static final Logger logger = Logger.getLogger(MarcXchangeWriter.class.getName());

    private static final int DEFAULT_BUFFER_SIZE = 65536;

    private static final String NAMESPACE_URI = MARCXCHANGE_V2_NS_URI;

    private static final String NAMESPACE_SCHEMA_LOCATION = MARCXCHANGE_V2_0_SCHEMA_LOCATION;

    private static final QName COLLECTION_ELEMENT = new QName(NAMESPACE_URI, COLLECTION, "");

    private static final QName RECORD_ELEMENT = new QName(NAMESPACE_URI, RECORD, "");

    private static final QName LEADER_ELEMENT = new QName(NAMESPACE_URI, LEADER, "");

    private static final QName CONTROLFIELD_ELEMENT = new QName(NAMESPACE_URI, CONTROLFIELD, "");

    private static final QName DATAFIELD_ELEMENT = new QName(NAMESPACE_URI, DATAFIELD, "");

    private static final QName SUBFIELD_ELEMENT = new QName(NAMESPACE_URI, SUBFIELD, "");

    protected final XMLEventFactory eventFactory;

    private final Namespace namespace;

    private final Lock lock;

    private Writer writer;

    private boolean indent;

    private XMLEventConsumer xmlEventConsumer;

    private Iterator<Namespace> namespaces;

    private Exception exception;

    private boolean documentStarted;

    private boolean collectionStarted;

    private boolean recordStarted;

    private boolean fatalErrors;

    private boolean schemaWritten;

    private String fileNamePattern;

    private AtomicInteger fileNameCounter;

    private int splitlimit;

    private int bufferSize;

    private boolean compress;

    private boolean isClosed;

    /**
     * Create a MarcXchange writer on an underlying output stream.
     * @param out the underlying output stream
     * @throws IOException if writer can not be created
     */
    public MarcXchangeWriter(OutputStream out) throws IOException {
        this(out, false);
    }

    /**
     * Create a MarcXchange writer on an underlying output stream.
     * @param out the underlying output stream
     * @param indent if true, indent MarcXchange output
     * @throws IOException if writer can not be created
     */
    public MarcXchangeWriter(OutputStream out, boolean indent) throws IOException {
        this(new OutputStreamWriter(out, StandardCharsets.UTF_8), indent);
    }

    /**
     * Create a MarcXchange writer on an underlying writer.
     * @param writer the underlying writer
     * @throws IOException if writer can not be created
     */
    public MarcXchangeWriter(Writer writer) throws IOException {
        this(writer, false);
    }

    /**
     * Create a MarcXchange writer on an underlying writer.
     * @param writer the underlying writer
     * @param indent if true, indent MarcXchange output
     * @throws IOException if writer can not be created
     */
    public MarcXchangeWriter(Writer writer, boolean indent) throws IOException {
        this.writer = writer;
        this.indent = indent;
        this.bufferSize = DEFAULT_BUFFER_SIZE;
        this.lock = new ReentrantLock();
        this.documentStarted = false;
        this.collectionStarted = false;
        eventFactory = XMLEventFactory.newInstance();
        namespace = createNameSpace();
        setupEventConsumer(writer, indent);
    }

    /**
     * Create a MarcXchange writer in "split writer" mode.
     * @param fileNamePattern file name pattern
     * @param splitlimit split write limit. Split records if configured. A splitlimit of -1 prevents splitting.
     * @param bufferSize buffer size
     * @param compress if true, compress MarcXchange output
     * @param indent if true, indent MarcXchange output
     * @throws IOException if writer can not be created
     */
    public MarcXchangeWriter(String fileNamePattern, int splitlimit, int bufferSize, boolean compress, boolean indent)
            throws IOException {
        this.fileNameCounter = new AtomicInteger(0);
        this.fileNamePattern = fileNamePattern;
        this.splitlimit = splitlimit;
        this.bufferSize = bufferSize;
        this.compress = compress;
        this.indent = indent;
        this.lock = new ReentrantLock();
        this.documentStarted = false;
        this.collectionStarted = false;
        this.eventFactory = XMLEventFactory.newInstance();
        this.namespace = createNameSpace();
        newWriter(fileNamePattern, fileNameCounter, bufferSize, compress);
        setupEventConsumer(writer, indent);
    }

    /**
     * Write MarcXchange to an XML event consumer.
     *
     * @param consumer an XML event consumer
     */
    public MarcXchangeWriter(XMLEventConsumer consumer) {
        this.xmlEventConsumer = consumer;
        this.lock = new ReentrantLock();
        this.eventFactory = XMLEventFactory.newInstance();
        this.namespace = createNameSpace();
        this.namespaces = Collections.singletonList(namespace).iterator();
    }

    @Override
    public MarcXchangeWriter setFormat(String format) {
        super.setFormat(format);
        return this;
    }

    @Override
    public MarcXchangeWriter setType(String type) {
        super.setType(type);
        return this;
    }

    @Override
    public MarcXchangeWriter setMarcValueTransformers(MarcValueTransformers marcValueTransformers) {
        super.setMarcValueTransformers(marcValueTransformers);
        return this;
    }

    public MarcXchangeWriter setFatalErrors(boolean fatalErrors) {
        this.fatalErrors = fatalErrors;
        return this;
    }

    @Override
    public MarcXchangeWriter setMarcListener(MarcListener listener) {
        super.setMarcListener(listener);
        return this;
    }

    public MarcXchangeWriter setMarcRecordListener(MarcRecordListener recordListener) {
        super.setMarcRecordListener(recordListener);
        return this;
    }

    @Override
    public void startDocument() {
        if (exception != null) {
            return;
        }
        try {
            if (!documentStarted) {
                xmlEventConsumer.add(eventFactory.createStartDocument("UTF-8", "1.0"));
                documentStarted = true;
            }
        } catch (XMLStreamException e) {
            handleException(new IOException(e));
        }
    }

    @Override
    public void endDocument() {
        if (exception != null) {
            return;
        }
        try {
            if (documentStarted) {
                xmlEventConsumer.add(eventFactory.createEndDocument());
                documentStarted = false;
            }
        } catch (XMLStreamException e) {
            handleException(new IOException(e));
        }
    }

    @Override
    public void beginCollection() {
        super.beginCollection();
        if (exception != null) {
            return;
        }
        try {
            if (!collectionStarted) {
                Iterator<Attribute> attrs = null;
                if (!schemaWritten) {
                    List<Attribute> list = new ArrayList<>();
                    writeSchema(list);
                    attrs = list.iterator();
                }
                xmlEventConsumer.add(eventFactory.createStartElement(getCollectionElement(), attrs, namespaces));
                schemaWritten = true;
                collectionStarted = true;
            }
        } catch (XMLStreamException e) {
            handleException(new IOException(e));
        }
    }

    @Override
    public void beginRecord(String format, String type) {
        super.beginRecord(format, type);
        if (exception != null) {
            return;
        }
        try {
            if (!recordStarted) {
                List<Attribute> attrs = new LinkedList<>();
                if (createFormatAttribute()) {
                    String realformat = getFormat() != null ? getFormat() : format != null ? format : getDefaultFormat();
                    attrs.add(eventFactory.createAttribute(FORMAT_ATTRIBUTE, realformat));
                }
                if (createTypeAttribute()) {
                    String realtype = getType() != null ? getType() : type != null ? type : getDefaultType();
                    attrs.add(eventFactory.createAttribute(TYPE_ATTRIBUTE, realtype));
                }
                if (!schemaWritten) {
                    writeSchema(attrs);
                    schemaWritten = true;
                }
                xmlEventConsumer.add(eventFactory.createStartElement(getRecordElement(), attrs.iterator(), namespaces));
                recordStarted = true;
            }
        } catch (XMLStreamException e) {
            handleException(new IOException(e));
        }
    }

    @Override
    public void leader(RecordLabel label) {
        super.leader(label);
        if (exception != null) {
            return;
        }
        if (label == null) {
            return;
        }
        try {
            xmlEventConsumer.add(eventFactory.createStartElement(getLeaderElement(), null, namespaces));
            RecordLabel recordLabel = RecordLabel.builder()
                    .from(label)
                    .setRecordLength(0) // reset record length, does not make sense in XML
                    .build();
            xmlEventConsumer.add(eventFactory.createCharacters(recordLabel.toString()));
            xmlEventConsumer.add(eventFactory.createEndElement(getLeaderElement(), namespaces));
        } catch (XMLStreamException e) {
            handleException(new IOException(e));
        }
    }

    @Override
    public void field(MarcField field) {
        try {
            super.field(field);
            if (exception != null) {
                return;
            }
            if (field.isControl()) {
                String value = field.recoverControlFieldValue();
                if (value != null && !value.isEmpty()) {
                    Iterator<Attribute> attrs = Collections.singletonList(eventFactory.createAttribute(TAG_ATTRIBUTE,
                            transform(field.getTag()))).iterator();
                    xmlEventConsumer.add(eventFactory.createStartElement(getControlfieldElement(), attrs, namespaces));
                    xmlEventConsumer.add(eventFactory.createCharacters(transform(value)));
                    xmlEventConsumer.add(eventFactory.createEndElement(getControlfieldElement(), namespaces));
                }
            } else if (!field.isEmpty()) {
                String tag = field.getTag();
                String indicator = field.getIndicator();
                String ind1 = indicator != null && indicator.length() > 0 ? indicator.substring(0, 1) : " ";
                String ind2 = indicator != null && indicator.length() > 1 ? indicator.substring(1, 2) : " ";
                List<Attribute> attrs = new LinkedList<>();
                attrs.add(eventFactory.createAttribute(TAG_ATTRIBUTE, transform(tag)));
                // not full MarcXchange indicators
                attrs.add(eventFactory.createAttribute(IND_ATTRIBUTE + "1", transform(ind1)));
                attrs.add(eventFactory.createAttribute(IND_ATTRIBUTE + "2", transform(ind2)));
                xmlEventConsumer.add(eventFactory.createStartElement(getDatafieldElement(), attrs.iterator(), namespaces));
                for (MarcField.Subfield subfield : field.getSubfields()) {
                    String value = subfield.getValue();
                    // we skip null values because XML event consumer will fail on length() with NPE.
                    // we do not skip empty values because of subfield ID transport in MAB or UNIMARC.
                    if (value == null) {
                        continue;
                    }
                    String code = subfield.getId();
                    // From https://www.loc.gov/standards/iso25577/ISO_DIS_25577_2(E)070727.doc
                    // "There is one restriction. A special mode (identifier length = 0) of ISO 2709 operates with
                    // data fields without subfields. In the MarcXchange schema subfields are required,
                    // i.e. identifier length = 0 is not supported."
                    // But we support it! A subfield ID of length 0 will be substituted by blank (" ").
                    if (code.isEmpty()) {
                        code = " ";
                    }
                    List<Attribute> subfieldattrs = new LinkedList<>();
                    subfieldattrs.add(eventFactory.createAttribute(CODE_ATTRIBUTE, transform(code)));
                    xmlEventConsumer.add(eventFactory.createStartElement(getSubfieldElement(),
                            subfieldattrs.iterator(), namespaces));
                    xmlEventConsumer.add(eventFactory.createCharacters(transform(value)));
                    xmlEventConsumer.add(eventFactory.createEndElement(getSubfieldElement(), namespaces));
                }
                String value = field.getValue();
                if (value != null && !value.isEmpty()) {
                    // if we have data in a datafield, create subfield blank (" ") with data
                    attrs = new LinkedList<>();
                    attrs.add(eventFactory.createAttribute(CODE_ATTRIBUTE, " "));
                    xmlEventConsumer.add(eventFactory.createStartElement(getSubfieldElement(), attrs.iterator(), namespaces));
                    xmlEventConsumer.add(eventFactory.createCharacters(transform(value)));
                    xmlEventConsumer.add(eventFactory.createEndElement(getSubfieldElement(), namespaces));
                }
                xmlEventConsumer.add(eventFactory.createEndElement(getDatafieldElement(), namespaces));
            }
        } catch (XMLStreamException e) {
            handleException(new IOException(e));
        }
    }

    @Override
    public void endRecord() {
        super.endRecord();
        if (exception != null) {
            return;
        }
        try {
            if (recordStarted) {
                xmlEventConsumer.add(eventFactory.createEndElement(getRecordElement(), namespaces));
                afterRecord();
                recordStarted = false;
            }
        } catch (XMLStreamException e) {
            handleException(new IOException(e));
        }
    }

    @Override
    public void endCollection() {
        super.endCollection();
        if (exception != null) {
            return;
        }
        try {
            if (collectionStarted) {
                xmlEventConsumer.add(eventFactory.createEndElement(getCollectionElement(), namespaces));
                collectionStarted = false;
            }
            if (xmlEventConsumer instanceof XMLEventWriter) {
                ((XMLEventWriter) xmlEventConsumer).flush();
            }
            flush();
        } catch (IOException | XMLStreamException e) {
            handleException(new IOException(e));
        }
    }

    @Override
    public void record(MarcRecord marcRecord) {
        if (exception != null) {
            return;
        }
        lock.lock();
        try {
            super.record(marcRecord);
            if (xmlEventConsumer instanceof XMLEventWriter) {
                ((XMLEventWriter) xmlEventConsumer).flush();
            }
        } catch (XMLStreamException e) {
            handleException(new IOException(e));
        } finally {
            lock.unlock();
        }
    }

    public void startCustomElement(String prefix, String uri, String localname) {
        try {
            xmlEventConsumer.add(eventFactory.createStartElement(prefix, uri, localname, null,
                    Collections.singletonList(eventFactory.createNamespace(prefix, uri)).iterator()));
        } catch (XMLStreamException e) {
            handleException(new IOException(e));
        }
    }

    public void endCustomElement(String prefix, String uri, String localname) {
        try {
            xmlEventConsumer.add(eventFactory.createEndElement(prefix, uri, localname));
        } catch (XMLStreamException e) {
            handleException(new IOException(e));
        }
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        isClosed = true;
        writer.close();
    }

    public boolean isClosed() {
        return isClosed;
    }

    public Exception getException() {
        return exception;
    }

    protected Namespace createNameSpace() {
        return eventFactory.createNamespace("", NAMESPACE_URI);
    }

    protected boolean createFormatAttribute() {
        return true;
    }

    protected boolean createTypeAttribute() {
        return true;
    }

    protected void writeSchema(List<Attribute> attrs) throws XMLStreamException {
        attrs.add(eventFactory.createAttribute("xmlns:xsi",
                XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI));
        attrs.add(eventFactory.createAttribute("xsi:schemaLocation",
                NAMESPACE_URI + " " + NAMESPACE_SCHEMA_LOCATION));
    }

    protected QName getCollectionElement() {
        return COLLECTION_ELEMENT;
    }

    protected QName getRecordElement() {
        return RECORD_ELEMENT;
    }

    protected QName getLeaderElement() {
        return LEADER_ELEMENT;
    }

    protected QName getControlfieldElement() {
        return CONTROLFIELD_ELEMENT;
    }

    protected QName getDatafieldElement() {
        return DATAFIELD_ELEMENT;
    }

    protected QName getSubfieldElement() {
        return SUBFIELD_ELEMENT;
    }

    /**
     * Split records if configured. A splitlimit of -1 prevents splitting.
     */
    private void afterRecord() {
        if (fileNamePattern != null) {
            if (splitlimit != -1) {
                if (getRecordCounter() % splitlimit == 0) {
                    try {
                        endCollection();
                        endDocument();
                        writer.close();
                        newWriter(fileNamePattern, fileNameCounter, bufferSize, compress);
                        setupEventConsumer(writer, indent);
                        startDocument();
                        beginCollection();
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            }
        }
    }

    private void newWriter(String fileNamePattern, AtomicInteger fileNameCounter,
                                    int bufferSize, boolean compress)
            throws IOException {
        String name = String.format(fileNamePattern, fileNameCounter.getAndIncrement());
        OutputStream out = Files.newOutputStream(Paths.get(name), StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
        writer = new OutputStreamWriter(compress ?
                new CompressedOutputStream(out, bufferSize) :
                new BufferedOutputStream(out, bufferSize), StandardCharsets.UTF_8);
    }

    private void setupEventConsumer(Writer writer, boolean indent) throws IOException {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        try {
            this.xmlEventConsumer = indent ?
                    new IndentingXMLEventWriter(outputFactory.createXMLEventWriter(writer)) :
                    outputFactory.createXMLEventWriter(writer);
            this.namespaces = Collections.singletonList(namespace).iterator();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    private String transform(String value) {
        return marcValueTransformers != null ? marcValueTransformers.transform(value) : value;
    }

    private void handleException(IOException e) {
        exception = e;
        if (fatalErrors) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * A GZIP output stream, modified for best compression.
     */
    private static class CompressedOutputStream extends GZIPOutputStream {

        CompressedOutputStream(OutputStream out, int size) throws IOException {
            super(out, size, true);
            def.setLevel(Deflater.BEST_COMPRESSION);
        }
    }
}
