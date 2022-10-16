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
import java.io.Writer;
import java.util.Arrays;

/**
 * Controls the formatting of the JSON output. Use one of the available constants.
 */
@FunctionalInterface
public interface JsonWriterConfig {

    JsonWriter createWriter(Writer writer);

    /**
     * Write JSON in its minimal form, without any additional whitespace. This is the default.
     */
    static JsonWriterConfig minimal() {
        return JsonWriter::new;
    }

    /**
     * Write JSON in pretty-print, with each value on a separate line and an indentation of two
     * spaces.
     */
    static JsonWriterConfig prettyPrint(int n) {
        return new PrettyPrint(n);
    }

    /**
     * Enables human readable JSON output by inserting whitespace between values.after commas and
     * colons. Example:
     *
     * <pre>
     * jsonValue.writeTo(writer, WriterConfig.prettyPrint());
     * </pre>
     */
    class PrettyPrint implements JsonWriterConfig {

        private final char[] indentChars;

        PrettyPrint(char[] indentChars) {
            this.indentChars = indentChars;
        }

        /**
         * Print every value on a separate line. Use the given number of spaces for indentation.
         *
         * @param number the number of spaces to use
         */
        PrettyPrint(int number) {
            this(fillChars(number));
        }

        private static char[] fillChars(int number) {
            if (number < 0) {
                throw new IllegalArgumentException("number is negative");
            }
            char[] chars = new char[number];
            Arrays.fill(chars, ' ');
            return chars;
        }

        @Override
        public JsonWriter createWriter(Writer writer) {
            return new PrettyPrintWriter(writer, indentChars);
        }
    }

    class PrettyPrintWriter extends JsonWriter {

        private final char[] indentChars;
        private int indent;

        private PrettyPrintWriter(Writer writer, char[] indentChars) {
            super(writer);
            this.indentChars = indentChars;
        }

        @Override
        protected void writeArrayOpen() throws IOException {
            indent++;
            writer.write('[');
            writeNewLine();
        }

        @Override
        protected void writeArrayClose() throws IOException {
            indent--;
            writeNewLine();
            writer.write(']');
        }

        @Override
        protected void writeArraySeparator() throws IOException {
            writer.write(',');
            if (!writeNewLine()) {
                writer.write(' ');
            }
        }

        @Override
        protected void writeObjectOpen() throws IOException {
            indent++;
            writer.write('{');
            writeNewLine();
        }

        @Override
        protected void writeObjectClose() throws IOException {
            indent--;
            writeNewLine();
            writer.write('}');
        }

        @Override
        protected void writeMemberSeparator() throws IOException {
            writer.write(':');
            writer.write(' ');
        }

        @Override
        protected void writeObjectSeparator() throws IOException {
            writer.write(',');
            if (!writeNewLine()) {
                writer.write(' ');
            }
        }

        private boolean writeNewLine() throws IOException {
            if (indentChars == null) {
                return false;
            }
            writer.write('\n');
            for (int i = 0; i < indent; i++) {
                writer.write(indentChars);
            }
            return true;
        }

    }
}
