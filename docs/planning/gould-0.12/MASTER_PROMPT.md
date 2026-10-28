# GOULD 0.12 — Codex Master Prompt

You are the implementation engineer for **Gould 0.12**, the next version of the existing native Android app at https://github.com/stokonn/Gould (formerly Goldberg). Continue its real codebase and approved visual language; **do not reinvent a new application**.

## First: mandatory preflight, without risky mutations

1. Find the owner's canonical checkout (normally `~/Projects/Opus`). Read the repository's root `AGENTS.md` **in full**, `docs/ai/STATE.md`, `docs/ai/HANDOFF.md`, `docs/ai/DECISIONS.md`, `docs/ai/VERIFICATION.md`, `docs/ai/GOULD-MAIN-MAINTENANCE.md`, `docs/maintenance/ADMIN-SAFETY-REVIEW.md`, `docs/planning/gould-0.12/STAGE-CHECKLIST.md`, and existing PMP instructions/checkpoints. Check the actual Git remotes, clean/dirty state, current main commit, worktrees, relevant branches, outstanding safety and maintenance gates, and current CI. Never assume this bundle describes the very latest state.
2. Read `docs/planning/gould-0.12/WIDGETS_SPEC.md` (widget requirements MUST NOT be dropped), `docs/planning/gould-0.12/IMPLEMENTATION_BRIEF.md`, `docs/design/gould-0.12/README.md`, and `docs/design/gould-0.12/ACCEPTANCE_MATRIX.md`. Inspect available local private reference images (not for publication). Inspect 0.10/0.10.1/0.11 approved refs and actual Compose screens. Report discrepancies before implementing; owner requirements and actual app behavior supersede illustrative image pixels.
3. **STOP if unresolved maintenance/security prerequisites block safe 0.12 work.** In particular, do not merge `maintenance/admin-safety-review` or delete protected historical worktrees/private data without separate authorization. Do not modify `main` for feature work. Summarize blocked items and the precise safe next action. Do not take unrelated maintenance cleanup into your own hands.
4. When preflight proves a safe baseline and this prompt has been intentionally submitted by the owner to begin development, establish a **dedicated feature branch/worktree for 0.12** using safe Git operations, preserving all existing work. Snapshot status and commit ancestry. No resets, force pushes, history rewriting, secret export, signing-key changes or unreviewed deletions.

## Scope: ONE complete development stage, internal checkpoints allowed

Implement the complete 0.12 feature scope in `IMPLEMENTATION_BRIEF.md`, with this acceptance list:

**A. Studio and audio**
- Rename user-facing Practice to Studio; keep existing five bottom destinations Home / Library / Discover / Studio / Profile. Keep `PracticeCoordinator` as one serialized timing owner. No duplicate timers or duplicate records. Manual fallback stays.
- Correct Smart Practice activity-credit behavior: main timer is credited playing time in Smart mode, not raw wall time; show listening/playing/auto-pause/manual-pause/error states and optionally secondary elapsed session time. Account accurately for natural short musical rests, long silence, sensor stalls and recovery. Test actual algorithms before claiming any detection bug fixed.
- Add optional, **user-started continuous audio recording from Record to Stop** (option A, which supersedes earlier option B), preserving silence, rests and reverberation in the stored file. Smart Practice activity detection and recording must coexist safely (prefer shared mic capture ownership). Never start recording simply by opening Studio or enabling Smart Practice, and never auto-resume capture after process recovery. Respect permissions, privacy indicator, foreground behavior, interruptions, small/large files, data integrity, corruption/low-storage recovery, deletion and export. Keep saved audio duration independent of credited playing duration; recordings are local by default.
- Preserve compact Smart Metronome, Smart Practice, multi-piece Session Queue (manual switch + Undo where supported), and unobtrusive notes. The Studio upper-right history glyph opens Previous. No intrusive red Stop button: use the configured semantic accent or neutral styling, with a small restrained recording indicator.

**A2. Android Home Screen Widgets — REQUIRED (not optional)**
- Implement owner-approved **Quick Practice (A)**, **Weekly Activity (C)**, **Intelligent Open (B)**, **Adaptive Sizes (B)** and **Stale/last-known-data behavior (B)**, **Monday–Sunday week (A)**. Follow `WIDGETS_SPEC.md` in full; earlier packages accidentally omitted widgets.
- Widgets are native Android launcher widgets, not new tabs. Quick Practice takes users safely to Studio; widgets must NEVER automatically begin a timer, microphone capture, or audio recording. Derive weekly charts from persisted actual credited practice time, not elapsed time or recording length.
- Support applicable compact/expanded sizes, correct empty/error/last-updated states, offline mode, app appearance/CZ-EN/accessibility and efficient updates. Test on Android API 36 emulator launcher, resizing/taps/date change/reboot and screenshot variants.
- Check existing app category/distribution metadata: consider Education app classification where supported without confusing `widgetCategory` with Play Store category or breaking manifest compatibility. No invented sample data, personal info uploads, or screenshot asset bundling.

**B. Library and Previous**
- Keep existing Library structure, including current search + add, All / My Repertoire / Artists, sort/filters and catalogue/local separation. **No Folders** and no new permanent bottom tab.
- Add a discreet Library entry to the dedicated Previous screen (also accessible from Studio), containing two tabs: Practice History and Recordings. Preserve historical sessions' full edit, per-piece credit and notes semantics, and safely list/play/manage only actual local audio recordings. Existing URL/bookmark recordings are a separate data type and must remain intact.

**C. Profile**
- Preserve avatar alongside name/bio, admin capability gate, Favorite Composer, badge showcase and Settings entry. Compact the **four statistics into an evenly sized, centered 2×2 grid** (numbers and labels horizontally and vertically centered) with accessible reflow for large font/Czech text. The images' four-across layouts are NOT approved.
- Add Favorite Era autocomplete alongside Favorite Composer, backed by actual supported periods/local data, with proper accent-insensitive matching. Prevent unsaved profile changes on Back, dismissal, rotation or navigation: autosave only when truly persisted and/or display Save / Discard / Keep editing confirmation. Never display false success.

**D. Achievements and exclusive early-user badge**
- Preserve all 17 historical badge IDs, earned timestamps, original medals, progress, showcase selections and backup/upgrade semantics.
- Make collection cards equal-size per row/consistent in position (medal/name/rarity/date/progress); adaptive 2-column normal screen or 1-column on constrained font/width; no truncation of the important text.
- Design and implement validated new achievement definitions corresponding to: first saved recording, 10 saved recordings, three different pieces in one session, ten valid notes, verified playback of an owned saved recording. No click-only fake credit. Verify name/threshold details against this brief before treating them as final.
- Add **Founding Pianist** (Legacy / Legendary) for eligible practice performed *before the eventual actual Gould 1.0 stable release cutoff*, including older Goldberg users with preserved qualifying historical sessions. Pre-1.0 new users may earn it; users starting only after 1.0 must not. Preserve permanent unlocks, late legacy migration and export/restore. Do not claim an offline clock or local history is tamper-proof. Do not silently grant unearned awards.
- Implement a non-intrusive, themed badge-unlock popup (not only Snackbar), supporting multiple unlocks in a queue, once-per-event display acknowledgment, postpone during active playing, View Badge and Dismiss; reduced-motion and process recovery safe.

**E. Tux and Home daily quote**
- Fix only broken Tux ASCII alignment. Retain original pink terminal style, exact original ASCII intent, monospace spacing, kernel-panic message, access path and signature `created by stokonn with love <3`. Inspect `TuxScreen.kt`, 0.10.1 approval docs, the prior actual emulator screenshot and any approved PRIVATE local Tux reference. Test narrow phones and text scaling. Never invent a new penguin illustration.
- Keep current Home layout unchanged except add a subtle, **locally bundled legally permitted original/licensed nature landscape** inside the existing upper-right daily quote card, with overlay for text contrast; no network requirement, no image spanning the whole Home and no new action buttons. Preserve quote source, current cache/offline fallback, attribution, CZ/EN, variable accent, light/dark, accessibility. An AI illustration in this ZIP is **not an approved production media asset**, and any quote text in a mockup is placeholder content.

## Precedence and safety

- Priority: current explicit owner decisions > `AGENTS.md` and mandatory security/release controls > accepted `IMPLEMENTATION_BRIEF.md` and `ACCEPTANCE_MATRIX.md` > verified previous app references > concept images. Where safety policies conflict, stop and ask.
- Preserve `com.opus.piano`, `opus.db`, compatible in-place upgrades, local personal data, existing Room migrations and backup format readers, source provenance, original signing certificate, historical tags/releases, privacy constraints and independent PMP repository. New recording/notification/legacy-badge persistence must have properly tested additive migration/backup logic where necessary.
- **No physical phone actions at all**. ADB only with an explicitly selected emulator serial. Use debug builds for development; no release candidates, signing/key actions, main merge, tag, GitHub Release, production deploy or cloud publication without separately explicit owner authorization. No auto-start microphone, no background-upload of personal audio, no community networking expansion.
- Routine safe coding, tests, intermediate commits, updates to the worktree's feature branch and handoff files can proceed per verified owner policies; do not nag for every normal action.

## Verification and handoff

- One development stage in `docs/planning/gould-0.12/STAGE-CHECKLIST.md`; use internal milestones for review, not new public stages. Update AGENTS-compatible STATE/HANDOFF/VERIFICATION/DECISIONS and PMP at safe checkpoints, retaining older history.
- Verify Kotlin/Compose build, JVM and relevant Android instrumentation (API 36 preferred if supported), lint, accessibility, offline tests, 0.11→0.12 in-place seeded upgrade, backups/restore, Room/recording retention, legacy unlock, profile save-draft, re-entry/recovery, Smart audio capture contention and correct credits. Capture named emulator comparisons in Ivory/Nocturne, CZ/EN and narrow/large-font. Clearly distinguish actual executed test results from proposed tests and visual mockups.
- Provide exact changed files/commits, tests and numbers of cases, safe debug APK location/hash if made, screenshot comparisons, open blockers and owner-visible visual sign-off points. **Do not mark complete or release until accepted.** If maintenance is still blocking, present an audit/checklist only and do not pretend feature work happened.

This master prompt is an implementation handoff, not permission for destructive admin work or stable publication.

created by stokonn with love <3
