package io.github.cpjust.testng_annotations.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Disables a test when the current date is between the provided start and end dates (inclusive).
 * Dates must be in ISO-8601 format (yyyy-MM-dd). Example: {@code @DisableBetweenDates(start = "2025-01-01", end = "2025-12-31")}.
 * This annotation can be applied at the class or method level. Method-level annotations take precedence over class-level annotations.
 * If multiple @DisableBetweenDates annotations are present, the test will be disabled if the current date falls within any of the specified ranges.
 * Dates are based on the specified time zone, or the system default time zone if not specified.
 */
@Documented
@Repeatable(DisableBetweenDates.Container.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface DisableBetweenDates {
    /**
     * The start date (inclusive) in ISO-8601 format (yyyy-MM-dd).
     * @return start date string.
     */
    String start();

    /**
     * The end date (inclusive) in ISO-8601 format (yyyy-MM-dd).
     * @return end date string.
     */
    String end();

    /**
     * The time zone to use for date calculations. If empty, uses the system default time zone.
     * Must be a valid time zone ID, such as "UTC", "America/New_York", or "+01:00" (offset format).
     * Note: When comparing dates, the test's local date is evaluated in this timezone.
     * For example, with timezone="-11:00" and date="2026-02-16", a test run at 2026-02-16T10:59:59Z
     * would NOT be in range (it's 2026-02-15 in -11:00 timezone).
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
     * Container annotation for repeatable @DisableBetweenDates.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    @interface Container {
        /**
         * The array of @DisableBetweenDates annotations. This is required for repeatable annotations to work.
         *
         * @return array of @DisableBetweenDates annotations.
         */
        DisableBetweenDates[] value();
    }
}
