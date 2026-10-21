# Q1 — Operator readiness: who may lawfully hold the provider accounts (D5b)

**Goldberg 0.11 · Stage 6 · task 6.0b/6.0d support document · documentation-only ·
prepared 2026-10-21 · branch `docs/goldberg-0.11-stage-6-planning`**

> **Status: research and analysis only.** This document does **not** resolve D5b,
> does **not** select an operator, does **not** provide legal advice, does **not**
> claim GDPR compliance or any legal approval, and does **not** authorize
> provisioning. It exists to give the owner and (separately) a qualified
> professional a precise, sourced picture of what is verified, what is unknown,
> and what must be decided before any Cloudflare or Supabase account is created
> (task 6.0d). It complements
> [C3-EDGE-PRIVACY-REVIEW.md](C3-EDGE-PRIVACY-REVIEW.md) §9 Q1 and feeds the
> final C4 gate ([C4-PREPROVISIONING-CHECKLIST.md](C4-PREPROVISIONING-CHECKLIST.md)).

**Standing constraints honored by this document:**

- No identity, contact detail, family detail or credential of any person appears
  here, and none is requested. Nothing here may be published in a place that
  identifies an individual.
- The owner is a minor (fact carried from C3 Q1). That fact alone is stated; no
  legal consequence is *concluded* from it in this document.
- "Account holder", "contract counterparty", "developer", "controller" and
  "operator" are **distinct roles**. Holding an account does not automatically
  make a person the GDPR controller, and being named in a contract does not by
  itself settle the controller question. This document keeps them separate
  throughout.
- Sections are separated as: **verified facts → unresolved questions →
  professional-review questions → options (no selection) → explicit owner
  decisions**.

---

## 1. Verified facts (primary sources, accessed 2026-10-21)

### 1.1 Cloudflare — contract chain and eligibility

| # | Verified fact | Source |
|---|---|---|
| CF-1 | The **Self-Serve Subscription Agreement** (Last Updated 2025-09-12) contains **no explicit minimum-age clause** in its full text. The contracting party is "*you, or the Entity you represent*". | https://www.cloudflare.com/terms/ |
| CF-2 | SSSA **§2.2.1(a)**: the customer warrants it is not signing up "*on behalf of a third party*" as a principal/user of the services (no accounts created for another person's benefit as the customer of record). | https://www.cloudflare.com/terms/ |
| CF-3 | SSSA **§2.3** permits granting third parties access to the account (e.g., developers/contractors) at the **account holder's sole risk** — i.e., an account holder may let someone else *operate* an account they hold. | https://www.cloudflare.com/terms/ |
| CF-4 | SSSA **§6.1** incorporates the **Data Processing Addendum** by reference; **§2.6** governs Free Services; governing law/venue is California, USA. | https://www.cloudflare.com/terms/ |
| CF-5 | The **Cloudflare Website Terms of Service** (effective 2025-08-01) eligibility section states **no age requirement**. | https://www.cloudflare.com/website-terms/ |
| CF-6 | **Cloudflare DPA v6.4** (effective 2026-04-03): "*you warrant that (a) you have full legal authority to bind Customer … If you do not have the legal authority to bind Customer, please do not accept this DPA.*" Customer = **Controller**, Cloudflare = **Processor**; EU SCCs Module Two; DPF relied on as fallback; 30-day subprocessor-change notice with 10-day objection window. | https://www.cloudflare.com/cloudflare-customer-dpa/ |
| CF-7 | Cloudflare docs, *Create account*: a **first account needs only e-mail + password**; **additional Free accounts** require an existing Cloudflare user **≥ 7 days old** with Super Administrator role (max 5 additional accounts). **No minimum age is stated for the first account.** | https://developers.cloudflare.com/fundamentals/account/create-account/ |
| CF-8 | Cloudflare for Students program requires **18+** — this is a **program-specific** eligibility rule, not an account rule. | Cloudflare for Students program page |
| CF-9 | Workers Free: 100,000 requests/day, 10 ms CPU/invocation, static assets free (pricing page updated 2026-10-02); **Workers Logs Free** = 200k events/day, 3-day retention, but **Workers Logs moves to "Cloudflare Observability pricing" beginning 2026-12-01** — a dated change that must be re-checked before any provisioning or launch after that date. | https://developers.cloudflare.com/workers/platform/pricing/ |
| CF-10 | Cloudflare subprocessor list *For Cloudflare services* (Last Updated 2025-10-01): Developer Platform third parties include **Google LLC** (EEA/US/Australia/India) and **Oracle America** (US/EEA/UK/Japan/Australia/Canada/Singapore); e-mail/RSS notification available. | https://www.cloudflare.com/gdpr/subprocessors/cloudflare-services/ |

**Verified negative finding (CF):** no published minimum account age for a *first*
Cloudflare account was found in the SSSA, the Website Terms or the account-creation
docs as fetched on 2026-10-21. The binding hurdles found instead are **capacity/
authority warranties** (CF-2, CF-6), not an age number.

### 1.2 Supabase — contract chain and eligibility

| # | Verified fact | Source |
|---|---|---|
| SB-1 | **Supabase Terms of Service** ("Version 2 — May 6, 2026"; page footer "Last Modified: 1 May 2026") — read in full on 2026-10-21: it contains **no age or eligibility clause anywhere**. | https://supabase.com/company/terms |
| SB-2 | Supabase ToS require the accepter to have "*right, power, and authority to enter … legal authority to bind that organization*". California law; arbitration clause; §5(c) credit/fraud controls. | https://supabase.com/company/terms |
| SB-3 | **Supabase DPA Version 1 — August 1, 2026**: Customer = **controller**, Supabase = **processor**; accepting the Agreement constitutes signing the **SCCs (Module Two, Irish law)**; 30-day subprocessor notice with **5-day** objection window; 48-hour incident notice; Schedule 1 states daily backups "by default" (in tension with the Free plan's "Automatic backups: Not included" — see C4 item SB-P4). | https://supabase.com/legal/customer-resources/data-processing-addendum |
| SB-4 | **Free plan (current pricing page):** $0 — 500 MB DB, 5 GB egress, 5 GB cached egress, 1 GB storage, 50k MAU, 2 active projects, unlimited API requests; projects **paused after 1 week of inactivity**; **Auth audit-log retention = 1 hour** on Free; **API & DB log retention = 1 day** on Free; log ingestion 1 GB; **Automatic backups: Not included**; custom domains paid-only ($10/mo); spend cap not applicable on Free. | https://supabase.com/pricing |
| SB-5 | **Fair Use / overage behavior (Billing FAQ):** exceeding the Free quota first triggers notification + grace period, then **service restrictions** — pausing projects, read-only database, disabling new projects, **HTTP 402 on all API requests**. No silent paid overage on Free; overdue invoices pause projects and downgrade to Free. Credit card is the only payment method (no payment method is needed while staying within Free). | https://supabase.com/docs/guides/platform/billing-faq |
| SB-6 | **Regions:** each project picks one primary region; **specific region `eu-central-1` (Frankfurt)** exists alongside the general "Europe" grouping (which may deploy to non-EU London/Zurich). Supabase itself states region selection is "a data-location control, **not proof of regulatory compliance**". | https://supabase.com/docs/guides/platform/regions |
| SB-7 | Subprocessor list page documents updated **2026-10-12** and **2026-06-01**; e-mail subscription available. The full PDF contents were **not parsed** (C3 Q8, still open → C4). | https://supabase.com/legal/customer-resources/subprocessor-list |

**Verified negative finding (SB):** no age/eligibility clause exists anywhere in
the current Supabase Terms full text. As with Cloudflare, the binding hurdle found
is the **authority-to-bind warranty** (SB-2), not a stated minimum age.

### 1.3 Google — OAuth dependency (feeds task 6.0e)

| # | Verified fact | Source |
|---|---|---|
| GO-1 | Google Terms of Service (effective 2026-07-30): "*If you're under the age required to manage your own Google Account, you must have your parent or legal guardian's permission*" — a parental-permission rule applies to using Google accounts. Whether Google sign-in is even offered to the project's end users, and under which console/age policy, is a **6.0e question**, but the age rule is a verified dependency for any Google OAuth plan. | https://policies.google.com/terms |

### 1.4 Applicable-law capacity framework (Czechia)

Retrieved 2026-10-21 as the official Czech text of **Act No. 89/2012 Coll.,
Civil Code** (zakonyprolidi.cz copy of the authentic text; the Ministry of
Justice's official English PDF failed TLS verification from this environment and
was **not** retrieved):

| # | Verified fact (Czech Civil Code) |
|---|---|
| CZ-1 | **§30** — full legal capacity (plná způsobilost k právním úkonům) is acquired on reaching **18 years**, or earlier by court grant, emancipation (§37, from 16) or marriage. |
| CZ-2 | **§31** — a minor has capacity to the extent of their **development/maturity** (své povaze a schopnostem přiměřeným). |
| CZ-3 | **§32** — for specific legal acts, the **legal representative's consent** (souhlas zákonného zástupce) is required; consent can be limited and withdrawn. |
| CZ-4 | **§33** — consent of the legal representative to a minor **independently operating a business or other gainful activity** (samostatně podnikat ani jinak výdělečně činně jednat) requires **court approval** (přivolení soudu). |
| CZ-5 | **§36** — a minor is never capable alone where the representative would need court approval for the act; **§37** — emancipation possible from 16. |

**What this does and does not establish:** it establishes that, under the
apparently applicable law, the capacity of a minor to accept provider agreements
and to act "independently" in an economic activity is **governed by statutes that
can require representative consent and, for gainful activity, court approval**
(CZ-2–CZ-5). It does **not** establish whether *this specific project* — a free,
non-commercial, open-source-adjacent community backend — constitutes "podnikání /
výdělečná činnost" under §33, nor who the correct counterparty would be. **Those
are precisely the questions for professional review (§4 below).** No conclusion
is drawn here.

### 1.5 GDPR provisions relevant to Q1 (NOT independently retrieved today)

- The provisions that govern Q1 are **GDPR Art. 4(7)** (definition of
  "controller") and **Art. 8** (conditions applicable to a child's consent for
  information society services offered directly to a child, with the member-state
  age floor between 13 and 16) — plus Art. 6/9 bases and Art. 26/28 role
  assignments that the professional review will need to weigh.
- **Honest retrieval note:** two independent attempts to fetch the official
  EUR-Lex text (CELEX:32016R0679, HTML and PDF) from this environment on
  2026-10-21 returned **0 bytes** (blocked/unreachable). Therefore the exact
  wording of those articles was **not independently verified in this session**;
  the article numbers above are pointers for the reviewer, not quoted text.
  Re-retrieving the official text is an action item in
  [C4-PREPROVISIONING-CHECKLIST.md](C4-PREPROVISIONING-CHECKLIST.md) (X-6).
- No statement anywhere in this document claims GDPR compliance, controller
  status, or a lawful basis. C3 already records: free hosting is not proof of
  compliance, and the architecture relies on transfer mechanisms (DPF + SCCs)
  rather than EU-only processing.

---

## 2. Unresolved questions (known-unknowns, explicitly not answered here)

1. **Which law and forum ultimately govern the capacity question** — the Czech
   provisions in §1.4 appear applicable (project/home jurisdiction), but the
   provider contracts select California law and, for the Supabase DPA SCCs,
   Irish law. How these interact for a capacity/consent dispute is unresolved
   and is a professional question, not an assumption.
2. **Whether the project constitutes "independent business/gainful activity"
   under Czech Civil Code §33** (triggering court approval) or is a non-economic
   hobby/open-source activity covered instead by §31/§32 consent logic —
   genuinely open; **not decided here**.
3. **Whether either provider's *actual* account-creation flow applies an age
   check, self-declaration or additional verification** that the published terms
   do not describe — only observable at signup, which is itself gated behind
   6.0d; therefore unverifiable in advance without creating an account
   (forbidden in this task).
4. **Who, as a matter of GDPR law, is the controller** of the community backend
   (the account holder? the person who decides purposes/means? a legal entity? a
   guardian acting for a minor?) — this is D5b itself, deferred by owner decision
   to the pre-launch gate and **still legally unresolved**.
5. **Whether a parent/guardian accepting a contract "on behalf of" the minor's
   activity collides with Cloudflare SSSA §2.2.1(a)** (no sign-up on behalf of a
   third party) — the direction of the representation (guardian as principal vs.
   minor as principal) has contractual consequences that are not resolvable from
   the text alone.
6. **Whether an adult account holder "automatically" becomes the controller** —
   **it does not, and this document does not assume it does.** Account holding,
   contractual capacity and controller status are three separate determinations
   that the professional review must make together.
7. **Any provider-side record/registration obligations of the eventual
   controller** (Art. 30 records, Art. 35 DPIA screening, supervisory-authority
   questions) — carried from C3 Q9, deferred to the pre-launch legal gate.
8. **Whether the community is "directed at" minors** despite D5f's
   general-audience design (C3 Q10) — open.

---

## 3. Why this blocks (gate status — no status changed by this document)

- **D5b: LEGALLY UNRESOLVED / DEFERRED** (pre-launch gate, per owner decision in
  [OPEN-DECISIONS.md](OPEN-DECISIONS.md) §2). C3 Q1 upgraded it to **blocking
  for task 6.0d provisioning**: account creation requires accepting agreements
  whose authority warranties (CF-6, SB-2) cannot honestly be warranted by an
  unknown/undetermined party.
- **6.0d provisioning: ⬜ not started and not authorized** — remains gated on a
  separate explicit owner instruction *and* on Q1/D5b resolution (this document
  resolves neither).
- **6.0e Google OAuth: ⬜ not started** — GO-1 adds an age dependency to review
  before any console configuration.
- **6.16 privacy/terms publication + real-user collection: ⬜ blocked** as before
  (D5a contact, D5b, D5d, published notices) — unchanged.
- **This document changes no checkbox.** C4 final verification stays open; Issue
  #31 stays open; Stage 6 production implementation stays NOT STARTED.

---

## 4. Questions for an appropriate professional/legal assessment

Route these verbatim; they are deliberately neutral and contain no personal
identifying information:

1. Under the applicable law (apparently Czech Civil Code §§30–33), may a minor
   of the project owner's age validly (a) accept Cloudflare's Self-Serve
   Subscription Agreement and Supabase's Terms of Service as the customer of
   record, (b) accept the incorporated DPAs' "full legal authority to bind"
   warranty, and (c) act as GDPR controller of a non-commercial community
   backend — and what consent (§32) or court approval (§33) is required, if any?
2. Is this project plausibly "independent business/gainful activity" requiring
   §33 court approval, or outside that provision? What facts determine the
   answer?
3. If an adult/guardian must be the counterparty, does Cloudflare SSSA
   §2.2.1(a) (no sign-up on behalf of a third party) permit that structure, or
   must the adult be the genuine customer principal (with the minor operating
   under §2.3 third-party-access provisions)?
4. In each structurally permitted option, **who is the GDPR controller**, who is
   the processor, and what instrument (guardianship, mandate/zastoupení,
   employment, entity appointment) legally connects the parties? Does any
   structure require written documentation beyond the provider contracts?
5. If a legal entity is used (Option B): what is the simplest lawful formation
   path in the applicable jurisdiction for this purpose, what are its ongoing
   obligations (registration, accounting, tax — even at 0 revenue), and does it
   change any provider contract terms (entity vs individual customer)?
6. What must be true before any **real user data** is collected under the chosen
   structure: controller identification/contact (D5a), privacy notice content,
   lawful basis, retention (D5d), minors/consent posture (D5f), Art. 30/35
   screening?
7. Do Google's age terms (GO-1) permit the planned Google sign-in for the
   project's end users, and under what age/consent configuration?
8. Which, if any, obligations attach **now** (before launch) versus only upon
   opening the service to real contributors?

**Nothing in this section may be answered by the agent, by the owner alone where
professional judgment is required, or by inference from this document.**

---

## 5. Operator options A / B / C — for the owner's professional review

**No option is selected, recommended-as-decision, or pre-accepted here.** Each is
presented with prerequisites, responsibilities, risks, open legal questions, the
0 CZK impact, the real-user-collection status, and the exact next action. The
choice belongs to the owner **after** professional input.

### Option A — Adult/guardian of record; minor as technical operator

**Shape.** A parent/legal guardian is the contracting customer and (subject to
professional confirmation) the controller of record for both provider accounts;
the minor participates as a technical operator with access granted under
Cloudflare SSSA §2.3-style third-party access, and as a non-controller
contributor to the project.

| Dimension | Content |
|---|---|
| **Prerequisites** | Professional confirmation that the structure satisfies CF-2/CF-6/SB-2 warranties and Czech §§32–33; guardian's genuine authority and willingness; no third-party identity published anywhere. |
| **Responsibilities** | Guardian holds credentials/MFA (D8c), accepts provider contracts and DPAs, is the public operator contact (D5a), answers data-subject requests; minor does day-to-day development. |
| **Risks / open questions** | §2.2.1(a) direction-of-representation question (Q4.3); controller ≠ account holder must be verified (Q4.4); family/identity data must never be published; guardian availability over the project's life; Q4.2 (is guardian-as-customer "signing up on behalf of" the minor?). |
| **0 CZK impact** | **None** — Free tiers unaffected; no paid plan implied. |
| **Real-user collection** | **Remains embargoed** — unchanged; still requires D5a/D5b/D5d/6.16. |
| **Exact next action** | Take Q4.1–Q4.4 to an appropriate professional review; do **not** start account creation. |

### Option B — Legal entity of record (adult-controlled) as customer/controller

**Shape.** An adult-controlled legal entity (formation path per Q4.5) becomes the
customer of record for Cloudflare and Supabase and, subject to professional
confirmation, the controller; the minor's involvement is documented through the
entity's internal arrangement.

| Dimension | Content |
|---|---|
| **Prerequisites** | Lawful, simple, sustainable entity formation confirmed by a professional; entity genuinely controls purposes/means; provider contracts reviewed for entity-customer specifics. |
| **Responsibilities** | Entity holds accounts/contracts/DPA warranties, bears controller duties (notice, retention, rights requests), maintains its own statutory obligations even at zero revenue. |
| **Risks / open questions** | Q4.5 (formation cost/effort could exceed the "0 CZK" spirit — **any fee is an owner decision, never absorbed silently**); ongoing compliance burden; complexity disproportionate to a Free-tier side project; controller-determination still requires confirmation. |
| **0 CZK impact** | Provider side stays Free; **entity formation/maintenance may cost money → potential conflict with the permanent 0 CZK requirement (owner decision, D4a-adjacent).** Flagged, not resolved. |
| **Real-user collection** | **Remains embargoed** — unchanged. |
| **Exact next action** | Ask the professional only: "what is the simplest lawful entity path and its real cost/obligations here?" — before any formation step; no formation initiated by the agent. |

### Option C — Defer provisioning; no accounts until capacity/structure is settled

**Shape.** No provider accounts are created at all until Q4.1–Q4.5 are answered
and the owner picks A or B. Local, offline Stage 6 server-core implementation
(Phase 1 tasks 6.1–6.9 against a local database) may proceed if/when separately
authorized, since it needs no cloud accounts; provisioning-dependent tasks
(6.0d, 6.0e, integration rows of 6.12–6.15) stay parked.

| Dimension | Content |
|---|---|
| **Prerequisites** | None — this is the default posture **today** (nothing provisioned). |
| **Responsibilities** | Keep the collection embargo, keep fail-closed design, keep this register synchronized. |
| **Risks / open questions** | Delays any end-to-end verification of the C1/C2 architecture (C2 §12 production runtime check needs a real Worker); provider terms/pricing/subprocessors continue to move — hence the dated C4 re-verification requirement. |
| **0 CZK impact** | **None.** |
| **Real-user collection** | **Remains embargoed** — unchanged. |
| **Exact next action** | Schedule the professional review; meanwhile re-verify nothing below the C4 list has drifted (documentation-only). |

**Comparison note (factual, non-recommendatory):** A and B differ on *who the
customer/controller is*; C differs on *when anything happens*. All three keep the
0 CZK free-tier architecture (C1) and the collection embargo intact. **Selecting
any option is an explicit owner decision after professional review — the agent
will not select one, and provisioning stays blocked until then.**

---

## 6. Explicit owner decisions required (none made here)

- [ ] **D5b (blocking for 6.0d):** commission the professional assessment in §4
      (questions are ready to forward verbatim; they contain no identifying
      information).
- [ ] **Choose operator structure:** Option A, B or C — only after §4 input.
- [ ] **D5a remains open:** no contact supplied/authorized for publication;
      do not supply identifying details in this public repository or in Issue #31.
- [ ] **Confirm the 0 CZK boundary** against Option B's potential formation
      costs, if that option is considered.
- [ ] **Confirm whether local-only Stage 6 implementation (no accounts) may
      proceed** while Option C is in effect — a separate owner instruction.
- [ ] **Re-affirm the collection embargo** (unchanged): no real user data before
      6.16 evidence.

---

## 7. What this document deliberately does NOT do

- Does not resolve D5b or mark it legally resolved; does not select an operator.
- Does not conclude that the owner is or is not capable of any specific act; does
  not conclude that an adult holder becomes the controller.
- Does not claim GDPR compliance, lawful basis, or legal approval of any kind.
- Does not request or record identity, age, family, contact or credential data.
- Does not authorize provisioning, OAuth configuration, deployment or collection.

---

## 8. Sources (all accessed 2026-10-21)

| # | Document | URL | Note |
|---|---|---|---|
| 1 | Cloudflare Self-Serve Subscription Agreement (Last Updated 2025-09-12) | https://www.cloudflare.com/terms/ | Full text read: no age clause; §2.2.1(a), §2.3, §2.6, §6.1 |
| 2 | Cloudflare Website Terms (effective 2025-08-01) | https://www.cloudflare.com/website-terms/ | Full text read: no age clause |
| 3 | Cloudflare Data Processing Addendum v6.4 (effective 2026-04-03) | https://www.cloudflare.com/cloudflare-customer-dpa/ | Full text read: authority-to-bind warranty |
| 4 | Cloudflare Docs — Create account | https://developers.cloudflare.com/fundamentals/account/create-account/ | First-account flow; ≥7-day rule for *additional* accounts |
| 5 | Cloudflare Workers Pricing (updated 2026-10-02) | https://developers.cloudflare.com/workers/platform/pricing/ | Free limits; Observability-pricing change 2026-12-01 |
| 6 | Cloudflare Workers Limits (updated 2026-10-12) | https://developers.cloudflare.com/workers/platform/limits/ | Fail-closed/open routing, subrequests, vars |
| 7 | Cloudflare Sub-Processors — For Cloudflare services (Last Updated 2025-10-01) | https://www.cloudflare.com/gdpr/subprocessors/cloudflare-services/ | Google/Oracle on Developer Platform; RSS feed |
| 8 | Supabase Terms of Service (Version 2 — May 6, 2026) | https://supabase.com/company/terms | Full text read: no age clause; authority-to-bind |
| 9 | Supabase Data Processing Addendum (Version 1 — August 1, 2026) | https://supabase.com/legal/customer-resources/data-processing-addendum | SCCs Module Two; 5-day objection window |
| 10 | Supabase Pricing (current) | https://supabase.com/pricing | Free limits; backups not included; log retention 1 h/1 day |
| 11 | Supabase Docs — Billing FAQ (Fair Use, Free overage behavior) | https://supabase.com/docs/guides/platform/billing-faq | Restrictions, not paid overage, on Free |
| 12 | Supabase Docs — Available regions | https://supabase.com/docs/guides/platform/regions | Specific `eu-central-1`; "not proof of compliance" |
| 13 | Supabase Subprocessor List page (docs updated 2026-10-12) | https://supabase.com/legal/customer-resources/subprocessor-list | PDF not parsed (C3 Q8) |
| 14 | Google Terms of Service (effective 2026-07-30) | https://policies.google.com/terms | Parental-permission rule for under-age users |
| 15 | Act No. 89/2012 Coll., Czech Civil Code §§30–37 (authentic Czech text) | https://www.zakonyprolidi.cz/1950-140 | Official MoJ English PDF **not** retrieved (TLS failure) |
| 16 | GDPR, CELEX:32016R0679 (Art. 4(7), Art. 8 pointers) | https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:32016R0679 | **Retrieval FAILED 2026-10-21 (0 bytes, 2 attempts)** — re-retrieve: C4 X-6 |

---

## 9. Exact next steps (documentation-safe, in order)

1. Owner forwards §4 questions to an appropriate adult/professional review
   (agent can format them, but cannot perform the review).
2. Professional assessment returns; owner records the D5b outcome in
   [OPEN-DECISIONS.md](OPEN-DECISIONS.md) and chooses Option A/B/C.
3. Only then: separately authorize 6.0d, executing the final C4
   provisioning-day re-verification
   ([C4-PREPROVISIONING-CHECKLIST.md](C4-PREPROVISIONING-CHECKLIST.md)).
4. Collection stays embargoed until 6.16 evidence exists, regardless of option.

---

created by stokonn with love <3
