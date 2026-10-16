# 0.9.0 Beta — The Redesign Update

- Unify Ivory/Nocturne colors, editorial typography, curated accents and global card styling.
- Focus bottom navigation on Home, Repertoire, Practice and Profile; retain the complete badge collection inside Profile.
- Add Balanced Home with hybrid free/piece practice start, two chosen credited-time metrics and distinct recently practiced pieces.
- Improve repertoire search, persistent sorting/density, contextual add and compact piece details with attributed practice history.
- Prioritize the timer, real Smart Practice states and anchored controls while retaining the existing background service and timing semantics.
- Reorganize Settings into focused subpages; persist visual section order/visibility and provide a confirmed appearance-only reset.
- Localize application UI, notifications and achievements into Czech and English with per-app language support and plural resources.
- Introduce the original adaptive Classic Minimal Piano icon, night artwork and monochrome themed icon.
- Retain application ID, compatible signing identity, Room schema 6, migration graph and supported format-1/format-2 backups.
- Verify 52 JVM tests, clean APK/test compilation, lint (zero errors), host migration/legacy fixture/localization checks and the actual published 0.8 signing certificate. On-device checks remain unverified and disclosed for this beta.
- Details: [release notes](docs/RELEASE-0.9.md), [verification](docs/VERIFICATION-0.9.md).

# 0.8.0 Beta — Smart Practice

- Add one authoritative practice coordinator, durable drafts, measured timing intervals and idempotent completion.
- Add optional experimental local microphone activity detection with a microphone foreground service, locked-screen/background operation, sensitivity, Smart Pause and notification Pause/Stop.
- Add Practice Review, elapsed/credited/detected time separation, session reflection and editable per-piece/session notes.
- Add opt-in inexact reminders, separate spreadsheet-safe UTF-8 CSV reports, integrated content headers and skippable first-run onboarding that excludes upgrades.
- Add Bach’s Secret with synthesized plucks and an insert-once Curious Pianist badge.
- Add explicit schema 5 → 6 migration and format-2 backups with format-1/schema-4/5 conversion; preserve historical IDs, notes, dates, preferences and totals.
- Add in-app privacy/license notices, runtime dependency audit and targeted regression tests.
- Beta publication explicitly authorized with physical-device testing, screen-off microphone behavior, Android instrumentation execution, real installation upgrades, accessibility and battery behavior unverified. Automated results are preserved in docs/VERIFICATION-0.8.md.

# 0.7.0 — Smart Cache & Classical Editorial (unpublished candidate)

- Add bounded disposable response/portrait caches, usage display and safe Clear Cache.
- Share transactional background journal summaries across screens; retain offline catalogue rows and retry visible failed pages after connectivity returns.
- Introduce Classical Editorial typography, bundled Cormorant Garamond/Inter and a persistent System Font option.
- Add a personal Home overview, compact repertoire, timer-first Practice, contextual navigation, numbered badge widgets and configurable profile statistics.
- Add schema 4 → 5 preference migration and compatible schema-4 backup conversion; preserve historical records, badge dates and selections.
- Add delayed static skeletons, startup/summary diagnostics, upgrade-signing checks and regression coverage.
- Publication blocked: the current host debug key does not match the published 0.6.1 certificate. Device validation and runtime performance measurements remain outstanding.

# Changelog

## Goldberg 0.6.1 — Profile Reimagined

- Replace the large Profile editor card with a circular avatar, display name and dedicated Edit Profile dialog.
- Add responsive lifetime practice/repertoire/session/streak cards and compact period filters, readable scrollable activity bars and explicit empty states.
- Present selected badges in a horizontal shelf with a dedicated selection/reordering editor; retain the five-unlocked-badge limit and full Badges tab.
- Move Appearance and Backup & Restore into dedicated Settings, opened from the Profile app-bar gear. Remove Search from Profile’s app bar.
- Fix bottom tabs restoring saved Search stacks; select tab roots and dismiss search focus/keyboard, with keyboard-safe bottom navigation insets.
- Use system-default typography in the shared theme, with adaptive spacing and accessibility descriptions.
- Cache per-session part grouping for history and retain lightweight lazy lists.
- Keep schema 4, backup format 1, application ID, database and signing configuration unchanged; increment version code to 4.
- Add JVM coverage for period/streak/formatting/tab ownership/showcase behavior and instrumentation for real navigation and 0.6.0-format backup/theme compatibility.
- Full release description: [docs/RELEASE-0.6.1.md](docs/RELEASE-0.6.1.md).

## Goldberg 0.6.0 — A New Era

- Rebrand Opus to Goldberg, version code 3; retain application ID, database name, stable identifiers and signing setup.
- Add a flat harpsichord adaptive/themed icon and native Android 12+ splash resources.
- Add multi-piece sessions, live switching/selection/removal, per-piece timing and notes, free practice, pause/resume checkpoints and expandable summaries.
- Replace Home with Profile: editable identity/gallery avatar, period statistics/charts, current streak, most-practiced compositions and lazy visible history.
- Add persistent, ordered, user-selected showcase of up to five unlocked badges; keep the complete Badges destination.
- Unify search across personal repertoire, cached/online composers and works.
- Add validated version-1 JSON export/import using Android document pickers and explicit atomic replacement confirmation.
- Add Light/Dark/System appearance and matching system-bar contrast; retain scalable text and reduced-motion behavior.
- Add lightweight repertoire/addition changes, saved-session feedback and badge-unlock notices.
- Add schema 3 → 4 migration, preserving old session rows and backfilling per-piece history, plus targeted query indexes.
- Remove duplicate startup achievement evaluation, stop idle timer polling, reduce active refreshes from five per second to one, isolate timer collection to relevant destinations and bound portrait decoding.
- Add timer/attribution/showcase tests, seeded migration and backup persistence instrumentation, host schema checks and a local APK naming script.

## Opus 0.5 — The Classical Experience

Previous committed baseline: `346bc04`. Historical behavior and verification are preserved in `docs/OPUS-0.5.md`.
