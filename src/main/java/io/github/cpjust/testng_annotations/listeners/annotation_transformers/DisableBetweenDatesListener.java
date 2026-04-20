package io.github.cpjust.testng_annotations.listeners.annotation_transformers;

import io.github.cpjust.testng_annotations.annotations.DisableBetweenDates;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Method;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * TestNG annotation listener that disables @Test methods when the current date falls inside any configured
 * {@link DisableBetweenDates} ranges. The listener throws a SkipException or optionally sets {@code enabled=false}
 * on the TestNG annotation.
 */
@Slf4j
public class DisableBetweenDatesListener implements IInvokedMethodListener {
    private final Clock clock;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Public no-arg constructor used by TestNG; uses system default clock.
     */
    public DisableBetweenDatesListener() {
        this(Clock.systemDefaultZone());
    }

    /**
     * Package-private constructor for unit tests to inject a fixed clock.
     */
    DisableBetweenDatesListener(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        log.debug("Checking @DisableBetweenDates on method {}.{}",
                method.getTestMethod().getRealClass().getSimpleName(), method.getTestMethod().getMethodName());

        boolean shouldDisable = isDisabledByAnnotation(method.getTestMethod().getConstructorOrMethod().getMethod(), true)
                || isDisabledByAnnotation(method.getTestMethod().getRealClass(), true);

        if (shouldDisable) {
            throw new SkipException("Skipped via annotation @DisableBetweenDates because date is between specified range");
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
            log.debug("Setting test '{}' enabled=false based on DisableBetweenDates check", testMethod.getName());
            annotation.setEnabled(false);
        }
    }

    /**
     * Retrieves the list of @DisableBetweenDates annotations on the given class that match the throwSkipException value.
     *
     * @param testClass         The class to check for annotations.
     * @param withSkipException Whether to filter for annotations that throw SkipException (true) or those that set enabled=false (false).
     * @return A list of matching @DisableBetweenDates annotations, or an empty list if none are found.
     */
    private List<DisableBetweenDates> getDisableBetweenDatesAnnotations(@NonNull Class<?> testClass, boolean withSkipException) {
        DisableBetweenDates[] methodAnnotations = testClass.getAnnotationsByType(DisableBetweenDates.class);
        return Arrays.stream(methodAnnotations)
                .filter(annotation -> annotation.throwSkipException() == withSkipException)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the list of @DisableBetweenDates annotations on the given method that match the throwSkipException value.
     *
     * @param method            The method to check for annotations.
     * @param withSkipException Whether to filter for annotations that throw SkipException (true) or those that set enabled=false (false).
     * @return A list of matching @DisableBetweenDates annotations, or an empty list if none are found.
     */
    private List<DisableBetweenDates> getDisableBetweenDatesAnnotations(@NonNull Method method, boolean withSkipException) {
        DisableBetweenDates[] methodAnnotations = method.getAnnotationsByType(DisableBetweenDates.class);
        return Arrays.stream(methodAnnotations)
                .filter(annotation -> annotation.throwSkipException() == withSkipException)
                .collect(Collectors.toList());
    }

    /**
     * Checks if the given class has any @DisableBetweenDates annotations that match the current date.
     *
     * @param clazz             The class to check for annotations.
     * @param withSkipException Whether to check for annotations that throw SkipException (true) or those that set enabled=false (false).
     * @return True if any matching annotation is found, false otherwise.
     */
    private boolean isDisabledByAnnotation(Class<?> clazz, boolean withSkipException) {
        if (clazz == null) {
            return false;
        }

        List<DisableBetweenDates> classAnnotations = getDisableBetweenDatesAnnotations(clazz, withSkipException);

        if (!classAnnotations.isEmpty()) {
            log.debug("Class {} has @DisableBetweenDates annotations with throwSkipException={}, checking if any match",
                    clazz.getSimpleName(), withSkipException);

            return classAnnotations.stream()
                    .anyMatch(this::isNowInRange);
        }

        return false;
    }

    /**
     * Checks if the given method has any @DisableBetweenDates annotations that match the current date.
     *
     * @param method            The method to check for annotations.
     * @param withSkipException Whether to check for annotations that throw SkipException (true) or those that set enabled=false (false).
     * @return True if any matching annotation is found, false otherwise.
     */
    private boolean isDisabledByAnnotation(Method method, boolean withSkipException) {
        if (method == null) {
            return false;
        }

        List<DisableBetweenDates> methodAnnotations = getDisableBetweenDatesAnnotations(method, withSkipException);

        if (!methodAnnotations.isEmpty()) {
            log.debug("Method {}.{} has @DisableBetweenDates annotations with throwSkipException={}, checking if any match",
                    method.getDeclaringClass().getSimpleName(), method.getName(), withSkipException);

            return methodAnnotations.stream()
                    .anyMatch(this::isNowInRange);
        }

        return false;
    }

    /**
     * Checks if the current date is within the range specified by the annotation.
     *
     * @param annotation The DisableBetweenDates annotation to check.
     * @return True if the current date is between the start and end dates (inclusive), false otherwise.
     * @throws DateTimeParseException If the start or end date is not in the expected format.
     * @throws IllegalArgumentException If the end date is before the start date.
     */
    private boolean isNowInRange(@NonNull DisableBetweenDates annotation) {
        LocalDate now = LocalDate.now(clock);
        LocalDate start = LocalDate.parse(annotation.start().trim(), FORMATTER);
        LocalDate end = LocalDate.parse(annotation.end().trim(), FORMATTER);

        if (end.isBefore(start)) {
            throw new IllegalArgumentException(String.format(
                    "Invalid @DisableBetweenDates annotation: end date '%s' must not be before start date '%s'",
                    annotation.end(), annotation.start()));
        }

        if (now.isAfter(end)) {
            log.warn("@DisableBetweenDates annotation has expired: current date '{}' is after end date '{}'",
                    now, annotation.end());
        }

        // inclusive range
        return (!now.isBefore(start)) && (!now.isAfter(end));
    }
}
