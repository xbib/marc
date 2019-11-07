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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.xbib.marc.json.JsonLiteral.FALSE;
import static org.xbib.marc.json.JsonLiteral.NULL;
import static org.xbib.marc.json.JsonLiteral.TRUE;

import org.junit.Test;

import java.io.IOException;

/**
 *
 */
public class JsonLiteralTest {

    @Test
    public void isNull() {
        assertTrue(NULL.isNull());
        assertFalse(TRUE.isNull());
        assertFalse(FALSE.isNull());
    }

    @Test
    public void isTrue() {
        assertTrue(TRUE.isTrue());
        assertFalse(NULL.isTrue());
        assertFalse(FALSE.isTrue());
    }

    @Test
    public void isFalse() {
        assertTrue(FALSE.isFalse());
        assertFalse(NULL.isFalse());
        assertFalse(TRUE.isFalse());
    }

    @Test
    public void isBoolean() {
        assertTrue(TRUE.isBoolean());
        assertTrue(FALSE.isBoolean());
        assertFalse(NULL.isBoolean());
    }

    @Test
    public void nullwrite() throws IOException {
        JsonWriter writer = mock(JsonWriter.class);
        NULL.write(writer);
        verify(writer).writeLiteral("null");
        verifyNoMoreInteractions(writer);
    }

    @Test
    public void truewrite() throws IOException {
        JsonWriter writer = mock(JsonWriter.class);
        TRUE.write(writer);
        verify(writer).writeLiteral("true");
        verifyNoMoreInteractions(writer);
    }

    @Test
    public void falsewrite() throws IOException {
        JsonWriter writer = mock(JsonWriter.class);
        FALSE.write(writer);
        verify(writer).writeLiteral("false");
        verifyNoMoreInteractions(writer);
    }

    @Test
    public void nulltoString() {
        assertEquals("null", NULL.toString());
    }

    @Test
    public void truetoString() {
        assertEquals("true", TRUE.toString());
    }

    @Test
    public void falsetoString() {
        assertEquals("false", FALSE.toString());
    }

    @Test
    public void nullequals() {
        assertEquals(NULL, NULL);
        assertNotEquals(null, NULL);
        assertNotEquals(NULL, TRUE);
        assertNotEquals(NULL, FALSE);
        assertNotEquals(NULL, Json.of("null"));
    }

    @Test
    public void trueequals() {
        assertEquals(TRUE, TRUE);
        assertNotEquals(null, TRUE);
        assertNotEquals(TRUE, FALSE);
        assertNotEquals(TRUE, Boolean.TRUE);
        assertNotEquals(NULL, Json.of("true"));
    }

    @Test
    public void falseequals() {
        assertEquals(FALSE, FALSE);
        assertNotEquals(null, FALSE);
        assertNotEquals(FALSE, TRUE);
        assertNotEquals(FALSE, Boolean.FALSE);
        assertNotEquals(NULL, Json.of("false"));
    }

}
