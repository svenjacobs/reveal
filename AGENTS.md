# AGENTS.md

This file provides guidance to AI coding agents working in this repository.

## Project overview

Reveal is a Kotlin Multiplatform library (Compose Multiplatform) implementing a "reveal" / coach
mark / onboarding effect, targeting Android, iOS, Desktop and Web.

Modules:

| Module                  | Purpose                                                                                            |
|-------------------------|----------------------------------------------------------------------------------------------------|
| `reveal-common`         | Shared common code used by other modules (commonMain only)                                         |
| `reveal-core`           | Core library: `RevealCanvas`, `Reveal` composable, public API                                      |
| `reveal-shapes`         | Additional shapes for explanatory overlay items                                                    |
| `reveal-compat-android` | Compatibility utilities for mixed View/Compose Android setups                                      |
| `android-tests`         | Android instrumentation/screenshot tests (Roborazzi) for the library                               |
| `demo-app`              | Separate Gradle build (own `settings.gradle.kts`) demonstrating usage across Android/iOS/Desktop   |
| `convention-plugins`    | Gradle convention plugins (`convention.multiplatform`, `convention.publication`) shared by modules |

`reveal-core`, `reveal-shapes` and `reveal-common` are Kotlin Multiplatform modules using
`commonMain`/`commonTest` source sets (plus Android-specific source sets like `androidUnitTest`
where relevant). `reveal-compat-android` and `android-tests` are Android-only.

`demo-app` is its own root project with a separate `settings.gradle.kts` and Gradle wrapper —
treat it as an independent build, not a subproject of the root build.

## Build & test commands

Run from the repository root unless noted otherwise.

```bash
./gradlew check                               # Run all checks (lint, tests, API validation, kotlinter) for the root build
./gradlew :reveal-core:test                   # Run common/unit tests for a single module
./gradlew :android-tests:verifyRoborazziDebug # Verify screenshot tests
./gradlew :android-tests:recordRoborazziDebug # Re-record screenshot baselines after an intentional UI change
./gradlew connectedAndroidTest                # Run instrumented tests on a connected device/emulator
./gradlew apiDump                             # Regenerate public API dump files (api/ dirs) after a public API change
./gradlew apiCheck                            # Verify the public API surface matches the committed dump (part of `check`)
./gradlew formatKotlin                        # Auto-format Kotlin code with kotlinter/ktlint
./gradlew lintKotlin                          # Check Kotlin formatting without modifying files
```

The `demo-app` directory is a separate Gradle build; run its own `./gradlew` from inside
`demo-app/` (e.g. `cd demo-app && ./gradlew build`).

CI (`.github/workflows/verify-pr.yml`) runs `./gradlew check`,
`:android-tests:verifyRoborazziDebug`, and `connectedAndroidTest` on API levels 23 and 29 — mirror
these locally before considering a change done.

## Code style

- Indentation is **spaces**, size 4 (see `.editorconfig`). Do not convert to tabs.
- Kotlin formatting/linting is enforced by `kotlinter` (ktlint, `android_studio` code style) on
  every `check` build. Run `./gradlew formatKotlin` to auto-fix style issues before finishing a
  change.
- Trailing commas are allowed both in declarations and at call sites.
- `@Composable`-annotated functions are exempt from the function-naming lint rule (so
  PascalCase composable function names are expected and correct).

## Public API surface

`reveal-core`, `reveal-shapes` and `reveal-compat-android` use the Kotlin binary-compatibility
validator. Each module has an `api/` directory with dumped public API signatures (including
per-target `.klib` dumps). Any change to public API (new/changed/removed public
classes/functions/properties) requires running `./gradlew apiDump` and committing the updated
files in `api/`, otherwise `./gradlew check` (via `apiCheck`) will fail.

## Versioning & publishing

- The library version is derived from the `RELEASE_TAG_NAME` environment variable (set in CI on
  release); locally it builds as `SNAPSHOT`. Do not hardcode version numbers in module build
  files.
- Publishing to Maven Central (Sonatype) is handled by the `convention.publication` plugin and the
  `deploy-release.yml` workflow — this is not something to trigger from local changes.

## Working in this repo

- Minimum Android SDK is 23 (`androidMinSdk` in the root `build.gradle.kts`); avoid introducing
  APIs that require a higher minSdk without updating that constant deliberately.
- Prefer adding code to the most specific module that needs it: shared/common logic with no
  platform dependency belongs in `reveal-common`; public reveal-effect API belongs in
  `reveal-core`; optional shapes belong in `reveal-shapes`.
- When changing visual/overlay behavior, check whether `android-tests` screenshot baselines need
  re-recording (`recordRoborazziDebug`) and review the resulting screenshots before committing
  them.
- Any non-trivial change should be validated with all three test layers before being considered
  done: unit tests (`./gradlew test` / module-specific `test` tasks), instrumentation tests
  (`./gradlew connectedAndroidTest`), and screenshot tests
  (`./gradlew :android-tests:verifyRoborazziDebug`). CI runs all three, so a change that only
  passes one of them is not complete.
