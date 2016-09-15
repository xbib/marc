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

/**
 * A reference to bytes.
 */
public interface BytesReference {

    /**
     * Returns the byte at the specified index. Need to be between 0 and length.
     *
     * @param index index
     * @return byte at specified index
     */
    byte get(int index);

    /**
     * The length.
     *
     * @return length
     */
    int length();

    /**
     * Find the index of a given byte, in the given area.
     * @param b the byte
     * @param offset offset
     * @param len len
     * @return -1 if not found, otherwise the position, counting from offset
     */
    int indexOf(byte b, int offset, int len);

    /**
     * Slice the bytes from the <tt>from</tt> index up to <tt>length</tt>.
     *
     * @param from   from
     * @param length length
     * @return bytes reference
     */
    BytesReference slice(int from, int length);

    /**
     * Returns the bytes as a single byte array.
     *
     * @return bytes
     */
    byte[] toBytes();

    /**
     * Converts to a string based on utf8.
     *
     * @return UTF-8 encoded string
     */
    String toUtf8();
}
