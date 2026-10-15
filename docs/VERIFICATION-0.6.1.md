# Goldberg 0.6.1 verification

Date: 2026-10-16. Baseline: committed Goldberg 0.6.0, `20cf5eb86711c9126915f682e50311c54b892d90`. Feature branch: `feature/goldberg-0.6.1`.

## Completed checks

- `./gradlew assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug`: successful.
- **29 JVM tests passed**, zero failures/errors. Five new tests cover period boundaries, future-dated all-time chart totals, streak semantics, duration formatting, tab ownership and showcase ordering/limits.
- Lint: **zero errors, 13 warnings**. Existing SDK/dependency notices and optional Bitmap KTX style suggestion remain.
- `python3 scripts/check_migrations.py`: v1 → v4, v2 → v4, v3 → v4 pass against actual SQL and exported Room schemas.
- `git diff --check`: passed.
- `bash scripts/build-apk.sh`: successful; named APK and standard debug APK are identical.
- Published 0.6.0 APK downloaded to a temporary directory and compared with `apksigner verify --print-certs`: both certificates have SHA-256 digest `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.
- `app/schemas`, `OpusDatabase.kt`, `PersonalData.kt` and `BackupRepository.kt` have no changes from 0.6.0. Application ID and database remain `com.opus.piano` / `opus.db`; schema 4, backup format 1; version name 0.6.1 / code 4. Existing signing configuration is untouched.
- Remote main at inspection was `346bc04` (Opus 0.5), an ancestor of the feature baseline. The feature branch includes the complete committed Goldberg 0.6.0 implementation. Merge publication is authorized separately by the user; no history rewriting is needed.

## Navigation diagnosis and correction

The previous tab handler saved and restored destination stacks using the graph start destination as the pop boundary. Search pushed above Profile could become part of saved start-destination state and reappear when Profile was selected. Other tabs could similarly restore Search rather than their root.

`selectTopLevel` now returns immediately for the current root, pops to an existing requested root, or navigates to it above the stable HOME start destination without restoring a saved deep stack. Search focus/keyboard are dismissed on tab taps. Bottom navigation has IME insets, and content consumes Scaffold padding before applying its own IME insets. Profile has only the wordmark and app-bar Settings gear; Settings is a normal pushed destination and Back pops to the existing Profile entry.

## Android tests compiled, not executed

- `NavigationRegressionTest`: real Compose navigator and production tab helper; repeated Search/Repertoire/Profile switches, Settings Back, other tabs, and final root stack.
- `ProfileCompatibilityTest`: a synthetic 0.6.0 format-1/schema-4 fixture restored over an existing isolated test journal, retaining IDs/notes/time/unlock/showcase and round-tripping all three theme values.
- Existing Android migration, disk-reopen, multi-piece persistence and backup/restore/error cases continue to compile.

**No Android device was connected** (`adb devices` returned an empty list). These instrumentation tests were not executed. No on-device migration, UI, screenshot, large-font, narrow-screen, TalkBack, theme-transition, picker or Search-to-Profile tap test is claimed. The 0.6.0 emulator failures are recorded in the historical verification document; no working runtime target was available for this maintenance release.

The release is explicitly a **pre-release debug APK** with the above outstanding device checks. Source-level layout review includes single-column cards at larger font scales, scrolling filters/chart labels, wrapped names, accessible icon labels and theme radio semantics; it is not a substitute for device visual inspection.

## Artifact

`app/build/outputs/apk/debug/Goldberg-v0.6.1.apk`

Size: 18,107,512 bytes. SHA-256: `adef8176e32fe4e8eec8a9d0d41b5d3d43585e3fb70139f42b4a97f7e3b7334d`.

The original untracked root `RELEASE_NOTES.md` (0.6.0 notes) is preserved untouched and excluded from the 0.6.1 commit. Current release body is `docs/RELEASE-0.6.1.md`.
