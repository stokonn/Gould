# Current-state source audit and migration risk

**Verified at `556970ab7407ec6986e3678657adbd2168516468`, 2026-10-18.** Links resolve to the audited repository tree when this planning branch is checked out. No production changes are made in Stage 1.

## Application and build

[app/build.gradle.kts](../../../app/build.gradle.kts) declares versionName **0.10.1**, versionCode **11**, applicationId/namespace **com.opus.piano**, minSdk 26, compile/targetSdk 36, Java/Kotlin JVM target 17. [Root plugins](../../../build.gradle.kts): AGP 9.1.0, Compose Kotlin plugin 2.2.10, KSP 2.3.12. [Wrapper](../../../gradle/wrapper/gradle-wrapper.properties): Gradle 9.3.1. Single `:app` module; dependency versions are literal in Gradle, not a version catalogue. No custom lint configuration or baseline was found in tracked build files.

Dependencies include Compose BOM 2025.08.01/Material 3, Navigation 2.9.3, Lifecycle 2.9.2, Room 2.7.2, Credentials 1.5.0, Google ID 1.1.1, JUnit 4.13.2 and AndroidX test runner. No WorkManager, dedicated image-loading/network library, MusicBrainz SDK, Supabase SDK, web app or backend deployment configuration exists. Do not add dependencies merely to document proposed architecture.

[Manifest](../../../app/src/main/AndroidManifest.xml): OpusApplication, MainActivity, foreground microphone service, private action/reminder receivers; INTERNET, ACCESS_NETWORK_STATE, RECORD_AUDIO, foreground-service/microphone, notification and boot permissions. Android automatic backup is disabled. Existing package/component identities and trusted signing certificate are upgrade contracts; preserve them. Package areas: `data`, `practice`, `audio`, `identity`, `preferences`, `reminders`, `service`, `i18n`, `ui`.

## Persistence inventory

[OpusApplication](../../../app/src/main/java/com/opus/piano/OpusApplication.kt) lazily owns `Room.databaseBuilder(..., "opus.db")`, repositories and the single PracticeCoordinator. [OpusDatabase](../../../app/src/main/java/com/opus/piano/data/OpusDatabase.kt) is **schema 8**, exportSchema=true, with all migrations 1→2→3→4→5→6→7→8 registered. **No destructive fallback is present.** [Schema 8](../../../app/schemas/com.opus.piano.data.OpusDatabase/8.json) contains 15 tables:

| Table / source entity | DAO / ownership | Important contract |
|---|---|---|
| pieces / Piece | OpusDao, OpusRepository | Autoincrement local Long ID; title/composer strings; nullable unique catalogWorkId; status/difficulty/notes/instrument/genre/era. Multiple null catalogue links allowed. |
| sessions / PracticeSession | OpusDao, PracticeDao | Recorded timestamps/day/zone, credited/elapsed/detected times, mode/quality, notes/reflection, unique completionToken; title/catalogue/composer snapshots. |
| session_parts / SessionPart | PersonalDao | Measured duration and notes; FK session with cascade; historical title/composer snapshots survive repertoire removal. |
| session_intervals / SessionInterval | PracticeDao | Timing evidence with stable partIndex and creditedMillis; cascade only from session. |
| practice_drafts / PracticeDraft | PracticeDao | Singleton versioned JSON recovery payload, checkpoint timestamp. |
| practice_recovery_archives / RecoveryArchive | PracticeDao | Original unreadable/abandoned recovery bytes retained before confirmed fresh start. |
| local_app_state / LocalAppState | PracticeDao | Onboarding/reminder state and generation. |
| profile_settings / ProfileSettings | PersonalDao | Nickname/base64 avatar, theme/typography/statistics, Smart settings, favorites/biography, showcase. |
| badge_unlocks / BadgeUnlock | ExperienceDao, AchievementStore | Insert-ignore; original unlock dates immutable; showcase distinct earned IDs capped at five. |
| session_piece_links / SessionPieceLink | JournalEditsDao, JournalEdits | Untimed after-session links; composite session/piece key, no duration. |
| favorite_composers / FavoriteComposer | JournalEditsDao | Open Opus composer ID and original addedAt. |
| catalog_composers / CatalogComposer | CatalogDao, CatalogRepository | Provider string ID, name, life dates, epoch. |
| catalog_works / CatalogWork | CatalogDao | Provider string ID, one composerId, title/subtitle/genre/searchTerms/catalogue/key/popular/recommended. |
| catalog_cache / CatalogCache | CatalogDao / bounded repository | Key/raw JSON/fetchedAt; includes Open Opus responses and biography/portrait metadata. |
| saved_recordings / SavedRecording | ExperienceDao, RecordingRepository | Local HTTPS links, performer/title/platform/preferred; not recording binaries or structured recording identities. |

Six DAO interfaces are exposed: OpusDao, CatalogDao, ExperienceDao, PersonalDao, PracticeDao, JournalEditsDao. Other important services are BackupRepository, CsvExportRepository, DailyQuotes and GoogleIdentity. Full-journal reactive snapshots are transactional; history itself uses bounded `(completedAt,id)` keyset paging. Global statistics/achievement evaluation still load broad snapshots: a larger catalogue needs measured invalidation and query costs.

### Migration history

| Path | Actual additions |
|---|---|
| 1→2 | catalogue composer/work/response tables; unique Piece.catalogWorkId |
| 2→3 | catalogue flags, piece metadata, local session day/zone/catalogue attribution, saved links and badge dates |
| 3→4 | measured session parts backfilled from sessions; profile; history indexes |
| 4→5 | profile typography/statistics |
| 5→6 | timing evidence/quality/mode/notes/token; intervals; draft/local state; Smart preferences |
| 6→7 | favorite artist/piece/era/biography; recovery archives |
| 7→8 | untimed session-piece associations; composer favorites |

Historical schema exports remain protected. Host migration fixtures cover versions 1–7→8; Android fixtures and historical backup fixtures supplement these. Host SQL simulation is not the same as executing every migration in Room on Android.

## Catalogue, matching and network facts

[CatalogModels](../../../app/src/main/java/com/opus/piano/data/CatalogModels.kt) has **no** general Artist, Alias, Arrangement, Release/Album, structured Recording or Collection entity. `CatalogWork.composerId` assumes one composer. `searchTerms` is a string, not a sourced alias table. `catalogText` removes accents/punctuation, folds selected characters and lowercases for matching; preserve original strings. [MusicMetadata](../../../app/src/main/java/com/opus/piano/data/MusicMetadata.kt) has hardcoded Bach/Chopin/Mozart fallback identities. [CatalogRepository](../../../app/src/main/java/com/opus/piano/data/CatalogRepository.kt) deduplicates repertoire by provider ID or exact normalized manual title AND composer. This may block legitimate alternate arrangements; it is not multi-provider entity resolution.

Open Opus endpoints currently used: composer/list/pop.json, omnisearch/{normalized query}/{offset}.json, work/list/composer/{id}/genre/all.json, work/detail/{id}.json. Detail/composer requests require numeric IDs. Imported difficulty is UNRATED. Dates often encode January 1 placeholders and the UI shows years. Imports merge metadata transactionally and trigger AchievementStore evaluation, which may backfill previously missing session metadata; catalogue refresh is therefore not currently isolated from all journal writes.

[WikimediaProfile](../../../app/src/main/java/com/opus/piano/data/WikimediaProfile.kt) searches Wikidata by English name, accepts a unique full-name/alias + birth-year + human match, reads English Wikipedia extracts and rightsinfo, and fetches Commons imageinfo/extmetadata for P18. This excludes many groups/unknown-birth artists and stores no stable QID column in CatalogComposer. Profile JSON stores wikidata/article URL and license, but not article revision/history snapshot. Current image gate permits CC BY/BY-SA/public-domain URLs or a public-domain label, requires artist/source/thumbnail and empty Restrictions. This is a useful filter, not a complete independent rights determination. Do not claim universal legal clearance.

CatalogHttp uses HttpURLConnection on IO, a per-client mutex, fixed HTTPS host allowlist, disabled redirects, 12s connect/15s read limits and 8 MiB JSON / 4 MiB image limits. It has **no delay-based provider limiter, Retry-After scheduling, conditional ETag revalidation or persistent retry queue**. User-Agent still says Goldberg/0.9.2 and lacks a contact URL. MusicBrainz hosts are not allowed. Cancellation and cached fallback are supported; serialization alone does not guarantee a one-request-per-second limit.

[RecordingLinks](../../../app/src/main/java/com/opus/piano/data/RecordingLinks.kt) generates external Spotify/YouTube/IMSLP/MuseScore searches and validates saved links. Work keys require `(catalog|piece):[0-9]+`; these are compatibility-sensitive numeric assumptions. No audio/score content is downloaded through this repository. DailyQuotes independently contacts Project Gutenberg at most once daily with bounded response/time and bundled Schumann/Pierson fallback; it is outside CatalogHttp/media consent.

## Cache, downloads, consent

Open Opus responses have seven-day freshness; composer supplements/small portrait metadata use 30 days. Failure can return older saved data. Raw responses are pruned oldest fetched first at 16 MiB; portrait files are SHA-256-URL keyed, bounded at 32 MiB and trimmed by last use. Bitmaps are sampled to about 512 pixels. Cache clear deletes response/image files, **not normalized catalogue or journal tables**. Normalized catalogue has no current size eviction rule. [MediaPreferences](../../../app/src/main/java/com/opus/piano/preferences/MediaPreferences.kt) defaults explicit metered consent off; allowed media requires validated unmetered Wi-Fi or consent. Consent is rechecked during media reads/import. Metadata Open Opus queries do not use this media gate. There are no pinned entity bundles, durable progress manifests, background update scheduler or Smart Cache 2.0. Current cache locks coordinate restore and downloads; future download workers must preserve that exclusion.

## Practice, backup and identity

[PracticeCoordinator](../../../app/src/main/java/com/opus/piano/practice/PracticeCoordinator.kt) serializes state with mutexes and monotonic elapsedRealtime, checkpoints before publishing a new running identity, and publishes completion only after persistence. PracticeRepository uses completionToken idempotency and an atomic session/parts/intervals/draft transaction. Recovery pauses timing and never starts microphone capture. Bad recovery remains retryable/exportable; explicit fresh start archives the original payload. Do not introduce a catalogue-specific timer or reassign duration when mapping works. Untimed links and their labeled session annotations never earn measured credit.

[BackupRepository](../../../app/src/main/java/com/opus/piano/data/BackupRepository.kt) exports **format 4/schema 8**, max 32 MiB, 13 tables: all above except active practice_drafts and local_app_state. It **includes** normalized catalogue and catalog_cache. It reads format 1/schema 4 or 5, format 2/schema 6, format 3/schema 7 and format 4/schema 8. Validation uses a temporary in-memory Room DB and field/relationship/timing checks; replacement uses one transaction. ProfileViewModel requires preview/confirmation and coordinates PracticeCoordinator and cache locks. Successful restore clears the active draft; this is existing explicit replacement behavior, not a safe model for an automatic migration. Recovery archives are exported. CSV session and per-piece exports are separate.

Only Room profile appearance fields are portable. SharedPreferences `goldberg-appearance` (accent/cards/density/sort/layout), `goldberg-media`, `goldberg-identity`, quotes and app locale are not in portable JSON. An in-place upgrade preserves them; a portable restore is not an exact installation clone. Do not describe all settings/drafts as backed up. Avatar is stored in profile as bounded encoded image data, not a cache entry.

[GoogleIdentity](../../../app/src/main/java/com/opus/piano/identity/GoogleIdentity.kt) uses Credentials/Google ID with a random nonce and configured public Web client ID. It requires a nonempty provider token, imports optional identity locally, then discards the token. It does not cryptographically verify a server credential, establish a remote session, enforce admin roles or synchronize history. Local stored account string is not a backend identity proof. Identity preferences retain account/name/photo/manual-override flags; sign-out clears provider/account/photo and retains journal/profile. HTTPS Google avatar import is host/size/decode/consent bounded. Server authorization must be a new separately tested boundary.

## UI, tests and existing workflow

Four tabs remain Home/Library/Practice/Profile. Internal `HOME` means Profile and `REPERTOIRE` means Library; do not confuse route names with user-facing labels. Compose Navigation routes include catalogue/composer/work, local piece, recording links, history and badges; Settings is reached from Profile. RepertoireScreen integrates All/My Repertoire and local/catalogue results, search, filters/sort/favorites/manual Add. CatalogViewModel debounces 350ms and cancels superseded jobs, but filters an entire normalized catalogue snapshot in memory. Visible suggestions are capped/incremented by 40; this is not database paging. New All/Works/Artists/Albums/Lists categories do not exist.

Reuse Theme.kt/LocalAppearance, EditorialHeader, JournalViewport/Card/buttons/empty state, EditorialSkeleton, Choice/WorkCard/PieceRowContent and ComposerPortrait. Preserve Cormorant Garamond/Inter, semantic colors, light/dark, font scaling, reduced motion and CZ/EN resources. No redesign is approved.

[CI](../../../.github/workflows/quality.yml) pins checkout/setup-java/setup-android; builds, JVM tests, instrumentation APK compilation and lint, then host migration/backup/localization/history/policy checks. CI **does not execute emulator instrumentation** or publish a signed release. Existing tests cover timing failures/recovery, identity/media policies, cache, persistence, backup/history/navigation and UI-related rules; runner tests are not comprehensive screenshot automation. Stage 1 executed checks and limits are [recorded separately](VERIFICATION.md).

Repository is stokonn/Goldberg, default main; Wiki enabled and initialized with English maintainable sources in docs/wiki. Existing private Project 2 “Goldberg Development” has Status/Priority/Version/Work Type, one open 0.11 milestone (4), umbrella issue #31. No duplicate milestone/Project is needed. Stage 1 branch is intentionally not merged into main. External backup and untracked RELEASE_NOTES.md remain protected.

## Migration risks and required response

| Risk | Required future mitigation |
|---|---|
| Bare numeric provider IDs collide across sources; numeric routes/link regexes | Add typed source + entity-kind mapping alongside legacy IDs; retain old IDs/routes/keys and snapshots. Test every consumer, not just DAO. |
| Unique Piece.catalogWorkId blocks two arrangements | Add optional sidecar repertoire-to-work/arrangement relation; do not collapse/delete existing personal rows or remove uniqueness without a separately tested migration. |
| Single composer and three canonical fallback IDs | Introduce role-bearing many-to-many credits; preserve old composer snapshots/favorite dates and badge identities. |
| Catalogue import invokes journal metadata/badge backfill | Separate proposed provider refresh from journal reconciliation; prove expected legacy backfill versus forbidden changed credit in upgrade fixtures. |
| Full snapshot search/evaluation grows with multi-genre catalogue | Measure before import; add bounded indexed queries and UI paging later; avoid importing a full MusicBrainz dump into the app. |
| Cache contributes to portable backup size/strict table whitelist | Test 32 MiB limit and new tables; cache-only data should be optional in a future format. Continue reading formats 1–4; never silently discard critical fields. |
| Draft JSON v1 contains legacy IDs; install prefs outside Room | Preserve parser and original bytes; no provider resolution needed to recover. Seed pending draft/archives/prefs for upgrades. |
| Incorrect author/license/revision matching | Quarantine ambiguous records/media; keep immutable per-field provenance and offline attribution. |

Schema 9 is **a proposal, not reserved or implemented**. First agree logical model and backup scope, then additive transactional DDL/backfill. No destructive fallback, network in migration, journal rewrite or file rename. Test every historical version→new version with actual Room plus host fixtures, integrity/foreign-key checks, repeated opening, failure rollback, WAL-safe upgrade and current/old backup imports. An older APK generally cannot open a newer Room schema: rollback requires a separately preserved compatible backup/install plan, not an assumed automatic downgrade. Stage 2 must resolve contracts before schema work.

created by stokonn with love <3
