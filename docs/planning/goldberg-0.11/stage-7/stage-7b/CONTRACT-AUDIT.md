## Stage 7B preserved checkpoint and CI continuation — 2026-10-23

**Stage7B IN PROGRESS; acceptance incomplete.** Source/evidence checkpoint
`0703cd8ec40a86e6f92e7fd9cf172c5921e1e90e` committed and ordinary-pushed; exact
remote SHA readback PASS. Initial cee9330 +26 modified tracked /125 publishable new
files preserved; private PMP excluded. Fresh JVM **275/0/0/0**,36 suites,12s;
assembly/lint gate SUCCESS (mostly cached), six host scripts and syntax checks PASS.
No app-source/contract/private PMP/prior-evidence bytes changed during recovery.

[API36 CI run40011091868](https://github.com/stokonn/Goldberg/actions/runs/40011091868)
FAILED before build/tests at this exact SHA: emulator executable absent during
pre-build diagnostics (exit127). Zero test bodies; no application failure. Narrow
verified fix explicitly installs emulator before diagnostics. Corrected commit
`3e04e2b716435684a0d4d88a71a4a59407201ada` pushed with exact remote SHA readback;
[run40011450671](https://github.com/stokonn/Goldberg/actions/runs/40011450671)
FAILED before build/tests: installed emulator lacks libpulse.so.0 (exit127).
Second narrow CI fix installs libpulse0 before diagnostics; corrected run pending. Issue31 dashboard updated (connector403; authenticated gh succeeded),
umbrella OPEN / Project In Progress; final chronological checkpoint awaits results.
See [SAFE-RECOVERY.md](SAFE-RECOVERY.md) for scope/tasks/audit/commands/artifact identity.

Stage7A finally owner-accepted; X10 NOT FIXED/NOT VERIFIED, owner-deferred Gould0.12.
Stage8 material readability/accessibility/touch targets remain required. Existing
historical summaries below are superseded for current status/authorization only.
Production networking/decisions DISABLED; G1–G9/G11 integration and Admin visual/
accessibility/WorkManager/notification/upgrade/owner acceptance remain pending.

---

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

## Codex read-only cross-check of current Stage 6 candidate (2026-10-23)

Inspected immutable Git objects at `2f1524e4083a98a64d5fa9790f3cdc002d1f8237`
(`implement/goldberg-0.11-stage-6-10-11-local` at time of inspection), **without
reading or modifying its working tree**. This supersedes any implication above
that the C6 spike is the latest backend candidate. Normative API-CONTRACTS §6 still
lists the earlier body, while the newer local handler README explicitly calls its
additional details **local candidate fixtures, not a frozen client API**.

- **G4 — current catalogue before-values:** the legacy admin detail supplies proposal
  snapshots, not verified current catalogue field values. UI must say unavailable;
  previous proposal revisions must never be labeled current catalogue values.
- **G5 — Android live session integration:** this branch has encrypted token storage
  but no approved end-to-end admin credential exchange/refresh path. Stage 6's local
  Android candidate uses `DisabledCommunityTransport`; its synthetic fixtures are
  not a deployed session API. Do not connect local Google identity directly to roles.
- **G6 — revision contract drift (blocks live decisions):** current local moderation
  handlers distinguish `expectedRevision` (entity) and `expectedSubmissionRevision`
  (immutable proposal). They also support an explicit open transition and different
  decision success codes (approve 201, others 200). Legacy C6 uses one revision.
  Do not guess mappings, add an invented HTTP operation, or treat the old client as
  integrated. **Production `OpusApplication` sets `decisionsEnabled = false`; the
  legacy decision state machine is exercised only through hermetic fixtures.**

Stage 6 coordination request (Issue #31, existing umbrella only): freeze shared
request/response fixtures for capabilities, queue/detail, open, dual expectations,
audit BIGINT/cursors and session lifecycle; specify current catalogue values and
G1/G2/G3 server queries. After agreement, adapt this one Android facade and rerun
shared integration tests. This is not authority to deploy, merge Stage 6 code or
change any Stage 6 gate. No live endpoint or backend test was executed here.

## API integration preparation — committed comparison (2026-10-23)

Pinned source: **bcc779685d79e99451796b3fa45657b3bca8dd99**, latest committed
Android host-verified lineage plus packaging/recovery clarification. Later local
security tip **faf9ba3** has byte-identical auth/handler/admin implementations;
it adds security probes/docs, not an approved HTTP binding. Read through Git
objects only; Stage 6 worktrees untouched. Provenance and 29 authored cases:
[shared comparison fixtures](../../../../../contracts/admin/v1/README.md).
No backend test, listener, production configuration or new endpoint is created.

The initial “C6 + normative are the reference implementation” table above is
historical. C6 remains a legacy dialect; the normative document is design-level,
not a complete success-DTO freeze. The current candidate is in-process only.

| Area | Normative / legacy C6 Android | Committed local candidate | Disposition |
|---|---|---|---|
| Auth/session/capabilities (G5) | `/v1/session` lifecycle; C6 capabilities includes principalId; DELETE returns revoked:true | challenge/login/refresh/session/logout operations; capabilities has only requestId + capability array; session has principalId/sessionId/familyId/expiry; logout 204/null | No guessed HTTP binding or principal composition; session integration OPEN |
| Queue/search (G2, G7) | status/cursor/limit; row owner/revision/evidenceCount/epoch createdAt; string cursor | only schemaVersion/cursor; fixed ≤20; target_kind/current_revision text/microsecond createdAt, no owner or display/evidence labels; object cursor `{createdAt,id}` | Missing status/search/limit parameters are rejected; no stringification or query invention |
| Detail (G4, G7) | id/owner/status/revision/revisions/reviewEvents DTO | submission row + revisions (text revision/payload/hash) + reviews (from/to/reason/audit_seq), no review timestamps or actor in that projection | Do not default absent values or assert raw rows are a frozen public DTO |
| Decisions (G6) | single expectedRevision treated as proposal; all success bodies carry submissionId/decision/revision | explicit moderation.open (submitted only) first; only in_review may be decided; both entity and submission expectations required; approve 201 id/status/entityId/new entity revision; reject/request_changes 200 id/status only | Mapping proposal is unapproved and inactive; neither expectation may be inferred/defaulted |
| Conflicts/replay (G6) | C6 409 details currentStatus/currentRevision | generic 409 without current fields; both revision and state conflicts; live authorization before identical operation/body/key stored replay; 422 on changed body/key reuse | Re-read latest detail on 409, never invent current revisions or auto-resubmit; validate replay under agreed result semantics |
| Audit (G3, G7–G9) | seq/ts/actor/action/subject/camelCase hashes, chainHead; opaque string cursor | decimal seq/target_revision, actor_id/target_id/created_at/underscore hashes/canonical_content, head object; signed scope/highwater cursor | Preserve opaque cursor, reject incompatible DTOs, no silent hash/name/date/unit conversion |
| Stats (G1, G9) | legacy decision.approve/reject events, exact-or-unavailable bounded complete walks | transitions in_review->approved then approved->published, plus rejection/change-request transitions | Two approval events cannot be treated as two approvals; no automatic alias/count mapping |

### Additional gaps

- **G7 — DTO and cursor dialect freeze OPEN.** Exact public projections, route ↔
  operation binding, object queue cursor serialization, date precision and error
  response envelopes must be approved. Candidate fields are not auto-adapted.
- **G8 — numeric representation boundary OPEN for integration.** PG audit BIGINT
  permits 9223372036854775807 as decimal text; local moderation revision inputs are
  JS safe integers (0…9007199254740991), while legacy Android uses positive Int.
  Exact parsing of legacy-supported Long/Int values can be hardened now, but range
  widening, entity-zero semantics and candidate conversion require approval.
- **G9 — audit vocabulary/statistical window OPEN.** Freeze which transition counts
  as one approval, the authority/timezone for “today”, actor/date/history fields,
  snapshot completeness and cursor/head semantics. No arbitrary decision.* alias.
- **G10 — Android networking boundary:** the inherited editable endpoint previously
  selected a real HTTPS transport. This task closes that local safety gap by making
  the application default transport explicitly disabled even for a stored endpoint.
  Fixture transport is test-only. Decisions remain disabled independently.

### Proposed dual-revision mapping — APPROVAL REQUIRED, NOT ACTIVATED

Candidate submission.expected_revision → expectedRevision (entity expectation);
submission.current_revision → expectedSubmissionRevision (immutable proposal).
Approval result.revision is a **new published entity revision**, never proof that
proposal revision equals it. Reject/request_changes omit revision entirely. A
missing value must block a decision, not become 0 or 1. For a new entity, 0 requires
explicit approved server evidence. Explicit open/state transitions, confirmations,
receipt/replay fields and conflict refetch must be frozen together. Current C6
request builder and review flow are not silently changed to this mapping.

Decisions needed from Stage 6/owner: approve the public DTO/HTTP binding and both
revision meanings; decide capabilities/session principal binding; supply current
catalogue field values; freeze audit decimal/date/cursor/action semantics, error
and replay result shapes; settle G1–G3 query support. Only then implement a
versioned adapter and test it against an isolated approved local backend.

- **G11 — validation/policy limits OPEN.** Candidate non-approve decisions require
  a nonblank reason, with 1000-byte UTF-8/plain-text validation; the legacy Android
  confirmation currently limits 2000 characters. Do not claim equivalent accepted
  content. Field/source/license rules are trusted injected/default-deny candidate
  policy, not an approved real-source policy. Freeze request byte/field/reason limits
  and reviewed license/evidence semantics with the shared contract.


## Recovery disposition — 2026-10-23

Audit and Android-only preparation verified; G1–G6 remain OPEN. G7–G9 and G11
also remain integration decisions. G10 local disabled-transport change and parser
safety regressions already implemented before interruption are verified by the
recovered 275-test gate (24 contract cases); six host scripts rerun PASS during
recovery. No candidate adapter, revision mapping or HTTP operation activated.
See CHECKLIST.md and results/contract-preparation-2026-10-23 for exact recovered
versus newly executed evidence. Approval, runtime and live integration remain pending.
