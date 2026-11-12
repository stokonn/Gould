# Gould 0.14 — implementation checklist

## One development stage: Activity and weekly progression

Purpose: add a deterministic Activity experience and weekly progression while preserving the existing personal journal and preparing a clean shared Kotlin domain boundary.

Scope: shared-ready domain rules; Android/Room persistence and backup; Activity navigation/UI; historical goals, Beta Week, manual activity, weekly streak and Freeze; cosmetic ownership/rewards; temporary navigation hiding; SDK compatibility; documentation, verification, integration, and prerelease.

### Phase 0 — Preflight
- [x] Confirm canonical repository identity and active worktree.
- [x] Confirm starting commit, branch state, and clean status.
- [x] Select one 0.14 feature branch.
- [ ] Confirm GitHub authentication and inspect remote PR/release/issue state. **Blocked:** saved `stokonn` CLI token is invalid; no remote mutations attempted.
- [x] Inspect signing setup and release workflow. Production signing remains a required later gate; never use the historical debug signing init as production.

### Phase 1 — Domain contracts
- [x] Define week identity, effective goals, Beta Week, day classification, corrections, streak, Freeze, and reward idempotency.
- [x] Extract deterministic logic behind platform-free interfaces in `:activity-domain` `commonMain`.
- [x] Add focused shared/JVM domain tests and compare legacy daily-statistics outputs.

### Phase 2 — Persistence and backup
- [x] Add only required Room records and forward migration from v11.
- [x] Preserve historical dates, profile, achievements, recovery, audio metadata, and backup compatibility in additive schema/backup handling.
- [ ] Verify populated v11 upgrade and backup round trip on Android/emulator.

### Phase 3 — Activity and goals UI
- [x] Replace visible Discover tab with Activity while retaining Discover code/routes.
- [x] Implement year heatmap, day details, week overview, statistics details, and retrospective entry/deletion UI.
- [x] Implement goal settings, Beta Week, weekly streak, Day Off/Miss, and automatic Freeze logic.
- [x] Implement cosmetic catalog, durable ownership, weekly reward, and collection UI.
- [x] Hide visible moderation/admin entry; retain implementation.

### Phase 4 — SDK, documentation, and release verification
- [x] Complete API 37/minSdk 30 compatibility assessment and changes.
- [x] Update user/developer/release documentation; emulator screenshots remain pending.
- [x] Run source-final host JVM, static migration/backup/localization, lint and instrumentation compilation checks.
- [ ] Execute populated v11 migration/backup/repository instrumentation and Activity UI acceptance on a disposable emulator.
- [ ] Verify release artifact identity, signature, upgrade safety, and CI.

### Phase 5 — GitHub integration and prerelease
- [ ] Synchronize one Issue/checklist and Project state when GitHub access is available.
- [ ] Push branch, open one PR, pass CI, and merge normally preserving commits.
- [ ] Publish and verify prerelease v0.14.0.

### Verification and completion status
- Status: IN PROGRESS; domain, persistence, and UI implementation exists, but final verification and review remain open.
- Preflight: active isolated worktree branch `feature/gould-0.14`, based on `b436294ea02f76e0f4fcef33299e7df9210c3751`; initial status was clean. GitHub CLI reports the saved `stokonn` token is invalid. No remote mutation has been attempted.
- Current implementation: new JVM-targeted KMP domain module; Room schema 12 with additive 11→12 migration; backup format v7/journal schema12; Activity navigation/UI; manual activity; weekly goals/Freeze and cosmetic ownership/reward. Discover and admin implementations remain present.
- Verification so far: final mandatory clean RELEASE host gate passed in 328s after the last app-source edit: debug build, 403 JVM tests (0 failures/errors/skips), instrumentation APK compilation, lint and five policy/static checks. Localization 1,281 EN/CZ, migration DDL v1–v12, backup fixtures and history-paging checks passed. A final bounded unsigned R8 release build passed in 310s and produced `app-release-unsigned.apk` (9,131,062 bytes; SHA-256 `4d9e407a28d4c9d48e8865b3bf3723b8d60a1d38652db827257db53c4976d7fb`), metadata package `com.opus.piano`, code16 / `0.14.0-dev`, minSdk30, target37. It is unsigned and not release evidence. Native instrumentation and visual UI inspection remain unexecuted.
- SDK assessment: API37 platform/AGP9.1.1/Gradle9.3.1 compile successfully; target37 and minSdk30 configured. minSdk30 drops update eligibility for API26–29 users while leaving their installed local data intact; this compatibility cost is documented. Large-screen and widget API37 runtime acceptance remains pending.
- Current blockers: emulator startup is blocked by userdata disk capacity; a proposed AVD data relocation was rejected by automatic review and will not be retried by another route. GitHub saved credentials are invalid. Production signing, signed upgrade, screenshots, PR, merge, tag and release verification are incomplete.
- Next: review final diff, make local logical commits, and stop native acceptance/GitHub integration at the emulator and invalid-auth blockers.
