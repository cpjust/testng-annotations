package io.github.cpjust.testng_annotations.listeners.annotation_transformers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Base class for source listeners that handle data provider annotations.
 */
@RequiredArgsConstructor
public abstract class SourceListenerBase {
    private final List<Map.Entry<Class<?>, String>> allowedDataProviders;

    /**
     * Throws an exception if the test method has multiple data provider annotations that use different listeners.
     *
     * @param testMethod The test method.
     */
    protected static void throwIfTestHasMultipleDataProviders(@NonNull Method testMethod) {
        boolean hasCsvSource = CsvSourceListener.isCsvSourcePresent(testMethod);
        boolean hasEnumSource = EnumSourceListener.isEnumSourcePresent(testMethod);
        boolean hasValueSource = ValueSourceListener.isValueSourcePresent(testMethod);

        long sourceCount = Stream.of(hasCsvSource, hasEnumSource, hasValueSource)
                .filter(b -> b) // Filter only true values.
                .count();

        if (sourceCount > 1) {
            throw new IllegalStateException(String.format(
                    "Cannot combine @CsvSource with @EnumSource or any ValueSource annotation on method: %s.%s. "
                            + "Only one of these annotations may be present.",
                    testMethod.getDeclaringClass().getName(), testMethod.getName()));
        }
    }

    /**
     * Throws an exception if a data provider is specified in the @Test that is not allowed with the present source annotations.
     *
     * @param annotation The TestNG test annotation.
     * @param testMethod The test method.
     */
    protected void throwIfDataProviderNotAllowed(@NonNull ITestAnnotation annotation, @NonNull Method testMethod) {
        if (hasAnySource(testMethod) && !isDataProviderAllowed(annotation)) {
            throw new IllegalStateException(String.format(
                    "Cannot specify a dataProvider in @Test when also using @CsvSource or any ValueSource annotation on method: %s.%s. "+
                            "Remove either the dataProvider or the source annotation.",
                    testMethod.getDeclaringClass().getName(), testMethod.getName()
            ));
        }
    }

    /**
     * Checks if the specified data provider is allowed.
     * Ex. If a data provider is specified in the @Test annotation that matches the one used by the source annotation.
     *
     * @param annotation The TestNG test annotation.
     * @return True if the data provider is allowed, false otherwise.
     */
    protected boolean isDataProviderAllowed(@NonNull ITestAnnotation annotation) {
        Class<?> dataProviderClass = annotation.getDataProviderClass();
        String dataProvider = annotation.getDataProvider();
        boolean isDataProviderAllowed = ((dataProviderClass == null) && ((dataProvider == null) || dataProvider.isBlank()));

        // If either dataProvider or dataProviderClass is specified, then both must match an allowed pair.
        if (!isDataProviderAllowed) {
            // If no dataProvider name is specified, it defaults to "", so only continue checking allowedDataProviders if it's not blank.
            isDataProviderAllowed = (dataProvider != null) && !dataProvider.isBlank() && allowedDataProviders.stream()
                    .anyMatch(kv -> (kv.getKey() == dataProviderClass) &&
                            (kv.getValue().equals(dataProvider)));
        }

        return isDataProviderAllowed;
    }

    /**
     * Checks if any source annotation is present on the given method.
     *
     * @param testMethod The test method.
     * @return True if any source annotation is present, false otherwise.
     */
    protected boolean hasAnySource(@NonNull Method testMethod) {
        return CsvSourceListener.isCsvSourcePresent(testMethod) ||
               ValueSourceListener.isValueSourcePresent(testMethod) ||
               EnumSourceListener.isEnumSourcePresent(testMethod);
    }
}
