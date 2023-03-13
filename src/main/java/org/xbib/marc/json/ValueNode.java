package org.xbib.marc.json;

public class ValueNode implements Node<Object> {

    private Object value;

    public ValueNode(Object value) {
        this.value = value;
    }

    @Override
    public int getDepth() {
        return 0;
    }

    public void set(Object value) {
        this.value = value;
    }

    @Override
    public Object get() {
        return value;
    }

    @Override
    public String toString() {
        return value != null ? value.toString() : null;
    }
}
