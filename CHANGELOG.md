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
