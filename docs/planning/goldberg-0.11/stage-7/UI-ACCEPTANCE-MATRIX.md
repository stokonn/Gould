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

Current owner decision (2026-10-23): Stage 7A COMPLETE by final acceptance with a
visual deviation; X9 ACCEPTED. X10 remains unfixed and unverified, deferred to
Gould 0.12. Existing executed results below are historical evidence, not reruns.
Stage 8 retains material readability/accessibility/touch-target checks.


| # | Acceptance check | Status |
|---|---|---|
| X1 | One coherent editorial design system (Cormorant Garamond/Inter, tokens, spacing) | Approved |
| X2 | Ivory + Nocturne both inspected | **EXECUTED** (refinement pass + round 3) — all six screens in both themes; theme readback asserted per switch; Artist hero, Work art, Library separation, Discover states and Settings toggle visually inspected in both themes; round 3 re-inspected all six screens in both themes after the shared typography change (incl. the Discover header lock icon and popover in both themes) |
| X3 | CZ + EN both inspected; plurals/dates/numbers localized | **EXECUTED** (refinement pass + round 3) — all six screens in both languages; CZ bio-language fallback label verified; 12 new string pairs EN/CZ parity-checked in the refinement pass; round 3 added the `stage7a_privacy_info` EN/CZ pair (localization check PASS, parity 951) and re-inspected all six screens in both languages |
| X4 | Normal + enlarged font (1.5×/2×), small screens, touch targets, content reflow | **EXECUTED** (refinement pass + round 2 + round 3) — 1.5× + 2.0× (EN full/CZ) + 720×1560 narrow-display sweep; round 2 re-verified the affected surfaces at all three scales: Artist tabs never wrap mid-word (scrollable row at 2×/narrow), Work metadata cells stack adaptively, hero grows with text, no overlap observed; round 3 re-swept all scales after the `includeFontPadding=false` change — Settings rows, Profile cards, Discover rows and the Customise previews centered with no clipping at 2×, and the privacy popover open/dismiss cycle verified at 1×/1.5×/2×/narrow |
| X5 | `assembleDebug`, JVM tests, `lintDebug`, `scripts/check_*.py` pass | **EXECUTED — green** (round 3, latest: build SUCCESSFUL, JVM 174/0F/0E/0S, lint 0 errors / 124 warnings (unchanged — no new warnings from `includeFontPadding`), `assembleDebugAndroidTest` compiles, 5/5 check scripts PASS, EN/CZ parity 951 intact; final `assembleDebug` re-ran after a comment-only edit) |
| X6 | Fresh emulator screenshots for all six screens, explicit emulator serial | **EXECUTED** (refinement pass 48 + round 2: 47 `r2-*` + round 3: **51 targeted `r3-*` captures** — 4 theme×language combos × all six screens at 1×, Data & Storage EN/CS, 1.5×/2× EN Ivory, 2× CS Nocturne, 720×1560 narrow, targeted Appearance & Customize previews; privacy popover cycle ×8 UIAutomator-asserted — at 1×/1.5×/2×/narrow, SHA-256 manifest; `emulator-5554` + `ro.kernel.qemu=1` asserted) |
| X7 | Screenshot-vs-reference comparison + explicit mismatch report | This document + `stage-7a/results/README.md` (refinement + round-2 + round-3 sections, incl. honest limitations: bottom-nav visual ellipsis at 2× carried by verified full-name semantics; edge-cut third tab in the 2× scrollable tab row; round-3 capture-automation incidents documented) |
| X8 | Personal data intact after changes (seeded upgrade check) | **EXECUTED** (refinement pass + round 2 + round 3 re-run) — `adb install -r` in-place upgrade over the seeded AVD; all table counts + profile_settings row (`Musician\|System`) byte-identical before/after; online-catalogue preference defaults OFF (read back `true`/`false` on-device, restored) |
| X9 | Owner acceptance recorded before any "Stage 7 complete" claim | **ACCEPTED — final owner acceptance, 2026-10-23. Stage 7A COMPLETE with documented visual deviation.** X10 is deferred to Gould 0.12, not passed. Stage 7 overall and release authorization remain separate. Earlier conditional decision is preserved in CHECKPOINT.md §7A.17; final decision §7A.18 supersedes it. |
| X10 | Samsung text alignment follow-up | **DEFERRED TO GOULD 0.12 — NOT FIXED, NOT VERIFIED, NOT PASS** (owner, 2026-10-23). Owner-reported inconsistent Samsung alignment persists after round 3; emulator evidence does not close it. Minor cosmetic alignment no longer blocks Stage 7A/0.11 by itself. Stage 8 must independently verify material readability, accessibility and touch targets; those defects are not waived. [Original observations, hypothesis and future verification](../../gould-0.12/X10-SAMSUNG-TEXT-ALIGNMENT.md). |

created by stokonn with love <3
