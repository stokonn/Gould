# Goldberg 0.6.1 — Profile Reimagined

## Introduction — Profile Reimagined

Goldberg 0.6.1 refines the personal side of your practice journal. Building on Goldberg 0.6.0, this focused maintenance release brings a compact Profile, clearer statistics, a curated badge shelf, and dedicated Settings, alongside a correction to Search and bottom-navigation behavior.

**This is a pre-release debug APK.** Automated build and host checks pass, while physical-device upgrade and UI verification remain pending.

## What's New

- A compact Profile header with a circular avatar, display name, and a dedicated Edit Profile dialog.
- Responsive practice-statistic cards and a cleaner activity chart with compact time filters.
- A horizontal showcase of up to five selected unlocked badges.
- Dedicated Settings for appearance and local backup/restore, opened from the Profile app bar.
- Bottom-tab routing that returns to the selected tab’s root instead of restoring Search over it.
- System-default shared typography and improved layout behavior for larger text and narrower screens.

## Redesigned Profile

Your avatar and display name now lead the page without an oversized editing card. **Edit Profile** opens a separate dialog for saving a display name, choosing a gallery avatar, or returning to the default avatar. Existing names and avatar data are retained.

Most-practiced compositions and expandable session history remain available below the activity and badge sections. Profile focuses on your music and progress; appearance and backup controls now have their own destination.

## Improved Statistics & Activity Charts

Compact cards display actual persisted data:

- **Total practice:** lifetime recorded practice, formatted as readable hours and minutes.
- **Repertoire pieces:** the number of saved compositions.
- **Practice sessions:** completed session count, with multi-piece sessions counted once.
- **Current streak:** consecutive qualifying practice days, allowing a streak to continue from yesterday.

**Week, Month, Year, and All time** filters update period totals, activity, most-practiced compositions, and history. Week covers seven days, Month thirty, and Year 365. Charts retain both partial boundary months in a trailing year and represent future-dated saved records in the all-time view.

Activity uses clean rounded bars, readable date labels, peak-time context, and explicit empty states. Dense charts scroll horizontally rather than compressing their labels. TalkBack descriptions include each bar’s recorded duration. History reuses grouped session entries rather than repeatedly scanning the complete per-piece list for every visible row.

## Badge Showcase

Profile presents selected badges in a compact horizontal shelf. Existing selection and order are preserved.

Use **Edit** to select unlocked badges, move them earlier or later, or remove them. The existing limit of **five unlocked badges** remains enforced, and tapping a showcased badge opens its details. An empty showcase explains how to begin.

The separate **Badges** tab and its full collection remain unchanged. Showcase preferences continue to be included in backups.

## Dedicated Settings

On Profile, the top app bar now places **GOLDBERG on the left** and a **Settings gear on the far right**. There is no Search icon on Profile, and the gear belongs to the app bar.

Settings contains:

- **Appearance:** Light, Dark, or Follow system.
- **Export backup:** save the journal through Android’s system file picker.
- **Import backup:** validate a selected backup, review the replacement confirmation, then restore it atomically.

The existing validation, explicit replacement warning, rollback behavior, and backup limits remain. A restore is unavailable while a practice session is being saved. Back from Settings returns to the existing Profile entry, preserving its current view state.

## Search & Navigation Fixes

The previous navigation configuration could associate a saved Search stack with a top-level destination and restore that stack when a bottom tab was selected. Tab navigation now explicitly returns to the requested root and avoids restoring a saved Search/detail stack over it.

- Search remains accessible from Repertoire and other relevant sections.
- Profile and Repertoire tab taps select their expected root screen.
- Bottom-tab actions clear search focus and dismiss the keyboard.
- Keyboard-aware bottom-navigation insets keep tab controls above the keyboard; content consumes those insets to avoid duplicate spacing.
- Search and Settings remain normal back-stack destinations, with Android Back returning to the preceding screen.

A regression test using the real Compose navigator and production tab-routing helper covers repeated Search → Repertoire → Profile transitions, Settings Back, and other bottom tabs. It compiles but has not yet run on an Android device.

## Typography & Accessibility

The shared theme now uses system-default typography instead of explicitly assigning a serif family to its headings. Android font scaling remains active.

Statistic cards switch to a single column when text is large or available width is narrow. Filters and dense chart labels can scroll horizontally, names wrap within the compact header, and the badge shelf avoids squeezing five cards into a fixed width. Settings theme options expose radio selection semantics, and icon actions include accessibility descriptions.

Goldberg’s ivory, charcoal, and gold palettes remain, including Light, Dark, and System appearance. Runtime checks of contrast, TalkBack, large fonts, narrow screens, and theme transitions remain part of device acceptance.

## Performance & Stability

This update retains the existing architecture, timer, catalogue, and persistence layers. It introduces **no new dependencies**.

Profile totals, filtered history, badge selections, and grouped session entries are remembered against their source data. Visible history and badge rows remain lazy. The chart avoids misleading minimum-height bars for zero activity and provides an empty state when no time was recorded.

No new Android startup or frame-time performance claim is made; hardware profiling is still pending. Goldberg Wrapped, cloud accounts, and Goldberg 0.7 Smart Cache are outside this release.

## Compatibility & Data Preservation

- **Application ID:** `com.opus.piano` — unchanged.
- **Database:** `opus.db`, Room schema **4** — unchanged; no migration is required from Goldberg 0.6.0.
- **Version:** `0.6.1`, version code **4**.
- **Backup format:** version **1**, schema **4** — compatible with Goldberg 0.6.0.
- **Android support:** Android 8.0+ (API 26).

Database entities, migration SQL, and the backup codec are unchanged. Existing repertoire, sessions, per-piece notes and durations, statistics source data, badge unlock dates, profile identity, showcase order, and theme preferences retain their existing representation. Earlier Opus migration paths remain available.

Signature verification confirms that this APK’s certificate matches the **published Goldberg 0.6.0 APK**. Upgrade compatibility requires the installed application to use that same signing key. Do not uninstall the existing app or clear its storage when upgrading.

## Testing & Verification

- **29 JVM tests passed**, with zero failures or errors: the previous 24 tests plus five maintenance tests covering period boundaries, current streaks, readable time formatting, tab ownership, and showcase ordering/limits. All-time chart coverage also checks future-dated saved records.
- **Android lint reported zero errors** and 13 warnings, concerning existing pinned SDK/dependency versions and an optional Bitmap KTX style suggestion.
- Debug application and Android instrumentation test APKs built successfully.
- Host SQLite migration checks passed for **v1 → v4, v2 → v4, and v3 → v4**.
- New real-navigation and 0.6.0-format backup/theme compatibility instrumentation tests compile. Existing migration, multi-piece persistence, backup/restore, and invalid-backup tests also compile.
- APK signature verification passed and matched the downloaded published 0.6.0 artifact.
- Whitespace validation passed; database schemas and the backup codec were checked for changes and remain identical to the committed 0.6.0 baseline.

**Android instrumentation tests were not executed: no Android device was connected.** Physical-device upgrade testing, navigation tap/Back behavior, Settings and document-picker flows, theme transitions, TalkBack, and visual checks at large font sizes and narrow widths are still pending. Compiled instrumentation coverage is not a claim that those runtime checks passed.

## Installation Instructions

1. Download **`Goldberg-v0.6.1.apk`** from this pre-release’s assets.
2. Before upgrading, export your current journal. In 0.6.0, the backup controls are on Profile; in 0.6.1, use the Profile app-bar gear → Settings → Export backup.
3. Install the APK over the existing application, approving installation from your download source if Android requests it. With ADB, use:

   ```bash
   adb install -r Goldberg-v0.6.1.apk
   ```

4. Open Goldberg and confirm that your repertoire, session history, profile, and selected badges are present.

**Do not uninstall or clear application storage.** A signature mismatch requires the original compatible signing key; uninstalling would remove locally stored data. Backups are plain JSON, not encrypted, so store exported files privately.

## Known Limitations

- This is a **pre-release debug build**, not a production-signed release. Android runtime and physical-device upgrade verification remain outstanding.
- Layout adaptations and navigation fixes have been reviewed and compiled, but have not been visually or interactively verified on a device in this environment.
- Aggregate statistics still read complete saved session lists; very large journals may need later database aggregation and hardware profiling.
- Backups replace rather than merge journals, remain limited to 32 MiB and 100,000 rows per table, and exclude unfinished timers and derived portrait files.
- Catalogue discovery and external listening/score services retain their existing connectivity and source-data limitations.
- Wrapped storytelling and Goldberg 0.7 features are not included.
