# Stage 3B acceptance and handoff

Status: implementation and all local mandatory verification complete:78 JVM tests,75 suite entries on each API26/API37, all eight actual process-death/restart cases and both seeded upgrades passed. Publication/tracking are verified in [the completion checkpoint](CHECKPOINT.md); no merge/release or automatic Stage4 continuation.

## Compatibility conclusions

Personal data remains authoritative in opus.db, explicitly migrated9→10. Catalogue schema1 stays optional and rebuildable. Portable v5 carries durable personal UUID/scope/anchor snapshots, settings and raw recovery independently of catalogue availability. Readers1–4 remain supported; legacy imports create a new personal scope because those formats never contained portable UUIDs. A newer personal database cannot be opened by the old0.10.1 APK: rollback means a compatible retained backup and compatible reader, never destructive recreation or opening schema10 with schema8 code.

Exact draft payload bytes and original timestamps are preserved; only a successfully decoded execution identity/status is intentionally regenerated in memory. No microphone, runtime permission, Google credential or cellular grant is restored. Timing inconsistencies remain errors with original bytes intact. Completion evidence is retained even when an earlier journal is restored. No per-piece time is inferred from untimed associations.

## Known design limits

- JSON SHA-256 detects corruption, not malicious authorship; backups remain plaintext.
-32MiB portable limit is retained with complexity/raw-payload bounds, not truncation. Large originals exceeding bounds remain untouched and need a separately designed larger streaming format, not forced repair.
- A bounded JSON tree plus isolated Room staging is used. This is not an unlimited incremental importer.
- Private snapshots/source inputs/recovery evidence intentionally accumulate. No automatic recovery-copy deletion is implemented; storage pressure must fail safely. SAF provider publication cannot guarantee atomic replacement of an external file.
- Pending settings application can partially change individual settings; the durable Room generation/hash/status enables idempotent retry. No global SQLite/preferences/filesystem transaction is claimed.
- Personal anchors/metadata are preservation snapshots, not publication/license evidence or verified provider matches.

## Stage 4 prerequisites

Stage4 requires a separate explicit owner instruction; this stage grants no implementation/deployment/release authority. Start from the verified Stage3B commit and remote CI, preserve historical schemas/fixtures and format1–5 readers, rerun applicable recovery/migration regressions, and follow Stage2 provider/version/provenance/rights allowlists and bounded search contracts. Preserve opaque personal UUIDs and original practice credit during any provider mapping/reconciliation. Catalogue deletion or provider/network/auth failure must not block the journal. Cloud/Admin decisions and0CZK constraints remain separately gated; no paid service/account/production OAuth provisioning was performed here.

No new owner product decision is required to finish the approved local Stage3B scope. Unverified checks, if any, are technical blockers rather than assumed approvals.

created by stokonn with love <3
