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
package org.xbib.marc.dialects.bibliomondo;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xbib.marc.Marc;
import org.xbib.marc.xml.MarcXchangeWriter;
import org.xmlunit.matchers.CompareMatcher;

import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

/**
 *
 */
public class BiblioMondoInputStreamTest extends Assert {

    @Test
    @Ignore("skip this because data license is unclear")
    public void biblioMondoRecords() throws Exception {
        for (String s : new String[]{
                "bibliomondo.marc",
                "bibliomondo2.marc"
        }) {
            InputStream in = getClass().getResource(s).openStream();
            StringWriter sw = new StringWriter();
            try (MarcXchangeWriter writer = new MarcXchangeWriter(sw, true)) {
                Marc marc = Marc.builder()
                        .setInputStream(in)
                        .setCharset(Charset.forName("ANSEL"))
                        .setMarcListener(writer)
                        .build();
                marc.wrapIntoCollection(marc.bibliomondo());
            }
            assertThat(sw.toString(), CompareMatcher.isIdenticalTo(getClass().getResource(s + ".xml").openStream()));
        }
    }
}
