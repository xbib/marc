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

import org.junit.jupiter.api.Test;
import org.xbib.marc.xml.MarcXchangeWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * This test reads ISO 2709 MARC files, writes them to XML, parses the XML and writes back ISO 2709.
 */
public class MarcRoundTripTest {

    @Test
    public void testProperMarc() throws Exception {
        for (String s : new String[]{
                "summerland2.mrc",
                //"chabon.mrc",
                //"chabon-loc.mrc"
        }) {
            StreamMatcher.roundtrip(getClass(), s, ".xml",
                    (inputStream, outputStream) -> {
                        try (MarcXchangeWriter writer = new MarcXchangeWriter(outputStream)) {
                            Marc.builder()
                                    .setInputStream(inputStream)
                                    .setCharset(Charset.forName("ANSEL"))
                                    .setMarcListener(writer)
                                    .build()
                                    .writeCollection();
                        }
                    }, (inputStream, outputStream) -> {
                        try (MarcWriter writer = new MarcWriter(outputStream, StandardCharsets.UTF_8)) {
                            Marc.builder()
                                    .setInputStream(inputStream)
                                    .setMarcListener(writer)
                                    .build()
                                    .xmlReader().parse();
                        }
                    });
        }
    }
}
