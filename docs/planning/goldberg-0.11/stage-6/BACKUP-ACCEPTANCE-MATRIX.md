# Synthetic backup operations acceptance matrix

This matrix is local readiness evidence for the design in
[BACKUP-OPERATIONS-RUNBOOK.md](BACKUP-OPERATIONS-RUNBOOK.md). It uses synthetic
PostgreSQL 17 data only and does not activate scheduling or access Supabase.

| Check | Local evidence | Status | Production limitation |
|---|---|---|---|
| Logical export completes | `run-local.sh` export phase | PASS | No hosted export authorization |
| Authenticated encryption | GnuPG AES-256/MDC; wrong-key rejection | PASS | Recovery custody must be provisioned by owner |
| Integrity manifest | SHA-256 manifest checked before restore | PASS | Destination durability is unverified |
| Tamper/corruption rejection | Payload tamper and partial archive cases | PASS | No object-store failure drill |
| Atomic publication | `.part` artifacts are never accepted as backups | PASS | Production filesystem semantics remain unverified |
| Separate-process recovery | Export and restore run in separate processes | PASS | No disaster-site restore |
| Key rotation | v1 artifact restored after v2 becomes active | PASS | No owner-held off-site recovery copy |
| Schema and row counts | Restore SQL checks fixture schema/counts | PASS | Synthetic counts are not production completeness |
| Audit-chain fixture | Hash-chain validation passes on synthetic rows | PASS | Provider audit/log retention unverified |
| Interrupted operation cleanup | Failure paths remove temporary state | PASS | No power-loss or remote-storage simulation |
| Freshness/age policy | Runbook defines daily cadence and 24-hour target | DESIGN ONLY | No production scheduler or alert destination |
| Retention policy | Runbook defines daily/weekly/monthly classes | DESIGN ONLY | No live deletion or retention execution |
| Restore drill | Quarterly procedure documented | DESIGN ONLY | Hosted restore is not authorized |
| Measurable production RTO | Deliberately not inferred from local timings | OPEN | Owner must set and verify production RTO |

The previously recorded synthetic timings remain local-only evidence:
export 3,915 ms, restore 4,341 ms, rotation export 4,009 ms, total 30,033
ms. They must not be presented as production RPO or RTO.

created by stokonn with love <3


## New local archive lifecycle evidence — 2026-10-25

New executable store/health/isolated-output implementation and5 regressions PASS;
new real synthetic pg_dump archive roundtrip and fresh-database marker/RLS/policy
restore PASS. Existing encryption/key-rotation/community-audit proofs retained.
Freshness/age detection is now LOCALLY VERIFIED by24h boundary/future/incomplete
checks; scheduler/alert/durable destination/custody and hosted RPO/RTO remain OPEN.
No automatic retention deletion or production backup operation configured.
[Execution and limits](LOCAL-BACKUP-EXECUTION.md).
