# Gould main integration and final maintenance handoff

Current repository: **https://github.com/stokonn/Gould**, repository ID `1421247418`. Canonical checkout: `~/Projects/Opus`, branch **main**. The owner explicitly authorized normal verified main integration and separately superseded the earlier RC retention policy: remove only the obsolete RC GitHub prerelease after private backup, preserving its tag and all Stable releases.

## Main integration and verification

Main was fast-forwarded from `1c943dc925231a3725a0e9d9aa6b3e34d19e445a` to the accepted maintenance commit `bf837f2e04d12a5e8c277b7f1db76af8fa3e1a1d` through a non-forced, expected-head-guarded update. No unique changes were overwritten. Local main and origin/main were reconciled to the confirmed remote state. Later main commits update current references, audit documentation and administrative tooling; use `git rev-parse HEAD` and the live main ref for the exact metadata commit.

The administration receipt `.gradle/final-github-administration/report.json` was read and independently checked against the live API: rename complete, independent remote clone passed, and both exact-bf837f2 maintenance CI runs succeeded (quality `40556364094`, backend `40556364109`). The subsequent main push started main quality/backend verification; inspect the exact current main runs rather than treating prior branch runs as new main execution. The final session receipts are private under `.git/portability-audit/`.

The owner-executed host report `422d1273275945fba6a8c537bd7c1002` passed **19/19 checks**, **348 JVM tests / zero failures, errors or skips**, Android build, instrumentation compilation, lint, backup/migration/localization/paging/security, encrypted-backup, locked npm installation, PostgreSQL/workerd and Android HTTP integration. All **9,574** recorded source hashes matched when validated; **2,463 application/backend/contract inputs** remain unchanged through metadata cleanup. No new native instrumented or physical-phone execution is claimed. The final main CI must be checked live for its exact SHA.

Application ID/namespace `com.opus.piano`, `opus.db`, personal schema 10, catalogue schema 1, backup format 5 and older readers, all stored/API identifiers and signing configuration remain unchanged. Trusted certificate: `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`. The verified debug APK hash remains `1c4def749e3bec0730bcfa7dc2da7c4546e96c1a3e56d8b935694a12a7deaf97`. No new application release or Gould 0.12 feature was created.

## Branch and worktree disposition

All 33 current remote branches were audited against integrated main, existing independent bundle coverage, source links, PRs, releases and worktrees. The original history bundle checksum and integrity passed (101 refs). Public Issue #31 and all 217 comments were inspected for historical branch links. [FINAL-BRANCH-AUDIT.md](../maintenance/FINAL-BRANCH-AUDIT.md) classifies **23 KEEP, four SAFE TO DELETE and six BLOCKED / NEEDS OWNER DECISION**. The four reviewed deletion candidates are pinned in `docs/maintenance/BRANCH-DELETION-PLAN.json`; execution requires fresh dependency/head checks. Historical source-link branches are retained even when merged. Unique/unmerged work is not blindly merged.

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

## Complete Actions audit — mandatory outstanding gate

Final main source has three workflow definitions: `quality.yml`, `community-bff.yml`, `stage5-emulator.yml`. They cover Android/JVM/lint/migration/backup/security, backend/PostgreSQL/workerd, and dispatch-only instrumentation/upgrade/network/restart respectively. The old five-tab screenshot YAML was removed. The historical quality display name remains because its source is byte-exact pinned by the evidence verifier; its name alone does not make it obsolete.

**A complete workflow-registration inventory, including disabled records, has not yet been obtained through this executor.** Its connector rejects `/actions/workflows` as an unsupported endpoint, while shell Git/gh has no DNS access. The live source-definition count is not a claim about active API registrations. Do not declare workflow cleanup or maintenance complete until the full inventory, disable results and exact-current main CI are verified.

The reviewed host controller below enumerates every workflow registration, compares both main and maintenance definitions, retains essential/defined workflows, checks ruleset/protection dependencies, and disables only registrations whose YAML is absent from both canonical definitions. Unknown managed/security workflows are blocked for review. Historical registrations may remain visible as disabled records; **no historical workflow runs are deleted**.

## Obsolete RC prerelease — backed up, deletion pending

Live API identity: prerelease **`v0.11-rc.1`, ID `423537826`, prerelease=true**. Stable is the distinct **`v0.11`, ID `425356546`, prerelease=false**; its original APK digest is `290a15e7b15da701b8f0a40e98bf8b7c1f7cc00ce487504dcb6d4ab2e11b40d6`.

Complete private backup: `.gradle/private-release-backups/v0.11-rc.1/`, owner-only directory/files. Metadata, release notes, Stable-before metadata, preserved tag refs and all three RC assets are present with SHA-256 checksums. Each asset's bytes and size matched live GitHub metadata:

- `Goldberg-v0.11-rc.1.apk`: `c5662d8ead2b175fac0be09145776e84936c2b8f794c85b967f71aaab1938bb5`.
- `artifact-receipt.json`: `451b507179100367755c03ae1b3cea7d9ae447d7ab48bd7a8952d6e2959176ef`.
- `SHA256SUMS`: `245c92c93b967b55fc62744da6b9093d2af67242dec6a35140529e39a066a20a`.

**Deletion has not been executed.** Release-delete and workflow-disable operations are not exposed by the connected tools. Do not claim absence until the API returns 404 for the prerelease and its three assets, its tag remains unchanged, and all other releases/assets are unchanged. Preserve the private backup permanently; the source-only USB archive excludes `.gradle`, so transfer this backup separately and securely when required.

## Minimum remaining administrative action

After the current reviewed source is saved on main, run in a normal authenticated host terminal:

```sh
cd "$HOME/Projects/Opus"
python3 scripts/finish_maintenance_admin.py --apply
```

This single controller performs the full workflow API audit, guarded disabling, fresh checks/deletion of the four independently backed-up branch candidates, and deletion of only release ID 423537826 after re-verifying its backup. It preserves all tags, Stable/other releases and historical workflow runs, and records before/after inventories and hashes in `.gradle/final-maintenance-administration/report.json`. It stops on ambiguous security dependencies or changed candidate/backup state. It does not build an APK, publish a release, rewrite history or delete local data. Four workflow-classification regressions and the existing five deletion-guard regressions passed locally.

Return that report path/outcome. The next agent must validate actual API outcomes, final active workflow count, main CI and remaining branch count; update this handoff and PMP; and continue preserving unresolved local records. **Readiness for Gould 0.12: BLOCKED on complete Actions audit/admin receipt and protected-data disposition.** No feature scope is inferred by maintenance.

## PMP

Standalone `~/Projects/project-memory-protocol` remains separate and unchanged. Keep project UUID `ac3a7942-cde5-4e79-8a43-e1a0d2c7df81`, private state/history and original recovery records. Public handoff and private reported facts distinguish successful host/CI checks from pending administration. Keep `.pmp/` and `.pmp.yml` out of public commits; `github: null` remains truthful. Use `pmp check`, `resume`, `history`, `verify` and a private snapshot after final source reconciliation. No native PMP evidence is fabricated from historical results.

created by stokonn with love <3
