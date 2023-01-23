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

import java.util.HashSet;
import org.xbib.marc.MarcField;
import org.xbib.marc.MarcListener;
import org.xbib.marc.MarcXchangeConstants;
import org.xbib.marc.label.RecordLabel;
import org.xbib.marc.transformer.value.MarcValueTransformers;

import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;

/**
 * The MarcXchange event consumer listens to StaX events and converts them to MarcXchange events.
 */
public class MarcXchangeEventConsumer implements XMLEventConsumer, MarcXchangeConstants, MarcListener {

    private final Deque<MarcField.Builder> stack;

    private final Map<String, MarcListener> marcListeners;

    private final StringBuilder content;

    private MarcValueTransformers marcValueTransformers;

    private MarcListener marcListener;

    private String format;

    private String type;

    private final Set<String> validNamespaces;

    private boolean endRecordReached;

    public MarcXchangeEventConsumer() {
        this.stack = new LinkedList<>();
        this.marcListeners = new HashMap<>();
        this.content = new StringBuilder();
        this.format = MARC21_FORMAT;
        this.type = BIBLIOGRAPHIC_TYPE;
        this.validNamespaces = new HashSet<>();
        this.validNamespaces.addAll(Set.of(MARCXCHANGE_V1_NS_URI, MARCXCHANGE_V2_NS_URI, MARC21_SCHEMA_URI));
        this.endRecordReached = false;
    }

    public MarcXchangeEventConsumer setMarcListener(String type, MarcListener listener) {
        this.marcListeners.put(type, listener);
        return this;
    }

    public MarcXchangeEventConsumer setMarcListener(MarcListener listener) {
        this.marcListeners.put(BIBLIOGRAPHIC_TYPE, listener);
        return this;
    }

    public MarcXchangeEventConsumer addNamespace(String uri) {
        this.validNamespaces.add(uri);
        return this;
    }

    public MarcXchangeEventConsumer setMarcValueTransformers(MarcValueTransformers marcValueTransformers) {
        this.marcValueTransformers = marcValueTransformers;
        return this;
    }

    @Override
    public void beginCollection() {
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
        this.marcListener = marcListeners.get(type != null ? type : BIBLIOGRAPHIC_TYPE);
        if (marcListener != null) {
            marcListener.beginRecord(format, type);
        }
    }

    @Override
    public void endRecord() {
        if (marcListener != null) {
            marcListener.endRecord();
        }
        this.endRecordReached = true;
    }

    @Override
    public void leader(RecordLabel label) {
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
        if (marcListener != null) {
            marcListener.field(field);
        }
    }

    @Override
    public void add(XMLEvent event) throws XMLStreamException {
        if (event.isStartElement()) {
            StartElement element = (StartElement) event;
            String uri = element.getName().getNamespaceURI();
            if (!validNamespaces.contains(uri)) {
                return;
            }
            String localName = element.getName().getLocalPart();
            Iterator<?> it = element.getAttributes();
            String thisformat = null;
            String thistype = null;
            String tag = null;
            String code = null;
            StringBuilder sb = new StringBuilder();
            sb.setLength(10);
            int min = 10;
            int max = 0;
            while (it.hasNext()) {
                Attribute attr = (Attribute) it.next();
                QName attributeName = attr.getName();
                String name = attributeName.getLocalPart();
                if (TAG_ATTRIBUTE.equals(name)) {
                    tag = attr.getValue();
                } else if (CODE_ATTRIBUTE.equals(name)) {
                    code = attr.getValue();
                } else if (name.startsWith(IND_ATTRIBUTE)) {
                    int pos = Integer.parseInt(name.substring(3));
                    if (pos >= 0 && pos < 10) {
                        char ind = attr.getValue().charAt(0);
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
                } else if (FORMAT_ATTRIBUTE.equals(name)) {
                    thisformat = attr.getValue();
                } else if (TYPE_ATTRIBUTE.equals(name)) {
                    thistype = attr.getValue();
                }
            }
            if (thisformat == null) {
                thisformat = this.format;
            }
            if (thistype == null) {
                thistype = this.type;
            }
            content.setLength(0);
            switch (localName) {
                case COLLECTION -> {
                    beginCollection();
                }
                case RECORD -> {
                    setFormat(thisformat);
                    setType(thistype);
                    beginRecord(thisformat, thistype);
                }
                case LEADER -> {
                }
                case CONTROLFIELD, DATAFIELD -> {
                    MarcField.Builder builder = MarcField.builder().tag(tag);
                    if (max > 0) {
                        builder.indicator(sb.substring(min - 1, max));
                    }
                    stack.push(builder);
                }
                case SUBFIELD -> {
                    stack.peek().subfield(code, null);
                }
                default -> {
                }
            }
        } else if (event.isEndElement()) {
            EndElement element = (EndElement) event;
            String uri = element.getName().getNamespaceURI();
            if (!validNamespaces.contains(uri)) {
                return;
            }
            String localName = element.getName().getLocalPart();
            switch (localName) {
                case COLLECTION -> {
                    endCollection();
                }
                case RECORD -> {
                    endRecord();
                }
                case LEADER -> {
                    leader(RecordLabel.builder().from(content.toString().toCharArray()).build());
                }
                case CONTROLFIELD -> {
                    field(transformValue(stack.pop().value(content.toString()).build()));
                }
                case DATAFIELD -> {
                    field(transformValue(stack.pop().build()));
                }
                case SUBFIELD -> {
                    stack.peek().subfieldValue(content.toString());
                }
                default -> {
                }
            }
            content.setLength(0);
        } else if (event.isCharacters()) {
            Characters c = (Characters) event;
            if (!c.isIgnorableWhiteSpace()) {
                content.append(c.getData());
            }
        } else if (event.isStartDocument()) {
            stack.clear();

        }
    }

    public String getFormat() {
        return format;
    }

    public MarcXchangeEventConsumer setFormat(String format) {
        this.format = format;
        return this;
    }

    public String getType() {
        return type;
    }

    public MarcXchangeEventConsumer setType(String type) {
        this.type = type;
        return this;
    }

    public boolean isEndRecordReached() {
        return endRecordReached;
    }

    public void resetEndRecordReached() {
        endRecordReached = false;
    }

    private MarcField transformValue(MarcField field) {
        return marcValueTransformers != null ? marcValueTransformers.transformValue(field) : field;
    }
}
