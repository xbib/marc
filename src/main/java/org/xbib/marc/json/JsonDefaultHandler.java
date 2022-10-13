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

public class JsonDefaultHandler implements JsonHandler<JsonArray, JsonObject> {

    protected JsonValue value;

    public JsonDefaultHandler() {
    }

    public JsonValue getValue() {
        return value;
    }

    @Override
    public JsonArray startArray() {
        return new JsonArray();
    }

    @Override
    public JsonObject startObject() {
        return new JsonObject();
    }

    @Override
    public void startNull() {
    }

    @Override
    public void endNull() {
        value = JsonLiteral.NULL;
    }

    @Override
    public void startBoolean() {
    }

    @Override
    public void endBoolean(boolean bool) {
        value = bool ? JsonLiteral.TRUE : JsonLiteral.FALSE;
    }

    @Override
    public void startString() {
    }

    @Override
    public void endString(String string) {
        value = new JsonString(string);
    }

    @Override
    public void startNumber() {
    }

    @Override
    public void endNumber(String string) {
        value = new JsonNumber(string);
    }

    @Override
    public void endArray(JsonArray array) {
        value = array;
    }

    @Override
    public void startArrayValue(JsonArray array) {
    }

    @Override
    public void endObject(JsonObject object) {
        value = object;
    }

    @Override
    public void startObjectName(JsonObject object) {
    }

    @Override
    public void endObjectName(JsonObject object, String name) {
    }

    @Override
    public void startObjectValue(JsonObject object, String name) {
    }

    @Override
    public void endArrayValue(JsonArray array) {
        array.add(value);
    }

    @Override
    public void endObjectValue(JsonObject object, String name) {
        object.add(name, value);
    }
}
