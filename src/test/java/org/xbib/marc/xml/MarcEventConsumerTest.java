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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcRecord;
import org.xbib.marc.MarcRecordIterator;
import org.xmlunit.matchers.CompareMatcher;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLInputFactory;

/**
 *
 */
public class MarcEventConsumerTest {

    private static final Logger logger = Logger.getLogger(MarcEventConsumerTest.class.getName());

    /**
     * Parsing XML by STAX (streaming XML) from Aleph publishing interface (hbz dialect).
     *
     * Interestingly, if you change StringWriter to FileWriter, then
     * <code><[!CDATA[<?xml version="1.0"?>]]></code>
     * will become
     * <code><[!CDATA[<?xml version="1.0" encoding="UTF-8"?>]]></code>.
     *
     * @throws Exception if test fails
     */
    @Test
    public void testMarcXchangeEventConsumer() throws Exception {
        String s = "HT016424175.xml";
        InputStream in = getClass().getResourceAsStream(s);
        StringWriter sw = new StringWriter();
        MarcXchangeWriter writer = new MarcXchangeWriter(sw);
        writer.setFormat("AlephXML").setType("Bibliographic");
        writer.startDocument();
        writer.beginCollection();
        MarcXchangeEventConsumer consumer = new MarcXchangeEventConsumer();
        consumer.setMarcListener(writer);
        consumer.addNamespace("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd");
        Marc.builder()
                .setInputStream(in)
                .setCharset(StandardCharsets.UTF_8)
                .setFormat("AlephXML")
                .setType("Bibliographic")
                .build()
                .parse(XMLInputFactory.newFactory(), consumer);
        writer.endCollection();
        writer.endDocument();
        sw.close();
        assertNull(writer.getException());
        assertThat(sw.toString(), CompareMatcher.isIdenticalTo(getClass().getResource(s + "-eventconsumer.xml").openStream()));
    }

    @Test
    public void testMarcXchangeWriterWithEventConsumer() throws Exception {
        String s = "HT016424175.xml";
        InputStream in = getClass().getResourceAsStream(s);
        MarcXchangeEventConsumer consumer = new MarcXchangeEventConsumer();
        consumer.addNamespace("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd");
        try (MarcXchangeWriter writer = new MarcXchangeWriter(consumer)) {
            writer.setFormat("AlephXML").setType("Bibliographic");
            writer.startDocument();
            Marc.builder()
                    .setInputStream(in)
                    .setCharset(StandardCharsets.UTF_8)
                    .setFormat("AlephXML")
                    .setType("Bibliographic")
                    .build()
                    .writeCollection();
            writer.endDocument();
            assertNull(writer.getException());
        }
    }

    @Test
    public void testXmlIterable() {
        String s = "chabon.mrc.xml";
        InputStream in = getClass().getResourceAsStream(s);
        AtomicInteger count = new AtomicInteger();
        for (MarcRecord marcRecord : Marc.builder()
                .setInputStream(in)
                .setCharset(StandardCharsets.UTF_8)
                .xmlIterable()) {
            logger.log(Level.INFO, marcRecord.toString());
            count.incrementAndGet();
        }
        assertEquals(2, count.get());
    }

    @Test
    public void testXmlIterator() {
        String s = "HT016424175.xml";
        InputStream in = getClass().getResourceAsStream(s);
        MarcXchangeEventConsumer consumer = new MarcXchangeEventConsumer();
        consumer.addNamespace("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd");
        Iterator<MarcRecord> iterator = Marc.builder()
                .setInputStream(in)
                .setCharset(StandardCharsets.UTF_8)
                .xmlRecordIterator(consumer);
        AtomicInteger count = new AtomicInteger();
        while (iterator.hasNext()) {
            logger.log(Level.INFO, iterator.next().toString());
            count.incrementAndGet();
        }
        assertEquals(1, count.get());
    }

    @Test
    public void testSRUXMLIterable() {
        String s = "lvi.xml";
        InputStream in = getClass().getResourceAsStream(s);
        AtomicInteger count = new AtomicInteger();
        MarcXchangeEventConsumer consumer = new MarcXchangeEventConsumer();
        MarcRecordIterator iterator = Marc.builder()
                .setInputStream(in)
                .setCharset(StandardCharsets.UTF_8)
                .xmlRecordIterator(consumer);
        while (iterator.hasNext()) {
            MarcRecord marcRecord = iterator.next();
            logger.log(Level.INFO, marcRecord.toString());
            count.incrementAndGet();
        }
        assertEquals(5, count.get());
        assertEquals(5L, iterator.getTotalNumberOfRecords());
    }
}
