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
        try (InputStream marcInputStream = getClass().getResource(s).openStream()) {
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
    }

    @Test
    public void testDom() throws Exception {
        String s = "summerland.mrc";
        try (InputStream marcInputStream = getClass().getResource(s).openStream()) {
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
    }

    @Test
    public void testStylesheetSax() throws Exception {
        String s = "summerland.mrc";
        try (InputStream marcInputStream = getClass().getResource(s).openStream()) {
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
            // required for realtive URI resolving in xsl:include
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
            xslInputStream.close();
            assertThat(out.toByteArray(),
                    CompareMatcher.isIdenticalTo(getClass().getResource("summerland-sax-mods.xml").openStream()));
        }
    }

    @Test
    public void testStylesheetDom() throws Exception {
        String s = "summerland.mrc";
        try (InputStream marcInputStream = getClass().getResource(s).openStream()) {
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
            xslInputStream.close();
            assertThat(out.toByteArray(),
                    CompareMatcher.isIdenticalTo(getClass().getResource("summerland-dom-mods.xml").openStream()));
        }
    }

    /**
     * With regard to sandboxed CI/CD platforms, we avoid loading external XSL style sheets from loc.gov
     * We run the test with a local copy.
     *
     * @throws Exception if test fails
     */
    @Test
    public void testLocLocalCopyStyleSheet() throws Exception {
        String s = "summerland.mrc";
        try (InputStream marcInputStream = getClass().getResource(s).openStream()) {
            Marc marc = Marc.builder()
                    .setInputStream(marcInputStream)
                    .setCharset(Charset.forName("ANSEL"))
                    .setSchema(MARC21_FORMAT)
                    .build();
            StringWriter sw = new StringWriter();
            Result result = new StreamResult(sw);
            URL url = getClass().getResource("MARC21slim2MODS3.xsl");
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setURIResolver(new ClasspathResourceURIResolver());
            marc.transform(factory, url, result);
            assertThat(sw.toString(),
                    CompareMatcher.isIdenticalTo(getClass().getResource("summerland-mods-loc-goc-local-copy.xml").openStream()));
        }
    }

    private static class ClasspathResourceURIResolver implements URIResolver {
        @Override
        public Source resolve(String href, String base) {
            return new StreamSource(getClass().getResourceAsStream(href));
        }
    }
}
