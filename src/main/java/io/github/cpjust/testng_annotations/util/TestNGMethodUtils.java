package io.github.cpjust.testng_annotations.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.testng.IMethodInstance;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import java.lang.reflect.Method;

/**
 * Utility methods for reading TestNG methods and producing consistent method names.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestNGMethodUtils {

    /**
     * Returns the reflected Java method for a TestNG method instance.
     *
     * @param methodInstance the TestNG method instance
     * @return the reflected method, or null when unavailable
     */
    public static Method getMethod(IMethodInstance methodInstance) {
        if ((methodInstance == null) || (methodInstance.getMethod() == null)) {
            return null;
        }

        return methodInstance.getMethod().getConstructorOrMethod().getMethod();
    }

    /**
     * Returns the reflected Java method for a TestNG method.
     *
     * @param testNgMethod the TestNG method
     * @return the reflected method, or null when unavailable
     */
    public static Method getMethod(ITestNGMethod testNgMethod) {
        if (testNgMethod == null) {
            return null;
        }

        return testNgMethod.getConstructorOrMethod().getMethod();
    }

    /**
     * Returns the reflected Java method for a TestNG test result.
     *
     * @param result the TestNG result
     * @return the reflected method, or null when unavailable
     */
    public static Method getMethod(ITestResult result) {
        if (result == null || result.getMethod() == null) {
            return null;
        }

        return result.getMethod().getConstructorOrMethod().getMethod();
    }

    /**
     * Produces a simple class-and-method name for logging.
     *
     * @param method the method to format
     * @return the class and method name, or an empty string when method is null
     */
    public static String methodName(Method method) {
        if (method == null) {
            return "";
        }

        return method.getDeclaringClass().getSimpleName() + "." + method.getName();
    }

    /**
     * Produces a simple class-and-method name for logging.
     *
     * @param testNgMethod the TestNG method to format
     * @return the class and method name, or an empty string when unavailable
     */
    public static String methodName(ITestNGMethod testNgMethod) {
        return methodName(getMethod(testNgMethod));
    }

    /**
     * Produces a simple class-and-method name for logging.
     *
     * @param result the TestNG result to format
     * @return the class and method name, or an empty string when unavailable
     */
    public static String methodName(ITestResult result) {
        return methodName(getMethod(result));
    }
}
