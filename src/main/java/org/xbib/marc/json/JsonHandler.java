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

/**
 * An interface for parser events. A {@link JsonHandler} can be given to a {@link JsonReader}. The
 * parser will then call the methods of the given handler while reading the input.

 * <p>
 * Implementations that build an object representation of the parsed JSON can return arbitrary handler
 * objects for JSON arrays and JSON objects in {@link #startArray()} and {@link #startObject()}.
 * These handler objects will then be provided in all subsequent parser events for this particular
 * array or object. They can be used to keep track the elements of a JSON array or object.
 * </p>
 *
 * @param <A> The type of handlers used for JSON arrays
 * @param <O> The type of handlers used for JSON objects
 * @see JsonReader
 */
public interface JsonHandler<A, O> {

    /**
     * Indicates the beginning of a <code>null</code> literal in the JSON input. This method will be
     * called when reading the first character of the literal.
     */
    void startNull();

    /**
     * Indicates the end of a <code>null</code> literal in the JSON input. This method will be called
     * after reading the last character of the literal.
     */
    void endNull();

    /**
     * Indicates the beginning of a boolean literal (<code>true</code> or <code>false</code>) in the
     * JSON input. This method will be called when reading the first character of the literal.
     */
    void startBoolean();

    /**
     * Indicates the end of a boolean literal (<code>true</code> or <code>false</code>) in the JSON
     * input. This method will be called after reading the last character of the literal.
     *
     * @param value the parsed boolean value
     */
    void endBoolean(boolean value);

    /**
     * Indicates the beginning of a string in the JSON input. This method will be called when reading
     * the opening double quote character (<code>'&quot;'</code>).
     */
    void startString();

    /**
     * Indicates the end of a string in the JSON input. This method will be called after reading the
     * closing double quote character (<code>'&quot;'</code>).
     *
     * @param string the parsed string
     */
    void endString(String string);

    /**
     * Indicates the beginning of a number in the JSON input. This method will be called when reading
     * the first character of the number.
     */
    void startNumber();

    /**
     * Indicates the end of a number in the JSON input. This method will be called after reading the
     * last character of the number.
     *
     * @param string the parsed number string
     */
    void endNumber(String string);

    /**
     * Indicates the beginning of an array in the JSON input. This method will be called when reading
     * the opening square bracket character (<code>'['</code>).
     * <p>
     * This method may return an object to handle subsequent parser events for this array. This array
     * handler will then be provided in all calls to {@link #startArrayValue(Object)
     * startArrayValue()}, {@link #endArrayValue(Object) endArrayValue()}, and
     * {@link #endArray(Object) endArray()} for this array.
     * </p>
     *
     * @return a handler for this array, or <code>null</code> if not needed
     */
    A startArray();

    /**
     * Indicates the end of an array in the JSON input. This method will be called after reading the
     * closing square bracket character (<code>']'</code>).
     *
     * @param array the array handler returned from {@link #startArray()}, or <code>null</code> if not
     *              provided
     */
    void endArray(A array);

    /**
     * Indicates the beginning of an array element in the JSON input. This method will be called when
     * reading the first character of the element, just before the call to the <code>start</code>
     * method for the specific element type ({@link #startString()}, {@link #startNumber()}, etc.).
     *
     * @param array the array handler returned from {@link #startArray()}, or <code>null</code> if not
     *              provided
     */
    void startArrayValue(A array);

    /**
     * Indicates the end of an array element in the JSON input. This method will be called after
     * reading the last character of the element value, just after the <code>end</code> method for the
     * specific element type (like {@link #endString(String) endString()}, {@link #endNumber(String)
     * endNumber()}, etc.).
     *
     * @param array the array handler returned from {@link #startArray()}, or <code>null</code> if not
     *              provided
     */
    void endArrayValue(A array);

    /**
     * Indicates the beginning of an object in the JSON input. This method will be called when reading
     * the opening curly bracket character (<code>'{'</code>).
     * <p>
     * This method may return an object to handle subsequent parser events for this object. This
     * object handler will be provided in all calls to {@link #startObjectName(Object)
     * startObjectName()}, {@link #endObjectName(Object, String) endObjectName()},
     * {@link #startObjectValue(Object, String) startObjectValue()},
     * {@link #endObjectValue(Object, String) endObjectValue()}, and {@link #endObject(Object)
     * endObject()} for this object.
     * </p>
     *
     * @return a handler for this object, or <code>null</code> if not needed
     */
    O startObject();

    /**
     * Indicates the end of an object in the JSON input. This method will be called after reading the
     * closing curly bracket character (<code>'}'</code>).
     *
     * @param object the object handler returned from {@link #startObject()}, or null if not provided
     */
    void endObject(O object);

    /**
     * Indicates the beginning of the name of an object member in the JSON input. This method will be
     * called when reading the opening quote character ('&quot;') of the member name.
     *
     * @param object the object handler returned from {@link #startObject()}, or <code>null</code> if not
     *               provided
     */
    void startObjectName(O object);

    /**
     * Indicates the end of an object member name in the JSON input. This method will be called after
     * reading the closing quote character (<code>'"'</code>) of the member name.
     *
     * @param object the object handler returned from {@link #startObject()}, or null if not provided
     * @param name   the parsed member name
     */
    void endObjectName(O object, String name);

    /**
     * Indicates the beginning of the name of an object member in the JSON input. This method will be
     * called when reading the opening quote character ('&quot;') of the member name.
     *
     * @param object the object handler returned from {@link #startObject()}, or <code>null</code> if not
     *               provided
     * @param name   the member name
     */
    void startObjectValue(O object, String name);

    /**
     * Indicates the end of an object member value in the JSON input. This method will be called after
     * reading the last character of the member value, just after the <code>end</code> method for the
     * specific member type (like {@link #endString(String) endString()}, {@link #endNumber(String)
     * endNumber()}, etc.).
     *
     * @param object the object handler returned from {@link #startObject()}, or null if not provided
     * @param name   the parsed member name
     */
    void endObjectValue(O object, String name);

}
