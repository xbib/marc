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
package org.xbib.marc.dialects.pica;

import org.xbib.marc.MarcGenerator;
import org.xbib.marc.io.BytesArray;
import org.xbib.marc.io.BytesReference;
import org.xbib.marc.io.BytesStreamOutput;
import org.xbib.marc.io.Chunk;
import org.xbib.marc.io.DefaultChunk;
import org.xbib.marc.io.InformationSeparator;
import org.xbib.marc.io.PatternInputStream;
import org.xbib.marc.label.RecordLabel;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Pica plain, a line-feed-delimited text file dialect of MARC.
 *
 * The format is specified as follows:
 *
 * <ul>
 * <li>character set is PICA or UTF-8</li>
 * <li>each record is delimited by empy line</li>
 * <li>each field is delimited by line-feed</li>
 * <li>no record label</li>
 * <li>each field tag has one indicator (including "@") and a repeat counter (like 201B/01)</li>
 * <li>subfields are delimited by '$' and can even be empty</li>
 * </ul>
 */
public class PicaPlainInputStream extends PatternInputStream {

    private final MarcGenerator marcGenerator;

    private final BytesStreamOutput bytesStreamOutput;

    private boolean started;

    public PicaPlainInputStream(InputStream in,
                                byte[] pattern,
                                MarcGenerator marcGenerator) {
        this(in, pattern, marcGenerator, 8192);
    }

    public PicaPlainInputStream(InputStream in,
                                byte[] pattern,
                                MarcGenerator marcGenerator,
                                int bufferSize) {
        super(in, pattern, bufferSize);
        this.marcGenerator = marcGenerator;
        this.bytesStreamOutput = new BytesStreamOutput();
        this.started = true;
    }

    @Override
    protected void processChunk(Chunk<byte[], BytesReference> chunk) throws IOException {
        BytesArray array = new BytesArray(chunk.data().toBytes());
        Chunk<byte[], BytesReference> newChunk;
        // no leaders, so we need our own start logic
        if (started) {
            startRecord();
            started = false;
        }
        if (chunk.data().length() < 2) {
            startRecord();
            return;
        }
        // control field?
        if (array.get(0) == '0' && array.get(1) == '0') {
            bytesStreamOutput.reset();
            bytesStreamOutput.write(array.slice(0, 3).toBytes());
            // skip two indicators and subfield delimiter '$' and '0'
            bytesStreamOutput.write(array.slice(7, array.length() - 7).toBytes());
            newChunk = new DefaultChunk(InformationSeparator.RS, bytesStreamOutput.bytes());
            marcGenerator.chunk(newChunk);
        } else {
            // split into subfields
            List<byte[]> subfields = array.split((byte) '$');
            if (!subfields.isEmpty()) {
                byte[] datafield = subfields.get(0);
                // move PICA key into MARC indicators
                byte[] ind1 = array.slice(3, 1).toBytes();
                byte ind2 = ' ';
                if (datafield.length == 8) {
                    // does not always work well...
                    ind2 = (byte) ('0' + (10 * (datafield[5] - '0')) + (datafield[6] - '0'));
                }
                bytesStreamOutput.reset();
                bytesStreamOutput.write(array.slice(0, 3).toBytes());
                bytesStreamOutput.write(ind1); // indicator 1
                bytesStreamOutput.write(ind2); // indicator 2
                newChunk = new DefaultChunk(InformationSeparator.RS, bytesStreamOutput.bytes());
                marcGenerator.chunk(newChunk);
                for (int i = 1; i < subfields.size(); i++) {
                    byte[] subfield = subfields.get(i);
                    if (subfield.length > 1) {
                        bytesStreamOutput.reset();
                        bytesStreamOutput.write(subfield);
                        newChunk = new DefaultChunk(InformationSeparator.US, bytesStreamOutput.bytes());
                        marcGenerator.chunk(newChunk);
                    }
                }
            }
        }
    }

    private void startRecord() throws IOException {
        RecordLabel label = RecordLabel.builder()
                .setIndicatorLength(2).setSubfieldIdentifierLength(2).build();
        marcGenerator.chunk(new DefaultChunk(InformationSeparator.GS, new BytesArray(label.asBytes())));
    }

}
