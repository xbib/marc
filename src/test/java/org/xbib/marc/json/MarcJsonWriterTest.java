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
package org.xbib.marc.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.xbib.marc.StreamMatcher.assertStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcRecordAdapter;
import org.xbib.marc.MarcXchangeConstants;
import org.xbib.marc.StreamMatcher;
import org.xbib.marc.transformer.value.MarcValueTransformers;
import org.xbib.marc.xml.MarcContentHandler;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.EnumSet;

public class MarcJsonWriterTest {

    /**
     * {@code MarcJsonWriter} can receive MARC fields.
     *
     * @throws Exception if test fails
     */
    @Test
    public void testMarcJson() throws Exception {
        for (String s : new String[]{
                "summerland.mrc",
                "chabon.mrc",
                "chabon-loc.mrc"
        }) {
            StreamMatcher.fileMatch(getClass(), s, ".json", (inputStream, outputStream) -> {
               try (MarcJsonWriter writer = new MarcJsonWriter(outputStream)
                        .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                        .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
                ) {
                    Marc.builder()
                            .stableFieldOrder()
                            .setInputStream(inputStream)
                            .setCharset(Charset.forName("ANSEL"))
                            .setMarcListener(writer)
                            .build()
                            .writeCollection();
                }
            });
        }
    }

    /**
     * {@code MarcJsonWriter} can receive MARC records.
     *
     * @throws Exception if test fails
     */
    @Test
    public void testMarcRecordJson() throws Exception {
        for (String s : new String[]{
                "summerland.mrc",
                "chabon.mrc",
                "chabon-loc.mrc"
        }) {
            StreamMatcher.fileMatch(getClass(), s, ".record.json", (inputStream, outputStream) -> {
                try (MarcJsonWriter writer = new MarcJsonWriter(outputStream)
                ) {
                    Marc.builder()
                            .stableFieldOrder()
                            .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                            .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
                            .setInputStream(inputStream)
                            .setCharset(Charset.forName("ANSEL"))
                            .setMarcRecordListener(writer)
                            .build()
                            .writeRecordCollection();
                }
            });
        }
    }

    /**
     * The MARC record adapter receives field events and collects them into a MARC record,
     * which is passed on. Useful for writing MARC record-by-record, e.g. in a multithreaded
     * environment, or if the source can only emit MARC fields (like the XML content handler).
     * @throws Exception if test fails
     */
    @Test
    public void testMarcRecordAdapterJson() throws Exception {
        for (String s : new String[]{
                "summerland.mrc",
                "chabon.mrc",
                "chabon-loc.mrc"
        }) {
            StreamMatcher.fileMatch(getClass(), s, ".record.adapter.json", (inputStream, outputStream) -> {
                try (MarcJsonWriter writer = new MarcJsonWriter(outputStream)) {
                    Marc.builder()
                            .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                            .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
                            .setInputStream(inputStream)
                            .setCharset(Charset.forName("ANSEL"))
                            .setMarcListener(new MarcRecordAdapter(writer, true))
                            .build()
                            .writeCollection();
                }
            });
        }
    }

    @Test
    public void testAlephPublishRecordAdapterJson() throws Exception {
        String s = "HT016424175.xml";
        StreamMatcher.fileMatch(getClass(), s, ".json", (inputStream, outputStream) -> {
            try (MarcJsonWriter writer = new MarcJsonWriter(outputStream, EnumSet.of(MarcJsonWriter.Style.LINES))
                    .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                    .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
            ) {
                MarcContentHandler contentHandler = new MarcContentHandler();
                contentHandler.addNamespace("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd");
                contentHandler.setFormat("MARC21");
                contentHandler.setType("Bibliographic");
                contentHandler.setMarcListener(new MarcRecordAdapter(writer, true));
                Marc.builder()
                        .setInputStream(inputStream)
                        .setContentHandler(contentHandler)
                        .build()
                        .xmlReader().parse();
            }
        });
    }

    @Test
    public void splitMARC() throws Exception {
        String s = "IRMARC8.bin";
        InputStream in = getClass().getResource("/org/xbib/marc/" + s).openStream();
        MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
        marcValueTransformers.setMarcValueTransformer(value -> Normalizer.normalize(value, Normalizer.Form.NFC));
        try (MarcJsonWriter writer = new MarcJsonWriter("build/%d.json", 3)) {
            writer.setMarcValueTransformers(marcValueTransformers);
            Marc.builder()
                    .setInputStream(in)
                    .setCharset(Charset.forName("ANSEL"))
                    .setMarcListener(writer)
                    .build()
                    .writeCollection();
            assertEquals(10, writer.getRecordCounter());
            assertNull(writer.getException());
        }
        Path f0 = Paths.get("build/0.json");
        assertTrue(Files.exists(f0));
        assertEquals(6015, Files.size(f0));
        Path f1 = Paths.get("build/1.json");
        assertTrue(Files.exists(f1));
        assertEquals(7130, Files.size(f1));
        Path f2 = Paths.get("build/2.json");
        assertTrue(Files.exists(f2));
        assertEquals(6426, Files.size(f2));
        Path f3 = Paths.get("build/3.json");
        assertTrue(Files.exists(f3));
        assertEquals(2110, Files.size(f3));
        Path f4 = Paths.get("build/4.json");
        assertFalse(Files.exists(f4));
    }

    @Test
    public void elasticsearchBulkFormat() throws Exception {
        String s = "IRMARC8.bin";
        InputStream in = getClass().getResource("/org/xbib/marc/" + s).openStream();
        MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
        marcValueTransformers.setMarcValueTransformer(value -> Normalizer.normalize(value, Normalizer.Form.NFC));
        try (MarcJsonWriter writer = new MarcJsonWriter("build/bulk%d.jsonl",
                3, EnumSet.of(MarcJsonWriter.Style.ELASTICSEARCH_BULK))
                .setIndex("testindex", "testtype")) {
            writer.setMarcValueTransformers(marcValueTransformers);
            Marc.builder()
                    .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                    .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
                    .setInputStream(in)
                    .setCharset(Charset.forName("ANSEL"))
                    .setMarcListener(writer)
                    .build()
                    .writeCollection();
            assertNull(writer.getException());
            assertEquals(10, writer.getRecordCounter());
        }
        Path f0 = Paths.get("build/bulk0.jsonl");
        assertTrue(Files.exists(f0));
        assertEquals(6295, Files.size(f0));
        Path f1 = Paths.get("build/bulk1.jsonl");
        assertTrue(Files.exists(f1));
        assertEquals(7410, Files.size(f1));
        Path f2 = Paths.get("build/bulk2.jsonl");
        assertTrue(Files.exists(f2));
        assertEquals(6706, Files.size(f2));
        Path f3 = Paths.get("build/bulk3.jsonl");
        assertTrue(Files.exists(f3));
        assertEquals(2204, Files.size(f3));
        Path f4 = Paths.get("build/bulk4.jsonl");
        assertFalse(Files.exists(f4));
    }

    /**
     * Ugly idea to check against gzipped file lengths!
     *
     * TODO: fix the test, gzip and gunzip, and check against clear text content.
     *
     * @throws Exception if test errors
     */
    @Test
    public void elasticsearchBulkFormatCompressed() throws Exception {
        String s = "IRMARC8.bin";
        InputStream in = getClass().getResource("/org/xbib/marc/" + s).openStream();
        MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
        marcValueTransformers.setMarcValueTransformer(value -> Normalizer.normalize(value, Normalizer.Form.NFC));
        // split at 3, Elasticsearch bulk format, buffer size 65536, compress = true
        try (MarcJsonWriter writer = new MarcJsonWriter("build/bulk%d.jsonl.gz",
                3, EnumSet.of(MarcJsonWriter.Style.ELASTICSEARCH_BULK), 65536, true)
                .setIndex("testindex", "testtype")) {
            writer.setMarcValueTransformers(marcValueTransformers);
            Marc.builder()
                    .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                    .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
                    .setInputStream(in)
                    .setCharset(Charset.forName("ANSEL"))
                    .setMarcListener(writer)
                    .build()
                    .writeCollection();
            assertNull(writer.getException());
            assertEquals(10, writer.getRecordCounter());
            Path f0 = Paths.get("build/bulk0.jsonl.gz");
            assertTrue(Files.exists(f0));
            assertEquals(2142, Files.size(f0));
            Path f1 = Paths.get("build/bulk1.jsonl.gz");
            assertTrue(Files.exists(f1));
            assertEquals(2608, Files.size(f1));
            Path f2 = Paths.get("build/bulk2.jsonl.gz");
            assertTrue(Files.exists(f2));
            assertEquals(2666, Files.size(f2));
            Path f3 = Paths.get("build/bulk3.jsonl.gz");
            assertTrue(Files.exists(f3));
            assertEquals(1020, Files.size(f3));
            Path f4 = Paths.get("build/bulk4.jsonl.gz");
            assertFalse(Files.exists(f4));
        }
    }

    @Test
    public void testBundeskunsthalle() throws Exception {
        String s = "bundeskunsthalle.xml";
        InputStream in = getClass().getResource("/org/xbib/marc/xml/" + s).openStream();
        try (MarcJsonWriter writer = new MarcJsonWriter("build/bk-bulk%d.jsonl", 1,
                EnumSet.of(MarcJsonWriter.Style.ELASTICSEARCH_BULK))
                .setIndex("testindex", "testtype")) {
            Marc.builder()
                    .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                    .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
                    .setInputStream(in)
                    .setMarcListener(writer)
                    .build()
                    .xmlReader().parse();
            assertNull(writer.getException());
        }
    }

    @Test
    public void testJsonWriterWithMultipleInput() throws Exception {
        Path path = Files.createTempFile("multi.", ".json");
        try (OutputStream outputStream = Files.newOutputStream(path);
                MarcJsonWriter writer = new MarcJsonWriter(outputStream, EnumSet.of(MarcJsonWriter.Style.ARRAY))) {
            writer.beginCollection();
            try (InputStream inputStream = getClass().getResource("/org/xbib/marc/summerland.mrc").openStream()) {
                Marc.builder()
                        .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                        .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
                        .setInputStream(inputStream)
                        .setCharset(Charset.forName("ANSEL"))
                        .setMarcRecordListener(writer)
                        .build()
                        .writeRecords();
            }
            writer.writeLine();
            try (InputStream inputStream = getClass().getResource("/org/xbib/marc/chabon.mrc").openStream()) {
                Marc.builder()
                        .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                        .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
                        .setInputStream(inputStream)
                        .setCharset(Charset.forName("ANSEL"))
                        .setMarcRecordListener(writer)
                        .build()
                        .writeRecords();
            }
            writer.endCollection();
            assertStream("multi", Files.newInputStream(path),
                    getClass().getResource("/org/xbib/marc/json/multi.json").openStream());
        } finally {
            Files.delete(path);
        }
    }

    /**
     * Test a MARC file which defines an empty subfield for correct JSON output.
     *
     * @throws Exception if test fails
     */
    @Test
    public void testMarcRecordWithEmptySubfieldJson() throws Exception {
        for (String s : new String[]{
                "rism_190101037.mrc"
        }) {
            StreamMatcher.fileMatch(getClass(), s, ".json", (inputStream, outputStream) -> {
                try (MarcJsonWriter writer = new MarcJsonWriter(outputStream)
                ) {
                    Marc.builder()
                            .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                            .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
                            .setInputStream(inputStream)
                            .setCharset(Charset.forName("ANSEL"))
                            .setMarcRecordListener(writer)
                            .build()
                            .writeRecordCollection();
                }
            });
        }
    }

    /**
     * Test JSON format that allows duplicate keys. This allows to format MARC in order,
     * as defined by cataloging rules.
     *
     * @throws Exception if test has an error
     */
    @Test
    public void testMarcRecordJsonWithDuplicateKeys() throws Exception {
        for (String s : new String[]{
                "test_ubl.mrc"
        }) {
            StreamMatcher.fileMatch(getClass(), s, ".json", (inputStream, outputStream) -> {
                try (MarcJsonWriter writer = new MarcJsonWriter(outputStream, EnumSet.of(MarcJsonWriter.Style.ALLOW_DUPLICATES))
                ) {
                    Marc.builder()
                            .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                            .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
                            .setInputStream(inputStream)
                            .setCharset(Charset.forName("ANSEL"))
                            .setMarcListener(writer)
                            .build()
                            .writeCollection();
                }
            });
        }
    }
}
