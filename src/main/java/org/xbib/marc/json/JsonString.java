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
import java.util.Objects;

/**
 *
 */
class JsonString extends JsonValue {

    private final String string;

    JsonString(String string) {
        Objects.requireNonNull(string);
        this.string = string;
    }

    @Override
    void write(JsonWriter writer) throws IOException {
        writer.writeString(string);
    }

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public String asString() {
        return string;
    }

    @Override
    public int hashCode() {
        return string.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        return this == object || object != null && getClass() == object.getClass()
                && string.equals(((JsonString) object).string);
    }

}
