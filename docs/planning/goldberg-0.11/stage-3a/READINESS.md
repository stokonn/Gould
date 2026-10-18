# Stage 3B prerequisites

Stage 3A is a local persistence bridge. The verified result is in [VERIFICATION.md](VERIFICATION.md), with implementation boundaries in [IMPLEMENTATION.md](IMPLEMENTATION.md). Stop after Stage 3A; Stage 3B needs a separate owner instruction and a branch containing its final commit. Do not start providers, downloads, cloud work or UI redesign implicitly.

1. Verify the actual personal schema (Stage 3A:9) and independent catalogue schema1, branch ancestry, CI and protected user work. Preserve all historical schema exports and migration registrations.
2. Implement portable backup v5 only in Stage 3B. Keep readers1–4 and exact old fields. Include the journal UUID registry, anchor snapshots and journal scope; independent catalogue DB/files must remain unnecessary for opening a backup. Format4 cannot preserve these UUIDs on another installation and is not an extended backup.
3. Build complete Room-compatible v5 golden fixtures, including every critical row/setting, malformed opaque draft bytes, recovery archives, multiple timed parts, untimed links, manual arrangements, removed pieces, numeric legacy keys and provider collisions. The Stage 2 minimal envelope remains insufficient as a full backup fixture.
4. Preserve the pending-draft rejection safety gate until replacement can durably retain the original destination recovery payload. Preserve destination archives as evidence; never interpret confirmation to replace a journal as consent to destroy unreadable recovery. Completed+draft token conflicts must remain exportable and must not earn duplicate credit.
5. Validate/hash/bound inputs in isolated staging before one authoritative personal transaction. Add a tested, idempotent preference replay record rather than assuming SharedPreferences and the journal can commit atomically. Restore paused with a new execution identity and no microphone or restored OS/network/auth permission. Catalogue reconstruction remains independently retryable.
6. Test format1/schema4–5,2/schema6,3/schema7,4/schema8 and full/minimal v5 imports; repeated imports/exports, interrupted phases, rollback, malformed/oversize inputs, completion-token conflict, unchanged original recovery bytes, snapshots and preferences. Rerun historical Android migrations, final seeded upgrade, lint/JVM/compatibility checks and min26/current emulator suites.
7. Document downgrade constraints, source/artifact identity and exact verification. Keep issue31/milestone4/Project2; update only verified English Wiki details, stop at Stage 3B and do not release/merge without the later required authorization.

D4 backend, D5 operator/community processing and D8 Admin choices remain deferred to later cloud stages. No owner product decision blocks local backup work; technical acceptance gates above remain required. Strict0 CZK/month is unchanged.

created by stokonn with love <3
