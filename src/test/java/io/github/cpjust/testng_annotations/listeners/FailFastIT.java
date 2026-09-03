package io.github.cpjust.testng_annotations.listeners;

import io.github.cpjust.testng_annotations.annotations.FailFast;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/**
 * Integration test that verifies the annotation does not interfere with a normal suite startup.
 */
@Slf4j
public class FailFastIT {

    private static boolean beforeSuiteRan;
    private static boolean beforeClassRan;
    private static boolean testRan;

    @BeforeSuite
    @FailFast
    public void beforeSuite() {
        beforeSuiteRan = true;
        log.info("FailFastIT.beforeSuite completed without error to confirm the annotation is harmless when setup succeeds.");
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        beforeClassRan = true;
        log.info("FailFastIT.beforeClass ran as usual.");
    }

    @Test
    public void classCanRunNormallyWhenBeforeSuiteDoesNotFail() {
        testRan = true;
        Assert.assertTrue(beforeSuiteRan, "BeforeSuite should have run before the test");
        Assert.assertTrue(beforeClassRan, "BeforeClass(alwaysRun = true) should still run");
        Assert.assertTrue(testRan, "The test method should run normally when no fail-fast trigger occurs");
    }
}
