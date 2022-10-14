package org.xbib.marc.xml;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.xbib.marc.Marc;
import org.xmlunit.matchers.CompareMatcher;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.xml.stream.XMLInputFactory;

/**
 *
 */
public class MarcEventConsumerTest {

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
                .parseEvents(XMLInputFactory.newFactory(), consumer);
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
