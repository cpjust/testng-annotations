package io.github.cpjust.testng_annotations.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a TestNG configuration or test method as a fail-fast trigger.
 *
 * <p>If the annotated method fails, the listener will skip all subsequent TestNG tests and
 * configuration methods, even when they declare {@code alwaysRun = true}.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FailFast {
}
