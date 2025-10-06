package io.github.cpjust.testng_annotations.listeners;

import io.github.cpjust.testng_annotations.annotations.CsvSource;
import org.testng.Assert;
import org.testng.annotations.Test;

// NOTE: Using '@Listeners({CsvSourceListener.class})' here doesn't work for some reason.
// If you want to use the CsvSourceListener, you need to register it in META-INF/services/org.testng.ITestNGListener
public class CsvSourceIT {
    private static final String UNEXPECTED_PARAMETER_VALUES_S_S = "Unexpected parameter values: %s, %s";

    @Test
    @CsvSource({"foo,bar", "baz,qux"})
    public void testTwoStrings(String a, String b) {
        Assert.assertNotNull(a, "First parameter should not be null");
        Assert.assertNotNull(b, "Second parameter should not be null");
        Assert.assertTrue((a.equals("foo") && b.equals("bar")) || (a.equals("baz") && b.equals("qux")),
                String.format(UNEXPECTED_PARAMETER_VALUES_S_S, a, b));
    }

    @Test
    @CsvSource(value = {"1;2", "3;4"}, delimiter = ';')
    public void testIntsAsStrings(String a, String b) {
        Assert.assertTrue(a.equals("1") || a.equals("3"), "Unexpected value for a: " + a);
        Assert.assertTrue(b.equals("2") || b.equals("4"), "Unexpected value for b: " + b);
    }

    @Test
    @CsvSource({"'hello, world',42"})
    public void testQuotedComma(String a, String b) {
        Assert.assertEquals(a, "hello, world", "Quoted value not parsed correctly");
        Assert.assertEquals(b, "42", "Second value not parsed correctly");
    }

    @Test
    @CsvSource({"'',bar"})
    public void testEmptyQuotedValue(String a, String b) {
        Assert.assertEquals(a, "", "First parameter should be empty string");
        Assert.assertEquals(b, "bar", "Second parameter should be 'bar'");
    }

    @Test
    @CsvSource({",bar", "foo,"})
    public void testDelimiterAtStartOrEnd(String a, String b) {
        // First row: a = "", b = "bar"
        // Second row: a = "foo", b = ""
        if ("bar".equals(b)) {
            Assert.assertEquals(a, "", "Delimiter at start should yield empty string");
        } else if ("foo".equals(a)) {
            Assert.assertEquals(b, "", "Delimiter at end should yield empty string");
        } else {
            Assert.fail(String.format(UNEXPECTED_PARAMETER_VALUES_S_S, a, b));
        }
    }

    @Test
    @CsvSource({"foo"})
    public void testSingleColumn(String a) {
        Assert.assertEquals(a, "foo", "Single column value should be 'foo'");
    }

    @Test
    @CsvSource({"   ,bar"})
    public void testWhitespaceOnlyValue(String a, String b) {
        Assert.assertEquals(a, "", "Whitespace-only value should be trimmed to empty string");
        Assert.assertEquals(b, "bar", "Second parameter should be 'bar'");
    }

    @Test
    @CsvSource({"'  ,quoted  ', bar "})
    public void testQuotedValueWithWhitespace(String a, String b) {
        Assert.assertEquals(a, ",quoted", "Quoted value should trim whitespace and preserve comma inside quotes");
        Assert.assertEquals(b, "bar", "Second parameter should trim whitespace");
    }

    @Test
    @CsvSource({"'''foo''',bar"})
    public void testQuotedEscapedAtStartEnd(String a, String b) {
        Assert.assertEquals(a, "'foo'", "Escaped quotes should be parsed correctly");
        Assert.assertEquals(b, "bar", "Second parameter should be 'bar'");
    }

    @Test
    @CsvSource(value = {"'  ,foo' ,'  bar ' "}, trimWhitespace = false)
    public void testQuotedValueAtStartTrimWhitespaceFalse(String a, String b) {
        Assert.assertEquals(a, "  ,foo ", "Quoted value at the start should preserve whitespace and comma when trimWhitespace=false");
        Assert.assertEquals(b, "  bar  ", "Second quoted value at the start should preserve whitespace when trimWhitespace=false");
    }

    @Test
    @CsvSource(value = {" ' foo' ,  bar  "}, trimWhitespace = false)
    public void testQuotedValueNotAtStartTrimWhitespaceFalse(String a, String b) {
        Assert.assertEquals(a, " ' foo' ", "Quoted value not at the start should preserve whitespace when trimWhitespace=false");
        Assert.assertEquals(b, "  bar  ", "Second parameter should preserve whitespace when trimWhitespace=false");
    }

    @Test
    @CsvSource(value = {"\" foo,bar \",baz", "qux,\"quux, \"\"corge\""}, quoteCharacter = '"')
    public void testCustomQuoteCharacter(String a, String b) {
        // First row: a = foo,bar   b = baz
        // Second row: a = qux      b = quux, "corge
        if ("baz".equals(b)) {
            Assert.assertEquals(a, "foo,bar", "Custom quote char should parse quoted comma");
        } else if ("quux, \"corge".equals(b)) {
            Assert.assertEquals(a, "qux", "Custom quote char should parse quoted value");
        } else {
            Assert.fail(String.format(UNEXPECTED_PARAMETER_VALUES_S_S, a, b));
        }
    }
}
