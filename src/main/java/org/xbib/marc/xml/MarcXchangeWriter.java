/*
   Copyright 2016 Jörg Prante

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.xbib.marc.xml;

import org.xbib.marc.MarcField;
import org.xbib.marc.MarcListener;
import org.xbib.marc.MarcRecord;
import org.xbib.marc.transformer.value.MarcValueTransformers;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.util.XMLEventConsumer;

/**
 * This MarcXchange Writer writes MarcXchange events to a StaX XML output stream or XML event consumer.
 * Default output format is MarcXchange.
 */
public class MarcXchangeWriter extends MarcContentHandler implements Flushable, Closeable {

    private static final Logger logger = Logger.getLogger(MarcXchangeWriter.class.getName());

    private static final String NAMESPACE_URI = MARCXCHANGE_V2_NS_URI;

    private static final String NAMESPACE_SCHEMA_LOCATION = MARCXCHANGE_V2_0_SCHEMA_LOCATION;

    private static final QName COLLECTION_ELEMENT = new QName(NAMESPACE_URI, COLLECTION, "");

    private static final QName RECORD_ELEMENT = new QName(NAMESPACE_URI, RECORD, "");

    private static final QName LEADER_ELEMENT = new QName(NAMESPACE_URI, LEADER, "");

    private static final QName CONTROLFIELD_ELEMENT = new QName(NAMESPACE_URI, CONTROLFIELD, "");

    private static final QName DATAFIELD_ELEMENT = new QName(NAMESPACE_URI, DATAFIELD, "");

    private static final QName SUBFIELD_ELEMENT = new QName(NAMESPACE_URI, SUBFIELD, "");

    private final XMLEventFactory eventFactory;

    private final Namespace namespace;

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

    private MarcValueTransformers marcValueTransformers;

    private String fileNamePattern;

    private int fileNameCounter;

    private long recordCounter;

    private int splitlimit;

    private final Lock lock = new ReentrantLock();

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
        this.documentStarted = false;
        this.collectionStarted = false;
        eventFactory = XMLEventFactory.newInstance();
        namespace = eventFactory.createNamespace("", NAMESPACE_URI);
        setupEventConsumer(writer, indent);
    }

    /**
     * Create a MarcXchange writer in "split writer" mode.
     * @param indent if true, indent MarcXchange output
     * @param fileNamePattern file name pattern
     * @param splitlimit split write limit
     * @throws IOException if writer can not be created
     */
    public MarcXchangeWriter(boolean indent, String fileNamePattern, int splitlimit) throws IOException {
        this.fileNameCounter = 0;
        this.fileNamePattern = fileNamePattern;
        this.splitlimit = splitlimit;
        this.recordCounter = 0L;
        this.writer = newWriter(fileNamePattern, fileNameCounter);
        this.indent = indent;
        this.documentStarted = false;
        this.collectionStarted = false;
        this.eventFactory = XMLEventFactory.newInstance();
        this.namespace = eventFactory.createNamespace("", NAMESPACE_URI);
        setupEventConsumer(writer, indent);
    }

    /**
     * Write MarcXchange to an XML event consumer.
     *
     * @param consumer an XML event consumer
     */
    public MarcXchangeWriter(XMLEventConsumer consumer) {
        this.xmlEventConsumer = consumer;
        this.eventFactory = XMLEventFactory.newInstance();
        this.namespace = eventFactory.createNamespace("", NAMESPACE_URI);
        this.namespaces = Collections.singletonList(namespace).iterator();
    }

    private static Writer newWriter(String fileNamePattern, int fileNameCounter) throws IOException {
        return Files.newBufferedWriter(Paths.get(String.format(fileNamePattern, fileNameCounter)));
    }

    private void setupEventConsumer(Writer writer, boolean indent) throws IOException {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        try {
            outputFactory.setProperty("com.ctc.wstx.useDoubleQuotesInXmlDecl", Boolean.TRUE);
        } catch (IllegalArgumentException e) {
            logger.log(Level.FINEST, e.getMessage(), e);
        }
        try {
            this.xmlEventConsumer = indent ?
                    new IndentingXMLEventWriter(outputFactory.createXMLEventWriter(writer)) :
                    outputFactory.createXMLEventWriter(writer);
            this.namespaces = Collections.singletonList(namespace).iterator();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
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

    public MarcXchangeWriter setMarcValueTransformers(MarcValueTransformers marcValueTransformers) {
        this.marcValueTransformers = marcValueTransformers;
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
                Iterator<Attribute> attrs = schemaWritten ? null : Arrays.asList(
                        eventFactory.createAttribute("xmlns:xsi",
                                XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI),
                        eventFactory.createAttribute("xsi:schemaLocation",
                                NAMESPACE_URI + " " + NAMESPACE_SCHEMA_LOCATION)
                ).iterator();
                xmlEventConsumer.add(eventFactory.createStartElement(COLLECTION_ELEMENT, attrs, namespaces));
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
                String realformat = getFormat() != null ? getFormat() : format != null ? format : getDefaultFormat();
                attrs.add(eventFactory.createAttribute(FORMAT_ATTRIBUTE, realformat));
                String realtype = getType() != null ? getType() : type != null ? type : getDefaultType();
                attrs.add(eventFactory.createAttribute(TYPE_ATTRIBUTE, realtype));
                if (!schemaWritten) {
                    attrs.add(eventFactory.createAttribute("xmlns:xsi",
                            XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI));
                    attrs.add(eventFactory.createAttribute("xsi:schemaLocation",
                            NAMESPACE_URI + " " + NAMESPACE_SCHEMA_LOCATION));
                    schemaWritten = true;
                }
                xmlEventConsumer.add(eventFactory.createStartElement(RECORD_ELEMENT, attrs.iterator(), namespaces));
                recordStarted = true;
            }
        } catch (XMLStreamException e) {
            handleException(new IOException(e));
        }
    }

    @Override
    public void leader(String label) {
        super.leader(label);
        if (exception != null) {
            return;
        }
        if (label == null) {
            return;
        }
        try {
            xmlEventConsumer.add(eventFactory.createStartElement(LEADER_ELEMENT, null, namespaces));
            xmlEventConsumer.add(eventFactory.createCharacters(label));
            xmlEventConsumer.add(eventFactory.createEndElement(LEADER_ELEMENT, namespaces));
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
            if (field.isControl() && field.getValue() != null) {
                Iterator<Attribute> attrs =
                        Collections.singletonList(eventFactory.createAttribute(TAG_ATTRIBUTE,
                                transform(field.getTag()))).iterator();
                xmlEventConsumer.add(eventFactory.createStartElement(CONTROLFIELD_ELEMENT, attrs, namespaces));
                String value = field.getValue();
                if (value != null && !value.isEmpty()) {
                    xmlEventConsumer.add(eventFactory.createCharacters(transform(value)));
                }
                xmlEventConsumer.add(eventFactory.createEndElement(CONTROLFIELD_ELEMENT, namespaces));
            } else if (!field.isEmpty()) {
                String tag = field.getTag();
                String indicator = field.getIndicator();
                String ind1 = indicator != null && indicator.length() > 0 ? indicator.substring(0, 1) : " ";
                String ind2 = indicator != null && indicator.length() > 1 ? indicator.substring(1, 2) : " ";
                List<Attribute> attrs = new LinkedList<>();
                attrs.add(eventFactory.createAttribute(TAG_ATTRIBUTE, transform(tag)));
                attrs.add(eventFactory.createAttribute(IND_ATTRIBUTE + "1", transform(ind1)));
                attrs.add(eventFactory.createAttribute(IND_ATTRIBUTE + "2", transform(ind2)));
                xmlEventConsumer.add(eventFactory.createStartElement(DATAFIELD_ELEMENT, attrs.iterator(), namespaces));
                for (MarcField.Subfield subfield : field.getSubfields()) {
                    String code = subfield.getId();
                    // From https://www.loc.gov/standards/iso25577/ISO_DIS_25577_2(E)070727.doc
                    // "There is one restriction. A special mode (identifier length = 0) of ISO 2709 operates with
                    // data fields without subfields. In the MarcXchange schema subfields are required,
                    // i.e. identifier length = 0 is not supported."
                    // We support it! A subfield ID of length 0 will be substituted by "a"
                    if (code.isEmpty()) {
                        code = "a";
                    }
                    List<Attribute> subfieldattrs = new LinkedList<>();
                    subfieldattrs.add(eventFactory.createAttribute(CODE_ATTRIBUTE, transform(code)));
                    xmlEventConsumer.add(eventFactory.createStartElement(SUBFIELD_ELEMENT,
                            subfieldattrs.iterator(), namespaces));
                    xmlEventConsumer.add(eventFactory.createCharacters(transform(subfield.getValue())));
                    xmlEventConsumer.add(eventFactory.createEndElement(SUBFIELD_ELEMENT, namespaces));
                }
                String value = field.getValue();
                if (value != null && !value.isEmpty()) {
                    // if we have data in a datafield, create subfield "a" with data
                    attrs = new LinkedList<>();
                    attrs.add(eventFactory.createAttribute(CODE_ATTRIBUTE, "a"));
                    xmlEventConsumer.add(eventFactory.createStartElement(SUBFIELD_ELEMENT, attrs.iterator(), namespaces));
                    xmlEventConsumer.add(eventFactory.createCharacters(transform(value)));
                    xmlEventConsumer.add(eventFactory.createEndElement(SUBFIELD_ELEMENT, namespaces));
                }
                xmlEventConsumer.add(eventFactory.createEndElement(DATAFIELD_ELEMENT, namespaces));
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
                xmlEventConsumer.add(eventFactory.createEndElement(RECORD_ELEMENT, namespaces));
                afterRecord();
                flush();
                recordStarted = false;
            }
        } catch (IOException | XMLStreamException e) {
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
                xmlEventConsumer.add(eventFactory.createEndElement(COLLECTION_ELEMENT, namespaces));
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
            flush();
        } catch (IOException | XMLStreamException e) {
            handleException(new IOException(e));
        } finally {
            lock.unlock();
        }
    }

    public void startCustomElement(String prefix, String uri, String localname) {
        try {
            Namespace namespace = eventFactory.createNamespace(prefix, uri);
            xmlEventConsumer.add(eventFactory.createStartElement(prefix, uri, localname, null,
                    Collections.singletonList(namespace).iterator()));
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
        writer.close();
    }

    public Exception getException() {
        return exception;
    }

    /**
     * Split records, if configured.
     */
    protected void afterRecord() {
        if (fileNamePattern != null) {
            recordCounter++;
            if (recordCounter > splitlimit) {
                try {
                    endCollection();
                    writer.close();
                    writer = newWriter(fileNamePattern, fileNameCounter++);
                    setupEventConsumer(writer, indent);
                    beginCollection();
                    recordCounter = 0L;
                } catch (IOException e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            }
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
}
