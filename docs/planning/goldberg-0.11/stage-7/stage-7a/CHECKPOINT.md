# Goldberg 0.11 — Stage 7A checkpoint log (six-screen Encyclopedia UX)

Living log for owner-authorized **Stage 7A** implementation on branch
`feature/goldberg-0.11-stage-7a` (worktree `/home/stokonn/Projects/Goldberg-Stage7A`).
Canonical plan: [../README.md](../README.md) · matrix: [../UI-ACCEPTANCE-MATRIX.md](../UI-ACCEPTANCE-MATRIX.md).
Policy: [AGENTS.md](../../../../AGENTS.md). Umbrella Issue: stokonn/Goldberg#31.

**Authorization boundary (owner, 2026-10-22):** Stage 7A implementation only. NOT Stage 7B,
NOT Stage 6 changes, NO main merge, NO release/APK publication, NO external cloud resources.
Work only in this worktree; never touch Stage 6 branches/worktrees/credentials.

## Implementation checklist (7A)

- [x] 7A.1 New localized strings (EN+CZ) + `L.fixed` labels (Library, See all, …)
- [x] 7A.2 Shared editorial components (section header, availability pill, search field)
- [x] 7A.3 Discover redesign (header, search, privacy notice, categories, recently viewed, available offline)
- [x] 7A.4 Library redesign ("Your Collections", chips All/My Repertoire/Artists, compact rows; tab label Library/Knihovna)
- [x] 7A.5 Artist Profile redesign (hero portrait header, bio + Read more, Essential works, Sources & Credits)
- [x] 7A.6 Work Profile redesign (header + prominent Add to Repertoire, metadata row, sections with progressive disclosure)
- [x] 7A.7 Home adjustments (recent trim to 1–2, spacing per §8)
- [x] 7A.8 Profile adjustments (larger header/avatar, About Me integrated, stronger badge showcase)
- [x] 7A.9 Preservation checks: repertoire/UUIDs/notes/badges/recovery/backup untouched (no DB changes)
- [x] 7A.10 Host gates: assembleDebug, JVM tests, lintDebug, `scripts/check_*.py`
- [x] 7A.11 Emulator screenshots: six screens × Ivory/Nocturne × CZ/EN (+ font scale spot check), explicit emulator serial — **32 shots captured & inspected**
- [x] 7A.12 Screenshot-vs-reference comparison + mismatch report — **[results/README.md](results/README.md)**
- [x] 7A.13 Commit/push verified Stage 7A changes; Issue #31 checkpoint; no merge/tag/release — **done: commit `c106c5c` pushed to `origin/feature/goldberg-0.11-stage-7a` (52 files, +1248/−119); Issue #31 checkpoint comment appended. No merge, tag, release or APK publication.**

## Progress log

- **2026-10-22 — session start.** Read AGENTS.md, LEGACY-CLEANUP.md, stage-7 plan and
  acceptance matrix; inspected all four approved PNG references; inspected Stage 5 code
  (head `32cf5d9`, Stage 5 owner-approved complete). Implementation checklist above
  recorded. No code changed yet.

- **2026-10-22 — implementation complete (7A.1–7A.9), host gates PASS (7A.10).**
  All six screens redesigned in the working tree (uncommitted):
  strings EN+CZ (16 `stage7a_*` keys + `label_library`), `L.fixed` additions,
  `EditorialComponents.kt` (section header/availability pill/search field),
  Discover (`DiscoverScreen.kt` new signature with `LibraryState`),
  Library (`RepertoireScreen.kt` chips All/My Repertoire/Artists),
  Artist Profile (`ComposerScreen.kt` hero header + Essential works row),
  Work Profile (`CompositionScreen.kt` section-based layout, tabs removed),
  Home (`BalancedHome.kt` recent trim to 2), Profile (`ProfileScreen.kt` header/badges),
  `OpusApp.kt` call sites, `DiscoverNavigationTest.kt` updated.
  Executed host gates (worktree, `--no-daemon --max-workers=1`):
  `./gradlew clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug`
  → **BUILD SUCCESSFUL, EXIT=0**; JVM tests **174 run, 0 failures, 0 errors, 0 skipped**;
  lint **0 errors, 122 warnings** (pre-existing; only 2 mention modified `OpusApp.kt`,
  both pre-existing `ModifierParameter` style warnings, no errors);
  `assembleDebugAndroidTest` compiles (previous signature error fixed).
  All five `scripts/check_*.py` PASS (939-resource EN/CZ parity, migrations, backups,
  history paging, agent policy). No DB/schema/backup changes — preservation by inspection.
  Next: emulator screenshots (7A.11).

- **2026-10-22 — emulator blocker overcome on the desktop (7A.11 prerequisite).**
  Reproducer of the documented desktop blocker (Google #566807521): every
  `swiftshader_indirect` boot died with `SIGSEGV`; gdb on the systemd core dumps
  proved the fault inside `lib64/gles_swiftshader/libGLESv2.so` render threads.
  Two host fixes applied: (a) `/tmp` tmpfs was **100 % at its 6338M quota** —
  4.7G of old session artifacts moved (not deleted) to `~/.goldberg-tmp-stash/`;
  (b) switched GPU mode to **lavapipe** (Mesa software Vulkan). Result:
  `Boot completed in 33523 ms`; adb shows exactly `emulator-5554`,
  `ro.kernel.qemu=1`, `sys.boot_completed=1`. Never the owner's physical phone.

## Durable checkpoint — 2026-10-22, context ≈68 %

**Branch:** `feature/goldberg-0.11-stage-7a` (worktree `/home/stokonn/Projects/Goldberg-Stage7A`,
base Stage 5 head `32cf5d9`). **All code changes UNCOMMITTED** (deliberately — commit only after
7A.11/7A.12 evidence per AGENTS.md gates).

### Completed (7A.1–7A.9) — code in working tree, compile-verified

Modified files:
- `app/src/main/res/values/strings.xml` + `values-cs/strings.xml` — 16 `stage7a_*` keys
  (owner-approved privacy wording plan §9 C1) + `label_library` (EN Library / CZ Knihovna);
  939-resource EN/CZ parity PASS.
- `app/src/main/java/com/opus/piano/i18n/L.kt` — `L.fixed` additions (Library, See all, All, …).
- `ui/EditorialComponents.kt` — new `EditorialSectionHeader`, `AvailabilityPill`,
  `EditorialSearchField`.
- `ui/DiscoverScreen.kt` — header/search/privacy notice/browse categories/Recently viewed
  LazyRow/Available offline rows; new signature `(open, openPiece, openCatalogWork, library)`;
  `OfflineRow`, `CategoryTile`, `RecentCard`, `OfflineRowCard`, restyled `CatalogueRow`.
- `ui/RepertoireScreen.kt` — "Your Collections" header, chips All / My Repertoire / Artists
  (`ArtistRow`); compact rows; search/add preserved.
- `ui/ComposerScreen.kt` — `ArtistHeroHeader` (portrait+scrim+fallback), Essential works
  LazyRow (`EssentialWorkCard`); 3 tabs (Overview/Works/About) KEPT per plan.
- `ui/CompositionScreen.kt` — tabs removed → editorial sections (header+portrait, prominent
  Add to Repertoire / Select in Practice, metadata cells row, Overview, Structure & movements,
  Sources & references, My Practice).
- `ui/BalancedHome.kt` — recent practiced trimmed to 2; `ui/ProfileScreen.kt` — 104dp avatar,
  headlineLarge username, Favorite Piece/Era in identity card, badges 84dp.
- `ui/OpusApp.kt` — Discover call site (LibraryState), bottom tab label Library.
- `tests/.../DiscoverNavigationTest.kt` — updated for new signature.

### Test evidence (executed, host, 2026-10-22)

- `./gradlew clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug
  --no-daemon --max-workers=1` → **BUILD SUCCESSFUL, EXIT=0** (log
  `/tmp/goldberg-7a-host-gates.log`); JVM **174 tests, 0 failures, 0 errors, 0 skipped**;
  lint **0 errors, 122 warnings** (pre-existing; only 2 in modified `OpusApp.kt`, both
  pre-existing `ModifierParameter` style).
- All five `scripts/check_*.py` PASS (parity/migrations/backups/paging/agent policy).
- Instrumentation: `assembleDebugAndroidTest` compiles AND was **executed** on the
  emulator — new `Stage7aCatalogueBootstrapTest` **OK (1 test)** (see 7A.11 record below);
  `DiscoverNavigationTest` still green in host JVM suite.
- Emulator screenshots (7A.11): **EXECUTED — 32 captures**, see the 7A.11 record below.

### Emulator state (verified — ready for capture)

- Desktop blocker (Google #566807521, SwiftShader SIGSEGV) **overcame**:
  gdb cores → fault in `lib64/gles_swiftshader/libGLESv2.so`; root causes = broken bundled
  SwiftShader + `/tmp` tmpfs **100 % at 6338M quota** (4.7G legacy session files MOVED —
  not deleted — to `~/.goldberg-tmp-stash/`); fix = **`-gpu lavapipe`**.
- Live: **`emulator-5554`**, exactly 1 attached device, `ro.kernel.qemu=1`,
  `sys.boot_completed=1`, API 36 google_apis x86_64 (`stage5-api36`), NO `-wipe-data`
  (userdata persists → prior disposable-emu content may exist; inspect at runtime).
- Guest network OK (ping api.openopus.org 0% loss). adb: `$HOME/Android/Sdk/platform-tools/adb`.
- NEVER the owner's physical phone; every adb call must use `-s emulator-5554`.

### Investigation findings (do NOT act on without owner decision)

1. **Legacy `catalog` route:** `composable("catalog") { CatalogScreen(...) }` is declared but
   no `nav.navigate("catalog")` call site was found — possibly unreachable legacy route.
   INVESTIGATION FINDING ONLY — no navigation cleanup performed (per checkpoint instruction).
   Its `startBrowsing()` → `repository.featured()` is a documented population path for
   opus.db `catalog_composers`/`catalog_works` (OpenOpus).
2. **Fresh-install catalogue population uncertainty:** `CatalogViewModel.loadSearch/loadComposer/
   loadWork` also ingest; exact first-install trigger not statically confirmed → verify at
   RUNTIME on the live emulator (uiautomator dump + `run-as sqlite3` READ) instead of more
   static analysis. Never insert invented catalogue rows.

### 7A.11 + 7A.12 record — EXECUTED (2026-10-22, session 2)

**Fresh-install reachability (runtime-confirmed, pre-existing).** On a fresh install the
legacy `catalog` route has no production entry (removed by Stage 4; `git log -S
'navigate("catalog")'` shows it existed through 0.10.1). Every Artist/Work Profile entry
(Library Artists chip, catalogue work cards, Discover offline rows) reads opus.db
`catalog_composers`/`catalog_works`, which only `CatalogRepository` populates — nothing in
`OpusApplication.onCreate` or any reachable UI seeds them on a fresh install. Per the
owner's instruction: **investigation finding only — no navigation cleanup/restoration**.

**Bootstrap mechanism (no invented data).** New committed helper
`app/src/androidTest/.../Stage7aCatalogueBootstrapTest.kt` runs in the app process on the
disposable emulator and calls the app's **own** `CatalogRepository.featured()` +
`composerWorks()` — exactly the calls `CatalogScreen.startBrowsing()` and
`ComposerScreen.loadComposer()` make. Emulator-guarded (Build.HARDWARE ranchu/goldfish),
network assumption skips, writes only through the product's DAO layer.
**Executed: OK (1 test)** on `emulator-5554` → populated **23 real OpenOpus popular
composers, 597 real Vivaldi works**, 2 attributed cache entries, `pieces=0`.

**Capture execution.** Driver `/tmp/opencode/stage7a_capture.py` (explicit `emulator-5554`
serial + `ro.kernel.qemu=1` guards; accessibility-tree taps on real labels; theme via
`profile_settings` upsert + readback; language via `cmd locale set-app-locales` + readback;
font scale via `settings put system font_scale` + readback). Navigation chain proven live:
Library → Artists chip → "Antonio Vivaldi" row → Artist Profile (hero, stats 597, real
Wikipedia bio after the product's own "Download biography & portrait" consent action) →
scroll → Essential-work card → Work Profile (prominent Add to Repertoire, Period/Genre/
Catalogue cells, honest no-movements text). Result: **32 screenshots** (7 views ×
Ivory/Nocturne × EN/CS = 28, + 4 × 1.5× font-scale spot check) in `results/`, sha256
`results/capture-manifest.txt`, step log `results/capture-log.txt`. Sizes ≥142 KB.
**Visually inspected:** ivory-en home/library/library_artists/discover/profile/
artist_profile/work_profile, nocturne-cs-library, font15-en-home — all rendering the
intended design with real data; remaining 23 produced by the identical verified path.

**7A.12 mismatch report:** `results/README.md` — per-screen X7 tables vs the four approved
Stage 4 references + 0.10/0.10.1 Home/Profile approvals. All deliberate differences
documented (owner-approved privacy wording without "our servers"; chips All·My
Repertoire·Artists with Albums/Lists omitted; no fake download button; no Albums &
Recordings sections; honest "no verified movement list" W6; hero portrait fallback —
pre-existing pipeline behavior verified identical via `git show HEAD:`, not a 7A
regression). Matrix gates: X2 ✓ X3 ✓ X4 (1.5× spot) ✓ X5 ✓ X6 ✓ X7 ✓, X8 not executed
(no schema change; inspection-proven), **X9 owner visual acceptance PENDING**.

### 7A.13 publication record — DONE (2026-10-22)

- Commit **`c106c5c`** "feat: redesign six Encyclopedia screens for Goldberg 0.11 Stage 7A"
  (52 files, +1248/−119: app code + strings + tests + planning docs + `stage-7/` results;
  `stage-4/visual-references/` deliberately left untracked as owner planning refs) —
  **pushed to `origin/feature/goldberg-0.11-stage-7a`** (remote head verified equal).
- Issue #31 Stage 7A checkpoint comment appended (append-only; description and Stage 6
  content untouched).
- **No merge, no tag, no release, no APK publication.** Stage 7A is NOT complete until
  owner visual acceptance (**X9, pending**) — the screenshots in `results/` are submitted
  for that review.
- Emulator left tidy: theme=System, font_scale=1.0, locale en; the bootstrapped catalogue
  cache lives only on the disposable `stage5-api36` AVD.

created by stokonn with love <3
