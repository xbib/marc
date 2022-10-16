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
package org.xbib.marc.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.xbib.marc.transformer.field.MarcFieldTransformer.Operator.HEAD;
import static org.xbib.marc.transformer.field.MarcFieldTransformer.Operator.TAIL;
import org.junit.jupiter.api.Test;
import org.xbib.marc.MarcField;
import org.xbib.marc.transformer.field.MarcFieldTransformer;
import org.xbib.marc.transformer.field.MarcFieldTransformers;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MarcFieldTransformersTest {

    @Test
    public void simpleTransformers() throws IOException {
        MarcFieldTransformers transformers = new MarcFieldTransformers();
        MarcFieldTransformer t0 = MarcFieldTransformer.builder()
                .fromTo("001$$a", "002$$b")
                .fromTo("003$$a", "002$$b")
                .operator(MarcFieldTransformer.Operator.HEAD)
                .build();
        transformers.add(t0);

        MarcField a0 = MarcField.builder().tag("001").subfield("a", "Hello World 1").build();
        MarcField a1 = MarcField.builder().tag("002").subfield("a", "Hello World 2").build();
        MarcField a2 = MarcField.builder().tag("003").subfield("a", "Hello World 3").build();

        List<MarcField> marcFieldList = Arrays.asList(a0, a1, a2);
        assertEquals("[002$$b[b=Hello World 1], 002$$a[a=Hello World 2], 002$$b[b=Hello World 3]]",
                transformers.transform(marcFieldList).toString());
    }

    @Test
    public void simpleCombinationOfTransformers() throws IOException {
        MarcFieldTransformers transformers = new MarcFieldTransformers();
        MarcFieldTransformer t0 = MarcFieldTransformer.builder()
                .fromTo("663$01$a", "662$01$x")
                .fromTo("663$02$a", "662$02$x")
                .fromTo("663$21$a", "662$21$x")
                .fromTo("663$22$a", "662$22$x")
                .operator(MarcFieldTransformer.Operator.HEAD)
                .build();
        MarcFieldTransformer t1 = MarcFieldTransformer.builder()
                .fromTo("001$$a", "002$$b")
                .fromTo("003$$a", "002$$b")
                .operator(MarcFieldTransformer.Operator.TAIL)
                .build();
        transformers.add(t0);
        transformers.add(t1);

        MarcField a0 = MarcField.builder().tag("001").subfield("a", "Hello World").build();
        MarcField a1 = MarcField.builder().tag("663").indicator("01").subfield("a", "Hello World").build();

        List<MarcField> marcFieldList = Arrays.asList(a0, a1);
        assertEquals("[002$$b[b=Hello World], 662$01$x[x=Hello World]]",
                transformers.transform(marcFieldList).toString());
    }

    @Test
    public void combinationOfTransformers() throws IOException {
        MarcFieldTransformers transformers = new MarcFieldTransformers();
        // the "head" transformations which open a new MARC field
        MarcFieldTransformer t0 = MarcFieldTransformer.builder()
                .fromTo("663$01$a", "662$01$x")
                .fromTo("663$02$a", "662$02$x")
                .fromTo("663$21$a", "662$21$x")
                .fromTo("663$22$a", "662$22$x")
                .fromTo("001$$a", "002$$b")
                .operator(MarcFieldTransformer.Operator.HEAD)
                .build();
        // the "tail" transformations that append to "head" transformations
        MarcFieldTransformer t1 = MarcFieldTransformer.builder()
                .fromTo("003$$a", "002$$b")
                .operator(MarcFieldTransformer.Operator.TAIL)
                .build();
        transformers.add(t0);
        transformers.add(t1);

        MarcField a0 = MarcField.builder().tag("001").subfield("a", "Hello World 1").build();
        MarcField a1 = MarcField.builder().tag("003").subfield("a", "Hello World 2").build();
        MarcField a2 = MarcField.builder().tag("663").indicator("01").subfield("a", "Hello World 3").build();
        List<MarcField> marcFieldList = Arrays.asList(a0, a1, a2);
        assertEquals("[002$$bb[b=Hello World 1, b=Hello World 2], 662$01$x[x=Hello World 3]]",
                transformers.transform(marcFieldList).toString());
    }

    @Test
    public void testManyTails() {
        MarcFieldTransformers transformers = new MarcFieldTransformers();
        MarcFieldTransformer t1 = MarcFieldTransformer.builder()
                .fromTo("451$ 1$a", "830$ 0$t")
                .operator(HEAD)
                .build();
        MarcFieldTransformer t2 = MarcFieldTransformer.builder()
                .fromTo("453$ 1$a", "830$ 0$w")
                .fromTo("455$ 1$a", "830$ 0$v")
                .fromTo("456$ 1$a", "830$ 0$n")
                .operator(TAIL)
                .build();
        transformers.add(t1);
        transformers.add(t2);
        MarcField a1 = MarcField.builder().tag("451").indicator(" 1").subfield("a", "Hello World 1").build();
        MarcField a2 = MarcField.builder().tag("453").indicator(" 1").subfield("a", "Hello World 2").build();
        MarcField a3 = MarcField.builder().tag("455").indicator(" 1").subfield("a", "Hello World 3").build();
        MarcField a4 = MarcField.builder().tag("456").indicator(" 1").subfield("a", "Hello World 4").build();
        List<MarcField> marcFieldList = Arrays.asList(a1, a2, a3, a4);
        assertEquals("[830$ 0$ntvw[t=Hello World 1, w=Hello World 2, v=Hello World 3, n=Hello World 4]]",
                transformers.transform(marcFieldList).toString());
    }

    @Test
    public void testSubjectChainTransformation() {
        MarcFieldTransformers transformers = new MarcFieldTransformers();
        MarcFieldTransformer t0 = MarcFieldTransformer.builder()
                .ignoreSubfieldIds()
                .fromTo("902$ 1$9s", "689$0{r}$0s") // transform MAB to MARC21 subject numbering
                .fromTo("907$ 1$9s", "689$1{r}$0s") // transform MAB to MARC21 subject numbering
                .drop("903$ 1$a")  // "Permutationsmuster" -> drop
                .drop("908$ 1$a")  // "Permutationsmuster" -> drop
                .operator(HEAD)
                .build();
        transformers.add(t0);
        MarcField a1 = MarcField.builder().tag("902").indicator(" 1")
                .subfield("s", "Alkoholismus").subfield("9", "(DE-588)4001220-7").build();
        MarcField a2 = MarcField.builder().tag("902").indicator(" 1")
                .subfield("s", "Notfalltherapie").subfield("9", "(DE-588)4172068-4").build();
        MarcField a3 = MarcField.builder().tag("903").indicator(" 1")
                .subfield("a", "21").build();
        MarcField b1 = MarcField.builder().tag("907").indicator(" 1")
                .subfield("s", "Sucht").subfield("9", "(DE-588)4058361-2").build();
        MarcField b2 = MarcField.builder().tag("907").indicator(" 1")
                .subfield("s", "Notfalltherapie").subfield("9", "(DE-588)4172068-4").build();
        MarcField b3 = MarcField.builder().tag("908").indicator(" 1")
                .subfield("a", "21").build();
        List<MarcField> marcFieldList = Arrays.asList(a1, a2, a3, b1, b2, b3);
        // create sequence 00, 01, 10, 11
        assertEquals("[689$00$9s[s=Alkoholismus, 9=(DE-588)4001220-7], " +
                        "689$01$9s[s=Notfalltherapie, 9=(DE-588)4172068-4], " +
                        "689$10$9s[s=Sucht, 9=(DE-588)4058361-2], " +
                        "689$11$9s[s=Notfalltherapie, 9=(DE-588)4172068-4]]",
                transformers.transform(marcFieldList).toString());
    }

    @Test
    public void testPeriodicFieldGroups() {
        MarcFieldTransformers transformers = new MarcFieldTransformers();
        // the "head" transformations which open a new MARC field
        MarcFieldTransformer t1 = MarcFieldTransformer.builder()
                .fromTo("451$ 1$a", "830$ 0$t")
                .fromTo("461$ 1$a", "830$ 0$t")
                .operator(HEAD)
                .build();
        // the "tail" transformations that append to "head" transformations
        MarcFieldTransformer t2 = MarcFieldTransformer.builder()
                .fromTo("453$ 1$a", "830$ 0$w")
                .fromTo("455$ 1$a", "830$ 0$v")
                .fromTo("456$ 1$a", "830$ 0$n")
                .fromTo("463$ 1$a", "830$ 0$w")
                .fromTo("465$ 1$a", "830$ 0$v")
                .fromTo("466$ 1$a", "830$ 0$n")
                .operator(TAIL)
                .build();
        transformers.add(t1);
        transformers.add(t2);
        MarcField a1 = MarcField.builder().tag("451").indicator(" 1").subfield("a", "Hello World 1").build();
        MarcField a2 = MarcField.builder().tag("453").indicator(" 1").subfield("a", "Hello World 2").build();
        MarcField a3 = MarcField.builder().tag("461").indicator(" 1").subfield("a", "Hello World 3").build();
        MarcField a4 = MarcField.builder().tag("463").indicator(" 1").subfield("a", "Hello World 4").build();
        List<MarcField> marcFieldList = Arrays.asList(a1, a2, a3, a4);
        assertEquals("[830$ 0$tw[t=Hello World 1, w=Hello World 2], 830$ 0$tw[t=Hello World 3, w=Hello World 4]]",
                transformers.transform(marcFieldList).toString());
    }

}
