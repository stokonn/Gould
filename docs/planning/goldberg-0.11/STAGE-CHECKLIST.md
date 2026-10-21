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

**Status: 🚧 in progress.** Branch `feature/goldberg-0.11-stage-5`, baseline Stage 4 verified
head `99ea73a`. Owner-approved decisions: D5.1 file-based manifests, D5.3 WorkManager
(owner-mandated), D7 storage caps + per-job metered consent, D5.6 test-only org.json
(see `docs/ai/DECISIONS.md`). No release/merge/tag authorized.

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
- [x] 5.10 Execute `Stage5*Test` instrumentation on an explicitly identified emulator (store I/O, runner pipeline, real WorkManager execution/cancel/reconcile). — **COMPLETE: run 39557624554 (attempt 6), 177 tests / 0 failures / 0 errors / 2 skipped, stable across runs 4–6; 2 skips investigated = designed fixture assumptions (`skipped-tests.md`).**
- [x] 5.11 Emulator acceptance: Wi-Fi→mobile transition, Data Saver, consent revocation mid-stream, real process death at boundaries, low disk, seeded in-place upgrade from trusted 0.10.1, disk/network cost measurements. — **COMPLETE: every item has passed executed evidence.** Attempt 6 (run 39557624554): transition `101→100` + Data Saver both directions with state readback, process-death + cold restart, post-restart DB integrity (`ok`/`none`/`user_version 10`), seeded in-place upgrade all-green, startup measured. Instrumentation (same run, 177 tests): consent revocation mid-stream (`consentRevocationMidStreamPausesAndKeepsReadyContent` + worker/persistence variants), low disk (`lowStorageRefusesBeforeAnyByteMoves` + backup/portrait variants), corrupt assets, concurrent remove/clear/restore, old-READY-survives-interrupted-refresh, practice-available-with-caches-removed. **Screenshots: run [39576173002](https://github.com/stokonn/Goldberg/actions/runs/39576173002) GREEN — 12/12 (Home/Repertoire/Discover/Practice/Profile/Settings × Ivory/Nocturne), agent-inspected pair-by-pair, preserved in `results/ci-run-39576173002/` (after 3 script-defect attempts, each root-caused + fixed via workflow-only PRs #36–#38; no application defect in any of them).** Disk/network cost measurements: owner-dispositioned (DECISIONS.md) — executed tests' exact byte accounting + real file I/O + `disk-usage.txt`; real-CDN transfer recorded as a Stage 6/7 dependency (no bundle feed exists).
- [x] 5.12 Full host gate re-run + `scripts/check_*.py` (migrations/backup/localization/paging/policy). — **COMPLETE: `./gradlew clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1` → BUILD SUCCESSFUL (3 m 41 s), unit tests 174/0/0/0, lint 0 errors / 110 pre-existing warnings; all 5 check scripts PASS (agent_policy, backup_fixtures, history_paging, localization, migrations).**
- [x] 5.13 Update `docs/ai/*` (STATE/VERIFICATION/HANDOFF/DECISIONS), reviewed commit, push to Stage 5 branch, verified evidence to GitHub Issue #31 + Project. No merge/tag/release/APK publication; Stage 6 not started. — **COMPLETE: STATE/HANDOFF/VERIFICATION/DECISIONS + STAGE-CHECKLIST synchronized through the green screenshot run; evidence bundles committed (`ci-run-39557624554` 22 MB, `ci-run-39576173002` 12 MB); reviewed commit pushed; Issue #31 checkpoints posted for every session (run-6 green, decisions, screenshot attempts 1–4); Project kept In Progress. Only workflow files ever merged to `main` (PRs #35–#38); no application code merged; no tag/release/APK publication; Stage 6/7 not started.**

**Verification (executed so far, 2026-10-20 sessions):**

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
- 2026-10-20 first CI dispatch + failure-triage session (Phase C/D authorized by owner;
  **tasks 5.10/5.11 still open — both jobs failed BEFORE any application test ran**):
  - **Phase C/D executed:** CI-only PR #32 merged to `main` as merge commit `577d811`
    (diff verified workflow-only, 1 file, +384 lines; comment fix `e61313b` also mirrored
    to the Stage 5 branch as `bcd8894`); workflow dispatched against
    `feature/goldberg-0.11-stage-5` @ `bcd8894` (SHA binding asserted in-run).
  - **Run [39503450845](https://github.com/stokonn/Goldberg/actions/runs/39503450845):
    both jobs FAILURE, pre-test.** 5.10: GitHub-hosted API 36 emulator booted in ≈53 s
    (KVM works on runners), then the script died at line 1 — the emulator-runner invokes
    it with `/usr/bin/sh` (dash), which rejects `set -o pipefail`
    (`/usr/bin/sh: 1: set: Illegal option -o pipefail`, exit 2). 5.11: builds, trusted
    0.10.1 download and digest check succeeded; the signing step logged
    `MISSING` for all three APKs and the emulator step was skipped.
    **Owner-confirmed classification: CI setup/script failures — not application
    defects; no application conclusion may be drawn from this run.**
  - **Root causes established with evidence** (full bundle preserved under
    `docs/planning/goldberg-0.11/stage-5/results/ci-run-39503450845/`, raw logs SHA-256
    `a6b48abb…ad85`): (1) the sh/dash incompatibility above; (2) apksigner's print format
    changed in build-tools 37.0.0 (preinstalled on the runner and selected by
    `sort -V | tail -n1`) from `Signer #1 certificate SHA-256 digest:` to
    `V2 Signer: certificate SHA-256 digest:` — locally reproduced with the same release
    APK: both versions report the identical digest `53eface4…d59e`, only the label
    differs, so the old sed pattern matched nothing with exit 0 (no certificate
    mismatch occurred and none was concluded).
  - **Minimal workflow-only corrections applied (assertions unchanged/strict):** both
    emulator scripts now materialize into `$RUNNER_TEMP` via quoted heredoc and run with
    explicit `exec bash`; the signing step records raw apksigner stdout/stderr/exit
    codes + build-tools inventory to uploaded artifacts (no suppressed diagnostics) and
    parses both apksigner formats requiring exactly one distinct signer digest; trusted-
    certificate equality and baseline/candidate equality checks preserved verbatim;
    all three APKs inspected before failing.
  - **Corrections statically + functionally validated on the host (executed):** YAML/
    `bash -n`/`py_compile` all pass; wrapper mechanics proven under `sh -c` with
    stubbed adb/gradlew (guards, trap-through-exec, failure propagation); signing step
    sandbox in exact runner conditions (apksigner 37.0.0 selected) — success exit 0,
    genuine cert mismatch (separately generated test keystore) exit 1, corrupt APK
    exit 1 with diagnostics preserved. dash itself not installed locally (POSIX
    reasoning only for the 3 outer wrapper lines); actual API 36 suite runs pending
    re-dispatch.
  - **Workflow-fix PR for `main` prepared; STOPPED before merge/dispatch per owner
    instruction** (explicit approval required for significant main-branch change or new
    CI dispatch). Tasks 5.10–5.13 remain open; no merge of Stage 5 application code; no
    tag/release/APK publication; Stage 6/7 not started.
- 2026-10-20 second CI dispatch + failure-triage session (owner-approved attempt 2;
  **again no application tests executed — CI script-execution-model failure, not an
  application defect**):
  - **Approved steps executed:** pre-merge diff verified workflow-only (1 file,
    +79/−8); PR #33 merged to `main` as merge commit `a4a9509`; corrected workflow
    dispatched against `feature/goldberg-0.11-stage-5` @ `5cdc0d6` →
    [run 39508388525](https://github.com/stokonn/Goldberg/actions/runs/39508388525)
    (headSha verified); both jobs monitored to completion; evidence preserved at
    `docs/planning/goldberg-0.11/stage-5/results/ci-run-39508388525/`
    (raw logs SHA-256 `efa4baf3…42e7`, 12 artifacts incl. the new signing diagnostics).
  - **Attempt-2 fixes verified on the actual runner:** SHA binding passed (both jobs);
    all builds passed; **signing step PASSED with full diagnostics** — the runner
    recorded `apksigner used: /usr/local/lib/android/sdk/build-tools/37.0.0/apksigner`
    (empirically confirming the run-39503450845 root cause on the runner itself),
    release digest `53eface4…d59e` = trusted certificate, baseline = candidate =
    runner key `47aebcfd…34c6`, all exit codes 0, raw streams uploaded as artifacts;
    API 36 emulator booted in **49 524 ms** (KVM works).
  - **Run-2 root cause (proven from the pinned action's own source):**
    `reactivecircus/android-emulator-runner@a421e43` `parseScript()` splits the
    `script` input into **single lines** (comments/blanks stripped) and `main.ts` runs
    each line via a **separate `sh -c`** — state cannot persist between lines and
    multi-line constructs cannot work; the log shows three consecutive `sh -c` calls
    (`set -e`, heredoc opener, `set -euo pipefail` → dash error, exit 2). Run
    39503450845 died at line 1 of this same model. The action's `script` input only
    supports single-line commands.
  - **Meaningful fix applied (workflow-only; script body byte-identical):** a regular
    GitHub `run:` step (one `bash -e` process) materializes the approved script into
    `$RUNNER_TEMP/stage5-<job>.sh` + `bash -n` checks it; the action's `script:`
    becomes exactly one line `bash "$RUNNER_TEMP/stage5-<job>.sh"` → `parseScript`
    yields one chunk → the whole script runs in a single bash process.
  - **Fix validated on the host (executed):** `validate_v3.py` — YAML OK; faithful
    `parseScript` replica yields **exactly 1 chunk per job**; `bash -n` OK on both
    materialize steps and both materialized bodies; end-to-end simulation (GH run step
    → parseScript → `sh -c` → bash file with stubbed adb/gradlew/sqlite/scripts):
    **job 1 exit 0** (guards, variable persistence, EXIT trap, gradle invoked);
    **job 2 exit 0 through the full acceptance flow** (WiFi→mobile 100→101 transition
    with net-id inequality, Data Saver exact readbacks, force-stop/restart pidof
    gates, DB pull + `PRAGMA integrity_check/foreign_key_check/user_version`
    assertions, 5× startup, trap; 25 deep `emulator-5554` adb calls prove persistence);
    negative guard test propagates exit 1. Canned stubs validate the execution
    model only — application assertions are verified solely by the real CI run.
  - **Attempt 3 launch (2026-10-21, documentation-first):** workflow-only PR #34 merged
    to `main` as `816d142` (pre-merge `gh pr diff 34 --name-only` = workflow file only,
    +42/−35; post-merge `git diff a4a9509 origin/main` re-verified workflow-only; PR
    branch byte-identical to the Stage 5 workflow copy); dispatched against Stage 5
    checkpoint `9b80d65` (headSha verified).
  - **Attempt 3 result (2026-10-20/21, [run 39511602936](https://github.com/stokonn/Goldberg/actions/runs/39511602936)
    @ `9b80d65): FIRST RUN TO EXECUTE REAL APPLICATION TESTS — CI pipeline now works
    end-to-end** (materialize steps ✓, both scripts ran as single bash processes on
    booted emulators, `connectedDebugAndroidTest` executed). Evidence:
    `docs/planning/goldberg-0.11/stage-5/results/ci-run-39511602936/`
    (raw logs SHA-256 `6e93fdd4…1ba11`, 253 artifacts incl. per-test logcats).
  - **5.10 — EXECUTED, FAILED: 177 tests, 162 passed, 13 failed, 0 errors, 2 skipped.**
    All Stage 1–4 suites passed on-device; failures all in 3 Stage 5 classes:
    Stage5BundleJobRunnerTest (6: concurrent shared dependency, interrupted download/
    refresh retry, corrupt source/stored blob, consent revocation mid-stream),
    Stage5BundleStoreTest (2: interrupted refresh atomic promote, reopen survival),
    Stage5BundleWorkerTest (5: queued bundle, offline retry, transport retry, cancelled
    job, missing metered consent — all `got Failure {mOutputData=Data {}}`). Observed
    signatures include on-device `Retryable(error=integrity/transport)`,
    `Integer<22> vs Long<22>` JSON type coercion, `List has more than one element`.
    Root causes NOT yet determined — code-level triage in progress; no assertion
    weakened.
  - **5.11 — EXECUTED, FAILED at the FINAL offline-state assertion only
    (`verify_emulator_upgrade.py:143`, 'Offline state changed during upgrade').**
    Everything before it PASSED on the emulator: baseline install + launch + schema
    verify, seed push, candidate install + launch, prefs unchanged, `before == after`
    persistent-data equality, `PRAGMA user_version` = expected schema,
    `foreign_key_check` clean, UUID-v4 identity bridge — **the seeded in-place upgrade
    from v0.10.1 with full data preservation is verified working on the emulator.**
    Offline-signature assumption on API 36 to be investigated (capture dumpsys output;
    do not weaken).
  - **Gate held:** diagnosis + documentation recorded BEFORE any re-dispatch; no new
    dispatch started. Tasks 5.10–5.13 remain open; Stage 5 IN PROGRESS.
  - **Code-level triage COMPLETED (2026-10-21) — all 13 root causes confirmed from code
    + the preserved CI artifacts** (Issue #31 checkpoint carries the full table):
    5 = test-harness context bug (`TestListenableWorkerBuilder` stores the raw
    `targetContext`, so `BundleDownloadWorker`'s `applicationContext as? OpusApplication`
    failed at entry → `Result.failure()`; real WorkManager passes the `Application`,
    proven by its sibling test passing on-device); 7 = deterministic test/fixture
    defects never surfaced before (these androidTest-only tests had never executed
    anywhere — KDoc: "compile-verified only until task 5.10 executes them"): random
    per-call fixture bundleIds ×2, URL selector matching every fixture URL, blob never
    installed before promote, JUnit `assertEquals(String, Int, Long)` Object-overload
    boxing (`Integer<22>` vs `Long<22>` — NOT JSON coercion); 1 = **genuine product bug**
    (`settle()` kept `validatedAt` on STALE→QUEUED, violating the manifest invariant;
    `settleQuietly` swallowed the IAE → bundle wedged STALE forever) + 1 **invariant gap**
    (`readPublished()` exposed partial QUEUED/FAILED_RETRYABLE manifests as "published",
    violating "a partial bundle is never published"). The earlier real-network hypothesis
    was **ruled out by code** and the run summary corrected.
  - **Fixes implemented + locally verified (commit `dcea5b2`, working tree = pushed
    Stage 5 branch):** product fixes in `BundleJobRunner.settle()` (drop `validatedAt`
    for non-authoritative targets), `BundleStore.readPublished()` (authoritative states
    only, `PUBLISHED_STATES`), `BundleJobRunner.locate()` (audit-mandated: resolve via
    `readAll()` + `readPending()`), comment alignment in `OfflineBundle.kt`; harness fixes
    in all three test classes (builder `applicationContext`, shared bundleIds, exact-URL
    routing with identical shared bytes, blob installed before promote,
    `content.size.toLong()`). **No assertion weakened; no test skipped** (verified: diff
    removes no assertion except the type-corrected one, which remains with identical
    message/expected/actual semantics).
  - **Verification EXECUTED locally:** `./gradlew testDebugUnitTest` → **BUILD
    SUCCESSFUL, 174 tests, 0 failures** (bundle suites: Job 8, Codec 21, Policy 10,
    StateMachine 13, StoreLogic 10, TransferEngine 15 all green); `assembleDebugAndroidTest`
    → **BUILD SUCCESSFUL**. Not executed locally: androidTest runtime (host emulator
    blocker documented in STATE) — device execution is what the CI dispatch provides.
  - **Attempt 4 dispatched under the sanctioned loop (2026-10-21,
    [run 39518973833](https://github.com/stokonn/Goldberg/actions/runs/39518973833) @
    `dcea5b2`, headSha verified; both jobs' `source-commit.txt` confirm the SHA).**
  - **5.10 — EXECUTED AND PASSED: 177 tests, 0 failures, 0 errors, 2 skipped on the
    API 36 emulator.** All 13 run-3 fixes verified on-device (worker cluster,
    fixture/endpoint routing, JUnit overload, product `settle()`/`readPublished()`/
    `locate()` fixes). Evidence:
    `docs/planning/goldberg-0.11/stage-5/results/ci-run-39518973833/`
    (raw logs SHA-256 `4b92dc7a…ab28`, counts/XML/testlog/logcat; full 257-file set in
    GitHub artifacts, 30-day retention). Task 5.10's connected-suite execution now has
    executed-**passing** evidence; remaining 5.10 items (notebook/manual-only: consent
    revocation mid-stream, low disk, screenshots, real bundle-transfer costs) still open.
  - **5.11 — upgrade validation PASSED again; failed at the final offline assertion
    with the root cause now PROVEN from logcat (not inferred):** `svc wifi disable`
    worked (`WifiService: setWifiEnabled … enable=false`, 00:11:59.904), but **`svc data
    disable` had zero observable effect** — no telephony data-disable event anywhere in
    the 84 730-line acceptance logcat, and the emulated CELLULAR network registered at
    00:12:15, became the active default network at 00:12:19, and served `com.opus.piano`
    throughout the "offline" upgrade. The assertion (`Active default network:
    none|null|-1`) correctly caught a premise that was **never achieved** — not an app
    regression. Post-python acceptance sections (Wi-Fi→mobile transition, Data Saver,
    process-death, post-restart DB, startup measurement) still never executed in CI.
  - **5.11 fix designed + documented BEFORE implementation:** verify/retry loop after
    the disables (`settings put global mobile_data 0` belt-and-suspenders; escalation to
    `cmd connectivity airplane-mode enable` if still online), `dumpsys connectivity`
    captured to the artifact dir at every stage, fail-fast with evidence if offline
    cannot be achieved, final assertion semantics **unchanged**, workflow section (b)
    adds airplane-mode disable before the restores. No assertion weakened anywhere.
  - **Fix implemented + locally verified (2026-10-21, documented before dispatch):**
    `scripts/verify_emulator_upgrade.py` — `connectivity_dump(tag)`/`offline_reached()`
    helpers; `--offline` now runs the original `svc wifi/data disable` **plus**
    `settings put global mobile_data 0`, a 10×3 s verify/retry loop (re-issuing the
    disables; dumpsys evidence saved per attempt), `cmd connectivity airplane-mode
    enable` escalation with 5 further attempts, and an **assertion that offline was
    established before the upgrade** (fail fast, evidence-first). The final assertion
    (was line 143) is **semantically unchanged** (same regex, same message) but now
    saves `connectivity-final.txt` first. Workflow section (b) clears airplane mode
    and re-enables data (`svc data enable` + `settings put global mobile_data 1`)
    before the Wi-Fi→mobile transition. **Local verification EXECUTED:**
    `python3 -m py_compile` OK; workflow YAML parse OK (both jobs); `bash -n` OK on
    both materialized scripts extracted exactly as GitHub will run them; behavioral
    simulation with a stubbed adb **3/3 scenarios correct** (A: toggles apply after 2
    attempts → offline at attempt 3, proceeds to install; B: toggles no-op → airplane
    fires once, offline at attempt 13; C: never offline → fail fast with the exact
    message **before install**, 15 evidence files); `check_agent_policy.py` PASS. Not
    executed locally: real emulator behavior (host blocker) — attempt 5's purpose.
  - **Owner approved fix + verify + fifth dispatch (recovery session); the diagnose-
    and-document-first gate was honored: run-4 results + root cause + fix design were
    documented (commit `a427d88`) before implementation, and the implementation +
    local verification are committed before dispatching.**
  - **Attempt 5 dispatched (2026-10-21,
    [run 39552116455](https://github.com/stokonn/Goldberg/actions/runs/39552116455) @
    `6da1021`, headSha + both jobs' `source-commit.txt` verified):**
  - **5.10 — EXECUTED AND PASSED (second consecutive green): 177 tests, 0 failures, 0
    errors, 2 skipped** on the API 36 emulator. Evidence:
    `results/ci-run-39552116455/instrumentation-counts.txt` + full per-test XML
    (SHA-256 `eee30ad8…98a141`).
  - **5.11 — the python upgrade verification PASSED IN FULL for the first time ever:**
    **`offlineVerified: true` — the attempt-5 offline fix worked** (offline reached at
    verify-loop attempt 1, held through the whole upgrade: `connectivity-final.txt` =
    `none`; no airplane escalation needed); seeded v0.10.1 → candidate in-place
    upgrade fully green (`originalRowsUnchanged`, `preferencesUnchanged`, 37 sessions,
    UUID bridge 3/113, `foreignKeys: pass`, schema 8 → 10); **startup measurement
    executed for the first time** (baseline ≈ 3.6–5.1 s vs candidate ≈ 2.2–2.6 s cold
    process, small-sample caveat noted in `result.json`).
  - **5.11 — failed in section (b) (the Wi-Fi→mobile transition parser, its FIRST
    real execution) with the root cause PROVEN from artifacts:** the host-side
    `assert_default_network.py` read the transport from the legacy `ni{…} type:` token,
    but the API 36 dump prints it only in `nc{[ Transports: … ]}`
    (`connectivity-dump.txt`: `Active default network: 101`,
    `ni{WIFI CONNECTED extra: }`, `nc{[ Transports: WIFI …]}`). **The emulator itself
    behaved correctly** — Wi-Fi reconnected within ~4–8 s and became the validated
    active default (14 loop iterations of evidence in `network-evidence.txt`); only the
    parser could not read the token, so `wait_for_transport WIFI` hit its 60 s deadline
    and `set -e` killed the script. Not an app defect (app not running in this
    section); not a data issue (the upgrade validation had already passed). Sections
    (c)/(d) and the Data Saver half of (b) still never executed. Secondary defect found
    in triage: the deadline FATAL echoed into the `$(…)` substitution and reached
    neither log. Full narrative:
    `results/ci-run-39552116455/run-summary.md`.
  - **Fix implemented + locally verified (documented BEFORE implementation):** parser
    now reads `nc{\[\s*Transports:\s*(\w+)` (canonical API 36 location) with explicit
    `WIFI→WIFI`/`MOBILE→CELLULAR` mapping and the legacy `ni type:` path kept as
    fallback; deadline FATAL echoed to stderr. **Assertions unchanged** (active default
    must be numeric AND declare the expected transport). Local verification
    **EXECUTED:** YAML parse OK; `bash -n` OK on both materialized scripts; **fixed
    parser run against the REAL run-5 dumps — 5/5 checks pass** (A: WIFI @ real dump →
    OK; B: MOBILE @ cellular-active variant → OK via CELLULAR; C: WIFI @ cellular dump
    → still FATAL, negative strictness preserved; D: offline dump → numeric FATAL
    unchanged; E: legacy-format synthetic → fallback OK). Not executed locally: real
    emulator behavior (host blocker). A sixth dispatch awaits owner approval.
  - Tasks 5.10–5.13 remain open; Stage 5 IN PROGRESS.
  - **Attempt 6 dispatched after owner approval and FULLY GREEN (2026-10-21,
    [run 39557624554](https://github.com/stokonn/Goldberg/actions/runs/39557624554) @
    `44645fa`, headSha + both jobs' `source-commit.txt` verified) — the first fully
    green Stage 5 CI run:**
  - **5.10 — PASSED (third consecutive green): 177 tests, 0 failures, 0 errors, 2
    skipped.** Evidence: `results/ci-run-39557624554/instrumentation-counts.txt` +
    per-test XML.
  - **5.11 — PASSED IN FULL; every acceptance section executed for the first time:**
    - **(a) python upgrade verification** (unchanged since run 5): `offlineVerified:
      true` (offline at attempt 1, held through upgrade), `originalRowsUnchanged`,
      `preferencesUnchanged`, 37 sessions, bridge 3/113, `foreignKeys: pass`;
      signing green (trusted cert `53eface4…` matched; baseline = candidate debug
      cert; 3× apksigner exit 0).
    - **(b) Wi-Fi→mobile transition — first execution, PASSED:** the fixed parser
      read the API 36 format: Wi-Fi restored → `active default network: 101`,
      `svc wifi disable` → `100`, **`OK: default network transitioned (101 → 100)`**
      — the run-5 parser fix verified by real execution, not just local checks.
    - **(b) Data Saver — first execution, PASSED:** enable/disable with `cmd
      netpolicy` state readback (`enabled`/`disabled`) **and** `dumpsys netpolicy`
      (`Restrict background: true`/`false`) — `netpolicy-enabled/disabled.txt`.
    - **(c) process-death + cold restart — first execution, PASSED:** force-stop
      confirmed dead → relaunch confirmed alive → databases listed.
    - **(d) post-restart DB integrity — first execution, PASSED:** pulled
      `db-after-restart/opus.db` (preserved): `PRAGMA integrity_check: ok`,
      `foreign_key_check violations: none`, `user_version: 10`.
    - **Startup measurement — first execution, PASSED:** workflow cold launches
      TotalTime 2262/2408/2587/2445/2004 ms; python matched samples baseline
      `[4765,5204,4774,4908,4019]` vs candidate `[2423,2436,1997,2323,2049]` ms
      (candidate ≈2× faster; small-emulator-sample caveat recorded).
    - Evidence: `results/ci-run-39557624554/` (28 files; large logcats retained in
      GitHub artifacts with recorded SHA-256).
  - **CI gate status: the mandatory CI verification for 5.10/5.11 is green
    end-to-end.** Stage 5 stays IN PROGRESS until the notebook/manual-only items
    (consent revocation mid-stream, low disk, screenshots, real bundle-transfer
    costs → 5.12/5.13) also pass. No merge/tag/release performed.
  - **Post-run-6 session (2026-10-21): evidence preservation + skip investigation +
    coverage mapping + 5.12 host gate (all EXECUTED):**
    - *Evidence preserved permanently* — attempt-6 bundle grew to 22 MB: both **full
      logcats** (`acceptance-logcat.txt` `1d29f99c…70cdba`,
      `instrumentation-logcat.txt` `c7c47039…630d9b`, SHA-256 verified identical to
      the downloaded artifacts), per-test logcat for each acceptance-relevant test
      (`consentRevocationMidStream…`, `lowStorageRefusesBeforeAnyByteMoves`,
      `revocationWithdrawsConsentCancelsWork…`), JUnit XML + counts, test-results
      log, DB checks (`db-after-restart/opus.db` + integrity text), startup
      measurement, netpolicy/transition/upgrade evidence — nothing needed remains
      only in the 30-day GitHub artifacts. Reports/HTML (derivative of the XML) not
      committed; retained in artifacts.
    - *Skip investigation* — **`skipped-tests.md`**: both skips are designed
      `assumeTrue` fixture conditions (`Stage4ExternalProviderTest` line 17, `Stage4ExternalPortraitTest`
      line 26 — separately-acquired licensed fixtures **never bundled into either
      APK** by policy); skip set byte-identical across runs 3/4/5/6; `AppLanguageTest`
      **ran and passed** (9.2 s) — an earlier looser parse that suggested otherwise was
      a regex-pairing bug in the ad-hoc extractor, corrected and documented. No test
      hidden: run-1 audit already proved 0 `@Ignore`/filters/exclusions.
    - *5.11 coverage mapping (executed vs open)* — **EXECUTED on the emulator:**
      consent revocation mid-stream → `Stage5BundleJobRunnerTest::consentRevocationMidStreamPausesAndKeepsReadyContent`
      (revoke fired **while bytes stream**, per test source), `Stage5BundleWorkerTest::revocationWithdrawsConsentCancelsWorkAndPausesTheManifest`,
      `missingMeteredConsentPausesAsSuccessAndKeepsRefusalDurable`, `Quality092PersistenceTest::mediaConsentCacheOfflineFailureAndRevocationAreEnforced`;
      low disk → `Stage5BundleJobRunnerTest::lowStorageRefusesBeforeAnyByteMoves`
      (+ `Stage3bBackupTest::exportIsAtomicOnLowSpace…`, `Stage4PortraitTest::missingConsentBlocksTransferAndInsufficientReserve…`)
      — all with per-test logcats in the bundle. Plus attempt-6's (b)/(c)/(d)/startup
      results above. **OPEN:** UI screenshots in both themes (host emulator blocked by
      the upstream SIGSEGV bug → needs a purpose-built CI job) and **real
      bundle-transfer disk/network costs** — structural blocker: the product has **no
      production bundle feed/CDN URL and no production enqueue path** (verified:
      `BundleScheduler` has no non-test caller; no bundle URL anywhere in
      `app/src/main`; download UI is Stage 7) → real-CDN costs are not measurable
      today; current measured evidence = exact byte accounting through the real
      engine + real file I/O in the executed tests + emulator `disk-usage.txt`
      snapshots. Owner decision requested (see Issue #31).
    - *5.12 host gate* — **green** (checkbox above); exact results recorded.
  - **Screenshot session (2026-10-21, owner-approved "build + dispatch"): the last
    open 5.11 item is now EXECUTED and green.**
    - Workflow `.github/workflows/stage5-screenshots.yml` (dispatch-only, same
      safety rules as the green workflow) merged workflow-only via **PR #35**;
      theme switched through the product's **own persistence**
      (`profile_settings.theme` in `opus.db` via `run-as` + `sqlite3` with
      readback assertion), UI driven via the accessibility tree, onboarding
      dismissed through its own Skip button.
    - **Three failures, three distinct script defects — each root-caused from
      raw logs with host-side proof where possible BEFORE re-dispatch; zero
      application defects:** (1) run 39571926478 — theme write before first
      launch, DB absent → `prime_app()` (PR #36); (2) run 39572917657 —
      adb device-shell re-parse split the SQL → stdin-piped `sql()` (PR #37,
      host-proven byte-for-byte); (3) run 39574447299 — fresh install has **no
      settings row** (independently confirmed by the green run's
      `prime.txt`: `row count: 0`) → upsert of the entity-default row (PR #38,
      host-proven for both row states); the script's own readback guard
      (exit 42) caught it — assertions never weakened.
    - **GREEN: run [39576173002](https://github.com/stokonn/Goldberg/actions/runs/39576173002)
      @ `3579417` — 12/12 screenshots** (Home/Repertoire/Discover/Practice/
      Profile/Settings × Ivory/Nocturne), cold start 9.4 s, sha256 manifest,
      preserved in `results/ci-run-39576173002/` (+ `run-summary.md`).
    - **Agent inspection: every pair verified** — identical layouts, correct
      Ivory/Nocturne semantic palettes, adapted system bars, real content,
      no blank frames. Owner visual sign-off offered as courtesy.
  - **Stage 5 status after this session: ALL mandatory verification
    requirements have executed evidence that passed.** 5.10 ✓ (run 6),
    5.11 ✓ (run 6 + screenshots run + owner-dispositioned cost measurement),
    5.12 ✓ (host gate). Remaining: task 5.13 final documentation/handoff.
  - **OWNER APPROVAL (2026-10-21): the owner approved the Stage 5 completion
    claim** based on the verified CI evidence (runs 39557624554 + 39576173002),
    the completed checklist, the archived screenshots and the final
    documentation. **STAGE 5 IS COMPLETE.** The GitHub Project item #31 is
    moved to Done; Issue #31 keeps the permanent audit trail. Carried forward
    as a **future integration dependency**: real bundle-transfer disk/network
    costs against a production CDN remain unmeasurable until a production
    bundle feed/CDN URL + enqueue path exist (Stage 6/7 scope; no feed exists
    today — DECISIONS.md). Application code stays unmerged; no tag/release/APK.

**Completion criteria (from STAGES.md Stage 5 acceptance):** process-death/resume,
low-space, corrupt-asset, concurrent remove/clear/restore tests; Wi-Fi→mobile,
consent-revoked and Data Saver cases on an emulator; no unexpected mobile traffic; complete
offline credits and partial states; old READY revision survives interrupted refresh;
practice data stays available when all bundles are removed; measured disk/network costs.
Mark ✅ only when all of the above have executed-passed evidence.

---

## Stage 6 — Secured optional community backend

**Status: ⬜ not started.** Requires a separate owner instruction; depends on D4/D5/D8 and a
zero-cost operational plan. No provisioning, no client-side speculation.

## Stage 7 — Encyclopedia UX and Android/Web Admin clients

**Status: ⬜ not started.** Depends on stable Stage 4/5 contracts and Stage 6 admin actions.

## Stage 8 — Integration, legal review and release readiness

**Status: ⬜ not started.** Only after all prior gates; requires explicit release instruction
and current mandatory verification gates.

---

created by stokonn with love <3
