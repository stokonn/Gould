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

