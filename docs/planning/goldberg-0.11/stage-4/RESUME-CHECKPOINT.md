# Goldberg 0.11 Stage4 — resumed WIP verification checkpoint

**Not complete and not a release.** Branch `feature/goldberg-0.11-stage-4`; resumed source checkpoint `9daadb3e47d62eef87f650d9ff068787ccbcf77b`, based on previous WIP `72a27cbfad530f0ec2db1b73c425c35c33257e4a` / implementation `435600ca13ead3cfa3cf8827128f805bbb7a59cd`. All descend from verified Stage3B `9ae8d33e113feba531cdfa8558f0505cd4205749`. No main merge, tag, Release, APK publication, Stage5, cloud, paid infrastructure or physical device operation.

## Implementation and checkpoint status

| Checkpoint | Actual resumed implementation | Status / remaining |
|---|---|---|
|4A|Separate adapters; reviewed narrow core relationship projection; typed lookups; bounded cancellable transport; JSON errors, Retry-After/backoff; conditional public metadata cache; realistic synthetic fixtures and stopped-on-throttle probes.|Partial. Live MusicBrainz/Wikidata acceptance blocked/unverified; reviewed redirect flow and further adversarial coverage remain.|
|4B|Per-record hash-bound source observations; immutable original assertions; unresolved candidates; pending/quarantined typed edges; no names/score reconciliation; explicit verified cross-IDs; catalogue-only validation.|Partial. Conflicting/reverse-order cross-ID revisions and bounded candidate/diagnostic retention need further work.|
|4C|Indexed alias/credit intersections; catalogue-number normalization; bounded one-edit retrieval; generation/query/category/filter/sort/UUID-bound v2 cursor; local-first independent fan-out; persisted projected response cache with monotonic LRU access index, freshness/304/no-store/coalescing/stale failures; local artist/period filters.|Partial. Provider continuation paging, full revision/normalized-catalogue eviction, low-battery policy and broader relevance/performance work remain.50k search latency needs investigation.|
|4D|Five tabs; Discover local landing/history/search; uncertainty/source states; linked details; explicit small rights-checked portrait path/credits; verified recording Spotify destinations; licensed plain-text biographies; preferred CZ/EN naming; correction proposal SAF export; transient raw search input and stale-page guards.|Partial. Real licensed portrait rendering, fallback language/revision UX, richer errors/diagnostics, complete accessibility/restoration and visual acceptance remain.|
|4E|Optional verified links preserve personal UUIDs and manual edits; existing/legacy links prevent duplicates; grouped selected parts retain durable snapshots or explicitly create separate entries; recordings never create practice entries.|Partial. Targeted safety and portable v5 regressions pass on both APIs; complete final integration acceptance remains.|
|4F|Frozen build/JVM/lint, host checks, actual Android regression execution and synthetic performance/artifact checks.|Incomplete. See results below; no final Stage4 acceptance claim.|

## Architecture and compatibility

`DiscoverRepository` owns explicit catalogue actions over provider-independent `ProviderClient`/transport and adapter projection. `LocalCatalogueStore` serializes independent catalogue access; no cross-database atomicity or personal metadata rewrite is claimed. Response cache stores only projected public content; hashed request keys exclude raw search URLs/text from persisted envelopes. Failed refresh preserves older accepted observations with a separate failure state. Sources, original values, language, license, attribution, revision/hash and timestamps remain available. Search equivalence is never identity evidence.

**Personal schema10, catalogue schema1, backup v5/readers1–4 unchanged. No new migration.** Historical exports remain unchanged. Catalogue state/DAO additions and optional-link evidence are versioned in [contract extensions](CONTRACT-EXTENSIONS.md), without silently changing Stage2 historical contracts. The personal registry/anchors, snapshots, sessions, raw drafts/archives and completion/preference replay remain authoritative. New source acquisition/cache clearing never deletes personal files. The owner’s untracked `RELEASE_NOTES.md` remains excluded; SHA256 `db920f0b1ff88579b6bea9f9da567af16e6db0c7ce5b6439781ca1754fcd4b9b`.

## Verification actually executed

Frozen candidate command:

```sh
GRADLE_USER_HOME=/tmp/goldberg-gradle-home ./gradlew --offline assembleDebug assembleDebugAndroidTest testDebugUnitTest lintDebug --no-daemon --max-workers=1
```

Passed:90 JVM tests,0 failures/errors; lint0 errors/94 warnings; APK and instrumentation APK compiled. Latest log [checkpoint-fixed-build.log](results/checkpoint-fixed-build.log). A preceding new-test compile failed on two incorrect API names; corrected without weakening checks, original log retained. Earlier internal FIR lint failure during concurrent edits was resolved by freezing source for builds, not suppressing lint.

Host checks passed: `python3 scripts/check_migrations.py`, `check_backup_fixtures.py`, `docs/planning/goldberg-0.11/stage-3b/check_fixtures.py`, `scripts/check_localization.py` (882 matching resources), `check_history_paging.py`, `check_agent_policy.py`; `git diff --exit-code -- app/schemas`; whitespace checks. These include host DDL checks; actual Android migration cases are in the executed suite.

Every ADB operation targets an explicitly identified qemu/API-verified emulator. Full execution command (repeat with the verified API37 serial):

```sh
/home/stokonn/Android/Sdk/platform-tools/adb -s emulator-5584 shell am instrument -w -r com.opus.piano.test/androidx.test.runner.AndroidJUnitRunner
```

Latest API26 full suite: **105/105 passed**. Two entries are argument-driven process-death probe methods that return without phase arguments; actual process termination/restart is separately executed. API26 four actual phases (`before_commit`, `after_commit`, `after_preferences`, `before_ack`) each terminated as intended and passed a separate-process restart verification. API37 current-UI-candidate full execution: **105/105 passed**. All four actual process terminations and separate restart verifications also passed on API37. Both genuine seeded upgrades have now passed, as detailed below.

Earlier combined26 Stage4 tests passed on each API. The first combined run exposed an API26 LRU ordering failure; an explicit persisted monotonic access index fixed filesystem timestamp precision. Earlier full101-entry runs each passed100/failed1 due to the pre-Stage4 automatic portrait expectation. The updated test asserts zero default requests, explicit download, cached offline access and consent revocation. Both earlier failures and subsequent passing evidence remain in [results](results).

## Performance and artifact

API26 latest105-entry candidate measurement,21 samples at each scale:

| Rows | Median ms | p95 ms | Catalogue bytes |
|---:|---:|---:|---:|
|1,000|10.67|11.47|1,060,864|
|10,000|48.79|59.64|7,323,648|
|50,000|254.93|270.44|35,536,896|

Journal readiness under50k catalogue activity19.57ms;301ms session credit reconciled exactly once. Small synthetic homogeneous metadata; no physical-device inference. Java heap delta is noisy and excludes native/SQLite allocations. Initial separate run was slower; preserve both vectors, investigate50k relevance query cost rather than claiming universal thresholds. API37 current-UI catalogue measurements:1k median7.67/p95 8.54ms;10k38.65/40.12ms;50k188.36/197.89ms. DB sizes match the API26 fixture. Journal ready under50k load9.52ms;301ms saved credit reconciled exactly once. Raw vectors: `results/performance-ui-api37.json`. Five matched initialized cold-process startup samples:API26 baseline median687ms/current-backend704ms; API37 baseline median2314ms/current-UI2002ms. API37 samples trend down during warm-up, so no startup improvement is inferred. Five samples do not establish stablep95 or satisfy a marginal-regression investigation requiring30 comparable samples; idlePSS/native/frame traces remain unexecuted.

Local verification artifact `app/build/outputs/apk/debug/app-debug.apk`: package `com.opus.piano`, version0.10.1/code11, SHA256 `67594aadc1deac61c70bfed954203a4d3eaf8388526a7da83c7bde044e70f861`, trusted signing certificate `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`. [Frozen source/artifact manifest](results/checkpoint-artifact-source.json) covers compiled source hashes and both APKs. No source change followed the successful frozen build.

`verify-upgrade-signing.sh` passed against retained0.10.1. Release-specific `verify_quality_apk.py` rejects a same-version0.10.1 comparison at its strict version-increase assertion; that failed invocation is not passed. The unchanged checker then passed package/version/ZIP/artwork/trusted signing using the retained0.10/code10 baseline, matching the established Stage3B verification procedure. This does not substitute for the seeded0.10.1 upgrade.

## API compliance and external limitations

See [current official sources and bounded probes](API-EVIDENCE.md). Open Opus and EN/CS Wikipedia returned valid response shapes; Wikidata returned JSONmaxlag/Retry-After and was stopped; MusicBrainz was skipped after documentation429. No fixture constitutes verified real-world identity mapping. Commons’ probed public-domain label lacked accepted rights and was blocked without image download. App spacing is conservative client policy, not a guarantee about upstream shared-IP quotas. Unsupported metadata rights remain denied; no MusicBrainz derived annotations/tags/ratings/cover art are enabled. Real licensed portrait acceptance remains open.

## Tracking and next action

Issue31 and milestone4 remain open; existing Project2 item is **In Progress**. No duplicate tracking resources. English Wiki remains at the previously verified Stage3B state; Stage4 is not advertised as complete. Pushed9daadb3 source CI: https://github.com/stokonn/Goldberg/actions/runs/39164659852 **passed**. Later UI/documentation checkpoint CI must also be verified after push.

Continue the current safe emulator/upgrade checks, retain all results, then provider cursor continuation and bounded identity/candidate/revision retention. Complete licensed media/live source acceptance, full UI/navigation/restoration/CZ/EN/theme/font inspection and final acceptance before marking4A–4F complete. Do not restart completed work or repeat unchanged expensive suites without a new reason. Verify this branch/HEAD/worktree and owner-file hash before resuming.

created by stokonn with love <3

## Verified follow-up after actual renders

Actual API26 screenshots found the legacy Library/Knihovna labels and an unconditional verified-source notice for observed local snapshots. Both were corrected. Only those three presentation/resource files differ from the105-entry API26 backend candidate. The targeted `assembleDebug assembleDebugAndroidTest lintDebug` build passed (0 lint errors/94 warnings), and **29 API26 catalogue/graph/cache/link-backup/transport/navigation tests passed** after installation. JVM90 results from the unchanged core remain relevant; remote CI will rerun them for the final pushed UI source. Do not claim105 entries executed on this exact APK on API26.

Current verification APK SHA256 `1dd7e3339f40998b3880c5fc3d2c07f9cb2c2ecad0b396d9365fe7d12737f2ee`; package/version/signer unchanged. Installed API26 APK hash matched. [Current UI source/artifact manifest](results/checkpoint-ui-artifact-source.json) is separate from the preserved9daadb3 artifact manifest. Current signing/ZIP/package/artwork checks pass; see `results/ui-signing.log` and `ui-artifact-check.log`.

[Actual screenshots and inspection limits](UI-VERIFICATION.md) include API26 EN Nocturne/Ivory and current CZ Ivory at1×/1.5×, corrected detail uncertainty, wrapped categories and visible five tabs. No complete screen-reader/restoration/portrait/all-detail acceptance is claimed. The unreadable seeded draft remained retained and the UI explicitly says it was not deleted. RECORD_AUDIO app-ops returned `No operations.`

Both fresh disposable upgrade AVDs verified original schema8 before seeding, then performed trusted signed `install -r` upgrades to schema10; **37 original sessions and compared critical rows/preferences unchanged**,113 anchors and FKs valid. API26 direct upgrade artifact was67594a…, followed by the UI-only1dd7e3… update; API37 direct upgrade used current1dd7e3… artifact. No uninstall/recreation/signature bypass. Structured results `results/upgrade-api26.json` / `upgrade-api37.json` include exact artifact hashes and raw startup samples.

Commands:

```sh
python3 scripts/verify_emulator_upgrade.py --serial emulator-5584 --baseline .gradle/release-artifacts/Goldberg-v0.10.1.apk --baseline-version 0.10.1 --baseline-schema 8 --expected-schema 10 --candidate app/build/outputs/apk/debug/app-debug.apk --output /tmp/goldberg-stage4-upgrade-api26 --seed-disposable --offline-preconfigured --startup-runs 5
python3 scripts/verify_emulator_upgrade.py --serial emulator-5582 --baseline .gradle/release-artifacts/Goldberg-v0.10.1.apk --baseline-version 0.10.1 --baseline-schema 8 --expected-schema 10 --candidate app/build/outputs/apk/debug/app-debug.apk --output /tmp/goldberg-stage4-upgrade-api37 --seed-disposable --offline --startup-runs 5
```

API26 shell Wi-Fi control was denied `CHANGE_WIFI_STATE` and returned137; failed attempts were preserved, not treated as upgrades. An initial multi-command diagnosis mistakenly exposed only the final command's success; isolated Wi-Fi execution and Android SecurityException proved the restriction. Wi-Fi was disabled through the ordinary emulator Settings UI; `Active default network: none` was asserted before/after the upgrade. The helper's explicit `--offline-preconfigured` mode verifies this state without granting privileges or weakening assertions. API37's ordinary offline commands succeeded.2GiB fresh API37 boot was stopped under host contention;4GiB sequential boot succeeded. Retained AVDs and all owner files remain intact.

## Latest verification and resumable boundary

API37 full current-UI suite passed105/105 (`results/full-ui-api37.log`); both APIs passed all four actual crash/restart phases (`results/process-api*-result.json`). [Structured verification matrix](results/resume-verification.json) distinguishes artifacts, commands/results and remaining requirements. API37 installed APK hash matches current1dd7e3…; app-op mode is `RECORD_AUDIO: ignore`, while API26 reported `No operations.` These observations do not claim physical-device or acoustic verification.

Current compiler/JVM/lint/targeted/full Android checks have no unresolved failure in their stated scopes. Previous failures and exact fixes remain in this ledger; mandatory product/provider/media/performance/UI acceptance is still incomplete. Next implementation: provider continuation with query/category/language/generation/sort/tie-breaker binding, deterministic local-plus-online paging and stale/cancelled ownership tests. Then complete bounded candidate/diagnostic/accepted-revision retention and the remaining4A–4F rows. Do not repeat the full expensive unchanged suite without a relevant new change.

[Changed-file inventory](CHANGED-FILES.md) lists all Stage4 paths including initial WIP work. Final pushed checkpoint SHA and CI result are recorded in the owner report and Issue31; identify the local checkpoint by `git log -1` after this file is committed. Both completed test emulators were stopped without deleting their data/configs. Owner RELEASE_NOTES.md remains untracked/unchanged. This is a safe resumable WIP boundary, not Stage4 completion or release readiness.

## Active continuation after d3d68ba (uncommitted recovery state)

Recovered local/remote `d3d68ba0759b68626a7426d3599421b33c810498`; CI39166071398 passed. Preserved every working-tree change and owner RELEASE_NOTES.md. Implementation continues; this paragraph is not a stopping/completion declaration.

Implemented since that checkpoint: explicit Open Opus/MusicBrainz/Wikidata continuation; opaque, query/category/filter/language/online/generation/sort-bound process-local cursors; deterministic replay and stable append paging; source status per provider request kind; projected response envelope3 with safe legacy2 unconditional refresh; cancelled-owner coalescing takeover; unresolved Wikidata observation schema; bounded unresolved candidate/pending-edge/diagnostic cache using existing catalogue_state (no schema migration). Accepted identities, sources, assertions, graph and personal data are outside observation eviction. Eviction count overflow and repeated identical relationship observations were corrected during review.

Executed before the latest source-status/observation edits: `/tmp/goldberg-stage4-paging-build.log` passed assembleDebug/assembleDebugAndroidTest/testDebugUnitTest/lintDebug,91 JVM tests/0 failures/errors; `/tmp/goldberg-stage4-paging-api26.log` passed12 actual instrumentation tests (Stage4PagingTest5 + Stage4ProviderCacheTest7) on qemu1/API26 emulator-5584. These results do NOT cover later edits.

Current frozen build command: `GRADLE_USER_HOME=/tmp/goldberg-gradle-home ./gradlew --offline assembleDebug assembleDebugAndroidTest testDebugUnitTest lintDebug --no-daemon --max-workers=1 > /tmp/goldberg-stage4-observation-build.log 2>&1`. Includes new Stage4ObservationCacheTest and legacy-cache/unsolicited304 tests. Result pending at this entry. Next: inspect build, install -r and execute Stage4PagingTest,Stage4ProviderCacheTest,Stage4ObservationCacheTest on verified emulator-5584. Then cap initial multi-source result append at40, test per-kind source partial success/retry, finish critical-storage reserve and language fallback. API37 execution of these new changes, final artifact/upgrades/full suite and remaining checklist are still pending. All4A–4F remain incomplete.


### Observation/paging verification result

Frozen build above **passed** (2m35s):91 JVM/0 failures/errors/skips, lint0 errors/94 warnings, both APKs compiled. Actual targeted instrumentation passed **16/16**,8.222s on qemu-verified API26/emulator-5584. Install used `adb -s emulator-5584 install -r` for both APKs; execution used `shell am instrument -w -r -e class com.opus.piano.Stage4PagingTest,com.opus.piano.Stage4ProviderCacheTest,com.opus.piano.Stage4ObservationCacheTest com.opus.piano.test/androidx.test.runner.AndroidJUnitRunner`. Logs and exact source/artifact hashes: `results/paging-observation-*`. APK SHA256 `3d73475c6784de8b0dee2dc75e0213e7d1785937897a2cd0fc77f736cfd97f9f`. No current compiler/test failure in this targeted scope. API37 has NOT executed these new changes. Prior105-entry suites/seeded upgrades are historical per-artifact evidence, not final verification of this new candidate.

Host migration1–9→10 DDL, historical backup fixtures, Stage3Bv5 fixture hashes,884 CZ/EN localization, journal paging and policy checks passed again; schema exports and owner RELEASE_NOTES.md unchanged. Next implementation remains initial multi-source append bound, per-kind partial failure fixture, dynamic critical-storage reserve, then language fallback/typed details and remaining checklist. Accepted-ledger lifecycle, final performance/UI/provider acceptance remain open. Continue after checkpoint while execution capacity remains.

### Continuing after pushed paging checkpoint4891cbd

`4891cbd2712663e2cf2540f9e6198fd8dbd51989` is pushed; CI39184750641 was in progress at inspection. Continuing uncommitted edits cap combined initial provider rows at40 (unseen rows remain pageable), add same-provider/per-kind partial-failure and multi-source bound tests, and dynamically reserve personal DB/WAL/SHM + existing critical backup files + two32MiB exports +32MiB journal growth (minimum128MiB) for all Stage4 cache/image intake. Reserve math is overflow-safe and never gates personal export/recovery. Portrait access now touches the image as well as its manifest, avoiding eviction of actively viewed images. Build/test command log `/tmp/goldberg-stage4-bounded-search-build.log`; expanded targeted API26 log `/tmp/goldberg-stage4-bounded-search-api26.log`. Both pending at this entry. Source is frozen during the build.

One additional bounded Commons metadata probe found an original within byte/pixel limits but CC BY2.0, outside the current exact allowlist; no image transfer or accepted portrait is claimed. Evidence/rights review recorded separately. Next after passing frozen checks: API37 targeted execution, then language fallback, typed detail labels, licensed-attribution extension if fully evidenced, accepted-ledger retention and remaining acceptance rows. Preserve all source, logs and owner RELEASE_NOTES.md.

Expanded API26 matrix initially ran18 tests:17 passed/1 failed. `invalidEndpointDoesNotHideSuccessfulEndpointsOfSameProvider` expected INVALID but got UNAVAILABLE for malformed JSON. Root cause: Android JsonReader's parse IOException/IllegalStateException entered the transport exception/retry branch. The assertion is retained; fix the synchronous byte-parser boundary to reject malformed JSON (including malformed UTF8) with IllegalArgumentException. Real transport IOExceptions must remain UNAVAILABLE with limited retry. Initial failure log preserved as `results/bounded-search-api26-initial-failure.log`. The result-bound regression passed in that run. Do not claim the expanded matrix is green until corrected execution.

The classification fix is now implemented: ProviderJson normalizes only synchronous in-memory parse IO/structure exceptions to invalid payload, uses strict UTF8 decoding, and leaves real transport IO outside that boundary. New Android test requires one request each for malformed/truncated/non-object/invalidUTF8 responses and two bounded attempts for actual transport IOException. Current frozen verification log `/tmp/goldberg-stage4-bounded-search-fixed-build.log`; result pending. After compilation run expanded19-test API26 matrix, then execute current changes on API37. Previous18-test result remains failed, not relabeled. Do not edit compiled source concurrently with lint.


### Corrected bounded-search/storage verification

Frozen `/tmp/goldberg-stage4-bounded-search-fixed-build.log` passed (2m10s):93 JVM tests,0 failures/errors; lint0 errors/94 warnings; APK and Android test APK compiled. Expanded targeted execution passed **19/19 on API26** (9.687s) and **19/19 on API37** (13.422s). Both qemu identities/API levels/boot were verified explicitly; install -r preserved existing emulator data. API26 was stopped before API37 boot to limit host contention; neither AVD was deleted. Exact class command is the same three-class matrix above. Corrected logs/source hashes are in `results/bounded-search-fixed-*`; the initial failure remains preserved. No unresolved compiler/test failure in these targeted scopes.

APK: `app/build/outputs/apk/debug/app-debug.apk`, SHA256 `d2d3dbaa3151406fb30e283553c25fb85283e68dcfc89b318a67bf34bc3cfb8c`, package `com.opus.piano`, version0.10.1/code11, trusted certificate SHA256 `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e` verified with build-tools36 apksigner/aapt. No publication/version bump. Personal10/catalogue1/backup5 unchanged. This candidate has NOT repeated full105-entry suites, seeded upgrades, rendered UI or performance measurements; their earlier artifact evidence remains historical.

Next work: language fallback and typed movement/detail labels; licensed portrait acceptance with complete attribution (current CC BY2.0 probe remains blocked); identity conflict/reverse-order and immutable accepted-ledger retention; aggregate intake safety and performance; comprehensive UI/privacy/accessibility/restoration and final4A–4F acceptance. Continue from current HEAD, not a historical hardcoded SHA. Preserve owner RELEASE_NOTES.md. Intermediate successful builds/checkpoints do not complete Stage4.

### Active document/detail follow-up after d67982e

`d67982e` is the pushed bounded-search/storage checkpoint (full SHA via git log; CI pending inspection). Continuing uncommitted implementation: document lookup tries sourced requested-language title then sourced English only; missing/invalid/blocked-rights/empty excerpt may fall back, provider outage/overload stops extra edition requests; explicit network policy checked at each call. Each edition status is retained with distinct `document:cs/en` key; accepted excerpts retain actual language, own returned revision/source, requested language and fallback reason. Storage refusal is a source-specific unavailable state. Work detail reads actual workType from existing typed table and localizes composition/movement/part/medley/mashup, with no schema change or guessed movement. New Stage4DocumentTest has4 deterministic cases (fallback/revision/script removal, overload/offline, absent title, movement subtype). Frozen build `/tmp/goldberg-stage4-document-build.log` is pending. Next execute Stage4DocumentTest plus prior paging/cache matrix on current API37 emulator-5582, then API26 as needed; retain exact outcomes before further source edits. RELEASE_NOTES.md remains untouched.

Document/detail frozen build passed93 JVM/0 failures, lint0errors/94warnings; **23/23 actual tests passed on API26** (11.203s) and **23/23 on API37** (9.07s). Logs and exact source/APK hashes: `results/document-*`. Later uncommitted rights/graph guards are NOT covered by those results: online title evidence must be approved/from Wikidata; a rejected relationship restores its tentative composition→part promotion before quarantine. Added one document guard and one conflicting-parent graph regression. Current frozen build `/tmp/goldberg-stage4-evidence-guard-build.log` pending. Next execute `Stage4DocumentTest,Stage4GraphTest,Stage4PagingTest,Stage4ProviderCacheTest,Stage4ObservationCacheTest` on verified API26, then API37. API26 emulator-5584 remains running; API37 emulator-5582 stopped with AVD retained. No owner data/files discarded. CI for pushed d67982e/39185227123 was still in progress; earlier4891cbd CI was automatically cancelled after newer push, NOT passed.


## Latest verified source state — document/evidence guards

All code is frozen and coherent. Latest command `GRADLE_USER_HOME=/tmp/goldberg-gradle-home ./gradlew --offline assembleDebug assembleDebugAndroidTest testDebugUnitTest lintDebug --no-daemon --max-workers=1` passed in2m12s: **93 JVM tests,0 failures/errors/skips; lint0 errors/94 warnings; both APKs built**. Newest five-class Android command (explicit verified serial, install -r before execution) passed **30/30 on API26** (12.415s) and **30/30 on API37** (10.909s):

```sh
/home/stokonn/Android/Sdk/platform-tools/adb -s emulator-5582 shell am instrument -w -r -e class com.opus.piano.Stage4DocumentTest,com.opus.piano.Stage4GraphTest,com.opus.piano.Stage4PagingTest,com.opus.piano.Stage4ProviderCacheTest,com.opus.piano.Stage4ObservationCacheTest com.opus.piano.test/androidx.test.runner.AndroidJUnitRunner
```

Repeat uses emulator-5584 only after qemu/API26/boot verification. No implicit device selection or physical-phone operation. Evidence is `results/evidence-guard-*`; source/artifact manifest hashes every app/src input and available Gradle config. APK `app/build/outputs/apk/debug/app-debug.apk` SHA256 **d9e445666a54d031d84fa904cf11397697299fc818e74a50b8c8bcc4e388e626**, package com.opus.piano,0.10.1/code11, trusted cert53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e. No schema change (personal10/catalogue1), migration, version bump, APK upload or release. Owner RELEASE_NOTES.md unchanged/untracked/excluded. No current compiler/test failure in these targeted scopes; initial18-test failure and correction remain preserved.

New work actually implemented/verified: explicit provider continuation and stable append/replay paging; per-endpoint partial failure; bounded unresolved observation eviction; legacy response-cache refresh; strict malformed-JSON/UTF8 classification; dynamic critical-storage reserve; approved sourced-language fallback with own revision; actual work subtype detail reads; restoration of tentative subtype after rejected relationship.4A–4F remain **incomplete**. Prior105-entry suites, signed seeded upgrades, screenshots and performance are historical per-artifact evidence, NOT final acceptance of this APK. Current new subtype renders/large-font accessibility and full lifecycle regression have not been executed.

### Precise remaining work and next action

1. Recover actual latest local/remote HEAD and CI (`git log -1`, `git status --short`, `gh run list --branch feature/goldberg-0.11-stage-4 --limit 3`). Preserve every modification/untracked owner file. Inspect this latest source manifest rather than restarting initial7 tests. Check prior remote39185227123 and the newer final WIP run; cancelled runs are not green.
2. Complete immutable accepted-evidence lifecycle and aggregate concurrent intake guard under catalogue serialization. Never delete contradictory accepted assertions/identity revisions to satisfy eviction; keep disposable observations/media separate. Test cache pressure and cancelled/stale refreshes without personal writes.
3. Complete reversed/conflicting cross-provider identity resolution, redirects and diagnostics; inspect candidate corrections, retain evidence, no title/score-based merges. Fix document observation key/language retention if identical response hashes occur across editions; retain old evidence.
4. Complete licensed portrait acceptance. Current one-shot small-image probe is CC BY2.0 and remains outside exact3/4 allowlist; no image downloaded. Any extension needs exact terms, complete supplied title/notices/credit retention and tests, not a blanket license relaxation. Biography selection/presentation and provenance still need full UI acceptance; raw technical field labels and numeric legacy-artist fallbacks need attention. Search-result subtype labels need broader coverage beyond linked details.
5. Execute final CZ/EN/detail/navigation/Back/restoration/offline/theme/font/accessibility screens and inspect captures; do not reuse old screenshots as current renders. Complete remaining privacy/legacy-network audit and real permitted provider compatibility, stopping on overload.
6. Investigate measured50k query cost, storage/native-PSS/frame/startup uncertainty; retain actual1k/10k/50k vectors. Then full required JVM/host/Android suites and all real process-death phases, signed seeded0.10.1 upgrades against final source on API26/API37. Existing scripts/AVDs are retained; no uninstall to bypass upgrade.
7. Verify package/cert/hash/source, final changed-file inventory, commit/push and mandatory clean remote CI. Keep Issue31/Project2 open/In Progress until all acceptance passes; synchronize only verified English Wiki/docs. No main merge/tag/release/APK publication/Stage5.

This is a resumable WIP checkpoint due session context budget, not product completion. Continue authorized implementation from latest HEAD; checkpoint/green CI alone is not a reason to stop while capacity remains.

Prior pushed `d67982ebe7c2323bcafd24bf5c8b66f2b3f81037` CI **passed**: https://github.com/stokonn/Goldberg/actions/runs/39185227123 . New document/graph source is newer; do not transfer that remote result to it. Final cheap host checks (migration1–9→10, historical backup fixtures, Stage3Bv5 fixture hashes,888 CZ/EN resources, journal paging, policy, unchanged schema exports/whitespace) all passed. Both test emulators are now stopped, with their AVD data/configs preserved. Local APK/testAPK copies are retained under ignored `.gradle/stage4-wip/Goldberg-stage4-evidence-guard*.apk`.
