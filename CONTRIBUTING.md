# Contributing

Thank you for your interest in contributing! ❤️

## Questions & Feature Requests

If you have a question or an idea for a new feature, please open a
[discussion](https://github.com/svenjacobs/reveal/discussions). Issues are reserved for
[bug reports](#reporting-bugs).

## Reporting Bugs

Before reporting a bug please ensure that you can reproduce the issue with the latest version of
Reveal. If this is the case, please
[open an issue](https://github.com/svenjacobs/reveal/issues/new?template=bug_report.md) and fill
out the template, ideally with a minimal sample project that reproduces the issue.

## Contributing Code

By contributing to this project, you confirm that you are the original author of your
contributions, have the necessary rights to submit them, and agree that your contributions may be
distributed under the project license.

### Setup

- Use the latest stable version of [Android Studio](https://developer.android.com/studio/) for
  development. Gradle provisions the required JDK automatically.
- Optional: Install the latest LTS version of [Node.js](https://nodejs.org/) and run `npm install`
  in the root folder of the project. This sets up a Git `commit-msg` hook via
  [husky](https://typicode.github.io/husky/) that validates your commit messages with
  [commitlint](https://commitlint.js.org/) before each commit.

> [!NOTE]
> Code style and commit messages are verified via Continuous Integration on every pull request, so
> we suggest setting up the commit hook to catch errors early.

### Rules

- Document your code.
- Ensure your code is covered by tests. Changes to the overlay's appearance should be covered by
  screenshot tests in `android-tests`.
- If you change the public API, run `./gradlew apiDump` and commit the updated files in the `api/`
  directories.
- Before opening a pull request, run `./gradlew check` and
  `./gradlew :android-tests:verifyRoborazziDebug`.
- If you use an LLM for code generation, **clearly state this** in your pull request. All generated
  code must be **thoroughly reviewed by humans**.

### Styleguide

#### Code

- Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).
- Use spaces with an indentation of 4 characters.
- Run `./gradlew formatKotlin` to format source code according to our rules.

#### Commit Messages

- Use the [Conventional Commits](https://www.conventionalcommits.org/) specification.
- Write commit messages in present tense, imperative mood (e.g. `feat: add abc to xyz`).
- Pull requests are squash-merged, so the pull request title becomes the commit message and must
  follow the same rules.
- The commit type determines the next release version and whether the change appears in the
  changelog: `feat` creates a minor release, `fix` and `perf` create a patch release, and a `!`
  after the type or a `BREAKING CHANGE:` footer creates a major release. Other types such as
  `chore`, `docs`, `refactor`, `test` or `ci` don't trigger a release.
