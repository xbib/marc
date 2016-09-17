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

import org.xbib.marc.label.RecordLabel;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.regex.Pattern;

/**
 * A MARC field.
 */
public class MarcField implements Comparable<MarcField> {

    public static final MarcField EMPTY = builder().build();

    public static final String KEY_DELIMITER = "$";

    private static final String EMPTY_STRING = "";

    private final String tag;

    private final String indicator;

    private final int position;

    private final int length;

    private final String value;

    private final String subfieldIds;

    private final LinkedList<Subfield> subfields;

    private final boolean iscontrol;

    private MarcField(String tag, String indicator, int position, int length,
                      String value, LinkedList<Subfield> subfields, String subfieldIds,
                      boolean iscontrol) {
        this.tag = tag;
        this.indicator = indicator;
        this.position = position;
        this.length = length;
        this.value = value;
        this.subfields = subfields;
        this.subfieldIds = subfieldIds;
        this.iscontrol = iscontrol;
    }

    /**
     * Return a build for a MARC field.
     * @return a builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Return the MARC field tag.
     * @return the tag
     */
    public String getTag() {
        return tag;
    }

    /**
     * Return the MARC field indicator, one or more chaarcters.
     * @return the indicator
     */
    public String getIndicator() {
        return indicator;
    }

    /**
     * Return the MARC field position. The position is recorded in the MARC directory.
     * @return position
     */
    public int getPosition() {
        return position;
    }

    /**
     * Return the MARC field length. The length is recorded in the MARC directory.
     * @return the MARC field length
     */
    public int getLength() {
        return length;
    }

    /**
     * Return the subfields associated with this MARC field.
     * @return a list of MARC subfields
     */
    public LinkedList<Subfield> getSubfields() {
        return subfields;
    }

    /**
     * Return the field value of this MAR field. Mostly used for control fields,
     * but also on formats that do not use subfields.
     * @return the field value
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns if this MARC field is a control field.
     * @return true if control field, false if not
     */
    public boolean isControl() {
        return iscontrol;
    }

    /**
     * Returns if this MARC field is empty.
     * @return true if MARC field is empty, false if not
     */
    public boolean isEmpty() {
        return tag == null;
    }

    public boolean isTagValid() {
        return tag.length() == 3
                && ((tag.charAt(0) >= '0' && tag.charAt(0) <= '9')
                || (tag.charAt(0) >= 'A' && tag.charAt(0) <= 'Z'))
                && ((tag.charAt(1) >= '0' && tag.charAt(1) <= '9')
                || (tag.charAt(1) >= 'A' && tag.charAt(1) <= 'Z'))
                && ((tag.charAt(2) >= '0' && tag.charAt(2) <= '9')
                || (tag.charAt(2) >= 'A' && tag.charAt(2) <= 'Z'));
    }

    public boolean isIndicatorValid() {
        if (isControl()) {
            return true;
        }
        boolean b = indicator.length() <= 9;
        for (int i = 0; i < indicator.length(); i++) {
            b = indicator.charAt(i) == ' '
                    || (indicator.charAt(i) >= '0' && indicator.charAt(i) <= '9')
                    || (indicator.charAt(i) >= 'a' && indicator.charAt(i) <= 'z')
                    || (indicator.charAt(i) >= 'A' && indicator.charAt(i) <= 'Z')
                    || indicator.charAt(i) == '@'; // for our PICA hack
            if (!b) {
                break;
            }
        }
        return b;
    }

    public boolean isSubfieldValid() {
        if (isControl()) {
            return true;
        }
        boolean b = true;
        for (int i = 0; i < subfieldIds.length(); i++) {
            b = subfieldIds.charAt(i) == ' '
                    || (subfieldIds.charAt(i) >= '0' && subfieldIds.charAt(i) <= '9')
                    || (subfieldIds.charAt(i) >= 'a' && subfieldIds.charAt(i) <= 'z')
                    || (subfieldIds.charAt(i) >= 'A' && subfieldIds.charAt(i) <= 'Z') // can appear in german MARC
                    || subfieldIds.charAt(i) == '$' // can appear in german MARC
                    || subfieldIds.charAt(i) == '=' // can appear in german MARC
                    ;
            if (!b) {
                break;
            }
        }
        return b;
    }

    public boolean isValid() {
        return isTagValid() && isIndicatorValid() && isSubfieldValid();
    }

    /**
     * Check if pattern matches the tag/indicator key  {@code tag '$' indicator}.
     * @param pattern the pattern
     * @return this MARC field if pattern macthes, otherwise null
     */
    public MarcField matchKey(Pattern pattern) {
        return pattern.matcher(toTagIndicatorKey()).matches() ? this : null;
    }

    /**
     * Search for fields that match a pattern.
     * @param pattern the pattern to match
     * @return thhis MARC field if pattern matches, or null if not
     */
    public MarcField matchValue(Pattern pattern) {
        if (value != null && pattern.matcher(value).matches()) {
            return this;
        }
        for (Subfield subfield : subfields) {
            if (pattern.matcher(subfield.getValue()).matches()) {
                return this;
            }
        }
        return null;
    }

    /**
     * A MARC field can be denoted by a key, independent of values.
     * This key is a string, consisting of tag, indicator, subfield IDs, delimited by a dollar sign.
     *
     * @return the key of this MARC field
     */
    public String toKey() {
        return (tag == null ? EMPTY_STRING : tag) + KEY_DELIMITER + (indicator == null ? EMPTY_STRING : indicator) +
                KEY_DELIMITER + subfieldIds;
    }

    /**
     * A MARC field can be denoted by a key, independent of values.
     * This key is a string, consisting of tag and indicator delimited by a dollar sign.
     *
     * @return the tag/indicator-based key of this MARC field
     */
    public String toTagIndicatorKey() {
        return (tag == null ? EMPTY_STRING : tag) + KEY_DELIMITER + (indicator == null ? EMPTY_STRING : indicator);
    }

    /**
     * A MARC field can be denoted by a key, independent of values.
     * This key is a string, consisting of the tag.
     *
     * @return the tag-based key of this MARC field
     */
    public String toTagKey() {
        return tag == null ? EMPTY_STRING : tag;
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
        return toKey() + (getValue() != null && !getValue().isEmpty() ? getValue() : "")
                + (!getSubfields().isEmpty() ? getSubfields() : "");
    }

    /**
     * MARC field builder. The builder accepts all information required for building
     * a new MARC field.
     */
    public static class Builder {

        private String tag;

        private String indicator;

        private int position;

        private int length;

        private String value;

        private LinkedList<Subfield> subfields;

        private LinkedList<String> subfieldIds;

        Builder() {
            this.subfields = new LinkedList<>();
            this.subfieldIds = new SubfieldIds();
            this.position = -1;
            this.length = -1;
        }

        /**
         * Set the tag.
         * @param tag the tag
         * @return this builder
         */
        public Builder tag(String tag) {
            this.tag = tag;
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
            // check if indicators are "-" (like Aleph does). Replace with blank.
            this.indicator = indicator != null ? indicator.replace('-', ' ') : null;
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
         * Set (non-subfield) value.
         * @param value the value
         * @return this builder
         */
        public Builder value(String value) {
            this.value = value;
            return this;
        }

        /**
         * Set subfield with ID and value.
         * @param subfieldId the subfield ID
         * @param value the subfield value
         * @return this builder
         */
        public Builder subfield(String subfieldId, String value) {
            this.subfields.add(new Subfield(subfieldId, value));
            subfieldIds.add(subfieldId);
            return this;
        }

        /**
         * Set subfield ID.
         * @param subfieldId the subfield ID
         * @return this builder
         */
        public Builder subfield(String subfieldId) {
            this.subfields.add(new Subfield(subfieldId, null));
            subfieldIds.add(subfieldId);
            return this;
        }

        /**
         * Set subfield ID.
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
         * Set subfield value to the last subfield which was denoted by a subfield ID.
         * @param value the subfield value
         * @return this builder
         */
        public Builder subfieldValue(String value) {
            Subfield subfield = this.subfields.removeLast();
            this.subfields.add(new Subfield(subfield.getId(), value));
            return this;
        }

        /**
         * Set subfield with help of record label information from raw data.
         * @param label the record label
         * @param raw the raw data
         * @return this builder
         */
        public Builder subfield(RecordLabel label, String raw) {
            // subtract 1 because subfield identifier length includes US separator character
            int subfieldidlen = label.getSubfieldIdentifierLength() - 1;
            if (subfieldidlen > 0 && raw.length() >= subfieldidlen) {
                String subfieldId = raw.substring(0, subfieldidlen);
                subfields.add(new Subfield(subfieldId, raw.substring(subfieldidlen)));
                subfieldIds.add(subfieldId);
            }
            return this;
        }

        /**
         * Set a new data field with help of a record label from raw data.
         * @param label the record label
         * @param raw raw data
         * @return this builder
         */
        public Builder field(RecordLabel label, String raw) {
            this.tag = raw.length() > 2 ? raw.substring(0, 3) : null;
            if (isControl()) {
                if (raw.length() > 3) {
                    value(raw.substring(3));
                }
            } else {
                int pos = 3 + label.getIndicatorLength();
                this.indicator = raw.length() >= pos ? raw.substring(3, pos) : null;
                int subfieldidlen = label.getSubfieldIdentifierLength();
                if (raw.length() >= pos + subfieldidlen) {
                    String subfieldId = raw.substring(pos, pos + subfieldidlen);
                    this.subfields.add(new Subfield(subfieldId, raw.substring(pos + subfieldidlen)));
                    subfieldIds.add(subfieldId);
                }
            }
            return this;
        }

        /**
         * Copy a MARC field.
         * @param field the MARC field to copy
         * @return this builder
         */
        public Builder marcField(MarcField field) {
            this.tag = field.getTag();
            this.indicator = field.getIndicator();
            this.position = field.getPosition();
            this.length = field.getLength();
            this.value = field.getValue();
            this.subfields = new LinkedList<>(field.getSubfields());
            for (Subfield subfield : subfields) {
                subfieldIds.add(subfield.getId());
            }
            return this;
        }

        /**
         * Is the MARC field a control field?
         * @return true if control field, false if not
         */
        public boolean isControl() {
            return tag != null && tag.charAt(0) == '0' && tag.charAt(1) == '0';
        }

        /**
         * Is the MARC field empty?
         * @return true if empty, false if not
         */
        public boolean isEmpty() {
            return tag == null;
        }

        /**
         * Build a MARC field.
         * @return the built MARC field.
         */
        public MarcField build() {
            return new MarcField(tag, indicator, position, length,
                    value, subfields, subfieldIds.toString(), isControl());
        }
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

    private static class SubfieldIds extends LinkedList<String> {

        private static final long serialVersionUID = 7016733919690084153L;

        /**
         * Insertion sort. This is considered faster than sorting afterwards,
         * especially for short lists (we have << 10 subfields at average in a field).
         * @param string the string to insert
         * @return true if collection changed
         */
        @Override
        public boolean add(String string) {
            ListIterator<String> it = listIterator();
            boolean added = false;
            while (it.hasNext()) {
                if (it.next().compareTo(string) > 0) {
                    it.previous();
                    it.add(string);
                    added = true;
                    break;
                }
            }
            if (!added) {
                it.add(string);
            }
            return true;
        }

        @Override
        public String toString() {
            // comma-less appearance
            StringBuilder sb = new StringBuilder();
            for (String s : this) {
                sb.append(s);
            }
            return sb.toString();
        }
    }

}
