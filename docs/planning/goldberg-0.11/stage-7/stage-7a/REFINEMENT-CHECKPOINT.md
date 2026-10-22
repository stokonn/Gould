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

## Round 2 — final accessibility + spacing fixes (owner, 2026-10-22, commit `33214d3`)

Independent screenshot review found three remaining issues; owner authorized a second
targeted refinement round. No redesign, no provider/network behavior changes, JournalSpace
tokens only. Stage 6 untouched; no merge/tag/release; Stage 7A NOT complete until X9.

### Owner issues and fixes

1. **2× font mid-word wrapping.**
   - Artist Profile tabs (`ComposerScreen.kt`): `TabRow` → `ScrollableTabRow`
     (intrinsic tab width, `edgePadding = JournalSpace.small`) — labels never break
     mid-word; at 1.5× all three fit, at 2×/narrow the row scrolls horizontally (the
     owner-approved "scrollable tab pattern"); tab targets keep full default height.
   - Work Profile metadata cells (`CompositionScreen.kt`): adaptive `BoxWithConstraints`
     — stacked full-width Column with `HorizontalDivider` separators when
     `maxWidth < 340.dp || fontScale > 1.15f` (same precedent as Settings › About), the
     previous compact three-cell Row retained for regular widths at 1×.
   - Bottom navigation (`OpusApp.kt`): every item's label now carries an explicit full-name
     `semantics { contentDescription }` so screen readers announce "Discover"/"Practice"
     completely even where the visual label ellipsizes at 2× (5 tabs cannot fit full
     words; visual ellipsis remains as a documented limitation).
2. **Settings → Data & Storage spacing** (`SettingsScreen.kt`): the downloaded-content
   item is now a `Column(verticalArrangement = Arrangement.spacedBy(JournalSpace.medium))`
   grouping section header, media toggle card, supplementary hint and the online-catalogue
   card — no more touching boundaries. The online-catalogue preference and every warning
   text are preserved unchanged.
3. **Discover landing declutter** (`DiscoverScreen.kt` + strings): the privacy notice is
   shortened in EN+CZ to the single essential third-party disclosure ("Searches stay on
   this device. When you start an online search, your query is sent to external
   music-data providers." / CZ equivalent); the online-off hint now renders only while
   the user is actually searching (`active && !catalogOnline`); the technical
   network-status line renders only while searching with the catalogue preference ON
   (`active && catalogOnline`). Landing = one concise notice, nothing else.

### Verification plan (executed — see results below)

Host gates + all check scripts; seeded in-place upgrade re-run; targeted emulator captures
(`r2-*`) of the affected surfaces at 1×/1.5×/2×, EN/CS, Ivory/Nocturne + 720×1560 narrow;
UIAutomator evidence for bottom-nav semantics; visual inspection of every changed surface.

### State log (round 2)

- [x] Code edits: ComposerScreen (ScrollableTabRow), CompositionScreen (adaptive
      BoxWithConstraints cells), OpusApp (nav label semantics), SettingsScreen
      (Data & Storage Column spacing), DiscoverScreen (contextual hint/network items,
      `if(active)` gating), strings EN+CZ (`stage7a_privacy_notice` shortened, parity kept).
- [x] Host gates (EXECUTED, 2026-10-22): `./gradlew assembleDebug testDebugUnitTest
      assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1` → BUILD SUCCESSFUL;
      JVM **174/0F/0E/0S**; lint **0 errors, 124 warnings** (unchanged from round 1);
      `assembleDebugAndroidTest` compiles (`DiscoverNavigationTest` asserts only
      session/query retention — compatible, verified by reading the test);
      all five `scripts/check_*.py` **PASS** (localization parity 950 EN↔CS intact).
- [x] Emulator (EXECUTED on `emulator-5554`, `ro.kernel.qemu=1` asserted, never a
      physical device):
      · **Seeded in-place upgrade re-PASSED** — `adb install -r` of the round-2 APK over
        the seeded AVD; all 22 table counts + `profile_settings` row byte-identical;
        pref `true`→`false` readback across an on-device toggle cycle, restored OFF.
      · **47 targeted `r2-*` screenshots** + SHA-256 manifest (`capture-manifest-r2.txt`)
        + log (`capture-log-r2.txt`): EN Ivory 1× full set incl. the pref-ON flow
        (`discover_search_on`), CS Nocturne 1×, EN Ivory 1.5×, EN Ivory 2× (+ navcheck),
        CS Nocturne 2×, EN Nocturne 1× changed-surface coverage, 720×1560 narrow sweep.
      · Visually inspected: artist tabs no mid-word wrap at 1.5×/2×/narrow (scrollable
        row); work cells stacked cleanly at 1.5×/2×/narrow (EN + CZ); Data & Storage
        spacing clear in EN/CS, Ivory/Nocturne, 1×/2×/narrow; Discover landing carries
        exactly one concise notice in EN/CZ, both themes, all font scales; off-hint and
        Search-online + network line appear only in the searching state (asserted via
        UIAutomator text presence/absence, not by eye alone).
      · **Bottom-nav semantics evidence (2×, UIAutomator):** all five items report full
        `content-desc` names (Home/Library/Discover/Practice/Profile) — `NAV-EVIDENCE`
        lines in the capture log; visual ellipsis at 2× remains (documented limitation).
      · Script incidents resolved honestly (capture automation, not app defects): the
        first run failed on a flaky fixed-swipe work-card tap at 720 px (dump-driven
        scroll replaced it); the Settings section can open at a preserved scroll anchor
        at narrow width — the capture now scrolls to the top before screenshotting;
        `wait_for("Essential works")` timeouts at 1.5×/2× are non-fatal (header below
        the fold; dump-driven card detection still tapped real cards).
      · Emulator left tidy: font 1.0, theme System, locale follows system (readback
        `[]`), `wm size` reset to physical 1080×2400.
- [x] Docs: this checkpoint; stage-7a/CHECKPOINT.md 7A.15; results/README.md round-2
      section; STAGE-CHECKLIST round-2 entry; UI-ACCEPTANCE-MATRIX X4/X6 evidence.
- [x] Commit + push same branch + Issue #31 append-only comment (see git log).

Known limitations (honest): bottom-nav labels ellipsize visually at 2× ("Disc…",
"Pract…") — full names carried by accessibility semantics (verified); at 2× the third
artist tab ("Biography") sits at the scrollable row's edge and requires a horizontal
swipe — labels never break mid-word; Discover category tiles ellipsize at 2× (pre-existing
tile behavior, unchanged this round).

## Round 3 — vertical alignment audit + Discover privacy popover (owner, 2026-10-22, commit `d7c0aef`)

Owner tested the round-2 APK on a physical Samsung device; X9 remains open. Two defects:

**R3-1 — vertical text bias** (Settings rows, Discover result cards, Profile cards,
"other shared components"). Audit result: every shared Row already used
`verticalAlignment = Alignment.CenterVertically` (`SettingsRow`, `CatalogueRow`, Profile
rows, header rows — verified by reading each call site, plus a scripted sweep of every
`Row(` in `ui/*.kt` that checks bodies as well as call arguments). The root cause was
text-box metrics, not Row placement: the app's custom `journalTypography()` built its
`TextStyle` without `PlatformTextStyle(includeFontPadding = false)`, so every Text kept
the legacy font padding — extra leading beyond the font's core ascent/descent, uneven
against the explicit line heights. Material 3's own default Typography sets this to
`false`; the app bypassed that by constructing `TextStyle` directly. On real devices
(including Samsung system-font metrics) the inflated boxes made visually centered
content read top-biased. **Fix:** one place — the shared `text()` builder in `Theme.kt`
now applies `platformStyle = PlatformTextStyle(includeFontPadding = false)` (root cause,
app-wide, matches M3 defaults). No blind padding anywhere; no clipping (wrapping still
measures real ascent/descent — only the extra leading is trimmed). The structural sweep
found exactly one further real defect: the `CustomizePanel` preview placed the "♫" glyph
at default top alignment against its two-line title block → now `CenterVertically`.
Top-aligned decorative quote/info icons (Home quote mark, work info panel) intentionally
kept — conventional first-line/decoration alignment, not text bias.

**R3-2 — Discover privacy.** The persistent "Searches stay on this device…" card below
the search field is removed. The Discover header is now a Row (title/subtitle column +
`IconButton` lock in the upper-right) opening a compact anchored `DropdownMenu` popover
(browser site-information style) with the same truthful disclosure — no HTTPS/security
claim. The popover dismisses on outside tap **and** on Back (both asserted per context);
the button carries an accessible label (`stage7a_privacy_info`, EN "Privacy information"
/ CZ "Informace o soukromí"); `privacyInfo` state is hoisted beside `onlineRequested`
(survives LazyColumn item recycling). The online-results setting stays in
Settings → Data & Storage (default OFF) and network/session behavior is untouched
(no `onlineRequested`/`catalogOnline` logic changes).

- [x] Code: 5 files (+35/−16) — `Theme.kt`, `DiscoverScreen.kt`, `CustomizePanel.kt`,
      `values/strings.xml` + `values-cs/strings.xml` (EN/CZ parity 951).
- [x] Host gates (EXECUTED, green): `assembleDebug testDebugUnitTest
      assembleDebugAndroidTest lintDebug` **BUILD SUCCESSFUL** (2m51s); JVM
      **174/0F/0E/0S**; lint **0 errors, 124 warnings** (unchanged — `includeFontPadding`
      produced no new warnings); all five `scripts/check_*.py` **PASS** (parity 951 with
      the new label pair). A final `assembleDebug` re-ran after a comment-only edit —
      source/artifact identity restored (BUILD SUCCESSFUL, 39s).
- [x] Emulator (EXECUTED on `emulator-5554`, `ro.kernel.qemu=1` asserted, never a
      physical device): **seeded in-place upgrade re-PASSED** (`adb install -r` of the
      round-3 APK over the seeded AVD; all table counts + `profile_settings` row
      unchanged); **51 `r3-*` screenshots** (50 from the scripted sweep + 1 targeted
      Appearance & Customize shot) + SHA-256 manifest (`capture-manifest-r3.txt`) +
      log (`capture-log-r3.txt`): 4 theme×language combos at 1× covering all six
      screens, 1.5×/2× EN Ivory, 2× CS Nocturne, 720×1560 narrow, both Data & Storage
      variants, both Appearance & Customize previews.
      · **Privacy popover cycle verified ×8** (all four combos + 1.5× + 2× EN + 2× CS +
        narrow): landing absence of the disclosure card asserted, open → notice visible,
        outside-tap dismissal, reopen → Back dismissal, still on Discover — every step
        asserted via UIAutomator text presence/absence, not by eye alone.
      · Visually inspected: lock icon position + popover anchoring/content in EN/CZ ×
        Ivory/Nocturne × 1×/1.5×/2×/narrow; Settings switches centered against 2-line
        and 4-line subtitle blocks (EN Ivory + CS Nocturne Data & Storage); Profile
        stat tiles/Favorite-artist row/badge header centered; Discover offline rows
        centered; "♫" glyph node center 1101 vs preview block center 1100.5
        (`r3-ivory-en-appearance_custom.png`); no clipping at 2× in either language.
      · Script incidents resolved honestly (capture automation, not app defects): on
        API 36 `uiautomator dump` returns **only the popup window** while the
        DropdownMenu is open (background hierarchy excluded) — the first two runs failed
        hunting for the lock node inside the popup dump; the outside-tap point is now
        derived from the notice node's own bounds (left page margin above the popup).
        At 720×1560 the popup reports `left=0` (full width) — a bounds sanity check
        refused the first derived tap; the notice-node approach handles both dump modes.
        The script now resets `wm size` at startup (re-run safety after an interrupted
        run left the override active).
      · Emulator left tidy: font 1.0, theme System, locale follows system (readback
        `[]`), `wm size` physical 1080×2400.
- [x] Docs: this checkpoint; stage-7a/CHECKPOINT.md 7A.16; results/README.md round-3
      section; STAGE-CHECKLIST round-3 entry; UI-ACCEPTANCE-MATRIX evidence.
- [x] Commit + push same branch + Issue #31 append-only comment (see git log).

Known limitations (honest): the round-2 limitations carry over unchanged (bottom-nav
visual ellipsis at 2× with verified full-name semantics; third artist tab edge-cut in
the 2× scrollable row; Discover category tiles ellipsize at 2× — pre-existing). The 2×
CZ popover renders tall (7 wrapped lines) but stays anchored and fully contained; its
dismissal cycle is verified programmatically at every tested scale.

created by stokonn with love <3
