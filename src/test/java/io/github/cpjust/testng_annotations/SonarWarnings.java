package io.github.cpjust.testng_annotations;

import lombok.NoArgsConstructor;

/**
 * Constants for SonarQube warning codes suppressed in this project.
 * NOTE: Using these constants in @SuppressWarnings annotations doesn't always work with the SonarQube plugin,
 * so you may need to redefine them in the class file and use those local constants instead.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class SonarWarnings {
    public static final String METHODS_SHOULD_NOT_BE_EMPTY = "java:S1186"; // Suppress "Methods should not be empty" warning
    public static final String METHODS_SHOULD_NOT_HAVE_IDENTICAL_IMPLEMENTATIONS = "java:S4144"; // Suppress "Methods should not have identical implementations" warning
    public static final String SIMILAR_TESTS_SHOULD_BE_PARAMETRIZED = "java:S5976"; // Suppress "Similar tests should be grouped in a single Parameterized test" warning
}
