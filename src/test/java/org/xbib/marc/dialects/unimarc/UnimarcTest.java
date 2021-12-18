package org.xbib.marc.dialects.unimarc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.xbib.marc.Marc;
import org.xbib.marc.xml.MarcXchangeWriter;
import org.xmlunit.matchers.CompareMatcher;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class UnimarcTest {

    /**
     * UNIMARC test.
     * Found at https://github.com/medialab/reference_manager/raw/master/data/unimarc/periouni.mrc
     * License: LGPL
     *
     * @throws Exception if test fails
     */
    @Test
    public void testPerioUni() throws Exception {
        String s = "periouni.mrc";
        File file = File.createTempFile("periouni.", ".xml");
        file.deleteOnExit();
        FileOutputStream out = new FileOutputStream(file);
        try (MarcXchangeWriter writer = new MarcXchangeWriter(out, true)
                .setFormat("UNIMARC")
                .setType("Bibliographic");
             InputStream inputStream = getClass().getResource(s).openStream()) {
            Marc.builder()
                    .setInputStream(inputStream)
                    .setCharset(StandardCharsets.UTF_8)
                    .setMarcListener(writer)
                    .build()
                    .writeCollection();
            assertNull(writer.getException());
        }
        assertThat(file, CompareMatcher.isIdenticalTo(getClass().getResource(s + ".xml").openStream()));
    }

}
