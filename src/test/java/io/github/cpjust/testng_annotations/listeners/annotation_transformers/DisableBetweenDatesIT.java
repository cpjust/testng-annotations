package io.github.cpjust.testng_annotations.listeners.annotation_transformers;

import io.github.cpjust.testng_annotations.TestUtils;
import io.github.cpjust.testng_annotations.annotations.DisableBetweenDates;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static io.github.cpjust.testng_annotations.TestUtils.getCurrentMethodNameWithParams;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

@Slf4j
public class DisableBetweenDatesIT {
    static final String METHODS_SHOULD_NOT_HAVE_IDENTICAL_IMPLEMENTATIONS = "java:S4144"; // Suppress "Methods should not have identical implementations" warning
    static final String SIMILAR_TESTS_SHOULD_BE_PARAMETRIZED = "java:S5976"; // Suppress "Similar tests should be grouped in a single Parameterized test" warning

    private static final List<String> testsRun = new ArrayList<>();
    private static final String DATE_IN_PAST_START = "2000-01-01";
    private static final String DATE_IN_PAST2_START = "1999-01-01";
    private static final String DATE_IN_PAST_END = "2000-12-31";
    private static final String DATE_IN_PAST2_END = "1999-12-31";
    private static final String DATE_IN_FUTURE_START = "2999-01-01";
    private static final String DATE_IN_FUTURE_END = "2999-12-31";

    @Test
    public void noAnnotation_isRun() {
        testsRun.add(getCurrentMethodNameWithParams());
    }

    // Use a far future range so the test is not disabled by current date
    @DisableBetweenDates(start = DATE_IN_FUTURE_START, end = DATE_IN_FUTURE_END, throwSkipException = false)
    @Test
    public void oneAnnotation_futureDisabledRange_isRun() {
        testsRun.add(getCurrentMethodNameWithParams());
    }

    // Use a past range so the test is not disabled by current date
    @DisableBetweenDates(start = DATE_IN_PAST_START, end = DATE_IN_PAST_END, throwSkipException = true)
    @Test
    public void oneAnnotation_pastDisabledRange_throwSkipException_isRun() {
        testsRun.add(getCurrentMethodNameWithParams());
    }

    @DisableBetweenDates(start = DATE_IN_PAST_START, end = DATE_IN_FUTURE_END, throwSkipException = false)
    @SuppressWarnings({SIMILAR_TESTS_SHOULD_BE_PARAMETRIZED, METHODS_SHOULD_NOT_HAVE_IDENTICAL_IMPLEMENTATIONS})
    @Test
    public void oneAnnotation_currentDateDisabledRange_isNotRun() {
        testsRun.add(getCurrentMethodNameWithParams());
        TestUtils.failTestThatShouldNotRun();
    }

    @DisableBetweenDates(start = DATE_IN_PAST_START, end = DATE_IN_FUTURE_END, throwSkipException = false)
    @DisableBetweenDates(start = DATE_IN_PAST2_START, end = DATE_IN_PAST2_END, throwSkipException = false)
    @SuppressWarnings({SIMILAR_TESTS_SHOULD_BE_PARAMETRIZED, METHODS_SHOULD_NOT_HAVE_IDENTICAL_IMPLEMENTATIONS})
    @Test
    public void twoAnnotations_firstAnnotationShouldExclude_isNotRun() {
        testsRun.add(getCurrentMethodNameWithParams());
        TestUtils.failTestThatShouldNotRun();
    }

    @DisableBetweenDates(start = DATE_IN_PAST2_START, end = DATE_IN_PAST2_END, throwSkipException = false)
    @DisableBetweenDates(start = DATE_IN_PAST_START, end = DATE_IN_FUTURE_END, throwSkipException = false)
    @SuppressWarnings({SIMILAR_TESTS_SHOULD_BE_PARAMETRIZED, METHODS_SHOULD_NOT_HAVE_IDENTICAL_IMPLEMENTATIONS})
    @Test
    public void twoAnnotations_secondAnnotationShouldExclude_isNotRun() {
        testsRun.add(getCurrentMethodNameWithParams());
        TestUtils.failTestThatShouldNotRun();
    }

    //region Tests with throwSkipException = true
    @DisableBetweenDates(start = DATE_IN_PAST_START, end = DATE_IN_FUTURE_END, throwSkipException = true)
    @SuppressWarnings({SIMILAR_TESTS_SHOULD_BE_PARAMETRIZED, METHODS_SHOULD_NOT_HAVE_IDENTICAL_IMPLEMENTATIONS})
    @Test
    public void oneAnnotation_currentDateDisabledRange_throwSkipException_isNotRun() {
        testsRun.add(getCurrentMethodNameWithParams());
        TestUtils.failTestThatShouldNotRun();
    }

    @DisableBetweenDates(start = DATE_IN_PAST_START, end = DATE_IN_FUTURE_END, throwSkipException = true)
    @DisableBetweenDates(start = DATE_IN_PAST2_START, end = DATE_IN_PAST2_END, throwSkipException = true)
    @SuppressWarnings({SIMILAR_TESTS_SHOULD_BE_PARAMETRIZED, METHODS_SHOULD_NOT_HAVE_IDENTICAL_IMPLEMENTATIONS})
    @Test
    public void twoAnnotations_firstAnnotationShouldExclude_throwSkipException_isNotRun() {
        testsRun.add(getCurrentMethodNameWithParams());
        TestUtils.failTestThatShouldNotRun();
    }

    @DisableBetweenDates(start = DATE_IN_PAST2_START, end = DATE_IN_PAST2_END, throwSkipException = true)
    @DisableBetweenDates(start = DATE_IN_PAST_START, end = DATE_IN_FUTURE_END, throwSkipException = true)
    @SuppressWarnings({SIMILAR_TESTS_SHOULD_BE_PARAMETRIZED, METHODS_SHOULD_NOT_HAVE_IDENTICAL_IMPLEMENTATIONS})
    @Test
    public void twoAnnotations_secondAnnotationShouldExclude_throwSkipException_isNotRun() {
        testsRun.add(getCurrentMethodNameWithParams());
        TestUtils.failTestThatShouldNotRun();
    }
    //endregion Tests with throwSkipException = true

    @Test(priority = 2)
    public void verifyIncludedTests() {
        List<String> expected = List.of(
                "noAnnotation_isRun()",
                "oneAnnotation_futureDisabledRange_isRun()",
                "oneAnnotation_pastDisabledRange_throwSkipException_isRun()"
        );

        assertThat("Wrong number of tests run!", testsRun, hasSize(expected.size()));
        expected.forEach(e -> assertThat("Expected test to be run", testsRun, hasItem(e)));
    }

    @Test(priority = 2)
    public void verifyDisabledTests() {
        List<String> expectedDisabled = List.of(
                "oneAnnotation_currentDateDisabledRange_isNotRun()",
                "twoAnnotations_firstAnnotationShouldExclude_isNotRun()",
                "twoAnnotations_secondAnnotationShouldExclude_isNotRun()",
                // throwSkipException-based disabled tests
                "oneAnnotation_currentDateDisabledRange_throwSkipException_isNotRun()",
                "twoAnnotations_firstAnnotationShouldExclude_throwSkipException_isNotRun()",
                "twoAnnotations_secondAnnotationShouldExclude_throwSkipException_isNotRun()"
        );

        expectedDisabled.forEach(excludedTest ->
                assertThat("Expected test to be disabled", testsRun, not(hasItem(excludedTest))));

        // Added extra checks in case test refactoring break the above asserts.
        testsRun.forEach(test ->
                assertThat("Disabled test was run!", test, not(containsString("_isNotRun("))));
    }
}
