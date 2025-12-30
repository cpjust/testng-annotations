package io.github.cpjust.testng_annotations.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to provide enum constants as parameters to a test method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EnumSource {

    /**
     * The enum class to provide constants from.
     *
     * @return The enum class.
     */
    @SuppressWarnings("java:S1452") // Suppress "Generic wildcard types should not be used in return types" warning.
    Class<? extends Enum<?>> value();

    /**
     * Specific constants to include. If empty, all constants are included.
     *
     * @return The specific constants to include.
     */
    String[] names() default {};

    // TODO: Add other filtering options like in JUnit (e.g., mode, etc.)
}
