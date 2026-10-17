# Historical development log — superseded status

Earlier blocked/unverified statuses below describe the time recorded, not current acceptance. See docs/VERIFICATION-0.9.2.md for the current report.

# Goldberg 0.9.2 — development verification

Status: **UNRELEASED; publication blocked by remaining unverified Google account behaviors and incomplete full UI acceptance matrix.** This is not a claim that all 0.9.2 requirements are complete.

## Source and implementation

Branch `feature/goldberg-0.9.2`; application HEAD remains the published 0.9.1 baseline `9eeabd798dcf8c70766a530f32835e499d68da3b`. App changes are local and uncommitted. No application push, release tag or 0.9.2 release was made. Unrelated RELEASE_NOTES.md is preserved.

Implemented work includes centralized editorial theme/shared buttons/rows, daily Home activity/quotes, pinned repertoire controls, Practice-only session start, compact profile/favorites/bio/showcase management, metered media consent/cached portraits/optional supplementary downloads, connected keyboard/Tux, localization, permanent agent policy and GitHub documentation/tracking. See MODIFIED-FILES-0.9.2.md and PLAN-0.9.2.md.

Google identity uses AndroidX Credentials/Credentials Play Services Auth 1.5.0 and Google ID 1.1.1. Google ID 1.2.1 failed compilation because its Kotlin metadata exceeds this project's compiler; the compatible stable version is used. The real supplied public Web client ID is configured; no client secret or fake authentication exists. Manual/legacy nicknames and existing avatars remain authoritative; optional Google photo import is bounded and consent-controlled. Account binding persists separately from portable backups. Tokens are never stored or logged. Settings sign-out retains the local journal/profile. See GOOGLE-IDENTITY-SETUP.md.

## Data changes

Room schema 6→7 explicitly adds four default-empty profile personalization fields and a recovery archive table. Confirmed/exported failed-recovery drafts are archived unchanged transactionally rather than silently deleted. Backup format 3 exports those additions and retains format 1/2 readers. No destructive fallback or package/signing change.

Actual instrumentation exposed a manual-recovery validation defect: nullable detection duration used an Int zero while comparing Long totals. The fix uses 0L; a JVM regression and executed Android recovery tests cover it. Test-fixture optional indices and asynchronous locale/navigation synchronization were corrected; assertions remain intact.

## Executed checks

```
./gradlew clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1
./gradlew assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1
python3 scripts/check_migrations.py
python3 scripts/check_backup_fixtures.py
python3 scripts/check_history_paging.py
python3 scripts/check_localization.py
python3 scripts/check_agent_policy.py
python3 scripts/verify_quality_apk.py /tmp/goldberg092-baseline/Goldberg-v0.9.1.apk app/build/outputs/apk/debug/app-debug.apk 0.9.2 9
```

The clean Gradle run and both final follow-up runs succeeded. Follow-up runs check the empty-credential guard, bundled legal-notice update and removal of stale ineffective Home/Profile appearance controls after the clean run. Stored legacy layout preferences are retained; the new Home has the specified fixed daily overview and profile editing remains available from Profile. **70 JVM tests passed, zero failures/errors. Lint: zero errors, 57 warnings.** Warnings include available dependency upgrades, unused resources, style suggestions and CredentialManagerMisuse (the repository delegates failures to ProfileViewModel's localized exception handler). These warnings are not represented as resolved.

All host scripts passed: six historical schema upgrades into schema 7, old format-1 and 0.9.1 format-2 backup preservation, 106 history sessions across four pages, 752 matching English/Czech resources with placeholders/plurals, and policy/Wiki link structure. Agent runtime loading was not simulated.

Actual Android execution, only on `emulator-5580`:

```
adb -s emulator-5580 install -r app/build/outputs/apk/debug/app-debug.apk
adb -s emulator-5580 install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s emulator-5580 shell am instrument -w com.opus.piano.test/androidx.test.runner.AndroidJUnitRunner
```

**30 instrumentation tests passed**, including Room migrations, backup/restore, practice recovery, navigation, cache/media permission policy, quote persistence and manual avatar override persistence. Final execution log: `/tmp/goldberg092-owner-update-instrumentation.log`. Earlier runs exposed failures that were fixed; only the final result is reported as passing. Codex performed no physical-phone commands, installation or testing. Separately, the owner reports a successful physical-phone installation and real Google authentication; see MANUAL-VERIFICATION-0.9.2.md.

## Emulator evidence

A separate temporary Google Play emulator (API 37, lavapipe, serial emulator-5580) was used; owner phone/AVD data was not replaced. A controlled schema-6 baseline installed with the trusted 0.9.1 APK upgraded in place: 37 sessions, two pieces, exact timing/parts/intervals, unfinished draft, local/profile fields and existing badge date retained. Existing startup metadata backfills and eligible badge additions are expected; this was not a byte-for-byte database claim. Final candidate installations also used install -r, never uninstall.

Actual inspected captures are under `/tmp/goldberg092-screens/`: Home/Repertoire/Profile/Settings/About dark, Home/Profile/Practice light and Czech, signed-out Google settings, real Google provider setup and cancellation, piece details, badge collection, Data & Storage, Appearance, corrected keyboard and Tux. Home/Profile and final recovered Practice were also inspected at 360dp width and font scale 1.3. The final Practice capture shows timer, Resume and Finish & Review above navigation without overlap; an earlier blank reconfiguration capture was discarded. The entire screen/theme/font/dialog/network matrix is not yet complete. Verified selected screenshots are preserved in docs/wiki/images and the published Wiki, labelled development captures with test data.

The real provider flow opened Google's account setup. Back cancelled cleanly, showed a localized cancellation message and retained the local profile. **The isolated emulator has no signed-in Google account. The owner separately reports successful real Google authentication on a Samsung Galaxy A55 after adb install -r. Record this as user-reported manual PASS, not Codex testing. Actual Google avatar import, nickname preservation during authenticated synchronization, account switching, provider sign-out, authenticated restart and offline authenticated behavior remain UNVERIFIED.** Pure nickname policy and local-avatar persistence tests do not establish successful provider authentication. Cloud Console registration/consent/test-user configuration cannot be inspected through this environment.

Network/cache policy and failed/offline paths use executed bounded fake HTTP fixtures; these are not a claim that every live upstream Wi-Fi/cellular/interrupted-download combination has been manually exercised. Harpsichord rendering is inspected, but audible playback is not verified because the emulator runs with no audio.

## APK

Path: `/home/stokonn/Projects/Opus/app/build/outputs/apk/debug/app-debug.apk`

- Package com.opus.piano; version 0.9.2; code 9 (published baseline code 8).
- ZIP integrity, light/dark/monochrome supplied artwork pixels, adaptive mask bounds and resources passed.
- SHA-256: `7470fe7b477678946040df452813fc47708489883543bb3ea57bb2148bab4e7f`.
- Signing SHA-256: `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.
- Signing SHA-1: `4F:BB:35:7F:14:74:0D:B3:6F:CE:48:67:27:50:75:57:D4:E3:C4:20`.

The actual 0.9.1 release APK and final debug candidate use the same existing certificate. No additional release Android OAuth client is needed for this certificate; a different future signer needs its own registration. Google Cloud Console registration is owner-reported, not independently verified. This APK is a development artifact, not a published release.

## GitHub and remaining gates

CLI authentication is verified with repo/project/workflow scopes. Permanent Project: https://github.com/users/stokonn/projects/2 ; milestones, labels, 17 linked issues, four statuses, Priority/Version/Work Type and requested views exist. Type is reserved by GitHub, so the custom field is Work Type. On resumption, Status grouping was present but Board used a table layout. The supported API restored BOARD_LAYOUT while preserving grouping; both are now verified. This cosmetic view setting is not an application publication gate. Project remains private: automatic approval review rejected changing its visibility. Do not bypass that restriction.

English Wiki: https://github.com/stokonn/Goldberg/wiki ; verified published master `2864a8836b2ae57fb92f8a7e05ec76e4506a6f03`. Wiki issue #13 is closed/Done with remote commit evidence. Google issue #7 is In Progress/open; release #17 remains open. Other application tasks are not prematurely closed without source commit/complete acceptance evidence. GitHub CI is prepared but not pushed or remotely executed. Agent integration syntax is documented; Claude/Gemini/Cursor/Copilot runtimes were not tested or installed.

Do not publish until the remaining Google account behaviors and full UI/network acceptance are verified. Codex is not authorized to perform physical-device testing; the owner’s separately reported successful authentication is documented without extending it to unconfirmed behaviors. Keep the existing compatible signing key and user data. No 0.9.2 release URL exists.

Final follow-up build log: `/tmp/goldberg092-appearance-final.log` (BUILD SUCCESSFUL). Final small/large-text Practice evidence: `/tmp/goldberg092-screens/practice-small-font130-final.png`; corrected Appearance: `/tmp/goldberg092-screens/appearance-corrected-final.png`.

## Follow-up after the owner’s authentication report

The owner’s Galaxy A55 Google authentication is recorded as user-reported manual PASS in MANUAL-VERIFICATION-0.9.2.md, Google issue #7 and the published Wiki Verification page. It is not Codex physical-device testing. Avatar import, nickname preservation, authenticated restart, switching and sign-out remain individually unverified. The owner did not provide an APK digest; do not claim that the subsequently rebuilt candidate was physically tested.

The candidate was rebuilt after fixing Czech composer collection/sort controls that incorrectly mixed translated labels with internal identifiers, plus untranslated standard catalogue metadata/statistic labels. These are display/filter changes; the Google authentication code is unchanged. User-entered genres/eras, work titles and composer names are not rewritten. Build command: `./gradlew assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1`; logs `/tmp/goldberg092-catalogue-filters-checks.log` and `/tmp/goldberg092-catalogue-localization-checks.log`, both successful. Final counts remain 70 JVM tests, 30 executed instrumentation tests, zero errors and 55 lint warnings. APK signing/resources/version verification passed again, log `/tmp/goldberg092-owner-update-apk.log`.

Live emulator verification now includes an explicitly requested Vivaldi biography/portrait download, offline process restart with downloaded content still displayed, denial on cellular without media consent, and completion of a Debussy download while Wi-Fi remained disabled and media consent was enabled. Wi-Fi and the default consent=false preference were restored. Evidence: `/tmp/goldberg092-screens/composer-download-settled.png`, `offline-home-settled.png`, `composer-cached-offline-settled.png`, `metered-without-consent.png`, `cellular-consented-complete.png` and `metered-consent-restored-off.png`. These are composer-media checks, not Google-avatar verification. Earlier transient loading/splash screenshots are not accepted as settled-content evidence.

The Czech In repertoire filter was exercised against Vivaldi with zero personal pieces and correctly displayed an empty result instead of unadded works. The Not added selection restores works, and catalogue sorting uses its stable identifier. New resources pass the 752-key CZ/EN parity check. Broader full-screen/theme/font/dialog coverage remains separately tracked; do not mark uninspected cases passed.

## Resumed verification — 2026-10-17

All application changes remained uncommitted on feature/goldberg-0.9.2; main is still 9eeabd7. No tag/release was created during the interrupted work. /tmp logs, the temporary emulator and most screenshots did not survive; only the four images in docs/wiki/images remained. Historical evidence above is retained as historical, not silently reclassified as a fresh run.

Recovered command execution record 78581 shows the final previous inset/history command exited **0**, with APK install Success and three reported captures. Earlier exit-1 runs were UI control lookup failures (Settings, Díla instead of Skladby, or a Czech selector on an English screen), not APK installation failures. One transient UI-automation null-root message was retried successfully. The lost images are being recaptured. The new evidence helper also encountered Android's misspelled `UI hierchary dumped` status line; accepting the stable `dumped to:` suffix fixed that helper issue without changing app code.

Current source applies IME avoidance once to the root Scaffold, consumes scaffold padding in the child content, and removes redundant Practice bottom-bar IME padding. Current Gradle build/unit/instrumentation compilation/lint passed (`/tmp/goldberg092-resume-checks.log`); 70 JVM tests, zero failures, lint zero errors/57 warnings. All host scripts passed again, including 755 matching CZ/EN keys. Thirty instrumentation tests were executed on a newly created isolated temporary API-37 emulator and passed. Fresh logs are preserved under docs/verification/0.9.2 rather than only in /tmp. The owner AVD and physical phone were not used.

Actual English/light keyboard capture now shows the entire focused note field, Resume, Finish & Review and navigation without the former blank inset region. The fresh empty Home shows real zero values and no misleading nonzero bars. The daily quote cache contains a verified online-source entry for 2026-10-17; this is separate from the executed offline/cached fixture tests.

Project Board is now BOARD_LAYOUT grouped by Status. The Google issue remains open; no new authenticated account behaviors are inferred from the owner's successful sign-in. Original design mockups are not available in this workspace; visual comparison uses the written approved specification and actual screenshots, not invented reference-image matching.

Fresh Czech/dark captures also pass the Practice IME check. The review screen, historical review dialog and recording editor were opened with their notes/performer field focused: focused fields remain visible, the recording editor retains Save/Cancel, and long review forms retain their scroll fallback. A controlled two-second manual practice was saved on the isolated emulator; history renders one compact chronological row and opens the full editable review. No owner data was involved.

New persistent captures in docs/verification/0.9.2/screens include empty Home, onboarding, Practice keyboards (EN/light and CZ/dark), Profile, Appearance, compact history, review, piece detail, recording editor, About, Tux and Bach's Secret. They were actually inspected. The source and audio engine were unchanged; audible playback remains unverified. Lint warning review found 24 unused resources, 14 dependency-version notices, eight KTX suggestions and 11 other warnings. CredentialManagerMisuse requests an explicit NoCredentialException reference; the existing general Exception handler already handles that failure with a localized error. No provider-success behavior is inferred from this static review.

At 360dp width and font scale 1.3, Czech Home, Profile and idle Practice were inspected. Text remains readable and the Practice Start/Finish area stays above navigation. The Home check exposed a misleading first-session empty message after Free Practice; Home now falls back to the latest valid recorded session using the shared SessionRow when no recent repertoire piece exists. No timing/data query/schema change was introduced. New build verification follows this display-only correction.

A focused Android regression test exposed a real Google-avatar decoding defect: BitmapFactory.Options defaults inSampleSize to zero, and the import path divided by it before initializing it. The test failed with ArithmeticException: divide by zero (one test, one failure), preserved in docs/verification/0.9.2/avatar-regression-before-fix.log. The real import now calls the extracted bounded decoder with inSampleSize initialized to 1. The regression exercises a 16×16 PNG and a 1024×768 PNG, expecting unchanged small dimensions and a bounded 512×384 decode. This uses actual Android bitmap decoding, not a fake authentication result. Provider/network avatar import remains separately unverified.

## Final resumed candidate — 2026-10-17

After the Home fallback and Google-avatar decoder corrections, the exact command `./gradlew assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1` passed in 1m 18s: 70 JVM tests, zero failures, lint zero errors/57 warnings. The final APK and instrumentation APK were installed with `adb -s emulator-5580 install -r`, then `adb -s emulator-5580 shell am instrument -w com.opus.piano.test/androidx.test.runner.AndroidJUnitRunner` executed **31 tests, all passed** in 15.24 seconds. The prior avatar failure is resolved by the added regression; successful authenticated avatar retrieval is still unverified. Final logs, host script results, APK verification, candidate metadata and source hashes are preserved under docs/verification/0.9.2.

Final APK SHA-256: `7470fe7b477678946040df452813fc47708489883543bb3ea57bb2148bab4e7f`. Package/version/code/signing and supplied light/dark/monochrome artwork verification pass against the downloaded trusted 0.9.1 release APK. Source remains uncommitted at baseline 9eeabd798dcf8c70766a530f32835e499d68da3b on feature/goldberg-0.9.2. The manifest records the actual application/test/build source for this artifact; no later application-code change is included without revalidation.

Release-blocking verification still needed: actual Google display-name/avatar initialization, customized nickname preservation during authenticated synchronization, authenticated restart, account switching and provider sign-out. The isolated emulator has no signed-in Google account. Owner-reported sign-in success remains a separate manual PASS; no other account behavior is inferred. No application commit, push, tag or release is authorized while these mandatory checks remain unverified. The complete UI cross-product and audible playback are also not claimed tested.

English Wiki update published independently at commit 2864a8836b2ae57fb92f8a7e05ec76e4506a6f03; local and remote master hashes match. Google issue #7 and QA #16 now contain the actual resumed results and remaining release gates. No application Issues were newly closed. The permanent Project Board grouping is confirmed in the retained API response.
