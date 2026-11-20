package io.github.cpjust.testng_annotations.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.testng.IInvokedMethod;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestResult;

/**
 * Utility methods for getting TestNG attributes from results and contexts.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestNGAttributeUtils {
    /**
     * Returns the active TestNG context for the supplied result.
     *
     * @param result The result associated with a TestNG method.
     * @return The current context, or null if the result is missing.
     */
    public static ITestContext testContextFor(ITestResult result) {
        return (result == null) ? null : result.getTestContext();
    }

    /**
     * Returns the suite associated with the test context.
     *
     * @param context The executing TestNG context.
     * @return The enclosing suite, or null when unavailable.
     */
    public static ISuite suiteFor(ITestContext context) {
        return (context == null) ? null : context.getSuite();
    }

    /**
     * Returns the name of the suite associated with the test context.
     *
     * @param context The executing TestNG context.
     * @return The name of the enclosing suite, or null when unavailable.
     */
    public static String suiteNameFor(ITestContext context) {
        ISuite suite = suiteFor(context);
        return (suite == null) ? null : suite.getName();
    }

    /**
     * Determines whether the supplied method has a valid TestNG method.
     *
     * @param method The method to check.
     * @return true when the method has a valid TestNG method.
     */
    public static boolean hasTestNGMethod(IInvokedMethod method) {
        return (method != null) && (method.getTestMethod() != null);
    }

    /**
     * Determines whether the supplied result has a valid TestNG method.
     *
     * @param result The result to check.
     * @return true when the result has a valid TestNG method.
     */
    public static boolean hasTestNGMethod(ITestResult result) {
        return (result != null) && (result.getMethod() != null);
    }

    /**
     * Determines whether the supplied result has a specific TestNG status.
     *
     * @param testResult The result to check.
     * @param status     The TestNG status to check for.
     * @return true when the result has the specified status.
     */
    public static boolean hasTestResultStatus(ITestResult testResult, int status) {
        return (testResult != null) && (testResult.getStatus() == status);
    }
}
