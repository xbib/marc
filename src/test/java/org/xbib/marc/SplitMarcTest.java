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

import org.junit.Assert;
import org.junit.Test;
import org.xbib.marc.transformer.value.MarcValueTransformers;
import org.xbib.marc.xml.MarcXchangeWriter;
import org.xmlunit.matchers.CompareMatcher;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.Normalizer;

/**
 *
 */
public class SplitMarcTest extends Assert {

    @Test
    public void splitMARC() throws Exception {
        String s = "IRMARC8.bin";
        InputStream in = getClass().getResource("/org/xbib/marc//" + s).openStream();
        MarcValueTransformers marcValueTransformers = new MarcValueTransformers();
        marcValueTransformers.setMarcValueTransformer(value -> Normalizer.normalize(value, Normalizer.Form.NFC));
        MarcXchangeWriter writer = new MarcXchangeWriter(true, "build/%d.xml", 2)
                .setMarcValueTransformers(marcValueTransformers);
        Marc.builder()
                .setInputStream(in)
                .setCharset(Charset.forName("ANSEL"))
                .setMarcListener(writer)
                .build()
                .writeCollection();
        File f1 = new File("build/0.xml");
        assertThat(f1, CompareMatcher.isIdenticalTo(getClass().getResource("0.xml").openStream()));
        File f2 = new File("build/1.xml");
        assertThat(f2, CompareMatcher.isIdenticalTo(getClass().getResource("1.xml").openStream()));
        File f3 = new File("build/2.xml");
        assertThat(f3, CompareMatcher.isIdenticalTo(getClass().getResource("2.xml").openStream()));
    }

}
