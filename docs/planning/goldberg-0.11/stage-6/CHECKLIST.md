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
  legally unresolved and blocking for 6.0d**); D5d principle approved
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
      prepared; open: C2 §12 production verification, final C4 verification,
      D5b; cloud-readiness/gate-ordering plan prepared 2026-10-21 —
      [CLOUD-READINESS-PLAN.md](CLOUD-READINESS-PLAN.md), ORD-A/B/C decision
      pending) [ ] 6.0d [ ] 6.0e [ ] 6.0f

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

created by stokonn with love <3
