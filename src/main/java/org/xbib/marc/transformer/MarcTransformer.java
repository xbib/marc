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
package org.xbib.marc.transformer;

import org.xbib.marc.MarcField;
import org.xbib.marc.label.RecordLabel;

/**
 * Interface for transformers for MARC when parsing fields and subfields.
 */
@FunctionalInterface
public interface MarcTransformer {

    /**
     * Transform data into a MARC field, using a record label.
     * @param builder the MARC field builder
     * @param recordLabel the record label
     * @param data the raw data
     */
    void transform(MarcField.Builder builder, RecordLabel recordLabel, String data);
}
