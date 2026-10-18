# Goldberg 0.10.1 — Final Redesign Polish (local design references)

This package is a **visual reference bundle**, not app assets and not a new design direction.
The authoritative implementation instructions are `CODEX_TASK.md` and `APPROVAL_MATRIX.md` in this directory.

## Authority / approved decisions

1. `01_HOME_APPROVED.png` — FINAL Home layout reference; remove settings gear; move top cluster slightly upward and enlarge greeting, name and subtitle only a little. Otherwise *do not change the design*.
2. `02_LIBRARY_APPROVED.png` — Library/Knihovna single combined view; **All** / **My Repertoire** filters; search, composer, difficulty, sort and filter controls. The screenshot illustrates a narrow dense row: use accessible horizontal scrolling or adaptive layouts rather than tiny tap targets.
3. `03_PRACTICE_COMPLETION_STYLE_REFERENCE.png` — style guidance ONLY for the completion popup. Include “Add pieces played”, “Add note”, and “Done”, and safely persist before confirming completion. The existing main Practice timer UI must stay unchanged. No science-fiction/neon styling. The exact totals/individual composers shown are fictional.
4. `04_ABOUT_THREE_ACTIONS_APPROVED.png` — overall layout and order of three actions: Revisit introduction | GitHub | Privacy & licenses. Use the **actual existing Goldberg app icon**; never recreate logo from the mockup. Display actual build version.
5. `05_APPEARANCE_PREVIEWS_INSPIRATION.png` and `06_APPEARANCE_ALTERNATIVE_INSPIRATION.png` — examples of *small live preview tiles* for theme, font, accent, cards, repertoire density and sorting. Current settings and actual supported values are authoritative; don't introduce invented options.
6. `07_TUX_ORIGINAL_APPEARANCE_PRESERVE.png` — user screenshot of the original Tux easter egg. Preserve its colors, wording, activation mechanism and style, fixing only ASCII layout, text whitespace and correct monospace alignment.

**Profile:** The actual approved Profile design was an in-chat wireframe, not one of these raster images. Follow `APPROVAL_MATRIX.md` precisely. Do not infer Profile requirements from obsolete 0.10 mockups.

## Important caveats

- These are *concept mockups*, not verified APK screenshots, and contain sample/fictional piece data, durations, difficulty and composer images. Never hardcode these values or install the references as resources.
- The Home reference includes a personal name. **Keep all images local, untracked, and out of any public GitHub push**. On local Fedora repo, use `.git/info/exclude` with `docs/design/goldberg-0.10.1/references/`, or equivalent safe local exclusion. Never git-add the references. Do not leak personal identifiers into Issues, Wiki, releases, screenshots or logs.
- This project has four bottom tabs: Home, **Library**, Practice, Profile. Settings is reached from Profile via its gear, NOT from Home and NOT as a fifth tab.
- Preserve user-selectable dark/light/system, accent colors, card styles, font choice, density and sorting; a slate-colored mockup is illustrative.
- This package does not authorize changing applicationId `com.opus.piano`, namespace, signing key, Room database filename `opus.db`, Android backup compatibility, or deleting user data.
- Work is version **0.10.1**. Future **0.11 = major Database Update**, planning only in this task.
