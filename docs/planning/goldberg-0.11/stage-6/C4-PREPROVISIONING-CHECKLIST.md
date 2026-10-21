# C4 — Pre-provisioning checklist (terms, pricing, DPA, subprocessors) — preparation snapshot

**Goldberg 0.11 · Stage 6 · D8b condition C4 support document · documentation-only ·
prepared 2026-10-21 · branch `docs/goldberg-0.11-stage-6-planning`**

> **Standing of this document.** This is a **PRE-PROVISIONING checklist** — the
> current-source baseline captured on 2026-10-21 so the final C4 verification can
> be executed quickly and consistently on the day of any separately authorized
> provisioning (task 6.0d). It **does not discharge C4**. C4 stays **OPEN**, task
> **6.0f stays unchecked**, and the final C4 verification must be re-run
> **immediately before** any provisioning under a separate explicit owner
> instruction, using the "⚠ RE-VERIFY" items below. Prepared in response to C3
> §11 item 2 and C3 §13 decision point 1. No accounts exist, none were created,
> no provisioning, OAuth configuration or deployment is authorized by this file.

**Hard rules for execution (from AGENTS.md / OPEN-DECISIONS.md / BFF-FEASIBILITY.md):**

- **Zero-cost rule:** the architecture is approved at a strict **0 CZK/month**
  (Free tiers only: Cloudflare Workers Free + Supabase Free). If any verification
  shows a required capability is no longer available at 0 CZK (or a legal/terms
  assumption fails), **report the blocker and request an owner decision — never
  silently substitute a paid service, trial or different architecture** (D8b).
- **Access date discipline:** every row records what was verified **2026-10-21**
  and whether it must be re-verified on provisioning day. Provider documents move
  frequently; a version pinned today is evidence of *today*, not of tomorrow.
- **No compliance claims:** passing this checklist proves current-term alignment
  only. It is not legal advice, not GDPR compliance, not D5b resolution
  ([Q1-OPERATOR-READINESS.md](Q1-OPERATOR-READINESS.md) remains the blocking
  legal question), and not authorization to collect real user data (6.16).
- **Statuses:** ✅ verified 2026-10-21 · ⚠ verified 2026-10-21 **and must be
  re-verified on provisioning day** · ⬜ cannot be verified until provisioning
  (or until an account exists) · ❌ unresolved/open question carried from C3.

---

## A. Cloudflare (BFF hosting/runtime — C1 architecture)

| ID | Item | Current state (verified 2026-10-21) | Status |
|---|---|---|---|
| CF-P1 | Self-Serve Subscription Agreement version/date + no age clause + §2.2.1(a) third-party-signup rule, §2.3 third-party access, §2.6 Free Services, §6.1 DPA-by-reference | Last Updated **2025-09-12**, https://www.cloudflare.com/terms/ | ⚠ |
| CF-P2 | Website Terms version/eligibility | Effective **2025-08-01**, no age requirement stated, https://www.cloudflare.com/website-terms/ | ⚠ |
| CF-P3 | DPA version + effective date + acceptance mechanics ("full legal authority to bind Customer") | **v6.4**, effective **2026-04-03**, https://www.cloudflare.com/cloudflare-customer-dpa/ | ⚠ |
| CF-P4 | DPA transfer mechanisms (SCCs Module Two; DPF fallback; Global CBPR/PRP addition) and any DPF/adequacy status change | As in DPA v6.4; adequacy developments must be checked on the day (external legal state, not the doc) | ⚠ |
| CF-P5 | Workers Free pricing limits (100k req/day, 10 ms CPU/invocation, static assets free) | Pricing page updated **2026-10-02**, https://developers.cloudflare.com/workers/platform/pricing/ | ⚠ |
| CF-P6 | **Workers Logs → "Cloudflare Observability pricing" change beginning 2026-12-01** — re-check whether Workers Logs Free (200k events/day, 3-day retention) survives, changes, or starts charging after that date | Stated on pricing page; change date **2026-12-01** is *before* any realistic provisioning window → **mandatory re-check if date ≥ 2026-12-01** | ⚠ **dated** |
| CF-P7 | Workers plan limits (fail-closed/fail-open error routing 1027, 50 subrequests, 64 vars, 256 KB log/request) — needed for fail-closed design (D4b) | Limits page updated **2026-10-12**, https://developers.cloudflare.com/workers/platform/limits/ | ⚠ |
| CF-P8 | Account eligibility/age rules: no minimum age for first account; ≥7-day-old user + Super Admin for *additional* Free accounts; Students program 18+ is program-specific | https://developers.cloudflare.com/fundamentals/account/create-account/ | ⚠ (and Q1 blocking) |
| CF-P9 | Free plan posture: no billing method attached, no paid add-ons, no auto-upgrade path (C1 condition) | Configuration check **only possible once an account exists** — verify on provisioning day, record screenshot/evidence | ⬜ |
| CF-P10 | Subprocessor list "For Cloudflare services": version/date + Developer Platform entries (Google, Oracle) + notification subscription (RSS/e-mail) | Last Updated **2025-10-01**, https://www.cloudflare.com/gdpr/subprocessors/cloudflare-services/ | ⚠ + subscribe |
| CF-P11 | Privacy Policy + GDPR FAQ currency (edge processing in 330+ cities; US/EU metadata; "limited period of time" retention — C3 Q3 still unquantified) | Privacy Policy effective **2025-11-04** per C3 source list; retention quantity ❌ still open | ⚠ + ❌ Q3 |
| CF-P12 | Observability config set **explicitly in wrangler config**; invocation-log field set verified in an isolated test Worker post-provisioning (C3 Q2) | Cannot be done before an account/Worker exists | ⬜ (C3 Q2) |
| CF-P13 | Zero-cost fit of the actual chosen configuration (Free plan only) after any 2026-12-01 pricing change | Depends on CF-P6 | ⚠ |

## B. Supabase (data/auth backend — D4a, D5c `eu-central-1`)

| ID | Item | Current state (verified 2026-10-21) | Status |
|---|---|---|---|
| SB-P1 | Terms of Service version/date; authority-to-bind clause; **no age clause anywhere** | "**Version 2 — May 6, 2026**" (footer: Last Modified **1 May 2026**), https://supabase.com/terms (canonical URL as of 2026-10-21 reassessment — the older `/company/terms` path now returns 404) | ⚠ |
| SB-P2 | DPA version + date; SCCs Module Two (Irish law) on acceptance; 30-day notice / **5-day** objection window; 48 h incident notice | **Version 1 — August 1, 2026**, https://supabase.com/legal/customer-resources/data-processing-addendum | ⚠ |
| SB-P3 | Free plan limits: 500 MB DB, 5 GB egress, 5 GB cached egress, 1 GB storage, 50k MAU, 2 projects, 1 GB log ingestion; **pause after 1 week inactivity**; auth audit logs **1 h**, API/DB logs **1 day**; custom domains paid-only; no spend cap on Free | https://supabase.com/pricing | ⚠ |
| SB-P4 | **Automatic backups: "Not included" on Free** vs DPA Schedule 1 "daily backups by default" — resolve the discrepancy; self-export (`db dump`) is the accepted mitigation; record honest RPO (ZERO-COST-OPS.md §4) | Pricing page vs DPA Schedule 1; backups docs | ⚠ + ❌ (C3 Q7) |
| SB-P5 | Free-tier overage behavior: notification → grace period → **restrictions** (pause, read-only, 402), **no silent paid usage**; no payment method required on Free | https://supabase.com/docs/guides/platform/billing-faq | ⚠ |
| SB-P6 | Fair-Use restriction behavior matches D4b fail-closed design (402/read-only must map to graceful denial, practice never blocked) | Billing FAQ Fair Use section; verify in integration tests (6.14) | ⚠ design / ⬜ test |
| SB-P7 | Region availability: **specific `eu-central-1`** (not general "Europe") selectable in the real project-creation flow; residency limits + "not proof of compliance" wording unchanged | https://supabase.com/docs/guides/platform/regions | ⚠ + ⬜ in-flow |
| SB-P8 | Data-residency & transfers FAQ (v1, 2026-10-06): exception categories, SG/US processor, SCCs | https://supabase.com/legal/privacy-resources/data-residency-and-transfers-faq | ⚠ |
| SB-P9 | Subprocessor list: **parse the current PDF** (docs updated 2026-10-12 / 2026-06-01); subscribe to notifications; calendar the 5-day objection window | https://supabase.com/legal/customer-resources/subprocessor-list — ❌ PDF not yet parsed (C3 Q8) | ⚠ + ❌ Q8 |
| SB-P10 | Trust Center / Transfer Impact Assessment accessibility for Free-plan customers | Not opened (C3 Q5) | ❌ Q5 |
| SB-P11 | Auth/dashboard log retention (login/auth events) — not quantified in fetched docs (pricing gives 1 h audit / 1 day API+DB on Free) | Partially resolved by SB-P3; dashboard-side ❌ (C3 Q6) | ❌ Q6 |
| SB-P12 | SSL enforcement, MFA (D8c), RLS posture, no billing path enabled — configuration evidence at provisioning | Only observable in console post-signup | ⬜ |
| SB-P13 | Scheduled `db dump` export target established (0 CZK) at provisioning | Part of 6.0d | ⬜ |

## C. Cross-cutting / external dependencies

| ID | Item | Current state (verified 2026-10-21) | Status |
|---|---|---|---|
| X-1 | **D5b/Q1 unresolved** — do not provision if the operator/controller/capacity question is unresolved (C3 §11 last item) | Blocking; see [Q1-OPERATOR-READINESS.md](Q1-OPERATOR-READINESS.md) | ❌ **blocking** |
| X-2 | Transfer-mechanism landscape: EU-US DPF adequacy still in force + SCCs as fallback; re-check on the day for any adequacy/legal development since 2026-10-21 | EC EU-US transfers page per C3 source #17 | ⚠ |
| X-3 | Google Terms age rule (parental permission for users under the account-management age) before any 6.0e Google OAuth console work | Effective **2026-07-30**, https://policies.google.com/terms | ⚠ (blocks 6.0e review) |
| X-4 | Zero-cost assumption intact: Workers Free + Supabase Free + 0 CZK export path; **no paid plan, trial, billing method or upgrade anywhere** | Verified conceptually today; concrete confirmation at account setup (CF-P9, SB-P12) | ⚠ + ⬜ |
| X-5 | If any item fails the free/legal assumption → **blocker report to owner**, never a silent substitution | Standing rule (D8b, AGENTS.md) | standing |
| X-6 | **Re-retrieve official GDPR text (CELEX:32016R0679, Art. 4(7)/Art. 8)** — EUR-Lex fetch returned 0 bytes twice on 2026-10-21 (environment-blocked); **5 failed attempts total across 2026-10-21 sessions**; needed for the professional review package, not for provisioning itself | Retrieval failed; re-try from another network or official mirror. Q1 §1.5 currently relies on the **non-official** gdpr-info.eu mirror and says so explicitly — not a substitute for the official text | ❌ not retrieved |
| X-7 | Czech Civil Code official **MoJ English** translation — official PDF failed TLS verification (curl exit 60) from this environment; authentic Czech text used instead | Re-obtain official English text if the professional reviewer needs it | ❌ not retrieved |

## D. Provisioning-day execution sequence (final C4 — run under separate owner instruction)

1. [ ] Confirm **X-1 (D5b/Q1)** resolved per [Q1-OPERATOR-READINESS.md](Q1-OPERATOR-READINESS.md)
      §6 — stop if unresolved.
2. [ ] Re-fetch and re-read **CF-P1…CF-P5, CF-P7, CF-P8, CF-P10, CF-P11, CF-P13**
      (Cloudflare) and **SB-P1…SB-P9** (Supabase); update this table with new
      versions/dates and the new access date; investigate any diff.
3. [ ] **CF-P6:** if the date is ≥ 2026-12-01, confirm the post-change Workers
      Logs/Observability pricing still yields 0 CZK — **blocker report if not**.
4. [ ] **SB-P4:** confirm Free still has no automatic backups; export path works;
      record RPO in ZERO-COST-OPS.md.
5. [ ] **SB-P7:** confirm `eu-central-1` is selectable for the new project in the
      actual creation flow.
6. [ ] **SB-P9:** parse the current subprocessor PDF; subscribe to both providers'
      notifications; calendar objection windows (CF 10 days / SB 5 days).
7. [ ] Create nothing until steps 1–6 pass; then provision only what 6.0d's
      separate authorization specifies: Free plan only, no billing method, region
      per D5c, secrets server-side only, kill switch, export target.
8. [ ] Post-provisioning (⬜-items): CF-P9, CF-P12, SB-P12, SB-P13, and the C3
      §11 remainder (SSL/MFA evidence, staging-Worker log-field check).
9. [ ] Record results here and in [CHECKLIST.md](CHECKLIST.md) + Issue #31;
      **only then** may C4 be discussed as closed — by the owner, not by assumption.

## E. Sources for this snapshot (all accessed 2026-10-21)

Primary sources with URLs and version/date stamps are embedded in tables A–C;
the shared source set overlaps [C3-EDGE-PRIVACY-REVIEW.md](C3-EDGE-PRIVACY-REVIEW.md)
§12 (sources 1–18). Additional sources fetched for this document:

| # | Document | URL | Accessed |
|---|---|---|---|
| 1 | Cloudflare Self-Serve Subscription Agreement | https://www.cloudflare.com/terms/ | 2026-10-21 |
| 2 | Cloudflare Website Terms | https://www.cloudflare.com/website-terms/ | 2026-10-21 |
| 3 | Cloudflare Docs — Create account | https://developers.cloudflare.com/fundamentals/account/create-account/ | 2026-10-21 |
| 4 | Cloudflare Workers Pricing (upd. 2026-10-02) | https://developers.cloudflare.com/workers/platform/pricing/ | 2026-10-21 |
| 5 | Supabase Terms of Service (v2 — May 6, 2026) | https://supabase.com/company/terms | 2026-10-21 |
| 6 | Supabase DPA (v1 — August 1, 2026) | https://supabase.com/legal/customer-resources/data-processing-addendum | 2026-10-21 |
| 7 | Supabase Pricing (Free tier facts) | https://supabase.com/pricing | 2026-10-21 |
| 8 | Supabase Docs — Billing FAQ (Fair Use / Free overage) | https://supabase.com/docs/guides/platform/billing-faq | 2026-10-21 |
| 9 | Supabase Docs — Available regions | https://supabase.com/docs/guides/platform/regions | 2026-10-21 |
| 10 | Google Terms of Service (eff. 2026-07-30) | https://policies.google.com/terms | 2026-10-21 |
| 11 | Czech Civil Code (authentic Czech text) | https://www.zakonyprolidi.cz/1950-140 | 2026-10-21 |
| 12 | GDPR CELEX:32016R0679 | eur-lex.europa.eu | **FAILED 2026-10-21 (0 bytes ×2)** |

**Not executed today:** no accounts created, no console sessions, no API calls to
either provider, no PDF subprocessor parse (SB-P9), no TIA check (SB-P10), no
GDPR official-text retrieval (X-6), no MoJ English Civil Code download (X-7).

---

**C4 status after this document: OPEN.** This snapshot is preparation only; the
final C4 verification (section D) must run immediately before any separately
authorized provisioning.

created by stokonn with love <3
