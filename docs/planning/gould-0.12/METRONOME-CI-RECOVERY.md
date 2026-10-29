# Metronome CI scheduling recovery

Base feature/f62e41c; Phase2 remains In Progress. FullAPI36 run40937907162
failed269cases/255PASS/1failure/0errors/13explicit skips. Sole failure was
StudioControlsTest.nativeMetronomeStartsAndStopsWithoutCreatingPractice,
TimeoutCancellationException5000ms. Acceptance subset passed. Discover journey
and exact evidence assertions passed; no Discover source change is needed.

Raw log/artifacts retained privately under .gradle/gould012-checks/ci-40937907162.
Instrumentation artifact12927830345, API digest
sha256:2a3af8b5ead66c0852c232f859724cc55cdadb3e1a9c78bfc8f0b60ac4dba154.
At19:32:35.251–.289, media focus/track allocations were repeatedly acquired and
abandoned; no competing-focus request or metronome exception followed before
the timeout19:32:40.278. Coroutine stack does not identify the exact predicate.

Unchanged isolated original native test PASS1/1,0.525s on preserved disposable
API36 gould012-pristine/emulator5590/qemu1. This does not disprove a scheduling race.

Controlled dispatcher regression executes newest queued start before the older
start: actual AudioTrack starts, then old start tears it down. Deterministic
pre-fix FAIL1/1,0.108s: Superseded start must retain latest playback. Internal
dispatcher constructor is a test seam; public constructor still selects IO.

Minimal correction checks generation under the mutex BEFORE start cleanup;
queued stop cleanup also requires its generation still owns the request. Atomic
StateFlow updates preserve newer configuration fields during cleanup. Existing
media attributes, focus requests, explicit start/stop, route/lifecycle controls,
PCM and every5000ms assertion remain unchanged. Regression also covers stale
stop cleanup after an explicit restart. Corrected host PASS3m53s:393JVM/48suites/0 failures/errors/skips, debug/testAPK, lint0errors/165warnings/1hint. API36 StudioControls6/6 PASS3.773s; API26 affected2/2 PASS0.298s. API26 all28 tables/37sessions/1draft/8WAV hash comparisons exact, integrity/FK PASS. Trusted debugAPK SHA256 f62854078215bc2d9434b268d349658cee65f7968ea7f69e9ddecc1314cdcf30; package0.12-dev/code14, ZIP/artwork/mask and signer verified against published v0.11. Corrected full native CI pending.

Owner GalaxyA55 media/Tux/Library acceptance at02c5298 is synchronized from
[comment6505538907](https://github.com/stokonn/Gould/issues/41#issuecomment-6505538907).
Category: owner-reported physical-device manual PASS, never agent-operated.
No further phone/visual correction retest is required without relevant changes.

All original protected profile/API26 data, historical failed runs and private
references retained. No unchanged fullsuite retry, timeout increase, main/tag/release.

created by stokonn with love <3
