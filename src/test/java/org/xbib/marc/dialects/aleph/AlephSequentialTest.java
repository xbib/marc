package org.xbib.marc.dialects.aleph;

import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcXchangeConstants;
import org.xbib.marc.xml.MarcXchangeWriter;
import org.xmlunit.matchers.CompareMatcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class AlephSequentialTest {

    @Test
    public void testAlephBatch() throws Exception {
        String s = "batch.seq";
        InputStream in = getClass().getResource(s).openStream();
        File file = File.createTempFile(s, ".xml");
        file.deleteOnExit();
        FileOutputStream out = new FileOutputStream(file);
        try (MarcXchangeWriter writer = new MarcXchangeWriter(out, true)
                .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)) {
            Marc marc = Marc.builder()
                    .setInputStream(in)
                    .setCharset(StandardCharsets.UTF_8)
                    .setMarcListener(writer)
                    .build();
            marc.wrapIntoCollection(marc.aleph());
        }
        assertThat(file, CompareMatcher.isIdenticalTo(getClass().getResource(s + ".xml").openStream()));
    }
}
