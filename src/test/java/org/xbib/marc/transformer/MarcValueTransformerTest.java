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

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.xbib.marc.MarcField;
import org.xbib.marc.transformer.value.MarcValueTransformer;
import org.xbib.marc.transformer.value.MarcValueTransformers;

public class MarcValueTransformerTest {

    @Test
    public void testValueTransformer() {
        MarcValueTransformer marcValueTransformer = value -> value.equals("World") ? "Earth" : value;
        MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
        marcValueTransformers.setMarcValueTransformer(marcValueTransformer);
        MarcField a = MarcField.builder().tag("100").subfield("a", "Hello").subfield("b", "World").build();
        MarcField b = marcValueTransformers.transformValue(a);
        assertEquals("100$$ab[a=Hello, b=Earth]", b.toString());
    }

    @Test
    public void testValueControlFieldTransformer() {
        MarcValueTransformer marcValueTransformer = value -> value.equals("World") ? "Earth" : value;
        MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
        marcValueTransformers.setMarcValueTransformer(marcValueTransformer);
        MarcField a = MarcField.builder().tag("001").value("World").build();
        MarcField b = marcValueTransformers.transformValue(a);
        assertEquals("001$$Earth", b.toString());
    }

    @Test
    public void testValueTransformerForSingleSubfield() {
        MarcValueTransformer marcValueTransformer = value -> value.equals("World") ? "Earth" : value;
        MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
        // select only subfield 'b' for transformation
        marcValueTransformers.setMarcValueTransformer("100$$b", marcValueTransformer);
        // create MARC field with two subfields
        MarcField a = MarcField.builder().tag("100").subfield("a", "World").subfield("b", "World").build();
        MarcField b = marcValueTransformers.transformValue(a);
        // check that transformation has been applied only to subfield 'b'
        assertEquals("100$$ab[a=World, b=Earth]", b.toString());
    }
    @Test
    public void testValueTransformerForManySubfields() {
        MarcValueTransformer marcValueTransformer = value -> value.equals("World") ? "Earth" : value;
        MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
        // select subfield 'a' and 'b' for transformation
        marcValueTransformers.setMarcValueTransformer("100$$ab", marcValueTransformer);
        // create MARC field with two subfields
        MarcField a = MarcField.builder().tag("100").subfield("a", "World").subfield("b", "World").build();
        MarcField b = marcValueTransformers.transformValue(a);
        // check that transformation has been applied to both subfields
        assertEquals("100$$ab[a=Earth, b=Earth]", b.toString());
    }

}
