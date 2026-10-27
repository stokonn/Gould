# Existing catalogue providers — scoped processing review

October 27, 2026. Scope: existing Cloudflare Worker/Hyperdrive and Supabase
catalogue database only. No new service, account, payment, processor, OAuth,
community writes or personal-practice storage is approved by this review.

## Applicable framework and evidence

Cloudflare's [Self-Serve Agreement §6.1](https://www.cloudflare.com/terms/)
incorporates its [DPA v6.4](https://www.cloudflare.com/cloudflare-customer-dpa/)
for covered processing. Supabase's [Terms §7](https://supabase.com/terms)
incorporate its [DPA v1](https://supabase.com/legal/customer-resources/data-processing-addendum).
The account configuration APIs do not expose click-through acceptance receipts
or bespoke contractual variations. The review uses the standard incorporated
terms for these existing self-service accounts, with the already accepted
owner-reported Q1/D5b operator/contractual eligibility. It does not claim a new
agreement was signed or independently certify the account's contract history.
No evidence of a conflicting bespoke agreement has been supplied.

Account-specific authenticated readback, retained in
`results/launch-operations-2026-10-27/`:

- Existing Supabase project healthy, Frankfurt, PostgreSQL17.11; organization
  Free/tier_free.20 private tables/20 RLS,55 policies. SSL on; connection and
  disconnection logs off; statement logging ddl/error threshold unchanged.
- Existing Cloudflare deployed bundle exactly matches reviewed local build;
  catalogue-only true, backend Google false, only two Worker server keys.
  Persisted application observability absent/disabled, logpush false,0 tail consumers.
  Hyperdrive cache disabled, origin verify-full with the existing CA reference.
- Public/previews off at preflight. No public request claimed by that readback.
  Initial stale-session403 was resolved by revalidating the existing session;
  no new login. Billing/subscription GETs still403: independent invoice/payment
  inspection unavailable. Existing owner Free/no-card/no-paid evidence is retained,
  not replaced by a claim of audited zero invoices. No billing change made.

## Recipients, transfers and minimized exposure

Cloudflare receives visitor connection metadata at its global edge. Hyperdrive
connects as a restricted service role; the origin does not receive the visitor's
IP as a catalogue query parameter. Supabase hosts catalogue rows in Frankfurt;
it also processes service/account/operational metadata. Account information may
be provider-controlled, distinct from processing on Goldberg's behalf.

Cloudflare DPA §6 provides the relevant transfer framework, including SCCs where
applicable. Supabase DPA §12 and its
[regional FAQ](https://supabase.com/legal/privacy-resources/data-residency-and-transfers-faq)
address international processing. Their published
[Cloudflare service list](https://www.cloudflare.com/gdpr/subprocessors/cloudflare-services/)
and [Supabase October12 list](https://supabase.com/legal/subprocessor-list/October-12-2026.pdf)
include overseas entities. These lists describe possible service recipients, not
proof that every listed entity receives every catalogue request. No EU-only
guarantee, fresh DPF certification check or professional transfer certification
is claimed. Supabase's fuller TIA is linked from its FAQ, not inspected as an
account-specific legal opinion here.

For this narrow configuration, risks are reduced by no account/journal upload,
no persisted operator request log, verified origin TLS, restricted SQL and
disabled caches. TLS does not prevent the providers themselves processing data.
The documented standard safeguards and minimal delivery scope support this
bounded deployment; there is no evidence here of a prohibited transfer requiring
the existing route to stay disabled. Reassess on provider/terms/location or data
scope changes, safeguards lapse, a substantiated objection or incident.

## Retention and ongoing ownership

No provider-wide fixed deletion period has been verified. Queryable log windows
are not universal erasure deadlines. Goldberg adds no visitor request register;
the response is processed for request lifetime. Provider infrastructure/security
retention follows applicable agreements and provider criteria, not the mailbox's
90 days or runtime credential's90-day validity. This limitation is disclosed.

The owner handles rights/provider escalation privately through the existing
provider accounts, checks material contractual/processing changes during monthly
operational review, and reassesses before enabling additional processing.
No provider was contacted, mailing-list signup made or paid legal/trust access
requested. A manual process is used; no monitoring automation is claimed.

## Disposition

Standard processing arrangements reviewed against actual account/deployment
facts, with the limits above. Combined with the separate proportionate processing
assessment and owner-confirmed manual mailbox operation, this resolves the
previous generic processing/operations hold for the two read-only routes. It
does not certify general compliance or clear content ingestion, community, backup
automation or authenticated workflows. The live public smoke pass and final
integrated release gates still have to execute.

created by stokonn with love <3
