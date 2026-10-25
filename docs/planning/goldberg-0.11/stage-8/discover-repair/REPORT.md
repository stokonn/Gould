# Stage 8 — Discover repair verification report

Current status: bounded Discover repair **verified; debug APK ready for owner review** at `04bf2025f991b1eb3ef4a30b43a0e227fae7b85a`. Application implementation is byte-identical to `fc8a440`; only the evidence-proven native viewport helper changed. Host verification:308JVM/0failures/errors/skips,debug/testAPK builds,lint0errors120warnings1hint. Final API 36 verification and visual inspection passed as detailed below; owner review remains OPEN. Full Stage 8 remains incomplete. Debug APK only; no RC,Release or main merge.

## Scope and behavior

Baseline: exact published `v0.11-rc.1`, `a73a7e21adf656dcbe7ed6da351a71e9a681a384`. Final verification source: `04bf2025f991b1eb3ef4a30b43a0e227fae7b85a` (application implementation `fc8a440be2e00321876acf5990bbf5ca82cc1604`) on `fix/goldberg-0.11-discover-ux`; ordinary push/remote readback exact. No worktree or branch switch.

- CatalogueSearch ranks exact aliases first, then verified composer surname tokens, then prefixes and broader matches. Provider emissions use current ranked pages, rather than arrival-order append. Cursor sort version changes explicitly; no catalogue identity is inferred from titles.
- MusicBrainz Person/Group/Orchestra/Choir/Character/Other classifications remain typed and tested. A person is not automatically labelled a composer. Row context reads explicit OpenOpus composer identities and composer-credit relationships, and shows provider names to distinguish independent similarly named entries. Three bounded page-wide queries run in the existing IO repository scope.
- Default-off Online Catalogue in Settings authorizes automatic provider search: local results immediately, ~500ms debounce, at least two trimmed characters, cancellation for query/filter/consent changes, separate local/online sections and canonical-ID deduplication. Provider-origin IDs remain bound through paging. Transport/client/repository also check the persisted consent and existing mobile-data policy. No production community networking is activated.
- Discover uses the editorial title/type/composer hierarchy, selected approved essentials, primary Add/Open action, related people/music and optional licensed biography. Library/Discover share responsive metadata cards and existing button/theme/typography/navigation primitives. Existing entries are found by reviewed provider IDs using read-only queries, never by title. Original Add selection reconciliation remains intact.
- Sources & credits collapses exact field/value/language duplicates into display groups that retain every source row, rights object, original assertion and conflict. Unapproved content is not promoted. Human date/language/license labels and optional detailed source/revision/hash links replace the initial metadata wall. Correction/export and technical identity notices are secondary Advanced tools.

## OpenOpus investigation

Six bounded public HTTP response snapshots are retained under `app/src/androidTest/assets/discover-repair`, with query, URL, observed date, CC0 attribution and SHA256 manifest. These are test-only, not shipped in the app.

Current live responses for Frédéric Chopin, Ballade No.1 Op.23, Goldberg Variations, Johann Sebastian Bach and Mozart returned valid JSON. This does **not** prove the screenshot's particular failure fixed. Its exact historical response is unavailable and that disposition remains open.

The controlled empty-result request returns HTTP200 with `status.success="false"`, `status.error="Nothing found"` and no result arrays. The RC adapter rejected this legitimate empty envelope. The repair accepts only that exact empty condition, while rejecting other false/malformed/contradictory envelopes. Provider parsing requires a results/works/composers array for successful envelopes. Diagnostic codes distinguish `invalid_json`, `invalid_provider_schema` and `invalid_pagination`; HTTP failures retain their HTTP state. Invalid payloads are not retried automatically. Existing bounded transport retry, provider backoff, cached/stale fallback and projected provenance are preserved.

Regression coverage includes malformed JSON/schema/pagination, legitimate empty, cached success, stale fallback after failure, offline cache reads, no invalid-response retry, composer ranking, stable paging, namesakes, entity kinds, consent/debounce cancellation and immutable assertions. All four Discover native cases executed and passed in the final API 36 run, including recorded-response UI journeys and consent/cancellation checks.

## Actual host verification

Command (using the project wrapper):

```sh
ANDROID_HOME=/home/stokonn/Android/Sdk ./gradlew --offline assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1
```

Final application build8 **PASS,2m37s**: **308JVM,0failures/errors/skips**; debug/testAPK assembly; lint **0errors,120warnings,1hint**. Final native-harness compilation build12 PASS24s. [Current source/artifact inventory and host receipt](host-results/visual-follow-up/HOST-VERIFICATION.json); [original source host receipt](host-results/HOST-VERIFICATION.json).

Also executed/passed: `scripts/check_localization.py` (1073 matching EN/CZ resources), `check_agent_policy.py`, `check_history_paging.py`, `check_migrations.py`, `check_backup_fixtures.py`, `check_admin_contract_fixtures.py` (29 fixtures,15 pinned source hashes), `bash -n scripts/ci_android_instrumentation.sh`, fixture URL/query/hash validation and `git diff --check`. Host migration/backup DDL checks are not Android migration/restore execution.

Initial build.log FAILED: production compilation ran before helper additions while new tests referenced `groups`, `languageLabel`, `retrievedLabel`. Fresh build2 passed. Build3 FAILED on nullable `DiscoverDetail?` in the new native harness; explicit available/non-null detail handling corrected it. Builds4,5,6 passed. Failed logs remain retained; no failure is hidden or counted as a pass.

## API36 execution and visual evidence

`/dev/kvm` is absent locally. Read-only software emulator5596 stayed offline and exited SIGSEGV139. No native/local visual pass is inferred. Original AVD/data and all other worktrees/artifacts remain preserved. No owner's physical phone was used; every issued ADB command explicitly selected emulator5596.

Three source-bound executions are retained; no unchanged redispatch:

| Source | Verification | Actual disposition |
| --- | --- | --- |
| `7db1d91` | [API3640221541462](https://github.com/stokonn/Goldberg/actions/runs/40221541462), [quality40221513320](https://github.com/stokonn/Goldberg/actions/runs/40221513320) |207native,196PASS,0failures/errors,11existing skips; seeded upgrade PASS.28 PNG/tree pairs validated;12 original PNGs inspected and concrete visual findings identified. |
| `fc8a440` | [API3640225676513](https://github.com/stokonn/Goldberg/actions/runs/40225676513), [quality40225660057](https://github.com/stokonn/Goldberg/actions/runs/40225660057) |Quality/upgrade PASS; native207cases,195PASS,1failure,0errors,11existing skips. Five additional original frames inspected. Fully-visible action was rejected by a helper requiring a scrollable container; saved bounds/PNG prove the actual button fits. |
| `04bf202` | [API3640237642842](https://github.com/stokonn/Goldberg/actions/runs/40237642842), [quality40237601629](https://github.com/stokonn/Goldberg/actions/runs/40237601629) |SUCCESS: 207 native / 196 passed / 0 failures/errors / 11 existing skips; all four Discover cases passed. Quality PASS, 308 JVM / 0 failures/errors/skips. Seeded upgrade PASS. All 41 PNG/tree pairs validated and original PNGs inspected. Application hashes/APK unchanged from fc8a440. |

The new native journey uses production composables in a test NavHost/scaffold and recorded public metadata through controlled transport. Real emulator PNGs, accessibility trees, five query outputs, request/cancellation counts and immutable-assertion evidence are retained. Before frames replay the immutable RC layout with current repository/resources; they are **not** execution of the published RC binary. The matrix covers two actual sizes (824×1830 and720×1280 pixels at320dpi), each with EN/Light1×,EN/Dark2×,CS/Light2×,CS/Dark1×. It does not claim every Cartesian combination or physical-device testing.

Observed first-run findings and implemented repairs: cache/history/source controls now secondary; singular entity icons corrected; controlled main genre/period values localized without changing evidence/names; empty Library Overview and large default missing-movement panel removed, with truthful explanation/tools retained under Sources & Study. JSON detail preparation runs on Dispatchers.Default. History toggles have named accessibility and cause no unrelated query refresh.

The fc8a440 failure is retained and classified accurately: standardEN1× action textRect212,652–613,692 is fully within windowRect0,0–824,1830; the fitting list has no scroll capability. The harness now accepts that state while requiring the entire enabled clickable action to fit. The launcher `discover-FAIL` frame was captured after ActivityScenario cleanup and is not an app-crash frame. New failure capture runs before cleanup. Source-bound [geometry receipt](ci-40225676513/visibility-cause.json) and original artifacts remain preserved.

Existing small-screen2× navigation-label wrapping/hyphenation is unchanged accepted Stage8 behavior. Primary actions require normal vertical scrolling on small screens at2×; credits frames show the full reachable action. Android clips accessibility bounds, so the helper pass alone does not prove full-control visibility. See the [final visual review and all 41 original PNG/tree pairs](ci-40237642842/VISUAL-REVIEW.md). Optional external-provider/portrait fixture cases and separately driven process/reboot probes are not counted as executed passes.


Final seeded upgrade: all original typed cells in 12 tables compare equal, preserving 37 sessions/37 attribution rows, 2 pieces, 4 badges, profile, settings, draft and recovery archive. Offline schema8→10, upgraded/post-restart integrity and foreign keys PASS; preferences unchanged. [Raw upgrade receipts](ci-40237642842/upgrade/). CI baseline/candidate use the same temporary debug signer; the local review APK trusted production-compatible signer is independently verified below. Full SQLite snapshots remain in the CI artifact, not committed to the repository.

Final raw [source/counts/assertions/requests/screenshot/tree evidence](ci-40237642842/) and [artifact verifier receipt](ci-40237642842/artifact-verification.json) are preserved. Actual controlled requests: 10; actual in-flight cancellation: 1. The independent validator verifies 41 pairs, source SHA and work assertions unchanged 2→2. The native test additionally checks composer history unchanged.

## Debug artifact

Local review APK: `.gradle/discover-review/Goldberg-0.11-discover-debug.apk`, 24,405,679 bytes. `com.opus.piano`, `0.11-dev`, code12, **debuggable**.

SHA256: `6a5669b17e6c7ef47b4adcbbee2c4df8a742b7532caa87628ac586fb842a31e0`.

Actual signer: `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`, matching the actual published RC. ZIP CRC and production DEX/assets exclusion of test fixtures/RC replay PASS. Trusted RC download retains original `c5662d8ead2b175fac0be09145776e84936c2b8f794c85b967f71aaab1938bb5`. Original7db debug APK remains preserved separately. No signing key change, uninstall workaround or release APK build.

## Preservation and open gates

No personal schema/migration, database name, application ID, practice state/timing, history, backup format, settings keys, source evidence/rights, community production setting or cloud credentials changed. The final full native suite passed with 11 existing skips: two optional external-provider/portrait fixtures and nine separately driven process/reboot/restore probes. Skips are not passes. Owner review and the exact historical OpenOpus failure disposition remain open. Hosted community/OAuth/legal/private-runtime release gates are unaffected and remain separately blocked. Issue31 OPEN / Goldberg Development In Progress; no duplicate issue/project/stage, Wiki publication, main merge, tag or Release.

Permanent owner policy is recorded in AGENTS.md, docs/ai/DECISIONS.md and release documentation: development debug APKs only; preserve historical RC; stable release/main merge require explicit approval, full verification and a fresh clean trusted-signed non-debuggable release build with safe upgrade checks.

created by stokonn with love <3

Final synchronized [Issue31 checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6404212021); Project remains In Progress. Documentation/evidence receipt follows tested source04bf202 without application changes.
