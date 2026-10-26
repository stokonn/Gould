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

# Stage 6 final integration — active execution checklist

Purpose: compose the accepted PostgreSQL domain services with executable BFF
transport and canonical clients, preserving the approved contract and disabled
production boundary. Starting source: da2423bcea3b9ef8d0ea88a9b08a2e5468f173d6.

Scope: Stage 6 transport/composition, regression tests, isolated Stage 8 client
adoption, read-only hosted reconciliation, acceptance and recovery documentation.
No personal database migration, main merge, public activation or release.

- [x] Recover clean branch, worktrees, private PMP and Issue 31 dashboard.
- [x] Read-only hosted catalog: 20 RLS tables, ledger v1 checksum
  d39a3afd68a2671dd642a17a82f96edea76dfc0ae1a09edb4be9ffde6aa5c935,
  intake false, one marker, SELECT-only probe policy and application-table denial.
- [x] Read-only Advisor: 19 INFO default-deny findings, no earlier function warnings;
  anon/authenticated function EXECUTE both false. Project healthy, Frankfurt, PG17.11.
- [x] Recover reviewed Stage 8 integration sequence and actual Android Admin source.
- [x] Implement and test BFF composition and bounded Fetch transport (506 bodies pass).
- [x] Verify connection lifecycle, policy/kill-switch, canonical routes and errors.
- [x] Implement controlled canonical application service/transport in isolated branch f5fc296; actual Kotlin/HTTP/PG1pass.
- [ ] Complete accepted legacy UI/Keystore adoption before public community activation.
- [x] Execute feasible local/backend/operations andAPI36CI subset:506backend/302JVM/203native(192pass,11existing skips),seeded upgrade PASS.
- [ ] Hosted runtime,API26 andseparately driven preservation probes not executed in this candidate run;production matrix remains incomplete.
- [x] Prepare revised remaining-only packet; account403/pooler/trust/secret prerequisites still unbound.
- [x] Publish Stage6 ce7ce21/af420b7 andStage8 f5fc296;exact remotes read back.
- [x] Final CI:rootf300b09 quality40212884026/BFF40212884120 SUCCESS;RCa73a7e2 quality40212615987/native+upgrade40212684482 SUCCESS. Published prerelease andremote asset digest verified.
- [x] Reconcile24 requirements/G1–G11/C4/handoffs andactual source/CI/artifact results;Issue31 description/checkpoint6400636551 readback andProjectInProgress verified;private PMP saved.

Current status: IN PROGRESS. No new hosted mutation, credential provisioning,
Worker deployment or remote session activation authorized or performed.
Owner-supplied Free/no-card evidence applies to both providers; billing403 is an
automated visibility limitation. Operational Pi/SD backup automation is deferred;
production real-data collection remains blocked. Synthetic developer verification
must be assessed separately from production privacy and backup acceptance.

Verification separates hosted read-only catalog/Advisor observations from executed
local backend/client/operations andAPI36 native/upgrade evidence. No hosted Worker
execution is inferred. Earlier raw failure andacceptance evidence remains intact.

created by stokonn with love <3
