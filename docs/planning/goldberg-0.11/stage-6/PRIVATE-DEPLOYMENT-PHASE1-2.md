> Continuation: [authenticated read-only inspection](CLOUDFLARE-READONLY-2026-10-25.md) supersedes the historical no-login status below. B1 SATISFIED; B2/B4/B5/B6 remain open in the precise scope recorded there.

# Private deployment Phase 1/2 — 2026-10-25

**Preparation advanced; hosted execution NO-GO.** The owner authorized fresh
read-only investigation and local implementation/preparation, followed by validated
source publication. Specific hosted changes still require the consolidated
authorization described below, after hard prerequisites pass. This report does
not reopen the chosen providers, region, existing project, budget, operator
decision, accepted Stage 7 scope or accepted Cloud Readiness tests.

## Scope and progress

Base `c8b903bbab147b239683c21204f524f7201849f0`, branch
`feature/goldberg-0.11-stage-6-cloud`. New `spikes/stage6-private-staging/`
preflight, synthetic probe role, containment SQL, server adapter, disabled handler,
route-less/local-caller templates and new verification only. No Android/Room,
accepted migration/ACL/shell, historical evidence or other worktree changes.

- [x] Recover Git, AGENTS, private PMP, C4, deployment checklist and Issue 31.
- [x] Collect fresh read-only Supabase catalog, plan, migration and Advisor facts.
- [x] Evaluate exact accepted ACL/bootstrap candidates against that catalog.
- [x] Investigate authenticated cf access, plugin and browser availability.
- [x] Identify a documented route-less service-binding testing path; exclude
  Zero Trust onboarding requiring payment details and public preview alternatives.
- [x] Prepare least-privilege synthetic role, bounded TLS adapter, authenticated
  disabled probe, private caller/config templates and reversible containment.
- [x] Execute only new role/adapter/caller/runtime tests; preserve accepted results.
- [x] Finalize bounded logging, retention, backup and execution requirements.
- [ ] Obtain missing account-specific evidence and external dispositions below.
- [ ] Obtain one exact hosted authorization only when prerequisites pass.
- [ ] Hosted mutations, connectivity, private-session and recovery acceptance.

## Fresh observations and exact candidate validation

`catalog-observation.json` records the read-only snapshot at
**2026-10-25 00:39:23 UTC**. The existing project is ACTIVE_HEALTHY, Free/tier_free,
Frankfurt, PG17.11. Catalog has no public/private application tables or private
schema; recorded migrations empty. Existing provider roles are available;
postgres has CREATE-schema and CREATE-role capability. These bounded observations
do not prove arbitrary hosted DDL is permitted by provider restrictions.

The function is postgres-owned SECURITY DEFINER, `search_path=pg_catalog`, default
NULL ACL, anon/authenticated EXECUTE true; `ensure_rls` is enabled, bound correctly
on ddl_command_end with the three expected tags. Definition SHA256:
`b21d88816c445d14cef5032c3cd4abaebc1db2001fcb3435022ab6b496182ca4`.
Both Advisor warnings remain present. Server SSL is ON; external SSL enforcement,
actual exposed API schemas, Auth settings and operator MFA remain **UNKNOWN**.

`candidate-audit.json` pins both exact accepted SQL files and confirms they are
byte-identical to c8b903b: ACL guard matches; PG17/empty schema/provider-role/schema
creation prerequisites match the bootstrap. This is read-only catalog compatibility
evaluation, not a remote DDL rehearsal, transaction rollback test or permission
guarantee. No hosted function/table/role/ACL/configuration was changed.

An additional read-only privilege snapshot confirms the connected executor is
the function owner, and PUBLIC has no CREATE privilege on the public schema or
current database. This avoids assuming those inherited defaults are restrictive;
the snapshot must still be refreshed before eventual role creation.

## Account evidence and retained decisions

Existing cf CLI reports `authenticated:false` / not logged in; no injected
Cloudflare API token, callable Cloudflare connector, discoverable Cloudflare
plugin or browser tool is available. No new login, token, temporary account or
claim deployment was started. CLI text suggesting a temporary public deployment
was ignored. Known existing accounts/project and chosen Free plans are retained.

C4's October 22 owner observation of Workers Free/no payment method is retained
with its original attribution. C4 explicitly says no screenshots or console
exports were supplied in that observation. No unseen screenshot is invented, and
no accepted choice is sent back for reapproval. Fresh charges/invoices, paid
add-ons/trials/upgrade paths and usage remain unobserved. Supabase Free metadata
cannot supply those fields; resource-creation cost quotes are not billing proof.

## Private transport result

**Prepared candidate:** deploy a route-less target only after authorization,
with workers.dev/version previews/domains/fetch routes OFF; use a loopback-only
local caller with a remote service binding over an authenticated Cloudflare
development/control-plane session. Both caller and probe require the short-lived
probe credential. Service bindings do not require a publicly reachable Worker
URL. The local-caller cf binding shape typechecks. No session was created and no
account entitlement, remote invocation or anonymous Internet denial was tested.
The development tool's listener must be explicitly bound to 127.0.0.1; URL Host
validation alone is not a network isolation control.

This avoids activating Zero Trust. Its official onboarding currently requests
payment details even for Zero Trust Free, so that setup path conflicts with this
project's no-card requirement. A random/public Version URL or a public URL with
only an application bearer check is not substituted for the private path.
Remote development uploads/session creation and resource binding are hosted
operations; they remain approval-gated. Actual private reachability/Free accounting
is a bounded post-authorization test, not something local typechecking establishes.

Sources accessed 2026-10-25:
[Service bindings](https://developers.cloudflare.com/workers/runtime-apis/bindings/service-bindings/),
[remote binding API and service-binding development](https://developers.cloudflare.com/workers/local-development/),
[Zero Trust onboarding](https://developers.cloudflare.com/cloudflare-one/setup/),
[public Version URL access](https://developers.cloudflare.com/workers/versions-and-deployments/version-urls/).

## Probe and privilege boundaries

The new role candidate runs after the unchanged approved ACL/bootstrap candidates.
It creates `goldberg_staging_probe`: no inheritance, superuser, role/database
creation, replication or RLS bypass; two connections, statement/query3s and lock1s
bounds. One constrained synthetic marker table and one SELECT policy; no application
table/function, service_role, contribution or moderation grants. Existing role or
table causes a stop rather than overwrite. LOGIN has no password until a separately
approved credential operation. Its actual confidential delivery mechanism and
verified pooler host/CA still require the account operator; no credential payload
is generated, requested in GitHub or embedded in SQL.

Adapter accepts only the probe role, expected server hostname, postgres database,
5432/6543 ports and verified TLS CA/hostname settings. URI SSL overrides fail closed.
It uses one fixed SELECT, no caller SQL/body, per-request connection and bounded
driver/server timeouts. Handler is disabled unless its exact flag is true, checks
HMAC-based constant-time credential verification first and returns only synthetic
status. Target DB secrets are read lazily after authorization. Shared probe auth
is not per-user BFF authorization; no account-isolation/moderation acceptance is
claimed. pg8.23.1 is pinned in the isolated driver lock; no runtime dependency or
new entrypoint is wired into the accepted readiness shell or Android application.

Sources: [Worker pg support](https://developers.cloudflare.com/workers/tutorials/postgres/),
[Supabase TLS/connection guidance](https://supabase.com/docs/guides/database/connecting-to-postgres).

## New executed verification

- **18 new PostgreSQL checks PASS**, final run
  `spikes/stage6-private-staging/results/goldberg-private-probe-a65fcef971334fd8a22a891f3454a4fb/`.
  Real local probe-role login, role flags, SELECT-only access, denied application
  table/definer/INSERT/UPDATE/DELETE access, actual3s statement timeout, API-role
  denial, transactional rollback, collision stop, logical restore denial before
  explicit grants, restored marker and NOLOGIN/ACL containment preserving data.
  Source hashes unchanged; exact UUID-owned container/volumes removed and absence
  verified. Accepted SQL was fixture setup, not a rerun of the20 accepted tests.
- **Eight new component test groups PASS** in three Node file wrappers: handler,
  TLS/driver factory and authenticated loopback caller. Only new files tested.
- **Strict core/config typecheck, packaged pg syntax, five local workerd requests
  PASS** in `results/component-final-8o7WsQ/`. Requests prove disabled503, missing/
  wrong auth403, unrelated route404 and authorized-but-unconfigured DB503. No
  database credentials or remote binding are supplied to workerd. Local telemetry
  is off; instances disposed. No hosted connectivity or successful TLS connection
  is inferred from these local negative checks.
- An initial strict type error was fixed by explicit token narrowing. Plain
  esbuild ESM pg import failed dynamic CommonJS require; the final tested build
  includes `createRequire('/worker.js')` rather than assuming Workers import.meta.url.
  Local workerd startup also required the pinned Miniflare5 legacy-option adapter
  and build-root correction. Earlier partial logs and probe runs remain preserved.
  No assertion or denied-access test was removed.

Accepted tests, encrypted drill, historical source CI and Stage7 evidence were
reused; no unchanged accepted suite or manual CI dispatch repeated. No local
Gradle, Android/native instrumentation, UI/screenshots, seeded upgrade or release
signing checks were performed. No release APK/path/hash/signing claim. Existing
push-triggered GitHub quality CI is separate from the new local checks.

## Logging, retention and backup requirements

**Local policy finalized for this synthetic scope:** invocation/application logs,
traces, persistence and exports OFF in target/caller templates. No credentials,
headers, URL queries, SQL errors, IPs or payloads in app logs/responses. Keep only
sanitized operator receipts (UTC time, source/artifact hash, check outcome and
cleanup state) in owner-controlled private custody. Probe rows/roles are retained
on containment rather than deleted. Test infrastructure lifetime is bounded in
the approval packet; provider-side security/control-plane retention remains an
external observation/review gap, not disabled by app observability settings.

**Backup requirements carried forward without rerunning accepted encryption:**
existing encrypted procedure, separate key custody, owner-controlled durable
0CZK destination, atomic publication, wrong-key/tamper protection, seven daily
copies plus monthly checkpoint, age/failure alert and isolated restore. Target
RPO≤24h; achieved hosted RPO/RTO still unmeasured. New role passwords are not in
logical dumps; credential restoration requires a separate approved secret step.
No scheduler or notification route is activated. Owner destination/key custody,
notification target and schedule are not available here and remain unbound.

Published Free pricing excludes automated backups while DPA generic wording still
describes daily backups. No Free restore entitlement is inferred; use the existing
logical-export mitigation and obtain applicability clarification/disposition.
Provider global account/support/telemetry processing remains distinct from the
accepted Frankfurt database location. Fresh ToS HTML still has no extractable
version/date; this documentary limitation is not a request to redo accepted Q1.

Sources: [backups](https://supabase.com/docs/guides/platform/backups),
[DPA](https://supabase.com/legal/customer-resources/data-processing-addendum),
[transfers](https://supabase.com/legal/privacy-resources/data-residency-and-transfers-faq),
[Workers log retention](https://developers.cloudflare.com/workers/observability/logs/workers-logs/),
[December1 transition](https://developers.cloudflare.com/observability/pricing/).

## Precise blocker classification

| ID | Class | Status and next executable action |
| --- | --- | --- |
| B1 | Access SATISFIED 2026-10-25 08:05 UTC | Owner login independently confirmed by Wrangler4.152.0 whoami and authenticated account/Workers GET. See CLOUDFLARE-READONLY-2026-10-25.md; earlier no-login observations are historical. |
| B2 | Hard financial evidence | Current both-provider charges/add-ons/trials/usage and no-card posture not observable. Reuse dated valid console evidence; collect only missing sanitized facts, not card/account details. Public pricing is insufficient. |
| B3 | Excluded path | Standard Zero Trust onboarding requires payment details. Do not activate it. Route-less service-binding candidate is prepared instead. |
| B4 | Security/secret prerequisite | Fresh account MFA, external SSL enforcement, actual exposed schemas/Auth posture, verified pooler host/CA and confidential role-secret delivery remain unknown. Bounded catalog compatibility passed; this does not close these items. |
| B5 | Operational prerequisite | Durable encrypted destination, separate recovery custody, scheduler/alert target and restore venue are unbound. Existing encryption acceptance is reused; do not fabricate operational readiness. |
| B6 | External applicability/disposition | Actual service/recipient/transfer/retention mapping, provider TIA/access and backup-entitlement interpretation require account/reviewer evidence or explicit bounded C4 disposition. Prior Q1-D outcome remains accepted. |
| B7 | Gated hosted acceptance | Actual authenticated remote-binding session, no-URL reachability, TLS/password auth, Advisor postflight, CPU/latency/quotas and hosted restore/cleanup. These are post-authorization tests under ORD-A, not impossible pre-deployment demands. |
| B8 | Out of scope/reference-only | OAuth/country terms, optional translation/legal-source retrieval and production per-user moderation are not synthetic probe work. No activation or new owner decision sought here; production gates remain separate. |

**No consolidated hosted approval is requested now:** B2 and unresolved
security/operational/disposition evidence prevent satisfying the preconditions.
The next executable step is existing-account read-only access plus missing
sanitized account/security/backup facts, not another accepted test run.

See [EXACT-PRIVATE-EXECUTION-PACKET.md](EXACT-PRIVATE-EXECUTION-PACKET.md) for the
future consolidated action set, rollback and stop rules. C4 remains OPEN; its
standing33-row classification8 PASS/19 PARTIAL/6 OPEN is not upgraded by local
tests. New source publication/remote/CI receipt is recorded in Issue31/private PMP.

created by stokonn with love <3
