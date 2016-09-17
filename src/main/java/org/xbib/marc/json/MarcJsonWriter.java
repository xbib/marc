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
import org.xbib.marc.xml.MarcContentHandler;

import java.io.BufferedWriter;
import java.io.Closeable;
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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This Marc Writer is a MarcContentHandler that writes Marc events to JSON.
 */
public class MarcJsonWriter extends MarcContentHandler implements Flushable, Closeable {

    public static final String LEADER_TAG = "_LEADER";

    public static final String FORMAT_TAG = "_FORMAT";

    public static final String TYPE_TAG = "_TYPE";

    private final Lock lock = new ReentrantLock();

    private final BufferedWriter writer;

    private final StringBuilder sb;

    private Marc.Builder builder;

    private boolean fatalErrors = false;

    private boolean jsonlines;

    private Exception exception;

    public MarcJsonWriter(OutputStream out) throws IOException {
        this(out, false);
    }

    public MarcJsonWriter(OutputStream out, boolean jsonlines) throws IOException {
        this(new OutputStreamWriter(out, StandardCharsets.UTF_8), jsonlines);
    }

    public MarcJsonWriter(Writer writer) throws IOException {
        this(writer, false);
    }

    public MarcJsonWriter(Writer writer, boolean jsonlines) throws IOException {
        this.writer = new BufferedWriter(writer);
        this.sb = new StringBuilder();
        this.jsonlines = jsonlines;
        this.builder = Marc.builder();
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
        super.beginRecord(format, type);
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
        builder.addField(field);
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
        } catch (IOException e) {
            handleException(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void endRecord() {
        super.endRecord();
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
     * @param sb a string builder to append JSON to
     */
    @SuppressWarnings("unchecked")
    private void toJson(MarcRecord marcRecord, StringBuilder sb) {
        if (marcRecord.isEmpty()) {
            return;
        }
        if (recordCounter.get() > 0) {
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
                        if (list2.size() > 1) {
                            sb.append("[");
                        }
                        int c3 = 0;
                        for (Object value2 : list2) {
                            if (c3 > 0) {
                                sb.append(",");
                            }
                            if (value2 instanceof Map) {
                                sb.append("{");
                                Map<String, Object> map = (Map<String, Object>) value2;
                                int c4 = 0;
                                for (Map.Entry<String, Object> subfield : map.entrySet()) {
                                    if (c4 > 0) {
                                        sb.append(",");
                                    }
                                    sb.append("\"").append(subfield.getKey()).append("\":");
                                    if (subfield.getValue() instanceof List) {
                                        sb.append("[");
                                        int c5 = 0;
                                        for (String s : (List<String>)subfield.getValue()) {
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
                                }
                                sb.append("}");
                            } else {
                                sb.append("\"").append(escape(value2.toString())).append("\"");
                            }
                            c3++;
                        }
                        if (list2.size() > 1) {
                            sb.append("]");
                        }
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
            c0++;
        }
        sb.append('}');
        if (jsonlines) {
            sb.append("\n");
        }
    }

    public Exception getException() {
        return exception;
    }

    private static String escape(String value) {
        return value != null ? value.replaceAll("\"", "\\\"") : null;
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
}
