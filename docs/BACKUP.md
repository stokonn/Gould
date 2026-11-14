# Goldberg portable backups

## Gould 0.14 development format — not yet released

The 0.14 development branch exports `goldberg-backup` format 7 with journal schema 12. It retains the existing Room `opus.db` identity, adds only the Activity records (`activity_settings`, weekly goals/week metadata, manual Activity entries, cosmetic ownership/reward grants and loadout), and retains readers for formats 1–6. The 11→12 Room migration is additive; no database reset or destructive fallback is used.

Format 7 validates Activity values (week Mondays, dates/zones, durations, known cosmetic IDs, rarity/catalog ownership consistency and equipped-item category) in an isolated Room database before restoration. Legacy journal replacement follows the existing atomic restore path. Activity records merge with `INSERT OR IGNORE` so a restore cannot replace an existing first-use boundary, duplicate a manual-entry ID, revoke owned cosmetics or issue a second reward for the same week. Existing cosmetic ownership remains durable after later progression corrections. A populated v11 upgrade and v7/v6 restore instrumentation are authored and compile but have not yet executed on an emulator; do not interpret host DDL/fixture checks as full Android restore acceptance.

The published Gould 0.13 release uses format 6/schema 11. Format 7 is development-only until a verified 0.14 prerelease is published. Always preserve an original backup before updating; keep audio media archives separate where needed. Backups are not encrypted.

Historical notes below retain earlier format-specific behavior and should not be read as the current development schema.

Goldberg 0.8 exports `goldberg-backup`, format version 2, schema 6. UTF-8 JSON; maximum 32 MiB and 100,000 rows per portable table. All column names/types must match the exported Room schema. SQL and filesystem paths never come from a backup.

Portable tables: pieces, sessions, catalog_composers, catalog_works, catalog_cache, saved_recordings, badge_unlocks, session_parts, profile_settings, session_intervals. IDs, original notes, historical practice dates/zones, profile/avatar, appearance, smart preferences, badge first timestamps and showcase order are preserved. Portrait files, practice_drafts and local_app_state (onboarding/reminder activation) are excluded.

Format-1/schema-4 and schema-5 backups from Goldberg 0.6/0.7 remain accepted. Explicit conversion adds empty session notes/reflection, Manual/Legacy timing semantics, null elapsed/detected measurements, and default Smart preferences; no historical duration is recomputed. Schema 4 additionally receives typography/statistic defaults. Unknown versions/tables/columns are rejected. Earlier apps cannot read format 2: retain original pre-upgrade backups.

Validation stages values in an isolated in-memory Room database, checking enums, keys, foreign keys, durations, overflow, dates/zones, profile/avatar, showcase and link constraints. New timing rows must be ordered/contiguous, bounded by elapsed time, use valid part indexes/reasons and reconcile to both session and per-piece credited totals. Detected time is required for Smart/Mixed and absent for Manual. Legacy unknown measurements are not invented.

Export reads portable data in one transaction. Restore validates again, then replaces portable tables and clears the unfinished draft in a single transaction. Device-local state is retained. The application coordinator stops capture and serializes mutations during replacement; catalogue downloads are serialized against restore. Failed replacement rolls back. Cache maintenance failure after successful replacement does not falsely report that old portable data was retained. Repeated import cannot accumulate duplicates.

Restoration requires explicit replacement confirmation, including the unfinished-session warning. It does not merge journals, activate reminders or start microphone capture. Backups and CSV are unencrypted. Android automatic cloud backup remains disabled; keep independent copies. Cancelling a document picker does not change data. A selected cloud document provider may upload the file independently of Goldberg.

## Goldberg 0.9 compatibility

The redesign retains Room schema 6 and both supported backup formats without changing portable journal fields. New accent, card, section layout, metric and repertoire presentation preferences use a separate device-local appearance store and are intentionally outside portable backups. Appearance reset never replaces journal data. Existing profile/theme fields retain their prior backup behavior. Real-device restore execution remains unverified for this beta; host legacy-fixture and migration checks passed.
