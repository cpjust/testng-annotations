package io.github.cpjust.testng_annotations.listeners;

import io.github.cpjust.testng_annotations.TestBase;
import io.github.cpjust.testng_annotations.annotations.ExcludeOnEnv;
import io.github.cpjust.testng_annotations.annotations.IncludeOnEnv;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * This class demonstrates the use of the ExcludeOnEnv and IncludeOnEnv annotations with test priorities.
 * Tests are expected to run in the order of their priority values.  This test will fail on TestNG versions below 7.5
 * due to a TestNG bug in those versions.
 */
@Slf4j
@ExcludeOnEnv(value = {"unmatchEnv"})
@IncludeOnEnv(value = {"matchEnv"})
@Listeners(value = {ExcludeOnEnvListener.class, IncludeOnEnvListener.class})
public class ClassWithExcludeOnEnvAndIncludeOnEnvWithTestPriorityIT extends TestBase {
    private static final List<String> testExecutionOrder = new ArrayList<>();

    @Test(priority = 3, dataProvider = "testData")
    public void testPriorityThree(String foo) {
        testExecutionOrder.add("testPriorityThree");
    }

    @Test(priority = 2, dataProvider = "testData")
    public void testPriorityTwo(String foo) {
        testExecutionOrder.add("testPriorityTwo");
    }

    @Test(priority = -1)
    public void testPriorityOne() {
        testExecutionOrder.add("testPriorityOne");
    }

    @Test(priority = 4)
    public void testPriorityFour() {
        testExecutionOrder.add("testPriorityFour");
    }

    @Test(priority = 100)
    public void verifyTestOrder() {
        // This test should run last due to highest priority value
        List<String> expectedOrder = List.of(
                "testPriorityOne",
                "testPriorityTwo",
                "testPriorityTwo",
                "testPriorityThree",
                "testPriorityThree",
                "testPriorityFour"
        );
        log.debug("Verifying test execution order: {}", testExecutionOrder);
        assertThat("Test execution order does not match priority order!", testExecutionOrder, contains(expectedOrder.toArray()));
    }

    @DataProvider
    private String[] testData() {
        return new String[] { "one", "two" };
    }
}

