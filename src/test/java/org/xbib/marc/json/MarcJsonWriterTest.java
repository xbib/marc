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
package org.xbib.marc.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.xbib.marc.StreamMatcher.assertStream;

import org.junit.Test;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcRecordAdapter;
import org.xbib.marc.MarcXchangeConstants;
import org.xbib.marc.transformer.value.MarcValueTransformers;
import org.xbib.marc.xml.MarcContentHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.EnumSet;

/**
 *
 */
public class MarcJsonWriterTest {

    /**
     * {@code }MarcJsonWriter} can receive MARC fields.
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
            InputStream in = getClass().getResource("/org/xbib/marc/" + s).openStream();
            File file = File.createTempFile(s + ".", ".json");
            file.deleteOnExit();
            FileOutputStream out = new FileOutputStream(file);
            try (MarcJsonWriter writer = new MarcJsonWriter(out)
                    .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                    .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
            ) {
                Marc.builder()
                        .setInputStream(in)
                        .setCharset(Charset.forName("ANSEL"))
                        .setMarcListener(writer)
                        .build()
                        .writeCollection();
            }
            assertStream(s, getClass().getResource("/org/xbib/marc/json/" + s + ".json").openStream(),
                    new FileInputStream(file));
        }
    }

    /**
     * {@code }MarcJsonWriter} can receive MARC records.
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
            InputStream in = getClass().getResource("/org/xbib/marc/" + s).openStream();
            File file = File.createTempFile(s + ".", ".json");
            file.deleteOnExit();
            FileOutputStream out = new FileOutputStream(file);
            try (MarcJsonWriter writer = new MarcJsonWriter(out)
            ) {
                Marc.builder()
                        .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                        .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
                        .setInputStream(in)
                        .setCharset(Charset.forName("ANSEL"))
                        .setMarcRecordListener(writer)
                        .build()
                        .writeRecordCollection();
            }
            assertStream(s, getClass().getResource("/org/xbib/marc/json/" + s + ".json").openStream(),
                    new FileInputStream(file));
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
            InputStream in = getClass().getResource("/org/xbib/marc/" + s).openStream();
            File file = File.createTempFile(s + ".", ".json");
            file.deleteOnExit();
            FileOutputStream out = new FileOutputStream(file);
            try (MarcJsonWriter writer = new MarcJsonWriter(out)) {
                Marc.builder()
                        .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                        .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
                        .setInputStream(in)
                        .setCharset(Charset.forName("ANSEL"))
                        .setMarcListener(new MarcRecordAdapter(writer))
                        .build()
                        .writeCollection();
            }
            assertStream(s, getClass().getResource("/org/xbib/marc/json/" + s + ".json").openStream(),
                    new FileInputStream(file));
        }
    }

    @Test
    public void testAlephPublishRecordAdapterJson() throws Exception {
        String s = "HT016424175.xml";
        InputStream in = getClass().getResource("/org/xbib/marc/dialects/mab/" + s).openStream();
        File file = File.createTempFile(s + ".", ".json");
        file.deleteOnExit();
        FileOutputStream out = new FileOutputStream(file);
        try (MarcJsonWriter writer = new MarcJsonWriter(out, EnumSet.of(MarcJsonWriter.Style.LINES))
                .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
        ) {
            MarcContentHandler contentHandler = new MarcContentHandler();
            contentHandler.addNamespace("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd");
            contentHandler.setFormat("MARC21");
            contentHandler.setType("Bibliographic");
            contentHandler.setMarcListener(new MarcRecordAdapter(writer));
            Marc.builder()
                    .setInputStream(in)
                    .setContentHandler(contentHandler)
                    .build()
                    .xmlReader().parse();
        }
        assertStream(s, getClass().getResource("/org/xbib/marc/json/" + s + ".json").openStream(),
                new FileInputStream(file));
    }

    @Test
    public void splitMARC() throws Exception {
        String s = "IRMARC8.bin";
        InputStream in = getClass().getResource("/org/xbib/marc//" + s).openStream();
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
        File f0 = new File("build/0.json");
        assertTrue(f0.exists());
        assertEquals(6015, f0.length());
        File f1 = new File("build/1.json");
        assertTrue(f1.exists());
        assertEquals(7130, f1.length());
        File f2 = new File("build/2.json");
        assertTrue(f2.exists());
        assertEquals(6426, f2.length());
        File f3 = new File("build/3.json");
        assertTrue(f3.exists());
        assertEquals(2110, f3.length());
        File f4 = new File("build/4.json");
        assertFalse(f4.exists());
    }

    @Test
    public void elasticsearchBulkFormat() throws Exception {
        String s = "IRMARC8.bin";
        InputStream in = getClass().getResource("/org/xbib/marc//" + s).openStream();
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
        File f0 = new File("build/bulk0.jsonl");
        assertTrue(f0.exists());
        assertEquals(6295, f0.length());
        File f1 = new File("build/bulk1.jsonl");
        assertTrue(f1.exists());
        assertEquals(7410, f1.length());
        File f2 = new File("build/bulk2.jsonl");
        assertTrue(f2.exists());
        assertEquals(6706, f2.length());
        File f3 = new File("build/bulk3.jsonl");
        assertTrue(f3.exists());
        assertEquals(2204, f3.length());
        File f4 = new File("build/bulk4.jsonl");
        assertFalse(f4.exists());
    }

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
            File f0 = new File("build/bulk0.jsonl.gz");
            assertTrue(f0.exists());
            assertEquals(2141, f0.length());
            File f1 = new File("build/bulk1.jsonl.gz");
            assertTrue(f1.exists());
            assertEquals(2608, f1.length());
            File f2 = new File("build/bulk2.jsonl.gz");
            assertTrue(f2.exists());
            assertEquals(2667, f2.length());
            File f3 = new File("build/bulk3.jsonl.gz");
            assertTrue(f3.exists());
            assertEquals(1021, f3.length()); // but, it's 1031???
            File f4 = new File("build/bulk4.jsonl.gz");
            assertFalse(f4.exists());
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
        File file = File.createTempFile("multi.", ".json");
        file.deleteOnExit();
        FileOutputStream out = new FileOutputStream(file);
        try (MarcJsonWriter writer = new MarcJsonWriter(out, EnumSet.of(MarcJsonWriter.Style.ARRAY))) {
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
        }
        assertStream("multi", getClass().getResource("/org/xbib/marc/json/multi.json").openStream(),
                new FileInputStream(file));
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
            InputStream in = getClass().getResource("/org/xbib/marc/" + s).openStream();
            File file = File.createTempFile(s + ".", ".json");
            file.deleteOnExit();
            FileOutputStream out = new FileOutputStream(file);
            try (MarcJsonWriter writer = new MarcJsonWriter(out)
            ) {
                Marc.builder()
                        .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                        .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
                        .setInputStream(in)
                        .setCharset(Charset.forName("ANSEL"))
                        .setMarcRecordListener(writer)
                        .build()
                        .writeRecordCollection();
            }
            assertStream(s, getClass().getResource("/org/xbib/marc/json/" + s + ".json").openStream(),
                    new FileInputStream(file));
        }
    }

    /**
     * Test JSON format that allows duplicate keys. This allows to format MARC in order,
     * as defined by cataloging rules.
     *
     * @throws Exception if test fails
     */
    @Test
    public void testMarcRecordJsonWithDuplicateKeys() throws Exception {
        for (String s : new String[]{
                "test_ubl.mrc"
        }) {
            InputStream in = getClass().getResource("/org/xbib/marc/" + s).openStream();
            File file = File.createTempFile(s + ".", ".json");
            file.deleteOnExit();
            FileOutputStream out = new FileOutputStream(file);
            try (MarcJsonWriter writer = new MarcJsonWriter(out, EnumSet.of(MarcJsonWriter.Style.ALLOW_DUPLICATES))
            ) {
                Marc.builder()
                        .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                        .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
                        .setInputStream(in)
                        .setCharset(Charset.forName("ANSEL"))
                        .setMarcListener(writer)
                        .build()
                        .writeCollection();
            }
            assertStream(s, getClass().getResource("/org/xbib/marc/json/" + s + ".dupkey.json").openStream(),
                    new FileInputStream(file));
        }
    }
}
