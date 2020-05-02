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

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * A buffered input stream that is organized in chunks separated by byte array patterns.
 * Convenience implements are give by {@code PatternInputStream.lf()} for line-feed separated
 * streams, and {@code PatternInputStream.CRLF} for carriage-rturn/line-feed separated streams.
 */
public class PatternInputStream extends BaseChunkStream {

    private static final byte[] LF = {'\n'};

    private static final byte[] CRLF = {'\r', '\n'};

    private final byte[] pattern;

    /**
     * Create a pattern delimited input stream.
     * @param in the underlying input stream
     * @param pattern the pattern
     * @param bufferSize buffer size
     */
    public PatternInputStream(InputStream in, byte[] pattern, int bufferSize) {
        super(in, bufferSize);
        requireNonNull(pattern);
        this.pattern = pattern.clone();
    }

    /**
     * Convenience method to cerate a line-feed pattern separated input stream.
     * @param in the input stream to wrap
     * @param bufferSize buffer size
     * @return the pattern input stream
     */
    public static PatternInputStream lf(InputStream in, int bufferSize) {
        return new PatternInputStream(in, LF, bufferSize);
    }

    /**
     * Convenience method to cerate a carriage-return/line-feed pattern separated input stream.
     * @param in the input stream to wrap
     * @param bufferSize buffer size
     * @return the pattern input stream
     */
    public static PatternInputStream crlf(InputStream in, int bufferSize) {
        return new PatternInputStream(in, CRLF, bufferSize);
    }

    /**
     * Read next chunk from this stream.
     * @return a chunk
     * @throws IOException if chunk can not be read
     */
    @Override
    public Chunk<byte[], BytesReference> readChunk() throws IOException {
        Chunk<byte[], BytesReference> chunk = internalReadChunk();
        if (chunk != null) {
            processChunk(chunk);
        }
        return chunk;
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

    private Chunk<byte[], BytesReference> internalReadChunk() throws IOException {
        int matches = 0;
        while (true) {
            end = fillBuf();
            if (end == -1) {
                if (ref.size() > 0) {
                    // return last read chunk
                    final BytesReference bytesReference = ref.bytes();
                    ref.reset();
                    return new Chunk<byte[], BytesReference>() {
                        @Override
                        public byte[] separator() {
                            return pattern;
                        }

                        @Override
                        public BytesReference data() {
                            return bytesReference;
                        }

                        @Override
                        public String toString() {
                            return Arrays.toString(separator())
                                    + ": " + Arrays.toString(bytesReference.toBytes())
                                    + ": " + bytesReference.toUtf8();
                        }

                    };
                } else {
                    return null;
                }
            }
            for (int i = begin; i < end; i++) {
                if (buffer[i] == pattern[matches]) {
                    matches++;
                } else {
                    matches = 0;
                }
                if (matches == pattern.length) {
                    int len = i - begin - pattern.length + 1;
                    if (len < 0) {
                        ref.skip(len);
                    } else {
                        ref.write(buffer, begin, len);
                    }
                    final BytesReference bytesReference = ref.bytes();
                    Chunk<byte[], BytesReference> chunk = new Chunk<byte[], BytesReference>() {
                        @Override
                        public byte[] separator() {
                            return pattern;
                        }

                        @Override
                        public BytesReference data() {
                            return bytesReference;
                        }

                        @Override
                        public String toString() {
                            return Arrays.toString(separator())
                                    + ": " + Arrays.toString(bytesReference.toBytes())
                                    + ": " + bytesReference.toUtf8();
                        }
                    };
                    ref.reset();
                    begin = i + 1;
                    return chunk;
                }
            }
            ref.write(buffer, begin, end - begin);
            begin = end;
        }
    }
}
