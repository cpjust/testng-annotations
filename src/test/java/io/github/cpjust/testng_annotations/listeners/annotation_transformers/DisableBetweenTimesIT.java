package io.github.cpjust.testng_annotations.listeners.annotation_transformers;

import io.github.cpjust.testng_annotations.TestUtils;
import io.github.cpjust.testng_annotations.annotations.DisableBetweenTimes;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import static io.github.cpjust.testng_annotations.TestUtils.getCurrentMethodNameWithParams;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

@Slf4j
public class DisableBetweenTimesIT {
    static final String METHODS_SHOULD_NOT_HAVE_IDENTICAL_IMPLEMENTATIONS = "java:S4144"; // Suppress "Methods should not have identical implementations" warning
    static final String SIMILAR_TESTS_SHOULD_BE_PARAMETRIZED = "java:S5976"; // Suppress "Similar tests should be grouped in a single Parameterized test" warning

    private static final List<String> testsRun = Collections.synchronizedList(new java.util.ArrayList<>());
    // Times that are extremely unlikely to match the current time.
    private static final String TIME_IN_PAST_START = "00:00:00";
    private static final String TIME_IN_PAST_END = "00:00:01";
    private static final String TIME_IN_FUTURE_START = "23:59:58";
    private static final String TIME_IN_FUTURE_END = "23:59:59";
    // Times that will match the current time.
    private static final String TIME_IN_RANGE_START = "00:00:00";
    private static final String TIME_IN_RANGE_END = "23:59:59";

    private static final List<String> EXPECTED_RUN_TESTS = List.of(
            "noAnnotation_isRun()",
            "oneAnnotation_futureDisabledRange_isRun()",
            "oneAnnotationWithTimezone_futureDisabledRange_isRun()",
            "oneAnnotation_pastDisabledRange_withThrowSkipException_isRun()",
            "oneAnnotationWithTimezone_pastDisabledRange_withThrowSkipException_isRun()"
    );

    private static final List<String> EXPECTED_DISABLED_TESTS = List.of(
            "oneAnnotation_currentTimeDisabledRange_isNotRun()",
            "oneAnnotationWithTimezone_currentTimeDisabledRange_isNotRun()",
            "twoAnnotations_firstAnnotationShouldExclude_isNotRun()",
            "twoAnnotations_secondAnnotationShouldExclude_isNotRun()",
            // throwSkipException-based disabled tests
            "oneAnnotation_currentTimeDisabledRange_withThrowSkipException_isNotRun()",
            "oneAnnotationWithTimezone_currentTimeDisabledRange_withThrowSkipException_isNotRun()",
            "twoAnnotations_firstAnnotationShouldExclude_withThrowSkipException_isNotRun()",
            "twoAnnotations_secondAnnotationShouldExclude_withThrowSkipException_isNotRun()"
    );

    @Test
    public void noAnnotation_isRun() {
        testsRun.add(getCurrentMethodNameWithParams());
    }

    //region Tests with throwSkipException = false
    // Use a time range in the future so the test is not disabled by current time.
    @DisableBetweenTimes(start = TIME_IN_FUTURE_START, end = TIME_IN_FUTURE_END, throwSkipException = false)
    @Test
    public void oneAnnotation_futureDisabledRange_isRun() {
        testsRun.add(getCurrentMethodNameWithParams());
    }

    @DisableBetweenTimes(start = TIME_IN_FUTURE_START, end = TIME_IN_FUTURE_END, timezone = "GMT", throwSkipException = false)
    @Test
    public void oneAnnotationWithTimezone_futureDisabledRange_isRun() {
        testsRun.add(getCurrentMethodNameWithParams());
    }

    @DisableBetweenTimes(start = TIME_IN_RANGE_START, end = TIME_IN_RANGE_END, throwSkipException = false)
    @SuppressWarnings({SIMILAR_TESTS_SHOULD_BE_PARAMETRIZED, METHODS_SHOULD_NOT_HAVE_IDENTICAL_IMPLEMENTATIONS})
    @Test
    public void oneAnnotation_currentTimeDisabledRange_isNotRun() {
        testsRun.add(getCurrentMethodNameWithParams());
        TestUtils.failTestThatShouldNotRun();
    }

    @DisableBetweenTimes(start = TIME_IN_RANGE_START, end = TIME_IN_RANGE_END, timezone = "America/Chicago", throwSkipException = false)
    @SuppressWarnings({SIMILAR_TESTS_SHOULD_BE_PARAMETRIZED, METHODS_SHOULD_NOT_HAVE_IDENTICAL_IMPLEMENTATIONS})
    @Test
    public void oneAnnotationWithTimezone_currentTimeDisabledRange_isNotRun() {
        testsRun.add(getCurrentMethodNameWithParams());
        TestUtils.failTestThatShouldNotRun();
    }

    @DisableBetweenTimes(start = TIME_IN_RANGE_START, end = TIME_IN_RANGE_END, throwSkipException = false)
    @DisableBetweenTimes(start = TIME_IN_FUTURE_START, end = TIME_IN_FUTURE_END, throwSkipException = false)
    @SuppressWarnings({SIMILAR_TESTS_SHOULD_BE_PARAMETRIZED, METHODS_SHOULD_NOT_HAVE_IDENTICAL_IMPLEMENTATIONS})
    @Test
    public void twoAnnotations_firstAnnotationShouldExclude_isNotRun() {
        testsRun.add(getCurrentMethodNameWithParams());
        TestUtils.failTestThatShouldNotRun();
    }

    @DisableBetweenTimes(start = TIME_IN_FUTURE_START, end = TIME_IN_FUTURE_END, throwSkipException = false)
    @DisableBetweenTimes(start = TIME_IN_RANGE_START, end = TIME_IN_RANGE_END, throwSkipException = false)
    @SuppressWarnings({SIMILAR_TESTS_SHOULD_BE_PARAMETRIZED, METHODS_SHOULD_NOT_HAVE_IDENTICAL_IMPLEMENTATIONS})
    @Test
    public void twoAnnotations_secondAnnotationShouldExclude_isNotRun() {
        testsRun.add(getCurrentMethodNameWithParams());
        TestUtils.failTestThatShouldNotRun();
    }
    //endregion Tests with throwSkipException = false

    //region Tests with throwSkipException = true
    // Use a time range in the past so the test is not disabled by current time.
    @DisableBetweenTimes(start = TIME_IN_PAST_START, end = TIME_IN_PAST_END, throwSkipException = true)
    @Test
    public void oneAnnotation_pastDisabledRange_withThrowSkipException_isRun() {
        testsRun.add(getCurrentMethodNameWithParams());
    }

    @DisableBetweenTimes(start = TIME_IN_PAST_START, end = TIME_IN_PAST_END, timezone = "-08:00", throwSkipException = true)
    @Test
    public void oneAnnotationWithTimezone_pastDisabledRange_withThrowSkipException_isRun() {
        testsRun.add(getCurrentMethodNameWithParams());
    }

    @DisableBetweenTimes(start = TIME_IN_RANGE_START, end = TIME_IN_RANGE_END, throwSkipException = true)
    @SuppressWarnings({SIMILAR_TESTS_SHOULD_BE_PARAMETRIZED, METHODS_SHOULD_NOT_HAVE_IDENTICAL_IMPLEMENTATIONS})
    @Test
    public void oneAnnotation_currentTimeDisabledRange_withThrowSkipException_isNotRun() {
        testsRun.add(getCurrentMethodNameWithParams());
        TestUtils.failTestThatShouldNotRun();
    }

    @DisableBetweenTimes(start = TIME_IN_RANGE_START, end = TIME_IN_RANGE_END, timezone = "UTC", throwSkipException = true)
    @SuppressWarnings({SIMILAR_TESTS_SHOULD_BE_PARAMETRIZED, METHODS_SHOULD_NOT_HAVE_IDENTICAL_IMPLEMENTATIONS})
    @Test
    public void oneAnnotationWithTimezone_currentTimeDisabledRange_withThrowSkipException_isNotRun() {
        testsRun.add(getCurrentMethodNameWithParams());
        TestUtils.failTestThatShouldNotRun();
    }

    @DisableBetweenTimes(start = TIME_IN_RANGE_START, end = TIME_IN_RANGE_END, throwSkipException = true)
    @DisableBetweenTimes(start = TIME_IN_FUTURE_START, end = TIME_IN_FUTURE_END, throwSkipException = true)
    @SuppressWarnings({SIMILAR_TESTS_SHOULD_BE_PARAMETRIZED, METHODS_SHOULD_NOT_HAVE_IDENTICAL_IMPLEMENTATIONS})
    @Test
    public void twoAnnotations_firstAnnotationShouldExclude_withThrowSkipException_isNotRun() {
        testsRun.add(getCurrentMethodNameWithParams());
        TestUtils.failTestThatShouldNotRun();
    }

    @DisableBetweenTimes(start = TIME_IN_FUTURE_START, end = TIME_IN_FUTURE_END, throwSkipException = true)
    @DisableBetweenTimes(start = TIME_IN_RANGE_START, end = TIME_IN_RANGE_END, throwSkipException = true)
    @SuppressWarnings({SIMILAR_TESTS_SHOULD_BE_PARAMETRIZED, METHODS_SHOULD_NOT_HAVE_IDENTICAL_IMPLEMENTATIONS})
    @Test
    public void twoAnnotations_secondAnnotationShouldExclude_withThrowSkipException_isNotRun() {
        testsRun.add(getCurrentMethodNameWithParams());
        TestUtils.failTestThatShouldNotRun();
    }
    //endregion Tests with throwSkipException = true

    @Test(priority = 2)
    public void verifyIncludedTests() {
        assertThat("Wrong number of tests run!", testsRun, hasSize(EXPECTED_RUN_TESTS.size()));
        EXPECTED_RUN_TESTS.forEach(e -> assertThat("Expected test to be run", testsRun, hasItem(e)));
    }

    @Test(priority = 2)
    public void verifyDisabledTests() {
        EXPECTED_DISABLED_TESTS.forEach(excludedTest ->
                assertThat("Expected test to be disabled", testsRun, not(hasItem(excludedTest))));

        // Added extra checks in case test refactoring break the above asserts.
        testsRun.forEach(test ->
                assertThat("Disabled test was run!", test, not(containsString("_isNotRun("))));
    }
}
