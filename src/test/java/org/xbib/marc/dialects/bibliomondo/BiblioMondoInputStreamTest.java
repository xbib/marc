package org.xbib.marc.dialects.bibliomondo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xbib.marc.Marc;
import org.xbib.marc.xml.MarcXchangeWriter;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BiblioMondoInputStreamTest {

    @Test
    @Disabled("skip this because data license is unclear")
    public void biblioMondoRecords() throws Exception {
        Path path = Paths.get("/data/fix/DE-380/Stbib_Fernleihe_20150427.MARC");
        Path out = Paths.get("/var/tmp/Stbib_Fernleihe_20150427.marcxchange");
        try (InputStream inputStream = Files.newInputStream(path);
             Writer outWriter = Files.newBufferedWriter(out);
             MarcXchangeWriter writer = new MarcXchangeWriter(outWriter, true)) {
            Marc marc = Marc.builder()
                    .setInputStream(inputStream)
                    .setCharset(Charset.forName("ANSEL"))
                    .setMarcListener(writer)
                    .build();
            marc.wrapIntoCollection(marc.bibliomondo());
        }
        // assertThat(sw.toString(), CompareMatcher.isIdenticalTo(getClass().getResource(s + ".xml").openStream()));
    }
}
