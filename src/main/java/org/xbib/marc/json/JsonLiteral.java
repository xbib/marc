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

/**
 * A JSON literal.
 */
class JsonLiteral extends JsonValue {

    public static final JsonLiteral NULL = new JsonLiteral("null");

    public static final JsonLiteral TRUE = new JsonLiteral("true");

    public static final JsonLiteral FALSE = new JsonLiteral("false");

    private final String value;

    private final boolean isNull;

    private final boolean isTrue;

    private final boolean isFalse;

    JsonLiteral(String value) {
        this.value = value;
        isNull = "null".equals(value);
        isTrue = "true".equals(value);
        isFalse = "false".equals(value);
    }

    @Override
    void write(JsonWriter writer) throws IOException {
        writer.writeLiteral(value);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean isNull() {
        return isNull;
    }

    @Override
    public boolean isTrue() {
        return isTrue;
    }

    @Override
    public boolean isFalse() {
        return isFalse;
    }

    @Override
    public boolean isBoolean() {
        return isTrue || isFalse;
    }

    @Override
    public boolean asBoolean() {
        return isNull ? super.asBoolean() : isTrue;
    }

    @Override
    public boolean equals(Object object) {
        return this == object || object != null && getClass() == object.getClass() && value.equals(((JsonLiteral) object).value);
    }

}
