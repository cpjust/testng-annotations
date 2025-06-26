# testng-annotations
The testng-annotations project contains some extra annotations that are useful when running tests in TestNG.

## Requirements:
- Minimum Java version: 11
- TestNG 7.x
  - NOTE: TestNG upgraded to Java 11 starting from 7.6.0.
  - If you need to port this project to Java 8, you won't be able to go beyond TestNG 7.5.

## Annotations:

### @ExcludeOnEnv
This annotation will exclude tests if the current environment (as defined by a Java property) matches one of the
environments to be excluded.  This annotation will not just mark a test as skipped, it will not even attempt to run the
test and the test will not appear in the list of tests that were run if the test was excluded.

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
test and the test will not appear in the list of tests that were run if the test was excluded.

Ex. If a test is annotated with `@IncludeOnEnv(value = {"Stage", "Prod"}, propertyName = "environment")` and you run
with the `-Denvironment=Prod` option, the test will be included.  If you omit the `propertyName` attribute, it will use
`"env"` as the default property to check.

Tests should be included using the following rules:

|                     | No class annotation: | Include by class: | Exclude by class: |
| ------------------- | -------------------- | ----------------- | ----------------- |
| No test annotation: |    INCLUDE           |   INCLUDE         |   EXCLUDE         |
| Include by test:    |    INCLUDE           |   INCLUDE         |   INCLUDE         |
| Exclude by test:    |    EXCLUDE           |   EXCLUDE         |   EXCLUDE         |

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
