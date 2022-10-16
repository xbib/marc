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
package org.xbib.marc.dialects.bibliomondo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xbib.marc.Marc;
import org.xbib.marc.xml.MarcXchangeWriter;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BiblioMondoInputStreamTest {

    @Test
    @Disabled("skip this because data license is unclear")
    public void biblioMondoRecords() throws Exception {
        Path path = Paths.get("/data/fix/DE-380/Stbib_Fernleihe_20150427.MARC");
        Path out = Paths.get("/var/tmp/Stbib_Fernleihe_20150427.marcxchange");
        try (InputStream inputStream = Files.newInputStream(path);
             Writer outWriter = Files.newBufferedWriter(out);
             MarcXchangeWriter writer = new MarcXchangeWriter(outWriter, true)) {
            Marc marc = Marc.builder()
                    .setInputStream(inputStream)
                    .setCharset(Charset.forName("ANSEL"))
                    .setMarcListener(writer)
                    .build();
            marc.wrapIntoCollection(marc.bibliomondo());
        }
        // assertThat(sw.toString(), CompareMatcher.isIdenticalTo(getClass().getResource(s + ".xml").openStream()));
    }
}
