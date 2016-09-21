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
package org.xbib.marc;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.marc.io.ReplaceStringInputStream;
import org.xbib.marc.transformer.value.MarcValueTransformers;
import org.xbib.marc.transformer.value.Xml10MarcValueCleaner;
import org.xbib.marc.xml.MarcXchangeWriter;
import org.xmlunit.matchers.CompareMatcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class MarcTest extends Assert {

    @Test
    public void testProperMarc() throws Exception {
        for (String s : new String[]{
                "summerland.mrc",
                "chabon.mrc",
                "chabon-loc.mrc"
        }) {
            InputStream in = getClass().getResource(s).openStream();
            File file = File.createTempFile(s + ".", ".xml");
            file.deleteOnExit();
            FileOutputStream out = new FileOutputStream(file);
            try (MarcXchangeWriter writer = new MarcXchangeWriter(out)) {
                Marc.builder()
                        .setInputStream(in)
                        .setCharset(Charset.forName("ANSEL"))
                        .setMarcListener(writer)
                        .build()
                        .writeCollection();
            }
            assertThat(file, CompareMatcher.isIdenticalTo(getClass().getResource(s + ".xml").openStream()));
        }
    }

    @Test
    public void testDiacriticMarc() throws Exception {
        for (String s : new String[]{
                "diacritic4.mrc",
                "makrtest.mrc",
                "brkrtest.mrc"
        }) {
            InputStream in = getClass().getResource(s).openStream();
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
                        .setMarcListener(writer)
                        .build()
                        .writeCollection();
            }
            assertThat(file, CompareMatcher.isIdenticalTo(getClass().getResource(s + ".xml").openStream()));
        }
    }

    /**
     * Test faulty MARC, try to repair if possible.
     * @throws Exception if test fails
     */
    @Test
    public void testFaultyMarc() throws Exception {
        for (String s : new String[]{
                "error.mrc",
                "bad_leaders_10_11.mrc",
                "bad_too_long_plus_2.mrc",
                // result is invalid XML. Fix later.
                //"bad-characters-in-various-fields.mrc"
        }) {
            InputStream in = getClass().getResource(s).openStream();
            File file = File.createTempFile(s + ".", ".xml");
            file.deleteOnExit();
            FileOutputStream out = new FileOutputStream(file);
            MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
            marcValueTransformers.setMarcValueTransformer(new Xml10MarcValueCleaner());
            try (MarcXchangeWriter writer = new MarcXchangeWriter(out, true)
                    .setMarcValueTransformers(marcValueTransformers)) {
                Marc.builder()
                        .setInputStream(in)
                        .setCharset(Charset.forName("ANSEL"))
                        .setMarcListener(writer)
                        .build()
                        .writeCollection();
            }
            assertThat(file, CompareMatcher.isIdenticalTo(getClass().getResource(s + ".xml").openStream()));
        }
    }

    @Test
    public void testAMS() throws Exception {
        String s = "amstransactions.mrc";
        InputStream in = getClass().getResource(s).openStream();
        File file = File.createTempFile(s + ".", ".xml");
        file.deleteOnExit();
        FileOutputStream out = new FileOutputStream(file);
        try (MarcXchangeWriter writer = new MarcXchangeWriter(out)) {
            Marc.builder()
                    .setInputStream(in)
                    .setCharset(StandardCharsets.UTF_8)
                    .setMarcListener(writer)
                    .build()
                    .writeCollection();
            assertNull(writer.getException());
        }
        assertThat(file, CompareMatcher.isIdenticalTo(getClass().getResource(s + ".xml").openStream()));
    }

    /**
     * ANSEL US-MARC.
     */
    @Test
    public void testIRMARC8() throws Exception {
        String s = "IRMARC8.bin";
        InputStream in = getClass().getResource(s).openStream();
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
                    .setMarcListener(writer)
                    .build()
                    .writeCollection();
            assertNull(writer.getException());
            assertEquals(10, writer.getRecordCounter());
        }
        assertThat(file, CompareMatcher.isIdenticalTo(getClass().getResource(s + ".xml").openStream()));
    }

    @Test
    public void testRecordStream() throws Exception {
        String s = "IRMARC8.bin";
        InputStream in = getClass().getResource(s).openStream();
        Marc.Builder builder = Marc.builder()
                .setInputStream(in)
                .setCharset(StandardCharsets.UTF_8);
        List<String> recordIDs = builder.recordStream().map(r -> r.get("001").toString()).collect(Collectors.toList());
        in.close();
        assertEquals("[{1=ocn132792681}, {1=ocn132786677}, {1=ocn125170297}, {1=ocn137607921}, {1=ocn124081299}, "
                + "{1=ocn135450843}, {1=ocn137458539}, {1=ocn124411460}, {1=ocn131225106}, {1=ocn124450154}]",
                recordIDs.toString());
    }

    /**
     * Ther may be faulty input streams that contain information separators at the wrong place.
     * For the problem, see {@code org.marc4j.test.PermissiveReaderTest#testCyrillicEFix()}.
     * @throws Exception if test fails
     */
    @Test
    public void moreMarcRecords() throws Exception {
        for (String s : new String[]{
                "cyrillic_capital_e.mrc"
        }) {
            InputStream in = getClass().getResource(s).openStream();
            // repair file by replacing \u001f after escape sequence for cyrillic
            ReplaceStringInputStream rin = new ReplaceStringInputStream(in, "\u001b(N\u001f", "\u001b(N|");
            File file = File.createTempFile(s + ".", ".xml");
            file.deleteOnExit();
            FileOutputStream out = new FileOutputStream(file);
            MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
            marcValueTransformers.setMarcValueTransformer(value -> Normalizer.normalize(value, Normalizer.Form.NFC));
            try (MarcXchangeWriter writer = new MarcXchangeWriter(out, true)
                    .setMarcValueTransformers(marcValueTransformers)) {
                Marc.builder()
                        .setInputStream(rin)
                        .setCharset(Charset.forName("ANSEL"))
                        .setMarcListener(writer)
                        .build()
                        .writeCollection();
                assertNull(writer.getException());
            }
            assertThat(file, CompareMatcher.isIdenticalTo(getClass().getResource(s + ".xml").openStream()));
        }
    }

    @Test
    public void moreUtf8MarcRecords() throws Exception {
        for (String s : new String[]{
                "oclc_63111280_export_as_UTF8_from_connexion.mrc"
        }) {
            InputStream in = getClass().getResource(s).openStream();
            File file = File.createTempFile(s + ".", ".xml");
            file.deleteOnExit();
            FileOutputStream out = new FileOutputStream(file);
            try (MarcXchangeWriter writer = new MarcXchangeWriter(out)) {
                Marc.builder()
                        .setInputStream(in)
                        .setCharset(StandardCharsets.UTF_8)
                        .setMarcListener(writer)
                        .build()
                        .writeCollection();
                assertNull(writer.getException());
            }
            assertThat(file, CompareMatcher.isIdenticalTo(getClass().getResource(s + ".xml").openStream()));
        }
    }

    @Test
    public void indentMarcXml() throws Exception {
        for (String s : new String[]{
                "oclc_63111280_export_as_UTF8_from_connexion.mrc"
        }) {
            InputStream in = getClass().getResource(s).openStream();
            File file = File.createTempFile(s + ".", ".xml");
            file.deleteOnExit();
            FileOutputStream out = new FileOutputStream(file);
            try (MarcXchangeWriter writer = new MarcXchangeWriter(out, true)) {
                Marc.builder()
                        .setInputStream(in)
                        .setCharset(StandardCharsets.UTF_8)
                        .setMarcListener(writer)
                        .build()
                        .writeCollection();
                assertNull(writer.getException());
            }
            assertThat(file, CompareMatcher.isIdenticalTo(getClass().getResource(s + ".indented.xml").openStream()));
        }
    }

    /**
     * Dump an erraneous MARC file for diagnostics.
     * @throws Exception if test fails
     */
    @Test
    public void dumpMarc() throws Exception {
        StringBuilder sb = new StringBuilder();
        String s  = "185258.mrc";
        InputStream in = getClass().getResource(s).openStream();
            Marc marc = Marc.builder()
                    .setInputStream(in)
                    .setCharset(Charset.forName("ANSEL"))
                    .build();
        marc.iso2709Stream().chunks().forEach(chunk -> sb.append(chunk.toString()).append("\n"));
        assertTrue(sb.length() > 0);
    }
}
