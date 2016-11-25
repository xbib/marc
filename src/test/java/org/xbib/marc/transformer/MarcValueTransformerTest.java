package org.xbib.marc.transformer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.xbib.marc.MarcField;
import org.xbib.marc.transformer.value.MarcValueTransformer;
import org.xbib.marc.transformer.value.MarcValueTransformers;

/**
 *
 */
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
