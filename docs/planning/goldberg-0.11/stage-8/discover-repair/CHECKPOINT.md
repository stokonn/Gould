# Stage 8 — Discover stabilization and UX repair

## Final Discover repair checkpoint — ready for owner review

Verified source `04bf2025f991b1eb3ef4a30b43a0e227fae7b85a` on `fix/goldberg-0.11-discover-ux`; application implementation `fc8a440`. [Final report](REPORT.md). Exact-SHA API36 run40237642842 and quality40237601629 SUCCESS. 308 JVM / 0 failures/errors/skips; 207 native / 196 passed / 0 failures/errors / 11 existing skips; all four Discover cases passed. Seeded offline upgrade preserved every original cell across12tables, preferences and post-restart integrity. All41 original PNGs inspected with hierarchy evidence; see visual review for clipped initial small-screen2× action and accepted navigation wrapping limitations.

- [x] Ranking/category/identity and automatic consented500ms search, cancellation and dedup regression verification.
- [x] Malformed/empty/cache/offline/provider-failure handling and bounded retries, without claiming the unavailable original screenshot response reproduced.
- [x] Shared editorial detail UI, localized selected metadata, grouped immutable source evidence and secondary advanced tools.
- [x] Host JVM/lint/debug/testAPK/repository checks and actual source-bound API36 journeys.
- [x] Visual review of41original PNG/tree pairs, EN/CZ, two sizes/themes and enlarged fonts; before is RC-layout replay, not RC-binary testing.
- [x] Reviewed implementation ordinary-pushed with exact remote readback; final evidence/docs receipt follows without application changes.
- [ ] Owner review of debug APK; fullStage8/canonical/hosted integration and release-specific gates remain open.
- [ ] Exact historical OpenOpus screenshot request/response remains unavailable; do not infer its root cause from live success.

Debug `.gradle/discover-review/Goldberg-0.11-discover-debug.apk`, SHA256 `6a5669b17e6c7ef47b4adcbbee2c4df8a742b7532caa87628ac586fb842a31e0`; trusted signer53eface… matches historicalRC. No data/schema/practice/backup/provenance loss, physical phone, newRC/tag/Release/main/community activation. Next: owner reviews debug/report; implement only concrete review findings, with new source-bound tests if code changes. Keep Issue31 OPEN / Project In Progress. Historical checkpoints below preserve their original source-bound results.

Purpose: repair published RC catalogue relevance, provider feedback and editorial details without changing identities, source history or personal data. This continues Stage 8.

Branch: `fix/goldberg-0.11-discover-ux`; baseline/RC tag: `a73a7e21adf656dcbe7ed6da351a71e9a681a384` (exact match, clean initial worktree). Release and all other worktrees/artifacts preserved. Issue #31 OPEN; Goldberg Development In Progress; milestone 4. Latest published-RC checkpoint read before implementation.

Scope: provider adapters/client, bounded catalogue search/paging, Discover presentation, shared Library editorial components, localized resources, regression tests and evidence. No personal schema/migration, backup format, practice state, production community networking, credentials, main or release change.

- [x] Inspect baseline, policy, maintenance ledger, release, Issue/milestone/Project and latest checkpoint.
- [x] Read provider parsing/ranking/intake and Discover/Library detail rendering.
- [x] Fetch bounded public OpenOpus diagnostics for five acceptance searches plus empty-result control. HTTP JSON for five succeeds; empty result uses `success:"false", error:"Nothing found"`, currently rejected as invalid. Original screenshot failure is not yet independently reproduced for its exact request.
- [x] Repair relevance and category handling; retain independent provider identities and stable bounded paging.
- [x] Reproduce provider parsing/failure/cache handling with controlled fixtures; localize useful status and bound requests.
- [x] Editorial detail hierarchy, shared metadata layout, grouped/collapsible preserved provenance and advanced tools.
- [x] Meaningful JVM and instrumentation regressions.
- [x] API 36 before/after search/detail/repertoire/offline/failure/navigation journeys; EN/CZ, Ivory/Nocturne and enlarged fonts; inspect actual captures.
- [x] Host Gradle JVM/lint/APK/androidTest compilation and applicable repository checks PASS at build6; native execution remains a separate unchecked task.
- [x] Review diff, synchronize Issue description/comments/Project and local checkpoint; commit/push only verified scope on this branch.

Initial inspection checkpoint (historical): six public HTTP diagnostics; no build/native result at that point. Current verification and owner scope are recorded below. Implementation IN PROGRESS, native acceptance pending. No release publication authorized.

created by stokonn with love <3

## Verified host milestone and expanded owner scope

Owner decisions during implementation: debug APKs only; no new RC/prerelease/development Release; fresh clean stable build and main merge/publication each require explicit approval. Existing RC/tag remain immutable. Recorded in canonical AGENTS.md, docs/ai/DECISIONS.md and Stage 8 release/checklist documents.

Online Catalogue Settings consent now authorizes automatic ~500 ms debounced provider search, local-first sections, cancellation on query/filter/consent changes, minimum two trimmed characters and canonical-ID deduplication. The app's provider transport/client/repository enforce the persisted consent plus existing mobile-data policy. Community networking remains disabled. No per-query button.

Implementation progress (acceptance still pending):

- Ranking prefers verified composer surname tokens ahead of weak prefix names; provider refresh emissions are re-ranked instead of appended in arrival order. Cursor ordering changes are explicitly versioned; independent same-title identities remain separate. Unknown candidates stay in All rather than leaking into typed filters.
- OpenOpus recognizes only the exact legitimate empty envelope. Invalid JSON, schema and pagination have distinct safe diagnostic codes; invalid responses receive no automatic retries. Stale-cache fallback/backoff remain intact. The screenshot's original failing request has NOT been reproduced, and is NOT marked fixed solely from successful live calls.
- Discover details lead with title/type/composer, essential selected approved metadata, repertoire action and meaningful related entities. Optional licensed biography follows. Credits group exact field/value/language equivalents without modifying any assertion, source, rights or history. Conflicts stay separate. Advanced tools are secondary. Library/Discover share the responsive editorial metadata component.
- Native regression and screenshot/tree journeys added, including recorded public CC0 provider bytes with source URL/hash manifest. Before frames explicitly replay the RC layout in test-only code on the same evidence; they are not claimed as published RC binary execution. Fixtures/RC replay must be excluded from production APK.

Actual commands/results so far:

- Initial `/tmp/goldberg-discover-build.log`: FAILED at `compileDebugUnitTestKotlin`. Helpers were added after production compilation had already run, while newly compiled tests referenced them (unresolved `groups`, `languageLabel`, `retrievedLabel`). This was a moving-source build, not an HTTP/provider or Gradle infrastructure failure.
- Fresh completed-source `/tmp/goldberg-discover-build2.log`: `ANDROID_HOME=/home/stokonn/Android/Sdk ./gradlew --offline assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1` PASS, 3m27s. 306 JVM/0 failures/errors/skips; APK and test APK assembled; lint 0 errors/120 warnings/1 hint. This precedes the subsequently authorized automatic-search changes and is not transferred to them.
- `/tmp/goldberg-discover-build3.log`: production/JVM build passed; new visual test compilation FAILED on nullable `DiscoverDetail?` dereferences. Corrected with explicit non-null assertions on previously checked available detail; next build in progress.
- Local API36 software-only emulator-5596 remained offline and exited with SIGSEGV (139). `/dev/kvm` is absent. NO native pass or inspected UI evidence is claimed from it. Existing AVD was opened read-only; original data untouched. Use the existing dispatch-only GitHub API36 workflow with exact pushed-commit binding.
- Localization PASS (1066 resources before two subsequent related-section labels); agent policy, history paging, DDL migrations v1–v9→v10 and backup fixtures formats1–3 PASS. These are host checks, not Android migration/backup execution.

Next: finish latest host validation/review, publish reviewed source to this isolated branch as needed for exact-SHA CI, execute full existing native workflow, inspect PNGs and accessibility trees, fix demonstrated defects with meaningful new evidence, then synchronize final source-bound results and deliver debug APK/report. No main, release, signing-key, database-schema or production community change.


## Automatic-search source host validation

`/tmp/goldberg-discover-build4.log`: Gradle debug APK + JVM + instrumentation APK compilation + lint PASS, 2m53s. 306 JVM/0 failures/errors/skips. This includes automatic debounce/provider-origin tracking and the compiled visual journey, not its execution. Latest follow-up adds the read-only identity-based Open action, malformed historical-scalar grouping preservation and a regression; host validation of that follow-up is running in build5. Native acceptance remains pending.

Project readback: existing Goldberg Development item `PVTI_lAHOBZNsL84BnaAUzhBhW5c` remains In Progress, Version0.11, PriorityMedium, TypeFeature; no duplicate issue/project/stage created.


## Reviewed source ready for exact-SHA CI

Final pre-CI build6 PASS, 3m3s: **307 JVM / 0 failures/errors/skips; debug APK + testAPK assembled; lint 0 errors / 119 warnings / 1 hint**. Final source adds read-only verified-identity Open action, exact historical assertion grouping fallback and three page-wide reads for truthful composer/artist-type/provider labels (no per-row main-thread I/O). Native lookup/ranking/cache/debounce and screenshot harness compile; execution still PENDING.

APK: `.gradle/discover-review/Goldberg-0.11-discover-debug.apk`, `com.opus.piano`, `0.11-dev`/code12, **debuggable**, 24,404,807 bytes; SHA256 `877abadbcb74d8f2c25d6b664e52207473338e9390a61a1d4bfd0e7ad98bd36c`. Actual signer matches trusted `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e` and the actual downloaded RC. ZIP CRC PASS; six public fixtures and test-only RC layout replay excluded from app DEX/assets. Published RC remains byte-identical `c5662d8e…`. Full source hashes and commands: [host receipt](host-results/HOST-VERIFICATION.json); all successful/failed host logs retained alongside it. No release APK built.

Checklist/review: localization1071 resources PASS; agent policy, history paging, migrations/backup fixture DDL and whitespace PASS. All six fixture query/URL/hash bindings PASS. No personal schema, database recreation, backup/practice history, source/rights deletion, cloud credentials, community enablement, main, other worktree or release artifact mutation. Source commit/push is solely to execute the owner's required exact-SHA API36 CI; this is not native acceptance or release approval. Existing workflow will run the full suite plus seeded upgrade on explicit guarded emulators. Native PNG/tree inspection and original OpenOpus screenshot disposition remain OPEN.

## Exact-source publication for required emulator CI

Reviewed source committed and ordinary-pushed to the existing isolated branch: **`7db1d91230d157d1f2033a2e61ec3ac86c1ba8c3`**, remote readback exact. Initial post-push worktree clean. Actual production files match every hash in HOST-VERIFICATION.json. Admin contract checker also PASS:29 fixtures/15 pinned hashes; no backend execution.

[API36 full instrumentation + seeded upgrade run40221541462](https://github.com/stokonn/Goldberg/actions/runs/40221541462) is IN PROGRESS; headSha readback exactly `7db1d91230d157d1f2033a2e61ec3ac86c1ba8c3`. [Quality40221513320](https://github.com/stokonn/Goldberg/actions/runs/40221513320) is also in progress at the same source. NO CI success/native/UI pass is claimed yet. Existing dispatch-only workflow and guarded emulator serials retained; only its existing evidence collector adds the new test's PNG/tree directory. Full native suite is not narrowed or skipped. Current local documentation updates follow the frozen tested source; wait for actual artifacts before final acceptance.

## Independent quality CI milestone

[Quality40221513320](https://github.com/stokonn/Goldberg/actions/runs/40221513320) **SUCCESS**, exact headSha `7db1d91230d157d1f2033a2e61ec3ac86c1ba8c3` verified by readback. API36 workflow40221541462 has reached actual full instrumentation and seeded-upgrade execution; results/visual acceptance still pending. A local artifact verifier now checks immutable source binding, actual native JUnit execution, four new Discover cases, PNG CRCs/dimensions, paired accessibility trees, five query outputs and source-assertion preservation. It explicitly requires separate visual inspection and cannot turn compilation into a native pass.

## API36 upgrade job milestone; full suite pending

Workflow40221541462 `5.11 acceptance subset — API36 emulator` job reports SUCCESS at the dispatched source. Raw upgrade diagnostics/artifacts still need inspection before detailed preservation assertions are added. The full instrumentation job is still executing; no full native or visual acceptance claim yet.

One bounded public MusicBrainz Chopin artist probe returned HTTP503 at 18:42UTC (October25), recorded without retry. Actual artist classifications for the screenshot's weak names cannot be inferred from that failed response. Adapter projection and controlled Person/Group/Orchestra/Choir/Character/Other tests are independent evidence; public source availability is not an application test result.

Upgrade artifact inspection PASS: source-commit attestation exactly7db1d91; `upgrade/result.json` records offline schema8→10, unchanged original rows/preferences,37sessions and foreign keys PASS. Independently compared `before.json == after.json` for every cell of all12 original tables:4badges/2pieces/1draft/1recovery archive/profile/settings/37sessions/37parts, including empty original tables. Independently opened upgraded and post-process-restart SQLite snapshots read-only:integrity `ok`,0FK violations,schema10,37sessions both. CI baseline/candidate use the same temporary signer9b9ac2…; downloaded stable certificate53eface… and the separate local review APK's trusted signing are verified independently. This is not a claim that the CI APK carries the production key. Source-bound raw receipt subset preserved in `ci-40221541462/upgrade/`; full original artifact remains downloaded under /tmp and on the linked CI run. Full native/visual result still pending.

## First native pass and actual visual findings — follow-up in progress

Source7db1d91 [API36 workflow40221541462](https://github.com/stokonn/Goldberg/actions/runs/40221541462) SUCCESS. Raw JUnit/attestation inspection: **207 cases,196 PASS,0 failures/errors,11 existing skips** (2 optional external fixture cases and9 separately driven preservation/process/reboot probes). All3 DiscoverRepairTest methods and1 DiscoverEditorialAcceptanceTest method executed/passed. Artifact verifier PASS:28 PNG/tree pairs,5 native query outputs,10 controlled requests/1 actual cancelled in-flight request,work assertions2→2 unchanged. Original screenshots/trees/JUnit/source attestations preserved under `ci-40221541462/native/` with hashes.

Actually inspected12 original PNGs plus their hierarchy evidence (names in follow-up host receipt): before/after work detail, automatic Chopin/Goldberg, provider failure/offline, Ballade, Library composition and representative CS/EN/light/dark/2× captures. Do not infer that all28 were visually inspected. Findings requiring the following bounded repair:

- Default short search still exposed cache/history explanations and oversized source controls. These now live in collapsed Advanced catalogue tools; connection/error feedback remains compact beside online results.
- Artist/work rows incorrectly shared the generic fallback icon because singular entity kinds did not match plural category keys. Explicit kind aliases fix actual types.
- Czech main metadata showed the controlled genre `Keyboard` in English. Known genre/period labels now use existing localization mappings; original source values and all work/composer names remain unchanged.
- Library showed an empty Overview and a large missing-movement panel. Empty Overview is omitted; the truthful explanation and original Listen & Study tools remain available through a shared secondary disclosure. No movements are fabricated or source tools removed.
- Detail source JSON is prepared once per detail/language on Dispatchers.Default. Historical evidence stays intact, including malformed-scalar fallback; no database change.
- History preference controls are named for accessibility and update local state without triggering unrelated provider searches.

Existing accepted navigation/hyphenation behavior is preserved. At2× on small screens the primary action can require scrolling; the expanded matrix now explicitly scrolls it fully into view and retains action frames. Native coverage also adds filter-based cancellation, history preference/no-refetch, actual composer detail/Back, expanded repeated credits/license/date evidence and optional Library study tools.

Follow-up host build7 PASS3m3s; build8 PASS2m37s: **308 JVM/0 failures/errors/skips,debug/testAPK assembly,lint0errors120warnings1hint**. Final expanded test-only compilation build10 PASS25s. Localization1073/policy/paging/whitespace PASS. New review APK SHA256 **`6a5669b17e6c7ef47b4adcbbee2c4df8a742b7532caa87628ac586fb842a31e0`**,24,405,679 bytes,0.11-dev/code12/debuggable/trusted53eface…; ZIP/test-fixture exclusion/source inventory PASS. Original debug7db APK is preserved separately. [Follow-up host receipt](host-results/visual-follow-up/HOST-VERIFICATION.json).

- [x] Investigate actual first-run native XML and inspect representative original screenshots/trees; retain all raw pairs.
- [x] Implement and host-verify the demonstrated visual/localization/accessibility/JSON-thread fixes.
- [x] Commit/push reviewed follow-up fc8a440 and dispatch exact-SHA workflow40225676513 after documented verified fixes; outcome recorded below.
- [ ] Inspect final-source native/upgrade results and new actual PNG/tree frames; synchronize final report and owner-review debug APK.

This remains Stage8 IN PROGRESS. First-source CI success does not transfer to changed source; updated native acceptance is pending. No release,main,cloud credential or production community action.

## Follow-up source fc8a440 — native visibility failure under investigation

Reviewed follow-up source committed/ordinary-pushed: `fc8a440be2e00321876acf5990bbf5ca82cc1604`; remote exact. Existing workflow [40225676513](https://github.com/stokonn/Goldberg/actions/runs/40225676513) dispatched after the documented screenshot-driven fixes; headSha readback exact. [Quality40225660057](https://github.com/stokonn/Goldberg/actions/runs/40225660057) SUCCESS; API36 seeded-upgrade job SUCCESS. Full instrumentation FAILED:207 cases,1failure,0errors,11existing skips (195passes). The reported failure is solely `DiscoverEditorialAcceptanceTest.automaticSearchAndEditorialJourneysWithPreservedEvidence`: `Unreachable Open existing repertoire entry`, at the expanded matrix's fully-visible action check (line297), through the forward-only `scrollTo` helper. This is not yet classified as an app or harness defect; original PNG/tree/JUnit artifacts are being retrieved before deciding a fix. No unchanged redispatch.

The final source has NOT passed native/visual acceptance. The original 7db1d91 pass remains historical evidence. Local debug APK remains SHA2566a5669b17e6c7ef47b4adcbbee2c4df8a742b7532caa87628ac586fb842a31e0; no new release,main merge or community activation. Read-only CI inspection initially timed out in automatic permission review; its permitted single retry succeeded. No approval bypass or authentication change.

Next: inspect the failed frame/hierarchy and XML; fix a demonstrated cause; compile/review and push only that correction, then execute the existing exact-SHA workflow and inspect actual final captures before owner-review delivery.

## Demonstrated harness cause and correction (application unchanged)

Downloaded raw fc8a440 artifact:207native/195PASS/1failure/11existing skips. Failure occurs at the FIRST standard/EN/Light/1× matrix action check, before later font configurations. The original `standard-en-Light-f1.0-detail.png` visibly shows the whole Open action; its hierarchy contains0scrollable containers, windowRect(0,0–824,1830),action textRect(212,652–613,692). Independent saved-geometry replay confirms full containment. The old helper required a non-null scrollable viewport even when content fit, so it failed despite a visible action. Original `discover-FAIL` shows the launcher AFTER ActivityScenario cleanup; it is preserved but is not an app-failure frame. Raw screenshots/hierarchies/JUnit and the hash-bound geometry receipt are retained under `ci-40225676513/`.

Corrected test helper: choose the target's scrolling ancestor; use window bounds when scrolling is unnecessary; check the entire enabled clickable action, not just its text; scroll backward if the target is clipped above the viewport; retain a failure capture while the activity is still open. Assertions remain full-visibility/enabled bounds checks. Application code/APK are unchanged: every production-source hash and debug APK hash6a5669… still matches the follow-up host receipt. Native compilation build12 PASS24s; no unexecuted native pass claimed. A new exact-SHA workflow is justified by this demonstrated and independently replayed harness defect, not an unchanged retry.

The fc8a440 journey completed automatic consent/debounce, history/no-refetch, filter cancellation, five searches, composer navigation/Back, expanded grouped evidence/date/license, repertoire add and optional Library study checks before the matrix assertion failed. These are observed completed substeps, not a passing whole test. Five original fc8a440 PNGs additionally inspected: standardEN1×detail,workdetail,expandedcomposerprovenance,Librarycomposition,automaticGoldberg. Source-app findings from the first run are visibly repaired in these frames; remaining CZ/theme/large-font matrix requires a successful final run.

## Corrected harness dispatched at exact source

Native-only correction and retained diagnostics committed/ordinary-pushed: `04bf2025f991b1eb3ef4a30b43a0e227fae7b85a`; remote exact. New [API36 run40237642842](https://github.com/stokonn/Goldberg/actions/runs/40237642842) and [quality40237601629](https://github.com/stokonn/Goldberg/actions/runs/40237601629) IN PROGRESS; both headSha readbacks equal04bf2025f991b1eb3ef4a30b43a0e227fae7b85a. No unchanged redispatch: the concrete no-scroll viewport defect is fixed and independently replayed. Final application source remains byte-identical to fc8a440, debug APK still6a5669…. Final native/visual acceptance remains OPEN pending this run and actual matrix PNG/tree inspection. No new RC,Release,main merge or community action.

Final-source quality milestone: [40237601629](https://github.com/stokonn/Goldberg/actions/runs/40237601629) SUCCESS at exact04bf2025f991b1eb3ef4a30b43a0e227fae7b85a. API36 run40237642842 has reached actual full instrumentation and upgrade acceptance; results/remaining matrix inspection are still pending. Application/debug APK hashes remain unchanged.

Final synchronized [Issue31 checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6404212021); Project remains In Progress. Documentation/evidence receipt follows tested source04bf202 without application changes.
