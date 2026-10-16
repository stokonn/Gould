# Goldberg 0.8 performance validation

No physical-device runtime, battery, CPU or thermal results have been measured for this candidate. Do not claim screen-off reliability or performance percentages from build duration or synthetic unit tests.

Capture uses bounded PCM/FFT buffers, no raw-audio storage, no background network processing and no application wake lock. UI updates are throttled; notification updates are state-driven/coalesced. Drafts checkpoint at transitions/roughly five seconds, with debounced note edits, without invalidating the journal summary. Idle/paused practice does not poll the clock. Timing intervals merge adjacent equal states. Existing complete journal/catalogue snapshots remain a scaling limitation.

On representative API 26–36 phones, record matched 30–60 minute manual versus Smart sessions with identical brightness/screen-off conditions, network state, device thermal/battery condition and piano placement. Include sustained playing, long silence and auto-resume while locked. Capture CPU time, resident/Java/native memory, thermal state, battery change, capture continuity and checkpoint gaps with Android Studio/Perfetto. Repeat with vendor battery restrictions and do not bypass them. Check 10,000-session journals, rapid UI reconnection and repeated microphone/harpsichord cleanup.

An Android foreground service is not a CPU wake-lock guarantee. If capture/analysis stalls, unknown intervals must remain uncredited. Any proposed application wake lock requires documented measured need and a bounded lifecycle; none is currently declared. Confirm shutdown latency and microphone indicators on Pause, Stop, completion, calls, privacy changes and process termination.
