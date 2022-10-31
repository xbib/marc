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

import static org.xbib.marc.json.MarcJsonWriter.FORMAT_TAG;
import static org.xbib.marc.json.MarcJsonWriter.LEADER_TAG;
import static org.xbib.marc.json.MarcJsonWriter.TYPE_TAG;

import org.xbib.marc.label.RecordLabel;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * A MARC record. This is an extended MARC record augmented with MarcXchange information.
 */
public class MarcRecord implements Map<String, Object> {

    private static final MarcRecord EMPTY_RECORD = Marc.builder().buildRecord();

    private final Map<String, Object> delegate;

    private String format;

    private String type;

    private transient RecordLabel recordLabel;

    private final transient List<MarcField> marcFields;

    private MarcRecord(Map<String, Object> delegate) {
        this.delegate = delegate;
        this.marcFields = new LinkedList<>();
    }

    /**
     * Create a MARC record. Use {@link Marc.Builder} to create a MARC record.
     *
     * @param format      the format of the record
     * @param type        the type
     * @param recordLabel the record label
     * @param marcFields  the MARC field
     * @param lightweight true if MARC record fields should not be entered into the underlying hash map.
     */
    public MarcRecord(String format,
                      String type,
                      RecordLabel recordLabel,
                      List<MarcField> marcFields,
                      boolean lightweight,
                      boolean stable) {
        this.format = format;
        this.type = type;
        this.recordLabel = recordLabel;
        if (recordLabel == null) {
            throw new NullPointerException("record label must not be null");
        }
        this.marcFields = marcFields;
        this.delegate = lightweight ? Map.of() : createMap(stable);
    }

    /**
     * Return the empty MARC record.
     * @return empty MARC record
     */
    public static MarcRecord emptyRecord() {
        return EMPTY_RECORD;
    }

    public static MarcRecord from(Map<String, Object> map) {
        return from(map, FORMAT_TAG, TYPE_TAG, LEADER_TAG, RecordLabel.EMPTY);
    }

    public static MarcRecord from(Map<String, Object> map,
                                  String formatTag,
                                  String typeTag,
                                  String leaderTag,
                                  RecordLabel recordLabel) {
        MarcRecord marcRecord = new MarcRecord(map);
        marcRecord.parseMap(map, ".", "", (key, value) ->
            marcRecord.marcFields.add(MarcField.builder().key(key, "\\.", value).build()));
        if (map.containsKey(formatTag)) {
            marcRecord.format = map.get(formatTag).toString();
        }
        if ( map.containsKey(typeTag)) {
            marcRecord.type = map.get(typeTag).toString();
        }
        if (map.containsKey(leaderTag)) {
            marcRecord.recordLabel = RecordLabel.builder().from(map.get(leaderTag).toString()).build();
        } else {
            marcRecord.recordLabel = recordLabel;
        }
        return marcRecord;
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
     * Filter the MARC fields of this record with a given tag.
     *
     * @param tag the MARC tag
     */
    public void filter(String tag, MarcFieldHandler handler) {
        filter(marcField -> marcField.getTag().equals(tag), handler);
    }

    public void filter(String tag, String indicator, MarcFieldHandler handler) {
        filter(marcField -> marcField.getTag().equals(tag) && marcField.getIndicator().equals(indicator), handler);
    }

    public void filter(String tag, String indicator, String subfieldId, MarcFieldHandler handler) {
        filter(marcField -> marcField.getTag().equals(tag) &&
                marcField.getIndicator().equals(indicator) &&
                marcField.getSubfieldIds().contains(subfieldId), handler);
    }

    public void filter(Pattern pattern, MarcFieldHandler handler) {
        filter(field -> field.matchesKey(pattern), handler);
    }

    public void filter(Predicate<? super MarcField> predicate, MarcFieldHandler handler) {
        marcFields.stream().filter(predicate).forEach(handler::field);
    }

    public void filterFirst(Predicate<? super MarcField> predicate, MarcFieldHandler handler) {
        marcFields.stream().filter(predicate).findFirst().ifPresent(handler::field);
    }

    public MarcField getFirst(String tag) {
        return getFirst(marcField -> marcField.getTag().equals(tag));
    }

    public MarcField getFirst(String tag, String indicator) {
        return getFirst(marcField -> marcField.getTag().equals(tag) && marcField.getIndicator().equals(indicator));
    }

    public MarcField getFirst(String tag, String indicator, String subfieldId) {
        return getFirst(marcField -> marcField.getTag().equals(tag) &&
                marcField.getIndicator().equals(indicator) &&
                marcField.getSubfieldIds().contains(subfieldId));
    }

    public MarcField getFirst(Predicate<? super MarcField> predicate) {
        final MarcField[] array = new MarcField[1];
        filterFirst(predicate, marcField -> array[0] = marcField);
        return array[0];
    }

    public List<MarcField> getAll(String tag) {
        return getAll(marcField -> marcField.getTag().equals(tag));
    }

    public List<MarcField> getAll(String tag, String indicator) {
        return getAll(marcField -> marcField.getTag().equals(tag) && marcField.getIndicator().equals(indicator));
    }

    public List<MarcField> getAll(String tag, String indicator, String subfieldId) {
        return getAll(marcField -> marcField.getTag().equals(tag) &&
                marcField.getIndicator().equals(indicator) &&
                marcField.getSubfieldIds().contains(subfieldId));
    }

    public List<MarcField> getAll(Predicate<? super MarcField> predicate) {
        List<MarcField> list = new LinkedList<>();
        filter(predicate, list::add);
        return list;
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return delegate.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return delegate.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return delegate.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        delegate.putAll(m);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Set<String> keySet() {
        return delegate.keySet();
    }

    @Override
    public Collection<Object> values() {
        return delegate.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return delegate.entrySet();
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

    public String toString() {
        return delegate.toString();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> createMap(boolean stable) {
        Map<String, Object> map = stable ? new TreeMap<>() : new LinkedHashMap<>();
        map.put(FORMAT_TAG, format);
        map.put(TYPE_TAG, type);
        map.put(LEADER_TAG, recordLabel.toString());
        for (MarcField marcField : marcFields) {
            String tag = marcField.getTag();
            int repeat;
            Map<String, Object> repeatMap;
            if (!map.containsKey(tag)) {
                repeatMap = new LinkedHashMap<>();
                repeat = 1;
                map.put(tag, repeatMap);
            } else {
                repeatMap = (Map<String, Object>) map.get(tag);
                repeat = repeatMap.size() + 1;
            }
            String indicator = marcField.getIndicator();
            if (indicator != null && !indicator.isEmpty()) {
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
        return map;
    }

    @SuppressWarnings("unchecked")
    private void parseMap(Map<String, Object> source,
                          String separator, String prefix,
                          BiConsumer<String, Object> consumer) {
        List<Map.Entry<String, Object>> list = new LinkedList<>();
        source.forEach((k, v) -> {
            if (v instanceof Map) {
                parseMap((Map<String, Object>) v, separator, prefix + k + separator, consumer);
            } else if (v instanceof Collection) {
                Collection<Object> collection = (Collection<Object>) v;
                for (Object object : collection) {
                    if (object instanceof Map) {
                        parseMap((Map<String, Object>) object, separator, prefix + k + separator, consumer);
                    } else {
                        list.add(Map.entry(k, object));
                    }
                }
            } else {
                list.add(Map.entry(k, v));
            }
        });
        if (!list.isEmpty()) {
            consumer.accept(prefix, list);
        }
    }
}
