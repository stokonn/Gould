# Gould 0.15 — verification decision matrix

Three tiers, defined in `AGENTS.md`. Read this matrix to decide what a change
invalidates; do not rerun tiers wholesale without an invalidation reason.
Every executed tier run appends one line to
`verification-audit.jsonl` in this directory via `scripts/verify-audit.py`
(called automatically by the tier scripts). Older versions' audit trails
(`gould-0.13`, `gould-0.14` records) remain untouched as historical evidence.

| Change area | FAST (everyday) | INTEGRATION (feature boundary) | RELEASE (source freeze, additive) |
| --- | --- | --- | --- |
| Kotlin logic, no UI/DB contract | `compileDebugKotlin` + focused JVM tests | full JVM suite | full clean policy gate |
| Compose UI / layout / theme | compile + affected JVM tests (`Redesign*Test`, `WidgetProjectionTest`…) | full JVM + androidTest compilation; emulator instrumentation is optional | owner-run Galaxy A55 visual/accessibility checklist; no emulator prerequisite |
| Room entity / DAO / migration | + `check_migrations`, `check_backup_fixtures`, `check_history_paging` | host checks + androidTest compilation; native migration instrumentation is optional | owner-run 0.14.0 update/migration/data-preservation checks using the artifact notes in `DEVICE-TEST-CHECKLIST.md`; stop on any install error and never uninstall |
| Backup / restore / serialization | + `check_backup_fixtures` | host fixtures + androidTest compilation; native restore instrumentation is optional | owner-run backup export/restore and legacy-format checks on isolated test data |
| Audio / recording / metronome | compile + `MetronomePcmTest`, `PlaybackEvidenceTest`, `DurableWavTest` | full JVM + lint + androidTest compilation; native instrumentation optional | owner-run Galaxy A55 sound routing and recording checks |
| Widgets | + `WidgetProjectionTest` | full JVM + androidTest compilation including `WidgetTest`; native bitmap/render assertion optional until executed | owner-run launcher/widget refresh matrix |
| Practice engine / timing | + `PracticeEngineTest`, `PracticeQueueTest` | full JVM + androidTest compilation; native instrumentation optional | owner-run controlled process interruption/restart integrity check |
| Activity rules / streaks / rewards | + `:activity-domain:jvmTest`, `ActivityDomainTest`, `WeeklyCosmeticRewardStateTest` | host Activity tests + androidTest compilation; native instrumentation optional | owner-run Activity checklist incl. BOTH goals, Freeze/reward edge cases and process restart |
| Onboarding / guided tour | + tour JVM tests, `check_localization` | Compose instrumentation compilation; execution is optional and reported separately | owner-run six-topic tour QA in EN/CZ, TalkBack, font scale, reduced motion |
| Localization (EN/CZ) | + `check_localization` | androidTest compilation; native instrumentation optional | owner-run locale/plural/date rendering QA |
| Dependencies / Gradle / build config | compile + full JVM | full JVM + lint + instrumentation **compilation** + smoke install | full clean gate + APK composition + signing + upgrade + CI re-verified at frozen SHA |
| `minifyEnabled` / R8 / resource shrinking | compile + full JVM (shrunk unit tests unaffected) | full JVM + instrumentation compile; emulator execution optional | owner-run core journeys on the production candidate + compatible upgrade + every artifact gate; **never claim device checks from compilation** |
| Docs / planning only | static checks (`check_agent_policy`, `check_localization`) | not applicable | not applicable |
| Publication workflows | `check_publication_workflows.py` + policy unit tests | wired into `quality.yml`; no publishing actions permitted | separate owner-approved manual publication only |
| Community draft archive | repository JVM round-trip/rejection/rollback tests | full JVM + lint + androidTest compilation | owner SAF picker and archive-handling QA |
| No change (re-verification) | reuse source-bound evidence | reuse | never rerun without invalidation reason |

## Latest evidence — 2026-11-15

- Catalogue instrumentation follow-up final source: FAST PASS at 21:58:42 UTC;
  INTEGRATION PASS at 21:59:25 UTC in 36 s. JVM suite: 329 passed, zero
  failures/errors/skips; debug lint and AndroidTest compilation passed.
  AndroidTest was not executed. Release Kotlin compilation and `lintRelease`
  passed in 40 s. A verified signed diagnostic APK was built afterward; details
  are in `CHECKPOINT.md`. The mandatory clean RELEASE script was not run
  because it invokes `gradlew clean`, prohibited by the owner’s preservation
  instruction. No A55 performance evidence exists. Full details are in
  `CATALOGUE-PERFORMANCE-INVESTIGATION.md` and `CHECKPOINT.md`.

- Current catalogue performance / Library tab source gate: RELEASE PASS at
  `2026-11-15T18:50:42Z` in 110 s on `a908c96-dirty`; 327 JVM tests passed,
  lint and all seven policy checks passed, and AndroidTest sources compiled.
  New Android benchmarks/layout checks were not executed. Host timings and
  remaining Galaxy A55 work are recorded in
  `CATALOGUE-PERFORMANCE-INVESTIGATION.md` and `CHECKPOINT.md`.

- RELEASE: `scripts/verify-release.sh` PASS at 2026-11-15 12:16:53 UTC,
  115 seconds; clean build, JVM tests (325 at gate time), Android-test
  compilation, lint, and seven policy checks. Audit row binds dirty source
  `a908c96` and source hash `902c1e13e1b468813ba364cc192d9ec093b08406f259c35dc9c7d826dfb31c42`.
- After the added provider-policy tests, focused checks passed and
  `:app:testDebugUnitTest` reports 327 tests, 0 failures/errors/skips; latest
  execution was up-to-date from Gradle. `:activity-domain:jvmTest` passed 13/13.
- Curated DB checker and APK checker passed: 100 composers, 105,032 works,
  105,421 credit relationships; DB 283,987,968 bytes, gzip 89,123,721 bytes.
  Migration v1–v13, backup fixtures 1–3, localization, static policy checks,
  `lintRelease`, Android-test compilation and `git diff --check` passed.
- Release APK is trusted-signed and non-debuggable; its exact hash, package,
  signature and inspected contents are in `CHECKPOINT.md`. The full dump and
  superseded payload are absent from the APK.
- One bounded direct MusicBrainz API request succeeded. It does not verify app
  runtime. Instrumentation and online/offline/detail/repertoire user journeys
  were not executed; no emulator or physical device was accessed. Stage 3 is
  incomplete pending those runtime checks. No publication occurred.


### Historical evidence — 2026-11-14

Current final INTEGRATION PASS at `2026-11-14T22:28:12Z` ran
`GRADLE_USER_HOME=/tmp/gould-015-gradle-home scripts/verify-integration.sh`
in 6 s (incremental): all 426 app JVM tests passed, Android lint passed, and
`assembleDebugAndroidTest` compiled successfully. This compiled (but did not
execute) the new catalogue installer, widget bitmap, guided-tour, and migration
instrumentation. Community archive tests ran in the JVM suite. Host validation
also passed: catalogue generator 8/8, publication workflow guard 9/9,
localization (1322 EN/CZ resources), migration DDL v1–v13, backup fixtures
formats 1–3, history paging, UI literals, agent policy and `git diff --check`.
The first sandbox startup failed before task execution because Gradle could not
determine wildcard IP; the approved execution path completed. Two source/test
defects found during retries were corrected before the final PASS. No emulator,
physical device, Android instrumentation execution, real catalogue input or
production generation/import is included in this evidence.
Exact audit identity: `a908c96-dirty`, source SHA-256
`f52eb5bf2adf54451394cbefbc8368f437d63dc82ba0454f12374dec8b332661`.

Earlier Stage 2 evidence (superseded by the current run above) changed catalogue generator validation,
`WidgetRenderer`, the Android widget test, and a KDoc in activity-domain.
INTEGRATION PASS at `2026-11-14T21:33:54Z` is bound to
`a908c96-dirty` / source SHA-256
`9c7cc47c263abd75b01d66af9e966f0c2016016589f847d0fcc0a69a313cd203`: full app
JVM suite, lint and Android-test compilation. The rendered-bitmap assertion was
compiled but not executed; no emulator render or physical-device acceptance is
claimed. `:activity-domain:jvmTest` passed from Gradle task cache. Catalogue
Python tests passed 7/7; localization parity, UI literal scan and
`git diff --check` passed. The initial sandbox Gradle invocation failed before
build startup; the serialized approved retry completed successfully.

The generator and importer remain unverified against real MusicBrainz data.
Snapshot signature/key authentication is still blocked; production generation
and import remain unauthorized pending the separate owner decision.

## Invalidation rules

- **Source invalidation:** any edit to files under `app/src/main` invalidates all
  tiers that compile or test those files; docs-only edits invalidate nothing.
- **Environment invalidation:** SDK/AGP/Kotlin/Gradle/JDK change, new AVD, or
  dependency refresh invalidates INTEGRATION and RELEASE even if source is unchanged.
- **Evidence identity:** retain the exact run commit and dirty-state annotation.
  New audit rows also bind application/build inputs and verification tooling by
  SHA-256. Reuse across docs/tooling commits requires matching relevant app/build
  hashes, unchanged environment and an explicit reuse reason; tooling edits get
  their own small checks. A different commit alone does not justify replaying
  accepted app tests.
- **Failed-gate rule:** a failed mandatory RELEASE gate blocks publication until
  resolved at the smallest reproducing scope; no blind full-suite retries.

## Tier commands

```bash
scripts/verify-fast.sh                     # FAST: compile + JVM + static checks (daemon, incremental)
scripts/verify-fast.sh --tests 'Pattern*'  # FAST: focused
scripts/verify-integration.sh              # INTEGRATION: full JVM + lint + androidTest compile
INSTRUMENTATION_CLASSES=com.opus.piano.DatabaseTest scripts/verify-integration.sh   # + native
scripts/verify-release.sh                  # RELEASE: exact policy clean gate (mandatory host command)
```

RELEASE-only artifact/signing/upgrade/security gates keep their existing
documented scripts (`verify_quality_apk.py`, `verify-upgrade-signing.sh`,
`verify_emulator_upgrade.py`) and are executed by the Stage 3 release runbook,
not duplicated here. The emulator upgrade script is an optional CI/engineering
tool, not the owner's acceptance path. The owner performs real-device checks;
agents do not access the owner's phone. See `DEVICE-TEST-CHECKLIST.md`.

## Physical-device acceptance policy

The owner tests on a real device, primarily Samsung Galaxy A55. Missing
emulator binaries, AVDs or `/dev/kvm` never block Gould acceptance. When native
instrumentation is unavailable, record it as **not executed**; do not claim a
pass from APK/test compilation. The owner-run checklist is
`DEVICE-TEST-CHECKLIST.md`. The Stage 2 test APK uses the release variant and
the existing trusted signer. Verify its actual metadata, signature and
debuggable flag before owner testing; it is a testing build, not a public
release. The owner may test the update after exporting a backup, but no
in-place update result is claimed. Never uninstall or clear the published
installation to work around any install error.

## Host resource discipline

All tier scripts hold one shared host lock via `scripts/gradle-lock.sh`; Gradle
calls use `--max-workers=1 --no-parallel`. Never start another heavyweight
Gradle operation while one runs (a second attempt exits 75 by design). Finish
the build before any optional emulator run. For an invalidated local unsigned
R8 experiment only: `scripts/build-optimized-local.sh` (bounded, offline,
refuses a running emulator; not full RELEASE evidence).

Stage 2 implementation closeout used the INTEGRATION tier, focused host checks,
and an owner-testing Release-variant build plus `lintRelease`. The complete
`scripts/verify-release.sh` source-freeze gate and publication/upgrade
acceptance were not run; those remain separate Stage 3 gates. No publication,
main merge or promotion occurred.
