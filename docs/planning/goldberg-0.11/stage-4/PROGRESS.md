# Stage 4 — WIP implementation checkpoint

**Current resumable report: [RESUME-CHECKPOINT.md](RESUME-CHECKPOINT.md). Stage4 remains WIP. Read the appended resumed-work sections for current results; earlier checkpoint paragraphs describe the original72a27cb state and are preserved as history. No completion/release is claimed.**

**Incomplete. Not a Stage 4 acceptance checkpoint or a release candidate.** The owner requested a resumable WIP checkpoint as the session approached its usage limit. No main merge, tag, Release, APK upload, Stage5 work, cloud service or paid infrastructure is authorized or performed.

## Baseline and branch

- Repository: https://github.com/stokonn/Goldberg
- Branch: `feature/goldberg-0.11-stage-4`
- Exact Stage3B baseline: `9ae8d33e113feba531cdfa8558f0505cd4205749`.
- Stage3B implementation `c431e9725f9ff601c1d3ed69a5d6b57a2b69eab7` is an ancestor; branch created directly from the expected HEAD.
- Initial worktree: only unrelated untracked `RELEASE_NOTES.md`. Its SHA256 remains `db920f0b1ff88579b6bea9f9da567af16e6db0c7ce5b6439781ca1754fcd4b9b`.
- Issue31/milestone4 remain open. Existing Goldberg Development Project2 was inspected (Backlog). No duplicate tracking resources created; no completion claim posted.
- WIP source commit **`435600ca13ead3cfa3cf8827128f805bbb7a59cd`** was pushed and verified against `git ls-remote` on the Stage4 branch. `results/checkpoint.json` records the source checkpoint and pending CI; this follow-up documentation commit changes no production/test/resource source. Final documentation HEAD is reported in the owner handoff. No Stage4 completion claim.

## Implemented so far

### 4A — partial

Separate Open Opus, MusicBrainz, Wikidata, Wikipedia and Commons adapter files, explicit request/result types, bounded Android JSON parsing, HTTPS host allowlist, contact User-Agent, serialized per-provider scheduling, cancellation, request coalescing, response byte limits, limited transport retries, HTTP/JSON overload classification and Retry-After parsing.

Open Opus and MusicBrainz searches project a subset of permitted core fields; Wikidata search hits remain unresolved until a typed MusicBrainz-ID-bearing item lookup. Wikipedia revision/license gates and Commons per-file metadata checks are connected to an explicit detail action. There is no portrait byte downloader/renderer in this checkpoint. Wikipedia extraction is plain text through Android HTML parsing, never a WebView. These adapters have not had live compatibility probes or Android parser tests executed yet. Error classification, cancellation and retry semantics need the adversarial verification below before being considered complete.

### 4B — partial

Provider ingestion uses the existing schema1 entities, subtypes, aliases, sources, rights, assertions, identity maps and revision tables. Repeated identical response hashes reuse accepted source identities. New field assertions append evidence; first accepted values remain selected and later contradictory assertions remain visible. No title-based cross-provider deduplication. A Wikidata claim can map onto an already-known typed MusicBrainz ID. Existing graph constraints and reversible redirects are retained.

No personal or catalogue schema definition changed. Personal schema10, catalogue schema1, historical exports, portable format5/readers1–4 remain unchanged. New DAO queries do not require a migration. No network migration or destructive fallback added. Broader graph acquisition, candidate/conflict persistence semantics and reverse-order reconciliation are unfinished.

### 4C — partial

Indexed FTS4 local search, category and repository-level artist/period filters, deterministic relevance/UUID ordering, bounded pages, generation/query/filter-bound cursors, local-first Flow emission, independent provider fan-out, debounce in UI and append ordering for arriving results. A one-edit helper is authored/tested but is **not yet integrated into retrieval**. Online provider pagination is unfinished; current incremental paging covers saved local results only.

Freshness timestamps avoid repeat searches for24h; failed refresh keeps accepted catalogue data. Storage guard uses a provisional256MiB aggregate catalogue limit and128MiB free-space reserve; metadata intake stops at the guard. Media age/size eviction scaffolding exists but media storage is not integrated. No automatic graph eviction, accepted-revision retention budget, conditional request refresh, battery handling, complete stale-source reporting, or performance measurement is claimed. Catalogue generations retained by Stage3 rebuild count against the guard; this checkpoint does not delete them.

### 4D — partial

Five navigation roots are Home, Repertoire, Discover, Practice, Profile. Discover has local landing content, search, category controls, explicit online checkbox (reset when the composition is recreated), local/cached/provider statuses, retry, saved-result paging, linked details, source assertions/credits, optional20-entry ID-only local history with removal/clear/disable, and CZ/EN resources. A source detail action can request licensed biography/portrait metadata. No portrait image renderer, verified streaming-link UI, correction export UI or complete filter UI yet.

Repertoire search now opens Discover. Personal piece opening no longer calls legacy online work lookup. Legacy small portraits are cache-only and portrait rendering defaults to no network. Existing explicit legacy catalogue routes remain; audit these and all remaining supplement routes before claiming comprehensive privacy compliance. **No Stage4 screen has been captured or visually inspected; no UI execution/accessibility/state-restoration acceptance is claimed.**

### 4E — partial

Optional catalogue-link evidence uses a `catalogue-link-v1:` JSON record within the existing durable personal identity evidence field, retaining the personal UUID and original anchor. Portable v5 already round-trips that opaque field; the new evidence structure needs an explicit contract addendum and end-to-end backup regression execution. Adding accepted work/arrangement checks provider keys for an existing piece and opens it without changing manual edits. Selected parts can remain one parent practice entry, or the user explicitly chooses separate repertoire entries. No recording automatically becomes a practice entry and this code inserts no practice sessions.

These paths are compiled and have authored Android tests, **not executed Android acceptance tests**. Grouped part selection/re-add behavior, safe absent-catalogue linking, restored UUIDs, graph selection and recovery interactions remain to verify/refine. Do not treat this evidence field as a replacement for the personal UUID or as proof of publication rights.

### 4F — incomplete

See verification evidence below. No emulator, upgrade, screenshot, live-probe, performance, final remote-CI acceptance or Wiki synchronization is claimed.

## Verification actually executed

Commands use the project wrapper and existing cached Gradle home. First sandbox compiler startup failed with `Could not determine a usable wildcard IP`; rerunning through approved escalation succeeded. An intermediate Discover compile failed due to a missing category-layout brace; corrected before the successful compiler run. No compiler failure is currently known in the final built sources.

```sh
GRADLE_USER_HOME=/tmp/goldberg-gradle-home ./gradlew --offline compileDebugKotlin --no-daemon --max-workers=1
GRADLE_USER_HOME=/tmp/goldberg-gradle-home ./gradlew --offline assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1
python3 scripts/check_localization.py
python3 scripts/check_migrations.py
python3 scripts/check_backup_fixtures.py
python3 docs/planning/goldberg-0.11/stage-3b/check_fixtures.py
python3 scripts/check_history_paging.py
python3 scripts/check_agent_policy.py
git diff --exit-code -- app/schemas
git diff --check
ANDROID_HOME=/home/stokonn/Android/Sdk bash scripts/verify-upgrade-signing.sh .gradle/release-artifacts/Goldberg-v0.10.1.apk app/build/outputs/apk/debug/app-debug.apk
```

- Compiler-only invocation: passed (`BUILD SUCCESSFUL`,31s).
- Full invocation failed at lint with2 `LocalContextConfigurationRead` errors; JVM/build/instrumentation compilation had passed. Fixed both UI locale reads with `LocalConfiguration.current`. Targeted `assembleDebug lintDebug` then passed (`BUILD SUCCESSFUL`,1m38s), **zero lint errors /92 warnings**. JVM tests were not needlessly repeated after the UI-only fix. Logs are retained in `results/initial-build.log` and `results/targeted-build.log`; structured evidence in `results/local-checks.json`.
- JVM: **89 tests,21 suites,0 failures,0 errors**, including11 new projection/search contract tests. Android JSON parsing, transport scheduling and Room execution are not covered by the new JVM tests.
- Instrumentation APK compilation/package: completed. Seven new Stage4 Android tests authored. **Zero Stage4 instrumentation executions**.
- Localization: **860 matching English/Czech resources**, placeholders/plurals/locales passed.
- Personal migrations1–9→10: host DDL checks passed. Runtime Android migrations not re-executed in Stage4.
- Historical backup fixtures: formats1–3 fixture checks passed; full/minimal/unknown v5 fixture hashes passed. Full Android readers1–5 and restore/recovery not re-executed.
- History paging and canonical policy/Wiki-link scripts: passed. Runtime agent loading is not tested by the policy script.
- Room schema exports unchanged. Whitespace diff check passed.
- Signing compatibility check against trusted retained0.10.1 APK: passed. This is **not** an in-place upgrade test.

## Verification APK (local only)

- Path: `app/build/outputs/apk/debug/app-debug.apk`.
- Package: `com.opus.piano`; versionName `0.10.1`; versionCode `11`; no version bump.
- SHA256: `69c169712653832c2e9dc299415ca08fb3c8c2428f6914d789abfcb38bcf3065`.
- Trusted certificate SHA256: `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.
- APK ZIP integrity passed; preserved local copy `.gradle/stage4-wip/Goldberg-stage4-wip.apk`. Full resource/source-artifact acceptance is unfinished; `results/source-files.json` records changed source hashes. Do not distribute it as Goldberg0.11.

Final local state: no known compiler/JVM/lint errors;92 lint warnings remain. The original2-error lint failure is preserved rather than hidden. No new expensive suite was started after the owner requested this checkpoint.

## Immediate next action

Read this checkpoint and `CHECKLIST.md`, verify current HEAD/worktree/remote, preserve `RELEASE_NOTES.md`, and inspect `results/local-checks.json`/CI. Start with the seven targeted `Stage4CatalogueTest` tests on an identified disposable emulator, verifying qemu and API before every test session. Current Room ingestion/search/linking paths need actual Android execution before further product work. Do not run the owner's physical phone. Then resolve the gaps listed below systematically through4A–4F.

## Remaining mandatory work / known limitations

1. Execute adversarial Android parser/transport tests: timeouts, cancellation during reads/delays/coalescing, policy changes, stale queries, maxlag/Retry-After/backoff and independent partial success. Current overload/backoff policy, JSON error classification and rights failures need stronger tests and refinement. Supplement rights failures currently may surface as invalid data; cache source state is incomplete.
2. Complete provider field/relationship projection and evidence: MB movements/parts/arrangements/recording/release graph acquisition, actual uncertainty/quarantine diagnostics, missing identifiers, redirects and conflicting cross-ID revisions. Current search ingestion alone does not fulfill full hierarchy ingestion. Do not weaken Stage2 rights allowlist to make missing data appear.
3. Version/test optional-link contract extension; test grouped part selections and duplicate behavior, UUID preservation through portable backup and catalogue loss/rebuild, manual overrides and active recovery/practice completion. Keep private snapshots out of provider indexes/requests.
4. Integrate bounded typo retrieval, multilingual field choice, catalogue normalization/ranking fixture matrix, available filter UI and provider continuation bound to cursor state. `SearchCursor` currently binds local query/filter/generation/sort/rank/ID only; provider continuation is not implemented.
5. Finish Smart Cache: conditional freshness where supported, LRU/age eviction of disposable metadata, revision retention/conflict selection, coalescing ownership tests, size accounting and free-space reserve measured against critical backup needs. Current guard is a provisional intake ceiling, not complete eviction.
6. Finish licensed portraits, external listening links with verified typed identity/destination, biography revision/history/fallback display, local correction export and diagnostics. Current Commons work is metadata-only; no image acceptance evidence. Wikimedia candidates can remain unresolved; no live identity claim exists.
7. Audit all legacy network entry points, state restoration and explicit online-action semantics. Complete source-state/error/offline UX, route restoration/Back behavior, accessibility, CZ/EN small-screen/large-font and both-theme visual inspection. No screenshots exist for this checkpoint.
8. Verify current official source restrictions with bounded live probes (stop on throttle); preserve non-sensitive hashes/status only. Fixture tests are authored, not real identity mappings. Do not transmit personal queries to obtain evidence.
9. Measure actual1k/10k/50k Android catalogue query latency/paging/memory/storage, startup against baseline and practice-timer responsiveness. **No Stage4 performance numbers measured.**
10. Execute full API26/API37 instrumentation, backup/recovery/process-death regressions and trusted signed seeded0.10.1 in-place upgrade; verify original practice time, UUIDs, notes/profile/badges/drafts/preferences and no permission/microphone activation.
11. Finish artifact ZIP/resources/source identity, clean final remote CI and documentation/tracking/English Wiki synchronization. Do not close Issue31 or mark Stage4 complete before all mandatory gates pass.

created by stokonn with love <3


## Resumed work — current verification boundary

The owner resumed Stage4 on the exact72a27cb branch. Remote CI for that HEAD passed: https://github.com/stokonn/Goldberg/actions/runs/39154044750 . Source435600c and documentation72a27cb were inspected. All subsequent modified/untracked source/test/evidence files are preserved, including owner RELEASE_NOTES.md unchanged/untracked. `results/recovered-worktree.json` records hashes; it is an inventory, not a final source/artifact acceptance claim.

Executed on qemu/API-verified disposable emulators: API26 `emulator-5584`, API37 `emulator-5582`. Original7 tests initially failed2 on each due to a real source revision/hash uniqueness bug. Fixed per-record source observations, retained original revision plus payload hash, and cancellation cleanup. All7 then passed on both. Expanded graph/search tests: **12 passed on each API** (`results/graph-api*.log`). Persistent cache tests: **6 passed on each API** (`results/cache-api*.log`). These were targeted runs on successive built candidates; they are not a final combined suite or full regression acceptance.

Added typed unresolved candidates and explicit lookup, provider-proven structural/derivation/performance/edition/track projection, pending/quarantined edges, original-query-free public source URLs/hash-only request keys, FTS intersections across aliases/credits and bounded one-edit candidate matching. Added persistent projected response cache with conditional ETag/304, freshness/no-store/no-cache handling, stale accepted data after failure, per-provider overload backoff, coalescing/cancellation and LRU/age budgets. Contract extensions are documented in CONTRACT-EXTENSIONS.md. Catalogue/personal schemas remain1/10.

Latest source also adds explicit small, rights-checked, original-image hash-verified portrait loading/credits; local correction SAF export; multilingual preferred names; metadata conflict notices; legacy repertoire duplicate checks; grouped part snapshots and selection-conflict reporting; independent arrangement personal UUIDs. New Stage4SafetyTest/Stage4TransportTest are authored. These latest paths await combined execution. A constructor lambda compilation error was corrected; the interrupted integrated/safety runs had no successful terminal result and must not be called passed. The interrupted Gradle/emulator processes were confirmed absent before restarting a single recovered build, logged at `/tmp/goldberg-stage4-recovered-build.log`; emulator37 restarted with the retained AVD/swangle settings. No reset/clean/uninstall/owner-file overwrite occurred.

Remaining priorities: complete the combined26-test catalogue/graph/cache/safety/transport run on both APIs after latest source compiles; fix any failures; then provider continuation/filter UI, complete identity/candidate bounds/diagnostics, licensed media/live probe acceptance, actual UI/locale/font/theme review,1k/10k/50k performance, full API26/API37 regression/upgrade/backup/recovery/source-artifact checks and final clean CI/docs/tracking. No Stage4 completion, merge, tag, release, Stage5 or paid/cloud work is claimed or authorized.

### Latest executed regression boundary

Frozen command `GRADLE_USER_HOME=/tmp/goldberg-gradle-home ./gradlew --offline assembleDebug assembleDebugAndroidTest testDebugUnitTest lintDebug --no-daemon --max-workers=1` passed after the cache-access index and Wikipedia license URL fixes: **90 JVM tests,0 failures/errors; lint0 errors/94 warnings**. `results/lru-license-build.log` records the build. Catalogue schema1 and personal schema10 exports remain unchanged.

Combined Stage4 catalogue/graph/cache/safety/transport26-test execution: first API37 passed26, API26 passed25/failed1 on access-order eviction. API26 filesystem timestamps had insufficient precision; explicit monotonic persisted access indexing fixed it. The next combined execution passed **26/26 on both APIs** (`results/combined-after-lru-api*.log`). Earlier failure logs are retained.

Full Android regression execution then ran **101 tests on each API:100 passed/1 failed**. Both failures were Quality092PersistenceTest's old expectation of an automatic portrait download. Stage4 intentionally defaults that path to cache-only. The test has been updated to assert zero automatic requests, then exercise explicit download, cached offline reads and mid-download consent revocation. This updated test is awaiting rebuild/execution; do not claim the101-test suite passed yet. Existing migrations/backup/recovery/practice tests reported no other failure in those runs.

Host checks rerun: migration1–9→10 DDL; historical backup fixtures; full/minimal/unknown v5 fixture checks; history paging; policy/Wiki-navigation; localization **882 matching CZ/EN resources**; unchanged schema exports; whitespace checks passed. Latest unbuilt source additionally adds bounded local artist/period filter controls, stale-pagination UI guards, authored relevance/history and1k/10k/50k measurement tests. These new paths are not yet verified. The next frozen build log is `/tmp/goldberg-stage4-filter-perf-build.log`.

### Search/filter follow-up

Filter/performance build passed (90 JVM, no failures/errors; lint no errors/94 warnings), then API26 executed the three new search/history/measurement cases and four Quality092 cases: **7 passed** (`results/search-initial-api26.log`). These tests executed against the filter build before the period-credit follow-up. The period-credit build subsequently passed compilation/JVM/lint. Latest frozen build additionally removes raw queries from Compose saved-state persistence and adds a portable-v5 optional-link/grouped-snapshot/UUID/raw-recovery regression; that latest case is not yet executed.

Actual first API26 measurement:1k median16.23/p95 26.56ms,10k median49.27/p95 52.22ms,50k median308.23/p95 326.21ms. DB sizes1,060,864 /7,323,648 /35,536,896 bytes. Journal ready under50k catalogue load47.38ms; saved302ms reconciled exactly once. Raw21-sample vectors and noisy Java heap deltas are in `results/performance-initial-api26.json`. The50k latency is an investigation target; this is not a physical-device or worst-case memory claim. No forced GC/native memory trace/startup comparison yet.

Trusted certificate compatibility rechecked successfully against retained0.10.1. The release-specific `verify_quality_apk.py ... 0.10.1 11` failed its strict `old_code < expected_code` assertion because Stage4 intentionally does not bump published versions; its other post-version resource checks were not reached. Do not call that script passed or bump a version to satisfy a release-only rule. Verification APK package/version/ZIP/resources must be checked separately. Upgrade helper now accepts `--baseline-schema` to prevent a previously upgraded emulator DB from masquerading as a genuine initialized old baseline; fresh isolated AVD configs were created for the upcoming seeded upgrade, not yet booted.


### Resumed checkpoint — executed final boundary

See [current report](RESUME-CHECKPOINT.md), [machine matrix](results/resume-verification.json), [actual rendered states](UI-VERIFICATION.md) and [changed paths](CHANGED-FILES.md). Source checkpoint9daadb3 pushed and CI39164659852 passed. Subsequent presentation fixes are the approved CZ/EN Repertoire label and no verified-source claim for observed snapshots. Current artifact1dd7e3… passes targeted build/lint(0errors/94warnings), signing/ZIP/artwork/package checks,29 targeted API26 tests and105 API37 suite entries. API26 backend67594a… previously passed105; JVM90 unchanged-core tests passed. Both APIs executed four actual crash/restart checks and signed seeded schema8 baseline0.10.1 upgrades preserving37 sessions/113 anchors/compared critical data/preferences. API26 Wi-Fi shell denial was resolved through ordinary Settings UI and strict offline-state assertions, not permission grants. Source/schema/data protections unchanged; owner file hash retained. All4A–4F remain incomplete as product checkpoints; no release/main merge/Stage5. Next source work is provider continuation and retention/identity/cache ownership gaps, not redoing the already-passed original seven tests.

## Paging and unresolved-cache continuation

Explicit provider continuation, opaque bound/replay-safe Discover pages, per-kind provider states, cancellation takeover and projected cache3 implemented. Unresolved candidate/pending-edge/diagnostic state has a200-entry-per-prefix LRU bound and protected accepted evidence. Frozen build:91 JVM passed; lint0errors/94warnings;16 targeted API26 Android tests passed. Exact hashes/logs and pending API37/final acceptance are in RESUME-CHECKPOINT.md. Initial multi-source emission bound and accepted-ledger lifecycle remain under review;4A–4F are not complete.
