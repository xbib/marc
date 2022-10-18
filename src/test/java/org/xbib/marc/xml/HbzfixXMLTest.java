/**
 *  Copyright 2016-2022 Jörg Prante <joergprante@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache License 2.0</a>
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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

public class HbzfixXMLTest {

    private static final Logger logger = Logger.getLogger(HbzfixXMLTest.class.getName());

    @Test
    public void testHbzFixRecordListener() throws Exception {
        String s = "hbzfix.xml";
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
