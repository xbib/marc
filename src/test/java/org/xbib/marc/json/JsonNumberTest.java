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
public class JsonNumberTest extends TestUtil {

    private StringWriter output;
    private JsonWriter writer;

    @Before
    public void setUp() {
        output = new StringWriter();
        writer = new JsonWriter(output);
    }

    @Test
    public void constructorfailsWithNull() {
        assertException(NullPointerException.class, null, (Runnable) () -> new JsonNumber(null));
    }

    @Test
    public void write() throws IOException {
        new JsonNumber("23").write(writer);
        assertEquals("23", output.toString());
    }

    @Test
    public void toStringreturnsInputString() {
        assertEquals("foo", new JsonNumber("foo").toString());
    }

    @Test
    public void isInt() {
        assertTrue(new JsonNumber("23").isInt());
    }

    @Test
    public void asInt() {
        assertEquals(23, new JsonNumber("23").asInt());
    }

    @Test(expected = NumberFormatException.class)
    public void asIntfailsWithExceedingValues() {
        new JsonNumber("10000000000").asInt();
    }

    @Test(expected = NumberFormatException.class)
    public void asIntfailsWithExponent() {
        new JsonNumber("1e5").asInt();
    }

    @Test(expected = NumberFormatException.class)
    public void asIntfailsWithFractional() {
        new JsonNumber("23.5").asInt();
    }

    @Test
    public void asLong() {
        assertEquals(23L, new JsonNumber("23").asLong());
    }

    @Test(expected = NumberFormatException.class)
    public void asLongfailsWithExceedingValues() {
        new JsonNumber("10000000000000000000").asLong();
    }

    @Test(expected = NumberFormatException.class)
    public void asLongfailsWithExponent() {
        new JsonNumber("1e5").asLong();
    }

    @Test(expected = NumberFormatException.class)
    public void asLongfailsWithFractional() {
        new JsonNumber("23.5").asLong();
    }

    @Test
    public void asFloat() {
        assertEquals(23.05f, new JsonNumber("23.05").asFloat(), 0);
    }

    @Test
    public void asFloatreturnsInfinityForExceedingValues() {
        assertEquals(Float.POSITIVE_INFINITY, new JsonNumber("1e50").asFloat(), 0);
        assertEquals(Float.NEGATIVE_INFINITY, new JsonNumber("-1e50").asFloat(), 0);
    }

    @Test
    public void asDouble() {
        double result = new JsonNumber("23.05").asDouble();
        assertEquals(23.05, result, 0);
    }

    @Test
    public void asDoublereturnsInfinityForExceedingValues() {
        assertEquals(Double.POSITIVE_INFINITY, new JsonNumber("1e500").asDouble(), 0);
        assertEquals(Double.NEGATIVE_INFINITY, new JsonNumber("-1e500").asDouble(), 0);
    }

    @Test
    public void equalstrueForSameInstance() {
        JsonNumber number = new JsonNumber("23");
        assertEquals(number, number);
    }

    @Test
    public void equalstrueForEqualNumberStrings() {
        assertEquals(new JsonNumber("23"), new JsonNumber("23"));
    }

    @Test
    public void equalsfalseForDifferentNumberStrings() {
        assertNotEquals(new JsonNumber("23"), new JsonNumber("42"));
        assertNotEquals(new JsonNumber("1e+5"), new JsonNumber("1e5"));
    }

    @Test
    public void equalsfalseForNull() {
        assertNotEquals(null, new JsonNumber("23"));
    }

    @Test
    public void equalsfalseForSubclass() {
        assertNotEquals(new JsonNumber("23"), new JsonNumber("23") {
        });
    }

    @Test
    public void hashCodeequalsForEqualStrings() {
        assertEquals(new JsonNumber("23").hashCode(), new JsonNumber("23").hashCode());
    }

    @Test
    public void hashCodediffersForDifferentStrings() {
        assertNotEquals(new JsonNumber("23").hashCode(), new JsonNumber("42").hashCode());
    }
}
