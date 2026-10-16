# Goldberg 0.9.1 verification

## Baseline and preservation

Fetched origin branches/tags before development. `origin/main`, feature/goldberg-0.9 and peeled tag v0.9.0 resolve to `4618b50c67e223d7f1b5bf9ee0951f3a79ecfac7`. The published v0.9.0 APK independently reports com.opus.piano, version 0.9.0, code 7. Code 8 is used for 0.9.1. Work is isolated on feature/goldberg-0.9.1.

The pre-existing untracked RELEASE_NOTES.md is excluded from staging and remains unchanged: SHA-256 `db920f0b1ff88579b6bea9f9da567af16e6db0c7ce5b6439781ca1754fcd4b9b`. No user database or phone was modified. Room remains version 6; no schema or destructive migration changes. Existing application ID, signing key, fonts and license notices remain.

The requested ~/GoldbergIconAssets directory was empty. The matching supplied archive `/home/stokonn/Stažené/Goldberg_0.9.1_Icon_Assets.zip` was inspected and extracted into /tmp. Its transparent 432px light/dark/monochrome artwork is packaged under distinct Classic resource names. Editable source SVGs and the supplied provenance README are retained in docs/icon-0.9.1. Starter Android resource files were inspected rather than copied over project files.

## Executed checks

Final source-frozen command:

```
./gradlew clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1
```

PASS: BUILD SUCCESSFUL, 2m 9s, 84 tasks executed. **63 JVM tests, zero failures/errors/skips**. Both application and instrumentation APKs compile. **Lint: zero errors, 23 warnings**, without suppressions. Warnings cover dependency/toolchain updates, existing target SDK 36 and API-33 localeConfig guidance, unused retained legacy resources, an empty obsolete resource folder, a composable modifier-parameter convention and optional KTX substitutions. These are not confirmed correctness/data-loss defects. Dependencies were intentionally not upgraded as part of this quality release.

New JVM tests:

- PracticeFailureTest: nine cases covering failed manual startup with original durable draft retention and retry; failed Smart startup without running identity; retryable read failure without writes/capture; duplicate concurrent Start requests; bounded dirty-age flush under continuous note editing; failed pause checkpoint with retry; immediate Smart startup waiting for initialization without deadlock; matching-export requirement for fresh recovery; and backup replacement refusing to checkpoint over unreadable recovery data.
- JournalFormattingTest: two cases for English/Czech weekdays and explicit locale/time-zone formatting across midnight.
- Existing 52 JVM tests include multi-piece attribution, timing reconciliation, stale Smart evidence, mixed/manual modes, achievements/badges, calendar metrics, navigation ownership, appearance normalization, statistics, reminders, recording links and CSV behavior.

PASS: `python3 scripts/check_migrations.py`: every historical schema 1–5 → 6 path, retained legacy fields/totals/settings/unlock dates, matching Room columns/indexes/foreign keys.

PASS: `python3 scripts/check_backup_fixtures.py`: portable format-1/schema-4 fixture retains every original field through migration. This checks host SQLite fixture preservation, not execution of Android backup code.

PASS: `python3 scripts/check_localization.py`: 689 paired CZ/EN resources, matching format placeholders, Czech one/few/many/other forms and locale declarations. This is not native-speaker or Android rendering verification.

PASS: `python3 scripts/check_history_paging.py`: actual Room history SQL executes on SQLite; 106 sessions across four bounded pages, timestamp ties, duplicate matching parts, legacy attribution, catalogue filtering, insertion of newer sessions during paging and empty results.

PASS: whitespace checks, source hash checks and scoped secret-pattern review. All 125 application source/schema/build files match the frozen manifest in TESTED-SOURCE-0.9.1.json after the final build. Documentation and host verification scripts added afterward do not modify application build inputs. Earlier intermediate compile/test failures were corrected; an overlapping intermediate clean/lint run was discarded. Only the final isolated clean run is the release gate result.

## Artifact and signing

APK: `app/build/outputs/apk/debug/Goldberg-v0.9.1.apk`, byte-for-byte identical to final app-debug.apk.

- Application ID: `com.opus.piano`
- Version: `0.9.1`, code `8`; min API 26, target API 36
- SHA-256: `32b246f158e8d97b13089da0150bd9ddb3bba42bcfe09545b69a571583fee31d`
- Actual certificate SHA-256: `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`

PASS: `scripts/verify_quality_apk.py` compares actual baseline/candidate apksigner certificates to the trusted fingerprint, verifies version/package metadata, ZIP integrity and pixel-identical packaged light/dark/monochrome PNGs. All nontransparent foreground pixels fit the central 72dp circular mask (maximum radius 143.68px against 144px); rounded-square clipping has more room. No square background is baked into a foreground. This is a host geometry check, not launcher certification.

Compiled adaptive XML inspected with aapt: background resolves to goldberg_ivory, foreground to ic_goldberg_classic_foreground and monochrome to ic_goldberg_classic_monochrome. The resource table includes both night/day foregrounds and background colors. Manifest normal and round icons share the adaptive resource; API-31 splash references the new foreground. The old vector notification icon is retained separately.

## Unexecuted checks and limitations

Instrumentation tests are **COMPILED, NOT EXECUTED**. An emulator AVD is configured, but `/dev/kvm` is unavailable; no emulator execution was attempted. Physical-device testing is explicitly waived for this Beta. No ADB phone operations were performed.

This Beta build passed the reported automated checks but has not yet been validated on a physical Android device. Real-device Smart Practice behavior, actual in-place upgrade and launcher rendering remain unverified.

Screen-off detection, microphone/foreground-service behavior, Android accessibility, larger-font rendering, battery behavior and actual installation data preservation are not certified. Existing Android persistence, backup round-trip, navigation and locale tests compile but remain unexecuted. Host timing/SQLite checks do not replace these runtime checks. Recovery exports are raw draft payloads for diagnosis/recovery, not ordinary importable backups. No unresolved mandatory host release blocker remained at completion.
