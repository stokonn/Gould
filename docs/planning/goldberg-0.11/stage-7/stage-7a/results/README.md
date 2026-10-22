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

created by stokonn with love <3
