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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcRecord;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public class HBZTest {

    @Test
    public void testMarcStream() throws Exception {
        String[] files = {
                "HBZ.mab"
        };
        for (String file : files) {
            AtomicInteger count = new AtomicInteger();
            try (InputStream in = getClass().getResource(file).openStream()) {
                Marc marc = Marc.builder()
                        .setInputStream(in)
                        .setCharset(StandardCharsets.UTF_8)
                        .build();
                marc.iso2709Stream().chunks().forEach(chunk -> {
                    count.incrementAndGet();
                });
            }
            assertTrue(count.get() > 0);
        }
    }

    @Test
    public void testMarcRecordIterable() throws Exception {
        String[] files = {
                "HBZ.mab"
        };
        for (String file : files) {
            int count = 0;
            try (InputStream in = getClass().getResource(file).openStream()) {
                Marc.Builder builder = Marc.builder()
                        .setFormat("MAB")
                        .setType("Titel")
                        .setInputStream(in)
                        .setCharset(Charset.forName("x-MAB"));
                for (MarcRecord marcRecord : builder.iterable()) {
                    count++;
                }
            }
            assertEquals(1, count);
        }
    }
}
