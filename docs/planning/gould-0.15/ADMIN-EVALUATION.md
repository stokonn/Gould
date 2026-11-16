# Gould 0.15 — administration systems evaluation

## Owner-directed retirement update — 2026-11-15

The owner superseded the earlier preserve-both decision. The Android legacy
admin implementation and UI have now been removed, including its application
wiring, settings/session persistence, HTTP client, repository, WorkManager
worker, and legacy-focused tests. The old unique periodic job is cancelled at
upgrade by its persisted WorkManager name; no worker class remains to schedule
or execute it. The canonical Android client/repository and canonical BFF remain.
Canonical transport no longer imports the legacy package, and its application
default remains fail-closed. No production endpoint or live request was added.
Focused verification is recorded in `STAGE-CHECKLIST.md` after it runs.

**Status:** legacy app UI/runtime exposure retired; source cleanup is partially
blocked by pre-existing modified work. No remote service was changed.

## Finding

There are two Android-side admin clients with overlapping read/review concepts,
but they are not interchangeable implementations of one frozen protocol. The
legacy `com.opus.piano.admin` routes are registered and their screens/view
models exist, but source navigation currently does not reach them:
`ProfileScreen` is constructed with `administration = null` and there is no
navigation call to an admin route. The canonical
`data.community.canonical` path is a Stage 6/8 candidate whose explicit JSON
contract and decimal revision model are not wired to the UI. App transports
remain disabled by default.

## Evidence

- `ui/admin/AdminViewModel.kt` receives `OpusApplication.adminRepository` and
  the legacy admin routes/screens are declared in `OpusApp.kt`. However,
  `ProfileScreen` is passed `administration = null`, and searches of the active
  navigation source found no `nav.navigate(Routes.ADMIN)` or equivalent admin
  route navigation. Thus source proves registration, not current user
  reachability. Do not add or remove an entry without an owner product decision.
- `OpusApplication.adminRepository` builds `AdminApi`, Keystore-backed session
  storage, trust policy, personal markers, queue/review/audit services and
  bounded stats. Its `decisionsEnabled` is explicitly `false`; comments state
  that the legacy HTTP mapping must not send live decisions.
- `OpusApplication.canonicalAdminRepository()` is the sole production-source
  caller of the canonical repository factory; the other caller is an
  instrumented integration test. It is lazy, loads
  `community/admin-canonical-schema.json`, and uses `DisabledAdminTransport`
  unless an explicit test override is set.
- `CanonicalAdminClient` validates against that supplied schema, uses
  decimal-string revisions, binds authentication to an epoch, verifies audit
  walks, and requires explicit confirmation/reconciliation for uncertain
  writes. It does not reuse the legacy repository's persisted session/trust
  model or queue/review services.
- The canonical path shares only the low-level `AdminTransport` interface and
  disabled transport implementation with the legacy package. The app-level
  legacy repository also has active JVM tests; the canonical path has its own
  client/repository and application integration tests.
- `services/community-bff/deployment/cloudflare.config.ts` specifies a private,
  read-only catalogue deployment (`CATALOGUE_READ_ONLY=true`, Google OAuth
  disabled, Worker and Hyperdrive configured). The deployment README and Stage
  6 continuation describe a read-only deployment, but
  `services/community-bff/README.md` also contains an older statement that no
  Worker is deployed. Checked-in configuration/documentation does not verify
  current remote deployment state, operator use, account ownership, or data
  custody. No live endpoint, cloud account, credentials, or deployment was
  inspected.
- Existing tests cover disabled application transports, default-off sync
  settings, opt-in/consent checks, revocation, and canonical client/repository
  auth/cache/audit behavior (for example
  `CommunityLocalInstrumentedTest.applicationTransportStaysDisabledWithConfiguredEndpointAndCredentials`,
  `CommunityLocalInstrumentedTest.allOptInsAreOffAndWifiIsDefault`,
  `CanonicalApplicationIntegrationTest.applicationCanonicalServiceRemainsOfflineAndUnauthenticated`).
  These tests were not rerun specifically for this evaluation; the latest
  integration verification compiled instrumentation and ran the app JVM suite.

## Owner retirement decision — 2026-11-14

The owner superseded the earlier retain-both decision and authorized legacy
admin retirement. `MainActivity` no longer creates the legacy
`AdminViewModel`; legacy routes were removed from `AppNavigation` and `OpusApp`;
startup cancels persisted legacy periodic sync. The canonical
`data.community.canonical` client/repository and canonical `/v1/admin` BFF
contract remain. Production transport is disabled by default.

The old source packages and tests remain because three files contain pre-existing
uncommitted changes: `AdminSettingsStore.kt`, `AdminSyncWorker.kt`, and
`AdminViewModel.kt`. A filesystem safety review rejected deleting them under the
worktree-preservation requirement; no alternative deletion path was used. This
leaves obsolete code in the repository but without a supported app route.
Complete source/test retirement needs an owner decision on those edits. No
credentials, local settings, remote data, deployed services, BFF artifacts or
operator data were deleted.

## Limits

This is repository/source evidence, not live backend usage telemetry.
The checked-in BFF deployment configuration was read, but deployed state was
not validated. No server endpoint was contacted, no cloud account or secrets
were read, and no operator usage or data-custody evidence was available.

The canonical BFF is retained. External deployment and operator usage are
**unverified**; no remote resource was inspected or changed. The owner-approved
retirement is repository-side only, and deployment status is not claimed.
