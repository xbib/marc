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
package org.xbib.marc.json;

import org.xbib.marc.Marc;
import org.xbib.marc.MarcField;
import org.xbib.marc.MarcListener;
import org.xbib.marc.MarcRecord;
import org.xbib.marc.label.RecordLabel;
import org.xbib.marc.transformer.value.MarcValueTransformers;
import org.xbib.marc.xml.MarcContentHandler;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

/**
 * This Marc Writer is a MarcContentHandler that writes Marc events to JSON.
 */
public class MarcJsonWriter extends MarcContentHandler implements Flushable, Closeable {

    public static final String LEADER_TAG = "_LEADER";

    public static final String FORMAT_TAG = "_FORMAT";

    public static final String TYPE_TAG = "_TYPE";

    private static final Logger logger = Logger.getLogger(MarcJsonWriter.class.getName());

    private static final int DEFAULT_BUFFER_SIZE = 65536;

    private final Lock lock;

    private Writer writer;

    private JsonWriter jsonWriter;

    private Marc.Builder builder;

    private boolean fatalErrors;

    private EnumSet<Style> style;

    private Exception exception;

    private String fileNamePattern;

    private AtomicInteger fileNameCounter;

    private int splitlimit;

    private final int bufferSize;

    private boolean compress;

    private String index;

    private String indexType;
    /**
     * Flag for indicating if writer is at top of file.
     */
    private boolean top;

    public MarcJsonWriter(OutputStream out) {
        this(out, DEFAULT_BUFFER_SIZE);
        this.style = EnumSet.of(Style.ARRAY);
    }

    public MarcJsonWriter(OutputStream out, int bufferSize) {
        this(new OutputStreamWriter(out, StandardCharsets.UTF_8), bufferSize);
    }

    public MarcJsonWriter(Writer writer) {
        this(writer, DEFAULT_BUFFER_SIZE);
        this.style = EnumSet.of(Style.ARRAY);
    }

    public MarcJsonWriter(Writer writer, int bufferSize) {
        this.writer = new BufferedWriter(writer, bufferSize);
        this.jsonWriter = new JsonWriter(this.writer);
        this.bufferSize = bufferSize;
        this.lock = new ReentrantLock();
        this.builder = Marc.builder();
        this.top = true;
    }

    public MarcJsonWriter(String fileNamePattern, int splitlimit) throws IOException {
        this(fileNamePattern, splitlimit, DEFAULT_BUFFER_SIZE, false);
        this.style = EnumSet.of(Style.LINES);
    }

    public MarcJsonWriter(String fileNamePattern,
                          int splitlimit,
                          int bufferSize, boolean compress)
            throws IOException {
        this.fileNameCounter = new AtomicInteger(0);
        this.fileNamePattern = fileNamePattern;
        this.splitlimit = splitlimit;
        this.bufferSize = bufferSize;
        this.lock = new ReentrantLock();
        this.builder = Marc.builder();
        this.top = true;
        this.compress = compress;
        newWriter(fileNamePattern, fileNameCounter, bufferSize, compress);
    }

    public MarcJsonWriter setStyle(EnumSet<Style> style) {
        this.style = style;
        return this;
    }

    public MarcJsonWriter setIndex(String index, String indexType) {
        this.index = index;
        this.indexType = indexType;
        return this;
    }

    public MarcJsonWriter setFatalErrors(boolean fatalErrors) {
        this.fatalErrors = fatalErrors;
        return this;
    }

    @Override
    public MarcJsonWriter setMarcListener(MarcListener listener) {
        super.setMarcListener(listener);
        return this;
    }

    @Override
    public MarcJsonWriter setMarcValueTransformers(MarcValueTransformers marcValueTransformers) {
        super.setMarcValueTransformers(marcValueTransformers);
        return this;
    }

    @Override
    public MarcJsonWriter setFormat(String format) {
        super.setFormat(format);
        builder.setFormat(format);
        return this;
    }

    @Override
    public MarcJsonWriter setType(String type) {
        super.setType(type);
        builder.setType(type);
        return this;
    }

    @Override
    public void startDocument() {
        // nothing to do here
    }

    @Override
    public void beginCollection() {
        if (style.contains(Style.ARRAY)) {
            try {
                jsonWriter.writeArrayOpen();
            } catch (IOException e) {
                handleException(e);
            }
        }
    }

    @Override
    public void beginRecord(String format, String type) {
        setFormat(format);
        setType(type);
    }

    @Override
    public void leader(String label) {
        super.leader(label);
        builder.recordLabel(RecordLabel.builder().from(label.toCharArray()).build());
    }

    @Override
    public void field(MarcField field) {
        super.field(field);
        MarcField marcField = field;
        if (marcValueTransformers != null) {
            marcField = marcValueTransformers.transformValue(field);
        }
        builder.addField(marcField);
    }

    @Override
    public void record(MarcRecord marcRecord) {
        if (exception != null) {
            return;
        }
        // do not call super method in MarcContentHandler, it branches to the field methods and this
        // would confuse us. Plus, we have our own locking here on record level.
        lock.lock();
        try {
            if (style.contains(Style.ALLOW_DUPLICATES)) {
                writeWithDuplicateKeys(marcRecord);
            } else {
                writeUnderlyingMap(marcRecord);
            }
            recordCounter.incrementAndGet();
            afterRecord();
        } catch (Exception e) {
            handleException(new IOException(e));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void endRecord() {
        if (format != null) {
            builder.setFormat(format);
        }
        if (type != null) {
            builder.setType(type);
        }
        record(builder.buildRecord());
        builder = Marc.builder();
    }

    @Override
    public void endCollection() {
        if (style.contains(Style.ARRAY)) {
            try {
                jsonWriter.writeArrayClose();
            } catch (IOException e) {
                handleException(e);
            }
        }
        if (style.contains(Style.ELASTICSEARCH_BULK)) {
            // finish with line-feed "\n", not with System.lineSeparator()
            try {
                writer.write("\n");
            } catch (IOException e) {
                handleException(e);
            }
        }
        try {
            flush();
        } catch (IOException e) {
            handleException(e);
        }
    }

    @Override
    public void endDocument() {
        try {
            flush();
        } catch (IOException e) {
            handleException(e);
        }
    }

    /**
     * Write MARC record using fields, indicators, and subfield structures,
     * therefore allowing duplicate keys in the output.
     * @param marcRecord the MARC record
     * @throws IOException if writing fails
     */
    private void writeWithDuplicateKeys(MarcRecord marcRecord) throws IOException {
        if (marcRecord.isEmpty()) {
            return;
        }
        if (top) {
            top = false;
            if (style.contains(Style.ELASTICSEARCH_BULK)) {
                writeMetaDataLine(marcRecord);
            }
        } else {
            if (style.contains(Style.ARRAY)) {
                jsonWriter.writeArraySeparator();
            } else if (style.contains(Style.LINES)) {
                jsonWriter.writeLiteral("\n");
            } else if (style.contains(Style.ELASTICSEARCH_BULK)) {
                jsonWriter.writeLiteral("\n");
                writeMetaDataLine(marcRecord);
            }
        }
        jsonWriter.writeObjectOpen();
        if (marcRecord.getFormat() != null) {
            jsonWriter.writeMemberName(FORMAT_TAG);
            jsonWriter.writeMemberSeparator();
            jsonWriter.writeString(marcRecord.getFormat());
            jsonWriter.writeObjectSeparator();
        }
        if (marcRecord.getType() != null) {
            jsonWriter.writeMemberName(TYPE_TAG);
            jsonWriter.writeMemberSeparator();
            jsonWriter.writeString(marcRecord.getType());
            jsonWriter.writeObjectSeparator();
        }
        if (!RecordLabel.EMPTY.equals(marcRecord.getRecordLabel())) {
            jsonWriter.writeMemberName(LEADER_TAG);
            jsonWriter.writeMemberSeparator();
            jsonWriter.writeString(marcRecord.getRecordLabel().toString());
            jsonWriter.writeObjectSeparator();
        }
        boolean fieldseparator = false;
        for (MarcField marcField : marcRecord.getFields()) {
            if (fieldseparator) {
                jsonWriter.writeObjectSeparator();
            }
            jsonWriter.writeMemberName(marcField.getTag());
            jsonWriter.writeMemberSeparator();
            if (marcField.isControl()) {
                jsonWriter.writeArrayOpen();
                jsonWriter.writeString(marcField.getValue());
                jsonWriter.writeArrayClose();
            } else {
                jsonWriter.writeObjectOpen();
                jsonWriter.writeMemberName(marcField.getIndicator());
                jsonWriter.writeMemberSeparator();
                jsonWriter.writeArrayOpen();
                boolean subfieldseparator = false;
                for (MarcField.Subfield subfield : marcField.getSubfields()) {
                    if (subfieldseparator) {
                        jsonWriter.writeObjectSeparator();
                    }
                    jsonWriter.writeObjectOpen();
                    jsonWriter.writeMemberName(subfield.getId());
                    jsonWriter.writeMemberSeparator();
                    jsonWriter.writeString(subfield.getValue());
                    jsonWriter.writeObjectClose();
                    subfieldseparator = true;
                }
                jsonWriter.writeArrayClose();
                jsonWriter.writeObjectClose();
            }
            fieldseparator = true;
        }
        jsonWriter.writeObjectClose();
    }

    /**
     * Write MARC record from underlying map as key-oriented JSON. Use repeat maps to create lists.
     * @param marcRecord the MARC record
     * @throws IOException if writing fails
     */
    @SuppressWarnings("unchecked")
    private void writeUnderlyingMap(MarcRecord marcRecord) throws IOException {
        if (marcRecord.isEmpty()) {
            return;
        }
        if (top) {
            top = false;
            if (style.contains(Style.ELASTICSEARCH_BULK)) {
                writeMetaDataLine(marcRecord);
            }
        } else {
            if (style.contains(Style.ARRAY)) {
                writer.write(",");
            } else if (style.contains(Style.LINES)) {
                writer.write("\n");
            } else if (style.contains(Style.ELASTICSEARCH_BULK)) {
                writer.write("\n");
                writeMetaDataLine(marcRecord);
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        int c0 = 0;
        for (Map.Entry<String, Object> tags : marcRecord.entrySet()) {
            if (c0 > 0) {
                sb.append(",");
            }
            String tag = tags.getKey();
            sb.append("\"").append(tag).append("\":");
            Object o = tags.getValue();
            if (o instanceof Map) {
                int c00 = 0;
                Map<String, Object> repeatMap = (Map<String, Object>) o;
                sb.append("[");
                for (Map.Entry<String, Object> repeats : repeatMap.entrySet()) {
                    if (c00 > 0) {
                        sb.append(",");
                    }
                    o = repeats.getValue();
                    if (!(o instanceof List)) {
                        o = Collections.singletonList(o);
                    }
                    List<?> list = (List<?>) o;
                    if (list.size() > 1) {
                        sb.append("[");
                    }
                    int c1 = 0;
                    for (Object value : list) {
                        if (c1 > 0) {
                            sb.append(",");
                        }
                        if (value instanceof Map) {
                            sb.append("{");
                            int c2 = 0;
                            for (Map.Entry<String, Object> indicators : ((Map<String, Object>) value).entrySet()) {
                                if (c2 > 0) {
                                    sb.append(",");
                                }
                                String indicator = indicators.getKey();
                                sb.append("\"").append(indicator).append("\":");
                                o = indicators.getValue();
                                if (!(o instanceof List)) {
                                    o = Collections.singletonList(o);
                                }
                                List<?> list2 = (List<?>) o;
                                sb.append("[");
                                int c3 = 0;
                                for (Object value2 : list2) {
                                    if (c3 > 0) {
                                        sb.append(",");
                                    }
                                    if (value2 instanceof Map) {
                                        Map<String, Object> map = (Map<String, Object>) value2;
                                        int c4 = 0;
                                        for (Map.Entry<String, Object> subfield : map.entrySet()) {
                                            if (c4 > 0) {
                                                sb.append(",");
                                            }
                                            sb.append("{");
                                            sb.append("\"").append(subfield.getKey()).append("\":");
                                            if (subfield.getValue() instanceof List) {
                                                sb.append("[");
                                                int c5 = 0;
                                                for (String s : (List<String>) subfield.getValue()) {
                                                    if (c5 > 0) {
                                                        sb.append(",");
                                                    }
                                                    sb.append("\"").append(escape(s)).append("\"");
                                                    c5++;
                                                }
                                                sb.append("]");
                                            } else {
                                                sb.append("\"").append(escape(subfield.getValue().toString())).append("\"");
                                            }
                                            c4++;
                                            sb.append("}");
                                        }
                                    } else {
                                        sb.append("\"").append(escape(value2.toString())).append("\"");
                                    }
                                    c3++;
                                }
                                sb.append("]");
                                c2++;
                            }
                            sb.append("}");
                        } else {
                            if (value == null) {
                                sb.append("null");
                            } else {
                                sb.append("\"").append(escape(value.toString())).append("\"");
                            }
                        }
                        c1++;
                    }
                    if (list.size() > 1) {
                        sb.append("]");
                    }
                    c00++;
                }
                sb.append("]");
            } else {
                if (o == null) {
                    sb.append("null");
                } else {
                    sb.append("\"").append(escape(o.toString())).append("\"");
                }
            }
            c0++;
        }
        sb.append('}');
        writer.write(sb.toString());
    }

    public Exception getException() {
        return exception;
    }

    public void writeLine() throws IOException {
        writer.write(System.lineSeparator());
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    /**
     * Split records, if configured.
     */
    private void afterRecord() {
        if (fileNamePattern != null && getRecordCounter() % splitlimit == 0) {
            try {
                endCollection();
                close();
                newWriter(fileNamePattern, fileNameCounter, bufferSize, compress);
                top = true;
                beginCollection();
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    private void newWriter(String fileNamePattern, AtomicInteger fileNameCounter,
                           int bufferSize, boolean compress) throws IOException {
        String name = String.format(fileNamePattern, fileNameCounter.getAndIncrement());
        OutputStream out = Files.newOutputStream(Paths.get(name), StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
        writer = new OutputStreamWriter(compress ?
                new CompressedOutputStream(out, bufferSize) :
                new BufferedOutputStream(out, bufferSize), StandardCharsets.UTF_8);
        jsonWriter = new JsonWriter(writer);
    }

    @SuppressWarnings("unchecked")
    private void writeMetaDataLine(MarcRecord marcRecord) {
        String id;
        Object object = marcRecord.get("001");
        // step down to indicator/subfield ID levels if possible, get first value, assuming single field/value in 001
        if (object instanceof Map) {
            object = ((Map<String, Object>) object).values().iterator().next();
        }
        if (object instanceof Map) {
            object = ((Map<String, Object>) object).values().iterator().next();
        }
        id = object.toString();
        if (index != null && indexType != null && id != null) {
            try {
                writer.write("{\"index\":{" +
                        "\"_index\":\"" + index + "\"," +
                        "\"_type\":\"" + indexType + "\"," +
                        "\"_id\":\"" + id + "\"}}" +
                        "\n");
            } catch (IOException e) {
                handleException(e);
            }
        }
    }

    private void handleException(IOException e) {
        exception = e;
        if (fatalErrors) {
            throw new UncheckedIOException(e);
        }
    }

    private static String escape(String value) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x1f) {
                        sb.append("\\u").append(String.format("%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        return sb.toString();
    }

    /**
     *
     */
    public enum Style {
        ARRAY, LINES, ELASTICSEARCH_BULK, ALLOW_DUPLICATES
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
