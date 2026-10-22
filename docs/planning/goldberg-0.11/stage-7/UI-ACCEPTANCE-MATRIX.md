# Goldberg 0.11 — Stage 7 UI acceptance matrix (PLANNING PROPOSAL)

Companion to [README.md](README.md). Every row is an acceptance check to be executed
**only when Stage 7 implementation is separately authorized**. Status column values:
**Approved** = owner-approved requirement · **Existing** = verified in current builds ·
**Planned** = unimplemented Stage 7 work · **Blocked** = gated on another stage.
No row is "Done"; no Stage 7 screenshot evidence exists.

## Discover

| # | Acceptance check | Status |
|---|---|---|
| D1 | Elegant header + prominent search field per `01-discover.png` | Planned |
| D2 | Opening Discover performs **no** network request (local-first) | Existing (Stage 4) — must be preserved |
| D3 | Online search explicit and user-initiated; loading/offline/unavailable/source states truthful and localized | Existing — preserve |
| D4 | Truthful privacy notice using owner-approved EN/CZ wording (no "our servers" claim — plan §9 C1) | Approved (wording) / Planned (implementation) |
| D5 | Browse categories render from real data; "See all" only with real paged destination | Planned |
| D6 | Recently Viewed section with Local/Online availability labels | Planned |
| D7 | "Available offline" section fed by Stage 4 local content + Stage 5 READY bundles | Planned (D7 = Blocked on Stage 5 verification for bundle part) |
| D8 | Navigation Artist/Work/Recording/Album details + back to Discover | Existing — preserve |
| D9 | Unresolved identities stay visibly unresolved; no fabricated catalogue content | Existing — preserve |
| D10 | No reference PNG/mockup artwork in APK | Mandatory build check |

## Library / Your Collections

| # | Acceptance check | Status |
|---|---|---|
| L1 | Header "Your Collections" + subtitle per `02-library.png` | Planned |
| L2 | Bottom tab reads **Library / Knihovna** (restore approved 0.10.1 label) | Planned (currently Repertoire/Repertoár — verified) |
| L3 | Filter chips All · Works · Artists · Albums · Lists, adaptive at narrow widths | Planned |
| L4 | Search access on Library | Existing — preserve |
| L5 | Offline-library entry point routes to Stage 5 management (Downloaded/Updates/Settings only where supported) | Blocked on Stage 5 |
| L6 | Compact rows/cards; thumbnails only with real licensed media + fallbacks | Planned |
| L7 | Personal collections clearly separated from global discovery | Planned |
| L8 | Repertoire/sessions/notes/favorites/UUIDs/recovery/density+sort prefs preserved | Existing — mandatory preservation |
| L9 | No mockup-only counts, covers, lists or recommendations | Mandatory |
| L10 | Recordings never auto-create practice entries | Existing — preserve |

## Artist Profile

| # | Acceptance check | Status |
|---|---|---|
| A1 | Portrait/header per `03-artist-profile.png` when licensed image exists; meaningful fallback otherwise | Existing parts (portrait pipeline) + Planned layout |
| A2 | Name + verified identity, roles, verified dates | Existing — preserve |
| A3 | Short bio + Read More; only real sourced text | Planned |
| A4 | Essential Works from real verified data | Planned |
| A5 | Albums & Recordings only where actually available | Planned |
| A6 | Sources & Credits with source/revision/license provenance | Existing — preserve |
| A7 | Layout adapts to composer/performer/ensemble; unresolved identity visible | Planned |
| A8 | Back navigation consistent with Discover/Library stacks | Existing — preserve |

## Work / Composition Profile

| # | Acceptance check | Status |
|---|---|---|
| W1 | Title/composer/catalogue ID per `04-work-profile.png` | Existing — preserve |
| W2 | Prominent Add to Repertoire; UUIDs preserved; duplicate prevention | Existing — preserve |
| W3 | Compact verified metadata row (only real fields) | Planned |
| W4 | Overview / Structure & Movements / Arrangements / Recordings / Sources sections with progressive disclosure | Planned |
| W5 | Movement/arrangement/complete-work selection matches existing data model | Existing — preserve |
| W6 | No invented movements, durations, catalogue IDs, recordings, links | Mandatory |
| W7 | External "Listen" actions only with verified destination identity (else omitted) | Planned / conditional |
| W8 | Recordings never become automatic practice entries | Existing — preserve |

## Home

| # | Acceptance check | Status |
|---|---|---|
| H1 | Activity dashboard, not a profile screen | Approved |
| H2 | Greeting + username top left | Approved (existing 0.10/0.10.1 layout — restyle only) |
| H3 | Daily quote in separate distinct block top right | Existing — preserve |
| H4 | Today Practiced + Current Streak stats | Existing — preserve |
| H5 | Compact seven-day activity graph | Existing — preserve |
| H6 | Recently Practiced: one or two most recent items | Existing — preserve (trim to 1–2) |
| H7 | Primary content visible without unnecessary scrolling; small-screen/large-font fallback | Approved |
| H8 | No settings gear, no Favorite Artists, no Start Practice, no duplicate profile info, no filler | Mandatory |
| H9 | Practice still startable only from Practice tab | Existing — preserve |

## User Profile

| # | Acceptance check | Status |
|---|---|---|
| P1 | Larger avatar + prominent username; stronger header slightly higher | Approved |
| P2 | About Me integrated directly below name in header area | Existing 0.10.1 approval — restyle per 0.11 |
| P3 | Favorite Artist/Composer, Favorite Piece, Favorite Era (configured values only) | Existing data — Planned presentation |
| P4 | Prominent Badge Showcase; max five displayed badges | Existing rule — Planned stronger presentation |
| P5 | Add/select showcase badges directly from Profile | Existing (0.10.1) — preserve |
| P6 | Pencil edit overlay on avatar kept; Settings gear kept on Profile | Existing — preserve |
| P7 | No Practice History, no "Your Musical Journey", no generic Customize, no Home duplication | Mandatory |
| P8 | Profile/badge/identity data, dates, selections unchanged | Mandatory preservation |

## Cross-screen gates (all six)

| # | Acceptance check | Status |
|---|---|---|
| X1 | One coherent editorial design system (Cormorant Garamond/Inter, tokens, spacing) | Approved |
| X2 | Ivory + Nocturne both inspected | **EXECUTED** (refinement pass) — all six screens in both themes; theme readback asserted per switch; Artist hero, Work art, Library separation, Discover states and Settings toggle visually inspected in both themes |
| X3 | CZ + EN both inspected; plurals/dates/numbers localized | **EXECUTED** (refinement pass) — all six screens in both languages; CZ bio-language fallback label verified; 12 new string pairs EN/CZ parity-checked |
| X4 | Normal + enlarged font (1.5×/2×), small screens, touch targets, content reflow | **EXECUTED** (refinement pass) — 1.5× (initial pass) + 2.0× EN full/CZ partial spot checks + 720×1560 narrow-display sweep; hero grows with text, no overlap observed |
| X5 | `assembleDebug`, JVM tests, `lintDebug`, `scripts/check_*.py` pass | **EXECUTED — green** (refinement pass: build SUCCESSFUL, JVM 174/0F/0E/0S, lint 0 errors / 124 warnings (+2 stylistic, documented), `assembleDebugAndroidTest` compiles, 5/5 check scripts PASS) |
| X6 | Fresh emulator screenshots for all six screens, explicit emulator serial | **EXECUTED** (refinement pass) — 48 screenshots incl. Settings toggle flow + 2× + narrow; `emulator-5554` + `ro.kernel.qemu=1` asserted |
| X7 | Screenshot-vs-reference comparison + explicit mismatch report | This document + `stage-7a/results/README.md` (refinement section) |
| X8 | Personal data intact after changes (seeded upgrade check) | **EXECUTED** (refinement pass) — `adb install -r` in-place upgrade over the seeded AVD; all 22 table counts + profile_settings row (`Musician\|System`) byte-identical before/after; online-catalogue preference defaults OFF |
| X9 | Owner acceptance recorded before any "Stage 7 complete" claim | **PENDING — required before any "Stage 7 complete" claim** |

created by stokonn with love <3
