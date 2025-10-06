package io.github.cpjust.testng_annotations;

import lombok.NoArgsConstructor;

/**
 * Constants for IntelliJ warning codes suppressed in this project.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class IntelliJWarnings {
    public static final String PASSING_NULL_ARG_TO_NONNULL_PARAMETER = "ConstantConditions"; //Passing 'null' argument to parameter annotated as @NotNull
}
