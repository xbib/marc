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
package org.xbib.marc;

import static org.junit.jupiter.api.Assertions.fail;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;

/**
 *
 */
public class StreamMatcher {

    public static void assertStream(String name, InputStream expected, String actual) throws IOException {
        assertStream(name, expected, new ByteArrayInputStream(actual.getBytes(StandardCharsets.UTF_8)));
    }

    public static void assertStream(String name, InputStream expected, InputStream actual) throws IOException {
        int offset = 0;
        try (ReadableByteChannel ch1 = Channels.newChannel(expected);
             ReadableByteChannel ch2 = Channels.newChannel(actual)) {
            ByteBuffer buf1 = ByteBuffer.allocateDirect(4096);
            ByteBuffer buf2 = ByteBuffer.allocateDirect(4096);
            while (true) {
                int n1 = ch1.read(buf1);
                int n2 = ch2.read(buf2);
                if (n1 == -1 || n2 == -1) {
                    if (n1 != n2) {
                        fail(name + ": stream length mismatch: " + n1 + " != " + n2 + " offset=" + offset);
                    } else {
                        return;
                    }
                }
                buf1.flip();
                buf2.flip();
                for (int i = 0; i < Math.min(n1, n2); i++) {
                    int b1 = buf1.get() & 0xFF;
                    int b2 = buf2.get() & 0xFF;
                    if (b1 != b2) {
                        fail(name + ": mismatch at offset " + (offset + i)
                                + " (" + Integer.toHexString(b1)
                                + " != " + Integer.toHexString(b2) + ")"
                        );
                    }
                }
                buf1.compact();
                buf2.compact();
                offset += Math.min(n1, n2);
            }
        }
    }
}
