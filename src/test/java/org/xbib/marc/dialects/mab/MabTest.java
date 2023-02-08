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
package org.xbib.marc.dialects.mab;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.xbib.marc.StreamMatcher.assertStream;
import static org.xbib.marc.transformer.field.MarcFieldTransformer.Operator.HEAD;
import static org.xbib.marc.transformer.field.MarcFieldTransformer.Operator.TAIL;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcField;
import org.xbib.marc.MarcListener;
import org.xbib.marc.StreamMatcher;
import org.xbib.marc.dialects.mab.xml.MabXMLContentHandler;
import org.xbib.marc.label.RecordLabel;
import org.xbib.marc.transformer.field.MarcFieldTransformer;
import org.xbib.marc.transformer.field.MarcFieldTransformers;
import org.xbib.marc.transformer.value.MarcValueTransformer;
import org.xbib.marc.transformer.value.MarcValueTransformers;
import org.xbib.marc.xml.MarcContentHandler;
import org.xbib.marc.xml.MarcXchangeWriter;
import org.xml.sax.InputSource;
import org.xmlunit.matchers.CompareMatcher;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class MabTest {

    /**
     * Shows how to override erraneous subfield ID length label settings, which is notorious to MAB dialect.
     * @throws Exception if test faul
     */
    @Test
    public void testOldZDBMab() throws Exception {
        String s = "1217zdbtit.dat";
        StreamMatcher.xmlMatch(getClass(), s, ".xml", (inputStream, outputStream) -> {
            try (MarcXchangeWriter writer = new MarcXchangeWriter(outputStream, true)
                    .setFormat("MAB")
                    .setType("Titel")
            ) {
                Marc.builder()
                        .setFormat("MAB")
                        .setType("Titel")
                        .setInputStream(inputStream)
                        .setCharset(Charset.forName("x-MAB"))
                        .setMarcListener("Titel", writer)
                        .build()
                        .writeCollection("Titel");
            }
        });
    }

    /**
     * Test a MAB-XML file conversion to MarcXchange.
     * MAB-XML has no record labels.
     *
     * @throws Exception if test fails
     */
    @Test
    public void testNLM22420274X() throws Exception {
        String s = "NLM22420274X.xml";
        StreamMatcher.fileMatch(getClass(), s, ".txt", (inputStream, outputStream) -> {
            try (Listener listener = new Listener(outputStream, StandardCharsets.UTF_8)) {
                MarcContentHandler contentHandler = new MabXMLContentHandler()
                        .addNamespace("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd")
                        .setFormat("MabXML")
                        .setType("h")
                        .setMarcListener(listener);
                Marc marc = Marc.builder()
                        .setInputStream(inputStream)
                        .setCharset(StandardCharsets.UTF_8)
                        .setContentHandler(contentHandler)
                        .build();
                marc.xmlReader().parse(new InputSource(inputStream));
            }
        });
    }

    /**
     * Wrong-formatted  "MARC" XML, declared as MAB-XML. So we need the MarcContentHandler!
     */
    @Test
    public void testAleph() throws Exception {
        String s = "HT016424175.xml";
        StreamMatcher.fileMatch(getClass(), s, ".txt", (inputStream, outputStream) -> {
            try (Listener listener = new Listener(outputStream, StandardCharsets.UTF_8)) {
                MarcContentHandler contentHandler = new MarcContentHandler();
                contentHandler.addNamespace("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd");
                contentHandler.setFormat("MARC21");
                contentHandler.setType("Bibliographic");
                contentHandler.setMarcListener(listener);
                Marc marc = Marc.builder()
                        .setInputStream(inputStream)
                        .setContentHandler(contentHandler)
                        .build();
                marc.xmlReader().parse(new InputSource(inputStream));
            }
        });
    }

    @Test
    public void testCombinedWriting() throws Exception {
        String s = "HT016424175.xml";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StringWriter sw = new StringWriter();
        try (Listener listener = new Listener(outputStream, StandardCharsets.UTF_8)) {
            MarcXchangeWriter writer = new MarcXchangeWriter(sw);
            writer.setFormat("AlephXML");
            writer.setType("Bibliographic");
            writer.setMarcListener(listener);
            writer.beginCollection();
            // write one record twice to test correct beginCollection/endCollection with two inner parse() calls
            MarcContentHandler contentHandler = new MarcContentHandler();
            contentHandler.addNamespace("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd");
            contentHandler.setFormat("MARC21");
            contentHandler.setType("Bibliographic");
            contentHandler.setMarcListener(writer);
            try (InputStream in = getClass().getResourceAsStream(s)) {
                Marc marc = Marc.builder()
                        .setInputStream(in)
                        .setCharset(StandardCharsets.UTF_8)
                        .setContentHandler(contentHandler)
                        .build();
                marc.xmlReader().parse(new InputSource(in));
            }
            try (InputStream inputStream = getClass().getResourceAsStream(s)) {
                Marc marc = Marc.builder()
                        .setInputStream(inputStream)
                        .setCharset(StandardCharsets.UTF_8)
                        .setContentHandler(contentHandler)
                        .build();
                marc.xmlReader().parse(new InputSource(inputStream));
            }
            writer.endCollection();
            assertNull(writer.getException());
        }
        sw.close();
        //try (Writer writer = Files.newBufferedWriter(Paths.get("HT016424175-combined.xml"))) {
        //    writer.write(sw.toString());
        //}
        assertStream(s, new ByteArrayInputStream(outputStream.toByteArray()),
                getClass().getResource("HT016424175.xml.combined.txt").openStream());
        assertThat(sw.toString(),
                CompareMatcher.isIdenticalTo(getClass().getResource("HT016424175-combined.xml").openStream()));
    }

    /**
     * "MAB in MARC" with UTF-8, subfield delimiter "$$", but subfield code length 2 (not 3).
     * This is also known as "Aleph MAB with $$ delimited subfields format".
     * Because this format is so deviant from any convention, we need a custom Marc data transformer.
     */
    @Test
    public void testAlephMab() throws Exception {
        String s = "aleph-mab.mrc";
        StreamMatcher.xmlMatch(getClass(), s, ".xml", (inputStream, outputStream) -> {
            try (MarcXchangeWriter writer = new MarcXchangeWriter(outputStream)
                    .setFormat("MAB").setType("Bibliographic")) {
                Marc.builder()
                    .setInputStream(inputStream)
                    .setCharset(StandardCharsets.UTF_8)
                    .setFormat("MAB")
                    .setType("Bibliographic")
                    .setMarcListener(writer)
                    .setRecordLabelFixer(recordLabel ->
                            RecordLabel.builder().from(recordLabel).setSubfieldIdentifierLength(1).build())
                    .setMarcTransformer((builder, label, data) -> {
                        builder.tag(data.length() > 2 ? data.substring(0, 3) : null);
                        if (builder.isControl()) {
                            // tricky: "Aleph MAB" control fields come with a blank indicator, cut it off here.
                            if (data.length() > 4) {
                                builder.value(data.substring(4));
                            }
                        } else {
                            int pos = 3 + label.getIndicatorLength();
                            builder.indicator(data.length() >= pos ? data.substring(3, pos) : null);
                            // tricky: "Aleph MAB" first subfield has no subfield ID! We set a blank ID.
                            // tag len 3 + ind len 1 = 4
                            String subfields = data.length() > 4 ? " " + data.substring(4) : "";
                            for (String value : subfields.split(Pattern.quote("$$"))) {
                                builder.subfield(value.substring(0, 1), value.substring(1));
                            }
                        }
                    })
                    .build()
                    .writeCollection();
            }
        });
    }

    @Test
    public void testAlephMabWithFieldMapper() throws Exception {
        String s = "aleph-mab.mrc";
        StreamMatcher.xmlMatch(getClass(), s, ".mapped.xml", (inputStream, outputStream) -> {
            MarcFieldTransformers transformers = new MarcFieldTransformers();
            MarcFieldTransformer t0 = MarcFieldTransformer.builder()
                    .ignoreSubfieldIds()
                    .fromTo("902$ $ 9", "689$0{r}$a0")
                    .fromTo("902$ $ ", "689$0{r}$a")
                    .operator(HEAD)
                    .build();
            transformers.add(t0);
            try (MarcXchangeWriter writer = new MarcXchangeWriter(outputStream, true)
                    .setFormat("MAB").setType("Bibliographic")) {
                Marc.builder()
                        .setInputStream(inputStream)
                        .setCharset(StandardCharsets.UTF_8)
                        .setFormat("MAB")
                        .setType("Bibliographic")
                        .setMarcListener(writer)
                        .setMarcFieldTransformers(transformers)
                        .setRecordLabelFixer(recordLabel ->
                                RecordLabel.builder().from(recordLabel).setSubfieldIdentifierLength(1).build())
                        .setMarcTransformer((builder, label, data) -> {
                            builder.tag(data.length() > 2 ? data.substring(0, 3) : null);
                            if (builder.isControl()) {
                                // tricky: "Aleph MAB" control fields come with a blank indicator, cut it off here.
                                if (data.length() > 4) {
                                    builder.value(data.substring(4));
                                }
                            } else {
                                int pos = 3 + label.getIndicatorLength();
                                builder.indicator(data.length() >= pos ? data.substring(3, pos) : null);
                                // tricky: "Aleph MAB" first subfield has no subfield ID! We set a blank ID.
                                // tag len 3 + ind len 1 = 4
                                String subfields = data.length() > 4 ? " " + data.substring(4) : "";
                                for (String value : subfields.split(Pattern.quote("$$"))) {
                                    builder.subfield(value.substring(0, 1), value.substring(1));
                                }
                            }
                        })
                        .build()
                        .writeCollection();
            }
        });
    }

    @Test
    public void testAlephPublishSax() throws Exception {
        String s = "DE-605-aleph-publish.xml";
        StreamMatcher.xmlMatch(getClass(), s, ".mapped.xml", (inputStream, outputStream) -> {
            MarcFieldTransformers transformers = new MarcFieldTransformers();
            MarcFieldTransformer t0 = MarcFieldTransformer.builder()
                    .ignoreSubfieldIds()
                    .fromTo("902$ 1$9s", "689$0{r}$0s") // transform MAB to MARC21 subject numbering
                    .drop("903$ 1$a")  // "Permutationsmuster" -> drop
                    .operator(HEAD)
                    .build();
            MarcFieldTransformer t1 = MarcFieldTransformer.builder()
                    // transform periodic fields of MAB "Gesamttitel" to MARC21 "Series added entry - uniform title"
                    .fromTo("451$ 1$a", "830$ 0$t")
                    // transform periodic fields of MAB "Gesamttitel" to MARC21 "Series added entry - uniform title"
                    .fromTo("461$ 1$a", "830$ 0$t")
                    .operator(HEAD)
                    .build();
            MarcFieldTransformer t2 = MarcFieldTransformer.builder()
                    .fromTo("453$ 1$a", "830$ 0$w")
                    .fromTo("455$ 1$a", "830$ 0$v")
                    .fromTo("456$ 1$a", "830$ 0$n")
                    .fromTo("463$ 1$a", "830$ 0$w")
                    .fromTo("465$ 1$a", "830$ 0$v")
                    .fromTo("466$ 1$a", "830$ 0$n")
                    .operator(TAIL)
                    .build();
            transformers.add(t0);
            transformers.add(t1);
            transformers.add(t2);
            MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
            marcValueTransformers.setMarcValueTransformer("LOW$ 1$a", new LOWTransformer());
            try (MarcXchangeWriter writer = new MarcXchangeWriter(outputStream, true)) {
                MarcContentHandler contentHandler = new MarcContentHandler();
                contentHandler.addNamespace("http://www.ddb.de/professionell/mabxml/mabxml-1.xsd");
                contentHandler.setMarcListener(writer);
                Marc marc = Marc.builder()
                        .setInputStream(inputStream)
                        .setCharset(StandardCharsets.UTF_8)
                        .setFormat("AlephXML")
                        .setType("Bibliographic")
                        .setContentHandler(contentHandler)
                        .setMarcFieldTransformers(transformers)
                        .setMarcValueTransformers(marcValueTransformers)
                        .build();
                writer.beginCollection();
                marc.xmlReader().parse(new InputSource(inputStream));
                writer.endCollection();
            }
        });
    }

    private static class LOWTransformer implements MarcValueTransformer {

        @Override
        public String transform(String value) {
            return value.equals("C5001") ? "DE-Bi10" : value;
        }
    }

    private static class Listener implements MarcListener, AutoCloseable {

        private final BufferedWriter writer;

        Listener(OutputStream outputStream, Charset charset) {
            this.writer = new BufferedWriter(new OutputStreamWriter(outputStream, charset));
        }

        @Override
        public void beginCollection() {
        }

        @Override
        public void endCollection() {
        }

        @Override
        public void beginRecord(String format, String type) {
            try {
                writer.append("beginRecord").append("\n");
                writer.append("format=").append(format).append("\n");
                writer.append("type=").append(type).append("\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void leader(RecordLabel label) {
            try {
                writer.append("leader=").append(label.toString()).append("\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void field(MarcField field) {
            try {
                writer.append(field.toString()).append("\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void endRecord() {
            try {
                writer.append("endRecord").append("\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void close() throws IOException {
            writer.close();
        }
    }
}
