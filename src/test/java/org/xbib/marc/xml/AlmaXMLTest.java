package org.xbib.marc.xml;

import org.junit.jupiter.api.Test;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcRecord;
import org.xbib.marc.MarcRecordListener;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AlmaXMLTest {

    private static final Logger logger = Logger.getLogger(AlmaXMLTest.class.getName());

    @Test
    public void testAlmaRecordListener() throws Exception {
        String s = "alma.xml";
        InputStream in = getClass().getResourceAsStream(s);
        AtomicBoolean found = new AtomicBoolean();
        MarcRecordListener marcRecordListener = new MarcRecordListener() {
            @Override
            public void beginCollection() {
            }

            @Override
            public void record(MarcRecord marcRecord) {
                logger.log(Level.INFO, "record = " + marcRecord);
                found.set(true);
            }

            @Override
            public void endCollection() {
            }
        };
        // attach record listener
        MarcContentHandler marcListener = new MarcContentHandler();
        marcListener.setMarcRecordListener(marcRecordListener);

        Marc.builder()
                .setInputStream(in)
                .setCharset(StandardCharsets.UTF_8)
                .setContentHandler(marcListener)
                .build()
                .xmlReader()
                .parse();
        assertTrue(found.get());
    }
}
