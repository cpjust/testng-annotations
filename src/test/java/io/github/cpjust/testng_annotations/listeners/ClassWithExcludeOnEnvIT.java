package io.github.cpjust.testng_annotations.listeners;

import io.github.cpjust.testng_annotations.BaseITEnvListener;
import io.github.cpjust.testng_annotations.annotations.ExcludeOnEnv;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

@Slf4j
@ExcludeOnEnv(value = {"matchEnv"})
public class ClassWithExcludeOnEnvIT extends BaseITEnvListener {
    private static final List<String> classWithExcludeOnEnvTestsRun = new ArrayList<>();

    @AfterSuite
    public void afterSuiteForClassWithExcludeOnEnvTests() {
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

    @Test
    public void classWithNoExcludeOnEnv_isNotRun() {
        classWithExcludeOnEnvTestsRun.add(getCurrentMethodName());
    }

    @ExcludeOnEnv(value = {"matchEnv"})
    @Test
    public void classWithExcludedEnv_isNotRun() {
        classWithExcludeOnEnvTestsRun.add(getCurrentMethodName());
    }

    @ExcludeOnEnv(value = {"matchEnv"})
    @Test(dataProvider = "testData")
    public void classWithExcludedEnv_isNotRun(String foo) {
        classWithExcludeOnEnvTestsRun.add(getCurrentMethodName(foo));
    }

    @ExcludeOnEnv(value = {"unmatchEnv"})
    @Test(dataProvider = "testData")
    public void classWithNonExcludedEnv_isNotRun(String foo) {
        classWithExcludeOnEnvTestsRun.add(getCurrentMethodName(foo));
    }

    @DataProvider
    private String[] testData() {
        return new String[] { "one", "two" };
    }
}
