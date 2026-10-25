> **Current publication phase —2026-10-25:** local re-verification and scoped
> commit/push now explicitly authorized. See [publication record](READINESS-PUBLICATION-2026-10-25.md)
> and [next-stage gates](NEXT-PRIVATE-DEPLOYMENT-CHECKLIST.md). PRIVATE STAGING NO-GO.
> October24 preparation/read-only observations below are preserved history;they
> are not a fresh day-of hosted verification or a restriction on authorized source publication.

# Private deployment readiness — October 24, 2026

**Result: local preparation complete; private staging NO-GO. C4 OPEN;
6.0f unchecked.** Current instruction authorizes local preparation and read-only
verification/tracking. It does not authorize remote SQL writes, Worker deployment,
secrets upload, public routes, OAuth, personal data or paid resources.
ORD-A remains sequencing approval; Q1-D/D5b retains its bounded owner-reported
resolution. No fresh legal conclusion or operator details are inferred.

## Purpose, scope and progress

Prepare a reviewable Free-tier private staging packet from verified Stage 6
architecture and migrations, preserving all historical evidence/worktrees.
Scope: `spikes/stage6-cloud-readiness`, ACL proposal/assertion, C4/stage/AI tracking.
No Android, Room/opus.db, application identifiers, backup formats, signing,
historical migrations or existing prototype sources changed.

- [x] Inspect branch/worktrees/status/remote/tags/releases/milestone and Issue31.
- [x] Read architecture, ORD-A/CR prerequisites, C4, ACL and historical migrations.
- [x] Refresh authorized Supabase metadata and read-only catalog/Advisor evidence.
- [x] Check day-of prerequisites; retain each incomplete requirement as blocked.
- [x] Prepare narrow guarded ACL candidate and correct PUBLIC pseudo-role check.
- [x] Prepare atomic idempotent locked-schema candidate using unchanged 001–004 SQL.
- [x] Prepare inert Worker entrypoint, route-free cf template and server-secret contract.
- [x] Execute isolated synthetic SQL/ACL/RLS/replay/rollback/restore and shell checks.
- [x] Prepare exact future connectivity/authorization/backup acceptance steps below.
- [ ] C4 closure and owner disposition of remaining prerequisites.
- [ ] Separate security maintenance approval and hosted ACL fix/postflight Advisor.
- [ ] Separate synthetic schema/runtime/secret/restore authorization under ORD-A.
- [ ] Hosted connectivity, runtime measurements, backup and 6.0f owner acceptance.

## Direct read-only observations

October24 tool checks: existing Supabase project ACTIVE_HEALTHY,
eu-central-1, PostgreSQL17.11.0.003; organization free/tier_free. Catalog query
returns zero tables in public/goldberg_private; recorded migrations empty.
This does not assert that provider-managed schemas are empty or Auth disabled.
Account identifiers/display names/credentials are intentionally absent here.

`public.rls_auto_enable()` is owned by postgres, SECURITY DEFINER, with
`search_path=pg_catalog`, NULL proacl (default EXECUTE for PUBLIC); ensure_rls
enabled O, bound to the same function, three CREATE TABLE/CREATE TABLE AS/SELECT
INTO tags. Advisor returns exactly two external WARN findings: anon and
authenticated SECURITY DEFINER execution. No fix has been applied remotely.
The body only handles public tables, catches errors and logs; explicit RLS is
required on the private schema regardless of the trigger.

No Cloudflare account/billing connector or cf executable is available in this
session. Supabase organization metadata has no payment/invoice/add-on fields.
Resource-creation `get_cost` quotes do not establish existing bills and were not
used. **Cards, accrued/upcoming charges, add-ons, trials, upgrade paths and complete
usage quotas remain unverified for both providers. 0 CZK is a requirement, not
an observed billing result.** No paid/private-connectivity substitute is proposed.

## Day-of C4 disposition

All nine current C4 §M.4 steps were assessed. No prerequisite is silently waived.
The 33-item classification remains **8 PASS /19 PARTIAL /6 OPEN** with the same
bounded meaning as §M; local candidate tests do not close hosted/legal/account rows.
Individual day-of refresh/missing evidence is mapped in C4 §O.

| Gate | Disposition / evidence still required |
| --- | --- |
| D1 operator | Carry Q1-D bounded owner report; reassess changed scope only. Review date still unspecified. |
| D2 terms | Public sources refreshed; SB ToS current version/date selector still unextractable. Applicability/acceptance and complete safeguards remain open. |
| D3 billing | OPEN: read-only account evidence for both providers of Free, zero current/upcoming charges, no cards/add-ons/trials/automatic paid paths and quota usage. |
| D4 logs | Local template OFF; actual hosted fields/retention/pricing not inspected. Recheck Dec1 observability transition on execution date. |
| D5 backup | Local logical restore tested; Free entitlement/DPA generic-backup discrepancy, real zero-cost encrypted destination/custody/schedule/alerts and measured restore/RPO still open. |
| D6 security | Region/metadata/Advisor verified; SSL enforcement, MFA, exposed schemas, defaults/Auth/API configuration and hosted fail-closed tests remain open. |
| D7 transfers | Actual recipient/category/location map, safeguards/TIA access, quantified provider retention and assigned change monitoring remain open. No contact/subscription sent. |
| D8 authorization | This phase authorizes preparation only. Separate exact-SQL security approval and synthetic runtime/schema/restore/secret approval required after prerequisites. |
| D9 closure | Reconcile evidence, obtain explicit C4 disposition, then bounded ORD-A execution; 6.0f requires separate measured acceptance. |

## Synthetic verification and evidence

`python3 spikes/stage6-cloud-readiness/prepare.py` renders the reviewed candidate;
the runner asserts byte equality with regeneration.
`node --test spikes/stage6-cloud-readiness/worker.test.mjs` PASS: six method cases
covering five paths each (30 requests), inert secret/flag access and no cookie/CORS.
Node's active runner reports one passing file wrapper, not 30 native tests.
`python3 spikes/stage6-cloud-readiness/verify-local.py` final run
`results/goldberg-cloud-readiness-36e27ec5fc4c4750b97cbdee6fb2d1ea/result.json`:
**20 SQL/environment checks PASS**, source hashes unchanged, exact container and
volumes removed/absence verified. PG17 immutable image recorded in result.

Executed checks include ACL rollback/replay, trigger auto-RLS after revoke,
unrelated grant retention, disabled-trigger rejection, schema transaction rollback,
checksum mismatch rejection, exact replay preserving a synthetic principal,
all-table RLS/no policies, anonymous/authenticated denial and zero visible rows
with a real synthetic row present after temporary grants; logical export/restore
into a separate database and restored access denial after re-hardening.
The initial sandbox-denied attempt and intermediate 19-check run remain preserved;
only the final source-bound 20-check run is current acceptance evidence.

Not executed: encrypted backup drill (existing historical evidence retained),
Worker build/typecheck/cloud runtime, direct hosted DB transport, hosted RLS/ACL
mutation/postflight, provider quota/CPU benchmarks, browser/Android/Gradle/JVM/lint/
APK/instrumentation/UI/screenshots, physical devices, OAuth or live integration.
No APK was produced; APK path/hash/signing are not applicable to this preparation.

## Future execution packet — gated, not run

1. Obtain missing D2–D7 evidence and record owner C4 disposition under ORD-A;
   authorize exact maintenance/bootstrap/runtime scope and cleanup separately.
   Refresh read-only catalog, function definition/hash/ACL, schema inventory,
   defaults/exposed schemas, roles and trigger immediately before any write.
2. In a reviewed maintenance window, apply only the ACL candidate, retain exact
   private preflight, compare postflight definition/owner/OID/config/trigger and
   unrelated grants; rerun Advisor. If anything differs, rollback before commit.
   A later restoration uses captured effective grants; NULL proacl representation
   cannot be restored by generic grants. Never update system catalogs.
3. Bootstrap only the private candidate in one transaction; inspect catalog vs
   source, counts, ACLs/defaults/RLS/no policies and disabled intake; replay once.
   Do not change public defaults, provider schemas or grant service_role access.
4. Approve a minimal runtime-role/transport design and pin/review cf toolchain.
   Materialize/compile/dry-run the template locally. Verify account Free status,
   no ingress routes/workers.dev/previews, no automatic resources or paid bindings.
   Upload server secrets only under separate explicit authorization; never use
   postgres as runtime. Current shell needs no secrets and cannot enable APIs.
5. Separately approved **private** ingress/probe design must be proven first;
   disabled public URLs alone do not prove authenticated private access. Do not
   open a route just to run connectivity tests. Then verify TLS with a synthetic
   principal, timeouts/pause/network failure → fail closed, no secret in responses/
   logs, no remote personal rows. No paid Hyperdrive/IPv4/private-network shortcut.
6. Reuse approved synthetic authorization corpus: missing/expired/replayed session,
   account A→B isolation, role revocation, admin MFA, CSRF/origin/cookie safeguards,
   immutable audit/revisions, idempotent retries/concurrency. Test API client roles
   and service-role denial, RLS including seeded rows, and denied direct RPC calls.
   Keep G1–G9/G11 and 6.17 open until actual consumer/hosted verification passes.
7. Export synthetic data only to an approved zero-cost destination; use existing
   encrypted procedure with separate key custody, wrong-key/tamper rejection,
   retention/age alerting and measured isolated restore. Reapply private ACLs on
   privilege-free restores **before** any access. Target RPO≤24h; achieved hosted
   RPO/RTO remain unmeasured. Do not infer scheduled recovery from local pg_dump.
8. Disable test ingress/intake, revoke temporary secrets/grants, preserve logs
   without identifiers. Roll back incomplete SQL transactions. After commit,
   retain schema/data and fix forward or restore to isolation: no DROP/overwrite
   of the original. Remove only separately approved synthetic Worker/resources;
   never delete the existing project or other worktrees. Verify cleanup and billing.
9. Measure C2 §12/CR6 budgets and request 6.0f disposition with exact evidence.
   This remains separate from production activation, OAuth, personal collection,
   Stage8 integration and publication authorization.

## Continuation

Branch `feature/goldberg-0.11-stage-6-cloud`, starting HEAD
`006fa577ff4faef8ee7a25daf851620feb5bc3a8`. Working changes are the local packet
and tracking only. Existing Issue31 remains OPEN / Goldberg Development In Progress;
Stage7 owner acceptance observed in the latest Issue is preserved. No commit/push,
tag, merge, Wiki publication or release implied. Resume from this report and the
latest Issue checkpoint, verify source/result hashes, then acquire missing
read-only billing/security/legal/operational evidence; do not apply either SQL
candidate or deploy while any required gate is unsatisfied.

created by stokonn with love <3

## Final tracking/host checks

All six existing scripts PASS:`check_migrations.py` (v1–v9→v10 DDL),
`check_backup_fixtures.py` (three portable fixtures),`check_localization.py`
(951 EN/CZ resources),`check_history_paging.py` (106 sessions/four pages),
`check_agent_policy.py`,`check_stage6_acl_proposal.py`.
`python3 spikes/stage6-cloud-readiness/verify-evidence.py` and
`git diff --check` PASS. Android bridge allocation/runtime tests remain unexecuted.

Issue description and appended checkpoint synchronized and read back:
https://github.com/stokonn/Goldberg/issues/31#issuecomment-6388528121 .
OPEN and Goldberg Development In Progress confirmed. Local packet remains
uncommitted/unpushed at the starting HEAD above; no main/Wiki/release publication.
