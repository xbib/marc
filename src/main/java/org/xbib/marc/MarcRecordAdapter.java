package org.xbib.marc;

import org.xbib.marc.label.RecordLabel;

/**
 *
 */
public class MarcRecordAdapter implements MarcListener {

    private final MarcRecordListener marcRecordListener;

    private Marc.Builder builder;

    public MarcRecordAdapter(MarcRecordListener marcRecordListener) {
        this.marcRecordListener = marcRecordListener;
        this.builder = Marc.builder().lightweightRecord();
    }

    @Override
    public void beginCollection() {
        marcRecordListener.beginCollection();
    }

    @Override
    public void beginRecord(String format, String type) {
        builder.setFormat(format);
        builder.setType(type);
    }

    @Override
    public void leader(String label) {
        builder.recordLabel(RecordLabel.builder().from(label.toCharArray()).build());
    }

    @Override
    public void field(MarcField field) {
        builder.addField(field);
    }

    @Override
    public void endRecord() {
        marcRecordListener.record(builder.buildRecord());
        builder = Marc.builder().lightweightRecord();
    }

    @Override
    public void endCollection() {
        marcRecordListener.endCollection();
    }
}
