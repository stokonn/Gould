# Goldberg CSV reports

Settings exports either sessions or piece times using Android CreateDocument. No storage permission or app-controlled upload is used. Your chosen document provider may synchronize files. Each file is a consistent database snapshot with UTF-8 BOM, CRLF records, comma separators, quoted text, doubled embedded quotes and multiline quoted notes. Null measurements are empty fields, not zero. Numeric durations are integer milliseconds; divide by 60000 for minutes. UTC timestamps use ISO 8601; practice_day/practice_zone retain historical calendar grouping where available.

Session columns:
`session_id,started_at_utc,completed_at_utc,practice_day,practice_zone,mode,timing_quality,credited_ms,elapsed_ms,detected_active_ms,manual_pause_ms,auto_pause_ms,interruption_ms,piece_count,session_notes,reflection`

Piece columns:
`session_id,part_id,practice_day,piece_id,catalog_work_id,title,composer,credited_ms,piece_session_notes`

A session total appears only in the session file. Piece credited_ms values sum to its session credited_ms; never add both files together. Legacy elapsed/detected/pause measurements remain empty. auto_pause_ms includes uncredited listening/silence around automatic pauses, not just time after the configured threshold. Incomplete recovered sessions exclude unknown process gaps; the timing_quality column discloses this.

Potential formula text (=,+,-,@ after leading whitespace, or leading tab/newline) receives an apostrophe before CSV quoting. This intentionally changes spreadsheet interpretation for safety. CSV is a report, not a lossless backup or import format. Failed/cancelled writes can leave a partial document; retry with a new document. Goldberg never changes journal data during export.
