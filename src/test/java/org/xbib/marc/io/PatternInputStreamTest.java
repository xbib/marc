package org.xbib.marc.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PatternInputStreamTest {

    @Test
    public void testLF() throws IOException {
        byte[] b = "Hello\nWorld".getBytes(StandardCharsets.UTF_8);
        Map<Integer, Integer> map = new LinkedHashMap<>();
        final AtomicInteger count = new AtomicInteger(0);
        PatternInputStream separatorStream = PatternInputStream.lf(new ByteArrayInputStream(b), 1024);
        ChunkListener<byte[], BytesReference> chunkListener =
                (chunk) -> map.put(count.incrementAndGet(), chunk.data().length());
        Chunk<byte[], BytesReference> chunk;
        while ((chunk = separatorStream.readChunk()) != null) {
            chunkListener.chunk(chunk);
        }
        separatorStream.close();
        assertEquals("{1=5, 2=5}", map.toString());
    }

    @Test
    public void testCRLF() throws IOException {
        byte[] b = "Hello\r\nWorld".getBytes(StandardCharsets.UTF_8);
        Map<Integer, Integer> map = new LinkedHashMap<>();
        final AtomicInteger count = new AtomicInteger(0);
        PatternInputStream separatorStream = PatternInputStream.crlf(new ByteArrayInputStream(b), 1024);
        ChunkListener<byte[], BytesReference> chunkListener =
                (chunk) -> map.put(count.incrementAndGet(), chunk.data().length());
        Chunk<byte[], BytesReference> chunk;
        while ((chunk = separatorStream.readChunk()) != null) {
            chunkListener.chunk(chunk);
        }
        separatorStream.close();
        assertEquals("{1=5, 2=5}", map.toString());
    }

    @Test
    public void testCRLFChunkCount() throws IOException {
        byte[][] bytes = {
                "Hello\r\nWorld".getBytes(StandardCharsets.UTF_8),
                "Hello\r\nWorld\r\n".getBytes(StandardCharsets.UTF_8)
        };
        for (byte[] b : bytes) {
            PatternInputStream separatorStream =
                    PatternInputStream.lf(new ByteArrayInputStream(b), 8192);
            long l = separatorStream.chunks().count();
            separatorStream.close();
            assertEquals(2L, l);
        }
    }

    @Test
    public void testLargeCRLFListener() throws IOException {
        BytesStreamOutput output = new BytesStreamOutput();
        for (int i = 0; i < 10000; i++) {
            output.write("Hello\r\nWorld\r\n".getBytes(StandardCharsets.UTF_8));
        }
        Map<Integer, Integer> map = new LinkedHashMap<>();
        final AtomicInteger count = new AtomicInteger(0);
        PatternInputStream separatorStream =
                PatternInputStream.crlf(new ByteArrayInputStream(output.bytes().toBytes()), 8192);
        ChunkListener<byte[], BytesReference> chunkListener =
                (chunk) -> map.put(count.incrementAndGet(), chunk.data().length());
        Chunk<byte[], BytesReference> chunk;
        while ((chunk = separatorStream.readChunk()) != null) {
            chunkListener.chunk(chunk);
        }
        separatorStream.close();
        assertEquals(20000, count.get());
    }

    @Test
    public void testLargeCRLFChunkSize() throws IOException {
        BytesStreamOutput output = new BytesStreamOutput();
        for (int i = 0; i < 10000; i++) {
            output.write("Hello\r\nWorld\r\n".getBytes(StandardCharsets.UTF_8));
        }
        final AtomicInteger count = new AtomicInteger(0);
        PatternInputStream separatorStream =
                PatternInputStream.crlf(new ByteArrayInputStream(output.bytes().toBytes()), 8192);
        separatorStream.chunks().forEach(chunk -> {
            count.incrementAndGet();
            assertEquals(5, chunk.data().length());
            assertTrue(chunk.data().toUtf8().equals("Hello") || chunk.data().toUtf8().equals("World"));
        });
        separatorStream.close();
        assertEquals(20000, count.get());
    }

}
