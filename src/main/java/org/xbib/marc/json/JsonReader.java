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
import java.io.Reader;
import java.util.Objects;

/**
 * A streaming parser for JSON text. The parser reports all events to a given handler.
 *
 * @param <A> the JSON array type
 * @param <O> the JSON object type
 */
public class JsonReader<A, O> {

    private static final int MAX_NESTING_LEVEL = 1000;

    private static final int DEFAULT_BUFFER_SIZE = 1024;

    private final Reader reader;

    private final JsonHandler<A, O> handler;

    private char[] buffer;

    private int index;

    private int fill;

    private int current;

    private StringBuilder captureBuffer;

    private int captureStart;

    private int nestingLevel;

    /**
     * Creates a new JsonParser with the given handler. The parser will report all parser events to
     * this handler.
     * @param reader the reader
     * @param handler the handler to process parser events
     */
    public JsonReader(Reader reader, JsonHandler<A, O> handler) {
        Objects.requireNonNull(handler);
        this.handler = handler;
        this.reader = reader;
    }

    /**
     * Reads the entire input from the given reader and parses it as JSON. The input must contain a
     * valid JSON value, optionally padded with whitespace.
     * <p>
     * Characters are read in chunks into a default-sized input buffer. Hence, wrapping a reader in an
     * additional <code>BufferedReader</code> likely won't improve reading performance.
     * </p>
     *
     * @throws IOException    if an I/O error occurs in the reader
     * @throws JsonException if the input is not valid JSON
     */
    public void parse() throws IOException {
        parse(DEFAULT_BUFFER_SIZE);
    }

    /**
     * Reads the entire input from the given reader and parses it as JSON. The input must contain a
     * valid JSON value, optionally padded with whitespace.
     * <p>
     * Characters are read in chunks into an input buffer of the given size. Hence, wrapping a reader
     * in an additional <code>BufferedReader</code> likely won't improve reading performance.
     * </p>
     *
     * @param buffersize the size of the input buffer in chars
     * @throws IOException    if an I/O error occurs in the reader
     * @throws JsonException if the input is not valid JSON
     */
    public void parse(int buffersize) throws IOException {
        if (reader == null) {
            throw new NullPointerException("reader is null");
        }
        if (buffersize <= 0) {
            throw new IllegalArgumentException("buffersize is zero or negative");
        }
        buffer = new char[buffersize];
        index = 0;
        fill = 0;
        current = 0;
        captureStart = -1;
        read();
        skipWhiteSpace();
        readValue();
        skipWhiteSpace();
        if (!isEndOfText()) {
            throw error("Unexpected character");
        }
    }

    private void readValue() throws IOException {
        switch (current) {
            case 'n':
                readNull();
                break;
            case 't':
                readTrue();
                break;
            case 'f':
                readFalse();
                break;
            case '"':
                readString();
                break;
            case '[':
                readArray();
                break;
            case '{':
                readObject();
                break;
            case '-':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                readNumber();
                break;
            default:
                throw expected("value");
        }
    }

    private void readArray() throws IOException {
        A array = handler.startArray();
        read();
        if (++nestingLevel > MAX_NESTING_LEVEL) {
            throw error("Nesting too deep");
        }
        skipWhiteSpace();
        if (readChar(']')) {
            nestingLevel--;
            handler.endArray(array);
            return;
        }
        do {
            skipWhiteSpace();
            handler.startArrayValue(array);
            readValue();
            handler.endArrayValue(array);
            skipWhiteSpace();
        } while (readChar(','));
        if (!readChar(']')) {
            throw expected("',' or ']'");
        }
        nestingLevel--;
        handler.endArray(array);
    }

    private void readObject() throws IOException {
        O object = handler.startObject();
        read();
        if (++nestingLevel > MAX_NESTING_LEVEL) {
            throw error("Nesting too deep");
        }
        skipWhiteSpace();
        if (readChar('}')) {
            nestingLevel--;
            handler.endObject(object);
            return;
        }
        do {
            skipWhiteSpace();
            handler.startObjectName(object);
            String name = readName();
            handler.endObjectName(object, name);
            skipWhiteSpace();
            if (!readChar(':')) {
                throw expected("':'");
            }
            skipWhiteSpace();
            handler.startObjectValue(object, name);
            readValue();
            handler.endObjectValue(object, name);
            skipWhiteSpace();
        } while (readChar(','));
        if (!readChar('}')) {
            throw expected("',' or '}'");
        }
        nestingLevel--;
        handler.endObject(object);
    }

    private String readName() throws IOException {
        if (current != '"') {
            throw expected("name");
        }
        return readStringInternal();
    }

    private void readNull() throws IOException {
        handler.startNull();
        read();
        readRequiredChar('u');
        readRequiredChar('l');
        readRequiredChar('l');
        handler.endNull();
    }

    private void readTrue() throws IOException {
        handler.startBoolean();
        read();
        readRequiredChar('r');
        readRequiredChar('u');
        readRequiredChar('e');
        handler.endBoolean(true);
    }

    private void readFalse() throws IOException {
        handler.startBoolean();
        read();
        readRequiredChar('a');
        readRequiredChar('l');
        readRequiredChar('s');
        readRequiredChar('e');
        handler.endBoolean(false);
    }

    private void readRequiredChar(char ch) throws IOException {
        if (!readChar(ch)) {
            throw expected("'" + ch + "'");
        }
    }

    private void readString() throws IOException {
        handler.startString();
        handler.endString(readStringInternal());
    }

    private String readStringInternal() throws IOException {
        read();
        startCapture();
        while (current != '"') {
            if (current == '\\') {
                pauseCapture();
                readEscape();
                startCapture();
            } else if (current < 0x20) {
                throw expected("valid string character");
            } else {
                read();
            }
        }
        String string = endCapture();
        read();
        return string;
    }

    private void readEscape() throws IOException {
        read();
        switch (current) {
            case '"':
            case '/':
            case '\\':
                captureBuffer.append((char) current);
                break;
            case 'b':
                captureBuffer.append('\b');
                break;
            case 'f':
                captureBuffer.append('\f');
                break;
            case 'n':
                captureBuffer.append('\n');
                break;
            case 'r':
                captureBuffer.append('\r');
                break;
            case 't':
                captureBuffer.append('\t');
                break;
            case 'u':
                char[] hexChars = new char[4];
                for (int i = 0; i < 4; i++) {
                    read();
                    if (!isHexDigit()) {
                        throw expected("hexadecimal digit");
                    }
                    hexChars[i] = (char) current;
                }
                captureBuffer.append((char) Integer.parseInt(new String(hexChars), 16));
                break;
            default:
                throw expected("valid escape sequence");
        }
        read();
    }

    private void readNumber() throws IOException {
        handler.startNumber();
        startCapture();
        readChar('-');
        int firstDigit = current;
        if (!readDigit()) {
            throw expected("digit");
        }
        if (firstDigit != '0') {
            while (true) {
                if (!readDigit()) {
                    break;
                }
            }
        }
        readFraction();
        readExponent();
        handler.endNumber(endCapture());
    }

    private void readFraction() throws IOException {
        if (!readChar('.')) {
            return;
        }
        if (!readDigit()) {
            throw expected("digit");
        }
        while (true) {
            if (!readDigit()) {
                break;
            }
        }
    }

    private void readExponent() throws IOException {
        if (!readChar('e') && !readChar('E')) {
            return;
        }
        if (!readChar('+')) {
            readChar('-');
        }
        if (!readDigit()) {
            throw expected("digit");
        }
        while (true) {
            if (!readDigit()) {
                break;
            }
        }
    }

    private boolean readChar(char ch) throws IOException {
        if (current != ch) {
            return false;
        }
        read();
        return true;
    }

    private boolean readDigit() throws IOException {
        if (!isDigit()) {
            return false;
        }
        read();
        return true;
    }

    private void skipWhiteSpace() throws IOException {
        while (isWhiteSpace()) {
            read();
        }
    }

    private void read() throws IOException {
        if (index == fill) {
            if (captureStart != -1) {
                captureBuffer.append(buffer, captureStart, fill - captureStart);
                captureStart = 0;
            }
            fill = reader.read(buffer, 0, buffer.length);
            index = 0;
            if (fill == -1) {
                current = -1;
                index++;
                return;
            }
        }
        current = buffer[index++];
    }

    private void startCapture() {
        if (captureBuffer == null) {
            captureBuffer = new StringBuilder();
        }
        captureStart = index - 1;
    }

    private void pauseCapture() {
        int end = current == -1 ? index : index - 1;
        captureBuffer.append(buffer, captureStart, end - captureStart);
        captureStart = -1;
    }

    private String endCapture() {
        int start = captureStart;
        int end = index - 1;
        captureStart = -1;
        if (captureBuffer.length() > 0) {
            captureBuffer.append(buffer, start, end - start);
            String captured = captureBuffer.toString();
            captureBuffer.setLength(0);
            return captured;
        }
        return new String(buffer, start, end - start);
    }

    private JsonException expected(String expected) {
        if (isEndOfText()) {
            return error("Unexpected end of input");
        }
        return error("Expected " + expected);
    }

    private JsonException error(String message) {
        return new JsonException(message);
    }

    private boolean isWhiteSpace() {
        return current == ' ' || current == '\t' || current == '\n' || current == '\r';
    }

    private boolean isDigit() {
        return current >= '0' && current <= '9';
    }

    private boolean isHexDigit() {
        return current >= '0' && current <= '9'
                || current >= 'a' && current <= 'f'
                || current >= 'A' && current <= 'F';
    }

    private boolean isEndOfText() {
        return current == -1;
    }

}
