package io.github.cpjust.testng_annotations.listeners;

import io.github.cpjust.testng_annotations.annotations.ValueSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValueSourceListenerTest {
    // region Positive cases
    // Dummy test methods for reflection
    @SuppressWarnings("java:S1186") // Suppress "Methods should not be empty" warning
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
    static Stream<org.junit.jupiter.params.provider.Arguments> successProvider() {
        return Stream.of(
            org.junit.jupiter.params.provider.Arguments.of("stringValues", String.class, new Object[]{"foo", "bar"}),
            org.junit.jupiter.params.provider.Arguments.of("charValues", char.class, new Object[]{'a', 'b'}),
            org.junit.jupiter.params.provider.Arguments.of("booleanValues", boolean.class, new Object[]{true, false}),
            org.junit.jupiter.params.provider.Arguments.of("byteValues", byte.class, new Object[]{(byte)1, (byte)2}),
            org.junit.jupiter.params.provider.Arguments.of("shortValues", short.class, new Object[]{(short)5, (short)6}),
            org.junit.jupiter.params.provider.Arguments.of("intValues", int.class, new Object[]{1, 2, 3}),
            org.junit.jupiter.params.provider.Arguments.of("longValues", long.class, new Object[]{10L, 20L}),
            org.junit.jupiter.params.provider.Arguments.of("floatValues", float.class, new Object[]{1.5f, 2.5f}),
            org.junit.jupiter.params.provider.Arguments.of("doubleValues", double.class, new Object[]{1.1, 2.2}),
            org.junit.jupiter.params.provider.Arguments.of("classValues", Class.class, new Object[]{String.class, Integer.class})
        );
    }

    @ParameterizedTest
    @MethodSource("successProvider")
    void provideValues_validValues_returnExpectedValues(String methodName, Class<?> paramType, Object[] expected) throws Exception {
        Method method = PositiveCases.class.getMethod(methodName, paramType);
        Object[] values = ValueSourceListener.provideValues(method);
        assertEquals(expected.length, values.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], values[i]);
        }
    }
    // endregion Positive cases

    // region Error cases
    // Dummy test methods for reflection
    @SuppressWarnings("java:S1186") // Suppress "Methods should not be empty" warning
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

    @Test
    void provideValues_noValueSourceAnnotation_throwsIllegalStateException() throws Exception {
        Method method = ErrorCases.class.getMethod("noValueSource", String.class);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                ValueSourceListener.provideValues(method)
        );
        assertEquals("@ValueSource annotation not found on method: noValueSource", ex.getMessage());
    }

    @Test
    void provideValues_noValues_throwsIllegalStateException() throws Exception {
        Method method = ErrorCases.class.getMethod("noValuesProvided", String.class);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            ValueSourceListener.provideValues(method)
        );
        assertEquals("No values provided in @ValueSource annotation", ex.getMessage());
    }

    @Test
    void provideValues_multipleParameters_throwsIllegalStateException() throws Exception {
        Method method = ErrorCases.class.getMethod("multipleParameters", String.class, String.class);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            ValueSourceListener.provideValues(method)
        );
        assertEquals("@ValueSource can only be used with single-parameter test methods", ex.getMessage());
    }

    @Test
    void provideValues_multipleValueTypes_throwsIllegalStateException() throws Exception {
        Method method = ErrorCases.class.getMethod("multipleValueTypes", String.class);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                ValueSourceListener.provideValues(method)
        );
        assertEquals("@ValueSource must have exactly one value parameter set (e.g., only strings, only ints, etc.)", ex.getMessage());
    }

    @ParameterizedTest
    @MethodSource("wrongTypeProvider")
    void provideValues_wrongParameterType_throwsIllegalStateException(String methodName, Class<?>[] paramTypes, String expectedMessage) throws Exception {
        Method method = ErrorCases.class.getMethod(methodName, paramTypes);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                ValueSourceListener.provideValues(method)
        );
        assertEquals(String.format("Test method parameter must be %s in @ValueSource", expectedMessage), ex.getMessage());
    }

    /**
     * Provides test cases for methods with wrong parameter types.
     * Each case includes the method name, its parameter types, and the unique part of the expected error message.
     */
    static Stream<org.junit.jupiter.params.provider.Arguments> wrongTypeProvider() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of("wrongStringType", new Class<?>[]{int.class}, "String when using strings()"),
                org.junit.jupiter.params.provider.Arguments.of("wrongCharType", new Class<?>[]{String.class}, "char/Character when using chars()"),
                org.junit.jupiter.params.provider.Arguments.of("wrongByteType", new Class<?>[]{int.class}, "byte/Byte when using bytes()"),
                org.junit.jupiter.params.provider.Arguments.of("wrongShortType", new Class<?>[]{int.class}, "short/Short when using shorts()"),
                org.junit.jupiter.params.provider.Arguments.of("wrongIntType", new Class<?>[]{long.class}, "int/Integer when using ints()"),
                org.junit.jupiter.params.provider.Arguments.of("wrongLongType", new Class<?>[]{short.class}, "long/Long when using longs()"),
                org.junit.jupiter.params.provider.Arguments.of("wrongFloatType", new Class<?>[]{double.class}, "float/Float when using floats()"),
                org.junit.jupiter.params.provider.Arguments.of("wrongDoubleType", new Class<?>[]{float.class}, "double/Double when using doubles()"),
                org.junit.jupiter.params.provider.Arguments.of("wrongClassType", new Class<?>[]{String.class}, "Class when using classes()")
        );
    }
    // endregion Error cases

    // region Empty value arrays
    // Dummy test methods for reflection
    @SuppressWarnings("java:S1186") // Suppress "Methods should not be empty" warning
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
    static Stream<org.junit.jupiter.params.provider.Arguments> emptyProvider() {
        return Stream.of(
            org.junit.jupiter.params.provider.Arguments.of("emptyStrings", String.class),
            org.junit.jupiter.params.provider.Arguments.of("emptyChars", char.class),
            org.junit.jupiter.params.provider.Arguments.of("emptyBooleans", boolean.class),
            org.junit.jupiter.params.provider.Arguments.of("emptyBytes", byte.class),
            org.junit.jupiter.params.provider.Arguments.of("emptyShorts", short.class),
            org.junit.jupiter.params.provider.Arguments.of("emptyInts", int.class),
            org.junit.jupiter.params.provider.Arguments.of("emptyLongs", long.class),
            org.junit.jupiter.params.provider.Arguments.of("emptyFloats", float.class),
            org.junit.jupiter.params.provider.Arguments.of("emptyDoubles", double.class),
            org.junit.jupiter.params.provider.Arguments.of("emptyClasses", Class.class)
        );
    }

    @ParameterizedTest
    @MethodSource("emptyProvider")
    void provideValues_emptyValues_throwsIllegalStateException(String methodName, Class<?> paramType) throws Exception {
        Method method = EmptyCases.class.getMethod(methodName, paramType);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            ValueSourceListener.provideValues(method)
        );
        assertEquals("No values provided in @ValueSource annotation", ex.getMessage());
    }
    // endregion Empty value arrays
}
