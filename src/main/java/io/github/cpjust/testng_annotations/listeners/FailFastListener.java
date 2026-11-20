package io.github.cpjust.testng_annotations.listeners;

import io.github.cpjust.testng_annotations.annotations.FailFast;
import io.github.cpjust.testng_annotations.util.TestNGAttributeUtils;
import io.github.cpjust.testng_annotations.util.TestNGMethodUtils;
import lombok.extern.slf4j.Slf4j;
import org.testng.IConfigurationListener;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.SkipException;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Skips methods in the class hierarchy of a failed {@link FailFast}-annotated method.
 *
 * <p>Failure state is shared by all threads in a suite and discarded when that suite finishes. A method
 * that has already started cannot be interrupted.</p>
 */
@Slf4j
public class FailFastListener implements IInvokedMethodListener, ITestListener, IConfigurationListener, ISuiteListener {
    static final String BLOCKS_SHOULD_BE_SYNCHRONIZED_ON_PRIVATE_FINAL_FIELDS = "java:S2445"; // Suppress "Blocks should be synchronized on private final fields" warning.

    private static final String STATE_ATTRIBUTE = FailFastListener.class.getName() + ".state";
    private static final String LOG_FAIL_FAST_TRIGGERED = "Fail-fast triggered: {}";
    private static final String LOG_FAIL_FAST_ENABLED_FOR = "Fail-fast enabled for {}";
    private static final String LOG_WHILE_INVOKING = "{} while invoking {}";
    private static final String SKIP_MESSAGE_PREFIX = "Skipped due to fail-fast triggered by: ";

    //region IConfigurationListener methods

    /**
     * Checks whether a configuration method should be skipped after a fail-fast failure.
     *
     * @param result The configuration result being evaluated.
     */
    @Override
    public void beforeConfiguration(ITestResult result) {
        if (!TestNGAttributeUtils.hasTestNGMethod(result)) {
            return;
        }

        ITestContext context = TestNGAttributeUtils.testContextFor(result);
        Method realMethod = TestNGMethodUtils.getMethod(result);
        skipOrLogFailFast(context, TestNGMethodUtils.getMethodName(result), realMethod, getExecutingClass(result, realMethod));
    }

    /**
     * Checks whether a configuration method should be skipped after a fail-fast failure.
     *
     * @param result The configuration result being evaluated.
     * @param method The TestNG method metadata.
     */
    @Override
    public void beforeConfiguration(ITestResult result, ITestNGMethod method) {
        if ((result == null) || (method == null)) {
            return;
        }

        ITestContext context = TestNGAttributeUtils.testContextFor(result);
        Method realMethod = TestNGMethodUtils.getMethod(method);
        skipOrLogFailFast(context, TestNGMethodUtils.getMethodName(method), realMethod, getExecutingClass(result, realMethod));
    }

    /**
     * Records a failure when the configuration method is annotated with {@link FailFast}.
     *
     * @param result The failed configuration result.
     */
    @Override
    public void onConfigurationFailure(ITestResult result) {
        recordFailure(result);
    }

    //endregion IConfigurationListener methods

    //region IInvokedMethodListener methods

    /**
     * Checks whether the invoked method should be skipped after a fail-fast failure.
     *
     * @param method The invoked method.
     * @param testResult The associated result.
     */
    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        if (!TestNGAttributeUtils.hasTestNGMethod(method)) {
            return;
        }

        ITestContext context = TestNGAttributeUtils.testContextFor(testResult);
        Method realMethod = TestNGMethodUtils.getMethod(method.getTestMethod());
        skipOrLogFailFast(context, methodIdentity(method), realMethod, getExecutingClass(testResult, realMethod));
    }

    /**
     * Records a failure when the invoked method is annotated with {@link FailFast}.
     *
     * @param method The invoked method.
     * @param testResult The result for that invocation.
     */
    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (TestNGAttributeUtils.hasTestNGMethod(method)
                && TestNGAttributeUtils.hasTestResultStatus(testResult, ITestResult.FAILURE)) {
            recordFailure(testResult);
        }
    }

    //endregion IInvokedMethodListener methods

    //region ITestListener methods

    /**
     * Records a failure when a test method is annotated with {@link FailFast}.
     *
     * @param result The failed test result.
     */
    @Override
    public void onTestFailure(ITestResult result) {
        recordFailure(result);
    }

    /**
     * No-op listener hook.
     *
     * @param context The active TestNG context.
     */
    @Override
    public void onStart(ITestContext context) {
        // State is initialized lazily so multiple contexts in a suite share the same state.
    }

    /**
     * No-op listener hook.
     *
     * @param context The completed TestNG context.
     */
    @Override
    public void onFinish(ITestContext context) {
        // State belongs to the suite and is removed by onFinish(ISuite).
    }

    /**
     * No-op listener hook.
     *
     * @param result The test result.
     */
    @Override
    public void onTestStart(ITestResult result) {
        // no-op
    }

    /**
     * No-op listener hook.
     *
     * @param result The successful test result.
     */
    @Override
    public void onTestSuccess(ITestResult result) {
        // no-op
    }

    /**
     * No-op listener hook.
     *
     * @param result The skipped test result.
     */
    @Override
    public void onTestSkipped(ITestResult result) {
        // no-op
    }

    /**
     * No-op listener hook.
     *
     * @param result The test result that failed within its success percentage.
     */
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // no-op
    }

    //endregion ITestListener methods

    //region ISuiteListener methods

    /**
     * Removes any stale state before a suite starts.
     *
     * @param suite The starting TestNG suite.
     */
    @SuppressWarnings(BLOCKS_SHOULD_BE_SYNCHRONIZED_ON_PRIVATE_FINAL_FIELDS)
    @Override
    public void onStart(ISuite suite) {
        if (suite != null) {
            // Need to synchronize on the suite because multiple threads may be starting different contexts in the same suite.
            synchronized (suite) {
                suite.removeAttribute(STATE_ATTRIBUTE);
            }
        }
    }

    /**
     * Removes the suite's fail-fast state after all suite methods have completed.
     *
     * @param suite The completed TestNG suite.
     */
    @SuppressWarnings(BLOCKS_SHOULD_BE_SYNCHRONIZED_ON_PRIVATE_FINAL_FIELDS)
    @Override
    public void onFinish(ISuite suite) {
        if (suite != null) {
            // Need to synchronize on the suite because multiple threads may be starting different contexts in the same suite.
            synchronized (suite) {
                suite.removeAttribute(STATE_ATTRIBUTE);
            }
        }
    }

    //endregion ISuiteListener methods

    /**
     * Checks the recorded suite failures and either skips the current method or logs its fail-fast annotation.
     *
     * @param context The active TestNG context.
     * @param currentMethod The method currently being evaluated.
     * @param realMethod The reflected Java method for the invocation.
     * @param executingClass The runtime class of the instance executing the method.
     */
    private static void skipOrLogFailFast(
            ITestContext context,
            String currentMethod,
            Method realMethod,
            Class<?> executingClass
    ) {
        if ((context == null) || (currentMethod == null)) {
            return;
        }

        SuiteState state = getSuiteState(context, false);

        if ((state != null) && state.shouldSkip(executingClass)) {
            String message = SKIP_MESSAGE_PREFIX + state.messageFor(executingClass);
            log.debug(LOG_WHILE_INVOKING, message, currentMethod);
            throw new SkipException(message);
        }

        if (hasFailFastAnnotation(realMethod)) {
            log.debug(LOG_FAIL_FAST_ENABLED_FOR, currentMethod);
        }
    }

    /**
     * Returns the runtime class of the instance executing a method.
     * i.e. The class of the object that is running the test method, or the class that declared the method when no instance is available.
     *
     * @param result The result for the current invocation.
     * @param method The reflected method for the current invocation.
     * @return The runtime instance class, or the method's declaring class when no instance is available.
     */
    private static Class<?> getExecutingClass(ITestResult result, Method method) {
        Object instance = (result == null) ? null : result.getInstance();

        if (instance != null) {
            return instance.getClass();
        } else if (method != null) {
            return method.getDeclaringClass();
        } else {
            return null;
        }
    }

    /**
     * Records an annotated method failure in the state associated with its suite.
     *
     * @param result The failed TestNG result.
     */
    private static void recordFailure(ITestResult result) {
        if (!TestNGAttributeUtils.hasTestNGMethod(result)) {
            return;
        }

        Method method = TestNGMethodUtils.getMethod(result);

        if (!hasFailFastAnnotation(method)) {
            return;
        }

        ITestContext context = TestNGAttributeUtils.testContextFor(result);
        SuiteState state = getSuiteState(context, true);

        if (state == null) {
            log.warn("Unable to record @FailFast failure because the TestNG suite context is unavailable.");
            return;
        }

        String message = makeTriggerMessage(result);

        if (state.recordFailure(method.getDeclaringClass(), message)) {
            log.warn(LOG_FAIL_FAST_TRIGGERED, message);
        }
    }

    /**
     * Returns the suite-owned state, creating it atomically when requested.
     *
     * @param context The active TestNG context.
     * @param create Whether state should be created when missing.
     * @return The suite state, or null when the context or suite is unavailable.
     */
    private static SuiteState getSuiteState(ITestContext context, boolean create) {
        ISuite suite = TestNGAttributeUtils.suiteFor(context);

        if (suite == null) {
            return null;
        }

        synchronized (suite) {
            Object value = suite.getAttribute(STATE_ATTRIBUTE);

            if (value instanceof SuiteState) {
                return (SuiteState) value;
            }

            if (value != null) {
                log.error("Unexpected value stored under fail-fast suite attribute: {}", value.getClass().getName());
                return null;
            }

            if (!create) {
                return null;
            }

            SuiteState state = new SuiteState();
            suite.setAttribute(STATE_ATTRIBUTE, state);
            return state;
        }
    }

    /**
     * Builds a descriptive message for the method that triggered fail-fast.
     *
     * @param result The result from the failing method.
     * @return A message containing the method name and failure reason.
     */
    private static String makeTriggerMessage(ITestResult result) {
        Method method = TestNGMethodUtils.getMethod(result);
        String methodName = (method == null)
                ? result.getMethod().getMethodName()
                : TestNGMethodUtils.getMethodName(method);
        Throwable throwable = result.getThrowable();
        String reason = (throwable == null)
                ? "failed"
                : throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
        return methodName + " " + reason;
    }

    /**
     * Determines whether the supplied method carries the fail-fast annotation.
     *
     * @param method The reflected Java method.
     * @return True when the method is annotated with {@link FailFast}.
     */
    private static boolean hasFailFastAnnotation(Method method) {
        return (method != null) && method.isAnnotationPresent(FailFast.class);
    }

    /**
     * Creates a stable identity string for a TestNG invoked method.
     *
     * @param method The invoked method to inspect.
     * @return The method's class-and-name identity.
     */
    private static String methodIdentity(IInvokedMethod method) {
        return TestNGMethodUtils.getMethodName(method.getTestMethod());
    }

    /**
     * Holds fail-fast failures for one suite.
     */
    static final class SuiteState {
        private final Map<Class<?>, String> failures = new LinkedHashMap<>();

        /**
         * Adds a failure for the class that declared its annotated method.
         *
         * @param triggerClass The class declaring the failed method.
         * @param message A human-readable failure message.
         * @return True when the class was newly recorded.
         */
        synchronized boolean recordFailure(Class<?> triggerClass, String message) {
            return failures.putIfAbsent(triggerClass, message) == null;
        }

        /**
         * Determines whether an executing class belongs to a failed class hierarchy.
         *
         * @param executingClass The runtime class of the instance being evaluated.
         * @return True when a recorded trigger class is an ancestor of the executing class.
         */
        synchronized boolean shouldSkip(Class<?> executingClass) {
            if (executingClass == null) {
                return false;
            }

            return failures.keySet().stream()
                    .anyMatch(triggerClass -> triggerClass.isAssignableFrom(executingClass));
        }

        /**
         * Returns the failure message for the trigger applicable to an executing class.
         *
         * @param executingClass The runtime class of the instance being evaluated.
         * @return The applicable failure message, or a fallback when no match is available.
         */
        synchronized String messageFor(Class<?> executingClass) {
            if (executingClass == null) {
                return "(no details)";
            }

            return failures.entrySet().stream()
                    .filter(entry -> entry.getKey().isAssignableFrom(executingClass))
                    // The most specific trigger is the one with the closest ancestor relationship to the executing class.
                    .reduce((mostSpecific, candidate) ->
                            mostSpecific.getKey().isAssignableFrom(candidate.getKey()) ? candidate : mostSpecific)
                    .map(Map.Entry::getValue)
                    .orElse("(no details)");
        }
    }
}
