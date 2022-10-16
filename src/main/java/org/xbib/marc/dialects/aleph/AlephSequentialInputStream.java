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
package org.xbib.marc.dialects.aleph;

import org.xbib.marc.MarcGenerator;
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
import java.util.regex.Pattern;

/**
 * ALEPH SEQUENTIAL input stream implementation.
 */
public class AlephSequentialInputStream extends PatternInputStream {

    private final MarcGenerator marcGenerator;

    private final BytesStreamOutput bytesStreamOutput;

    private RecordLabel label;

    private String alephSysNumber;

    public AlephSequentialInputStream(InputStream in,
                                      byte[] pattern,
                                      MarcGenerator marcGenerator) {
        this(in, pattern, marcGenerator, 8192);
    }

    public AlephSequentialInputStream(InputStream in,
                                      byte[] pattern,
                                      MarcGenerator marcGenerator,
                                      int bufferSize) {
        super(in, pattern, bufferSize);
        this.marcGenerator = marcGenerator;
        this.bytesStreamOutput = new BytesStreamOutput();
        // this format might come without a record label, create a default one
        this.label = RecordLabel.builder()
                .setIndicatorLength(2)
                .setSubfieldIdentifierLength(1)
                .build();
    }

    @Override
    protected void processChunk(Chunk<byte[], BytesReference> chunk) throws IOException {
        BytesReference data = chunk.data();
        String str = data.toUtf8();
        String value = str.substring(18);
        String nextAlephSysNumber = str.substring(0, 9);
        BytesReference tag = data.slice(10, 3);
        BytesReference indicator = data.slice(13, 2);
        boolean newLabel;
        if ("LDR".equals(tag.toUtf8())) {
            label = RecordLabel.builder().from(value.replace('^', ' ').toCharArray()).build();
            newLabel = true;
        } else {
            newLabel = false;
        }
        if (alephSysNumber == null || !alephSysNumber.equals(nextAlephSysNumber)) {
            bytesStreamOutput.reset();
            bytesStreamOutput.write(label.toString().getBytes(StandardCharsets.ISO_8859_1));
            marcGenerator.chunk(new DefaultChunk(InformationSeparator.GS, bytesStreamOutput.bytes()));
            this.alephSysNumber = nextAlephSysNumber;
            return;
        }
        if (newLabel) {
            return;
        }
        if ("00".equals(str.substring(10, 12))) {
            bytesStreamOutput.reset();
            bytesStreamOutput.write(tag.toBytes());
            // skip indicator on control fiels
            bytesStreamOutput.write(value.replace('^', ' ').getBytes(StandardCharsets.UTF_8));
            marcGenerator.chunk(new DefaultChunk(InformationSeparator.RS, bytesStreamOutput.bytes()));
            return;
        }
        bytesStreamOutput.reset();
        bytesStreamOutput.write(tag.toBytes());
        bytesStreamOutput.write(indicator.toBytes());
        marcGenerator.chunk(new DefaultChunk(InformationSeparator.RS, bytesStreamOutput.bytes()));
        String[] subfields = value.split(Pattern.quote("$$"));
        for (String subfield : subfields) {
            if (subfield.isEmpty()) {
                continue;
            }
            bytesStreamOutput.reset();
            bytesStreamOutput.write(subfield.getBytes(StandardCharsets.UTF_8));
            marcGenerator.chunk(new DefaultChunk(InformationSeparator.US, bytesStreamOutput.bytes()));
        }
    }

    @Override
    public void close() throws IOException {
        marcGenerator.chunk(new DefaultChunk(InformationSeparator.FS, null));
        super.close();
    }
}
