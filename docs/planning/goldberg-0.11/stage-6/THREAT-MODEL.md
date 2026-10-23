# Stage 6 — Threat model and adversarial acceptance matrix

**Design only.** Formalizes the required threat tests from
[COMMUNITY-ADMIN.md](../COMMUNITY-ADMIN.md) (final section) into an acceptance
matrix with concrete expected outcomes and **execution venues**. No test requires a
paid service, real user data or real Google credentials — synthetic identities only.

## 1. Trust boundaries and assets

- **Assets:** moderation integrity (no unpublished change becomes public, no
  approved change is lost), audit-chain integrity, contributor account security,
  the 0 CZK budget, and — above all — **personal practice data** (which never
  crosses this system's boundary at all; DATA-MODEL.md §7).
- **Boundaries:** internet→API (untrusted), authenticated client→API (semi-trusted),
  API→database (server-trusted), operator→console (fully trusted, documented),
  export→owner storage (trusted at rest, encrypted).
- **Out of scope threats:** provider-internal compromise by the cloud operator
  (documented caveat, DATA-MODEL.md §6), physical device seizure, and Stage 4/5
  provider-adapter threats (covered by their own stages).

## 2. Execution venues

- **[L] Local, no cloud:** runnable now or at implementation time against the
  server code with an in-process/local database, fake clock and synthetic JWKS —
  the majority of the matrix.
- **[I] Integration, later separately approved:** needs a provisioned Free-tier
  project + real Google OAuth console (backend/region decisions now settled —
  D4a/D4b/D5c 2026-10-21 — but provisioning still requires an explicit owner
  instruction, and D5a/D5b/D5d pre-launch gates still apply before any real-user
  collection). Never run without that approval.

## 3. Adversarial acceptance matrix

| # | Attack / precondition | Expected outcome | Venue |
|---|---|---|---|
| T1 | Expired session/access token presented | 401 `unauthenticated`; no data; event logged without token material | [L] + [I] |
| T2 | Forged signature; unknown `kid`; algorithm `none`/confusion; JWKS rotation with cold cache | Deny (401); unknown-key with exhausted cache fails closed; rotation picked up within header-bounded cache window | [L] fake JWKS + [I] real rotation |
| T3 | Replayed Google credential / replayed refresh token | Challenge single-use (atomic consume); rotated refresh replay ⇒ family revocation + audit event; no second session | [L] + [I] |
| T4 | Wrong audience (`aud`) or issuer token | 401; exact-match enforcement | [L] + [I] |
| T5 | Valid token of a revoked moderator / suspended principal | 403 at next privileged request; revocation effective without waiting for token expiry | [L] |
| T6 | Client-supplied role/author/status fields (role injection, mass-assignment) | 400 validation error; state unchanged | [L] |
| T7 | Contributor reads another user's pending submission (direct ID guess) | 404 existence-hidden | [L] |
| T8 | Cross-user modification (PATCH/revision on someone else's submission) | 403/404; row unchanged | [L] |
| T9 | Direct REST/RPC privilege escalation (service endpoints without role, security-definer function called by anonymous) | 403 default-deny; functions have fixed `search_path`, revoked default execution (Supabase variant) | [L] |
| T10 | Two moderators approve/request-changes simultaneously | Exactly one decision commits; loser gets 409; single published revision; both audit rows coherent or loser absent | [L] |
| T11 | Duplicate submission with same `idempotencyKey` + identical body | Original result returned; **no second publication** | [L] |
| T12 | Same `idempotencyKey`, different body | 422 `idempotency_mismatch` | [L] |
| T13 | Malicious input: HTML/script in text fields, `javascript:`/`data:` URLs, overlong payloads, unknown fields, deeply nested JSON | 400; nothing persisted; clients render escaped regardless | [L] |
| T14 | SSRF: evidence URL pointing at `169.254.169.254`, localhost, private ranges, DNS rebinding, redirect chains to private hosts | Fetch refused (allowlist scheme/host, resolve-then-check, no private ranges, redirects re-validated, size/time bounded); never a general proxy | [L] |
| T15 | CSRF against Web Admin browser session; CORS from foreign origin | CSRF token mismatch ⇒ 403; cookie `SameSite=Strict` + origin check; foreign-origin CORS refused | [L] + [I] (browser) |
| T16 | Quota exhaustion: spam submissions from one principal and distributed IPs | 429 per-principal; 429/503 per-IP throttle; global kill switch denies intake; no pending/audit data deleted; local app stays usable | [L] + [I] |
| T17 | Partial transaction failure (fault injection mid-approval) | Full rollback: no published row, no status advance, no dangling audit gap; retry idempotent | [L] |
| T18 | Audit tampering attempt: update/delete via API or SQL simulation on a copy | API cannot mutate; on an exported copy, hash-chain verification fails loudly in the drill | [L] chain verify + [I] console path documented |
| T19 | Export then isolated restore into a fresh empty database | Restore succeeds; chain verification passes; counts and sample records match; RPO evidence recorded | [L] local engine + [I] real provider |
| T20 | Backend unavailable / paused / quota-exhausted / kill-switch on | App and Admin degrade gracefully: last-known overlay or offline state, local drafts intact, truthful "unavailable" (no fake success, no local lockout) | [L] + [I] |
| T21 | Personal-data isolation: crafted submission containing journal-like fields; network-layer assertion across the app | Validation rejects unknown fields; request-body allowlist regression test passes; zero personal rows leave device | [L] (client regression) + code review |
| T22 | Bootstrap/audit-mutation abuse; registered role requests, invalid sessions and malformed requests tested separately | Owner-approved local clarification: well-formed unregistered audit/bootstrap operation403 without dispatch; authenticated self-grant403; missing/invalid session on registered role route401; malformed operation/body400. No client audit-edit/bootstrap endpoint exists. HTTP binding remains unapproved. | [L] |

## 4. Abuse and denial-of-service posture (summary)

Consistent with CLOUD.md: burst/distributed abuse can still consume provider quotas
before application controls run; **graceful service denial is a required outcome**
(T16/T20), and quota exhaustion is never justification for paid escalation. Attestation
may supplement, never replace, authentication.

## 5. Non-claims

- This matrix is an acceptance plan; **no row may be marked passed until actually
  executed** in its venue and recorded in the stage verification ledger.
- Passing [L] rows without [I] rows does not authorize public collection (D5 gate).
- Hash-chained audit is tamper-*evidence*, not tamper-*proof* storage
  (DATA-MODEL.md §6).

created by stokonn with love <3
