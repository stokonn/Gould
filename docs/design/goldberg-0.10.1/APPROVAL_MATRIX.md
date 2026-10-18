# Goldberg 0.10.1 — owner-approved acceptance matrix

Priority: **verbatim owner intent > this prose > mockup pixels > developer preferences**.

## Home — approved
- Keep current Goldberg 0.10 Home visual styling, ordering, typography, cards/list, graph, and layout.
- Remove only the gear/settings control from Home.
- Shift the greeting/name/subtitle cluster, and other main content as appropriate, slightly upward into the freed space while keeping safe Android system insets; no overlap with status bar.
- Enlarge the greeting, display name, and tagline **very slightly** (not a redesign). Other visual proportions are intentionally unchanged.
- Keep recent practice, daily quote, stats, weekly activity unchanged. Settings gear remains on Profile.

## Library (formerly Repertoire) — approved
- Bottom tab and screen title: **Library / Knihovna**; rename all corresponding localization and navigation display labels.
- Merge Catalogue and My Pieces into ONE unified browsing screen. No separate Catalogue/My Pieces tabs.
- Provide **All** and **My Repertoire / Můj repertoár** as clear filter choices in the same experience. Preserve all catalog and personal piece functionality and distinction in the data model.
- Retain composer/difficulty/sort/filter capabilities, search and add-piece flow. One consistent corner-radius system for Search, Sort and Filter (and related controls).
- Use adaptive chip arrangement/horizontal scrolling at narrow widths. Do not cram six full-width controls into an unusable single row just because an AI image shows it.
- Sample composer pictures, difficulty tags, durations and pieces in the mockups are illustrative and not required data/UI features.

## Practice — approved
- Existing main Practice screen (header, timer, Smart Practice, status, Start/Finish controls) is liked by the owner; preserve it.
- Do not show expanded, large piece- and session-note text inputs before/during practice. Keep an intuitive way to select/track pieces while practicing.
- When an actual session finishes, **persist first**, then show an elegant theme-consistent `Practice completed!` summary (duration + pieces played), integrated with existing finish/review behavior and safe recovery policy.
- Include action `Add pieces played`: select one or more omitted played pieces, preserve original session total, identity, notes, per-piece accounting, and avoid any double counting or invented per-piece durations. Ask for attribution only if actual saved data needs clarification.
- Include `Add note`: choose `Note to piece` or `Note to session` (session-wide). Existing repertoire-level notes and historical notes must be retained and semantics kept clear.
- `Done` dismisses summary; session is still saved even if user skips extras. Preserve later editing in History.
- Visually use **existing Goldberg 0.10 design tokens** (Cormorant Garamond / Inter when configured, semantic colors, corner radius, accessible sizing). No glowing neon, blue sci-fi outlines, pseudofuturistic style.
- No duplicate sessions, no accidental restart, no automatic microphone recording on return to Practice.

## Profile — approved with layout wireframe in chat
- Keep original premium editorial style; remove the subtitle `Your musical journey` entirely.
- Avatar and name a little higher, while respecting status bar and safe insets; keep hero spacious but not tall.
- Place **About Me short bio directly below the name, aligned as part of the same upper header row next to the avatar**, NOT a separate lower section. Bio text centered in its allocated area.
- Keep **only the pencil overlay on the avatar** as the visible edit/customize shortcut; remove redundant `Customize`/`Edit` links/buttons in the rest of the profile. Existing personalizations must remain reachable through sensible interactions (e.g. tapping a field or profile editor); do not silently remove ability to edit biography, featured composer, or identity.
- Remove `Practice History` link/section from Profile as duplicate of recently practiced/history access on Home. Keep History accessible from Home.
- Compact the four statistic boxes by modestly reducing padding while retaining readable, **center-aligned** numbers/labels and correct large-font reflow. Do not make them tiny.
- Enlarge and emphasize the Badge Showcase. Enable adding/selecting/removing eligible earned badges **directly from Profile**, without an unnecessary extra showcase-management detour. Honor the existing maximum of five, badge dates and selections. Preserve the actual original badge artwork.
- Retain featured Favorite Composer and its existing data and portrait, accessible without duplicate custom controls. Keep Settings gear on Profile.

## About — approved
- Rework page as elegant centered identity screen. Show **large original Goldberg app logo** centrally, app name directly below, and the **installed APK version dynamically** (do not hardcode 0.11; for this release use 0.10.1).
- Place exactly three aligned actions, **side-by-side** on normal screens, in this order:
  1) Revisit introduction
  2) GitHub (open `https://github.com/stokonn/Goldberg`)
  3) Privacy & licenses
- Responsive fallback for long Czech labels or enlarged font, with no clipping.
- Keep `created by stokonn with love <3` text.
- Preserve existing About-version-tap harpsichord Easter egg and any other existing easter eggs.

## Tux — approved
- The pink terminal Tux Easter egg is LIKED AS IS. **Only repair visibly broken ASCII art / glyph alignment / whitespace**.
- Use the appropriate bundled monospace/system-monospace font and preserve exact whitespace/newlines, including on small screens and with font scaling. Preserve pink, wording, layout, access method, and author credit. No visual redesign.

## Appearance & Customization — concept approved
- Every existing user-facing customization category must get a **small contextual preview**, preferably live-updating on selection, so choices are no longer blind guesses.
- Include actual supported options for Theme (system/light/dark), Typography (Goldberg vs system), Accent color, Card style, Repertoire density, and Sort order.
- Respect actual saved preferences, light/dark mode, translated labels and accessibility; small preview is illustrative of real appearance and order, not a fictional new screen.
- Avoid turning Settings into a huge wall of screenshots. Keep compact, readable controls and meaningful preview tiles.
- Do not remove any current customization setting, compatibility, or reset behavior.

## 0.11 roadmap (NOT 0.10.1 implementation)
- Owner's next major update is **Goldberg 0.11 — Database Update**.
- Prepare a proposal for expanding and improving composer/piece catalogue data quality, search, metadata and safe migrations only after auditing current structure. Keep 0.10.1 scope limited to polish and necessary bugfixes.
- No database expansion, broad schema changes, cloud sync or unrelated new features during 0.10.1.
