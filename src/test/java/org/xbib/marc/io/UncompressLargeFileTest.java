package org.xbib.marc.io;

import org.junit.Ignore;
import org.junit.Test;
import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.marc.Record;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcRecord;
import org.xbib.marc.MarcRecordListener;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

@Ignore
public class UncompressLargeFileTest {

    private static final Logger logger = Logger.getLogger(UncompressLargeFileTest.class.getName());

    @Test
    public void uncompress() throws IOException {
        InputStream inputStream = Files.newInputStream(Paths.get("/data/zdb/baseline/zdb_dnbmarc_20200309.mrc.gz"));
        GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream, 1024 * 1024);
        byte[] buffer = new byte[1024 * 1024];
        int length;
        while ((length = gzipInputStream.read(buffer)) != -1) {
            // do nothing
        }
        gzipInputStream.close();
        inputStream.close();
    }

    @Test
    public void uncompressAndDecodeChunks() throws Exception {
        logger.log(Level.INFO, "start decoding chunks");
        InputStream inputStream = Files.newInputStream(Paths.get("/data/zdb/baseline/zdb_dnbmarc_20200309.mrc.gz"));
        GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream, 1024 * 1024);
        final AtomicInteger counter = new AtomicInteger(0);
        BufferedSeparatorInputStream bufferedSeparatorInputStream =
                new BufferedSeparatorInputStream(gzipInputStream, 1024 * 1024);
        ChunkListener<byte[], BytesReference> chunkListener = (chunk) -> counter.incrementAndGet();
        Chunk<byte[], BytesReference> chunk;
        while ((chunk = bufferedSeparatorInputStream.readChunk()) != null) {
            chunkListener.chunk(chunk);
        }
        gzipInputStream.close();
        logger.log(Level.INFO, "stop decoding chunks, counter = " + counter.get());
    }

    @Test
    public void uncompressAndDecodeMarcRecords() throws IOException {
        logger.log(Level.INFO, "start decoding MARC");
        InputStream inputStream = Files.newInputStream(Paths.get("/data/zdb/baseline/zdb_dnbmarc_20200309.mrc.gz"));
        GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream, 1024 * 1024);
        final AtomicInteger counter = new AtomicInteger(0);
        Marc.builder()
                .setInputStream(gzipInputStream)
                .setMarcRecordListener(new MarcRecordListener() {
                    @Override
                    public void beginCollection() {
                    }

                    @Override
                    public void record(MarcRecord marcRecord) {
                        counter.incrementAndGet();
                    }

                    @Override
                    public void endCollection() {
                    }
                })
                .setCharset(StandardCharsets.UTF_8)
                .build()
                .writeRecords(1024 * 1024);
        gzipInputStream.close();
        inputStream.close();
        logger.log(Level.INFO, "stop deocding MARC, counter = " + counter.get());
    }

    @Test
    public void uncompressAndDecodeWithMarc4j() throws Exception {
        logger.log(Level.INFO, "start decoding MARC4J");
        InputStream inputStream = Files.newInputStream(Paths.get("/data/zdb/baseline/zdb_dnbmarc_20200309.mrc.gz"));
        GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream, 1024 * 1024);
        final AtomicInteger counter = new AtomicInteger(0);
        MarcReader reader = new MarcPermissiveStreamReader(gzipInputStream, true, true);
        while (reader.hasNext()) {
            Record record = reader.next();
            counter.incrementAndGet();
            // do nothing
        }
        gzipInputStream.close();
        inputStream.close();
        logger.log(Level.INFO, "stop deocding MARC4J, counter = " + counter.get());
    }
}
