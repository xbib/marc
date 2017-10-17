package org.xbib.marc.dialects.mab;

import org.junit.Test;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcRecord;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class HBZTest {

    private static final Logger logger = Logger.getLogger(HBZTest.class.getName());

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
                    logger.info("record = " + marcRecord);
                }
            }
            assertEquals(1, count);
        }
    }
}
