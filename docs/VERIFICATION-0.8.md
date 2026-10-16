# Goldberg 0.8 verification and release gates

Beta release prepared on feature/goldberg-0.8, based on 9e40d5c. The user explicitly authorized beta publication with runtime verification incomplete; the previous runtime publication gates are non-blocking for this beta only. Previously passed automated checks, APK presence/metadata and signing compatibility remain required. The original untracked RELEASE_NOTES.md was reviewed (historical 0.6.0 notes) and left untouched.

## Automated verification

The final build/test/lint results and artifact metadata are recorded below after verification. JVM tests cover manual/Smart accounting, silence/resume, short-gap bridging, stale evidence, generation changes, notes-only achievements, synthetic tonal/noise/sensitivity behavior, CSV escaping/formula neutralization, reminder calendar/DST behavior and existing features.

The host SQLite checker executes actual migration SQL from starting schemas 1–5 to schema 6, checks retained historical rows/notes/totals/profile/badge timestamps and compares exported Room columns, indexes and foreign keys. It enables FK enforcement and checks foreign_key_check. These are host checks, not Android Room execution.

Compiled Android coverage includes seeded schema-5 migration; older schema migration tests updated to reach 6; durable draft reopening; idempotent completion; format-2 backup round trip; format-1 import; note edits; retained local notification state; first badge timestamp; service manifest/startup policy; stale notification actions; coordinator reconnection, failed restore and obsolete capture generations. Compilation is not execution.

## Runtime matrix — all pending; non-blocking for this authorized beta

Use a dedicated authorized Android test installation. No installation or uninstallation was performed in this environment. adb devices reported no connected devices; /dev/kvm is absent. An API-37 AVD definition exists, but it does not establish usable test coverage for supported APIs 26–36. No emulator was started.

- Start while visible; microphone permission denial/permanent denial/revocation.
- Playing and silence while screen-off/locked; Smart Pause and automatic resume; return to app and Activity recreation without duplicate credit.
- Notification Pause/Stop, denied permission, disabled channel, Android Active apps Stop and recents dismissal.
- Calls, mic privacy toggle, competing capture, wired/USB/Bluetooth route changes, service termination, process death, reboot; no automatic recording restart.
- Manual background timing, Mixed conversion, exact multi-piece totals, notes-only records, save retry and recovered review.
- Real acoustic/digital piano, quiet passages, sustain, speech, fan, television and transient noise. Synthetic tones do not establish piano accuracy.
- Actual Room migrations/reopen and authentic 0.6.1/0.7 in-place upgrades with representative data; compare IDs, notes, totals, dates, badges and settings before/after.
- Old/new backup validation, successful replacement, malformed rollback, document providers and restore during active capture/catalogue requests.
- Reminder delivery/cancellation, reboot/update/timezone/DST/Doze, tap navigation, disabled notifications and duplicate delivery.
- CSV opening in spreadsheets, provider errors and multiline notes.
- Profile Settings/header alignment, Repertoire Search, detail Back, bottom tabs, cutouts, gesture/keyboard insets, narrow screens and 200% fonts.
- New/upgrade/skip/replay onboarding; TalkBack labels/contrast; harpsichord audio/focus/exit and badge persistence.
- At least 30–60 minute screen-off CPU/memory/battery/thermal runs and large journals; no app wake lock currently used.

## Publication decision

AUTHORIZED BETA PRE-RELEASE: the user explicitly accepted missing physical-device testing, screen-off microphone verification, Android instrumentation execution, real installation upgrades, accessibility and battery behavior. These checks remain unverified, not passed. Certificate comparison alone does not prove installability or preserved user data. Automated test failures, a signing mismatch, missing APK or failed Git/GitHub operation still stop publication.

## Final host results (2026-10-16)

Executed with the existing SDK /home/stokonn/Android/Sdk and frozen application/test inputs:

`./gradlew assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --offline --no-daemon --max-workers=1`

PASS: final run completed in 1m 20s; 47 JVM tests, zero failures/errors; application and instrumentation APKs compiled; lint zero errors and 13 warnings (pinned dependency/SDK versions and the existing optional Bitmap KTX suggestion). An earlier concurrent-development lint traversal crashed internally; a subsequent source-frozen serial run completed without disabling checks. Build time is not an application performance measurement.

PASS: `python3 -B scripts/check_migrations.py` — schemas 1,2,3,4,5 → 6, exported column/index/FK equality, preserved seed data and defaults.

PASS: `python3 -B scripts/check_backup_fixtures.py` — supplied goldberg-0.6.0-format1.json staged in its original schema, migrated to 6, every original portable field retained. This checks SQLite fixture compatibility, not Android BackupRepository execution.

PASS: `git diff --check` and shell syntax checks. Runtime dependency POM license inventory inspected. Existing font notices retained; Apache 2.0 license and user-facing notices added.

PASS: candidate APK metadata: com.opus.piano, versionName 0.8.0, versionCode 6, min SDK 26, target SDK 36. Permissions include microphone and its foreground-service declarations, notifications and reboot scheduling; no exact-alarm, broad storage or application wake-lock permission.

PASS: actual APK signature verification and certificate comparison against both downloaded published v0.6.1 and v0.7.0 assets. Certificate SHA-256:

`53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`

The published 0.7 metadata uses code 5, so the candidate uses code 6. This is a compatible Android Debug certificate, not a claim of production signing or verified installation.

Candidate APK:
`/home/stokonn/Projects/Opus/app/build/outputs/apk/debug/Goldberg-v0.8.0.apk`

SHA-256:
`57e0cefa60730a1415fd351bf8a679e30baef563c62dfd1372e1f2b7228c5be1`

The named APK equals app-debug.apk. Instrumentation compilation passed; execution was not performed because no device was connected. No APK was installed/uninstalled, and no actual user database was opened or changed by this work. Physical screen-off detection, lifecycle/permission behavior, Android backup/migration execution, populated installation upgrades, accessibility and battery results remain unverified. The initial publication stop was superseded by explicit authorization for a beta with those unverified checks publicly disclosed.

The release commit is identified by tag v0.8.0. Intended GitHub Pre-release: https://github.com/stokonn/Opus/releases/tag/v0.8.0 . RELEASE_NOTES.md remains untracked and untouched, SHA-256 db920f0b1ff88579b6bea9f9da567af16e6db0c7ce5b6439781ca1754fcd4b9b.

## Beta publication recheck

All 95 application/test/build input hashes match the final verified snapshot; the named APK is byte-for-byte identical to app-debug.apk and retains the recorded SHA-256. Saved reports still contain 47 passing JVM tests, zero failures/errors and zero lint errors. Both host migration/backup checks and shell syntax/whitespace checks were rerun successfully. Actual APK signatures were compared again with both published 0.6.1 and 0.7 APKs. Remote main was at the unchanged baseline, allowing a fast-forward merge. No source or asset changes were made for this publication step; only release documentation was revised.

The six pending runtime areas are prominently disclosed in the public English notes. No Android installation, instrumentation execution or physical test is claimed.
