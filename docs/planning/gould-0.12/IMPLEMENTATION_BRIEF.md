# Gould 0.12 — Owner-approved direction & implementation handoff

**Project:** https://github.com/stokonn/Gould  
**Status:** Planning/design handoff, not an implementation receipt.  
**Development model:** One complete development stage with internal checkpoints. No release, stable tag, main merge, or publication without separately confirmed owner approval.  
**Canonical checkout (on owner's PC):** `~/Projects/Opus`; inspect actual Git branch, status, remotes, AGENTS.md, docs/ai/STATE.md, docs/ai/HANDOFF.md, docs/ai/GOULD-MAIN-MAINTENANCE.md and the security review before changes. Do not discard or disturb protected private worktrees/PMP.  
**Compatibility:** Keep `com.opus.piano`, `opus.db`, all existing data, schema migration/backups, trusted signing identity, historical tags/releases and in-place update support.

## Product direction (approved or accepted as concept)

- Gould remains a native Kotlin / Jetpack Compose / Material 3 offline-first practice journal. Design evolves Goldberg 0.10.1–0.11 editorial visuals, Cormorant Garamond / Inter, semantic tokens and user-customizable colors; **do not rebuild as a new app**.
- Keep five bottom destinations **Home · Library · Discover · Studio (renamed Practice) · Profile**. Settings stays under Profile. No sixth tab, no Folders tab, no fabricated statistics/assets/data.
- Design targets Ivory / Nocturne themes, Czech / English, dynamic font sizes, reduced motion and accessible high-contrast/touch-target fallbacks. Keep the approved original UI structure rather than blindly recreating illustrative AI mockups.

## Studio — current practice only

- Preserve the existing practice timer and its reliable serialized `PracticeCoordinator` / `PracticeEngine`; do not introduce competing timers.
- **Smart Practice 2.0:** Main timer displays *credited actual playing time* from microphone detection when Smart mode is on, not uninterrupted wall-clock duration. Show clear listening / playing / paused / interruption states. Retain wall-clock duration as secondary metadata where useful. Maintain manual timing fallback; investigate user-reported behavior as a regression, do not claim the detector is perfect. Preserve natural short musical gaps and accurate accounting; test synthetic noise, quiet notes, long silence, stall, permissions and route changes.
- Compact Smart Metronome and Smart Practice controls; session queue for multiple pieces, manual switching, Undo as planned, and compact piece/session notes. Preserve existing recovery and per-piece credits.
- **Practice Recording — selected option A (supersedes option B):** Optional, user-initiated **continuous audio from Record until Stop**, including pauses and reverb. Smart Practice can concurrently detect activity and credit only detected playing. Recording must not automatically begin merely by opening Studio or enabling Smart Practice; reuse one microphone capture pipeline where feasible. Provide reliable stop/save/error handling, no duplicate mic owners and no automatic microphone restart after recovery. Local-only by default; explicitly user-initiated export/share; consider storage limits, retention/deletion and privacy disclosures. Do not confuse stored recording duration with credited practice duration.
- Rounded/history-arrow control at upper-right of Studio opens Library → Previous; never crowd historic lists into Studio.

## Android home-screen widgets — REQUIRED for 0.12

This was present in the owner's original Gould 0.12 plan but was mistakenly omitted from the first handoff ZIP. The binding requirements are in **`docs/planning/gould-0.12/WIDGETS_SPEC.md`**. Implement **Quick Practice (A)** and **Weekly Activity (C)**, with **Intelligent Open (B)**, **Adaptive Sizes (B)**, stale/last-known-data error behavior **(B)**, and Monday–Sunday week **(A)**. The widgets live on the Android launcher, not a sixth in-app tab. Widget taps never auto-start microphone capture, audio recording or practice timers. Offline-first, actual persisted credited values only, CZ/EN, adaptable light/dark and accents, appropriate Education application classification where supported. Preserve notifications, timer/recovery safety, battery and history consistency. Validate host/emulator home screen widget placement, resizing, navigation, state updates, timezone change and stale fallback. Do not treat sample artwork as APK assets.

## Library / Previous

- **Preserve current Library** as in current `RepertoireScreen.kt`: existing search + add, **All / My Repertoire / Artists**, Sort / Filters and catalogue/personal collection distinctions. Do not add a Folders tab, new fake categories or copy an image mockup literally.
- Add a discreet entry into **Previous**, a dedicated Library-associated screen containing **Practice History** and **Recordings** tabs; same destination available from Studio history icon.
- Practice History lists saved sessions with date, composition(s), correctly credited time and existing review/edit affordances; recordings show actually saved audio with playback and contextual piece/session association. Multiple items/pieces per session must remain accurate; recordings do not create practice records themselves.
- Recording detail/player should allow basic playback and appropriate explicit user-controlled share/delete. Preserve existing unrelated *linked external recordings* data; never treat a URL bookmark as a local captured audio file.

## Profile / editor

- Keep existing prominent avatar, name, bio, four summary metrics, favorite composer, badge showcase (max five), settings entry and administration role gate; no duplicated Home history.
- Extend **Favorite Era autocomplete** alongside existing Favorite Composer autocomplete, using actual supported eras / local data, not invented labels. Avoid data loss: save successfully persisted changes, or show Save / Discard / Keep editing when exiting with pending edits; no false Saved state.
- Make **all four statistic cards** more compact and *actually horizontally and vertically centered* (numbers and labels), with a consistent 2×2 grid at normal sizes and accessible reflow at large font scaling.

## Achievements

- Current registry holds 17 achievements. Preserve existing IDs, unlock dates, showcase slots and original engraved artwork.
- All achievement collection cards must be equal-size within rows and consistent in layout/medal/title/rarity/date placement, with adaptive sizing/columns; no clipping at Czech long labels or large fonts.
- Proposed new awards requiring owner-facing name/threshold validation while implementing: First Take (first saved recording), Recording Artist (10 saved recordings), Versatile Pianist (3 distinct pieces in one session), Musical Journal (10 saved practice notes), Critical Listener (played back a recorded performance). Record events durably and accurately; avoid trivial click-only unlocks.
- **Founding Pianist**: one-time exclusive Legacy / Legendary badge for qualifying people who completed valid practice before the actual stable Gould 1.0 cutoff. Include pre-Gould Goldberg owners with preserved genuine practice history, and qualifying 0.12–0.99 users. Never strip earned badges; migrate/restore historical earned unlocks. After 1.0, historical qualifying data can support a delayed award, but a newly created post-1.0 account/history cannot qualify. Offline clock/history is not tamper-proof; explicitly do not claim it is.
- Replace unobtrusive one-off snackbar with a **tasteful achievement-unlocked popup** showing original medal, title/rarity, View badge / Dismiss, and respectful motion/reduced-motion behavior. Queue multiple awards, do not repeatedly show already-seen unlocks, and defer intrusive display until active playing is over. Save notification-acknowledged state reliably.

## Final two owner requests — required

### Fix the Tux Easter egg (not a redesign)

- Owner screenshot shows broken ASCII alignment in the pink terminal popup. Inspect `app/src/main/java/com/opus/piano/ui/TuxScreen.kt` and the PRIVATE locally retained approved reference `docs/design/goldberg-0.10.1/references/07_TUX_ORIGINAL_APPEARANCE_PRESERVE.png` where present; existing verified historic reference: `docs/wiki/images/goldberg-0.10.1-tux.png`. Never publish the private owner reference.
- Preserve the original stylized ASCII Tux, pink/purple faux-kernel-panic styling, original easter-egg long-press on About logo, existing wording/accessibility, close action and exact signature **`created by stokonn with love <3`**. Change only sizing/monospaced glyph rendering/spacing/alignment and responsive layout. Do NOT replace it with new artwork or colorful modern UI.
- Inspect `trimIndent()` and styling interactions. Render ASCII with truly fixed-width glyphs / explicit `TextStyle` and stable line metrics; preserve interior whitespace/newlines and avoid automatic wrap, clipping and broken placement. Maintain dialog size compatibility with narrow phones, both themes, Czech/English and Android 1.5×/2× text scaling. Validate against before/after renders; Samsung-specific font handling must be checked using owner-supplied observations. Never claim a physical device was agent-tested.

### Nature backdrop on Home daily quotation

- Owner likes the subtle natural landscape from the *prior in-chat Gould UI visual concept*. Add this **only inside the existing separated daily quote card at upper-right of Home**, preserving Home greeting/stats/weekly activity/recent items and no Start Practice or Settings gear on Home.
- Visual direction: calm nature (misty hills/forest/mountains), restrained premium editorial effect; readable text over a subdued scrim/gradient, preserve quote attribution, adaptive height, text scaling, accessibility contrast and light/dark themes. No flashy image or full-screen Home redesign.
- Use an actually authorized original/licensed and locally available asset with appropriate attribution/licensing and reasonable size/decoding. **Do not embed an entire illustrative AI mockup as an app image** or require network access to view Home. Provide fallback solid card for reduced contrast/resources, and ensure chosen accent is respected.

## Engineering & release safety

- First establish current owner-approved security/maintenance baseline: `maintenance/admin-safety-review` was independently reviewed but historical private custody and main-merge approval were open at last known handoff. Recheck live state; do not merge/rewrite history or delete unresolved data without authorization. New 0.12 feature work should use a properly scoped branch/worktree after preflight.
- One **complete** development stage for approved 0.12 features, with internal commits/checkpoints, repeatable commands, documentation and PMP updates. Do not infer main-merge, release or publication authority from passing CI.
- All new persistent recordings / badge unlocks / editor changes require additive tested migrations or compatible formats as needed; verify seeded upgrades 0.11→0.12, backup/restore compatibility, notes, session/accounting, offline and profile persistence.
- CI / host: Android build, JVM tests, lint, instrumentation compilation; explicitly targeted emulator UI tests for Ivory/Nocturne, CZ/EN, small screens/font scaling, transition/permission/recovery/storage. No agent operation on owner's physical phone.
- Handoff must report changed files, tests actually executed, CI exact commit, emulator screenshot comparisons, failure limitations, APK type/signing/hash and remaining open decisions. Development APKs only until owner separately approves main integration and a stable release.

## Owner review checkpoint

This brief captures design decisions and proposed acceptance criteria; it is **not proof of implementation**. Before shipping, get owner visual sign-off for Studio, Library/Previous, Profile/Badges, Tux and nature-quote Home. If an illustrative mockup conflicts with actual approved UI/data or this explicit text, prefer this brief and current owner direction.

created by stokonn with love <3
