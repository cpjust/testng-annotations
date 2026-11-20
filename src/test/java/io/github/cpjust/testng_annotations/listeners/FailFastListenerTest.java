package io.github.cpjust.testng_annotations.listeners;

import io.github.cpjust.testng_annotations.annotations.FailFast;
import org.junit.jupiter.api.Test;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link FailFastListener}.
 */
class FailFastListenerTest {
    public static class FailureMessageBase {
        @FailFast
        public void failInBase() {
            // The test reports this failure directly to the listener.
        }
    }

    public static class FailureMessageSubclass extends FailureMessageBase {
        @FailFast
        public void failInSubclass() {
            // The test reports this failure directly to the listener.
        }

        public void continueSetup() {
            // The listener skips this method after either fail-fast failure is recorded.
        }
    }

    public static class SampleFailFastTest {
        static final AtomicBoolean beforeClassRan = new AtomicBoolean(false);
        static final AtomicBoolean beforeGroupsRan = new AtomicBoolean(false);
        static final AtomicBoolean beforeMethodRan = new AtomicBoolean(false);
        static final AtomicBoolean beforeTestRan = new AtomicBoolean(false);
        static final AtomicBoolean testRan = new AtomicBoolean(false);

        static final AtomicBoolean afterClassRan = new AtomicBoolean(false);
        static final AtomicBoolean afterGroupsRan = new AtomicBoolean(false);
        static final AtomicBoolean afterMethodRan = new AtomicBoolean(false);
        static final AtomicBoolean afterTestRan = new AtomicBoolean(false);
        static final AtomicBoolean afterSuiteRan = new AtomicBoolean(false);

        @org.testng.annotations.BeforeSuite
        @FailFast
        public void beforeSuite() {
            throw new RuntimeException("trigger fail-fast");
        }

        @org.testng.annotations.BeforeClass(alwaysRun = true)
        public void beforeClass() {
            beforeClassRan.set(true);
        }

        @org.testng.annotations.BeforeGroups(value = {"g1"}, alwaysRun = true)
        public void beforeGroups() {
            beforeGroupsRan.set(true);
        }

        @org.testng.annotations.BeforeMethod(alwaysRun = true)
        public void beforeMethod() {
            beforeMethodRan.set(true);
        }

        @org.testng.annotations.BeforeTest(alwaysRun = true)
        public void beforeTest() {
            beforeTestRan.set(true);
        }

        @org.testng.annotations.Test(groups = {"g1"})
        public void actualTest() {
            testRan.set(true);
        }

        @org.testng.annotations.AfterClass(alwaysRun = true)
        public void afterClass() {
            afterClassRan.set(true);
        }

        @org.testng.annotations.AfterGroups(value = {"g1"}, alwaysRun = true)
        public void afterGroups() {
            afterGroupsRan.set(true);
        }

        @org.testng.annotations.AfterMethod(alwaysRun = true)
        public void afterMethod() {
            afterMethodRan.set(true);
        }

        @org.testng.annotations.AfterTest(alwaysRun = true)
        public void afterTest() {
            afterTestRan.set(true);
        }

        @org.testng.annotations.AfterSuite(alwaysRun = true)
        public void afterSuite() {
            afterSuiteRan.set(true);
        }
    }

    public static class FailingSuiteBase {
        @BeforeSuite(alwaysRun = true)
        @FailFast
        public void failBeforeSuite() {
            throw new RuntimeException("First suite run should fail");
        }
    }

    public static class FailingSuiteTest extends FailingSuiteBase {
        static final AtomicBoolean beforeClassRan = new AtomicBoolean(false);

        @BeforeClass(alwaysRun = true)
        public void beforeClass() {
            beforeClassRan.set(true);
        }

        @org.testng.annotations.Test
        public void testShouldBeSkipped() {
            throw new AssertionError("Test should be skipped after the @BeforeSuite failure");
        }
    }

    public static class SuccessfulSuiteTest {
        static final AtomicBoolean beforeClassRan = new AtomicBoolean(false);
        static final AtomicBoolean testRan = new AtomicBoolean(false);

        @BeforeSuite
        @FailFast
        public void beforeSuite() {
            // A successful @FailFast method must not create a failure trigger.
        }

        @BeforeClass(alwaysRun = true)
        public void beforeClass() {
            beforeClassRan.set(true);
        }

        @org.testng.annotations.Test
        public void testRuns() {
            testRan.set(true);
        }
    }

    @Test
    void givenFailingBeforeSuiteWithFailFast_whenSuiteRuns_thenSkipAllRemainingMethods() {
        SampleFailFastTest.beforeClassRan.set(false);
        SampleFailFastTest.beforeGroupsRan.set(false);
        SampleFailFastTest.beforeMethodRan.set(false);
        SampleFailFastTest.beforeTestRan.set(false);
        SampleFailFastTest.testRan.set(false);

        SampleFailFastTest.afterClassRan.set(false);
        SampleFailFastTest.afterGroupsRan.set(false);
        SampleFailFastTest.afterMethodRan.set(false);
        SampleFailFastTest.afterTestRan.set(false);
        SampleFailFastTest.afterSuiteRan.set(false);

        TestNG testng = new TestNG();
        testng.setTestClasses(new Class[] {SampleFailFastTest.class});

        TestListenerAdapter listenerAdapter = new TestListenerAdapter();
        testng.addListener(listenerAdapter);

        testng.run();

        assertFalse(SampleFailFastTest.beforeClassRan.get(),
                "BeforeClass should not run when fail-fast is triggered by a failing @BeforeSuite");
        assertFalse(SampleFailFastTest.beforeGroupsRan.get(),
                "BeforeGroups should not run when fail-fast is triggered by a failing @BeforeSuite");
        assertFalse(SampleFailFastTest.beforeMethodRan.get(),
                "BeforeMethod should not run when fail-fast is triggered by a failing @BeforeSuite");
        assertFalse(SampleFailFastTest.beforeTestRan.get(),
                "BeforeTest should not run when fail-fast is triggered by a failing @BeforeSuite");
        assertFalse(SampleFailFastTest.testRan.get(),
                "Test should not run when fail-fast is triggered by a failing @BeforeSuite");

        assertFalse(SampleFailFastTest.afterSuiteRan.get(),
                "AfterSuite should not run when fail-fast is triggered by a failing @BeforeSuite");
        assertFalse(SampleFailFastTest.afterClassRan.get(),
                "AfterClass should not run when fail-fast is triggered by a failing @BeforeSuite");
        assertFalse(SampleFailFastTest.afterGroupsRan.get(),
                "AfterGroups should not run when fail-fast is triggered by a failing @BeforeSuite");
        assertFalse(SampleFailFastTest.afterMethodRan.get(),
                "AfterMethod should not run when fail-fast is triggered by a failing @BeforeSuite");
        assertFalse(SampleFailFastTest.afterTestRan.get(),
                "AfterTest should not run when fail-fast is triggered by a failing @BeforeSuite");

        assertThat("Fail-fast should record the original configuration failure.",
                listenerAdapter.getConfigurationFailures(), not(empty()));
    }

    @Test
    void givenTwoSuitesWithSameName_whenFirstSuiteFails_thenSecondSuiteDoesNotShareFailFastState() {
        FailingSuiteTest.beforeClassRan.set(false);
        SuccessfulSuiteTest.beforeClassRan.set(false);
        SuccessfulSuiteTest.testRan.set(false);

        ISuite failedSuite = runSuite("same-suite-name", FailingSuiteTest.class);
        ISuite successfulSuite = runSuite("same-suite-name", SuccessfulSuiteTest.class);

        assertFalse(FailingSuiteTest.beforeClassRan.get(),
                "The failed suite should skip its always-run BeforeClass method");
        assertTrue(SuccessfulSuiteTest.beforeClassRan.get(),
                "The successful suite should not inherit the previous suite's fail-fast state");
        assertTrue(SuccessfulSuiteTest.testRan.get(),
                "The successful suite's test should run when its own setup succeeds");
        assertNull(failedSuite.getAttribute(
                        FailFastListener.class.getName() + ".state"),
                "Fail-fast state should be removed when its suite finishes");
        assertNull(successfulSuite.getAttribute(
                        FailFastListener.class.getName() + ".state"),
                "Successful suite completion should leave no fail-fast state behind");
    }

    @Test
    void givenBaseAndSubclassFailuresAreRecorded_whenCallMessageFor_thenSkipMessageUsesMostSpecificFailure() {
        FailFastListener.SuiteState suiteState = new FailFastListener.SuiteState();

        suiteState.recordFailure(FailureMessageBase.class, "base failure");
        suiteState.recordFailure(FailureMessageSubclass.class, "subclass failure");

        String message = suiteState.messageFor(FailureMessageSubclass.class);

        assertEquals("subclass failure", message,
                "The skip message should use the most specific applicable subclass failure");
    }

    /**
     * Runs one test class in a TestNG suite and captures its suite object for lifecycle assertions.
     *
     * @param suiteName the suite name.
     * @param testClass the test class to execute.
     * @return the completed suite instance.
     */
    private static ISuite runSuite(String suiteName, Class<?> testClass) {
        XmlSuite suite = new XmlSuite();
        suite.setName(suiteName);
        XmlTest xmlTest = new XmlTest(suite);
        xmlTest.setName("test");
        xmlTest.setXmlClasses(Collections.singletonList(new XmlClass(testClass)));

        AtomicReference<ISuite> suiteReference = new AtomicReference<>();
        TestNG testng = new TestNG();
        testng.setXmlSuites(Collections.singletonList(suite));
        testng.addListener(new ISuiteListener() {
            @Override
            public void onStart(ISuite startedSuite) {
                suiteReference.set(startedSuite);
            }

            @Override
            public void onFinish(ISuite finishedSuite) {
                suiteReference.set(finishedSuite);
            }
        });

        testng.run();
        return suiteReference.get();
    }
}
