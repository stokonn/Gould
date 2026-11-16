# Community draft export and recovery

Gould's **Settings → Community drafts** screen offers owner-directed JSON
export/import through Android's system document picker. The archive is a
separate `gould-community-drafts` version 1 format; it does not alter or replace
portable backup formats 1–8, either Room database, or catalogue data.

Export preserves draft IDs, versions, editable payloads, pending immutable
request bodies/idempotency keys/retry outcomes, and receipts. Archives are
bounded to 16 MiB and are **not encrypted**; pending requests can contain
private evidence, so owners should store them securely.

Import reads and validates the entire archive before writing. It rejects
unsupported or incomplete structure, malformed records, duplicate IDs,
oversized archives, incompatible account scope, and any ID already present in
the destination. A conflict rejects the whole import and leaves existing
drafts unchanged. Files are staged before publication, and a publication error
rolls back files already published in that attempt. Same-principal archives
restore only while signed in to that exact principal. Guest archives restore
only into guest storage and are rebound to the current installation's guest
owner; they cannot enter an authenticated account. Empty archives are valid
no-ops.

Frozen pending CREATE/REVISE/WITHDRAW requests are checked against the stored
payload and reconstructed wire request before import. They remain frozen and
retain their idempotency identity after recovery. Editable drafts may be
intentionally incomplete and remain editable after restoration; validation is
applied when preview/submission is attempted. Import never submits requests,
contacts a server, merges over an existing draft, or changes credentials.

Automated repository tests cover account/guest round trips, pending requests
and receipts, empty/malformed/incomplete/duplicate/conflicting archives, and
rollback on an injected publication failure. Android document-picker runtime
behavior still requires device acceptance.
