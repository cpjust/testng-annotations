package io.github.cpjust.testng_annotations;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

/**
 * Utility class for test-related helper methods.
 */
@UtilityClass
public class TestUtils {
    /**
     * Gets the name of the method that called this function.
     *
     * @return The name of the method that called this function.
     */
    public static String getCurrentMethodName() {
        return getCurrentMethodName(1);
    }

    /**
     * Gets the name of the method that called this function.
     *
     * @param params The parameters to include in the method signature.
     * @return The name of the method that called this function.
     */
    public static String getCurrentMethodNameWithParams(@NonNull Object... params) {
        String paramString = Arrays.stream(params).map(Object::toString).collect(Collectors.joining(", "));
        return getCurrentMethodName(1) + String.format("(%s)", paramString);
    }

    /**
     * Gets the name of the method that called this function.
     *
     * @param depth The depth in the stack trace to retrieve the method name from (0 = immediate caller).
     * @return The name of the method that called this function.
     */
    private static String getCurrentMethodName(int depth) {
        int stackDepth = 2 + depth; // Adjust depth to account for this method and getStackTrace
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        assertThat("Not enough elements in stack trace!", stackTraceElements.length, greaterThanOrEqualTo(stackDepth));
        return stackTraceElements[stackDepth].getMethodName();
    }
}
