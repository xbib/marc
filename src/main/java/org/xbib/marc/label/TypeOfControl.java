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
 * Type of control
 *
 * # - No specified type
 * No type applies to the item being described.
 *
 * a - Archival
 * Material is described according to archival descriptive rules, which focus on the contextual
 * relationships between items and on their provenance rather than on bibliographic detail.
 * The specific set of rules for description may be found in field 040, subfield $e.
 * All forms of material can be controlled archivally.
 */
public enum TypeOfControl {

    UNSPECIFIED(' '),
    ARCHIVAL('a');

    private static final Logger logger = Logger.getLogger(TypeOfControl.class.getName());

    private final char ch;

    TypeOfControl(char ch) {
        this.ch = ch;
    }

    public char getChar() {
        return ch;
    }

    public static TypeOfControl from(char ch) {
        switch (ch) {
            case ' ' :
                return UNSPECIFIED;
            case 'a' :
                return ARCHIVAL;
            default:
                logger.log(Level.FINEST,"unknown type of control: " + ch);
                return UNSPECIFIED;
        }
    }
}
