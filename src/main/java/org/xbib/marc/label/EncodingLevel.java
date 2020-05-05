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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Encoding level
 *
 * One-character alphanumeric code that indicates the fullness of the bibliographic information
 * and/or content designation of the MARC record.
 *
 * # - Full level
 * Most complete MARC record created from information derived from an inspection of the physical item.
 *
 * For serials, at least one issue of the serial is inspected.
 *
 * 1 - Full level, material not examined
 * Next most complete MARC record after the full level created from information derived from an extant
 * description of the item (e.g., a printed catalog card or a description in an institutional guide)
 * without reinspection of the physical item. Used primarily in the retrospective conversion of records
 * when all of the information on the extant description is transcribed.
 * Certain control field coding and other data (e.g., field 043 (Geographic Area Code)) are based
 * only on explicit information in the description.
 *
 * 2 - Less-than-full level, material not examined
 * Less-than-full level record (i.e., a record that falls between minimal level and full) created
 * from an extant description of the material (e.g., a printed catalog card) without reinspection of
 * the physical item. Used primarily in the retrospective conversion of records when all of the
 * descriptive access points but only a specified subset of other data elements are transcribed.
 * Authoritative headings may not be current.
 *
 * 3 - Abbreviated level
 * Brief record that does not meet minimal level cataloging specifications. Headings in the records may
 * reflect established forms to the extent that such forms were available at the time the record was created.
 *
 * 4 - Core level
 * Less-than-full but greater-than-minimal level cataloging record that meets core record standards for completeness.
 *
 * 5 - Partial (preliminary) level
 * Preliminary cataloging level record that is not considered final by the creating agency
 * (e.g., the headings may not reflect established forms; the record may not meet national-level cataloging
 * specifications).
 *
 * 7 - Minimal level
 * Record that meets the U.S. National Level Bibliographic Record minimal level cataloging specifications
 * and is considered final by the creating agency. Headings have been checked against an authority file
 * and reflect established forms to the extent that such forms were available at the time the minimal level
 * record was created. The U.S. requirements for minimal-level records can be found in National Level
 * and Minimal Level Record Requirements
 *
 * 8 - Prepublication level
 * Prepublication level record. Includes records created in cataloging in publication programs.
 *
 * u - Unknown
 * Used by an agency receiving or sending data with a local code in Leader/17 cannot adequately determine
 * the appropriate encoding level of the record. Code u thus replaces the local code.
 * Not used in newly input or updated records.
 *
 * For example, code u is used in Dublin Core originated records.
 *
 * z - Not applicable
 * Concept of encoding level does not apply to the record.
 *
 */
public enum EncodingLevel {

    FULL(' '),
    FULL_NOT_EXAMINED('1'),
    LESS_THAN_FULL_NOT_EXAMINED('2'),
    ABBREV('3'),
    CORE('4'),
    PARTIAL('5'),
    MINIMAL('7'),
    PREPUBLICATION('8'),
    UNKNOWN('u'),
    NOT_APPLICABLE('z');

    private static final Logger logger = Logger.getLogger(EncodingLevel.class.getName());

    private final char ch;

    EncodingLevel(char ch) {
        this.ch = ch;
    }

    public char getChar() {
        return ch;
    }

    public static EncodingLevel from(char ch) {
        switch (ch) {
            case ' ':
                return FULL;
            case '1':
                return FULL_NOT_EXAMINED;
            case '2':
                return LESS_THAN_FULL_NOT_EXAMINED;
            case '3':
                return ABBREV;
            case '4':
                return CORE;
            case '5':
                return PARTIAL;
            case '7':
                return MINIMAL;
            case '8':
                return PREPUBLICATION;
            case 'u':
                return UNKNOWN;
            case 'z':
                return NOT_APPLICABLE;
            default:
                logger.log(Level.FINEST, () -> "unknown encoding level " + ch);
                return FULL;
        }
    }
}
