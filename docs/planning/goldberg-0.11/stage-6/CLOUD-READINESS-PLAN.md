# Stage 6 — Cloud readiness plan: gate ordering + private-cloud verification runbook

**Goldberg 0.11 · Stage 6 · task 6.0f/6.0d support document · documentation and
verification-planning only · prepared 2026-10-21 · branch
`docs/goldberg-0.11-stage-6-planning`**

> **Standing of this document.** This plan **authorizes nothing**. The owner's
> 2026-10-21 instruction that produced it authorizes **documentation and
> verification planning only** — it explicitly does **not** authorize
> provisioning or production implementation. Nothing here creates accounts,
> credentials, deployments, endpoints or code. Every phase in §5 is a **future
> procedure** that runs only under a separate, explicit, phase-scoped owner
> instruction. This document: (a) reconciles the operator's cloud-architecture
> decision with the planning assumptions; (b) proposes an explicit resolution of
> the apparent 6.0f gate-ordering conflict, leaving the gate-policy choice
> **pending as an owner decision**; (c) specifies the exact future private-cloud
> verification procedure (synthetic data only); and (d) records zero-cost
> estimates, stop conditions and rollback. Preserved without change: **C1
> approved · C2 CONDITIONAL PASS (local; production verification pending) · C3
> documentary review complete · C4 final verification OPEN · D5b legally
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
  *current factual posture* — nothing is provisioned and nothing may be until
  the gates in §3/§5 pass. Every planning document that described Option C as
  the default *intended* posture is hereby reconciled: the intended end state is
  the C1 topology; the "no accounts yet" state is a gated phase, not the goal.
- **Q1 Options A/B/C (operator structures) are untouched by this decision.**
  They concern *who* lawfully holds the accounts and acts as controller — a
  question the architecture choice does not answer. No option is selected here.
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

## 3. Proposed sequence (minimal, safe, does not bypass 6.0f)

The sequence distinguishes the eight conceptually separate steps the owner's
instruction requires, so that no step borrows another step's authority:

| # | Step | Nature | Who/what it unblocks | Gate status now |
|---|---|---|---|---|
| S1 | **Documentary verification** | Documentation work | Everything downstream | 🟧 baseline captured 2026-10-21 (C4 pre-provisioning checklist; C3 sources); final day-of run still required at S4 |
| S2 | **Account eligibility & legal/contractual readiness** | Owner action + professional review | S3, S4 | ⬜ **OPEN — D5b unresolved and blocking**; Q1 §4 question set ready to route verbatim |
| S3 | **Owner authorization for limited private provisioning** | Explicit owner decision, phase-scoped | S4/S5 only | ⬜ pending — requires the §4 gate-ordering decision first |
| S4 | **C4 execution immediately before provisioning** | Verification run (day-of, fresh) | S5 | ⬜ OPEN — must run immediately before S5, not weeks before |
| S5 | **Isolated production-runtime C2 verification (synthetic data only)** | Measurement on throwaway private resources (§5 runbook) | 6.0f evidence | ⬜ not started; needs S2+S3+S4 green |
| S6 | **Final 6.0f acceptance or rejection** | Owner decision on evidence | Implementation authorization | ⬜ 6.0f UNCHECKED |
| S7 | **Separate Stage 6 implementation authorization** | Explicit owner instruction | Tasks 6.1–6.18 (incl. production 6.0d setup, 6.0e) | ⬜ NOT STARTED |
| S8 | **Later real-user launch gates** | D5a contact, D5b final, D5d details, 6.16 published notices, [I]-venue rows, export/restore drill, owner authorization | Public collection | ⬜ blocked; **collection stays embargoed** |

**Property:** S5 (the only step that touches real infrastructure) sits behind
S2 (contractual readiness) and S4 (fresh C4), and in front of S6/S7 — 6.0f is
still genuinely satisfied before any implementation begins; no gate is
bypassed, one gate-policy clarification (S3 scope) is made explicit and
owner-decided.

## 4. Gate-ordering alternatives — **pending owner decision**

Nothing in §3 is adopted until the owner picks an option. Presented without
recommendation-in-the-record (MiMo's own view is in the final report, not here):

- **Option ORD-A — Scoped verification provisioning (the §3 sequence).**
  Owner authorizes a *limited, private, verification-only* provisioning phase
  (S3): two Free accounts (or an existing owner-authorized pair), one throwaway
  Worker, one minimal private Supabase project — synthetic data only, deleted
  with recorded evidence after S5. 6.0f closes on the resulting §12 evidence,
  *then* implementation is separately authorized (S7), with production 6.0d
  setup executed then. *Gate-policy change:* creates the S3 category; requires
  explicit owner decision. *Risk if 6.0f rejects:* resources are deleted
  (§6 rollback); sunk cost 0 CZK.
- **Option ORD-B — Close 6.0f on local evidence; defer §12.** Owner explicitly
  downgrades C2 §12 from required-now to deferred-to-integration-venue ([I]
  rows / 6.13). 6.0f could then be accepted on C2-local + C3 + C4 documentary
  evidence, with **zero cloud resources until implementation authorization**.
  *Gate-policy change:* weakens the D8b proof (production CPU/cold-start
  behavior unverified until after implementation begins); residual risk is
  owner-accepted, never agent-assumed.
- **Option ORD-C — Full 6.0d production provisioning before 6.0f closes.**
  Provision the complete intended topology (after S2 + S4), measure, then close
  6.0f. *Gate-policy change:* inverts the pre-implementation intent of 6.0f by
  committing production resources before the architecture proof closes;
  conflates verification provisioning with production provisioning. Legally
  possible, least clean; not recommended by the agent.

**Consequence of no decision:** the posture simply stays frozen — nothing
provisioned, 6.0f unchecked, no accounts. No option is "default".

## 5. Private-cloud verification runbook (future; each phase separately authorized)

**Scope invariant for every phase: synthetic test data only.** Every identity,
row, string and payload is machine-generated or obviously fictional. **No real
user data, no real contributor data, no owner personal data, no real Google
OAuth, no public endpoints, no billing method, no paid feature** — ever, at any
phase below. Each phase ends with a recorded evidence entry; any stop condition
(§6) halts the run.

### CR-0 — Preconditions (all must be true before CR-1 starts)

- [ ] Gate-ordering decision recorded (§4 — one of ORD-A/B/C; ORD-B skips
      CR-2–CR-5 entirely).
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
  source (none exists yet on this branch).
- Does **not** close, weaken or check 6.0f, C2 §12, C4 or D5b; it does **not**
  claim legal/GDPR approval, and does **not** treat account registration as
  compliance.
- Does **not** collect, request or record any personal data, and treats the
  earlier "minor owner" premise as an **unverified assumption** (§1.2).
- Does **not** merge, rebase, tag or release; does **not** touch the Stage 5
  branch or its evidence; does **not** change the GitHub Project status.

## 9. Pending owner decisions (consolidated)

1. **Gate ordering (§4):** ORD-A (scoped verification provisioning — the §3
   sequence), ORD-B (close 6.0f on local evidence, defer §12), or ORD-C (full
   6.0d before 6.0f). Until decided: frozen posture.
2. **D5b (blocking for any provisioning under every option except ORD-B's
   deferral, and for all real-user collection under all options):** route the
   [Q1-OPERATOR-READINESS.md](Q1-OPERATOR-READINESS.md) §4 questions to an
   appropriate professional review; record the outcome; choose the operator
   structure (Q1 Options A/B/C).
3. **Later, phase-scoped (not now):** CR-3 limited-provisioning authorization;
   CR-6 measurement scope sign-off; CR-8 6.0f acceptance/rejection; then the
   separate Stage 6 implementation authorization; then launch gates.

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
