package org.xbib.marc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.xbib.marc.label.RecordLabel;
import org.xbib.marc.transformer.value.MarcValueTransformers;
import org.xbib.marc.xml.MarcXchangeWriter;
import org.xmlunit.matchers.CompareMatcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 *
 */
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
    public void testIRMARC8AsRecordStream() throws Exception {
        String s = "IRMARC8.bin";
        try (InputStream in = getClass().getResource(s).openStream()) {
            File file = File.createTempFile(s + ".", ".xml");
            file.deleteOnExit();
            FileOutputStream out = new FileOutputStream(file);
            MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
            marcValueTransformers.setMarcValueTransformer(value -> Normalizer.normalize(value, Normalizer.Form.NFC));
            try (MarcXchangeWriter writer = new MarcXchangeWriter(out)
                    .setMarcValueTransformers(marcValueTransformers)) {
                Marc.builder()
                        .setInputStream(in)
                        .setCharset(Charset.forName("ANSEL"))
                        .setMarcRecordListener(writer)
                        .build()
                        .writeRecordCollection();
                assertNull(writer.getException());
            }
            assertThat(file, CompareMatcher.isIdenticalTo(getClass().getResource(s + ".xml").openStream()));
        }
    }

    @Test
    public void testIRMARC8AsLightweightRecordAdapter() throws Exception {
        String s = "IRMARC8.bin";
        try (InputStream in = getClass().getResource(s).openStream()) {
            File file = File.createTempFile(s + ".", ".xml");
            file.deleteOnExit();
            FileOutputStream out = new FileOutputStream(file);
            MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
            marcValueTransformers.setMarcValueTransformer(value -> Normalizer.normalize(value, Normalizer.Form.NFC));
            try (MarcXchangeWriter writer = new MarcXchangeWriter(out)
                    .setMarcValueTransformers(marcValueTransformers)) {
                writer.startDocument(); // just write XML processing instruction
                Marc.builder()
                        .setInputStream(in)
                        .setCharset(Charset.forName("ANSEL"))
                        .setMarcListener(new LightweightMarcRecordAdapter(writer))
                        .build()
                        .writeCollection();
                assertNull(writer.getException());
                writer.endDocument();
            }
            assertThat(file, CompareMatcher.isIdenticalTo(getClass().getResource(s + ".xml").openStream()));
        }
    }
}
