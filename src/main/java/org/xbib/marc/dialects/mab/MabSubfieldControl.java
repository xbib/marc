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
package org.xbib.marc.dialects.mab;

import java.util.Map;

/**
 *
 */
public class MabSubfieldControl {

    private static final Map<String, Integer> FIELDS =  Map.of("088", 2, "655", 2, "856", 2);

    private MabSubfieldControl() {
    }

    public static Integer getSubfieldIdLen(String tag) {
        return FIELDS.getOrDefault(tag, 0);
    }
}
