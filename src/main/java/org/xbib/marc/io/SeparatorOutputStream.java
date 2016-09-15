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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * A buffered output stream with separators, using the @{link ChunkSink} interface.
 */
public class SeparatorOutputStream extends BufferedOutputStream implements ChunkListener<byte[], BytesReference> {

    public SeparatorOutputStream(OutputStream out) {
        super(out);
    }

    public SeparatorOutputStream(OutputStream out, int buffersize) {
        super(out, buffersize);
    }

    @Override
    public void chunk(Chunk<byte[], BytesReference> chunk) throws IOException {
        Objects.requireNonNull(chunk);
        super.write(chunk.separator());
        if (chunk.data() != null) {
            super.write(chunk.data().toBytes());
        }
    }
}
