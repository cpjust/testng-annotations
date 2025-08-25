package io.github.cpjust.testng_annotations.listeners;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;

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
     * @param delimiter      The CSV string delimiter to use for splitting values. If empty, values are used as-is.
     * @return True if the current environment matches any of the specified environments.
     */
    boolean anyEnvMatches(@NonNull String propertyName, @NonNull String[] envs, @NonNull String annotationName, @NonNull String delimiter) {
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

        String[] envList = splitAndValidateEnvs(envs, annotationName, delimiter);

        // Case-insensitive comparison and trim whitespace
        return Arrays.stream(envList)
                .anyMatch(env -> env.equalsIgnoreCase(currentEnv.trim()));
    }

    /**
     * Splits and validates the provided environment names.
     * <p>
     * For each string in the {@code envs} array, this method splits it using the specified {@code delimiter}
     * (if provided and not empty), trims whitespace, filters out empty or blank values, and ensures all
     * resulting environment names are unique and non-blank.
     * </p>
     *
     * @param envs           The array of environment names or CSV strings to process.
     * @param annotationName The name of the annotation for error reporting.
     * @param delimiter      The delimiter to use for splitting environment names. If empty, no splitting is performed.
     * @return A deduplicated array of trimmed, non-blank environment names.
     * @throws IllegalArgumentException if any resulting environment name is null or blank.
     */
    String[] splitAndValidateEnvs(@NonNull String[] envs, @NonNull String annotationName, @NonNull String delimiter) {
        return Arrays.stream(envs)
                .flatMap(env -> {
                    if (delimiter.isEmpty()) {
                        return Arrays.stream(new String[] { env });
                    } else {
                        return Arrays.stream(env.split(Pattern.quote(delimiter)));
                    }
                })
                .peek(env -> {
                    if (env == null || env.isBlank()) {
                        throw new IllegalArgumentException(
                                String.format("The 'value' parameter of the %s annotation cannot be null or blank!", annotationName));
                    }
                })
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toArray(String[]::new);
    }
}
