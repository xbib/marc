package org.xbib.marc.dialects.mab;

import static org.xbib.marc.StreamMatcher.assertStream;
import org.junit.jupiter.api.Test;
import org.xbib.marc.Marc;
import org.xbib.marc.StreamMatcher;
import org.xbib.marc.dialects.mab.xml.MabXMLContentHandler;
import org.xbib.marc.json.MarcJsonWriter;
import org.xbib.marc.xml.MarcContentHandler;
import org.xbib.marc.xml.MarcXchangeWriter;
import org.xml.sax.InputSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;

public class MabXmlTest {

    @Test
    public void testMabXml2MarcXchangeExample() throws Exception {
        String s = "mabxml-example.xml";
        StreamMatcher.xmlMatch(getClass(), s, ".xml", (inputStream, outputStream) -> {
            try (MarcXchangeWriter writer = new MarcXchangeWriter(outputStream, true)
                         .setFormat("MARC21")
                         .setType("Bibliographic")) {
                writer.startDocument();
                writer.beginCollection();
                Marc.builder()
                        .setInputStream(inputStream)
                        .setCharset(StandardCharsets.UTF_8)
                        .setContentHandler(new MabXMLContentHandler()
                                .addNamespace("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd")
                                .setFormat("MabXML")
                                .setType("h")
                                .setMarcListener(writer))
                        .build().xmlReader().parse(new InputSource(inputStream));
                writer.endCollection();
                writer.endDocument();
            }
        });
    }

    @Test
    public void testMabXml2JsonExample() throws Exception {
        String s = "mabxml-example.xml";
        StreamMatcher.fileMatch(getClass(), s, ".jsonl", (inputStream, outputStream) -> {
            try (MarcJsonWriter writer = new MarcJsonWriter(outputStream,
                    10, EnumSet.of(MarcJsonWriter.Style.ELASTICSEARCH_BULK))
                    .setIndex("testindex", "testtype")) {
                MarcContentHandler contentHandler = new MabXMLContentHandler()
                        .addNamespace("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd")
                        .setFormat("MabXML")
                        .setType("h")
                        .setMarcListener(writer);
                Marc marc = Marc.builder()
                        .setInputStream(inputStream)
                        .setCharset(StandardCharsets.UTF_8)
                        .setContentHandler(contentHandler)
                        .build();
                marc.xmlReader().parse(new InputSource(inputStream));
            }
        });
    }
}
