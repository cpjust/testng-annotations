package io.github.cpjust.testng_annotations.listeners;

import io.github.cpjust.testng_annotations.annotations.FailFast;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.xml.XmlSuite;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies FailFast behavior with a class hierarchy: Base -> TestA, TestB
 * A failing @FailFast method in TestA should only skip methods in TestA (and subclasses), not TestB.
 */
@Slf4j
class FailFastClassHierarchyTest {
    private static final long LATCH_TIMEOUT_SECONDS = 10L;

    @Slf4j
    public static class TestBase {
        static final AtomicBoolean testABaseAfterClassRan = new AtomicBoolean(false);
        static final AtomicBoolean testBBaseAfterClassRan = new AtomicBoolean(false);

        @org.testng.annotations.BeforeSuite(alwaysRun = true)
        @FailFast
        public void baseBeforeSuite() {
            log.info("Base beforeSuite setup completed successfully.");
        }

        @org.testng.annotations.BeforeClass(alwaysRun = true)
        @FailFast
        public void baseBeforeClass() {
            log.info("Base beforeClass setup completed successfully.");
        }

        /**
         * Records which subclass receives the inherited always-run cleanup hook.
         */
        @org.testng.annotations.AfterClass(alwaysRun = true)
        public void baseAfterClass() {
            if (getClass() == TestA.class) {
                testABaseAfterClassRan.set(true);
            } else if (getClass() == TestB.class) {
                testBBaseAfterClassRan.set(true);
            }
        }
    }

    @Slf4j
    public static class TestA extends TestBase {
        static final AtomicBoolean beforeClassRan = new AtomicBoolean(false);
        static final AtomicBoolean testRan = new AtomicBoolean(false);

        @org.testng.annotations.BeforeClass(alwaysRun = true)
        @FailFast
        public void beforeClass() {
            log.info("Running TestA beforeClass setup.");
            beforeClassRan.set(true);
            throw new RuntimeException("TestA fails in beforeClass");
        }

        @org.testng.annotations.Test
        public void testA() {
            testRan.set(true);
        }
    }

    @Slf4j
    public static class TestB extends TestBase {
        static final AtomicBoolean beforeClassRan = new AtomicBoolean(false);
        static final AtomicBoolean testRan = new AtomicBoolean(false);

        @org.testng.annotations.BeforeClass(alwaysRun = true)
        @FailFast
        public void beforeClass() {
            log.info("Running TestB beforeClass setup.");
            beforeClassRan.set(true);
        }

        @org.testng.annotations.Test
        public void testB() {
            testRan.set(true);
            log.info("TestB test completed successfully.");
        }
    }

    public static class ParallelFailFastBase {
        @org.testng.annotations.BeforeSuite(alwaysRun = true)
        @FailFast
        public void failBeforeSuite() {
            throw new RuntimeException("Shared suite setup failed");
        }
    }

    public static class ParallelTestA extends ParallelFailFastBase {
        static final AtomicBoolean beforeClassRan = new AtomicBoolean(false);
        static final AtomicBoolean testRan = new AtomicBoolean(false);

        @org.testng.annotations.BeforeClass(alwaysRun = true)
        public void beforeClass() {
            beforeClassRan.set(true);
        }

        @org.testng.annotations.Test
        public void testA() {
            testRan.set(true);
        }
    }

    public static class ParallelTestB extends ParallelFailFastBase {
        static final AtomicBoolean beforeClassRan = new AtomicBoolean(false);
        static final AtomicBoolean testRan = new AtomicBoolean(false);

        @org.testng.annotations.BeforeClass(alwaysRun = true)
        public void beforeClass() {
            beforeClassRan.set(true);
        }

        @org.testng.annotations.Test
        public void testB() {
            testRan.set(true);
        }
    }

    public static class ParallelRaceBoundaryTest {
        static volatile CountDownLatch alreadyRunningMethodStarted = new CountDownLatch(1);
        static volatile CountDownLatch failFastFailureRecorded = new CountDownLatch(1);
        static final AtomicBoolean alreadyRunningMethodCompleted = new AtomicBoolean(false);
        static final AtomicBoolean queuedMethodRan = new AtomicBoolean(false);

        @org.testng.annotations.Test(priority = 0)
        @FailFast
        public void failFastAfterAnotherMethodStarts() throws InterruptedException {
            assertTrue(alreadyRunningMethodStarted.await(LATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS),
                    "The second test method should start before the fail-fast method completes");
            throw new RuntimeException("Parallel fail-fast trigger");
        }

        @org.testng.annotations.Test(priority = 1)
        public void alreadyRunningMethod() throws InterruptedException {
            alreadyRunningMethodStarted.countDown();
            assertTrue(failFastFailureRecorded.await(LATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS),
                    "The fail-fast failure should be recorded while this method is running");
            alreadyRunningMethodCompleted.set(true);
        }

        @org.testng.annotations.Test(priority = 2)
        public void queuedMethod() {
            queuedMethodRan.set(true);
        }
    }

    /**
     * Verifies that a failing @FailFast method in one subclass does not skip methods in another subclass.
     * Ex. If TestA fails in its BeforeClass, TestB should still run its BeforeClass and test method.
     * The inherited AfterClass(alwaysRun = true) should still run for TestB, but not for TestA since it failed in its BeforeClass.
     */
    @Test
    void whenFailInSubclassBeforeClass_thenOnlyThatSubclassIsSkipped_and_otherSubclassRuns() {
        TestA.beforeClassRan.set(false);
        TestA.testRan.set(false);
        TestB.beforeClassRan.set(false);
        TestB.testRan.set(false);
        TestBase.testABaseAfterClassRan.set(false);
        TestBase.testBBaseAfterClassRan.set(false);

        TestNG testng = new TestNG();
        testng.setTestClasses(new Class[]{TestA.class, TestB.class});

        TestListenerAdapter listenerAdapter = new TestListenerAdapter();
        testng.addListener(listenerAdapter);

        testng.run();

        // TestA should not run its test due to its failing beforeClass.
        assertFalse(TestA.testRan.get(), "TestA should not run when its beforeClass fails");
        assertFalse(TestBase.testABaseAfterClassRan.get(),
                "The inherited AfterClass(alwaysRun = true) should be skipped for TestA after its fail-fast failure");
        // TestB methods should still run despite TestA failure.
        assertTrue(TestB.beforeClassRan.get(), "TestB should run its beforeClass");
        assertTrue(TestB.testRan.get(), "TestB should run when TestA's beforeClass fails because the trigger is scoped to TestA");
        assertTrue(TestBase.testBBaseAfterClassRan.get(),
                "The inherited AfterClass(alwaysRun = true) should still run for unaffected sibling TestB");

        assertThat("There should be configuration failures recorded for the failing beforeClass",
                listenerAdapter.getConfigurationFailures(), not(empty()));
    }

    /**
     * Verifies that a failing @FailFast method in a shared BeforeSuite hook skips all subsequent tests in parallel classes.
     * Ex. If TestA and TestB are running in parallel, and a shared BeforeSuite fails, both classes should skip their tests.
     */
    @Test
    void whenSharedBeforeSuiteFails_inParallelClasses_thenAlwaysRunHooksAreSkippedInBothClasses() {
        ParallelTestA.beforeClassRan.set(false);
        ParallelTestA.testRan.set(false);
        ParallelTestB.beforeClassRan.set(false);
        ParallelTestB.testRan.set(false);

        TestNG testng = new TestNG();
        testng.setTestClasses(new Class[]{ParallelTestA.class, ParallelTestB.class});
        testng.setParallel(XmlSuite.ParallelMode.CLASSES);
        testng.setThreadCount(2);

        TestListenerAdapter listenerAdapter = new TestListenerAdapter();
        testng.addListener(listenerAdapter);
        testng.run();

        assertFalse(ParallelTestA.beforeClassRan.get(),
                "ParallelTestA BeforeClass(alwaysRun = true) should be skipped after the shared suite setup failure");
        assertFalse(ParallelTestA.testRan.get(),
                "ParallelTestA tests should be skipped after the shared suite setup failure");
        assertFalse(ParallelTestB.beforeClassRan.get(),
                "ParallelTestB BeforeClass(alwaysRun = true) should be skipped after the shared suite setup failure");
        assertFalse(ParallelTestB.testRan.get(),
                "ParallelTestB tests should be skipped after the shared suite setup failure");

        assertThat("The shared @BeforeSuite failure should be reported as a configuration failure.",
                listenerAdapter.getConfigurationFailures(), not(empty()));
    }

    /**
     * Verifies that a failing @FailFast method in one thread does not prevent a method already running in another thread from completing.
     * However, any queued methods checked after the fail-fast trigger should be skipped.
     * Ex. If TestClass starts running test methods test1 and test2 in parallel, and test3 is queued, then test1 fails with @FailFast, test2 should be allowed to complete, but test3 should be skipped.
     */
    @Test
    void whenFailFastTriggersDuringParallelExecution_thenRunningMethodCompletesAndQueuedMethodIsSkipped() {
        ParallelRaceBoundaryTest.alreadyRunningMethodStarted = new CountDownLatch(1);
        ParallelRaceBoundaryTest.failFastFailureRecorded = new CountDownLatch(1);
        ParallelRaceBoundaryTest.alreadyRunningMethodCompleted.set(false);
        ParallelRaceBoundaryTest.queuedMethodRan.set(false);

        TestNG testng = new TestNG();
        testng.setTestClasses(new Class[] {ParallelRaceBoundaryTest.class});
        testng.setParallel(XmlSuite.ParallelMode.METHODS);
        testng.setThreadCount(2);

        TestListenerAdapter listenerAdapter = new TestListenerAdapter();
        testng.addListener(listenerAdapter);
        testng.addListener(new ITestListener() {
            @Override
            public void onTestFailure(ITestResult result) {
                if ("failFastAfterAnotherMethodStarts".equals(result.getMethod().getMethodName())) {
                    ParallelRaceBoundaryTest.failFastFailureRecorded.countDown();
                }
            }
        });

        testng.run();

        assertTrue(ParallelRaceBoundaryTest.alreadyRunningMethodCompleted.get(),
                "A method already running when fail-fast triggers should be allowed to complete");
        assertFalse(ParallelRaceBoundaryTest.queuedMethodRan.get(),
                "A method checked after the fail-fast failure should be skipped");
        assertEquals(1, listenerAdapter.getFailedTests().size(),
                "Only the fail-fast trigger should be reported as a failed test");
        assertEquals(
                "failFastAfterAnotherMethodStarts",
                listenerAdapter.getFailedTests().get(0).getMethod().getMethodName(),
                "The fail-fast trigger should be the failed test");
        assertEquals(
                "Parallel fail-fast trigger",
                listenerAdapter.getFailedTests().get(0).getThrowable().getMessage(),
                "The trigger should fail only after the other method has started");
        assertEquals(1, listenerAdapter.getPassedTests().size(),
                "The already-running method should complete successfully");
        assertEquals(
                "alreadyRunningMethod",
                listenerAdapter.getPassedTests().get(0).getMethod().getMethodName(),
                "The already-running method should be the successful test");
        assertEquals(1, listenerAdapter.getSkippedTests().size(),
                "Only the method checked after fail-fast should be skipped");
        assertEquals(
                "queuedMethod",
                listenerAdapter.getSkippedTests().get(0).getMethod().getMethodName(),
                "The queued method should be the skipped test");
        assertThat("The queued test should be reported as skipped after fail-fast triggers",
                listenerAdapter.getSkippedTests(), not(empty()));
    }
}
