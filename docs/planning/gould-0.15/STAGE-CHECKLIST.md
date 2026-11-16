# Gould 0.15 — stage checklist (living document)

## Current owner-directed takeover — 2026-11-15

### Stage 2 — Full implementation: COMPLETE

- [x] Preserve and hash-verify the full-dump DB, compressed payload and diagnostic APK.
- [x] Pin exactly 100 reviewed composer identities to verified Core MBIDs across five periods.
- [x] Generate a compact seed with real linked works, aliases and composer relationships; validate integrity and foreign keys.
- [x] Package only the curated seed; inspect actual Release APK contents and verify the dump/payload are absent.
- [x] Integrate the direct HTTPS MusicBrainz adapter in unified Discover search, using the existing centralized throttle, paging, cache and MBID merge.
- [x] Add User-Agent/provider policy coverage and seed/APK verification scripts.
- [x] Run RELEASE host gate, app JVM suite, migration/backup/policy checks, lint and Android-test compilation.

### Stage 3 — Verification and release preparation: INCOMPLETE

- [x] Separate journal-owned copied identities from public search results; preserve existing personal records and quarantine old copied identities lacking approved public title evidence.
- [x] Propagate local catalogue initialization/access failures to Discover instead of converting them to an empty result page.
- [x] Add Android regressions for the actual packaged catalogue, Chopin/Bach/Mozart/Beethoven, accent/partial queries, Chopin work results, real Library/Discover rendering, empty results, initialization errors, personal-data preservation, Kanye West cache replay and MBID deduplication; compile instrumentation sources.
- [x] Run the final-source clean RELEASE gate: debug build, full JVM suite (327 passed, 0 failures/errors/skips), lint, Android-test APK compilation and seven mandatory policy checks.
- [x] Build and inspect a fresh trusted-signer, non-debuggable Release APK; verify curated asset bytes/hash and signer compatibility against the preserved prior artifact.
- [ ] Execute the bundled-catalogue repository and Discover/Library Compose instrumentation on an authorized disposable target. ADB access was rejected by automatic review because device enumeration could reach the owner’s phone; do not retry or work around that rejection. The existing checkpoint records the API 36 emulator exited before boot.
- [ ] Verify online-only search, detail loading, offline replay and repertoire-add UI at runtime; owner Galaxy A55 acceptance remains pending.
- [ ] Do not declare Stage 3 complete while runtime acceptance is unverified. No merge, push, tag, PR, release or public promotion is authorized.

The mandatory host/release gates passed. Stage 3 remains open solely for runtime
acceptance above; the APK is a verified local artifact, not a published release.
GitHub issue synchronization is pending because no existing Gould 0.15 Stage
issue was found; no duplicate was created.

## Latest Stage 2 implementation batch — 2026-11-15

### Admin/backend retirement continuation — 2026-11-15

- [x] Removed the obsolete Android legacy admin implementation, UI package,
      app-container wiring, unit tests, and Stage 7B runtime/acceptance tests.
- [x] Kept the canonical admin client/repository and existing BFF contract and
      deployment components. Replaced the canonical transport's dependency on
      `com.opus.piano.admin.AdminTransport` with its own bounded exchange type;
      app default still fails closed and performs no production requests.
- [x] Kept upgrade cancellation of persisted unique work
      `goldberg-admin-queue-check` and notification ID 701 without retaining the
      obsolete Worker implementation.
- [x] Added `check_admin_retirement.py` to guard retired routes, source packages,
      application wiring, and canonical imports; added it to repository quality CI.
- [x] Run focused JVM/static checks and relevant compilation: 325 app JVM tests
      passed (0 failures/errors/skips); `:app:compileDebugKotlin` and
      `:app:compileDebugAndroidTestKotlin` passed. `check_admin_retirement.py`,
      `check_localization.py` (1223 EN/CZ resources), and `git diff --check`
      passed. Instrumentation was compiled, not executed.

- Community draft recovery is implemented as a separate bounded version-1 JSON
  archive. Settings uses SAF create/open, with EN/CZ success/error copy and an
  explicit unencrypted-data warning. Import validates the whole archive,
  preserves IDs and frozen retry requests, scopes principal archives to the
  same account, rebinds guest archives to the local guest owner, rejects all
  duplicate/existing IDs, and stages/rolls back batch publication. Focused JVM
  tests cover round-trip, account and guest scopes, empty/malformed/incomplete
  data, duplicates/conflicts and an injected rollback failure. Exact behavior
  and limits: `COMMUNITY-DRAFT-RECOVERY.md`.
- Catalogue generator now rejects unknown fields and any non-MusicBrainz source
  provider in production input, while synthetic fixtures remain test-only.
  Installer rejects unsupported provider, license, assertion fields, documents
  and media. Validation precedes READY publication and
  revision promotion; disposable tests cover invalid DB/hash, previous
  generation retention, stable personal identity and repertoire anchor
  preservation. Android installer instrumentation compiled; native execution
  was not available in the scoped host run. This infrastructure is complete.
  Production source generation/import remains a separately gated prerequisite:
  the MusicBrainz key is unverified and final import requires separate owner
  approval.
- Publication workflow review is complete: existing workflows are read-only,
  do not access secrets on pull requests, and have no release action/command.
  A static policy guard and nine self-tests are wired into `quality.yml`; no
  publication workflow was added. Tests assert explicit least-privilege
  permissions, reject `write-all`, secrets and `pull_request_target`, require
  non-persistent checkout credentials, and reject release commands/actions.
- Admin/backend retirement is complete as recorded above;
  earlier evaluation and preserve-both statements are historical and superseded
  for the Android legacy client by the owner's current retirement decision.
- **Current verification:** see `VERIFICATION-MATRIX.md` and the 2026-11-15
  current checkpoint for the post-retirement run: app JVM 325/325, separate
  activity-domain JVM 13/13, lint and Android-test compilation passed; static
  guards passed. The earlier 426-test audit row is historical and predates
  deletion of obsolete legacy admin tests. A fresh clean Release-variant APK
  and `lintRelease` also passed; exact current artifact evidence is in the
  checkpoint. Instrumentation did not execute.
- Stage 2 is **COMPLETE** for its authorized implementation scope. All
  implementable Stage 2 work is complete and feasible host checks pass. The
  unverified MusicBrainz key/snapshot, real-record provenance review, and
  separate production-import approval remain prerequisites to future
  production catalogue data, not blockers to this infrastructure milestone.
  The canonical Android admin service and BFF are retained; legacy Android admin
  sources are retired. Production networking remains disabled. Independent
  MusicBrainz fingerprint authentication remains externally blocked; the
  incomplete archive transfer was stopped and no data was processed. The exact
  owner action is recorded in the current checkpoint and provenance policy.
  Galaxy A55 visual/accessibility and tour acceptance remain owner-run checks;
  native instrumentation was compiled but not executed. Stage 3 remains
  unauthorized. All work remains uncommitted.

## Earlier Stage 2 batch review — superseded by current completion record

- Catalogue: strict allowlists now reject unapproved fields at dataset, record,
  source, alias, rights-policy, and package-rights levels. Explicit examples
  include annotations, genres, ratings, tags, and popularity. Production mode
  rejects both the synthetic-fixture marker and fixture provider. Synthetic
  tests assert output UUID and external source ID preservation. Seven Python
  tests pass. Android installer/personal-identity preservation remains source
  reviewed, not runtime tested. Real source generation/import remains blocked
  by signing-key authentication and separate import approval.
- Activity/streak/rewards: source review found consumers consistently use the
  canonical both-goals rule; Beta week eligibility is intentionally distinct
  between cosmetic rewards and streak/Freeze progression. Corrected a
  misleading KDoc. Existing domain/repository tests cover mixed goals, Beta,
  Freeze, reward idempotency and retrospective corrections. App JVM suite and
  activity-domain JVM task passed (the latter from task cache).
- Widget: corrected weekday-label bitmap color to use theme `onSurfaceVariant`
  instead of hard-coded white. Added a bitmap color assertion to
  `WidgetTest`. Full app JVM suite, lint and Android-test compilation passed;
  the new Android bitmap assertion was compiled, not executed. No emulator
  screenshot or physical-device result is claimed; the owner Galaxy A55 check
  remains pending.
- Guided tour: reviewed six topics, persistence-before-navigation, resume,
  replay, responsive action layout, accessibility semantics and EN/CZ parity.
  No demonstrable defect was found and no code changed. JVM progress tests ran
  in the full app suite; UI instrumentation compiled only. Actual process-death,
  TalkBack and device journey acceptance remain outstanding.
- Verification: INTEGRATION PASS on exact dirty source SHA-256
  `9c7cc47c263abd75b01d66af9e966f0c2016016589f847d0fcc0a69a313cd203`,
  audit row `2026-11-14T21:33:54Z`, commit `a908c96-dirty`: full app JVM suite,
  lint and Android-test compilation. `:activity-domain:jvmTest` PASS from
  Gradle cache. Catalogue generator: 7/7 tests; Python byte-compilation,
  localization parity (1315 resources), UI literal check (8 audited legacy
  literals), and `git diff --check` passed. One sandbox Gradle preflight failed
  before build startup (wildcard IP); approved retry passed.
- At the time of that earlier checkpoint, Stage 2 remained **in progress**. No source snapshot/archive/key was fetched
  in this batch, no production catalogue was generated/imported, and the
  signing inquiry remains prepared but unsent. No Stage 3 or Git integration
  action occurred.

Exactly three major stages (owner-approved structure). This file is the single
local progress record; it is updated continuously as work progresses, never only
at the end. GitHub Issue/Project synchronization is recorded in the checkpoint
comments when executed; if GitHub is unavailable, that is reported here as
pending. Do not mark a task complete before its verification actually passed.

Review basis: Stage 1 reviewed tag `v0.14.0` (`b507aae`) plus post-tag docs
commit `a908c96` (`origin/main`). Implementation branch: `feature/gould-0.15`
(worktree `.worktrees/gould-0.15`), created from `origin/main` @ `a908c96`.

## Stage 1 — Complete Review & Planning: COMPLETED

- Full read-only review of Gould 0.14.0 across architecture/DI, data layer and
  backups, UI/navigation/localization, practice & activity core, tests/CI, and
  documentation/promotion readiness, consolidated into the report
  "Gould 0.15 — Complete Review & Implementation Plan" (delivered in the
  Stage 1 session; six parallel review agents, all findings cited with file
  references and verified/assumption labels).
- Documented compatibility requirements (Room additive-only, backup formats
  1–7, minSdk 30 notice) and 29 behavior invariants that must not regress.
- Onboarding architecture proposed and approved: anchor registry + overlay host
  at the existing dialog slot, additive `LocalAppState` migration, backup
  allow-list update, six tour topics, CZ/EN.
- Owner decisions received (binding, see [DECISIONS.md](DECISIONS.md)):
  streak success = BOTH sessions AND minutes with one canonical definition;
  admin systems = EVALUATE only; planned licenses are GPL-3.0-or-later for
  application code and CC-BY-SA-4.0 for documentation/original prose. This is
  planning only: ownership/provenance verification and separate application
  authorization are required before either license is applied or published.

## Stage 2 — Full Implementation: COMPLETE (authorized 2026-11-14)

Scope: one coherent implementation stage (internal batches A–E below; these are
work organization, not separate stages).

### Batch A — Foundation & verification infrastructure

- [x] A1. Create this checklist, `VERIFICATION-MATRIX.md`, `DECISIONS.md`.
- [x] A2. Point `scripts/verify-audit.py` at a Gould 0.15 audit file
        (`docs/planning/gould-0.15/verification-audit.jsonl`), preserving older
        audit trails untouched. Default is version-scoped; `GOULD_AUDIT_FILE`
        permits explicit alternate audits. First FAST attempt is recorded.
- [x] A3. Repair `scripts/check_backup_fixtures.py`: apply all migrations to
        the current schema (today 13), including studio/Activity DDL; fix the
        stale "→ 10 DDL" message. **Passed:** all three fixtures migrated to
        schema 13; Room-export shape and retained portable fields matched.
- [x] A4. Repair `scripts/check_history_paging.py`: build DDL from the highest
        exported schema (not 8.json). **Passed:** paging checker.
- [x] A5. Guard the two unguarded startup steps in `OpusApplication.onCreate`
        (`restoreDesiredLocale`, `L.refresh`) consistently with their
        neighbors.
- [x] A6. Extract a shared `Routes` constants object; fix the `DISCOVER`
        tab-highlight case mismatch in `AppNavigation.selectedTab`. FAST PASS:
        `compileDebugKotlin`, `ProfileMaintenanceTest`, and static checks.
- [x] A7. CI: add Gradle caching to `quality.yml` without weakening the
        mandatory command; keep required checks intact. `setup-java` uses its
        built-in Gradle cache; the mandatory command is unchanged.
- [x] A8. Classify emulator workflow as optional supplemental CI, not an
        acceptance prerequisite. Owner acceptance is on a real device; no
        emulator/AVD setup or dispatch is required. `stage5-emulator.yml`
        remains available for optional historical native CI evidence.

### Batch B — Activity, streak and data correctness (owner BOTH decision)

- [x] B1. Canonical weekly-success definition in `:activity-domain`
        (sessions AND minutes), used by: Activity/weekly progress UI, current
        and longest weekly streak, week close, Freeze consumption, weekly
        rewards, achievements, Profile statistics. No screen may interpret a
        successful week differently.
- [x] B2. Replace sessions-only "goal met" displays/streak bonuses with the
        canonical definition (`ActivityRepository.visibleStreak`/`currentMet`,
        `ActivityScreen` goal-met label) + tests for the mixed case
        (sessions met, minutes missed).
- [x] B3. Make `ActivityRepository.snapshot()` genuinely read-only; move
        reward evaluation/granting to one serialized write path; cancel or
        replace overlapping refreshes; prevent duplicate grants and races.
        Read-only snapshot and serialized/idempotent refresh regression are
        implemented; host suite passed and androidTest compiled. Native
        instrumentation was not executed; see owner device checklist.
- [x] B4. Widget invalidation: include `manual_activity_entries` and
        `activity_*` tables in `WidgetRefresh` invalidation; test. Table
        coverage regression added; FAST passed.
- [x] B5. Unify displayed streak sources (Activity snapshot canonical; fallback
        computes the same rules) — Profile/Home must not show a different
        number. Daily fallback includes manual activity days; regression test
        and host JVM suite passed.
- [x] B6. Review appearance persistence duplication (`AppearanceStore` ↔
        `PortablePreferences`); consolidate the key list/encoding into one
        shared definition with round-trip tests; focused persistence test and
        FAST passed.
- [x] B7. Preserve practice/recording invariants (no changes to
        `PracticeCoordinator` credit logic, completion idempotency, mic
        safeguards) — verify by focused tests after B1–B6. No coordinator,
        credit, microphone-startup or completion logic changed; FAST passed.

### Batch C — Architecture simplification (AppContainer)

- [x] C1. Introduce a lightweight `AppContainer` composition root on
        `OpusApplication`, preserving lazy lifetimes and startup ordering.
        Core persistence, identity, media/appearance, Activity, practice,
        recording and catalogue repositories retain lazy creation.
- [x] C2. Migrate `as OpusApplication` cast sites incrementally (MainActivity,
        services, receivers, ViewModels, widgets, stores). All main-source
        call sites now use the `Context.appContainer` boundary; compatibility
        getters retain remaining lazy service factories during incremental
        graph ownership migration. FAST passed after the migration.
- [x] C3. Remove/replace the public `@VisibleForTesting` override vars with
        container-scoped `testHooks`; existing instrumentation references migrated.
- [x] C4. Consolidate duplicated UI components and responsive breakpoints where
        justified (3 back affordances, 3 section-title helpers, 2 weekly
        charts, 3 stat tiles, ad-hoc breakpoints) — behavior-preserving,
        guarded by the UI regression matrix. Shared RTL-aware back affordance
        consolidated; candidate review and rationale: `C4-UI-CONSOLIDATION.md`.
- [x] C5. Localization hardening: no new hard-coded user-facing strings; fix
        `Heading` title/subtitle localization gap; add a static guard for raw
        `Text("…")` literals. Static guard is added, headings audited to keep
        localized UI copy and preserve original/user text, and FAST passed.

### Batch D — Interactive onboarding & guided tour

- [x] D1. Additive Room migration 12→13 for tour state on `local_app_state`
        (`tourVersion`, `tourCompleted`, `tourSkipped`, `tourSeenSteps`),
        schema 13.json export, backup format bump decision + allow-list/
        validation update, seeded-migration tests. Schema 13 exported;
        host migration/backup fixtures pass through v13; seeded migration and
        v8 round-trip instrumentation tests added and androidTest compiled.
        Native seeded migration was not executed; owner device checklist covers
        compatible signed-upgrade acceptance.
- [x] D2. Tour infrastructure: `TourAnchorRegistry` (composition local +
        `onGloballyPositioned`), `TourViewModel` (SavedStateHandle + Room-backed
        progress survives recreation), `GuidedTourHost` overlay in the existing
        `OpusApp` content slot, non-modal scrim, TalkBack heading/live-region
        semantics, no added motion, scrollable large-font content.
- [x] D3. Six topics: Home, Activity, Streaks, Profile, Collection (badges and
        cosmetics), Settings — short localized explanations (EN/CZ), contextual
        rule hints (Freeze, Beta Week, Monday-effective goals, metered consent,
        mic recovery), Tier-1 interactive demonstrations via existing
        navigation (`selectTopLevel`/`navigate` only — no second NavHost). EN/CZ
        copy includes BOTH goals, Freeze/Beta Week, Monday boundary, metered
        consent and microphone recovery.
- [x] D4. Skip, resume-after-interruption (rotation + process death), replay
        from Settings → About; subsume the legacy 3-page `OnboardingScreen`
        so users never see two first-run experiences. Implementation is in
        place and the legacy screen was removed; runtime journey is listed for
        owner device verification.
- [x] D5. Tests: JVM tour-state tests; Compose instrumentation for navigation
        between topics, skip/resume/replay, process-death resume; localization
        parity for all new strings. JVM progress tests and static localization
        checks passed; Android instrumentation sources compiled but were not
        executed and are explicitly classified that way.

### Batch E — Documentation & public presentation readiness

- [x] E1. Reconcile contradictory docs: 0.14 checklist publish box + HANDOFF
        "0.14 boundary" section vs actual publication; 0.12 classification
        conflict (CHANGELOG "Pre-release" vs release doc "Stable"); stale
        `docs/wiki/Verification.md` headline; `quality.yml` display name.
- [x] E2. Privacy: refresh `docs/PRIVACY.md` **and** bundled
        `app/src/main/assets/legal/privacy.md` for Activity/manual entries/
        cosmetics/backup format 7/tour state. Activity/cosmetics/format 7 are
        updated in both notices for activity/manual entries, cosmetics,
        format 8, audio exclusion and tour-state persistence.
- [x] E3. README refresh + sideload-safety page ("Is it safe to sideload?");
        installation/compatibility wording incl. minSdk 30.
- [x] E4. `CONTRIBUTING.md` + bug-report issue template (app version, APK
        SHA-256, device, locale, theme). Planned license terms are recorded in
        `DECISIONS.md`; no license files or metadata are applied before rights
        verification and separate owner authorization.
- [x] E5. Quarantine the five stray `docs/Screenshot_*.png` owner-device
        screenshots: preserve a private local backup outside the repository,
        remove from the tree, record evidence. Byte hashes and private backup
        location are recorded in `docs/maintenance/LEGACY-CLEANUP.md`.
- [x] E6. Prepare owner physical-device acceptance and visual review. The
        comprehensive Galaxy A55 checklist is `DEVICE-TEST-CHECKLIST.md`;
        screenshots/demo are not claimed and remain owner-run evidence.
- [x] E7. Wiki consistency check (`docs/wiki/` page set vs policy list); note
        public Wiki sync status truthfully. Required local pages are present;
        public Wiki sync remains unverified because GitHub authentication is
        invalid.
- [x] E8. Admin evaluation report (`ADMIN-EVALUATION.md`) — evidence-based
        disposition; both systems/BFF artifacts retained by owner decision,
        operational status explicitly unverified.
- [ ] E9. GitHub Issue (living checklist) + Project status checkpoints at
        verified milestones; branch push checkpoints (no PR, no merge without
        owner approval). Local checklist/checkpoint are current; remote sync and
        branch push remain pending because the configured GitHub token was
        invalid. This is remote administrative synchronization, not an
        implementation blocker; no sync is claimed.

### Stage 2 verification (executed results — updated as runs happen)

- [x] FAST tier: PASS 2026-11-14 12:46:21 UTC (1 s; compile,
      JVM unit suite, localization/migration/backup/history/UI-literal checks).
- [x] INTEGRATION host tier: PASS 2026-11-14 12:46:29 UTC (<1 s reported;
      full JVM, lint, androidTest compile). Tasks reused valid Gradle cache;
      instrumentation did not execute.
- [x] Final source-freeze host gate: `scripts/verify-release.sh` PASS
      2026-11-14 12:52:34 UTC (8 s; exact policy Gradle command and all six
      mandatory policy checks; 39 tasks executed and 46 from valid Gradle
      cache). It built the host-gate debug APK and compiled Android tests; no
      instrumentation was executed. Audit identity: `a908c96-dirty`, source
      SHA-256 `df25621711d8f40b9919f22c3fc1a7a6e52da641920752cb975ae6b52a97906b`.
- [x] Release-variant testing APK: clean `:app:assembleRelease` plus
      `:app:lintRelease` using the preserved production signing init script
      passed on 2026-11-14 (1m51s). APK metadata, non-debuggable flag, v2
      signature/trusted fingerprint, ZIP integrity and SHA-256 are recorded in
      `CHECKPOINT.md`.
- An initial FAST retry at 12:46:09 UTC could not start Gradle because the
  sandbox could not determine a wildcard IP; the escalation retry passed. The
  failed preflight remains in the audit and is not a test failure.
- Not run: owner installation/in-place upgrade, Stage 3 release-specific
  security and acceptance gates, merge or publication. The signed
  release-variant APK is for owner testing only.

### Stage 2 completion status — COMPLETE

This is the historical 2026-11-14 completion snapshot; its 426-test count,
stale APK, “admin systems remain preserved” statement, and no-archive statement
are superseded by the 2026-11-15 current record at the top of this checklist
and in `CHECKPOINT.md`. Current results are 325 app JVM tests and 13 separate
activity-domain JVM tests, both with zero failures/errors/skips; Release APK,
signer and static-check results are recorded in the current checkpoint.

### Owner master implementation directive — remaining Stage 2 work

This is one Stage 2 continuation organized into the owner's two permitted work
blocks, not additional stages.

#### Work Block 1 — Catalogue and backend

- [x] Prepare an owner-review-only MetaBrainz dump-signing inquiry with the
      exact snapshot/hash and questions about fingerprint, key status, SHA-1,
      trusted key distribution, replacement procedure and signature format.
      Destination proposed: `support@metabrainz.org`, the official direct staff
      contact. Draft is in `CATALOGUE-PROVENANCE-POLICY.md`; it has not been
      sent and requires separate owner approval.
- [x] Select and document the exact MusicBrainz Core snapshot authorized for
      inspection: `20261114-002230`, official index publication timestamp
      2026-11-14 05:10 UTC, archive listing timestamp 01:47 UTC, expected
      `mbdump.tar.bz2` SHA-256
      `da4f1afcbca8ca54eb6362ae45173543f079e8491d097a376d060551d1a03335`.
      Downloaded only checksum/signature metadata to `/tmp`; attempted GPG
      verification failed because the signing public key could not be obtained
      and authenticated in this environment. Per the mandatory gate, did not
      download or process the archive. Zero records inspected; no dataset
      metrics or examples are available. Exact next requirement: owner supplies
      an independently authenticated key matching
      `D5E63B4BDCCE195642948684B8FC2375C777580F`, or authorizes a usable trusted
      key-distribution path. See checkpoint and provenance policy.
- [x] Investigate key fingerprint and MBS-14311 before any archive download.
      Confirmed ticket status is In Progress / Unresolved (updated 2026-11-08),
      and its reporter-provided full fingerprint/subkey and Sequoia SHA-1
      rejection details; official repository instructions still show only
      short key ID `C777580F`. Parsed held detached signatures as RSA v4 with
      SHA-512 digest under GnuPG 2.4.9, but did not verify them because the key
      is unavailable. Independent full-fingerprint confirmation and normal-
      policy GPG verification remain unresolved. Next: owner provides a
      maintainer-confirmed key source and separate full-fingerprint evidence;
      if GPG rejects the key under normal policy, request a replacement signing
      key/procedure. GitHub INSTALL history fetch returned a cache miss, so no
      historical-change claim is made. No archive or catalogue data was
      downloaded/processed.
- [ ] External prerequisite for production catalogue data: complete a
      record-level provenance/licensing review and approve a real pinned input
      set. No approved real input or record-review policy exists. This is not a
      blocker to completion of the generated-package infrastructure. Do not
      generate production data until the signing/source evidence and separate
      owner authorization are available.
- [x] Inspect repository history/assets and verify candidate dataset license,
      attribution, and snapshot evidence from upstream documentation. Prepare
      a generator-compatible per-record/per-file policy proposal in
      `CATALOGUE-PROVENANCE-POLICY.md`. Owner selected MusicBrainz Core and
      snapshot `20261114-002230` for inspection. Its signature key remains
      unauthenticated, so no archive was acquired or records reviewed; separate
      owner approval is still required for production generation/import.
- [x] Implement deterministic schema-1 catalogue generation using the
      exported Room schema and Bundle manifest format 1. Five Python fixture
      tests pass, including byte-for-byte repeat generation, FTS, integrity,
      source SHA pinning, per-record/per-file rights, and synthetic-input gate.
- [x] Connect generated package import to `BundleStore` and
      `LocalCatalogueStore`: validate READY COLLECTION/schema 1, manifest file,
      blob hash/size, Room schema/integrity/foreign keys and approved title
      evidence; merge journal-owned identities and publish through the existing
      atomic generation pointer. Kotlin compilation passed; runtime Android
      installation has not been executed.
- [x] Review publication workflows and add a static policy guard to CI.
      Existing push/PR workflows have read-only `contents` permissions, do not
      reference secrets, and contain no release/publication action or command;
      release signing/publication remains a separate owner-approved process.
`check_publication_workflows.py` and nine policy tests pass. No workflow
      can publish an APK or access production signing credentials.
- [x] Implement local community-draft export/recovery while preserving draft
      identity, incomplete edits, pending request bodies, credentials, and
      existing storage. Never overwrite conflicts or alter backup formats 1–8.
      Focused tests pass; localized behavior/limits are documented in
      `COMMUNITY-RECOVERY-POLICY.md` and `COMMUNITY-DRAFT-RECOVERY.md`.
- [x] Retire the obsolete Android legacy admin system while preserving the
      canonical admin client/repository and required BFF/contract components.
      The 2026-11-14 preserve-both decision above is historical and superseded
      by the 2026-11-15 owner directive; no production networking was enabled.
      See the current checkpoint and `scripts/check_admin_retirement.py`.
- [x] Preserve Google identity and personal catalogue bridge behavior. No
      identity behavior changed; JVM regression suite passed and relevant
      Android instrumentation compiled. Device/runtime authentication checks
      remain owner acceptance and were not executed in this host run.
- [x] Preserve Room schema 1 and the existing package format; the generator
      reads the existing schema export and emits Bundle manifest format 1.
      `check_migrations.py` and `check_backup_fixtures.py` pass; backup code and
      formats 1–8 were not changed in this work block.

#### Work Block 2 — UI consistency and integration

- [x] Inspect all five owner screenshots read-only and map the observed
      layouts/fixes in `CHECKPOINT.md`. Existing Activity/Home alignment and
      the horizontally scrolling badge cards matched their intended alignment;
      no broad redesign was justified. Fix the actual tour action wrap and
      stretched tall-widget heatmap cells. Preserve intentional list alignment.
- [x] Equalize Discover recent-card title slots using the tallest measured
      visible title (current locale/font scale), two-line ellipsis, and the
      existing detail tap for full content.
- [x] Make the weekly widget change from vertical summary to horizontal
      summary-plus-chart when both width and height permit it; larger font
      scales retain the compact layout.
- [x] Bound weekly heatmap cell aspect ratio so tall launcher resizing does
      not stretch day bars vertically; verify representative narrow, wide,
      short and tall dimensions in the host projection test.
- [x] Repair the stretched widget chart: the weighted `ImageView` used
      `fitXY` to expand a fixed-height bitmap, stretching bars and weekday text
      and leaving a large gap. The image now wraps its size-matched bitmap;
      projection tests cover chart height, day-label width and visible labels
      at enlarged font scales. Wide chart bitmap width follows its weighted
      column.
- [x] Stack guided-tour actions at narrow widths / enlarged font scale so
      localized action labels remain complete and readable.
- [x] FAST `WidgetProjectionTest` and static checks pass on the current dirty
      source. INTEGRATION full JVM suite, lint and Android-test compilation
      pass; instrumentation was not executed. A fresh signed Release testing
      APK is recorded in the current checkpoint. Exact commands/results are in
      the audit and checkpoint.
- [x] Attempt widget inspection in authorized API 37/API 36 emulators; both
      exited with code 139 before ADB connected. No rendered result or visual
      success is claimed. Owner Galaxy A55 acceptance remains pending.
- [x] Run the Stage 2 INTEGRATION gate after all implementation changes; it
      passed as recorded above. Also built and verified a signed Release-variant
      owner-testing APK and passed `lintRelease`. The complete publication /
      release-acceptance gate remains separate; no publication occurred.
- [x] Update the Galaxy A55 owner checklist with the current verified APK,
      guided-tour narrow-layout check and adaptive widget-size checks. Owner
      runs it; no emulator or physical-device test is an agent prerequisite.

## Stage 3 — Testing, Release & Promotion: IN PROGRESS (bounded catalogue repair verification)

The owner explicitly requested the catalogue repair, mandatory host verification
and a local signed testing APK. That authorization covers only this bounded
verification and artifact build. Runtime acceptance remains incomplete. Final
public-release acceptance, owner device results, release notes, Wiki/Project
sync, merge, publication and promotion are outside this task and were not done.
Emulator availability is not an acceptance prerequisite.

## Library tabs redesign — host verification complete, runtime acceptance pending

- [x] Split Library into persistent Repertoire and Catalogue tabs without a new
      NavHost. Repertoire renders only `LibraryState.pieces`; Catalogue reuses
      the existing Discover repository and search UI. The embedded public view
      hides Discover's personal “Available offline” shelf.
- [x] Keep personal sorting, edit/delete and manual-add actions; Catalogue detail
      continues to provide add/open-existing behavior using the existing
      MBID-based `CatalogueRepertoire` idempotency path. Delete remains a
      personal journal operation; no schema, backup or catalogue architecture
      changes were made.
- [x] Add localized Repertoire empty-state copy and Catalogue switch action.
      Add Compose regression source for Repertoire/Catalogue data separation,
      offline composer/work search, empty/loading/error/retry states. Add data
      regression source for idempotent add and public-record survival after
      deleting the personal entry.
- [x] Final `scripts/verify-release.sh` passed 2026-11-15 18:07:42 UTC (49 s):
      327 JVM tests, 0 failures/errors/skips; debug lint; AndroidTest compilation;
      seven policy checks. Android instrumentation was compiled only, not run.
- [x] Final `:app:assembleRelease :app:lintRelease` passed after the host gate.
      APK `app/build/outputs/apk/release/app-release.apk`, 97,894,273 bytes,
      SHA-256 `1399fd74cf90a562c766aeb1dc0a16ed28fa46a8f8c0abd209d7b582e0738905`,
      package `com.opus.piano`, version `0.15.0-dev` / code 17, non-debuggable,
      signer SHA-256
      `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.
      Curated APK and compatible-signing checks passed.
- [ ] Owner visual/interaction acceptance on Galaxy A55 remains pending. The
      previous catalogue runtime failure investigation is still unresolved;
      this UI redesign and host verification do not prove that Discover search
      or catalogue initialization now works on device.

## Catalogue performance investigation and full-width tabs — IN PROGRESS

**Purpose:** remove repeat catalogue setup work and make the Repertoire / Catalogue
switcher equal-width and easier to use, while producing safe runtime timings for
the owner's later Galaxy A55 run.

**Scope:** `DiscoverRepository`, `LocalCatalogueStore`, bundled-catalogue setup,
Discover/Library result-frame reporting, Library tabs, and Gould 0.15 Android
regression instrumentation. No schema, FTS/index, backup, provenance, or online
rate-limit change.

- [x] Measure decompression, SHA-256, SQLite integrity/foreign-key checks, FTS
      availability, representative searches and query plans against the actual
      packaged catalogue on the Linux host.
- [x] Confirm source-level repeated setup: each Discover query re-entered
      local initialization and replayed legacy catalogue synchronization.
- [x] Reuse successful local initialization and legacy preparation across
      searches; preserve incomplete-batch and error retry behavior.
- [x] Add non-sensitive structured phase timings for install, database open,
      validation, first/repeated search, online provider calls, filter/history
      loading and visible Discover/Library result frames.
- [x] Change the tab switcher to full-width equal tabs with centered labels and
      a 64 dp minimum height; add an instrumented bounds/touch-height assertion.
- [x] Add actual packaged-catalogue instrumentation timings and assert local
      preparation is reused; retain existing cancellation, online cache,
      bounded paging and MusicBrainz rate-policy coverage.
- [x] Final `scripts/verify-release.sh` PASS at `2026-11-15T18:50:42Z` in 110 s:
      327 JVM tests, 0 failures/errors/skips; lint, AndroidTest compilation and
      all seven policy checks passed.
- [ ] Execute AndroidTest benchmarks and tab layout checks on an authorized
      Android target; they compiled only in this environment.
- [ ] Capture first-install, repeated-search, online and render timings on the
      Samsung Galaxy A55 and compare against a same-device pre-change baseline.

**Host timings:** payload decompression 421.7 ms, SHA-256 77.6 ms, integrity
check 893.2 ms, foreign-key check 174.5 ms; typed warm query repeats were
0.078–0.182 ms across Chopin/Bach/Mozart/Beethoven/Ballade. See
`CATALOGUE-PERFORMANCE-INVESTIGATION.md` and preserved JSON under
`.gould-015-diagnostics/catalogue-performance-20261115/`. These are host-only
measurements, not device latency. **Status remains incomplete** pending Galaxy
A55 runtime data. The mandatory verification gate produced only its debug APK;
no separate signed, non-debuggable testing APK was built for this investigation.

Owner-requested signed Release testing APK was built afterward without cleaning
on 2026-11-15. `:app:assembleRelease` passed using the established signing init
script; APK metadata, non-debuggable status, v2 signature, expected signer and
ZIP integrity passed. Exact command, artifact digest/size and warnings are in
`CHECKPOINT.md`. This is artifact verification only; no Galaxy A55 runtime or
performance acceptance was performed.

### Catalogue performance audit follow-up — IN PROGRESS

- [x] Audit repository, Compose, local query, provider, paging, and navigation
      paths; distinguish prior host measurements from device evidence.
- [x] Add opaque search-session correlation and outcome/result-count timing for
      local results, candidate state, online debounce/provider response, DB
      open, filters/history/cache support work, category first frame, and
      Discover/Library first result frame.
- [x] Add JVM context-field coverage and Android regression assertion that local
      and online emissions share a search-session ID.
- [x] Final-source FAST passed; INTEGRATION passed (329 JVM tests, lint,
      AndroidTest compilation); Release Kotlin compilation and lint passed.
- [x] Build and verify a signed, non-debuggable diagnostic Release APK for the
      later owner run; preserve previous APK artifacts.
- [ ] Capture baseline and instrumented timings on the Samsung Galaxy A55.
- [ ] Rank bottlenecks by measured device impact and implement only a justified
      Kotlin change; remeasure under the same conditions.
- [ ] Execute Android instrumentation on an authorized target; compilation is
      not execution.
- [ ] Complete visual review of result type hierarchy/category wording with the
      owner before making broader UI changes.

Status remains **IN PROGRESS**. The diagnostic APK is not device acceptance and
no performance gain is claimed. The mandatory clean RELEASE tier was
intentionally not run because the owner forbids destructive cleanup. See
`CATALOGUE-PERFORMANCE-INVESTIGATION.md` for the exact APK identity, results,
findings, Rust assessment, and owner-run device procedure.

### Library, artist pages, and active-provider remediation — 2026-11-16

- [x] Remove the Library subtitle and reclaim vertical space while retaining
      equal full-width, 64 dp Repertoire/Catalogue tabs and existing responsive
      touch-bound assertions.
- [x] Add per-artist public catalogue work search, clear/reset, localized
      deterministic A–Z/Z–A sorting, and available genre/work-type filtering;
      add JVM coverage for sort/type projection and update the subtitle UI
      assertion.
- [x] Restrict active Discover adapters/candidates to MusicBrainz Core; reject
      other candidate providers without removing legacy adapters, imported
      rows, legacy synchronization, provenance, or the separate CatalogScreen.
- [x] Reuse the first offline page/candidates for the online search stage;
      decouple filter/history loading from search completion and add explicit
      privacy-safe completion/seed-reuse events.
- [x] Final-source `:app:testDebugUnitTest`: 331 passed, zero failures/errors/
      skipped; AndroidTest APK compiled; `lintDebug`, `lintRelease`, and
      `assembleRelease` passed. `git diff --check` and
      `scripts/check_musicbrainz_catalogue.py` passed. No clean task ran.
- [x] Read-only host query comparison against preserved catalogue DB (All /
      Works / Artists, Chopin): repeat medians 0.575 / 0.571 / 0.361 ms; these
      exclude Android dispatch, mapping, providers, and UI.
- [ ] Execute the responsive-tab/subtitle, artist-page, cancellation, and
      preservation instrumentation on an authorized emulator; current
      instrumentation evidence is compilation only.
- [ ] No new Galaxy A55 result was collected. The owner's existing 2026-11-15
      exploratory report (empty search / Library skeleton, before this patch)
      remains the only attributed device evidence; it was not repeated. Host
      measurements are not A55 evidence.
- [ ] Migrate the long `DiscoverEditorialAcceptanceTest` Open Opus-backed online
      fixture to MusicBrainz/local catalogue fixtures before executing it; it
      currently compiles but reflects the retired active-provider policy.
- [x] Build output is isolated and unsigned; the prior signed diagnostic APK
      hash was rechecked unchanged. No publication or Git mutation occurred.

Status remains **IN PROGRESS** pending the runtime/owner checks above.

### Gould 0.15.0 release gate — owner-authorized publication of the owner-tested APK, 2026-11-16

- [x] Record owner approval for the exact verified MusicBrainz Core snapshot,
      GPL-3.0-or-later program source, and CC-BY-SA-4.0 original documentation.
- [x] Add canonical license texts and document third-party/data boundaries.
- [x] Run fresh app/activity JVM suites: 338 and 13 tests passed; lintDebug,
      lintRelease, Android-test APK compilation, Release assembly, UI literal,
      MusicBrainz catalogue, curated APK payload, and final diff checks passed.
- [x] Verify the protected owner-tested APK's package/version, SHA-256, v2
      signature/certificate, and bundled catalogue payload.
- [x] Build a separate final signed Release APK; verify package/version,
      non-debuggable flag, signer and payload.
- [x] Publish the **owner-tested** APK (`.gould-015-cancellation-build/.../
      app-release.apk`, SHA-256 `f3df006e…ade8ea`) — the exact artifact with
      recorded Galaxy A55 owner acceptance. The fresh APK
      `9dfce12a1e793c21af970526bb0f1118a5ab9bd5f31e45b20bdca8a17318a054`
      is entry-identical but was **not** owner-tested and is **not**
      published; the owner retest of that fresh artifact is therefore
      superseded and not required for this release.
- [ ] **L661 — Resolve whether packaged `googleid:1.1.1` and
      `play-services-auth:21.1.1` under the Android SDK License can be
      distributed with the GPL-covered application; no GPL-compatible
      grant/system-library exception is yet established.** This item stays
      **unchecked**: the audit verdict is *LEGAL REVIEW REQUIRED* and remains
      unresolved. On 2026-11-16 the owner explicitly authorized completing
      and publishing Gould 0.15 as a GitHub prerelease despite the
      unresolved questions, while instructing that no permission be invented
      and the questions not be represented as resolved. That authorization
      is a release decision only; it does not resolve, soften or reverse the
      audit findings (see `GOOGLE-DEPENDENCY-LICENSING-AUDIT.md` §12 and
      `docs/LICENSES.md`).
- [x] Perform the Git integration and publication sequence with a **regular
      merge commit** (explicitly no squash, no rebase), the `v0.15.0`
      annotated tag, and the GitHub Prerelease — per the owner's 2026-11-16
      instruction, which supersedes the earlier "squash-merge" wording
      above. Google Sign-In code is unchanged in 0.15; its replacement
      investigation is deferred to Gould 0.16.
      **Executed 2026-11-16:** PR
      [#53](https://github.com/stokonn/Gould/pull/53) merged with regular
      merge commit `06282c5aad4887b472373e925e6ead01e24c4cba` (exactly two
      parents; no squash/rebase), all three PR CI workflows passed;
      annotated tag `v0.15.0` → the merge commit; prerelease
      [Gould 0.15.0](https://github.com/stokonn/Gould/releases/tag/v0.15.0)
      published with the single asset `gould-0.15.0.apk` (97,931,121 bytes),
      downloaded back byte-identical, SHA-256
      `f3df006e36aa55fb940b5a938cc36737156c3ff77a5c960a9c5da10b53ade8ea`,
      signer `53eface4…d59e`. Full record in `CHECKPOINT.md`.

Status: **RELEASED as a GitHub prerelease under explicit owner
authorization** (2026-11-16). The two earlier gates are disposed of as
follows: the APK-retest gate is superseded by publishing the already
owner-tested artifact; the dependency-license gate is **not resolved** — it
is transparently documented and the owner explicitly authorized release
despite it. Android instrumentation was compiled but not executed. Exact
results, commands and the publication record are in `RELEASE-VERIFICATION.md`
and `CHECKPOINT.md`.
