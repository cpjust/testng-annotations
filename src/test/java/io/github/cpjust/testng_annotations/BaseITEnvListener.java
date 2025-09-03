package io.github.cpjust.testng_annotations;

import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
public class BaseITEnvListener {
    @BeforeSuite
    public void beforeSuite() {
        // Allow test environments to be overridden via system properties
        String env = System.getProperty("test.env", "matchEnv");
        String environment = System.getProperty("test.environment", "matchEnvironment");

        System.setProperty("env", env);
        System.setProperty("environment", environment);

        log.info("Test environments configured - env: {}, environment: {}", env, environment);
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

    /**
     * Fails a test that shouldn't run.
     */
    protected static void failTestThatShouldNotRun() {
        Assert.fail("This test shouldn't be run!");
    }
}
