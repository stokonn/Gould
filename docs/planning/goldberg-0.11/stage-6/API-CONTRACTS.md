# Stage 6 — Versioned API contracts (`/v1`)

**Design only.** Names were proposed in
[COMMUNITY-ADMIN.md](../COMMUNITY-ADMIN.md) §"Moderation lifecycle and API contract"
and are fixed here into an implementation-ready, client-agnostic contract. **One API
serves all clients**: the app's contributor surface, the Stage 7 Android Admin and
the Stage 7 Web Admin. Clients differ only in UI, never in server semantics.

## 1. Conventions

- Base path `/v1`; breaking changes require `/v2` (additive fields are allowed
  within `/v1`; clients must ignore unknown response fields, while **servers reject
  unknown request fields**).
- All request bodies carry `schemaVersion` (integer, currently 1).
- All writes require an authenticated session; all writes accept an
  `idempotencyKey` (UUID); reads are bounded (≤20 items/page, cursor pagination,
  deterministic ordering by `(created_at, id)`).
- Errors use a single envelope:
  `{error: {code, message, requestId, details?}}` — `message` is safe for display,
  `details` never leaks internals, tokens, other users' data or stack traces.
- Every response carries `requestId`; audit events reference the same ID.

## 2. Error taxonomy (clients must distinguish all of these)

| HTTP | Code class | Meaning / client behavior |
|---|---|---|
| 400 | `validation_failed` | Malformed/oversized/unknown-field payload; do not retry without change. |
| 401 | `unauthenticated` | Missing/expired/invalid session; re-authenticate. |
| 403 | `forbidden` | Authenticated but capability missing (default-deny); do NOT retry. |
| 404 | `not_found` | Nonexistent **or** existence-hidden resource. |
| 409 | `conflict` | Stale `expectedRevision` or concurrent decision; reload + re-confirm. |
| 422 | `idempotency_mismatch` | Same key, different body; use a new key. |
| 429 | `rate_limited` | Quota exceeded; back off using `Retry-After`; keep local draft. |
| 503 | `service_unavailable` | Kill switch on, provider paused/exhausted, maintenance; read-only/last-known state, never fake success. |
| 5xx | `internal` | Partial-failure rollbacks happened server-side; safe to retry idempotently. |

Validation errors are always distinct from auth, conflict, quota and availability
errors (baseline rule kept). Retries never create a second publication.

## 3. Session and identity endpoints

| Endpoint | Auth | Semantics |
|---|---|---|
| `POST /v1/session` | Google credential (body) | Verify (AUTH.md §2), mint session; returns session token + minimal `{principalId, displayName?, roles[]}`. |
| `DELETE /v1/session` | session | Immediate server-side revocation; idempotent. |
| `GET /v1/me/capabilities` | session | Server-computed capability list for this principal (convenience for UI; **never** an authorization source). |
| `DELETE /v1/me` | session | Reserved for the D5d account-deletion flow; principle approved 2026-10-21, **not implemented until the D5d retention/deletion details are settled** (OPEN-DECISIONS.md §2). |

## 4. Contribution endpoints (contributor)

| Endpoint | Capability | Semantics |
|---|---|---|
| `POST /v1/submissions` | `submit` | Create submission + revision 1 from allowlisted payload (DATA-MODEL.md §2). Enforces per-principal caps. |
| `GET /v1/submissions/mine` | `submit` | Own submissions only (ownership in query); cursor-paged. |
| `GET /v1/submissions/{id}` | owner or moderator | Others' pending submissions are existence-hidden (404). |
| `POST /v1/submissions/{id}/revisions` | owner, status=changes_requested | New immutable revision; returns to `in_review`. |
| `POST /v1/submissions/{id}/withdraw` | owner, pre-approval | Terminal withdrawal with optional reason. |

Drafts remain device-local. `returned` may be a UI label for
`changes_requested`, never a separate server status or wire alias.

Contributors can never set author, status, approval, roles or publication fields;
those fields in a request body cause 400, not silent stripping-with-effect.

## 5. Public read endpoints

| Endpoint | Auth | Semantics |
|---|---|---|
| `GET /v1/catalogue/overlay?kind=&cursor=` | none | Approved+published overlay entities only; typed envelopes per Stage 4 result shape (kind, UUID, display title, provenance summary, revision). |
| `GET /v1/catalogue/changes?cursor=` | none | Bounded change feed of published revisions/quarantines/supersessions with provenance. |

Pending submissions, identities, roles and audit details are **never** publicly
readable. The overlay is small metadata only — it is not an encyclopedia mirror and
not a bundle service (CLOUD.md sizing warning applies).

## 6. Admin endpoints (moderator + administrator)

| Endpoint | Capability | Semantics |
|---|---|---|
| `GET /v1/admin/queue?status=&cursor=` | `queue.read` | Pending queue with evidence summaries; deterministic order. |
| `GET /v1/admin/submissions/{id}` | `queue.read` | Full immutable revision history + review events + evidence. |
| `POST /v1/admin/submissions/{id}/decisions` | `decide` | Body `{decision: approve\|reject\|request_changes, expectedRevision, reason?, idempotencyKey}`. Runs the DATA-MODEL.md §4 approval transaction. Actor≠author enforced. |
| `POST /v1/admin/entities/{id}/quarantine` | `quarantine` (admin) | Reason-required; audited; read projection stops serving. |
| `POST /v1/admin/entities/{id}/rollback` | `quarantine` (admin) | Repoint to previous published revision; audited. |
| `GET /v1/admin/audit?cursor=` | `audit.read` (admin) | Hash-chained audit export view; append-only. |
| `POST /v1/admin/principals/{id}/roles` | `roles.manage` (admin) | Grant/revoke; audited; **no self-grant**. |
| `POST /v1/admin/principals/{id}/suspend` | `roles.manage` (admin) | Suspend/unsuspend; effective immediately server-side. |

There is **no** client endpoint that edits the audit log, service-role credentials
or infrastructure configuration (deployment-operator duties stay out of band).

## 7. Abuse controls wired into the contract

- Per-principal: ≤5 submissions/day, ≤10 pending, ≤16 KiB body, ≤20 fields,
  ≤10 evidence URLs (CLOUD.md design caps, tunable pre-beta).
- Coarse IP throttling with short retention complements account limits; global
  intake **kill switch** flips writes to 503 (`service_unavailable`) while reads may
  continue.
- Quota accounting is atomic; exhaustion returns 429 with `Retry-After`. Pending
  submissions and audit evidence are **never** deleted to regain quota.
- Attestation/CAPTCHA may later supplement — never replace — auth/authz.

## 8. Contract fixtures

Every endpoint ships (at implementation time) with request/response fixtures in the
style of Stage 2's `verify_stage.py` suite: valid cases, each error class, boundary
sizes, unknown-field rejection and idempotency replays. These fixtures are the
shared asset both Stage 7 Admin clients are built and tested against
(HANDOFF-STAGE7.md).

created by stokonn with love <3
