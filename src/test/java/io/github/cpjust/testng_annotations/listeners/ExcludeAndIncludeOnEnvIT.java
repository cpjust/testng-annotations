package io.github.cpjust.testng_annotations.listeners;

import io.github.cpjust.testng_annotations.TestBase;
import io.github.cpjust.testng_annotations.annotations.ExcludeOnEnv;
import io.github.cpjust.testng_annotations.annotations.IncludeOnEnv;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@Listeners(value = {ExcludeOnEnvListener.class, IncludeOnEnvListener.class})
public class ExcludeAndIncludeOnEnvIT extends TestBase {
    private static final List<String> testsRun = new ArrayList<>();

    @ExcludeOnEnv(value = "matchEnv")
    @IncludeOnEnv(value = "matchEnv")
    @Test
    public void testBothWithMatchEnv_isNotRun() {
        testsRun.add(getCurrentMethodName());
        failTestThatShouldNotRun();
    }

    @ExcludeOnEnv(value = "unmatchEnv")
    @IncludeOnEnv(value = "unmatchEnv")
    @Test
    public void testBothWithUnmatchEnv_isNotRun() {
        testsRun.add(getCurrentMethodName());
        failTestThatShouldNotRun();
    }

    @ExcludeOnEnv(value = "matchEnv")
    @IncludeOnEnv(value = "unmatchEnv")
    @Test
    public void testExcludeWithMatchEnvAndIncludeWithUnmatchEnv_isNotRun() {
        testsRun.add(getCurrentMethodName());
        failTestThatShouldNotRun();
    }

    @ExcludeOnEnv(value = "unmatchEnv")
    @IncludeOnEnv(value = "matchEnv")
    @Test
    public void testExcludeWithUnmatchEnvAndIncludeWithMatchEnv_isRun() {
        testsRun.add(getCurrentMethodName());
    }

    @Test(priority = 2)
    public void verifyIncludedTests() {
        log.debug("Verifying included tests: {}", testsRun);
        List<String> expectedIncludedTests = List.of(
                "testExcludeWithUnmatchEnvAndIncludeWithMatchEnv_isRun()"
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
                "testBothWithMatchEnv_isNotRun()",
                "testBothWithUnmatchEnv_isNotRun()",
                "testExcludeWithMatchEnvAndIncludeWithUnmatchEnv_isNotRun()"
        );

        expectedExcludedTests.forEach(excludedTest ->
                assertThat("Excluded test was run!", testsRun, not(hasItem(excludedTest))));

        // Added extra checks in case test refactoring break the above asserts.
        testsRun.forEach(test ->
                assertThat("Excluded test was run!", test, not(containsString("_isNotRun("))));
    }
}
