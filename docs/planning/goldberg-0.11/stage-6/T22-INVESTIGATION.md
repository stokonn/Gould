# T22 — reproduced routing discrepancy and approved local correction

**T22 LOCAL PASS: owner-approved correction verified in version3 Fedora PostgreSQL
and routing/model tests. Overall6.12 PARTIAL.** No HTTP listener, browser or hosted endpoint was tested.
The observed “HTTP400/403” values were response status numbers from in-process
adapters, not actual HTTP exchanges. Published e7dc7ea/checkpoint6369071469 and all
original FAIL evidence remain intact.

## Post-patch local acceptance evidence

Owner-tested6d0abad: outer run-x0nDSTXd108 PASS + nested PG17.11 run-TEmZzZQg237
PASS =345 executions/313 tests counting repeated32 suite once.205 database-backed
bodies include explicit handler/admin audit403, live self-grant/escalation/revocation
and SQL immutable-history restrictions.38 routing/scripted-SQL cases separately
verify malformed400, registered auth401 and denied403 without protected writes.
Nested exit0, exact stdout linkage,82 source hashes, five checks in both runners
and retained container-removal output verified. No inference solely from availability.

All explicitly approved LOCAL T22 cases now have executed evidence: local PASS.
No HTTP listener/hosted/Android acceptance is claimed. Earlier119-test/pending-PG
and original FAIL records below remain point-in-time history, not current blockers.

## Cause and contract review

`CommunityApi.invoke` previously checked the request envelope, bounded/snapshotted
body, then dispatched a registered operation or threw `invalid()` for an unknown
one. `failure()` translated that to400 validation_failed. There was no session,
capability, SQL or audit-edit handler call. The separate Auth `LocalApi` default
already threw `forbidden()` and returned403 for the same absent operation.

For a well-formed absent privileged operation,400 conflated routing denial with
malformed payload and differed from AUTH section6 default-deny. T22's blanket403
was also imprecise for a registered roles request with missing credentials, where
API-CONTRACTS section2 correctly requires401, and for malformed requests requiring400.
No privilege escalation was demonstrated by this discrepancy.

The owner explicitly approved the prepared local patch and clarification:

| Input/cause | Approved CommunityApi result | Work permitted |
|---|---|---|
| Envelope/structural/schemaVersion/UUID/boolean/size/getter validation failure |400 validation_failed|No SQL/handler|
| Empty/non-string operation, including array coercion |400 validation_failed|No SQL/handler|
| Well-formed unregistered audit/bootstrap operation |403 forbidden|No authentication, SQL or handler|
| Registered route, missing/malformed/unknown/expired/revoked session |401 unauthenticated|Only necessary session lookup/rollback for well-formed tokens|
| Suspended principal, missing/unknown role or missing live capability |403 forbidden|Read/lock/rollback only|
| Authenticated administrator targets own UUID, including uppercase spelling |403 forbidden|No role/audit mutation; transaction rolls back|
| Unsupported role enum after valid shape and authentication |400 validation_failed; missing session still401|No protected write; transaction rolls back|
| Authentication storage unavailable |503 service_unavailable|No false success or diagnostics|

The implementation patch adds operation type/empty validation and changes only
CommunityApi's unregistered-operation default to `forbidden()`. Corresponding local
regression expectations and T22/API/AUTH clarification were explicitly approved;
no new endpoint, actor input, privilege, HTTP binding, API dialect or6.17 freeze.
Auth LocalApi remains a separate operation adapter; no shared HTTP binder is implied.

## Trace of protected registered role dispatch

1. CommunityApi allowlists envelope, validates operation type, bounds/snapshots body.
2. `compose()` binds admin.roles to `AdminService.principal`.
3. That service validates schemaVersion, UUID, role-operation body/booleans, then
   requires the live roles.manage capability.
4. `SessionService.withSession` validates token syntax before connection; for valid
   syntax it opens a transaction, sets timeouts, finds and locks principal/family/
   session, checks expiry/revocation/issuance/state, then queries live role grants.
5. Idempotency runs in that same transaction. Expired-key DELETE housekeeping may
   be attempted before the nested `Authorization.changeRole` self-grant check.
   Self-grant403 rolls back the transaction; there is no role INSERT/UPDATE or
   append_audit call, and no COMMIT. Tests explicitly distinguish attempted
   transaction-local housekeeping from protected writes.
6. Safe error mapping/logging emits status/code/requestId only, without token/SQL.

Audit mutation follows NONE of this path: it is not registered in compose, and is
denied403 without dispatch. Own-property lookup prevents prototype names becoming
handlers. The old array-to-property-key coercion could reach a registered handler
(whose authorization still denied access); the approved type guard rejects it400.

## Historical patch-investigation evidence (superseded by v3 above)

At the patch checkpoint:38 T22 +11 shared contract +27 security +32 unit =108 Node tests;
11 Python report regressions separately, **119 PASS total**, zero failures/skips/
cancellations in final runs. Raw logs: results/security-local/t22-investigation/.
The T22 suite calls real routing, SessionService, Authorization, AdminService and
Idempotency over explicitly scripted SQL. It is NOT physical PostgreSQL evidence.
The existing pre-patch host296 executions/264 tests retain real SQL audit immutability,
role restrictions, rollback and replay proof for their original exact source.

At that earlier agent checkpoint, updated PostgreSQL suites had0 bodies due loopback EPERM; the owner has now executed them successfully as recorded above. The three
baseline test files change only absent-operation status assertions; their historical
logs/source hashes remain bound to the old commits, not relabeled as post-patch runs.
The current real PG17.11 run now satisfies local T22 acceptance. HTTP binding and
client/server integration remain unapproved/unexecuted.

Retained intermediate failures: initial30/33 due an overly broad harness no-write
assertion that counted rolled-back idempotency expiry housekeeping; corrected to
forbid protected writes and require rollback/no commit. One subsequent32/33 run
caught an accidental test expectation403 on malformed requests; fixed back to the
approved400. Initial capture harness had an unused-variable TypeError before writing
its corpus. These were harness defects; their evidence is retained, not relabeled.

## Stage7B consequence

AdminApi currently maps400/422 to Validation,401 to Unauthorized and403 to Forbidden;
401/403 trigger onAccessDenied. Absent audit mutation formerly looked like bad form
input; approved403 follows default-deny and safely invalidates local trust. No client
change or production activation was made. See STAGE7B-CONTRACT-READINESS.md and
contracts/admin/v1/readiness.json for G1–G11, versioned cases and owner decisions.

created by stokonn with love <3
