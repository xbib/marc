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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.xbib.marc.label.RecordLabel;
import org.xbib.marc.transformer.value.MarcValueTransformers;
import org.xbib.marc.xml.MarcXchangeWriter;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class MarcRecordTest {

    @Test
    public void testMarcRecordLabel() {
        MarcRecord marcRecord = Marc.builder()
                .recordLabel(RecordLabel.builder().build()).buildRecord();
        assertNotNull(marcRecord.getRecordLabel());
    }

    @Test
    public void testEmptyMarcRecord() {
        MarcRecord marcRecord = MarcRecord.emptyRecord();
        MarcRecord empty = Marc.builder().buildRecord();
        assertEquals(marcRecord, empty);
    }

    @Test
    public void testLightweightRecord() {
        MarcRecord marcRecord = Marc.builder()
                .recordLabel(RecordLabel.builder().build())
                .addField(MarcField.builder().tag("001").value("123456").build())
                .addField(MarcField.builder().tag("100").indicator(" ")
                        .subfield("a", "Hello").subfield("b", "World").build())
                .buildRecord();
        // format, type, leader, 001, 100 are in the record map
        assertEquals(5, marcRecord.size());
        assertEquals(2, marcRecord.getFields().size());
        marcRecord = Marc.builder()
                .lightweightRecord()
                .recordLabel(RecordLabel.builder().build())
                .addField(MarcField.builder().tag("001").value("123456").build())
                .addField(MarcField.builder().tag("100").indicator(" ")
                        .subfield("a", "Hello").subfield("b", "World").build())
                .buildRecord();
        assertEquals(0, marcRecord.size()); // woot - no record map
        assertEquals(2, marcRecord.getFields().size()); // yea
    }

    @Test
    public void testMarcRecordIterable() throws Exception {
        String s = "chabon.mrc";
        int count = 0;
        try (InputStream in = getClass().getResource(s).openStream()) {
            Marc.Builder builder = Marc.builder()
                    .setInputStream(in)
                    .setCharset(Charset.forName("ANSEL"));
            for (MarcRecord marcRecord : builder.iterable()) {
                assertTrue(marcRecord.getFields().size() > 0);
                count++;
            }
        }
        assertEquals(2, count);
    }

    @Test
    public void testFilterKeyIterable() throws Exception {
        String s = "summerland.mrc";
        try (InputStream in = getClass().getResource(s).openStream()) {
            Marc.Builder builder = Marc.builder()
                    .setInputStream(in)
                    .setCharset(Charset.forName("ANSEL"));
            // only single record
            for (MarcRecord marcRecord : builder.iterable()) {
                // single 245 field
                assertEquals(1, marcRecord.filterKey(Pattern.compile("^245.*")).size());
            }
        }
    }

    @Test
    public void testFilterKey() throws Exception {
        String s = "summerland.mrc";
        try (InputStream in = getClass().getResource(s).openStream()) {
            Marc.Builder builder = Marc.builder()
                    .setInputStream(in)
                    .setCharset(Charset.forName("ANSEL"))
                    .setKeyPattern(Pattern.compile("^245.*"));
            //  record with single field
            for (MarcRecord marcRecord : builder.iterable()) {
                assertEquals(1, marcRecord.getFields().size());
            }
        }
    }

    @Test
    public void testFilterValueIterable() throws Exception {
        String s = "summerland.mrc";
        try (InputStream in = getClass().getResource(s).openStream()) {
            Marc.Builder builder = Marc.builder()
                    .setInputStream(in)
                    .setCharset(Charset.forName("ANSEL"));
            for (MarcRecord marcRecord : builder.iterable()) {
                assertEquals(2, marcRecord.filterValue(Pattern.compile(".*?Chabon.*")).size());
            }
        }
    }

    @Test
    public void testFilterValue() throws Exception {
        String s = "summerland.mrc";
        try (InputStream in = getClass().getResource(s).openStream()) {
            Marc.Builder builder = Marc.builder()
                    .setInputStream(in)
                    .setCharset(Charset.forName("ANSEL"))
                    .setValuePattern(Pattern.compile(".*?Chabon.*"));
            for (MarcRecord marcRecord : builder.iterable()) {
                assertEquals(2, marcRecord.getFields().size());
            }
        }
    }

    @Test
    public void testSequentialIteration() throws Exception {
        String s = "dialects/unimarc/periouni.mrc";
        try (InputStream in = getClass().getResource(s).openStream()) {
            Marc.Builder builder = Marc.builder()
                    .setInputStream(in).setCharset(StandardCharsets.UTF_8);
            final AtomicInteger count = new AtomicInteger();
            // test for loop
            for (MarcRecord marcRecord : builder.iterable()) {
                count.incrementAndGet();
            }
            assertEquals(3064, count.get());
        }
    }

    @Test
    public void testRecordStream() throws Exception {
        String s = "dialects/unimarc/periouni.mrc";
        try (InputStream in = getClass().getResource(s).openStream()) {
            Marc.Builder builder = Marc.builder()
                    .setInputStream(in)
                    .setCharset(StandardCharsets.UTF_8);
            long count = builder.recordStream().map(r -> r.get("001")).count();
            assertEquals(3064, count);
        }
    }

    /**
     * Test MarcXchangeWriter as record listener. Result must be the same as with field listener.
     */
    @Test
    public void testIRMARC8AsRecordStream() throws IOException {
        StreamMatcher.xmlMatch(getClass(),"IRMARC8.bin", ".xml", (inputStream, outputStream) -> {
            MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
            marcValueTransformers.setMarcValueTransformer(value -> Normalizer.normalize(value, Normalizer.Form.NFC));
            try (MarcXchangeWriter writer = new MarcXchangeWriter(outputStream)
                    .setMarcValueTransformers(marcValueTransformers)) {
                Marc.builder()
                        .setInputStream(inputStream)
                        .setCharset(Charset.forName("ANSEL"))
                        .setMarcRecordListener(writer)
                        .build()
                        .writeRecordCollection();
                assertNull(writer.getException());
            }
        });
    }

    @Test
    public void testIRMARC8AsLightweightRecordAdapter() throws Exception {
        StreamMatcher.xmlMatch(getClass(),"IRMARC8.bin", ".xml", (inputStream, outputStream) -> {
            MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
            marcValueTransformers.setMarcValueTransformer(value -> Normalizer.normalize(value, Normalizer.Form.NFC));
            try (MarcXchangeWriter writer = new MarcXchangeWriter(outputStream)
                    .setMarcValueTransformers(marcValueTransformers)) {
                writer.startDocument(); // just write XML processing instruction
                Marc.builder()
                        .setInputStream(inputStream)
                        .setCharset(Charset.forName("ANSEL"))
                        .setMarcListener(new LightweightMarcRecordAdapter(writer))
                        .build()
                        .writeCollection();
                assertNull(writer.getException());
                writer.endDocument();
            }
        });
    }

    @Test
    public void testMarcRecordFromMap() {
        Map<String, Object> map = Map.of("001", "123",
                "100", Map.of("_", Map.of("a", "Hello World")));
        MarcRecord marcRecord = MarcRecord.from(map);
        assertEquals("123", marcRecord.getFields().stream().filter(m -> m.getTag().equals("001")).findFirst().get().getValue());
        assertEquals("Hello World", marcRecord.getFields().stream().filter(m -> m.getTag().equals("100")).findFirst().get().getFirstSubfieldValue("a"));
    }
}
