# Goldberg 0.11 privacy review — approval pending

The owner selected **stokonn.us@gmail.com** as the official privacy/support contact
on October 27, 2026. This resolves the missing contact input; it does not constitute
approval of the notice or permission to activate public routes. The owner expressly
requires final notice approval before activation. The accepted Q1/D5b operator-model
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

1. Owner approval of the **exact** review copy, followed by any requested revisions.
2. Final approved public operator identification and applicable processing basis.
   Do not publish private identity details or infer legal approval from an alias.
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
