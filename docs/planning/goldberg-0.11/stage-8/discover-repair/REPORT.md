# Stage 8 — Discover repair verification report

Current status: screenshot-driven follow-up HOST VERIFIED (308JVM,APK/testAPK/lint PASS); updated-source native/visual acceptance PENDING. First source7db1d91 passed API36:207native/196pass/0failures/errors/11existing skips plus seeded upgrade.28 actual PNG/tree pairs preserved,12 original PNGs inspected; the resulting concrete visual findings/fixes are recorded in the live checkpoint. Original-source results below are historical evidence; they are not transferred to changed code. Final report will bind the follow-up commit and actual final captures. Owner review remains required; debug only, no RC/Release/main merge.

## Scope and behavior

Baseline: exact published `v0.11-rc.1`, `a73a7e21adf656dcbe7ed6da351a71e9a681a384`. Reviewed source: `7db1d91230d157d1f2033a2e61ec3ac86c1ba8c3` on `fix/goldberg-0.11-discover-ux`; ordinary push/remote readback exact. No worktree or branch switch.

- CatalogueSearch ranks exact aliases first, then verified composer surname tokens, then prefixes and broader matches. Provider emissions use current ranked pages, rather than arrival-order append. Cursor sort version changes explicitly; no catalogue identity is inferred from titles.
- MusicBrainz Person/Group/Orchestra/Choir/Character/Other classifications remain typed and tested. A person is not automatically labelled a composer. Row context reads explicit OpenOpus composer identities and composer-credit relationships, and shows provider names to distinguish independent similarly named entries. Three bounded page-wide queries run in the existing IO repository scope.
- Default-off Online Catalogue in Settings authorizes automatic provider search: local results immediately, ~500ms debounce, at least two trimmed characters, cancellation for query/filter/consent changes, separate local/online sections and canonical-ID deduplication. Provider-origin IDs remain bound through paging. Transport/client/repository also check the persisted consent and existing mobile-data policy. No production community networking is activated.
- Discover uses the editorial title/type/composer hierarchy, selected approved essentials, primary Add/Open action, related people/music and optional licensed biography. Library/Discover share responsive metadata cards and existing button/theme/typography/navigation primitives. Existing entries are found by reviewed provider IDs using read-only queries, never by title. Original Add selection reconciliation remains intact.
- Sources & credits collapses exact field/value/language duplicates into display groups that retain every source row, rights object, original assertion and conflict. Unapproved content is not promoted. Human date/language/license labels and optional detailed source/revision/hash links replace the initial metadata wall. Correction/export and technical identity notices are secondary Advanced tools.

## OpenOpus investigation

Six bounded public HTTP response snapshots are retained under `app/src/androidTest/assets/discover-repair`, with query, URL, observed date, CC0 attribution and SHA256 manifest. These are test-only, not shipped in the app.

Current live responses for Frédéric Chopin, Ballade No.1 Op.23, Goldberg Variations, Johann Sebastian Bach and Mozart returned valid JSON. This does **not** prove the screenshot's particular failure fixed. Its exact historical response is unavailable and that disposition remains open.

The controlled empty-result request returns HTTP200 with `status.success="false"`, `status.error="Nothing found"` and no result arrays. The RC adapter rejected this legitimate empty envelope. The repair accepts only that exact empty condition, while rejecting other false/malformed/contradictory envelopes. Provider parsing requires a results/works/composers array for successful envelopes. Diagnostic codes distinguish `invalid_json`, `invalid_provider_schema` and `invalid_pagination`; HTTP failures retain their HTTP state. Invalid payloads are not retried automatically. Existing bounded transport retry, provider backoff, cached/stale fallback and projected provenance are preserved.

Regression coverage includes malformed JSON/schema/pagination, legitimate empty, cached success, stale fallback after failure, offline cache reads, no invalid-response retry, composer ranking, stable paging, namesakes, entity kinds, consent/debounce cancellation and immutable assertions. Native cases are compiled; their execution is still pending at this checkpoint.

## Actual host verification

Command (using the project wrapper):

```sh
ANDROID_HOME=/home/stokonn/Android/Sdk ./gradlew --offline assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1
```

Final pre-CI build6 **PASS, 3m3s**: **307 JVM, 0 failures/errors/skips**; debug APK/testAPK assembly; lint **0 errors,119 warnings,1 hint**. Source inventory and full raw logs: [host verification](host-results/HOST-VERIFICATION.json).

Also executed/passed: `scripts/check_localization.py` (1071 matching EN/CZ resources), `check_agent_policy.py`, `check_history_paging.py`, `check_migrations.py`, `check_backup_fixtures.py`, `check_admin_contract_fixtures.py` (29 fixtures,15 pinned source hashes), `bash -n scripts/ci_android_instrumentation.sh`, fixture URL/query/hash validation and `git diff --check`. Host migration/backup DDL checks are not Android migration/restore execution.

Initial build.log FAILED: production compilation ran before helper additions while new tests referenced `groups`, `languageLabel`, `retrievedLabel`. Fresh build2 passed. Build3 FAILED on nullable `DiscoverDetail?` in the new native harness; explicit available/non-null detail handling corrected it. Builds4,5,6 passed. Failed logs remain retained; no failure is hidden or counted as a pass.

## API36 execution and visual evidence

`/dev/kvm` is absent locally. Read-only software emulator5596 stayed offline and exited SIGSEGV139. No native/local visual pass is inferred. Original AVD/data and all other worktrees/artifacts remain preserved. No owner's physical phone was used; every issued ADB command explicitly selected emulator5596.

The existing guarded workflow was dispatched once for reviewed source:

- [API36 full instrumentation plus seeded-upgrade40221541462](https://github.com/stokonn/Goldberg/actions/runs/40221541462): IN PROGRESS, exact headSha verified.
- [Independent quality40221513320](https://github.com/stokonn/Goldberg/actions/runs/40221513320): IN PROGRESS at the same SHA.

The new native journey uses production composables/NavHost and recorded public metadata through controlled transport. It exports actual emulator PNGs, accessibility hierarchies, source-bound query outputs and assertion-preservation evidence. Before frames replay the immutable RC Discover layout against the same stored evidence in test-only code; they are **not** presented as execution of the published RC binary. The matrix requests EN/CZ, Light/Dark, standard/small sizes and 1×/2× fonts. Actual counts, captures inspected and observed differences remain **PENDING**, not claimed from compilation.

## Debug artifact

Local review APK: `.gradle/discover-review/Goldberg-0.11-discover-debug.apk`, 24,404,807 bytes. `com.opus.piano`, `0.11-dev`, code12, **debuggable**.

SHA256: `877abadbcb74d8f2c25d6b664e52207473338e9390a61a1d4bfd0e7ad98bd36c`.

Actual signer: `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`, matching the actual published RC. ZIP CRC and production DEX/assets exclusion of test fixtures/RC replay PASS. Trusted RC download retains original `c5662d8ead2b175fac0be09145776e84936c2b8f794c85b967f71aaab1938bb5`. No signing key change, uninstall workaround or release APK build.

## Preservation and open gates

No personal schema/migration, database name, application ID, practice state/timing, history, backup format, settings keys, source evidence/rights, community production setting or cloud credentials changed. Existing Stage7/8 behavior remains subject to the pending full-suite result. Native/UI acceptance, owner review and exact historical OpenOpus failure disposition remain open. Hosted community/OAuth/legal/private-runtime release gates are unaffected and remain separately blocked. Issue31 OPEN / Goldberg Development In Progress; no duplicate issue/project/stage, Wiki publication, main merge, tag or Release.

Permanent owner policy is recorded in AGENTS.md, docs/ai/DECISIONS.md and release documentation: development debug APKs only; preserve historical RC; stable release/main merge require explicit approval, full verification and a fresh clean trusted-signed non-debuggable release build with safe upgrade checks.

created by stokonn with love <3
