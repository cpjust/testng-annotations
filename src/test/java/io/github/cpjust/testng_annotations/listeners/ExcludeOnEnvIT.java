package io.github.cpjust.testng_annotations.listeners;

import io.github.cpjust.testng_annotations.BaseITEnvListener;
import io.github.cpjust.testng_annotations.annotations.ExcludeOnEnv;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
public class ExcludeOnEnvIT extends BaseITEnvListener {
    private static final List<String> testsRun = new ArrayList<>();

    @Test
    public void testWithNoExcludeOnEnv_isRun() {
        testsRun.add(getCurrentMethodName());
    }

    @ExcludeOnEnv(value = "matchEnv")
    @Test
    public void testWithExcludedEnv_isNotRun() {
        testsRun.add(getCurrentMethodName());
        failTestThatShouldNotRun();
    }

    @ExcludeOnEnv(value = "unmatchEnv")
    @Test
    public void testWithNonExcludedEnv_isRun() {
        testsRun.add(getCurrentMethodName());
    }

    @ExcludeOnEnv(value = "MATCHeNV")
    @Test
    public void testWithExcludedEnv_differentCase_isNotRun() {
        testsRun.add(getCurrentMethodName());
        failTestThatShouldNotRun();
    }

    @ExcludeOnEnv(value = {"matchEnvironment"}, propertyName = "environment")
    @Test
    public void testWithExcludedEnv_customPropertyName_isNotRun() {
        testsRun.add(getCurrentMethodName());
        failTestThatShouldNotRun();
    }

    @ExcludeOnEnv(value = {"unmatchEnvironment"}, propertyName = "environment")
    @Test
    public void testWithNonExcludedEnv_customPropertyName_isRun() {
        testsRun.add(getCurrentMethodName());
    }

    @ExcludeOnEnv(value = {"matchEnv", "unmatchEnv"})
    @Test
    public void testWithExcludedAndNonExcludedEnvs_isNotRun() {
        testsRun.add(getCurrentMethodName());
        failTestThatShouldNotRun();
    }

    @ExcludeOnEnv(value = {"unmatchEnv", "betterEnv"})
    @Test
    public void testWithMultipleNonExcludedEnvs_isRun() {
        testsRun.add(getCurrentMethodName());
    }

    @ExcludeOnEnv(value = {"matchEnv"})
    @Test(dataProvider = "testData")
    public void testWithExcludedEnv_isNotRun(String foo) {
        testsRun.add(getCurrentMethodName(foo));
        failTestThatShouldNotRun();
    }

    @ExcludeOnEnv(value = {"unmatchEnv"})
    @Test(dataProvider = "testData")
    public void testWithNonExcludedEnv_isRun(String foo) {
        testsRun.add(getCurrentMethodName(foo));
    }

    @ExcludeOnEnv(value = {"dev|matchEnv|stage"}, delimiter = "|")
    @Test
    public void testWithExcludedEnv_csvWithDelimiter_isNotRun() {
        testsRun.add(getCurrentMethodName());
        failTestThatShouldNotRun();
    }

    @ExcludeOnEnv(value = {"dev matchEnv stage"}, delimiter = " ")
    @Test
    public void testWithExcludedEnv_csvWithSpaceDelimiter_isNotRun() {
        testsRun.add(getCurrentMethodName());
        failTestThatShouldNotRun();
    }

    @ExcludeOnEnv(value = {"dev,stage,unmatchEnv"}, delimiter = ",")
    @Test
    public void testWithNonExcludedEnv_csvWithDelimiter_isRun() {
        testsRun.add(getCurrentMethodName());
    }

    @ExcludeOnEnv(value = {"dev|prod", "stage|unmatchEnv"}, delimiter = "|")
    @Test
    public void testWithNonExcludedEnv_arrayOfCsvWithDelimiter_isRun() {
        testsRun.add(getCurrentMethodName());
    }

    @ExcludeOnEnv(value = {"dev|prod", "stage|matchEnv"}, delimiter = "|")
    @Test
    public void testWithCsvExcludedEnv_arrayOfCsvWithDelimiter_isNotRun() {
        testsRun.add(getCurrentMethodName());
        failTestThatShouldNotRun();
    }

    @ExcludeOnEnv(value = {"stage|matchEnv"}, delimiter = ",")
    @Test
    public void testWithCsvExcludedEnv_csvWithWrongDelimiter_isRun() {
        testsRun.add(getCurrentMethodName());
    }

    @Test(priority = 2)
    public void verifyIncludedTests() {
        log.debug("Verifying included tests: {}", testsRun);
        List<String> expectedIncludedTests = List.of(
                "testWithNonExcludedEnv_isRun()",
                "testWithNonExcludedEnv_isRun(one)",
                "testWithNonExcludedEnv_isRun(two)",
                "testWithNonExcludedEnv_customPropertyName_isRun()",
                "testWithMultipleNonExcludedEnvs_isRun()",
                "testWithNoExcludeOnEnv_isRun()",
                "testWithNonExcludedEnv_csvWithDelimiter_isRun()",
                "testWithNonExcludedEnv_arrayOfCsvWithDelimiter_isRun()",
                "testWithCsvExcludedEnv_csvWithWrongDelimiter_isRun()"
        );

        assertThat("Wrong number of tests run!", testsRun, hasSize(expectedIncludedTests.size()));
        expectedIncludedTests.forEach(includedTest ->
                assertThat("Included test was not run!", testsRun, hasItem(includedTest)));

        // Added extra checks in case test refactoring break the above asserts.
        testsRun.forEach(test ->
                assertThat("Included test was not run!", test, containsString("_isRun(")));
    }

    @Test(priority = 2)
    public void verifyExcludedTests() {
        log.debug("Verifying excluded tests were not run");
        List<String> expectedExcludedTests = List.of(
                "testWithExcludedEnv_isNotRun()",
                "testWithExcludedEnv_differentCase_isNotRun()",
                "testWithExcludedEnv_isNotRun(one)",
                "testWithExcludedEnv_isNotRun(two)",
                "testWithExcludedEnv_customPropertyName_isNotRun()",
                "testWithExcludedAndNonExcludedEnvs_isNotRun()",
                "testWithExcludedEnv_csvWithDelimiter_isNotRun()",
                "testWithExcludedEnv_csvWithSpaceDelimiter_isNotRun()",
                "testWithCsvExcludedEnv_arrayOfCsvWithDelimiter_isNotRun()"
        );

        expectedExcludedTests.forEach(excludedTest ->
                assertThat("Excluded test was run!", testsRun, not(hasItem(excludedTest))));

        // Added extra checks in case test refactoring break the above asserts.
        testsRun.forEach(test ->
                assertThat("Excluded test was run!", test, not(containsString("_isNotRun("))));
    }

    @DataProvider
    private String[] testData() {
        return new String[] { "one", "two" };
    }
}
