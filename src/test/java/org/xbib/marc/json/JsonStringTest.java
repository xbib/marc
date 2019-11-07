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
package org.xbib.marc.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

/**
 *
 */
public class JsonStringTest extends TestUtil {

    private StringWriter stringWriter;
    private JsonWriter jsonWriter;

    @Before
    public void setUp() {
        stringWriter = new StringWriter();
        jsonWriter = new JsonWriter(stringWriter);
    }

    @Test
    public void constructor_failsWithNull() {
        assertException(NullPointerException.class, null, new Runnable() {
            public void run() {
                new JsonString(null);
            }
        });
    }

    @Test
    public void write() throws IOException {
        new JsonString("foo").write(jsonWriter);

        assertEquals("\"foo\"", stringWriter.toString());
    }

    @Test
    public void write_escapesStrings() throws IOException {
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
    public void equals_trueForSameInstance() {
        JsonString string = new JsonString("foo");

        assertEquals(string, string);
    }

    @Test
    public void equals_trueForEqualStrings() {
        assertEquals(new JsonString("foo"), new JsonString("foo"));
    }

    @Test
    public void equals_falseForDifferentStrings() {
        assertNotEquals(new JsonString(""), new JsonString("foo"));
        assertNotEquals(new JsonString("foo"), new JsonString("bar"));
    }

    @Test
    public void equals_falseForNull() {
        assertNotEquals(null, new JsonString("foo"));
    }

    @Test
    public void equals_falseForSubclass() {
        assertNotEquals(new JsonString("foo"), new JsonString("foo") {
        });
    }

    @Test
    public void hashCode_equalsForEqualStrings() {
        assertEquals(new JsonString("foo").hashCode(), new JsonString("foo").hashCode());
    }

    @Test
    public void hashCode_differsForDifferentStrings() {
        assertNotEquals(new JsonString("").hashCode(), new JsonString("foo").hashCode());
        assertNotEquals(new JsonString("foo").hashCode(), new JsonString("bar").hashCode());
    }
}
