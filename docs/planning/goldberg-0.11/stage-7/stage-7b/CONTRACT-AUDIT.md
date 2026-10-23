# Stage 7B.1 — Stage 6 contract and readiness audit

**Date:** 2026-10-23 · **Branch:** `feature/goldberg-0.11-stage-7b` (based on Stage 7A
`576f0d2`) · **Status:** COMPLETE — audit executed, gaps recorded, owner decisions
obtained for the two open forks (stats derivation, search bounds).

Purpose: verify what the Stage 6 backend actually provides today before writing any
Android admin code, so no endpoint, field or behavior is invented. Sources inspected:

- Normative contract: `docs/planning/goldberg-0.11/stage-6/API-CONTRACTS.md` (branch
  `docs/goldberg-0.11-stage-6-planning`) — design only, tasks 6.1–6.18 all unchecked.
- C6 contract spike (branch `spike/goldberg-0.11-stage-6-contract`, `d2a95d4`):
  `spikes/contract-c6/src/server.mjs` — in-process simulator, 79/79 local tests.
  Wire shapes below were read from this code, not inferred.
- C2 BFF spike (branch `spike/goldberg-0.11-c2-bff`, `7bc6976`) — local-only Cloudflare
  Worker stub; capability naming diverges (`admin:queue_read` vs C6 `queue.read`);
  **C6 + API-CONTRACTS.md are the reference implementation for 7B.**
- Cloud reality (Issue #31, 2026-10-22 checkpoint): Cloudflare 0 Workers, no
  workers.dev subdomain; Supabase project exists but 0 tables / 0 migrations / 0 Edge
  Functions. **No Stage 6 endpoint is reachable anywhere.** 6.0d/6.0e not authorized.

## Contract matrix (requirement → verified support)

| 7B requirement | Verified contract support | Verdict |
|---|---|---|
| Role/capability gate | `GET /v1/me/capabilities` → `{principalId, capabilities:[…], note}` — "UI convenience only — never an authorization source"; server enforces per endpoint | ✅ |
| Queue listing + filters | `GET /v1/admin/queue?status=&cursor=&limit=` → `{items:[{id, owner, status, revision, evidenceCount, createdAt}], nextCursor}`; single `status` value; default set `submitted+in_review+changes_requested`; cursor `"<created_at>:<id>"`, limit ≤20, deterministic `(created_at,id)` order | ✅ (multi-filter = client-side over the default walk) |
| Proposal detail + history | `GET /v1/admin/submissions/{id}` → `{id, owner, status, revision, revisions:[{revision,payload,createdAt}], reviewEvents:[{revision,decision,actor_id,reason,created_at}]}` | ✅ |
| Approve / Reject / Request changes | `POST /v1/admin/submissions/{id}/decisions` `{schemaVersion, decision∈{approve,reject,request_changes}, expectedRevision, reason?, idempotencyKey(required)}` → 201 `{submissionId, decision, status, revision, publishedRevisionId?, entityUuid?}`; 409 `{currentStatus, currentRevision}` on stale revision | ✅ (`request_changes` → server status `changes_requested`; "NEEDS_INFORMATION" is the same semantic, different name) |
| Conflict safety | `expectedRevision` + 409 with current server state; client reloads and re-confirms | ✅ |
| Audit log | `GET /v1/admin/audit?cursor=&limit=` → `{items:[{seq, ts, actor, action, subject, prevHash, thisHash}], nextCursor, chainHead}`; append-only, oldest-first (`ORDER BY seq`) | ✅ listing; ❌ no server search/filter (client-side over fetched pages only) |
| Deferred / Re-review markers | Explicitly personal local UX markers, not backend states | ✅ by design (local, account-scoped, non-sensitive) |
| Elevated verification | No step-up endpoint; approved scope's "elevated verification" = fresh `GET /v1/me/capabilities` re-check + 10-minute non-sliding local trust window | ✅ client-side per approved scope |
| Session sign-out | `DELETE /v1/session` → `{revoked:true}` (idempotent) | ✅ |
| **Dashboard stats** | **No stats/counts endpoint exists in contract, C6 or C2** | ❌ **gap G1** |
| **Queue search (title/composer/type)** | **No search parameter exists; queue rows carry no title/composer** (`{id, owner, status, revision, evidenceCount, createdAt}` only) | ❌ **gap G2** |
| Audit search/filter | No server parameters | ❌ **gap G3** (client-side only) |

## Recorded gaps (Stage 6 dependencies — not worked around with invented APIs)

- **G1 — stats endpoint.** Preferred durable fix: `GET /v1/admin/stats`. interim,
  owner-approved derivation (2026-10-23): exact counts only from complete authorized
  server results within bounded page walks (Pending/Needs info from the queue walk;
  Approved/Rejected today from the audit chain); any tile whose window cannot be
  covered completely shows an explicit unavailable state — never a partial count
  presented as a total.
- **G2 — queue search.** Interim, owner-approved (2026-10-23): ID search resolves via
  the verified detail endpoint; title/composer/type deep search runs only when the
  entire eligible queue is exhaustively scannable within safe request bounds
  (`SearchPolicy`, see `AdminRepository`); otherwise the UI states that full search
  requires a Stage 6 server-side search endpoint and never silently omits matches.
  Queue rows are hydrated for display via the verified detail endpoint (≤20 per page,
  bounded); carrying title/composer in queue rows is the durable contract fix.
- **G3 — audit search/filter.** Client-side filtering over fetched, role-scoped pages;
  server-side search is the durable fix.
- **Naming:** the 7B prompt's `NEEDS_INFORMATION` maps to the verified server status
  `changes_requested` (decision `request_changes`). The UI label is "Needs info".
- **C6 findings that are backend inputs, not Android concerns:** PG READ COMMITTED
  double-approve (F1 → task 6.1 must use `FOR UPDATE`); roles/suspend endpoints not
  accepting `idempotencyKey`; `POST /v1/admin/entities/{id}/rollback` and
  `DELETE /v1/me` in contract but absent from C6.

## Android-side consequences

- All admin traffic goes through one contract-shaped client (`admin/AdminApi.kt`)
  with a **configurable base URL (empty by default)**; no live URL exists, so the app
  shows honest "not configured / cannot reach service" states instead of fake data.
- Verification uses an injectable transport seam (`adminTransportOverride`, mirroring
  the established `bundleConnectOverride` pattern) with fixture payloads copied from
  the verified C6 response shapes — clearly fixture-driven, never presented as live
  backend evidence.
- Session tokens are Keystore-encrypted at rest (platform-protected) and excluded from
  backups by the existing full exclusion rules.

created by stokonn with love <3
