package io.github.cpjust.testng_annotations.listeners;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Properties;

/**
 * Base class containing common code for *EnvListener classes.
 */
@Slf4j
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public abstract class EnvListenerBase {
    final Properties systemProperties;

    /**
     * Constructor.
     */
    public EnvListenerBase() {
        this.systemProperties = System.getProperties();
    }

    /**
     * Checks if an environment name (defined by a system property) matches any of the specified environments.
     *
     * @param propertyName   The system property containing the current environment name.
     * @param envs           The array of environments to compare against.
     * @param annotationName The name of the annotation.
     * @return True if the current environment matches any of the specified environments.
     */
    boolean anyEnvMatches(@NonNull String propertyName, @NonNull String[] envs, @NonNull String annotationName) {
        if (propertyName.isBlank()) {
            throw new IllegalArgumentException(String.format("The 'propertyName' parameter of the %s annotation cannot be blank!", annotationName));
        }

        if (envs.length == 0) {
            throw new IllegalArgumentException(String.format("The 'value' parameter of the %s annotation cannot be an empty array!", annotationName));
        }

        String currentEnv = systemProperties.getProperty(propertyName);

        if (currentEnv == null) {
            log.warn("Environment property '{}' is not set", propertyName);
            return false;
        }

        // Case-insensitive comparison and trim whitespace
        return Arrays.stream(envs)
                .peek(env -> {
                    if (env.isBlank()) {
                        throw new IllegalArgumentException(
                                String.format("The 'value' parameter of the %s annotation cannot contain blank environments!", annotationName));
                    }
                })
                .anyMatch(env -> env.trim().equalsIgnoreCase(currentEnv.trim()));
    }
}
