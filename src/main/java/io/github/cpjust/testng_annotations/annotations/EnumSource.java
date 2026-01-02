package io.github.cpjust.testng_annotations.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to provide enum constants as parameters to a test method.
 *
 * <p>NOTE: The test method parameter must be declared with the exact enum type
 * specified in this annotation (for example, {@code MyEnum}). Supertypes such as
 * {@code java.lang.Enum} or {@code Object} are not supported.</p>
 */
@Documented
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
     * NOTE: The names must match the enum constant names exactly, and are case-sensitive.
     *
     * @return The specific constants to include.
     */
    String[] names() default {};

    // TODO: Add other filtering options like in JUnit (e.g., mode, etc.)
}
