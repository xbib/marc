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
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class JsonBuilder {

    private final Appendable appendable;

    private State state;

    protected JsonBuilder() {
        this(new StringBuilder());
    }

    protected JsonBuilder(Appendable appendable) {
        this.appendable = appendable;
        this.state = new State(null, 0, Structure.DOCSTART, true);
    }

    public JsonBuilder beginCollection() throws IOException {
        this.state = new State(state, state.level + 1, Structure.COLLECTION, true);
        appendable.append('[');
        return this;
    }

    public JsonBuilder endCollection() throws IOException {
        if (state.structure != Structure.COLLECTION) {
            throw new IOException("no array to close");
        }
        appendable.append(']');
        this.state = state != null ? state.parent : null;
        return this;
    }

    public JsonBuilder beginMap() throws IOException {
        if (state.structure == Structure.COLLECTION) {
            beginArrayValue();
        }
        this.state = new State(state, state.level + 1, Structure.MAP, true);
        appendable.append('{');
        return this;
    }

    public JsonBuilder endMap() throws IOException {
        if (state.structure != Structure.MAP && state.structure != Structure.KEY) {
            throw new IOException("no object to close");
        }
        appendable.append('}');
        this.state = state != null ? state.parent : null;
        return this;
    }

    public JsonBuilder buildMap(Map<String, Object> map) throws IOException {
        Objects.requireNonNull(map);
        boolean wrap = state.structure != Structure.MAP;
        if (wrap) {
            beginMap();
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            buildKey(entry.getKey());
            buildValue(entry.getValue());
        }
        if (wrap) {
            endMap();
        }
        return this;
    }

    public JsonBuilder buildCollection(Collection<?> collection) throws IOException {
        Objects.requireNonNull(collection);
        beginCollection();
        for (Object object : collection) {
            buildValue(object);
        }
        endCollection();
        return this;
    }

    @SuppressWarnings("unchecked")
    public JsonBuilder buildValue(Object object) throws IOException {
        if (object instanceof Map) {
            buildMap((Map<String, Object>) object);
            return this;
        } else if (object instanceof Collection) {
            buildCollection((Collection<Object>) object);
            return this;
        }
        if (state.structure == Structure.COLLECTION) {
            beginArrayValue();
        }
        if (object == null) {
            buildNull();
        } else if (object instanceof CharSequence) {
            buildString((CharSequence) object, true);
        } else if (object instanceof Boolean) {
            buildBoolean((Boolean) object);
        } else if (object instanceof Byte) {
            buildNumber((byte) object);
        } else if (object instanceof Integer) {
            buildNumber((int) object);
        } else if (object instanceof Long) {
            buildNumber((long) object);
        } else if (object instanceof Float) {
            buildNumber((float) object);
        } else if (object instanceof Double) {
            buildNumber((double) object);
        } else if (object instanceof Number) {
            buildNumber((Number) object);
        } else if (object instanceof Instant) {
            buildInstant((Instant) object);
        } else {
            throw new IllegalArgumentException("unable to write object class " + object.getClass());
        }
        return this;
    }

    public JsonBuilder buildKey(CharSequence string) throws IOException {
        if (state.structure == Structure.COLLECTION) {
            beginArrayValue();
        } else if (state.structure == Structure.MAP || state.structure == Structure.KEY) {
            beginKey(string != null ? string.toString() : null);
        }
        buildString(string, true);
        if (state.structure == Structure.MAP || state.structure == Structure.KEY) {
            endKey(string != null ? string.toString() : null);
        }
        state.structure = Structure.KEY;
        return this;
    }

    public JsonBuilder buildNull() throws IOException {
        buildString("null", false);
        return this;
    }

    public String build() {
        return appendable.toString();
    }

    void patchOpenMapState() {
        state.first = false;
    }

    private void beginKey(String k) throws IOException {
        if (state.first) {
            state.first = false;
        } else {
            appendable.append(",");
        }
    }

    private void endKey(String k) throws IOException {
        appendable.append(":");
    }

    private void beginArrayValue() throws IOException {
        if (state.first) {
            state.first = false;
        } else {
            appendable.append(",");
        }
    }

    private void buildBoolean(boolean bool) throws IOException {
        buildString(bool ? "true" : "false", false);
    }

    private void buildNumber(Number number) throws IOException {
        buildString(number != null ? number.toString() : null, false);
    }

    private void buildInstant(Instant instant) throws IOException {
        buildString(instant.toString(), true);
    }

    private void buildString(CharSequence string, boolean escape) throws IOException {
        appendable.append(escape ? escapeString(string) : string);
    }

    private CharSequence escapeString(CharSequence string) {
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        int start = 0;
        int l = string.length();
        for (int i = 0; i < l; i++) {
            char c = string.charAt(i);
            // In JavaScript, U+2028 and U+2029 characters count as line endings and must be encoded.
            // http://stackoverflow.com/questions/2965293/javascript-parse-error-on-u2028-unicode-character
            if (c == '"' || c == '\\' || c < 32 || c == '\u2028' || c == '\u2029') {
                if (i > start) {
                    sb.append(string, start, i);
                }
                start = i + 1;
                sb.append(escapeCharacter(c));
            }
        }
        if (l > start) {
            sb.append(string, start, l);
        }
        sb.append('"');
        return sb;
    }

    private static String escapeCharacter(char c) {
        switch (c) {
            case '\n' -> {
                return "\\n";
            }
            case '\r' -> {
                return "\\r";
            }
            case '\t' -> {
                return "\\t";
            }
            case '\\' -> {
                return "\\\\";
            }
            case '\'' -> {
                return "\\'";
            }
            case '\"' -> {
                return "\\\"";
            }
        }
        String hex = Integer.toHexString(c);
        return "\\u0000".substring(0, 6 - hex.length()) + hex;
    }

    private enum Structure {
        DOCSTART, MAP, KEY, COLLECTION
    }

    private static class State {
        State parent;
        int level;
        Structure structure;
        boolean first;

        State(State parent, int level, Structure structure, boolean first) {
            this.parent = parent;
            this.level = level;
            this.structure = structure;
            this.first = first;
        }
    }
}
