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
package org.xbib.marc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xmlunit.matchers.CompareMatcher;

public class StreamMatcher {

    private static final Logger logger = Logger.getLogger(StreamMatcher.class.getName());

    public interface Producer {
        void produce(InputStream inputStream, OutputStream outputStream) throws IOException;
    }

    public static void xmlMatch(Class<?> cl, String resourceName, String suffix, Producer producer) throws IOException {
        Path path = Files.createTempFile(resourceName, suffix);
        try (InputStream inputStream = cl.getResource(resourceName).openStream();
             OutputStream outputStream = Files.newOutputStream(path)) {
            producer.produce(inputStream, outputStream);
        } finally {
            assertThat(path, CompareMatcher.isIdenticalTo(cl.getResource(resourceName + suffix).openStream()));
            Files.delete(path);
        }
    }

    public static void fileMatch(Class<?> cl, String resourceName, String suffix, Producer producer) throws IOException {
        Path path = Files.createTempFile(resourceName, suffix);
        logger.log(Level.INFO, "path = " + resourceName);
        try (InputStream inputStream = cl.getResource(resourceName).openStream();
             OutputStream outputStream = Files.newOutputStream(path)) {
            producer.produce(inputStream, outputStream);
        } finally {
            logger.log(Level.INFO, "path = " + resourceName + suffix);
            assertStream(resourceName, path,  cl.getResource(resourceName + suffix).openStream());
            Files.delete(path);
        }
    }

    public static void roundtrip(Class<?> cl, String resourceName, String suffix,
                                 Producer producer1, Producer producer2) throws IOException {
        logger.log(Level.INFO, "original = " + new String(cl.getResource(resourceName).openStream().readAllBytes(), StandardCharsets.US_ASCII));
        Path path1 = Files.createTempFile(resourceName, suffix);
        Path path2 = Files.createTempFile(resourceName, suffix + ".orig");
        try (InputStream inputStream1 = cl.getResource(resourceName).openStream();
             OutputStream outputStream1 = Files.newOutputStream(path1)) {
            producer1.produce(inputStream1, outputStream1);
            logger.log(Level.INFO, "step 1: produced " + Files.readString(path1));
            logger.log(Level.INFO, "step 1: asserting XML");
            assertThat("step 1 " + path1, path1, CompareMatcher.isIdenticalTo(cl.getResource(resourceName + suffix).openStream()));
            logger.log(Level.INFO, "step 1: success");
            try (InputStream inputStream2 = Files.newInputStream(path1);
                OutputStream outputStream2 = Files.newOutputStream(path2)) {
                producer2.produce(inputStream2, outputStream2);
                logger.log(Level.INFO, "step 2: produced " + Files.readString(path2));
                logger.log(Level.INFO, "step 2: asserting");
                assertStream("step 2 " + path2, path2, cl.getResource(resourceName).openStream());
                logger.log(Level.INFO, "step 2: success");
            } finally {
                Files.delete(path2);
            }
        } finally {
            Files.delete(path1);
        }
    }

    public static void generate(Class<?> cl, String resourceName, String suffix, Producer producer) throws IOException {
        Path path = Paths.get("src/test/resources", cl.getPackageName().replace('.', '/'), resourceName + suffix);
        logger.log(Level.INFO, "path = " + path);
        URL url = cl.getResource(resourceName);
        if (url != null) {
            try (InputStream inputStream = url.openStream();
                 OutputStream outputStream = Files.newOutputStream(path)) {
                producer.produce(inputStream, outputStream);
            }
        }
    }

    public static void assertStream(String name, Path path1, Path path2) throws IOException {
        assertStream(name, Files.newInputStream(path1), Files.newInputStream(path2));
    }

    public static void assertStream(String name, Path path, InputStream expected) throws IOException {
        assertStream(name, expected, Files.newInputStream(path));
    }

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
