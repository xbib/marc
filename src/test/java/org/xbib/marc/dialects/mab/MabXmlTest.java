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
package org.xbib.marc.dialects.mab;

import org.junit.jupiter.api.Test;
import org.xbib.marc.Marc;
import org.xbib.marc.StreamMatcher;
import org.xbib.marc.dialects.mab.xml.MabXMLContentHandler;
import org.xbib.marc.json.MarcJsonWriter;
import org.xbib.marc.xml.MarcContentHandler;
import org.xbib.marc.xml.MarcXchangeWriter;
import org.xml.sax.InputSource;
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
            try (MarcJsonWriter writer = new MarcJsonWriter(outputStream, 10)
                    .setStyle(EnumSet.of(MarcJsonWriter.Style.ELASTICSEARCH_BULK))
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
