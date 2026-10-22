# Goldberg 0.11 — Stage 7A screenshot evidence & mismatch report (X7)

**Status:** executed 2026-10-22 on the verified local emulator. Owner visual acceptance
**pending** — this report is evidence for the owner, not a completion claim.

## Evidence set

- **Branch:** `feature/goldberg-0.11-stage-7a` (all Stage 7A code UNCOMMITTED at capture
  time; commit recorded in `capture-manifest.txt` header after publication of this evidence).
- **Device:** `emulator-5554` — API 36 google_apis x86_64 (`stage5-api36`), `ro.kernel.qemu=1`
  asserted by the capture driver, 1080×2400 @420 dpi. Never a physical device.
- **APK:** `app/build/outputs/apk/debug/app-debug.apk` (debug-signed; host gates green —
  174 JVM tests 0F/0E, lint 0 errors, all five `scripts/check_*.py` PASS).
- **Catalogue data:** bootstrapped through the app's own repository code by
  `Stage7aCatalogueBootstrapTest` (`CatalogRepository.featured()` +
  `composerWorks()` — exactly the calls `CatalogScreen.startBrowsing()` and
  `ComposerScreen.loadComposer()` make): **23 real OpenOpus popular composers, 597 real
  Vivaldi works** (2 cached responses in `catalog_cache` with full source attribution). No
  invented, seeded or hand-written catalogue rows anywhere.
- **Navigation:** every screen reached through the product's own UI (accessibility-tree taps
  on real labels; bottom tabs, filter chips, artist row, essential-work card). Theme switched
  through the product's persisted setting (`profile_settings.theme`, Stage 5 proven
  upsert + readback); language through the platform per-app locale (`cmd locale
  set-app-locales`, readback asserted); font scale through `settings put system font_scale`
  (readback asserted).
- **Captures:** **32 screenshots** — Home, Library, Library-Artists-chip view, Discover,
  Profile, Artist Profile, Work Profile × Ivory/Nocturne × EN/CS (28) plus a 1.5×
  font-scale spot check (4 tabs, Ivory EN). Sizes ≥ 142 KB (blank-frame guard 20 KB),
  sha256 manifest in `capture-manifest.txt`, step-by-step `capture-log.txt`.
- **Visual inspection (2026-10-22):** directly inspected renders — ivory-en
  (home, library, library_artists, discover, profile, artist_profile, work_profile),
  nocturne-cs-library, font15-en-home — all rendering the intended editorial design with
  real data and correct theme/locale/font tokens. The remaining 23 captures were produced
  by the identical verified navigation path (log-asserted taps + readback-asserted
  theme/locale switches) with plausible sizes; spot-checked for correctness but not all
  individually inspected.

## Fresh-install reachability finding (pre-existing, Stage 4 era)

On a fresh install the legacy `catalog` navigation route has **no production entry**
(git history: `navigate("catalog")` existed through 0.10.1 and was removed by the Stage 4
Discover redesign; `NavigationRegressionTest` still exercises the route). Every Artist/Work
Profile entry point (Library Artists chip, catalogue work cards, Discover offline rows)
reads the opus.db catalogue tables that only `CatalogRepository` populates — so those screens
cannot render real content on a fresh install until the catalogue is bootstrapped. Per the
owner's explicit instruction this is recorded as an **investigation finding only** — Stage
7A performed **no navigation cleanup or restoration**. The bootstrap helper exists so the
redesigned screens can still be inspected with genuine data.

## Per-screen comparison (vs approved references)

### Discover — vs `stage-4/visual-references/01-discover.png`

| Reference element | Implementation | Verdict |
|---|---|---|
| Large editorial "Discover" + subtitle | ✓ headlineLarge + "Explore music" | Match |
| Prominent search field | ✓ `EditorialSearchField` | Match |
| Privacy notice (lock icon) | ✓ owner-approved wording (plan §9 C1): "Searches stay on this device until you turn on online results." — the mockup's "our servers" claim is **deliberately not used** | Deliberate difference (D4, owner-approved) |
| Browse categories + See all | ✓ real categories (All/Works/Artists/Albums/Lists tiles); "See all" **omitted** — no real paged category destination exists (D5: only with real destination) | Deliberate difference |
| Recently viewed cards with Local/Online pills | ✓ `RecentCard` LazyRow with `AvailabilityPill`; hidden when history empty (honest) | Match (content-dependent) |
| Available offline rows | ✓ real local content only: catalogue-linked repertoire + cached popular/recommended works; **no bundle UI** (plan C6, L5 blocked on Stage 5 management) | Deliberate difference |
| (mockup) rich cover artwork | thumbnail only from real licensed media; designed icon fallbacks otherwise (L6) | Deliberate difference |

### Library — vs `02-library.png`

| Reference element | Implementation | Verdict |
|---|---|---|
| "Your Collections" + "Your music" | ✓ exact (L1) | Match |
| Bottom tab "Library" | ✓ `label_library` EN/CZ (L2) | Match |
| Chips All · Works · Artists · Albums · Lists | ✓ **All · My Repertoire · Artists** — Works folded into My Repertoire; **Albums/Lists omitted** (no data model; L9 forbids mockup-only lists) | Deliberate difference (plan-approved) |
| Top-right download button | **Omitted** — no Stage 5 management UI exists yet (L5 blocked); no fake action | Deliberate difference |
| Recently practiced / Favorite artists / Saved lists sections | personal pieces rows (real status/progress/practice time) + `ArtistRow` list from the real catalogue; empty-state panel when no pieces | Content differs by design (no invented sections/counts, L9) |
| Compact rows, real thumbnails | ✓ ComposerPortrait with designed fallback (L6) | Match |

### Artist Profile — vs `03-artist-profile.png`

| Reference element | Implementation | Verdict |
|---|---|---|
| Hero portrait + name + roles/dates overlay | ✓ `ArtistHeroHeader` 210dp, name headlineMedium + "epoch · years", favorite star; **portrait = designed fallback on fresh install** (see finding below) | Match layout; image depends on cached licensed media |
| Short bio + Read more | ✓ real Wikipedia intro after the product's own explicit "Download biography & portrait" (media-consent path), truncated + Read more (A3) | Match |
| Essential works cards | ✓ real popular/recommended works LazyRow → Work Profile (A4) | Match |
| Albums & Recordings section | **Omitted** — no albums/recordings data model for the legacy composer page (A5: only where actually available) | Deliberate difference |
| 3 tabs Overview/Works/Biography | **Kept** (plan decision — existing tab structure preserved; About tab = Sources & Credits provenance, A6) | Deliberate difference from the single-scroll mockup |
| Sources & Credits | ✓ Biography tab with OpenOpus/Wikipedia/Wikidata links + licenses (A6) | Match |

**Portrait pipeline note (pre-existing, unchanged by 7A):** the hero image renders only
from the repository's licensed-portrait file cache; nothing in the current product invokes
the explicit image download (`portrait(url, explicit=true)` has no caller since the Stage 4
redesign) — verified identical pre/post Stage 7A (`git show HEAD:` comparison). The designed
fallback icon renders honestly (A1 "meaningful fallback otherwise"); no broken images.

### Work Profile — vs `04-work-profile.png`

| Reference element | Implementation | Verdict |
|---|---|---|
| Title + composer + catalogue ID | ✓ headlineMedium + composer in primary + catalogue label (W1) | Match |
| Prominent gold Add to Repertoire | ✓ `JournalPrimaryButton` full-width 52dp + existing UUID-safe duplicate prevention caption (W2) | Match |
| Compact metadata row | ✓ Period · Genre · Catalogue number cells — **only fields that exist**; Duration omitted (no duration data) (W3, W6) | Deliberate difference |
| Overview section | ✓ subtitle/notes when present (progressive) | Match |
| Structure & movements | ✓ honest "No verified movement list is supplied by the current catalogue…" + progressive `Sources & Study` (W4/W6 — **no invented movements**, unlike the mockup's illustrative list) | Deliberate difference |
| Arrangements / Recordings sections | Recordings via existing Listen & Study (real saved recordings only); Arrangements not in the current data model | Deliberate difference |
| Sources & references | ✓ Explore composer + practice sections | Match |

### Home — vs 0.10/0.10.1 approved layout (H1–H9)

✓ Activity dashboard retained: greeting + username, daily quote block, Today Practiced +
Current Streak, seven-day graph, Recently Practiced **trimmed to 2** (H6). No settings gear,
no favorite artists, no Start Practice, no profile duplication (H8). Practice start remains
Practice-tab-only (H9).

### User Profile — vs 0.10/0.10.1 approved layout (P1–P8)

✓ 104dp avatar + headlineLarge username (P1), About Me + Favorite Piece/Era inside the
identity card (P2/P3), badge showcase 84dp (P4), pencil overlay + Settings gear kept (P6),
no Practice History / Journey / Home duplication (P7).

## Cross-screen execution (matrix)

| Gate | Result |
|---|---|
| X2 Ivory + Nocturne | **EXECUTED** — all six screens in both themes (theme readback asserted per switch) |
| X3 CZ + EN | **EXECUTED** — all six screens in both languages (`cs`/`en` per-app locale, readback asserted; composer/work names intentionally untranslated) |
| X4 font scale 1.5× | **EXECUTED (spot check)** — Home/Library/Discover/Profile at 1.5×, Ivory EN; 2× and small-screen sweep **not executed** |
| X5 host gates | **EXECUTED — green** (build EXIT=0, 174/0/0/0 JVM, lint 0 errors, 5× check scripts PASS) |
| X6 fresh-emulator screenshots, explicit serial | **EXECUTED** — 28 shots, `emulator-5554` asserted |
| X7 mismatch report | this document |
| X8 seeded upgrade check | **Not executed in 7A** — no schema/database/migration change in Stage 7A (code inspection: only UI + resources + tests); Stage 5 upgrade evidence remains authoritative |
| X9 owner acceptance | **PENDING — required before any "Stage 7 complete" claim** |

## Refinement pass (owner-authorized polish, 2026-10-22)

Seven targeted corrections + a systematic spacing audit on top of the approved six-screen
redesign. All verified on `emulator-5554` (qemu=1 asserted); 48 fresh screenshots
(`settings-*`, refreshed combo shots, `font20-*`, `small-*`) with SHA-256 manifest.

| # | Correction | Implementation | Verified |
|---|---|---|---|
| R1 | Online toggle → Settings, persisted, default OFF, explicit search only | New `preferences/CatalogPreferences.kt` (SharedPreferences `goldberg-catalogue`, StateFlow, default OFF; exposed as `catalogOnline`). Discover checkbox removed; explicit `onlineRequested` (plain `remember` — never restored, so restore/open never networks) set only by the "Search online" button when the pref is ON; pref persistence asserted on-device (`true`→`false` readback); privacy notice + off-hint rewritten to actual behavior | ✓ Settings EN/CS captures, `discover_online_on`/`discover_off_hint`, prefs XML readbacks |
| R2 | Artist name/dates vs portrait-placeholder overlap | `ArtistHeroHeader` restructured: image case keeps the full-bleed hero with a bottom scrim and `heightIn(min=210.dp)` (grows with text); fallback case is a side-by-side Row (text `weight(1)` + 84 dp watermark icon) — overlap impossible by construction | ✓ ivory/nocturne × EN/CS artist captures; 2× font + 720 px narrow captures |
| R3 | Personal vs public catalogue separation in Library | "From the catalogue" + "Catalogue artists" `EditorialSectionHeader`s with "not saved in your library" subtitle in the All view; Artists chip gets its own header + hint ("pinned favourites stay first") | ✓ `*-library.png`, `*-library_artists.png` all combos |
| R4 | Discover "no local content" contradiction | `discover_empty` only renders for an active search with no matches — never on the landing screen while "Available offline" rows exist | ✓ all four `*-discover.png` landing captures |
| R5 | Work Profile placeholder + empty sections | `ComposerPortrait` gained `fallbackIcon`/`placeholderDescription`; Work header uses MusicNote art (real portrait pipeline unchanged); "no verified movement list" wrapped in a soft Info panel (no invented data); button+warning merged into one 6 dp-spaced item | ✓ `*-work_profile.png` all combos |
| R6 | Biography language fallback label | Non-`en` locale + English bio → "Biography in English — no Czech version…" label under the bio (Overview + Biography tabs) | ✓ `ivory-cs-artist_profile.png` renders "Životopis v angličtině…" |
| R7 | First-install catalogue reachability (privacy-safe) | Library Artists empty state: explicit "Load catalogue artists (requires internet)" → `catalogModel.startBrowsing()` (the app's own `featured()` fetch — real OpenOpus rows, user-initiated, no silent downloads, no invented data, no navigation changes) | ✓ code + empty-state path (AVD has bootstrapped catalogue, so the button renders only on a truly fresh install) |

### Spacing audit (all six screens)

JournalSpace tokens (4/8/16/20/28 dp) for structural spacing; 12 dp retained as the
design-system's card inner padding (`Panel`/`JournalCard` convention). Normalized:
Discover header 2→4, tile/card rows 10→8+8-edge-padding (≈20 visual), card rows 10/11→12;
Library controls 8→small token, rows 10→12, title stacks 2→4; Artist screen list 14→16,
card row 10→12, stat labels 3→4; Work header 14→16, metadata cells 10→12, button/caption 6;
Home/Profile group gaps 10→12, 5→4, 7→8. No text/portrait/icon overlap observed at
1.0×/2× font, EN/CS, 1080 px and 720 px widths. Editorial look preserved — no new
borders or cards added for separation (section headers + one soft empty-state panel).

### Verification deltas vs the initial pass

- Seeded in-place upgrade (X8) now EXECUTED for 7A: `adb install -r` over the seeded AVD;
  all 22 table counts + profile_settings row identical; `goldberg-catalogue.xml` absent
  until first write (default OFF by absence) and read back `false`/`true` correctly.
- Lint warnings 122→124: +1 UseKtx in the new `CatalogPreferences` (identical to the
  existing `MediaPreferences` pattern), +1 ModifierParameter on `ComposerPortrait`'s
  signature; 0 errors, both stylistic and non-blocking.
- `DiscoverNavigationTest` updated to the new contract (query retention kept; the online
  request is composable-local state by construction).
- Known pre-existing observation (not introduced here): the Settings landing renders its
  section names via `L.fixed(...)`, localizing "Data & Storage"/"Data a úložiště"
  correctly; no action taken.

## Round 2 — final accessibility + spacing fixes (owner-reported, 2026-10-22)

Second targeted round after independent screenshot review: three fixes, no redesign, no
provider/network behavior changes. Verified on `emulator-5554` (qemu=1 asserted) with
**47 targeted `r2-*` screenshots** + SHA-256 manifest (`capture-manifest-r2.txt`) and log
(`capture-log-r2.txt`). Host gates re-run green: BUILD SUCCESSFUL, JVM 174/0F/0E/0S,
lint 0 errors / 124 warnings (unchanged), `assembleDebugAndroidTest` compiles, 5/5 check
scripts PASS (EN/CZ parity 950). Seeded in-place upgrade re-PASSED (22 table counts +
profile_settings row identical; pref `true`/`false` on-device readbacks, restored OFF).

| # | Owner-reported issue | Fix | Verified |
|---|---|---|---|
| N1 | 2× font mid-word wrapping in Artist tabs ("Overvi/ew", "Biogra/phy") | `TabRow` → `ScrollableTabRow` (intrinsic tab widths, `edgePadding = JournalSpace.small`) — labels never break mid-word; at 1.5× all three fit fully, at 2×/narrow the row scrolls horizontally (owner-approved scrollable pattern); default tab touch-target height kept | ✓ `r2-ivory-en-15/20-artist`, `r2-nocturne-cs-20-artist`, `r2-small-en-artist` (tabs render "Overview/Works/Biography" unbroken; narrow error-dump showed all three labels fully) |
| N2 | 2× font wrapping/ellipsizing in Work metadata cells ("Catalo/gue…") | Adaptive `BoxWithConstraints` — stacked full-width Column + `HorizontalDivider` when `maxWidth < 340.dp \|\| fontScale > 1.15f` (same precedent as Settings › About); compact three-cell Row retained at 1× regular width | ✓ `r2-*-15-work`, `r2-*-20-work` (EN+CS), `r2-small-en-work` — "Catalogue number / Katalogové číslo" fully readable, no wrap/ellipsis |
| N3 | Five-tab bottom nav labels + accessibility descriptions | Explicit full-name `semantics { contentDescription }` on every NavigationBarItem label | ✓ UIAutomator `NAV-EVIDENCE` at 2×: all five items expose complete names (Home/Library/Discover/Practice/Profile); visual ellipsis "Disc…"/"Pract…" remains (documented limitation — 5 tabs cannot fit full words at 2×) |
| N4 | Settings → Data & Storage: headers/toggles/descriptions visually touching | Downloaded-content item restructured as `Column(spacedBy(JournalSpace.medium))` — header, media card, hint, online-catalogue card separated by 16 dp; preference + all warnings preserved verbatim | ✓ `r2-ivory-en-10/15/20-settings_data`, `r2-nocturne-cs-10/20-settings_data`, `r2-nocturne-en-10-settings_data`, `r2-small-en-settings_data` |
| N5 | Discover: repetitive privacy/offline explanations on the landing | One concise notice kept (EN/CZ `stage7a_privacy_notice` shortened to the essential third-party disclosure); online-off hint now only while actually searching (`active && !catalogOnline`); technical network-status line only while searching with the pref ON (`active && catalogOnline`) | ✓ landing captures (4 combos + 1.5×/2×/narrow) show exactly one notice; search-state captures show the contextual hint (pref OFF) / Search-online + network line (pref ON); absence on landing asserted via UIAutomator, not by eye |

### Round-2 limitations (honest)

- Bottom-nav labels still ellipsize visually at 2× ("Disc…", "Pract…"); accessibility
  semantics now carry the full names (verified via UIAutomator dump). Fitting five full
  words at 2× would require shrinking the type scale — deliberately not done.
- At 2× the third artist tab ("Biography") is edge-cut by the scrollable tab row and
  needs one horizontal swipe — labels never break mid-word (scroll affordance).
- Discover category tiles ellipsize at 2× ("Wo…", "Arti…") — pre-existing tile behavior,
  unchanged this round (tiles are horizontally scrollable).
- Capture-automation incidents (not app defects), resolved: fixed-swipe work-card tap
  flaked at 720 px (replaced with dump-driven scrolling); the narrow Data & Storage
  section can open at a preserved scroll anchor (capture now scrolls to the top first);
  `wait_for("Essential works")` timeouts at enlarged fonts are non-fatal (header below
  the fold; real work cards were still tapped via dump-driven detection).

## Round 3 — vertical alignment + Discover privacy popover (owner-reported, 2026-10-22, commit `d7c0aef`)

**51 targeted `r3-*` screenshots** (50 scripted + 1 targeted Appearance & Customize shot)
with SHA-256 manifest (`capture-manifest-r3.txt`) and log (`capture-log-r3.txt`). Host
gates re-run green: BUILD SUCCESSFUL, JVM 174/0F/0E/0S, lint 0 errors / 124 warnings
(unchanged), `assembleDebugAndroidTest` compiles, 5/5 check scripts PASS (EN/CZ parity
951). Seeded in-place upgrade re-PASSED (all table counts + profile_settings row
identical). Emulator left tidy (font 1.0, theme System, locale `[]`, wm physical).

| # | Owner-reported issue (real device) | Fix | Verified |
|---|---|---|---|
| O1 | Text in Settings rows, Discover result cards, Profile cards and shared components appears biased toward the top | Root cause in the shared design system: `journalTypography()`'s `text()` builder lacked `PlatformTextStyle(includeFontPadding = false)` (the Material 3 default the app bypassed) — legacy font padding inflated text boxes with asymmetric extra leading, so centered content read top-biased. Fixed in the one shared builder (app-wide); no padding hacks, no clipping. Structural sweep of every `Row(` in `ui/*.kt`: all shared Rows already `CenterVertically`; one further real defect — `CustomizePanel` "♫" glyph row → `CenterVertically`. Decorative top-aligned quote/info icons intentionally kept | ✓ `r3-ivory-en/nocturne-en/ivory-cs/nocturne-cs-{home,library,discover_landing,practice,profile,settings_landing}` (all six screens × both languages × both themes at 1×); `r3-*-settings_data` (switch centered vs 2-line AND 4-line subtitle blocks); `r3-ivory-en-appearance_custom.png` (glyph node center 1101 vs preview block center 1100.5); `r3-*-15/*-20-*` + `r3-small-*` at 1.5×/2×/narrow — no clipping, no reflow regressions |
| O2 | Remove the large persistent privacy card below the search field; add a small accessible lock icon in the upper-right of the Discover header opening a compact anchored popover (browser site-info style) with the same accurate disclosure; dismiss on outside tap or Back; no misleading HTTPS/security guarantee | Landing card removed; header is now a title/subtitle column + lock `IconButton` with accessible label `stage7a_privacy_info` (EN "Privacy information" / CZ "Informace o soukromí") opening an anchored `DropdownMenu` popover showing the unchanged truthful `stage7a_privacy_notice`; `privacyInfo` state hoisted beside `onlineRequested`. Online-results setting stays in Settings → Data & Storage (default OFF); no network/session behavior changes (asserted: search state captures + untouched `onlineRequested`/`catalogOnline` logic) | ✓ Popover cycle verified ×8 via UIAutomator — each of `r3-{ivory-en,nocturne-en,ivory-cs,nocturne-cs}` (1×), `r3-ivory-en-15`, `r3-ivory-en-20`, `r3-nocturne-cs-20`, `r3-small-en`: landing card **absent**, lock tapped → notice visible (popover shot), outside tap → absent, reopen → Back → absent, still on Discover. Popover shots: `r3-*-privacy_popover.png` (EN/CZ × Ivory/Nocturne × 1×/1.5×/2×/narrow) |

### Round-3 limitations and incidents (honest)

- Round-2 limitations carry over unchanged: bottom-nav labels ellipsize visually at 2×
  (full names carried by verified accessibility semantics); the third artist tab is
  edge-cut at 2× in the scrollable row (swipe to reach; never breaks mid-word); Discover
  category tiles ellipsize at 2× (pre-existing).
- The 2× CZ popover wraps tall (7 lines) but stays anchored and fully contained; its
  dismissal cycle is verified programmatically at every tested scale.
- Capture-automation incidents (not app defects), resolved: on API 36 `uiautomator dump`
  returns **only the popup window** while the DropdownMenu is open (background hierarchy
  excluded) — the first two runs failed hunting the lock node inside the popup dump, so
  the outside-tap point is now derived from the notice node's own bounds (left page
  margin above the popup); at 720×1560 the popup reports `left=0` (full width), where a
  bounds sanity check refused the first derived tap — the notice-node approach handles
  both dump modes; the script now resets `wm size` at startup after an interrupted run
  left the override active.

created by stokonn with love <3
