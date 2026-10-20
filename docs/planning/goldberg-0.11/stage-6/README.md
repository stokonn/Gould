# Goldberg 0.11 — Stage 6: Secured Optional Community Backend — PLANNING PROPOSAL

Status: **planning prepared, unimplemented, awaiting owner decisions D4/D5/D8 and a
separate Stage 6 implementation instruction.** Prepared 2026-10-21 as an
owner-approved documentation-only deliverable on the isolated
`docs/goldberg-0.11-stage-6-planning` branch (based on Stage 5 checkpoint `bcd8894`).
No application code, cloud account, infrastructure, billing or OAuth configuration was
created, changed or provisioned. No backend exists; nothing in this directory is deployed.

Canonical policy: [AGENTS.md](../../../../AGENTS.md). Stage register:
[STAGES.md](../STAGES.md). Living checklist: [STAGE-CHECKLIST.md](../STAGE-CHECKLIST.md).
Authoritative prior designs (preserved unchanged, referenced not forked):
[COMMUNITY-ADMIN.md](../COMMUNITY-ADMIN.md), [CLOUD.md](../CLOUD.md).

## 1. What Stage 6 is for

Stage 6 adds an **optional, secured community backend** that lets consenting users
propose catalogue metadata corrections/additions and lets moderators review them —
without ever touching personal practice data. It exists to make the community
contribution loop real for Stage 7's Admin clients, under a strict 0 CZK/month
operational contract.

The existing Stage 1 designs in `COMMUNITY-ADMIN.md` (roles, moderation lifecycle,
API surface, threat list) and `CLOUD.md` (free-tier comparison, operating contract,
budgets) remain the **canonical baseline**. This stage-6 directory turns them into an
implementation-ready package: concrete contracts, state machines, data shapes, an
adversarial acceptance matrix, an operations plan and a decision register. Where this
documentation extends the baseline, it does so additively and cites it.

## 2. In scope

- Server-enforced authentication (Google identity, server validation), sessions and
  five roles (public reader, contributor, moderator, administrator, deployment
  operator) with default-deny authorization.
- Versioned `/v1` API contracts usable identically by the future Android Admin and
  Web Admin clients (Stage 7 Part B) and by the contributor surface in the app.
- Submission/revision state machines, immutable revision evidence, review-queue
  operations, idempotency, audit events, publication rollback and quarantine.
- Conceptual data model linked to Stage 2/4 typed catalogue UUIDs and provenance.
- Adversarial threat model and acceptance matrix, split into locally-runnable and
  later-integration test venues.
- Zero-cost operations, monitoring, export/restore and kill-switch procedures.
- Open-decision register for D4/D5/D8 with alternatives, trade-offs and
  recommendations (decisions themselves remain **owner calls, unresolved**).
- Stage 6 → Stage 7 handoff contract.

## 3. Out of scope (hard non-goals)

- **No practice-journal sync of any kind.** No sessions, notes, repertoire,
  microphone data, local avatars/nicknames, portable backups or recovery evidence
  ever leave the device through this system. The submission schema physically cannot
  carry personal fields (see `DATA-MODEL.md` §7).
- **No automatic publication.** Every published change passes a moderator decision
  transaction. No bulk import, no source write-back, no self-approval.
- **No cloud provisioning, billing, trial activation or real Google OAuth
  configuration** in this stage or by this documentation. Provisioning requires a
  subsequent explicit owner instruction after D4/D5/D8 are settled.
- **No Stage 7 UI.** Admin clients consume the contracts defined here but are built
  only in Stage 7 Part B. No speculative integration may be shown as working.
- **No Stage 5 interference.** Offline bundles/Smart Cache keep their boundaries;
  Stage 6 consumes Stage 4 catalogue identities read-only and must not alter bundle
  behavior (`OFFLINE-CACHE.md` remains authoritative).
- **No paid service activation ever**, per the permanent 0 CZK contract
  (`CLOUD.md` §"Zero-cost operating contract").

## 4. Dependencies

| Dependency | State | Effect |
|---|---|---|
| Stage 2 typed identities + provenance contracts | ✅ verified (Stage 2/3A/4) | Submissions map to catalogue UUIDs; expectedRevision semantics reuse catalogue revisions. |
| Stage 4 catalogue store/provenance | ✅ verified | Published overlay reads reuse typed result envelopes and rights/attribution rules. |
| Stage 5 offline bundle boundaries | 🚧 in progress (5.10–5.13 open) | No code dependency; published overlay must degrade gracefully offline (cache is Stage 5's concern only). |
| Owner decision **D4** (backend + free-tier acceptance) | ⬜ unresolved | Blocks provisioning and all server implementation. |
| Owner decision **D5** (operator/privacy/retention/licensing/audience) | ⬜ unresolved | Blocks public collection, OAuth console setup and terms publication. |
| Owner decision **D8** (Admin packaging, web session flow, bootstrap) | ⬜ unresolved | Blocks Admin-client activation; API contracts here are client-agnostic meanwhile. |
| Stage 7 Part B (Admin clients) | ⬜ gated on this stage | Consumes `API-CONTRACTS.md` + `HANDOFF-STAGE7.md`. |

## 5. Document map

| Document | Purpose |
|---|---|
| [ARCHITECTURE.md](ARCHITECTURE.md) | Backend comparison update (Supabase Free vs Workers+D1), recommended pilot, component layout. |
| [AUTH.md](AUTH.md) | Google token verification, session lifecycle, revocation, browser/Android security, admin bootstrap. |
| [DATA-MODEL.md](DATA-MODEL.md) | Conceptual schema, moderation state machines, immutability, retention shapes. |
| [API-CONTRACTS.md](API-CONTRACTS.md) | Versioned `/v1` endpoints, envelopes, error taxonomy, pagination, idempotency. |
| [THREAT-MODEL.md](THREAT-MODEL.md) | Adversarial acceptance matrix with execution venues (local vs later integration). |
| [ZERO-COST-OPS.md](ZERO-COST-OPS.md) | 0 CZK operations, monitoring, budgets, export/restore, kill switch. |
| [OPEN-DECISIONS.md](OPEN-DECISIONS.md) | D4/D5/D8 decision table: alternatives, trade-offs, recommendations, blocked work. |
| [CHECKLIST.md](CHECKLIST.md) | Stage 6 implementation checklist — every task unchecked until executed and verified. |
| [HANDOFF-STAGE7.md](HANDOFF-STAGE7.md) | Frozen-contract handoff requirements for the Stage 7 Admin clients. |

## 6. Status

- **Done (planning):** this directory, plus additive Stage 6 sections in
  `STAGE-CHECKLIST.md`, `STAGES.md` (reconciliation note), `docs/ai/DECISIONS.md` and
  `docs/ai/HANDOFF.md` — applied on this isolated branch, **uncommitted, awaiting
  owner commit approval**.
- **Not started:** every Stage 6 implementation task (6.1–6.18 in `CHECKLIST.md`),
  provisioning, OAuth configuration, integration environments. No backend code, no
  cloud resources, no credentials exist.
- **Authorization:** commits, pushes, GitHub sync, provisioning and implementation all
  require explicit owner approval after the D4/D5/D8 review described in
  [OPEN-DECISIONS.md](OPEN-DECISIONS.md).

created by stokonn with love <3
