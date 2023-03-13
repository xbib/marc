package org.xbib.marc.json;

import java.util.Deque;
import java.util.LinkedList;

public class MarcJsonListener implements JsonResultListener {

    private Node<?> node;

    private final Deque<Node<?>> stack = new LinkedList<>();

    private final ValueNode NULL_NODE = new ValueNode(null);

    private final ValueNode TRUE_NODE = new ValueNode(Boolean.TRUE);

    private final ValueNode FALSE_NODE = new ValueNode(Boolean.FALSE);

    public MarcJsonListener() {
    }

    @Override
    public Node<?> getResult() {
        return node;
    }

    @Override
    public void begin() {
        stack.clear();
    }

    @Override
    public void end() {
    }

    @Override
    public void onNull() {
        valueNode(NULL_NODE);
    }

    @Override
    public void onTrue() {
        valueNode(TRUE_NODE);
    }

    @Override
    public void onFalse() {
        valueNode(FALSE_NODE);
    }

    @Override
    public void onKey(CharSequence key) {
        stack.push(new KeyNode(key));
    }

    @Override
    public void onValue(CharSequence value) {
        valueNode(new ValueNode(value));
    }

    @Override
    public void onLong(Long value) {
        valueNode(new ValueNode(value));
    }

    @Override
    public void onDouble(Double value) {
        valueNode(new ValueNode(value));
    }

    @Override
    public void beginCollection() {
        stack.push(new MarcListNode());
    }

    @Override
    public void endCollection() {
        node = stack.pop();
        tryAppend(node);
    }

    @Override
    public void beginMap() {
        stack.push(new MarcMapNode());
    }

    @Override
    public void endMap() {
        node = stack.pop();
        tryAppend(node);
    }

    private void valueNode(ValueNode valueNode) {
        if (!tryAppend(valueNode)) {
            stack.push(valueNode);
            node = valueNode;
        }
    }

    private boolean tryAppend(Node<?> node) {
        if (!stack.isEmpty()) {
            if (stack.peek() instanceof MarcListNode listNode) {
                listNode.add(node);
                return true;
            } else if (stack.peek() instanceof KeyNode) {
                KeyNode keyNode = (KeyNode) stack.pop();
                MarcMapNode mapNode = (MarcMapNode) stack.peek();
                mapNode.put(keyNode.get(), node);
                return true;
            }
        }
        return false;
    }
}
