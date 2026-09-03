package io.github.cpjust.testng_annotations.listeners;

import io.github.cpjust.testng_annotations.annotations.FailFast;
import lombok.extern.slf4j.Slf4j;
import org.testng.IConfigurationListener;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.SkipException;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Listens for a method annotated with {@link FailFast} and skips subsequent methods once that
 * method fails.
 */
@Slf4j
public class FailFastListener implements IInvokedMethodListener, ITestListener, IConfigurationListener {

    private static final AtomicBoolean failFastTriggered = new AtomicBoolean(false);
    private static final AtomicReference<String> triggerMessage = new AtomicReference<>();
    private static final AtomicReference<String> activeFailFastMethod = new AtomicReference<>();

    /**
     * Builds a human-readable message for the method that triggered fail-fast.
     *
     * @param result the result from the failing method
     * @return a descriptive message
     */
    private static String makeTriggerMessage(ITestResult result) {
        if (result == null) {
            return "(no details)";
        }

        Method method = result.getMethod().getConstructorOrMethod().getMethod();
        String methodName = (method == null)
                ? result.getMethod().getMethodName()
                : method.getDeclaringClass().getSimpleName() + "." + method.getName();
        Throwable throwable = result.getThrowable();
        String reason = (throwable == null)
                ? "failed"
                : throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
        return methodName + " " + reason;
    }

    /**
     * Detects whether the given method has the {@link FailFast} annotation.
     *
     * @param method the method to inspect
     * @return true when it is annotated as fail-fast
     */
    private static boolean hasFailFastAnnotation(Method method) {
        return (method != null) && method.isAnnotationPresent(FailFast.class);
    }

    /**
     * Detects whether the given TestNG result belongs to a fail-fast method.
     *
     * @param result the result to inspect
     * @return true when the corresponding method is annotated as fail-fast
     */
    private static boolean hasFailFastAnnotation(ITestResult result) {
        if (result == null) {
            return false;
        }

        try {
            return hasFailFastAnnotation(result.getMethod().getConstructorOrMethod().getMethod());
        } catch (RuntimeException ex) {
            log.debug("Unable to inspect method for @FailFast annotation.", ex);
            return false;
        }
    }

    /**
     * Creates a stable identity string for a TestNG invoked method.
     *
     * @param method the invoked method to inspect
     * @return a unique identity string for the method
     */
    private static String methodIdentity(IInvokedMethod method) {
        if (method == null || method.getTestMethod() == null) {
            return "";
        }

        return method.getTestMethod().getRealClass().getName() + "." + method.getTestMethod().getMethodName();
    }

    /**
     * Returns the currently active fail-fast method identity.
     *
     * @return the active fail-fast method identity, or empty when there is no active fail-fast method
     */
    private static String activeFailFastMethodIdentity() {
        return activeFailFastMethod.get();
    }

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method == null || method.getTestMethod() == null) {
            return;
        }

        String currentMethod = methodIdentity(method);
        if (failFastTriggered.get()) {
            if (!Objects.equals(activeFailFastMethodIdentity(), currentMethod)) {
                String message = "Skipped due to fail-fast triggered by: " + triggerMessage.get();
                log.debug("{} while invoking {}", message, currentMethod);
                throw new SkipException(message);
            }
            return;
        }

        Method realMethod = method.getTestMethod().getConstructorOrMethod().getMethod();
        if (hasFailFastAnnotation(realMethod)) {
            activeFailFastMethod.set(currentMethod);
            failFastTriggered.set(true);
            log.debug("Fail-fast configured for {}", activeFailFastMethodIdentity());
        }
    }

    @Override
    public void beforeConfiguration(ITestResult result) {
        if (result == null || result.getMethod() == null) {
            return;
        }

        String currentMethod = result.getMethod().getRealClass().getName() + "." + result.getMethod().getMethodName();
        if (failFastTriggered.get()) {
            if (!Objects.equals(activeFailFastMethodIdentity(), currentMethod)) {
                String message = "Skipped due to fail-fast triggered by: " + triggerMessage.get();
                log.debug("{} while invoking {}", message, currentMethod);
                throw new SkipException(message);
            }
            return;
        }

        Method realMethod = result.getMethod().getConstructorOrMethod().getMethod();
        if (hasFailFastAnnotation(realMethod)) {
            activeFailFastMethod.set(currentMethod);
            failFastTriggered.set(true);
            log.debug("Fail-fast configured for {}", activeFailFastMethodIdentity());
        }
    }

    @Override
    public void beforeConfiguration(ITestResult result, ITestNGMethod method) {
        if (result == null || method == null) {
            return;
        }

        String currentMethod = method.getRealClass().getName() + "." + method.getMethodName();
        if (failFastTriggered.get()) {
            if (!Objects.equals(activeFailFastMethodIdentity(), currentMethod)) {
                String message = "Skipped due to fail-fast triggered by: " + triggerMessage.get();
                log.debug("{} while invoking {}", message, currentMethod);
                throw new SkipException(message);
            }
            return;
        }

        Method realMethod = method.getConstructorOrMethod().getMethod();
        if (hasFailFastAnnotation(realMethod)) {
            activeFailFastMethod.set(currentMethod);
            failFastTriggered.set(true);
            log.debug("Fail-fast configured for {}", activeFailFastMethodIdentity());
        }
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method == null || method.getTestMethod() == null) {
            return;
        }

        Method realMethod = method.getTestMethod().getConstructorOrMethod().getMethod();
        if (!hasFailFastAnnotation(realMethod)) {
            return;
        }

        if ((testResult != null) && (testResult.getStatus() == ITestResult.FAILURE)) {
            triggerMessage.set(makeTriggerMessage(testResult));
            log.warn("Fail-fast triggered: {}", triggerMessage.get());
            return;
        }

        failFastTriggered.set(false);
        triggerMessage.set(null);
        activeFailFastMethod.set(null);
    }

    @Override
    public void onConfigurationFailure(ITestResult result) {
        if (hasFailFastAnnotation(result)) {
            triggerMessage.set(makeTriggerMessage(result));
            failFastTriggered.set(true);
            log.warn("Fail-fast triggered: {}", triggerMessage.get());
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        if (hasFailFastAnnotation(result)) {
            triggerMessage.set(makeTriggerMessage(result));
            failFastTriggered.set(true);
            log.warn("Fail-fast triggered: {}", triggerMessage.get());
        }
    }

    @Override
    public void onStart(ITestContext context) {
        failFastTriggered.set(false);
        triggerMessage.set(null);
        activeFailFastMethod.set(null);
    }

    @Override
    public void onFinish(ITestContext context) {
        failFastTriggered.set(false);
        triggerMessage.set(null);
        activeFailFastMethod.set(null);
    }

    @Override
    public void onTestStart(ITestResult result) {
        // no-op
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        // no-op
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        // no-op
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // no-op
    }
}
