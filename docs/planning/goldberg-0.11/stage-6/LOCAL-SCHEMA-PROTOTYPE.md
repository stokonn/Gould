# Stage 6 — authorized local physical-schema prototype

**Owner-authorized 2026-10-22: isolated local-only synthetic implementation.**
Base 4d01489; branch `spike/goldberg-0.11-stage-6-schema-pg`. This is a prototype
milestone, not formal 6.1 or a waiver of the 6.0f pre-implementation policy.

Purpose: convert DATA-MODEL.md §1 and F1–F5 into executable PostgreSQL 17 DDL,
then prove physical integrity/migrations without touching Supabase or app code.
Scope: [spikes/stage6-schema-pg](../../../../spikes/stage6-schema-pg/README.md),
Stage 6 checkpoints only. No Stage 5/7 code or existing spike evidence altered.

- [x] Recover latest remote 4d01489 and create a distinct isolated prototype branch.
- [x] Implement eleven community entities, FK/unique/check graph, immutable revision/audit protections, audit_head and idempotency/feed storage.
- [x] Implement versioned/checksummed atomic migrations and loopback-only disposable PG17 runner.
- [x] Execute targeted physical-schema regressions: 34/34 PASS, no skipped tests.
- [x] Run all five existing repository host checks: PASS.
- [x] Record revision-status contract mismatch and scoped adoption limitations.
- [x] Publish implementation 16bfdfb, verified correction f91a4ce and [Issue checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6351050432); readback PASS.

Detailed [executed evidence](../../../../spikes/stage6-schema-pg/results/SUMMARY.md)
includes intermediate failed runs, final TAP, environment and source hashes. This
milestone covers empty/seeded/repeat migrations; FK/unique/immutable enforcement;
rollback at six boundaries; physical-schema audit/idempotency/target races; tamper
and integer boundaries. No old concurrency benchmark rerun, no full restore/RPO
or hosted/Android/Auth/API/RLS acceptance claim.

API-CONTRACTS.md §4 allows revision statuses draft/changes_requested/returned;
DATA-MODEL.md §3 makes draft local-only, does not define returned and documents
changes_requested → in_review. The prototype has no HTTP endpoint and follows the
server model only. Owner must reconcile before endpoint adoption; neither approved
contract is edited here.

**Next:** review the DDL and evidence as candidate inputs for 6.1; resolve the
contract discrepancy. Formal adoption needs explicit 6.0f acceptance and separate
implementation approval, with Q1/D5b/C4/C2 §12 and scoped cloud gates respected.
Do not deploy, run against Supabase, add production RLS/OAuth, invent retention
periods, merge or collect real-user data. If the next task crosses these boundaries,
stop that action and request exact phase-scoped authorization.

Q1/D5b unresolved/blocking; C4/C2 §12 OPEN; 6.0f/6.1–6.18 UNCHECKED;
6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing only; 0 CZK/month permanently.
Stage 7A conditional/X10 OPEN and Stage 7B not started remain untouched.

created by stokonn with love <3

Publication: implementation commit 16bfdfbe213ce66973a55d9746453601605e74d7;
follow-up f91a4ced846ad53a571c744c6c588f245fe3a582 corrects whitespace in saved
failed-run diagnostics and records the check failure. Tested source hashes and
34/34 results are unchanged; full baseline-to-current diff check PASS. Existing
Issue #31 checkpoint 6351050432 independently read back exactly; all 63 older
comments preserved, 64 total, Project In Progress unchanged. Later publication
metadata/checkpoint commits do not change tested prototype source.
