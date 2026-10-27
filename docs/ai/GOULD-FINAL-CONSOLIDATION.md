> Current main maintenance: the repository is **stokonn/Gould**, the accepted history is integrated into **main**, and host 19/19 plus exact-source maintenance CI passed. Full workflow-registration audit/cleanup and the privately backed-up RC prerelease deletion remain mandatory pending administrative API access. Read [GOULD-MAIN-MAINTENANCE.md](GOULD-MAIN-MAINTENANCE.md) first. Earlier statements below are preserved history, not current repository identity or approval scope.

> Host acceptance is now validated: all 19 checks passed at `0ffebeb`, including 348 JVM tests and local backend/encrypted-backup checks. Nine obsolete worktrees, including Gould-Preparation, have been retired with preservation verified. Current blocker is GitHub administration access, not the build. Read [HOST-ACCEPTED-CLEANUP.md](../maintenance/HOST-ACCEPTED-CLEANUP.md) first; older entries below are historical. Actual repository remains `stokonn/Goldberg` until the approved rename is confirmed.

# Gould final consolidation — active handoff

Continue the existing `maintenance/gould-portability` branch in `~/Projects/Opus`. Read [AGENTS.md](../../AGENTS.md), [current authorized scope](../maintenance/FINAL-CONSOLIDATION.md) and [portability/setup instructions](../maintenance/PORTABILITY.md). This handoff supersedes earlier statements that preparation is uncommitted or cleanup/rename lacks owner authorization. Their original evidence remains historical.

## Saved work and identity

- Baseline: verified current-main publication receipt `1c943dc925231a3725a0e9d9aa6b3e34d19e445a`. Prepared rebranding and portability changes committed as `b87d238a51ded7b2b7e33ecbb262827af7a655b9`; subsequent maintenance commits contain workflow audit, final verification runner, policy and PMP/handoff updates. Use `git log -3 --oneline` for current exact commits.
- Actual repository remains **https://github.com/stokonn/Goldberg**, ID `1421247418`. Renaming it to `stokonn/Gould` is explicitly approved after required project/CI verification. Local directory remains Opus. No remote URL is changed before the rename succeeds.
- Goldberg 0.11 Stable is published and immutable: `v0.11` → `ab7d66ddafe0bcf2f23270682116ee2e02f828eb`. Preserve all 14 tags, original APK/assets and complete history. No new application release or Gould 0.12 features were implemented; 0.12 still has one planned development stage.
- Application ID/namespace `com.opus.piano`, database `opus.db`, migration/backup implementations, API/storage identifiers and signing configuration remain unchanged. All **182 current Android main/build input files** were compared byte for byte against the independently verified preparation tree and match.
- Original owner `RELEASE_NOTES.md`, `START_HERE_PROMPT.txt`, stage-4 checkpoint archive/visual references and `render_stage7b.py` remain untracked and preserved. They were not silently published or discarded. Two preservation stashes remain available. Private PMP is ignored by Git. Host session metadata from the original checkpoint is preserved privately, rather than committed publicly.

## Verification and workflow audit

Migration DDL 1–9→10, historical backup fixtures, localization (1103 EN/CZ resources), paging, canonical policy, historical Stage 6 evidence verification and five private-runtime security tests passed in this continuation. Changed Python/shell syntax, credential-pattern review and Git whitespace checks passed. These are fresh host/static results; the prior 348-JVM/native/upgrade acceptance belongs to its original source/run.

All four workflow YAML files parse. Contents permission remains read-only; all action references retain full SHA pins, nine local script references exist, and **22 embedded Bash steps** pass syntax checks. No triggers or permissions widened. `community-bff.yml` and `stage5-emulator.yml` have Gould display names; `quality.yml` retains its byte-exact historically pinned Goldberg name after independent-copy verification caught the name change; the latter still provides essential current instrumentation/upgrade/network/restart coverage despite its historical filename. The dispatch-only screenshot workflow targets an older five-tab layout and is documented as historical, awaiting verified replacement. **Removed: zero; retained: four; modified: two.** Exact-commit GitHub CI execution is not yet verified.

The original preparation APK remains byte-identical (`1c4def749e3bec0730bcfa7dc2da7c4546e96c1a3e56d8b935694a12a7deaf97`); fresh local `aapt`/`apksigner` checks confirm `com.opus.piano`, `0.11-dev`, code 13 and trusted certificate `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`. This is verification of the retained artifact, not a new Opus APK or new runtime upgrade result.

## Execution blocker and minimum owner action

The only shell executor exposed by this session is sandboxed. One attempt to run the owner-authorized Gradle gate with previously approved unrestricted execution was rejected by the tool policy: `sandbox_approval: false`, so escalated permissions cannot be requested. There is no exposed alternate local/cloud shell. The known restricted-socket/DNS/GPG commands were not repeatedly retried.

Consequently no fresh Android compilation, JVM suite, instrumentation compilation/execution, lint, npm dependency installation, PostgreSQL/workerd acceptance, seeded runtime upgrade or exact-commit CI pass is claimed. The earlier independent-copy failures were socket creation denied, npm `EAI_AGAIN` and GPG-agent startup failure (three encrypted-backup errors out of six tests). Tests and security gates were not weakened.

The minimum owner action is to run the reviewed aggregate host verifier in a normal, unrestricted terminal:

```sh
cd "$HOME/Projects/Opus"
python3 scripts/verify_consolidation.py
```

Share only the report location/outcome, not credentials. Logs and exact source/commit hashes are saved under `.gradle/final-consolidation/<run>/report.json`. The runner uses no ADB, publication or destructive cleanup. Required tools/private signing restoration and the pinned local PostgreSQL image are documented in PORTABILITY.md. A passing host report does not replace exact-commit CI or the required seeded-emulator upgrade acceptance.

## Cleanup and GitHub disposition

Opus is approximately **1.19 GB allocated**, after the previously approved removal of five historical emulator/image directories. Protected backups, private configuration, evidence and standalone PianoApp Opus were preserved. No additional development checkout or preparation tree was created. Temporary independent verification copies are not retained as development directories.

**Remote branches: zero removed, 33 retained.** Fresh connector enumeration and comparison confirmed main at `1c943dc`; exact compare metadata was available for 32 branch heads. PR #40 additionally reports catalogue-licence branch head `b0d7c4b2c56a663a4e841cf1864c6f10899212a9`, so all 33 heads can be identified, while that branch's main-ancestry comparison remains unresolved. Open review PRs #39/#40 retain their proposal branches. [Branch review](../maintenance/REMOTE-BRANCH-REVIEW.md) contains the historical ancestry candidates; current private audit receipts retain exact heads and unresolved reasons. No branch is deleted merely for being merged while release/documentation/activity dependencies or required acceptance remain unresolved.

**Worktrees: zero removed, 17 registered including Opus.** Their unique commits remain referenced. Gould-Preparation remains intact because its newly committed integration has not passed fresh independent Android acceptance. Other worktrees still need private/untracked content disposition and actual host activity checks; this environment cannot inspect all host sessions. Earlier owner confirmation applied to Opus/preparation sessions, not every historical service/worktree. Never force-remove a dirty worktree.

The repository rename and its dependent URL/remote changes remain **approved, not executed**, pending the verification conditions explicitly required by the owner. No main merge, release/tag/asset deletion, force push, production credential change or feature implementation occurred.

## PMP and continuation

PMP version `1.0.0a1` is installed. Existing UUID `ac3a7942-cde5-4e79-8a43-e1a0d2c7df81` and all original history are preserved. The old Opus state still used unsupported `implementation`/`in_progress` values and stale Stage 5 work. Its exact original is retained under private `.pmp/recovery-origin/pre-consolidation-state.json`; only the invalid vocabulary was initially corrected. The old task was then archived and `gould-portability-consolidation` activated through the documented CLI with `docs/maintenance/FINAL-CONSOLIDATION.md` as approved scope. Current phase/status: **verifying / blocked**, not released/completed. No historical test result was inserted as new native PMP verification evidence.

The standalone `~/Projects/project-memory-protocol` repository is separate, unchanged and must not be deleted. `.pmp.yml` remains ignored, keeps the original identity and `github: null`, and does not claim native GitHub synchronization. Private recovery storage remains outside the checkout; preserve it securely during transfer. Current public handoff/STATE/PMP-RECOVERY/canonical policy agree on the maintenance task and blocked verification. After the final commit, private snapshot/handoff and `check`, `resume`, `history`, `verify` readbacks should be recorded without sharing private context publicly.

Next executor: inspect the host verifier report for the exact commit/source, resolve actual failed gates, ordinary-push only this maintenance branch when Git transport is permitted, inspect required CI, and perform the required emulator/signing acceptance. Only then retire verified obsolete worktrees, perform verified branch cleanup, execute the approved repository rename and update current references. Keep the same maintenance task and do not begin Gould 0.12 implementation during this cleanup.

created by stokonn with love <3
