package org.xbib.marc.dialects.mab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcRecord;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public class HBZTest {

    @Test
    public void testMarcStream() throws Exception {
        String[] files = {
                "HBZ.mab"
        };
        for (String file : files) {
            AtomicInteger count = new AtomicInteger();
            try (InputStream in = getClass().getResource(file).openStream()) {
                Marc marc = Marc.builder()
                        .setInputStream(in)
                        .setCharset(StandardCharsets.UTF_8)
                        .build();
                marc.iso2709Stream().chunks().forEach(chunk -> {
                    count.incrementAndGet();
                });
            }
            assertTrue(count.get() > 0);
        }
    }

    @Test
    public void testMarcRecordIterable() throws Exception {
        String[] files = {
                "HBZ.mab"
        };
        for (String file : files) {
            int count = 0;
            try (InputStream in = getClass().getResource(file).openStream()) {
                Marc.Builder builder = Marc.builder()
                        .setFormat("MAB")
                        .setType("Titel")
                        .setInputStream(in)
                        .setCharset(Charset.forName("x-MAB"));
                for (MarcRecord marcRecord : builder.iterable()) {
                    count++;
                }
            }
            assertEquals(1, count);
        }
    }
}
