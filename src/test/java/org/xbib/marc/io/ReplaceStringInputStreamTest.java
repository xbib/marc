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
package org.xbib.marc.io;

import static org.xbib.marc.StreamMatcher.assertStream;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ReplaceStringInputStreamTest {

    @Test
    public void testTokenFilterInputStream() throws Exception {
        String original = "";
        String expected = "";
        streamAssert(original, expected);

        original = "x";
        expected = "x";
        streamAssert(original, expected);

        original = "abcdefghijklmnop";
        expected = "abcdefghijklmnop";
        streamAssert(original, expected);

        original = "RED";
        expected = "pear";
        streamAssert(original, expected);

        original = "aRED";
        expected = "apear";
        streamAssert(original, expected);

        original = "REDb";
        expected = "pearb";
        streamAssert(original, expected);

        original = "aREDz";
        expected = "apearz";
        streamAssert(original, expected);

        original = "abREDz";
        expected = "abpearz";
        streamAssert(original, expected);

        original = "abREDyz";
        expected = "abpearyz";
        streamAssert(original, expected);

        original = "abcREDwxyz";
        expected = "abcpearwxyz";
        streamAssert(original, expected);

        original = "abcdREDwxyz";
        expected = "abcdpearwxyz";
        streamAssert(original, expected);

        original = "abcdeREDwxyz";
        expected = "abcdepearwxyz";
        streamAssert(original, expected);

        original = "abcdefghiREDwxyz";
        expected = "abcdefghipearwxyz";
        streamAssert(original, expected);

        original = "abcdefghiREDstuvwxyz";
        expected = "abcdefghipearstuvwxyz";
        streamAssert(original, expected);

        original = "REDBLUE";
        expected = "pearbanana";
        streamAssert(original, expected);

        original = "aREDBLUE";
        expected = "apearbanana";
        streamAssert(original, expected);

        original = "REDBLUEb";
        expected = "pearbananab";
        streamAssert(original, expected);

        original = "aREDzBLUE";
        expected = "apearzbanana";
        streamAssert(original, expected);

        original = "BLUEabREDz";
        expected = "bananaabpearz";
        streamAssert(original, expected);

        original = "abBLUEREDyzGREEN";
        expected = "abbananapearyzgrape";
        streamAssert(original, expected);

        original = "abcdefghiGREENjklREDwxyz";
        expected = "abcdefghigrapejklpearwxyz";
        streamAssert(original, expected);

        original = "abcGREENGREENdeBLUEBLUEfGREENBLUEghiREDstuvwxyz";
        expected = "abcgrapegrapedebananabananafgrapebananaghipearstuvwxyz";
        streamAssert(original, expected);

    }

    private void streamAssert(String original, String expected) throws IOException {
        InputStream expectedInputStream = new ByteArrayInputStream(expected.getBytes(StandardCharsets.UTF_8));
        InputStream in = new ByteArrayInputStream(original.getBytes(StandardCharsets.UTF_8));
        in = new ReplaceStringInputStream(in, "RED", "pear");
        in = new ReplaceStringInputStream(in, "GREEN", "grape");
        in = new ReplaceStringInputStream(in, "BLUE", "banana");
        assertStream("", expectedInputStream, in);
    }

}
