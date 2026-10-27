## Current Stage 6 — public read-only backend verified; integrated baseline next

Purpose/scope: existing public catalogue GET API on Worker/Hyperdrive/Supabase,
with practice/history/drafts/backups offline and community/OAuth/writes disabled.

- [x] Owner authorization6435585794; proportionate catalogue Article6(1)(f)
  assessment; separate support/rights bases; incorporated provider arrangements
  reviewed against actual accounts/configuration, with evidence limits disclosed.
- [x] Owner-reported manual Inbox/Spam checks and Tuesday/Friday monitoring,
  first-of-month retention review/private case tracking;90day ordinary-support
  retention adopted with justified exceptions. No agent Gmail access/automation.
- [x] Existing public workers.dev route enabled; previews remainOFF. ONE focused
  actual HTTPS smoke pass:10/10PASS. Catalogue/feed200(empty),cursor/query400,
  wrong method405,OAuth/submission/admin/practice/unknown404. JSON/no-store/
  redaction PASS. No new provider/service/subscription or backend deployment.
- [x] Postflight20exacttablecounts,intakefalse,runtime role flags/limit4/expiry
  unchanged. Earlier20RLS/55policy/verify-full/CA/cacheoff/two-key/logsoff evidence
  retained. See results/public-activation-2026-10-27 for actual request evidence.
- [x] Current notice/Android asset/CZ/EN synchronized;951localizations/policy/
  whitespace PASS.3254326 successful backend/quality CI reused; no full rerun.
- [ ] Integrate accepted Stage7/Stage8/Discover source with current Stage6; verify
  the integrated baseline before creating the dedicated Database Simplifier branch.
- [ ] Shared Discover/Library catalogue, data-preserving changed-scope tests,
  migration/upgrade/CI, final trusted Stable APK and release gates remain pending.

Backend milestone verified; whole integrated Stage6/release acceptance not inferred.
Issue31 OPEN/Project In Progress. Catalogue projection is empty,not populated;
licence/field/provenance approval precedes ingestion. Existing RC preserved.

---

## Current disposition — owner procedure adopted and manually confirmed

The owner has adopted the mailbox policy and reports actual access/Inbox/Spam
checks,Tuesday/Friday checks scheduled,monthly review on day1 and private case
tracking. This supersedes older "unadopted" wording below; no Gmail automation
or agent access is necessary. LAUNCH-PROCESSING-ASSESSMENT.md documents separate
catalogue/support/rights bases and assessment. PROVIDER-PROCESSING-2026-10-27.md
records actual account/configuration checks, incorporated DPAs and evidence limits.
These resolve the generic processing/operations hold for the authorized read-only
scope. Public activation and smoke pass remain to be executed; no new approval
required. Current local stage/AI checklists govern; historical review follows.

---

## Current authorization — supersedes older approval holds below

Owner decision [6435585794](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6435585794)
explicitly authorizes public read-only routing after genuine prerequisites pass.
Do not ask for the same activation approval again. Public identity/contact
acceptability is owner-reported and Q1/D5b eligibility stays accepted.

Remaining actual evidence: adopted processing basis, provider-account processing
review outcome, monitored support mailbox and adopted retention/rights workflow.
See the current entry in `docs/LEGAL-REVIEW-0.11.md` and stage checklist. No public
activation or new smoke pass is claimed; previous source/security evidence reused.
After resolution, run one focused public positive/negative pass, finish Stage6,
then branch Database Simplifier from the verified integrated baseline. No new RC.

---

## Historical preparation — current authorization above governs

# Online catalogue activation — privately deployed candidate

The queued task started after the 90-day renewal, private verification, commit,
push and clean remote checkpoint `f3936cdc6759a80d988654ca9c53fcebf07b3f8e`.
Its purpose is to expose only the approved catalogue projection over HTTPS using
the existing Worker, Hyperdrive and Supabase resources.

## Implementation and verification

- [x] Reuse `goldberg-community-bff-private`, Hyperdrive
  `3c684744936740c290361dcc0fb018dd` and the existing database. No new resource,
  role, schema, migration, data import or catalogue publication.
- [x] Renew only runtime expiry to **2027-01-25T04:39:50.052095Z**. Preserve the
  password, privileges, RLS, memberships, four-connection limit and data. Fresh
  authentication with the existing password and actual private connectivity pass.
- [x] Enforce `CATALOGUE_READ_ONLY=true` before reading private configuration or
  dispatching database work. Only `GET /v1/catalogue/overlay` and
  `GET /v1/catalogue/changes` can reach the BFF. Other paths return 404; other
  methods on those two paths return 405 with `Allow: GET`.
- [x] Deploy the candidate to the existing private Worker. Retain the exact CA,
  `verify-full` origin TLS, disabled query caching, two Worker server keys and
  disabled persisted application logs/traces. No admin credential is uploaded.
- [x] Focused Fetch tests: **12 PASS**. Compiled local workerd requests:
  **24 PASS**. No full local or Android suite repeated.
- [x] Actual private hosted checks: catalogue/feed 200; OAuth/admin/submission
  writes 404; invalid cursor 400; wrong method 405; unknown route 404; anonymous
  loopback caller 403. Responses use schema version 1, `no-store` and redacted
  errors. These are private service-binding tests, not public HTTPS tests.
- [x] Deployed bundle matches its canonical build. Exact version/hash are in
  `results/public-catalogue-candidate-2026-10-27/worker-postflight.json`.
- [x] Database postflight: 20 RLS tables, 55 policies, original 47 BFF table grants,
  ledger and data preserved; community intake remains false. Published entities,
  revisions and change feed contain zero records.
- [x] Recheck official Free pricing/limits. No subscription, trial, payment method,
  upgrade or paid fallback configured. Existing owner Free/no-card evidence is
  retained; billing API 403 remains a limit on independent invoice inspection.
- [x] Owner supplied public developer identity `stokonn` and official privacy/support
  contact `stokonn.us@gmail.com`; private identity details are not authorized.
- [x] Prepare the 0.11 notice, synchronized Android asset and CZ/EN references.
- [x] Owner approved the exact notice text at commit `2f0cf0b`; approved copy preserved.
- [ ] Resolution of the listed retention,
  formal controller identification/basis and provider-processing requirements. See docs/LEGAL-REVIEW-0.11.md.
- [ ] After applicable prerequisites pass, enable the existing HTTPS route and
  verify actual public reads, denied private/write requests, redaction and
  unchanged database security/data. **Public activation is not executed.**

## Exact blocker

The owner supplied the official contact and requested the final notice for approval.
`docs/PRIVACY.md` now contains the 0.11 review copy, synchronized into Android.
The owner has approved the exact notice text for the next step. The remaining
blocker is **the unresolved factual and operational requirements listed in
docs/LEGAL-REVIEW-0.11.md**, rather than missing contact or text approval. Q1/D5b's accepted operator-model outcome is preserved.

The owner expressly requires public routes to remain disabled pending the
remaining requirements and final approval. Original text approval remains recorded. No public URL is activated; no incomplete notice is treated as approved.
No identity, retention period, international-processing conclusion or compliance
claim is invented. Read-only deployment preparation continues privately.

Technical facts for the notice: only catalogue reads are supported; no account,
practice journal, profile, microphone or backup upload endpoint is exposed.
Cloudflare processes connection metadata and requests at its edge; the Supabase
origin is Frankfurt. Disabling application logs does not establish zero provider
metadata retention or EU-only processing. Provider retention/transfer statements
must be verified rather than invented. No real catalogue records are published;
future ingestion requires the approved field/source/license/attribution policy.

## Remaining execution

Once remaining conditions and final approval are recorded, record the exact public scope,
retain the catalogue boundary, OAuth disabled and intake false, change only the
existing route configuration, and perform bounded actual HTTPS checks. Do not
recreate working resources or introduce paid fallback.

Google OAuth, community writes and personal data remain disabled/protected.
Public HTTPS 200 is unexecuted. No Android release, tag, RC or main merge occurred.

Sources rechecked October 27:

- https://developers.cloudflare.com/workers/platform/pricing/
- https://developers.cloudflare.com/workers/platform/limits/
- https://developers.cloudflare.com/hyperdrive/platform/pricing/
- https://supabase.com/pricing

## PR #39 and latest owner clarification

Draft PR #39 was reviewed at head3cdd34f and reconciled, not merged. The newer
public nickname/contact decision is authoritative; formal controller identification
stays OPEN. Proposed support-email90-day retention is not adopted or implemented
and is distinct from runtime credential expiry. See the operations-review document
for remaining owner/guardian/reviewer decisions and safe rights-request desk review.
Fresh Cloudflare API readback confirms public/previews OFF,0 custom domains and
exact deployed source match. Android identity resource build/lint and packaged
notice/signature/secret-scan PASS; detailed immutable receipts are in
results/privacy-review-2026-10-27. No public HTTPS request is claimed.
