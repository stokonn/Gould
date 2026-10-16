# Goldberg 0.7.0 — Smart Cache & Classical Editorial

**Unpublished draft.** This candidate must not be distributed as an upgrade until its signing certificate matches 0.6.1. The intended publication is a **GitHub Pre-release** with a **debug-signed APK**. No production signing is claimed.

## A quieter, more personal practice journal

Classical Editorial brings warm ivory surfaces, considered dark-theme equivalents, delicate separators and a clearer typographic hierarchy. Home welcomes you with your saved practice and latest entry. Repertoire places the work and composer first. Practice puts the active piece, elapsed time and essential controls before the session notebook. Profile combines a prominent statistic, compact supporting measures, activity charts and a curated collection of earned badges. Settings groups appearance, typography, disposable cache and backup tools.

Ordinary app bars now use contextual headings. Repetitive wordmarks—including the inscription in badge artwork—have been removed. The installed app name and Android integration are retained.

## Smart Cache and offline access

- Previously saved composer and composition records remain available locally, without an account or connection.
- Fresh catalogue response copies avoid repeated requests and unnecessary replay of metadata ingestion.
- Shared journal summaries run in the background and supply practice totals, attribution and recent-practice dates to multiple screens.
- Transactional snapshots invalidate derived summaries when underlying repertoire, sessions or session parts change, including atomic backup replacement.
- Portrait downloads use a 32 MiB disk budget, recency-based eviction and reduced-size decoding. Downloaded response payloads have a separate 16 MiB budget.
- Settings shows approximate disposable download usage. Clear Cache removes response copies and downloaded portraits while retaining practice history, repertoire, badge unlocks and dates, profile settings, saved interpretations and normalized catalogue records.
- Saved content remains available after optional refresh failures, with cached-data messaging. Visible failed catalogue pages retry when validated connectivity returns while the app is active.
- Backup restoration waits for active catalogue downloads, preventing an older response from writing across the replacement.

Clearing or evicting downloaded biographies and portraits means those optional details may need to be downloaded again. The full remote catalogue is not bundled with the app.

## Cormorant Garamond and Inter

Cormorant Garamond gives headings and composition titles an editorial character. Inter keeps interface text, statistics, forms and controls readable. Both fonts are bundled, include Czech diacritics, and ship with their SIL Open Font licenses. The System Font preference follows the device font across all typography roles. Typography and statistic preferences persist locally and travel with new backups.

## Your profile, your emphasis

Choose the primary statistic and reorder supporting metrics using labeled controls. Existing users receive sensible defaults. Earned badge widgets show up to five selected badges in their saved order, with names, icons and original unlock dates. The complete badge collection, selection tools and permanent unlock records remain available. Week, month, year and all-time activity views and expandable session history are preserved.

## Performance and accessibility

Local summaries are shared across screens; repertoire reuses attribution totals and recent-practice dates. Badge verification is deferred from initial rendering. Static skeletons wait briefly before appearing, avoiding flashes for immediately available content. Lists retain stable identities, image decoding is bounded, and connectivity observation stops when the app is inactive.

The interface retains scalable text, scrolling content, accessible ordering buttons, labeled actions and Android animation-setting support. Core light/dark text colors pass a host contrast calculation of at least 4.5:1. Device-level accessibility and visual checks remain outstanding.

No measured startup, frame-rate or percentage improvement is claimed. A repeatable startup procedure and system-trace section are included in the repository.

## Data compatibility

Application ID `com.opus.piano` and database `opus.db` are unchanged. Version code increases to 5. Schema 5 adds only typography and statistic-order preferences and retains the full migration chain from schema 1. Existing practice sessions, multi-piece attribution and notes, repertoire records, profile settings, theme preferences, badge dates and showcase order are preserved by the migration.

Schema-4 format-1 backups from 0.6.0/0.6.1 remain importable. Schema-5 backups include the new preferences and require 0.7.0 or later; retain an earlier backup if older-version compatibility is needed. Restore remains validated and transactional. No destructive database fallback or application-data reset is introduced.

**Signing compatibility is currently blocked:** the local debug key differs from the published 0.6.1 key. Do not uninstall an existing app to work around this. The original key is required before publication.

## Verification

The independently frozen 0.6.1 baseline passed the required build checks, 29 JVM tests and lint with zero errors. The 0.7 candidate adds tests for summary correctness, safe preference recovery, cache clearing, duplicate prevention, offline fallback, schema-4 migration/reopen, backup conversion and preference round trips. Existing timer, multi-piece, badge, navigation and migration tests remain.

The final clean candidate run passed debug APK build, 31 JVM tests, instrumentation APK compilation and lint (zero errors, 11 warnings). Host migration checks passed all schema 1–4 upgrade paths to schema 5. See `docs/VERIFICATION-0.7.md` for exact artifact details and the failed signing gate. Android instrumentation compilation is distinct from execution.

## Known limitations

- No connected Android device or installed emulator was available. No physical-phone testing, executed instrumentation, real APK upgrade, visual review, large-font/narrow-screen inspection, TalkBack, picker or lifecycle stress test is claimed.
- No runtime performance comparison is available.
- Extremely large journals still load complete local session lists for aggregation; history renders a limited number of visible rows.
- Optional external biography, image and catalogue availability remains source-dependent.
- Wrapped remains the existing future-feature preview.
- This is a local debug candidate with incompatible signing until the original release key is supplied. No tag, merge or Pre-release publication is authorized by these notes alone; the successful-verification gate still applies.

## Local candidate artifact (not published)

`Goldberg-v0.7.0.apk` · Android Debug signing · SHA-256 `726a4b7ce676c90f208749bf8c8b8c0e3323d6d0d411efeaf9cfedafd34557ad`. This checksum describes the current incompatible-signing candidate only. Replace it after rebuilding with the original signing key and before publication.
