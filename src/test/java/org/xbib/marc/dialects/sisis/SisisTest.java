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

import org.junit.Assert;
import org.junit.Test;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcXchangeConstants;
import org.xbib.marc.transformer.field.MarcFieldTransformer;
import org.xbib.marc.transformer.field.MarcFieldTransformers;
import org.xbib.marc.xml.MarcXchangeWriter;
import org.xmlunit.matchers.CompareMatcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 *
 */
public class SisisTest extends Assert {

    /**
     * Test file donated from DE-A96.
     *
     * @throws Exception if test fails
     */
    @Test
    public void testSisis() throws Exception {
        String s = "unloaddipl";
        InputStream in = getClass().getResource(s).openStream();
        StringWriter sw = new StringWriter();
        try (MarcXchangeWriter writer = new MarcXchangeWriter(sw)
                .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)) {
            Marc marc = Marc.builder()
                    .setInputStream(in)
                    .setCharset(StandardCharsets.UTF_8)
                    .setMarcListener(writer)
                    .build();
            long l = marc.wrapIntoCollection(marc.sisis());
            assertEquals(36353, l);
        }
        assertThat(sw.toString(), CompareMatcher.isIdenticalTo(getClass().getResource(s + ".xml").openStream()));
    }

    /**
     * Test file donated from DE-836.
     *
     * @throws Exception if test fails
     */
    @Test
    public void testSisisMapped() throws Exception {
        String s = "testTit.tit";
        InputStream in = getClass().getResource(s).openStream();
        File file = File.createTempFile(s, ".xml");
        file.deleteOnExit();
        FileOutputStream out = new FileOutputStream(file);
        MarcFieldTransformers transformers = new MarcFieldTransformers();
        MarcFieldTransformer t0 = MarcFieldTransformer.builder()
                .fromTo("663$01$a", "662$01$x")
                .fromTo("663$02$a", "662$02$x")
                .fromTo("663$21$a", "662$21$x")
                .fromTo("663$22$a", "662$22$x")
                .operator(MarcFieldTransformer.Operator.HEAD)
                .build();
        transformers.add(t0);
        try (MarcXchangeWriter writer = new MarcXchangeWriter(out, true)
                .setFormat(MarcXchangeConstants.MARCXCHANGE_FORMAT)) {
            Marc marc = Marc.builder()
                    .setInputStream(in)
                    .setCharset(StandardCharsets.UTF_8)
                    .setMarcListener(writer)
                    .setMarcFieldTransformers(transformers)
                    .build();
            long l = marc.wrapIntoCollection(marc.sisisCRLF());
            assertEquals(1246, l);
        }
        assertThat(file, CompareMatcher.isIdenticalTo(getClass().getResource(s + ".xml").openStream()));
    }
}
