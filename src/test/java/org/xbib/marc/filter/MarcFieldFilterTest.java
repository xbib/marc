package org.xbib.marc.filter;

import static org.xbib.content.json.JsonXContent.contentBuilder;

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

/**
 * Demo of collecting ISSNs from a MARC file.
 *
 * "issns.mrc" courtesy of Steven Hirren (steven.hirren.gmail.com)
 */
public class MarcFieldFilterTest {

    private static final Logger logger = Logger.getLogger(MarcFieldFilterTest.class.getName());

    @Test
    public void findISSNs() throws IOException {
        Map<String, List<Map<String, String>>> result = new TreeMap<>();
        // set up MARC listener
        MarcListener marcListener = new MarcFieldAdapter() {
            @Override
            public void field(MarcField field) {
                Collection<Map<String, String>> values = field.getSubfields().stream()
                        .filter(f -> matchISSNField(field, f))
                        .map(f -> Collections.singletonMap(f.getId(), f.getValue()))
                        .collect(Collectors.toList());
                if (!values.isEmpty()) {
                    result.putIfAbsent(field.getTag(), new ArrayList<>());
                    List<Map<String, String>> list = result.get(field.getTag());
                    list.addAll(values);
                    result.put(field.getTag(), list);
                }
            }
        };
        // read MARC file
        Marc.builder()
                .setInputStream(getClass().getResource("issns.mrc").openStream())
                .setMarcListener(marcListener)
                .build()
                .writeCollection();
        // collect ISSNs
        List<String> issns = result.values().stream()
                .map(l -> l.stream()
                        .map(m -> m.values().iterator().next())
                        .collect(Collectors.toList()))
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());

        // JSON output
        XContentBuilder builder = contentBuilder().prettyPrint()
                .startObject();
        for (Map.Entry<String, List<Map<String, String>>> entry : result.entrySet()) {
            builder.field(entry.getKey(), entry.getValue());
        }
        builder.array("issns", issns);
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
