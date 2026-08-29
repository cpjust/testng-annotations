package io.github.cpjust.testng_annotations.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Disables a test every day between the provided start and end times (inclusive).
 * Times must be in ISO-8601 format (HH:mm:ss). Example: {@code @DisableBetweenTimes(start = "09:00:00", end = "17:00:00")}.
 * This annotation can be applied at the class or method level. Method-level annotations take precedence over class-level annotations.
 * If multiple @DisableBetweenTimes annotations are present, the test will be disabled if the current time falls within any of the specified ranges.
 * Times are based on the specified time zone, or the system default time zone if not specified.
 * The start time must be less than or equal to the end time. If the start time is greater than the end time,
 * the annotation will be ignored and a warning will be logged.
 * If you want to disable a test across midnight (e.g., from 22:00 to 06:00), you must use two separate annotations:
 * {@code @DisableBetweenTimes(start = "22:00:00", end = "23:59:59")} and {@code @DisableBetweenTimes(start = "00:00:00", end = "06:00:00")}.
 */
@Documented
@Repeatable(DisableBetweenTimes.Container.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface DisableBetweenTimes {
    /**
     * The start time (inclusive) in ISO-8601 format (HH:mm:ss).
     * @return start time string.
     */
    String start();

    /**
     * The end time (inclusive) in ISO-8601 format (HH:mm:ss).
     * @return end time string.
     */
    String end();

    /**
     * The time zone to use for time calculations. If empty, uses the system default time zone.
     * Must be a valid time zone ID, such as "UTC", "America/New_York", or "+01:00" (offset format).
     * Note: When comparing times, the test's local time is evaluated in this timezone.
     * For example, with timezone="America/Los_Angeles" and start="10:00:00", a test run at 2026-05-11T16:59:59Z
     * would NOT be in range (it's 09:59:59 in America/Los_Angeles timezone).
     *
     * @return time zone string.
     */
    String timezone() default "";

    /**
     * Whether to skip the test by throwing a SkipException (true) or to disable it by setting enabled=false on the TestNG annotation (false).
     * Default is true (skip by throwing SkipException). Setting this to false will prevent the test from being marked as "skipped" in
     * TestNG reports and instead mark it as "disabled".
     * Note that if using enabled=false, the test method will not be invoked at all, so any logic in listeners that relies on method
     * invocation may not run. Also, if using enabled=false, the test will not appear in the test results at all, which may affect reporting
     * and test coverage metrics.
     *
     * @return true to skip by throwing SkipException, false to disable by setting enabled=false.
     */
    boolean throwSkipException() default true;

    /**
     * Container annotation for repeatable @DisableBetweenTimes.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    @interface Container {
        /**
         * The array of @DisableBetweenTimes annotations. This is required for repeatable annotations to work.
         *
         * @return array of @DisableBetweenTimes annotations.
         */
        DisableBetweenTimes[] value();
    }
}
