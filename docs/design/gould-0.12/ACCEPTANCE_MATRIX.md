# Gould 0.12 — UI acceptance matrix

| ID | Area | Required change | Preserve / reject | Visual evidence |
|---|---|---|---|---|
| ST-01 | Studio | Existing prominent credited timer; status visibly listening/playing/paused/interrupted | Preserve existing Practice state machine; no extra timer | `01_studio_recording_concept.png` |
| ST-02 | Studio | Smart Practice + optional continuous audio Record→Stop with one microphone owner, small recording indicator | No auto-record, no red primary Stop; recording never changes credited time | `01_studio_recording_concept.png` |
| ST-03 | Studio | Compact Metronome/Queue/Notes and upper-right history icon | No previous-session browsing embedded in Studio | `01_studio_recording_concept.png` |
| LB-01 | Library | Search+Add, All / My Repertoire / Artists, Sort/Filters, real app rows | **No Folders, no four extra chips, no sixth bottom tab** | Current actual `RepertoireScreen.kt`; `07_library_existing_ui_concept.png` |
| LB-02 | Previous | Dedicated screen, two tabs Practice History/Recordings; date/piece association | Do not equate linked external recording URL with captured audio | `01_studio_recording_concept.png` |
| WG-01 | Quick Practice (A) | Android launcher widget opens Studio intelligently in ready/current/paused state | No auto-start timer/mic/recording; no sixth tab | `11_quick_practice_widget.png` + `WIDGETS_SPEC.md` |
| WG-02 | Weekly Activity (C) | Mon–Sun seven-day chart and credited practice totals; adaptive small and expanded sizes | No elapsed/recording-duration substitution; no fake stats | `12_weekly_activity_widget.png` + `WIDGETS_SPEC.md` |
| WG-03 | Intelligent Open (B) | Explicit safe navigation according to live/durable draft state | No duplicate session, accidental resume, discard, or microphone startup | `13_intelligent_open_widget.png` |
| WG-04 | Adaptive / stale (B) | 1×1 to 4×2 responsive layout where supported; offline last-known result clearly stale | Never present unavailable values as new zeros or fresh totals | `14_adaptive_widgets.png` |
| PR-01 | Profile | Four smaller boxes with true horizontal and vertical centering of numbers/labels | **2×2** at normal widths, preserve avatar beside name | `02_profile_stat_cards_before.png` = current defect reference, not desired layout |
| PR-02 | Editor | Favorite Composer & Favorite Era autocomplete; autosave only on confirmed writes or Save/Discard/Keep editing prompt | Keep nickname/photo/bio/other fields, no silently lost edits | `08_autocomplete_examples.png` (illustrative) + text spec |
| AC-01 | Badges | Equal heights/widths within adaptive grid and consistent medallion/title/rarity/date alignment | Respect long CZ names, 1.5×/2× font scaling | `03_achievements_current_issue.png` + `09_achievement_grid_concept.png` |
| AC-02 | Badges | Five genuinely-earned new proposals + Founding Pianist Legacy pre-1.0 award | Keep all old 17 badges/IDs/dates and original artwork | Code registry authoritative |
| AC-03 | Popup | In-app elegant new unlock popup, queued, deferred during active practice and nonrepeating | No intrusive full-screen overlay while playing | Text spec authoritative |
| TX-01 | Tux | Fix current penguin's ASCII alignment and row spacing in dialog | **No new penguin art, no new color/font personality** | `04_tux_current_issue.png` |
| HM-01 | Home | Existing top-right quote card gets subtle natural landscape with contrast overlay | No full Home redesign/fictional quotes; offline/local licensed asset | `05_quote_nature_mood.png` |

## Explicit image/implementation conflicts

- Concept screenshots sometimes show `Folders`, a four-across Profile stats row, wrong navigation labels, an irrelevant player illustration, fabricated extra stats, quotes or portraits. **Treat those pixels as WRONG** where conflicting with this matrix.
- Screenshot content (specific dates/names/durations/medal labels) is sample data. App must use persisted real data and actual verified attributions.
- Owner's Tux screenshot shows the EXISTING bug, not a goal screenshot. Check the actual original reference only if it exists privately on owner host.
- The quote moodboard is a visual cue, **not an authorized image asset or a quotation source**.
- Final responsive/semantic implementation follows Android system insets and central theme tokens, not iOS chrome from AI mockups.

- Widget images are AI concepts only. Do not treat any launcher dimensions, mock piece titles, values, colors or app-category icons as production sources.

## Visual review checklist

- [ ] Studio ready/playing/silent/paused/recording/completion in Ivory and Nocturne.
- [ ] Library unchanged except entry to Previous; compare actual 0.11 screen.
- [ ] Previous history and recordings correctly independent, no made-up sessions/audio.
- [ ] Profile stat grid 2×2 with all four card contents centered; also long Czech labels and 200% font.
- [ ] Edit Profile autocomplete and save/leave modal backed by successful persistence.
- [ ] Badges equal-size current and locked, multiple popup behavior, 0.11 migration.
- [ ] Tux mono ASCII pixel comparison with original at normal and 200% font.
- [ ] Quote overlay readable on both themes, licensed asset local, no Home layout regressions.
- [ ] Native Android home-screen Quick Practice + Weekly Activity widgets: launcher add/resize/tap, smart open, Mon–Sun, stale/offline, no auto microphone.
- [ ] Correct app tabs: Home / Library / Discover / Studio / Profile.

created by stokonn with love <3
