package org.xbib.marc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;
import org.xbib.marc.transformer.value.MarcValueTransformers;
import org.xbib.marc.xml.MarcXchangeWriter;
import org.xmlunit.matchers.CompareMatcher;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.zip.GZIPInputStream;

/**
 *
 */
public class MarcXchangeWriterTest {

    @Test
    public void splitMarcXchange() throws Exception {
        String s = "IRMARC8.bin";
        InputStream in = getClass().getResource("/org/xbib/marc//" + s).openStream();
        MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
        marcValueTransformers.setMarcValueTransformer(value -> Normalizer.normalize(value, Normalizer.Form.NFC));
        // fileNamePattern, splitSize, bufferSize, compress, indent
        try (MarcXchangeWriter writer = new MarcXchangeWriter("build/%d.xml", 3, 65536, false, true)
                .setMarcValueTransformers(marcValueTransformers)) {
            Marc.builder()
                    .setInputStream(in)
                    .setCharset(Charset.forName("ANSEL"))
                    .setMarcListener(writer)
                    .build()
                    .writeCollection();
        }
        File f0 = new File("build/0.xml");
        assertThat(f0, CompareMatcher.isIdenticalTo(getClass().getResource("0.xml").openStream()));
        File f1 = new File("build/1.xml");
        assertThat(f1, CompareMatcher.isIdenticalTo(getClass().getResource("1.xml").openStream()));
        File f2 = new File("build/2.xml");
        assertThat(f2, CompareMatcher.isIdenticalTo(getClass().getResource("2.xml").openStream()));
        File f3 = new File("build/3.xml");
        assertThat(f3, CompareMatcher.isIdenticalTo(getClass().getResource("3.xml").openStream()));
        File f4 = new File("build/4.xml");
        assertFalse(f4.exists());
    }

    @Test
    public void splitMarcXchangeCompressed() throws Exception {
        String s = "IRMARC8.bin";
        InputStream in = getClass().getResource("/org/xbib/marc//" + s).openStream();
        MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
        marcValueTransformers.setMarcValueTransformer(value -> Normalizer.normalize(value, Normalizer.Form.NFC));
        // fileNamePattern, splitSize, bufferSize, compress, indent
        try (MarcXchangeWriter writer = new MarcXchangeWriter("build/%d.xml.gz", 3, 65536, true, true)
                .setMarcValueTransformers(marcValueTransformers)) {
            Marc.builder()
                    .setInputStream(in)
                    .setCharset(Charset.forName("ANSEL"))
                    .setMarcListener(writer)
                    .build()
                    .writeCollection();
        }
        File f0 = new File("build/0.xml.gz");
        assertThat(new GZIPInputStream(new FileInputStream(f0)),
                CompareMatcher.isIdenticalTo(getClass().getResource("0.xml").openStream()));
        File f1 = new File("build/1.xml.gz");
        assertThat(new GZIPInputStream(new FileInputStream(f1)),
                CompareMatcher.isIdenticalTo(getClass().getResource("1.xml").openStream()));
        File f2 = new File("build/2.xml.gz");
        assertThat(new GZIPInputStream(new FileInputStream(f2)),
                CompareMatcher.isIdenticalTo(getClass().getResource("2.xml").openStream()));
        File f3 = new File("build/3.xml.gz");
        assertThat(new GZIPInputStream(new FileInputStream(f3)),
                CompareMatcher.isIdenticalTo(getClass().getResource("3.xml").openStream()));
        File f4 = new File("build/4.xml.gz");
        assertFalse(f4.exists());
    }
}
