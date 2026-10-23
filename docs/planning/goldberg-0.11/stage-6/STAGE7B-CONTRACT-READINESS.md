# Stage6 → Stage7B contract readiness — local audit only

**Overall PARTIAL; T22 LOCAL PASS, not6.17 API freeze.** Source baseline e7dc7ea; owner-approved local T22
routing patch is the only service behavior change. Stage7B actual uncommitted
working bytes inspected read-only and hashed; legacy C6 is not silently promoted
into the current candidate. See contracts/admin/v1/ for versioned synthetic cases.

Local readiness:30 actual in-process/scripted-storage captures,11 inactive proposals,
and29 preserved authored Stage7B comparison scenes. No PostgreSQL/HTTP claim from
these captures. Existing host296 evidence remains historical for its exact source.
Post-patch Fedora version3 regression proof is now verified: outer108 + nested237 =345 executions,313 tests counting repeated units once,205 database-backed bodies. This closes T22 locally, not shared public HTTP integration.

G10 is CLOSED on Android and compatible with the local-only backend. G1–G9/G11
remain BLOCKED for real integration; approved bounded fallback UX does not invent
missing endpoints. The local fixture corpus is ready for explicit cross-client
review/testing, not activation.

## Post-T22 Fedora v3 evidence reconciliation

Tested source6d0abad, outer run-x0nDSTXd and nested run-TEmZzZQg are now verified:
345 successful executions/313 tests counting repeated unit suite once;205 physical
PostgreSQL-backed bodies. Explicit nested exit0,82 source hashes and container removal
output checked. T22 LOCAL PASS under the owner-approved local clarification. All
G1–G9/G11 integration dependencies below remain BLOCKED; G10 stays CLOSED. No new
wire contract, fixture activation, server endpoint or Android work. Any earlier
pending-PostgreSQL text below is historical preparation context, superseded here.

## G1 — BLOCKED

- **Normative:** No normative stats endpoint; bounded reads and truthful unavailable.
- **Actual backend:** No stats operation or counts/snapshot endpoint. Queue pagination is keyset without a global snapshot highwater; audit does have signed highwater.
- **Android expectation:** Owner-approved exact-or-unavailable bounded complete walks; Pending/Needs info from queue, Approved/Rejected today from audit.
- **Mismatch/missing:** No server totals or instantaneous completeness proof under concurrent queue changes; no approved server timezone/window semantics.
- **Fixtures:** proposal-stats-snapshot, proposal-statistical-vocabulary
- **Proposed resolution:** Approve one coherent snapshot/count contract or retain unavailable; do not label partial walk as exact total.
- **Approval:** Owner: count definitions, consistency point, timezone, time window, endpoint/DTO. Implementation authorization needed.

## G2 — BLOCKED

- **Normative:** Admin queue status/cursor design; <=20 rows.
- **Actual backend:** moderation.queue accepts only schemaVersion/cursor; fixed20; object {createdAt,id}; target_kind/current_revision text; no title/composer/owner/evidence count/search/status filter.
- **Android expectation:** Legacy string cursor, status and limit parameters; owner/revision/evidenceCount/epoch date; bounded detail hydration/exhaustive search fallback.
- **Mismatch/missing:** Actual candidate rejects unsupported parameters; no approved display/search DTO or HTTP cursor serialization.
- **Fixtures:** actual-queue-page, actual-queue-filter-rejected, stage7b-comparison/fixtures.json#candidate-queue, proposal-queue-search
- **Proposed resolution:** Approve safe display fields and server-filtered deterministic cursor scope. Keep current exact-or-unavailable fallback only against its supported legacy fixture dialect.
- **Approval:** Owner: query/display/DTO and HTTP binding; new server functionality is not delivered here.

## G3 — BLOCKED

- **Normative:** Admin audit bounded append-only view.
- **Actual backend:** admin.audit only schemaVersion/cursor, fixed20, signed scope/high/head/after; no filter/search.
- **Android expectation:** Legacy audit cursor+limit; client-side filtering over fetched role-scoped pages, never complete search claim.
- **Mismatch/missing:** No server filter/search and candidate fields differ.
- **Fixtures:** actual-audit-page-1, actual-audit-page-2, actual-audit-search-rejected, proposal-audit-search
- **Proposed resolution:** Approve filter/search vocabulary and bind filters to signed cursor/snapshot.
- **Approval:** Owner: filter/query scope, privacy, DTO and route; implementation absent.

## G4 — BLOCKED

- **Normative:** Quarantine/rollback published entity concept; no complete current-field comparison DTO specified.
- **Actual backend:** Detail is proposal submission/revisions/reviews. Public overlay gives displayTitle/revision/provenance only; not arbitrary current fields.
- **Android expectation:** Smart Comparison needs authoritative current entity before-values; earlier proposal never labeled current catalogue.
- **Mismatch/missing:** No admin current-field lookup; public display cannot supply full field comparisons.
- **Fixtures:** actual-detail, actual-public-display-7, actual-public-display-8, proposal-current-entity-values
- **Proposed resolution:** Approve minimal authorized current entity projection with revision/status/hash and allowlisted fields. Keep unavailable when missing.
- **Approval:** Owner: field/privacy/license projection and endpoint. No current-value service added.

## G5 — BLOCKED

- **Normative:** Session exchange/delete/capabilities; Google identity verified server-side, roles live, no presentation sign-in privilege.
- **Actual backend:** Separate challenge/login/refresh/session/logout operations; capabilities response lacks principalId; session exposes principalId/sessionId/familyId/expiresAt; logout204/null; opaque token rotation/replay revocation.
- **Android expectation:** Capabilities requires principalId; legacy signOut expects revoked JSON. Android live exchange/refresh lifecycle is not integrated.
- **Mismatch/missing:** No approved HTTP binding or principal composition; AuthLocalApi body/version allowlists differ from generic normative body conventions; capabilities alone cannot identify current account.
- **Fixtures:** actual-capabilities, actual-session, actual-logout-204, actual-role-revoked, proposal-session-http-lifecycle, proposal-session-principal-binding
- **Proposed resolution:** Approve coherent authenticated principal/token/capability lifecycle DTO and route mapping.
- **Approval:** Owner: wire binding, principal/session lifecycle and stored-token semantics; real OAuth remains separately gated.

## G6 — BLOCKED

- **Normative:** Decisions use expectedRevision; reason rules; conflict409 and stable idempotency keys.
- **Actual backend:** Explicit moderation.open from submitted; decisions only in_review. Entity expectedRevision and proposal expectedSubmissionRevision both required safe JSON numbers. Approve201 id/status/entityId/new entity revision; other decisions200 id/status only. Generic409 lacks current values; current auth before replay; exact replay body incl original requestId; changed body422.
- **Android expectation:** Legacy one expectedRevision, no open method; all successes parsed as submissionId/decision/revision; conflict fields optional. Decisions disabled.
- **Mismatch/missing:** One revision cannot be heuristically mapped; missing expectations cannot default; current state refetch required for409.
- **Fixtures:** actual-open, actual-decision-approve, actual-decision-reject, actual-decision-request_changes, actual-approve-replay, actual-error-409, actual-error-422, proposal-dual-revision-http
- **Proposed resolution:** Approve explicit open plus dual meanings, separate entity/proposal result fields, exact replay/conflict UX contract; then one versioned adapter.
- **Approval:** Owner: whole decision contract; no mapping activated. Local capture is not database atomicity or HTTP proof.

## G7 — BLOCKED

- **Normative:** /v1 design and error envelope; no arbitrary unknown-field acceptance.
- **Actual backend:** In-process operation/body adapter only; no HTTP listener or method/path binder. Raw detail rows, object queue cursor, signed string audit/public cursor.
- **Android expectation:** Legacy HTTP facade requires distinct shapes; rejects incompatible object cursor/DTOs; no dialect inference.
- **Mismatch/missing:** Public DTO projection, microsecond timestamp representation, method/path binding and cursor serialization are not frozen.
- **Fixtures:** actual-queue-page, actual-detail, actual-audit-page-1, proposal-public-binding, all actual-error-*
- **Proposed resolution:** Review/approve explicit public projections and HTTP binding after local fixture agreement.
- **Approval:** Owner: contract decisions; formal6.17 freeze NOT started or complete.

## G8 — BLOCKED

- **Normative:** Lossless typed catalogue identity/revision conventions, no numeric rounding.
- **Actual backend:** PG BIGINT reads strings. Audit/admin entity/public int8 reads decimal strings up to9223372036854775807; moderation revision writes safe JS number0..9007199254740991.
- **Android expectation:** Legacy positive Int revision<=2147483647; Long audit parser accepts exact integer/string up to Long.MAX_VALUE; rejects fractions/overflow; no candidate widening.
- **Mismatch/missing:** Zero/new entity semantics, revision range/number-vs-string and timestamp precision need explicit agreement.
- **Fixtures:** actual-bigint-9007199254740993, actual-bigint-9223372036854775807, actual-decision-approve, proposal-revision-representation
- **Proposed resolution:** Choose per-field representation/ranges; preserve audit strings/opaque cursors; refuse unsupported writes rather than clamp/round.
- **Approval:** Owner: field-specific range/representation; existing Android parser hardening can remain.

## G9 — BLOCKED

- **Normative:** Append-only audit chronology/hash chain; today/window authority not specified.
- **Actual backend:** Transitions submitted->in_review, in_review->approved, approved->published, in_review->rejected/changes_requested, role.*, principal.*, session.*. Approve produces TWO transitions. Audit numeric seq with captured signed highwater/head.
- **Android expectation:** Legacy decision.approve/reject, epoch ts, actor/subject/camelCase hashes/chainHead. Stats uses device ZoneId.systemDefault today and complete bounded audit walk.
- **Mismatch/missing:** No automatic action aliases; double-counting hazard; no approved server timezone or statistical window.
- **Fixtures:** actual-audit-page-1, actual-audit-page-2, proposal-statistical-vocabulary
- **Proposed resolution:** Approve one authoritative approval event plus timezone/window/snapshot semantics. Keep exact-or-unavailable and raw vocabulary.
- **Approval:** Owner: vocabulary, statistical authority, time/date/actor DTO.

## G10 — CLOSED_ANDROID_STATIC_COMPATIBILITY_CONFIRMED

- **Normative:** Unavailable/not configured must remain truthful, local practice independent.
- **Actual backend:** No deployed endpoint; local adapter only; no activation is required or proposed.
- **Android expectation:** Actual working-tree AdminApi.forApplication chooses DisabledAdminTransport absent explicit test override; OpusApplication decisionsEnabled=false independently.
- **Mismatch/missing:** No reopening: static compatibility confirmed against current read-only sources; no new Android runtime claim.
- **Fixtures:** stage7b-readonly-source-sha256.json
- **Proposed resolution:** Preserve the closed Android disabled-network boundary and independent decision kill; fixture-only local work can proceed.
- **Approval:** No new owner decision needed to retain this boundary; enabling networking/actions requires explicit later approval.

## G11 — BLOCKED

- **Normative:** Strict schemaVersion/allowlists, 16KiB/20fields/10URLs; reason-required actions and reviewed provenance; no universal license acceptance.
- **Actual backend:** Plain text rejects HTML/control/encoded-dangerous forms. Reason max1000 UTF-8 bytes; reject/request_changes/quarantine/rollback required. Metadata strings<=2048; depth7/node1000; injected field/license/source allowlist, empty default-deny; evidence stored never fetched.
- **Android expectation:** Legacy confirmation max2000 characters; validation/422 mapped Validation; current reviewed source policy not agreed.
- **Mismatch/missing:** Byte/character limits differ; no real source/field/license policy. T22 400/401/403 classification approved locally, public HTTP binding remains open.
- **Fixtures:** actual-error-400, actual-error-422, actual-error-403, proposal-validation-policy
- **Proposed resolution:** Approve compatible byte limits/reason semantics/source evidence policy; keep real content default-deny. Distinguish local T22 classifications without adding privilege.
- **Approval:** Owner: real field/source/license policy and DTO validation; local T22 patch explicitly approved, not a license or HTTP approval.

## T22 and Android error handling

Current approved local routing distinguishes malformed400, registered missing/invalid
session401 and denied capability/self-grant403; well-formed unregistered audit or
bootstrap operation403 without dispatch. AdminApi maps400/422 to Validation,401 to
Unauthorized and403 to Forbidden; only401/403 invoke onAccessDenied and invalidate
local access/trust. Therefore the former absent-route400 was misleading validation
UX, not demonstrated privilege escalation. The approved403 follows default-deny;
Android must not reinterpret403 as success or retry by changing body/key.
T22's published FAIL/raw logs remain preserved; post-patch PostgreSQL assertions now pass; no HTTP binding or integration acceptance is implied. Malformed input remains
400, and no audit-edit/bootstrap endpoint is created.

## Safe work now / still disabled

Android can retain disabled production transport, independent decision disable,
strict legacy numeric/cursor parsing, error taxonomy, conflict draft preservation,
explicit confirmation and local exact-or-unavailable states. It can review/import
the shared fixtures in test-only work after coordination. It must not bind new
routes, choose a dialect by shape, infer/default either revision, synthesize current
catalogue values, double-count audit transitions or enable real networking/actions.

Before integration: owner-approved G5/G6/G7 DTO and session binding, G8/G9 numeric/
audit/time rules, G11 validation/source policy; implement approved G1–G4 functionality
under separate scope; shared backend/client fixture contract tests after an approved binding, future authorized
HTTP/error/cursor tests (current local PostgreSQL rollback/auth/replay proof is verified),
and separately permitted Android runtime/upgrade/UI evidence. No emulator in this
milestone. Hosted/OAuth/collection gates remain separate. No6.13–6.18 started.

Source and fixture hashes are in contracts/admin/v1/source-provenance.json; exact
inspected client hashes in stage7b-readonly-source-sha256.json. Previous Stage7B
mapping-proposal stays unapproved/inactive. No changes to its worktree.

created by stokonn with love <3

AuthLocalApi capability/session calls use operation-specific bodies without schemaVersion; the new actual-auth-schemaVersion-rejected capture proves400 for that extra field. This is a G5/G7 binding decision, not permission to strip/version fields heuristically.
