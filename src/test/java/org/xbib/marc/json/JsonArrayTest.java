package org.xbib.marc.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

public class JsonArrayTest {

    private JsonArray array;

    private static JsonArray array(String... values) {
        JsonArray array = new JsonArray();
        for (String value : values) {
            array.add(value);
        }
        return array;
    }

    @BeforeEach
    public void setUp() {
        array = new JsonArray();
    }

    @Test
    public void copyConstructorFailsWithNull() {
        Assertions.assertThrows(NullPointerException.class, () -> new JsonArray(null));
    }

    @Test
    public void copyConstructorHasSameValues() {
        array.add(23);
        JsonArray copy = new JsonArray(array);
        assertEquals(array.values(), copy.values());
    }

    @Test
    public void copyConstructorworksOnSafeCopy() {
        JsonArray copy = new JsonArray(array);
        array.add(23);
        assertTrue(copy.isEmpty());
    }

    @Test
    public void isEmptyisTrueAfterCreation() {
        assertTrue(array.isEmpty());
    }

    @Test
    public void isEmptyisFalseAfterAdd() {
        array.add(true);
        assertFalse(array.isEmpty());
    }

    @Test
    public void sizeisZeroAfterCreation() {
        assertEquals(0, array.size());
    }

    @Test
    public void sizeisOneAfterAdd() {
        array.add(true);
        assertEquals(1, array.size());
    }

    @Test
    public void iteratorisEmptyAfterCreation() {
        assertFalse(array.iterator().hasNext());
    }

    @Test
    public void iteratorhasNextAfterAdd() {
        array.add(true);
        Iterator<JsonValue> iterator = array.iterator();
        assertTrue(iterator.hasNext());
        assertEquals(JsonLiteral.TRUE, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void iteratordoesNotAllowModification() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            array.add(23);
            Iterator<JsonValue> iterator = array.iterator();
            iterator.next();
            iterator.remove();
        });
    }

    @Test
    public void iteratordetectsConcurrentModification() {
        Assertions.assertThrows(ConcurrentModificationException.class, () -> {
            Iterator<JsonValue> iterator = array.iterator();
            array.add(23);
            iterator.next();
        });
    }

    @Test
    public void valuesisEmptyAfterCreation() {
        assertTrue(array.values().isEmpty());
    }

    @Test
    public void valuescontainsValueAfterAdd() {
        array.add(true);
        assertEquals(1, array.values().size());
        assertEquals(JsonLiteral.TRUE, array.values().get(0));
    }

    @Test
    public void valuesReflectsChanges() {
        List<JsonValue> values = array.values();
        array.add(true);
        assertEquals(array.values(), values);
    }

    @Test
    public void valuesPreventsModification() {
        List<JsonValue> values = array.values();
        values.add(JsonLiteral.TRUE);
    }

    @Test
    public void getreturnsValue() {
        array.add(23);
        JsonValue value = array.get(0);
        assertEquals(Json.of(23), value);
    }

    @Test
    public void getfailsWithInvalidIndex() {
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> array.get(0));
    }

    @Test
    public void addint() {
        array.add(23);
        assertEquals("[23]", array.toString());
    }

    @Test
    public void addintenablesChaining() {
        assertSame(array, array.add(23));
    }

    @Test
    public void addlong() {
        array.add(23L);
        assertEquals("[23]", array.toString());
    }

    @Test
    public void addlongenablesChaining() {
        assertSame(array, array.add(23L));
    }

    @Test
    public void addfloat() {
        array.add(3.14f);
        assertEquals("[3.14]", array.toString());
    }

    @Test
    public void addfloatenablesChaining() {
        assertSame(array, array.add(3.14f));
    }

    @Test
    public void adddouble() {
        array.add(3.14d);
        assertEquals("[3.14]", array.toString());
    }

    @Test
    public void adddoubleenablesChaining() {
        assertSame(array, array.add(3.14d));
    }

    @Test
    public void addboolean() {
        array.add(true);
        assertEquals("[true]", array.toString());
    }

    @Test
    public void addbooleanenablesChaining() {
        assertSame(array, array.add(true));
    }

    @Test
    public void addstring() {
        array.add("foo");
        assertEquals("[\"foo\"]", array.toString());
    }

    @Test
    public void addstringenablesChaining() {
        assertSame(array, array.add("foo"));
    }

    @Test
    public void addstringtoleratesNull() {
        array.add((String) null);
        assertEquals("[null]", array.toString());
    }

    @Test
    public void addjsonNull() {
        array.add(JsonLiteral.NULL);
        assertEquals("[null]", array.toString());
    }

    @Test
    public void addjsonArray() {
        array.add(new JsonArray());
        assertEquals("[[]]", array.toString());
    }

    @Test
    public void addjsonObject() {
        array.add(new JsonObject());
        assertEquals("[{}]", array.toString());
    }

    @Test
    public void addjsonenablesChaining() {
        assertSame(array, array.add(JsonLiteral.NULL));
    }

    @Test
    public void addjsonfailsWithNull() {
        Assertions.assertThrows(NullPointerException.class, () -> array.add((JsonValue) null));
    }

    @Test
    public void addjsonnestedArray() {
        JsonArray innerArray = new JsonArray();
        innerArray.add(23);
        array.add(innerArray);
        assertEquals("[[23]]", array.toString());
    }

    @Test
    public void addjsonnestedArraymodifiedAfterAdd() {
        JsonArray innerArray = new JsonArray();
        array.add(innerArray);
        innerArray.add(23);
        assertEquals("[[23]]", array.toString());
    }

    @Test
    public void addjsonnestedObject() {
        JsonObject innerObject = new JsonObject();
        innerObject.add("a", 23);
        array.add(innerObject);
        assertEquals("[{\"a\":23}]", array.toString());
    }

    @Test
    public void addjsonnestedObjectmodifiedAfterAdd() {
        JsonObject innerObject = new JsonObject();
        array.add(innerObject);
        innerObject.add("a", 23);
        assertEquals("[{\"a\":23}]", array.toString());
    }

    @Test
    public void setint() {
        array.add(false);
        array.set(0, 23);
        assertEquals("[23]", array.toString());
    }

    @Test
    public void setintenablesChaining() {
        array.add(false);
        assertSame(array, array.set(0, 23));
    }

    @Test
    public void setlong() {
        array.add(false);
        array.set(0, 23L);
        assertEquals("[23]", array.toString());
    }

    @Test
    public void setlongenablesChaining() {
        array.add(false);
        assertSame(array, array.set(0, 23L));
    }

    @Test
    public void setfloat() {
        array.add(false);
        array.set(0, 3.14f);
        assertEquals("[3.14]", array.toString());
    }

    @Test
    public void setfloatenablesChaining() {
        array.add(false);
        assertSame(array, array.set(0, 3.14f));
    }

    @Test
    public void setdouble() {
        array.add(false);
        array.set(0, 3.14d);
        assertEquals("[3.14]", array.toString());
    }

    @Test
    public void setdoubleenablesChaining() {
        array.add(false);
        assertSame(array, array.set(0, 3.14d));
    }

    @Test
    public void setboolean() {
        array.add(false);
        array.set(0, true);
        assertEquals("[true]", array.toString());
    }

    @Test
    public void setbooleanenablesChaining() {
        array.add(false);
        assertSame(array, array.set(0, true));
    }

    @Test
    public void setstring() {
        array.add(false);
        array.set(0, "foo");
        assertEquals("[\"foo\"]", array.toString());
    }

    @Test
    public void setstringenablesChaining() {
        array.add(false);
        assertSame(array, array.set(0, "foo"));
    }

    @Test
    public void setjsonNull() {
        array.add(false);
        array.set(0, JsonLiteral.NULL);
        assertEquals("[null]", array.toString());
    }

    @Test
    public void setjsonArray() {
        array.add(false);
        array.set(0, new JsonArray());
        assertEquals("[[]]", array.toString());
    }

    @Test
    public void setjsonObject() {
        array.add(false);
        array.set(0, new JsonObject());
        assertEquals("[{}]", array.toString());
    }

    @Test
    public void setJsonFailsWithNull() {
        array.add(false);
        Assertions.assertThrows(NullPointerException.class, () ->
                array.set(0, (JsonValue) null));
    }

    @Test
    public void setjsonfailsWithInvalidIndex() {
        Assertions.assertThrows(IndexOutOfBoundsException.class, () ->
                array.set(0, JsonLiteral.NULL));
    }

    @Test
    public void setjsonenablesChaining() {
        array.add(false);
        assertSame(array, array.set(0, JsonLiteral.NULL));
    }

    @Test
    public void setjsonreplacesDifferntArrayElements() {
        array.add(3).add(6).add(9);
        array.set(1, 4).set(2, 5);
        assertEquals("[3,4,5]", array.toString());
    }

    @Test
    public void removefailsWithInvalidIndex() {
        Assertions.assertThrows(IndexOutOfBoundsException.class, () ->
            array.remove(0)
        );
    }

    @Test
    public void removeremovesElement() {
        array.add(23);
        array.remove(0);
        assertEquals("[]", array.toString());
    }

    @Test
    public void removekeepsOtherElements() {
        array.add("a").add("b").add("c");
        array.remove(1);
        assertEquals("[\"a\",\"c\"]", array.toString());
    }

    @Test
    public void writeempty() throws IOException {
        JsonWriter writer = mock(JsonWriter.class);
        array.write(writer);
        InOrder inOrder = inOrder(writer);
        inOrder.verify(writer).writeArrayOpen();
        inOrder.verify(writer).writeArrayClose();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void writewithSingleValue() throws IOException {
        JsonWriter writer = mock(JsonWriter.class);
        array.add(23);
        array.write(writer);
        InOrder inOrder = inOrder(writer);
        inOrder.verify(writer).writeArrayOpen();
        inOrder.verify(writer).writeNumber("23");
        inOrder.verify(writer).writeArrayClose();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void writewithMultipleValues() throws IOException {
        JsonWriter writer = mock(JsonWriter.class);
        array.add(23).add("foo").add(false);
        array.write(writer);
        InOrder inOrder = inOrder(writer);
        inOrder.verify(writer).writeArrayOpen();
        inOrder.verify(writer).writeNumber("23");
        inOrder.verify(writer).writeArraySeparator();
        inOrder.verify(writer).writeString("foo");
        inOrder.verify(writer).writeArraySeparator();
        inOrder.verify(writer).writeLiteral("false");
        inOrder.verify(writer).writeArrayClose();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void isArray() {
        assertTrue(array.isArray());
    }

    @Test
    public void asArray() {
        assertSame(array, array.asArray());
    }

    @Test
    public void equalstrueForEqualArrays() {
        assertEquals(array(), array());
        assertEquals(array("foo", "bar"), array("foo", "bar"));
    }

    @Test
    public void equalsfalseForDifferentArrays() {
        assertNotEquals(array("foo", "bar"), array("foo", "bar", "baz"));
        assertNotEquals(array("foo", "bar"), array("bar", "foo"));
    }

    @Test
    public void equalsfalseForNull() {
        assertNotEquals(null, array);
    }

    @Test
    public void equalsfalseForSubclass() {
        assertNotEquals(array, new JsonArray(array) {
        });
    }

    @Test
    public void hashCodeequalsForEqualArrays() {
        assertEquals(array().hashCode(), array().hashCode());
        assertEquals(array("foo").hashCode(), array("foo").hashCode());
    }

    @Test
    public void hashCodediffersForDifferentArrays() {
        assertNotEquals(array().hashCode(), array("bar").hashCode());
        assertNotEquals(array("foo").hashCode(), array("bar").hashCode());
    }
}
