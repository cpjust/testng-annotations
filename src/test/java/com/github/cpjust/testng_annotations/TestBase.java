package com.github.cpjust.testng_annotations;

import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.containsString;

@Slf4j
public class TestBase {
    protected static final List<String> classWithExcludeOnEnvTestsRun = new ArrayList<>();

    @BeforeSuite
    public void beforeSuite() {
        System.setProperty("env", "badEnv");
        System.setProperty("environment", "badEnvironment");
    }

    @AfterSuite
    public void afterSuite() {
        // Verify class-level attributes.
        log.debug("In afterSuite() checking expected test classes against these tests that were run: {}", classWithExcludeOnEnvTestsRun);
        List<String> expectedIncludedTests = Collections.emptyList();
        List<String> expectedExcludedTests = List.of("classWithNoExcludeOnEnv_isNotRun()", "classWithExcludedEnv_isNotRun()",
                "classWithExcludedEnv_isNotRun(one)", "classWithExcludedEnv_isNotRun(two)",
                "classWithNonExcludedEnv_isNotRun(one)", "classWithNonExcludedEnv_isNotRun(two)");

        assertThat("Wrong number of tests run!", classWithExcludeOnEnvTestsRun, hasSize(expectedIncludedTests.size()));

        expectedExcludedTests.forEach(excludedTest -> assertThat("Excluded test was run!", classWithExcludeOnEnvTestsRun, not(hasItem(excludedTest))));
        expectedIncludedTests.forEach(includedTest -> assertThat("Included test was not run!", classWithExcludeOnEnvTestsRun, hasItem(includedTest)));

        // Added extra checks in case test refactoring break the above asserts.
        classWithExcludeOnEnvTestsRun.forEach(test -> assertThat("Excluded test was run!", test, not(containsString("_isNotRun("))));
        classWithExcludeOnEnvTestsRun.forEach(test -> assertThat("Included test was not run!", test, containsString("_isRun(")));
    }

    /**
     * Gets the name of the method that called this function.
     *
     * @return The name of the method that called this function.
     */
    protected static String getCurrentMethodName(Object... params) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        assertThat("Not enough elements in stack trace!", stackTraceElements.length, greaterThanOrEqualTo(2));
        String paramString = Arrays.stream(params).map(Object::toString).collect(Collectors.joining(", "));
        return stackTraceElements[2].getMethodName() + String.format("(%s)", paramString);
    }
}
