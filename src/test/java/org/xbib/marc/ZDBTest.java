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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.xbib.helper.StreamMatcher.assertStream;

import org.junit.Test;
import org.xbib.marc.json.MarcJsonWriter;
import org.xbib.marc.transformer.value.MarcValueTransformers;
import org.xbib.marc.xml.MarcXchangeWriter;
import org.xmlunit.matchers.CompareMatcher;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;

/**
 *
 */
public class ZDBTest {

    private final StringBuilder sb = new StringBuilder();

    @Test
    public void testSRU() throws Exception {
        sb.setLength(0);
        MarcListener listener = new Listener();
        String s = "zdb-sru-marcxmlplus.xml";
        InputStream in = getClass().getResource(s).openStream();
        Marc marc = Marc.builder()
                .setInputStream(in)
                .setCharset(StandardCharsets.UTF_8)
                .setFormat("MARC21")
                .setMarcListener(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE, listener)
                .setMarcListener(MarcXchangeConstants.HOLDINGS_TYPE, listener)
                .build();
        marc.xmlReader().parse();
        assertStream(s, getClass().getResource("zdb-sru-marcxmlplus-keyvalue.txt").openStream(),
                new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testOAI() throws Exception {
        sb.setLength(0);
        MarcListener listener = new Listener();
        String s = "zdb-oai-marc.xml";
        InputStream in = getClass().getResource(s).openStream();
        Marc marc = Marc.builder()
                .setInputStream(in)
                .setCharset(StandardCharsets.UTF_8)
                .setFormat("MARC21")
                .setMarcListener(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE, listener)
                .setMarcListener(MarcXchangeConstants.HOLDINGS_TYPE, listener)
                .build();
        marc.xmlReader().parse();
        assertStream(s, getClass().getResource("zdb-oai-marc-keyvalue.txt").openStream(),
                new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8)));
    }

    private class Listener implements MarcListener {
        @Override
        public void beginCollection() {
        }

        @Override
        public void endCollection() {
        }

        @Override
        public void beginRecord(String format, String type) {
            sb.append("beginRecord").append("\n");
            sb.append("format=").append(format).append("\n");
            sb.append("type=").append(type).append("\n");
        }

        @Override
        public void leader(String label) {
            sb.append("leader=").append(label).append("\n");
        }

        @Override
        public void field(MarcField field) {
            sb.append(field).append("\n");
        }

        @Override
        public void endRecord() {
            sb.append("endRecord").append("\n");
        }

    }

    /**
     * ZDB MARC Bibliographic.
     */

    @Test
    public void testZDBBib() throws Exception {
        String s = "zdbtitutf8.mrc";
        InputStream in = getClass().getResource(s).openStream();
        File file = File.createTempFile(s, ".json");
        file.deleteOnExit();
        OutputStream out = new FileOutputStream(file);
        MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
        marcValueTransformers.setMarcValueTransformer(value -> Normalizer.normalize(value, Normalizer.Form.NFC));
        try (MarcJsonWriter writer = new MarcJsonWriter(out, MarcJsonWriter.Style.LINES)
                .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
                .setMarcValueTransformers(marcValueTransformers)) {
            Marc.builder()
                    .setInputStream(in)
                    .setMarcListener(writer)
                    .build()
                    .writeCollection();
            assertNull(writer.getException());
        }
    }

    @Test
    public void testZDBStream() throws IOException {
        String s = "zdblokutf8.mrc";
        InputStream in = getClass().getResource(s).openStream();
        long count = Marc.builder()
                .setInputStream(in)
                .setCharset(StandardCharsets.UTF_8)
                .build().iso2709Stream().chunks().count();
        in.close();
        assertEquals(10170L, count);

        in = getClass().getResource(s).openStream();
        Marc.builder()
                .setInputStream(in)
                .setCharset(StandardCharsets.UTF_8)
                .build().iso2709Stream().chunks()
                .forEach(chunk -> assertTrue(chunk.data().length() >= 0));
        in.close();
    }

    @Test
    public void testZDBLok() throws Exception {
        String s = "zdblokutf8.mrc";
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
                    .setCharset(StandardCharsets.UTF_8)
                    .setMarcListener(writer)
                    .build()
                    .writeCollection();
            assertNull(writer.getException());
        }
        assertThat(file, CompareMatcher.isIdenticalTo(getClass().getResource(s + ".xml").openStream()));
    }
}
