# Goldberg 0.11 — Stage 6 implementation checklist (all tasks UNCHECKED)

Canonical policy: [AGENTS.md](../../../../AGENTS.md) §"Stage checklist documentation".
Every task below stays **unchecked** until its verification evidence actually exists
and is recorded. Planning (this directory) is complete; **no implementation has
started**. This file is synchronized with the Stage 6 section of
[STAGE-CHECKLIST.md](../STAGE-CHECKLIST.md) and the GitHub Issue tracking once
Stage 6 is separately authorized.

Status legend: ⬜ not started · 🚧 in progress · ✅ complete (verified)

**Overall status: ⬜ not started — blocked on owner decisions D4/D5/D8
([OPEN-DECISIONS.md](OPEN-DECISIONS.md)) and a separate implementation instruction.**

## Phase 0 — Decisions and provisioning (owner-gated)

- **6.0a** Settle D4/D4b (backend + free-tier acceptance) — owner decision; planning
  provides alternatives only. *Blocks: everything below.*
- **6.0b** Settle D5a–D5g (operator/privacy/region/retention/licensing/audience) —
  owner decision. *Blocks: provisioning, terms, public collection.*
- **6.0c** Settle D8a–D8c (Admin packaging, web session flow, bootstrap) — owner
  decision. *Blocks: Admin endpoint activation; not the API implementation itself.*
- **6.0d** Provision the chosen Free-tier backend under a **separate explicit owner
  instruction**: project, database, auth provider config, region per D5c, secrets in
  server-side secrets only, kill switch, export target. No billing path enabled.
- **6.0e** Configure Google OAuth console (redirect/origin allowlists, client IDs)
  per AUTH.md — separately authorized, real credentials never committed.

- [ ] 6.0a [ ] 6.0b [ ] 6.0c [ ] 6.0d [ ] 6.0e

## Phase 1 — Server core (implementable locally before provisioning)

- **6.1** Conceptual schema → concrete DDL + migrations for DATA-MODEL.md §1 tables,
  append-only triggers, hash-chain support, uniqueness constraints (identity,
  idempotency keys). *Depends: 6.0a for engine choice; DDL drafted per candidate.*
  *Evidence: migration tests on empty + seeded DB, both engines if undecided.*
- **6.2** Auth layer per AUTH.md §2–3: token verification, session mint/rotate/
  revoke, JWKS/keys handling, fake-clock + fake-JWKS test harness. *Evidence: T1–T4
  THREAT-MODEL rows.*
- **6.3** Authorization layer per AUTH.md §6: capability resolution, default-deny,
  ownership, actor≠author, suspension, no-self-grant. *Evidence: T5–T9, T22 rows.*
- **6.4** Submission/revision handlers per API-CONTRACTS.md §4 + DATA-MODEL.md §2:
  allowlist validation, size caps, immutable revisions, hash chain. *Evidence: T13,
  T21 rows; boundary-size fixtures.*
- **6.5** Moderation decision transaction per DATA-MODEL.md §4 (single atomic unit,
  conflict detection, audit + feed append). *Evidence: T10, T17 rows.*
- **6.6** Idempotency engine (key storage, request hash, 24h+ expiry, mismatch
  handling). *Evidence: T11, T12 rows.*
- **6.7** Public read overlay + change feed per API-CONTRACTS.md §5; separation of
  private moderation tables from public projection. *Evidence: no pending data
  readable; deterministic cursors.*
- **6.8** Admin endpoints per API-CONTRACTS.md §6 (queue, decisions, quarantine,
  rollback, audit view, roles, suspend) — including SSRF-guarded evidence-URL
  verification. *Evidence: T14, T22 rows.*
- **6.9** Abuse controls per API-CONTRACTS.md §7: per-principal + coarse-IP quotas,
  atomic accounting, global kill switch. *Evidence: T16 row.*
- [ ] 6.1 [ ] 6.2 [ ] 6.3 [ ] 6.4 [ ] 6.5 [ ] 6.6 [ ] 6.7 [ ] 6.8 [ ] 6.9

## Phase 2 — Android integration (optional feature, local-first)

- **6.10** Community repository + typed submission objects on the app side; strict
  request-body allowlist; platform-protected token storage excluded from backups;
  never upload journal/notes/mic/backups (DATA-MODEL.md §7). *Evidence: client
  regression tests (T21), backup-exclusion test, offline-draft tests.*
- **6.11** Error/offline/unavailable/rate-limit/quota UX contracts (API-CONTRACTS.md
  §2): truthful states, retry-without-duplicate, drafts always preserved, practice
  never blocked. *Evidence: JVM tests + emulator states; cold-backend startup test
  (STAGES.md Stage 6 acceptance: unavailability must not block local app).*
- [ ] 6.10 [ ] 6.11

## Phase 3 — Verification gates (STAGES.md Stage 6 acceptance)

- **6.12** Full THREAT-MODEL.md matrix executed: [L] rows locally; [I] rows only
  after separately approved integration environment. Real counts recorded.
- **6.13** Role removal / replay / expiry / JWK rotation exercised for real ([I]).
- **6.14** Rate/quota/kill-switch/budget tests including simulated provider
  exhaustion; graceful denial verified.
- **6.15** Export + **isolated restore drill** with audit-chain verification; RPO
  evidence recorded (ZERO-COST-OPS.md §4).
- **6.16** Privacy/terms/contact/licensing **published before collection** (D5 gate
  evidence) — no collection earlier under any circumstance.
- [ ] 6.12 [ ] 6.13 [ ] 6.14 [ ] 6.15 [ ] 6.16

## Phase 4 — Handoff and closure

- **6.17** Freeze API contract version + fixtures; produce Stage 7 Part B handoff
  (HANDOFF-STAGE7.md updated with actuals, contract hash/version pinned).
- **6.18** Sync STAGE-CHECKLIST.md, docs/ai/*, GitHub Issue/Project with executed
  evidence; close only with verified commit evidence. No merge/tag/release without
  separate authorization.
- [ ] 6.17 [ ] 6.18

## Progress log

- 2026-10-21 — Planning documentation prepared on
  `docs/goldberg-0.11-stage-6-planning` (this directory + additive checklist/decisions/
  handoff/stages updates). **Uncommitted, awaiting owner approval. No implementation,
  no provisioning, no cloud/OAuth, no code.**

created by stokonn with love <3
