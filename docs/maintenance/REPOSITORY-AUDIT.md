# Gould repository maintenance audit

Continuation after administration outcome `12746b8`, October 27, 2026. This audit inventories the complete tracked repository and checks maintenance boundaries; it is not an exhaustive semantic/security review of every historical source file. Independent OpenCode review remains a separate gate in [INDEPENDENT-REVIEW.md](INDEPENDENT-REVIEW.md).

## Findings and preservation

- Canonical source is on main in `~/Projects/Opus`, origin `https://github.com/stokonn/Gould.git`. Git common directory is the local `.git` directory, with no object alternates, tracked symlinks, submodules or LFS filters. The canonical repository retains historical refs/stashes/objects; copied development repositories must omit linked-worktree administration records, as documented in PORTABILITY.md.
- The 9,582 tracked files at `12746b8` include complete Android source/tests/schemas/assets, backend composition, contracts, required historical-named spike modules, tools and source-bound historical evidence. No experimental branch integration or feature implementation was performed. Owner-untracked files stay outside commits and are included in the private portability inventory.
- All 2,463 application/backend/contracts/spikes inputs match the accepted 19-check host source. Android application/signing/database identifiers and migrations are unchanged. The maintenance edits affect administration safety and documentation only.
- No original-computer absolute path was found in the scanned executable/configuration scope (Android/Gradle, services, scripts and workflows, excluding results and test fixtures). Historical result files contain original paths; retain them as provenance. The documented historical direct-cache compiler helper is not the supported build path. The independent copy checks repo-relative configuration without copying SDKs/caches.
- Filename review found no tracked signing keystore or private environment file. A high-confidence private-key/GitHub-token/AWS-access-key pattern scan of tracked files up to 2 MB returned no matches. This bounded scan does not prove absence of every possible secret. Existing private PMP, signing adapters, credentials and RC backups remain ignored/outside public commits; no secret values were printed.
- All three workflow YAML files parse successfully. Quality/backend run on push and pull requests; emulator checks are dispatch-only. Each has contents-read permissions. The byte-pinned historical quality definition is intentionally retained. Actions registrations and historical runs are separately reviewed in FINAL-ACTIONS-AND-RC-AUDIT.md; no further deletion is requested.
- Local migration checks 1–9 to 10, three historical backup fixtures, 1,103 EN/CZ resources, history paging, canonical agent policy, historical evidence integrity and six maintenance administration regressions passed. These checks do not imply fresh emulator execution. Host acceptance and exact-current main CI remain distinct receipts.
- Git integrity passes. Eight worktrees remain, including canonical Opus; seven retain protected originals. All historical worktree commits have retained refs. Six unique remote branches are intentionally retained; no unfinished work is merged merely to reduce counts.

## PMP integration checkpoint

The existing Gould project UUID, private history and recovery originals are preserved. The current task state is synchronized through `pmp task set`, with resolved GitHub administration facts and explicit private-data/OpenCode blockers. Native evidence is created only by checks actually executed through `pmp verify --run`; historic host results remain attributed reports, not invented native evidence. Use private `snapshot`, `handoff`, `check`, `resume`, `history` and `verify` at the final source checkpoint. `github: null` remains accurate; this task does not install hooks, publish private memory or change the independent PMP repository.

OpenCode v2.1.4 is installed, but its service cannot listen inside this sandbox. PMP's adapter inspection certifies native runtime only for v2.0.32. The existing wrapper-based read-only host review avoids enabling unverified native hooks and preserves existing agent permissions. Independent review has not yet executed; do not treat a prepared command as a passed review.

## Remaining decisions

Private-data independent backup/custody must be resolved before retiring the seven protected worktrees. Read the owner-run OpenCode review, triage concrete findings and verify bounded corrections before declaring its gate complete. These are explicit remaining maintenance blockers. No new release, Gould 0.12 implementation, extra deletion or history rewrite is authorized by this audit.

created by stokonn with love <3
