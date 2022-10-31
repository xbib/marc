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
package org.xbib.marc.tools;

import org.junit.jupiter.api.Test;

public class ToolTest {

    @Test
    public void testToolSimple() {
        String[] args = {
            "--in", "src/test/resources/org/xbib/marc/chabon.mrc",
            "--charset", "ANSEL",
            "--out", "build/chabon.mrc.xml"
        };
        MarcTool.main(args);
    }

    @Test
    public void testToolStylesheet() {
        String[] args = {
                "--in", "src/test/resources/org/xbib/marc/summerland.mrc",
                "--out", "build/summerland.mrc.xml",
                "--charset", "ANSEL",
                "--schema", "MARC21",
                "--stylesheet", "http://www.loc.gov/standards/mods/v3/MARC21slim2MODS3.xsl",
                "--result", "build/summerland.mods"
        };
        MarcTool.main(args);
    }
}
