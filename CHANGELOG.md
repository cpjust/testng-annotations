# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased] - yyyy-mm-dd

## [1.1.0] - 2025-09-20

### Added
- `@ValueSource` annotation to provide a source for test values, just like JUnit 5's `@ValueSource`.
- `ValueSourceListener` to handle the `@ValueSource` annotation and provide test values for parameterized tests.

## [1.0.1] - 2025-09-19
### Added
- Support for CSV strings in `@IncludeOnEnv` and `@ExcludeOnEnv` annotations by a new `delimiter` attribute.
- A test that uses `@Test` priority to ensure that the `@IncludeOnEnv` and `@ExcludeOnEnv` work correctly with test priorities.

## [1.0.0] - 2025-08-05
Initial version.

Contains the following features:
- `@IncludeOnEnv` and `@ExcludeOnEnv` annotations for conditional test execution.
- `IncludeOnEnvListener` and `ExcludeOnEnvListener` to handle these annotations.
