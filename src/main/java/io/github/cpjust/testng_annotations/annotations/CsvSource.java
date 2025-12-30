package io.github.cpjust.testng_annotations.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that provides a comma-separated list of values for a parameterized test method.
 * Works similarly to JUnit's CsvSource.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CsvSource {
    /**
     * The CSV lines to use as arguments. Each line is split into parameters.
     */
    String[] value();

    /**
     * The delimiter to use for splitting values. Defaults to ','.
     * NOTE: You cannot use a newline '\n' or the quote character as a delimiter.
     */
    char delimiter() default ',';

    /**
     * The character used to quote values. Defaults to single quote '\''.
     * NOTE: You cannot use the delimiter character as a quote character.
     */
    char quoteCharacter() default '\'';

    /**
     * Whether to trim leading and trailing whitespace from each parameter value.
     * Defaults to true.
     */
    boolean trimWhitespace() default true;
}
