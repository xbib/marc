package org.xbib.marc.xml;

import org.junit.jupiter.api.Test;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcRecord;
import org.xbib.marc.MarcRecordListener;
import org.xbib.marc.transformer.value.MarcValueTransformer;
import org.xbib.marc.transformer.value.MarcValueTransformers;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BadRecordIdentifierTest {

    private static final Logger logger = Logger.getLogger(BadRecordIdentifierTest.class.getName());

    @Test
    public void testBadRecordId() throws Exception {
        String s = "badid.xml";
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
        MarcContentHandler marcListener = new MarcContentHandler();
        marcListener.setMarcRecordListener(marcRecordListener);
        MarcValueTransformer marcValueTransformer = BadRecordIdentifierTest::clean;
        MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
        marcValueTransformers.setMarcValueTransformer("001$$", marcValueTransformer);
        Marc.builder()
                .setInputStream(in)
                .setCharset(StandardCharsets.UTF_8)
                .setContentHandler(marcListener)
                .setMarcValueTransformers(marcValueTransformers)
                .build()
                .xmlReader()
                .parse();
        assertTrue(found.get());
    }

    private static String clean(String string) {
        StringBuilder sb = new StringBuilder();
        if (string != null) {
            for (char ch : string.toCharArray()) {
                if (ch < 32 || ch > 127) {
                    break;
                }
                if (sb.length() > 31) {
                    break;
                }
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}
