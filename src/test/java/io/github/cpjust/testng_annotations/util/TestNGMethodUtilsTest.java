package io.github.cpjust.testng_annotations.util;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.testng.IMethodInstance;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.internal.ConstructorOrMethod;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TestNGMethodUtils}.
 */
class TestNGMethodUtilsTest {
    private static final String SAMPLE_TARGET_SAMPLE_METHOD = "SampleTarget.sampleMethod";

    public static class SampleTarget {
        public void sampleMethod() {
            // Intentionally empty for reflective inspection in tests.
        }
    }

    @Test
    void getMethod_methodInstance_whenNullOrMissingMethod_thenReturnNull() {
        assertNull(TestNGMethodUtils.getMethod((IMethodInstance) null),
                "Null method instances should return null");

        IMethodInstance methodInstance = Mockito.mock(IMethodInstance.class);
        assertNull(TestNGMethodUtils.getMethod(methodInstance),
                "Method instances without a TestNG method should return null");

        when(methodInstance.getMethod()).thenReturn(null);
        assertNull(TestNGMethodUtils.getMethod(methodInstance),
                "Method instances with a null TestNG method should return null");
    }

    @Test
    void getMethod_methodInstance_whenMethodPresent_thenReturnJavaMethod() throws Exception {
        Method expectedMethod = SampleTarget.class.getMethod("sampleMethod");
        IMethodInstance methodInstance = Mockito.mock(IMethodInstance.class);
        ITestNGMethod testNgMethod = Mockito.mock(ITestNGMethod.class);

        when(methodInstance.getMethod()).thenReturn(testNgMethod);
        when(testNgMethod.getConstructorOrMethod()).thenReturn(new ConstructorOrMethod(expectedMethod));

        assertEquals(expectedMethod, TestNGMethodUtils.getMethod(methodInstance),
                "MethodInstance extraction should return the reflected Java method");
    }

    @Test
    void getMethod_testNgMethod_whenMethodPresent_thenReturnJavaMethod() throws Exception {
        Method expectedMethod = SampleTarget.class.getMethod("sampleMethod");
        ITestNGMethod testNgMethod = Mockito.mock(ITestNGMethod.class);

        when(testNgMethod.getConstructorOrMethod()).thenReturn(new ConstructorOrMethod(expectedMethod));

        assertEquals(expectedMethod, TestNGMethodUtils.getMethod(testNgMethod),
                "ITestNGMethod extraction should return the reflected Java method");
    }

    @Test
    void getMethod_testResult_whenMethodPresent_thenReturnJavaMethod() throws Exception {
        Method expectedMethod = SampleTarget.class.getMethod("sampleMethod");
        ITestResult result = Mockito.mock(ITestResult.class);
        ITestNGMethod testNgMethod = Mockito.mock(ITestNGMethod.class);

        when(result.getMethod()).thenReturn(testNgMethod);
        when(testNgMethod.getConstructorOrMethod()).thenReturn(new ConstructorOrMethod(expectedMethod));

        assertEquals(expectedMethod, TestNGMethodUtils.getMethod(result),
                "ITestResult extraction should return the reflected Java method");
    }

    @Test
    void methodName_whenNullOrMethodProvided_thenFormatConsistently() throws Exception {
        assertEquals("", TestNGMethodUtils.methodName((Method) null),
                "Null methods should format to an empty string");

        Method method = SampleTarget.class.getMethod("sampleMethod");
        assertEquals(SAMPLE_TARGET_SAMPLE_METHOD, TestNGMethodUtils.methodName(method),
                "Method formatting should use the declaring class simple name and method name");

        ITestNGMethod testNgMethod = Mockito.mock(ITestNGMethod.class);
        when(testNgMethod.getConstructorOrMethod()).thenReturn(new ConstructorOrMethod(method));
        assertEquals(SAMPLE_TARGET_SAMPLE_METHOD, TestNGMethodUtils.methodName(testNgMethod),
                "ITestNGMethod formatting should match the Java method format");

        ITestResult result = Mockito.mock(ITestResult.class);
        when(result.getMethod()).thenReturn(testNgMethod);
        assertEquals(SAMPLE_TARGET_SAMPLE_METHOD, TestNGMethodUtils.methodName(result),
                "ITestResult formatting should match the Java method format");
    }
}
