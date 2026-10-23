# Stage 6 → Stage 7 handoff requirements (Admin clients)

**Design only.** Defines what the Stage 7 Part B Android/Web Admin clients may rely
on once Stage 6 is implemented and verified, and what they must never assume. The
Stage 7 planning branch (`docs/goldberg-0.11-stage-7-planning`) is **not modified**
by this document; Stage 7 Part A (six-screen UX) has no Stage 6 dependency — its
Stage 5 prerequisite is complete (owner-approved 2026-10-21) — but starting Stage 7
itself still requires a separate owner instruction.

## 1. What Stage 6 will deliver to Stage 7

1. **One frozen, versioned API** — `/v1` exactly as
   [API-CONTRACTS.md](API-CONTRACTS.md), with contract fixtures (valid cases + every
   error class + idempotency replays + boundary sizes). Both Admin clients are built
   against these fixtures first; UI work never invents endpoints.
2. **A shared capability model** — `GET /v1/me/capabilities` reflects the
   server-computed role capabilities from [AUTH.md](AUTH.md) §6. Clients render from
   it for convenience; **authorization enforcement is always server-side** (wrong-role
   clients and forged calls must fail — STAGES.md Stage 7 acceptance).
3. **Deterministic moderation semantics** — submission lifecycle
   ([DATA-MODEL.md](DATA-MODEL.md) §3), one-transaction decisions, 409-on-conflict,
   idempotency keys, audit visibility for admins.
4. **A contract test suite** reusable from both clients' CI (Kotlin JVM tests and
   web tests hit the same fixture expectations against a local server instance).

## 2. Requirements Stage 7 Admin UI must satisfy (implied by this contract)

- **Conflict UX is mandatory:** a 409 from `decisions` must render a reloadable
  conflict state showing the current server revision — never a silent retry, never a
  false success (Stage 7 acceptance: "concurrent edits show conflict").
- **Idempotent action UX:** every approve/reject sends a stable `idempotencyKey`;
  network retry reuses it; duplicate publication is impossible by construction and
  the UI must not generate fresh keys per retry.
- **Evidence display:** pending diffs must render source evidence URLs and license
  intent, plain-text escaped (no HTML rendering of contributor text).
- **Reason-required actions:** reject/request-changes/quarantine/rollback send
  explicit reasons; the UI surfaces server validation errors distinctly from 401/
  403/404/409/429/503 (error taxonomy table is normative).
- **Account switching:** sign-out/account change must not let a different local
  account submit another user's local draft (Stage 7 acceptance) — client-side
  ownership binding plus server-side enforcement.
- **No journal access:** Admin clients never show, fetch or transmit personal
  practice data; the API has no endpoint for it.
- **Offline/unavailable honesty:** 503/unreachable renders truthful unavailable
  states; failed requests remain retryable; no speculative integration shown as
  working.

## 3. What Stage 7 must NOT assume

- No realtime/push notifications (polling the queue is the model).
- No webhooks, no email delivery, no file/media viewing (evidence is URLs/refs only).
- No bulk approval; no client-side role editing beyond the admin endpoints listed;
  no audit-log editing capability exists at all.
- No SLA, no availability guarantee, no "always-on" test environment (fixtures are
  the default development target; a live backend is optional and separately
  approved).
- No promise that overlay reads are complete — the overlay is a small moderated
  layer over the Stage 4 catalogue, not a mirror.

## 4. Freeze and versioning procedure

At Stage 6 task 6.17, the API contract, capability model and fixtures get a pinned
version identifier (e.g. `api-v1` + fixture-set hash) recorded in this file and the
Stage 6 checklist. Post-freeze changes to `/v1` are additive-only; breaking changes
require `/v2` and a new handoff section. Stage 7 reports any contract gap back to
Stage 6 tracking rather than silently diverging — per the owner's instruction, a
conflict between Stage 6 findings and approved Stage 7 requirements is **reported,
not silently resolved**.

## 5. Dependency status

| Stage 6 prerequisite for… | State |
|---|---|
| API contract freeze (6.17) | ⬜ not started |
| Verified threat matrix (6.12–6.14) | ⬜ not started |
| D8a (Android packaging: in-app, no separate APK) / D8b (BFF session — approved conditionally; zero-cost BFF proof 6.0f still open) / D8c (console bootstrap, MFA operator) | ✅ owner decisions recorded 2026-10-21; D8b proof gate open |
| Provisioned+authorized backend for live integration | ⬜ backend decided (Supabase Free, D4a) but provisioning still requires an explicit owner instruction + D5c verification; D5a/D5b/D5d pre-launch gates open |

Stage 7 Part B cannot start before this handoff is satisfied with executed evidence.
Stage 7 Part A remains independent of Stage 6 (its Stage 5 prerequisite is complete;
starting Stage 7 still needs a separate owner instruction).

created by stokonn with love <3

## Current local coordination checkpoint — Stage6.12, not6.17 freeze

The design-only handoff above remains the future acceptance target, not a claim
that its gates passed. Stage7B local work was separately owner-authorized and is
already in progress; no Stage7B source is changed here.

See [current readiness report](STAGE7B-CONTRACT-READINESS.md): G10's Android disabled
transport boundary remains closed by read-only static compatibility; G1–G9 andG11
remain blocked for actual integration. `contracts/admin/v1` contains30 real-service/
scripted-storage captures,11 inactive proposals and29 preserved authored Stage7B
comparison cases, with source/fixture hashes. These are not public HTTP or PostgreSQL
proof and not an API freeze. No heuristic dialect mapping or live activation.

The owner-approved T22 local routing clarification is recorded in
[T22 investigation](T22-INVESTIGATION.md); new unit/fixture evidence does not replace
updated PG17.11, HTTP or Android runtime acceptance. Formal6.12 stays PARTIAL;
6.17 and6.13–6.18 are not started by this audit/fixture coordination.

## Post-T22 Fedora verification — local proof accepted, public binding still OPEN

Owner-tested6d0abad version3: outer108 + matching nested237 =345 successful
executions/313 tests counting repeated32 suite once;205 PostgreSQL-backed bodies.
Explicit exit0,82 source hashes, exact stdout linkage and container-removal output
verified. T22 LOCAL PASS under the approved clarification; original FAIL retained.
Overall6.12 remains PARTIAL. G10 stays closed on Android; G1–G9/G11 remain blocked
for actual integration, with no fixture activation, HTTP binding or6.17 freeze.
See the current security matrix and STAGE7B-CONTRACT-READINESS.md. No repeat of
this successful PostgreSQL run is needed solely to update evidence/publication.
