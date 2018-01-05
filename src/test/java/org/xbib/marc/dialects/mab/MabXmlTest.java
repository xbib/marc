package org.xbib.marc.dialects.mab;

import static org.junit.Assert.assertThat;
import static org.xbib.helper.StreamMatcher.assertStream;

import org.junit.Test;
import org.xbib.marc.Marc;
import org.xbib.marc.dialects.mab.xml.MabXMLContentHandler;
import org.xbib.marc.json.MarcJsonWriter;
import org.xbib.marc.xml.MarcContentHandler;
import org.xbib.marc.xml.MarcXchangeWriter;
import org.xml.sax.InputSource;
import org.xmlunit.matchers.CompareMatcher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 *
 */
public class MabXmlTest {

    @Test
    public void testMabXml2MarcXchangeExample() throws Exception {
        String s = "mabxml-example.xml";
        File file = File.createTempFile(s + ".", ".xml");
        file.deleteOnExit();
        FileOutputStream out = new FileOutputStream(file);
        try (InputStream in = getClass().getResourceAsStream(s);
             MarcXchangeWriter writer = new MarcXchangeWriter(out, true)
                     .setFormat("MARC21")
                     .setType("Bibliographic")) {
            writer.startDocument();
            writer.beginCollection();
            Marc.builder()
                    .setInputStream(in)
                    .setCharset(StandardCharsets.UTF_8)
                    .setContentHandler(new MabXMLContentHandler()
                            .addNamespace("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd")
                            .setFormat("MabXML")
                            .setType("h")
                            .setMarcListener(writer))
                    .build().xmlReader().parse(new InputSource(in));
            writer.endCollection();
            writer.endDocument();
        }
        assertThat(file, CompareMatcher.isIdenticalTo(getClass().getResource(s + ".xml").openStream()));
    }

    @Test
    public void testMabXml2JsonExample() throws Exception {
        String s = "mabxml-example.xml";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (InputStream in = getClass().getResourceAsStream(s);
             MarcJsonWriter writer = new MarcJsonWriter(out,
                     10, MarcJsonWriter.Style.ELASTICSEARCH_BULK)
                .setIndex("testindex", "testtype")) {
            MarcContentHandler contentHandler = new MabXMLContentHandler()
                    .addNamespace("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd")
                    .setFormat("MabXML")
                    .setType("h")
                    .setMarcListener(writer);
            Marc marc = Marc.builder()
                    .setInputStream(in)
                    .setCharset(StandardCharsets.UTF_8)
                    .setContentHandler(contentHandler)
                    .build();
            marc.xmlReader().parse(new InputSource(in));
        }
        assertStream(s, getClass().getResource(s + ".jsonl").openStream(),
                new ByteArrayInputStream(out.toByteArray()));
    }
}
