# Goldberg 0.11 — Stage 6 implementation checklist (all tasks UNCHECKED)

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

Canonical policy: [AGENTS.md](../../../../AGENTS.md) §"Stage checklist documentation".
Every task below stays **unchecked** until its verification evidence actually exists
and is recorded. Planning (this directory) is complete; **no formal implementation has
started**. This file is synchronized with the Stage 6 section of
[STAGE-CHECKLIST.md](../STAGE-CHECKLIST.md) and the GitHub Issue tracking once
Stage 6 is separately authorized.

Status legend: ⬜ not started · 🚧 in progress · ✅ complete (verified)

**Overall status: ⬜ not started — owner decisions recorded 2026-10-21 (see
[OPEN-DECISIONS.md](OPEN-DECISIONS.md)); implementation remains blocked on a
separate owner instruction, the D8b zero-cost proof, and the remaining D5
pre-launch gates.**

## Phase 0 — Decisions and provisioning (owner-gated)

- **6.0a** Settle D4/D4b (backend + free-tier acceptance). ✅ **Decision recorded
  2026-10-21: Supabase Free approved; inactivity suspension/quotas accepted with
  graceful degradation, fail-closed, strict 0 CZK** (OPEN-DECISIONS.md §1).
- **6.0b** Settle D5a–D5g (operator/privacy/region/retention/licensing/audience).
  🚧 **Partially settled 2026-10-21:** D5c (eu-central-1), D5f (general audience,
  minimal data) and D5g (owner-only moderation, server-side audited grants) approved;
  D5a provisional — **no contact email supplied or authorized for publication**;
  D5b controller determination **deferred to pre-launch** (research deliverable
  prepared: [Q1-OPERATOR-READINESS.md](Q1-OPERATOR-READINESS.md) — **still
  legally unresolved and blocking for 6.0d**; reassessed 2026-10-21 with option
  sets **A/B/C/D — candidate Q1-D (natural person in own name) added, none
  selected**; **owner preference recorded 2026-10-22: Q1-D is the PREFERRED
  operator model, subject to contractual eligibility + professional
  verification — a directional preference only, NOT a selection or
  resolution; D5b stays legally unresolved and blocking**; plus product
  direction PD-1 free/non-commercial); D5d principle approved
  with **retention/deletion details unresolved**; D5e approved subject to pre-launch
  legal review. *Remaining open items block: terms/privacy publication, retention
  jobs, `DELETE /v1/me`, public collection.* (OPEN-DECISIONS.md §2.)
- **6.0c** Settle D8a–D8c (Admin packaging, web session flow, bootstrap).
  ✅ **Decisions recorded 2026-10-21** — D8a in-app Android Admin (no separate APK);
  D8b BFF session architecture **approved conditionally** (the zero-cost BFF
  hosting/runtime proof below remains a hard pre-implementation gate; do not silently
  substitute another architecture); D8c console bootstrap/recovery with MFA-protected
  operator access. (OPEN-DECISIONS.md §3.)
- **6.0d** Provision the chosen Free-tier backend under a **separate explicit owner
  instruction**: project, database, auth provider config, region per D5c **including
  its verification list** (availability, residency, transfers, DPA, subprocessors —
  executable via the final C4 runbook in
  [C4-PREPROVISIONING-CHECKLIST.md](C4-PREPROVISIONING-CHECKLIST.md) §D, after
  D5b/Q1 is resolved), secrets in server-side secrets only, kill switch, export
  target. No billing path
  enabled. ⬜ task NOT AUTHORIZED/NOT COMPLETE; owner-created project
  EXISTS via external ChatGPT integration verification (C4 §H), not
  completion of this task. **Reconciled 2026-10-21 (later session): the owner
  chose the approved cloud architecture (Cloudflare Workers Free + Supabase
  Free `eu-central-1`, strict 0 CZK) as the intended deployment path instead of
  the temporary local-only Option C — Option C is no longer the intended path,
  only the operational hold (existing Supabase project externally verified; C4 §H). The 2026-10-21
  gate-ordering analysis and the future provisioning/verification runbook are in
  [CLOUD-READINESS-PLAN.md](CLOUD-READINESS-PLAN.md); a scoped
  verification-only provisioning step (if the owner chooses ORD-A) is a distinct,
  separately authorized act — 6.0d production setup still follows 6.0f
  acceptance and its own day-of C4 run. D5b stays blocking under every option.**
- **6.0e** Configure Google OAuth console (redirect/origin allowlists, client IDs)
  per AUTH.md — separately authorized, real credentials never committed. ⬜ not
  started.
- **6.0f** **D8b pre-implementation gate:** prove the selected BFF hosting/runtime
  fits the permanent 0 CZK/month requirement including actual Free-tier limitations.
  If unprovable, report the blocker and request an owner decision — do not replace
  the approved architecture silently. 🚧 **Feasibility study executed 2026-10-21
  ([BFF-FEASIBILITY.md](BFF-FEASIBILITY.md)) → CONDITIONAL PASS:** a Cloudflare
  Workers Free Worker (static assets + `/v1` BFF on one origin) + Supabase Free data
  plane fits 0 CZK with fail-closed exhaustion; a Supabase-only same-origin host is
  blocked (Edge Functions rewrite HTML without a custom domain; custom domains are a
  paid add-on). Gate stays **unchecked** until conditions C1–C4 close: owner decision
  on the Cloudflare host, a measured CPU-budget spike (≤10 ms/invocation), the
  D5b/D5c edge-location review and terms re-verification before provisioning.
  **C1 APPROVED 2026-10-21** (architecture-level; no provisioning authorized).
  **C2 approach approved 2026-10-21 for planning only** — the isolated local
  CPU/auth spike is fully specified in [C2-SPIKE-PLAN.md](C2-SPIKE-PLAN.md)
  (`workerd`/Wrangler local mode, synthetic identities, simulated Supabase/JWKS;
  P50≤3 ms / P95≤5 ms / P99≤8 ms provisional targets vs the 10 ms Workers Free
  ceiling; full T1–T5/T15/T20 security matrix). **Spike EXECUTED 2026-10-21 on
  `spike/goldberg-0.11-c2-bff` (separate owner authorization; isolated local
  `workerd`, synthetic identities only, nothing deployed) → CONDITIONAL
  PASS:** security **19/19**, performance P1–P7 executed, kernel-measured
  average CPU **2.03 ms/invocation**; per-invocation production CPU remains
  **unverified** and the §12 production-runtime verification is still
  owner-gated (report: `spikes/bff-c2/results/SUMMARY.md`, final commit
  `7bc6976` on that branch). **C3 DOCUMENTARY REVIEW COMPLETE 2026-10-21**
  ([C3-EDGE-PRIVACY-REVIEW.md](C3-EDGE-PRIVACY-REVIEW.md)): conditional
  viability at 0 CZK, no compliance claim, legal/operator questions open.
  C4 (terms re-verification before provisioning) remains open; **6.0f stays
  unchecked.** **Gate-ordering analysis added 2026-10-21 (later session):** the
  apparent 6.0f ↔ provisioning circle (§12 production-runtime measurements need
  a deployed Worker; provisioning is owner-gated on C4 + D5b) is analyzed in
  [CLOUD-READINESS-PLAN.md](CLOUD-READINESS-PLAN.md) §§2–4 — proposed explicit
  minimal sequence (documentary verification → contractual readiness → owner
  authorization for *limited private verification* provisioning → day-of C4 →
  isolated synthetic-data production-runtime measurement → 6.0f acceptance/
  rejection → separate implementation authorization → later launch gates),
  with alternatives ORD-A/ORD-B/ORD-C **left pending as an owner decision**. No
  gate was changed, checked or bypassed by this analysis.**
  **ORD-A APPROVED 2026-10-21 (sequencing only):** the owner selected **ORD-A —
  Scoped Private Cloud Verification Provisioning** as the gate-ordering
  strategy (ORD-B/ORD-C not selected). This approves the *ordering* of future
  steps (CLOUD-READINESS-PLAN.md §3 steps 1–11) — it is **not** acceptance of
  6.0f, **not** authorization to create accounts, provision, deploy or
  implement. 6.0f stays **unchecked**; Q1/D5b, C2 §12 and C4 stay open; the
  ORD-A readiness checklist (CLOUD-READINESS-PLAN.md §5) must be fully green
  before any CR-5 provisioning under a separate phase-scoped instruction.
- **Q1/C4 research deliverables prepared 2026-10-21 (documentation-only, in
  support of 6.0b/6.0d):** [Q1-OPERATOR-READINESS.md](Q1-OPERATOR-READINESS.md)
  — verified contractual/capacity findings (neither provider publishes a minimum
  account age for a first account; authority-to-bind warranties and Cloudflare
  SSSA §2.2.1(a); Czech Civil Code §§30–33 incl. the court-approval question;
  Google age rule), operator **Options A/B/C laid out with NO selection**, and
  the professional-review question set — **D5b stays legally unresolved and
  blocking for 6.0d**. [C4-PREPROVISIONING-CHECKLIST.md](C4-PREPROVISIONING-CHECKLIST.md)
  — dated current-source baseline (CF DPA v6.4 / Supabase DPA v1, ToS versions,
  Free-tier limits incl. Workers Logs → Observability pricing change 2026-12-01,
  Supabase Free log-retention/no-backups facts, subprocessor lists) with
  verified-2026-10-21 vs re-verify-on-provisioning-day columns; **C4 stays OPEN
  (pre-provisioning checklist ≠ final verification)**. No accounts, no
  provisioning, no OAuth, no code.

- [x] 6.0a [ ] 6.0b [x] 6.0c (D8b proof gate 6.0f still open — C2 executed
      CONDITIONAL PASS, C3 documentary review complete; Q1/C4 research docs
      prepared (Q1 reassessed 2026-10-21: options A/B/C/D, candidate Q1-D,
      no selection); open: C2 §12 production verification, final C4 verification,
      D5b; ORD-A gate-ordering strategy APPROVED 2026-10-21 — sequencing only,
      [CLOUD-READINESS-PLAN.md](CLOUD-READINESS-PLAN.md); execution still
      blocked on D5b + phase-scoped authorizations) [ ] 6.0d [ ] 6.0e [ ] 6.0f

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
  handoff/stages updates). No implementation, no provisioning, no cloud/OAuth, no code.
- 2026-10-21 — **Owner architecture decision review completed**; outcomes recorded in
  [OPEN-DECISIONS.md](OPEN-DECISIONS.md) and synchronized across README/ARCHITECTURE/
  AUTH/DATA-MODEL/API-CONTRACTS/THREAT-MODEL/ZERO-COST-OPS/STAGE-CHECKLIST/docs-ai
  (documentation-only commit on this branch). Decision tasks 6.0a/6.0c recorded as
  settled; 6.0b remains partially open (D5a/D5b/D5d details + pre-launch reviews);
  the new 6.0f D8b zero-cost proof gate was added. **No implementation, no
  provisioning, no OAuth configuration, no code, no cloud resources.**
- 2026-10-21 — **D8b zero-cost feasibility study executed (task 6.0f, documentation
  only)** — [BFF-FEASIBILITY.md](BFF-FEASIBILITY.md): verdict **CONDITIONAL PASS**
  (Cloudflare Workers Free single-origin BFF + Supabase Free data plane; Supabase-only
  same-origin hosting blocked at 0 CZK). 6.0f stays **unchecked** pending owner
  decision on the Cloudflare host (C1), measured CPU spike (C2), D5b/D5c edge-location
  review (C3) and terms re-verification (C4). No code, no deployment, no accounts.
- 2026-10-21 — **C1 owner approval recorded; C2 spike planned (documentation and test
  specification only).** C1 **APPROVED**: Cloudflare Workers Free hosts the Web Admin
  static assets + `/v1` BFF on one HTTPS origin; Supabase Free remains the data/auth
  backend (`eu-central-1`); no paid plans/billing/trials/upgrades; architecture level
  only — **no provisioning, deployment or implementation authorized**. C2 approach
  **approved for planning only**: full local test specification at
  [C2-SPIKE-PLAN.md](C2-SPIKE-PLAN.md) (`workerd` via Wrangler local mode, synthetic
  identities, simulated Supabase/JWKS, fake clock, latency/failure injection; CPU vs
  wall-clock methodology; provisional P50≤3/P95≤5/P99≤8 ms targets against the 10 ms
  Workers Free ceiling; S1–S19 security matrix mapped to T1–T5/T15/T20; PASS/
  CONDITIONAL PASS/FAIL/INCONCLUSIVE outcomes; §12 owner-gated production-runtime
  verification gate). **Spike remains UNEXECUTED** — no code, harness, fixtures,
  dependencies or tests exist or were run. 6.0f stays **unchecked** (C2 evidence,
  C3, C4 open).
- 2026-10-21 — **C2 spike EXECUTED (separate owner authorization) → CONDITIONAL
  PASS.** Isolated local run on `spike/goldberg-0.11-c2-bff` (`workerd` via
  Wrangler local mode, loopback-only, synthetic identities, simulated
  Supabase/JWKS; spike code commit `d2ce77b`, final docs commit `7bc6976`;
  spike code was **not** merged into this planning branch or `main`).
  Security **S1–S19: 19/19 passed**; performance **P1–P7 all executed**;
  kernel-measured **average CPU 2.03 ms/invocation** (~4.9× headroom under
  the 10 ms Workers Free ceiling, flat under injected latency). **Not closed:**
  per-invocation production CPU unverified (local upper-bound argument only)
  and the §12 production-runtime verification remains owner-gated. Report:
  `spikes/bff-c2/results/SUMMARY.md`.
- 2026-10-21 — **C3 D5b/D5c edge-privacy review DOCUMENTARY WORK COMPLETE**
  (documentation-only) —
  [C3-EDGE-PRIVACY-REVIEW.md](C3-EDGE-PRIVACY-REVIEW.md): primary-source
  review of Cloudflare (Workers locations, Workers Logs, DPA v6.4,
  subprocessors, DPF/SCCs) and Supabase (Frankfurt residency and its explicit
  limits, DPA v1, SCCs, support/telemetry flows, Free-plan limits) plus
  EU institutional transfer guidance. Verdict: **conditionally viable at
  0 CZK, not a compliance certification**; EU-only edge processing is
  unavailable on Workers Free (Enterprise-only) and is disclosed as a
  constraint; legal/operator questions (controller identity incl. the
  minor-owner capacity question, retention, notice content) remain open and
  are carried to 6.0b/6.0d/6.16 and the pre-launch legal review. **No code,
  no deployment, no accounts, no personal data. 6.0f stays unchecked — open:
  C2 §12 production verification, C4 terms re-verification.**
- 2026-10-21 — **Q1 operator-readiness research + C4 pre-provisioning checklist
  prepared (documentation-only, no accounts).**
  [Q1-OPERATOR-READINESS.md](Q1-OPERATOR-READINESS.md) separates verified facts
  (no published minimum account age on either provider's current terms;
  Cloudflare SSSA §2.2.1(a)/DPA v6.4 and Supabase ToS v2/DPA v1 authority-to-bind
  warranties; Czech Civil Code §§30–33; Google age rule), unresolved questions,
  the professional-review question set and **Options A/B/C without selecting
  any**. **D5b remains legally unresolved and blocking for 6.0d**; no
  identity/contact recorded, no legal conclusions drawn. EUR-Lex GDPR text
  retrieval failed (0 bytes ×2) and is recorded as not-retrieved (C4 X-6).
  [C4-PREPROVISIONING-CHECKLIST.md](C4-PREPROVISIONING-CHECKLIST.md) is a
  **pre-provisioning** baseline with per-item URLs, version/date stamps and
  verified-2026-10-21 vs must-reverify-on-day columns (incl. the dated Workers
  Logs → Observability pricing change of 2026-12-01 and the Supabase Free
  backups discrepancy); **C4 final verification stays OPEN and must still run
  immediately before any separately authorized provisioning.** No checkboxes
  closed by this entry — 6.0b, 6.0d, 6.0e, 6.0f all unchanged.
- 2026-10-21 — **Cloud-readiness reconciliation + gate-ordering plan prepared
  (documentation-only, no accounts).** The owner chose the approved **cloud
  architecture as the intended deployment path** (Cloudflare Workers Free
  same-origin static + `/v1` BFF; Supabase Free `eu-central-1`; strict 0 CZK)
  **instead of the temporary local-only Option C**; Option C is now only the
  current gated posture, not the intended path. The earlier "owner is a minor"
  premise is **reclassified as an unverified assumption — not a verified current
  fact**; no age/identity/family detail is requested or recorded anywhere, and
  **D5b stays legally unresolved and blocking** (account registration ≠
  compliance; eligibility, legal capacity, authority-to-bind and the GDPR
  controller role all still require professional verification). New:
  [CLOUD-READINESS-PLAN.md](CLOUD-READINESS-PLAN.md) — reconciled assumptions;
  the precise 6.0f ↔ provisioning gate-ordering conflict analysis; an explicit
  minimal sequence (documentary → contractual readiness → owner-authorized
  *limited private* verification provisioning → day-of C4 → isolated
  synthetic-data production-runtime C2 verification → 6.0f accept/reject →
  separate implementation authorization → launch gates) with alternatives
  **ORD-A/B/C left pending as an owner decision**; the exact future private-cloud
  verification runbook (CR-0…CR-8: accounts, no-billing verification, Frankfurt
  region, secrets+MFA, CPU/cold-start/Supabase-latency/quota-fail-closed/logging
  measurements, rollback+cleanup, synthetic data only); Free-tier usage
  estimates (0 CZK, no billing method); binding stop/rollback conditions. No
  checkboxes closed by this entry — 6.0b, 6.0d, 6.0e, 6.0f all unchanged; no
  accounts, no provisioning, no OAuth, no code.
- 2026-10-21 — **ORD-A APPROVED by the owner (sequencing only, documentation
  only).** The owner selected **ORD-A — Scoped Private Cloud Verification
  Provisioning** as the approved sequencing strategy for closing 6.0f; **ORD-B
  and ORD-C are NOT selected** (retained as historical alternatives in
  [CLOUD-READINESS-PLAN.md](CLOUD-READINESS-PLAN.md) §4.3). Recorded:
  the approved 11-step sequence (§3: documentary readiness → Q1/D5b resolution
  → acceptable operator arrangement → separate phase-scoped provisioning
  authorization → day-of C4 → minimum private test resources → C2 §12
  synthetic-data production verification → cleanup/credential revocation →
  owner acceptance or rejection of 6.0f → separate implementation
  authorization → pre-launch gates); the reconciled gate table (§4.2: C1
  approved · C2 conditional pass · C2 §12 open · C3 complete · C4 open ·
  Q1/D5b open · ORD-A approved (sequencing only) · 6.0f unchecked · 6.0d/6.0e
  not authorized · Stage 6 implementation not started); the **ORD-A readiness
  checklist** for the future owner decision (§5, aligned with CR-0…CR-8); and
  a concise owner-facing list of the Q1 §4 professional-review questions (§9.1,
  identity-free, routed outside GitHub). **ORD-A approval is NOT acceptance of
  6.0f and NOT authorization to create accounts, provision, deploy, configure
  OAuth or implement anything.** No checkboxes closed by this entry — 6.0b,
  6.0d, 6.0e, 6.0f all unchanged; no accounts, no provisioning, no OAuth, no
  code, no money.
- 2026-10-21 — **Q1 reassessed: natural-person operator readiness + A/B/C/D
  decision matrix (documentation-only, no accounts).**
  [Q1-OPERATOR-READINESS.md](Q1-OPERATOR-READINESS.md) was rewritten after
  fresh source checks (Cloudflare SSSA re-read; Supabase ToS at its new
  canonical `https://supabase.com/terms` URL — the old `/company/terms` path
  now 404s; Czech Civil Code verze 18 eff. 2026-01-01 §§15/18/30–37/420–421;
  Trade Act 455/1991 §2/§3(1)(b); GDPR Art. 2/4/8 wording still from a
  **non-official mirror** — official EUR-Lex retrieval failed 5× total, C4 X-6
  remains ❌). New/changed: a QD-1…QD-10 findings classification; §1.6 Czech
  business-status/trade-licensing; §1.7 product direction **PD-1 (free of
  charge, non-commercial, no monetization approved or implemented)**; option
  sets now **A/B/C/D** with candidate **Q1-D (legally capable natural person
  acting in their own name)**, a full §5.5 matrix and a §5.6 technical
  (non-legal) recommendation to evaluate D first *after* professional review;
  §4 extended with Q4.9–Q4.13; §10 phased GDPR controller readiness.
  **Capacity is treated solely as an eligibility precondition for the eventual
  accountable party — no personal fact requested, recorded or inferred.**
  Synchronized: stage-6 README/OPEN-DECISIONS/CLOUD-READINESS-PLAN/C4
  (SB-P1 URL), STAGE-CHECKLIST, docs/ai/*. **No option selected; D5b remains
  unresolved and blocking for 6.0d; no GDPR compliance claimed; professional
  review still required; ORD-A stays sequencing-only; 6.0f stays unchecked.**
  No checkboxes closed by this entry — 6.0b, 6.0d, 6.0e, 6.0f all unchanged;
  no accounts, no provisioning, no OAuth, no code, no money.
- 2026-10-22 — **Q1 professional-review handoff brief prepared
  (documentation-only, no accounts, no review performed).**
  New: [Q1-PROFESSIONAL-REVIEW-BRIEF.md](Q1-PROFESSIONAL-REVIEW-BRIEF.md) — an
  identity-free routing document for a qualified professional containing (a) all
  **13 questions Q4.1–Q4.13 preserved verbatim** from Q1 §4, each with its
  primary-source references; (b) a neutral Q1-A/B/C/D summary explaining that
  **Q1-D is a candidate for review, not an approved solution** (textual contract
  compatibility ≠ lawfulness — eligibility, trade classification and controller
  determination all remain open); (c) a **verified-law vs unresolved-interpretation
  split**; (d) official guidance fetched 2026-10-22 from the **Czech Data
  Protection Authority (ÚOOÚ)** (Základní příručka §7, Desatero for small
  controllers, consultation-practice note) and the **European Commission**
  (Application of the GDPR; Obligations) — quotes recorded with access dates;
  and (e) the **C-1…C-6 confirmations a qualified professional must return
  before Q1/D5b can be resolved** (eligibility, contract acceptability, controller
  determination, trade classification, pre-collection readiness posture, and an
  explicit list of lawfully available structures — selection always the owner's).
  Official EUR-Lex GDPR text remains unretrieved (5 failures, C4 X-6 ❌) and is
  labeled as such. Synchronized: stage-6 README/OPEN-DECISIONS,
  STAGE-CHECKLIST, docs/ai/*. **No option selected; no legal approval simulated
  or claimed; D5b remains unresolved and blocking for 6.0d; C4 OPEN; 6.0f
  UNCHECKED; 6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing-only; 0 CZK unchanged.**
  No checkboxes closed by this entry — 6.0b, 6.0d, 6.0e, 6.0f all unchanged;
  no accounts, no provisioning, no OAuth, no code, no money.
- 2026-10-22 — **C6 contract-behavior verification spike EXECUTED
  (isolated, local-only, owner-authorized prototype; not production
  implementation).** New: `spikes/contract-c6/` on branch
  `spike/goldberg-0.11-stage-6-contract` (created from this branch's HEAD
  `c22adb1`; **never merged**, planning branch untouched) — a disposable
  in-process simulator of the `/v1` contract (API-CONTRACTS + DATA-MODEL §2–§6)
  with synthetic identities, fake sessions, fake clock, per-run isolated
  SQLite (`node:sqlite`), **zero npm dependencies and zero network** (static
  scan: 17 files, no http/net/dns/fetch surface). Verified THREAT-MODEL [L]
  rows T5, T6, T7, T8, T10 (incl. a 10-trial multi-process concurrent-approve
  race: exactly one commit per trial), T11, T12, T13, T14 (static portion),
  T16, T17 (fault-injection full rollback + idempotent retry), T18 (local
  chain + tamper detection), T22. **Result: 79 tests / 79 pass / 0 fail**
  (first run was 69/79 — 10 root-caused failures preserved in
  `results/SUMMARY.md` §1 failure log; one real server bug fixed: uncaught
  idempotency `HttpError` → 422 response). Evidence — stable links at the
  verified spike commit (the executable directory is absent from this branch):
  [results/SUMMARY.md](https://github.com/stokonn/Goldberg/blob/d2a95d4b83c634aacb4f7f74432f2bd8980cf218/spikes/contract-c6/results/SUMMARY.md),
  [results/raw/tap.txt](https://github.com/stokonn/Goldberg/blob/d2a95d4b83c634aacb4f7f74432f2bd8980cf218/spikes/contract-c6/results/raw/tap.txt),
  [results/raw/run.json](https://github.com/stokonn/Goldberg/blob/d2a95d4b83c634aacb4f7f74432f2bd8980cf218/spikes/contract-c6/results/raw/run.json)
  (executor `tools/execute.mjs`, run
  `2026-10-22T06:40Z`, Node v22.23.1). Blocked/skipped with precise blockers
  (not bypassed): C2 §12 production runtime (needs provisioning = 6.0d, gated
  by D5b/C4/ORD-A steps 4–6), all [I]-venue rows (real Google/Supabase/browser),
  real Postgres isolation proof, tasks 6.1–6.18, CPU re-measurement (C2
  already measured). Contract ambiguities found (5, incl.
  `DELETE /v1/session` idempotency reading and revision-timing) are documented
  in the spike README for implementation-time decisions. C2 reuse: token/
  HTTP-layer security (S1–S19) and CPU results NOT re-tested here.
  **No checkboxes closed by this entry — 6.0b, 6.0d, 6.0e, 6.0f all unchanged;
  6.1–6.18 all remain unchecked; Q1/D5b UNRESOLVED; C4 OPEN; ORD-A
  sequencing-only; no accounts, no provisioning, no OAuth, no merge, no tag,
  no release, 0 CZK/month, Stage 7 untouched.**
- 2026-10-22 — **Cloudflare MCP readiness verification CHECKPOINT #1
  (read-only cloud inspection, no cloud changes).** Under an explicit owner
  instruction, the official **Cloudflare MCP server** (OAuth, **read-only**
  permissions) was verified and used for **read-only inventory** of the
  owner-provisioned Cloudflare account; **the agent created nothing, modified
  nothing and deleted nothing** (all calls were GET; no write scope was
  exercised). Verified facts: **MCP connection WORKS** — `GET /user` → HTTP
  200; `GET /user/tokens/verify` → 401 is **expected** (that endpoint
  validates API tokens, not OAuth bearer sessions — not a defect). Read-only
  evidence: Workers R2 *read* scope present; **billing/subscription endpoints
  refused (403, no OAuth scope covers them)** — **billing state is NOT
  inspectable through this connection, and inaccessible billing is explicitly
  NOT evidence of zero costs** (the 0 CZK posture is enforced by the C4/
  ORD-A gates, not by this limitation). Account inventory (non-identifying;
  no email, account ID or credential recorded): **1 standard account**
  (created 2026-10-22, not suspended, no pro zones); **user-level 2FA
  ENABLED** (account-level 2FA-enforcement flag off — D8c-relevant when
  actionable); **0 zones (no domains); 0 Workers scripts; workers.dev
  subdomain NOT yet created** (404/10007 — auto-created only on the first
  Workers & Pages dashboard visit; not performed); **0 D1 databases; 0 KV
  namespaces; R2 not enabled** (403/10042 — not needed by the approved
  architecture). Comparison with the C1 architecture: capability available
  for a Workers Free verification Worker, **nothing exists yet**, the
  no-custom-domain constraint is trivially satisfied (0 zones), and
  **Supabase remains entirely unverified/unprovisioned from this side**.
  **Standing preserved: the existing account does NOT resolve Q1/D5b** —
  account eligibility, contractual capacity, authority-to-bind and the GDPR
  controller role remain for professional review; **Q1/D5b UNRESOLVED and
  blocking for 6.0d; C4 OPEN; C2 §12 OPEN; 6.0f UNCHECKED; 6.0d/6.0e NOT
  AUTHORIZED; 6.1–6.18 all unchecked; ORD-A sequencing-only; Stage 7
  untouched; Stage 5 remains COMPLETE; 0 CZK/month unchanged.** Checkpoint
  recorded in `docs/ai/{STATE,HANDOFF,VERIFICATION}.md` + GitHub Issue #31.
- 2026-10-22 — **Owner preference recorded: Q1-D preferred operator model
  (documentation-only; a preference, NOT a selection or resolution).** The
  owner instructed: operation **under their own name (Q1-D)** is recorded as
  the **preferred operator model**, **subject to contractual eligibility and
  professional verification**. Binding reading, synchronized across
  [Q1-OPERATOR-READINESS.md](Q1-OPERATOR-READINESS.md) (new PREFERENCE
  ADDENDUM; §6 checkbox annotated, still open; §7 non-actions unchanged),
  [OPEN-DECISIONS.md](OPEN-DECISIONS.md) (D5b row),
  [CLOUD-READINESS-PLAN.md](CLOUD-READINESS-PLAN.md) §4.1, this README,
  STAGE-CHECKLIST.md and `docs/ai/*`: this is the owner's directional
  preference for the eventual arrangement — **not** the completion of the
  operator-structure choice, **not** an approval of Q1-D, and **not** a
  resolution of D5b. **D5b stays legally UNRESOLVED and blocking for 6.0d**
  until the professional review (Q4.1–Q4.13 / C-1…C-6) returns and the owner
  records the outcome; textual contract compatibility ≠ lawfulness; **no
  account may be created**; ORD-A stays sequencing-only; Q1-C remains the
  operative posture (nothing provisioned); **no personal data is requested,
  recorded or implied**. No checkboxes closed by this entry — 6.0b, 6.0d,
  6.0e, 6.0f all unchanged; 6.1–6.18 all unchecked; C4 OPEN; C2 §12 OPEN;
  0 CZK/month; Stage 7 untouched.
- 2026-10-22 — **C6-PG spike EXECUTED: the C6 contract tests verified against
  a real, isolated, LOCAL PostgreSQL instance (owner-authorized local-only
  investigation; not production implementation).** New branch
  `spike/goldberg-0.11-stage-6-contract-pg` (from `spike/goldberg-0.11-stage-6-contract`
  @ `d2a95d4`; **never merged**, planning branch untouched), commits
  [`f7bef8e`](https://github.com/stokonn/Goldberg/tree/f7bef8ed405442349ff6644e969e6f02a966b754/spikes/contract-c6)
  (port) + [`e37ed00`](https://github.com/stokonn/Goldberg/tree/e37ed00b9205ce22e9e8ec494651e900708ee1e3/spikes/contract-c6)
  (evidence). Instance: **PostgreSQL 17.11** via rootless podman, bound to
  **127.0.0.1:54329 only**, trust auth on loopback, default isolation
  **`read committed`** (same default as Supabase's PostgREST pool), synthetic
  data only, per-test throwaway schemas; container stopped/removed after
  evidence capture (recreation commands in the spike README). Approach: the
  same async adapter interface + same server source + same suites on both
  engines (`C6_ENGINE=sqlite|postgres`; SQLite stays zero-dep, the PG venue's
  single new dependency is the `pg` wire client, spike branch only).
  **Results (executed): PostgreSQL venue 86 tests / 86 pass / 0 fail
  (v1–v10); SQLite post-port 79 / 79 / 0 fail + 7 v10-only skips (no
  regression vs the original 79/79); static isolation scan PASS.** v9
  multi-process race: SQLite 10/10 `[201,409]`; PostgreSQL 10/10
  `[201,500]`/`[500,201]` — **the T10 invariant does NOT hold under READ
  COMMITTED.** Key findings (details in
  [results/SUMMARY-PG.md](https://github.com/stokonn/Goldberg/blob/e37ed00b9205ce22e9e8ec494651e900708ee1e3/spikes/contract-c6/results/SUMMARY-PG.md)):
  **F1** faithful decision tx loses the exactly-one-decision guard on READ
  COMMITTED (deterministic double-approve, ISO-1; loser 500 not 409) —
  `SELECT … FOR UPDATE` (ISO-2) and SERIALIZABLE + explicit 40001→409 mapping
  (ISO-3) both restore it, demonstrated as probes only; **F2** rollback
  atomicity identical on PG (v6), but the C6 DDL has **no foreign keys**
  (orphan accepted — task 6.1 DDL gap); **F3** idempotency contract identical,
  concurrent duplicate key → SQLSTATE 23505 must map to replay, not 500;
  **F4** audit chain identical, but in-transaction “read last seq” appends race
  → 23505 (seq must be serialized in production DDL); **F5** SQLite→PG
  differences found: `BEGIN IMMEDIATE` has no PG equivalent, unqualified
  `ON CONFLICT DO UPDATE` column → 42702 (fixed portably), `rowid`/`?`
  translations, **int8 defaults to string in the pg driver** (parsers required
  to keep JSON numbers). Evidence — stable links at the verified spike commit:
  [SUMMARY-PG.md](https://github.com/stokonn/Goldberg/blob/e37ed00b9205ce22e9e8ec494651e900708ee1e3/spikes/contract-c6/results/SUMMARY-PG.md),
  [tap-postgres.txt](https://github.com/stokonn/Goldberg/blob/e37ed00b9205ce22e9e8ec494651e900708ee1e3/spikes/contract-c6/results/pg/raw/tap-postgres.txt),
  [tap-sqlite-postport.txt](https://github.com/stokonn/Goldberg/blob/e37ed00b9205ce22e9e8ec494651e900708ee1e3/spikes/contract-c6/results/pg/raw/tap-sqlite-postport.txt),
  [summary-data.json](https://github.com/stokonn/Goldberg/blob/e37ed00b9205ce22e9e8ec494651e900708ee1e3/spikes/contract-c6/results/pg/summary-data.json),
  [README (scope/blockers)](https://github.com/stokonn/Goldberg/blob/e37ed00b9205ce22e9e8ec494651e900708ee1e3/spikes/contract-c6/README.md).
  **Honest non-claims:** a local instance is **not** Supabase — the
  Supabase-hosted re-proof stays BLOCKED (D5b/Q1 + C4 + ORD-A steps 4–6); no
  production code exists (F1 fixes are probes); no Supabase project, no
  Worker, no billing change, no user data. **No checkboxes closed by this
  entry — 6.0b/6.0d/6.0e/6.0f all unchanged; 6.1–6.18 all remain unchecked
  (F1–F5 are design inputs FOR task 6.1, gated on 6.0f); Q1/D5b UNRESOLVED
  (Q1-D remains owner preference only); C4 OPEN; C2 §12 OPEN; ORD-A
  sequencing-only; 0 CZK/month; Stage 7 untouched.**

- 2026-10-22 — **C4 PRELIMINARY VERIFICATION + documentation reconciliation
  EXECUTED (documentation-only; C4 remains OPEN, no status changed, no
  checkbox closed).** Owner-instructed recheck of the official Cloudflare and
  Supabase pricing, terms, DPA, region and backup documentation against the
  2026-10-21 snapshot (public document fetches only — no console login, no
  MCP/API billing call, no resource created, no payment method inspected or
  changed, no account setting touched), recorded as **§F** of
  [C4-PREPROVISIONING-CHECKLIST.md](C4-PREPROVISIONING-CHECKLIST.md); the
  §A–§C tables and all row statuses left intact. **New facts recorded:**
  **both provider accounts now exist** (owner-side; Cloudflare corroborated by
  read-only MCP checkpoint #1
  ([comment `6343122203`](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6343122203),
  Supabase existence from the owner's statement only — never agent-verified);
  **Cloudflare MCP read-only authentication verified**; **Cloudflare Workers
  Free remains available** (100k req/day, 10 ms CPU, static assets free;
  pricing page "Last updated Oct 2, 2026" unchanged); **Supabase Free remains
  available with stated limits** (500 MB DB / 5 GB egress / 5 GB cached /
  1 GB storage / 50k MAU / 2 projects / 1 GB log ingestion / audit 1 h /
  API+DB logs 1 day / pause after 1 week); **`eu-central-1` remains a
  documented specific region** ("Central EU (Frankfurt)" in Specific regions);
  **automatic backups "Not included" on Free** (official docs guide free-tier
  `db dump` self-export + off-site); **Supabase Free quotas can result in
  service restrictions** (notification → grace period → pause / read-only /
  disabled launches / **402**); **billing and payment-method status NOT yet
  verified** (CF billing refused via MCP = not zero-cost evidence; Supabase
  billing never opened); **subprocessor and transfer reviews remain
  incomplete** (SB-P9 PDF unparsed, SB-P10 TIA not opened, CF-P10 subscribe
  pending, X-2/X-6/X-7 open). **Cloudflare documentation-date diff recheck:**
  SSSA 2025-09-12, Workers pricing 2026-10-02, limits 2026-10-12 (error
  routing 1027 present), DPA v6.4 (2026-04-03), CF subprocessors 2025-10-01 —
  **all unchanged**; CF Website Terms date not re-renderable this session
  (honest gap, remains ⚠ day-of). **Observability pricing announcement
  rechecked:** the 2026-12-01 change is unchanged, and its target page
  (Observability pricing, **upd. 2026-10-15 — new source, not in the 2026-10-21
  set**) now shows **Free = 0.5 GB/day ingestion + 7-day retention, additional
  ingestion unavailable on Free (hard stop), storage not separately metered,
  automatic migration 2026-12-01** → preliminary read that 0 CZK survives the
  change — **must still be re-verified day-of and again for any access
  ≥ 2026-12-01 (§D step 3); CF-P6/CF-P13 stay ⚠ dated.** Supabase terms date
  badge not re-renderable (honest gap; last verified 2026-10-21); Supabase DPA
  v1 (2026-08-01) and subprocessor page dates (2026-10-12 / 2026-06-01)
  unchanged. **Q1-D recorded as the owner's preferred structure subject to
  eligibility verification** (preference only — already at `78fece3`, repeated
  in §F.1; not a selection). **No checkboxes closed: C4 OPEN (§D steps 1–9
  unchecked), 6.0f UNCHECKED, 6.1–6.18 unchecked, Q1/D5b UNRESOLVED and
  blocking, C2 §12 OPEN, 6.0d/6.0e NOT AUTHORIZED, ORD-A sequencing-only, 0
  CZK/month; no provisioning, account change, billing/payment-method change,
  OAuth change, deployment, Stage 7 change; no merge/tag/release.**
  Validation: all five `scripts/check_*.py` PASS on this commit.

- 2026-10-22 — **C6-PG CONCURRENCY-HARDENING spike EXECUTED: both candidate
  F1/F3/F4 mitigations implemented in the isolated spike (experimental, never
  merged) and compared empirically on real PostgreSQL — row locking
  recommended.** Same isolated branch `spike/goldberg-0.11-stage-6-contract-pg`
  (never merged; planning branch untouched), commits
  [`d8e82d5`](https://github.com/stokonn/Goldberg/commit/d8e82d5807b0039e0f799ae3753558e970b74205)
  (code) + [`c21e550`](https://github.com/stokonn/Goldberg/commit/c21e550ceb5ec7a42317a4f2fe02e6d1225a2cab)
  (evidence). The faithful sources (`server.mjs`, `db-pg.mjs`,
  `execute-pg.mjs`, `decide-child.mjs`) and the **original 10/10-deviation
  evidence `results/pg/` remain byte-untouched** (verified by `git status`).
  Implementation (`src/harden.mjs`): an experimental hardened decision endpoint
  (auth gate + envelope/allowlist + kill switch + idempotency + decision
  transaction only) with two strategies over the **same** `runDecision` step
  sequence — **`for-update`** (`SELECT … FOR UPDATE` on the submission row +
  single-row `audit_head` append mutex) and **`serializable`**
  (`BEGIN ISOLATION LEVEL SERIALIZABLE`, faithful read-last-seq audit kept,
  **explicit SQLSTATE handling: bounded retry on 40001/40P01/23505; exhaustion
  maps to 409 — never an unhandled 500**); both share the **F3 store
  hardening** (concurrent duplicate idempotency INSERT → serve the stored
  replay, or faithful 422 on hash mismatch). **Results (executed, PostgreSQL
  17.11, `read committed`): v11 suite 7/7 (37 tests); the repeated
  multi-process barrier race — the mitigation counterpart of v9's 10/10
  deviations — restored `[201,409]` in 20/20 trials PER STRATEGY; for-update
  needed ZERO SQLSTATE retries everywhere (incl. a 20-decision F4 scale run);
  serializable needed 21× `40001` aborts across the 20-trial race and
  **218× `40001`** at 20-way scale (maxAttempts=64), 0 exhausted; audit seqs
  strictly consecutive + chain coherent in every run; deterministic F3 store
  race verified (23505 → stored replay; 1 row; 422 negative). Full executor:
  SQLite regression venue 93 tests / 79 pass / **0 fail** / 14 honest PG-only
  skips (faithful behavior unchanged); PostgreSQL venue **123/123 pass /
  0 fail**; static scan PASS (23 files). **Engineering conclusion:** row
  locking is the recommended production pattern (deterministic, zero retries,
  loser's 409 via the ordinary status guard); serializable is viable but must
  budget retry generously and map exhaustion to 409; a *concurrent* same-key
  decision duplicate answers `[201,409]` while *sequential* duplicates get the
  stored replay — task 6.1 must state this in the API contract. **Honest
  non-claims:** experimental spike only — **not production code, not a
  Supabase run** (Supabase-hosted proof stays BLOCKED: 6.0f + D5b/Q1 + C4 +
  ORD-A); production implementation stays gated on **6.0f (UNCHECKED)**;
  F1–F5 + this comparison are design inputs FOR task 6.1. Evidence — stable
  links at `c21e550`:
  [SUMMARY-HARDENING.md](https://github.com/stokonn/Goldberg/blob/c21e550ceb5ec7a42317a4f2fe02e6d1225a2cab/spikes/contract-c6/results/pg-hardening/SUMMARY-HARDENING.md),
  [tap-postgres-hardening.txt](https://github.com/stokonn/Goldberg/blob/c21e550ceb5ec7a42317a4f2fe02e6d1225a2cab/spikes/contract-c6/results/pg-hardening/raw/tap-postgres-hardening.txt),
  [tap-sqlite-regression.txt](https://github.com/stokonn/Goldberg/blob/c21e550ceb5ec7a42317a4f2fe02e6d1225a2cab/spikes/contract-c6/results/pg-hardening/raw/tap-sqlite-regression.txt),
  [summary-data.json](https://github.com/stokonn/Goldberg/blob/c21e550ceb5ec7a42317a4f2fe02e6d1225a2cab/spikes/contract-c6/results/pg-hardening/summary-data.json),
  [harden.mjs](https://github.com/stokonn/Goldberg/blob/c21e550ceb5ec7a42317a4f2fe02e6d1225a2cab/spikes/contract-c6/src/harden.mjs),
  [v11 suite](https://github.com/stokonn/Goldberg/blob/c21e550ceb5ec7a42317a4f2fe02e6d1225a2cab/spikes/contract-c6/tests/v11-concurrency-hardening.test.mjs).
  Container `c6-pg` stopped/removed after evidence capture. **No checkboxes
  closed: 6.0f UNCHECKED, 6.1–6.18 unchecked, Q1/D5b UNRESOLVED (Q1-D
  preference only), C4 OPEN, C2 §12 OPEN, 6.0d/6.0e NOT AUTHORIZED, ORD-A
  sequencing-only, 0 CZK/month; no provisioning, no billing/OAuth/account
  change, no deployment, Stage 7 untouched; no merge/tag/release.**
  Validation: all five `scripts/check_*.py` PASS on this commit.

- 2026-10-22 — **C4 owner-side console observations RECORDED (documentation
  only; attributed owner observations, never agent-verified; C4 stays OPEN).**
  The owner reported three first-hand console facts, recorded as **§F.5
  (N-11–N-13)** of
  [C4-PREPROVISIONING-CHECKLIST.md](C4-PREPROVISIONING-CHECKLIST.md):
  **(N-11) Cloudflare Workers Free is active with no payment method attached**
  (owner-observed; MCP has no billing scope — N-8 billing non-verification
  unchanged); **(N-12) a Supabase "Personal Free" organization exists with
  zero projects** (owner-observed; **no project was created** — the 2-project
  Free cap is not consumed; organization existing ≠ billing evidence);
  **(N-13) Frankfurt (`eu-central-1`) is selectable in the Supabase
  project-creation form** (owner-observed in-flow — partially informs
  SB-P7/§D step 6 but **does NOT close it**: no agent verification, no
  project, no console evidence stored). **Privacy: no screenshots, console
  exports or account identifiers are stored in GitHub.** Supabase
  billing/payment status remains unverified; project creation remains
  unperformed and unauthorized (6.0d NOT AUTHORIZED). **No checkboxes closed:
  C4 OPEN (§D steps 1–9 unchecked), 6.0f UNCHECKED, 6.1–6.18 unchecked,
  Q1/D5b UNRESOLVED and blocking, C2 §12 OPEN, 6.0d/6.0e NOT AUTHORIZED, ORD-A
  sequencing-only, 0 CZK/month; no agent console login, no billing/API call,
  no resource created or modified, no Stage 7 change.**
  Validation: all five `scripts/check_*.py` PASS on this commit.

created by stokonn with love <3


## Continuation checkpoint — 2026-10-22 (Stage 6 documentation only)

Purpose/scope: recover interrupted Issue sync, narrow C4 documentary gaps and
prepare F1–F5 for future 6.1; no production code or database change.
Session checklist/results: [READINESS-CONTINUATION.md](READINESS-CONTINUATION.md).
Design requirements: [POSTGRESQL-DESIGN-REQUIREMENTS.md](POSTGRESQL-DESIGN-REQUIREMENTS.md).
Issue description edit referencing b3edc2a/c21e550/checkpoint 6348121556 explicitly
owner-approved, applied and readback verified. PDF parsed; TIA not retrieved;
official GDPR still blocked. [Session checkpoint posted](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6349066370)
with explicit owner authorization. Verified documentation commit/push now authorized;
Project #31 verified In Progress. No approval gate passed.

**6.0f UNCHECKED; 6.1–6.18 UNCHECKED; Q1/D5b unresolved and blocking; C4 OPEN;
C2 §12 OPEN; 6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing only; 0 CZK/month.**

## Finalization publication checkpoint — 2026-10-22

The owner explicitly authorized this documentation-only finalization. Reviewed
12 Markdown files; all five host scripts re-executed and PASS; local relative
links, unchanged original task checkboxes and preservation of other stage sections
verified; git diff --check PASS. Source documentation commit
[6c40799](https://github.com/stokonn/Goldberg/commit/6c40799fefa331385c394fb621e72a5a62c20423)
was pushed to docs/goldberg-0.11-stage-6-readiness and exact remote SHA verified.
Prepared continuation checkpoint posted unchanged at
[comment 6349066370](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6349066370).
This containing documentation commit records publication evidence; consult the
branch HEAD and the finalization Issue comment for its exact revision.

Issue dashboard editorial draft prepared from the latest body and all 62 comments,
including Stage 7A conditional acceptance 6346899885; all 19 unique links checked
live. Snapshot/proposal/link report stored locally at
/home/stokonn/Projects/Opus/.gradle/issue31-cleanup/. No description replacement
until owner approval of the final draft; re-fetch immediately before any approved
write and stop on unrelated edits. Original comments remain untouched.

All technical gates unchanged: Q1/D5b unresolved/blocking; C4 OPEN; C2 §12 OPEN;
6.0f UNCHECKED; 6.1–6.18 UNCHECKED; 6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing
only; 0 CZK/month. Stage 7A conditional/X10 OPEN, 7B not started (latest separate
Stage 7 branch); no Stage 7 code or evidence modified. No Gradle, APK, screenshots,
emulator, hosted-runtime or new spike execution. No merge/tag/release/provisioning.

## Issue #31 dashboard baseline — published and verified 2026-10-22

Owner approved the exact editorial draft; only the Issue description was replaced.
[Current dashboard](https://github.com/stokonn/Goldberg/issues/31), verified at
2026-10-22T19:23:54Z: **8,925 Unicode characters, 52 lines**, down from 25,489
characters/136 lines (64.98% character reduction). Exact UTF-8 body SHA-256:
`c6fabfe7efefa331472fab9e1ba70ea4418bafe6dfedaa02e644e34c138318b7`.
Pre-write body matched the reviewed baseline; post-write body matched the approved
draft exactly. All 63 full comment records unchanged; no new comments. Labels,
milestone 4, title, state, assignees and lock state unchanged; Goldberg Development
item remains In Progress. This supersedes earlier editorial approval-pending notes.

For future edits, fetch current body/comments and compare with this baseline;
review unrelated changes before writing, preserve evidence and update the baseline
after verified publication. The Issue is current status/navigation, repository
docs hold specifications, and comments remain chronological history.

Technical gates unchanged: Q1/D5b unresolved/blocking; C4 and C2 §12 OPEN; 6.0f
and 6.1–6.18 UNCHECKED; 6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing only;
0 CZK/month. Stage 7A conditional/X10 OPEN; 7B not started. No new implementation,
merge, deployment, release or application changes. Next product actions remain
owner-routed Q1 review and owner-performed X10; no physical-device work by agents.

## Security & Auth readiness audit — 2026-10-22 (documentation-only)

Purpose/scope: evaluate owner-supplied advisor/catalog findings without treating
broad grants as proof of exploitation; prepare the existing-project read-only
Auth/RLS/API audit. No production code, database, migration or configuration change.
Tasks, progress, conditional remediation and verification are in
[SECURITY-AUTH-READINESS-AUDIT.md](SECURITY-AUTH-READINESS-AUDIT.md). Public-source
review is complete; hosted metadata checklist remains unexecuted. No 6.0d/6.0f,
Q1/D5b, C4, C2 §12 or 6.1–6.18 completion is implied. Owner approved one concise
C4 dashboard sentence, now published and exact readback verified; all 63 comments
and Issue/Project metadata preserved. No new comments. 0 CZK/month preserved.

### External security audit continuation — 2026-10-22 (base bac81fb)

Purpose/scope: reconcile the executed, read-only A1–A3 catalog evidence in Stage 6
documentation only. ChatGPT’s connected Supabase integration executed all three
successfully; this agent records owner-relayed external results, not its own SQL.

- [x] Record function OID 17603, plpgsql/definer/postgres/event_trigger, pg_catalog search path and effective API-role EXECUTE.
- [x] Record matching ensure_rls binding/event/tags/normal enabled mode and the supplied limited API-role/privileged owner capabilities.
- [x] Record default PUBLIC EXECUTE, no identified separate API-role grants and partial structural source checks with their limits.
- [ ] Complete inherited-access/other DDL paths, full private source/dependency review and sanitized advisor IDs/version.
- [ ] Complete read-only Auth/RLS/API configuration and billing/payment evidence review.

Evidence: [SECURITY-AUTH-READINESS-AUDIT.md](SECURITY-AUTH-READINESS-AUDIT.md)
§1/§3/§7. Both warnings remain; no exploit demonstrated/repaired, no hosted write
or function/RPC invocation. A1–A3 are complete externally; the overall hosted audit
is INCOMPLETE. Existing five host checks and documentation consistency checks PASS
for this continuation (details in docs/ai/VERIFICATION.md); no concurrency rerun.
Q1/D5b unresolved/blocking; C4 and C2 §12 OPEN; 6.0f/6.1–6.18 UNCHECKED;
6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing only; 0 CZK/month; collection NOT
AUTHORIZED. Keep ensure_rls; any permissions change requires separate authorization
and compatibility review. No Stage 7 or application changes; no gate acceptance.

### Additional external security inventories — 2026-10-22 (base 9fe83db)

Purpose/scope: reconcile four executed catalog-only findings in Stage 6 audit and
handoff documentation. ChatGPT executed these against the existing Supabase
project; the owner relayed the results. This agent did not execute hosted queries.

- [x] Record public function inventory: only rls_auto_enable().
- [x] Record direct anon/authenticated memberships: none returned.
- [x] Record function dependency count: 1, without assuming identity/type/direction.
- [x] Record public tables/views/materialized views/foreign tables: zero objects.
- [ ] Finish full private source/dependency and remaining cross-schema/DDL-path review; obtain sanitized advisor IDs/version.
- [ ] Finish read-only Auth/API/RLS configuration and billing/payment/C4 evidence review.

Progress/evidence: [security audit §1/§3/§8](SECURITY-AUTH-READINESS-AUDIT.md).
Only these scoped inventory portions are complete; broader hosted audit remains
INCOMPLETE. Neither warning is resolved; no vulnerability demonstrated/repaired,
no database writes or application-function invocation. Do not repeat A1–A3 or the
four completed inventories. Issue #31’s concise broader status remains accurate;
no edit/comment is needed. Documentation verification is recorded in docs/ai/VERIFICATION.md.

Q1/D5b unresolved/blocking; C4 and C2 §12 OPEN; 6.0f/6.1–6.18 UNCHECKED;
6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing only; 0 CZK/month; collection NOT
AUTHORIZED. Keep ensure_rls; future permissions changes require separate approval
and compatibility review. No Stage 7 or application changes; no gate acceptance.

### Live security evidence / bounded readiness — 2026-10-22 (base 31e0539)

Purpose/scope: reconcile five new external ChatGPT/Supabase read-only findings in
Stage 6 documentation; narrow remaining work to settings evidence and approval
choices, without continuing open-ended catalog exploration.

- [x] Record normal ensure_rls → rls_auto_enable dependency and function language/schema dependencies.
- [x] Record CREATE=false for anon/authenticated across all eight supplied schemas.
- [x] Record private deployed-source retrieval and structural checks, including exception handling, with safety limits.
- [x] Record ensure_rls plus six reported Supabase-related system triggers, unchanged.
- [x] Record both exact advisor names at WARN, unresolved after external rerun.
- [x] Check accessible integration tools; no callable Supabase tools here, Auth/API settings UNVERIFIED.
- [ ] Complete one sanitized Auth/API settings pass where access permits; retain inaccessible fields as limitations for scoped owner review.

[Audit §1/§3/§9](SECURITY-AUTH-READINESS-AUDIT.md) records exact provenance,
remaining settings and prioritized readiness verdict. Catalog reconciliation of
this snapshot is complete; the broader hosted audit/Stage 6 are NOT accepted.
No completed A1–A3/dependency/schema/inventory queries or concurrency tests rerun.
Documentation verification results are in docs/ai/VERIFICATION.md.

Q1/D5b unresolved/blocking; C4/C2 §12 OPEN; 6.0f/6.1–6.18 UNCHECKED;
6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing only; 0 CZK/month; collection NOT
AUTHORIZED. Prioritize owner/professional Q1, C4 evidence, separately authorized
C2 §12/6.0f acceptance, then explicit implementation authorization. Both warnings
remain; preserve ensure_rls. No hosted/production/Stage 7 changes. Issue #31’s
concise broader status remains accurate and unchanged; no edit/comment proposed.

### Owner-authorized local schema prototype — 2026-10-22

Purpose: prove physical PostgreSQL 17 schema/migration/integrity requirements using
synthetic data on a separately named spike. Scope: spikes/stage6-schema-pg and
Stage 6 checkpoints only; no production implementation or adoption into 6.1.

- [x] Create isolated spike/goldberg-0.11-stage-6-schema-pg from verified remote 4d01489.
- [x] Implement physical DDL, FK/unique/check graph, immutable histories, audit_head, idempotency and repeatable atomic migrations.
- [x] Execute disposable PostgreSQL 17.11 targeted suite: 34/34 PASS, 0 failures/skips.
- [x] Run five existing host scripts: PASS; preserve failed intermediate evidence and original PG comparison.
- [x] Flag API revision-eligible statuses versus server transitions; no approved contract changed.
- [x] Publish implementation 16bfdfb/correction f91a4ce and [Issue checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6351050432); exact readback PASS.

[Prototype checkpoint](LOCAL-SCHEMA-PROTOTYPE.md) links source, commands, evidence
and limitations. Verification includes empty/seeded/idempotent migrations, FKs,
immutable mutation/truncation, transactional rollback, new target-contention and
idempotency regressions, audit recomputation and int8 boundaries. No benchmark
rerun; no hosted RLS/Auth/API, full restore/RPO, app build/device or release proof.

This local milestone can later supply reviewed 6.1 inputs; it does not pass or
change 6.0f. Formal 6.1–6.18 remain UNCHECKED; Q1/D5b unresolved/blocking, C4/C2 §12
OPEN; 6.0d/6.0e NOT AUTHORIZED; 0 CZK/month; collection NOT AUTHORIZED.
Exact next step: review candidate schema, resolve status-contract ambiguity with
owner, then clear gates/obtain separate formal implementation authorization.
Stage 7A conditional/X10 OPEN and Stage 7B untouched. No merge/deploy/release.

Publication: implementation commit 16bfdfbe213ce66973a55d9746453601605e74d7;
follow-up f91a4ced846ad53a571c744c6c588f245fe3a582 corrects whitespace in saved
failed-run diagnostics and records the check failure. Tested source hashes and
34/34 results are unchanged; full baseline-to-current diff check PASS. Existing
Issue #31 checkpoint 6351050432 independently read back exactly; all 63 older
comments preserved, 64 total, Project In Progress unchanged. Later publication
metadata/checkpoint commits do not change tested prototype source.

### Reviewed prototype / formal-local adoption preparation — 2026-10-22

Purpose/scope: record owner-selected revision states, inspect dependencies and
prepare an exact local 6.1–6.3 scope/policy proposal reusing the tested prototype.
Documentation/proposals only; formal implementation remains NOT STARTED.

- [x] Publish only the owner-approved Stage 6 dashboard row and checkpoint link; exact body/metadata/comments readback PASS.
- [x] Record owner direction: draft local-only, changes_requested canonical, returned UI label only.
- [x] Inspect app, Stage 7A, prototype and old C6 dependencies; no approved API edit.
- [x] Prepare exact adoption scope/map, API patch and narrow gate-exception text for review.
- [ ] Owner approves the reviewed API patch and local-only formal 6.1–6.3 policy exception.
- [ ] After that authorization: perform extraction/adoption and targeted local Auth/AuthZ implementation/tests.

[LOCAL-CORE-ADOPTION.md](LOCAL-CORE-ADOPTION.md) holds concrete scope, compatibility
impact, retained 34/34 evidence and acceptance requirements. No tests claimed rerun
for unchanged prototype source. Local-only exception is proposed, NOT in force;
all original technical task/gate boxes unchanged. Verification: five host scripts,
source hashes, proposal applicability and links/scope/gate checks recorded in docs/ai/VERIFICATION.md.
Q1/D5b unresolved/blocking; C4/C2 §12 OPEN; 6.0f/6.1–6.18 UNCHECKED;
6.0d/6.0e NOT AUTHORIZED; 0 CZK/month; collection NOT AUTHORIZED. Stage 7A/X10
and Stage 7B preserved. No cloud, production code, API change, merge or release.

### Local Auth/session/authorization prototype — 2026-10-22

Purpose/scope: owner-authorized isolated LOCAL-ONLY prototype for 6.2/6.3 designs,
reusing the verified PostgreSQL schema. New spikes/stage6-auth-local code, additive
Auth migration, tests and Stage 6 checkpoints; no formal promotion or cloud work.

- [x] Create spike/goldberg-0.11-stage-6-auth-local from verified schema remote 0f966dc.
- [x] Implement real JWT/JWKS validation, issuer/subject identity, hashed opaque sessions, nonce/replay/rotation/family revocation and safe logout/error/log handling.
- [x] Implement fresh server role/state/capability/ownership checks, no-self-grant and actor≠author with a local in-process adapter.
- [x] Execute 50 Auth/security tests plus unchanged 34-schema regression suite: 84 PASS, 0 failures/skips/cancellations.
- [x] Run five repository host scripts: PASS; original schema/evidence/approved contracts and gate boxes preserved.
- [x] Commit/push source 76c1c87 and append [Issue checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6351715021); show latest-link-only dashboard proposal for approval.

[Local Auth checkpoint](LOCAL-AUTH-PROTOTYPE.md) links exact source/tests/commands
and partial T1–T9/T22 coverage. Formal 6.2/6.3 and all global task boxes remain
UNCHECKED; no hosted/real-provider/HTTP/RLS/CSRF or complete threat-row pass inferred.
No original concurrency benchmark/provider audit rerun. Remaining work: separately
authorized local 6.4–6.6 with bounded typed handlers, authenticated moderation and
idempotency; canonical revision contract patch awaits review before endpoint adoption.
Q1/D5b unresolved/blocking; C4/C2 §12 OPEN; 6.0f/6.1–6.18 UNCHECKED;
6.0d/6.0e NOT AUTHORIZED; 0 CZK/month; collection NOT AUTHORIZED. No Stage 5/7,
cloud, real credentials/users, APK, deployment, merge, tag or release changes.

Publication: tested source 76c1c879b5642236490e1f14b652fdf5e5ebdc7a pushed to the
separate Auth branch. Checkpoint 6351715021 independently read back exactly; all
64 prior full comments preserved, 65 total, Project In Progress unchanged. Dashboard
link-only proposal is prepared/shown; UNPUBLISHED pending explicit approval.
Source hashes and final 50+34 test artifacts unchanged by publication bookkeeping.

## Owner-authorized local 6.4–6.6 prototype — 2026-10-23

The explicit new owner instruction authorizes only synthetic local handler
implementation before unresolved gates, not formal acceptance or hosted adoption.
[Living purpose/scope/tasks/evidence](LOCAL-HANDLERS-PROTOTYPE.md).
Implemented strict validation, immutable submissions/revisions, private reads,
authenticated atomic moderation and principal/request-bound idempotency on the
unchanged schema/Auth components. PG17.11 final: 87 new + 50 Auth + 34 schema PASS,
zero failed/skipped/cancelled. Original failed-run/source evidence preserved.
The reviewed narrow state patch is applied; no schema/data conversion. Formal
6.4–6.6 remain unchecked; Q1/D5b, C4, C2 §12 and 6.0f unresolved. Cloud, Stage 5/7
and Android unchanged. Next recommendation: separately authorized 6.7–6.9 only.

## Local 6.7–6.9 candidate — PARTIAL, 2026-10-23

Purpose/scope: public overlay/feed, remaining Admin operations and PostgreSQL abuse
protection under spikes/stage6-public-admin-abuse; two narrow existing handler
extensions and additive local migration 004. Branch
implement/goldberg-0.11-stage-6-7-9-local; isolated worktree
/tmp/goldberg-stage6-public-admin-abuse; live GitHub-verified base
262847ebcdef18d186ded5b7af4825436a1dc4e3, direct child of c80c03e.
Source checkpoint is its containing implementation commit; publication follows.

- [x] Read canonical instructions, latest Issue #31 body/66 comments/checkpoint,
  verify live remote ancestry and isolate work without changing earlier branches.
- [x] Add candidate public, Admin and quota services; reuse existing core and
  role/suspension functions; preserve original 171 test files and historical evidence.
- [x] Execute 30 unit checks PASS, five repository checks PASS; single-user PG17.11
  migration SQL and scoped history/feed/quota/rollback smoke assertions PASS.
- [ ] Execute 33 new PostgreSQL integration tests and original 171 regressions.
  Actual attempts: 33 + 87/50/34 hook failures, 0 bodies executed (connect EPERM).
  Separate attempts on the unchanged original worktree fail identically.
- [ ] Verify concurrency, Admin transaction boundaries, end-to-end authorization,
  idempotency, quota windows and all integration assertions in a socket venue.
- [ ] Finish independently scheduled coarse-IP expiry cleanup/retention acceptance.
- [ ] Publish source branch and append a verified partial checkpoint to Issue #31.

Podman fails on read-only /run/user/1000/libpod. A disposable PostgreSQL17.11 cluster
initialized from cached local binaries; TCP startup is denied by the sandbox.
Single-user SQL execution succeeds but cannot prove JavaScript service or concurrent
behavior. Full source hashes, commands, failed attempts and exact threat limitations:
spikes/stage6-public-admin-abuse/results/SUMMARY.md. Original 32-source manifest:
30 unchanged; only adapter and publication revision allocation intentionally changed.
No old hash/log overwritten. Local retention currently prunes only during fresh
intake; real source/field/license policy stays unresolved/default-deny. No fetcher.
T14/T16/T17/T18/T20/T22 subchecks only; no whole threat row acceptance.

Formal 6.1–6.18/6.0f unchanged; Q1/D5b/C4/C2 §12 open. No hosted, real-user,
Android/Stage7/Stage5, main merge/tag/APK/release action. Not ready for 6.10–6.11.
Next: obtain an allowed local PostgreSQL17 socket/container venue, execute
run-local.sh and fix/verify actual results, finish retention, then acceptance.
No Issue dashboard diff proposed; description and prior comments remain untouched.
