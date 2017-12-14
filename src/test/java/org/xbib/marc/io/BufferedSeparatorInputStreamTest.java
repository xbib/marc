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
import static org.junit.Assert.fail;

import org.junit.Test;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class BufferedSeparatorInputStreamTest {

    private int dataCount = 0;
    private int unitCount = 0;
    private int recordCount = 0;
    private int groupCount = 0;
    private int fileCount = 0;

    private void incDataCount() {
        dataCount++;
    }

    private void incUnitCount() {
        unitCount++;
    }

    private void incRecordCount() {
        recordCount++;
    }

    private void incGroupCount() {
        groupCount++;
    }

    private void incFileCount() {
        fileCount++;
    }

    @Test
    public void testStreamSpearators() throws Exception {

        ChunkListener<byte[], BytesReference> listener = chunk -> {
            byte[] b = chunk.separator();
            char ch = (char) b[0];
            switch (ch) {
                case InformationSeparator.US:
                    incUnitCount();
                    break;
                case InformationSeparator.RS:
                    incRecordCount();
                    break;
                case InformationSeparator.GS:
                    incGroupCount();
                    break;
                case InformationSeparator.FS:
                    incFileCount();
                    break;
            }
            if (chunk.data() != null) {
                incDataCount();
            }
        };
        String s = "sequential.groupstream";
        InputStream in = getClass().getResource(s).openStream();
        BufferedSeparatorInputStream bufferedSeparatorInputStream = new BufferedSeparatorInputStream(in);
        Chunk<byte[], BytesReference> chunk;
        while ((chunk = bufferedSeparatorInputStream.readChunk()) != null) {
            listener.chunk(chunk);
        }
        in.close();
        assertEquals(23, unitCount);
        assertEquals(9, groupCount);
        assertEquals(389, dataCount);
        assertEquals(356, recordCount);
        assertEquals(1, fileCount);
    }

    @Test
    public void testGroupStream() throws Exception {
        String s = "sequential.groupstream";
        InputStream in = getClass().getResource(s).openStream();
        final AtomicInteger count = new AtomicInteger(0);
        BufferedSeparatorInputStream bufferedSeparatorInputStream = new BufferedSeparatorInputStream(in);
        ChunkListener<byte[], BytesReference> chunkListener = (chunk) -> count.incrementAndGet();
        Chunk<byte[], BytesReference> chunk;
        while ((chunk = bufferedSeparatorInputStream.readChunk()) != null) {
            chunkListener.chunk(chunk);
        }
        in.close();
        assertEquals(389, count.get());
    }

    @Test
    public void testSeparatorStream() throws Exception {
        String s = "periouni.mrc";
        Map<Integer, Integer> map2 = new LinkedHashMap<>();
        InputStream in2 = getClass().getResource("/org/xbib/marc/dialects/unimarc/" + s).openStream();
        final AtomicInteger count2 = new AtomicInteger(0);
        BufferedSeparatorInputStream bufferedSeparatorInputStream = new BufferedSeparatorInputStream(in2);
        ChunkListener<byte[], BytesReference> chunkListener2 =
                (chunk2) -> map2.put(count2.incrementAndGet(), chunk2.data().length());
        Chunk<byte[], BytesReference> chunk2;
        while ((chunk2 = bufferedSeparatorInputStream.readChunk()) != null) {
            chunkListener2.chunk(chunk2);
        }
        in2.close();

        // slow SeparatorStream
        Map<Integer, Integer> map = new LinkedHashMap<>();
        InputStream in = getClass().getResource("/org/xbib/marc/dialects/unimarc/" + s).openStream();
        final AtomicInteger count = new AtomicInteger(0);
        SeparatorInputStream stream = new SeparatorInputStream(in);
        ChunkListener<byte[], BytesReference> chunkListener =
                (chunk) -> map.put(count.incrementAndGet(), chunk.data().length());
        Chunk<byte[], BytesReference> chunk;
        while ((chunk = stream.readChunk()) != null) {
            chunkListener.chunk(chunk);
        }
        in.close();

        assertEquals(map.size(), map2.size());
        for (int i = 1; i <= map.size(); i++) {
            if (!map.get(i).equals(map2.get(i))) {
                fail("diff: " + i + " " + map.get(i) + " != " + map2.get(i));
            }
        }
    }

    @Test
    public void testChunkCount() throws Exception {
        String s = "periouni.mrc";
        InputStream in = getClass().getResource("/org/xbib/marc/dialects/unimarc/" + s).openStream();
        BufferedSeparatorInputStream bufferedSeparatorInputStream = new BufferedSeparatorInputStream(in);
        long l = bufferedSeparatorInputStream.chunks().count();
        assertEquals(192247, l);
    }

    @Test
    public void testMARC() throws Exception {
        String s = "summerland.mrc";
        Map<Integer, Integer> map = new LinkedHashMap<>();
        InputStream in = getClass().getResource("/org/xbib/marc/" + s).openStream();
        final AtomicInteger count = new AtomicInteger(0);
        BufferedSeparatorInputStream bufferedSeparatorInputStream = new BufferedSeparatorInputStream(in);
        ChunkListener<byte[], BytesReference> chunkListener =
                (chunk) -> map.put(count.incrementAndGet(), chunk.data().length());
        Chunk<byte[], BytesReference> chunk;
        while ((chunk = bufferedSeparatorInputStream.readChunk()) != null) {
            chunkListener.chunk(chunk);
        }
        assertEquals("{1=204, 2=8, 3=16, 4=40, 5=2, 6=11, 7=2, 8=18, 9=2, 10=4, 11=4, 12=4, 13=2, 14=17, 15=2, "
                + "16=13, 17=16, 18=2, 19=8, 20=2, 21=11, 22=43, 23=7, 24=2, 25=9, 26=7, 27=2, 28=171, 29=2, 30=9, "
                + "31=2, 32=9, 33=9, 34=2, 35=6, 36=9, 37=0}", map.toString());
        in.close();
    }

}
