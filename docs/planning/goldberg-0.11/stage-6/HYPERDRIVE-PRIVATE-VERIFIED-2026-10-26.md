## Current runtime lifecycle — 90days APPLIED andprivatehostedPASS (October27)

Owner explicitly approved manual90day validity fromexecution. Guarded expiry-only
SQL applied2026-10-27T04:39:50Z;newexpiry2027-01-25T04:39:50.052095Z. Existingpassword,
roleflags/memberships/connectionlimit4/ACL/RLS andall20tablecounts unchanged.
Fresh existing-password NodeTLS login andactualprivateWorker→Hyperdrive→Supabase
catalogue/feed200 plusnegativechecksPASS. Expired/seven-day statements below are
historical andsuperseded;no passwordreset/newruntimeidentity/bootstrap.

Safe manualrotation documented inCREDENTIAL-ROTATION.md. Importable secret-free
calendar reminders at30/14/7/1days generated;ownerimportrequired,not silently
scheduled. No auto-renewal/admincredential inCloudflare/GitHub/CI. Renewal evidence
inresults/runtime-renewal-2026-10-27. Publication commit recorded inIssue31/privatePMP.
Queued public catalogue task starts after this renewal checkpoint is pushed and
remote/cleanverified. Public/previews/OAuth/communitywrites remainoff until its
separate required conditions pass. No paidservice/release/mainmerge.

---

# Private Worker integration — hosted PASS, October26

The new owner deployment directive authorizes the routine reversible Free-only
transport work needed to finish this integration. Previous pending-Hyperdrive
statements are history. Public catalogue task remains queued until this private
checkpoint is committed/pushed/remote verified andtracking synchronized.

Existing Worker goldberg-community-bff-private and Supabase20-table schema reused.
Created only necessary Hyperdrive goldberg-stage6-catalogue
(3c684744936740c290361dcc0fb018dd) and PUBLIC Supabase CA record
(1c52bc7b-304f-4c7f-8179-dcb3e6df799f). Origin exact sessionpooler5432/postgres,
existing restricted catalogue login;verify-full/customCA/cachingdisabled readbacks
PASS. No subscription/card/trial/paid fallback requested. Free100000queries/day
errors on excess;owner Free/no-card evidence andprevious Free-only CPU rejection
retained. Billing403 inspection limitation remains;no claim of independent invoice access.

Two operational corrections were required:generated secret-name collision stopped
one upload before change;private binding exposes a virtual DB name and sslmode
parameter,not the public origin database name. Connector now accepts only private
*.hyperdrive.local addresses,one bounded virtual DB path,expected private-proxy
parameters;external hosts/overrides failclosed. Origin TLS remainsverify-full and
cannot fall back. Safe configuration diagnostics contain only booleans.

Actual hosted private service-binding requests PASS:catalogue200,feed200,
OAuthchallenge503,adminqueue401,badcursor400,wrongmethod405,missingroute404;
schema1/no-store/redacted;anonymous loopbackcaller403. Catalogue is empty;no real
records seeded/published andno fake sign-in. Worker remote bundle matches local
built bytes;public workers.dev/previews disabled,0domains. See exact version/hash
in results/hyperdrive-private-2026-10-26/worker-postflight.json.

Removed6obsolete directDB secret bindings in one supported bulk patch;onlyCURSOR_KEY
andIP_KEY remain inWorker. Origin credential remains solely inHyperdrive plus
protected owner-local custody;admin password never exported bylibpq/uploaded.
Role/grants/policies/data unchanged:20RLS/55policies/original47BFFgrants,intakefalse,
principals/sessions/audit/published/feed0,existingprobe1. No bootstrap/reset.
Credential expiry2026-10-27T03:00:38Z remains unchanged;durable activation requires
an approved runtime credential lifecycle,not silent extension.

Focused verification only:node services/community-bff/tests/fetch.test.mjs
11PASS;node services/community-bff/build-local.mjs14 actualworkerd requestsPASS.
Actual hosted7cases +anonymouscaller rejectionPASS. No full local/Android suite
rerun. Existing521backend/fullAPI36/upgrade andcanonicalKotlin1PASS remain bound
to their recorded source;new connector CI will be separately reported.

Durable private deployment project:services/community-bff/deployment,
cloudflare.config.ts andlockedpinnedcf1.0.0-beta.21/Wrangler4.152.0. It binds the
existingresource andhas no publicroutes/OAuth. Usecfbuild/cfdeploy--prebuilt
--provision=false withprotected2-key--secrets-file;neverCLIsecretvalues.

WholeStage6 remainsINPROGRESS:realGoogleconfig/authenticatedhostedworkflows,
Android→hosted andoperational/privacy/policy acceptance remainopen. Private
catalogue transport itself has now passed actual hosted acceptance.
