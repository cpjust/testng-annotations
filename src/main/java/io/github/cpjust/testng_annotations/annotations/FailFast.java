package io.github.cpjust.testng_annotations.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a TestNG configuration or test method as a fail-fast trigger.
 *
 * <p>If the annotated method fails, the listener will skip all subsequent TestNG tests and
 * configuration methods (in the same test class hierarchy), even when they declare {@code alwaysRun = true}.</p>
 * <p>Ex. If TestBaseA fails, TestA will be skipped, but TestB (which isn't derived from TestBaseA) won't be skipped.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FailFast {
}
