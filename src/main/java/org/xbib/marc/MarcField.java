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

import org.xbib.marc.dialects.mab.MabSubfieldControl;
import org.xbib.marc.label.RecordLabel;

import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A MARC field.
 */
public class MarcField implements Comparable<MarcField> {

    private static final MarcField EMPTY_FIELD = builder().build();

    private static final Subfield EMPTY_SUBFIELD = new Subfield(null, null);

    private static final String EMPTY_STRING = "";

    public static final MarcFieldValidator DEFAULT_VALIDATOR = new StrictMarcFieldValidator();

    public static final MarcFieldValidator STRICT_VALIDATOR = new StrictMarcFieldValidator();

    public static final MarcFieldValidator PERMISSIVE_VALIDATOR = new PermissiveMarcFieldValidator();

    public static final String DELIMITER = "$";

    private final Builder builder;

    private MarcField(Builder builder) {
        this.builder = builder;
    }

    /**
     * Return a build for a MARC field.
     * @return a builder
     */
    public static Builder builder() {
        return new Builder();
    }

    public static MarcField emptyMarcField() {
        return EMPTY_FIELD;
    }

    public static Subfield emptySubfield() {
        return EMPTY_SUBFIELD;
    }

    /**
     * Return the MARC field tag.
     * @return the tag
     */
    public String getTag() {
        return builder.tag;
    }

    /**
     * Return the MARC field indicator, one or more chaarcters.
     * @return the indicator
     */
    public String getIndicator() {
        return builder.indicator;
    }

    /**
     * Return the MARC field position. The position is recorded in the MARC directory.
     * @return position
     */
    public int getPosition() {
        return builder.position;
    }

    /**
     * Return the MARC field length. The length is recorded in the MARC directory.
     * @return the MARC field length
     */
    public int getLength() {
        return builder.length;
    }

    /**
     * Return the subfields associated with this MARC field.
     * @return a list of MARC subfields
     */
    public Deque<Subfield> getSubfields() {
        return builder.subfields;
    }

    /**
     * Return the subfields of this MARC field as Map. Attention: this works only if there are unique subfield IDs.
     * @return the subfields as map
     */
    public Map<String, Object> getSubfieldsAsMap() {
        return builder.subfields.stream().collect(Collectors.toMap(Subfield::getId, Subfield::getValue));
    }

    /**
     * Return the subfields of this MARC field as Map. Attention: this works only if there are unique subfield IDs.
     * @param mergeFunction the merge function
     * @param supplier the supplier
     * @return the subfields as map
     */
    public Map<String, Object> getSubfieldsAsMap(BinaryOperator<Object> mergeFunction, Supplier<Map<String, Object>> supplier) {
        return builder.subfields.stream().collect(Collectors.toMap(Subfield::getId, Subfield::getValue,
                mergeFunction, supplier));
    }

    /**
     * Collect subfield values of a specified collection of subfield IDs.
     * @param subfieldIds the given collection of subfield IDs
     * @return the list of subfield values
     */
    public List<String> getSubfieldValues(Collection<String> subfieldIds) {
        return builder.subfields.stream()
                .filter(subfield -> subfieldIds.contains(subfield.getId()))
                .map(Subfield::getValue)
                .collect(Collectors.toList());
    }

    /**
     * Return all subfields of a given subfield ID. Multiple occurences may occur.
     * @param subfieldId subfield ID
     * @return list of subfields
     */
    public Deque<Subfield> getSubfield(String subfieldId) {
        return builder.subfields.stream()
                .filter(subfield -> subfield.getId().equals(subfieldId))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Return first subfield or null. Avoids NoSuchElementException of java.util.LinkedList.
     * @return first subfield or null
     */
    public Subfield getFirstSubfield() {
        return builder.subfields.isEmpty() ? emptySubfield() : builder.subfields.getFirst();
    }

    public String getFirstSubfieldValue(String subfieldId) {
        Deque<Subfield> deque = getSubfield(subfieldId);
        return deque.isEmpty() ? null : deque.getFirst().getValue();
    }

    /**
     * Return last subfield or null. Avoids NoSuchElementException of java.util.LinkedList.
     * @return last subfield or null
     */
    public Subfield getLastSubfield() {
        return builder.subfields.isEmpty() ? emptySubfield() : builder.subfields.getLast();
    }

    public String getLastSubfieldValue(String subfieldId) {
        Deque<Subfield> deque = getSubfield(subfieldId);
        return deque.isEmpty() ? null : deque.getLast().getValue();
    }

    /**
     * Return the field value of this MAR field. Mostly used for control fields,
     * but also on formats that do not use subfields.
     * @return the field value
     */
    public String getValue() {
        return builder.value;
    }

    /**
     * Return a "top most" value, useful for recovering a control field value from
     * hierarchical structures like JSON formats.
     * @return the value
     */
    public String recoverControlFieldValue() {
        String value = getValue();
        if (value == null || value.isEmpty()) {
            // the control field is disguised as a data field, try lookup value in first subfield of "_"
            value = getFirstSubfieldValue("_");
            // if no value, maybe blank " "?
            if (value == null || value.isEmpty()) {
                value = getFirstSubfieldValue(" ");
            }
            // still no value? Then it is some exotic like MAB with subfield "a"?
            if (value == null || value.isEmpty()) {
                value = getFirstSubfieldValue("a");
            }
        }
        return value;
    }

    /**
     * Returns if this MARC field is a control field.
     * We invoke the isControl() function in the builder because the variable might have to be evaluated.
     * @return true if control field, false if not
     */
    public boolean isControl() {
        return builder.isControl();
    }

    public boolean isControlFieldsDisabled() {
        return builder.disableControlFields;
    }

    /**
     * Returns if this MARC field is empty.
     * @return true if MARC field is empty, false if not
     */
    public boolean isEmpty() {
        return builder.tag == null;
    }

    public boolean isTagValid() {
        return builder.validator.isTagValid(builder.tag);
    }

    public boolean isIndicatorValid() {
        if (builder.isControl()) {
            // ignore indicator check for control fields
            return true;
        }
        if (builder.indicator == null) {
            // we allow no indicator
            return true;
        }
        return builder.validator.isIndicatorValid(builder.indicator);
    }

    public boolean areAllSubfieldsValid() {
        if (isControl()) {
            // for control fields, there are no subfields, disable check
            return true;
        }
        return builder.subfields.stream().allMatch(s -> builder.validator.isSubfieldIdValid(s.getId()));
    }

    /**
     * Checks if the field has a valid tag (if present), a valid indicator (if present), and a valid subfield (if present).
     * @return true if valid
     */
    public boolean isValid() {
        return isTagValid() && isIndicatorValid() && areAllSubfieldsValid();
    }

    /**
     * Check if pattern matches the tag/indicator key  {@code tag '$' indicator}.
     * @param pattern the pattern
     * @return this MARC field if pattern macthes, otherwise null
     */
    public boolean matchesKey(Pattern pattern) {
        return pattern.matcher(toTagIndicatorKey()).matches();
    }

    /**
     * Search for fields that match a pattern.
     * @param pattern the pattern to match
     * @return thhis MARC field if pattern matches, or null if not
     */
    public boolean matchesValue(Pattern pattern) {
        if (builder.value != null && pattern.matcher(builder.value).matches()) {
            return true;
        }
        for (Subfield subfield : builder.subfields) {
            if (pattern.matcher(subfield.getValue()).matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * A MARC field can be denoted by a key, independent of values.
     * This key is a string, consisting of the tag.
     *
     * @return the tag-based key of this MARC field
     */
    public String toTagKey() {
        return builder.tag == null ? EMPTY_STRING : builder.tag;
    }

    /**
     * A MARC field can be denoted by a key, independent of values.
     * This key is a string, consisting of tag and indicator delimited by a dollar sign.
     *
     * @return the tag/indicator-based key of this MARC field
     */
    public String toTagIndicatorKey() {
        return toTagKey() + DELIMITER + (builder.indicator == null ? EMPTY_STRING : builder.indicator);
    }

    /**
     * A MARC field can be denoted by a key, independent of values.
     * This key is a string, consisting of tag, indicator, subfield IDs, delimited by a dollar sign.
     *
     * @return the key of this MARC field
     */
    public String toKey() {
        return toTagIndicatorKey() + DELIMITER + getSubfieldIds();
    }

    /**
     * Return subfield IDs.
     * @return the subfield ID list as a string.
     */
    public String getSubfieldIds() {
        return builder.subfields.stream().map(Subfield::getId).sorted().collect(Collectors.joining());
    }

    @Override
    public int compareTo(MarcField o) {
        return toKey().compareTo(o.toKey());
    }

    @Override
    public boolean equals(Object object) {
        return object == this || object instanceof MarcField && toKey().equals(((MarcField) object).toKey());
    }

    @Override
    public int hashCode() {
        return toKey().hashCode();
    }

    @Override
    public String toString() {
        return toKey() + (builder.hasSubfields() ? getSubfields() : getValue());
    }

    /**
     * MARC field builder.
     * The builder accepts all information required for building a new MARC field.
     */
    public static class Builder {

        private static final String BLANK_STRING = " ";

        private String tag;

        private String indicator;

        private String value;

        private int position;

        private int length;

        private final LinkedList<Subfield> subfields;

        private Boolean isControl;

        private boolean disableControlFields;

        private MarcFieldValidator validator;

        Builder() {
            this.subfields = new LinkedList<>();
            this.position = -1;
            this.length = -1;
            this.validator = DEFAULT_VALIDATOR;
        }

        /**
         * Set the tag.
         * @param tag the tag
         * @return this builder
         */
        public Builder tag(String tag) {
            this.tag = validator.validateTag(tag);
            return this;
        }

        /**
         * Returns the tag.
         * @return the tag
         */
        public String tag() {
            return tag;
        }

        /**
         * Set indicator.
         * @param indicator the indicator string
         * @return this builder
         */
        public Builder indicator(String indicator) {
            this.indicator = validator.validateIndicator(indicator);
            return this;
        }

        /**
         * Returns the indicator.
         * @return the indicator
         */
        public String indicator() {
            return indicator;
        }

        /**
         * Set position.
         * @param position the position
         * @return this builder
         */
        public Builder position(int position) {
            this.position = position;
            return this;
        }

        /**
         * Return position.
         * @return the position
         */
        public int position() {
            return position;
        }

        /**
         * Return length.
         * @return the length
         */
        public int length() {
            return length;
        }

        /**
         * Set length.
         * @param length the length
         * @return this builder
         */
        public Builder length(int length) {
            this.length = length;
            return this;
        }

        /**
         * Set value. The value is used for control/data fields.
         *
         * @param value the value
         * @return this builder
         */
        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder subfield(Subfield subfield) {
            subfields.add(subfield);
            return this;
        }

        /**
         * Set subfield with subfield ID and value.
         * @param subfieldId the subfield ID
         * @param value the subfield value
         * @return this builder
         */
        public Builder subfield(String subfieldId, String value) {
            String id = validator.validateSubfieldId(subfieldId);
            subfields.add(new Subfield(id, value));
            return this;
        }

        /**
         * Convenience method to add a subfield only if the value is not null.
         * @param subfieldId the dubfield ID
         * @param value the subfield value
         * @return this builder
         */
        public Builder subfieldIfNotNull(String subfieldId, String value) {
            if (value != null) {
                String id = validator.validateSubfieldId(subfieldId);
                subfields.add(new Subfield(id, value));
            }
            return this;
        }

        /**
         * Add subfield ID without a value.
         * @param subfieldId the subfield ID
         * @return this builder
         */
        public Builder subfield(String subfieldId) {
            subfield(subfieldId, null);
            return this;
        }

        /**
         * Add subfield ID.
         * @param subfieldId the subfield ID
         * @return this builder
         */
        public Builder subfield(char subfieldId) {
            subfield(Character.toString(subfieldId));
            return this;
        }

        /**
         * Set a list of subfield IDs.
         * @param subfieldIds the subfield IDs
         * @return this builder
         */
        public Builder subfields(String subfieldIds) {
            for (char ch : subfieldIds.toCharArray()) {
                subfield(ch);
            }
            return this;
        }

        /**
         * Set subfield value in the last subfield which was added.
         * @param value the subfield value
         * @return this builder
         */
        public Builder subfieldValue(String value) {
            if (!subfields.isEmpty()) {
                String id = subfields.removeLast().getId();
                subfields.add(new Subfield(id, value));
            }
            return this;
        }

        public Builder value(RecordLabel recordLabel, String value) {
            if (value.length() > 0) {
                int len = recordLabel.getSubfieldIdentifierLength() - 1; /* minus length of US separator char */
                if (!isControl() && len >= 0 && value.length() >= len) {
                    String id = value.substring(0, len);
                    String content = value.substring(len);
                    subfield(id, content);
                } else {
                    value(value);
                }
            }
            return this;
        }

        public Builder value(String format, String type, RecordLabel recordLabel, String value) {
            if (value.length() > 0) {
                int len = recordLabel.getSubfieldIdentifierLength() - 1; /* minus length of US separator char */
                // override in case of MAB, which contains a wild mixture of subfield ID lengths
                if ("MAB".equals(format) && "Titel".equals(type)) {
                    len = MabSubfieldControl.getSubfieldIdLen(tag());
                }
                boolean canDeriveSubfieldId = value.length() > len && len > 0;
                if (canDeriveSubfieldId) {
                    String id = value.substring(0, len);
                    String content = value.substring(len);
                    subfield(id, content);
                } else {
                    subfield(BLANK_STRING, value);
                }
            }
            return this;
        }

        /**
         * Set a new field with help of a record label from raw data.
         *
         * @param format the record format
         * @param type the record type
         * @param recordLabel the record label
         * @param raw raw data (tag plus indicator plus value)
         * @return this builder
         */
        public Builder field(String format, String type, RecordLabel recordLabel, String raw) {
            if (raw.length() >= 3) {
                tag(raw.substring(0, 3));
            }
            if (isControl()) {
                if (raw.length() > 3) {
                    value(raw.substring(3));
                }
            } else {
                int pos = 3 + recordLabel.getIndicatorLength();
                if (raw.length() >= pos) {
                    indicator(raw.substring(3, pos));
                    value(format, type, recordLabel, raw.substring(pos));
                }
            }
            return this;
        }

        /**
         * Copy a MARC field into this builder.
         * @param field the MARC field to copy
         * @return this builder
         */
        public Builder marcField(MarcField field) {
            this.tag = field.getTag();
            this.indicator = field.getIndicator();
            this.position = field.getPosition();
            this.length = field.getLength();
            this.value = field.getValue();
            this.subfields.clear();
            this.subfields.addAll(field.getSubfields());
            return this;
        }

        /**
         * A key is a compact representation of tag/indicator/value that are separated by a separator.
         * @param key the key as string
         * @param separator the separator in the key
         * @param value the value
         * @return this builder
         */
        public Builder key(String key, String separator, Object value) {
            return key(Arrays.asList(key.split(separator)), value);
        }

        @SuppressWarnings("unchecked")
        public Builder key(List<String> key, Object value) {
            switch (key.size()) {
                case 0 -> {
                    if (value instanceof Collection) {
                        Collection<Map.Entry<String, Object>> collection = (Collection<Map.Entry<String, Object>>) value;
                        for (Map.Entry<String, Object> entry : collection) {
                            tag(entry.getKey()).value(entry.getValue().toString());
                        }
                    }
                }
                case 1 -> {
                    tag(key.get(0));
                    if (value instanceof Collection) {
                        Collection<Map.Entry<String, Object>> collection = (Collection<Map.Entry<String, Object>>) value;
                        for (Map.Entry<String, Object> entry : collection) {
                            tag(entry.getKey()).value(entry.getValue().toString());
                        }
                    } else {
                        value(value.toString());
                    }
                }
                case 2 -> {
                    tag(key.get(0));
                    String indicator = key.get(1);
                    if (indicator.isEmpty()) {
                        indicator(" ");
                    } else {
                        indicator(indicator);
                    }
                    if (value instanceof Collection) {
                        Collection<Map.Entry<String, Object>> collection = (Collection<Map.Entry<String, Object>>) value;
                        for (Map.Entry<String, Object> entry : collection) {
                            subfield(entry.getKey(), entry.getValue().toString());
                        }
                    } else {
                        value(value.toString());
                    }
                }
                case 3 -> {
                    tag(key.get(0));
                    String indicator = key.get(1);
                    if (indicator.isEmpty()) {
                        indicator(" ");
                    } else {
                        indicator(indicator);
                    }
                    String subfieldIds = key.get(2);
                    if (subfieldIds.isEmpty()) {
                        subfieldIds = " ";
                    }
                    if (value instanceof Collection) {
                        Collection<Map.Entry<String, Object>> collection = (Collection<Map.Entry<String, Object>>) value;
                        for (Map.Entry<String, Object> entry : collection) {
                            subfield(entry.getKey(), entry.getValue().toString());
                        }
                    } else {
                        subfield(subfieldIds, value.toString());
                    }
                }
                default -> throw new IllegalArgumentException("key specification is invalid: " + key);
            }
            return this;
        }

        public Builder setValidator(MarcFieldValidator validator) {
            this.validator = validator;
            return this;
        }

        /**
         * For some MARC dialects, we may want to disable control field logic completely.
         * @return this builder
         */
        public Builder disableControlFields() {
            this.disableControlFields = true;
            return this;
        }

        /**
         * Is the MARC field a control field?
         *
         * @return true if control field, false if not
         */
        public boolean isControl() {
            if (disableControlFields) {
                return false;
            }
            if (isControl == null) {
                this.isControl = tag != null && tag.length() >= 2 && tag.charAt(0) == '0' && tag.charAt(1) == '0';
            }
            return isControl;
        }

        /**
         * Is the MARC field empty?
         * @return true if empty, false if not
         */
        public boolean isEmpty() {
            return tag == null;
        }

        /**
         * Has this MARC field subfields?
         * @return true if subfields exist
         */
        public boolean hasSubfields() {
            return !subfields.isEmpty();
        }

        /**
         * Build a MARC field.
         * @return the built MARC field.
         */
        public MarcField build() {
            return new MarcField(this);
        }

        @Override
        public String toString() {
            return "tag=" + tag + ",indicator=" + indicator + ",value=" + value + ",subfields=" + subfields;
        }
    }

    /**
     * Helper method to create a subfield.
     * @param id the subfield ID
     * @param value the subfeild value
     * @return the subfield
     */
    public static Subfield subfield(String id, String value) {
        return new Subfield(id, value);
    }

    /**
     * MARC subfield. A subfield consists of an ID and a value.
     */
    public static class Subfield {

        private final String id;

        private final String value;

        private Subfield(String id, String value) {
            this.id = id;
            this.value = value;
        }

        /**
         * Get ID of subfield.
         * @return the subfield ID
         */
        public String getId() {
            return id;
        }

        /**
         * Get value fo subfield.
         * @return the subfield value
         */
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return id + "=" + value;
        }
    }
}
