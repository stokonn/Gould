# Gould backend retirement — 2026-11-25

Status: **owner-approved architecture decision, implemented in the working tree.**
This document supersedes [BACKEND-DECISION-2026-10-31.md](BACKEND-DECISION-2026-10-31.md),
whose "keep the backend" recommendation is historical and no longer current guidance.

## Decision

Gould is an **Android-first, local-first application**. There is **no** product
community backend, BFF, moderation system, admin API, community database or
custom cloud backend — not deployed, not maintained and not present in the
repository.

- **Composer catalogue** is bundled with the app and installed into the local
  offline store; browsing it makes no network request.
- **Music discovery** uses third-party providers (MusicBrainz first, plus
  Wikidata, Wikipedia, Wikimedia Commons and Open Opus) contacted directly from
  the device under the default-off online-search consent.
- **Google sign-in** uses **Android Credential Manager**; Google profile
  name/avatar synchronization is an on-device profile update only.
- **Practical application data stays local** (Room `opus.db`, local drafts,
  recordings, backups, exports).

## What this retirement removed

| Area | Removed |
| --- | --- |
| Backend sources | `services/community-bff/` (BFF + deployment), `spikes/stage6-*` (API/auth/handlers/security/backup/cloud spikes), `contracts/admin/` (admin contract corpus) |
| Workflows | `.github/workflows/community-bff.yml`, `.github/workflows/maintenance-admin.yml` |
| Admin scripts | `scripts/stage6/*`, `scripts/finalize_github.py`, `scripts/finish_maintenance_admin.py`, `scripts/maintenance_admin_safety.py` and their tests, `scripts/check_admin_contract_fixtures.py`, `scripts/check_stage6_acl_proposal.py`, `scripts/ci_stage6_instrumentation.sh`, `scripts/ci_stage6_acceptance.sh` |
| In-app admin client | `app/src/main/java/com/opus/piano/data/community/canonical/` (canonical admin client/repository/contract/audit verifier), `app/src/main/assets/community/admin-canonical-schema.json`, `OpusApplication.canonicalAdminRepository()`, `AppContainer.testHooks.canonicalAdminTransport` |
| Tests | `app/src/canonicalIntegration/` (BFF HTTP integration), canonical admin unit and instrumentation tests, `contracts/admin` JVM test resources, the `goldbergCanonicalIntegration` Gradle switch |
| Docs | `docs/planning/goldberg-0.11/stage-6/` (backend stage tracking/evidence), `stage-7/stage-7b/` (admin contract design), `CLOUD.md`, `COMMUNITY-ADMIN.md` |

## What was deliberately preserved

- All functional Android application code, Room/local storage, migrations and
  backup formats; the local community **draft** store, archive, overlay cache
  and the Settings "Community drafts" panel (user data paths).
- Bundled MusicBrainz catalogue, provider clients, Credential Manager identity
  code and Google profile handling.
- Android build, JVM tests, instrumentation compilation, emulator verification
  (`scripts/ci_android_acceptance.sh`, renamed from `ci_stage6_acceptance.sh`
  with the checks unchanged apart from the current schema expectation), release
  signing and publication workflows.
- Generic security/CI helpers whose names merely contain `community` or
  `stage` (for example `check_agent_policy.py`, `check_publication_workflows.py`
  and the Stage 3–5 Android test classes).
- Historical records: dated stage checklists, verification ledgers, maintenance
  audits and release evidence are retained as history, not as current guidance.
- Remote infrastructure: no Cloudflare, Supabase or other remote resource was
  deployed, modified, deleted or accessed by this retirement.

## CI after retirement

| Workflow | Role |
| --- | --- |
| `quality.yml` (`host-quality`) | mandatory Gradle gate (clean assemble + JVM tests + instrumentation compilation + lint) plus migrations, backup fixtures, localization, history/paging, UI literals, agent policy, backend-retirement guard, publication-workflow policy, catalogue checks and unit suites |
| `stage5-emulator.yml` | optional, `workflow_dispatch` only: full instrumentation suite on one disposable API 37 emulator and the API 36 acceptance subset (in-place upgrade, network transport assertions, Data Saver, process-death integrity, startup timing) |
| `ios-cloud-build.yml` | Kotlin Multiplatform / iOS build and persistence tests |

The former `synthetic-postgres` and `administration-safety` required checks
belonged to the deleted workflows and no longer exist; repository required-check
settings must be updated by the owner.

## Enforcement

`scripts/check_admin_retirement.py` runs in `quality.yml` and fails if any of
the retired surface reappears (legacy admin routes, canonical admin code,
`contracts/admin` Gradle resources, BFF/admin workflows, URLs in the community
transport, or public documentation claiming an operated backend).

created by stokonn with love <3
