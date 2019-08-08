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

import static org.xbib.marc.json.MarcJsonWriter.FORMAT_TAG;
import static org.xbib.marc.json.MarcJsonWriter.LEADER_TAG;
import static org.xbib.marc.json.MarcJsonWriter.TYPE_TAG;

import org.xbib.marc.label.RecordLabel;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A MARC record. This is an extended MARC record augmented with MarcXchange information.
 */
public class MarcRecord extends LinkedHashMap<String, Object> {

    private static final MarcRecord EMPTY = Marc.builder().buildRecord();

    private static final long serialVersionUID = 5305809148724342653L;

    private final String format;

    private final String type;

    private final transient RecordLabel recordLabel;

    private final transient List<MarcField> marcFields;

    /**
     * Create a MARC record. Use {@link Marc.Builder} to create a MARC record.
     *
     * @param format      the format of the record
     * @param type        the type
     * @param recordLabel the record label
     * @param marcFields  the MARC field
     * @param lightweight true if MARC record fields should not be entered into the underlying hash map.
     */
    MarcRecord(String format, String type, RecordLabel recordLabel,
               List<MarcField> marcFields, boolean lightweight) {
        super();
        this.format = format;
        this.type = type;
        this.recordLabel = recordLabel;
        if (recordLabel == null) {
            throw new NullPointerException("record label must not be null");
        }
        this.marcFields = marcFields;
        if (marcFields == null) {
            throw new NullPointerException("fields must not be null");
        }
        if (!lightweight) {
            createMap();
        }
    }

    /**
     * Return the empty MARC record.
     * @return empty MARC record
     */
    public static MarcRecord emptyRecord() {
        return EMPTY;
    }

    /**
     * Return the MARC record format.
     *
     * @return the MARC record format
     */
    public String getFormat() {
        return format;
    }

    /**
     * Return the MARC record type.
     *
     * @return the MARC record type
     */
    public String getType() {
        return type;
    }

    /**
     * Return MARC record label.
     *
     * @return the MARC record label
     */
    public RecordLabel getRecordLabel() {
        return recordLabel;
    }

    /**
     * Return the MARC fields of this record.
     *
     * @return the MARC field list
     */
    public List<MarcField> getFields() {
        return marcFields;
    }

    /**
     * Return the MARC fields of this record with a given tag.
     *
     * @param tag the MARC tag
     * @return the MARC field list matching the given tag.
     */
    public List<MarcField> getFields(String tag) {
        return marcFields.stream().filter(marcField -> marcField.getTag().equals(tag))
                .collect(Collectors.toList());
    }

    /**
     * Return a list of MARC fields of this record where key pattern matches were found.
     *
     * @param pattern the pattern
     * @return a list of MARC fields
     */
    public List<MarcField> filterKey(Pattern pattern) {
        return marcFields.stream()
                .map(field -> field.matchKey(pattern))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Return a list of MARC fields of this record where pattern matches were found.
     *
     * @param pattern the pattern
     * @return a list of MARC fields
     */
    public List<MarcField> filterValue(Pattern pattern) {
        return marcFields.stream().map(field ->
                field.matchValue(pattern)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof MarcRecord)
                && recordLabel.equals(((MarcRecord) obj).getRecordLabel())
                && marcFields.equals(((MarcRecord) obj).getFields());
    }

    @Override
    public int hashCode() {
        return (recordLabel.toString() + marcFields.toString()).hashCode();
    }

    @SuppressWarnings("unchecked")
    private void createMap() {
        put(FORMAT_TAG, format);
        put(TYPE_TAG, type);
        put(LEADER_TAG, recordLabel.toString());
        for (MarcField marcField : marcFields) {
            String tag = marcField.getTag();
            int repeat;
            Map<String, Object> repeatMap;
            if (!containsKey(tag)) {
                repeatMap = new LinkedHashMap<>();
                repeat = 1;
                put(tag, repeatMap);
            } else {
                repeatMap = (Map<String, Object>) get(tag);
                repeat = repeatMap.size() + 1;
            }
            String indicator = marcField.getIndicator();
            if (indicator != null && !indicator.isEmpty()) {
                indicator = indicator.replace(' ', '_');
                Map<String, Object> indicators = new LinkedHashMap<>();
                repeatMap.put(Integer.toString(repeat), indicators);
                if (!indicators.containsKey(indicator)) {
                    indicators.put(indicator, new LinkedHashMap<>());
                }
                Map<String, Object> subfields = (Map<String, Object>) indicators.get(indicator);
                // we may have values instead of subfields, even on non-control fields. See MAB
                if (marcField.getValue() != null && !marcField.getValue().isEmpty()) {
                    repeatMap.put(Integer.toString(repeat), marcField.getValue());
                } else {
                    for (MarcField.Subfield subfield : marcField.getSubfields()) {
                        String code = subfield.getId();
                        code = code.replace(' ', '_');
                        Object subfieldValue = subfields.get(code);
                        if (subfieldValue instanceof List) {
                            List<String> list = (List<String>) subfieldValue;
                            list.add(subfield.getValue());
                        } else if (subfieldValue instanceof String) {
                            List<String> list = new LinkedList<>();
                            list.add((String) subfieldValue);
                            list.add(subfield.getValue());
                            subfields.put(code, list);
                        } else {
                            subfields.put(code, subfield.getValue());
                        }
                    }
                }
            } else {
                repeatMap.put(Integer.toString(repeat), marcField.getValue());
            }
        }
    }
}
