package io.github.cpjust.testng_annotations.listeners;

import io.github.cpjust.testng_annotations.annotations.ValueSource;
import io.github.cpjust.testng_annotations.annotations.NullSource;
import io.github.cpjust.testng_annotations.annotations.EmptySource;
import io.github.cpjust.testng_annotations.annotations.NullAndEmptySource;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * TestNG listener that processes @ValueSource annotations and converts them into data provider parameters.
 */
@Slf4j
public class ValueSourceListener implements IAnnotationTransformer {
    private static final String VALUE_SOURCE_PROVIDER = "valueSourceProvider";

    /**
     * Enum representing the possible value types in @ValueSource.
     */
    private enum ValueType {
        STRINGS,
        CHARS,
        BOOLEANS,
        BYTES,
        SHORTS,
        INTS,
        LONGS,
        FLOATS,
        DOUBLES,
        CLASSES
    }

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
        if (testMethod == null) {
            return; // Nothing to do if there's no test method.
        }

        if (testMethod.isAnnotationPresent(ValueSource.class) ||
            testMethod.isAnnotationPresent(NullSource.class) ||
            testMethod.isAnnotationPresent(EmptySource.class) ||
            testMethod.isAnnotationPresent(NullAndEmptySource.class)) {
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
        // Use a LinkedHashSet to maintain insertion order and avoid duplicates.
        Set<Object> resultSet = new LinkedHashSet<>();

        // Handle @NullSource
        if (method.isAnnotationPresent(NullSource.class)) {
            Class<?>[] paramTypes = method.getParameterTypes();

            if (paramTypes.length != 1) {
                throw new IllegalStateException("@NullSource can only be used with single-parameter test methods");
            }

            resultSet.add(null);
        }

        // Handle @EmptySource
        if (method.isAnnotationPresent(EmptySource.class)) {
            Class<?>[] paramTypes = method.getParameterTypes();

            if (paramTypes.length != 1) {
                throw new IllegalStateException("@EmptySource can only be used with single-parameter test methods");
            }

            Class<?> paramType = paramTypes[0];
            resultSet.add(getEmptyValue(paramType));
        }

        // Handle @NullAndEmptySource
        if (method.isAnnotationPresent(NullAndEmptySource.class)) {
            Class<?>[] paramTypes = method.getParameterTypes();

            if (paramTypes.length != 1) {
                throw new IllegalStateException("@NullAndEmptySource can only be used with single-parameter test methods");
            }

            Class<?> paramType = paramTypes[0];
            resultSet.add(null);
            resultSet.add(getEmptyValue(paramType));
        }

        // Handle @ValueSource
        if (method.isAnnotationPresent(ValueSource.class)) {
            Object[] values = getValueSourceValues(method);
            Collections.addAll(resultSet, values);
        }

        if (resultSet.isEmpty()) {
            // This would only happen if a test method is using this data provider without any @NullSource, @EmptySource,
            // @NullAndEmptySource or @ValueSource annotations.
            // Ex. @Test(dataProvider = "valueSourceProvider", dataProviderClass = ValueSourceListener.class)
            throw new IllegalStateException("No [@NullSource, @EmptySource, @NullAndEmptySource, @ValueSource] annotations found on method: " + method.getName());
        }

        return resultSet.toArray();
    }

    /**
     * Retrieves the values from the @ValueSource annotation on the given method.
     *
     * @param method The test method.
     * @return An array of values specified in the @ValueSource annotation.
     * @throws IllegalStateException if the annotation is misused or no values are provided.
     */
    private static Object[] getValueSourceValues(Method method) {
        ValueSource valueSource = method.getAnnotation(ValueSource.class);
        Class<?>[] paramTypes = method.getParameterTypes();

        if (paramTypes.length != 1) {
            throw new IllegalStateException("@ValueSource can only be used with single-parameter test methods");
        }

        Optional<ValueType> valueType = getSingleValueTypeProvided(valueSource);

        if (valueType.isEmpty()) {
            throw new IllegalStateException("No values provided in @ValueSource annotation");
        }

        Class<?> paramType = paramTypes[0];

        switch (valueType.get()) {
            case STRINGS:
                return handleStrings(valueSource, paramType);
            case CHARS:
                return handlePrimitives(valueSource::chars, paramType, char.class, Character.class, (arr, i) -> ((char[]) arr)[i]);
            case BOOLEANS:
                return handlePrimitives(valueSource::booleans, paramType, boolean.class, Boolean.class, (arr, i) -> ((boolean[]) arr)[i]);
            case BYTES:
                return handlePrimitives(valueSource::bytes, paramType, byte.class, Byte.class, (arr, i) -> ((byte[]) arr)[i]);
            case SHORTS:
                return handlePrimitives(valueSource::shorts, paramType, short.class, Short.class, (arr, i) -> ((short[]) arr)[i]);
            case INTS:
                return handlePrimitives(valueSource::ints, paramType, int.class, Integer.class, (arr, i) -> ((int[]) arr)[i]);
            case LONGS:
                return handlePrimitives(valueSource::longs, paramType, long.class, Long.class, (arr, i) -> ((long[]) arr)[i]);
            case FLOATS:
                return handlePrimitives(valueSource::floats, paramType, float.class, Float.class, (arr, i) -> ((float[]) arr)[i]);
            case DOUBLES:
                return handlePrimitives(valueSource::doubles, paramType, double.class, Double.class, (arr, i) -> ((double[]) arr)[i]);
            case CLASSES:
                return handleClasses(valueSource, paramType);
            default:
                throw new IllegalStateException("Unhandled value type in @ValueSource: " + valueType.get());
        }
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
     * Checks which value type is set in the ValueSource annotation.
     * @param valueSource The ValueSource annotation.
     * @return Optional containing the ValueType if exactly one is set, otherwise Optional.empty().
     */
    private static Optional<ValueType> getSingleValueTypeProvided(ValueSource valueSource) {
        int[] lengths = {
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
        };

        ValueType[] valueTypes = ValueType.values();
        int foundIndex = -1;

        for (int i = 0; i < lengths.length; ++i) {
            if (lengths[i] > 0) {
                if (foundIndex != -1) {
                    throw new IllegalStateException("@ValueSource must have exactly one value parameter set (e.g., only strings, only ints, etc.)");
                }
                foundIndex = i;
            }
        }

        return foundIndex != -1 ? Optional.of(valueTypes[foundIndex]) : Optional.empty();
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

    /**
     * Returns an empty value for the given parameter type (empty String, array, or collection).
     * @param paramType The parameter type.
     * @return An empty value appropriate for the type, or throws if not supported.
     */
    private static Object getEmptyValue(Class<?> paramType) {
        if (paramType.equals(String.class)) {
            return "";
        }

        if (paramType.isArray()) {
            return Array.newInstance(paramType.getComponentType(), 0);
        }

        if (Collection.class.isAssignableFrom(paramType)) {
            if (List.class.isAssignableFrom(paramType)) {
                return Collections.emptyList();
            }

            if (Set.class.isAssignableFrom(paramType)) {
                return Collections.emptySet();
            }

            if (Queue.class.isAssignableFrom(paramType)) {
                return new LinkedList<>();
            }

            return Collections.emptyList(); // fallback
        }

        if (Map.class.isAssignableFrom(paramType)) {
            return Collections.emptyMap();
        }

        throw new IllegalStateException("@EmptySource/@NullAndEmptySource not supported for parameter type: " + paramType.getName());
    }
}
