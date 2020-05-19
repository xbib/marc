package org.xbib.marc.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.StringWriter;

public class JsonWriterTest {

    private StringWriter output;

    private JsonWriter writer;

    private static String string(char... chars) {
        return String.valueOf(chars);
    }

    @BeforeEach
    public void setUp() {
        output = new StringWriter();
        writer = new JsonWriter(output);
    }

    @Test
    public void writeLiteral() throws IOException {
        writer.writeLiteral("foo");
        assertEquals("foo", output.toString());
    }

    @Test
    public void writeNumber() throws IOException {
        writer.writeNumber("23");
        assertEquals("23", output.toString());
    }

    @Test
    public void writeStringempty() throws IOException {
        writer.writeString("");
        assertEquals("\"\"", output.toString());
    }

    @Test
    public void writeStingescapesBackslashes() throws IOException {
        writer.writeString("foo\\bar");
        assertEquals("\"foo\\\\bar\"", output.toString());
    }

    @Test
    public void writeArrayParts() throws IOException {
        writer.writeArrayOpen();
        writer.writeArraySeparator();
        writer.writeArrayClose();
        assertEquals("[,]", output.toString());
    }

    @Test
    public void writeObjectParts() throws IOException {
        writer.writeObjectOpen();
        writer.writeMemberSeparator();
        writer.writeObjectSeparator();
        writer.writeObjectClose();
        assertEquals("{:,}", output.toString());
    }

    @Test
    public void writeMemberNameempty() throws IOException {
        writer.writeMemberName("");
        assertEquals("\"\"", output.toString());
    }

    @Test
    public void writeMemberNameescapesBackslashes() throws IOException {
        writer.writeMemberName("foo\\bar");
        assertEquals("\"foo\\\\bar\"", output.toString());
    }

    @Test
    public void escapesQuotes() throws IOException {
        writer.writeString("a\"b");
        assertEquals("\"a\\\"b\"", output.toString());
    }

    @Test
    public void escapesEscapedQuotes() throws IOException {
        writer.writeString("foo\\\"bar");
        assertEquals("\"foo\\\\\\\"bar\"", output.toString());
    }

    @Test
    public void escapesNewLine() throws IOException {
        writer.writeString("foo\nbar");
        assertEquals("\"foo\\nbar\"", output.toString());
    }

    @Test
    public void escapesWindowsNewLine() throws IOException {
        writer.writeString("foo\r\nbar");
        assertEquals("\"foo\\r\\nbar\"", output.toString());
    }

    @Test
    public void escapesTabs() throws IOException {
        writer.writeString("foo\tbar");
        assertEquals("\"foo\\tbar\"", output.toString());
    }

    @Test
    public void escapesSpecialCharacters() throws IOException {
        writer.writeString("foo\u2028bar\u2029");
        assertEquals("\"foo\\u2028bar\\u2029\"", output.toString());
    }

    @Test
    public void escapesZeroCharacter() throws IOException {
        writer.writeString(string('f', 'o', 'o', (char) 0, 'b', 'a', 'r'));
        assertEquals("\"foo\\u0000bar\"", output.toString());
    }

    @Test
    public void escapesEscapeCharacter() throws IOException {
        writer.writeString(string('f', 'o', 'o', (char) 27, 'b', 'a', 'r'));
        assertEquals("\"foo\\u001bbar\"", output.toString());
    }

    @Test
    public void escapesControlCharacters() throws IOException {
        writer.writeString(string((char) 1, (char) 8, (char) 15, (char) 16, (char) 31));
        assertEquals("\"\\u0001\\u0008\\u000f\\u0010\\u001f\"", output.toString());
    }

    @Test
    public void escapesFirstChar() throws IOException {
        writer.writeString(string('\\', 'x'));
        assertEquals("\"\\\\x\"", output.toString());
    }

    @Test
    public void escapesLastChar() throws IOException {
        writer.writeString(string('x', '\\'));
        assertEquals("\"x\\\\\"", output.toString());
    }
}
