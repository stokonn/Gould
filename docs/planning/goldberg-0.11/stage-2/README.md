# Stage 2 — contracts, fixtures and measured readiness

Research-only branch `planning/goldberg-0.11-stage-2`, descended from Stage 1 `c3d5e03b9a4936d8db393df664af209a8853c767`. Production remains 0.10.1. No application/Gradle/Room-export/migration changes, deployment, paid service, OAuth configuration, main merge or release. All new SQL and Python here are isolated research artifacts, not application implementations.

## Checklist and deliverables

- [x] Verify ancestry, canonical/maintenance policies and Stage 1 inputs; preserve unrelated RELEASE_NOTES.md.
- [x] Finalize logical/physical, adapter/search and rights/revision contracts.
- [x] Build deterministic licensed fixtures and adversarial contract tests.
- [x] Run bounded provider investigation and record coverage/failures.
- [x] Measure legacy/proposed query cost, memory/storage and bundle costs; inspect emulator startup where available.
- [x] Specify lossless personal-data migration and expanded backup/recovery tests.
- [x] Record resolved decisions, remaining blockers, exact verification and Stage 3 prerequisites.
- [x] Prepare the verified Stage 2 commit/push and existing tracking/Wiki update; exact remote evidence is recorded in issue31 at handoff.

Reading order: [contracts](CONTRACTS.md), [physical separation](DATABASE-BACKUP.md), [source evidence](SOURCE-RESEARCH.md), [measurements/budgets](MEASUREMENTS.md), [readiness and verification](READINESS.md). Machine-readable contract values are in [contracts.json](contracts.json); [catalogue-research.sql](catalogue-research.sql) is executable only against disposable host databases. [research.py](research.py) generates fixtures, validates them and benchmarks without opening an installed journal. Network research is an explicit separate command and is not part of deterministic tests.

The owner has settled D1/D2/D3/D6 and the direction of D7/D9: multi-genre distinct works/arrangements/selectable movements/medleys, labeled Albums including EPs/singles/soundtracks; UUIDs plus retained legacy IDs; **separate catalogue DB**, protected opus.db and extended backups/settings/paused drafts; Czech→English verified fallback; measured storage/consent/performance controls. Backend/operator/Admin decisions D4/D5/D8 remain later-stage dependencies, not blockers for local Stage 3 persistence.

created by stokonn with love <3

## Reproducing the research safely

Run `python3 docs/planning/goldberg-0.11/stage-2/verify_stage.py` offline, and `research.py bench --output /tmp/goldberg011-stage2-host.json` for disposable generated host workloads. See MEASUREMENTS.md for the explicit-emulator command. `collect_sources.py --full-html` is optional live research, creates a unique /tmp folder and stops each provider on overload; do not run it as a test or retry while a source is throttled. Historical observations reflect the original bounded run; the corrected collector deliberately sends fewer queries when maxlag occurs. No raw responses should be committed.
