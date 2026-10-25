# Remaining private execution packet — NOT YET EXECUTABLE

October25 final integration supersedes the historical packet below. The authorized
ACL fix,private schema bootstrap andprobe role/marker creation are ALREADY COMPLETE.
They are excluded from every new request. Never rerun CREATE SCHEMA/CREATE ROLE or
revoke/bootstrap SQL to manufacture a clean starting state. Read-only reconciliation
matches20RLS tables,ledger checksum andoriginal identities;intake stays disabled.

## Exact remaining scope after prerequisites

- Existing Supabase project hlfsouvndjsbsaldttvi/Frankfurt/Free andexisting
  goldberg_staging_probe only. No new role,application grants,real data orintake.
- One target goldberg-private-verification;no BFF production deployment. The full
  BFF is locally executable but is NOT equivalent to the least-privilege probe.
- Immutable source andcorrected workerd bundle hashes:
  results/hosted-reconciliation-2026-10-25/remaining-payload-manifest.json.
  Hashes must be rechecked immediately before any approved upload.
- workers.dev=false,preview/versionURLs=false,no route/custom domain/assets,
  logs/traces disabled,probe initially false. Caller stays127.0.0.1-bound with
  private authenticated remote service binding andseparate probe token.
- No Access onboarding,Hyperdrive,paidIPv4,subscription,trial,card orupgrade.
  Owner Free/no-card evidence accepted as owner evidence;billing403 is a limitation.
- One ≤60min/≤100synthetic-request window. No real accounts,OAuth orpersonal data;
  no background keepalive orprovider-exhaustion experiment.

## Confidential provisioning prerequisite — unavailable

The connected SQL tool records its arguments/results;it MUST NOT carry a generated
password,verifier orconnection string. No confidential local Supabase administrative
connection ormanagement credential is configured here. Password remains unset.
A future reviewed provisioner must receive its administrative credential andexact
Connect host/CA through an owner-controlled600-mode protected file/FD outside Git;
read secrets only into memory,generate the probe password locally,send ALTER ROLE
through verifiedTLS without SQL tracing,anddeliver only probe credentials to server
secrets through confidential stdin/FD. Never command arguments,tool text orevidence.
No such provisioning was executed orclaimed ready. The same provisioner must bind
secret cleanup andshort-lived custody before approval. No privileged BFF role may
substitute for this restricted role.

## Runtime trust andaccess prerequisites — unavailable

Fresh Cloudflare account GET now403;the earlier successful account inventory is
historical. Exact account selection/resource absence cannot be refreshed.No login
orremote session is started to work around this. Restore existing authorized access
through the supported account setup before one consolidated deployment request.

Supabase metadata confirms direct db.hlfsouvndjsbsaldttvi.supabase.co. Shared pooler
host/index andactual certificate/CA must be obtained from Connect,never guessed
from region. Official docs distinguish Free directIPv6 andshared-poolerIPv4.
Workers pg-cloudflare forwards expectedServerHostname but custom Node ca is not
part of the Workers StartTLS options. NodeTLS tests therefore do not prove Workers
custom-CA enforcement. Verify system-trust certificate/hostname behavior in the
approved window;an unsupported trust requirement stops deployment,without bypass.

## Bounded verification andcontainment to include in the ONE later request

Authenticate local caller/control plane andprobe token;positive marker read;
missing/wrong credentials deny;public routes unavailable;probe app-table andwrite
permissions denied;privileged-function clients denied;TLS/hostname failures deny;
DB timeout/rejection/outage deny;response/log redaction;realCPU/cold/warm/request
measurements;Free/route/catalog/Advisor postflight andunchanged records.

On fault:stop requests,disable probe,dispose only the approved remote session;
apply the explicitly approved containment payload hash(NOLOGIN/revoke probe access,
preserve role/table/data),close only this probe's identified sessions andremove
only approved temporary secrets/Worker after inventory checks. Never DROP/reset,
restore over the existing project ordelete historical data. Password cleanup must
be explicit because the baseline had no configured password.

Operational Pi/SD backup automation is deferred.This authorizes no unattended
service andsatisfies no production backup gate.Disposable synthetic marker data
alone does not justify real collection. C4/real privacy/source/license/public API
version andwhole-stage acceptance remain separate.

**No consolidated authorization is requested yet:**account GET403,exact pooler/
trust evidence andconfidential credential delivery prevent a precise executable
request. Complete source/tests/CI/handoff independently;do not deploy publicly as
an alternative. Once prerequisites are bound,present only the remaining resources,
exact hashes,secret method,private exposure,window andcontainment in one request.

created by stokonn with love <3

<details>
<summary>Historical pre-bootstrap packet — preserved audit evidence,not executable instructions</summary>

> End-to-end continuation: hard B2/B4/B5/B6 inputs remain missing per explicit owner clarification. No hosted request/execution; see END-TO-END-ACCEPTANCE.md. Local backup/TLS preparation is implemented, not operational custody approval.

> B1 read-only access now SATISFIED; see [current prerequisite evidence](CLOUDFLARE-READONLY-2026-10-25.md). B2/B4/B5/B6 still block this future authorization.

# Exact private execution packet — NOT AUTHORIZED FOR EXECUTION

This is the single future approval-request basis, not an approval request today.
Use only after B1/B2 and applicable B4–B6 evidence in the Phase1/2 report is
resolved/disposed and exact payload hashes/private credential delivery are fixed.
Reuse accepted decisions; do not demand hosted acceptance before its bounded
ORD-A verification window can be authorized.

## Consolidated future authorization must identify

1. Existing owner project/account selected privately, current Free/no-card/zero
   charge posture, MFA, SSL enforcement, current catalog/route/secret inventory,
   legal/operational evidence and explicit C4 disposition. No resource replacement.
2. Exact unchanged `acl-fix.candidate.sql` and `private-schema.candidate.sql`
   hashes in `spikes/stage6-private-staging/candidate-audit.json`; new probe-role,
   containment and Worker payload hashes in the preparation manifest. Read-only
   snapshots must be refreshed immediately before writes; stop on any drift.
3. Narrow ACL transaction: revoke only PUBLIC/anon/authenticated EXECUTE on the
   existing event-trigger function; preserve definition/owner/OID/search_path,
   trigger and unrelated grants. Run postflight Advisor and a synthetic trigger
   check. In-transaction mismatch aborts. Later ACL restoration needs the exact
   retained preflight effective grants, never generic PUBLIC restoration.
4. Existing private bootstrap transaction, then the new probe-role transaction.
   Only `goldberg_private` objects and the new `goldberg_staging_probe` role/table/
   SELECT policy are added. No existing data, public defaults or provider schemas
   are replaced. Any pre-existing probe role/table causes a stop, not overwrite.
5. An exact, separately reviewed confidential role-password provisioning method;
   TLS-verified pooler host, server CA, username and short-lived credential custody.
   No secret values in approval text, GitHub, arguments, SQL evidence or app logs.
   This mechanism is still unbound and is not silently implemented by this packet.
6. One route-less target Worker `goldberg-private-verification` only if fresh
   inventory proves the name is unused or its exact known version is authorized.
   workers.dev, Version URLs/previews, domains and fetch routes OFF. No assets,
   Access/Zero Trust subscription, Hyperdrive/VPC/paid IPv4 or other resource.
   Upload only the exact tested payload and server-only secrets. Initially keep
   `STAGING_PROBE_ENABLED=false`; enable only for the approved synthetic window.
7. Local-only caller, explicitly 127.0.0.1-bound, with authenticated remote service
   binding and separately scoped existing-account session. Never deploy the
   caller or create a public URL. Provisioning a remote proxy/session is included
   as an explicit hosted action. No unauthenticated API request may reach data.
8. At most60 minutes active testing and100 synthetic requests for the first
   window, then stop. Quota uncertainty never justifies paid upgrade. No keepalive
   evasion of Supabase inactivity, production moderation or real user identity.
9. Exact approved encrypted export destination/key custody, synthetic-only export
   and isolated restore venue, age/RPO measurement and cleanup. Never restore
   over the existing project or use a real-data dump. Export/import must preserve
   private function ACLs/RLS; re-harden before access. No unattended scheduler or
   notification activation unless separately listed and authorized.

## Hosted acceptance sequence

- Read back payload/flags/route inventory before sending data. Verify workers.dev,
  Version URLs and other unapproved routes are unreachable; invalid CF control-plane
  credentials cannot obtain the remote binding. No URL-guessing security claim.
- With valid private session but no/wrong probe token, verify denial before DB.
  Authorized fixed probe returns only synthetic status. No public `/v1`, OAuth,
  registration, account data or moderation functionality is enabled.
- Verify exact role privileges, denied INSERT/UPDATE/DELETE/application-table/
  definer access, actual TLS certificate/hostname/password auth, timeout/outage
  fail-closed behavior and secret-free logs/responses. Do not infer per-user BFF
  authorization from this shared probe. Do not exhaust provider quotas.
- Verify Advisor postflight, explicit RLS/table ACLs, immutable baseline source/
  data identity and accepted event trigger. Measure real Worker CPU/cold start/
  network latency under C2§12/CR6; local Node/workerd measurements are not substitutes.
- Verify approved encrypted restore in isolation, role-password custody, private
  ACLs after restore, actual RPO/RTO and original data unchanged.

## Reversible stop and cleanup

On unknown billing, card requirement, unexpected objects/privileges/routes,
unauthorized request success, TLS failure, secret disclosure or provider error:
stop immediately, disable the probe and dispose the remote session. Within an
uncommitted SQL transaction use ROLLBACK. After commit use the reviewed
`disable-probe.candidate.sql`: NOLOGIN and ACL revocation, preserving role/table/
data. New-login prevention does not terminate old sessions; identify only this
probe role's exact sessions and obtain/execute the explicitly approved containment
method, never a blanket backend termination.

Revoke/delete only the temporary secrets/session/Worker explicitly included in
the authorization after source/inventory verification. Preserve the Supabase
project, application history, accepted trigger, data and recovery exports. No
DROP/recreate/reset rollback. Keep non-secret receipts and final billing/route/
credential/cleanup readbacks. Any further grant, re-enable, retry after a security
finding or production use needs a new bounded decision.

## Current disposition

**NO-GO. No specific hosted authorization requested or granted.** Hard account
access/financial evidence and named security/operational/disposition inputs remain
open. This packet defines an exact future approval scope; it does not bypass
those inputs. Once they pass, present their evidence, exact hashes, bound secret/
restore methods and remaining measured risks in one owner request before execution.

created by stokonn with love <3

</details>
