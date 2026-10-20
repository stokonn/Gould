# Stage 5 CI run 39503450845 — first dispatch (2026-10-20)

**Classification (owner-confirmed 2026-10-20): CI setup/script failures — NOT application defects.**
No Stage 5 application tests executed in this run; no application conclusion (pass or fail) may be drawn from it.

| Property | Value |
| --- | --- |
| Run | https://github.com/stokonn/Goldberg/actions/runs/39503450845 |
| Dispatched commit | `bcd88949b7998ce30df25b19b71de30dd413483a` (branch `feature/goldberg-0.11-stage-5`) |
| Created / updated | 2026-10-20T21:49:30Z / 2026-10-20T21:57:05Z |
| Job 5.10 (`stage5-instrumentation`) | failure — script died at its first line |
| Job 5.11 (`stage5-acceptance`) | failure — signing step returned `MISSING` ×3; emulator step skipped |
| Instrumentation XML results | none produced (no suite ran) |
| SHA binding (`git rev-parse HEAD == GITHUB_SHA`) | passed in both jobs (artifacts: `instrumentation-source-commit.txt`, `acceptance-source-commit.txt`) |
| Candidate/baseline APK builds, KVM boot, trusted-release digest | all succeeded (see raw logs) |

Raw run logs preserved in `run-logs.zip` (SHA-256
`a6b48abbf6c4696121dd1641db6e88a58c0fe70ade15a243978136d02250ad85`); artifacts under `artifacts/`;
failed step logs: `instrumentation-emulator-step.log`, `signing-step.log`.

## 5.10 root cause — `/usr/bin/sh` (dash) rejects `set -o pipefail`

The `reactivecircus/android-emulator-runner` action invokes its `script` input with
`/usr/bin/sh -c`, and Ubuntu's `/usr/bin/sh` is dash. The very first script line
`set -euo pipefail` therefore aborted immediately:

```
[command]/usr/bin/sh -c set -euo pipefail
/usr/bin/sh: 1: set: Illegal option -o pipefail     (exit 2, 21:57:01)
```

Everything before the script worked: the API 36 emulator booted on the GitHub-hosted
runner (KVM functional), `sys.boot_completed` polling succeeded, boot completed
≈53 s after emulator start (21:56:06 → 21:56:59), and the action then invoked the
script, which failed at line 1. Evidence: `instrumentation-emulator-step.log`.

**Correction (applied 2026-10-20, workflow-only):** both jobs' emulator scripts now
materialize the real bash script into `$RUNNER_TEMP` via a quoted heredoc
(`cat > "$RUNNER_TEMP/…-script.sh" <<'BASH_SCRIPT' … BASH_SCRIPT`) and execute it
explicitly with `exec bash "$RUNNER_TEMP/…-script.sh"`. All guards, assertions and
traps are byte-identical to the approved script body.

## 5.11 root cause — apksigner print format changed in build-tools 37.0.0

`digest_of` parsed `sed -n 's/^Signer #1 certificate SHA-256 digest: //p'` from
apksigner stdout and discarded stdout/stderr/exit codes (`2>/dev/null`), so the
failure surfaced only as `MISSING` for all three APKs and the step exited 0 via a
misleadingly worded certificate error (no certificate mismatch occurred — the
emulator step was skipped before any upgrade test could run).

Empirical local reproduction (2026-10-20, same release APK
`Goldberg-v0.10.1.apk`, SHA-256 `854bbfebf617ec920df60d181a12e0dcbd2fed10052f2479903a78a290cef312`):

| build-tools | apksigner stdout label | SHA-256 digest | exit |
| --- | --- | --- | --- |
| 36.0.0 (workflow-installed) | `Signer #1 certificate SHA-256 digest: …` | `53eface4…d59e` | 0 |
| 37.0.0 (preinstalled on runner; selected by `sort -V \| tail -n1`) | `V2 Signer: certificate SHA-256 digest: …` | `53eface4…d59e` (identical) | 0 |

The old sed pattern matched the 36.0.0 label only; with 37.0.0 selected on the
runner it produced zero matches with exit 0 for every APK — exactly the observed
`MISSING` ×3. Certificate identity itself was never in doubt (both tool versions
report the same digest).

**Corrections (applied 2026-10-20, workflow-only, assertions unchanged/strict):**

- Every apksigner invocation now records raw stdout, stderr and exit code to
  `stage5-ci-results/apksigner-<name>.log/.err` and `apksigner-exit-codes.txt`
  (uploaded as artifacts) plus a `signing-environment.txt` inventory
  (apksigner path, build-tools directory, java). No diagnostics are suppressed.
- Digest parsing accepts both the 36.x (`Signer #1 …`) and 37.x
  (`V2 Signer: …`) formats and requires exactly one distinct signer digest
  (ambiguity → explicit error). Stdout is parsed first, stderr only as a
  recorded fallback.
- Strict assertions preserved verbatim in force: non-empty digest, release APK
  must equal the trusted certificate
  `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`, and the
  v0.10.1 debug-rebuild baseline must equal the candidate signer. All three
  APKs are inspected before the step fails, so one failure never hides the
  others.

## Local validation of the corrections (executed 2026-10-20 on the host)

- Static: YAML parse, `bash -n` on every run step, `bash -n` on outer (sh) and
  inner (bash) emulator scripts, `py_compile` on embedded Python — all pass.
- Wrapper mechanics under `sh -c` (stubbed adb/gradlew): guards pass with one
  `emulator-NNNN` device + `ro.kernel.qemu=1`; suite invoked; EXIT trap fires
  through `exec`; guard failure propagates exit 1.
- Signing step in a sandbox reproducing runner conditions (`tail -n1` selects
  apksigner 37.0.0): success path exit 0 with full evidence files; genuine
  certificate mismatch (separately generated test keystore) exit 1 with both
  digests named; corrupt APK exit 1 with non-zero exit code recorded and raw
  streams preserved.

Not executed on the host (requires a CI runner): the actual API 36 emulator
suite runs for tasks 5.10/5.11 — pending the re-dispatch after owner approval.
