package org.xbib.marc;

import org.junit.Test;
import org.xbib.marc.xml.MarcXchangeWriter;
import org.xmlunit.matchers.CompareMatcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertThat;

/**
 * This test reads ISO 2709 MARC files, werits them to XML, parses XML and writes ISO 2709,
 * and parses them to XML again for comparison.
 */
public class MarcRoundTripTest {

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
            try (FileOutputStream out = new FileOutputStream(file);
                 MarcXchangeWriter writer = new MarcXchangeWriter(out)) {
                Marc.builder()
                        .setInputStream(in)
                        .setCharset(Charset.forName("ANSEL"))
                        .setMarcListener(writer)
                        .build()
                        .writeCollection();
            }
            // parse XML and write ISO 2709
            in = new FileInputStream(file);
            File orig = File.createTempFile(s + ".", ".orig");
            orig.deleteOnExit();
            // we do not support ANSEL writing yet
            try (FileOutputStream out = new FileOutputStream(orig);
                MarcWriter writer = new MarcWriter(out, StandardCharsets.UTF_8)) {
                Marc.builder()
                        .setInputStream(in)
                        .setMarcListener(writer)
                        .build()
                        .xmlReader().parse();
            }
            // parse ISO 2709 again and write XML
            in = new FileInputStream(orig);
            File file2 = File.createTempFile(s + ".", ".2.xml");
            file2.deleteOnExit();
            try (FileOutputStream out = new FileOutputStream(file2);
                 MarcXchangeWriter writer = new MarcXchangeWriter(out)) {
                Marc.builder()
                        .setInputStream(in)
                        .setCharset(StandardCharsets.UTF_8)
                        .setMarcListener(writer)
                        .build()
                        .writeCollection();
            }
            // compare both to expected file structure
            assertThat(file, CompareMatcher.isIdenticalTo(getClass().getResource(s + ".xml").openStream()));
            assertThat(file2, CompareMatcher.isIdenticalTo(getClass().getResource(s + ".xml").openStream()));
        }
    }
}
