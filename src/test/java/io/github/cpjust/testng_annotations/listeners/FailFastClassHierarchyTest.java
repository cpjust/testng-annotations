package io.github.cpjust.testng_annotations.listeners;

import io.github.cpjust.testng_annotations.annotations.FailFast;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.xml.XmlSuite;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies FailFast behavior with a class hierarchy: Base -> TestA, TestB
 * A failing @FailFast method in TestA should only skip methods in TestA (and subclasses), not TestB.
 */
@Slf4j
class FailFastClassHierarchyTest {

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
}
