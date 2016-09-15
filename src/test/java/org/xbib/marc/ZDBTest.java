/*
   Copyright 2016 Jörg Prante

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.xbib.marc;

import static org.xbib.helper.StreamMatcher.assertStream;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 *
 */
public class ZDBTest {

    private final StringBuilder sb = new StringBuilder();

    @Test
    public void testSRU() throws Exception {
        sb.setLength(0);
        MarcListener listener = new Listener();
        String s = "zdb-sru-marcxmlplus.xml";
        InputStream in = getClass().getResource(s).openStream();
        Marc marc = Marc.builder()
                .setInputStream(in)
                .setCharset(StandardCharsets.UTF_8)
                .setFormat("MARC21")
                .setMarcListener(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE, listener)
                .setMarcListener(MarcXchangeConstants.HOLDINGS_TYPE, listener)
                .build();
        marc.xmlReader().parse();
        assertStream(s, getClass().getResource("zdb-sru-marcxmlplus-keyvalue.txt").openStream(),
                new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testOAI() throws Exception {
        sb.setLength(0);
        MarcListener listener = new Listener();
        String s = "zdb-oai-marc.xml";
        InputStream in = getClass().getResource(s).openStream();
        Marc marc = Marc.builder()
                .setInputStream(in)
                .setCharset(StandardCharsets.UTF_8)
                .setFormat("MARC21")
                .setMarcListener(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE, listener)
                .setMarcListener(MarcXchangeConstants.HOLDINGS_TYPE, listener)
                .build();
        marc.xmlReader().parse();
        assertStream(s, getClass().getResource("zdb-oai-marc-keyvalue.txt").openStream(),
                new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8)));
    }

    private class Listener implements MarcListener {
        @Override
        public void beginCollection() {
        }

        @Override
        public void endCollection() {
        }

        @Override
        public void beginRecord(String format, String type) {
            sb.append("beginRecord").append("\n");
            sb.append("format=").append(format).append("\n");
            sb.append("type=").append(type).append("\n");
        }

        @Override
        public void leader(String label) {
            sb.append("leader=").append(label).append("\n");
        }

        @Override
        public void field(MarcField field) {
            sb.append(field).append("\n");
        }

        @Override
        public void endRecord() {
            sb.append("endRecord").append("\n");
        }

    }
}
