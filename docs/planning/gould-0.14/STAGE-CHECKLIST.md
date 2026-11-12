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
- Preflight/integration: active isolated worktree `feature/gould-0.14`; fetched `origin/main` at `00ff0890e76392801b481e6b803db3e1c6a8a4ee` and `origin/feature/gould-0.14` at the expected implementation commit. Inspected all 45 main-only commits; their changed files are limited to current-status/maintenance guidance, changelog and Wiki. Integrated with normal merge commit `dd1ee4f`, preserving all seven implementation commits and main history. GitHub CLI API authentication reports the saved `stokonn` token is invalid; the ordinary authenticated Git push succeeded, but no PR/Issue/Project API mutation has been attempted.
- Current implementation: new JVM-targeted KMP domain module; Room schema 12 with additive 11→12 migration; backup format v7/journal schema12; Activity navigation/UI; manual activity; weekly goals/Freeze and cosmetic ownership/reward. Discover and admin implementations remain present.
- Verification so far: final mandatory clean RELEASE host gate passed in 328s after the last app-source edit: debug build, 403 JVM tests (0 failures/errors/skips), instrumentation APK compilation, lint and five policy/static checks. Localization 1,281 EN/CZ, migration DDL v1–v12, backup fixtures and history-paging checks passed. A final bounded unsigned R8 release build passed in 310s and produced `app-release-unsigned.apk` (9,131,062 bytes; SHA-256 `4d9e407a28d4c9d48e8865b3bf3723b8d60a1d38652db827257db53c4976d7fb`), metadata package `com.opus.piano`, code16 / `0.14.0-dev`, minSdk30, target37. It is unsigned and not release evidence. Native instrumentation and visual UI inspection remain unexecuted.
- SDK assessment: Android 17/API37 is publicly installable as SDK package `platforms;android-37.0`; the old quality workflow requested nonexistent `platforms;android-37`. Updated it to the exact installed SDK package while retaining compile/target37 and minSdk30. Official Android docs confirm AGP9.1.1 supports API37 with Gradle9.3.1, Kotlin2.2.10 and JDK17. Local compilation used the installed 37.0 platform. The corrected quality and current-source emulator SDK package declarations have not yet run on GitHub; the feature branch is now pushed, but GitHub API/PR status remains inaccessible through the invalid CLI token. minSdk30 drops update eligibility for API26–29 users while leaving their installed local data intact; this compatibility cost is documented. Large-screen and widget API37 runtime acceptance remains pending.
- Current blockers: the original `/tmp` AVD could not allocate its 7.3GB userdata. The proposed copy/registry rewrite was rejected by automatic review because the destination was not proven absent; it was not retried. A separate uniquely named API37 AVD was then created at `/home/stokonn/.android/avd/gould014-api37-clean-1112.avd` after confirming its destination absent. QEMU started and checked disk/GPU/system requirements, but emitted a protected-memory `cannot unmap ptr` warning and exited before ADB registered `emulator-5556`; no instrumentation or UI test ran. The only ADB command targeted `emulator-5556` and was stopped. No other AVD or physical phone was used. GitHub CLI API authentication is invalid (the configured Git transport authentication remains able to push). Production signing, signed upgrade, screenshots, PR update, remote PR merge, tag and release publication remain incomplete. The source branch has been locally merged with main; GitHub PR #50 remains unchanged.
- Local commits on `feature/gould-0.14`: `181c4fc` shared domain; `fed0399` Room and backup; `576f30a` Activity UI/navigation; `1a416b5` cosmetic catalog; `c643208` SDK37 CI; `862ba64` docs/checkpoint. The tree was clean after these commits. Final RELEASE audit records are at `b436294-dirty`; the application/build source fingerprint is the same committed source, with documentation/checkpoint commits afterward.
- Next: when a disposable API37 emulator can boot, run `INSTRUMENTATION_CLASSES=com.opus.piano.ActivityMigrationTest,com.opus.piano.ActivityRepositoryTest INSTRUMENTATION_SERIAL=<identified-emulator> scripts/verify-integration.sh`, then complete UI/theme/locale/accessibility acceptance, signed artifact and seeded in-place upgrade gates. Restore valid GitHub CLI/API authentication before inspecting or updating PR, Issue, Project, CI, merge or prerelease state. No GitHub remote mutation has been attempted. The local normal merge is recorded as `dd1ee4f`.
