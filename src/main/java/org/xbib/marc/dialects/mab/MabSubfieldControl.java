package org.xbib.marc.dialects.mab;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class MabSubfieldControl {

    private static final Map<String, Integer> FIELDS = new HashMap<>();
    static {
        // MARC-like fields with subfield structure
        FIELDS.put("088", 2);
        FIELDS.put("655", 2);
        FIELDS.put("856", 2);
    }

    public static Integer getSubfieldIdLen(String tag) {
        return FIELDS.getOrDefault(tag, 0);
    }
}
