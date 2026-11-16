# Gould 0.15 — Catalogue performance investigation

**Status:** source-level repeated-work fix and host measurements complete;
Samsung Galaxy A55 measurements pending. No device-speed improvement is claimed.

## Findings

- `DiscoverRepository.landing`, `library`, and `search` previously called local
  initialization and the `prepareLocal` callback on every query. In the app,
  preparation re-read the bundled manifest, checked the installed revision, and
  ran `LegacyCatalogueImport.synchronize()`. That importer walks the legacy
  response cache and retained composer/work tables, even when their rows have
  already been imported. This repeated whole-table pass is the proven source
  level cause of recurring setup work. Runtime contribution on the owner's
  phone has not been measured.
- `LocalCatalogueStore.initialize()` copies personal identity rows in batches
  of 128 and may report that more work remains. Initialization errors and
  incomplete batches remain retryable. `DiscoverRepository` now serializes
  initialization, runs legacy preparation once after it succeeds, and reuses
  the completed state for later searches. It logs whether initialization and
  preparation were reused. The Android regression test asserts the preparation
  callback runs once across repeated composer, work, filter, and page queries.
- Bundle validation is expensive only when a bundled revision needs installing.
  The database arrives with a populated FTS4 virtual table; runtime startup does
  not build or populate its search index. Search queries use that FTS index plus
  the entity ID/kind index and redirect primary-key indexes. The measured query
  plan did not scan the full `entities` table.
- Local result pages remain bounded at 40 rows with cursor pagination. Existing
  500 ms online debounce, cancellation, response cache, consent gate, and shared
  provider rate limiter were not changed.

## Host measurements

These timings are from Linux host Python 3 / SQLite 3.53.4 against the actual
packaged catalogue payload (283,987,968-byte SQLite DB). They are not Android or
Galaxy A55 measurements. One first-install pass measured:

| Phase | Host time |
|---|---:|
| Gzip decompression (89,123,721 bytes to SQLite file) | 421.7 ms |
| SHA-256 of SQLite file | 77.6 ms |
| SQLite read-only open / first query | 0.045 ms |
| `PRAGMA integrity_check` | 893.2 ms (`ok`) |
| `PRAGMA foreign_key_check` | 174.5 ms (0 errors) |
| FTS schema/index metadata inspection | 0.1 ms |

Representative typed FTS searches against the bundled database, with a warm
host OS/database cache, returned real catalogue rows:

| Query | Kind | First SQL query | Repeated median | Rows in first page |
|---|---|---:|---:|---:|
| Chopin | Composer | 0.584 ms | 0.085 ms | 1 |
| Bach | Composer | 0.794 ms | 0.182 ms | 3 |
| Mozart | Composer | 0.325 ms | 0.144 ms | 2 |
| Beethoven | Composer | 0.203 ms | 0.078 ms | 1 |
| Ballade | Work | 0.401 ms | 0.149 ms | 41 including the look-ahead row |

The plans use `alias_fts` as `VIRTUAL TABLE INDEX 3`, then the `entities`
`index_entities_id_kind` index and redirect primary-key indexes. Full JSON
measurements and plans are preserved at
`.gould-015-diagnostics/catalogue-performance-20261115/{before-host,after-host}/report.json`.
The first SQL timings above are query-only timings, not installation or full UI
latency. The original app-level setup path was not instrumented before the fix,
so a true before/after Android wall-clock comparison is unavailable.

## Added runtime measurements

Logcat tag `GouldCataloguePerf` reports structured phase and duration fields,
without query text, repertoire data, IDs, or provider payloads. Trace sections
are also added for synchronous installation phases. The phase names include:

- `bundle_discovery`, `bundle_revision_check`, `bundle_decompress`,
  `bundle_install_or_reuse`, and `bundle_install`
- `catalogue_initialize`, `catalogue_initialize_reused`,
  `catalogue_prepare`, `catalogue_prepare_reused`, database open, install copy,
  SHA-256, integrity, foreign-key, provenance, policy and FTS checks
- `local_search_first`, `local_search_repeat`, `online_search`,
  `discover_filter_load`, `discover_history_render_data`,
  `discover_result_frame`, and `library_catalogue_result_frame`

The Galaxy A55 run should capture this tag without clearing logcat, then open
the catalogue once and repeat local queries for Chopin, Bach, Mozart, Beethoven,
and a work title. Repeat after process restart and with the online consent
setting enabled separately. This distinguishes first-install cost, process
initialization, cached local search, provider delay, and first visible frame.

## UI refinement and verification limits

Library now uses a full-width Material primary tab row with equal tab weights,
centered labels, and a 64 dp minimum tab height. Labels may wrap to two lines;
the row has no horizontal scrolling or inset edge padding. The instrumentation
test checks equal tab bounds, adjacency, and touch height. Its narrow-screen,
landscape, and large-font runtime behavior still needs an Android UI run.

`scripts/verify-release.sh` passed on exact source `a908c96-dirty` at
`2026-11-15T18:45:31Z` in 110 seconds: 327 JVM tests, 0 failures/errors/skips;
debug lint; AndroidTest compilation; and all seven policy checks. The
instrumented bundle timing, query-plan, tab-layout, cancellation, cache and
large-result tests were compiled but not executed. No emulator or physical
phone was accessed. The direct MusicBrainz network path was not measured in
this session; existing provider-cache/rate-policy tests were included in the
host suite.

## Follow-up source audit and diagnostics — 2026-11-15

No Galaxy A55 access or logs were available. The host measurements above remain
the only timings; this follow-up adds correlated runtime measurements and does
not claim a performance improvement.

### Pipeline findings, ranked by source evidence

1. **Repeated setup was already removed by the preceding change.** Successful
   local initialization and the legacy synchronization callback are reused for
   the repository lifetime. The previous source path repeated the manifest and
   legacy table pass per search. Its A55 time contribution still has no device
   measurement.
2. **Each local search does bounded work beyond the FTS query.** A normal
   `search()` obtains the local page, checks unresolved candidate state, obtains
   a final page, and checks continuation availability. In the offline case the
   continuation check can issue another local existence query and candidate
   lookup. With online consent, `automaticSearch()` first emits this local-only
   pass, waits 500 ms, then runs the search flow again for provider results;
   provider responses refresh local rows and candidate state. The counts are
   directly visible in the call path, but their A55 latency is unmeasured.
3. **Screen support data is part of the keyed search effect.** Cache byte
   counting, filter loading, and recent-detail loading run on query/category
   changes, even though they do not depend on the typed query. Existing
   per-phase timers can now show their cost. I did not remove or relocate this
   work without device timing, since that could change visible loading and
   refresh behavior.
4. **The SQL path is not the leading host suspect.** The actual packaged DB
   query plans use the FTS4 virtual table and entity/redirect indexes. Repeated
   SQL-only host medians are 0.078–0.182 ms for the representative queries.
   This excludes Room dispatch, mapping, repeated reads, device storage, and
   Compose frames.
5. **List rendering is bounded and keyed.** Discover uses a `LazyColumn` with
   entity IDs as keys, a 40-row local page, and a 500-result cap. Category
   controls use a `LazyRow`; work subtype, composer, and provider display data
   is loaded page-wide rather than per row. No unstable row key or unbounded
   result list was found. Frame/jank behavior still needs a device trace.

The category value is part of the local `SearchFilter` and the effect key, so
All / Works / Artists / Albums / Lists changes the actual set and cancels the
previous search. The search ViewModel retains query text through detail
navigation; category/filter state is saveable. The MusicBrainz client keeps its
existing centralized per-provider gate and request cancellation. Live provider
latency and result-merge time have not been measured.

### Runtime instrumentation added

`GouldCataloguePerf` events now carry `session_id`, `source`, and `category`
fields. Session IDs are opaque UUIDs and raw query text, personal repertoire
content, provider payloads, and credentials are not logged. Timed operations
report `outcome` (including cancellation/error), duration, and a result count
where applicable. New or refined events cover:

- `catalogue_local_result`, `catalogue_first_local_result`,
  `catalogue_candidate_results`, and `catalogue_paging_availability`;
- `catalogue_online_debounce`, `online_search`,
  `catalogue_first_online_result`, and `catalogue_provider_result_refresh`;
- database open/index checks correlated to an active search, plus
  `discover_filter_load`, `discover_history_render_data`,
  `discover_provider_cache_measure`, `discover_result_frame`,
  `library_catalogue_result_frame`, and
  `catalogue_category_switch_first_frame`.

`automaticSearch()` now uses the same opaque session ID for its immediate local
emission and delayed online pass. A JVM test checks the event context shape, and
the Android regression test asserts the local and online emissions share one
search-session ID. Instrumentation event values have not yet been observed on
Android.

### Rust feasibility

No operation is presently a good Rust candidate. SQLite FTS and query planning
already execute in native SQLite. The Kotlin result sets and deduplication are
bounded to tens of rows per page and at most 500 per search; provider JSON and
network work have no CPU profile showing a Kotlin hot loop. JNI setup, string/
DTO conversion, and extra allocations would add cost without an identified
end-to-end target. No Rust or JNI benchmark was added; keep the implementation
in Kotlin unless a future A55 CPU trace isolates a repeatable CPU-bound pure
function. Any later feasibility benchmark must compare full call cost and
equivalent output, including cold/warm runs and realistic row counts.

### Catalogue UX recommendation (no visual redesign made)

Keep the existing full-width Repertoire / Catalogue tabs. Inside Catalogue,
retain a single search and category control, but use explicit category names
such as **All**, **Works**, **Composers and artists**, **Albums**, and
**Collections** so “Artists” does not imply composers are the only people.
Make the row type a prominent, consistently placed label; show a work’s
composer directly below its title; keep provider provenance secondary. Preserve
the detail route and keep all public results separate from private repertoire.
The current row already has a kind icon, a type/provider line, and composer
metadata for works; the improvement is hierarchy and category wording, not new
catalogue records or metadata. Defer visual changes until the owner reviews a
concrete mock or device build.

### Verification and remaining evidence

- FAST PASS 2026-11-15 21:58:42 UTC, 6 s: `compileDebugKotlin`, both focused
  `CataloguePerformanceTest` cases, and six fast static-check groups passed.
- INTEGRATION PASS 21:59:25 UTC, 36 s: final-source JVM suite **329 passed, 0
  failed, 0 errors, 0 skipped**; `lintDebug`; and AndroidTest compilation
  passed. AndroidTest was compiled, not executed.
- Final-source Release `compileReleaseKotlin` and `lintRelease` passed in 40 s.
  The signed diagnostic APK was then built without cleaning; exact identity
  and verification are below. The first packaging attempt had a temporary
  directory quota error; the retry with an isolated temporary directory
  completed successfully.
- The final-source FAST/INTEGRATION runs are recorded in
  `verification-audit.jsonl`. `git diff --check` is checked again at handoff.
  The mandatory `scripts/verify-release.sh` tier was not run because it invokes
  `gradlew clean`, prohibited by the owner's preservation instruction.
- Diagnostic APK: `app/build/outputs/apk/release/app-release.apk`, 97,927,041
  bytes, SHA-256
  `da2760795b835ad7c2f8ae2bd68e0428b336b92bb2d01184f6ce249e705fc54f`.
  Verified package `com.opus.piano`, version `0.15.0-dev`, versionCode 17,
  `debuggable=false`, valid v2 signature, ZIP integrity, and expected signer
  SHA-256 `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.
  Earlier APKs were preserved at unique paths under
  `.gould-015-diagnostics/catalogue-performance-20261115/` before replacing
  the build output. This APK is for owner diagnostics, not acceptance evidence.
- No Galaxy A55 timings/logs/jank samples, installed-APK identity, or live
  MusicBrainz timing were collected. No before/after performance comparison is
  available. The prior initialization reuse is the only behavior optimization;
  this follow-up changed diagnostics only. No performance improvement is
  claimed.

### Owner-run Galaxy A55 procedure

First identify the installed artifact; do not uninstall or clear storage:

```bash
set -eu
PKG=com.opus.piano
OUT="$(mktemp -d /tmp/gould-015-a55-XXXXXX)"
adb devices -l
adb shell dumpsys package "$PKG" | grep -E 'versionCode=|versionName=|codePath=|lastUpdateTime'
REMOTE_APK="$(adb shell pm path "$PKG" | tr -d '\r' | sed -n 's/^package://p' | grep '/base.apk$' | head -n 1)"
test -n "$REMOTE_APK"
adb pull "$REMOTE_APK" "$OUT/installed-base.apk"
sha256sum "$OUT/installed-base.apk"
SDK_ROOT="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}"
test -n "$SDK_ROOT"
APKSIGNER="$(find "$SDK_ROOT/build-tools" -type f -name apksigner | sort -V | tail -n 1)"
"$APKSIGNER" verify --print-certs "$OUT/installed-base.apk"
```

For this diagnostic build, compare the installed package/version and signer
with the expected values and inspect the pulled APK hash before updating. If
the package/signing identity is compatible, update in place with
`adb install -r /absolute/path/to/app-release.apk`; stop on any mismatch. Never
uninstall or clear app storage. Capture without clearing existing logcat:

```bash
adb logcat -v threadtime -s GouldCataloguePerf:I '*:S' > "$OUT/catalogue-perf.log"
```

While capture runs, open Library → Catalogue and wait for its landing list;
search Chopin, Bach, Mozart, Beethoven, then a work title; switch All / Works /
Artists; scroll through several pages; open a result and return. Repeat once
after process restart. Run online-enabled searches separately and include the
consent setting; run offline searches with connectivity disabled using device
settings. Stop capture with Ctrl+C. For frame data, capture
`adb shell dumpsys gfxinfo com.opus.piano framestats` before and after the same
scroll sequence. Keep the APK identity output and logs together, redact device
identifiers, and share only the GouldCataloguePerf lines plus frame summaries.
This owner-run check is still pending.

### 2026-11-16 — Library and active search remediation

- The online stage now receives the local page and candidate set collected
  before the 500 ms debounce. It skips the second initial local query and
  duplicate initial emission; provider ingestion refreshes local rows only
  when a usable response contains records. Existing cursors, page size,
  deduplication, cancellation, MusicBrainz cache, rate limiter, and User-Agent
  remain in place.
- Filter/history loading now has its own screen-lifecycle effect and error/retry
  message, so it no longer holds catalogue search's loading state or repeats on
  every query/category. Final Discover completion and local-seed reuse have
  explicit outcome/count events; no query, private repertoire, provider payload,
  credentials, or entity IDs are logged.
- Active Discover adapter construction filters to `musicbrainz`; cached
  non-MusicBrainz candidates are hidden and candidate resolution returns before
  networking for those providers. Open Opus/Wikidata adapters and record,
  identity, assertion, and provenance handling remain for legacy import and
  retained data. The separate route through `CatalogScreen`/`CatalogViewModel`
  was audited and left intact; it is not the Library's active Discover search.
- Read-only host category comparison used the preserved 283,987,968-byte
  catalogue DB at `.gould-015-diagnostics/catalogue-performance-20261115/
  before-host/catalogue.db`, opened with `mode=ro&immutable=1`, and the current
  `CatalogueSearch.searchPage` ranking/filter/FTS query shape for `chopin`, 40
  results plus look-ahead, first query plus 10 repeats:

  | Category | First SQL query | Repeat median | Rows |
  | --- | ---: | ---: | ---: |
  | All | 2.068 ms | 0.575 ms | 41 |
  | Works | 0.673 ms | 0.571 ms | 41 |
  | Artists | 0.344 ms | 0.361 ms | 1 |

  These Python/SQLite host timings exclude Room/Android dispatch, work-type and
  row-info mapping, provider calls/ingestion, Compose, and device storage. The
  plan uses FTS4 and entity/redirect/alias/relationship/assertion indexes; SQL
  was not changed because the measured queries were sub-millisecond after warmup.
  No Android or Galaxy A55 timings were collected.
- Final-source commands (offline, `--no-daemon --max-workers=1`, no clean;
  app outputs, Room schema exports, and Java temp directory redirected under
  `.gould-015-remediation-build/`):

  ```sh
  ./gradlew :app:testDebugUnitTest :app:assembleDebugAndroidTest \
    :app:lintDebug :app:lintRelease :app:assembleRelease
  ```

  PASS: 331 JVM tests, 0 failures/errors/skips; AndroidTest APK compiled only;
  `lintDebug`, `lintRelease`, `assembleRelease`, `git diff --check`, and
  `scripts/check_musicbrainz_catalogue.py` passed. No instrumentation was run.
  The Release output is **unsigned**, not a signed release artifact:
  `.gould-015-remediation-build/app/outputs/apk/release/
  app-release-unsigned.apk`, 97,918,833 bytes, SHA-256
  `5a1bc8830c589267df1b9039c75cc02b704b49d266ce01cad6f2f8fb85ccea78`, package
  `com.opus.piano`, version `0.15.0-dev`, code 17. The existing signed
  diagnostic APK at `app/build/outputs/apk/release/app-release.apk` was not
  overwritten; its SHA-256 remains
  `da2760795b835ad7c2f8ae2bd68e0428b336b92bb2d01184f6ce249e705fc54f`.
- The owner's latest earlier A55 exploratory report (2026-11-15) recorded
  empty Discover results and Library skeletons before this remediation. It is
  preserved as prior evidence; no duplicate test was requested or performed.
  This run establishes host source/build results only, not corrected A55
  behavior.
