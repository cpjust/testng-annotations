package io.github.cpjust.testng_annotations.listeners;

import io.github.cpjust.testng_annotations.BaseTestEnvListener;
import io.github.cpjust.testng_annotations.annotations.ExcludeOnEnv;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testng.IMethodInstance;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ExcludeOnEnvListenerTest extends BaseTestEnvListener {
    public static class TestClass {
        @SuppressWarnings("java:S1186") // Empty method is intentional for test purposes
        public void noAnnotation() {}

        @SuppressWarnings("java:S1186") // Empty method is intentional for test purposes
        @ExcludeOnEnv("unmatchEnv")
        public void methodNotExcluded() {}

        @SuppressWarnings("java:S1186") // Empty method is intentional for test purposes
        @ExcludeOnEnv(MATCH_ENV)
        public void methodExcluded() {}

        @SuppressWarnings("java:S1186") // Empty method is intentional for test purposes
        @ExcludeOnEnv(value = {"dev,matchEnv"}, delimiter = ",")
        public void methodExcludedCsv() {}
    }

    @ExcludeOnEnv("unmatchEnv")
    public static class ClassWithUnmatchingExclude extends TestClass {
        // Inherits methods from TestClass
    }

    @ExcludeOnEnv(MATCH_ENV)
    public static class ClassWithMatchingExclude extends TestClass {
        // Inherits methods from TestClass
    }

    @BeforeEach
    void setUp() {
        previousEnv = System.getProperty(ENV);
        listener = new ExcludeOnEnvListener();
    }

    // region ClassWithUnmatchingExclude
    @Test
    void classLevelNotExcluded_methodNotAnnotated_isIncluded() throws NoSuchMethodException {
        List<IMethodInstance> result = setupMethodAndCallIntercept(ClassWithUnmatchingExclude.class, "noAnnotation");
        assertEquals(1, result.size(), "Method should be included when class-level ExcludeOnEnv does not match and method has no annotation");
    }

    @Test
    void classLevelNotExcluded_methodLevelNotExcluded_isIncluded() throws NoSuchMethodException {
        List<IMethodInstance> result = setupMethodAndCallIntercept(ClassWithUnmatchingExclude.class, "methodNotExcluded");
        assertEquals(1, result.size(), "Method should be included when class-level and method-level ExcludeOnEnv do not match");
    }

    @Test
    void classLevelNotExcluded_methodLevelExcluded_isExcluded() throws NoSuchMethodException {
        List<IMethodInstance> result = setupMethodAndCallIntercept(ClassWithUnmatchingExclude.class, "methodExcluded");
        assertTrue(result.isEmpty(), "Method should be excluded when method-level ExcludeOnEnv matches even if class-level does not match");
    }
    // endregion ClassWithUnmatchingExclude

    // region ClassWithMatchingExclude
    @Test
    void classLevelExcluded_methodNotAnnotated_isExcluded() throws NoSuchMethodException {
        List<IMethodInstance> result = setupMethodAndCallIntercept(ClassWithMatchingExclude.class, "noAnnotation");
        assertTrue(result.isEmpty(), "Method should be excluded when class-level ExcludeOnEnv matches and method has no annotation");
    }

    @Test
    void classLevelExcluded_methodLevelNotExcluded_isIncluded() throws NoSuchMethodException {
        List<IMethodInstance> result = setupMethodAndCallIntercept(ClassWithMatchingExclude.class, "methodNotExcluded");
        assertTrue(result.isEmpty(), "Method should be excluded when class-level ExcludeOnEnv matches and method-level ExcludeOnEnv does not match");
    }

    @Test
    void classLevelExcluded_methodLevelExcluded_isExcluded() throws NoSuchMethodException {
        List<IMethodInstance> result = setupMethodAndCallIntercept(ClassWithMatchingExclude.class, "methodExcluded");
        assertTrue(result.isEmpty(), "Method should be excluded when both class-level and method-level ExcludeOnEnv match");
    }
    // endregion ClassWithMatchingExclude

    // region TestClass with no class-level annotation
    @Test
    void classLevelNoAnnotation_methodLevelNotExcluded_isIncluded() throws NoSuchMethodException {
        List<IMethodInstance> result = setupMethodAndCallIntercept(TestClass.class, "methodNotExcluded");
        assertEquals(1, result.size(), "Method should be included when annotated with unmatching ExcludeOnEnv");
    }

    @Test
    void classLevelNoAnnotation_methodLevelExcluded_isExcluded() throws NoSuchMethodException {
        List<IMethodInstance> result = setupMethodAndCallIntercept(TestClass.class, "methodExcluded");
        assertTrue(result.isEmpty(), "Method should be excluded when annotated with matching ExcludeOnEnv");
    }

    @Test
    void classLevelNoAnnotation_methodLevelExcludedWithCsvDelimiter_isExcluded() throws NoSuchMethodException {
        List<IMethodInstance> result = setupMethodAndCallIntercept(TestClass.class, "methodExcludedCsv");
        assertTrue(result.isEmpty(), "Method should be excluded when CSV values with delimiter match the env");
    }
    // endregion TestClass with no class-level annotation
}
