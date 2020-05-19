module org.xbib.marc {
    exports org.xbib.marc;
    exports org.xbib.marc.dialects.aleph;
    exports org.xbib.marc.dialects.bibliomondo;
    exports org.xbib.marc.dialects.mab;
    exports org.xbib.marc.dialects.mab.xml;
    exports org.xbib.marc.dialects.mab.diskette;
    exports org.xbib.marc.dialects.pica;
    exports org.xbib.marc.dialects.sisis;
    exports org.xbib.marc.io;
    exports org.xbib.marc.json;
    exports org.xbib.marc.label;
    exports org.xbib.marc.tools;
    exports org.xbib.marc.transformer;
    exports org.xbib.marc.transformer.field;
    exports org.xbib.marc.transformer.value;
    exports org.xbib.marc.xml;
    requires java.logging;
    requires java.xml;
}
