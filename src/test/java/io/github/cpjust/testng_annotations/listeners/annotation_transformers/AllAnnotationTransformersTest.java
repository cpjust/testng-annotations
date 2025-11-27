package io.github.cpjust.testng_annotations.listeners.annotation_transformers;

import io.github.cpjust.testng_annotations.annotations.CsvSource;
import io.github.cpjust.testng_annotations.annotations.NullAndEmptySource;
import io.github.cpjust.testng_annotations.annotations.NullSource;
import io.github.cpjust.testng_annotations.annotations.ValueSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AllAnnotationTransformersTest extends SourceListenerTestBase {
    static final String METHODS_SHOULD_NOT_BE_EMPTY = "java:S1186"; // Suppress "Methods should not be empty" warning

    //region Positive tests for valid annotation combinations
    // Dummy test class for valid annotation combinations
    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY)
    public static class PositiveCases {
        @CsvSource({"foo,bar"})
        public void testCsvSource_noException(String value) {}

        @ValueSource(ints = {1, 2, 3})
        public void testValueSource_noException(int value) {}

        public void testNoCsvSourceAndValidDataProviderNameAndClass_noException(String value) {}

        @CsvSource({"foo,bar"})
        public void testCsvSourceAndValidDataProviderNameAndClass_noException(String value) {}

        @ValueSource(ints = {1, 2, 3})
        public void testValueSourceAndValidDataProviderNameAndClass_throwsException(int value) {}

        @NullAndEmptySource
        @ValueSource(ints = {1, 2, 3})
        public void testNullAndEmptySourceValueSourceAndValidDataProviderNameAndClass_throwsException(int value) {}
    }

    static Stream<Arguments> positiveProvider() {
        return Stream.of(
                Arguments.of("testCsvSource_noException", String.class, CsvSourceListener.CSV_SOURCE_PROVIDER_CLASS_AND_NAME),
                Arguments.of("testValueSource_noException", int.class, ValueSourceListener.VALUE_SOURCE_PROVIDER_CLASS_AND_NAME),
                Arguments.of("testCsvSourceAndValidDataProviderNameAndClass_noException", String.class, CsvSourceListener.CSV_SOURCE_PROVIDER_CLASS_AND_NAME),
                Arguments.of("testValueSourceAndValidDataProviderNameAndClass_throwsException", int.class, ValueSourceListener.VALUE_SOURCE_PROVIDER_CLASS_AND_NAME),
                Arguments.of("testNullAndEmptySourceValueSourceAndValidDataProviderNameAndClass_throwsException", int.class, ValueSourceListener.VALUE_SOURCE_PROVIDER_CLASS_AND_NAME)
        );
    }

    @ParameterizedTest
    @MethodSource("positiveProvider")
    void transform_withValidAnnotationAndOrDataProvider_setsDataProvider(String methodName, Class<?> paramType,
                                                                         Map.Entry<Class<?>, String> providerNameAndClass) throws Exception {
        AllAnnotationTransformers listener = new AllAnnotationTransformers();
        ITestAnnotation annotation = Mockito.mock(ITestAnnotation.class);
        Method method = PositiveCases.class.getMethod(methodName, paramType);

        listener.transform(annotation, PositiveCases.class, null, method);

        Mockito.verify(annotation).setDataProvider(providerNameAndClass.getValue());
        Mockito.verify(annotation).setDataProviderClass(providerNameAndClass.getKey());
    }
    //endregion Positive tests for valid annotation combinations

    //region Negative tests for invalid annotation combinations
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
    //endregion Negative tests for invalid annotation combinations

    //region Negative tests for conflicting data providers
    static Stream<Arguments> dataProviderConflictProvider() {
        return Stream.of(
                Arguments.of("testValueSourceAndDataProvider_throwsException", int.class),
                Arguments.of("testCsvSourceAndDataProvider_throwsException", String.class),
                Arguments.of("testValueSourceAndValidDataProviderNameButWrongClass_throwsException", int.class),
                Arguments.of("testCsvSourceAndValidDataProviderNameButNoClass_throwsException", String.class)
        );
    }

    @ParameterizedTest
    @MethodSource("dataProviderConflictProvider")
    void transform_dataProviderAndSourceAnnotationPresent_throwsException(String methodName, Class<?> paramType) throws NoSuchMethodException {
        IllegalStateException thrown = transform_dataProviderAndSourceAnnotationPresent_throwsException(methodName, paramType, new AllAnnotationTransformers());

        assertThat(EXCEPTION_MESSAGE_SHOULD_MENTION_DATA_PROVIDER_CONFLICT,
                thrown.getMessage(),
                containsString(CANNOT_SPECIFY_A_DATA_PROVIDER_IN_TEST_WHEN_ALSO_USING_CSV_SOURCE_OR_ANY_VALUE_SOURCE_ANNOTATION));

    }
    //endregion Negative tests for conflicting data providers

    //region Tests for SourceListenerBase methods
    // Dummy test class for tests
    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Suppress "Methods should not be empty" warning
    public static class TestClass {
        @CsvSource({"1,2", "3,4"})
        public void testMethodWithOnlyCsvSource() {
        }

        @CsvSource({"1,2", "3,4"})
        @ValueSource(ints = {1, 2, 3})
        public void testMethodWithCsvAndValueSource() {
        }

        @CsvSource({"1,2", "3,4"})
        @NullSource
        public void testMethodWithNullAndCsvSource() {
        }
    }

    @Test
    void throwIfTestHasMultipleDataProviders_csvAndValueSource_throwsException() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("testMethodWithCsvAndValueSource");
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                SourceListenerBase.throwIfTestHasMultipleDataProviders(method));
        assertThat(exception.getMessage(), containsString("Cannot combine @CsvSource with any ValueSource annotation"));
    }

    @Test
    void throwIfTestHasMultipleDataProviders_nullSourceAndCsvSource_throwsException() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("testMethodWithNullAndCsvSource");
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                SourceListenerBase.throwIfTestHasMultipleDataProviders(method));
        assertThat(exception.getMessage(), containsString("Cannot combine @CsvSource with any ValueSource annotation"));
    }

    @Test
    void throwIfTestHasMultipleDataProviders_onlyCsvSource_doesNotThrow() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("testMethodWithOnlyCsvSource");
        SourceListenerBase.throwIfTestHasMultipleDataProviders(method); // Should not throw
    }

    @Test
    void throwIfDataProviderNotAllowed_withDisallowedDataProvider_throwsException() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("testMethodWithOnlyCsvSource");
        ITestAnnotation annotation = Mockito.mock(ITestAnnotation.class);
        Mockito.when(annotation.getDataProvider()).thenReturn("disallowedProvider");

        AllAnnotationTransformers transformer = new AllAnnotationTransformers();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                transformer.throwIfDataProviderNotAllowed(annotation, method));
        assertThat(exception.getMessage(), containsString("Cannot specify a dataProvider in @Test when also using @CsvSource"));
    }

    @Test
    void throwIfDataProviderNotAllowed_withAllowedDataProvider_doesNotThrow() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("testMethodWithOnlyCsvSource");
        ITestAnnotation annotation = Mockito.mock(ITestAnnotation.class);
        Mockito.when(annotation.getDataProvider()).thenReturn(CsvSourceListener.CSV_SOURCE_PROVIDER);
        Mockito.when(annotation.getDataProviderClass()).thenAnswer(invocation -> CsvSourceListener.class);

        AllAnnotationTransformers transformer = new AllAnnotationTransformers();
        transformer.throwIfDataProviderNotAllowed(annotation, method); // Should not throw
    }
    //endregion Tests for SourceListenerBase methods
}
