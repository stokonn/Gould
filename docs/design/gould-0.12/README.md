# Gould 0.12 — private design reference bundle

**State:** owner-liked 0.12 visual direction, NOT pixel-perfect final approval and NOT implementation proof. This bundle is for preflight + isolated development after safety gates, not a production image library.

## Where these files go

Extract this ZIP **at the root** of the existing repository `~/Projects/Opus` (actual GitHub repo `stokonn/Gould`). It provides:

- `docs/planning/gould-0.12/IMPLEMENTATION_BRIEF.md` — full approved product and safety design scope.
- `docs/planning/gould-0.12/WIDGETS_SPEC.md` — **REQUIRED** complete home-screen widget specification.
- `docs/planning/gould-0.12/MASTER_PROMPT.md` — Codex instruction file, to paste or point Codex at.
- `docs/design/gould-0.12/README.md` — current file and hierarchy.
- `docs/design/gould-0.12/ACCEPTANCE_MATRIX.md` — screen-specific decisions and image mismatches.
- `docs/design/gould-0.12/ASSET_MANIFEST.md` — descriptions/hash for references.
- `docs/design/gould-0.12/references/*.png` — **PRIVATE local visual evidence/examples**. Do not commit/push them to the public repository and do not package them in the APK.

**IMPORTANT:** The PNGs have been intentionally chosen/cropped to avoid carrying the owner's profile photo and full name. They are still private conversation/reference materials. Keep them locally only. Images may show fictional data, generic portraits, outdated labels, nonlocal screenshots and imperfections. Never infer schema/features from an image alone.

## Design precedence

1. Explicit owner direction and root `AGENTS.md`/safety controls.
2. `IMPLEMENTATION_BRIEF.md` + `ACCEPTANCE_MATRIX.md` in this bundle, plus 0.12 stage checklist.
3. Verified 0.10/0.10.1/0.11 design approvals and real app source/UI.
4. These illustrations only for color, spacing and general visual feel.

## Required no-regression rules

**Studio:** Existing timer emphasis, Smart activity credit, continuous optional recording from Record to Stop, compact metronome/queue/notes, history shortcut. No bright-red Stop.

**Library:** Existing All / My Repertoire / Artists, Search + Add, Sort/Filters. `Previous` is a linked separate destination with Practice History and Recordings; not a fourth Library chip and no Folders.

**Profile:** Avatar and name **side by side** as in 0.11; four statistic boxes **2×2** compact, centered in both axes; Favorite Era autocomplete, save/leave guard. Do not copy AI four-column Profile mockups.

**Widgets:** Required Quick Practice A + Weekly Activity C, intelligent open B, adaptive B, offline stale last-known-data B, week Monday–Sunday A. On Android home screen, **not** a new bottom navigation tab. Never start microphone/recording/timer just from a widget tap. Review `WIDGETS_SPEC.md`.

**Badges:** Adaptive equal-size cards, existing permanent medal art, Legacy Founding Pianist, new music/practice achievements, deferred nonblocking unlock popup.

**Tux:** ONLY repair existing ASCII spacing/rendering; pink terminal + exact signature unchanged. Screenshot is a bug reproduction, not a replacement penguin artwork.

**Home quote:** A calm nature background **only in existing quote card**, with readable scrim and locally available licensed asset. Do not use example image/caption as app media or quote source.

## Approval / delivery gate

Before release, compare the actual emulator screens against the approved previous UI and this bundle. Ask owner for **visual sign-off on Studio/Previous, compact Profile, badges/popups, Tux, Home quote, and both launcher widgets**. Never auto-merge main or publish a stable release.

created by stokonn with love <3
