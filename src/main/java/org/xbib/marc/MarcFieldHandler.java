package org.xbib.marc;

@FunctionalInterface
public interface MarcFieldHandler {

    void field(MarcField marcField);

}
