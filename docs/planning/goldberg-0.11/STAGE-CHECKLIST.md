## Database Simplifier — implemented and host-verified; native acceptance pending

Purpose: make Library and Discover use one canonical catalogue,while preserving
personal practice/history/drafts/backups and all original legacy metadata.
Branch feature/goldberg-0.11-database-simplifier from verified e05c692 baseline.

- [x] Stage6public launch and integrated348JVM baseline verified/pushed; dedicated branch created.
- [x] Library catalogue rows/detail now use the existing Discover repository/store,
  provider identities and pagination; personal Library rows remain journal-owned.
- [x] Local compatibility import preserves original response bytes/metadata and
  all personal rows. Cache replay retains hash/license/source and splits before
  adapter limits; unbacked normalized rows retain unknown/unreviewed rights.
  Atomic per-response checkpoint permits idempotent retries; no network/Room
  schema change/destructive migration or original-row deletion.
- [x] Frozen-source host gates PASS:348JVM/0failures/errors/skips,debug APK,
  testAPK compilation and lint0errors. Exact counts/task/artifact records in results/.
  Earlier FIR lint crash retained; final run passed without disabling checks.
- [ ] Four realSQLite/native preservation/idempotence/paging/retry cases and one
  shared production-row/detail UI test,light/dark/2x component frames:written,
  compiled successfully; actual native execution pending.
- [ ] Final host/CI,seeded upgrade/native acceptance,stable APK identity/signing,
  docs/Wiki/Project/release publication remain pending. No newRC.

Source checkpoint committed/pushed before final CI/native dispatch; receipt in Issue31.
Never infer final native acceptance from compilation.


---

 Verified integrated Stage6 baseline — October27

- [x] Existing public catalogue backend10/10HTTPS/security requests passed once;
  database/security postflight unchanged,privacy/operational prerequisites recorded.
- [x] Integrate accepted Stage7/Stage8/Discover9b8266e with Stage6source6102b3f.
  Preserve both histories,legal/security settings,fixtures and all personal data.
- [x] Final integrated Kotlin compilation/JVM suite PASS; exact counts/command
  in stage-6/results/integrated-baseline-2026-10-27/host-verification.json.
  Localization1102,authored29fixture/provenance/policy/whitespace checks PASS.
  Shared error redeclaration found/fixed; no Room/backup/practice schema change.
- [x] Stage6 read-only release scope implemented and verified. Community intake,
  backend OAuth and remote personal-data writes intentionally remain disabled;
  real-provider authenticated workflows are outside the approved0.11launch scope.
- [ ] Commit/push integrated baseline; create dedicated Database Simplifier branch.
- [ ] Unify active Discover/Library catalogue data; final changed-scope native,
  migration/upgrade/CI,stable APK/signing/docs/Wiki/Project/release gates pending.

This is a scoped Stage6/backend+host acceptance,not a claim of final integrated
native acceptance or a populated public catalogue. Existing native/upgrade evidence
is retained with its original source identities; final release regression pending.
Issue31 OPEN/Project In Progress. No newRC or stable publication yet.

---

## Current Stage 6 — public read-only backend verified; integrated baseline next

Purpose/scope: existing public catalogue GET API on Worker/Hyperdrive/Supabase,
with practice/history/drafts/backups offline and community/OAuth/writes disabled.

- [x] Owner authorization6435585794; proportionate catalogue Article6(1)(f)
  assessment; separate support/rights bases; incorporated provider arrangements
  reviewed against actual accounts/configuration, with evidence limits disclosed.
- [x] Owner-reported manual Inbox/Spam checks and Tuesday/Friday monitoring,
  first-of-month retention review/private case tracking;90day ordinary-support
  retention adopted with justified exceptions. No agent Gmail access/automation.
- [x] Existing public workers.dev route enabled; previews remainOFF. ONE focused
  actual HTTPS smoke pass:10/10PASS. Catalogue/feed200(empty),cursor/query400,
  wrong method405,OAuth/submission/admin/practice/unknown404. JSON/no-store/
  redaction PASS. No new provider/service/subscription or backend deployment.
- [x] Postflight20exacttablecounts,intakefalse,runtime role flags/limit4/expiry
  unchanged. Earlier20RLS/55policy/verify-full/CA/cacheoff/two-key/logsoff evidence
  retained. See results/public-activation-2026-10-27 for actual request evidence.
- [x] Current notice/Android asset/CZ/EN synchronized;951localizations/policy/
  whitespace PASS.3254326 successful backend/quality CI reused; no full rerun.
- [ ] Integrate accepted Stage7/Stage8/Discover source with current Stage6; verify
  the integrated baseline before creating the dedicated Database Simplifier branch.
- [ ] Shared Discover/Library catalogue, data-preserving changed-scope tests,
  migration/upgrade/CI, final trusted Stable APK and release gates remain pending.

Backend milestone verified; whole integrated Stage6/release acceptance not inferred.
Issue31 OPEN/Project In Progress. Catalogue projection is empty,not populated;
licence/field/provenance approval precedes ingestion. Existing RC preserved.


---

## Historical checkpoint — superseded where inconsistent with the current entry

## Current Stage 6 — PR #39 reconciled; public launch requirements OPEN

Purpose: verify private catalogue operation and reconcile privacy/retention work
without replacing newer owner decisions. Scope: existing Worker/Hyperdrive/database,
legal documentation, Android legal asset and English/Czech summaries.

- [x] Reviewed draft PR #39 at `3cdd34f62c54f607ad5182f54f3ec0283f229ce2`;
  no merge. Reconciled its operations review with newer nickname/contact decisions.
- [x] Public developer **stokonn**, privacy/support **stokonn.us@gmail.com**.
  No private identity details published; no claim that nickname identifies the
  controller legally. Exact original approved notice preserved byte-for-byte.
- [x] Current notice and Android asset match; CZ/EN summaries synchronized.
  `check_localization.py`:951 resources PASS; `assembleDebug lintDebug`:PASS,
  47 tasks/14 executed. New identity review APK signature/package/version/ZIP,
  exact notice and actual runtime-secret scan PASS. Prior APKs preserved.
- [x] Fresh read-only Cloudflare API check: workers.dev/previews OFF,0 custom
  domains, catalogue-only true, Google false,2 server keys, deployed source matches.
  A stale session initially returned403; existing Wrangler session refreshed and
  the check then passed. No routing/deployment/account/SQL modification.
- [x] Prior actual private Worker→Hyperdrive→Supabase reads200 and negative
  permission checks retained. No new hosted request, public HTTPS or native/UI
  execution claimed for this documentation/resource change.
- [x] Source2f0cf0b CI completed:backend523 tests/0fails/skips plus24 workerd
  requests; host-quality build/JVM/instrumentation compilation/lint/policy SUCCESS.
  Those CI runs precede this identity-only Android resource update.
- [x] Runtime expiry remains2027-01-25T04:39:50.052095Z;90-day manual renewal
  preserved password/ACL/RLS/limit4/data. No administrator-based automation.
- [x] Provider primary pages rechecked; enabled-log visibility is not a universal
  deletion deadline. Routine mailbox90-day deletion remains a proposal, not policy.
  Synthetic rights-request desk review completed; no inbox configured/contact sent.
- [ ] Formal legal controller identification/authority and any required guardian
  or appropriate legal review. Processing basis and provider account-specific
  retention/transfers remain OPEN; nickname/contact approval does not resolve them.
- [ ] Owner adopts and operates mailbox schedule, restricted case storage,
  reviewer and rights procedure. Future content licensing/fields before ingestion.
- [ ] Required launch conditions and final public-route approval; public HTTPS
  activation and live public checks NOT EXECUTED. OAuth/community/writes stay off.

Stage6 remains IN PROGRESS; Issue31 OPEN/Project In Progress. No paid activation,
main merge, tag, RC or Android release. Current records: LEGAL-REVIEW-0.11.md,
PRIVACY-LAUNCH-OPERATIONS-REVIEW-2026-10-27.md and results/privacy-review-2026-10-27.
New notice SHA256:68356de457b28d726af6a836fbb841b7c47a927823ae31adafb3be8ee9d48586.
New APK SHA256:4ed6b5b00497ba69e98c69a9a818975fbd12a21f7df93b36143d2ce73b26ee81.
Next: resolve owner/reviewer gates confidentially; do not collect identity details
in public tracking. Only after required conditions and final approval activate
read-only routing and execute minimal live security checks. Commit in Issue/PMP.

---

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

## October27 expiry checkpoint — public task remains queued

Private integration sourceeea407b andboth essentialCI passed. Hosted positives and
negativecases passed within the October26 authorized window. Current clock is
past runtimeexpiry2026-10-27T03:00:38Z:do not claim current availability orretry
expiredlogin/resetpassword/extendrole automatically. Finish/push this truthful
checkpoint;public activation remainsdisabled pending runtime lifecycle decision.
No mainmerge/RC/release/newpublicendpoint. All prior source/evidence preserved.

---

## Current private integration — Hyperdrive hosted PASS, October26

The owner’s new deployment directive authorizes reversible Free-only transport
completion. The earlier Hyperdrive exclusion/pending-decision checkpoint below
is superseded for this exact private CA/verify-full/cache-off scope.

- [x] Existing Worker/database reused;one necessary Hyperdrive and public CA added.
  No schema/data/RLS/password/bootstrap changes. Runtime role remains read-only,
  hard4connections,expiryOct27 03:00:38UTC;no admin runtime credential.
- [x] CIat eea407b:backend40409288204 SUCCESS522bodies;quality40409288198 SUCCESS.
- [x] Actual hosted private catalogue/feed200,OAuth503,admin401,cursor400,
  method405,missing404 and anonymous caller403 PASS. No public routes/previews.
- [x] Hyperdrive verify-full/exactCA/cacheoff readback PASS. Worker now carries
  only cursor/IP keys;6obsolete directDB secretbindings removed in one bulk patch.
- [x] Focused Fetch11PASS and actual local workerd14PASS;no full local regression
  repetition. Existing comprehensive521/Android/native receipts remain historical.
- [x] Database postflight20RLS/55policies/original47BFFgrants/data/intake preserved.
- [x] Sourceeea407b94254e7c8d5153da3eecbaf389f32e1cd committed/pushed;
 exactremote/clean verified;Issue31comment6428398033/privatePMP/handoff updated.
- [ ] Public catalogue activation is QUEUED;do not begin until private checkpoint
  complete. OAuth remainsdisabled;wholeStage6 real-provider/operational gates open.

See HYPERDRIVE-PRIVATE-VERIFIED-2026-10-26.md andresults/hyperdrive-private-2026-10-26.

---

## Stage 6 private integration — 2026-10-26 21:33 UTC

Current branch feature/goldberg-0.11-stage-6-cloud,source11e9bad;whole Stage6 **IN PROGRESS**.
Current authoritative report: docs/planning/goldberg-0.11/stage-6/PRIVATE-INTEGRATION-2026-10-26.md.
This supersedes historical connectivity-BLOCKED/no-Worker statements below.

- [x] Owner dbname correction;actual verify-full TLS1.3/Node→Supabase catalogue200.
- [x] Existing schema encrypted backup + isolated network-none restore PASS.
- [x] New catalogue-only login,4SELECT+1lock column/5newpolicies,expiresOct27 03:00:38UTC.
  Existing20RLS tables/data/roles/passwords/47BFFgrants preserved;intakefalse.
- [x] Fresh final-source canonical Kotlin→HTTP/BFF→PG1PASS,0fail/errors/skips;
 26/26Gradletasks executed/sourceunchanged/fixturecleanupverified.
- [x] Current local backend521 bodies PASS/0fail/skips;credential custody5PASS;
  actual local workerd package14PASS. Containers/volumes removed andverified absent.
- [x] Private BFF actually uploaded;8runtime secrets/no admin. workers.dev/previews
  disabled/0publicdomains. Owner authorized accountsubdomainregistrationonly;done.
- [x] Actual hosted negative requests503/401/400/405/404 PASS;redacted/no-store.
- [ ] Hosted catalogue/feed positive readsFAIL503. Exact pooler TLS:system roots
  fail19/approvedprivateCApasses/wronghostnamefails62. Raw Worker TLS protocolprobe
  gets no encrypted response;never weakenverification. Temporary verifier removed.
- [ ] Owner approval pending for concrete Free-only Hyperdrive verify-full/customCA,
  cacheoff/privatebinding proposal;earlierpacketexcludedHyperdrive. GETaccess/0configs.
- [x] API36 fullnative d86:182cases/180PASS/2expected external skips;1ee acceptance
  40394903151 SUCCESS (254Androidfiles identical). Offline seededupgrade37sessions,
  network/DataSaver/processrestart/integrity/FK PASS. Originals downloaded/hashrecorded.
- [ ] Approved Google OAuth Web Client ID absent;ownerkeepsOAuthdisabled/no placeholder.
  RealOAuth/authenticatedhostedworkflows/Android→hosted/operationalbackup/privacy
  acceptance pending. No release/main merge/publiccommunity/realuserdata.
- [x] Source11e9bad5c775838843d4a47e524328d69f35e229 committed/ordinary-pushed;
  exact-source BFF CI40405714480 andquality40405714475 bothSUCCESS.
- [x] Final evidence/acceptance ledger/Issue31/Project/privatePMP checkpoint prepared.
- [ ] Hyperdrive approval pending;GoogleclientID/realOAuth/fullhosted/operational gates open.

---

## Stage6 independent hardening — in progress after owner hosted block

Purpose:close unauthenticated API abuse andactual emulator failures onthisbranch.
Scope:BFF authentication throttle/reused abuse-limit table;three alreadyverified
bundle implementation files,three fixture tests andoffline upgrade helper from
preserved a73a7e2 source. No Room/schema migration orotherworktree edits.

- [x] Implement shared30/minute coarse-IP HMAC authentication budget,atomicPG
  counter andRetry-After;failed credentials count;missing peer metadata shares a
  bucket;outage denies beforechallenge. No rawIP/token storage orschema change.
- [x] Focused12 BFF/PostgreSQL tests PASS,includingnewquota concurrency/recovery.
- [x] Final517backend/actualforcedKotlin1/234freshJVM/APK/testAPK/lint PASS.
- [x] Preserve exactAPI36a3d99failure:182cases/13failures/2skips(167PASS),
  all13 inoldStage5bundle runner/store/worker tests;upgradeofflineassertionfailed.
- [x] Inspect/reuseverifieda73a7e2 offline core andfixture corrections;preserve
  timestamps/retry/readPublished semantics;offlinehelperwaitsforactualradiodown,
  acceptance restoresairplane/mobile_data before network transitions.
- [x] Commit/ordinarypush/remoteexact d86de8f50dece8d16ad7bd4af811c7facd1b974a.
- [ ] Exactsourcequality40241558565/BFF40241558522/API36+upgrade40241571850running.

Hosteddeployment/functionalverification remainsBLOCKEDbyowner IPv6/pooler-auth
report. No hostedDBlogin/reset/retry/TLSweakening/paidIPv4/release. Existing inert
runtimeACLs preserved. Priorquality/BFFCI a3d99PASS,notattributedtonewsource.

---

## Owner connectivity directive — hosted BLOCKED (October25,2026)

Owner reports:no working host IPv6 route for directSupabase;sessionpooler reachable
overIPv4 but rejects authentication after DB passwordrotation. These are owner
observations,not agent-executed hostedlogin tests. Do not resetpasswords,retry
authentication repeatedly,disableTLSverification orbuyIPv4addons. No release.
Hosted deployment andfunctional verification explicitlyBLOCKED untilauthenticated
connectivity/security prerequisites are resolved byowner. Continueindependent work.

Cloudflareaccess is nowRESTORED:installedWrangler revalidatedexistinglogin;fresh
GET-onlyinventory22:28UTC succeeded(1account/0Workers/services/domains/zones/MFAon).
No newlogin/Worker/publicroute/secret. Billing403visibility limitation retained.
Previouslyapplied NOLOGINrole/grants/policies remaininert;do not replayorundo them.
No furtherhostedmutation/deploymentwhileblocked. No hostedDBlogin wasattempted.
Next minimalowneraction:resolve authenticated session-pooler configuration outside
thisagent run,thenprovide protectedserverconnection/TLStrust configurationand
explicitlyconfirmconnectivity/securityprerequisites are resolved. No secret values
inchat/Git/logs. GoogleOAuthconfigurationremainsneededforreal sign-in.

---

## Current source and verification — a3d99dc (October25,2026)

Purpose/scope: finish Stage6 server runtime identity,least-privilege PostgreSQL
access andsource-bound Android/backend verification;preserve allotherworktrees.
Branch feature/goldberg-0.11-stage-6-cloud. Source/remote
`a3d99dc928769cbbaf5dd01bb6f6b567192bb5c8`;current documentation update only.

- [x] Reused existing20-table hosted schema/ledger;no bootstrap/data replacement.
- [x] Fixed GoogleJWKS transport,pg socketcleanup,restrictedACLplan,forcedclientrun.
- [x] 515backend bodies PASS/0failures/skips;exact source/rawhashes/fixturecleanup.
- [x] Actual forced Kotlin/HTTP/BFF/PG1PASS;8workerdpackage/6archive/6TLSrestorePASS.
- [x] Hosted NOLOGINrole/47tablegrants/49server-rolepolicies deployed;exact readback.
  No elevatedflags/inheritedroles/ownership;onlyownerADMINwithoutINHERIT/SET.
  Clientdenial/ledger/20RLS/intakefalse/applicationdata unchanged;Advisor1INFOonly.
- [x] Initialguard/metadata-probe failures rolledback;PG17exactcorrection tested.
- [x] Debug0.11-dev/code12com.opus.piano APK/testAPK/lint;trusted53efcertificate,
  baseline0.10.1compatibility/ZIP/artwork/fixtureexclusionPASS. APK SHA256
  `a1213cfb3b4f081712d56e1fa096de3aed3ab4d67c64a0dc15e36152f94adfe2`.
- [x] Commit/ordinarypush/exactremote;prior5b22quality40239495426/BFF40239495452SUCCESS.
- [ ] Currenta3d99quality40240146586/BFF40240146602/API36+upgrade40240154511running.
  FirstAPI36run40239512295failed before native execution;Bash/certparserrepaired.
- [ ] Worker/privateTLS/runtimepassword/login/realOAuth/hostedBFFacceptance:
  CFaccessrestored;ownerDBIPv6/pooler-authblock. No publicroute/realuserdata.

WholeStage6 INCOMPLETE;Issue31OPEN/ProjectInProgress. Existingroles/policiesare
alreadydeployed:do not rerunbootstrap/CREATE ROLE/policyplan. No paidfallback,
physicalphone,mainmerge,RC/tag/Release. No screenshots inspected thiscontinuation.
Next:finish exact-sourceCI/nativeXML/upgrade readback,fix demonstrated failures,
finalreceipt/docs/Issue/privatePMP. See stage-6/RUNTIME-CONTINUATION-2026-10-25.md.


---

## Published release candidate — October25,2026

Goldberg0.11-rc.1 is published as a prerelease,source/tag a73a7e21adf656dcbe7ed6da351a71e9a681a384.
Signed non-debuggable release/code12/trusted53efcertificate;SHA256c5662d8ead2b175fac0be09145776e84936c2b8f794c85b967f71aaab1938bb5.
302JVM PASS;API36native203/192pass/11existing fixture-probe skips/0failures;
seeded upgrade/offline/process-death integrity PASS. Essential CI SUCCESS.
Stage6engine f300b09:506bodies andactualKotlin/HTTP/PG1PASS;hosted Supabase20RLS
bootstrap verified. Production networking/real-user collection remainsdisabled;
no Worker/credential/session deployment. Finalcloud gates andStage6 acceptanceOPEN.
Actual release andremaining blockers: https://github.com/stokonn/Goldberg/releases/tag/v0.11-rc.1
Source/CI/artifact details: docs/planning/goldberg-0.11/stage-6/RELEASE-CANDIDATE-VERIFIED.md.
No main merge/physical phone/public cloud activation. Earlier checkpoints below historical.

---

## Current final integration checkpoint — October 25, 2026

Stage6 IN PROGRESS / PARTIAL. Supabase bootstrap is HOSTED VERIFIED:20 RLS tables,
accepted private ledger v1 checksum,intake disabled,marker-only probe,client
function EXECUTE denied;19 INFO Advisor findings only. Earlier empty-schema/ACL
warnings below are historical and superseded. No new hosted mutation performed.

New executable BFF composition:504 test bodies PASS (14 new Fetch/PG/workerd,
490 existing backend bodies),0 failures/skips in source-bound run local-7acbff96394f44cb9619e7bc75160ac8.
Actual local workerd TCP/PG passes after workerd export-condition fix;8 packaged
fail-closed requests also pass. Final updated-source/CI validation pending.
Isolated Stage8 branch integrate/goldberg-0.11-canonical-client at42f6897 baseline:
canonical application service/transport wiring,302 JVM tests PASS;APK/testAPK build
and lint0errors132warnings1hint PASS. New native tests compile/execution pending.
Kotlin real HTTP/PG integration in progress;local emulator crashes retained.
No UI adoption or whole-stage acceptance inferred. Accepted Stage7/8 worktrees intact.

Owner Free/no-card evidence retained;billing403 is visibility limitation. Fresh
Cloudflare account GET now403 (prior successful observation retained). No new
login,remote session,secret or Worker deployment. Pooler host/CA/confidential
credential delivery and actual hosted runtime remain unresolved. Pi/SD operational
backup automation deferred;production collection/license/privacy gates remain.

Detailed living tasks and next steps:
[final integration record](stage-6/FINAL-INTEGRATION-2026-10-25.md).
Issue31 OPEN/Goldberg Development In Progress;G10 disabled;6.0f/6.17 unchecked.
Current source changes uncommitted. No main merge,release or physical-device use.

---

## Final source publication receipt — October25,2026

Source be5104476cf6cb059b0f206b2c4f18027d6bc657 ordinary-pushed; remote exact.
Automatic CI40188291121 COMPLETED/SUCCESS at that exact source: clean Gradle APK/
JVM/instrumentation compilation/lint and host data/policy/backup/evidence checks.
Local234 JVM/20canonical,6archive and6actual local TLS/restore results remain
source-bound in android-json-final/; CI compilation is not native execution.
Earlier5fddbd0 CI40177518593 SUCCESS retained separately. No manual rerun/dispatch.

This final receipt changes documentation only; executable/test/schema/workflow
inputs match the successful source. It uses[skip ci]toavoid repeating accepted
unchanged tests; sourceCI is not falsely attributed to a later documentation SHA.
Final documentation HEAD/remote and exact Issue/Project/private PMP readback are
recorded in the final Issue31 comment. Stage6 BLOCKED; no hosted operation or
whole-stage acceptance inferred from source publication. No main/Wiki/release.

---

## Stage6 final source hardening — LOCALLY VERIFIED / HOSTED BLOCKED (2026-10-25)

- [x] Android-independent normalized duplicate-key rejection and isolated GnuPG
  timeout cleanup implemented and meaningfully regression-tested.
- [x] Final234 JVM/32suites/0failures/errors/skips,20new canonical; debug APK,
  instrumentation compilation and lint0errors96warnings PASS. No native execution.
- [x]6archive lifecycle and6actual local PG TLS/archive/isolated-restore checks
  PASS; exact disposable container/volumes absent; source/raw verifier PASS.
- [x] Prior source5fddbd0 ordinary-pushed/remote exact/CI40177518593 SUCCESS;
  final hardening source publication/CI receipt pending at this checkpoint.
- [ ] Hosted B2/B4/B5/B6,real policy/public version,actual Stage8 consumer/native
  integration,6.0f/6.17 and whole-stage acceptance remain OPEN. G10 disabled.

24-row evaluation unchanged:2complete decisions/9local/2deferred/11blocked;
formal closure8.3%,local-or-complete coverage45.8%,not an estimated code percentage.
Final current evidence:results/end-to-end-2026-10-25/android-json-final/ under
stage-6; END-TO-END-ACCEPTANCE.md has exact scope/commands/artifact/limitations.
No hosted writes,production networking,paid/public/OAuth/real-data/merge/release.

---

## Stage6 end-to-end continuation — LOCAL ENGINEERING VERIFIED / BLOCKED (2026-10-25)

Purpose/scope: canonical Kotlin contract/API/session/review/audit adapter and
local encrypted archive/health/isolated restore implementation, new TLS/backup
composition, acceptance/tracking. Preserve all accepted source/worktrees/history.

- [x] Recover actual56ff724 baseline and prior uncommitted read-only docs; accepted
  Stage7A/7B/8 status and shared490-body backend evidence retained.
- [x] Existing CF OAuth/actual GET access confirmed; SB Free/healthy/Frankfurt/PG17.11
  and unchanged expected function/trigger/two warnings; no hosted writes.
- [x] New Kotlin canonical adapter: strict DTO/int8/microseconds/nulls/UTF8,
  refresh/account/logout,exact replay/reconfirmation,server reads and audit integrity.
  G10 production transport remains disabled; no accepted Stage7 source changed.
- [x] Frozen-source host gate PASS:233JVM/32suites/0failures/errors/skips,19new;
  debug APK/instrumentation compilation/lint0errors96warnings. Instrumentation NOT RUN.
- [x]5new encrypted archive lifecycle tests;6new actual local PG TLS/archive/
  isolated-restore checks PASS. Exact fixture container/volumes absent. All existing
  data/policy/fixture/whitespace checks PASS; accepted backend/crypto suites reused.
- [x] Per-requirement24-row evaluation:2complete decisions,9locally verified scopes,
  2deferred actual-consumer/native integrations,11blocked. Formal closure2/24=8.3%;
  local-or-complete11/24=45.8%,not a code-completion estimate. Stage6 INCOMPLETE.
- [ ] B2 financial/B4 remaining security-secret/B5 durable custody/B6 disposition;
  public-version/real policy/native adoption/hosted runtime/6.0f/6.17 remain open.
- [ ] Single exact hosted approval only after prerequisites; no hosted request now.

Details:docs/planning/goldberg-0.11/stage-6/END-TO-END-ACCEPTANCE.md,
CANONICAL-KOTLIN-ADAPTER.md,LOCAL-BACKUP-EXECUTION.md and source/hash/raw evidence.
No UI/ADB/screenshots/seeded upgrade/hosted/OAuth/real-data/paid/merge/release action.
Issue31 OPEN/Goldberg Development In Progress; C4 remains8PASS/19PARTIAL/6OPEN.
Source commit/push/exact remote/CI receipt is maintained in Issue31/private PMP.

---

## Stage6 authenticated read-only access — B1 SATISFIED (2026-10-25)

Purpose/scope: recover existing preparation and verify Cloudflare account access,
inventory, security and financial-read availability; documentation/tracking only.
Branch feature/goldberg-0.11-stage-6-cloud; local/remote HEAD56ff7245b3f837a3721e3a4138a3abe35bc78015.
**PRIVATE STAGING NO-GO; Stage6/6.0f incomplete.** Earlier no-login statements below
are historical and superseded by this observation.

- [x] Recover Git/Issue31/Project/milestone/private PMP and existing preparation.
- [x] B1: Wrangler4.152.0 whoami and actual account/Workers GETs succeed; owner
  login independently confirmed. No token values copied or reported.
- [x] 0 Workers/services/custom domains/zones; proposed name unused. Subdomain
  GET404; default_usage_model=standard is not billing proof. CF user MFA=true,
  account-wide enforcement=false. No setting changed.
- [x] Billing/subscriptions/profile GETs each403; retain Free decisions and prior
  evidence without inferring current charges/card/add-ons/usage.
- [x] Existing exact-HEAD CI40165862051 read back COMPLETED/SUCCESS; no rerun.
- [x] Free/route-less design reviewed; accepted local tests/encryption reused.
- [ ] B2 financial facts/model reconciliation; remaining B4 SB/security/host/CA/
  confidential provisioning; B5 destination/custody/restore/schedule-alert binding
  or scoped disposition; B6 applicability/transfer/retention/backup disposition.
- [ ] One exact consolidated hosted approval after prerequisites; B7 session/TLS/
  private denial/CPU/Advisor/restore/cleanup executed only inside that window.

Full evidence, exact blockers/commands and next steps:
`docs/planning/goldberg-0.11/stage-6/CLOUDFLARE-READONLY-2026-10-25.md` and JSON.
First executable hosted action now: read-only missing-evidence collection.
First future hosted mutation after approval: guarded narrow ACL transaction,
then approved bootstrap/probe/disabled route-less Worker sequence. No deployment,
SQL/config/resource/secret/session/public URL/paid action; no new tests/APK/UI.
Issue31 OPEN / Goldberg Development In Progress; C4 remains8PASS/19PARTIAL/6OPEN.
Exact description/comment readback PASS: https://github.com/stokonn/Goldberg/issues/31#issuecomment-6395235069
Private PMP append/receipt saved; documentation remains uncommitted at56ff724.

---

## Stage6 private deployment — Phase1/2 prepared,hosted NO-GO (2026-10-25)

Purpose/scope:fresh read-only gate evaluation,new synthetic probe/access/private
transport preparation and tracking. Basec8b903b;accepted provider/operator/Stage7/
Cloud Readiness decisions and evidence reused. No accepted test suite rerun.

- [x] Fresh catalog/Advisor/plan metadata and exact unchanged candidate guards
  evaluated:expected function/trigger,empty application schema,SSL server ON.
  External enforcement,MFA/exposed schemas/Auth/API and billing remain UNKNOWN.
- [x] CF access investigated:CLI not logged in,no token/connector/browser.
  Standard Zero Trust Free onboarding requires payment details:excluded.
  Route-less remote service-binding path prepared,not hosted-verified.
- [x] New least-privilege synthetic role/marker-only access,TLS fixed-query
  adapter,authenticated disabled probe/local caller and containment packet.
- [x]18 new PG checks PASS (a65fcef9);8 new component groups in3 file wrappers;
  5 local workerd requests;strict core/config types,bundle/syntax/source/raw
  integrity PASS. Accepted SQL used only as fixture setup;old test bodies untouched.
- [x] Logging/retention/encrypted backup requirements and B1–B8 blocker classes
  finalized. Read-only compatibility is not hosted permission/runtime proof.
- [ ] Hard access/financial/security/operational/disposition inputs;one exact
  consolidated hosted approval only after prerequisites genuinely pass.
- [ ] Hosted execution/acceptance. No specific hosted authorization requested.

Reports:docs/planning/goldberg-0.11/stage-6/PRIVATE-DEPLOYMENT-PHASE1-2.md
and EXACT-PRIVATE-EXECUTION-PACKET.md. C4 OPEN8/19/6;6.0f unchecked.
Issue31 OPEN/Goldberg Development In Progress. Source publication/remote/CI
receipt lives in the latest Issue checkpoint/private PMP. No real data,public
routes,OAuth,paid setup,main merge or release;other worktrees preserved.

---

## Stage6 Cloud — re-verification/publication (2026-10-25)

**PRIVATE STAGING NO-GO.** Owner authorized scoped review,reruns,commit/ordinary
push only. Branch `feature/goldberg-0.11-stage-6-cloud`;base006fa577.

- [x] Recover Git/Issue31/private PMP;review all changed/new files;preserve old
  result bytes and all existing worktrees. Correct cf worker nesting;strengthen
  expected-error assertions and command/source/cleanup evidence.
- [x] 20 PostgreSQL checks PASS in332ae776;30 source Worker requests PASS;six
  validators/independent hashes PASS. Strict typecheck/local535-byte ESM bundle
  and30 bundled Worker requests PASS with pinned temporary toolchain.
- [x] Full150-file committed scope/credential/hash audit PASS;ordinary-pushed
  source2c38177c422d9b7b6cd697dec36d5484744423a6;remote SHA exact.
- [x] Exact-source CI40145692652 SUCCESS:Gradle clean assembly/JVM/instrumentation
  compilation/lint and host checks PASS (5m12s,83executed/1up-to-date).
- [x] Final documentation/source-CI receipt prepared. Final documentation HEAD
  receives its own remote/CI readback in the final Issue31/private PMP receipt;
  sourceCI is never attributed to a later SHA.
- [ ] C4 billing/privacy/security/backup/owner closure;hosted ACL/runtime/schema/
  private-ingress authorization and6.0f acceptance remain open.

Purpose/scope,commands/source/result paths and honest unexecuted checks:
`docs/planning/goldberg-0.11/stage-6/READINESS-PUBLICATION-2026-10-25.md`.
Exact next-stage gates:`docs/planning/goldberg-0.11/stage-6/NEXT-PRIVATE-DEPLOYMENT-CHECKLIST.md`.
C4=8 PASS/19 PARTIAL/6 OPEN;Issue31 OPEN/Goldberg Development In Progress.
No remote schema/Worker/OAuth/public/paid/data/merge/release action.

Published source checkpoint:https://github.com/stokonn/Goldberg/issues/31#issuecomment-6391267830
Final publication/CI receipt:consult the latest Stage6 Cloud checkpoint in Issue31.

---

## Historical October24 preparation checkpoint (preserved)

## Stage 6 private cloud deployment readiness — 2026-10-24

**Purpose/scope:** prepare Free-tier private schema/ACL/Worker/verification
candidates locally. Authorized preparation complete; **private staging NO-GO**.
Branch `feature/goldberg-0.11-stage-6-cloud`, base
`006fa577ff4faef8ee7a25daf851620feb5bc3a8`; work remains uncommitted locally.

- [x] Existing project/organization read-only metadata:healthy,Free,Frankfurt,PG17.11;
  zero public/private application tables and recorded migrations;two ACL warnings.
- [x] Assess33 C4 rows/9 day-of steps; retain8 PASS/19 PARTIAL/6 OPEN and billing gap.
- [x] Prepare narrow ACL correction preserving ensure_rls; fix PUBLIC pseudo-role
  assertion; prepare atomic checksum-guarded 001–004 private schema and inert
  route-free Worker shell/cf template/server-only secret contract.
- [x] 20 isolated synthetic PostgreSQL checks PASS,source hashes unchanged,container/
  volumes absence verified. Six shell method cases/five paths each PASS (one Node
  file wrapper reported). Schema replay/rollback,RLS with seeded row,ACL/trigger/
  unrelated grants,logical restore and restored denial verified.
- [ ] C4 closure:billing/cards/charges/add-ons/usage,terms/application,transfers/TIA/
  retention/monitoring,MFA/SSL/API configuration and operational encrypted backup.
- [ ] Separate owner/security authorization for exact hosted ACL fix,private schema,
  runtime/secrets/private ingress and synthetic hosted restore/measurements.
- [ ] Hosted Advisor postflight,connectivity/authorization/outage/CPU/backup acceptance;
  6.0f owner disposition. C2 §12 and G1–G9/G11 integration remain independently open.

**Verification/limits:** local preparation only;no hosted mutations,cloud runtime,
Worker build/typecheck,encrypted drill rerun,Android/Gradle/APK/UI/screenshot/physical
device checks. Historical Stage6 evidence/all other worktrees preserved. Stage7
owner acceptance observed in latest Issue retained;Stage8 remains pending.
No paid resources,OAuth,public routes,real data,commit/push/merge/tag/release.
Issue31 OPEN / Goldberg Development In Progress.

Detailed packet,next steps,source-bound final20-check result and remaining gates:
`docs/planning/goldberg-0.11/stage-6/PRIVATE-CLOUD-READINESS.md`.
Resume there and latest Issue checkpoint;obtain missing read-only evidence first.
No SQL application or deployment until all required prerequisites and scoped
authorizations are recorded.

Six repository validators,independent evidence/hash audit and whitespace PASS.
Issue description/checkpoint readback verified: https://github.com/stokonn/Goldberg/issues/31#issuecomment-6388528121

---

## Stage 6 HTTP → PostgreSQL integration — VERIFIED LOCALLY (2026-10-24)

**Purpose/scope:** close the separate HTTP/PG venue gap, implement O1–O8 approved
read DTOs and verify local transactions. API adapter/projections/tests/runner and
tracking only; native services/schema, Android and Stage7 worktrees unchanged.
Branch `feature/goldberg-0.11-stage-6-api-v1-local`; baseline
`944280bee28d3280479db3b4e311e69b8c49027c`. Explicit owner instruction authorizes
this local batch; older no-further-local-implementation statements are historical.

- [x] Queue safe DTOs, null unapproved labels, exact evidence counts, bounded SQL
  filters/literal NFC search and signed caller/filter/scope-bound keyset cursors.
- [x] Canonical audit DTO/filtering, fixed highwater/head, chain/filtered coverage.
- [x] Exact PostgreSQL repeatable-read statistics, Prague-day/DST boundaries,
  concurrent publication consistency and single-count approval semantics.
- [x] Real HTTP lifecycle, capabilities, moderation, dual revisions, Comparison,
  idempotent exact-byte replay, races, account isolation, role revocation, rollback,
  24h receipt/15m cursor expiry and SQL timeout recovery for all three reads.
- [x] Final source-bound run `spikes/stage6-api-v1-local/results/http-pg/pg-n4udfonz`:
  **24 HTTP→PG +240 independent PG** tests PASS (Auth50/schema34/handlers87/
  operations34/M2-19/M3-16); **API117/T22 scripted-unit38/operations unit32** PASS;
  **planning18/security-evidence17** PASS; **39 actual HTTP read DTOs** validate.
- [x] Policy, three portable backup fixtures, ACL proposal and whitespace checks
  PASS. Source/raw SHA-256 and exact disposable rootless PG17.11 container/volume
  absence verified. `verify-http-pg.py .../pg-n4udfonz` PASS.
- [x] Preserve failed/disqualified attempts and previously untracked M2/M3 evidence;
  private checkpoint `/tmp/goldberg-stage6-private-pmp/checkpoint.md` outside Git.
- [x] Implementation/evidence `ead440a27f5564b2366b50e3fb850a4f2c713fc5`
  ordinary-pushed and exact remote SHA read back; [published checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6382021802).
  Project remains In Progress; Issue31 OPEN. Original two untracked M2/M3 evidence
  directories preserved. Documentation-only receipt follows the verified source.

**Completion:** local engineering verification complete and source published;
this documentation-only receipt changes no tested behavior.
Stage6 remains OPEN,6.12 PARTIAL,6.17 NOT FROZEN. G1/G2/G3/G5/G6/G9 now have
combined local backend evidence; G4 HTTP Comparison available/unavailable verified;
G8 exact reads/max-int8 and fail-closed unsupported writes; G11 validation/default-deny
verified locally. G1–G9/G11 **consumer integration stays OPEN**; G10 static disabled
Android boundary CLOSED. `/v1` compatibility UNCONFIRMED. Title/composer policy,
full-int8 domain writes, final query budgets, client refetch/reconfirmation and
broader hosted/browser/operational/legal acceptance remain open.
C4 **8 PASS/19 PARTIAL/6 OPEN**;6.0f unchecked; C2 §12 remains independent.

No production Supabase/Cloudflare/OAuth, real credentials/data, paid resources,
Stage7 edits, device/ADB, merge/main/tag/release or public API activation. No
Gradle/JVM/lint/APK/instrumentation/screenshots/browser/proxy/hosted tests executed;
no APK/hash/signing claim. Prior encrypted-restore/ACL SQL runs are preserved
historical evidence, not rerun by the static backup/ACL checks above.

Full endpoint/G1–G11/evidence ledger:
`docs/planning/goldberg-0.11/stage-6/API-HTTP-PG-INTEGRATION.md`.
Next: separately scope consumer compatibility/shared fixtures, client reconfirmation,
full-int8 mutations and real policy/operational evidence; keep Android transport,
public API activation, hosted changes and Stage6 acceptance gated.

created by stokonn with love <3

## Stage 6 durable backup-key resilience verification — 2026-10-24

- [x] Add the design-only zero-cost backup operations runbook and link it from
  `ZERO-COST-OPS.md`.
- [x] Recheck official Cloudflare and Supabase pricing, backup, region, DPA and
  transfer pages; preserve the 33-item C4 matrix at **8 PASS / 19 PARTIAL /
  6 OPEN** because account, billing, legal, hosted and day-of evidence remain
  incomplete.

**Purpose:** verify recoverable synthetic encrypted backups after process
termination and key rotation without activating production operations.

**Scope:** `spikes/stage6-backup-local/`, private synthetic evidence and Stage
6 documentation only; no Supabase, real data, scheduler, cloud storage,
Android or Stage 7 worktree.

**Tasks**

- [x] Use external mode-600 recovery-key custody and isolated per-process
  GnuPG state.
- [x] Verify separate-process export/restore, wrong/missing-key rejection and
  v2 rotation with recovery of the older v1 backup.
- [x] Reject partial/tampered archives; verify atomic publication and SHA-256
  manifests.
- [x] Verify schema, 2 metadata, 2 submissions, 2 audit events and audit-chain
  integrity, with cleanup and secret scans.
- [x] Remove interrupted `.part` and manifest-temporary artifacts on phase
  failure.
- [ ] Approve production destination, disaster-recovery key custody, daily
  scheduling, monitoring, retention, hosted restore and measurable RTO.

**Verification:** fresh rootless PostgreSQL 17.11 run exited **0**; export
**3,915 ms**, restore **4,341 ms**, rotation export **4,009 ms**, total
**30,033 ms**. No production RPO/RTO claim is made.

**Completion status:** synthetic resilience drill **COMPLETE**; production
backup readiness and Stage 6 remain **OPEN**. C4 remains **8 PASS / 19
PARTIAL / 6 OPEN** and 6.0f remains unchecked.

created by stokonn with love <3

## C4 read-only verification and Q1-D owner decision — current2026-10-23

Purpose: reconcile current official cloud documentation,inspect only existing
read-only account metadata,and record the owner-reported professional outcome.
Scope: Stage6/Q1/C4 planning and docs/ai only; no backend/Android/schema/cloud changes.
Branch `feature/goldberg-0.11-stage-6-api-v1-local`, recovered clean/remote-matched
HEAD `f3668fc963e1c3b55e5137d397e1881e88237a79`; M3 source/evidence preserved.

- [x] Recover C4,Q1/operator/professional brief,cloud runbook,open decisions,Stage6
  handoffs/checklists and current Issue31; preserve earlier evidence and comments.
- [x] Record **Q1-D SELECTED by owner: operation under owner's own name**.
  Owner reports qualified professional outcome **acceptable / no blocking objection**
  for operator model,contractual eligibility and relevant privacy/protection obligations.
  **D5b RESOLVED only in that reported scope**; no independent professional verification,
  document/signature/legal citation or individual C-1…C-6 answers invented.
  Review date not supplied; report recorded2026-10-23,not inferred as review date.
- [x] Re-fetch official current sources and classify every C4 item in C4 §M.
  **33 items:8 PASS /19 PARTIAL /6 OPEN**,24 official source entries accessed2026-10-23.
- [x] Documentation coverage/history/link/gate/privacy assertions,agent policy and
  whitespace PASS; current M3 source/raw evidence verifier PASS with zero test reruns.
  Evidence:`docs/planning/goldberg-0.11/stage-6/c4-2026-10-23/verification.json`.
- [x] Actual authorized Supabase metadata:1 organization Free/tier_free,1 existing
  eu-central-1/ACTIVE_HEALTHY project,PG17.11.0.003. No identifying metadata stored.
  No current Cloudflare callable tool/plugin found; prior observations remain historical.
- [x] Billing observability checked: metadata omits cards,invoices,add-ons and charges.
  Billing/payment status **NOT VERIFIED** for either provider;0CZK remains goal,not proof.
- [x] Prepare exact day-of evidence matrix without entering any creation/configuration flow.
- [ ] C4 technical/billing/backup/transfer/retention/day-of evidence and owner closure.

**C1 APPROVED; Q1-D SELECTED/D5b bounded owner-reported resolution; C4 OPEN;
ORD-A sequencing only;6.0f UNCHECKED; cloud provisioning/deployment/OAuth/permission
changes NOT AUTHORIZED.** No GDPR/legal-compliance claim or real-user collection.
Other privacy/contact/consent/retention and actual configuration checks remain open.
No cloud resource created,changed or deleted; no keys,production credentials or user
rows accessed; no hosted SQL.3 read-only Supabase metadata calls only. No Android,
Gradle/APK/lint/screenshots/emulator/physical-device/hosted runtime/restore execution.
Stage7A acceptance and X10 deferral Gould0.12 preserved; Stage7B worktree untouched.

New owner selection and direct Supabase metadata materially change the record:
one combined factual Issue31 checkpoint is justified; no duplicate issue or old
checkpoint edit. Issue31 stays OPEN / Goldberg Development In Progress.
Smallest immediate next action: authorized read-only billing/payment/add-on/charge
verification for both providers. This alone cannot close remaining C4 evidence.
Earlier blanket Q1-unresolved/no-selection statements below are historical and
superseded only for this explicitly reported scope; M3 backend status is unchanged.

[Current C4 classifications,sources and day-of requirements](stage-6/C4-PREPROVISIONING-CHECKLIST.md#m-current-c4-verification--2026-10-23-c4-open).

---

## Stage 6 continuation recovery — 2026-10-24

- [x] Recover the canonical policy, maintenance ledger, Stage 6 planning,
  `docs/ai/*`, M1/M2/M3 ledgers, Issue #31, branch/worktrees and publication state.
- [x] Confirm M1, M2 and M3 are already implemented, PostgreSQL-verified and
  ordinarily pushed; no duplicate implementation or publication performed.
- [x] Run the source-bound M3 verifier:
  `python3 spikes/stage6-api-v1-local/verify-m3.py
  spikes/stage6-api-v1-local/results/m3/pg-ygh16ba3` — PASS; recorded evidence
  includes 22 Comparison DTOs, 16 M3 + 19 M2 PostgreSQL tests, 106 adapter/wire,
  38 T22 and 18 planning checks, with zero failures/skips.
- [ ] Obtain the separately approved read-only provider billing/payment/add-on/
  charge evidence; unavailable in this session.
- [ ] Obtain separate authorization before client refetch/reconfirmation,
  HTTP/Android integration, full-int8 writes, public compatibility or any
  production/cloud action.

Status: Stage 6 local API M1–M3 remain verified but integration is not accepted.
C4 remains 8 PASS / 19 PARTIAL / 6 OPEN; 6.0f remains unchecked. This recovery
performed no cloud, OAuth, device, Stage 7B, merge, tag or release action.

created by stokonn with love <3

## Local API M3 — VERIFIED / SOURCE PUBLISHED (2026-10-23)

Purpose: expose approved authorized live Comparison for moderation conflict recovery.
Scope: isolated API adapter/projection, tests/runner/evidence and tracking docs only;
native services/schema/migrations/Android and Stage7B untouched. Branch
`feature/goldberg-0.11-stage-6-api-v1-local`; starting HEAD
`fdbfbe06fcd3faf939d4f6a90cbdeac8ee683e67`. M2 remains published.

- [x] Recover durable checkpoints/contracts/O1–O8/M1/M2, inspect branch/status/ancestry,
  exact remote SHA/releases/Issue31; preserve legitimate work/history.
- [x] Strict local GET `/v1/admin/entities/{id}/comparison?submissionId={id}`;
  live session/queue.read, denied404, exact submission/entity binding, one coherent
  SQL content snapshot, explicit approved DTO, decimal-string live revision/hash/status.
- [x] Detail retains recorded expectedEntityRevision/current submissionRevision/exact
  timestamps. Comparison never relabels baseline, substitutes proposal values,
  mutates moderation or retries decisions; later decisions still validate both revisions.
- [x] Default-deny real policies; exact allowlisted text/evidence only, no private
  provenance. Absent/unmapped/projection_unavailable; no truncation or raw rows.
- [x] Actual PostgreSQL17.11:16 M3 +19 M2 PASS;106 adapter/wire +38 T22 scripted/unit
  regressions and18 planning tests PASS.22 actual PG Comparison DTO captures match
  schema. Zero failures/skips/cancellations in final run; failed attempts retained.
- [x] Current source/raw hashes/protected baseline PASS; exact disposable container
  AND recorded volume removed/absence verified. Evidence `results/m3/pg-ygh16ba3`.
- [x] Implementation/evidence committed and ordinary-pushed; exact remote SHA
  `b01f1e924601d2571ba399888755369de2db3f08` verified. Issue31 verified-progress
  [checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6375047517).
  Completion is tracked in the existing [Issue31](https://github.com/stokonn/Goldberg/issues/31)
  M3 dashboard/comment after final tracking-commit remote readback.

No cross-request snapshot promise; Comparison intentionally has no proposal revision
or timestamp fields. Detail supplies recorded intent separately; client reconfirmation
remains deferred. Public /v1 UNCONFIRMED; full-int8 writes separate; production
policies empty/default-deny. G10 CLOSED,6.12 PARTIAL,6.17 NOT FROZEN; no formal6.13–6.18,
HTTP/Android/APK/screenshots/device/hosted/cloud/OAuth/merge/deploy/release. Stage7A
FINAL ACCEPTED; X10 NOT FIXED/NOT VERIFIED, deferred Gould0.12. Issue31 OPEN /
Goldberg Development In Progress. Next smallest milestone: separately authorized
client Detail/Comparison refetch and explicit reconfirmation integration; no policy activation.

[Full milestone ledger](stage-6/API-V1-LOCAL-M3.md).

Continuity: M3 backend scope is verified; do not repeat M2/M3 publication or
implementation. Recover latest Issue31 M3 checkpoint and actual HEAD/status before
new work. Test-source checker is `verify-m3.py results/m3/pg-ygh16ba3` in the API
package (run from root with full paths); original M2 checker is historical/source-bound.

---

## Local API M2 — VERIFIED / SOURCE PUBLISHED (2026-10-23)

Purpose: bounded authoritative Detail/refetch and actual disposable PostgreSQL
acceptance for the existing local adapter/public receipts. Scope: isolated API
package and documentation; existing services/schema/migrations/Android untouched.

- [x] Strict GET Detail, one SQL content snapshot, live queue.read/account isolation,
  explicit approved DTO, independent baseline/proposal decimal strings, UTC microseconds,
  policy default-deny and bounded history overflow503 without truncation.
- [x] Actual PostgreSQL17.11:19 tests PASS;106 adapter/wire +38 T22 service/scripted-SQL
  regressions and18 planning tests PASS;13 real PG Detail captures match approved DTO.
  Zero failures/skips/cancellations in final run; prior345 baseline not rerun.
- [x] Source-before/after/current and raw hashes verified; UUID-named loopback-only
  container removed/absence verified; failed attempts and M1/security evidence retained.
- [x] Issue31 verified milestone description/comment synchronized; OPEN / Project In Progress.
- [x] Implementation/evidence committed and ordinary-pushed at `1ba7c1a67f5b8d1b2fea9084e9595fca97b7eb52`;
  exact remote SHA matched. [Completion checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6374687079)
  description/comment readback PASS, exactly one completion marker; Issue31 OPEN.

[Full purpose/scope/tasks, exact commands, attempts and limits](stage-6/API-V1-LOCAL-M2.md).
Current baseline is recorded submission intent, not live catalogue state. Comparison,
history cursors, client reconfirmation/full-int8 writes/public compatibility stay open.
G10 CLOSED;6.12 PARTIAL;6.17 NOT FROZEN; no formal6.13–6.18 or HTTP/Android/device/cloud/
OAuth/production/merge/release. Stage7A FINAL ACCEPTED; X10 unfixed/unverified, deferred
Gould0.12; No M2 writes to Stage7B/security worktrees. Stage7B advanced externally during this run;
its new work is preserved. Security HEAD/status/diff remained unchanged.

---

## Stage 6 API local M1 — verified source published (2026-10-23)

**LOCAL MILESTONE 1 VERIFIED / SOURCE PUBLISHED; INTEGRATION NOT ACCEPTED.**
Existing isolated worktree reused, clean at recovery HEAD `c84c4b99eb27034e3630ff18cf2c470410dbbea2`;
implementation `337847770a579e1f6689c135805ad07a9b2bab19`, approved ancestor
`6dd3136269ec7f5932c8c4b3689fb065d4db9105`. Source and security evidence were
already implemented/committed; recovery audited rather than duplicated that work.
Ordinary guarded push succeeded and exact remote SHA matched c84c4b9.
[Published factual checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6372592793). Issue31 OPEN / Goldberg Development In Progress.
Historical DNS/403 publication failures below remain unchanged audit records,
superseded by this successful authorized CLI publication. No new authentication.

- [x] Read approved design, services, adapter, test venues and current handoffs;
  retain existing separate branch/worktree and preserve other worktrees.
- [x] Execute `python3 spikes/stage6-api-v1-local/verify.py`: PASS;106 adapter/wire
  plus38 T22 and18 planning-verifier results, raw hashes and source-before/after/
  current binding verified. These are inspected prior executions, NOT tests rerun
  by publication recovery; prior345 executions were not repeated.
- [x] Protected services/schema/Android/contracts/security evidence byte-unchanged
  against approved baseline; whitespace and publication-scope/clean-HEAD guards PASS.
- [x] Guarded publisher ordinary push and Issue31 description/comment readbacks PASS;
  prior comments preserved, exactly one source-SHA-marked M1 checkpoint.
- [ ] Public `/v1` compatibility remains UNCONFIRMED; local/provisional only.
- [ ] PostgreSQL receipts/locking/durability proof, full-int8 domain writes,
  latest-detail/refetch, HTTP and client/session integration remain unverified.

Executable LOCAL operations: challenge/exchange/refresh/session/capabilities/logout,
explicit moderation open and approve/reject/request_changes. Native domain/Auth/
Community services are unchanged; the new adapter uses explicit mappings, canonical
wire validation and exact public receipts inside domain transactions. No listener.
G5–G8 LOCAL foundation verified; integration gaps remain OPEN. G1–G4 absent, G10
CLOSED, real-world policy empty/default-deny,6.12 PARTIAL,6.17 NOT FROZEN; no formal
6.13–6.18, Android/network activation, production OAuth/cloud, device, merge or release.
Historical PG preflight was EPERM; recovery launched no PG/container/listener and
executed zero PG/HTTP/Android bodies. No current-host availability inference.

Next smallest milestone: bounded authoritative latest-submission Detail/refetch for
409 recovery and permitted disposable-PG adapter/receipt verification. Full-int8
writes and public-version adoption remain separate gates. Stage7A FINAL ACCEPTED;
X10 NOT FIXED/NOT VERIFIED, deferred to Gould0.12; Stage8 readability/accessibility/
touch-target checks retained. Preserve Stage7B dirty work unchanged.

---

## Retained implementation / earlier publication record

## Stage 8 — Discover repair verified; owner debug review pending (October 25, 2026)

Purpose/scope/tasks and source-bound results: [Discover checklist](stage-8/discover-repair/CHECKPOINT.md), [final report](stage-8/discover-repair/REPORT.md). Exact RC baselinea73a7e2; isolatedbranchfix/goldberg-0.11-discover-ux; verifiedsource04bf202.308JVM,196native passes/11existing skips; exact-SHA API36/quality/seededupgrade PASS.41original PNGs inspected. Debug APK only; owner review and fullStage8/canonical/hosted/release gates remain OPEN. No new stage,RC/tag/Release,main merge or production activation.

## Isolated canonical integration — October 25, 2026

Branch integrate/goldberg-0.11-canonical-client,base42f6897;source publication and
nativeCI pending. Canonical application service/repository/Android transport seam
implemented with production networking disabled.302 JVM/0failures/skips,APK/testAPK
compile andlint0errors132warnings1hint PASS;actual Kotlin/HTTP/BFF/PG1PASS at final reviewed source. Local emulator crashes retained;no native PASS inferred.
Accepted legacy UI/Keystore adoption remains incomplete. FullStage8/Stage6 acceptance,
public version/OAuth/policy/hosted runtime remain OPEN. No personal schema/migration,
UI,practice orjournal change;no physicaldevice/main/release/public activation.
Detailed purpose/scope/living tasks:[canonical adoption](https://github.com/stokonn/Goldberg/blob/integrate/goldberg-0.11-canonical-client/docs/planning/goldberg-0.11/stage-8/CANONICAL-ADOPTION.md).

---

## Stage 8 — independent Android verification executed (October 25, 2026)

Independent checks are VERIFIED; source publication/automatic CI receipt is in progress. This continues existing Stage8,without a new named stage. Full Stage8 integration and Goldberg0.11 release acceptance remain INCOMPLETE.

Purpose/scope: trusted0.10.1 upgrade/data-preservation rehearsal, actual process/migration/restore/practice/reboot boundaries, missing orientation/platform observations and guarded artifact/readiness tooling. Baseline22fb50bedec7b932e47640673e52fdb57bdc791a on feature/goldberg-0.11-stage-8. Production source remains4d245b and APK68d48b…;only test/tool/docs/evidence changes. Accepted Stage7/Stage8C behavior retained.

- [x] Recover policy/PMP/AI/readiness/Issue/Project and remote/worktree state; preserve externally owned Stage6.
- [x] Trusted baseline854bbfeb…/certificate53eface… → unchanged development candidate68d48b… in-place install;schema8→10,all12 original tables/37sessions/37attribution rows/2pieces/4badges/profile/avatar/showcase/draft/archive/preferences retained;integrity/FK PASS.
- [x] Abandoned partial installer leaves baseline unchanged;normal install retry PASS. Actual migration process kill rolls back,then retries with every original typed cell intact. Old binary's10→8 downgrade is refused without loss;forward reinstall recovers.
- [x] V5 export/restore from upgraded journal,checksum-failure preservation/retry andfour actual restore kill phases/replay/retry PASS. Original destination recovery bytes remain exportable in evidence.
- [x] Actual monotonic practice:6.008s credited during6.221s observed,per-piece/interval sums exact;actual process death/reboot preserve stopped recovery,raw draft andno downtime credit;completion idempotent.
- [x] Actual reboot preserves WorkManager IDs/offline queue/READYblob;real cancellation PASS after54.159s (not3hdelivery). DataSaver/restricted bucket45/forcedLOW readbacks observed/restored;not actual ENOSPC/mobile transfer. Retain unchanged accepted SmartCache/interrupt/metered/notification and32-config UI evidence.
- [x] Actual EN/Dark portrait/landscape plusfour CZ/Dark font1.3/2 rotations;allfive tabs,eight keyboard-focus steps andIMEBack PASS. Eight selected original frames inspected via two review sheets.
- [x] Package/manifest/assets/ZIP/trusted signer/test-fixture exclusion/source traceability PASS. Rehearsal artifact isdebuggable0.10.1/code11;strict release guard correctlyBLOCKS it. No official version/signing/RC selection.
- [ ] Commit/push verified scope,read exact remote and relevant automatic CI;final append-only Issue/private PMP/AI/matrix receipt. No unchanged full native CI redispatch.

Counts:new12 targeted JUnit PASS,0new skips,6deliberate actual process terminations (notJUnit passes),1corrected invalid-UUID fixture failure retained,0unresolved app failures. New final AndroidTest compile PASS21s;no production/JVM logic changes. Retained CI40170295032:190native/188pass/2oldexternal skips and275JVM at its original source;not new executions here.

Stage6 refreshed read-only:receipt da2423bcea3b9ef8d0ea88a9b08a2e5468f173d6,verified engineering source be5104476cf6cb059b0f206b2c4f18027d6bc657/[CI40188291121](https://github.com/stokonn/Goldberg/actions/runs/40188291121). Its234JVM/20canonical/localarchive/TLS evidence is externally reported,not executed here;native/accepted-consumer/hosted acceptance remainsOPEN. Actual-client G1–G9/G11,C4,6.0f/6.17,real policy/publicversion/OAuth andB2/B4/B5/B6 remain blocked or pending;G10 disabled. No Stage6 source/DTO/auth/session/repository/fixture adoption or external writes.

Remaining gaps:final integrated/official-version/signed-release artifact acceptance;real three-hour/reboot notification delivery;audible speech/physical-device acceptance (not performed);real disk-full/production transfers (not performed). Existing accepted deterministic/transport evidence stays source-bound. Next shortest path:review final Stage6 consumer handoff/policy gates,obtain authorized integration scope,reconcile affected paths preserving current UI/data,then rerun only impacted combined-source/native/data checks plusfinal trusted-signer upgrade. No additional named stage,main merge,cloud activation,production networking,phone testing,tag orRelease.

created by stokonn with love <3

[Detailed commands,evidence,reproduction andlimits](stage-8/INDEPENDENT-ANDROID-VERIFICATION.md).

---

## Stage 8C — targeted UI corrections COMPLETE (October 25, 2026)

**The bounded UI correction milestone is complete. A8-1, A8-2 and A8-3 are FIXED within the verified scope. Full Stage 8 integration/release remains PENDING/BLOCKED.**

Branch `feature/goldberg-0.11-stage-8`; baseline `29ce6071741df7567f782730bd78d6d1e0192780`. Production correction source `4d245b46936d640740799dfcc6e8d61f3d44b249`; corrected test source `a41cd11a1b32af2949c43cb6e5b6aac94a3c2a51`. Later documentation/evidence receipts retain these exact runtime source bindings.

Purpose: remove duplicate Admin Settings focus stops, retain readable full navigation labels with Android font scaling and make test-scaffold system bars reflect MainActivity. Scope: three production UI files, three instrumentation files, host evidence scripts and Stage 8 records. Application ID/namespace `com.opus.piano`, database `opus.db`, schema, backup/practice logic, contracts, dependencies, production theme behavior and version metadata are unchanged.

- [x] Recover policy/readiness/AI/Issue31/Project and source heads; preserve all concurrent work and historical evidence.
- [x] A8-1: one named, stateful Switch row; minimum48dp, disabled suppression, keyboard/touch/native accessibility actions and exactly-once callbacks verified.
- [x] A8-2: all five destinations retain full names, original font metrics, icons/colors, selection and navigation; natural bar height/wrapping/hyphenation replaces ellipsis.
- [x] A8-3: test-only system-bar effect mirrors MainActivity; production theme handling unchanged; four opposite-system scaffold/NavHost pairs agree.
- [x] Verify32 EN/CZ × Light/Dark × fonts1.0/1.3/1.5/2.0 ×360×640/412×915dp configurations, four real TalkBack traversals/40 actual focused steps, five actual routes/two Back transitions and worst-case2x scroll reachability.
- [x] Gradle/JVM/build/lint/host checks and independent source-bound native acceptance; preserve unsuccessful attempts and the failed first CI run.
- [x] Update local reports/checklists/matrix/AI and publish verified source/evidence; synchronize existing Issue31/Project and private PMP checkpoints. Private PMP/APKs/secrets are excluded from Git.

Verification:275 JVM cases/36 suites,0 failures/errors/skips; independent native190 cases/**188 passed/0 failures/0 errors/2 existing external-source skips**, including all3 Stage8C and9 Stage7B cases. CI lint0 errors/132 warnings/1 hint; local lint0 errors/119 warnings. [Native CI2](https://github.com/stokonn/Goldberg/actions/runs/40170295032) succeeded at `a41cd11a1b32af2949c43cb6e5b6aac94a3c2a51`; [quality](https://github.com/stokonn/Goldberg/actions/runs/40168589919) succeeded at identical production source `4d245b46936d640740799dfcc6e8d61f3d44b249`. Unchanged successful automatic quality was not repeated after the test-only correction.

Limits: actual TalkBack focus/events are verified; audible speech quality and supplemental double-tap activation remain unverified (unsuccessful injections retained). Native ACTION_CLICK/Space/pointer/disabled and exact callback counts independently passed. Two terminal Czech baseline focus snapshots without a focused node are excluded from focus proof. Combined backend/source, trusted-release seeded upgrade, full lifecycle/three-hour/reboot/live operational acceptance and final release/version selection remain open. Stage6 G1–G9/G11, C4,6.0f and live integration remain OPEN; concurrent Cloud56ff724/hosted NO-GO was not adopted. No main merge, cloud activation, physical-device test, deployment or Release.

Current verified APK: ignored local `app/build/outputs/apk/debug/app-debug.apk` (preserved copy `.gradle/stage8c-artifacts/candidate-app-wrap.apk`), development0.10.1/code11,23,067,944 bytes. SHA256 `68d48b75436f6bd3f865d784714ec48570e2745c14aa0e9682514753009f3db0`; trusted certificate SHA256 `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`. Installed-byte hash/ZIP/package/signature checks passed; CI uses its temporary debug signer and is not a release artifact.

created by stokonn with love <3

---

## Stage 8B — first technical assessment verified, October 25, 2026

**TECHNICAL ASSESSMENT COMPLETE / SOURCE PUBLISHED. Full Stage8 integration/release PENDING/BLOCKED.** Branch `feature/goldberg-0.11-stage-8`, baseline2f4d1b8. Stage7 client/synthetic acceptance remains COMPLETE.

Automatic new-source quality [40147220442](https://github.com/stokonn/Goldberg/actions/runs/40147220442) **SUCCESS at394b6b58b2adcb0eb07b4f873161bbd7cc70ed3c**:clean Gradle build/JVM/androidTest compilation/lint PASS7m49s,83executed/1up-to-date;all six workflow validators PASS. Individual new JVM counts not exported;0 native suite executions. This supersedes earlier local-only no-full-host statements below;CI25 was not manually rerun. Documentation-only receipt skips redundant CI;source-bound success is not transferred to its later SHA.


Purpose/scope: reconcile accepted7A documentation, audit mainCI/13 Stage 6 overlaps, assess A8-1/A8-2/A8-3 offline, verify retained APK/source/upgrade prerequisites and map critical-data coverage. Production behavior/database/schema/contracts/CI unchanged; new opt-in instrumentation recorder/host evidence tools only.

- [x] Refresh16 worktrees/remotes/Issue31/milestone4/releases/Wiki/Goldberg Development; preserve all 2203 external original untracked files/APKs/PMP. Concurrent Cloud source 2c38177 / receipt c8b903b untouched, not adopted; PRIVATE STAGING NO-GO retained.
- [x] Reconcile9 Stage 7A documentation deltas:four isolated acceptance/follow-up files adopted, shared current dashboards preserve newer Stage7B/8 decisions. X10 NOT FIXED/NOT VERIFIED,deferred Gould0.12; material accessibility retained.
- [x] Audit main-onlyCI and classify all 13overlaps; stronger accepted Stage7B workflows retained; no source combination.
- [x] New test-recorder wrapper assembly PASS54 s; default runner manifest PASS13 s. App APK byte-identical93ae9ec…; trusted package/signature/ZIP/source/version/baseline review PASS,still0.10.1/code11.75 historical critical CI25 cases inventoried,not rerun.
- [x] Offline API36 emulator5588/qemu1:28 unique settings configurations (16small+4standard+4opposite-theme NavHost+4scaffold),29 captures including retry;161 PNG/tree pairs. Four real TalkBack traversals / 40 swipes with events. Emulator stopped; AVD/APKs retained.
- [x] A8-1 actual duplicate Settings stops and A8-2 actual large-font label ellipsis reproduced, bothMedium/P2,NOT FIXED. A8-3 scaffold statuscontrast~1.05–1.13:1 reproduced; matching real NavHost5.56–19.48:1,gesture5.74–15.25:1; no product contrast defect demonstrated in tested scope. Proposed fixes await separate implementation approval.
- [x] Evidence verifier,Python compilation,production-preservation and whitespace checks PASS. No new JUnit/full CI/lint/data-suite execution claimed; no audible speech/motion/full3h-reboot/live integration acceptance.
- [x] Source `394b6b58b2adcb0eb07b4f873161bbd7cc70ed3c` ordinary-pushed and remote SHA exact; [authoritative Issue checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6391483885), Issue OPEN / Project In Progress synchronized/read back. Final documentation receipt follows without changing runtime evidence.

[Full commands/results/limitations and next milestone](stage-8/8B-TECHNICAL-VERIFICATION.md); [all 13risk resolutions](stage-8/INTEGRATION-SEQUENCE.md). No production correction, networking activation, Android/backend combination, main merge, deploy or release.

Next: retain published bounded evidence; seek separate Stage8C scope for targeted row semantics/responsive labels/scaffold-window correction and affected before/after regressions. Stage6 G1–G9/G11,C4,6.0f/public-version/real-policy/live/production dependencies remain OPEN. Trusted-signer seeded0.11upgrade/version selection and broader release gates remain pending.

created by stokonn with love <3

---

## Stage8A preparation completion and publication receipt

**8A readiness preparation COMPLETE / packet PREPARED AND PUBLISHED. Stage8 integration/release acceptance PENDING/BLOCKED.** Published preparation commit `ea1758c6fb10c9f4cdecf83d4c47523e9a5b553a` on `feature/goldberg-0.11-stage-8`, ordinary push and exact remote readback PASS. [Existing Issue31 source checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6388549455) and Project overview/item readbacks PASS; Issue OPEN / umbrella In Progress. Documentation-only preservation checks passed; no runtime tests/builds/CI/merges/deploy/releases executed. Stage7 client/synthetic acceptance remains COMPLETE. Concurrent Stage6-Cloud WIP was recorded untouched, not adopted; all Stage6/live/production gates remain OPEN. Final handoff commit SHA is recorded in the next Issue31 receipt.

# Stage 8A readiness preparation — October 24, 2026

**Stage7 Android client/synthetic acceptance COMPLETE and owner-approved. Stage8A preparation packet PREPARED; Stage8 integration/release acceptance remains PENDING/BLOCKED.** Current authorized scope: documentation, read-only inventories and separate worktree creation only. No source integration or live adoption is authorized.

Worktree `/home/stokonn/Projects/Goldberg-Stage8`, branch `feature/goldberg-0.11-stage-8`, preparation base `4c6a91d600ad18455a280f941f89740694d5afa0`. All15 prior worktrees/53 cached refs and fresh remote heads/tags/Issue31 comments were inspected first. Stage7A's sole unique acceptance commit is documentation-only; Stage6 diverges54/78 with13 potential path overlaps; main diverges81/11. [Readiness packet](stage-8/README.md), integration sequence, release checklist, accessibility assessments, Stage6 ledger and20-row verification matrix are prepared. Existing Stage7 evidence/PMP remains in its owning worktree; no copying, deletion or reset.

Stage6 G1–G9/G11,C4,6.0f, Android/backend live integration and all production authorization gates remain OPEN. G10 is closed for disabled transport/actions only. Duplicate Settings TalkBack stops, large-font bottom-tab truncation and scaffold contrast remain NOT FIXED, awaiting explicit Stage8 release assessment. Speech/motion/exhaustive accessibility/real three-hour-reboot delivery remain UNVERIFIED. X10 remains under its existing Gould0.12 deferral.

No tests/builds/emulators/live calls/merges/deploys/releases executed in8A. Earlier source-bound CI25/275JVM/262pairs/seeded-upgrade evidence remains accepted within Stage7 scope; no new candidate exists. Next: finish documentation/preservation checks, commit/push the packet, synchronize Issue31/Project and record exact publication SHA. Later work must refresh input HEADs and obtain separate integration/production authorization; blocked Stage6 gates cannot be silently omitted.

created by stokonn with love <3

## Stage8A — readiness preparation checklist

Purpose/scope: safe input/base inventory, a separate worktree and future integration/release verification planning; no product behavior changes.

- [x] Inspect existing branches/worktrees/unique changes and choose accepted Stage7B base before worktree creation.
- [x] Create separate Stage8 preparation worktree; retain Stage7 acceptance/evidence and private PMP.
- [x] Prepare integration sequence, release-readiness checklist, deferred accessibility assessments, Stage6 dependencies and verification matrix.
- [x] Complete documentation/preservation checks, commit/push and Issue31/Project publication readback.
- [ ] Stage8 integration/release gates remain PENDING/BLOCKED; all applicable Stage6 gates OPEN, no source combination/activation/publication authorized.

Completion status:8A readiness preparation COMPLETE; packet published and Issue31/Project synchronized with readback. All application tests and live/release matrix rows remain unexecuted for8A. Full Stage8 is not complete.

<details>
<summary>Prior stage checklist and accepted Stage7 history — preserved</summary>

# Stage 7 owner acceptance — October 24, 2026

**Stage 7B: ACCEPTED WITH DOCUMENTED LIMITATIONS. Stage 7 Android client/synthetic acceptance scope: COMPLETED.** Stage 7A was already owner-accepted. The owner explicitly approved the October 24 final owner report in this conversation; [permanent owner-decision checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6388393525). This decision supersedes the earlier READY FOR OWNER REVIEW / pending-approval status. It covers the verified Android client/synthetic scope only.

Purpose/scope: close owner acceptance of the existing Android Admin dashboard, Queue/re-review/decision safeguards, Audit, Settings, local sync/notifications and documented synthetic data safeguards. This update changes documentation only; application/test/CI source, the APK and historical evidence remain unchanged.

- [x] Record explicit owner acceptance of Stage 7B with the October 24 documented limitations; Stage 7A + Stage 7B Android client/synthetic acceptance scope completed.
- [x] Retain prior results without rerunning unchanged tests: CI #25 `40097540179` SUCCESS at tested source `fb1d3f971ed79de09dccbdc771e1c45a09dc0a6c`, 187 native cases /185 passed /2 existing skips /0 failures or errors, all9 Stage7B cases;262 screenshot/tree pairs reviewed; seeded12 collections/37 sessions/schema8→10 preserved. Prior275 JVM tests and seven validation scripts passed; normal restored assembly/lint passed (0 errors/119 warnings/1 hint). Representative October24 EN/Light/1.0 and CZ/Dark/1.3 emulator TalkBack/touch/Back/keyboard/IME/reduced-motion/permission-revocation observations retained. No fresh execution is claimed.
- [x] Retain exact manual tooling archive, default AndroidJUnitRunner, production APK and source manifest. Pre-decision published documentation HEAD `9f6adf806bff034e884d12bbb4259f920975248d`, branch `feature/goldberg-0.11-stage-7b`; the final publication checkpoint records this decision's documentation SHA.
- [x] Owner-approved deferral to Stage 8 for explicit release-readiness assessment: duplicate Settings TalkBack focus stops, large-font bottom-tab truncation and scaffold contrast findings. **Deferred, NOT FIXED**; acceptance does not waive Stage 8 assessment.
- [ ] **UNVERIFIED:** audible speech/pronunciation, subjective motion comfort/frame timing, exhaustive accessibility/font/keyboard/configuration coverage and real three-hour/reboot notification delivery. These checks remain unexecuted or bounded as originally recorded, not passed by owner approval.
- [ ] **OPEN:** Stage 6 G1–G9/G11, C4, 6.0f, Android/backend live integration and every production authorization gate. Live integration remains blocked pending separate authorization. **Stage 8 remains PENDING.**
- [ ] Separate integration/release verification and explicit authorization for any main merge, production activation or release remain outstanding. Issue #31 stays OPEN; Goldberg Development's umbrella item stays In Progress.

Next executor: retain the accepted Stage 7 client/synthetic disposition; assess the three deferred findings only within separately authorized Stage 8 release-readiness work. Do not mark them fixed, infer unverified checks passed, reopen completed Stage 7 tests or treat acceptance as live/backend/release authorization. Original raw observations/hashes, screenshots, logs, test results and private PMP are preserved. Earlier packets below are historical records, not the current acceptance status.

created by stokonn with love <3

<details>
<summary>Historical Stage 7B verification and pre-approval checkpoints</summary>

## Final administrative closure — October 24, 2026

**READY FOR OWNER REVIEW — NOT ACCEPTED. Recommendation: ACCEPT WITH DOCUMENTED LIMITATIONS**, subject to the owner's explicit disposition of the limitations below. This recommendation covers the completed Stage 7B Android client and synthetic/offline verification scope; it does not authorize live integration or release.

- [x] Admin dashboard, Queue/re-review/decision safeguards, Audit, Settings and local sync/notification behavior verified within the documented synthetic scope.
- [x] CI #25 retained unchanged: 187 native tests, 185 passed, 2 existing skips, no failures/errors; all 9 Stage 7B tests passed. All 262 screenshot/tree pairs reviewed; seeded upgrade retained 12 collections/37 sessions, schema 8→10. Prior clean host gate: 275 JVM tests passed and seven validation scripts passed.
- [x] October 24 emulator observations retained: real EN/CZ TalkBack traversal and accessible Back, touch exploration, Tab order, IME/insets/system Back, zero-animation navigation and cross-process POST_NOTIFICATIONS revocation/restart/worker suppression. EN/Light/1.0 and CZ/Dark/1.3 are representative samples, not an exhaustive configuration matrix.
- [x] Unique temporary manual recorder and exact Gradle patch archived under `results/manual-acceptance-2026-10-24/sources/`, with byte/hash receipts, before removal from the active test tree. The 270-file source manifest matches; active app, tests, Gradle and CI match the verified baseline. Default AndroidJUnitRunner restored. No production behavior changed.
- [x] Restored normal build: `./gradlew assembleDebug assembleDebugAndroidTest lintDebug --offline --no-daemon --max-workers=1` PASS in 2m08s (13 executed/64 up-to-date); lint 0 errors/119 warnings/1 hint. This compiled instrumentation; it did not execute a fresh complete native suite. CI #25 was not repeated.
- [x] Production APK ZIP integrity, unchanged SHA-256 `93ae9ec88472e4f07dc5328980786e9ce252887ccbac507c0034195cd9b51961`, package/version and trusted signing verified. Evidence-consistency verifier PASS; original screenshots/logs/JSON/hash inventory and historical failures preserved.
- [ ] Owner explicitly approves final disposition. Stage remains READY FOR OWNER REVIEW; Issue #31 stays OPEN and the umbrella Project stays In Progress.
- [ ] Proposed Stage 8 deferral, requiring owner agreement: duplicate TalkBack label/control focus stops in three Admin Settings rows; existing large-font bottom-tab truncation and historical scaffold status contrast limitation. Controls remain named, reachable and checkable; no semantics fix is claimed.
- [ ] Unverified quality limits retained: audible speech/pronunciation, subjective motion comfort/frame timing, exhaustive accessibility/font/keyboard matrix and real three-hour/reboot delivery. These checks are not marked passed.
- [ ] Stage 6 authorization/approved live backend contracts and credentials: G1–G9/G11 remain explicitly BLOCKED. Stage 8 broader integration/release gates remain separate; no backend activation, merge or release is authorized by this packet.

Evidence (relative to the Stage 7B directory): `results/manual-acceptance-2026-10-24/verified-results.json`, `closure-verification.json`, `raw-evidence-inventory.json` and `sources/archive-receipt.json`. The final publication receipt in existing Issue #31 records the exact documentation commit; CI evidence remains bound to `fb1d3f971ed79de09dccbdc771e1c45a09dc0a6c`. Private PMP and unrelated historical files remain local and uncommitted. Earlier checkpoints below are chronological history and do not reopen completed verification.

created by stokonn with love <3

## Stage7B — manual acceptance preparation executed; owner decision OPEN

Purpose: complete feasible emulator-only accessibility/navigation/motion/permission
checks without repeating CI25. Scope: new isolated AVD, opt-in test recorder and
build flag, retained evidence/checklists/Issue31/Project/Wiki. Production APK,
Kotlin/resources, schema, migrations, backups and practice behavior unchanged.

- [x] Preserve private/history; retain CI25 source-bound results without rerun.
- [x] Boot changed Lavapipe/ANGLE+llvmpipe API36 venue; explicit emulator-5586/qemu1.
- [x] Real TalkBack EN/Light/1.0 and CZ/Dark/1.3 Audit order, kernel touch exploration
  and accessible Back; Settings control order sampled. Record duplicate focus stops.
- [x] Audit Tab8, Queue/endpoint IME558px, layered system Back and real NavHost.
- [x] All3 animation scales0; route/Back behavior; original1 scales restored.
- [x] Host-process notification revoke: PID6176→absent→restart7748/DENIED;
  new worker7815 executes6 fixture calls/signature change/zero notifications.
- [x] Final recorder compile/lint PASS1m05s; default runner compile/lint PASS13s,
  lint0errors/119warnings/1hint; default runner restored, isolated AVD stopped.
- [x] Retain107 PNG observations, native events/state/commands/setup history and
  read-only evidence verifier PASS. No new JVM/full-suite/CI execution claimed.
- [ ] Owner disposition on the3 repeated Settings label/control TalkBack focus
  stops, audible-speech/subjective-motion and non-exhaustive matrix limits.
- [ ] Explicit Stage7B approval; live G1–G9/G11 stays BLOCKED pending Stage6.
- [ ] Real3h/reboot-delivery evidence remains unexecuted.

Verification/completion: representative checks executed; preparation complete,
Stage7B NOT accepted. Decision: approve verified client scope with explicitly
accepted limitations, or hold for focused accessibility/further review. Neither
selected. See [owner report](stage-7/stage-7b/FINAL-OWNER-REPORT.md) and [manual record](stage-7/stage-7b/MANUAL-ACCEPTANCE.md) for exact commands,
source/APK identities, raw results, screenshots inspected and limitations.
No phone, other-worktree edits, production activation, merge or release.

---

> **Current verified checkpoint — 2026-10-24:** correction `fb1d3f971ed79de09dccbdc771e1c45a09dc0a6c`; exact-SHA acceptance40097540179 and automatic quality40097530580 SUCCESS. Independent raw187 tests/0 failures/errors/2 existing skips, all9Stage7B cases PASS; **262 required screenshot/tree pairs** and all262 PNGs inspected in33 contact sheets,32dashboards/16actual row frames/516named-focus-checked occurrences. Seeded12collections/37sessions/schema8→10/integrity PASS; temporary CI signing limits disclosed. Clean host275JVM/0fail,lint0errors119warnings1hint/seven scripts PASS. **Stage7B remains IN PROGRESS; explicit owner acceptance and manual/live gates remain open.** [Authoritative owner report and exact limitations](stage-7/stage-7b/FINAL-OWNER-REPORT.md). Earlier entries below are historical; their pending states/counts do not describe the latest source.

## Stage 7B — final recovery verified; owner acceptance OPEN

**Purpose:** finish interrupted verification without bypassing the legitimate
changed-revision acknowledgement. **Scope:** Stage7bAcceptanceTest, CI evidence,
checklists/AI handoffs and existing Issue31/Project/Wiki. No production/database/
migration/backup/practice changes.

- [x] Preserve initialdc60e83 work/private PMP/historical artifacts; independently
  inventory CI24 using existing ZIPs (732 member hashes, no repeat downloads).
- [x] Review the existing test-only acknowledgement → completion → verification
  flow; retain all earlier assertions and production safeguards.
- [x] Focused gate13s and mandatory clean host3m58s PASS:275JVM/36suites/0 failures,
  errors or skips;lint0errors119warnings1hint;seven validation scripts PASS.
- [x] Ordinary-push correctionfb1d3f971ed79de09dccbdc771e1c45a09dc0a6c, remote exact;
  dispatch full acceptance40097540179 exactly once;both jobs and quality40097530580 SUCCESS.
- [x] Independently verify187native XML cases/185passed/0 failures/errors/2existing
  external-fixture skips;all9Stage7B cases PASS. No locally executed instrumentation.
- [x] Inventory262PNG/tree pairs and visually inspect all262 through33sheets plus
  selected originals;32dashboard configs/16actual row frames/516named-focus-checked
  occurrences. Native focus/toggle/disabled guard and actual row-open/Back PASS.
- [x] Seeded immutable0.10.1→candidate upgrade:12personal collections equal,
  37sessions/preferences preserved,schema8→10/integrity/foreign keys PASS;
  temporary CI signing versus trusted local/release certificate disclosed.
- [x] Prepare owner report and synchronize existing Issue description+checkpoint,
  Project In Progress and English Wiki/local sources;preserve prior history.
- [ ] Explicit owner acceptance;manual TalkBack/complete navigation/system Back/
  keyboard/reduced motion/platform-permission revocation/real3h-reboot delivery.
- [ ] Approved live G1–G9/G11 integration;production network/decisions stay disabled.

**Verification/completion:** synthetic recovery PASS;Stage7B remains IN PROGRESS.
Timed snapshot, existing navigation/contrast/extreme-font limitations and exact
commands/artifact/source hashes are in [the owner report](stage-7/stage-7b/FINAL-OWNER-REPORT.md).
[Final independent Issue checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6384951524). No phone, other-worktree edits,
production activation, main merge or release. The entries below are historical.

---

## Stage 7B recovery and final acceptance — October 24, 2026

**Purpose:** resolve inaccessible Admin checkable owners and dashboard Refresh, then independently verify actual UI/data preservation. **Status: IN PROGRESS, final owner acceptance not ready.** Green CI alone is not visual acceptance.

**Scope:** native AdminDashboard/Queue/Audit/Proposal/Settings, shared FilterChip/checkable focus semantics and AdminListViewport; Stage7bAcceptanceTest; evidence and English checkpoints. Existing ViewModel/repositories/navigation callbacks, database opus.db/schema10, practice timing/data and backups are unchanged. No Stage6/7A worktree changes.

- [x] Recover actual initial HEAD1bb5aac, branch/status/remote/tags/releases/milestone/Issue31/Project/checkpoints; preserve private PMP and all old untracked evidence.
- [x] Audit exact Material3 1.3.2/Compose1.9 synthetic-child semantics. Explicit named owners preserve role, selection/toggle, click, disabled guard and real FocusRequester-backed input focus; native EN/CZ assertions pass on5591e76.
- [x] Fix async dashboard scroll synchronization and failure captures; require complete48dp actions, no-scroll≤1.3, actual locale/font. Natural labels remove ordinary1.3 ellipses and extreme-font clipping. All32dashboard+16footer5591e76 renders inspected.
- [x] Independently inspect5591e76 CI40080892105:187 instrumentation/0 failures/0 errors/2 existing external-fixture skips; all9Stage7B cases PASS.242state pairs/243trees/419named-focus-checked occurrences and artifact digests PASS. Seeded schema8→10/all12 personal collections equal/37sessions/preferences/integrity/signing limits verified. These are bounded historical results, not proof of later source.
- [x] Reject final visual acceptance after detailed32destination review found small2.0 list collapse (Queue60.5dp EN/12.5dp CZ; Audit0) and stale previous-route hierarchies.
- [x] Implement finite accessibility list viewport only above1.3; preserve normal layout/anchors. Clear UiAutomation cache and require actual destination/authorized controls; add Queue/Audit row reachability and actual row-open/Back at8configs,16extra captures. Focused36s and clean host4m03s PASS275 JVM/0failures/errors/skips,lint0 errors/119warnings/1hint; seven scripts PASS.
- [x] Publish viewport follow-updc60e8379114b44f758e0bb5184a0b6b83d7a465, remote exact; dispatch40083355927 once.
- [ ] Verify this viewport follow-up on its exactSHA: full API36 native suite,258state pairs/fresh matching hierarchies,32dashboard configurations, actual list rows and seeded preservation.
- [ ] Manual emulator TalkBack/full navigation/platform notification-permission revocation, approved G1–G9/G11 integration and explicit owner acceptance remain open. Production networking/decisions stay disabled; no merge/release.

**Verification environment:** local Java25/SDK36 host builds; Ubuntu24.04/KVM/API36/emulator-5554/qemu guard in existing dispatch-only CI. Local emulator known SIGSEGV was not retried; no phone used. Two existing Stage4 external-provider/portrait cases lack separately acquired fixtures and remain honestly skipped. No new skips/exclusions/weakened assertions.

**Completion:** Stage7B remains IN PROGRESS; current viewport follow-up runtime/visual acceptance pending. [Detailed living tasks, commands, hashes, screenshots, failures and next steps](stage-7/stage-7b/RECOVERY-2026-10-24.md).

---

## Stage 7B recovery — 2026-10-24, IN PROGRESS

Current purpose, scope, implementation checklist, verification and pending gates: [recovery checklist](stage-7/stage-7b/RECOVERY-2026-10-24.md). Initial HEAD `1bb5aac`; acceptance is not complete. Earlier checkpoints below are historical.

## Stage 7B coordinated acceptance continuation — 2026-10-23

**Current: IN PROGRESS; owner acceptance pending.** Scope: approved remaining
Admin visual/no-scroll/accessibility, sync/notification runtime, seeded upgrade and
contract/session verification. Initial HEAD/remote3dc8488; prior CI40023191770 at
3a4aac2 retained. Concrete matrix and running tasks: [OWNER-REVIEW.md](stage-7/stage-7b/OWNER-REVIEW.md).

- [x] Recover exact state/pinned contracts/criteria; private PMP and Stage6 preserved.
- [x] Changed read-only local emulator5584 attempt: host SIGSEGV139 before ADB,
  zero app executions/screenshots. Use existing KVM CI; no unchanged retry.
- [x] Test-only production-screen render harness and controlled WorkManager/permission
  checks compile. Host assembly/JVM/lint gate PASS, 275 JVM/0 failures/errors/skips; six host scripts PASS. Default app networking/decisions remain disabled.
- [x] Source63f7e21 normal-pushed/read back; run40027522387 executing full suite.
- [x] Upgrade job failed before boot: corrupt API36 system-image ZIP; zero upgrade
  checks. Trusted baseline/release signer checks PASS; raw evidence retained.
- [x] Bounded image-install preflight synthetic command harness PASS; additional
  worker-delivery fixture source compiled/host gate PASS2m25s. Runtime pending.
- [ ] Execute source-bound CI and immutable-v0.10.1 same-runner upgrade; inspect
  actual Admin screenshots, document limitations and synchronize publication.
- [ ] G1–G9/G11 approved live integration BLOCKED; explicit owner acceptance pending.

This section supersedes older current-status text. Stage7A FINAL ACCEPTED; X10
NOT FIXED/NOT VERIFIED, deferred Gould0.12. No main merge/release/production activation.

---

## Stage 7B Android Admin — preserved implementation and verified CI (2026-10-23)

**CI recovery COMPLETE; Stage 7B acceptance remains IN PROGRESS.**
Purpose: preserve the interrupted Android Admin work and recover reproducible
verification. Scope: existing queue/review/conflict/audit/settings/sync, contract
fixtures, native tests, dispatch-only CI and durable evidence. Personal Room,
backup and practice systems have no recovery-session changes. Production Admin
networking and decisions remain disabled.

- [x] Preserve original cee9330 work in checkpoint0703cd8, with private PMP excluded.
- [x] Publish corrected source `3a4aac27e2eb8ae70716538d86acd374cdd38077`;
  ordinary push and exact remote SHA readback PASS.
- [x] [Run40023191770 SUCCESS](https://github.com/stokonn/Goldberg/actions/runs/40023191770),
  exact source binding/Ubuntu24.04/KVM/API36/emulator-5554/qemu1 verified.
- [x] Fresh full host gate SUCCESS7m41s:275 JVM tests,0 failures/errors/skips;
  lint0 errors/132 warnings/1 hint; six host scripts PASS,1050 EN/CZ resources.
- [x] Full connected task SUCCESS3m38s: XML182 tests,0 failures/errors,2 existing
  external-fixture skips; all4 Stage7bAdminRuntimeTest bodies PASS. No filters,
  added skips or weakened assertions. Console184/XML182 discrepancy retained;
  XML182 testcase elements are authoritative. Skip messages are empty in XML;
  unchanged source guards require external portrait/provider files not supplied.
- [x] Teardown removal verified in actual logcat. Same-built-APK install-r Success;
  MainActivity cold launch Status:ok. Both smoke screenshots inspected: Home with
  onboarding. These are not Admin/no-scroll/theme/font/accessibility acceptance.
- [x] Artifact12532680601 ZIP integrity and remote SHA256 digest verified:
  5c23ceb0ede9690a82934eae9fa87f75930839aa5859d85bc535bb7ac58ac525.
  Raw logs/XML/skip details/screenshots/file hashes retained in results/ci-run-40023191770.
- [x] Raw evidence retained byte-for-byte; narrow CRLF EOL allowance leaves normal
  source whitespace checks active. Old-source run40015851111 is historical only.
- [x] Issue31 dashboard/checkpoint and local handoffs/checklists synchronized;
  umbrella OPEN / Goldberg Development Project In Progress; private PMP unchanged.
- [ ] Admin visual/no-scroll/locale/font/accessibility, notification/WorkManager
  runtime, seeded upgrade, approved live contract/session integration and owner
  acceptance remain OPEN. G1–G9/G11 integration unresolved; G10 locally closed.

[Final CI checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6374873953) · [Recovery record](stage-7/stage-7b/SAFE-RECOVERY.md).
The final docs-only checkpoint contains this evidence; its exact SHA/readback is
recorded in Issue31 after publication. No source/runtime claim is transferred from
3a4aac2 to another commit. Next: execute the remaining Admin fixture screenshot/
no-scroll matrix on an explicitly identified emulator, retaining all other gates.
Stage7A final owner acceptance and X10 deferred Gould0.12 remain unchanged; Stage8
readability/accessibility/touch-target checks remain required. No physical device,
main merge, release, deployment or production activation.

---


</details>

# Goldberg 0.11 — stage checklist (permanent, living document)

## Current — Stage 6 API local Milestone 1 (2026-10-23)

**LOCAL IMPLEMENTATION VERIFIED / PROVISIONAL v1 / INTEGRATION NOT ACCEPTED.**
Owner authorization permits this isolated adapter foundation and focused verification,
superseding planning-only limits for this milestone only. Branch
`feature/goldberg-0.11-stage-6-api-v1-local`, worktree
`/home/stokonn/Projects/Goldberg-Stage6-API`, exact approved baseline
`6dd3136269ec7f5932c8c4b3689fb065d4db9105`; original security branch/evidence preserved.
[Approved-design checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6371704250).

Purpose/scope: explicit in-process public request/response adapter around existing local
services for G5–G8. No listener, Android changes, browser/hosted integration or public
version adoption. `/v1` consumer compatibility remains unconfirmed, so local paths
are provisional; never silently select `/v2`.

- [x] Inspect Stage6 worktrees and dirty Stage7B read-only; create separate branch from
  exact baseline. No unrelated worktree reset/cleanup/edit or security branch change.
- [x] Implement strict method/path/body/schemaVersion/unknown-field/duplicate-key
  validation, explicit operation projection, safe error taxonomy, request correlation
  and empty204 logout. No competing-dialect inference.
- [x] Execute challenge/exchange/session/capabilities/refresh/logout through actual
  local services; stored identity/expiry projection, live grants, family revocation,
  approved durations and controlled RSA/JWKS exchange. Account principal is server-derived.
- [x] Execute explicit open and three decisions with independent entity/proposal
  revisions; typed200/201, generic409, exact public receipts stored atomically with
  effects,24h from completion, changed intent422 and live authorization before replay.
- [x] Lossless decimal-string int8 reads; valid unsupported full-int8 writes fail503
  after authentication/authorization; malformed values400; domain safe-increment
  exhaustion409. UTC microseconds preserved, scoped signed cursor codec15m tested.
- [x]106 adapter/wire tests PASS:34 service/scripted-SQL,27 original request-scene
  adapter tests,24 routing/validation/security,21 wire/cursor/composition units.
  Includes all8 protocol scenarios and one controlled RSA/JWKS exchange.38 relevant
  T22 regressions and18 planning-verifier tests PASS separately. Source bound before/
  after run and current verification; raw logs retained, no hidden skipped bodies.
- [ ] Local PostgreSQL preflight EPERM;0 DB bodies. No actual HTTP/Android/browser/
  runtime/hosted acceptance. Prior345 executions/313 tests/205 PG-backed bodies
  unchanged and not rerun; full threat matrix remains6.12 PARTIAL/T22 LOCAL PASS.
- [ ] Public latest-detail/refetch DTO, full-int8 moderation writes, PG receipt proof,
  client single-flight/account isolation/204 handling and real HTTP binding remain open.
- [ ] No G1–G4 implementation, real-world policy approval, cloud/OAuth production,
  networking activation, formal6.13–6.18 implementation or6.17 freeze. All real
  source/field/license/attribution/evidence policies stay empty/default-deny.

G10 CLOSED; G1–G9/G11 integration acceptance BLOCKED. Original service/schema/Android/
contract/evidence files unchanged. New receipt wrapper uses existing storage helpers/
table, no migration. See `stage-6/API-V1-LOCAL-M1.md` and package README for actual
operations, test venues, compatibility findings, limitations and next milestone.

Stage7A is FINAL ACCEPTED; X10 is NOT FIXED/NOT VERIFIED, owner-deferred to Gould0.12
([acceptance](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6371745218)).
This supersedes earlier conditional/X10-open cosmetic release gating in historical
entries below. Stage8 readability/accessibility/touch-target verification remains
required; Stage7B integration acceptance remains separate and blocked.

Next smallest local milestone: bounded authoritative latest-submission Detail/refetch
projection for409 recovery, with permitted disposable-PG verification of adapter/public
receipts. Numeric full-int8 writes stay a separate tested change. Stop at all remaining
production, policy, public-version and deployment gates. Publication instructions and
actual outcomes: docs/ai/STAGE6-API-M1-PUBLICATION.md.
Publication blocked: normal push DNS error; connected new branch lookup422/no commit.
Both Issue31 writes denied403; description and81 historical comments unchanged.
No new checkpoint URL; guarded Fedora source publication remains pending.


## Current — API v1 owner design approval recorded (2026-10-23)

**O1–O8 APPROVED AS QUALIFIED DESIGN; O7 PARTIAL; UNIMPLEMENTED / INACTIVE / NOT FROZEN.**
Owner authority: explicit instruction in this conversation; durable record
`stage-6/API-V1-OWNER-DECISIONS-2026-10-23.md` and machine-readable
`contracts/admin/v1/decision-proposal/design-approval-2026-10-23/approval.json`.
Published proposal baseline `fefa6e83c2a7b9c4c12660ff1542ab086356de88` and
[checkpoint6371392069](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6371392069)
verified through connected GitHub reads. This supersedes earlier unpublished-proposal
and pending O1–O8 summaries below; all previous evidence remains historical.

Purpose/scope: record the owner's exact design decisions, reconcile planning/readiness
and verify synthetic fixtures only. Current lifecycle/TTL/revision/numeric/HTTP/audit/
validation design is approved; actual local backend and Android behavior are unchanged.

- [x] Record O1 explicit mapper/204 logout/no secret-bearing offline queue, O2 exact
  access15m/family7d/challenge5m/skew≤60s and refresh rules, O3 independent revisions/
  explicit open/typed200–201/generic409/refetch/reconfirm/exact24h receipts.
- [x] Record O4 lossless int8 strings/UTC precision/scoped cursors15m/404–405 AS DESIGN;
  O5 server Europe/Prague civil daily boundaries including DST, UTC transport,
  coherent exact snapshot and one approval count; O6 NFC/case-insensitive/
  diacritic-sensitive literal search AS DESIGN; O8 documentation sequence only.
- [x] Record O7 PARTIAL: safety validation/1000 UTF-8-byte reason/current projection
  approved by design. All concrete real-world source/field/license/attribution/
  evidence policy sets remain empty/default-deny; no synthetic promotion/legal clearance.
- [x] Preserve original61 DTO/8 protocol scenes and historical UTC alternative;
  add separate4 Prague civil/DST windows and10 CZ/EN/literal search planning cases.
- [ ] Confirm no previously approved/deployed incompatible v1 consumer before `/v1`;
  otherwise prepare `/v2` for separate owner decision. No confirmation claimed here.
- [ ] Determine final query budgets by later local measurement; separately review
  concrete policies and authorize numeric/HTTP/backend/Android implementation.
- [ ] G1–G9/G11 integration BLOCKED; G10 CLOSED;6.12 PARTIAL/T22 LOCAL PASS;
  6.17 NOT FROZEN. No formal6.13–6.18 implementation or API freeze authorized.

Prior345 executions/313 tests/205 PostgreSQL-backed bodies preserved, not rerun.
No Stage7B edits, devices, live endpoints/auth/network/actions, cloud/OAuth production,
merge, deployment or release. Stage7B acceptance BLOCKED, X10 OPEN, Stage8 pending;
existing security/hosted/runtime/zero-cost/legal gates remain. Separate implementation
and policy authorization gates are the stopping point; no repeated O1–O8 approval needed.
Verification: docs/ai/VERIFICATION.md and the separate dated approval verification log.
Publication: docs/ai/API-V1-PROPOSAL-PUBLICATION.md; guarded publisher now emits the
approval checkpoint, never republishes the superseded pending-design summary.
Publication attempt blocked: normal push DNS failure; connected branch remains fefa6e8.
Both Issue31 writes denied403; description and79 prior comments unchanged, no new URL.
Guarded Fedora publisher with final exact clean SHA remains the next publication step.


Canonical policy: [AGENTS.md](../../../AGENTS.md) (§ "Stage checklist documentation").

**Release status (2026-10-22): Goldberg 0.11 = IN PROGRESS.** Stages 1–5 complete (Stage 5 owner-approved); Stages 6–8 pending. **Stage 7A = 🟧 CONDITIONALLY ACCEPTED by the owner on 2026-10-22 (X9 with reservation) — one text-alignment issue remains explicitly OPEN, with a required real-device regression check before the 0.11 release (gate X10; see the Stage 7A conditional-acceptance entry below).** The single umbrella issue [#31](https://github.com/stokonn/Goldberg/issues/31) tracks the whole release and stays **OPEN** until 0.11 ships (its GitHub Project item is **In Progress**); the Stage 5 entry below remains **Done**. Earlier it had been closed/set Done on Stage-5 approval — a tracking inconsistency corrected the same day (see DECISIONS.md).
This checklist is synchronized continuously as work progresses. A stage is marked
**complete** only after its verification section records checks that were actually
executed and passed. Checkboxes without verified evidence must stay unchecked; checked
boxes cite their evidence below or in the linked stage ledger.

Status legend: ✅ complete (verified) · 🚧 in progress · ⬜ not started

---

## Stage 1 — Architecture & readiness planning

**Status: ✅ complete (documentation-only stage).** Branch `planning/goldberg-0.11-stage-1`,
baseline `556970ab7407ec6986e3678657adbd2168516468`; evidence:
[Stage 1 verification](VERIFICATION.md).

- **Purpose:** Define the 0.11 product architecture, stage boundaries, decision register
  (D1–D9) and acceptance gates from the approved product scope, without touching code.
- **Scope:** `docs/planning/goldberg-0.11/*` only. No application, Gradle, schema, CI,
  signing, cloud account or release change.

**Tasks**

- [x] Verify ancestry, canonical/maintenance policies and preserved owner work (RELEASE_NOTES.md untouched).
- [x] Write stage independence gates, decision register and handoff rules (STAGES.md).
- [x] Write architecture/data-model/contracts sketches and offline-cache proposal (OFFLINE-CACHE.md, DATA-MODEL.md).
- [x] Record cloud/backend/admin gating documents (CLOUD.md, COMMUNITY-ADMIN.md) as future-stage decisions.
- [x] Run baseline host gates and record exact results (no new emulator runs claimed).
- [x] Reuse existing GitHub Project/milestone/umbrella issue #31; planning-only Wiki link.

**Verification (executed 2026-10-18):** Gradle cached build BUILD SUCCESSFUL + explicit
`testDebugUnitTest --rerun` **75 tests / 0 failures**; lint **0 errors** (83 warnings);
`check_migrations`, `check_backup_fixtures`, `check_history_paging`, `check_localization`,
`check_agent_policy` all PASS; 10-doc link/fence check PASS. Instrumentation not executed
(documentation-only change set; explicitly not claimed).

---

## Stage 2 — Contracts, fixtures and measured readiness

**Status: ✅ complete (research-only stage).** Branch `planning/goldberg-0.11-stage-2`,
descends from Stage 1 `c3d5e03b9a4936d8db393df664af209a8853c767`; evidence:
[stage-2/README.md](stage-2/README.md), [stage-2/READINESS.md](stage-2/READINESS.md).

- **Purpose:** Turn the approved product scope into concrete, testable contracts: catalogue
  data model and physical separation, provider field/rights policy, identity matching,
  backup evolution, measured storage/performance budgets, and adversarial fixtures.
- **Scope:** `docs/planning/goldberg-0.11/stage-2/**` (SQL fixtures, Python research tools,
  JSON contracts). No application code, no schema export change, no deployment.

**Tasks**

- [x] Finalize logical/physical model, adapter/search/provenance/bundle envelopes (CONTRACTS.md, contracts.json).
- [x] Specify separate catalogue DB with durable personal references (DATABASE-BACKUP.md).
- [x] Build deterministic licensed fixtures + adversarial contract tests (fixtures/, verify_stage.py).
- [x] Run bounded provider investigation with overload stops; record coverage and failures (SOURCE-RESEARCH.md).
- [x] Measure query cost, memory/storage and bundle budgets (MEASUREMENTS.md).
- [x] Owner settled D1/D2/D3/D6 and D7/D9 direction (multi-genre works/arrangements, UUIDs + legacy IDs, separate catalogue DB, measured caps).
- [x] Record remaining blockers (D4/D5/D8 backend/operator/Admin) as later-stage gates.
- [x] Commit/push planning branch; update existing tracking (issue #31 evidence).

**Verification (executed 2026-10-18):** deterministic fixture/contract suite via
`stage-2/verify_stage.py` PASS; research/bench runs are recorded as measurements, not as
production capacity claims; no application build was altered (research-only artifacts).

---

## Stage 3A — Additive local persistence (journal schema9 + catalogue schema1)

**Status: ✅ complete (verified).** Branch `feature/goldberg-0.11-stage-3a`, baseline
Stage 2 `d6049add3e46399d8f48afcb2aaa03422ebe0017`, prerequisite CI
[39120954962](https://github.com/stokonn/Goldberg/actions/runs/39120954962) passed;
evidence: [stage-3a/VERIFICATION.md](stage-3a/VERIFICATION.md).

- **Purpose:** Add the safe persistence bridge: transactional personal schema 9 (UUID
  anchors, catalogue links) and an independent catalogue database (schema 1, FTS4, bounded
  DAOs) without destructive migrations or journal rewrite.
- **Scope:** `OpusDatabase` (additive schema 9 + migrations), new catalogue DB/DAO/store,
  personal↔catalogue bridge, lazy retryable catalogue reconstruction, tests.

**Tasks**

- [x] Transactional schema 9 bridge preserving all schema 8 tables/bytes.
- [x] Independent catalogue schema 1 with bounded DAOs, FTS4, typed graph/rights validation.
- [x] Lazy, retryable local reconstruction isolated from journal startup.
- [x] All historical Android migrations + journal regression + catalogue failure tests.
- [x] Seeded signed in-place 0.10.1 upgrade with original data/prefs comparison.
- [x] JVM/build/lint/host compatibility checks and targeted measurements.
- [x] Technical documentation + English Wiki sources.

**Verification (executed):** full historical migration matrix on Android and host fixtures;
seeded in-place upgrade PASS; JVM/build/lint gates PASS (see stage-3a/VERIFICATION.md for
exact counts); instrumentation executed on emulator only.

---

## Stage 3B — Portable backup v5 and safe recovery

**Status: ✅ complete (verified).** Branch `feature/goldberg-0.11-stage-3b`, descends from
verified Stage 3A `cb36535d8a733143213347dd2ce355d1bf81ae96`, prerequisite CI 39125571414
passed; evidence: [stage-3b/VERIFICATION.md](stage-3b/VERIFICATION.md),
[stage-3b/CHECKPOINT.md](stage-3b/CHECKPOINT.md).

- **Purpose:** Make new identities/settings/recovery portable: additive journal schema 10,
  bounded hash-validated portable format 5, unchanged format 1–4 readers, raw-recovery
  evidence and allowlisted preference replay.
- **Scope:** backup reader/writer, journal schema 10 (additive), recovery/draft flows,
  golden fixtures, crash/corruption/size/atomic-publication tests.

**Tasks**

- [x] Additive journal persistence for raw recovery evidence, completion evidence, preference replay.
- [x] Bounded/hash-validated portable v5; historical readers 1–4 keep their meaning.
- [x] Destination snapshots/drafts/archives and imported UUIDs preserved; paused recovery with fresh execution IDs.
- [x] Allowlisted preference replay with explicit pending/retry state.
- [x] Full/minimal golden fixtures + corruption/crash/size/atomic-publication tests.
- [x] Sequential API 26 + API 37 instrumentation, seeded upgrade, offline and process-death checks.
- [x] Implementation/verification/handoff documentation + GitHub/Wiki synchronization.

**Verification (executed):** build/JVM/lint/host checks PASS; sequential API 26/API 37
instrumentation PASS; seeded upgrade, offline and process-death (×4 boundaries) checks PASS
(see stage-3b/VERIFICATION.md for exact counts and commands).

---

## Stage 4 — Provider adapters and unified read/search

**Status: ✅ complete (verified).** Branch `feature/goldberg-0.11-stage-4`, baseline Stage 3B
`9ae8d33e113feba531cdfa8558f0505cd4205749`; verified head `99ea73a` (also the Stage 5
baseline). Evidence: [stage-4/CHECKLIST.md](stage-4/CHECKLIST.md),
[stage-4/PROGRESS.md](stage-4/PROGRESS.md), `docs/ai/VERIFICATION.md` (baseline section).

- **Purpose:** Ship the local-first catalogue experience: five independent provider
  adapters (Open Opus, MusicBrainz, Wikidata, Wikipedia, Commons) behind typed contracts,
  a unified Explore/Search UI, indexed local search with generation-bound paging, cached
  provider responses with rights/attribution, and safe work/arrangement linking.
- **Scope:** provider transport/adapters, catalogue store/search/paging, Explore/Search UI,
  catalogue cache/portraits storage admission, personal-library links, CZ/EN resources.

**Tasks**

- [x] 4A Independent adapters, bounded cancellable transport, host allowlist, rights fields, adversarial fixtures, live provider probes.
- [x] 4B Typed catalogue integration, hash-bound observations, graph/identity validation, deletion/rebuild regressions.
- [x] 4C Indexed local-first search, ranking/normalization, generation-bound cursor paging, cache budgets/coalescing, privacy/offline/performance.
- [x] 4D Explore/Search/detail UI with provenance, CZ/EN, accessibility, rendered emulator checks (0.10.1 design identity preserved; owner waived screenshot review for 0.10, documented).
- [x] 4E Explicit work/arrangement selection, duplicate prevention, durable links/snapshots, practice/recovery regressions.
- [x] 4F Host checks, JVM/lint/APK, executed API 26 + API 37 instrumentation, seeded trusted 0.10.1 upgrade, artifact identity, branch verification docs.

**Verification (executed 2026-10-20 re-verification at `99ea73a`, emulator `emulator-5592`
API 37, explicit serial):** `testDebugUnitTest` **97 tests / 0 failures**; `lintDebug`
**0 errors** (107 warnings); `assembleDebug` + `assembleDebugAndroidTest` BUILD SUCCESSFUL;
`connectedDebugAndroidTest` **143 tests / 0 failures / 2 skipped** (`assumeTrue`
external-fixture guards); host checks `check_migrations`, `check_backup_fixtures`,
`check_localization`, `check_history_paging`, `check_agent_policy` PASS; seeded in-place
upgrade `verify_emulator_upgrade.py` (0.10.1/schema8 → schema10) PASS with exact
persistent-table equality; real process death ×4 boundaries PASS; APK SHA-256
`ac1ad27a5c42ec8f30a306c522943575183e2f5f4d9e41baef8f394d333aa3f4`, signer
`53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`, `com.opus.piano 0.10.1/code 11`.

---

## Stage 5 — Offline bundles and Smart Cache 2.0

**Status: ✅ complete — owner-approved 2026-10-21.** Branch `feature/goldberg-0.11-stage-5`,
baseline Stage 4 verified head `99ea73a`; completion commit `736f6d3` (owner approval) on
head `32cf5d9`. Owner-approved decisions: D5.1 file-based manifests, D5.3 WorkManager
(owner-mandated), D7 storage caps + per-job metered consent, D5.6 test-only org.json
(see `docs/ai/DECISIONS.md`). Verified evidence: emulator regression CI run
[39557624554](https://github.com/stokonn/Goldberg/actions/runs/39557624554)
(177 instrumentation tests / 0 failures / 0 errors / 2 designed skips) and screenshot
CI run [39576173002](https://github.com/stokonn/Goldberg/actions/runs/39576173002)
(12/12 required screenshots captured and inspected). **Limitation preserved by owner
disposition:** real production-CDN bundle-transfer measurements remain a future
integration dependency (no bundle feed exists; must be re-measured when it ships —
no numbers fabricated). The authoritative Stage 5 completion record (full executed
ledger, evidence bundles under `stage-5/results/`, final verification detail) lives on
the Stage 5 branch; **this Stage 6 planning branch does not repeat it and Stage 5 is
not reopened here.** No release/merge/tag authorized.

- **Purpose:** Let users keep encyclopedia/media content available offline through
  explicitly pinned, rights-complete bundles: durable file-based manifests, a
  content-addressed store with mark-and-sweep, a WorkManager-backed background scheduler
  that never downloads over mobile data without explicit per-job consent, hash-verified
  transfers with hard byte caps, atomic publication that keeps the previous READY revision
  through interrupted refreshes, and full isolation from personal journal/recovery data.
- **Scope:** `data/offline/*` (model, codec, policy, state machine, store, job records,
  network monitor, transfer engine, runner, scheduler, worker), `OpusApplication` lazy
  wiring + startup reconcile, test-only Gradle additions (org.json, work-runtime,
  work-testing). **No** journal/database/backup/signing/package change; no UI redesign.

**Tasks**

- [x] 5.1 Durable model + strict JSON manifest codec with mandatory per-file rights (OfflineBundle.kt, BundleManifestCodec.kt) — 21/21 codec tests.
- [x] 5.2 Reviewed lifecycle state machine incl. process-restart mapping and READY-safe recovery paths (BundleStateMachine.kt) — 13/13 tests.
- [x] 5.3 D7 policy matrix: validated network, per-job consent, Data Saver, stop reasons, confirmation gates (BundlePolicy.kt, BundleNetwork.kt) — 11/11 tests.
- [x] 5.4 Content-addressed store + mark-and-sweep + staging + quarantine (BundleStore.kt, BundleStoreLogic.kt) — logic 10/10 JVM tests; file I/O needs instrumentation.
- [x] 5.5 Durable per-job state: allowance/confirmation/attempt/cancel records (BundleJob.kt, BundleJobStore.kt, AndroidBundleJobPreferences.kt) — 8/8 tests.
- [x] 5.6 Transfer engine: streaming with hard caps, per-64 KiB policy re-checks, hash verification, provider host allowlist (BundleTransferEngine.kt) — 14/14 tests.
- [x] 5.7 Pipeline runner: QUEUED→READY with durable PAUSED/WAITING/FAILED_RETRYABLE/QUARANTINED outcomes (BundleJobRunner.kt) — instrumentation tests written.
- [x] 5.8 WorkManager scheduler + worker: unique work per `bundleId:revision`, UNMETERED unless per-job consent, revoke/grant/cancel, startup reconciliation (BundleScheduler.kt, BundleDownloadWorker.kt).
- [x] 5.9 `OpusApplication` lazy wiring + startup reconcile + hermetic test seams.
- [x] 5.10 Execute `Stage5*Test` instrumentation on an explicitly identified emulator (store I/O, runner pipeline, real WorkManager execution/cancel/reconcile). — **COMPLETE (owner-approved 2026-10-21): CI run 39557624554 (attempt 6), 177 tests / 0 failures / 0 errors / 2 designed skips.**
- [x] 5.11 Emulator acceptance: Wi-Fi→mobile transition, Data Saver, consent revocation mid-stream, real process death at boundaries, low disk, seeded in-place upgrade from trusted 0.10.1, disk/network cost measurements. — **COMPLETE: every item has executed passing evidence (run 39557624554 + instrumentation); screenshots run 39576173002 GREEN 12/12 (Ivory/Nocturne × six screens, pair-by-pair inspected). Disk/network costs: executed byte accounting + real file I/O recorded; real-CDN transfer measurements owner-dispositioned as a future integration dependency (no bundle feed exists).**
- [x] 5.12 Full host gate re-run + `scripts/check_*.py` (migrations/backup/localization/paging/policy). — **COMPLETE: host gate BUILD SUCCESSFUL, unit tests 174/0/0/0, lint 0 errors / 110 pre-existing warnings; all 5 check scripts PASS.**
- [x] 5.13 Update `docs/ai/*` (STATE/VERIFICATION/HANDOFF/DECISIONS), reviewed commit, push to Stage 5 branch, verified evidence to GitHub Issue #31 + Project. No merge/tag/release/APK publication; Stage 6 not started. — **COMPLETE: docs synchronized, evidence bundles committed, Issue #31 checkpoints posted, only workflow files merged to main (PRs #35–#38); no application code merged, no tag/release/APK publication.**

**Verification (executed so far, 2026-10-20 sessions — HISTORICAL mid-progress log,
retained as audit trail):**

- [x] 5.10 Execute `Stage5*Test` instrumentation on an explicitly identified emulator (store I/O, runner pipeline, real WorkManager execution/cancel/reconcile). — **COMPLETE: run 39557624554 (attempt 6), 177 tests / 0 failures / 0 errors / 2 skipped, stable across runs 4–6; 2 skips investigated = designed fixture assumptions (`skipped-tests.md`).**
- [x] 5.11 Emulator acceptance: Wi-Fi→mobile transition, Data Saver, consent revocation mid-stream, real process death at boundaries, low disk, seeded in-place upgrade from trusted 0.10.1, disk/network cost measurements. — **COMPLETE: every item has passed executed evidence.** Attempt 6 (run 39557624554): transition `101→100` + Data Saver both directions with state readback, process-death + cold restart, post-restart DB integrity (`ok`/`none`/`user_version 10`), seeded in-place upgrade all-green, startup measured. Instrumentation (same run, 177 tests): consent revocation mid-stream (`consentRevocationMidStreamPausesAndKeepsReadyContent` + worker/persistence variants), low disk (`lowStorageRefusesBeforeAnyByteMoves` + backup/portrait variants), corrupt assets, concurrent remove/clear/restore, old-READY-survives-interrupted-refresh, practice-available-with-caches-removed. **Screenshots: run [39576173002](https://github.com/stokonn/Goldberg/actions/runs/39576173002) GREEN — 12/12 (Home/Repertoire/Discover/Practice/Profile/Settings × Ivory/Nocturne), agent-inspected pair-by-pair, preserved in `results/ci-run-39576173002/` (after 3 script-defect attempts, each root-caused + fixed via workflow-only PRs #36–#38; no application defect in any of them).** Disk/network cost measurements: owner-dispositioned (DECISIONS.md) — executed tests' exact byte accounting + real file I/O + `disk-usage.txt`; real-CDN transfer recorded as a Stage 6/7 dependency (no bundle feed exists).
- [x] 5.12 Full host gate re-run + `scripts/check_*.py` (migrations/backup/localization/paging/policy). — **COMPLETE: `./gradlew clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1` → BUILD SUCCESSFUL (3 m 41 s), unit tests 174/0/0/0, lint 0 errors / 110 pre-existing warnings; all 5 check scripts PASS (agent_policy, backup_fixtures, history_paging, localization, migrations).**
- [x] 5.13 Update `docs/ai/*` (STATE/VERIFICATION/HANDOFF/DECISIONS), reviewed commit, push to Stage 5 branch, verified evidence to GitHub Issue #31 + Project. No merge/tag/release/APK publication; Stage 6 not started. — **COMPLETE: STATE/HANDOFF/VERIFICATION/DECISIONS + STAGE-CHECKLIST synchronized through the green screenshot run; evidence bundles committed (`ci-run-39557624554` 22 MB, `ci-run-39576173002` 12 MB); reviewed commit pushed; Issue #31 checkpoints posted for every session (run-6 green, decisions, screenshot attempts 1–4); Project kept In Progress. Only workflow files ever merged to `main` (PRs #35–#38); no application code merged; no tag/release/APK publication; Stage 6/7 not started.**

> **Reconciliation note (2026-10-21, Stage 6 planning branch):** this log was frozen
> mid-Stage-5 when this branch was cut (`bcd8894`). It is kept unchanged as historical
> evidence. The **authoritative final** Stage 5 verification record — including the
> green CI runs 39557624554 (177/0/0/2) and 39576173002 (12/12 screenshots), the
> root-cause triage of the earlier failed runs and the owner-approved completion
> (2026-10-21) — lives on `feature/goldberg-0.11-stage-5` (completion commit `736f6d3`,
> head `32cf5d9`) in its `docs/ai/*` files, `STAGE-CHECKLIST.md` and
> `docs/planning/goldberg-0.11/stage-5/results/`. Nothing here supersedes that record.

- Checkpoint #5 host gate (`clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug`) BUILD SUCCESSFUL; **146 tests / 0 failures**; lint **0 errors** (109 warnings).
- After 5.5–5.9 implementation: `compileDebugKotlin` BUILD SUCCESSFUL; `testDebugUnitTest` **174 tests / 0 failures** (28 new scheduler/engine/job tests).
- 2026-10-20 durable checkpoint: all Stage 5 work committed and pushed to the Stage 5 branch;
  permanent AGENTS.md rules (stage checklists, GitHub Issue checklists) added; STAGE-CHECKLIST.md created.
- 2026-10-20 instrumentation-fix session (uncommitted working tree, HEAD `54f2b45`):
  `Stage5BundleJobRunnerTest` compile issues fixed (6 errors, no assertion weakened) →
  **EXECUTED** `:app:assembleDebugAndroidTest` **BUILD SUCCESSFUL** (4m54s, 0 errors, 7 new
  nullable-`listFiles()` warnings); **EXECUTED** `:app:testDebugUnitTest` but all tasks
  UP-TO-DATE → 0 tests re-executed (last real counts remain the prior session's 174/0).
  `Stage5BundleWorkerTest` (12 tests, real WorkManager test infra) written; **EXECUTED**
  androidTest compile after adding it → **FAILED, exactly 1 error** (`Stage5BundleWorkerTest.kt:324`,
  nullable `TestDriver?` — one-line fix pending). Two static findings (F1 rights-quarantine
  unreachable, F2 corrupt-blob repair dead-end) recorded in `docs/ai/STATE.md`/`DECISIONS.md`,
  **not executed, no production change applied**. `docs/ai/*` checkpoint created (STATE/
  VERIFICATION/HANDOFF/DECISIONS). No tasks 5.10–5.13 completed; Stage 5 remains incomplete.
- Pending (not yet executed, no claim made): fix the worker-test compile error, re-verify
  androidTest compile + fresh `testDebugUnitTest --rerun`, resolve F1/F2, then Stage 5
  instrumentation on an explicitly identified emulator (5.10), emulator acceptance matrix (5.11),
  final host gate + `scripts/check_*.py` (5.12), `docs/ai/*` commit/push + Issue #31/Project sync (5.13).
- 2026-10-20 second resume session (checkpoint commit pushed as owner-authorized IN-PROGRESS
  checkpoint, verification incomplete): worker-test L324
  compile error FIXED → **EXECUTED** `assembleDebugAndroidTest` **BUILD SUCCESSFUL 0 errors**;
  **EXECUTED** `testDebugUnitTest --rerun` → **174/0/0/0** (fresh). F1/F2 investigated,
  confirmed against source and **owner-approved**: F1 test retargeted to the reachable contract
  (test-only), F2 `BundleStore.installBlob` dedup now verifies the existing target and
  quarantines invalid bytes before installing the verified copy (production fix, ~15 lines).
  After both changes: **EXECUTED** `testDebugUnitTest + assembleDebugAndroidTest` → **BUILD
  SUCCESSFUL, 174/0** (reports 19:23). PMP identified (Project Memory Protocol,
  `stokonn/project-memory-protocol`); local state verified **absent everywhere** (CLI, XDG
  state, `.pmp`/`.pmp.yml` in any commit, clones) — not reinitialized. Emulator toolchain
  installed (emulator 37.2.12.0 + android-36 google_apis x86_64), AVD `stage5-api36` created
  (AVD home: `/home/stokonn/.config/.android/avd` → needs `ANDROID_AVD_HOME`), **booted
  emulator-5580 (API 36, qemu=1)**; stale `app-debug.apk` (Oct 16) caught before any test and
  rebuilt (**EXECUTED** `assembleDebug` → BUILD SUCCESSFUL, fresh APK 19:45). Battery emergency
  stopped the session: emulator cleanly killed (`emu kill` OK); **zero instrumentation tests
  executed**; tasks 5.10–5.13 all remain open.
- 2026-10-20 third session (desktop machine, checkpoint commit pushed): fresh
  `assembleDebug + assembleDebugAndroidTest` **BUILD SUCCESSFUL** (1m08s, 0 errors, fresh
  APK hashes recorded in `docs/ai/VERIFICATION.md`). All five `scripts/check_*.py`
  **PASS**; full host gate re-run executed (result in VERIFICATION.md). Emulator work on
  the desktop **blocked by upstream SIGSEGV bug (Google #566807521)** on Fedora kernel
  7.2.10: both AVDs (playstore API 37 + new google_apis API 36 `stage5-api36`) and both
  emulator versions (37.2.12 + 37.3.4 canary) crash after the "protected range" warning,
  independent of GPU mode — **no instrumentation executed; tasks 5.10/5.11 deferred to the
  notebook's working API 36 emulator** (exact steps in `docs/ai/HANDOFF.md`). Host-side
  portion of task 5.12 executed on the desktop; 5.13 checkpoint docs updated + pushed, but
  5.13 stays open pending Issue #31/Project sync after 5.10/5.11. PMP local
  `.pmp/`/`.pmp.yml` preserved untouched (never reinitialized).
- 2026-10-20 GUI-mode + CI follow-up (same session): the owner-requested **GUI-mode**
  emulator attempt on the GNOME desktop (explicit `emulator-5580`, `-gpu auto`, session
  XAUTHORITY) also ended **`Killed by SIGSEGV`** with no ADB device → all local emulator
  retries stopped. A **CI verification workflow was prepared and later committed+pushed**
  as `1ef15bf` (`.github/workflows/stage5-emulator.yml`, `workflow_dispatch` only,
  SHA-pinned, full `connectedDebugAndroidTest` for 5.10, digest-verified 0.10.1 upgrade +
  network/process-death/startup evidence for 5.11); **no CI execution has happened; no CI
  results claimed**. See `docs/ai/STATE.md` items 6–7 and `docs/ai/VERIFICATION.md` rows
  11–12.
- 2026-10-20 CI-activation session (Phase A/B of the owner's CI plan; **no CI run, no
  emulator run, no instrumentation — approval gate pending**):
  - **Phase A — CI-only PR prepared, NOT merged.** Worktree off `origin/main`
    (`556970a`), branch `ci/stage5-emulator-verification`, commit `9870d99`, draft
    [PR #32](https://github.com/stokonn/Goldberg/pull/32) — diff verified **workflow-only**
    (1 file, +379): no application source, database, release, PMP or unrelated documents.
    Stage 5 working tree and all untracked files preserved untouched. Workflow safety
    re-reviewed: `workflow_dispatch` only, `contents: read`, `persist-credentials: false`,
    all five action SHA pins re-verified against their release tags, 30-day evidence
    retention, 150/120-min timeouts, per-ref concurrency, qemu-verified `emulator-NNNN`
    serial guards on every adb call.
  - **Phase B — findings investigated and resolved in the revised workflow (same file
    now also on this branch):** (1) *signing* — apksigner-verified the trusted 0.10.1
    release APK carries cert `53eface4…`; a CI debug candidate can never be installed
    over it in place, so the upgrade test now uses a **debug rebuild of the exact
    v0.10.1 tag built in the same job** (same runner keystore, signatures match by
    construction) while release-signature compatibility stays a publication gate —
    no production credentials in CI; (2) *network checks* — the old
    `cmd netpolicy set data_saver enable` is **invalid on API 36** (AOSP-verified) and
    the Wi-Fi→mobile/Data Saver steps only collected logs → replaced with
    `set/get restrict-background` state readback + `dumpsys` state assertions + a
    transport-level default-network parser (unit-tested; mobile data is re-enabled
    after the `--offline` upgrade run); (3) *process death* — now pulls the database
    after real force-stop → cold restart and asserts `PRAGMA integrity_check`/`foreign_key_check`/
    `user_version`; (4) *exclusions* — scan confirms **0** `@Ignore`/filters/excluded
    classes; the full 42-class suite runs unfiltered; (5) *coverage honesty* —
    CI-covered vs manual-only 5.11 items (consent revocation mid-stream, low disk,
    screenshots, real bundle-transfer costs) documented in the workflow header;
    (6) *SHA binding* — both jobs record and assert `git rev-parse HEAD == GITHUB_SHA`.
    Full executed/not-executed ledger: `docs/ai/VERIFICATION.md` (CI-activation section).
  - **STOPPED at the owner approval gate as instructed:** merging PR #32 into `main` and
    any manual `workflow_dispatch` require explicit owner approval (Phase C). Tasks
    5.10–5.13 remain open; no merge/tag/release; Stage 6 not started.
- 2026-10-20 first CI dispatch + failure-triage session (Phase C/D authorized by owner;
  **tasks 5.10/5.11 still open — both jobs failed BEFORE any application test ran**):
  - **Phase C/D executed:** CI-only PR #32 merged to `main` as merge commit `577d811`
    (diff verified workflow-only, 1 file, +384 lines; comment fix `e61313b` also mirrored
    to the Stage 5 branch as `bcd8894`); workflow dispatched against
    `feature/goldberg-0.11-stage-5` @ `bcd8894` (SHA binding asserted in-run).
  - **Run [39503450845](https://github.com/stokonn/Goldberg/actions/runs/39503450845):
    both jobs FAILURE, pre-test.** 5.10: GitHub-hosted API 36 emulator booted in ≈53 s
    (KVM works on runners), then the script died at line 1 — the emulator-runner invokes
    it with `/usr/bin/sh` (dash), which rejects `set -o pipefail`
    (`/usr/bin/sh: 1: set: Illegal option -o pipefail`, exit 2). 5.11: builds, trusted
    0.10.1 download and digest check succeeded; the signing step logged
    `MISSING` for all three APKs and the emulator step was skipped.
    **Owner-confirmed classification: CI setup/script failures — not application
    defects; no application conclusion may be drawn from this run.**
  - **Root causes established with evidence** (full bundle preserved under
    `docs/planning/goldberg-0.11/stage-5/results/ci-run-39503450845/`, raw logs SHA-256
    `a6b48abb…ad85`): (1) the sh/dash incompatibility above; (2) apksigner's print format
    changed in build-tools 37.0.0 (preinstalled on the runner and selected by
    `sort -V | tail -n1`) from `Signer #1 certificate SHA-256 digest:` to
    `V2 Signer: certificate SHA-256 digest:` — locally reproduced with the same release
    APK: both versions report the identical digest `53eface4…d59e`, only the label
    differs, so the old sed pattern matched nothing with exit 0 (no certificate
    mismatch occurred and none was concluded).
  - **Minimal workflow-only corrections applied (assertions unchanged/strict):** both
    emulator scripts now materialize into `$RUNNER_TEMP` via quoted heredoc and run with
    explicit `exec bash`; the signing step records raw apksigner stdout/stderr/exit
    codes + build-tools inventory to uploaded artifacts (no suppressed diagnostics) and
    parses both apksigner formats requiring exactly one distinct signer digest; trusted-
    certificate equality and baseline/candidate equality checks preserved verbatim;
    all three APKs inspected before failing.
  - **Corrections statically + functionally validated on the host (executed):** YAML/
    `bash -n`/`py_compile` all pass; wrapper mechanics proven under `sh -c` with
    stubbed adb/gradlew (guards, trap-through-exec, failure propagation); signing step
    sandbox in exact runner conditions (apksigner 37.0.0 selected) — success exit 0,
    genuine cert mismatch (separately generated test keystore) exit 1, corrupt APK
    exit 1 with diagnostics preserved. dash itself not installed locally (POSIX
    reasoning only for the 3 outer wrapper lines); actual API 36 suite runs pending
    re-dispatch.
  - **Workflow-fix PR for `main` prepared; STOPPED before merge/dispatch per owner
    instruction** (explicit approval required for significant main-branch change or new
    CI dispatch). Tasks 5.10–5.13 remain open; no merge of Stage 5 application code; no
    tag/release/APK publication; Stage 6/7 not started.
- 2026-10-20 second CI dispatch + failure-triage session (owner-approved attempt 2;
  **again no application tests executed — CI script-execution-model failure, not an
  application defect**):
  - **Approved steps executed:** pre-merge diff verified workflow-only (1 file,
    +79/−8); PR #33 merged to `main` as merge commit `a4a9509`; corrected workflow
    dispatched against `feature/goldberg-0.11-stage-5` @ `5cdc0d6` →
    [run 39508388525](https://github.com/stokonn/Goldberg/actions/runs/39508388525)
    (headSha verified); both jobs monitored to completion; evidence preserved at
    `docs/planning/goldberg-0.11/stage-5/results/ci-run-39508388525/`
    (raw logs SHA-256 `efa4baf3…42e7`, 12 artifacts incl. the new signing diagnostics).
  - **Attempt-2 fixes verified on the actual runner:** SHA binding passed (both jobs);
    all builds passed; **signing step PASSED with full diagnostics** — the runner
    recorded `apksigner used: /usr/local/lib/android/sdk/build-tools/37.0.0/apksigner`
    (empirically confirming the run-39503450845 root cause on the runner itself),
    release digest `53eface4…d59e` = trusted certificate, baseline = candidate =
    runner key `47aebcfd…34c6`, all exit codes 0, raw streams uploaded as artifacts;
    API 36 emulator booted in **49 524 ms** (KVM works).
  - **Run-2 root cause (proven from the pinned action's own source):**
    `reactivecircus/android-emulator-runner@a421e43` `parseScript()` splits the
    `script` input into **single lines** (comments/blanks stripped) and `main.ts` runs
    each line via a **separate `sh -c`** — state cannot persist between lines and
    multi-line constructs cannot work; the log shows three consecutive `sh -c` calls
    (`set -e`, heredoc opener, `set -euo pipefail` → dash error, exit 2). Run
    39503450845 died at line 1 of this same model. The action's `script` input only
    supports single-line commands.
  - **Meaningful fix applied (workflow-only; script body byte-identical):** a regular
    GitHub `run:` step (one `bash -e` process) materializes the approved script into
    `$RUNNER_TEMP/stage5-<job>.sh` + `bash -n` checks it; the action's `script:`
    becomes exactly one line `bash "$RUNNER_TEMP/stage5-<job>.sh"` → `parseScript`
    yields one chunk → the whole script runs in a single bash process.
  - **Fix validated on the host (executed):** `validate_v3.py` — YAML OK; faithful
    `parseScript` replica yields **exactly 1 chunk per job**; `bash -n` OK on both
    materialize steps and both materialized bodies; end-to-end simulation (GH run step
    → parseScript → `sh -c` → bash file with stubbed adb/gradlew/sqlite/scripts):
    **job 1 exit 0** (guards, variable persistence, EXIT trap, gradle invoked);
    **job 2 exit 0 through the full acceptance flow** (WiFi→mobile 100→101 transition
    with net-id inequality, Data Saver exact readbacks, force-stop/restart pidof
    gates, DB pull + `PRAGMA integrity_check/foreign_key_check/user_version`
    assertions, 5× startup, trap; 25 deep `emulator-5554` adb calls prove persistence);
    negative guard test propagates exit 1. Canned stubs validate the execution
    model only — application assertions are verified solely by the real CI run.
  - **Attempt 3 launch (2026-10-21, documentation-first):** workflow-only PR #34 merged
    to `main` as `816d142` (pre-merge `gh pr diff 34 --name-only` = workflow file only,
    +42/−35; post-merge `git diff a4a9509 origin/main` re-verified workflow-only; PR
    branch byte-identical to the Stage 5 workflow copy); dispatched against Stage 5
    checkpoint `9b80d65` (headSha verified).
  - **Attempt 3 result (2026-10-20/21, [run 39511602936](https://github.com/stokonn/Goldberg/actions/runs/39511602936)
    @ `9b80d65): FIRST RUN TO EXECUTE REAL APPLICATION TESTS — CI pipeline now works
    end-to-end** (materialize steps ✓, both scripts ran as single bash processes on
    booted emulators, `connectedDebugAndroidTest` executed). Evidence:
    `docs/planning/goldberg-0.11/stage-5/results/ci-run-39511602936/`
    (raw logs SHA-256 `6e93fdd4…1ba11`, 253 artifacts incl. per-test logcats).
  - **5.10 — EXECUTED, FAILED: 177 tests, 162 passed, 13 failed, 0 errors, 2 skipped.**
    All Stage 1–4 suites passed on-device; failures all in 3 Stage 5 classes:
    Stage5BundleJobRunnerTest (6: concurrent shared dependency, interrupted download/
    refresh retry, corrupt source/stored blob, consent revocation mid-stream),
    Stage5BundleStoreTest (2: interrupted refresh atomic promote, reopen survival),
    Stage5BundleWorkerTest (5: queued bundle, offline retry, transport retry, cancelled
    job, missing metered consent — all `got Failure {mOutputData=Data {}}`). Observed
    signatures include on-device `Retryable(error=integrity/transport)`,
    `Integer<22> vs Long<22>` JSON type coercion, `List has more than one element`.
    Root causes NOT yet determined — code-level triage in progress; no assertion
    weakened.
  - **5.11 — EXECUTED, FAILED at the FINAL offline-state assertion only
    (`verify_emulator_upgrade.py:143`, 'Offline state changed during upgrade').**
    Everything before it PASSED on the emulator: baseline install + launch + schema
    verify, seed push, candidate install + launch, prefs unchanged, `before == after`
    persistent-data equality, `PRAGMA user_version` = expected schema,
    `foreign_key_check` clean, UUID-v4 identity bridge — **the seeded in-place upgrade
    from v0.10.1 with full data preservation is verified working on the emulator.**
    Offline-signature assumption on API 36 to be investigated (capture dumpsys output;
    do not weaken).
  - **Gate held:** diagnosis + documentation recorded BEFORE any re-dispatch; no new
    dispatch started. Tasks 5.10–5.13 remain open; Stage 5 IN PROGRESS.
  - **Code-level triage COMPLETED (2026-10-21) — all 13 root causes confirmed from code
    + the preserved CI artifacts** (Issue #31 checkpoint carries the full table):
    5 = test-harness context bug (`TestListenableWorkerBuilder` stores the raw
    `targetContext`, so `BundleDownloadWorker`'s `applicationContext as? OpusApplication`
    failed at entry → `Result.failure()`; real WorkManager passes the `Application`,
    proven by its sibling test passing on-device); 7 = deterministic test/fixture
    defects never surfaced before (these androidTest-only tests had never executed
    anywhere — KDoc: "compile-verified only until task 5.10 executes them"): random
    per-call fixture bundleIds ×2, URL selector matching every fixture URL, blob never
    installed before promote, JUnit `assertEquals(String, Int, Long)` Object-overload
    boxing (`Integer<22>` vs `Long<22>` — NOT JSON coercion); 1 = **genuine product bug**
    (`settle()` kept `validatedAt` on STALE→QUEUED, violating the manifest invariant;
    `settleQuietly` swallowed the IAE → bundle wedged STALE forever) + 1 **invariant gap**
    (`readPublished()` exposed partial QUEUED/FAILED_RETRYABLE manifests as "published",
    violating "a partial bundle is never published"). The earlier real-network hypothesis
    was **ruled out by code** and the run summary corrected.
  - **Fixes implemented + locally verified (commit `dcea5b2`, working tree = pushed
    Stage 5 branch):** product fixes in `BundleJobRunner.settle()` (drop `validatedAt`
    for non-authoritative targets), `BundleStore.readPublished()` (authoritative states
    only, `PUBLISHED_STATES`), `BundleJobRunner.locate()` (audit-mandated: resolve via
    `readAll()` + `readPending()`), comment alignment in `OfflineBundle.kt`; harness fixes
    in all three test classes (builder `applicationContext`, shared bundleIds, exact-URL
    routing with identical shared bytes, blob installed before promote,
    `content.size.toLong()`). **No assertion weakened; no test skipped** (verified: diff
    removes no assertion except the type-corrected one, which remains with identical
    message/expected/actual semantics).
  - **Verification EXECUTED locally:** `./gradlew testDebugUnitTest` → **BUILD
    SUCCESSFUL, 174 tests, 0 failures** (bundle suites: Job 8, Codec 21, Policy 10,
    StateMachine 13, StoreLogic 10, TransferEngine 15 all green); `assembleDebugAndroidTest`
    → **BUILD SUCCESSFUL**. Not executed locally: androidTest runtime (host emulator
    blocker documented in STATE) — device execution is what the CI dispatch provides.
  - **Attempt 4 dispatched under the sanctioned loop (2026-10-21,
    [run 39518973833](https://github.com/stokonn/Goldberg/actions/runs/39518973833) @
    `dcea5b2`, headSha verified; both jobs' `source-commit.txt` confirm the SHA).**
  - **5.10 — EXECUTED AND PASSED: 177 tests, 0 failures, 0 errors, 2 skipped on the
    API 36 emulator.** All 13 run-3 fixes verified on-device (worker cluster,
    fixture/endpoint routing, JUnit overload, product `settle()`/`readPublished()`/
    `locate()` fixes). Evidence:
    `docs/planning/goldberg-0.11/stage-5/results/ci-run-39518973833/`
    (raw logs SHA-256 `4b92dc7a…ab28`, counts/XML/testlog/logcat; full 257-file set in
    GitHub artifacts, 30-day retention). Task 5.10's connected-suite execution now has
    executed-**passing** evidence; remaining 5.10 items (notebook/manual-only: consent
    revocation mid-stream, low disk, screenshots, real bundle-transfer costs) still open.
  - **5.11 — upgrade validation PASSED again; failed at the final offline assertion
    with the root cause now PROVEN from logcat (not inferred):** `svc wifi disable`
    worked (`WifiService: setWifiEnabled … enable=false`, 00:11:59.904), but **`svc data
    disable` had zero observable effect** — no telephony data-disable event anywhere in
    the 84 730-line acceptance logcat, and the emulated CELLULAR network registered at
    00:12:15, became the active default network at 00:12:19, and served `com.opus.piano`
    throughout the "offline" upgrade. The assertion (`Active default network:
    none|null|-1`) correctly caught a premise that was **never achieved** — not an app
    regression. Post-python acceptance sections (Wi-Fi→mobile transition, Data Saver,
    process-death, post-restart DB, startup measurement) still never executed in CI.
  - **5.11 fix designed + documented BEFORE implementation:** verify/retry loop after
    the disables (`settings put global mobile_data 0` belt-and-suspenders; escalation to
    `cmd connectivity airplane-mode enable` if still online), `dumpsys connectivity`
    captured to the artifact dir at every stage, fail-fast with evidence if offline
    cannot be achieved, final assertion semantics **unchanged**, workflow section (b)
    adds airplane-mode disable before the restores. No assertion weakened anywhere.
  - **Fix implemented + locally verified (2026-10-21, documented before dispatch):**
    `scripts/verify_emulator_upgrade.py` — `connectivity_dump(tag)`/`offline_reached()`
    helpers; `--offline` now runs the original `svc wifi/data disable` **plus**
    `settings put global mobile_data 0`, a 10×3 s verify/retry loop (re-issuing the
    disables; dumpsys evidence saved per attempt), `cmd connectivity airplane-mode
    enable` escalation with 5 further attempts, and an **assertion that offline was
    established before the upgrade** (fail fast, evidence-first). The final assertion
    (was line 143) is **semantically unchanged** (same regex, same message) but now
    saves `connectivity-final.txt` first. Workflow section (b) clears airplane mode
    and re-enables data (`svc data enable` + `settings put global mobile_data 1`)
    before the Wi-Fi→mobile transition. **Local verification EXECUTED:**
    `python3 -m py_compile` OK; workflow YAML parse OK (both jobs); `bash -n` OK on
    both materialized scripts extracted exactly as GitHub will run them; behavioral
    simulation with a stubbed adb **3/3 scenarios correct** (A: toggles apply after 2
    attempts → offline at attempt 3, proceeds to install; B: toggles no-op → airplane
    fires once, offline at attempt 13; C: never offline → fail fast with the exact
    message **before install**, 15 evidence files); `check_agent_policy.py` PASS. Not
    executed locally: real emulator behavior (host blocker) — attempt 5's purpose.
  - **Owner approved fix + verify + fifth dispatch (recovery session); the diagnose-
    and-document-first gate was honored: run-4 results + root cause + fix design were
    documented (commit `a427d88`) before implementation, and the implementation +
    local verification are committed before dispatching.**
  - **Attempt 5 dispatched (2026-10-21,
    [run 39552116455](https://github.com/stokonn/Goldberg/actions/runs/39552116455) @
    `6da1021`, headSha + both jobs' `source-commit.txt` verified):**
  - **5.10 — EXECUTED AND PASSED (second consecutive green): 177 tests, 0 failures, 0
    errors, 2 skipped** on the API 36 emulator. Evidence:
    `results/ci-run-39552116455/instrumentation-counts.txt` + full per-test XML
    (SHA-256 `eee30ad8…98a141`).
  - **5.11 — the python upgrade verification PASSED IN FULL for the first time ever:**
    **`offlineVerified: true` — the attempt-5 offline fix worked** (offline reached at
    verify-loop attempt 1, held through the whole upgrade: `connectivity-final.txt` =
    `none`; no airplane escalation needed); seeded v0.10.1 → candidate in-place
    upgrade fully green (`originalRowsUnchanged`, `preferencesUnchanged`, 37 sessions,
    UUID bridge 3/113, `foreignKeys: pass`, schema 8 → 10); **startup measurement
    executed for the first time** (baseline ≈ 3.6–5.1 s vs candidate ≈ 2.2–2.6 s cold
    process, small-sample caveat noted in `result.json`).
  - **5.11 — failed in section (b) (the Wi-Fi→mobile transition parser, its FIRST
    real execution) with the root cause PROVEN from artifacts:** the host-side
    `assert_default_network.py` read the transport from the legacy `ni{…} type:` token,
    but the API 36 dump prints it only in `nc{[ Transports: … ]}`
    (`connectivity-dump.txt`: `Active default network: 101`,
    `ni{WIFI CONNECTED extra: }`, `nc{[ Transports: WIFI …]}`). **The emulator itself
    behaved correctly** — Wi-Fi reconnected within ~4–8 s and became the validated
    active default (14 loop iterations of evidence in `network-evidence.txt`); only the
    parser could not read the token, so `wait_for_transport WIFI` hit its 60 s deadline
    and `set -e` killed the script. Not an app defect (app not running in this
    section); not a data issue (the upgrade validation had already passed). Sections
    (c)/(d) and the Data Saver half of (b) still never executed. Secondary defect found
    in triage: the deadline FATAL echoed into the `$(…)` substitution and reached
    neither log. Full narrative:
    `results/ci-run-39552116455/run-summary.md`.
  - **Fix implemented + locally verified (documented BEFORE implementation):** parser
    now reads `nc{\[\s*Transports:\s*(\w+)` (canonical API 36 location) with explicit
    `WIFI→WIFI`/`MOBILE→CELLULAR` mapping and the legacy `ni type:` path kept as
    fallback; deadline FATAL echoed to stderr. **Assertions unchanged** (active default
    must be numeric AND declare the expected transport). Local verification
    **EXECUTED:** YAML parse OK; `bash -n` OK on both materialized scripts; **fixed
    parser run against the REAL run-5 dumps — 5/5 checks pass** (A: WIFI @ real dump →
    OK; B: MOBILE @ cellular-active variant → OK via CELLULAR; C: WIFI @ cellular dump
    → still FATAL, negative strictness preserved; D: offline dump → numeric FATAL
    unchanged; E: legacy-format synthetic → fallback OK). Not executed locally: real
    emulator behavior (host blocker). A sixth dispatch awaits owner approval.
  - Tasks 5.10–5.13 remain open; Stage 5 IN PROGRESS.
  - **Attempt 6 dispatched after owner approval and FULLY GREEN (2026-10-21,
    [run 39557624554](https://github.com/stokonn/Goldberg/actions/runs/39557624554) @
    `44645fa`, headSha + both jobs' `source-commit.txt` verified) — the first fully
    green Stage 5 CI run:**
  - **5.10 — PASSED (third consecutive green): 177 tests, 0 failures, 0 errors, 2
    skipped.** Evidence: `results/ci-run-39557624554/instrumentation-counts.txt` +
    per-test XML.
  - **5.11 — PASSED IN FULL; every acceptance section executed for the first time:**
    - **(a) python upgrade verification** (unchanged since run 5): `offlineVerified:
      true` (offline at attempt 1, held through upgrade), `originalRowsUnchanged`,
      `preferencesUnchanged`, 37 sessions, bridge 3/113, `foreignKeys: pass`;
      signing green (trusted cert `53eface4…` matched; baseline = candidate debug
      cert; 3× apksigner exit 0).
    - **(b) Wi-Fi→mobile transition — first execution, PASSED:** the fixed parser
      read the API 36 format: Wi-Fi restored → `active default network: 101`,
      `svc wifi disable` → `100`, **`OK: default network transitioned (101 → 100)`**
      — the run-5 parser fix verified by real execution, not just local checks.
    - **(b) Data Saver — first execution, PASSED:** enable/disable with `cmd
      netpolicy` state readback (`enabled`/`disabled`) **and** `dumpsys netpolicy`
      (`Restrict background: true`/`false`) — `netpolicy-enabled/disabled.txt`.
    - **(c) process-death + cold restart — first execution, PASSED:** force-stop
      confirmed dead → relaunch confirmed alive → databases listed.
    - **(d) post-restart DB integrity — first execution, PASSED:** pulled
      `db-after-restart/opus.db` (preserved): `PRAGMA integrity_check: ok`,
      `foreign_key_check violations: none`, `user_version: 10`.
    - **Startup measurement — first execution, PASSED:** workflow cold launches
      TotalTime 2262/2408/2587/2445/2004 ms; python matched samples baseline
      `[4765,5204,4774,4908,4019]` vs candidate `[2423,2436,1997,2323,2049]` ms
      (candidate ≈2× faster; small-emulator-sample caveat recorded).
    - Evidence: `results/ci-run-39557624554/` (28 files; large logcats retained in
      GitHub artifacts with recorded SHA-256).
  - **CI gate status: the mandatory CI verification for 5.10/5.11 is green
    end-to-end.** Stage 5 stays IN PROGRESS until the notebook/manual-only items
    (consent revocation mid-stream, low disk, screenshots, real bundle-transfer
    costs → 5.12/5.13) also pass. No merge/tag/release performed.
  - **Post-run-6 session (2026-10-21): evidence preservation + skip investigation +
    coverage mapping + 5.12 host gate (all EXECUTED):**
    - *Evidence preserved permanently* — attempt-6 bundle grew to 22 MB: both **full
      logcats** (`acceptance-logcat.txt` `1d29f99c…70cdba`,
      `instrumentation-logcat.txt` `c7c47039…630d9b`, SHA-256 verified identical to
      the downloaded artifacts), per-test logcat for each acceptance-relevant test
      (`consentRevocationMidStream…`, `lowStorageRefusesBeforeAnyByteMoves`,
      `revocationWithdrawsConsentCancelsWork…`), JUnit XML + counts, test-results
      log, DB checks (`db-after-restart/opus.db` + integrity text), startup
      measurement, netpolicy/transition/upgrade evidence — nothing needed remains
      only in the 30-day GitHub artifacts. Reports/HTML (derivative of the XML) not
      committed; retained in artifacts.
    - *Skip investigation* — **`skipped-tests.md`**: both skips are designed
      `assumeTrue` fixture conditions (`Stage4ExternalProviderTest` line 17, `Stage4ExternalPortraitTest`
      line 26 — separately-acquired licensed fixtures **never bundled into either
      APK** by policy); skip set byte-identical across runs 3/4/5/6; `AppLanguageTest`
      **ran and passed** (9.2 s) — an earlier looser parse that suggested otherwise was
      a regex-pairing bug in the ad-hoc extractor, corrected and documented. No test
      hidden: run-1 audit already proved 0 `@Ignore`/filters/exclusions.
    - *5.11 coverage mapping (executed vs open)* — **EXECUTED on the emulator:**
      consent revocation mid-stream → `Stage5BundleJobRunnerTest::consentRevocationMidStreamPausesAndKeepsReadyContent`
      (revoke fired **while bytes stream**, per test source), `Stage5BundleWorkerTest::revocationWithdrawsConsentCancelsWorkAndPausesTheManifest`,
      `missingMeteredConsentPausesAsSuccessAndKeepsRefusalDurable`, `Quality092PersistenceTest::mediaConsentCacheOfflineFailureAndRevocationAreEnforced`;
      low disk → `Stage5BundleJobRunnerTest::lowStorageRefusesBeforeAnyByteMoves`
      (+ `Stage3bBackupTest::exportIsAtomicOnLowSpace…`, `Stage4PortraitTest::missingConsentBlocksTransferAndInsufficientReserve…`)
      — all with per-test logcats in the bundle. Plus attempt-6's (b)/(c)/(d)/startup
      results above. **OPEN:** UI screenshots in both themes (host emulator blocked by
      the upstream SIGSEGV bug → needs a purpose-built CI job) and **real
      bundle-transfer disk/network costs** — structural blocker: the product has **no
      production bundle feed/CDN URL and no production enqueue path** (verified:
      `BundleScheduler` has no non-test caller; no bundle URL anywhere in
      `app/src/main`; download UI is Stage 7) → real-CDN costs are not measurable
      today; current measured evidence = exact byte accounting through the real
      engine + real file I/O in the executed tests + emulator `disk-usage.txt`
      snapshots. Owner decision requested (see Issue #31).
    - *5.12 host gate* — **green** (checkbox above); exact results recorded.
  - **Screenshot session (2026-10-21, owner-approved "build + dispatch"): the last
    open 5.11 item is now EXECUTED and green.**
    - Workflow `.github/workflows/stage5-screenshots.yml` (dispatch-only, same
      safety rules as the green workflow) merged workflow-only via **PR #35**;
      theme switched through the product's **own persistence**
      (`profile_settings.theme` in `opus.db` via `run-as` + `sqlite3` with
      readback assertion), UI driven via the accessibility tree, onboarding
      dismissed through its own Skip button.
    - **Three failures, three distinct script defects — each root-caused from
      raw logs with host-side proof where possible BEFORE re-dispatch; zero
      application defects:** (1) run 39571926478 — theme write before first
      launch, DB absent → `prime_app()` (PR #36); (2) run 39572917657 —
      adb device-shell re-parse split the SQL → stdin-piped `sql()` (PR #37,
      host-proven byte-for-byte); (3) run 39574447299 — fresh install has **no
      settings row** (independently confirmed by the green run's
      `prime.txt`: `row count: 0`) → upsert of the entity-default row (PR #38,
      host-proven for both row states); the script's own readback guard
      (exit 42) caught it — assertions never weakened.
    - **GREEN: run [39576173002](https://github.com/stokonn/Goldberg/actions/runs/39576173002)
      @ `3579417` — 12/12 screenshots** (Home/Repertoire/Discover/Practice/
      Profile/Settings × Ivory/Nocturne), cold start 9.4 s, sha256 manifest,
      preserved in `results/ci-run-39576173002/` (+ `run-summary.md`).
    - **Agent inspection: every pair verified** — identical layouts, correct
      Ivory/Nocturne semantic palettes, adapted system bars, real content,
      no blank frames. Owner visual sign-off offered as courtesy.
  - **Stage 5 status after this session: ALL mandatory verification
    requirements have executed evidence that passed.** 5.10 ✓ (run 6),
    5.11 ✓ (run 6 + screenshots run + owner-dispositioned cost measurement),
    5.12 ✓ (host gate). Remaining: task 5.13 final documentation/handoff.
  - **OWNER APPROVAL (2026-10-21): the owner approved the Stage 5 completion
    claim** based on the verified CI evidence (runs 39557624554 + 39576173002),
    the completed checklist, the archived screenshots and the final
    documentation. **STAGE 5 IS COMPLETE.** The GitHub Project item #31 is
    moved to Done; Issue #31 keeps the permanent audit trail. Carried forward
    as a **future integration dependency**: real bundle-transfer disk/network
    costs against a production CDN remain unmeasurable until a production
    bundle feed/CDN URL + enqueue path exist (Stage 6/7 scope; no feed exists
    today — DECISIONS.md). Application code stays unmerged; no tag/release/APK.

**Completion criteria (from STAGES.md Stage 5 acceptance):** process-death/resume,
low-space, corrupt-asset, concurrent remove/clear/restore tests; Wi-Fi→mobile,
consent-revoked and Data Saver cases on an emulator; no unexpected mobile traffic; complete
offline credits and partial states; old READY revision survives interrupted refresh;
practice data stays available when all bundles are removed; measured disk/network costs.
Mark ✅ only when all of the above have executed-passed evidence.

---

## Stage 6 — Secured optional community backend

**Status: ⬜ planning prepared and owner decisions recorded (2026-10-21);
implementation not started.** The detailed implementation-ready plan lives in
[stage-6/](stage-6/README.md) (architecture, auth, data model, API contracts, threat
model, zero-cost ops, decision register, handoff); the authoritative task list is
[stage-6/CHECKLIST.md](stage-6/CHECKLIST.md) — **every implementation task there is
unchecked.**

**Owner decision review (2026-10-21), outcomes recorded in
[stage-6/OPEN-DECISIONS.md](stage-6/OPEN-DECISIONS.md):**

- ✅ **Approved:** D4a Supabase Free (one optional backend, strict 0 CZK/month, no
  paid plans/trials/auto-upgrades); D4b free-tier suspension/quotas accepted with
  graceful degradation, core local functionality preserved and fail-closed behavior;
  D5c Frankfurt `eu-central-1` (with pre-provisioning verification of availability,
  residency, transfers, DPA, subprocessors); D5f general-audience moderated community
  with minimal data; D5g owner-only moderation initially with server-side audited
  grants; D8a in-app Android Admin (no separate APK); D8c Supabase-console bootstrap
  with MFA-protected operator access and audited recovery.
- 🟧 **Approved conditionally:** D8b Web Admin BFF (HttpOnly/Secure/SameSite=Strict
  cookies, server-side authz/CSRF, no bearer tokens in browser JS) — **must first
  prove the 0 CZK fit** (task 6.0f) or report the blocker. *Feasibility study
  executed 2026-10-21 → CONDITIONAL PASS
  ([stage-6/BFF-FEASIBILITY.md](stage-6/BFF-FEASIBILITY.md)); **C1 APPROVED
  2026-10-21** (Cloudflare Workers Free host, architecture-level only — no
  provisioning); **C2 EXECUTED 2026-10-21 → CONDITIONAL PASS** (isolated local
  `workerd` spike on `spike/goldberg-0.11-c2-bff`, final commit `7bc6976`:
  19/19 security, P1–P7 executed, kernel CPU **2.03 ms/invocation**;
  per-invocation production CPU unverified, §12 production-runtime
  verification owner-gated); **C3 documentary review COMPLETE 2026-10-21**
  ([stage-6/C3-EDGE-PRIVACY-REVIEW.md](stage-6/C3-EDGE-PRIVACY-REVIEW.md):
  conditional privacy viability at 0 CZK, no compliance claim, legal/operator
  questions open). **C4 remains open** — its pre-provisioning checklist was
  prepared 2026-10-21
  ([stage-6/C4-PREPROVISIONING-CHECKLIST.md](stage-6/C4-PREPROVISIONING-CHECKLIST.md),
  preparation ≠ final verification).*
- 🟨 **Provisional / deferred:** D5a personal contact email is the intended public
  contact but **no address was supplied or authorized — nothing may be published**;
  D5b controller/operator determination deferred to pre-launch review — **research
  deliverable [stage-6/Q1-OPERATOR-READINESS.md](stage-6/Q1-OPERATOR-READINESS.md)
  prepared 2026-10-21; still legally unresolved and blocking for 6.0d**; D5d
  retention/deletion principle approved with **details unresolved**; D5e licensing
  approved subject to pre-launch legal review.

**Separately gated (never authorized by the decision review):** implementation start,
provisioning (6.0d), Google OAuth console setup (6.0e), and public launch/collection
(pre-collection gate, OPEN-DECISIONS.md §5). No provisioning, no OAuth configuration,
no client-side speculation, no paid service has occurred.

**Cloud-readiness reconciliation (2026-10-21, later session, documentation-only):**
the owner chose the approved **cloud architecture as the intended deployment
path** (Cloudflare Workers Free + Supabase Free `eu-central-1`, strict 0 CZK)
**instead of the temporary local-only Option C** — Option C is now only the
current gated posture, not the intended path; **no gate changed**. The earlier
"owner is a minor" premise is **reclassified as an unverified assumption, not a
verified current fact** (no age/identity/family detail may be requested or
recorded); **D5b stays legally unresolved and blocking for 6.0d**, and account
registration would not establish compliance. The apparent **6.0f ↔
provisioning gate-ordering conflict** (C2 §12 production measurements need a
deployed Worker; provisioning is owner-gated on C4 + D5b) is analyzed in
[stage-6/CLOUD-READINESS-PLAN.md](stage-6/CLOUD-READINESS-PLAN.md) with an
explicit minimal sequence (documentary verification → contractual readiness →
owner-authorized *limited private verification* provisioning → day-of C4 →
isolated synthetic-data production-runtime C2 verification → 6.0f accept/
reject → separate implementation authorization → later launch gates) and
alternatives **ORD-A/B/C left pending as an owner decision**.

**ORD-A APPROVED 2026-10-21 (sequencing only, documentation-only session):**
the owner selected **ORD-A — Scoped Private Cloud Verification Provisioning**
as the sequencing strategy for closing 6.0f (**ORD-B/ORD-C not selected**),
recorded with the approved 11-step sequence, the reconciled gate table (C1
approved · C2 conditional pass · C2 §12 open · C3 complete · C4 open · Q1/D5b
open · ORD-A approved (sequencing only) · 6.0f unchecked · 6.0d/6.0e not
authorized · Stage 6 implementation not started) and the **ORD-A readiness
checklist** for the future owner decision in
[stage-6/CLOUD-READINESS-PLAN.md](stage-6/CLOUD-READINESS-PLAN.md) §§3–5. **ORD-A
approves the ordering of future steps only** — it is not acceptance of 6.0f,
not a D5b resolution, and not authorization to create accounts, provision,
deploy, configure OAuth or implement. **6.0f stays unchecked; Stage 6
implementation stays NOT STARTED; nothing provisioned.**

**Q1 natural-person operator reassessment 2026-10-21 (documentation-only,
current-state; supersedes only earlier Q1 framing, all gates unchanged):**
[stage-6/Q1-OPERATOR-READINESS.md](stage-6/Q1-OPERATOR-READINESS.md) was
reassessed after fresh source checks (Cloudflare SSSA, Supabase ToS at its new
canonical `https://supabase.com/terms` URL, Czech Civil Code verze 18 eff.
2026-01-01, Trade Act 455/1991, GDPR Art. 2/4/8 wording from an unofficial
mirror — official EUR-Lex retrieval still failing). Its option set now covers
**four frames — Q1-A (adult/guardian of record, minor as technical operator),
Q1-B (legal entity of record), Q1-C (deferred provisioning, still the current
posture), candidate Q1-D (legally capable natural-person operator acting in
their own name)** — mutually exclusive ownership/governance frames for one
future accountable GDPR controller, compared in a full A/B/C/D decision matrix
(§5.5) with a technical (non-legal) recommendation to evaluate D first after
professional review (§5.6). **Legal capacity is treated solely as an
eligibility precondition for the eventual accountable party — never a personal
fact to be requested, recorded or inferred** (PA-2 scope). The
free/non-commercial product direction is recorded as PD-1 (Goldberg intended
to remain free of charge; commercial features may be considered in a future
version; **no monetization approved or implemented**).
**No option selected; Q1/D5b remain UNRESOLVED and blocking for 6.0d; D5b is
not resolved; no GDPR compliance claimed; professional review is not
unnecessary; 6.0f stays unchecked; no account, provisioning or implementation.**
A professional-review **routing package** was added 2026-10-22:
[stage-6/Q1-PROFESSIONAL-REVIEW-BRIEF.md](stage-6/Q1-PROFESSIONAL-REVIEW-BRIEF.md)
— the identity-free handoff carrying all 13 questions Q4.1–Q4.13 verbatim with
primary sources, the neutral A/B/C/D framing (Q1-D = candidate for review, **not
approved**), official ÚOOÚ + European Commission guidance (accessed 2026-10-22)
and the C-1…C-6 confirmations required before D5b can be resolved. **The brief
performs no review and changes no gate: Q1/D5b stay UNRESOLVED, C4 OPEN, 6.0f
UNCHECKED, ORD-A sequencing-only.**

- **Purpose:** Optional, secured community backend: server-validated Google auth,
  default-deny five-role authorization, revisioned submissions with immutable
  evidence, one-transaction moderation decisions with audit trail, versioned `/v1`
  API shared by future Android/Web Admin clients — under the permanent 0 CZK
  contract, with zero practice-journal data ever crossing the boundary.
- **Scope (planned, not implemented):** backend server code + database (Supabase
  Free per D4a), Android community repository/token storage (optional feature),
  export/restore tooling. **No** journal schema/backup/signing change; no Stage 7 UI;
  no bundle changes.

**Tasks — see [stage-6/CHECKLIST.md](stage-6/CHECKLIST.md) for full definitions
(6.0a–6.0f decisions/provisioning; 6.1–6.9 server core; 6.10–6.11 Android
integration; 6.12–6.16 verification gates; 6.17–6.18 handoff/closure). All
implementation tasks UNCHECKED.**

- [x] 6.0a D4/D4b — decided 2026-10-21 (Supabase Free; graceful denial; fail closed;
      strict 0 CZK).
- [ ] 6.0b D5a–D5g — partially decided; D5a details, D5b determination, D5d details
      and D5e/D5f pre-launch reviews remain open.
- [x] 6.0c D8a–D8c — decided 2026-10-21 (D8b conditional on the 6.0f proof gate).
- [ ] 6.0d Provisioning (separate owner instruction + D5c verification list).
- [ ] 6.0e Google OAuth console configuration (separately authorized).
- [ ] 6.0f D8b zero-cost BFF hosting/runtime proof (or blocker report to owner) —
      🚧 **feasibility study executed 2026-10-21 → CONDITIONAL PASS**
      ([stage-6/BFF-FEASIBILITY.md](stage-6/BFF-FEASIBILITY.md)); **C1 APPROVED
      2026-10-21** (Cloudflare Workers Free host, single origin, architecture-level
      only — no provisioning); **C2 EXECUTED 2026-10-21 (separate owner
      authorization) → CONDITIONAL PASS** — isolated local `workerd` spike on
      `spike/goldberg-0.11-c2-bff` (final commit `7bc6976`): **19/19 security,
      P1–P7 executed, kernel-measured average CPU 2.03 ms/invocation**;
      per-invocation production CPU **unverified** and the §12
      production-runtime verification remains owner-gated; spike code stays
      isolated (unmerged); **C3 DOCUMENTARY REVIEW COMPLETE 2026-10-21**
      ([stage-6/C3-EDGE-PRIVACY-REVIEW.md](stage-6/C3-EDGE-PRIVACY-REVIEW.md)):
      conditional privacy viability at 0 CZK, no compliance claim, legal/
      operator questions open (controller identity incl. minor-owner capacity
      now blocking for 6.0d; **Q1 research deliverable prepared 2026-10-21**
      ([stage-6/Q1-OPERATOR-READINESS.md](stage-6/Q1-OPERATOR-READINESS.md) —
      Options A/B/C documented, **no option selected, D5b still legally
      unresolved**; reassessed later 2026-10-21 → **options A/B/C/D with
      candidate Q1-D added, still no selection**, see the Stage 6 Q1
      reassessment note above; **owner preference recorded 2026-10-22: Q1-D
      is the PREFERRED operator model, subject to contractual eligibility +
      professional verification — a directional preference only, NOT a
      selection or resolution; D5b stays legally unresolved and blocking**).
      **Still open: C2 §12 production verification and
      C4 terms re-verification (checklist prepared; final run required before
      any provisioning).**
- [ ] 6.1–6.9 Server core: schema, auth, authorization, submissions, decision
      transaction, idempotency, read overlay, admin endpoints, abuse controls.
- [ ] 6.10–6.11 Android optional integration with strict personal-data isolation and
      graceful offline/unavailable behavior.
- [ ] 6.12–6.16 Adversarial matrix (local + separately approved integration rows),
      export/restore drill, privacy/terms published before any collection.
- [ ] 6.17–6.18 Frozen contract + Stage 7 handoff; tracking sync with evidence.

**Verification:** none executed — nothing is claimed. Planning-document consistency
checks (links, contract reuse, decision-reference synchronization) are recorded in
the Stage 6 documentation session report, not as implementation evidence.

**C6 spike addendum (2026-10-22, isolated local prototype — NOT implementation):**
an owner-authorized, local-only verification prototype ran on
`spike/goldberg-0.11-stage-6-contract` (never merged; planning baseline
`c22adb1`): `spikes/contract-c6/` simulates the `/v1` contract in-process
(synthetic identities, fake sessions, fake clock, disposable SQLite, zero npm
deps, zero network) and executed THREAT-MODEL [L] rows T5–T18/T22 including a
10-trial multi-process approval race — **79 tests / 79 pass / 0 fail** (run
`2026-10-22T06:40Z`, Node v22.23.1; first run 69/79 with 10 root-caused,
fixed and preserved failures). Evidence — stable links at the verified spike
commit, because the executable `spikes/contract-c6/` directory exists only on
`spike/goldberg-0.11-stage-6-contract` and is deliberately absent here:
[results/SUMMARY.md](https://github.com/stokonn/Goldberg/blob/d2a95d4b83c634aacb4f7f74432f2bd8980cf218/spikes/contract-c6/results/SUMMARY.md),
[results/raw/tap.txt](https://github.com/stokonn/Goldberg/blob/d2a95d4b83c634aacb4f7f74432f2bd8980cf218/spikes/contract-c6/results/raw/tap.txt),
[README (scope/blockers)](https://github.com/stokonn/Goldberg/blob/d2a95d4b83c634aacb4f7f74432f2bd8980cf218/spikes/contract-c6/README.md)
— all at commit [`d2a95d4`](https://github.com/stokonn/Goldberg/tree/d2a95d4b83c634aacb4f7f74432f2bd8980cf218/spikes/contract-c6).
This is **contract-behavior evidence only**: no
integration/production readiness claimed, HTTP/token layer covered by C2 (not
re-tested), real Postgres isolation and all [I]-venue rows NOT executed
(provisioning gate unchanged), CPU not re-measured (C2). **This addendum closes
no task: 6.1–6.18 remain unchecked and 6.0f remains UNCHECKED; D5b/Q1
unresolved, C4 open, 6.0d/6.0e not authorized, ORD-A sequencing-only.**

**C6-PG addendum (2026-10-22, local PostgreSQL verification — NOT implementation):**
under a follow-up owner instruction, the same C6 contract suites were executed
against a **real, isolated, LOCAL PostgreSQL 17.11** (rootless podman container,
host-loopback `127.0.0.1:54329` only, trust auth on loopback, default isolation
`read committed` = Supabase PostgREST's default, synthetic data only, per-test
throwaway schemas; container removed after evidence capture) on
`spike/goldberg-0.11-stage-6-contract-pg` (from `d2a95d4`; **never merged**),
commits `f7bef8e` (port) + `e37ed00` (evidence). Same server source, same
suites, two engines (`C6_ENGINE=sqlite|postgres`); the PG venue's only new
dependency is the `pg` wire client (spike branch only; SQLite venue stays
zero-dep). **Results: PostgreSQL 86/86 pass / 0 fail (v1–v10); SQLite
post-port 79/79 / 0 fail + 7 v10-only skips (no regression); static scan
PASS.** v9 race: SQLite 10/10 `[201,409]` vs PostgreSQL **10/10
`[201,500]`/`[500,201]`** — the T10 exactly-one-decision invariant does not
hold under READ COMMITTED. Findings F1–F5 (full detail in
[results/SUMMARY-PG.md](https://github.com/stokonn/Goldberg/blob/e37ed00b9205ce22e9e8ec494651e900708ee1e3/spikes/contract-c6/results/SUMMARY-PG.md)):
F1 isolation/lost-update with both demonstrated fixes (`SELECT … FOR UPDATE`;
SERIALIZABLE + required 40001→409 mapping) as probes only; F2 rollback parity
+ missing FK constraints; F3 idempotency parity + concurrent 23505→replay
requirement; F4 audit-chain parity + audit-seq race; F5 SQLite→PG SQL/type
differences (`BEGIN IMMEDIATE` absent, 42702 column qualification, `rowid`,
`?`, int8→string default). **Honest scope: a local instance is not Supabase —
the Supabase-hosted proof stays BLOCKED (D5b/Q1 + C4 + ORD-A steps 4–6); no
production code exists; this addendum closes no task: 6.1–6.18 remain
unchecked (F1–F5 are design inputs FOR task 6.1) and 6.0f remains UNCHECKED;
D5b/Q1 unresolved (Q1-D preference unchanged), C4 open, C2 §12 open,
6.0d/6.0e not authorized, ORD-A sequencing-only.**

**C4 preliminary-verification addendum (2026-10-22, documentation
reconciliation — C4 stays OPEN):** an owner-instructed docs-only recheck
re-fetched the official Cloudflare and Supabase pricing, terms, DPA, region
and backup sources and reconciled them with the 2026-10-21 snapshot; results
recorded as §F of
[stage-6/C4-PREPROVISIONING-CHECKLIST.md](stage-6/C4-PREPROVISIONING-CHECKLIST.md)
(the §A–§C tables and all statuses untouched). **Recorded:** both provider
accounts now exist (owner-side; Cloudflare corroborated by read-only MCP
checkpoint #1, Supabase from the owner's statement only — never agent-verified);
Cloudflare MCP read-only auth verified; **Workers Free remains available**;
**Supabase Free remains available** with the snapshotted limits; **`eu-central-1`
still a documented specific region**; **automatic backups "Not included" on
Free** (official `db dump` self-export guidance confirmed); **Free quota
overage can trigger service restrictions** (pause / read-only / 402);
**billing and payment-method status NOT verified; subprocessor and transfer
reviews still incomplete.** Documentation-date diff: all 2026-10-21 stamps
unchanged (CF Website Terms date and Supabase terms version badge not
re-renderable — honest gaps, remain ⚠ day-of); the **CF observability-pricing
announcement (change 2026-12-01) is unchanged**, and its target page (upd.
2026-10-15, new source) now shows **Free = 0.5 GB/day ingestion + 7-day
retention with no paid overage available on Free** — preliminary 0-CZK read
only, to be re-verified day-of and again for any access ≥ 2026-12-01. Q1-D
recorded as the owner's preferred structure, subject to eligibility
verification (preference only, not a selection). **This addendum closes no
item: §D steps 1–9 unchecked, C4 stays OPEN, 6.0f UNCHECKED, 6.1–6.18
unchecked, D5b/Q1 unresolved, C2 §12 open, 6.0d/6.0e not authorized, ORD-A
sequencing-only, 0 CZK/month.**

**C6-PG hardening addendum (2026-10-22, experimental mitigation comparison —
NOT implementation, never merged):** the F1/F3/F4 candidate fixes were
implemented **in the isolated spike only** and compared on the same local
PostgreSQL 17.11 instance (`read committed`), same branch
`spike/goldberg-0.11-stage-6-contract-pg`, commits `d8e82d5` (code) +
`c21e550` (evidence); the faithful sources and the original `results/pg/`
deviation evidence remain byte-untouched. `src/harden.mjs` implements both
strategies over the identical `runDecision` step sequence: **`for-update`**
(submission-row lock + `audit_head` append mutex) and **`serializable`** (SSI +
faithful audit pattern + bounded retry with explicit 40001/40P01/23505
handling; exhaustion → 409, never 500), sharing the F3 hardening (concurrent
idempotency 23505 → serve stored replay; 422 on mismatch). **Executed results
(v11, 37 tests, 7/7):** the repeated 20-trial multi-process race restored
`[201,409]` in **20/20 trials per strategy** (the exact invariant the faithful
server broke 10/10 in v9); **for-update: 0 SQLSTATE retries everywhere**,
including a 20-decision F4 scale run with consecutive audit seqs and a
coherent chain; **serializable: 21× `40001`** across the race and **218×
`40001`** at 20-way scale (maxAttempts=64), **0 exhausted**; deterministic F3
store race verified. Full executor: SQLite regression **93 tests / 79 pass /
0 fail / 14 PG-only skips**; PostgreSQL venue **123/123 pass / 0 fail**; static
scan PASS (23 files). **Conclusion for task 6.1: `SELECT … FOR UPDATE` is the
recommended pattern** (deterministic, zero retries, loser's 409 via the normal
guard); serializable works but needs a generous bounded retry and 40001→409
mapping; concurrent same-key decision duplicates answer `[201,409]` while
sequential ones replay — to be stated in the production API contract. **This
addendum closes no task: experimental evidence only, not a Supabase run
(Supabase-hosted proof stays BLOCKED), production implementation remains gated
on 6.0f UNCHECKED; 6.1–6.18 remain unchecked (this is design input FOR 6.1);
D5b/Q1 unresolved, C4 open, C2 §12 open, 6.0d/6.0e not authorized, ORD-A
sequencing-only, 0 CZK/month.**

**C4 owner-observation addendum (2026-10-22, attributed owner console facts —
C4 stays OPEN):** three first-hand owner observations were recorded as §F.5
(N-11–N-13) of
[stage-6/C4-PREPROVISIONING-CHECKLIST.md](stage-6/C4-PREPROVISIONING-CHECKLIST.md),
never agent-verified, with **no screenshots or account identifiers stored in
GitHub**: **Cloudflare Workers Free active with no payment method attached**
(billing non-verification N-8 unchanged); a **Supabase "Personal Free"
organization with zero projects** (no project created; the 2-project Free cap
not consumed; existing ≠ billing evidence); **Frankfurt (`eu-central-1`)
selectable in the Supabase project-creation form** (informs SB-P7/§D step 6
but does not close it). Supabase billing/payment status remains unverified;
project creation remains unperformed and unauthorized. **This addendum closes
no item: C4 OPEN (§D steps 1–9 unchecked), 6.0f UNCHECKED, 6.1–6.18
unchecked, D5b/Q1 unresolved, C2 §12 open, 6.0d/6.0e not authorized, ORD-A
sequencing-only, 0 CZK/month.**

**Stage 6 continuation (2026-10-22, documentary readiness only):** purpose,
scope, session tasks, current progress, verification and remaining blockers in
[stage-6/READINESS-CONTINUATION.md](stage-6/READINESS-CONTINUATION.md).
Approved interrupted Issue #31 description sync applied and exact readback passed
(b3edc2a, c21e550, checkpoint 6348121556); C4 PDF now parsed, TIA not retrieved,
official GDPR still unavailable. Future F1–F5 requirements prepared in
[stage-6/POSTGRESQL-DESIGN-REQUIREMENTS.md](stage-6/POSTGRESQL-DESIGN-REQUIREMENTS.md).
No new spike execution. **6.0f and 6.1–6.18 remain unchecked; Q1/D5b blocking;
C4 and C2 §12 OPEN; 6.0d/6.0e NOT AUTHORIZED; 0 CZK/month.**
[Continuation checkpoint posted](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6349066370); verified documentation commit/push
explicitly authorized in the finalization instruction. No production implementation.

**Stage 6 documentation finalization (2026-10-22):** all 12 documentation files
reviewed, five host checks re-executed PASS, source commit `6c40799` pushed to
`docs/goldberg-0.11-stage-6-readiness` with exact remote SHA verified; continuation
checkpoint 6349066370 posted. Publication evidence and the subsequently owner-approved, published and verified
Issue dashboard baseline are documented in stage-6/READINESS-CONTINUATION.md. All Stage 6
technical gates unchanged; this is documentation completion, not backend acceptance.

**Stage 6 existing-project reconciliation (2026-10-22, documentation only):**
purpose/scope: reconcile existing Supabase project in Stage 6 planning/handoff
and one C4 dashboard bullet, without cloud changes or gate acceptance. ChatGPT's
connected Supabase integration externally verified organization/project identity,
Free/tier_free, Frankfurt/eu-central-1, ACTIVE_HEALTHY, PG 17.11, 0 public tables,
0 recorded migrations and 0 Edge Functions. This coding agent did not execute
those checks; billing/payment methods and remaining settings remain UNVERIFIED.
Current evidence: [C4 §H](stage-6/C4-PREPROVISIONING-CHECKLIST.md); tasks/results:
[readiness checklist](stage-6/READINESS-CONTINUATION.md). Owner-approved C4 correction
published and exact readback passed; all 63 comments/Issue metadata/Project preserved.
Earlier zero-project observations remain dated history. Production backend NOT
IMPLEMENTED, Worker NOT DEPLOYED in verified record, Google OAuth not configured
in implementation record/hosted settings UNVERIFIED; real-user collection NOT
AUTHORIZED. **Q1/D5b, C4, C2 §12 and 6.0f OPEN; 6.0f/6.1–6.18 UNCHECKED;
6.0d/6.0e NOT AUTHORIZED; 0 CZK/month.** Documentation verification passed
(five host scripts, scope/links/attribution/unchanged-gate checks); checkpoint
prepared for authorized commit/push. No Stage 7 changes or retroactive 6.0d
authorization/completion.

**Stage 6 Security & Auth readiness audit (2026-10-22, read-only):** purpose:
assess two owner-supplied rls_auto_enable() advisor warnings in PostgreSQL event
context; scope: Stage 6 security/Auth planning and handoff only. Evidence ledger,
individual tasks/progress, official sources, conditional least-privilege proposal,
read-only Auth/RLS/API checklist and prepared catalog SELECTs are in
[stage-6/SECURITY-AUTH-READINESS-AUDIT.md](stage-6/SECURITY-AUTH-READINESS-AUDIT.md).
Source review complete; exploitability not established; hosted checklist NOT
EXECUTED. Documentary verification passed (five host checks, links/scope/original
checkbox preservation); owner-approved C4 sentence published and exact readback
verified with all comments/metadata preserved. No hosted mutation, no repeated
concurrency suite, no Stage 7 changes.
Q1/D5b, C4, C2 §12 OPEN; 6.0f/6.1–6.18 UNCHECKED; 6.0d/6.0e NOT AUTHORIZED;
0 CZK/month. No technical gate acceptance.


### External security audit continuation — 2026-10-22 (base bac81fb)

Purpose/scope: reconcile the executed, read-only A1–A3 catalog evidence in Stage 6
documentation only. ChatGPT’s connected Supabase integration executed all three
successfully; this agent records owner-relayed external results, not its own SQL.

- [x] Record function OID 17603, plpgsql/definer/postgres/event_trigger, pg_catalog search path and effective API-role EXECUTE.
- [x] Record matching ensure_rls binding/event/tags/normal enabled mode and the supplied limited API-role/privileged owner capabilities.
- [x] Record default PUBLIC EXECUTE, no identified separate API-role grants and partial structural source checks with their limits.
- [ ] Complete inherited-access/other DDL paths, full private source/dependency review and sanitized advisor IDs/version.
- [ ] Complete read-only Auth/RLS/API configuration and billing/payment evidence review.

Evidence: [SECURITY-AUTH-READINESS-AUDIT.md](stage-6/SECURITY-AUTH-READINESS-AUDIT.md)
§1/§3/§7. Both warnings remain; no exploit demonstrated/repaired, no hosted write
or function/RPC invocation. A1–A3 are complete externally; the overall hosted audit
is INCOMPLETE. Existing five host checks and documentation consistency checks PASS
for this continuation (details in docs/ai/VERIFICATION.md); no concurrency rerun.
Q1/D5b unresolved/blocking; C4 and C2 §12 OPEN; 6.0f/6.1–6.18 UNCHECKED;
6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing only; 0 CZK/month; collection NOT
AUTHORIZED. Keep ensure_rls; any permissions change requires separate authorization
and compatibility review. No Stage 7 or application changes; no gate acceptance.


### Additional external security inventories — 2026-10-22 (base 9fe83db)

Purpose/scope: reconcile four executed catalog-only findings in Stage 6 audit and
handoff documentation. ChatGPT executed these against the existing Supabase
project; the owner relayed the results. This agent did not execute hosted queries.

- [x] Record public function inventory: only rls_auto_enable().
- [x] Record direct anon/authenticated memberships: none returned.
- [x] Record function dependency count: 1, without assuming identity/type/direction.
- [x] Record public tables/views/materialized views/foreign tables: zero objects.
- [ ] Finish full private source/dependency and remaining cross-schema/DDL-path review; obtain sanitized advisor IDs/version.
- [ ] Finish read-only Auth/API/RLS configuration and billing/payment/C4 evidence review.

Progress/evidence: [security audit §1/§3/§8](stage-6/SECURITY-AUTH-READINESS-AUDIT.md).
Only these scoped inventory portions are complete; broader hosted audit remains
INCOMPLETE. Neither warning is resolved; no vulnerability demonstrated/repaired,
no database writes or application-function invocation. Do not repeat A1–A3 or the
four completed inventories. Issue #31’s concise broader status remains accurate;
no edit/comment is needed. Documentation verification is recorded in docs/ai/VERIFICATION.md.

Q1/D5b unresolved/blocking; C4 and C2 §12 OPEN; 6.0f/6.1–6.18 UNCHECKED;
6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing only; 0 CZK/month; collection NOT
AUTHORIZED. Keep ensure_rls; future permissions changes require separate approval
and compatibility review. No Stage 7 or application changes; no gate acceptance.


### Live security evidence / bounded readiness — 2026-10-22 (base 31e0539)

Purpose/scope: reconcile five new external ChatGPT/Supabase read-only findings in
Stage 6 documentation; narrow remaining work to settings evidence and approval
choices, without continuing open-ended catalog exploration.

- [x] Record normal ensure_rls → rls_auto_enable dependency and function language/schema dependencies.
- [x] Record CREATE=false for anon/authenticated across all eight supplied schemas.
- [x] Record private deployed-source retrieval and structural checks, including exception handling, with safety limits.
- [x] Record ensure_rls plus six reported Supabase-related system triggers, unchanged.
- [x] Record both exact advisor names at WARN, unresolved after external rerun.
- [x] Check accessible integration tools; no callable Supabase tools here, Auth/API settings UNVERIFIED.
- [ ] Complete one sanitized Auth/API settings pass where access permits; retain inaccessible fields as limitations for scoped owner review.

[Audit §1/§3/§9](stage-6/SECURITY-AUTH-READINESS-AUDIT.md) records exact provenance,
remaining settings and prioritized readiness verdict. Catalog reconciliation of
this snapshot is complete; the broader hosted audit/Stage 6 are NOT accepted.
No completed A1–A3/dependency/schema/inventory queries or concurrency tests rerun.
Documentation verification results are in docs/ai/VERIFICATION.md.

Q1/D5b unresolved/blocking; C4/C2 §12 OPEN; 6.0f/6.1–6.18 UNCHECKED;
6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing only; 0 CZK/month; collection NOT
AUTHORIZED. Prioritize owner/professional Q1, C4 evidence, separately authorized
C2 §12/6.0f acceptance, then explicit implementation authorization. Both warnings
remain; preserve ensure_rls. No hosted/production/Stage 7 changes. Issue #31’s
concise broader status remains accurate and unchanged; no edit/comment proposed.


### Owner-authorized local schema prototype — 2026-10-22

Purpose: prove physical PostgreSQL 17 schema/migration/integrity requirements using
synthetic data on a separately named spike. Scope: spikes/stage6-schema-pg and
Stage 6 checkpoints only; no production implementation or adoption into 6.1.

- [x] Create isolated spike/goldberg-0.11-stage-6-schema-pg from verified remote 4d01489.
- [x] Implement physical DDL, FK/unique/check graph, immutable histories, audit_head, idempotency and repeatable atomic migrations.
- [x] Execute disposable PostgreSQL 17.11 targeted suite: 34/34 PASS, 0 failures/skips.
- [x] Run five existing host scripts: PASS; preserve failed intermediate evidence and original PG comparison.
- [x] Flag API revision-eligible statuses versus server transitions; no approved contract changed.
- [x] Publish implementation 16bfdfb/correction f91a4ce and [Issue checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6351050432); exact readback PASS.

[Prototype checkpoint](stage-6/LOCAL-SCHEMA-PROTOTYPE.md) links source, commands, evidence
and limitations. Verification includes empty/seeded/idempotent migrations, FKs,
immutable mutation/truncation, transactional rollback, new target-contention and
idempotency regressions, audit recomputation and int8 boundaries. No benchmark
rerun; no hosted RLS/Auth/API, full restore/RPO, app build/device or release proof.

This local milestone can later supply reviewed 6.1 inputs; it does not pass or
change 6.0f. Formal 6.1–6.18 remain UNCHECKED; Q1/D5b unresolved/blocking, C4/C2 §12
OPEN; 6.0d/6.0e NOT AUTHORIZED; 0 CZK/month; collection NOT AUTHORIZED.
Exact next step: review candidate schema, resolve status-contract ambiguity with
owner, then clear gates/obtain separate formal implementation authorization.
Stage 7A conditional/X10 OPEN and Stage 7B untouched. No merge/deploy/release.


Publication: implementation commit 16bfdfbe213ce66973a55d9746453601605e74d7;
follow-up f91a4ced846ad53a571c744c6c588f245fe3a582 corrects whitespace in saved
failed-run diagnostics and records the check failure. Tested source hashes and
34/34 results are unchanged; full baseline-to-current diff check PASS. Existing
Issue #31 checkpoint 6351050432 independently read back exactly; all 63 older
comments preserved, 64 total, Project In Progress unchanged. Later publication
metadata/checkpoint commits do not change tested prototype source.


### Reviewed prototype / formal-local adoption preparation — 2026-10-22

Purpose/scope: record owner-selected revision states, inspect dependencies and
prepare an exact local 6.1–6.3 scope/policy proposal reusing the tested prototype.
Documentation/proposals only; formal implementation remains NOT STARTED.

- [x] Publish only the owner-approved Stage 6 dashboard row and checkpoint link; exact body/metadata/comments readback PASS.
- [x] Record owner direction: draft local-only, changes_requested canonical, returned UI label only.
- [x] Inspect app, Stage 7A, prototype and old C6 dependencies; no approved API edit.
- [x] Prepare exact adoption scope/map, API patch and narrow gate-exception text for review.
- [ ] Owner approves the reviewed API patch and local-only formal 6.1–6.3 policy exception.
- [ ] After that authorization: perform extraction/adoption and targeted local Auth/AuthZ implementation/tests.

[LOCAL-CORE-ADOPTION.md](stage-6/LOCAL-CORE-ADOPTION.md) holds concrete scope, compatibility
impact, retained 34/34 evidence and acceptance requirements. No tests claimed rerun
for unchanged prototype source. Local-only exception is proposed, NOT in force;
all original technical task/gate boxes unchanged. Verification: five host scripts,
source hashes, proposal applicability and links/scope/gate checks recorded in docs/ai/VERIFICATION.md.
Q1/D5b unresolved/blocking; C4/C2 §12 OPEN; 6.0f/6.1–6.18 UNCHECKED;
6.0d/6.0e NOT AUTHORIZED; 0 CZK/month; collection NOT AUTHORIZED. Stage 7A/X10
and Stage 7B preserved. No cloud, production code, API change, merge or release.


### Local Auth/session/authorization prototype — 2026-10-22

Purpose/scope: owner-authorized isolated LOCAL-ONLY prototype for 6.2/6.3 designs,
reusing the verified PostgreSQL schema. New spikes/stage6-auth-local code, additive
Auth migration, tests and Stage 6 checkpoints; no formal promotion or cloud work.

- [x] Create spike/goldberg-0.11-stage-6-auth-local from verified schema remote 0f966dc.
- [x] Implement real JWT/JWKS validation, issuer/subject identity, hashed opaque sessions, nonce/replay/rotation/family revocation and safe logout/error/log handling.
- [x] Implement fresh server role/state/capability/ownership checks, no-self-grant and actor≠author with a local in-process adapter.
- [x] Execute 50 Auth/security tests plus unchanged 34-schema regression suite: 84 PASS, 0 failures/skips/cancellations.
- [x] Run five repository host scripts: PASS; original schema/evidence/approved contracts and gate boxes preserved.
- [x] Commit/push source 76c1c87 and append [Issue checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6351715021); show latest-link-only dashboard proposal for approval.

[Local Auth checkpoint](stage-6/LOCAL-AUTH-PROTOTYPE.md) links exact source/tests/commands
and partial T1–T9/T22 coverage. Formal 6.2/6.3 and all global task boxes remain
UNCHECKED; no hosted/real-provider/HTTP/RLS/CSRF or complete threat-row pass inferred.
No original concurrency benchmark/provider audit rerun. Remaining work: separately
authorized local 6.4–6.6 with bounded typed handlers, authenticated moderation and
idempotency; canonical revision contract patch awaits review before endpoint adoption.
Q1/D5b unresolved/blocking; C4/C2 §12 OPEN; 6.0f/6.1–6.18 UNCHECKED;
6.0d/6.0e NOT AUTHORIZED; 0 CZK/month; collection NOT AUTHORIZED. No Stage 5/7,
cloud, real credentials/users, APK, deployment, merge, tag or release changes.


Publication: tested source 76c1c879b5642236490e1f14b652fdf5e5ebdc7a pushed to the
separate Auth branch. Checkpoint 6351715021 independently read back exactly; all
64 prior full comments preserved, 65 total, Project In Progress unchanged. Dashboard
link-only proposal is prepared/shown; UNPUBLISHED pending explicit approval.
Source hashes and final 50+34 test artifacts unchanged by publication bookkeeping.

## Stage 7 — Encyclopedia UX and Android/Web Admin clients

**Status: COMPLETE within the Android client/synthetic acceptance scope.** Stage 7A is already owner-accepted; Stage 7B is **ACCEPTED WITH DOCUMENTED LIMITATIONS** by explicit owner decision on October24,2026. Scope/tasks/results/limitations are recorded in the current acceptance entry at the top of this checklist and the Stage7B final owner report. Android/backend live integration remains OPEN; this client acceptance does not complete Stage6 or Stage8 or authorize production.

<details>
<summary>Historical Stage 7A refinement and acceptance checkpoints — later owner acceptance supersedes pending wording</summary>

### Stage 7A — six-screen Encyclopedia UX redesign (owner-authorized 2026-10-22)

**Purpose:** redesign Discover, Library, Artist Profile, Work Profile, Home and User Profile
to the owner's approved editorial direction while preserving every existing behaviour and
all user data. **Scope:** `app/src/main` UI + localized strings only (+ one committed
instrumentation helper); NO database/schema/migration/backup changes, NO navigation changes,
NO Stage 6/7B changes. Branch `feature/goldberg-0.11-stage-7a`, worktree
`/home/stokonn/Projects/Goldberg-Stage7A`, base `32cf5d9`. Living checklist & progress log:
[stage-7/stage-7a/CHECKPOINT.md](stage-7/stage-7a/CHECKPOINT.md).

- **Tasks/progress — COMPLETE (implementation + verification):** 7A.1 strings EN+CZ
  (16 `stage7a_*` keys incl. owner-approved privacy wording + `label_library`;
  939-resource parity PASS) · 7A.2 shared editorial components · 7A.3–7A.6 the four
  Encyclopedia screens redesigned · 7A.7–7A.8 Home/Profile adjustments · 7A.9 preservation
  by inspection (repertoire/UUIDs/notes/badges/recovery/backup untouched) · 7A.10 host
  gates green · 7A.11 emulator screenshots · 7A.12 mismatch report · 7A.13 commit
  `c106c5c` pushed to `origin/feature/goldberg-0.11-stage-7a` + Issue #31 checkpoint —
  **all checklist items done.**
- **Verification — executed 2026-10-22 (real results):**
  - Host gates: `./gradlew clean assembleDebug testDebugUnitTest assembleDebugAndroidTest
    lintDebug --no-daemon --max-workers=1` → **BUILD SUCCESSFUL EXIT=0**; JVM
    **174 run / 0 failures / 0 errors / 0 skipped**; lint **0 errors, 122 warnings**
    (pre-existing); `assembleDebugAndroidTest` compiles; all five `scripts/check_*.py`
    **PASS** (939-resource EN/CZ parity, migrations, backup fixtures, history paging,
    agent policy). Lint/test logs: `/tmp/goldberg-7a-host-gates.log` (session-local).
  - Instrumentation on disposable emulator `emulator-5554` (API 36, `ro.kernel.qemu=1`
    asserted; never a physical device): new `Stage7aCatalogueBootstrapTest` **OK (1 test)**
    — drives the app's own `CatalogRepository.featured()`/`composerWorks()` (the same
    product code `CatalogScreen`/`ComposerScreen` call) and thereby populated **23 real
    OpenOpus composers + 597 real Vivaldi works** with source attribution; no invented or
    hand-written catalogue rows anywhere. This helper exists because on a fresh install
    the legacy `catalog` route has no production entry (pre-existing Stage 4-era
    finding, recorded only — no navigation change made).
  - **Screenshots (X6/X2/X3/X4): 32 captures** — Home, Library, Library-Artists, Discover,
    Profile, Artist Profile, Work Profile × Ivory/Nocturne × EN/CS + 1.5× font-scale spot
    check — in [stage-7/stage-7a/results/](stage-7/stage-7a/results/) with sha256
    manifest and step log; 9 directly inspected (all six screens ivory-en +
    nocturne-cs-library + font15-en-home), all rendering the intended design with real
    data, correct theme/locale/font tokens and no clipping.
  - **Comparison (X7):** [stage-7/stage-7a/results/README.md](stage-7/stage-7a/results/README.md)
    documents every difference vs the four approved Stage 4 references and the 0.10/0.10.1
    Home/Profile approvals — all deliberate and plan/owner-approved (privacy wording
    without "our servers"; chips All·My Repertoire·Artists; no fake download button; no
    Albums/Recordings sections; honest "no verified movement list"; hero-portrait fallback
    verified pre-existing, not a 7A regression).
  - X8 seeded upgrade check: **not executed** — Stage 7A contains no schema/database/
    migration change (inspection-proven); Stage 5 upgrade evidence remains authoritative.
- **Completion status:** Stage 7A implementation and its verification are complete; the
  owner recorded **conditional acceptance (X9, 2026-10-22)** — see the Stage 7A
  conditional-acceptance entry below. The stage is **NOT unconditionally complete**: the
  text-alignment issue stays **OPEN (gate X10)** pending a real-device regression check
  before the 0.11 release. No merge, tag, release or APK publication is authorized or
  performed.

### Stage 7A refinement pass — seven corrections + spacing audit (owner-authorized 2026-10-22)

**Purpose:** targeted polish on top of the approved redesign: seven owner-specified
corrections (online toggle → Settings; artist hero overlap; Library catalogue separation;
Discover empty-state contradiction; Work Profile placeholder + empty sections; biography
language fallback; privacy-safe first-install catalogue reachability) plus a systematic
JournalSpace-token spacing audit across all six screens. **Scope:** same UI/strings scope
as 7A (plus one new `preferences/CatalogPreferences.kt` file and
`ComposerPortrait` optional parameters); NO schema/database/backup/navigation changes,
NO Stage 6/7B changes. Living plan/state log:
[stage-7/stage-7a/REFINEMENT-CHECKPOINT.md](stage-7/stage-7a/REFINEMENT-CHECKPOINT.md).

- **Tasks/progress — COMPLETE (implementation + verification), submitted for X9:**
  persisted default-OFF `CatalogPreferences` + Settings toggle row; Discover explicit
  "Search online" button (never networks on open/restore; plain-`remember` request state)
  + rewritten privacy wording; overlap-proof artist hero (growing image hero with bottom
  scrim / Row fallback); Library "From the catalogue"/"Catalogue artists" separation
  headers + explicit `startBrowsing()` bootstrap button for fresh installs; Work header
  MusicNote art + soft Info empty-section panel + merged button/caption group; CZ/EN
  bio-language fallback label; token spacing pass on all six screens; 12 new EN/CZ string
  pairs; `DiscoverNavigationTest` updated to the new contract.
- **Verification — executed 2026-10-22 (real results):**
  - Host gates: assembleDebug + testDebugUnitTest + lintDebug **BUILD SUCCESSFUL**;
    JVM **174/0F/0E/0S**; lint **0 errors, 124 warnings** (+2 stylistic, documented:
    UseKtx in the new `CatalogPreferences` matching the existing `MediaPreferences`
    pattern; ModifierParameter on `ComposerPortrait`); `assembleDebugAndroidTest`
    **compiles**; all five `scripts/check_*.py` **PASS** (EN/CZ parity intact).
  - **Seeded in-place upgrade (X8, now executed for 7A):** `adb install -r` over the
    seeded `emulator-5554` AVD — all **22 table counts + profile_settings row
    (`Musician|System`) identical** before/after; online-catalogue preference defaults
    OFF (file absent until first write; read back `true`/`false` correctly on toggle).
  - **Screenshots: 48 fresh captures** (4 theme×language combos incl. Library
    separation/artists/artist/work, Settings toggle flow EN+CS, 2× font EN+CS,
    720×1560 narrow sweep) with sha256 manifest; hero no-overlap, Work MusicNote art,
    Library headers, Discover hint/empty-state and CZ bio label visually inspected.
  - Spacing/mismatch documentation: [stage-7/stage-7a/results/README.md](stage-7/stage-7a/results/README.md)
    (refinement section); acceptance matrix X2–X8 updated with refinement evidence.
- **Completion status:** refinement implementation and verification are complete and
  **submitted for owner final acceptance (X9, pending)** — Stage 7A remains NOT complete
  until then. No merge, tag, release or APK publication is authorized or performed.

### Stage 7A refinement round 2 — final accessibility + spacing fixes (owner-authorized 2026-10-22)

**Purpose:** fix the three remaining issues found in independent screenshot review of the
refinement pass: (1) 2× font mid-word wrapping in Artist Profile tabs and Work Profile
metadata cells (plus a review of the five-tab bottom navigation labels and accessibility
descriptions), (2) Settings → Data & Storage spacing (headers/toggles/descriptions/cards
no longer touching), (3) repetitive privacy/offline explanations on the Discover landing
(one concise notice; contextual online hint; technical network status moved out of the
landing). **Scope:** 5 Kotlin UI files + EN/CZ strings (+64/−28); NO schema/database/
backup/navigation/provider-behavior changes; JournalSpace tokens only; Stage 6/7B
untouched. Living plan/state log:
[stage-7/stage-7a/REFINEMENT-CHECKPOINT.md](stage-7/stage-7a/REFINEMENT-CHECKPOINT.md)
(round-2 section).

- **Tasks/progress — COMPLETE (implementation + verification), submitted for X9:**
  Artist tabs `TabRow` → `ScrollableTabRow` (intrinsic widths — labels never break
  mid-word; scrollable at 2×/narrow); Work metadata cells → adaptive
  `BoxWithConstraints` stacking (stacked Column + dividers when narrow or font >
  1.15×, same precedent as Settings › About; compact row retained at 1×); bottom-nav
  labels gain explicit full-name `semantics { contentDescription }`; Settings →
  Data & Storage first item → `Column(spacedBy(JournalSpace.medium))` (preference +
  every warning preserved verbatim); Discover landing keeps one concise third-party
  disclosure notice (shortened EN+CZ), online-off hint only while searching, technical
  network status only while searching with the catalogue pref ON.
- **Verification — executed 2026-10-22 (real results):**
  - Host gates: assembleDebug + testDebugUnitTest + assembleDebugAndroidTest + lintDebug
    **BUILD SUCCESSFUL**; JVM **174/0F/0E/0S**; lint **0 errors, 124 warnings**
    (unchanged from the round-1 baseline); all five `scripts/check_*.py` **PASS**
    (EN/CZ parity 950 intact).
  - **Seeded in-place upgrade re-PASSED:** `adb install -r` over the seeded
    `emulator-5554` AVD — all **22 table counts + profile_settings row identical**;
    online-catalogue preference read back `true`→`false` on-device and restored OFF.
  - **Screenshots: 47 targeted `r2-*` captures** (1×/1.5×/2×, EN/CS, Ivory/Nocturne,
    720×1560 narrow; incl. pref-ON search flow and navcheck) with SHA-256 manifest +
    log; every changed surface visually inspected; Discover landing notice/hint/network
    status presence+absence asserted via UIAutomator; bottom-nav full-name `content-desc`
    verified at 2× (`NAV-EVIDENCE` log lines). Emulator left tidy.
  - Documentation: round-2 sections in
    [stage-7/stage-7a/results/README.md](stage-7/stage-7a/results/README.md) (mismatch
    + honest limitations) and the Stage 7A checkpoint log (7A.15).
- **Completion status:** round-2 implementation and verification are complete and
  **submitted for owner final acceptance (X9, pending)** — Stage 7A remains NOT complete
  until then. No merge, tag, release or APK publication is authorized or performed.
  Known honest limitations: bottom-nav labels ellipsize visually at 2× (full names
  carried by verified accessibility semantics); at 2× the third artist tab is edge-cut
  by the scrollable row (swipe to reach; never breaks mid-word).

### Stage 7A refinement round 3 — vertical alignment audit + Discover privacy popover (owner-reported 2026-10-22, commit `d7c0aef`)

**Purpose:** fix two defects the owner found testing the round-2 APK on a physical
Samsung device: (1) text in Settings rows, Discover result cards, Profile cards and other
shared components reads top-biased — audit the real Compose layout constraints
(`verticalAlignment`/`contentAlignment`, line height, font padding, asymmetric modifiers)
and fix the root cause in shared components (no blind top padding, no clipping or
accessibility breakage); (2) replace the large persistent Discover privacy card with a
small accessible lock icon in the header's upper-right opening a compact anchored
popover (browser site-information style) with the same truthful disclosure, dismissing
on outside tap and Back. **Scope:** 5 Kotlin/resource files (+35/−16) — shared typography
(`Theme.kt`), `DiscoverScreen.kt`, `CustomizePanel.kt`, EN/CZ strings (parity 951); NO
schema/database/backup/navigation/provider-behavior/network changes; online-results
setting stays in Settings → Data & Storage default OFF; Stage 6/7B untouched. Living
plan/state log: [stage-7/stage-7a/REFINEMENT-CHECKPOINT.md](stage-7/stage-7a/REFINEMENT-CHECKPOINT.md)
(round-3 section).

- **Tasks/progress — COMPLETE (implementation + verification), submitted for X9:**
  · **Alignment audit:** every shared Row verified `CenterVertically` per call site plus
  a scripted sweep of all `Row(` bodies/arguments in `ui/*.kt`; root cause found in
  `journalTypography()` — the shared `text()` builder lacked
  `PlatformTextStyle(includeFontPadding = false)` (M3 default the app bypassed), so
  legacy font padding inflated text boxes with asymmetric leading → fixed in the one
  shared builder (app-wide, no padding hacks, no clipping). Sweep found exactly one more
  real defect: `CustomizePanel` "♫" preview glyph row → `CenterVertically` (+ missing
  `Alignment` import). Decorative top-aligned quote/info icons intentionally kept.
  · **Discover privacy:** persistent landing card removed; header Row = title/subtitle
  column + lock `IconButton` (accessible label `stage7a_privacy_info` EN/CZ) opening an
  anchored `DropdownMenu` popover with the unchanged truthful disclosure (no
  HTTPS/security claim); `privacyInfo` state hoisted; dismisses on outside tap and Back;
  no network/session logic touched.
- **Verification — executed 2026-10-22 (real results):**
  - Host gates: assembleDebug + testDebugUnitTest + assembleDebugAndroidTest + lintDebug
    **BUILD SUCCESSFUL**; JVM **174/0F/0E/0S**; lint **0 errors, 124 warnings**
    (unchanged — `includeFontPadding` produced no new warnings); all five
    `scripts/check_*.py` **PASS** (EN/CZ parity 951 with the new label pair); final
    `assembleDebug` re-ran after a comment-only edit (source/artifact identity, 39s).
  - **Seeded in-place upgrade re-PASSED:** `adb install -r` over the seeded
    `emulator-5554` AVD — all table counts + `profile_settings` row unchanged.
  - **Screenshots: 51 targeted `r3-*` captures** (4 combos × all six screens at 1×,
    Data & Storage EN/CS, 1.5×/2× EN Ivory, 2× CS Nocturne, 720×1560 narrow, targeted
    Appearance & Customize previews) with SHA-256 manifest + log; **privacy popover
    cycle (open / outside-tap / Back) verified ×8** via UIAutomator in every
    combo/scale; landing-card absence asserted per context; alignment surfaces
    visually inspected in both languages, both themes and all scales — no clipping at
    2×. Capture-automation incidents (popup-only uiautomator dump on API 36, narrow
    `left=0` popup bounds, startup `wm size` reset) documented honestly in the
    round-3 checkpoint. Emulator left tidy.
  - Documentation: round-3 sections in
    [stage-7/stage-7a/results/README.md](stage-7/stage-7a/results/README.md) (issue
    table + honest limitations/incidents) and the Stage 7A checkpoint log (7A.16).
- **Completion status:** round-3 implementation and verification are complete and were
  **submitted for owner final acceptance (X9)** — resolved 2026-10-22 with **🟧
  CONDITIONAL owner acceptance** (see the conditional-acceptance entry below): the owner
  reports text alignment **still inconsistent on their physical Samsung device despite
  round 3**, so the issue stays **OPEN (gate X10)** and Stage 7A is **not
  unconditionally complete**. No merge, tag, release or APK publication is authorized or
  performed.
  Known honest limitations: round-2 limitations carry over (bottom-nav visual ellipsis
  at 2× with verified full-name semantics; third artist tab edge-cut in the 2×
  scrollable row; category tiles ellipsize at 2× — pre-existing); the 2× CZ popover
  wraps tall but stays anchored and its dismissal cycle is verified per scale.

### Stage 7A conditional owner acceptance (2026-10-22) — X9 🟧 CONDITIONAL, one issue OPEN

**Purpose:** record the owner's Stage 7A acceptance decision and its explicitly
unresolved reservation. **Scope:** documentation-only update — this checklist, both
Stage 7A checkpoints, [stage-7/UI-ACCEPTANCE-MATRIX.md](stage-7/UI-ACCEPTANCE-MATRIX.md)
and Issue #31. **No further UI changes were made or are authorized in this pass**; all
round-1–3 code and verification evidence are preserved unchanged (no build/test/lint
re-run for a documentation-only change — not applicable; prior green results remain the
standing evidence).

- **Decision (owner, 2026-10-22):** Stage 7A (X9) is **🟧 CONDITIONALLY ACCEPTED** —
  the owner accepts the six-screen redesign and all three refinement rounds **with one
  explicitly unresolved issue**. This is not unconditional sign-off, and no
  "Stage 7 complete" claim follows from it.
- **OPEN ISSUE — text alignment (NOT fixed):** despite round 3 (shared-typography
  `includeFontPadding = false` root-cause fix in `Theme.kt` + `CustomizePanel` row
  centering; all host/emulator gates green; 51 `r3-*` screenshots inspected),
  **text alignment still appears inconsistent on the owner's physical Samsung
  device.** The issue is recorded **OPEN**, not fixed: emulator renders cannot
  reproduce or close a real-device rendering difference, and round-3 evidence must not
  be mistaken for real-device confirmation.
- **REQUIRED BEFORE THE 0.11 RELEASE: a real-device regression check of text
  alignment** on the owner's physical Samsung device — tracked as gate **X10** in
  [stage-7/UI-ACCEPTANCE-MATRIX.md](stage-7/UI-ACCEPTANCE-MATRIX.md) and in Issue #31;
  the check is owner-performed (agents never test the owner's physical phone). The
  0.11 release must not proceed while X10 is open.
- **Completion status:** Stage 7A = **🟧 conditionally accepted (X9)** with the
  text-alignment issue **OPEN (X10)**; Stage 7A is not treated as unconditionally
  complete and "Stage 7 complete" is still not claimed (Stage 7B not started). No
  merge, no tag, no release, no APK publication; Stage 6 and Stage 7B untouched.

---

</details>

## Stage 8 — Integration, legal review and release readiness

**Status: PENDING.** Stage7 Android client/synthetic acceptance is complete. Stage6 G1–G9/G11, C4, 6.0f, Android/backend live integration and all production authorization gates remain OPEN; Stage8 requires its own authorized scope and verification.

- [ ] Explicit release-readiness assessment of duplicate Settings TalkBack focus stops — deferred, NOT FIXED.
- [ ] Explicit release-readiness assessment of large-font bottom-tab truncation — deferred, NOT FIXED.
- [ ] Explicit release-readiness assessment of scaffold contrast findings — deferred, NOT FIXED.
- [ ] Resolve or explicitly disposition unverified speech, motion, accessibility coverage and real three-hour/reboot notification delivery with actual evidence; no pass is inferred.
- [ ] Complete authorized integration and release gates, with separate permission before any main merge, production activation or publication.

---

created by stokonn with love <3

## Stage 6 local handlers milestone — 6.4–6.6 (2026-10-23)

Purpose: implement bounded submissions, atomic authorized moderation and request-bound
idempotency on the verified PostgreSQL/Auth prototypes, using synthetic data only.
Scope: spikes/stage6-handlers-local, reviewed revision-status contract clarification,
and Stage 6 tracking. Original 34 schema/50 Auth source and historical evidence remain intact.

- [x] Verify remote Auth HEAD 7993444 and create isolated implementation branch.
- [x] Read latest Issue #31 checkpoint and publish approved link-only dashboard update.
- [x] Review exact state patch/dependencies; reconcile changes_requested canonical state.
- [x] Implement separate validation, submission, moderation, idempotency and adapter modules.
- [x] Execute focused PG17 security/concurrency/rollback tests plus all 84 regressions.
- [x] Verify source hashes, host checks and scope; push c80c03e and synchronize checkpoint #6357457691.

Progress: local code/verification/publication complete; formal acceptance pending.
Verification: PostgreSQL 17.11 — 87 new + 50 Auth + 34 schema = 171 PASS, zero
failures/skips/cancellations; five host checks and original source hashes PASS.
Initial 77/81 and corrected 81/81 artifacts retained, including diagnosed export
ordering bug and fixture correction. No hosted/HTTP/DNS/Android tests executed.
[Purpose, scope, limits and tasks](stage-6/LOCAL-HANDLERS-PROTOTYPE.md).
Completion: local milestone VERIFIED AND PUBLISHED (c80c03e, Issue checkpoint
6357457691 independently read back, 65 older comments preserved). Formal
6.4–6.6 and 6.0f remain unchecked; Q1/D5b, C4 and C2 §12 unresolved.

## Local 6.7–6.9 candidate — PARTIAL, 2026-10-23

Purpose/scope: public overlay/feed, remaining Admin operations and PostgreSQL abuse
protection under spikes/stage6-public-admin-abuse; two narrow existing handler
extensions and additive local migration 004. Branch
implement/goldberg-0.11-stage-6-7-9-local; isolated worktree
/tmp/goldberg-stage6-public-admin-abuse; live GitHub-verified base
262847ebcdef18d186ded5b7af4825436a1dc4e3, direct child of c80c03e.
Source checkpoint is its containing implementation commit; publication follows.

- [x] Read canonical instructions, latest Issue #31 body/66 comments/checkpoint,
  verify live remote ancestry and isolate work without changing earlier branches.
- [x] Add candidate public, Admin and quota services; reuse existing core and
  role/suspension functions; preserve original 171 test files and historical evidence.
- [x] Execute 30 unit checks PASS, five repository checks PASS; single-user PG17.11
  migration SQL and scoped history/feed/quota/rollback smoke assertions PASS.
- [ ] Execute 33 new PostgreSQL integration tests and original 171 regressions.
  Actual attempts: 33 + 87/50/34 hook failures, 0 bodies executed (connect EPERM).
  Separate attempts on the unchanged original worktree fail identically.
- [ ] Verify concurrency, Admin transaction boundaries, end-to-end authorization,
  idempotency, quota windows and all integration assertions in a socket venue.
- [ ] Finish independently scheduled coarse-IP expiry cleanup/retention acceptance.
- [ ] Publish source branch and append a verified partial checkpoint to Issue #31.

Podman fails on read-only /run/user/1000/libpod. A disposable PostgreSQL17.11 cluster
initialized from cached local binaries; TCP startup is denied by the sandbox.
Single-user SQL execution succeeds but cannot prove JavaScript service or concurrent
behavior. Full source hashes, commands, failed attempts and exact threat limitations:
spikes/stage6-public-admin-abuse/results/SUMMARY.md. Original 32-source manifest:
30 unchanged; only adapter and publication revision allocation intentionally changed.
No old hash/log overwritten. Local retention currently prunes only during fresh
intake; real source/field/license policy stays unresolved/default-deny. No fetcher.
T14/T16/T17/T18/T20/T22 subchecks only; no whole threat row acceptance.

Formal 6.1–6.18/6.0f unchanged; Q1/D5b/C4/C2 §12 open. No hosted, real-user,
Android/Stage7/Stage5, main merge/tag/APK/release action. Not ready for 6.10–6.11.
Next: obtain an allowed local PostgreSQL17 socket/container venue, execute
run-local.sh and fix/verify actual results, finish retention, then acceptance.
No Issue dashboard diff proposed; description and prior comments remain untouched.

## Follow-up — network diagnosis and retention candidate (2026-10-23)

PARTIAL. Local implementation checkpoint remains
`defbf346904c9aa082a4afc8cac625ce8ea8b923` on
`implement/goldberg-0.11-stage-6-7-9-local`; no new commit or successful push.
Live GitHub matching-refs confirms implementation branch absent; commit lookup
returns HTTP422 No commit found; handler base remains 262847e. Exact-SHA ordinary
push retry fails before authentication. Local IPv4 socket creation fails EPERM;
github.com DNS returns EAI_AGAIN. No force-push or existing work overwrite.

Issue #31 comment NOT POSTED: connector403 Resource not accessible by integration;
CLI cannot connect. Fresh independent reads confirm body and all 66 comments
unchanged. Exact pending comment and publication diagnostics are retained in
spikes/stage6-public-admin-abuse/results/publication-attempts.json. No dashboard diff.

Independent retention scheduler/prune follow-up exists in the working tree:
32 unit tests PASS; PostgreSQL17.11 single-user retention smoke PASS. It runs
independently of intake with explicit lifecycle, suppresses overlapping sweeps,
drains on stop and retries storage faults without private diagnostic content.
SQL checks preserve active/principal counters and submission/audit/publication
history. Compose exposes janitor.start()/stop() for the future local runtime;
no daemon, HTTP listener, hosted scheduler or infrastructure was started.
**Follow-up is UNCOMMITTED** because its PostgreSQL transaction wiring is not
verified, respecting the owner's instruction to commit/push only verified work.

Latest new integration attempt: **34 hook failures, 0 test bodies executed**,
connection EPERM. Original 87/50/34 regression attempts remain setup failures, not
passes; prior 171 PASS is historical only. No code failure conclusion can be drawn
from blocked setup. Five host checks rerun PASS; current source hashes preserved
separately in retention-candidate-sha256.json, without overwriting earlier manifests.

Unfinished 6.7: execute public/private, quarantine, supersession/rollback, equal-time,
concurrent pagination/highwater/cursor and BIGINT integration assertions.
Unfinished 6.8: execute capability/revocation/suspension/role wrapper, reason/conflict,
Admin races/idempotency, transaction-boundary faults and audit/history assertions.
Unfinished 6.9: execute PostgreSQL quota/pending/IP last-slot races, clock windows,
idempotency/fault/kill-switch/Retry-After and independent cleanup integration.
All 171 original regression bodies must run successfully in an allowed PG17 venue.
Full HTTP/browser/hosted/Android threat acceptance unexecuted; no full threat row PASS.
Formal gates unchanged; no cloud/Supabase/Stage5/7 modification. Not ready for 6.10–6.11.
Next: restore permitted local PG17 network/container execution, run run-local.sh,
fix any actual assertion failures, then commit verified follow-up and safely publish.

## Recovery attempt — permitted host execution unavailable (2026-10-23)

PARTIAL, NOT COMPLETE. No new commit/push; local HEAD remains
`defbf346904c9aa082a4afc8cac625ce8ea8b923` on
`implement/goldberg-0.11-stage-6-7-9-local`.
Preserved all existing staged/unstaged/untracked work, original evidence and 171-test
baseline. Before changes, saved tracked.patch, working-files.tar.gz and per-file
hashes for all 24 modified/untracked files under
`/tmp/goldberg-stage679-recovery-20261023T052407Z`. No existing scheduler/service/test
source or raw result was overwritten; unrelated worktrees untouched.

Minimal fresh diagnostics: IPv4 socket creation EPERM; Unix socket creation allowed
but bind/listen/connect probe fails EPERM; github.com DNS EAI_AGAIN. Seccomp is active
and NoNewPrivs=1; exact filter rule is not inspected/claimed. Rootless Podman fails
before startup on read-only /run/user/1000/libpod. These are execution-environment
failures before application/database test assertions, not proven code defects.

Requested narrowly scoped host execution of the existing disposable-PG17 runner.
Automatic approval review rejected it before execution: granular approval policy
has sandbox_approval:false and disallows require_escalated. No permission prompt
can be presented through this tool. No bypass, alternate host, deployment or hosted
fallback attempted. Host permission therefore could not be obtained in this session.

Improved only the reproduction runner: fail-fast local TCP preflight with machine-
readable diagnostics before any test hooks/container, plus all five host checks.
`bash -n` PASS; restricted preflight correctly exits78 and executes zero DB test
bodies. Fresh direct unit run: 32 PASS,0 failed/skipped/cancelled. Fresh five host
checks: all PASS. Prior single-user PG17.11 smoke evidence retained; not rerun.
Full 34 new integration bodies and 87+50+34 original regression bodies NOT executed
in this recovery. Previous connection-hook failure records retained unchanged.
Uncommitted cleanup scheduler still awaits real PostgreSQL integration; no source
acceptance or complete threat-model claim. Remaining 6.7/6.8/6.9 integration assertions
are exactly those listed in the previous checkpoint; no newly observed code defect.

Live connected GitHub read: implementation branch still absent; Issue31 OPEN with66
comments. Remote URL remains https://github.com/stokonn/Goldberg.git. No push after
unverified tests; no new public checkpoint/body/dashboard edit. Exact pending comment
remains in results/publication-attempts.json as UNPUBLISHED. Previously observed
connector403 and CLI network errors remain publication blockers.

Exact host-side command (same worktree, existing installed pinned dependencies and
cached PG17.11 image; starts/removes only its own synthetic container):
`bash /tmp/goldberg-stage6-public-admin-abuse/spikes/stage6-public-admin-abuse/run-local.sh`
It runs units, all34 new PG tests including janitor, original87/50/34 suites and the
five checks, preserving each attempt in a fresh results/run-* directory. In a
permitted host, inspect actual assertion results and fix defects before committing
and pushing normally; verify exact remote SHA before posting Issue31 checkpoint.

No hosted/Supabase/OAuth/Android/Stage5/Stage7/merge/tag/APK/release action. Formal
6.1–6.18/6.0f gates unchanged. NOT ready for Stage6.10–6.11. Next action requires a
host execution venue permitting local sockets; this session cannot grant it.

## Local 6.7–6.9 implementation VERIFIED — Fedora host evidence, 2026-10-23

Publication is PENDING. Owner explicitly reports executing the existing runner
on Fedora in the permitted host environment. The agent independently read its
retained TAP/log files at spikes/stage6-public-admin-abuse/results/run-8SWyGyB0/;
this is owner-executed host evidence inspected by the agent, not agent-executed
sandbox PostgreSQL tests. Supersedes the earlier PARTIAL/execution-blocked entries
below for local implementation verification only. Historical failures remain intact.

Purpose/scope: reusable public catalogue overlay/change-feed, remaining Admin
operations and abuse protection/independent cleanup in the existing in-process
adapter. Original Auth/session/idempotency/audit/queue/open/decision implementations
are reused; additive local 004 migration; no HTTP listener or deployed API.
Branch implement/goldberg-0.11-stage-6-7-9-local, based on 262847e, preserving
partial source defbf346904c9aa082a4afc8cac625ce8ea8b923 as an ancestor.
Final implementation is the containing verified source commit; publication details
will be recorded separately. Stage5/Stage7A/X10 and unrelated worktrees untouched.

- [x] Inspect host preflight (localTcp available), PostgreSQL17.11, Node22.23.1,
  raw TAP test bodies/results, all five host check logs and own-container cleanup.
- [x] New PostgreSQL integration34 + unit32 + original handlers87 + Auth50 + schema34:
  **237 PASS; 0 failed, skipped, cancelled**. No hookFailed/not-ok records.
- [x] Current service/test bytes match all11 pre-run retention-candidate hashes;
  current runner matches its pre-run hash. Both manifests predate host execution.
  Owner confirms execution from this worktree; mtimes/TAP names agree. Host runner
  did not capture an at-run source manifest: do not claim cryptographic execution
  attestation. Additive final manifest covers46 source/dependency/test/migration/
  runner files;15 retained host artifacts hashed separately.
- [x] Original171 test sources, schema/Auth sources and old migration/history files
  unchanged. Original32-source manifests:30 unchanged; existing adapter and
  publication max-revision allocation are the two intentional extensions.
- [x] Public private-data separation, published projection, quarantine on old
  overlay cursors, publication/supersession/rollback events, numeric feed highwater/
  concurrent polling/equal timestamps/more-than-nine events and int8 max sequences.
- [x] Admin capability/default-deny/reason/exact-conflict checks, self-grant denial,
  role revocation/suspension/reactivation, role replay after revocation, Admin
  same-key replay/mismatch and distinct-key entity races, immutable audit export.
  Quarantine/rollback faults after lock/projection/audit/feed and at deferred commit
  preserve exact snapshots; same-key retry succeeds without rewriting history.
- [x] Daily/pending limits, competing final principal/IP quota slots, keyed coarse
  groups, duplicate replay without recounting, IP/day windows, explicit Retry-After,
  intake kill-switch read-only behavior, quota-store outage/recovery and rollback
  of both counters/business work at faults/idempotency insertion.
- [x] Independent janitor PostgreSQL cleanup with intake disabled; only expired
  coarse hashes removed, principal counters and submission/audit/publication
  evidence preserved. Unit lifecycle covers idle tick, no overlap, stop/drain,
  storage-fault retry and minimal safe diagnostics. Explicit start/stop is a future
  local runtime responsibility, not an installed daemon or hosted schedule.
- [ ] Push verified source normally and verify exact remote branch SHA.
- [ ] Append the factual verified checkpoint to existing Issue31 after remote
  verification; preserve all prior66 comments and unchanged dashboard.

Exact coverage/limitations: spikes/stage6-public-admin-abuse/results/SUMMARY.md,
verified-host-results.json, verified-host-source-sha256.json and
verified-host-artifacts-sha256.json. Source review found no genuine defect requiring
changes after this green host run; verified implementation/tests/runner unchanged.
Local T14/T16/T17/T18/T20/T22 subchecks only, no entire threat row accepted.
T14 remains no-fetch/URL deny-policy; DNS/redirect/rebinding retrieval unexecuted.
No HTTP/browser/hosted RLS/real OAuth/Android/offline/restore-RPO venue tested. Public
output supports reviewed plain-text display metadata and license/count summaries;
real source/field/license compatibility stays unresolved and default-deny. No real
users/credentials/IP diagnostics. Synthetic windows are not legal retention periods.
Number-based legacy writes still reject unsafe JSON integers; public/new Admin
int8 values use exact decimal strings. No production load/SLA claim. Event and
Admin expectedEventSeq fixtures are local extensions; client freeze remains later.

Formal6.1–6.18/6.0f and Q1/D5b/C4/C2 §12 remain unchanged; no hosted/cloud/Supabase/
release/merge/tag/APK action. Local server milestone is ready as the basis for a
separately authorized6.10–6.11 milestone; Android work has NOT started and is NOT
implicitly authorized. Remaining work in this milestone is GitHub publication,
not another general planning/security pass or a repeat of the host-green suites.

## Android 6.10–6.11 partial source checkpoint — 2026-10-23

Branch implement/goldberg-0.11-stage-6-10-11-local; isolated worktree
/tmp/goldberg-stage6-android-local, remote-verified backend base9d7150c.
Implementation commit is the containing scoped source commit. Backend237 PASS and
published Issue31 checkpoint6358952579 verified;67 old comments preserved.
Purpose: local-first Android community repository, contribution DTOs and safe
unavailable/error/retry behavior. Scope: data/community, lazy OpusApplication
composition, small Settings subpage, EN/CZ strings, JVM/native tests and host scripts.

- [x] Typed strict privacy-isolated request DTOs, public overlay/feed DTOs and
  disabled production transport, deterministic synthetic-only fixture transport.
- [x] File-backed local drafts with stable IDs/account namespaces, atomic saves,
  optimistic conflicts, incomplete editing, exact pending request/key persistence,
  explicit reviewed copy for new logical work, no background publishing.
- [x] Distinct400/401/403/404/409/422/429/503/offline/server/storage states, retry
  windows, uncertain outcome/receipt-save/cancellation safety and stale cache labels.
- [x] Candidate AndroidKeyStore AES-GCM credential adapter, no-backup location,
  local credential clearing; Google profile identity remains presentation-only.
- [x] Local draft Settings subpage using existing navigation/theme/components and
 29 localized strings; actual remote-submit control disabled, no fixture success UI.
- [x] Direct Kotlin/JVM **40 PASS,0 failures**; credential class compiles against
  SDK36. Separate JVM process recreates persisted draft store. Five host checks PASS,
 951 EN/CZ resources; host Room1→10..9→10 and backup fixtures PASS.
- [ ] Full Gradle JVM/lint/build/androidTest compilation: blocked before tasks by
  FileLockContentionHandler wildcard-IP failure. Initial raw log retained.
- [ ]5 new Android runtime tests, Room/backup instrumentation, emulator startup/
  process-death/upgrade and actual contributor screenshots. No physical phone used.
- [ ] Push verified partial source/checkpoint, then obtain permitted host execution.

Narrow host run request was AUTOMATICALLY REJECTED: sandbox_approval:false prevents
require_escalated and cannot display an approval prompt. No bypass attempted.
This is PARTIAL Android implementation, not formal6.10/6.11 completion. No actual
network privacy assertion, real service auth, hosted or UI runtime claim. Initial
new unit fixture error31/32 (Integer-vs-Long assertion) fixed; final40 PASS. Original
regression sources unchanged; full app regressions remain unexecuted.

No Room migration: schema10/backup v5/practice/profile/Google identity and backend
source untouched. Stage5/Stage7A/Stage7B branches/worktrees preserved. Backend
baseline predates six later Stage5 fixes; they remain on32cf5d9 for future integration.
No real endpoint or OAuth, upload/deploy/release/tag/main merge/subsequent-stage work.
Source/evidence/limits: stage-6/results/android-local/SUMMARY.md and source manifest.
Next: run scripts/verify-community-android-local.sh on a permitted host; inspect/fix
actual build/test results, then explicitly targeted disposable-emulator checks.
Only after acceptance recommend separate6.12–6.14 verification; do not start it.

## Android partial source saved — publication and acceptance pending

Implementation candidate commit6e4a627eab72c5c2056151c122556d5442417d91 on
implement/goldberg-0.11-stage-6-10-11-local, descendant of verified backend9d7150c.
Direct Kotlin/JUnit40 PASS and five host checks PASS; final captured source run is
stage-6/results/android-local/jvm-ofm366xf. Core/credential Kotlin was compiled;
full Android UI/build/lint and5 runtime tests remain unexecuted. Formal6.10/6.11
are unchecked and this local Android milestone is PARTIAL.

Git shell read failed resolving github.com; connected GitHub confirms the new
branch is absent. No unverified Android candidate was pushed. Issue31 remains at67
comments; no new checkpoint/dashboard edit. Prepared host verification is
scripts/verify-community-android-local.sh; optional EMULATOR_SERIAL must explicitly
name a disposable confirmed emulator. Host publication instructions and an exact
partial checkpoint are saved under ignored .gradle/community-publication/; publish
only after appropriate host verification and a normal exact-SHA remote readback.

The source commit retained raw log trailing blank lines. A scoped .gitattributes
rule disables blank-at-eof checks only for .log artifacts, preserving their raw
bytes; source/document whitespace checks remain enabled. No test failure was hidden.
Host execution approval was automatically rejected because sandbox_approval:false
prevents escalation prompts. No bypass, real network API, deployment, real account,
physical phone, main merge or release action. Next action is permitted-host Android
verification of this source, followed by emulator acceptance; not a new milestone.

## Android host Gradle verification confirmed — 2026-10-23

**JVM/build/lint VERIFIED; Android runtime acceptance still PENDING.**
Branch implement/goldberg-0.11-stage-6-10-11-local; owner-tested HEAD
1277bedf5056e794a94566e828ffecb4b14338a3 (implementation6e4a627). Before this
recording pass, working tree was clean and all15 recorded source hashes matched.
Owner executed on permitted Fedora host:
`./gradlew --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug --stacktrace`
and reported BUILD SUCCESSFUL. Agent inspected the actual generated outputs;
original console transcript was not supplied, so no compiler-warning count is
invented. No application/test source change was needed after this green run.

- [x]31 actual JUnit XML suites: **214 PASS,0 failures/errors/skips**; includes
 40 CommunityRepositoryTest tests and174 existing regressions. These40 are included
 in214, not an additional execution total. Full app/contributor UI Kotlin compiled.
- [x] LintDebug: **0 errors/fatal,109 warnings**. Category counts and raw XML/TXT/HTML
 retained. No warnings located in new data/community or CommunityDraftPanel files;
 SettingsScreen has an existing UseKtx suggestion on an unchanged expression.
- [x] Debug APK assembled, agent signature/badging inspected. ApplicationId
 com.opus.piano, inherited version0.10.1/code11, min26/target36. Trusted signing
 SHA25653eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e matches.
 APK SHA2568e30d783d223b7fc9b48c780edf6a3f5f24624aa016a77c3312d6f4149a0751d;
 preserved privately under .gradle/community-host-artifacts/1277bed/app-debug.apk.
 APK is not committed, uploaded or released.
- [x] Packaged manifest and data-extraction XML independently inspected: allowBackup
 false/fullBackupContent false; all cloud/device-transfer domains excluded. This is
 structural policy proof, not an executed Android restore/Keystore test.
- [x] Coverage mapped to exact executed XML test names for privacy/DTOs, file drafts,
 independent JVM restart, account isolation, error/retry/conflict/rate limits, cache,
 disabled transport and source-level backup/startup assertions. No network-path
 privacy assertion or Android process-death inference from JVM tests.
- [x] Direct standalone compilation of CommunityLocalInstrumentedTest.kt against
 the host-built app classes, SDK36 and cached AndroidX PASS. **0 Android test bodies
 executed**; no Gradle androidTest APK packaging claim.
- [x] Agent reran five repository checks: all PASS, including host DDL migrations
 1→10..9→10 and portable backup fixtures. No Room/personal/backup source change.
- [ ] Gradle :app:assembleDebugAndroidTest, packaging/install of test APK,5 new
 Android Keystore/tamper/backup-flag/filesystem/disabled-boundary tests.
- [ ] Existing Room/backup native migration and seeded upgrade verification;
 actual Android process death/recreation, cold startup, contributor UI/theme/font/
 CZ/EN screenshots and user-visible draft/error states. No physical phone use.
- [ ] Normal branch push/exact remote-SHA verification and Issue31 checkpoint.

Evidence copied before build outputs can be overwritten:
stage-6/results/android-local/host-confirmed-1277bed/ contains31 JUnit XML files,
raw lint reports, coverage.json, verification.json, source/artifact hashes and
APK/policy/direct-instrumentation-compile checks. Source identity relies on owner's
exact tested-revision report, initially clean tree and matching recorded hashes;
manual host command did not capture a contemporaneous source manifest. No claim
of signed execution attestation. Earlier blocked runs/failure logs remain intact.

Runtime venue: fresh executor loopback-bind probe returns EPERM; no active emulator
observed. Stage7B checkpoint6359455362 separately reports host qemu SIGSEGV. No
repeat crashing launch, AVD wipe, phone use or sandbox bypass. Existing granular
policy forbids host escalation prompts. A functioning permitted emulator venue is
required; shared Stage5/7 emulator state/worktrees must remain preserved.

All formal6.10/6.11/cloud/release gates remain unchecked; overall milestone remains
PARTIAL pending runtime acceptance. No real OAuth/endpoint/production upload,
Supabase/Cloudflare change, main merge, release or6.12–6.14 start. Next action:
compile test APK on permitted host, then execute scoped tests and native migration/
upgrade/UI acceptance on a named disposable emulator; publish this factual partial
checkpoint after normal source push and exact remote readback.

## OWNER STATUS CORRECTION — packaging and source publication confirmed (2026-10-23)

**The implementation is PUBLISHED. JVM/build/lint and instrumentation APK packaging
PASS. Android runtime/UI acceptance remains PENDING.** This entry supersedes all
older statements that packaging or source publication are blocked/unpublished.
Historical checkpoints below are preserved as point-in-time evidence.

Branch implement/goldberg-0.11-stage-6-10-11-local; published implementation HEAD
2f1524e4083a98a64d5fa9790f3cdc002d1f8237. Agent independently read the exact remote
branch HEAD and published Issue #31 checkpoint 6362788710:
https://github.com/stokonn/Goldberg/issues/31#issuecomment-6362788710.
Owner's guarded publisher record confirms preservation of all 70 earlier comments
and the Issue description at publication. Fresh connected read saw 72 comments;
no checkpoint was reposted or edited in this correction, and no build/publication
command was repeated. New documentation correction remains distinct from published
application source; application/test/build files have not changed.

- [x] Prior confirmed Fedora host JVM 214 PASS, 0 failures/errors/skips; debug APK and
 lint BUILD SUCCESSFUL (lint 0 errors / 109 warnings); results remain valid, not rerun.
- [x] Owner executed `./gradlew --no-daemon :app:assembleDebugAndroidTest --stacktrace`:
 BUILD SUCCESSFUL; 51 tasks (30 executed, 21 up-to-date). Task counts are owner-reported;
 original console transcript not supplied. Full instrumentation APK packaging PASS.
- [x] Agent inspected the actual packaged test APK/metadata/manifest/signature:
 com.opus.piano.test targets com.opus.piano using AndroidJUnitRunner, min 26 / target 36,
 trusted signing certificate matches. 991419 bytes; SHA256
 519dcf93d767e9a8fa5fade656f3361fe0f5d332e6e833603d8c66e75c60305e.
 Preserved privately at .gradle/community-host-artifacts/2f1524e/app-debug-androidTest.apk.
 All 15 recorded source hashes match. No APK upload/release.
- [x] Source branch publication and existing checkpoint read back independently.
 No duplicate Issue/checkpoint, dashboard change or repeat publisher execution.
- [x] Narrow non-destructive recovery/verification procedure prepared:
 stage-6/LOCAL-ANDROID-EMULATOR-RECOVERY.md. Preparation only; no VM was launched.
- [ ] Instrumentation execution: 0 Android test bodies; 5 new tests remain pending.
- [ ] Android Keystore/backup runtime, native Room/backup migration/seeded upgrade,
 actual app process death/recreation/cold startup and contributor UI/theme/font/
 CZ/EN screenshots/acceptance remain pending. Overall 6.10–6.11 is NOT fully accepted.

Owner diagnostics (not newly agent-executed VM tests): Fedora 44, Emulator 37.2.12,
KVM available/usable; AVDs stage5-api36 and Medium_Phone_API_37.0; no ADB-connected
device. Repeated SIGSEGV in qemu-system-x86_64-headless, command included
-gpu swiftshader_indirect; last inspected core PID 1321999. **Root cause UNCONFIRMED.**
Official graphics guidance marks this GPU mode deprecated; that is not causal proof.
Procedure retains existing diagnostic evidence and permits at most one explicit,
fresh isolated API36 desktop trial with a documented renderer, existing cached image,
separate AVD registry/data, bounded boot observation and stop-on-crash rules.
No original AVD boot/wipe/config edit, global ADB reset, SDK upgrade, permission/
SELinux change, physical-phone use or repeated crashing launch. If the trial fails,
stop and obtain stack-based diagnosis or a functioning separately selected venue.

New packaging/publication evidence and owner-attribution details:
stage-6/results/android-local/owner-status-correction-20261023/.
Existing application source, Stage 5/7 worktrees and all user/history data untouched.
No code defect/root-cause fix or runtime success is inferred. Formal 6.10/6.11/cloud/
release gates remain unchecked. No 6.12–6.14 start, merge, deployment, hosted change
or release. Exact next action: review the prepared procedure on a permitted host;
no rebuild or republishing of the already successful implementation is needed.

## Stage 6.12 — local security verification (2026-10-23; PARTIAL)

Purpose/scope: audit T1–T22 at assertion level and add focused local synthetic
regressions; verification/evidence only. No service, API, SQL or Android behavior
changes. Branch `verify/goldberg-0.11-stage-6-12-local`, based on `bcc7796`;
implementation ancestors: backend `9d7150c5b3f20880fe4d587d9c3bfd65a0f612ce`,
Android `2f1524e4083a98a64d5fa9790f3cdc002d1f8237`, both remote SHAs read back.
This owner authorization supersedes prior instructions deferring *local* 6.12;
6.13–6.18 and cloud/real OAuth/collection/merge/tag/release remain unauthorized.

- [x] Recover instructions, latest Issue #31 checkpoints and isolated worktree.
- [x] Inspect actual assertions and index exact successful test names/files/logs.
- [x] T1–T22 matrix: **13 local PASS, 6 PARTIAL, 2 NOT TESTED, 1 FAIL**. Local PASS
  can use explicit historical unchanged-source execution; no full [I] acceptance.
- [x] Fresh final run: **27 focused +32 existing unit =59 PASS**, zero failures,
  skips or cancellations; five repository checks PASS. Initial27-test log retained
  separately and not added to59. Pre/post runner source hashes unchanged.
- [x] Independently verify historical backend46 source +15 artifact hashes and
  Android15 source hashes. Retain host237 and JVM214 baselines without relabeling
  them as fresh reruns. All historical/failure evidence preserved.
- [x] Record T22 exact-status finding: unknown audit-edit operation returns400,
  threat model expects403. No mutation dispatch or privilege bypass demonstrated.
  Passing diagnostic probe records400; it does NOT pass the403 acceptance gate.
- [ ] Fresh PostgreSQL rerun: loopback socket preflight EPERM, runner exit78;
  **zero database test bodies**, not connection failures counted as executed tests.
- [ ] T13 rendering, T14 DNS/redirect/rebinding/fetch protections, T15 HTTP/browser,
  T19 actual isolated restore/RPO, T16/T20 runtime usability and T21 actual network
  privacy assertions remain OPEN. Hosted [I] requires separate authorization.
- [ ] Reconcile T22 exact status explicitly before accepting it; no silent API or
  contract change in this verification milestone.
- [ ] Normal branch push/exact remote SHA and one append-only Issue31 checkpoint
  pending at this source checkpoint; record actual publication separately.

Matrix: `docs/planning/goldberg-0.11/stage-6/LOCAL-SECURITY-MATRIX.md`.
Raw fresh results/index: `stage-6/results/security-local/`, final `run-x1eRKaqL`.
Reproducible narrow host command: `bash spikes/stage6-security-local/run-local.sh`
on a permitted local host with existing locked dependencies and cached PG17.11
image. It reuses the unchanged disposable backend runner; no hosted connection.
Do not rerun known-blocked PostgreSQL setup in this agent venue. Overall/formal
6.12 remains UNCHECKED/PARTIAL. Android6.10–6.11 runtime/emulator recovery DEFERRED;
packaging already PASS, zero native bodies. Stage5/7A/X10/7B and all user data
untouched. Exact next action: publish the verified audit scope normally, then
obtain a permitted PostgreSQL venue and resolve documented acceptance gaps.

## Stage 6.12 publication blocker — verified source preserved locally

Audit commit `ebd813509c046c3b6119e2aed953f270ec7d8d92` is LOCAL ONLY on
`verify/goldberg-0.11-stage-6-12-local`. Normal push failed before authentication:
`Could not resolve host: github.com` (exit128). Connected read after failure confirms
exact branch404. Issue31 checkpoint attempt returned403 `Resource not accessible
by integration`; fresh read found73 comments, zero matching new checkpoint marker.
**Source and new Issue checkpoint UNPUBLISHED.** Prior backend9d7150c and Android
2f1524e publication remain verified; unrelated new comment belongs to its author.

Guarded host publisher: `spikes/stage6-security-local/publish-host.py` requires an
explicit reviewed40-character HEAD, correct clean branch/origin/ancestry, matching
baseline/raw evidence/source hashes and PARTIAL findings; normal push only, exact
remote readback, duplicate guard, append-only Issue31 and old description/comments
readback. It neither builds nor deploys. Verify locally with `--verify-only`; actual
network publication remains UNEXECUTED until a permitted host runs it. This receipt
commit follows the verified audit source; use its exact current SHA explicitly.

59 tests/five checks PASS and T22 finding remain as below; no fresh PostgreSQL,
Android/emulator, hosted or later-stage execution. Next: run the guarded publisher
and unchanged disposable PostgreSQL verification on a permitted host, preserve raw
results and continue6.12 acceptance. Overall6.12 PARTIAL, all formal gates unchanged.

## Stage 6.12 — fresh owner PostgreSQL evidence confirmed (PARTIAL)

Owner executed the existing version1 security runner on Fedora at exact source
`faf9ba3ec24e7098289c80d6e119519ccc21b95e`. Agent independently inspected both runs:
`stage-6/results/security-local/run-vQd6iGn5/` and
`spikes/stage6-public-admin-abuse/results/run-qx9TUTDk/`.
This supersedes prior statements that fresh PostgreSQL execution is blocked;
all older checkpoints and raw failures remain historical evidence.

- [x] Wrapper:27 focused +32 unit PASS; PostgreSQL17.11:50 Auth +87 handler +34
  schema +34 integration +32 unit PASS. **296 successful executions;264 tests
  counting the repeated32 unit suite once. Zero failures/skips/cancellations.**
- [x] All five repository checks PASS in both runners; wrapper flags false for
  checksOrTestsFailed/postgresBlocked, true for sourceUnchanged. At-run62 source
  hashes all matched before tool edits; immutable55 raw-artifact hashes recorded.
- [x] Exact wrapper stdout equals nested TAP/check concatenation; container removal
  log retained. Successful exit uses owner report/v1 result/control flow; no numeric
  exit sidecar was captured. Live agent Podman inventory is denied by read-only
  runtime configuration; do not claim an additional live-container check passed.
- [x] Preserve run-csaqQFXV/run-cTS0AuDe dependency-startup failures, missing TAP
  summaries and all earlier successful/blocked runs. No destructive cleanup.
- [x] Fix only local verification tooling: version2 writes structured failure JSON
  for missing/duplicate/truncated/inconsistent TAP, retains raw logs, fails closed
  on false-green summaries and source changes; records future nested exit status.
  **10 Python reporting regressions PASS**, separate from296 host executions.
- [x] Matrix uses fresh exact names/logs:168 references; **13 local PASS,6 PARTIAL,
  2 NOT TESTED,1 FAIL**. T22 remains FAIL (observed400 vs required403), with no
  mutation dispatch or demonstrated privilege bypass. No API/SQL/Android changes.
- [x] Publisher now requires the successful actual host counts, immutable artifact
  hashes, tested-source ancestry/identity and current tool/index hashes; it keeps
  PARTIAL wording and duplicate/history guards. Old postgresBlocked=true gate removed.
- [ ] Full version2 PostgreSQL runner execution NOT performed; reporting tests pass.
- [ ] T22 explicit contract/security review; T13 rendering; T14 DNS/redirect/rebinding/
  fetch; T15 HTTP/browser; T19 real restore/RPO; T16/T20 runtime usability; T21 actual
  network privacy; all hosted [I] portions remain OPEN. No automatic gate acceptance.
- [ ] Publication of this correction awaits normal push/exact remote readback and
  one new factual Issue31 checkpoint; prior failed publication remains historical.

Branch `verify/goldberg-0.11-stage-6-12-local`, based on preservedfaf9ba3;
implementation/evidence commit for this update follows that ancestor. Current tool
changes are explicitly separated from the owner-tested version1 runner; at-run
source hashes are checked against current unchanged services or immutablefaf9ba3
for the identified tooling edits. Matrix/index and source hashes remain reviewable.
Read `stage-6/LOCAL-SECURITY-MATRIX.md` and `results/security-local/host-confirmed-faf9ba3.json`.
Exact next: publish the verified evidence/tool fix normally, then resolve T22 and
remaining local acceptance gaps; hosted checks require separate authorization.
Formal6.12 stays UNCHECKED/PARTIAL. Android runtime/emulator recovery deferred;
Stage5/7A/X10/7B untouched. No hosted/OAuth/real-data/paid/merge/tag/release or6.13–6.18.

## Stage 6.12 publication outcome — host tests VERIFIED, source/checkpoint UNPUBLISHED

Fresh Fedora PostgreSQL verification is confirmed:296 executions/264 tests counting
repeated unit suite once;10 separate reporting regressions PASS. This is independent
of publication status. Source update `a21dbafb1180563b88ed36909a801da179b5bf55` preserves
owner-testedfaf9ba3; this following receipt also excludes Python bytecode caches
from the version2 runner source snapshot. Full version2 PostgreSQL run unexecuted.

Normal publication failed: guarded remote lookup and direct non-force push could
not resolve github.com; connector comment returned403; directgh API could not connect.
Issue31 readback:75 comments, all previously read bodies preserved, zero new matching
checkpoint. Exact branch read returned404; no remote SHA or checkpoint URL claimed.
**Source and checkpoint remain UNPUBLISHED.** Existing backend/Android publications
remain valid. New draft `results/security-local/ISSUE31-HOST-VERIFIED-UNPUBLISHED.md`
and raw blocker record `fresh-publication-blocked.json` are retained.

Updated `spikes/stage6-security-local/publish-host.py` passed local read-only guards
at the verified source commit. On a permitted host, supply the exact current clean
HEAD with `--expected-head` (first `--verify-only`, then publish normally). It checks
fresh successful counts/hashes and preserves all prior Issue comments/description;
no repeated PostgreSQL/build run is needed merely to publish these verified facts.
T22 remains FAIL and formal6.12 PARTIAL; remaining local/hosted gates OPEN. Android
runtime deferred, no Stage7B/emulator/hosted/merge/release or6.13–6.18 work.

## Stage6.12 — approved T22 correction and Stage7B contract coordination (PARTIAL)

Published base `e7dc7ea871f61965600ba31076e8d28dc01822fe` and Issue31 checkpoint
6369071469 independently read back; older UNPUBLISHED statements below are historical.
Continue on `verify/goldberg-0.11-stage-6-12-local`, isolated from Stage7B.
Purpose/scope: trace and correct the approved local T22 routing classification,
add focused negative regressions, and prepare exact G1–G11 shared fixture handoff.
No formal6.13–6.18 work or6.17 API freeze.

- [x] Reproduce old400 before authentication/dispatch versus Auth adapter403.
- [x] Explicit owner approval recorded: malformed/non-string/empty operation400;
  well-formed unregistered audit/bootstrap operation403 with no dispatch; registered
  missing/invalid session401; live capability/self-grant403. Applied exact local
  patch and corresponding regression expectations; no new privileged endpoint.
- [x] Final local119 PASS:38 T22 +11 shared-contract +27 security +32 unit Node tests,
  plus11 Python reporting regressions. Zero failures/skips/cancellations in final
  runs. All five repository checks PASS; raw intermediate harness failures retained.
- [x] T22 tests use real services with scripted SQL, not actual PostgreSQL. No role/
  audit mutation attempted in denied cases; expired-key housekeeping rolls back.
  Non-string operation coercion is rejected before handler selection.
- [x] Matrix13 local PASS/7 PARTIAL/2 NOT TESTED; original T22 FAIL remains historical.
  T22 now PARTIAL after explicit owner decision and local fix, not full acceptance.
- [x] Read actual Stage7B source/checklist/acceptance/proposals read-only; hash all
  inspected bytes, including uncommitted work. No Stage7B file/index/build changed.
- [x] Shared Admin v1 preparation:30 actual in-process/scripted-storage captures,
  11 inactive proposals,29 byte-preserved authored Stage7B comparison cases. Exact
  source/corpus hashes; all synthetic, no HTTP binding or PostgreSQL claim.
- [x] G1–G11 ledger records normative/backend/Android/mismatch/fixtures/resolution/
  approval/status. G10 remains CLOSED on Android by static compatibility; G1–G9
  andG11 remain blocked for real integration. No dialect or revision mapping guessed.
- [ ] Current updated PostgreSQL assertions: agent socketEPERM,0 database bodies;
  run version3 local runner on permitted host. Prior owner296 executions/264 tests
  are pre-patch baseline only; original raw manifests remain immutable/Git-verifiable.
- [ ] Hosted/browser/fetch/restore/network-privacy/Android-runtime gates remain OPEN.
- [ ] Commit/push/readback and one non-duplicate Issue31 checkpoint for this update.

Reports: `stage-6/T22-INVESTIGATION.md`, `stage-6/STAGE7B-CONTRACT-READINESS.md`,
`contracts/admin/v1/readiness.json`. Raw final evidence and owner decision:
`stage-6/results/security-local/t22-investigation/`. Production networking and
privileged Android actions remain disabled. Formal6.12 PARTIAL,6.17 not accepted;
no hosted/OAuth/real data/paid/merge/tag/release/emulator/phone/Stage7B writes.
Exact next: publish reviewed scope, execute updated PG17.11 regressions on permitted
host, and obtain G1–G9/G11 implementation/DTO/session/validation decisions before
one approved Android adapter or any live integration.

## T22/Stage7B update publication outcome — LOCAL ONLY (base publication intact)

Verified code/fixture update `262a76ed08c7910e9a4ad86018188589efc58c37` is committed
on the isolated security branch. Normal non-force push failed DNS resolution;
connected read confirms remote still `e7dc7ea871f61965600ba31076e8d28dc01822fe`.
That base and Issue31 checkpoint6369071469 remain PUBLISHED. This new update's
checkpoint write returned403; no new checkpoint URL or remote success claimed.
The prior76 comments had no matching new marker; no dashboard/history edit.

Raw blocker and draft: `results/security-local/t22-handoff-publication-blocked.json`
and `ISSUE31-T22-HANDOFF-UNPUBLISHED.md`. Updated guarded publisher passed local
read-only verification at262a76e; this following documentation receipt changes no
service/fixture/test behavior. On a permitted Fedora host, run the publisher with
exact final clean HEAD, then the updated version3 PostgreSQL runner. It checks
source/corpus/evidence/approval/counts, normal push ancestry and exact remote SHA,
appends one factual checkpoint and retains history. No repeated network attempts here.

Final119 local tests/five checks PASS; updated PostgreSQL0 bodies dueEPERM. T22
PARTIAL after explicit owner-approved local correction; G10 closed, G1–G9/G11
blocked for real integration. No HTTP freeze, Stage7B edit, emulator, hosted changes,
merge/release or6.13–6.18. Exact next action: publish current scope from permitted
host and obtain current real PostgreSQL regression evidence plus shared decisions.

## Stage6.12 — post-T22 Fedora version3 VERIFIED; T22 LOCAL PASS, overall PARTIAL

Published/tested base `6d0abad3decf0b724c0e00f1b4e270bf8d244fba` independently read
back on the existing security branch; Issue31 checkpoint6370125421 preserved.
Purpose: reconcile the new actual post-patch PostgreSQL execution and T22 criteria,
not change service behavior or activate public/client integration.

- [x] Exact outer run-x0nDSTXd:27 focused +32 unit +38 T22 +11 contract =108 PASS.
- [x] Matching nested run-TEmZzZQg PG17.11:50 Auth +87 handler +34 schema +34
  integration +32 unit =237 PASS. **345 successful executions;313 tests counting
  repeated32 unit suite once;205 PostgreSQL-backed bodies.** Zero failures/skips/
  cancellations across nine TAPs; model/fixture tests remain distinctly labeled.
- [x] Explicit nested exit0, empty summaryErrors, successful outer flags, exact
  nested stdout linkage; all five checks PASS in both runners. All82 at-run source
  hashes matched before reconciliation edits and immutable31 raw artifacts hashed.
- [x] Removal output names goldberg-public-admin-abuse-3645015; container ID/version
  retained. No new live inventory pass is claimed. All previous runs/FAIL findings
  and pre-patch296/264 baseline preserved without deletion or relabeling.
- [x] T22 LOCAL PASS: actual handler/admin assertions require403 on absent audit edit;
  live self-grant/revocation/escalation and physical SQL immutable-history guards
  executed.38 routing/service models separately verify approved400/401/403 cases.
  No HTTP listener/hosted/Android assertion inferred from local execution.
- [x] Current matrix14 local PASS/6 PARTIAL/2 NOT TESTED. Browser, SSRF fetching,
  restore/RPO, network privacy, Android runtime and hosted portions remain OPEN.
- [x] Stage7B readiness reconciled: G10 remains closed by pinned Android boundary;
  G1–G9/G11 remain BLOCKED for real integration. Corpus captures/proposals retain
  original qualifications and source identities; no public API freeze or activation.
- [x] Evidence verifier/reporting regression tests executed separately; raw results
  in results/security-local/post-t22-tools/. No service/SQL/Android code changes.
- [ ] Normal source push/exact remote readback and one new factual Issue31 checkpoint
  for this reconciliation; record actual publication separately.

Current branch `verify/goldberg-0.11-stage-6-12-local`; current commit follows the
verified6d0abad ancestor. Exact source binding is to6d0abad, with only specifically
identified evidence/index/publication/documentation metadata changed afterward.
Reports: LOCAL-SECURITY-MATRIX.md, T22-INVESTIGATION.md, STAGE7B-CONTRACT-READINESS.md,
post-t22-independent-verification.json. Next: publish verified evidence normally,
then resolve G1–G9/G11 decisions and remaining acceptance under proper authorization.
Formal6.12 PARTIAL; no6.13–6.18, hosted/OAuth/real data/paid, emulator/phone, Stage7B
writes, merge/tag/release or6.17 acceptance. Successful v3 PG run need not be repeated.

## Post-T22 reconciliation publication outcome — VERIFIED locally, update UNPUBLISHED

Reconciliation commit `f7d88aae69bdf2a4b1adb248e1020c9a9b3baa8a` preserves the
published/tested6d0abad baseline and checkpoint6370125421. Normal push failed DNS;
connected read confirms remote still6d0abad. Checkpoint write returned403, with
no new URL; prior77 comments had no matching new marker. No dashboard/history edit.
This documentation receipt changes no tested service/fixture/SQL behavior.

345 host executions/313 tests counting repeated unit once;205 PostgreSQL-backed
bodies;17 additional tool regressions PASS. T22 LOCAL PASS, overall6.12 PARTIAL.
G10 remains closed; G1–G9/G11 integration-blocked. No HTTP/hosted/Android acceptance.
New draft and exact blocker: ISSUE31-POST-T22-V3-UNPUBLISHED.md and
post-t22-publication-blocked.json under results/security-local/.

Updated guarded publisher passed local --verify-only atf7d88aa. On permitted Fedora,
supply exact final clean HEAD to publish normally; it verifies the successful v3
counts/source hashes/exit/cleanup, prior evidence, source ancestry and history,
then appends one factual checkpoint after exact remote readback. No repeat of the
successful PostgreSQL suite, force push, guard bypass, emulator, Stage7B changes,
cloud operation, merge/release or6.13–6.18 work. Next: host publication, remaining
local acceptance and explicitly authorized G1–G9/G11 decisions/integration.


## Current — API v1 owner decision preparation (2026-10-23)

**PROPOSED / INACTIVE / NOT FROZEN.** Worktree `/tmp/goldberg-stage6-security-local`,
branch `verify/goldberg-0.11-stage-6-12-local`, starts at verified published baseline
`19f5642192148051a7b4eb5bc5efb1090192e3c0` and
[checkpoint 6370511889](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6370511889).
This supersedes older “unpublished baseline” summaries below; their evidence is retained.

Purpose/scope: planning-only G1–G9/G11 contract decision package, exact public DTO/
endpoint proposals, synthetic fixtures and offline verification tooling. No production
endpoint/auth/network/moderation activation; G10 remains CLOSED. Stage7B worktree
unchanged. Existing backend, Android source, SQL and historical corpora/evidence unchanged.

- [x] Separate normative design, verified local behavior, Android legacy expectations
  and unapproved preferred design in `stage-6/API-V1-DECISION-PROPOSAL.md`.
- [x] Prepare G1 snapshot statistics, G2 queue metadata/search, G3 audit filters,
  G4 authorized current values, G5 session-derived identity/lifecycle, G6 explicit
  open + independent revisions/replay/conflict recovery, G7 binding/schemaVersion,
  G8 lossless decimal int8, G9 canonical audit/window, G11 byte/policy validation.
- [x] Prepare 61 authored synthetic DTO cases (37 positive/24 negative), 15 verifier
  regressions; preserve 41 primary cases (30 captures/11 proposals) and 29 comparison
  scenes. Exact offline verification results are in docs/ai/VERIFICATION.md.
- [ ] Owner O1–O8 choices in decision document: public lifecycle/version mapper and
  offline logout amendment; TTLs; explicit revisions/receipts; int8/cursor/version
  compatibility; server timezone/window; search/privacy; actual field/source/license
  policy; separate future implementation authorization. No approval inferred.
- [ ] G1–G9/G11 integration BLOCKED; formal 6.12 PARTIAL; 6.17 NOT FROZEN;
  no 6.13–6.18 implementation or acceptance from this task. Browser/fetch/restore/
  privacy/hosted and Android runtime acceptance remain OPEN. X10 OPEN; Stage8 pending.

Prior 345 successful executions/313 tests counting repeated 32 once/205 PostgreSQL
bodies are historical verified source evidence, **not rerun**. Q1/D5b, C4, C2 §12,
cloud/zero-cost/legal and activation gates remain unchanged. Stop at owner decision gate.
Next agent: read proposed decision package and O1–O8; collect owner decisions before
any implementation. Do not run old publisher for this new package; use the dedicated
guarded `contracts/admin/v1/decision-proposal/publish.py` with exact final SHA.
Publication receipt/fallback: `docs/ai/API-V1-PROPOSAL-PUBLICATION.md`.
Normal push blocked by DNS; both Issue31 writes denied403 by integration.
No new checkpoint URL;78 previous comments and issue description unchanged.
Publication pending via guarded Fedora publisher with exact final clean SHA.
## Stage 6 ACL hardening and backup review — 2026-10-24

**Purpose:** review the owner-supplied candidate privilege hardening and
validate it only in a disposable PostgreSQL 17 runtime, while reconciling the
zero-cost backup/recovery requirements without exporting real data.

**Scope:** supplied review package; Stage 6 security/readiness and verification
documentation; no hosted Supabase permissions, application source, Room
schema, Android integration, Stage 7 worktree or production backup scheduler.

**Tasks**

- [x] Locate the extracted package using `xdg-user-dir DOWNLOAD` and read its
  README, ACL SQL, runner and backup review.
- [x] Review candidate preflight/postchecks, effective ACL intent and trigger
  preservation; no unrelated authorization change identified.
- [x] Inspect the local runtime without pulling dependencies.
- [ ] Execute disposable PostgreSQL 17 acceptance checks.
- [ ] Verify `anon`/`authenticated` effective `EXECUTE` denial, preserved
  `ensure_rls` behavior and no unintended privilege escalation.
- [ ] Verify encrypted synthetic backup/restore, manifest and recovery
  procedure; no real-data export or scheduler activation.
- [x] Record the actual blocked result and preserve raw output privately.
- [x] Synchronize Stage 6 handoff/state/verification and security audit docs.

**Progress and verification:** Podman is installed, but the required cached
`docker.io/library/postgres:17` image is unavailable. The supplied runner
returned **BLOCKED, exit 2** and correctly attempted no pull; no container or
SQL was run. PostgreSQL test count is **0 executed**. The candidate is
conditionally suitable for disposable testing only and is not approved for
hosted deployment. Backup readiness remains design-only: destination,
encryption keys, schedule, export, isolated restore and measured RTO are not
verified.

**Completion status:** **OPEN / BLOCKED**, not complete. C4 remains 8 PASS /
19 PARTIAL / 6 OPEN and 6.0f remains unchecked. M1–M3 remain preserved and
Stage 7B was not touched.

created by stokonn with love <3
## Stage 6 ACL hardening and backup review — 2026-10-24 verification update

**Progress:** The owner-authorized official PostgreSQL image was pulled
rootlessly with Podman. The unchanged supplied runner exited **0**. PostgreSQL
17.11 synthetic acceptance passed: effective `EXECUTE` is false for
`PUBLIC`, `anon` and `authenticated`; `ensure_rls` remains enabled; synthetic
table creation enables RLS; no unexpected authorization change was observed.
The final detailed run captured clean PostgreSQL logs and removed all
disposable resources.

**Evidence:** image manifest
`sha256:3518071376d0b6156b02e1db2747a78511b5b9dc3763cc79c506ebe2dc2ee71c`;
local ID `82903352d6faf8f8a318bc4353d1cb1db67d4a5ff968d2799b2718e2cb00afa2`.
Raw runner/detail logs are preserved privately. Two auxiliary diagnostic
attempts exited 1 due query syntax errors; corrected final diagnostics passed.

**Completion status:** ACL disposable verification **COMPLETE**. Hosted
deployment approval, rollback/migration review, C4, 6.0f and backup/recovery
requirements remain open. No production or cloud change occurred.

created by stokonn with love <3
## Stage 6 backup and recovery verification — 2026-10-24

**Purpose:** exercise the documented zero-cost logical backup design against
only synthetic PostgreSQL 17 data, including encryption, integrity, isolated
restore and audit-chain verification.

**Scope:** new `spikes/stage6-backup-local/` fixture, shell harness and SQL;
private synthetic evidence; Stage 6 documentation. No Supabase, real data,
production scheduler, provider storage, Android or Stage 7 worktree.

**Tasks**

- [x] Review `ZERO-COST-OPS.md`, `BACKUP-REVIEW.md`, `DATA-MODEL.md` and
  `THREAT-MODEL.md`.
- [x] Implement a no-port, disposable PostgreSQL 17 synthetic fixture.
- [x] Export a custom-format logical dump and encrypt it with a temporary key.
- [x] Generate and verify a SHA-256 manifest; reject deliberate ciphertext
  tampering.
- [x] Decrypt and restore into a fresh database.
- [x] Verify row counts, normalized schema equality and audit-chain/head
  integrity.
- [x] Verify cleanup and scan preserved evidence for secrets/endpoints.
- [x] Record local export, restore and total elapsed times.
- [ ] Verify owner-controlled destination, key custody/recovery, schedule,
  retention, hosted restore and production backup failure handling.

**Verification:** final harness exit **0**; export **368 ms**, restore
**997 ms**, total **4,354 ms**. Restored counts were 2 metadata, 2
submissions and 2 audit events. Encryption, manifest, tamper rejection,
schema comparison, audit chain and cleanup passed. The measured timings are
synthetic local timings only; no production RPO/RTO claim is made.

**Completion status:** synthetic local drill **COMPLETE**; backup readiness
and Stage 6 remain **OPEN** pending private destination/key/schedule/restore
decisions and the independent C4/6.0f gates.

created by stokonn with love <3
## Stage 6 major engineering/readiness batch — 2026-10-24

**Purpose:** reduce independently actionable Stage 6 readiness risk without
activating production services or changing the approved C4 classification.

**Scope:** ACL migration proposal, synthetic backup acceptance, Stage 6/7B
contract comparison, local verification, and synchronized handoff documents.

**Tasks**

- [x] Prepare a narrow reversible `rls_auto_enable()` ACL migration with
  preflight, postflight, and exact-ACL rollback guidance.
- [x] Re-run the supplied disposable PostgreSQL 17 procedure unchanged.
- [x] Record backup resilience acceptance and distinguish synthetic evidence
  from production scheduling, retention, restore, and RTO gates.
- [x] Compare Stage 6 API local semantics with the immutable Stage 7B source
  and preserve the inactive mapping proposal.
- [x] Run focused API/security/policy/fixture checks.
- [ ] Resolve the pinned Stage 7B source-hash mismatch and obtain owner approval
  for the shared public API fixture decisions.
- [ ] Close hosted/legal/C4/6.0f and G1–G9/G11 requirements.

**Verification:** PostgreSQL 17 ACL procedure exit 0; API **106/106 PASS**;
security evidence regressions **17/17 PASS**; policy, backup fixture, and
whitespace checks PASS. `verify_contract.py` failed closed on the pre-existing
Stage 7B `ACCEPTANCE.md` hash mismatch. No hosted or Android runtime checks
were executed. **Status: in progress; not complete.**
## Stage 6 integration engineering batch — 2026-10-24

**Purpose:** establish executable local compatibility and provenance guards
without activating an unapproved API mapping or production service.

- [x] Compare the pinned Stage 7B hash with later branch history and current
  source; preserve historical evidence.
- [x] Add synthetic executable compatibility tests for approved distinctions.
- [x] Add a static ACL proposal safety regression.
- [ ] Obtain owner decisions and implement only an approved shared binding.

**Verification:** compatibility and ACL checks are executed with the batch
test results; no Stage 7B files, hosted services, Android devices, or live
databases were touched. Status remains **in progress**.
## Stage 6 local API contract hardening — VERIFIED LOCALLY (2026-10-24)

**Purpose:** implement independently testable, fail-closed API contract
primitives without activating the provisional `/v1` surface.

**Scope:** local API statistics/time-zone and query validation helpers plus
synthetic tests; no HTTP listener, deployment, hosted database or Android
transport.

- [x] Derive Europe/Prague civil-day windows with UTC six-digit timestamps.
- [x] Verify ordinary, spring-DST and autumn-DST windows against the approved
  fixture corpus.
- [x] Implement NFC-normalized, case-insensitive, diacritic-sensitive literal
  search.
- [x] Validate bounded queue filters and exact audit vocabulary/ranges.
- [x] Reject unknown, unsafe, malformed and ambiguous query inputs.
- [x] Run local API and policy/design checks: 115/115 API tests passed.
- [ ] Implement queue/audit DTO projections and statistics aggregation only
  after owner-approved source fields and snapshot semantics are available.

**Verification:** `npm test --silent` passed 115/115; targeted statistics tests
passed 4/4; policy, backup fixture, ACL proposal, design fixture and whitespace
checks passed. No HTTP/PG/Android runtime acceptance was claimed.

**Completion:** local contract-hardening substage verified; Stage 6 remains
incomplete because `/v1` compatibility, hosted/security/legal/operational and
G1–G9/G11 gates remain open.

created by stokonn with love <3
## Stage 6 loopback HTTP integration — VERIFIED LOCALLY (2026-10-24)

**Purpose:** exercise the approved local adapter through real HTTP request and
response handling without exposing a public listener or activating `/v1`.

**Scope:** `spikes/stage6-api-v1-local/src/http.mjs`, adapter route guards,
synthetic loopback tests and local evidence only. No hosted service, real
credentials, Android transport or Stage 7 worktree.

- [x] Bind only to `127.0.0.1` on an ephemeral port.
- [x] Preserve adapter validation, authorization, request IDs and response
  headers through HTTP.
- [x] Enforce body-size, duplicate-key, duplicate-query and unsupported-method
  rejection.
- [x] Verify challenge/login/capabilities/logout and empty 204 behavior over
  HTTP.
- [x] Recognize queue/audit/stats routes with capability guards and explicit
  unavailable responses; do not fabricate records or totals.
- [x] Run loopback tests 2/2 and complete local suite 117/117.
- [x] Re-run disposable PostgreSQL M2 and M3 acceptance; both passed with
  cleanup and source-unchanged checks.
- [ ] Add exact queue/audit DTO projections and database-backed statistics
  snapshots once approved source fields and snapshot semantics are available.

**Verification:** actual loopback HTTP 2/2 PASS; complete API suite 117/117
PASS; M2 and M3 PostgreSQL acceptance PASS. M2/M3 report `httpExecutions: 0`;
they are separate database-backed checks and do not prove HTTP behavior.

**Completion:** loopback transport substage verified. Stage 6 remains
incomplete because public-version compatibility, exact queue/audit projections,
statistics snapshots, hosted/legal/operational gates and G1–G9/G11 remain
open.

created by stokonn with love <3

## Stage 6 full-int8 recovery — LOCAL ACCEPTANCE VERIFIED (2026-10-24)

Purpose/scope: recover and complete the interrupted, owner-authorized shared
handler/local API int8 implementation, current provenance and disposable-PG
acceptance. Branch `feature/goldberg-0.11-stage-6-api-v1-local`; recovered published
baseline `0142e997f3a63a026c76219d8dcb04185b66e7ee`. Implementation/evidence commit `023d290e5f30c26e85ffba5f20b1d16914a1e1e5`
ordinary-pushed; exact remote SHA verified. Implementation [quality run40098429341](https://github.com/stokonn/Goldberg/actions/runs/40098429341) **SUCCESS**; publication receipt below.

- [x] Preserve dirty work, historical metadata/provenance/raw failures and private
  `/tmp/goldberg-stage6-private-pmp/checkpoint.md`; save new private recovery
  checkpoint separately. Other agent worktrees/processes untouched.
- [x] Canonical decimal strings through the wire/shared handler bridge; entity≥0,
  submission≥1; BigInt checked increments through signed int8 maximum;409 overflow,
  400 malformed input. Existing atomic locking/idempotency/audit mechanisms retained.
  No schema, migration, transaction utility or Android change.
- [x] Final run `results/http-pg/pg-kr8b1q9g`: **469 Node bodies PASS**, including
  **24 existing HTTP→PG +13 int8 HTTP→PG +240 independent PG** bodies;
  **API122/T22-38/operations-unit32**. Zero failures/skips/cancellations.
- [x] **Historical18 design +17 security-evidence** regressions PASS on original
  `6dd3136` source (source baseline `19f5642`); **current parser11** PASS.
  **Current DTO61 expectations** match37 positive/24 negative;39 actual HTTP read
  DTOs and PostgreSQL Comparison captures validate. Frozen current-file pin rejection
  is retained explicitly as EXPECTED FAILURE, never a current-source PASS.
- [x] Policy, three portable backup fixtures, ACL proposal and whitespace PASS.
  **181 current-source files + manifest (182 run hashes)** unchanged during execution;
  independent verifier exit0 and live exact container/volume absence probes PASS.
- [x] Ordinary commit/push/exact remote readback of `023d290e5f30c26e85ffba5f20b1d16914a1e1e5`;
  Issue31/Project publication checkpoint synchronized. All182 committed source/manifest
  files match final run.
- [x] Implementation [quality run40098429341](https://github.com/stokonn/Goldberg/actions/runs/40098429341) SUCCESS; record this documentation-only
  publication receipt and committed source identity. Issue31 stays OPEN / Goldberg
  Development In Progress. Final receipt SHA/CI are retained in Issue31/private PMP.

The source-set SHA256 is
`b7c6db2d251429e7edbbcef4ae3095a915ee8d5330ac7233254e4573b075c745`.
All commands, counts, failure history and full-range submission testing limits are
in `docs/planning/goldberg-0.11/stage-6/API-FULL-INT8-ACCEPTANCE.md`.
Extreme submission increments/overflow have scripted service coverage; actual PG
chains preserve every guard and exercise1→2, without fabricated high predecessors.

**Completion:** this local numeric substage passed acceptance. Stage6 OPEN,
6.12 PARTIAL,6.17 NOT FROZEN; C4 **8 PASS/19 PARTIAL/6 OPEN**,6.0f unchecked;
consumer G1–G9/G11, `/v1` compatibility, policy, client reconfirmation, hosted,
operational and legal gates remain open. G10 closed/Android transport disabled.
No production/cloud/OAuth/real-user/device/ADB/other-worktree/merge/release/deploy.
No local Gradle/JVM/lint/APK/signing/instrumentation/screenshots executed; existing
GitHub quality workflow is checked separately after publication. Prior restore/ACL
SQL evidence is historical, not rerun by static checks. Failed attempts retained.

created by stokonn with love <3

## Stage6/7B executable integration readiness — LOCAL ACCEPTANCE VERIFIED (2026-10-24)

**Purpose/scope:** maintained canonical shared fixtures, actual HTTP/PG behavior,
source-pinned consumer compatibility, isolated actual Kotlin JVM replay and measured
query bounds. Stage6 branch `feature/goldberg-0.11-stage-6-api-v1-local`; recovered
published baseline `a3fb7e2f1436c93b49a0f4f0596d8d06e241fcef`. Stage7B inspected
read-only at `0887d3fe33cf5d49d46e14e1de95168f08a493a2`; its source/acceptance intact.

- [x] Recover actual branch/remote/checkpoints/O1–O8/Issue31/private PMP and inspect
  actual Admin API/Models/WireValues/Repository/Review/Session/Queue/Audit/Stats.
- [x] Implement versioned `contracts/admin/shared/v1`:66 actual HTTP scene fixtures,
  canonical schema references, consumer source/history pins, executable local
  session/reconfirmation reference and34 independently checked compatibility rows.
- [x] Compile four unmodified selected actual Kotlin sources into/tmp;22 replay
  probes PASS:12 compatible/10 incompatible. Full matrix:12 VERIFIED COMPATIBLE,
  12 VERIFIED INCOMPATIBLE (including2 explicit static contradictions),7 NOT EXECUTED,
  2 BLOCKED BY OWNER/POLICY,1 NOT APPLICABLE. No Android runtime claim.
- [x] Backend accepts explicit UTF8 JSON charset; no legacy DTO/revision aliases.
  Measured queue optimization bounds header page before payload joins; all filters,
  ordering/snapshot/lock semantics preserved. No schema/index/migration change.
- [x] Final `results/http-pg/pg-ojxz4mbi`: **490 Node bodies PASS**,0 failures/skips/
  cancellations: all469 baseline bodies plus20 shared HTTP/PG and1 query-plan body.
  Existing realHTTP/PG37, full-int8 coverage, auth/T22/concurrency/replay/rollback
  retained.66 actual fixture responses validate before synthetic-token redaction;
  2 actual response streams destroyed to verify loss recovery.39 existing read and
  22 Comparison captures still match schema.
- [x] Current DTO61 expectations PASS (37 positive/24 negative), historical18 design/
  17 security-evidence tests on original snapshot and current parser11 PASS; policy,
  three backup fixtures, static ACL proposal and whitespace PASS. Historical pin on
  evolved current handlers remains EXPECTED FAILURE, not reclassified PASS.
- [x]196 source/test/input files plus separate integration manifest unchanged through
  execution and independently verified; original full-int8 manifest unchanged.
  Exact synthetic container/volume removed and independently probed absent.
- [x]5000-row actual-query EXPLAIN: five variants,5s timeout, page≤20/SQL21;
  revision work≤21. Default/keyset samples2.877/2.871ms vs13.148/13.368ms before.
  Header scans/statistics remain scaling limits; no production/free-tier budget claim.
- [x] Exact consumer request/session/conflict/metadata/freshness handoff, G1–G11,
 34-row matrix and gated concrete /v2 migration proposal prepared. External/deployed
  consumer state UNKNOWN; no public version selection/activation/freeze.
- [x] Source/evidence `b2fed621ef32665072faa6172d7947c99db3af61` ordinary-pushed and exact remote SHA verified;
  all197 committed input/manifest files match final run; recovered baseline is an
  ancestor. Documentation-only receipt records publication; exact final-head GitHub
  quality result is maintained in the final Issue31 checkpoint/private PMP.

**Source-set SHA256:**
`ddffb491f5e99f1db6bd20673312be888b8b4f846d30038d15fdcad2eea2a9c0`.
**Commands/results:** `python3 spikes/stage6-api-v1-local/build-current-provenance.py`,
`python3 spikes/stage6-api-v1-local/run-http-postgres.py`, then
`python3 spikes/stage6-api-v1-local/verify-http-pg.py spikes/stage6-api-v1-local/results/http-pg/pg-6ape0ubv`
all PASS/exit0. Selected Kotlin replay is integrated by `consumer-compatibility.py`;
independent scene/matrix/plan verification by `verify-shared-contract.py`.
Full handoff: `docs/planning/goldberg-0.11/stage-6/INTEGRATION-READINESS.md`.

**Completion:** local foundation passed acceptance and source is published; exact final-head
CI result/receipt is referenced by the existing Issue31 checkpoint. Stage6
and Stage7B remain OPEN/IN PROGRESS; G1–G9/G11 consumer adoption, owner version/real
policy, client lifecycle/UI and hosted/legal/operational gates remain open. G10
CLOSED for disabled production transport/actions. C4 **8 PASS/19 PARTIAL/6 OPEN**,
6.0f unchecked,6.12 PARTIAL,6.17 NOT FROZEN. No production/OAuth/real-user/device/
ADB/other-worktree/main merge/release/deploy. No local Android build/APK/signing/
instrumentation/screenshots; isolated Kotlin JVM replay is not Android runtime.
Failed/intermediate raw evidence and private PMP preserved; original two untracked
M2/M3 directories retained. Next: publish verified local source and inspect exact-SHA
CI; then only separately authorized remaining adoption/owner/hosted work.

created by stokonn with love <3

## Stage 7B Android Admin — current continuation (2026-10-23)

**Purpose:** secure Android moderation with the approved minimal dark interface.
**Scope:** admin facade/services, Compose Admin routes/theme, EN/CZ strings,
account-scoped markers and optional WorkManager checks. Personal Room schemas,
backups, practice state and Stage 6 working trees are unchanged.

- [x] Recover OpenCode state and inspect Issue #31/latest checkpoint + references.
- [x] Queue host milestone: retained bounded service, search/filter/pagination,
  update detection/manual apply, marker/context preservation; 12 new JVM cases PASS.
- [x] Review/conflict host milestone: confirmations, revision/role validation,
  same-key uncertain-response replay, guided re-review; 18 new cases PASS, followed
  by four lifecycle/cancellation/conflict cases PASS.
- [x] Audit/consent host milestone: chronological projection and bounded digest
  scan/notification consent policy; nine new JVM cases PASS.
- [x] Final source build/lint/JVM/instrumentation compilation — actual results are
  recorded in the [living checklist](stage-7/stage-7b/CHECKLIST.md).
- [ ] Catalogue before-values and live session/dual-revision integration (G4/G5/G6).
  **Production decisions disabled** pending frozen Stage 6 wire fixtures.
- [ ] Emulator/instrumentation execution, actual screenshots, dashboard no-scroll,
  accessibility and seeded-upgrade acceptance: **BLOCKED / UNEXECUTED**, not passed.
- [ ] Separate owner acceptance; Stage 7A X10 remains OPEN.

Final host gate **PASS (2m41s): 251 JVM tests, 0 failures/errors/skips**, debug APK
and instrumentation compilation PASS; lint 0 errors/132 warnings. Five host scripts
PASS, 1050 matching EN/CZ resources. Instrumentation was not executed. Final evidence
and APK source/hash/signing identity are retained in the living checkpoint.

**Status:** 7B.4–7B.7 implemented within documented contract limits; 7B.8 preparation
in progress. No stage marked fully complete while its verification is blocked.
[Acceptance packet](stage-7/stage-7b/ACCEPTANCE.md) contains exact remaining checks.
Branch `feature/goldberg-0.11-stage-7b`, HEAD `cee9330` + uncommitted continuation.
No Stage 6 tree edits, physical device, deployment, merge or release.


## Stage 7B contract preparation — recovered verified checkpoint (2026-10-23)

**Current status: local preparation verified; Stage 7B acceptance remains IN PROGRESS / BLOCKED.**
Branch `feature/goldberg-0.11-stage-7b`, HEAD `cee9330f82cf2e57dcdc66a63d94b7a4e3749888`
plus preserved uncommitted continuation. No application code was changed by this
recovery session. Stage 6 trees untouched; no commit/push, merge, release or deploy.
`.pmp/` and `.pmp.yml` remain private/untracked. This entry supersedes older current-status
summaries below; historical checkpoints and earlier counts remain audit history.

**Purpose/scope:** finish the interrupted API contract integration preparation by
recovering source/evidence, retaining supported Android safety changes, and making
contract decisions explicit without inventing or activating a backend mapping.

- [x] Read policy, PMP sequence-3 audit checkpoint, AI handoffs, Stage 7B checklist /
  acceptance, latest Issue #31 comments, branch/status/diffs and existing evidence.
- [x] Recover immutable `bcc7796` audit and `faf9ba3` comparison; 29 synthetic cases
  and 15 pinned hashes. Normative design, legacy C6, local candidate and inactive
  mapping proposal stay separate. No Stage 6 backend tests executed here.
- [x] Retain already implemented application-disabled transport even with a saved
  endpoint/token; independent decision disable; strict capability principal/string
  validation, exact legacy Int revisions and Long audit sequences, cursor type
  rejection, typed malformed-payload errors. No candidate dialect adapter enabled.
- [x] Preserve recovered successful Gradle outputs: 36 suites / **275 JVM tests,
  0 failures/errors/skips**, including **24 AdminContractIntegrationTest cases**.
  `./gradlew testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug
  --no-daemon --max-workers=1` completed in 2m26s; retained follow-up 13s pass.
  These are **prior-session executions inspected now**, not new JVM executions.
  Initial two capability-test failures remain preserved and are superseded by
  the passing reports after their pre-interruption fix.
- [x] Lint recovered: **0 errors/fatal, 132 warnings, 1 hint**. Instrumentation APK
  compiled/assembled only; **0 instrumentation bodies executed**.
- [x] Recovery session executed all six host scripts: contract fixtures/provenance,
  agent policy, backup formats 1–3, history paging, localization (1050 EN/CZ), and
  migrations v1–9→10 DDL; all PASS. `git diff --check` PASS.
- [x] Recovery session verified APK signing, badging, ZIP integrity and exclusion of
  test corpus. APK `app/build/outputs/apk/debug/app-debug.apk`, 24,140,673 bytes,
  SHA-256 `6976d76e9bd936cd5b45e26f1910f86c3105863a8fe2bbf6f1269aaafc7e269b`;
  `com.opus.piano`, development version **0.10.1/code 11**; trusted signer
  `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.
  No application source file newer than APK; test-only corpus files are newer.
  Source manifest reconstructed on recovery, not contemporaneous build/CI proof.
- [x] Recover bounded emulator investigation: read-only `stage5-api36`, explicit
  intended serial `emulator-5582`, no snapshot/wipe; **SIGSEGV / exit -11 after
  21.68s**. This is host setup failure, not application evidence. No new launch,
  ADB operation or physical device use in recovery; no screenshots inspected.
- [ ] Stage 6/owner contract approval and actual Android/backend integration.
- [ ] Functioning permitted emulator; rendered/no-scroll screenshots, accessibility,
  WorkManager/notification runtime, seeded upgrade and owner acceptance.

**Unresolved G1–G6:** G1 server stats; G2 server queue search/display labels;
G3 audit server filtering/search; G4 verified current catalogue before-values;
G5 live credential/session/principal lifecycle; G6 explicit open + two revision
expectations and result/conflict/replay HTTP semantics. All remain OPEN. Existing
bounded exact-or-unavailable/client-side fallbacks do not close server gaps.
Additional G7 public DTO/cursor/HTTP binding, G8 numeric representation, G9 audit
vocabulary/time windows and G11 validation/reason/license policy remain OPEN.
**G10 local Android production networking boundary is closed**; this is no backend
approval. Production networking and moderation decisions remain DISABLED. X10 OPEN.
Personal database/schema/backup/practice data unchanged.

**Next milestone:** obtain explicit approval of versioned shared public
session/capability/queue/detail/open/dual-revision/audit/error/validation fixtures;
then implement one approved adapter and run isolated integration tests. Obtain a
working allowed emulator venue before executing the remaining acceptance matrix.
Do not select a dialect heuristically, default missing expectations, revive
networking from an endpoint setting or retry the known crashing emulator unchanged.

Evidence: `docs/planning/goldberg-0.11/stage-7/stage-7b/results/contract-preparation-2026-10-23/`
(recovered raw logs/XML, failure history, emulator diagnostics, source hashes,
artifact identity and recovery host checks). [Recovery Issue checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6368949572). Final PMP checkpoint is read through
`pmp resume` / `pmp history`; native PMP test-count evidence is not fabricated from
recovered JUnit outputs. Issue #31 remains OPEN; Project remains In Progress.

</details>
