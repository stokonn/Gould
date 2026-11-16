# Community draft recovery policy

The recovery archive is user initiated, local-only JSON in the separate
`gould-community-drafts` format version 1. It does not change backup formats
1–8, either Room database, credentials, catalogue records, or server state.
Detailed screen behavior and known limits are documented in
[`COMMUNITY-DRAFT-RECOVERY.md`](COMMUNITY-DRAFT-RECOVERY.md).

## Required guarantees

- Bound input and output to 16 MiB; reject malformed UTF-8/JSON, unknown or
  missing keys, unsupported versions, invalid IDs/enums, duplicate draft IDs,
  invalid frozen request bodies, and structurally invalid records before any
  destination write.
- Preserve draft ID/version, incomplete editable payloads, exact frozen wire
  body/idempotency key/retry outcome, and receipt. Never submit during import.
- Principal archives can be restored only to the same authenticated principal.
  Guest archives can be restored only while unauthenticated and are rebound to
  the destination installation's guest owner.
- Reject any pre-existing destination ID and abort the whole archive before
  publication. Stage all files, publish atomically per file under the store
  lock, and remove earlier publications and newly created folders if a later
  publication fails.
- Warn that exports are unencrypted and can contain private pending-request
  data. Do not route archive bytes through the network or portable backups.

## Verification

`CommunityRepositoryTest` covers principal and guest round trips, frozen
requests/receipts, account mismatch, empty archives, malformed and incomplete
records, duplicate and existing IDs, no-partial-write behavior, and rollback
after an injected mid-publication failure. This suite passed in the Stage 2
INTEGRATION run recorded in `CHECKPOINT.md`. Android SAF picker interaction is
not emulator-tested; owner device acceptance remains separate.
