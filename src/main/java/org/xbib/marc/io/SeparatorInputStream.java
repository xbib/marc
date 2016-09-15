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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An unbuffered separator stream. This is a very slow implementation.
 * Use this only if {@link BufferedSeparatorInputStream} is not possible.
 */
public class SeparatorInputStream extends FilterInputStream {

    private BytesStreamOutput ref;

    private char separator = InformationSeparator.FS;

    /**
     * Create separator stream.
     * @param in the underlying input stream
     */
    public SeparatorInputStream(InputStream in) {
        super(in);
        this.ref = new BytesStreamOutput();
    }

    /**
     * Read next chunk. This is slow, it uses the {@code read()} method.
     * @return the next chunk
     * @throws IOException if chunk reading fails
     */
    public Chunk<byte[], BytesReference> readChunk() throws IOException {
        while (true) {
            int ch = super.read();
            if (ch == -1) {
                return null;
            }
            if (ch == InformationSeparator.US || ch == InformationSeparator.RS ||
                    ch == InformationSeparator.GS || ch == InformationSeparator.FS) {
                final BytesReference bytesReference = ref.bytes();
                Chunk<byte[], BytesReference> chunk = new Chunk<byte[], BytesReference>() {
                    @Override
                    public byte[] separator() {
                        return new byte[]{(byte) separator };
                    }

                    @Override
                    public BytesReference data() {
                        return bytesReference;
                    }
                };
                separator = (char) ch;
                ref.reset();
                return chunk;
            } else {
                ref.write(ch);
            }
        }
    }

}
