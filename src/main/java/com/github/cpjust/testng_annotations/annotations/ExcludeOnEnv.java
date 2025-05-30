package com.github.cpjust.testng_annotations.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excludes a test when run on a specific set of environments.
 * Ex. If a test is annotated with `@ExcludeOnEnv(value = {"Stage", "Prod"}, propertyName = "environment")` and you run
 * with the `-Denvironment=Prod` option, the test will be excluded.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ExcludeOnEnv {
    /**
     * The environment names to exclude the test on.
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
