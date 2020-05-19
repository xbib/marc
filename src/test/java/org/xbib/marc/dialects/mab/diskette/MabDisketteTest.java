package org.xbib.marc.dialects.mab.diskette;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.xbib.marc.Marc;
import org.xbib.marc.transformer.value.MarcValueTransformers;
import org.xbib.marc.xml.MarcXchangeWriter;
import org.xmlunit.matchers.CompareMatcher;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

public class MabDisketteTest {

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
