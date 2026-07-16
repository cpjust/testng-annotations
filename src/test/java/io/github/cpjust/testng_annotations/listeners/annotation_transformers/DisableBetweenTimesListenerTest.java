package io.github.cpjust.testng_annotations.listeners.annotation_transformers;

import io.github.cpjust.testng_annotations.annotations.DisableBetweenTimes;
import lombok.NonNull;
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
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.stream.Stream;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link DisableBetweenTimesListener}.
 */
class DisableBetweenTimesListenerTest {
    static final String METHODS_SHOULD_NOT_BE_EMPTY = "java:S1186"; // Suppress "Methods should not be empty" warning

    // Time constants with descriptive names
    private static final String TIME_IN_RANGE_START = "09:00:00";
    private static final String TIME_IN_RANGE_END = "17:00:00";
    private static final String TIME_IN_RANGE_START_EXTRA_WHITESPACE = " 09:00:00";
    private static final String TIME_IN_RANGE_END_EXTRA_WHITESPACE = "17:00:00 ";
    private static final String TIME_ON_BOUNDARY = "12:30:00";
    private static final String TIME_OUT_OF_RANGE_START = "17:00:01";
    private static final String TIME_OUT_OF_RANGE_END = "23:59:59";
    private static final String TIME_EARLY_MORNING_START = "06:00:00";
    private static final String TIME_EARLY_MORNING_END = "08:59:59";
    private static final String TIME_INVALID_TIME = "invalid-time";
    private static final String TIMEZONE_OFFSET_MINUS_11 = "-11:00";
    private static final String TIMEZONE_OFFSET_PLUS_14 = "Pacific/Kiritimati";
    private static final String TIMEZONE_UTC = "UTC";
    private static final String INVALID_TIMEZONE = "invalid";
    private static final LocalTime FIXED_TIME = LocalTime.of(12, 30, 0);
    private static final ZoneId FIXED_ZONE = ZoneId.of(TIMEZONE_UTC);

    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Empty method is intentional for test purposes
    public static class MethodAnnotatedCases {
        @DisableBetweenTimes(start = TIME_IN_RANGE_START, end = TIME_IN_RANGE_END, throwSkipException = false)
        public void inRange() {}

        @DisableBetweenTimes(start = TIME_IN_RANGE_START_EXTRA_WHITESPACE, end = TIME_IN_RANGE_END_EXTRA_WHITESPACE, throwSkipException = false)
        public void inRangeExtraWhitespace() {}

        @DisableBetweenTimes(start = TIME_ON_BOUNDARY, end = TIME_IN_RANGE_END, throwSkipException = false)
        public void inRangeStartOnBoundary() {}

        @DisableBetweenTimes(start = TIME_OUT_OF_RANGE_START, end = TIME_OUT_OF_RANGE_END, throwSkipException = false)
        public void outOfRange() {}

        @DisableBetweenTimes(start = TIME_IN_RANGE_END, end = TIME_IN_RANGE_START, throwSkipException = false)
        public void endBeforeStart() {}

        @DisableBetweenTimes(start = TIME_INVALID_TIME, end = TIME_OUT_OF_RANGE_END, throwSkipException = false)
        public void invalidStartTime() {}

        @DisableBetweenTimes(start = TIME_IN_RANGE_START, end = TIME_IN_RANGE_END, throwSkipException = true)
        public void inRangeThrowSkip() {}

        @DisableBetweenTimes(start = TIME_IN_RANGE_START_EXTRA_WHITESPACE, end = TIME_IN_RANGE_END_EXTRA_WHITESPACE, throwSkipException = true)
        public void inRangeThrowSkipExtraWhitespace() {}

        @DisableBetweenTimes(start = TIME_IN_RANGE_START, end = TIME_ON_BOUNDARY, throwSkipException = true)
        public void inRangeThrowSkipEndOnBoundary() {}

        @DisableBetweenTimes(start = TIME_OUT_OF_RANGE_START, end = TIME_OUT_OF_RANGE_END, throwSkipException = true)
        public void outOfRangeThrowSkip() {}

        @DisableBetweenTimes(start = TIME_IN_RANGE_END, end = TIME_IN_RANGE_START, throwSkipException = true)
        public void endBeforeStartThrowSkip() {}

        @DisableBetweenTimes(start = TIME_INVALID_TIME, end = TIME_OUT_OF_RANGE_END, throwSkipException = true)
        public void invalidStartTimeThrowSkip() {}

        @DisableBetweenTimes(start = TIME_ON_BOUNDARY, end = TIME_ON_BOUNDARY, timezone = TIMEZONE_UTC, throwSkipException = false)
        public void inRangeTimezoneUTC() {}

        @DisableBetweenTimes(start = TIME_ON_BOUNDARY, end = TIME_ON_BOUNDARY, timezone = TIMEZONE_OFFSET_MINUS_11, throwSkipException = false)
        public void inRangeTimezoneMinus11() {}

        @DisableBetweenTimes(start = TIME_ON_BOUNDARY, end = TIME_ON_BOUNDARY, timezone = TIMEZONE_OFFSET_PLUS_14, throwSkipException = false)
        public void inRangeTimezonePlus14() {}

        @DisableBetweenTimes(start = TIME_IN_RANGE_START, end = TIME_IN_RANGE_END, timezone = INVALID_TIMEZONE, throwSkipException = false)
        public void invalidTimezone() {}

        @DisableBetweenTimes(start = TIME_ON_BOUNDARY, end = TIME_ON_BOUNDARY, timezone = TIMEZONE_UTC, throwSkipException = true)
        public void inRangeTimezoneUTCThrowSkip() {}

        @DisableBetweenTimes(start = TIME_IN_RANGE_START, end = TIME_IN_RANGE_END, timezone = " UTC ", throwSkipException = false)
        public void inRangeTimezoneWithWhitespace() {}

        @DisableBetweenTimes(start = TIME_IN_RANGE_START, end = TIME_IN_RANGE_END, timezone = " UTC ", throwSkipException = true)
        public void inRangeTimezoneWithWhitespaceThrowSkip() {}

        @DisableBetweenTimes(start = TIME_ON_BOUNDARY, end = TIME_ON_BOUNDARY, timezone = TIMEZONE_OFFSET_MINUS_11, throwSkipException = true)
        public void inRangeTimezoneMinus11ThrowSkip() {}

        @DisableBetweenTimes(start = TIME_ON_BOUNDARY, end = TIME_ON_BOUNDARY, timezone = TIMEZONE_OFFSET_PLUS_14, throwSkipException = true)
        public void inRangeTimezonePlus14ThrowSkip() {}

        public void noAnnotation() {}
    }

    //region Tests with throwSkipException = false
    @DisableBetweenTimes(start = TIME_IN_RANGE_START, end = TIME_IN_RANGE_END, throwSkipException = false)
    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Empty method is intentional for test purposes
    public static class NoThrowSkipClassInRange {
        @DisableBetweenTimes(start = TIME_OUT_OF_RANGE_START, end = TIME_OUT_OF_RANGE_END, throwSkipException = false)
        public void classInRange() {}

        @DisableBetweenTimes(start = TIME_EARLY_MORNING_START, end = TIME_EARLY_MORNING_END, throwSkipException = false)
        public void bothInRange() {}

        @DisableBetweenTimes(start = TIME_INVALID_TIME, end = TIME_OUT_OF_RANGE_END, throwSkipException = false)
        public void invalidStartTime() {}

        public void noAnnotation() {}
    }

    @DisableBetweenTimes(start = TIME_OUT_OF_RANGE_START, end = TIME_OUT_OF_RANGE_END, throwSkipException = false)
    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Empty method is intentional for test purposes
    public static class NoThrowSkipClassOutOfRange {
        @DisableBetweenTimes(start = TIME_IN_RANGE_START, end = TIME_IN_RANGE_END, throwSkipException = false)
        public void methodInRange() {}

        @DisableBetweenTimes(start = TIME_EARLY_MORNING_START, end = TIME_EARLY_MORNING_END, throwSkipException = false)
        public void neitherMatches() {}

        @DisableBetweenTimes(start = TIME_INVALID_TIME, end = TIME_OUT_OF_RANGE_END, throwSkipException = false)
        public void invalidStartTime() {}

        public void noAnnotation() {}
    }

    static Stream<Arguments> noThrowSkipArgumentsProvider() {
        return Stream.of(
                Arguments.of("MethodAnnotatedCases", "inRange", MethodAnnotatedCases.class, true, null),
                Arguments.of("MethodAnnotatedCases", "inRangeExtraWhitespace", MethodAnnotatedCases.class, true, null),
                Arguments.of("MethodAnnotatedCases", "inRangeStartOnBoundary", MethodAnnotatedCases.class, true, null),
                Arguments.of("MethodAnnotatedCases", "outOfRange", MethodAnnotatedCases.class, false, null),
                Arguments.of("MethodAnnotatedCases", "endBeforeStart", MethodAnnotatedCases.class, false, IllegalArgumentException.class),
                Arguments.of("MethodAnnotatedCases", "invalidStartTime", MethodAnnotatedCases.class, false, DateTimeParseException.class),
                Arguments.of("MethodAnnotatedCases", "invalidTimezone", MethodAnnotatedCases.class, false, IllegalArgumentException.class),
                Arguments.of("MethodAnnotatedCases", "noAnnotation", MethodAnnotatedCases.class, false, null),

                Arguments.of("NoThrowSkipClassInRange", "classInRange", NoThrowSkipClassInRange.class, true, null),
                Arguments.of("NoThrowSkipClassInRange", "bothInRange", NoThrowSkipClassInRange.class, true, null),
                Arguments.of("NoThrowSkipClassInRange", "invalidStartTime", NoThrowSkipClassInRange.class, false, DateTimeParseException.class),
                Arguments.of("NoThrowSkipClassInRange", "noAnnotation", NoThrowSkipClassInRange.class, true, null),

                Arguments.of("NoThrowSkipClassOutOfRange", "methodInRange", NoThrowSkipClassOutOfRange.class, true, null),
                Arguments.of("NoThrowSkipClassOutOfRange", "neitherMatches", NoThrowSkipClassOutOfRange.class, false, null),
                Arguments.of("NoThrowSkipClassOutOfRange", "invalidStartTime", NoThrowSkipClassOutOfRange.class, false, DateTimeParseException.class),
                Arguments.of("NoThrowSkipClassOutOfRange", "noAnnotation", NoThrowSkipClassOutOfRange.class, false, null),

                // Mixed throwSkipException values - class has throwSkipException=true with in-range time
                Arguments.of("MixedThrowSkipOnClass", "methodDisabledOutOfRange", MixedThrowSkipOnClass.class, false, null),
                Arguments.of("MixedThrowSkipOnClass", "methodThrowOutOfRange", MixedThrowSkipOnClass.class, false, null),
                Arguments.of("MixedThrowSkipOnClass", "noMethodAnnotation", MixedThrowSkipOnClass.class, false, null),

                // Mixed throwSkipException values - class has throwSkipException=false with in-range time, so should disable
                Arguments.of("MixedThrowSkipOnClassDisabledInRange", "methodThrowInRange", MixedThrowSkipOnClassDisabledInRange.class, true, null),
                Arguments.of("MixedThrowSkipOnClassDisabledInRange", "methodThrowOutOfRange", MixedThrowSkipOnClassDisabledInRange.class, true, null),


                // Class has throwSkipException=false, method has throwSkipException=true with in-range time, so should disable
                Arguments.of("MixedThrowSkipOnClassDisabledNotInRange", "methodThrowInRange", MixedThrowSkipOnClassDisabledNotInRange.class, false, null)
        );
    }

    @ParameterizedTest(name = "{0}.{1} with throwSkipException=false")
    @MethodSource(value = "noThrowSkipArgumentsProvider")
    <T extends Throwable> void givenClassOrMethodInRange_whenCallTransform_thenDisableIfInRange(
            String className, String testMethod, Class<?> testClass, boolean shouldDisable, Class<T> expectedException) throws Exception {
        // Arrange
        Clock clock = createFixedClock();
        DisableBetweenTimesListener transformer = new DisableBetweenTimesListener(clock);

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
        // If the method is in range, setEnabled(false) should be called once. Otherwise, it should never be called.
        if (shouldDisable) {
            verify(mockAnnotation, times(1)).setEnabled(false);
        } else {
            verify(mockAnnotation, never()).setEnabled(false);
        }
    }
    //endregion Tests with throwSkipException = false

    //region Tests with throwSkipException = true
    @DisableBetweenTimes(start = TIME_IN_RANGE_START, end = TIME_IN_RANGE_END, throwSkipException = true)
    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Empty method is intentional for test purposes
    public static class ThrowSkipClassInRange {
        @DisableBetweenTimes(start = TIME_OUT_OF_RANGE_START, end = TIME_OUT_OF_RANGE_END, throwSkipException = true)
        public void classInRange() {}

        @DisableBetweenTimes(start = TIME_EARLY_MORNING_START, end = TIME_EARLY_MORNING_END, throwSkipException = true)
        public void bothInRange() {}

        @DisableBetweenTimes(start = TIME_INVALID_TIME, end = TIME_OUT_OF_RANGE_END, throwSkipException = true)
        public void invalidStartTime() {}

        public void noAnnotation() {}
    }

    @DisableBetweenTimes(start = TIME_OUT_OF_RANGE_START, end = TIME_OUT_OF_RANGE_END, throwSkipException = true)
    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Empty method is intentional for test purposes
    public static class ThrowSkipClassOutOfRange {
        @DisableBetweenTimes(start = TIME_IN_RANGE_START, end = TIME_IN_RANGE_END, throwSkipException = true)
        public void methodInRange() {}

        @DisableBetweenTimes(start = TIME_EARLY_MORNING_START, end = TIME_EARLY_MORNING_END, throwSkipException = true)
        public void neitherMatches() {}

        @DisableBetweenTimes(start = TIME_INVALID_TIME, end = TIME_OUT_OF_RANGE_END, throwSkipException = true)
        public void invalidStartTime() {}

        public void noAnnotation() {}
    }

    @DisableBetweenTimes(start = TIME_INVALID_TIME, end = TIME_IN_RANGE_END, throwSkipException = true)
    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Empty method is intentional for test purposes
    public static class ClassWithMalformedTimeThrowSkip {
        @DisableBetweenTimes(start = TIME_IN_RANGE_START, end = TIME_IN_RANGE_END, throwSkipException = true)
        public void methodInRange() {}

        @DisableBetweenTimes(start = TIME_EARLY_MORNING_START, end = TIME_EARLY_MORNING_END, throwSkipException = true)
        public void neitherMatches() {}

        public void noAnnotation() {}
    }

    //region Tests with mixed throwSkipException values
    @DisableBetweenTimes(start = TIME_IN_RANGE_START, end = TIME_IN_RANGE_END, timezone = TIMEZONE_UTC, throwSkipException = true)
    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Empty method is intentional for test purposes
    public static class MixedThrowSkipOnClass {
        // Method annotation with throwSkipException=false doesn't affect beforeInvocation (which looks for throwSkipException=true)
        // Class annotation still has throwSkipException=true and is in range, so should throw
        @DisableBetweenTimes(start = TIME_OUT_OF_RANGE_START, end = TIME_OUT_OF_RANGE_END, throwSkipException = false)
        public void methodDisabledOutOfRange() {}

        // Method with throwSkipException=true but out of range - class is in range with throwSkipException=true
        @DisableBetweenTimes(start = TIME_OUT_OF_RANGE_START, end = TIME_OUT_OF_RANGE_END, throwSkipException = true)
        public void methodThrowOutOfRange() {}

        // Method without annotation should inherit class behavior
        public void noMethodAnnotation() {}
    }

    @DisableBetweenTimes(start = TIME_IN_RANGE_START, end = TIME_IN_RANGE_END, throwSkipException = false)
    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Empty method is intentional for test purposes
    public static class MixedThrowSkipOnClassDisabledInRange {
        // Method has throwSkipException=true and both are in range, so should throw
        @DisableBetweenTimes(start = TIME_IN_RANGE_START, end = TIME_IN_RANGE_END, throwSkipException = true)
        public void methodThrowInRange() {}

        // Method with throwSkipException=true but out of range - class is in range with throwSkipException=false, so should not throw
        @DisableBetweenTimes(start = TIME_OUT_OF_RANGE_START, end = TIME_OUT_OF_RANGE_END, throwSkipException = true)
        public void methodThrowOutOfRange() {}
    }

    @DisableBetweenTimes(start = TIME_OUT_OF_RANGE_START, end = TIME_OUT_OF_RANGE_END, throwSkipException = false)
    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Empty method is intentional for test purposes
    public static class MixedThrowSkipOnClassDisabledNotInRange {
        // Method has throwSkipException=true and is in range, so should throw
        @DisableBetweenTimes(start = TIME_IN_RANGE_START, end = TIME_IN_RANGE_END, throwSkipException = true)
        public void methodThrowInRange() {}
    }
    //endregion Tests with mixed throwSkipException values

    static Stream<Arguments> throwSkipArgumentsProvider() {
        return Stream.of(
                Arguments.of("MethodAnnotatedCases", "inRangeThrowSkip", MethodAnnotatedCases.class, SkipException.class),
                Arguments.of("MethodAnnotatedCases", "inRangeThrowSkipExtraWhitespace", MethodAnnotatedCases.class, SkipException.class),
                Arguments.of("MethodAnnotatedCases", "inRangeThrowSkipEndOnBoundary", MethodAnnotatedCases.class, SkipException.class),
                Arguments.of("MethodAnnotatedCases", "outOfRangeThrowSkip", MethodAnnotatedCases.class, null),
                Arguments.of("MethodAnnotatedCases", "endBeforeStartThrowSkip", MethodAnnotatedCases.class, IllegalArgumentException.class),
                Arguments.of("MethodAnnotatedCases", "invalidStartTimeThrowSkip", MethodAnnotatedCases.class, DateTimeParseException.class),

                Arguments.of("ThrowSkipClassInRange", "classInRange", ThrowSkipClassInRange.class, SkipException.class),
                Arguments.of("ThrowSkipClassInRange", "bothInRange", ThrowSkipClassInRange.class, SkipException.class),
                Arguments.of("ThrowSkipClassInRange", "noAnnotation", ThrowSkipClassInRange.class, SkipException.class),
                Arguments.of("ThrowSkipClassInRange", "invalidStartTime", ThrowSkipClassInRange.class, DateTimeParseException.class),

                Arguments.of("ThrowSkipClassOutOfRange", "methodInRange", ThrowSkipClassOutOfRange.class, SkipException.class),
                Arguments.of("ThrowSkipClassOutOfRange", "neitherMatches", ThrowSkipClassOutOfRange.class, null),
                Arguments.of("ThrowSkipClassOutOfRange", "noAnnotation", ThrowSkipClassOutOfRange.class, null),
                Arguments.of("ThrowSkipClassOutOfRange", "invalidStartTime", ThrowSkipClassOutOfRange.class, DateTimeParseException.class),

                Arguments.of("ClassWithMalformedTimeThrowSkip", "methodInRange", ClassWithMalformedTimeThrowSkip.class, SkipException.class),
                Arguments.of("ClassWithMalformedTimeThrowSkip", "neitherMatches", ClassWithMalformedTimeThrowSkip.class, DateTimeParseException.class),
                Arguments.of("ClassWithMalformedTimeThrowSkip", "noAnnotation", ClassWithMalformedTimeThrowSkip.class, DateTimeParseException.class),

                // Mixed throwSkipException values - class has throwSkipException=true with in-range time, so should throw
                Arguments.of("MixedThrowSkipOnClass", "methodDisabledOutOfRange", MixedThrowSkipOnClass.class, SkipException.class),
                Arguments.of("MixedThrowSkipOnClass", "methodThrowOutOfRange", MixedThrowSkipOnClass.class, SkipException.class),
                Arguments.of("MixedThrowSkipOnClass", "noMethodAnnotation", MixedThrowSkipOnClass.class, SkipException.class),

                // Mixed throwSkipException values - class has throwSkipException=false with in-range time
                Arguments.of("MixedThrowSkipOnClassDisabledInRange", "methodThrowInRange", MixedThrowSkipOnClassDisabledInRange.class, SkipException.class),
                Arguments.of("MixedThrowSkipOnClassDisabledInRange", "methodThrowOutOfRange", MixedThrowSkipOnClassDisabledInRange.class, null),

                // Method has throwSkipException=true with in-range time, so should throw
                Arguments.of("MixedThrowSkipOnClassDisabledNotInRange", "methodThrowInRange", MixedThrowSkipOnClassDisabledNotInRange.class, SkipException.class)
        );
    }

    @ParameterizedTest(name = "{0}.{1} with throwSkipException=true")
    @MethodSource(value = "throwSkipArgumentsProvider")
    <T extends Throwable> void givenClassOrMethodInRange_whenCallBeforeInvocation_thenThrowIfInRange(
            String className, String testMethod, Class<?> testClass, Class<T> expectedException) throws Exception {
        // Arrange
        Clock clock = createFixedClock();
        DisableBetweenTimesListener transformer = new DisableBetweenTimesListener(clock);

        Method method = testClass.getMethod(testMethod);
        IInvokedMethod invoked = mockInvokedMethod(method, testClass);
        ITestAnnotation mockAnnotation = Mockito.mock(ITestAnnotation.class);
        ITestResult result = Mockito.mock(ITestResult.class);

        // Act & Assert
        // setEnabled(false) should never be called in transform when throwSkipException=true, even for in-range methods, since beforeInvocation should handle skipping by throwing SkipException.
        verify(mockAnnotation, never()).setEnabled(false);

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
    //endregion Tests with throwSkipException = true

    //region Timezone tests
    static Stream<Arguments> timezoneNoThrowSkipArgumentsProvider() {
        return Stream.of(
                Arguments.of(TIMEZONE_UTC, "inRangeTimezoneUTC", true),
                Arguments.of(TIMEZONE_UTC, "inRangeTimezoneWithWhitespace", true),
                // When clock is UTC 12:30:00, in -11:00 timezone it would be 01:30:00 same day, which is NOT in the 12:30-12:30 range
                Arguments.of(TIMEZONE_UTC, "inRangeTimezoneMinus11", false),
                // When clock is UTC 12:30:00, in +14:00 timezone it would be 02:30:00 next day, which is NOT in the 12:30-12:30 range
                Arguments.of(TIMEZONE_UTC, "inRangeTimezonePlus14", false),
                // When clock is -11:00 12:30:00, in UTC it would be 23:30:00 same day, which is NOT in the 12:30-12:30 UTC range
                Arguments.of(TIMEZONE_OFFSET_MINUS_11, "inRangeTimezoneUTC", false),
                Arguments.of(TIMEZONE_OFFSET_MINUS_11, "inRangeTimezoneMinus11", true),
                Arguments.of(TIMEZONE_OFFSET_PLUS_14, "inRangeTimezonePlus14", true)
        );
    }

    @ParameterizedTest(name = "clock timezone {0} with method {1} should disable {2}")
    @MethodSource(value = "timezoneNoThrowSkipArgumentsProvider")
    void givenDifferentClockAndAnnotationTimezones_whenCallTransform_thenDisableCorrectly(
            String clockTimezone, String methodName, boolean shouldDisable) throws Exception {
        // Arrange
        Clock clock = createFixedClock(ZoneId.of(clockTimezone));
        DisableBetweenTimesListener transformer = new DisableBetweenTimesListener(clock);

        Method method = MethodAnnotatedCases.class.getMethod(methodName);
        ITestAnnotation mockAnnotation = Mockito.mock(ITestAnnotation.class);

        // Act
        assertDoesNotThrow(() -> transformer.transform(mockAnnotation, MethodAnnotatedCases.class, method));

        // Assert
        // If the method is in range, setEnabled(false) should be called once. Otherwise, it should never be called.
        if (shouldDisable) {
            verify(mockAnnotation, times(1)).setEnabled(false);
        } else {
            verify(mockAnnotation, never()).setEnabled(false);
        }
    }

    static Stream<Arguments> timezoneThrowSkipArgumentsProvider() {
        return Stream.of(
                Arguments.of(TIMEZONE_UTC, "inRangeTimezoneUTCThrowSkip", SkipException.class),
                Arguments.of(TIMEZONE_UTC, "inRangeTimezoneWithWhitespaceThrowSkip", SkipException.class),
                // This test shouldn't be skipped since the clock timezone is UTC and the annotation timezone is -11:00, which is 1:30am on the same day.
                Arguments.of(TIMEZONE_UTC, "inRangeTimezoneMinus11ThrowSkip", null),
                // When clock is UTC 12:30:00, in +14:00 timezone it would be 02:30:00 next day, which is NOT in the 12:30-12:30 range
                Arguments.of(TIMEZONE_UTC, "inRangeTimezonePlus14ThrowSkip", null),
                // When clock is -11:00 12:30:00, in UTC it would be 23:30:00 same day, which is NOT in the 12:30-12:30 UTC range
                Arguments.of(TIMEZONE_OFFSET_MINUS_11, "inRangeTimezoneUTCThrowSkip", null),
                Arguments.of(TIMEZONE_OFFSET_MINUS_11, "inRangeTimezoneMinus11ThrowSkip", SkipException.class),
                Arguments.of(TIMEZONE_OFFSET_PLUS_14, "inRangeTimezonePlus14ThrowSkip", SkipException.class)
        );
    }

    @ParameterizedTest(name = "clock timezone {0} with method {1} should throw {2}")
    @MethodSource(value = "timezoneThrowSkipArgumentsProvider")
    <T extends Throwable> void givenDifferentClockAndAnnotationTimezones_whenCallBeforeInvocation_thenThrowCorrectly(
            String clockTimezone, String methodName, Class<T> expectedException) throws Exception {
        // Arrange
        Clock clock = createFixedClock(ZoneId.of(clockTimezone));
        DisableBetweenTimesListener transformer = new DisableBetweenTimesListener(clock);

        Method method = MethodAnnotatedCases.class.getMethod(methodName);
        IInvokedMethod invoked = mockInvokedMethod(method, MethodAnnotatedCases.class);
        ITestResult result = Mockito.mock(ITestResult.class);

        // Act & Assert
        if (expectedException != null) {
            assertThrows(expectedException, () -> transformer.beforeInvocation(invoked, result));
        } else {
            assertDoesNotThrow(() -> transformer.beforeInvocation(invoked, result));
        }
    }
    //endregion Timezone tests

    /**
     * Creates a fixed clock set to 12:30:00 UTC for consistent testing.
     */
    private static Clock createFixedClock() {
        return createFixedClock(FIXED_ZONE);
    }

    /**
     * Creates a fixed clock set to 12:30:00 in the specified timezone for consistent testing.
     */
    private static Clock createFixedClock(@NonNull ZoneId zone) {
        ZonedDateTime zdt = FIXED_TIME.atDate(java.time.LocalDate.of(2026, 5, 11)).atZone(zone);
        return Clock.fixed(zdt.toInstant(), zone);
    }

    /**
     * Creates a mock IInvokedMethod for testing beforeInvocation.
     */
    private static IInvokedMethod mockInvokedMethod(@NonNull Method method, Class<?> testClass) {
        IInvokedMethod invoked = Mockito.mock(IInvokedMethod.class);
        ITestNGMethod testMethod = Mockito.mock(ITestNGMethod.class);

        when(invoked.getTestMethod()).thenReturn(testMethod);
        when(testMethod.getConstructorOrMethod()).thenReturn(new ConstructorOrMethod(method));
        when(testMethod.getRealClass()).thenReturn(testClass);
        when(testMethod.getMethodName()).thenReturn(method.getName());

        return invoked;
    }
}
