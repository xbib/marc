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
package org.xbib.marc.label;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 *
 */
public class RecordLabelTest {

    @Test
    public void characterToString() {
        char[] ch = new char[]{'1', '2', '3', '4', '5'};
        assertEquals(12345, Integer.parseInt(String.valueOf(ch[0]) + ch[1] + ch[2] + ch[3] + ch[4]));
    }

    @Test
    public void testIndicatorLength() {
        RecordLabel recordLabel = RecordLabel.builder().setIndicatorLength(2).build();
        assertEquals(2, recordLabel.getIndicatorLength());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalIndicatorLength() {
        RecordLabel recordLabel = RecordLabel.builder().setIndicatorLength(10).build();
        assertEquals(10, recordLabel.getIndicatorLength());
    }

    @Test
    public void testSubfieldIdentifierLength() {
        RecordLabel recordLabel = RecordLabel.builder().setSubfieldIdentifierLength(2).build();
        assertEquals(2, recordLabel.getSubfieldIdentifierLength());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalSubfieldIdentifierLength() {
        RecordLabel recordLabel = RecordLabel.builder().setSubfieldIdentifierLength(10).build();
        assertEquals(10, recordLabel.getSubfieldIdentifierLength());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeMaxRecordLength() {
        RecordLabel recordLabel = RecordLabel.builder().setRecordLength(-1).build();
    }

    @Test
    public void testMaxRecordLength() {
        RecordLabel recordLabel = RecordLabel.builder().setRecordLength(9999).build();
        assertEquals(9999, recordLabel.getRecordLength());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOverflowRecordLength() {
        RecordLabel recordLabel = RecordLabel.builder().setRecordLength(10000).build();
    }

    @Test
    public void testBaseAddressOfData() {
        RecordLabel recordLabel = RecordLabel.builder().setBaseAddressOfData(456).build();
        assertEquals(456, recordLabel.getBaseAddressOfData());
    }

    @Test
    public void testDataFieldLength() {
        RecordLabel recordLabel = RecordLabel.builder().setDataFieldLength(5).build();
        assertEquals(5, recordLabel.getDataFieldLength());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalDataFieldLength() {
        RecordLabel recordLabel = RecordLabel.builder().setDataFieldLength(10).build();
        assertEquals(10, recordLabel.getDataFieldLength());
    }

    @Test
    public void testStartingCharacterPositionLength() {
        RecordLabel recordLabel = RecordLabel.builder().setStartingCharacterPositionLength(4).build();
        assertEquals(4, recordLabel.getStartingCharacterPositionLength());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalStartingCharacterPositionLength() {
        RecordLabel recordLabel = RecordLabel.builder().setStartingCharacterPositionLength(10).build();
        assertEquals(10, recordLabel.getStartingCharacterPositionLength());
    }

    @Test
    public void testSegmentIdentifierLength() {
        RecordLabel recordLabel = RecordLabel.builder().setSegmentIdentifierLength(2).build();
        assertEquals(2, recordLabel.getSegmentIdentifierLength());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalSegmentIdentifierLength() {
        RecordLabel recordLabel = RecordLabel.builder().setSegmentIdentifierLength(10).build();
        assertEquals(10, recordLabel.getSegmentIdentifierLength());
    }

    @Test
    public void testBibliographicLevelRecord() {
        RecordLabel recordLabel = RecordLabel.builder().setBibliographicLevel(BibliographicLevel.MONOGRAPH).build();
        assertEquals(BibliographicLevel.MONOGRAPH, recordLabel.getBibliographicLevel());
    }

    @Test
    public void testRecordStatus() {
        RecordLabel recordLabel = RecordLabel.builder().setRecordStatus(RecordStatus.DELETED).build();
        assertEquals(RecordStatus.DELETED, recordLabel.getRecordStatus());
    }

    @Test
    public void testEncoding() {
        RecordLabel recordLabel = RecordLabel.builder().setEncoding(Encoding.MARC8).build();
        assertEquals(Encoding.MARC8, recordLabel.getEncoding());
    }

    @Test
    public void testRecordLabelBuilder() {
        RecordLabel recordLabel = RecordLabel.builder()
                .setRecordStatus(RecordStatus.DELETED)
                .setBibliographicLevel(BibliographicLevel.INTEGRATING_RESOURCE)
                .setEncodingLevel(EncodingLevel.ABBREV)
                .setDescriptiveCatalogingForm(DescriptiveCatalogingForm.ISBD_PUNCTUATION_INCLUDED)
                .setMultipartResourceRecordLevel(MultipartResourceRecordLevel.PART_WITH_DEPENDENT_TITLE)
                .setTypeOfRecord(TypeOfRecord.ARTIFACT)
                .setTypeOfControl(TypeOfControl.NO_SPECIFIED_TYPE)
                .build();
        assertEquals(RecordStatus.DELETED, recordLabel.getRecordStatus());
        assertEquals(BibliographicLevel.INTEGRATING_RESOURCE, recordLabel.getBibliographicLevel());
        assertEquals(EncodingLevel.ABBREV, recordLabel.getEncodingLevel());
        assertEquals(DescriptiveCatalogingForm.ISBD_PUNCTUATION_INCLUDED, recordLabel.getDescriptiveCatalogingForm());
        assertEquals(MultipartResourceRecordLevel.PART_WITH_DEPENDENT_TITLE, recordLabel.getMultipartResourceRecordLevel());
        assertEquals(TypeOfRecord.ARTIFACT, recordLabel.getTypeOfRecord());
        assertEquals(TypeOfControl.NO_SPECIFIED_TYPE, recordLabel.getTypeOfControl());
    }

    @Test
    public void testWrongRecordLabel() {
        String s = "123456789";
        RecordLabel label = RecordLabel.builder().from(s.toCharArray()).build();
        assertEquals(RecordLabel.LENGTH, label.toString().length());
        s = "123456789012345678901234567890";
        label = RecordLabel.builder().from(s.toCharArray()).build();
        assertEquals(RecordLabel.LENGTH, label.toString().length());
    }

}
