package io.github.cpjust.testng_annotations.listeners;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnvListenerBaseNullAndTrimTest {
    private static final String ANNOTATION_NAME = "Foo";
    private static final String ENV = "env";

    @Mock
    Properties mockProperties;

    private EnvListenerBase listener;

    @BeforeEach
    void setUp() {
        listener = new EnvListenerBase(mockProperties) {};
    }

    @Test
    @DisplayName("anyEnvMatches should throw NPE when null parameters are passed")
    @SuppressWarnings("ConstantConditions") // Intentionally passing null to verify NPE behavior
    void anyEnvMatches_nullParams_throwNPE() {
        assertThrows(NullPointerException.class, () -> listener.anyEnvMatches(null, new String[] {ENV}, ANNOTATION_NAME, ""));
        assertThrows(NullPointerException.class, () -> listener.anyEnvMatches(ENV, null, ANNOTATION_NAME, ""));
        assertThrows(NullPointerException.class, () -> listener.anyEnvMatches(ENV, new String[] {ENV}, null, ""));
        assertThrows(NullPointerException.class, () -> listener.anyEnvMatches(ENV, new String[] {ENV}, ANNOTATION_NAME, null));
    }

    @Test
    @DisplayName("splitAndValidateEnvs should throw NPE when null array is passed")
    @SuppressWarnings("ConstantConditions") // Intentionally passing null to verify NPE behavior
    void splitAndValidateEnvs_nullArray_throwsNPE() {
        assertThrows(NullPointerException.class, () -> listener.splitAndValidateEnvs(null, ANNOTATION_NAME, ""));
        assertThrows(NullPointerException.class, () -> listener.splitAndValidateEnvs(new String[] { "a" }, null, ""));
        assertThrows(NullPointerException.class, () -> listener.splitAndValidateEnvs(new String[] { "a" }, ANNOTATION_NAME, null));
    }

    @Test
    @DisplayName("anyEnvMatches should trim current environment value before comparing")
    void anyEnvMatches_currentEnvTrimmed_matches() {
        when(mockProperties.getProperty(ENV)).thenReturn(" prod ");
        boolean result = listener.anyEnvMatches(ENV, new String[] { "prod" }, ANNOTATION_NAME, "");
        assertTrue(result, "anyEnvMatches should trim the current environment value before comparison");
    }

    @Test
    @DisplayName("anyEnvMatches should be able to use space as a delimiter")
    void anyEnvMatches_spaceDelimiter_matches() {
        when(mockProperties.getProperty(ENV)).thenReturn("prod");
        boolean result = listener.anyEnvMatches(ENV, new String[] { "dev prod" }, ANNOTATION_NAME, " ");
        assertTrue(result, "anyEnvMatches should be able to use space as a delimiter");
    }

    @Test
    @DisplayName("splitAndValidateEnvs should treat regex-special delimiters as literals (Pattern.quote)")
    void splitAndValidateEnvs_regexDelimiter_isQuoted() {
        String[] input = { "a.b" };
        String[] result = listener.splitAndValidateEnvs(input, ANNOTATION_NAME, ".");
        assertArrayEquals(new String[] { "a", "b" }, result, "Delimiter '.' should be treated literally and split on '.'");
    }
}
