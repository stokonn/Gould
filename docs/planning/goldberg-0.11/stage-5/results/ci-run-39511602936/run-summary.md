# Stage 5 CI run 39511602936 — third dispatch (2026-10-20T22:56–23:06Z)

**Classification: FIRST RUN TO EXECUTE REAL APPLICATION TESTS.** The CI pipeline now
works end-to-end; the remaining failures are genuine Stage 5 application/test findings,
not CI plumbing. **Stage 5 is NOT complete — 13 instrumentation failures + 1
acceptance-script assertion failure remain open.**

| Property | Value |
| --- | --- |
| Run | https://github.com/stokonn/Goldberg/actions/runs/39511602936 |
| Dispatched commit | `9b80d65a12e0fd8096c8e5a4db4fe5021ead602f` (branch `feature/goldberg-0.11-stage-5`, headSha verified) |
| Created / updated | 2026-10-20T22:56:06Z / 2026-10-20T23:06:32Z |
| Job 5.10 | failure — instrumentation suite **ran**: 177 tests, **13 failures**, 0 errors, 2 skipped |
| Job 5.11 | failure — upgrade validation **passed**, final offline-state assertion failed |
| Raw logs | `run-logs.zip` (SHA-256 `6e93fdd4b6cf03154117511ece4f8255a60be7d249b382e348ca486ea491ba11`) |
| Artifacts | `artifacts/` (253 files: XML/pb results, per-test logcats, HTML reports, logcat.txt, upgrade DBs/JSONs, signing evidence) |

## What this run PROVED working (CI pipeline end-to-end)

- All prior fixes verified: SHA binding, builds, signing diagnostics, **and the
  attempt-3 execution-model fix** — materialize steps succeeded, both scripts ran as
  single bash processes on booted emulators (`Running … on verified emulator
  emulator-5554`; boot ≈43 s).
- `./gradlew connectedDebugAndroidTest` executed the full connected suite on the API 36
  emulator: **177 tests ran (162 passed, 13 failed, 2 skipped)**; all Stage 1–4 suites
  passed on-device; only Stage 5 bundle classes failed.
- Job 5.11 upgrade path executed far past all previous attempts: v0.10.1 baseline
  debug-rebuild installed, seed DB + appearance XML pushed, baseline launched, **baseline
  schema verified, per-table data captured, candidate installed and launched, prefs
  unchanged, persistent data equality (`before == after`) passed, `PRAGMA user_version`
  = expected schema, `foreign_key_check` clean, UUID-v4 identity bridge verified** —
  i.e. **the seeded in-place upgrade from v0.10.1 with full data preservation PASSED on
  the emulator**. Only the final offline-state check failed (below).

## Job 5.10 — 13 failures (all `code -2` assertion failures; 0 errors)

Counts source: `instrumentation-counts.txt` + `testlog/test-results.log` (parsed
per-test status codes). Full stacks: emulator-step log and per-test logcat artifacts.

**Stage5BundleJobRunnerTest (6):**
1. `concurrentBundlesSharingOneDependencyKeepTheSharedContentSingle`
2. `interruptedDownloadLeavesRetryableStateAndCompletesOnRetry`
3. `interruptedRefreshKeepsTheOldReadyRevisionAndPublishesWhenComplete`
4. `corruptSourceContentNeverReachesReady`
5. `corruptStoredBlobIsQuarantinedAndRepairedOnRetry`
6. `consentRevocationMidStreamPausesAndKeepsReadyContent`

**Stage5BundleStoreTest (2):**
7. `interruptedRefreshKeepsPreviousReadyRevisionUntilAtomicPromote`
8. `storeStateSurvivesReopenLikeAProcessRestart`

**Stage5BundleWorkerTest (5):**
9. `queuedBundleIsDownloadedVerifiedAndPublishedByTheWorker`
10. `offlineEnvironmentRetriesWithoutMovingAByte`
11. `transportFailureRetriesAndRecordsTheAttempt`
12. `cancelledJobSucceedsWithoutTouchingTheBundle`
13. `missingMeteredConsentPausesAsSuccessAndKeepsRefusalDurable`

**Verified failure signatures (from the run log — observed facts, not yet root-caused):**
- `expected:<Completed> but was:<Retryable(error=integrity)>` — sha256 verification
  path failed on-device.
- `a partial bundle is never published expected null, but was:<BundleManifest(…
  lastError=transport …)>` and `… lastError=integrity …` — retryable states where the
  tests expected no/other manifest.
- `repair re-downloads and republishes expected:<Completed> but was:<Retryable(error=transport:IllegalArgumentException)>`
  — with `java.lang.IllegalArgumentException: List has more than one element.`
- `prior ready content is never deleted expected:<1> but was:<2>`
- `bytes survive reopen exactly expected: java.lang.Integer<22> but was:<java.lang.Long<22>>`
  — JSON number type differs on-device (Gson/JVM vs Android coercion).
- All 5 worker tests: `got Failure {mOutputData=Data {}}` instead of retry/success,
  including `missingMeteredConsentPausesAsSuccess…` (13 ms runtime, no exception logged
  — the worker returned `Result.failure()` cleanly). Hypothesis (UNVERIFIED — must be
  confirmed by code reading before any fix): worker gates read the **real device
  network** (ConnectivityManager: emulator on unmetered Wi-Fi) where the JVM tests
  injected metered/offline states, changing flow into an unexpected failure path.

## Job 5.11 — offline-state assertion failure (acceptance script, not the app)

`scripts/verify_emulator_upgrade.py:143` — `AssertionError: Offline state changed during
upgrade`: after the (fully passing) upgrade validation, the `--offline-preconfigured`
check asserted `dumpsys connectivity` showed `Active default network: none|null|-1`.
On the fresh API 36 CI emulator an active default network was still reported 31 s after
the disable commands. Not yet determined: whether `svc wifi/data disable` semantics on
API 36 emulators leave a default network, or timing. **Do not weaken the assertion** —
next step is capturing the actual `dumpsys connectivity` output (evidence-preserving
script change) and verifying real API 36 offline behavior before deciding.

## Status

Per the owner's rule: documented first; **no re-dispatch** until meaningful application
fixes are identified, implemented and verified (JVM tests locally, then CI). Stage 5
remains IN PROGRESS. No physical phone, no production credentials, no APK publication.
