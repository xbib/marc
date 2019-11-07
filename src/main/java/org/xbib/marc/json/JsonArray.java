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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Represents a JSON array, an ordered collection of JSON values.
 * <p>
 * Elements can be added using the <code>add(...)</code> methods which accept instances of
 * {@link JsonValue}, strings, primitive numbers, and boolean values. To replace an element of an
 * array, use the <code>set(int, ...)</code> methods.
 * </p>
 * <p>
 * Elements can be accessed by their index using {@link #get(int)}. This class also supports
 * iterating over the elements in document order using an {@link #iterator()} or an enhanced for
 * loop:
 * </p>
 * <pre>
 * for (JsonValue value : jsonArray) {
 *   ...
 * }
 * </pre>
 * <p>
 * An equivalent {@link List} can be obtained from the method {@link #values()}.
 * </p>
 * <p>
 * Note that this class is <strong>not thread-safe</strong>. If multiple threads access a
 * <code>JsonArray</code> instance concurrently, while at least one of these threads modifies the
 * contents of this array, access to the instance must be synchronized externally. Failure to do so
 * may lead to an inconsistent state.
 * </p>
 */
public class JsonArray extends JsonValue implements Iterable<JsonValue> {

    private final List<JsonValue> values;

    /**
     * Creates a new empty JsonArray.
     */
    public JsonArray() {
        values = new ArrayList<>();
    }

    /**
     * Creates a new JsonArray with the contents of the specified JSON array.
     *
     * @param array the JsonArray to get the initial contents from, must not be <code>null</code>
     */
    public JsonArray(JsonArray array) {
        Objects.requireNonNull(array);
        values = new ArrayList<>(array.values);
    }

    /**
     * Appends the JSON representation of the specified <code>int</code> value to the end of this
     * array.
     *
     * @param value the value to add to the array
     * @return the array itself, to enable method chaining
     */
    public JsonArray add(int value) {
        values.add(Json.of(value));
        return this;
    }

    /**
     * Appends the JSON representation of the specified <code>long</code> value to the end of this
     * array.
     *
     * @param value the value to add to the array
     * @return the array itself, to enable method chaining
     */
    public JsonArray add(long value) {
        values.add(Json.of(value));
        return this;
    }

    /**
     * Appends the JSON representation of the specified <code>float</code> value to the end of this
     * array.
     *
     * @param value the value to add to the array
     * @return the array itself, to enable method chaining
     */
    public JsonArray add(float value) {
        values.add(Json.of(value));
        return this;
    }

    /**
     * Appends the JSON representation of the specified <code>double</code> value to the end of this
     * array.
     *
     * @param value the value to add to the array
     * @return the array itself, to enable method chaining
     */
    public JsonArray add(double value) {
        values.add(Json.of(value));
        return this;
    }

    /**
     * Appends the JSON representation of the specified <code>boolean</code> value to the end of this
     * array.
     *
     * @param value the value to add to the array
     * @return the array itself, to enable method chaining
     */
    public JsonArray add(boolean value) {
        values.add(Json.of(value));
        return this;
    }

    /**
     * Appends the JSON representation of the specified string to the end of this array.
     *
     * @param value the string to add to the array
     * @return the array itself, to enable method chaining
     */
    public JsonArray add(String value) {
        values.add(Json.of(value));
        return this;
    }

    /**
     * Appends the specified JSON value to the end of this array.
     *
     * @param value the JsonValue to add to the array, must not be <code>null</code>
     * @return the array itself, to enable method chaining
     */
    public JsonArray add(JsonValue value) {
        Objects.requireNonNull(value);
        values.add(value);
        return this;
    }

    /**
     * Replaces the element at the specified position in this array with the JSON representation of
     * the specified <code>int</code> value.
     *
     * @param index the index of the array element to replace
     * @param value the value to be stored at the specified array position
     * @return the array itself, to enable method chaining
     * @throws IndexOutOfBoundsException if the index is out of range, i.e. <code>index &lt; 0</code> or
     *                                   <code>index &gt;= size</code>
     */
    public JsonArray set(int index, int value) {
        values.set(index, Json.of(value));
        return this;
    }

    /**
     * Replaces the element at the specified position in this array with the JSON representation of
     * the specified <code>long</code> value.
     *
     * @param index the index of the array element to replace
     * @param value the value to be stored at the specified array position
     * @return the array itself, to enable method chaining
     * @throws IndexOutOfBoundsException if the index is out of range, i.e. <code>index &lt; 0</code> or
     *                                   <code>index &gt;= size</code>
     */
    public JsonArray set(int index, long value) {
        values.set(index, Json.of(value));
        return this;
    }

    /**
     * Replaces the element at the specified position in this array with the JSON representation of
     * the specified <code>float</code> value.
     *
     * @param index the index of the array element to replace
     * @param value the value to be stored at the specified array position
     * @return the array itself, to enable method chaining
     * @throws IndexOutOfBoundsException if the index is out of range, i.e. <code>index &lt; 0</code> or
     *                                   <code>index &gt;= size</code>
     */
    public JsonArray set(int index, float value) {
        values.set(index, Json.of(value));
        return this;
    }

    /**
     * Replaces the element at the specified position in this array with the JSON representation of
     * the specified <code>double</code> value.
     *
     * @param index the index of the array element to replace
     * @param value the value to be stored at the specified array position
     * @return the array itself, to enable method chaining
     * @throws IndexOutOfBoundsException if the index is out of range, i.e. <code>index &lt; 0</code> or
     *                                   <code>index &gt;= size</code>
     */
    public JsonArray set(int index, double value) {
        values.set(index, Json.of(value));
        return this;
    }

    /**
     * Replaces the element at the specified position in this array with the JSON representation of
     * the specified <code>boolean</code> value.
     *
     * @param index the index of the array element to replace
     * @param value the value to be stored at the specified array position
     * @return the array itself, to enable method chaining
     * @throws IndexOutOfBoundsException if the index is out of range, i.e. <code>index &lt; 0</code> or
     *                                   <code>index &gt;= size</code>
     */
    public JsonArray set(int index, boolean value) {
        values.set(index, Json.of(value));
        return this;
    }

    /**
     * Replaces the element at the specified position in this array with the JSON representation of
     * the specified string.
     *
     * @param index the index of the array element to replace
     * @param value the string to be stored at the specified array position
     * @return the array itself, to enable method chaining
     * @throws IndexOutOfBoundsException if the index is out of range, i.e. <code>index &lt; 0</code> or
     *                                   <code>index &gt;= size</code>
     */
    public JsonArray set(int index, String value) {
        values.set(index, Json.of(value));
        return this;
    }

    /**
     * Replaces the element at the specified position in this array with the specified JSON value.
     *
     * @param index the index of the array element to replace
     * @param value the value to be stored at the specified array position, must not be <code>null</code>
     * @return the array itself, to enable method chaining
     * @throws IndexOutOfBoundsException if the index is out of range, i.e. <code>index &lt; 0</code> or
     *                                   <code>index &gt;= size</code>
     */
    public JsonArray set(int index, JsonValue value) {
        if (value == null) {
            throw new NullPointerException();
        }
        values.set(index, value);
        return this;
    }

    /**
     * Removes the element at the specified index from this array.
     *
     * @param index the index of the element to remove
     * @return the array itself, to enable method chaining
     * @throws IndexOutOfBoundsException if the index is out of range, i.e. <code>index &lt; 0</code> or
     *                                   <code>index &gt;= size</code>
     */
    public JsonArray remove(int index) {
        values.remove(index);
        return this;
    }

    /**
     * Returns the number of elements in this array.
     *
     * @return the number of elements in this array
     */
    public int size() {
        return values.size();
    }

    /**
     * Returns <code>true</code> if this array contains no elements.
     *
     * @return <code>true</code> if this array contains no elements
     */
    public boolean isEmpty() {
        return values.isEmpty();
    }

    /**
     * Returns the value of the element at the specified position in this array.
     *
     * @param index the index of the array element to return
     * @return the value of the element at the specified position
     * @throws IndexOutOfBoundsException if the index is out of range, i.e. <code>index &lt; 0</code> or
     *                                   <code>index &gt;= size</code>
     */
    public JsonValue get(int index) {
        return values.get(index);
    }

    /**
     * Returns a list of the values in this array in document order.
     *
     * @return a list of the values in this array
     */
    public List<JsonValue> values() {
        return values;
    }

    /**
     * Returns an iterator over the values of this array in document order. The returned iterator
     * cannot be used to modify this array.
     *
     * @return an iterator over the values of this array
     */
    @Override
    public Iterator<JsonValue> iterator() {
        final Iterator<JsonValue> iterator = values.iterator();
        return new Iterator<>() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public JsonValue next() {
                return iterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    void write(JsonWriter writer) throws IOException {
        writer.writeArrayOpen();
        Iterator<JsonValue> iterator = iterator();
        if (iterator.hasNext()) {
            iterator.next().write(writer);
            while (iterator.hasNext()) {
                writer.writeArraySeparator();
                iterator.next().write(writer);
            }
        }
        writer.writeArrayClose();
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public JsonArray asArray() {
        return this;
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    /**
     * Indicates whether a given object is "equal to" this JsonArray. An object is considered equal
     * if it is also a <code>JsonArray</code> and both arrays contain the same list of values.
     * <p>
     * If two JsonArrays are equal, they will also produce the same JSON output.
     * </p>
     *
     * @param object the object to be compared with this JsonArray
     * @return <tt>true</tt> if the specified object is equal to this JsonArray, <code>false</code>
     * otherwise
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        JsonArray other = (JsonArray) object;
        return values.equals(other.values);
    }

}
