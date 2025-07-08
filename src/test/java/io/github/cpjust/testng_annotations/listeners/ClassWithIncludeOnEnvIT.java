package io.github.cpjust.testng_annotations.listeners;

import io.github.cpjust.testng_annotations.TestBase;
import io.github.cpjust.testng_annotations.annotations.IncludeOnEnv;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.containsString;

@Slf4j
@IncludeOnEnv(value = {"unmatchEnv"})
@Listeners(value = IncludeOnEnvListener.class)
public class ClassWithIncludeOnEnvIT extends TestBase {
    private static final List<String> classWithIncludeOnEnvTestsRun = new ArrayList<>();

    @AfterSuite
    public void afterSuiteForClassWithIncludeOnEnvTests() {
        // Verify class-level attributes.
        log.debug("In afterSuite() checking expected test classes against these tests that were run: {}", classWithIncludeOnEnvTestsRun);
        List<String> expectedIncludedTests = List.of("classWithIncludedEnv_isRun()", "classWithIncludedEnv_isRun(one)", "classWithIncludedEnv_isRun(two)");
        List<String> expectedExcludedTests = List.of("classWithNoIncludeOnEnv_isNotRun()", "classWithNonIncludedEnv_isNotRun()",
                "classWithNonIncludedEnv_isNotRun(one)", "classWithNonIncludedEnv_isNotRun(two)");

        assertThat("Wrong number of tests run!", classWithIncludeOnEnvTestsRun, hasSize(expectedIncludedTests.size()));

        expectedExcludedTests.forEach(excludedTest -> assertThat("Excluded test was run!", classWithIncludeOnEnvTestsRun, not(hasItem(excludedTest))));
        expectedIncludedTests.forEach(includedTest -> assertThat("Included test was not run!", classWithIncludeOnEnvTestsRun, hasItem(includedTest)));

        // Added extra checks in case test refactoring break the above asserts.
        classWithIncludeOnEnvTestsRun.forEach(test -> assertThat("Excluded test was run!", test, not(containsString("_isNotRun("))));
        classWithIncludeOnEnvTestsRun.forEach(test -> assertThat("Included test was not run!", test, containsString("_isRun(")));
    }

    @Test
    public void classWithNoIncludeOnEnv_isNotRun() {
        classWithIncludeOnEnvTestsRun.add(getCurrentMethodName());
    }

    @IncludeOnEnv(value = {"unmatchEnv"})
    @Test
    public void classWithNonIncludedEnv_isNotRun() {
        classWithIncludeOnEnvTestsRun.add(getCurrentMethodName());
    }

    @IncludeOnEnv(value = {"matchEnv"})
    @Test
    public void classWithIncludedEnv_isRun() {
        classWithIncludeOnEnvTestsRun.add(getCurrentMethodName());
    }

    @IncludeOnEnv(value = {"matchEnv"})
    @Test(dataProvider = "testData")
    public void classWithIncludedEnv_isRun(String foo) {
        classWithIncludeOnEnvTestsRun.add(getCurrentMethodName(foo));
    }

    @IncludeOnEnv(value = {"unmatchEnv"})
    @Test(dataProvider = "testData")
    public void classWithNonIncludedEnv_isNotRun(String foo) {
        classWithIncludeOnEnvTestsRun.add(getCurrentMethodName(foo));
    }

    @DataProvider
    private String[] testData() {
        return new String[] { "one", "two" };
    }
}
