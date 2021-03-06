package org.xbib.marc.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.xbib.marc.json.JsonLiteral.FALSE;
import static org.xbib.marc.json.JsonLiteral.NULL;
import static org.xbib.marc.json.JsonLiteral.TRUE;
import org.junit.jupiter.api.Test;
import java.io.IOException;

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
