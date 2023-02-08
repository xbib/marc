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

import org.xbib.marc.io.BytesReference;
import org.xbib.marc.io.Chunk;
import org.xbib.marc.io.ChunkListener;
import org.xbib.marc.label.RecordLabel;
import org.xbib.marc.label.RecordLabelFixer;
import org.xbib.marc.transformer.MarcTransformer;
import org.xbib.marc.transformer.field.MarcFieldTransformers;
import org.xbib.marc.transformer.value.MarcValueTransformers;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import static org.xbib.marc.io.InformationSeparator.GS;
import static org.xbib.marc.io.InformationSeparator.RS;
import static org.xbib.marc.io.InformationSeparator.US;

/**
 * This chunk listener interprets the chunks from a stream and generates MARC events to a given MARC listener.
 */
public class MarcGenerator implements ChunkListener<byte[], BytesReference> {

    private String format;

    private String type;

    private Charset charset;

    private MarcListener marcListener;

    private RecordLabelFixer recordLabelFixer;

    private MarcFieldTransformers marcFieldTransformers;

    private MarcValueTransformers marcValueTransformers;

    private MarcTransformer marcTransformer;

    private boolean fatalerrors;

    private String data;

    private int position;

    private RecordLabel recordLabel;

    private MarcFieldDirectory directory;

    private MarcField.Builder builder;

    private final List<MarcField> marcFieldList;

    public MarcGenerator() {
        this.builder = MarcField.builder();
        this.position = 0;
        this.marcFieldList = new LinkedList<>();
    }

    public MarcGenerator setValidator(MarcFieldValidator validator) {
        this.builder.setValidator(validator);
        return this;
    }

    public MarcGenerator setFormat(String format) {
        if (format != null) {
            this.format = format;
        }
        return this;
    }

    public MarcGenerator setType(String type) {
        if (type != null) {
            this.type = type;
        }
        return this;
    }

    public MarcGenerator setCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public MarcGenerator setMarcListener(MarcListener marcListener) {
        this.marcListener = marcListener;
        return this;
    }

    public MarcGenerator setRecordLabelFixer(RecordLabelFixer recordLabelFixer) {
        this.recordLabelFixer = recordLabelFixer;
        return this;
    }

    public MarcGenerator setMarcFieldTransformers(MarcFieldTransformers marcFieldTransformers) {
        this.marcFieldTransformers = marcFieldTransformers;
        return this;
    }

    public MarcGenerator setMarcValueTransformers(MarcValueTransformers marcValueTransformers) {
        this.marcValueTransformers = marcValueTransformers;
        return this;
    }

    public MarcGenerator setMarcTransformer(MarcTransformer marcTransformer) {
        this.marcTransformer = marcTransformer;
        return this;
    }

    public MarcGenerator setFatalErrors(boolean fatalerrors) {
        this.fatalerrors = fatalerrors;
        return this;
    }

    public MarcGenerator disableControlFields() {
        builder.disableControlFields();
        return this;
    }

    @Override
    public void chunk(Chunk<byte[], BytesReference> chunk) throws IOException {
        char separator = (char) chunk.separator()[0];
        BytesReference bytesReference = chunk.data();
        if (bytesReference == null || bytesReference.length() == 0) {
            emitMarcField();
            emitMarcRecord();
            return;
        }
        this.data = new String(bytesReference.toBytes(), charset);
        if (position == 0) {
            newRecord();
            position += bytesReference.length() + 1;
            return;
        }
        switch (separator) {
            case GS: /* 1d */ {
                emitMarcField();
                emitMarcRecord();
                newRecord();
                break;
            }
            case RS: /* 1e */ {
                emitMarcField();
                if (directory == null || directory.isEmpty()) {
                    if (marcTransformer != null) {
                        marcTransformer.transform(builder, recordLabel, data);
                    } else {
                        builder.field(format, type, recordLabel, data);
                    }
                } else if (directory.containsKey(position)) {
                    builder = directory.get(position);
                    if (builder.isControl()) {
                        builder.value(data);
                    } else {
                        int pos = recordLabel.getIndicatorLength();
                        builder.indicator(data.substring(0, pos));
                        if (pos < data.length()) {
                            builder.value(data.substring(pos));
                        }
                    }
                } else {
                    boolean found = false;
                    // try more than one position
                    for (int offset = 1; offset < 5; offset++) {
                        if (directory.containsKey(position + offset)) {
                            position = position + offset;
                            builder = directory.get(position);
                            if (builder.isControl()) {
                                builder.value(data);
                            } else {
                                int pos = recordLabel.getIndicatorLength();
                                builder.indicator(data.substring(0, pos));
                                if (pos < data.length()) {
                                    builder.value(this.data.substring(pos));
                                }
                            }
                            found = true;
                            break;
                        } else if (directory.containsKey(position - offset)) {
                            position = position - offset;
                            builder = directory.get(position);
                            if (builder.isControl()) {
                                builder.value(data);
                            } else {
                                int pos = recordLabel.getIndicatorLength();
                                builder.indicator(data.substring(0, pos));
                                if (pos < data.length()) {
                                    builder.value(data.substring(pos));
                                }
                            }
                            found = true;
                            break;
                        }
                    }
                    if (!found && fatalerrors) {
                        throw new IOException("byte position not found in MARC directory: "
                                + position + " - broken directory or bad encoding?");
                    }
                }
                break;
            }
            case US: /* 1f */ {
                builder.value(recordLabel, data);
                break;
            }
            default: {
                break;
            }
        }
        position += bytesReference.length() + 1;
    }

    /**
     * This method will emit the last record, if not emitted already.
     * Useful if chunk streams have no closing record separator.
     */
    public void flush() {
        if (position > 0) {
            emitMarcRecord();
        }
    }

    private void emitMarcField() {
        MarcField marcField = builder.build();
        if (marcValueTransformers != null) {
            marcField = marcValueTransformers.transformValue(marcField);
        }
        if (marcFieldTransformers != null) {
            marcFieldList.add(marcField);
        } else {
            if (!marcField.isEmpty() && marcListener != null) {
                marcListener.field(marcField);
            }
        }
        builder = MarcField.builder();
    }

    private void emitMarcRecord() {
        if (marcFieldTransformers != null) {
            for (MarcField marcField : marcFieldTransformers.transform(marcFieldList)) {
                if (!marcField.isEmpty() && marcListener != null) {
                    marcListener.field(marcField);
                }
            }
            marcFieldTransformers.reset();
            marcFieldList.clear();
        }
        if (marcListener != null) {
            marcListener.endRecord();
        }
        position = 0;
    }

    private void newRecord() throws IOException {
        // skip line-feed (OCLC PICA quirk)
        if (data.charAt(0) == '\n') {
            data = data.substring(1);
        }
        if (data.length() > RecordLabel.LENGTH) {
            // record label + record content = old directory-based format
            recordLabel = RecordLabel.builder().from(this.data.substring(0, RecordLabel.LENGTH).toCharArray()).build();
            if (recordLabelFixer != null) {
                this.recordLabel = recordLabelFixer.fix(recordLabel);
            }
            if (marcListener != null) {
                marcListener.beginRecord(format, type);
                marcListener.leader(recordLabel);
            }
            directory = new MarcFieldDirectory(recordLabel, this.data);
            if (directory.isEmpty()) {
                builder.field(format, type, recordLabel, data.substring(RecordLabel.LENGTH));
            }
        } else if (this.data.length() == RecordLabel.LENGTH) {
            recordLabel = RecordLabel.builder().from(this.data.substring(0, RecordLabel.LENGTH).toCharArray()).build();
            if (recordLabelFixer != null) {
                this.recordLabel = recordLabelFixer.fix(recordLabel);
            }
            // record label only = new format without directory
            directory = new MarcFieldDirectory(recordLabel, this.data);
            if (directory.isEmpty()) {
                if (marcListener != null) {
                    marcListener.beginRecord(format, type);
                    marcListener.leader(recordLabel);
                }
            } else {
                builder = MarcField.builder();
            }
        } else {
            // leader too short, ignore. Use a default record label
            this.recordLabel = RecordLabel.builder().build();
            if (recordLabelFixer != null) {
                this.recordLabel = recordLabelFixer.fix(recordLabel);
            }
        }
    }
}
