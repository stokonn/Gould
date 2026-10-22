# Stage 6 PostgreSQL physical-schema prototype

Local-only synthetic prototype authorized by the owner on 2026-10-22, based on
readiness commit 4d01489. This is not formal task 6.1 and cannot be deployed or
promoted without gate clearance and separate approval. No hosted resources,
production RLS, OAuth, retention/deletion jobs or real-user data are in scope.

## Milestone checklist

- [x] Recover contracts, gate statuses, remote SHA and preserved worktrees.
- [x] Implement versioned DDL for DATA-MODEL.md §1 and explicit history constraints.
- [x] Implement audit_head transaction locking, idempotency storage and safe int8 conversion.
- [x] Execute empty/seeded/repeat migration and integrity/rollback/concurrency tests.
- [x] Run repository checks and record limitations.
- [x] Publish the isolated branch and [Issue checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6351050432); exact readback verified.

Q1/D5b unresolved; C4/C2 §12 OPEN; 6.0f and 6.1–6.18 UNCHECKED;
6.0d/6.0e NOT AUTHORIZED; 0 CZK/month; collection NOT AUTHORIZED.
Stage 7A conditional/X10 OPEN and Stage 7B untouched.

## Contract ambiguity

API-CONTRACTS.md §4 permits new revisions from draft/changes_requested/returned.
DATA-MODEL.md §3 keeps drafts device-only, does not define returned, and documents
only changes_requested → in_review for an author revision. The prototype has no
HTTP endpoint and introduces neither draft nor returned server transitions.
Approved API text is unchanged; reconcile with the owner before endpoint adoption.

created by stokonn with love <3

## Implemented physical design

`001_entities.sql` creates all eleven conceptual entities plus idempotency results,
change feed and audit_head. Stable UUID PKs, issuer/subject and external-identity
uniqueness, partial active-role uniqueness and explicit RESTRICT FKs preserve
history. Current submission/publication revision FKs are deferred for atomic
header+snapshot creation. Typed existing-publication targets use a composite FK.
Audit targets bind to submission revisions, published revisions or principals;
this local prototype does not model additional polymorphic target kinds.

`002_integrity.sql` adds immutable UPDATE/DELETE/TRUNCATE guards for submission and
published revisions, review/audit events and change feed; revision hash/previous
hash/contiguous-version checks; atomic header advancement; documented server-state
transitions; deferred review+audit evidence; source-snapshot/evidence consistency;
soft role/session revocation guards; and a pre-existing singleton audit mutex.
No cascading FK erases history. No principal deletion/pseudonymization service or
retention period is invented. Restrict/tombstone storage is a reversible prototype
safety posture, not final D5d legal deletion policy.

The transaction exerciser is local SQL only, not an API or production repository.
Its order is idempotency-key advisory lock → submission FOR UPDATE → entity advisory
lock + existing row FOR UPDATE → audit_head FOR UPDATE. The entity advisory lock
covers the absent-row case. Actual bounded lock/statement waits are 2s/5s; native
unexpected SQLSTATE errors propagate and roll back, without blanket 409 mapping or
an unbounded retry loop. These bounds are local test choices, not production SLOs.

Audit append and head advancement occur in the caller's transaction. Trigger
callbacks pin pg_catalog and qualify all prototype objects. PUBLIC function/table
access is revoked. A temporary local test role proves scoped append access cannot
update history/head or call other definer functions. Actor identity supplied to the
local append function is **not authenticated**; do not grant it to hosted API roles.
Owner/superuser can disable guards: this is tamper evidence, not tamper-proof hosting.

Audit encoding `goldberg-audit-v1` hashes `prev_hash + ':' + canonical array text`
with native SHA-256. PostgreSQL JSONB array text uses exact decimal int8 strings and
UTC timestamps with six fractional digits. Export includes events and audit_head
from one REPEATABLE READ snapshot. Independent Node recomputation reconstructs
fields and catches content edits, reorder, middle/tail deletion and head tampering.
This encoding is prototype-specific; adoption must review canonicalization and
export/version compatibility before any production data exists.

Idempotency is unique by principal/key and stores request hash plus successful
status/body. Exact named 23505 recovery uses a savepoint before replay lookup;
other SQLSTATE/unique errors stay errors. Publication rolls back all effects if an
outside-protocol insertion wins. Entries are valid until `expires_at` (exclusive),
constrained to at least 24 hours after creation. At the boundary a transaction
holding the key lock may reuse an expired key; no automatic deletion job exists.
This is API replay eligibility, not retention for submissions/audits/backups.
Failed decisions consume no key. Same-key concurrent local requests serialize and
replay the winner; the old spike's [201,409] race evidence remains unchanged.

The pg driver's int8 string parser is unchanged. `safeInt8` rejects already-rounded
Number inputs and out-of-safe-range conversion; audit exports retain strings.
Publication checks the response revision before committing, so a revision beyond
JavaScript's safe integer range cannot create an unrepresentable committed result.
No approved HTTP envelope or transition has been changed.

## Reproduce locally

Requires rootless Podman, Node 22 and the pinned cached PostgreSQL 17 image below.
No cloud URL/host/credentials are accepted by the code. The runner accepts only its
own ephemeral loopback port. Do not substitute a hosted database or mounted volume.

```bash
cd spikes/stage6-schema-pg
npm ci --ignore-scripts --no-audit --no-fund
# Cache the official image if absent; pulling an image is not cloud provisioning.
podman pull docker.io/library/postgres@sha256:e4cb93687b062cd25a0e8f672d85a7708235009d70d31f8a7698d9370092a9c4
./run-local.sh
```

The runner uses `--pull=never`, no host volume, trust auth only on a disposable
loopback-bound container, fresh proto_UUID schemas and synthetic identities. It
waits for TCP readiness, not the temporary initialization socket, and removes only
its own container on exit. Never use this trust configuration for hosting.
`npm test` alone requires the runner's local database (default loopback port 54339;
SCHEMA_PG_PORT can select another numeric local port, never another host).

## Verification and remaining work

[Results](results/SUMMARY.md): final 34/34 PASS, 0 failures/skips on PostgreSQL 17.11;
five repository host checks PASS. Failed intermediate runs are preserved separately.
The original comparison at c21e550 and original PostgreSQL failures are untouched.
This suite adds physical-schema/migration regressions; it is not a rerun of the
20-trial concurrency-strategy benchmarks or the SQLite comparison.

Not executed: Supabase/PostgREST transaction-path proof, production RLS/privileges,
hosted Auth/OAuth, network/API security, full restore/RPO drill, application builds,
Android/emulator/device tests, APK/signing, deployments, merge or release. JSONB
snapshots/evidence do not implement the complete API allowlist/size/SSRF/license
validation; those remain later separately authorized server work. Session/quota
storage does not implement live authentication, refresh tokens or abuse endpoints.

**Promotion:** candidate input to 6.1 only after explicit 6.0f acceptance and
separate formal implementation approval, with Q1/D5b, C4, C2 §12 and other scoped
gates respected. Reconcile the revision-eligible status mismatch with the owner;
review the physical schema/locking/encoding and real deployment transaction path.
Do not copy the prototype into a live Supabase migration automatically. 6.1–6.18
remain unchecked and 6.0d/6.0e unauthorized.

Publication: implementation commit 16bfdfbe213ce66973a55d9746453601605e74d7;
follow-up f91a4ced846ad53a571c744c6c588f245fe3a582 corrects whitespace in saved
failed-run diagnostics and records the check failure. Tested source hashes and
34/34 results are unchanged; full baseline-to-current diff check PASS. Existing
Issue #31 checkpoint 6351050432 independently read back exactly; all 63 older
comments preserved, 64 total, Project In Progress unchanged. Later publication
metadata/checkpoint commits do not change tested prototype source.
