# Gould 0.14 — implementation checklist

## One development stage: Activity and weekly progression

Purpose: add a deterministic Activity experience and weekly progression while preserving the existing personal journal and preparing a clean shared Kotlin domain boundary.

Scope: shared-ready domain rules; Android/Room persistence and backup; Activity navigation/UI; historical goals, Beta Week, manual activity, weekly streak and Freeze; cosmetic ownership/rewards; temporary navigation hiding; SDK compatibility; documentation, verification, integration, and prerelease.

### Phase 0 — Preflight
- [x] Confirm canonical repository identity and active worktree.
- [x] Confirm starting commit, branch state, and clean status.
- [x] Select one 0.14 feature branch.
- [x] Confirm GitHub authentication and inspect remote PR/release/issue state. PR #50 is open and draft; no merge/tag/release action authorized.
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
- [ ] Execute populated v11 migration/backup/repository instrumentation and Activity UI acceptance on a disposable Android 17/API 37.0 emulator. The Android instrumentation test exists but has not executed; the first emulator run completed zero tests after Android system services failed during APK installation.
- [ ] Verify release artifact identity, trusted production signature, signed in-place upgrade, and PR CI.

### Phase 5 — GitHub integration and prerelease
- [ ] Synchronize one Issue/checklist and Project state when GitHub access is available.
- [ ] Push branch, open one PR, pass CI, and merge normally preserving commits.
- [ ] Publish and verify prerelease v0.14.0.

### Verification and completion status
- Status: IN PROGRESS; domain, persistence, and UI implementation exists, but final verification and review remain open.
- Preflight/integration: active isolated worktree `feature/gould-0.14`; fetched `origin/main` at `00ff0890e76392801b481e6b803db3e1c6a8a4ee` and `origin/feature/gould-0.14` at the expected implementation commit. Inspected all 45 main-only commits; their changed files are limited to current-status/maintenance guidance, changelog and Wiki. Integrated with normal merge commit `dd1ee4f`, preserving all seven implementation commits and main history. GitHub CLI account `stokonn` authentication is restored and PR #50 was inspected; it remains open/draft. No merge/tag/release action has been taken.
- Current implementation: new JVM-targeted KMP domain module; Room schema 12 with additive 11→12 migration; backup format v7/journal schema12; Activity navigation/UI; manual activity; weekly goals/Freeze and cosmetic ownership/reward. Discover and admin implementations remain present.
- Verification so far: final mandatory clean RELEASE host gate passed in 328s after the last app-source edit: debug build, 403 JVM tests (0 failures/errors/skips), instrumentation APK compilation, lint and five policy/static checks. Localization 1,281 EN/CZ, migration DDL v1–v12, backup fixtures and history-paging checks passed. A final bounded unsigned R8 release build passed in 310s and produced `app-release-unsigned.apk` (9,131,062 bytes; SHA-256 `4d9e407a28d4c9d48e8865b3bf3723b8d60a1d38652db827257db53c4976d7fb`), metadata package `com.opus.piano`, code16 / `0.14.0-dev`, minSdk30, target37. It is unsigned and not release evidence. Native instrumentation and visual UI inspection remain unexecuted.
- SDK assessment: Android 17/API37 preview is installable as SDK package `platforms;android-37.0`; the earlier CI package `platforms;android-37` was invalid. The quality workflow now passes with the exact package, preserving compile/target37 and minSdk30. The emulator workflow also installs API 37 successfully. minSdk30 drops update eligibility for API26–29 users while leaving their installed local data intact; this compatibility cost is documented. Large-screen and widget API37 runtime acceptance remains pending.
- Current GitHub acceptance checkpoint (2026-11-12, source `c1516a5`): PR #50 is open and draft. Required host quality run `43243161485` and repository maintenance checks passed. Emulator workflow `43243176345` failed both jobs. API37 built app and test APKs, booted Android 17 and verified QEMU emulator `emulator-5554`, but system services became unavailable during APK install (`Broken pipe`, missing `activity` service); zero tests ran. API36 completed a seeded v8→12 app upgrade and verified the original data before the verifier's table-set assertion failed. The verifier assumed no schema-added tables and had not allowed the three schema-9 catalogue tables, the schema-11 badge notice table, or schema-12's seven Activity/cosmetic tables. Corrections now enumerate those expected additions by target schema; API37 uses the previously accepted `swangle` software renderer after hosted SwiftShader instability. These changes are uncommitted pending repeat workflow validation. The dedicated `ActivityMigrationTest.populatedV11UpgradePreservesPersonalDataAndAddsEmptyActivityTables` covers populated v11→12 and checks protected data, empty new tables and foreign keys; it has not run on an emulator. Artifact download returned HTTP 503 from GitHub artifact storage due to account egress limits.
- Production signing, signed in-place upgrade, runtime UI/theme/locale/accessibility acceptance and screenshots remain incomplete. PR #50 must stay draft. No merge, tag or release is authorized. No physical phone or `/home/stokonn/Projects/Opus` was accessed or modified.
- Local commits on `feature/gould-0.14`: `181c4fc` shared domain; `fed0399` Room and backup; `576f30a` Activity UI/navigation; `1a416b5` cosmetic catalog; `c643208` SDK37 CI; `862ba64` docs/checkpoint. The tree was clean after these commits. Final RELEASE audit records are at `b436294-dirty`; the application/build source fingerprint is the same committed source, with documentation/checkpoint commits afterward.
- Next: validate the updated upgrade harness and API37 `swangle` renderer, commit and push the corrections, then dispatch the full emulator workflow on the exact resulting commit. If the new run passes, inspect test counts and runtime evidence before updating PR #50's description. Continue investigating trusted production signing without exposing or replacing credentials. Keep PR draft and stop short of merge/tag/release absent explicit approval.
