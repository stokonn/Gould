# Gould 0.14 — development notes (unreleased)

**Status:** implementation is in progress. This is not a download or release announcement. Gould 0.13 remains the latest published pre-release.

## Intended user-visible changes on this branch

- Activity replaces Discover in visible bottom navigation. Discover implementation remains in the app.
- Activity presents a Monday-first annual heatmap with Training, Day Off and Miss; day details show date, workout count and exercise minutes. Week Overview reports sessions and optional minutes progress; total workouts, minutes and weekly streak statistics have detail dialogs.
- Retrospective Activity entries are separate from practice sessions and microphone evidence. They may be added through yesterday within the Activity history boundary and deleted with confirmation.
- Weekly session goals become effective the following Monday. The first partial week after Activity is initialized is Beta Week; weekly streak success is based on sessions, and automatic Freeze protects missed session goals. Optional minutes rewards do not affect streaks.
- A data-driven cosmetic collection can be equipped in Customize Profile. Eligible weekly minutes rewards are persisted once per week; collection exhaustion does not issue duplicates.
- Visible moderation/admin entry is hidden temporarily while its implementation and routes are retained. Ivory, Nocturne and system theme selection remain.

## Compatibility and implementation

The branch retains package/namespace `com.opus.piano`, database `opus.db`, Room and the existing practice timing/journal model. Room schema is 12 with additive migration 11→12. Backup format 7/schema 12 adds Activity records and retains earlier format readers. New manual records do not rewrite old PracticeSession rows. `:activity-domain` supplies a deterministic Kotlin Multiplatform `commonMain` boundary and JVM target; it does not add an iOS app. Rust was not adopted because no measurable workload justifies its integration cost.

Development version is code 16 / `0.14.0-dev`, with minSdk 30 and compile/target SDK 37. The published Gould 0.13 APK remains API 26+; these branch targets are not instructions to update an installed release.

The Android 17 SDK is publicly available as the 37.0 preview platform. The quality workflow must install the SDK Manager package `platforms;android-37.0`; requesting `platforms;android-37` fails because that package ID is not published. AGP 9.1.1 supports API 37 with Gradle 9.3.1, Kotlin 2.2.10 and JDK 17. See the [official Android 17 SDK setup](https://developer.android.com/about/versions/17/setup-sdk) and [AGP 9.1 compatibility table](https://developer.android.com/build/releases/agp-9-1-0-release-notes).

Raising minSdk to 30 means Android 8–10 devices on Gould 0.13 will not be eligible to install 0.14. Their local databases are not migrated or erased, but those users remain on their installed version. This compatibility effect must be prominent in eventual release installation guidance.

## Verification status at this checkpoint

- FAST tier: PASS; focused Activity JVM test task was up to date from the previously completed focused test run. A subsequent localization parity check passed for 1,281 EN/CZ resources. Static Room DDL checks passed v1–v11 to v12 and backup fixture checks passed.
- INTEGRATION tier: PASS, 402 JVM tests, 0 failures/errors/skips; lint and Android instrumentation APK compilation passed (376 seconds). No instrumentation methods were executed.
- `:activity-domain:jvmTest`: previously passed. Populated schema 11 migration and Activity repository instrumentation are authored and compile, but have not executed. The newest DST and week-zone tests were added after the last integration compile; the final clean RELEASE tier is pending.
- API 37 is publicly available as the Android 17 SDK Platform 37.0 preview, and AGP 9.1.1 supports API 37. The quality workflow was corrected from nonexistent package `platforms;android-37` to `platforms;android-37.0`. The current-source emulator workflow now installs both API 36 (its emulator) and API 37.0 (the app compile SDK); its separate historical baseline job remains on API 36. These updated workflows have not yet run on GitHub. The relevant official sources are [Android 17 SDK setup](https://developer.android.com/about/versions/17/setup-sdk) and [AGP 9.1 compatibility](https://developer.android.com/build/releases/agp-9-1-0-release-notes).
- API 37 emulator execution remains blocked. The original AVD needed about 7.3 GB in `/tmp` where about 3.1 GB was free. After the relocation proposal was rejected because its destination was not proven absent, a new uniquely named AVD was created in a verified absent home-filesystem directory. It passed emulator host resource checks and QEMU began startup, but it emitted a protected-memory `cannot unmap ptr` warning and exited before ADB registration. No instrumentation ran.
- Final-source unsigned R8 `assembleRelease` passed. It produced package `com.opus.piano`, code 16 / `0.14.0-dev`, minSdk 30, targetSdk 37; artifact SHA-256 `4d9e407a28d4c9d48e8865b3bf3723b8d60a1d38652db827257db53c4976d7fb`. It is unsigned and is not a releasable artifact. Trusted signature, in-place upgrade, screenshots and visual emulator inspection remain pending. GitHub CLI authentication is invalid, so remote tracking, push, PR, merge and prerelease steps remain pending.
- Final-source mandatory RELEASE tier passed in 328 seconds: clean debug build, 403 JVM tests, Android instrumentation APK compilation, lint, and five policy checks. Instrumentation methods were not run.

See the [0.14 stage checklist](../planning/gould-0.14/STAGE-CHECKLIST.md) and [architecture note](../architecture/GOULD-0.14.md) for detailed status. This document must be revised to final user-facing release notes only after every release gate passes.
