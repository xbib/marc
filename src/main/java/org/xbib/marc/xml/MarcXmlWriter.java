package org.xbib.marc.xml;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.util.XMLEventConsumer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MarcXmlWriter extends MarcXchangeWriter {

    private static final String NAMESPACE_URI = MARCXML_NS_URI;

    private static final String NAMESPACE_SCHEMA_LOCATION = MARCXML_SCHEMA_LOCATION;

    private static final QName COLLECTION_ELEMENT = new QName(NAMESPACE_URI, COLLECTION, "");

    private static final QName RECORD_ELEMENT = new QName(NAMESPACE_URI, RECORD, "");

    private static final QName LEADER_ELEMENT = new QName(NAMESPACE_URI, LEADER, "");

    private static final QName CONTROLFIELD_ELEMENT = new QName(NAMESPACE_URI, CONTROLFIELD, "");

    private static final QName DATAFIELD_ELEMENT = new QName(NAMESPACE_URI, DATAFIELD, "");

    private static final QName SUBFIELD_ELEMENT = new QName(NAMESPACE_URI, SUBFIELD, "");

    public MarcXmlWriter(OutputStream out) throws IOException {
        super(out);
    }

    /**
     * Create a MarcXML writer on an underlying output stream.
     * @param out the underlying output stream
     * @param indent if true, indent MarcXchange output
     * @throws IOException if writer can not be created
     */
    public MarcXmlWriter(OutputStream out, boolean indent) throws IOException {
        super(new OutputStreamWriter(out, StandardCharsets.UTF_8), indent);
    }

    /**
     * Create a MarcXML writer on an underlying writer.
     * @param writer the underlying writer
     * @throws IOException if writer can not be created
     */
    public MarcXmlWriter(Writer writer) throws IOException {
        super(writer, false);
    }

    /**
     * Create a MarcXML writer on an underlying writer.
     * @param writer the underlying writer
     * @param indent if true, indent MarcXchange output
     * @throws IOException if writer can not be created
     */
    public MarcXmlWriter(Writer writer, boolean indent) throws IOException {
        super(writer, indent);
    }

    public MarcXmlWriter(String fileNamePattern, int splitlimit, int bufferSize, boolean compress, boolean indent)
            throws IOException {
        super(fileNamePattern, splitlimit, bufferSize, compress, indent);
    }

    public MarcXmlWriter(XMLEventConsumer consumer) {
        super(consumer);
    }

    @Override
    protected Namespace createNameSpace() {
        return eventFactory.createNamespace("", NAMESPACE_URI);
    }

    @Override
    protected boolean createFormatAttribute() {
        return false;
    }

    @Override
    protected boolean createTypeAttribute() {
        return false;
    }

    @Override
    protected void writeSchema(List<Attribute> attrs) throws XMLStreamException {
        attrs.add(eventFactory.createAttribute("xmlns:xsi",
                XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI));
        attrs.add(eventFactory.createAttribute("xsi:schemaLocation",
                NAMESPACE_URI + " " + NAMESPACE_SCHEMA_LOCATION));
    }

    @Override
    protected QName getCollectionElement() {
        return COLLECTION_ELEMENT;
    }

    @Override
    protected QName getRecordElement() {
        return RECORD_ELEMENT;
    }

    @Override
    protected QName getLeaderElement() {
        return LEADER_ELEMENT;
    }

    @Override
    protected QName getControlfieldElement() {
        return CONTROLFIELD_ELEMENT;
    }

    @Override
    protected QName getDatafieldElement() {
        return DATAFIELD_ELEMENT;
    }

    @Override
    protected QName getSubfieldElement() {
        return SUBFIELD_ELEMENT;
    }
}
