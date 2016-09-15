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
import org.xbib.marc.MarcRecordListener;
import org.xbib.marc.MarcXchangeConstants;
import org.xbib.marc.label.RecordLabel;
import org.xbib.marc.transformer.field.MarcFieldTransformers;
import org.xbib.marc.transformer.value.MarcValueTransformers;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Sax-ContentHandler-based MarcXchange handler can handle incoming SaX events
 * and fires events to a Marc listener.
 */
public class MarcContentHandler
        implements  MarcXchangeConstants, MarcListener, MarcRecordListener,
        EntityResolver, DTDHandler, ContentHandler, ErrorHandler {

    private static final Logger logger = Logger.getLogger(MarcContentHandler.class.getName());

    protected Deque<MarcField.Builder> stack = new LinkedList<>();

    protected Map<String, MarcListener> listeners = new HashMap<>();

    protected MarcListener marcListener;

    protected StringBuilder content = new StringBuilder();

    protected String format;

    protected String type;

    protected final AtomicInteger recordCounter = new AtomicInteger();

    protected MarcValueTransformers marcValueTransformers;

    private MarcFieldTransformers marcFieldTransformers;

    private boolean isCollection = false;

    private List<MarcField> marcFieldList = new LinkedList<>();


    private Set<String> validNamespaces =
            new HashSet<>(Arrays.asList(MARCXCHANGE_V1_NS_URI, MARCXCHANGE_V2_NS_URI, MARC21_SCHEMA_URI));

    protected String getDefaultFormat() {
        return MARC21_FORMAT;
    }

    protected String getDefaultType() {
        return BIBLIOGRAPHIC_TYPE;
    }

    /**
     * Set MARC listener for a specific record type.
     * @param type the record type
     * @param listener the MARC listener
     * @return this handler
     */
    public MarcContentHandler setMarcListener(String type, MarcListener listener) {
        this.listeners.put(type, listener);
        return this;
    }

    /**
     * Set MARC listener for the default record type.
     * @param listener the MARC listener
     * @return this handler
     */
    public MarcContentHandler setMarcListener(MarcListener listener) {
        this.listeners.put(this.type, listener);
        return this;
    }

    public MarcContentHandler addNamespace(String uri) {
        this.validNamespaces.add(uri);
        return this;
    }

    /**
     * Set MARC field value transformer for transforming field values.
     * @param marcValueTransformers the value transformer
     * @return this handler
     */
    public MarcContentHandler setMarcValueTransformers(MarcValueTransformers marcValueTransformers) {
        this.marcValueTransformers = marcValueTransformers;
        return this;
    }

    /**
     * Set MARC field transformers.
     * @param marcFieldTransformers the MARC field transformers
     * @return this handler
     */
    public MarcContentHandler setMarcFieldTransformers(MarcFieldTransformers marcFieldTransformers) {
        this.marcFieldTransformers = marcFieldTransformers;
        return this;
    }

    @Override
    public void beginCollection() {
        // early setup of MARC listener, even before beginRecord(format, type), it works only
        // if type is set to this handler.
        this.marcListener = listeners.get(this.type);
        if (marcListener != null) {
            marcListener.beginCollection();
        }
    }

    @Override
    public void endCollection() {
        if (marcListener != null) {
            marcListener.endCollection();
        }
    }

    @Override
    public void beginRecord(String format, String type) {
        this.marcListener = listeners.get(type != null ? type : this.type);
        if (marcListener != null) {
            marcListener.beginRecord(format, type);
        }
    }

    @Override
    public void leader(String label) {
        if (marcListener != null) {
            marcListener.leader(label);
        }
    }

    @Override
    public void field(MarcField marcField) {
        MarcField field = marcField;
        if (marcValueTransformers != null) {
            field = marcValueTransformers.transformValue(field);
        }
        if (marcFieldTransformers != null) {
            marcFieldList.add(marcField);
        } else {
            if (!marcField.isEmpty() && marcListener != null) {
                marcListener.field(field);
            }
        }
    }

    @Override
    public void record(MarcRecord marcRecord) {
        try {
            beginRecord(marcRecord.getFormat(), marcRecord.getType());
            leader(marcRecord.getRecordLabel().toString());
            for (MarcField marcField : marcRecord.getFields()) {
                field(marcField);
            }
            endRecord();
        } finally {
            recordCounter.incrementAndGet();
        }
    }

    @Override
    public void endRecord() {
        if (marcFieldTransformers != null) {
            for (MarcField marcField : marcFieldTransformers.transform(marcFieldList)) {
                if (!marcField.isEmpty() && marcListener != null) {
                    marcListener.field(marcField);
                }
            }
            marcFieldTransformers.reset();
            marcFieldList.clear();
        }
        if (marcListener != null) {
            marcListener.endRecord();
        }
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        // not in use yet
    }

    @Override
    public void startDocument() throws SAXException {
        stack.clear();
    }

    @Override
    public void endDocument() throws SAXException {
        // nothing to do here
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        // ignore all mappings
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        // ignore all mappings
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        content.setLength(0);
        if (!isNamespace(uri)) {
            return;
        }
        switch (localName) {
            case COLLECTION: {
                if (!isCollection) {
                    beginCollection();
                    isCollection = true;
                }
                break;
            }
            case RECORD: {
                String thisformat = null;
                String thistype = null;
                for (int i = 0; i < atts.getLength(); i++) {
                    if (FORMAT_ATTRIBUTE.equals(atts.getLocalName(i))) {
                        thisformat = atts.getValue(i);
                    } else if (TYPE_ATTRIBUTE.equals(atts.getLocalName(i))) {
                        thistype = atts.getValue(i);
                    }
                }
                if (thisformat == null) {
                    thisformat = this.format;
                }
                if (thistype == null) {
                    thistype = this.type;
                }
                beginRecord(thisformat, thistype);
                break;
            }
            case LEADER: {
                break;
            }
            case CONTROLFIELD: // fall-through
            case DATAFIELD: {
                String tag = null;
                StringBuilder sb = new StringBuilder();
                sb.setLength(atts.getLength());
                int min = atts.getLength();
                int max = 0;
                for (int i = 0; i < atts.getLength(); i++) {
                    String name = atts.getLocalName(i);
                    if (TAG_ATTRIBUTE.equals(name)) {
                        tag = atts.getValue(i);
                    }
                    if (name.startsWith(IND_ATTRIBUTE)) {
                        int pos = Integer.parseInt(name.substring(3));
                        if (pos >= 0 && pos < atts.getLength()) {
                            char ind = atts.getValue(i).charAt(0);
                            if (ind == '-') {
                                ind = ' '; // replace illegal '-' symbols
                            }
                            sb.setCharAt(pos - 1, ind);
                            if (pos < min) {
                                min = pos;
                            }
                            if (pos > max) {
                                max = pos;
                            }
                        }
                    }
                }
                MarcField.Builder builder = MarcField.builder().tag(tag);
                if (max > 0) {
                    builder.indicator(sb.substring(min - 1, max));
                }
                stack.push(builder);
                break;
            }
            case SUBFIELD: {
                stack.peek().subfield(atts.getValue(CODE_ATTRIBUTE), null);
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (!isNamespace(uri)) {
            return;
        }
        switch (localName) {
            case COLLECTION: {
                if (isCollection) {
                    endCollection();
                    isCollection = false;
                }
                break;
            }
            case RECORD: {
                endRecord();
                break;
            }
            case LEADER: {
                leader(RecordLabel.builder().from(content.toString().toCharArray()).build().toString());
                break;
            }
            case CONTROLFIELD: {
                MarcField marcField = stack.pop().value(content.toString()).build();
                if (marcValueTransformers != null) {
                    marcField = marcValueTransformers.transformValue(marcField);
                }
                field(marcField);
                break;
            }
            case DATAFIELD: {
                MarcField marcField = stack.pop().build();
                if (marcValueTransformers != null) {
                    marcField = marcValueTransformers.transformValue(marcField);
                }
                field(marcField);
                break;
            }
            case SUBFIELD: {
                stack.peek().subfieldValue(content.toString());
                break;
            }
            default: {
                break;
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        content.append(ch, start, length);
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        // ignore
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        // ignore
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
        // ignore
    }

    @Override
    public void notationDecl(String name, String publicId, String systemId) throws SAXException {
        // ignore
    }

    @Override
    public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException {
        // ignore
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        // do not resolve any entities
        return null;
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        logger.log(Level.WARNING, exception.getMessage(), exception);
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        logger.log(Level.SEVERE, exception.getMessage(), exception);
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        logger.log(Level.SEVERE, exception.getMessage(), exception);
    }

    public String getFormat() {
        return format;
    }

    public MarcContentHandler setFormat(String format) {
        if (format != null) {
            this.format = format;
        }
        return this;
    }

    public String getType() {
        return type;
    }

    public MarcContentHandler setType(String type) {
        if (type != null) {
            this.type = type;
        }
        return this;
    }

    public int getRecordCounter() {
        return recordCounter.get();
    }

    protected boolean isNamespace(String uri) {
        return validNamespaces.contains(uri);
    }
}
