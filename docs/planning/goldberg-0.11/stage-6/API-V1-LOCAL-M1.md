# API v1 local implementation — Milestone 1

Owner-authorized narrowly scoped LOCAL implementation from approved6dd3136 on
feature/goldberg-0.11-stage-6-api-v1-local in `/home/stokonn/Projects/Goldberg-Stage6-API`.
[O1–O8 design approval](API-V1-OWNER-DECISIONS-2026-10-23.md) remains normative design;
this checkpoint records actual implementation separately from approved but absent
functionality and unapproved real-world policy. No formal Stage6 completion/freeze.

## Implemented backend boundary

`spikes/stage6-api-v1-local/src/adapter.mjs` explicitly maps raw in-process method/
path/headers/UTF-8 JSON to unchanged AuthLocalApi/SessionService/Authorization and
CommunityApi/ModerationService methods. There is no listener or Android dependency.
SchemaVersion1 is mandatory on POST; exact allowlists are checked before projecting
operation-specific local bodies. Unknown/duplicate/nested unsafe fields, malformed
UTF-8/Unicode/JSON, request-ID injection and unsafe numeric forms fail400 before dispatch.
Known-route wrong-method405 includes Allow; absent routes404. Direct CommunityApi
unknown-operation403 is retained; no audit-edit/bootstrap/role-control endpoint added.

| Executable local operation | Actual result |
|---|---|
| POST /v1/session/challenge |201 with actual stored challenge expiry; single-use/expiry checked by original service |
| POST /v1/session |201 with validated principal/session and both tokens/actual expiry; fixed15m/7d durations |
| POST /v1/session/refresh |200 with rotated pair and unchanged absolute family expiry; used replay revokes family401 |
| GET /v1/session |200 verified principal/session/UTC expiry; invalid/expired/revoked401 |
| GET /v1/me/capabilities |200 principal/session and live capabilities from one session transaction; UI hints only |
| DELETE /v1/session |204 empty/no Content-Type; same underlying logout and audit/header request ID |
| POST /v1/admin/submissions/{id}/open |200 typed OpenResult; explicit entity/proposal expectation guards |
| POST /v1/admin/submissions/{id}/decisions |approve201/published; reject and request_changes200 with actual proposal revision; no invented entity revision |

Suspension/live denied grants/self-review403; missing submission404; stale entity,
proposal/state409 with no invented current values. Refetch/reconfirmation is required,
never automatically retried; the public Detail route is not yet implemented. Changes
between authorization and replay cannot bypass existing transactional session locks.
Canonical public method/path/body/key identity is persisted with exact public JSON
result/status/requestId. Reused key with changed operation/body422. Original actor,
state, locking, evidence validation, paired audit and feed semantics stay unchanged.

## Minimal persistence/identity corrections

No DDL or old service mutation. New PublicReceipts wrapper reuses the existing table,
storedResult/insertResult helpers and lock namespace; projects the public response
inside the domain transaction and samples created_at at effect/DTO completion. SQL
expires_at=created_at+24h gives exact24h retention; replay lookup clock sampled after
lock waits. Failed projection/insertion rolls back domain and receipt together. This
has real service/scripted-SQL execution proof, but lacks new PostgreSQL proof.

AuthLocalApi deliberately drops its generated ID when returning204/null. The public
logout calls the same SessionService with the public request ID directly to preserve
header/audit correlation. Login/refresh projections query the actual authenticated
session and family after minting; failure/loss of this response requires new
authentication. No arbitrary token decoding or inferred expiry fields. Tokens and
credential data are never emitted in logs, errors, backups or exports by this module.

## Numeric/cursor safety and remaining G5–G8 gaps

| Gap | Local result | Remaining gap |
|---|---|---|
| G5 |Challenge/exchange/identity/capabilities/expiry/refresh/logout/revocation executable with synthetic providers; approved lifetimes; real RSA/JWKS fixture exchange |Post-mint metadata enrichment is a second authenticated transaction; client single-flight refresh/credential clearing/account switching and real OAuth/integration remain outside scope. Server refresh intentionally does not coalesce replay. |
| G6 |Open/three outcomes/independent revisions/conflicts and exact public receipts execute using real services |Latest Detail/refetch/history projection absent; PG locking/durability/collision acceptance and real client confirmation remain unverified. |
| G7 |Explicit routes, schema/body mapping, safe error envelopes, request IDs and204 semantics |`/v1` remains provisional; public compatibility absence unconfirmed. No actual HTTP/proxy/browser/headers serialization venue; only listed operations registered. |
| G8 |Canonical decimal strings preserve full signed-int8 storage domain with field-specific nonnegative/positive ranges on decoding; UTC six-digit precision; opaque canonical scoped HMAC cursors15m |Existing moderation writes accept only safe JS integers; valid larger values fail503 after live auth. Increment beyond safe range remains existing domain409/rollback. Cursor codec has no queue/audit endpoint; do not claim full-int8-write or read-service support. |

Public version inspection: existing normative/proposal/C6 contracts inspected; current
Android boundary remains disabled and no HTTP listener in original/new service source.
These local observations cannot establish absence of every approved/deployed v1
consumer. Gate OPEN; local/provisional only. An incompatible consumer would require
separate owner decision about v2; no silent version adoption/change.

G1–G4 server functionality absent. Real source/field/license/attribution/evidence
policy remains empty/default-deny; only test harness injects clearly synthetic policy.
G10 CLOSED. G1–G9/G11 integration acceptance BLOCKED;6.12 PARTIAL/T22 LOCAL PASS;
no6.13–6.18 formal implementation, API freeze, production activation or Stage7B changes.

## Verification actually executed

Latest source-bound run is resolved by `spikes/stage6-api-v1-local/results/LATEST.json`.
106 tests PASS (34 real-service/scripted-SQL,27 approved request-scene adapter calls,
24 routing/validation/security,21 wire/cursor/composition). All8 authored protocol
scenes executed against actual adapter/service code; original61 DTO scenes serve as
schema expectations/fixtures, not independent backend proof. Controlled RSA/JWKS
exchange uses existing actual verifier and ephemeral synthetic keys, zero network.
38 relevant original T22 regressions PASS separately.18 planning-verifier regressions,
original41 primary/29 comparison fixture/provenance/read-only Android hashes, agent
policy and whitespace PASS. Raw logs and before/after/current source hashes retained.
No current failures/skips/cancellations; initial successful development runs retained.
A reporting-tool counter edit briefly corrupted the pinned SHA and the protected-source
check failed closed; exact baseline restored, new source-bound run PASS. The retained
verifier-correction.json records this failure; no guard was waived.

PG preflight bind EPERM:0 database bodies; no PG runner/container launched. Harness
transaction serialization/savepoint emulation is not PostgreSQL locking/constraint/
durability proof. No HTTP/browser/Android/JVM/build/runtime/device/hosted test. Prior
345 executions/313 tests/205 PG-backed bodies unchanged, not rerun. No new evidence
is inferred for formal6.12/hosted threats, restore or budget/zero-cost gates.

## Next milestone and current cross-stage facts

Smallest next local implementation: bounded authoritative latest-submission Detail/
refetch projection for generic409 recovery; verify adapter/public receipts on permitted
disposable PostgreSQL before integration claims. Full-int8 domain writes need their
own verified numeric change. No G1–G4/search/stats/policy/public-version activation.
Stage7A FINAL ACCEPTED by owner; X10 unfixed/unverified but deferred to Gould0.12.
Stage8 readability/accessibility/touch-target gates remain; Stage7B acceptance blocked.
No cloud/production OAuth, Android networking, devices, merge/deploy/release.

created by stokonn with love <3
