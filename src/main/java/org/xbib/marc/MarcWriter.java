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
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An ISO 2709 "stream format" MARC writer.
 */
public class MarcWriter extends MarcContentHandler implements Flushable, Closeable {

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    private final ReentrantLock lock = new ReentrantLock(true);

    private final BytesStreamOutput bytesStreamOutput;

    private final SeparatorOutputStream out;

    private final Charset charset;

    private boolean fatalErrors;

    private Exception exception;

    /**
     * Create a MarcWriter on an underlying output stream.
     * @param out the underlying output stream
     * @param charset the character set
     * @throws IOException if writer can not be created
     */
    public MarcWriter(OutputStream out, Charset charset) {
        this(out, charset, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Create a MarcWriter on an underlying output stream.
     * @param out the underlying output stream
     * @param charset the character set
     * @param buffersize the buffer size writing to the underlying output stream
     * @throws IOException if writer can not be created
     */
    public MarcWriter(OutputStream out, Charset charset, int buffersize) {
        this.out = new SeparatorOutputStream(out, buffersize);
        this.charset = charset;
        this.bytesStreamOutput = new BytesStreamOutput();
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
        } catch (IOException e) {
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

    private void handleException(IOException e) {
        exception = e;
        if (fatalErrors) {
            throw new UncheckedIOException(e);
        }
    }
}
