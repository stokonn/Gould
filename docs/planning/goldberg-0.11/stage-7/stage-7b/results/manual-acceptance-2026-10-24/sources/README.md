# Archived opt-in manual instrumentation

The source and exact Gradle patch are retained byte-for-byte with SHA256 in
archive-receipt.json. They produced the October24 emulator observations. They are
**not active application/test/CI sources** after final closure. The normal build
uses its original AndroidJUnitRunner and exactly matches the CI25 source manifest.

For separately authorized reproduction on an isolated emulator, use a temporary
checkout: copy Stage7bManualProbe.kt to its original androidTest path recorded in
the receipt, apply manual-probe-build.patch, then build with
`./gradlew assembleDebugAndroidTest -PgoldbergManualProbe=true --offline --no-daemon --max-workers=1`.
Verify explicit emulator-NNNN and ro.kernel.qemu=1 before installing/running.
The component is com.opus.piano.test/com.opus.piano.Stage7bManualProbe;
-e goldbergManual interactive or revoked explicitly selects the phase.
Never apply the probe automatically in standard CI or enable live Admin transport.

Original manual APK and raw archives remain ignored under .gradle/manual-acceptance-artifacts/.
No source, screenshot, log, raw observation, historical artifact or private PMP
was discarded by relocating the recorder out of the active test source set.

created by stokonn with love <3
