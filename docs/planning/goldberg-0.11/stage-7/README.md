# Goldberg 0.11 — Stage 7: Encyclopedia UX (six-screen redesign) — PLANNING PROPOSAL

Status: **approved direction, planned but unimplemented.** Prepared 2026-10-20 as a
planning/documentation-only deliverable (applied on the isolated
`docs/goldberg-0.11-stage-7-planning` branch, uncommitted pending owner approval). No UI
code has been changed for Stage 7; no emulator screenshots of Stage 7 screens exist; the
visual mockups are owner-approved design direction, **not** evidence of implemented
functionality.

Canonical policy: [AGENTS.md](../../../../AGENTS.md). Stage register:
[STAGES.md](../STAGES.md). Living checklist: [STAGE-CHECKLIST.md](../STAGE-CHECKLIST.md).
Executed-evidence ledger for the accepted criteria: this stage's tasks, once authorized.

## 1. What Stage 7 is for

Stage 7 restyles and completes the encyclopedia experience on top of the verified
Stage 4 catalogue architecture and (once verified) the Stage 5 offline bundles. It brings
the six approved screens in line with the owner's editorial/classical-music visual
direction and finishes the UX contracts that Stage 4 deliberately deferred as
"reliability-first". All six screens belong to Goldberg **0.11**; none is deferred to 0.12.

Stage 7 has two parts:

- **Part A — six-screen encyclopedia UX (this document).** Depends on stable Stage 4
  contracts and Stage 5 bundle states. Ready to implement once Stage 5 verification
  (tasks 5.10–5.13) completes.
- **Part B — Android/Web Admin clients (unchanged, still gated).** Per [STAGES.md](../STAGES.md)
  Stage 7, admin queue/diff/source/decision UI depends on Stage 6 backend work and D4/D5/D8.
  Part B is out of scope for the six-screen work and is not started.

## 2. Screens in scope

| # | Screen | Current source (verified, Stage 4 / 0.10.1) | Reference |
|---|---|---|---|
| 1 | Discover | `app/src/main/java/com/opus/piano/ui/DiscoverScreen.kt` (+ Stage 4 catalogue screens) | `docs/planning/goldberg-0.11/stage-4/visual-references/01-discover.png` |
| 2 | Library / Your Collections | `ui/RepertoireScreen.kt`, `ui/CatalogScreens.kt` | `…/visual-references/02-library.png` |
| 3 | Artist Profile | `ui/ComposerScreen.kt`, `ui/ComposerPortrait.kt` | `…/visual-references/03-artist-profile.png` |
| 4 | Work / Composition Profile | `ui/CompositionScreen.kt` | `…/visual-references/04-work-profile.png` |
| 5 | Home | `ui/BalancedHome.kt` | `docs/design/goldberg-0.10/screens/01_HOME_APPROVED.png`, `composites/HOME_PROFILE_LATEST_APPROVED.png`, this document §7 |
| 6 | User Profile | `ui/ProfileScreen.kt`, `ui/BadgesScreens.kt` | `docs/design/goldberg-0.10/screens/02_PROFILE_APPROVED.png`, `docs/design/goldberg-0.10.1/APPROVAL_MATRIX.md` (Profile section), this document §8 |

Reference README: [visual-references/README.md](../stage-4/visual-references/README.md).
Home/Profile have **no dedicated 0.11 mockups** among the four supplied PNGs; their
approved direction is the owner's Stage 7 instruction (captured here and in
`docs/ai/DECISIONS.md`) plus the still-binding 0.10/0.10.1 approvals. Newer explicit
instructions always win over older mockup pixels (e.g. the 0.10 Home mockup's gear is
superseded by the 0.10.1 and 0.10.1 approvals: no gear on Home).

## 3. Shared visual design system (all six screens)

- Editorial, Apple-Music-Classical-inspired; elegant, minimal, refined.
- Cormorant Garamond (display) + Inter (UI) via the existing theme system; centralized
  semantic tokens in `ui/Theme.kt`/`EditorialComponents.kt` — extend, don't fork.
- Ivory (light) / Nocturne (dark) both required; existing user accent-color and
  appearance preferences must keep working.
- Consistent hierarchy, spacing, compact layouts without excessive empty space.
- CZ/EN localized resources, real plurals, application-locale dates/numbers.
- Accessible contrast, Android font scaling (incl. 1.5×/2× reflow), ≥48dp touch targets,
  reduced-motion respected.
- Do not copy iPhone device chrome from mockups; keep Android system insets and the
  existing five-tab navigation (Home · Library · Practice · Profile · Discover).
- Never bundle reference PNGs or mockup artwork into the APK.

## 4. Discover (approved direction)

Primary music-exploration/encyclopedia destination; stays a separate bottom tab.

Required structure (from `01-discover.png` + owner instruction):

1. Elegant "Discover" header + subtitle.
2. Prominent search field ("Search music, artists, works…").
3. Privacy/source notice under the search field using the owner-approved wording (§9 C1):
   EN "Local search works offline. When you explicitly start an online search, your query
   is sent to external music-data providers." / CZ "Místní vyhledávání funguje offline.
   Když výslovně spustíte online vyhledávání, dotaz se odešle externím poskytovatelům
   hudebních dat." — subject to implementation accuracy review.
4. Browsable categories row (Works, Composers, Recordings, …) with "See all" where a real
   paged destination exists.
5. Recently Viewed section (cards showing Local/Online availability).
6. "Available offline" section — locally available music/resources with clear Local
   labels, distinct from online-only content.
7. Consistent navigation into Artist / Work / Recording / Album details and back.

Functional requirements (preserve verified Stage 4 behavior):

- Opening Discover sends **no** network request; search is local-first.
- Online searching is explicitly user-initiated only; loading/offline/unavailable/source
  states are truthful and localized.
- Privacy, source attribution and licensing preserved; unresolved catalogue identities
  stay visibly unresolved ("Unnamed artist" pattern).
- No data-dependent UI that requires unsupported backend features (no moderation-only
  sections, no "our servers" claims — see §9 C1).

Stage 7 refines the existing `DiscoverScreen.kt` experience per the reference; it does
not rewrite the Stage 4 provider/search architecture.

## 5. Library — Your Collections (approved direction)

Rename/organize the current Library screen per `02-library.png`:

1. Header **"Your Collections"** (full label) with subtitle; search access.
2. Compact filter chips: **All · Works · Artists · Albums · Lists** ("Lists" is the
   compact filter label only).
3. Offline-library / download-management entry point (top-right download action),
   routing into the Stage 5 offline management UI — Downloaded / Updates / Settings
   **only where Stage 5 actually supports them**.
4. Compact, readable music rows/cards; consistent artwork/portrait thumbnails **only
   where legally and technically available**, with meaningful fallbacks.
5. Clear separation of the user's personal collections from global discovery (Discover).

Bottom-tab label: **Library / Knihovna** (restores the owner-approved 0.10.1 rename; the
current tab still reads Repertoire/Repertoár — verified in `RepertoireScreen.kt:59`,
`label_repertoire`).

Constraints:

- Preserve personal repertoire, practice sessions, notes, favorites, UUIDs, recovery data
  and the repertoire-density/sort preferences.
- Downloaded public catalogue resources are **not** the authoritative personal library;
  never conflate the two.
- Recordings never auto-create practice entries.
- Mockup-only sections ("Favorite artists" with work counts, "Saved lists" with piece
  counts, sample covers) render **only** from real data; empty/unsupported groups are
  omitted, never faked.

## 6. Artist Profile (approved direction)

Blend of (A) editorial presentation and (B) immediate access to musical content,
per `03-artist-profile.png`:

1. Back affordance (Android), prominent portrait/header **only** when a properly
   licensed image exists; graceful fallback otherwise.
2. Artist name + verified identity; roles (composer/pianist/performer/ensemble) and
   verified dates/period.
3. Short bio with **Read More**; never fabricate biography text — mockup prose is
   illustrative only (see §9 C2).
4. Essential Works (real, verified works only).
5. Albums & Recordings where actually available.
6. Sources & Credits with original source/revision/license provenance.
7. Navigation to related profiles; consistent back to Discover/Library.
8. Layout must adapt to composers, performers, ensembles; unresolved identities remain
   visibly unresolved.

## 7. Work / Composition Profile (approved direction)

Per `04-work-profile.png`:

1. Clear work title; verified composer attribution; catalogue designation (BWV/Op./K.)
   when available.
2. Prominent **Add to Repertoire** action — preserves UUIDs, prevents duplicates, keeps
   existing work/movement/arrangement hierarchy and typed identity.
3. Compact verified metadata row (period/genre/instrumentation/duration — only fields
   that exist).
4. Compact sections with progressive disclosure: **Overview · Structure & Movements ·
   Arrangements · Recordings · Sources & References**.
5. Movement/arrangement selection supported only where the existing data model supports
   it; recordings are never automatic practice entries.
6. No invented movements, durations, catalogue IDs, recordings or external links.
   "Listen"/external actions require verified destination identity; omit when unsupported.

## 8. Home and User Profile (approved 0.11 redesigns)

### Home — activity dashboard (not a profile screen)

1. Greeting with username at **top left**.
2. Daily musical quote in a **separate, visually distinct block top right**.
3. Main stats: **Today Practiced**, **Current Streak**.
4. Compact seven-day activity graph.
5. **Recently Practiced** — most recent one or two items.
6. All primary content visible without unnecessary scrolling (accessible scroll fallback
   on small screens / large fonts).

Removed: settings gear, Favorite Artists, Start Practice section/button, duplicate
profile identity, dashboard filler. Practice remains startable only from the Practice
tab. Keep the existing approved editorial language and the 0.10.1 "shift content
slightly upward" spacing intent.

### User Profile — musical identity (not another dashboard)

1. Larger avatar; larger, more prominent username; stronger header positioned slightly
   higher (respecting system insets).
2. **About Me** directly below the name, integrated in the avatar-header area — not a
   large separate card.
3. Music identity: **Favorite Artist / Composer · Favorite Piece · Favorite Era**
   (render only configured values).
4. **Prominent Badge Showcase**, maximum **five** displayed badges, add/select showcase
   badges directly from Profile; preserve badge dates, selections and original artwork.
5. Compact, centered, consistently spaced cards.

Removed: Practice History, "Your Musical Journey" subtitle, generic Customize buttons,
menu filler, Home duplication. The avatar **pencil edit overlay stays** (explicitly
allowed); Settings gear stays on Profile (0.10.1 approval). No underlying profile/badge
data changes.

## 9. Conflicts, gaps and resolutions (require owner visibility)

- **C1 — Discover mockup privacy copy is inaccurate — RESOLVED by owner 2026-10-20.**
  The mockup says "…send the query to our servers." Goldberg 0.11 has no query backend;
  explicit online search goes to third-party providers (Open Opus/MusicBrainz/Wikimedia).
  **Owner-approved wording (subject to implementation accuracy):**
  EN "Local search works offline. When you explicitly start an online search, your query
  is sent to external music-data providers."
  CZ "Místní vyhledávání funguje offline. Když výslovně spustíte online vyhledávání,
  dotaz se odešle externím poskytovatelům hudebních dat."
- **C2 — Illustrative content in mockups.** Biographies, work counts ("128 works"),
  piece counts, durations, saved lists, "Grove Music Online" credits and all
  portraits/artwork are illustrative. Never production text/data; real credits come from
  actual integrated sources. No mockup image is a licensed production asset.
- **C3 — Optional mockup affordances.** "See all" links, share/overflow buttons and
  "Listen" actions render only when a real, verified destination/feature exists.
- **C4 — Missing 0.11 Home/Profile mockups.** The four supplied PNGs cover Discover/
  Library/Artist/Work only. Home/Profile direction comes from the owner's Stage 7
  instruction + 0.10/0.10.1 approvals; if dedicated 0.11 mockups exist elsewhere they
  should be added to the visual-references folder.
- **C5 — Library tab rename regression.** 0.10.1 approved "Library / Knihovna"; current
  code shows Repertoire/Repertoár. Stage 7 restores the Library label while keeping all
  repertoire functionality and the "My Repertoire" filter concept.
- **C6 — Stage 7 vs Stage 5 boundary.** Offline-management screens only expose what
  Stage 5 actually ships (Downloaded/Updates/Settings where supported). No speculative
  bundle UI.

All conflicts are documented for owner review; none is silently "solved" by redesign.

## 10. Implementation acceptance criteria (documented now, executed only when authorized)

Full per-screen matrix: [UI-ACCEPTANCE-MATRIX.md](UI-ACCEPTANCE-MATRIX.md). Summary of
the mandatory gates when Stage 7 is separately authorized:

1. Implement Discover + Library per references; 2. Artist + Work layouts; 3. Home
redesign; 4. Profile redesign; 5. Preserve functional behavior, privacy and personal-data
integrity (UUIDs, sessions, notes, badges, recovery); 6. Build the APK; 7. Automated
tests + regression checks (`./gradlew clean assembleDebug testDebugUnitTest
assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1`, `scripts/check_*.py`,
plus new UI tests where meaningful); 8. Fresh emulator screenshots for all six screens
(explicit `emulator-NNNN` serials only); 9. Light + dark; 10. CZ + EN; 11. Normal +
enlarged fonts, small screens, navigation; 12. Direct comparison against references;
13. Explicit mismatch report; 14. Owner acceptance before any completion claim.

## 11. Current progress and status

- **Done (planning):** this document, the acceptance matrix and the checklist/decisions/
  handoff/visual-reference updates — applied 2026-10-20 on the isolated
  `docs/goldberg-0.11-stage-7-planning` branch (uncommitted; awaiting owner commit approval).
- **Not started:** every implementation task (7.1–7.7), every build/screenshot/verification
  task (7.8–7.11). No Stage 7 emulator evidence exists. Do not describe any screen as
  implemented from mockups.
- **Dependency:** Part A waits for Stage 5 tasks 5.10–5.13 to complete (another agent is
  actively working on Stage 5 — this planning task does not touch its tree).
- **Authorization:** implementation, commits, pushes and GitHub sync require explicit
  owner approval. This planning work lives on the isolated Git worktree
  `/tmp/opencode/goldberg-stage7-docs`, branch `docs/goldberg-0.11-stage-7-planning`
  (based on Stage 5 checkpoint `7e7c3ba`) — the Stage 5 working tree was never modified.

created by stokonn with love <3
