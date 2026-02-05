package io.github.cpjust.testng_annotations.listeners.annotation_transformers;

import io.github.cpjust.testng_annotations.annotations.CsvSource;
import io.github.cpjust.testng_annotations.annotations.EnumSource;
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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AllAnnotationTransformersTest extends SourceListenerTestBase {
    static final String METHODS_SHOULD_NOT_BE_EMPTY = "java:S1186"; // Suppress "Methods should not be empty" warning
    static final String CANNOT_COMBINE_MULTIPLE_SOURCE_ANNOTATIONS_ON_METHOD = "Cannot combine @CsvSource with @EnumSource or any ValueSource annotation on method";

    // Dummy test class for tests
    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Suppress "Methods should not be empty" warning
    public static class TestClass {
        @CsvSource({"1,2", "3,4"})
        public void testMethodWithOnlyCsvSource(String value) {}

        @ValueSource(strings = {"5", "6"})
        public void testMethodWithOnlyValueSource(String value) {}

        @NullAndEmptySource
        @ValueSource(strings = {"5", "6"})
        public void testMethodWithNullAndEmptySourceAndValueSource(String value) {}

        // Invalid annotation combinations for negative tests:
        @CsvSource({"1,2", "3,4"})
        @NullSource
        public void testMethodWithCsvSourceAndNullSource(String value) {}

        @CsvSource({"foo,bar"})
        @NullAndEmptySource
        public void testMethodWithCsvSourceAndNullAndEmptySource(String a) {}

        @CsvSource({"1,2", "3,4"})
        @ValueSource(strings = {"5", "6"})
        public void testMethodWithCsvSourceAndValueSource(String value) {}

        @CsvSource({"foo,bar"})
        @EnumSource(TestEnum.class)
        public void testMethodWithCsvSourceAndEnumSource(String value) {}

        @ValueSource(strings = {"5", "6"})
        @EnumSource(TestEnum.class)
        public void testMethodWithValueSourceAndEnumSource(String value) {}

    }
    
    //region Positive tests for valid annotation combinations
    static Stream<Arguments> positiveProvider() {
        return Stream.of(
                Arguments.of("testMethodWithOnlyCsvSource", CsvSourceListener.CSV_SOURCE_PROVIDER_CLASS_AND_NAME),
                Arguments.of("testMethodWithOnlyValueSource", ValueSourceListener.VALUE_SOURCE_PROVIDER_CLASS_AND_NAME),
                Arguments.of("testMethodWithNullAndEmptySourceAndValueSource", ValueSourceListener.VALUE_SOURCE_PROVIDER_CLASS_AND_NAME)
        );
    }

    @ParameterizedTest
    @MethodSource("positiveProvider")
    void transform_withValidAnnotationAndOrDataProvider_setsDataProvider(String methodName,
                                                                         Map.Entry<Class<?>, String> providerNameAndClass) throws Exception {
        AllAnnotationTransformers listener = new AllAnnotationTransformers();
        ITestAnnotation annotation = Mockito.mock(ITestAnnotation.class);
        Method method = TestClass.class.getMethod(methodName, String.class);

        listener.transform(annotation, TestClass.class, null, method);

        Mockito.verify(annotation).setDataProvider(providerNameAndClass.getValue());
        Mockito.verify(annotation).setDataProviderClass(providerNameAndClass.getKey());
    }
    //endregion Positive tests for valid annotation combinations

    //region Negative tests for invalid annotation combinations
    static Stream<Arguments> incompatibleAnnotationsProvider() {
        return Stream.of(
                Arguments.of("testMethodWithCsvSourceAndNullAndEmptySource"),
                Arguments.of("testMethodWithCsvSourceAndNullSource"),
                Arguments.of("testMethodWithCsvSourceAndValueSource"),
                Arguments.of("testMethodWithCsvSourceAndEnumSource"),
                Arguments.of("testMethodWithValueSourceAndEnumSource")
        );
    }

    @ParameterizedTest
    @MethodSource("incompatibleAnnotationsProvider")
    void transform_bothCsvSourceAndValueSourcePresent_throwsException(String methodName) throws NoSuchMethodException {
        AllAnnotationTransformers transformer = new AllAnnotationTransformers();
        ITestAnnotation mockAnnotation = Mockito.mock(ITestAnnotation.class);
        Method method = TestClass.class.getMethod(methodName, String.class);

        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> transformer.transform(mockAnnotation, TestClass.class, null, method),
                "Expected transform() to throw if both @CsvSource and ValueSource are present"
        );
        assertThat("Exception message should mention both annotations",
                thrown.getMessage(),
                containsString(CANNOT_COMBINE_MULTIPLE_SOURCE_ANNOTATIONS_ON_METHOD)
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
    @ParameterizedTest
    @MethodSource("incompatibleAnnotationsProvider")
    void throwIfTestHasMultipleDataProviders_incompatibleAnnotations_throwsException(String methodName) throws NoSuchMethodException {
        Method method = TestClass.class.getMethod(methodName, String.class);
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                SourceListenerBase.throwIfTestHasMultipleDataProviders(method),
                "throwIfTestHasMultipleDataProviders() should throw an exception when multiple source annotations are present");
        assertThat(WRONG_EXCEPTION_MESSAGE, exception.getMessage(),
                containsString(CANNOT_COMBINE_MULTIPLE_SOURCE_ANNOTATIONS_ON_METHOD));
    }

    @Test
    void throwIfTestHasMultipleDataProviders_onlyCsvSource_doesNotThrow() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("testMethodWithOnlyCsvSource", String.class);
        assertDoesNotThrow(() -> SourceListenerBase.throwIfTestHasMultipleDataProviders(method),
                "No exception should be thrown when test is annotated with only @CsvSource");
    }

    static Stream<Arguments> disallowedDataProvidersProvider() {
        return Stream.of(
                Arguments.of(CsvSourceListener.CSV_SOURCE_PROVIDER, EnumSourceListener.class),
                Arguments.of(EnumSourceListener.ENUM_SOURCE_PROVIDER, ValueSourceListener.class),
                Arguments.of(ValueSourceListener.VALUE_SOURCE_PROVIDER, CsvSourceListener.class)
        );
    }

    @ParameterizedTest
    @MethodSource("disallowedDataProvidersProvider")
    void throwIfDataProviderNotAllowed_withDisallowedDataProvider_throwsException(String dataProvider, Class<?> dataProviderClass) throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("testMethodWithOnlyCsvSource", String.class);
        ITestAnnotation annotation = Mockito.mock(ITestAnnotation.class);
        Mockito.when(annotation.getDataProvider()).thenReturn(dataProvider);
        Mockito.when(annotation.getDataProviderClass()).thenAnswer(invocation -> dataProviderClass);

        AllAnnotationTransformers transformer = new AllAnnotationTransformers();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                        transformer.throwIfDataProviderNotAllowed(annotation, method),
                "throwIfDataProviderNotAllowed() should throw an exception when a disallowed data provider is present");
        assertThat(WRONG_EXCEPTION_MESSAGE, exception.getMessage(),
                containsString("Cannot specify a dataProvider in @Test when also using @CsvSource or any ValueSource annotation on method"));
    }

    static Stream<Arguments> allowedDataProvidersProvider() {
        return Stream.of(
                Arguments.of(CsvSourceListener.CSV_SOURCE_PROVIDER, CsvSourceListener.class),
                Arguments.of(EnumSourceListener.ENUM_SOURCE_PROVIDER, EnumSourceListener.class),
                Arguments.of(ValueSourceListener.VALUE_SOURCE_PROVIDER, ValueSourceListener.class)
        );
    }

    @ParameterizedTest
    @MethodSource("allowedDataProvidersProvider")
    void throwIfDataProviderNotAllowed_withAllowedDataProvider_doesNotThrow(String dataProvider, Class<?> dataProviderClass) throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("testMethodWithOnlyCsvSource", String.class);
        ITestAnnotation annotation = Mockito.mock(ITestAnnotation.class);
        Mockito.when(annotation.getDataProvider()).thenReturn(dataProvider);
        Mockito.when(annotation.getDataProviderClass()).thenAnswer(invocation -> dataProviderClass);

        AllAnnotationTransformers transformer = new AllAnnotationTransformers();
        assertDoesNotThrow(() -> transformer.throwIfDataProviderNotAllowed(annotation, method),
                "No exception should be thrown when an allowed data provider is present");
    }
    //endregion Tests for SourceListenerBase methods
}
