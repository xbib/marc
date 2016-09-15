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
package org.xbib.marc.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * A buffered input stream for iterating over structured data streams with information
 * separators.
 *
 * The information separators of the C0 control group are defined in:
 * - ANSI X3.4-1967 (ASCII)
 * - IETF RFC 20 (Vint Cerf, 1969)
 * - ISO-646:1972
 * - ECMA-6 3rd revision August 1973
 * - ECMA-48
 * - ISO/IEC 6429
 * - CCITT International Telegraph Alphabet Number 5 (ITA-5)
 *
 * From ASCII-1967:
 * "Can be used as delimiters to mark fields of data structures.
 * If used for hierarchical levels, US is the lowest level (dividing
 * plain-text data items), while RS, GS, and FS are of increasing level
 * to divide groups made up of items of the level beneath it."
 *
 * Form IETF RFC 20:
 * "Information Separator: A character which is used to separate
 * and qualify information in a logical sense.  There is a group of four
 * such characters, which are to be used in a hierarchical order."
 *
 * From ECMA-48 (ISO/IEC 6429):
 *
 * "Each information separator is given two names. The names,
 * INFORMATION SEPARATOR FOUR (IS4), INFORMATION SEPARATOR THREE (IS3),
 * INFORMATION SEPARATOR TWO (IS2), and INFORMATION SEPARATOR ONE (IS1)
 * are the general names. The names FILE SEPARATOR (FS), GROUP SEPARATOR (GS),
 * RECORD SEPARATOR (RS), and UNIT SEPARATOR (US) are the specific names and
 * are intended mainly for applications where the information separators are
 * used hierarchically. The ascending order is then US, RS, GS, FS.
 * In this case, data normally delimited by a particular separator cannot
 * be split by a higher-order separator but will be considered as delimited by
 * any other higher-order separator.
 * In ISO/IEC 10538, IS3 and IS4 are given the names PAGE TERMINATOR (PT)
 * and DOCUMENT TERMINATOR (DT), respectively and may be used to reset
 * presentation attributes to the default state."
 */
public class BufferedSeparatorInputStream extends BaseChunkStream {

    /**
     * Trick: first separator emitted will be a file separator.
     */
    private char separator = InformationSeparator.FS;

    /**
     * Create a buffered information separator stream.
     * @param in the underlying input stream
     */
    public BufferedSeparatorInputStream(InputStream in) {
        super(in);
    }

    @Override
    public Chunk<byte[], BytesReference> readChunk() throws IOException {
        while (true) {
            end = fillBuf();
            if (end == -1) {
                return null;
            }
            Coordinate c = indexOf(buffer, begin, end);
            if (c.pos != -1) {
                ref.write(buffer, begin, c.pos - begin);
                final char chunkSeparator = separator;
                final BytesReference chunkData = ref.bytes();
                Chunk<byte[], BytesReference> chunk = new Chunk<byte[], BytesReference>() {
                    @Override
                    public byte[] separator() {
                        return new byte[]{ (byte) chunkSeparator };
                    }

                    @Override
                    public BytesReference data() {
                        return chunkData;
                    }

                    @Override
                    public String toString() {
                        return Integer.toHexString(chunkSeparator)
                                + ": " + Arrays.toString(chunkData.toBytes())
                                + ": " + chunkData.toUtf8();
                    }
                };
                processChunk(chunk);
                ref.reset();
                separator = c.sep;
                begin = c.pos + 1;
                return chunk;
            } else {
                ref.write(buffer, begin, buffersize - begin);
                begin = buffersize;
            }
        }
    }

    /**
     * This method can be overriden by classes to extend the processing of a chunk
     * before it is returned to the caller.
     * @param chunk the chunk to be processed.
     * @throws IOException if chunk processing fails
     */
    protected void processChunk(Chunk<byte[], BytesReference> chunk) throws IOException {
        // intentionally left blank
    }

    private Coordinate indexOf(byte[] b, int start, int end) {
        for (int i = start; i < end; i++) {
            if (b[i] == InformationSeparator.US) {
                return new Coordinate(i, InformationSeparator.US);
            } else if (b[i] == InformationSeparator.RS) {
                return new Coordinate(i, InformationSeparator.RS);
            } else if (b[i] == InformationSeparator.GS) {
                return new Coordinate(i, InformationSeparator.GS);
            } else if (b[i] == InformationSeparator.FS) {
                return new Coordinate(i, InformationSeparator.FS);
            }
        }
        return new Coordinate(-1, '\u0000');
    }

    private static class Coordinate {
        int pos;
        char sep;

        Coordinate(int pos, char sep) {
            this.pos = pos;
            this.sep = sep;
        }
    }
}
