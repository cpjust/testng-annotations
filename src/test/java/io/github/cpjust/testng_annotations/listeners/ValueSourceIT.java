package io.github.cpjust.testng_annotations.listeners;

import io.github.cpjust.testng_annotations.annotations.ValueSource;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Slf4j
// NOTE: Using '@Listeners(ValueSourceListener.class)' here doesn't work for some reason.
// If you want to use the ValueSourceListener, you need to register it in META-INF/services/org.testng.ITestNGListener
public class ValueSourceIT {
    private static final String VALUE_SHOULD_NOT_BE_REPEATED = "Value should not be repeated: ";

    private final Set<String> seenStrings = new HashSet<>();
    private final Set<Character> seenChars = new HashSet<>();
    private final Set<Boolean> seenBooleans = new HashSet<>();
    private final Set<Byte> seenBytes = new HashSet<>();
    private final Set<Short> seenShorts = new HashSet<>();
    private final Set<Integer> seenInts = new HashSet<>();
    private final Set<Long> seenLongs = new HashSet<>();
    private final Set<Float> seenFloats = new HashSet<>();
    private final Set<Double> seenDoubles = new HashSet<>();
    private final Set<Class<?>> seenClasses = new HashSet<>();

    @Test
    public void testNoValueSource() {
        log.info("This test does not use ValueSource and should run only once.");
        Assert.assertTrue(true);
    }

    // NOTE: We only need to provide the dataProvider and dataProviderClass in the @Test annotation if we don't register the ValueSourceListener
    // in the resources/META-INF/services/org.testng.ITestNGListener file.
    @Test(dataProvider = "valueSourceProvider", dataProviderClass = ValueSourceListener.class)
    @ValueSource(strings = {"test1", "test2", "test3"})
    public void testStringValues(String value) {
        log.info("Testing string value: {}", value);
        assertNotNull(value, "Value should not be null");
        assertThat("Wrong value!", value, startsWith("test"));
        assertTrue(seenStrings.add(value), VALUE_SHOULD_NOT_BE_REPEATED + value);
    }

    // As you can see by the rest of the tests, we don't need to provide the dataProvider and dataProviderClass in the @Test annotation
    // since we registered the ValueSourceListener in the resources/META-INF/services/org.testng.ITestNGListener file.
    @Test
    @ValueSource(chars = {'a', 'b', 'c'})
    public void testCharValues(char value) {
        log.info("Testing char value: {}", value);
        assertTrue(seenChars.add(value), VALUE_SHOULD_NOT_BE_REPEATED + value);
    }

    @Test
    @ValueSource(booleans = {true, false})
    public void testBooleanValues(boolean value) {
        log.info("Testing boolean value: {}", value);
        assertTrue(seenBooleans.add(value), VALUE_SHOULD_NOT_BE_REPEATED + value);
    }

    @Test
    @ValueSource(bytes = {1, 2, 3})
    public void testByteValues(byte value) {
        log.info("Testing byte value: {}", value);
        assertTrue(seenBytes.add(value), VALUE_SHOULD_NOT_BE_REPEATED + value);
    }

    @Test
    @ValueSource(shorts = {10, 20, 30})
    public void testShortValues(short value) {
        log.info("Testing short value: {}", value);
        assertTrue(seenShorts.add(value), VALUE_SHOULD_NOT_BE_REPEATED + value);
    }

    @Test
    @ValueSource(ints = {1, 2, 3})
    public void testIntValues(int value) {
        log.info("Testing int value: {}", value);
        assertTrue(seenInts.add(value), VALUE_SHOULD_NOT_BE_REPEATED + value);
    }

    @Test
    @ValueSource(longs = {100L, 200L, 300L})
    public void testLongValues(long value) {
        log.info("Testing long value: {}", value);
        assertTrue(seenLongs.add(value), VALUE_SHOULD_NOT_BE_REPEATED + value);
    }

    @Test
    @ValueSource(floats = {1.5f, 2.5f, 3.5f})
    public void testFloatValues(float value) {
        log.info("Testing float value: {}", value);
        assertTrue(seenFloats.add(value), VALUE_SHOULD_NOT_BE_REPEATED + value);
    }

    @Test
    @ValueSource(doubles = {1.1, 2.2, 3.3})
    public void testDoubleValues(double value) {
        log.info("Testing double value: {}", value);
        assertTrue(seenDoubles.add(value), VALUE_SHOULD_NOT_BE_REPEATED + value);
    }

    @Test
    @ValueSource(classes = {String.class, Integer.class, Double.class})
    public void testClassValues(Class<?> value) {
        log.info("Testing class value: {}", value);
        assertTrue(seenClasses.add(value), VALUE_SHOULD_NOT_BE_REPEATED + value);
    }

    @Test(priority = 100)
    public void verifyAllValues() {
        Assert.assertEquals(seenStrings.size(), 3, "Expected 3 unique string values");
        Assert.assertEquals(seenChars.size(), 3, "Expected 3 unique char values");
        Assert.assertEquals(seenBooleans.size(), 2, "Expected 2 unique boolean values");
        Assert.assertEquals(seenBytes.size(), 3, "Expected 3 unique byte values");
        Assert.assertEquals(seenShorts.size(), 3, "Expected 3 unique short values");
        Assert.assertEquals(seenInts.size(), 3, "Expected 3 unique int values");
        Assert.assertEquals(seenLongs.size(), 3, "Expected 3 unique long values");
        Assert.assertEquals(seenFloats.size(), 3, "Expected 3 unique float values");
        Assert.assertEquals(seenDoubles.size(), 3, "Expected 3 unique double values");
        Assert.assertEquals(seenClasses.size(), 3, "Expected 3 unique class values");
    }
}
