package io.github.cpjust.testng_annotations.listeners;

import io.github.cpjust.testng_annotations.TestBase;
import io.github.cpjust.testng_annotations.annotations.IncludeOnEnv;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.containsString;

@Slf4j
@Listeners(value = IncludeOnEnvListener.class)
public class IncludeOnEnvTests extends TestBase {
    private static final List<String> testsRun = new ArrayList<>();

    @Test
    public void testWithNoIncludeOnEnv_isRun() {
        testsRun.add(getCurrentMethodName());
    }

    @IncludeOnEnv(value = "unmatchEnv")
    @Test
    public void testWithNonIncludedEnv_isNotRun() {
        testsRun.add(getCurrentMethodName());
    }

    @IncludeOnEnv(value = "matchEnv")
    @Test
    public void testWithIncludedEnv_isRun() {
        testsRun.add(getCurrentMethodName());
    }

    @IncludeOnEnv(value = {"unmatchEnvironment"}, propertyName = "environment")
    @Test
    public void testWithNonIncludedEnv_customPropertyName_isNotRun() {
        testsRun.add(getCurrentMethodName());
    }

    @IncludeOnEnv(value = {"matchEnvironment"}, propertyName = "environment")
    @Test
    public void testWithIncludedEnv_customPropertyName_isRun() {
        testsRun.add(getCurrentMethodName());
    }

    @IncludeOnEnv(value = {"unmatchEnv", "matchEnv"})
    @Test
    public void testWithIncludedAndNonIncludedEnvs_isRun() {
        testsRun.add(getCurrentMethodName());
    }

    @IncludeOnEnv(value = {"unmatchEnv"})
    @Test(dataProvider = "testData")
    public void testWithNonIncludedEnv_isNotRun(String foo) {
        testsRun.add(getCurrentMethodName(foo));
    }

    @IncludeOnEnv(value = {"matchEnv"})
    @Test(dataProvider = "testData")
    public void testWithIncludedEnv_isRun(String foo) {
        testsRun.add(getCurrentMethodName(foo));
    }

    @Test(priority = 2)
    public void verifyTests() {
        log.debug("In verifyTests() checking expected tests against these tests that were run: {}", testsRun);
        List<String> expectedIncludedTests = List.of("testWithIncludedEnv_isRun()", "testWithIncludedEnv_isRun(one)", "testWithIncludedEnv_isRun(two)",
                "testWithIncludedEnv_customPropertyName_isRun()", "testWithNoIncludeOnEnv_isRun()",
                "testWithIncludedAndNonIncludedEnvs_isRun()");
        List<String> expectedExcludedTests = List.of("testWithNonIncludedEnv_isNotRun()", "testWithNonIncludedEnv_isNotRun(one)", "testWithNonIncludedEnv_isNotRun(two)",
                "testWithNonIncludedEnv_customPropertyName_isNotRun()");

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
