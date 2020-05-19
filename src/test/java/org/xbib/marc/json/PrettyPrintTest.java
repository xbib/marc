package org.xbib.marc.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.xbib.marc.json.JsonWriterConfig.prettyPrint;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;

public class PrettyPrintTest {

    @Test
    public void testIndentWithSpaces_emptyArray() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter output = JsonWriterConfig.prettyPrint(2).createWriter(sw);
        new JsonArray().write(output);
        assertEquals("[\n  \n]", sw.toString());
    }

    @Test
    public void testIndentWithSpaces_emptyObject() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter output = JsonWriterConfig.prettyPrint(2).createWriter(sw);
        new JsonObject().write(output);
        assertEquals("{\n  \n}", sw.toString());
    }

    @Test
    public void testIndentWithSpaces_array() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter output = JsonWriterConfig.prettyPrint(2).createWriter(sw);
        new JsonArray().add(23).add(42).write(output);
        assertEquals("[\n  23,\n  42\n]", sw.toString());
    }

    @Test
    public void testIndentWithSpaces_nestedArray() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter output = JsonWriterConfig.prettyPrint(2).createWriter(sw);
        new JsonArray().add(23).add(new JsonArray().add(42)).write(output);
        assertEquals("[\n  23,\n  [\n    42\n  ]\n]", sw.toString());
    }

    @Test
    public void testIndentWithSpaces_object() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter output = JsonWriterConfig.prettyPrint(2).createWriter(sw);
        new JsonObject().add("a", 23).add("b", 42).write(output);
        assertEquals("{\n  \"a\": 23,\n  \"b\": 42\n}", sw.toString());
    }

    @Test
    public void testIndentWithSpaces_nestedObject() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter output = JsonWriterConfig.prettyPrint(2).createWriter(sw);
        new JsonObject().add("a", 23).add("b", new JsonObject().add("c", 42)).write(output);
        assertEquals("{\n  \"a\": 23,\n  \"b\": {\n    \"c\": 42\n  }\n}", sw.toString());
    }

    @Test
    public void testIndentWithSpaces_zero() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter output = JsonWriterConfig.prettyPrint(0).createWriter(sw);
        new JsonArray().add(23).add(42).write(output);
        assertEquals("[\n23,\n42\n]", sw.toString());
    }

    @Test
    public void testIndentWithSpaces_one() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter output = JsonWriterConfig.prettyPrint(1).createWriter(sw);
        new JsonArray().add(23).add(42).write(output);
        assertEquals("[\n 23,\n 42\n]", sw.toString());
    }

    @Test
    public void testIndentWithSpaces_failsWithNegativeValues() {
        try {
            prettyPrint(-1);
            fail();
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().toLowerCase(Locale.US).contains("negative"));
        }
    }

    @Test
    public void testIndentWithSpaces_createsIndependentInstances() {
        Writer writer = mock(Writer.class);
        JsonWriterConfig config = prettyPrint(1);
        Object instance1 = config.createWriter(writer);
        Object instance2 = config.createWriter(writer);
        assertNotSame(instance1, instance2);
    }
}
