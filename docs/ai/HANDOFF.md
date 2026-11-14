# Gould current maintenance handoff

## Current release handoff — November 14, 2026

Gould 0.14.0 has been published as GitHub pre-release `v0.14.0` from merged
commit `b507aaeb47187ac2280ecce2873bdf05cf2b40ee`. See the [current status](GOULD-CURRENT-STATUS.md)
and [release verification](../releases/GOULD-0.14.md). Do not publish another
release or retag this version. Signed in-place upgrade verification and visual
emulator review remain unexecuted. The maintenance checkpoint below is dated
historical context; its Gould 0.14 planning statements are superseded.

Start with [the current Gould status](GOULD-CURRENT-STATUS.md), then read the root [AGENTS.md](../../AGENTS.md) and [legacy cleanup policy](../maintenance/LEGACY-CLEANUP.md).

## Current task

The repository maintenance pass is tracked in the documentation-only PR created from the verified `main` baseline `c8980539bb76408d6f1284e4b3a885cdb71af6fc`. Review its exact branch/commit and CI before any integration. Do not merge it without explicit owner approval.

This pass updates current status and maintenance handoff records only. It does not alter application/backend code, workflows, release assets, tags, branches, protected worktrees, signing material or user data. The CI results recorded in the status document belong to the exact base commit, not automatically to a later documentation commit.

## Protected maintenance boundaries

- Preserve all seven unresolved historical worktrees and their private/ignored evidence. No deletion or replacement is authorized.
- Preserve the six blocked historical branches and every release tag/asset.
- Do not modify the independent PMP repository.
- Do not run destructive cleanup, `git reset`, `git clean`, force operations, or `pmp init`.
- Do not install, launch, test or use ADB against the owner's physical phone. Any future ADB work must target one explicitly identified disposable emulator.
- Public Wiki synchronization is still unverified. `docs/wiki/` remains the canonical source; inspect the actual Wiki before attempting a non-destructive sync.

## Gould 0.14 boundary

No 0.14 scope or implementation is started by this maintenance pass. Begin 0.14 separately with Phase 1 architecture, compatibility, visual, test and acceptance planning. Keep application implementation out of Phase 1 and wait for explicit approval before Phase 2.

created by stokonn with love <3
