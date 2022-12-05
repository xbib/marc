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
import org.junit.jupiter.api.Test;
import org.xbib.marc.transformer.value.MarcValueTransformers;
import org.xbib.marc.xml.MarcXchangeWriter;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public class MarcWriterTest {

    @Test
    public void testMarcWriter() throws Exception {
        Map<String, Object> map = new TreeMap<>(Map.of("001", "123",
                "100", Map.of("_", Map.of("a", "Hello World"))));
        MarcRecord marcRecord = MarcRecord.from(map);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (MarcWriter writer = new MarcWriter(outputStream, StandardCharsets.UTF_8)) {
            writer.startDocument();
            writer.record(marcRecord);
            writer.endDocument();
            assertNull(writer.getException());
        }
        assertEquals("\u001D00000     0000000   000 \u001E100 \u001FaHello World\u001E001123\u001D\u001C",
                outputStream.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void testUtf8MarcWriter() throws Exception {
        for (String s : new String[]{
                "summerland.mrc",
                "chabon.mrc",
                "chabon-loc.mrc"
        }) {
            StreamMatcher.fileMatch(getClass(), s, ".utf8", (inputStream, outputStream) -> {
                try (MarcWriter writer = new MarcWriter(outputStream, StandardCharsets.UTF_8)) {
                    Marc.builder()
                            .setInputStream(inputStream)
                            .setCharset(Charset.forName("ANSEL"))
                            .setMarcListener(writer)
                            .build()
                            .writeCollection();
                    assertNull(writer.getException());
                }
            });
            StreamMatcher.xmlMatch(getClass(), s + ".utf8", ".xml", (inputStream, outputStream) -> {
                try (MarcXchangeWriter writer = new MarcXchangeWriter(outputStream)) {
                    Marc.builder()
                            .setInputStream(inputStream)
                            .setMarcListener(writer)
                            .build()
                            .writeCollection();
                }
            });
        }
    }

    @Test
    public void testUtf8MarcWriterWithTransformer() throws Exception {
        for (String s : new String[]{
                "summerland.mrc",
                "chabon.mrc",
                "chabon-loc.mrc"
        }) {
            StreamMatcher.fileMatch(getClass(), s, ".trans", (inputStream, outputStream) -> {
                MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
                marcValueTransformers.setMarcValueTransformer("245$10$a", t -> t.replaceAll("Chabon", "Chibon"));
                try (MarcWriter writer = new MarcWriter(outputStream, StandardCharsets.UTF_8)
                                .setMarcValueTransformers(marcValueTransformers)) {
                    Marc.builder()
                            .setInputStream(inputStream)
                            .setCharset(Charset.forName("ANSEL"))
                            .setMarcListener(writer)
                            .build()
                            .writeCollection();
                    assertNull(writer.getException());
                }
            });
            StreamMatcher.xmlMatch(getClass(), s + ".trans", ".xml", (inputStream, outputStream) -> {
                MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
                marcValueTransformers.setMarcValueTransformer("245$10$a", t -> t.replaceAll("Chibon", "Chabon"));
                try (MarcXchangeWriter writer = new MarcXchangeWriter(outputStream)) {
                    Marc.builder()
                            .setInputStream(inputStream)
                            .setMarcListener(writer)
                            .build()
                            .writeCollection();
                }
            });
        }
    }
}
