package org.xbib.marc;

import java.util.Iterator;

public interface MarcRecordIterator extends Iterator<MarcRecord> {

    long getTotalNumberOfRecords();
}
