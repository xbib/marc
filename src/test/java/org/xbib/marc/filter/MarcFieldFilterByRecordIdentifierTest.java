package org.xbib.marc.filter;

import org.junit.Test;
import org.xbib.content.XContentBuilder;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcField;
import org.xbib.marc.MarcFieldAdapter;
import org.xbib.marc.MarcListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.xbib.content.json.JsonXContent.contentBuilder;

/**
 * Demo of collecting ISSNs by record identifier from a MARC file.
 *
 * "issns.mrc" courtesy of Steven Hirren (steven.hirren.gmail.com)
 */
public class MarcFieldFilterByRecordIdentifierTest {

    private static final Logger logger = Logger.getLogger(MarcFieldFilterByRecordIdentifierTest.class.getName());

    @Test
    public void findISSNs() throws IOException {
        Map<String, Map<String, List<Map<String, String>>>> result = new TreeMap<>();
        // set up MARC listener
        MarcListener marcListener = new MarcFieldAdapter() {
            String recordIdentifier;
            Map<String, List<Map<String, String>>> fields;
            @Override
            public void field(MarcField field) {
                if ("001".equals(field.getTag())) {
                    recordIdentifier = field.getValue();
                    fields = new TreeMap<>();
                }
                Collection<Map<String, String>> values = field.getSubfields().stream()
                        .filter(f -> matchISSNField(field, f))
                        .map(f -> Collections.singletonMap(f.getId(), f.getValue()))
                        .collect(Collectors.toList());
                if (!values.isEmpty()) {
                    fields.putIfAbsent(field.getTag(), new ArrayList<>());
                    List<Map<String, String>> list = fields.get(field.getTag());
                    list.addAll(values);
                    fields.put(field.getTag(), list);
                }
            }
            @Override
            public void endRecord() {
                result.put(recordIdentifier, fields);
            }
        };
        // read MARC file
        Marc.builder()
                .setInputStream(getClass().getResource("issns.mrc").openStream())
                .setMarcListener(marcListener)
                .build()
                .writeCollection();

        // collect all ISSNs from all records and all fields and make them unique
        List<? super Map<String, String>> issns =
                result.values().stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .collect(Collectors.toList()).stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList()).stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());

        // JSON output
        XContentBuilder builder = contentBuilder().prettyPrint()
                .startObject();
        for (Map.Entry<String, Map<String, List<Map<String, String>>>> entry : result.entrySet()) {
            builder.field(entry.getKey(), entry.getValue());
        }
        builder.field("issns", issns);
        builder.endObject();

        logger.log(Level.INFO, builder.string());
    }

    private static boolean matchISSNField(MarcField field, MarcField.Subfield subfield) {
        switch (field.getTag()) {
            case "011": {
                return "a".equals(subfield.getId()) || "f".equals(subfield.getId());
            }
            case "421":
            case "451":
            case "452":
            case "488":
                return "x".equals(subfield.getId());
        }
        return false;
    }
}
