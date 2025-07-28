package io.github.cpjust.testng_annotations.listeners;

import io.github.cpjust.testng_annotations.annotations.IncludeOnEnv;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A listener for TestNG tests that are annotated with <code>@IncludeOnEnv</code>.
 * To register this listener, either define it in the <code>src/test/resources/META-INF/services/org.testng.ITestNGListener</code>
 * file or add the <code>@Listeners({IncludeOnEnvListener.class})</code> annotation to the test class.
 */
@Slf4j
@NoArgsConstructor
public class IncludeOnEnvListener extends EnvListenerBase implements IMethodInterceptor {
    /**
     * Intercepts the list of tests that TestNG intends to run and allows us to modify that list by removing any tests
     * that are annotated with <code>@IncludeOnEnv</code> which has an environment that doesn't match the current environment.
     *
     * @param methods The list of test methods to filter.
     * @param context Unused.
     * @return The new list of test methods to run.
     */
    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        return methods.stream()
                .filter(this::shouldIncludeTest)
                .collect(Collectors.toList());
    }

    /**
     * Determines if a test method or test class should be included based on its annotations.
     * If class has `@IncludeOnEnv` but doesn't match current env -> exclude unless test method specifically matches
     * Exclusion rules:
     * 1. If method is excluded -> exclude test
     * 2. If method is included -> include test
     * 3. If method has no annotation and class is excluded -> exclude test
     * 4. Otherwise -> include test
     *
     * @param methodInstance The test method to check.
     * @return True if the class or method should be included, otherwise false.
     */
    private boolean shouldIncludeTest(@NonNull IMethodInstance methodInstance) {
        AnnotatedElement testClass = methodInstance.getMethod().getRealClass();
        AnnotatedElement testMethod = methodInstance.getMethod().getConstructorOrMethod().getMethod();

        // Tests should be included using the following rules:
        // |                     | No class annotation: | Include by class: | Exclude by class: |
        // | ------------------- | -------------------- | ----------------- | ----------------- |
        // | No test annotation: |    INCLUDE           |   INCLUDE         |   EXCLUDE         |
        // | Include by test:    |    INCLUDE           |   INCLUDE         |   INCLUDE         |
        // | Exclude by test:    |    EXCLUDE           |   EXCLUDE         |   EXCLUDE         |

        boolean hasAnnotationOnClass = testClass.isAnnotationPresent(IncludeOnEnv.class);
        boolean hasAnnotationOnTest = testMethod.isAnnotationPresent(IncludeOnEnv.class);
        boolean shouldIncludeClass = isIncludedByAnnotation(testClass);
        boolean shouldIncludeMethod = isIncludedByAnnotation(testMethod);
        boolean shouldInclude = shouldIncludeMethod;

        if (hasAnnotationOnClass && !shouldIncludeClass) {
            shouldInclude = (hasAnnotationOnTest && shouldIncludeMethod);
        }

        if (log.isDebugEnabled()) {
            log.debug("*** hasAnnotationOnClass: {}, hasAnnotationOnTest: {}, shouldIncludeClass: {}, shouldIncludeMethod: {}",
                    hasAnnotationOnClass, hasAnnotationOnTest, shouldIncludeClass, shouldIncludeMethod);

            log.debug("Test {}.{} should {}be included",
                    methodInstance.getMethod().getConstructorOrMethod().getDeclaringClass().getSimpleName(),
                    methodInstance.getMethod().getConstructorOrMethod().getMethod().getName(),
                    shouldInclude ? "" : "NOT ");
        }

        return shouldInclude;
    }

    /**
     * Checks if a class or method has the <code>@IncludeOnEnv</code> annotation and should be included.
     *
     * @param element The AnnotatedElement to check.
     * @return True if the AnnotatedElement should be included or doesn't have a <code>@IncludeOnEnv</code> annotation, otherwise false.
     */
    private boolean isIncludedByAnnotation(@NonNull AnnotatedElement element) {
        if (!element.isAnnotationPresent(IncludeOnEnv.class)) {
            return true; // If no annotation, allow test.
        }

        IncludeOnEnv includeOnEnv = element.getAnnotation(IncludeOnEnv.class);
        return anyEnvMatches(includeOnEnv.propertyName(), includeOnEnv.value(), "IncludeOnEnv");
    }
}
