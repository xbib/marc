package org.xbib.marc.io;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BytesStreamOutputTest {

    /**
     * Try to exercise the byte array allocation.
     *
     * @throws IOException if test fails
     */
    @Test
    public void testBytesStream() throws IOException {
        BytesStreamOutput bytesStreamOutput = new BytesStreamOutput();
        for (int i = 0; i < 1000000; i++) {
            bytesStreamOutput.write("Hello World".getBytes(StandardCharsets.UTF_8));
        }
        assertTrue(bytesStreamOutput.size() > 1000000);
        bytesStreamOutput.close();
    }
}
