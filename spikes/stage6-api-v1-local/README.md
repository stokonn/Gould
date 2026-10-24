## Current shared integration foundation — locally verified (2026-10-24)

Canonical package `contracts/admin/shared/v1` provides66 actual HTTP/PG fixture
scenes,22 unmodified selected Kotlin JVM replay probes and34 compatibility rows.
490 Node tests pass (469 preserved baseline+21 new bodies); all historical/schema
and independent source/cleanup checks pass. Public version choice, Android adoption,
real policy and hosted gates remain open; no production transport is enabled.

The measured queue query bounds headers before payload joins; no schema/index
change. UTF8 JSON media parameters are supported without legacy DTO aliases.
Current build/runner commands below remain valid; the current manifest is now
`integration-readiness-source-provenance.json`. The full-int8 manifest is preserved.

[Full integration handoff, matrix, version proposal and query evidence](../../docs/planning/goldberg-0.11/stage-6/INTEGRATION-READINESS.md).
Source publication/CI receipt follows verified local acceptance.

## Historical full-int8 local engineering acceptance (2026-10-24)

Shared moderation/submission handlers now preserve canonical decimal int8 strings
through validation, comparison, checked increment, persistence and responses.
Entity expectations allow0; submission expectations start at1. Increment beyond
`9223372036854775807` conflicts409 atomically; malformed wire values fail400.
Existing lock ordering, receipt replay and audit chains remain intact. Real
policies remain default-deny, public `/v1` compatibility unconfirmed.

Final source-bound local verification PASS (469 Node tests; historical18+17 and
current parser11 separately PASS). Implementation `023d290e5f30c26e85ffba5f20b1d16914a1e1e5`
is published with exact remote SHA and quality CI40098429341 SUCCESS; see the
[full-int8 acceptance ledger](../../docs/planning/goldberg-0.11/stage-6/API-FULL-INT8-ACCEPTANCE.md).
Build current provenance **before** executing the guarded runner:

```sh
python3 spikes/stage6-api-v1-local/build-current-provenance.py
python3 spikes/stage6-api-v1-local/run-http-postgres.py
python3 spikes/stage6-api-v1-local/verify-http-pg.py RUN_DIRECTORY
```

The current manifest is separate from immutable historical provenance. Frozen
validators run on original source snapshots; original positive/negative fixture
expectations and raw evidence stay unchanged. Older statements below describe
their historical milestones, including the old safe-Number write limitation.
No production, Android transport, cloud service, merge, release or deployment.

## Historical local HTTP → PostgreSQL checkpoint (2026-10-24)

Queue, audit and exact Prague-day statistics now have PostgreSQL-backed DTO
projections. The loopback HTTP listener exercises real transactions; native
services/schema and Android remain unchanged. `/v1` is provisional, policies
remain default-deny, title/composer are null and full-int8 writes remain unavailable.

Run `python3 spikes/stage6-api-v1-local/run-http-postgres.py` from repository root
on the authorized rootless host, then verify its printed run directory with
`python3 spikes/stage6-api-v1-local/verify-http-pg.py RUN_DIRECTORY`.
No image pull, external database URL, host data mount or production service is used.
The runner uses the exact inspected cached PostgreSQL17 image ID, ephemeral
localhost ports, synthetic fixtures and verified container/volume cleanup.

See [current implementation, G1–G11 and evidence ledger](../../docs/planning/goldberg-0.11/stage-6/API-HTTP-PG-INTEGRATION.md).
Older venue/absence statements below describe historical milestones, not this batch.

## Local M3 Comparison — verified

Strict in-process GET `/v1/admin/entities/{id}/comparison?submissionId={id}` now
projects approved live values; Detail retains recorded baseline/proposal semantics.
Real policies remain empty/default-deny. Run `python3 spikes/stage6-api-v1-local/run-m3-postgres.py`
from repository root for isolated disposable PostgreSQL acceptance. Verify the saved
run with `verify-m3.py RUN_DIRECTORY`. M2 evidence remains historical, byte-preserved;
its current-source checker is bound to its pre-M3 source.

[Scope, executed results, limitations and continuity](../../docs/planning/goldberg-0.11/stage-6/API-V1-LOCAL-M3.md).
No listener/Android/public compatibility/production activation or release acceptance.

---

# Current: local API M2 — verified Detail and PostgreSQL

GET `/v1/admin/submissions/{id}` now provides the approved bounded Detail. See
[scope, results and limits](../../docs/planning/goldberg-0.11/stage-6/API-V1-LOCAL-M2.md).
Run `python3 spikes/stage6-api-v1-local/run-m2-postgres.py` on an authorized Fedora
host with the existing cached pinned PG17 image/dependencies. This creates/removes
only its UUID-named loopback container, never accepts a database URL or touches
host services. Verify a saved run with `python3 spikes/stage6-api-v1-local/verify-m2.py RUN_DIRECTORY`.

M1 `verify.py` and source manifests remain historical and intentionally source-bound
to M1; use the M2 verifier for current code. Do not invoke the obsolete M1 publisher.
No live catalogue head is added to Detail; current Comparison and client reconfirmation
remain integration gaps. Histories>20 fail503 without truncation; production policy
default-deny. Public compatibility is unconfirmed; no HTTP/Android/cloud activation.

---

# Stage 6 API v1 local adapter — Milestone 1

**LOCAL / PROVISIONAL PUBLIC VERSION / NOT FROZEN / NO LISTENER.** Isolated branch
feature/goldberg-0.11-stage-6-api-v1-local from owner-approved6dd3136. This package
exports an in-process adapter only. No Android import/wiring, browser, HTTP server,
deployment entrypoint, cloud provider or production identity configuration exists.

Request representation: `{method,path,headers?,body?}`. POST body is raw UTF-8 JSON
string/Buffer and Content-Type must be exactly application/json. GET/DELETE have no
body. Response is `{status,headers,body:DTO|null}`; this is not an executed HTTP venue.
`/v1` paths are provisional local names only: absence of incompatible approved/deployed
consumers remains unconfirmed. No silent v2 selection. Unknown paths404; registered
wrong methods405/Allow. Existing CommunityApi unknown-operation403 remains unchanged.

## Executable operations

| Operation | Result |
|---|---|
| POST /v1/session/challenge | 201 challenge/nonce and actual stored expiry |
| POST /v1/session | 201 server principal/session, access/refresh and stored expiry values |
| POST /v1/session/refresh | 200 rotating pair, same absolute family expiry; consumed replay revokes family401 |
| GET /v1/session | 200 validated principal/session/expiry; expired/revoked401, suspended403 |
| GET /v1/me/capabilities | 200 principal/session/capabilities from one live session transaction |
| DELETE /v1/session | 204/null/no Content-Type; server family revocation, correlated audit/header ID |
| POST /v1/admin/submissions/{id}/open | 200 typed open receipt; explicit independent entity/proposal expectations |
| POST /v1/admin/submissions/{id}/decisions | approve201; reject/request_changes200; typed exact receipt; generic409, changed intent/key422 |

Required public schemaVersion1 is validated before explicit operation projection;
AuthLocalApi receives its actual operation-specific allowlists, never a guessed dialect.
Principal/session/capability binding uses existing server authentication/authorization,
not client claims. Tokens/challenge metadata are read from actual service storage;
familyId/private rows are not exposed. Logout calls the same SessionService directly
because AuthLocalApi deliberately suppresses its generated ID on204.

`createLocalAdapter` requires localOnly:true, explicit clock and controlled verifier.
Lifetimes:15m access,7d absolute family,5m challenge; original verifier caps provider
skew at60s. Factory default uses empty/default-deny Validator. Tests alone inject
clearly synthetic policy; no real source/field/license/attribution/evidence permission.
Real credential/JWKS URLs and production OAuth are not configured. No log callback
receives request bodies/tokens; public error messages are fixed safe text.

The domain/Auth/CommunityApi code is byte-unchanged. The per-request bridge supplies
canonical public operation/path/body identity and projects the result **inside** the
existing live-authorized moderation transaction, before receipt insertion/commit.
Proposal revision comes from the locked submission row, not an inferred entity value.
PublicReceipts reuses existing idempotency table/helpers and lock namespace; no DDL
change. Expiry lookup samples clock after lock wait; created_at samples after effect/
DTO completion so SQL expires_at=created_at+24h. Exact JSON values/status/requestId
replay; no second audit/feed/review. Receipt projection/storage failure rolls back.
No blanket claim about HTTP byte serialization or database durability from these tests.

## Verification and limits

106 executed adapter/wire tests: real service methods on state-bearing SCRIPTED SQL,
strict routing/parser/DTO and scalar/cursor units; includes27 original request scenes
and all8 protocol scenes plus controlled RSA/JWKS exchange. The SQL harness serializes
transactions globally and emulates rollback/savepoints: it does not prove PG row locks,
constraints, crash recovery or concurrency. 38 unchanged T22 service/scripted-storage
regressions PASS separately. 18 planning-verifier tests PASS separately. Original61
DTO/8 protocol files and prior345 execution evidence stay byte-preserved.

Local PostgreSQL preflight: socket bind EPERM, zero database bodies. No PostgreSQL,
actual HTTP/browser, Android/JVM/build/runtime, device or hosted execution is claimed.
Cached pg/jose dependencies are reused read-only (pg import is required by original
service helpers even without a database); pinned package locks remain unchanged.
Only scoped local node_modules caches are ignored; never commit them.

```bash
# If dependencies are missing on an authorized local host, install the existing locks:
test -f spikes/stage6-schema-pg/node_modules/pg/package.json || npm ci --prefix spikes/stage6-schema-pg --ignore-scripts
test -f spikes/stage6-auth-local/node_modules/jose/package.json || npm ci --prefix spikes/stage6-auth-local --ignore-scripts
python3 spikes/stage6-api-v1-local/run-local.py
python3 spikes/stage6-api-v1-local/verify.py
```

The runner records a fresh unique run directory, before/after source manifests,
exit codes, TAP and limits; it does not launch PostgreSQL or repeat the345 suites.
Historical initial/final logs remain, not overwritten. Publication uses publish.py
with an exact clean SHA and ordinary push; it never deploys or changes policies.

## Historical M1 remaining gaps (superseded by the current ledger)

- G5: post-mint metadata enrichment uses a second authenticated transaction; a lost
  response requires fresh authentication (no credential replay). Android credential
  clearing/account switching/single-flight refresh remain client integration work.
  Server refresh is deliberately not coalesced: used-token replay must revoke family.
- G6: latest Detail/refetch/history DTO route is not implemented.409 is generic and
  never silently retried; end-to-end refetch/reconfirmation remains incomplete.
  New public receipts lack actual PostgreSQL validation while loopback is blocked.
- G7: only the operations above are registered. No G1–G4 server functionality;
  real HTTP serialization/headers/proxy/CORS/browser venue and public version gate open.
- G8: int8 read utility is lossless through9223372036854775807. Existing moderation
  can write only safe JS integers. Valid unsupported values return503 after live auth;
  malformed/overflow representation400. A publication increment beyond safe integer
  stays the domain's409 with rollback. Never round/clamp or claim full-int8 writes.
  Scoped signed cursor codec is tested independently; no new queue/audit route uses it.
- G10 CLOSED; G1–G9/G11 integration acceptance BLOCKED;6.12 PARTIAL/T22 LOCAL PASS;
  no formal6.13–6.18 implementation or6.17 freeze. Production/network/actions inactive.

Next smallest local implementation: bounded latest-submission Detail DTO/refetch support
for generic409, with permitted disposable-PG verification of this adapter/receipt bridge.
Full-int8 domain writes remain a separately tested numeric task; no policy/public-version/
production decision is inferred. Stage7A FINAL ACCEPTED; X10 unfixed/unverified but
owner-deferred to Gould0.12, not a cosmetic0.11 blocker. Stage8 readability/accessibility/
touch-target verification remains required under separate scope.

created by stokonn with love <3
