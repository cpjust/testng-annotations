package io.github.cpjust.testng_annotations.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated test method should be invoked with a single null argument and an empty argument (empty String, array, or collection).
 * Similar to JUnit's @NullAndEmptySource.
 * <p>
 * This is equivalent to using both @NullSource and @EmptySource on the same method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NullAndEmptySource {
}