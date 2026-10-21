# Stage 6 — D8b zero-cost BFF feasibility study

**Evaluation date: 2026-10-21. Research/documentation only — nothing here is
provisioned, deployed or configured.** This study executes Stage 6 task **6.0f**:
the pre-implementation proof required by owner decision **D8b** (Web Admin =
Backend-for-Frontend, approved conditionally on proving the permanent **0 CZK/month**
fit — [OPEN-DECISIONS.md](OPEN-DECISIONS.md) §3). Quota terms change; **re-verify
every limit below immediately before any future provisioning** (task 6.0d).
A documentation study cannot guarantee future provider pricing or availability.

## 1. Verdict

**CONDITIONAL PASS** — D8b's approved security architecture (same-origin HTTPS,
`HttpOnly`/`Secure`/`SameSite=Strict` cookies, server-side authorization, CSRF,
strict CSP, no privileged bearer tokens or service-role secrets in browser
JavaScript) **can be operated at a proven 0 CZK/month** using:

> **One Cloudflare Worker on the Workers Free plan** serving both the static Web
> Admin bundle (static-asset requests are free and unlimited) **and** the `/v1` BFF
> API on a **single origin**, with **Supabase Free** (`eu-central-1`) as the data
> and identity plane behind the Worker.

The pass is **conditional** on four named items (§8) — most importantly a new owner
decision accepting **Cloudflare Workers Free as the BFF hosting runtime** (a second
provider alongside Supabase). A **Supabase-only** same-origin arrangement is
**BLOCKED at zero cost** (§4, finding S1) and was not silently worked around.

## 2. Official sources consulted (all retrieved 2026-10-21)

| # | Source | Used for |
|---|---|---|
| 1 | https://supabase.com/pricing | Free plan quotas, custom-domain pricing, feature matrix |
| 2 | https://supabase.com/docs/guides/platform/free-project-pausing | 7-day inactivity pausing, restore window |
| 3 | https://supabase.com/docs/guides/functions/limits | Edge Function runtime/platform limits, HTML-serving restriction |
| 4 | https://supabase.com/docs/guides/functions/regional-invocation · https://supabase.com/regions | Edge Function placement; region data-residency statement |
| 5 | https://developers.cloudflare.com/workers/platform/pricing/ | Workers/Pages Functions free quotas, CPU limits, static-asset billing, KV, logs |
| 6 | https://developers.cloudflare.com/pages/platform/limits/ | Pages free build/file/domain/project limits, function billing model |
| 7 | https://supabase.com/docs/guides/platform/billing-faq · CLOUD.md (repo, 2026-10-18 evaluation) | billing/upgrade behavior, prior audit baseline |

## 3. Provider evaluation (verified limits)

### 3.1 Supabase Free (data + identity plane) — approved backend (D4a)

| Dimension | Free-tier fact (source) |
|---|---|
| Price | $0/month; Pro is a paid upgrade, never automatic (pricing page) |
| Database | 500 MB, shared CPU/500 MB RAM; **unlimited API requests** (fair-use) |
| Auth | 50,000 monthly active users; Google social auth included; basic MFA included |
| Egress | 5 GB uncached + 5 GB cached per month |
| Edge Functions (if used) | 500,000 invocations/month; 150 s wall-clock (Free); 2 s CPU/request; 256 MB RAM; 100 functions/project |
| Storage | 1 GB (not used by this design — no file uploads) |
| Inactivity | **Free projects paused after ~1 week of low database activity**; restore free, within 1 year; owner-resumable (pausing docs) — **accepted under D4b** |
| Billing method | **Not required for the Free plan**; paid plans are explicit opt-ins |
| Auto-escalation | None: exceeding Free limits restricts/blocks usage; upgrade only by explicit owner action |
| Region | Frankfurt `eu-central-1` available; **Postgres, Auth and Storage objects stay in the chosen region** (regions page). Edge Functions run **at the edge nearest the caller** by default but can be **region-pinned** (regional-invocation docs) — though for this topology the Worker, not an Edge Function, is the BFF |
| Custom domains | **Not included on Free** — $10/domain/month add-on requires Pro ($25/mo) (pricing matrix) → cannot be used at 0 CZK |

### 3.2 Cloudflare Workers Free (BFF + static hosting) — candidate, not yet owner-approved

| Dimension | Free-tier fact (source 5–6) |
|---|---|
| Price | **Workers Free is the default plan** ("By default, users have access to the Workers Free plan"); Workers Paid ($5/mo minimum) is a separate, explicit opt-in |
| Requests | **100,000/day** (inbound Worker requests; resets 00:00 UTC); **subrequests not billed** |
| **Static assets** | **Free and unlimited** — requests to static assets do not count against the Worker quota |
| CPU | **10 ms per invocation**; no wall-clock duration charge on Free |
| KV (optional) | 100k reads/day, **1k writes/day**, 1 GB stored — session state is therefore placed in Supabase, not KV (§5) |
| Logs | Workers Logs Free: 200k events/day, 3-day retention (pricing-change notice for Dec 2026 exists — re-verify before relying on it) |
| Hosting | `*.workers.dev` subdomain included; custom domains attachable on the Free plan (Pages Free allows 100 custom domains/project) |
| Builds | Pages Free: 500 builds/month, 1 concurrent, 20,000 files, 25 MiB/file (static-asset Workers use their own upload path) |
| Inactivity suspension | **None identified** in reviewed Workers/Pages free-tier docs (not an uptime guarantee) |
| Billing method | Not required for free plans; exceeding Free quotas **rejects requests** rather than charging |
| Auto-escalation | None — no path from Free to Paid without explicit account action |
| Region | **Edge location is global and not pinnable on Free** → data-at-rest stays in Supabase `eu-central-1`, but **transient request processing may occur outside the EU** — must be covered by the D5b/D5c pre-launch review (§7 risk R2) |
| HTTPS/Cookies | Full control of response headers over HTTPS on the Worker origin → `HttpOnly`/`Secure`/`SameSite=Strict`, CSP, origin allowlist all implementable |

### 3.3 Cloudflare Pages Free (static-only variant) — viable alternative, not preferred

Pages Free serves static sites + Pages Functions (billed as Workers, same 100k/day
free quota). Pages Functions run on the **same `*.pages.dev` origin** as the static
bundle, so same-origin cookies work. Not preferred because one Worker with static
assets is one fewer moving part (single deploy unit, single origin, no build-pipeline
dependency), and Pages' git-build pipeline adds maintenance surface for no gain.

### 3.4 Other candidates — evaluated, not pursued

| Candidate | Reason not pursued |
|---|---|
| **Supabase Edge Functions as the Web Admin host** | **BLOCKED (finding S1, §4)** — see below. |
| Vercel / Netlify / Deno Deploy free tiers | Function-duration/memory caps and cold-start models add risk without advantage over Workers; account/billing-card policies and hobby-use terms vary and were not verified to the same depth — **justified exclusion, not deep evaluation**. No feature of the approved design requires them. |
| Self-hosting (owner machine / free VM) | Requires an always-on host, TLS and a public endpoint — new cost/ops burden or a paid VPS; violates the hobby-scale 0 CZK operating model (CLOUD.md). |

## 4. Decisive findings

**S1 — A Supabase-only same-origin BFF is blocked at 0 CZK.** Supabase Edge
Functions **rewrite `GET` responses with `Content-Type: text/html` to `text/plain`
unless a custom domain is configured** (limits doc, "Other limits & restrictions"),
and custom domains are a **paid $10/month add-on requiring the $25/month Pro plan**
(pricing matrix). Serving the Web Admin's HTML shell from the Edge Function domain
without a custom domain is therefore impossible, and hosting the shell elsewhere
(e.g. Supabase Storage) puts frontend and API on **different origins**, which breaks
`SameSite=Strict` cookies. Working around this (sniffing tricks, `text/plain`
delivery, cross-site cookies) would **weaken the approved D8b architecture** and is
prohibited. Conclusion: Supabase Free alone cannot host the same-origin BFF+SPA; a
free static/function host with one origin is required.

**S2 — Workers Free satisfies every D8b security requirement on one origin.** A
single Worker serves static assets (free/unlimited) and `/v1` BFF routes (100k req/day)
from one `*.workers.dev` (or free custom) origin; full header control enables the
exact cookie/CSP posture; the service-role key lives only in Worker secrets
(encrypted at rest, never delivered to the browser); authorization is computed
server-side per request against the Supabase-backed session/role tables; revocation
is effective at the next request because the session table is consulted on every
privileged call (AUTH.md §3/§6 unchanged).

**S3 — The 10 ms CPU budget is plausible but not yet measured.** A BFF request is:
cookie parse (trivial) + CSRF check (trivial) + Supabase session/role lookup via
PostgREST (**network I/O — not CPU-time**) + optional Supabase JWT verification via
WebCrypto (Ed25519 verify, well under 1 ms in worker runtimes) + response. Design
target ≤3 ms CPU leaves >3× headroom under the 10 ms cap. **This remains an estimate;
the actual BFF code path must be measured on a Workers-compatible runtime as the
first implementation spike** (conditional item C2, §8).

**S4 — Quota exhaustion fails closed at zero cost on both providers.** Workers Free
rejects requests beyond 100k/day; Supabase Free restricts/blocks beyond its meters;
neither charges automatically. This is exactly the D4b graceful-denial model — the
economic failure mode of this topology is **service denial, never a bill**.

**S5 — Supabase pausing (D4b) interacts safely with the Worker.** When the Supabase
project pauses, BFF calls fail → the Worker returns `503` (`service_unavailable`,
API-CONTRACTS.md §2), the Web Admin shows a truthful unavailable state, no local
lockout; the owner resumes from the dashboard. The Worker/Pages host itself has no
identified inactivity pause.

## 5. Proposed deployment topology (design only — not provisioned)

```
Browser (admin only)
  │  HTTPS, same origin, session cookie: HttpOnly; Secure; SameSite=Strict + CSRF token
  ▼
Cloudflare Worker (Workers Free, e.g. <admin>.workers.dev — one origin)
  ├─ /*            static Web Admin bundle        (free, unlimited requests)
  ├─ /v1/*         BFF API: cookie→session→capability check (default-deny)→Supabase
  ├─ secrets:      SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY, SESSION_SIGNING_KEY
  └─ headers:      strict CSP (default-src 'self', no inline script), origin
                   allowlist, referrer-policy, frame-ancestors 'none'
  │  HTTPS (service-role key never leaves the Worker)
  ▼
Supabase Free project — Postgres/Auth in eu-central-1 (D4a/D5c)
  ├─ PostgREST / RPC: approval transaction, sessions, roles, audit (server-side only)
  └─ Auth: Google social sign-in (console-configured under 6.0e, separately gated)
```

- **Sessions live in Supabase** (not KV) — KV Free allows only 1,000 writes/day;
  Supabase "unlimited API requests" absorbs session writes; revocation and expiry
  are server-side rows consulted on every privileged request (AUTH.md §3).
- **Supabase Edge Functions remain available** (500k free invocations/month) for
  non-HTML server workloads (e.g. the export job, ZERO-COST-OPS.md §4) where the
  HTML restriction does not apply; they are **not** part of the Web Admin origin.
- The Android app (D8a) uses the **same Worker `/v1` boundary** — one API, identical
  server-enforced semantics (ARCHITECTURE.md §4).
- **No custom domain, no DNS change, no billing entry** anywhere in this topology.

## 6. Security assessment against D8b requirements

| Requirement (AUTH.md §5, D8b) | Status on this topology |
|---|---|
| Same-origin HTTPS requests | ✅ one Worker origin serves SPA + API |
| `HttpOnly`, `Secure`, `SameSite=Strict` cookies | ✅ full response-header control; **Strict is kept** — known UX quirk: a first top-level navigation from an external site arrives without the cookie and bounces to login; acceptable for an admin console, not a reason to weaken to `Lax` |
| CSRF protection | ✅ double-submit token on every state-changing request + origin check (T15) |
| Strict CSP | ✅ `default-src 'self'`, no inline script, `frame-ancestors 'none'` — the Worker sets headers on every response including static assets |
| Google auth via approved Supabase architecture | ✅ browser completes Google sign-in via Supabase Auth; the Worker exchanges/validates server-side; **no token ever exposed to page JavaScript** |
| Server-side role authorization | ✅ per-request capability resolution from Supabase role rows; default-deny (AUTH.md §6) |
| Immediate role revocation | ✅ session/role tables consulted per request; no token-expiry waiting period |
| Session expiry + secure logout | ✅ server-tracked sessions, rotating refresh, `DELETE /v1/session` revocation (AUTH.md §3) |
| Admin recovery | ✅ unchanged — D8c Supabase Console out-of-band re-grant (AUTH.md §7) |
| XSS / session theft | ✅ strict CSP + HttpOnly cookies + escaped rendering; static bundle has no bearer tokens to steal |
| No privileged credentials in browser JS | ✅ browser holds only a session cookie; service-role key exists solely in Worker secrets |
| No Supabase service-role secrets in client code | ✅ same |

**Limitations (honest):** Cloudflare as hosting operator can serve/modify the Worker
bundle (trust-by-operator, same class as any host); edge processing location is not
pinnable on Free (risk R2); free-tier limits are provider-changeable at any time.

## 7. Zero-cost capacity model — **estimates, not measurements**

Assumptions: one admin, page loads fan out ~5 API calls; a `/v1` request costs 1–3
Supabase PostgREST calls (session, capability, action); average response 10 KB.

| Dimension | Scenario 1 — pilot (1 admin + 3 contributors) | Scenario 2 — small community (~40 contributors) | Scenario 3 — moderate hobby (~300 contributors) |
|---|---|---|---|
| BFF requests/day | ~150 | ~1,000 | ~5,000 |
| vs Worker Free 100k/day | **0.15%** | **1%** | **5%** |
| BFF requests/month | ~4.5k | ~30k | ~150k |
| Supabase PostgREST calls/day | ~400 | ~3,000 | ~15,000 (fair-use "unlimited API"; egress is the real meter) |
| Auth MAU (limit 50k) | 4 | ~41 | ~301 |
| DB growth/month | <1 MB | ~5–10 MB | ~30–50 MB (500 MB cap; 350 MB stop ≈ years away) |
| Egress/month (Supabase 5 GB) | ~50 MB | ~0.9 GB | **~3.6 GB → hits the existing 2.5/3.5 GB warn/stop ladder** |
| Session/auth overhead | negligible | negligible | ~300 session ops/month |
| Headroom | enormous | large | egress is the binding constraint; mitigations: cache overlay reads in the Worker (free), shrink payloads, throttle refresh — **never upgrade** |

**Proposed conservative thresholds (added to the ZERO-COST-OPS ladder):**

| Signal | Warning (degrade) | Stop (intake off) |
|---|---|---|
| Worker requests/day (new) | 5,000 sustained (5% of free cap) | 10,000 (10%) or kill switch |
| Supabase egress/month | 2.5 GB (existing) | 3.5 GB (existing) |
| Supabase DB size | 250 MB (existing) | 350 MB (existing) |
| Edge Function invocations/month (if used for exports) | 250k (existing) | 350k (existing) |

Every stop path is **deny/degrade, never pay** (S4). No automatic upgrade exists on
either provider; **none is recommended.**

## 8. Risks and unresolved questions

| # | Risk / open question | Disposition |
|---|---|---|
| R1 | **Cloudflare becomes a second provider** in the approved arrangement (D4a named Supabase as the backend; the data plane stays Supabase, but the BFF host is new) | **Requires an explicit owner decision** (C1) — this study does not assume it |
| R2 | Worker edge location is not pinnable on Free → transient processing may occur outside the EU even though data at rest is `eu-central-1` | Must be covered by the **D5b/D5c pre-launch review**; region-pinning exists only on paid tiers — no paid mitigation may be assumed |
| R3 | 10 ms CPU/invocation is an **estimate** for the real BFF code | Measured in the C2 spike before implementation proceeds |
| R4 | Provider terms/quota changes (incl. the announced Dec 2026 Workers Logs pricing change) | Re-verify all limits at 6.0d provisioning time and quarterly thereafter (ZERO-COST-OPS.md §1) |
| R5 | Supabase pausing after quiet weeks (accepted D4b) | Handled: truthful 503 + owner resume (S5) |
| R6 | SameSite=Strict login bounce on first external navigation | Documented UX quirk; do not weaken to `Lax` without an owner decision |
| R7 | Workers Logs retention only 3 days on Free | Ops incidents beyond 3 days rely on the Supabase audit chain + owner ops log — acceptable for owner-only moderation |

**Conditions to close the conditional pass:**

- **C1 — Owner decision** accepting **Cloudflare Workers Free as the BFF
  hosting/runtime** (topology §5), or a directive to study an alternative.
  ✅ **APPROVED 2026-10-21** (architecture-decision level: single origin for static
  assets + `/v1`; Supabase Free remains the data/auth backend in `eu-central-1`; no
  paid plans, billing, trials or upgrades; **no provisioning, deployment or
  implementation authorized** — OPEN-DECISIONS.md §4a).
- **C2 — Measured CPU spike** on a Workers-compatible runtime with the real
  BFF code path (cookie→CSRF→PostgREST→JWT verify), proving ≤10 ms CPU/request,
  recorded as executed evidence. 🚧 **Executed 2026-10-21 on branch
  `spike/goldberg-0.11-c2-bff` (isolated local `workerd`, synthetic identities,
  simulated Supabase/JWKS; final docs commit `7bc6976`, spike code commit
  `d2ce77b`) → CONDITIONAL PASS:** security **19/19 passed**; performance P1–P7
  all executed; **kernel-measured average CPU 2.03 ms/invocation** (~4.9× under
  the 10 ms Workers Free ceiling; distribution flat under injected latency).
  Full report: `spikes/bff-c2/results/SUMMARY.md` on that branch.
  **C2 is not closed:** per-invocation production CPU remains **unverified**
  (local workerd ≠ production edge; percentile claims rest on conservative
  local upper bounds), and the **§12 production-runtime verification remains
  owner-gated**. Production business logic beyond the minimal pipeline must be
  re-measured, not assumed to carry over.
- **C3 — D5b/D5c review** explicitly covering R2 (edge processing location).
  ✅ **Documentary review COMPLETE 2026-10-21**
  ([C3-EDGE-PRIVACY-REVIEW.md](C3-EDGE-PRIVACY-REVIEW.md)): architecture
  **conditionally viable** for a privacy-compliant path at 0 CZK (both DPAs in
  place at 0 CZK; DPF + EU SCCs cover transfers; every configurable risk has a
  Free-tier mitigation). **Not a compliance certification:** Frankfurt hosting
  does not mean EU-only processing (Cloudflare metadata processed in US/EU
  core DCs; Supabase support/account/telemetry flows to Singapore/US under
  SCCs); EU-only edge metadata processing is Enterprise-only and **unavailable
  on Workers Free** — reported as a disclosed constraint, not silently
  absorbed. **Legal/operator questions remain open** (controller/operator
  identity incl. the minor-owner capacity question, retention details,
  privacy-notice content) and are carried to 6.0b/6.0d/6.16 and the pre-launch
  legal review — **C3 closes no legal or provisioning gate**.
- **C4 — Terms re-verification** for both providers immediately before 6.0d
  provisioning.

## 9. Recommendation

Adopt topology §5 (Cloudflare Workers Free BFF + Supabase Free data plane in
`eu-central-1`) as the **preferred arrangement**, subject to C1–C4 — **C1
approved by the owner 2026-10-21; C2 executed 2026-10-21 → CONDITIONAL PASS
(19/19 security, kernel CPU 2.03 ms/invocation; production-runtime §12
verification still owner-gated); C3 documentary review complete
(conditional viability, legal/operator items open); C4 open** (§8). It satisfies
every D8b security requirement on a single origin, has **no billing method, no
paid tier, no chargeable trial and no automatic escalation path on either
provider**, and its economic failure mode is graceful denial — matching D4a/D4b
exactly. The Supabase-only arrangement is **blocked at 0 CZK** (S1) and no approved
design element was weakened to make hosting easier. **This study cannot guarantee
future provider terms**; the standing re-verification rules apply.

**Verdict: CONDITIONAL PASS (task 6.0f evidence recorded; C1 approved
2026-10-21; C2 executed 2026-10-21 → CONDITIONAL PASS with the §12
production-runtime verification still open; C3 documentary review complete
2026-10-21 — conditional viability, no compliance claim;
gate remains open until the remaining C2 conditions and C4 are closed).**

created by stokonn with love <3
