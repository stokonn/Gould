# Goldberg 0.11 — stage checklist (permanent, living document)

Canonical policy: [AGENTS.md](../../../AGENTS.md) (§ "Stage checklist documentation").
This checklist is synchronized continuously as work progresses. A stage is marked
**complete** only after its verification section records checks that were actually
executed and passed. Checkboxes without verified evidence must stay unchecked; checked
boxes cite their evidence below or in the linked stage ledger.

Status legend: ✅ complete (verified) · 🚧 in progress · ⬜ not started

---

## Stage 1 — Architecture & readiness planning

**Status: ✅ complete (documentation-only stage).** Branch `planning/goldberg-0.11-stage-1`,
baseline `556970ab7407ec6986e3678657adbd2168516468`; evidence:
[Stage 1 verification](VERIFICATION.md).

- **Purpose:** Define the 0.11 product architecture, stage boundaries, decision register
  (D1–D9) and acceptance gates from the approved product scope, without touching code.
- **Scope:** `docs/planning/goldberg-0.11/*` only. No application, Gradle, schema, CI,
  signing, cloud account or release change.

**Tasks**

- [x] Verify ancestry, canonical/maintenance policies and preserved owner work (RELEASE_NOTES.md untouched).
- [x] Write stage independence gates, decision register and handoff rules (STAGES.md).
- [x] Write architecture/data-model/contracts sketches and offline-cache proposal (OFFLINE-CACHE.md, DATA-MODEL.md).
- [x] Record cloud/backend/admin gating documents (CLOUD.md, COMMUNITY-ADMIN.md) as future-stage decisions.
- [x] Run baseline host gates and record exact results (no new emulator runs claimed).
- [x] Reuse existing GitHub Project/milestone/umbrella issue #31; planning-only Wiki link.

**Verification (executed 2026-10-18):** Gradle cached build BUILD SUCCESSFUL + explicit
`testDebugUnitTest --rerun` **75 tests / 0 failures**; lint **0 errors** (83 warnings);
`check_migrations`, `check_backup_fixtures`, `check_history_paging`, `check_localization`,
`check_agent_policy` all PASS; 10-doc link/fence check PASS. Instrumentation not executed
(documentation-only change set; explicitly not claimed).

---

## Stage 2 — Contracts, fixtures and measured readiness

**Status: ✅ complete (research-only stage).** Branch `planning/goldberg-0.11-stage-2`,
descends from Stage 1 `c3d5e03b9a4936d8db393df664af209a8853c767`; evidence:
[stage-2/README.md](stage-2/README.md), [stage-2/READINESS.md](stage-2/READINESS.md).

- **Purpose:** Turn the approved product scope into concrete, testable contracts: catalogue
  data model and physical separation, provider field/rights policy, identity matching,
  backup evolution, measured storage/performance budgets, and adversarial fixtures.
- **Scope:** `docs/planning/goldberg-0.11/stage-2/**` (SQL fixtures, Python research tools,
  JSON contracts). No application code, no schema export change, no deployment.

**Tasks**

- [x] Finalize logical/physical model, adapter/search/provenance/bundle envelopes (CONTRACTS.md, contracts.json).
- [x] Specify separate catalogue DB with durable personal references (DATABASE-BACKUP.md).
- [x] Build deterministic licensed fixtures + adversarial contract tests (fixtures/, verify_stage.py).
- [x] Run bounded provider investigation with overload stops; record coverage and failures (SOURCE-RESEARCH.md).
- [x] Measure query cost, memory/storage and bundle budgets (MEASUREMENTS.md).
- [x] Owner settled D1/D2/D3/D6 and D7/D9 direction (multi-genre works/arrangements, UUIDs + legacy IDs, separate catalogue DB, measured caps).
- [x] Record remaining blockers (D4/D5/D8 backend/operator/Admin) as later-stage gates.
- [x] Commit/push planning branch; update existing tracking (issue #31 evidence).

**Verification (executed 2026-10-18):** deterministic fixture/contract suite via
`stage-2/verify_stage.py` PASS; research/bench runs are recorded as measurements, not as
production capacity claims; no application build was altered (research-only artifacts).

---

## Stage 3A — Additive local persistence (journal schema9 + catalogue schema1)

**Status: ✅ complete (verified).** Branch `feature/goldberg-0.11-stage-3a`, baseline
Stage 2 `d6049add3e46399d8f48afcb2aaa03422ebe0017`, prerequisite CI
[39120954962](https://github.com/stokonn/Goldberg/actions/runs/39120954962) passed;
evidence: [stage-3a/VERIFICATION.md](stage-3a/VERIFICATION.md).

- **Purpose:** Add the safe persistence bridge: transactional personal schema 9 (UUID
  anchors, catalogue links) and an independent catalogue database (schema 1, FTS4, bounded
  DAOs) without destructive migrations or journal rewrite.
- **Scope:** `OpusDatabase` (additive schema 9 + migrations), new catalogue DB/DAO/store,
  personal↔catalogue bridge, lazy retryable catalogue reconstruction, tests.

**Tasks**

- [x] Transactional schema 9 bridge preserving all schema 8 tables/bytes.
- [x] Independent catalogue schema 1 with bounded DAOs, FTS4, typed graph/rights validation.
- [x] Lazy, retryable local reconstruction isolated from journal startup.
- [x] All historical Android migrations + journal regression + catalogue failure tests.
- [x] Seeded signed in-place 0.10.1 upgrade with original data/prefs comparison.
- [x] JVM/build/lint/host compatibility checks and targeted measurements.
- [x] Technical documentation + English Wiki sources.

**Verification (executed):** full historical migration matrix on Android and host fixtures;
seeded in-place upgrade PASS; JVM/build/lint gates PASS (see stage-3a/VERIFICATION.md for
exact counts); instrumentation executed on emulator only.

---

## Stage 3B — Portable backup v5 and safe recovery

**Status: ✅ complete (verified).** Branch `feature/goldberg-0.11-stage-3b`, descends from
verified Stage 3A `cb36535d8a733143213347dd2ce355d1bf81ae96`, prerequisite CI 39125571414
passed; evidence: [stage-3b/VERIFICATION.md](stage-3b/VERIFICATION.md),
[stage-3b/CHECKPOINT.md](stage-3b/CHECKPOINT.md).

- **Purpose:** Make new identities/settings/recovery portable: additive journal schema 10,
  bounded hash-validated portable format 5, unchanged format 1–4 readers, raw-recovery
  evidence and allowlisted preference replay.
- **Scope:** backup reader/writer, journal schema 10 (additive), recovery/draft flows,
  golden fixtures, crash/corruption/size/atomic-publication tests.

**Tasks**

- [x] Additive journal persistence for raw recovery evidence, completion evidence, preference replay.
- [x] Bounded/hash-validated portable v5; historical readers 1–4 keep their meaning.
- [x] Destination snapshots/drafts/archives and imported UUIDs preserved; paused recovery with fresh execution IDs.
- [x] Allowlisted preference replay with explicit pending/retry state.
- [x] Full/minimal golden fixtures + corruption/crash/size/atomic-publication tests.
- [x] Sequential API 26 + API 37 instrumentation, seeded upgrade, offline and process-death checks.
- [x] Implementation/verification/handoff documentation + GitHub/Wiki synchronization.

**Verification (executed):** build/JVM/lint/host checks PASS; sequential API 26/API 37
instrumentation PASS; seeded upgrade, offline and process-death (×4 boundaries) checks PASS
(see stage-3b/VERIFICATION.md for exact counts and commands).

---

## Stage 4 — Provider adapters and unified read/search

**Status: ✅ complete (verified).** Branch `feature/goldberg-0.11-stage-4`, baseline Stage 3B
`9ae8d33e113feba531cdfa8558f0505cd4205749`; verified head `99ea73a` (also the Stage 5
baseline). Evidence: [stage-4/CHECKLIST.md](stage-4/CHECKLIST.md),
[stage-4/PROGRESS.md](stage-4/PROGRESS.md), `docs/ai/VERIFICATION.md` (baseline section).

- **Purpose:** Ship the local-first catalogue experience: five independent provider
  adapters (Open Opus, MusicBrainz, Wikidata, Wikipedia, Commons) behind typed contracts,
  a unified Explore/Search UI, indexed local search with generation-bound paging, cached
  provider responses with rights/attribution, and safe work/arrangement linking.
- **Scope:** provider transport/adapters, catalogue store/search/paging, Explore/Search UI,
  catalogue cache/portraits storage admission, personal-library links, CZ/EN resources.

**Tasks**

- [x] 4A Independent adapters, bounded cancellable transport, host allowlist, rights fields, adversarial fixtures, live provider probes.
- [x] 4B Typed catalogue integration, hash-bound observations, graph/identity validation, deletion/rebuild regressions.
- [x] 4C Indexed local-first search, ranking/normalization, generation-bound cursor paging, cache budgets/coalescing, privacy/offline/performance.
- [x] 4D Explore/Search/detail UI with provenance, CZ/EN, accessibility, rendered emulator checks (0.10.1 design identity preserved; owner waived screenshot review for 0.10, documented).
- [x] 4E Explicit work/arrangement selection, duplicate prevention, durable links/snapshots, practice/recovery regressions.
- [x] 4F Host checks, JVM/lint/APK, executed API 26 + API 37 instrumentation, seeded trusted 0.10.1 upgrade, artifact identity, branch verification docs.

**Verification (executed 2026-10-20 re-verification at `99ea73a`, emulator `emulator-5592`
API 37, explicit serial):** `testDebugUnitTest` **97 tests / 0 failures**; `lintDebug`
**0 errors** (107 warnings); `assembleDebug` + `assembleDebugAndroidTest` BUILD SUCCESSFUL;
`connectedDebugAndroidTest` **143 tests / 0 failures / 2 skipped** (`assumeTrue`
external-fixture guards); host checks `check_migrations`, `check_backup_fixtures`,
`check_localization`, `check_history_paging`, `check_agent_policy` PASS; seeded in-place
upgrade `verify_emulator_upgrade.py` (0.10.1/schema8 → schema10) PASS with exact
persistent-table equality; real process death ×4 boundaries PASS; APK SHA-256
`ac1ad27a5c42ec8f30a306c522943575183e2f5f4d9e41baef8f394d333aa3f4`, signer
`53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`, `com.opus.piano 0.10.1/code 11`.

---

## Stage 5 — Offline bundles and Smart Cache 2.0

**Status: ✅ complete — owner-approved 2026-10-21.** Branch `feature/goldberg-0.11-stage-5`,
baseline Stage 4 verified head `99ea73a`; completion commit `736f6d3` (owner approval) on
head `32cf5d9`. Owner-approved decisions: D5.1 file-based manifests, D5.3 WorkManager
(owner-mandated), D7 storage caps + per-job metered consent, D5.6 test-only org.json
(see `docs/ai/DECISIONS.md`). Verified evidence: emulator regression CI run
[39557624554](https://github.com/stokonn/Goldberg/actions/runs/39557624554)
(177 instrumentation tests / 0 failures / 0 errors / 2 designed skips) and screenshot
CI run [39576173002](https://github.com/stokonn/Goldberg/actions/runs/39576173002)
(12/12 required screenshots captured and inspected). **Limitation preserved by owner
disposition:** real production-CDN bundle-transfer measurements remain a future
integration dependency (no bundle feed exists; must be re-measured when it ships —
no numbers fabricated). The authoritative Stage 5 completion record (full executed
ledger, evidence bundles under `stage-5/results/`, final verification detail) lives on
the Stage 5 branch; **this Stage 6 planning branch does not repeat it and Stage 5 is
not reopened here.** No release/merge/tag authorized.

- **Purpose:** Let users keep encyclopedia/media content available offline through
  explicitly pinned, rights-complete bundles: durable file-based manifests, a
  content-addressed store with mark-and-sweep, a WorkManager-backed background scheduler
  that never downloads over mobile data without explicit per-job consent, hash-verified
  transfers with hard byte caps, atomic publication that keeps the previous READY revision
  through interrupted refreshes, and full isolation from personal journal/recovery data.
- **Scope:** `data/offline/*` (model, codec, policy, state machine, store, job records,
  network monitor, transfer engine, runner, scheduler, worker), `OpusApplication` lazy
  wiring + startup reconcile, test-only Gradle additions (org.json, work-runtime,
  work-testing). **No** journal/database/backup/signing/package change; no UI redesign.

**Tasks**

- [x] 5.1 Durable model + strict JSON manifest codec with mandatory per-file rights (OfflineBundle.kt, BundleManifestCodec.kt) — 21/21 codec tests.
- [x] 5.2 Reviewed lifecycle state machine incl. process-restart mapping and READY-safe recovery paths (BundleStateMachine.kt) — 13/13 tests.
- [x] 5.3 D7 policy matrix: validated network, per-job consent, Data Saver, stop reasons, confirmation gates (BundlePolicy.kt, BundleNetwork.kt) — 11/11 tests.
- [x] 5.4 Content-addressed store + mark-and-sweep + staging + quarantine (BundleStore.kt, BundleStoreLogic.kt) — logic 10/10 JVM tests; file I/O needs instrumentation.
- [x] 5.5 Durable per-job state: allowance/confirmation/attempt/cancel records (BundleJob.kt, BundleJobStore.kt, AndroidBundleJobPreferences.kt) — 8/8 tests.
- [x] 5.6 Transfer engine: streaming with hard caps, per-64 KiB policy re-checks, hash verification, provider host allowlist (BundleTransferEngine.kt) — 14/14 tests.
- [x] 5.7 Pipeline runner: QUEUED→READY with durable PAUSED/WAITING/FAILED_RETRYABLE/QUARANTINED outcomes (BundleJobRunner.kt) — instrumentation tests written.
- [x] 5.8 WorkManager scheduler + worker: unique work per `bundleId:revision`, UNMETERED unless per-job consent, revoke/grant/cancel, startup reconciliation (BundleScheduler.kt, BundleDownloadWorker.kt).
- [x] 5.9 `OpusApplication` lazy wiring + startup reconcile + hermetic test seams.
- [x] 5.10 Execute `Stage5*Test` instrumentation on an explicitly identified emulator (store I/O, runner pipeline, real WorkManager execution/cancel/reconcile). — **COMPLETE (owner-approved 2026-10-21): CI run 39557624554 (attempt 6), 177 tests / 0 failures / 0 errors / 2 designed skips.**
- [x] 5.11 Emulator acceptance: Wi-Fi→mobile transition, Data Saver, consent revocation mid-stream, real process death at boundaries, low disk, seeded in-place upgrade from trusted 0.10.1, disk/network cost measurements. — **COMPLETE: every item has executed passing evidence (run 39557624554 + instrumentation); screenshots run 39576173002 GREEN 12/12 (Ivory/Nocturne × six screens, pair-by-pair inspected). Disk/network costs: executed byte accounting + real file I/O recorded; real-CDN transfer measurements owner-dispositioned as a future integration dependency (no bundle feed exists).**
- [x] 5.12 Full host gate re-run + `scripts/check_*.py` (migrations/backup/localization/paging/policy). — **COMPLETE: host gate BUILD SUCCESSFUL, unit tests 174/0/0/0, lint 0 errors / 110 pre-existing warnings; all 5 check scripts PASS.**
- [x] 5.13 Update `docs/ai/*` (STATE/VERIFICATION/HANDOFF/DECISIONS), reviewed commit, push to Stage 5 branch, verified evidence to GitHub Issue #31 + Project. No merge/tag/release/APK publication; Stage 6 not started. — **COMPLETE: docs synchronized, evidence bundles committed, Issue #31 checkpoints posted, only workflow files merged to main (PRs #35–#38); no application code merged, no tag/release/APK publication.**

**Verification (executed so far, 2026-10-20 sessions — HISTORICAL mid-progress log,
retained as audit trail):**

> **Reconciliation note (2026-10-21, Stage 6 planning branch):** this log was frozen
> mid-Stage-5 when this branch was cut (`bcd8894`). It is kept unchanged as historical
> evidence. The **authoritative final** Stage 5 verification record — including the
> green CI runs 39557624554 (177/0/0/2) and 39576173002 (12/12 screenshots), the
> root-cause triage of the earlier failed runs and the owner-approved completion
> (2026-10-21) — lives on `feature/goldberg-0.11-stage-5` (completion commit `736f6d3`,
> head `32cf5d9`) in its `docs/ai/*` files, `STAGE-CHECKLIST.md` and
> `docs/planning/goldberg-0.11/stage-5/results/`. Nothing here supersedes that record.

- Checkpoint #5 host gate (`clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug`) BUILD SUCCESSFUL; **146 tests / 0 failures**; lint **0 errors** (109 warnings).
- After 5.5–5.9 implementation: `compileDebugKotlin` BUILD SUCCESSFUL; `testDebugUnitTest` **174 tests / 0 failures** (28 new scheduler/engine/job tests).
- 2026-10-20 durable checkpoint: all Stage 5 work committed and pushed to the Stage 5 branch;
  permanent AGENTS.md rules (stage checklists, GitHub Issue checklists) added; STAGE-CHECKLIST.md created.
- 2026-10-20 instrumentation-fix session (uncommitted working tree, HEAD `54f2b45`):
  `Stage5BundleJobRunnerTest` compile issues fixed (6 errors, no assertion weakened) →
  **EXECUTED** `:app:assembleDebugAndroidTest` **BUILD SUCCESSFUL** (4m54s, 0 errors, 7 new
  nullable-`listFiles()` warnings); **EXECUTED** `:app:testDebugUnitTest` but all tasks
  UP-TO-DATE → 0 tests re-executed (last real counts remain the prior session's 174/0).
  `Stage5BundleWorkerTest` (12 tests, real WorkManager test infra) written; **EXECUTED**
  androidTest compile after adding it → **FAILED, exactly 1 error** (`Stage5BundleWorkerTest.kt:324`,
  nullable `TestDriver?` — one-line fix pending). Two static findings (F1 rights-quarantine
  unreachable, F2 corrupt-blob repair dead-end) recorded in `docs/ai/STATE.md`/`DECISIONS.md`,
  **not executed, no production change applied**. `docs/ai/*` checkpoint created (STATE/
  VERIFICATION/HANDOFF/DECISIONS). No tasks 5.10–5.13 completed; Stage 5 remains incomplete.
- Pending (not yet executed, no claim made): fix the worker-test compile error, re-verify
  androidTest compile + fresh `testDebugUnitTest --rerun`, resolve F1/F2, then Stage 5
  instrumentation on an explicitly identified emulator (5.10), emulator acceptance matrix (5.11),
  final host gate + `scripts/check_*.py` (5.12), `docs/ai/*` commit/push + Issue #31/Project sync (5.13).
- 2026-10-20 second resume session (checkpoint commit pushed as owner-authorized IN-PROGRESS
  checkpoint, verification incomplete): worker-test L324
  compile error FIXED → **EXECUTED** `assembleDebugAndroidTest` **BUILD SUCCESSFUL 0 errors**;
  **EXECUTED** `testDebugUnitTest --rerun` → **174/0/0/0** (fresh). F1/F2 investigated,
  confirmed against source and **owner-approved**: F1 test retargeted to the reachable contract
  (test-only), F2 `BundleStore.installBlob` dedup now verifies the existing target and
  quarantines invalid bytes before installing the verified copy (production fix, ~15 lines).
  After both changes: **EXECUTED** `testDebugUnitTest + assembleDebugAndroidTest` → **BUILD
  SUCCESSFUL, 174/0** (reports 19:23). PMP identified (Project Memory Protocol,
  `stokonn/project-memory-protocol`); local state verified **absent everywhere** (CLI, XDG
  state, `.pmp`/`.pmp.yml` in any commit, clones) — not reinitialized. Emulator toolchain
  installed (emulator 37.2.12.0 + android-36 google_apis x86_64), AVD `stage5-api36` created
  (AVD home: `/home/stokonn/.config/.android/avd` → needs `ANDROID_AVD_HOME`), **booted
  emulator-5580 (API 36, qemu=1)**; stale `app-debug.apk` (Oct 16) caught before any test and
  rebuilt (**EXECUTED** `assembleDebug` → BUILD SUCCESSFUL, fresh APK 19:45). Battery emergency
  stopped the session: emulator cleanly killed (`emu kill` OK); **zero instrumentation tests
  executed**; tasks 5.10–5.13 all remain open.
- 2026-10-20 third session (desktop machine, checkpoint commit pushed): fresh
  `assembleDebug + assembleDebugAndroidTest` **BUILD SUCCESSFUL** (1m08s, 0 errors, fresh
  APK hashes recorded in `docs/ai/VERIFICATION.md`). All five `scripts/check_*.py`
  **PASS**; full host gate re-run executed (result in VERIFICATION.md). Emulator work on
  the desktop **blocked by upstream SIGSEGV bug (Google #566807521)** on Fedora kernel
  7.2.10: both AVDs (playstore API 37 + new google_apis API 36 `stage5-api36`) and both
  emulator versions (37.2.12 + 37.3.4 canary) crash after the "protected range" warning,
  independent of GPU mode — **no instrumentation executed; tasks 5.10/5.11 deferred to the
  notebook's working API 36 emulator** (exact steps in `docs/ai/HANDOFF.md`). Host-side
  portion of task 5.12 executed on the desktop; 5.13 checkpoint docs updated + pushed, but
  5.13 stays open pending Issue #31/Project sync after 5.10/5.11. PMP local
  `.pmp/`/`.pmp.yml` preserved untouched (never reinitialized).
- 2026-10-20 GUI-mode + CI follow-up (same session): the owner-requested **GUI-mode**
  emulator attempt on the GNOME desktop (explicit `emulator-5580`, `-gpu auto`, session
  XAUTHORITY) also ended **`Killed by SIGSEGV`** with no ADB device → all local emulator
  retries stopped. A **CI verification workflow was prepared and later committed+pushed**
  as `1ef15bf` (`.github/workflows/stage5-emulator.yml`, `workflow_dispatch` only,
  SHA-pinned, full `connectedDebugAndroidTest` for 5.10, digest-verified 0.10.1 upgrade +
  network/process-death/startup evidence for 5.11); **no CI execution has happened; no CI
  results claimed**. See `docs/ai/STATE.md` items 6–7 and `docs/ai/VERIFICATION.md` rows
  11–12.
- 2026-10-20 CI-activation session (Phase A/B of the owner's CI plan; **no CI run, no
  emulator run, no instrumentation — approval gate pending**):
  - **Phase A — CI-only PR prepared, NOT merged.** Worktree off `origin/main`
    (`556970a`), branch `ci/stage5-emulator-verification`, commit `9870d99`, draft
    [PR #32](https://github.com/stokonn/Goldberg/pull/32) — diff verified **workflow-only**
    (1 file, +379): no application source, database, release, PMP or unrelated documents.
    Stage 5 working tree and all untracked files preserved untouched. Workflow safety
    re-reviewed: `workflow_dispatch` only, `contents: read`, `persist-credentials: false`,
    all five action SHA pins re-verified against their release tags, 30-day evidence
    retention, 150/120-min timeouts, per-ref concurrency, qemu-verified `emulator-NNNN`
    serial guards on every adb call.
  - **Phase B — findings investigated and resolved in the revised workflow (same file
    now also on this branch):** (1) *signing* — apksigner-verified the trusted 0.10.1
    release APK carries cert `53eface4…`; a CI debug candidate can never be installed
    over it in place, so the upgrade test now uses a **debug rebuild of the exact
    v0.10.1 tag built in the same job** (same runner keystore, signatures match by
    construction) while release-signature compatibility stays a publication gate —
    no production credentials in CI; (2) *network checks* — the old
    `cmd netpolicy set data_saver enable` is **invalid on API 36** (AOSP-verified) and
    the Wi-Fi→mobile/Data Saver steps only collected logs → replaced with
    `set/get restrict-background` state readback + `dumpsys` state assertions + a
    transport-level default-network parser (unit-tested; mobile data is re-enabled
    after the `--offline` upgrade run); (3) *process death* — now pulls the database
    after real force-stop → cold restart and asserts `PRAGMA integrity_check`/`foreign_key_check`/
    `user_version`; (4) *exclusions* — scan confirms **0** `@Ignore`/filters/excluded
    classes; the full 42-class suite runs unfiltered; (5) *coverage honesty* —
    CI-covered vs manual-only 5.11 items (consent revocation mid-stream, low disk,
    screenshots, real bundle-transfer costs) documented in the workflow header;
    (6) *SHA binding* — both jobs record and assert `git rev-parse HEAD == GITHUB_SHA`.
    Full executed/not-executed ledger: `docs/ai/VERIFICATION.md` (CI-activation section).
  - **STOPPED at the owner approval gate as instructed:** merging PR #32 into `main` and
    any manual `workflow_dispatch` require explicit owner approval (Phase C). Tasks
    5.10–5.13 remain open; no merge/tag/release; Stage 6 not started.

**Completion criteria (from STAGES.md Stage 5 acceptance):** process-death/resume,
low-space, corrupt-asset, concurrent remove/clear/restore tests; Wi-Fi→mobile,
consent-revoked and Data Saver cases on an emulator; no unexpected mobile traffic; complete
offline credits and partial states; old READY revision survives interrupted refresh;
practice data stays available when all bundles are removed; measured disk/network costs.
Mark ✅ only when all of the above have executed-passed evidence.

---

## Stage 6 — Secured optional community backend

**Status: ⬜ planning prepared and owner decisions recorded (2026-10-21);
implementation not started.** The detailed implementation-ready plan lives in
[stage-6/](stage-6/README.md) (architecture, auth, data model, API contracts, threat
model, zero-cost ops, decision register, handoff); the authoritative task list is
[stage-6/CHECKLIST.md](stage-6/CHECKLIST.md) — **every implementation task there is
unchecked.**

**Owner decision review (2026-10-21), outcomes recorded in
[stage-6/OPEN-DECISIONS.md](stage-6/OPEN-DECISIONS.md):**

- ✅ **Approved:** D4a Supabase Free (one optional backend, strict 0 CZK/month, no
  paid plans/trials/auto-upgrades); D4b free-tier suspension/quotas accepted with
  graceful degradation, core local functionality preserved and fail-closed behavior;
  D5c Frankfurt `eu-central-1` (with pre-provisioning verification of availability,
  residency, transfers, DPA, subprocessors); D5f general-audience moderated community
  with minimal data; D5g owner-only moderation initially with server-side audited
  grants; D8a in-app Android Admin (no separate APK); D8c Supabase-console bootstrap
  with MFA-protected operator access and audited recovery.
- 🟧 **Approved conditionally:** D8b Web Admin BFF (HttpOnly/Secure/SameSite=Strict
  cookies, server-side authz/CSRF, no bearer tokens in browser JS) — **must first
  prove the 0 CZK fit** (task 6.0f) or report the blocker. *Feasibility study
  executed 2026-10-21 → CONDITIONAL PASS
  ([stage-6/BFF-FEASIBILITY.md](stage-6/BFF-FEASIBILITY.md)); **C1 APPROVED
  2026-10-21** (Cloudflare Workers Free host, architecture-level only — no
  provisioning); **C2 EXECUTED 2026-10-21 → CONDITIONAL PASS** (isolated local
  `workerd` spike on `spike/goldberg-0.11-c2-bff`, final commit `7bc6976`:
  19/19 security, P1–P7 executed, kernel CPU **2.03 ms/invocation**;
  per-invocation production CPU unverified, §12 production-runtime
  verification owner-gated); **C3 documentary review COMPLETE 2026-10-21**
  ([stage-6/C3-EDGE-PRIVACY-REVIEW.md](stage-6/C3-EDGE-PRIVACY-REVIEW.md):
  conditional privacy viability at 0 CZK, no compliance claim, legal/operator
  questions open). **C4 remains open** — its pre-provisioning checklist was
  prepared 2026-10-21
  ([stage-6/C4-PREPROVISIONING-CHECKLIST.md](stage-6/C4-PREPROVISIONING-CHECKLIST.md),
  preparation ≠ final verification).*
- 🟨 **Provisional / deferred:** D5a personal contact email is the intended public
  contact but **no address was supplied or authorized — nothing may be published**;
  D5b controller/operator determination deferred to pre-launch review — **research
  deliverable [stage-6/Q1-OPERATOR-READINESS.md](stage-6/Q1-OPERATOR-READINESS.md)
  prepared 2026-10-21; still legally unresolved and blocking for 6.0d**; D5d
  retention/deletion principle approved with **details unresolved**; D5e licensing
  approved subject to pre-launch legal review.

**Separately gated (never authorized by the decision review):** implementation start,
provisioning (6.0d), Google OAuth console setup (6.0e), and public launch/collection
(pre-collection gate, OPEN-DECISIONS.md §5). No provisioning, no OAuth configuration,
no client-side speculation, no paid service has occurred.

- **Purpose:** Optional, secured community backend: server-validated Google auth,
  default-deny five-role authorization, revisioned submissions with immutable
  evidence, one-transaction moderation decisions with audit trail, versioned `/v1`
  API shared by future Android/Web Admin clients — under the permanent 0 CZK
  contract, with zero practice-journal data ever crossing the boundary.
- **Scope (planned, not implemented):** backend server code + database (Supabase
  Free per D4a), Android community repository/token storage (optional feature),
  export/restore tooling. **No** journal schema/backup/signing change; no Stage 7 UI;
  no bundle changes.

**Tasks — see [stage-6/CHECKLIST.md](stage-6/CHECKLIST.md) for full definitions
(6.0a–6.0f decisions/provisioning; 6.1–6.9 server core; 6.10–6.11 Android
integration; 6.12–6.16 verification gates; 6.17–6.18 handoff/closure). All
implementation tasks UNCHECKED.**

- [x] 6.0a D4/D4b — decided 2026-10-21 (Supabase Free; graceful denial; fail closed;
      strict 0 CZK).
- [ ] 6.0b D5a–D5g — partially decided; D5a details, D5b determination, D5d details
      and D5e/D5f pre-launch reviews remain open.
- [x] 6.0c D8a–D8c — decided 2026-10-21 (D8b conditional on the 6.0f proof gate).
- [ ] 6.0d Provisioning (separate owner instruction + D5c verification list).
- [ ] 6.0e Google OAuth console configuration (separately authorized).
- [ ] 6.0f D8b zero-cost BFF hosting/runtime proof (or blocker report to owner) —
      🚧 **feasibility study executed 2026-10-21 → CONDITIONAL PASS**
      ([stage-6/BFF-FEASIBILITY.md](stage-6/BFF-FEASIBILITY.md)); **C1 APPROVED
      2026-10-21** (Cloudflare Workers Free host, single origin, architecture-level
      only — no provisioning); **C2 EXECUTED 2026-10-21 (separate owner
      authorization) → CONDITIONAL PASS** — isolated local `workerd` spike on
      `spike/goldberg-0.11-c2-bff` (final commit `7bc6976`): **19/19 security,
      P1–P7 executed, kernel-measured average CPU 2.03 ms/invocation**;
      per-invocation production CPU **unverified** and the §12
      production-runtime verification remains owner-gated; spike code stays
      isolated (unmerged); **C3 DOCUMENTARY REVIEW COMPLETE 2026-10-21**
      ([stage-6/C3-EDGE-PRIVACY-REVIEW.md](stage-6/C3-EDGE-PRIVACY-REVIEW.md)):
      conditional privacy viability at 0 CZK, no compliance claim, legal/
      operator questions open (controller identity incl. minor-owner capacity
      now blocking for 6.0d; **Q1 research deliverable prepared 2026-10-21**
      ([stage-6/Q1-OPERATOR-READINESS.md](stage-6/Q1-OPERATOR-READINESS.md) —
      Options A/B/C documented, **no option selected, D5b still legally
      unresolved**). **Still open: C2 §12 production verification and
      C4 terms re-verification (checklist prepared; final run required before
      any provisioning).**
- [ ] 6.1–6.9 Server core: schema, auth, authorization, submissions, decision
      transaction, idempotency, read overlay, admin endpoints, abuse controls.
- [ ] 6.10–6.11 Android optional integration with strict personal-data isolation and
      graceful offline/unavailable behavior.
- [ ] 6.12–6.16 Adversarial matrix (local + separately approved integration rows),
      export/restore drill, privacy/terms published before any collection.
- [ ] 6.17–6.18 Frozen contract + Stage 7 handoff; tracking sync with evidence.

**Verification:** none executed — nothing is claimed. Planning-document consistency
checks (links, contract reuse, decision-reference synchronization) are recorded in
the Stage 6 documentation session report, not as implementation evidence.

## Stage 7 — Encyclopedia UX and Android/Web Admin clients

**Status: ⬜ not started.** Depends on stable Stage 4/5 contracts and Stage 6 admin actions.

## Stage 8 — Integration, legal review and release readiness

**Status: ⬜ not started.** Only after all prior gates; requires explicit release instruction
and current mandatory verification gates.

---

created by stokonn with love <3
