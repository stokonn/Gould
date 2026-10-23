# Stage 6 — Cloud readiness plan: gate ordering + private-cloud verification runbook

## Current Q1/D5b outcome — owner report received 2026-10-23

- **Q1-D: SELECTED by the owner** — operation under the owner's own name.
- **Professional-review outcome:** acceptable / no blocking objection reported.
- **Scope:** Q1/D5b operator model, contractual eligibility and relevant
  privacy/protection obligations for this project.
- **Evidence posture:** owner-reported professional confirmation. This agent did
  not perform the review or independently verify its underlying materials.
- **Review date:** not supplied by the owner; unspecified. **Report recorded:
  2026-10-23.** The report date is not asserted to be the professional's review date.
- **D5b: RESOLVED for this reported operator-model/eligibility scope only.** No
  document, signature, legal citation, reviewer identity or individual answer to
  Q4.1–Q4.13/C-1…C-6 is inferred. The owner's explicit reporting instruction
  permits this bounded record without requiring a signed or written review artifact.
- This does not establish general GDPR/legal compliance or discharge unrelated
  privacy, transfer, retention, notice, consent, security or pre-collection duties.
  D5a/D5d/D5e/D5f and specific unanswered operational questions remain separately gated.

**C1 approved; C4 OPEN; ORD-A sequencing approval only; 6.0f UNCHECKED;
cloud provisioning, deployment, OAuth/permission changes and real-user collection
NOT AUTHORIZED.** The permanent goal remains 0 CZK/month, not a verified billing
result. Existing accounts/projects do not change those gates.

This current record supersedes only older Q1-D preference/no-selection and blanket
D5b-unresolved/blocking statements below. Dated research, questions and earlier
observations remain historical evidence; they must not be read as today's decision.

---

## Preserved research and earlier checkpoints (historical; current outcome above governs)

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

**Goldberg 0.11 · Stage 6 · task 6.0f/6.0d support document · documentation and
verification-planning only · prepared 2026-10-21 · branch
`docs/goldberg-0.11-stage-6-planning`**

> Standing of this document. This plan **authorizes nothing**. The owner's
> 2026-10-21 instruction that produced it authorizes **documentation and
> verification planning only** — it explicitly does **not** authorize
> provisioning or production implementation. Nothing here creates accounts,
> credentials, deployments, endpoints or code. Every phase in §5 is a **future
> procedure** that runs only under a separate, explicit, phase-scoped owner
> instruction. This document: (a) reconciles the operator's cloud-architecture
> decision with the planning assumptions; (b) records the **approved ORD-A
> gate-ordering strategy (2026-10-21 — sequencing only)** resolving the apparent
> 6.0f gate-ordering conflict, with the still-open prerequisites and
> phase-scoped authorizations left pending; (c) specifies the exact future
> private-cloud verification procedure (synthetic data only); and (d) records
> zero-cost estimates, stop conditions and rollback. Preserved without change:
> **C1 approved · C2 CONDITIONAL PASS (local; production verification pending)
> · C3 documentary review complete · C4 final verification OPEN · D5b legally
> unresolved · 6.0f UNCHECKED · Stage 5 COMPLETE · Stage 6 implementation NOT
> STARTED · Stages 7–8 pending.**

## 1. Reconciled operator decision (2026-10-21)

### 1.1 Cloud architecture is the intended deployment path

The owner has chosen the **previously approved cloud architecture** as the
intended Stage 6 deployment path, **instead of the temporary local-only Option C**
of [Q1-OPERATOR-READINESS.md](Q1-OPERATOR-READINESS.md) §5:

> Cloudflare Workers Free — same-origin Web Admin static assets + `/v1` BFF API;
> Supabase Free — PostgreSQL and authentication; Supabase region
> **`eu-central-1` (Frankfurt)**; permanent operational budget **0 CZK/month**;
> no paid plans, trials that incur charges, automatic upgrades or unapproved
> billing.

Planning-assumption reconciliation:

- **Option C ("defer provisioning; no accounts until capacity/structure is
  settled") is no longer the intended deployment path.** It remains only the
  operational hold pending the gates in §3/§5. ChatGPT connected integration
  externally verified the existing Supabase project (C4 §H); the older no-resource inventory is superseded, not
  retroactively approved. The intended end state remains the C1 topology. No
  additional provisioning or configuration is authorized by this factual change.
- **Q1 Options A/B/C/D (operator structures) are untouched by this decision.**
  They concern *who* lawfully holds the accounts and acts as controller — a
  question the architecture choice does not answer. No option is selected here.
  (Reassessment 2026-10-21: the set now includes candidate **Q1-D — a legally
  capable natural person acting in their own name** — see
  [Q1-OPERATOR-READINESS.md](Q1-OPERATOR-READINESS.md) §5/§5.5; still no
  selection.)
- This decision is **architecture/deployment-path level only**. It changes no
  gate: provisioning (6.0d), OAuth console work (6.0e), implementation
  (6.1–6.18) and collection (6.16) remain separately owner-gated, and **6.0f
  stays unchecked** (§3, §4).

### 1.2 The "minor owner" assumption is reclassified (D5b framing)

Earlier Stage 6 documents (C3 §9 Q1/Q10, Q1 §1.4, and text derived from them)
carry an assumption that the project owner is a minor. **Per the owner's
2026-10-21 instruction, that assumption must not be treated as a verified
current fact.** Reconciled framing, binding from now on:

- The prior statements are retained as **historical document context**, not as
  verified facts. No current document may cite "the owner is a minor" as an
  established fact; the capacity question is **unverified in either direction**.
- **No age, birth date, personal identity or family details may be requested,
  collected, committed, published or inferred** — in this repository, in Issue
  #31, in provider consoles or anywhere else. This instruction adds nothing to
  the existing data-minimization rules; it narrows, not widens, what may be
  stated.
- **D5b stays legally unresolved and blocking for 6.0d** — unchanged. Reclassifying
  the assumption resolves nothing; it only prevents an unverified premise from
  hardening into fact.

**Reassessment addendum (2026-10-21, same day, later session):** the capacity
question is now framed **purely as an eligibility precondition for the eventual
accountable party** — an abstract requirement to be confirmed by professional
review, never a fact about any specific person. No option among Q1-A/B/C/D is
selected; candidate **Q1-D (a legally capable natural person acting in their
own name)** was added to the option set for evaluation; the product direction
is recorded as free/non-commercial (Q1 §1.7, PD-1). Nothing in this addendum
changes any gate, authorization or the embargo on real-user collection.

### 1.3 Contractual requirements actually at stake (from Q1, unchanged)

The blocking questions for provisioning are **contractual and legal-capacity
questions that must be answered by an accountable party, not assumed either
way** ([Q1-OPERATOR-READINESS.md](Q1-OPERATOR-READINESS.md) §§1–4 — the primary
research, not repeated here):

1. **Account eligibility** — neither provider's current terms publish a minimum
   age for a first account (Q1 CF-1/CF-7, SB-1); the hurdles found are
   **authority warranties**, not age numbers. Actual signup flows may differ
   from published terms (Q1 Q2.3) — observable only at signup, which is gated.
2. **Legal capacity** — the apparently applicable domestic law (Czech Civil
   Code §§30–33, Q1 §1.4) can require representative consent and, for gainful
   activity, court approval; whether it applies to this project is a
   **professional question, not an agent assumption in either direction**.
3. **Authority to accept agreements** — Cloudflare SSSA §2.2.1(a) (no sign-up
   on behalf of a third party) and the DPA v6.4 "full legal authority to bind
   Customer" warranty; Supabase ToS v2 "right, power and authority to bind".
   Account creation requires a counterparty who can honestly give those
   warranties.
4. **GDPR controller role** — controller status is **separate from** account
   holding, contractual capacity and technical operation (Q1 Q2.6). **Account
   registration does not establish compliance and does not settle controller
   status.** D5b (who is the controller, by what legal instrument) remains for
   professional review before provisioning; no GDPR compliance is claimed,
   and free hosting is not proof of compliance (C3, CLOUD.md).

## 2. The apparent 6.0f gate-ordering conflict, stated precisely

- **T1** — Task **6.0f** is defined as a **pre-implementation** gate: prove the
  approved D8b BFF hosting/runtime fits the permanent 0 CZK requirement before
  implementation (CHECKLIST.md 6.0f; OPEN-DECISIONS.md §3 D8b).
- **T2** — C2 executed locally → **CONDITIONAL PASS**; C2 §12 states the
  **production-runtime verification** (a throwaway Worker on the real runtime,
  measured, then deleted) is still required where local results are not
  conclusive — and it is currently **owner-gated** (C2-SPIKE-PLAN.md §12).
- **T3** — A production-runtime measurement requires **deploying a Worker on a
  real Cloudflare account** — an act of provisioning.
- **T4** — Provisioning (task **6.0d**) is separately owner-gated and requires
  the **final C4 verification immediately before it**
  (C4-PREPROVISIONING-CHECKLIST.md: "preparation ≠ final verification"; §D
  runbook), which in turn requires **X-1 (D5b/Q1) resolved** (C4 §D step 1).
- **T5** — Read naively: 6.0f must close before provisioning; C2 §12 can only
  happen after provisioning → **apparent circle**.

**Analysis.** The circle is *apparent, not strictly encoded*: the current text
gates **implementation** on 6.0f, not provisioning — 6.0d is gated on its own
conditions (owner instruction + D5c/C4 verification + D5b), and C2 §12 already
framed the account decision as "a new decision". So a legal reading exists in
which a **limited, private, verification-only provisioning** step precedes 6.0f
closure without violating any written gate. But exercising that reading is
itself a **gate-policy clarification the owner has never authorized** (no
account may be created by anyone today; §12 says so explicitly). Therefore the
resolution below is **proposed, not adopted**, and any branch of it that changes
gate policy is **left pending as an owner decision**.
*(Superseded 2026-10-21, same day, later session: the owner decided — **ORD-A
approved, sequencing only** — see the supersession note at §3 and the decision
record at §4. The analysis above is retained unchanged as the historical
reasoning behind the decision.)*

## 3. Approved sequence (ORD-A — owner-approved sequencing strategy, 2026-10-21)

> **SUPERSESSION NOTE (2026-10-21, ORD-A approval session):** this section was
> originally written as a *proposal with pending alternatives*. The owner has
> now **approved ORD-A** as the sequencing strategy (§4). The sequence below is
> therefore the **approved gate ordering** — while remaining, as before, a
> plan that **authorizes no execution**: every step involving real accounts or
> infrastructure still requires its own separate, phase-scoped owner
> instruction (§5).

The sequence distinguishes the conceptually separate steps so that no step
borrows another step's authority. The owner-approved ordering (recorded
2026-10-21) maps onto the S-table as follows — the **11 owner-approved steps**
are authoritative; S-IDs are the shorthand used elsewhere in this document:

| # | Step (owner-approved sequence) | Shorthand | Nature | Gate status now |
|---|---|---|---|---|
| 1 | Complete documentary readiness | S1 | Documentation work | 🟧 baseline captured 2026-10-21 (C4 pre-provisioning checklist; C3 sources); final day-of run still required at step 5 |
| 2 | Resolve Q1/D5b account eligibility, contractual capacity and controller responsibilities through appropriate review | S2 | Owner action + professional review | ⬜ **OPEN — D5b unresolved and blocking**; Q1 §4 question set ready to route verbatim |
| 3 | Establish a legally and contractually acceptable account/operator arrangement | S2 | Owner action (Q1 Options A/B/C/D — **none selected yet**) | ⬜ OPEN |
| 4 | Obtain separate explicit owner authorization for limited private verification provisioning | S3 | Explicit owner decision, phase-scoped | ⬜ not yet — ORD-A approves the *ordering*, not this authorization |
| 5 | Execute the final C4 terms/pricing/privacy verification immediately before provisioning | S4 | Verification run (day-of, fresh) | ⬜ OPEN — must run immediately before step 6 |
| 6 | Create only the minimum separately authorized private test resources | S5 prep | Minimal private infrastructure (CR-5) | ⬜ NOT AUTHORIZED |
| 7 | Execute C2 §12 production-runtime verification using synthetic data | S5 | Measurement on throwaway private resources (§5 runbook CR-6) | ⬜ not started; needs steps 2–4 green |
| 8 | Remove temporary resources, revoke credentials and verify cleanup | S5 post | Mandatory recorded rollback (CR-7) | ⬜ planned |
| 9 | Evaluate evidence and obtain explicit owner acceptance or rejection of 6.0f | S6 | Owner decision on evidence | ⬜ 6.0f UNCHECKED |
| 10 | Obtain separate Stage 6 implementation authorization | S7 | Explicit owner instruction | ⬜ NOT STARTED |
| 11 | Complete additional legal/privacy/launch gates before collecting any real-user data | S8 | D5a contact, D5b final, D5d details, 6.16 published notices, [I]-venue rows, export/restore drill, owner authorization | ⬜ blocked; **collection stays embargoed** |

**Property:** step 7 (the only step that touches real infrastructure) sits
behind steps 2–4 (contractual readiness + phase-scoped authorization) and step
5 (fresh C4), and in front of steps 9–10 — **6.0f remains a pre-implementation
gate**: it must be genuinely satisfied before any implementation begins; the
narrowly scoped verification provisioning of steps 4–8 may precede its closure
**only after its own prerequisites and a separate authorization**. No gate is
bypassed.

**Permanent constraints attached to the approved sequence:** strict **0
CZK/month**; **no billing methods, paid features, trials that incur charges,
auto-upgrades or chargeable usage** anywhere in any step — a required capability
that is no longer available at 0 CZK is a **blocker report to the owner, never
a silent upgrade** (D8b, C4 X-5).

## 4. Gate-ordering decision — **ORD-A APPROVED (2026-10-21), sequencing only**

> **Owner decision recorded 2026-10-21: ORD-A — Scoped Private Cloud
> Verification Provisioning is APPROVED as the sequencing strategy for closing
> the 6.0f feasibility gate. ORD-B and ORD-C are NOT selected.** This approval
> is **sequencing only** — it authorizes recording the decision, reconciling
> gates and preparing prerequisites (what this document does). It is **NOT
> approval to execute any step involving real provider accounts or
> infrastructure**: no account creation, no provisioning, no deployment, no
> OAuth configuration, no production code, no real-user data collection. 6.0f
> remains a **pre-implementation gate** and stays **UNCHECKED**.

### 4.1 What ORD-A approves (restated for the record)

The §3 table (steps 1–11) is the approved ordering. Key clarifications, all
binding:

- **ORD-A ≠ Q1 Options A/B/C/D.** ORD-A is the *gate-ordering* strategy (when
  things may happen). Q1 Options A/B/C/D are possible *legal/operator
  arrangements* (who holds the accounts and carries controller duties) —
  **none is selected**; that selection belongs to the owner after professional
  review ([Q1-OPERATOR-READINESS.md](Q1-OPERATOR-READINESS.md) §5).
  *(2026-10-22: the owner recorded **Q1-D as the preferred operator model**,
  subject to contractual eligibility and professional verification — a
  preference only; no option is selected, **D5b stays unresolved and
  blocking**, and Q1-C remains the operative posture meanwhile.)*
- **Q1/D5b stays unresolved** and blocks steps 2–6 under every reading: no
  provider account may be created while the contractual and legal prerequisites
  remain unresolved, and creating an account would not resolve D5b or establish
  compliance.
- Verification provisioning under ORD-A is **narrowly scoped** (CR-5: one
  throwaway Worker, one minimal private Supabase project, synthetic data only,
  mandatory recorded deletion) and **never** becomes production by default; the
  production 6.0d setup happens only after step 10's separate authorization.
- The permanent **0 CZK/month** requirement (no billing methods, paid features,
  chargeable trials, auto-upgrades or chargeable usage) applies to every step;
  any 0 CZK failure is an owner blocker report, never a silent upgrade.

### 4.2 Reconciled Stage 6 gate statuses (authoritative, as of 2026-10-21)

| Gate | Status |
|---|---|
| C1 | **APPROVED** — Cloudflare Workers Free + Supabase Free (architecture level) |
| C2 | **CONDITIONAL PASS** — local verification completed (`workerd`, 19/19 security, CPU 2.03 ms/invocation avg) |
| C2 §12 | **OPEN** — real production-runtime verification pending |
| C3 | **DOCUMENTARY REVIEW COMPLETE** — conditional viability at 0 CZK, no compliance claim |
| C4 | **OPEN** — checklist prepared, final verification not executed |
| Q1/D5b | **OPEN** — contractual/legal readiness unresolved |
| ORD-A | **APPROVED — sequencing decision only** |
| 6.0f | **UNCHECKED** |
| 6.0d | **NOT AUTHORIZED** |
| 6.0e | **NOT AUTHORIZED** |
| Stage 6 implementation | **NOT STARTED** |

No technical, legal or privacy gate may be closed without evidence; ORD-A
approval is **not** acceptance of the 6.0f feasibility gate.

### 4.3 Preserved historical alternatives (not selected — retained for the record)

- **Option ORD-B — Close 6.0f on local evidence; defer §12.** Would downgrade
  C2 §12 to deferred-to-[I]-venue; **NOT SELECTED** (2026-10-21).
- **Option ORD-C — Full 6.0d production provisioning before 6.0f closes.**
  Would commit production resources before the architecture proof closes;
  **NOT SELECTED** (2026-10-21).

The original "no decision → frozen posture" consequence is now historical: the
decision exists (ORD-A), and the posture remains frozen **only because steps
2–6 are still open** — no other reason.

## 5. Private-cloud verification runbook (future; each phase separately authorized)

**Scope invariant for every phase: synthetic test data only.** Every identity,
row, string and payload is machine-generated or obviously fictional. **No real
user data, no real contributor data, no owner personal data, no real Google
OAuth, no public endpoints, no billing method, no paid feature** — ever, at any
phase below. Each phase ends with a recorded evidence entry; any stop condition
(§6) halts the run.

### ORD-A readiness checklist for the future owner decision (all must be green before CR-5)

Prepared 2026-10-21 under the approved ORD-A sequencing (§4). This is the
bounded checklist the owner (or a fresh agent on their instruction) verifies
before any limited private verification provisioning is created. **Nothing here
is executed by this document.**

- [ ] Q1/D5b prerequisites **resolved** (professional review returned; operator
      arrangement established per Q1 Options A/B/C/D — none pre-selected).
- [ ] **Confirmed authority to accept provider contracts** by the determined
      counterparty (CF SSSA §2.2.1(a) / DPA v6.4; Supabase ToS v2 authority-to-bind
      warranties) — recorded as a fact, with no identity data in the repository.
- [ ] **Separate explicit owner approval to create temporary resources**
      (distinct from this ORD-A sequencing approval — CR-3).
- [ ] **Day-of C4 verification executed** (CR-1/CR-4) with every ⚠/⬜ item
      current; CF-P6 checked if the date is ≥ 2026-12-01.
- [ ] **Cloudflare Workers Free only** — Free plan confirmed, **no billing
      method, no paid features, no add-ons, no custom domains**.
- [ ] **Supabase Free in `eu-central-1` (Frankfurt)** — the specific region
      selected in the real project-creation flow (not the general "Europe"
      grouping); no billing method; no paid features.
- [ ] **MFA-protected operator access** on both accounts (D8c), recovery codes
      held offline by the owner; no credential ever enters the repository.
- [ ] **Minimal, temporary, protected resources only** — one throwaway Worker
      (verification name, secret-gated, fail-closed), one minimal Supabase
      project (RLS on, synthetic schema only, service-role server-side only);
      target lifetime ≤ 1 week.
- [ ] **Synthetic identities and synthetic data only** — machine-generated or
      obviously fictional; no real users, no real catalogue data copied from
      elsewhere, no owner personal data.
- [ ] **No public service launch or open participation** — the Worker is never
      referenced by any client, no public endpoints are activated, nothing is
      indexed or shared; collection stays embargoed (6.16 gates unchanged).
- [ ] **Measurements planned and executed per CR-6:** CPU per invocation, cold
      starts, Supabase latency, quota/fail-closed behavior (no deliberate
      provider-quota exhaustion), secret-safe minimized logging audit.
- [ ] **Mandatory resource deletion and credential revocation** (CR-7): Worker
      deleted and route verified 404; Supabase project deleted/emptied and
      paused; API tokens revoked; local secrets removed.
- [ ] **Recorded cleanup and verification evidence** in CHECKLIST.md +
      docs/ai/VERIFICATION.md + Issue #31 before 6.0f may be discussed.
- [ ] **Immediate stop on any contractual, financial, privacy or security
      blocker** (§6) — reported to the owner, never worked around.

If any checklist item cannot be satisfied at **0 CZK**, the outcome is a
**blocker report to the owner** — never an upgrade, trial or substitution (D8b).

### CR-0 — Preconditions (all must be true before CR-1 starts)

- [ ] Gate-ordering decision recorded (§4): ✅ **ORD-A approved 2026-10-21 —
      sequencing only; this CR checklist is the ORD-A-aligned readiness gate
      and every item below must be green before CR-5 provisions anything.**
- [ ] D5b/X-1 resolved per [Q1-OPERATOR-READINESS.md](Q1-OPERATOR-READINESS.md)
      §6 (professional review returned; operator structure chosen; the
      counterparty who will accept the provider agreements is determined and
      accountable). **Stop if unresolved** (C4 §D step 1).
- [ ] Explicit owner instruction authorizing the CR phases by name, including
      who performs signup (the determination is theirs to execute — the agent
      never creates accounts, never handles the owner's credentials).
- [ ] C4 baseline items marked ⚠ dated 2026-10-21 are current enough to survive
      the day-of re-run; the **2026-12-01 Workers Logs → Observability pricing
      change (CF-P6)** is checked if the run date is ≥ 2026-12-01.

### CR-1 — Documentary verification (day-of C4 final run)

- [ ] Execute [C4-PREPROVISIONING-CHECKLIST.md](C4-PREPROVISIONING-CHECKLIST.md)
      §D steps 1–6 **on the day**: re-fetch CF-P1…CF-P13, SB-P1…SB-P9, X-2…X-5;
      update versions/dates; investigate every diff; parse the current Supabase
      subprocessor PDF (SB-P9); subscribe to both notification lists; calendar
      the 10-day/5-day objection windows.
- [ ] Confirm the Workers Free CPU ceiling and 100k req/day limit are still the
      applicable published limits (C4 discipline from C2 §14.4).
- [ ] Record results in the C4 document + CHECKLIST.md + Issue #31. **Any
      free/legal failure → blocker report to the owner; never a silent paid
      substitution (D8b, X-5).**

### CR-2 — Account eligibility & contractual readiness (owner-executed)

Executed by the accountable counterparty determined in CR-0, not by the agent:

- [ ] Create (or name an existing) **Cloudflare** account and **Supabase**
      account under the determined structure; **MFA enabled on both immediately**
      (D8c), recovery codes stored offline by the owner, password manager, no
      reused secrets.
- [ ] Record only **non-identifying** facts in the repo: that MFA is on, that
      the plan is Free, that no billing method is attached. **Never** commit
      emails, credentials, token values or recovery codes.
- [ ] Confirm during signup whether either flow demanded age/identity
      verification not present in the published terms (Q1 Q2.3) — record the
      fact, not the identity data.

### CR-3 — Owner authorization for limited private provisioning (explicit, phase-scoped)

- [ ] Owner instruction names the exact scope: **one throwaway Workers Free
      worker** (verification only) + **one minimal Supabase Free project**
      (verification only); everything else in the topology (production DB
      schema, sessions, admin endpoints, OAuth) is **out of scope until S7**.
- [ ] Scope, duration (target: ≤ 1 week) and the mandatory cleanup (CR-7) are
      part of the instruction.

### CR-4 — C4 re-confirmation immediately before provisioning

- [ ] C4 §D step 7: nothing was created in CR-1…CR-3; the C4 run is still
      fresh (same day/next day); the D5b confirmation still stands. **Then and
      only then, provision.**

### CR-5 — Minimal private test infrastructure (synthetic only)

- [ ] **Supabase:** create the project directly in region
      **`eu-central-1 (Frankfurt)`** — not the general "Europe" grouping (SB-P7;
      verify it is selectable in the real flow and record that fact). Verify in
      the console: **Free plan, no billing method, no add-ons, SSL enforced,
      RLS on for every table, service-role key used only server-side.**
      Create only a tiny synthetic schema (e.g. `spike_sessions`,
      `spike_principals`, `spike_grants` with obviously fictional rows) — the
      production DDL of task 6.1 is **not** created here.
- [ ] **Cloudflare:** deploy the **C2 spike BFF** (or its minimally re-hosted
      successor — spike code stays unmerged; the deployed artifact is a
      verification build) as one Worker on a `*.workers.dev` subdomain with a
      **verification-only name**. **Honest limitation (recorded):** workers.dev
      routes are publicly reachable on Free — mitigate, never hide: require a
      shared-secret header (fail-closed 403 without it), serve **only synthetic
      responses**, publish nothing, keep the Worker out of any client config,
      and delete it in CR-7. Observability set **explicitly in wrangler config**
      (CF-P12). Secrets (`SUPABASE_URL`, service-role key, session key) via
      `wrangler secret put` only — never in the repo, never in a log.
- [ ] **API tokens:** create the **minimum-scope** tokens needed, record their
      scope in the checklist, and **revoke them in CR-7**.

### CR-6 — Measurements (all against the private resources above, synthetic data)

1. **CPU per invocation (the §12 evidence).** Reproduce the C2 pipeline on the
   real runtime: `wrangler tail --format json` / Workers Logs `cpuTime` per
   request; ≥ 1,000 measured invocations after warm-up, across restarts; report
   P50/P95/P99/max per run **and** pooled; compare against the **day-of**
   published Workers Free CPU ceiling; any invocation over the ceiling or P99
   in the 8–10 ms band → investigate or FAIL (C2 §8/§9 outcomes apply).
2. **Cold starts.** ≥ 20 fresh deployments/isolate starts after idle; record
   first-request wall time and CPU; compare with C2 P2 local numbers.
3. **Supabase latency.** From the Worker, measure PostgREST round-trips
   (session + capability lookup) to the Frankfurt project at several times of
   day; record wall distribution; confirm CPU stays flat (validity check from
   C2 §7). This is the missing real-region datapoint behind C2 §11.3.
4. **Quota and fail-closed tests.** Exercise the BFF's own controls at low
   volume: per-principal rate limiter, kill switch, and the error mapping of
   402/429/5xx/timeout shapes to the API-CONTRACTS §2 envelopes (real Supabase
   responses where safely producible, injected shapes otherwise — the injection
   harness exists from C2 S14–S16). **Do NOT deliberately exhaust either
   provider's real quotas** (risks project restriction flags and fair-use
   review for no evidentiary gain — provider fail-closed semantics are
   re-verified from current documentation instead, SB-P5/CF-P5). Deliberate
   real-quota exhaustion, if ever wanted, is a separate owner decision.
5. **Minimal/redacted logging.** Verify in an isolated test invocation which
   fields the invocation logs actually contain (C3 Q2 / CF-P12): assert **no
   cookies, no Authorization headers, no tokens, no service-role key** appear;
   confirm retention behaves as documented (Free: 3-day Workers Logs; the
   2026-12-01 Observability change checked per CF-P6 if applicable). Any
   sensitive-field capture → configure/limit or disable, re-test, and if not
   fixable at 0 CZK → stop condition.
6. **Evidence recording.** Raw JSON + SUMMARY table (C2 format) committed to the
   designated spike/verification location on its isolated branch; `docs/ai/VERIFICATION.md`
   and CHECKLIST.md gain executed/not-executed rows; Issue #31 gets the
   checkpoint. Counts are real; nothing is rounded-to-pass.

### CR-7 — Rollback and cleanup (mandatory, recorded, same session as the last measurement)

- [ ] Revoke all API tokens created in CR-5.
- [ ] `wrangler delete` the verification Worker; **verify the route now 404s**;
      record the check.
- [ ] Delete the Supabase verification project (or, if deletion is unavailable,
      empty every table and pause it) — record the console evidence.
- [ ] Remove local secrets/env files; verify no credential-shaped value exists
      in the worktree (`git status` clean; secret scan of any committed logs).
- [ ] Record cleanup evidence in CHECKLIST.md + Issue #31. **If 6.0f later
      rejects, this same section is the decommission plan — nothing is kept
      "just in case".**

### CR-8 — 6.0f acceptance or rejection (owner decision on evidence)

- [ ] Present the §6 evidence + this plan's compliance record to the owner.
- [ ] Owner **accepts** (6.0f may then be checked with the evidence recorded) or
      **rejects** (→ report; redesign options only via a new owner decision;
      **no silent architecture change**, D8b).
- [ ] Only after S6: a **separate** Stage 6 implementation authorization (S7)
      covering tasks 6.1–6.18, production 6.0d setup (which re-runs its own
      day-of C4 + D5c verification list) and, later and separately, 6.0e.
- [ ] Launch gates (S8) remain exactly as OPEN-DECISIONS.md §5 states — nothing
      in this plan moves them.

## 6. Stop and rollback conditions (binding on every CR phase)

**Stop immediately and report to the owner — never work around — if:**

- Any charge, invoice, spend prompt, billing-method requirement for a needed
  capability, paid-plan upsell gate, or trial-with-charges appears on either
  provider; any auto-upgrade path is observed. (Expected per SB-P5/CF-P5:
  quota exhaustion **denies** service; any observed *charging* behavior is a
  finding, not a surprise to absorb.)
- A required capability turns out to need a **paid feature** (e.g. a Free-tier
  change since the 2026-10-21 baseline) → blocker report, **never** a silent
  substitution (D8b).
- Any **contractual/eligibility issue** at signup or during CR-1 (terms changed;
  the determined counterparty cannot honestly accept the agreements; D5b
  structure no longer tenable).
- Any **privacy issue**: personal data requested/collected/logged unintentionally;
  sensitive fields in logs after mitigation attempts; region `eu-central-1` not
  actually selectable; a subprocessor/terms change that invalidates the C3
  transfer analysis.
- Any **failed verification**: CPU ceiling exceeded unexplained, P99 stuck in
  the 8–10 ms band without a clear optimization path, contaminated CPU
  accounting, fail-open security behavior, or secrets in any response/log.
- Any scope doubt (rule: **if in doubt, stop and ask**).

**Rollback** = CR-7 for infrastructure; documentation rollback = revert the
single documentation commit(s) on the planning branch (this branch has never
carried anything else). **Rollback must leave `git status` clean, no cloud
resources, no credentials, and the Android build untouched** — as with the C2
spike (C2 §13), the verification footprint is fully contained and deletable.

## 7. Estimated Free-tier usage and cost (estimates, not measurements)

| Dimension | Expected verification usage | Free-tier cap | Headroom |
|---|---|---|---|
| Cloudflare Worker requests | ~5k–15k total across CR-6 runs | 100,000/**day** | ≥ 6–20× per day |
| Worker CPU/invocation | expected 2–4 ms (local avg 2.03 ms) | 10 ms/invocation | ≥ 2.5× |
| Workers Logs events | ~50k/day during measurement days | 200k/day (3-day retention; CF-P6 dated change) | 4× |
| KV / Durable Objects / R2 / custom domains | **not used at all** | — | — |
| Supabase DB | < 10 MB (synthetic rows only) | 500 MB | 50× |
| Supabase egress | < 0.5 GB over the verification window | 5 GB (+5 GB cached) | 10× |
| Supabase Auth MAU | ≤ ~5 synthetic test users | 50,000 | — |
| Supabase projects | 1 of 2 allowed | 2 | 1 spare |
| **Money** | **0 CZK / $0 — no billing method on either account, no paid plan, no trial, no upgrade** | — | — |

The economic failure mode remains **service denial, never a bill** (S4). If any
measured usage approaches a warning threshold (BFF-FEASIBILITY.md §7 ladder:
5k req/day sustained, etc.), measurements are throttled or stopped — the ladder
is exercised, not stress-tested, at verification time.

## 8. What this document explicitly does NOT do / authorize

- Does **not** authorize provisioning, account creation, OAuth configuration,
  deployment, implementation, or any change to Android/backend production
  source (none exists yet on this branch) — **including under the approved
  ORD-A sequencing: ORD-A approves the ordering of future steps only, not the
  execution of any step involving real accounts or infrastructure.**
- Does **not** close, weaken or check 6.0f, C2 §12, C4 or D5b; it does **not**
  claim legal/GDPR approval, and does **not** treat account registration as
  compliance.
- Does **not** collect, request or record any personal data, and treats the
  earlier "minor owner" premise as an **unverified assumption** (§1.2).
- Does **not** merge, rebase, tag or release; does **not** touch the Stage 5
  branch or its evidence; does **not** change the GitHub Project status.

## 9. Remaining owner actions (consolidated, post-ORD-A)

1. **D5b (the only substantive blocker before steps 4–7):** route the
   [Q1-OPERATOR-READINESS.md](Q1-OPERATOR-READINESS.md) §4 questions to an
   appropriate professional review; record the outcome; choose the operator
   arrangement (Q1 Options A/B/C/D — none selected). Owner-facing summary of
   those questions is in §9.1 below.
2. **Phase-scoped authorizations (later, never implied by ORD-A):** CR-3
   limited-provisioning approval; CR-6 measurement scope sign-off; CR-8 6.0f
   acceptance/rejection; then the separate Stage 6 implementation
   authorization; then the launch gates (step 11).

### 9.1 Q1 §4 questions — concise owner-facing list (route to a qualified professional; no private replies needed in GitHub)

These are the neutral, identity-free questions from
[Q1-OPERATOR-READINESS.md](Q1-OPERATOR-READINESS.md) §4, restated briefly for
routing. They contain and request no personal data; answers should be recorded
in OPEN-DECISIONS.md, not in the public issue. *(Reassessment 2026-10-21: each
question is about the **eligibility and role of whichever party would ultimately
be accountable** — an abstract condition under every option set A/B/C/D — and
requests no fact about any particular person.)*

1. Under each candidate structure (Q1-A/B/C/D), can the eventual accountable
   party validly accept the Cloudflare and Supabase agreements as customer of
   record, give the DPAs' authority-to-bind warranties **for themselves**, and
   act as GDPR controller — and what consent or court approval does that path
   require, if any (apparently Czech Civil Code §§30–37)?
2. Is the planned activity plausibly "independent business/gainful activity"
   requiring representative consent/court approval or trade registration (Czech
   Civil Code §33, Trade Act 455/1991 §2/§3(1)(b)), or outside those provisions
   — in particular for free, non-commercial operation (Q4.10–Q4.12)?
3. If a structure with a different legal representative is chosen (Q1-A/Q1-B),
   does Cloudflare SSSA §2.2.1(a) permit that structure, or must the
   representative be the genuine customer principal (with the technical operator
   under §2.3 third-party access)?
4. In each permitted structure, **who is the GDPR controller**, who is the
   processor, and what legal instrument connects the parties? Does written
   documentation beyond the provider contracts exist/need creating?
5. (If a legal entity is considered) what is the simplest lawful formation
   path, its obligations even at zero revenue, and its cost — any fee is an
   owner decision against the 0 CZK boundary, never absorbed silently.
6. What must be true before any real user data is collected (controller
   identification/contact, privacy notice, lawful basis, retention,
   minors/consent posture, Art. 30/35 screening)?
7. Do Google's age terms permit the planned Google sign-in for end users, and
   under what age/consent configuration (feeds 6.0e review)?
8. Which obligations attach **now** (before launch) versus only on opening the
   service to real contributors?

**The agent cannot perform this review and draws no legal conclusion.**

## 10. Sources

Inherits the dated primary-source sets of
[BFF-FEASIBILITY.md](BFF-FEASIBILITY.md) §2,
[C3-EDGE-PRIVACY-REVIEW.md](C3-EDGE-PRIVACY-REVIEW.md) §12,
[Q1-OPERATOR-READINESS.md](Q1-OPERATOR-READINESS.md) §8 and
[C4-PREPROVISIONING-CHECKLIST.md](C4-PREPROVISIONING-CHECKLIST.md) §E (all
accessed 2026-10-21). This plan adds **no new external claims**; every factual
statement about provider limits, terms and behavior is inherited from those
documents and subject to the same day-of C4 re-verification. The owner decision
reconciled in §1 comes from the owner's 2026-10-21 Stage 6 cloud-readiness
instruction (recorded in `docs/ai/DECISIONS.md` and this repository's progress
log, not in any external source).

---

created by stokonn with love <3

## Local prototype authorization — 2026-10-22

The owner separately authorized isolated local-only synthetic schema prototyping
on `spike/goldberg-0.11-stage-6-schema-pg`. [LOCAL-SCHEMA-PROTOTYPE.md](LOCAL-SCHEMA-PROTOTYPE.md)
records concrete implementation and 34/34 targeted PostgreSQL 17.11 checks. This
is candidate evidence for future 6.1, not formal implementation or a policy waiver.
6.0f remains UNCHECKED; Q1/D5b/C4/C2 §12 gates and all cloud/collection approvals
remain unchanged. No hosted action, production RLS, OAuth, retention policy,
Stage 7 change, merge or release is authorized by this prototype instruction.

## Local-core adoption proposal — 2026-10-22 (NOT AUTHORIZED)

Owner reviewed the local prototype; [LOCAL-CORE-ADOPTION.md](LOCAL-CORE-ADOPTION.md)
now specifies exact local 6.1–6.3 scope and a proposed exception to the current
6.0f-before-implementation ordering for that subset. The exception has NOT been
approved: §§2–4/ORD-A step 10 remain unchanged and formal adoption cannot start.
The draft would keep Q1/D5b/C4/C2 §12/6.0f open and every cloud/collection gate
binding. It does not select ORD-B or authorize deployment. Next action is review
of the concrete approval text, not another general security/planning loop.
