package io.github.cpjust.testng_annotations.listeners.annotation_transformers;

import de.siegmar.fastcsv.reader.CommentStrategy;
import de.siegmar.fastcsv.reader.CsvRecordHandler;
import de.siegmar.fastcsv.reader.FieldModifiers;
import io.github.cpjust.testng_annotations.annotations.CsvSource;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRecord;

import java.io.IOException;

/**
 * TestNG listener that processes {@link CsvSource} annotations and converts them into data provider parameters.
 */
@Slf4j
public class CsvSourceListener implements IAnnotationTransformer {
    static final String CSV_SOURCE_PROVIDER = "csvSourceProvider";

    /**
     * Transforms test methods annotated with {@link CsvSource} to use a data provider.
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

        if (isCsvSourcePresent(testMethod)) {
            annotation.setDataProvider(CSV_SOURCE_PROVIDER);
            annotation.setDataProviderClass(CsvSourceListener.class);
        }
    }

    /**
     * Checks if the given method is annotated with {@link CsvSource}.
     *
     * @param method The test method.
     * @return True if the method is annotated with {@link CsvSource}, false otherwise.
     */
    public static boolean isCsvSourcePresent(@NonNull Method method) {
        return method.isAnnotationPresent(CsvSource.class);
    }

    /**
     * Data provider that supplies parameter values for methods annotated with {@link CsvSource}.
     *
     * @param method The test method.
     * @return A 2D array of parameter values.
     */
    @DataProvider(name = CSV_SOURCE_PROVIDER)
    public static Object[][] csvSourceProvider(Method method) {
        return provideValues(method);
    }

    /**
     * Provides parameter values for a method annotated with {@link CsvSource}.
     *
     * @param method The test method.
     * @return A 2D array of parameter values.
     */
    public static Object[][] provideValues(@NonNull Method method) {
        CsvSource csvSource = method.getAnnotation(CsvSource.class);

        if (csvSource == null) {
            throw new IllegalStateException("No @CsvSource annotation found on method: " + method.getName());
        }

        validateCsvSourceParameters(csvSource);

        // Ensure all parameters are Strings
        Class<?>[] parameterTypes = method.getParameterTypes();

        for (int i = 0; i < parameterTypes.length; ++i) {
            if (!parameterTypes[i].equals(String.class)) {
                throw new IllegalStateException(String.format(
                        "Parameter at index %d of method '%s' does not match method parameter type (expected: String, found: %s)",
                        i, method.getName(), parameterTypes[i].getSimpleName()
                ));
            }
        }

        String[] lines = csvSource.value();

        if (lines.length == 0) {
            throw new IllegalStateException("No values provided in @CsvSource annotation");
        }

        int paramCount = method.getParameterCount();
        Object[][] result = new Object[lines.length][];

        for (int i = 0; i < lines.length; ++i) {
            List<String> parsed = null;

            // First try to parse the lines.
            try {
                parsed = parseCsvLineWithFastCsv(lines[i], csvSource);
            } catch (IOException e) {
                throw new IllegalStateException(String.format("Error parsing value: '%s'", lines[i]), e);
            }

            // Then validate that the number of parsed parameters matches the method's parameter count.
            if (parsed.size() != paramCount) {
                throw new IllegalStateException(String.format(
                        "CSV line at index %d does not match method parameter count (parsed %d params, but method takes %d params): %s",
                        i, parsed.size(), paramCount, lines[i]
                ));
            }

            // Finally, convert the List<String> to Object[] for a TestNG @DataProvider.
            result[i] = parsed.toArray(new Object[0]);
        }

        return result;
    }

    /**
     * Validates the parameters of the CsvSource annotation.
     *
     * @param csvSource The CsvSource annotation.
     */
    private static void validateCsvSourceParameters(@NonNull CsvSource csvSource) {
        char delimiter = csvSource.delimiter();
        char quoteCharacter = csvSource.quoteCharacter();

        if (delimiter == '\n' || delimiter == '\r') {
            throw new IllegalArgumentException("CsvSource delimiter cannot be a newline or carriage return character");
        }

        if (delimiter == quoteCharacter) {
            throw new IllegalArgumentException("CsvSource delimiter cannot be the same as the quote character");
        }
    }

    /**
     * Parses a single CSV line using the FastCSV library according to the settings in the CsvSource annotation.
     *
     * @param line      The CSV line to parse.
     * @param csvSource The CsvSource annotation containing parsing settings.
     * @return A list of parsed values.
     * @throws IOException If an error occurs during parsing.
     */
    private static List<String> parseCsvLineWithFastCsv(@NonNull String line, @NonNull CsvSource csvSource) throws IOException {
        List<String> result = new ArrayList<>();

        // This is needed because FastCSV doesn't consider a string as being quoted if there is whitespace before the first quote (ex. " ' abc'").
        if (csvSource.trimWhitespace()) {
            line = line.trim();
        }

        CsvRecordHandler handler = CsvRecordHandler.builder()
                .fieldModifier(csvSource.trimWhitespace() ? FieldModifiers.STRIP : FieldModifiers.NOP)
                .build();

        try (CsvReader<CsvRecord> csvReader = CsvReader.builder()
                // Specifies whether the presence of characters between a closing quote and a field separator
                // or the end of a line should be treated as an error or not.
                .acceptCharsAfterQuotes(true) // Default is true, but being explicit.
                // NOTE: Even though CommentStrategy is NONE, it still fails if delimiter or quote char is same as comment char,
                // so set comment char to null for now to make it less likely that someone will use null for those.
                .commentCharacter('\0')
                .commentStrategy(CommentStrategy.NONE) // Default is NONE, but being explicit.
                // Defines if an optional BOM (Byte order mark) header should be detected.
                .detectBomHeader(false) // Default is false, but being explicit.
                .fieldSeparator(csvSource.delimiter())
                // Defines if an CsvParseException should be thrown if records do contain a different number of fields.
                .ignoreDifferentFieldCount(false)
                .quoteCharacter(csvSource.quoteCharacter())
                .skipEmptyLines(true) // Default is true, but being explicit.
                .build(handler, line)) {
            csvReader.forEach(csvRecord -> result.addAll(csvRecord.getFields()));
        }

        return result;
    }
}
