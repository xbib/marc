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
package org.xbib.marc.io;

/**
 * An interface for Information separators for formatted data.
 * Also known as control characters group 0 ("C0"), ASCII-1967
 * defines units, records, groups and files as separable hierarchically
 * organized data structures. The structures are separated not by protocol,
 * but by embedded separator codes.
 * Originally, these codes were used to simulate punch card data on magnetic
 * tape. Trailing blanks on tape could be saved by using separator characters
 * instead.
 */
public interface InformationSeparator {

    /**
     * FILE SEPARATOR.
     */
    char FS = '\u001c';
    /**
     * RECORD TERMINATOR / GROUP SEPARATOR  / Satzende (SE).
     */
    char GS = '\u001d';
    /**
     * FIELD TERMINATOR / RECORD SEPARATOR / Feldende (FE).
     */
    char RS = '\u001e';
    /**
     * SUBFIELD DELIMITER / UNIT SEPARATOR /  Unterfeld (UF).
     */
    char US = '\u001f';

}
