package com.github.cpjust.testng_annotations.listeners;

import com.github.cpjust.testng_annotations.TestBase;
import com.github.cpjust.testng_annotations.annotations.ExcludeOnEnv;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
public class ExcludeOnEnvTests extends TestBase {
    private static final List<String> testsRun = new ArrayList<>();

    @Test
    public void testWithNoExcludeOnEnv_isRun() {
        testsRun.add(getCurrentMethodName());
    }

    @ExcludeOnEnv(value = {"badEnv"})
    @Test
    public void testWithExcludedEnv_isNotRun() {
        testsRun.add(getCurrentMethodName());
    }

    @ExcludeOnEnv(value = {"goodEnv"})
    @Test
    public void testWithNonExcludedEnv_isRun() {
        testsRun.add(getCurrentMethodName());
    }

    @ExcludeOnEnv(value = {"badEnvironment"}, propertyName = "environment")
    @Test
    public void testWithExcludedEnv_customPropertyName_isNotRun() {
        testsRun.add(getCurrentMethodName());
    }

    @ExcludeOnEnv(value = {"goodEnv"}, propertyName = "environment")
    @Test
    public void testWithNonExcludedEnv_customPropertyName_isRun() {
        testsRun.add(getCurrentMethodName());
    }

    @ExcludeOnEnv(value = {"badEnv", "goodEnv"})
    @Test
    public void testWithExcludedAndNonExcludedEnvs_isNotRun() {
        testsRun.add(getCurrentMethodName());
    }

    @ExcludeOnEnv(value = {"goodEnv", "betterEnv"})
    @Test
    public void testWithMultipleNonExcludedEnvs_isRun() {
        testsRun.add(getCurrentMethodName());
    }

    @ExcludeOnEnv(value = {"badEnv"})
    @Test(dataProvider = "testData")
    public void testWithExcludedEnv_isNotRun(String foo) {
        testsRun.add(getCurrentMethodName(foo));
    }

    @ExcludeOnEnv(value = {"goodEnv"})
    @Test(dataProvider = "testData")
    public void testWithNonExcludedEnv_isRun(String foo) {
        testsRun.add(getCurrentMethodName(foo));
    }

    @Test(priority = 2)
    public void verifyTests() {
        log.debug("In verifyTests() checking expected tests against these tests that were run: {}", testsRun);
        List<String> expectedIncludedTests = List.of("testWithNonExcludedEnv_isRun()", "testWithNonExcludedEnv_isRun(one)", "testWithNonExcludedEnv_isRun(two)",
                "testWithNonExcludedEnv_customPropertyName_isRun()", "testWithMultipleNonExcludedEnvs_isRun()", "testWithNoExcludeOnEnv_isRun()");
        List<String> expectedExcludedTests = List.of("testWithExcludedEnv_isNotRun()", "testWithExcludedEnv_isNotRun(one)", "testWithExcludedEnv_isNotRun(two)",
                "testWithExcludedEnv_customPropertyName_isNotRun()", "testWithExcludedAndNonExcludedEnvs_isNotRun()");

        assertThat("Wrong number of tests run!", testsRun, hasSize(expectedIncludedTests.size()));

        expectedExcludedTests.forEach(excludedTest -> assertThat("Excluded test was run!", testsRun, not(hasItem(excludedTest))));
        expectedIncludedTests.forEach(includedTest -> assertThat("Included test was not run!", testsRun, hasItem(includedTest)));

        // Added extra checks in case test refactoring break the above asserts.
        testsRun.forEach(test -> assertThat("Excluded test was run!", test, not(containsString("_isNotRun("))));
        testsRun.forEach(test -> assertThat("Included test was not run!", test, containsString("_isRun(")));
    }

    @DataProvider
    private String[] testData() {
        return new String[] { "one", "two" };
    }
}
