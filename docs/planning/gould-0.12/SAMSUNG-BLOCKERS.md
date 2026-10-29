# Gould0.12 — owner-reported Samsung corrections

## Owner-reported Samsung Stable blockers — 2026-10-29

Confirmed by owner testing on Samsung; no agent physical-device verification.
These are mandatory0.12 Stable blockers within the existing single Phase2 stage.
The accidental pasted terminal transcript introduced no instructions. Widget
checkpoint05a92c8 is pushed; Issue41 comment6503995190, canonical PMP snapshot51
61258e08-3801-47a8-b3df-77be56839ab3 (check valid/no errors/warnings), Project In Progress.

- [x] Metronome: replace system-sonification usage with media-volume playback;
  preserve focus, explicit start/stop, route/lifecycle handling. Verify system
  sounds muted/media audible routing, stop and focus loss with targeted native
  evidence. Do not equate emulator audio routing with Samsung acoustic testing.
- [x] Tux: inspect actual before rendering, whitespace and font metrics; isolate
  from OEM font substitution/variable advances. Preserve original ASCII/pink
  character and exact signature `created by stokonn with love <3`. Verify actual
  normal/large-font emulator renders and glyph advance/line alignment. Existing
  system Monospace styling is owner-confirmed insufficient on Samsung.
- [x] Library: align Previous horizontally with main heading Vaše sbírky; keep
  Vaše hudba below as subtitle. Verify EN/CZ, normal/large fonts and constrained
  widths, touch target, navigation and no clipping/overlap.
- [ ] Synchronize actual results/source/evidence with Issue41/PMP/Project/handoff;
  retain remaining mandatory gates. Stable remains blocked until all pass.

created by stokonn with love <3

## Implementation checkpoint (not yet accepted)

Base05a92c8; uncommitted corrections preserved. Metronome now uses shared
USAGE_MEDIA/CONTENT_TYPE_MUSIC attributes for both focus and AudioTrack. Existing
mutex/generation/start/stop/noisy/lifecycle behavior retained. Native test also
checks STREAM_MUSIC and explicit competing-focus loss after stop/restart.

LibraryHeader groups main heading/action in one row with full-width subtitle
below; at constrained large-font widths the same accessible Previous action uses
its48dp history icon. No heading/subtitle wording or unrelated Library state changed.

Tux selects bundled original AOSP DroidSansMono instead of generic OEM monospace,
explicit normal weight/style, LTR, no ligatures/contextual shaping/kerning, unchanged
12sp/15sp and pink theme. Original ASCII literal byte-for-byte equal to05a92c8,
SHA256f3c892437710d700240168e8266011527c8a87c6ad89f20362a92b815aa46f3f;
exact signature unchanged. Both original source and current emulator font have
1229-unit advances on2048-unit em; bundled hhea ascent1901/descent-483, emulator
1900/-500. Original complete Apache notice bundled, official tree README states
font licensing. No dependency introduced; no device-font global setting changed.

Debug/test build PASS1m2s; final test-fixture compilation PASS29s. Debug APK
614521a6e18c2361548dbbf83e0b62ec6f320a4b4a3cf1200ae9867de53a5db7,
trusted certificate53eface4… verified. Actual before Tux screenshot inspected on
emulator5582. Some earlier About captures hit the decorative Settings heading and
are not Tux evidence. Native/renders/lint/acceptance remain pending.

created by stokonn with love <3

## Targeted native follow-up

Bundled-font metrics PASS1/1: every original ASCII/signature character has equal
advance and fits12sp/15sp line metrics at1/1.5/2. Metronome test first failed JUnit
initialization because a new coroutine assertion inferred a non-void return; test
signature explicitly Unit, rebuilt26s, corrected native routing/focus test PASS1/1
0.112s. Media usage/content and STREAM_MUSIC mapping, explicit stop/restart and
competing-client focus loss are verified. System sound_effects_enabled=0 was
verified; the shell system-volume-zero command did NOT persist (STREAM_SYSTEM
remained5), so no system-volume-zero or acoustic-audibility PASS is claimed.
Actual foreground UI/audio diagnostics remain pending. Raw failed log retained.

Responsive fixture first EN320 scene passed geometry/touch/action. Next cs360
scene rendered localized heading/subtitle but retained the previous English button
content lambda: direct L.refresh in the isolated test is not a real application-
locale recreation. Diagnostic screenshot/tree/log preserved. Test now keys each
locale/width/font case to recreate its subtree, matching the real locale lifecycle;
no production locale behavior was weakened or falsely accepted. Matrix rerun pending.

## Render audit correction

Corrected keyed Library matrix PASS1/1,16.622s: six cases spanning320/360/412dp,
EN/CZ, Light/Dark and font1/1.5/2; heading/action same row, subtitle below,
48dp action and actual click callback assertions pass. Actual app Library→Previous
navigation also verified. Foreground actual metronome UI at sound effects0 has an
active unmuted AudioFlinger track, ST3/usage1/content2, advancing frame count and
media focus; explicit Stop leaves focus stack empty. Original sound effects1 restored.

The same native matrix captured four Tux scenes, but screenshot inspection found
that Dialog did not inherit the fixture-only LocalDensity override: files named
`tux-2.0-*` show actual font1 and MUST NOT count as200% acceptance. Their assertions
only prove visible original ASCII/signature under normal dialog metrics. Actual
Android system font2/About route capture is now in progress; do not claim complete
Tux font acceptance from mislabeled fixture scenes. Raw files retained.

A frozen381-input full host gate is running in final-host.log (session73245).
No application/test source changes while it runs; do not duplicate Gradle.

## Verified targeted acceptance — 2026-10-29

- [x] Metronome media-stream/focus correction: native1/1 PASS; actual foreground
  UI at sound effects0 has unmuted ST3/Usg1/CT2 track, advancing frames and media
  focus; Stop releases focus; no automatic practice or recording.
- [x] Deterministic Tux font, unchanged ASCII/signature, native metrics1/1 PASS.
  Actual Android font2/About capture inspected; signature horizontally pans to
  love <3, Close visible. Corrected real-platform-scale probe1/1 PASS9.09s, both
  themes inspected. Earlier fixture-only font2 filenames remain invalid evidence.
- [x] Library heading/action same-row and subtitle-below correction; six-case
  responsive geometry/touch/action matrix1/1 PASS16.622s; actual app Previous route
  verified. Normal CZ360 capture shows Vaše sbírky/Předchozí side by side with
  Vaše hudba below; large/constrained variants retain a48dp accessible history action.
- [x] Full frozen381-input host gate PASS3m25s:393JVM/48suites/0failures/errors/skips,
  debug/testAPK and lint0errors/165warnings/1hint. Production/JVM inputs unchanged
  afterward; only the honest Dialog-scale probe refinement compiled28s and passed.
- [x] ALL28 database tables exact against initial baseline; all3 WAV hashes exact;
  integrity/FK PASS, original widgets2/3 and added5 retained. System font1 and sound
  effects1 restored; system/media volumes5 unchanged. Emulator remains offline.
- [ ] Commit/push and exact-source CI/native/upgrade/compatibility follow-up.

These are actual emulator/host results. No agent used or verified the Samsung;
owner's original physical findings remain attributed to the owner. No release
readiness is inferred; all other mandatory0.12 gates remain open. No schema,
application/storage/signing identity, protected data or independent PMP changed.

See verification/samsung-blockers.json for source/artifact/log binding.

created by stokonn with love <3
