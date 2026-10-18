# Portable backup v5 — implementation contract

Status: implemented on the Stage 3B branch, acceptance verification in progress. This is unreleased development, not production 0.10.1 functionality. See [verification](VERIFICATION.md).

## Identity and databases

The Android application remains `com.opus.piano`, version0.10.1/code11 with the existing signing configuration. Personal `opus.db` advances explicitly from Room9 to10; the separate optional `goldberg-catalogue.db` remains schema1. Historical schema exports and migrations remain retained. No destructive migration, cross-file foreign key or distributed transaction is introduced.

Migration9→10 adds `backup_restore_state`, `recovery_evidence` and `practice_completion_evidence`. It records existing nonempty session completion tokens with their durations without changing the original records or timestamps. An insertion trigger retains subsequent completion evidence. Different durations for a token remain separate evidence, never an instruction to invent credit. Existing repertoire IDs, registry UUIDs, scope and numeric Open Opus IDs are unchanged.

## Portable envelope and included fields

UTF-8 JSON carries format `goldberg-backup`, version5, journalSchema10, createdAt, sections and SHA-256. Every section has version1, data and its own SHA-256. Canonicalization sorts object keys; arrays retain order, integers remain signed64-bit and strings preserve content. Duplicate fields, floating numbers, invalid UTF-8/surrogates, unexpected sections and unsupported versions are rejected. Hashes detect corruption; they are not signatures, encryption or proof of authorship.

| Section | Included |
|---|---|
| journal | Complete pieces, sessions, session_parts, session_intervals, session_piece_links, notes/reflections, historical title/composer snapshots, completion tokens, favorite_composers, badge_unlocks, saved_recordings, profile_settings; legacy catalog_composers/catalog_works needed by existing UI. |
| identities | All personal_catalogue_identities, personal_catalogue_anchors and the personal_catalogue_state scope, with exact UUIDs, legacy keys, original title/creator and mapping evidence. Orphan anchors intentionally survive deletion of an owner. |
| preferences | Allowlisted appearance accent/cards/home section order/hidden sections/two metrics/profile order/hidden sections/repertoire density/sort; desired CZ/EN/system locale; previousMeteredConsent as evidence only. Local onboarding/reminder desired enabled/hour/minute. |
| recovery | Single active draft and all legacy archives as exact base64 bytes plus SHA-256, original IDs and timestamps; raw recovery_evidence with origin/classification/token; completion evidence ledger. |

Profile settings already include username/avatar, theme/typography/statistics layout, favorites/biography, Smart Practice enabled/sensitivity/pause, and badge showcase. Badge unlock dates are separate journal rows. User text is never translated. No personal field is truncated to fit the format.

Excluded: independent catalogue content/indexes/generations/media, legacy catalog_cache, downloaded files, authentication/OAuth tokens/accounts/login sessions/Google avatar provenance, administrator roles, secrets, private network credentials, platform microphone/notification/storage permission grants. Local reminder last-fired day and journal generation are transient: last-fired day resets and destination generation advances. The completion ledger and recovery evidence are unioned with destination evidence, not erased. All other SharedPreferences keys are excluded by default.

Prior mobile consent never activates cellular downloads on import, even on the same installation. Reminder preferences are desired settings, subject to current platform permissions; import requests no permission. Profile name/avatar are marked manual so a later unrelated Google identity refresh cannot silently replace the restored values. No Google login or provider access is necessary.

## Validation and replacement protocol

1. Receive a bounded immutable private input file, retaining source bytes. Parse strictly and validate hashes, structure, versions, settings, IDs/types/references/UUID uniqueness and timing invariants. Construct an isolated in-memory Room10 staging database; validate foreign keys and original semantic journal rules. No authoritative write occurs during staging.
2. Serialize restores with the repository mutex and the existing PracticeCoordinator replacement gate. Stop capture; checkpoint an existing readable active draft. A malformed destination draft may be replaced only through the explicitly recovery-preserving backup path, never the ordinary discard path.
3. Atomically publish a complete private destination snapshot. Failure to save it aborts replacement. Preserve original incoming input and a canonical source copy. Do not treat generic import confirmation as permission to erase original recovery data.
4. In one personal Room transaction: preserve destination raw draft, archives and identity context; suspend the bridge triggers; replace authoritative journal/UUID rows; union raw recovery and completion evidence; retain destination archives with original IDs in evidence even if numeric archive IDs collide; reinstall bridge capture. For formats1–4, retain the previous registry as unanchored evidence and mint a fresh scope for the imported journal. For v5, replace the active registry with the source registry and retain destination identity context in portable raw evidence even when there was no draft/archive. Before commit, raise future SQLite owner-ID allocation floors to cover retained/tombstoned anchors and known piece references. Existing IDs and UUIDs stay unchanged; a fresh restored database must not reuse a deleted owner ID and inherit its old anchor. No catalogue or filesystem write is claimed atomic with this transaction.
5. For v5, persist desired preference JSON/hash/generation and pending status in that same transaction. After commit apply allowlisted SharedPreferences, locale and reminder scheduling idempotently, explicitly disabling mobile consent. Acknowledge applied status in a further Room transaction only after application succeeds. Startup retries pending work; Settings exposes pending status and retry. Another import is blocked while preference application is pending. Export uses desired settings while pending.

Failure before journal commit leaves the original journal authoritative; the snapshot/input copies may remain. Failure after commit leaves a durable pending state and restored journal available. Applying settings twice is safe. Cancellation or process termination cannot justify declaring successful application prematurely. AtomicFile preserves the prior private published file on failed publication; SAF/document-provider streams cannot promise atomic replacement. A partially written external destination may need selecting again, while the private complete snapshot remains.

## Recovery and practice credit

Opaque drafts, including invalid UTF-8 or unknown versions, are retained byte-for-byte rather than repaired. Successfully decoded drafts recover paused with a fresh transient execution identity; token, original timing and content remain, and the original payload is archived before a new checkpoint replaces it. No microphone starts automatically. Preparatory selections/notes with no token and zero elapsed/credited time recover without inventing a started session; their derived completion evidence is null. A duplicate completion lookup leaves an unrelated draft untouched. Failed import from an empty idle journal does not fabricate a checkpoint. Unreadable drafts remain in a recoverable error state with unchanged-byte export and separate archive confirmation.

An imported token with already completed matching duration is archived rather than offered for renewed practice credit. Conflicting durations preserve both records, block automatic recovery/completion, and require recovery export/explicit handling. Replacing the journal with an older backup cannot revive a previously completed token because destination completion evidence is retained. These rules do not rewrite historical practice credit or infer per-piece duration from untimed associations.

Evidence ID conflicts preserve both payloads, deterministically rekeying the incoming record and retaining the original ID in origin. Large raw BLOB/TEXT evidence is read in64KiB chunks to avoid Android CursorWindow truncation. Catalogue reconstruction is lazy and retryable; imported scope changes force a fresh optional catalogue generation. Personal rows/UUIDs remain sufficient to render legacy journal/repertoire offline without that database.

## Bounds, storage and privacy

Historical input limit stays32MiB; v5 input and canonical output also stop at32MiB. Validation additionally bounds nesting24, JSON nodes500,000, array/table rows100,000 and raw payload reads24MiB. Bounds reject safely and never shorten notes or evidence. Output is serialized to a file with a size-counting pass; input is streamed to a bounded file before object validation. Staging still uses a bounded JSON tree and Room memory database, not an unlimited streaming importer. Measurements and limitations are recorded separately; these are safety ceilings, not arbitrary advertised journal quotas.

Before private export, available storage must cover the measured output plus64KiB; concurrent filesystem consumers can still cause ENOSPC, handled through atomic rollback. Repeated input/import/previous snapshots are deliberately retained as critical recovery copies. There is no automatic GC or indefinite-storage promise; storage pressure fails safely rather than deleting originals. Backups are plaintext personal data and may contain private avatar/notes/drafts; the user chooses a trusted storage location. No new telemetry or remote upload exists.

## Fixtures and stage boundary

Complete deterministic synthetic CC0 fixtures derive from the actual Room10 export; historical fixture files remain unchanged. Fixtures exercise arrangements, selectable movements, a medley and multiple timed/untimed associations without requiring external catalogue metadata. These are personal backup tests, not new provider adapters or catalogue reconciliation. Stage4, cloud, authentication and downloads remain unimplemented.

created by stokonn with love <3
