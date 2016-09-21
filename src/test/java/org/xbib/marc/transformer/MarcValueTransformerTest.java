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
        MarcValueTransformer marcValueTransformer = new MarcValueTransformer() {
            @Override
            public String transform(String value) {
                return value.equals("World") ? "Earth" : value;
            }
        };
        MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
        marcValueTransformers.setMarcValueTransformer(marcValueTransformer);
        MarcField a = MarcField.builder().tag("100").subfield("a", "Hello").subfield("b", "World").build();
        MarcField b = marcValueTransformers.transformValue(a);
        assertEquals("100$$ab[a=Hello, b=Earth]", b.toString());
    }

    @Test
    public void testValueControlFieldTransformer() {
        MarcValueTransformer marcValueTransformer = new MarcValueTransformer() {
            @Override
            public String transform(String value) {
                return value.equals("World") ? "Earth" : value;
            }
        };
        MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
        marcValueTransformers.setMarcValueTransformer(marcValueTransformer);
        MarcField a = MarcField.builder().tag("001").value("World").build();
        MarcField b = marcValueTransformers.transformValue(a);
        assertEquals("001$$Earth", b.toString());
    }

}
