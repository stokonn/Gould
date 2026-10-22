# Goldberg 0.11 — Stage 6 implementation checklist (all tasks UNCHECKED)

Canonical policy: [AGENTS.md](../../../../AGENTS.md) §"Stage checklist documentation".
Every task below stays **unchecked** until its verification evidence actually exists
and is recorded. Planning (this directory) is complete; **no implementation has
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
  enabled. ⬜ not started. **Reconciled 2026-10-21 (later session): the owner
  chose the approved cloud architecture (Cloudflare Workers Free + Supabase
  Free `eu-central-1`, strict 0 CZK) as the intended deployment path instead of
  the temporary local-only Option C — Option C is no longer the intended path,
  only the current gated posture (nothing provisioned). The 2026-10-21
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

created by stokonn with love <3
