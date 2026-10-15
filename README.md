# Goldberg 0.6.1 — Profile Reimagined

Goldberg is the next version of Opus, a private classical-music practice journal for Android 8.0+ (API 26). Built with Kotlin, Jetpack Compose, Material 3, Room and MVVM. No new dependencies, accounts, analytics or paid services.

Upgrade identity stays **`com.opus.piano`**, database **`opus.db`**, version code **4** (Goldberg 0.6.0 used 3; Opus 0.5 used 2). Internal `Opus` class names and stable catalogue, repertoire and achievement identifiers remain compatible. Signing configuration is unchanged. Never uninstall or clear storage to upgrade. The debug APK is suitable for upgrading an installation signed with the same debug certificate; a differently signed distribution requires its original signing key.

This is a **pre-release debug build**. Profile’s app bar places the GOLDBERG wordmark on the left and Settings on the right, without Search. Settings contains Appearance and Backup & Restore. Search remains available from Repertoire and other relevant screens. Bottom tabs select their root screen, dismiss search focus/keyboard, and do not resurrect a saved Search stack. Settings Back returns to the existing Profile state.

Schema 4 and format-1 backups are unchanged from 0.6.0; no database migration is needed. The pre-existing local `RELEASE_NOTES.md` remains the 0.6.0 description. Current release notes are in [docs/RELEASE-0.6.1.md](docs/RELEASE-0.6.1.md).

## Features

- **Compact Profile:** circular avatar and display name, dedicated Edit Profile dialog, responsive lifetime statistic cards, current calendar-day streak, period activity charts, most-practiced works and expandable session history. Week means the last 7 days, month the last 30, year the last 365; all-time charts group older activity into at most 12 bins. History loads 20 visible sessions at a time.
- **Badge showcase:** view up to five selected unlocked badges in a compact horizontal shelf; use Edit to select, reorder or remove badges, and tap badges for details. Selection and order persist locally and are backed up. The separate Badges destination retains all 16 achievements, filtering, progress, rarity and original first-verification dates.
- **Multi-piece practice:** use the focus menu to select several repertoire pieces before starting, then tap a selected piece to switch. Add or remove pieces while running or paused. Free practice is always available. Every monotonic time interval belongs to exactly one active entry. Pauses add no time. Removing a piece retains its recorded time and notes in the saved summary. Notes are per piece within the session. Save once for one session; expand Session summary in history for all times and notes. Interrupted processes restore the last saved checkpoint as paused, without counting time while dead.
- **Universal search:** the top search button opens categorized repertoire, composer and composition results. Repertoire matches respond immediately; catalogue discovery is debounced 350 ms. Search ignores accents, uses cached information offline and can discover additional catalogue results online. It does not imply that the entire remote catalogue is stored locally.
- **Backup & restore:** the dedicated Settings screen uses Android’s Create/Open Document pickers. A complete, versioned JSON snapshot includes repertoire, sessions and per-piece notes/times, badge unlocks/showcase, username/avatar/theme, saved interpretation links and catalogue cache. Restore validates in an isolated in-memory Room database before offering a confirmation and validates again before an atomic replacement. Invalid files leave saved data intact. Restore replaces rather than merges, so repeating a restore does not duplicate rows. Export first if you want to keep the current journal. An active timer is discarded only after a confirmed, successful restore.
- **Appearance:** Light, Dark and Follow system (System), persisted in Settings; high-contrast text, scalable typography and matching system-bar contrast. Android animation settings control motion. Navigation fades, repertoire item changes, addition confirmation, saved-session feedback and new badge notices remain lightweight.
- **Identity:** warm ivory, geometric harpsichord keyboard adaptive icon, a separate monochrome mask for themed launchers, and the platform’s native Android 12+ splash. No splash activity, artificial delay or media assets.

Existing repertoire editing, filters/sorting, sourced composer biographies/portraits/credits, composition details, recording/score searches, saved preferred interpretations, badge collection and Wrapped preview remain. Per-composition and composer practice statistics include multi-piece attribution. Global statistics, streaks and session achievements count the parent sessions once. The Baroque badge counts distinct works with at least one second of attributed practice.

All personal data stays on the device unless the user exports a backup through their chosen document provider. Android automatic cloud backup remains disabled. Gallery access uses the system picker, without broad storage permissions. Avatars are copied as bounded JPEGs; the source gallery image is never modified. Exported backups are plain JSON, not encrypted. Store them privately.

## Build and checks

Use the existing Android SDK 36, Java 17 bytecode target and pinned Gradle/AGP configuration. Configure `local.properties` for the local SDK.

```bash
./gradlew assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug
python3 scripts/check_migrations.py
# On a connected test device/emulator:
./gradlew connectedDebugAndroidTest
# Build and copy to the requested local filename (does not publish):
./scripts/build-apk.sh
```

Named APK: **`app/build/outputs/apk/debug/Goldberg-v0.6.1.apk`**. Standard Gradle output: `app/build/outputs/apk/debug/app-debug.apk`. Reports: `app/build/reports/tests/testDebugUnitTest/index.html`, `app/build/reports/lint-results-debug.html`, and (when executed) `app/build/reports/androidTests/connected/debug/index.html`.

## Migration and backup contract

Room schema **4** retains the complete **1 → 2 → 3 → 4** upgrade chain. The new migration only adds:

- `session_parts`, with a cascading foreign key to the parent session and an index on `sessionId`. Existing session rows are untouched. Each old session gets a matching part containing its original piece/title/duration and source snapshots. No foreign key to repertoire: deleting a repertoire piece preserves history.
- `profile_settings`, a single local profile with username, bounded avatar bytes, theme and ordered showcase IDs. Missing settings mean default profile and System theme.
- Indexes on `sessions.completedAt` and `sessions.pieceId`.

A completed session and its parts are inserted in one Room transaction together with achievement evaluation. Sum of part durations must equal the parent duration; negative durations are rejected. Existing unlock timestamps use insert-ignore semantics and remain permanent. Historical local-day/time-zone policy from Opus 0.5 remains unchanged.

Backup format details are in [docs/BACKUP.md](docs/BACKUP.md). This first Goldberg backup format does not import arbitrary SQLite files or unsupported future schemas. Derived downloaded portrait files and an unfinished timer are excluded; saved biography/cache metadata and completed practice are included.

## Architecture

| Area | Files under `app/src/main/java/com/opus/piano/` |
| --- | --- |
| Room/migrations and repository | `data/OpusDatabase.kt`, `data/PersonalData.kt`, exported schemas 1–4 |
| Timer and attribution | `ui/OpusViewModel.kt`, `data/PersonalData.kt` |
| Profile, appearance and file pickers | `ui/ProfileViewModel.kt`, `ui/ProfileScreen.kt`, `ui/Theme.kt` |
| Validated snapshot backup | `data/BackupRepository.kt` |
| Search/catalogue | `ui/CatalogScreens.kt`, `ui/CatalogViewModel.kt`, `data/CatalogRepository.kt` |
| Badges and saved recordings | `data/Achievements.kt`, `data/AchievementStore.kt`, `data/ExperienceData.kt` |
| Navigation and existing screens | `ui/OpusApp.kt`, repertoire/composer/composition/recordings/badges screens |

[CHANGELOG.md](CHANGELOG.md) records the update. [docs/OPUS-0.5.md](docs/OPUS-0.5.md) preserves historical architecture, source licensing, achievement rules and earlier verification. Open Opus public-domain metadata and Wikipedia/Wikimedia/Wikidata source credits remain in the app. No screenshots existed in the project.

## Verification and limitations

See [docs/VERIFICATION-0.6.1.md](docs/VERIFICATION-0.6.1.md) for current results, measurements and remaining device checks. Host migration checks are SQLite checks against actual migration statements and exported Room schemas; they do not substitute for an Android upgrade test. Instrumentation covers upgrading a seeded 0.5 schema, disk reopening, multi-piece persistence, repeated restore into an existing journal, invalid backup rejection and original unlock/link retention.

Data flows still load complete saved session lists for aggregate statistics; visible history and catalogue rows are bounded/lazy, but extremely large journals may warrant paged database aggregation in a later measured update. No speculative database rewrite or new library was introduced. Native splash is provided on Android 12+; earlier Android versions use a matching launch-window background. Catalogue metadata completeness and external service availability remain source-dependent; Wrapped monthly/yearly storytelling is the existing future preview.
