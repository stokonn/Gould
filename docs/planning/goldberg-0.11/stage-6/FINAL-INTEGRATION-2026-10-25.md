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
- [ ] Implement and test BFF composition and bounded Fetch transport.
- [ ] Verify connection lifecycle, policy/kill-switch, canonical routes and errors.
- [ ] Implement controlled client transport/adoption in an isolated integration branch.
- [ ] Run backend, Android, operations and feasible emulator regression matrix.
- [ ] Prepare revised exact hosted packet excluding completed SQL operations.
- [ ] Publish verified source, read back remote SHA and inspect corresponding CI.
- [ ] Reconcile 24 requirements, G1–G11, C4, handoffs, Issue/Project and private PMP.

Current status: IN PROGRESS. No new hosted mutation, credential provisioning,
Worker deployment or remote session activation authorized or performed.
Owner-supplied Free/no-card evidence applies to both providers; billing403 is an
automated visibility limitation. Operational Pi/SD backup automation is deferred;
production real-data collection remains blocked. Synthetic developer verification
must be assessed separately from production privacy and backup acceptance.

Verification recorded above is hosted read-only catalog/Advisor observation only.
New source tests have not yet run. Earlier evidence remains historical and intact.

created by stokonn with love <3
