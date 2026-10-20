# Stage 6 → Stage 7 handoff requirements (Admin clients)

**Design only.** Defines what the Stage 7 Part B Android/Web Admin clients may rely
on once Stage 6 is implemented and verified, and what they must never assume. The
Stage 7 planning branch (`docs/goldberg-0.11-stage-7-planning`) is **not modified**
by this document; Stage 7 Part A (six-screen UX) has no Stage 6 dependency and
remains gated only on Stage 5.

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
| D8a (Android packaging) / D8b (web session) / D8c (bootstrap) | ⬜ unresolved owner decisions |
| Provisioned+authorized backend for live integration | ⬜ blocked on D4/D5 + explicit owner instruction |

Stage 7 Part B cannot start before this handoff is satisfied with executed evidence.
Stage 7 Part A remains independent (Stage 5-gated only).

created by stokonn with love <3
