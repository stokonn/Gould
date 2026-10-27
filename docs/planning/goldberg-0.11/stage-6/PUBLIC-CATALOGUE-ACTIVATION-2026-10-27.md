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
- [x] Owner supplied official privacy/support contact: stokonn.us@gmail.com.
- [x] Prepare the 0.11 notice, synchronized Android asset and CZ/EN references.
- [ ] Owner approval of the exact notice and resolution of the listed retention,
  operator/basis and provider-processing requirements. See docs/LEGAL-REVIEW-0.11.md.
- [ ] After applicable prerequisites pass, enable the existing HTTPS route and
  verify actual public reads, denied private/write requests, redaction and
  unchanged database security/data. **Public activation is not executed.**

## Exact blocker

The owner supplied the official contact and requested the final notice for approval.
`docs/PRIVACY.md` now contains the 0.11 review copy, synchronized into Android.
The current blocker is **owner approval of that exact text and the unresolved
requirements explicitly listed in docs/LEGAL-REVIEW-0.11.md**, rather than a missing
contact. Q1/D5b's accepted operator-model outcome is preserved.

The owner expressly requires public routes to remain disabled before notice
approval. No public URL is activated; no incomplete notice is treated as approved.
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

Once the approved notice/contact is available, record the exact public scope,
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
