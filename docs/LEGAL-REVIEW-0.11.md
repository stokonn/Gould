# Current launch decision — October27 owner authorization reconciled

[Owner decision6435585794](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6435585794)
supersedes all older requests below for another public-route approval or identity
acceptability review. Public read-only activation is authorized, conditional on
remaining genuine prerequisites. Public nickname/contact acceptability is
owner-reported; accepted Q1/D5b eligibility is preserved. No independent legal
certification is claimed or requested by this checkpoint.

The remaining concrete evidence gap is the adopted processing basis and actual
support/rights procedure, plus the provider-account processing disposition.
The operations review still labels these as candidates/proposals, not adopted
outcomes. A routine mailbox90-day policy has not been implemented by this agent.
No private identifying information should be placed in GitHub.

Primary sources rechecked during this continuation:

- [Cloudflare DPA](https://www.cloudflare.com/cloudflare-customer-dpa/), v6.4:
  sets processor duties within the customer's agreement; reading it does not
  establish the applicable account acceptance or this service's lawful basis.
- [Supabase DPA](https://supabase.com/legal/customer-resources/data-processing-addendum),
  v1: supplements its customer agreement and provides conditional transfer
  safeguards. No separate signature requirement is invented. No account contract
  was accepted or changed by this agent.
- [Commission legal grounds](https://commission.europa.eu/law/law-topic/data-protection/information-business-and-organisations/legal-grounds-processing-data_en):
  legitimate interests require checking the impact on individuals. The existing
  candidate-basis matrix does not record a completed/adopted assessment.

Exact next step: record the existing review outcome, adopted basis and real
mailbox monitoring/retention/rights workflow without sensitive details. Once these
prerequisites pass, activate under standing authorization and perform the single
focused public smoke pass. Do not demand reauthorization. The local stage/AI
checklists carry current CI and source evidence. Historical review follows.

---

# Goldberg 0.11 privacy review — text approved; launch requirements remain

The owner confirmed public developer identity **stokonn** and selected **stokonn.us@gmail.com** as the official privacy/support contact
on October 27, 2026. This resolves the public nickname/contact input. The earlier exact notice text
was approved for the next step; that approval does not clear launch requirements.
The owner expressly requires final approval before public activation. The accepted Q1/D5b operator-model
review remains accepted within its recorded scope and is not reopened.

## Review copy and Android references

The complete review copy is `docs/PRIVACY.md`. Its bundled Android copy is
`app/src/main/assets/legal/privacy.md`; bytes must match. LegalScreen already reads
that asset off the main thread. English and Czech `privacy_text` summaries now
reference the same contact, cloud candidate, logging uncertainty and approval hold.
No network flag, Google client ID or community permission is enabled by this text.

The draft covers local journal/profile/practice data, transient microphone analysis,
format-5 backups and retained recovery material, third-party metadata/media queries,
local Google profiles versus disabled backend OAuth, Cloudflare Workers/Hyperdrive,
Supabase, request IP/technical metadata, logging/retention uncertainty, support email,
user choices and conditional rights/contact procedures. It does not claim blanket
compliance, invent a retention period or promise erasure of copies not controlled
by the operator.

## Executed technical facts

| Area | Evidence and limit |
|---|---|
| Catalogue boundary | Actual private hosted reads 200; auth/admin/submission routes 404; wrong method 405; invalid cursor 400. Only the two catalogue GET paths reach the BFF. Public/preview routes stay off. |
| Credentials | Runtime expiry renewed manually for 90 days, unchanged password/permissions/RLS/data. Hyperdrive verifies the exact CA/hostname; cache off. Only cursor/IP keys are Worker secrets. No database admin credential in Cloudflare/GitHub/CI. |
| Worker logging | Deployment explicitly disables persisted application/invocation logs and traces. Fresh `/script-settings` readback: `observability:null`, `logpush:false`; no tail consumers configured. This is not proof of no provider internal logs. |
| PostgreSQL logging | Fresh read-only settings: connection/disconnection logs off; `log_statement=ddl`; `log_min_error_statement=error`. They do not establish Supabase platform log retention or deletion of every provider copy. No setting changed. |
| Local data | BackupRepository implements format 5/schema 10 and includes practice draft/recovery sections. Local GoogleIdentity retains profile data locally and does not upload tokens to this catalogue backend. No journal upload endpoint is exposed. |
| Catalogue records | Existing publication projection is empty. No unapproved music data or attribution/license policy was invented; no import performed. |
| Calendar reminders | Importable 30/14/7/1-day reminders generated. Calendar import is required; no notification/renewal service or administrator-based scheduler installed. |

## Unresolved requirements — keep public routes disabled

1. **Complete:** the owner approved the exact review copy at commit `2f0cf0b`,
   SHA-256 `b76e3479ccc2f4054302ad7a738406440c32cb0ab4420b283b77b8b3fa979759`,
   for the next step. The approved copy is preserved byte-for-byte; this approval
   does not clear the following requirements or authorize public activation.
2. **Explicitly unresolved by owner:** formal legal identification of the data
   controller, pending appropriate review. Publish only the authorized nickname
   and contact; no legal name, residential address or other private details. The
   nickname is not claimed to satisfy this requirement. The applicable processing
   basis also remains to be confirmed.
3. The deployment-specific provider logging/retention and international-processing
   arrangements. Frankfurt origin is not an EU-only edge/metadata guarantee, and
   a dashboard retention window is not a deletion guarantee for all provider copies.
4. Support-correspondence retention/deletion criteria and an operational procedure
   for rights requests and applicable response obligations. No period is invented.
5. Before any future records are ingested/published, the approved field/source/
   license/attribution policy. Empty read-only projection does not approve new content.

A text-approval response must not silently discharge unresolved factual or
operational requirements. No fresh legal certification or professional opinion is
claimed. Public HTTPS 200 remains unexecuted; private synthetic proof is not
substituted for it. OAuth and community intake remain disabled.

## Primary references checked October 27, 2026

These support the review, not a claim that every provider setting or legal duty has
been verified for this account. No support request or message was sent to a provider.

- https://www.cloudflare.com/privacypolicy/
- https://www.cloudflare.com/cloudflare-customer-dpa/
- https://developers.cloudflare.com/workers/observability/logs/workers-logs/
- https://supabase.com/privacy
- https://supabase.com/legal/privacy-resources/data-residency-and-transfers-faq
- https://supabase.com/docs/guides/database/postgres/postgres-log-config
- https://metabrainz.org/privacy
- https://foundation.wikimedia.org/wiki/Policy:Privacy_policy
- https://policies.google.com/privacy
- https://www.edpb.europa.eu/topics/key-gdpr-concepts/data-subject-rights_en
- https://commission.europa.eu/law/law-topic/data-protection/information-business-and-organisations/dealing-requests-individuals_en
- https://uoou.gov.cz/

## Draft PR #39 reconciliation

Reviewed draft [PR #39](https://github.com/stokonn/Goldberg/pull/39), head
`3cdd34f62c54f607ad5182f54f3ec0283f229ce2`, without merging it. Its safe operational
proposals, source checks and outstanding owner/reviewer decisions are reconciled
in [the operations review](planning/goldberg-0.11/stage-6/PRIVACY-LAUNCH-OPERATIONS-REVIEW-2026-10-27.md).
The proposed routine-support-email deletion period of 90 days is **not adopted**,
implemented or a provider-log retention setting. It does not change the current
notice. Candidate legal bases remain candidates. No age, private identity or legal
capacity finding is published. Formal controller identification remains **OPEN**.
