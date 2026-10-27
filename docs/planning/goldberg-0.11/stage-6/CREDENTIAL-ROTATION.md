# Catalogue runtime credential lifecycle

Policy:manual owner-authorized renewal gives **90 days from execution**. The
renewal SQL uses the transaction timestamp in UTC,not a hard-coded date. It changes
expiry only. It must not change the password,role permissions,RLS,memberships,
connection limit,data or billing. The existing role owns no objects,has no elevated
flags or inherited memberships,and retains four connections plus catalogue-only
SELECT/row-lock privileges. A failed guard stops without repairing permissions.

Current expiry:2027-01-25T04:39:50.052095Z. Actual expiry-only renewal,unchanged
security/counts,and fresh existing-password/hosted connectivity evidence are in
results/runtime-renewal-2026-10-27. Earlier six-hour/seven-day proposals are history.

## Advance reminders

Import runtime-expiry-reminders.ics into an owner-controlled calendar. It contains
DISPLAY reminders at30,14,7 and1 day before the verified expiry:December26,2026;
January11,18 and24,2027,at04:39:50UTC. Calendar software displays local time.
The file is generated andvalidated;**no calendar integration or notification
service has been installed**. Import is required to receive reminders. No emails,
third-party messages or scheduler were activated by this work.

- 30days:review expiry,owner availability,Free status,backup custody androtation need.
- 14days:prepare a confidential rotation window andverify the exact Hyperdrive/role.
- 7days:perform an explicitly authorized rotation andfresh hosted verification.
- 1day:escalate an unfinished rotation;keep failed/unverified access disabled.
- Expiry:fail closed. Do not bootstrap,reset passwords or silently renew to hide it.

After every approved renewal/rotation,read back the actual server expiry,regenerate
these reminder dates andreplace prior calendar events to avoid stale reminders.
Read-only reminders may use only non-secret dates/role identifiers. They must not
connect with administrator credentials or execute renewal SQL.

## Manual expiry-only renewal

1. Owner authorizes the new window. Review RUNTIME-EXPIRY-RENEWAL-2026-10-27.sql.
2. From the trusted host,use the protected0600 PostgreSQL service andverify-full
   TLS. Keep its admin password inside libpq. Never put it in CLI arguments,
   shell history,SQL tool transcripts,Cloudflare,GitHub,CI artifacts orsource.
3. Run the guarded transaction once. On uncertain COMMIT outcome,inspect metadata
   first;do not automatically replay andmove the expiry again.
4. Compare role settings/memberships/ACL/RLS andall20 table counts before/after;
   verify expiry is execution+90days andconnection limit remains4.
5. Verify a **fresh** connection with the existing protected runtime password,
   then actual private Worker→Hyperdrive→Supabase positive/negative requests.
6. Record only sanitized timestamps,counts,hashes,statuses andresource identifiers;
   update checkpoints/reminders. No password/verifier/connection string evidence.

## Future password rotation — separate owner authorization

Expiry renewal is not password rotation. Routine renewal must preserve the
password;rotation must be explicitly authorized and performed manually.

1. Identify only goldberg_bff_catalogue_login andits exact Hyperdrive configuration.
   Arrange a bounded maintenance window;keep public reads disabled until verified.
   Preserve offline Android functionality andexisting backups/records.
2. Generate a new high-entropy password inside a new owned0700 local custody
   directory. Save with exclusive0600 creation. Retain uncertain-outcome recovery
   material;never overwrite an existing envelope orprint values.
3. On the protected trusted host,prepare the SCRAM verifier in memory with the
   established libpq helper. Apply an explicitly reviewed,password-only/expiry
   transaction. Never send plaintext passwords in SQL. Preserve flags,connection
   limit,memberships,grants,RLS andall data;do not rerun the bootstrap/provisioner.
4. Update only Hyperdrive origin password through the supported CLI protected
   @file input. Keep exact user/host/database,approvedCA,verify-full,cacheoff and
   Free limits unchanged. The Worker retains only cursor/IP keys;it receives no
   PostgreSQL admin credential. Never log the request body orsecret file contents.
5. Restart only this Hyperdrive pool through its supported command so verification
   uses the new credential rather than an old pooled connection. Check one fresh
   new-password login,least-privilege denials,actual hosted catalogue responses,
   redaction,unchanged data andprivate/public boundaries. Do not repeatedly retry
   authentication ordisable certificate verification on failure.
6. On failure,keep access disabled andrepair the authorized delivery path. Do not
   automatically restore a retired/possibly compromised password,raise privileges,
   add paid fallback ordelete/recreate data. Preserve confidential recovery material
   until outcome is certain. Re-enable only separately approved public read scope.
7. After successful cutover andretired-session verification,remove obsolete local
   plaintext/verifier envelopes through an explicitly scoped custody cleanup;
   preserve encrypted data backups andtheir independent keys. Record new expiry
   andregenerate reminders. Publish only sanitized receipts.

**No automated renewal is configured.** Administrator credentials remain solely
in the owner-protected local PostgreSQL service. Neither Cloudflare norGitHub stores
an administrator password for a renewal job. Importable calendar reminders have
no credential orrenewal capability.
