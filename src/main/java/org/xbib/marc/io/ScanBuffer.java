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

/**
 * Scan buffer.
 */
public class ScanBuffer {

    private boolean cs;

    private char[] buffer = new char[0];

    private int[] buffer2 = new int[0];

    private char[] token = new char[0];

    private int pos;

    /**
     * Create scan buffer.
     * @param size the size
     */
    public ScanBuffer(int size) {
        buffer = new char[size];
        buffer2 = new int[size];
        token = new char[size];
        flush();
        cs = true;
    }

    /**
     * Create scan buffer.
     * @param scanString the scan string
     * @param caseSensitive true if case sensitive, false if not
     */
    public ScanBuffer(String scanString, boolean caseSensitive) {
        this(scanString.length());
        setScanString(scanString, caseSensitive);
    }

    /**
     * Return scan buffer size.
     * @return scan buffer size
     */
    public int size() {
        return buffer.length;
    }

    /**
     * Reset scan buffer position.
     */
    public void resetPosition() {
        pos = 0;
    }

    /**
     * Get scan string.
     * @return scan string
     */
    public String getScanString() {
        return new String(token);
    }

    public void setScanString(String stringToken, boolean caseSensitive) {
        cs = caseSensitive;
        token = new char[stringToken.length()];
        stringToken.getChars(0, token.length, token, 0);
        if (token.length > buffer.length) {
            buffer = new char[token.length * 4];
            buffer2 = new int[token.length * 4];
        }
        pos = 0;
        if (!cs) {
            for (int i = 0; i < token.length; i++) {
                token[i] = Character.toLowerCase(token[i]);
            }
        }
        flush();
    }

    /**
     * Append byte.
     * @param newByte the byte
     * @return old byte
     */
    public int append(int newByte) {
        int old = buffer2[pos];
        buffer2[pos] = newByte;
        buffer[pos] = cs ? (char) newByte : Character.toLowerCase((char) newByte);
        pos = (++pos < buffer.length) ? pos : 0;
        return old;
    }

    public void flush() {
        char ch = (char) -1;
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = ch;
            buffer2[i] = -1;
        }
        resetPosition();
    }

    public boolean match() {
        int apos = token.length - 1;
        int rpos = pos - 1;

        for (; rpos > -1 && apos > -1; rpos--, apos--) {
            if (buffer[rpos] != token[apos]) {
                return false;
            }
        }
        for (rpos = buffer.length - 1; apos > -1; rpos--, apos--) {
            if (buffer[rpos] != token[apos]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Has scan buffer any data?
     * @return true if it has data
     */
    public boolean hasData() {
        int apos = token.length - 1;
        int rpos = pos - 1;
        for (; rpos > -1 && apos > -1; rpos--, apos--) {
            if (buffer2[rpos] != -1) {
                return true;
            }
        }
        for (rpos = buffer2.length - 1; apos > -1; rpos--, apos--) {
            if (buffer2[rpos] != -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Clear scan buffer at position.
     * @param i the position
     */
    public void clear(int i) {
        char ch = (char) -1;
        int apos = i - 1;
        int rpos = pos - 1;
        for (; rpos > -1 && apos > -1; rpos--, apos--) {
            buffer[rpos] = ch;
            buffer2[rpos] = -1;
        }
        for (rpos = buffer.length - 1; apos > -1; rpos--, apos--) {
            buffer[rpos] = ch;
            buffer2[rpos] = -1;
        }
    }

    public byte[] getBuffer() {
        byte[] out = new byte[getSize()];
        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) getByte(buffer.length - out.length + i);
        }
        return out;
    }

    private int getSize() {
        int size = 0;
        for (int i = buffer.length - 1; i >= 0; i--) {
            int b = getByte(i);
            if (b != -1) {
                size++;
            } else {
                break;
            }
        }
        return size;
    }

    private int getByte(int absolutePosition) {
        if (absolutePosition >= buffer.length) {
            throw new IndexOutOfBoundsException();
        }
        int realPosition = (pos + absolutePosition) % buffer.length;
        return buffer2[realPosition];
    }
}
