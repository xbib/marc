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

import org.xbib.marc.MarcField;
import org.xbib.marc.MarcListener;
import org.xbib.marc.MarcRecord;
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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This Marc Writer is a MarcContentHandler that writes Marc events to JSON.
 */
public class MarcJsonWriter extends MarcContentHandler implements Flushable, Closeable {

    public static final String LEADER_TAG = "_LEADER";

    public static final String FORMAT_TAG = "_FORMAT";

    public static final String TYPE_TAG = "_TYPE";

    private static final String JSON_1 = "\":\"";

    private final Lock lock = new ReentrantLock();

    private final BufferedWriter writer;

    private final StringBuilder sb;

    private boolean fatalErrors = false;

    private boolean jsonlines;

    private int fieldCount;

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
        this.fieldCount = 0;
        this.jsonlines = jsonlines;
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
        return this;
    }

    @Override
    public MarcJsonWriter setType(String type) {
        super.setType(type);
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
        if (recordCounter.get() > 0) {
            sb.append(jsonlines ? "\n" : ",");
        }
        sb.append("{");
        String s = format != null ? format : this.format;
        sb.append("\"").append(FORMAT_TAG).append("\":\"").append(escape(s)).append("\"");
        fieldCount++;
        s = type != null ? type : this.type;
        if (fieldCount > 0) {
            sb.append(",");
        }
        sb.append("\"").append(TYPE_TAG).append("\":\"").append(escape(s)).append("\"");
        fieldCount++;
    }

    @Override
    public void leader(String label) {
        super.leader(label);
        if (fieldCount > 0) {
            sb.append(",");
        }
        sb.append("\"").append(LEADER_TAG).append("\":\"").append(label).append("\"");
        fieldCount++;
    }

    @Override
    public void field(MarcField field) {
        super.field(field);
        fieldCount = toJson(field, fieldCount, sb);
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
        try {
            sb.append("}");
            writer.write(sb.toString());
            sb.setLength(0);
            recordCounter.incrementAndGet();
        } catch (IOException e) {
            handleException(e);
        }
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
    private void toJson(MarcRecord marcRecord, StringBuilder sb) {
        if (recordCounter.get() > 0) {
            sb.append(jsonlines ? "\n" : ",");
        }
        sb.append("{");
        int recordFieldCount = 0;
        if (format != null) {
            sb.append("\"_FORMAT\":\"").append(escape(format)).append("\"");
            recordFieldCount++;
        }
        if (type != null) {
            if (recordFieldCount > 0) {
                sb.append(",");
            }
            sb.append("\"_TYPE\":\"").append(escape(type)).append("\"");
            recordFieldCount++;
        }
        if (recordFieldCount > 0) {
            sb.append(",");
        }
        sb.append("\"_LEADER\":\"").append(marcRecord.getRecordLabel()).append("\"");
        recordFieldCount++;
        for (MarcField field : marcRecord.getFields()) {
            recordFieldCount = toJson(field, recordFieldCount, sb);
        }
        sb.append('}');
        if (jsonlines) {
            sb.append("\n");
        }
    }

    /**
     * Print a key-oriented JSON represenation of this MARC field.
     *
     * @param fieldCount how many MARC field are writte before. Used for emitting a comma
     *                   if necessary.
     * @param sb the string builder to attach the JSON representation to.
     *
     * @return the new MARC field count. Empty MARC fields not not increase the field count.
     */
    private int toJson(MarcField marcField, int fieldCount, StringBuilder sb) {
        int count = fieldCount;
        if (marcField.isControl()) {
            if (count > 0) {
                sb.append(",");
            }
            sb.append("\"").append(marcField.getTag()).append(JSON_1).append(escape(marcField.getValue())).append("\"");
            count++;
            return count;
        } else if (!marcField.isEmpty()) {
            if (count > 0) {
                sb.append(",");
            }
            sb.append("\"").append(marcField.getTag()).append("\":{\"")
                    .append(marcField.getIndicator().replace(' ', '_')).append("\":");
            if (marcField.getSubfields().size() == 1) {
                MarcField.Subfield subfield = marcField.getSubfields().get(0);
                sb.append("{\"").append(subfield.getId()).append(JSON_1).append(escape(subfield.getValue())).append("\"}");
            } else {
                sb.append("[");
                StringBuilder subfieldBuilder = new StringBuilder();
                for (MarcField.Subfield subfield : marcField.getSubfields()) {
                    if (subfieldBuilder.length() > 0) {
                        subfieldBuilder.append(",");
                    }
                    subfieldBuilder.append("{\"").append(subfield.getId()).append(JSON_1)
                            .append(escape(subfield.getValue())).append("\"}");
                }
                sb.append(subfieldBuilder);
                sb.append("]");
            }
            sb.append("}");
            count++;
        }
        return count;
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
