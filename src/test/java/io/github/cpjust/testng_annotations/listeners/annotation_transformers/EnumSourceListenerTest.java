package io.github.cpjust.testng_annotations.listeners.annotation_transformers;

import io.github.cpjust.testng_annotations.annotations.EnumSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link EnumSourceListener}.
 */
class EnumSourceListenerTest extends SourceListenerTestBase {
    // region Positive test cases
    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Empty method is intentional for test purposes
    public static class PositiveCases {
        @EnumSource(TestEnum.class)
        public void allConstants(TestEnum testEnum) {}

        @EnumSource(value = TestEnum.class, names = {"VALUE_ONE", "VALUE_TWO"})
        public void specificConstants(TestEnum testEnum) {}
    }

    @Test
    void enumSourceProvider_allConstantsProvided_enumValuesSupplied() throws Exception {
        // Arrange
        Method testMethod = PositiveCases.class.getMethod("allConstants", TestEnum.class);

        // Act
        Object[][] result = EnumSourceListener.enumSourceProvider(testMethod);

        // Assert
        assertThat("EnumSourceListener.enumSourceProvider() returned the wrong enum values!",
                Arrays.stream(result).map(arr -> arr[0]).collect(Collectors.toList()),
                contains(TestEnum.VALUE_ONE, TestEnum.VALUE_TWO, TestEnum.VALUE_THREE));
    }

    @Test
    void enumSourceProvider_specificConstantsProvided_specificEnumValuesSupplied() throws Exception {
        // Arrange
        Method testMethod = PositiveCases.class.getMethod("specificConstants", TestEnum.class);

        // Act
        Object[][] result = EnumSourceListener.enumSourceProvider(testMethod);

        // Assert
        assertThat("EnumSourceListener.enumSourceProvider() returned the wrong enum values!",
                Arrays.stream(result).map(arr -> arr[0]).collect(Collectors.toList()),
                contains(TestEnum.VALUE_ONE, TestEnum.VALUE_TWO));
    }
    // endregion Positive test cases

    // region Negative test cases
    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Empty method is intentional for test purposes
    public static class NegativeCases {
        public enum WrongEnum {
            A, B, C
        }

        @EnumSource(value = TestEnum.class, names = {"NON_EXISTENT"})
        public void noMatchingConstants(TestEnum testEnum) {}

        @EnumSource(value = WrongEnum.class)
        public void wrongEnum(TestEnum testEnum) {}

        @EnumSource(value = WrongEnum.class)
        public void noParams() {}

        @EnumSource(value = WrongEnum.class)
        public void tooManyParams(TestEnum testEnum1, TestEnum testEnum2) {}
    }

    @Test
    void enumSourceProvider_noMatchingConstants_throwsException() throws Exception {
        // Arrange
        Method testMethod = NegativeCases.class.getMethod("noMatchingConstants", TestEnum.class);

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                EnumSourceListener.enumSourceProvider(testMethod),
                "enumSourceProvider() should throw an IllegalStateException when no matching enum constants are found.");
        assertEquals("No matching enum constants found for method: noMatchingConstants", ex.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    void enumSourceProvider_wrongEnum_throwsException() throws Exception {
        // Arrange
        Method testMethod = NegativeCases.class.getMethod("wrongEnum", TestEnum.class);

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                EnumSourceListener.enumSourceProvider(testMethod),
                "enumSourceProvider() should throw an IllegalStateException when enum class is not compatible with method parameter type.");
        assertEquals("Enum class WrongEnum is not compatible with parameter type TestEnum in method: wrongEnum",
                ex.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    void enumSourceProvider_noParams_throwsException() throws Exception {
        // Arrange
        Method testMethod = NegativeCases.class.getMethod("noParams");

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                EnumSourceListener.enumSourceProvider(testMethod),
                "enumSourceProvider() should throw an IllegalStateException when method has no parameters.");
        assertEquals("Method 'noParams' should have 1 parameter, but it has 0", ex.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    void enumSourceProvider_tooManyParams_throwsException() throws Exception {
        // Arrange
        Method testMethod = NegativeCases.class.getMethod("tooManyParams", TestEnum.class, TestEnum.class);

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                EnumSourceListener.enumSourceProvider(testMethod),
                "enumSourceProvider() should throw an IllegalStateException when method has more than one parameter.");
        assertEquals("Method 'tooManyParams' should have 1 parameter, but it has 2", ex.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }
    // region Negative test cases

    //region Negative tests for conflicting data providers
    static Stream<Arguments> dataProviderConflictProvider() {
        return Stream.of(
                Arguments.of("testEnumSourceAndDataProvider_throwsException", TestEnum.class),
                Arguments.of("testEnumSourceAndValidDataProviderClassButNoName_throwsException", TestEnum.class)
        );
    }

    @ParameterizedTest
    @MethodSource("dataProviderConflictProvider")
    void transform_dataProviderAndSourceAnnotationPresent_throwsException(String methodName, Class<?> paramType) throws NoSuchMethodException {
        IllegalStateException ex = transform_dataProviderAndSourceAnnotationPresent_throwsException(methodName, paramType, new EnumSourceListener());

        assertThat(EXCEPTION_MESSAGE_SHOULD_MENTION_DATA_PROVIDER_CONFLICT,
                ex.getMessage(),
                containsString(CANNOT_SPECIFY_A_DATA_PROVIDER_IN_TEST_WHEN_ALSO_USING_CSV_SOURCE_OR_ANY_VALUE_SOURCE_ANNOTATION));
    }
    //endregion Negative tests for conflicting data providers
}
