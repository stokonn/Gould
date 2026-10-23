# Executed evidence — partial local 6.7–6.9 milestone, 2026-10-23

**NOT COMPLETE. No concurrent PostgreSQL service acceptance.**
Branch implement/goldberg-0.11-stage-6-7-9-local, base
262847ebcdef18d186ded5b7af4825436a1dc4e3; implementation checkpoint is the
containing source commit. Historical 171 PASS belongs to c80c03e, not this run.

| Venue / command | Actual result |
|---|---|
| `node spikes/stage6-public-admin-abuse/tests/unit.test.mjs` | 30 PASS, 0 failed/skipped/cancelled; unit-final.tap |
| PostgreSQL 17.11 single-user mode, original 001/002/003 + additive 004 | SQL application succeeded; default intake false; postgres-single-migrations-attempt-1.log |
| PostgreSQL 17.11 single-user mode, tests/single-user.sql | Smoke assertions PASS; postgres-single-smoke-attempt-1.log; no service/concurrency claim |
| New tests/postgres.test.mjs | 33 hook failures: connection setup EPERM; 0 test bodies executed; postgres-integration-attempt-1.tap |
| Current handler/Auth/schema regressions (unchanged test files) | 87/50/34 hook failures, connection EPERM; 0 bodies executed; *-regression-attempt-1.tap |
| Original unchanged 262847e worktree suites | 87/50/34 hook failures, same EPERM; 0 bodies executed; unchanged-baseline-*-attempt-1.tap |
| Five scripts/check_*.py | all exit 0; host-checks.json and separate logs |
| Historical source manifests | 30/32 byte-identical; two intentional reusable handler extensions; original tests/schema/Auth and historical manifests/results unchanged |

Do not aggregate hook failures into passing regression evidence or label them as
skips. Both actual failed attempts and their stacks are retained. --test originally
reported one passing file wrapper without nested counts (unit-attempt-1.tap);
this is insufficient evidence and is superseded by actual direct 28/30-test runs.
No source modification was made to any of the original 171 test cases.

Podman fails before container startup: cannot chmod read-only
/run/user/1000/libpod. A separate runroot has the same limitation. The local runner
retains its failed container log under results/run-*/. Standalone cached image
binaries were then copied into /tmp with a local musl interpreter path, matching
image libraries/ICU data, and a fresh UTF8 synthetic PostgreSQL 17.11 cluster was
initialized. No cached image file was modified. Listener startup fails on
127.0.0.1 with **Operation not permitted** (postgres-start-denied.log). No
privilege escalation or hosted fallback was attempted. This is a sandbox blocker,
not evidence that transaction/concurrency assertions passed.

Single-user SQL checks actually exercised the schema: default-off intake control;
published immutable revisions; audited publication, supersession, rollback and
quarantine feed classification; quarantine projection status; UPDATE/DELETE
rejection on immutable history/audit; quota upsert/exhaustion boundaries; counter
rollback in a PostgreSQL subtransaction; SQL hash-content consistency. These checks
do not invoke JavaScript handlers, idempotency, the request adapter, or multi-client
locking. Source test and exact engine/source hashes are included.

Unit checks actually exercised full int8 string bounds, numeric alias regression
inputs, signed cursor corruption/scope separation, reserved synthetic IPv4/IPv6
coarsening/mapped-address normalization, keyed-hash separation, explicit public
field projection, safe 429/503 and Retry-After, deny-default license validation,
independent 12-event audit export verification/tamper/omission/head failure, no
registered audit-edit operation and unavailable public storage.

Threat qualification: T14 remains URL-policy/no-fetch only; DNS rebinding and
redirect/network protections UNEXECUTED. Local T16 quota SQL and hash/Retry-After
units, T17 SQL counter rollback, T18 immutable SQL + independent chain-tamper units,
T20 safe unavailability units, and T22 absent audit-edit route exercised only as
specified above. No entire threat-model row accepted. New PostgreSQL concurrency,
authorization integration, request idempotency and atomic Admin fault tests remain
UNEXECUTED inside their failed setup hooks. No HTTP/browser, hosted RLS/provider,
Android/offline or full restore/RPO venue was tested.

Known limitations / remaining acceptance:

- Run all 33 new PostgreSQL tests and original 171 tests in an allowed disposable
  PG17 loopback venue; inspect failures and fix code before acceptance.
- The new suite includes races, Admin boundary faults/deferred commit, supersession/
  rollback, numeric BIGINT feed sequences, equal timestamps, cursor/highwater polling,
  role/revocation/replay, clock windows, quota/IP/pending/kill and storage faults.
  Written cases are not execution evidence.
- IP cleanup currently runs during fresh intake; an independently scheduled local
  janitor/retention acceptance remains unfinished. Configurable synthetic retention
  is not a real-user/GDPR policy. No pending/audit/publication data is deleted.
- Public projection supports reviewed plain-text title/name/displayTitle and a
  license/count summary only. No universal real-world license acceptance policy.
- Intake gating covers create/revise/approve, retaining withdrawal/Admin remediation;
  verify this local interpretation with future integration contracts.
- Existing number-based submission/publication writes still fail closed outside
  safe JSON integers. Public/Admin read and new Admin request IDs preserve int8.
- New expectedEventSeq/decimal Admin fixtures and sequence-based feed cursors are
  candidate local extensions; client contract freeze/integration unexecuted.
- No HTTP listener, real source content, evidence fetcher, paid service or cloud
  changes. Stage 5, Stage 7A/X10 and unrelated worktrees are untouched.

Formal 6.1–6.18 and 6.0f stay unchecked. Q1/D5b, C4, C2 §12 remain unresolved.
No main merge/tag/APK/release. **Not ready to start 6.10–6.11 Android integration.**
Exact next action: restore a permitted local PostgreSQL 17 socket/container venue,
run run-local.sh without overwriting evidence, finish retention enforcement, fix
and verify all integration/regression failures, then publish a verified checkpoint.
