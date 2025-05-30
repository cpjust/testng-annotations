# testng-annotations
The testng-annotations project contains some extra annotations that are useful when running tests in TestNG.

## Annotations:

### @ExcludeOnEnv
This annotation will exclude tests if the current environment (as defined by a Java property) matches one of the
environments to be excluded.  This annotation will not just mark a test as skipped, it will not even attempt to run the
test and the test will not appear in the list of tests that were run if the test was excluded.

Ex. If a test is annotated with `@ExcludeOnEnv(value = {"Stage", "Prod"}, propertyName = "environment")` and you run
with the `-Denvironment=Prod` option, the test will be excluded.  If you omit the `propertyName` attribute, it will use
`"env"` as the default property to check.

## Listeners:

### ExcludeOnEnvListener
This is the listener for TestNG tests that are annotated with `@ExcludeOnEnv`.
To register this listener, either define it in the `src/test/resources/META-INF/services/org.testng.ITestNGListener`
file (by adding `com.github.cpjust.testng_annotations.listeners.ExcludeOnEnvListener` to the file)
or add the `@Listeners({ExcludeOnEnvListener.class})` annotation to the test class.

## Notes on annotation implementations:
After implementing the IAnnotationTransformer & IMethodInterceptor interfaces, getting TestNG to actually run them was tricky.
- IAnnotationTransformer only executes if it's defined in the `src/test/resources/META-INF/services/org.testng.ITestNGListener` file.
- IMethodInterceptor executes if it's defined in the `src/test/resources/META-INF/services/org.testng.ITestNGListener` file or 
  if you add the `@Listeners({ExcludeOnEnvListener.class})` annotation to the test class.
- The `env` system property is only picked up if defined with `-Denv` on the command line or in the `@BeforeSuite`, but not with `@BeforeClass`.
