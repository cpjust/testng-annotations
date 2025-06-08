package com.github.cpjust.testng_annotations;

import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeSuite;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
public class TestBase {
    @BeforeSuite
    public void beforeSuite() {
        System.setProperty("env", "matchEnv");
        System.setProperty("environment", "matchEnvironment");
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
