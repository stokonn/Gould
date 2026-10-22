# Q1 — Professional review brief: operator eligibility and controller determination (D5b)

**Goldberg 0.11 · Stage 6 · handoff document for a qualified professional · documentation-only ·
prepared 2026-10-22 · companion to [Q1-OPERATOR-READINESS.md](Q1-OPERATOR-READINESS.md) ·
branch `docs/goldberg-0.11-stage-6-planning`**

> **Status: routing document only.** This brief does **not** perform or simulate professional
> legal approval, does **not** resolve D5b, does **not** select an operator option (A, B, C or D),
> does **not** claim GDPR compliance, and does **not** authorize account creation, provisioning,
> OAuth configuration, deployment, implementation or collection. It contains **no personal
> identifying information** — no name, age, birth date, identity document, family detail,
> address, contact detail or credential of any person — and it must be answered the same way:
> all capacity questions are stated as **eligibility conditions** for an eventual accountable
> party, never as facts about an individual.
>
> **Gate status preserved by this document:** Q1/D5b **UNRESOLVED** (blocking for 6.0d) · C4
> **OPEN** · 6.0f **UNCHECKED** · 6.0d/6.0e **NOT AUTHORIZED** · ORD-A **APPROVED (sequencing
> only)** · Stage 6 implementation **NOT STARTED** · permanent **0 CZK/month** requirement
> unchanged · real-user collection **embargoed** (6.16).

---

## 1. Purpose and how to use this brief

- **Audience:** a qualified professional (Czech-qualified lawyer/attorney, or a data-protection
  specialist with Czech + EU competence) engaged by the project owner to answer §5 below.
- **What it consolidates:** the identity-free question set **Q4.1–Q4.13** from
  [Q1-OPERATOR-READINESS.md](Q1-OPERATOR-READINESS.md) §4 (all 13 preserved verbatim), the four
  operator structures under consideration, the verified primary sources behind each question,
  and the official guidance retrieved from the **Czech Data Protection Authority (ÚOOÚ)** and the
  **European Commission**.
- **How the owner will use the answers:** the written outcome is recorded in
  [OPEN-DECISIONS.md](OPEN-DECISIONS.md) (D5b row), and only then may the owner select an
  operator option. This brief itself decides nothing.
- **Repository rule:** the reviewer's answers may be summarized in the repository **only in
  role/eligibility terms**; no answer may cause personal identifying data to be written into
  this repository or into GitHub Issue #31.

## 2. Project facts relevant to the review (identity-free, verified in project docs)

| # | Fact (as recorded in project documentation) | Source in repo |
|---|---|---|
| F-1 | Goldberg is a **free, non-commercial** open-source Android application with a planned *optional* community backend; **no monetization is approved or implemented now** (commercial features are only a possible future-version consideration). | Q1 §1.7 (PD-1) |
| F-2 | Intended architecture: **Cloudflare Workers Free** (static + `/v1` BFF) + **Supabase Free, region `eu-central-1` (Frankfurt)**, under a permanent **0 CZK/month** requirement (no paid plans, billing methods, chargeable trials or auto-upgrades). | CLOUD-READINESS-PLAN §1.1; C1 |
| F-3 | **Owner-created provider accounts exist (reported 2026-10-22); no project or Worker is provisioned.** Existing accounts do not resolve eligibility/controller duties. Provisioning (6.0d) requires a separate explicit owner instruction and a passed Q1/D5b. | C4 §F.1/§F.5; OPEN-DECISIONS §5; Q1 §3 |
| F-4 | Under both providers' DPAs, **Cloudflare and Supabase are contracted as processors** for the service's personal data; each is a controller for its own account/billing data. | Q1 §1.1 CF-4/CF-6, §1.2 SB-3, §10 |
| F-5 | Roles are kept distinct: **contract holder · technical administrator · GDPR controller · D5a operator contact** (no contact authorized for publication today). | Q1 standing constraints; Q1 §10.1 |
| F-6 | First real-user collection is gated at **6.16**; until then only synthetic data may be used; the **journal/practice data of the app never crosses** into this backend. | C3; OPEN-DECISIONS §5 |
| F-7 | Planned end users are a **general audience** (D5f: general-audience posture, minimal data, no age collection designed in). | OPEN-DECISIONS §2 |

## 3. Verified law and official guidance vs. unresolved interpretation

> The separation below is deliberate and binding: **(A)** is text this project retrieved and
> read; **(B)** is official authority guidance retrieved and read; **(C)** is interpretation that
> **only the qualified professional may settle** — this document asserts nothing in (C).

### 3.1 Verified law (primary text, retrieved by this project)

| # | Instrument | What was verified | Retrieval note |
|---|---|---|---|
| L-1 | **GDPR (Reg. (EU) 2016/679)** — Art. 2 (scope), Art. 4(7) (controller), Art. 8 (child consent, 13–16 member-state floor) | A natural person can be a controller; the household exception covers only *purely personal/household* activity; free-of-charge operation is **not** an exemption (Art. 2 has no commercial-purpose carve-out). | **Official EUR-Lex text NOT retrieved** — 5 failed attempts on 2026-10-21 (C4 X-6 ❌). Wording currently relied on comes from a **non-official mirror** (gdpr-info.eu) and must be re-verified against CELEX:32016R0679 before external reliance. |
| L-2 | **Act No. 89/2012 Coll., Czech Civil Code**, verze 18, účinná od 01.01.2026 — §§15, 18, 30–37, 420–421 | Full capacity at 18 (§30); limited capacity by maturity (§31); representative consent (§32); consent to independent business/gainful activity requires **court approval** (§33); §§420–421 entrepreneur definition keyed to **profit intent**. | Authentic Czech text via zakonyprolidi.cz; official MoJ English PDF failed TLS (C4 X-7 ❌). |
| L-3 | **Trade Licensing Act No. 455/1991 Coll.** — §2, §3(1)(b); current version incl. amendment eff. 2026-07-01 (Act 360/2025) | A *trade* requires systematic activity, own name, own responsibility **and profit purpose** (§2); **using one's own protected intellectual/creative results is excluded** from trade (§3(1)(b)). | e-Sbírka §2 text cross-checked against two full-text copies + MPO currency note (2026-10-21). |

### 3.2 Official guidance (fetched and read on 2026-10-22)

**Czech Data Protection Authority (Úřad pro ochranu osobních údajů, ÚOOÚ):**

- **G-1 — *Základní příručka k ochraně údajů*** (Basic Guide, §7 "Správce, zpracovatel"):
  the controller is responsible for complying with all obligations the Regulation places on it
  and must be able to **demonstrate** compliance with the principles; each controller must
  perform **its own risk-based analysis** of its processing to determine which obligations
  apply (DPO, DPIA and records obligations are *not* universal); a **written controller–processor
  contract** (Art. 28(3) content) is required, and engaging a processor **does not transfer the
  controller's responsibility**; a **cloud provider is named as a typical processor**; and —
  directly relevant to this brief — **"neither for the controller nor the processor is his legal
  form decisive"** (právní forma není určující). The Guide also records the 1-month data-subject
  response deadline, the 72-hour breach notification rule, the Art. 6(1) legal-basis list and the
  transfer instruments (adequacy, BCR, SCC, Art. 49).
  Source: https://uoou.gov.cz/verejnost/zakladni-prirucka-k-ochrane-udaju (accessed 2026-10-22)
- **G-2 — *Desatero zpracování pro správce*** (Ten Rules for Controllers): ÚOOÚ publishes this
  simplified guidance explicitly as usable by **"small controllers, sole traders (živnostníci)
  or smaller businesses"** — i.e. the authority itself addresses single natural-person/small
  controllers, which is the category this project's Q1-D shape would fall into *if* selected and
  confirmed. It does not, however, answer any of the capacity, trade or controller-determination
  questions in §5.
  Source: https://uoou.gov.cz/desatero-zpracovani-pro-spravce-a-aktualizovana-zakladni-prirucka-k-gdpr (accessed 2026-10-22)
- **G-3 — ÚOOÚ consultation practice:** the Office's consultations are described as directed
  primarily at **sectoral associations representing controllers** and at DPOs — the brief's
  author does **not** rely on any direct ÚOOÚ pre-assessment being obtainable for an individual
  project; a private qualified professional is the appropriate reviewer.
  Source: same as G-1, §12 (accessed 2026-10-22)

**European Commission (official Commission guidance pages):**

- **G-4 — *Application of the GDPR*:** the controller determines the **"why" and "how"** of
  processing and is responsible for Art. 5 compliance and demonstrating it (refs Arts. 4(7),
  24, 26, 28–29; EDPB Guidelines 07/2020 on the concepts of controller and processor); a
  **processor acts only on the controller's behalf under a binding contract** — with cloud
  storage given as the typical processor example; the GDPR applies to services offered
  **"(paid or for free)"** and expressly regulates processing **"by an individual, a company or
  an organisation"**; the household exception applies only where there is **no connection to a
  professional or commercial activity** — *"When an individual uses personal data outside the
  personal sphere, for socio-cultural or financial activities, for example, then the data
  protection law has to be respected."*; and application **does not depend on organisation
  size**, only certain obligations do (<250-employee records nuance, DPO thresholds — risk-based).
  Source: https://commission.europa.eu/law/law-topic/data-protection/information-business-and-organisations/application-gdpr_en (accessed 2026-10-22)
- **G-5 — *Obligations*:** transparency information content (Arts. 12–14: identity, purposes,
  categories, legal basis, retention, rights, DPA complaint route; plain language; 1 month for
  indirectly obtained data); **data protection by design/default (Art. 25)**; risk-adjusted
  security (Art. 32); **72-hour breach notification** (Arts. 33–34, processor → controller
  notification); **DPIA thresholds** (Arts. 35–36, national DPAs publish binding DPIA lists,
  residual-risk consultation with the DPA); **DPO only where core activities involve
  large-scale sensitive data or large-scale regular/systematic monitoring** (Arts. 37–39).
  Source: https://commission.europa.eu/law/law-topic/data-protection/information-business-and-organisations/obligations_en (accessed 2026-10-22)

### 3.3 Unresolved interpretation (explicitly NOT settled by anything above)

- Whether **this specific project** is "independent business/gainful activity" (CZ Civil Code
  §33), a *živnost*/podnikání (Trade Act §2), or a non-economic open-source activity —
  model-dependent, fact-dependent, professional determination.
- Whether the §3(1)(b) author carve-out reaches the **hosted service**, not just use of the work.
- Which law/forum governs **capacity** when the provider contracts select California law (both
  providers) and Irish law (Supabase DPA SCCs) while the apparently applicable capacity law is Czech.
- Whether Cloudflare SSSA **§2.2.1(a)** permits a representative structure at all (Option A), and
  whether SSSA **§18.1**'s Data Act Addendum attaches to a Free account with no EU billing address.
- Whether Supabase ToS **§2(a) "internal business purposes"** burdens free non-commercial
  individual use (versus the individual/non-commercial branch its own §13(b)(iii) creates).
- **Who is the GDPR controller** in each candidate structure (D5b itself).
- The **Czech Art. 8 GDPR age floor** figure and its practical configuration for sign-in (also
  feeds 6.0e/GO-1).
- The **actual signup flows'** age/identity declarations — unobservable without creating an
  account (forbidden until 6.0d).
- The **official EUR-Lex GDPR wording** (X-6) — pending successful retrieval.

## 4. The four operator structures — neutral summary (no option selected)

| | **Q1-A** Adult/guardian of record | **Q1-B** Legal entity of record | **Q1-C** Deferred provisioning | **Q1-D** Natural person in own name |
|---|---|---|---|---|
| **Shape** | Guardian is customer/controller of record; second person operates under SSSA §2.3 third-party access. | Adult-controlled entity is customer/controller. | No accounts created until the review returns and an option is chosen. | One legally capable natural person contracts **in their own name** for both providers and, if they genuinely determine purposes/means, is the controller. |
| **Key prerequisite** | §§32–33 consent/court approval where applicable; direction-of-representation analysis (Q4.3). | Lawful formation + ongoing duties; possible fees vs. the 0 CZK rule (Q4.5). | None (this is today's posture). | Capacity/eligibility confirmed as a condition (CZ-1–CZ-5a); own-name warranties honestly giveable (CF-6/SB-8); non-trade classification (Q4.10–Q4.11); controller determination (Q4.12). |
| **0 CZK impact** | None. | ⚠ Formation/maintenance fees possible — an owner decision against the 0 CZK boundary. | None. | None (no formation cost). |
| **Status** | Candidate — not selected. | Candidate — not selected. | Current gated posture. | **Candidate — not selected, not approved.** |

**Why Q1-D is a candidate for review and not an approved solution.** The 2026-10-21
reassessment found (Q1 §1.8, QD-1–QD-10) that **both providers' current contract texts
expressly contemplate natural-person, non-commercial and EU-consumer customers**, and that no
published contract term is facially incompatible with own-name contracting — so the shape is
*worth reviewing*. That is a **textual compatibility finding only**. Everything that would make
Q1-D actually lawful for this project remains open: the accountable party's **eligibility**
(never examined here, never a recorded personal fact), the **trade/tax classification** of the
operating model, the **controller determination**, the **signup-flow unknowns** (QD-7) and the
interpretive clauses (QD-8). The technical/operational recommendation in Q1 §5.6 is labeled as
such precisely because it is **not** legal advice and **selects nothing**. Q1-D therefore
enters this review as a *question*, not as an answer — as does any pressure to treat "free of
charge" or "natural person" as reducing GDPR obligations (they do not: Art. 2, Art. 4(7), G-1,
G-4).

## 5. Questions for professional assessment — Q4.1–Q4.13 (preserved verbatim from Q1 §4, with primary-source references)

> Routing note (from Q1 §4): these questions are deliberately **capacity-agnostic**. They refer
> to "the accountable contracting party" and to the project's operating model. They request and
> contain **no personal identifying information**; please answer them as **eligibility
> conditions** and as **model-dependent legal questions**, not by asking for anyone's personal
> status. Section cross-references (§4, §1.8, §10 etc.) point to
> [Q1-OPERATOR-READINESS.md](Q1-OPERATOR-READINESS.md); the italic *Primary sources* lines are
> added routing aids and are not part of the questions themselves.

1. Under the applicable law (apparently Czech Civil Code §§30–33, §1.4), may an individual who
   holds full legal capacity — or through a valid consent (§32)/court-approval (§33) structure
   where relevant — validly (a) accept Cloudflare's Self-Serve Subscription Agreement and
   Supabase's Terms of Service as the customer of record in their own name, (b) accept the
   incorporated DPAs' "full legal authority to bind" warranty, and (c) act as GDPR controller of
   a non-commercial community backend? **Please answer as eligibility conditions the accountable
   party must satisfy — no individual's personal status is disclosed to or sought from you.**
   *Primary sources: Act No. 89/2012 Coll. §§30–33 (zakonyprolidi.cz/cs/2012-89); Cloudflare
   SSSA §2.2.1(a) + preamble (cloudflare.com/terms); Cloudflare DPA v6.4 (cloudflare.com/cloudflare-customer-dpa/);
   Supabase ToS (supabase.com/terms); GDPR Art. 4(7) (official text pending, X-6).*
2. Is this project plausibly "independent business/gainful activity" requiring §33 court
   approval, or outside that provision? What facts determine the answer?
   *Primary sources: Civil Code §§31–33; Q1 §1.6 CZ-6–CZ-10 (Trade Act §§2, 3(1)(b); Civil Code
   §§420–421).*
3. If a guardian/adult structure is ever used (Option A), does Cloudflare SSSA §2.2.1(a) (no
   sign-up on behalf of a third party) permit that structure, or must the adult be the genuine
   customer principal (with the technical operator under §2.3 third-party access)?
   *Primary sources: cloudflare.com/terms §§2.2.1(a), 2.3; Q1 CF-2/CF-3.*
4. In each structurally permitted option, **who is the GDPR controller**, who is the processor,
   and what instrument (guardianship, mandate/zastoupení, employment, entity appointment, or
   simply the person's own operation under Q1-D) legally connects the parties? Does any
   structure require written documentation beyond the provider contracts?
   *Primary sources: GDPR Arts. 4(7), 24, 26, 28 (official text pending, X-6); Cloudflare DPA
   v6.4 and Supabase DPA v1 (both providers = processor); ÚOOÚ Základní příručka §7 (G-1);
   EC "Application of the GDPR" (G-4).*
5. If a legal entity is used (Option B): what is the simplest lawful formation path in the
   applicable jurisdiction for this purpose, what are its ongoing obligations (registration,
   accounting, tax — even at 0 revenue), and does it change any provider contract terms
   (entity vs individual customer)?
   *Primary sources: Q1 §5 Option B; provider contract preamble/"Entity you represent"
   clauses (CF-11, SB-8); 0 CZK rule (D4a/PD-1).*
6. What must be true before any **real user data** is collected under the chosen structure:
   controller identification/contact (D5a), privacy notice content, lawful basis (Art. 6(1)),
   retention (D5d), minors/consent posture (D5f, Art. 8), Art. 30/35 screening?
   *Primary sources: GDPR Arts. 6(1), 8, 12–15, 30, 35 (official text pending, X-6); EC
   "Obligations" (G-5); ÚOOÚ Základní příručka §§4, 6 (G-1); Q1 §10.3.*
7. Do Google's age terms (GO-1) permit the planned Google sign-in for the project's end users,
   and under what age/consent configuration?
   *Primary sources: Google Terms eff. 2026-07-30 (policies.google.com/terms); GDPR Art. 8;
   feeds task 6.0e.*
8. Which, if any, obligations attach **now** (before launch) versus only upon opening the
   service to real contributors?
   *Primary sources: GDPR Art. 2 scope + Recitals 14/18/27 framing (mirror wording, X-6); EC
   "Application of the GDPR" scope/household passages (G-4); Q1 §10 phasing.*
9. **(Q1-D)** May a legally capable natural person operate this type of **free, non-commercial
   community backend directly, in their own name, without registering a company** — accepting
   both provider contracts and DPAs as the customer of record — under Czech law and the provider
   terms summarized in §1.8 (QD-1–QD-6)? What must be true for that structure to be valid?
   *Primary sources: cloudflare.com/terms (CF-11–CF-13); supabase.com/terms (SB-8–SB-11);
   Civil Code §§15, 18, 30–37; ÚOOÚ Desatero (G-2, small-controller guidance).*
10. **(Q1-D)** Does operating this free community service constitute **živnost, podnikání or
    other výdělečná činnost** under CZ-6–CZ-8 (incl. the §3(1)(b) author carve-out for one's own
    protected works, CZ-7) — and does the answer change between the **current free model** and a
    **possible future monetized model** (not approved, PD-1)?
    *Primary sources: Trade Act 455/1991 §§2, 3(1)(b) (current version, MPO currency 2026-07-01);
    Civil Code §420; Q1 §1.6.*
11. **(Q1-D)** What trade-licensing, income-tax, VAT and consumer-protection obligations arise
    **if and when** monetization is later implemented, what triggers them, and what lead time is
    required? Does the mere *consideration* of future commercial features create any present
    obligation?
    *Primary sources: Trade Act §2 profit-purpose element; Civil Code §420(1)–(2); Q1 §1.6
    CZ-11; PD-1 (no monetization approved).*
12. **(Q1-D)** Under Q1-D, would the natural-person operator be the GDPR
    controller for all Goldberg Community processing (account metadata, contributor submissions, moderation decisions) — and
    which controller obligations (Arts. 5/6/12–15/17/24/30/32/33) attach at each rollout phase
    (§10)? How should the operator's privacy-notice identity/contact obligation (D5a) be
    satisfied **without** publishing unnecessary personal data?
    *Primary sources: GDPR Arts. 5, 6, 12–15, 17, 24, 30, 32, 33 (official text pending, X-6);
    EC "Obligations" (G-5); ÚOOÚ Základní příručka §§3–8 (G-1); Q1 §10.*
13. **(Q1-D)** Are there any Czech or EU rules that would make an individual's acceptance of
    Cloudflare SSSA §18.1 (EU individual terms) or Supabase's EU-consumer provisions (SB-11)
    inapplicable or disadvantageous for this operating model?
    *Primary sources: cloudflare.com/terms §18.1 (CF-12); supabase.com/terms §§5(a)(i), 13
    (SB-11); Civil Code consumer provisions as applicable.*

**Nothing in this section may be answered by the project's AI agent, by the owner alone where
professional judgment is required, or by inference from project documentation.**

## 6. What a qualified professional must confirm before Q1/D5b can be resolved

D5b becomes resolvable only when **all** of the following exist as a written professional
outcome (recorded by the owner in [OPEN-DECISIONS.md](OPEN-DECISIONS.md); eligibility facts
recorded **without any identity data in the repository**):

- [ ] **C-1 · Eligibility of the eventual accountable contracting party** — confirmed as an
      *eligibility condition* (capacity to accept the provider agreements by their own legal
      act, or through a valid consent/court-approval structure), under Czech law (L-2), for the
      chosen structure. No personal status data is requested for or stored in the repository.
- [ ] **C-2 · Contract acceptability of the chosen structure** — the selected option's way of
      contracting satisfies SSSA §2.2.1(a)/§2.3, the CF DPA and Supabase ToS authority-to-bind
      warranties, honestly giveable by that party (QD-4/QD-5).
- [ ] **C-3 · Controller determination (D5b itself)** — in the chosen structure, who determines
      purposes and means, who is processor, and what instrument connects them; consistent with
      GDPR Art. 4(7) as described in G-1/G-4, and with the providers' DPA role allocation.
- [ ] **C-4 · Business-status/trade classification** — whether the current free model is
      živnost/podnikání/gainful activity or not (Q4.10), and the obligation triggers/lead time
      if monetization is ever implemented (Q4.11).
- [ ] **C-5 · Pre-collection readiness posture** — the Art. 6 basis, notice content (D5a route
      that publishes no unnecessary personal data), retention/deletion (D5d), minors/consent
      posture (D5f/Art. 8 Czech floor) and Art. 30/35 screening conclusions, phased per Q1 §10.
- [ ] **C-6 · Explicit statement of which option structures are lawfully available** to this
      project — leaving the **selection** to the owner (an option may be ruled *unavailable*;
      none may be ruled *chosen* by the review).

**What does NOT count toward resolution:** this brief; Q1's own analysis; free-tier hosting or
the Frankfurt region; being a natural person; operating at 0 CZK; the owner's unilateral reading;
or any agent-produced "approval". A partial answer that leaves C-1–C-3 open leaves **D5b
unresolved and 6.0d blocked**.

## 7. Requested output format (for the reviewer)

1. Numbered answers Q4.1–Q4.13, each marked **VERIFIED / CONDITIONAL (facts required) /
   OUT OF SCOPE**, citing the instrument and provision relied on.
2. A short **"facts required from the project"** list — limited to operating-model facts
   (never personal identification).
3. The confirmations C-1–C-6 from §6, in the same numbering.
4. An explicit **no-compliance-claim** disclaimer: the review resolves eligibility and structure
   questions; GDPR compliance itself remains demonstrated continuously (Art. 5(2), G-1).

## 8. Standing restrictions attached to this handoff

Identity-free in both directions · no option selected · no compliance claimed · no professional
approval simulated · Q1/D5b unresolved, C4 open, 6.0f unchecked, 6.0d/6.0e not authorized ·
ORD-A sequencing-only · no account, provisioning, deployment, OAuth, credentials, implementation,
real user data or monetization · permanent **0 CZK/month** · Stage 7 untouched · historical
notes (incl. prior "minor owner" framing) remain distinguishable historical context, never
current requirements.

## 9. Sources

| # | Source | URL | Accessed |
|---|---|---|---|
| 1 | Q1-OPERATOR-READINESS.md (all 13 questions, options, findings QD-1–QD-10) | local: `Q1-OPERATOR-READINESS.md` | 2026-10-21 |
| 2 | Cloudflare SSSA / Website Terms / DPA v6.4 / account docs | https://www.cloudflare.com/terms/ · https://www.cloudflare.com/website-terms/ · https://www.cloudflare.com/cloudflare-customer-dpa/ · https://developers.cloudflare.com/fundamentals/account/create-account/ | 2026-10-21 |
| 3 | Supabase ToS (canonical) / DPA v1 / pricing | https://supabase.com/terms · https://supabase.com/legal/customer-resources/data-processing-addendum · https://supabase.com/pricing | 2026-10-21 |
| 4 | Google Terms of Service (eff. 2026-07-30) | https://policies.google.com/terms | 2026-10-21 |
| 5 | Czech Civil Code, verze 18 (eff. 2026-01-01), §§15/18/30–37/420–421 | https://www.zakonyprolidi.cz/cs/2012-89 | 2026-10-21 |
| 6 | Trade Licensing Act 455/1991 (§2, §3(1)(b); amendment eff. 2026-07-01) | https://e-sbirka.gov.cz/sb/1991/455 · https://www.zakonyprolidi.cz/cs/1991-455 · MPO currency note | 2026-10-21 |
| 7 | GDPR CELEX:32016R0679 — **official text NOT retrieved (5 failures, C4 X-6)**; mirror wording used as pointer only | https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:32016R0679 · https://gdpr-info.eu/art-4-gdpr/ | attempted 2026-10-21 |
| 8 | ★ ÚOOÚ — Základní příručka k ochraně údajů | https://uoou.gov.cz/verejnost/zakladni-prirucka-k-ochrane-udaju | 2026-10-22 |
| 9 | ★ ÚOOÚ — Desatero zpracování pro správce (small controllers) | https://uoou.gov.cz/desatero-zpracovani-pro-spravce-a-aktualizovana-zakladni-prirucka-k-gdpr | 2026-10-22 |
| 10 | ★ European Commission — Application of the GDPR | https://commission.europa.eu/law/law-topic/data-protection/information-business-and-organisations/application-gdpr_en | 2026-10-22 |
| 11 | ★ European Commission — Obligations | https://commission.europa.eu/law/law-topic/data-protection/information-business-and-organisations/obligations_en | 2026-10-22 |

---

## 10. Owner decisions that remain after this brief exists

- [ ] Route this brief (§5) to a qualified professional — the review has **not** been
      commissioned or performed.
- [ ] Record the outcome in OPEN-DECISIONS (D5b) — resolution requires **all** of C-1–C-6.
- [ ] Choose Q1-A, Q1-B, Q1-C or Q1-D — an explicit owner act, only after the outcome; **no
      option is pre-selected here or anywhere in project documentation.**
- [ ] Only then: separate phase-scoped authorization for limited private verification
      provisioning (ORD-A steps 4+, CR-3), day-of C4 re-verification, with 6.0f acceptance
      still a separate gate.

---

created by stokonn with love <3
