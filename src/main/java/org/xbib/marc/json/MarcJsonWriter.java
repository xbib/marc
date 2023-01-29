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

    private JsonBuilder jsonBuilder;

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
        this.jsonBuilder = new JsonBuilder(this.writer);
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

    public JsonBuilder getJsonBuilder() {
        return jsonBuilder;
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
        if (style.contains(Style.EMBEDDED_RECORD)) {
            try {
                jsonBuilder.beginMap();
            } catch (IOException e) {
                handleException(e);
            }
        }
    }

    @Override
    public void beginCollection() {
        if (style.contains(Style.ARRAY)) {
            try {
                jsonBuilder.beginCollection();
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
    public void leader(RecordLabel label) {
        super.leader(label);
        builder.recordLabel(label);
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
                jsonBuilder.endCollection();
            } catch (IOException e) {
                handleException(e);
            }
        }
        if (style.contains(Style.ELASTICSEARCH_BULK)) {
            // finish with line-feed "\n", not with System.lineSeparator() !!!
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
        if (style.contains(Style.EMBEDDED_RECORD)) {
            try {
                jsonBuilder.endMap();
            } catch (IOException e) {
                handleException(e);
            }
        } else {
            try {
                flush();
            } catch (IOException e) {
                handleException(e);
            }
        }
    }

    public void write(Map<String, Object> map) {

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
                writer.write(",");
            } else if (style.contains(Style.LINES)) {
                writer.append(System.lineSeparator());
            } else if (style.contains(Style.ELASTICSEARCH_BULK)) {
                writer.append(System.lineSeparator());
                writeMetaDataLine(marcRecord);
            }
        }
        if (!style.contains(Style.EMBEDDED_RECORD)) {
            jsonBuilder.beginMap();
        }
        if (marcRecord.getFormat() != null) {
            jsonBuilder.buildKey(FORMAT_TAG).buildValue(marcRecord.getFormat());
        }
        if (marcRecord.getType() != null) {
            jsonBuilder.buildKey(TYPE_TAG).buildValue(marcRecord.getType());
        }
        if (!RecordLabel.EMPTY.equals(marcRecord.getRecordLabel())) {
            jsonBuilder.buildKey(LEADER_TAG).buildValue(marcRecord.getRecordLabel().toString());
        }
        for (MarcField marcField : marcRecord.getFields()) {
            jsonBuilder.buildKey(marcField.getTag());
            if (marcField.isControl()) {
                jsonBuilder.buildValue(marcField.recoverControlFieldValue());
            } else {
                jsonBuilder.beginMap();
                jsonBuilder.buildKey(marcField.getIndicator());
                jsonBuilder.beginCollection();
                for (MarcField.Subfield subfield : marcField.getSubfields()) {
                    jsonBuilder.beginMap();
                    jsonBuilder.buildKey(subfield.getId());
                    jsonBuilder.buildValue(subfield.getValue());
                    jsonBuilder.endMap();
                }
                jsonBuilder.endCollection();
                jsonBuilder.endMap();
            }
        }
        if (!style.contains(Style.EMBEDDED_RECORD)) {
            jsonBuilder.endMap();
        }
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
                writer.write(System.lineSeparator());
            } else if (style.contains(Style.ELASTICSEARCH_BULK)) {
                writer.write(System.lineSeparator());
                writeMetaDataLine(marcRecord);
            }
        }
        StringBuilder sb = new StringBuilder();
        if (!style.contains(Style.EMBEDDED_RECORD)) {
            sb.append("{");
        }
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
                                                    if (s != null) {
                                                        sb.append("\"").append(escape(s)).append("\"");
                                                    } else {
                                                        sb.append("null");
                                                    }
                                                    c5++;
                                                }
                                                sb.append("]");
                                            } else {
                                                Object object = subfield.getValue();
                                                if (object != null) {
                                                    sb.append("\"").append(escape(object.toString())).append("\"");
                                                } else {
                                                    sb.append("null");
                                                }
                                            }
                                            c4++;
                                            sb.append("}");
                                        }
                                    } else {
                                        if (value2 != null) {
                                            sb.append("\"").append(escape(value2.toString())).append("\"");
                                        } else {
                                            sb.append("null");
                                        }
                                    }
                                    c3++;
                                }
                                sb.append("]");
                                c2++;
                            }
                            sb.append("}");
                        } else {
                            if (value != null) {
                                sb.append("\"").append(escape(value.toString())).append("\"");
                            } else {
                                sb.append("null");
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
                if (o != null) {
                    sb.append("\"").append(escape(o.toString())).append("\"");
                } else {
                    sb.append("null");
                }
            }
            c0++;
        }
        if (!style.contains(Style.EMBEDDED_RECORD)) {
            sb.append('}');
        }
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
        //jsonWriter = new JsonWriter(writer);
        jsonBuilder = new JsonBuilder(writer);
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
                        System.lineSeparator());
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
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x1f) {
                        sb.append("\\u").append(String.format("%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     *
     */
    public enum Style {
        ARRAY,
        LINES,
        ELASTICSEARCH_BULK,
        ALLOW_DUPLICATES,
        EMBEDDED_RECORD
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
