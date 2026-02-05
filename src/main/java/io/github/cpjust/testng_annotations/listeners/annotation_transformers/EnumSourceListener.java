package io.github.cpjust.testng_annotations.listeners.annotation_transformers;

import io.github.cpjust.testng_annotations.annotations.EnumSource;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * TestNG listener that processes {@link EnumSource} annotations and converts them into data provider parameters.
 */
@Slf4j
public class EnumSourceListener extends SourceListenerBase implements IAnnotationTransformer {
    static final String ENUM_SOURCE_PROVIDER = "enumSourceProvider";
    static final Map.Entry<Class<?>, String> ENUM_SOURCE_PROVIDER_CLASS_AND_NAME = Map.entry(EnumSourceListener.class, ENUM_SOURCE_PROVIDER);

    /**
     * Constructs the listener with the enum source data provider.
     */
    public EnumSourceListener() {
        super(List.of(ENUM_SOURCE_PROVIDER_CLASS_AND_NAME));
    }

    /**
     * Transforms test methods annotated with {@link EnumSource} to use a data provider.
     * @param annotation      The TestNG annotation being transformed.
     * @param testClass       The test class (unused).
     * @param testConstructor The test constructor (unused).
     * @param testMethod      The test method.
     */
    @Override
    public void transform(@NonNull ITestAnnotation annotation, Class testClass,
                          Constructor testConstructor, Method testMethod) {
        if (testMethod == null) {
            return;
        }

        throwIfDataProviderNotAllowed(annotation, testMethod);
        throwIfTestHasMultipleDataProviders(testMethod);

        if (isEnumSourcePresent(testMethod)) {
            annotation.setDataProvider(ENUM_SOURCE_PROVIDER_CLASS_AND_NAME.getValue());
            annotation.setDataProviderClass(ENUM_SOURCE_PROVIDER_CLASS_AND_NAME.getKey());
        }
    }

    /**
     * Checks if the given method is annotated with {@link EnumSource}.
     *
     * @param method The test method.
     * @return True if the method is annotated with {@link EnumSource}, false otherwise.
     */
    public static boolean isEnumSourcePresent(@NonNull Method method) {
        return method.isAnnotationPresent(EnumSource.class);
    }

    /**
     * Data provider that supplies parameter values for methods annotated with {@link EnumSource}.
     *
     * @param method The test method.
     * @return A 2D array of parameter values.
     */
    @DataProvider(name = ENUM_SOURCE_PROVIDER)
    public static Object[][] enumSourceProvider(@NonNull Method method) {
        EnumSource enumSource = method.getAnnotation(EnumSource.class);

        Enum<?>[] allEnumConstants = validateMethodAndAnnotation(method, enumSource);

        String[] names = enumSource.names();
        EnumSource.Mode mode = enumSource.mode();
        List<Enum<?>> filteredConstants;

        // If no names are specified, include all enum constants.
        if (names.length == 0) {
            filteredConstants = Arrays.asList(allEnumConstants);
        } else {
            filteredConstants = filterEnumConstants(mode, names, allEnumConstants);
        }

        if (filteredConstants.isEmpty()) {
            throw new IllegalStateException("No matching enum constants found for method: " + method.getName());
        }

        // Convert List to Object[][] for data provider.
        return filteredConstants.stream()
                .map(constant -> new Object[]{ constant })
                .toArray(Object[][]::new);
    }

    /**
     * Filters enum constants based on the specified mode and names.
     *
     * @param mode              The filtering mode.
     * @param names             The names or patterns to filter by.
     * @param allEnumConstants  All enum constants of the enum class.
     * @return A list of filtered enum constants.
     */
    private static List<Enum<?>> filterEnumConstants(EnumSource.Mode mode, String[] names, Enum<?>[] allEnumConstants) {
        switch (mode) {
            case MATCH_ANY: {
                final Pattern[] patterns = compilePatterns(names);
                return Arrays.stream(allEnumConstants)
                        .filter(constant -> matchesAny(patterns, constant.name()))
                        .collect(Collectors.toList());
            }
            case MATCH_ALL: {
                final Pattern[] patterns = compilePatterns(names);
                return Arrays.stream(allEnumConstants)
                        .filter(constant -> matchesAll(patterns, constant.name()))
                        .collect(Collectors.toList());
            }
            case EXCLUDE: {
                // Use HashSet backed by Arrays.asList for clearer intent and faster lookups
                final Set<String> namesToFilter = new HashSet<>(Arrays.asList(names));
                return Arrays.stream(allEnumConstants)
                        .filter(constant -> !namesToFilter.contains(constant.name()))
                        .collect(Collectors.toList());
            }
            case INCLUDE:
            default: {
                final Set<String> namesToFilter = new HashSet<>(Arrays.asList(names));
                return Arrays.stream(allEnumConstants)
                        .filter(constant -> namesToFilter.contains(constant.name()))
                        .collect(Collectors.toList());
            }
        }
    }

    /**
     * Returns true if any pattern matches the given input.
     *
     * @param patterns The array of compiled regex patterns.
     * @param input    The input string to match against.
     * @return True if any pattern matches the input, false otherwise.
     */
    private static boolean matchesAny(Pattern[] patterns, String input) {
        for (Pattern p : patterns) {
            if (p.matcher(input).find()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if all patterns match the given input.
     *
     * @param patterns The array of compiled regex patterns.
     * @param input    The input string to test.
     * @return True if all patterns match the input, false otherwise.
     */
    private static boolean matchesAll(Pattern[] patterns, String input) {
        for (Pattern p : patterns) {
            if (!p.matcher(input).find()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Compiles an array of string regex patterns into Pattern objects.
     *
     * @param names The array of string regex patterns.
     * @return An array of compiled Pattern objects.
     * @throws IllegalArgumentException If any of the patterns are invalid.
     */
    private static Pattern[] compilePatterns(String[] names) {
        try {
            return Arrays.stream(names)
                    .map(Pattern::compile)
                    .toArray(Pattern[]::new);
        } catch (PatternSyntaxException ex) {
            throw new IllegalArgumentException("Invalid regular expression in EnumSource.names(): " + ex.getMessage(), ex);
        }
    }

    /**
     * Validates that the method and annotation are compatible.
     *
     * @param method     The test method.
     * @param enumSource The EnumSource annotation.
     * @return The enum constants of the specified enum class.
     */
    private static Enum<?>[] validateMethodAndAnnotation(Method method, EnumSource enumSource) {
        // Verify that the annotation is present and get the enum class and constants.
        Objects.requireNonNull(enumSource, "No @EnumSource annotation found on method: " + method.getName());

        Class<? extends Enum<?>> enumClass = enumSource.value();
        Enum<?>[] constants = enumClass.getEnumConstants();

        if (constants == null) {
            throw new IllegalStateException("Provided class is not an enum: " + enumClass.getName());
        }

        // Validate the method has exactly one parameter of the correct enum type.
        Class<?>[] methodParamClasses = method.getParameterTypes();
        final String methodName = method.getName();

        if (methodParamClasses.length != 1) {
            throw new IllegalStateException(String.format("Method '%s' should have 1 parameter, but it has %d", methodName, methodParamClasses.length));
        }

        if (!enumClass.equals(methodParamClasses[0])) {
            throw new IllegalStateException(String.format(
                    "Enum class %s is not compatible with parameter type %s in method: %s",
                    enumClass.getSimpleName(), methodParamClasses[0].getSimpleName(), methodName));
        }

        return constants;
    }
}
