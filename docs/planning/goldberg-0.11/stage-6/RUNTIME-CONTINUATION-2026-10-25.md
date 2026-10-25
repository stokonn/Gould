# Stage 6 runtime continuation — October 25, 2026

Status: runtime implementation locally verified; inert hosted PostgreSQL permissions deployed; whole-stage verification IN PROGRESS. Whole Stage 6 remains
incomplete. Starting source `2044cfac18e30d0fb353cda9ce812e200c40b7e5` on
`feature/goldberg-0.11-stage-6-cloud`. Discover, Stage7/8, release-candidate and
other worktrees are preserved. This execution creates no Release, tag or main merge.

## Implemented scope

- Reuse accepted services/repositories/transactions and API v1; no backend rewrite.
- Fixed Google JWKS URL and bounded response (64KiB, fatal UTF8, no redirects),
  RS256 identity verification through the accepted verifier. Required configured
  client audience; nonce, issuer, age, expiry and signature checks retained. Cache
  respects max-age, Age, no-store/no-cache and the existing single-flight/rotation
  mechanism. Only issuer/subject/nonce cross the identity boundary; JWT roles and
  profile fields do not grant privileges. Private verifier RPC remains supported.
- Explicit PostgreSQL runtime grant/RLS plan for18 application tables; no ALL,
  BYPASSRLS, SECURITY DEFINER, schema ownership or DDL. No migration-ledger/probe
  grants and no blanket default grants. Runtime role must exist separately, own no
  objects, have no inherited roles or elevated flags. PostgreSQL17 creator administration is
permitted only for the schema owner with ADMIN=true/INHERIT=false/SET=false. Policies are server-role only;
  live BFF principal/ownership/role checks remain authoritative for user requests.
  This does not claim per-end-user PostgreSQL policies. Plan is transactional,
  replay fails on existing policies; it does not provision credentials or bootstrap.
- Minimum column UPDATE permissions permit required row locks on intake and
  receipts without allowing intake enablement or receipt response/hash changes.
  Existing append-only/transition/hash-chain triggers stay in force.
- Request-scoped pg socket errors are handled and retire the connection once.
- Kotlin integration runner forces execution because external BFF files are not
  Gradle test inputs. An earlier cached XML is preserved as evidence of the bug,
  and is explicitly excluded from new integration execution claims.
- Package runner writes new UUID receipts, preserving original evidence bytes.
- Stage6 branch debug version corrected from stale0.10.1/code11 to0.11-dev/code12.
  Application ID/database/signing identity remain compatible; no database migration.

## Executed verification

`python3 services/community-bff/run-local.py`:515 bodies PASS,0failures/skips;
actual PostgreSQL17, local HTTP/workerd/TCP and unit venues separated in the receipt:
`services/community-bff/results/local-36fe08480372485284e8619d4c477e04/result.json`.
Source remained unchanged during that run; UUID container and volumes were removed
and absence verified. Focused runtime test uses actual restricted database logins,
not SET ROLE from a superuser session. It executes sessions,contribution,open,
approve,replay,feed,refresh; rejects DDL/audit deletion/ledger reads/intake changes
and role escalation. A second client granted table SELECT sees zero rows under RLS.
Elevated runtime roles are rejected before any grant;transaction rollback verified.
Earlier failed attempts and their cleanup/source receipts are retained.

`node services/community-bff/build-local.mjs`:8 actual local workerd fail-closed
requests PASS. This is package execution,not a Cloudflare hosted deployment.
`python3 scripts/stage6/test_backup_store.py`:6 archive tests PASS on host;
sandbox GnuPG socket failures retained separately,not counted as successful.
`python3 spikes/stage6-private-staging/verify-tls-local.py`:6actual localTLS,
password,encrypted export/new-path isolated restore checks PASS;fixture/volumes
removed and verified absent. Receipt under `spikes/stage6-private-staging/results/
goldberg-probe-tls-47c38f5efedf46bea5cc0aff8c0b6481/`.
All six `scripts/check_*.py` gates PASS(migration,backup,localization,paging,policy,
ACL proposal). Historical `scripts/stage6/verify_evidence.py` PASS;original pins
and raw acceptance artifacts retained,not relabeled as current-source native tests.

Public Google JWKS connectivity:HTTP200,2public keys,5s deadline,no credentials,
no user data sent. This proves key endpoint connectivity only,not real Google OAuth.
Actual forced Kotlin integration1PASS at22:12:37UTC,0skips;receipt local-0ea30b4861.
Versioned Android APK/testAPK/lint PASS2m48s;host234JVM XML cached,not freshly executed.
APK com.opus.piano0.11-dev/code12,SHA256
`a1213cfb3b4f081712d56e1fa096de3aed3ab4d67c64a0dc15e36152f94adfe2`,
trusted53efcertificate;downloaded0.10.1 compatibility,ZIP/artwork/fixture exclusion PASS.
Final exact-source cleanCI/API36 receipts remain pending after source push. No current-source UI screenshots inspected.

## Hosted state and exact external prerequisites

Supabase read-only fresh observation:ACTIVE_HEALTHY;20private tables/20RLS;
ledger v1 checksum `d39a3afd68a2671dd642a17a82f96edea76dfc0ae1a09edb4be9ffde6aa5c935`;
intake=false;anon/authenticated private schema USAGE and public DDL callback EXECUTE
both denied. Marker-only probe role remains separate. A new goldberg_bff_runtime role and
47 explicit table/command grants,2column-lock grants and49server-role RLS policies
are now deployed. Exact hosted grant/policy comparison PASS;NOLOGIN,no elevated
flags,no ownership,no inherited roles;only schema-owner maintenance ADMIN with
INHERIT=false/SET=false. No password created or runtime login enabled.
Runtime cannot enable intake,change receipt response,read ledger/probe;client
schema denial andoriginal ledger/RLS preserved. No application rows changed.
Advisor now1expected INFO(private ledger),0WARN/ERROR. Hosted runtime not tested.

Initial strict guard rolled back fully. A metadata-only intentional rollback
confirmed non-superuser PG17 CREATE ROLE automatically gives the creator ADMIN
without INHERIT/SET. Added real PG regression rejects INHERIT/SET/non-ADMIN cases;
the exact correction permits only the schema-owner maintenance grant.
Readback/source SQL is retained in results/runtime-continuation-2026-10-25/.
Do not replay role creation/grants:they are already applied;policies intentionally
fail on replay. No existing object bootstrap/migration ordata replacement occurred.

Existing Cloudflare credential account GET returned403 at22:01:45UTC. No configured
Cloudflare/Supabase/Goldberg credential variable names were present. Connected
Supabase management access is available;it is not a server database runtime credential.
No login enablement,secret upload,session/Worker deployment,public route or real data enabled.
Accepted owner Free/no-card evidence retained;current Cloudflare account/config/cost
cannot be independently read with403. No paid fallback or future-zero-cost promise.

Minimal owner action:configure an account-scoped Cloudflare API token for the
intended account with Account Settings Read and Workers Scripts Edit(and the
read/remote private binding permissions required by the reviewed execution path),
then provide only its variable name or protected600-mode file path. Supply an
owner-controlled server connection file with the exact Supabase Connect host/port,
restricted BFF login and password,TLS hostname/trust material;never paste values in
chat/Git/logs. Real sign-in also needs the approved Google OAuth web client ID and
Android client configuration for com.opus.piano and the trusted signing certificate.
The existing probe role is deliberately insufficient for application data.

The owner now authorizes safe private staging within verified security,cost,privacy
and backup prerequisites. Do not repeat obsolete blanket permission requests.
Once credentials exist,verify private transport/account/resources,reuse the already deployed NOLOGIN role and reviewed grants(no replay/bootstrap),
provision its bounded runtime login through the confidential server channel,upload server-only
secrets,deploy without workers.dev/preview/public routes and execute actual hosted
synthetic acceptance/denial/TLS/outage/cleanup and cost readback. Stop if prerequisites
fail;do not weaken authentication,RLS,TLS or default-deny contribution policies.

Production OAuth,approved real source/field/license policy,accepted legacy UI/
Keystore adoption,public API freeze,production privacy/backup-custody/scheduling
and whole-stage acceptance remain outstanding. These are not inferred from local
synthetic success. Existing24-requirement acceptance matrix remains PARTIAL;
Issue31 OPEN/Goldberg Development In Progress. No public collection authorized.

created by stokonn with love <3

## API36 first run and corrected harness

Run40239512295 at5b22ffb failed before native execution:multiline emulator-action
script was run by /bin/sh and rejected pipefail;certificate parser discarded the
actual signing output. No native PASS claimed. Preserve original failed log.
Materialize the exact existing full-suite/acceptance bodies into two Bash scripts
and invoke them as one command. Reuse the proven a73a7e2 signing step,retaining both
streams/exit codes and digest extraction. No test exclusion or physical-device
selection added;all ADB operations retain explicit verified emulator serials.
YAML parsing and both bash -n checks PASS. Corrected-source dispatch follows.
