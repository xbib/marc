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
 * Descriptive cataloging form
 *
 * One-character alphanumeric code that indicates characteristics of the descriptive data in the
 * record through reference to cataloging norms. Subfield $e (Description conventions)
 * of field 040 (Cataloging Source) also contains information on the cataloging conventions used.
 *
 * # - Non-ISBD
 * Descriptive portion of the record does not follow International Standard Bibliographic
 * Description (ISBD) cataloging and punctuation provisions.
 *
 * a - AACR 2
 * Descriptive portion of the record is formulated according to the description and punctuation
 * provisions as incorporated into the Anglo-American Cataloging Rules, 2nd Edition (AACR 2)
 * and its manuals.
 *
 * c - ISBD punctuation omitted
 * Descriptive portion of the record contains the punctuation provisions of ISBD, except ISBD punctuation
 * is not present at the end of a subfield.
 *
 * i - ISBD punctuation included
 * Descriptive portion of the record contains the punctuation provisions of ISBD.
 *
 * u - Unknown
 * Institution receiving or sending data in Leader/18 cannot adequately determine the appropriate
 * descriptive cataloging form used in the record. May be used in records converted from another
 * metadata format.
 *
 */
public enum DescriptiveCatalogingForm {

    NON_ISBD(' '),
    AACR2('a'),
    ISBD_PUNCTUATION_OMITTED('c'),
    ISBD_PUNCTUATION_INCLUDED('i'),
    UNKNOWN('u');

    private static final Logger logger = Logger.getLogger(DescriptiveCatalogingForm.class.getName());

    private final char ch;

    DescriptiveCatalogingForm(char ch) {
        this.ch = ch;
    }

    public char getChar() {
        return ch;
    }

    public static DescriptiveCatalogingForm from(char ch) {
        switch (ch) {
            case ' ':
                return NON_ISBD;
            case 'a':
                return AACR2;
            case 'c':
                return ISBD_PUNCTUATION_OMITTED;
            case 'i':
                return ISBD_PUNCTUATION_INCLUDED;
            case 'u':
                return UNKNOWN;
            default:
                logger.log(Level.FINEST, "unknown descriptive cataloging form: " + ch);
                return NON_ISBD;
        }
    }
}
