> Current safe recovery: see [SAFE-RECOVERY.md](SAFE-RECOVERY.md). The owner now
> authorizes checkpoint commit/push and Stage7B dispatch. Latest Issue31 records
> O1–O8 design decisions and local Stage6 M1; integration stays gated. Stage7A
> finally accepted; X10 deferred to Gould0.12, unfixed/unverified. Earlier statuses
> below describe the interrupted preparation, not current authorization.

# Stage 7B — GitHub Actions CI recovery

Purpose: move blocked Android verification to the existing dispatch-only Actions
workflow; this is verification recovery, not a new stage or Admin redesign.
Branch/worktree verified: `feature/goldberg-0.11-stage-7b`,
`/home/stokonn/Projects/Goldberg-Stage7B`; main checkout `/home/stokonn/Projects/Opus`.
Initial HEAD `cee9330`; all pre-existing dirty source preserved.

## Scope and tasks

- [x] Read AGENTS/maintenance/checkpoints/checklist/acceptance and latest Issue31.
- [x] Audit existing quality, Stage5 emulator and screenshot workflows. Existing
  API36 full-suite job uses Ubuntu24.04, JDK17, KVM, pinned actions, ephemeral
  runner debug signing and exact dispatched SHA checks. No production keys.
- [x] Preserve prior275 JVM passes (24 contract tests), lint0 errors/132 warnings/
  1 hint, native APK compilation and recovered emulator SIGSEGV diagnostics.
- [x] Latest Stage6 checkpoint inspected: API-v1 package at `fefa6e8` is PROPOSED;
  owner O1–O8 pending. G1–G9/G11 remain integration gates; G10 locally closed.
- [x] Extend existing workflow with stage7b dispatch mode, one API36 emulator job,
  Gradle cache, build/JVM/lint/host checks and always-upload reports (14days).
- [x] Add four native Admin regressions: Keystore roundtrip/tamper, Android marker
  persistence/account isolation, disabled transport with saved credentials,
  native JSON exact BIGINT/revision overflow. Synthetic data only.
- [x] Workflow YAML/run-block syntax and helper bash syntax PASS; assembly gate
  SUCCESS (cached compilation), fresh JVM task 275/0/0/0 in12s. Six host scripts PASS.
- [ ] Put the intended verification source on GitHub under an approved scope;
  existing uncommitted implementation cannot be tested by checking out cee9330.
- [ ] Dispatch exact commit, inspect actual job logs/results and retrieve artifacts.
- [ ] Inspect captured smoke screenshots; classify their limited scope honestly.
- [ ] Synchronize Issue31/dashboard/Project, AI docs, master/stage checklist and PMP.

## Coverage and limits

Full `connectedDebugAndroidTest` suite; no class filters, exclusions or new skips.
Existing `Stage7aCatalogueBootstrapTest` may assumption-skip when public OpenOpus
is unavailable; report actual XML skips. Explicit target emulator-5554 with qemu=1
and API36 guards. CI smoke screencaps do not prove Admin dashboard/no-scroll,
confirmations, re-review, TalkBack, locale/font/theme matrix, notifications/worker
runtime, seeded upgrade or live contracts. These acceptance rows stay pending.
API37 compatibility is UNVERIFIED and is not scheduled alongside the stable job.
Fedora44/emulator37.2.12 graphics SIGSEGV/offline is environmental evidence, not
an app defect. No emulator reinstall, Fedora driver change or local relaunch.

No Stage6 worktree/contracts, application models, production networking/moderation,
physical devices, main merge, deployment or release. X10 OPEN; Stage8 pending.
