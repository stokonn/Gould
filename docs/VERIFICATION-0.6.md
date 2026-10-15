# Goldberg 0.6 verification

Verified on 2026-10-15, branch `feature/goldberg-0.6`. Baseline `346bc04` is the committed Opus 0.5 release. The worktree was clean before implementation. No user database was opened, cleared or replaced. No push, release publication, uninstall or signing configuration change was performed.

## Results

- `./gradlew assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug`: **successful**.
- **24 JVM tests pass**, zero failures/errors: 11 existing achievement tests, 4 recording/link tests, 3 catalogue tests, 4 time/attribution/showcase tests, 2 chart boundary tests.
- Lint: **zero errors, 13 warnings** (existing pinned SDK/dependency version notices and optional Bitmap KTX style suggestion). An initial lint run hit an internal FIR analysis exception; subsequent complete runs passed without suppressing checks.
- `python3 scripts/check_migrations.py`: **v1 → v4, v2 → v4 and v3 → v4 pass**. This executes actual migration SQL on host SQLite and compares exported Room columns/indexes. Checks cover original IDs/notes/status/durations, per-piece duration backfill, catalogue cache, saved link/date/preference and first unlock timestamps.
- `git diff --check` and `bash -n scripts/build-apk.sh`: pass.
- `bash scripts/build-apk.sh`: successful. Named artifact matches the standard debug APK byte for byte.

## Android runtime testing remains unverified

All existing/new instrumentation tests **compile**, including seeded schema-3 upgrade; disk reopening with multiple pieces and notes; restore into an existing journal; repeated restore without duplicates; profile/theme/showcase retention; and rejection of malformed/future-version/negative-duration files without losing records. The old schema-1/schema-2 migration tests include the new migration.

**Instrumentation was not executed.** `./gradlew connectedDebugAndroidTest` fails with `No connected devices!`. The available API-37 emulator was attempted in an isolated `-read-only` session that preserves saved emulator data. Both SwiftShader and GPU-off/acceleration-off attempts segfaulted before boot. This environment exposes no `/dev/kvm`.

A successful Android upgrade, restore on an existing installation, gallery/document-picker round trip, visual/font-scale/TalkBack/reduced-motion checks and runtime startup/scroll measurements remain required. No runtime test success is claimed.

## Compatibility and packaging

Committed Opus 0.5 was built separately in `/tmp/goldberg-baseline`. Both APKs pass `apksigner verify` with identical SHA-256 signing certificate digest:

`53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`

This confirms compatibility with this workspace's debug key. A differently signed installed distribution still requires its original signing key; no installed device certificate was available to compare. Application ID remains `com.opus.piano`; version advances from code 2 / 0.5 to code 3 / 0.6.0. Database stays `opus.db`. The internal Home navigation route remains stable while its screen/label become Profile.

Final APK: **18,044,564 bytes**, versus **17,549,380 bytes** for the unminified Opus 0.5 debug APK (about 484 KiB additional features). No new dependency; launcher/splash assets are native vectors/XML. Output:

`app/build/outputs/apk/debug/Goldberg-v0.6.0.apk`

## Performance evidence and limits

- Active timer refreshes reduced from five per second to one; idle polling removed. Exact monotonic deltas are still charged on switching/pause/save.
- Timer collection is isolated to Practice and the restore confirmation, avoiding whole-shell/Profile recompositions on timer ticks.
- Duplicate startup achievement evaluation removed; existing ViewModel evaluation/retry remains.
- Repertoire shares work/composer indexes instead of rebuilding them for every piece. Portraits decode with bounded sampling; gallery avatars are copied to at most 512 pixels.
- History ordering uses the completedAt index. A synthetic host SQLite query of all 10,000 sessions, 30 repetitions, measured median **11.679 ms before / 9.384 ms after** adding the index. EXPLAIN confirms indexed ordering without a temporary sort. This is a host microbenchmark, not Android startup/frame profiling.
- Visible history/catalogue lists are lazy and bounded. Aggregate source lists are still fully loaded; very large journals may need measured database aggregation/paging later. Backups are limited to 32 MiB and 100,000 rows per table.

## Remaining device acceptance

1. On a test device, seed Opus 0.5 with repertoire, notes/statuses, sessions, links and unlocks. Upgrade with `adb install -r` using the matching key, without uninstalling. Verify all data and first unlock dates after restart.
2. Select several pieces; start, switch, pause/resume, remove/re-add, add notes and save. Check parent/part time equality and one global session. Test process recreation and cross-midnight grouping.
3. Set identity/avatar/theme and five reordered showcased badges. Export, modify the journal, confirm restore, reopen and repeat. Reject corrupted/incompatible files. Export before replacing any real journal.
4. Verify Light/Dark/System, adaptive/themed icons, native splash, large fonts/small screens, TalkBack, disabled animations, all existing screens and offline behavior.
5. Profile cold/warm launches, navigation and scroll frames on hardware.
