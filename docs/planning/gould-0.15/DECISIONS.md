# Gould 0.15 — decision record

Architectural and product decisions for Gould 0.15. Owner decisions are
binding; engineering decisions record rationale at the time of adoption.

## Owner decisions (Stage 1 review → Stage 2 authorization, 2026-11-14)

### 1. Streak semantics — BOTH (binding)

A successful week requires satisfying **both** the configured weekly session
goal **and** the configured weekly practice-minutes goal. One canonical
definition must be shared by: Activity and weekly progress UI, current and
longest streak calculations, weekly completion, Freeze consumption, weekly
rewards and achievements, Profile statistics and all related summaries.
Different screens or subsystems may not interpret a successful week
differently. (Note: `ActivityDomain.perfectWeek` already encoded BOTH; the
violations were the sessions-only `visibleStreak` bonus and `activity_goal_met`
display — see Stage-2 Batch B.)

### 2. Admin systems — EVALUATE (binding)

Map actual usage, dependencies, responsibilities and compatibility requirements
of the legacy (`admin/` package, `AdminApi`) and canonical
(`data/community/canonical/`) admin implementations. Identify duplicated or
obsolete functionality. Recommend consolidation or removal only where supported
by evidence. Preserve required functionality and data. No risky removal merely
to reduce line count. Deliverable: `ADMIN-EVALUATION.md`.

**Stage 2 closure decision (owner, 2026-11-14):** backend uncertainty does not
block Gould 0.15 Stage 2. Preserve both legacy and canonical systems and all
existing BFF artifacts/integrations; do not delete, consolidate, disable,
migrate or redesign them. Their real-world operational usage remains
unverified. Perform no additional operational investigation for Stage 2. This
closes the evaluation task without claiming either system is deployed, active
or production-ready.

**Superseding owner decision (2026-11-15):** retire and remove the obsolete
legacy Android admin/backend implementation after resolving dependencies. Keep
one canonical admin implementation, its genuinely required client/repository
and BFF components. Do not reactivate production networking. The 2026-11-14
preserve-both decision is historical and superseded for legacy Android source;
it does not authorize removal of the canonical BFF or unrelated backend
infrastructure. See the current Stage 2 checkpoint for files and verification.

**Final superseding owner decision (2026-11-25) — backend retirement:**
Gould is an Android-first, local-first application. Remove the entire
community/backend architecture: BFF sources and deployment, Stage 6 backend
spikes, `contracts/admin`, the canonical in-app admin client/repository/asset,
BFF and maintenance-admin workflows, backend-only scripts and tests, and
one-off backend stage documentation, and repair every affected Gradle, CI,
script, test and document reference. No product community backend, BFF,
moderation system, admin API, community database or custom cloud backend
exists or may be reintroduced; `scripts/check_admin_retirement.py` enforces
this in `quality.yml`. Preserve all functional Android code, Room/local
catalogue/MusicBrainz/Credential Manager/Google profile sync behavior, Android
build, instrumentation, emulator verification, release signing and publication
workflows, and historical records. Do not touch remote Cloudflare/Supabase or
any other remote infrastructure. This supersedes the 2026-11-14
preserve-both and 2026-11-15 keep-canonical decisions in full. See
`docs/architecture/BACKEND-RETIRED-2026-11-25.md`.

### 3. License terms — binding planning decision (2026-11-14)

- Application source code is planned under **GPL-3.0-or-later**.
- Documentation and original prose are planned under **CC-BY-SA-4.0**.
- This selects the planned terms only. It is **not authorization to apply,
  publish, or represent either license as currently effective**. No `LICENSE`,
  `COPYING`, `NOTICE`, SPDX header, license metadata, or in-app license asset
  may be created or changed until separate explicit owner authorization.
- **Blocking prerequisite before application:** verify copyright ownership and
  provenance for all relevant code contributions, bundled assets, AI-assisted
  artwork, and documentation. Resolve rights for every relevant contribution
  and asset; do not infer a single rights holder from repository history.
- Before distributing any binary under GPL-3.0-or-later, ensure the complete
  corresponding source for **each distributed binary** is available to every
  recipient for the required period and is the source corresponding to that
  exact binary. Confirm the release process can meet this requirement.
- Keep code and documentation licensing distinct. Third-party materials keep
  their own applicable terms and are not relicensed by this planning decision.
- Separate follow-ups: verify Google ID / Play Services terms and F-Droid
  eligibility if F-Droid is pursued; preserve the existing exclusion of
  MusicBrainz CC BY-NC-SA data from redistribution. Neither follow-up changes
  the selected planned terms.

### 4. Physical-device testing (binding, 2026-11-14)

Gould acceptance is tested by the owner on a real Android device, primarily a
Samsung Galaxy A55. Emulator/AVD availability, `/dev/kvm`, emulator binaries,
and emulator instrumentation are not acceptance prerequisites. Run the
available host checks, JVM tests, Android compilation, lint and static data / UI
checks. Classify emulator-dependent instrumentation as not executed when no
emulator is available; never report compilation as execution. Prepare and
maintain a physical-device checklist and record device results only after the
owner reports them. This decision authorizes owner-performed testing; it does
not authorize an agent to install, launch, inspect, or run ADB against the
owner's phone.

The owner has directed preparation of a normal release-variant testing APK
with versionName `0.15.0-dev`, versionCode 17, and the existing trusted
production signing configuration. Verify the artifact's metadata, non-debuggable
flag and certificate before owner testing; this is not public release
authorization. It does not itself verify an in-place install or migration.
The owner must export a backup first and never uninstall or clear the existing
installation to bypass any Package Manager error.

## Stage 2 engineering decisions

### D-2.1 Implementation branch

`feature/gould-0.15` created from `origin/main` @ `a908c96` in worktree
`.worktrees/gould-0.15`. The docs-branch working tree with its uncommitted
owner work remains untouched. Unmerged remote documentation branches
(`docs/gould-0.14-prerelease-docs`, `docs/gould-0.14-release-documentation`)
are not merged; useful wording may be referenced when rewriting user-facing
docs, with attribution to those branches in the Stage 2 report.

### D-2.2 Verification audit trail

`scripts/verify-audit.py` now writes Gould 0.15 tier runs to
`docs/planning/gould-0.15/verification-audit.jsonl` (override:
`GOULD_AUDIT_FILE`). Historical audit files for earlier versions are preserved
unchanged. The 0.13/0.14 matrices remain as historical records; this file is
the active matrix.

### D-2.3 AppContainer over a DI framework

Replace the service-locator access pattern with a single lightweight
`AppContainer` composition root. Hilt/Koin explicitly rejected (owner scope:
no unnecessary dependencies/KMP expansion/Rust). Lazy lifetimes and startup
ordering (`L.initialize` first; recovery lazy; nothing in startup touching
admin/community stores) must be preserved.

### D-2.4 No schema merge; two Room databases stay separate

`opus.db` (personal) and `goldberg-catalogue.db` remain separate; the
`PersonalCatalogueBridge` and backup anchor validation are compatibility-
critical and untouched.

### D-2.5 Onboarding persistence

Tour state is persisted additively on `local_app_state` (Room 12→13 migration,
schema export, backup round-trip). Any new persisted field is added to the
portable-preference allow-list/validation in the same change so backup
round-trips cannot break. No destructive migration; no second NavHost.

## Owner release authorization — 2026-11-16

The owner explicitly approved production use of MusicBrainz Core snapshot
`20261114-002230` with archive SHA-256
`da4f1afcbca8ca54eb6362ae45173543f079e8491d097a376d060551d1a03335`, and
authorized GPL-3.0-or-later for project-owned program source and CC-BY-SA-4.0
for project-owned documentation/original prose. The completed provenance,
source-scope audit, and known qualifications are in
`provenance-evidence/FINAL-PROVENANCE-REPORT.md` and `docs/LICENSES.md`.

The owner also explicitly authorized final verification, integration to
`main`, and publication of `v0.15.0` as a GitHub **Pre-release**, conditional
on all required gates passing. Prefer squash-and-merge through a pull request.
This authorization does not permit destructive cleanup, history rewriting,
force-push, backend deployment, physical-device access, Stable classification,
or publication of an APK other than the verified owner-tested candidate.
