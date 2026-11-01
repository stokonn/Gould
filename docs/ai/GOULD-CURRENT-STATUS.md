# Gould current status

**Status date:** 2026-11-01
**Canonical branch:** `main` at `17d483f4f309be203dbd8dc4b220ed1e662913d3`
**Published release:** `v0.13` (`f9ba9b795ac65ddd4c0651cdc7f6bcbbb1e73de8`), Gould 0.13 — Project Renaissance, GitHub pre-release

## Current truth

- Gould 0.13 is published as a pre-release. It is not Stable.
- Documentation PR [#45](https://github.com/stokonn/Gould/pull/45) is merged
  into `main`.
- Gould remains a local-first Android application. Practice data, recordings,
  repertoire and backups remain on the device unless the user deliberately
  exports them.
- The existing backend sources, contracts and CI are retained with their
  fail-closed boundaries. Community submissions, backend OAuth and practice-data
  uploads remain disabled; the restricted read-only catalogue remains optional
  and does not change the local-first promise.
- Application identity, database identity, signing compatibility, historical
  releases, legal notices and migration evidence remain protected.

## Maintenance priorities

1. Keep current instructions, release status and historical evidence clearly
   separated.
2. Preserve protected worktrees, owner files, private artifacts and untracked
   verification evidence until their custody is explicitly resolved.
3. Review documentation drift without rewriting signed-release or checkpoint
   history.
4. Retire branches only through the separately approved, backed-up,
   expected-head procedure; no branch or worktree retirement is authorized by
   this status document.

## Instructions versus history

`AGENTS.md`, this status document and `docs/ai/HANDOFF.md` describe current
operating instructions. `docs/planning/gould-0.13/CHECKPOINT.md`,
`RELEASE-READINESS.md`, historical verification files and older Stage 6/7
documentation preserve the state and decisions of earlier work. Their
development-era statements must not override the current published-release
status, but their evidence must not be rewritten to appear current.

created by stokonn with love <3
