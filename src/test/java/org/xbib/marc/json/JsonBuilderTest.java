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
package org.xbib.marc.json;

import java.io.IOException;
import java.io.StringWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonBuilderTest {

    private StringWriter output;

    private JsonBuilder jsonBuilder;

    private static String string(char... chars) {
        return String.valueOf(chars);
    }

    @BeforeEach
    public void setUp() {
        output = new StringWriter();
        jsonBuilder = new JsonBuilder(output);
    }

    @Test
    public void writeLiteral() throws IOException {
        jsonBuilder.buildValue("foo");
        assertEquals("\"foo\"", output.toString());
    }

    @Test
    public void writeNumber() throws IOException {
        jsonBuilder.buildValue(23);
        assertEquals("23", output.toString());
    }

    @Test
    public void writeStringEmpty() throws IOException {
        jsonBuilder.buildValue("");
        assertEquals("\"\"", output.toString());
    }

    @Test
    public void writeStingescapesBackslashes() throws IOException {
        jsonBuilder.buildValue("foo\\bar");
        assertEquals("\"foo\\\\bar\"", output.toString());
    }

    @Test
    public void writeEmptyArray() throws IOException {
        jsonBuilder.beginCollection();
        jsonBuilder.endCollection();
        assertEquals("[]", output.toString());
    }

    @Test
    public void writeEmptyObject() throws IOException {
        jsonBuilder.beginMap();
        jsonBuilder.endMap();
        assertEquals("{}", output.toString());
    }

    @Test
    public void escapesQuotes() throws IOException {
        jsonBuilder.buildValue("a\"b");
        assertEquals("\"a\\\"b\"", output.toString());
    }

    @Test
    public void escapesEscapedQuotes() throws IOException {
        jsonBuilder.buildValue("foo\\\"bar");
        assertEquals("\"foo\\\\\\\"bar\"", output.toString());
    }

    @Test
    public void escapesNewLine() throws IOException {
        jsonBuilder.buildValue("foo\nbar");
        assertEquals("\"foo\\nbar\"", output.toString());
    }

    @Test
    public void escapesWindowsNewLine() throws IOException {
        jsonBuilder.buildValue("foo\r\nbar");
        assertEquals("\"foo\\r\\nbar\"", output.toString());
    }

    @Test
    public void escapesTabs() throws IOException {
       jsonBuilder.buildValue("foo\tbar");
        assertEquals("\"foo\\tbar\"", output.toString());
    }

    @Test
    public void escapesSpecialCharacters() throws IOException {
        jsonBuilder.buildValue("foo\u2028bar\u2029");
        assertEquals("\"foo\\u2028bar\\u2029\"", output.toString());
    }

    @Test
    public void escapesZeroCharacter() throws IOException {
        jsonBuilder.buildValue(string('f', 'o', 'o', (char) 0, 'b', 'a', 'r'));
        assertEquals("\"foo\\u0000bar\"", output.toString());
    }

    @Test
    public void escapesEscapeCharacter() throws IOException {
        jsonBuilder.buildValue(string('f', 'o', 'o', (char) 27, 'b', 'a', 'r'));
        assertEquals("\"foo\\u001bbar\"", output.toString());
    }

    @Test
    public void escapesControlCharacters() throws IOException {
        jsonBuilder.buildValue(string((char) 1, (char) 8, (char) 15, (char) 16, (char) 31));
        assertEquals("\"\\u0001\\u0008\\u000f\\u0010\\u001f\"", output.toString());
    }

    @Test
    public void escapesFirstChar() throws IOException {
        jsonBuilder.buildValue(string('\\', 'x'));
        assertEquals("\"\\\\x\"", output.toString());
    }

    @Test
    public void escapesLastChar() throws IOException {
        jsonBuilder.buildValue(string('x', '\\'));
        assertEquals("\"x\\\\\"", output.toString());
    }
}