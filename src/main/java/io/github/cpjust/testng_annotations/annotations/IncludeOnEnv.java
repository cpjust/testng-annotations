package io.github.cpjust.testng_annotations.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Includes a test only when run on a specific set of environments.
 * Ex. If a test is annotated with <code>@IncludeOnEnv(value = {"Stage", "Prod"}, propertyName = "environment")</code> and you run
 * with the <code>-Denvironment=Prod</code> option, the test will be included.
 * <br/>
 * The following attributes can be used with this annotation:
 * <ul>
 *     <li>value: (required) A list of environment names where the test should be included (case-insensitive).</li>
 *     <li>propertyName: (optional) The name of the Java property to read to get the name of the current environment.  Defaults to "env".</li>
 *     <li>delimiter: (optional) If set, all values in the array will be split on this delimiter as a CSV string and combined into a single list (duplicates removed).
 *     If not set (empty string), values are used as-is. Defaults to "".</li>
 * </ul>
 * NOTE: The environment names are compared case-insensitively.
 * <br/><br/>
 * Tests should be included using the following rules:
 * <br/>
 * <table>
 *     <caption>Inclusion rules for IncludeOnEnv</caption>
 *     <tr>
 *         <th></th><th>No class annotation:</th><th>Include by class:</th><th>Exclude by class:</th>
 *     </tr>
 *     <tr>
 *         <th scope="row">No test annotation:</th><td>INCLUDE</td><td>INCLUDE</td><td>EXCLUDE</td>
 *     </tr>
 *     <tr>
 *         <th scope="row">Include by test:</th><td>INCLUDE</td><td>INCLUDE</td><td>INCLUDE</td>
 *     </tr>
 *     <tr>
 *         <th scope="row">Exclude by test:</th><td>EXCLUDE</td><td>EXCLUDE</td><td>EXCLUDE</td>
 *     </tr>
 * </table>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface IncludeOnEnv {
    /**
     * The environment names to include the test on.
     * You can pass a single environment with a string (ex. "prod") or multiple environments in braces (ex. {"Stage", "Prod"}).
     * Cannot be null, blank or an empty array, otherwise an IllegalArgumentException is thrown.
     *
     * @return An array of environment names.
     */
    String[] value();

    /**
     * The Java property name from which to read the current environment where the tests are being executed
     * (ex. "dev", "stage", "Prod"...).
     * Cannot be null or blank, otherwise an IllegalArgumentException is thrown.
     *
     * @return The property name to read.  Defaults to "env".
     */
    String propertyName() default "env";

    /**
     * The CSV string delimiter to use when splitting the values in the value array. If set to a non-empty string,
     * all values in the array will be split on this delimiter and combined into a single list (duplicates removed).
     * If empty, values are used as-is.
     *
     * @return The delimiter string. Defaults to "" (no splitting).
     */
    String delimiter() default "";
}
