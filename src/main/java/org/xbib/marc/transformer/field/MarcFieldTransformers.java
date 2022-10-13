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
package org.xbib.marc.transformer.field;

import org.xbib.marc.MarcField;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("serial")
public class MarcFieldTransformers extends LinkedList<MarcFieldTransformer> {

    public MarcFieldTransformers() {
    }

    public List<MarcField> transform(List<MarcField> marcFields) {
        LinkedList<MarcField> list = new LinkedList<>();
        // lastBuilt allows us to access the last MarcField built across different MarcFieldTransformer
        MarcField lastBuilt = null;
        // critial area - marc fields must not change here - so we can not reuse transformes by multiple threads
        for (MarcField marcField : marcFields) {
            boolean found = false;
            String key;
            for (MarcFieldTransformer marcFieldTransformer : this) {
                MarcFieldTransformer.Operator op = marcFieldTransformer.getOperator();
                key = marcFieldTransformer.getTransformKey(marcField);
                if (key != null) {
                    MarcField transformedMarcField = marcFieldTransformer.
                            transform(op, marcField, key, lastBuilt);
                    if (!transformedMarcField.equals(MarcField.emptyMarcField())) {
                        if (op == MarcFieldTransformer.Operator.TAIL && list.size() > 0) {
                            list.removeLast(); // tail operation means to nullify previous result
                        }
                        list.add(transformedMarcField);
                    }
                    found = true;
                    lastBuilt = marcFieldTransformer.getLastBuilt();
                    break;
                }
            }
            if (!found) {
                list.add(marcField);
            }
        }
        return list;
    }

    public void reset() {
        for (MarcFieldTransformer transformer : this) {
            transformer.reset();
        }
    }
}
