# Goldberg 0.10 — approved UI reference package

These are **reference images, not Android source code**, and are intended for the Goldberg 0.10 redesign. The ZIP is structured to extract directly into the existing repository, under `docs/design/goldberg-0.10/`. Do NOT bundle these mockup PNGs into the APK as UI assets. Use them to guide real Jetpack Compose layouts and screenshot comparison.

## Visual approval precedence

| File | Status | Purpose |
| --- | --- | --- |
| `screens/01_HOME_APPROVED.png` | **LATEST, APPROVED** | Home layout: greeting/name at upper left, typography-only daily quote at upper right, separated practice/streak cards, weekly activity, recent sessions, four bottom tabs, Settings gear above. |
| `screens/02_PROFILE_APPROVED.png` | **LATEST, APPROVED** | Profile layout: larger avatar/name with breathing space, distinct personal stats and favorite artist/bio/showcase, Settings gear at top, exactly four bottom tabs. |
| `screens/03_REPERTOIRE_REFERENCE.png` | Approved *design direction*, conceptual data | Compact composer rows, rounded rectangular portraits, pinned favorite composers, Sort and Filters side by side. Keep Goldberg's actual My Pieces/Catalog structure. |
| `screens/04_COMPLETED_SESSION_EDIT_REFERENCE.png` | Approved *feature concept* | Add pieces to saved/completed practice without losing original timing or journal data; no invented per-piece durations. |
| `screens/05_SETTINGS_REFERENCE.png` | Approved *design direction* | Compact, clearly separated setting categories and much shorter explanatory copy. Keep all functioning options and essential destructive warnings. |
| `screens/06_PRACTICE_REFERENCE.png` | Older conceptual visual inspiration | Use only for timer/control hierarchy. Preserve existing practice logic, and remember **Practice is the sole start-session entry point**. |
| `screens/07_FAVORITE_ARTIST_AUTOCOMPLETE_DETAIL.png` | Conceptual interaction | Composer suggestions with portraits, using the existing local database only. |

Full original composite references are under `composites/`. **Do not use older Home/Profile artwork from those secondary composites**: the latest approved `HOME_PROFILE_LATEST_APPROVED.png` supersedes earlier designs.

## Critical product rules (override illustrative details in any image)

1. **Exactly four tabs:** Home, Repertoire, Practice, Profile. Settings is **NOT** a bottom tab. Keep the **top-right gear** for Settings, particularly on Profile. The latest mockup places it on Home too.
2. **Home = activity dashboard** (today, streak, weekly chart, daily quote in its own upper-right card, recent practice). No favorites/profile sections and **no Start Practice button**. No decorative photo in the quote card.
3. **Profile = identity** (larger avatar and name, optional bio, up to four meaningful statistics, single featured favorite artist, favorite piece/era if configured, badge showcase up to five). Prevent repeated favorite sections from other tabs. Avatar/name should not be cramped against top system bars.
4. **Repertoire = music:** top Search + Add on same row; **Sort + Filters side by side**; small composer images **rounded rectangles, not circles**; pin any number of existing composers as favorites; local-catalog-only autocomplete and portraits. Preserve the existing My Pieces/Catalog tabs and real data. Do not perform the later large database expansion.
5. **Practice = only place to start practice.** Support safe editing of pieces in completed sessions, preserving total time, timestamps, notes and data integrity; do not invent per-piece time allocation.
6. **Settings:** preserve theme/light/dark/system, font, accent, card style, repertoire density, sorting, backup/restore, media consent, etc. Shorten nonessential copy and visually separate groups. Keep meaningful warnings about backups, nonencrypted exports and data replacement.
7. **Google account:** investigate user-reported name/avatar import not appearing reliably; respect local nickname/avatar overrides and media consent; don't replace local journal.
8. **No made-up UI functionality:** the mockups include sample names, pieces, counts, daily-goal progress, image portraits and icons that may not exist. Use **actual application data**. Only display a goal indicator if a genuine goal exists; no additional settings bottom tab, no imaginary sync/notifications or external catalog search.
9. **Themes:** warm gold seen in references is illustrative. Honor the app's user-selectable accent color and its full customization system, including light/dark modes and CZ/EN.
10. **Accessibility and viewport:** aspire to Home/Profile/Practice single-screen layouts at typical device sizes, but use an accessible scrolling fallback on small displays/high font scales. Do not clip controls.
11. **Release gating:** implement and run unit/emulator/lint/migration tests as already specified in the separate Goldberg 0.10 Codex prompt. Compare real rendered screenshots against these references. **Owner visual sign-off before releasing 0.10**; no automatic phone testing.
12. **Safety:** preserve the existing version 0.9.2 release, real user data, Git history and compatible signing. Follow project AGENTS.md and its GitHub Issues/Wiki/Project workflow.

## How the agent should use the images

- Inspect `screens/*.png` and the `composites/*.png` context, then review the current Android UI sources and app behavior.
- Implement reusable layouts in the existing codebase instead of rendering screenshots as backgrounds.
- Capture equivalent real screenshots using emulator(s) and compare visually; record justified deviations.
- Treat the latest Home/Profile mockup as the highest-priority reference; conceptual Repertoire/Settings/Practice visuals must be adapted to the existing app architecture and approved text specification.
- Keep an explicit list of unresolved differences for the owner's approval.
