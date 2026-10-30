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
- [ ] T5 Release APK composition inspected; size reduced with supported build techniques; measured after each change (target <15 MB, stretch 10 MB).
- [ ] T6 Handoff/documentation simplified: one authoritative current-state doc, AGENTS.md documents three tiers, Issue/Project/PMP kept consistent.
- [ ] T7 Integration verification executed per matrix for all changed areas.
- [ ] T8 Final release verification prepared and executed against frozen 0.13 source; report produced.

### Verification (actual results)

_Updated as runs execute; each line mirrors `verification-audit.jsonl`._

- FAST PASS 126 s cold baseline @ `d10aa49-dirty` — compile + 48 JVM suites + 4 static checks (localization 1211 resources, migrations v1→v11, backup formats 1–3, history paging 106 sessions).
- FAST PASS 6 s warm after `gradle.properties` (caching + parallel + configuration-cache): compile 2 s, JVM 3 s, static <1 s. Same checks, daemon-reused, no clean build.
- APK composition inspected: published 0.12 APK SHA `14f4ef28…` = 18,757,232 B; 50.7 MB of 57 MB uncompressed payload is 3 unminified DEX files; release build has **no buildTypes/minify**; fonts are variable TTFs (already compact); `quote_nature.png` lossless recompression headroom only 5 KB.

### Completion status

**IN PROGRESS** — T1–T3 complete and committed. T4–T8 pending. No stage may be
marked complete until its verification has actually passed.
