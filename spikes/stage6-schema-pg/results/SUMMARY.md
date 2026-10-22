# Local PostgreSQL schema prototype — executed results

2026-10-22; isolated branch `spike/goldberg-0.11-stage-6-schema-pg`, base 4d01489.
Exact source is the containing implementation commit; `source-sha256.json` binds
SQL, modules, tests, dependency manifests and runner to `final.tap`.

**Final: 34 tests PASS; 0 failed, 0 skipped, 0 cancelled.**
Command: `./run-local.sh > results/final.tap 2>&1` from this prototype directory.
Venue: PostgreSQL 17.11 x86_64 Alpine, Node v22.23.1, pg 8.23.1, pinned image
and ephemeral 127.0.0.1 binding in [environment.json](environment.json).
Fresh disposable runner container and original development container removed;
no host volume or remote project used. Synthetic identities only.

| Coverage | Executed result |
|---|---|
| Empty/seeded/repeated/concurrent migration; checksum drift; failed upgrade rollback | PASS; v1 seeded principals/roles/quota survive v2, no destructive recreation |
| F2 FK graph and uniqueness | PASS; orphan actors/session/mapping/revision/review/audit/feed/typed targets rejected; active-role/issuer-sub/external identity constraints enforced |
| Immutable records/revision chain/server transitions | PASS; UPDATE/DELETE/TRUNCATE rejected; prior/hash/version and atomic current pointer enforced; no draft/returned server states introduced |
| F1 decision protocol on physical DDL | PASS; same-submission and different-submission same absent/existing target contention commits one publication; loser ordinary Conflict; actor-author rejection |
| Transaction rollback | PASS after locked/publication/approved/audit/feed/idempotency boundaries; all effects/head/key restored; subsequent retry succeeds |
| F3 idempotency | PASS; exact savepoint recovery from named PK collision, mismatch 422, principal isolation, >=24h and exact expiry reuse, same-key concurrent replay; unrelated unique/FK errors remain errors |
| F4 audit chain | PASS; four independent physical-schema appenders serialize, rollback restores head, independent exported-field recomputation detects tamper/reorder/middle/tail deletion/head edits; restricted role cannot rewrite head/history |
| F5 BIGINT | PASS; pg returns exact int8 strings through min/max and ±JS boundaries, safe conversion or rejection; unsafe next publication revision rolls back |
| Bounded lock contention | PASS; native 55P03 lock-timeout remains diagnosable rather than blanket 409; rollback and later retry succeed |

Repository checks (isolated checkout root):

- `python3 scripts/check_agent_policy.py` PASS; runtime agent loading not tested.
- `python3 scripts/check_backup_fixtures.py` PASS, 3 fixture formats.
- `python3 scripts/check_history_paging.py` PASS, 106 sessions/4 pages.
- `python3 scripts/check_localization.py` PASS, 922 EN/CZ resources.
- `python3 scripts/check_migrations.py` PASS, host v1–9→10 DDL; Android bridge/trigger runtime not executed.

Intermediate evidence retained honestly:

- [initial.tap](initial.tap): 0/27; shared migration syntax error blocked all tests.
- [second.tap](second.tap): 25/27; two fixture UUID/text parameter inference errors.
- [third.tap](third.tap): 31/31 before final integrity/timeout coverage was added.
- [runner-initial.log](runner-initial.log): setup failure before tests; readiness saw a temporary Unix-socket server, then host TCP reset. Corrected TCP readiness guard.
- [final.tap](final.tap): clean container, final 34/34 PASS. Earlier failures do not describe this final source.

No repeat of the old concurrency-strategy benchmark. Original PostgreSQL failure
and hardening evidence remain at e37ed00/c21e550. No production/Supabase proof,
RLS/Auth/OAuth, complete API validation, full DB restore/RPO, Android/APK, deployment
or release checks performed. Audit export JSON round-trip/recomputation is not a
full restore drill. Source snapshots are synthetically generated and the prototype
has no client/server endpoints.

Contract ambiguity: API-CONTRACTS.md permits revision from draft/changes_requested/
returned; DATA-MODEL.md defines draft device-only and only changes_requested →
in_review. Approved contracts unchanged, owner reconciliation required before
endpoint adoption. Local concurrent identical-key replay is stronger than the
old spike's decision-loser [201,409]; do not rewrite that historical evidence.

All formal gates preserved: Q1/D5b unresolved/blocking; C4/C2 §12 OPEN; 6.0f and
6.1–6.18 UNCHECKED; 6.0d/6.0e NOT AUTHORIZED; 0 CZK/month; collection NOT AUTHORIZED.
Stage 7A conditional/X10 OPEN and Stage 7B untouched. This local milestone is
complete; it is eligible only for future reviewed adoption after gate clearance,
never automatic promotion into formal 6.1 or a hosted migration.

created by stokonn with love <3
