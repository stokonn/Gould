# Catalogue runtime credential lifecycle

An owner-authorized manual renewal gives **90 days from execution**. The renewal
SQL uses the transaction timestamp in UTC. It changes expiry only; it never
changes the password, privileges, RLS, memberships, four-connection limit, data or
billing. A failed guard stops without repairing permissions.

Current expiry: **2027-01-25T04:39:50.052095Z**. Renewal, unchanged security/table
counts, fresh authentication with the existing password and actual private
Worker connectivity are recorded in `results/runtime-renewal-2026-10-27`.
Earlier six-hour/seven-day windows are historical. Do not replay the original
one-time provisioner or bootstrap an existing role.

## Advance reminders

Import `runtime-expiry-reminders.ics` into an owner-controlled calendar. It has
DISPLAY reminders 30, 14, 7 and 1 day before expiry: December 26, 2026, and January
11, 18 and 24, 2027, at 04:39:50 UTC. Calendar software displays local time.
**The file is generated; import is required. No calendar integration, notification
service or scheduler was installed, and no message was sent to another person.**

- 30 days: review expiry, owner availability, Free status and backup custody.
- 14 days: prepare the confidential rotation window and verify the exact role and
  Hyperdrive configuration.
- 7 days: perform an explicitly authorized rotation and fresh hosted verification.
- 1 day: escalate unfinished work; keep failed or unverified access disabled.

Expiry blocks new PostgreSQL password authentication. Existing authenticated
pooled sessions may survive: `VALID UNTIL` does not terminate them. Keep overdue
public access disabled and close only identified runtime sessions/restart the
approved pool during authorized maintenance. Do not bootstrap, reset passwords
or silently renew to conceal an overdue lifecycle.

After each authorized renewal/rotation, read back the server expiry and regenerate
calendar dates. Replace previous events to avoid stale reminders. A reminder may
use only non-secret dates/role identifiers; it must not connect as administrator
or execute renewal SQL.

## Manual expiry-only renewal

1. Obtain owner authorization and review `RUNTIME-EXPIRY-RENEWAL-2026-10-27.sql`.
2. On the trusted host, use the owned 0600 PostgreSQL service with `verify-full`
   TLS. Keep its admin password inside libpq. Never put it in arguments, history,
   transcripts, Cloudflare, GitHub, CI artifacts or source.
3. Execute the guarded transaction once. If COMMIT has an uncertain outcome,
   inspect metadata first; do not automatically replay and move expiry again.
4. Compare role settings, memberships, ACLs, RLS and all 20 table counts. Verify
   expiry is execution + 90 days and the connection limit remains four.
5. Establish a fresh connection using the existing protected runtime password,
   then test the actual private Worker → Hyperdrive → Supabase positive and
   negative paths. Do not treat a previously pooled session as fresh password proof.
6. Record only sanitized dates, counts, hashes, statuses and resource identifiers;
   update checkpoints and reminders. Never record passwords/verifiers/connection
   strings as evidence.

## Future password rotation — separate authorization

Expiry renewal is not password rotation. The present renewal preserves the password.

1. Identify `goldberg_bff_catalogue_login` and its exact Hyperdrive configuration.
   Arrange an authorized maintenance window; keep public reads disabled until
   verification succeeds. Preserve Android offline behavior and existing backups.
2. Generate a high-entropy password inside new owned 0700 local custody. Save it
   with exclusive 0600 creation. Preserve recovery material on uncertain outcomes;
   never overwrite an existing envelope or print values.
3. Use the established libpq helper to prepare SCRAM in memory. Verify statement
   logging/redaction before the reviewed password/expiry transaction; stop if the
   credential/verifier channel is not confidential. Never send plaintext passwords
   in SQL. Preserve flags, connection limit, memberships, grants, RLS and data.
4. Update only Hyperdrive's origin password using supported protected `@file` input.
   Preserve user, host, database, approved CA, `verify-full`, cache-off and Free
   limits. Worker secrets remain cursor/IP keys; no DB admin credential is uploaded.
5. Restart only this Hyperdrive pool using its supported command. Verify a fresh
   login with the new password, least-privilege denials, actual hosted catalogue
   responses, redaction, data and private/public boundaries. Do not repeatedly
   retry authentication or disable TLS verification on failure.
6. On failure, leave access disabled and repair the authorized delivery path. Do
   not automatically restore a retired/possibly compromised password, raise
   privileges, buy fallback or recreate data. Preserve confidential recovery
   material until the outcome is certain. Re-enable only separately approved scope.
7. After successful cutover and retired-session verification, remove obsolete
   plaintext/verifier envelopes through scoped custody cleanup. Preserve encrypted
   data backups and their independent keys. Record expiry and regenerate reminders.

**No automated renewal is configured.** Administrator credentials remain solely
in the owner's protected local PostgreSQL service. Cloudflare and GitHub do not
store a database administrator credential for renewal. Calendar reminders have no
credential or renewal capability.
