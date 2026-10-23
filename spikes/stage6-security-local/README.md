# Stage 6.12 local security verification

Verification-only extension of the approved local services; synthetic identities,
synthetic metadata, real local JWT crypto. No service/API/schema behavior changes.
The [threat matrix](../../docs/planning/goldberg-0.11/stage-6/LOCAL-SECURITY-MATRIX.md)
records exact assertions, successful executed test names and open acceptance gaps.

Run from the isolated worktree:

```bash
bash spikes/stage6-security-local/run-local.sh
```

Prerequisites: Node22, Python3, existing locked `jose6.2.13` and `pg8.23.1`
dependencies installed under their original `stage6-auth-local`/`stage6-schema-pg`
packages. Reuse the permitted local dependency cache. If missing, first inspect the
original lockfiles; an offline `npm ci --ignore-scripts --offline` in each original
package may be used on this isolated worktree only. The runner never installs or
downloads dependencies automatically. Never copy credentials or hosted URLs.

On a permitted Fedora host, PostgreSQL stage also requires Podman and the already
cached PostgreSQL17.11 image pinned by the **unchanged**
`spikes/stage6-public-admin-abuse/run-local.sh`. No image pull, cloud connection or
host database deletion. The existing runner creates/removes only its uniquely
named disposable synthetic container, loopback-only port and prototype schemas.
It reruns 34 integration +32 unit +87 handler +50 Auth +34 schema tests and five
checks, retaining a new results directory without overwriting past evidence.
The outer security runner also executes 27 focused +32 unit tests; the repeated32
are separate executions, never added as distinct tests to the historical237 count.

Exit78 means socket preflight blocked: focused/unit/check results remain valid,
zero PostgreSQL test bodies execute, and overall acceptance remains PARTIAL. Do
not repeatedly rerun this command in a venue known to deny sockets. The original agent run was59 PASS with PostgreSQL EPERM. The owner subsequently
executed `run-vQd6iGn5` + nested `run-qx9TUTDk` successfully atfaf9ba3:
**296 PASS executions;264 tests counting the repeated32 suite once**, five checks
PASS in both runners,62 at-run source hashes verified. No emulator is involved.

`python3 spikes/stage6-security-local/build-matrix.py` rechecks exact retained TAP/
XML records and baseline hashes, and regenerates the deterministic evidence index.
It intentionally refers to the reviewed host runs `run-vQd6iGn5`/`run-qx9TUTDk`; new host results
must be explicitly reviewed and then incorporated, never silently treated as PASS.

The historical T22 probe observed400 without dispatch; that FAIL evidence remains.
The owner explicitly approved the current local routing correction: absent valid
operation403, malformed operation/body400, registered missing/invalid session401,
capability/self-grant403. Current38 scripted-storage T22 tests pass; T22 remains
PARTIAL pending updated PostgreSQL execution. No new endpoint or privilege.

No full T14 fetch/redirect/rebinding assertion, T15 HTTP/browser scenario, T19
restore/RPO drill, Android runtime or actual network privacy assertion was executed.
See matrix for every local/hosted gap. Do not infer full security/compliance from
296 passing executions or historical237/214 totals. Formal6.12 stays unchecked.

created by stokonn with love <3

## Host publication after sandbox DNS/integration-write failures

Do not repeat the original Android/backend publisher. For this new security branch,
inspect the current local commit and run the scoped publisher with its exact SHA:

```bash
python3 spikes/stage6-security-local/publish-host.py --expected-head REVIEWED_40_CHARACTER_SHA --verify-only
python3 spikes/stage6-security-local/publish-host.py --expected-head REVIEWED_40_CHARACTER_SHA
```

The first command performs local read-only guards. The second uses existing host
Git/gh access, normal push and exact remote readback before appending one factual
PARTIAL checkpoint. No automatic authentication/setup, dashboard edit, build,
deployment, release or APK upload. An existing matching checkpoint is reused.
If a remote branch diverged, the script stops; never force-push or rewrite it.
Publisher history checks stop on concurrent description/comment changes without
editing or restoring them. Checkpoint/publication receipt stays ignored under
`.gradle/stage612-publication/published-host.json` for agent readback.

## Reporting fix and evidence identity

Version1 produced the confirmed host evidence; no application/service/SQL/Android
source was changed afterward. Version2 excludes regenerable Python bytecode caches from source snapshots and replaces fragile regex indexing with
`tap_report.py`: absent/duplicate/truncated/inconsistent summaries produce clear
`summaryErrors`, unverified counts stay absent, source changes and unsuccessful
results fail closed, and nested runner exit status is saved explicitly. Ten focused
Python regressions pass, including the retained missing-dependency startup log:

```bash
python3 -m unittest discover -s spikes/stage6-security-local/tests -p 'test_*.py' -v
```

The full version2 PostgreSQL runner has NOT been executed. Do not relabel the
owner's version1 run as version2 acceptance or repeat the successful suites merely
to restate them. `verified_evidence.py` rechecks immutable55 raw-artifact hashes,
exact seven TAP counts, repeated unit names, nested stdout linkage and successful
wrapper flags. All62 at-run source hashes matched before edits; expected tooling
edits are validated against immutablefaf9ba3 bytes. Current tool/index hashes are
separate; older raw/index manifests are retained with their original commit identity.
The live agent Podman inventory check was blocked by read-only runtime configuration;
cleanup is evidenced by the retained host removal output, not a new inventory pass.

## Current T22 and Stage7B handoff extension

Version3 local runner also executes38 T22 and11 shared-contract tests; reporting
includes these summaries when present and fails closed on missing summaries. Final
current evidence:108 Node +11 Python =119 PASS, separately from the pre-patch host296.
Current PG suites have not run in the denied socket venue; run on permitted host
only. The old87 handler/34 integration/32 unit sources change only the authorized
absent-operation status assertion; no old logs or manifests are overwritten.

`contracts/admin/v1` has30 actual in-process/scripted-storage captures,11 inactive
proposals and29 imported authored comparison cases. Read provenance qualifications,
G1–G11 ledger and Stage6 T22/readiness reports; these are not HTTP/PG/Android/frozen
contract proof. G10 stays closed; live networking/actions remain disabled. No Stage7B
files changed. Use `python3 spikes/stage6-security-local/verify_contract.py` to verify
source/corpus hashes and the read-only client snapshot. Corpus changes must be
reviewed and intentionally rehashed; never silently auto-approve a new dialect.
