# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased] - yyyy-mm-dd

## [1.0.1] - 2025-08-24
### Added
- Support for CSV strings in `@IncludeOnEnv` and `@ExcludeOnEnv` annotations by a new `delimiter` attribute.

## [1.0.0] - 2025-07-29
Initial version.

Contains the following features:
- `@IncludeOnEnv` and `@ExcludeOnEnv` annotations for conditional test execution.
- `IncludeOnEnvListener` and `ExcludeOnEnvListener` to handle these annotations.
