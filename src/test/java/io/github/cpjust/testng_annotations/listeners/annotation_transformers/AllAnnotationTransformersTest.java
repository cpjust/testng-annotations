package io.github.cpjust.testng_annotations.listeners.annotation_transformers;

import io.github.cpjust.testng_annotations.annotations.CsvSource;
import io.github.cpjust.testng_annotations.annotations.NullAndEmptySource;
import io.github.cpjust.testng_annotations.annotations.NullSource;
import io.github.cpjust.testng_annotations.annotations.ValueSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AllAnnotationTransformersTest {
    static final String METHODS_SHOULD_NOT_BE_EMPTY = "java:S1186"; // Suppress "Methods should not be empty" warning

    // Dummy test class for mutual exclusion
    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Suppress "Methods should not be empty" warning
    public static class ErrorCases {
        @CsvSource({"foo,bar"})
        @NullAndEmptySource
        public void testWithBothCsvSourceAndNullAndEmptySourceAnnotations(String a) {}

        @CsvSource({"foo,bar"})
        @NullSource
        public void testWithBothCsvSourceAndNullSourceAnnotations(String a) {}

        @CsvSource({"foo,bar"})
        @ValueSource(strings = {"baz"})
        public void testWithBothCsvSourceAndValueSourceAnnotations(String a) {}
    }

    static Stream<Arguments> errorProvider() {
        return Stream.of(
                Arguments.of("testWithBothCsvSourceAndNullAndEmptySourceAnnotations"),
                Arguments.of("testWithBothCsvSourceAndNullSourceAnnotations"),
                Arguments.of("testWithBothCsvSourceAndValueSourceAnnotations")
        );
    }

    @ParameterizedTest
    @MethodSource("errorProvider")
    void transform_bothCsvSourceAndValueSourcePresent_throwsException(String methodName) throws NoSuchMethodException {
        AllAnnotationTransformers transformer = new AllAnnotationTransformers();
        ITestAnnotation mockAnnotation = Mockito.mock(ITestAnnotation.class);
        Method method = ErrorCases.class.getMethod(methodName, String.class);

        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> transformer.transform(mockAnnotation, ErrorCases.class, null, method),
                "Expected transform() to throw if both @CsvSource and ValueSource are present"
        );
        assertThat("Exception message should mention both annotations",
                thrown.getMessage(),
                containsString("Cannot combine @CsvSource with any ValueSource annotation")
        );
    }
}
