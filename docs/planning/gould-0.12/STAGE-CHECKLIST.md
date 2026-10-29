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


### Studio integrity and controls batch — 2026-10-29, implementation in progress

- [x] Serialized capture writer lifecycle, identity-checked retained callbacks, synced interrupted files and recoverable preparation identity.
- [x] Visible Activity context and honest foreground-start failure; removed arbitrary Finish delay; frozen practice credit before awaited finalization; record-only notification Stop implemented.
- [x] Actual API36 native recording lifecycle6 + prior repository4:10/10 PASS on intermediate audio source. Actual UI consent→AudioRecord→Stop saved93.158s WAV,22050Hz/mono/PCM16/4108332bytes, SHA2564fd881e67f2472f26348effde76cb83606c7cb5de714fd1df4f4a7758f1b57b1. Acoustic performance is not established.
- [ ] Verify combined queue intent/order/one-level Undo persistence with stable interval indexes, piece/session note fields and bounded manual metronome. Sources/tests added; full host gate running.
- [ ] Verify async lifecycle/focus/seek/pause player, advancing-playback achievement evidence, keyset audio pages, explicit recovery and failure-retaining deletion. Sources/tests added.
- [ ] Verify explicit SAF journal+audio archive UI, durable pending receipts and active-capture restore rejection/locking. High-risk native backup regression added.
- [ ] Complete badge equal-row layout, full launcher/UI/native/data/upgrade/security/CI/release matrix. No acceptance inferred from source presence.

Full batch log: .gradle/gould012-checks/studio-combined-host.log. Previous failed metronome syntax attempt retained in studio-controls-targeted.log and corrected before this gate. Remaining functions and acceptance still block Stable.


### Corrected native and layout checkpoint — 2026-10-29

Native retest13/13 PASS (RecordingLifecycle7/StudioRecording4/schema10→11 migration1/
metronome1),0.643s. Corrected both genuine failures from the34-case run; do not hide
that failed run. Delayed startup reconciliation cannot interrupt the current live
take. Final widget RemoteViews/font-change footer2/2 PASS,0.379s; final targeted
debug/test APK build2m43s PASS. Source/evidence in verification/studio-controls-
checkpoint.json. Full393JVM host gate and lint0errors/165warnings/1hint preceded
these bounded native/layout fixes; final lint/full native remain pending.
Current required quality CI e2e0b0a failure40832264736 is the same paging extractor
bug now fixed locally; backend and maintenance CI passed. No unchanged CI retry.
New source checkpoint must be pushed then quality/full native/acceptance verified.
Additional source audit to finish: rapid recording stop/new-start service ownership
and capture-start acknowledgment; do not promote final audio acceptance prematurely.
Actual new controls/badge/player/archive flow and full widget/font/theme/locale/
reboot/rollover matrix, API26, seeded trusted upgrade, security/release and required
CI remain pending. No Stable tag/build/publication or main integration performed.


### Live full-native/capture follow-up recovery — 2026-10-29

Remote09b1fe4 is pushed; required CI3/3 PASS: quality40835545804, maintenance
40835545828, backend40835545895. Canonical PMP snapshot40/30e537c2-457f-413e-8a98-
7b816c2ae095; Issue41 comment6490687721. Unfiltered actual API36 native run
full-native-09b1fe4.log (tool session33894) is still running through253 cases;
do NOT start another run or modify emulator configuration while it runs. It began
with inherited font2/offline settings. Current known failures: obsolete-request
fixture assumes exactly one cancellation though filter changes can start another;
direct receiver invocation has no PendingResult; controlled-worker test requires
a default network, unavailable because offline was retained. Known gated external/
synthetic assumptions are skips, not passes. Final counts pending; retain all logs.

Uncommitted follow-up is intentional and must be preserved: AudioRecord startup
acknowledgment keeps controller Preparing until capture really starts; service
tracks closing/active ownership, stops microphone after recording even during
manual timing, and waits before a new explicit take. Preparing interruption retains
bytes; notifications update at phase transitions; duplicate UI start jobs guarded.
Native real-service two-take/manual-timer preservation test and acknowledgment
regressions added. Receiver null PendingResult guard and all-obsolete-jobs fixture
accounting fixed. Also discovered unused audio association table: now records exact
PCM boundaries/title/composer snapshots on manual switch/Undo and links practice
started after recording; Previous shows bounded previews and related saved practice.
Native exact sample/recovery/backup validation and complete decoded playback award
regressions added. All are pending current-source verification.

Single frozen host gate capture-associations-final-host.log (session23543) runs
assembleDebug/assembleDebugAndroidTest/testDebugUnitTest/lintDebug with3GB/in-process
compiler. Earlier capture-ownership-build.log passed but source changed during
that build, so it is NOT final evidence for the newest ownership/association fixes.
Do not duplicate Gradle or install its new APK over the running old-source suite.
When suite ends, restore network online/font1 for nominal targeted native cases;
classify offline controlled-worker failure as setup, not an application pass/fail.
Then install the final built APKs and execute RecordingLifecycleTest,
RecordingServiceTest, StudioControlsTest, StudioBackupTest, PracticeServicePolicyTest,
DiscoverEditorialAcceptanceTest and affected worker test; fix genuine failures.
New RecordingServiceTest expects no unrelated pending practice and creates/pauses
a named emulator-owned manual draft without discarding data. Preserve it honestly.

API26 image installed and isolated gould012-api26 AVD prepared under .gradle/profile-
avds (not booted yet). Never API37/phone. New badge/widget layouts compile and
focused widget footer2/2 passed at09b1fe4; full actual new badge/Studio/player/archive
visual matrix and final font-change launcher retest remain pending. Additional
audit item: popup acknowledgement currently lacks graceful storage-failure handling;
fix before final release if not yet addressed. Main/Stable untouched.


### API26 trusted upgrade and native correction checkpoint — 2026-10-29

Unfiltered API36 source09b1fe4 finished253:238PASS/3FAIL/12assumption skips.
Full log retained, not accepted as a clean full-native pass. Failures: obsolete
query fixture's exact-one cancellation assumption; direct receiver PendingResult
null; controlled-worker expected network absent under inherited offline state.
Current source fixes accounting of every obsolete request and null receiver token.
Online nominal worker then revealed its second fixture requirement: notifications
must start denied. After pre-run emulator-only revoke, worker passed fully. No
application check was weakened. Discover progressed beyond cancellation, then its
helper clicked a different partially visible duplicate Source records button;
whole-control viewport selection fixed in the test, native retest pending.

Current capture/association full host PASS11m50s:393JVM/0failures/errors/skips,
debug/testAPK and lint0errors/165warnings/1hint. API36 affected native26 had25PASS
and1worker-fixture failure; the worker subsequently passed with correct preconditions.
Exact sample-boundary associations and archive roundtrip tests added, positive
full decoded short-take award verified. Burst metronome settings now apply the last
requested tempo/beat state; serialized queued seeks exclude jump credit; popup
dismissal storage errors retain durable notices instead of crashing.

Actual published Goldberg-v0.11.apk downloaded SHA256290a15e7b15da701b8f0a40e98bf8b7c1f7cc00ce487504dcb6d4ab2e11b40d6,
trusted signer53eface4… verified. Isolated API26 gould012-api26/emulator-5584 booted
(qemu=1). Guarded root-fresh upgrade from actual non-debuggable0.11/schema10 to
debug0.12-dev/schema11 PASS:37 original sessions, original tables/notes/profile/
badge dates/draft bytes/preferences retained, integrity/FK PASS, no manufactured
audio/events. Candidate83e96f8e5c77886aac65169fd59b8a6edff7f114a7f9a088ce13f2e246a6b774.
Evidence: .gradle/gould012-checks/trusted-upgrade-api26/result.json and snapshots.
Upgrade harness now requires exact published digest/trusted certificates and an
empty identified API26/36 AVD before root seeding; normal CI keeps throwaway signing.
Current CI acceptance expected schema updated10→11; old stable tags/history unchanged.

API26 actual native compatibility33/33 PASS5.351s: recording lifecycle/clock/
associations, repository/media backup8, decoded playback/seek/metronome, historical
migration and focused widget inflation/footer/provider/DAO. No global widget tests
that reset the seeded recovery were used. Real API26 microphone record-only test
PASS1/1/4.291s, saved validated WAV and exact original draft/token/notes/credit and
37-session count unchanged. Initial test helper used UiAutomation method unavailable
onAPI26; preserved failure and corrected to shell permission grant + assertion.
API36 emulator-5582 was stopped gracefully with userdata retained, never wiped;
its profile AVD can be restarted. Current active target is emulator-5584/API26 only.

Final privacy/focus polish is uncommitted: actual capture now blocks local playback
(and no idle playback polling), recording duration uses semantic error ink, saved
vs incomplete export labels are honest, analysis/record consent/onboarding and
bundled/full privacy notices distinguish saved PCM and unencrypted audio ZIPs.
Privacy dialogs scroll at200%. Current single final host gate is capture-privacy-
final-host.log (session to recover from tool if needed); do not duplicate Gradle.
Then install final APK/testAPK on identified emulator, reverify capture/playback
block/regressions and actual UI renders, commit/push and synchronize Issue41/PMP.
Full final API36 CI/native run, actual new UI/widget matrix, boot/rollover/recovery/
SAF exports/backups/security/licensing and final Stable artifact gates remain open.
No main merge, release build, tag or publication; owner visual waiver stays valid.


### Verified capture/privacy/compatibility source checkpoint — 2026-10-29

Final full host PASS393JVM/0failures/errors/skips; debug/testAPK; lint0errors/165
warnings/1hint,6m54s. Localization1207/policy PASS. Follow-up bounded frame ownership/
prompt teardown build2m52s and playback-delete integrity build2m16s PASS.
API26 final affected18/18 PASS7.205s, then final playback/delete/real microphone
6/6 PASS6.303s. Already-started frames are not assigned retroactively to a new
take; stopped/replaced sinks cannot append. Recording uses actual microphone-start
acknowledgment and stops capture promptly when only manual timing remains.
Playback is blocked during microphone use, has no idle polling, serializes queued
seeks and awaits verified listening event commits before explicit deletion/restore.
Exact original recovered draft/token/notes/credit and37 original sessions remained
unchanged through actual API26 microphone tests. Earlier33/33 compatibility and
actual published trusted0.11 upgrade remain separately source/artifact-bound.

Consent/onboarding/settings/public+bundled privacy now truthfully distinguish
analysis from explicit recorded PCM/WAV, storage/export privacy, optional audio
ZIPs and incomplete unchanged exports; original backend/legal policy retained.
Profile/signature/15 private references and protected worktrees remain intact.
Final debug APK e109904ab46e1e6de197848eb776fe718adb8592f47d5e4bba33353d1af1b613;
trusted signer53eface4… verified. See verification/capture-privacy-api26-checkpoint.json.

Next: push this checkpoint, synchronize canonical PMP/Issue41, dispatch existing
API36 full native+upgrade CI on exact source; continue actual rendered UI/widget/
recovery/SAF/backup/security/licensing acceptance. Do not claim a clean full native
pass from the failed253-case run or assumption skips. Discover whole-control
helper correction needs rerun. Final non-debuggable Stable remains unbuilt/unpublished.
Active emulator5584/API26; preserved profile5582/API36 stopped, can restart for
final full UI matrix. No physical phone, data reset, new stage or main integration.

### Full API 36 CI follow-up — 2026-10-29

Exact source `1d06f0830d013ec443071982cf55651d8218ebed` passed the three required automatic CI workflows and the API36 acceptance-subset job in [run40849112564](https://github.com/stokonn/Gould/actions/runs/40849112564). The full native job **FAILED** two widget cases: a preceding actual-microphone test left its own paused practice, violating their empty-fixture precondition. The recording test now resets only its own matching synthetic token in `finally`, preserving actual WAVs and unrelated recovery data. Native test compilation passed; corrected full-suite execution remains pending. Application source and its prior393JVM/affected-native evidence are unchanged. Raw CI logs/reports are retained under ignored `.gradle/gould012-checks/`.

Actual320dp/font2 UI checking is in progress on identified `emulator-5584`/API26. A driver gesture that began over Studio's fixed footer failed to scroll its content; the driver now derives gestures from the observed scrollable viewport. Captures alone are not visual/accessibility acceptance. Remaining full UI/widgets/SAF/process-death/native/security/Stable artifact gates stay open. No main integration, release build, tag or publication.


## Current verification checkpoint — 2026-10-29, source2c5420c

- **Required automatic CI3/3 PASS**, and [full API36 native + upgrade run40871204814 PASS](https://github.com/stokonn/Gould/actions/runs/40871204814). Canonical XML totals:262 cases,251 passed,0 failures/errors,11 fixture/probe-dependent skips. Two separately acquired provider/portrait cases and nine opt-in synthetic migration/restore/process-death/reboot probes still need separate applicable execution; skips are not passes. The preceding failed run remains preserved.
- Actual API26 visible recording consent → capture → intentional app force-stop → cold start produced a recoverable take with no restarted capture service. The original3s draft payload/updatedAt and37 sessions remained exact. Android SAF export of interrupted bytes is byte-identical; explicit recovery retains all661504 PCM samples at22050Hz,1323052-byte WAV SHA256 `816cb1c3761994e4c323e4e28d92f921b351553f629dc793a3409d00c316927f`. Actual player pause remains3s across observations, seek moves to21s and resume advances23s. Saved WAV SAF export is byte-identical. Emulator input only; no acoustic piano fidelity claim.
- Actual SAF format6/schema11 JSON and journal/audio ZIP validate; all4 actual audio entries match manifest sizes/hashes and recovered PCM. Same-state restore UI validation is in progress. Evidence: [audio UI](verification/actual-audio-ui-recovery-api26.json), [native CI](verification/full-native-fixture-followup.json).
- EN/CZ, Light/Dark,320/360/412dp and1/1.5/2 font extreme captures exist; only inspected scenes count as review. Full meaningful visual/accessibility/widget matrix remains open. The temporary driver now excludes decorative headings and derives scroll gestures from the actual viewport. Earlier Data & Storage captures showing Settings root are invalid for backup-page acceptance and are retained as diagnostics. Official open-source Google TalkBack QA-tool build is in progress with isolated throwaway signing; no spoken/TalkBack PASS inferred.
- English Wiki sources now describe implemented0.12 behavior and format6/schema11 while clearly retaining0.11 as latest published Stable. No Wiki publication or Stable claim is implied. App inputs/debug APK remain unchanged from1d06f08,393JVM host and affected API26 evidence retained. Owner unattended conditional publication/visual waiver remain applicable.

**Phase2 remains IN PROGRESS.** Complete actual UI/widgets/editor/queue/IME/TalkBack, remaining applicable synthetic/backup/recording/privacy/security/licensing/performance, then final clean trusted non-debuggable Stable artifact/upgrade gates. No main merge/tag/release build/publication. API36 + API26 only; no physical phone. Preserve all15 private references and original drafts/recordings.

Historical chronological checkpoints below retain their original source-bound scope; this current dashboard supersedes their pending implementation descriptions.

