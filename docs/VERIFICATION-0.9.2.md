# Goldberg 0.9.2 — release verification

Final application acceptance review: **PASS**. The previously outstanding six Google scenarios were individually confirmed by the owner on 2026-10-17. Those are owner-reported manual results, not automated tests or independently observed emulator results. No critical mandatory application blocker remains. Publication and remote asset verification are recorded separately in the release record.

## Source and artifact identity

The final APK is unchanged from the source that passed 70 JVM and 31 executed Android tests. All 139 files in `verification/0.9.2/source-manifest.sha256` matched again during the release review; no application code changed after that run. Documentation changes do not invalidate those results. Historical development findings and superseded blockers are retained in `verification/0.9.2/development-history.md`.

- Package/namespace: `com.opus.piano`; version `0.9.2`; versionCode `9` (published 0.9.1: code 8).
- APK: `/home/stokonn/Projects/Opus/app/build/outputs/apk/debug/app-debug.apk`.
- APK SHA-256: `7470fe7b477678946040df452813fc47708489883543bb3ea57bb2148bab4e7f`.
- Actual signing SHA-256: `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.
- Actual signing SHA-1: `4F:BB:35:7F:14:74:0D:B3:6F:CE:48:67:27:50:75:57:D4:E3:C4:20`.

Actual certificate comparison with the downloaded trusted 0.9.1 release APK passes. Both use the existing compatible Android Debug signing identity. This is disclosed, not silently substituted for a production key. No key rotation, uninstall or package change occurred. Archive integrity, package/version/code, supplied transparent light/dark/monochrome artwork pixels and adaptive mask bounds pass. Google Cloud configuration was owner-supplied; Console registrations were not independently inspected.

## Owner-reported manual Google verification

The owner personally tested the latest installed 0.9.2 build on a Samsung Galaxy A55. **Codex did not perform these physical-device tests and executed no commands against the owner's phone.** No exact APK digest or device logs accompanied the report; do not infer unrelated results.

| Scenario | Result | Reported observation |
| --- | --- | --- |
| Google display-name import | PASS — owner-reported manual verification | Account name retrieved and displayed |
| Google profile-picture import | PASS — owner-reported manual verification | Imported after required media consent |
| Manual nickname preservation | PASS — owner-reported manual verification | Customized nickname not overwritten |
| Authentication persistence | PASS — owner-reported manual verification | Signed-in state retained after full close/reopen |
| Sign-out and local data preservation | PASS — owner-reported manual verification | Sign-out worked; existing local data remained intact |
| Google account switching | PASS — owner-reported manual verification | Another account selected without losing existing local data |

Real Google sign-in and an in-place installation were also reported by the owner earlier. These observations do not certify all data fields, all network conditions, every provider failure or unrelated UI/migration behavior. See `MANUAL-VERIFICATION-0.9.2.md`.

## Executed automated checks

```
./gradlew assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1
python3 scripts/check_migrations.py
python3 scripts/check_backup_fixtures.py
python3 scripts/check_history_paging.py
python3 scripts/check_localization.py
python3 scripts/check_agent_policy.py
python3 scripts/verify_quality_apk.py /tmp/goldberg092-baseline/Goldberg-v0.9.1.apk app/build/outputs/apk/debug/app-debug.apk 0.9.2 9
adb -s emulator-5580 install -r app/build/outputs/apk/debug/app-debug.apk
adb -s emulator-5580 install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s emulator-5580 shell am instrument -w com.opus.piano.test/androidx.test.runner.AndroidJUnitRunner
```

- Final Gradle run: **BUILD SUCCESSFUL**, 1m 18s; **70 JVM tests, zero failures**.
- Actual Android execution: **31 tests passed**, 15.24 seconds. Instrumentation was executed, not merely compiled.
- Lint: **zero errors, 57 warnings**. Reviewed warnings include 24 unused resources, 14 dependency-update notices, eight KTX suggestions and 11 other suggestions/warnings. CredentialManagerMisuse requests an explicit NoCredentialException reference; ProfileViewModel already catches failures and shows a localized error. Warnings are not claimed resolved.
- Host migrations: schemas 1–6 into schema 7 retain original tested fields and match the Room export.
- Historical backup fixtures: format 1/schema 4 and format 2/schema 6 retain all original portable fields. Android format-3 round-trip and recovery-archive tests pass.
- History: 106 sessions over four pages; stable equal timestamps, multi-part deduplication and piece/catalogue attribution pass.
- Localization: **755 CZ/EN resources**, placeholders/plurals/declared locales pass.
- Canonical policy, thin agent adapters and Wiki navigation pass. Other AI-agent runtime loading was not tested.

The new avatar regression first failed with a real division by zero, then passed after initializing the decoder sample size to 1. Small 16×16 and large 1024×768 PNGs exercise actual Android decoding. The defect and fix are preserved in the retained logs. This decoder test is separate from the owner's authenticated image-import result.

## Data and upgrade acceptance

Room schema 6→7 explicitly adds favoriteArtist, favoritePiece, favoriteEra and biography with empty defaults, plus unchanged-payload recovery archives. Backup format 3 includes these additions and reads formats 1/2. Older app versions cannot read format-3 backups. No destructive fallback or database recreation exists.

Executed recovery/startup failure, exact timer/multi-piece attribution, idempotent completion, backup/restore, avatar/profile persistence, showcase and cache-isolation checks pass. A manual-recovery Long/Int zero mismatch was corrected to 0L and covered by regression tests.

Earlier seeded emulator verification installed trusted 0.9.1 then upgraded in place: 37 sessions, two pieces, exact timing/parts/intervals, unfinished draft, profile and existing badge date retained. Expected metadata backfills/eligible badge additions are documented; this is not byte-for-byte database identity. That historical upgrade evidence remains applicable: subsequent changes affected presentation and bounded avatar decoding, not the migration or persistence contract. Final emulator candidate installations used install -r, never uninstall. The owner's six Google reports add only the specific local-data-preservation observations stated above.

## Rendered UI acceptance

Actual screenshots were inspected against the written editorial design specification. Original visual mockups are unavailable. Representative light/dark, English/Czech, 360dp-width and 130% font-scale checks pass; scrolling remains an accessibility fallback. This is not an exhaustive cross-product of every screen/theme/font/device.

Inspected destinations include Home and real seven-day activity/empty state, Repertoire search/Add/Sort/Filters, Practice, Profile, Settings/Appearance/Data & Storage, onboarding, piece/composer details, badge collection, practice review/history, recording editors, About/legal, Bach's Secret and Tux. Nested notes/recording editors retain focused fields above the keyboard. Practice controls remain above navigation; duplicate IME padding is absent. Home shows actual Free Practice history when no recent repertoire piece exists. The connected keyboard retains the existing thirteen-note audio engine and correct two-plus-three dark-key grouping; the exact Tux signature is visible.

Current persistent captures and logs are indexed in `verification/0.9.2/README.md`; older lost /tmp images are identified as historical evidence in the development log. New release-review captures include offline badges/catalogue, online retry and composer detail.

## Network and identity failure acceptance

Executed fixtures cover offline/cached daily quotations and once-per-day selection, media consent denial/allow, cache reuse/clearing, download failure and consent revocation without orphan cache files. These are deterministic fixtures, not claims of live upstream reliability.

Earlier live emulator checks passed unmetered Wi-Fi optional Vivaldi content download, cached content after offline process restart, cellular denial without consent, and Debussy completion on cellular with explicit consent while Wi-Fi stayed disabled. Defaults/connectivity were restored. On final review, Google's actual provider showed its no-network error while Wi-Fi/data were disabled; Back returned with a localized cancellation message and unchanged local test profile/history/repertoire. Offline catalogue fallback and badges rendered, then online catalogue retry succeeded. No successful Google account was synthesized on the emulator.

## Non-blocking limitations and workflow

Smart Practice remains experimental; physical-device screen-off capture, battery behavior and a broad hardware/Android-version matrix are not certified. Audible harpsichord playback was not heard on the no-audio emulator; its existing engine was preserved. External catalogue/media/quotation/Google services have no availability guarantee. Claude/Gemini/Cursor/Copilot runtime loading remains unverified, with supported instruction files and provenance checks documented. Project is private to authorized accounts; its Board layout and Status grouping are verified.

The permanent Project, Issues/milestones, English Wiki, README/changelog, AGENTS.md and thin adapters are maintained. Google Issue #7 and QA #16 may close only with verified source evidence; release #17 closes after successful remote publication/asset verification. Never infer publication from this acceptance report alone. See `RELEASE-0.9.2.md` for release contents and limitations.
