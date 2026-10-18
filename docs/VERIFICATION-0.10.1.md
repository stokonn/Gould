# Goldberg 0.10.1 verification

Verification performed on 2026-10-18 using host tests and the explicitly targeted Android emulator `emulator-5582`. No physical device was accessed and no owner testing or screenshot approval is required for this release under the current instructions.

## Source and artifact

Release branch: feature/goldberg-0.10.1, based on main `6bde1ad` and the published 0.10 source `7b5874e`. Source input hashes are recorded in TESTED-SOURCE-0.10.1.json and checked against the built tree. No Room schema, backup format, package/namespace, manifest component, stored preference key or signing identity was renamed.

- Version: 0.10.1 / code 11, package com.opus.piano, database opus.db.
- Preserved release artifact: `.gradle/release-artifacts/Goldberg-v0.10.1.apk` (copied from app/build/outputs/apk/debug/app-debug.apk).
- APK SHA-256: `854bbfebf617ec920df60d181a12e0dcbd2fed10052f2479903a78a290cef312`.
- Signing certificate SHA-256: `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`, matching the trusted published 0.10 APK.
- ZIP/resource/icon checks pass. Packaged privacy/OFL notices match source bytes. No design references are packaged.
- Private reference PNGs remain ignored/untracked. Unrelated RELEASE_NOTES.md and the external Opus-backup-0101 backup are preserved.

## Commands and executed results

All Gradle commands use the project wrapper and the cached writable GRADLE_USER_HOME=/tmp/goldberg-gradle-home. Sandbox network-interface restrictions require the approved execution context.

| Command | Result |
|---|---|
| `./gradlew --offline clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1` | PASS: 84 tasks executed; clean source rebuild. |
| `./gradlew --offline assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1` after final Library corrections | PASS: 83 tasks; final release APK rebuilt. |
| JVM test reports | 75 tests, zero failures/errors/skips; includes failed-save completion and annotation preservation/length regressions. |
| `adb -s emulator-5582 shell am instrument -w com.opus.piano.test/androidx.test.runner.AndroidJUnitRunner` | 36 tests passed; separately executed, not inferred from compilation. |
| `python3 scripts/check_migrations.py` | PASS: every historical schema 1–7 to schema 8 retains fixture data. |
| `python3 scripts/check_backup_fixtures.py` | PASS: formats 1–3 / historical schemas 4, 6, 7 retain portable fields. Current format-4 round trip is covered by instrumentation. |
| `python3 scripts/check_history_paging.py` | PASS: bounded paging, tied timestamps, duplicate parts and untimed attribution. |
| `python3 scripts/check_localization.py` | PASS: 804 CZ/EN resources, placeholders, plurals and locale declarations. |
| `python3 scripts/check_agent_policy.py` | PASS: canonical policy, thin agent references and Wiki navigation; tool runtime instruction loading is not tested. |
| `python3 scripts/verify_quality_apk.py /tmp/goldberg-0.10-release/Goldberg-v0.10.apk app/build/outputs/apk/debug/app-debug.apk 0.10.1 11` | PASS: package/version/code, signer, ZIP and packaged artwork. |
| `./gradlew --offline :app:dependencies --configuration debugRuntimeClasspath --no-daemon --max-workers=1` | PASS: resolved inventory audited; see legal report. |
| `git diff --check` | PASS. |

Lint: zero errors, 74 warnings in the final build report. These are non-failing dependency/resource/style recommendations; no identified release-critical lint defect remains. Logs are local under /tmp/goldberg101-final-build.log, goldberg101-final-refinement.log, goldberg101-release-instrumentation.log and goldberg101-apk-verification.log.

## Seeded upgrade and accounting

Reproducible command:

```sh
python3 scripts/verify_emulator_upgrade.py --serial emulator-5582 \
  --baseline /tmp/goldberg-0.10-release/Goldberg-v0.10.apk \
  --candidate .gradle/release-artifacts/Goldberg-v0.10.1.apk \
  --output /tmp/goldberg101-release-upgrade --seed-disposable
```

PASS: install-over-0.10 succeeds without uninstall. Exact equality is checked for all persistent journal tables, including 37 sessions, 37 measured parts, repertoire, profile, notes, badge dates, recovery archive and an unfinished three-second draft. Appearance/media/identity preference hashes remain equal; the fixture includes Slate/Outline/Compact/Composer settings. Recovery renders Interrupted and does not restart timing or microphone capture.

The initial fixture-copy bug was fixed by committing AND checkpointing SQLite WAL before copying the main file. A later comparison correctly caught existing 0.10 metadata/badge backfill because the older fixture had not first been opened by 0.10. The script now lets the baseline settle before taking its snapshot; exact post-upgrade equality then passes. Neither failed setup attempt is represented as a successful data-preservation test.

Runtime UI actions completed the recovered draft, added one omitted piece, saved a session note, edited a measured-piece note and saved a labeled untimed-piece annotation through History. A database assertion confirmed exactly one new session, 3000ms credit, 3000ms summed measured parts, one untimed link, retained original draft/session/piece text and total practice of 2,223,000ms across 38 sessions. No invented duration or duplicate session was introduced. Oversized annotations reject instead of truncating existing text.

## Rendered UI and reference comparison

All seven private references were inspected in the original implementation and remain local-only. Written decisions govern illustrative differences. Captures are local under /tmp/goldberg101-final-ui; selected identity-safe captures are in docs/wiki/images. UI automation uses scripts/emulator_ui.py and explicit emulator serials.

| Screen | Captures inspected | Findings |
|---|---|---|
| Home | home, home-light, home-cs-large, home-offline | Gear removed; greeting raised/slightly enlarged; existing 0.10 quote/stats/chart/recent hierarchy preserved. Normal width retains the existing two-column greeting/quote; narrow/large-font layout stacks them. Long names wrap, and lower content scrolls. |
| Library | library, library-mine, library-filters, library-light, library-cs-large, library-offline | One All/My Repertoire experience; adaptive controls, personal/catalogue distinction, portraits and favorites retained. Narrow Czech search label was shortened/capped after review. Final captures library-release, library-empty-release, library-reset-release and library-cs-large-release confirm the correction. Empty/filter behavior was corrected and recaptured on final artifact. Catalogue difficulty remains unassessed rather than fictional mockup ratings. |
| Practice and completion | practice-recovery, practice-light, practice-cs-large, completion, completion-picker, piece-note-fields | Main timer styling retained, active note inputs removed, recovered draft paused. Persisted completion shows actual time/count and safe optional editing. Long details use scrolling. |
| Profile | profile, profile-light, profile-cs-large, showcase, profile-editor | Biography beside avatar, no subtitle/history duplicate, centered compact statistics with 2×2 large-font fallback, larger medals and direct + selection. Pencil retains all personal editing fields. |
| About / legal | about, about-light, about-cs-large, legal | Actual installed icon/version, three balanced actions, stacked narrow/large-font fallback, credits and accessible notice/license text. Lower actions/credits scroll into view on smaller displays. |
| Tux / keyboard | tux, tux-light, tux-cs-large, keyboard | Pink terminal and monospace whitespace retained; non-wrapping ASCII with horizontal/vertical scroll fallback. Creator line may wrap at large fonts. Logo long-press and seven version taps still open the two easter eggs. |
| Appearance | appearance, appearance-light, appearance-bottom, appearance-cs-large | Compact live theme/font/accent/card/density/sort previews use supported saved values. Settings remain scrollable; no invented options. |

Additional final-artifact dark/130% captures inspected: home-dark-large, library-dark-large, practice-dark-large, completion-dark-large, profile-dark-large, about-dark-large, tux-dark-large and appearance-dark-large.

Normal display: 1080×2400. Narrow display: 840×1800 at 130% system font scale; both English and Czech were exercised, with light/dark themes. Offline/online emulator Wi-Fi/data transitions retained personal Library/Home and cached fallbacks. Instrumentation also tests denied media consent, cache reuse/clear/failure and quote offline fallback. Portrait placeholders are valid when no licensed cached image is available. No new portrait files are bundled or published in the selected new Wiki captures.

## Limits and recovered test-environment failures

The interrupted build was not accepted as final evidence. A fresh clean build and final refinement checks replaced it. AVD creation in /tmp failed due to space; software/host graphics startup also stalled or crashed. The preserved test AVD booted with ANGLE, 4096MB and virtual cameras disabled; all final emulator gates ran there. Test failures caused by incorrect automation labels/offscreen targets were corrected and the relevant interactions rerun.

No physical-device checks, new Google-account end-to-end login, real mobile carrier billing/transport, acoustic instrument-detection accuracy, screen-off hardware audio fidelity or packet-capture audit was performed. These are not claimed. Existing Google identity implementation is unchanged; automated identity preservation tests pass. Legal/controller/audience/provider-policy uncertainties are listed precisely in LEGAL-AUDIT-0.10.1.md; no legal certification is claimed. 0.11 remains planning-only.

created by stokonn with love <3
