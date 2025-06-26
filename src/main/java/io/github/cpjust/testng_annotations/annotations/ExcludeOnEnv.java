package io.github.cpjust.testng_annotations.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excludes a test when run on a specific set of environments.
 * Ex. If a test is annotated with <code>@ExcludeOnEnv(value = {"Stage", "Prod"}, propertyName = "environment")</code> and you run
 * with the <code>-Denvironment=Prod</code> option, the test will be excluded.
 * <br/>
 * The following attributes can be used with this annotation:
 * <ul>
 *     <li>value: (required) A list of environment names where the test should be excluded.</li>
 *     <li>propertyName: (optional) The name of the Java property to read to get the name of the current environment.  Defaults to "env".</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ExcludeOnEnv {
    /**
     * The environment names to exclude the test on.
     * You can pass a single environment with a string (ex. "prod") or multiple environments in braces (ex. {"Stage", "Prod"}).
     *
     * @return An array of environment names.
     */
    String[] value();

    /**
     * The Java property name from which to read the current environment where the tests are being executed
     * (ex. "dev", "stage", "Prod"...).
     *
     * @return The property name to read.  Defaults to "env".
     */
    String propertyName() default "env";
}
