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
package org.xbib.marc.dialects.sisis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcXchangeConstants;
import org.xbib.marc.StreamMatcher;
import org.xbib.marc.transformer.field.MarcFieldTransformer;
import org.xbib.marc.transformer.field.MarcFieldTransformers;
import org.xbib.marc.xml.MarcXchangeWriter;
import java.nio.charset.StandardCharsets;

public class SisisTest {

    /**
     * Test file donated from DE-A96.
     *
     * @throws Exception if test fails
     */
    @Test
    public void testSisis() throws Exception {
        StreamMatcher.xmlMatch(getClass(), "unloaddipl", ".xml", (inputStream, outputStream) -> {
            try (MarcXchangeWriter writer = new MarcXchangeWriter(outputStream)
                    .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)) {
                Marc marc = Marc.builder()
                        .setInputStream(inputStream)
                        .setCharset(StandardCharsets.UTF_8)
                        .setMarcListener(writer)
                        .build();
                long l = marc.wrapIntoCollection(marc.sisis());
                assertEquals(36353, l);
            }
        });
    }

    /**
     * Test file donated from DE-836.
     *
     * @throws Exception if test fails
     */
    @Test
    public void testSisisMapped() throws Exception {
        StreamMatcher.xmlMatch(getClass(), "testTit.tit", ".xml", (inputStream, outputStream) -> {
            MarcFieldTransformers transformers = new MarcFieldTransformers();
            MarcFieldTransformer t0 = MarcFieldTransformer.builder()
                    .fromTo("663$01$a", "662$01$x")
                    .fromTo("663$02$a", "662$02$x")
                    .fromTo("663$21$a", "662$21$x")
                    .fromTo("663$22$a", "662$22$x")
                    .operator(MarcFieldTransformer.Operator.HEAD)
                    .build();
            transformers.add(t0);
            try (MarcXchangeWriter writer = new MarcXchangeWriter(outputStream, true)
                    .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)) {
                Marc marc = Marc.builder()
                        .setInputStream(inputStream)
                        .setCharset(StandardCharsets.UTF_8)
                        .setMarcListener(writer)
                        .setMarcFieldTransformers(transformers)
                        .build();
                long l = marc.wrapIntoCollection(marc.sisisCRLF());
                assertEquals(1246, l);
            }
        });
    }
}
