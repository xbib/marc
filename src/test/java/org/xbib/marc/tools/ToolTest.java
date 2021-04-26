package org.xbib.marc.tools;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("system exit ends testing?")
public class ToolTest {

    @Test
    @ExpectedSystemExit
    public void testToolSimple(ExpectedSystemExit.ExitCapture exitCapture) {
        String[] args = {
            "--in", "src/test/resources/org/xbib/marc/chabon.mrc",
            "--charset", "ANSEL",
            "--out", "build/chabon.mrc.xml"
        };
        exitCapture.expectSystemExitWithStatus(0);
        MarcTool.main(args);
    }

    @Test
    @ExpectedSystemExit
    public void testToolStylesheet(ExpectedSystemExit.ExitCapture exitCapture) {
        String[] args = {
                "--in", "src/test/resources/org/xbib/marc/summerland.mrc",
                "--out", "build/summerland.mrc.xml",
                "--charset", "ANSEL",
                "--schema", "MARC21",
                "--stylesheet", "http://www.loc.gov/standards/mods/v3/MARC21slim2MODS3.xsl",
                "--result", "build/summerland.mods"
        };
        exitCapture.expectSystemExitWithStatus(0);
        MarcTool.main(args);
    }
}
