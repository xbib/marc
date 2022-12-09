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
package org.xbib.marc.dialects.mab.diskette;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.xbib.marc.Marc;
import org.xbib.marc.StreamMatcher;
import org.xbib.marc.transformer.value.MarcValueTransformers;
import org.xbib.marc.xml.MarcXchangeWriter;
import java.nio.charset.Charset;

public class MabDisketteTest {

    @Test
    public void testMABDiskette() throws Exception {
        String s = "mgl.txt";
        StreamMatcher.xmlMatch(getClass(), s, ".xml", (inputStream, outputStream) -> {
            try (MarcXchangeWriter writer = new MarcXchangeWriter(outputStream)) {
                Marc marc = Marc.builder()
                        .setInputStream(inputStream)
                        .setCharset(Charset.forName("cp850"))
                        .setMarcListener(writer)
                        .build();
                long l = marc.wrapIntoCollection(marc.mabDisketteLF());
                assertEquals(90, l);
                assertNull(writer.getException());
            }
        });
    }

    @Test
    public void testMABDisketteWithSubfields() throws Exception {
        String s = "DE-Bo410-sample.ma2";
        StreamMatcher.xmlMatch(getClass(), s, ".xml", (inputStream, outputStream) -> {
            // in abstracts content, ctrl-character 0x7 must be replaced for clean XML. We replace it by fffd.
            MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
            marcValueTransformers.setMarcValueTransformer(value -> value.replaceAll("\\u0007", "\ufffd"));
            try (MarcXchangeWriter writer = new MarcXchangeWriter(outputStream, true)
                    .setMarcValueTransformers(marcValueTransformers)) {
                Marc marc = Marc.builder()
                        .setInputStream(inputStream)
                        .setCharset(Charset.forName("cp850"))
                        .setMarcListener(writer)
                        .build();
                long l = marc.wrapIntoCollection(marc.mabDiskettePlusSubfieldsCRLF('$'));
                assertEquals(10007, l);
                assertNull(writer.getException());
            }
        });
    }
}
