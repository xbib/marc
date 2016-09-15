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
package org.xbib.marc.dialects.pica;

import static org.junit.Assert.assertThat;
import static org.xbib.helper.StreamMatcher.assertStream;

import org.junit.Test;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcField;
import org.xbib.marc.MarcListener;
import org.xbib.marc.MarcXchangeConstants;
import org.xbib.marc.xml.MarcXchangeWriter;
import org.xmlunit.matchers.CompareMatcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 *
 */
public class PicaTest {

    @Test
    public void testPicaBinary() throws Exception {
        String s = "pica.binary";
        InputStream in = getClass().getResource(s).openStream();
        File file = File.createTempFile(s + ".", ".xml");
        file.deleteOnExit();
        FileOutputStream out = new FileOutputStream(file);
        try (MarcXchangeWriter writer = new MarcXchangeWriter(out, true)) {
            Marc marc = Marc.builder()
                    .setInputStream(in)
                    .setCharset(StandardCharsets.UTF_8)
                    .setMarcListener(writer)
                    .build();
            marc.wrapIntoCollection(marc.pica());
        }
        assertThat(file, CompareMatcher.isIdenticalTo(getClass().getResource(s + ".xml").openStream()));
    }

    @Test
    public void testPicaPlain() throws Exception {
        for (String s : new String[]{
                "pica.plain",
                "bgb.example"
        }) {
            InputStream in = getClass().getResource(s).openStream();
            File file = File.createTempFile(s + ".", ".xml");
            file.deleteOnExit();
            FileOutputStream out = new FileOutputStream(file);
            try (MarcXchangeWriter writer = new MarcXchangeWriter(out, true)
                    .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                    .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
            ) {
                Marc marc = Marc.builder()
                        .setInputStream(in)
                        .setCharset(StandardCharsets.UTF_8)
                        .setMarcListener(writer)
                        .build();
                marc.wrapIntoCollection(marc.picaPlain());
            }
            assertThat(file, CompareMatcher.isIdenticalTo(getClass().getResource(s + ".xml").openStream()));
        }
    }

    @Test
    public void testPicaXML() throws Exception {
        for (String s : new String[]{
                "zdb-oai-bib.xml",
                "sru_picaxml.xml"
        }) {
            InputStream in = getClass().getResourceAsStream(s);
            StringBuilder sb = new StringBuilder();

            MarcListener listener = new MarcListener() {
                @Override
                public void beginCollection() {
                }

                @Override
                public void endCollection() {
                }

                @Override
                public void leader(String label) {
                    sb.append("leader=").append(label).append("\n");
                }

                @Override
                public void beginRecord(String format, String type) {
                    sb.append("beginRecord\n")
                            .append("format=").append(format).append("\n")
                            .append("type=").append(type).append("\n");
                }

                @Override
                public void field(MarcField field) {
                    sb.append("field=").append(field).append("\n");
                }

                @Override
                public void endRecord() {
                    sb.append("endRecord\n");
                }

            };
            PicaXMLContentHandler contentHandler = new PicaXMLContentHandler();
            contentHandler.setFormat("Pica");
            contentHandler.setType("XML");
            contentHandler.setMarcListener(listener);
            Marc marc = Marc.builder()
                    .setInputStream(in)
                    .setContentHandler(contentHandler)
                    .build();
            marc.xmlReader().parse();
            assertStream(s, getClass().getResource(s + "-keyvalue.txt").openStream(),
                    sb.toString());
        }
    }
}
