package org.xbib.marc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.xbib.marc.MarcXchangeConstants.MARC21_FORMAT;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xbib.marc.xml.Sax2Dom;
import org.xml.sax.InputSource;
import org.xmlunit.matchers.CompareMatcher;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class MarcToModsTest {

    @Test
    public void testSax() throws Exception {
        String s = "summerland.mrc";
        InputStream marcInputStream = getClass().getResource(s).openStream();
        Marc marc = Marc.builder()
                .setInputStream(marcInputStream)
                .setCharset(Charset.forName("ANSEL"))
                .setSchema(MARC21_FORMAT)
                .build();
        Source source = new SAXSource(marc.iso2709XmlReader(), new InputSource(marcInputStream));
        StringWriter writer = new StringWriter();
        StreamResult streamResult = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.transform(source, streamResult);
        assertThat(writer.toString(),
                CompareMatcher.isIdenticalTo(getClass().getResource("summerland-sax-marc.xml").openStream()));
    }

    @Test
    public void testDom() throws Exception {
        String s = "summerland.mrc";
        InputStream marcInputStream = getClass().getResource(s).openStream();
        Marc marc = Marc.builder()
                .setInputStream(marcInputStream)
                .setCharset(Charset.forName("ANSEL"))
                .setSchema(MARC21_FORMAT)
                .build();
        Sax2Dom sax2Dom = new Sax2Dom(marc.iso2709XmlReader(), new InputSource(marcInputStream));
        Document document = sax2Dom.document();
        StringWriter writer = new StringWriter();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        assertThat(writer.toString(),
                CompareMatcher.isIdenticalTo(getClass().getResource("summerland-dom-marc.xml").openStream()));
    }

    @Test
    public void testStylesheetSax() throws Exception {
        String s = "summerland.mrc";
        InputStream marcInputStream = getClass().getResource(s).openStream();
        Marc marc = Marc.builder()
                .setInputStream(marcInputStream)
                .setCharset(Charset.forName("ANSEL"))
                .setSchema(MARC21_FORMAT)
                .build();
        Source source = new SAXSource(marc.iso2709XmlReader(), new InputSource(marcInputStream));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamResult streamResult = new StreamResult(out);
        InputStream xslInputStream = getClass().getResourceAsStream("MARC21slim2MODS3-6.xsl");
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setURIResolver(new ClasspathResourceURIResolver());
        Transformer transformer = factory.newTemplates(new StreamSource(xslInputStream)).newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setErrorListener(new ErrorListener() {
            @Override
            public void warning(TransformerException exception) throws TransformerException {
                exception.printStackTrace();
            }

            @Override
            public void error(TransformerException exception) throws TransformerException {
                exception.printStackTrace();
            }

            @Override
            public void fatalError(TransformerException exception) throws TransformerException {
                exception.printStackTrace();
            }
        });
        transformer.transform(source, streamResult);
        marcInputStream.close();
        xslInputStream.close();
        assertThat(out.toByteArray(),
                CompareMatcher.isIdenticalTo(getClass().getResource("summerland-sax-mods.xml").openStream()));
    }

    @Test
    public void testStylesheetDom() throws Exception {
        String s = "summerland.mrc";
        InputStream marcInputStream = getClass().getResource(s).openStream();
        Marc marc = Marc.builder()
                .setInputStream(marcInputStream)
                .setCharset(Charset.forName("ANSEL"))
                .setSchema(MARC21_FORMAT)
                .build();
        Sax2Dom sax2Dom = new Sax2Dom(marc.iso2709XmlReader(), new InputSource(marcInputStream));
        Document document = sax2Dom.document();
        Source source = new DOMSource(document);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamResult streamResult = new StreamResult(out);
        InputStream xslInputStream = getClass().getResourceAsStream("MARC21slim2MODS3-6.xsl");
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setURIResolver(new ClasspathResourceURIResolver());
        Transformer transformer = factory.newTemplates(new StreamSource(xslInputStream)).newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setErrorListener(new ErrorListener() {
            @Override
            public void warning(TransformerException exception) throws TransformerException {
                exception.printStackTrace();
            }

            @Override
            public void error(TransformerException exception) throws TransformerException {
                exception.printStackTrace();
            }

            @Override
            public void fatalError(TransformerException exception) throws TransformerException {
                exception.printStackTrace();
            }
        });
        transformer.transform(source, streamResult);
        marcInputStream.close();
        xslInputStream.close();
        assertThat(out.toByteArray(),
                CompareMatcher.isIdenticalTo(getClass().getResource("summerland-dom-mods.xml").openStream()));
    }

    @Test
    public void testLocStyleSheet() throws Exception {
        String s = "summerland.mrc";
        InputStream marcInputStream = getClass().getResource(s).openStream();
        Marc marc = Marc.builder()
                .setInputStream(marcInputStream)
                .setCharset(Charset.forName("ANSEL"))
                .setSchema(MARC21_FORMAT)
                .build();
        StringWriter sw = new StringWriter();
        Result result = new StreamResult(sw);
        System.setProperty("http.agent", "Java Agent");
        marc.transform(TransformerFactory.newInstance(),
                new URL("http://www.loc.gov/standards/mods/v3/MARC21slim2MODS3.xsl"), result);
        assertThat(sw.toString(),
                CompareMatcher.isIdenticalTo(getClass().getResource("summerland-mods-loc-goc.xml").openStream()));
    }


    private static class ClasspathResourceURIResolver implements URIResolver {
        @Override
        public Source resolve(String href, String base) throws TransformerException {
            return new StreamSource(getClass().getResourceAsStream(href));
        }
    }
}
