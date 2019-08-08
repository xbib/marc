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
 * Record status
 *
 * One-character alphabetic code that indicates the relationship of the record to a file
 * for file maintenance purposes.
 *
 * a - Increase in encoding level
 * Encoding level (Leader/17) of the record has been changed to a higher encoding level.
 *
 * Indicates an increase in the level of cataloging (e.g., code a is used when a preliminary
 * cataloging record (code 5 in Leader/17) is raised to full cataloging level (code # in Leader/17)).
 *
 * c - Corrected or revised
 * Addition/change other than in the Encoding level code has been made to the record.
 *
 * d - Deleted
 * Record has been deleted.
 *
 * n - New
 * Record is newly input.
 *
 * p - Increase in encoding level from prepublication
 * Prepublication record has had a change in cataloging level resulting from the availability
 * of the published item.
 *
 * Example: a CIP record (code 8 in Leader/17)) upgraded to a full record (code # or 1 in Leader/17.)
 */
public enum RecordStatus {

    UNKNOWN(' '),
    INCREASE_IN_ENCODING_LEVEL('a'),
    CORRECTED_OR_REVISED('c'),
    DELETED('d'),
    NEW('n'),
    INCREASE_IN_ENCODING_LEVEL_FROM_PREPUBLICATION('p');

    private static final Logger logger = Logger.getLogger(RecordStatus.class.getName());

    private final char ch;

    RecordStatus(char ch) {
        this.ch = ch;
    }

    public char getChar() {
        return ch;
    }

    public static RecordStatus from(char ch) {
        switch (ch) {
            case ' ':
                return UNKNOWN;
            case 'a':
                return INCREASE_IN_ENCODING_LEVEL;
            case 'c':
                return CORRECTED_OR_REVISED;
            case 'd':
                return DELETED;
            case 'n':
                return NEW;
            case 'p':
                return INCREASE_IN_ENCODING_LEVEL_FROM_PREPUBLICATION;
            default:
                logger.log(Level.FINEST,"unknown record status: " + ch);
                return UNKNOWN;
        }
    }
}
