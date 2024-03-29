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

import java.util.Objects;
import org.xbib.marc.label.RecordLabel;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.xbib.marc.json.MarcJsonWriter.FORMAT_TAG;
import static org.xbib.marc.json.MarcJsonWriter.LEADER_TAG;
import static org.xbib.marc.json.MarcJsonWriter.TYPE_TAG;

/**
 * A MARC record.
 */
public class MarcRecord implements Map<String, Object> {

    private static final MarcRecord EMPTY_RECORD = Marc.builder().buildRecord();

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final DateTimeFormatter SHORT_DATE_FORMAT =
            new DateTimeFormatterBuilder()
                    .appendValueReduced(ChronoField.YEAR_OF_ERA, 2, 2, LocalDate.now().minusYears(75))
                    .appendPattern("MMdd")
                    .toFormatter();

    private Map<String, Object> delegate;

    private String format;

    private String type;

    private transient RecordLabel recordLabel;

    private transient Collection<MarcField> marcFields;

    private MarcRecord(Map<String, Object> delegate) {
        this.delegate = delegate;
    }

    /**
     * Create a MARC record. Use {@link Marc.Builder} to create a MARC record.
     *
     * @param format      the format of the record
     * @param type        the type
     * @param recordLabel the record label
     * @param marcFields  the MARC field
     * @param lightweight true if MARC record fields should not be entered into the underlying hash map.
     * @param comparator  a tag comparator for the underlying map
     */
    public MarcRecord(String format,
                      String type,
                      RecordLabel recordLabel,
                      Collection<MarcField> marcFields,
                      boolean lightweight,
                      Comparator<String> comparator) {
        this.format = format;
        this.type = type;
        this.recordLabel = recordLabel;
        Objects.requireNonNull(recordLabel, "record label must not be null");
        this.marcFields = marcFields;
        this.delegate = lightweight ? Map.of() : createMapFromMarcFields(comparator);
    }

    /**
     * Return the empty MARC record.
     * @return empty MARC record
     */
    public static MarcRecord emptyRecord() {
        return EMPTY_RECORD;
    }

    public static MarcRecord from(Map<String, Object> map) {
        return from(map, MarcField.DEFAULT_VALIDATOR,
                FORMAT_TAG, TYPE_TAG, LEADER_TAG, RecordLabel.EMPTY, Collections.emptyList());
    }

    public static MarcRecord from(Map<String, Object> map, Collection<String> privateTags) {
        return from(map, MarcField.DEFAULT_VALIDATOR,
                FORMAT_TAG, TYPE_TAG, LEADER_TAG, RecordLabel.EMPTY, privateTags);
    }

    public static MarcRecord from(Map<String, Object> map,
                                  MarcFieldValidator validator,
                                  String formatTag,
                                  String typeTag,
                                  String leaderTag,
                                  RecordLabel recordLabel,
                                  Collection<String> privateTags) {
        MarcRecord marcRecord = new MarcRecord(map);
        marcRecord.marcFields = new LinkedList<>();
        Set<String> forbidden = new HashSet<>(privateTags);
        forbidden.add(formatTag);
        forbidden.add(typeTag);
        forbidden.add(leaderTag);
        marcRecord.parseMap(map, "", new LinkedList<>(), forbidden, (key, value) ->
            marcRecord.marcFields.add(MarcField.builder()
                    .setValidator(validator)
                    .key(key, value)
                    .build()));
        if (map.containsKey(formatTag)) {
            marcRecord.format = map.get(formatTag).toString();
        }
        if (map.containsKey(typeTag)) {
            marcRecord.type = map.get(typeTag).toString();
        }
        if (map.containsKey(leaderTag)) {
            marcRecord.recordLabel = RecordLabel.builder().from(map.get(leaderTag).toString()).build();
        } else {
            marcRecord.recordLabel = recordLabel;
        }
        return marcRecord;
    }

    public static Iterable<MarcRecord> from(InputStream inputStream, Charset charset) {
        return Marc.builder().setInputStream(inputStream).setCharset(charset).iterable();
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
    public Collection<MarcField> getFields() {
        return marcFields;
    }

    public LocalDate getCreationDate(LocalDate defaultDate) {
        if (marcFields != null) {
            MarcField marcField = getFirst("008");
            if (marcField != null) {
                String value = marcField.recoverControlFieldValue();
                if (value != null && value.length() >= 6 && value.substring(0, 6).matches("\\d+")) {
                    try {
                        return LocalDate.parse(value.substring(0, 6), SHORT_DATE_FORMAT);
                    } catch (DateTimeException e) {
                        return defaultDate;
                    }
                }
            }
        }
        return defaultDate;
    }

    public LocalDate getLastModificationDate(LocalDate defaultDate) {
        if (marcFields != null) {
            MarcField marcField = getFirst("005");
            if (marcField != null) {
                String value = marcField.recoverControlFieldValue();
                if (value != null && value.length() >= 8 && value.substring(0, 8).matches("\\d+")) {
                    try {
                        return LocalDate.parse(value.substring(0, 8), DATE_FORMAT);
                    } catch (DateTimeException e) {
                        return defaultDate;
                    }
                }
            }
        }
        return defaultDate;
    }

    public void filterFields(Comparator<MarcField> comparator) {
        if (marcFields != null) {
            Stream<MarcField> stream = marcFields.stream();
            if (comparator != null) {
                stream = stream.sorted(comparator);
            }
            marcFields = stream.toList();
        }
    }

    public void filterFields(Predicate<? super MarcField> predicate,
                             Comparator<MarcField> comparator) {
        if (marcFields != null) {
            Stream<MarcField> stream = marcFields.stream();
            if (predicate != null) {
                stream = stream.filter(predicate);
            }
            if (comparator != null) {
                stream = stream.sorted(comparator);
            }
            marcFields = stream.toList();
        }
    }

    public void filterFields(Predicate<? super MarcField> predicate,
                             Stream<MarcField> marcFieldStream,
                             Comparator<MarcField> comparator) {
        if (marcFields != null) {
            Stream<MarcField> stream = marcFields.stream();
            if (predicate != null) {
                stream = stream.filter(predicate);
            }
            if (marcFieldStream != null) {
                stream = Stream.concat(stream, marcFieldStream);
            }
            if (comparator != null) {
                stream = stream.sorted(comparator);
            }
            marcFields = stream.toList();
        }
    }

    /**
     * Filter all MARC fields of this record with a given tag.
     *
     * @param tag the MARC tag
     * @param handler the handler
     */
    public void all(String tag, MarcFieldHandler handler) {
        all(marcField -> marcField.getTag().equals(tag), handler);
    }

    /**
     * Filter all MARC fields of this record with a given tag and indicator.
     * @param tag the tag
     * @param indicator the indicator
     * @param handler the handler
     */
    public void all(String tag, String indicator, MarcFieldHandler handler) {
        all(marcField -> marcField.getTag().equals(tag) && marcField.getIndicator().equals(indicator), handler);
    }

    /**
     * Filter all MARC fields of this record with a given tag and indicator and subfield ID.
     * @param tag the tag
     * @param indicator the indicator
     * @param subfieldId the subfield ID
     * @param handler the handler
     */
    public void all(String tag, String indicator, String subfieldId, MarcFieldHandler handler) {
        all(marcField -> marcField.getTag().equals(tag) &&
                marcField.getIndicator().equals(indicator) &&
                marcField.getSubfieldIds().contains(subfieldId), handler);
    }

    /**
     * Filter all MARC fields of this record with a given key pattern.
     * @param pattern a pattern that must match the key
     * @param handler the handler
     */
    public void all(Pattern pattern, MarcFieldHandler handler) {
        all(field -> field.matchesKey(pattern), handler);
    }

    /**
     * Filter all MARC fields of this record that satisfy a given predicate.
     * @param predicate the predicate
     * @param handler the handler
     */
    public void all(Predicate<? super MarcField> predicate, MarcFieldHandler handler) {
        if (marcFields != null) {
            marcFields.stream().filter(predicate).forEach(handler::field);
        }
    }

    /**
     * Return all MARC fields of this record with a given tag and indicator.
     * @param tag the tag
     * @return a list of MARC fields
     */
    public List<MarcField> getAll(String tag) {
        return getAll(marcField -> marcField.getTag().equals(tag));
    }

    /**
     * Return all MARC fields of this record with a given tag and indicator.
     * @param tag the tag
     * @param indicator the indicator
     * @return a list of MARC fields
     */
    public List<MarcField> getAll(String tag, String indicator) {
        return getAll(marcField -> marcField.getTag().equals(tag) && marcField.getIndicator().equals(indicator));
    }

    /**
     * Return all MARC fields of this record with a given tagm indicator, and subfield ID.
     * @param tag the tag
     * @param indicator the indicator
     * @param subfieldId the subfield ID
     * @return a list of MARC fields
     */
    public List<MarcField> getAll(String tag, String indicator, String subfieldId) {
        return getAll(marcField -> marcField.getTag().equals(tag) &&
                marcField.getIndicator().equals(indicator) &&
                marcField.getSubfieldIds().contains(subfieldId));
    }

    /**
     * Return all MARC fields of this record that satisfy a given predicate.
     * @param predicate the predicate
     * @return a list of MARC fields
     */
    public List<MarcField> getAll(Predicate<? super MarcField> predicate) {
        List<MarcField> list = new LinkedList<>();
        all(predicate, list::add);
        return list;
    }

    /**
     * Filter for the first MARC field of this record with a given tag.
     * @param tag the MARC tag
     * @param handler the handler
     */
    public void first(String tag, MarcFieldHandler handler) {
        first(marcField -> marcField.getTag().equals(tag), handler);
    }

    /**
     * Filter for the first MARC field of this record with a given tag and indicator.
     * @param tag the MARC tag
     * @param indicator the indicator
     * @param handler the handler
     */
    public void first(String tag, String indicator, MarcFieldHandler handler) {
        first(marcField -> marcField.getTag().equals(tag) && marcField.getIndicator().equals(indicator), handler);
    }

    /**
     * Filter for the first MARC field of this record with a given tag and indicator and subfield ID.
     * @param tag the tag
     * @param indicator the indicator
     * @param subfieldId the subfield ID
     * @param handler the handler
     */
    public void first(String tag, String indicator, String subfieldId, MarcFieldHandler handler) {
        first(marcField -> marcField.getTag().equals(tag) &&
                marcField.getIndicator().equals(indicator) &&
                marcField.getSubfieldIds().contains(subfieldId), handler);
    }

    /**
     * Filter the first MARC field of this record that satisfies a given predicate.
     * @param predicate the predicate
     * @param handler the handler
     */
    public void first(Predicate<? super MarcField> predicate, MarcFieldHandler handler) {
        if (marcFields != null) {
            marcFields.stream().filter(predicate).findFirst().ifPresent(handler::field);
        }
    }

    /**
     * Return the first MARC field of this record with a given tag.
     * @param tag the tag
     * @return the MARC field or null
     */
    public MarcField getFirst(String tag) {
        return getFirst(marcField -> marcField.getTag().equals(tag));
    }

    /**
     * Return the first MARC field of this record with a given tag and indicator.
     * @param tag the tag
     * @param indicator the indicator
     * @return the MARC field or null
     */
    public MarcField getFirst(String tag, String indicator) {
        return getFirst(marcField -> marcField.getTag().equals(tag) && marcField.getIndicator().equals(indicator));
    }

    /**
     * Return the first MARC field of this record with a given tag and indicator and subfield ID.
     * @param tag the tag
     * @param indicator the indicator
     * @param subfieldId the subfield ID
     * @return the MARC field or null
     */
    public MarcField getFirst(String tag, String indicator, String subfieldId) {
        return getFirst(marcField -> marcField.getTag().equals(tag) &&
                marcField.getIndicator().equals(indicator) &&
                marcField.getSubfieldIds().contains(subfieldId));
    }

    /**
     * Return the first MARC field of this record that satisfies a given predicate.
     * @param predicate the predicate
     * @return the MARC field or null
     */
    public MarcField getFirst(Predicate<? super MarcField> predicate) {
        final MarcField[] array = new MarcField[1];
        first(predicate, marcField -> array[0] = marcField);
        return array[0];
    }

    /**
     * Filter any MARC field of this record that satisfies a given predicate.
     * @param predicate the predicate
     * @param handler the handler
     */
    public void any(Predicate<? super MarcField> predicate, MarcFieldHandler handler) {
        if (marcFields != null) {
            marcFields.stream().filter(predicate).findAny().ifPresent(handler::field);
        }
    }

    /**
     * Get any MARC field of this record that satisfies a given predicate.
     * @param predicate the predicate
     * @return the MARC field or null
     */
    public MarcField getAny(Predicate<? super MarcField> predicate) {
        final MarcField[] array = new MarcField[1];
        any(predicate, marcField -> array[0] = marcField);
        return array[0];
    }

    public void rebuildMap() {
        rebuildMap(Comparator.naturalOrder());
    }

    public void rebuildMap(Comparator<String> comparator) {
        this.delegate = createMapFromMarcFields(comparator);
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
        return (recordLabel.toString() + (marcFields != null ? marcFields.toString() : "")).hashCode();
    }

    public String toString() {
        return delegate.toString();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> createMapFromMarcFields(Comparator<String> comparator) {
        Map<String, Object> map = comparator!= null ? new TreeMap<>(comparator) : new LinkedHashMap<>();
        if (marcFields == null) {
            return map;
        }
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
        if (format != null) {
            map.put(FORMAT_TAG, format);
        }
        if (type != null) {
            map.put(TYPE_TAG, type);
        }
        if (recordLabel != null && !RecordLabel.EMPTY.equals(recordLabel)) {
            map.put(LEADER_TAG, recordLabel.toString());
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private void parseMap(Map<String, Object> source,
                          String prefix,
                          LinkedList<String> key,
                          Set<String> forbidden,
                          BiConsumer<List<String>, Object> consumer) {
        if (!prefix.isEmpty()) {
            key.addLast(prefix);
        }
        LinkedList<Map.Entry<String, Object>> list = new LinkedList<>();
        source.forEach((k, v) -> {
            // skip our forbidden keys
            if (!forbidden.contains(k)) {
                if (v instanceof Map) {
                    parseMap((Map<String, Object>) v, k, key, forbidden, consumer);
                } else if (v instanceof Collection) {
                    Collection<Object> collection = (Collection<Object>) v;
                    // join into a single map if we have a collection of plain maps
                    Map<String, Object> map = new LinkedHashMap<>();
                    for (Object object : collection) {
                        if (object instanceof Map) {
                            Map<String, Object> m = (Map<String, Object>) object;
                            if (!join(map, m)) {
                                parseMap(m, k, key, forbidden, consumer);
                            }
                        } else {
                            list.add(Map.entry(k, object));
                        }
                    }
                    if (!map.isEmpty()) {
                        parseMap(map, k, key, forbidden, consumer);
                    }
                } else {
                    list.add(Map.entry(k, v));
                }
            }
        });
        if (!list.isEmpty()) {
            consumer.accept(key, list);
        }
        if (!prefix.isEmpty()) {
            key.removeLast();
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean join(Map<String, Object> map1, Map<String, Object> map2) {
        if (isPlainMap(map2)) {
            String key2 = map2.keySet().iterator().next();
            Object value2 = map2.values().iterator().next();
            // collapse values into a single key
            if (map1.containsKey(key2)) {
                Object value1 = map1.get(key2);
                Collection<Object> collection;
                if (value1 instanceof Collection) {
                    collection = (Collection<Object>) value1;
                    collection.add(value2);
                } else {
                    collection = new LinkedList<>();
                    collection.add(value1);
                    collection.add(value2);
                }
                map1.put(key2, collection);
            } else {
                map1.put(key2, value2);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * A "plain" map is a map with exactly one element where the element value is not a map or a collection.
     * This technique is used in Elasticsearch for repeating values with (possibly) the same key.
     * @param map the map to be tested
     * @return true if map is a plain map
     */
    private static boolean isPlainMap(Map<String, Object> map) {
        if (map.size() == 1) {
            Object object = map.values().iterator().next();
            return !(object instanceof Map) && !(object instanceof Collection<?>);
        } else {
            return false;
        }
    }
}
