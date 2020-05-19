package org.xbib.marc.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import java.util.NoSuchElementException;

public class JsonObjectTest {

    private JsonObject object;

    private static JsonObject object(String... namesAndValues) {
        JsonObject object = new JsonObject();
        for (int i = 0; i < namesAndValues.length; i += 2) {
            object.add(namesAndValues[i], namesAndValues[i + 1]);
        }
        return object;
    }

    @BeforeEach
    public void setUp() {
        object = new JsonObject();
    }

    @Test
    public void copyConstructorfailsWithNull() {
        Assertions.assertThrows(NullPointerException.class, () -> new JsonObject(null));
    }

    @Test
    public void copyConstructorhasSameValues() {
        object.add("foo", 23);
        JsonObject copy = new JsonObject(object);
        assertEquals(object.names(), copy.names());
        assertSame(object.get("foo"), copy.get("foo"));
    }

    @Test
    public void copyConstructorworksOnSafeCopy() {
        JsonObject copy = new JsonObject(object);
        object.add("foo", 23);
        assertTrue(copy.isEmpty());
    }

    @Test
    public void isEmptytrueAfterCreation() {
        assertTrue(object.isEmpty());
    }

    @Test
    public void isEmptyfalseAfterAdd() {
        object.add("a", true);
        assertFalse(object.isEmpty());
    }

    @Test
    public void sizezeroAfterCreation() {
        assertEquals(0, object.size());
    }

    @Test
    public void sizeoneAfterAdd() {
        object.add("a", true);
        assertEquals(1, object.size());
    }

    @Test
    public void keyRepetitionallowsMultipleEntries() {
        object.add("a", true);
        object.add("a", "value");
        assertEquals(2, object.size());
    }

    @Test
    public void keyRepetitiongetsLastEntry() {
        object.add("a", true);
        object.add("a", "value");
        assertEquals("value", object.getString("a", "missing"));
    }

    @Test
    public void keyRepetitionequalityConsidersRepetitions() {
        object.add("a", true);
        object.add("a", "value");
        JsonObject onlyFirstProperty = new JsonObject();
        onlyFirstProperty.add("a", true);
        assertNotEquals(onlyFirstProperty, object);
        JsonObject bothProperties = new JsonObject();
        bothProperties.add("a", true);
        bothProperties.add("a", "value");
        assertEquals(bothProperties, object);
    }

    @Test
    public void namesemptyAfterCreation() {
        assertTrue(object.names().isEmpty());
    }

    @Test
    public void namescontainsNameAfterAdd() {
        object.add("foo", true);
        List<String> names = object.names();
        assertEquals(1, names.size());
        assertEquals("foo", names.get(0));
    }

    @Test
    public void namesreflectsChanges() {
        List<String> names = object.names();
        object.add("foo", true);
        assertEquals(1, names.size());
        assertEquals("foo", names.get(0));
    }

    @Test
    public void namespreventsModification() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            List<String> names = object.names();
            names.add("foo");
        });
    }

    @Test
    public void iteratorisEmptyAfterCreation() {
        assertFalse(object.iterator().hasNext());
    }

    @Test
    public void iteratorhasNextAfterAdd() {
        object.add("a", true);
        Iterator<JsonObject.Member> iterator = object.iterator();
        assertTrue(iterator.hasNext());
    }

    @Test
    public void iteratornextReturnsActualValue() {
        object.add("a", true);
        Iterator<JsonObject.Member> iterator = object.iterator();
        assertEquals(new JsonObject.Member("a", JsonLiteral.TRUE), iterator.next());
    }

    @Test
    public void iteratornextProgressesToNextValue() {
        object.add("a", true);
        object.add("b", false);
        Iterator<JsonObject.Member> iterator = object.iterator();
        iterator.next();
        assertTrue(iterator.hasNext());
        assertEquals(new JsonObject.Member("b", JsonLiteral.FALSE), iterator.next());
    }

    @Test
    public void iteratornextFailsAtEnd() {
        Assertions.assertThrows(NoSuchElementException.class, () -> {
            Iterator<JsonObject.Member> iterator = object.iterator();
            iterator.next();

        });
    }

    @Test
    public void iteratordoesNotAllowModification() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            object.add("a", 23);
            Iterator<JsonObject.Member> iterator = object.iterator();
            iterator.next();
            iterator.remove();
        });
    }

    @Test
    public void iteratordetectsConcurrentModification() {
        Assertions.assertThrows(ConcurrentModificationException.class, () -> {
            Iterator<JsonObject.Member> iterator = object.iterator();
            object.add("a", 23);
            iterator.next();
        });
    }

    @Test
    public void getfailsWithNullName() {
        Assertions.assertThrows(NullPointerException.class, () -> object.get(null));
    }

    @Test
    public void getreturnsNullForNonExistingMember() {
        assertNull(object.get("foo"));
    }

    @Test
    public void getreturnsValueForName() {
        object.add("foo", true);
        assertEquals(JsonLiteral.TRUE, object.get("foo"));
    }

    @Test
    public void getreturnsLastValueForName() {
        object.add("foo", false).add("foo", true);
        assertEquals(JsonLiteral.TRUE, object.get("foo"));
    }

    @Test
    public void getintreturnsValueFromMember() {
        object.add("foo", 23);

        assertEquals(23, object.getInt("foo", 42));
    }

    @Test
    public void getintreturnsDefaultForMissingMember() {
        assertEquals(23, object.getInt("foo", 23));
    }

    @Test
    public void getlongreturnsValueFromMember() {
        object.add("foo", 23L);

        assertEquals(23L, object.getLong("foo", 42L));
    }

    @Test
    public void getlongreturnsDefaultForMissingMember() {
        assertEquals(23L, object.getLong("foo", 23L));
    }

    @Test
    public void getfloatreturnsValueFromMember() {
        object.add("foo", 3.14f);

        assertEquals(3.14f, object.getFloat("foo", 1.41f), 0);
    }

    @Test
    public void getfloatreturnsDefaultForMissingMember() {
        assertEquals(3.14f, object.getFloat("foo", 3.14f), 0);
    }

    @Test
    public void getdoublereturnsValueFromMember() {
        object.add("foo", 3.14);

        assertEquals(3.14, object.getDouble("foo", 1.41), 0);
    }

    @Test
    public void getdoublereturnsDefaultForMissingMember() {
        assertEquals(3.14, object.getDouble("foo", 3.14), 0);
    }

    @Test
    public void getbooleanreturnsValueFromMember() {
        object.add("foo", true);

        assertTrue(object.getBoolean("foo", false));
    }

    @Test
    public void getbooleanreturnsDefaultForMissingMember() {
        assertFalse(object.getBoolean("foo", false));
    }

    @Test
    public void getstringreturnsValueFromMember() {
        object.add("foo", "bar");

        assertEquals("bar", object.getString("foo", "default"));
    }

    @Test
    public void getstringreturnsDefaultForMissingMember() {
        assertEquals("default", object.getString("foo", "default"));
    }

    @Test
    public void addfailsWithNullName() {
        Assertions.assertThrows(NullPointerException.class, () ->  object.add(null, 23));
    }

    @Test
    public void addint() {
        object.add("a", 23);
        assertEquals("{\"a\":23}", object.toString());
    }

    @Test
    public void addintenablesChaining() {
        assertSame(object, object.add("a", 23));
    }

    @Test
    public void addlong() {
        object.add("a", 23L);
        assertEquals("{\"a\":23}", object.toString());
    }

    @Test
    public void addlongenablesChaining() {
        assertSame(object, object.add("a", 23L));
    }

    @Test
    public void addfloat() {
        object.add("a", 3.14f);
        assertEquals("{\"a\":3.14}", object.toString());
    }

    @Test
    public void addfloatenablesChaining() {
        assertSame(object, object.add("a", 3.14f));
    }

    @Test
    public void adddouble() {
        object.add("a", 3.14d);
        assertEquals("{\"a\":3.14}", object.toString());
    }

    @Test
    public void adddoubleenablesChaining() {
        assertSame(object, object.add("a", 3.14d));
    }

    @Test
    public void addboolean() {
        object.add("a", true);
        assertEquals("{\"a\":true}", object.toString());
    }

    @Test
    public void addbooleanenablesChaining() {
        assertSame(object, object.add("a", true));
    }

    @Test
    public void addstring() {
        object.add("a", "foo");
        assertEquals("{\"a\":\"foo\"}", object.toString());
    }

    @Test
    public void addstringtoleratesNull() {
        object.add("a", (String) null);
        assertEquals("{\"a\":null}", object.toString());
    }

    @Test
    public void addstringenablesChaining() {
        assertSame(object, object.add("a", "foo"));
    }

    @Test
    public void addjsonNull() {
        object.add("a", JsonLiteral.NULL);
        assertEquals("{\"a\":null}", object.toString());
    }

    @Test
    public void addjsonArray() {
        object.add("a", new JsonArray());
        assertEquals("{\"a\":[]}", object.toString());
    }

    @Test
    public void addjsonObject() {
        object.add("a", new JsonObject());
        assertEquals("{\"a\":{}}", object.toString());
    }

    @Test
    public void addjsonenablesChaining() {
        assertSame(object, object.add("a", JsonLiteral.NULL));
    }

    @Test
    public void addjsonfailsWithNull() {
        Assertions.assertThrows(NullPointerException.class, () ->
                object.add("a", (JsonValue) null));
    }

    @Test
    public void addjsonnestedArray() {
        JsonArray innerArray = new JsonArray();
        innerArray.add(23);
        object.add("a", innerArray);
        assertEquals("{\"a\":[23]}", object.toString());
    }

    @Test
    public void addjsonnestedArraymodifiedAfterAdd() {
        JsonArray innerArray = new JsonArray();
        object.add("a", innerArray);
        innerArray.add(23);
        assertEquals("{\"a\":[23]}", object.toString());
    }

    @Test
    public void addjsonnestedObject() {
        JsonObject innerObject = new JsonObject();
        innerObject.add("a", 23);
        object.add("a", innerObject);
        assertEquals("{\"a\":{\"a\":23}}", object.toString());
    }

    @Test
    public void addjsonnestedObjectmodifiedAfterAdd() {
        JsonObject innerObject = new JsonObject();
        object.add("a", innerObject);
        innerObject.add("a", 23);
        assertEquals("{\"a\":{\"a\":23}}", object.toString());
    }

    @Test
    public void setint() {
        object.set("a", 23);
        assertEquals("{\"a\":23}", object.toString());
    }

    @Test
    public void setintenablesChaining() {
        assertSame(object, object.set("a", 23));
    }

    @Test
    public void setlong() {
        object.set("a", 23L);
        assertEquals("{\"a\":23}", object.toString());
    }

    @Test
    public void setlongenablesChaining() {
        assertSame(object, object.set("a", 23L));
    }

    @Test
    public void setfloat() {
        object.set("a", 3.14f);

        assertEquals("{\"a\":3.14}", object.toString());
    }

    @Test
    public void setfloatenablesChaining() {
        assertSame(object, object.set("a", 3.14f));
    }

    @Test
    public void setdouble() {
        object.set("a", 3.14d);

        assertEquals("{\"a\":3.14}", object.toString());
    }

    @Test
    public void setdoubleenablesChaining() {
        assertSame(object, object.set("a", 3.14d));
    }

    @Test
    public void setboolean() {
        object.set("a", true);

        assertEquals("{\"a\":true}", object.toString());
    }

    @Test
    public void setbooleanenablesChaining() {
        assertSame(object, object.set("a", true));
    }

    @Test
    public void setstring() {
        object.set("a", "foo");

        assertEquals("{\"a\":\"foo\"}", object.toString());
    }

    @Test
    public void setstringenablesChaining() {
        assertSame(object, object.set("a", "foo"));
    }

    @Test
    public void setjsonNull() {
        object.set("a", JsonLiteral.NULL);

        assertEquals("{\"a\":null}", object.toString());
    }

    @Test
    public void setjsonArray() {
        object.set("a", new JsonArray());

        assertEquals("{\"a\":[]}", object.toString());
    }

    @Test
    public void setjsonObject() {
        object.set("a", new JsonObject());

        assertEquals("{\"a\":{}}", object.toString());
    }

    @Test
    public void setjsonenablesChaining() {
        assertSame(object, object.set("a", JsonLiteral.NULL));
    }

    @Test
    public void setaddsElementIfMissing() {
        object.set("a", JsonLiteral.TRUE);

        assertEquals("{\"a\":true}", object.toString());
    }

    @Test
    public void setmodifiesElementIfExisting() {
        object.add("a", JsonLiteral.TRUE);

        object.set("a", JsonLiteral.FALSE);

        assertEquals("{\"a\":false}", object.toString());
    }

    @Test
    public void setmodifiesLastElementIfMultipleExisting() {
        object.add("a", 1);
        object.add("a", 2);

        object.set("a", JsonLiteral.TRUE);

        assertEquals("{\"a\":1,\"a\":true}", object.toString());
    }

    @Test
    public void removefailsWithNullName() {
        Assertions.assertThrows(NullPointerException.class, () -> object.remove(null));
    }

    @Test
    public void removeremovesMatchingMember() {
        object.add("a", 23);

        object.remove("a");

        assertEquals("{}", object.toString());
    }

    @Test
    public void removeremovesOnlyMatchingMember() {
        object.add("a", 23);
        object.add("b", 42);
        object.add("c", true);

        object.remove("b");

        assertEquals("{\"a\":23,\"c\":true}", object.toString());
    }

    @Test
    public void removeremovesOnlyLastMatchingMember() {
        object.add("a", 23);
        object.add("a", 42);

        object.remove("a");

        assertEquals("{\"a\":23}", object.toString());
    }

    @Test
    public void removeremovesOnlyLastMatchingMemberafterRemove() {
        object.add("a", 23);
        object.remove("a");
        object.add("a", 42);
        object.add("a", 47);

        object.remove("a");

        assertEquals("{\"a\":42}", object.toString());
    }

    @Test
    public void removedoesNotModifyObjectWithoutMatchingMember() {
        object.add("a", 23);

        object.remove("b");

        assertEquals("{\"a\":23}", object.toString());
    }

    @Test
    public void mergefailsWithNull() {
        Assertions.assertThrows(NullPointerException.class, () -> object.merge(null));
    }

    @Test
    public void mergeappendsMembers() {
        object.add("a", 1).add("b", 1);
        object.merge(Json.object().add("c", 2).add("d", 2));

        assertEquals(Json.object().add("a", 1).add("b", 1).add("c", 2).add("d", 2), object);
    }

    @Test
    public void mergereplacesMembers() {
        object.add("a", 1).add("b", 1).add("c", 1);
        object.merge(Json.object().add("b", 2).add("d", 2));

        assertEquals(Json.object().add("a", 1).add("b", 2).add("c", 1).add("d", 2), object);
    }

    @Test
    public void writeempty() throws IOException {
        JsonWriter writer = mock(JsonWriter.class);
        object.write(writer);

        InOrder inOrder = inOrder(writer);
        inOrder.verify(writer).writeObjectOpen();
        inOrder.verify(writer).writeObjectClose();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void writewithSingleValue() throws IOException {
        JsonWriter writer = mock(JsonWriter.class);
        object.add("a", 23);

        object.write(writer);

        InOrder inOrder = inOrder(writer);
        inOrder.verify(writer).writeObjectOpen();
        inOrder.verify(writer).writeMemberName("a");
        inOrder.verify(writer).writeMemberSeparator();
        inOrder.verify(writer).writeNumber("23");
        inOrder.verify(writer).writeObjectClose();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void writewithMultipleValues() throws IOException {
        JsonWriter writer = mock(JsonWriter.class);
        object.add("a", 23);
        object.add("b", 3.14f);
        object.add("c", "foo");
        object.add("d", true);
        object.add("e", (String) null);

        object.write(writer);

        InOrder inOrder = inOrder(writer);
        inOrder.verify(writer).writeObjectOpen();
        inOrder.verify(writer).writeMemberName("a");
        inOrder.verify(writer).writeMemberSeparator();
        inOrder.verify(writer).writeNumber("23");
        inOrder.verify(writer).writeObjectSeparator();
        inOrder.verify(writer).writeMemberName("b");
        inOrder.verify(writer).writeMemberSeparator();
        inOrder.verify(writer).writeNumber("3.14");
        inOrder.verify(writer).writeObjectSeparator();
        inOrder.verify(writer).writeMemberName("c");
        inOrder.verify(writer).writeMemberSeparator();
        inOrder.verify(writer).writeString("foo");
        inOrder.verify(writer).writeObjectSeparator();
        inOrder.verify(writer).writeMemberName("d");
        inOrder.verify(writer).writeMemberSeparator();
        inOrder.verify(writer).writeLiteral("true");
        inOrder.verify(writer).writeObjectSeparator();
        inOrder.verify(writer).writeMemberName("e");
        inOrder.verify(writer).writeMemberSeparator();
        inOrder.verify(writer).writeLiteral("null");
        inOrder.verify(writer).writeObjectClose();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void isObject() {
        assertTrue(object.isObject());
    }

    @Test
    public void asObject() {
        assertSame(object, object.asObject());
    }

    @Test
    public void equalstrueForSameInstance() {
        assertEquals(object, object);
    }

    @Test
    public void equalstrueForEqualObjects() {
        assertEquals(object(), object());
        assertEquals(object("a", "1", "b", "2"), object("a", "1", "b", "2"));
    }

    @Test
    public void equalsfalseForDifferentObjects() {
        assertNotEquals(object("a", "1"), object("a", "2"));
        assertNotEquals(object("a", "1"), object("b", "1"));
        assertNotEquals(object("a", "1", "b", "2"), object("b", "2", "a", "1"));
    }

    @Test
    public void equalsfalseForNull() {
        assertNotEquals(null, new JsonObject());
    }

    @Test
    public void equalsfalseForSubclass() {
        JsonObject jsonObject = new JsonObject();

        assertNotEquals(jsonObject, new JsonObject(jsonObject) {
        });
    }

    @Test
    public void hashCodeequalsForEqualObjects() {
        assertEquals(object().hashCode(), object().hashCode());
        assertEquals(object("a", "1").hashCode(), object("a", "1").hashCode());
    }

    @Test
    public void hashCodediffersForDifferentObjects() {
        assertNotEquals(object().hashCode(), object("a", "1").hashCode());
        assertNotEquals(object("a", "1").hashCode(), object("a", "2").hashCode());
        assertNotEquals(object("a", "1").hashCode(), object("b", "1").hashCode());
    }

    @Test
    public void indexOfreturnsNoIndexIfEmpty() {
        assertEquals(-1, object.indexOf("a"));
    }

    @Test
    public void indexOfreturnsIndexOfMember() {
        object.add("a", true);

        assertEquals(0, object.indexOf("a"));
    }

    @Test
    public void indexOfreturnsIndexOfLastMember() {
        object.add("a", true);
        object.add("a", true);

        assertEquals(1, object.indexOf("a"));
    }

    @Test
    public void indexOfreturnsIndexOfLastMemberafterRemove() {
        object.add("a", true);
        object.add("a", true);
        object.remove("a");

        assertEquals(0, object.indexOf("a"));
    }

    @Test
    public void indexOfreturnsUpdatedIndexAfterRemove() {
        // See issue #16
        object.add("a", true);
        object.add("b", true);
        object.remove("a");

        assertEquals(0, object.indexOf("b"));
    }

    @Test
    public void indexOfreturnsIndexOfLastMemberforBigObject() {
        object.add("a", true);
        // for indexes above 255, the hash index table does not return a value
        for (int i = 0; i < 256; i++) {
            object.add("x-" + i, 0);
        }
        object.add("a", true);

        assertEquals(257, object.indexOf("a"));
    }

    @Test
    public void hashIndexTablecopyConstructor() {
        JsonObject.HashIndexTable original = new JsonObject.HashIndexTable();
        original.add("name", 23);
        JsonObject.HashIndexTable copy = new JsonObject.HashIndexTable(original);
        assertEquals(23, copy.get("name"));
    }

    @Test
    public void hashIndexTableadd() {
        JsonObject.HashIndexTable indexTable = new JsonObject.HashIndexTable();

        indexTable.add("name-0", 0);
        indexTable.add("name-1", 1);
        indexTable.add("name-fe", 0xfe);
        indexTable.add("name-ff", 0xff);

        assertEquals(0, indexTable.get("name-0"));
        assertEquals(1, indexTable.get("name-1"));
        assertEquals(0xfe, indexTable.get("name-fe"));
        assertEquals(-1, indexTable.get("name-ff"));
    }

    @Test
    public void hashIndexTableaddoverwritesPreviousValue() {
        JsonObject.HashIndexTable indexTable = new JsonObject.HashIndexTable();
        indexTable.add("name", 23);
        indexTable.add("name", 42);
        assertEquals(42, indexTable.get("name"));
    }

    @Test
    public void hashIndexTableaddclearsPreviousValueIfIndexExceeds0xff() {
        JsonObject.HashIndexTable indexTable = new JsonObject.HashIndexTable();
        indexTable.add("name", 23);
        indexTable.add("name", 300);
        assertEquals(-1, indexTable.get("name"));
    }

    @Test
    public void hashIndexTableremove() {
        JsonObject.HashIndexTable indexTable = new JsonObject.HashIndexTable();
        indexTable.add("name", 23);
        indexTable.remove(23);
        assertEquals(-1, indexTable.get("name"));
    }

    @Test
    public void hashIndexTableremoveupdatesSubsequentElements() {
        JsonObject.HashIndexTable indexTable = new JsonObject.HashIndexTable();
        indexTable.add("foo", 23);
        indexTable.add("bar", 42);
        indexTable.remove(23);
        assertEquals(41, indexTable.get("bar"));
    }

    @Test
    public void hashIndexTableremovedoesNotChangePrecedingElements() {
        JsonObject.HashIndexTable indexTable = new JsonObject.HashIndexTable();
        indexTable.add("foo", 23);
        indexTable.add("bar", 42);
        indexTable.remove(42);
        assertEquals(23, indexTable.get("foo"));
    }

    @Test
    public void memberreturnsNameAndValue() {
        JsonObject.Member member = new JsonObject.Member("a", JsonLiteral.TRUE);
        assertEquals("a", member.getName());
        assertEquals(JsonLiteral.TRUE, member.getValue());
    }

    @Test
    public void memberequalstrueForSameInstance() {
        JsonObject.Member member = new JsonObject.Member("a", JsonLiteral.TRUE);
        assertEquals(member, member);
    }

    @Test
    public void memberequalstrueForEqualObjects() {
        JsonObject.Member member = new JsonObject.Member("a", JsonLiteral.TRUE);
        assertEquals(member, new JsonObject.Member("a", JsonLiteral.TRUE));
    }

    @Test
    public void memberequalsfalseForDifferingObjects() {
        JsonObject.Member member = new JsonObject.Member("a", JsonLiteral.TRUE);
        assertNotEquals(member, new JsonObject.Member("b", JsonLiteral.TRUE));
        assertNotEquals(member, new JsonObject.Member("a", JsonLiteral.FALSE));
    }

    @Test
    public void memberequalsfalseForNull() {
        JsonObject.Member member = new JsonObject.Member("a", JsonLiteral.TRUE);
        assertNotNull(member);
    }

    @Test
    public void memberequalsfalseForSubclass() {
        JsonObject.Member member = new JsonObject.Member("a", JsonLiteral.TRUE);
        assertNotEquals(member, new JsonObject.Member("a", JsonLiteral.TRUE) {
        });
    }

    @Test
    public void memberhashCodeequalsForEqualObjects() {
        JsonObject.Member member = new JsonObject.Member("a", JsonLiteral.TRUE);
        assertEquals(member.hashCode(), new JsonObject.Member("a", JsonLiteral.TRUE).hashCode());
    }

    @Test
    public void memberhashCodediffersForDifferingobjects() {
        JsonObject.Member member = new JsonObject.Member("a", JsonLiteral.TRUE);
        assertNotEquals(member.hashCode(), new JsonObject.Member("b", JsonLiteral.TRUE).hashCode());
        assertNotEquals(member.hashCode(), new JsonObject.Member("a", JsonLiteral.FALSE).hashCode());
    }

}
