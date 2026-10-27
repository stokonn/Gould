# Final Actions, branch and RC execution audit

Repository: https://github.com/stokonn/Gould, ID `1421247418`. The owner executed the reviewed controller on the host. The agent directly read `.gradle/final-maintenance-administration/report.json`, verified its backup hashes and independently checked live release/tag/branch/run API outcomes. The original receipt is preserved unchanged as `original-controller-report.json` beside it; the separate private review is `.git/portability-audit/final-administration-review.json`.

## Complete workflow inventory

The host fetched the complete paginated Actions workflow API with no state filter, including disabled registrations. Both final main and maintenance source directories were compared. The first full API capture was after main integration; it is not represented as a pre-integration API snapshot.

| Workflow / registration | ID | Final decision |
| --- | --- | --- |
| `quality.yml` — Goldberg quality checks | 387515817 | KEEP: Android build, JVM tests, instrumentation compilation, lint, migration/backup/security checks. Historical source pin requires its byte-exact definition; its name does not make it obsolete. |
| `community-bff.yml` — Gould community BFF integration | 396166235 | KEEP: PostgreSQL/workerd, backend/security regressions and fail-closed packaging. |
| `stage5-emulator.yml` — Gould Android emulator verification | 391056307 | KEEP: current dispatch-only native, seeded-upgrade, network and restart acceptance. Historical filename, essential coverage. |
| `dynamic/copilot-swe-agent/copilot` — Copilot cloud agent | 394495129 | KEEP: existing GitHub-managed agent integration, not a repository YAML pipeline or an obsolete Goldberg stage job. No task was started and its settings were not changed. |

Before source cleanup there were **four repository YAML definitions**. The obsolete five-tab `stage5-screenshots.yml` was removed from final main, leaving **three repository definitions**. The complete API inventory recorded **four registrations before and four after administrative review, all active; zero disabled entries were returned**. **Final active workflow count: four = three required repository workflows plus one managed Copilot integration.** No obsolete active registration remains in that inventory, so no workflow-disable mutation was necessary.

The controller conservatively reported `complete:false` because the managed, non-YAML Copilot entry was unknown to its initial classifier. Manual review resolved that exact path as KEEP using its actual API identity and [GitHub's cloud-agent documentation](https://docs.github.com/en/enterprise-cloud%40latest/copilot/concepts/about-assigning-tasks-to-copilot). The classifier now recognizes that exact integration; arbitrary unknown managed/security paths remain blocked. The original conservative receipt was not edited or relabelled as an exit-zero run.

Historical records/runs were preserved. [Screenshot run 39576173002](https://github.com/stokonn/Gould/actions/runs/39576173002), named “Goldberg Stage 5 screenshot capture (both themes)”, still exists with its original successful result. Its old name may remain visible through historical runs; it is absent from the current registration inventory and main source. **Historical workflow runs deleted: zero.** Essential coverage, action pins and contents-read-only permissions remain intact. See [GitHub's workflow API](https://docs.github.com/en/rest/actions/workflows) for the distinction between listing/disabled states and workflow execution history.

Both main quality/backend runs passed at `bf837f2`, `6d372a3` and the checksum-safety fix `e98f709` (quality40564403004, backend40564402861). Final audit-only updates also require exact-current main CI readback; live results and the final SHA are recorded in the session handoff/private verification receipts rather than inferred from old runs.

## Remote branches

The four reviewed, independently backed-up merged heads were deleted with ordinary Git commands after fresh head/protection/ancestry/PR/release/worktree/reference checks:

- `feature/goldberg-0.11-stage-6-api-v1-local`
- `implement/goldberg-0.11-stage-6-7-9-local`
- `maintenance/legacy-cleanup-2026-10-18`
- `verify/goldberg-0.11-stage-6-12-local`

Live API readback: **29 branches remain: 23 KEEP, six BLOCKED/retained**. Their detailed reasons and the preserved pre-deletion review are in [FINAL-BRANCH-AUDIT.md](FINAL-BRANCH-AUDIT.md). No force push/history rewrite or tag deletion occurred. The existing independent history bundle checksum and integrity were verified before deletion; it is a separate same-filesystem archive, not a claim of off-device disaster recovery.

## Obsolete prerelease removal

Deleted only **GitHub prerelease `v0.11-rc.1`, ID423537826**, and its three associated assets (IDs675606830,675606828,675606837). Direct API readback returned **404 for the prerelease and all three asset endpoints**, and the release collection now contains 13 other releases. No release was recreated or published.

**Git tag `v0.11-rc.1` remains**, object `4cb0059e36ed9e918ae1408e14f0c84a28ac2402`. All 14 tag refs are unchanged. **Goldberg 0.11 Stable remains release ID425356546, prerelease=false**, with original APK digest `290a15e7b15da701b8f0a40e98bf8b7c1f7cc00ce487504dcb6d4ab2e11b40d6`. IDs/names/sizes/digests for every other release asset were compared and are unchanged.

Complete private backup: `.gradle/private-release-backups/v0.11-rc.1/verified-backup/`. Original assets retain their exact names under `assets/`; metadata, byte-exact notes, preserved tag refs and before-deletion metadata are checksum-covered by the separate `BACKUP-SHA256SUMS.txt`. The directory/files are owner-only. A checksum-name collision was caught and corrected before deletion; the backup used for deletion was reverified afterward. Preserve this backup and transfer it separately in secure custody when needed; the source-only USB archive excludes `.gradle`.

## Remaining maintenance boundary

The complete Actions audit and RC operation are resolved. **Overall maintenance is not declared complete:** seven historical worktrees retain private/ignored originals with unestablished independent custody/backup coverage, and six unique historical branches remain deliberately unmerged. Canonical source is on main, compiles/tests successfully and does not depend on those worktrees. Do not erase protected records to make directory counts smaller; do not infer Gould 0.12 feature scope from this audit.

created by stokonn with love <3
