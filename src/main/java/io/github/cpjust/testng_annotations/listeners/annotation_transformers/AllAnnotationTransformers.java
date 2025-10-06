package io.github.cpjust.testng_annotations.listeners.annotation_transformers;

import io.github.cpjust.testng_annotations.annotations.CsvSource;
import io.github.cpjust.testng_annotations.annotations.ValueSource;
import io.github.cpjust.testng_annotations.annotations.NullSource;
import io.github.cpjust.testng_annotations.annotations.EmptySource;
import io.github.cpjust.testng_annotations.annotations.NullAndEmptySource;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * TestNG annotation transformer that processes all custom annotation transformers like: {@link CsvSource},
 * {@link ValueSource}, {@link NullSource}, {@link EmptySource}, and {@link NullAndEmptySource}
 * to set up data providers for test methods.
 * <p>
 * Since you cannot use multiple transformers in TestNG, you need to register this transformer if you want to use multiple
 * of the provided annotations. You can do this by adding the fully qualified class name to a file named
 * 'org.testng.IAnnotationTransformer' in the 'META-INF/services' directory of your resources.
 */
public class AllAnnotationTransformers implements IAnnotationTransformer {
    /**
     * Transforms test methods annotated with {@link CsvSource}, {@link ValueSource}, {@link NullSource},
     * {@link EmptySource}, and {@link NullAndEmptySource} to use a data provider.
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

        if (CsvSourceListener.isCsvSourcePresent(testMethod)) {
            annotation.setDataProvider(CsvSourceListener.CSV_SOURCE_PROVIDER);
            annotation.setDataProviderClass(CsvSourceListener.class);
        } else if (ValueSourceListener.isValueSourcePresent(testMethod)) {
            annotation.setDataProvider(ValueSourceListener.VALUE_SOURCE_PROVIDER);
            annotation.setDataProviderClass(ValueSourceListener.class);
        }
    }
}
