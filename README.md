# testng-annotations
The testng-annotations project contains some extra annotations that are useful when running tests in TestNG.

## Requirements:
- Minimum Java version: 11
- TestNG 7.x
  - NOTE: TestNG upgraded to Java 11 starting from 7.6.0.
  - If you need to port this project to Java 8, you won't be able to go beyond TestNG 7.5.

## Quick Start

1. Add dependency:
```xml
<dependency>
    <groupId>io.github.cpjust</groupId>
    <artifactId>testng-annotations</artifactId>
    <version>1.1.0</version>
    <scope>test</scope>
</dependency>
```

2. Register listeners (pick one):
- Add to test class:
```java
@Listeners({IncludeOnEnvListener.class, ExcludeOnEnvListener.class})
```
- OR add to `src/test/resources/META-INF/services/org.testng.ITestNGListener`:
```
io.github.cpjust.testng_annotations.listeners.IncludeOnEnvListener
io.github.cpjust.testng_annotations.listeners.ExcludeOnEnvListener
io.github.cpjust.testng_annotations.listeners.ValueSourceListener
```

3. Use annotations in tests:
```java
// Skip in production
@ExcludeOnEnv("prod")
@Test
public void productionSafeTest() {
    // Won't run when -Denv=prod
}

// Only run in dev/qa
@IncludeOnEnv(value = {"dev", "qa"}, propertyName = "environment")
@Test
public void nonProductionTest() {
    // Only runs with -Denvironment=dev or -Denvironment=qa
}

// Using CSV string with delimiter
@ExcludeOnEnv(value = {"dev,qa,prod"}, delimiter = ",")
@Test
public void csvDelimitedTest() {
    // Won't run when -Denv=dev, -Denv=qa, or -Denv=prod
}

// Complex example with inheritance
@ExcludeOnEnv("ci")
public class BaseTest {
    // Class-wide exclusion
}

public class MyTests extends BaseTest {
    @IncludeOnEnv("staging")
    @Test // Combines class and method rules
    public void stagingOnlyTest() {
        // Runs if:
        // - Not in CI (-Denv=ci)
        // - In staging (-Denv=staging)
    }
}

// Using @ValueSource for parameterized tests
@Test
@ValueSource(strings = {"test1", "test2"})
public void testWithStrings(String value) {
    // Test runs twice, once with "test1" and once with "test2"
}

@Test
@ValueSource(ints = {1, 2, 3})
public void testWithInts(int value) {
    // Test runs three times with values 1, 2, and 3
}
```

## Annotations

### @ExcludeOnEnv
This annotation will exclude tests if the current environment (as defined by a Java property) matches one of the
environments to be excluded.  This annotation will not just mark a test as skipped, it will not even attempt to run the
test and the test will not appear in the list of tests that were run if the test was excluded.
NOTE: The environment names are compared case-insensitively.

**CSV string support:**  
You can use the `delimiter` attribute to split values in the `value` array as CSV strings. For example:
```java
@ExcludeOnEnv(value = {"dev,qa,prod"}, delimiter = ",")
```
This is equivalent to `@ExcludeOnEnv(value = {"dev", "qa", "prod"})`.

Ex. If a test is annotated with `@ExcludeOnEnv(value = {"Stage", "Prod"}, propertyName = "environment")` and you run
with the `-Denvironment=Prod` option, the test will be excluded.  If you omit the `propertyName` attribute, it will use
`"env"` as the default property to check.

Tests should be excluded using the following rules:

|                     | No class annotation: | Include by class: | Exclude by class: |
| ------------------- | -------------------- | ----------------- | ----------------- |
| No test annotation: |    INCLUDE           |   INCLUDE         |   EXCLUDE         |
| Include by test:    |    INCLUDE           |   INCLUDE         |   EXCLUDE         |
| Exclude by test:    |    EXCLUDE           |   EXCLUDE         |   EXCLUDE         |

### @IncludeOnEnv
This annotation will include tests if the current environment (as defined by a Java property) matches one of the
environments to be included.  This annotation will not just mark a test as skipped, it will not even attempt to run the
test and the test will not appear in the list of tests that were run if the test was not included.
NOTE: The environment names are compared case-insensitively.

**CSV string support:**  
You can use the `delimiter` attribute to split values in the `value` array as CSV strings. For example:
```java
@IncludeOnEnv(value = {"dev,qa,prod"}, delimiter = ",")
```
This is equivalent to `@IncludeOnEnv(value = {"dev", "qa", "prod"})`.

Ex. If a test is annotated with `@IncludeOnEnv(value = {"Stage", "Prod"}, propertyName = "environment")` and you run
with the `-Denvironment=Prod` option, the test will be included.  If you omit the `propertyName` attribute, it will use
`"env"` as the default property to check.

Tests should be included using the following rules:

|                     | No class annotation: | Include by class: | Exclude by class: |
| ------------------- | -------------------- | ----------------- | ----------------- |
| No test annotation: |    INCLUDE           |   INCLUDE         |   EXCLUDE         |
| Include by test:    |    INCLUDE           |   INCLUDE         |   INCLUDE         |
| Exclude by test:    |    EXCLUDE           |   EXCLUDE         |   EXCLUDE         |

### @ValueSource
Provides a simple way to parameterize tests with literal values. Similar to JUnit's ValueSource, this annotation runs the test method once for each provided value.
This annotation is less powerful than TestNG's native DataProvider, but is easier to use for simple cases, and keeps the test data with the test method.

Supported value types:
- `strings()`: String values
- `chars()`: Char values
- `booleans()`: Boolean values
- `bytes()`: Byte values
- `shorts()`: Short values
- `ints()`: Integer values
- `longs()`: Long values
- `floats()`: Float values
- `doubles()`: Double values
- `classes()`: Class values

**Requirements:**
- Test method must have exactly one parameter.
- Parameter type must match the value type being provided.
- At least one value must be provided.

**Example:**
```java
@Test
@ValueSource(strings = {"hello", "world"})
public void testStrings(String value) {
    assertNotNull(value);
}

@Test
@ValueSource(ints = {1, 2, 3})
public void testInts(int value) {
    assertTrue(value > 0);
}

@Test
@ValueSource(bytes = {1, 2})
public void testBytes(byte value) {
    // ...
}

@Test
@ValueSource(chars = {'a', 'b'})
public void testChars(char value) {
    // ...
}

@Test
@ValueSource(shorts = {10, 20})
public void testShorts(short value) {
    // ...
}

@Test
@ValueSource(floats = {1.5f, 2.5f})
public void testFloats(float value) {
    // ...
}

@Test
@ValueSource(classes = {String.class, Integer.class})
public void testClasses(Class<?> value) {
    // ...
}
```

#### Listener: ValueSourceListener

This is the listener for TestNG tests that are annotated with `@ValueSource`.

**Registration options: (pick one)**
- Add to `src/test/resources/META-INF/services/org.testng.ITestNGListener` (RECOMMENDED):
```
io.github.cpjust.testng_annotations.listeners.ValueSourceListener
```
- or register it in the testng.xml file.  Ex.
```xml
<listeners>
    <listener class-name="io.github.cpjust.testng_annotations.listeners.ValueSourceListener"/>
</listeners>
```

**Notes:**
- If you do not register the listener globally, you must specify the data provider in your @Test annotation:
  ```java
  @Test(dataProvider = "valueSourceProvider", dataProviderClass = ValueSourceListener.class)
  @ValueSource(strings = {"foo", "bar"})
  public void testWithStrings(String value) { ... }
  ```
- If registered globally, you can simply use `@Test` and `@ValueSource` together.

## Listeners:

### ExcludeOnEnvListener
This is the listener for TestNG tests that are annotated with `@ExcludeOnEnv`.
To register this listener, either define it in the `src/test/resources/META-INF/services/org.testng.ITestNGListener`
file (by adding `io.github.cpjust.testng_annotations.listeners.ExcludeOnEnvListener` to the file)
or add the `@Listeners({ExcludeOnEnvListener.class})` annotation to the test class.

### IncludeOnEnvListener
This is the listener for TestNG tests that are annotated with `@IncludeOnEnv`.
To register this listener, either define it in the `src/test/resources/META-INF/services/org.testng.ITestNGListener`
file (by adding `io.github.cpjust.testng_annotations.listeners.IncludeOnEnvListener` to the file)
or add the `@Listeners({IncludeOnEnvListener.class})` annotation to the test class.

### ValueSourceListener
This is the listener for TestNG tests that are annotated with `@ValueSource`.
To register this listener, either define it in the `src/test/resources/META-INF/services/org.testng.ITestNGListener`
file (by adding `io.github.cpjust.testng_annotations.listeners.ValueSourceListener` to the file)
or add the listener to the testng.xml file.

**Usage notes:**
- The listener automatically provides the correct data provider for methods annotated with `@ValueSource`.
- If not registered globally, you must specify `dataProvider = "valueSourceProvider", dataProviderClass = ValueSourceListener.class` in your `@Test` annotation.
- You cannot register this listener in the `@Listeners` annotation.

## Notes on annotation implementations:
After implementing the IAnnotationTransformer & IMethodInterceptor interfaces, getting TestNG to actually run them was tricky.
- IAnnotationTransformer only executes if it's defined in the `src/test/resources/META-INF/services/org.testng.ITestNGListener` file.
- IMethodInterceptor executes if it's defined in the `src/test/resources/META-INF/services/org.testng.ITestNGListener` file or 
  if you add the `@Listeners({ExcludeOnEnvListener.class})` annotation to the test class.
- The `env` system property is only picked up if defined with `-Denv` on the command line or in the `@BeforeSuite`, but not with `@BeforeClass`.

## Building and deploying:
To just build, run `mvn clean install`

To build with signed artifacts, run `mvn clean install -Psign-artifacts`

You should follow the instructions on https://central.sonatype.org/publish/publish-maven/#gpg-signed-components if you run into problems.
You may need to add configuration similar to the following to your `settings.xml` file:

```xml
<profiles>
  <profile>
    <id>testng_annotations_profile_id</id>
    <activation>
      <activeByDefault>true</activeByDefault>
    </activation>
    <properties>
      <gpg.executable>gpg</gpg.executable>
      <gpg.passphrase>xxx your passphrase here xxx</gpg.passphrase>
      <gpg.keyname>0x12345678</gpg.keyname>
    </properties>
  </profile>
</profiles>
```

Where the value of the `gpg.keyname` is found in the 'sig 3' line of the output of this command: `gpg --list-signatures --keyid-format 0xshort`

NOTE: If the signing fails with a "Bad passphrase" error, and you have the right passphrase and keyname, you may need to also set a
`MAVEN_GPG_PASSPHRASE` environment variable to your passphrase value.  Ex. `export MAVEN_GPG_PASSPHRASE="your passphrase"`

Deploy with `mvn clean deploy -Psign-artifacts`

## Troubleshooting

| Issue | Solution |
|-------|-----------------------------------------------------------------------------------------------------------------------------------------|
| Listeners not working | Verify registration via either:<br>- `@Listeners` annotation on test class, OR<br>- `META-INF/services/org.testng.ITestNGListener` file |
| Environment not detected | Set properties:<br>- Via command line: `-Denv=value`<br>- In `@BeforeSuite` method<br>Note: `@BeforeClass` is too late! |
| Unexpected test execution | Check annotation precedence rules above<br>Verify property name matches in annotations |
| IllegalArgumentException | Verify you aren't passing a blank or empty string as tha `value` or `propertyName` annotation properties |
| Signing failures | 1. Verify GPG is installed<br>2. Set `MAVEN_GPG_PASSPHRASE` env var<br>3. Check `settings.xml` config |
| The @Test priority attribute is ignored | This is a bug in TestNG versions below 7.5 |
| Other issues | Try upgrading/downgrading TestNG version<br>Check Java version compatibility |

## Best Practices

1. **Consistent Environments**:
  - Standardize on a property name (e.g. always use "env")
  - Document expected values (dev, qa, prod)

2. **Annotation Strategy**:
  - Use class-level for broad rules
  - Method-level for exceptions
  - Avoid mixing @Include and @Exclude
  - Do not use @ValueSource and TestNG DataProvider on the same method

3. **Debugging**:
```java
@BeforeSuite
public void logEnvironment() {
    System.getProperties().forEach((k,v) ->
        log.info("Prop: {}={}", k, v));
}
```

4. **CI Integration**:
```yaml
# Sample GitHub Actions
jobs:
  test:
    env:
      ENV: ci
    steps:
      - run: mvn test -Denv=$ENV
```
