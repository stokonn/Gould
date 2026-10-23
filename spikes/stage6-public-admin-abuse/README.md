# Local public catalogue, Admin operations and abuse protection

Owner-authorized Stage 6 local 6.7–6.9 candidate. **PARTIAL / NOT ACCEPTED.**
No HTTP listener, hosted integration, real users, credentials or deployment.
Existing formal 6.1–6.18 and 6.0f statuses are unchanged.

`src/compose.mjs` composes the existing Authorization, Idempotency, submission and
moderation services into the existing CommunityApi in-process adapter:

| Adapter operation | Contract route |
|---|---|
| catalogue.overlay | GET /v1/catalogue/overlay |
| catalogue.changes | GET /v1/catalogue/changes |
| admin.quarantine / admin.rollback | POST /v1/admin/entities/{id}/quarantine or rollback |
| admin.audit | GET /v1/admin/audit |
| admin.roles / admin.suspend | POST /v1/admin/principals/{id}/roles or suspend |

Existing queue/open/decision handlers are reused. Existing submission operations
are decorated at their transaction/idempotency boundary, without another database,
authentication, session, audit or idempotency implementation.

All bodies have schemaVersion:1. Admin quarantine/rollback bodies require `id`,
`expectedRevision` (exact decimal string), `expectedEventSeq` (exact decimal string),
`reason`, `idempotencyKey`; rollback adds `targetRevision` (a smaller prior revision).
The event expectation prevents stale requests after quarantine or rollback, even
when the projection returns to a previously observed revision. Revision payloads
are never changed. New publications allocate max historical revision + 1, rather
than reusing a revision after rollback. Existing submission/decision number bounds
remain fail closed; public/Admin decimal representations preserve full int8.
These new request details are local candidate fixtures, not a frozen client API.

Role bodies require `principalId`, `role`, `revoke` boolean and idempotencyKey;
suspension bodies require `principalId`, `suspended` boolean and idempotencyKey.
Existing Authorization.changeRole/suspend execute within the same outer live
capability/target-lock/idempotency transaction. Replays require live authorization.
No self-grant or audit editing operation exists. Bootstrap/recovery remains out of
band; no default administrator is created.

Public metadata is an explicit title/kind/UUID/revision and license/count summary.
No stored row or entire payload is spread into a response. The change feed returns
public event classification and identifiers, without moderation reasons, actors,
submission IDs or audit references. Superseded/quarantined historic payloads are
not exposed through this feed. Snapshot highwater uses the existing audit-head
commit barrier. Feed order is numeric immutable sequence, avoiding seq::text alias
ordering. Overlay order is first-publication (created_at,entity UUID). Both cap at
20. Signed cursors bind endpoint/filter and highwater; current quarantine or
replacement filters override an old overlay snapshot. Such entries can disappear
between pages; history is not rewritten. Finish nextCursor before using pollCursor.

004 is additive, checksummed and local only; it extends the existing abuse_limits
and feed contracts. `migrateOperations()` reuses 001/002/003 migration functions.
Intake defaults disabled. Enable only on a disposable synthetic venue using a
local operator SQL update, not a new public configuration endpoint.

The intake guard applies to submission creation, revision and approval; withdrawal
and Admin remediation remain available. The replay check precedes kill-switch and
quota consumption, after live authorization. Principal/day and coarse-IP counters
share the work transaction; principal locks serialize pending/daily checks, and
PostgreSQL upsert/advisory locks serialize shared IP quota. Quotas count committed
new submissions only. 429 carries Retry-After; unavailable storage/intake yields
503. No pending/audit/history cleanup or paid fallback exists.

The trusted second argument to invoke supplies `remoteAddress`; the request JSON
cannot select its peer or forwarded headers. IPv4 /24 and IPv6 /64 groups are
HMAC-SHA256 keyed with an explicit server key (IPv4-mapped IPv6 normalizes to IPv4).
Keys are injected, never committed. Tests use only reserved synthetic addresses.
Local short retention is configurable, with expired coarse counters pruned during
fresh intake; no production retention/legal period is selected. **No independent
janitor is scheduled: expiry cleanup during inactivity remains an integration
limitation.** This is not complete bounded-retention acceptance.

Real-world source/field/license compatibility remains unresolved. Validator defaults
still reject unreviewed content. Fixtures are explicitly permitted authored synthetic
metadata only. No evidence fetcher/proxy, DNS/redirect or network resolution has been
added; URL allowlisting does not complete T14.

Run `./run-local.sh` with existing pinned pg/jose dependencies and cached pinned
PostgreSQL 17 image in an environment permitting rootless Podman and loopback sockets.
Each execution creates a new results directory and removes only its own container.
`npm run test:unit` needs no database. Direct node:test file execution preserves
actual subtest counts; a wrapper-only --test result is not sufficient evidence.
Read [results/SUMMARY.md](results/SUMMARY.md) for executed results and blocked tests.

created by stokonn with love <3
