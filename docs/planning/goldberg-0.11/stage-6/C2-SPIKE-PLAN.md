# Stage 6 — C2 CPU/auth spike: test plan and execution specification

**Planning and test specification only. Nothing in this document is implemented,
installed, executed or provisioned. The C2 spike is UNEXECUTED (§15).**
Evaluation/plan date: **2026-10-21**, the day the owner approved the C2 approach.

This plan executes the owner's decisions recorded 2026-10-21:

- **C1 — APPROVED:** Cloudflare Workers Free hosts the Web Admin static assets and
  the `/v1` BFF on **one HTTPS origin**; Supabase Free remains the data and
  authentication backend in **Frankfurt `eu-central-1`**; no paid plans, billing,
  trials or upgrades; **no provisioning/deployment/implementation authorized**.
  C1 is resolved at the architecture-decision level (see
  [OPEN-DECISIONS.md](OPEN-DECISIONS.md) §4b).
- **C2 — APPROACH APPROVED, PLANNING ONLY:** an isolated, fully local CPU and
  authentication feasibility spike in a Cloudflare Workers-compatible runtime
  (`workerd` via Wrangler local mode) with synthetic identities and simulated
  Supabase/JWKS services. Writing the spike code, creating the harness, running
  tests, installing dependencies and creating fixture files all require a
  **separate, explicit owner authorization** (§14).

C2, C3 and C4 remain open conditions of the 6.0f gate
([BFF-FEASIBILITY.md](BFF-FEASIBILITY.md) §8); recording this plan does **not**
mark 6.0f satisfied.

## 1. Purpose and explicit scope

**Purpose.** Produce reproducible, honest evidence for the two open questions about
the approved C1 topology that cannot be settled from documentation alone:

1. **CPU budget (condition C2):** does a realistic BFF request pipeline — cookie
   parse, session validation, CSRF, cryptographic token verification, simulated
   Supabase session/capability lookup, default-deny authorization, response with
   security headers — fit the Workers Free **10 ms CPU per invocation** limit with
   engineering headroom?
2. **Authentication contract fidelity:** does the BFF pipeline as specified in
   [AUTH.md](AUTH.md) §2–3/§5–6 behave fail-closed against the full adversarial
   matrix (THREAT-MODEL.md T1–T5, T15, T20) when exercised in a Workers-compatible
   runtime?

**In scope (at future execution time, after §14 authorization):** spike-only
TypeScript Worker code; a local fake Supabase stub; synthetic key/JWT/cookie/CSRF
fixtures; scenario definitions; measurement collection and a results report.

**Explicitly out of scope, forever, for this spike:** the production community
backend, real Google OAuth, any Supabase or Cloudflare account/resource, any
deployment (`wrangler deploy` is never run), billing, Android production code,
Stage 7 Admin UI, Stage 7/8 work, real user data of any kind, and any change to the
approved D8b architecture (a change would require a new owner decision).

## 2. Architecture under test

The exact topology of [BFF-FEASIBILITY.md](BFF-FEASIBILITY.md) §5, with the data
plane **simulated**:

```
HTTP client (load generator / scenario runner — local)
  │  HTTPS in production; http://127.0.0.1 in the spike (local-only, never shipping)
  ▼
Worker (workerd, local Wrangler mode) — ONE origin
  ├─ /*           static asset stub (small fixture files; header assertions only)
  ├─ /v1/session  sign-in handover (AUTH.md §3), logout, capabilities
  ├─ /v1/*        BFF pipeline (§4 below), security headers on every response
  └─ config:      synthetic JWT secret/JWKS URI, issuer/audience, origin allowlist,
                  fake-clock seam, kill-switch flag        [NO real secrets ever]
  │  http fetch with configurable injected latency/failure
  ▼
Fake Supabase stub (local Node HTTP server on 127.0.0.1)
  ├─ /auth/v1/.well-known/jwks.json   synthetic JWKS (rotatable)
  ├─ /auth/v1/token(s)-equivalent     validates synthetic handover token, mints
  │                                   stub session rows (fake GoTrue behavior)
  └─ /rest/v1/rpc, /rest/v1/*         PostgREST-shaped session/principal/grant
                                      responses + latency/failure/quota injection
```

The Worker never contacts any real network host. The stub never contacts Supabase.
No Cloudflare account login, API token or account ID is configured anywhere; the
future `wrangler.toml` is local-dev-only and its execution instructions forbid
`deploy`.

## 3. Local runtime design

| Element | Choice (proposed, pinned at execution time) | Rationale |
|---|---|---|
| Runtime | `workerd` executed through **Wrangler local mode** (`wrangler dev --local`; Miniflare 3 underneath) | Same runtime family that executes production Workers — the point of the owner-approved approach |
| Language/toolchain | TypeScript, compiled with the bundler bundled with Wrangler | No native build toolchain; reproducible |
| Test harness | Worker tests via `@cloudflare/vitest-pool-workers` (candidate) or scenario runner driving `wrangler dev` over HTTP (candidate); final choice recorded at execution | Both are the documented local paths for testing Workers |
| Fake Supabase | Small Node (LTS) HTTP server; PostgREST/GoTrue/JWKS **shapes only** — enough for the BFF's calls, not a Supabase emulator | Full control of latency, failures, quota responses, JWKS rotation |
| Fake clock | **Test seam:** the future BFF obtains time through an injected `now()` (default `Date.now()`); tests swap a controllable clock | workerd has no built-in time virtualization; a seam is the only honest way to test exp/skew/cache windows |
| Latency/failure injection | Stub supports per-scenario: 0/5/25/100 ms added latency, `blackhole` (never respond → workerd fetch timeout), 5xx burst, `429` quota-exceeded, connection refused, slow-then-fail | Lets the study prove CPU and wall-clock are independent (§7) and rehearse T20 |
| Concurrency | Scenario parameter 1/5/20 parallel clients against the single local workerd | Rehearses warm-runtime contention (local semantics — see §11) |
| Isolation | Everything under a future `spikes/bff-c2/` directory; fixtures generated into a **gitignored** directory; zero coupling to Gradle/Android modules | The Android build must be completely unaffected |
| Network hygiene | Stub binds 127.0.0.1 only; scenario runner asserts zero external DNS/connect attempts (e.g. run with outbound blocked or a DNS trap) | "No external cloud connections" is a hard requirement |

**Prohibited in the environment by design:** real credentials of any kind, real
Google/Supabase endpoints, `CLOUDFLARE_API_TOKEN`, billing pages, production
domains, owner's personal data. Fixture generation script refuses to run if any
credential-shaped environment variable is present.

## 4. Authentication and authorization flow (contract under test)

The pipeline tested is exactly AUTH.md §2–3 and §5–6 — **no redesign**. Per-request
stages, in order:

1. **Session cookie parse** — `HttpOnly; Secure; SameSite=Strict` session cookie
   present or absent; malformed cookies rejected without exception paths.
2. **Session validation** — server-tracked session row (fake PostgREST lookup):
   exists, not revoked, not expired (fake clock), principal not suspended/deleted.
3. **CSRF verification** — double-submit token + exact `Origin`/`Host` match on
   every state-changing request (T15).
4. **Cryptographic verification of the synthetic token** — where the flow uses a
   Supabase-issued credential or API JWT (AUTH.md §2 Supabase variant): signature
   against the synthetic JWKS/`kid` (or HS256 project secret fixture), `iss` exact
   match, `aud` exact match, `exp`/`iat` with ≤60 s bounded skew, **algorithm
   allowlist** (reject `none` and confusion), unknown `kid` with exhausted cache ⇒
   **deny**.
5. **Simulated Supabase session and capability lookup** — sessions, principals and
   `role_grants` rows from the stub.
6. **Server-side authorization** — capability-based, resolved from trusted grant
   rows only, **default-deny** (AUTH.md §6); ownership and actor≠author enforced.
7. **Response construction and security headers** — strict CSP (`default-src 'self'`,
   no inline script, `frame-ancestors 'none'`), referrer-policy, origin allowlist;
   error envelope per [API-CONTRACTS.md](API-CONTRACTS.md) §2; `requestId` present.

**Supabase Auth is not replaced.** The browser's Google sign-in remains managed
Supabase Auth (out of spike scope). The spike's `POST /v1/session` handover consumes
a **synthetic Supabase-issued access token in the request body** (the credential
shape API-CONTRACTS.md §3 already specifies) and performs AUTH.md §2 verification
against the fake JWKS. The spike therefore proves the BFF's *consumption* of
Supabase-issued credentials — it does **not** implement, imitate or substitute a
custom Google authentication flow, and it must not drift into one. The exact
browser↔Supabase handover mechanics remain as AUTH.md §3 defines and are unchanged
by this plan.

## 5. Synthetic fixtures (to be generated at future execution — none exist now)

| Fixture | Content | Policy |
|---|---|---|
| Signing keys | Generated EC/RSA keypair set + one HS256 secret, with `kid`s; JWKS document served by the stub | Generated locally into a gitignored directory at execution time; **private keys never committed anywhere, not even synthetic ones**; public JWKS may be committed as an inert fixture |
| Identity tokens | Synthetic Supabase-shaped access tokens: valid; expired; future-`iat`; wrong `iss`; wrong `aud`; `alg=none`; algorithm-confused; unknown `kid`; revoked-key; over-skew | Purely synthetic subjects (`spike-admin-1` etc.); no emails, no real Google `sub`s |
| Session rows | Sessions: active, expired, revoked; principals: admin, moderator, contributor, suspended, deleted | Stub state, deterministic seeds |
| Grant rows | `role_grants` covering admin/mod/contributor/anonymous; one deliberately unknown role | Stub state |
| CSRF | Valid pairs; mismatched token; missing token; foreign `Origin` | Synthetic |
| Cookies | Valid, malformed, tampered-signature, cross-site `SameSite` simulation (forged `Origin`/`Referer`) | Synthetic |
| Bodies | Minimal valid submission-shaped payloads for admin endpoints; oversized/unknown-field variants | Synthetic catalogue-like strings only |

**Synthetic-only test data policy:** every identity, string, UUID and payload in
the spike is machine-generated or obviously fictional; no owner data, no real
catalogue entries copied from elsewhere, no personal data of any kind (DATA-MODEL.md
§7 boundary respected — nothing here is the practice journal, but the discipline is
the same).

## 6. Complete test matrix

### 6.1 Security scenarios (positive / negative / adversarial)

Threat rows from [THREAT-MODEL.md](THREAT-MODEL.md) §3. **No scenario counts as
passed until actually executed under §14 authorization and recorded with evidence.**

| # | Scenario | Threat | Fixture | Procedure | Expected result | Failure criteria | Evidence |
|---|---|---|---|---|---|---|---|
| S1 | Valid administrator session | (baseline) | Active admin cookie + CSRF + valid token | `GET /v1/admin/queue` and one state-changing admin call | 200; capability honored; security headers on response | Any 4xx/5xx, missing header, or unauthorized effect | Response capture incl. headers; CPU/wall sample |
| S2 | Authenticated non-administrator | T5-adjacent | Contributor session hitting an admin endpoint | `POST /v1/admin/decisions` as contributor | 403 `forbidden`, default-deny; state unchanged | 200 or any state mutation | Stub state diff + response capture |
| S3 | Missing credentials | T1 | No cookie/token | Privileged endpoint | 401 `unauthenticated`; no data | Any data leakage or non-401 | Response capture |
| S4 | Expired session | T1 | Revoked/expired session row (fake clock advanced) | Privileged request | 401 `unauthenticated`; event logged without token material | 200 or token material in logs | Response + log capture |
| S5 | Invalid token signature | T2 | Token signed with unknown key | Handover/verification path | 401; deny | 200 or fallback to unsigned trust | Response capture |
| S6 | Wrong issuer or audience | T4 | `iss`/`aud` mutated tokens | Verification path | 401; exact-match enforcement | Any acceptance | Response capture |
| S7 | Unknown signing key | T2 | Fresh `kid` absent from JWKS; cache exhausted | Verification path | 401 fail-closed, no fall-through | Any fall-through acceptance | Response + cache-state capture |
| S8 | JWKS rotation + cache expiry | T2 | Stub rotates keys mid-run; fake clock advances past cache header bound | Continue traffic across rotation | Old cached keys expire per `Cache-Control` bound; new keys picked up within the bounded window; never unbounded-stale trust | Unbounded stale acceptance, or hard denial beyond the documented bound | Timeline log with clock steps |
| S9 | Unsupported algorithm | T2 | `alg=none`, HS/RS confusion variants | Verification path | 401 for every variant | Any variant accepted | Response capture per variant |
| S10 | Revoked administrator role | T5 | Valid session; grant row removed (fake clock frozen — token still "valid") | Next privileged request | 403 at next request; **no waiting for token expiry** | 200, or any delay tied to token TTL | Before/after grant-state capture + response |
| S11 | Suspended account | T5 | Principal flipped to suspended | Privileged request | 403/limited session per policy; no privileged effect | Any privileged effect | Response + state capture |
| S12 | Invalid or missing CSRF token | T15 | Mismatched / absent CSRF, valid cookie | State-changing admin call | 403; state unchanged | 200 or mutation | Response + state diff |
| S13 | Cross-origin request rejection | T15 | Forged `Origin`/`Referer`; foreign-origin CORS preflight | State-changing call + OPTIONS | Non-matching origin rejected; foreign CORS refused (no `Access-Control-Allow-Origin` echo) | Echoed ACAO or accepted write | Header capture |
| S14 | Simulated Supabase timeout | T20 | Stub `blackhole` mode | Privileged request | 503 `service_unavailable`, truthful envelope; no partial effect | Fake success, hang beyond workerd timeout w/o response, partial mutation | Response + timing capture |
| S15 | Simulated backend unavailability | T20 | Stub connection refused / 5xx burst | Privileged request; also static route | 503 with `requestId`; static asset still served; app-facing behavior truthful | Unhandled exception leak, stack trace, or 200 | Response capture |
| S16 | Simulated backend quota exhaustion | T20 (+T16 rehearsal) | Stub returns 429-shaped quota responses | Privileged request | BFF maps to `503`/`429` per contract; kill-switch path denies intake; **no paid-escalation path exists in code** | Any escalation attempt, data deletion, or silent success | Response + kill-switch state |
| S17 | Replayed/rotated session or refresh | T3 (BFF layer) + AUTH.md §3 | Rotated refresh replayed; BFF session cookie replayed after logout rotation | Replay same credential twice | Second use ⇒ family/session revocation + audit event; no second session | Second acceptance | Both responses + audit capture |
| S18 | Logout then subsequent access | AUTH.md §3 | `DELETE /v1/session` then reuse cookie | Privileged request | 401; revocation immediate (fake clock frozen) | 200 | Response capture |
| S19 | No privileged secrets/tokens in responses | D8b invariant | All of the above responses | Diff every response body+headers against an allowlist (cookie names, CSP, no `service_role`, no Supabase JWT, no JWKS private material, no stack traces) | Zero privileged material in any response | Any occurrence | Allowlist-diff report |

### 6.2 Performance scenarios

| # | Scenario | Variables | Purpose |
|---|---|---|---|
| P1 | Warm happy path | 1,000+ requests, caches warm, stub latency 5 ms | Primary CPU compliance evidence |
| P2 | Cold start | 20 fresh workerd processes, first request each (cold JWKS + cold session cache) | Cold-start CPU/wall behavior |
| P3 | JWKS cold vs warm | Empty JWKS cache vs cached | Crypto overhead attribution |
| P4 | Latency sweep | Stub 0/5/25/100 ms | Prove CPU flat while wall grows (metric-validity check, §7) |
| P5 | Backend failure modes | blackhole/5xx/429 | Error-path CPU and truthful behavior |
| P6 | Concurrency | 1/5/20 parallel clients | Warm-runtime contention (local semantics) |
| P7 | Stability/leak run | 10,000 repeated requests | CPU drift, error rate, RSS stability |

## 7. CPU measurement methodology

**Distinguishing CPU from wall-clock is the core methodological requirement.**

- **CPU time per invocation** — read from **workerd's per-request accounting** in
  Wrangler's structured dev logs (`--log-format json`, `cpuTime` per request). This
  is the metric compared against the Workers Free 10 ms CPU limit. It excludes I/O
  wait by construction.
- **Sub-phase CPU attribution** — the future BFF exposes a test-only phase timer
  via the `now()` seam (§3): checkpoints around cookie parse, CSRF, JWT verify,
  session lookup, capability lookup, headers. Sum of phase CPU ≈ invocation CPU
  (sanity cross-check).
- **Wall-clock latency** — measured by the scenario runner around the full HTTP
  request (client-side). Wall = CPU + network wait + stub delay + queueing.
- **Network wait** — derived: wall − CPU, cross-checked against injected stub
  latency.
- **Validity check (mandatory before any PASS):** in the P4 latency sweep, **median
  CPU must stay statistically flat** (±10%) as injected latency moves 0→100 ms
  while wall grows correspondingly. If CPU tracks latency, the measurement pipeline
  is contaminated (e.g. blocking I/O counted as CPU) and results are **INCONCLUSIVE**
  until fixed.
- **Memory** — workerd process RSS sampled at start, after warm-up and end of P7;
  secondary target: no monotonic growth (>10% drift) across 10,000 requests.
- **Warm-up and sampling:** first 50 invocations of every run are discarded as
  warm-up; each performance scenario collects **≥1,000 measured invocations** and
  is repeated across **3 independent workerd process restarts**; P2 uses 20 fresh
  processes. Report per-run P50/P95/P99/max **and** the pooled distribution — never
  report only the best run.
- **Tooling and versions:** exact `workerd`, `wrangler`, Node LTS and harness
  versions recorded in every results file; dependencies pinned by lockfile at
  future execution (installation itself is §14-gated).
- **Reproducibility:** one command re-runs everything; results are regenerated, not
  edited; raw JSON committed alongside the summary (synthetic data only).

**Honesty rule:** local workerd results are strong evidence for the *runtime's* CPU
accounting but are **not definitive proof of Cloudflare production CPU usage**
(§11, §12). No performance number may be estimated, extrapolated or invented — a
metric that was not measured is reported as *not measured*.

## 8. Performance targets (provisional engineering targets — not measurements, not provider guarantees)

| Metric | Proposed target |
|---|---|
| Median CPU (P50) | ≤ 3 ms |
| P95 CPU | ≤ 5 ms |
| P99 CPU | ≤ 8 ms |
| Workers Free CPU ceiling (provider limit, per invocation) | 10 ms |

Supporting targets: cold-start wall < 1 s local; error rate 0 in P1/P7; P99 wall at
100 ms injected latency ≈ CPU + 100 ms ± 20%; RSS drift ≤ 10% over P7. The 10 ms
provider ceiling governs acceptance: a run with P99 CPU ≤ 8 ms but any invocation
over 10 ms is investigated, not averaged away. **Borderline measurements (P99 in
8–10 ms, or occasional >10 ms) require investigation or redesign — never a
fabricated PASS** (§10 outcomes).

## 9. Security acceptance criteria

Aligned with the 6.0f acceptance framing in [BFF-FEASIBILITY.md](BFF-FEASIBILITY.md):

- **Performance:** evidence demonstrates CPU compliance with the *applicable*
  Workers Free limit as re-verified at execution time (C4 discipline — quotas are
  re-checked before trusting any threshold); the §8 targets provide additional
  headroom.
- **Security:** S1–S19 — invalid or unauthorized requests **fail closed**; revoked
  roles take effect **at the next request without waiting for access-token expiry**
  (S10); backend failures (S14–S16) never expose privileged operations; **no
  session or authorization bypass** in any scenario; responses never carry
  privileged secrets (S19).
- **Reliability:** backend outages must not break local Goldberg practice
  functionality — within spike scope this means the Worker's static route keeps
  serving and error responses are explicit and truthful (API-CONTRACTS.md §2), and
  scenario retries show **no duplicate or unintended operations** (idempotency
  rehearsal); the full "practice never blocked" claim stays an Android-side
  acceptance (6.11), not claimed here.
- **Cost:** no paid service anywhere in the spike; no real deployment; free-tier
  assumptions remain subject to C4 re-verification before any future provisioning.

**Outcome definitions (applied to the future executed run):**

| Outcome | Definition |
|---|---|
| **PASS** | All §8 targets met with headroom (P99 ≤ 8 ms, no invocation >10 ms), P4 validity check clean, S1–S19 all behave fail-closed as specified, stability/leak targets met. |
| **CONDITIONAL PASS** | Compliance demonstrated but with a named caveat requiring owner-visible mitigation (e.g. P99 8–10 ms with documented optimization path; one non-security doc-level discrepancy). Caveats listed explicitly. |
| **FAIL** | Any invocation >10 ms unexplained after investigation; any fail-open security result; any secret exposure in a response; any architectural discovery that breaks D8b as approved. → redesign options reported to the owner; **no silent architecture change**. |
| **INCONCLUSIVE** | Measurement pipeline invalid (CPU tracks latency, harness error, version mismatch), insufficient samples, or environment drift. → fix the harness and re-run; never reported as pass. |

**Planning work — including this document — is not a PASS. C2 stays open until an
authorized execution produces evidence.**

## 10. Expected evidence and reporting format

At future execution, evidence is collected as:

- `spikes/bff-c2/results/raw/*.json` — per-run per-scenario samples (timestamps,
  workerd/wrangler/node versions, scenario config, per-request CPU/wall, outcomes);
  synthetic data only; committed for auditability.
- `spikes/bff-c2/results/SUMMARY.md` — one table row per scenario: scenario ID,
  executed yes/no, pass/fail/inconclusive, sample count, CPU P50/P95/P99/max, wall
  P50/P95, versions, run date, git commit of the spike code.
- Stage ledger update: `docs/ai/VERIFICATION.md` gains an "executed / not executed"
  entry per scenario; `CHECKLIST.md` progress log records the run; Issue #31 gets a
  checkpoint with the commit SHA.
- Reporting rules: counts are real; unexecuted scenarios are listed as *not
  executed*; failures are reported as failures; **no fabricated or rounded-to-pass
  numbers** (AGENTS.md quality gates).

## 11. Known limitations of local testing

1. `workerd` locally is the production runtime family, but the local environment
   does not reproduce Cloudflare's multi-tenant scheduling, CPU contention, edge
   placement or network path — local CPU numbers are **necessary but not
   sufficient** evidence (§12).
2. Concurrency semantics (P6) differ from production isolate scheduling; treat P6
   as a behavioral rehearsal, not a capacity forecast.
3. The fake Supabase stub proves the BFF's behavior against *Supabase-shaped*
   responses, not against a real project — payload sizes, timing distributions and
   Auth quirks can differ. Real-project behavior belongs to the [I] venue
   (THREAT-MODEL.md §2) and 6.0d/6.13, both separately gated.
4. The Google browser flow is Supabase-managed and out of spike scope — the spike
   proves credential *verification*, not end-to-end sign-in UX.
5. `Secure` cookies over plain-HTTP local endpoints require test-client tolerance
   (§3 note in AUTH.md: local dev is not a shipping configuration).
6. A documentation/plan review cannot guarantee future provider terms (standing
   rule, BFF-FEASIBILITY.md header).

## 12. Future production-runtime verification gate (owner-gated, separate)

If local results cannot **conclusively** demonstrate production CPU compliance
(e.g. P99 lands in the 8–10 ms band, or the owner wants production-grade proof
before 6.0d), a **separate, explicitly owner-authorized** verification step is
required, involving at minimum:

- Owner decision to create a Cloudflare account (or authorize an existing one) —
  itself a new decision (account holder, ToS acceptance, recovery email = D5a
  contact question surfaces again);
- A throwaway Workers Free worker deployed with the spike BFF (real deployment —
  never part of this task), measured with production telemetry (`wrangler tail`
  CPU accounting / Workers Logs), then **deleted**, with the deletion recorded;
- C4 re-verification of the then-current free-tier terms before anything runs.

**This gate is not authorized, not scheduled and not assumed. It is documented here
so local results are never overstated.**

## 13. Stop conditions and rollback

**Stop immediately and report to the owner if:**

- Any invocation exceeds the applicable CPU limit unexplained after one
  investigation pass, or P99 sits in 8–10 ms without a clear optimization path;
- Any S1–S19 scenario fails open, exposes secrets, or reveals that a required
  behavior (e.g. immediate revocation) is not achievable as designed;
- The P4 validity check shows contaminated CPU accounting;
- Completing the spike would require touching anything outside `spikes/bff-c2/`,
  any real credential, any network host beyond 127.0.0.1, or any architecture
  change;
- Any doubt about scope (rule: **if in doubt, stop and ask**).

**Rollback:** the spike is fully contained — delete `spikes/bff-c2/`, remove its
`.gitignore` entries, revert its single planning-authorized commit. No cloud
resources exist to decommission, no Android code is touched, no dependencies are
added to the Gradle build. Rollback must leave `git status` clean and the Android
build byte-identical.

## 14. Prerequisites for a separately authorized execution

All of the following must be true before anyone writes a line of spike code:

1. **Explicit owner authorization** naming this plan
   (`stage-6/C2-SPIKE-PLAN.md`) as the approved execution scope.
2. **Dependency installation approval** — proposed pinned toolchain: Node LTS,
   `wrangler` (brings `workerd`), TypeScript tooling as bundled, one test harness
   (`@cloudflare/vitest-pool-workers` or the HTTP scenario-runner alternative),
   an HTTP load generator. Installed **only** inside `spikes/bff-c2/` with its own
   lockfile; nothing added to the Android Gradle build. *(Installing dependencies
   is currently NOT authorized.)*
3. Synthetic-only data policy (§5) accepted as binding; no fixture files exist
   before execution and private keys are never committed.
4. C4 discipline: the then-applicable Workers Free CPU limit re-checked against
   Cloudflare's official docs at execution time and recorded in the results.
5. Execution environment with outbound network restricted to 127.0.0.1 during runs.
6. One commit boundary: spike code + results in `spikes/bff-c2/` only; no other
   paths modified.

**Estimated complexity:** medium — ~600–900 LOC of future spike code plus fixtures
and scenario definitions; roughly one to two focused sessions. **Stopping
conditions and rollback as in §13.**

**Items needing an additional owner decision if they ever arise:** executing the
spike at all (this authorization); any deviation from this plan's scope; any
architecture change suggested by results; the §12 production verification; using
spike code as the seed of the production BFF (a separate Stage 6 implementation
decision — the spike is evidence, not a prototype to be promoted silently).

## 15. Status statement

**C2 remains UNEXECUTED.** This document is a test specification produced under the
owner's 2026-10-21 planning-only approval. No spike code, harness, fixtures,
dependencies, benchmarks or security tests exist or were run. No performance or
security result is claimed anywhere in this document — the §8 numbers are
**provisional engineering targets**, and every scenario in §6 is **not executed**.
Conditions C2, C3 and C4 remain open; C1 is approved at the architecture-decision
level only. Task 6.0f stays unchecked. Stage 6 implementation remains NOT STARTED.

created by stokonn with love <3
