# Stage 3B verification — 2026-10-18

Local mandatory acceptance passed on the final production source. Git commit/push and remote CI are recorded at the final checkpoint below. This is unreleased development; production remains0.10.1. No merge, tag, release, cloud service or Stage4 work.

## Source and protected state

Base `cb36535d8a733143213347dd2ce355d1bf81ae96`; prerequisite Stage3A CI39125571414 passed. Branch `feature/goldberg-0.11-stage-3b`. Personal `opus.db`9→10 is explicit/additive; independent catalogue remains1. Historical schemas1–9, original historical fixture files, application/manifest/Gradle configuration and signing identity are unchanged. The new Android schema9 asset exactly matches the retained Room9 export.

[Source/artifact manifest](results/source-artifact.json) hashes174 production/test/schema/configuration files and both APKs. Final app APK: `app/build/outputs/apk/debug/app-debug.apk`, SHA256 **14b19f8085f7a9dca189e80e6743e082c8add732cd31fec5a675c9f07d682f4a**. Verified package **com.opus.piano**, version **0.10.1/code11**, ZIP/resources/canonical artwork and trusted compatible signing SHA256 **53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e**. No instrumentation fixtures or private reference images are packaged in the app APK. This verification APK is not an official new0.10.1 release.

Trusted published0.10.1 APK stays `.gradle/release-artifacts/Goldberg-v0.10.1.apk`, SHA256 `854bbfebf617ec920df60d181a12e0dcbd2fed10052f2479903a78a290cef312`. Owner RELEASE_NOTES.md remains untracked with SHA256 `db920f0b1ff88579b6bea9f9da567af16e6db0c7ce5b6439781ca1754fcd4b9b`. External backup, release tags and old APKs are untouched. Active checkout path is unchanged.

## Build, JVM and host checks

```sh
GRADLE_USER_HOME=/tmp/goldberg-gradle-home ./gradlew --offline assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1
python3 scripts/check_migrations.py
python3 scripts/check_backup_fixtures.py
python3 scripts/check_localization.py
python3 scripts/check_history_paging.py
python3 scripts/check_agent_policy.py
python3 docs/planning/goldberg-0.11/stage-3b/check_fixtures.py
python3 docs/planning/goldberg-0.11/stage-3b/verify_stage.py
ANDROID_HOME=/home/stokonn/Android/Sdk bash scripts/verify-upgrade-signing.sh .gradle/release-artifacts/Goldberg-v0.10.1.apk app/build/outputs/apk/debug/app-debug.apk
python3 scripts/verify_quality_apk.py /tmp/goldberg-0.10-release/Goldberg-v0.10.apk app/build/outputs/apk/debug/app-debug.apk 0.10.1 11
```

Passed: debug build, instrumentation compilation, **78 JVM tests /0 failures/errors/skips**, lint **0 errors /89 warnings**, host historical DDL1–9→10 and original-field backup fixtures1–3, **809 CZ/EN resources**, history paging/untimed-credit and canonical policy/Wiki links. Complete v5 full/minimal/unknown fixture hashes, complete SQL rows/FKs/UUIDs pass. Format4 is additionally executed on Android. Host DDL checks do not replace actual Room migration execution. [Host records](results/host-checks.json), [fixture sizes/hashes](results/fixture-sizes.json).

Logs: `/tmp/goldberg011-stage3b-verified-build.log` (final production source), `/tmp/goldberg011-stage3b-final-checks.log` (subsequent test-only Unit-signature correction; same app APK). JVM reports are under app/build/test-results/testDebugUnitTest. Lint's UsableSpace/SharedPreferences style warnings remain; no error suppression was introduced. Actual source/artifact and protected-file checks passed after the harness correction.

## Actual Android execution

Sequential disposable x86_64 emulators only: API26 `emulator-5584`,2 cores/1536MiB/480×800; API37 `emulator-5582`,2 cores/4096MiB/1080×2400; software graphics. Every ADB operation explicitly targets an identified emulator and ro.kernel.qemu was verified. No physical phone or owner testing.

| Final suite | Entries passed | Time | Evidence |
|---|---:|---:|---|
| API26 |75/75 |61.003s |[record](results/instrumentation-api26.json), `/tmp/goldberg011-stage3b-final-api26-suite.log` |
| API37 |75/75 |61.584s |[record](results/instrumentation-api37.json), `/tmp/goldberg011-stage3b-complete-api37-full.log` |

Both suites ran with no active default network. Each includes73 assertion-bearing cases plus two argument-driven process-death entrypoints that return without phase arguments in an ordinary suite; actual death/restart execution is separate below. Compilation alone is never counted as execution.

Coverage includes every historical Room1–9 upgrade/validation/FKs, migration rollback, v1–4 readers, full/minimal v5, raw invalid UTF-8/unknown versions, source/destination conflicts/archives, strict malformed fields/numbers/hashes/versions/references, manual/missing catalogue snapshots, arrangements/movements/medley/multiple timed parts/untimed links, stable UUIDs, catalogue corruption/deletion/scope rebuild, navigation, existing journal/recovery and permission policy. Fresh-database restore reserves future owner IDs so deleted anchors cannot be adopted by new rows. Pre-start selections/notes recover with zero credit; duplicate completion leaves an unrelated draft untouched. Main-dispatcher recovery-byte calls execute successfully through IO dispatch.

The completion test verifies8000ms source history→9500ms after one1500ms completion, duplicate completion unchanged, then restoring the earlier8000ms journal cannot revive that completed draft/token. Conflict evidence prevents a different duration from receiving automatic credit. Raw original bytes remain available throughout.

## Restore interruption and actual process death

```sh
adb -s emulator-5584 shell am instrument -w -r com.opus.piano.test/androidx.test.runner.AndroidJUnitRunner
adb -s emulator-5582 shell am instrument -w -r com.opus.piano.test/androidx.test.runner.AndroidJUnitRunner
python3 docs/planning/goldberg-0.11/stage-3b/process_death.py --serial emulator-5584 --output /tmp/goldberg011-stage3b-final-api26-process-death/result.json
python3 docs/planning/goldberg-0.11/stage-3b/process_death.py --serial emulator-5582 --output /tmp/goldberg011-stage3b-api37-process-death/result.json
```

Both APIs: actual process termination at **before_commit, after_commit, after_preferences, before_ack**, each followed by a successful separate-process restart verification. The four preparation runs per API deliberately produce `shortMsg=Process crashed.` and are **not passed instrumentation tests**. Fsynced markers and four subsequent OK(1 test) verifications prove execution/restart. [API26](results/process-death-api26.json), [API37](results/process-death-api37.json).

Before commit: original notes/draft bytes and journal survive. After commit: restored8000ms journal, retained destination bytes and pending preferences survive; explicit replay is idempotent and acknowledges applied only after completion. The full suites additionally inject failures at validated, destination_preserved, before_commit, after_commit, before_preferences, after_preferences and before_ack, reopening Room and checking original/committed state. AtomicFile publication is exercised with low-space preflight and injected ENOSPC/interrupted-write rollback, plus oversized streamed input. These are controlled storage-failure simulations, not a deliberately filled emulator disk or physical power-loss certification.

## Trusted seeded in-place upgrades

```sh
python3 scripts/verify_emulator_upgrade.py --serial emulator-5584 --baseline .gradle/release-artifacts/Goldberg-v0.10.1.apk --baseline-version 0.10.1 --expected-schema 10 --candidate app/build/outputs/apk/debug/app-debug.apk --output /tmp/goldberg011-stage3b-complete-upgrade-api26 --seed-disposable
python3 scripts/verify_emulator_upgrade.py --serial emulator-5582 --baseline .gradle/release-artifacts/Goldberg-v0.10.1.apk --baseline-version 0.10.1 --expected-schema 10 --candidate app/build/outputs/apk/debug/app-debug.apk --output /tmp/goldberg011-stage3b-complete-upgrade-api37 --seed-disposable --offline
```

Both passed on fresh emulators: same signed package installed with -r, **37 sessions**, exact compared personal rows/IDs/title snapshots/notes/profile/badges/repertoire/raw drafts/archives and appearance/media/identity preferences unchanged; Room10/FKs/UUID anchors valid. API37 upgrade itself ran offline; API26's subsequent full suite/backup tests ran offline. The test-only JUnit signature fix did not change the app APK; API26's installed APK hash was explicitly matched again before rerunning its final75 cases. No uninstall/signature bypass. [API26](results/upgrade-api26.json), [API37](results/upgrade-api37.json).

The helper excludes disposable catalogue/cache and additive bridge/replay/evidence tables from equality comparisons; separate Android historical migration tests compare seeded legacy catalogue tables too. Raw SQLite fixtures are copied/checkpointed with WAL sidecars, avoiding the earlier historical WAL-copy issue. The37-session fixture's intentionally incomplete old timing draft remains exact bytes and may require recovery export; no missing intervals are fabricated to make it resumable.

## Real app UI and settings integration

In addition to isolated Room suites, actual offline API37 UI import through Android's document picker restored the full fixture into the app database. Process stop/reopen inspection confirmed2 sessions/8000ms, profile, exact active raw bytes, applied preference state, Czech/Slate, inactive cellular consent and no microphone permission/service. The original destination draft survives in evidence and the complete37-session destination snapshot remains private and readable. [Live record](results/live-ui-api37.json).

Inspected API26 dark English backup panel at font1.3 on480×800: buttons, v5/historical-format/plaintext explanations and system import picker are reachable by scrolling. Inspected API37 English confirmation, settled Czech navigation/profile after live import, and Czech/light Appearance and backup panels. Real offline UI export through Downloads produced53,997B with valid section/root hashes, Room10 SQL/FKs, current light/Czech settings and exact paused draft bytes; SHA256 `79e21cc1ca5b313a9d64a5d960c3d864d6aa3723c393baa338160a8aa5797304`. Screenshots are local `/tmp/goldberg011-stage3b-api26-ui/` and `/tmp/goldberg011-stage3b-api37-ui/`; none are committed. Intermediate captures taken during locale recreation are not evidence of a settled render.

## Sizes and measured costs

Golden formatted input: full20,498B, minimal2,283B, unknown19,006B. The actual Room workload has1002 sessions,1,024,000 session-note bytes and2,101,248 raw evidence bytes; canonical exported backup **4,837,707B**. Raw bytes over Android's2MiB CursorWindow survive repeat restore/export exactly.

| Measured workload | API26 | API37 |
|---|---:|---:|
| Export |569ms |731ms |
| Validation |2324ms |1462ms |
| Restore |4411ms |3641ms |
| Repeated restore |4581ms |3629ms |
| PSS before /after |120494/148974KiB |188281/206509KiB |

[API26 raw record](results/performance-api26.json), [API37](results/performance-api37.json). PSS snapshots are not peak-memory measurements. Proposed regression investigation guards for this exact fixture/emulator setup: export2s, validation5s, restore10s; after-minus-before PSS64MiB. These are measurement-based development budgets, not phone guarantees or unlimited backup promises. The32MiB ceiling itself is not a measured maximum-size performance claim.

## Resolved failures and limitations

The initial v5 JVM run exposed the old unreadable-recovery guard; ordinary replacement still refuses to overwrite it, while the explicitly preserving backup path saves originals first. A compiler helper receiver mistake was corrected. One lint FIR run failed while source files were being edited; final analysis held source stable and passed. Initial Android serialization was slow: ART sampling identified per-character ICU encoding; buffering preserves canonical bytes and resolves the hotspot. One full API26 run failed prior-UUID retention on legacy import; registry/evidence preservation was corrected without weakening that existing assertion. A later full run found a non-void inferred Kotlin test method; explicit Unit signatures corrected test discovery and all19 Stage3B cases executed in each final suite. The host fixture checker initially assumed JSON object order matched FK insertion order; it now inserts in actual Room entity order and validates both canonical real UI exports and golden fixtures with FK enforcement. A shell UI locator selected a section heading rather than its navigation row; the corrected actual row successfully selected light mode. Failed/superseded runs and transient screenshots are not final acceptance evidence.

Bounded parsing/output (32MiB input/output, depth24,500,000 nodes,100,000 rows/table,24MiB raw read), plaintext files, retained private snapshots and SAF's lack of atomic external replacement are explicit limits in the implementation contract. No truncation, automatic recovery-copy GC, encryption, larger streaming format, provider/cloud/auth/download work or hardware microphone accuracy is claimed. Existing source bytes remain untouched on rejection. Google account/OAuth behavior is unchanged and account-free offline practice is retained.

## Final checkpoint

Local mandatory gates passed. Branch commit/push, clean remote CI and synchronized tracking/Wiki are recorded in the final report; no release or main merge is authorized by this checkpoint. Stage4 requires a separate instruction.

created by stokonn with love <3
