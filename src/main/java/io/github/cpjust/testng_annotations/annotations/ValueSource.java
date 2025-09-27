package io.github.cpjust.testng_annotations.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides a set of values to be used as parameters in a test method.
 * Similar to JUnit's ValueSource, this annotation allows you to specify an array of values that will be used to run the
 * test method multiple times, once with each value.  The test method must have exactly 1 parameter of the corresponding type.
 * You must use only one type of value array per annotation instance.  Ex. You can use strings or ints, but not both.
 * Values can be of type String, char, boolean, byte, short, int, long, float, double, or Class and must not contain empty value arrays.
 *
 * Example usage:
 * <pre>
 * {@code
 * @Test
 * @ValueSource(strings = {"foo", "bar"})
 * public void testWithString(String value) {
 *     assertNotNull(value);
 * }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ValueSource {
    /**
     * Array of string values to use as parameters.
     * @return String array of values
     */
    String[] strings() default {};

    /**
     * Array of char values to use as parameters.
     * @return char array of values
     */
    char[] chars() default {};

    /**
     * Array of boolean values to use as parameters.
     * @return boolean array of values
     */
    boolean[] booleans() default {};

    /**
     * Array of byte values to use as parameters.
     * @return byte array of values
     */
    byte[] bytes() default {};

    /**
     * Array of short values to use as parameters.
     * @return short array of values
     */
    short[] shorts() default {};

    /**
     * Array of int values to use as parameters.
     * @return int array of values
     */
    int[] ints() default {};

    /**
     * Array of long values to use as parameters.
     * @return long array of values
     */
    long[] longs() default {};

    /**
     * Array of float values to use as parameters.
     * @return float array of values
     */
    float[] floats() default {};

    /**
     * Array of double values to use as parameters.
     * @return double array of values
     */
    double[] doubles() default {};

    /**
     * Array of Class values to use as parameters.
     * @return Class array of values
     */
    Class<?>[] classes() default {};
}
