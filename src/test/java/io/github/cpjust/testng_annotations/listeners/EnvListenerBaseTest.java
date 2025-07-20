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
                () -> listener.anyEnvMatches(blankPropertyName, new String[] { "env" }, ANNOTATION_NAME),
                "anyEnvMatches() should throw an IllegalArgumentException for a blank propertyName!"
        );
        assertThat("anyEnvMatches() threw the wrong assert message!", ex.getMessage(),
                equalTo(String.format("The 'propertyName' parameter of the %s annotation cannot be blank!", ANNOTATION_NAME)));
    }

    @Test
    @DisplayName("null currentEnv (property not set) returns false")
    void anyEnvMatches_missingProperty_returnsFalse() {
        when(mockProperties.getProperty("foo")).thenReturn(null);
        boolean result = listener.anyEnvMatches("foo", new String[] { "anything" }, ANNOTATION_NAME);
        assertFalse(result, "if system property is not set, should return false");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n"})
    @DisplayName("blank entry in envs array should throw IllegalArgumentException")
    void anyEnvMatches_blankEnvValue_throwsException(String blankEnv) {
        when(mockProperties.getProperty("foo")).thenReturn("bar");
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> listener.anyEnvMatches("foo", new String[] { blankEnv, "bar" }, ANNOTATION_NAME),
                "anyEnvMatches() should throw an IllegalArgumentException for blank env values!"
        );
        assertThat("anyEnvMatches() threw the wrong assert message!", ex.getMessage(),
                equalTo(String.format("The 'value' parameter of the %s annotation cannot contain blank environments!", ANNOTATION_NAME)));
    }

    @Test
    @DisplayName("empty array of envs should throw IllegalArgumentException")
    void anyEnvMatches_emptyEnvValueArray_throwsException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> listener.anyEnvMatches("foo", new String[] {}, ANNOTATION_NAME),
                "anyEnvMatches() should throw an IllegalArgumentException for an empty array of env values!"
        );
        assertThat("anyEnvMatches() threw the wrong assert message!", ex.getMessage(),
                equalTo(String.format("The 'value' parameter of the %s annotation cannot be an empty array!", ANNOTATION_NAME)));
    }

    @Test
    @DisplayName("exact match in envs returns true")
    void anyEnvMatches_exactMatch_returnsTrue() {
        when(mockProperties.getProperty("envKey")).thenReturn("PROD");
        boolean matched = listener.anyEnvMatches("envKey", new String[] { "PROD", "TEST" }, ANNOTATION_NAME);
        assertTrue(matched, "anyEnvMatches() should return true if an env is matched!");
    }

    @Test
    @DisplayName("case-insensitive and trimmed match returns true")
    void anyEnvMatches_trimmedCaseInsensitiveMatch_returnsTrue() {
        when(mockProperties.getProperty("envKey")).thenReturn("  pRoD  ");
        boolean matched = listener.anyEnvMatches("envKey", new String[] { " prod ", "staging" }, ANNOTATION_NAME);
        assertTrue(matched, "anyEnvMatches() should return true if an env is matched, even if the case is different!");
    }

    @Test
    @DisplayName("no match in envs returns false")
    void anyEnvMatches_noMatch_returnsFalse() {
        when(mockProperties.getProperty("envKey")).thenReturn("development");
        boolean matched = listener.anyEnvMatches("envKey", new String[] { "qa", "prod" }, ANNOTATION_NAME);
        assertFalse(matched, "anyEnvMatches() should return false if no env is matched!");
    }
}
