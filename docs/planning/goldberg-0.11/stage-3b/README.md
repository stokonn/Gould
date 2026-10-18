# Stage 3B — portable backup v5 and safe recovery

Implemented and locally verified on `feature/goldberg-0.11-stage-3b`, descending from verified Stage 3A `cb36535d8a733143213347dd2ce355d1bf81ae96`. Prerequisite CI39125571414 passed. Production remains0.10.1/code11; no release/merge/provider/cloud work.

- [x] Verify policies, ancestry, actual backup/recovery/preference flows and existing tracking.
- [x] Add additive journal persistence for raw recovery evidence, completion evidence and preference replay.
- [x] Implement bounded/hash-validated portable v5, retaining historical readers1–4 unchanged in meaning.
- [x] Preserve destination snapshots/drafts/archives and imported UUIDs; recover paused with fresh execution IDs.
- [x] Implement allowlisted preference replay and explicit pending/retry state.
- [x] Add full/minimal golden fixtures and corruption/crash/size/atomic-publication tests.
- [x] Run build/JVM/lint/host checks and complete sequential API26/API37 instrumentation, seeded upgrade/offline/process-death checks.
- [x] Document implementation, exact executed verification and Stage4 prerequisites.
- [ ] Finish existing GitHub/Wiki synchronization and verified branch publication checkpoint.

Stop after Stage 3B. Stage 4 requires a separate instruction.

Documents: [implementation](IMPLEMENTATION.md), [verification](VERIFICATION.md), [handoff](READINESS.md). Checked implementation entries do not substitute for pending acceptance checks.
