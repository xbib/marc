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
package org.xbib.marc;

import org.junit.Assert;
import org.junit.Test;

import java.util.TreeSet;

/**
 *
 */
public class MarcFieldTest extends Assert {

    @Test
    public void testFieldData() {
        MarcField marcField = MarcField.builder().tag("100").indicator("").value("Hello World").build();
        assertEquals(marcField.getValue(), "Hello World");
    }

    @Test
    public void testLonerField() {
        MarcField marcField = MarcField.builder().build();
        assertEquals("$$", marcField.toKey());
    }

    @Test
    public void testControlField() {
        MarcField marcField = MarcField.builder().tag("001").value("ID").build();
        assertEquals("001$$", marcField.toKey());
        assertEquals("001", marcField.getTag());
        assertEquals("ID", marcField.getValue());
        assertTrue(marcField.isControl());
    }

    @Test
    public void testSingleTagField() {
        MarcField marcField = MarcField.builder().tag("100").build();
        assertEquals("100$$", marcField.toKey());
    }

    @Test
    public void testSingleFieldWithIndicators() {
        MarcField marcField = MarcField.builder()
                .tag("100")
                .indicator("01")
                .build();
        assertEquals("100$01$", marcField.toKey());
    }

    @Test
    public void testSingleFieldWithSubfields() {
        MarcField marcField = MarcField.builder()
                .tag("100")
                .indicator("01")
                .subfield("1", null)
                .subfield("2", null)
                .build();
        assertEquals("100$01$12", marcField.toKey());
    }

    @Test
    public void testNumericSubfields() {
        MarcField marcField = MarcField.builder()
                .tag("016")
                .subfield("1", null)
                .subfield("2", null)
                .subfield("3", null)
                .build();
        assertEquals("016$$123", marcField.toKey());
    }

    @Test
    public void testAlphabeticSubfields() {
        MarcField marcField = MarcField.builder()
                .tag("016")
                .subfield("a", null)
                .subfield("b", null)
                .subfield("c", null)
                .build();
        assertEquals("016$$abc", marcField.toKey());
    }

    @Test
    public void testRepeatingSubfields() {
        MarcField marcField = MarcField.builder()
                .tag("016")
                .subfield("a", null)
                .subfield("a", null)
                .subfield("a", null)
                .build();
        assertEquals("016$$aaa", marcField.toKey());
    }

    @Test
    public void testEmptyIndicatorWithSubfields() {
        MarcField marcField = MarcField.builder()
                .tag("016")
                .subfield("1", null)
                .subfield("2", null)
                .subfield("3", null)
                .build();
        assertEquals("016$$123", marcField.toKey());
    }

    @Test
    public void testValid() {
        MarcField marcField = MarcField.builder()
                .tag("016")
                .indicator("  ")
                .subfield("1", null)
                .subfield("2", null)
                .subfield("3", null)
                .build();
        assertTrue(marcField.isTagValid());
        assertTrue(marcField.isIndicatorValid());
        assertTrue(marcField.isSubfieldValid());
    }

    @Test
    public void testInvalidTag() {
        MarcField marcField = MarcField.builder()
                .tag("---")
                .subfield("1", null)
                .subfield("2", null)
                .subfield("3", null)
                .build();
        assertFalse(marcField.isTagValid());
    }

    @Test
    public void testInvalidIndicator() {
        MarcField marcField = MarcField.builder()
                .tag("100")
                .indicator("$")
                .subfield("1", null)
                .subfield("2", null)
                .subfield("3", null)
                .build();
        assertFalse(marcField.isIndicatorValid());
    }

    @Test
    public void testInvalidSubfield() {
        MarcField marcField = MarcField.builder()
                .tag("100")
                .indicator("0")
                .subfield("-", null)
                .build();
        assertFalse(marcField.isSubfieldValid());
    }

    // 901  =, 901  a=98502599, 901  d=0, 901  e=14, 901  =f, 901  =h]
    @Test
    public void testBeginEndFields() {
        MarcField marcField = MarcField.builder()
                .tag("901")
                .indicator("  ")
                .subfield("a", null)
                .subfield("d", null)
                .subfield("e", null)
                .build();
        assertEquals("901$  $ade", marcField.toKey());
    }

    @Test
    public void testEquality() {
        MarcField m1 = MarcField.builder().tag("001").build();
        MarcField m2 = MarcField.builder().tag("001").build();
        assertEquals(m1, m2);
        m1 = MarcField.builder().tag("001").indicator("  ").build();
        m2 = MarcField.builder().tag("001").indicator("  ").build();
        assertEquals(m1, m2);
        m1 = MarcField.builder().tag("001").indicator("  ").subfield('a').build();
        m2 = MarcField.builder().tag("001").indicator("  ").subfield('a').build();
        assertEquals(m1, m2);
        boolean b = m1.equals(m2);
        assertTrue(b);
        int h1 = m1.hashCode();
        int h2 = m2.hashCode();
        assertEquals(h1, h2);
        int cmp = m1.compareTo(m2);
        assertTrue(cmp == 0);
        m1 = MarcField.builder().tag("001").indicator("  ").subfield('a').build();
        m2 = MarcField.builder().tag("002").indicator("  ").subfield('a').build();
        cmp = m1.compareTo(m2);
        assertTrue(cmp != 0);
        TreeSet<MarcField> map = new TreeSet<>();
        map.add(m1);
        map.add(m2);
        assertEquals(2, map.size());
    }

    @Test
    public void testFirstLastSubfields() {
        MarcField marcField = MarcField.builder()
                .tag("100")
                .indicator("  ")
                .subfield("a", "one")
                .subfield("d", "two")
                .subfield("e", "three")
                .build();
        assertEquals("one", marcField.getFirstSubfield().getValue());
        assertEquals("three", marcField.getLastSubfield().getValue());

        marcField = MarcField.builder().tag("100").build();
        assertNull(marcField.getFirstSubfield().getValue());
        assertNull(marcField.getLastSubfield().getValue());
    }

}
