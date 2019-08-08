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
 * Bibliographic level
 *
 * One-character alphabetic code indicating the bibliographic level of the record.
 *
 * a - Monographic component part
 * Monographic bibliographic unit that is physically attached to or contained in another unit
 * such that the retrieval of the component part is dependent on the identification and location
 * of the host item or container. Contains fields that describe the component part and data that
 * identify the host, field 773 (Host Item Entry).
 *
 * Examples of monographic component parts with corresponding host items include an article in a
 * single issue of a periodical, a chapter in a book, a band on a phonodisc, and a map on a single
 * sheet that contains several maps.
 *
 * b - Serial component part
 * Serial bibliographic unit that is physically attached to or contained in another unit such
 * that the retrieval of the component part is dependent on the identification and location
 * of the host item or container. Contains fields that describe the component part and data that
 * identify the host, field 773 (Host Item Entry).
 *
 * Example of a serial component part with corresponding host item is a regularly appearing column
 * or feature in a periodical.
 *
 * c - Collection
 * Made-up multipart group of items that were not originally published, distributed, or produced
 * together. The record describes units defined by common provenance or administrative convenience
 * for which the record is intended as the most comprehensive in the system.
 *
 * d - Subunit
 * Part of collection, especially an archival unit described collectively elsewhere in the system.
 * Contains fields that describe the subunit and data that identify the host item.
 *
 * Subunits may be items, folders, boxes, archival series, subgroups, or subcollections.
 *
 * i - Integrating resource
 * Bibliographic resource that is added to or changed by means of updates that do not remain
 * discrete and are integrated into the whole. Examples include updating loose-leafs and
 * updating Web sites.
 *
 * Integrating resources may be finite or continuing.
 *
 * m - Monograph/Item
 * Item either complete in one part (e.g., a single monograph, a single map, a single manuscript,
 * etc.) or intended to be completed, in a finite number of separate parts (e.g., a multivolume
 * monograph, a sound recording with multiple tracks, etc.).
 *
 * s - Serial
 * Bibliographic item issued in successive parts bearing numerical or chronological designations
 * and intended to be continued indefinitely. Includes periodicals; newspapers; annuals (reports,
 * yearbooks, etc.); the journals, memoirs, proceedings, transactions, etc., of societies; and
 * numbered monographic series, etc.
 */
public enum BibliographicLevel {

    UNSPECIFIED(' '),
    MONOGRPAHIC_COMPONENT_PART('a'),
    SERIAL_COMPONENT_PART('b'),
    COLLECTION('c'),
    SUBUNIT('d'),
    INTEGRATING_RESOURCE('i'),
    MONOGRAPH('m'),
    SERIAL('s');

    private static final Logger logger = Logger.getLogger(BibliographicLevel.class.getName());

    private final char ch;

    BibliographicLevel(char ch) {
        this.ch = ch;
    }

    public char getChar() {
        return ch;
    }

    public static BibliographicLevel from(char ch) {
        switch (ch) {
            case ' ':
                return UNSPECIFIED;
            case 'a':
                return MONOGRPAHIC_COMPONENT_PART;
            case 'b':
                return SERIAL_COMPONENT_PART;
            case 'c':
                return COLLECTION;
            case 'd':
                return SUBUNIT;
            case 'i':
                return INTEGRATING_RESOURCE;
            case 'm':
                return MONOGRAPH;
            case 's':
                return SERIAL;
            default:
                logger.log(Level.FINEST, "unknown bibliographic level: " + ch);
                return UNSPECIFIED;
        }
    }
}
