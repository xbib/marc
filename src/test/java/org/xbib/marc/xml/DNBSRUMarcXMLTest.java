package org.xbib.marc.xml;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcRecord;
import org.xbib.marc.MarcRecordListener;
import org.xbib.marc.MarcXchangeConstants;
import org.xmlunit.matchers.CompareMatcher;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class DNBSRUMarcXMLTest {

    /**
     * Parsing MARC XML embedded in OAI response from DNB/ZDB.
     * This is tricky, there are no collection elements in the source,
     * so we have to add faked beginCollection()/endCollection() calls.
     * @throws Exception if test fails
     */
    @Test
    public void testOAIMarcXml() throws Exception {
        String s = "zdb-oai-marc.xml";
        InputStream in = getClass().getResourceAsStream(s);
        StringWriter sw = new StringWriter();
        //FileWriter sw = new FileWriter(s + "-marcxchange.xml");
        try (MarcXchangeWriter writer = new MarcXchangeWriter(sw, true)
                .setFormat("MARC21")
                .setType("Bibliographic")) {
            writer.startDocument();
            writer.beginCollection();
            Marc.builder()
                    .setInputStream(in)
                    .setCharset(StandardCharsets.UTF_8)
                    .setContentHandler(new MarcContentHandler()
                            .setFormat("MarcXML")
                            .setType("Bibliographic")
                            .addNamespace("http://www.loc.gov/MARC21/slim")
                            .setTrim(true)
                            .setMarcListener(writer))
                    .build()
                    .xmlReader().parse();
            writer.endCollection();
            writer.endDocument();
            assertNull(writer.getException());
        }
        assertThat(sw.toString(),
                CompareMatcher.isIdenticalTo(getClass().getResource(s + "-marcxchange.xml").openStream()));
    }

    /**
     * Parsing MARC XML "plus", embedded in SRU repsonse from DNB/ZDB.
     * "Plus" means, bibliographic and holdings records are merged in a MARC collection in the SRU result.
     * Each collection encapsulates bibliographic/holding records of the same logical unit.
     * The solution is tricky:
     * <ul>
     *  <li>wrap into a custom element {@code root} around result to allow {@code collection} element sequence</li>
     *  <li>set a MARC content handler with format {@code MarcXML} and
     *  {@code setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)} enables beginCollection()/endCollection</li>
     *  <li>two parallel MARC listeners are required, one for {@code Bibliographic}, one for {@code Holdings}</li>
     *  <li>setType() is not used in MarcXchangeWriter setup in order to preserve the parsed types</li>
     * </ul>
     * @throws Exception if test fails
     */
    @Test
    public void testSRUMarcXmlPlus() throws Exception {
        String s = "zdb-sru-marcxmlplus.xml";
        InputStream in = getClass().getResourceAsStream(s);
        StringWriter sw = new StringWriter();
        try (MarcXchangeWriter writer = new MarcXchangeWriter(sw, true)
             .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)) {
            writer.startDocument();
            writer.startCustomElement("custom", "http://foobar", "root");
            Marc.builder()
                    .setInputStream(in)
                    .setCharset(StandardCharsets.UTF_8)
                    .setContentHandler(new MarcContentHandler()
                            .setFormat("MarcXML")
                            .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
                            .addNamespace("http://www.loc.gov/MARC21/slim")
                            .setMarcListener(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE, writer)
                            .setMarcListener(MarcXchangeConstants.HOLDINGS_TYPE, writer))
                    .build()
                    .xmlReader().parse();
            writer.endCustomElement("custom", "http://foobar", "root");
            writer.endDocument();
            assertNull(writer.getException());
        }
        assertThat(sw.toString(),
                CompareMatcher.isIdenticalTo(getClass().getResource(s + "-marcxchange.xml").openStream()));
    }

    @Test
    public void testSRUMarcXmlPlus1() throws Exception {
        String s = "zdb-sru-marcxmlplus1.xml";
        InputStream in = getClass().getResourceAsStream(s);
        StringWriter sw = new StringWriter();
        //FileWriter sw = new FileWriter("zdb-sru-marcxmlplus1.xml-marcxchange.xml");
        try (MarcXchangeWriter writer = new MarcXchangeWriter(sw, true)
                .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)) {
            writer.startDocument();
            writer.startCustomElement("custom", "http://foobar", "root");
            Marc.builder()
                    .setInputStream(in)
                    .setCharset(StandardCharsets.UTF_8)
                    .setContentHandler(new MarcContentHandler()
                            .setFormat("MarcXML")
                            .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
                            .addNamespace("http://www.loc.gov/MARC21/slim")
                            .setMarcListener(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE, writer)
                            .setMarcListener(MarcXchangeConstants.HOLDINGS_TYPE, writer))
                    .build()
                    .xmlReader().parse();
            writer.endCustomElement("custom", "http://foobar", "root");
            writer.endDocument();
            assertNull(writer.getException());
        }
        sw.close();
        assertThat(sw.toString(),
                CompareMatcher.isIdenticalTo(getClass().getResource(s + "-marcxchange.xml").openStream()));
    }

    @Test
    public void testSRUMarcXmlPlus1RecordListener() throws Exception {
        String s = "zdb-sru-marcxmlplus1.xml";
        InputStream in = getClass().getResourceAsStream(s);
        AtomicBoolean found = new AtomicBoolean();
        MarcContentHandler marcListener = new MarcContentHandler();
        MarcRecordListener marcRecordListener = new MarcRecordListener() {
            @Override
            public void beginCollection() {
            }

            @Override
            public void record(MarcRecord marcRecord) {
                found.set(true);
            }

            @Override
            public void endCollection() {
            }
        };
        // attach record listener
        marcListener.setMarcRecordListener(marcRecordListener);
        Marc.builder()
                .setInputStream(in)
                .setCharset(StandardCharsets.UTF_8)
                .setContentHandler(new MarcContentHandler()
                        .setFormat("MarcXML")
                        .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
                        .addNamespace("http://www.loc.gov/MARC21/slim")
                        .setMarcListener(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE, marcListener)
                        .setMarcListener(MarcXchangeConstants.HOLDINGS_TYPE,marcListener))
                .build()
                .xmlReader()
                .parse();
        assertTrue(found.get());
    }
}