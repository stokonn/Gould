# Gould 0.13 — Stage Checklist (living)

Single development stage, per permanent two-phase Gould workflow. This file is
updated continuously as work progresses; unchecked boxes are never implied done.

## Stage: Project Renaissance — development velocity, performance, APK size

**Purpose:** reduce development/verification overhead and release APK size
without weakening release safety; profile before optimizing; keep a truthful
audit trail of executed checks.

**Scope:** verification tooling (`scripts/verify-*.sh`, audit trail), Gradle
build performance, application hotspots (startup, recomposition, Room, audio),
release APK packaging, agent handoff documentation. No feature redesign, no
schema change unless strictly necessary, no backend expansion.

**Baseline:** `d10aa49` (Gould 0.12 Stable, tag `v0.12`), release APK
18,757,232 bytes, SHA-256 `14f4ef28e13901f0dbbf54510fb9b5bd668e35b0a409e9e14d057bae00d932e8`.

### Tasks

- [x] T1 Environment audit: main/tags/releases/worktree state verified; untracked owner files and PMP preserved; `feature/gould-0.13` worktree created from `d10aa49`.
- [x] T2 Three-tier verification workflow implemented (FAST / INTEGRATION / RELEASE scripts + decision matrix + audit trail).
- [x] T3 Developer build/CI speed measured and improved (Gradle daemon/caching; before/after timings recorded).
- [ ] T4 Application hotspots profiled (startup, recomposition, Room, audio, memory); small measurable optimizations applied where justified.
- [x] T5 Release APK composition inspected; local optimized artifact and focused runtime accepted: 6,492,506 B unsigned / 6,532,669 B test-signed, both below 10 MB. Final release gates remain T8.
- [ ] T6 Handoff/documentation simplified: one authoritative current-state doc, AGENTS.md documents three tiers, Issue/Project/PMP kept consistent.
- [x] T7 R8 boundary acceptance: reuse accepted host JVM/lint/instrumentation compilation plus actual optimized-APK startup/Room/WorkManager/widgets/Studio recording/playback/persistence journeys. No androidTest-runner execution or final release gate inferred.
- [ ] T8 Final release verification prepared and executed against frozen 0.13 source; report produced.

### Verification (actual results)

_Updated as runs execute; each line mirrors `verification-audit.jsonl`._

- FAST PASS 126 s cold baseline @ `d10aa49-dirty` — compile + 48 JVM suites + 4 static checks (localization 1211 resources, migrations v1→v11, backup formats 1–3, history paging 106 sessions).
- FAST PASS 6 s warm after `gradle.properties` (caching + parallel + configuration-cache): compile 2 s, JVM 3 s, static <1 s. Same checks, daemon-reused, no clean build. **Unchanged sources: this does not measure a real source-change rebuild.**
- APK composition inspected: published 0.12 APK SHA `14f4ef28…` = 18,757,232 B; 50.7 MB of 57 MB uncompressed payload is 3 unminified DEX files; published 0.12 release configuration had **no buildTypes/minify**; fonts are variable TTFs (already compact); `quote_nature.png` lossless recompression headroom only 5 KB.

### Completion status

**IN PROGRESS** — T1–T3 were committed previously; T5/T7 local R8 milestone verified. T4 profiling and T6 tracking/handoff remain in progress; T8 final-source release verification is pending. See [current checkpoint](CHECKPOINT.md). No final release acceptance is implied.

### R8 continuation — October 30, 2026

Owner explicitly requested a local unsigned/test-signed optimized release
candidate. This permits local release-variant testing; it does not authorize
production signing/publication, Stable changes or main integration.

- [x] Inspect `7a132f1` worktree: preserve modified `app/build.gradle.kts` and untracked `app/proguard-rules.pro`; no other initial changes.
- [x] Review R8/resource shrinking configuration and existing verification matrix.
- [x] FAST after build-config invalidation: PASS 29 s on host. Initial sandbox attempt failed before compilation (Gradle local socket restriction); preserved in audit, not an application failure.
- [x] Isolated unsigned optimized release build PASS: 246 s Gradle / 247.29 s wall, 38 tasks up-to-date and 9 executed; mapping/resources/source manifest retained.
- [x] ZIP-validated unsigned APK 6,492,506 B, 65.3867% below verified 18,757,232 B baseline. Test-signed copy 6,532,669 B; every payload entry identical. Runtime acceptance remains pending.
- [x] Exact optimized payload accepted on disposable API 36: Room persistence, actual WorkManager worker SUCCESS, both live launcher widgets and safe taps, microphone recording, WAV/hash validation, playback and recording retained through process/emulator restart.
- [x] Host INTEGRATION PASS 3930 s: JVM task outputs reused, lint and instrumentation APK compilation passed. No native execution. Accepted evidence retained; do not rerun without invalidation.
- [ ] Profile focused runtime flows before considering application changes.
- [x] Consolidated source-bound artifact/runtime/performance reports, inspected screenshots, [current checkpoint](CHECKPOINT.md), shared host locking and exact source/tool audit fingerprints.

Read-only GitHub inspection found repository `stokonn/Gould`, Stable `v0.12`,
no open issues and no 0.13 milestone/issue (including closed issue search).
Remote tracking synchronization is pending; no completion is inferred from
local checklist entries. Private PMP remains untouched per owner instruction.

### Isolated R8 recovery — October 30, 2026

- Previous boot `93ded8c48f664bd8b1b3de76724583b7`: kernel explicitly reports
  OOM killing Java PID **1016964** at 19:49:29, matching the failed release
  daemon. The cause is now confirmed by previous-boot records, rather than
  inferred from current-boot logs. Raw matching records are preserved under
  `.gradle/renaissance/r8-isolated/previous-boot-oom.txt`.
- Current-boot preflight: about 4.1 GiB available RAM, zero used swap, no Gradle
  build/compiler daemon or emulator running. No unrelated process stopped.
- Retry only `:app:assembleRelease`, with `--max-workers=1 --no-parallel
  --no-daemon --offline`, 2560 MiB heap and `-XX:ActiveProcessorCount=2`.
  Configuration cache retained; no clean, dependency refresh or test rerun.
- All 238 app/schema/build hashes match the pre-failure source manifest.
  Runtime/emulator work waits until this build exits.

- Isolated R8 artifact SHA-256:
  `2cfbd10d13bfa2a2c366f4c6176dadf65d05dbae2dd7b5e418d1a58faa43cdda`.
  Test-only signed copy SHA-256:
  `bae4a44516b0199a71612b2a13b8c9a640cebbf374cec8ce2fceab7c9f3636a5`.
  DEX uncompressed 50,747,224 → 4,725,240 B (3 → 1 files), resource table
  1,453,692 → 957,264 B. Generated R8 configuration includes dependency-provided
  Room database and WorkManager worker name/constructor keep rules.
- The inherited checkpoint still identifies the local APK as `0.12-dev`, code14;
  this is a local 0.13 development experiment, not final 0.13 release metadata.
- New emulator `gould013-r8-isolated`, explicit `emulator-5592`, API 36/QEMU=1,
  software `swangle`, two cores. Emulator enforces 2560 MiB guest RAM despite
  the requested1536 MiB; no Gradle overlaps. First install before boot readiness
  was refused by Android; only post-boot successful installation is evidence.

### Focused optimized acceptance and profiling

- [x] Exact installed APK SHA matches test-signed copy; all 414 unsigned payload entries identical after signing. Production keys untouched.
- [x] Actual optimized startup, Room v11 integrity/FK/28-table readback, WorkManager success (logs and WorkSpec), both real launcher widgets and idle Studio navigation.
- [x] Actual 12.4459 s WAV capture/finalization; playback advances with active AudioFlinger track; WAV hash/sample metadata matches database; saved take survives process and emulator restart.
- [x] Profiled one Studio→Previous→Recordings→playback flow: 698 CPU samples / 0 lost; 72.36% self samples RenderThread,44.27% emulator goldfish pipe. No speculative app rewrite.
- [x] Exploratory 10+10 startup and one PSS snapshot per side recorded with software-renderer/run-order caveats; not release-performance evidence.
- [x] Workflow locking, bounded local unsigned R8 command and single-worker/nonparallel flags; contention/exit/reuse/daemon-descriptor checks, shell syntax and agent policy pass without Android-suite repeats.
- [ ] Dedicated Compose recomposition, Room-query and recording-CPU profiles; no app-owned bottleneck demonstrated by the current framework-dominated trace.
- [ ] Final 0.13 metadata/source freeze and mandatory release-only acceptance; no Stable/main authorization.

Detailed evidence and limitations: [current checkpoint](CHECKPOINT.md),
[artifact/runtime report](verification/R8-ACCEPTANCE.json),
[performance report](verification/PERFORMANCE.json). Original private/raw files
remain preserved; only synthetic screenshots and compact summaries are committed.

created by stokonn with love <3
