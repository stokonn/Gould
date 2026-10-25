# Stage 8 — Discover stabilization and UX repair

Purpose: repair published RC catalogue relevance, provider feedback and editorial details without changing identities, source history or personal data. This continues Stage 8.

Branch: `fix/goldberg-0.11-discover-ux`; baseline/RC tag: `a73a7e21adf656dcbe7ed6da351a71e9a681a384` (exact match, clean initial worktree). Release and all other worktrees/artifacts preserved. Issue #31 OPEN; Goldberg Development In Progress; milestone 4. Latest published-RC checkpoint read before implementation.

Scope: provider adapters/client, bounded catalogue search/paging, Discover presentation, shared Library editorial components, localized resources, regression tests and evidence. No personal schema/migration, backup format, practice state, production community networking, credentials, main or release change.

- [x] Inspect baseline, policy, maintenance ledger, release, Issue/milestone/Project and latest checkpoint.
- [x] Read provider parsing/ranking/intake and Discover/Library detail rendering.
- [x] Fetch bounded public OpenOpus diagnostics for five acceptance searches plus empty-result control. HTTP JSON for five succeeds; empty result uses `success:"false", error:"Nothing found"`, currently rejected as invalid. Original screenshot failure is not yet independently reproduced for its exact request.
- [ ] Repair relevance and category handling; retain independent provider identities and stable bounded paging.
- [ ] Reproduce provider parsing/failure/cache handling with controlled fixtures; localize useful status and bound requests.
- [ ] Editorial detail hierarchy, shared metadata layout, grouped/collapsible preserved provenance and advanced tools.
- [ ] Meaningful JVM and instrumentation regressions.
- [ ] API 36 before/after search/detail/repertoire/offline/failure/navigation journeys; EN/CZ, Ivory/Nocturne and enlarged fonts; inspect actual captures.
- [x] Host Gradle JVM/lint/APK/androidTest compilation and applicable repository checks PASS at build6; native execution remains a separate unchecked task.
- [ ] Review diff, synchronize Issue description/comments/Project and local checkpoint; commit/push only verified scope on this branch.

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
