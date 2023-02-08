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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.xbib.marc.json.MarcJsonWriter;
import org.xbib.marc.xml.MarcXchangeWriter;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConcurrencyTest {

    /**
     * Open same file 16 times, but write to a single XML writer.
     * We have three streaks:
     * <ul>
     *     <li>file header / collection start (not parallelizable)</li>
     *     <li>records (can be parallelized, implemented by {@code writeRecords()})</li>
     *     <li>collection end / file close (not parallelizable)</li>
     * </ul>
     * @throws Exception if test fails
     */
    @Test
    public void concurrentXmlWrite() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (MarcXchangeWriter writer = new MarcXchangeWriter(outputStream, true)
                    .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                    .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)) {
            writer.startDocument();
            writer.beginCollection();
            int n = 16;
            ExecutorService executorService = Executors.newFixedThreadPool(n);
            for (int i = 0; i < n; i++) {
                executorService.submit(() -> {
                    URL url = getClass().getResource("zdblokutf8.mrc");
                    if (url != null) {
                        try (InputStream inputStream = url.openStream()) {
                            Marc.builder()
                                    .setInputStream(inputStream)
                                    .setMarcRecordListener(writer)
                                    .build()
                                    .writeRecords();
                        }
                    }
                    return true;
                });
            }
            executorService.shutdown();
            executorService.awaitTermination(30L, TimeUnit.SECONDS);
            writer.endCollection();
            writer.endDocument();
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
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (MarcJsonWriter writer = new MarcJsonWriter(outputStream)
                .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
        ) {
            writer.startDocument();
            writer.beginCollection();
            int n = 16;
            ExecutorService executorService = Executors.newFixedThreadPool(n);
            for (int i = 0; i < n; i++) {
                executorService.submit(() -> {
                    URL url = getClass().getResource("zdblokutf8.mrc");
                    if (url != null) {
                        try (InputStream inputStream = url.openStream()) {
                            Marc.builder()
                                    .setInputStream(inputStream)
                                    .setMarcRecordListener(writer)
                                    .build()
                                    .writeRecords();
                        }
                    }
                    return true;
                });
            }
            executorService.shutdown();
            executorService.awaitTermination(30L, TimeUnit.SECONDS);
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
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (MarcJsonWriter writer = new MarcJsonWriter(outputStream)
                .setStyle(EnumSet.of(MarcJsonWriter.Style.LINES))
                .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)
                .setType(MarcXchangeConstants.BIBLIOGRAPHIC_TYPE)
        ) {
            writer.startDocument();
            writer.beginCollection();
            int n = 16;
            ExecutorService executorService = Executors.newFixedThreadPool(n);
            for (int i = 0; i < n; i++) {
                executorService.submit(() -> {
                    URL url = getClass().getResource("zdblokutf8.mrc");
                    if (url != null) {
                        try (InputStream inputStream = url.openStream()) {
                            Marc.builder()
                                    .setInputStream(inputStream)
                                    .setMarcRecordListener(writer)
                                    .build()
                                    .writeRecords();
                        }
                    }
                    return true;
                });
            }
            executorService.shutdown();
            executorService.awaitTermination(30L, TimeUnit.SECONDS);
            writer.endCollection();
            writer.endDocument();
            assertNull(writer.getException());
            assertEquals(n * 293, writer.getRecordCounter());
        }
    }
}
