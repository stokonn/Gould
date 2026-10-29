# Gould 0.12 — current-source launcher acceptance

Source: `feature/gould-0.12` at `50e32edf18cceb9b6d0918a7a683f420199424a2`.
This is a continuation of the single Phase 2 stage, not a new stage. Acceptance
is IN PROGRESS; Stable remains blocked by all outstanding mandatory gates.

## Checklist and executed evidence

- [x] Inspect clean Git state, HEAD, remote, latest recovery/handoff, Issue #41 and
  canonical PMP snapshot 50 (`fe4525d4-4ba1-4c2a-98ad-3f03160af2b0`). Repository
  API identity is `stokonn/Gould`, ID 1421247418; milestone 5 remains open.
- [x] Required CI for exact HEAD: quality `40916591492`, BFF `40916591566`,
  maintenance `40916591543`, all successful. No redundant dispatch.
- [x] Identify `gould012-profile`, `emulator-5582`, API36, `ro.kernel.qemu=1`.
  No other emulator or physical device used in this batch.
- [x] Read-only private baseline of app files/database/preferences and recovery
  store; three Saved recordings, database integrity `ok`, zero completed sessions.
  Preserve the interrupted practice, original widget IDs 2/3 and recordings.
- [x] Installed APK SHA-256 equals existing built/verified debug APK:
  `30b281eb39074169d80d0520cfc7c6df4e029c78518f2b1ab540f09d55e6f618`.
  All five widget production Kotlin files, both layouts and WidgetProjectionTest
  exactly match the prior verified host source manifest. New screenshots therefore
  cover current layouts (historical pre-09b1fe4 screenshots are not substituted).
- [x] Reuse existing 13/13 WidgetProjectionTest results from 2026-10-29 14:15:24Z:
  Mon–Sun, calendar/year boundary, both DST transitions, midnight scheduling,
  recorded practice-day/timezone rules, credited totals, stale/future/zone/week
  handling, unavailable/corrupt/overflow and recovery context. No fresh run claimed.
- [x] Retain pristine emulator-5590 WidgetTest 8/8 evidence; no rerun on the
  protected profile fixture and no weakening its load-bearing test preconditions.
- [x] Actual Weekly resize 4×2 → 2×2 and Quick resize 2×1 → 1×1 through observed
  Pixel Launcher handles. Both original IDs remain bound; live layouts inspected.
- [x] EN/light font 1.0/1.5/2.0 captures inspected. Weekly 4×2 retains all seven
  days at 2.0 after refresh; 2×2 simplifies at 2.0 and retains total/week/status.
  Quick 1×1 is icon-only; 2×1 simplifies to Studio at 2.0.
- [x] Actual Quick and Weekly taps open Studio showing the original Interrupted
  practice, 00:00:07 and explicit Resume, without automatic resume or microphone.
- [x] Add a second live Quick instance through real launcher Widgets → Gould →
  Quick Practice → Add. Original widgets retained; no synthetic host substituted.
- [x] Disable Wi-Fi/mobile data, background app process death (`am kill`, not
  force-stop/data clear), inspect cached launcher and cold safe widget navigation.
- [x] Actual app Appearance → Dark updates all three widgets with readable dark
  surfaces/ink, preserving context, total and verified status.
- [x] CZ locale selected through real Settings; dark normal/200% captures inspected.
- [x] Remaining accent refresh: actualGold→Rose→Gold through Appearance; persisted snapshot tracks both changes/new generation; settled launcher captures inspected and originalGold restored. All required larger-size combinations are recorded below. Zero-activity profile does not visually exercise nonzero accent-colored bars.
- [x] Actual added Quick 2×2 and 4×1 layouts; 4×1 normal/200% Czech dark captures inspected. Original Quick restored2×1 and Weekly4×2; added ID5 retained on adjacent page.
- [x] Opt-in actual-host unavailable/stale/accessibility probe passes on all3 then-visible instances: honest localized descriptions, actionable roots, minimum48dp bounds, unchanged persisted cache.
- [x] Added4×1 instance independently opens the same Interrupted00:00:07 practice with Resume.
- [x] Settled offline reboot after Gradle completion: no ANR since boot, live original widgets, Weekly tap opens same Interrupted00:00:07/Resume.
- [x] Offline reboot/post-reboot recovery/tap and controlled real-host stale/unavailable display; date/timezone logic reuses unchanged host tests.
- [x] Final ALL28 database tables exact,3 WAV hashes exact, integrity/FK PASS; original widget IDs retained, system locale/theme/font1 restored.
- [x] Source-bound evidence manifest, Issue dashboard/history, ProjectInProgress and canonicalPMP/checkpoint synchronization at05a92c8/f62e41c, retained history.
- [ ] Remaining finite full UI matrix, security/privacy/licensing/performance and
  final clean trusted non-debuggable Stable artifact/in-place upgrade/release gates.

Private raw evidence is under `.gradle/gould012-checks/widget-current/`:
`source.json`, `commands.log`, private baseline tar files/installed APK, XML trees,
PNG screenshots and appwidget dumps. Do not commit private fixture contents.
Screenshots are actual emulator evidence only, not physical-device evidence.

## Observations and limitations

Font changes first scale the old host RemoteViews before the app's queued refresh.
The intermediate 200% chart is compressed; the settled refreshed 4×2 chart has clear
seven-day labels and pinned timestamp. Both intermediate and settled captures are
retained. No permanent rendering defect is claimed from that transient capture.
At narrow 2×2/200%, week/status text ellipsizes; full dates/day values and safe
navigation remain in the widget root's accessibility description. Further checks
remain open; no complete widget acceptance is inferred.

The first language-navigation automation failed to obtain a hierarchy, and a
premature dependent command found no Czech selector. Files named `dark-cs-*` from
that attempt show English and are diagnostic only, never CZ acceptance. Subsequent
language QA must use confirmed UI state. Existing failed automation logs remain.

created by stokonn with love <3

## Bounded defect found during actual acceptance

The controlled actual-launcher stale/unavailable probe passed its semantic/touch/
cache checks (1/1), but screenshot inspection revealed the compact Quick `!` was
vertically clipped at 200% font because its text box was fixed at 24dp. This is a
real defect, not accepted as cosmetic. Changed only the marker's wrapping dimensions,
minimum dimensions and inset in `widget_quick_practice.xml`; added a focused native
layout regression for both direct 200% and retained normal→200% RemoteViews,
stale and unavailable states. Debug/test compilation PASS37s; trusted signer
53eface4… retained; candidate SHA256
`18d1062adf031283b7c7e4487efd4278a3a7618bb8af4feba1e53c8761d85d70`.
New layout regression PASS1/1 (0.094s); real-launcher fixed-state probe PASS1/1 (3.816s). Fixed 200% stale PNG inspected: complete exclamation marks, pinned Weekly last-verified label and full localized descriptions. Original failing screenshots remain private diagnostics.

Offline reboot retained all three widgets, but early MENU input hit a real Pixel
Launcher ANR. Preserve `launcher-anr.txt`, `post-reboot-logcat.txt`, screenshot and
XML: memory pressure some15.84%, CPU pressure some76.72%, I/O pressure some41.70%,
95% total CPU and launcher RenderThread kernel time are observed, not a proven
Gould defect or a clean reboot pass. Choosing Wait recovered all three live widgets.
A settled repeat without a simultaneous build remains required.

## Protected data readback after in-place update

`protected-mid.tar` compared against the original private baseline: all three WAV
SHA-256 hashes identical, exact practice_drafts(1), local_audio(3), sessions(0),
session_parts(0), badge_unlocks(1), saved_recordings(0) rows identical; integrity_check
`ok`. Widget IDs2/3 remain and added Quick ID5 is bound independently. No clear-data,
uninstall, practice reset, clock adjustment or data reseeding occurred.

Lint is running; low host available memory (~365MiB of8GiB) is recorded. Do not
repeat reboot under simultaneous Gradle load or terminate unrelated owner processes.

## Latest bounded verification

Lint PASS4m25s,0errors/165warnings/1hint. Localization1211, policy, Git diff
whitespace and trusted APK quality gates PASS. Installed debug APK remains
18d1062a…; no release artifact. Both original widgets are back at2×1 and4×2;
added independent Quick ID5 remains on adjacent page at4×1. Normal font,
system-following language and theme restored through real Settings. Final exact
preference/data readback is pending. The unrelated historical failed runs remain.

created by stokonn with love <3

## Final reboot and exact readback

Corrected repeat procedure waits for boot and launcher rendering with no Gradle
load or early MENU injection. Actual live normal/EN/system-theme launcher inspected;
no ANR since this boot. Weekly tap opens the same Interrupted00:00:07 practice,
with Resume available and no automatic capture. Earlier ANR is preserved history.

Final comparison against the initial baseline: **all28 database tables have exact
rows**, all3 WAV SHA-256 hashes match, SQLite integrity_check=ok and FK check empty.
System-following app locales=[] and theme=System match baseline; system font=1.0.
The emulator remains offline intentionally. No clear-data/uninstall/reset occurred.
Added Quick ID5 remains4×1 on the adjacent page; original IDs2/3 retain2×1/4×2.

Residual widget work is limited to explicit unverified spec cells (including user
accent refresh), not repeating8/8 pristine or13 calendar tests. The opt-in launcher
probe requires all registered widgets visible on its current page, as they were
for the recorded passing run; current adjacent-page placement does not meet that
probe precondition. Do not misclassify a fixture mismatch as an application result.

created by stokonn with love <3

## Recovered final accent/readback evidence

Production source02c5298/f62e41c, debugAPK614521a6…. Actual Gold→Rose→Gold
selection and live snapshot refresh verified (generated1793301370951 →
1793301447405 →1793301487200). Settled widget-accent-rose and
widget-accent-gold-restored actual launcher PNGs retained; Rose PNG inspected.
Full private readback after UI: ALL28 tables and3WAV hashes exact vs original
protected-before baseline; integrityok/FKempty. Font1/window default restored;
subsequent interrupted DataStorage locale venue is Czech with system-following
theme, originalGold; restore system-following language when that visual check ends.
Evidence: .gradle/gould012-checks/samsung-blockers/accent-*-full.tar,
profile-after-final-ui.tar, profile-final-ui-integrity.json. No practice seeded
for colors, no nonzero-Rose-bar visualPASS. New metronome correction does not
change these widget/DataStorage production inputs.
