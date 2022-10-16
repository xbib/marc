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
import java.io.Reader;
import java.io.StringReader;
import java.util.Objects;

/**
 * This class serves as the entry point to the JSON API.
 * <p>
 * To <strong>parse</strong> a given JSON input, use the <code>parse()</code> methods like in this
 * example:
 * </p>
 * <pre>
 * JsonObject object = Json.parse(string).asObject();
 * </pre>
 * <p>
 * To <strong>create</strong> a JSON data structure to be serialized, use the methods
 * <code>value()</code>, <code>array()</code>, and <code>object()</code>. For example, the following
 * snippet will produce the JSON string <em>{"foo": 23, "bar": true}</em>:
 * </p>
 * <pre>
 * String string = Json.object().add("foo", 23).add("bar", true).toString();
 * </pre>
 * <p>
 * To create a JSON array from a given Java array, you can use one of the <code>array()</code>
 * methods with varargs parameters:
 * </p>
 * <pre>
 * String[] names = ...
 * JsonArray array = Json.array(names);
 * </pre>
 */
public final class Json {

    private Json() {
        // not meant to be instantiated
    }

    /**
     * Returns a JsonValue instance that represents the given <code>int</code> value.
     *
     * @param value the value to get a JSON representation for
     * @return a JSON value that represents the given value
     */
    public static JsonValue of(int value) {
        return new JsonNumber(Integer.toString(value, 10));
    }

    /**
     * Returns a JsonValue instance that represents the given <code>long</code> value.
     *
     * @param value the value to get a JSON representation for
     * @return a JSON value that represents the given value
     */
    public static JsonValue of(long value) {
        return new JsonNumber(Long.toString(value, 10));
    }

    /**
     * Returns a JsonValue instance that represents the given <code>float</code> value.
     *
     * @param value the value to get a JSON representation for
     * @return a JSON value that represents the given value
     */
    public static JsonValue of(float value) {
        if (Float.isInfinite(value) || Float.isNaN(value)) {
            throw new IllegalArgumentException("Infinite and NaN values not permitted in JSON");
        }
        return new JsonNumber(cutOffPointZero(Float.toString(value)));
    }

    /**
     * Returns a JsonValue instance that represents the given <code>double</code> value.
     *
     * @param value the value to get a JSON representation for
     * @return a JSON value that represents the given value
     */
    public static JsonValue of(double value) {
        if (Double.isInfinite(value) || Double.isNaN(value)) {
            throw new IllegalArgumentException("Infinite and NaN values not permitted in JSON");
        }
        return new JsonNumber(cutOffPointZero(Double.toString(value)));
    }

    /**
     * Returns a JsonValue instance that represents the given string.
     *
     * @param string the string to get a JSON representation for
     * @return a JSON value that represents the given string
     */
    public static JsonValue of(String string) {
        return string == null ? JsonLiteral.NULL : new JsonString(string);
    }

    /**
     * Returns a JsonValue instance that represents the given <code>boolean</code> value.
     *
     * @param value the value to get a JSON representation for
     * @return a JSON value that represents the given value
     */
    public static JsonValue of(boolean value) {
        return value ? JsonLiteral.TRUE : JsonLiteral.FALSE;
    }

    /**
     * Creates a new empty JsonArray. This is equivalent to creating a new JsonArray using the
     * constructor.
     *
     * @return a new empty JSON array
     */
    public static JsonArray array() {
        return new JsonArray();
    }

    /**
     * Creates a new JsonArray that contains the JSON representations of the given <code>int</code>
     * values.
     *
     * @param values the values to be included in the new JSON array
     * @return a new JSON array that contains the given values
     */
    public static JsonArray array(int... values) {
        Objects.requireNonNull(values);
        JsonArray array = new JsonArray();
        for (int value : values) {
            array.add(value);
        }
        return array;
    }

    /**
     * Creates a new JsonArray that contains the JSON representations of the given <code>long</code>
     * values.
     *
     * @param values the values to be included in the new JSON array
     * @return a new JSON array that contains the given values
     */
    public static JsonArray array(long... values) {
        Objects.requireNonNull(values);
        JsonArray array = new JsonArray();
        for (long value : values) {
            array.add(value);
        }
        return array;
    }

    /**
     * Creates a new JsonArray that contains the JSON representations of the given <code>float</code>
     * values.
     *
     * @param values the values to be included in the new JSON array
     * @return a new JSON array that contains the given values
     */
    public static JsonArray array(float... values) {
        Objects.requireNonNull(values);
        JsonArray array = new JsonArray();
        for (float value : values) {
            array.add(value);
        }
        return array;
    }

    /**
     * Creates a new JsonArray that contains the JSON representations of the given <code>double</code>
     * values.
     *
     * @param values the values to be included in the new JSON array
     * @return a new JSON array that contains the given values
     */
    public static JsonArray array(double... values) {
        Objects.requireNonNull(values);
        JsonArray array = new JsonArray();
        for (double value : values) {
            array.add(value);
        }
        return array;
    }

    /**
     * Creates a new JsonArray that contains the JSON representations of the given
     * <code>boolean</code> values.
     *
     * @param values the values to be included in the new JSON array
     * @return a new JSON array that contains the given values
     */
    public static JsonArray array(boolean... values) {
        Objects.requireNonNull(values);
        JsonArray array = new JsonArray();
        for (boolean value : values) {
            array.add(value);
        }
        return array;
    }

    /**
     * Creates a new JsonArray that contains the JSON representations of the given strings.
     *
     * @param strings the strings to be included in the new JSON array
     * @return a new JSON array that contains the given strings
     */
    public static JsonArray array(String... strings) {
        Objects.requireNonNull(strings);
        JsonArray array = new JsonArray();
        for (String value : strings) {
            array.add(value);
        }
        return array;
    }

    /**
     * Creates a new empty JsonObject. This is equivalent to creating a new JsonObject using the
     * constructor.
     *
     * @return a new empty JSON object
     */
    public static JsonObject object() {
        return new JsonObject();
    }

    /**
     * Parses the given input string as JSON. The input must contain a valid JSON value, optionally
     * padded with whitespace.
     *
     * @param string the input string, must be valid JSON
     * @return a value that represents the parsed JSON
     * @throws IOException if the input is not valid JSON
     */
    public static JsonValue parse(String string) throws IOException {
        Objects.requireNonNull(string);
        JsonDefaultHandler handler = new JsonDefaultHandler();
        new JsonReader<>(new StringReader(string), handler).parse();
        return handler.getValue();
    }

    /**
     * Reads the entire input from the given reader and parses it as JSON. The input must contain a
     * valid JSON value, optionally padded with whitespace.
     * <p>
     * Characters are read in chunks into an input buffer. Hence, wrapping a reader in an additional
     * <code>BufferedReader</code> likely won't improve reading performance.
     * </p>
     *
     * @param reader the reader to read the JSON value from
     * @return a value that represents the parsed JSON
     * @throws IOException    if an I/O error occurs in the reader
     * @throws JsonException if the input is not valid JSON
     */
    public static JsonValue parse(Reader reader) throws IOException {
        JsonDefaultHandler handler = new JsonDefaultHandler();
        try (reader) {
            new JsonReader<>(reader, handler).parse();
        }
        return handler.getValue();
    }

    private static String cutOffPointZero(String string) {
        if (string.endsWith(".0")) {
            return string.substring(0, string.length() - 2);
        }
        return string;
    }

}
