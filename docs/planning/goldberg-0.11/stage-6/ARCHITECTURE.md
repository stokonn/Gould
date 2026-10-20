# Stage 6 — Architecture and backend comparison

**Design only.** This document updates and extends the canonical
[CLOUD.md](../CLOUD.md) comparison (evaluated 2026-10-18) with a Stage-6-focused
re-evaluation for a *moderation-only* workload. Provider facts below were re-checked
against provider documentation during planning (2026-10-21) but **quota terms change;
re-verify immediately before any future provisioning**. No account, project, database
or function exists. The backend choice stays with the owner (decision D4,
[OPEN-DECISIONS.md](OPEN-DECISIONS.md)).

## 1. Fixed design constraints (from approved baseline)

- **0 CZK/month forever.** No trial, no paid tier, no automatic upgrade. Free-tier
  suspension and quota exhaustion are *expected operational states*, handled by
  graceful denial (CLOUD.md operating contract §5–6).
- **Moderation-only scope.** Small approved catalogue overlay, authenticated
  contribution and review operations. No journal sync, no media hosting, no bundle
  serving, no provider proxy, no Realtime.
- **Both Admin clients (Stage 7) hit the same versioned API** with identical
  server-enforced semantics (`API-CONTRACTS.md`).
- Fail-closed everywhere: exhausted quotas, missing config or unknown states deny
  authorization, never fall open.

## 2. Candidates — re-evaluation for the Stage 6 workload

The Stage 6 workload is tiny by design (design caps in CLOUD.md §"Proposed limits":
16 KiB payloads, 20 rows/page, 10 pending/principal, 5 submissions/day/principal,
~hundreds of writes/day). Both candidates fit capacity-wise; the differentiator is
**how much security-critical machinery Goldberg must write and maintain itself**.

| Criterion | Supabase Free (recommended proposal) | Cloudflare Workers Free + D1 (alternative) |
|---|---|---|
| Auth complexity | Managed Google social auth + managed sessions; app verifies its own API JWTs server-side; audited provider flows. | Fully custom: one-time challenge, Google ID-token verification via WebCrypto + cached JWKS (kid/alg/issuer/audience/expiry/nonce), own session minting/revocation. No custom primitives, but real security-review and CPU-budget burden on a 10 ms/invocation limit. |
| Authorization / row security | PostgreSQL Row Level Security with default-deny policies + `WITH CHECK`; service-role key bypasses RLS and must never reach any client. Misconfigured RLS/service key is the top risk class. | No managed RLS: every row/role check lives in Worker code over prepared SQL. No DB binding or REST token reaches clients. Risk concentrates in hand-written authorization code paths. |
| Database transactions | Native PostgreSQL transactions; approval = single transaction (authz → revision compare → validate → publish → status → audit → feed). Well-understood conflict semantics. | D1 supports interactive transactions within a single Worker invocation, but batch writes are more constrained; concurrency, uniqueness races and 409-conflict semantics need explicit designs and tests. Indexes consume the write quota. |
| Free-tier fit (this workload) | 500 MB DB, 50k Auth MAU, 500k edge-function invocations/month, 5 GB egress — far above moderation-only load. **Free projects may pause after ~7 days inactivity** (D4b acceptance required); resuming is owner-actionable but adds operational duty. | 100k req/day, 10 ms CPU, D1 500 MB/10 DBs, 5 M reads + 100k writes/day — also far above load. No inactivity pause identified in reviewed docs, but daily exhaustion fails closed and is not an uptime guarantee. |
| Moderation reliability | Constraints + trigger-free transactional RPC suit revisioned decisions; provider logs ≠ application audit (append-only `review_events` still required). | Atomic batches can commit decision+audit, but concurrent-moderator conflicts and idempotency uniqueness need explicit SQLite constraint designs proven by tests. |
| Abuse prevention | Per-principal quotas in DB + provider Auth rate limits; RLS prevents bypass through direct REST if intake limits are proven. | Per-principal quotas in D1; some abusive traffic consumes the request budget *before* app logic runs — exhaustion is denial, never a reason to pay. |
| Export / recovery | Own off-site encrypted logical exports required (no free PITR); restore drill mandatory. | D1 Time Travel (~7 days) helps but does not replace independent logical exports + restore drill. |
| Maintenance burden | Vendor SDK/policy churn, RLS policy review on every schema change, paused-project watch. | Larger always-on custom surface: auth/session/JWKS/rate-limit code, runtime differences, measured CPU headroom. |
| Long-term single-maintainer risk | **Lower** — less custom security code to keep correct. | Higher — security machinery owned by Goldberg forever. |

## 3. Recommendation (proposal, NOT an approved decision)

Retain the CLOUD.md recommendation: a **Supabase Free moderation-only pilot**,
contingent on the owner accepting inactivity pausing/restrictions (D4b) and settling
D5 privacy/operator decisions. Rationale for a moderation workload specifically:
managed auth removes the largest block of custom security code; PostgreSQL
transactions map 1:1 onto the approval contract in `API-CONTRACTS.md` §6; RLS gives a
second enforcement layer behind the API.

Workers+D1 remains the alternative, provable only in a **separately authorized
security/performance spike** (custom-auth CPU measurement, JWKS rotation test,
transaction/conflict proof). It is not an automatic fallback and is not selected by
provisioning failure. **Do not build both backends to postpone D4** (CLOUD.md
standing rule).

If inactivity pausing is unacceptable to the owner *and* the spike is declined, the
honest outcome is **no community backend** (contributions stay local-only) — not a
paid tier.

## 4. Logical component layout (backend-agnostic)

```
[Android app / Web Admin]  --HTTPS /v1-->  [API boundary]
                                              |-- auth layer: Google-identity verification + session check (AUTH.md)
                                              |-- authorization: role + suspension + capability lookup (default-deny)
                                              |-- handlers: submissions, revisions, queue, decisions, catalogue overlay read
                                              |-- validation: bounded payloads, URL/SSRF allowlist, no mass-assignment
                                              |-- quota layer: per-principal + coarse IP accounting, kill switch
                                              '-- database: conceptual schema (DATA-MODEL.md)
[Export job]  --scheduled-->  [encrypted off-site export, owner-controlled storage]  (ZERO-COST-OPS.md)
```

- Exactly **one** API boundary. Neither client ever holds a database credential; the
  Supabase service-role key (if that variant is chosen) exists only in server-side
  secrets.
- Read projection of approved content is separate from private moderation tables
  (COMMUNITY-ADMIN.md §"Roles and server data").
- External source-URL verification is allowlisted, size-bounded and
  private-network/redirect-blocking (SSRF rules in THREAT-MODEL.md) — it is
  verification, never a general proxy.

## 5. Explicit non-components

To prevent scope drift during implementation: no realtime subscriptions, no edge
CDN logic, no image/file upload pipeline, no email sending (moderator/author
notification is in-app or manual), no analytics/telemetry beyond provider logs, no
custom domain, no second database, no message queues.

created by stokonn with love <3
