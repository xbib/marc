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
package org.xbib.marc.dialects.sisis;

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

/**
 * An input stream for reading SISIS format, a dialect of MARC.
 *
 * Example:
 *
 * <code>
 * 0000:248821
 * 0002:04.03.2009
 * 0014.001:61 P 08/09-36B
 * 0015.001:ger
 * 0029.001:06
 * 0100.001:Beimdiek, Christoph
 * 0101.001:Hanrath, Wilhelm [Betreuer]
 * 0331.001:Portierung des webbasierten multimedialen Lehr- und Lernsystems NUMAS zur numerischen Mathematik
 * und Statistik auf ein Linux-basiertes Serversystem
 * 0425:2008
 * 0519.001:Aachen, FH, Bachelorarbeit, 2008
 * 9999:
 *
 * 0000:468277
 * 0002:13.12.2013
 * 0014.001:61 EU 13-46B
 * 0015.001:eng
 * 0029.001:06
 * 0100.001:Belouanas, Abdel
 * 0101.001:Helsper, Christoph [Betreuer]
 * 0331.001:A proposal for future clean power production for morocco
 * 0425:2013
 * 0519.001:Aachen, FH, Bachelorarbeit, 2013
 * 9999:
 *
 * </code>
 */
public class SisisInputStream extends PatternInputStream {

    private final MarcGenerator marcGenerator;

    private final BytesStreamOutput bytesStreamOutput;

    private final RecordLabel label;

    private boolean labelEmitted;

    /**
     * Create a SISIS input stream.
     * @param in the underlying input stream
     * @param pattern the pattern for the separator
     * @param marcGenerator a MARC generator
     */
    public SisisInputStream(InputStream in, byte[] pattern, MarcGenerator marcGenerator) {
        super(in, pattern);
        this.marcGenerator = marcGenerator;
        this.bytesStreamOutput = new BytesStreamOutput();
        // this format comes without a record label, create a default one
        this.label = RecordLabel.builder().setIndicatorLength(2).setSubfieldIdentifierLength(1).build();
        this.labelEmitted = false;
    }

    @Override
    protected void processChunk(Chunk<byte[], BytesReference> chunk) throws IOException {
        BytesReference data = chunk.data();
        int pos = data.indexOf((byte) ':', 0, data.length());
        if (pos <= 0) {
            return;
        }
        byte[] numberBytes = data.slice(0, pos).toBytes();
        String number = new String(numberBytes, StandardCharsets.US_ASCII);
        String ind2 = " ";
        // number can have a counter for field repetitions
        int pos2 = number.indexOf('.');
        if (pos2 > 0) {
            ind2 = number.substring(pos2 + 3, pos2 + 4); // drop pos+1, pos+2 (always "0"?)
            number = number.substring(0, pos2);
        }
        // number is always four characters, take the last three and make the first character to "indicator 1"
        String ind1 = number.substring(0, 1);
        number = number.substring(1, 4);
        // special field 9999 means "end of record" (i.e. group delimiter)
        if ("999".equals(number)) {
            labelEmitted = false;
        } else {
            String designator;
            // move "kat key" from 000 to 001
            if ("000".equals(number)) {
                number = "001";
                designator = number;
            } else if (number.startsWith("00")) {
                if (!" ".equals(ind2)) {
                    // move fields out of controlfield area "000"-"009"
                    // to (a hopefully unndefined) area "900-909" (plus ind2="9")
                    designator = "9" + number.substring(1, 3) + "9" + ind2 + "a";
                } else {
                    designator = number;
                }
            } else {
                designator = number + ind1 + ind2 + "a";
            }
            if (!labelEmitted) {
                bytesStreamOutput.write(label.toString().getBytes(StandardCharsets.US_ASCII));
                marcGenerator.chunk(new DefaultChunk(InformationSeparator.GS, bytesStreamOutput.bytes()));
                bytesStreamOutput.reset();
                labelEmitted = true;
            }
            bytesStreamOutput.write(designator.getBytes(StandardCharsets.US_ASCII));
            bytesStreamOutput.write(data.slice(pos + 1, data.length() - (pos + 1)).toBytes());
            marcGenerator.chunk(new DefaultChunk(InformationSeparator.RS, bytesStreamOutput.bytes()));
            bytesStreamOutput.reset();
        }
    }

    @Override
    public void close() throws IOException {
        marcGenerator.chunk(new DefaultChunk(InformationSeparator.FS, null));
        super.close();
    }
}
