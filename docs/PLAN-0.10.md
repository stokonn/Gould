# Goldberg 0.10 — implementation and visual acceptance plan

Baseline verified: published normal v0.9.2, main/tag commit 78fd88fbf4ead8772b87d2b986807a381241b7dd, code 9, APK SHA-256 7470fe7b477678946040df452813fc47708489883543bb3ea57bb2148bab4e7f. Branch feature/goldberg-0.10. Existing untracked RELEASE_NOTES.md and owner-supplied design package are preserved. No phone operations.

Read README/manifest, all seven screen references and three composites. 01_HOME_APPROVED and 02_PROFILE_APPROVED supersede older composites. Reference images remain documentation only and must not enter APK resources/assets.

## Audit / approach

0.9.2 uses Compose Navigation (four tabs), application-scoped PracticeCoordinator, Room schema 7, backup format 3, central OpusTheme/LocalAppearance, Journal buttons/viewport, persistent AppearanceStore and local profile/identity stores. Home/Profile currently lack the reference's distinct panels and hierarchy. Repertoire uses FlowRow for Sort/Filters (can wrap); compact portraits are circular. Profile editor has a plain artist text field. Completed review edits notes only. Google imports silently preserve manual values and swallow optional picture errors; consent granted after sign-in has no clearly explained import outcome.

Extend these components rather than introduce a second theme or practice owner. Use theme-derived subtle card borders, user accents/fonts/card style, compact shared headers/rows and rounded rectangular composer portraits. Keep four tabs and top-right gear. Preserve all appearance preference values.

## Data strategy

Favorites: unique local composer IDs, independently of Profile's single Favorite Artist. No external catalogue search or expansion.

Completed-session editing: separate untimed piece associations for omitted existing repertoire pieces. Never alter session ID, duration, timestamps, measured parts, intervals, notes or badge acquisition dates. Added associations carry **no duration** and are explicitly labelled "time not allocated"; adding/removing them cannot increase credited practice. Piece-specific history includes these sessions, with zero additional credited time. Measured entries remain intact. Use tested explicit schema 7→8 and backward-compatible backup reader additions if needed.

Google: preserve overrides by default; show why local values were kept and offer separate explicit name/photo replacement choices. Retry consent-dependent photo import safely, handle missing/invalid images, update observed Room profile immediately. Never fake provider authentication; the 0.10 owner-reported manual results are recorded separately from Codex tests.

## Checklist

- [x] Inspect baseline, canonical/tool policies and every supplied image.
- [x] Establish 0.10 Issues/milestone/Project tracking.
- [x] Add regression tests before sensitive history/identity logic.
- [x] Safe favorites/untimed historical associations, migration/backup compatibility.
- [x] Shared cards/headers/rectangular portraits and refined navigation.
- [x] Home reference layout: greeting/quote, two statistic cards, chart panel, bounded recent list.
- [x] Profile identity panel/four metrics, favorite artist/local autocomplete, bio/favorites/showcase.
- [x] Repertoire pinned controls, Sort/Filters side by side, local pinned favorite composers.
- [x] Google import precedence/explanations/retry/explicit conflict choices; owner-reported manual verification on the authenticated 0.10 candidate is recorded in the verification ledger.
- [x] Practice visual hierarchy and completed-session piece editor; preserve engine.
- [x] Grouped compact Settings and warnings; all customization retained.
- [x] CZ/EN, long labels, insets, light/dark, normal/enlarged fonts (emulator screenshot checks; font-scale fallback scrolls).
- [x] JVM/Android/migration/backup/history/localization/lint/APK and seeded emulator upgrade checks; see `docs/VERIFICATION-0.10.md`.
- [x] Actual emulator screenshots and written comparisons/deviations against approved references, including selected autocomplete suggestion and normal keyboard IME.
- [x] Prepare English Wiki/changelog and screenshot evidence locally; owner waived personal screenshot review.
- [x] Owner-reported manual verification on the Goldberg 0.10 candidate: sign-in, name/avatar import with consent, preservation of manual overrides, authenticated restart, sign-out/account switching and local practice-data preservation all passed on the Samsung Galaxy A55. This is explicitly not Codex device testing.
- [x] Rerun mandatory quality gates, publish Wiki and synchronize Issues/Project, commit/push, and publish the verified GitHub Release; compare the downloaded asset digest.

The owner explicitly waived personal screenshot approval for this release; small visual differences are documented for possible 0.10.1 work. Record the six 0.10 authentication checks only as owner-reported manual verification. Goldberg 0.10/code 10 is published from main at tag `v0.10`; application ID and compatible signing remain unchanged.
