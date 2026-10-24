# Stage 7B final recovery — owner acceptance report

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

Only application-test change: `Stage7bAcceptanceTest.kt` waits for verified actual
Queue-row detail, asserts the legitimate changed-revision re-review gate, clicks
revision acknowledgement and Complete re-review, waits for verified/non-re-review,
and then retains every decision confirmation assertion. Four new acknowledgement
captures extend the previous 258-pair protocol to **262**. No assertions, test
classes or production safeguards were removed. Production code, architecture,
repositories, Room schemas, migrations, backups, practice/timing and localized
resources have no changes in this recovery. Application ID `com.opus.piano` and
`opus.db` are unchanged. The 270-file clean-build source manifest matches the
published source.

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

CI uses Ubuntu24.04/KVM/Java17/API36 and explicit `emulator-5554`, with emulator
serial and `ro.kernel.qemu=1` guards. No local emulator retry, physical phone,
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
- [ ] Manual emulator TalkBack announcements/order/touch exploration, complete
  keyboard/system Back/production OpusApp navigation and reduced-motion checks.
- [ ] Separate-process Android POST_NOTIFICATIONS revocation; controlled tests
  cover denial→grant and role revocation, not that separate platform transition.
  WorkManager tests verify unique3h interval/constraints/cancellation, not a real3h
  wait or reboot-delivery guarantee.
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
