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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 *
 */
public class JsonTest extends TestUtil {

    @Test
    public void literalConstants() {
        assertTrue(JsonLiteral.NULL.isNull());
        assertTrue(JsonLiteral.TRUE.isTrue());
        assertTrue(JsonLiteral.FALSE.isFalse());
    }

    @Test
    public void valueInt() {
        assertEquals("0", Json.of(0).toString());
        assertEquals("23", Json.of(23).toString());
        assertEquals("-1", Json.of(-1).toString());
        assertEquals("2147483647", Json.of(Integer.MAX_VALUE).toString());
        assertEquals("-2147483648", Json.of(Integer.MIN_VALUE).toString());
    }

    @Test
    public void valueLong() {
        assertEquals("0", Json.of(0L).toString());
        assertEquals("9223372036854775807", Json.of(Long.MAX_VALUE).toString());
        assertEquals("-9223372036854775808", Json.of(Long.MIN_VALUE).toString());
    }

    @Test
    public void valueFloat() {
        assertEquals("23.5", Json.of(23.5f).toString());
        assertEquals("-3.1416", Json.of(-3.1416f).toString());
        assertEquals("1.23E-6", Json.of(0.00000123f).toString());
        assertEquals("-1.23E7", Json.of(-12300000f).toString());
    }

    @Test
    public void valueFloatCutsOffPointZero() {
        assertEquals("0", Json.of(0f).toString());
        assertEquals("-1", Json.of(-1f).toString());
        assertEquals("10", Json.of(10f).toString());
    }

    @Test
    public void valueFloatFailsWithInfinity() {
        String message = "Infinite and NaN values not permitted in JSON";
        assertException(IllegalArgumentException.class, message,
                (Runnable) () -> Json.of(Float.POSITIVE_INFINITY));
    }

    @Test
    public void valuefloatfailsWithNaN() {
        String message = "Infinite and NaN values not permitted in JSON";
        assertException(IllegalArgumentException.class, message, (Runnable) () -> Json.of(Float.NaN));
    }

    @Test
    public void valueDouble() {
        assertEquals("23.5", Json.of(23.5d).toString());
        assertEquals("3.1416", Json.of(3.1416d).toString());
        assertEquals("1.23E-6", Json.of(0.00000123d).toString());
        assertEquals("1.7976931348623157E308", Json.of(1.7976931348623157E308d).toString());
    }

    @Test
    public void valueDoublecutsOffPointZero() {
        assertEquals("0", Json.of(0d).toString());
        assertEquals("-1", Json.of(-1d).toString());
        assertEquals("10", Json.of(10d).toString());
    }

    @Test
    public void valuedoublefailsWithInfinity() {
        String message = "Infinite and NaN values not permitted in JSON";
        assertException(IllegalArgumentException.class, message, (Runnable) () -> Json.of(Double.POSITIVE_INFINITY));
    }

    @Test
    public void valuedoublefailsWithNaN() {
        String message = "Infinite and NaN values not permitted in JSON";
        assertException(IllegalArgumentException.class, message, (Runnable) () -> Json.of(Double.NaN));
    }

    @Test
    public void valueboolean() {
        assertSame(JsonLiteral.TRUE, Json.of(true));
        assertSame(JsonLiteral.FALSE, Json.of(false));
    }

    @Test
    public void valuestring() {
        assertEquals("", Json.of("").asString());
        assertEquals("Hello", Json.of("Hello").asString());
        assertEquals("\"Hello\"", Json.of("\"Hello\"").asString());
    }

    @Test
    public void valuestringtoleratesNull() {
        assertSame(JsonLiteral.NULL, Json.of(null));
    }

    @Test
    public void array() {
        assertEquals(new JsonArray(), Json.array());
    }

    @Test
    public void arrayint() {
        assertEquals(new JsonArray().add(23), Json.array(23));
        assertEquals(new JsonArray().add(23).add(42), Json.array(23, 42));
    }

    @Test
    public void arrayintfailsWithNull() {
        assertException(NullPointerException.class, null, (Runnable) () -> Json.array((int[]) null));
    }

    @Test
    public void arraylong() {
        assertEquals(new JsonArray().add(23L), Json.array(23L));
        assertEquals(new JsonArray().add(23L).add(42L), Json.array(23L, 42L));
    }

    @Test
    public void arraylongfailsWithNull() {
        assertException(NullPointerException.class, null, (Runnable) () -> Json.array((long[]) null));
    }

    @Test
    public void arrayfloat() {
        assertEquals(new JsonArray().add(3.14f), Json.array(3.14f));
        assertEquals(new JsonArray().add(3.14f).add(1.41f), Json.array(3.14f, 1.41f));
    }

    @Test
    public void arrayfloatfailsWithNull() {
        assertException(NullPointerException.class, null, (Runnable) () -> Json.array((float[]) null));
    }

    @Test
    public void arraydouble() {
        assertEquals(new JsonArray().add(3.14d), Json.array(3.14d));
        assertEquals(new JsonArray().add(3.14d).add(1.41d), Json.array(3.14d, 1.41d));
    }

    @Test
    public void arraydoublefailsWithNull() {
        assertException(NullPointerException.class, null, (Runnable) () -> Json.array((double[]) null));
    }

    @Test
    public void arrayboolean() {
        assertEquals(new JsonArray().add(true), Json.array(true));
        assertEquals(new JsonArray().add(true).add(false), Json.array(true, false));
    }

    @Test
    public void arraybooleanfailsWithNull() {
        assertException(NullPointerException.class, null, (Runnable) () -> Json.array((boolean[]) null));
    }

    @Test
    public void arraystring() {
        assertEquals(new JsonArray().add("foo"), Json.array("foo"));
        assertEquals(new JsonArray().add("foo").add("bar"), Json.array("foo", "bar"));
    }

    @Test
    public void arraystringfailsWithNull() {
        assertException(NullPointerException.class, null, (Runnable) () -> Json.array((String[]) null));
    }

    @Test
    public void object() {
        assertEquals(new JsonObject(), Json.object());
    }

    @Test
    public void parsestring() throws IOException {
        assertEquals(Json.of(23), Json.parse("23"));
    }

    @Test
    public void parsestringfailsWithNull() {
        assertException(NullPointerException.class, null, (Runnable) () -> {
            try {
                Json.parse((String) null);
            } catch (IOException e) {
                //
            }
        });
    }

    @Test
    public void parseReader() throws IOException {
        Reader reader = new StringReader("23");
        assertEquals(Json.of(23), Json.parse(reader));
    }
}
