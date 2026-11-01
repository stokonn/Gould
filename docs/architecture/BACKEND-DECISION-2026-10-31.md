# Gould backend disposition — 2026-10-31

Status: **architecture recommendation / pending owner approval**, not a production activation or deletion authorization.

## Decision recommended

**KEEP the existing backend sources, contract corpus and CI on main, but maintain their current fail-closed boundaries.** Do not split, remove, deploy new write routes or enable OAuth/community submissions as part of the README/screenshot task.

The local-first user promise and a maintained optional backend are compatible. Gould's current native Android application does not depend on successful community networking to record practice. Backend deletion would make the app *less* maintainable at this point: it would break test fixtures, workflow checks and source-bound Stage 6 evidence, while not removing the client functionality already included in the Android source.

## Verified code facts (main at fc333a2878989e42d1d3e0d70da665c4d00252f9)

- `settings.gradle.kts` includes only `:app`; `services/community-bff` is not an Android Gradle module.
- `app/build.gradle.kts` includes `contracts/admin/v1` in JVM test resources; canonical integration sources are optionally enabled with `goldbergCanonicalIntegration`.
- `OpusApplication.community` creates `LocalCommunityRepository` without an enabled transport. `DisabledCommunityTransport` has no URL or HTTP factory and its availability is `DISABLED`. Fixture transport is explicitly synthetic.
- `OpusApplication` also contains test-gated canonical/admin transport seams with disabled production transports. Do not infer live app submissions from existing interfaces.
- `.github/workflows/community-bff.yml` executes locked dependencies, local PostgreSQL / workerd regression checks and fail-closed packaging. Simply deleting backend sources would break this check.
- `services/community-bff/deployment/README.md` reports that a restricted catalogue-only Worker exposes two GET routes with an **empty catalogue** and rejects auth/admin/write routes. This historical deployment report is not a fresh live endpoint probe.
- `docs/PRIVACY.md` already documents the restricted read-only catalogue and associated infrastructure metadata.
- `AGENTS.md` and current verification policy preserve source-bound evidence, user data, signing, old identifiers, branches and protected artifacts.

## Separation of concerns

| Part | Current intended role | Disposition |
| --- | --- | --- |
| Android journal + Room | Local practice, repertoire, recordings, backups | Keep, unaffected |
| Community Android draft UI/transport | Local drafts; network submission disabled | Keep fail-closed; no implicit activation |
| Community BFF and Admin contracts | Development/integration fixtures and future gated contribution service | Keep source/tests/CI in main |
| Public catalogue-only Worker | Restricted read-only GET routes; approved publication set empty | Maintain existing controls; do not alter hosted resources |
| Full backend Google OAuth, admin decisions, community writes | Not approved for public operation | Stay disabled |

## Reconsider only with explicit scope

An eventual backend split may make sense if the owner deliberately abandons collaboration features or chooses a separate service lifecycle. Before splitting, require a file-level Android/CI dependency inventory, evidence-retention plan, independent host build and migration/restore tests, Git history portability checks and a documented owner decision.

### Explicit non-goals

No `git rm`, branch archiving/cleanup, force push, deployment, secret operations, Supabase/Cloudflare mutations, OAuth activation, API freeze, user journal upload, Room schema change or APK release.

Maintaining the backend is not a claim that community functionality is available to end users. When described publicly, distinguish local practice, optional third-party catalogue requests, a restricted empty first-party catalogue, and disabled community submissions.
