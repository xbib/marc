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

import org.xbib.marc.io.BytesStreamOutput;
import org.xbib.marc.io.DefaultChunk;
import org.xbib.marc.io.InformationSeparator;
import org.xbib.marc.io.SeparatorOutputStream;
import org.xbib.marc.transformer.value.MarcValueTransformers;
import org.xbib.marc.xml.MarcContentHandler;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

/**
 * An ISO 2709 "stream format" MARC writer.
 */
public class MarcWriter extends MarcContentHandler implements Flushable, Closeable {

    private static final int DEFAULT_BUFFER_SIZE = 65536;

    private final ReentrantLock lock;

    private final BytesStreamOutput bytesStreamOutput;

    private final Charset charset;

    private SeparatorOutputStream out;

    private boolean fatalErrors;

    private Exception exception;

    private String fileNamePattern;

    private AtomicInteger fileNameCounter;

    private int splitlimit;

    private int bufferSize;

    private boolean compress;

    /**
     * Create a MarcWriter on an underlying output stream.
     * @param out the underlying output stream
     * @param charset the character set
     */
    public MarcWriter(OutputStream out, Charset charset) {
        this(out, charset, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Create a MarcWriter on an underlying output stream.
     * @param out the underlying output stream
     * @param charset the character set
     * @param buffersize the buffer size writing to the underlying output stream
     */
    public MarcWriter(OutputStream out, Charset charset, int buffersize) {
        this.out = new SeparatorOutputStream(out, buffersize);
        this.charset = charset;
        this.bytesStreamOutput = new BytesStreamOutput();
        this.lock = new ReentrantLock();
    }

    public MarcWriter(String fileNamePattern, Charset charset, int bufferSize, int splitlimit, boolean compress) throws IOException {
        this.fileNameCounter = new AtomicInteger(0);
        this.fileNamePattern = fileNamePattern;
        this.splitlimit = splitlimit;
        this.bufferSize = bufferSize;
        this.compress = compress;
        this.charset = charset;
        this.bytesStreamOutput = new BytesStreamOutput();
        this.lock = new ReentrantLock();
        newOut(fileNamePattern, fileNameCounter, bufferSize, compress);
    }

    @Override
    public MarcWriter setMarcValueTransformers(MarcValueTransformers marcValueTransformers) {
        super.setMarcValueTransformers(marcValueTransformers);
        return this;
    }

    public MarcWriter setFatalErrors(boolean fatalErrors) {
        this.fatalErrors = fatalErrors;
        return this;
    }

    @Override
    public MarcWriter setMarcListener(MarcListener listener) {
        super.setMarcListener(listener);
        return this;
    }

    @Override
    public void beginRecord(String format, String type) {
        super.beginRecord(format, type);
        if (exception != null) {
            return;
        }
        lock.lock();
    }

    @Override
    public void leader(String label) {
        super.leader(label);
        if (exception != null) {
            return;
        }
        if (label == null) {
            return;
        }
        try {
            bytesStreamOutput.reset();
            bytesStreamOutput.write(label.getBytes(StandardCharsets.ISO_8859_1));
            out.chunk(new DefaultChunk(InformationSeparator.GS, bytesStreamOutput.bytes()));
        } catch (IOException e) {
            handleException(e);
        }
    }

    @Override
    public void field(MarcField field) {
        super.field(field);
        if (exception != null) {
            return;
        }
        try {
            MarcField marcField = marcValueTransformers != null ? marcValueTransformers.transformValue(field) : field;
            bytesStreamOutput.reset();
            // we clean up a bit. Write control field, and fields that are not empty.
            // Do not care about the control field / data field order.
            if (marcField.isControl()) {
                String value = marcField.getValue();
                if (value != null && !value.isEmpty()) {
                    bytesStreamOutput.write(marcField.getTag().getBytes(StandardCharsets.ISO_8859_1));
                    bytesStreamOutput.write(value.getBytes(charset));
                    out.chunk(new DefaultChunk(InformationSeparator.RS, bytesStreamOutput.bytes()));
                }
            } else if (!marcField.isEmpty()) {
                bytesStreamOutput.write(marcField.getTag().getBytes(StandardCharsets.ISO_8859_1));
                bytesStreamOutput.write(marcField.getIndicator().getBytes(StandardCharsets.ISO_8859_1));
                String value = marcField.getValue();
                if (value != null && !value.isEmpty()) {
                    bytesStreamOutput.write(value.getBytes(charset));
                }
                out.chunk(new DefaultChunk(InformationSeparator.RS, bytesStreamOutput.bytes()));
                for (MarcField.Subfield subfield : marcField.getSubfields()) {
                    value = subfield.getValue();
                    if (value != null && !value.isEmpty()) {
                        bytesStreamOutput.reset();
                        bytesStreamOutput.write(subfield.getId().getBytes(StandardCharsets.ISO_8859_1));
                        bytesStreamOutput.write(value.getBytes(charset));
                        out.chunk(new DefaultChunk(InformationSeparator.US, bytesStreamOutput.bytes()));
                    }
                }
            }
        } catch (IOException e) {
            handleException(e);
        }
    }

    @Override
    public void endRecord() {
        super.endRecord();
        try {
            if (exception != null) {
                return;
            }
            // "A record terminator (RT), ASCII control character 1D(hex), is used as the final character
            // of the record, following the field terminator of the last data field."
            // https://www.loc.gov/marc/specifications/specrecstruc.html
            out.chunk(new DefaultChunk(InformationSeparator.GS, null));
            afterRecord();
        } catch (Exception e) {
            handleException(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() throws IOException {
        // not specified in MARC, but we require a file separator as last character of the file.
        out.chunk(new DefaultChunk(InformationSeparator.FS, null));
        out.close();
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    public Exception getException() {
        return exception;
    }

    private void handleException(Exception e) {
        exception = e;
        if (fatalErrors) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void record(MarcRecord marcRecord) {
        if (exception != null) {
            return;
        }
        lock.lock();
        try {
            super.record(marcRecord);
        } catch (Exception e) {
            handleException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Split records if configured.
     */
    private void afterRecord() {
        if (fileNamePattern != null) {
            if (splitlimit != -1) {
                if (getRecordCounter() % splitlimit == 0) {
                    if (out != null) {
                        try {
                            endCollection();
                            endDocument();
                            out.close();
                            newOut(fileNamePattern, fileNameCounter, bufferSize, compress);
                            startDocument();
                            beginCollection();
                        } catch (Exception e) {
                            handleException(e);
                        }
                    }
                }
            }
        }
    }

    private void newOut(String fileNamePattern, AtomicInteger fileNameCounter, int bufferSize, boolean compress)
            throws IOException {
        String name = String.format(fileNamePattern, fileNameCounter.getAndIncrement());
        OutputStream outputStream = Files.newOutputStream(Paths.get(name), StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
        out = new SeparatorOutputStream(compress ? new CompressedOutputStream(outputStream, bufferSize) : outputStream, bufferSize);
    }

    /**
     * A GZIP output stream, modified for best compression.
     */
    private static class CompressedOutputStream extends GZIPOutputStream {

        CompressedOutputStream(OutputStream out, int size) throws IOException {
            super(out, size, true);
            def.setLevel(Deflater.BEST_COMPRESSION);
        }
    }
}
