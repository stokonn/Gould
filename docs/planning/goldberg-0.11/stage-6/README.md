# Goldberg 0.11 — Stage 6: Secured Optional Community Backend — PLANNING PROPOSAL

## Existing Supabase project — externally verified reconciliation (2026-10-22)

The owner relayed verification by **ChatGPT's connected Supabase integration** on
2026-10-22: existing organization/project identity, organization Free/`tier_free`,
project Frankfurt (`eu-central-1`), `ACTIVE_HEALTHY`, PostgreSQL 17.11, **0 public
tables, 0 recorded migrations, 0 Edge Functions**. These are externally verified
observations; **this coding agent did not execute those checks**. Display names
stay in ignored local notes under the public no-account-identifiers policy.
This session still exposes no callable Supabase tools; that capability limitation
does not negate the separate external verification.

Billing/payment methods, quota usage, Auth providers/security, direct database
connectivity from this agent and non-public schema configuration remain **UNVERIFIED**.
Zero public tables is not proof that every schema is empty or that Auth is disabled.
Production backend **NOT IMPLEMENTED** in the verified repository; Cloudflare Worker
**NOT DEPLOYED** in the verified record. Google OAuth not configured in Goldberg's
implementation record; hosted Google configuration UNVERIFIED. Real-user collection
**NOT AUTHORIZED**. Do not create another project or modify the existing one.

Q1/D5b, C4, C2 §12 and 6.0f remain **OPEN** (6.0f UNCHECKED); 6.0d/6.0e **NOT
AUTHORIZED**, 6.1–6.18 UNCHECKED; ORD-A sequencing only; 0 CZK/month permanently.
Project existence does not complete/authorize 6.0d. Q1-C denotes the operational
hold, not zero-resource inventory; no operator option is selected. Prior zero-project
observations are historical. Stage 7A conditional/X10 OPEN and 7B not started stay
unchanged; no Stage 7 work is performed here.

Status: **planning prepared and owner decision review completed (2026-10-21);
unimplemented, awaiting a separate Stage 6 implementation instruction.** The owner
settled the design decisions D4/D5/D8 on 2026-10-21 (outcomes recorded in
[OPEN-DECISIONS.md](OPEN-DECISIONS.md)); several **legal/operational details and
pre-launch gates remain open**, and **provisioning, OAuth configuration and the
implementation tasks themselves are separately gated**. This planning work created no application code, cloud accounts or infrastructure,
and changed no billing or OAuth configuration. The existing
Supabase project is externally verified by ChatGPT connected integration (C4 §H); production backend implementation has not started and
nothing in this directory is deployed.

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
- Open-decision register for D4/D5/D8 with alternatives, trade-offs and the
  **owner's 2026-10-21 decision outcomes** (approved / provisional / deferred /
  conditionally approved) plus the remaining gates.
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
| Stage 5 offline bundle boundaries | ✅ complete (owner-approved 2026-10-21; real-CDN transfer measurements preserved as a future integration dependency) | No code dependency; published overlay must degrade gracefully offline (cache is Stage 5's concern only). |
| Owner decision **D4** (backend + free-tier acceptance) | ✅ decided 2026-10-21 — Supabase Free, graceful denial, fail closed, strict 0 CZK | Provisioning and all server implementation remain gated on a separate owner instruction. |
| Owner decision **D5** (operator/privacy/retention/licensing/audience) | 🚧 partially decided 2026-10-21 — D5c/D5f/D5g approved; D5a provisional; D5b/D5d details deferred to pre-launch review | Blocks public collection, privacy/terms publication; D5c verification list gates provisioning. |
| Owner decision **D8** (Admin packaging, web session flow, bootstrap) | 🚧 decided 2026-10-21 with a condition — D8a/D8c approved; D8b BFF approved **conditionally** on proving the 0 CZK fit before implementation | Admin-client direction is fixed; D8b proof is a pre-implementation gate. |
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
| [BFF-FEASIBILITY.md](BFF-FEASIBILITY.md) | D8b zero-cost proof (task 6.0f): provider evaluation, topology, security assessment, capacity model, CONDITIONAL PASS verdict. |
| [C2-SPIKE-PLAN.md](C2-SPIKE-PLAN.md) | C2 CPU/auth spike test specification: local `workerd` runtime design, synthetic fixtures, S1–S19 security matrix, CPU methodology, targets, acceptance outcomes. **Historical spec — the spike was executed 2026-10-21 → CONDITIONAL PASS** on `spike/goldberg-0.11-c2-bff` (report `spikes/bff-c2/results/SUMMARY.md`, final commit `7bc6976`); spike code stays isolated, unmerged. |
| [C3-EDGE-PRIVACY-REVIEW.md](C3-EDGE-PRIVACY-REVIEW.md) | C3 D5b/D5c edge privacy & data-processing review (documentation-only): Cloudflare/Supabase primary-source findings, data-flow mapping, transfer analysis, risk matrix, Free-tier mitigations, open legal/operator questions, conditional-viability verdict, pre-provisioning checklist, sources with access dates. |
| [Q1-OPERATOR-READINESS.md](Q1-OPERATOR-READINESS.md) | Q1/D5b operator-readiness research (documentation-only; reassessed 2026-10-21): verified contractual/capacity facts with sources (no published minimum account age on either provider; authority-to-bind warranties; CF SSSA §2.2.1(a); Czech Civil Code §§15/18/30–37/420–421; Trade Act §2/§3(1)(b); GDPR Art. 2/4/8 via non-official mirror), a QD-1…QD-10 findings classification, free/non-commercial direction PD-1, operator **Options A/B/C + candidate D (legally capable natural person in own name) with an A/B/C/D matrix and no selection** — D5b stays legally unresolved and blocking for 6.0d; capacity is an eligibility precondition only, never a recorded personal fact. |
| [Q1-PROFESSIONAL-REVIEW-BRIEF.md](Q1-PROFESSIONAL-REVIEW-BRIEF.md) | Identity-free professional-review handoff (documentation-only, 2026-10-22): all **13 questions Q4.1–Q4.13 preserved verbatim** with primary-source references, neutral A/B/C/D summary (Q1-D = candidate for review, explicitly not approved), verified law vs unresolved interpretation split, official **ÚOOÚ + European Commission** guidance (accessed 2026-10-22), and the **C-1…C-6 confirmations a qualified professional must return before Q1/D5b can be resolved**. Performs no review, selects nothing, authorizes nothing. |
| [C4-PREPROVISIONING-CHECKLIST.md](C4-PREPROVISIONING-CHECKLIST.md) | C4 **pre-provisioning** checklist (2026-10-21 baseline): per-item source URLs, version/date stamps, verified-today vs must-reverify-on-provisioning-day columns, §D execution runbook — preparation only; **C4 final verification stays OPEN** and must run immediately before any separately authorized provisioning. |
| [CLOUD-READINESS-PLAN.md](CLOUD-READINESS-PLAN.md) | Cloud-readiness plan (documentation-only, 2026-10-21): reconciled operator decision (cloud architecture is the intended deployment path instead of temporary local-only Option C; "minor owner" premise reclassified as unverified); the 6.0f ↔ provisioning **gate-ordering** analysis — **ORD-A APPROVED 2026-10-21 as the sequencing strategy (sequencing only; ORD-B/C not selected)** — with the approved 11-step sequence, the reconciled gate table and the **ORD-A readiness checklist** for the future owner decision; the exact future **private-cloud verification runbook** (CR-0…CR-8: account readiness, no-billing verification, Frankfurt region, secrets+MFA, CPU/cold-start/Supabase-latency/quota-fail-closed/logging measurements, rollback/cleanup — **synthetic data only**); Free-tier usage estimates (0 CZK); stop/rollback conditions. **Authorizes nothing.** |
| [CHECKLIST.md](CHECKLIST.md) | Stage 6 implementation checklist — every task unchecked until executed and verified. |
| [HANDOFF-STAGE7.md](HANDOFF-STAGE7.md) | Frozen-contract handoff requirements for the Stage 7 Admin clients. |

## 6. Status

- **Done (planning):** this directory, plus additive Stage 6 sections in
  `STAGE-CHECKLIST.md`, `STAGES.md` (reconciliation notes), `docs/ai/DECISIONS.md` and
  `docs/ai/HANDOFF.md` — committed and pushed on
  `docs/goldberg-0.11-stage-6-planning`; the owner's decision review was recorded in a
  follow-up documentation commit (2026-10-21).
- **Owner decisions (2026-10-21):** D4a/D4b, D5c, D5f, D5g, D8a and D8c approved;
  D8b approved conditionally (zero-cost BFF proof required before implementation);
  D5a provisional (no contact supplied — nothing may be published); D5b/D5d details
  deferred to pre-launch review. Full outcomes and remaining gates:
  [OPEN-DECISIONS.md](OPEN-DECISIONS.md).
- **Owner preference (2026-10-22):** the owner recorded **Q1-D — operation
  under their own name as a legally capable natural person — as the preferred
  operator model**, subject to contractual eligibility determination and
  professional verification. **A directional preference only: not a
  selection, not an approval, not a D5b resolution** — D5b stays legally
  unresolved and blocking for 6.0d; Q1-C remains the operative posture
  (operational hold; owner-created project reported in C4 §H); see
  [Q1-OPERATOR-READINESS.md](Q1-OPERATOR-READINESS.md) PREFERENCE ADDENDUM.
- **Not started:** every Stage 6 implementation task (6.1–6.18 in `CHECKLIST.md`),
  authorized production backend setup, OAuth configuration and integration execution.
  The owner-created Supabase project and public metadata are externally verified
  by ChatGPT connected integration; remaining hosted schema/security configuration
  is UNVERIFIED. No backend implementation exists.
- **Still requires explicit owner approval:** implementation start, provisioning
  (6.0d), Google OAuth console configuration (6.0e), and opening the service to real
  contributors (pre-collection gate, OPEN-DECISIONS.md §5).

created by stokonn with love <3


## Current Stage 6 continuation (2026-10-22)

[READINESS-CONTINUATION.md](READINESS-CONTINUATION.md) is the latest documentary
recovery/readiness checkpoint; [POSTGRESQL-DESIGN-REQUIREMENTS.md](POSTGRESQL-DESIGN-REQUIREMENTS.md)
contains future 6.1 design inputs. Earlier “no accounts” wording is historical:
owner-created accounts exist, with owner-attributed observations in C4 §F.5.
This does not authorize provisioning or resolve any gate.
