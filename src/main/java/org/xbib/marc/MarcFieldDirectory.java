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
package org.xbib.marc;

import org.xbib.marc.label.RecordLabel;

import java.io.IOException;
import java.util.TreeMap;

/**
 *
 */
public class MarcFieldDirectory extends TreeMap<Integer, MarcField.Builder> {

    private static final long serialVersionUID = 4339262603982720001L;

    public MarcFieldDirectory(RecordLabel label, String encodedDirectory) throws IOException {
        super();
        if (label == null) {
            throw new IllegalArgumentException("label must not be null");
        }
        int directoryLength = label.getBaseAddressOfData() - (RecordLabel.LENGTH + 1);
        // assume that negative values means prohibiting directory access
        int taglength = 3;
        if (directoryLength > 0
                && encodedDirectory.length() >= directoryLength
                && label.getDataFieldLength() > 0
                && label.getStartingCharacterPositionLength() > 0
                && label.getSegmentIdentifierLength() >= 0) {
            // directory entry size = key length (fixed at 3)
            // plus data field length
            // plus starting character position length
            // plus segment identifier length
            int entrysize = taglength
                    + label.getDataFieldLength()
                    + label.getStartingCharacterPositionLength()
                    + label.getSegmentIdentifierLength();
            if (directoryLength % entrysize != 0) {
                throw new IOException("invalid ISO 2709 directory length: "
                        + directoryLength + ", definitions in record label: "
                        + " data field length = " + label.getDataFieldLength()
                        + " starting character position length = " + label.getStartingCharacterPositionLength()
                        + " segment identifier length = " + label.getSegmentIdentifierLength());
            }
            for (int i = RecordLabel.LENGTH; i < RecordLabel.LENGTH + directoryLength; i += entrysize) {
                String tag = null;
                try {
                    tag = encodedDirectory.substring(i, i + taglength);
                    int l = i + taglength + label.getDataFieldLength();
                    int length = Integer.parseInt(encodedDirectory.substring(i + taglength, l));
                    int position = label.getBaseAddressOfData() +
                            Integer.parseInt(encodedDirectory.substring(l, l + label.getStartingCharacterPositionLength()));
                    put(position, MarcField.builder().tag(tag).position(position).length(length));
                } catch (StringIndexOutOfBoundsException | NumberFormatException e) {
                    throw new IOException("directory entry corrupt for tag = " + tag + " at position " + i +
                            " directory length = " + directoryLength, e);
                }
            }
        }
    }
}
