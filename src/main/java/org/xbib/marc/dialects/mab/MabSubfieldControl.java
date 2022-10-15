package org.xbib.marc.dialects.mab;

import java.util.Map;

/**
 *
 */
public class MabSubfieldControl {

    private static final Map<String, Integer> FIELDS =  Map.of("088", 2, "655", 2, "856", 2);

    private MabSubfieldControl() {
    }

    public static Integer getSubfieldIdLen(String tag) {
        return FIELDS.getOrDefault(tag, 0);
    }
}
