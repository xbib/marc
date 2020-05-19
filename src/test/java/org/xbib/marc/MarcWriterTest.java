package org.xbib.marc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.xbib.marc.transformer.value.MarcValueTransformers;
import org.xbib.marc.xml.MarcXchangeWriter;
import org.xmlunit.matchers.CompareMatcher;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 *
 */
public class MarcWriterTest {

    @Test
    public void testUtf8MarcWriter() throws Exception {
        for (String s : new String[]{
                "summerland.mrc",
                "chabon.mrc",
                "chabon-loc.mrc"
        }) {
            InputStream in = getClass().getResource(s).openStream();
            File file = File.createTempFile(s + ".", ".utf8");
            file.deleteOnExit();
            FileOutputStream out = new FileOutputStream(file);
            try (MarcWriter writer = new MarcWriter(out, StandardCharsets.UTF_8)) {
                Marc.builder()
                        .setInputStream(in)
                        .setCharset(Charset.forName("ANSEL"))
                        .setMarcListener(writer)
                        .build()
                        .writeCollection();
                assertNull(writer.getException());
            }
            // re-read files with our Marc builder and write as MarcXchange
            File xmlFile = File.createTempFile(s + ".", ".utf8");
            xmlFile.deleteOnExit();
            out = new FileOutputStream(xmlFile);
            try (MarcXchangeWriter writer = new MarcXchangeWriter(out)) {
                Marc.builder()
                        .setInputStream(new FileInputStream(file))
                        .setMarcListener(writer)
                        .build()
                        .writeCollection();
            }
            // compare result
            assertThat(xmlFile, CompareMatcher.isIdenticalTo(getClass().getResource(s + ".xml").openStream()));
        }
    }

    @Test
    public void testUtf8MarcWriterWithTransformer() throws Exception {
        for (String s : new String[]{
                "summerland.mrc",
                "chabon.mrc",
                "chabon-loc.mrc"
        }) {
            InputStream in = getClass().getResource(s).openStream();
            File file = File.createTempFile(s, ".utf8");
            file.deleteOnExit();
            FileOutputStream out = new FileOutputStream(file);
            MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
            marcValueTransformers.setMarcValueTransformer("245$10$a", t -> t.replaceAll("Chabon", "Chibon"));
            try (MarcWriter writer = new MarcWriter(out, StandardCharsets.UTF_8)
                    .setMarcValueTransformers(marcValueTransformers)) {
                Marc.builder()
                        .setInputStream(in)
                        .setCharset(Charset.forName("ANSEL"))
                        .setMarcListener(writer)
                        .build()
                        .writeCollection();
                assertNull(writer.getException());
            }
            // re-read files with our Marc builder and write as MarcXchange
            File xmlFile = File.createTempFile(s, ".utf8");
            xmlFile.deleteOnExit();
            out = new FileOutputStream(xmlFile);
            marcValueTransformers = new MarcValueTransformers();
            marcValueTransformers.setMarcValueTransformer("245$10$a", t -> t.replaceAll("Chibon", "Chabon"));
            try (MarcXchangeWriter writer = new MarcXchangeWriter(out)
                    .setMarcValueTransformers(marcValueTransformers)) {
                Marc.builder()
                        .setInputStream(new FileInputStream(file))
                        .setMarcListener(writer)
                        .build()
                        .writeCollection();
            }
            // compare result
            assertThat(xmlFile, CompareMatcher.isIdenticalTo(getClass().getResource(s + ".xml").openStream()));
        }
    }
}
