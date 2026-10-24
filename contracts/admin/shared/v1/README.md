# Goldberg Admin shared fixture package — version1

Executable local integration foundation, derived from the approved O1–O8 DTO
schema and actual Stage6 loopback HTTP/PostgreSQL. Package version1 does **not**
select or freeze public `/v1`. No second DTO dialect or legacy field aliases.

- `contract.json`: canonical rules and qualified approval boundary.
- `scenarios.json`: maintained required runtime scene IDs/statuses/DTO references.
  The suite produces actual responses, validates them before scrubbing synthetic
  token fields, and writes fresh source-bound captures under the run directory.
- `consumer-source.json`: exact inspected Stage7B Git source hashes, read-only.
- `consumer-cases.json`: expected outcomes from compiling unmodified selected
  Kotlin API/Models/WireValues/Stats code and replaying actual captured responses.
- `compatibility-matrix.json`: per-operation normative/backend/consumer evidence,
  classification, mechanism and exact remediation. Static facts and unexecuted
  Android behavior are explicitly distinguished from selected JVM replay.
- `consumer-reference.mjs`: executable reference for immediate local clearing,
  session/account binding, single-flight refresh, lost refresh response handling,
  explicit dual-revision reconfirmation and identical pending-intent recovery.
  Refresh invalidates old acknowledgements while preserving uncertain intent for
  explicit same-body/key recovery within the same principal.
  It accepts only an injected test request seam; no URL/persistence/deployment.
- `version-evidence.json`: searched immutable release tags, disabled legacy client
  and source history. External/deployed consumer state remains UNKNOWN.

Run from the repository root on the authorized rootless host:

```sh
python3 spikes/stage6-api-v1-local/build-current-provenance.py
python3 spikes/stage6-api-v1-local/run-http-postgres.py
python3 spikes/stage6-api-v1-local/verify-http-pg.py RUN_DIRECTORY
```

The runner selects all baseline suites and new shared/query suites, compiles the
selected Kotlin files from Git into a temporary directory using locally cached
Kotlin2.2.10/coroutines/JSON dependencies, and executes the independent fixture,
consumer, query-plan and source checks. It preserves failures and removes only
its exact synthetic container/anonymous volume. The independent verifier probes
resource absence again. No Stage7 worktree build, source write or Android runtime.

The historical61-case proposal corpus, frozen validators, metadata, provenance
and old raw evidence are unchanged. This package's runtime scene count is separate
from that historical61-case offline schema suite. Full-int8's original current
manifest stays byte-exact; this batch has a separate integration-readiness manifest.

Real source/field/license/attribution/evidence policy remains default-deny. Null
labels and unavailable comparison values stay unavailable. Public version choice,
Android transport/actions, C4/6.0f, hosted capacity and final Stage6/7B acceptance
remain gated. See the Stage6 integration-readiness handoff for exact next changes.

created by stokonn with love <3
