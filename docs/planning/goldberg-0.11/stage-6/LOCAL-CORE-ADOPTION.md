## 2026-10-23 local-handler continuation

The owner explicitly authorized the local-only 6.4–6.6 prototype and selected
canonical server changes_requested. The exact narrow state patch below was reviewed
with its recorded dependencies and applied for these local handlers; no unseen-data
migration or old C6 change. This does not accept formal adoption/6.0f.
Current scope/results: [LOCAL-HANDLERS-PROTOTYPE.md](LOCAL-HANDLERS-PROTOTYPE.md).
The preparation record below remains the 2026-10-22 historical proposal.

# Stage 6 — prototype adoption preparation and compatibility impact

2026-10-22; base f54881e; branch spike/goldberg-0.11-stage-6-schema-pg.
**Preparation complete; formal adoption/6.1–6.3 implementation NOT STARTED.**
Owner reviewed the local milestone and selected a state direction. This record
prepares a concrete local implementation scope and an explicit policy decision;
it does not accept 6.0f, alter the approved API or execute cloud work.

- [x] Verify latest remote baseline and publish the exact approved Issue #31 row/link edit.
- [x] Inspect existing application, Stage 7A, prototype and original C6 dependencies read-only.
- [x] Record selected state direction and compatibility impact before any API edit.
- [x] Verify the existing 34-test source hashes; prepare its adoption map without recoding/retesting unchanged source.
- [x] Prepare exact API patch and narrow local-only authorization proposal for review.
- [ ] Owner reviews the API compatibility report and authorizes applying the prepared contract patch.
- [ ] Owner explicitly approves the local 6.1–6.3 gate exception and implementation scope below.
- [ ] After authorization only: adopt the candidate and implement the scoped local core with executed acceptance evidence.

## Selected direction and current contract standing

Owner-selected on 2026-10-22:

- draft remains device-local only;
- changes_requested is the canonical server state eligible for author revisions;
- returned is never a separate server state; it may be a display label only.

The prototype already enforces this direction. API-CONTRACTS.md §4 still lists
{draft, changes_requested, returned}; that approved text is deliberately unchanged
pending review of this report. [Prepared patch](adoption-proposals/API-REVISION-STATUS.patch)
changes only the revision eligibility and the older COMMUNITY-ADMIN role wording.
It is a review artifact, not an applied API change or permission to start 6.4.

## Dependency and compatibility findings

Inspection used repository text/reference searches plus direct reads; no hosted
queries, old benchmark or Stage 7 execution. Baselines: prototype f54881e,
original C6 c21e550 and Stage 7A 576f0d2. Source references below are branch-specific.

| Consumer / evidence | Verified observation | Compatibility impact |
|---|---|---|
| [API-CONTRACTS.md §4](API-CONTRACTS.md) | Revision endpoint allows draft/changes_requested/returned. | Removing two values narrows the written contract. A hypothetical caller relying on them would break; do not label the change additive or automatically backward compatible. |
| [DATA-MODEL.md §3](DATA-MODEL.md) | Draft is device-only; author revision is changes_requested → in_review. | Selected direction agrees; no state or data migration needed for the tested prototype. |
| [COMMUNITY-ADMIN.md role table](../COMMUNITY-ADMIN.md) | Contributor may revise own returned/draft item; lifecycle already uses changes_requested. | Clarify local draft editing versus server revision when the patch is approved. |
| [Prototype DDL](../../../../spikes/stage6-schema-pg/migrations/001_entities.sql) and [state guard/test](../../../../spikes/stage6-schema-pg/tests/schema.test.mjs) | Server domain excludes draft/returned; test rejects both and exercises changes_requested → immutable revision → in_review. | Existing tested schema needs no change. Hash-bound SQL/source and 34/34 evidence are reused. |
| [Original isolated C6 server](https://github.com/stokonn/Goldberg/blob/c21e550ceb5ec7a42317a4f2fe02e6d1225a2cab/spikes/contract-c6/src/server.mjs#L552) | Revision handler explicitly accepts all three values. Its normal submission/request-changes lifecycle uses submitted/changes_requested. | Do not copy this eligibility guard into the adopted core. Narrow a future adapter/fixture set after approval; preserve the old simulator and original evidence unchanged. |
| [C6 V4.3 test](https://github.com/stokonn/Goldberg/blob/c21e550ceb5ec7a42317a4f2fe02e6d1225a2cab/spikes/contract-c6/tests/v4-moderation.test.mjs#L75) | Happy path revises only after request_changes. Existing test source does not demonstrate live compatibility for draft/returned rows. | No need to rerun or rewrite completed tests now; future endpoint fixtures must explicitly reject server draft/returned and preserve the canonical path. |
| App at prototype base / [Stage 7A 576f0d2](https://github.com/stokonn/Goldberg/tree/576f0d2dee561bd93f22daa616fe2f866eeb3365) | No matching community revision route/status consumer found in app source or Stage 7 documents. The app's prose “source returned … bytes” is unrelated. | No in-repository shipped client edit or personal Room migration identified. This search is not proof about unknown external clients or unseen data. Stage 7A/X10 unchanged; 7B not started. |
| [Stage 7 handoff §4/§5](HANDOFF-STAGE7.md) | Contract freeze task 6.17 unstarted; future Admin clients must use shared fixtures; post-freeze breaking changes need /v2. | Seek explicit approval of this pre-freeze /v1 clarification. Do not invent a /v2 deployment or modify Stage 7. |

No real database inspection/import was performed. Any future import that actually
contains draft/returned server rows requires a separate migration/compatibility
decision; no automatic relabeling or deletion is authorized. UI text returned must
map to canonical changes_requested, not become a wire value, persisted status or
extra transition. Preserve local drafts and user-entered text.

## Reusable candidate and exact next local scope

Source implementation 16bfdfb; diagnostic correction f91a4ce; publication f54881e.
[34/34 executed evidence](../../../../spikes/stage6-schema-pg/results/SUMMARY.md),
[exact source hashes](../../../../spikes/stage6-schema-pg/results/source-sha256.json)
and original e37ed00/c21e550 failure/comparison evidence remain intact.

After the approval below, create a dedicated feature/goldberg-0.11-stage-6-local-core
branch from the reviewed prototype tip. No merge of Stage 5/7 branches. Proposed
module root backend/community/ is a local server-core source directory, not a
new architecture, running server, deployment package or cloud configuration.
Keep the original spike and evidence immutable as the adoption reference.

| Task | Exact implementation scope | Reuse and acceptance |
|---|---|---|
| 6.1 local schema adoption | Carry reviewed 001/002 migrations and tested migration/audit/numeric helpers into backend/community; retain loopback/synthetic test guard. Review schema-to-model map and trusted DB call boundary. Make any schema amendment a new migration, preserving checksums. | Byte/hash comparison before extraction, migration/upgrade/immutable/FK/audit/int8 tests after actual extraction or schema changes. Preserve original 34-test artifact; rerun only because adoption changes code locations/wiring, not as a benchmark. No hosted schema application, production RLS or real-data conversion. |
| 6.2 local Auth core | Token verification with algorithm allowlist, exact issuer/audience, signature/expiry/iat/skew checks, bounded injected JWKS cache/refetch and fail-closed unknown keys; session mint/rotate/revoke and atomic replay-family revocation in local PG. Inject fake clock, fake JWKS and test-only ephemeral keys; no live provider discovery/Google credential flow. | Review/reuse C2 7bc6976 verification scenarios and fixture techniques, not its hand-written crypto wrapper blindly. Compose audited primitives/libraries; no new crypto design. New local T1–T4 cases cover forged/none/confused alg, wrong issuer/audience, expiry, cold/rotated/unavailable keys, single-use challenge where applicable, concurrent refresh/replay and revocation. Managed Supabase Auth compatibility remains a later integration requirement. |
| 6.3 local authorization core | Resolve capabilities from trusted active role_grants and live session/principal state; default deny; ownership/existence hiding; actor≠author; suspension; immediate revocation; no self-grant. Bind storage calls to verified server context instead of caller-supplied actor/role fields. | Local T5–T9/T22-focused tests: roles, wrong actor, unknown/empty/error states, forged role input, cross-user access, revoked/suspended session and DB function boundary. Include local DB privilege-negative checks; do not claim hosted REST/RPC/RLS proof. |

6.4–6.18 are outside this immediate implementation authorization proposal. No
submission/admin HTTP handlers, Android/UI integration, real OAuth, hosted Auth
configuration, production RLS, retention/deletion periods, public collection,
Worker, Supabase writes, billing, merge, tag or release. Tests stay on disposable
local PostgreSQL 17 with synthetic identities and generated test keys only.
Existing Supabase/PostgreSQL + Workers/BFF architecture and 0 CZK/month unchanged.

## Exact policy approval still needed

The selected state direction and prototype review are NOT 6.0f acceptance or an
explicit amendment of its “before any implementation” rule. That rule remains in
[CHECKLIST.md 6.0f](CHECKLIST.md), [OPEN-DECISIONS.md §3 D8b](OPEN-DECISIONS.md),
[CLOUD-READINESS-PLAN.md §§2–4](CLOUD-READINESS-PLAN.md) and the introduction of
[POSTGRESQL-DESIGN-REQUIREMENTS.md](POSTGRESQL-DESIGN-REQUIREMENTS.md).

[Exact proposed owner authorization](adoption-proposals/LOCAL-CORE-AUTHORIZATION.md)
permits only local-only 6.1–6.3 ahead of 6.0f. It does not close, downgrade or
relabel 6.0f/C2 §12/C4/Q1; it creates an explicitly scoped exception to ordering
for these tasks. ORD-A's cloud sequence remains operative; its step 10 is amended
only for this local subset. All other implementation retains its existing gates.
Without that explicit approval, preserve the prototype/preparation boundary and
stop formal adoption/implementation. Another planning or security loop is not a
prerequisite: owner review of the concrete artifacts is the next action.

If approved, synchronize the scoped exception in those four gate references and
STAGE-CHECKLIST/docs/ai/Issue tracking, without checking 6.0f or formal task boxes.
Track actual local milestones with their executed evidence; global tasks remain
unchecked until the relevant acceptance and explicit completion decisions exist.
Cloud/integration/collection requirements cannot be satisfied by local results.

## Verification / publication

This continuation changes documentation/proposals only. Original prototype SQL,
modules, tests, package manifests and runner hashes must match source-sha256.json;
prior 34/34 is historical executed evidence, not rerun here. Five repository host
checks, Markdown/local links, untouched API/model/code/Stage 7 and original gate
checkbox checks are recorded in docs/ai/VERIFICATION.md. No hosted operation.
Dashboard exact approved body: 9,592 characters/52 lines, 64 comments preserved;
2026-10-22T20:58:16Z SHA-256
dd2829c6576e9648396505c9cd87e70e3ae8667e4f35a7ac571bb90e59e35f2a.

created by stokonn with love <3
