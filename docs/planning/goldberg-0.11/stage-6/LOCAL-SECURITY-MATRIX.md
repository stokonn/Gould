# Stage 6.12 — local threat-coverage matrix

**PARTIAL. Formal 6.12 remains unchecked.** Local verification only; no hosted,
OAuth, personal-data, emulator, phone, merge, tag or release operations.

Branch `verify/goldberg-0.11-stage-6-12-local`; owner-tested source
`faf9ba3ec24e7098289c80d6e119519ccc21b95e`. Backend `9d7150c` and Android `2f1524e`
are preserved ancestors. Stage 7B untouched; Android runtime acceptance deferred.

## Fresh host evidence and interpretation

- Owner Fedora wrapper `results/security-local/run-vQd6iGn5/`: 27 focused +32 unit
  PASS; `checksOrTestsFailed=false`, `postgresBlocked=false`, `sourceUnchanged=true`.
- Nested PG17.11 `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/`:
  Auth50 + handlers87 + schema34 + integration34 + unit32 =237 PASS.
  **296 successful executions; 264 tests counting the repeated32 unit suite once.**
  All seven TAP files have zero failures, skips, cancellations or hook failures;
  exact body and summary counts checked independently. Five checks PASS in both
  runners. Wrapper stdout exactly matches nested TAP/check log concatenation.
- All62 at-run source hashes matched before tooling changes. Version1 runner exit
  success is evidenced by owner report and wrapper control flow/result, rather than
  an invented numeric exit file. Cleanup log names the removed disposable container;
  live agent Podman inventory is denied by read-only runtime configuration.
- New version2 reporting tools:10 socket-free Python regressions PASS, retained in
  `results/security-local/runner-v2/unittest.log`. These are additional tool tests,
  separate from the296 host executions; full version2 PostgreSQL rerun NOT executed.
- Original host237 baseline, Android JVM214 and older failed runs remain preserved.
  All46 backend-source/15 original-artifact/15 Android-source hashes still match.
  Android evidence is prior JVM execution, not runtime or actual network proof.
- Local PASS covers only stated assertions. Hosted [I] portions remain OPEN.
  **T22 remains FAIL:** unknown audit-edit operation returns400, required403;
  no handler dispatch or privilege bypass demonstrated. Passing observation probes
  never waive that exact acceptance requirement. No API behavior was changed.

Machine-readable [coverage](results/security-local/coverage.json) and
[test inventory](results/security-local/test-inventory.json) provide exact test
file/name/log/venue for168 evidence references. Current local statuses: {"PASS": 13, "PARTIAL": 6, "NOT TESTED": 2, "FAIL": 1}.

| Threat | Local status | Hosted [I] remaining | Assertions / gaps |
|---|---|---|---|
| T1 — Expired session/access token | PASS | OPEN | Exact access expiry rejects; actual handlers return401, publish nothing and omit token from logs. New generic denial probe asserts request.denied event and correlation ID. **Missing:** No real HTTP/hosted session/logging check. Fresh owner SQL execution confirmed; no hosted expiry/logging execution. |
| T2 — Signature, kid, algorithms and JWKS cache/rotation | PASS | OPEN | Signature/none/HS confusion rejected401; fake provider bounded/cold outage yields safe503; concurrent refresh coalesces; header cap, zero-age, exact expiry and unknown-kid cooldown verified with synthetic clock. Attacker jku/x5u/jwk rejected before resolver invocation. **Missing:** Real JWKS rotation, transport/DNS and deployed cache window untested. Provider outage503 is deliberate fail-closed behavior, not a successful identity. |
| T3 — Credential/refresh replay | PASS | OPEN | Nonce mismatch/expiry/replay cannot mint second session; concurrent credential consumption has one winner. Refresh reuse commits family revocation/audit; even concurrent winner access is revoked. **Missing:** Fresh owner local SQL execution; no Google/hosted replay venue. |
| T4 — Wrong audience or issuer | PASS | OPEN | Exact issuer/audience, unexpected multi-audience and required claims rejected401 with real local JWT crypto; fresh SQL suite confirms no principal rows. **Missing:** Hosted provider audience/issuer integration OPEN. |
| T5 — Revoked moderator/suspended principal | PASS | Not required by original row; other gaps below | Still-live sessions lose privileged capabilities on next request; suspension403 and role revocation403; integration wrappers use live authorization including replay. **Missing:** Fresh owner SQL execution confirmed. No wall-clock production propagation claim. |
| T6 — Role injection/mass assignment | PASS | Not required by original row; other gaps below | Forged role/author/status rejected400 with exact snapshot equality; nested unknown fields rejected; role claims grant no authority; no self-approval. **Missing:** Fresh owner SQL execution confirmed; no wire-layer deserialization/HTTP claim. |
| T7 — Guess another contributor pending ID | PASS | Not required by original row; other gaps below | Other-owner and absent IDs both404 on private reads; own list empty; contributor queue denied. **Missing:** Evidence is local in-process/SQL; no HTTP listener tested. |
| T8 — Cross-user modification | PASS | Not required by original row; other gaps below | Other-owner revise/withdraw both404; no revision/status effect; ownership SQL query hides rows. **Missing:** No deployed PATCH route; local service operation is tested. |
| T9 — REST/RPC privilege escalation | PARTIAL | OPEN | Fresh restricted SQL roles cannot read private tables, mutate audit/head or invoke ungranted functions. SQL functions pin search_path=pg_catalog and revoke PUBLIC execution (source review). Local arbitrary RPC denied by Auth adapter. New composed admin route defers to injected authorization. **Missing:** No real REST/RPC listener, hosted Supabase RLS/anonymous JWT or deployed function identity. SQL mocks are not hosted tests. Current unknown CommunityApi operation taxonomy400 rather than Auth LocalApi403; see T22. |
| T10 — Concurrent moderator decisions | PASS | Not required by original row; other gaps below | Two moderators exactly201/409; one immutable publication/feed. Same absent/existing entity contention compares revisions atomically. Admin quarantine/rollback distinct-key races exactly200/409. **Missing:** Fresh owner PostgreSQL concurrency execution confirmed. No production load inference. |
| T11 — Identical idempotent retry | PASS | Not required by original row; other gaps below | Concurrent identical create/moderation replay stored response with one submission/publication; successful retries do not consume quota again. **Missing:** Fresh owner SQL execution confirmed; no network-loss wire replay tested. |
| T12 — Key reused with changed body | PASS | Not required by original row; other gaps below | Same key different payload/operation422; principals cannot reuse another stored result. Admin same-key changed reason422. **Missing:** No HTTP transport integration; fresh owner PostgreSQL evidence confirmed. |
| T13 — Script/URL/oversize/unknown/deep input | PARTIAL | Not required by original row; other gaps below | HTML/encoded HTML and unknown fields rejected400 with unchanged SQL snapshots. Node/string/byte/nesting/getter/cycle limits enforced before dispatch; no handler called in new probe. **Missing:** Actual Android/browser rendering/escaping unexecuted. Compose Text source review cannot substitute runtime rendering. No HTTP parser-size limit assertion. |
| T14 — Evidence SSRF/private IP/rebinding/redirect | PARTIAL | Not required by original row; other gaps below | Unsafe literal/private/metadata/local and non-reviewed URLs refused; empty policy denies even synthetic input. Evidence is stored only; no fetcher/proxy introduced. **Missing:** No DNS resolve-then-check, rebinding, redirect revalidation, real fetch size/time tests. URL syntax/allowlist validation DOES NOT pass full T14. No unrestricted fetching may be enabled. |
| T15 — Browser CSRF/CORS | NOT TESTED | OPEN | No current browser session/cookie/HTTP path exists. Older isolated C2 spike is not execution of this composed adapter. **Missing:** No current CSRF mismatch403, SameSite=Strict cookie, Origin/CORS or browser scenario execution. Both current local HTTP and hosted/browser paths OPEN. |
| T16 — Principal/distributed IP quota exhaustion | PARTIAL | OPEN | Daily/pending limits, principal last-slot and cross-account coarse-IP races; hashes/grouping; exact windows;429 Retry-After; kill503; storage failure503; rollback/retry counts; janitor preserves submissions/audit. **Missing:** Android/Admin actual runtime usability and deployed distributed-IP/edge quota exhaustion untested. Key/retention fixtures synthetic, not legal retention policy. |
| T17 — Partial transaction failure | PASS | Not required by original row; other gaps below | Approval fault boundaries locked/entity/snapshot/projection/approved/audit/feed/sql; idempotency insertion and deferred commit failure; exact snapshot equality then same-key success. Admin quarantine/rollback locked/projection/audit/feed/deferred; principal/quota faults atomic. **Missing:** Fresh owner PostgreSQL fault-injection run confirmed. No claim of provider failure injection. |
| T18 — Audit tamper/update/delete | PASS | OPEN | SQL immutable UPDATE/DELETE/TRUNCATE and restricted-role guards; exported copy modification/omission/order/head mismatch rejected by independent recomputation, including >9 sequences. No audit mutation handler dispatched. **Missing:** Operator console/provider path [I] OPEN. Chain proves tamper evidence relative to trusted head, not protection from privileged full-history replacement. T22 separately records mutation-route403 mismatch. |
| T19 — Export and isolated restore/RPO | NOT TESTED | OPEN | Fresh F4 verifies an exported JSON audit copy; it does NOT restore a database. **Missing:** No full fresh-empty-database restore, table/count/sample comparison, restored chain or RPO execution. Local and provider [I] OPEN; do not start 6.14 export/restore implementation here. |
| T20 — Unavailable/paused/quota backend graceful behavior | PARTIAL | OPEN | Backend safe503 and kill-switch read availability; JVM fake transport retains drafts, error taxonomy, explicit stale cache and disabled production transport without startup fetch. **Missing:** No Android cold startup/process death/UI or Admin runtime, no deployed pause/exhaustion. JVM file-store recreation is not Android runtime. Emulator recovery explicitly deferred. |
| T21 — Personal-data isolation | PARTIAL | Not required by original row; other gaps below | Server private fields rejected at root/target/field/evidence; fresh SQL no submissions; Kotlin explicit nested DTO allowlists and credential redaction executed in JVM fixtures; fresh additional server private-field probes. **Missing:** Actual network-layer assertion absent: production transport disabled. Zero personal rows leaving device cannot be claimed from DTO/fixture assertions. Android/runtime venue deferred; no real data needed. |
| T22 — Bootstrap self-grant/audit self-edit | FAIL | Not required by original row; other gaps below | Fresh SQL/live Auth tests reject self-grant403, moderator escalation403 and ungranted SQL mutation. Auth LocalApi unknown RPC403. New probe observes no audit-edit dispatch for anonymous/synthetic token, but composed CommunityApi returns400 validation_failed. **Missing:** EXACT ACCEPTANCE MISMATCH: threat model demands403; current composed missing audit-edit endpoint returns400, corroborating the fresh handler/admin tests. Denial/no-mutation holds; no privilege bypass demonstrated. Probe deliberately records observed400 and is not a passing403 assertion. Reconcile status semantics before accepting T22; contracts/production behavior left unchanged. Fresh anonymous role-route probe uses injected deny, not real unauthenticated session. |

## Exact executed evidence by threat

### T1 — PASS

- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T1 exact access expiry rejects; refresh can rotate before absolute family expiry**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/auth-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **malformed, expired and revoked sessions safely reject actual handlers with 401 and sanitized logs**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T1 denial envelope and structured log contain no supplied credential or exception text**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T1/T4 independent verifier rejects expired**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
### T2 — PASS

- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T2 invalid signature, malformed/no token, none and HMAC confusion fail closed**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T2 unknown kid refreshes once; rotation succeeds; attacker URLs are never fetched**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T2 bounded cache expiry and unavailable provider never fall back to stale keys**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T2 cold unavailable/hanging provider, ambiguous/private keys and malformed JWKS deny**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T2 concurrent cold verification coalesces one bounded JWKS request**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T22 admin cannot self-grant; operator capability/role stays outside application roles**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/auth-regression.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T2 untrusted jku header rejected before trusted resolver invocation**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T2 untrusted x5u header rejected before trusted resolver invocation**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T2 untrusted jwk header rejected before trusted resolver invocation**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T2 none HMAC and forged signatures deny without successful identity**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T2 header cache cap exact expiry fails closed then recovers with rotated keys**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T2 zero max age cannot reuse keys on the next verification**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T2 unknown kid flood bounded and later rotation eligible at exactly one second**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T2 parallel cold resolver outage coalesces and never returns identity**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T21 server rejects private journal at every selected object boundary**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T21 server rejects private practiceSessions at every selected object boundary**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T21 server rejects private personalNotes at every selected object boundary**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T21 server rejects private microphoneRecordings at every selected object boundary**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T21 server rejects private backup at every selected object boundary**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T21 server rejects private privateRepertoireHistory at every selected object boundary**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T21 server rejects private profile at every selected object boundary**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T22 probe records composed unknown audit mutation as 400 with no dispatch; required 403 remains a finding**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
### T3 — PASS

- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T3 nonce binding, expired challenge and credential replay cannot mint a second session**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T3 concurrent identical credential exchange consumes challenge exactly once**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T3 refresh rotation revokes old access; replay commits family revocation and audit**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T3 concurrent refresh has one winner and replay revokes even its newly issued access**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/auth-regression.tap`.
### T4 — PASS

- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T1/T4 strict claims reject wrong issuer with 401**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T1/T4 strict claims reject wrong audience with 401**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **strict audience is the exact configured value, not an unexpected multi-audience envelope**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/auth-regression.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T1/T4 independent verifier rejects wrong audience**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T1/T4 independent verifier rejects multi audience**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T1/T4 independent verifier rejects wrong issuer**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
### T5 — PASS

- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T5 valid token from suspended/deleted principal gets 403, expired/revoked token 401**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T5 role revocation takes effect on next request despite a still-live session**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T5 administrator suspension is immediate, audited and cannot resurrect deleted principal**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/auth-regression.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **role/suspension integration reuses live Auth, denies escalation, self grant and revoked replay**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **read role revocation is immediate; owner retains data but cannot bypass current capabilities**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
### T6 — PASS

- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T6/T13 validation rejects unknown top field without effects**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T6/T13 validation rejects nested role without effects**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T6 self-approval, forged role/author/status, revoked moderator and suspended identity deny**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **strict verified identity maps by issuer/subject; email/name/role claims grant no authority**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T6 strict allowlist rejects forged role/author/status in protected resource requests**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/auth-regression.tap`.
### T7 — PASS

- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T7/T8 owner-only private reads/revisions/withdrawals hide another contributor and absent IDs equally**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T7/T8 ownership in query hides other private submissions and denies cross-user writes**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/auth-regression.tap`.
### T8 — PASS

- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T7/T8 owner-only private reads/revisions/withdrawals hide another contributor and absent IDs equally**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T7/T8 ownership in query hides other private submissions and denies cross-user writes**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/auth-regression.tap`.
### T9 — PARTIAL

- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T9/T22 public database role has no private-table or privileged-function access**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T9/T22 local transport exposes no audit mutation or arbitrary RPC, even to admin**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/auth-regression.tap`.
- `spikes/stage6-schema-pg/tests/schema.test.mjs` — **restricted local role cannot mutate history/head or invoke ungranted definer functions**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/schema-regression.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T9/T22 configured admin role route defers to live authorization and never opens storage when denied**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
### T10 — PASS

- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T10 two moderators race on one submission: exactly [201,409] and coherent publication/feed**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T10 distinct submissions same absent entity: one winner, stale loser 409**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T10 distinct submissions same existing entity: one winner, stale loser 409**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin.quarantine replay, mismatch and concurrent distinct-key decisions**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin.rollback replay, mismatch and concurrent distinct-key decisions**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
### T11 — PASS

- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T11 same-key concurrent create and moderation replay exact stored response; different-key loser conflicts**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **daily limit atomic last slot, duplicate replay, mismatch and pending quota**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **kill switch truthful 503, committed replay and public/private reads stay available**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
### T12 — PASS

- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T12 same key different payload/operation gives 422; principals cannot see/reuse stored results**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin.quarantine replay, mismatch and concurrent distinct-key decisions**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin.rollback replay, mismatch and concurrent distinct-key decisions**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
### T13 — PARTIAL

- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T6/T13 validation rejects HTML without effects**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T6/T13 validation rejects encoded HTML without effects**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **boundary checks: 20 fields, 10 URLs, 2 KiB strings, 16 KiB exact request, nesting/cycle/getters**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T13 malformed object getters nesting and size fail before any handler dispatch**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
### T14 — PARTIAL

- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL javascript:alert(1)**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL data:text/plain,x**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL http://evidence.synthetic.invalid/source**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://localhost/source**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://127.0.0.1/source**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://127.1/source**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://0x7f000001/source**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://10.0.0.1/source**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://172.16.0.1/source**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://192.168.1.1/source**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://169.254.169.254/source**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://[::1]/source**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://[::ffff:7f00:1]/source**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://user:pass@evidence.synthetic.invalid/source**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://evidence.synthetic.invalid:444/source**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://evidence.synthetic.invalid./source**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://evidence.synthetic.invalid/source\#private**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://evidence.synthetic.invalid\\@localhost/source**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://evil.invalid/source**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T14 refuses unsafe/unreviewed URL https://evidence.synthetic.invalid/xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T14 empty policy refuses synthetic evidence and URL policy is not an SSRF fetch pass**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
- `spikes/stage6-public-admin-abuse/tests/unit.test.mjs` — **fail-closed evidence policy remains empty and no evidence fetch abstraction is added**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/unit.tap`.
### T15 — NOT TESTED

No current candidate test executed for this principal scenario.
### T16 — PARTIAL

- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **daily limit atomic last slot, duplicate replay, mismatch and pending quota**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **coarse IP last slot across different accounts, hashed storage and no raw diagnostics**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **quota rollback on principal counter fault**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **quota rollback on coarse_ip counter fault**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **quota boundary exactly resets the IP window without recounting prior idempotency replay**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **kill switch truthful 503, committed replay and public/private reads stay available**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **quota table unavailable is 503 without work loss; recovery permits same key**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **idempotency insert failure rolls quota, submission and audit back together**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **independent janitor prunes expired coarse hashes with intake disabled and preserves evidence**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/unit.test.mjs` — **keyed hashes hide peer, preserve group, separate groups and key rotations**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/unit.tap`.
- `spikes/stage6-public-admin-abuse/tests/unit.test.mjs` — **retention scheduler cleans during idle intake, suppresses overlap and drains on stop**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/unit.tap`.
- `spikes/stage6-public-admin-abuse/tests/unit.test.mjs` — **retention scheduler retries unavailable storage at later ticks and reports no private diagnostics**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/unit.tap`.
### T17 — PASS

- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T17 full rollback and same-key retry after locked fault**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T17 full rollback and same-key retry after entity fault**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T17 full rollback and same-key retry after snapshot fault**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T17 full rollback and same-key retry after projection fault**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T17 full rollback and same-key retry after approved fault**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T17 full rollback and same-key retry after audit fault**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T17 full rollback and same-key retry after feed fault**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T17 full rollback and same-key retry after sql fault**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T17 idempotency insert and deferred commit failures rollback all effects and allow same-key retry**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin.quarantine T17 atomic rollback at locked**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin.quarantine T17 atomic rollback at projection**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin.quarantine T17 atomic rollback at audit**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin.quarantine T17 atomic rollback at feed**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin.quarantine T17 atomic rollback at deferred**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin.rollback T17 atomic rollback at locked**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin.rollback T17 atomic rollback at projection**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin.rollback T17 atomic rollback at audit**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin.rollback T17 atomic rollback at feed**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin.rollback T17 atomic rollback at deferred**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **principal operation fault rolls role and audit/idempotency back together**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **quota rollback on principal counter fault**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **quota rollback on coarse_ip counter fault**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `spikes/stage6-schema-pg/tests/schema.test.mjs` — **atomic publication rollback after locked leaves all tables/head/key unchanged**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/schema-regression.tap`.
- `spikes/stage6-schema-pg/tests/schema.test.mjs` — **atomic publication rollback after publication leaves all tables/head/key unchanged**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/schema-regression.tap`.
- `spikes/stage6-schema-pg/tests/schema.test.mjs` — **atomic publication rollback after approved leaves all tables/head/key unchanged**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/schema-regression.tap`.
- `spikes/stage6-schema-pg/tests/schema.test.mjs` — **atomic publication rollback after audit leaves all tables/head/key unchanged**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/schema-regression.tap`.
- `spikes/stage6-schema-pg/tests/schema.test.mjs` — **atomic publication rollback after feed leaves all tables/head/key unchanged**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/schema-regression.tap`.
- `spikes/stage6-schema-pg/tests/schema.test.mjs` — **atomic publication rollback after idempotency leaves all tables/head/key unchanged**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/schema-regression.tap`.
### T18 — PASS

- `spikes/stage6-schema-pg/tests/schema.test.mjs` — **immutable revision, published revision, review, audit and feed reject UPDATE/DELETE/TRUNCATE**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/schema-regression.tap`.
- `spikes/stage6-schema-pg/tests/schema.test.mjs` — **F4 physical audit triggers serialize independent appenders, rollback head, export and detect tampering**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/schema-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T18 immutable publication/audit/review/feed; exported chain tamper detected; no audit mutation adapter**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **audit export sorts int8 numerically beyond nine and detects an omitted row/head tamper**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-public-admin-abuse/tests/unit.test.mjs` — **T18 independent audit verification rejects tamper, omission, sequence reordering and head mismatch**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/unit.tap`.
### T19 — NOT TESTED

- `spikes/stage6-schema-pg/tests/schema.test.mjs` — **F4 physical audit triggers serialize independent appenders, rollback head, export and detect tampering**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/schema-regression.tap`.
### T20 — PARTIAL

- `spikes/stage6-public-admin-abuse/tests/unit.test.mjs` — **T20 unavailable PostgreSQL connector fails public read closed with safe 503**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/unit.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **kill switch truthful 503, committed replay and public/private reads stay available**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **quota table unavailable is 503 without work loss; recovery permits same key**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `app/src/test/java/com/opus/piano/community/CommunityRepositoryTest.kt` — **everyServerErrorRetainsDraftAndMapsDistinctly**; prior owner Fedora JVM; no Android runtime; `docs/planning/goldberg-0.11/stage-6/results/android-local/host-confirmed-1277bed/TEST-com.opus.piano.community.CommunityRepositoryTest.xml`.
- `app/src/test/java/com/opus/piano/community/CommunityRepositoryTest.kt` — **disabledProductionBoundaryNeverSendsAndRetainsDraft**; prior owner Fedora JVM; no Android runtime; `docs/planning/goldberg-0.11/stage-6/results/android-local/host-confirmed-1277bed/TEST-com.opus.piano.community.CommunityRepositoryTest.xml`.
- `app/src/test/java/com/opus/piano/community/CommunityRepositoryTest.kt` — **repositoryConstructionAndLocalEditingDoNotUploadOrFetch**; prior owner Fedora JVM; no Android runtime; `docs/planning/goldberg-0.11/stage-6/results/android-local/host-confirmed-1277bed/TEST-com.opus.piano.community.CommunityRepositoryTest.xml`.
- `app/src/test/java/com/opus/piano/community/CommunityRepositoryTest.kt` — **cachedOverlayIsExplicitlyStaleSyntheticAndSurvivesRestart**; prior owner Fedora JVM; no Android runtime; `docs/planning/goldberg-0.11/stage-6/results/android-local/host-confirmed-1277bed/TEST-com.opus.piano.community.CommunityRepositoryTest.xml`.
### T21 — PARTIAL

- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T21 rejects personal journal at every object level**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T21 rejects personal notes at every object level**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T21 rejects personal microphone at every object level**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T21 rejects personal backup at every object level**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T21 rejects personal sessions at every object level**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T21 rejects personal repertoire at every object level**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-handlers-local/tests/handlers.test.mjs` — **T21 rejects personal profile at every object level**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/handlers-regression.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T21 server rejects private journal at every selected object boundary**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T21 server rejects private practiceSessions at every selected object boundary**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T21 server rejects private personalNotes at every selected object boundary**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T21 server rejects private microphoneRecordings at every selected object boundary**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T21 server rejects private backup at every selected object boundary**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T21 server rejects private privateRepertoireHistory at every selected object boundary**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T21 server rejects private profile at every selected object boundary**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
- `app/src/test/java/com/opus/piano/community/CommunityRepositoryTest.kt` — **dtoUsesExplicitNestedAllowlists**; prior owner Fedora JVM; no Android runtime; `docs/planning/goldberg-0.11/stage-6/results/android-local/host-confirmed-1277bed/TEST-com.opus.piano.community.CommunityRepositoryTest.xml`.
- `app/src/test/java/com/opus/piano/community/CommunityRepositoryTest.kt` — **privateObjectSmugglingFailsAtEveryPayloadLevel**; prior owner Fedora JVM; no Android runtime; `docs/planning/goldberg-0.11/stage-6/results/android-local/host-confirmed-1277bed/TEST-com.opus.piano.community.CommunityRepositoryTest.xml`.
- `app/src/test/java/com/opus/piano/community/CommunityRepositoryTest.kt` — **credentialsAreRedactedAndNotInFixtureCallsOrPersistedDrafts**; prior owner Fedora JVM; no Android runtime; `docs/planning/goldberg-0.11/stage-6/results/android-local/host-confirmed-1277bed/TEST-com.opus.piano.community.CommunityRepositoryTest.xml`.
### T22 — FAIL

- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T22 admin cannot self-grant; operator capability/role stays outside application roles**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **self-grant and self-suspension cannot bypass guards using uppercase UUID spellings**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/auth-regression.tap`.
- `spikes/stage6-auth-local/tests/auth.test.mjs` — **T9/T22 local transport exposes no audit mutation or arbitrary RPC, even to admin**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/auth-regression.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **role/suspension integration reuses live Auth, denies escalation, self grant and revoked replay**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `spikes/stage6-public-admin-abuse/tests/postgres.test.mjs` — **admin audit paginates numerically with hash head and has no mutation endpoint**; fresh owner Fedora PostgreSQL17.11; `spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/postgres.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T22 probe records composed unknown audit mutation as 400 with no dispatch; required 403 remains a finding**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.
- `spikes/stage6-security-local/tests/security.test.mjs` — **T9/T22 configured admin role route defers to live authorization and never opens storage when denied**; fresh owner Fedora security wrapper; `docs/planning/goldberg-0.11/stage-6/results/security-local/run-vQd6iGn5/focused.tap`.

## Remaining work / exact next action

1. Preserve the confirmed host PostgreSQL run and updated immutable evidence hashes.
   Full version2 runner execution remains untested; ten reporting regressions pass.
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
