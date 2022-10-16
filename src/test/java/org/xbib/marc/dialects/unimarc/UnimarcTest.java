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
package org.xbib.marc.dialects.unimarc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.xbib.marc.Marc;
import org.xbib.marc.xml.MarcXchangeWriter;
import org.xmlunit.matchers.CompareMatcher;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class UnimarcTest {

    /**
     * UNIMARC test.
     * Found at https://github.com/medialab/reference_manager/raw/master/data/unimarc/periouni.mrc
     * License: LGPL
     *
     * @throws Exception if test fails
     */
    @Test
    public void testPerioUni() throws Exception {
        String s = "periouni.mrc";
        File file = File.createTempFile("periouni.", ".xml");
        file.deleteOnExit();
        FileOutputStream out = new FileOutputStream(file);
        try (MarcXchangeWriter writer = new MarcXchangeWriter(out, true)
                .setFormat("UNIMARC")
                .setType("Bibliographic");
             InputStream inputStream = getClass().getResource(s).openStream()) {
            Marc.builder()
                    .setInputStream(inputStream)
                    .setCharset(StandardCharsets.UTF_8)
                    .setMarcListener(writer)
                    .build()
                    .writeCollection();
            assertNull(writer.getException());
        }
        assertThat(file, CompareMatcher.isIdenticalTo(getClass().getResource(s + ".xml").openStream()));
    }

}
