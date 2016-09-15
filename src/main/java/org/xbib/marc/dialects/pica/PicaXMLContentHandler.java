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
package org.xbib.marc.dialects.pica;

import org.xbib.marc.MarcField;
import org.xbib.marc.label.RecordLabel;
import org.xbib.marc.xml.MarcContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The Sax-ContentHandler-based Pica XML handler can handle SaX event input
 * and fires events to a MarcXchange listener.
 */
public class PicaXMLContentHandler extends MarcContentHandler implements PicaConstants {

    private Set<String> validNamespaces =
            new HashSet<>(Arrays.asList(PICAXML_NAMESPACE, SRW_PICAXML_NAMESPACE));

    @Override
    protected String getDefaultFormat() {
        return "Pica";
    }

    @Override
    protected String getDefaultType() {
        return "XML";
    }

    @Override
    public void beginRecord(String format, String type) {
        this.marcListener = listeners.get(type != null ? type : this.type);
        if (marcListener != null) {
            marcListener.beginRecord(format, type);
        }
    }

    @Override
    public void field(MarcField marcField) {
        MarcField field = marcField;
        if (marcValueTransformers != null) {
            field = marcValueTransformers.transformValue(field);
        }
        if (marcListener != null) {
            marcListener.field(field);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        content.setLength(0);
        if (!isNamespace(uri)) {
            return;
        }
        switch (localName) {
            case RECORD: {
                beginRecord(format, type);
                // create leader
                RecordLabel recordLabel = RecordLabel.builder().setIndicatorLength(1)
                        .setSubfieldIdentifierLength(0).build();
                leader(recordLabel.toString());
                break;
            }
            case DATAFIELD: {
                String tag = null;
                String indicator = null;
                for (int i = 0; i < atts.getLength(); i++) {
                    String name = atts.getLocalName(i);
                    String value = atts.getValue(i);
                    if (TAG_ATTRIBUTE.equals(name)) {
                        tag = value.substring(0, 3);
                        indicator = value.substring(3);
                    }

                }
                MarcField.Builder builder = MarcField.builder().tag(tag).indicator(indicator);
                stack.push(builder);
                break;
            }
            case TAG_ATTRIBUTE: {
                String tag = null;
                String indicator = null;
                for (int i = 0; i < atts.getLength(); i++) {
                    String name = atts.getLocalName(i);
                    String value = atts.getValue(i);
                    if (ID_ATTRIBUTE.equals(name)) {
                        tag = value.substring(0, 3);
                        indicator = value.substring(3);
                    }

                }
                MarcField.Builder builder = MarcField.builder().tag(tag).indicator(indicator);
                stack.push(builder);
                break;
            }
            case SUBFIELD: {
                stack.peek().subfield(atts.getValue(CODE_ATTRIBUTE), null);
                break;
            }
            case SUBF_TAG: {
                stack.peek().subfield(atts.getValue(ID_ATTRIBUTE), null);
                break;
            }
            case GLOBAL_TAG:
                break;
            default:
                throw new IllegalArgumentException("unknown begin element: " +
                        uri + " " + localName + " " + qName + " atts=" + atts.toString());
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (!isNamespace(uri)) {
            return;
        }
        switch (localName) {
            case RECORD: {
                endRecord();
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
            case TAG_ATTRIBUTE: {
                MarcField marcField = stack.pop().value(content.toString()).build();
                if (marcValueTransformers != null) {
                    marcField = marcValueTransformers.transformValue(marcField);
                }
                field(marcField);
                break;
            }
            case SUBFIELD:
            case SUBF_TAG: {
                stack.peek().subfieldValue(content.toString());
                break;
            }
            case GLOBAL_TAG:
                break;
            default:
                throw new IllegalArgumentException("unknown end element: " + uri + " " + localName + " " + qName);
        }
        content.setLength(0);
    }

    @Override
    protected boolean isNamespace(String uri) {
        return validNamespaces.contains(uri);
    }
}
