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
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.xbib.content.XContentBuilder;
import org.xbib.content.json.JsonXContent;
import org.xbib.marc.label.RecordLabel;
import org.xbib.marc.transformer.value.MarcValueTransformers;
import org.xbib.marc.xml.MarcXchangeWriter;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
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
        // label, 001, 100 are in the record map
        assertEquals(3, marcRecord.size());
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
                assertEquals(LocalDate.of(2002, 8, 5), marcRecord.getCreationDate());
                assertEquals(LocalDate.of(2003, 6, 16), marcRecord.getLastModificationDate());
                // check if single 245 field
                List<MarcField> list = new ArrayList<>();
                Pattern pattern = Pattern.compile("^245.*");
                marcRecord.all(field -> pattern.matcher(field.getTag()).matches(), list::add);
                assertEquals(1, list.size());
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

    @Test
    public void testMarcRecordFromMapNested() {
        // test if we can have more than one map in a list
        Map<String, Object> map = Map.of("001", "123",
                "100", Map.of("_", Map.of("a", "Hello World")),
                "016", Map.of("7_", List.of(Map.of("2", "DE-101", "a", "010000151"), Map.of("2", "DE-600", "a", "23-1"))));
        MarcRecord marcRecord = MarcRecord.from(map);
        assertEquals("123", marcRecord.getFields().stream()
                .filter(m -> m.getTag().equals("001")).findFirst().get().getValue());
        assertEquals("Hello World", marcRecord.getFields().stream()
                .filter(m -> m.getTag().equals("100")).findFirst().get().getFirstSubfieldValue("a"));
        assertEquals(4,  marcRecord.getFields().size());
        List<MarcField> list = new LinkedList<>();
        marcRecord.all(f -> "016".equals(f.getTag()), list::add);
        assertEquals(2, list.size());
        AtomicBoolean match = new AtomicBoolean();
        marcRecord.all(f -> "016".equals(f.getTag()) && "7 ".equals(f.getIndicator()), f -> {
            if ("DE-600".equals(f.getFirstSubfieldValue("2"))) {
                match.set("23-1".equals(f.getFirstSubfieldValue("a")));
            }
        });
        assertTrue(match.get());
    }

    @Test
    public void testMarcRecordFromMapsWithJoinedPlainMaps() {
        // test if we can collapse "plain" subfield maps into a common MARC field
        // 016=[{7_=[{2=DE-101}, {a=010000151}]}, {7_=[{2=DE-600}, {a=23-1}]}]
        Map<String, Object> f1 = Map.of("7_", List.of(Map.of("2", "DE-101"), Map.of("a", "010000151")));
        Map<String, Object> f2 = Map.of("7_", List.of(Map.of("2", "DE-600"), Map.of("a", "23-1")));
        Map<String, Object> map = Map.of("016", List.of(f1, f2));
        MarcRecord marcRecord = MarcRecord.from(map);
        List<MarcField> list = new LinkedList<>();
        marcRecord.all(f -> "016".equals(f.getTag()), list::add);
        assertEquals(2, list.size());
        AtomicBoolean match = new AtomicBoolean();
        marcRecord.all(f -> "016".equals(f.getTag()) && "7 ".equals(f.getIndicator()), f -> {
            if ("DE-600".equals(f.getFirstSubfieldValue("2"))) {
                match.set("23-1".equals(f.getFirstSubfieldValue("a")));
            }
        });
        assertTrue(match.get());
    }

    @Test
    public void testMarcRecordFromMapsWithSameSubfieldId() {
        // 016=[{7_=[{a=foo}, {a=bar}}]
        Map<String, Object> f1 = Map.of("7_", List.of(Map.of("a", "foo"), Map.of("a", "bar")));
        Map<String, Object> map = Map.of("016", List.of(f1));
        MarcRecord marcRecord = MarcRecord.from(map);
        // we must have a single 016 field
        List<MarcField> list = new LinkedList<>();
        marcRecord.all(f -> "016".equals(f.getTag()), list::add);
        assertEquals(1, list.size());
        // we count for occurences of "foo" and "bar", both must exist
        AtomicInteger count = new AtomicInteger();
        marcRecord.all(f -> "016".equals(f.getTag()) && "7 ".equals(f.getIndicator()), f ->
                f.getSubfield("a").forEach(sf -> {
                    if ("foo".equals(sf.getValue())) {
                        count.incrementAndGet();
                    }
                    if ("bar".equals(sf.getValue())) {
                        count.incrementAndGet();
                    }
                }));
        assertEquals(2, count.get());
    }

    @Test
    public void testMarcRecordFromMapAsMap() throws IOException {
        Map<String, Object> map = new TreeMap<>(Map.of("001", "123",
                "100", Map.of("_", Map.of("a", "Hello World"))));
        MarcRecord marcRecord = MarcRecord.from(map);
        assertEquals("{001=123, 100={_={a=Hello World}}}", marcRecord.toString());
        XContentBuilder builder = JsonXContent.contentBuilder();
        builder.map(marcRecord);
        assertEquals("{\"001\":\"123\",\"100\":{\"_\":{\"a\":\"Hello World\"}}}", builder.string());
    }

    @Test
    public void testMarcRecordFilter() {
        Map<String, Object> map = Map.of("001", "123",
                "100", Map.of("_", Map.of("a", "Hello World")));
        MarcRecord marcRecord = MarcRecord.from(map);
        marcRecord.all("001", field -> assertEquals("123", field.getValue()));
        marcRecord.all("100", field -> assertEquals("Hello World", field.getFirstSubfieldValue("a")));
    }
}
