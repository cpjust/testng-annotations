package io.github.cpjust.testng_annotations.listeners;

import io.github.cpjust.testng_annotations.annotations.FailFast;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.testng.IInvokedMethod;
import org.testng.ITestContext;
import org.testng.ITestNGListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.internal.ConstructorOrMethod;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.lang.reflect.Method;

import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link FailFastListener}.
 */
class FailFastListenerTest {

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

    @Test
    void programmaticRun_whenBeforeSuiteAnnotatedWithFailFast_thenSkipAlwaysRunMethods() {
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
        testng.addListener((ITestNGListener) listenerAdapter);

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
    void unitListener_whenFailFastAnnotatedMethodFails_thenListenerSkipsOtherConfigAndInvocationMethods() throws Exception {
        FailFastListener listener = new FailFastListener();
        ITestContext context = Mockito.mock(ITestContext.class);

        // Arm the listener by invoking a real @FailFast method
        Method failMethod = SampleFailFastTest.class.getMethod("beforeSuite");
        IInvokedMethod invokedFail = Mockito.mock(IInvokedMethod.class);
        ITestNGMethod failTnm = Mockito.mock(ITestNGMethod.class);
        when(invokedFail.getTestMethod()).thenReturn(failTnm);
        when(failTnm.getConstructorOrMethod()).thenReturn(new ConstructorOrMethod(failMethod));

        ITestResult failResult = Mockito.mock(ITestResult.class);
        when(failResult.getTestContext()).thenReturn(context);
        when(failResult.getMethod()).thenReturn(failTnm);
        when(failResult.getThrowable()).thenReturn(new RuntimeException("boom"));
        when(failResult.getStatus()).thenReturn(ITestResult.FAILURE);

        listener.beforeInvocation(invokedFail, failResult);
        listener.afterInvocation(invokedFail, failResult);

        // Methods to verify are skipped
        String[] methodNames = new String[] {
                "beforeClass", "beforeGroups", "beforeMethod", "beforeTest",
                "actualTest", "afterMethod", "afterGroups", "afterClass", "afterTest", "afterSuite"
        };

        for (String name : methodNames) {
            Method m = SampleFailFastTest.class.getMethod(name);
            IInvokedMethod im = Mockito.mock(IInvokedMethod.class);
            ITestNGMethod tnm = Mockito.mock(ITestNGMethod.class);
            when(im.getTestMethod()).thenReturn(tnm);
            when(tnm.getConstructorOrMethod()).thenReturn(new ConstructorOrMethod(m));

            ITestResult res = Mockito.mock(ITestResult.class);
            when(res.getMethod()).thenReturn(tnm);
            when(res.getTestContext()).thenReturn(context);

            assertThrows(SkipException.class, () -> listener.beforeInvocation(im, res),
                    "Listener should skip invocation of " + name);
            assertThrows(SkipException.class, () -> listener.beforeConfiguration(res),
                    "Listener should skip configuration callback for " + name);
        }
    }
}
