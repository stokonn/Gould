# Stage 5 CI run 39508388525 — second dispatch (2026-10-20T22:28–22:35Z)

**Classification: CI script-execution-model failure — NOT an application defect.**
The instrumentation suite still never started (zero XMLs); no application conclusion
(pass or fail) may be drawn. This run otherwise validated nearly every prior fix.

| Property | Value |
| --- | --- |
| Run | https://github.com/stokonn/Goldberg/actions/runs/39508388525 |
| Dispatched commit | `5cdc0d64da4049022527c676f26d404ffa691753` (branch `feature/goldberg-0.11-stage-5`, the owner-approved commit) |
| Created / updated | 2026-10-20T22:28:41Z / 2026-10-20T22:35:52Z |
| Job 5.10 | failure — emulator step only (all prior steps success) |
| Job 5.11 | failure — emulator step only (all prior steps success) |
| Instrumentation XML results | none (suite never started) |

Raw logs: `run-logs.zip` (SHA-256 `efa4baf3fc26558826f3b65354ebee64a793df7269e9ca3fb2852a4205ba42e7`).
Artifacts: `artifacts/` (12 files — including the new signing diagnostics).
Failed-step logs: `instrumentation-emulator-step.log`, `acceptance-emulator-step.log`.
Passing signing-step log: `signing-step.log`.

## What this run PROVED working (attempt-2 fixes verified on the actual runner)

- **SHA binding** passed in both jobs (source-commit artifacts = `5cdc0d6…`).
- **All builds:** candidate APK, androidTest APKs, and the v0.10.1-tag debug-rebuild
  baseline all built successfully.
- **Signing step PASSED with full diagnostics** — the attempt-2 fix works end-to-end:
  - `apksigner used: /usr/local/lib/android/sdk/build-tools/37.0.0/apksigner` —
    **the runner itself confirms the run-39503450845 root cause** (37.0.0 preinstalled
    and selected by `sort -V | tail -n1`); its `V2 Signer:` format was parsed by the
    dual-format parser.
  - release APK digest `53eface4…d59e` **= trusted certificate** ✓
  - baseline = candidate = `47aebcfd…34c6` (the runner's temporary debug keystore,
    equal by construction) ✓
  - all three apksigner exit codes 0; raw stdout/stderr uploaded as artifacts
    (`apksigner-*.log/.err`, `signing-environment.txt`, `apksigner-exit-codes.txt`).
- **KVM emulator boot works:** API 36 google_apis emulator booted in **49 524 ms**
  (`Boot completed in 49524 ms`), serial `emulator-5554`.

## Root cause — the action's `script` input is executed line-by-line

Both jobs died at the emulator step with the same error:

```
[command]/usr/bin/sh -c set -e
[command]/usr/bin/sh -c cat > "$RUNNER_TEMP/stage5-instrumentation-script.sh" <<'BASH_SCRIPT'
[command]/usr/bin/sh -c set -euo pipefail
/usr/bin/sh: 1: set: Illegal option -o pipefail   (exit 2)
```

Three **separate** `sh -c` processes for one `script:` input. Proven from the pinned
action's own source (`reactivecircus/android-emulator-runner@a421e43`, files fetched
2026-10-20):

```ts
// src/script-parser.ts
parseScript(raw) = raw.trim().split(/\r\n|\n|\r/).map(trim)
                    .filter(line => !line.startsWith('#') && line.length > 0)
// src/main.ts
for (const script of scripts) await exec.exec('sh', ['-c', script], { env … })
```

The input is split into **single lines** (comments/blanks stripped) and each line runs
in its **own shell**. Consequences: `set -o pipefail` hits dash per-line; my heredoc
wrapper's opener ran alone (empty file); and — more fundamentally — **any multi-line
construct or variable assignment in the `script:` input cannot work**, because state
does not persist between lines. (The approved script body itself relies on `SERIAL`
persisting across guard lines.) The action's `script` input effectively accepts only
single-line commands; run 39503450845 failed at line 1 of this same model.

## Meaningful fix applied (workflow-only, assertions byte-identical)

1. A regular GitHub `run:` step (executed by GitHub as **one** `bash -e` process —
   not parsed by the action) writes the full approved script into
   `$RUNNER_TEMP/stage5-<job>.sh` via a quoted heredoc, then `bash -n` syntax-checks it.
2. The action's `script:` input becomes exactly one line:
   `bash "$RUNNER_TEMP/stage5-<job>.sh"` → `parseScript` yields one chunk →
   `sh -c 'bash "$RUNNER_TEMP/…"'` → the whole script runs in **one bash process**.

All guards (`emulator-NNNN`, single-device, `ro.kernel.qemu=1`), assertions, traps and
the strict signing checks remain unchanged. Not yet executed at the time of writing:
the third dispatch (documented here first, per the owner's instruction).
