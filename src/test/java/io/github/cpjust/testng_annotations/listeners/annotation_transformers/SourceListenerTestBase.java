package io.github.cpjust.testng_annotations.listeners.annotation_transformers;

import io.github.cpjust.testng_annotations.annotations.CsvSource;
import io.github.cpjust.testng_annotations.annotations.ValueSource;
import lombok.NonNull;
import org.mockito.Mockito;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class SourceListenerTestBase {
    static final String METHODS_SHOULD_NOT_BE_EMPTY = "java:S1186"; // Suppress "Methods should not be empty" warning
    static final String EXCEPTION_MESSAGE_SHOULD_MENTION_DATA_PROVIDER_CONFLICT = "Exception message should mention dataProvider conflict";
    static final String CANNOT_SPECIFY_A_DATA_PROVIDER_IN_TEST_WHEN_ALSO_USING_CSV_SOURCE_OR_ANY_VALUE_SOURCE_ANNOTATION = "Cannot specify a dataProvider in @Test when also using @CsvSource or any ValueSource annotation";

    // Dummy test class for data provider conflicts
    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY)
    public static class ConflictCases {
        @ValueSource(ints = {1, 2, 3})
        @org.testng.annotations.Test(dataProvider = "nonExistingProvider")
        public void testValueSourceAndDataProvider_throwsException(int value) {}

        @CsvSource({"foo,bar"})
        @org.testng.annotations.Test(dataProvider = "nonExistingProvider")
        public void testCsvSourceAndDataProvider_throwsException(String value) {}

        @ValueSource(ints = {1, 2, 3})
        @org.testng.annotations.Test(dataProvider = ValueSourceListener.VALUE_SOURCE_PROVIDER, dataProviderClass = CsvSourceListener.class)
        public void testValueSourceAndValidDataProviderNameButWrongClass_throwsException(int value) {}

        @CsvSource({"foo,bar"})
        @org.testng.annotations.Test(dataProvider = CsvSourceListener.CSV_SOURCE_PROVIDER)
        public void testCsvSourceAndValidDataProviderNameButNoClass_throwsException(String value) {}
    }

    /**
     * Helper method to test that the transformer throws an exception when both dataProvider and ValueSource/CsvSource are present.
     *
     * @param methodName  The name of the method to test.
     * @param paramType   The parameter type of the method.
     * @param transformer The annotation transformer to test.
     * @return The thrown IllegalStateException.
     * @throws NoSuchMethodException If the method does not exist.
     */
    protected IllegalStateException transform_dataProviderAndSourceAnnotationPresent_throwsException(
            @NonNull String methodName, @NonNull Class<?> paramType, @NonNull IAnnotationTransformer transformer) throws NoSuchMethodException {
        ITestAnnotation mockAnnotation = Mockito.mock(ITestAnnotation.class);

        Method method = ConflictCases.class.getMethod(methodName, paramType);
        // Read the real Test annotation from the Method
        org.testng.annotations.Test testAnnotation = method.getAnnotation(org.testng.annotations.Test.class);
        String dataProviderName = (testAnnotation == null) ? null : testAnnotation.dataProvider();

        // Extract the dataProviderClass from the annotation and normalize common defaults to null
        Class<?> dataProviderClassFromAnnotation = null;

        if (testAnnotation != null) {
            Class<?> dpClass = testAnnotation.dataProviderClass();

            if ((dpClass != null) && (dpClass != Object.class) && (dpClass != void.class)) {
                dataProviderClassFromAnnotation = dpClass;
            }
        }

        // Configure the mock ITestAnnotation from the real @Test annotation values
        Mockito.doReturn(dataProviderName).when(mockAnnotation).getDataProvider();
        // Use doReturn to avoid type-inference/null ambiguity
        Mockito.doReturn(dataProviderClassFromAnnotation).when(mockAnnotation).getDataProviderClass();

        return assertThrows(
                IllegalStateException.class,
                () -> transformer.transform(mockAnnotation, ConflictCases.class, null, method),
                "Expected transform() to throw if both dataProvider and ValueSource/CsvSource are present"
        );
    }
}
