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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 *
 */
public class ScanBufferTest {

    @Test
    public void testScanBuffer() {
        ScanBuffer scanBuffer = new ScanBuffer(10);
        assertEquals("", new String(scanBuffer.getBuffer()));
        scanBuffer.append('0');
        assertEquals("0", new String(scanBuffer.getBuffer()));
        scanBuffer.append('1');
        assertEquals("01", new String(scanBuffer.getBuffer()));
        scanBuffer.append('2');
        assertEquals("012", new String(scanBuffer.getBuffer()));
        scanBuffer.append('3');
        assertEquals("0123", new String(scanBuffer.getBuffer()));
        scanBuffer.append('4');
        assertEquals("01234", new String(scanBuffer.getBuffer()));
        scanBuffer.append('5');
        assertEquals("012345", new String(scanBuffer.getBuffer()));
        scanBuffer.append('6');
        assertEquals("0123456", new String(scanBuffer.getBuffer()));
        scanBuffer.append('7');
        assertEquals("01234567", new String(scanBuffer.getBuffer()));
        scanBuffer.append('8');
        assertEquals("012345678", new String(scanBuffer.getBuffer()));
        scanBuffer.append('9');
        assertEquals("0123456789", new String(scanBuffer.getBuffer()));
        scanBuffer.append('a');
        assertEquals("123456789a", new String(scanBuffer.getBuffer()));
        scanBuffer.append('b');
        assertEquals("23456789ab", new String(scanBuffer.getBuffer()));
        scanBuffer.append('c');
        assertEquals("3456789abc", new String(scanBuffer.getBuffer()));
        scanBuffer.append('d');
        assertEquals("456789abcd", new String(scanBuffer.getBuffer()));
        scanBuffer.append('e');
        assertEquals("56789abcde", new String(scanBuffer.getBuffer()));
        scanBuffer.append('f');
        assertEquals("6789abcdef", new String(scanBuffer.getBuffer()));
        scanBuffer.append('g');
        assertEquals("789abcdefg", new String(scanBuffer.getBuffer()));
        scanBuffer.append('h');
        assertEquals("89abcdefgh", new String(scanBuffer.getBuffer()));
        scanBuffer.append('i');
        assertEquals("9abcdefghi", new String(scanBuffer.getBuffer()));
        scanBuffer.append('j');
        assertEquals("abcdefghij", new String(scanBuffer.getBuffer()));
        scanBuffer.append('0');
        assertEquals("bcdefghij0", new String(scanBuffer.getBuffer()));
        scanBuffer.append('1');
        assertEquals("cdefghij01", new String(scanBuffer.getBuffer()));
        scanBuffer.append('2');
        assertEquals("defghij012", new String(scanBuffer.getBuffer()));
        scanBuffer.append('3');
        assertEquals("efghij0123", new String(scanBuffer.getBuffer()));
    }
}
