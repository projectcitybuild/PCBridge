# AGENTS.md

## Project Overview

PCBridge is a Kotlin/JVM multi-module Gradle project that bridges Project City Build services and a Minecraft Paper server.

The project is organized into these main areas:

- `core/` — shared libraries and utilities
- `platform/` — platform-specific implementations, including Paper and the web server
- `app/` — application entry points, including the Paper plugin
- `runtime/` — runtime integration and configuration
- `integrations/` — integrations such as Dynmap, LuckPerms, and Essentials
- `features/` — individual server features

Before modifying code, inspect the target module, its `build.gradle.kts`, neighboring implementations, and existing tests. Preserve established architecture and module boundaries.

## Toolchain and Build

- Use the Gradle Wrapper (`./gradlew`) for Gradle commands.
- Kotlin compilation targets JVM 21.
- Node.js 22.14.0 is specified in `.mise.toml` for end-to-end tests.
- Use `mise install` to install configured tools when mise is available.
- Never commit generated build output, local configuration, credentials, or secrets.

Useful commands:

```bash
# Run unit tests
./gradlew test

# Run verification checks
./gradlew check

# Build the shaded plugin JAR
./gradlew shadowJar

# Start the development Minecraft server
./gradlew runServer

# Run Paper plugin end-to-end tests
mise install
mise exec -- ./gradlew :app:paper-plugin:plugwrightTest
```

Run the narrowest relevant tests during development, followed by broader checks when practical. If a command cannot run because of missing tools, services, or credentials, report the limitation instead of claiming success.

## Kotlin Style and Architecture

- Follow the repository's ktlint configuration in `.editorconfig`.
- Keep changes focused and consistent with neighboring code.
- Prefer descriptive names, cohesive functions, and explicit error handling.
- Reuse existing coroutine and kotlinx-serialization patterns.
- Avoid introducing competing libraries when the project already provides suitable functionality.
- Preserve public API compatibility unless a breaking change is explicitly requested.
- Check downstream module usages before changing shared APIs.
- Place reusable code in `core/` only when it is genuinely shared.
- Follow established patterns for logging, observability, HTTP, configuration, localization, permissions, and platform interactions.
- Avoid blocking calls in coroutine or server event paths.
- Respect Paper/Bukkit thread-safety requirements and established server scheduling patterns.
- Prefer Duration for time constants, configuration, and timeouts.

## Documentation

- When writing documentation or code comments, follow ASD-STE100 Simplified Technical English.

## Testing and Verification

- Add or update tests for behavior changes and bug fixes.
- Prefer the existing JUnit 5, coroutine-test, and Mockito Kotlin conventions.
- Cover relevant edge cases, failure paths, and authorization boundaries.
- For Paper-facing changes, consider whether unit tests and Plugwright end-to-end tests are appropriate.
- Never remove or weaken tests solely to make a change pass.
- Review the final diff for unintended changes, secrets, generated files, and formatting problems.
- Report the verification commands actually run and any remaining unverified behavior.

## Configuration and Security

- Treat API tokens, credentials, player data, and server configuration as sensitive.
- Never hard-code secrets or include real credentials in tests, logs, examples, or commits.
- Follow existing configuration-loading and validation conventions.
- Update default configuration and documentation when settings change.
- Validate external input at system boundaries.
- Avoid logging sensitive request or player information.
- Preserve authorization checks for commands and features, and test unauthorized access where appropriate.

## Repository Workflow

- Keep changes scoped to the requested task.
- Do not overwrite unrelated user changes or perform destructive Git operations.
- Avoid unnecessary dependencies; explain why any new dependency is needed.
- Update relevant documentation when behavior, configuration, setup, or developer workflows change.
