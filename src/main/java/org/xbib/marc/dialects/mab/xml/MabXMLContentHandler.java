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
package org.xbib.marc.dialects.mab.xml;

import org.xbib.marc.MarcField;
import org.xbib.marc.label.RecordLabel;
import org.xbib.marc.xml.MarcContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The Sax-ContentHandler-based MAB XML handler can handle SaX event input
 * and fires events to a MarcXchange listener.
 */
public class MabXMLContentHandler extends MarcContentHandler implements MabXMLConstants {

    private Set<String> validNamespaces = new HashSet<>(Collections.singletonList(MABXML_NAMESPACE));

    @Override
    protected String getDefaultFormat() {
        return "MabXML";
    }

    @Override
    protected String getDefaultType() {
        return "h";
    }

    @Override
    public MabXMLContentHandler addNamespace(String uri) {
        this.validNamespaces.add(uri);
        return this;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        content.setLength(0);
        if (!isNamespace(uri)) {
            return;
        }
        switch (localName) {
            case DATEI: {
                beginCollection();
                break;
            }
            case DATENSATZ: {
                String type = null;
                for (int i = 0; i < atts.getLength(); i++) {
                    if (TYP.equals(atts.getLocalName(i))) {
                        type = atts.getValue(i);
                    }
                }
                if (type == null) {
                    type = this.type;
                }
                beginRecord(format, type);
                // create leader
                RecordLabel recordLabel = RecordLabel.builder().setIndicatorLength(1).setSubfieldIdentifierLength(0)
                        .build();
                leader(recordLabel.toString());
                break;
            }
            case FELD: {
                String tag = null;
                StringBuilder sb = new StringBuilder();
                sb.setLength(atts.getLength());
                for (int i = 0; i < atts.getLength(); i++) {
                    String name = atts.getLocalName(i);
                    if (NR.equals(name)) {
                        tag = atts.getValue(i);
                    }
                    if (name.startsWith(IND_ATTRIBUTE)) {
                        // 'ind', one char for one indicator
                        sb.setCharAt(0, atts.getValue(i).charAt(0));
                        sb.setLength(1);
                    }
                }
                MarcField.Builder builder = MarcField.builder().tag(tag);
                builder.indicator(sb.toString());
                stack.push(builder);
                break;
            }
            case UF: {
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
            case DATEI: {
                endCollection();
                break;
            }
            case DATENSATZ: {
                endRecord();
                break;
            }
            case FELD: {
                MarcField marcField = stack.pop().value(content.toString()).build();
                if (marcValueTransformers != null) {
                    marcField = marcValueTransformers.transformValue(marcField);
                }
                field(marcField);
                break;
            }
            case UF: {
                stack.peek().subfieldValue(content.toString());
                break;
            }
            default:
                break;
        }
        content.setLength(0);
    }

    @Override
    protected boolean isNamespace(String uri) {
        return validNamespaces.contains(uri);
    }
}
