# Stage 7A refinement pass — durable checkpoint (2026-10-22, context ≈76 %)

**Branch:** `feature/goldberg-0.11-stage-7a` (worktree Goldberg-Stage7A).
**Base:** initial Stage 7A implementation committed+pushed: `c106c5c` (code) + `f50f86a`
(docs). That work is DONE and preserved — this pass is a targeted polish on top, NOT a redesign.

**Owner authorization:** final UX refinement + spacing audit before final Stage 7A
acceptance. Stage 6 untouched, no Stage 7B, no merge/tag/release. Stage 7A NOT complete
until owner accepts (matrix X9).

## Required corrections (owner, this pass)

1. **Discover online toggle → Settings.** Persisted preference (default **OFF**, new
   `preferences/CatalogPreferences.kt`, SharedPreferences `goldberg-catalogue` /
   `online-catalogue`, StateFlow; expose `catalogOnline` on OpusApplication). Remove the
   Checkbox row from DiscoverScreen. Online search **explicitly user-initiated**: a
   "Search online" button runs the online leg (state `onlineRequested` counter; local
   search stays as-you-type). Opening Discover / restoring a screen does no network
   (landing = local). Update privacy notice wording to actual behavior (new EN/CZ text).
2. **Artist Profile hero overlap.** `ArtistHeroHeader` (ComposerScreen 136–155): fallback
   icon currently centered under overlaid text → long names/enlarged fonts overlap.
   Fix: image case keeps scrim overlay but hero uses `heightIn(min=210.dp)` (grows);
   fallback case restructured so icon and text cannot overlap (icon top-end watermark,
   text column in normal flow below it).
3. **Library separation.** RepertoireScreen "all" view currently interleaves personal
   pieces with public catalogue works/composers. Add `EditorialSectionHeader`
   "From the catalogue" (+ "not saved in your library" subtitle) before catalogue rows;
   Artists chip gets its own explanatory header; favorites (pinned) stay first.
4. **Discover empty-state contradiction.** `discover_empty` message currently also shows
   on landing while "Available offline" rows exist below → show only when `active`
   (real search) with no results; never on the landing screen.
5. **Work Profile placeholder.** CompositionScreen header uses person-icon
   `ComposerPortrait` → replace with work-specific art (MusicNote in surfaceVariant
   box, same 96×116 footprint; real portrait pipeline unchanged for artist rows).
   Empty sections: wrap "no verified movement list" in a soft Surface+icon (no invented
   data). Button+warning caption into one item spacedBy(6.dp).
6. **Biography language fallback.** ComposerScreen bio sections: when app locale ≠ `en`
   and English bio present → small label "Biography in English…" (new localized string).
7. **First-install catalogue reachability (privacy-safe).** Library Artists chip empty
   state gets an explicit `JournalSecondaryButton` "Load catalogue artists (requires
   internet)" → `catalogModel.startBrowsing()` (the app's own featured() fetch, real
   OpenOpus rows, explicit user action, no silent download, no invented data, no
   navigation changes).

## Spacing audit (all six screens, JournalSpace tokens)

Tokens: tiny 4 / small 8 / medium 16 / page 20 / large 28 dp (Theme.kt:18).
Baseline: 4–8 related, 12–16 groups, 20–24 sections. Planned normalizations:
Discover header block 2→4, list spacing 12→16, offline card vertical 10→12;
Library chips→controls 8→12, section headers added with medium spacing;
ComposerScreen list 14→16, essential row 10→12; CompositionScreen header spacing
14→16, button+caption 6, metadata cell padding 10→12; Home/Profile via grep-guided
token pass (no full rewrite).

## Verification plan

- Host gates: `./gradlew clean assembleDebug testDebugUnitTest assembleDebugAndroidTest
  lintDebug --no-daemon --max-workers=1` + all `scripts/check_*.py` (incl. new-string
  EN/CZ parity).
- **Seeded in-place upgrade:** `adb install -r` new debug APK over current emulator app
  (DB has 23 composers/597 works/2 cache rows + profile_settings) → verify counts and
  settings row unchanged.
- Emulator (`emulator-5554`, qemu=1 asserted, never physical): fresh screenshots of
  affected screens × Ivory/Nocturne × EN/CZ; **2× font scale** spot check; **small
  screen** (`wm size 720×1560`, reset after) spot check; honest reporting of anything
  not executed.
- Update UI-ACCEPTANCE-MATRIX (new rows for online-toggle/settings), results/README.md
  (mismatch + spacing), CHECKPOINT.md, STAGE-CHECKLIST Stage 7. Commit/push same branch.
  Append Issue #31 comment (append-only).

## State log (update as work proceeds)

- [x] Checkpoint written.
- [x] Bounded reads: SettingsScreen Data&Storage block, OpusApplication tail,
      EditorialComponents/Panel/JournalCard conventions (inner padding 12 dp is the
      de-facto design-system card value; JournalSpace tokens 4/8/16/20/28 for structure).
- [x] CatalogPreferences.kt created (SharedPreferences "goldberg-catalogue",
      key "online-catalogue", StateFlow, default OFF) + `catalogOnline` on OpusApplication.
- [x] DiscoverScreen: Checkbox row removed; `DiscoverSearchSession.online` dropped;
      explicit `onlineRequested` (plain remember — never restored, so restoring a screen
      never networks) set ONLY by the new "Search online" button (shown when pref ON) or
      reset to false on every query/filter change; online leg gated on
      `onlineRequested && catalogOnline`; landing stays local-only;
      `discover_empty` now only when `active && rows.isEmpty() && !loading`;
      privacy notice + off-hint strings rewritten to actual behavior.
- [x] SettingsScreen Data & Storage: persisted Switch row
      "Include online catalogue results" (+detail text) via app.catalogOnline.
- [x] ComposerScreen: ArtistHeroHeader restructured — image case keeps full-bleed hero
      with bottom scrim + heightIn(min=210dp) (grows with text); fallback case is a
      side-by-side Row (text weight(1) + 84dp watermark icon) so overlap is impossible;
      bio language-fallback label (non-en locale + English bio) in Overview and About;
      spacing 14→16 list, 10→12 card row, 3→4 stat label.
- [x] CompositionScreen: header art uses ComposerPortrait with MusicNote fallback
      (+stage7a_work_art_unavailable description; real portrait pipeline unchanged);
      status/primary-button and add-button/warning-caption merged into single items
      spacedBy(6dp); "no verified movement list" wrapped in soft Surface+Info icon panel;
      header spacing 14→16, metadata cell padding 10→12, label stacks → tiny token.
- [x] ComposerPortrait.kt: added `fallbackIcon` + `placeholderDescription` params
      (defaults preserve every existing call site).
- [x] RepertoireScreen: "All" view gets "From the catalogue" +
      "Catalogue artists" section headers with "not saved in your library" subtitle;
      Artists chip gets its own section header + hint; first-install empty state now
      shows an explicit "Load catalogue artists (requires internet)" button →
      catalogModel.startBrowsing() (app's own featured() fetch — privacy-safe
      reachability); controls/rows spacing normalized (8→small token, 10→12, 2→tiny).
- [x] BalancedHome/ProfileScreen: grep-guided token pass (10→12 group gaps,
      5→tiny, 7→small); internal structure untouched.
- [x] Strings EN+CZ: 12 new keys (search-online button, off-hint, settings
      setting+detail, from-catalogue, catalogue-not-saved, catalogue-artists,
      artists-hint, load-artists, bio-language, work-art) + stage7a_privacy_notice
      rewritten in both languages to describe the actual (default-OFF, explicit-only)
      behavior; no duplicate keys.
- [x] Host gates (EXECUTED, refinement pass 2026-10-22):
      `./gradlew assembleDebug testDebugUnitTest lintDebug --no-daemon --max-workers=1`
      → BUILD SUCCESSFUL; JVM tests 174 run / 0 failures / 0 errors / 0 skipped;
      lint 0 errors, 124 warnings (baseline was 122: the +2 are stylistic UseKtx in the
      new CatalogPreferences — identical to the existing MediaPreferences pattern — and a
      ModifierParameter warning on ComposerPortrait's signature; both non-blocking);
      `assembleDebugAndroidTest` → BUILD SUCCESSFUL after updating
      DiscoverNavigationTest to the new contract (query retention kept; the explicit
      online request is now composable-local remember state that cannot survive
      navigation/process death by construction). All five `scripts/check_*.py` PASS
      (agent policy, backup fixtures, history paging, localization parity, migrations).
- [x] Emulator (EXECUTED on `emulator-5554`, `ro.kernel.qemu=1` asserted throughout,
      never a physical device):
      · **Seeded in-place upgrade PASSED** — `adb install -r` of the new debug APK over
        the seeded AVD; all 22 table counts + profile_settings row (`Musician|System`)
        byte-identical before/after; `goldberg-catalogue.xml` absent until first write
        (default OFF by absence) and read back `true`→`false` across a toggle cycle.
      · 54 screenshots with regenerated SHA-256 manifest: 4 theme×language combos
        (home, library, library_artists, artist_profile, work_profile, discover,
        profile), Settings toggle flow EN+CS (`data_storage_off/on`,
        `discover_online_on`, `discover_off_hint`), 2.0× font EN (home/library/discover/
        library_artists/artist/work) + CS (home/discover), 720×1560 narrow sweep
        (home/library/discover/library_artists/artist/work).
      · Visually inspected: artist hero no-overlap in both themes, at 2× and 720 px
        (band grows, icon right of text); Work MusicNote art; Library catalogue
        separation headers; Discover off-hint + no empty-state contradiction;
        Settings toggle row EN/CS; CZ bio-language fallback label.
      · Script hiccups resolved honestly: `cmd locale reset-app-locales` does not exist
        on API 36 — tidy used `set-app-locales --locales ""` (readback `[]`); one
        mislabeled narrow capture recaptured; emulator left tidy (theme System,
        font 1.0, locale follows system, wm size reset).
- [x] Docs: UI-ACCEPTANCE-MATRIX X2–X8 updated with refinement evidence (X8 now
      EXECUTED); results/README.md refinement mismatch+spacing section; CHECKPOINT.md
      7A.14 record; STAGE-CHECKLIST Stage 7A refinement entry.
- [x] Commit + push + Issue #31 append-only checkpoint (see git log).

Blockers: none. Emulator alive: emulator-5554 (tidy state; catalogue bootstrapped on AVD
only). adb: `$HOME/Android/Sdk/platform-tools/adb`.

created by stokonn with love <3
