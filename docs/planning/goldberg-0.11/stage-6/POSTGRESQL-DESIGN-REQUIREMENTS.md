# Stage 6 — PostgreSQL findings as future task 6.1 design requirements

**Design preparation only, 2026-10-22.** Task 6.1 remains UNCHECKED and cannot
start until explicit 6.0f acceptance and separate implementation authorization.
No DDL, production code, migrations or backend is implemented by this document.
The experimental spike must not be merged into production.

Evidence: original [SUMMARY-PG.md](https://github.com/stokonn/Goldberg/blob/e37ed00b9205ce22e9e8ec494651e900708ee1e3/spikes/contract-c6/results/SUMMARY-PG.md)
and [SUMMARY-HARDENING.md](https://github.com/stokonn/Goldberg/blob/c21e550ceb5ec7a42317a4f2fe02e6d1225a2cab/spikes/contract-c6/results/pg-hardening/SUMMARY-HARDENING.md).
Local PostgreSQL 17.11 only, not Supabase. Preserve all original failures.
Existing DATA-MODEL.md, API-CONTRACTS.md and THREAT-MODEL.md remain the baseline;
this is a requirements checklist to reconcile when 6.1 is authorized, not an
implicit approval of new API behavior.

## Required design and acceptance checks (all future, not executed here)

| Finding | Requirement for future physical design | Required verification |
|---|---|---|
| F1 — stale status / double approval / loser 500 under READ COMMITTED | Recommend SELECT … FOR UPDATE on submission before status/revision guard, with audit_head append mutex (F4). Commit publication, review, audit and feed atomically. A waiting loser must re-evaluate state and return ordinary 409. Every state-changing decision path must follow the locking contract. | Barrier races for new/existing entities, repeated trials, exactly one decision with [201,409]; injection at each write boundary leaves no partial effects. Cover different submissions targeting the same entity: choose and test target-row locking or atomic expected-revision comparison; same-submission evidence alone does not prove this case. |
| F2 — absent foreign keys | Specify FK graph for principals, submissions, immutable revisions, review/publication and feed/audit references, including nullable/tombstoned identities. Decide restrict/soft-delete/quarantine behavior consistent with historical attribution and eventual reviewed deletion policy. | Empty and seeded migration runs, orphan rejection and transactional rollback; no destructive recreation/cascade that erases required evidence. No claim that the spike proved production FKs. Personal opus.db and all Android migrations remain untouched. |
| F3 — idempotency unique collision | Unique principal/key scope; catch the specific idempotency-key conflict and serve committed stored response for same request hash, 422 on mismatch. Recover transaction state after 23505 before reading replay (rollback/savepoint or equivalent); never blindly map every unique violation to replay. Keep expiry >=24h and failed-request non-consumption. | Deterministic two-connection store race, one stored row, hash mismatch 422, isolation by principal, expiry and crash/retry. Distinguish sequential replay from concurrent decision guard conflicts. |
| F4 — audit last+1 race | Recommend a single pre-existing audit_head mutex row; lock, read last seq/hash, append event, update head in the SAME decision transaction. Consistent lock order across all writers, bounded contention/deadlock behavior, append-only protection. A sequence alone does not serialize prev_hash selection or prove a coherent chain. | 20+ simultaneous independent decisions: unique ordered audit entries and coherent chain; rollback preserves head/event equality; tamper/deletion detection, restore recomputation, privileges prevent unauthorized mutation. Verify export includes audit_head. |
| F5 — SQL and int8 portability | Native PostgreSQL transaction semantics and parameter binding; qualified ON CONFLICT expressions; explicit stable IDs/order instead of rowid; declared JSON number/string policy for BIGINT. Convert only within JavaScript safe-integer range, reject or use a documented lossless representation otherwise. Test schemas isolated per process. | Driver-default strings vs contractual envelope types, numeric boundaries, stable paging order, SQLSTATE handling and prepared statements; do not copy the spike's broad Number parser without range checks. |

- [ ] Reconcile these requirements with concrete DDL and API/error contracts during authorized 6.1.
- [ ] Specify lock ordering for submission, target entity and global audit_head; test conflicts/deadlocks.
- [ ] Define bounded SQLSTATE retry and conflict mapping; unexpected failures stay diagnosable.
- [ ] Add migration, rollback, FK, concurrency, idempotency, chain and numeric-boundary regressions.
- [ ] Re-prove via the intended Supabase/PostgREST transaction boundary, role/RLS and connection path after separate authorization. Several HTTP calls are not one atomic database transaction.

## Measured comparison preserved

Local hardening: PostgreSQL 123/123 pass; SQLite 79 pass / 0 fail / 14
PG-only skips. FOR UPDATE race 20/20, zero SQLSTATE retries including 20-way
audit scale; SERIALIZABLE race 20/20, 21 serialization retries in races and
218 at 20-way scale (maxAttempts 64), none exhausted. Recommendation remains
row locking plus audit_head; these small local runs do not establish production
throughput or a safe production retry bound.

SERIALIZABLE is a documented alternative: bounded whole-transaction retries for
known conflicts, explicit 40001/40P01 handling and final 409 on exhaustion;
23505 needs constraint-specific handling. Do not generalize every SQL error to 409.
This option is comparison evidence, not a second production implementation task.

## Contract clarification to resolve before implementation

The spike's concurrent same-key decision race returned [201,409], while sequential
same-key duplicates replayed the stored response. Record this distinction explicitly
when finalizing the API: retrying the loser after the winner committed can obtain
the stored replay. This preserves one effect; it does not prove that every concurrent
duplicate receives the winner's response immediately. Existing broad replay wording
must be reconciled and reviewed, not silently changed in this documentary session.

**Unchanged gates:** Q1/D5b unresolved; C4 OPEN; C2 §12 OPEN; 6.0f UNCHECKED;
6.1–6.18 UNCHECKED; 6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing only; 0 CZK/month.

created by stokonn with love <3

## Local prototype authorization — 2026-10-22

The owner separately authorized isolated local-only synthetic schema prototyping
on `spike/goldberg-0.11-stage-6-schema-pg`. [LOCAL-SCHEMA-PROTOTYPE.md](LOCAL-SCHEMA-PROTOTYPE.md)
records concrete implementation and 34/34 targeted PostgreSQL 17.11 checks. This
is candidate evidence for future 6.1, not formal implementation or a policy waiver.
6.0f remains UNCHECKED; Q1/D5b/C4/C2 §12 gates and all cloud/collection approvals
remain unchanged. No hosted action, production RLS, OAuth, retention policy,
Stage 7 change, merge or release is authorized by this prototype instruction.
