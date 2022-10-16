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
package org.xbib.marc.transformer.value;

public class Xml10MarcValueCleaner implements MarcValueTransformer {

    public Xml10MarcValueCleaner() {
    }

    @Override
    public String transform(String string) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, len = string.length(); i < len; i++) {
            char c = string.charAt(i);
            boolean legal = c == '\u0009' || c == '\n' || c == '\r'
                    || (c >= '\u0020' && c <= '\uD7FF')
                    || (c >= '\uE000' && c <= '\uFFFD');
            if (legal) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
