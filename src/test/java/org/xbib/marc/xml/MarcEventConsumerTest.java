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
package org.xbib.marc.xml;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.marc.Marc;
import org.xmlunit.matchers.CompareMatcher;

import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 *
 */
public class MarcEventConsumerTest extends Assert {

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
                .parseEvents(consumer);
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
        MarcXchangeWriter writer = new MarcXchangeWriter(consumer);
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
