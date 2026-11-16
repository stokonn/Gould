# Contributing to Gould

Gould is a native Kotlin, Jetpack Compose and Room Android app. Read
[AGENTS.md](AGENTS.md), the [Developer Guide](docs/wiki/Developer-Guide.md), and
the relevant compatibility or architecture notes before changing application
behavior.

## Before opening a change

- Keep `com.opus.piano`, `opus.db`, signing compatibility and stored preference
  identifiers stable.
- Room changes need additive migrations and updated schema/backup checks. Never
  recreate a user's database or replace an unfinished recovery draft.
- Keep user-entered names and catalogue source names unchanged; add EN/CZ
  resources for new user-facing copy.
- Do not add credentials, personal journals, private screenshots or owner data
  to a commit. The owner performs acceptance testing on a real Android device
  (primary device: Samsung Galaxy A55); emulator availability is not required.
  Coding agents must not install, launch, test, or run ADB/instrumentation on
  the owner's phone. See the Gould 0.15 physical-device checklist.
- Use the Gradle wrapper and the narrowest relevant verification tier. Report
  tests that were not run; instrumentation compilation is not execution.
- Preserve existing human authorship and `created by stokonn with love <3`.
  Do not add coding agents or AI systems as co-authors.

## License status

Project-owned application source is licensed under GPL-3.0-or-later, and
project-owned documentation/original prose under CC-BY-SA-4.0. By submitting a
contribution, you confirm you have the right to submit it under the applicable
project license. Identify third-party material and preserve its original
license and attribution; do not assume these project licenses cover it. See
[LICENSE](LICENSE), [LICENSE-DOCS](LICENSE-DOCS), and
[docs/LICENSES.md](docs/LICENSES.md).

## Bug reports

Use the GitHub bug report form. Include only the minimum diagnostic details
needed to reproduce the issue. Never attach a journal backup, credentials,
private recording, or personal screenshot. Redact account and device
identifiers.

created by stokonn with love <3
