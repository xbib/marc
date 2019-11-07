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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.io.IOException;
import java.util.Objects;

/**
 */
public class JsonMapperTest {

    @Test
    public void mapperMapTest() throws IOException {
        String json = "{\"Hello\":\"World\"}";
        JsonValue jsonValue = Json.parse(json);
        Object object = JsonMapper.asObject(jsonValue);
        assertEquals("{Hello=World}", Objects.requireNonNull(object).toString());
    }

    @Test
    public void mapperNumericMapTest() throws IOException {
        String json = "{\"Hello\":123}";
        JsonValue jsonValue = Json.parse(json);
        Object object = JsonMapper.asObject(jsonValue);
        assertEquals("{Hello=123}", Objects.requireNonNull(object).toString());
    }

    @Test
    public void mapperArrayTest() throws IOException {
        String json = "[\"Hello\",\"World\"]";
        JsonValue jsonValue = Json.parse(json);
        Object object = JsonMapper.asObject(jsonValue);
        assertEquals("[Hello, World]", Objects.requireNonNull(object).toString());
    }

    @Test
    public void mapperBooleanAndNullArrayTest() throws IOException {
        String json = "[true, false, null]";
        JsonValue jsonValue = Json.parse(json);
        Object object = JsonMapper.asObject(jsonValue);
        assertEquals("[true, false, null]", Objects.requireNonNull(object).toString());
    }

    @Test
    public void mapperFloatArrayTest() throws IOException {
        String json = "[1.23, 4.56]";
        JsonValue jsonValue = Json.parse(json);
        Object object = JsonMapper.asObject(jsonValue);
        assertEquals("[1.23, 4.56]", Objects.requireNonNull(object).toString());
    }

    @Test
    public void mapperIntArrayTest() throws IOException {
        String json = "[123, 456]";
        JsonValue jsonValue = Json.parse(json);
        Object object = JsonMapper.asObject(jsonValue);
        assertEquals("[123, 456]", Objects.requireNonNull(object).toString());
    }
}
