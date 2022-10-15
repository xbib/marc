package org.xbib.marc;

import org.junit.jupiter.api.Test;
import org.xbib.marc.xml.MarcXchangeWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * This test reads ISO 2709 MARC files, writes them to XML, parses the XML and writes back ISO 2709.
 */
public class MarcRoundTripTest {

    @Test
    public void testProperMarc() throws Exception {
        for (String s : new String[]{
                "summerland2.mrc",
                //"chabon.mrc",
                //"chabon-loc.mrc"
        }) {
            StreamMatcher.roundtrip(getClass(), s, ".xml",
                    (inputStream, outputStream) -> {
                        try (MarcXchangeWriter writer = new MarcXchangeWriter(outputStream)) {
                            Marc.builder()
                                    .setInputStream(inputStream)
                                    .setCharset(Charset.forName("ANSEL"))
                                    .setMarcListener(writer)
                                    .build()
                                    .writeCollection();
                        }
                    }, (inputStream, outputStream) -> {
                        try (MarcWriter writer = new MarcWriter(outputStream, StandardCharsets.UTF_8)) {
                            Marc.builder()
                                    .setInputStream(inputStream)
                                    .setMarcListener(writer)
                                    .build()
                                    .xmlReader().parse();
                        }
                    });
        }
    }
}
