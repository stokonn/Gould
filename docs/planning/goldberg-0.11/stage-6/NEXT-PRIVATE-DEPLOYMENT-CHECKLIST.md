# Next private deployment checklist — NO-GO (2026-10-25)

Purpose: collect exact missing evidence before any remotely mutating private
verification. These boxes are acceptance gates,not instructions to execute now.
Source publication and green CI do not authorize deployment. Preserve original
project,data,worktrees and historical evidence;synthetic data only;0CZK mandatory.

- [ ] **C4 D2 / terms:** refresh all33 C4 sources immediately before execution;
  resolve/record current Supabase ToS version/date-selector limitation and actual
  agreement/DPA applicability/acceptance. Record changes and explicit disposition.
- [ ] **C4 D3 / billing:** sanitized read-only evidence for BOTH providers showing
  Free plan,current/upcoming charges/invoices=0,no payment method,no paid add-ons,
  chargeable trial or auto-upgrade;actual usage/quotas including tests,logs,exports.
  Public pricing and Supabase Free metadata alone are insufficient.
- [ ] **C4 D4 / logs:** inspect actual field/configuration/retention;exclude URL/query/
  headers/cookies/tokens/payload/IP identifiers. Approve minimal logging and
  Free limits,including the announced2026-12-01 observability transition. Local
  template is OFF;no hosted minimization or retention proof exists.
- [ ] **C4 D5 / backups:** resolve Free-backup entitlement versus generic DPA
  wording;approve0CZK encrypted destination,custody,scheduled exports,age alerts,
  retention,isolated restore and cleanup. TargetRPO≤24h;measure achieved RPO/RTO.
  Local plaintext synthetic restore does not establish these operations.
- [ ] **C4 D6 / security:** fresh region/health/catalog/exposed-schema/default ACL
  evidence,SSL enforcement,operator MFA/offline recovery,Auth/API posture. Retain
  exact rls_auto_enable definition/OID/owner/search_path/ACL/trigger snapshot.
  Both Advisor warnings remain unresolved remotely. No production keys fetched.
- [ ] **C4 D7 / privacy:** actual service/recipient/data-category/location mapping,
  transfer safeguards/certification scope/TIA access,quantified provider edge/
  dashboard/security retention and assigned change monitoring. Resolve applicable
  legal-source retrieval gaps with reviewer disposition. No EU-only claim.
- [ ] **C4 D1 / operator:** carry bounded Q1-D/D5b owner-reported resolution only;
  reassess changed scope/terms. Do not reopen resolved eligibility without cause
  or infer general compliance. No identity/reviewer details requested or published.
- [ ] **C4 D8–D9 / owner closure:** reconcile every row and retain explicit C4
  disposition plus phase-scoped authorization under ORD-A. ORD-A is sequencing
  approval,not deployment permission. Existing project existence grants no write.
- [ ] **Security maintenance approval:** owner/security approval for exact narrow
  ACL candidate. Future authorized execution requires private preflight,transactional
  guard/postflight,trigger functionality/unrelated-grant checks and Advisor rerun.
  Stop/rollback on drift. Never replace/drop the function/event trigger or broadly
  restore PUBLIC grants. Do not apply this change as part of documentation push.
- [ ] **Schema/runtime approval:** separate authorization for private bootstrap,
  least-privilege runtime-role/transport design,server-only secrets and short-lived
  synthetic infrastructure. Keep intake/APIs disabled;never run as postgres,expose
  the private schema,grant service_role access or add paid connectivity shortcuts.
- [ ] **Private ingress approval:** review and prove a Free authenticated private
  probe path. workers.dev/previews OFF and zero routes are an inert configuration,
  not proof of authenticated private access. No public endpoint may be activated.
- [ ] **Hosted acceptance approval/execution:** only after prerequisite evidence and
  authorization,verify TLS/connectivity,anonymous/account A→B denial,role revocation,
  sessions/MFA/CSRF,seeded RLS/ACL,direct RPC denial,network/pause/timeouts,quota
  fail-closed behavior,C2§12/CR6 CPU/latency/logging and encrypted restore/cleanup.
  No deliberate provider-quota exhaustion. Retain exact source/commands/results.
- [ ] **6.0f owner acceptance:** review actual runtime/billing/recovery results and
  explicit acceptance before lifting this gate. G1–G9/G11 consumer integration,
  6.17 API freeze,OAuth,real-data collection,Stage8 integration and release remain
  separate decisions. No public service activation follows automatically.

On any unknown cost,security/privacy/terms failure:remain NO-GO;no paid fallback.
Incomplete SQL rolls back. After commit,preserve original data and fix forward/
restore in isolation;never DROP/recreate/overwrite the existing project. Cleanup
may remove only explicitly authorized disposable synthetic resources;record
credential revocation,resource absence and final billing observations.

created by stokonn with love <3
