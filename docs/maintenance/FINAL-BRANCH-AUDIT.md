> Execution verified: the four SAFE candidates below were deleted by the reviewed host controller; 29 remote branches remain (23 KEEP, six BLOCKED/retained). All release tags remain unchanged. See [Actions and RC execution audit](FINAL-ACTIONS-AND-RC-AUDIT.md). The table preserves the reviewed pre-deletion classification.

# Gould remote branch classification

Live API audit against main `bf837f2`; 33 branches. Existing independent history bundle checksum and integrity passed (101 refs). Public Issue #31 and all 217 historical comments were checked for branch-addressed source links, in addition to local source/docs, worktree, PR and release dependencies. Do not delete historically linked heads merely because they are merged.

| Branch | Class | Unique commits | Reason |
| --- | --- | --- | --- |
| `docs/goldberg-0.11-stage-6-planning` | KEEP | 0 | Published release-wide Issue #31 source links must remain resolvable |
| `docs/goldberg-0.11-stage-6-readiness` | KEEP | 0 | Published release-wide Issue #31 source links must remain resolvable |
| `docs/goldberg-0.11-stage-7-planning` | BLOCKED / NEEDS OWNER DECISION | 1 | 1 commits outside main; do not merge unfinished work |
| `docs/goldberg-011-catalogue-source-license-review` | KEEP | 1 | open review PR head/base dependency |
| `docs/goldberg-011-privacy-operations-review` | KEEP | 1 | open review PR head/base dependency |
| `feature/goldberg-0.10.1` | KEEP | 0 | Historical rollback/compatibility reference; preserve until old release reference audit is complete |
| `feature/goldberg-0.11-database-simplifier` | KEEP | 0 | retained worktree has unresolved private/evidence state |
| `feature/goldberg-0.11-stage-3a` | KEEP | 0 | branch-addressed source/documentation links: docs/wiki/Developer-Guide.md |
| `feature/goldberg-0.11-stage-3b` | KEEP | 0 | branch-addressed source/documentation links: docs/wiki/Developer-Guide.md |
| `feature/goldberg-0.11-stage-4` | KEEP | 0 | branch-addressed source/documentation links: docs/wiki/Developer-Guide.md |
| `feature/goldberg-0.11-stage-5` | KEEP | 0 | Published release-wide Issue #31 source links must remain resolvable |
| `feature/goldberg-0.11-stage-6-api-v1-local` | SAFE TO DELETE | 0 | zero unique commits; exact head covered by verified independent bundle; no protected/worktree/PR/release/source-link dependency found |
| `feature/goldberg-0.11-stage-6-cloud` | KEEP | 0 | open review PR head/base dependency |
| `feature/goldberg-0.11-stage-7a` | BLOCKED / NEEDS OWNER DECISION | 1 | 1 commits outside main; do not merge unfinished work |
| `feature/goldberg-0.11-stage-7b` | KEEP | 0 | retained worktree has unresolved private/evidence state |
| `feature/goldberg-0.11-stage-8` | KEEP | 1 | retained worktree has unresolved private/evidence state |
| `fix/goldberg-0.11-discover-ux` | KEEP | 0 | retained worktree has unresolved private/evidence state |
| `implement/goldberg-0.11-stage-6-7-9-local` | SAFE TO DELETE | 0 | zero unique commits; exact head covered by verified independent bundle; no protected/worktree/PR/release/source-link dependency found |
| `implement/goldberg-0.11-stage-6-10-11-local` | KEEP | 0 | retained worktree has unresolved private/evidence state |
| `integrate/goldberg-0.11-canonical-client` | BLOCKED / NEEDS OWNER DECISION | 1 | 1 commits outside main; do not merge unfinished work |
| `main` | KEEP | 0 | canonical default branch |
| `maintenance/gould-portability` | BLOCKED / NEEDS OWNER DECISION | 0 | current head absent from independently verified existing history bundle |
| `maintenance/legacy-cleanup-2026-10-18` | SAFE TO DELETE | 0 | zero unique commits; exact head covered by verified independent bundle; no protected/worktree/PR/release/source-link dependency found |
| `planning/goldberg-0.11-stage-1` | KEEP | 0 | branch-addressed source/documentation links: docs/wiki/Changelog-and-Roadmap.md |
| `planning/goldberg-0.11-stage-2` | KEEP | 0 | branch-addressed source/documentation links: docs/wiki/Changelog-and-Roadmap.md |
| `release/goldberg-0.11-rc.1` | KEEP | 1 | retained worktree has unresolved private/evidence state |
| `spike/goldberg-0.11-c2-bff` | KEEP | 2 | retained worktree has unresolved private/evidence state |
| `spike/goldberg-0.11-stage-6-auth-local` | KEEP | 0 | Published release-wide Issue #31 source links must remain resolvable |
| `spike/goldberg-0.11-stage-6-contract` | BLOCKED / NEEDS OWNER DECISION | 2 | 2 commits outside main; do not merge unfinished work |
| `spike/goldberg-0.11-stage-6-contract-pg` | BLOCKED / NEEDS OWNER DECISION | 6 | 6 commits outside main; do not merge unfinished work |
| `spike/goldberg-0.11-stage-6-handlers-local` | KEEP | 0 | Published release-wide Issue #31 source links must remain resolvable |
| `spike/goldberg-0.11-stage-6-schema-pg` | KEEP | 0 | Published release-wide Issue #31 source links must remain resolvable |
| `verify/goldberg-0.11-stage-6-12-local` | SAFE TO DELETE | 0 | zero unique commits; exact head covered by verified independent bundle; no protected/worktree/PR/release/source-link dependency found |

SAFE candidates still require fresh head/protection/dependency rechecks at execution. Keep all release tags. The separately approved RC operation removes only prerelease ID 423537826 and its three backed-up assets, not its tag or any Stable release.

created by stokonn with love <3
