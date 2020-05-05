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
 * Multipart resource record level
 * Record level to which a resource pertains and any record dependencies.
 * This information will facilitate processing the record in different situations.
 * For example, the record may describe a set of items, or it may describe a part of a set.
 * The part may only have a dependent title to be used for identification purposes thus
 * requiring use of additional information to understand its context.
 *
 * # - Not specified or not applicable
 * The distinction between record levels is not specified or not applicable for the type of resource.
 *
 * a - Set
 * Record is for a set consisting of multiple items.
 *
 * b - Part with independent title
 * The record is for a resource which is part of a set and has a title that allows it
 * to be independent of the set record.
 *
 * c - Part with dependent title
 * The record is for a resource which is part of a set but has a title that makes it dependent
 * on the set record to understand its context.
 */
public enum MultipartResourceRecordLevel {

    NOT_SPECIFIED(' '),
    SET('a'),
    PART_WITH_INDEPENDENT_TITLE('b'),
    PART_WITH_DEPENDENT_TITLE('c');

    private static final Logger logger = Logger.getLogger(MultipartResourceRecordLevel.class.getName());

    private final char ch;

    MultipartResourceRecordLevel(char ch) {
        this.ch = ch;
    }

    public char getChar() {
        return ch;
    }

    public static MultipartResourceRecordLevel from(char ch) {
        switch (ch) {
            case ' ':
                return NOT_SPECIFIED;
            case 'a':
                return SET;
            case 'b':
                return PART_WITH_INDEPENDENT_TITLE;
            case 'c':
                return PART_WITH_DEPENDENT_TITLE;
            default:
                logger.log(Level.FINEST, () -> "unknown multipart resource record level: " + ch);
                return NOT_SPECIFIED;
        }
    }
}
