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

    /**
     * The mode to use when interpreting the {@link #names()} values. Defaults to {@link Mode#INCLUDE}.
     *
     * <ul>
     *   <li>INCLUDE - treat {@code names()} as exact names to include (default behavior).</li>
     *   <li>EXCLUDE - treat {@code names()} as exact names to exclude.</li>
     *   <li>MATCH_ANY - treat each entry in {@code names()} as a regular expression and include a constant if at least one pattern matches its name.</li>
     *   <li>MATCH_ALL - treat each entry in {@code names()} as a regular expression and include a constant only if every pattern matches its name.</li>
     * </ul>
     *
     * @return The matching mode.
     */
    Mode mode() default Mode.INCLUDE;

    /**
     * Mode controls how {@link #names()} is interpreted when selecting enum constants.
     */
    enum Mode {
        /** Include only the named constants (default). */
        INCLUDE,
        /** Exclude the named constants. */
        EXCLUDE,
        /** Treat names as regular expressions; include constant if any pattern matches. */
        MATCH_ANY,
        /** Treat names as regular expressions; include constant only if all patterns match. */
        MATCH_ALL
    }

    // TODO: Add other filtering options like in JUnit (e.g., mode, etc.)
}
