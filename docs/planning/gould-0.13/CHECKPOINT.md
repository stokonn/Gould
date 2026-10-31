# Gould 0.13 — current development checkpoint

Project Renaissance is **in progress** on `feature/gould-0.13`, based on
Gould 0.12 Stable `d10aa49`. This is the authoritative resume entry point.
The [living checklist](STAGE-CHECKLIST.md) retains task history; the
[verification matrix](VERIFICATION-MATRIX.md) defines invalidation.

## Current result

R8 code optimization and resource shrinking are verified for a **local,
non-debuggable, test-signed development artifact**. The original in-progress
`app/build.gradle.kts` and `app/proguard-rules.pro` contents were preserved.
Application Kotlin/resources, schema v11, package `com.opus.piano`, database
`opus.db`, and production signing configuration were not changed. Source
metadata is now versionName `0.13-dev` / versionCode `15`; the artifact table
below intentionally describes the earlier accepted local R8 experiment
(`0.12-dev` / code14), not a final 0.13 artifact.

| Artifact | Exact bytes | SHA-256 |
| --- | ---: | --- |
| Published 0.12 baseline | 18,757,232 | `14f4ef28e13901f0dbbf54510fb9b5bd668e35b0a409e9e14d057bae00d932e8` |
| Optimized unsigned | 6,492,506 | `2cfbd10d13bfa2a2c366f4c6176dadf65d05dbae2dd7b5e418d1a58faa43cdda` |
| Optimized local test signer | 6,532,669 | `bae4a44516b0199a71612b2a13b8c9a640cebbf374cec8ce2fceab7c9f3636a5` |

Unsigned reduction: **12,264,726 bytes / 65.3867%**. Signing adds overhead;
every payload entry in the test-signed copy matches the unsigned artifact.
APK metadata remains the inherited `0.12-dev`, code14: this is not final 0.13
version metadata or a public RC. The test certificate is separate from production.

See [artifact/runtime evidence](verification/R8-ACCEPTANCE.json),
[exploratory performance evidence](verification/PERFORMANCE.json), and inspected
[recording](verification/r8-recording.png), [playback](verification/r8-playback.png)
and [launcher widgets](verification/r8-widgets.png) captures.

## Build recovery and accepted verification

- Previous boot `93ded8c48f664bd8b1b3de76724583b7` explicitly records kernel OOM
  killing failed release daemon PID 1016964 at 19:49:29. Earlier speculation is
  superseded by this matching-PID evidence. The original failed build log is kept.
- Isolated `:app:assembleRelease` PASS **246 s Gradle / 247.29 s wall**; 9 tasks
  executed, 38 up-to-date. One worker, no parallel tasks, no daemon retained,
  offline caches, 2560 MiB heap, two JVM-visible CPUs. No clean or concurrent
  build/emulator. R8 mapping/usage/resource reports confirm optimization ran.
- Reuse FAST **29 s PASS**, host INTEGRATION **3930 s PASS** and instrumentation
  APK compilation. No FAST/INTEGRATION rerun: all 238 saved app/schema/build hashes
  still match. The earlier 6 s result was warm **unchanged-source** reuse, not a
  source-change rebuild benchmark.
- Focused acceptance used the optimized payload on one disposable API 36 emulator,
  `gould013-r8-isolated` / explicit `emulator-5592`, QEMU=1, two cores, swangle.
  It enforces 2560 MiB guest RAM. Startup A/B used 1080×2400/density420; functional
  journeys used 540×1200/density210 for cheaper software rendering.
- Test-signed baseline→optimized in-place install, startup, Room initialization,
  both live launcher widgets and safe Studio navigation, production WorkManager
  worker SUCCESS, explicit microphone consent/permissions, actual recording,
  WAV finalization and playback with advancing position/active audio track passed.
- Readback: schema 11/all 28 tables present, integrity `ok`, no FK violations;
  23/27 non-SQLite tables equal baseline canonical rows. Four previously empty
  tables gained expected recording/association/achievement/app-state rows.
  One 12.4459 s mono 22050 Hz/16-bit WAV matches its stored SHA and sample count.
  Saved recording survived process **and emulator restart**; installed APK SHA
  matches the test-signed artifact. The disposable AVD was stopped and retained.
- Android test-runner instrumentation was **not executed**. These are actual
  optimized-APK black-box journeys, not substituted debug-test evidence.
  Acoustic output is unverified: emulator host audio driver was unavailable.

## Profiling and tooling progress

Ten sequential onboarding cold-process samples each: baseline median 6576.5 ms,
optimized 4814 ms. Idle PSS snapshots 55982→38852 KiB. These are exploratory:
software rendering, host contention, run-order bias and one PSS sample prevent a
production performance/leak claim.

A focused Studio→Previous→Recordings→playback CPU profile recorded 698 samples,
zero lost, about 20 s/3.49 sampled CPU seconds. RenderThread accounts for 72.36%
of self samples; emulator `goldfish_pipe_read_write` alone 44.27%. Frame stats:
133 frames, p50/p90/p95=40/150/200 ms under software rendering. This identifies
emulator render transport overhead, not an app-owned function to rewrite.
No speculative application optimization was applied. The profiling task is now
closed with negative evidence: the trace is framework/emulator dominated, while
the bounded Room query has direct result-count evidence and an 8/8 API 36
instrumentation regression.

All tier scripts now serialize through a shared host lock across worktrees and
pass `--max-workers=1 --no-parallel`. Lock contention, exit propagation, reuse,
and detached-child descriptor isolation passed lightweight checks. Shell syntax
and canonical agent-policy checks pass. No Android suite was rerun for tooling.
Future audit entries include app/build-source and verification-tool fingerprints;
docs-only commits no longer need an expensive app rebuild to establish identity.

For a future **invalidated** local artifact build, stop the emulator and use:

```bash
scripts/build-optimized-local.sh
```

This bounded unsigned command retains fresh logs and uses the successful memory
limits. It never signs, installs, tags, cleans, publishes, or runs full QA. It has
shell/locking checks, not a second end-to-end Gradle execution; the underlying
Gradle command above is the actually executed build evidence.

Raw logs, source manifest, mappings, profiles and disposable synthetic snapshots
remain in `.gradle/renaissance/` and `app/build/outputs/mapping/release/`.
Test keystore/raw app data are ignored and must never be committed or published.

## October 30 continuation

The paged recordings UI now loads association previews, linked practice
sessions and original-file availability in batches instead of per-row. The
development metadata is prepared as versionName `0.13-dev` (stable property
`0.13`) and versionCode `15`. Targeted FAST passed in 85 seconds with
`compileDebugKotlin`, `*Recording*Test` JVM tests and all four static checks.

The association query now caps results in SQL at six rows per recording using
only correlated subqueries supported by Android API 26; it no longer loads all
association rows before Kotlin truncation. A synthetic SQLite run with 30
recordings x 100 associations returned 3,000 rows / 3.17 ms median before the
cap and 180 rows / 7.51 ms median after it. The bounded query trades a few
milliseconds of CPU for 16.7x lower result materialization; no emulator was
available for Room instrumentation execution.

## October 31 continuation

The existing Stage 5 emulator workflow had no focused recording scope. It now
supports `verification_scope=recordings`, which builds the debug and
instrumentation APKs and executes only `com.opus.piano.StudioRecordingTest` on
the disposable API 36/KVM emulator; the acceptance job is skipped. This is the
least expensive safe native path and does not change automatic triggers.

Recording correctness evidence is bounded as follows: capture-state changes
invalidate the page through `captureKey`; recovery and confirmed deletion call
`reload`; manual retry calls the same reload path; reload clears both visible
rows and cached details before querying. Existing repository tests cover
retained recovery bytes, explicit deletion tombstones/events, active-state
export blocking and paged visibility. The new Android regression additionally
checks six-row-per-recording association bounds.

See the concise [release-readiness checklist](RELEASE-READINESS.md). The
focused instrumentation gate passed on exact commit `e812b7c`: CI run
[41146502208](https://github.com/stokonn/Gould/actions/runs/41146502208)
executed `StudioRecordingTest` 8/8 with zero failures, errors or skips on
API 36/QEMU emulator `emulator-5554` at 1080x2400/density 420. The unrelated
acceptance job was skipped. This closes the focused bounded-association native
regression gate; it does not close final RELEASE acceptance.

## Final host verification — October 31

The source/docs freeze at `14882d3` passed the mandatory non-production
RELEASE host gate in 248 seconds: clean `assembleDebug`,
`testDebugUnitTest`, `assembleDebugAndroidTest`, `lintDebug`, and all five
policy checks. The audit row records `RELEASE PASS` with 238 source files.
No production signing, release APK, tag, publication or main integration was
performed.

No app/build source changed between focused CI commit `e812b7c` and the
documentation/audit-only freeze commits. The focused native evidence is
therefore retained for the unchanged app source, but final-SHA CI is still
open under the release checklist at this checkpoint; it is closed by the
final-SHA run recorded below.

## Final-SHA and artifact preflight — October 31

Workflow run
[41149284775](https://github.com/stokonn/Gould/actions/runs/41149284775)
passed at exact commit `6dbd2b8`. The full instrumentation job recorded 271
tests with zero failures and zero errors; 13 tests were skipped. The acceptance
job passed the seeded CI-debug in-place migration/data-preservation sequence,
Data Saver transitions, process restart, and post-restart Room integrity,
foreign-key and schema checks. Its certificate pairing is only for the two
same-run CI debug rebuilds, not production signing evidence.

The final stable-metadata unsigned R8 build passed in 289 seconds. The artifact
is `app/build/outputs/apk/release/app-release-unsigned.apk`, package
`com.opus.piano`, versionName `0.13`, versionCode `15`, minSdk 26 and targetSdk
36. ZIP integrity passed and SHA-256 is
`9bfb5a3e166bfcbab1069265eff3e46bf296e49af84c8ce0df558dcf26086c2f`.
The local test-signed copy passed `verify_quality_apk.py` against the published
0.12 APK for metadata progression, trusted-fingerprint preflight, ZIP integrity
and packaged launcher artwork bounds. This does not prove production-key
compatibility or a published signed upgrade.

Host migration, backup, paging, localization and policy checks also pass.
Manifest permissions, bundled legal/privacy assets and the declared dependency
surface were inspected. Focused API 36 visual/accessibility smoke passed for
English/Czech, dark appearance/settings/profile screens and 200% font scaling.

## Trusted release build and upgrade blocker — October 31

The clean build using the preserved trusted signing init completed successfully
in 284 seconds:

```text
./gradlew -I /home/stokonn/Projects/Opus/docs/releases/goldberg-0.11-stable-signing.init.gradle \
  -PgoldbergStable=true clean assembleRelease lintRelease --no-daemon \
  --max-workers=1 --no-parallel --offline
```

The resulting non-debuggable APK is
`app/build/outputs/apk/release/app-release.apk`, package `com.opus.piano`,
versionName `0.13`, versionCode `15`, certificate SHA-256
`53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`, and
SHA-256
`5325fb6c37c9f8acf977dfbb7d2dd00c8e602cddca3f40384a62d467c739bfbd`.
`verify_quality_apk.py` and `verify-upgrade-signing.sh` both pass against the
published Gould 0.12 APK; ZIP integrity, package metadata and launcher
artwork checks pass.

The authentic trusted upgrade subsequently passed on disposable API 36
`emulator-5574` using the published 0.12 APK and this exact 0.13 APK without
uninstalling between versions. The verifier retained 37 sessions, profile,
repertoire, settings, recovery archive, unfinished draft and badge history;
Room schema 11, SQLite integrity and foreign-key checks passed, and no
recording or achievement-event rows were manufactured. Its synthetic fixture
seeds historical badge notices as acknowledged; no production code changed.

## Remaining work and boundaries

1. The recordings page now batches association/session metadata and filesystem
   availability checks per fetched page, replacing the former per-row N+1
   pattern. Focused Compose/audio traces remain framework- or emulator-dominated;
   no further application rewrite is justified without new app-owned evidence.
2. Keep the checklist/audit current. No 0.13 issue/milestone exists remotely;
   Issue/Project synchronization remains pending. Private PMP stays untouched by
   explicit owner instruction. Historical handoff entries remain in
   `docs/ai/HANDOFF-HISTORY.md`.
3. Development metadata is now prepared as versionName `0.13-dev` /
   stable-property `0.13`, versionCode `15`. The source/docs freeze, RELEASE
   host gate, final-SHA CI, trusted artifact, authentic upgrade, focused
   visual/accessibility smoke and artifact-specific security/privacy/licensing
   checks are PASS.
4. Release completion: documentation commit `c43b32a` was pushed, integrated
   into `main` at `f9ba9b7`, and tagged `v0.13`. GitHub release
   [Gould 0.13 — Project Renaissance](https://github.com/stokonn/Gould/releases/tag/v0.13)
   is published as a pre-release, not Stable or Latest Stable. Its downloaded
   APK and checksum match the trusted artifact; no owner's phone, protected
   data or private PMP was touched.

created by stokonn with love <3
