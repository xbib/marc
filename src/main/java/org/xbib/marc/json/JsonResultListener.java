package org.xbib.marc.json;

public interface JsonResultListener extends JsonListener {

    Node<?> getResult();
}
