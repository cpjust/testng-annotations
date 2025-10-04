package io.github.cpjust.testng_annotations.listeners;

import io.github.cpjust.testng_annotations.annotations.EmptySource;
import io.github.cpjust.testng_annotations.annotations.NullAndEmptySource;
import io.github.cpjust.testng_annotations.annotations.NullSource;
import io.github.cpjust.testng_annotations.annotations.ValueSource;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;

import static io.github.cpjust.testng_annotations.TestUtils.getCurrentMethodName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

@Slf4j
public class NullAndEmptySourceIT {
    private final Map<String, List<Object>> testNamesAndValues = new HashMap<>();
    private final Map<String, List<Object>> testNamesAndExpectedValues = Map.of(
            "testNullSourceAndEmptySourceAndValueSource_stringValue_injectsExpectedStringValues", Arrays.asList(null, "", "value1", "value2"),
            "testNullAndEmptySourceAndValueSource_stringValue_injectsExpectedStringValues", Arrays.asList(null, "", "value1", "value2"),
            "testNullSourceAndEmptySourceAndNullAndEmptySourceAndValueSource_stringValue_noDuplicatesReturned", Arrays.asList(null, "", "value1"),
            "testNullSourceAndEmptySource_intArray_injectsExpectedIntArrays", Arrays.asList(null, new int[]{}),
            "testNullSourceAndEmptySource_listValue_injectsNullAndEmptyList", Arrays.asList(null, List.of())
    );

    //region EmptySource
    @Test
    @EmptySource
    public void testEmptySource_stringValue_injectsEmptyString(String value) {
        assertThat("Parameter should be empty string", value, is(""));
    }

    @Test
    @EmptySource
    public void testEmptySource_intArray_injectsEmptyArray(int[] arr) {
        assertThat("Parameter should be empty array", arr.length, is(0));
    }

    @Test
    @EmptySource
    public void testEmptySource_listValue_injectsEmptyList(List<String> list) {
        assertThat("Parameter should be empty list", list, empty());
    }

    @Test
    @EmptySource
    public void testEmptySource_setValue_injectsEmptySet(Set<String> set) {
        assertThat("Parameter should be empty set", set, empty());
    }

    @Test
    @EmptySource
    public void testEmptySource_queueValue_injectsEmptyQueue(Queue<String> queue) {
        assertThat("Parameter should be empty queue", queue, empty());
    }
    //endregion EmptySource

    //region NullSource
    @Test
    @NullSource
    public void testNullSource_stringValue_injectsNull(String value) {
        assertThat("Parameter should be null", value, nullValue());
    }
    //endregion NullSource

    //region NullAndEmptySource
    @Test
    @NullAndEmptySource
    public void testNullAndEmptySource_stringValue_injectsNullAndEmptyString(String value) {
        // This test will be run twice: once with null, once with ""
        if (value == null) {
            assertThat("Parameter should be null", value, nullValue());
        } else {
            assertThat("Parameter should be empty string", value, is(""));
        }
    }

    @Test
    @NullAndEmptySource
    public void testNullAndEmptySource_listValue_injectsNullAndEmptyList(List<String> list) {
        // This test will be run twice: once with null, once with empty list
        if (list == null) {
            assertThat("Parameter should be null", list, nullValue());
        } else {
            assertThat("Parameter should be empty list", list, empty());
        }
    }
    //endregion NullAndEmptySource

    //region multiple annotations
    @Test
    @NullSource
    @EmptySource
    @ValueSource(strings = {"value1", "value2"})
    public void testNullSourceAndEmptySourceAndValueSource_stringValue_injectsExpectedStringValues(String value) {
        validateTestNameAndValue(getCurrentMethodName(), value);
    }

    @Test
    @NullAndEmptySource
    @ValueSource(strings = {"value1", "value2"})
    public void testNullAndEmptySourceAndValueSource_stringValue_injectsExpectedStringValues(String value) {
        validateTestNameAndValue(getCurrentMethodName(), value);
    }

    @Test
    @NullSource
    @EmptySource
    @NullAndEmptySource
    @ValueSource(strings = {"", "", "value1"})
    public void testNullSourceAndEmptySourceAndNullAndEmptySourceAndValueSource_stringValue_noDuplicatesReturned(String value) {
        validateTestNameAndValue(getCurrentMethodName(), value);
    }

    @Test
    @NullSource
    @EmptySource
    public void testNullSourceAndEmptySource_intArray_injectsExpectedIntArrays(int[] values) {
        String testName = getCurrentMethodName();

        if (values != null) {
            // NOTE: We can't use assertThat(values, is(new int[]{})) because it checks for reference equality of arrays,
            // so just ensure it's an empty array.
            assertThat("Array should be empty", values.length, is(0));

            testNamesAndValues.putIfAbsent(testName, new ArrayList<>());
            List<Object> actualValues = testNamesAndValues.get(testName);
            actualValues.add(values); // Add the empty array instance.
        } else {
            validateTestNameAndValue(testName, values);
        }
    }

    @Test
    @EmptySource
    @NullSource
    public void testNullSourceAndEmptySource_listValue_injectsNullAndEmptyList(List<String> list) {
        validateTestNameAndValue(getCurrentMethodName(), list);
    }

    @Test(priority = 2)
    public void verifyMultipleAnnotationsTests() {
        log.info("Verifying tests with multiple annotations were run correctly: {}", testNamesAndValues);

        // Ensure all expected tests were run
        testNamesAndExpectedValues.forEach((testName, expectedValues) -> {
            assertThat(String.format("Test '%s' was not run!", testName), testNamesAndValues, hasKey(testName));
            List<Object> actualValues = testNamesAndValues.get(testName);
            assertThat(String.format("Test '%s' was not run with all expected values!", testName), actualValues, containsInAnyOrder(expectedValues.toArray()));
        });
    }
    //endregion multiple annotations

    /**
     * Validates that the given value for the given test name is one of the expected values and was not already used.
     *
     * @param testName The name of the test method.
     * @param value    The value to validate.
     */
    private void validateTestNameAndValue(@NonNull String testName, Object value) {
        List<Object> expectedValues = testNamesAndExpectedValues.get(testName);

        testNamesAndValues.putIfAbsent(testName, new ArrayList<>());
        List<Object> values = testNamesAndValues.get(testName);
        log.info("Test '{}' has expected values: {} and current values: {}", testName, expectedValues, values);

        // Verify that the value is one of the expected values
        assertThat(String.format("Test '%s' was run with unexpected value: %s", testName, value), value, in(expectedValues));

        // Verify that the value was not already used
        assertThat(String.format("Test '%s' was run more than once with the same value: %s", testName, value), values, not(containsInAnyOrder(value)));

        log.info("Adding value '{}' for test '{}'", value, testName);
        values.add(value);
    }
}
