# C4 — Pre-provisioning checklist (terms, pricing, DPA, subprocessors) — preparation snapshot

> **Current inventory amendment (2026-10-22):** owner-created Supabase project
> EXISTS; ChatGPT connected-integration observations are attributed in §H below.
> This coding agent did not execute those checks. Earlier zero-project
> observations are historical. No C4 checkbox or provisioning authorization changes.

**Goldberg 0.11 · Stage 6 · D8b condition C4 support document · documentation-only ·
prepared 2026-10-21 · preliminarily re-verified and reconciled 2026-10-22 (§F) ·
branch `docs/goldberg-0.11-stage-6-planning`**

> **Standing of this document.** This is a **PRE-PROVISIONING checklist** — the
> current-source baseline captured on 2026-10-21 so the final C4 verification can
> be executed quickly and consistently on the day of any separately authorized
> provisioning (task 6.0d). It **does not discharge C4**. C4 stays **OPEN**, task
> **6.0f stays unchecked**, and the final C4 verification must be re-run
> **immediately before** any provisioning under a separate explicit owner
> instruction, using the "⚠ RE-VERIFY" items below. Prepared in response to C3
> §11 item 2 and C3 §13 decision point 1. At the original snapshot no accounts existed;
> subsequent owner-created accounts/project are reconciled in §F/§H. No agent created them;
> no provisioning, OAuth configuration or deployment is authorized by this file.
> **AMENDED 2026-10-22 (§F): the owner reports both provider accounts now
> exist** — an owner-side fact that authorizes nothing and discharges no row:
> no resource provisioning, billing/payment-method change, OAuth
> reconfiguration or deployment is authorized by this file, and the §D day-of
> sequence remains mandatory before any 6.0d provisioning.

**Hard rules for execution (from AGENTS.md / OPEN-DECISIONS.md / BFF-FEASIBILITY.md):**

- **Zero-cost rule:** the architecture is approved at a strict **0 CZK/month**
  (Free tiers only: Cloudflare Workers Free + Supabase Free). If any verification
  shows a required capability is no longer available at 0 CZK (or a legal/terms
  assumption fails), **report the blocker and request an owner decision — never
  silently substitute a paid service, trial or different architecture** (D8b).
- **Access date discipline:** every row records what was verified **2026-10-21**
  and whether it must be re-verified on provisioning day; the **2026-10-22
  preliminary recheck is recorded separately in §F** and changes no status.
  Provider documents move frequently; a version pinned today is evidence of
  *today*, not of tomorrow.
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

## F. Preliminary verification & documentation reconciliation — 2026-10-22 (C4 remains OPEN)

Owner-instructed, documentation-only recheck of the official Cloudflare and
Supabase pricing, terms, DPA, region and backup documentation against this
snapshot, plus recording of new account facts. **Method: public document
fetches only — no console login, no MCP/API billing call, no resource created,
no payment method inspected or changed, no account setting touched.** The §A–§C
tables and their statuses are left intact; this section is dated evidence, not
a status change.

### F.1 New recorded facts

| # | Fact | Source / verification |
|---|---|---|
| N-1 | **Both provider accounts now exist.** | Owner instruction 2026-10-22. Cloudflare side corroborated by read-only MCP checkpoint #1 ([Issue #31 comment `6343122203`](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6343122203): 1 standard account, created 2026-10-22, 0 zones, 0 Workers scripts, not suspended); the **Supabase account's existence is recorded from the owner's statement only** — no agent console/API verification performed. Accounts were created owner-side; no agent created, modified or deleted any account. |
| N-2 | **Cloudflare MCP read-only authentication is verified.** | Checkpoint #1: `GET /user` → 200; Workers R2 *read* scope present; billing/subscription endpoints refused (403, no OAuth scope); no write scope exercised; **inaccessible billing is explicitly NOT evidence of zero costs.** |
| N-3 | **Cloudflare Workers Free remains available.** | Workers pricing page re-fetched 2026-10-22 ("Last updated Oct 2, 2026" — unchanged): Workers Free is the default plan — 100,000 requests/day, 10 ms CPU per invocation, no charge for duration, static assets free & unlimited. Still ⚠ re-verify day-of (CF-P5). |
| N-4 | **Supabase Free remains available with stated limits.** | Pricing page re-fetched 2026-10-22: $0/month, 500 MB database (shared CPU), 5 GB egress, 5 GB cached egress, 1 GB file storage, 50k MAU, 2 active projects, 1 GB log ingestion, auth audit logs 1 h, API/DB log retention 1 day, unlimited API requests, pause after 1 week inactivity — matches the §B snapshot. Still ⚠ day-of (SB-P3). |
| N-5 | **Supabase `eu-central-1` remains documented as an available specific region.** | Regions page re-fetched 2026-10-22: "Central EU (Frankfurt), `eu-central-1`" listed under **Specific regions**; residency wording unchanged ("Region selection is a data-location control, not proof of regulatory compliance"). In-flow selectability still ⬜ (SB-P7) — no console opened. |
| N-6 | **Automatic backups are not included on Supabase Free.** | Pricing feature table re-fetched 2026-10-22: Automatic backups — Free **"Not included"** (Pro 7 days / Team 14 days / Enterprise custom). Backups docs re-fetched: daily backups are Pro+; official guidance — "free tier plan projects regularly export their data using the Supabase CLI `db dump` command and maintain off-site backups". DPA Schedule 1 discrepancy ❌ (C3 Q7) unchanged; RPO per ZERO-COST-OPS.md §4. |
| N-7 | **Supabase Free quotas can result in service restrictions.** | Billing FAQ re-fetched 2026-10-22: notification on quota exceed → if continued, **"service restrictions will apply"**; Fair Use restrictions = project pause, database read-only mode, disabled new project launches/transfers, **402 status code for all API requests**; grace period first; without prior notice only for suspected abuse. Consistent with SB-P5/SB-P6 (the 6.14 integration test is still ⬜). |
| N-8 | **Billing and payment-method status are NOT yet verified.** | ⬜ Cloudflare: billing endpoints refused via MCP (403, no scope) — explicitly not zero-cost evidence; dashboard billing never inspected. Supabase: billing/payment pages never opened (no console session this session or prior ones). CF-P9, SB-P12 and X-4 remain ⬜ day-of items; the 0 CZK posture still rests on the C4/ORD-A gates and the owner's accounting, never on a tool limitation. |
| N-9 | **Subprocessor and transfer reviews remain incomplete.** | ❌ SB-P9 subprocessor **PDF still unparsed** (page dates re-checked: Updated Oct 12, 2026 / Jun 1, 2026 — unchanged); ❌ SB-P10 Trust Center / TIA not opened; CF-P10 notification subscription not yet made; X-2 transfer landscape ⚠ day-of; X-6 official GDPR text still not retrieved (5 failed attempts across 2026-10-21); X-7 MoJ English translation not retrieved. |
| N-10 | **Q1-D is the owner's PREFERRED operator structure, subject to eligibility verification.** | Preference recorded 2026-10-22 (`78fece3`) — a directional preference only, **NOT a selection, NOT an approval, NOT a D5b resolution**; contractual eligibility (CF-P8 account-eligibility rows) and professional verification remain open; D5b/Q1 stay ❌ unresolved and blocking; no option selected or implied by any agent. |

### F.2 Cloudflare documentation-date recheck & observability pricing announcement

Rechecked against the 2026-10-21 source set (§E) on **2026-10-22**:

| Doc | Snapshot (2026-10-21) | Rechecked 2026-10-22 | Difference |
|---|---|---|---|
| Self-Serve Subscription Agreement (CF-P1) | Last Updated 2025-09-12 | "Last Updated September 12, 2025" | none |
| Workers pricing (CF-P5) | updated 2026-10-02 | "Last updated Oct 2, 2026" (dateModified 2026-10-02) | none |
| Workers limits (CF-P7) | updated 2026-10-12 | dateModified 2026-10-12; fail-closed error-routing code **1027** present | none |
| Customer DPA (CF-P3) | v6.4, effective 2026-04-03 | "Version 6.4", effective April 3, 2026 | none |
| Subprocessors page (CF-P10) | Last Updated 2025-10-01 | "Last Updated: October 1, 2025" | none |
| Website Terms (CF-P2) | Effective 2025-08-01 | **date not re-extractable this session** (client-rendered) | honest gap — remains ⚠ day-of |
| Observability pricing (CF-P6 target page) | **not in the §E source set** | **"Last updated Oct 15, 2026"** | **NEW source — see below** |

**Observability pricing announcement rechecked (CF-P6).** The announcement is
unchanged on the Workers pricing page: *"Beginning December 1, 2026, Workers
Logs will use Cloudflare Observability pricing. Until then, the Workers Logs
pricing in this table applies."* The linked Observability pricing page
(updated **2026-10-15**, i.e. after the §E snapshot was assembled) now spells
out the **Free-plan terms of the change**: Free = **0.5 GB/day included
ingestion + 7-day retention included; additional ingestion NOT available on
Free** (Cloudflare stops ingesting new data at the daily allowance and resumes
at 00:00 UTC; storage not separately metered); existing Free/Pro/Business
accounts **migrate automatically on 2026-12-01**; Log Explorer datasets remain
a separate $1/GB, 30-day pool. **Preliminary read only: the Free posture still
appears to yield 0 CZK after the change (hard cap, no paid overage available
on Free) — an announced change not yet in effect. ⚠ MUST re-verify on the day
and again for any access on/after 2026-12-01 (§D step 3); CF-P6/CF-P13
statuses unchanged.**

### F.3 Supabase terms/backup/region recheck (2026-10-22)

| Doc | Snapshot (2026-10-21) | Rechecked 2026-10-22 | Difference |
|---|---|---|---|
| Terms of Service (SB-P1) | Version 2 — May 6, 2026 | **version badge not re-renderable this session** (client-side) | honest gap — last verified 2026-10-21; remains ⚠ day-of |
| DPA (SB-P2) | Version 1 — August 1, 2026 | "Version 1 — August 1, 2026" | none |
| Pricing / Free limits (SB-P3) | see N-4 | all stated limits confirmed | none |
| Backups (SB-P4) | Free "Not included" | confirmed in pricing feature table + backups docs (N-6) | none |
| Free overage behavior (SB-P5) | see N-7 | confirmed in billing FAQ (N-7) | none |
| Regions (SB-P7) | `eu-central-1` specific | confirmed (N-5) | none |
| Subprocessors (SB-P9) | page updated 2026-10-12 / 2026-06-01 | "Updated October 12, 2026" / "Updated June 1, 2026" | none (PDF still ❌ unparsed) |

### F.4 What section F does NOT change

**C4 remains OPEN and §D steps 1–9 all remain unchecked.** Accounts existing
is not verification: billing/payment-method status is unverified, subprocessor
and transfer reviews are incomplete, no eligibility determination was made, no
in-flow region check happened, and no console evidence was collected — all
still pending. Unchanged gates: **6.0f UNCHECKED · 6.1–6.18 unchecked ·
Q1/D5b UNRESOLVED and blocking for 6.0d (Q1-D = preference subject to
eligibility verification only) · C2 §12 OPEN · 6.0d/6.0e NOT AUTHORIZED ·
ORD-A sequencing-only · 0 CZK/month unchanged.** No agent created, modified or
deleted any account or resource, touched billing or payment methods, changed
OAuth configuration, deployed anything, or touched Stage 7. No merge, no tag,
no release.

### F.5 Owner-side console observations (2026-10-22, second session — attributed, not agent-verified)

Additional facts reported by the **owner** from their own console sessions on
2026-10-22. Method note: these are **explicitly attributed owner observations**
(manual, first-hand), never agent-verified; **no screenshots, console exports
or account identifiers are stored in GitHub** (per the privacy rule); no agent
logged into any console, called any billing API, created or modified any
resource, or touched any payment method. Recorded because they materially
inform C4 preparation — they change **no status** (F.4 stands).

| # | Owner-observed fact | Verification status |
|---|---|---|
| N-11 | **Cloudflare Workers Free is active on the account with no payment method attached** (owner-reported). | ⚠ Owner observation only — never agent-verified (MCP has no billing scope; N-8 unchanged). Still a §D day-of item. |
| N-12 | **A Supabase "Personal Free" organization exists with zero projects** (owner-reported; **no project was created**). | ⚠ Owner observation only — never agent-verified (no console session, no API call). The 2-project Free cap (N-4) is therefore not consumed. |
| N-13 | **Frankfurt (`eu-central-1`) is selectable in the Supabase project-creation form** (owner-reported, in-flow). | ⚠ Partially informs SB-P7/§D step 6 (in-flow availability of the documented specific region) but **does NOT close it**: no agent verification, no project created, no console evidence stored. Remains ⬜. |

**Explicit non-claims:** Supabase **billing/payment status remains unverified**
(N-8 unchanged — an organization existing is not billing evidence); **project
creation remains unperformed and unauthorized** (6.0d NOT AUTHORIZED; the
owner's empty organization is not a Goldberg resource); nothing here closes
§D steps 1–9. Unchanged gates: **C4 OPEN · 6.0f UNCHECKED · 6.1–6.18
unchecked · Q1/D5b UNRESOLVED and blocking · C2 §12 OPEN · 6.0d/6.0e NOT
AUTHORIZED · ORD-A sequencing-only · 0 CZK/month.**

---

**C4 status after this document: OPEN.** This snapshot is preparation only; the
final C4 verification (section D) must run immediately before any separately
authorized provisioning. The 2026-10-22 preliminary verification (§F) rechecked
sources and recorded new account facts — it changed **no status** and closed
**no item**: §D steps 1–9 all remain unchecked and C4 stays OPEN. §F.5 adds
owner-attributed console observations (N-11–N-13) with the same effect.

created by stokonn with love <3


## G. Continuation review — 2026-10-22 (documentary progress only; C4 OPEN)

See [READINESS-CONTINUATION.md](READINESS-CONTINUATION.md) for current findings,
sources, remaining owner actions and verification. This dated addendum supersedes
only the earlier retrieval states: SB-P9/C3 Q8 current PDF now text-parsed, both
pages (22 entries), while applicability/location and notification review remain
open; SB-P10 Trust Center public page opened, but TIA not retrieved and Free-plan
access still unverified. X-6 official GDPR text remains unavailable (JavaScript
challenge on EUR-Lex ELI/HTML/PDF; EDPS 403); X-7 not retried. X-2 EC adequacy
guidance re-read, but recipient certification and current legal-status assessment
remain open. No C4 §D checkbox changes. N-11–N-13 owner observations preserved;
Supabase billing/payment posture remains unverified; no configuration changed.

created by stokonn with love <3


## H. Existing owner-created Supabase project — 2026-10-22

### Purpose, scope and attribution

Reconcile the existing project without creating another or changing hosted state.
Scope: Stage 6 documentation and one Issue #31 C4 bullet. The owner first reported
project existence/Free/Frankfurt, then supplied **externally verified observations
from ChatGPT's connected Supabase integration on 2026-10-22**. This coding agent
**did not execute those metadata, inventory or health checks**; their source is the
owner-relayed ChatGPT integration verification. Preserve that attribution in every
future checkpoint. Project/organization display names are retained only in ignored
local notes; no account identifiers or secrets are committed to public GitHub.

| Item | Dated observation / source | Current verification status |
|---|---|---|
| Project existence, name and organization | ChatGPT connected Supabase integration, 2026-10-22; matches the owner-supplied display names | VERIFIED EXTERNALLY; not checked by this coding agent |
| Organization plan | free / tier_free | VERIFIED EXTERNALLY; not proof of payment-method absence |
| Project region | eu-central-1 (Frankfurt) | VERIFIED EXTERNALLY; not an EU-only processing/compliance claim |
| Project status | ACTIVE_HEALTHY | VERIFIED EXTERNALLY at the recorded date; not a continuous health guarantee |
| PostgreSQL version | 17.11 | VERIFIED EXTERNALLY |
| Public tables | 0 | VERIFIED EXTERNALLY; scope is public schema, not all database namespaces |
| Recorded migrations | 0 | VERIFIED EXTERNALLY; no Goldberg migration/application rollout is inferred |
| Edge Functions | 0 | VERIFIED EXTERNALLY; not a statement about Cloudflare or hosted Auth providers |
| Direct database accessibility from this coding agent | No connection/query executed by this agent | UNVERIFIED; not a failed connectivity test |
| Non-public Goldberg schema/configuration | No broader schema inventory supplied | UNVERIFIED; empty public schema does not prove absence of every database object |
| Auth providers/security | No sanitized Auth/security metadata supplied | UNVERIFIED: Google enablement, redirect/origin settings, MFA, SSL/RLS and related settings |
| Billing/payment methods/add-ons/quota usage | Free organization confirmed externally; no other billing/usage evidence supplied | UNVERIFIED; Free does not establish no card, no paid add-ons or zero accrued costs |

**This session's own discovery:** Supabase plugin reported installed/enabled by
Plugin Management; zero callable Supabase tools are exposed, including after
rechecking. Installation metadata is not project access. This agent issued no
Supabase project/DB request, SQL, user-row read, key request, login, token inspection
or hosted configuration operation. No secrets were requested or exposed. The
session limitation and the separate ChatGPT verification are distinct evidence.

**Implementation/authorization record:** production backend NOT IMPLEMENTED;
Cloudflare Worker NOT DEPLOYED in the last verified record. Google OAuth NOT
CONFIGURED in Goldberg's implementation record; hosted Google settings UNVERIFIED.
Real-user collection NOT AUTHORIZED. Existing project does not complete or authorize
6.0d. No project deletion/recreation, migrations, Auth changes or replacement project.

### Reconciliation tasks (documentation only)

- [x] Recover eab990b, latest Issue/checkpoints and unchanged remote Stage 6/7 heads.
- [x] Discover this session's integration availability without accessing credentials/user rows.
- [x] Record externally verified ChatGPT metadata separately from this agent's checks and remaining unknowns.
- [x] Publish only the owner-approved C4 bullet and the subsequent explicitly authorized external-verification correction; exact body readback passed.
- [x] Final documentation/policy consistency verification: five host scripts, link/scope/attribution and unchanged-gate checks PASS.
- [x] Prepare the verified documentation checkpoint for the authorized dedicated-branch commit/push.

Issue #31 now has 9,276 Unicode characters / 52 lines; verified publication time
2026-10-22T19:41:30Z. Exact UTF-8 body SHA-256:
`970b5a9e1dbcc632fba28403d440a4ec6183ddc6b48720d0dd30784566699328`.
The correction changed only C4's observation text; all 63 full comment records,
Issue labels/milestone/state and Project In Progress were preserved. No new comment.
The brief earlier owner-report-only update was corrected after the external
verification arrived; its now-superseded inventory is not the current baseline.

Next safe task: through an already authorized session exposing Supabase tools,
read only sanitized Auth/security flags, database metadata and billing/usage
indicators for the existing project. No user rows or secret-bearing configuration;
no new credentials, authentication, scope changes or resource modifications.
Otherwise leave each unknown UNVERIFIED. Q1 professional-review handoff and C4
TIA/transfer/retention gaps remain independently actionable documentary work.

**Gates unchanged:** Q1/D5b unresolved/blocking, C4 OPEN, C2 §12 OPEN, 6.0f OPEN
and UNCHECKED; 6.0d/6.0e NOT AUTHORIZED; 6.1–6.18 UNCHECKED; ORD-A sequencing
only; 0 CZK/month. All §D checkboxes unchanged. N-12 zero-project observation is
historical, superseded for current inventory only. No Stage 7 changes.

created by stokonn with love <3
