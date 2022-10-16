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
package org.xbib.marc.dialects.mab.diskette;

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
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * An input stream for MAB DISKETTE, using an underlying pattern input stream.
 * The chunks will be rearranged to get fired off using a
 * given MarcListener in order to produce valid MARC chunks.
 */
public class MabDisketteInputStream extends PatternInputStream {

    private final MarcGenerator marcGenerator;

    private final BytesStreamOutput bytesStreamOutput;

    private final char subfieldDelimiter;

    private byte[] lastchunk;

    public MabDisketteInputStream(InputStream in, byte[] pattern, MarcGenerator marcGenerator) {
        this(in, pattern, '\u0000', marcGenerator);
    }

    public MabDisketteInputStream(InputStream in,
                                  byte[] pattern,
                                  char subfieldDelimiter,
                                  MarcGenerator marcGenerator) {
        this(in, pattern, subfieldDelimiter, marcGenerator, 8192);
    }

    public MabDisketteInputStream(InputStream in,
                                  byte[] pattern,
                                  char subfieldDelimiter,
                                  MarcGenerator marcGenerator,
                                  int bufferSize) {
        super(in, pattern, bufferSize);
        this.marcGenerator = marcGenerator;
        this.subfieldDelimiter = subfieldDelimiter;
        this.bytesStreamOutput = new BytesStreamOutput();
    }

    @Override
    protected void processChunk(Chunk<byte[], BytesReference> chunk) throws IOException {
        BytesReference data = chunk.data();
        if (data.length() < 5) {
            return;
        }
        byte[] numberBytes = data.slice(0, 3).toBytes();
        String number = new String(numberBytes, StandardCharsets.ISO_8859_1);
        if ("###".equals(number)) { // leader
            // skip blank after ###
            String value = new String(data.slice(4, data.length() - 4).toBytes(), StandardCharsets.ISO_8859_1);
            RecordLabel label = RecordLabel.builder().from(value.toCharArray())
                    .setIndicatorLength(2).setSubfieldIdentifierLength(2).build();
            marcGenerator.chunk(new DefaultChunk(InformationSeparator.GS, new BytesArray(label.asBytes())));
        } else if (number.startsWith("00")) {
            // control field, throw away indicators and subfields (if any)
            bytesStreamOutput.reset();
            bytesStreamOutput.write(numberBytes);
            bytesStreamOutput.write(data.slice(4, data.length() - 4).toBytes());
            marcGenerator.chunk(new DefaultChunk(InformationSeparator.RS, bytesStreamOutput.bytes()));
        } else if (isNumber(number)) {
            // data field
            bytesStreamOutput.reset();
            bytesStreamOutput.write(numberBytes);
            bytesStreamOutput.write(data.slice(3, 1).toBytes()); // first indicator
            bytesStreamOutput.write(' '); // second indicator
            Chunk<byte[], BytesReference> newchunk = new DefaultChunk(InformationSeparator.RS, bytesStreamOutput.bytes());
            marcGenerator.chunk(newchunk);
            lastchunk = newchunk.data().toBytes().clone();
            boolean hasSubfields = data.slice(4, 1).toBytes()[0] == subfieldDelimiter;
            if (!hasSubfields) {
                // The challenge of mapping MAB-Diskette to MarcXchange is
                // that MAB is subfield-less. We map to a "default" subfield, e.g. "a".
                bytesStreamOutput.reset();
                bytesStreamOutput.write('a');
                bytesStreamOutput.write(data.slice(4, data.length() - 4).toBytes());
                marcGenerator.chunk(new DefaultChunk(InformationSeparator.US, bytesStreamOutput.bytes()));
            } else {
                // challenge: split only if subfieldDelimiter is set on the first character. If not set, no subfields at all.
                BytesArray bytesArray = new BytesArray(data.slice(4, data.length() - 4).toBytes());
                List<byte[]> list = bytesArray.split((byte) subfieldDelimiter);
                for (byte[] b : list) {
                    if (b.length < 3) {
                        continue; //empty subfield, skip
                    }
                    bytesStreamOutput.reset();
                    bytesStreamOutput.write(b); // subfield ID + value
                    marcGenerator.chunk(new DefaultChunk(InformationSeparator.US, bytesStreamOutput.bytes()));
                }
            }
        } else if (lastchunk != null) {
            // we found a continuation line. Just repeat last tag with the continuation content.
            bytesStreamOutput.reset();
            bytesStreamOutput.write(lastchunk);
            marcGenerator.chunk(new DefaultChunk(InformationSeparator.RS, bytesStreamOutput.bytes()));
            bytesStreamOutput.reset();
            bytesStreamOutput.write('a');
            bytesStreamOutput.write(data.toBytes());
            marcGenerator.chunk(new DefaultChunk(InformationSeparator.US, bytesStreamOutput.bytes()));
        }
    }

    private static boolean isNumber(String s) {
        boolean b = true;
        for (char ch : s.toCharArray()) {
            if (ch < '0' || ch > '9') {
                b = false;
                break;
            }
        }
        return b;
    }
}
