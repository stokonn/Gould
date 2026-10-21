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
| D5b | Privacy & data-processing responsibilities | **DEFERRED:** the legal data controller/operator will be determined **before public launch**, after reviewing applicable requirements. Privacy-by-design and data minimization apply meanwhile. | Do not claim GDPR compliance and do not authorize real-user collection. Applicable requirements concerning a minor project owner must be accounted for in the pre-launch review. |
| D5c | Processing region & transfers | **APPROVED: Frankfurt, Germany (`eu-central-1`).** Before provisioning, verify actual region availability, data residency, international processing/transfers, the DPA and subprocessors. EU database hosting alone must **not** be presented as a guarantee that all processing stays within the EU. | Region choice is fixed for provisioning task 6.0d; the verification list is a hard pre-provisioning sub-gate. |
| D5d | Account deletion & retention | **PRINCIPLE APPROVED — DETAILS PENDING:** preserve legitimately published non-personal catalogue data where rights and applicable law permit; minimize and appropriately erase account-linked personal data; support account deletion. | Retention periods for pending/rejected contributions, audit records and backups remain **unresolved**. Attribution requirements, pseudonymization vs anonymization, legal exceptions and deletion semantics must be reviewed before public launch. **Do not invent final retention periods or claim legal approval.** `DELETE /v1/me` stays reserved (API-CONTRACTS.md §3). |
| D5e | Contribution terms & licensing | **APPROVED, SUBJECT TO PRE-LAUNCH LEGAL REVIEW:** CC0 for contributor-owned structured catalogue metadata; CC BY-SA 4.0 for original contributor-authored text. | Third-party rights, source licenses, attribution and database rights remain fully applicable. Wikipedia content and copied third-party prose are **not** automatically relicensed. Field-level license/provenance requirements are preserved. Compatibility with D5d deletion/attribution must be verified before launch. |
| D5f | Community audience & age safeguards | **APPROVED DESIGN DIRECTION:** general-audience moderated community. No private messages; no public personal profiles; no unnecessary collection of dates of birth or age. Public browsing of approved catalogue information without login; authentication and review required for contributions; every contribution passes moderation before publication. | Applicable requirements involving minors, consent and Google authentication must be reviewed before enabling public participation. Avoiding age collection does **not** eliminate legal obligations and must not be claimed to. |
| D5g | Moderator/admin authorization | **APPROVED:** owner-only moderation initially. The architecture must permit adding or revoking moderators securely later. | All role grants, revocations and moderation decisions must be authorized **server-side and audited**. No self-approval, no client-controlled role assignment (AUTH.md §6). |

## 3. Decision D8 — Admin packaging and bootstrap

| # | Question | Outcome (2026-10-21) | Consequences |
|---|---|---|---|
| D8a | Android Admin packaging | **APPROVED: integrate Android Admin into the existing Goldberg application.** Preserve the four main navigation tabs; admin access appears only for authorized accounts. **No separate Admin APK** for the initial implementation. | Hidden UI is **not** a security boundary: the server must independently authorize every privileged action (AUTH.md §6). Blocks Stage 7 Part B Android work on this direction only. |
| D8b | Web Admin auth/session | **APPROVED CONDITIONALLY: Backend-for-Frontend (BFF).** Same-origin `HttpOnly`/`Secure`/`SameSite=Strict` session cookies; server-side authorization, session revocation and CSRF protection; restrictive CSP, origin allowlists and appropriate XSS protections. Browser JavaScript must not receive privileged bearer tokens. | **Before implementation**, prove the selected BFF hosting/runtime fits the permanent 0 CZK/month requirement including actual Free-tier limitations. If the zero-cost condition cannot be proven, **report the blocker and request an owner decision — do not silently replace the approved architecture** (AUTH.md §5). Feasibility study executed 2026-10-21 → **CONDITIONAL PASS** ([BFF-FEASIBILITY.md](BFF-FEASIBILITY.md)): recommended topology is a Cloudflare Workers Free Worker (static + BFF on one origin) + Supabase Free data plane; a Supabase-only same-origin host is blocked at 0 CZK. **New owner decision required: accept the Cloudflare Free host (condition C1)**; C2–C4 also open. |
| D8c | Admin bootstrap & recovery | **APPROVED:** initial administrator role assigned manually through a **secured Supabase Console procedure** after verifying the account. No hardcoded admin accounts, passwords, embedded API secrets or privileged bypass endpoints. Privileged operator access must be **MFA-protected**. Every grant/recovery action is auditable. Lost admin access uses a controlled out-of-band recovery and re-grant procedure (AUTH.md §7). | No users, accounts, credentials or role grants are provisioned during planning or documentation tasks. Blocks admin endpoint activation only. |

## 4. Gate status after the 2026-10-21 decisions

### 4a. Resolved by this review

- [x] D4a/D4b decided — Supabase Free, graceful denial, fail closed, strict 0 CZK.
- [x] D5c decided — `eu-central-1` (Frankfurt), with pre-provisioning verification.
- [x] D5f decided — general-audience moderated community, minimal data.
- [x] D5g decided — owner-only moderation initially, server-side audited grants.
- [x] D8a decided — in-app Android Admin, no separate APK.
- [x] D8c decided — console bootstrap, MFA-protected operator, audited recovery.

### 4b. Still open (each blocks only its named dependents)

- [ ] **D5a details** — actual public contact supplied/authorized (blocks privacy notice
      publication and public collection).
- [ ] **D5b** — legal controller/operator determination (pre-launch gate).
- [ ] **D5d details** — retention periods, deletion semantics, attribution vs
      pseudonymization (blocks retention jobs and `DELETE /v1/me` implementation;
      pre-launch review gate).
- [ ] **D5e legal review** — pre-launch verification of license compatibility with D5d
      and attribution requirements.
- [ ] **D5f pre-participation review** — minors/consent/Google-auth requirements before
      enabling public participation.
- [ ] **D8b zero-cost proof** — BFF hosting/runtime Free-tier fit, **before any
      implementation**; blocker → owner decision if unprovable. 🚧 *Feasibility study
      executed 2026-10-21 → CONDITIONAL PASS ([BFF-FEASIBILITY.md](BFF-FEASIBILITY.md));
      conditions C1 (owner accepts the Cloudflare Free host), C2 (measured CPU spike),
      C3 (D5b/D5c edge-location review) and C4 (terms re-verification) still open.*

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

created by stokonn with love <3
