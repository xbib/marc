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
import java.io.OutputStream;
import java.util.Arrays;

/**
 * A growable stream of bytes, with random access methods.
 */
public class BytesStreamOutput extends OutputStream {

    private static final int DEFAULT_BUFFER_SIZE = 1024;

    private static final boolean JRE_IS_64BIT;

    static {
        String oaarch = System.getProperty("os.arch");
        String sunarch = System.getProperty("sun.arch.data.model");
        JRE_IS_64BIT = sunarch != null ? sunarch.contains("64") :
                oaarch != null && oaarch.contains("64");
    }

    /**
     * The buffer where data is stored.
     */
    private byte[] buf;

    /**
     * The number of valid bytes in the buffer.
     */
    private int count;

    /**
     * Create a new {@code BytesStreamOutput} with default buffer size.
     */
    public BytesStreamOutput() {
        this(DEFAULT_BUFFER_SIZE);
    }

    /**
     * Create a new {@code BytesStreamOutput} with given buffer size.
     * @param size size
     */
    public BytesStreamOutput(int size) {
        this.buf = new byte[size];
    }

    /**
     * Return the position in the stream.
     * @return the position
     */
    public long position() {
        return count;
    }

    /**
     * Set to new position in stream. Must be in the current buffer.
     * @param position the new position.
     */
    public void seek(long position) {
        if (position > Integer.MAX_VALUE) {
            throw new UnsupportedOperationException();
        }
        count = (int) position;
    }

    /**
     * Write an integer.
     *
     * @param b int
     * @throws IOException if write fails
     */
    @Override
    public void write(int b) throws IOException {
        int newcount = count + 1;
        if (newcount > buf.length) {
            buf = Arrays.copyOf(buf, oversize(newcount));
        }
        buf[count] = (byte) b;
        count = newcount;
    }

    /**
     * Write byte array.
     *
     * @param b      byte array
     * @param offset offset
     * @param length length
     * @throws IOException if write fails
     */
    @Override
    public void write(byte[] b, int offset, int length) throws IOException {
        if (length == 0) {
            return;
        }
        int newcount = count + length;
        if (newcount > buf.length) {
            buf = Arrays.copyOf(buf, oversize(newcount));
        }
        System.arraycopy(b, offset, buf, count, length);
        count = newcount;
    }

    /**
     * Skip a number of bytes.
     * @param length the number of bytes to skip.
     */
    public void skip(int length) {
        int newcount = count + length;
        if (newcount > buf.length) {
            buf = Arrays.copyOf(buf, oversize(newcount));
        }
        count = newcount;
    }

    /**
     * Seek to absolute position. Must be in buffer.
     * @param pos the position.
     */
    public void seek(int pos) {
        count = pos;
    }

    public void reset() {
        count = 0;
    }

    @Override
    public void flush() throws IOException {
        // nothing to do there
    }

    @Override
    public void close() throws IOException {
        // nothing to do here
    }

    /**
     * Return a {@link BytesReference} to the buffer of this output stream.
     * @return the byets reference
     */
    public BytesReference bytes() {
        return new BytesArray(buf, 0, count);
    }

    /**
     * Returns the current size of the buffer.
     *
     * @return the value of the <code>count</code> field, which is the number
     * of valid bytes in this output stream.
     * @see java.io.ByteArrayOutputStream#count
     */
    public int size() {
        return count;
    }

    /**
     * Returns an array size &gt;= minTargetSize, generally
     * over-allocating exponentially to achieve amortized
     * linear-time cost as the array grows.
     * NOTE: this was originally borrowed from Python 2.4.2
     * listobject.c sources (attribution in LICENSE.txt), but
     * has now been substantially changed based on
     * discussions from java-dev thread with subject "Dynamic
     * array reallocation algorithms", started on Jan 12
     * 2010.
     *
     * @param minTargetSize   Minimum required value to be returned.
     * @return int
     */
    private static int oversize(int minTargetSize) {
        if (minTargetSize < 0) {
            // catch usage that accidentally overflows int
            throw new IllegalArgumentException("invalid array size " + minTargetSize);
        }
        if (minTargetSize == 0) {
            // wait until at least one element is requested
            return 0;
        }
        // asymptotic exponential growth by 1/8th, favors
        // spending a bit more CPU to not tie up too much wasted
        // RAM:
        int extra = minTargetSize >> 3;
        if (extra < 3) {
            // for very small arrays, where constant overhead of
            // realloc is presumably relatively high, we grow
            // faster
            extra = 3;
        }
        int newSize = minTargetSize + extra;
        // add 7 to allow for worst case byte alignment addition below:
        if (newSize + 7 < 0) {
            // int overflowed -- return max allowed array size
            return Integer.MAX_VALUE;
        }
        if (JRE_IS_64BIT) {
            // round up to multiple of 8
            return (newSize + 7) & 0x7ffffff8;
        } else {
            // round up to multiple of 4
            return (newSize + 3) & 0x7ffffffc;
        }
    }
}
