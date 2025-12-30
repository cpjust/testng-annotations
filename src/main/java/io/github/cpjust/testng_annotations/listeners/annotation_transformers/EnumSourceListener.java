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

        Enum<?>[] constants = validateMethodAndAnnotation(method, enumSource);

        String[] names = enumSource.names();
        List<Enum<?>> filteredConstants = Arrays.stream(constants)
                .filter(constant -> names.length == 0 || Arrays.asList(names).contains(constant.name()))
                .collect(Collectors.toList());

        if (filteredConstants.isEmpty()) {
            throw new IllegalStateException("No matching enum constants found for method: " + method.getName());
        }

        return filteredConstants.stream()
                .map(constant -> new Object[]{ constant })
                .toArray(Object[][]::new);
    }

    /**
     * Validates that the method and annotation are compatible.
     *
     * @param method     The test method.
     * @param enumSource The EnumSource annotation.
     * @return The enum constants of the specified enum class.
     */
    private static Enum<?>[] validateMethodAndAnnotation(Method method, EnumSource enumSource) {
        if (enumSource == null) {
            throw new IllegalStateException("No @EnumSource annotation found on method: " + method.getName());
        }

        Class<? extends Enum<?>> enumClass = enumSource.value();
        Enum<?>[] constants = enumClass.getEnumConstants();

        if (constants == null) {
            throw new IllegalStateException("Provided class is not an enum: " + enumClass.getName());
        }

        Class<?>[] methodParamClasses = method.getParameterTypes();

        if (methodParamClasses.length != 1) {
            throw new IllegalStateException(String.format("Method '%s' should have 1 parameter, but it has %d", method.getName(), methodParamClasses.length));
        }

        if (!enumClass.isAssignableFrom(methodParamClasses[0]) || !methodParamClasses[0].isAssignableFrom(enumClass)) {
            throw new IllegalStateException(String.format(
                    "Enum class %s is not compatible with parameter type %s in method: %s",
                    enumClass.getSimpleName(), method.getParameterTypes()[0].getSimpleName(), method.getName()));
        }

        return constants;
    }
}
