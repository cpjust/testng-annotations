package com.github.cpjust.testng_annotations.listeners;

import com.github.cpjust.testng_annotations.annotations.ExcludeOnEnv;
import lombok.extern.slf4j.Slf4j;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A listener for TestNG tests that are annotated with `@ExcludeOnEnv`.
 * To register this listener, either define it in the `src/test/resources/META-INF/services/org.testng.ITestNGListener`
 * file or add the `@Listeners({ExcludeOnEnvListener.class})` annotation to the test class.
 */
@Slf4j
public class ExcludeOnEnvListener implements IMethodInterceptor {
    /**
     * Intercepts the list of tests that TestNG intends to run and allows us to modify that list by removing any tests
     * that are annotated with `@ExcludeOnEnv` which has an environment that matches the current environment.
     *
     * @param methods The list of test methods to filter.
     * @param context Unused.
     * @return The new list of test methods to run.
     */
    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        List<IMethodInstance> result = new ArrayList<>();

        for (IMethodInstance m : methods) {
            Method testMethod = m.getMethod().getConstructorOrMethod().getMethod();
            Objects.requireNonNull(testMethod, "testMethod was null!");
            log.debug("intercept() is checking method: {}", testMethod.getName());

            if (!testMethod.isAnnotationPresent(ExcludeOnEnv.class)) {
                log.debug("-> adding method because it doesn't have a ExcludeOnEnv annotation.");
                result.add(m);
            } else {
                ExcludeOnEnv excludeOnEnv = testMethod.getAnnotation(ExcludeOnEnv.class);
                String[] excludedEnvs = excludeOnEnv.value();
                String propertyName = excludeOnEnv.propertyName();
                String currentEnv = System.getProperty(propertyName, null);

                if (!Arrays.asList(excludedEnvs).contains(currentEnv)) {
                    log.debug("-> adding method because it doesn't exclude env '{}'.", currentEnv);
                    result.add(m); // Add test if currentEnv doesn't match any excluded environment.
                } else {
                    log.debug("-> method NOT added because it excludes env '{}'.", currentEnv);
                }
            }
        }

        return result;
    }
}
