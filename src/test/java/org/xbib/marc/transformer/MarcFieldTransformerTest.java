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
package org.xbib.marc.transformer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.xbib.marc.MarcField;
import org.xbib.marc.transformer.field.MarcFieldTransformer;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class MarcFieldTransformerTest {

    @Test
    public void testMarcFieldTagTransform() {
        MarcFieldTransformer marcFieldTransformer = MarcFieldTransformer.builder()
                .fromTo(MarcField.builder().tag("001").build(), MarcField.builder().tag("002").build())
                .build();
        MarcField a = MarcField.builder().tag("001").value("Hello World").build();
        MarcField b = marcFieldTransformer.transform(a);
        assertEquals("002$$Hello World", b.toString());
    }

    @Test
    public void testMarcFieldTagWithSubfieldTransform() {
        MarcFieldTransformer marcFieldTransformer = MarcFieldTransformer.builder()
                .fromTo(MarcField.builder().tag("001").subfield("a").subfield("b").build(),
                        MarcField.builder().tag("002").subfield("c").subfield("d").build())
                .build();
        MarcField a = MarcField.builder().tag("001").subfield("a", "Hello").subfield("b", "World").build();
        MarcField b = marcFieldTransformer.transform(a);
        assertEquals("002$$cd[c=Hello, d=World]", b.toString());
    }

    @Test
    public void testIgnoreSubfield() {
        MarcFieldTransformer marcFieldTransformer = MarcFieldTransformer.builder()
                .ignoreSubfieldIds()
                .fromTo(MarcField.builder().tag("001").subfield("a").subfield("b").build(),
                        MarcField.builder().tag("002").subfield("c").subfield("d").build())
                .build();
        MarcField a = MarcField.builder().tag("001").subfield("a", "Hello").subfield("b", "World").build();
        MarcField b = marcFieldTransformer.transform(a);
        assertEquals("002$$ab[a=Hello, b=World]", b.toString());
    }

    @Test
    public void testIgnoreIndicator() {
        MarcFieldTransformer marcFieldTransformer = MarcFieldTransformer.builder()
                .ignoreSubfieldIds()
                .ignoreIndicator()
                .fromTo(MarcField.builder().tag("001").build(),
                        MarcField.builder().tag("002").build())
                .build();
        MarcField a = MarcField.builder().tag("001").indicator("00").subfield("a", "Hello").subfield("b", "World").build();
        MarcField b = marcFieldTransformer.transform(a);
        assertEquals("002$00$ab[a=Hello, b=World]", b.toString());
    }

    @Test
    public void testRepeatTransformer() {
        MarcFieldTransformer marcFieldTransformer = MarcFieldTransformer.builder()
                .ignoreSubfieldIds()
                .fromTo(MarcField.builder().tag("001").indicator("00").build(),
                        MarcField.builder().tag("002").indicator("0{r}").build())
                .build();
        MarcField a = MarcField.builder().tag("001").indicator("00").subfield("a", "Hello").subfield("b", "World").build();
        MarcField b = marcFieldTransformer.transform(a);
        assertEquals("002$00$ab[a=Hello, b=World]", b.toString());
        b = marcFieldTransformer.transform(a);
        assertEquals("002$01$ab[a=Hello, b=World]", b.toString());
        b = marcFieldTransformer.transform(a);
        assertEquals("002$02$ab[a=Hello, b=World]", b.toString());
    }

    @Test
    public void testHeadTail() {
        MarcFieldTransformer marcFieldTransformer = MarcFieldTransformer.builder()
                .ignoreSubfieldIds()
                .fromTo(MarcField.builder().tag("001").build(),
                        MarcField.builder().tag("002").build())
                .build();
        MarcField a1 = MarcField.builder().tag("001").subfield("a", "Hello").subfield("b", "World").build();
        MarcField b = marcFieldTransformer.head(a1);
        assertEquals("002$$ab[a=Hello, b=World]", b.toString());
        MarcField a2 = MarcField.builder().tag("001").subfield("c", "Hello").subfield("d", "World").build();
        b = marcFieldTransformer.tail(a2, b);
        assertEquals("002$$abcd[a=Hello, b=World, c=Hello, d=World]", b.toString());
    }

    @Test
    public void testDrop() {
        MarcFieldTransformer marcFieldTransformer = MarcFieldTransformer.builder()
                .ignoreSubfieldIds()
                .drop(MarcField.builder().tag("001").build())
                .build();
        MarcField a = MarcField.builder().tag("001").subfield("a", "Hello").subfield("b", "World").build();
        MarcField b = marcFieldTransformer.transform(a);
        assertEquals(MarcField.EMPTY, b);
    }

    @Test
    public void testStringSpec() {
        MarcFieldTransformer marcFieldTransformer = MarcFieldTransformer.builder()
                .ignoreSubfieldIds()
                .fromTo("001$$", "002$$")
                .build();
        MarcField a = MarcField.builder().tag("001").subfield("a", "Hello").subfield("b", "World").build();
        MarcField b = marcFieldTransformer.transform(a);
        assertEquals("002$$ab[a=Hello, b=World]", b.toString());
    }

    @Test
    public void testStringSpecSubfields() {
        MarcFieldTransformer marcFieldTransformer = MarcFieldTransformer.builder()
                .fromTo("001$$ab", "002$$bb")
                .build();
        MarcField a = MarcField.builder().tag("001").subfield("a", "Hello").subfield("b", "World").build();
        MarcField b = marcFieldTransformer.transform(a);
        assertEquals("002$$bb[b=Hello, b=World]", b.toString());
    }

    @Test
    public void testTwoStringSpecSubfields() {
        MarcFieldTransformer marcFieldTransformer = MarcFieldTransformer.builder()
                .fromTo("001$$ab", "002$$bb")
                .fromTo("003$$ab", "002$$bb")
                .build();
        MarcField a = MarcField.builder().tag("001").subfield("a", "Hello").subfield("b", "World").build();
        MarcField b = marcFieldTransformer.transform(a);
        assertEquals("002$$bb[b=Hello, b=World]", b.toString());
        MarcField a1 = MarcField.builder().tag("003").subfield("a", "Hello").subfield("b", "World").build();
        MarcField b1 = marcFieldTransformer.transform(a1);
        assertEquals("002$$bb[b=Hello, b=World]", b1.toString());
    }

    @Test
    public void testTail() {
        MarcFieldTransformer marcFieldTransformer = MarcFieldTransformer.builder()
                .fromTo("001$$a", "002$$b")
                .fromTo("003$$a", "002$$b")
                .build();
        MarcField a = MarcField.builder().tag("001").subfield("a", "Hello World").build();
        MarcField b = marcFieldTransformer.transform(a);
        assertEquals("002$$b[b=Hello World]", b.toString());
        MarcField a1 = MarcField.builder().tag("003").subfield("a", "Hello World").build();
        MarcField b1 = marcFieldTransformer.tail(a1, b);
        assertEquals("002$$bb[b=Hello World, b=Hello World]", b1.toString());
    }

    @Test
    public void testFromMap() {
        Map<String, String> map = new HashMap<>();
        map.put("001$$a", "002$$b");
        map.put("003$$a", "002$$b");
        MarcFieldTransformer marcFieldTransformer = MarcFieldTransformer.builder()
                .from(map)
                .build();
        MarcField a = MarcField.builder().tag("001").subfield("a", "Hello World").build();
        MarcField b = marcFieldTransformer.transform(a);
        assertEquals("002$$b[b=Hello World]", b.toString());
        MarcField a1 = MarcField.builder().tag("003").subfield("a", "Hello World").build();
        MarcField b1 = marcFieldTransformer.tail(a1, b);
        assertEquals("002$$bb[b=Hello World, b=Hello World]", b1.toString());
    }

}
