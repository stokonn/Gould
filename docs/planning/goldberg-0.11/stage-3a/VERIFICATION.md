# Stage 3A verification ledger

Final-source verification is recorded here separately from the interrupted exploratory runs. No physical device, release, merge, tag or cloud provisioning is involved. Application version remains the development baseline **0.10.1/code11**; schema9 on this branch is not the published 0.10.1 APK.

## Executed host checks

```sh
GRADLE_USER_HOME=/tmp/goldberg-gradle-home ./gradlew --offline assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1
python3 scripts/check_migrations.py
python3 scripts/check_backup_fixtures.py
python3 scripts/check_history_paging.py
python3 scripts/check_localization.py
python3 scripts/check_agent_policy.py
python3 docs/planning/goldberg-0.11/stage-2/research.py check
python3 -m py_compile scripts/check_migrations.py scripts/check_backup_fixtures.py scripts/verify_emulator_upgrade.py
```

Production implementation Gradle log `/tmp/goldberg011-stage3a-final-gradle.log`: **BUILD SUCCESSFUL in 1m37s**, 83 tasks (20 executed/63 up-to-date), incremental build. **77 JVM tests, zero failures/errors/skips**. Instrumentation assembly is separate from actual execution below. Lint: **0 errors, 83 warnings** (existing baseline count). Final test-only navigation synchronization build `/tmp/goldberg011-stage3a-final-gradle-navigation.log` also **passed in 41 s**, 83 tasks (9 executed/74 up-to-date). JVM outputs remained unchanged at 77 passed; lint still had zero errors/83 warnings. The app APK hash did not change. No new dependency or build configuration change.

Host migration check validates legacy SQL paths1–8 and additive schema9 DDL against the Room export; allocation/view/trigger behavior is covered by actual Android tests, not falsely represented as host execution of Kotlin. Three historical backup fixtures retain all original fields. Existing paging check covers106 sessions/four pages/untimed associations; localization804 CZ/EN resources; policy/Wiki navigation and49 Stage 2 research contract checks pass. Stage 2's research-only changed-path guard is intentionally inapplicable to a production-persistence stage and is not claimed passed.

## Actual Android execution

Both explicitly identified emulators must report `ro.kernel.qemu=1`. Suites use disposable database filenames and authored Stage 2 CC0 assets; no production installation data or physical phone is used.

```sh
adb -s emulator-5584 shell am instrument -w -r com.opus.piano.test/androidx.test.runner.AndroidJUnitRunner
adb -s emulator-5582 shell am instrument -w -r com.opus.piano.test/androidx.test.runner.AndroidJUnitRunner
```

- **API26 x86_64 / emulator-5584:** final full suite **53 passed in 36.596 s**, `/tmp/goldberg011-stage3a-final-api26-settled.log` (the earlier sequential suite also passed53 in32.374s). Official image `x86_64-26_r01.zip`, 474178332 bytes, published SHA1 `432f149c048bffce7f9de526ec65b336daf7a0a3`, verified before extraction. A fresh final AVD preserved the interrupted AVD's test data. Android Settings automation enabled airplane mode; connectivity showed **no active default network/current networks** before and after testing.
- **API37 x86_64 / emulator-5582:** final full suite **53 passed in 31.017 s**, `/tmp/goldberg011-stage3a-final-api37-settled.log`. Both Wi-Fi and mobile data were disabled; connectivity reported no active default network/current networks.

New cases cover schema1–8→9 with Room validation/FKs and exact old-column SQLite type/HEX bytes; failed personal migration rollback; persisted UUIDv4/reopen and numeric work/artist separation; manual name collisions;26 authored multi-genre entities, independent arrangements/movements, medley/mashup sources, edition/recording/collection relationships; alias/FTS4 quoting, paging and bounds; wrong-kind/cyclic mappings, reversible redirects/revision evidence; rights rejection; catalogue deletion/corruption/rebuild/reopen, interrupted copy rollback and repeated initialization; journal operation despite failed catalogue opening; historical backups1–4 and pending-recovery restore rejection. Existing tests cover timing/completion-token idempotency, profile/favorites/notes/links/badges, recovery, cache policies, navigation and identity persistence. Performance case exercises the real PracticeCoordinator under catalogue query load with one reconciled completed session.

## Seeded signed in-place upgrade

```sh
python3 scripts/verify_emulator_upgrade.py --serial emulator-5584 --baseline .gradle/release-artifacts/Goldberg-v0.10.1.apk --baseline-version 0.10.1 --expected-schema 9 --candidate app/build/outputs/apk/debug/app-debug.apk --output /tmp/goldberg011-stage3a-final-upgrade26 --seed-disposable
```

**PASS on final APK**, while offline. Installs use `-r`; no uninstall/signature workaround. Baseline is the immutable published0.10.1 APK SHA256 `854bbfebf617ec920df60d181a12e0dcbd2fed10052f2479903a78a290cef312`. Exact equality after upgrade:37 sessions, repertoire, measured parts, notes/reflection, profile/badges, recovery archive, active paused draft and selected appearance/media/identity preference file hashes. Bridge:3 UUID identities/113 anchors; schema9/FKs valid. WAL-safe capture commits and checkpoints the host fixture before copying; snapshot copies main/WAL/SHM before host checkpoint. Evidence: [upgrade-api26.json](results/upgrade-api26.json); full synthetic before/after snapshots remain local under the command's output directory.

The upgrade script excludes disposable legacy catalogue/cache tables from its equality comparison; separate Android historical migration tests seed and compare those tables as well, including work/composer/cache data. Session-token/interval/favorite/recording edge cases are independently seeded in the Android migration tests rather than falsely attributed to the37-session fixture.

## APK, source and protected work

```sh
ANDROID_HOME=/home/stokonn/Android/Sdk bash scripts/verify-upgrade-signing.sh .gradle/release-artifacts/Goldberg-v0.10.1.apk app/build/outputs/apk/debug/app-debug.apk
python3 scripts/verify_quality_apk.py /tmp/goldberg-0.10-release/Goldberg-v0.10.apk app/build/outputs/apk/debug/app-debug.apk 0.10.1 11
```

PASS: **com.opus.piano,0.10.1/code11**, valid ZIP, unchanged canonical packaged artwork/resources, trusted signer SHA256 `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`. APK `app/build/outputs/apk/debug/app-debug.apk`, SHA256 **`e77c832cccd419ae0c34efc70f5539accd7e0645097ad1d72b0c33f48f0d2abd`**. The version-increment artwork script compares the older code10 baseline; the separate certificate check compares the actual published0.10.1 APK. This is an experimental local APK, not a new official0.10.1 release.

[Source/artifact manifest](results/source-artifact.json) hashes all main sources/resources and build configuration. Test/private-reference assets are absent from the app APK. Authored fixture copies match Stage 2 byte-for-byte; schema1–8 exports remain unchanged. Untracked owner `RELEASE_NOTES.md` SHA256 remains `db920f0b1ff88579b6bea9f9da567af16e6db0c7ce5b6439781ca1754fcd4b9b`; external backup and release tags/artifacts remain untouched.

## Performance and inspected renders

Actual Room benchmark: 10,000 synthetic works/aliases, 31 warm 40-result token-prefix queries, plus 100 concurrent catalogue queries while a real PracticeCoordinator initializes/completes one manual session. These are emulator observations, not phone guarantees or arbitrary substring comparisons.

| Metric | API 26 | API 37 |
|---|---:|---:|
| Transactional build | 16,850.639 ms | 7,539.267 ms |
| Query median / p95 | 2.462 / 2.887 ms | 1.956 / 2.635 ms |
| Journal ready during catalogue load | 27.127 ms | 12.142 ms |
| Catalogue file after checkpoint | 7,348,224 bytes | 7,348,224 bytes |
| Completed credit / part sum | 251 / 251 ms | 251 / 251 ms |

Raw [API 26](results/performance-api26.json) / [API 37](results/performance-api37.json) records retain every sample. Proposed reproducible guardrails for these fixtures/emulators: bounded query p95 ≤10 ms at 10k, journal ready under this load ≤250 ms, no root/alias materialization into UI. Catalogue bulk copying remains outside startup. These allowances derive from the observed maxima, not a universal hardware SLA. Rich provider joins, large-catalogue growth, bundle/media decoding and broader timing fairness remain later-stage measurements.

[Five matched API 37 process launches](results/startup-api37.json):1426/1520/1611/1542/1597 ms; median **1542 ms**, versus Stage 2 1650 ms. Idle PSS131439–131742 KiB, versus baseline130086–130428 KiB. No 20% launch/10% PSS investigation guard was triggered. Same emulator/4 GiB configuration; ordinary process starts, not cold filesystem boots. Existing disposable data/settings changed during regression tests, so this is a regression screen, not proof of a speed improvement.

Actually inspected local captures: `/tmp/goldberg011-stage3a-api26-ui.png` (480×800, light, font scale1.3) and `/tmp/goldberg011-stage3a-api37-dark.png` (1080×2400, dark). Journal/home data and established navigation render after migration. Only startup/Home renders are claimed; no new full design acceptance or new provider UI exists. Screenshots are not committed.

## Recovered failures and limitations

The first targeted run found incorrect FTS4 wildcard placement outside quotes; corrected and regression-tested. Both first full suites completed53 tests with two failures: old test expected schema8, and outer UPDATE's conflict policy overrode trigger INSERT OR IGNORE. Corrected version assertion and explicit absence predicates fix the actual trigger conflict. The first API26 svc command exited137; API26 shell cannot send the protected airplane broadcast. Settings UI automation performed the legitimate network action instead. No permission bypass was used. API 37 then reproduced a pre-existing navigation test lifecycle crash. The test had treated two scheduled frames as transition completion; it now waits for the current entry to be RESUMED and outgoing visible entries to disappear. Production navigation code is unchanged. The final API 37 full suite passed with this synchronization fix.

The connection failure terminated emulators/ADB and a Gradle job with143; that Gradle run is **not** treated as successful. Final verification runs one Gradle job with emulators stopped, then one emulator at a time. No hardware microphone accuracy, physical-device OEM behavior, real provider ingestion, cloud auth, offline bundle streaming, UI redesign or v5 restore is claimed. No manual user acceptance is required. Local FTS paging is not the future provider/generation search contract. Public OAuth configuration remains outside scope.

Stage 3B must add portable UUID/settings/raw-draft backup v5, safe multi-store restore/recovery conflict handling and full golden fixtures before claiming complete Stage 3 backup acceptance. Old-format exports cannot carry new UUIDs across installations. Catalogue generations are retained until separately verified safe GC exists. A newer journal cannot be downgraded by opening it with an old APK.

created by stokonn with love <3
