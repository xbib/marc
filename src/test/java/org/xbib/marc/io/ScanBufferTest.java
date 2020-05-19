package org.xbib.marc.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

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
