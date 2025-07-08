package io.github.cpjust.testng_annotations.listeners;

import io.github.cpjust.testng_annotations.annotations.ExcludeOnEnv;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A listener for TestNG tests that are annotated with <code>@ExcludeOnEnv</code>.
 * To register this listener, either define it in the <code>src/test/resources/META-INF/services/org.testng.ITestNGListener</code>
 * file or add the <code>@Listeners({ExcludeOnEnvListener.class})</code> annotation to the test class.
 */
@Slf4j
@NoArgsConstructor
public class ExcludeOnEnvListener extends EnvListenerBase implements IMethodInterceptor {
    /**
     * Intercepts the list of tests that TestNG intends to run and allows us to modify that list by removing any tests
     * that are annotated with <code>@ExcludeOnEnv</code> which has an environment that matches the current environment.
     *
     * @param methods The list of test methods to filter.
     * @param context Unused.
     * @return The new list of test methods to run.
     */
    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        List<IMethodInstance> result = new ArrayList<>();

        for (IMethodInstance methodInstance : methods) {
            Class<?> testClass = methodInstance.getMethod().getRealClass();
            Method testMethod = methodInstance.getMethod().getConstructorOrMethod().getMethod();

            Objects.requireNonNull(testClass, "testClass was null!");
            Objects.requireNonNull(testMethod, "testMethod was null!");

            log.debug("intercept() is checking method: {}", testMethod.getName());

            if (!shouldExcludeTest(testClass, testMethod)) {
                log.debug("-> adding method because it doesn't exclude the current environment.");
                result.add(methodInstance);
            } else {
                log.debug("-> method NOT added due to environment exclusion.");
            }
        }

        return result;
    }

    /**
     * Determines if a test method or test class should be excluded based on its annotations.
     *
     * @param testClass  The test class to check.
     * @param testMethod The test method to check.
     * @return True if the class or method should be excluded, otherwise false.
     */
    private boolean shouldExcludeTest(@NonNull Class<?> testClass, @NonNull Method testMethod) {
        // Tests should be excluded using the following rules:
        // |                     | No class annotation: | Include by class: | Exclude by class: |
        // | ------------------- | -------------------- | ----------------- | ----------------- |
        // | No test annotation: |    INCLUDE           |   INCLUDE         |   EXCLUDE         |
        // | Include by test:    |    INCLUDE           |   INCLUDE         |   EXCLUDE         |
        // | Exclude by test:    |    EXCLUDE           |   EXCLUDE         |   EXCLUDE         |
        return isExcludedByAnnotation(testClass) || isExcludedByAnnotation(testMethod);
    }

    /**
     * Checks if a class or method has the <code>@ExcludeOnEnv</code> annotation and should be excluded.
     *
     * @param element The AnnotatedElement to check.
     * @return True if the AnnotatedElement should be excluded, otherwise false.
     */
    private boolean isExcludedByAnnotation(@NonNull AnnotatedElement element) {
        if (!element.isAnnotationPresent(ExcludeOnEnv.class)) {
            return false;
        }

        ExcludeOnEnv excludeOnEnv = element.getAnnotation(ExcludeOnEnv.class);
        return anyEnvMatches(excludeOnEnv.propertyName(), excludeOnEnv.value());
    }
}
