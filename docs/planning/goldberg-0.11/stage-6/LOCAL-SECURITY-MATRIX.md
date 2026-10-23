# Stage 6.12 — local threat-coverage matrix

**PARTIAL. Formal 6.12 remains unchecked.** Owner authorized local verification only.
No cloud, OAuth, real data, emulator, phone, merge, tag or release operations.

Branch `verify/goldberg-0.11-stage-6-12-local`, base `bcc7796`. Published backend
`9d7150c5b3f20880fe4d587d9c3bfd65a0f612ce` and Android `2f1524e4083a98a64d5fa9790f3cdc002d1f8237`
read back from GitHub. Stage 7B untouched; Android acceptance deferred.

## Evidence and interpretation

- **Historical owner Fedora PostgreSQL17.11:** 237 PASS (34 integration + 32 unit +
  87 handler + 50 Auth + 34 schema), zero failures/skips/cancellations. Exact raw
  evidence under `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/`.
  All46 source and15 artifact hashes independently match. At-run source manifest
  was not captured; original provenance qualification remains unchanged.
- **Historical owner JVM:** 214 PASS including40 community tests; exact XML names
  indexed below. All15 Android source hashes match. No JVM/build/packaging rerun.
- **Fresh final run:** 27 focused +32 existing unit =59 PASS, zero failures/skips/
  cancellations; five repository checks PASS. `run-x1eRKaqL/` retains raw results
  and pre/post source hashes. Earlier27-test run is retained separately, not added
  to the59 total. PostgreSQL preflight EPERM; zero fresh database test bodies.
- **Local statuses:** PASS only for the stated local assertions, including explicit
  historical unchanged-source executions. It never implies full threat acceptance
  when [I] remains OPEN. PARTIAL means assertions missing; NOT TESTED means the
  principal scenario was not executed. FAIL can mean exact acceptance discrepancy
  even when a narrower diagnostic test succeeds.
- **T22 finding:** unknown audit-edit operation returns400, while the threat model
  requires403. No handler is dispatched; no privilege bypass demonstrated. Existing
  tests also assert400. No contract or API behavior silently changed.

Machine-readable [coverage](results/security-local/coverage.json) includes every
exact test file/name/log/venue, not just the representative names below.
[Test inventory](results/security-local/test-inventory.json) verifies successful TAP
records and JUnit bodies. Counts: {"PASS": 13, "PARTIAL": 6, "NOT TESTED": 2, "FAIL": 1}.

| Threat | Local status | Hosted [I] remaining | Assertions / gaps |
|---|---|---|---|
| T1 — Expired session/access token | PASS | OPEN | Exact access expiry rejects; actual handlers return401, publish nothing and omit token from logs. New generic denial probe asserts request.denied event and correlation ID. **Missing:** No real HTTP/hosted session/logging check. Fresh SQL rerun blocked; actual expiry assertion comes from prior unchanged PostgreSQL suite. |
| T2 — Signature, kid, algorithms and JWKS cache/rotation | PASS | OPEN | Signature/none/HS confusion rejected401; fake provider bounded/cold outage yields safe503; concurrent refresh coalesces; header cap, zero-age, exact expiry and unknown-kid cooldown verified with synthetic clock. Attacker jku/x5u/jwk rejected before resolver invocation. **Missing:** Real JWKS rotation, transport/DNS and deployed cache window untested. Provider outage503 is deliberate fail-closed behavior, not a successful identity. |
| T3 — Credential/refresh replay | PASS | OPEN | Nonce mismatch/expiry/replay cannot mint second session; concurrent credential consumption has one winner. Refresh reuse commits family revocation/audit; even concurrent winner access is revoked. **Missing:** Prior real local SQL execution only; no fresh SQL rerun or Google/hosted replay venue. |
| T4 — Wrong audience or issuer | PASS | OPEN | Exact issuer/audience, unexpected multi-audience and required claims rejected401 with real local JWT crypto; prior SQL suite confirms no principal rows. **Missing:** Hosted provider audience/issuer integration OPEN. |
| T5 — Revoked moderator/suspended principal | PASS | Not required by original row; other gaps below | Still-live sessions lose privileged capabilities on next request; suspension403 and role revocation403; integration wrappers use live authorization including replay. **Missing:** Fresh SQL rerun blocked. No wall-clock production propagation claim. |
| T6 — Role injection/mass assignment | PASS | Not required by original row; other gaps below | Forged role/author/status rejected400 with exact snapshot equality; nested unknown fields rejected; role claims grant no authority; no self-approval. **Missing:** Fresh SQL rerun blocked; no wire-layer deserialization/HTTP claim. |
| T7 — Guess another contributor pending ID | PASS | Not required by original row; other gaps below | Other-owner and absent IDs both404 on private reads; own list empty; contributor queue denied. **Missing:** Evidence is local in-process/SQL; no HTTP listener tested. |
| T8 — Cross-user modification | PASS | Not required by original row; other gaps below | Other-owner revise/withdraw both404; no revision/status effect; ownership SQL query hides rows. **Missing:** No deployed PATCH route; local service operation is tested. |
| T9 — REST/RPC privilege escalation | PARTIAL | OPEN | Prior restricted SQL roles cannot read private tables, mutate audit/head or invoke ungranted functions. SQL functions pin search_path=pg_catalog and revoke PUBLIC execution (source review). Local arbitrary RPC denied by Auth adapter. New composed admin route defers to injected authorization. **Missing:** No real REST/RPC listener, hosted Supabase RLS/anonymous JWT or deployed function identity. SQL mocks are not hosted tests. Current unknown CommunityApi operation taxonomy400 rather than Auth LocalApi403; see T22. |
| T10 — Concurrent moderator decisions | PASS | Not required by original row; other gaps below | Two moderators exactly201/409; one immutable publication/feed. Same absent/existing entity contention compares revisions atomically. Admin quarantine/rollback distinct-key races exactly200/409. **Missing:** Fresh concurrency rerun blocked; accepted evidence is unchanged prior PostgreSQL run. No production load inference. |
| T11 — Identical idempotent retry | PASS | Not required by original row; other gaps below | Concurrent identical create/moderation replay stored response with one submission/publication; successful retries do not consume quota again. **Missing:** Fresh SQL rerun blocked; no network-loss wire replay tested. |
| T12 — Key reused with changed body | PASS | Not required by original row; other gaps below | Same key different payload/operation422; principals cannot reuse another stored result. Admin same-key changed reason422. **Missing:** No HTTP transport integration; PostgreSQL evidence historical unchanged. |
| T13 — Script/URL/oversize/unknown/deep input | PARTIAL | Not required by original row; other gaps below | HTML/encoded HTML and unknown fields rejected400 with unchanged SQL snapshots. Node/string/byte/nesting/getter/cycle limits enforced before dispatch; no handler called in new probe. **Missing:** Actual Android/browser rendering/escaping unexecuted. Compose Text source review cannot substitute runtime rendering. No HTTP parser-size limit assertion. |
| T14 — Evidence SSRF/private IP/rebinding/redirect | PARTIAL | Not required by original row; other gaps below | Unsafe literal/private/metadata/local and non-reviewed URLs refused; empty policy denies even synthetic input. Evidence is stored only; no fetcher/proxy introduced. **Missing:** No DNS resolve-then-check, rebinding, redirect revalidation, real fetch size/time tests. URL syntax/allowlist validation DOES NOT pass full T14. No unrestricted fetching may be enabled. |
| T15 — Browser CSRF/CORS | NOT TESTED | OPEN | No current browser session/cookie/HTTP path exists. Older isolated C2 spike is not execution of this composed adapter. **Missing:** No current CSRF mismatch403, SameSite=Strict cookie, Origin/CORS or browser scenario execution. Both current local HTTP and hosted/browser paths OPEN. |
| T16 — Principal/distributed IP quota exhaustion | PARTIAL | OPEN | Daily/pending limits, principal last-slot and cross-account coarse-IP races; hashes/grouping; exact windows;429 Retry-After; kill503; storage failure503; rollback/retry counts; janitor preserves submissions/audit. **Missing:** Android/Admin actual runtime usability and deployed distributed-IP/edge quota exhaustion untested. Key/retention fixtures synthetic, not legal retention policy. |
| T17 — Partial transaction failure | PASS | Not required by original row; other gaps below | Approval fault boundaries locked/entity/snapshot/projection/approved/audit/feed/sql; idempotency insertion and deferred commit failure; exact snapshot equality then same-key success. Admin quarantine/rollback locked/projection/audit/feed/deferred; principal/quota faults atomic. **Missing:** Fresh PostgreSQL rerun blocked. No claim of provider failure injection. |
| T18 — Audit tamper/update/delete | PASS | OPEN | SQL immutable UPDATE/DELETE/TRUNCATE and restricted-role guards; exported copy modification/omission/order/head mismatch rejected by independent recomputation, including >9 sequences. No audit mutation handler dispatched. **Missing:** Operator console/provider path [I] OPEN. Chain proves tamper evidence relative to trusted head, not protection from privileged full-history replacement. T22 separately records mutation-route403 mismatch. |
| T19 — Export and isolated restore/RPO | NOT TESTED | OPEN | Prior F4 verifies an exported JSON audit copy; it does NOT restore a database. **Missing:** No full fresh-empty-database restore, table/count/sample comparison, restored chain or RPO execution. Local and provider [I] OPEN; do not start 6.14 export/restore implementation here. |
| T20 — Unavailable/paused/quota backend graceful behavior | PARTIAL | OPEN | Backend safe503 and kill-switch read availability; JVM fake transport retains drafts, error taxonomy, explicit stale cache and disabled production transport without startup fetch. **Missing:** No Android cold startup/process death/UI or Admin runtime, no deployed pause/exhaustion. JVM file-store recreation is not Android runtime. Emulator recovery explicitly deferred. |
| T21 — Personal-data isolation | PARTIAL | Not required by original row; other gaps below | Server private fields rejected at root/target/field/evidence; prior SQL no submissions; Kotlin explicit nested DTO allowlists and credential redaction executed in JVM fixtures; fresh additional server private-field probes. **Missing:** Actual network-layer assertion absent: production transport disabled. Zero personal rows leaving device cannot be claimed from DTO/fixture assertions. Android/runtime venue deferred; no real data needed. |
| T22 — Bootstrap self-grant/audit self-edit | FAIL | Not required by original row; other gaps below | Prior SQL/live Auth tests reject self-grant403, moderator escalation403 and ungranted SQL mutation. Auth LocalApi unknown RPC403. New probe observes no audit-edit dispatch for anonymous/synthetic token, but composed CommunityApi returns400 validation_failed. **Missing:** EXACT ACCEPTANCE MISMATCH: threat model demands403; current composed missing audit-edit endpoint returns400, corroborating prior handler/admin tests. Denial/no-mutation holds; no privilege bypass demonstrated. Probe deliberately records observed400 and is not a passing403 assertion. Reconcile status semantics before accepting T22; contracts/production behavior left unchanged. Fresh anonymous role-route probe uses injected deny, not real unauthenticated session. |

## Exact executed evidence by threat

### T1 — PASS

- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T1 exact access expiry rejects; refresh can rotate before absolute family expiry**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/auth-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **malformed, expired and revoked sessions safely reject actual handlers with 401 and sanitized logs**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T1 denial envelope and structured log contain no supplied credential or exception text**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T1/T4 independent verifier rejects expired**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
### T2 — PASS

- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T2 invalid signature, malformed/no token, none and HMAC confusion fail closed**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T2 unknown kid refreshes once; rotation succeeds; attacker URLs are never fetched**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T2 bounded cache expiry and unavailable provider never fall back to stale keys**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T2 cold unavailable/hanging provider, ambiguous/private keys and malformed JWKS deny**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T2 concurrent cold verification coalesces one bounded JWKS request**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T22 admin cannot self-grant; operator capability/role stays outside application roles**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/auth-regression.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T2 untrusted jku header rejected before trusted resolver invocation**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T2 untrusted x5u header rejected before trusted resolver invocation**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T2 untrusted jwk header rejected before trusted resolver invocation**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T2 none HMAC and forged signatures deny without successful identity**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T2 header cache cap exact expiry fails closed then recovers with rotated keys**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T2 zero max age cannot reuse keys on the next verification**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T2 unknown kid flood bounded and later rotation eligible at exactly one second**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T2 parallel cold resolver outage coalesces and never returns identity**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T21 server rejects private journal at every selected object boundary**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T21 server rejects private practiceSessions at every selected object boundary**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T21 server rejects private personalNotes at every selected object boundary**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T21 server rejects private microphoneRecordings at every selected object boundary**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T21 server rejects private backup at every selected object boundary**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T21 server rejects private privateRepertoireHistory at every selected object boundary**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T21 server rejects private profile at every selected object boundary**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T22 probe records composed unknown audit mutation as 400 with no dispatch; required 403 remains a finding**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
### T3 — PASS

- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T3 nonce binding, expired challenge and credential replay cannot mint a second session**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T3 concurrent identical credential exchange consumes challenge exactly once**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T3 refresh rotation revokes old access; replay commits family revocation and audit**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T3 concurrent refresh has one winner and replay revokes even its newly issued access**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/auth-regression.tap`.
### T4 — PASS

- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T1/T4 strict claims reject wrong issuer with 401**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T1/T4 strict claims reject wrong audience with 401**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **strict audience is the exact configured value, not an unexpected multi-audience envelope**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/auth-regression.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T1/T4 independent verifier rejects wrong audience**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T1/T4 independent verifier rejects multi audience**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T1/T4 independent verifier rejects wrong issuer**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
### T5 — PASS

- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T5 valid token from suspended/deleted principal gets 403, expired/revoked token 401**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T5 role revocation takes effect on next request despite a still-live session**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T5 administrator suspension is immediate, audited and cannot resurrect deleted principal**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/auth-regression.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **role/suspension integration reuses live Auth, denies escalation, self grant and revoked replay**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **read role revocation is immediate; owner retains data but cannot bypass current capabilities**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
### T6 — PASS

- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T6/T13 validation rejects unknown top field without effects**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T6/T13 validation rejects nested role without effects**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T6 self-approval, forged role/author/status, revoked moderator and suspended identity deny**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **strict verified identity maps by issuer/subject; email/name/role claims grant no authority**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T6 strict allowlist rejects forged role/author/status in protected resource requests**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/auth-regression.tap`.
### T7 — PASS

- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T7/T8 owner-only private reads/revisions/withdrawals hide another contributor and absent IDs equally**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T7/T8 ownership in query hides other private submissions and denies cross-user writes**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/auth-regression.tap`.
### T8 — PASS

- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T7/T8 owner-only private reads/revisions/withdrawals hide another contributor and absent IDs equally**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T7/T8 ownership in query hides other private submissions and denies cross-user writes**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/auth-regression.tap`.
### T9 — PARTIAL

- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T9/T22 public database role has no private-table or privileged-function access**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T9/T22 local transport exposes no audit mutation or arbitrary RPC, even to admin**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/auth-regression.tap`.
- `spikes/stage6-schema-pg/tests/schema.test.mjs` — **restricted local role cannot mutate history/head or invoke ungranted definer functions**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/schema-regression.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T9/T22 configured admin role route defers to live authorization and never opens storage when denied**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
### T10 — PASS

- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T10 two moderators race on one submission: exactly [201,409] and coherent publication/feed**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T10 distinct submissions same absent entity: one winner, stale loser 409**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T10 distinct submissions same existing entity: one winner, stale loser 409**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin.quarantine replay, mismatch and concurrent distinct-key decisions**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin.rollback replay, mismatch and concurrent distinct-key decisions**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
### T11 — PASS

- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T11 same-key concurrent create and moderation replay exact stored response; different-key loser conflicts**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **daily limit atomic last slot, duplicate replay, mismatch and pending quota**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **kill switch truthful 503, committed replay and public/private reads stay available**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
### T12 — PASS

- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T12 same key different payload/operation gives 422; principals cannot see/reuse stored results**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin.quarantine replay, mismatch and concurrent distinct-key decisions**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin.rollback replay, mismatch and concurrent distinct-key decisions**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
### T13 — PARTIAL

- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T6/T13 validation rejects HTML without effects**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T6/T13 validation rejects encoded HTML without effects**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **boundary checks: 20 fields, 10 URLs, 2 KiB strings, 16 KiB exact request, nesting/cycle/getters**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T13 malformed object getters nesting and size fail before any handler dispatch**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
### T14 — PARTIAL

- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL javascript:alert(1)**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL data:text/plain,x**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL http://evidence.synthetic.invalid/source**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://localhost/source**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://127.0.0.1/source**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://127.1/source**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://0x7f000001/source**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://10.0.0.1/source**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://172.16.0.1/source**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://192.168.1.1/source**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://169.254.169.254/source**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://[::1]/source**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://[::ffff:7f00:1]/source**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://user:pass@evidence.synthetic.invalid/source**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://evidence.synthetic.invalid:444/source**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://evidence.synthetic.invalid./source**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://evidence.synthetic.invalid/source\#private**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://evidence.synthetic.invalid\\@localhost/source**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://evil.invalid/source**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://evidence.synthetic.invalid/xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T14 empty policy refuses synthetic evidence and URL policy is not an SSRF fetch pass**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
- `spikes/stage6-public-admin-abuse/tests/unit.test.mjs` — **fail-closed evidence policy remains empty and no evidence fetch abstraction is added**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/unit.tap`.
### T15 — NOT TESTED

No current candidate test executed for this principal scenario.
### T16 — PARTIAL

- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **daily limit atomic last slot, duplicate replay, mismatch and pending quota**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **coarse IP last slot across different accounts, hashed storage and no raw diagnostics**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **quota rollback on principal counter fault**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **quota rollback on coarse_ip counter fault**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **quota boundary exactly resets the IP window without recounting prior idempotency replay**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **kill switch truthful 503, committed replay and public/private reads stay available**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **quota table unavailable is 503 without work loss; recovery permits same key**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **idempotency insert failure rolls quota, submission and audit back together**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **independent janitor prunes expired coarse hashes with intake disabled and preserves evidence**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/unit.test.mjs` — **keyed hashes hide peer, preserve group, separate groups and key rotations**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/unit.tap`.
- `spikes/stage6-public-admin-abuse/tests/unit.test.mjs` — **retention scheduler cleans during idle intake, suppresses overlap and drains on stop**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/unit.tap`.
- `spikes/stage6-public-admin-abuse/tests/unit.test.mjs` — **retention scheduler retries unavailable storage at later ticks and reports no private diagnostics**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/unit.tap`.
### T17 — PASS

- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T17 full rollback and same-key retry after locked fault**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T17 full rollback and same-key retry after entity fault**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T17 full rollback and same-key retry after snapshot fault**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T17 full rollback and same-key retry after projection fault**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T17 full rollback and same-key retry after approved fault**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T17 full rollback and same-key retry after audit fault**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T17 full rollback and same-key retry after feed fault**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T17 full rollback and same-key retry after sql fault**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T17 idempotency insert and deferred commit failures rollback all effects and allow same-key retry**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin.quarantine T17 atomic rollback at locked**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin.quarantine T17 atomic rollback at projection**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin.quarantine T17 atomic rollback at audit**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin.quarantine T17 atomic rollback at feed**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin.quarantine T17 atomic rollback at deferred**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin.rollback T17 atomic rollback at locked**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin.rollback T17 atomic rollback at projection**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin.rollback T17 atomic rollback at audit**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin.rollback T17 atomic rollback at feed**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin.rollback T17 atomic rollback at deferred**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **principal operation fault rolls role and audit/idempotency back together**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **quota rollback on principal counter fault**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **quota rollback on coarse_ip counter fault**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `spikes/stage6-schema-pg/tests/schema.test.mjs` — **atomic publication rollback after locked leaves all tables/head/key unchanged**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/schema-regression.tap`.
- `spikes/stage6-schema-pg/tests/schema.test.mjs` — **atomic publication rollback after publication leaves all tables/head/key unchanged**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/schema-regression.tap`.
- `spikes/stage6-schema-pg/tests/schema.test.mjs` — **atomic publication rollback after approved leaves all tables/head/key unchanged**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/schema-regression.tap`.
- `spikes/stage6-schema-pg/tests/schema.test.mjs` — **atomic publication rollback after audit leaves all tables/head/key unchanged**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/schema-regression.tap`.
- `spikes/stage6-schema-pg/tests/schema.test.mjs` — **atomic publication rollback after feed leaves all tables/head/key unchanged**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/schema-regression.tap`.
- `spikes/stage6-schema-pg/tests/schema.test.mjs` — **atomic publication rollback after idempotency leaves all tables/head/key unchanged**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/schema-regression.tap`.
### T18 — PASS

- `spikes/stage6-schema-pg/tests/schema.test.mjs` — **immutable revision, published revision, review, audit and feed reject UPDATE/DELETE/TRUNCATE**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/schema-regression.tap`.
- `spikes/stage6-schema-pg/tests/schema.test.mjs` — **F4 physical audit triggers serialize independent appenders, rollback head, export and detect tampering**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/schema-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T18 immutable publication/audit/review/feed; exported chain tamper detected; no audit mutation adapter**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **audit export sorts int8 numerically beyond nine and detects an omitted row/head tamper**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-public-admin-abuse/tests/unit.test.mjs` — **T18 independent audit verification rejects tamper, omission, sequence reordering and head mismatch**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/unit.tap`.
### T19 — NOT TESTED

- `spikes/stage6-schema-pg/tests/schema.test.mjs` — **F4 physical audit triggers serialize independent appenders, rollback head, export and detect tampering**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/schema-regression.tap`.
### T20 — PARTIAL

- `spikes/stage6-public-admin-abuse/tests/unit.test.mjs` — **T20 unavailable PostgreSQL connector fails public read closed with safe 503**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/unit.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **kill switch truthful 503, committed replay and public/private reads stay available**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **quota table unavailable is 503 without work loss; recovery permits same key**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `app/src/test/java/com/opus/piano/community/CommunityRepositoryTest.kt` — **everyServerErrorRetainsDraftAndMapsDistinctly**; prior owner Fedora JVM; no Android runtime; `docs/planning/goldberg-0.11/stage-6/results/android-local/host-confirmed-1277bed/TEST-com.opus.piano.community.CommunityRepositoryTest.xml`.
- `app/src/test/java/com/opus/piano/community/CommunityRepositoryTest.kt` — **disabledProductionBoundaryNeverSendsAndRetainsDraft**; prior owner Fedora JVM; no Android runtime; `docs/planning/goldberg-0.11/stage-6/results/android-local/host-confirmed-1277bed/TEST-com.opus.piano.community.CommunityRepositoryTest.xml`.
- `app/src/test/java/com/opus/piano/community/CommunityRepositoryTest.kt` — **repositoryConstructionAndLocalEditingDoNotUploadOrFetch**; prior owner Fedora JVM; no Android runtime; `docs/planning/goldberg-0.11/stage-6/results/android-local/host-confirmed-1277bed/TEST-com.opus.piano.community.CommunityRepositoryTest.xml`.
- `app/src/test/java/com/opus/piano/community/CommunityRepositoryTest.kt` — **cachedOverlayIsExplicitlyStaleSyntheticAndSurvivesRestart**; prior owner Fedora JVM; no Android runtime; `docs/planning/goldberg-0.11/stage-6/results/android-local/host-confirmed-1277bed/TEST-com.opus.piano.community.CommunityRepositoryTest.xml`.
### T21 — PARTIAL

- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T21 rejects personal journal at every object level**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T21 rejects personal notes at every object level**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T21 rejects personal microphone at every object level**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T21 rejects personal backup at every object level**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T21 rejects personal sessions at every object level**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T21 rejects personal repertoire at every object level**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T21 rejects personal profile at every object level**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/handlers-regression.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T21 server rejects private journal at every selected object boundary**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T21 server rejects private practiceSessions at every selected object boundary**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T21 server rejects private personalNotes at every selected object boundary**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T21 server rejects private microphoneRecordings at every selected object boundary**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T21 server rejects private backup at every selected object boundary**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T21 server rejects private privateRepertoireHistory at every selected object boundary**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T21 server rejects private profile at every selected object boundary**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
- `app/src/test/java/com/opus/piano/community/CommunityRepositoryTest.kt` — **dtoUsesExplicitNestedAllowlists**; prior owner Fedora JVM; no Android runtime; `docs/planning/goldberg-0.11/stage-6/results/android-local/host-confirmed-1277bed/TEST-com.opus.piano.community.CommunityRepositoryTest.xml`.
- `app/src/test/java/com/opus/piano/community/CommunityRepositoryTest.kt` — **privateObjectSmugglingFailsAtEveryPayloadLevel**; prior owner Fedora JVM; no Android runtime; `docs/planning/goldberg-0.11/stage-6/results/android-local/host-confirmed-1277bed/TEST-com.opus.piano.community.CommunityRepositoryTest.xml`.
- `app/src/test/java/com/opus/piano/community/CommunityRepositoryTest.kt` — **credentialsAreRedactedAndNotInFixtureCallsOrPersistedDrafts**; prior owner Fedora JVM; no Android runtime; `docs/planning/goldberg-0.11/stage-6/results/android-local/host-confirmed-1277bed/TEST-com.opus.piano.community.CommunityRepositoryTest.xml`.
### T22 — FAIL

- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T22 admin cannot self-grant; operator capability/role stays outside application roles**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **self-grant and self-suspension cannot bypass guards using uppercase UUID spellings**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T9/T22 local transport exposes no audit mutation or arbitrary RPC, even to admin**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/auth-regression.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **role/suspension integration reuses live Auth, denies escalation, self grant and revoked replay**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin audit paginates numerically with hash head and has no mutation endpoint**; prior owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/postgres.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T22 probe records composed unknown audit mutation as 400 with no dispatch; required 403 remains a finding**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T9/T22 configured admin role route defers to live authorization and never opens storage when denied**; fresh agent socket-free run; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-x1eRKaqL/focused.tap`.

## Remaining work / exact next action

1. On a permitted host, run the documented unchanged disposable PostgreSQL runner
   and retain the new source manifest/results; do not rerun inside denied sockets.
2. Resolve T22 exact status discrepancy before accepting it. This verification
   commit changes no service, API contract, SQL or Android behavior.
3. Keep T13 rendering, T14 real fetch protections, T15 browser/HTTP, T19 actual
   restore/RPO, T16/T20 actual client usability and T21 network assertions OPEN.
   Unsupported fetch and disabled production transport must stay disabled.
4. Hosted [I] requires separate authorization. Android runtime/emulator recovery
   remains deferred. Stage7B G1–G6 dependencies remain as reported in Issue31
   comment6362661598; no wire contract guessed or changed.
5. No6.13–6.18, provisioning, merge/tag/deploy/release starts automatically.

created by stokonn with love <3
