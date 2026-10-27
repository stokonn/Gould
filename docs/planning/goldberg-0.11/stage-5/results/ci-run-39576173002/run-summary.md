# Screenshot capture runs 39571926478 → 39576173002 (Stage 5 task 5.11, screenshots in both themes)

**GREEN RUN: [39576173002](https://github.com/stokonn/Goldberg/actions/runs/39576173002)
@ `3579417` (2026-10-21) — all steps passed; 12/12 screenshots captured and
agent-inspected.** The three prior attempts failed on three distinct *script*
defects, each root-caused from raw logs (never an application defect) and fixed
via workflow-only PRs #36–#38; the audit trail is preserved in the Issue #31
checkpoint comments.

## What the green run did (workflow `.github/workflows/stage5-screenshots.yml`)

- Same proven configuration as the green verification runs: API 36 /
  google_apis / x86_64 / pixel_6 / KVM, `ubuntu-24.04`, emulator-5554 with the
  `ro.kernel.qemu=1` guard; SHA binding asserted (`1ae42ba…` history; this run
  `3579417 == GITHUB_SHA` per `source-commit.txt`).
- Installed the candidate debug APK (throwaway runner keystore), drove the real
  UI via the accessibility tree (onboarding dismissed through its own **Skip**
  button), switched the theme through the product's own persistence
  (`profile_settings.theme` in Room `opus.db` via `run-as` + `sqlite3`, with
  readback assertion) and relaunched through the normal startup path.
- Captured Home / Repertoire / Discover / Practice / Profile / Settings in
  **Ivory (`theme='Light'`)** and **Nocturne (`theme='Dark'`)**, then reset the
  disposable emulator to `System`.

## Executed evidence in this directory

| File | Content |
|---|---|
| `png/ivory-*.png`, `png/nocturne-*.png` | 12 screencaps, 1080×2400, 116–232 KB each (SHA-256 in `png-sha256.txt`) |
| `source-commit.txt` | `GITHUB_SHA == checkout HEAD` binding |
| `prime.txt` | `id=1 row count: 0 (upsert handles both)` — independently confirms the run-3 root cause: fresh installs have no settings row |
| `theme-readback.txt` | final reset readback `'System' (expected 'System')`; the Light/Dark readbacks passed their assertions in-run (visible in the job log; the file is overwritten per call) |
| `launch.txt` | cold start `TotalTime 9391 ms`, focus verified on `com.opus.piano.MainActivity` |
| `capture-manifest.txt` | file listing + final focus |
| `install.txt`, `wm-size.txt`, `wm-density.txt`, `logcat.txt` | install OK; 1080×2400 @ 420 dpi; full logcat |

## Attempt history (each failure root-caused BEFORE any re-dispatch)

| Run | Commit | Result | Root cause |
|---|---|---|---|
| 39571926478 | `1ae42ba` | FAILED pre-capture (5 m 9 s) | `set_theme` ran before the app's first launch → `databases/opus.db` did not exist (`unable to open database file`). Fix: `prime_app()` (PR #36). |
| 39572917657 | `df8376c` | FAILED pre-capture (7 m 3 s) | SQL passed as an adb-shell argument was split by the **device shell** re-parse → sqlite received only the bare `UPDATE` (`in prepare, incomplete input`; proven byte-for-byte on the host). Fix: stdin-piped `sql()` (PR #37). |
| 39574447299 | `e1351b6` | FAILED at readback guard, exit 42 (7 m 34 s) | Fresh install has **no `profile_settings` row** (app's own `?: ProfileSettings()` fallback) → `UPDATE … WHERE id=1` matched 0 rows silently → empty readback; the script's own assertion caught it. Fix: upsert of the entity-default row (PR #38; host-proven for both row states). |
| 39576173002 | `3579417` | **GREEN (9 m 4 s)** | — all 12 captures succeeded. |

## Agent visual inspection results (2026-10-21, all 12 inspected)

Every Ivory/Nocturne pair shows the **identical layout with only the palette
changed** — correct semantic theming, adapted system bars, no blank/lock
frames, no clipping, real content:

- **Home** — greeting + Schumann quote card, Today's Practice/Current streak
  cards, weekly-activity chart, "Recently practiced" empty state, 5-tab nav.
- **Repertoire** — Search Library, All/My Repertoire chips, Sort/Filters,
  "No matching compositions" empty state + Discover action.
- **Discover** — offline-first messaging ("Only an active online catalogue
  search sends your query…"), unchecked online checkbox, media-preference
  notice, filter chips, `0 B` cache + clear controls, history toggle.
- **Practice** — Free practice timer card (00:00:00 · Ready), Smart Practice
  toggle off, Start Practice (brown in Ivory / gold in Nocturne), disabled
  Finish & Review.
- **Profile** — Musician card (app-default row created by the upsert),
  stats tiles, Favorite Artist, Badge showcase, trophy + Settings top-right.
- **Settings** — full section structure (Google account / Appearance /
  Smart Practice / Data & storage).

Notes for honesty: screenshots are fresh-install states (empty journal by
design); Google sign-in deliberately untouched; the emulator is disposable and
no owner data was ever involved. Owner visual sign-off on these artifacts is
the remaining courtesy step, not a technical blocker.

*created by stokonn with love <3*
