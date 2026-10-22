# Stage 6 — Authentication and authorization contracts

**Design only, no implementation.** Extends the canonical authentication boundary in
[COMMUNITY-ADMIN.md](../COMMUNITY-ADMIN.md) §"Authentication boundary" with
implementation-ready specifics. **Owner decisions (2026-10-21): Supabase Free is the
approved backend (D4a); the BFF browser flow is approved conditionally (D8b, §5);
out-of-band console bootstrap is approved (D8c, §7)** ([OPEN-DECISIONS.md](OPEN-DECISIONS.md));
the invariants below hold regardless. No custom cryptographic primitives are designed
here — only composition of platform primitives and audited libraries.

## 1. Identity

- Stable identity = **verified `issuer` + `subject`** pair from Google's ID token.
  Never mutable email, display name, photo or device preference.
- The local optional Google *profile* feature in the app remains presentation-only
  and grants **no role** (COMMUNITY-ADMIN.md baseline, unchanged).
- Server stores a `principals` row keyed by internal UUID with the minimal stable
  subject (`iss`+`sub`), creation time and moderation state. No Google profile
  history, photos or email beyond what D5 retention later permits.

## 2. Token verification (server-side, both variants)

Mandatory checks before any session is minted or API JWT accepted:

1. Signature against provider keys — Supabase: managed Auth-issued API JWTs
   verified server-side with the project's JWT secret/JWKS; Workers variant: Google
   ID token verified against Google's cached JWKS with `kid` selection and an
   **allowlist of accepted algorithms** (reject `none`, reject algorithm confusion).
2. `iss` exact match (Google accounts issuer, or Supabase project issuer).
3. `aud` exact match to the configured client ID(s) — wrong-audience tokens always
   rejected.
4. `exp`/`iat` with a small bounded clock skew (≤60 s); expired tokens rejected.
5. `nonce`/PKCE state binding for the sign-in attempt where the flow uses them
   (browser flow below; Android uses the platform credential flow).
6. Unknown `kid` with an exhausted key cache ⇒ **deny**, never fall through
   (COMMUNITY-ADMIN.md rule, kept).

Key material is cached per provider headers (`Cache-Control` max-age bounds); cache
expiry failure denies authorization. JWKS rotation must be exercised in the
integration test plan (THREAT-MODEL.md T2).

## 3. Session lifecycle

- Sign-in: `POST /v1/session` with a fresh Google credential ⇒ server performs §2
  checks, resolves/creates the principal, applies **server-side account-state gates**
  (suspension ⇒ limited session or 403 per policy), mints a short-lived service
  session token.
- Sessions are **server-tracked** (revocable): session ID + principal + issued/expires
  + revoked flag. Access tokens are short-lived (proposed 15 min); refresh is
  rotating and revokes the used refresh token (replay of a rotated refresh token ⇒
  session family revoked + audit event).
- **Replay protection:** service tokens are bearer but single-session-bound and
  short-lived; refresh rotation catches replay. Supabase variant additionally relies
  on managed Auth token lifetimes. Nonce/challenge single-use is enforced in the
  Workers variant (challenge stored consumed atomically).
- Sign-out: `DELETE /v1/session` revokes server-side immediately. Role revocation or
  suspension is effective at next authorization check **without waiting for token
  expiry** (session table is consulted on every privileged request).
- Every request: authenticate (token valid) → load session (not revoked) → load
  principal (not suspended/deleted) → capability check. Any step failing ⇒ deny.

## 4. Android client security

- Service tokens stored in **platform-protected storage** (EncryptedSharedPreferences
  or Keystore-backed equivalent); **excluded from portable backups/exports**
  (backup rules must exclude token files; regression-tested).
- Tokens never logged, never placed in URLs, never in screenshots of error states.
- Sign-out clears tokens locally even if the server call fails (retry queued, but
  local credentials gone first).
- Offline draft editing never authorizes publishing (drafts are device-local until
  an authenticated explicit submit).

## 5. Browser (Web Admin) security — D8b: BFF approved conditionally (2026-10-21)

**Owner decision (2026-10-21): the BFF flow is APPROVED CONDITIONALLY** for the Web
Admin (OPEN-DECISIONS.md §3):

- **BFF (approved direction):** same-origin `HttpOnly; Secure; SameSite=Strict`
  session cookie + CSRF token on every state-changing request; the tiny static
  frontend never sees a bearer token; server-side session table as above. Strongest
  XSS posture; requires the small backend to serve the session endpoints.
- **Conditional gate — before any implementation:** prove that the selected BFF
  hosting/runtime fits the permanent **0 CZK/month** requirement, including actual
  Free-tier limitations. If the zero-cost condition cannot be proven, **report the
  blocker and request an owner decision — do not silently replace the approved
  architecture** (CHECKLIST.md 6.0f/D8b gate). Feasibility study executed 2026-10-21:
  **CONDITIONAL PASS** — recommended topology is a single-origin Cloudflare Workers
  Free Worker (static bundle + `/v1` BFF) in front of the Supabase Free data plane;
  a Supabase-only same-origin host is blocked at 0 CZK (Edge Functions rewrite
  `text/html` without a paid custom domain). Full evidence, capacity model and
  conditions C1–C4: [BFF-FEASIBILITY.md](BFF-FEASIBILITY.md). **C1 APPROVED
  2026-10-21:** Cloudflare Workers Free is the approved BFF hosting/runtime (single
  origin; Supabase Free data/auth backend; `eu-central-1`; no paid plans/billing/
  trials/upgrades; architecture-level only — no provisioning, deployment or
  implementation authorized). **C2 executed 2026-10-21 (separate owner
  authorization; isolated local `workerd`, synthetic identities) → CONDITIONAL
  PASS** — 19/19 security, P1–P7 executed, kernel CPU 2.03 ms/invocation;
  per-invocation production CPU unverified and the §12 production-runtime
  verification remains owner-gated (report `spikes/bff-c2/results/SUMMARY.md`
  on `spike/goldberg-0.11-c2-bff`, final commit `7bc6976`). **C3 documentary
  review complete 2026-10-21**
  ([C3-EDGE-PRIVACY-REVIEW.md](C3-EDGE-PRIVACY-REVIEW.md)) — conditional
  viability, no compliance claim; C4 open.
- The static-SPA alternative is **not selected** and must not be substituted without
  a new owner decision (it must never pretend to have HttpOnly credential storage —
  COMMUNITY-ADMIN.md prohibition).

Shared browser requirements: exact redirect/origin allowlists
(no wildcards), `state`+PKCE where applicable, nonce, CSRF protection, strict CSP
(`default-src 'self'`; no inline script), no tokens in `localStorage` for the BFF
flow, CORS restricted to the one admin origin, referrer-policy and frame-ancestors
hardening. Cookies `Secure` only; local dev over plain HTTP is not a shipping
configuration.

## 6. Roles, capabilities, default-deny

Roles exactly as in [COMMUNITY-ADMIN.md](../COMMUNITY-ADMIN.md) §"Roles and server
data": public reader, authenticated contributor, moderator, administrator,
deployment operator. **Owner decision D5g (2026-10-21): moderation starts
owner-only** — the moderator/administrator grant paths below must exist and be
secure from day one so moderators can be added or revoked later, but no moderator
is seeded at launch. Contract details:

- Authorization is **capability-based per endpoint**, resolved from trusted
  `role_grants` rows — never from client-supplied claims, user-editable metadata or
  JWT JSON fields the client can influence.
- **Default-deny:** an endpoint with no matching grant rule returns 403 (or 404 for
  existence-hiding). Unknown roles, empty grants and error states deny.
- Contributors can only touch **their own** submissions (ownership enforced in the
  query, not by hidden UI). Moderators cannot self-approve their own submissions
  (actor≠author enforced server-side). Administrators get role/suspension control +
  quarantine/rollback but **cannot edit the append-only audit log** through any
  client endpoint. Deployment operator operates entirely outside both clients
  (console/secrets/exports).
- Suspension blocks writes immediately and reads per policy; it is a
  `principals.state` check, not a role deletion, so audit history stays intact.

## 7. Admin bootstrap and recovery — D8c approved (2026-10-21)

**Owner decision (2026-10-21): the out-of-band console procedure below is APPROVED**
(OPEN-DECISIONS.md §3), with an added requirement: **privileged operator access must
be MFA-protected**, and every grant/recovery action must be auditable.

- **No pre-created trusted administrator identities may be invented.** At
  provisioning time (a future, separately authorized step) the deployment operator
  performs an out-of-band grant: the owner signs in with their own Google account
  through the normal flow, and the operator inserts the first
  `role_grants (principal, 'administrator')` row directly in the **Supabase Console**,
  recording the grant in the audit log.
- Recovery (lost admin access): the same out-of-band console path re-grants to a
  verified subject; no emailed magic links, no support-ticket identity proofing, no
  hardcoded bypass accounts, no "break-glass" API key. The recovery procedure is
  documented in ZERO-COST-OPS.md and exercised at least once in the restore drill.
- Secrets (JWT/service keys, export keys) live only in provider secrets managers or
  the owner's password manager — never in the repo, APK, web bundle, logs or audit
  events (AGENTS.md permanent rule).

created by stokonn with love <3

## Read-only readiness audit (2026-10-22; implementation gates unchanged)

[SECURITY-AUTH-READINESS-AUDIT.md](SECURITY-AUTH-READINESS-AUDIT.md) supplies the
current catalog/flag-only checklist for existing Auth providers, redirects,
sessions/MFA, RLS/ACLs and API exposure. It records the owner-supplied event-trigger
warnings, execution-context limitations and remaining unknowns. It does not
configure OAuth, create sessions/users, execute RPC, change privileges or verify
any integration/runtime acceptance row. Existing AUTH.md contracts remain binding.
