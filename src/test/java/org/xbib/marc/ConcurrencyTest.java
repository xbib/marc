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
package org.xbib.marc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.xbib.marc.json.MarcJsonWriter;
import org.xbib.marc.xml.MarcXchangeWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 */
public class ConcurrencyTest {

    /**
     * Open same file 16 times, but write to a single XML writer.
     * We have three streaks:
     * <ul>
     *     <li>file header / collection start (not parallizable)</li>
     *     <li>records (can be parallelized, implemented by {@code writeRecords()})</li>
     *     <li>collection end / file close (not parallizable)</li>
     * </ul>
     * @throws Exception if test fails
     */
    @Test
    public void concurrentXmlWrite() throws Exception {
        int n = 16;
        ExecutorService executorService = Executors.newFixedThreadPool(n);
        String s = "zdblokutf8.mrc";
        File file = File.createTempFile(s + ".", ".xml");
        file.deleteOnExit();
        FileOutputStream out = new FileOutputStream(file);
        try (MarcXchangeWriter writer = new MarcXchangeWriter(out, true)
             .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
             .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
        ) {
            writer.startDocument();
            writer.beginCollection();
            for (int i = 0; i < n; i++) {
                InputStream in = getClass().getResource(s).openStream();
                executorService.submit(() -> {
                    Marc.builder()
                            .setInputStream(in)
                            .setMarcRecordListener(writer)
                            .build()
                            .writeRecords();
                    return true;
                });
            }
            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
            writer.endCollection();
            writer.endDocument();
            assertNull(writer.getException());
            assertEquals(n * 293, writer.getRecordCounter());
        }
    }

    /**
     * Write MARC records to JSON array.
     *
     * @throws Exception if test fails
     */
    @Test
    public void concurrentJsonArrayWrite() throws Exception {
        int n = 16;
        ExecutorService executorService = Executors.newFixedThreadPool(n);
        String s = "zdblokutf8.mrc";
        File file = File.createTempFile(s + ".", ".json");
        file.deleteOnExit();
        FileOutputStream out = new FileOutputStream(file);
        try (MarcJsonWriter writer = new MarcJsonWriter(out)
                .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
        ) {
            writer.startDocument();
            writer.beginCollection();
            for (int i = 0; i < n; i++) {
                InputStream in = getClass().getResource(s).openStream();
                executorService.submit(() -> {
                    Marc.builder()
                            .setInputStream(in)
                            .setMarcRecordListener(writer)
                            .build()
                            .writeRecords();
                    return true;
                });
            }
            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
            writer.endCollection();
            writer.endDocument();
            assertNull(writer.getException());
            assertEquals(n * 293, writer.getRecordCounter());
        }
    }
    /**
     * Write JSON lines format. This is shorter than array, because commas are not required.
     *
     * @throws Exception if test fails
     */
    @Test
    public void concurrentJsonLinesWrite() throws Exception {
        int n = 16;
        ExecutorService executorService = Executors.newFixedThreadPool(n);
        String s = "zdblokutf8.mrc";
        File file = File.createTempFile(s + ".", ".jsonlines");
        file.deleteOnExit();
        FileOutputStream out = new FileOutputStream(file);
        try (MarcJsonWriter writer = new MarcJsonWriter(out, EnumSet.of(MarcJsonWriter.Style.LINES))
                .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
        ) {
            writer.startDocument();
            writer.beginCollection();
            for (int i = 0; i < n; i++) {
                InputStream in = getClass().getResource(s).openStream();
                executorService.submit(() -> {
                    Marc.builder()
                            .setInputStream(in)
                            .setMarcRecordListener(writer)
                            .build()
                            .writeRecords();
                    return true;
                });
            }
            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
            writer.endCollection();
            writer.endDocument();
            assertNull(writer.getException());
            assertEquals(n * 293, writer.getRecordCounter());
        }
    }
}
