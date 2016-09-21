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
package org.xbib.marc.json;

import org.xbib.marc.Marc;
import org.xbib.marc.MarcField;
import org.xbib.marc.MarcListener;
import org.xbib.marc.MarcRecord;
import org.xbib.marc.label.RecordLabel;
import org.xbib.marc.transformer.value.MarcValueTransformers;
import org.xbib.marc.xml.MarcContentHandler;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileWriter;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This Marc Writer is a MarcContentHandler that writes Marc events to JSON.
 */
public class MarcJsonWriter extends MarcContentHandler implements Flushable, Closeable {

    private static final Logger logger = Logger.getLogger(MarcJsonWriter.class.getName());

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    public static final String LEADER_TAG = "_LEADER";

    public static final String FORMAT_TAG = "_FORMAT";

    public static final String TYPE_TAG = "_TYPE";

    private final Lock lock;

    private final StringBuilder sb;

    private BufferedWriter writer;

    private Marc.Builder builder;

    private boolean fatalErrors;

    private boolean jsonlines;

    private Exception exception;

    private String fileNamePattern;

    private AtomicInteger fileNameCounter;

    private int splitlimit;

    private int bufferSize;

    /**
     * Flag for indicating if writer is at top of file.
     */
    private boolean top;

    public MarcJsonWriter(OutputStream out) throws IOException {
        this(out, false);
    }

    public MarcJsonWriter(OutputStream out, boolean jsonlines) throws IOException {
        this(out, DEFAULT_BUFFER_SIZE, jsonlines);
    }

    public MarcJsonWriter(OutputStream out, int bufferSize, boolean jsonlines) throws IOException {
        this(new OutputStreamWriter(out, StandardCharsets.UTF_8), bufferSize, jsonlines);
    }

    public MarcJsonWriter(Writer writer) throws IOException {
        this(writer, DEFAULT_BUFFER_SIZE, false);
    }

    public MarcJsonWriter(Writer writer, int bufferSize, boolean jsonlines) throws IOException {
        this.writer = new BufferedWriter(writer, bufferSize);
        this.bufferSize = bufferSize;
        this.jsonlines = jsonlines;
        this.lock = new ReentrantLock();
        this.sb = new StringBuilder();
        this.builder = Marc.builder();
        this.top = true;
    }

    public MarcJsonWriter(String fileNamePattern, int splitlimit) throws IOException {
        this(fileNamePattern, DEFAULT_BUFFER_SIZE, splitlimit);
    }

    public MarcJsonWriter(String fileNamePattern, int bufferSize, int splitlimit) throws IOException {
        this.fileNameCounter = new AtomicInteger(0);
        this.fileNamePattern = fileNamePattern;
        this.splitlimit = splitlimit;
        this.writer = newWriter(fileNamePattern, fileNameCounter, bufferSize);
        this.bufferSize = bufferSize;
        this.lock = new ReentrantLock();
        this.sb = new StringBuilder();
        this.builder = Marc.builder();
        this.top = true;
        this.jsonlines = true;
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
        if (jsonlines) {
            return;
        }
        sb.append("[");
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
            toJson(marcRecord, sb);
            writer.write(sb.toString());
            sb.setLength(0);
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
        if (jsonlines) {
            return;
        }
        sb.append("]");
        try {
            writer.write(sb.toString());
        } catch (IOException e) {
            handleException(e);
        }
        sb.setLength(0);
    }

    @Override
    public void endDocument() {
        try {
            writer.flush();
        } catch (IOException e) {
            handleException(e);
        }
    }

    /**
     * Format MARC record as key-oriented JSON.
     *
     * @param sb a string builder to append JSON to
     */
    @SuppressWarnings("unchecked")
    private void toJson(MarcRecord marcRecord, StringBuilder sb) {
        if (marcRecord.isEmpty()) {
            return;
        }
        if (top) {
            top = false;
        } else {
            sb.append(jsonlines ? "\n" : ",");
        }
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
        if (fileNamePattern != null) {
            if (getRecordCounter() % splitlimit == 0) {
                try {
                    endCollection();
                    close();
                    writer = newWriter(fileNamePattern, fileNameCounter, bufferSize);
                    top = true;
                    beginCollection();
                } catch (IOException e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
    }

    private static BufferedWriter newWriter(String fileNamePattern, AtomicInteger fileNameCounter, int bufferSize)
            throws IOException {
        String s = String.format(fileNamePattern, fileNameCounter.getAndIncrement());
        return new BufferedWriter(new FileWriter(s), bufferSize);
    }

    private static final Pattern p = Pattern.compile("\"", Pattern.LITERAL);

    private static final String replacement = "\\\"";

    private static String escape(String value) {
        return p.matcher(value).replaceAll(Matcher.quoteReplacement(replacement));
    }

}
