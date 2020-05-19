package org.xbib.marc.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.io.StringWriter;

public class JsonNumberTest {

    private StringWriter output;

    private JsonWriter writer;

    @BeforeEach
    public void setUp() {
        output = new StringWriter();
        writer = new JsonWriter(output);
    }

    @Test
    public void constructorfailsWithNull() {
        Assertions.assertThrows(NullPointerException.class, () -> new JsonNumber(null));
    }

    @Test
    public void write() throws IOException {
        new JsonNumber("23").write(writer);
        assertEquals("23", output.toString());
    }

    @Test
    public void toStringreturnsInputString() {
        assertEquals("foo", new JsonNumber("foo").toString());
    }

    @Test
    public void isInt() {
        assertTrue(new JsonNumber("23").isInt());
    }

    @Test
    public void asInt() {
        assertEquals(23, new JsonNumber("23").asInt());
    }

    @Test
    public void asIntfailsWithExceedingValues() {
        Assertions.assertThrows(NumberFormatException.class, () -> {
            new JsonNumber("10000000000").asInt();
        });
    }

    @Test
    public void asIntfailsWithExponent() {
        Assertions.assertThrows(NumberFormatException.class, () -> {
            new JsonNumber("1e5").asInt();
        });
    }

    @Test
    public void asIntfailsWithFractional() {
        Assertions.assertThrows(NumberFormatException.class, () -> {
            new JsonNumber("23.5").asInt();
        });
    }

    @Test
    public void asLong() {
        assertEquals(23L, new JsonNumber("23").asLong());
    }

    @Test
    public void asLongfailsWithExceedingValues() {
        Assertions.assertThrows(NumberFormatException.class, () -> {
            new JsonNumber("10000000000000000000").asLong();
        });
    }

    @Test
    public void asLongfailsWithExponent() {
        Assertions.assertThrows(NumberFormatException.class, () -> {
            new JsonNumber("1e5").asLong();
        });
    }

    @Test
    public void asLongfailsWithFractional() {
        Assertions.assertThrows(NumberFormatException.class, () -> {
            new JsonNumber("23.5").asLong();
        });
    }

    @Test
    public void asFloat() {
        assertEquals(23.05f, new JsonNumber("23.05").asFloat(), 0);
    }

    @Test
    public void asFloatreturnsInfinityForExceedingValues() {
        assertEquals(Float.POSITIVE_INFINITY, new JsonNumber("1e50").asFloat(), 0);
        assertEquals(Float.NEGATIVE_INFINITY, new JsonNumber("-1e50").asFloat(), 0);
    }

    @Test
    public void asDouble() {
        double result = new JsonNumber("23.05").asDouble();
        assertEquals(23.05, result, 0);
    }

    @Test
    public void asDoublereturnsInfinityForExceedingValues() {
        assertEquals(Double.POSITIVE_INFINITY, new JsonNumber("1e500").asDouble(), 0);
        assertEquals(Double.NEGATIVE_INFINITY, new JsonNumber("-1e500").asDouble(), 0);
    }

    @Test
    public void equalstrueForSameInstance() {
        JsonNumber number = new JsonNumber("23");
        assertEquals(number, number);
    }

    @Test
    public void equalstrueForEqualNumberStrings() {
        assertEquals(new JsonNumber("23"), new JsonNumber("23"));
    }

    @Test
    public void equalsfalseForDifferentNumberStrings() {
        assertNotEquals(new JsonNumber("23"), new JsonNumber("42"));
        assertNotEquals(new JsonNumber("1e+5"), new JsonNumber("1e5"));
    }

    @Test
    public void equalsfalseForNull() {
        assertNotEquals(null, new JsonNumber("23"));
    }

    @Test
    public void equalsfalseForSubclass() {
        assertNotEquals(new JsonNumber("23"), new JsonNumber("23") {
        });
    }

    @Test
    public void hashCodeequalsForEqualStrings() {
        assertEquals(new JsonNumber("23").hashCode(), new JsonNumber("23").hashCode());
    }

    @Test
    public void hashCodediffersForDifferentStrings() {
        assertNotEquals(new JsonNumber("23").hashCode(), new JsonNumber("42").hashCode());
    }
}
