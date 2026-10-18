# Stage 4 — WIP implementation checkpoint

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
