# Stage 5 CI run 39518973833 — fourth dispatch (2026-10-21T00:03–00:15Z)

**Classification: JOB 5.10 PASSED — the full instrumentation suite is green on the
emulator (all 13 run-3 fixes verified on-device). Job 5.11 failed at the same final
offline-state assertion; root cause now proven from logcat — `svc data disable` had no
effect on the API 36 CI emulator and cellular came up 15 s later as the default network
for the entire "offline" upgrade.** Stage 5 remains IN PROGRESS (5.11 pending its
evidence-preserving fix; notebook/manual-only items and 5.12/5.13 untouched).

| Property | Value |
| --- | --- |
| Run | https://github.com/stokonn/Goldberg/actions/runs/39518973833 |
| Dispatched commit | `dcea5b21796bf6587bb89a9267df8bc3c3c8b6bd` (branch `feature/goldberg-0.11-stage-5`, headSha verified; both jobs' `source-commit.txt` confirm the same SHA) |
| Created / updated | 2026-10-21T00:03:48Z / 2026-10-21T00:15:18Z |
| Job 5.10 | **SUCCESS** — 177 tests, **0 failures**, 0 errors, 2 skipped |
| Job 5.11 | failure — step 13, `verify_emulator_upgrade.py:143` `AssertionError: Offline state changed during upgrade` |
| Raw logs | `run-logs.zip` (SHA-256 `4b92dc7a05f7d742e8b37a013f3f0ab7fa1c2ac186d133c24191131b2668ab28`) |
| Evidence policy | Curated bundle below; full 257-file artifact set retained by GitHub for 30 days from the run URL. Unlike run 3, no per-test failure logs/logcats are needed — 5.10 had zero failures, so only success proof (counts, XML, testlog, logcat) and 5.11 diagnostics were kept. |

## Job 5.10 — PASSED: 177 tests, 0 failures, 0 errors, 2 skipped

All 13 run-3 failures are fixed and verified on the API 36 emulator:

- Worker cluster (5): `context.applicationContext` to `TestListenableWorkerBuilder`
  resolved the `as? OpusApplication` entry failure — the direct-builder tests now reach
  the worker logic (success/retry/pause semantics all correct on-device).
- Fixture/endpoint cluster (4): shared bundleIds, exact-URL routing with identical
  shared bytes, blob installed before promote.
- JUnit overload (1): `content.size.toLong()`.
- Product fixes (2 + invariant gap): `settle()` timestamp rule, `readPublished()`
  authoritative-only + `locate()` via `readAll()`.

Counts source: `instrumentation/instrumentation-counts.txt`
(`Tests: 177  Failures: 0  Errors: 0  Skipped: 2`); per-test pass codes in
`instrumentation/test-results.log`; `instrumentation/TEST-results.xml`; full run logcat
in `instrumentation/logcat.txt`. Task 5.10's instrumentation execution requirement now
has executed-**passing** evidence (subject to the checklist's remaining 5.10 items).

## Job 5.11 — upgrade validation PASSED again; offline premise never actually achieved

Everything before the final assertion passed on the emulator for the second time
(baseline v0.10.1 install/launch + schema verify, seed push, candidate install/launch,
prefs unchanged, `before == after` persistent-data equality, `PRAGMA user_version` +
`foreign_key_check`, UUID-v4 identity bridge). Signing evidence step passed.

### Root cause of the offline assertion (proven from acceptance logcat, not inferred)

Timeline (device logcat vs runner step log; `acceptance-logcat.txt`,
`acceptance-emulator-step.log`):

| Time (UTC) | Event |
| --- | --- |
| 00:11:59.7 | acceptance script starts (`bash "$RUNNER_TEMP/stage5-acceptance.sh"`); `verify_emulator_upgrade.py --offline` runs `svc wifi disable` + `svc data disable` (python `adb()` = `subprocess.check_output` — any non-zero exit would have raised; the baseline install at 00:12:07 proves both returned 0, and no stderr appears in the step log) |
| 00:11:59.904 | **Wi-Fi disable took effect** — logcat: `WifiService: setWifiEnabled package=com.android.shell uid=2000 enable=false isPrivileged=true`, wifi state → DISABLED at 00:12:00.2 |
| — | **The mobile-data disable left NO trace anywhere in the 84 730-line logcat**: no `setMobileData`/`setUserDataEnabled`/`DataEnabledSettings` event, no settings change |
| 00:12:15.222 | cellular registers anyway: `registerNetworkAgent … network{100} … ni{MOBILE[LTE] CONNECTING extra: epc.tmobile.com}` |
| 00:12:19.463 | network 100 becomes the **active default network** (`Switching to new default network … MOBILE[LTE] CONNECTED`, `eth0`, `10.0.2.15/24`) |
| 00:13:16–00:13:51 | network 100 keeps serving requests **during the whole "offline" upgrade** (e.g. `NetReassign [158 : null → 100]` for `com.opus.piano`) |
| 00:13:53 | final assertion `Active default network: none\|null\|-1` fails — correctly: the state was never offline |

So the assertion did exactly its job: it caught that the `--offline` premise was
**never achieved** (not that the app re-enabled networking). Wi-Fi was off the whole
time; the emulated CELLULAR network was never disabled. `svc wifi disable` works on
API 36; **`svc data disable` had zero observable effect** (candidate explanations: the
command is a silent no-op/usage-error on this image with output discarded into
`check_output`'s stdout capture and exit 0, or it was issued while the telephony stack
was still initializing 14 s after boot — the logcat shows phone-account registration in
progress at 00:11:59.05; the planned fix's verification loop + belt-and-suspenders
`settings put global mobile_data 0` + airplane-mode fallback covers both).

### Not caused by

The Goldberg app (it never had a chance to act — the network was up before the baseline
was even installed), the workflow execution model, or the assertion format (the active
network was real).

## Sections after the python call still never executed in CI

The WiFi→mobile transition test, Data Saver toggles with readback, process-death and
post-restart DB checks, and startup measurement that follow `verify_emulator_upgrade.py`
in the acceptance script have **never run on a real CI emulator** — every attempt has
ended at the python assertion. They only ever ran in the local stubbed simulation
(which validates the execution model, not behavior). Attempt 5 will reach them for the
first time if (and only if) the offline premise is genuinely achieved and the assertion
passes.

## Next: evidence-preserving fix (no assertion weakening)

Planned change (documented before implementation, per the diagnose-and-document-first
rule):

1. `scripts/verify_emulator_upgrade.py` `--offline` mode: after `svc wifi/data disable`,
   additionally `settings put global mobile_data 0`; then a verify/retry loop (~30 s,
   re-issuing the disables) because radios tear down asynchronously and telephony may
   still be initializing; escalate to `cmd connectivity airplane-mode enable` if still
   online; **save `dumpsys connectivity` (+ `settings get global mobile_data`) to the
   output dir at every stage** and **fail fast with that evidence** if offline cannot be
   achieved (the offline premise is invalid, so continuing would be the very lie the
   assertion exists to catch — and the upgrade evidence from runs 3/4 is already
   preserved).
2. Final assertion (line 143): **unchanged semantics** — same regex, same message —
   but the `dumpsys connectivity` output is captured to the artifact dir immediately
   before asserting.
3. `.github/workflows/stage5-emulator.yml` section (b): disable airplane mode before
   the `svc data/wifi enable` restores (needed only if the fallback fired; harmless
   otherwise).
4. Local verification: `python3 -m py_compile`, workflow YAML parse + existing
   `scripts/check_*.py`, `bash -n` on the materialized script; host emulator remains
   blocked (documented), so behavioral verification is attempt 5's job.

No assertion is removed, relaxed, or skipped anywhere.

## Compliance

Restrictions held: no physical phone, no production signing credentials in CI, no APK
publication/tag/release, no Stage 5 application code merged to `main` (only CI workflow
files via PRs #33/#34), Stage 6/7 untouched, Home/Profile redesign requirements
preserved. Stage 5 stays IN PROGRESS.
