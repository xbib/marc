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

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Receive SAX events and create a DOM.
 */
public class Sax2Dom {

    private final XMLReader xmlReader;
    private final InputSource inputSource;

    public Sax2Dom(XMLReader xmlReader, InputSource inputSource) {
        this.xmlReader = xmlReader;
        this.inputSource = inputSource;
    }

    public Document document() throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document doc;
        try {
            doc = factory.newDocumentBuilder().newDocument();
            xmlReader.setContentHandler(new SaxToDomHandler(doc));
            xmlReader.parse(inputSource);
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException(e);
        }
        return doc;
    }

    private static class SaxToDomHandler extends DefaultHandler {

        private static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";
        private static final String XMLNS_PREFIX = "xmlns";
        private Document document;
        private Node node;
        private List<String> namespaceDecls;

        SaxToDomHandler(Document document) {
            this.document = document;
            this.node = document;
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) {
            if (this.namespaceDecls == null) {
                this.namespaceDecls = new ArrayList<>();
            }
            this.namespaceDecls.add(prefix);
            this.namespaceDecls.add(uri);
        }

        @Override
        public void startElement(String uri, String name, String fullName, Attributes attrs) {
            Element element = document.createElementNS(uri, fullName);
            if (this.namespaceDecls != null) {
                final int nDecls = this.namespaceDecls.size();
                for (int i = 0; i < nDecls; i += 2) {
                    String prefix = this.namespaceDecls.get(i);
                    String uriStr = this.namespaceDecls.get(i + 1);
                    element.setAttributeNS(XMLNS_URI, XMLNS_PREFIX + (prefix.isEmpty() ? "" : ":" + prefix), uriStr);
                }
                this.namespaceDecls.clear();
            }
            for (int i = 0; i < attrs.getLength(); ++i) {
                Attr attr = document.createAttributeNS(attrs.getURI(i), attrs.getQName(i));
                attr.setValue(attrs.getValue(i));
                element.setAttributeNodeNS(attr);
            }
            node.appendChild(element);
            node = element;
        }

        @Override
        public void endElement(String uri, String name, String fullName) {
            node = node.getParentNode();
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            node.appendChild(document.createTextNode(new String(ch, start, length)));
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) {
            node.appendChild(document.createTextNode(new String(ch, start, length)));
        }

        @Override
        public void processingInstruction(String target, String data) {
            node.appendChild(document.createProcessingInstruction(target, data));
        }
    }
}
