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

The production Worker additionally requires explicit enablement, an approved Google
client ID (or a private identity verifier binding), fixed DB host, restricted role,
CA configuration and stable server-only cursor/IP keys. The fixed Google JWKS
transport bounds reads, rejects redirects and respects cache age; it reuses the
accepted signature/issuer/audience/nonce verifier. Identity supplies no roles.
Real OAuth client configuration remains absent from the inert candidate.
Application database grants are not provisioned by the Worker or probe role.
`runtime-access.mjs` emits a reviewable transaction with explicit table/command
grants and server-role RLS policies. It never connects or creates roles/credentials.
Runtime roles cannot own objects, inherit memberships or bypass RLS. Application
requests still pass live ownership/session/role checks. Local integration uses
actual restricted logins and tests denial of DDL, migration reads, intake edits
and role escalation. The reviewed NOLOGIN runtime role/grants/RLS transaction is now applied to the
existing Supabase schema. No password, login enablement or Worker is deployed.
Its only creator-maintenance membership is schema-owner ADMIN with INHERIT=false
and SET=false. Hosted exact grants/policies/client denials and Advisor pass.
Do not replay role creation/policies;actual hosted runtime acceptance remains open.

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

Latest backend execution:517 bodies/0failures/0skips,including14 Fetch/identity,
12 combined BFF/PG and1 actual local workerd/PG test. Production bundle:8 workerd
fail-closed requests. Local first/warm wall times337.424ms/29.791ms,3 workerd
requests;these are not hosted latency or CPU measurements. Kotlin real HTTP/PG
execution:1 passed integration case,0skips. Full receipts live under results/.

created by stokonn with love <3

Continuation evidence and external prerequisites: [runtime continuation](../../docs/planning/goldberg-0.11/stage-6/RUNTIME-CONTINUATION-2026-10-25.md).
The client runner forces Gradle execution; old cached XML is not fresh evidence.
Package receipts use unique directories and preserve prior results.

Authentication POST challenge/login/refresh share a database-backed30/minute
coarse-IP budget by default (server-only authBurst configuration,max120). Failed
credentials count;atomic counters survive new adapters with the stable IP_KEY.
Only HMAC/domain-separated coarse-IP keys are stored;missing peer metadata shares
a bounded bucket. 429includesRetry-After;counter/database failures deny requests.
Hosted deployment/verification is BLOCKED by the owner's current connectivity
directive;no database authentication retries or credential changes are allowed.
