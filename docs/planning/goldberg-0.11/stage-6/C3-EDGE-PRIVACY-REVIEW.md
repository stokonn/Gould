# Stage 6 — C3 edge privacy & data-processing review (documentation-only)

**Task:** condition **C3** of the task 6.0f gate (D8b zero-cost BFF proof):
the **D5b/D5c edge-location, privacy and international-transfer review**
explicitly covering risk **R2 (edge processing location)** from
[BFF-FEASIBILITY.md](BFF-FEASIBILITY.md) §7.

**Scope of this review:** the owner-approved Stage 6 architecture —
**Cloudflare Workers Free** (Web Admin static assets + `/v1` BFF, one HTTPS
origin) in front of **Supabase Free, Frankfurt `eu-central-1`** (PostgreSQL +
Auth) — evaluated for a credible privacy-compliant deployment path under the
permanent 0 CZK/month contract.

> **This is a technical and documentary assessment, not a legal certification.**
> No claim of GDPR compliance is made or implied. No personal data was
> collected. No account was created, no provider was provisioned, nothing was
> deployed. This review does **not** close any legal, controller/operator or
> provisioning gate (§9–§11).

> **RECONCILIATION NOTE (2026-10-21, later session — no finding, risk row or
> question in this review is answered or changed by it):** (1) the owner chose
> this cloud architecture as the **intended deployment path** instead of the
> temporary local-only Option C — see
> [CLOUD-READINESS-PLAN.md](CLOUD-READINESS-PLAN.md) §1; the review's scope and
> verdict are unchanged. (2) Statements in this review that the project owner is
> a minor (§1, risk P7, §9 Q1/Q10, §13 item 2) are **reclassified as an
> unverified assumption carried into this document — not a verified current
> fact**; they remain here as historical document context and may not be cited
> as established fact. No age, identity or family detail may be requested or
> recorded. The legal-capacity, eligibility, authority-to-bind and
> controller-role questions remain **open and blocking for provisioning**
> regardless of any assumption in either direction (D5b — Q1 research).

**Review date:** 2026-10-21 · **Method:** current official provider
documentation, EU institutional guidance (§12 sources, all accessed
2026-10-21). Evidence is classified as **[Verified]** (directly stated in a
primary source), **[Interpretation]** (reasonable reading, not contractually
guaranteed) or **[Unresolved]** (cannot be settled from public documentation;
listed in §9).

---

## 1. Executive summary

1. **A credible privacy-compliant deployment path exists on the Free tiers —
   with mandatory conditions and no claim of compliance.** Both providers offer
   GDPR processor terms at 0 CZK: Cloudflare's Data Processing Addendum is
   **incorporated by reference** into the Self-Serve Subscription Agreement
   (no paid plan or separate signature required), and Supabase's DPA (which
   embeds the EU Standard Contractual Clauses) takes effect through acceptance
   of the Supabase Terms. International transfers are covered by existing
   mechanisms (EU-U.S. Data Privacy Framework certifications, EU SCCs with
   supplementary measures). **No essential privacy requirement identified in
   this review is blocked by the Free tier** — i.e., nothing here requires a
   paid Cloudflare or Supabase feature to mitigate.
2. **Frankfurt hosting does not mean EU-only processing — verified.**
   Supabase explicitly states it **does not guarantee** data never leaves the
   selected region (four named exception categories: account information,
   globally distributed support access, usage/telemetry, CDN delivery), and
   its primary processor is **Supabase Pte. Ltd. (Singapore)** with US
   affiliate support flows, both covered by SCCs. Cloudflare Workers execute
   at whichever of its 330+ edge cities receives the request; **metadata about
   that activity is processed in Cloudflare data centers in the US and
   Europe**. The approved architecture therefore **inherently involves
   processing outside the EU and international transfers**, lawful only via
   the providers' transfer mechanisms. This must be reflected honestly in the
   future privacy notice (gate 6.16) — "EU-only" must never be claimed.
3. **Workers Logs is the largest concrete, configurable exposure** and is
   **enabled by default for newly created Workers** **[Verified]**: invocation
   logs (request method, URL, response and metadata), custom `console.log`
   output and uncaught exceptions are stored in the developer's Cloudflare
   account (Free tier: 200,000 log events/day, 3-day retention). Mitigations
   exist entirely within the Free tier (`observability.enabled`,
   `head_sampling_rate`, `invocation_logs=false`, and strict log-content
   discipline) — §8.
4. **Free-tier constraint, reported explicitly (not a compliance blocker):**
   Cloudflare's Data Localization Suite / Customer Metadata Boundary (which
   keeps customer-identifying logs inside one region) is an **Enterprise**
   offering **[Verified via Cloudflare service-specific terms]**, and Workers
   Free offers no control over which edge locations execute requests. The
   project therefore **cannot purchase EU-only edge metadata processing** at
   0 CZK; it must instead rely on transfer mechanisms + minimization. If the
   owner ever requires strict EU-only edge processing, that is a **budget /
   architecture decision** — it must be reported as a blocker, never silently
   absorbed.
5. **The deferred controller/operator question (D5b) now has a concrete new
   dimension — the project owner is a minor.** Cloudflare's DPA requires the
   accepting party to warrant "full legal authority to bind Customer";
   GDPR controller status carries legal obligations (privacy notice, data-subject
   rights, accountability). **Who can lawfully hold these accounts and act as
   controller is a question for an appropriate adult/professional review**
   (§9 Q1). No identity, contact or company detail is invented here.
6. **C3 verdict (§10): DOCUMENTARY REVIEW COMPLETE — CONDITIONALLY VIABLE.**
   The evidence base for C3 is complete for a documentation-level gate. The
   residual items are **legal/operator questions** (carried to gates 6.0b /
   6.16 and the pre-launch review) and **pre-provisioning verifications**
   (§11). Task **6.0f stays unchecked**: C4 (terms re-verification) and the C2
   §12 production-runtime verification remain open, and no legal gate is
   closed by this review.

## 2. Architecture and data-flow diagram

Approved topology (C1, 2026-10-21 — architecture level only; nothing exists):

```
 [Android app (community feature, optional)]
        |  HTTPS/TLS
        v
 ┌──────────────────────────────────────────────┐
 │ Cloudflare Workers Free (single HTTPS origin)│
 │  · static Web Admin assets                   │
 │  · /v1 BFF (stateless; no KV/D1/R2/DO)       │
 │  · session cookie mint/check, CSRF,          │
 │    JWT verification (JWKS from Supabase),    │
 │    default-deny authz, response shaping      │
 │  · executes at the Cloudflare edge location  │
 │    that receives the request (any of 330+)   │
 └───────────────┬──────────────────────────────┘
                 |  HTTPS/TLS (fetch from Cloudflare egress IPs)
                 v
 ┌──────────────────────────────────────────────┐
 │ Supabase Free — AWS eu-central-1 (Frankfurt) │
 │  · Auth (GoTrue): Google sign-in, sessions,  │
 │    JWKS, account records                     │
 │  · PostgreSQL: community submissions,        │
 │    moderation decisions, audit chain         │
 └──────────────────────────────────────────────┘
```

Boundary crossings (each analyzed in §3):

- **Client → Cloudflare edge:** request arrives at whichever Cloudflare data
  center anycast routing selects — **not pinnable to the EU on Workers Free**.
- **Cloudflare edge → Cloudflare core:** invocation metadata/logs flow to
  Cloudflare core data centers in the **US and Europe**.
- **Cloudflare edge → Supabase Frankfurt:** outbound `fetch()` to
  `*.supabase.co` (PostgREST/GoTrue/JWKS) — data lands in `eu-central-1`;
  the *egress point* may be outside the EU.
- **Supabase → Supabase Pte. Ltd. (Singapore) / Supabase, Inc. (US):**
  account, support, telemetry flows — covered by SCCs (§5).

**Strict Goldberg boundary (preserved, non-negotiable):** no personal practice
journal, private practice statistics, recordings or local practice history is
ever sent to this backend. The client sends only allowlisted community
submission fields and authentication material (DATA-MODEL.md §7). C3 confirms
this boundary also holds server-side: nothing in the Cloudflare/Supabase flow
needs practice data to function.

## 3. Data categories and processing locations

**[Verified]** = stated in a primary source; **[Interpretation]** = reasonable
reading; **[Unresolved]** = see §9.

| Category | Crossing | Where processed / stored | Basis |
|---|---|---|---|
| **IP address + network metadata** (TLS SNI, URL, method, timing) | Client → edge (any of 330+ cities, location not pinnable on Free) | Edge transiently; **metadata processed in Cloudflare core DCs, US + Europe**; most often as IP addresses in log data, "for a limited period of time" | Cloudflare GDPR FAQ; Privacy Policy §2 (End Users) **[Verified]** |
| **Session cookie (`sid`, HttpOnly/Secure/SameSite=Strict) + CSRF value** | Client → edge on every admin request | Transient in Worker memory; **must never be logged**; no edge storage in design | D8b; C2 S19 **[Verified design / Interpretation on logging]** |
| **Google OAuth credentials / Supabase access+refresh tokens** | Client ↔ Supabase Auth (via BFF where applicable) | `eu-central-1` (Auth hosted in project region per Supabase residency FAQ) | Supabase residency FAQ **[Verified]** |
| **Account identifiers** (Supabase `auth.users.id`, Google subject) | Edge ↔ Supabase | `eu-central-1`; may appear in Cloudflare logs **only if the Worker logs them — prohibited by §8** | DATA-MODEL **[Verified design]** |
| **Submission metadata** (community text drafts, revisions, hashes) | Edge → Supabase | `eu-central-1` Postgres + backups in region | DATA-MODEL.md; residency FAQ **[Verified]** |
| **Moderation decisions + audit events** | Edge → Supabase | `eu-central-1` Postgres | DATA-MODEL.md §4 **[Verified design]** |
| **Security/audit events** (login, grant, suspend) | Supabase Auth logs / DB rows | `eu-central-1`; **dashboard auth-log retention [Unresolved — Q6]** | Supabase docs **[Partly verified]** |
| **Operational logs** (Worker invocation logs, `console.log`, exceptions) | Worker → Workers Logs | Stored in **Cloudflare account** (developer-side logs; Free: 200k events/day, **3-day retention**); Cloudflare processes customer logs as **processor** | Workers Logs docs; Privacy Policy §6 **[Verified]** |
| **Error reporting** (stack traces, request context) | Worker → Workers Logs; client → BFF | Must be redacted (§8); no internals to clients (C2 S15) | **[Verified design]** |
| **Backups** | Supabase internal | Frankfurt region per residency FAQ; **Free-plan automated backup availability [Unresolved — Q7]** | **[Partly verified]** |

## 4. Cloudflare Free findings

1. **Edge execution is global and location is not controllable on Free.**
   Workers run "on Cloudflare's global network — a growing global network of
   thousands of machines distributed across hundreds of locations" (How
   Workers works, 2026-09-18); the network spans "more than 330 cities"
   (Cloudflare GDPR FAQ). **[Verified]** No Workers Free setting restricts
   execution to EU locations; the Data Localization Suite (Regional
   Services, Customer Metadata Boundary, Geo Key Manager) is described in
   Cloudflare's service-specific terms as configurable **"on your Enterprise
   account(s)"** **[Verified]** → **unavailable at 0 CZK**.
2. **Transient edge processing.** For core services "the vast majority of
   data that transits Cloudflare's network stays on Cloudflare's Edge
   servers"; **metadata** about that activity "is processed on behalf of our
   customers in our data centers in the United States and Europe", and some
   log data "contains extremely limited personal data, most often in the form
   of IP addresses", processed "for a limited period of time" (GDPR FAQ).
   **[Verified]** Concrete duration for Workers Free customer logs is not
   published → **[Unresolved Q3]**.
3. **Controller/processor split.** Cloudflare is a **processor** for Customer
   Logs, Administrative User Audit Logs, some account-settings data, and
   customer/end-user content transiting the network; it is a **controller**
   for its own account/billing data and for Network Data it derives
   (Privacy Policy §6, §2). **[Verified]** → the Goldberg operator is the
   controller for everything the BFF processes; Cloudflare is the processor
   for the request/logs path.
4. **Workers Logs: enabled by default for newly created Workers**
   (Workers Logs docs, 2026-10-12). **[Verified]** Free tier: **200,000 log
   events/day, 3-day retention**. Invocation logs record the request method,
   URL, response and metadata; custom logs capture everything the Worker
   `console.log`s; per-request log cap 256 KB. Configurable entirely within
   Free: `observability.enabled`, `head_sampling_rate` (0–1),
   `observability.logs.invocation_logs=false`. **[Verified]** Which exact
   request headers appear in invocation logs is not enumerated publicly →
   **[Unresolved Q2]** — mitigation treats headers/cookies as never-log.
5. **Free-plan limits relevant to privacy posture** (Workers limits docs,
   2026-10-12): 100,000 requests/day with **configurable fail-closed
   behavior** for quota exhaustion (Error 1027; fail-closed recommended "for
   security-critical Workers"); 10 ms CPU/invocation (matches C2's measured
   2.03 ms average); 50 subrequests/request (enough for Auth + PostgREST +
   JWKS calls); 64 variables/secrets per Worker (enough for the small secret
   set). **[Verified]** Nothing in the privacy posture depends on a paid
   limit.
6. **DPA + transfer mechanisms at 0 CZK.** The Cloudflare Data Processing
   Addendum (**Version 6.4, effective 2026-04-03**) is **incorporated by
   reference into the Self-Serve Subscription Agreement**; for self-serve
   customers "no action is required to ensure that the appropriate
   cross-border data transfer mechanisms are in place" (Cloudflare GDPR FAQ).
   Transfers EEA→US rely on Cloudflare's **EU-U.S./UK/Swiss DPF
   certifications**, falling back to **EU SCCs (Module Two, controller→processor)
   with supplementary measures** if the DPF lapses. **[Verified]**
7. **Subprocessors.** Cloudflare maintains a public list
   (`cloudflare.com/gdpr/subprocessors/`, last updated **2025-10-01**): for
   the **Developer Platform** (which Workers belongs to) the listed
   third-party subprocessors are **Google LLC** (EEA, US, Australia, India)
   and **Oracle America, Inc.** (US, EEA, UK, Japan, Australia, Canada,
   Singapore), plus Cloudflare group entities (US, UK, DE, FR, NL, PT, JP,
   …) for account/engineering support. **[Verified]** The DPA gives
   **30 days' notice** of new subprocessors with a customer **objection
   right** (10 days). **[Verified]** The precise role/location combination
   for a plain Workers-Free deployment is **[Unresolved Q4]**.
8. **Government-access posture.** Cloudflare's DPA §8 commits to notifying
   the customer of third-party legal process unless legally prohibited, to
   directing requesters to the customer, and to pursuing legal remedies for
   conflict-of-law requests; it publishes semiannual Transparency Reports.
   **[Verified]** This is a contractual commitment, not a guarantee against
   all access — relevant to the transfer-risk analysis in §6.

## 5. Supabase Frankfurt findings

1. **Region pinning is real but scoped.** `eu-central-1` (Central EU,
   Frankfurt) is an available **specific region**. Supabase states: "your
   core database and backups stay in your selected region" and "Your
   PostgreSQL database, as well as Auth service and Storage objects are
   hosted in that region, alongside backups." **[Verified]** Supabase warns
   that general "Europe" grouping includes London/Zurich (non-EU) — choosing
   the **specific** `eu-central-1` region is the correct D5c choice, and the
   docs state plainly: "Region selection is a data-location control, not
   proof of regulatory compliance." **[Verified]**
2. **Explicit non-guarantee of region-exclusive processing.** Supabase's
   Data Residency and Transfers FAQ (v1, 2026-10-06) states "No" to "Does
   Supabase guarantee my data never leaves my selected region?", and lists
   four exception categories: **(a) Customer Account Information**
   (processed centrally, Supabase is controller); **(b) Customer Support
   Requests** — support staff are **distributed globally**, may access
   project data when support is requested or for security response, access
   is logged and least-privilege; **(c) Usage Information** — telemetry/logs
   processed outside the region to operate the platform ("logs are ingested
   and stored at EU-based data centers"), not application data; **(d)
   Content Delivery** via global CDN (Storage — not used by Goldberg).
   **[Verified]** Goldberg's exposure is (a) always, (b) only if support is
   ever requested, (c) always, (d) not applicable.
3. **Processor identity and transfers.** **Supabase Pte. Ltd. (Singapore)**
   is the primary data processor; **Supabase, Inc. (US)** provides technical
   support/operations; data flows to the US affiliate only when necessary.
   Transfers to Singapore and the US are made under the **Standard
   Contractual Clauses incorporated in the Supabase DPA**, supported by a
   published **Transfer Impact Assessment** (trust.supabase.io —
   **[Unresolved Q5: confirm free-tier access at provisioning]**).
   **[Verified]**
4. **DPA (Version 1, effective 2026-08-01).** Customer = controller,
   Supabase = processor; **acceptance of the Supabase Terms has the same
   effect as signing the SCCs** (Module Two controller→processor; Irish law;
   Irish courts); subprocessor list with **30 days' notice and a 5-day
   objection window**; security-incident notification "without undue delay,
   and where feasible, within **48 hours**"; deletion/return rights at
   contract end; Schedule 1 technical measures (AES-256 at rest with
   per-project keys in FIPS 140-2 HSMs, TLS 1.2+ in transit, audit trails,
   project segmentation). **[Verified]** Project deletion → data deleted
   within **30 days**; account closure → personal information deleted up to
   **60 days** later (residency FAQ). **[Verified]**
5. **Free-plan limitations that shape privacy/ops** (Production Checklist;
   Backups docs):
   - Projects may be **paused after low activity in a 7-day period**
     (restorable from the dashboard) — already accepted under D4b with
     fail-closed degradation. **[Verified]**
   - **Automated daily backups are documented for Pro/Team/Enterprise;**
     free-tier projects are advised to **regularly export via `supabase db
     dump`**, and "Database backups are not available for download for Free
     Plan projects." **[Verified]** Whether any automated backup exists for
     a Free project is **[Unresolved Q7]** → drives the §11 export
     obligation and honest RPO statements in ZERO-COST-OPS.md.
   - Auth email-sending endpoints default to **2 emails/hour** without
     custom SMTP; OTP defaults are configurable. **[Verified]**
   - MFA on the Supabase account, SSL enforcement and network restrictions
     are available on Free and are recommended in the Production Checklist —
     all fit the approved D8c posture and 0 CZK. **[Verified]**
6. **Certifications.** Supabase states SOC 2 Type 2, ISO 27001 and
   HIPAA-readiness (Trust Center / footer claims). **[Verified as claims;**
   the underlying reports were not retrieved — acceptable for a
   documentation gate, noted for §11.]

## 6. DPA / subprocessors / transfers analysis

**Contract chain at 0 CZK:**

| Layer | Instrument | Effect | Status |
|---|---|---|---|
| Cloudflare | Self-Serve Subscription Agreement + **DPA v6.4 (2026-04-03)** incorporated by reference | Art. 28 processor terms; EU SCCs Module Two; DPF fallback; subprocessor notice/objection | **[Verified]** — takes effect by using the service; no signature or payment needed |
| Supabase | Terms + **DPA v1 (2026-08-01)**, acceptance = SCC signature | Art. 28 processor terms; EU SCCs Module Two/Three, Irish law; UK/Swiss addenda | **[Verified]** — takes effect on account/terms acceptance |
| EU → US transfers | EU-U.S. DPF adequacy decision (European Commission, **2023-07-10**, Impl. Dec. 2023/1795); EO 14086 safeguards; **EDPB Recommendations 01/2020** (v2.0, 2021-06-18) on supplementary measures | Free flow to DPF-certified importers; SCCs + supplementary measures as parallel/backstop mechanism | **[Verified]** (Cloudflare DPF-certified; Supabase uses SCCs + TIA rather than DPF — both paths lawful mechanisms) |
| Subprocessors | Cloudflare public list (2025-10-01: Google, Oracle for Developer Platform; group entities); Supabase public list (PDF **2026-10-12** — content not parsed here, **[Unresolved Q8]**) | Both DPAs grant general authorization with 30-day advance notice + objection right (CF objection window 10 days; Supabase 5 days) | **[Verified]** process; Supabase list contents to be reviewed at provisioning |

**Transfer reality of the approved architecture [Interpretation, grounded in
§4–§5 verified facts]:**

1. **Inevitable transfer #1 — end-user request metadata, anywhere → US/EU
   Cloudflare core.** Even with a Frankfurt database, every admin request's
   metadata (IP, URL, timing) is processed in Cloudflare core data centers in
   the US and Europe. Lawful basis: Cloudflare DPA (SCCs/DPF) with Goldberg's
   operator as exporting controller. **Cannot be eliminated on Workers
   Free** (no Metadata Boundary/Regional Services).
2. **Inevitable transfer #2 — support/telemetry flows, Supabase.** Account
   data (controller-side Supabase) and any support-triggered data access
   involve Singapore/US entities under SCCs. Minimized by: never requesting
   support that requires data access where avoidable; awareness that support
   access is logged.
3. **Not a transfer — application data at rest.** Submissions, moderation
   decisions, audit rows, Auth records and backups rest in `eu-central-1`.
4. **The Worker's egress to Supabase may originate outside the EU** (edge
   location is uncontrolled) — the *data payload* still lands in Frankfurt;
   the egress IP is Cloudflare's, not the end user's. **[Interpretation]**

**Consequence for the future privacy notice (6.16, not written now):** it
must state that request metadata is processed globally including the US, name
both processors, reference the DPF/SCC mechanisms, and must **never** claim
EU-only processing or GDPR compliance certification. No notice text is
drafted here (D5a contact and controller identity are not yet decided).

## 7. Privacy and security risk matrix

| ID | Risk | Likelihood/impact | Tier-free mitigation | Residual |
|---|---|---|---|---|
| P1 | End-user IP/URL processed in US Cloudflare core (transfer) | Certain / low–moderate (minimal data) | Data minimization; no unnecessary PII in URLs; DPA/SCC chain; honest notice | Accepted; disclosed at 6.16 |
| P2 | **Workers Logs retention of request metadata/URLs/cookies if misconfigured or default-on** | High if unmanaged / moderate | §8: disable/minimize observability, `invocation_logs=false`, never log headers/cookies/tokens; log only synthetic IDs | Must be re-verified post-provisioning (Q2) |
| P3 | Supabase support staff (globally distributed) access to project data on support request | Only on request / moderate | Avoid support that requires data access; access is logged per DPA; prefer self-service; MFA-protected admin (D8c) | Accepted under DPA |
| P4 | Subprocessor change introducing new processing locations | Uncertain / moderate | Subscribe to both providers' subprocessor notifications; calendar the 10-day (CF) / 5-day (Supabase) objection windows | Operational obligation — added to §11 |
| P5 | Free-tier project pause exposing stale data (D4b) | Moderate / low | Already accepted: graceful denial, fail-closed, core app unaffected | Accepted (D4b) |
| P6 | No downloadable/verifiable Free-tier backup → data loss or forced erasure-delay | Uncertain / moderate–high (integrity) | §11: scheduled `db dump` exports to the operator's own storage (0 CZK); honest RPO in ZERO-COST-OPS | Operational obligation |
| P7 | Account-holder legal capacity (minor owner) invalidating account/DPA formation | Uncertain / **high (legal)** | **Out of technical scope — adult/professional review (Q1); controller/operator determination (D5b) is the natural place to resolve** | **Blocking for provisioning until resolved** |
| P8 | Edge location outside EU processes admin-session requests | Certain / low | Cookies are opaque tokens; no PII in URL; TLS everywhere; server-side authz | Accepted; disclosed |
| P9 | Cloudflare DPF certification lapses (Schrems-style invalidation) | Low / moderate | DPA auto-falls back to EU SCCs + supplementary measures; standing terms re-verification (C4/6.0d) | Monitored |
| P10 | Practice-data boundary breach via logging or error paths | Low / high (core value) | Never-log discipline; allowlisted logging; C2 S15/S19 patterns carry to production; no practice data leaves the device by design | Enforced by 6.12 matrix |

## 8. Free-tier mitigations (all available at 0 CZK)

None of the following requires a paid plan; none silently changes providers,
architecture or budget. Items marked ✅ are **already satisfied by the
approved design/C2**; items marked 📋 are **obligations to carry into
implementation (6.1–6.9) and provisioning (6.0d)**.

1. ✅ **Data minimization at the boundary** — strict endpoint
   request/response allowlists (API-CONTRACTS.md); no unnecessary
   identifiers; general-audience, minimal-data community (D5f).
2. ✅ **Strict Goldberg boundary** — no journal/statistics/recordings/local
   history ever crosses to the backend (DATA-MODEL.md §7).
3. ✅ **No edge storage** — the BFF is stateless; no KV/D1/R2/Durable
   Objects usage; nothing personal persists at the edge.
4. 📋 **Redacted, minimal Workers Logs** — set `observability` explicitly in
   wrangler config (never rely on dashboard defaults); consider
   `head_sampling_rate` < 1 and `invocation_logs: false`; **never** log
   cookies, tokens, JWTs, emails, IPs or request bodies; log only synthetic
   request IDs + coarse outcomes. Retention is capped at 3 days on Free
   anyway. Re-verify actual invocation-log fields after provisioning (Q2).
5. ✅ **Fail-closed handling** — quota exhaustion → Error 1027 fail-closed
   route mode; backend failure → truthful 503 (C2 S14–S16); kill switch.
6. ✅ **Secure cookies + restricted origins** — HttpOnly/Secure/
   SameSite=Strict session cookie, CSRF token, strict CSP, no ACAO echo
   (C2 S12/S13/S19); restricted origin allowlist.
7. ✅ **Encrypted transport end-to-end** — TLS to the edge (universal SSL)
   and edge→Supabase (TLS 1.2+ per Supabase DPA Schedule 1); AES-256 at
   rest in Supabase with per-project keys.
8. ✅ **Server-side authorization, no privileged credentials in browser JS**
   — Supabase service-role keys exist only as Worker secrets (64-secrets
   Free limit is ample); the browser holds only the session cookie.
9. 📋 **Supabase hardening at provisioning** — choose **specific** region
   `eu-central-1` (not the general "Europe" grouping); enable SSL
   enforcement; MFA on all operator access (D8c); restrict dashboard/DB
   network access where the Free tier allows; keep RLS on for any
   client-reachable table (design keeps PostgREST reachable only from the
   Worker where possible).
10. 📋 **Scheduled exports instead of paid backups** — `supabase db dump`
    on a schedule (Free tier has no downloadable automated backups);
    record honest RPO (up to 24 h, or worse if paused).
11. 📋 **Subprocessor monitoring** — subscribe to Cloudflare's RSS feed and
    Supabase's subprocessor notifications; calendar objection windows.
12. 📋 **Honest external communication at 6.16** — privacy notice names
    both processors, states global edge processing incl. US metadata flows,
    cites DPF/SCCs, never claims EU-only processing or GDPR certification.
    (No text drafted now — controller identity/contact unresolved.)

**Free-tier blocker check (required by the C3 charter):** after full review,
**no identified essential privacy requirement needs a paid feature.** The
one structural limitation — no EU-only edge metadata processing on Workers
Free (Enterprise-only Data Localization Suite) — reduces control but does
not make lawful processing impossible (transfer mechanisms exist). It is
therefore **reported as a constraint to disclose and monitor, not a
blocker**. Should the owner later require strict EU-only edge processing,
that is an explicit budget/architecture decision and must be re-decided by
the owner — never absorbed silently.

## 9. Unresolved legal / operator questions

Carried forward verbatim; none is answered here, none is invented:

1. **Q1 — Controller/operator identity and legal capacity (D5b; heightened by
   the owner being a minor).** Who is the GDPR controller for the community
   backend? Who can lawfully hold the Cloudflare and Supabase accounts and
   accept agreements whose terms warrant "full legal authority to bind
   Customer"? Does an adult/guardian or a legal entity need to be the
   account/controller of record? **Requires appropriate adult/professional
   review before provisioning (6.0d).** This review deliberately makes no
   legal conclusion and invents no identity or contact details.
2. **Q2 — Exact Workers Logs invocation-log field set** (which request
   headers, if any, are captured by default) — cannot be settled from public
   docs; verify empirically in an isolated test Worker **after** the
   (separately authorized) provisioning, or treat all headers/cookies as
   never-log by policy (the design already assumes the latter).
3. **Q3 — Concrete retention duration of Cloudflare customer/request logs on
   the Free plan** beyond Workers Logs' documented 3-day retention (e.g.,
   edge/security metadata Cloudflare retains as controller/processor for its
   own operations). "Limited period of time" is not quantified publicly.
4. **Q4 — Role/location combination of Google/Oracle subprocessors** for a
   plain Workers-Free deployment (listed for the Developer Platform as a
   whole).
5. **Q5 — Supabase Transfer Impact Assessment accessibility** for Free-plan
   customers (trust.supabase.io) — confirm at provisioning.
6. **Q6 — Supabase Auth/dashboard log retention** for login and auth events
   — not quantified in the fetched documentation.
7. **Q7 — Automated backup availability for Supabase Free projects** — the
   docs document daily backups for paid plans and advise free projects to
   self-export; whether any automated Free backup exists is unclear. The
   §8.10 export obligation stands regardless.
8. **Q8 — Full contents of the current Supabase subprocessor list** (PDF,
   2026-10-12) — the list page was verified; the PDF itself was not parsed
   in this review. Review it at provisioning and subscribe to updates.
9. **Q9 — Applicable-law and notice obligations of the eventual controller**
   (e.g., Art. 30 records, Art. 35 DPIA screening, Czech/EU supervisory
   registration questions, D5a contact, retention periods under D5d) — all
   deferred to the pre-launch legal gate by prior owner decision; preserved.
10. **Q10 — Whether the community is directed at minors** (the operator is a
    minor; the audience decision D5f is "general audience") and any
    resulting heightened obligations — question for professional review, no
    conclusion drawn.

## 10. C3 verdict and remaining conditions

**Verdict: DOCUMENTARY REVIEW COMPLETE — architecture CONDITIONALLY VIABLE
for a privacy-compliant deployment path at 0 CZK.**

- **What is settled:** the providers' contract chain (DPA-by-reference /
    DPA-on-terms-acceptance), transfer mechanisms (DPF + EU SCCs +
    supplementary measures), processor identities, region-pinning semantics
    and its explicit limits, log/retention surfaces on both Free tiers, and
    the availability of Free-tier mitigations for every identified
    configurable risk.
- **What this verdict does NOT mean:** it is **not** GDPR compliance, not
  legal sign-off, not authorization to provision or deploy. All legal
  questions in §9 remain open. **D5b (controller/operator) remains deferred**
  by the owner's prior decision and is flagged as **blocking for 6.0d
  provisioning** given Q1.
- **Effect on task 6.0f:** C3 (the D5b/D5c edge-location review, risk R2) is
  **discharged at the documentary level**. **6.0f stays UNCHECKED** because:
  **C4** (terms re-verification immediately before provisioning) is open,
  and the **C2 §12 production-runtime verification** remains an owner-gated
  open condition. C3 closes no legal or provisioning gate.

## 11. Pre-provisioning verification checklist (for the separately authorized 6.0d)

To be executed only under a separate explicit owner instruction, before any
public launch or collection:

- [ ] Resolve **Q1/D5b** — controller/operator of record and legal capacity
      to hold the accounts, via appropriate adult/professional review.
- [ ] Re-verify both providers' **current terms, DPA versions and subprocessor
      lists** on the day of provisioning (C4 — versions will have moved past
      CF DPA v6.4 / Supabase DPA v1 as fetched today).
- [ ] Parse the **current Supabase subprocessor PDF** (Q8); subscribe to both
      notification lists; calendar the 10-day/5-day objection windows.
- [ ] Confirm **Supabase project region = specific `eu-central-1`** (not the
      general Europe grouping) in the actual project creation flow.
- [ ] Confirm **Cloudflare account plan = Free**, no billing method attached,
      no paid add-ons (per C1).
- [ ] Set observability configuration **explicitly in wrangler config**;
      verify in a staging Worker which fields invocation logs contain (Q2);
      confirm no cookies/headers/tokens appear.
- [ ] Verify **SSL enforcement** and MFA (D8c) are enabled; record evidence.
- [ ] Establish the **scheduled `db dump` export** (0 CZK) and record the
      honest RPO in ZERO-COST-OPS.md (Q7).
- [ ] Verify Supabase **TIA accessibility** (Q5) for the record.
- [ ] Route every §9 open question that is legal to the pre-launch review;
      do not provision if Q1 is unresolved.

## 12. Sources (all accessed 2026-10-21)

Primary sources; document titles and access date recorded per the C3 charter.

| # | Document | URL | Accessed |
|---|---|---|---|
| 1 | Cloudflare Docs — *Limits* (Workers Free plan limits; last updated 2026-10-12) | https://developers.cloudflare.com/workers/platform/limits/ | 2026-10-21 |
| 2 | Cloudflare Docs — *How Workers works* (global network, isolates; 2026-09-18) | https://developers.cloudflare.com/workers/reference/how-workers-works/ | 2026-10-21 |
| 3 | Cloudflare Docs — *Workers Logs* (default-on, Free 200k/day, 3-day retention; 2026-10-12) | https://developers.cloudflare.com/workers/observability/logs/workers-logs/ | 2026-10-21 |
| 4 | Cloudflare — *Privacy Policy* (effective 2025-11-04; controller/processor split; transfers) | https://www.cloudflare.com/privacypolicy/ | 2026-10-21 |
| 5 | Cloudflare Trust Hub — *Cloudflare and GDPR compliance* (330+ cities; metadata US/EU; DPF/SCC; Data Localization Suite) | https://www.cloudflare.com/trust-hub/gdpr/ | 2026-10-21 |
| 6 | Cloudflare — *Data Processing Addendum* (v6.4, effective 2026-04-03; SCCs Module Two; Annex 1/2) | https://www.cloudflare.com/cloudflare-customer-dpa/ | 2026-10-21 |
| 7 | Cloudflare — *Sub-Processors for Cloudflare services* (last updated 2025-10-01) | https://www.cloudflare.com/gdpr/subprocessors/cloudflare-services/ | 2026-10-21 |
| 8 | Cloudflare — *Service-Specific Terms* (Customer Metadata Boundary on Enterprise accounts) | https://www.cloudflare.com/service-specific-terms-other-terms/ | 2026-10-21 |
| 9 | Cloudflare Trust Hub — main page (certifications index) | https://www.cloudflare.com/trust-hub/ | 2026-10-21 |
| 10 | Supabase — *Data Residency and Transfers FAQ* (v1, 2026-10-06; four exception categories; SG/US processor; SCCs; retention) | https://supabase.com/legal/privacy-resources/data-residency-and-transfers-faq | 2026-10-21 |
| 11 | Supabase — *Data Processing Addendum* (v1, effective 2026-08-01; SCCs; 48 h incident notice; Schedule 1–3) | https://supabase.com/legal/customer-resources/data-processing-addendum | 2026-10-21 |
| 12 | Supabase — *Subprocessor List* page (PDF dated 2026-10-12 listed; PDF not parsed) | https://supabase.com/legal/customer-resources/subprocessor-list | 2026-10-21 |
| 13 | Supabase Docs — *Available regions* (specific `eu-central-1`; "data-location control, not proof of compliance") | https://supabase.com/docs/guides/platform/regions | 2026-10-21 |
| 14 | Supabase Docs — *Production Checklist* (Free pause; backups not downloadable; auth email limits; MFA) | https://supabase.com/docs/guides/platform/going-into-prod | 2026-10-21 |
| 15 | Supabase Docs — *Database Backups* (daily backups documented for paid plans; free-tier export advice; PITR paid) | https://supabase.com/docs/guides/platform/backups | 2026-10-21 |
| 16 | Supabase Docs — *GDPR compliance and Supabase* (shared responsibility; DPA pointer) | https://supabase.com/docs/guides/security/gdpr-compliance | 2026-10-21 |
| 17 | European Commission — *EU-US data transfers* (DPF adequacy 2023-07-10; EO 14086 safeguards apply to all transfers) | https://commission.europa.eu/law/law-topic/data-protection/international-dimension-data-protection/eu-us-data-transfers_en | 2026-10-21 |
| 18 | EDPB — *Recommendations 01/2020 on measures that supplement transfer tools* (v2.0 final, 2021-06-18; landing page; PDF not parsed) | https://www.edpb.europa.eu/our-work-tools/our-documents/recommendations/recommendations-012020-measures-supplement-transfer_en | 2026-10-21 |

Notes: the EDPB *Information note on data transfers to the US after the 2023
adequacy decision* is referenced via source #5 but its PDF was not retrieved;
Supabase's Trust Center/TIA (source #10) was not opened (Q5). No source was
misrepresented as read in full where only a landing page was retrieved.

## 13. Exact next owner decisions

1. **Proceed to C4** (terms re-verification for both providers immediately
   before any provisioning) — documentation-only, can be prepared on request.
2. **Resolve D5b/Q1 via an appropriate adult/professional review** before any
   6.0d provisioning: who is the controller/operator of record, and who may
   lawfully hold the provider accounts given the owner is a minor. No
   provisioning should be authorized until this is settled.
3. **Note and accept (or reject) the disclosed constraint:** EU-only edge
   metadata processing is not achievable on Workers Free; the architecture
   relies on DPF/SCC transfer mechanisms + minimization. Owner may accept
   this (recommended: it is lawful and standard) or re-open the architecture
   discussion — explicitly, not silently.
4. **Keep collection embargoed** until the privacy/terms/contact/licensing
   gate (6.16, D5) is satisfied by separate review. Nothing published, no
   real personal data collected.

---

created by stokonn with love <3
