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
package org.xbib.marc.label;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Record label of ISO 2709 records.
 */
public class RecordLabel {

    /**
     * The length of a record label is fixed at 24 characters.
     */
    public static final int LENGTH = 24;

    public static final RecordLabel EMPTY = RecordLabel.builder().build();

    private final Builder builder;

    private final String label;

    private RecordLabel(Builder builder, String label) {
        this.builder = builder;
        this.label = label;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getRecordLength() {
        return builder.recordLength;
    }

    public RecordStatus getRecordStatus() {
        return builder.recordStatus;
    }

    public BibliographicLevel getBibliographicLevel() {
        return builder.bibliographicLevel;
    }

    public EncodingLevel getEncodingLevel() {
        return builder.encodingLevel;
    }

    public DescriptiveCatalogingForm getDescriptiveCatalogingForm() {
        return builder.descriptiveCatalogingForm;
    }

    public MultipartResourceRecordLevel getMultipartResourceRecordLevel() {
        return builder.multipartResourceRecordLevel;
    }

    public TypeOfRecord getTypeOfRecord() {
        return builder.typeOfRecord;
    }

    public TypeOfControl getTypeOfControl() {
        return builder.typeOfControl;
    }

    public Encoding getEncoding() {
        return builder.encoding;
    }

    public int getIndicatorLength() {
        return builder.indicatorLength;
    }

    public int getSubfieldIdentifierLength() {
        return builder.subfieldIdentifierLength;
    }

    public int getBaseAddressOfData() {
        return builder.baseAddressOfData;
    }

    public int getDataFieldLength() {
        return builder.dataFieldLength;
    }

    public int getStartingCharacterPositionLength() {
        return builder.startingCharacterPositionLength;
    }

    public int getSegmentIdentifierLength() {
        return builder.segmentIdentifierLength;
    }

    public byte[] asBytes() {
        return label.getBytes(StandardCharsets.ISO_8859_1);
    }

    @Override
    public String toString() {
        return label;
    }

    /**
     * Builder for record label.
     */
    public static class Builder {

        private final char[] empty = new char[] {
                ' ', ' ', ' ', ' ', ' ', ' ',
                ' ', ' ', ' ', ' ', ' ', ' ',
                ' ', ' ', ' ', ' ', ' ', ' ',
                ' ', ' ', ' ', ' ', ' ', ' '
        };

        private char[] cfix;

        private int recordLength;

        private RecordStatus recordStatus;

        private BibliographicLevel bibliographicLevel;

        private int indicatorLength;

        private int subfieldIdentifierLength;

        private int baseAddressOfData;

        private int dataFieldLength;

        private int startingCharacterPositionLength;

        private int segmentIdentifierLength;

        private EncodingLevel encodingLevel;

        private DescriptiveCatalogingForm descriptiveCatalogingForm;

        private MultipartResourceRecordLevel multipartResourceRecordLevel;

        private TypeOfRecord typeOfRecord;

        private TypeOfControl typeOfControl;

        private Encoding encoding;

        private Builder() {
            cfix = empty;
            repair();
        }

        public Builder from(RecordLabel recordLabel) {
            cfix = recordLabel.toString().toCharArray();
            repair();
            return this;
        }

        /**
         * Five decimal digits, right justified, with zero fill where necessary,
         * representing the number of characters in the entire record, including the
         * label itself, the directory, and the variable fields. This data element
         * is normally calculated automatically when the total record is assembled
         * for exchange.
         *
         * @param length the length
         * @return this builder
         */
        public Builder setRecordLength(int length) {
            if (length >= 0 && length < 10000) {
                this.recordLength = length;
                String s = String.format("%05d", length);
                for (int i = 0; i < 5; i++) {
                    cfix[i] = s.charAt(i);
                }
            } else {
                throw new IllegalArgumentException();
            }
            return this;
        }

        /**
         * A single character, denoting the processing status of the record.
         *
         * c corrected record
         *
         * A record to which changes have been made to correct errors, one which has
         * been amended to bring it up to date, or one where fields have been
         * deleted. However, if the previous record was a prepublication record
         * (e.g.; CIP) and a full record replacement is now being issued, code 'p'
         * should be used instead of 'c'. A record labelled 'n', 'o' or 'p' on which
         * a correction is made is coded as 'c'.
         *
         * d deleted record
         *
         * A record which is exchanged in order to indicate that a record bearing
         * this control number is no longer valid. The record may contain only the
         * label, directory; and 001 (record control number) field, or it may
         * contain all the fields in the record as issued; in either case GENERAL
         * NOTE 300 field may be used to explain why the record is deleted.
         *
         * n new record
         *
         * A new record (including a pre-publication record, e.g., CIP). If code 'o'
         * applies, it is used in preference to ' n '.
         *
         * o previously issued higher level record
         *
         * A new record at a hierarchical level below the highest level for which a
         * higher level record has already been issued (see also character position
         * 8).
         *
         * p previously issued as an incomplete, pre-publication record
         *
         * A record for a published item replacing a pre-publication record, e.g.,
         * CIP.
         *
         * @param recordStatus the record status
         * @return this builder
         */
        public Builder setRecordStatus(RecordStatus recordStatus) {
            this.recordStatus = recordStatus;
            cfix[5] = recordStatus.getChar();
            return this;
        }

        /**
         * Set type of record. See {@link TypeOfRecord}.
         * @param typeOfRecord the type of record
         * @return this builder
         */
        public Builder setTypeOfRecord(TypeOfRecord typeOfRecord) {
            this.typeOfRecord = typeOfRecord;
            cfix[6] = typeOfRecord.getChar();
            return this;
        }

        /**
         * The bibliographic level of a record relates to the main part of the
         * record. Some cataloguing codes may not make a clear distinction between a
         * multipart item (multivolume monograph) and a monographic series. In such
         * cases an agency should use whichever of the values is more appropriate in
         * the majority of cases. Where such a distinction is made, but cannot be
         * determined in a particular instance, the item should be coded as a
         * serial.
         *
         * 'a' analytic (component part)
         *
         * bibliographic item that is physically contained in another item such that
         * the location of the component part is dependent upon the physical
         * identification and location of the containing item. A component part may
         * itself be either monographic or serial.
         *
         * The following are examples of materials that are coded 'a': an article in
         * a journal; a continuing column or feature within a journal; a single
         * paper in a collection of conference proceedings.
         *
         * 'c' collection
         *
         * bibliographic item that is a made-up collection.
         *
         * The following are examples of materials which are coded 'c': a collection
         * of pamphlets housed in a box; a set of memorabilia in various formats
         * kept together as a collection; all the manuscripts of an individual
         * author.
         *
         * This code is used only for made-up collections.
         *
         * 'm' monographic
         *
         * bibliographic item complete in one physical part or intended to be
         * completed in a finite number of parts.
         *
         * The following are examples of materials which are coded 'm': a single
         * part item (monograph); a multipart item (multivolume monograph); a
         * separately catalogued single part of a multipart item; a book in a
         * series; a separately catalogued special issue of a newspaper; a sheet map
         * in a series; a complete series of maps, assuming the series was intended
         * to be completed in a finite number of parts; a single globe.
         *
         * 's' serial
         *
         * bibliographic item issued in successive parts and intended to be
         * continued indefinitely.
         *
         * The following are examples of materials which are coded 's': a journal
         * that is still being published; a complete run of a journal that has
         * ceased publication; a newspaper; a monographic series.
         *
         * @param level the level
         * @return this builder
         */
        public Builder setBibliographicLevel(BibliographicLevel level) {
            this.bibliographicLevel = level;
            cfix[7] = level.getChar();
            return this;
        }

        /**
         * Set type of control. See {@link TypeOfControl}.
         * @param typeOfControl the type of control
         * @return this builder
         */
        public Builder setTypeOfControl(TypeOfControl typeOfControl) {
            this.typeOfControl = typeOfControl;
            cfix[8] = typeOfControl.getChar();
            return this;
        }

        /**
         * Set encoding. See {@link Encoding}.
         * @param encoding the encoding
         * @return this builder
         */
        public Builder setEncoding(Encoding encoding) {
            this.encoding = encoding;
            cfix[9] = encoding.getChar();
            return this;
        }

        /**
         * Indicator length is a numeric digit giving the length of the indicators.
         *
         * @param length the length
         * @return this builder
         */
        public Builder setIndicatorLength(int length) {
            if (length >= 0 && length < 10) {
                this.indicatorLength = length;
                cfix[10] = (char) ('0' + length);
                return this;
            } else {
                throw new IllegalArgumentException();
            }
        }

        /**
         * A numeric digit giving the length of the subfield identifier.
         *
         * @param subfieldIdentifierLength the subfield identifier length
         * @return this builder
         */
        public Builder setSubfieldIdentifierLength(int subfieldIdentifierLength) {
            if (subfieldIdentifierLength >= 0 && subfieldIdentifierLength < 10) {
                this.subfieldIdentifierLength = subfieldIdentifierLength;
                cfix[11] = (char) ('0' + subfieldIdentifierLength);
            } else {
                throw new IllegalArgumentException();
            }
            return this;
        }

        /**
         * Base address of data. The location within the record at which the first
         * datafield begins, relative to the first character in the record, which is
         * designated character position `0' (zero).
         *
         * Five numeric digits, right justified with leading zeros, indicating the
         * starting character position of the first data field relative to the
         * beginning of the record. Since the first character of the record is
         * numbered 0 (zero), the number entered as the base address of data will be
         * equal to the total number of characters in the label and directory
         * including the field separator that terminates the directory. In the
         * directory, the starting character position for each field is given
         * relative to the first character of the first data field which will be
         * field 001, rather than the beginning of the record. The base address thus
         * gives the base from which the position of each field is calculated.
         *
         * @param baseAddressOfData the base address of data
         * @return this builder
         */
        public Builder setBaseAddressOfData(int baseAddressOfData) {
            if (baseAddressOfData >= 0 && baseAddressOfData < 10000) {
                this.baseAddressOfData = baseAddressOfData;
                String s = String.format("%05d", baseAddressOfData);
                for (int i = 12; i < 17; i++) {
                    cfix[i] = s.charAt(i - 12);
                }
            } else {
                throw new IllegalArgumentException();
            }
            return this;
        }

        /**
         * Set encoding level. See {@link EncodingLevel}.
         * @param encodingLevel the encoding level
         * @return this builder
         */
        public Builder setEncodingLevel(EncodingLevel encodingLevel) {
            this.encodingLevel = encodingLevel;
            cfix[17] = encodingLevel.getChar();
            return this;
        }

        /**
         * Set descriptive cataloging form. See {@link DescriptiveCatalogingForm}.
         * @param descriptiveCatalogingForm the descriptive cataloging form
         * @return this builder
         */
        public Builder setDescriptiveCatalogingForm(DescriptiveCatalogingForm descriptiveCatalogingForm) {
            this.descriptiveCatalogingForm = descriptiveCatalogingForm;
            cfix[18] = descriptiveCatalogingForm.getChar();
            return this;
        }

        /**
         * Set multipart resource record level. See {@link MultipartResourceRecordLevel}.
         * @param multipartResourceRecordLevel the multipart resource record level
         * @return this builder
         */
        public Builder setMultipartResourceRecordLevel(MultipartResourceRecordLevel multipartResourceRecordLevel) {
            this.multipartResourceRecordLevel = multipartResourceRecordLevel;
            cfix[19] = multipartResourceRecordLevel.getChar();
            return this;
        }

        /**
         * Length of data field A four-digit number showing how many characters are
         * occupied the datafield, including indicators and datafield separator but
         * excluding the record separator code if the datafield is the last field in
         * the record. The use of 4 characters permits datafields as long as 9999
         * characters.
         *
         * @param length the length
         * @return this builder
         */
        public Builder setDataFieldLength(int length) {
            if (length >= 0 && length < 10) {
                this.dataFieldLength = length;
                cfix[20] = (char) ('0' + length);
            } else {
                throw new IllegalArgumentException();
            }
            return this;
        }

        /**
         * Length of the starting-character-position portion of each entry.
         *
         * @param length the length
         * @return this builder
         */
        public Builder setStartingCharacterPositionLength(int length) {
            if (length >= 0 && length < 10) {
                this.startingCharacterPositionLength = length;
                cfix[21] = (char) ('0' + length);
            } else {
                throw new IllegalArgumentException();
            }
            return this;
        }

        /**
         * The length of implementation-defined section of each entry in the
         * directory. Of the two characters, one is used for the segment identifier,
         * the other for the occurrence identifier.
         *
         * @param length the length
         * @return tthis builder
         */
        public Builder setSegmentIdentifierLength(int length) {
            if (length >= 0 && length < 10) {
                this.segmentIdentifierLength = length;
                cfix[22] = (char) ('0' + length);
            } else {
                throw new IllegalArgumentException();
            }
            return this;
        }

        public Builder from(String label) {
            return from(label.toCharArray());
        }

        /**
         * Parse given record label.
         * @param recordLabel the record label
         * @return this builder
         */
        public Builder from(char[] recordLabel) {
            char[] label = recordLabel;
            if (label.length > LENGTH) {
                label = Arrays.copyOf(label, LENGTH);
            } else if (label.length < LENGTH) {
                // fill with blanks
                char[] ch = new char[LENGTH - label.length];
                Arrays.fill(ch, ' ');
                char[] newLabel = new char[LENGTH];
                System.arraycopy(label, 0, newLabel, 0, label.length);
                System.arraycopy(ch, 0, newLabel, label.length, ch.length);
                label = newLabel;
            }
            System.arraycopy(label, 0, cfix, 0, LENGTH);
            return this;
        }

        private void repair() {
            int[] pos = new int[] { 0, 1, 2, 3, 4, 10, 11, 12, 13, 14, 15, 16, 20, 21, 22 };
            for (int i : pos) {
                if (cfix[i] < '0' || cfix[i] > '9') {
                    cfix[i] = '0';
                }
            }
            pos = new int[] { 5, 6, 7, 8, 9, 17, 18, 19, 23 };
            for (int i : pos) {
                if (cfix[i] == '^' || cfix[i] == '-') {
                    cfix[i] = ' '; // unspecified
                }
                // suppress C0 control chars (for XML 1.0 output)
                if (cfix[i] < 32) {
                    cfix[i] = ' '; // unspecified
                }
            }
        }

        private void assign() {
            this.recordLength = Integer.parseInt(String.valueOf(cfix[0]) + cfix[1] + cfix[2] + cfix[3] + cfix[4]);
            this.recordStatus = RecordStatus.from(cfix[5]);
            this.typeOfRecord = TypeOfRecord.from(cfix[6]);
            this.bibliographicLevel = BibliographicLevel.from(cfix[7]);
            this.typeOfControl = TypeOfControl.from(cfix[8]);
            this.encoding = Encoding.from(cfix[9]);
            this.indicatorLength = cfix[10] - '0';
            this.subfieldIdentifierLength = cfix[11] - '0';
            this.baseAddressOfData = Integer.parseInt(String.valueOf(cfix[12]) + cfix[13] + cfix[14] + cfix[15] + cfix[16]);
            this.encodingLevel = EncodingLevel.from(cfix[17]);
            this.descriptiveCatalogingForm = DescriptiveCatalogingForm.from(cfix[18]);
            this.multipartResourceRecordLevel = MultipartResourceRecordLevel.from(cfix[19]);
            this.dataFieldLength = cfix[20] - '0';
            this.startingCharacterPositionLength = cfix[21] - '0';
            this.segmentIdentifierLength = cfix[22] - '0';
        }

        /**
         * Build record label.
         * @return the record label
         */
        public RecordLabel build() {
            repair();
            assign();
            return new RecordLabel(this, new String(cfix));
        }
    }
}
