# Gould 0.14 — development notes (unreleased)

**Status:** implementation is in progress. This is not a download or release announcement. Gould 0.13 remains the latest published pre-release.

## Intended user-visible changes on this branch

- Activity replaces Discover in visible bottom navigation. Discover implementation remains in the app.
- Activity presents a Monday-first annual heatmap with Training, Day Off and Miss; day details show date, workout count and exercise minutes. Week Overview reports sessions and optional minutes progress; total workouts, minutes and weekly streak statistics have detail dialogs.
- Manual Activity entries are separate from practice sessions and microphone evidence. They can be entered for today and the previous two local calendar dates, contribute duration and streak/activity totals, and are deleted with confirmation.
- Weekly session goals become effective the following Monday and are edited from the pencil in the Activity header. Beta Week is a transition period: it can earn its own cosmetic when configured weekly requirements are met, but does not advance official Streak or Freeze progress.
- Each eligible perfect week creates an independent persisted cosmetic reward to claim in Activity. Four consecutive perfect official Streak weeks grant one additional Streak Freeze; this milestone neither gates nor replaces the weekly cosmetic. Claiming reveals and owns the cosmetic, which can then be applied from the Profile collection shortcut.
- Home, Profile and Activity use the same combined activity sources for totals and daily streaks. The annual heatmap uses larger, more compact day cells while retaining 48dp touch targets.
- Visible moderation/admin entry is hidden temporarily while its implementation and routes are retained. Ivory, Nocturne and system theme selection remain.

## Compatibility and implementation

The branch retains package/namespace `com.opus.piano`, database `opus.db`, Room and the existing practice timing/journal model. Room schema is 12 with additive migration 11→12. Backup format 7/schema 12 adds Activity records and retains earlier format readers. New manual records do not rewrite old PracticeSession rows. `:activity-domain` supplies a deterministic Kotlin Multiplatform `commonMain` boundary and JVM target; it does not add an iOS app. Rust was not adopted because no measurable workload justifies its integration cost.

Development version is code 16 / `0.14.0-dev`, with minSdk 30 and compile/target SDK 37. The published Gould 0.13 APK remains API 26+; these branch targets are not instructions to update an installed release.

The Android 17 SDK is publicly available as the 37.0 preview platform. The quality workflow must install the SDK Manager package `platforms;android-37.0`; requesting `platforms;android-37` fails because that package ID is not published. AGP 9.1.1 supports API 37 with Gradle 9.3.1, Kotlin 2.2.10 and JDK 17. See the [official Android 17 SDK setup](https://developer.android.com/about/versions/17/setup-sdk) and [AGP 9.1 compatibility table](https://developer.android.com/build/releases/agp-9-1-0-release-notes).

Raising minSdk to 30 means Android 8–10 devices on Gould 0.13 will not be eligible to install 0.14. Their local databases are not migrated or erased, but those users remain on their installed version. This compatibility effect must be prominent in eventual release installation guidance.

## Historical verification status through November 13, 2026

- FAST tier: PASS; focused Activity JVM test task was up to date from the previously completed focused test run. A subsequent localization parity check passed for 1,281 EN/CZ resources. Static Room DDL checks passed v1–v11 to v12 and backup fixture checks passed.
- INTEGRATION tier: PASS, 402 JVM tests, 0 failures/errors/skips; lint and Android instrumentation APK compilation passed (376 seconds). No instrumentation methods were executed.
- `:activity-domain:jvmTest`: previously passed. Populated schema 11 migration and Activity repository instrumentation are authored and compile, but have not executed. The newest DST and week-zone tests were added after the last integration compile; the final clean RELEASE tier is pending.
- API 37 is publicly available as the Android 17 SDK Platform 37.0 preview, and AGP 9.1.1 supports API 37. The quality workflow was corrected from nonexistent package `platforms;android-37` to `platforms;android-37.0`. The current-source emulator workflow now installs both API 36 (its emulator) and API 37.0 (the app compile SDK); its separate historical baseline job remains on API 36. These updated workflows have not yet run on GitHub. The relevant official sources are [Android 17 SDK setup](https://developer.android.com/about/versions/17/setup-sdk) and [AGP 9.1 compatibility](https://developer.android.com/build/releases/agp-9-1-0-release-notes).
- The dispatch-only emulator workflow now targets the installed Android 17/API 37.0 Google Play system image for current-source instrumentation; its historical acceptance job remains API 36. The API 37 workflow ran on `dbd740c`: package installation, emulator boot and APK compilation passed, but a retained script assertion expected API 36 and exited before test execution. The API check is now parameterized to the explicit expected API 37. The parallel API 36 acceptance job also exposed a test-harness assumption that `badge_notices` existed in an older schema-8 baseline; it now seeds that table only when present, leaving later Room migrations responsible for creating it. A corrected full rerun is pending.
- Local API 37 emulator execution remains blocked. The original AVD needed about 7.3 GB in `/tmp` where about 3.1 GB was free. After the relocation proposal was rejected because its destination was not proven absent, a new uniquely named AVD was created in a verified absent home-filesystem directory. It passed emulator host resource checks and QEMU began startup, but it emitted a protected-memory `cannot unmap ptr` warning and exited before ADB registration. No instrumentation ran.
- Final-source unsigned R8 `assembleRelease` passed. It produced package `com.opus.piano`, code 16 / `0.14.0-dev`, minSdk 30, targetSdk 37; artifact SHA-256 `4d9e407a28d4c9d48e8865b3bf3723b8d60a1d38652db827257db53c4976d7fb`. It is unsigned and is not a releasable artifact. Trusted signature, in-place upgrade, screenshots and visual emulator inspection remain pending. GitHub CLI authentication is invalid, so remote tracking, push, PR, merge and prerelease steps remain pending.
- Final-source mandatory RELEASE tier passed in 328 seconds: clean debug build, 403 JVM tests, Android instrumentation APK compilation, lint, and five policy checks. Instrumentation methods were not run.

See the [0.14 stage checklist](../planning/gould-0.14/STAGE-CHECKLIST.md) and [architecture note](../architecture/GOULD-0.14.md) for detailed status. This document must be revised to final user-facing release notes only after every release gate passes.

## November 14, 2026 — manual-test defect follow-up

This follow-up corrects the session/manual statistics disconnect, date window,
heatmap spacing, Profile goal placement, reward refresh and Day Off/Freeze
clarity. Automated test details and any remaining limits are recorded in the
stage checklist after the final focused API 37 run.

## November 14, 2026 — follow-up verification artifact

The focused API 37 runtime verification passed 24 tests with zero failures,
errors, or skips, including Activity repository/reward persistence, migration,
widget rendering, Profile goal placement, and cosmetic collection selection,
Apply, and persistence UI coverage. The clean RELEASE host gate passed with 405
unit tests, instrumentation APK compilation, lint, and five repository policy
checks. Release-mode lint and assembly also passed.

A fresh signed review APK is at
`app/build/outputs/apk/release/app-release.apk`: package `com.opus.piano`,
version `0.14.0`, version code 16, non-debuggable, valid ZIP, and signed with
the trusted certificate SHA-256
`53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`. APK
SHA-256: `0fe2c9d546148f983a26cb62021c4e4011980ffc86928c935e2e430a30305dcf`.
The previous APK was separately preserved at
`/tmp/gould014-prior-release-before-clean.apk` (SHA-256
`ff88b504ae42ff884347e6c5f8baed7ba30eec4d4c8619fb1e4b71f28de97eb3`).

This is an internal review artifact, not a published release. Signed upgrade
verification against the published baseline, launcher widget activity refresh,
and broader visual review remain unverified. See the [stage checklist](../planning/gould-0.14/STAGE-CHECKLIST.md)
for exact commands, results, and limitations. No Git or publication operation
occurred.

## November 14, 2026 — real-device UX correction build

Owner phone testing identified that the Weekly Goal edit action remained below
Week Overview and that Claim was missing. The updated source places the pencil
beside the Weekly Goal heading, places goal progress and any pending Claim in
that section, and renders pending Claim independently of summary loading. A
fresh signed review APK was built after release lint passed:
`app/build/outputs/apk/release/app-release.apk`, package `com.opus.piano`,
version `0.14.0`, code 16, `debuggable=false`, certificate SHA-256
`53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`, APK
SHA-256 `ad9343a5b3aa4108c4ea6d8fba3bde46cc3924c187bdf6761428fc3f88b15f8a`.
Release lint and archive/signature/metadata checks passed. The corrected
non-visual Compose semantics test compiled but could not be executed because
the disposable emulator did not remain visible to ADB; no visual emulator
testing was performed. Owner phone review remains pending. No commit or
publication occurred.

## November 14, 2026 — claimable reward UX update

The later source update moves Weekly Goal editing into Activity, tightens
contribution-cell spacing, and keeps perfect-week cosmetics pending until the
user taps Claim. Claim reveals the item and atomically persists ownership with
the claimed timestamp. Host unit/build/lint checks, 11 repository tests on API
37, and the non-visual Activity Goal editor semantics test on API 36 passed.
API 37 could not run that Compose test because the current Espresso harness
fails before assertions while looking up `InputManager.getInstance`. No visual
emulator testing was performed. The signed review APK described above predates
this update and does not contain it; no new signed APK or release was created.

## November 14, 2026 — weekly cosmetic states and separate Freeze milestone

Confirmed product rules: each eligible completed perfect week grants one
cosmetic that can be claimed individually. Four consecutive successful weeks
separately add one Streak Freeze; they do not gate or replace cosmetics. The
Weekly Session Goal card now derives explicit Loading, Locked, Claim and Claimed
states from its observed persisted reward rows. Pending rewards remain
available across restart; claiming one exposes the next pending week, while the
latest claimed reward remains visibly claimed. A broken week resets Freeze
milestone progress without removing cosmetic grants. The Freeze bonus is
recomputed from persisted weekly activity history and repeated evaluation does
not duplicate it.

Host checks passed: 12 domain tests, 409 app unit tests, Android instrumentation
compilation, localization, debug lint, and release lint. The Room repository and
Compose instrumentation cases compiled but were not executed because this host
has no `/dev/kvm` and ADB could not start its local server. No visual emulator
testing was performed.

Fresh signed review APK:
`app/build/outputs/apk/release/app-release.apk`, package `com.opus.piano`,
version `0.14.0`, code 16, `debuggable=false`, trusted certificate SHA-256
`53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`, APK
SHA-256 `a49a77a453b8de1c584e1c9537328d0d927491bcea09e9aaeb1e4b8b42205b8e`.
Signature, ZIP integrity, package metadata and release lint checks passed. This
is an internal review artifact; owner phone review remains pending. No Git or
publication operation occurred.
