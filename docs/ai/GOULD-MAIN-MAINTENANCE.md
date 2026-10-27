# Gould main integration and final maintenance handoff

Current repository: **https://github.com/stokonn/Gould**, repository ID `1421247418`. Canonical checkout: `~/Projects/Opus`, branch **main**. The owner explicitly authorized normal verified main integration and separately superseded the earlier RC retention policy: remove only the obsolete RC GitHub prerelease after private backup, preserving its tag and all Stable releases.

## Main integration and verification

Main was fast-forwarded from `1c943dc925231a3725a0e9d9aa6b3e34d19e445a` to the accepted maintenance commit `bf837f2e04d12a5e8c277b7f1db76af8fa3e1a1d` through a non-forced, expected-head-guarded update. No unique changes were overwritten. Local main and origin/main were reconciled to the confirmed remote state. Later main commits update current references, audit documentation and administrative tooling; use `git rev-parse HEAD` and the live main ref for the exact metadata commit.

The administration receipt `.gradle/final-github-administration/report.json` was read and independently checked against the live API: rename complete, independent remote clone passed, and both exact-bf837f2 maintenance CI runs succeeded (quality `40556364094`, backend `40556364109`). The subsequent main push started main quality/backend verification; inspect the exact current main runs rather than treating prior branch runs as new main execution. The final session receipts are private under `.git/portability-audit/`.

The owner-executed host report `422d1273275945fba6a8c537bd7c1002` passed **19/19 checks**, **348 JVM tests / zero failures, errors or skips**, Android build, instrumentation compilation, lint, backup/migration/localization/paging/security, encrypted-backup, locked npm installation, PostgreSQL/workerd and Android HTTP integration. All **9,574** recorded source hashes matched when validated; **2,463 application/backend/contract inputs** remain unchanged through metadata cleanup. No new native instrumented or physical-phone execution is claimed. The final main CI must be checked live for its exact SHA.

Application ID/namespace `com.opus.piano`, `opus.db`, personal schema 10, catalogue schema 1, backup format 5 and older readers, all stored/API identifiers and signing configuration remain unchanged. Trusted certificate: `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`. The verified debug APK hash remains `1c4def749e3bec0730bcfa7dc2da7c4546e96c1a3e56d8b935694a12a7deaf97`. No new application release or Gould 0.12 feature was created.

## Branch and worktree disposition

All 33 pre-deletion remote branches were audited against integrated main, existing independent bundle coverage, source links, PRs, releases and worktrees. The original history bundle checksum and integrity passed (101 refs). Public Issue #31 and all 217 comments were inspected for historical branch links. [FINAL-BRANCH-AUDIT.md](../maintenance/FINAL-BRANCH-AUDIT.md) classifies **23 KEEP, four SAFE TO DELETE and six BLOCKED / NEEDS OWNER DECISION**. The four reviewed deletion candidates are pinned in `docs/maintenance/BRANCH-DELETION-PLAN.json`; execution subsequently passed fresh dependency/head checks and removed only those four heads. Historical source-link branches are retained even when merged. Unique/unmerged work is not blindly merged.

Nine worktrees were previously safely retired. The remaining seven historical worktrees were re-audited by file: **6,769 non-generated file hashes** are recorded privately in `.git/portability-audit/remaining-worktree-file-audit.json`. All their commits remain under retained local branches; no commit is known to exist only in a worktree. Independent coverage of their unresolved ignored/private files has not been established, so originals remain protected:

| Worktree | Exact retention blocker |
| --- | --- |
| `Goldberg-011-Discover` | Three original Discover-review artifact files plus historical outputs; do not discard unique APK evidence without disposition. |
| `Goldberg-Stage6-Cloud` | Original stable/delivery artifacts, private probe environment and canonical emulator state; also the parent of the nested RC worktree. |
| nested `.gradle/goldberg-0.11-rc` | Private existing-signing init configuration and historical artifact/test records. All published RC assets are now independently backed up, but the remaining local configuration is not discarded. |
| `Goldberg-Stage7B` | 2,090 untracked files, 1,358 ignored planning/evidence files and manual-acceptance emulator/artifact state. |
| `Goldberg-Stage8` | Private PMP, original diagnostics/security-incident records, captures and emulator state. |
| `goldberg-c2-spike` | Private ignored prototype runtime state; unique prototype commits remain referenced. |
| `/tmp/goldberg-stage6-android-local` | Original host/instrumentation/publication/JVM artifacts and local SDK configuration. |

The owner previously confirmed historical sessions/services had saved and closed or relocated. This executor still cannot inspect all host processes; that limitation is recorded rather than represented as a fresh host observation. No further worktree is removed until private originals/backups are fully accounted for. These worktrees are not source/build dependencies of Opus. Original owner untracked files, private PMP, protected backups and standalone PianoApp Opus remain untouched.

## Actions and RC cleanup — executed and reviewed

Read [FINAL-ACTIONS-AND-RC-AUDIT.md](../maintenance/FINAL-ACTIONS-AND-RC-AUDIT.md) for the complete API inventory, historical-record distinction, managed-workflow review and release deletion evidence. Three repository workflow definitions remain. The full API lists four active registrations: those three plus the existing GitHub-managed Copilot cloud-agent integration. No obsolete registration remains; no historical workflow runs were deleted. The old screenshot YAML is removed from main and its original successful historical run remains available.

The four backed-up SAFE branch candidates were deleted; 29 remote branches remain (23 KEEP, six BLOCKED/retained). Exact heads and reasons are recorded in the branch audit. Stable and all 14 tags are unchanged.

The obsolete RC prerelease ID423537826 and its three assets were deleted after complete private backup. Direct API reads confirm 404 for the prerelease and all asset endpoints. Its Git tag remains object4cb0059e36ed9e918ae1408e14f0c84a28ac2402. Stable ID425356546 and every other release/asset identity are unchanged. Private backup is `.gradle/private-release-backups/v0.11-rc.1/verified-backup/`, with exact original asset names in `assets/` and a distinct `BACKUP-SHA256SUMS.txt` manifest.

The original controller receipt conservatively flagged its unknown managed Copilot row; manual review identified that exact existing integration as KEEP. The original receipt remains unchanged, with the review recorded separately. There is no need to rerun destructive administration just to change that flag. Current code recognizes only that exact managed integration; unrelated unknown security/managed workflows remain blocked.

## Remaining blockers and readiness

**Verified development baseline:** canonical Gould main, all application/backend/database/signing inputs unchanged from accepted host source, host 19/19 checks and 348 JVM tests, independent Git/source/remote clone checks, and required quality/backend CI on integrated main. Final metadata commit CI is checked against its exact SHA.

**Maintenance remains partial:** seven historical worktrees contain private signing/runtime/PMP/incident/capture records whose independent backup/encrypted custody has not been established. The existing backup explicitly records incomplete private coverage; do not treat its source-history bundle as a backup of those files. Six blocked historical branches remain preserved for unresolved work or backup coverage; the maintenance branch is already integrated but absent from the older verified bundle. Neither condition is a missing application build dependency, but neither authorizes deleting unresolved originals.

Repository-wide maintenance review and the read-only independent OpenCode workflow are documented in [REPOSITORY-AUDIT.md](../maintenance/REPOSITORY-AUDIT.md) and [INDEPENDENT-REVIEW.md](../maintenance/INDEPENDENT-REVIEW.md). OpenCode startup is blocked by sandbox listen EPERM; no independent review has executed. The existing host architect agent/PMP wrapper command is ready without permission/configuration changes.

Next: read the exact current main CI, final private receipts and host OpenCode findings; arrange owner-approved encrypted custody/recipient or explicit per-file disposition for unresolved records, then independently verify backups before any further retirement. Source/toolchain/USB instructions are in PORTABILITY.md. **Do not declare full maintenance complete while these protected-data blockers remain. Gould 0.12 features have not started and require their own owner-approved scope.**

## PMP

Standalone `~/Projects/project-memory-protocol` remains separate and unchanged. Keep project UUID `ac3a7942-cde5-4e79-8a43-e1a0d2c7df81`, private state/history and original recovery records. Public handoff and private reported facts distinguish successful host/CI checks and resolved administration from unresolved private-data custody. Keep `.pmp/` and `.pmp.yml` out of public commits; `github: null` remains truthful. Use `pmp check`, `resume`, `history`, `verify` and a private snapshot after final source reconciliation. No native PMP evidence is fabricated from historical results.

created by stokonn with love <3
