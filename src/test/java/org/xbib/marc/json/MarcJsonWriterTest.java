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

import static org.xbib.helper.StreamMatcher.assertStream;

import org.junit.Test;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcRecordAdapter;
import org.xbib.marc.MarcXchangeConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 *
 */
public class MarcJsonWriterTest {

    /**
     * {@code }MarcJsonWriter} can receive MARC fields.
     *
     * @throws Exception if test fails
     */
    @Test
    public void testMarcJson() throws Exception {
        for (String s : new String[]{
                "summerland.mrc",
                "chabon.mrc",
                "chabon-loc.mrc"
        }) {
            InputStream in = getClass().getResource("/org/xbib/marc/" + s).openStream();
            File file = File.createTempFile(s + ".", ".json");
            file.deleteOnExit();
            FileOutputStream out = new FileOutputStream(file);
            try (MarcJsonWriter writer = new MarcJsonWriter(out)
                    .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                    .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
            ) {
                Marc.builder()
                        .setInputStream(in)
                        .setCharset(Charset.forName("ANSEL"))
                        .setMarcListener(writer)
                        .build()
                        .writeCollection();
            }
            assertStream(s, getClass().getResource("/org/xbib/marc/json/" + s + ".json").openStream(),
                    new FileInputStream(file));
        }
    }

    /**
     * {@code }MarcJsonWriter} can receive MARC records.
     *
     * @throws Exception if test fails
     */
    @Test
    public void testMarcRecordJson() throws Exception {
        for (String s : new String[]{
                "summerland.mrc",
                "chabon.mrc",
                "chabon-loc.mrc"
        }) {
            InputStream in = getClass().getResource("/org/xbib/marc/" + s).openStream();
            File file = File.createTempFile(s + ".", ".json");
            file.deleteOnExit();
            FileOutputStream out = new FileOutputStream(file);
            try (MarcJsonWriter writer = new MarcJsonWriter(out)
                    .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                    .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
            ) {
                Marc.builder()
                        .setInputStream(in)
                        .setCharset(Charset.forName("ANSEL"))
                        .setMarcRecordListener(writer)
                        .build()
                        .writeRecordCollection();
            }
            assertStream(s, getClass().getResource("/org/xbib/marc/json/" + s + ".json").openStream(),
                    new FileInputStream(file));
        }
    }

    /**
     * The MARC record adapter receives field events and collects them into a MARC record,
     * which is passed on. Useful for writing MARC record-by-record, e.g. in a multithreaded
     * environment, or if the source can only emit MARC fields (like the XML content handler).
     * @throws Exception if test fails
     */
    @Test
    public void testMarcRecordAdapterJson() throws Exception {
        for (String s : new String[]{
                "summerland.mrc",
                "chabon.mrc",
                "chabon-loc.mrc"
        }) {
            InputStream in = getClass().getResource("/org/xbib/marc/" + s).openStream();
            File file = File.createTempFile(s + ".", ".json");
            file.deleteOnExit();
            FileOutputStream out = new FileOutputStream(file);
            try (MarcJsonWriter writer = new MarcJsonWriter(out)
                    .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                    .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
            ) {
                Marc.builder()
                        .setInputStream(in)
                        .setCharset(Charset.forName("ANSEL"))
                        .setMarcListener(new MarcRecordAdapter(writer))
                        .build()
                        .writeCollection();
            }
            assertStream(s, getClass().getResource("/org/xbib/marc/json/" + s + ".json").openStream(),
                    new FileInputStream(file));
        }
    }
}
