package io.github.cpjust.testng_annotations;

import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;

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
     * Fails a test that shouldn't run.
     */
    protected static void failTestThatShouldNotRun() {
        Assert.fail("This test shouldn't be run!");
    }
}
