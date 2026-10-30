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
`opus.db`, and production signing configuration were not changed.

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
No speculative application optimization was applied. Dedicated recomposition,
Room-query and recording-CPU profiling remain open.

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
   stable-property `0.13`, versionCode `15`. Freeze final source before mandatory RELEASE
   verification. No final clean gate, full native/visual matrix, trusted signer
   upgrade/security/licensing release acceptance or new-commit CI is implied.
4. Commit/push only verified development milestones on `feature/gould-0.13`.
   No main integration, Stable tag or publication without new owner authorization.
   No production signing keys, owner's phone, protected data or private PMP.

created by stokonn with love <3
