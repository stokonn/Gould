# Gould 0.12 — single development stage

Status: **Phase 2 IN PROGRESS — owner-approved complete scope plus mandatory icon.**
Exactly two main phases; one complete implementation stage. The owner-supplied
scope is now available; the earlier maintenance placeholder is superseded.

## Phase 1 — Architecture, Review & Planning

- [x] Inspect policies, continuity, PMP, Git/GitHub and maintenance prerequisites.
- [x] Analyze complete v2 written specification and inspect all 14 PNGs/hash manifest.
- [x] Review existing practice/audio/data/backup/navigation/Profile/badge/Home/Tux source and historical approvals.
- [x] Prepare one complete [architectural review and plan](ARCHITECTURE-AND-PLAN.md), dependencies, risks and acceptance gates.
- [x] Verify all mandatory handoffs/specs/private visual references in the isolated worktree; protect PNGs from public commits.
- [x] Obtain explicit owner approval for Phase2 and proposed product-rule details; see OWNER-AUTHORIZATION.md.

## Stage 1 — Complete approved Gould 0.12 scope (Phase 2)

Purpose: deliver the complete specified Studio/audio/Library/Profile/achievement/
widget/Tux/Home scope together, preserving user data and the approved identity.
Scope: existing Kotlin/Compose UI/navigation/repositories; shared capture/service;
new local audio/events/launcher widget components, additive personal schema/backup
extensions, EN/CZ resources, tests and documentation. Catalogue identity and
production backend permissions remain unchanged. Exact files and schema are
recorded as implementation proceeds. Internal batches are not additional stages.

- [x] Approve technical/product details and establish feature/gould-0.12 from29bb4d1 in the existing isolated worktree.
- [x] Read icon addendum, inspect reference15, copy both into development worktree and verify SHA-256/private exclusion.
- [ ] Faithful three-ivory/two-black/gold launcher icon: adaptive layers, monochrome, legacy densities; actual masks/sizes/emulator/upgrade acceptance.
- [ ] Add tested additive persistence, compatible old-backup import and new journal/audio backup/restore contracts.
- [ ] Studio rename, credited Smart timing characterization/corrections, truthful detection/interruption states and manual fallback.
- [ ] One capture pipeline; explicit continuous Record→Stop, safe finalization/recovery/export/delete, separate media duration.
- [ ] Compact metronome, multi-piece queue/manual switch/safe Undo and notes; accent/neutral Stop/history shortcut.
- [ ] Preserve Library controls; Previous with Practice History/Recordings, actual local playback and retained URL bookmarks.
- [ ] Profile centered 2×2 statistics/adaptive reflow, local era autocomplete and persistence-backed dirty-exit guard.
- [ ] Preserve old17 badges/dates/art/showcase; equal-row adaptive cards; five genuine new awards and Founding Pianist.
- [ ] Durable queued deferred unlock popup with acknowledged-event recovery and reduced motion.
- [ ] Quick Practice A with Intelligent Open B, Adaptive B and safe zero-auto-start navigation.
- [ ] Weekly Activity C, Monday–Sunday A, true persisted credits, adaptive/stale B/offline snapshots and bounded refresh.
- [ ] Repair Tux ASCII rendering only; preserve signature/access/style and record Samsung verification limits.
- [ ] Licensed/original local landscape inside existing Home quote card only, contrast and solid fallback.
- [ ] Run host/JVM/lint/debug/instrumentation compilation and actual emulator/launcher/UI/data/upgrade/backup gates in the plan.
- [ ] Synchronize living issue description/checkpoints/Project, PMP, README/changelog/English Wiki and AI handoff with executed evidence.
- [ ] Deliver source-bound debug APK/path/hash/certificate/report; owner visual/product acceptance for every required surface including both widgets.
- [x] Record conditional owner authorization for final main integration and Stable publication, gated on all mandatory checks and acceptance.
- [ ] After every gate passes, clean production-signed non-debuggable Stable build, trusted upgrade, main/tag/publication and remote asset verification.

Verification: Phase 1 source/reference/inventory checks only. No new 0.12 Gradle,
JVM, instrumentation, emulator, schema/backup execution or APK. Prior main CI3/3
is read back at ef82898, not rerun. Detailed evidence/limitations in the plan.

Completion: pending. Phase 1 proposal does not mark implementation or acceptance done.
Overall historical maintenance remains INCOMPLETE due to protected-data custody.

created by stokonn with love <3

## Phase2 checkpoint — 2026-10-28

Icon reference SHA256 `fb962bdc0ba9798d676e7be86f934a69159d586e22679aea470464b5ca63b37d`; addendum SHA256 `62917de87128aec514bed62208c035197506c53d1fac248395219455ba7e394c`. Both development-worktree copies match originals; reference remains ignored. Phase2 is one stage; no acceptance execution yet.

- [ ] Audio backup disclosure/media integrity/interrupted restore preservation verified.
- [ ] Genuine distinct recording deduplication, legacy Goldberg eligibility and durable backup-compatible notices verified.
- [ ] Canonical supported PMP checkpoints bind exact feature commits and source manifests; private history preserved.

Internal foundation host gate PASSED:358JVM/0failures/errors/skips, debug/test APK
compilation, lint0errors/132warnings;390unchanged source inputs. Host DDL10paths
passed. Native schema/backup/UI/audio/widget/launcher/upgrade acceptance pending.

Backup v6 + recording repository + streaming media archive host gate PASSED
2026-10-28:363JVM/0failures/errors/skips (5new RecordingArchiveTest), lint0errors/
145warnings, debug APK signer verified, source manifest bound. Native execution of
StudioRecordingTest/StudioBackupTest still pending; see verification/backup-v6-host.json.
Do not mark the audio-backup task complete from host evidence alone.

Shared capture/recording lifecycle (ST-01–03) implemented 2026-10-28:
RecordingController state machine (Idle→Preparing→Recording→Finalizing→Saved
+Recoverable), MicrophoneController fan-out (one AudioRecord, detection+recording
consumers), SmartPracticeService dual-action (START/START_RECORDING), combined
practice+recording lifecycle watcher, Studio UI with Record/Stop controls,
recording consent dialog, EN/CZ localization. Host gate: compile OK, 363 JVM
tests pass, lint 0 errors/147 warnings, localization PASS (1118 resources).
Native/emulator execution of recording lifecycle NOT performed yet.

Previous screen (LB-01) committed d937cdc 2026-10-28: PreviousScreen two tabs
(Practice History keyset-paged + Recordings with play/pause/export/delete),
"previous" route Library-associated, Studio history icon + Library Previous entry.
Host gate: 363 tests pass, lint 0 errors/146 warnings, localization PASS.

Achievements (AC-01–03) implemented 2026-10-28 (host-verified): 6 new badges
(first_take, recording_artist, versatile_pianist, musical_journal,
critical_listener, founding_pianist) with 5 new metrics and 3 new MedalArt
engravings; studio events wired into AchievementEngine/AchievementStore;
durable BadgeNotice unlock popup replacing Snackbar (defers during capture,
acknowledges on dismiss/view); FoundingPolicy cutoff stays null until real 1.0.
AchievementEngineTest extended with 64-hex studio-event fixtures; founding
verified before cutoff. Host gate: compile OK, 363 JVM tests pass (42 suites,
0 failures/errors/skips), lint 0 errors/147 warnings, localization PASS
(1144 EN/CZ resources), policy PASS. Native/emulator execution NOT performed.
BadgeNotice unlock popup is host-verified only; on-device popup behavior is
pending emulator acceptance. No badge is claimed earned on a device from
host-test evidence alone.

Next: Profile 2×2 statistics + era autocomplete + edit draft guards (PR-01–02);
widgets WG-01–04; Tux/quote; metronome/queue-Undo; native/emulator acceptance.
See verification/foundation-host.json and CHECKPOINT.md; do not promote the
whole persistence/audio/icon tasks to complete from these bounded results.


## Profile PR-01–02 continuation — 2026-10-28

One existing Phase 2 implementation stage continues. OpenCode handoff preserved
at `096c9d3`; all 15 private reference hashes/ignore rules rechecked. Inherited
reports inspected: 42 suites/363 JVM tests/0 failures/errors/skips; lint 0 errors/
147 warnings. These are inherited host reports, not native acceptance.

- [x] Implement centered 2×2 statistics with equal row heights and one-column constrained/200% font fallback (source implementation; rendered acceptance pending).
- [x] Implement local distinct-era autocomplete with canonical/localized accent-insensitive matching; retain manual values and composer search.
- [x] Implement SavedStateHandle-backed original/current profile draft, private avatar staging, atomic settings write/three-way merge and explicit save acknowledgement; original stored values and unrelated preferences retained.
- [x] Implement Save/Discard/Keep editing guards and activity-scoped modal; retain failed/conflicting drafts; serialize identity overrides with Google imports.
- [x] Add eight JVM regression cases and three native editor cases.
- [x] Repair inherited instrumentation constructor callers for the shared recording dependency.
- [x] Complete final source-bound debug/JVM/androidTest compile/lint verification (375 JVM; lint0errors/153warnings; source manifest336 unchanged).
- [ ] Execute native editor/audio/backup regressions and actual emulator Profile dismissal/recreation/theme/font acceptance; no native result yet.
- [ ] Synchronize exact verified source/evidence with Issue #41, Project/PMP and AI handoff; continue both widgets afterward.

Initial host gate failed at instrumentation compilation on two inherited obsolete
OpusViewModel constructor callers. Corrected; subsequent host verification is in
progress. No native/UI/upgrade acceptance or release readiness is claimed.


Native blocker correction follow-up (same stage): all **13 targeted Android tests
PASS on emulator-5582/API36** after replacing both forbidden hard links with
RecordingPublication's shared cross-process locked atomic rename. Counts: three
ProfileEditorTest, four StudioRecordingTest, six StudioBackupTest (including new
media archive interruption/retry and collision-preservation cases). The prior
11-case/8-failure result remains recorded in CHECKPOINT.md; it is superseded only
for this corrected source. Host JVM: 44 suites/375 tests/0 failures/errors/skips;
final lint remains in progress. This is not real microphone/player/widget/full
upgrade acceptance. Initial actual Ivory Profile render inspected: centered 2×2
statistics and retained avatar/name arrangement; full visual matrix pending.


PR-02 real cold-process test exposed a missing draft despite SavedStateHandle.
Added private atomic file persistence, serialized off-main writes, explicit
cleanup acknowledgement and corrupt-draft retry/unchanged export; two additional
native regressions. Rebuild/retest is in progress. PR-02 stays unchecked until
this acceptance gap is verified, irrespective of the preceding passing host and
13-case native gate. See the chronological CHECKPOINT.md.


## Verified Profile/publication checkpoint — 2026-10-29

- [x] Add persistent private AtomicFile draft after real cold-process loss was reproduced; retain original/current fields and staged-avatar identity off main, no schema change.
- [x] Verify fresh-task/no-Bundle restoration and corrupt-original preservation in native tests; corruption offers Retry/unchanged SAF export without blocking unrelated navigation or replacing the original.
- [x] Pass final host gate: 44 suites/375 JVM/0 failures/errors/skips, debug/test APK build, lint0errors/153warnings, localization1157/policy/migration-DDL/backup-fixture/paging checks.
- [x] Execute 15 targeted native tests on identified emulator-5582/API36/qemu=1: Profile5, recording4, backup/media6, all PASS (2.948s).
- [x] Verify actual dirty Back/Keep/cold-process recovery (PID6650→6858, COLD launch, unchanged draft bytes and MusicianKept restored), then confirmed Discard/draft-file clear.
- [x] Inspect actual Ivory/EN standard 2×2 and Nocturne/CZ/200% one-column Profile renders, editor and restored-draft capture.
- [ ] Complete remaining visual matrix, actual avatar/document-picker/autocomplete interactions and owner approval; no full PR acceptance is inferred from this subset.
- [ ] Both widgets WG-01–04 remain next; all other unfinished scope and release gates stay open.

[Source-bound final evidence](verification/profile-durable-host-native.json).
All336 inputs unchanged through verification; APK SHA256
`d851e54cff4526c0c57b10e3ee74f1edb3fdc5d4ad86b0d04fb510ac4726e12c`,
trusted certificate verified. Prior 13-case gate and earlier failures remain in
the chronological checkpoint. This is one internal milestone, not a new stage.


## Widgets WG-01–04 — implementation in progress, 2026-10-29

- [x] Implement Quick Practice and Weekly Activity platform providers with immutable explicit activity PendingIntents; no practiceRequest/start action or microphone path.
- [x] Await coordinator recovery before widget/notification navigation; preserve existing active/draft state without selecting over it or creating a session.
- [x] Implement one transactional, week-bounded persisted ledger projection (Mon–Sun, stored-day compatibility, no expanded part/elapsed/audio double count).
- [x] Implement local verified display cache with unavailable/stale distinction, old-week dates, coherent total/day values and semantic theme/accent/font resources.
- [x] Implement API31 responsive RemoteViews and pre31 option sizing; Quick1×1/extended, Weekly compact/2×2/4×2; no tiny chart in constrained/high-font form.
- [x] Implement bounded data/context observation, six-hour WorkManager refresh and local-date rollover; date/zone/locale/package/boot invalidation, no network.
- [x] Verify installed SDK appCategory enum: Education does not exist in Android's manifest category. Use supported audio category for this music app; Education remains a distribution category only where actually supported. No Play/store metadata or publication invented.
- [x] Add 13 host projection/recovery/stale/date/overflow/DST rollover regressions.
- [x] Complete core host/native checkpoint:388JVM/0failures, lint0errors/166warnings, debug/test APK and7 real API36 WidgetTest cases PASS; actual launcher acceptance remains separate.
- [ ] Real launcher add/resize/tap/stale/offline/locale/theme/font/date/reboot acceptance and owner visual approval.

No widget acceptance is claimed from source implementation or compilation. The
one Phase2 stage remains in progress; next source checkpoint follows real checks.


Widget core checkpoint host/native PASSED; source manifest348 unchanged. Native7
cases cover framework inflation and state safety, not actual launcher acceptance.
See verification/widgets-core-host-native.json. Existing high-risk audio/backup/
practice source hashes still match the prior15-case native gate; retain that
bound evidence without redundant reruns during unrelated widget layout edits.
Full combined mandatory acceptance remains required before Stable publication.


Owner decision2026-10-29: personal visual sign-off waived for0.12 Stable; all
references above to owner visual approval are historical and now require documented
actual emulator visual/layout/accessibility/screenshots/automated acceptance instead.
All mandatory functional/data/security/native/signing/upgrade/CI gates remain open
until proven. No new stage; conditional unattended main/Stable authorization applies.


Current visual/widget batch: actual launcher added both providers, verified empty
week/ready state, resized Weekly4×2 and inspected screenshots. Quick2×1 context
responsive key correction and Tux/nature integration are compiled, awaiting focused
native/actual UI verification. Audio/Studio/player/media-backup UI gaps identified
in CHECKPOINT.md remain mandatory implementation, not cosmetic deferrals.


### Recovered visual batch — targeted checkpoint, 2026-10-29

Preserved and completed inherited changes on feature/gould-0.12 from1d4234a.
Targeted debug/test APK build PASS39s; two actual affected native WidgetTest cases
PASS on emulator-5582/API36/qemu=1. Trusted certificate verified; debug APK hash
b81a15a6f76eebdb4e73ed40de57957b4b98ac2fa44af67bb18dda46da1b770b.
Actual settled Home nature and normal pink aligned Tux screenshots inspected.
Font recreation now remains About instead of replaying widget Studio navigation;
Tux dialog itself requires reopening. Initial blank Home capture is not accepted.
Widget safe initial Studio action, actual2×1 context size and4×2 large-font size
threshold implemented. Full launcher/offline/resize/reboot/font/theme matrix pending.
All14 v2 reference hashes match; separate original penguin remains private.
See verification/visual-recovery-targeted.json. Prior388JVM/7native widget evidence
is unchanged-source history, not a fresh complete gate for this batch. Remaining
critical audio/Studio/player/archive and final acceptance gates block publication.
Next coherent batch: serialize writer lifecycle, fix resumed Activity recording
start, await finalization at a frozen practice boundary and repair notification Stop.
