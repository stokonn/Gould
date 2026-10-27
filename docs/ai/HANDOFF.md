> Latest review continuation: interrupted Actions/RC outcome committed at `12746b8`; no destructive operations repeated. Repository-wide maintenance inventory is in [REPOSITORY-AUDIT.md](../maintenance/REPOSITORY-AUDIT.md). Independent OpenCode review is pending host execution because sandbox service listen returns EPERM; use [the existing read-only workflow](../maintenance/INDEPENDENT-REVIEW.md). PMP task facts are synchronized; private-data custody remains unresolved.

# Gould — current handoff

Repository: https://github.com/stokonn/Gould (ID 1421247418). Canonical checkout: `~/Projects/Opus`, branch **main**. Verified maintenance commit `bf837f2` was integrated by non-forced fast-forward from `1c943dc`. Current metadata SHA is reported by `git rev-parse HEAD` and the live GitHub main ref.

Read [GOULD-MAIN-MAINTENANCE.md](GOULD-MAIN-MAINTENANCE.md) first for the complete current checklist, verification, branch/worktree audit, Actions/RC requirements and remaining blockers. It supersedes earlier maintenance handoffs.

- Goldberg 0.11 Stable remains published and protected at `v0.11` → `ab7d66ddafe0bcf2f23270682116ee2e02f828eb`.
- Gould 0.12 has one planned development stage; no feature implementation has started.
- Accepted host verification: 19/19 checks, 348 JVM tests / zero failures/errors/skips, Android build/lint/instrumentation compilation, backup/migration/security and synthetic backend/HTTP/workerd checks. All 9,574 recorded hashes matched; application/backend/database/signing inputs are unchanged by subsequent metadata cleanup.
- Exact maintenance CI: quality40556364094 and backend40556364109 passed at bf837f2. Final main CI must be verified at its exact SHA.
- Complete source/Git portability checks and independent remote clone passed. Private configuration and signing keys require secure separate restoration.
- Nine obsolete worktrees were retired with preservation verified. Seven historical worktrees retain unresolved private/evidence state; their commits remain referenced. Do not discard them.
- Branch audit: 23 KEEP, four SAFE TO DELETE, six BLOCKED; independent history bundle verified. Execute only fresh-rechecked, backed-up deletion candidates.
- Actions/RC administration is resolved: four backed-up branches and the obsolete RC prerelease/assets were deleted; all tags/Stable are unchanged. Full API inventory: three required YAML workflows plus one existing managed Copilot integration, four active total. Historical runs remain. Overall maintenance remains **BLOCKED** on protected private-worktree custody/disposition.
- The obsolete prerelease v0.11-rc.1 and only its assets are deleted; its tag, all Stable releases/assets and historical workflow runs are preserved. The complete private backup is retained securely.
- Administrative receipt and live API outcomes were verified; read [the final Actions/RC audit](../maintenance/FINAL-ACTIONS-AND-RC-AUDIT.md). Do not rerun destructive administration merely to replace the conservative original managed-workflow flag.

Keep original owner untracked files, private PMP and the independent `~/Projects/project-memory-protocol` repository intact. Preserve `com.opus.piano`, `opus.db`, schema/backup compatibility and the trusted signing identity. No application release, force push, history rewrite or Gould 0.12 feature is authorized by this maintenance task.

The complete earlier handoff and chronological acceptance history remain byte-exact in Git: [bf837f2:HANDOFF.md](https://github.com/stokonn/Gould/blob/bf837f2e04d12a5e8c277b7f1db76af8fa3e1a1d/docs/ai/HANDOFF.md). Offline: `git show bf837f2:docs/ai/HANDOFF.md`. No historical checkpoint data was discarded or copied into a second active archive.

created by stokonn with love <3
