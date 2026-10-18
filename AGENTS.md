# Goldberg — permanent development policy

This is the canonical policy for every coding agent and every Goldberg version (0.9.2, 0.10 and beyond). Preserve it in future updates. Thin tool-specific instruction files reference this file; do not maintain competing copies. Newer explicit owner instructions and higher-priority system/security rules take precedence.

## Work autonomously and protect data

Inspect the current branch, status, remote, tags, releases, milestone and Issues before work. Document an implementation checklist. Perform routine inspection, editing, builds, tests and safe Git operations independently. Ask only for important product decisions, unavailable credentials or genuinely destructive/security-sensitive operations. Respect mandatory sandbox and permission controls; never bypass them. Preserve unrelated work, especially the existing untracked RELEASE_NOTES.md. Never force-push, reset unrelated work, commit secrets, rotate signing keys or overwrite release tags.

Data integrity outranks feature completeness and visual polish. Preserve history, unfinished drafts, multi-piece attribution, repertoire, profile/avatar/nickname, settings, badge dates and showcase. Never use destructive Room migrations or database recreation. Add explicit tested migrations only when necessary. Preserve backup compatibility and isolate disposable media from critical data. Never silently replace/delete an unfinished draft; recovery retry and unchanged-data export must remain possible. New-session consent alone is not consent to delete the original recovery data.

## Architecture and coding

Goldberg is an existing native Kotlin / Jetpack Compose / Material 3 Android application. Keep application ID/namespace com.opus.piano and database opus.db. Reuse the application-scoped PracticeCoordinator state machine, established repositories and Compose Navigation; do not introduce competing timers or unnecessary dependencies. Use monotonic time for elapsed practice and preserve exact per-piece/session reconciliation and idempotent completion. Persist startup before publishing running state or starting capture; recovery must never automatically restart the microphone.

Extend the central design system and reusable components. Use centralized semantic theme tokens, Cormorant Garamond/Inter, consistent spacing, responsive layouts, accessible touch targets and Android font scaling. Support Ivory/Nocturne and CZ/EN; use localized resources, proper plurals, application-locale dates/numbers and accessibility descriptions. Never automatically translate user-entered text or original catalogue work/composer names. Respect reduced motion. Keep network/storage/decoding work off the main thread. Metered media downloads require explicit permission; retain attribution/license metadata and safe offline fallbacks.

## Never use the owner's physical phone

Never install, launch, test, wipe or run ADB/instrumentation against the owner's physical Android phone. Use host tests and emulators. Every ADB operation must explicitly target an identified emulator serial (emulator-NNNN); never use implicit device selection. Never claim that an agent performed physical-device verification. Explicit owner-reported manual results may be recorded with clear attribution; never infer additional checks from them. Emulator screenshots are not physical-device evidence.

## Permanent GitHub workflow

Discover the actual repository (currently stokonn/Goldberg; legacy Opus URLs may redirect). Inspect existing resources; do not duplicate them. Reuse one modern Project named Goldberg Development across releases. Track significant tasks with Issues, version milestones, labels and Status (Backlog/Todo/In Progress/Done); add Priority/Version/Type when supported. Keep unfinished/unverified work open; close tasks only after relevant implementation and verification, with commit/PR evidence. Do not invent future-version scope.

Maintain English-only official Wiki pages and navigation: Home, Getting Started, User Guide, Settings & Customization, Developer Guide, FAQ & Troubleshooting, Changelog & Roadmap, Credits & Licenses. Document only actual working features; clearly label blocked integrations and unverified behavior. Use verified screenshots and licensed attribution; include `created by stokonn with love <3`. Keep local maintainable Wiki sources in docs/wiki. Check Wiki enablement/initialization and GitHub token permissions; report exact blockers without claiming success or starting unauthorized authentication.

For each release: inspect → identify milestone/Issues → plan → implement → run relevant checks → build APK → verify acceptance → synchronize Project/Wiki → update README/changelog/docs → commit/push → publish release → report results. Commit/push/release are automatically authorized only after every mandatory gate passes. Never invoke an old release script without inspecting its actions; do not publish merely because Gradle succeeds.

## Quality and publication gates

Run appropriate existing/new JVM tests, migration and backup checks, localization/paging checks, lint, debug APK build and instrumentation compilation/execution where available. Add meaningful regression tests for data/timing/cache/permission changes. Inspect rendered UI across light/dark themes, representative sizes and larger fonts. Verify network/offline behavior, recovery and an in-place upgrade on a seeded emulator when required. Distinguish executed/passed, failed, compiled-only and unexecuted checks. No fabricated results.

Use the project's Gradle wrapper; typical checks:
`./gradlew clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1`
Run relevant `scripts/check_*.py` checks. Do not treat APK compilation as instrumentation execution. Verify actual package/version/code, ZIP/resources, compatible certificate against the trusted previous APK, APK SHA-256 and source/artifact identity. Trusted signing SHA-256: 53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e. Preserve existing compatible signing configuration; never uninstall to bypass mismatch.

If a mandatory check fails or cannot be verified, stop publication, explain the blocker, and continue independent safe work. Missing Google OAuth configuration must be reported; never fake sign-in. Release type follows the explicit owner requirement for that version (0.9.2: normal release titled Goldberg 0.9.2, only after its mandatory gates pass). Safely integrate verified source without history rewriting, create a new annotated version tag, upload the verified APK/changelog, verify remote release/asset digests and update tracking/docs. Never overwrite conflicting tags/releases.

Final reports must include significant files/architecture/database changes, exact commands/results, screenshots actually inspected, unexecuted checks, external setup/blockers, branch/status/commit, APK path/hash/signing, and truthful Project/Wiki/release URLs and Issue statuses.

## Goldberg 0.10 visual approval gate

Use docs/design/goldberg-0.10/README.md and manifest.json; 01_HOME_APPROVED and 02_PROFILE_APPROVED are the primary references. Inspect all supplied screens, retain four tabs/top-right Settings and every appearance preference, and never package reference images in the APK. Compare actual emulator renders in both themes/font scales and document meaningful differences. Obtain explicit owner visual approval before publication unless the owner explicitly waives screenshot review for that release. For 0.10 the owner waived personal screenshot review on 2026-10-17; minor differences are listed for possible 0.10.1 improvements. Earlier mockup approval and passing automated tests alone are not final visual sign-off.

## Legacy Cleanup & Repository Maintenance

Before development or repository maintenance, read [docs/maintenance/LEGACY-CLEANUP.md](docs/maintenance/LEGACY-CLEANUP.md). Follow its documented policy for identifying obsolete code, redundant assets, unused files and technical debt; remove only verified unnecessary components. Preserve user data, migrations, backup formats, release history, legal documentation, signing identity and compatibility-sensitive identifiers. Do not start substantial cleanup during unfinished release verification. Perform safe routine maintenance autonomously, using automated checks and explicitly targeted emulators rather than physical user devices. Document cleanup changes and actual verification in the permanent maintenance record, preserving its history across future Goldberg versions. Do not publish a new application release solely for maintenance without authorization.
