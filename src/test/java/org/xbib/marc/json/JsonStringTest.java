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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.StringWriter;

public class JsonStringTest {

    private StringWriter stringWriter;

    private JsonWriter jsonWriter;

    @BeforeEach
    public void setUp() {
        stringWriter = new StringWriter();
        jsonWriter = new JsonWriter(stringWriter);
    }

    @Test
    public void constructorFailsWithNull() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            new JsonString(null);
        });
    }

    @Test
    public void write() throws IOException {
        new JsonString("foo").write(jsonWriter);
        assertEquals("\"foo\"", stringWriter.toString());
    }

    @Test
    public void writeEscapesStrings() throws IOException {
        new JsonString("foo\\bar").write(jsonWriter);
        assertEquals("\"foo\\\\bar\"", stringWriter.toString());
    }

    @Test
    public void isString() {
        assertTrue(new JsonString("foo").isString());
    }

    @Test
    public void asString() {
        assertEquals("foo", new JsonString("foo").asString());
    }

    @Test
    public void equalsTrueForSameInstance() {
        JsonString string = new JsonString("foo");
        assertEquals(string, string);
    }

    @Test
    public void equalsTrueForEqualStrings() {
        assertEquals(new JsonString("foo"), new JsonString("foo"));
    }

    @Test
    public void equalsFalseForDifferentStrings() {
        assertNotEquals(new JsonString(""), new JsonString("foo"));
        assertNotEquals(new JsonString("foo"), new JsonString("bar"));
    }

    @Test
    public void equalsFalseForNull() {
        assertNotEquals(null, new JsonString("foo"));
    }

    @Test
    public void equalsFalseForSubclass() {
        assertNotEquals(new JsonString("foo"), new JsonString("foo") {
        });
    }

    @Test
    public void hashCodeEqualsForEqualStrings() {
        assertEquals(new JsonString("foo").hashCode(), new JsonString("foo").hashCode());
    }

    @Test
    public void hashCodeDiffersForDifferentStrings() {
        assertNotEquals(new JsonString("").hashCode(), new JsonString("foo").hashCode());
        assertNotEquals(new JsonString("foo").hashCode(), new JsonString("bar").hashCode());
    }
}
