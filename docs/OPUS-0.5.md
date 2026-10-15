# Opus 0.5 — The Classical Experience

An elegant classical-music practice journal for Android 8.0+ (API 26). Kotlin, Jetpack Compose, Material 3, Room, and MVVM. Application ID `com.opus.piano`; database `opus.db`; version name **0.5**, version code **2** (previous code 1). Existing signing configuration is unchanged.

## What is implemented

- **Home:** real practice statistics, seven-day activity chart, recent repertoire/sessions, and a compact Recently Unlocked badge shelf. No sample statistics.
- **Repertoire:** compact composition rows, accent-insensitive search, learning progress, composer/era/instrument/genre/status filters, and alphabetical/composer/recent-addition/recent-practice sorting. Manual add/edit/delete is preserved. Optional musical metadata can be entered in the editor. Legacy entries without addition timestamps use their original row order as the sorting fallback; historical dates are not invented.
- **Composers:** compact portrait beside an editorial heading, years, era, available birthplace/citizenship, sourced introduction with Read More, personal repertoire/mastery/practice statistics, Open Opus featured works where supplied, and Overview / Works / About tabs. The catalogue count comes from the loaded complete API list; partial data is labeled Works loaded. Works support search, genre/collection filters, title/catalogue sorting, and explicit increments of 40 rendered results. Attribution lives in About → Sources & credits.
- **Composition details:** metadata, available source description and personal notes, Movements, Listen & Study, and Practice sections. Manual and catalogue compositions both have details. Existing status values are unchanged; **Performance ready** is the mastery state. Practice history is preserved and a piece can be selected for practice without replacing an unfinished session's focus.
- **Listen & Study:** clearly labeled generated searches for Spotify, YouTube, IMSLP, and MuseScore, using encoded composer/title/catalogue/movement metadata. Saved links support URL, optional title/performer, inferred platform, save date, add/edit/open/remove, and one preferred interpretation per composition. Links are validated; preferred changes and edits are transactional. Save dates are preserved on edits. Catalogued compositions retain their saved interpretations across repertoire removal/re-addition.
- **Practice:** start/pause/resume, save, discard confirmation, a responsive circular timer, process-recreation checkpoints, and source snapshots retained with new completed sessions. No microphone access or audio recording.
- **Badges:** 16 permanent achievements, deterministic progress, four defined rarity levels, original vector medallions with milled rims, metallic relief, engraved musical motifs/monograms and laurel detailing. Responsive collection grid, category/lock filters, sorting, recently unlocked awards, and detail pages with localized exact verification dates/times. Locked medals are desaturated and never show an unlock date.
- **Wrapped:** preserved preview with real saved weekly activity; monthly/yearly reflections remain a future feature.

No paid services, API keys, accounts, analytics, downloads of recordings, or new library dependencies were introduced. Practice, personal repertoire, recordings metadata, badges, and previously loaded catalogue information remain usable offline. New discoveries and external services require a connection. Repertoire, notes, recordings preferences and practice history are never uploaded. Android cloud backup remains disabled.

## Build and checks

`local.properties` contains the local Android SDK path (excluded from Git). On another machine, configure it for that SDK. The existing Gradle/AGP, SDK 36 and Java 17 bytecode configuration is preserved.

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest lintDebug assembleDebugAndroidTest
python3 scripts/check_migrations.py
# Requires an online emulator or connected device:
./gradlew connectedDebugAndroidTest
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.

Lint report: `app/build/reports/lint-results-debug.html`.

Unit-test report: `app/build/reports/tests/testDebugUnitTest/index.html`.

## Architecture and important files

| Area | Files | Responsibility |
| --- | --- | --- |
| Database | `data/OpusDatabase.kt`, `app/schemas/.../1.json`, `2.json`, `3.json` | Existing repertoire/session entities, safe incremental migrations and repository writes |
| Catalogue | `data/CatalogRepository.kt`, `CatalogModels.kt`, `WikimediaProfile.kt` | Open Opus client, persistent cache, metadata, verified biographies and licensed portraits |
| Music identity | `data/MusicMetadata.kt` | Stable composer identity, existing mastery status, conservative piano identification and metadata helpers |
| Achievements | `data/Achievements.kt`, `AchievementStore.kt` | Extensible registry, pure facts/progress calculations, transactional permanent unlock persistence |
| Recordings | `data/ExperienceData.kt`, `RecordingLinks.kt` | Room entities/DAO, recording transactions, supported URL validation and encoded service searches |
| State and lifecycle | `ui/OpusViewModel.kt`, `CatalogViewModel.kt`, `ExperienceViewModel.kt`, `OpusApplication.kt` | Saved timer checkpoints, debounced discovery, observable badge/recording state and initial existing-data evaluation |
| Screens | `ui/RepertoireScreen.kt`, `ComposerScreen.kt`, `CompositionScreen.kt`, `RecordingsScreen.kt`, `BadgesScreens.kt` | Compact archive, reference shelf, badge collection/details |
| Visual system | `ui/Theme.kt`, `MedalArtwork.kt`, `OpusApp.kt` | Warm charcoal/ivory/gold design, native vector medals, shared components and five-destination navigation |
| Tests | `src/test/.../AchievementEngineTest.kt`, `RecordingLinksTest.kt`, `CatalogTextTest.kt`; `src/androidTest/.../ExperiencePersistenceTest.kt`, `CatalogDatabaseTest.kt`, `DatabaseTest.kt` | Pure calculations, URL contracts, migrations, CRUD and disk-backed persistence |

## Database migration and data preservation

Room advances from schema **2 to 3**, retaining the **1 → 2** migration for older installs. No destructive fallback or table drops are used. Original repertoire/session IDs, notes, statuses and durations remain intact.

- `pieces`: adds `createdAt` (default 0 = unavailable for legacy entries), and optional instrument, genre and musical-era strings. Existing status/difficulty encodings and catalogue links remain compatible.
- `sessions`: adds nullable completion-day/time-zone and catalogue/composer source snapshots, plus a musical-era snapshot. Existing session timestamps/durations are unchanged.
- `catalog_works`: adds cached popular/recommended flags so featured works use actual Open Opus classifications.
- `saved_recordings`: new durable reference links keyed by `catalog:<Open Opus work ID>` or `piece:<local ID>`. No cascading deletion discards saved reference links.
- `badge_unlocks`: new achievement ID and epoch-millisecond unlock timestamp. The DAO only inserts with **IGNORE on conflict**; there is no replacement/update path for unlock dates.

The inspected v1/v2 schemas contain **no quest tables or quest history**. Quests was a UI-only placeholder. Only that user-facing destination was replaced; migration does not delete unknown or unrelated tables.

## Achievement rules

The registry defines a stable ID, name, description, category, rarity, original artwork reference, metric, target, requirement and units for each of the 16 requested achievements. All calculation logic is separate from rendering.

Evaluation runs in the same transaction as repertoire additions/edits/deletions and completed practice writes, and when newly changed catalogue metadata resolves musical identities. Identical cached catalogue rows are not rewritten or re-evaluated. Initial evaluation also runs on upgrade/startup against actual saved facts. Timer ticks, searches, application launches without qualifying data, and recording edits do not create achievement progress.

Unlocks are permanent. Current progress can decrease when an entry is removed or edited, but already-earned medals and their original timestamps remain. Repertoire-growth/mastery thresholds count the current repertoire when verified. Data that was removed before the upgrade cannot be reconstructed. Existing eligible data is awarded at the **first verification timestamp**, not at an invented historical achievement date.

Composer awards use Open Opus IDs **87** (Bach), **152** (Chopin), **196** (Mozart), verified against the API. Linked catalogue IDs take precedence over edited display names. Manual entries can use exact recognized full names (with accent normalization); ambiguous surnames such as “Bach” or “Mozart” alone are not assumed to identify one composer. Piano mastery requires an explicit piano/pianoforte title, description or instrument; the broad Keyboard genre is insufficient. Baroque practice requires five distinct identified works, not repeated sessions or free practice.

### Streak policy

A valid session has at least **1,000 ms** of recorded practice and a positive completion timestamp, matching the existing timer's save threshold. A valid practice day contains at least one such completed session. A session crossing midnight belongs to its **completion day**; launches and unsaved timers do not count.

New sessions store the local completion date and zone ID. Once stored, traveling or changing the device time zone does not move past sessions between days. On initial evaluation, legacy sessions without this metadata receive a date derived from their actual completion timestamp using the upgrade-time zone; the original zone is not knowable. That date is then retained. Multiple sessions on the same date count once. Streak calculations use consecutive `LocalDate` values, so 23/25-hour daylight-saving days work correctly. Progress is the **longest recorded streak**, including a qualifying streak that ended before upgrading.

## Catalogue, sources and licensing

Open Opus remains the primary classical metadata source. Endpoints used: `/composer/list/pop.json`, `/omnisearch/{query}/{offset}.json`, `/work/list/composer/{id}/genre/all.json`, and `/work/detail/{id}.json`. Search uses 350 ms debounce and Unicode/diacritic normalization; responses and known records persist in Room. New discovery is only requested when the catalogue is opened. Search covers cached records offline, not the complete remote database. API search pagination uses the returned `next` offset. The anonymous bulk dump is not used because it lacks the work IDs required for reliable identity.

Responses are reused for 7 days, verified composer profiles for 30 days, with explicit refresh controls and saved-data fallback on network errors. Portraits are persistently cached in app-private storage with bounded downloads and atomic file replacement. HTTPS requests have an identifying User-Agent, allowed hosts, timeouts, serialized access and bounded response sizes. Before broad distribution, add the maintainer's actual contact URL/email to the User-Agent in accordance with [Wikimedia guidance](https://www.mediawiki.org/wiki/Wikimedia_APIs/Access_policy).

- [Open Opus documentation](https://github.com/openopus-org/openopus_api/blob/master/USAGE.md) and [licence](https://openopus.org/): public-domain metadata. Server GPL code is not bundled.
- [Wikidata](https://www.wikidata.org/wiki/Help:Data_access): CC0 identity, precise life dates where supplied, birthplace and citizenship. A unique full-name/alias, birth-year and human-classification match is required before using an English Wikipedia sitelink. Ambiguous identities remain unavailable.
- Wikipedia: complete available introductory text, initially collapsed to four lines. Article, contributor/history and the API-supplied licence links are in About → Sources & credits. The text is unchanged; missing biographies and historical context are not generated.
- Portraits: verified Wikidata P18 → Commons image metadata. Only public-domain/CC0/CC BY/CC BY-SA images with author, source and no stated restrictions are used. Author, credit, source, licence and crop notice remain accessible. There is no arbitrary image search or stock composition artwork.
- Badge medals: original app-native Canvas vectors, drawn locally without third-party imagery.

## Listen & Study behavior

Generated searches are explicitly labeled, not claimed to be verified recordings. Terms use available composer/title/catalogue/movement text and URL encoding. Service availability and access restrictions are controlled by Spotify, YouTube, IMSLP and MuseScore; Opus requires no authentication or service API keys. Links open with an Android `ACTION_VIEW` intent and report a missing handler gracefully.

Saved interpretation URLs are user-provided references, not independently verified performances. Only HTTPS URLs from recognized service hosts and relevant recording/score paths are accepted; credential-bearing, unsupported-port, lookalike-host and search-only URLs are rejected. Platform is derived from the validated URL. Save dates and work association are immutable on edits; a preference change clears the previous preference in the same transaction. Removing a saved link never removes external content. Nothing is scraped, streamed inside Opus, or downloaded as media.

## Verification for 0.5

- `./gradlew assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug`: successful. **18 JVM tests pass** (11 achievement/streak/identity tests, 4 recording/search-link tests, 3 catalogue normalization/key tests).
- `python3 scripts/check_migrations.py`: both **v1 → v3 and v2 → v3 pass** against the actual migration SQL and exported schemas. Legacy data and catalogue cache survive, tables/indices/defaults match Room's v3 export, and repeated unlock insertion retains the first timestamp. This is a host SQLite check, not an Android Room runtime test.
- Android test APK compiles. Tests cover existing CRUD/history, catalogue import duplication, v1/v2 migration, legacy-session date/source backfill, immutable unlocks after reopening, and saved interpretation editing/preference/removal after reopening. **Not executed on a device:** ADB reports no connected devices. No Android UI/runtime testing is claimed.
- Lint: zero errors; remaining SDK/Gradle/dependency update notices are reported in the generated report. Existing pinned versions were retained.

## Known limits and device acceptance checks

Open Opus does not currently supply reliable movement lists or complete composition histories/instrumentation for every work. Those sections explicitly state when data is absent, and direct users to scores rather than inventing movements. Composer biographies show the sourced available introduction, not an internally scraped full encyclopedia article. Cached catalogue coverage and the source's metadata completeness limit offline discovery and metadata-based badge eligibility. Legacy addition dates and original session time zones cannot be recovered.

On an emulator/device, check the following before distributing:

1. Upgrade an existing install without uninstalling. Verify repertoire notes/statuses and session durations, then open Badges and restart to check permanent unlock times.
2. Filter/sort repertoire, edit manual metadata, open a linked composer and work, add a work once, and verify duplicate prevention. Switch off connectivity and revisit loaded profiles and details.
3. Start/pause/resume a session, rotate/navigate, finish and save. Cross midnight or simulate date/zone changes on a test device. Verify completion-day grouping and that a live timer's focus is never silently changed.
4. Open all four service searches, save/edit/remove links and change the preferred interpretation. Restart and reopen the saved reference shelf.
5. Inspect composer tabs, Read More, Sources & credits, large font sizes/small screens, TalkBack labels, bottom/system/keyboard insets, reduced motion, badge filtering and back navigation from every detail screen.
