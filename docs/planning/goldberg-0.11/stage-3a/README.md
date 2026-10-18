# Stage 3A — safe local persistence

Implemented and locally verified on `feature/goldberg-0.11-stage-3a`, based on Stage 2 `d6049add3e46399d8f48afcb2aaa03422ebe0017`. Prerequisite CI [39120954962](https://github.com/stokonn/Goldberg/actions/runs/39120954962) completed successfully. Production remains 0.10.1; this branch is not a release.

[Implementation and schema choices](IMPLEMENTATION.md) · [Executed verification](VERIFICATION.md) · [Stage 3B prerequisites](READINESS.md)

## Implementation checklist

- [x] Verify ancestry, source schema 8, Stage 2 CI, policies and unrelated owner work.
- [x] Add transactional personal schema 9 bridge, preserving all schema 8 tables and bytes.
- [x] Add independent catalogue schema 1, bounded DAOs, FTS4 and typed graph/rights validation.
- [x] Add lazy, retryable local reconstruction; isolate failures from journal startup.
- [x] Execute all historical Android migrations, journal regression and catalogue failure tests.
- [x] Execute seeded signed in-place 0.10.1 upgrade; compare original data and preferences.
- [x] Run JVM/build/lint/host compatibility checks and targeted measurements.
- [x] Prepare verified technical documentation and English Wiki sources. Final branch SHA, CI and remote synchronization evidence are recorded on [issue31](https://github.com/stokonn/Goldberg/issues/31) after push; the umbrella stays open for later stages.

Stage 3B portable backup v5 and Stage 4 providers/search integration remain excluded. Historical format 1–4 readers/writer remain compatible; new identities are not yet portable between installations through format 4. No backend, downloads, UI redesign, package rename, merge, tag or release.
