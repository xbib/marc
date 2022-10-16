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

import java.util.Arrays;

/**
 * A default chunk implementation. The separator is a abyte array,
 * and the data is a {@link BytesReference}.
 */
public class DefaultChunk implements Chunk<byte[], BytesReference> {

    private final byte[] separator;

    private final BytesReference bytesReference;

    /**
     * Create a default chunk.
     * @param separator the separator
     * @param bytesReference the bytes reference for the chunk data
     */
    public DefaultChunk(char separator, BytesReference bytesReference) {
        this.separator = new byte[]{(byte) separator};
        this.bytesReference = bytesReference;
    }

    @Override
    public byte[] separator() {
        return separator;
    }

    @Override
    public BytesReference data() {
        return bytesReference;
    }

    @Override
    public String toString() {
        return Arrays.toString(separator)
                + (bytesReference == null ?
                "" : ": " + Arrays.toString(bytesReference.toBytes()) + ": " + bytesReference.toUtf8());
    }

}
