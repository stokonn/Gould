# Goldberg community BFF

Executable composition of the accepted Stage 6 services, canonical Admin adapter
and request-scoped Fetch/PostgreSQL transport. It never migrates or seeds a hosted
database. Production configuration is disabled; no Worker has been deployed by
this implementation. `cloudflare.config.ts.template` is a packaging candidate.

Session/challenge/refresh/logout, principal-bound capabilities, canonical queue,
Detail/Comparison, explicit open, dual-revision decisions, audit and statistics
reuse `spikes/stage6-api-v1-local`. Its existing Admin wire schema is unchanged.
The composition adds these accepted baseline domain routes:

| Routes | Existing operation/service |
|---|---|
| POST submissions; GET submissions/mine; GET submissions/{id} | ProtectedSubmissions create/list/read |
| POST submissions/{id}/revisions or /withdraw | Immutable revision / ownership lifecycle |
| GET catalogue/overlay or /changes | CatalogueService projection / bounded signed feed |
| POST admin/entities/{id}/quarantine or /rollback | AdminService atomic projection/audit/feed |
| POST admin/principals/{id}/roles or /suspend | AdminService live authorization / principal locks |

All paths have `/v1` in this **local provisional** implementation. Additional
routes retain the baseline documented contribution/domain field names, including
`expectedRevision`; canonical Admin decisions retain their separate
`expectedEntityRevision` and `expectedSubmissionRevision`. No legacy Admin DTO
fallback or automatic translation is provided. This does not freeze a public API.

`createBffAdapter` explicitly accepts the database connector, trusted identity
verifier, clock, policy and keys. Empty production field/source/license policies
deny contributions. Its canonical moderation path now uses the same intake guard
as contribution creation/revision. Existing idempotency transactions and live
principal/role locks remain authoritative. Raw database headers are not returned.

`fetch.mjs` bounds streamed requests to16KiB and responses to1MiB, enforces a
10s deadline, propagates cancellation and closes request-scoped connections.
204 responses contain no body. Unknown routes and wrong methods retain404/405.
Database and internal errors are redacted. No shared connection pool, scheduled
janitor, background retry, cookie authorization or browser credential is added.

The production Worker additionally requires explicit enablement, private identity
verifier binding, fixed DB host, restricted role, CA configuration and stable
server-only cursor/IP keys. The identity binding supplies no authorization roles.
It is deliberately absent from the inert candidate: real OAuth remains unconfigured.
Application database grants are not provisioned by this code or the probe role.

TLS: Node pg uses the configured CA and servername. `pg-cloudflare`1.4.1 forwards
`expectedServerHostname` to Workers StartTLS, whose documented type has no custom
CA property. Do not infer custom-CA enforcement from Node or injected-driver tests.
Actual trusted-pooler certificate/hostname/runtime validation remains a hosted gate.

Run from the repository root after `npm ci --prefix` for the BFF, schema and auth
packages:

```sh
python3 services/community-bff/run-local.py
node services/community-bff/build-local.mjs
python3 services/community-bff/run-local.py --client-worktree /tmp/goldberg-stage8-canonical-integration --client-only
```

The local runner creates one UUID-named rootless PG fixture, records every command,
source/raw hashes and test counts, then removes only that container/its volumes
and verifies absence. It never accepts a database URL. The client mode executes
the actual isolated Android repository/transport against real HTTP and PostgreSQL.
CI uses a separate explicitly disposable PostgreSQL service and the same bodies.

Latest final backend execution:506 bodies/0failures/0skips,including8 Fetch,
7 combined BFF/PG and1 actual local workerd/PG test. Production bundle:8 workerd
fail-closed requests. Local first/warm wall times337.424ms/29.791ms,3 workerd
requests;these are not hosted latency or CPU measurements. Kotlin real HTTP/PG
execution:1 passed integration case,0skips. Full receipts live under results/.

created by stokonn with love <3
