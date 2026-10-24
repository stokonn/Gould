## Closure synchronization receipt — October 24, 2026

Existing Issue #31 dashboard and appended [closure checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6388271729) synchronized; Project 2 overview read back with READY FOR OWNER REVIEW and umbrella In Progress. Official Wiki published at `beac7e6fb908bdaafb3368a7da9ace35fe5e8372`; remote exact, both changed pages match maintained local sources. Final ordinary branch push and immutable documentation SHA are recorded in the subsequent Issue #31 publication receipt. No acceptance, merge, tag, release or production activation occurred.

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

# Stage7B — final owner acceptance decision packet

## Manual acceptance follow-up — October 24, 2026

**Preparation completed; owner decision PENDING. Stage 7B is not accepted.**
CI #25 was not repeated. The temporary opt-in recorder and build selection are now archived outside the active build; final changes contain only retained tooling/evidence/documentation; production Kotlin,
resources, application identity, databases, migrations, backups and practice code
are unchanged. The production APK is still byte-identical to the CI25 recovery
local artifact (SHA-256 `93ae9ec88472e4f07dc5328980786e9ce252887ccbac507c0034195cd9b51961`).
The 270-file manifest was rechecked after restoration and matches the active source exactly.

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

October 24, 2026. **Verified synthetic acceptance passes; Stage 7B remains IN
PROGRESS, awaiting owner acceptance and the explicitly open gates below.** This
report supersedes historical pending-run summaries. It is not release approval.

## Source, scope and preserved work

Branch: `feature/goldberg-0.11-stage-7b`. Tested/published correction:
[`fb1d3f971ed79de09dccbdc771e1c45a09dc0a6c`](https://github.com/stokonn/Goldberg/commit/fb1d3f971ed79de09dccbdc771e1c45a09dc0a6c).
Initial local and remote HEAD were exactly
`dc60e8379114b44f758e0bb5184a0b6b83d7a465`. The existing uncommitted correction and
seven documentation changes were preserved and incorporated; historical untracked
artifacts and all five private local PMP files were retained. PMP hash comparison
passes; `.pmp/` and `.pmp.yml` are never staged. No owner release notes were removed.
No other worktree was edited. Later evidence/documentation commits do not change
the tested executable source; final receipt is recorded in Issue #31.

The earlier CI25 recovery application-test change: `Stage7bAcceptanceTest.kt` waits for verified actual
Queue-row detail, asserts the legitimate changed-revision re-review gate, clicks
revision acknowledgement and Complete re-review, waits for verified/non-re-review,
and then retains every decision confirmation assertion. Four new acknowledgement
captures extend the previous 258-pair protocol to **262**. No assertions, test
classes or production safeguards were removed. Production code, architecture,
repositories, Room schemas, migrations, backups, practice/timing and localized
resources have no changes in this recovery. Application ID `com.opus.piano` and
`opus.db` are unchanged. The 270-file clean-build source manifest matched that recovery
published source; the manual follow-up is described above.

## CI #24 recovered without repeated downloads

[Run40083355927](https://github.com/stokonn/Goldberg/actions/runs/40083355927),
source dc60e83, remains failed historical evidence: raw XML **187 tests /1 failure
/0 errors /2 existing skips**. Both existing ZIPs from
`/tmp/opencode/run-40083355927/` match fresh GitHub digests and pass ZIP integrity.
All **732 archive-member hashes** are inventoried; retained files include complete
raw upgrade evidence. No old artifact was downloaded again.

Independent source and screenshot review confirms MiMo's diagnosis: actual Queue
row-open first reviewed revision1; the stale-queue fixture advanced to revision2.
The app correctly refused decision preparation while re-review acknowledgement
was required. The standard-en-Light failure screenshot visibly shows an unchecked
revision2 acknowledgement and disabled Complete re-review. This failure does not
demonstrate a production defect. Old-source upgrade independently preserves all12
personal collections/37sessions, schema8→10 and database integrity. None of those
old results substitute for new-source verification.

Evidence: [CI #24 independent inventory](results/ci-run-40083355927/independent-inventory.json),
[original archive members](results/ci-run-40083355927/archive-member-inventory.json).

## Executed verification

| Check | Executed result and environment |
|---|---|
| Focused review service + androidTest assembly | PASS13s, 1 task executed/57 up-to-date; native bodies not executed locally |
| Mandatory clean host gate | PASS3m58s, 84 tasks:82 executed/2 up-to-date; Java25, SDK36 |
| Fresh full JVM XML | **275 tests,36 suites,0 failures/errors/skips**; includes26 AdminReview tests |
| Lint | **0 errors,119 warnings,1 hint** |
| Seven existing validation scripts | PASS:29 contract fixtures/15 pinned hashes; policy; portable backups1–3;106-session paging;1050 EN/CZ resources; migrations1–9→10 DDL; Stage3B v5 fixtures |
| Automatic exact-SHA quality | [40097530580 SUCCESS](https://github.com/stokonn/Goldberg/actions/runs/40097530580) |
| Full acceptance, dispatched exactly once | [40097540179 SUCCESS](https://github.com/stokonn/Goldberg/actions/runs/40097540179), both jobs; exact source binding verified |
| Full native raw XML | **187 testcase elements,185 passed,0 failures/errors,2 existing skips**; all9 Stage7B cases PASS |
| Matrix inventory | **262 required PNG/tree pairs**,263 text files including matrix-results; no missing/unexpected PNG states; PNG integrity PASS |
| Checkable hierarchy audit | **516 named occurrences**, enabled focusable/disabled nonfocusable; actual ACTION_FOCUS/isFocused and toggle/disabled guard exercised by native regression in EN/CZ |
| Destination/state audit | All matrix locale/font/dimension metadata matches;32 base destination headings/localized Back owners present;16 actual Queue/Audit sub_1 row captures |
| Independent visual review | **All262 PNGs** viewed in33 contact sheets; selected original PNGs separately inspected;11 supplied design references re-inspected |
| Seeded upgrade | All12 personal collections exactly equal/37sessions;preferences assertion PASS;schema8→10;both raw databases integrity=ok/0 foreign-key violations |

Raw console says189 tests while XML contains187 testcase elements; **XML is the
authoritative count**. Both outputs remain retained. The two unchanged skips are
Stage4ExternalPortraitTest (separately acquired licensed original) and
Stage4ExternalProviderTest (external provider-response fixtures), which were not
supplied. No new skips, filters or exclusions were added.

Host commands (project Gradle wrapper):

```sh
ANDROID_HOME=/home/stokonn/Android/Sdk ./gradlew testDebugUnitTest --tests 'com.opus.piano.admin.AdminReviewTest' assembleDebugAndroidTest --offline --no-daemon --max-workers=1
ANDROID_HOME=/home/stokonn/Android/Sdk ./gradlew clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --offline --no-daemon --max-workers=1
python3 scripts/check_admin_contract_fixtures.py
python3 scripts/check_agent_policy.py
python3 scripts/check_backup_fixtures.py
python3 scripts/check_history_paging.py
python3 scripts/check_localization.py
python3 scripts/check_migrations.py
python3 docs/planning/goldberg-0.11/stage-3b/check_fixtures.py
```

CI25 used Ubuntu24.04/KVM/Java17/API36 and explicit `emulator-5554`, with emulator
serial and `ro.kernel.qemu=1` guards. During that recovery no local emulator retry, physical phone,
production credentials or actual backend activation was used. Full connected
Gradle task passes7m59s. Host/native logs, JUnit, source bindings, original member
hashes and independent inventories are in
[final evidence](results/ci-run-40097540179/independent-inventory.json) and
[clean host evidence](results/final-recovery-2026-10-24/host-results.json).

Independent retained-evidence commands also pass:

```sh
python3 docs/planning/goldberg-0.11/stage-7/stage-7b/validate-final-evidence.py docs/planning/goldberg-0.11/stage-7/stage-7b/results/ci-run-40097540179/admin-evidence --output docs/planning/goldberg-0.11/stage-7/stage-7b/results/ci-run-40097540179/required-state-inventory.json
python3 docs/planning/goldberg-0.11/stage-7/stage-7b/results/ci-run-40097540179/review-extra-inspect.py docs/planning/goldberg-0.11/stage-7/stage-7b/results/ci-run-40097540179/admin-evidence
```

The supplementary audit initially assumed the Back icon description and click
were on one node. Actual native hierarchy puts the description inside its48dp
clickable parent. The audit now checks that ownership geometry; runtime row-open
and Back assertions were unchanged and passed at all8 detailed configurations.
This audit correction required no app or instrumentation change/redispatch.

## Visual and interaction findings

The32 dashboard configurations cover small360×640dp and standard412×915dp,
EN/CZ, Light/Dark parent themes, font1.0/1.3/1.5/2.0. Admin intentionally remains
dark. Ready-state no-scroll≤1.3, exact requested font/locale and complete48dp
reachable actions pass. The16 enlarged-font footer captures show reachable
Refresh and destinations without shrinking text. Four tiles/three destinations
match the approved direction.

All32 base destination renders and16 Queue/Audit reachable-row frames were
inspected. On small2.0, headers/filters can scroll away, exposing usable full
Queue cards and Audit entries. Actual Queue row-open→verified proposal→Back
passes in all8 detailed configurations. Fresh heading/Back checks prevent
previous-route hierarchy export. Four acknowledged revision2 screenshots remove
the re-review panel before confirmation; revision3 screenshots restore the gate
and disclose incomplete history. Old/new proposal text remains distinct from
unavailable current catalogue values.

Confirmations, reason entry/keyboard frames, metered consent, invalid endpoint,
proposal tabs, empty/error/loading and bottom captures were also inspected.
Known differences/limits remain visible: parent scaffold bottom-tab truncation at
larger fonts, low dark-parent status-bar contrast, awkward word breaks at2.0,
and abbreviated extreme-font status/error strings. Confirmation flows use native
Material dialogs rather than the schematics' full-page panels. Contributor text
and field names remain unchanged across locales. Synthetic counts/content are
clearly fixtures, not production observations.

Timed loading/error/keyboard frames do **not** prove every settled state or full
lifecycle: some dashboard-loading captures retain prior unavailable-state text,
and some keyboard visibility varies while animations settle. This recovery does
not claim comprehensive keyboard/inset/error-state acceptance from those labels.
No additional production defect is established by this test-only recovery.

## Upgrade and artifact identity

Immutable baseline: v0.10.1 commit
`b19b77b81e76feef6baf83e6c4bfa935e7c10bea`, debug rebuilt in the same runner.
Both raw before/after SQLite databases were opened read-only for independent
schema/integrity/foreign-key checks, and every original JSON personal collection
was compared. Preference preservation is an executed helper assertion; independent
before/after preference files are not separately claimed. Raw evidence also
confirms Wi-Fi→mobile default-network transition, Data Saver readback and intact
schema10/integrity after actual process death/restart.

| Artifact | SHA-256 |
|---|---|
| Native ZIP12561801732 | `8a75b0c22423cb792f893fa8ce06b04b1ca58bfba023f5b22ef1dfbaf5426223` |
| Upgrade ZIP12561432389 | `c195d8743bce8fa5b2a2e7f6b711b552f82693cd595b4c0a3e85d9be1053d191` |
| Local APK | `93ae9ec88472e4f07dc5328980786e9ce252887ccbac507c0034195cd9b51961` |
| CI upgrade candidate APK | `3b0afc1e5e8318dcec628db7b97ea4dd0977188423e7a587cb5b70eb51da08d0` |

Each new ZIP was downloaded once; the upgrade ZIP was reused during final
collection. ZIP CRC/source bindings and all919 original member hashes verified.
Original ZIPs/extractions remain under `/tmp/goldberg-final-run-40097540179/`;
complete raw upgrade is additionally retained locally under the result directory.

Local APK: `app/build/outputs/apk/debug/app-debug.apk`,22,915,036bytes;
`com.opus.piano`, existing development version0.10.1/code11. Actual badging,
ZIP integrity and test-corpus exclusion pass; APK bytes equal the prior production
build because production source is unchanged. Local signer/trusted previous
release certificate:
`53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.
The downloaded trusted release certificate is separately verified in CI.
Upgrade baseline/candidate instead share temporary CI signer
`3d60b00755b7e67dfa8b035a23d05b1496e4112a8673dec3a824e6fa18aaaf00`.
**This is not release-signed in-place installation verification or a release APK.**

## Open gates and owner disposition

- [x] Interrupted recovery, CI #24 inventory, narrow test correction, clean host
  verification, normal push, one exact-source CI and independent raw/visual audit.
- [x] Prepare this concrete owner review packet with preserved evidence/limitations.
- [x] Representative emulator TalkBack order/touch exploration/accessible Back,
  keyboard/insets/system Back/real OpusApp navigation and zero-scale behavior
  executed as described in the current follow-up.
- [ ] Audible speech accuracy, subjective motion comfort, exhaustive configuration/
  keyboard matrix and owner disposition on repeated Settings focus stops.
- [x] Separate-host-process POST_NOTIFICATIONS revocation, process termination,
  cold restart and denied-permission worker suppression verified in the follow-up.
- [ ] Real3h wait/reboot-delivery guarantee remains unexecuted; earlier scheduling
  tests and this permission probe do not establish it.
- [ ] Approved live G1–G9/G11 DTO/session/principal/current-catalogue/dual-revision/
  audit/validation integration. G10 local networking boundary stays closed;
  production networking and moderation decisions remain disabled. No real sign-in
  or OAuth success is claimed.
- [ ] Explicit owner acceptance of the documented Stage7B scope/limitations.

[Final independent checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6384951524).
[Issue31](https://github.com/stokonn/Goldberg/issues/31) remains OPEN and
[Goldberg Development](https://github.com/users/stokonn/projects/2) In Progress.
[Official Wiki](https://github.com/stokonn/Goldberg/wiki) verification notes are
synchronized with local maintainable sources. Published production remains
[Goldberg0.10.1](https://github.com/stokonn/Goldberg/releases/tag/v0.10.1).
Stage7A remains accepted; X10 is NOT FIXED/NOT VERIFIED and deferred to Gould0.12.
No main merge, production activation, release, tag overwrite or phone testing.

Next agent: read this report and the final Issue checkpoint. Preserve private and
historical evidence; do not redispatch unchanged code. Obtain owner disposition
and separately resolve only the open gates under their authorized scope. Keep
Stage7B/umbrella/Project open until applicable acceptance actually passes.

created by stokonn with love <3
