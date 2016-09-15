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
package org.xbib.marc.tools;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

/**
 */
public class ToolTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Test
    public void testToolSimple() throws Exception {
        String[] args = {
            "--in", "src/test/resources/org/xbib/marc/chabon.mrc",
            "--out", "build/chabon.mrc.xml"
        };
        exit.expectSystemExitWithStatus(0);
        MarcTool.main(args);
    }

    @Test
    public void testToolStylesheet() throws Exception {
        String[] args = {
                "--in", "src/test/resources/org/xbib/marc/summerland.mrc",
                "--out", "build/summerland.mrc.xml",
                "--schema", "MARC21",
                "--stylesheet", "http://www.loc.gov/standards/mods/v3/MARC21slim2MODS3.xsl",
                "--result", "build/summerland.mods"
        };
        exit.expectSystemExitWithStatus(0);
        MarcTool.main(args);
    }
}
