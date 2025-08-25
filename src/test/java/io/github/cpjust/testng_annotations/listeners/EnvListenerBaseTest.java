package io.github.cpjust.testng_annotations.listeners;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

// NOTE: These tests need to be unit tests and use Mockito because they throw exceptions that would cause TestNG to
// not run any tests.
@ExtendWith(MockitoExtension.class)
class EnvListenerBaseTest {
    private static final String ANNOTATION_NAME = "Foo";

    @Mock
    Properties mockProperties;

    private EnvListenerBase listener;

    @BeforeEach
    void setUp() {
        // Anonymous concrete subclass so we can call anyEnvMatches().
        listener = new EnvListenerBase(mockProperties) {};
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n"})
    @DisplayName("blank propertyName should throw IllegalArgumentException")
    void anyEnvMatches_blankPropertyName_throwsException(String blankPropertyName) {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> listener.anyEnvMatches(blankPropertyName, new String[] { "env" }, ANNOTATION_NAME, ""),
                "anyEnvMatches() should throw an IllegalArgumentException for a blank propertyName!"
        );
        assertThat("anyEnvMatches() threw the wrong assert message!", ex.getMessage(),
                equalTo(String.format("The 'propertyName' parameter of the %s annotation cannot be blank!", ANNOTATION_NAME)));
    }

    @Test
    @DisplayName("null currentEnv (property not set) returns false")
    void anyEnvMatches_missingProperty_returnsFalse() {
        when(mockProperties.getProperty("foo")).thenReturn(null);
        boolean result = listener.anyEnvMatches("foo", new String[] { "anything" }, ANNOTATION_NAME, "");
        assertFalse(result, "if system property is not set, should return false");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n"})
    @DisplayName("blank entry in envs array should throw IllegalArgumentException")
    void anyEnvMatches_blankEnvValue_throwsException(String blankEnv) {
        when(mockProperties.getProperty("foo")).thenReturn("bar");
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> listener.anyEnvMatches("foo", new String[] { blankEnv, "bar" }, ANNOTATION_NAME, ""),
                "anyEnvMatches() should throw an IllegalArgumentException for blank env values!"
        );
        assertThat("anyEnvMatches() threw the wrong assert message!", ex.getMessage(),
                equalTo(String.format("The 'value' parameter of the %s annotation cannot be null or blank!", ANNOTATION_NAME)));
    }

    @Test
    @DisplayName("empty array of envs should throw IllegalArgumentException")
    void anyEnvMatches_emptyEnvValueArray_throwsException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> listener.anyEnvMatches("foo", new String[] {}, ANNOTATION_NAME, ""),
                "anyEnvMatches() should throw an IllegalArgumentException for an empty array of env values!"
        );
        assertThat("anyEnvMatches() threw the wrong assert message!", ex.getMessage(),
                equalTo(String.format("The 'value' parameter of the %s annotation cannot be an empty array!", ANNOTATION_NAME)));
    }

    // --- New tests for CSV support ---

    @Test
    @DisplayName("CSV value with default delimiter matches current environment")
    void anyEnvMatches_csvWithDelimiter_returnsTrue() {
        when(mockProperties.getProperty("env")).thenReturn("prod");
        boolean result = listener.anyEnvMatches("env", new String[] { "dev,stage,prod" }, ANNOTATION_NAME, ",");
        assertTrue(result, "CSV string with default delimiter should match current environment");
    }

    @Test
    @DisplayName("CSV value with custom delimiter matches current environment")
    void anyEnvMatches_arrayOfCsvWithDelimiter_returnsTrue() {
        when(mockProperties.getProperty("env")).thenReturn("qa");
        boolean result = listener.anyEnvMatches("env", new String[] { "dev|stage", "qa|prod" }, ANNOTATION_NAME, "|");
        assertTrue(result, "CSV string with custom delimiter should match current environment");
    }

    @Test
    @DisplayName("CSV value with no match returns false")
    void anyEnvMatches_csvNoMatch_returnsFalse() {
        when(mockProperties.getProperty("env")).thenReturn("uat");
        boolean result = listener.anyEnvMatches("env", new String[] { "dev,stage,prod" }, ANNOTATION_NAME, ",");
        assertFalse(result, "CSV string with no match should return false");
    }

    @Test
    @DisplayName("CSV value with blank or empty string throws exception")
    void anyEnvMatches_csvBlankValue_throwsException() {
        when(mockProperties.getProperty("env")).thenReturn("qa");
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> listener.anyEnvMatches("env", new String[] { "   " }, ANNOTATION_NAME, ","),
                "anyEnvMatches() should throw an IllegalArgumentException for blank CSV value!"
        );
        assertThat("anyEnvMatches() threw the wrong assert message!", ex.getMessage(),
                equalTo(String.format("The 'value' parameter of the %s annotation cannot be null or blank!", ANNOTATION_NAME)));
    }

    @Test
    @DisplayName("non-CSV value with a delimiter passed returns true if env matches")
    void anyEnvMatches_nonCsvValueWithDelimiter_returnsTrue() {
        when(mockProperties.getProperty("env")).thenReturn("qa");
        boolean result = listener.anyEnvMatches("env", new String[] { "qa" }, ANNOTATION_NAME, ",");
        assertTrue(result, "anyEnvMatches() should return true for non-CSV value when a delimiter is passed and env matches");
    }

    // --- Tests for splitAndValidateEnvs() ---

    @Test
    @DisplayName("splitAndValidateEnvs with no delimiter returns trimmed, deduped values")
    void splitAndValidateEnvs_noDelimiter_returnsTrimmedDeduped() {
        String[] input = { "dev", " stage ", "prod", "dev" };
        String[] result = listener.splitAndValidateEnvs(input, ANNOTATION_NAME, "");
        assertArrayEquals(
                new String[] { "dev", "stage", "prod" },
                result,
                "splitAndValidateEnvs should trim and duplicate values when no delimiter is provided"
        );
    }

    @Test
    @DisplayName("splitAndValidateEnvs with delimiter splits and trims values")
    void splitAndValidateEnvs_withDelimiter_splitsAndTrims() {
        String[] input = { "dev, stage", "prod ,qa" };
        String[] result = listener.splitAndValidateEnvs(input, ANNOTATION_NAME, ",");
        assertArrayEquals(
                new String[] { "dev", "stage", "prod", "qa" },
                result,
                "splitAndValidateEnvs should split, trim, and duplicate values with delimiter"
        );
    }

    @Test
    @DisplayName("splitAndValidateEnvs removes duplicates after splitting")
    void splitAndValidateEnvs_removesDuplicates() {
        String[] input = { "dev,dev,stage", "stage,prod" };
        String[] result = listener.splitAndValidateEnvs(input, ANNOTATION_NAME, ",");
        assertArrayEquals(
                new String[] { "dev", "stage", "prod" },
                result,
                "splitAndValidateEnvs should remove duplicates after splitting"
        );
    }

    @ParameterizedTest
    @ValueSource(strings = { "dev,,prod", " " })
    @DisplayName("splitAndValidateEnvs throws on blank or null env after splitting")
    void splitAndValidateEnvs_throwsOnBlankOrNull(String inputString) {
        String[] input = { inputString };
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> listener.splitAndValidateEnvs(input, ANNOTATION_NAME, ","),
                "splitAndValidateEnvs should throw for blank or null env"
        );
        assertThat(
                "splitAndValidateEnvs threw the wrong assert message!",
                ex.getMessage(),
                equalTo(String.format("The 'value' parameter of the %s annotation cannot be null or blank!", ANNOTATION_NAME))
        );
    }
}
