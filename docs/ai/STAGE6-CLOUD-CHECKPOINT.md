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

## Private cloud readiness checkpoint — October 24, 2026

LOCAL PREPARATION COMPLETE / **PRIVATE STAGING NO-GO**. Existing Stage6 section
in the Issue description carries the authoritative living checklist;prior comments
and all historical evidence are preserved.

Purpose/scope: review-only ACL fix, private PostgreSQL bootstrap from unchanged
001–004 migrations, disabled Worker shell/configuration and synthetic verification
packet in isolated `feature/goldberg-0.11-stage-6-cloud`. Starting/current committed
HEAD `006fa577ff4faef8ee7a25daf851620feb5bc3a8`; new preparation/evidence is
**local and uncommitted/unpushed**, not a published or deployed implementation.

- [x] Existing Supabase read-only metadata:ACTIVE_HEALTHY,Free/tier_free,
  eu-central-1,PG17.11.0.003;0 tables in public/goldberg_private,0 recorded migrations.
  Two external Security Advisor WARN findings on public.rls_auto_enable confirmed.
- [x] Audit:owner postgres,SECURITY DEFINER,search_path=pg_catalog,NULL default ACL,
  enabled ensure_rls with three expected DDL tags. Narrow candidate revokes only
  EXECUTE from PUBLIC/anon/authenticated,preserves definition/OID/owner/config/
  trigger/unrelated grants. Earlier PUBLIC pseudo-role assertion corrected.
- [x] Atomic checksum-ledger private-schema candidate reuses historical SQL;
  no API/client/service-role grants,all-table RLS/no policies,intake disabled.
  Inert Worker returns503 for all routes;route-free cf template has workers.dev/
  preview URLs/logs/traces disabled;server-secret names documented,no values fetched.
- [x] Final local20 PostgreSQL checks PASS:ACL rollback/replay/trigger and unrelated
  grants,schema replay/rollback/checksum guard,seeded-row RLS,anonymous/authenticated
  denial,logical backup/restore and restored denial after hardening. Exact isolated
  network=none rootless container/volumes removed and verified absent. All source
  and raw-artifact hashes independently verified;historical attempts retained.
- [x] Worker six method cases ×five paths=30 request assertions PASS;Node reports
  one passing file wrapper. All six repository validators (migration,backup fixture,
  localization,history paging,policy,ACL proposal),evidence verifier and whitespace PASS.
- [ ] **Billing/0CZK gate:** both providers' current/upcoming charges,invoice totals,
  payment-method absence,paid add-ons/trials/upgrade paths and applicable usage.
  Supabase metadata proves Free only;no current Cloudflare billing tool/CLI.
  Prior owner billing observations remain historical;zero costs NOT proved.
- [ ] **C4 documentary/operational/security evidence:** current SB ToS version/date
  selector,applicability/acceptance,recipient/location/transfer safeguards/TIA,exact
  provider retention/change monitoring;MFA/SSL/exposed-schema/Auth/API/default ACL
  checks;encrypted0CZK destination/custody/schedule/alerts and hosted restore/RPO.
  EUR-Lex anti-bot and Czech Google terms retrieval limitations retained.
- [ ] **Separate owner/security maintenance approval:** exact hosted ACL change,
  retained preflight/postflight and Advisor rerun. Candidate has NOT been applied.
- [ ] **Separate ORD-A synthetic execution approval:** private schema bootstrap,
  minimal runtime role/transport,pinned cf build/dry-run,server-secret upload,
  approved private ingress and synthetic restore/cleanup. No public route shortcut.
- [ ] **Hosted acceptance:** connectivity/TLS,authorization/account isolation/role
  revocation/MFA/CSRF,fail-closed outage/quota behavior,C2§12/CR6 CPU/latency and
  log minimization,encrypted recovery/RPO/RTO,cleanup/billing readback;explicit
  C4/6.0f owner disposition. G1–G9/G11 live integration and6.17 stay open.

Final evidence:`spikes/stage6-cloud-readiness/results/
 goldberg-cloud-readiness-36e27ec5fc4c4750b97cbdee6fb2d1ea/result.json`
(remove the display line break);packet/next steps:
`docs/planning/goldberg-0.11/stage-6/PRIVATE-CLOUD-READINESS.md`;
local overall/stage/AI checklists synchronized. No Worker build/typecheck/cloud
runtime,encrypted drill rerun,Android/Gradle/APK/UI/instrumentation/screenshots
performed. No new APK/hash/signing claim. No remote schema mutations,deploy,
OAuth,real personal data,paid resource,public route,merge,tag,release or Wiki
publication. Existing worktrees/evidence and Stage7 acceptance preserved.
Issue31 OPEN / Goldberg Development In Progress.

Next: review packet and acquire missing read-only billing/security/operational/
legal evidence;record C4 disposition and separate exact phase authorizations
before any hosted write/deployment. Preserve original data on rollback;never
delete/recreate the existing project. Public source refresh remains day-of only.

created by stokonn with love <3

GitHub description and checkpoint synchronized/read back: https://github.com/stokonn/Goldberg/issues/31#issuecomment-6388528121.
Issue31 OPEN / Goldberg Development In Progress verified. Source remains local/uncommitted.
