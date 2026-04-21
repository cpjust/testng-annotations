package io.github.cpjust.testng_annotations.listeners.annotation_transformers;

import io.github.cpjust.testng_annotations.annotations.DisableBetweenDates;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.testng.IInvokedMethod;
import org.testng.ITestResult;
import org.testng.ITestNGMethod;
import org.testng.SkipException;
import org.testng.annotations.ITestAnnotation;
import org.testng.internal.ConstructorOrMethod;

import java.lang.reflect.Method;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.stream.Stream;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link DisableBetweenDatesListener}.
 */
class DisableBetweenDatesListenerTest {
    static final String METHODS_SHOULD_NOT_BE_EMPTY = "java:S1186"; // Suppress "Methods should not be empty" warning

    // Date constants with descriptive names
    private static final String DATE_IN_RANGE_START = "2026-02-01";
    private static final String DATE_IN_RANGE_END = "2026-02-28";
    private static final String DATE_IN_RANGE_START_EXTRA_WHITESPACE = " 2026-02-01";
    private static final String DATE_IN_RANGE_END_EXTRA_WHITESPACE = "2026-02-28 ";
    private static final String DATE_ON_BOUNDARY = "2026-02-16";
    private static final String DATE_OUT_OF_RANGE_START = "2026-03-01";
    private static final String DATE_OUT_OF_RANGE_END = "2026-03-31";
    private static final String DATE_FUTURE_START = "2026-04-01";
    private static final String DATE_FUTURE_END = "2026-04-30";
    private static final String DATE_INVALID_DATE = "invalid-date";
    private static final String DATE_YEAR_RANGE_START = "2026-01-01";
    private static final String DATE_YEAR_RANGE_END = "2026-12-31";
    private static final LocalDate FIXED_DATE = LocalDate.of(2026, 2, 16);

    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Empty method is intentional for test purposes
    public static class MethodAnnotatedCases {
        @DisableBetweenDates(start = DATE_IN_RANGE_START, end = DATE_IN_RANGE_END, throwSkipException = false)
        public void inRange() {}

        @DisableBetweenDates(start = DATE_IN_RANGE_START_EXTRA_WHITESPACE, end = DATE_IN_RANGE_END_EXTRA_WHITESPACE, throwSkipException = false)
        public void inRangeExtraWhitespace() {}

        @DisableBetweenDates(start = DATE_ON_BOUNDARY, end = DATE_IN_RANGE_END, throwSkipException = false)
        public void inRangeStartOnBoundary() {}

        @DisableBetweenDates(start = DATE_OUT_OF_RANGE_START, end = DATE_OUT_OF_RANGE_END, throwSkipException = false)
        public void outOfRange() {}

        @DisableBetweenDates(start = DATE_IN_RANGE_END, end = DATE_IN_RANGE_START, throwSkipException = false)
        public void endBeforeStart() {}

        @DisableBetweenDates(start = DATE_INVALID_DATE, end = DATE_OUT_OF_RANGE_END, throwSkipException = false)
        public void invalidStartDate() {}

        @DisableBetweenDates(start = DATE_IN_RANGE_START, end = DATE_IN_RANGE_END, throwSkipException = true)
        public void inRangeThrowSkip() {}

        @DisableBetweenDates(start = DATE_IN_RANGE_START_EXTRA_WHITESPACE, end = DATE_IN_RANGE_END_EXTRA_WHITESPACE, throwSkipException = true)
        public void inRangeThrowSkipExtraWhitespace() {}

        @DisableBetweenDates(start = DATE_IN_RANGE_START, end = DATE_ON_BOUNDARY, throwSkipException = true)
        public void inRangeThrowSkipEndOnBoundary() {}

        @DisableBetweenDates(start = DATE_OUT_OF_RANGE_START, end = DATE_OUT_OF_RANGE_END, throwSkipException = true)
        public void outOfRangeThrowSkip() {}

        @DisableBetweenDates(start = DATE_IN_RANGE_END, end = DATE_IN_RANGE_START, throwSkipException = true)
        public void endBeforeStartThrowSkip() {}

        @DisableBetweenDates(start = DATE_INVALID_DATE, end = DATE_OUT_OF_RANGE_END, throwSkipException = true)
        public void invalidStartDateThrowSkip() {}

        public void noAnnotation() {}
    }

    @DisableBetweenDates(start = DATE_IN_RANGE_START, end = DATE_IN_RANGE_END, throwSkipException = false)
    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Empty method is intentional for test purposes
    public static class NoThrowSkipClassInRange {
        @DisableBetweenDates(start = DATE_OUT_OF_RANGE_START, end = DATE_OUT_OF_RANGE_END, throwSkipException = false)
        public void classInRange() {}

        @DisableBetweenDates(start = DATE_YEAR_RANGE_START, end = DATE_YEAR_RANGE_END, throwSkipException = false)
        public void bothInRange() {}

        @DisableBetweenDates(start = DATE_INVALID_DATE, end = DATE_FUTURE_END, throwSkipException = false)
        public void invalidStartDate() {}

        public void noAnnotation() {}
    }

    @DisableBetweenDates(start = DATE_OUT_OF_RANGE_START, end = DATE_OUT_OF_RANGE_END, throwSkipException = false)
    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Empty method is intentional for test purposes
    public static class NoThrowSkipClassOutOfRange {
        @DisableBetweenDates(start = DATE_IN_RANGE_START, end = DATE_IN_RANGE_END, throwSkipException = false)
        public void methodInRange() {}

        @DisableBetweenDates(start = DATE_FUTURE_START, end = DATE_FUTURE_END, throwSkipException = false)
        public void neitherMatches() {}

        @DisableBetweenDates(start = DATE_INVALID_DATE, end = DATE_FUTURE_END, throwSkipException = false)
        public void invalidStartDate() {}

        public void noAnnotation() {}
    }

    static Stream<Arguments> noThrowSkipArgumentsProvider() {
        return Stream.of(
                Arguments.of("MethodAnnotatedCases", "inRange", MethodAnnotatedCases.class, true, null),
                Arguments.of("MethodAnnotatedCases", "inRangeExtraWhitespace", MethodAnnotatedCases.class, true, null),
                Arguments.of("MethodAnnotatedCases", "inRangeStartOnBoundary", MethodAnnotatedCases.class, true, null),
                Arguments.of("MethodAnnotatedCases", "outOfRange", MethodAnnotatedCases.class, false, null),
                Arguments.of("MethodAnnotatedCases", "endBeforeStart", MethodAnnotatedCases.class, false, IllegalArgumentException.class),
                Arguments.of("MethodAnnotatedCases", "invalidStartDate", MethodAnnotatedCases.class, false, DateTimeParseException.class),
                Arguments.of("MethodAnnotatedCases", "noAnnotation", MethodAnnotatedCases.class, false, null),

                Arguments.of("NoThrowSkipClassInRange", "classInRange", NoThrowSkipClassInRange.class, true, null),
                Arguments.of("NoThrowSkipClassInRange", "bothInRange", NoThrowSkipClassInRange.class, true, null),
                Arguments.of("NoThrowSkipClassInRange", "invalidStartDate", NoThrowSkipClassInRange.class, false, DateTimeParseException.class),
                Arguments.of("NoThrowSkipClassInRange", "noAnnotation", NoThrowSkipClassInRange.class, true, null),

                Arguments.of("NoThrowSkipClassOutOfRange", "methodInRange", NoThrowSkipClassOutOfRange.class, true, null),
                Arguments.of("NoThrowSkipClassOutOfRange", "neitherMatches", NoThrowSkipClassOutOfRange.class, false, null),
                Arguments.of("NoThrowSkipClassOutOfRange", "invalidStartDate", NoThrowSkipClassOutOfRange.class, false, DateTimeParseException.class),
                Arguments.of("NoThrowSkipClassOutOfRange", "noAnnotation", NoThrowSkipClassOutOfRange.class, false, null)
        );
    }

    @ParameterizedTest(name = "{0}.{1} with throwSkipException=false")
    @MethodSource(value = "noThrowSkipArgumentsProvider")
    <T extends Throwable> void givenClassOrMethodInRange_whenCallTransform_thenDisableIfInRange(
            String className, String testMethod, Class<?> testClass, boolean shouldDisable, Class<T> expectedException) throws Exception {
        // Arrange
        Clock clock = createFixedClock();
        DisableBetweenDatesListener transformer = new DisableBetweenDatesListener(clock);

        Method method = testClass.getMethod(testMethod);
        ITestAnnotation mockAnnotation = Mockito.mock(ITestAnnotation.class);

        // Act
        if (expectedException != null) {
            assertThrows(expectedException, () -> transformer.transform(mockAnnotation, testClass, method),
                    String.format("transform should throw %s for %s.%s",
                            expectedException.getSimpleName(), className, testMethod));
        } else {
            assertDoesNotThrow(() -> transformer.transform(mockAnnotation, testClass, method),
                    String.format("transform should not throw for %s.%s",
                            className, testMethod));
        }

        // Assert
        if (shouldDisable) {
            verify(mockAnnotation, times(1)).setEnabled(false);
        } else {
            verify(mockAnnotation, never()).setEnabled(false);
        }
    }

    @DisableBetweenDates(start = DATE_IN_RANGE_START, end = DATE_IN_RANGE_END, throwSkipException = true)
    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Empty method is intentional for test purposes
    public static class ThrowSkipClassInRange {
        @DisableBetweenDates(start = DATE_OUT_OF_RANGE_START, end = DATE_OUT_OF_RANGE_END, throwSkipException = true)
        public void classInRange() {}

        @DisableBetweenDates(start = DATE_YEAR_RANGE_START, end = DATE_YEAR_RANGE_END, throwSkipException = true)
        public void bothInRange() {}

        @DisableBetweenDates(start = DATE_INVALID_DATE, end = DATE_FUTURE_END, throwSkipException = true)
        public void invalidStartDate() {}

        public void noAnnotation() {}
    }

    @DisableBetweenDates(start = DATE_OUT_OF_RANGE_START, end = DATE_OUT_OF_RANGE_END, throwSkipException = true)
    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Empty method is intentional for test purposes
    public static class ThrowSkipClassOutOfRange {
        @DisableBetweenDates(start = DATE_IN_RANGE_START, end = DATE_IN_RANGE_END, throwSkipException = true)
        public void methodInRange() {}

        @DisableBetweenDates(start = DATE_FUTURE_START, end = DATE_FUTURE_END, throwSkipException = true)
        public void neitherMatches() {}

        @DisableBetweenDates(start = DATE_INVALID_DATE, end = DATE_FUTURE_END, throwSkipException = true)
        public void invalidStartDate() {}

        public void noAnnotation() {}
    }

    @DisableBetweenDates(start = DATE_INVALID_DATE, end = DATE_IN_RANGE_END, throwSkipException = true)
    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Empty method is intentional for test purposes
    public static class ClassWithMalformedDateThrowSkip {
        @DisableBetweenDates(start = DATE_IN_RANGE_START, end = DATE_IN_RANGE_END, throwSkipException = true)
        public void methodInRange() {}

        @DisableBetweenDates(start = DATE_FUTURE_START, end = DATE_FUTURE_END, throwSkipException = true)
        public void neitherMatches() {}

        public void noAnnotation() {}
    }

    static Stream<Arguments> throwSkipArgumentsProvider() {
        return Stream.of(
                Arguments.of("MethodAnnotatedCases", "inRangeThrowSkip", MethodAnnotatedCases.class, SkipException.class),
                Arguments.of("MethodAnnotatedCases", "inRangeThrowSkipExtraWhitespace", MethodAnnotatedCases.class, SkipException.class),
                Arguments.of("MethodAnnotatedCases", "inRangeThrowSkipEndOnBoundary", MethodAnnotatedCases.class, SkipException.class),
                Arguments.of("MethodAnnotatedCases", "outOfRangeThrowSkip", MethodAnnotatedCases.class, null),
                Arguments.of("MethodAnnotatedCases", "endBeforeStartThrowSkip", MethodAnnotatedCases.class, IllegalArgumentException.class),
                Arguments.of("MethodAnnotatedCases", "invalidStartDateThrowSkip", MethodAnnotatedCases.class, DateTimeParseException.class),

                Arguments.of("ThrowSkipClassInRange", "classInRange", ThrowSkipClassInRange.class, SkipException.class),
                Arguments.of("ThrowSkipClassInRange", "bothInRange", ThrowSkipClassInRange.class, SkipException.class),
                Arguments.of("ThrowSkipClassInRange", "noAnnotation", ThrowSkipClassInRange.class, SkipException.class),
                Arguments.of("ThrowSkipClassInRange", "invalidStartDate", ThrowSkipClassInRange.class, DateTimeParseException.class),

                Arguments.of("ThrowSkipClassOutOfRange", "methodInRange", ThrowSkipClassOutOfRange.class, SkipException.class),
                Arguments.of("ThrowSkipClassOutOfRange", "neitherMatches", ThrowSkipClassOutOfRange.class, null),
                Arguments.of("ThrowSkipClassOutOfRange", "noAnnotation", ThrowSkipClassOutOfRange.class, null),
                Arguments.of("ThrowSkipClassOutOfRange", "invalidStartDate", ThrowSkipClassOutOfRange.class, DateTimeParseException.class),

                Arguments.of("ClassWithMalformedDateThrowSkip", "methodInRange", ClassWithMalformedDateThrowSkip.class, SkipException.class),
                Arguments.of("ClassWithMalformedDateThrowSkip", "neitherMatches", ClassWithMalformedDateThrowSkip.class, DateTimeParseException.class),
                Arguments.of("ClassWithMalformedDateThrowSkip", "noAnnotation", ClassWithMalformedDateThrowSkip.class, DateTimeParseException.class)
        );
    }

    @ParameterizedTest(name = "{0}.{1} with throwSkipException=true")
    @MethodSource(value = "throwSkipArgumentsProvider")
    <T extends Throwable> void givenClassOrMethodInRange_whenCallBeforeInvocation_thenThrowIfInRange(
            String className, String testMethod, Class<?> testClass, Class<T> expectedException) throws Exception {
        // Arrange
        Clock clock = createFixedClock();
        DisableBetweenDatesListener transformer = new DisableBetweenDatesListener(clock);

        Method method = testClass.getMethod(testMethod);
        IInvokedMethod invoked = mockInvokedMethod(method, testClass);
        ITestResult result = Mockito.mock(ITestResult.class);

        // Act & Assert
        if (expectedException != null) {
            assertThrows(expectedException, () -> transformer.beforeInvocation(invoked, result),
                    String.format("beforeInvocation should throw %s for %s.%s",
                            expectedException.getSimpleName(), className, testMethod));
        } else {
            assertDoesNotThrow(() -> transformer.beforeInvocation(invoked, result),
                    String.format("beforeInvocation should not throw for %s.%s",
                            className, testMethod));
        }
    }

    /**
     * Creates a fixed clock set to 2026-02-16 for consistent testing.
     */
    private static Clock createFixedClock() {
        return Clock.fixed(FIXED_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
    }

    /**
     * Creates a mock IInvokedMethod for testing beforeInvocation.
     */
    private static IInvokedMethod mockInvokedMethod(Method method, Class<?> testClass) {
        IInvokedMethod invoked = Mockito.mock(IInvokedMethod.class);
        ITestNGMethod testMethod = Mockito.mock(ITestNGMethod.class);

        when(invoked.getTestMethod()).thenReturn(testMethod);
        when(testMethod.getConstructorOrMethod()).thenReturn(new ConstructorOrMethod(method));
        when(testMethod.getRealClass()).thenReturn(testClass);
        when(testMethod.getMethodName()).thenReturn(method.getName());

        return invoked;
    }
}
