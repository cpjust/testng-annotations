package io.github.cpjust.testng_annotations.listeners;

import io.github.cpjust.testng_annotations.BaseITEnvListener;
import io.github.cpjust.testng_annotations.annotations.IncludeOnEnv;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

@Slf4j
@Listeners(value = IncludeOnEnvListener.class)
public class IncludeOnEnvIT extends BaseITEnvListener {
    private static final List<String> testsRun = new ArrayList<>();

    @Test
    public void testWithNoIncludeOnEnv_isRun() {
        testsRun.add(getCurrentMethodName());
    }

    @IncludeOnEnv(value = "unmatchEnv")
    @Test
    public void testWithNonIncludedEnv_isNotRun() {
        testsRun.add(getCurrentMethodName());
        failTestThatShouldNotRun();
    }

    @IncludeOnEnv(value = "matchEnv")
    @Test
    public void testWithIncludedEnv_isRun() {
        testsRun.add(getCurrentMethodName());
    }

    @IncludeOnEnv(value = "MATCHeNV")
    @Test
    public void testWithIncludedEnv_differentCase_isRun() {
        testsRun.add(getCurrentMethodName());
    }

    @IncludeOnEnv(value = {"unmatchEnvironment"}, propertyName = "environment")
    @Test
    public void testWithNonIncludedEnv_customPropertyName_isNotRun() {
        testsRun.add(getCurrentMethodName());
        failTestThatShouldNotRun();
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
        failTestThatShouldNotRun();
    }

    @IncludeOnEnv(value = {"matchEnv"})
    @Test(dataProvider = "testData")
    public void testWithIncludedEnv_isRun(String foo) {
        testsRun.add(getCurrentMethodName(foo));
    }

    @IncludeOnEnv(value = {"dev,matchEnv,stage"}, delimiter = ",")
    @Test
    public void testWithIncludedEnv_csvWithDelimiter_isRun() {
        testsRun.add(getCurrentMethodName());
    }

    @IncludeOnEnv(value = {"dev matchEnv stage"}, delimiter = " ")
    @Test
    public void testWithIncludedEnv_csvWithSpaceDelimiter_isRun() {
        testsRun.add(getCurrentMethodName());
    }

    @IncludeOnEnv(value = {"dev|stage|unmatchEnv"}, delimiter = "|")
    @Test
    public void testWithNonIncludedEnv_csvWithDelimiter_isNotRun() {
        testsRun.add(getCurrentMethodName());
        failTestThatShouldNotRun();
    }

    @IncludeOnEnv(value = {"dev|prod", "stage|unmatchEnv"}, delimiter = "|")
    @Test
    public void testWithNonIncludedEnv_arrayOfCsvWithDelimiter_isNotRun() {
        testsRun.add(getCurrentMethodName());
        failTestThatShouldNotRun();
    }

    @IncludeOnEnv(value = {"dev|prod", "stage|matchEnv|"}, delimiter = "|")
    @Test
    public void testWithCsvIncludedEnv_arrayOfCsvWithDelimiter_isRun() {
        testsRun.add(getCurrentMethodName());
    }

    @Test(priority = 2)
    public void verifyIncludedTests() {
        log.debug("In verifyIncludedTests() checking expected tests against these tests that were run: {}", testsRun);
        List<String> expectedIncludedTests = List.of(
                "testWithIncludedEnv_isRun()",
                "testWithIncludedEnv_differentCase_isRun()",
                "testWithIncludedEnv_isRun(one)",
                "testWithIncludedEnv_isRun(two)",
                "testWithIncludedEnv_customPropertyName_isRun()",
                "testWithNoIncludeOnEnv_isRun()",
                "testWithIncludedAndNonIncludedEnvs_isRun()",
                "testWithIncludedEnv_csvWithDelimiter_isRun()",
                "testWithIncludedEnv_csvWithSpaceDelimiter_isRun()",
                "testWithCsvIncludedEnv_arrayOfCsvWithDelimiter_isRun()"
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
        log.debug("In verifyExcludedTests() checking expected tests against these tests that were run: {}", testsRun);
        List<String> expectedExcludedTests = List.of(
                "testWithNonIncludedEnv_isNotRun()",
                "testWithNonIncludedEnv_isNotRun(one)",
                "testWithNonIncludedEnv_isNotRun(two)",
                "testWithNonIncludedEnv_customPropertyName_isNotRun()",
                "testWithNonIncludedEnv_csvWithDelimiter_isNotRun()",
                "testWithNonIncludedEnv_arrayOfCsvWithDelimiter_isNotRun()"
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
