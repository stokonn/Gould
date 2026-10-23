# Local 6.4–6.6 submission/moderation/idempotency milestone

2026-10-23. Owner explicitly authorized implementation of this synthetic local
prototype; not formal acceptance, 6.0f waiver, extraction into production or cloud
permission. Isolated branch `spike/goldberg-0.11-stage-6-handlers-local`, based on
remote-verified Auth HEAD `7993444b86690d95aae03aa23383b80014dec031`.
Implementation commit is the containing source commit; publication details follow
in docs/ai and Issue #31. Original schema/Auth source and historical evidence remain.

Purpose: supply reusable typed submission/revision handlers, authenticated atomic
moderation and request-bound principal-scoped idempotency. Scope:
[local module](../../../../spikes/stage6-handlers-local/README.md), narrow reviewed
API/COMMUNITY-ADMIN state clarification, and Stage 6 documentation only. No schema
change or migration is needed; no original migration checksum was overwritten.

- [x] Inspect actual branch/worktrees/remotes/tags/releases/milestone/Issues/Project.
- [x] Read latest Issue #31 checkpoint and verify baseline before isolated branch creation.
- [x] Publish only the approved Stage 6 latest URL replacement; verify exact body,
  all 65 full historical comment records, labels/milestone/state and Project unchanged.
- [x] Review LOCAL-CORE-ADOPTION and exact patch/dependencies before applying its
  changes_requested clarification. Draft stays device-local; returned is no wire state.
  Unknown consumers remain unverified; no unseen-data migration or old C6 edit.
- [x] Implement validation, immutable submission lifecycle/private reads, atomic
  moderation/events/feed and canonical principal/request-bound idempotency.
- [x] Execute focused PG17 tests, retain failed-run evidence, fix discovered errors.
- [x] Final integrated 84-regression/host/hash verification: 171 total PASS; five host checks PASS.
- [ ] Commit/push and remote source/scope verification.
- [ ] Publish append-only Issue #31 checkpoint with final verified source and results.
- [ ] Formal 6.4–6.6 acceptance (not granted by local results).

Initial 81-test run: 77 passed/4 failed. Three failures exposed the inherited
export helper ordering `seq::text` by its alias after sequence 9; the database chain
was coherent, but exports were not numerically ordered. New exporter fixes this
without editing original sources. Fourth failure was a test open-helper hardcoding
expected entity revision 0 for a revision-1 fixture. Corrected run: 81 new + 50 Auth
+ 34 schema PASS. Expanded final handler run: 87/87 PASS; final integration evidence
is finalized in [results](../../../../spikes/stage6-handlers-local/results/SUMMARY.md).
No failed evidence was deleted, no old strategy benchmark repeated.

Policy decisions still missing: full real contribution field/evidence/source/license
compatibility rules. Empty/default validator denies all; only injected synthetic
reviewed fixtures are used in tests. Non-text catalogue edits fail closed. No live
DNS/fetch/redirect, hosted session/RLS, HTTP/browser CSRF, real OAuth, quota engine
or full restore evidence. T6–T14/T17/T18/T21 local subchecks only, no entire threat
row accepted. Stage 5 and 7A/7B, Android, opus.db, backup/signing and cloud unchanged.

Q1/D5b unresolved; C4 and C2 §12 OPEN; 6.0f and formal 6.1–6.18 UNCHECKED;
6.0d/6.0e unauthorized; 0 CZK/month; no real-user collection, merge/tag/release.
Next recommendation is separately authorized **6.7–6.9**: public overlay/feed,
remaining Admin operations and abuse controls; do not start automatically.

created by stokonn with love <3
