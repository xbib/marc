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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * A byte array, wrapped in a {@link BytesReference}.
 */
public class BytesArray implements BytesReference {

    private static final String EMPTY_STRING = "";

    private byte[] bytes;

    private int offset;

    private int length;

    /**
     * Create {@link BytesArray} from a byte array.
     * @param bytes the byte array
     */
    public BytesArray(byte[] bytes) {
        this.bytes = bytes;
        this.offset = 0;
        this.length = bytes.length;
    }

    /**
     * Create {@link BytesArray} from a part of a byte array.
     * @param bytes the byte array
     * @param offset the offset
     * @param length the length
     */
    public BytesArray(byte[] bytes, int offset, int length) {
        this.bytes = bytes;
        this.offset = offset;
        this.length = length;
    }


    @Override
    public byte get(int index) {
        return bytes[offset + index];
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public int indexOf(byte b, int offset, int len) {
        if (offset < 0 || (offset + length) > this.length) {
            throw new IllegalArgumentException();
        }
        for (int i = offset; i < offset + len; i++) {
            if (bytes[i] == b) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public BytesReference slice(int from, int length) {
        if (from < 0 || (from + length) > this.length) {
            throw new IllegalArgumentException("can't slice a buffer with length [" + this.length +
                    "], with slice parameters from [" + from + "], length [" + length + "]");
        }
        return new BytesArray(bytes, offset + from, length);
    }

    @Override
    public byte[] toBytes() {
        if (offset == 0 && bytes.length == length) {
            return bytes;
        }
        return Arrays.copyOfRange(bytes, offset, offset + length);
    }

    @Override
    public String toUtf8() {
        if (length == 0) {
            return EMPTY_STRING;
        }
        return new String(bytes, offset, length, StandardCharsets.UTF_8);
    }

    /**
     * Split byte array by a separator byte.
     * @param sep the separator
     * @return a list of byte arrays
     */
    public List<byte[]> split(byte sep) {
        List<byte[]> l = new LinkedList<>();
        int start = 0;
        for (int i = 0; i < bytes.length; i++) {
            if (sep == bytes[i]) {
                byte[] b = Arrays.copyOfRange(bytes, start, i);
                if (b.length > 0) {
                    l.add(b);
                }
                start = i + 1;
                i = start;
            }
        }
        l.add(Arrays.copyOfRange(bytes, start, bytes.length));
        return l;
    }
}
