# Private cloud readiness candidates

Local preparation authorized October 24, 2026. **NO-GO for private staging.**
No remote mutation/deploy commands or credentials are provided. These candidates
do not complete Stage 6 or implement a live BFF transport.

`prepare.py` renders `private-schema.candidate.sql` from the unchanged historical
001–004 migrations. One atomic transaction, advisory lock, PG17 check and checksum
ledger make exact replay safe; an unrecognized existing namespace or checksum
fails closed. There is no DROP, reset, data replacement or destructive rollback.
The dedicated `goldberg_private` schema has RLS on every table, zero policies,
no client/service-role grants, restricted functions/sequences/default privileges,
and disabled intake. Owners retain RLS bypass for historical audit triggers.
This is a locked storage bootstrap, not a runtime-role authorization design.
Replay validates the source ledger, not arbitrary catalog drift: a pre-apply and
post-apply catalog comparison remains a required hosted acceptance step.
Per-schema default-privilege revokes cannot remove a global default grant such
as PostgreSQL's default PUBLIC function EXECUTE. The schema remains inaccessible;
every future function must also have an explicit EXECUTE revoke in the same DDL
transaction, as this bootstrap does for all current functions. Do not treat
per-schema defaults alone as sufficient function isolation.

`acl-fix.candidate.sql` changes only EXECUTE on `public.rls_auto_enable()`.
Export its read-only identity/ACL/definition first using the planning proposal;
retain exact preflight evidence privately. The candidate compares its in-transaction
snapshot before commit. It never replaces the function or event trigger.
The local trigger is a representative fixture, not an export of hosted code.
Approval for this maintenance fix is separate from schema/bootstrap authorization.

`cloudflare.config.ts.template` uses the documented cf configuration shape, has
no public domains/routes, workers.dev, version previews, assets or resource
bindings. No account ID, runtime dependency or deployment script is included. A pinned
CLI/typechecker/bundler was installed only in/tmp for October25 local checks.
Free is an account subscription, not something this template can enforce.
Observability is explicitly OFF during readiness to respect C4 log minimization
and unverified billing; before production, approve sanitized fields, sampling,
retention and Free pricing before enabling logs/traces. No production observability
acceptance is claimed. October25 strict config/Worker typecheck and local esbuild bundle passed;
provider build/deploy/runtime remain unexecuted. Do not substitute Wrangler silently.

`worker.mjs` is an inert fetch entrypoint: all requests return 503/no-store, even
if activation-looking environment variables are supplied. No flag can activate
unverified handlers. No secrets are needed for this shell. The existing local
Node/pg adapter is not imported: its loopback restriction remains unchanged.
Future authenticated transport, least-privilege database role, authorization,
CSRF and bounded database connection design must be reviewed before adding code.
Do not expose this schema via PostgREST or grant service_role access as a shortcut.

Future server-only secret contract (names only; **not created or fetched**):

| Name | Intended scope / rule |
| --- | --- |
| `PRIVATE_VERIFICATION_TOKEN` | Short-lived synthetic probe gate, only after separately authorized private ingress; constant-time verification, never a query parameter. |
| `DATABASE_URL` | TLS-verified pooler/direct PostgreSQL URL for a separately reviewed least-privilege role; never postgres/superuser or a client binding. |
| `SESSION_HMAC_KEY` | Independent server-side session integrity key; no client storage/logs. |
| `CURSOR_HMAC_KEY` | Independent cursor signing key, distinct from session key. |

Keep values in provider server secrets or an owner-controlled mode-600 file
outside Git, bundles, APKs and result directories. Do not fetch service-role or
OAuth secrets for preparation. Uploading secrets is itself a gated remote write.
Before any future local secret file, add exact ignore rules and verify exclusion.
Never embed secrets in `vars`, command arguments, route URLs, screenshots or logs.
Do not log raw URLs, headers, cookies, payloads, IPs or database errors.

Run from the repository root:

```sh
python3 spikes/stage6-cloud-readiness/prepare.py
node --test spikes/stage6-cloud-readiness/worker.test.mjs
python3 spikes/stage6-cloud-readiness/verify-local.py
```

The runner accepts no database URL, host, credentials, network or port. It uses
one UUID-named rootless container with `--network=none`, a cached immutable image,
psql via exec and no mounts/published ports. It removes only its own container and
volumes and checks absence. Results contain synthetic data only. Its SQL backup
is a **plaintext synthetic fixture**, not a production backup or encrypted export.
Use the existing `stage6-backup-local` encrypted drill and operational runbook
for cryptographic custody/tamper/scheduling/retention requirements; their earlier
results are preserved and not claimed as rerun here.

created by stokonn with love <3

## October25 re-verification

The previous top-level cf template shape was invalid and is corrected to
`defineConfig({worker:{...}})` with a string entrypoint. Readiness SQL and Worker
behavior are unchanged. New20-check run332ae776 uses specific error assertions,
bounded subprocesses,interruption cleanup and durable command logs.
`verify-evidence.py --run <result-directory>` checks current sources;without the
argument it reads results/CURRENT.json. Never rewrite old manifests to pass a
current-source check. Previous results/Worker TAP are immutable historical evidence.
All30 source and30 bundled Worker request cases pass. Build pins/lock/tsconfig/
logs are intentional evidence under results/reverification-2026-10-25;no compiled
bundle,node_modules or secrets are tracked. The bundled probe is an evidence
script for the temporary build directory,not an application entrypoint.
