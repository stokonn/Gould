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
