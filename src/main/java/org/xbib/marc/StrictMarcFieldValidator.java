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
package org.xbib.marc;

import java.util.Set;

public class StrictMarcFieldValidator implements MarcFieldValidator {

    private static final Set<Character> ASCII_GRAPHICS = Set.of(
            '\u0020', '\u0021', '\u0022', '\u0023', '\u0024', '\u0025', '\u0026', '\'',
            '\u0028', '\u0029', '\u002A', '\u002B', '\u002C', '\u002D', '\u002E', '\u002F',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '\u003A', '\u003B', '\u003C', '\u003D', '\u003E', '\u003F', '\u0040',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z',
            '\u005B', '\\', '\u005D', '\u005E', '\u005F', '\u0060',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z',
            '\u007B', '\u007C', '\u007D', '\u007E'
    );

    private static final char BLANK = ' ';

    private static final String BLANK_STRING = " ";

    private static final String BLANK_TAG = "   ";

    public StrictMarcFieldValidator() {
    }

    @Override
    public String validateTag(String tagCandidate) {
        String tag = tagCandidate;
        if (tag != null) {
            // do not allow empty tags
            if (tag.isEmpty()) {
                tag = BLANK_TAG;
            } else {
                // We have inconsistent use of tag symbols as placeholders for a "blank space"
                // and we need to fix it here for consistency.
                tag = tag.replaceAll("[-#.^_]", BLANK_STRING);
            }
        }
        return tag;
    }

    @Override
    public String validateIndicator(String indicatorCandidate) {
        String indicator = indicatorCandidate;
        if (indicator != null) {
            // we do not allow an empty indicator. Elasticsearch field names require a length > 0.
            if (indicator.isEmpty()) {
                indicator = BLANK_STRING;
            } else {
                // We have inconsistent use of indicator symbols as placeholders for a "blank space"
                // and we need to fix it here for consistency.
                indicator = indicator.replaceAll("[-#.^_]", BLANK_STRING);
            }
        }
        return indicator;
    }

    @Override
    public String validateSubfieldId(String subfieldIdCandidate) {
        String id = subfieldIdCandidate;
        if (id != null) {
            // we do not allow an empty subfield id. Elasticsearch field names require a length > 0.
            if (id.isEmpty()) {
                id = BLANK_STRING;
            } else {
                // We have inconsistent use of subfield id symbols as placeholders for a "blank space"
                // and we need to fix it here for consistency.
                id = id.replaceAll("[-#.^_]", BLANK_STRING);
            }
        }
        return id;
    }

    @Override
    public boolean isTagValid(String tag) {
        if (tag == null) {
            // we allow no tag
            return true;
        }
        // only tags of length 3 are supposed to be valid or an empty tag
        return BLANK_TAG.equals(tag) || tag.length() == 3
                && ((tag.charAt(0) >= '0' && tag.charAt(0) <= '9')
                || (tag.charAt(0) >= 'A' && tag.charAt(0) <= 'Z'))
                && ((tag.charAt(1) >= '0' && tag.charAt(1) <= '9')
                || (tag.charAt(1) >= 'A' && tag.charAt(1) <= 'Z'))
                && ((tag.charAt(2) >= '0' && tag.charAt(2) <= '9')
                || (tag.charAt(2) >= 'A' && tag.charAt(2) <= 'Z'));
    }

    @Override
    public boolean isIndicatorValid(String indicator) {
        boolean b = indicator.length() <= 9;
        for (int i = 0; i < indicator.length(); i++) {
            b = indicator.charAt(i) == ' '
                    || (indicator.charAt(i) >= '0' && indicator.charAt(i) <= '9')
                    || (indicator.charAt(i) >= 'a' && indicator.charAt(i) <= 'z')
                    || (indicator.charAt(i) >= 'A' && indicator.charAt(i) <= 'Z')
                    || indicator.charAt(i) == '@'; // must be valid, for PICA dialect
            if (!b) {
                break;
            }
        }
        return b;
    }

    @Override
    public boolean isSubfieldIdValid(String subfieldId) {
        return ASCII_GRAPHICS.contains(subfieldId.charAt(0));
    }
}
