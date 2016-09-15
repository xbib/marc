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
import org.xbib.marc.MarcXchangeConstants;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * The inverse MARC content handler receives MARC events and fires XML SAX events to a content handler.
 */
public class InverseMarcContentHandler implements MarcListener, MarcXchangeConstants {

    private static final String EMPTY_STRING = "";

    private static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();

    private static final String CDATA = "CDATA";

    private static final String NS_PREFIX = "xmlns";

    private static final String NS_URI = "http://www.w3.org/2000/xmlns/";

    private static final String XSI_NS_URI = "http://www.w3.org/2001/XMLSchema-instance";

    private static final String XSI_NS_PREFIX = "xsi";

    private final ContentHandler contentHandler;

    private String nsUri = MARCXCHANGE_V2_NS_URI;

    private String schema = MARCXCHANGE_FORMAT;

    private String prefix = null;

    private boolean fatalErrors = false;

    private boolean schemaWritten = false;

    private Exception exception;

    /**
     * Create an {@link InverseMarcContentHandler} with an underlying XML content handler.
     * @param contentHandler the underlying XML content handler
     */
    public InverseMarcContentHandler(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
        Objects.requireNonNull(contentHandler);
    }

    /**
     * Return underlying XML content handler.
     * @return XML content handler
     */
    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    public InverseMarcContentHandler setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    public InverseMarcContentHandler setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public InverseMarcContentHandler setFatalErrors(Boolean fatalerrors) {
        this.fatalErrors = fatalerrors;
        return this;
    }

    @Override
    public void beginCollection() {
        // nothing to do here
    }

    @Override
    public void beginRecord(String format, String type) {
        try {
            contentHandler.startDocument();
            AttributesImpl attrs = new AttributesImpl();
            writeSchema(attrs);
            if (format != null && !MARC21_FORMAT.equalsIgnoreCase(schema)) {
                attrs.addAttribute(nsUri, FORMAT_ATTRIBUTE, prefix(FORMAT_ATTRIBUTE), CDATA, format);
            }
            if (type != null) {
                attrs.addAttribute(nsUri, TYPE_ATTRIBUTE, prefix(TYPE_ATTRIBUTE), CDATA, type);
            }
            contentHandler.startElement(nsUri, RECORD, prefix(RECORD), attrs);
        } catch (SAXException e) {
            handleException(new IOException(e));
        }
    }

    @Override
    public void leader(String label) {
        try {
            contentHandler.startElement(nsUri, LEADER, prefix(LEADER), EMPTY_ATTRIBUTES);
            contentHandler.characters(label.toCharArray(), 0, label.length());
            contentHandler.endElement(nsUri, LEADER, prefix(LEADER));
        } catch (SAXException e) {
            handleException(new IOException(e));
        }
    }

    @Override
    public void field(MarcField field) {
        try {
            if (field.isControl()) {
                AttributesImpl attrs = new AttributesImpl();
                attrs.addAttribute(EMPTY_STRING, TAG_ATTRIBUTE, prefix(TAG_ATTRIBUTE), CDATA, field.getTag());
                contentHandler.startElement(nsUri, CONTROLFIELD, prefix(CONTROLFIELD), attrs);
                String value = field.getValue();
                if (value != null && !value.isEmpty()) {
                    switch (field.getTag()) {
                        case "006":
                        case "007":
                        case "008":
                            // fix wrong fill characters here
                            value = value.replace('^', '|');
                            break;
                        default:
                            break;
                    }
                    contentHandler.characters(value.toCharArray(), 0, value.length());
                }
                contentHandler.endElement(nsUri, CONTROLFIELD, prefix(CONTROLFIELD));
            } else {
                String tag = field.getTag();
                AttributesImpl attrs = new AttributesImpl();
                attrs.addAttribute(EMPTY_STRING, TAG_ATTRIBUTE, prefix(TAG_ATTRIBUTE), CDATA, tag);
                String indicator = field.getIndicator();
                if (indicator != null) {
                    int ind = indicator.length();
                    // force at least two default blank indicators if schema is MARC21
                    if (MARC21_FORMAT.equalsIgnoreCase(schema)) {
                        for (int i = ind == 0 ? 1 : ind; i <= 2; i++) {
                            attrs.addAttribute(EMPTY_STRING, IND_ATTRIBUTE + i, prefix(IND_ATTRIBUTE + i), CDATA, " ");
                        }
                    }
                    // set indicators
                    for (int i = 1; i <= ind; i++) {
                        attrs.addAttribute(EMPTY_STRING, IND_ATTRIBUTE + i, prefix(IND_ATTRIBUTE + i),
                                CDATA, indicator.substring(i - 1, i));
                    }
                }
                contentHandler.startElement(nsUri, DATAFIELD, prefix(DATAFIELD), attrs);
                // subfields
                for (MarcField.Subfield subfield : field.getSubfields()) {
                    AttributesImpl subfieldAttrs = new AttributesImpl();
                    String value = subfield.getValue();
                    if (value != null && !value.isEmpty()) {
                        String subfieldId = subfield.getId();
                        if (subfieldId == null || subfieldId.length() == 0) {
                            subfieldId = "a"; // fallback
                        }
                        subfieldAttrs.addAttribute(EMPTY_STRING, CODE_ATTRIBUTE, prefix(CODE_ATTRIBUTE), CDATA, subfieldId);
                        contentHandler.startElement(nsUri, SUBFIELD, prefix(SUBFIELD), subfieldAttrs);
                        contentHandler.characters(value.toCharArray(), 0, value.length());
                        contentHandler.endElement(nsUri, SUBFIELD, prefix(SUBFIELD));
                    }
                }
                contentHandler.endElement(nsUri, DATAFIELD, prefix(DATAFIELD));
            }
        } catch (SAXException e) {
            handleException(new IOException(e));
        }
    }

    @Override
    public void endRecord() {
        try {
            contentHandler.endElement(nsUri, RECORD, prefix(RECORD));
            if (prefix != null) {
                contentHandler.endPrefixMapping(prefix);
            }
            contentHandler.endDocument();
        } catch (SAXException e) {
            handleException(new IOException(e));
        }
    }

    @Override
    public void endCollection() {
        // nothing to do here
    }

    public Exception getException() {
        return exception;
    }

    private String prefix(String s) {
        return prefix != null ? prefix + ":" + s : s;
    }

    private void writeSchema(AttributesImpl attrs) throws SAXException {
        if (!schemaWritten) {
            String schemaLocation;
            if (MARC21_FORMAT.equalsIgnoreCase(schema)) {
                this.nsUri = MARC21_SCHEMA_URI;
                schemaLocation = MARC21_SCHEMA_URI + " " + MARC21_SCHEMA_LOCATION;

            } else {
                this.nsUri = MARCXCHANGE_V2_NS_URI;
                schemaLocation = MARCXCHANGE_V2_NS_URI + " " + MARCXCHANGE_V2_0_SCHEMA_LOCATION;
            }
            if (prefix != null) {
                contentHandler.startPrefixMapping(prefix, nsUri);
            }
            attrs.addAttribute(NS_URI, XSI_NS_PREFIX, NS_PREFIX + ":" + XSI_NS_PREFIX, CDATA, XSI_NS_URI);
            attrs.addAttribute(XSI_NS_URI, "schemaLocation", XSI_NS_PREFIX + ":schemaLocation", CDATA, schemaLocation);
            schemaWritten = true;
        }
    }

    private void handleException(IOException e) {
        exception = e;
        if (fatalErrors) {
            throw new UncheckedIOException(e);
        }
    }
}
