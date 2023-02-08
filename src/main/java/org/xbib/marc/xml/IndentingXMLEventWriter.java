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

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IndentingXMLEventWriter implements XMLEventWriter {

    private static final Logger logger = Logger.getLogger(IndentingXMLEventWriter.class.getName());

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private final Map<Integer, String> indentCache = new HashMap<>();

    private final XMLEventFactory xmlEventFactory;

    private final Deque<Set<State>> scopeState = new LinkedList<>();

    private final XMLEventWriter out;

    private int indentSize = 2;

    private int depth = 0;

    public IndentingXMLEventWriter(XMLEventWriter out) {
        this.out = out;
        xmlEventFactory = XMLEventFactory.newFactory();
        scopeState.add(EnumSet.noneOf(State.class));
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return out.getNamespaceContext();
    }

    @Override
    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        out.setNamespaceContext(context);
    }

    @Override
    public void setDefaultNamespace(String uri) throws XMLStreamException {
        out.setDefaultNamespace(uri);
    }

    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        out.setPrefix(prefix, uri);
    }

    @Override
    public String getPrefix(String uri) throws XMLStreamException {
        return out.getPrefix(uri);
    }

    @Override
    public void add(XMLEventReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            add(reader.nextEvent());
        }
    }

    @Override
    public void flush() throws XMLStreamException {
        out.flush();
    }

    @Override
    public void close() throws XMLStreamException {
        out.close();
    }

    public void setIndentSize(int indentSize) {
        this.indentSize = indentSize;
    }

    private static String repeat(final char ch, final int repeat) {
        if (repeat <= 0) {
            return "";
        }
        final char[] buf = new char[repeat];
        for (int i = repeat - 1; i >= 0; i--) {
            buf[i] = ch;
        }
        return new String(buf);
    }

    @Override
    public void add(XMLEvent event) throws XMLStreamException {
        switch (event.getEventType()) {
            case XMLStreamConstants.CHARACTERS, XMLStreamConstants.CDATA, XMLStreamConstants.SPACE -> {
                out.add(event);
                afterData();
            }
            case XMLStreamConstants.START_ELEMENT -> {
                beforeStartElement();
                out.add(event);
                afterStartElement();
            }
            case XMLStreamConstants.END_ELEMENT -> {
                beforeEndElement();
                out.add(event);
                afterEndElement();
            }
            case XMLStreamConstants.START_DOCUMENT, XMLStreamConstants.PROCESSING_INSTRUCTION, XMLStreamConstants.COMMENT, XMLStreamConstants.DTD -> {
                beforeMarkup();
                out.add(event);
                afterMarkup();
            }
            case XMLStreamConstants.END_DOCUMENT -> {
                out.add(event);
            }
            default -> {
                out.add(event);
                afterEndDocument();
            }
        }
    }

    private void beforeMarkup() {
        final Set<State> state = scopeState.getFirst();
        if (!state.contains(State.WROTE_DATA) && (depth > 0 || !state.isEmpty())) {
            final String indent = getIndent(this.depth, this.indentSize);
            final Characters indentEvent = xmlEventFactory.createCharacters(indent);
            try {
                out.add(indentEvent);
            } catch (XMLStreamException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
            afterMarkup();
        }
    }

    private void afterMarkup() {
        final Set<State> state = scopeState.getFirst();
        state.add(State.WROTE_MARKUP);
    }

    private void afterData() {
        final Set<State> state = scopeState.getFirst();
        state.add(State.WROTE_DATA);
    }

    private void beforeStartElement() {
        beforeMarkup();
    }

    private void afterStartElement() {
        afterMarkup();
        depth++;
        scopeState.push(EnumSet.noneOf(State.class));
    }

    private void beforeEndElement() {
        final Set<State> state = scopeState.getFirst();
        if (depth > 0 && state.contains(State.WROTE_MARKUP) && !state.contains(State.WROTE_DATA)) {
            final String indent = this.getIndent(depth - 1, indentSize);
            final Characters indentEvent = xmlEventFactory.createCharacters(indent);
            try {
                out.add(indentEvent);
            } catch (XMLStreamException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    private void afterEndElement() {
        if (depth > 0) {
            --depth;
            scopeState.pop();
        }
    }

    private void afterEndDocument() {
        depth = 0;
        scopeState.clear();
        scopeState.push(EnumSet.noneOf(State.class));
    }

    private String getIndent(int depth, int size) {
        final int length = depth * size;
        return indentCache.computeIfAbsent(length, l -> LINE_SEPARATOR + repeat(' ', l));
    }

    private enum State {
        WROTE_MARKUP,
        WROTE_DATA
    }
}
