# Private synthetic staging preparation

**NO-GO for hosted execution.** This folder adds the new Phase1/2 preparation
on top of accepted readiness HEAD c8b903b. Existing readiness SQL, disabled shell,
tests and historical evidence remain unchanged. No account login, product
activation, hosted schema/role/secret/Worker mutation or public route was performed.

`catalog-preflight.sql` reads only metadata. Its sanitized observation records the
actual function fingerprint/guard match, empty application catalog, role/schema
creation capability and SSL server flag. NULL exposure/enforcement fields remain
unknown; they are never interpreted as PASS. Candidate fingerprints are pinned
in the final preparation manifest. This is catalog compatibility evidence, not
an execution/rollback rehearsal on Supabase.

`probe-role.candidate.sql` follows the existing narrow ACL maintenance and private
bootstrap. It creates a distinct passwordless LOGIN role with no inheritance,
superuser, role/database creation, replication or RLS bypass; connection limit2,
3s query/statement and1s lock limits. It grants USAGE plus SELECT on one constrained
synthetic marker table only. No application-table/function/moderation grants.
Existing role/table identities cause a rollback and review rather than overwrite.
Role credentials must be set through a separately approved confidential operation;
there is no credential setter or password in this packet. The exact credential
delivery mechanism is an unresolved hosted execution detail, not a prepared secret.

`disable-probe.candidate.sql` blocks new login and revokes probe schema/table
access without deleting the role or data. Existing sessions need a separately
reviewed operator action if termination is necessary. Do not DROP/recreate the
project, schema or recovery data. Rollback before COMMIT is normal transaction
rollback; after COMMIT use containment and retained data, not destructive rollback.

The new `probe-handler.mjs` accepts only one authenticated POST and is disabled
unless its explicit flag is exactly true. It uses cryptographic HMAC verification
to compare bounded synthetic probe credentials, then calls a fixed adapter. It
never forwards caller SQL/body/URL to PostgreSQL. `probe-db.mjs` constrains the
role, expected host, port/database and TLS CA/hostname verification, rejects URI
SSL overrides, uses fixed SQL and closes per-request connections. Raw errors and
credentials are not logged or returned. New templates are NOT wired into the
accepted inert shell. Future packaging pins pg8.23.1 with nodejs_compat. No
Hyperdrive, paid IPv4, VPC, custom domain, OAuth or Access product is added.

Private transport candidate: a route-less target Worker plus a **local-only**
authenticated caller using a remote service binding and authenticated Cloudflare
control-plane session. Cloudflare documents that service bindings can reach
Workers without public URLs, and that remote bindings can call deployed Workers.
The cf template typechecks, but this account's session/Free entitlement, actual
private reachability, negative Internet probes and lifecycle cleanup are NOT
verified. No remote session has been started. Bind any development listener to
127.0.0.1 explicitly; URL Host checks are only defense in depth, not the bind rule.
The caller itself requires the probe credential and only forwards a fixed request.
Never deploy the local caller or replace this design with a public secret-gated URL.

`verify-new.py` creates only a rootless, network=none, immutable cached PG17
fixture. Accepted SQL is setup, not a rerun of accepted tests. New checks cover
the probe role's actual local login, privilege denial, real statement timeout,
transaction rollback, restore re-hardening and non-destructive containment.
Local trust auth proves fixture role behavior only, not hosted password/TLS auth.
The original encrypted backup drill is reused; new logical restore does not prove
an encrypted destination, schedule, alerting or achieved hosted RPO/RTO.

The probe is a single synthetic operational check, not the community BFF or
accepted moderation authorization matrix. Shared credentials do not establish
per-user authorization. Account isolation/MFA/CSRF and production app integration
remain outside this probe and cannot be marked passed by its tests.

created by stokonn with love <3
