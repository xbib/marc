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
package org.xbib.marc.transformer.value;

import org.xbib.marc.MarcField;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class MarcValueTransformers {

    private final Map<String, MarcValueTransformer> marcValueTransformerMap = new HashMap<>();

    private final Map<String, String> subfieldMap = new HashMap<>();

    public MarcValueTransformers setMarcValueTransformer(MarcValueTransformer transformer) {
        this.marcValueTransformerMap.put("_default", transformer);
        return this;
    }

    /**
     * Set MARC value transformer for transforming field values.
     * @param fieldKey the MARC field key for the field to be transformed
     * @param transformer the string transformer
     * @return this handler
     */
    public MarcValueTransformers setMarcValueTransformer(String fieldKey, MarcValueTransformer transformer) {
        // split into  tag + indicator and subfield IDs
        int pos = fieldKey.lastIndexOf('$');
        String tagind = pos > 0 ? fieldKey.substring(0, pos) : fieldKey;
        String subs = pos > 0 ? fieldKey.substring(pos + 1) : "";
        this.marcValueTransformerMap.put(tagind, transformer);
        // subfield IDs
        this.subfieldMap.put(tagind, subs);
        return this;
    }

    /**
     * Transform value.
     * @param field the MARC field where values are transformed
     * @return a new MARC field with transformed values
     */
    public MarcField transformValue(MarcField field) {
        String key = field.toTagIndicatorKey();
        if (marcValueTransformerMap.isEmpty()) {
            return field;
        }
        final MarcValueTransformer transformer = marcValueTransformerMap.containsKey(key) ?
                marcValueTransformerMap.get(key) : marcValueTransformerMap.get("_default");
        if (transformer != null) {
            MarcField.Builder builder = MarcField.builder();
            builder.tag(field.getTag()).indicator(field.getIndicator());
            if (field.getValue() != null) {
                builder.value(transformer.transform(field.getValue()));
            }
            // select only subfields configured for this tag
            String subs = subfieldMap.containsKey(key) ? subfieldMap.get(key) : field.getSubfieldIds();
            field.getSubfields().forEach(subfield ->
                    builder.subfield(subfield.getId(), subs.contains(subfield.getId()) ?
                                    transformer.transform(subfield.getValue()) : subfield.getValue()));
            return builder.build();
        }
        return field;
    }

    public String transform(String value) {
        MarcValueTransformer marcValueTransformer = marcValueTransformerMap.get("_default");
        return marcValueTransformer != null ? marcValueTransformer.transform(value) : value;
    }
}
