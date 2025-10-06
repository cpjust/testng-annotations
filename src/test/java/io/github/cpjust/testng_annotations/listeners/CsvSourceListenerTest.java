package io.github.cpjust.testng_annotations.listeners;

import io.github.cpjust.testng_annotations.annotations.CsvSource;
import io.github.cpjust.testng_annotations.listeners.annotation_transformers.CsvSourceListener;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class CsvSourceListenerTest {
    static final String METHODS_SHOULD_NOT_BE_EMPTY = "java:S1186"; // Suppress "Methods should not be empty" warning

    // Dummy test methods for reflection
    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Empty method is intentional for test purposes
    public static class PositiveCases {
        @CsvSource({"foo,bar", "baz,qux"})
        public void twoStrings(String a, String b) {}

        @CsvSource({"a,42", "'b',43"})
        public void charAndInt(String a, String b) {}

        @CsvSource(value = {"1;2", "3;4"}, delimiter = ';')
        public void twoIntsWithCustomDelimiter(String a, String b) {}

        @CsvSource(value = {"a\\b\\c"}, delimiter = '\\')
        public void backslashDelimiter(String a, String b, String c) {}

        @CsvSource(value = {"a b", "c d"}, delimiter = ' ')
        public void spaceDelimiter(String a, String b) {}

        @CsvSource({"'hello, world',42"})
        public void quotedComma(String a, String b) {}

        @CsvSource({"'a,''b''',c"})
        public void quotedEscapedQuote(String a, String b) {}

        @CsvSource(value = {" ' a, '' b '' ',c"})
        public void quotedEscapedValueWithWhitespace(String a, String b) {}

        // NOTE: We can't start with a space before the ' when not trimming whitespace, otherwise FastCSV doesn't treat it as a quoted string.
        @CsvSource(value = {"' a, '' b '' ',c"}, trimWhitespace = false)
        public void quotedEscapedValueTrimWhitespaceFalse(String a, String b) {}

        @CsvSource({" a , b ", "  c ,  d  "})
        public void whitespaceTrimmed(String a, String b) {}

        @CsvSource(value = {"  x ,  y  "}, trimWhitespace = false)
        public void whitespaceNotTrimmed(String a, String b) {}

        @CsvSource({"'',bar"})
        public void emptyQuotedValue(String a, String b) {}

        @CsvSource({",bar", "foo,"})
        public void delimiterAtStartOrEnd(String a, String b) {}

        @CsvSource({"foo"})
        public void singleColumn(String a) {}

        @CsvSource({"   ,bar"})
        public void whitespaceOnlyValue(String a, String b) {}

        @CsvSource(value = {"'  quoted  ',bar"}, trimWhitespace = false)
        public void quotedValueWithWhitespace(String a, String b) {}

        @CsvSource(value = {"'''foo''',bar"})
        public void quotedEscapedAtStartEnd(String a, String b) {}
    }

    static Stream<Arguments> successProvider() {
        return Stream.of(
            Arguments.of("twoStrings", new Class<?>[] {String.class, String.class}, new Object[][] {
                {"foo", "bar"},
                {"baz", "qux"}
            }),
            Arguments.of("charAndInt", new Class<?>[] {String.class, String.class}, new Object[][] {
                {"a", "42"},
                {"b", "43"}
            }),
            Arguments.of("twoIntsWithCustomDelimiter", new Class<?>[] {String.class, String.class}, new Object[][] {
                {"1", "2"},
                {"3", "4"}
            }),
            Arguments.of("backslashDelimiter", new Class<?>[] {String.class, String.class, String.class}, new Object[][] {
                {"a", "b", "c"}
            }),
            Arguments.of("spaceDelimiter", new Class<?>[] {String.class, String.class}, new Object[][] {
                {"a", "b"},
                {"c", "d"}
            }),
            Arguments.of("quotedComma", new Class<?>[] {String.class, String.class}, new Object[][] {
                {"hello, world", "42"}
            }),
            Arguments.of("quotedEscapedQuote", new Class<?>[] {String.class, String.class}, new Object[][] {
                {"a,'b'", "c"}
            }),
            Arguments.of("quotedEscapedValueWithWhitespace", new Class<?>[] {String.class, String.class}, new Object[][] {
                {"a, ' b '", "c"}
            }),
            Arguments.of("quotedEscapedValueTrimWhitespaceFalse", new Class<?>[] {String.class, String.class}, new Object[][] {
                {" a, ' b ' ", "c"}
            }),
            Arguments.of("whitespaceTrimmed", new Class<?>[] {String.class, String.class}, new Object[][] {
                {"a", "b"},
                {"c", "d"}
            }),
            Arguments.of("whitespaceNotTrimmed", new Class<?>[] {String.class, String.class}, new Object[][] {
                {"  x ", "  y  "}
            }),
            Arguments.of("emptyQuotedValue", new Class<?>[] {String.class, String.class}, new Object[][] {
                {"", "bar"}
            }),
            Arguments.of("delimiterAtStartOrEnd", new Class<?>[] {String.class, String.class}, new Object[][] {
                {"", "bar"},
                {"foo", ""}
            }),
            Arguments.of("singleColumn", new Class<?>[] {String.class}, new Object[][] {
                {"foo"}
            }),
            Arguments.of("whitespaceOnlyValue", new Class<?>[] {String.class, String.class}, new Object[][] {
                {"", "bar"}
            }),
            Arguments.of("quotedValueWithWhitespace", new Class<?>[] {String.class, String.class}, new Object[][] {
                {"  quoted  ", "bar"}
            }),
            Arguments.of("quotedEscapedAtStartEnd", new Class<?>[] {String.class, String.class}, new Object[][] {
                {"'foo'", "bar"}
            })
        );
    }

    @ParameterizedTest
    @MethodSource("successProvider")
    void provideValues_validCsvLines_returnExpectedValues(String methodName, Class<?>[] paramTypes, Object[][] expected) throws Exception {
        Method method = PositiveCases.class.getMethod(methodName, paramTypes);
        Object[][] values = CsvSourceListener.provideValues(method);
        log.info("Method '{}' produced {} rows.", methodName, values.length);
        assertEquals(expected.length, values.length, "Wrong number of rows returned!");

        for (int i = 0; i < expected.length; ++i) {
            log.info("Row {}: expected = {}, actual = {}", i, expected[i], values[i]);
            assertArrayEquals(expected[i], values[i], "Row " + i + " does not match!");
        }
    }

    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Empty method is intentional for test purposes
    public static class ErrorCases {
        public void noCsvSource(String a) {}

        @CsvSource({})
        public void emptySource(String a) {}

        @CsvSource({"foo,bar"})
        public void wrongParamCount(String a) {}

        @CsvSource({"foo,bar"})
        public void wrongParamType(int a, int b) {}

        @CsvSource(value = {"aaa,bbb\rccc"}, delimiter = '\r')
        public void carriageReturnDelimiter(String a, String b) {}

        @CsvSource(value = {"aaa,bbb\nccc"}, delimiter = '\n')
        public void newlineDelimiter(String a, String b) {}

        @CsvSource(value = {"aaa,bbb#ccc"}, delimiter = ':', quoteCharacter = ':')
        public void delimiterSameAsQuoteChar(String a, String b) {}
    }

    // Parameterized test for no CsvSource, empty source, wrong param count
    static Stream<Arguments> noCsvSourceOrEmptySourceOrWrongParamCountProvider() {
        return Stream.of(
                Arguments.of("noCsvSource", "No @CsvSource annotation found on method: noCsvSource"),
                Arguments.of("emptySource", "No values provided in @CsvSource annotation"),
                Arguments.of("wrongParamCount", "does not match method parameter count")
        );
    }

    @ParameterizedTest
    @MethodSource("noCsvSourceOrEmptySourceOrWrongParamCountProvider")
    void provideValues_noCsvSourceOrEmptySourceOrWrongParamCount_throwsIllegalStateException(String methodName, String expectedMessage) throws Exception {
        Method method = ErrorCases.class.getMethod(methodName, String.class);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                CsvSourceListener.provideValues(method)
        );

        log.info("Caught expected exception: '{}'", ex.getMessage());
        assertThat("Wrong exception message.", ex.getMessage(),
                containsString(expectedMessage));
    }

    @Test
    void provideValues_wrongParamType_throwsIllegalStateException() throws Exception {
        Method method = ErrorCases.class.getMethod("wrongParamType", int.class, int.class);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                CsvSourceListener.provideValues(method)
        );

        assertTrue(ex.getMessage().contains("does not match method parameter type"));
    }

    // Parameterized test for delimiter/quote char errors
    static Stream<Arguments> invalidDelimiterOrQuoteProvider() {
        return Stream.of(
                Arguments.of("carriageReturnDelimiter", "CsvSource delimiter cannot be a newline or carriage return character"),
                Arguments.of("newlineDelimiter", "CsvSource delimiter cannot be a newline or carriage return character"),
                Arguments.of("delimiterSameAsQuoteChar", "CsvSource delimiter cannot be the same as the quote character")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidDelimiterOrQuoteProvider")
    void provideValues_illegalDelimiterOrQuote_throwsIllegalArgumentException(String methodName, String expectedMessage) throws Exception {
        Method method = ErrorCases.class.getMethod(methodName, String.class, String.class);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                CsvSourceListener.provideValues(method)
        );

        log.info("Caught expected exception: '{}'", ex.getMessage());
        assertThat("Exception message should mention invalid delimiter usage.", ex.getMessage(),
                containsString(expectedMessage));
    }

    @Test
    void provideValues_zeroParameters_throwsIllegalStateException() throws Exception {
        @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Empty method is intentional for test purposes
        class ZeroParamCase {
            @CsvSource({"foo"})
            public void zeroParams() {}
        }

        Method method = ZeroParamCase.class.getMethod("zeroParams");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            CsvSourceListener.provideValues(method)
        );

        assertTrue(ex.getMessage().contains("does not match method parameter count"), "Should throw for param count mismatch");
    }

    @Test
    void provideValues_moreParamsThanColumns_throwsIllegalStateException() throws Exception {
        @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Empty method is intentional for test purposes
        class MoreParamsThanColumns {
            @CsvSource({"foo"})
            public void moreParams(String a, String b) {}
        }

        Method method = MoreParamsThanColumns.class.getMethod("moreParams", String.class, String.class);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            CsvSourceListener.provideValues(method)
        );

        assertTrue(ex.getMessage().contains("does not match method parameter count"), "Should throw for param count mismatch");
    }

    @Test
    void provideValues_zeroCsvLines_throwsIllegalStateException() throws Exception {
        @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Empty method is intentional for test purposes
        class ZeroCsvLines {
            @CsvSource({})
            public void zeroCsvLines(String a) {}
        }

        Method method = ZeroCsvLines.class.getMethod("zeroCsvLines", String.class);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            CsvSourceListener.provideValues(method)
        );

        assertEquals("No values provided in @CsvSource annotation", ex.getMessage());
    }

    @Test
    void transform_withCsvSourceAnnotation_setsDataProvider() throws Exception {
        @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Empty method is intentional for test purposes
        class Dummy {
            @CsvSource({"a,b"})
            public void test(String a, String b) {}
        }

        CsvSourceListener listener = new CsvSourceListener();
        ITestAnnotation annotation = Mockito.mock(ITestAnnotation.class);
        Method method = Dummy.class.getMethod("test", String.class, String.class);

        listener.transform(annotation, Dummy.class, null, method);

        Mockito.verify(annotation).setDataProvider(CsvSourceListener.CSV_SOURCE_PROVIDER);
        Mockito.verify(annotation).setDataProviderClass(CsvSourceListener.class);
    }
}
