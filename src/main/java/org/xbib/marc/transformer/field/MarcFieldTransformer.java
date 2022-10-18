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
package org.xbib.marc.transformer.field;

import static org.xbib.marc.transformer.field.MarcFieldTransformer.Operator.HEAD;

import org.xbib.marc.MarcField;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
@SuppressWarnings("serial")
public class MarcFieldTransformer extends LinkedHashMap<String, MarcField> {

    private static final Logger logger = Logger.getLogger(MarcFieldTransformer.class.getName());

    // the repeat counter pattern, simple integer
    private static final Pattern REP = Pattern.compile("\\{r\\}");

    // the two-string numeric repeat counter pattern
    private static final Pattern NREP = Pattern.compile("\\{n\\}");

    private final boolean ignoreIndicator;

    private final boolean ignoreSubfieldIds;

    private transient MarcField lastReceived;

    private transient MarcField lastBuilt;

    private int repeatCounter;

    private final Operator operator;

    private MarcFieldTransformer(Map<String, MarcField> map,
                                 boolean ignoreIndicator,
                                 boolean ignoreSubfieldIds,
                                 Operator operator) {
        super(map);
        this.ignoreIndicator = ignoreIndicator;
        this.ignoreSubfieldIds = ignoreSubfieldIds;
        this.operator = operator;
        this.repeatCounter = 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Operator getOperator() {
        return operator;
    }

    public MarcField getLastBuilt() {
        return lastBuilt;
    }

    public void reset() {
        repeatCounter = 0;
        lastReceived = null;
        lastBuilt = null;
    }

    public String getTransformKey(MarcField marcField) {
        String key = ignoreIndicator ? marcField.toTagKey() : ignoreSubfieldIds ?
                marcField.toTagIndicatorKey() : marcField.toKey();
        return containsKey(key) ? key : null;
    }

    public MarcField transform(MarcField marcField) {
        switch (operator) {
            case HEAD: return head(marcField);
            case TAIL: return tail(marcField, lastBuilt);
            default: break;
        }
        return null;
    }

    public MarcField transform(Operator operator, MarcField marcField, String key, MarcField lastBuilt) {
        switch (operator) {
            case HEAD: return head(marcField, key);
            case TAIL: return tail(marcField, key, lastBuilt);
            default: break;
        }
        return null;
    }

    public MarcField head(MarcField marcField) {
        return head(marcField, getTransformKey(marcField));
    }

    public MarcField head(MarcField marcField, String key) {
        if (key == null) {
            return marcField;
        }
        MarcField newMarcField = get(key);
        if (lastReceived != null) {
            String lastKey = getTransformKey(lastReceived);
            if (key.equals(lastKey)) {
                repeatCounter++;
            } else {
                reset();
            }
        }
        lastReceived = marcField;
        MarcField.Builder builder = MarcField.builder();
        builder.tag(newMarcField.getTag()).value(marcField.getValue());
        if (ignoreIndicator) {
            builder.indicator(marcField.getIndicator());
        } else {
            builder.indicator(interpolate(marcField, newMarcField.getIndicator()));
        }
        if (!builder.isEmpty()) {
            if (ignoreSubfieldIds) {
                // just copy subfields as they are
                for (MarcField.Subfield subfield : marcField.getSubfields()) {
                    builder.subfield(subfield.getId(), subfield.getValue());
                }
            } else {
                // transform subfields
                Iterator<MarcField.Subfield> subfields = marcField.getSubfields().iterator();
                Iterator<MarcField.Subfield> newSubfields = newMarcField.getSubfields().iterator();
                while (subfields.hasNext() && newSubfields.hasNext()) {
                    MarcField.Subfield subfield = subfields.next();
                    MarcField.Subfield newSubfield = newSubfields.next();
                    builder.subfield(newSubfield.getId(), subfield.getValue());
                }
            }
        }
        lastBuilt = builder.build();
        return lastBuilt;
    }

    public MarcField tail(MarcField marcField, MarcField appendToThisField) {
        return tail(marcField, getTransformKey(marcField), appendToThisField);
    }

    /**
     * Tail (appending) mode.
     * @param marcField MARC field
     * @param key key for the MARC field
     * @param appendToThisField the MARC field to append to
     * @return transformed MARC field
     */
    public MarcField tail(MarcField marcField, String key, MarcField appendToThisField) {
        if (key == null) {
            return marcField;
        }
        MarcField newMarcField = appendToThisField;
        if (newMarcField == null) {
            newMarcField = get(key);
        }
        if (lastReceived != null) {
            String lastKey = getTransformKey(lastReceived);
            if (key.equals(lastKey)) {
                repeatCounter++;
            } else {
                repeatCounter = 0;
            }
        }
        lastReceived = marcField;
        MarcField.Builder builder = MarcField.builder();
        if (appendToThisField != null) {
            builder.marcField(appendToThisField);
        } else {
            builder.tag(newMarcField.getTag())
                    .value(marcField.getValue());
            if (ignoreIndicator) {
                builder.indicator(marcField.getIndicator());
            } else {
                builder.indicator(interpolate(marcField, newMarcField.getIndicator()));
            }
        }
        if (ignoreSubfieldIds) {
            // just copy subfields as they are
            for (MarcField.Subfield subfield : marcField.getSubfields()) {
                builder.subfield(subfield.getId(), subfield.getValue());
            }
        } else {
            // get the correct MARC field to map subfield IDs
            MarcField marcField1 = get(key);
            Iterator<MarcField.Subfield> subfields = marcField.getSubfields().iterator();
            Iterator<MarcField.Subfield> newSubfields = marcField1.getSubfields().iterator();
            while (subfields.hasNext() && newSubfields.hasNext()) {
                MarcField.Subfield subfield = subfields.next();
                MarcField.Subfield newSubfield = newSubfields.next();
                builder.subfield(newSubfield.getId(), subfield.getValue());
            }
        }
        lastBuilt = builder.build();
        return lastBuilt;
    }

    /**
     * Interpolate variables.
     *
     * @param marcField MARC field
     * @param value the input value
     * @return the interpolated string
     */
    private String interpolate(MarcField marcField, String value) {
        if (value == null) {
            return null;
        }
        Matcher m = REP.matcher(value);
        if (m.find()) {
            return m.replaceAll(Integer.toString(repeatCounter));
        }
        m = NREP.matcher(value);
        if (m.find()) {
            if (repeatCounter > 99) {
                repeatCounter = 99;
                logger.log(Level.WARNING, () -> "counter > 99, overflow in " + marcField);
            }
            return m.replaceAll(String.format("%02d", repeatCounter));
        }
        return value;
    }

    /**
     *
     */
    public enum Operator {
        HEAD, TAIL
    }

    /**
     *
     */
    public static class Builder {

        private final LinkedHashMap<String, MarcField> map;

        private boolean ignoreIndicator = false;

        private boolean ignoreSubfieldIds = false;

        private Operator operator = HEAD;

        private Builder() {
            map = new LinkedHashMap<>();
        }

        public Builder ignoreIndicator() {
            this.ignoreIndicator = true;
            return this;
        }

        public Builder ignoreSubfieldIds() {
            this.ignoreSubfieldIds = true;
            return this;
        }

        public Builder operator(Operator operator) {
            this.operator = operator;
            return this;
        }

        public Builder fromTo(MarcField a, MarcField b) {
            map.put(getTransformKey(a), b);
            return this;
        }

        public Builder fromTo(String a, String b) {
            if (a == null) {
                return this;
            }
            String[] from = a.split(Pattern.quote(MarcField.DELIMITER), -1);
            MarcField.Builder fromBuilder = MarcField.builder();
            switch (from.length) {
                case 1:
                    fromBuilder.tag(from[0]);
                    break;
                case 2:
                    fromBuilder.tag(from[0]);
                    if (!from[1].isEmpty()) {
                        fromBuilder.indicator(from[1]);
                    }
                    break;
                case 3:
                    fromBuilder.tag(from[0]);
                    if (!from[1].isEmpty()) {
                        fromBuilder.indicator(from[1]);
                    }
                    if (!from[2].isEmpty()) {
                        fromBuilder.subfields(from[2]);
                    }
                    break;
                default:
                    break;
            }
            if (b != null) {
                String[] to = b.split(Pattern.quote(MarcField.DELIMITER), -1);
                MarcField.Builder toBuilder = MarcField.builder();
                switch (to.length) {
                    case 1:
                        toBuilder.tag(to[0]);
                        break;
                    case 2:
                        toBuilder.tag(to[0]);
                        if (!to[1].isEmpty()) {
                            toBuilder.indicator(to[1]);
                        }
                        break;
                    case 3:
                        toBuilder.tag(to[0]);
                        if (!to[1].isEmpty()) {
                            toBuilder.indicator(to[1]);
                        }
                        if (!to[2].isEmpty()) {
                            toBuilder.subfields(to[2]);
                        }
                        break;
                    default:
                        break;
                }
                fromTo(fromBuilder.build(), toBuilder.build());
            } else {
                drop(fromBuilder.build());
            }
            return this;
        }

        public Builder drop(MarcField a) {
            map.put(getTransformKey(a), MarcField.emptyMarcField());
            return this;
        }

        public Builder drop(String drop) {
            String[] s = drop.split(Pattern.quote(MarcField.DELIMITER), -1);
            MarcField.Builder builder = MarcField.builder();
            switch (s.length) {
                case 1:
                    builder.tag(s[0]);
                    break;
                case 2:
                    builder.tag(s[0]).indicator(s[1]);
                    break;
                case 3:
                    builder.tag(s[0]).indicator(s[1]).subfields(s[2]);
                    break;
                default:
                    break;
            }
            drop(builder.build());
            return this;
        }

        public Builder drop(Collection<String> drops) {
            for (String drop : drops) {
                drop(drop);
            }
            return this;
        }

        /**
         * Convenience method for processing specs from map.
         * <code>
         *     map : {
         *        "fromtag$fromind$fromsubf" : "totag$toind$tosubf"
         *     }
         * </code>
         * @param map a map of mappable tags
         * @return a MARC field tranformer
         */
        public Builder from(Map<String, String> map) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                fromTo(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public MarcFieldTransformer build() {
            return new MarcFieldTransformer(map, ignoreIndicator, ignoreSubfieldIds, operator);
        }

        private String getTransformKey(MarcField marcField) {
            return ignoreIndicator ? marcField.toTagKey() : ignoreSubfieldIds ?
                    marcField.toTagIndicatorKey() : marcField.toKey();
        }
    }
}
