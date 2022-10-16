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
package org.xbib.marc.dialects.bibliomondo;

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
 * BiblioMondo dialect of MARC.
 *
 * The format is specified as follows:
 *
 * <ul>
 * <li>character set is ANSEL</li>
 * <li>each MARC field is delimited by carriage-return/line-feed (not by RS)</li>
 * <li>each record is introduced by a record label with tag "###"</li>
 * <li>each control field has a tag immediately followed by value</li>
 * <li>each data field has two indicator characters, and subfields</li>
 * <li>subfields are delimited by US</li>
 * <li>but each subfield begins with a subfield ID plus FS (so FS must be dropped)</li>
 * <li>there may be extra empty lines between records</li>
 * </ul>
 */
public class BiblioMondoInputStream extends PatternInputStream {

    private final MarcGenerator marcGenerator;

    private final BytesStreamOutput bytesStreamOutput;

    public BiblioMondoInputStream(InputStream in,
                                  byte[] pattern,
                                  MarcGenerator marcGenerator) {
        this(in, pattern, marcGenerator, 8192);
    }

    public BiblioMondoInputStream(InputStream in,
                                  byte[] pattern,
                                  MarcGenerator marcGenerator,
                                  int bufferSize) {
        super(in, pattern, bufferSize);
        this.marcGenerator = marcGenerator;
        this.bytesStreamOutput = new BytesStreamOutput();
    }

    @Override
    protected void processChunk(Chunk<byte[], BytesReference> chunk) throws IOException {
        BytesReference data = chunk.data();
        if (data.length() < 5) {
            // broken, invalid tag+indicator info
            return;
        }
        byte[] bytes = data.slice(0, 3).toBytes();
        String tag = new String(bytes, StandardCharsets.ISO_8859_1);
        if ("###".equals(tag)) { // leader
            String value = new String(data.slice(4, data.length() - 4).toBytes(), StandardCharsets.ISO_8859_1);
            RecordLabel label = RecordLabel.builder().from(value.toCharArray())
                    .setIndicatorLength(2).setSubfieldIdentifierLength(2).build();
            marcGenerator.chunk(new DefaultChunk(InformationSeparator.GS, new BytesArray(label.asBytes())));
        } else if (tag.startsWith("00")) {
            // control field
            bytesStreamOutput.reset();
            bytesStreamOutput.write(bytes);
            bytesStreamOutput.write(data.slice(3, data.length() - 3).toBytes());
            bytesStreamOutput.close();
            marcGenerator.chunk(new DefaultChunk(InformationSeparator.RS, bytesStreamOutput.bytes()));
        } else {
            bytesStreamOutput.reset();
            bytesStreamOutput.write(bytes); // tag
            bytesStreamOutput.write(data.slice(3, 2).toBytes()); // indicator
            marcGenerator.chunk(new DefaultChunk(InformationSeparator.RS, bytesStreamOutput.bytes()));
            BytesArray bytesArray = new BytesArray(data.slice(5, data.length() - 5).toBytes());
            List<byte[]> list = bytesArray.split((byte) 0x1f);
            for (byte[] b : list) {
                if (b.length < 3) {
                    continue; //empty subfield, skip
                }
                bytesStreamOutput.reset();
                bytesStreamOutput.write(b[0]); // subfield ID
                // skip strange 0x1f byte at b[1]
                bytesStreamOutput.write(b, 2, b.length - 2); // subfield value
                marcGenerator.chunk(new DefaultChunk(InformationSeparator.US, bytesStreamOutput.bytes()));
            }
        }
    }

    @Override
    public void close() throws IOException {
        marcGenerator.chunk(new DefaultChunk(InformationSeparator.FS, null));
        super.close();
    }
}
