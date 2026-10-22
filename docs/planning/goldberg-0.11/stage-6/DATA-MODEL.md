# Stage 6 — Community data model and moderation state machine

**Design only, conceptual schema.** Extends the table list proposed in
[COMMUNITY-ADMIN.md](../COMMUNITY-ADMIN.md) §"Roles and server data" with shapes,
invariants and state machines precise enough to implement on either candidate
backend (PostgreSQL/Supabase or SQLite/D1). Physical DDL is chosen at implementation
time after D4; the invariants below are binding regardless of engine.

## 1. Entities (conceptual)

| Table | Purpose | Key invariants |
|---|---|---|
| `principals` | Stable server identity (`internal UUID`, `iss`, `sub`, `state`, `created_at`) | Unique `(iss, sub)`; no email/photo/profile history; `state ∈ {active, suspended, deleted}`. |
| `role_grants` | `(principal_id, role, granted_by, granted_at, revoked_at?)` | Only deployment operator/admin grant path; revocation is soft (`revoked_at`); history preserved. |
| `sessions` | Server-tracked sessions/refresh families | Revocable; rotation-aware; expiry enforced. |
| `submissions` | Contribution header: target entity, kind, owner, status, expected revision, timestamps | `author_id` set only by server from session; status transitions only via §3 rules; bounded counts per principal. |
| `submission_revisions` | **Immutable** full payload snapshots per submission version | Append-only; each row hash-chained to previous revision hash; no UPDATE/DELETE ever. |
| `review_events` | Moderator/author actions with reasons | Append-only; includes decisions, change-requests, withdrawals, reopens; no bearer tokens/journal data. |
| `published_entities` | Approved overlay entity rows (typed UUID, kind, revision, status) | Only the approval transaction writes; `status ∈ {published, superseded, quarantined}`. |
| `published_revisions` | Immutable published snapshots (hash, payload, actor, source evidence) | Append-only; `superseded`/`quarantined` are new rows or status transitions with reason — never payload rewrites. |
| `identity_mappings` | Submission target ↔ Stage 2/4 catalogue UUID ↔ external provider IDs | Reuses Stage 2 external-identity uniqueness shape `(provider, providerEntityType, externalId)`; unresolved mappings stay visibly unresolved. |
| `audit_events` | Security/decision audit trail | Append-only, monotonic sequence, hash-chained (`prev_hash`/`this_hash`); no client endpoint can mutate; contains actor internal ID, action, target/revision, before/after hashes, reason, server time, request ID. |
| `abuse_limits` | Per-principal and coarse-IP quota accounting | Atomic increments; survives restarts; per-process-only limits explicitly insufficient (CLOUD.md). |

Typed catalogue UUIDs come from the Stage 2 contract (DATA-MODEL.md of stage-2:
opaque Goldberg UUIDs, not provider IDs as primary keys). A submission targeting a
not-yet-mapped external work references the external identity and leaves the
internal mapping `needs_review` — matching never auto-merges (Stage 2 rule reused).

## 2. Payload boundaries

A submission revision payload is a typed, allowlisted object — conceptually:

```
{ schemaVersion, target: {kind, catalogueUuid? , externalIdentity?},
  changeKind: new_metadata | correction,
  fields: [ {field, newValue, evidence: [{type: source_url|reference, value, license}]} ],
  intendedLicense, contributorNote? }
```

- **Allowlist only** — unknown top-level fields rejected (no mass-assignment), not
  silently stored. Bounded: ≤16 KiB total, ≤20 fields, ≤10 evidence URLs, each URL
  ≤2 KiB (CLOUD.md design caps).
- No arbitrary HTML; plain text with length limits, rendered escaped by any client.
- No file uploads, no image/audio embedding.
- **There is no field, nested object or escape hatch for personal practice data**
  (sessions, notes, repertoire, microphone, backups, profile) — the schema in §7.

## 3. Submission lifecycle state machine

```
local draft (device-only)
   │ explicit authenticated submit
   ▼
submitted ──moderator opens──► in_review ──approve──► approved ──publish tx──► published*
   │                                │                                    (* overlay read model)
   │ author withdraws               ├─request changes─► changes_requested ──author revises──► in_review
   ▼                                └─reject──► rejected (terminal; new submission to retry)
withdrawn (terminal before approval)
published ──later decision──► superseded | quarantined (reason required, evidence kept)
```

Rules:

- Transitions are **only** through server decision endpoints inside one transaction
  (API-CONTRACTS.md §6); no direct row writes from any client.
- Author may withdraw only while `submitted`/`in_review`; withdrawal after approval
  follows the correction/quarantine path instead (historical attribution preserved).
- Every transition appends a `review_events` row and an `audit_events` row.
- `changes_requested` → revised submission creates a **new immutable revision** and
  returns to `in_review`; the old revision stays readable forever.
- No automatic approval, no automatic publication, no bulk path.

## 4. Concurrency, revisions, idempotency

- `expectedRevision` (optimistic concurrency): every write names the entity revision
  the author/reviewer saw. Mismatch ⇒ **409 conflict** with current server state;
  clients reload and re-confirm (Stage 7 must render this as a conflict UI,
  HANDOFF-STAGE7.md).
- Concurrent moderator decisions on the same submission: the transaction re-reads
  status under lock/serializable semantics; the second decision gets 409 — never
  double-approve, never last-write-wins on decisions.
- `idempotencyKey` (client-generated UUID, stored per principal with request hash):
  replay with identical body ⇒ returns the original result, **no second effect**;
  replay with different body ⇒ 422; unknown/expired keys expire after ≥24 h.
- Approval transaction (canonical order, single atomic unit):
  1. authorize role + non-suspension + actor≠author;
  2. lock submission, compare status + expectedRevision;
  3. validate payload schema, evidence URLs (allowlist/SSRF), license intent;
  4. create `published_revisions` row + update/insert `published_entities`;
  5. advance submission status, append `review_events`;
  6. append hash-chained `audit_events` + change-feed entry;
  7. commit. Any failure ⇒ full rollback, no partial publication, retryable 5xx.

## 5. Publication rollback and quarantine

- Quarantine: administrator action marks a published entity `quarantined` with
  reason; the read projection stops serving it; the full history and attribution
  evidence remain. Rollback = point the entity at the previous published revision
  (new event rows; nothing rewritten).
- Quarantine/rollback are moderator/admin decision transactions with the same
  audit/idempotency guarantees; both appear in `GET /v1/catalogue/changes`.

## 6. Audit integrity

- Hash chain: `audit_events[i].prev_hash = audit_events[i-1].this_hash`;
  `this_hash = H(canonical row content)`.
- Periodic export includes the chain head; the restore drill recomputes and verifies
  the chain (ZERO-COST-OPS.md). This is **application-level tamper-evidence, not a
  claim of tamper-proof storage against the cloud provider** (baseline caveat
  retained).
- Client endpoints can only append (decisions), never update/delete. Database
  console edits by the operator are possible by nature of hosting — that trust
  boundary is documented, not hidden.

## 7. Personal-data isolation contract

- The community schema contains **zero personal practice data** by construction: no
  table references `opus.db`, no field exists for journal content, and validation
  rejects unknown fields, so a smuggled `{"notes": …}` object cannot persist.
- Conversely, the app never writes journal rows to the backend; the network layer
  for community features accepts only the typed submission objects (regression test
  target: assert request bodies against the schema allowlist).
- Deletion/pseudonymization shapes (**D5d, principle approved 2026-10-21, details
  pending**): the owner approved preserving legitimately published non-personal
  catalogue data where rights and applicable law permit, minimizing and appropriately
  erasing account-linked personal data, and supporting account deletion. Deleting a
  principal detaches identity from contributions (`principal → deleted`), keeps
  approved attribution pseudonymized if the contribution license requires, or removes
  attribution entirely where legally chosen. **Specific retention periods for
  pending/rejected contributions, audit records and backups remain unresolved**, as do
  attribution requirements, pseudonymization vs anonymization, legal exceptions and
  deletion semantics — all subject to pre-launch legal review (OPEN-DECISIONS.md §2).
  The API reserves `DELETE /v1/me` until those details are settled; **no retention
  period may be invented and no legal approval may be claimed**.

created by stokonn with love <3


## Future task 6.1 PostgreSQL requirements (2026-10-22, design input only)

[POSTGRESQL-DESIGN-REQUIREMENTS.md](POSTGRESQL-DESIGN-REQUIREMENTS.md) maps F1–F5
and the completed local comparison to required DDL/transaction/API verification.
Recommended SELECT … FOR UPDATE + audit_head mutex; FK/deletion semantics, targeted
idempotency conflict recovery, numeric safety and hosted transaction boundaries
still need authorized implementation and verification. No conceptual invariant or
approval gate is changed here; 6.1 remains unchecked.
