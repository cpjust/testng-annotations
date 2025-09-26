package io.github.cpjust.testng_annotations.listeners;

import io.github.cpjust.testng_annotations.annotations.ValueSource;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * TestNG listener that processes @ValueSource annotations and converts them into data provider parameters.
 */
@Slf4j
public class ValueSourceListener implements IAnnotationTransformer {
    private static final String VALUE_SOURCE_PROVIDER = "valueSourceProvider";

    /**
     * Transforms test methods annotated with {@link ValueSource} to use a data provider.
     *
     * @param annotation      The TestNG annotation being transformed.
     * @param testClass       The test class.
     * @param testConstructor The test constructor.
     * @param testMethod      The test method.
     */
    @Override
    public void transform(ITestAnnotation annotation, Class testClass,
                        Constructor testConstructor, Method testMethod) {
        if (testMethod != null && testMethod.isAnnotationPresent(ValueSource.class)) {
            annotation.setDataProvider(VALUE_SOURCE_PROVIDER);
            annotation.setDataProviderClass(ValueSourceListener.class);
        }
    }

    /**
     * Provides values for test methods annotated with {@link ValueSource} just like a DataProvider would.
     *
     * @param method The test method.
     * @return An array of parameter values for the test method.
     * @throws IllegalStateException if the annotation is missing or misused.
     */
    @DataProvider(name = VALUE_SOURCE_PROVIDER)
    public static Object[] provideValues(@NonNull Method method) {
        ValueSource valueSource = method.getAnnotation(ValueSource.class);

        if (valueSource == null) {
            // This would only happen if a test method is using this data provider without the @ValueSource annotation.
            // Ex. @Test(dataProvider = "valueSourceProvider", dataProviderClass = ValueSourceListener.class)
            throw new IllegalStateException("@ValueSource annotation not found on method: " + method.getName());
        }

        Class<?>[] paramTypes = method.getParameterTypes();

        if (paramTypes.length != 1) {
            throw new IllegalStateException("@ValueSource can only be used with single-parameter test methods");
        }

        verifyOnlyOneValueTypeProvided(valueSource);

        Class<?> paramType = paramTypes[0];

        // Array of handlers for each value type
        ValueTypeHandler[] handlers = new ValueTypeHandler[] {
            new ValueTypeHandler(valueSource.strings().length, () -> handleStrings(valueSource, paramType)),
            new ValueTypeHandler(valueSource.chars().length, () -> handlePrimitives(valueSource::chars, paramType, char.class, Character.class, (arr, i) -> ((char[]) arr)[i])),
            new ValueTypeHandler(valueSource.booleans().length, () -> handlePrimitives(valueSource::booleans, paramType, boolean.class, Boolean.class, (arr, i) -> ((boolean[]) arr)[i])),
            new ValueTypeHandler(valueSource.bytes().length, () -> handlePrimitives(valueSource::bytes, paramType, byte.class, Byte.class, (arr, i) -> ((byte[]) arr)[i])),
            new ValueTypeHandler(valueSource.shorts().length, () -> handlePrimitives(valueSource::shorts, paramType, short.class, Short.class, (arr, i) -> ((short[]) arr)[i])),
            new ValueTypeHandler(valueSource.ints().length, () -> handlePrimitives(valueSource::ints, paramType, int.class, Integer.class, (arr, i) -> ((int[]) arr)[i])),
            new ValueTypeHandler(valueSource.longs().length, () -> handlePrimitives(valueSource::longs, paramType, long.class, Long.class, (arr, i) -> ((long[]) arr)[i])),
            new ValueTypeHandler(valueSource.floats().length, () -> handlePrimitives(valueSource::floats, paramType, float.class, Float.class, (arr, i) -> ((float[]) arr)[i])),
            new ValueTypeHandler(valueSource.doubles().length, () -> handlePrimitives(valueSource::doubles, paramType, double.class, Double.class, (arr, i) -> ((double[]) arr)[i])),
            new ValueTypeHandler(valueSource.classes().length, () -> handleClasses(valueSource, paramType))
        };

        for (ValueTypeHandler handler : handlers) {
            if (handler.length > 0) {
                return handler.supplier.get();
            }
        }

        throw new IllegalStateException("No values provided in @ValueSource annotation");
    }

    /**
     * Helper class for value type handling.
     */
    @AllArgsConstructor
    private static class ValueTypeHandler {
        /**
         * The length of the value array.
         */
        final int length;
        /**
         * The supplier that provides the boxed value array.
         */
        final java.util.function.Supplier<Object[]> supplier;
    }

    /**
     * Functional interface to retrieve an element from an array at a given index.
     * Used to abstract array element access for different primitive types.
     */
    @FunctionalInterface
    private interface ArrayElementGetter {
        /**
         * Returns the element at the specified index from the given array.
         *
         * @param arr The array to access (must be of the correct primitive type).
         * @param i The index of the element to retrieve.
         * @return The element at the specified index, boxed as an Object.
         */
        Object get(Object arr, int i);
    }

    /**
     * Handles string values for @ValueSource.
     * @param valueSource The ValueSource annotation.
     * @param paramType The parameter type of the test method.
     * @return Array of string values.
     * @throws IllegalStateException if the parameter type is incorrect.
     */
    private static Object[] handleStrings(ValueSource valueSource, Class<?> paramType) {
        checkParamType(String.class.equals(paramType),
            "Test method parameter must be String when using strings() in @ValueSource");
        return valueSource.strings();
    }

    /**
     * Handles primitive values for @ValueSource.
     * @param valueSourceMethod Supplier method to get the primitive array from ValueSource.  Ex. valueSource::ints
     * @param actualParamType The parameter type of the test method.
     * @param primitiveType The expected primitive type (e.g., int.class).
     * @param boxedType The corresponding boxed type (e.g., Integer.class).
     * @param getter Lambda to get the primitive value at index i.
     * @return Array of boxed values.
     * @throws IllegalStateException if the parameter type is incorrect.
     */
    private static Object[] handlePrimitives(@NonNull Supplier<Object> valueSourceMethod, @NonNull Class<?> actualParamType,
                                             @NonNull Class<?> primitiveType, @NonNull Class<?> boxedType, @NonNull ArrayElementGetter getter) {
        String message = String.format("Test method parameter must be %s/%s when using %ss() in @ValueSource",
                primitiveType.getSimpleName(), boxedType.getSimpleName(), primitiveType.getSimpleName());
        checkParamType(primitiveType.equals(actualParamType) || Character.class.equals(actualParamType), message);
        return boxArray(valueSourceMethod.get(), i -> (Object[]) Array.newInstance(boxedType, i), getter);
    }

    /**
     * Handles class values for @ValueSource.
     * @param valueSource The ValueSource annotation.
     * @param paramType The parameter type of the test method.
     * @return Array of Class values.
     * @throws IllegalStateException if the parameter type is incorrect.
     */
    private static Object[] handleClasses(ValueSource valueSource, Class<?> paramType) {
        checkParamType(Class.class.equals(paramType) || Class.class.isAssignableFrom(paramType),
            "Test method parameter must be Class when using classes() in @ValueSource");
        return valueSource.classes();
    }

    /**
     * Verifies that only one value type is provided in the ValueSource annotation.
     * @param valueSource The ValueSource annotation.
     * @throws IllegalStateException if more than one value type is provided.
     */
    private static void verifyOnlyOneValueTypeProvided(ValueSource valueSource) {
        // Count how many value arrays are non-empty
        int nonEmptyCount = (int) Stream.of(
                        valueSource.strings().length,
                        valueSource.chars().length,
                        valueSource.booleans().length,
                        valueSource.bytes().length,
                        valueSource.shorts().length,
                        valueSource.ints().length,
                        valueSource.longs().length,
                        valueSource.floats().length,
                        valueSource.doubles().length,
                        valueSource.classes().length
                )
                .filter(len -> len > 0)
                .count();

        if (nonEmptyCount > 1) {
            throw new IllegalStateException("@ValueSource must have exactly one value parameter set (e.g., only strings, only ints, etc.)");
        }
    }

    /**
     * Checks the parameter type condition and throws an IllegalStateException with the given message if the condition is false.
     *
     * @param condition The condition to check.
     * @param message The exception message if the condition is false.
     * @throws IllegalStateException If the condition is false.
     */
    private static void checkParamType(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * Generic method to box primitive arrays to their wrapper type arrays.
     *
     * @param arr The primitive array (as Object).
     * @param arrayConstructor Constructor reference for the wrapper array.
     * @param getter Lambda to get the primitive value at index i.
     * @return Boxed array as Object[].
     */
    private static <T> Object[] boxArray(Object arr, @NonNull IntFunction<T[]> arrayConstructor, @NonNull ArrayElementGetter getter) {
        int length = Array.getLength(arr);
        Object[] result = arrayConstructor.apply(length);

        for (int i = 0; i < length; i++) {
            result[i] = getter.get(arr, i);
        }

        return result;
    }
}
