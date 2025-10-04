package io.github.cpjust.testng_annotations.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated test method should be invoked with a single empty argument (empty String, array, or collection).
 * Similar to JUnit's {@code @EmptySource}.
 *
 * <p><b>Supported parameter types:</b></p>
 * <ul>
 *   <li>{@code String} (injected value: {@code ""})</li>
 *   <li>Array (injected value: empty array of the appropriate type)</li>
 *   <li>Collection types:
 *     <ul>
 *       <li>{@code List} (injected value: empty {@code List})</li>
 *       <li>{@code Set} (injected value: empty {@code Set})</li>
 *       <li>{@code Queue} (injected value: empty {@code Queue})</li>
 *       <li>{@code Map} (injected value: empty {@code Map})</li>
 *     </ul>
 *   </li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EmptySource {
}
