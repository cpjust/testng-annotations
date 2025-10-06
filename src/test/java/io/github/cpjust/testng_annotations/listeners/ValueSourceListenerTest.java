package io.github.cpjust.testng_annotations.listeners;

import io.github.cpjust.testng_annotations.annotations.EmptySource;
import io.github.cpjust.testng_annotations.annotations.NullAndEmptySource;
import io.github.cpjust.testng_annotations.annotations.NullSource;
import io.github.cpjust.testng_annotations.annotations.ValueSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValueSourceListenerTest {
    static final String METHODS_SHOULD_NOT_BE_EMPTY = "java:S1186"; // Suppress "Methods should not be empty" warning
    static final String SIMILAR_TESTS_SHOULD_BE_PARAMETRIZED = "java:S5976"; // Suppress "Similar tests should be grouped in a single Parameterized test" warning
    private static final String UNEXPECTED_EXCEPTION_MESSAGE = "Unexpected exception message";

    // region Positive cases
    // Dummy test methods for reflection
    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY)
    public static class PositiveCases {
        @ValueSource(strings = {"foo", "bar"})
        public void stringValues(String value) {}

        @ValueSource(chars = {'a', 'b'})
        public void charValues(char value) {}

        @ValueSource(booleans = {true, false})
        public void booleanValues(boolean value) {}

        @ValueSource(bytes = {1, 2})
        public void byteValues(byte value) {}

        @ValueSource(shorts = {5, 6})
        public void shortValues(short value) {}

        @ValueSource(ints = {1, 2, 3})
        public void intValues(int value) {}

        @ValueSource(longs = {10L, 20L})
        public void longValues(long value) {}

        @ValueSource(floats = {1.5f, 2.5f})
        public void floatValues(float value) {}

        @ValueSource(doubles = {1.1, 2.2})
        public void doubleValues(double value) {}

        @ValueSource(classes = {String.class, Integer.class})
        public void classValues(Class<?> value) {}
    }

    /**
     * Provides test cases for successful value extraction.
     * Each case includes the method name, its parameter type, and the expected values.
     */
    static Stream<Arguments> successProvider() {
        return Stream.of(
            Arguments.of("stringValues", String.class, new Object[]{"foo", "bar"}),
            Arguments.of("charValues", char.class, new Object[]{'a', 'b'}),
            Arguments.of("booleanValues", boolean.class, new Object[]{true, false}),
            Arguments.of("byteValues", byte.class, new Object[]{(byte)1, (byte)2}),
            Arguments.of("shortValues", short.class, new Object[]{(short)5, (short)6}),
            Arguments.of("intValues", int.class, new Object[]{1, 2, 3}),
            Arguments.of("longValues", long.class, new Object[]{10L, 20L}),
            Arguments.of("floatValues", float.class, new Object[]{1.5f, 2.5f}),
            Arguments.of("doubleValues", double.class, new Object[]{1.1, 2.2}),
            Arguments.of("classValues", Class.class, new Object[]{String.class, Integer.class})
        );
    }

    @ParameterizedTest
    @MethodSource("successProvider")
    void provideValues_validValues_returnExpectedValues(String methodName, Class<?> paramType, Object[] expected) throws Exception {
        Method method = PositiveCases.class.getMethod(methodName, paramType);
        Object[] values = ValueSourceListener.provideValues(method);
        assertEquals(expected.length, values.length, "Wrong number of values returned!");

        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], values[i], "Value at index " + i + " does not match!");
        }
    }
    // endregion Positive cases

    // region Error cases
    // Dummy test methods for reflection
    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Suppress "Methods should not be empty" warning
    public static class ErrorCases {
        public void noValueSource(String value) {} // no ValueSource annotation

        @ValueSource
        public void noValuesProvided(String value) {} // no values provided by ValueSource

        @ValueSource(strings = {"test"})
        public void multipleParameters(String first, String second) {} // multiple parameters not allowed with ValueSource

        @ValueSource(ints = {1, 2}, strings = {"aa", "bb"})
        public void multipleValueTypes(String value) {} // multiple value types not allowed

        @ValueSource(strings = {"test"})
        public void wrongStringType(int value) {} // should be String

        @ValueSource(chars = {'a', 'b'})
        public void wrongCharType(String value) {} // should be char/Character

        @ValueSource(bytes = {1, 2})
        public void wrongByteType(int value) {} // should be byte/Byte

        @ValueSource(shorts = {3, 4})
        public void wrongShortType(int value) {} // should be short/Short

        @ValueSource(ints = {5, 6})
        public void wrongIntType(long value) {} // should be int/Integer

        @ValueSource(longs = {7L, 8L})
        public void wrongLongType(short value) {} // should be long/Long

        @ValueSource(floats = {1.0f, 2.0f})
        public void wrongFloatType(double value) {} // should be float/Float

        @ValueSource(doubles = {3.0, 4.0})
        public void wrongDoubleType(float value) {} // should be double/Double

        @ValueSource(classes = {String.class})
        public void wrongClassType(String value) {} // should be Class
    }

    @SuppressWarnings(SIMILAR_TESTS_SHOULD_BE_PARAMETRIZED)
    @Test
    void provideValues_noValueSourceAnnotation_throwsIllegalStateException() throws Exception {
        Method method = ErrorCases.class.getMethod("noValueSource", String.class);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                ValueSourceListener.provideValues(method)
        );
        assertEquals("No [@NullSource, @EmptySource, @NullAndEmptySource, @ValueSource] annotations found on method: noValueSource",
                ex.getMessage(), UNEXPECTED_EXCEPTION_MESSAGE);
    }

    @Test
    void provideValues_noValues_throwsIllegalStateException() throws Exception {
        Method method = ErrorCases.class.getMethod("noValuesProvided", String.class);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            ValueSourceListener.provideValues(method)
        );
        assertEquals("No values provided in @ValueSource annotation", ex.getMessage(), UNEXPECTED_EXCEPTION_MESSAGE);
    }

    @Test
    void provideValues_multipleParameters_throwsIllegalStateException() throws Exception {
        Method method = ErrorCases.class.getMethod("multipleParameters", String.class, String.class);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            ValueSourceListener.provideValues(method)
        );
        assertEquals("@ValueSource can only be used with single-parameter test methods", ex.getMessage(), UNEXPECTED_EXCEPTION_MESSAGE);
    }

    @Test
    void provideValues_multipleValueTypes_throwsIllegalStateException() throws Exception {
        Method method = ErrorCases.class.getMethod("multipleValueTypes", String.class);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                ValueSourceListener.provideValues(method)
        );
        assertEquals("@ValueSource must have exactly one value parameter set (e.g., only strings, only ints, etc.)",
                ex.getMessage(), UNEXPECTED_EXCEPTION_MESSAGE);
    }

    @ParameterizedTest
    @MethodSource("wrongTypeProvider")
    void provideValues_wrongParameterType_throwsIllegalStateException(String methodName, Class<?>[] paramTypes, String expectedMessage) throws Exception {
        Method method = ErrorCases.class.getMethod(methodName, paramTypes);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                ValueSourceListener.provideValues(method)
        );
        assertEquals(String.format("Test method parameter must be %s in @ValueSource", expectedMessage), ex.getMessage(), UNEXPECTED_EXCEPTION_MESSAGE);
    }

    /**
     * Provides test cases for methods with wrong parameter types.
     * Each case includes the method name, its parameter types, and the unique part of the expected error message.
     */
    static Stream<Arguments> wrongTypeProvider() {
        return Stream.of(
                Arguments.of("wrongStringType", new Class<?>[]{int.class}, "String when using strings()"),
                Arguments.of("wrongCharType", new Class<?>[]{String.class}, "char/Character when using chars()"),
                Arguments.of("wrongByteType", new Class<?>[]{int.class}, "byte/Byte when using bytes()"),
                Arguments.of("wrongShortType", new Class<?>[]{int.class}, "short/Short when using shorts()"),
                Arguments.of("wrongIntType", new Class<?>[]{long.class}, "int/Integer when using ints()"),
                Arguments.of("wrongLongType", new Class<?>[]{short.class}, "long/Long when using longs()"),
                Arguments.of("wrongFloatType", new Class<?>[]{double.class}, "float/Float when using floats()"),
                Arguments.of("wrongDoubleType", new Class<?>[]{float.class}, "double/Double when using doubles()"),
                Arguments.of("wrongClassType", new Class<?>[]{String.class}, "Class when using classes()")
        );
    }
    // endregion Error cases

    // region Empty value arrays
    // Dummy test methods for reflection
    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Suppress "Methods should not be empty" warning
    public static class EmptyCases {
        @ValueSource(strings = {})
        public void emptyStrings(String value) {}

        @ValueSource(chars = {})
        public void emptyChars(char value) {}

        @ValueSource(booleans = {})
        public void emptyBooleans(boolean value) {}

        @ValueSource(bytes = {})
        public void emptyBytes(byte value) {}

        @ValueSource(shorts = {})
        public void emptyShorts(short value) {}

        @ValueSource(ints = {})
        public void emptyInts(int value) {}

        @ValueSource(longs = {})
        public void emptyLongs(long value) {}

        @ValueSource(floats = {})
        public void emptyFloats(float value) {}

        @ValueSource(doubles = {})
        public void emptyDoubles(double value) {}

        @ValueSource(classes = {})
        public void emptyClasses(Class<?> value) {}
    }

    /**
     * Provides test cases for empty value arrays.
     * Each case includes the method name, its parameter type, and the expected exception message.
     */
    static Stream<Arguments> emptyProvider() {
        return Stream.of(
            Arguments.of("emptyStrings", String.class),
            Arguments.of("emptyChars", char.class),
            Arguments.of("emptyBooleans", boolean.class),
            Arguments.of("emptyBytes", byte.class),
            Arguments.of("emptyShorts", short.class),
            Arguments.of("emptyInts", int.class),
            Arguments.of("emptyLongs", long.class),
            Arguments.of("emptyFloats", float.class),
            Arguments.of("emptyDoubles", double.class),
            Arguments.of("emptyClasses", Class.class)
        );
    }

    @ParameterizedTest
    @MethodSource("emptyProvider")
    void provideValues_emptyValues_throwsIllegalStateException(String methodName, Class<?> paramType) throws Exception {
        Method method = EmptyCases.class.getMethod(methodName, paramType);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            ValueSourceListener.provideValues(method)
        );

        assertEquals("No values provided in @ValueSource annotation", ex.getMessage(), UNEXPECTED_EXCEPTION_MESSAGE);
    }
    // endregion Empty value arrays

    // region NullSource/EmptySource/NullAndEmptySource positive cases
    // Dummy test methods for reflection
    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Suppress "Methods should not be empty" warning
    public static class NullAndEmptyPositiveCases {
        @NullSource
        public void nullString(String value) {}

        @NullSource
        public void nullList(List<String> value) {}

        @EmptySource
        public void emptyString(String value) {}

        @EmptySource
        public void emptyArray(int[] arr) {}

        @EmptySource
        public void emptyList(List<String> list) {}

        @EmptySource
        public void emptySet(Set<String> set) {}

        @EmptySource
        public void emptyQueue(Queue<String> queue) {}

        @EmptySource
        public void emptyMap(Map<String, String> map) {}

        @NullAndEmptySource
        public void nullAndEmptyString(String value) {}

        @NullAndEmptySource
        public void nullAndEmptyArray(int[] arr) {}

        @NullAndEmptySource
        public void nullAndEmptyList(List<String> list) {}

        @NullAndEmptySource
        public void nullAndEmptySet(Set<String> set) {}

        @NullAndEmptySource
        public void nullAndEmptyQueue(Queue<String> queue) {}

        @NullAndEmptySource
        public void nullAndEmptyMap(Map<String, String> map) {}
    }

    /**
     * Provides test cases for methods annotated with @NullSource, @EmptySource, and @NullAndEmptySource.
     * Each case includes the method name, its parameter types, and the expected values.
     */
    static Stream<Arguments> nullAndEmptyProvider() {
        return Stream.of(
            Arguments.of("nullString", new Class<?>[]{String.class}, new Object[]{null}),
            Arguments.of("nullList", new Class<?>[]{List.class}, new Object[]{null}),
            Arguments.of("emptyString", new Class<?>[]{String.class}, new Object[]{""}),
            Arguments.of("emptyArray", new Class<?>[]{int[].class}, new Object[]{new int[]{}}),
            Arguments.of("emptyList", new Class<?>[]{List.class}, new Object[]{Collections.emptyList()}),
            Arguments.of("emptySet", new Class<?>[]{Set.class}, new Object[]{Collections.emptySet()}),
            Arguments.of("emptyQueue", new Class<?>[]{Queue.class}, new Object[]{new LinkedList<>()}),
            Arguments.of("emptyMap", new Class<?>[]{Map.class}, new Object[]{Collections.emptyMap()}),
            Arguments.of("nullAndEmptyString", new Class<?>[]{String.class}, new Object[]{null, ""}),
            Arguments.of("nullAndEmptyArray", new Class<?>[]{int[].class}, new Object[]{null, new int[]{}}),
            Arguments.of("nullAndEmptyList", new Class<?>[]{List.class}, new Object[]{null, Collections.emptyList()}),
            Arguments.of("nullAndEmptySet", new Class<?>[]{Set.class}, new Object[]{null, Collections.emptySet()}),
            Arguments.of("nullAndEmptyQueue", new Class<?>[]{Queue.class}, new Object[]{null, new LinkedList<>()}),
            Arguments.of("nullAndEmptyMap", new Class<?>[]{Map.class}, new Object[]{null, Collections.emptyMap()})
        );
    }

    @ParameterizedTest
    @MethodSource("nullAndEmptyProvider")
    void provideValues_nullAndEmptyAnnotations_returnExpectedValues(String methodName, Class<?>[] paramTypes, Object[] expected) throws Exception {
        Method method = NullAndEmptyPositiveCases.class.getMethod(methodName, paramTypes);
        Object[] values = ValueSourceListener.provideValues(method);
        assertEquals(expected.length, values.length, "Wrong number of values returned!");

        for (int i = 0; i < expected.length; ++i) {
            Object expectedValue = expected[i];
            Object actualValue = values[i];

            if (expectedValue == null) {
                assertEquals(null, actualValue, "Expected null at index " + i);
            } else if (expectedValue instanceof int[] && actualValue instanceof int[]) {
                assertEquals(((int[]) expectedValue).length, ((int[]) actualValue).length, "Expected empty int[] at index " + i);
            } else {
                assertEquals(expectedValue, actualValue, "Value at index " + i + " does not match!");
            }
        }
    }
    // endregion

    // region NullSource/EmptySource/NullAndEmptySource error cases
    // Dummy test methods for reflection
    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Suppress "Methods should not be empty" warning
    public static class NullAndEmptyErrorCases {
        @NullSource
        public void nullTwoParams(String a, String b) {}

        @EmptySource
        public void emptyTwoParams(String a, String b) {}

        @NullAndEmptySource
        public void nullAndEmptyTwoParams(String a, String b) {}

        @EmptySource
        public void emptyUnsupportedType(Integer value) {}

        @NullAndEmptySource
        public void nullAndEmptyUnsupportedType(Double value) {}
    }

    /**
     * Provides test cases for methods with incorrect usage of @NullSource, @EmptySource, and @NullAndEmptySource.
     * Each case includes the method name, its parameter types, and the expected exception message.
     */
    static Stream<Arguments> nullAndEmptyErrorProvider() {
        return Stream.of(
            Arguments.of("nullTwoParams", new Class<?>[]{String.class, String.class},
                    "@NullSource can only be used with single-parameter test methods"),
            Arguments.of("emptyTwoParams", new Class<?>[]{String.class, String.class},
                    "@EmptySource can only be used with single-parameter test methods"),
            Arguments.of("nullAndEmptyTwoParams", new Class<?>[]{String.class, String.class},
                    "@NullAndEmptySource can only be used with single-parameter test methods"),
            Arguments.of("emptyUnsupportedType", new Class<?>[]{Integer.class},
                    "@EmptySource/@NullAndEmptySource not supported for parameter type: java.lang.Integer"),
            Arguments.of("nullAndEmptyUnsupportedType", new Class<?>[]{Double.class},
                    "@EmptySource/@NullAndEmptySource not supported for parameter type: java.lang.Double")
        );
    }

    @ParameterizedTest
    @MethodSource("nullAndEmptyErrorProvider")
    void provideValues_nullAndEmptyAnnotations_errorCases(String methodName, Class<?>[] paramTypes, String expectedMessage) throws Exception {
        Method method = NullAndEmptyErrorCases.class.getMethod(methodName, paramTypes);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            ValueSourceListener.provideValues(method)
        );

        assertEquals(expectedMessage, ex.getMessage(), "Unexpected exception message");
    }
    // endregion
}
