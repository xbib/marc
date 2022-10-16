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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A replacement input stream with a fixed token.
 */
public class FixedTokenReplacementInputStream extends FilterInputStream {

    private final ScanBuffer tokenBuffer;

    private final StreamTokenHandler handler;

    private InputStream value;

    private StreamReadingStrategy strategy;

    private boolean done = false;

    private final StreamReadingStrategy lookingForToken = new StreamReadingStrategy() {
        @Override
        public int internalRead() throws IOException {
            int stream = superRead();
            int buffer = tokenBuffer.append(stream);
            if (tokenBuffer.match()) {
                tokenBuffer.flush();
                value = handler.processToken(tokenBuffer.getScanString());
                strategy = flushingValue;
                return buffer == -1 && stream != -1 ? read() : buffer;
            }
            return buffer == -1 && tokenBuffer.hasData() ? internalRead() : buffer;
        }
    };
    private final StreamReadingStrategy flushingValue = new StreamReadingStrategy() {
        @Override
        public int internalRead() throws IOException {
            int i = value.read();
            if (i == -1) {
                strategy = lookingForToken;
                i = read();
            }
            return i;
        }
    };

    /**
     * Creates a case-sensitive replacement input stream with fixed token.
     * @param in the underlying input stream
     * @param token the token
     * @param handler the stream token handler
     */
    public FixedTokenReplacementInputStream(InputStream in, String token, StreamTokenHandler handler) {
        this(in, token, handler, true);
    }

    /**
     * Creates a replacement input stream with fixed token.
     * @param in the underlying input stream
     * @param token the token
     * @param handler the stream token handler
     * @param caseSensitive true if case sensitive, false if not
     */
    public FixedTokenReplacementInputStream(InputStream in, String token, StreamTokenHandler handler,
                                            boolean caseSensitive) {
        super(in);
        tokenBuffer = new ScanBuffer(token, caseSensitive);
        this.handler = handler;
        strategy = lookingForToken;
    }

    @Override
    public int read() throws IOException {
        return strategy.internalRead();
    }

    private int superRead() throws IOException {
        return super.read();
    }

    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
        int count = 0;
        if (done) {
            return -1;
        }
        for (int i = off, max = off + len; i < max; i++) {
            final int read = read();
            if (read == -1) {
                done = true;
                return count == 0 ? -1 : count;
            }
            bytes[i] = (byte) read;
            count++;
        }
        return count;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * Interface for reading strategy.
     */
    @FunctionalInterface
    interface StreamReadingStrategy {
        /**
         * Read next byte.
         * @return next byte
         * @throws IOException if read fails
         */
        int internalRead() throws IOException;
    }
}
