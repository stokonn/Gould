# Stage 6 — Decision register (D4 / D5 / D8) — owner outcomes 2026-10-21

This register formalizes the D4/D5/D8 rows of the canonical register in
[STAGES.md](../STAGES.md) and the reconciliation in [stage-2/READINESS.md](../stage-2/READINESS.md).
The alternatives and trade-offs below were the planning proposals; **the owner completed
the architecture decision review on 2026-10-21** and the "Outcome" columns record those
decisions. No identity, contact details, controller information or credentials are
invented here. **No legal compliance (incl. GDPR) is claimed or inferred** — free hosting
is not proof of compliance (CLOUD.md), and the unresolved items below must be settled
before the gates they block.

Status vocabulary used below:

- **APPROVED** — owner decision recorded; design direction is settled.
- **PROVISIONAL PREFERENCE** — owner's stated preference, not yet actionable; no
  details were supplied and none may be invented.
- **DEFERRED** — owner explicitly deferred the decision to a named later gate.
- **CONDITIONAL / DETAILS PENDING** — principle approved, with a named verification or
  resolution step required before the dependent work may proceed.

## 1. Decision D4 — backend and free-tier acceptance

| # | Question | Outcome (2026-10-21) | Consequences |
|---|---|---|---|
| D4a | Which backend? | **APPROVED: Supabase Free.** One optional community backend. Strict **0 CZK/month** operating requirement: no paid plans, no trials that incur charges, no automatic upgrades, no billing escalation. | Supabase Free is the selected backend (ARCHITECTURE.md §2–3 updated). Workers+D1 is no longer a candidate for the initial implementation. Provisioning itself remains **separately gated** (§5 below). |
| D4b | Accept free-tier suspension/quotas? | **APPROVED: accept the graceful-denial model.** Potential Free-tier inactivity suspension and quota limitations are accepted. Community functionality must degrade gracefully; core local Goldberg functionality must remain operational; the system **fails closed** when the backend is unavailable. | Kill switch, warning/stop ladder and truthful "unavailable" copy (ZERO-COST-OPS.md §2, API-CONTRACTS.md §2 503) are binding design requirements, not proposals. |

## 2. Decision D5 — community operation, privacy and licensing

| # | Question | Outcome (2026-10-21) | Consequences |
|---|---|---|---|
| D5a | Who is the operator/controller contact? | **PROVISIONAL OWNER PREFERENCE:** a personal contact email is the intended public contact. **No email address has been supplied or authorized for publication.** | Do not invent, expose, request or publish personal contact information. Legally required controller identification and public contact information must be verified before launch. Blocks: privacy notice publication and public collection. |
| D5b | Privacy & data-processing responsibilities | **DEFERRED:** the legal data controller/operator will be determined **before public launch**, after reviewing applicable requirements. Privacy-by-design and data minimization apply meanwhile. | Do not claim GDPR compliance and do not authorize real-user collection. Applicable requirements concerning a minor project owner must be accounted for in the pre-launch review. **Research deliverable prepared 2026-10-21 — [Q1-OPERATOR-READINESS.md](Q1-OPERATOR-READINESS.md)** (verified findings, **Options A/B/C + candidate D (natural person in own name) with an A/B/C/D matrix and no selection**, professional-review questions Q4.1–Q4.13, product direction PD-1 free/non-commercial); **D5b remains legally unresolved and blocking for 6.0d provisioning.** |
| D5c | Processing region & transfers | **APPROVED: Frankfurt, Germany (`eu-central-1`).** Before provisioning, verify actual region availability, data residency, international processing/transfers, the DPA and subprocessors. EU database hosting alone must **not** be presented as a guarantee that all processing stays within the EU. | Region choice is fixed for provisioning task 6.0d; the verification list is a hard pre-provisioning sub-gate. |
| D5d | Account deletion & retention | **PRINCIPLE APPROVED — DETAILS PENDING:** preserve legitimately published non-personal catalogue data where rights and applicable law permit; minimize and appropriately erase account-linked personal data; support account deletion. | Retention periods for pending/rejected contributions, audit records and backups remain **unresolved**. Attribution requirements, pseudonymization vs anonymization, legal exceptions and deletion semantics must be reviewed before public launch. **Do not invent final retention periods or claim legal approval.** `DELETE /v1/me` stays reserved (API-CONTRACTS.md §3). |
| D5e | Contribution terms & licensing | **APPROVED, SUBJECT TO PRE-LAUNCH LEGAL REVIEW:** CC0 for contributor-owned structured catalogue metadata; CC BY-SA 4.0 for original contributor-authored text. | Third-party rights, source licenses, attribution and database rights remain fully applicable. Wikipedia content and copied third-party prose are **not** automatically relicensed. Field-level license/provenance requirements are preserved. Compatibility with D5d deletion/attribution must be verified before launch. |
| D5f | Community audience & age safeguards | **APPROVED DESIGN DIRECTION:** general-audience moderated community. No private messages; no public personal profiles; no unnecessary collection of dates of birth or age. Public browsing of approved catalogue information without login; authentication and review required for contributions; every contribution passes moderation before publication. | Applicable requirements involving minors, consent and Google authentication must be reviewed before enabling public participation. Avoiding age collection does **not** eliminate legal obligations and must not be claimed to. |
| D5g | Moderator/admin authorization | **APPROVED:** owner-only moderation initially. The architecture must permit adding or revoking moderators securely later. | All role grants, revocations and moderation decisions must be authorized **server-side and audited**. No self-approval, no client-controlled role assignment (AUTH.md §6). |

## 3. Decision D8 — Admin packaging and bootstrap

| # | Question | Outcome (2026-10-21) | Consequences |
|---|---|---|---|
| D8a | Android Admin packaging | **APPROVED: integrate Android Admin into the existing Goldberg application.** Preserve the four main navigation tabs; admin access appears only for authorized accounts. **No separate Admin APK** for the initial implementation. | Hidden UI is **not** a security boundary: the server must independently authorize every privileged action (AUTH.md §6). Blocks Stage 7 Part B Android work on this direction only. |
| D8b | Web Admin auth/session | **APPROVED CONDITIONALLY: Backend-for-Frontend (BFF).** Same-origin `HttpOnly`/`Secure`/`SameSite=Strict` session cookies; server-side authorization, session revocation and CSRF protection; restrictive CSP, origin allowlists and appropriate XSS protections. Browser JavaScript must not receive privileged bearer tokens. | **Before implementation**, prove the selected BFF hosting/runtime fits the permanent 0 CZK/month requirement including actual Free-tier limitations. If the zero-cost condition cannot be proven, **report the blocker and request an owner decision — do not silently replace the approved architecture** (AUTH.md §5). Feasibility study executed 2026-10-21 → **CONDITIONAL PASS** ([BFF-FEASIBILITY.md](BFF-FEASIBILITY.md)): recommended topology is a Cloudflare Workers Free Worker (static + BFF on one origin) + Supabase Free data plane; a Supabase-only same-origin host is blocked at 0 CZK. **C1 APPROVED 2026-10-21:** Cloudflare Workers Free hosts the static assets + BFF on one origin, Supabase Free remains the data/auth backend (`eu-central-1`), no paid plans/billing/trials/upgrades; architecture-level approval only — **no provisioning, deployment or implementation authorized**. **C2 EXECUTED 2026-10-21 (separate owner authorization) → CONDITIONAL PASS** (`spike/goldberg-0.11-c2-bff`, final commit `7bc6976`: 19/19 security, P1–P7 executed, kernel CPU 2.03 ms/invocation; per-invocation production CPU unverified, §12 production-runtime verification owner-gated). **C3 documentary review COMPLETE 2026-10-21** ([C3-EDGE-PRIVACY-REVIEW.md](C3-EDGE-PRIVACY-REVIEW.md)): conditional viability at 0 CZK; no compliance claim; legal/operator questions open. C4 (terms re-verification) also open. |
| D8c | Admin bootstrap & recovery | **APPROVED:** initial administrator role assigned manually through a **secured Supabase Console procedure** after verifying the account. No hardcoded admin accounts, passwords, embedded API secrets or privileged bypass endpoints. Privileged operator access must be **MFA-protected**. Every grant/recovery action is auditable. Lost admin access uses a controlled out-of-band recovery and re-grant procedure (AUTH.md §7). | No users, accounts, credentials or role grants are provisioned during planning or documentation tasks. Blocks admin endpoint activation only. |

## 4. Gate status after the 2026-10-21 decisions

### 4a. Resolved by this review

- [x] D4a/D4b decided — Supabase Free, graceful denial, fail closed, strict 0 CZK.
- [x] D5c decided — `eu-central-1` (Frankfurt), with pre-provisioning verification.
- [x] D5f decided — general-audience moderated community, minimal data.
- [x] D5g decided — owner-only moderation initially, server-side audited grants.
- [x] D8a decided — in-app Android Admin, no separate APK.
- [x] D8c decided — console bootstrap, MFA-protected operator, audited recovery.
- [x] **C1 decided 2026-10-21** — Cloudflare Workers Free **approved as the D8b BFF
      hosting/runtime** (single origin for static assets + `/v1`; Supabase Free stays
      the data/auth backend in `eu-central-1`; no paid plans, billing, trials or
      upgrades). Architecture-decision level only: **provisioning, deployment and
      implementation remain separately gated.** C2–C4 stay open (§4b).

### 4b. Still open (each blocks only its named dependents)

- [ ] **D5a details** — actual public contact supplied/authorized (blocks privacy notice
      publication and public collection).
- [ ] **D5b** — legal controller/operator determination (pre-launch gate).
      **Heightened by C3 (2026-10-21):** the project owner is a minor, and
      both providers' agreements require the accepting party to be able to
      bind the account/customer (Cloudflare's DPA warrants "full legal
      authority to bind Customer"). Who may lawfully hold the accounts and
      act as controller is now a **blocking question for 6.0d provisioning**
      and must go to an appropriate adult/professional review
      (C3-EDGE-PRIVACY-REVIEW.md §9 Q1). No identity or contact is invented.
      **Research prepared 2026-10-21:** [Q1-OPERATOR-READINESS.md](Q1-OPERATOR-READINESS.md)
      — verified facts (no published minimum account age on either provider's
      terms; authority-to-bind warranties; Czech Civil Code §§30–33; Google age
      rule), unresolved questions, professional-review question set, operator
      **Options A/B/C (no selection made)**. **Still unresolved — no legal
      conclusion recorded; D5b stays open and blocking for 6.0d.**
      **Reassessment 2026-10-21 (same day, later session; supersedes only the
      framing above, no gate changed):** capacity is now treated purely as an
      **eligibility precondition for the eventual accountable party** — never a
      personal fact about anyone; the C3 sentence above about the owner's
      personal status remains historical context, not a current verified fact
      (see §6 note). The option set is now **A/B/C/D**, adding candidate
      **Q1-D — a legally capable natural person acting in their own name**
      (Q1 §5, matrix §5.5, technical recommendation §5.6), and the product
      direction is recorded as **PD-1: free of charge, non-commercial, no
      monetization approved or implemented** (Q1 §1.7). Still **no option
      selected; D5b unresolved and blocking; professional review (Q4.9–Q4.13)
      still required.**
      **Routing package prepared 2026-10-22:**
      [Q1-PROFESSIONAL-REVIEW-BRIEF.md](Q1-PROFESSIONAL-REVIEW-BRIEF.md) — all 13
      questions Q4.1–Q4.13 verbatim with primary-source references, neutral
      A/B/C/D summary, official ÚOOÚ/European Commission guidance, and the
      C-1…C-6 confirmations required before D5b can be resolved. **The review
      itself is not commissioned, not performed, and nothing here resolves D5b.**
- [ ] **D5d details** — retention periods, deletion semantics, attribution vs
      pseudonymization (blocks retention jobs and `DELETE /v1/me` implementation;
      pre-launch review gate).
- [ ] **D5e legal review** — pre-launch verification of license compatibility with D5d
      and attribution requirements.
- [ ] **D5f pre-participation review** — minors/consent/Google-auth requirements before
      enabling public participation.
- [ ] **D8b zero-cost proof** — BFF hosting/runtime Free-tier fit, **before any
      implementation**; blocker → owner decision if unprovable. 🚧 *Feasibility study
      executed 2026-10-21 → CONDITIONAL PASS ([BFF-FEASIBILITY.md](BFF-FEASIBILITY.md)).
      **C1 APPROVED 2026-10-21** (Cloudflare Workers Free host, one origin; Supabase
      Free data/auth backend; `eu-central-1`; no paid plans/billing/trials/upgrades;
      architecture-level only — no provisioning authorized). **C2 EXECUTED
      2026-10-21 → CONDITIONAL PASS** (`spike/goldberg-0.11-c2-bff`, final commit
      `7bc6976`: 19/19 security, P1–P7 executed, kernel CPU 2.03 ms/invocation;
      per-invocation production CPU unverified, §12 production-runtime
      verification owner-gated). **C3 documentary review COMPLETE 2026-10-21**
      ([C3-EDGE-PRIVACY-REVIEW.md](C3-EDGE-PRIVACY-REVIEW.md)): conditional
      viability at 0 CZK; no compliance claim; legal/operator questions remain
      open (see D5b above). Still open: C2 §12 production verification, C4
      terms re-verification — **C4 pre-provisioning checklist prepared
      2026-10-21** ([C4-PREPROVISIONING-CHECKLIST.md](C4-PREPROVISIONING-CHECKLIST.md);
      dated current-source baseline, execution runbook §D; **C4 final
      verification still required immediately before any provisioning**).*

## 5. Separately gated activities (never authorized by these decisions)

- [ ] **Provisioning** (task 6.0d): Supabase Free project/database, region per D5c,
      secrets, kill switch, export target — requires a **separate explicit owner
      instruction**, after the D5c pre-provisioning verification list passes.
- [ ] **Google OAuth console configuration** (task 6.0e) — separately authorized;
      real credentials never committed.
- [ ] **Opening the service to real contributors** — requires, before any collection:
      the D5a contact, D5b controller determination, D5d retention/deletion shapes,
      published privacy notice and contribution terms, THREAT-MODEL.md [I]-venue rows
      executed for the chosen backend, and the export + isolated restore drill passed
      (ZERO-COST-OPS.md §4), plus explicit owner authorization.
- [ ] **Stage 6 implementation itself** — remains blocked on a separate owner
      instruction even though the design decisions are now settled (CHECKLIST.md).

An unresolved item blocks only its dependent tasks (STAGES.md standing rule); it never
blocks unrelated local work.

## 6. Operator decision reconciliation (2026-10-21, later session — documentation-only)

Recorded from the owner's cloud-readiness instruction; full analysis:
[CLOUD-READINESS-PLAN.md](CLOUD-READINESS-PLAN.md).

- **Intended deployment path = the approved cloud architecture** (Cloudflare
  Workers Free same-origin static + `/v1` BFF; Supabase Free PostgreSQL/Auth in
  `eu-central-1` Frankfurt; permanent 0 CZK; no paid plans, chargeable trials,
  automatic upgrades or unapproved billing) — **instead of the temporary
  local-only Option C** of Q1 §5. Option C is now only the *current gated
  posture* (nothing provisioned; nothing may be until its gates pass), not the
  intended end state. This is a deployment-path decision only: **no gate
  changed** — provisioning (6.0d), OAuth (6.0e), implementation and collection
  stay separately owner-gated, and 6.0f stays unchecked.
- **The earlier "owner is a minor" premise is reclassified as an UNVERIFIED
  assumption, not a verified current fact** (it was carried as a premise in C3
  Q1/Q10 and Q1 §1.4; those statements remain historical document context).
  No age, birth date, personal identity or family detail may be requested,
  recorded, committed or inferred anywhere. **D5b stays legally unresolved and
  blocking for 6.0d** — the contractual questions (account eligibility, legal
  capacity, authority to accept the provider agreements, GDPR controller role)
  still require professional verification per Q1 §4, and account registration
  would not establish compliance.
- **Gate-ordering decision — RESOLVED 2026-10-21: ORD-A APPROVED (sequencing
  only).** The owner selected **ORD-A — Scoped Private Cloud Verification
  Provisioning** as the sequencing strategy for closing 6.0f; **ORD-B and ORD-C
  are NOT selected**. The approved sequence (11 steps: documentary readiness →
  Q1/D5b resolution → acceptable operator arrangement → separate phase-scoped
  authorization for limited private verification provisioning → day-of C4 →
  minimum private test resources → C2 §12 synthetic-data production-runtime
  verification → resource removal + credential revocation + recorded cleanup →
  explicit owner acceptance or rejection of 6.0f → separate Stage 6
  implementation authorization → pre-launch legal/privacy gates) and the
  reconciled gate table are in [CLOUD-READINESS-PLAN.md](CLOUD-READINESS-PLAN.md)
  §§3–4, with the ORD-A readiness checklist in §5. **Clarifications binding:**
  ORD-A is a sequencing decision **only** — it is not acceptance of 6.0f, not
  a resolution of D5b, and not authorization to create accounts, provision,
  deploy, configure OAuth or implement; 6.0f remains a **pre-implementation
  gate** while narrowly scoped verification provisioning may precede its
  closure only after its own prerequisites and a separate authorization; the
  permanent **0 CZK/month** requirement (no billing methods, paid features,
  chargeable trials, auto-upgrades) applies to every step. **ORD-A is distinct
  from Q1 Options A/B/C/D (operator arrangements), none of which is selected —
  see the note below.**
- **Q1/D5b remains OPEN and blocking:** no provider account may be created
  while the contractual and legal prerequisites are unresolved; creating an
  account would not resolve D5b and would not establish compliance. Account
  eligibility, legal capacity to accept agreements, authority to bind, technical
  account administration and GDPR controller responsibilities remain five
  separate determinations. The earlier "owner is a minor" premise remains an
  **unverified assumption, not a verified current personal fact**; no age,
  birth date, identity, family detail, contact information or credential may
  be requested, inferred, committed or published. A concise owner-facing list
  of the Q1 §4 professional-review questions is prepared in
  [CLOUD-READINESS-PLAN.md](CLOUD-READINESS-PLAN.md) §9.1 for routing **outside
  GitHub**.

## 7. Q1 reassessment — natural-person operator readiness (2026-10-21, later session — documentation-only)

Full analysis: [Q1-OPERATOR-READINESS.md](Q1-OPERATOR-READINESS.md) (rewritten
in the reassessment). Current state, superseding only earlier Q1 framing:

- **Four option sets now assessed, none selected:** Q1-A (adult/guardian of
  record), Q1-B (legal entity of record), Q1-C (deferred provisioning — still
  the only permissible posture), candidate **Q1-D (legally capable natural
  person acting in their own name)** — compared in an A/B/C/D decision matrix
  (§5.5) with a clearly labeled technical/non-legal recommendation to evaluate
  D first *after* professional review (§5.6).
- **Capacity = eligibility precondition only:** whether the eventual
  accountable party meets the capacity/eligibility conditions is an abstract
  requirement for professional confirmation — **no personal fact (age, birth
  date, identity, family, contact) is requested, recorded, committed or
  inferred**, in either direction (PA-2).
- **Product direction PD-1:** Goldberg is intended to remain free of charge;
  commercial features may be considered in a future version; **no monetization
  is approved or implemented now.**
- **Nothing resolved:** D5b remains unresolved and blocking for 6.0d; no
  GDPR compliance is claimed; professional review is not unnecessary; Q4.9–
  Q4.13 are open; no account may be created, nothing provisioned, 6.0f stays
  unchecked, ORD-A stays sequencing-only.

created by stokonn with love <3
