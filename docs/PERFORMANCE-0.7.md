# Performance diagnostics — 0.7.0

No device runtime performance numbers are available in this environment. No startup, frame-rate or percentage improvement is claimed.

## Reproducible startup comparison

Use a dedicated Android test device containing a representative journal and the published 0.6.1 APK. Export a backup first. Do not uninstall or clear app storage. Ensure the candidate has the **same signing certificate**, then use `adb install -r` to update.

1. Record device model, Android version, animation scale, battery/thermal state and journal size.
2. Run `scripts/measure-startup.sh 10` for 0.6.1 and save stdout.
3. Run `scripts/verify-upgrade-signing.sh baseline.apk candidate.apk`. Stop on failure.
4. Update to 0.7.0 with `adb install -r candidate.apk`, keeping the journal intact.
5. Run the same ten process-cold starts and compare median/range of `TotalTime`. This is process-cold start, not a guaranteed storage-cold start.
6. Capture Android Studio System Traces while opening Repertoire, Profile and Badges with the same journal. Repeat with airplane mode. Examine `Journal.summary` sections, main-thread work, frame deadlines and first/fully drawn startup events. `reportFullyDrawn()` is emitted after the first saved library snapshot is available. It is a data-readiness signal, not proof that optional external content has loaded.
7. Record results with the APK checksums. Keep build duration separate from app runtime performance.

Do not compare different devices or quote a single best run. Re-test with at least 10,000 sessions and a large repertoire. Run a sustained practice session and check battery use, background pause/resume and memory after repeatedly opening composer portraits.

## Implementation changes to measure

- Initialization retains lazy application repositories and does not start network discovery at launch. Existing badge verification is deferred 750 ms, with coroutine cancellation tied to its ViewModel.
- One eagerly observed Room journal snapshot preloads local data for Home, Repertoire and Profile. Reads are transactional; unchanged snapshots are removed before deriving summaries. Summaries run on the default dispatcher, are shared across screens, and retain only the latest snapshot. Repertoire reuses attribution totals and last-practice dates.
- Catalogue cache hits no longer replay already-ingested metadata or reevaluate badges. Failed optional refreshes retain saved data and report it as cached. Connectivity retries run only while the app is started and a visible catalogue page has an error.
- Portraits decode at reduced size, use a 32 MiB disk budget and update recency on access. Response copies use a 16 MiB budget, evict oldest entries, and preserve normalized catalogue rows. Only active screen portrait state owns decoded images; no new unbounded bitmap cache is introduced.
- Skeletons wait 180 ms and do not animate. Lists retain stable record IDs. Typography objects are reused until the preference changes.

## Baseline

Frozen source `v0.6.1` (`34821ea071734b1f1571b116b2e656e64c1a8587`) was independently built under `/tmp/goldberg061-source`. Required build/test/lint tasks succeeded; 29 JVM tests passed and lint had zero errors/12 warnings. Its full first build/check run took 5m 8s on this host; this is a toolchain observation, not app startup timing. Parallel Gradle work and swapping make this unsuitable for build-speed comparisons.
