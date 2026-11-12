package io.github.cpjust.testng_annotations.listeners;

import io.github.cpjust.testng_annotations.annotations.FailFast;
import io.github.cpjust.testng_annotations.util.TestNGAttributeUtils;
import io.github.cpjust.testng_annotations.util.TestNGMethodUtils;
import lombok.extern.slf4j.Slf4j;
import org.testng.IConfigurationListener;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.SkipException;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Listens for a method annotated with {@link FailFast} and skips subsequent methods once that
 * method fails.
 */
@Slf4j
public class FailFastListener implements IInvokedMethodListener, ITestListener, IConfigurationListener {

    private static final ThreadLocal<Map<String, Object>> FAIL_FAST_STATE = ThreadLocal.withInitial(HashMap::new);
    private static final ThreadLocal<String> ACTIVE_SUITE_NAME = new ThreadLocal<>();

    private static final String FAIL_FAST_TRIGGERED = "failFastTriggered";
    private static final String FAIL_FAST_TRIGGER_MESSAGE = "failFastTriggerMessage";
    private static final String FAIL_FAST_ACTIVE_METHOD = "failFastActiveMethod";
    private static final String FAIL_FAST_TRIGGER_CLASS = "failFastTriggerClass";
    private static final String LOG_FAIL_FAST_TRIGGERED = "Fail-fast triggered: {}";
    private static final String LOG_FAIL_FAST_ARMED_FOR = "Fail-fast armed for {}";
    private static final String LOG_WHILE_INVOKING = "{} while invoking {}";
    private static final String LOG_SKIPPED_DUE_TO_FAIL_FAST_TRIGGERED_BY = "Skipped due to fail-fast triggered by: ";

    /**
     * Checks the active suite state and skips the current invocation when a fail-fast trigger is active.
     * From the IInvokedMethodListener interface, this method is called before each configuration and test method invocation
     * (irrespective of whether they pass/fail or get skipped), allowing the listener to prevent further execution of tests
     * when a fail-fast condition has been triggered.
     *
     * @param method The invoked method.
     * @param testResult The associated result.
     */
    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        if (TestNGAttributeUtils.hasTestNGMethod(method)) {
            return;
        }

        ITestContext context = TestNGAttributeUtils.testContextFor(testResult);
        String suiteName = TestNGAttributeUtils.suiteNameFor(context);
        String currentMethod = methodIdentity(method);
        Method realMethod = TestNGMethodUtils.getMethod(method.getTestMethod());

        setActiveSuiteName(suiteName, context);
        skipOrSetActiveMethod(context, currentMethod, realMethod);
    }

    /**
     * Prevents configuration methods from running after a fail-fast trigger is active.
     *
     * @param result The configuration result being evaluated.
     */
    @Override
    public void beforeConfiguration(ITestResult result) {
        if (TestNGAttributeUtils.hasTestNGMethod(result)) {
            return;
        }

        ITestContext context = TestNGAttributeUtils.testContextFor(result);
        String suiteName = TestNGAttributeUtils.suiteNameFor(context);
        String currentMethod = TestNGMethodUtils.getMethodName(result);
        Method realMethod = TestNGMethodUtils.getMethod(result);

        setActiveSuiteName(suiteName, context);
        skipOrSetActiveMethod(context, currentMethod, realMethod);
    }

    /**
     * Prevents configuration methods from running after a fail-fast trigger is active.
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
        String suiteName = TestNGAttributeUtils.suiteNameFor(context);
        String currentMethod = TestNGMethodUtils.getMethodName(method);
        Method realMethod = TestNGMethodUtils.getMethod(method);

        setActiveSuiteName(suiteName, context);
        skipOrSetActiveMethod(context, currentMethod, realMethod);
    }

    /**
     * Records a fail-fast trigger when the annotated method fails and keeps the state active for the suite.
     *
     * @param method The invoked method.
     * @param testResult The result for the method invocation.
     */
    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (TestNGAttributeUtils.hasTestNGMethod(method)) {
            return;
        }

        ITestContext context = TestNGAttributeUtils.testContextFor(testResult);
        Method realMethod = TestNGMethodUtils.getMethod(method.getTestMethod());

        if (!hasFailFastAnnotation(realMethod)) {
            return;
        }

        if (TestNGAttributeUtils.hasTestResultStatus(testResult, ITestResult.FAILURE)) {
            setFailFastTriggerMessage(context, makeTriggerMessage(testResult));
            Method failing = TestNGMethodUtils.getMethod(testResult);
            setActiveFailFastTriggerClass(context, (failing == null) ? null : failing.getDeclaringClass());
            setFailFastTriggered(context, true);
            log.warn(LOG_FAIL_FAST_TRIGGERED, failFastTriggerMessage(context));
            return;
        }

        if (isFailFastTriggered(context) || failFastTriggerMessage(context) != null) {
            return;
        }

        clearFailFastState(context);
    }

    /**
     * Records a fail-fast trigger when a configuration method fails.
     *
     * @param result The configuration result.
     */
    @Override
    public void onConfigurationFailure(ITestResult result) {
        handleConfigOrTestFailure(result);
    }

    /**
     * Records a fail-fast trigger when a test method fails.
     *
     * @param result The failing test result.
     */
    @Override
    public void onTestFailure(ITestResult result) {
        handleConfigOrTestFailure(result);
    }

    /**
     * No-op lifecycle hook: fail-fast state is retained for the active suite while its setup is in progress.
     *
     * @param context The active suite context.
     */
    @Override
    public void onStart(ITestContext context) {
        // Intentionally no-op: fail-fast state is scoped to the active suite lifecycle and should not be cleared
        // in the middle of a failing configuration sequence.
    }

    /**
     * No-op lifecycle hook: fail-fast state is retained until the failing method lifecycle has completed.
     *
     * @param context The active suite context.
     */
    @Override
    public void onFinish(ITestContext context) {
        // Intentionally no-op: the listener keeps the fail-fast trigger alive until the suite is finished by the
        // failing method lifecycle itself.
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
     * @param result The test result.
     */
    @Override
    public void onTestSuccess(ITestResult result) {
        // no-op
    }

    /**
     * No-op listener hook.
     *
     * @param result The test result.
     */
    @Override
    public void onTestSkipped(ITestResult result) {
        // no-op
    }

    /**
     * No-op listener hook.
     *
     * @param result The test result.
     */
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // no-op
    }

    /**
     * Stores a fail-fast attribute in the thread-local state and the TestNG context.
     *
     * @param context The active TestNG context.
     * @param key The attribute key.
     * @param value The new value to store.
     */
    private static void setContextValue(ITestContext context, String key, Object value) {
        Map<String, Object> state = FAIL_FAST_STATE.get();
        ISuite suite = TestNGAttributeUtils.suiteFor(context);

        if (value == null) {
            state.remove(key);

            if (context != null) {
                context.removeAttribute(key);
            }

            if (suite != null) {
                suite.removeAttribute(key);
            }

            return;
        }

        state.put(key, value);

        if (context != null) {
            context.setAttribute(key, value);
        }

        if (suite != null) {
            suite.setAttribute(key, value);
        }
    }

    /**
     * Reads a fail-fast attribute from the TestNG context or fallback thread-local state.
     *
     * @param context The active TestNG context.
     * @param key The attribute key.
     * @return the stored value, or null when missing.
     */
    private static Object getContextValue(ITestContext context, String key) {
        if (context != null) {
            Object value = context.getAttribute(key);

            if (value != null) {
                return value;
            }

            ISuite suite = TestNGAttributeUtils.suiteFor(context);

            if (suite != null) {
                value = suite.getAttribute(key);

                if (value != null) {
                    return value;
                }
            }
        }

        return FAIL_FAST_STATE.get().get(key);
    }

    /**
     * Determines whether fail-fast has already been triggered for the active suite.
     *
     * @param context The active TestNG context.
     * @return true when the trigger has been marked as active.
     */
    private static boolean isFailFastTriggered(ITestContext context) {
        return Boolean.TRUE.equals(getContextValue(context, FAIL_FAST_TRIGGERED));
    }

    /**
     * Marks the fail-fast trigger as active or inactive for the current suite.
     *
     * @param context The active TestNG context.
     * @param value The trigger status.
     */
    private static void setFailFastTriggered(ITestContext context, boolean value) {
        setContextValue(context, FAIL_FAST_TRIGGERED, value);
    }

    /**
     * Returns the descriptive trigger message recorded for the fail-fast call.
     *
     * @param context The active TestNG context.
     * @return the trigger message, or null when no failure has been recorded.
     */
    private static String failFastTriggerMessage(ITestContext context) {
        Object value = getContextValue(context, FAIL_FAST_TRIGGER_MESSAGE);
        return (value == null) ? null : value.toString();
    }

    /**
     * Stores the fail-fast trigger message in the active context.
     *
     * @param context The active TestNG context.
     * @param value The trigger message.
     */
    private static void setFailFastTriggerMessage(ITestContext context, String value) {
        setContextValue(context, FAIL_FAST_TRIGGER_MESSAGE, value);
    }

    /**
     * Returns the method that most recently armed the fail-fast listener.
     *
     * @param context The active TestNG context.
     * @return the active fail-fast method name.
     */
    private static String activeFailFastMethod(ITestContext context) {
        Object value = getContextValue(context, FAIL_FAST_ACTIVE_METHOD);
        return (value == null) ? null : value.toString();
    }

    /**
     * Stores the name of the method that armed fail-fast.
     *
     * @param context The active TestNG context.
     * @param value The active method name.
     */
    private static void setActiveFailFastMethod(ITestContext context, String value) {
        setContextValue(context, FAIL_FAST_ACTIVE_METHOD, value);
    }

    /**
     * Returns the class that triggered the current fail-fast event.
     *
     * @param context The active TestNG context.
     * @return the triggering class, or null when no class has triggered fail-fast.
     */
    private static Class<?> activeFailFastTriggerClass(ITestContext context) {
        Object value = getContextValue(context, FAIL_FAST_TRIGGER_CLASS);
        return (value == null) ? null : (Class<?>) value;
    }

    /**
     * Stores the class that triggered the current fail-fast event.
     *
     * @param context The active TestNG context.
     * @param value The trigger class.
     */
    private static void setActiveFailFastTriggerClass(ITestContext context, Class<?> value) {
        setContextValue(context, FAIL_FAST_TRIGGER_CLASS, value);
    }

    /**
     * Clears all fail-fast state from the supplied context.
     *
     * @param context The active TestNG context.
     */
    private static void clearFailFastState(ITestContext context) {
        setFailFastTriggered(context, false);
        setFailFastTriggerMessage(context, null);
        setActiveFailFastMethod(context, null);
        setActiveFailFastTriggerClass(context, null);
        FAIL_FAST_STATE.remove();
    }

    /**
     * Determines whether the current method should be skipped because fail-fast is active.
     *
     * @param context The active TestNG context.
     * @param currentMethod The method currently being evaluated.
     * @param realMethod The reflected Java method for the invocation.
     * @return true when the current method should be skipped.
     */
    private static boolean shouldSkipDueToFailFast(ITestContext context, String currentMethod, Method realMethod) {
        if (context == null || currentMethod == null) {
            return false;
        }

        if (!isFailFastTriggered(context) && failFastTriggerMessage(context) == null) {
            return false;
        }

        Class<?> triggerClass = activeFailFastTriggerClass(context);
        Class<?> currentDeclaring = (realMethod == null) ? null : realMethod.getDeclaringClass();

        return (triggerClass != null)
                && (currentDeclaring != null)
                && triggerClass.isAssignableFrom(currentDeclaring)
                && !Objects.equals(activeFailFastMethod(context), currentMethod);
    }

    /**
     * Builds a human-readable message for the method that triggered fail-fast.
     *
     * @param result The result from the failing method.
     * @return a descriptive message about the method and the reason for failure.
     */
    private static String makeTriggerMessage(ITestResult result) {
        if (result == null) {
            return "(no details)";
        }

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
     * Detects whether the given method has the {@link FailFast} annotation.
     *
     * @param method The method to inspect.
     * @return true when it is annotated as fail-fast.
     */
    private static boolean hasFailFastAnnotation(Method method) {
        return (method != null) && method.isAnnotationPresent(FailFast.class);
    }

    /**
     * Detects whether the given TestNG result belongs to a fail-fast method.
     *
     * @param result The result to inspect.
     * @return true when the corresponding method is annotated as fail-fast.
     */
    private static boolean hasFailFastAnnotation(ITestResult result) {
        if (result == null) {
            return false;
        }

        try {
            return hasFailFastAnnotation(TestNGMethodUtils.getMethod(result));
        } catch (RuntimeException ex) {
            log.debug("Unable to inspect method for @FailFast annotation.", ex);
            return false;
        }
    }

    /**
     * Creates a stable identity string for a TestNG invoked method.
     *
     * @param method The invoked method to inspect.
     * @return a unique identity string for the method.
     */
    private static String methodIdentity(IInvokedMethod method) {
        if (TestNGAttributeUtils.hasTestNGMethod(method)) {
            return "";
        }

        return TestNGMethodUtils.getMethodName(method.getTestMethod());
    }

    /**
     * Determines whether the current method should be skipped because fail-fast is active.
     *
     * @param suiteName The name of the suite currently being executed.
     * @param context The TestNG context.
     */
    private static void setActiveSuiteName(String suiteName, ITestContext context) {
        if ((suiteName != null) && !Objects.equals(suiteName, ACTIVE_SUITE_NAME.get())) {
            clearFailFastState(context);
            ACTIVE_SUITE_NAME.set(suiteName);
        } else if (suiteName != null) {
            ACTIVE_SUITE_NAME.set(suiteName);
        }
    }

    /**
     * Determines whether the current method should be skipped because fail-fast is active, then either skips the method
     * or sets it as the active fail-fast method.
     *
     * @param context The TestNG context.
     * @param currentMethod The name of the method currently being evaluated.
     * @param realMethod The reflected Java method for the invocation.
     */
    private static void skipOrSetActiveMethod(ITestContext context, String currentMethod, Method realMethod) {
        if (shouldSkipDueToFailFast(context, currentMethod, realMethod)) {
            String message = LOG_SKIPPED_DUE_TO_FAIL_FAST_TRIGGERED_BY + failFastTriggerMessage(context);
            log.debug(LOG_WHILE_INVOKING, message, currentMethod);
            throw new SkipException(message);
        } else if (hasFailFastAnnotation(realMethod)) {
            setActiveFailFastMethod(context, currentMethod);
            setFailFastTriggered(context, true);
            log.debug(LOG_FAIL_FAST_ARMED_FOR, activeFailFastMethod(context));
        }
    }

    /**
     * Records a fail-fast trigger when a configuration or test method fails.
     *
     * @param result The result from the failing method.
     */
    private static void handleConfigOrTestFailure(ITestResult result) {
        ITestContext context = TestNGAttributeUtils.testContextFor(result);

        if (hasFailFastAnnotation(result)) {
            setFailFastTriggerMessage(context, makeTriggerMessage(result));
            Method failing = TestNGMethodUtils.getMethod(result);
            setActiveFailFastTriggerClass(context, (failing == null) ? null : failing.getDeclaringClass());
            setFailFastTriggered(context, true);
            log.warn(LOG_FAIL_FAST_TRIGGERED, failFastTriggerMessage(context));
        }
    }
}
