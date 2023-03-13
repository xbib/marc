package org.xbib.marc.json;

import java.util.LinkedHashMap;
import java.util.Map;

public class MarcMapNode extends LinkedHashMap<CharSequence, Node<?>> implements MapNode {

    public MarcMapNode() {
    }

    @Override
    public Map<CharSequence, Node<?>> get() {
        return this;
    }
}
