# Stage 6 private integration — October 26, 2026

Whole Stage 6 remains **IN PROGRESS**. This record supersedes historical
connectivity-blocked statements in the October25 handoff. Existing schema/data,
Stage8/Discover worktrees, release-candidate artifacts and signing identity remain
preserved. No release/main merge/public community activation.

## Implemented and executed

The accepted BFF/repository/transaction/auth/workflow architecture is retained.
OAuth can now remain explicitly disabled while catalogue reads work. All three
session creation/refresh POST endpoints return503 before database work when
`GOOGLE_OAUTH_ENABLED` is absent/false. No Google client ID placeholder, substitute
identity binding or fabricated sign-in. Existing Google verification requires an
approved audience before enabling it.

`python3 services/community-bff/run-local.py`: **521 bodies PASS**, no failures or
skips. Venue-separated result:
`services/community-bff/results/local-760943f585d640128a1570d7dbe89900/result.json`.
Actual restricted catalogue login test reuses the same grant generator as the
hosted provisioner; reads succeed and auth/private records, writes, DDL and role
escalation are denied. Container/volume cleanup and unchanged source during run
are recorded. This is local integration, not hosted Worker acceptance.

`python3 -m unittest discover -s scripts/stage6 -p test_private_runtime.py`:
**5 PASS**. Protects owned0600 service files, symlink rejection, exclusive secret
creation, exact catalogue scope, plaintext exclusion from SQL and uncertain-COMMIT
custody. `node services/community-bff/build-local.mjs`: **14 actual local workerd
requests PASS**, including all three disabled OAuth POST routes in both configurations.

## Hosted PostgreSQL

Owner fixed only the service dbname to `postgres`. Actual session-pooler connection
uses verify-full hostname/CA verification, TLS1.3. Administrative password stays
inside libpq service access. No bootstrap or password reset.

Before mutation, existing private-schema dump encrypted through the established
backup_store helper and restored into isolated network-none PostgreSQL17.
All20 table counts,20RLS/50existing policies, client-schema and ledger denial
verified. Ciphertext SHA256:
`b211813bdf3698f3eec472c02d747b00a140e935519cc5d385f1ea1cca893020`.
Encrypted archive and separate0600 key remain in owner-private stage6 custody;
plaintext dump/decrypted copy removed only after PASS. Target fixture and volumes
removed and absence verified. Initial local TCP restore failure is historical;
success used the container Unix socket, without any hosted TLS downgrade.
This proves a private empty-application baseline restore, not production recovery
of real personal data, PITR, offsite retention or a restore over the hosted DB.

Provisioned **new** `goldberg_bff_catalogue_login` once. It owns no objects, inherits
no application role, has no elevated flags, maximum4 connections and expiry
**2026-10-27T03:00:38Z**. Direct SELECT on published_entities, published_revisions,
change_feed and audit_head; UPDATE(singleton) on audit_head permits feed row locks.
Five role-specific policies added. Existing roles/passwords/grants/policies/data
were preserved. Never replay provisioner: it deliberately refuses an existing role.
Do not extend/reset this credential automatically; later windows need explicit
owner credential lifecycle authorization. Stored server-only credentials expire
closed; no credentials enter APK/Git/CI.

Actual Node pg8.23.1→Supabase: TLS encrypted/authorized1.3, databasepostgres and
expected runtime role. Catalogue/feed200 empty;7 denied private-data/privilege
operations42501; schema DDL and public callback execution denied. This proves
host→database transport and BFF handlers on Node, not Cloudflare socket behavior.

`python3 scripts/stage6/verify_private_runtime.py`: read-only hosted postflight PASS.
20RLS tables/55policies (original50 + new5), exact original47 BFF table grants
unchanged, BFF grant-role still NOLOGIN. Catalogue grants exactly4SELECT +1lock
column,0memberships/owned objects. Ledger checksum unchanged, intakefalse,
principals/sessions/audit/catalogue/feed all0 and existing probe marker1.

## Cloudflare deployment and private invocation

Initial pinned cf1.0.0-beta.21/Wrangler4.152.0 upload:
`goldberg-community-bff-private`, version
`8cfa680a-b28f-4927-836c-86668065ab6e`.
Remote bundle bytes match SHA256
`489a6133f653808e11d768ddc5f6e80249736ddec34f802d674aae313774d12f`.
Eight server-side secrets contain only new runtime credential/CA/server keys.
No administrative credential uploaded. GOOGLE_OAUTH_ENABLED=false;
COMMUNITY_BFF_ENABLED=true permits the independently authorized catalogue API;
database community intake remainsfalse. Logs/traces persisted logging disabled.

workers.dev=false, preview URLs=false, no custom domains or zone routes. No paid
resource provisioned. Free rejected custom CPU override100328; deployment removed
that override and retained enforced Free defaults. Owner Free/no-card checkpoint
retained; billing API visibility403 is still a limit on independent billing inspection.

Initially both private remote-binding and route-less Cron tests failed10063 because
this account had no registered workers.dev subdomain. Owner explicitly authorized
account registration while retaining all Worker public routes/previews disabled.
Registration succeeded and public-route denial was reread. A loopback-only caller
and a temporary route-less scheduled verifier exercise only private service bindings.
Initial tunnel TLS handshake failure is retained; no TLS verification bypass.
Actual hosted requests now executed through authenticated private service binding
and independently through the route-less scheduled verifier. Five negative API
cases PASS:OAuth503/admin401/cursor400/method405/missing404,all schema1/no-store/
redacted. Catalogue/feed503 instead of200: **hosted positive reads FAIL**.
Credential-free encrypted-protocol probe fails; socket-open metadata alone was
insufficient. Actual pooler TLS controls:system roots FAIL19/private CA PASS/wrong
hostname FAIL62. Raw platform StartTLS cannot configure the required custom CA.
No password submitted by TLS probes/no certificate verification bypass.

Temporary verifier/cron removed after bounded tests. A Free-only Hyperdrive
custom-CA verify-full option is prepared separately; owner approval pending
because earlier execution packet excluded it. Existing account inventory allows
Hyperdrive reads and has0 configurations. No Hyperdrive mutation yet.

## Android verification and debug deliverable

Full native d86 run40241571850:182 cases/180PASS/2 expected external Stage4 skips.
Final acceptance-only run40394903151 at1ee SUCCESS after a254-file Android source
provenance guard. Original artifacts downloaded and hashed. API36 emulator
emulator-5554: seeded0.10.1 schema8→10 offline upgrade,37 sessions/prefs retained;
Wi-Fi101→mobile100, Data Saver readback enabled→disabled, process-death SQLite
integrity/FK PASS. Five emulator cold launches1564–2007ms (no device-performance claim).
No physical phone touched; no current UI screenshot review/API26 execution.

Local host Android234 fresh JVM tests, debug/testAPK/lint PASS. Debug APK:
`.gradle/stage6-delivery/Goldberg-0.11-dev-stage6.apk`,
com.opus.piano/0.11-dev/code12, SHA256
`86fc13739a9bcfbb9896e6bb146310afda81dd815d5dcad92412f75efb957f24`,
trusted signer53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e.
CI-signed candidate844039ec65014cb833a7cec94421571ff6ae8e31c8375a4dbe1adf2a07d47b51
is a separate verified CI artifact; do not relabel its digest as the local APK.

## Remaining acceptance

- [ ] Actual hosted private Worker requests and Cloudflare TLS/Free CPU behavior.
- [ ] Approved Google OAuth Web Client ID, real sign-in and authenticated hosted
  permission/workflow tests; owner explicitly keeps OAuth disabled meanwhile.
- [ ] Full runtime write-role deployment only after OAuth/security/operational gates;
  catalogue-only credential deliberately cannot run submission/review/publication.
- [ ] Android→hosted private BFF end-to-end (local Kotlin HTTP→BFF→PG already passes).
- [ ] Operational backup/retention/restore and applicable privacy/legal acceptance
  from existing approved contracts; baseline synthetic restore is bounded evidence.
- [x] Source11e9bad ordinary-pushed;BFF40405714480/quality40405714475 SUCCESS.
  Final acceptance/evidence checkpoint follows separately;Issue31 OPEN/ProjectInProgress.

Public community networking remains disabled. All unexecuted checks remain open.

Latest diagnostics deployment: version `63b5089d-9e6b-4156-a10f-320ef0bc9f8f`, bundle SHA256 `02139f5b682b23774e60341d781d639b64fd20c7c6c2587bcb3316d1a32e0047`; remote bytes match. Static403 billing visibility limitation retained; no plan upgrade.

Final inspection: known disabled public Worker URL403; malformed control token400
before account data (not a valid-format invalid-signature token test). Fresh
Supabase Advisor0WARN/0ERROR/1INFO for intentionally default-denied migration ledger.
Loopback caller8787 and identified tail sessions closed; no listener remains.
Temporary verifier/cron removed;only private BFF remains. All254 Android/contract
file hashes match retained native source;local debug APK digest reverified.
Hyperdrive approval remains pending;no Hyperdrive or CA resource created.
