package io.github.cpjust.testng_annotations.listeners;

import io.github.cpjust.testng_annotations.BaseTestEnvListener;
import io.github.cpjust.testng_annotations.annotations.IncludeOnEnv;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testng.IMethodInstance;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class IncludeOnEnvListenerTest extends BaseTestEnvListener {
    static final String METHODS_SHOULD_NOT_BE_EMPTY = "java:S1186"; // Suppress "Methods should not be empty" warning

    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Empty methods are intentional for test purposes
    public static class TestClass {
        public void noAnnotation() {}

        @IncludeOnEnv("unmatchEnv")
        public void methodNotIncluded() {}

        @IncludeOnEnv(MATCH_ENV)
        public void methodIncluded() {}

        @IncludeOnEnv(value = {"dev,matchEnv"}, delimiter = ",")
        public void methodIncludedCsv() {}
    }

    @IncludeOnEnv("unmatchEnv")
    public static class ClassWithUnmatchingInclude extends TestClass {
        // Inherits methods from TestClass
    }

    @IncludeOnEnv(MATCH_ENV)
    public static class ClassWithMatchingInclude extends TestClass {
        // Inherits methods from TestClass
    }

    @BeforeEach
    void setUp() {
        previousEnv = System.getProperty(ENV);
        listener = new IncludeOnEnvListener();
    }

    // region ClassWithUnmatchingInclude
    @Test
    void classLevelNotIncluded_methodNotAnnotated_notIncluded() throws NoSuchMethodException {
        List<IMethodInstance> result = setupMethodAndCallIntercept(ClassWithUnmatchingInclude.class, "noAnnotation");
        assertTrue(result.isEmpty(), "Method should be excluded when class-level IncludeOnEnv does not match and method has no annotation");
    }

    @Test
    void classLevelNotIncluded_methodLevelNotIncluded_notIncluded() throws NoSuchMethodException {
        List<IMethodInstance> result = setupMethodAndCallIntercept(ClassWithUnmatchingInclude.class, "methodNotIncluded");
        assertTrue(result.isEmpty(), "Method should be excluded when class-level and method-level IncludeOnEnv does not match");
    }

    @Test
    void classLevelNotIncluded_methodLevelIncluded_isIncluded() throws NoSuchMethodException {
        List<IMethodInstance> result = setupMethodAndCallIntercept(ClassWithUnmatchingInclude.class, "methodIncluded");
        assertEquals(1, result.size(), "Method-level IncludeOnEnv matching should include the test even if class-level include does not match");
    }
    // endregion ClassWithUnmatchingInclude

    // region ClassWithMatchingInclude
    @Test
    void classLevelIncluded_methodNotAnnotated_isIncluded() throws NoSuchMethodException {
        List<IMethodInstance> result = setupMethodAndCallIntercept(ClassWithMatchingInclude.class, "noAnnotation");
        assertEquals(1, result.size(), "Class-level IncludeOnEnv matching should include the test even if method-level include does not match");
    }

    @Test
    void classLevelIncluded_methodLevelNotIncluded_notIncluded() throws NoSuchMethodException {
        List<IMethodInstance> result = setupMethodAndCallIntercept(ClassWithMatchingInclude.class, "methodNotIncluded");
        assertTrue(result.isEmpty(), "Method should be excluded when class-level IncludeOnEnv matches and method-level IncludeOnEnv does not match");
    }

    @Test
    void classLevelIncluded_methodLevelIncluded_isIncluded() throws NoSuchMethodException {
        List<IMethodInstance> result = setupMethodAndCallIntercept(ClassWithMatchingInclude.class, "methodIncluded");
        assertEquals(1, result.size(), "Class-level IncludeOnEnv matching should include the test even if method-level include does not match");
    }
    // endregion ClassWithMatchingInclude

    // region TestClass with no class-level annotation
    @Test
    void classLevelNoAnnotation_methodLevelNotIncluded_notIncluded() throws NoSuchMethodException {
        List<IMethodInstance> result = setupMethodAndCallIntercept(TestClass.class, "methodNotIncluded");
        assertTrue(result.isEmpty(), "Method annotated with unmatching IncludeOnEnv should not be included");
    }

    @Test
    void classLevelNoAnnotation_methodLevelIncluded_isIncluded() throws NoSuchMethodException {
        List<IMethodInstance> result = setupMethodAndCallIntercept(TestClass.class, "methodIncluded");
        assertEquals(1, result.size(), "Method annotated with matching IncludeOnEnv should be included");
    }

    @Test
    void classLevelNoAnnotation_methodLevelIncludedWithCsvDelimiter_isIncluded() throws NoSuchMethodException {
        List<IMethodInstance> result = setupMethodAndCallIntercept(TestClass.class, "methodIncludedCsv");
        assertEquals(1, result.size(), "CSV values with delimiter should be split and treated as separate envs");
    }
    // endregion TestClass with no class-level annotation
}
