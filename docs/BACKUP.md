# Goldberg local backup format 1

UTF-8 JSON, maximum 32 MiB. Root fields:

```json
{
  "format": "goldberg-backup",
  "version": 1,
  "schema": 4,
  "createdAt": 1792100000000,
  "tables": {
    "pieces": [], "sessions": [], "catalog_composers": [],
    "catalog_works": [], "catalog_cache": [], "saved_recordings": [],
    "badge_unlocks": [], "session_parts": [], "profile_settings": []
  }
}
```

Each table array contains JSON objects with exactly the exported Room schema-4 column names. SQL INTEGER values are JSON integers, TEXT values strings, SQL NULL values JSON null. IDs are retained. `profile_settings.avatar` is base64 JPEG data (maximum 512 pixels per side); `showcase` is an ordered comma-separated list of at most five distinct unlocked achievement IDs. Profile theme is System, Light or Dark. Session parts retain piece/composer/title/source snapshots and notes. The `practiceDay` and `practiceZone` fields retain original calendar grouping.

All nine tables are required, even when empty. Unknown tables/columns, missing columns, null required fields, bad enums/types, duplicate primary or unique keys, broken session-part foreign keys, negative or inconsistent durations, invalid dates/zones, invalid profiles/avatars and unsupported format/schema versions are rejected. Maximum 100,000 rows per table. Validation uses a fresh in-memory schema-4 Room database, then checks time totals and profile semantics. The original database is untouched during validation.

Export reads all tables in one transaction. Restore repeats validation and then deletes/reinserts the permitted tables in one transaction after user confirmation. Inserts use bound values and a fixed table/column allowlist; no SQL, filesystem path or executable statement comes from the file. Any failed import rolls back the entire replacement. Repeated replacement cannot accumulate duplicates. Restore does not merge journals or synthesize historical badge dates. First-verification times in the selected snapshot are retained exactly.

Statistics are reconstructed from completed session headers and piece entries. Catalogue biographies/metadata caches and saved links are included. Downloaded portrait image files are derived cache and excluded; they can be downloaded again. The avatar is included, independent of gallery URI grants. The unfinished timer, UI navigation and transient searches are excluded. No persistent preferences existed outside the Room database in Opus 0.5.

Backups are not encrypted or uploaded automatically. A document provider chosen in the system picker may be a cloud provider. Cancelling either picker leaves local data unchanged. Exporting does not delete local data. Before replacement the confirmation shows repertoire/session/unlock counts and warns that current saved data will be replaced and an unfinished timer will be discarded on success.
