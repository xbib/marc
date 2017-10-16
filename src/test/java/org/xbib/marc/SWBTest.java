package org.xbib.marc;

import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class SWBTest {

    @Test
    public void testMarcStream() throws Exception {
        String[] files = {
                "SWB1.marc21",
                "SWB2.marc21",
                "SWB3.marc21"
        };
        for (String file : files) {
            AtomicInteger count = new AtomicInteger();
            try (InputStream in = getClass().getResource(file).openStream()) {
                Marc marc = Marc.builder()
                        .setInputStream(in)
                        .setCharset(Charset.forName("UTF-8"))
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
                "SWB1.marc21",
                "SWB2.marc21",
                "SWB3.marc21"
        };
        for (String file : files) {
            int count = 0;
            try (InputStream in = getClass().getResource(file).openStream()) {
                Marc.Builder builder = Marc.builder()
                        .setInputStream(in)
                        .setCharset(Charset.forName("UTF-8"))
                        .setFormat("Marc21")
                        .setType("Bibliographic");
                for (MarcRecord marcRecord : builder.iterable()) {
                    count++;
                }
            }
            assertEquals(1, count);
        }
    }
}
