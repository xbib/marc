package org.xbib.marc;

@FunctionalInterface
public interface MarcRecordHandler {

    void record(MarcRecord marcRecord);
}
