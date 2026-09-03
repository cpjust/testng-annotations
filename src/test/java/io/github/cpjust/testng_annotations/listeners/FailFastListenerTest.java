package io.github.cpjust.testng_annotations.listeners;

import io.github.cpjust.testng_annotations.annotations.FailFast;
import org.junit.jupiter.api.Test;
import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Unit tests for {@link FailFastListener}.
 */
class FailFastListenerTest {

    public static class SampleFailFastTest {
        static final AtomicBoolean beforeClassRan = new AtomicBoolean(false);
        static final AtomicBoolean testRan = new AtomicBoolean(false);

        @org.testng.annotations.BeforeSuite
        @FailFast
        public void beforeSuite() {
            throw new RuntimeException("trigger fail-fast");
        }

        @org.testng.annotations.BeforeClass(alwaysRun = true)
        public void beforeClass() {
            beforeClassRan.set(true);
        }

        @org.testng.annotations.Test
        public void actualTest() {
            testRan.set(true);
        }
    }

    @Test
    void programmaticRun_whenBeforeSuiteAnnotatedWithFailFast_thenSkipAlwaysRunMethods() {
        SampleFailFastTest.beforeClassRan.set(false);
        SampleFailFastTest.testRan.set(false);

        TestNG testng = new TestNG();
        testng.setTestClasses(new Class[] {SampleFailFastTest.class});

        TestListenerAdapter listenerAdapter = new TestListenerAdapter();
        testng.addListener((ITestNGListener) listenerAdapter);

        testng.run();

        assertFalse(SampleFailFastTest.beforeClassRan.get(),
                "BeforeClass should not run when fail-fast is triggered by a failing @BeforeSuite");
        assertFalse(SampleFailFastTest.testRan.get(),
                "Test should not run when fail-fast is triggered by a failing @BeforeSuite");
        assertThat("Fail-fast should record the original configuration failure.",
                listenerAdapter.getConfigurationFailures(), not(empty()));
    }
}
