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
package org.xbib.marc;

import static org.xbib.marc.MarcXchangeConstants.BIBLIOGRAPHIC_TYPE;
import static org.xbib.marc.MarcXchangeConstants.MARCXCHANGE_FORMAT;

import org.w3c.dom.Document;
import org.xbib.marc.dialects.aleph.AlephSequentialInputStream;
import org.xbib.marc.dialects.bibliomondo.BiblioMondoInputStream;
import org.xbib.marc.dialects.mab.diskette.MabDisketteInputStream;
import org.xbib.marc.dialects.pica.PicaInputStream;
import org.xbib.marc.dialects.pica.PicaPlainInputStream;
import org.xbib.marc.dialects.sisis.SisisInputStream;
import org.xbib.marc.io.BufferedSeparatorInputStream;
import org.xbib.marc.io.BytesReference;
import org.xbib.marc.io.Chunk;
import org.xbib.marc.io.ChunkStream;
import org.xbib.marc.label.RecordLabel;
import org.xbib.marc.label.RecordLabelFixer;
import org.xbib.marc.transformer.MarcTransformer;
import org.xbib.marc.transformer.field.MarcFieldTransformers;
import org.xbib.marc.transformer.value.MarcValueTransformers;
import org.xbib.marc.xml.InverseMarcContentHandler;
import org.xbib.marc.xml.MarcContentHandler;
import org.xbib.marc.xml.MarcXchangeEventConsumer;
import org.xbib.marc.xml.Sax2Dom;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

/**
 * A MARC instance for top-level fluent API style access to the most viable methods of
 * parsing, converting, transforming ISO 2709 and XML input streams.
 */
public final class Marc {

    private static final byte[] LF = { '\n'};

    private static final byte[] CRLF = { '\r', '\n'};

    private final Builder builder;

    private Marc(Builder builder) {
        this.builder = builder;
    }

    /**
     * Create a new MARC builder.
     * @return a new MARC builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Return an XML reader on a ISO 2709 input stream.
     * @return XML reader
     */
    public MarcIso2709Reader iso2709XmlReader() {
        return new MarcIso2709Reader(builder);
    }

    /**
     * Return an XML reader on an XML input stream.
     * @return XML reader
     */
    public MarcXmlReader xmlReader() {
        return new MarcXmlReader(builder);
    }

    /**
     * Run XML stream parser over an XML input stream, with an XML event consumer.
     * @param consumer the XML event consumer
     * @throws XMLStreamException if parsing fails
     */
    public void parseEvents(MarcXchangeEventConsumer consumer) throws XMLStreamException {
        Objects.requireNonNull(consumer);
        if (builder.getMarcListeners() != null) {
            for (Map.Entry<String, MarcListener> entry : builder.getMarcListeners().entrySet()) {
                consumer.setMarcListener(entry.getKey(), entry.getValue());
            }
        }
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader xmlEventReader = inputFactory.createXMLEventReader(builder.getInputStream());
        while (xmlEventReader.hasNext()) {
            consumer.add(xmlEventReader.nextEvent());
        }
        xmlEventReader.close();
    }

    /**
     * Return ISO 2709 stream.
     * @return ISO 2709 stream
     */
    public BufferedSeparatorInputStream iso2709Stream() {
        return builder.iso2709Stream();
    }

    /**
     * Return a MAB DISKETTE input stream with carriage-return/line-feed spearator.
     * @return the MAB DISKETTE input stream
     */
    public MabDisketteInputStream mabDisketteCRLF() {
        return builder.mabDiskette(CRLF);
    }

    /**
     * Return a MAB DISKETTE input stream with line-feed spearator.
     * @return the MAB DISKETTE input stream
     */
    public MabDisketteInputStream mabDisketteLF() {
        return builder.mabDiskette(LF);
    }

    /**
     * Return a MAB DISKETTE input stream with carriage-return/line-feed spearator and subfields.
     * @param subfieldDelimiter subfield delimiter
     * @return the MAB DISKETTE input stream
     */
    public MabDisketteInputStream mabDiskettePlusSubfieldsCRLF(char subfieldDelimiter) {
        return builder.mabDiskette(CRLF, subfieldDelimiter);
    }

    /**
     * Return a SISIS input stream with line-feed spearator.
     * @return the SISIS input stream
     */
    public SisisInputStream sisis() {
        return builder.sisis(LF);
    }

    /**
     * Return a SISIS input stream with carriage-return/line-feed spearator.
     * @return the SISIS input stream
     */
    public SisisInputStream sisisCRLF() {
        return builder.sisis(CRLF);
    }

    /**
     * Return an ALEPH SEQUENTIAL input stream with line-feed separator.
     * @return an ALEPH SEQUENTIAL input stream
     */
    public AlephSequentialInputStream aleph() {
        return builder.aleph(LF);
    }

    /**
     * Return a Pica input stream with line-feed separator.
     * @return Pica input stream
     */
    public PicaInputStream pica() {
        return builder.pica(LF);
    }

    /**
     * Return a Pica Plain input stream with line-feed separator.
     * @return Pica Plain input stream
     */
    public PicaPlainInputStream picaPlain() {
        return builder.picaPlain(LF);
    }

    /**
     * Return a BiblioMonod MARC export input stream. Default is carriage-return/line-feed spearator.
     * @return a BiblioMonod MARC export input stream
     */
    public BiblioMondoInputStream bibliomondo() {
        return builder.bibliomondo(CRLF);
    }

    /**
     * Return a W3C DOM of the record in the ISO 2709 input stream.
     * @return W3C DOM
     * @throws IOException if parsing fails
     */
    public Document document() throws IOException {
        return new Sax2Dom(iso2709XmlReader(), new InputSource(builder.getInputStream())).document();
    }

    /**
     * Transform W3C document of the record in the ISO 2709 input stream by an XSL stylesheet.
     * @param stylesheetUrl the URL of the stylesheet
     * @param result the result of the transformation
     * @throws IOException if transformation fails
     */
    public void transform(URL stylesheetUrl, Result result) throws IOException {
        transform(TransformerFactory.newInstance(), stylesheetUrl, result);
    }

    /**
     * Transform W3C document of the record in the ISO 2709 input stream by an XSL stylesheet.
     *
     * @param factory the transformer factory
     * @param stylesheetUrl the URL of the stylesheet
     * @param result the result of the transformation
     * @throws IOException if transformation fails
     */
    public void transform(TransformerFactory factory, URL stylesheetUrl, Result result)
            throws IOException {
        try (InputStream xslInputStream = stylesheetUrl.openStream()) {
            factory.newTemplates(new StreamSource(xslInputStream)).newTransformer()
                    .transform(new DOMSource(document()), result);
        } catch (TransformerException e) {
            throw new IOException(e);
        } finally {
            if (builder.getInputStream() != null) {
                // essential
                builder.getInputStream().close();
            }
        }
    }

    /**
     * Write MARC bibliographic data from seperator stream chunk by chunk to a MARC collection.
     * @throws IOException if writing fails
     */
    public void writeCollection() throws IOException {
        wrapIntoCollection(new BufferedSeparatorInputStream(builder.getInputStream()));
    }

    public void writeCollection(String type) throws IOException {
        wrapIntoCollection(type, new BufferedSeparatorInputStream(builder.getInputStream()));
    }

    public int wrapIntoCollection(ChunkStream<byte[], BytesReference> stream) throws IOException {
        return wrapFields(BIBLIOGRAPHIC_TYPE, stream, true);
    }

    public int wrapIntoCollection(String type, ChunkStream<byte[], BytesReference> stream) throws IOException {
        return wrapFields(type, stream, true);
    }

    /**
     * Pass a given chunk stream to a MARC generator, chunk by chunk.
     * Can process any MARC streams, not only separator streams.
     * @param stream a chunk stream
     * @param withCollection true if stream should be wrapped into a collection element
     * @return the number of chunks in the stream
     * @throws IOException if chunk reading fails
     */
    public int wrapFields(String type, ChunkStream<byte[], BytesReference> stream,
                                  boolean withCollection) throws IOException {
        int count = 0;
        MarcListener marcListener = builder.getMarcListener(type);
        if (marcListener == null) {
            return count;
        }
        try {
            if (withCollection) {
                // write XML declaration if required
                if (marcListener instanceof ContentHandler) {
                    ((ContentHandler) marcListener).startDocument();
                }
                marcListener.beginCollection();
            }
            if (builder.marcGenerator == null) {
                builder.marcGenerator = builder.createGenerator();
            }
            Chunk<byte[], BytesReference> chunk;
            while ((chunk = stream.readChunk()) != null) {
                builder.marcGenerator.chunk(chunk);
                count++;
            }
            stream.close();
            builder.marcGenerator.flush();
            if (withCollection) {
                marcListener.endCollection();
                if (marcListener instanceof ContentHandler) {
                    ((ContentHandler) marcListener).endDocument();
                }
            }
        } catch (SAXException e) {
            throw new IOException(e);
        } finally {
            if (builder.getInputStream() != null) {
                // essential
                builder.getInputStream().close();
            }
        }
        return count;
    }

    /**
     * Write MARC bibliographic events from a separator strem, record by record, wrapped into a
     * pair of {@code collection} elements.
     * @throws IOException if writing fails
     */
    public void writeRecordCollection() throws IOException {
        wrapRecords(new BufferedSeparatorInputStream(builder.getInputStream()), true);
    }

    /**
     * Write MARC bibliographic events from a separator strem, record by record.
     * @throws IOException if writing fails
     */
    public void writeRecords() throws IOException {
        wrapRecords(new BufferedSeparatorInputStream(builder.getInputStream()), false);
    }

    /**
     * Wrap records into a collection. Can process any MARC streams, not only
     * separator streams.
     * @param stream a chunk stream
     * @param withCollection true if {@code collection} elements should be used, false if not
     * @return the number of chunks in the stream
     * @throws IOException if chunk reading fails
     */
    public long wrapRecords(ChunkStream<byte[], BytesReference> stream,
                            boolean withCollection) throws IOException {
        final AtomicInteger l = new AtomicInteger();
        try {
            // keep reference to our record listener here, because builder will
            // enforce an internal record listener
            MarcRecordListener marcRecordListener = builder.getMarcRecordListener();
            if (withCollection) {
                // write XML declaration if required
                if (marcRecordListener instanceof ContentHandler) {
                    ((ContentHandler) marcRecordListener).startDocument();
                }
                marcRecordListener.beginCollection();
            }
            if (builder.marcGenerator == null) {
                builder.marcGenerator = builder.createGenerator();
            }
            // short-circuit: set MARC listener of the MARC generator to builder to capture records
            builder.marcGenerator.setMarcListener(builder);
            builder.marcGenerator.setCharset(builder.getCharset());
            builder.stream = stream;
            // voila: now we can stream records
            builder.recordStream().forEach(record -> {
                marcRecordListener.record(record);
                l.incrementAndGet();
            });
            stream.close();
            builder.marcGenerator.flush();
            if (withCollection) {
                marcRecordListener.endCollection();
                if (marcRecordListener instanceof ContentHandler) {
                    ((ContentHandler) marcRecordListener).endDocument();
                }
            }
        } catch (SAXException e) {
            throw new IOException(e);
        } finally {
            // we close input stream always
            if (builder.getInputStream() != null) {
                builder.getInputStream().close();
            }
        }
        return l.get();
    }

    /**
     * A XML reader for MARC XML.
     */
    public static class MarcXmlReader implements XMLReader {

        protected final Builder builder;

        private MarcXmlReader(Builder builder) {
            this.builder = builder;
        }

        @Override
        public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
            builder.setFeature(name, value);
        }

        @Override
        public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
            return builder.getFeature(name);
        }

        @Override
        public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
            builder.setProperty(name, value);
        }

        @Override
        public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
            return builder.getProperty(name);
        }

        @Override
        public EntityResolver getEntityResolver() {
            return null;
        }

        @Override
        public void setEntityResolver(EntityResolver resolver) {
            // ignore for now
        }

        @Override
        public DTDHandler getDTDHandler() {
            return null;
        }

        @Override
        public void setDTDHandler(DTDHandler handler) {
            // ignore for now
        }

        @Override
        public ErrorHandler getErrorHandler() {
            return null;
        }

        @Override
        public void setErrorHandler(ErrorHandler handler) {
            // ignore for now
        }

        @Override
        public void setContentHandler(ContentHandler handler) {
            builder.setContentHandler(handler);
        }

        @Override
        public ContentHandler getContentHandler() {
            return builder.getContentHandler();
        }

        /**
         * Parse MARC XML via SAX.
         *
         * @param inputSource the SAX input source
         * @throws IOException if parse fails
         */
        @Override
        public void parse(InputSource inputSource) throws IOException {
            MarcContentHandler handler = new MarcContentHandler();
            handler.setFormat(builder.getFormat() != null ? builder.getFormat() : MARCXCHANGE_FORMAT);
            handler.setType(builder.getType() != null ? builder.getType() : BIBLIOGRAPHIC_TYPE);
            if (builder.getMarcListeners() != null) {
                for (Map.Entry<String, MarcListener> entry : builder.getMarcListeners().entrySet()) {
                    handler.setMarcListener(entry.getKey(), entry.getValue());
                }
            }
            if (builder.getContentHandler() == null) {
                builder.setContentHandler(handler);
            }
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                SAXParser sax = factory.newSAXParser();
                if (builder.getFeatures() != null) {
                    for (Map.Entry<String, Boolean> entry : builder.getFeatures().entrySet()) {
                        sax.getXMLReader().setFeature(entry.getKey(), entry.getValue());
                    }
                }
                if (builder.getProperties() != null) {
                    for (Map.Entry<String, Object> entry : builder.getProperties().entrySet()) {
                        sax.getXMLReader().setProperty(entry.getKey(), entry.getValue());
                    }
                }
                sax.getXMLReader().setContentHandler(builder.getContentHandler());
                sax.getXMLReader().parse(inputSource);
            } catch (SAXException | ParserConfigurationException e) {
                throw new IOException(e);
            } finally {
                // close all available input streams
                if (builder.getInputStream() != null) {
                    builder.getInputStream().close();
                }
                if (inputSource.getCharacterStream() != null) {
                    inputSource.getCharacterStream().close();
                }
                if (inputSource.getByteStream() != null) {
                    inputSource.getByteStream().close();
                }
            }
        }

        @Override
        public void parse(String systemId) throws IOException, SAXException {
            parse(new InputSource(systemId));
        }

        public void parse() throws IOException {
            parse(false);
        }

        public void parse(boolean withCollection) throws IOException {
            if (withCollection) {
                builder.getMarcListener().beginCollection();
            }
            InputSource inputSource = new InputSource();
            inputSource.setByteStream(builder.getInputStream());
            inputSource.setEncoding(builder.getCharset().name());
            parse(inputSource);
            if (withCollection) {
                builder.getMarcListener().endCollection();
            }
        }
    }

    /**
     * Read from a ISO 2709 stream and emit SAX events.
     */
    public static class MarcIso2709Reader extends MarcXmlReader {

        private MarcIso2709Reader(Builder builder) {
            super(builder);
        }

        @Override
        public void parse(InputSource input) throws IOException {
            if (input.getByteStream() == null) {
                throw new IllegalArgumentException("no input stream found");
            }
            try (BufferedSeparatorInputStream stream = new BufferedSeparatorInputStream(input.getByteStream())) {
                MarcGenerator marcGenerator = builder.createGenerator();
                Chunk<byte[], BytesReference> chunk;
                while ((chunk = stream.readChunk()) != null) {
                    marcGenerator.chunk(chunk);
                }
                marcGenerator.flush();
            } finally {
                builder.getInputStream().close();
            }
        }
    }

    /**
     * A builder for MARC field streams, MARC records, and XML.
     */
    public static class Builder implements MarcXchangeConstants, MarcListener, MarcRecordListener {

        private InputStream inputStream;

        private Charset charset = StandardCharsets.UTF_8;

        private String schema;

        private String prefix;

        private InverseMarcContentHandler defaultContentHandler;

        private Map<String, Boolean> features = new HashMap<>();

        private Map<String, Object> properties = new HashMap<>();

        private Map<String, MarcListener> listeners = new HashMap<>();

        private MarcListener listener;

        private RecordLabelFixer recordLabelFixer;

        private MarcTransformer marcTransformer;

        private MarcValueTransformers marcValueTransformers;

        private MarcFieldTransformers marcFieldTransformers;

        private String format;

        private String type;

        private boolean fatalErrors = false;

        private MarcRecordListener marcRecordListener;

        private ChunkStream<byte[], BytesReference> stream;

        private List<MarcField> marcFieldList;

        private MarcRecord marcRecord;

        private RecordLabel recordLabel;

        private MarcGenerator marcGenerator;

        private boolean islightweightRecord;

        private Pattern keyPattern;

        private Pattern valuePattern;

        private Builder() {
            this.recordLabel = RecordLabel.EMPTY;
            this.marcFieldList = new LinkedList<>();
        }

        public Builder setFormat(String format) {
            this.format = format;
            if (marcGenerator != null) {
                marcGenerator.setFormat(format);
            }
            return this;
        }

        public String getFormat() {
            return format;
        }

        public Builder setType(String type) {
            this.type = type;
            if (marcGenerator != null) {
                marcGenerator.setType(type);
            }
            return this;
        }

        public String getType() {
            return type;
        }

        public Builder setSchema(String schema) {
            this.schema = schema;
            return this;
        }

        public Builder setPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * Set input stream for MARC instance.
         * @param inputStream the input stream
         * @return this builder
         */
        public Builder setInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
            return this;
        }

        /**
         * Return input stream.
         * @return input stream
         */
        public InputStream getInputStream() {
            return inputStream;
        }

        /**
         * Set the character set encoding.
         * @param charset the character set encoding of the MARC fields
         * @return charset
         */
        public Builder setCharset(Charset charset) {
            this.charset = charset;
            return this;
        }

        /**
         * Get character set encoding.
         * @return the character set encoding
         */
        public Charset getCharset() {
            return charset;
        }

        public Builder setMarcListener(MarcListener listener) {
            this.listeners.put(BIBLIOGRAPHIC_TYPE, listener);
            return this;
        }

        public MarcListener getMarcListener() {
            return listeners.get(BIBLIOGRAPHIC_TYPE);
        }

        public MarcListener getMarcListener(String type) {
            return listeners.get(type);
        }

        public Map<String, MarcListener> getMarcListeners() {
            return listeners;
        }

        /**
         * Set MARC listener for a specific bibliographic type.
         * @param type the  bibliographic type
         * @param listener the MARC listener
         * @return this builder
         */
        public Builder setMarcListener(String type, MarcListener listener) {
            this.listeners.put(type, listener);
            return this;
        }

        public Builder setMarcRecordListener(MarcRecordListener marcRecordListener) {
            this.marcRecordListener = marcRecordListener;
            return this;
        }

        public MarcRecordListener getMarcRecordListener() {
            return marcRecordListener;
        }

        public Builder setRecordLabelFixer(RecordLabelFixer recordLabelFixer) {
            this.recordLabelFixer = recordLabelFixer;
            return this;
        }

        /**
         * A custom transformer that helps while parsing unusual MARC field structures.
         * @param marcTransformer a MARC transformer
         * @return this builder
         */
        public Builder setMarcTransformer(MarcTransformer marcTransformer) {
            this.marcTransformer = marcTransformer;
            return this;
        }

        /**
         * Transform MARC field tags/indicators/subfield IDs on-the-fly.
         * @param marcFieldTransformers the MARC field transformers
         * @return this builder
         */
        public Builder setMarcFieldTransformers(MarcFieldTransformers marcFieldTransformers) {
            this.marcFieldTransformers = marcFieldTransformers;
            // set also for XML format parsers
            if (getContentHandler() instanceof MarcContentHandler) {
                MarcContentHandler marcContentHandler = (MarcContentHandler) getContentHandler();
                marcContentHandler.setMarcFieldTransformers(marcFieldTransformers);
            }
            return this;
        }

        /**
         * Tranform MARC field values on-the-fly.
         * @param marcValueTransformers the MARC field value transfomer
         * @return this builder
         */
        public Builder setMarcValueTransformers(MarcValueTransformers marcValueTransformers) {
            this.marcValueTransformers = marcValueTransformers;
            // set also for XML format parsers
            if (getContentHandler() instanceof MarcContentHandler) {
                MarcContentHandler marcContentHandler = (MarcContentHandler) getContentHandler();
                marcContentHandler.setMarcValueTransformers(marcValueTransformers);
            }
            return this;
        }

        public Builder setFatalErrors(boolean fatalErrors) {
            this.fatalErrors = fatalErrors;
            return this;
        }

        /**
         * Set XML content handler.
         * @param contentHandler the XML content handler
         * @return this adapter
         */
        public Builder setContentHandler(ContentHandler contentHandler) {
            if (contentHandler instanceof MarcContentHandler) {
                MarcContentHandler marcContentHandler = (MarcContentHandler) contentHandler;
                marcContentHandler.setMarcFieldTransformers(marcFieldTransformers);
            }
            this.defaultContentHandler = new InverseMarcContentHandler(contentHandler);
            defaultContentHandler.setSchema(schema);
            defaultContentHandler.setPrefix(prefix);
            defaultContentHandler.setFatalErrors(fatalErrors);
            return this;
        }

        /**
         * Get XML content handler.
         * @return XML content handler
         */
        public ContentHandler getContentHandler() {
            return defaultContentHandler != null ? defaultContentHandler.getContentHandler() : null;
        }

        /**
         * Create MARC generator with specified settings.
         * @return MARC generator
         */
        public MarcGenerator createGenerator() {
            this.marcGenerator = new MarcGenerator()
                    .setFormat(format)
                    .setType(type)
                    .setCharset(charset)
                    .setMarcListener(this)
                    .setFatalErrors(fatalErrors)
                    .setRecordLabelFixer(recordLabelFixer)
                    .setMarcTransformer(marcTransformer)
                    .setMarcFieldTransformers(marcFieldTransformers)
                    .setMarcValueTransformers(marcValueTransformers);
            return marcGenerator;
        }

        /**
         * Set feature.
         * @param name name of the feature
         * @param value true to enable feature, fals to disable feature
         */
        public void setFeature(String name, boolean value) {
            this.features.put(name, value);
        }

        /**
         * Get feature.
         * @param name name of the feature
         * @return true if feature is enabled, false if disabled
         */
        public Boolean getFeature(String name) {
            return this.features.get(name);
        }

        /**
         * Get all features.
         * @return features
         */
        public Map<String, Boolean> getFeatures() {
            return features;
        }

        /**
         * Set property.
         * @param name the property name
         * @param value the property value
         */
        public void setProperty(String name, Object value) {
            this.properties.put(name, value);
        }

        /**
         * Get property.
         * @param name the property name
         * @return the property value or null if not existent
         */
        public Object getProperty(String name) {
            return properties.get(name);
        }

        /**
         * Get all properties.
         * @return the properties
         */
        public Map<String, Object> getProperties() {
            return properties;
        }

        /**
         * Build a MARC instance.
         * @return a MARC instance
         */
        public Marc build() {
            return new Marc(this);
        }

        /**
         * Create an ISO 2709 stream.
         * @return ISO 2709 stream
         */
        public BufferedSeparatorInputStream iso2709Stream() {
            return new BufferedSeparatorInputStream(inputStream);
        }

        /**
         * Create MAB DISKETTE input stream with a given separator pattern.
         * @param pattern the separator pattern
         * @return MAB DISKETTE input stream
         */
        public MabDisketteInputStream mabDiskette(byte[] pattern) {
            return new MabDisketteInputStream(inputStream, pattern, createGenerator());
        }

        /**
         * Create MAB DISKETTE input stream with a given separator pattern and subfield delimiter.
         * @param pattern the separator pattern
         * @param subfieldDelimiter subfield delimiter
         * @return MAB DISKETTE input stream
         */
        public MabDisketteInputStream mabDiskette(byte[] pattern, char subfieldDelimiter) {
            return new MabDisketteInputStream(inputStream, pattern, subfieldDelimiter, createGenerator());
        }

        /**
         * Create SISIS input stream with a given separator pattern.
         * @param pattern the separator pattern
         * @return SISIS input stream
         */
        public SisisInputStream sisis(byte[] pattern) {
            return new SisisInputStream(inputStream, pattern, createGenerator());
        }

        /**
         * Create ALEPH SEQUENTIAL input stream a given separator pattern.
         * @param pattern the separator pattern
         * @return ALEPH SEQUENTIAL input stream
         */
        public AlephSequentialInputStream aleph(byte[] pattern) {
            return new AlephSequentialInputStream(inputStream, pattern, createGenerator());
        }

        /**
         * Create Pica input stream.
         * @param pattern the separator pattern
         * @return Pica input stream
         */
        public PicaInputStream pica(byte[] pattern) {
            return new PicaInputStream(inputStream, pattern, createGenerator());
        }

        /**
         * Create Pica plain input stream.
         * @param pattern the separator pattern
         * @return Pica input stream
         */
        public PicaPlainInputStream picaPlain(byte[] pattern) {
            return new PicaPlainInputStream(inputStream, pattern, createGenerator());
        }

        /**
         * Create BiblioMondo MARC input stream.
         * @param pattern the separator pattern
         * @return MARC tagged input stream
         */
        public BiblioMondoInputStream bibliomondo(byte[] pattern) {
            return new BiblioMondoInputStream(inputStream, pattern, createGenerator());
        }

        public Builder setKeyPattern(Pattern keyPattern) {
            this.keyPattern = keyPattern;
            return this;
        }

        public Builder setValuePattern(Pattern valuePattern) {
            this.valuePattern = valuePattern;
            return this;
        }

        /**
         * Not used as there is no known input with collection events yet.
         */
        @Override
        public void beginCollection() {
            // not used
        }

        @Override
        public void beginRecord(String format, String type) {
            this.listener = listeners.get(type != null ? type : BIBLIOGRAPHIC_TYPE);
            if (listener != null) {
                listener.beginRecord(format, type);
            }
            if (defaultContentHandler != null) {
                defaultContentHandler.beginRecord(format, type);
            }
            if (marcRecordListener != null) {
                setFormat(format);
                setType(type);
                marcFieldList = new LinkedList<>();
            }
        }

        @Override
        public void leader(String value) {
            if (listener != null) {
                listener.leader(value);
            }
            if (defaultContentHandler != null) {
                defaultContentHandler.leader(value);
            }
            if (marcRecordListener != null) {
                recordLabel(RecordLabel.builder().from(value.toCharArray()).build());
            }
        }

        @Override
        public void field(MarcField marcField) {
            if (listener != null) {
                listener.field(marcField);
            }
            if (defaultContentHandler != null) {
                defaultContentHandler.field(marcField);
            }
            if (marcRecordListener != null) {
                addField(marcField);
            }
        }

        @Override
        public void record(MarcRecord marcRecord) {
            this.marcRecord = marcRecord;
        }

        @Override
        public void endRecord() {
            if (listener != null) {
                listener.endRecord();
            }
            if (defaultContentHandler != null) {
                defaultContentHandler.endRecord();
            }
            if (marcRecordListener != null) {
                marcRecordListener.record(buildRecord());
            }
        }

        /**
         * Not used as there is no known input with collection events yet.
         */
        @Override
        public void endCollection() {
            // not used
        }

        public Marc.Builder recordLabel(RecordLabel recordLabel) {
            this.recordLabel = recordLabel;
            return this;
        }

        public RecordLabel getRecordLabel() {
            return recordLabel;
        }

        public Marc.Builder addField(MarcField marcField) {
            boolean keymatch = keyPattern == null || marcField.matchKey(keyPattern) != null;
            boolean valuematch = valuePattern == null || marcField.matchValue(valuePattern) != null;
            if (keymatch && valuematch) {
                this.marcFieldList.add(marcField);
            }
            return this;
        }

        public Marc.Builder lightweightRecord() {
            this.islightweightRecord = true;
            return this;
        }

        /**
         * Build MARC record.
         * @return MARC record
         */
        public MarcRecord buildRecord() {
            return new MarcRecord(format, type, recordLabel, marcFieldList, islightweightRecord);
        }

        /**
         * Iterator over specified MARC records.
         * @return a MARC record iterator
         */
        public Iterator<MarcRecord> recordIterator() {
            if (stream == null) {
                this.stream = new BufferedSeparatorInputStream(inputStream);
            }
            if (marcGenerator == null) {
                this.marcGenerator = createGenerator();
            }
            this.marcRecordListener = this;
            return new Iterator<MarcRecord>() {

                @Override
                public boolean hasNext() {
                    try {
                        MarcRecord record;
                        record(null);
                        Chunk<byte[], BytesReference> chunk;
                        while ((chunk = stream.readChunk()) != null) {
                            marcGenerator.chunk(chunk);
                            record = getMarcRecord();
                            if (record != null) {
                                return true;
                            }
                        }
                        marcGenerator.flush();
                        record = getMarcRecord();
                        if (record != null) {
                            return true;
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                    return false;
                }

                @Override
                public MarcRecord next() {
                    MarcRecord record = getMarcRecord();
                    if (record == null) {
                        throw new NoSuchElementException();
                    }
                    return record;
                }
            };
        }

        /**
         * For easy {@code for} statements.
         * @return iterable
         */
        public Iterable<MarcRecord> iterable() {
            return this::recordIterator;
        }

        /**
         * This methods creates a Java 8 stream of MARC records.
         * @return a stream of records
         */
        public Stream<MarcRecord> recordStream() {
            return StreamSupport.stream(iterable().spliterator(), false);
        }

        /**
         * Send chunk to MARC generator.
         * @param chunk chunk
         * @return this builder
         * @throws IOException if MARC generator fails
         */
        public Marc.Builder chunk(Chunk<byte[], BytesReference> chunk) throws IOException {
            marcGenerator.chunk(chunk);
            return this;
        }

        private MarcRecord getMarcRecord() {
            return marcRecord;
        }
    }
}
