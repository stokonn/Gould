# Q1 — Operator readiness: who may lawfully hold the provider accounts (D5b)

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

**Goldberg 0.11 · Stage 6 · task 6.0b/6.0d support document · documentation-only ·
prepared 2026-10-21 · reassessed 2026-10-21 (later session — Q1/D5b natural-person
reassessment) · branch `docs/goldberg-0.11-stage-6-planning`**

> **Status: research and analysis only.** This document does **not** resolve D5b,
> does **not** select an operator, does **not** provide legal advice, does **not**
> claim GDPR compliance or any legal approval, and does **not** authorize
> provisioning. It exists to give the owner and (separately) a qualified
> professional a precise, sourced picture of what is verified, what is unknown,
> and what must be decided before any Cloudflare or Supabase account is created
> (task 6.0d). It complements
> [C3-EDGE-PRIVACY-REVIEW.md](C3-EDGE-PRIVACY-REVIEW.md) §9 Q1 and feeds the
> final C4 gate ([C4-PREPROVISIONING-CHECKLIST.md](C4-PREPROVISIONING-CHECKLIST.md)).

> **REASSESSMENT NOTE (2026-10-21, later session — Q1/D5b natural-person
> reassessment; documentation and research only).** Per the owner's
> clarification: (1) **The previous personal-capacity assumptions must not be
> treated as established facts** — and no *new* personal fact may replace them.
> Legal and contractual capacity is analyzed here strictly as **an eligibility
> condition to be satisfied by the eventual accountable contracting party**;
> this document neither requests, nor infers, nor stores any age, birth date,
> government identification, family information, address, private contact
> detail or credential of anyone. Earlier statements built on the prior
> assumption remain identifiable as historical context (this note, the earlier
> RECONCILIATION NOTE below, CLOUD-READINESS-PLAN §1.2 and C3's reconciliation
> note govern). (2) A new **Q1-D candidate — legally capable natural-person
> operator acting in their own name** — is evaluated in §5.4: it is a *candidate
> arrangement*, **not an approved or selected decision**. (3) **Product
> direction recorded: Goldberg is currently intended to remain free of charge;
> commercial features may be considered in a future version, but no monetization
> is approved or implemented now** (§1.7). (4) Verified contractual and Czech
> legal sources were **re-verified or newly retrieved** this session (§1.4, §1.6,
> §1.8); the official EUR-Lex GDPR text **still could not be retrieved**
> (§1.5). (5) All gates are preserved unchanged: **D5b UNRESOLVED and blocking
> for 6.0d; 6.0f UNCHECKED; ORD-A APPROVED (sequencing only); no option
> selected.**

> **OWNER PREFERENCE ADDENDUM (2026-10-22 — documentation-only; a preference,
> NOT a resolution, selection or approval).** The owner has recorded **Q1-D —
> operation under the owner's own name, as a legally capable natural person
> acting in their own name — as the PREFERRED operator model**, subject to
> (a) contractual eligibility determination and (b) the professional
> verification of §4 (questions Q4.1–Q4.13; brief
> [Q1-PROFESSIONAL-REVIEW-BRIEF.md](Q1-PROFESSIONAL-REVIEW-BRIEF.md)
> confirmations C-1…C-6). Binding reading: this is the owner's **directional
> preference** for the eventual arrangement — **not** completion of the §6
> "choose operator structure" act, **not** a selection that resolves D5b, and
> **not** an approval of Q1-D. **D5b stays legally UNRESOLVED and blocking for
> 6.0d** until the professional review returns and the owner records the
> outcome; textual contract compatibility ≠ lawfulness; no account may be
> created; ORD-A stays sequencing-only; **no personal data is requested,
> recorded or implied by this preference**; Q1-A/Q1-B remain documented
> fallbacks, and Q1-C remains the operative posture meanwhile (nothing
> provisioned). Recorded per the owner's instruction of 2026-10-22.

> **RECONCILIATION NOTE (2026-10-21, earlier session — no question below is
> answered or changed by it):** (1) The owner chose the approved **cloud
> architecture as the intended deployment path** — Cloudflare Workers Free +
> Supabase Free `eu-central-1` at 0 CZK — **instead of the temporary local-only
> Option C** (§5). Option C is therefore no longer the intended end state (only
> the current gated posture); its table below is retained as historical record.
> This changes **nothing about D5b**: every Q1 question, the professional-review
> set (§4) and the blocking status of D5b for 6.0d remain exactly as written.
> (2) The statement in the constraints below that "the owner is a minor" was
> **reclassified then as an unverified assumption carried from C3, not a verified
> current fact** — and is now **superseded entirely by the REASSESSMENT NOTE
> above**: personal capacity is treated only as an open eligibility requirement.
> See [CLOUD-READINESS-PLAN.md](CLOUD-READINESS-PLAN.md) §1.2.

**Standing constraints honored by this document:**

- No identity, contact detail, family detail, age, birth date or credential of
  any person appears here, and none is requested — **in either direction**: this
  document does not assert that the owner (or anyone) is or is not a minor, an
  adult, or legally capable. **Eligibility conditions are stated; personal facts
  are never assumed.** Nothing here may be published in a place that identifies
  an individual.
- Historical premises carried from earlier documents (incl. the former
  "owner is a minor" framing) are **historical context only**, clearly marked as
  such, and are never cited as established current fact.
- "Account holder", "contract counterparty", "developer", "technical
  administrator", "controller" and "operator" are **distinct roles**. Holding an
  account does not automatically make a person the GDPR controller, being named
  in a contract does not by itself settle the controller question, and GDPR
  obligations are **not** removed by being a natural person or by operating for
  free. This document keeps the roles separate throughout.
- Sections are separated as: **verified facts → unresolved questions →
  professional-review questions → options (no selection) → explicit owner
  decisions → deliberate non-actions → sources → next steps → phased GDPR
  controller readiness (§10, additive; existing §1–§9 numbering preserved for
  cross-document references).**

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
| CF-11 | **Re-verified 2026-10-21 (later session, full-text re-read):** the SSSA preamble states the agreement is "*between Cloudflare … and you, or the Entity you represent*", and the binding-authority representation is attached specifically to entering "*on behalf of a company, organization, or another legal entity*" — i.e., **entering in one's own name as a natural person is an expressly contemplated form of contracting**; no organizational customer is required by the agreement text. | https://www.cloudflare.com/terms/ |
| CF-12 | **Re-verified 2026-10-21:** SSSA **§18.1 (Regional Terms — European Union)** expressly addresses "*you (1) … an individual living in the European Union … and (2) [with] a billing address in the European Union*" — the agreement therefore creates an **individual-EU-resident customer category** (with the Data Act Addendum attaching when both conditions hold; whether it attaches for a Free account with **no billing address** is an open interpretive question — §2 Q2.13). | https://www.cloudflare.com/terms/ |
| CF-13 | **Re-verified 2026-10-21:** the complete current SSSA text again contains **no minimum-age or identity-verification clause** for account creation; the binding hurdles remain the capacity/authority warranties (CF-2, CF-6). | https://www.cloudflare.com/terms/ |

**Verified negative finding (CF):** no published minimum account age for a *first*
Cloudflare account was found in the SSSA, the Website Terms or the account-creation
docs as fetched/re-read on 2026-10-21. The binding hurdles found instead are
**capacity/authority warranties** (CF-2, CF-6), not an age number.

### 1.2 Supabase — contract chain and eligibility

| # | Verified fact | Source |
|---|---|---|
| SB-1 | **Supabase Terms of Service** ("Version 2 — May 6, 2026"; page footer "Last Modified: 1 May 2026") — read in full on 2026-10-21: it contains **no age or eligibility clause anywhere**. | https://supabase.com/terms (the previously recorded `supabase.com/company/terms` URL returned 404 on 2026-10-21 — see SB-8) |
| SB-2 | Supabase ToS require the accepter to have "*right, power, and authority to enter … legal authority to bind that organization*". California law; arbitration clause; §5(c) credit/fraud controls. | https://supabase.com/terms |
| SB-3 | **Supabase DPA Version 1 — August 1, 2026**: Customer = **controller**, Supabase = **processor**; accepting the Agreement constitutes signing the **SCCs (Module Two, Irish law)**; 30-day subprocessor notice with **5-day** objection window; 48-hour incident notice; Schedule 1 states daily backups "by default" (in tension with the Free plan's "Automatic backups: Not included" — see C4 item SB-P4). | https://supabase.com/legal/customer-resources/data-processing-addendum |
| SB-4 | **Free plan (current pricing page):** $0 — 500 MB DB, 5 GB egress, 5 GB cached egress, 1 GB storage, 50k MAU, 2 active projects, unlimited API requests; projects **paused after 1 week of inactivity**; **Auth audit-log retention = 1 hour** on Free; **API & DB log retention = 1 day** on Free; log ingestion 1 GB; **Automatic backups: Not included**; custom domains paid-only ($10/mo); spend cap not applicable on Free. | https://supabase.com/pricing |
| SB-5 | **Fair Use / overage behavior (Billing FAQ):** exceeding the Free quota first triggers notification + grace period, then **service restrictions** — pausing projects, read-only database, disabling new projects, **HTTP 402 on all API requests**. No silent paid overage on Free; overdue invoices pause projects and downgrade to Free. Credit card is the only payment method (no payment method is needed while staying within Free). | https://supabase.com/docs/guides/platform/billing-faq |
| SB-6 | **Regions:** each project picks one primary region; **specific region `eu-central-1` (Frankfurt)** exists alongside the general "Europe" grouping (which may deploy to non-EU London/Zurich). Supabase itself states region selection is "a data-location control, **not proof of regulatory compliance**". | https://supabase.com/docs/guides/platform/regions |
| SB-7 | Subprocessor list page documents updated **2026-10-12** and **2026-06-01**; e-mail subscription available. The full PDF contents were **not parsed** (C3 Q8, still open → C4). | https://supabase.com/legal/customer-resources/subprocessor-list |
| SB-8 | **Re-verified 2026-10-21 (full-text re-read at the canonical URL):** the ToS is "*a binding contract between you ("Customer," "you," or "your") and Supabase*" — the acceptance warranty reads "*YOU HAVE THE RIGHT, POWER, AND AUTHORITY TO ENTER INTO THIS AGREEMENT **AND, IF ENTERING INTO THIS AGREEMENT FOR AN ORGANIZATION**, THAT YOU HAVE THE LEGAL AUTHORITY TO BIND THAT ORGANIZATION*" — **individual entry in one's own name is expressly contemplated**; organizational status is an *optional* branch, not a requirement. The previous `supabase.com/company/terms` path now **404s** — the canonical URL is `https://supabase.com/terms` (C4 SB-P1 updated accordingly). | https://supabase.com/terms |
| SB-9 | **Re-verified 2026-10-21:** ToS **§13(b)(iii)** provides dispute-resolution terms that expressly differentiate "*If Customer is an **individual** using the Services for **non-commercial purposes***" (small-claims route preserved for such customers) — a **published contract category that directly matches the Q1-D shape** (natural person + free, non-commercial use). | https://supabase.com/terms |
| SB-10 | **Re-verified 2026-10-21:** ToS **§13(c)** states the class-action/jury waiver applies "*to all users (**both natural persons and entities**), regardless of whether Customer has obtained or used the service for personal, commercial, or other purposes*" — the agreement repeatedly recognizes **natural-person and non-commercial customers**. | https://supabase.com/terms |
| SB-11 | **Re-verified 2026-10-21:** ToS **§5(a)(i)** contains an **EU/UK consumer withdrawal-right waiver** ("*If you are a resident of the European Union or the United Kingdom and subject to applicable consumer protection laws…*") — the provider's own terms **contemplate natural-person consumer customers from the EU**, i.e., consumer-protection law is expected to engage at least for paid/self-serve signup flows. | https://supabase.com/terms |
| SB-12 | **Re-verified 2026-10-21:** ToS **§2(a)–(b)** limit use "*solely for … Customer's internal business purposes*" — boilerplate that a **non-commercial individual operator** may read as in tension with their actual use; whether this wording restricts free non-commercial individual use is an **interpretive question for the reviewer** (§2 Q2.14), not a verified bar. | https://supabase.com/terms |

**Verified negative finding (SB):** no age/eligibility clause exists anywhere in
the current Supabase Terms full text. As with Cloudflare, the binding hurdle
found is the **authority-to-bind warranty** (SB-2/SB-8), not a stated minimum age
— and, unlike Cloudflare, the Supabase text **explicitly names natural-person,
non-commercial and consumer customers** (SB-9–SB-11).

### 1.3 Google — OAuth dependency (feeds task 6.0e)

| # | Verified fact | Source |
|---|---|---|
| GO-1 | Google Terms of Service (effective 2026-07-30): "*If you're under the age required to manage your own Google Account, you must have your parent or legal guardian's permission*" — a parental-permission rule applies to using Google accounts. Whether Google sign-in is even offered to the project's end users, and under which console/age policy, is a **6.0e question**, but the age rule is a verified dependency for any Google OAuth plan. | https://policies.google.com/terms |

### 1.4 Applicable-law capacity framework (Czechia) — re-verified 2026-10-21

Retrieved as the official text of **Act No. 89/2012 Coll., Civil Code**, current
version **verze 18, účinná od 01.01.2026** (zakonyprolidi.cz copy of the
authentic text; the Ministry of Justice's official English PDF failed TLS
verification from this environment in the earlier session and was **not**
retrieved — C4 X-7):

| # | Verified fact (Czech Civil Code) |
|---|---|
| CZ-1 | **§30** — full legal capacity (plná svéprávnost) is acquired on reaching **18 years** (§30(1)); earlier by court grant of capacity or by marriage (§30(2)). |
| CZ-2 | **§31** — a minor who has not acquired full capacity is capable of legal acts to the extent appropriate to their **development/maturity** relative to minors of that age. |
| CZ-3 | **§32** — for specific legal acts, the **legal representative's consent** (souhlas zákonného zástupce) is required; consent can be limited and withdrawn. |
| CZ-4 | **§33 (re-verified exact current text)** — a legal representative's consent to a minor "*k samostatnému provozování obchodního závodu nebo k jiné obdobné výdělečné činnosti*" (independent operation of a **business undertaking or other analogous gainful activity**) makes the minor capable of the acts connected with that activity; **such consent requires court approval (přivolení soudu)** for validity (§33(1)); revocation of consent also requires court approval (§33(3)). *(This is the authentic current wording; an earlier paraphrase in this document's first draft rendered §33 with looser phrasing — superseded by this row.)* |
| CZ-5 | **§36** — a minor is never capable alone where the representative would need court approval for the act (§36(1)); **§37** — court grant of capacity possible from 16 with conditions. |
| CZ-5a | **§15(2)** — *svéprávnost* (legal capacity) is the capacity to acquire rights and assume obligations **by one's own legal act**; **§18** — a person is natural or legal. These are the definitional hooks that make capacity a **personal eligibility condition of the contracting party**, not of the project. |

**What this does and does not establish:** it establishes that under the
apparently applicable law the capacity of a *minor* to accept provider
agreements and to act independently in an economic activity can require
representative consent and, for gainful activity, **court approval**
(CZ-2–CZ-5). **Applied to this reassessment it establishes the general
eligibility frame only:** the accountable contracting party must be a person
who can *by their own legal act* assume the provider obligations (CZ-5a) —
**full capacity at 18 (CZ-1), or an earlier lawful route (CZ-5), or a
consent/court-approval structure (CZ-2–CZ-5)**. It does **not** establish
anything about any particular individual's situation — that is deliberately
**not known, not inferred and not recorded here** — and it does **not**
establish whether *this specific project* (a free, non-commercial, open-source
community backend) constitutes "podnikání / výdělečná činnost". **Those remain
questions for professional review (§4).** No conclusion is drawn here.

### 1.5 GDPR provisions relevant to Q1 (official EUR-Lex text STILL not retrieved)

- The provisions that govern Q1 are **GDPR Art. 2 (material scope)**,
  **Art. 4(7)** (definition of "controller"), **Art. 8** (conditions applicable
  to a child's consent for information society services, member-state age floor
  between 13 and 16) — plus Art. 6 bases, Art. 12–22 rights, Art. 24/28/30/32/33
  controller obligations and Art. 44–49 transfers that the professional review
  will need to weigh (§10 maps them to phases).
- **Verified this session from a NON-OFFICIAL mirror (gdpr-info.eu, wording
  usable only as reviewer pointers):**
  - **Art. 4(7):** "*'controller' means the **natural or legal person**, public
    authority, agency or other body which, alone or jointly with others,
    determines the purposes and means of the processing of personal data…*" —
    a **natural person can be a controller**; being an individual neither
    removes controller obligations nor substitutes for them.
  - **Art. 2(1)–(2):** the Regulation applies to processing of personal data
    wholly or partly by automated means, with four exclusions — the only
    plausibly relevant one being "*by a natural person in the course of a
    **purely personal or household activity***". A community service offered
    to the public is **not** a purely personal/household activity on its face —
    therefore **"currently free of charge" does not place the activity outside
    GDPR** (Art. 2 contains no commercial-purpose exemption).
  - **Art. 8:** consent-based processing for services offered directly to a
    child is lawful from **16** (member states may lower to **13**); below that,
    parental consent/authorization applies, with reasonable verification
    efforts — and "*paragraph 1 shall not affect the general contract law of
    Member States*" (i.e., **Art. 8 does not decide contractual capacity** —
    that stays with §1.4/Czech law and the reviewer).
- **Honest retrieval note:** official EUR-Lex retrieval (CELEX:32016R0679) was
  attempted again this session in three further forms (HTML, PDF, ELI URL) —
  **all returned no content**, following the two failed attempts recorded from
  the earlier session (**5 failed attempts total on 2026-10-21**). The mirror
  wording above is therefore **not an official-source verification**; retrieving
  the official text remains an open action item (C4 X-6, status updated).
- No statement anywhere in this document claims GDPR compliance, controller
  status, or a lawful basis. C3 already records: free hosting is not proof of
  compliance, and the architecture relies on transfer mechanisms (DPF + SCCs)
  rather than EU-only processing.

### 1.6 Czech business-status, trade-licensing and classification framework (new 2026-10-21)

Sources: **e-Sbírka** (official digital collection of Czech laws,
`e-sbirka.gov.cz/sb/1991/455` — served as a JavaScript shell to this fetcher,
but its indexed §2 text matched the full-text copies below) and
zakonyprolidi.cz authentic-text copies; **Czech Ministry of Industry and Trade
(MPO)** page for the trade act's current version (published 2026-07-01). All
accessed 2026-10-21.

| # | Verified fact |
|---|---|
| CZ-6 | **Trade Licensing Act No. 455/1991 Coll., §2:** "*Živností je soustavná činnost provozovaná samostatně, vlastním jménem, na vlastní odpovědnost, **za účelem dosažení zisku**…*" — a **trade** requires *all* elements: systematic activity, independently, in one's own name, on one's own responsibility, **for the purpose of achieving profit**. An activity without the profit purpose does not meet the statutory definition on its face; whether the actual Goldberg operating model meets any of the elements is **fact-dependent → professional question (§4 Q4.10)**. |
| CZ-7 | **Trade Act §3(1)(b):** "*živností není … využívání výsledků duševní tvůrčí činnosti, chráněných zvláštními zákony, **jejich původci nebo autory***" — **using the results of one's own protected intellectual creative activity is expressly excluded from being a trade.** Author-created software therefore has a **specific statutory carve-out** from trade licensing *when exploited by its author* — but *operating a hosted community service* built on that software is a **different act** than "using the results" of the work, so the carve-out's reach over the service itself is **open → Q4.11**. |
| CZ-8 | **Civil Code §420(1)** — a person who independently carries on gainful activity in a trade-like manner, systematically, **with the intention of achieving profit**, is treated as an **entrepreneur (podnikatel)** for that activity; **§420(2)** — for consumer-protection purposes, a person concluding contracts connected with their own business/professional activity is also treated as an entrepreneur. The **profit-intent element** recurs — a free, non-profit operation is not an entrepreneur *on these definitions by default*, but §420(2) can reach a person whose activity looks business-like → **Q4.12**. |
| CZ-9 | **Current version currency:** the trade act was **amended effective 2026-07-01** (Act No. 360/2025 Coll., per MPO) — any trade-licensing advice must use the current consolidated text, not a pre-2026 copy. |
| CZ-10 | **Categories distinguished (as the sources present them):** (a) **non-commercial software development** — author's own protected results, expressly outside the trade definition (CZ-7); (b) **free community-service operation** — systematic, own-name, own-responsibility operation of an information service **without profit purpose**, which on the statutory-element analysis (CZ-6/CZ-8) is not automatically a trade or entrepreneurial activity, but is **not automatically outside every obligation either**; (c) **gainful activity** — arises when the profit-purpose/consideration elements enter (e.g., paid features later). Which category the actual Goldberg operation falls into **depends on the operating model facts and is a professional determination, not an agent conclusion**. |
| CZ-11 | **Merely considering future commercial features creates no present business obligation on the sources reviewed** — the statutory triggers (CZ-6, CZ-8) key off *performing* activity for profit/consideration, not off *consideration itself*. Consequently: trade licensing, income-tax/VAT registration and paid-service consumer-protection obligations are understood to arise **at the earliest when monetization is actually implemented** — the precise trigger points (trade commencement, first taxable supply, thresholds) are **professional questions (§4 Q4.12/Q4.13)**, and **no monetization is approved now (§1.7)**. |

**Distinctions this document keeps explicit:**

- **Currently free ≠ outside all obligations.** Free/non-commercial status
  affects the *trade/tax* analysis (CZ-6/CZ-8) but has **no exemption effect on
  GDPR** (Art. 2, §1.5) and does not remove contract-law obligations toward
  users or providers.
- **Potential future monetization ≠ approval.** Considering future commercial
  features is **not** approval to sell, advertise, accept payments or operate
  paid infrastructure. No company, paid feature, billing arrangement or
  commercial operation is authorized by this task.

### 1.7 Product direction (recorded from the owner's clarification, 2026-10-21)

| # | Recorded direction |
|---|---|
| PD-1 | **Goldberg is currently intended to remain free of charge.** **Commercial features may be considered in a future version, but no monetization is approved or implemented now.** This is a product-direction record, not a decision to sell, advertise, accept payments, register a trade, form an entity or operate paid infrastructure. The permanent infrastructure requirement (**0 CZK/month, no paid plans, billing methods, chargeable trials or automatic upgrades**) is unchanged and applies to every option in §5. |

### 1.8 Provider contractual review for Q1-D — classification of findings

Each finding relevant to *direct natural-person contracting* is classified as
**VERIFIED** (stated in a current official contract/doc), **CONDITIONAL**
(verified text whose application depends on facts), **UNKNOWN** (only
observable at signup — forbidden to test now), or **LEGAL REVIEW**
(needs qualified assessment). Compiled from §1.1–§1.2 re-verifications:

| # | Finding | Classification |
|---|---|---|
| QD-1 | An individual may contract with Cloudflare in their own name — "*you, or the Entity you represent*" with the entity branch optional (CF-11); the agreement expressly addresses "*an individual living in the European Union*" (CF-12). | **VERIFIED** |
| QD-2 | An individual may contract with Supabase in their own name — "*binding contract between you … and Supabase*" with the organization branch conditional ("*IF … FOR AN ORGANIZATION*") (SB-8); natural-person and non-commercial customers are named throughout the ToS (SB-9–SB-11). | **VERIFIED** |
| QD-3 | Neither provider's current terms publish a minimum account age or an identity-verification step for a first free account (CF-13, SB-1). | **VERIFIED (negative finding)** |
| QD-4 | The accepting party must be able to give the **authority-to-bind warranties** honestly: CF DPA v6.4 "*full legal authority to bind Customer*" (CF-6), Supabase "*right, power, and authority to enter*" (SB-8); Cloudflare additionally prohibits signing up **on behalf of a third party** (CF-2) — which cuts *against* Option A's structure and *for* Q1-D's own-name structure, subject to interpretation. | **VERIFIED text / LEGAL REVIEW application** |
| QD-5 | DPA acceptance by a natural-person customer: both DPAs are incorporated into the self-serve terms (CF-4, SB-3); nothing in either DPA requires a corporate customer. | **VERIFIED (text) / CONDITIONAL (that the actual person may honestly warrant)** |
| QD-6 | Free-tier eligibility of an individual: Workers Free and Supabase Free publish no customer-type restriction; Supabase's own ToS names individual non-commercial users (SB-9). | **VERIFIED** |
| QD-7 | Whether the actual signup flows demand any age/identity declaration beyond the published terms (Q2.3) — observable only at signup, which is gated behind 6.0d. | **UNKNOWN (untestable without creating an account — forbidden here)** |
| QD-8 | Whether Cloudflare's §18.1 Data Act Addendum attaches to a Free account with no EU billing address (CF-12), and whether Supabase's "*internal business purposes*" scoping (SB-12) burdens a non-commercial individual operator. | **CONDITIONAL / LEGAL REVIEW** |
| QD-9 | Whether the operation of this free community service constitutes gainful activity/trade requiring registration or tax obligations (§1.6), and when those would trigger if monetization is later implemented. | **LEGAL REVIEW (model-dependent)** |
| QD-10 | Who, as a matter of GDPR law, would be the controller under Q1-D — the natural person **if** they genuinely determine the purposes and essential means (Art. 4(7), §1.5) — and whether that aligns with account holding and technical administration in this project's actual facts. | **LEGAL REVIEW (D5b itself)** |

**No incompatibility with Q1-D was found in the published contract texts.** The
residual hurdles are (a) the *actual* accountable person's capacity/eligibility
(CZ-1–CZ-5a), (b) the honestability of the authority warranties by that person,
and (c) the Czech business-status/classification questions (§1.6) — **none of
which this document resolves, and none of which is removed by being a natural
person operating for free.**

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
   (the account holder? the person who decides purposes/means? a legal entity?
   a guardian acting for a minor?) — this is D5b itself, deferred by owner
   decision to the pre-launch gate and **still legally unresolved**.
5. **Whether a parent/guardian accepting a contract "on behalf of" the minor's
   activity collides with Cloudflare SSSA §2.2.1(a)** (no sign-up on behalf of
   a third party) — the direction of the representation (guardian as principal vs.
   minor as principal) has contractual consequences that are not resolvable from
   the text alone. *(Applies to Option A; Q1-D avoids the question only if the
   accountable person contracts genuinely in their own name.)*
6. **Whether an adult account holder "automatically" becomes the controller** —
   **it does not, and this document does not assume it does.** Account holding,
   contractual capacity, technical administration and controller status are
   separate determinations that the professional review must make together —
   this applies identically to Options A, B and D.
7. **Any provider-side record/registration obligations of the eventual
   controller** (Art. 30 records, Art. 35 DPIA screening, supervisory-authority
   questions) — carried from C3 Q9, deferred to the pre-launch legal gate.
8. **Whether the community is "directed at" minors** despite D5f's
   general-audience design (C3 Q10) — open; interacts with GDPR Art. 8 (§1.5).
9. **NEW — Eligibility of the eventual accountable contracting party.** Does the
   person who would accept the Cloudflare/Supabase agreements possess (or
   lawfully hold through consent/court approval, CZ-1–CZ-5a) the capacity to
   assume those obligations **by their own legal act**? **Framed strictly as a
   requirement to be confirmed — this document knows nothing about, and does not
   ask about, any individual's personal status.**
10. **NEW — Under Q1-D, does the free non-commercial operation of this service
    constitute živnost/podnikání/other gainful activity** on the actual operating
    model (CZ-6–CZ-8), and what registrations (if any) attach — recognizing
    that the *current* free model and any *future* monetized model may classify
    differently (CZ-10, CZ-11)?
11. **NEW — If monetization is ever implemented in a future version (not
    approved now, PD-1):** when exactly do trade-licensing, tax/VAT,
    consumer-protection or other commercial obligations arise, and what is the
    lead time before a paid feature could launch lawfully (§4 Q4.12/Q4.13)?
12. **NEW — Whether author-created software + its hosted service have different
    classifications** (the §3(1)(b) author carve-out covers *using the results*
    of one's own work — does operating a free community *service* on top of it
    fall inside or outside that carve-out?) — CZ-7, open.
13. **NEW — Cloudflare SSSA §18.1 Data Act Addendum applicability to a Free
    account with no billing address** (CF-12), and any EU-individual-specific
    obligations it creates.
14. **NEW — Whether Supabase ToS §2(a)'s "*internal business purposes*"
    limitation (SB-12) is read by Supabase to burden free non-commercial
    individual use**, or is standard boilerplate inapplicable to the
    non-commercial branch the same ToS creates in §13(b)(iii).

---

## 3. Why this blocks (gate status — no status changed by this document)

- **D5b: LEGALLY UNRESOLVED / DEFERRED** (pre-launch gate, per owner decision in
  [OPEN-DECISIONS.md](OPEN-DECISIONS.md) §2). C3 Q1 upgraded it to **blocking
  for task 6.0d provisioning**: account creation requires accepting agreements
  whose authority warranties (CF-6, SB-2/SB-8) cannot honestly be warranted by
  an unknown/undetermined party. **The addition of Q1-D as a candidate changes
  nothing about this status** — a candidate arrangement is not a resolution.
- **6.0d provisioning: ⬜ not started and not authorized** — remains gated on a
  separate explicit owner instruction *and* on Q1/D5b resolution (this document
  resolves neither).
- **6.0e Google OAuth: ⬜ not started** — GO-1 adds an age dependency to review
  before any console configuration.
- **6.16 privacy/terms publication + real-user collection: ⬜ blocked** as before
  (D5a contact, D5b, D5d, published notices) — unchanged.
- **This document changes no checkbox.** C4 final verification stays open; Issue
  #31 stays open; Stage 6 production implementation stays NOT STARTED; ORD-A
  stays approved **as sequencing only**.

---

## 4. Questions for an appropriate professional/legal assessment

Route these verbatim; they are deliberately neutral, **capacity-agnostic**
(they refer to "the accountable contracting party" and the project's actual
operating model — they request and contain **no personal identifying
information of any kind**):

> **SUPERSESSION (2026-10-21 reassessment):** the first-draft versions of these
> questions (preserved in git history) framed Q1 around "a minor of the project
> owner's age" — a phrasing that rested on an **unverified personal assumption**.
> The capacity-neutral wording below supersedes it; no question is dropped, and
> the new Q1-D items (9–13) are additive.

1. Under the applicable law (apparently Czech Civil Code §§30–33, §1.4), may an
   individual who holds full legal capacity — or through a valid consent
   (§32)/court-approval (§33) structure where relevant — validly (a) accept
   Cloudflare's Self-Serve Subscription Agreement and Supabase's Terms of
   Service as the customer of record in their own name, (b) accept the
   incorporated DPAs' "full legal authority to bind" warranty, and (c) act as
   GDPR controller of a non-commercial community backend? **Please answer as
   eligibility conditions the accountable party must satisfy — no individual's
   personal status is disclosed to or sought from you.**
2. Is this project plausibly "independent business/gainful activity" requiring
   §33 court approval, or outside that provision? What facts determine the
   answer?
3. If a guardian/adult structure is ever used (Option A), does Cloudflare SSSA
   §2.2.1(a) (no sign-up on behalf of a third party) permit that structure, or
   must the adult be the genuine customer principal (with the technical operator
   under §2.3 third-party access)?
4. In each structurally permitted option, **who is the GDPR controller**, who is
   the processor, and what instrument (guardianship, mandate/zastoupení,
   employment, entity appointment, or simply the person's own operation under
   Q1-D) legally connects the parties? Does any structure require written
   documentation beyond the provider contracts?
5. If a legal entity is used (Option B): what is the simplest lawful formation
   path in the applicable jurisdiction for this purpose, what are its ongoing
   obligations (registration, accounting, tax — even at 0 revenue), and does it
   change any provider contract terms (entity vs individual customer)?
6. What must be true before any **real user data** is collected under the chosen
   structure: controller identification/contact (D5a), privacy notice content,
   lawful basis (Art. 6(1)), retention (D5d), minors/consent posture (D5f,
   Art. 8), Art. 30/35 screening?
7. Do Google's age terms (GO-1) permit the planned Google sign-in for the
   project's end users, and under what age/consent configuration?
8. Which, if any, obligations attach **now** (before launch) versus only upon
   opening the service to real contributors?
9. **NEW (Q1-D):** May a legally capable natural person operate this type of
   **free, non-commercial community backend directly, in their own name, without
   registering a company** — accepting both provider contracts and DPAs as the
   customer of record — under Czech law and the provider terms summarized in
   §1.8 (QD-1–QD-6)? What must be true for that structure to be valid?
10. **NEW (Q1-D):** Does operating this free community service constitute
    **živnost, podnikání or other výdělečná činnost** under CZ-6–CZ-8 (incl. the
    §3(1)(b) author carve-out for one's own protected works, CZ-7) — and does
    the answer change between the **current free model** and a **possible future
    monetized model** (not approved, PD-1)?
11. **NEW (Q1-D):** What trade-licensing, income-tax, VAT and
    consumer-protection obligations arise **if and when** monetization is later
    implemented, what triggers them, and what lead time is required? Does the
    mere *consideration* of future commercial features create any present
    obligation?
12. **NEW (Q1-D):** Under Q1-D, would the natural-person operator be the GDPR
    controller for all Goldberg Community processing (account metadata,
    contributor submissions, moderation decisions) — and which controller
    obligations (Arts. 5/6/12–15/17/24/30/32/33) attach at each rollout phase
    (§10)? How should the operator's privacy-notice identity/contact obligation
    (D5a) be satisfied **without** publishing unnecessary personal data?
13. **NEW (Q1-D):** Are there any Czech or EU rules that would make an
    individual's acceptance of Cloudflare SSSA §18.1 (EU individual terms) or
    Supabase's EU-consumer provisions (SB-11) inapplicable or disadvantageous
    for this operating model?

**Nothing in this section may be answered by the agent, by the owner alone where
professional judgment is required, or by inference from this document.**

---

## 5. Operator options A / B / C / D — for the owner's professional review

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

*Status note (reassessment):* Option A remains valid **where such a structure
applies**, but it must no longer be presented as the presumptive arrangement —
the personal circumstances it addresses are **not established facts** (REASSESSMENT
NOTE (1)). Its §2.2.1(a) exposure (Q4.3) is structurally *avoided* by Q1-D.

### Option B — Legal entity of record (adult-controlled) as customer/controller

**Shape.** An adult-controlled legal entity (formation path per Q4.5) becomes the
customer of record for Cloudflare and Supabase and, subject to professional
confirmation, the controller; any other person's involvement is documented
through the entity's internal arrangement.

| Dimension | Content |
|---|---|
| **Prerequisites** | Lawful, simple, sustainable entity formation confirmed by a professional; entity genuinely controls purposes/means; provider contracts reviewed for entity-customer specifics. |
| **Responsibilities** | Entity holds accounts/contracts/DPA warranties, bears controller duties (notice, retention, rights requests), maintains its own statutory obligations even at zero revenue. |
| **Risks / open questions** | Q4.5 (formation cost/effort could exceed the "0 CZK" spirit — **any fee is an owner decision, never absorbed silently**); ongoing compliance burden; complexity disproportionate to a Free-tier side project; controller-determination still requires confirmation. |
| **0 CZK impact** | Provider side stays Free; **entity formation/maintenance may cost money → potential conflict with the permanent 0 CZK requirement (owner decision, D4a-adjacent).** Flagged, not resolved. |
| **Real-user collection** | **Remains embargoed** — unchanged. |
| **Exact next action** | Ask the professional only: "what is the simplest lawful entity path and its real cost/obligations here?" — before any formation step; no formation initiated by the agent. |

*Status note (reassessment):* Option B's cost/burden is **less clearly justified**
now that the product direction is confirmed permanently free/non-commercial
(PD-1) — but it remains the natural fallback if monetization or liability
concerns ever become real, and it is the only option that self-evidently
satisfies "accountable party" questions through a legal person.

### Option C — Defer provisioning; no accounts until capacity/structure is settled

**Shape.** No provider accounts are created at all until Q4.1–Q4.13 are answered
and the owner picks an option. Local, offline Stage 6 server-core implementation
(Phase 1 tasks 6.1–6.9 against a local database) may proceed if/when separately
authorized, since it needs no cloud accounts; provisioning-dependent tasks
(6.0d, 6.0e, integration rows of 6.12–6.15) stay parked.

| Dimension | Content |
|---|---|
| **Prerequisites** | Original deferred-provisioning option; current operational hold retains the same gates, but an existing Supabase project is externally verified by ChatGPT connected integration (C4 §H). It is no longer a literal zero-resource inventory. |
| **Responsibilities** | Keep the collection embargo, keep fail-closed design, keep this register synchronized. |
| **Risks / open questions** | Delays any end-to-end verification of the C1/C2 architecture (C2 §12 production runtime check needs a real Worker); provider terms/pricing/subprocessors continue to move — hence the dated C4 re-verification requirement. |
| **0 CZK impact** | **None.** |
| **Real-user collection** | **Remains embargoed** — unchanged. |
| **Exact next action** | Schedule the professional review; meanwhile re-verify nothing below the C4 list has drifted (documentation-only). |

*Status note (reassessment):* Option C is **not** the intended deployment end
state (the cloud architecture is — CLOUD-READINESS-PLAN §1.1); it remains the
**current gated posture** and, while the professional review is pending, the
only permissible one.

### Option D (NEW — candidate, NOT approved) — Legally capable natural-person operator acting in their own name

**Shape.** A single **legally capable natural person** acts as the customer of
record for Cloudflare and Supabase **in their own name** (no company, no
guardian-as-principal), operates the free, non-commercial Goldberg Community
backend, retains technical control of the open-source project, and — **if they
genuinely determine the purposes and essential means of the processing** — is
the GDPR controller of record. **This is a candidate arrangement under
evaluation, not a selected or approved decision.**

| Dimension | Content |
|---|---|
| **Prerequisites** | (i) The accountable person satisfies the **capacity/eligibility conditions** of §1.4 (CZ-1–CZ-5a) — confirmed by professional review as an *eligibility fact*, with **no personal data recorded in this repository**; (ii) that person can honestly give the CF-6 / SB-8 authority warranties **for themselves** (no third-party sign-up issue — CF-2 is structurally satisfied since they contract genuinely in their own name); (iii) professional confirmation that free non-commercial operation needs no trade/tax registration on the actual model (§1.6, Q4.10–Q4.12); (iv) controller determination under D5b confirmed (Q4.12/Q4.4). |
| **Responsibilities** | The same person typically holds **four distinct roles** — account owner (contractual responsibility), technical administrator (MFA, secrets, D8c), project maintainer (technical control of the OSS repo), and GDPR controller (if purposes/means determination is theirs) — plus the D5a operator contact role *if and when* a contact is authorized (none is today). Data-subject requests, retention, security, breach handling and privacy-notice duties land on them (§10). |
| **Advantages** | Simplest and least costly structure; no formation fees (fits PD-1/0 CZK naturally); no §2.2.1(a) third-party-signup tension; contract texts of **both** providers explicitly contemplate natural-person, non-commercial and EU-consumer customers (QD-1, QD-2); aligns accountability with the person who actually decides purposes/means in a solo-maintained project. |
| **Risks / open questions** | All eligibility/classification questions remain **open professional questions** (Q4.9–Q4.13); single-point-of-failure on availability for data-subject requests and account recovery; personal liability exposure as sole counterparty and controller (no entity veil); future monetization would require **re-evaluating** the structure (trade/tax/consumer duties, Q4.11); provider signup-flow unknowns (QD-7); QD-8 interpretive clauses; **being a natural person does not reduce GDPR obligations (Art. 2/Art. 4(7))**. |
| **0 CZK impact** | **None** — Free tiers, no formation cost. Any future monetization is a **separate, explicitly approved future decision (PD-1)**. |
| **Real-user collection** | **Remains embargoed** — unchanged; Q1-D would *not* permit earlier collection; §10 phases still apply. |
| **Professional-review dependency** | **Heavy** — Q4.9–Q4.13 exist precisely for this option; §4 must be answered before Q1-D could be chosen. |
| **Limited private verification eventually authorizable?** | **Yes, conditionally** — under the already-approved ORD-A sequence, Q1-D could satisfy step 3 ("legally and contractually acceptable operator arrangement") **only after** the professional review confirms eligibility and the owner explicitly selects it; then the phase-scoped CR-3 authorization, day-of C4 etc. follow unchanged. |
| **Exact next owner decision** | Route §4 (incl. the new Q4.9–Q4.13) to a qualified professional; if the review confirms the eligibility conditions, **owner may then choose D** — selection is an explicit owner act, never implied by this document. |

### 5.5 Decision matrix — Q1-A / Q1-B / Q1-C / Q1-D comparison

| Dimension | **Q1-A** Adult/guardian of record | **Q1-B** Legal entity | **Q1-C** Deferred provisioning | **Q1-D** Natural person in own name (candidate) |
|---|---|---|---|---|
| **Legal/contractual prerequisites** | §§32–33 consent/court approval where applicable; CF-2 direction-of-representation analysis; professional confirmation | Lawful formation path; entity's own statutory duties; professional confirmation | None today | Accountable person's capacity/eligibility (CZ-1–CZ-5a); own-name warranties (CF-6/SB-8); non-trade classification (CZ-6–CZ-8); controller determination — **all via professional review** |
| **Accountable customer** | The guardian/adult | The entity | *n/a (none)* | The natural person, personally |
| **Who determines processing purposes** | **Open professional determination** — not automatically the account holder | **Open** — must be genuinely the entity | *n/a (no processing)* | **The natural person, if (and only if) they genuinely determine purposes/essential means** (Art. 4(7)) |
| **Operational complexity** | Medium — two people, access split under §2.3 | High — formation + ongoing duties | Zero | **Low — single operator** (but single-point-of-failure) |
| **Potential financial obligations** | None (0 CZK) | **Formation/maintenance fees possible → 0 CZK conflict risk** | None | None now; **future monetization would add trade/tax/consumer duties** (PD-1, Q4.11) |
| **Compatibility with 0 CZK requirement** | ✅ | ⚠ conditional (fees) | ✅ | ✅ |
| **Advantages** | Covers the scenario the earlier (now unverified) assumption anticipated | Liability separation; durable if monetized | Safest while undecided | Simplest, cheapest, contract-text-friendly, honest accountability alignment |
| **Risks** | §2.2.1(a) tension; guardian dependency; family data never published | Cost/complexity disproportionate; ongoing duties at 0 revenue | Delays architecture verification | Personal liability; eligibility/classification open; signup-flow unknowns; no obligation relief from "natural person" status |
| **Professional-review dependency** | Q4.1–Q4.4 | Q4.5 | Q4.1 (still) | **Q4.9–Q4.13** |
| **Limited private verification authorizable later?** | Yes, after review + explicit selection (ORD-A steps 3–4) | Yes, after review + selection + any formation | Verification happens only after an option is chosen | **Yes, conditionally — same ORD-A path, after review confirms eligibility and owner selects D** |
| **Exact next owner decision** | Choose A *if* review supports the structure | Choose B *if* formation is acceptable | Keep C as posture while routing the review | Choose D *only if* the review confirms Q4.9–Q4.13 conditions |

### 5.6 MiMo's recommendation (technical/operational opinion — NOT a legal conclusion)

Clearly labeled: this is the agent's **technical and operational** reasoning,
offered to structure the owner's decision; it is **not** legal advice, it does
**not** resolve D5b, and it **selects nothing**.

1. **Procedural first:** commission the §4 professional review (now including
   Q4.9–Q4.13) **before** choosing among A/B/D — every option depends on it, and
   no amount of documentation substitutes for it.
2. **Substantively, Q1-D is the arrangement worth evaluating first**, because:
   it fits the confirmed product direction (PD-1: free, non-commercial, future
   monetization only *possible*) and the permanent 0 CZK rule with **zero
   formation cost**; both providers' current contract texts **expressly
   contemplate natural-person, non-commercial and EU-consumer customers**
   (QD-1, QD-2) — no published incompatibility was found (QD-1–QD-6); it
   structurally **avoids Cloudflare SSSA §2.2.1(a)**'s third-party-signup
   problem, which is Option A's weakest point (Q4.3); and in a solo-maintained
   project it puts contractual responsibility, technical administration and
   (subject to confirmation) controllership in the **same honestly accountable
   person** — which is exactly what the authority warranties are asking for.
3. **Q1-C remains the correct posture meanwhile** (nothing provisioned); Q1-A is
   the natural fallback **if** the review finds an eligibility gap in the
   accountable person's situation; Q1-B becomes attractive **only if** liability
   separation or monetization turns into a real requirement (and its costs must
   then be weighed explicitly against 0 CZK).
4. **Hard limits on this recommendation:** Q1-D is **not approved** by it;
   D5b stays **UNRESOLVED/blocking**; being a natural person and being free
   remove **no** GDPR, contract or (later) commercial obligations; **no account
   may be created** under ORD-A until the review returns, Q1 is selected, the
   readiness checklist (CLOUD-READINESS-PLAN §5) is green and a **separate
   phase-scoped authorization (CR-3)** is given.

---

## 6. Explicit owner decisions required (none made here)

- [ ] **D5b (blocking for 6.0d):** commission the professional assessment in §4
      (questions are ready to forward verbatim; they contain no identifying
      information).
- [ ] **Choose operator structure:** Option A, B, C **or the new D** — only after
      §4 input. *(Q1-D is a candidate only; choosing it is an explicit act.
      **2026-10-22: the owner recorded Q1-D as the PREFERRED model — subject to
      eligibility + professional verification; the choice is still NOT made and
      this checkbox stays open until the review returns.**)*
- [ ] **D5a remains open:** no contact supplied/authorized for publication;
      do not supply identifying details in this public repository or in Issue #31.
- [ ] **Confirm the 0 CZK boundary** against Option B's potential formation
      costs, if that option is considered.
- [ ] **Confirm whether local-only Stage 6 implementation (no accounts) may
      proceed** while Option C is in effect — a separate owner instruction.
- [ ] **Re-affirm the collection embargo** (unchanged): no real user data before
      6.16 evidence.
- [ ] **Acknowledge the recorded product direction (PD-1):** currently free of
      charge; **no monetization approved or implemented**; any future commercial
      features are a separate future decision with their own review (Q4.11).

---

## 7. What this document deliberately does NOT do

- Does not resolve D5b or mark it legally resolved; does not select an operator
  (A, B, C **or D**). *(The owner's 2026-10-22 recorded preference for Q1-D —
  see the PREFERENCE ADDENDUM at the top — is a preference only; this document
  still selects nothing and D5b stays unresolved.)*
- Does not conclude that any person is or is not capable of any specific act; it
  states **eligibility conditions only** and records **no personal facts** — not
  the owner's, not anyone else's.
- Does not claim GDPR compliance, lawful basis, or legal approval of any kind,
  and does not treat free hosting, natural-person status or region selection as
  compliance.
- Does not request or record identity, age, birth date, family, address,
  contact or credential data — and rejects such data even if offered.
- Does not authorize provisioning, OAuth configuration, deployment, collection,
  entity formation, monetization or account creation.
- Does not claim that professional review is unnecessary — the opposite: §4 is
  the load-bearing step for every option.

---

## 8. Sources (all accessed 2026-10-21; ★ = added/re-verified in the reassessment session)

| # | Document | URL | Note |
|---|---|---|---|
| 1 | Cloudflare Self-Serve Subscription Agreement (Last Updated 2025-09-12) | https://www.cloudflare.com/terms/ | Full text read: no age clause; §2.2.1(a), §2.3, §2.6, §6.1; ★re-read for individual/§18.1 clauses (CF-11–CF-13) |
| 2 | Cloudflare Website Terms (effective 2025-08-01) | https://www.cloudflare.com/website-terms/ | Full text read: no age clause |
| 3 | Cloudflare Data Processing Addendum v6.4 (effective 2026-04-03) | https://www.cloudflare.com/cloudflare-customer-dpa/ | Full text read: authority-to-bind warranty |
| 4 | Cloudflare Docs — Create account | https://developers.cloudflare.com/fundamentals/account/create-account/ | First-account flow; ≥7-day rule for *additional* accounts |
| 5 | Cloudflare Workers Pricing (updated 2026-10-02) | https://developers.cloudflare.com/workers/platform/pricing/ | Free limits; Observability-pricing change 2026-12-01 |
| 6 | Cloudflare Workers Limits (updated 2026-10-12) | https://developers.cloudflare.com/workers/platform/limits/ | Fail-closed/open routing, subrequests, vars |
| 7 | Cloudflare Sub-Processors — For Cloudflare services (Last Updated 2025-10-01) | https://www.cloudflare.com/gdpr/subprocessors/cloudflare-services/ | Google/Oracle on Developer Platform; RSS feed |
| 8 | ★Supabase Terms of Service (canonical URL; Version 2 — May 6, 2026) | https://supabase.com/terms | ★Full text re-read for individual/non-commercial/consumer clauses (SB-8–SB-12); old `/company/terms` path 404s |
| 9 | Supabase Data Processing Addendum (Version 1 — August 1, 2026) | https://supabase.com/legal/customer-resources/data-processing-addendum | SCCs Module Two; 5-day objection window |
| 10 | Supabase Pricing (current) | https://supabase.com/pricing | Free limits; backups not included; log retention 1 h/1 day |
| 11 | Supabase Docs — Billing FAQ (Fair Use, Free overage behavior) | https://supabase.com/docs/guides/platform/billing-faq | Restrictions, not paid overage, on Free |
| 12 | Supabase Docs — Available regions | https://supabase.com/docs/guides/platform/regions | Specific `eu-central-1`; "not proof of compliance" |
| 13 | Supabase Subprocessor List page (docs updated 2026-10-12) | https://supabase.com/legal/customer-resources/subprocessor-list | PDF not parsed (C3 Q8) |
| 14 | Google Terms of Service (effective 2026-07-30) | https://policies.google.com/terms | Parental-permission rule for under-age users |
| 15 | ★Act No. 89/2012 Coll., Czech Civil Code — authentic Czech text, **verze 18, účinná od 01.01.2026** (§§15, 18, 30–37, 420–421) | https://www.zakonyprolidi.cz/cs/2012-89 | ★Re-retrieved and re-verified this session; official MoJ English PDF **still not** retrieved (TLS failure, C4 X-7) |
| 16 | ★Act No. 455/1991 Coll., Trade Licensing Act — §2 definition, §3(1)(b) author carve-out; current version incl. amendment effective 2026-07-01 (Act 360/2025) | https://e-sbirka.gov.cz/sb/1991/455 (official) · https://www.zakonyprolidi.cz/cs/1991-455 (authentic-text copy) · https://mpo.gov.cz/cz/podnikani/zivnostenske-podnikani/pravni-predpisy/zivnostensky-zakon--166698 (MPO currency note) | ★New this session; e-Sbírka served as JS shell to the fetcher — §2 text cross-checked against two full-text copies |
| 17 | GDPR, CELEX:32016R0679 (Art. 2, Art. 4(7), Art. 8 pointers) | https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:32016R0679 | **Retrieval FAILED 2026-10-21 — 0 bytes/no content, 5 attempts total (2 earlier + 3 in this session: HTML, PDF, ELI)** — re-retrieve: C4 X-6 |
| 18 | ★GDPR Art. 2, Art. 4, Art. 8 full wording (NON-OFFICIAL mirror — pointer-grade only) | https://gdpr-info.eu/art-2-gdpr/ · https://gdpr-info.eu/art-4-gdpr/ · https://gdpr-info.eu/art-8-gdpr/ | ★Used only because official retrieval keeps failing; wording must be re-verified against EUR-Lex (X-6) before being relied on externally |

---

## 9. Exact next steps (documentation-safe, in order)

1. Owner forwards §4 questions (now incl. Q4.9–Q4.13) to an appropriate
   qualified professional review (agent can format them, but cannot perform the
   review).
2. Professional assessment returns; owner records the D5b outcome in
   [OPEN-DECISIONS.md](OPEN-DECISIONS.md) and chooses **Option A, B, C or D**.
3. Only then: separately authorize 6.0d, executing the final C4
   provisioning-day re-verification
   ([C4-PREPROVISIONING-CHECKLIST.md](C4-PREPROVISIONING-CHECKLIST.md)).
4. Collection stays embargoed until 6.16 evidence exists, regardless of option.
5. Monetization (if ever wanted) is a **separate future version decision** with
   its own Q4.11 review — never implied by anything here (PD-1).

---

## 10. GDPR controller readiness (phased requirements — no compliance claimed)

Additive section (existing §1–§9 numbering preserved for cross-references).
**Roles first:** under Art. 4(7) the **controller** determines purposes and
means; **processors** act on the controller's behalf — here Supabase (per DPA v1)
and Cloudflare (per DPA v6.4) are contracted as **processors** for the Goldberg
service's personal data, while each provider is a **controller** for its own
account/billing data. **A natural-person controller carries the full controller
obligation set — natural-person status and free-of-charge operation are not
exemptions (Art. 2, §1.5).** Free-tier hosting and the Frankfurt region are
**not** compliance evidence (SB-6, C3).

Requirements are separated by the gate at which they bite:

### 10.1 Before initial provider signup (ORD-A steps 2–4)

- [ ] Accountable counterparty **determined** with capacity/eligibility
      confirmed by professional review (§4) — **recorded as a fact only, with no
      identity data in the repository**.
- [ ] Authority-to-bind warranties (CF-6, SB-8) identified as honestly
      giveable by that party (QD-4/QD-5).
- [ ] Role map drafted: contract holder · technical administrator · controller
      · D5a contact (not yet supplied) — each named by **role**, never by
      identity, in project docs.
- [ ] Data-minimization baseline: the signup itself creates provider-side
      account records (e-mail etc.) — provider is controller for those; no
      *Goldberg* personal data exists yet. **No controller identity or contact
      is published at this phase.**

### 10.2 Before private synthetic-data testing (ORD-A CR-5/CR-6)

- [ ] DPA coverage understood: acceptance mechanics, SCC modules, subprocessor
      objection windows (CF 10-day / SB 5-day), transfer realities from C3
      (US/EU edge metadata; Supabase SG/US support/telemetry flows) — as
      **disclosed constraints**, never absorbed silently.
- [ ] **Synthetic data only** (CR scope invariant): no real personal data ⇒
      most substantive controller duties are not yet *materially* triggered —
      but **security-by-design, log minimization and secret hygiene are
      exercised now** (CR-6.5: no cookies/tokens/service-role keys in logs;
      Free-tier log retention limits 3-day Workers / 1-hour auth / 1-day DB
      noted as a *security-monitoring tension* to manage, not a compliance claim).
- [ ] Fail-closed/degradation behavior (D4b) designed so a suspended/unavailable
      backend never degrades users' local data or silently loses submissions.

### 10.3 Before public real-user collection (6.16 gate — OPEN-DECISIONS §5)

- [ ] **Controller determined and final (D5b resolved)**; controller
      identification and contact route established (D5a — owner-supplied,
      verified as legally sufficient **before** publication; nothing published
      by the agent).
- [ ] **Lawful basis per processing purpose** (Art. 6(1)) documented: account
      creation/auth; contributor submissions; moderation; audit logs — chosen by
      the controller with professional input, not assumed here.
- [ ] **Privacy notice** (Arts. 13/14) covering purposes, bases, processors,
      transfers (C3's DPF/SCC analysis), retention pointers, rights, contact;
      **contribution terms** (D5e) and moderation rules published — 6.16 gates.
- [ ] **Data minimization** (Art. 5(1)(c)) verified against DATA-MODEL §7:
      journal/notes/mic/backups never cross the boundary; no birth dates/ages
      collected (D5f); auth metadata inventory written down.
- [ ] **Retention & deletion (D5d — details still open):** periods for
      pending/rejected contributions, audit records and backups must be set by
      professional review — **invent none**; `DELETE /v1/me` semantics defined;
      attribution-vs-erasure tension (D5e ↔ D5d) resolved before launch.
- [ ] **Data subject rights** (Arts. 15–22) operational: access, rectification,
      erasure, restriction, objection, portability; a real inbox/route exists;
      moderation/audit records addressed; response deadlines respected.
- [ ] **Children/general audience (Art. 8, D5f):** member-state age floor
      (13–16 range; the applicable Czech figure to be confirmed by the
      reviewer) posture set for account/contribution consent; general-audience
      moderation remains the design baseline; avoiding age collection does not
      eliminate obligations (OPEN-DECISIONS D5f).
- [ ] **Security & accountability (Arts. 24/30/32):** Art. 30 records screening
      performed; Art. 35 DPIA screening performed (C3 Q9); MFA'd operator access
      (D8c); encryption/RLS posture evidenced; **breach process (Arts. 33/34)
      written despite short Free-tier log retention** — the retention tension
      must be solved or reported as a blocker, never ignored.
- [ ] **International transfers** as identified in C3: DPF status re-checked
      (C4 X-2), SCCs in place via both DPAs, transfer realities disclosed in the
      notice.
- [ ] **Independent verification** that the notice/roles/retention/rights
      actually work (test request drill) — **before** the first real user, not
      after.

**Nothing in §10 constitutes compliance, and none of it may be declared
satisfied by Free-tier hosting or region selection.**

---

created by stokonn with love <3
