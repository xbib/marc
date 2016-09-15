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
package org.xbib.marc.dialects.mab.diskette;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.marc.Marc;
import org.xbib.marc.transformer.value.MarcValueTransformers;
import org.xbib.marc.xml.MarcXchangeWriter;
import org.xmlunit.matchers.CompareMatcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 *
 */
public class MabDisketteTest extends Assert {

    @Test
    public void testMABDiskette() throws Exception {
        String s = "mgl.txt";
        InputStream in = getClass().getResource(s).openStream();
        File file = File.createTempFile(s, ".xml");
        file.deleteOnExit();
        FileOutputStream out = new FileOutputStream(file);
        try (MarcXchangeWriter writer = new MarcXchangeWriter(out)) {
            Marc marc = Marc.builder()
                    .setInputStream(in)
                    .setCharset(Charset.forName("cp850"))
                    .setMarcListener(writer)
                    .build();
            long l = marc.wrapIntoCollection(marc.mabDisketteLF());
            assertEquals(90, l);
            assertNull(writer.getException());
        }
        assertThat(file, CompareMatcher.isIdenticalTo(getClass().getResource(s + ".xml").openStream()));
    }

    @Test
    public void testMABDisketteWithSubfields() throws Exception {
        String s = "DE-Bo410-sample.ma2";
        InputStream in = getClass().getResource(s).openStream();
        File file = File.createTempFile(s, ".xml");
        file.deleteOnExit();
        FileOutputStream out = new FileOutputStream(file);
        // in abstracts content, ctrl-character 0x7 must be replaced for clean XML. We replace it by fffd.
        MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
        marcValueTransformers.setMarcValueTransformer(value -> value.replaceAll("\\u0007", "\ufffd"));
        try (MarcXchangeWriter writer = new MarcXchangeWriter(out, true)
                .setMarcValueTransformers(marcValueTransformers)) {
            Marc marc = Marc.builder()
                    .setInputStream(in)
                    .setCharset(Charset.forName("cp850"))
                    .setMarcListener(writer)
                    .build();
            long l = marc.wrapIntoCollection(marc.mabDiskettePlusSubfieldsCRLF('$'));
            assertEquals(10007, l);
            assertNull(writer.getException());
        }
        assertThat(file, CompareMatcher.isIdenticalTo(getClass().getResource(s + ".xml").openStream()));
    }
}
