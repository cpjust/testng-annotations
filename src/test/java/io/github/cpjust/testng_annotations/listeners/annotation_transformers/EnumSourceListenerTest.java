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

        @EnumSource(value = TestEnum.class, names = {"VALUE_ONE"}, mode = EnumSource.Mode.EXCLUDE)
        public void excludeMode(TestEnum testEnum) {}

        @EnumSource(value = TestEnum.class, names = {"^VALUE_O[A-Z]{2}$", "^VALUE_T[A-Z]{2}$"}, mode = EnumSource.Mode.MATCH_ANY)
        public void matchAnyMode(TestEnum testEnum) {}

        @EnumSource(value = TestEnum.class, names = {"VALUE_.*", ".*_THREE"}, mode = EnumSource.Mode.MATCH_ALL)
        public void matchAllMode(TestEnum testEnum) {}
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

    @Test
    void enumSourceProvider_excludeMode_excludesSpecifiedValue() throws Exception {
        // Arrange
        Method testMethod = PositiveCases.class.getMethod("excludeMode", TestEnum.class);

        // Act
        Object[][] result = EnumSourceListener.enumSourceProvider(testMethod);

        // Assert
        assertThat("EnumSourceListener.exclude mode did not exclude the specified value",
                Arrays.stream(result).map(arr -> arr[0]).collect(Collectors.toList()),
                contains(TestEnum.VALUE_TWO, TestEnum.VALUE_THREE));
    }

    @Test
    void enumSourceProvider_matchAnyMode_matchesRegex() throws Exception {
        // Arrange
        Method testMethod = PositiveCases.class.getMethod("matchAnyMode", TestEnum.class);

        // Act
        Object[][] result = EnumSourceListener.enumSourceProvider(testMethod);

        // Assert
        assertThat("EnumSourceListener.matchAny did not match expected values",
                Arrays.stream(result).map(arr -> arr[0]).collect(Collectors.toList()),
                contains(TestEnum.VALUE_ONE, TestEnum.VALUE_TWO));
    }

    @Test
    void enumSourceProvider_matchAllMode_matchesAllRegexes() throws Exception {
        // Arrange
        Method testMethod = PositiveCases.class.getMethod("matchAllMode", TestEnum.class);

        // Act
        Object[][] result = EnumSourceListener.enumSourceProvider(testMethod);

        // Assert
        assertThat("EnumSourceListener.matchAll did not match expected values",
                Arrays.stream(result).map(arr -> arr[0]).collect(Collectors.toList()),
                contains(TestEnum.VALUE_THREE));
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

    // Additional negative tests added to cover strict-typing behavior and empty enum edge case
    @SuppressWarnings(METHODS_SHOULD_NOT_BE_EMPTY) // Empty methods are intentional for test purposes
    public static class AdditionalNegativeCases {
        public enum EmptyEnum {}

        @EnumSource(EmptyEnum.class)
        public void emptyEnumCase(EmptyEnum value) {}

        @EnumSource(TestEnum.class)
        public void paramAsEnum(java.lang.Enum<?> value) {}

        @EnumSource(value = TestEnum.class, names = {"*INVALID["}, mode = EnumSource.Mode.MATCH_ANY)
        public void invalidRegex(TestEnum value) {}
    }

    @Test
    void enumSourceProvider_emptyEnum_throwsException() throws Exception {
        // Arrange
        Method testMethod = AdditionalNegativeCases.class.getMethod("emptyEnumCase", AdditionalNegativeCases.EmptyEnum.class);

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                EnumSourceListener.enumSourceProvider(testMethod),
                "enumSourceProvider() should throw an IllegalStateException when the enum has no constants.");
        assertEquals("No matching enum constants found for method: emptyEnumCase", ex.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    void enumSourceProvider_paramAsEnum_supertypeNotAllowed_throwsException() throws Exception {
        // Arrange
        Method testMethod = AdditionalNegativeCases.class.getMethod("paramAsEnum", java.lang.Enum.class);

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                EnumSourceListener.enumSourceProvider(testMethod),
                "enumSourceProvider() should throw when method parameter type is a supertype (Enum) under strict typing policy.");
        assertEquals("Enum class TestEnum is not compatible with parameter type Enum in method: paramAsEnum",
                ex.getMessage(), WRONG_EXCEPTION_MESSAGE);
    }

    @Test
    void enumSourceProvider_invalidRegex_throwsIllegalArgumentException() throws Exception {
        // Arrange
        Method testMethod = AdditionalNegativeCases.class.getMethod("invalidRegex", TestEnum.class);

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                EnumSourceListener.enumSourceProvider(testMethod),
                "enumSourceProvider() should throw an IllegalArgumentException when an invalid regex is provided in names().");
        assertThat("Exception message should mention invalid regular expression",
                ex.getMessage(), containsString("Invalid regular expression in EnumSource.names()"));
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
