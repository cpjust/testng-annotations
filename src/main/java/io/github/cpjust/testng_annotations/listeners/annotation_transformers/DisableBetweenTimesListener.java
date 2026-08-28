package io.github.cpjust.testng_annotations.listeners.annotation_transformers;

import io.github.cpjust.testng_annotations.annotations.DisableBetweenTimes;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Method;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * TestNG annotation listener that disables @Test methods when the current time falls inside any configured
 * {@link DisableBetweenTimes} ranges. The listener throws a SkipException or optionally sets {@code enabled=false}
 * on the TestNG annotation.
 *
 * <p>This listener is thread-safe and stateless, making it suitable for parallel test execution.</p>
 */
@Slf4j
public class DisableBetweenTimesListener implements IInvokedMethodListener {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;
    private final Clock clock;

    /**
     * Public no-arg constructor used by TestNG; uses system default clock.
     */
    public DisableBetweenTimesListener() {
        this(Clock.systemDefaultZone());
    }

    /**
     * Package-private constructor for unit tests to inject a fixed clock.
     */
    DisableBetweenTimesListener(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        log.debug("Checking @DisableBetweenTimes on method {}.{}",
                method.getTestMethod().getRealClass().getSimpleName(), method.getTestMethod().getMethodName());

        boolean shouldDisable = isDisabledByAnnotation(method.getTestMethod().getConstructorOrMethod().getMethod(), true)
                || isDisabledByAnnotation(method.getTestMethod().getRealClass(), true);

        if (shouldDisable) {
            log.info("Skipped {}.{} via annotation @DisableBetweenTimes because time is between specified range",
                    method.getTestMethod().getRealClass().getSimpleName(), method.getTestMethod().getMethodName());
            throw new SkipException("Skipped via annotation @DisableBetweenTimes because time is between specified range");
        }
    }

    /**
     * This method is needed to support the use case where users want to set enabled=false instead of throwing SkipException.
     * Normally this would override the IAnnotationTransformer.transform() method, but since TestNG doesn't allow multiple transformers,
     * we have to create a separate method and not implement IAnnotationTransformer to do this in the same class as the
     * beforeInvocation logic, and instead IAnnotationTransformer.transform() is handled by AllAnnotationTransformers.
     *
     * @param annotation The TestNG annotation being transformed.
     * @param testClass  The test class.
     * @param testMethod The test method.
     */
    public void transform(ITestAnnotation annotation, Class<?> testClass, Method testMethod) {
        if (testMethod == null) {
            return; // nothing to do
        }

        boolean shouldDisable = isDisabledByAnnotation(testMethod, false)
                || isDisabledByAnnotation(testClass, false);

        if (shouldDisable) {
            log.info("Setting test '{}' enabled=false based on DisableBetweenTimes check", testMethod.getName());
            annotation.setEnabled(false);
        } else {
            log.debug("Test '{}' is not disabled based on DisableBetweenTimes check", testMethod.getName());
        }
    }

    /**
     * Filters the provided annotations to only include those matching the throwSkipException value.
     *
     * @param annotations       The annotations to filter.
     * @param withSkipException Whether to filter for annotations that throw SkipException (true) or those that set enabled=false (false).
     * @return A list of matching @DisableBetweenTimes annotations.
     */
    private List<DisableBetweenTimes> filterAnnotations(DisableBetweenTimes[] annotations, boolean withSkipException) {
        return Arrays.stream(annotations)
                .filter(annotation -> annotation.throwSkipException() == withSkipException)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the list of @DisableBetweenTimes annotations on the given class that match the throwSkipException value.
     *
     * @param testClass         The class to check for annotations.
     * @param withSkipException Whether to filter for annotations that throw SkipException (true) or those that set enabled=false (false).
     * @return A list of matching @DisableBetweenTimes annotations, or an empty list if none are found.
     */
    private List<DisableBetweenTimes> getDisableBetweenTimesAnnotations(@NonNull Class<?> testClass, boolean withSkipException) {
        return filterAnnotations(testClass.getAnnotationsByType(DisableBetweenTimes.class), withSkipException);
    }

    /**
     * Retrieves the list of @DisableBetweenTimes annotations on the given method that match the throwSkipException value.
     *
     * @param method            The method to check for annotations.
     * @param withSkipException Whether to filter for annotations that throw SkipException (true) or those that set enabled=false (false).
     * @return A list of matching @DisableBetweenTimes annotations, or an empty list if none are found.
     */
    private List<DisableBetweenTimes> getDisableBetweenTimesAnnotations(@NonNull Method method, boolean withSkipException) {
        return filterAnnotations(method.getAnnotationsByType(DisableBetweenTimes.class), withSkipException);
    }

    /**
     * Checks if the given class has any @DisableBetweenTimes annotations that match the current time.
     *
     * @param clazz             The class to check for annotations.
     * @param withSkipException Whether to check for annotations that throw SkipException (true) or those that set enabled=false (false).
     * @return True if any matching annotation is found, false otherwise.
     */
    private boolean isDisabledByAnnotation(Class<?> clazz, boolean withSkipException) {
        if (clazz == null) {
            return false;
        }

        List<DisableBetweenTimes> classAnnotations = getDisableBetweenTimesAnnotations(clazz, withSkipException);

        if (!classAnnotations.isEmpty()) {
            log.debug("Class {} has @DisableBetweenTimes annotations with throwSkipException={}, checking if any match",
                    clazz.getSimpleName(), withSkipException);

            return classAnnotations.stream()
                    .anyMatch(this::isNowInRange);
        } else {
            log.debug("Class {} does not have any @DisableBetweenTimes annotations with throwSkipException={}",
                    clazz.getSimpleName(), withSkipException);
        }

        return false;
    }

    /**
     * Checks if the given method has any @DisableBetweenTimes annotations that match the current time.
     *
     * @param method            The method to check for annotations.
     * @param withSkipException Whether to check for annotations that throw SkipException (true) or those that set enabled=false (false).
     * @return True if any matching annotation is found, false otherwise.
     */
    private boolean isDisabledByAnnotation(Method method, boolean withSkipException) {
        if (method == null) {
            return false;
        }

        List<DisableBetweenTimes> methodAnnotations = getDisableBetweenTimesAnnotations(method, withSkipException);

        if (!methodAnnotations.isEmpty()) {
            log.debug("Method {}.{} has @DisableBetweenTimes annotations with throwSkipException={}, checking if any match",
                    method.getDeclaringClass().getSimpleName(), method.getName(), withSkipException);

            return methodAnnotations.stream()
                    .anyMatch(this::isNowInRange);
        } else {
            log.debug("Method {}.{} does not have any @DisableBetweenTimes annotations with throwSkipException={}",
                    method.getDeclaringClass().getSimpleName(), method.getName(), withSkipException);
        }

        return false;
    }

    /**
     * Checks if the current time is within the range specified by the annotation.
     *
     * @param annotation The DisableBetweenTimes annotation to check.
     * @return True if the current time is between the start and end times (inclusive), false otherwise.
     * @throws DateTimeParseException If the start or end time is not in the expected format.
     * @throws IllegalArgumentException If the end time is before the start time or if the timezone is invalid.
     */
    private boolean isNowInRange(@NonNull DisableBetweenTimes annotation) {
        ZoneId zone = parseTimezone(annotation.timezone());

        // Use the clock to get the current time in the specified timezone, to allow for testing with a fixed clock.
        ZonedDateTime zdt = ZonedDateTime.now(clock);
        ZonedDateTime zonedNow = zdt.withZoneSameInstant(zone);
        LocalTime now = zonedNow.toLocalTime();

        LocalTime start = LocalTime.parse(annotation.start().trim(), FORMATTER);
        LocalTime end = LocalTime.parse(annotation.end().trim(), FORMATTER);

        if (end.isBefore(start)) {
            throw new IllegalArgumentException(String.format(
                    "Invalid @DisableBetweenTimes annotation: start time '%s' must be before end time '%s'",
                    annotation.start(), annotation.end()));
        }

        // inclusive range
        return (!now.isBefore(start)) && (!now.isAfter(end));
    }

    /**
     * Parses the timezone string and returns the corresponding ZoneId. If the string is null or blank, returns the clock's timezone.
     *
     * @param timezoneString The timezone string to parse.
     * @return The corresponding ZoneId, or the clock's zone if the string is null or blank.
     * @throws IllegalArgumentException If the timezone string is not a valid timezone ID.
     */
    private ZoneId parseTimezone(String timezoneString) {
        if ((timezoneString == null) || timezoneString.isBlank()) {
            return clock.getZone();
        }

        try {
            return ZoneId.of(timezoneString.trim());
        } catch (DateTimeException e) {
            throw new IllegalArgumentException(
                    String.format("Invalid timezone: '%s'", timezoneString), e);
        }
    }
}
