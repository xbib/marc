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
package org.xbib.marc.dialects.pica;

import org.junit.jupiter.api.Test;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcField;
import org.xbib.marc.MarcListener;
import org.xbib.marc.MarcXchangeConstants;
import org.xbib.marc.StreamMatcher;
import org.xbib.marc.label.RecordLabel;
import org.xbib.marc.xml.MarcXchangeWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class PicaTest {

    @Test
    public void testPicaBinary() throws Exception {
        StreamMatcher.fileMatch(getClass(), "pica.binary", ".xml", (inputStream, outputStream) -> {
            try (MarcXchangeWriter writer = new MarcXchangeWriter(outputStream, true)) {
                Marc marc = Marc.builder()
                        .setInputStream(inputStream)
                        .setCharset(StandardCharsets.UTF_8)
                        .setMarcListener(writer)
                        .build();
                marc.wrapIntoCollection(marc.pica());
            }
        });
    }

    @Test
    public void testPicaPlain() throws Exception {
        StreamMatcher.fileMatch(getClass(), "pica.plain", ".xml", (inputStream, outputStream) -> {
            try (MarcXchangeWriter writer = new MarcXchangeWriter(outputStream, true)
                    .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                    .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
            ) {
                Marc marc = Marc.builder()
                        .setInputStream(inputStream)
                        .setCharset(StandardCharsets.UTF_8)
                        .setMarcListener(writer)
                        .build();
                marc.wrapIntoCollection(marc.picaPlain());
            }
        });
    }

    @Test
    public void testBgbExample() throws Exception {
        StreamMatcher.fileMatch(getClass(), "bgb.example", ".xml", (inputStream, outputStream) -> {
            try (MarcXchangeWriter writer = new MarcXchangeWriter(outputStream, true)
                    .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                    .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
            ) {
                Marc marc = Marc.builder()
                        .setInputStream(inputStream)
                        .setCharset(StandardCharsets.UTF_8)
                        .setMarcListener(writer)
                        .build();
                marc.wrapIntoCollection(marc.picaPlain());
            }
        });
    }

    @Test
    public void testZdbOaiBibXml() throws Exception {
        StreamMatcher.fileMatch(getClass(), "zdb-oai-bib.xml", "-keyvalue.txt", (inputStream, outputStream) -> {
            try (Listener listener = new Listener(outputStream, StandardCharsets.UTF_8)) {
                PicaXMLContentHandler contentHandler = new PicaXMLContentHandler();
                contentHandler.setFormat("Pica");
                contentHandler.setType("XML");
                contentHandler.setMarcListener(listener);
                Marc marc = Marc.builder()
                        .setInputStream(inputStream)
                        .setContentHandler(contentHandler)
                        .build();
                marc.xmlReader().parse();
            }
        });
    }

    @Test
    public void testSruPicaXml() throws Exception {
        StreamMatcher.fileMatch(getClass(), "sru_picaxml.xml", "-keyvalue.txt", (inputStream, outputStream) -> {
            try (Listener listener = new Listener(outputStream, StandardCharsets.UTF_8)) {
                PicaXMLContentHandler contentHandler = new PicaXMLContentHandler();
                contentHandler.setFormat("Pica");
                contentHandler.setType("XML");
                contentHandler.setMarcListener(listener);
                Marc marc = Marc.builder()
                        .setInputStream(inputStream)
                        .setContentHandler(contentHandler)
                        .build();
                marc.xmlReader().parse();
            }
        });
    }

    @Test
    public void testDE1a() throws IOException {
        StreamMatcher.fileMatch(getClass(), "DE-1a.pp.xml", ".xml", (inputStream, outputStream) -> {
            // we can not simply write MarcXchange out of Pica. We will fix it later.
            try (MarcXchangeWriter writer = new MarcXchangeWriter(outputStream, true)
                    .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                    .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
            ) {
                PicaXMLContentHandler contentHandler = new PicaXMLContentHandler();
                contentHandler.setFormat("Pica");
                contentHandler.setType("XML");
                contentHandler.setMarcListener(writer);
                Marc marc = Marc.builder()
                        .setInputStream(inputStream)
                        .setContentHandler(contentHandler)
                        .build();
                marc.xmlReader().parse();
            }
        });
    }


    private static class Listener implements MarcListener, AutoCloseable {

        private final BufferedWriter writer;

        Listener(OutputStream outputStream, Charset charset) {
            this.writer = new BufferedWriter(new OutputStreamWriter(outputStream, charset));
        }

        @Override
        public void beginCollection() {
        }

        @Override
        public void endCollection() {
        }

        @Override
        public void beginRecord(String format, String type) {
            try {
                writer.append("beginRecord").append("\n");
                writer.append("format=").append(format).append("\n");
                writer.append("type=").append(type).append("\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void leader(RecordLabel label) {
            try {
                writer.append("leader=").append(label.toString()).append("\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void field(MarcField field) {
            try {
                writer.append("field=").append(field.toString()).append("\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void endRecord() {
            try {
                writer.append("endRecord").append("\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void close() throws IOException {
            writer.close();
        }
    }
}
