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
not repeatedly rerun this command in a venue known to deny sockets. Current agent
run: 59 PASS; five checks PASS; PostgreSQL EPERM, exit78. No emulator is involved.

`python3 spikes/stage6-security-local/build-matrix.py` rechecks exact retained TAP/
XML records and baseline hashes, and regenerates the deterministic evidence index.
It intentionally refers to the reviewed final run `run-x1eRKaqL`; new host results
must be explicitly reviewed and then incorporated, never silently treated as PASS.

The T22 audit-edit probe **observes400 without dispatch**. It is a passing
observation test but a **failed403 threat acceptance expectation**. It does not
waive or redefine the contract. No privilege bypass demonstrated. Any behavior/
contract reconciliation must be explicit and preserve existing baseline tests.

No full T14 fetch/redirect/rebinding assertion, T15 HTTP/browser scenario, T19
restore/RPO drill, Android runtime or actual network privacy assertion was executed.
See matrix for every local/hosted gap. Do not infer full security/compliance from
59 passing probes or historical237/214 totals. Formal6.12 stays unchecked.

created by stokonn with love <3
