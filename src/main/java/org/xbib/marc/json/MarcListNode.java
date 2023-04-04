package org.xbib.marc.json;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("serial")
public class MarcListNode extends LinkedList<Node<?>> implements ListNode {

    public MarcListNode() {
    }

    @Override
    public List<Node<?>> get() {
        return this;
    }
}
