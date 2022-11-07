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
package org.xbib.marc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import org.junit.jupiter.api.Test;
import org.xbib.marc.json.MarcJsonWriter;
import org.xbib.marc.transformer.value.MarcValueTransformers;
import org.xbib.marc.xml.MarcXchangeWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.EnumSet;

public class ZDBTest {

    @Test
    public void testSRU() throws Exception {
        String s = "zdb-sru-marcxmlplus.xml";
        StreamMatcher.fileMatch(getClass(), s, ".txt", (inputStream, outputStream) -> {
            try (Listener listener = new Listener(outputStream, StandardCharsets.UTF_8)) {
                Marc marc = Marc.builder()
                        .setInputStream(inputStream)
                        .setCharset(StandardCharsets.UTF_8)
                        .setFormat("MARC21")
                        .setMarcListener(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE, listener)
                        .setMarcListener(MarcXchangeConstants.HOLDINGS_TYPE, listener)
                        .build();
                marc.xmlReader().parse();
            }
        });
    }

    @Test
    public void testOAI() throws Exception {
        String s = "zdb-oai-marc.xml";
        StreamMatcher.fileMatch(getClass(), s, ".txt", (inputStream, outputStream) -> {
            try (Listener listener = new Listener(outputStream, StandardCharsets.UTF_8)) {
                Marc marc = Marc.builder()
                        .setInputStream(inputStream)
                        .setCharset(StandardCharsets.UTF_8)
                        .setFormat("MARC21")
                        .setMarcListener(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE, listener)
                        .setMarcListener(MarcXchangeConstants.HOLDINGS_TYPE, listener)
                        .build();
                marc.xmlReader().parse();
            }
        });
    }

    @Test
    public void testZDBBibAsJson() throws Exception {
        String s = "zdbtitutf8.mrc";
        StreamMatcher.fileMatch(getClass(), s, ".json", (inputStream, outputStream) -> {
            MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
            marcValueTransformers.setMarcValueTransformer(value -> Normalizer.normalize(value, Normalizer.Form.NFC));
            try (MarcJsonWriter writer = new MarcJsonWriter(outputStream)
                    .setStyle(EnumSet.of(MarcJsonWriter.Style.LINES))
                    .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                    .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
                    .setMarcValueTransformers(marcValueTransformers)) {
                Marc.builder()
                        .setInputStream(inputStream)
                        .setMarcListener(writer)
                        .build()
                        .writeCollection();
                assertNull(writer.getException());
            }
        });
    }

    @Test
    public void testZDBStream() throws IOException {
        String s = "zdblokutf8.mrc";
        long count;
        try (InputStream inputStream = getClass().getResource(s).openStream()) {
            count = Marc.builder()
                    .setInputStream(inputStream)
                    .setCharset(StandardCharsets.UTF_8)
                    .build().iso2709Stream().chunks().count();
        }
        assertEquals(10170L, count);
        try (InputStream inputStream = getClass().getResource(s).openStream()) {
            Marc.builder()
                    .setInputStream(inputStream)
                    .setCharset(StandardCharsets.UTF_8)
                    .build().iso2709Stream().chunks()
                    .forEach(chunk -> assertTrue(chunk.data().length() >= 0));
        }
    }

    @Test
    public void testZDBLok() throws Exception {
        String s = "zdblokutf8.mrc";
        StreamMatcher.xmlMatch(getClass(), s, ".xml", (inputStream, outputStream) -> {
            MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
            marcValueTransformers.setMarcValueTransformer(value -> Normalizer.normalize(value, Normalizer.Form.NFC));
            try (MarcXchangeWriter writer = new MarcXchangeWriter(outputStream)
                    .setMarcValueTransformers(marcValueTransformers)) {
                Marc.builder()
                        .setInputStream(inputStream)
                        .setCharset(StandardCharsets.UTF_8)
                        .setMarcListener(writer)
                        .build()
                        .writeCollection();
                assertNull(writer.getException());
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
        public void leader(String label) {
            try {
                writer.append("leader=").append(label).append("\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void field(MarcField field) {
            try {
                writer.append(field.toString()).append("\n");
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
