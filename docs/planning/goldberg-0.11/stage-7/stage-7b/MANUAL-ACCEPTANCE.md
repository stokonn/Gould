## Final administrative closure — October 24, 2026

**READY FOR OWNER REVIEW — NOT ACCEPTED. Recommendation: ACCEPT WITH DOCUMENTED LIMITATIONS**, subject to the owner's explicit disposition of the limitations below. This recommendation covers the completed Stage 7B Android client and synthetic/offline verification scope; it does not authorize live integration or release.

- [x] Admin dashboard, Queue/re-review/decision safeguards, Audit, Settings and local sync/notification behavior verified within the documented synthetic scope.
- [x] CI #25 retained unchanged: 187 native tests, 185 passed, 2 existing skips, no failures/errors; all 9 Stage 7B tests passed. All 262 screenshot/tree pairs reviewed; seeded upgrade retained 12 collections/37 sessions, schema 8→10. Prior clean host gate: 275 JVM tests passed and seven validation scripts passed.
- [x] October 24 emulator observations retained: real EN/CZ TalkBack traversal and accessible Back, touch exploration, Tab order, IME/insets/system Back, zero-animation navigation and cross-process POST_NOTIFICATIONS revocation/restart/worker suppression. EN/Light/1.0 and CZ/Dark/1.3 are representative samples, not an exhaustive configuration matrix.
- [x] Unique temporary manual recorder and exact Gradle patch archived under `results/manual-acceptance-2026-10-24/sources/`, with byte/hash receipts, before removal from the active test tree. The 270-file source manifest matches; active app, tests, Gradle and CI match the verified baseline. Default AndroidJUnitRunner restored. No production behavior changed.
- [x] Restored normal build: `./gradlew assembleDebug assembleDebugAndroidTest lintDebug --offline --no-daemon --max-workers=1` PASS in 2m08s (13 executed/64 up-to-date); lint 0 errors/119 warnings/1 hint. This compiled instrumentation; it did not execute a fresh complete native suite. CI #25 was not repeated.
- [x] Production APK ZIP integrity, unchanged SHA-256 `93ae9ec88472e4f07dc5328980786e9ce252887ccbac507c0034195cd9b51961`, package/version and trusted signing verified. Evidence-consistency verifier PASS; original screenshots/logs/JSON/hash inventory and historical failures preserved.
- [ ] Owner explicitly approves final disposition. Stage remains READY FOR OWNER REVIEW; Issue #31 stays OPEN and the umbrella Project stays In Progress.
- [ ] Proposed Stage 8 deferral, requiring owner agreement: duplicate TalkBack label/control focus stops in three Admin Settings rows; existing large-font bottom-tab truncation and historical scaffold status contrast limitation. Controls remain named, reachable and checkable; no semantics fix is claimed.
- [ ] Unverified quality limits retained: audible speech/pronunciation, subjective motion comfort/frame timing, exhaustive accessibility/font/keyboard matrix and real three-hour/reboot delivery. These checks are not marked passed.
- [ ] Stage 6 authorization/approved live backend contracts and credentials: G1–G9/G11 remain explicitly BLOCKED. Stage 8 broader integration/release gates remain separate; no backend activation, merge or release is authorized by this packet.

Evidence: `results/manual-acceptance-2026-10-24/verified-results.json`, `closure-verification.json`, `raw-evidence-inventory.json` and `sources/archive-receipt.json`. The final publication receipt in existing Issue #31 records the exact documentation commit; CI evidence remains bound to `fb1d3f971ed79de09dccbdc771e1c45a09dc0a6c`. Private PMP and unrelated historical files remain local and uncommitted. Earlier checkpoints below are chronological history and do not reopen completed verification.

created by stokonn with love <3

# Stage7B manual acceptance — verified preparation, owner decision pending

## Manual acceptance follow-up — October 24, 2026

**Preparation completed; owner decision PENDING. Stage 7B is not accepted.**
CI #25 was not repeated. This follow-up changed only an opt-in test instrumentation
recorder, its build selection, and evidence/documentation; production Kotlin,
resources, application identity, databases, migrations, backups and practice code
are unchanged. The production APK is still byte-identical to the CI25 recovery
local artifact (SHA-256 `93ae9ec88472e4f07dc5328980786e9ce252887ccbac507c0034195cd9b51961`).
The 270-file manifest later in this report describes the earlier recovery source,
not the newly added manual recorder/test-build configuration.

### Venue and scope

A new disposable API36 AVD booted in37.387s using emulator37.2.12,
Lavapipe/ANGLE+Mesa llvmpipe. This avoids the earlier crashing SwiftShader GLES
backend. Every ADB operation explicitly targeted **emulator-5586** with qemu=1
verification before application operations. Existing AVDs and other worktrees
were untouched. Temporary-filesystem storage preflights failed because the
emulator enforces a6GiB userdata minimum; the working isolated AVD is preserved at
`.gradle/manual-acceptance-avds/Goldberg7BManualDisk.avd` and is now stopped.

Real installed **TalkBack16.0.0.738667889** and Google TTS were used. The recorder
keeps other accessibility services active via FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES.
It renders the real production **OpusApp/NavHost**, injecting only an in-memory
synthetic AdminRepository. No production Admin transport or moderation activation
occurred. English/Light/font1.0 and Czech/Dark/font1.3 were exercised at720×1280px,
320dpi (360×640dp). The second session verified actual locale, font scale and saved
theme metadata, not merely requested settings.

### Executed results

| Gate | Actual result | Limits/finding |
|---|---|---|
| TalkBack navigation | EN Audit8 stops: heading→search→scope notice→All→My actions→Moderation→event title→sub_1. CZ Audit6 localized stops through Moderace. Real service focus events/screenshots retained. | Representative routes/configurations; no audible-speech accuracy claim. |
| Touch exploration | Kernel touchscreen input generated native exploration start/end events and focused Search. | Shell-injected swipes did not establish exploration and were excluded. |
| Accessible Back | Real TalkBack swipe-previous reached Back/Zpět; kernel double tap returned Audit→Administration in both locales. | No programmatic accessibility-focus action substituted for gestures. |
| Keyboard order | Actual Tab8 order in Audit: Back→search→All→My actions→Moderation→Refresh→Home→Library. | Not an exhaustive hardware-key/shortcut matrix. |
| Keyboard/insets | Ordinary Queue and endpoint-dialog text entry visible with settled558px IME inset. | Gboard's initial stylus tutorial was setup history, not an app result. |
| System Back | QueueIME→Queue→Administration; endpointIME→dialog→Settings→Administration; Administration→Profile. Real production NavHost. | Other app routes/dialog combinations are not exhaustively covered. |
| Reduced motion | All3 system animation scales0 read back; Queue/Audit/Settings navigation and Back to Administration/Profile worked; original1 scales restored. | No high-speed frame-duration or subjective vestibular assessment. |
| Platform permission revoke | Separate host process revoked POST_NOTIFICATIONS while PID6176/notification701 were active. Target PID terminated; cold restartPID7748 retained DENIED. New workerPID7815 made6 fixture calls/changed signature/posted no notification. | Planned runner termination at revoke is expected platform behavior, not an app defect. No real3h/reboot-delivery claim. |
| Settings controls | All3 named checkable controls reachable in CZ/Dark/1.3; current state and names retained. | **New observed limitation:** label and associated control are separate TalkBack stops, repeating each label. |

The last finding is visible in `cs-settings-focus-3.png` versus `-4.png`, and
likewise at5/6 and7/8: a TextView label is followed by its named checkable control.
It was recorded without changing production semantics. Existing larger-font
bottom-tab truncation remains visible. The representative dark full-NavHost sample
has readable light status icons; the older CI scaffold contrast limitation is not
claimed to be a new production fix.

### Validation and retained evidence

Final opt-in recorder assembly/lint PASS **1m05s**,9 tasks executed/51 up-to-date.
Default-runner assembly/lint PASS **13s**,4 executed/56 up-to-date; packaged and
installed runner verified as `androidx.test.runner.AndroidJUnitRunner`.
Lint remains **0 errors /119 warnings /1 hint**. No new JVM bodies or complete
instrumentation suite ran; no full CI dispatch was made. Manual probe completion
is not reported as a JUnit test count. The existing CI25 results remain bound to
fb1d3f971ed79de09dccbdc771e1c45a09dc0a6c.

```sh
ANDROID_HOME=/home/stokonn/Android/Sdk ./gradlew assembleDebugAndroidTest lintDebug -PgoldbergManualProbe=true --offline --no-daemon --max-workers=1
ANDROID_HOME=/home/stokonn/Android/Sdk ./gradlew assembleDebugAndroidTest lintDebug --offline --no-daemon --max-workers=1
```

The opt-in component is
`com.opus.piano.test/com.opus.piano.Stage7bManualProbe`, selected only by the build
property. Its `goldbergManual=interactive` phase records actual host/TalkBack
interactions; `goldbergManual=revoked` verifies denied-permission worker behavior.
Default CI discovery/runner and all pre-existing test assertions are unchanged.
The manual APK is retained only under `.gradle/manual-acceptance-artifacts/`,
SHA-256 `3a6bc0b50c7b4224a83282a6e874dee8bc81b37dbae497b68b2ef8b48cb09bde`.

[Raw observations/inventory](results/manual-acceptance-2026-10-24/raw-evidence-inventory.json)
retain107 PNG observations plus JSON/native events, host command transcripts,
compile logs and setup history. This count is not a pass count. Selected original
screenshots were actually inspected: both locale Audit focus/Back frames,
Queue/endpoint IME, reduced-motion Settings, restart screen, Czech dashboard/Audit
and the three repeated Settings label/control stops. Complete original app-side
archives and unsanitized emulator logs remain locally under
`.gradle/manual-acceptance-artifacts/raw-evidence/`; published emulator logs omit
local ADB public-key/JWK-path lines and are explicitly marked sanitized.

The read-only [observation verifier](results/manual-acceptance-2026-10-24/verify-observations.py)
passes against retained hashes, actual native focus, route/IME states, permission
PID transitions and the repeated Settings focus stops. [Verified results](results/manual-acceptance-2026-10-24/verified-results.json)
record both successes and limitations. Initial input calibration, Gboard setup,
probe registration/theme-ordering and controller-label lookup trials are retained;
they were corrected before counted results and are not production fixes.

### Prepared owner decision

- [x] Execute feasible representative emulator checks and retain actual results.
- [x] Verify separate-process platform permission revocation and restart behavior.
- [x] Restore default test runner and animation scales; leave notification
  permission DENIED; stop only the new isolated emulator; preserve evidence/PMP.
- [ ] Owner disposition on the **repeated Settings TalkBack label/control stops**
  and the disclosed non-exhaustive/audible-speech/reduced-motion limits.
- [ ] Explicit Stage7B approval. No agent acceptance is recorded.
- [ ] Live **G1–G9/G11 remains BLOCKED until Stage6 authorization**. No sign-in,
  current-catalogue/dual-revision/real moderation integration is approved here.

The decision is prepared as: **approve the verified client scope with explicitly
accepted limitations, or hold for a focused accessibility fix/further checks.**
Neither option is selected. This is not approval for live integration, main merge,
production deployment or release. Issue31 and Project2 remain open/In Progress.

---

<details>
<summary>Earlier preparation checkpoints (historical; preserved)</summary>

# Stage 7B manual acceptance preparation — October 24, 2026

Status: IN PROGRESS; no owner acceptance claimed. CI25 remains verified historical
source-bound evidence and will not be repeated. Initial branch
feature/goldberg-0.11-stage-7b, HEAD0887d3fe33cf5d49d46e14e1de95168f08a493a2.

Purpose: perform the feasible remaining emulator interaction/accessibility/permission
checks, recording actual outcomes and limits before an explicit owner decision.
Scope: disposable emulator, test-only helpers if required, evidence and tracking.
No production/database changes, physical phone, live backend, merge or release.

- [x] Review policy, owner report, latest local checkpoints and Issue31; inspect
  branch/remote/tags/releases/milestone/Project. Tracked tree initially clean;
  private PMP and historical evidence preserved.
- [x] Inspect previous emulator crash diagnostics: actual libGLESv2 SwiftShader
  SIGSEGV. Existing emulator37.2.12 explicitly supports Lavapipe; no running QEMU.
- [x] Create disposable API36 AVD and boot changed Lavapipe/ANGLE+llvmpipe renderer; emulator-5586/qemu1/boot PASS37.387s. /tmp storage preflight failed (6GiB minimum overrides); isolated AVD moved to ignored .gradle/manual-acceptance-avds with sufficient space. Existing AVDs unchanged.
- [x] Installed trusted APK; production SHA unchanged93ae9ec…; real TalkBack16.0.0.738667889 and Google TTS available.
- [x] Separate manual runner compiles/lints PASS1m11s; no JUnit checks rerun. AGP discarded additional instrumentation manifest entry; corrected explicit opt-in build property preserves default CI runner. Opt-in compilation in progress.
- [ ] Check real TalkBack availability and accessibility navigation order.
- [ ] Exercise touch exploration and accessible Back with actual service evidence.
- [ ] Exercise keyboard/insets and system Back; distinguish synthetic Admin from
  complete production navigation, without enabling production transport.
- [ ] Exercise reduced-motion settings and observe actual behavior.
- [ ] Revoke POST_NOTIFICATIONS from a separate host process while app is running;
  verify post-revocation state across app restart without runner suppression.
- [ ] Record actual results, screenshots inspected, limitations and exact blockers.
- [ ] Synchronize owner report, version/stage checklists, AI checkpoints, existing
  Issue31 description AND checkpoint, Project In Progress.
- [ ] Explicit owner acceptance. G1–G9/G11 live integration remains BLOCKED until
  Stage6 authorization; production networking/moderation stay disabled.

Next: attempt changed disposable emulator venue. Zero new Android checks executed
so far. Preserve original AVDs and all personal/recovery/private data.

created by stokonn with love <3

## Interactive checkpoint

Default recorder build/lint PASS1m11s; opt-in recorder APK assembly PASS17s.
Production APK unchanged93ae9ec88472e4f07dc5328980786e9ce252887ccbac507c0034195cd9b51961.
Real production OpusApp/NavHost launched with only synthetic AdminRepository injected;
Profile→Administration→Audit observed. Real TalkBack16 bound with spoken/haptic/audible
feedback and touch exploration enabled; notification permission prompt handled.
**TalkBack gesture/navigation-order acceptance NOT yet established:** shell-injected
swipe/key-combination did not produce navigation/touch-exploration events. Kernel
emulator touch input is next. First bounded interactive session ended normally after
its recording window; second explicit session starting, earlier events preserved.

Actual granted-permission worker baseline PASS: PID5265,6synthetic fixture calls,
signature changed and notification701 posted. Cross-process revoke still pending.
Raw files: /tmp/goldberg-manual-evidence; helper /tmp/goldberg-manual-host.py (load
with runpy.run_path because filename has hyphens). Emulator5586 remains running;
all ADB calls MUST use -s emulator-5586 and qemu guard. Opt-in component:
com.opus.piano.test/com.opus.piano.Stage7bManualProbe; -e goldbergManual interactive
or revoked. Build: ./gradlew assembleDebugAndroidTest -PgoldbergManualProbe=true.
Host requests snapshots by touching app-private files/manual-capture-NAME via run-as;
recorder writes files/stage7b-manual/NAME.json/.png; host helper retrieves them.
Current source is uncommitted test-only Stage7bManualProbe.kt + app/build.gradle.kts
opt-in property (default AndroidJUnitRunner unchanged). No JUnit/full CI rerun.
No owner acceptance, live integration or production changes.

## Verified emulator milestones — October24,2026 continuation

- Real TalkBack bound, no UiAutomation service suppression. Kernel input (not
  adb shell input swipes) produces actual touch-exploration events and focus.
  Audit forward order8: heading→search→scope notice→All→My actions→Moderation→
  event title→sub_1. Touch search then swipe-previous reaches heading→Back;
  kernel double tap activates Back and real NavHost returns Administration PASS.
- Actual keyboard Tab8 order: Back→search→All→My actions→Moderation→Refresh→
  Home→Library PASS (Audit). Initial setup controller assumed Dashboard while on
  Audit; corrected route, no app/test assertion changed.
- Initial keyboard trial opened Gboard's first-use stylus tutorial, excluded from
  app acceptance. Tutorial dismissed; handwriting disabled only in disposable
  emulator. Ordinary Queue and endpoint dialog IME visible/bottom558px, text field
  visible and entered synthetic text. System Back: QueueIME→Queue→Administration;
  endpointIME→dialog→Settings→Administration PASS; no endpoint saved.
- All3 animation scales0 read back; actual Queue/Audit/Settings navigation and
  Back to Administration/Profile PASS. Profile Edit Profile label is a content
  description (initial text-only controller assertion corrected from raw snapshot).
  Original scales1 restored. No subjective vestibular/high-speed frame claim.
- Separate-host-process POST_NOTIFICATIONS revoke PASS: livePID6176/notice701
  before; PID absent after pm revoke; cold startPID7748, permission DENIED persists.
  New probePID7815: permissionDENIED,6fixture calls,signature changed,no notice.
  Expected runner termination during pm revoke retained, not an app defect.
- Production APK still unchanged; no full CI/JVM suite repeated. G1–G9/G11 remains
  BLOCKED; no sign-in/backend/production activation or owner acceptance.

Raw proof under /tmp/goldberg-manual-evidence includes original app-side evidence
archive before extra sampling. Further feasible sampling: Czech/dark TalkBack and
larger fonts if available; then restore default test runner/build, retain evidence,
synchronize final report/checklists/Issue31/Project and prepare explicit owner
acceptance options with all remaining limitations. Do not repeat CI25.

</details>
