# Project Overview
This project contains some extra annotations that are useful when running tests in TestNG.

## Library and Frameworks
- Maven.
- Java 11.
- TestNG for integration testing.
- JUnit 5 for unit testing.
- Mockito for mocking in unit tests.
- Lombok to reduce boilerplate code.
- SLF4J with Log4j for logging.
- Surefire plugin for running unit tests.
- Failsafe plugin for running integration tests.

## Coding Standards
- Use Lombok to reduce boilerplate code.
- Use SLF4J with Log4j for logging.
- Try to follow best practices such as: SOLID, DRY and KISS design principles and clean code.
- Avoid deep nesting of code blocks; refactor into smaller methods if necessary.
- Try to keep cognitive complexity low; refactor complex methods into smaller, more manageable ones.
- Avoid using magic numbers; use named constants instead.
- Use pre-increment and pre-decrement operators instead of post-increment and post-decrement, unless you need to return the old value.

### Writing Tests
- Write unit tests for all new features and bug fixes.
  - Unit test names should follow the pattern: `methodName_stateUnderTest_expectedBehavior`.
  - Use Mockito for mocking dependencies in unit tests.
  - Unit test class names should end with `Test`.
- Write integration tests for all new features and bug fixes.
  - Integration test class names should end with `IT`.
- Assertions in tests should always have a message describing the failure.
- Prefer `assertThat` from org.hamcrest.MatcherAssert for assertions in tests with more specific hamcrest matchers.
- Ensure high code coverage with unit tests.
- Try to use parameterized tests where applicable.
- Use meaningful names for classes, methods, and variables.

### Documentation
- Write clear and concise comments where necessary.
- All functions should have Javadoc comments, even private functions, but not test functions.
- Update the README.md and CHANGELOG.md files for all new features and bug fixes.

### Code Style
- Functions should be separated by a single blank line.
- Add a blank line between different blocks of code (e.g., between fields and methods) for readability, and add a comment above blocks of code that might be complicated.
- All loops and if-else blocks should use braces, even for single statements.
- Use 4 spaces for indentation.
- Try to limit lines to a maximum of 140 characters.
- Use camelCase for variable and method names.
- Use PascalCase for class and interface names.
- Use UPPER_SNAKE_CASE for constants.
- New interfaces should be prefixed with an uppercase 'I' (e.g., `IExample`).
