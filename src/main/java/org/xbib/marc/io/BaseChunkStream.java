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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The base class for streams that work with chunks. The chunks are delimited by
 * information separator characters or by patterns (CR/LF for example).
 */
abstract class BaseChunkStream extends BufferedInputStream implements ChunkStream<byte[], BytesReference> {

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    protected final BytesStreamOutput ref;

    protected byte[] buffer;

    protected int begin;

    protected int end;

    int buffersize;

    /**
     * Create a base chunk stream.
     * @param in the underlying input stream
     * @param buffersize the buffer size, default is 8192
     */
    BaseChunkStream(InputStream in, int buffersize) {
        super(in, buffersize);
        this.buffersize = buffersize;
        this.buffer = new byte[buffersize];
        this.begin = 0;
        this.end = -1;
        this.ref = new BytesStreamOutput();
    }

    /**
     * This methods creates a Java 8 stream of chunks.
     * @return a stream of chunks
     */
    @Override
    public Stream<Chunk<byte[], BytesReference>> chunks() {
        Iterator<Chunk<byte[], BytesReference>> iterator = new Iterator<Chunk<byte[], BytesReference>>() {
            Chunk<byte[], BytesReference> nextData = null;

            @Override
            public boolean hasNext() {
                if (nextData != null) {
                    return true;
                } else {
                    try {
                        nextData = readChunk();
                        return nextData != null;
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }

            @Override
            public Chunk<byte[], BytesReference> next() {
                if (nextData != null || hasNext()) {
                    Chunk<byte[], BytesReference> data = nextData;
                    nextData = null;
                    return data;
                } else {
                    throw new NoSuchElementException();
                }
            }
        };
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator,
                Spliterator.ORDERED | Spliterator.NONNULL), false);
    }

    int fillBuf() throws IOException {
        return fillBuf(buffersize);
    }

    private int fillBuf(int n) throws IOException {
        if (end - begin <= 0) {
            begin = 0;
            return super.read(buffer, begin, n);
        } else {
            return end;
        }
    }
}
