# Gould 0.15 Stage 2 — checkpoint, 2026-11-15

## Catalogue category-switch cancellation fix — 2026-11-16

On `feature/gould-0.15`, the category effect already keyed on query/category,
so changing category did request a replacement. The race was in its lifecycle:
the obsolete effect unconditionally set shared `loading=false` in `finally`
and its collector updated shared results without a current-request check. A
late obsolete completion could therefore hide the replacement spinner or
replace the new category’s rows. Repository/provider cancellation and the
MusicBrainz gate were traced and left unchanged.

- Added `DiscoverSearchLifecycle`, a per-screen generation owner. Query,
  category, filter and retry changes invalidate the prior generation
  immediately. Each launched effect starts a fresh generation; result,
  failure and completion callbacks are applied only while that generation is
  current. The complete effect key (query, category, artist/period filters,
  online consent, retry revision and language) is also checked before applying
  state. Cancellation is rethrown, not marked as an error; only the owning
  request may finish loading or report failure. Provider-cache measurement is
  now inside the lifecycle’s try/finally boundary.
- Added seven deterministic coroutine-test-dispatcher tests using deferred
  fake repository/provider gates: All→Artists during local work; All→Works
  during online work; Works→Artists during ingestion; rapid All→Works→Artists;
  offline and cached switches; same-query/latest-category assertions; late
  obsolete emission and cleanup protection; failure/cancellation terminal
  state and successful follow-up. No sleeps or real provider requests.
- Verification command (offline, one worker, no clean task) passed:
  `:app:testDebugUnitTest :app:assembleDebugAndroidTest :app:lintDebug
  :app:lintRelease :app:assembleRelease`. JVM result: **338 tests, 0 failures,
  0 errors, 0 skipped** (the seven lifecycle regression tests passed).
  Android instrumentation sources compiled; instrumentation was not executed.
  `git diff --check` passed.
- Signed Release APK was written to the separate path
  `.gould-015-cancellation-build/app/outputs/apk/release/app-release.apk`:
  97,931,121 bytes, SHA-256
  `f3df006e36aa55fb940b5a938cc36737156c3ff77a5c960a9c5da10b53ade8ea`.
  Package/version `com.opus.piano`, `0.15.0-dev` / code 17; non-debuggable;
  APK v2 signature verified against trusted certificate SHA-256
  `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.
  Earlier signed and diagnostic APKs were not overwritten.
- No schema/data changes, provider changes, device access, Git integration or
  publication occurred. Owner A55 acceptance and any release approval remain
  pending.

## Final host verification and signed APK — 2026-11-16

The final verification pass was run on `feature/gould-0.15` at
`a908c96d6c3fa2712b77be1ad1e8941a1ac2e9da`. Existing dirty work and all prior
diagnostic/recovery/build artifacts were preserved. No Git integration,
publication, database migration, clean task, app uninstall, or data wipe ran.

- Migrated `DiscoverEditorialAcceptanceTest` from an active Open Opus fixture to
  an isolated deterministic MusicBrainz-ID test adapter/transport. It retains
  debounced search, query cancellation, offline/error paths, detail/composer
  navigation, catalogue-to-repertoire addition, evidence preservation, and
  responsive layout checks. The separately retained Open Opus fixture is read
  only to locate an already-imported legacy identity for a preservation check.
- Active `DiscoverRepository` constructs and filters search adapters to
  `musicbrainz`; candidate rows are filtered to that provider and
  `resolveCandidate` rejects all other providers before invoking the client.
  `OpenOpusAdapter` remains used by legacy import and old adapter-specific tests;
  Wikidata remains in existing provenance/read-only supplement logic and its
  adapter implementation. The separate `CatalogScreen` and legacy catalogue
  import path remain present. Existing rows, caches and provenance were not
  modified by this verification pass.
- Fresh command `JAVA_TOOL_OPTIONS="-Djava.io.tmpdir=$PWD/.gould-015-final-signed-build/tmp"
  GRADLE_USER_HOME=/home/stokonn/.gradle ./gradlew --offline --no-daemon
  --max-workers=1 -I gould-015-final-signed-verification.init.gradle
  -I docs/releases/goldberg-0.11-stable-signing.init.gradle
  :app:testDebugUnitTest :app:assembleDebugAndroidTest :app:lintDebug
  :app:lintRelease :app:assembleRelease` passed. Android test sources compiled;
  instrumentation did not execute. JVM task was then run uncached with
  `:app:testDebugUnitTest --rerun-tasks`: **331 tests, 0 failures, 0 errors,
  0 skipped**; `:activity-domain:jvmTest --rerun-tasks`: **13 tests, 0
  failures, 0 errors, 0 skipped**. `git diff --check`, `scripts/check_ui_literals.py`,
  `scripts/check_musicbrainz_catalogue.py`, and `scripts/check_curated_apk.py`
  passed. The check_ui_literals result was 7 audited legacy Text literals and
  no new raw UI strings. The MusicBrainz guard confirmed 100 composers, 105,032
  works and 105,421 composer relationships in the preserved snapshot.
- Android SDK inspection found existing AVD configuration files, but no
  running emulator. Instrumentation was **NOT EXECUTED** because safe target
  identity/data could not be established without device enumeration or starting
  an AVD; no ADB/device access occurred. Android-test APK compilation is not
  execution. No physical Galaxy A55 acceptance is claimed.
- Signed Release APK is at
  `.gould-015-final-signed-build/app/outputs/apk/release/app-release.apk`,
  **97,931,121 bytes**, SHA-256
  `67c24bf80a43683bef27a9e82e8df1936098919016867ac859285c467e205316`.
  It is `com.opus.piano`, version `0.15.0-dev` / code `17`,
  `debuggable=false`, ZIP-valid, with v2 signature verified against the
  existing trusted certificate SHA-256
  `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.
  Curated payload validation passed and found no diagnostic full-dump payload.
  The previous signed diagnostic APK and prior unsigned build remain untouched.
- Stage remains in progress pending owner installation/acceptance on the
  Samsung Galaxy A55 and any desired runtime acceptance. No commit, push,
  merge, tag, release, or APK publication occurred.

## Library, artist pages, and catalogue remediation — 2026-11-16

Current work remains on `feature/gould-0.15`; the owner’s pre-existing dirty
worktree was preserved. No commit, push, merge, tag, release, device access, or
database migration occurred.

- Library no longer renders “Your repertoire and the public catalogue.” The
  full-width equal Repertoire/Catalogue tabs remain 64 dp high; header and list
  spacing were reduced. The existing tab bounds/touch-height instrumentation
  remains, and its header assertion now verifies the subtitle is absent.
- Both catalogue search screens expose artist-scoped work search with clear and
  reset actions, deterministic localized Title A–Z / Title Z–A sorting, and
  available genre/work-type filters. Discover artist works query only local
  public catalogue rows by the selected artist relationship; pages remain
  bounded and personal repertoire is untouched.
- The active Discover adapter set defaults to and filters for MusicBrainz Core;
  non-MusicBrainz candidate rows are excluded and candidate resolution rejects
  Open Opus/Wikidata. Existing legacy adapters, imported rows, identities,
  provenance, synchronization, and the separate `CatalogScreen` route remain.
- `automaticSearch` reuses the initial offline page/candidates through the online
  debounce and avoids re-emitting the seeded initial page. Filters and history
  load independently of search completion, with their own retryable error UI.
  Search completion and seed reuse have explicit privacy-safe timing events.
- Final source verification: `git diff --check` PASS; `python3
  scripts/check_musicbrainz_catalogue.py` exit 0; `:app:testDebugUnitTest` PASS
  (**331 tests**, 0 failures/errors/skips); `:app:assembleDebugAndroidTest`
  PASS (compiled only); `:app:lintDebug`, `:app:lintRelease`, and
  `:app:assembleRelease` PASS. Command used the existing `/home` Gradle cache,
  one worker, offline mode, and a separate worktree build/schema/temp directory;
  no clean task ran.
- Updated the prior active-provider instrumentation assumptions in
  `Stage4CatalogueTest`, `Stage4PagingTest`, and `DiscoverRepairTest` to use
  MusicBrainz or assert rejection. AndroidTest compilation passes. The long
  `DiscoverEditorialAcceptanceTest` still has an Open Opus-backed online fixture
  and is not valid for execution under the new provider boundary until that
  fixture is migrated; it was compiled only and was not run.
- Release output is unsigned, `com.opus.piano`, `0.15.0-dev` / code 17,
  97,918,833 bytes, SHA-256
  `5a1bc8830c589267df1b9039c75cc02b704b49d266ce01cad6f2f8fb85ccea78`, at
  `.gould-015-remediation-build/app/outputs/apk/release/app-release-unsigned.apk`.
  No signing identity applies. The pre-existing signed diagnostic APK remains
  unchanged at SHA-256
  `da2760795b835ad7c2f8ae2bd68e0428b336b92bb2d01184f6ce249e705fc54f`.
- Immutable host SQL comparison of All/Works/Artists against the preserved
  catalogue DB: repeat medians **0.575 / 0.571 / 0.361 ms**, respectively, for
  a Chopin query (host-only, no result mapping/provider/Compose time). Existing
  FTS and entity/redirect indexes remain in use; no speculative SQL change was
  made. No Galaxy A55 or Android instrumentation timing was collected.
- Remaining: Android instrumentation execution, owner visual/accessibility and
  installed-upgrade acceptance; A55 catalogue timings. The owner's prior
  2026-11-15 exploratory report said search remained empty/skeleton before this
  remediation; it is preserved as prior evidence and was not repeated. No
  issue/project sync is claimed. Stage remains in progress.

## Owner takeover — catalogue implementation and Stage 3 verification (2026-11-15)

Current state: Stage 2 implementation is complete; Stage 3 verification is in
progress and cannot be declared complete while runtime acceptance remains
unverified. Branch `feature/gould-0.15`, HEAD
`a908c96d6c3fa2712b77be1ad1e8941a1ac2e9da`; the existing dirty worktree and
all unrelated changes remain preserved. No other checkout/worktree was
accessed and no Git integration or publication action occurred.

- Full-dump database, superseded gzip payload and diagnostic APK were verified
  at the reported paths against their expected SHA-256 values. They remain
  preserved outside packaged assets.
- Curated seed: 100 unique verified MusicBrainz MBIDs; 105,032 real Core works
  and 105,421 composer-credit relationships. SQLite size 283,987,968 bytes;
  packaged gzip size 89,123,721 bytes. Integrity, FK, selection, archive and
  APK content checks pass. Exact selection is in `CATALOGUE-SELECTION.md` and
  `app/src/main/assets/catalogue/classical-composers.json`.
- Direct MusicBrainz HTTPS uses the existing adapter, centralized 1,100 ms
  provider interval, pagination, cache and MBID reconciliation. Gould 0.15
  User-Agent is set. Discover uses one search field and one merged, deduplicated
  list. A bounded public API request succeeded; this is not app runtime proof.
- `scripts/verify-release.sh` passed at 2026-11-15 12:16:53 UTC (115 s),
  including clean debug build, 325 JVM tests at that run, Android-test
  compilation, lint and seven policy checks. After the added provider policy
  test, focused checks and the complete app JVM task are up to date/passed;
  result XML reports 327 tests, 0 failures/errors/skips. `git diff --check` and
  `scripts/check_curated_catalogue.py` pass. The separate activity-domain task
  passed 13/13.
- Signed Release APK: `app/build/outputs/apk/release/app-release.apk`,
  97,910,513 bytes, SHA-256
  `fa2a57025a8c14e0b000ce0462927244834aa5485b53007460f8d3957641bcae`.
  Package `com.opus.piano`, version `0.15.0-dev` / code 17, non-debuggable;
  v2 signature verifies with trusted certificate SHA-256
  `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.
  ZIP inspection finds only the curated catalogue payload (89,123,721 bytes)
  and selection JSON; payload decompresses to the expected DB hash. Full dump
  and superseded payload are absent. `scripts/check_curated_apk.py` passes.
- Runtime search, cached-offline replay, detail loading and repertoire-add UI
  were not exercised end-to-end. No emulator was running; no physical device
  was accessed. Android instrumentation compiled only. These remain Stage 3
  acceptance gaps; no stage is marked complete pending those checks.
- GitHub Issue/Project synchronization is pending because no existing Gould
  0.15 Stage issue was found; no duplicate issue was created.

## Final Stage 2 continuation — 2026-11-15

The owner-directed implementation and host verification are complete on
`feature/gould-0.15` at `a908c96d6c3fa2712b77be1ad1e8941a1ac2e9da`; all existing
dirty work was preserved. A recovery copy of tracked/staged diffs, untracked
files, status, branch and HEAD is verified at
`/tmp/gould-015-stage2-recovery-20261115/` (tracked patch SHA-256
`dcec10361e7eae7efdc9f7a438481edc60f71f654789b2f29e1a7254c069c3ee`, untracked
archive SHA-256 `838612d01153958dae36ae14ae90a909efa072a6a3d4c8d220918cb836792218`).

- **Admin retirement:** legacy Android admin implementation, UI, app wiring,
  worker/repository/settings/transports, and obsolete tests are removed. The
  canonical client/repository and required BFF/contract remain; canonical
  transport is independent of `com.opus.piano.admin` and disabled by default.
  Upgrade startup cancels the persisted legacy WorkManager unique job and
  notification. No production request was made. Guard: `scripts/check_admin_retirement.py`.
- **Current host checks:** `scripts/verify-integration.sh` passed on the current
  source: full JVM suite, lint, and Android instrumentation APK compilation.
  Current result XML independently reports app JVM **325 tests, 0 failures,
  0 errors, 0 skipped**; `:activity-domain:jvmTest` reports **13 tests, 0
  failures, 0 errors, 0 skipped**. Instrumentation was compiled only. Static
  checks passed: admin retirement; migrations v1–v13; backup fixtures 1–3;
  localization 1,223 EN/CZ resources; agent policy; history paging; UI literals;
  catalogue generator 8/8; publication workflow policy 9/9; and `git diff --check`.
- **Release-variant testing APK:** clean `:app:assembleRelease` and
  `:app:lintRelease` passed. Current artifact is
  `app/build/outputs/apk/release/app-release.apk`, package `com.opus.piano`,
  version `0.15.0-dev` / code 17, 8,780,414 bytes, SHA-256
  `221340a389bf39478277d5cbf7d36966fe9596e2bfcec13b09840ef882cbcb85`.
  APK v2 signature verifies against trusted certificate SHA-256
  `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`; ZIP
  integrity passes and Release manifest has no debuggable flag. This is an
  owner-testing artifact, not a published release.
- **MusicBrainz bounded blocker:** independent full-fingerprint authentication
  is still unresolved. GPG in an isolated keyring verified the signed checksum
  list cryptographically with a matching full-fingerprint key fetched from an
  Ubuntu keyserver, but reported the key as uncertified; that keyserver result
  does not authenticate MetaBrainz identity. A mistakenly started archive
  transfer was stopped at 3,120,558,080 bytes; the partial file was then
  removed. It was not verified, extracted or processed. No records/counts are
  claimed.
  Exact owner action: obtain independent full-fingerprint confirmation from
  MetaBrainz through an official support/security channel. Do not resume or
  restart download until confirmed; if normal-policy signature verification
  fails, request a supported replacement key/signing procedure. Production
  catalogue import also requires separate owner approval.
- **Status: STAGE 2 COMPLETE** for the authorized implementation scope. The
  external trust gate blocks catalogue data inspection only. Galaxy A55 visual,
  accessibility, upgrade and runtime acceptance remain for the owner; Android
  instrumentation was not executed. Stage 3 has not started. No commit, push,
  merge, tag, PR, publication or external infrastructure change occurred.

This section supersedes conflicting older statements below (including the
previous 426-test count, stale APK details, “preserve both admin systems”, and
claims that no archive bytes were downloaded). Those entries remain historical
evidence and are not current status.

## Admin retirement continuation — 2026-11-15

Owner-directed continuation supersedes earlier Stage 2 notes that retained
both Android admin clients. The legacy `com.opus.piano.admin` implementation
and `ui.admin` screens, their app wiring, unit tests, and Stage 7B runtime tests
have been removed. `AppContainer` and `OpusApplication` no longer instantiate
legacy repositories/settings/sessions/transports/workers. The canonical client,
repository, contract asset, and existing BFF deployment remain. Canonical HTTP
exchange now has a canonical-package interface and remains disabled by default.
Upgrade startup cancels the persisted unique WorkManager job by name and its old
notification ID. `scripts/check_admin_retirement.py` passes and is included in
quality CI. Focused JVM verification passed: 325 tests, no failures/errors/skips;
app and Android-test Kotlin compilation passed. The retirement/localization guards
and `git diff --check` passed. Instrumentation was compiled, not executed. No
production networking or external backend operation occurred. Stage 3 has not started.

The current checkpoint below is authoritative. Subsequent dated entries are a
chronological implementation/evidence log; older statements of open Stage 2
work are superseded by the current Stage 2 COMPLETE status below.

## Current implementation checkpoint — 2026-11-14

Branch `feature/gould-0.15`, baseline/HEAD `a908c96`; owner worktree changes
remain dirty and preserved. This batch completed the safe local draft-recovery
path, hardened catalogue generator/installer boundaries, added publication
workflow policy validation, and completed owner-directed Android legacy admin
retirement. No external service or publication action occurred.

- **Community drafts:** added bounded (16 MiB) version-1 JSON archive export
  and recovery through the Settings SAF document picker. It is separate from
  backup formats 1–8 and is not encrypted. Export preserves IDs, editable and
  intentionally incomplete content, pending exact request bodies/keys/retry
  outcomes and receipts. Import validates everything before writing, requires
  exact-principal scope or guest-to-current-guest rebinding, rejects duplicate
  and already-present IDs as a whole, and stages/rolls back publication.
  `COMMUNITY-DRAFT-RECOVERY.md` records behavior/limits; focused tests cover
  successful round trips, mismatch/malformed/incomplete/duplicate/conflict,
  empty archive, and an injected mid-publication fault.
- **Catalogue:** generator schema/policy inputs reject unknown fields (including
  MusicBrainz supplementary fields) and fixture providers outside synthetic
  test mode; production mode now accepts only MusicBrainz Core source records.
  Installer validates SQLite through `LocalCatalogueStore` before
  marking a manifest READY and promotes higher revisions only after successful
  atomic local installation. It rejects non-MusicBrainz sources, non-CC0 rights,
  unsupported assertion fields, documents, and media. Added instrumentation for invalid hash/size/DB,
  active-generation retention, stable personal identity, anchor and private
  title preservation, fixture-provider rejection, and supplementary metadata
  rejection. The instrumentation source compiled, but did not execute.
  No MusicBrainz archive/key was acquired, no real data processed, and no
  production catalogue generated/imported.
- **Publication:** read-only permissions, no PR secrets and no release action
  or publication command found in current Actions workflows. Added
  `check_publication_workflows.py` plus nine policy tests to `quality.yml`.
  The guard requires explicit read-only permissions and non-persistent checkout
  credentials; it rejects write-all/write scopes, secrets,
  `pull_request_target`, and release commands/actions. No publishing workflow,
  credential, or release was created.
- **Admin/backend:** the Android legacy client/UI, app wiring, tests and worker
  were retired per the owner's current decision. The canonical client/repository,
  contract and existing BFF components remain. The canonical transport has its
  own interface and fails closed by default; no production networking was added.
  The prior source-only evaluation remains historical evidence.
- **Verification:** final
  `GRADLE_USER_HOME=/tmp/gould-015-gradle-home scripts/verify-integration.sh`
  final PASS in 6 s at `2026-11-14T22:28:12Z`: 426 app JVM tests (0 failures/errors,
  0 skipped), lint, and Android test APK compilation. Android instrumentation
  was not executed. Audit source SHA-256 is
  `f52eb5bf2adf54451394cbefbc8368f437d63dc82ba0454f12374dec8b332661`
  (`a908c96-dirty`).
  Earlier attempts exposed and fixed a compile error and rollback cleanup
  defect; the final run passed.
  The final host checks passed: catalogue 8/8, publication 9/9,
  localization 1322 resources, migrations v1–v13 DDL, backup fixtures formats
  1–3, history paging, UI literals, agent policy, and `git diff --check`.
- **Status: STAGE 2 COMPLETE.** Authorized implementation, focused tests, and
  feasible host verification are complete. The MusicBrainz key and real
  snapshot/provenance remain unverified; the inquiry is prepared but unsent.
  Production catalogue generation/import still requires those prerequisites
  and separate owner approval, but does not block catalogue infrastructure or
  Stage 2 closure. Galaxy A55 widget/tour acceptance and Android instrumentation
  execution remain unperformed owner/runtime checks. Stage 3 and publication
  remain unauthorized; no Git integration action occurred. Remote GitHub
  Issue/Project synchronization remains pending because the configured token
  was previously invalid; local records are current and no remote sync is
  claimed. This administrative sync does not block implementation completion.

## Latest update — independent Stage 2 work areas reviewed and verified

Branch `feature/gould-0.15`, HEAD `a908c96`; all pre-existing dirty work is
preserved. This batch only changed the catalogue generator/tests, widget
renderer/instrumentation assertion, and a streak-rule KDoc; the guided tour
review required no change.

- **Catalogue infrastructure:** strict field allowlists reject unapproved
  fields across every generator input boundary, including MusicBrainz
  annotations, genres, ratings, tags, and popularity. Production mode rejects
  synthetic-test marker and fixture-provider data. Test asserts generated
  catalogue UUID and source external ID are preserved. Existing installer
  verifies package hashes/schema and uses separate catalogue DB; personal
  identity preservation is source reviewed, not runtime tested. Python tests
  passed 7/7. No real data, keys, source archive, or production package used.
- **Activity/streak/rewards:** verified shared `goalMet` semantics for both
  session and minute goals across weekly UI, current/longest streak, week close,
  Freeze and rewards. Profile/Home streaks are intentionally daily. Beta weeks
  can award cosmetics but never advance streak/Freeze. Corrected only a
  misleading `perfectWeek` KDoc; existing mixed-goal, Beta, Freeze, reward
  idempotency and retrospective-correction coverage remains. App JVM suite
  passed; `:activity-domain:jvmTest` reported PASS from Gradle cache.
- **Widget:** weekday chart labels now use semantic `onSurfaceVariant` rather
  than white, fixing light-theme contrast. Added actual rendered-bitmap color
  assertion in `WidgetTest`. Source review confirmed activity/cosmetic table
  invalidation and locale refresh paths. Assertion compiled but native test and
  visual render were not executed. Owner Galaxy A55 acceptance remains pending.
- **Guided tour:** six sections, persistence-before-navigation, interruption
  resume/replay, route integration, responsive action stacking, accessibility
  semantics and Czech/English resources reviewed. No defect demonstrated and
  no source change made. JVM progress tests ran; instrumentation compiled but
  was not executed, so Android process death/TalkBack journeys remain unverified.
- **Verification:**
  `GRADLE_USER_HOME=/tmp/gould-015-gradle-home scripts/verify-integration.sh`
  PASS, 87 s, 2026-11-14 21:33:54 UTC: full app JVM suite, lint and
  `assembleDebugAndroidTest`. Source SHA-256
  `9c7cc47c263abd75b01d66af9e966f0c2016016589f847d0fcc0a69a313cd203`;
  audit row binds `a908c96-dirty`. Direct activity-domain JVM task PASS from
  cache. `python3 scripts/test_catalogue_generator.py` 7/7; Python byte-compile;
  localization check (1315 EN/CZ resources); UI-literal check (8 audited
  existing literals); and `git diff --check` passed. Initial sandbox Gradle
  attempt failed before build startup due unusable wildcard IP; serialized
  approved retry passed. No emulator or phone was accessed.
- **Still blocked:** independent authentication/current-policy verification of
  the MusicBrainz signing key. The owner-approved inquiry to
  `support@metabrainz.org` is drafted and unsent. No MusicBrainz archive or
  production catalogue generation/import. Separate final owner approval for
  production import is still required after successful inspection. Static
  publication workflow, safe community-draft export/recovery and operational
  admin/backend disposition remain pending. Stage 2 is not complete; Stage 3
  remains unauthorized. No commits/pushes/merges/tags/releases/PRs.

## Latest update — support inquiry prepared for owner review (2026-11-14)

Prepared an unsent technical inquiry about the signing key and snapshot
`20261114-002230` (Core archive expected SHA-256
`da4f1afcbca8ca54eb6362ae45173543f079e8491d097a376d060551d1a03335`). The
recommended destination is `support@metabrainz.org`, listed as the direct
MetaBrainz staff address on <https://metabrainz.org/contact>; request routing to
the MusicBrainz dump/database maintainer. The draft asks for current full
fingerprint and support status of `C777580F`, clarification of MBS-14311's
SHA-1 certificate concern, trusted key distribution/replacement procedure, and
availability of a newer signature format for this snapshot. Full draft is in
`CATALOGUE-PROVENANCE-POLICY.md`.

**Not sent:** external contact requires separate owner approval. No archive or
key retrieval, catalogue processing, or production/personal data modification
occurred. Stage 2 remains blocked at key authentication.

## Latest update — signing-key investigation remains blocked (2026-11-14)

Owner-approved investigation consulted the official
`metabrainz/musicbrainz-server` INSTALL instructions, official MusicBrainz dump
verification documentation, and the live MBS-14311 ticket. The GitHub INSTALL
instructions still direct consumers to short key ID `C777580F`; they do not
provide a full fingerprint or announce a replacement. Official dump docs
publish full fingerprint
`D5E63B4BDCCE195642948684B8FC2375C777580F`. MBS-14311 exists and showed
In Progress / Unresolved, no fix version, updated 2026-11-08 12:05 UTC. Its
2026-03-28 reporter-submitted description repeats that fingerprint, names RSA
2048 and subkey
`3CA330EE23B47A79AB76FA3619AF6A3243513089`, and includes a Sequoia report
rejecting SHA-1 certificate/binding signatures. This is not a maintainer
resolution; ticket activity/comments were not exposed in the inspected page.

GnuPG 2.4.9/libgcrypt 1.12.2 parsed both held detached signature files as v4
RSA with SHA-512 digest (algorithm 10) and issuer fingerprint
`D5E63B4BDCCE195642948684B8FC2375C777580F`. This only describes packet data,
not validity/authentication. The independent full-fingerprint confirmation is
still inadequate: the official repo gives only the key ID; the ticket gives
full fingerprint/crypto evidence only through its outside reporter. No
independent maintained confirmation or maintainer announcement/replacement was
located in the bounded sources checked. Ubuntu keyserver retrieval failed and
the restricted network could not resolve it; keys.openpgp.org fingerprint
lookup had previously returned 404. The isolated keyring remains empty; GPG
verification fails `Can't check signature: No public key`. The reported SHA-1
certificate issue cannot be tested with current GPG without the authenticated
key, and no override/global policy changes were used. GitHub file history could
not be fetched due a web cache miss; no historical-commit claim is made.

**Blocker/next owner action:** provide or identify a MetaBrainz-maintainer-
confirmed public-key distribution, plus a separate trustworthy full-fingerprint
confirmation (or maintainer-signed announcement/artifact). Then verify using
the isolated current GPG installation and normal policy. If rejected, request
a replacement key/signature method from MetaBrainz. No archive was downloaded,
no catalogue data processed, and production generation/import remains outside
this inspection authorization. Details/source URLs are recorded in
`CATALOGUE-PROVENANCE-POLICY.md`; checklist updated. `git diff --check` passed.

## Latest update — authorized MusicBrainz Core snapshot inspection blocked at verification

The owner authorized inspection of one snapshot only. The official full-export
index was previously inspected and identified snapshot `20261114-002230`,
published in the index at 2026-11-14 05:10 UTC. The official directory is
`https://ftp.musicbrainz.org/pub/musicbrainz/data/fullexport/20261114-002230/`;
the main Core file is `mbdump.tar.bz2` (directory listing timestamp
2026-11-14 01:47) and the listed checksum is
`da4f1afcbca8ca54eb6362ae45173543f079e8491d097a376d060551d1a03335`.
The official signer fingerprint recorded in the existing research is
`D5E63B4BDCCE195642948684B8FC2375C777580F`.

Only verification metadata was downloaded to the isolated directory
`/tmp/gould-015-mb-inspection-20261114/`: `SHA256SUMS`,
`SHA256SUMS.asc`, and `mbdump.tar.bz2.asc`. Their local SHA-256 values are,
respectively, `e09d4470c42548ef73f8d1cf3762af9f1bc9b03a74a4370a1be531887a953f09`,
`7ff8b1de19df6787ff1861464cb9157564c8fe883456f43af552fdad7cb8f001`, and
`ec0016687eb4657d15c6f9ae41a9f666445cd2469bc4a0c71db532fecacd41c1`.
Attempted GPG verification of the signed checksum file failed because the
isolated keyring has no public key. Keyserver retrieval failed and a subsequent
keyserver URL could not resolve in this network-restricted environment. The
signature's displayed key ID matches the documented fingerprint, but this is
not cryptographic verification. No public key was authenticated, no archive
was downloaded, and no checksum was computed for the archive. The archive
signature is likewise unverified. Processing stopped at the mandatory gate.

No Core, derived, CD stubs, or supplementary data archive was downloaded,
extracted, or mixed into production inputs. No records were inspected; record
counts, composer relationship counts, duplicates, piano candidates, and
examples are therefore unavailable, not zero. No generator/importer or
personal data was changed. **Next owner action:** provide an authenticated
MusicBrainz signing public key (or authorize a network path to an independently
verifiable official key distribution), after which this exact snapshot's
checksum-list signature and archive signature can be checked before acquiring
the Core archive. The inspection-only authorization does not authorize
production generation/import; that still requires separate approval.

The checklist and provenance policy now record this blocked inspection. Stage
2 remains incomplete. No commit, push, merge, PR, tag, release, production
catalogue generation/import, or Stage 3 action occurred.

## Latest update — catalogue source/provenance review

The existing history, source research, assets, local credits, provider adapters,
fixture assets and PR #40 proposal were inspected. No production source data was
downloaded, ingested, generated or published.

- The only catalogue database fixture is authored fiction (26 entities). The
  24 local factual projections in Goldberg 0.11's `provider-facts.json` are 23
  MusicBrainz search candidates and one Open Opus work observation, explicitly
  not verified canonical identities or cross-provider mappings. Their
  observation log includes request hashes/times but says raw response bodies
  were not committed. These facts are not a complete or reproducible source
  snapshot.
- Upstream rights evidence: Open Opus states its data is public domain and
  documents `/work/dump.json`; its API code is GPLv3 separately. It says its
  starting lists included material scraped from Wikipedia/IMSLP, and its dump
  example does not show stable per-record IDs. MusicBrainz documents
  `mbdump.tar.bz2` core data as CC0, separates supplementary CC BY-NC-SA 3.0
  files/live feed, creates full snapshots twice weekly, and documents
  SHA256SUMS plus GPG signatures. Wikidata structured entity data is CC0 and
  weekly JSON dumps are documented, but dumps are very large and no local QID
  matches were verified. Wikipedia prose normally carries CC BY-SA 4.0/GFDL
  and per-page history/change attribution; Commons rights remain per-file.
- Prepared `CATALOGUE-PROVENANCE-POLICY.md`, a concrete CC0-only policy draft
  aligned with `catalogue_generator.py` input and external policy fields. It
  recommends evaluating MusicBrainz `mbdump.tar.bz2` core-only first, excludes
  supplementary/derived data, and requires a single accurate CC0 package-file
  license, source MBIDs, per-record hashes, retrieval/revision details and
  courtesy attribution. This is a proposal, not a license metadata artifact or
  source approval.
- **Owner decision required:** select (or reject) MusicBrainz core-only vs
  Open Opus full dump; approve the initial record kinds/field allowlist and
  attribution text; explicitly authorize provisioning/acquiring the exact
  snapshot; then separately approve ingestion after snapshot/signature/hash
  and record-level extraction review. If neither upstream rights statement
  resolves the source-lineage concern, owner must supply written rights
  confirmation or legal guidance. No exact full snapshot SHA can be recorded
  until it is approved and supplied/acquired.
- Stage 2 remains blocked at Work Block 1. Generator/importer remain unchanged.
  No licenses/metadata were applied, no upstream was contacted beyond reading
  public documentation, and there were no commits, pushes, merges, PRs, tags,
  releases, or Stage 3 actions.

## Latest update — Work Block 1 catalogue infrastructure (2026-11-14)

Resumed from the existing Stage 2 decisions without changing scope. Repository
evidence confirmed Room schema 1 and the existing offline Bundle manifest
format 1. Local commit `b0d7c4b` is only the PR #40 source/license proposal and
explicitly grants no approval. No verified real input set or record-level
rights policy is available, and no licence metadata was added to the
repository. No catalogue records were fabricated.

- Added `scripts/catalogue_generator.py`. It reads the exported Room schema,
  requires the exact source input SHA-256 in an external review policy, stable
  canonical UUIDs, matching per-record payload/source pins, approved supported
  rights, HTTPS sources/license URLs, and package-file attribution/rights. It
  creates deterministic schema-1 SQLite and the existing Bundle manifest v1.
  Synthetic inputs are rejected unless the explicit test-only CLI switch is
  used. No review policy or real catalogue data was created.
- Added `scripts/test_catalogue_generator.py`: five tests pass for repeatable
  DB/manifest bytes, schema/version/integrity, FTS aliases and canonical
  normalization, provenance/rights rejection, source hash pinning and the
  synthetic-input gate.
- Added `CatalogueBundleInstaller` to import the generated manifest/database
  through existing BundleStore content-addressed storage, then install only a
  published READY COLLECTION/schema-1 package. `LocalCatalogueStore` checks
  blob hash/length, Room open/schema, SQLite integrity/foreign keys, and
  approved title evidence; it merges personal catalogue identities and swaps
  the current generation through the existing AtomicFile pointer. A collision
  between a catalogue ID and a personal ID with different kinds is rejected.
  `opus.db`, repertoire anchors, Google identity, backup code/formats and
  schema 1 were not changed.
- `python3 -m py_compile scripts/catalogue_generator.py
  scripts/test_catalogue_generator.py`, the five focused Python tests,
  `scripts/check_migrations.py`, `scripts/check_backup_fixtures.py`, and
  `git diff --check` passed. `:app:compileDebugKotlin` passed after the first
  Gradle sandbox preflight failed before build startup; retry used the
  approved execution path. Only existing unrelated Kotlin warnings appeared.
- Android runtime installation, personal-bridge behavior, Google Identity and
  full formats 1–8 runtime compatibility were not exercised in this block.
  The Android install path is compile-verified only. Community drafts,
  credentials, caches, admin/BFF systems and operator data remain untouched.
- Stage 2 Work Block 1 remains incomplete. Exact blocker: owner-approved,
  redistributable real source records with field/record-level provenance and
  compatible license evidence are unavailable. Also pending: static package
  publication workflow review and safe draft export/recovery plus verified
  admin/backend operational disposition. Next step: provide and separately
  approve the pinned source dataset and policy; then run production generator,
  import a representative package on a disposable Android test target, and
  verify identity/repertoire/personal-data preservation before final Stage 2
  gates. No Stage 3, commit, push, merge, PR, tag or release action occurred.

## Latest update — weekly widget layout repair (2026-11-14)

The owner-provided Galaxy A55 Home screenshot
`/tmp/gould-015-owner-screenshots/Screenshot_20261113_181702_One UI Home.png`
was inspected. The compact widget's weighted `ImageView` used `fitXY` while
the chart bitmap was generated at a fixed 64 dp. At the tall launcher size the
host scaled that bitmap into the much taller remaining view: bars and weekday
glyphs were vertically stretched, and the labels sat far below the chart. The
wide layout also generated a bitmap wider than its weighted chart column. The
vertical composition also needs to reserve the summary's height on medium
widgets, so chart sizing accounts for that header space.

The renderer now sizes the bitmap from available widget height and actual
chart-column width; both weekly layouts let the image wrap its bitmap height.
Cell height remains proportional to the day slot. Day initials use one or two
localized letters according to slot width and scale with Android font scale;
labels are omitted only if the chart itself is too small to render them
readably. Projection tests cover layout thresholds, chart heights, label
visibility/abbreviation and large font scale.

- FAST PASS:
  `GRADLE_USER_HOME=/tmp/gould-015-gradle-home scripts/verify-fast.sh --tests 'WidgetProjectionTest'`
  — Kotlin compilation, focused JVM tests and all FAST static checks passed.
- INTEGRATION PASS:
  `GRADLE_USER_HOME=/tmp/gould-015-gradle-home scripts/verify-integration.sh`
  — full JVM suite, lint and Android-test compilation passed. Instrumentation
  was compiled only; it was not executed.
- Fresh signed Release testing APK and `lintRelease` PASS:
  `GRADLE_USER_HOME=/tmp/gould-015-gradle-home ./gradlew -I docs/releases/goldberg-0.11-stable-signing.init.gradle :app:clean :app:assembleRelease :app:lintRelease --no-daemon --max-workers=1 --no-parallel`.
  Artifact `app/build/outputs/apk/release/app-release.apk`; package
  `com.opus.piano`, version `0.15.0-dev` (code 17), minSdk 30, targetSdk 37,
  non-debuggable, 8,971,594 bytes. ZIP and v2 signature verified. Trusted
  signer SHA-256:
  `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.
  APK SHA-256:
  `72e85916b2f28746c8bea90999d29e9d68c5fde5ff405a2c0e4add3ccf500dd8`.
  It is a local testing artifact only; no install, release or Stage 3 action.
- Emulator QA was attempted only for this widget. The existing API 37 AVD
  (`1080×2400`, 420 dpi; nominal 411×914 dp) exited with code 139 before ADB
  connected. Retries with GPU off, the window enabled, and the existing API 36
  AVD also exited 139. No widget was installed/rendered; no emulator viewport
  or font scale was visually tested and no after screenshot exists. The owner
  Galaxy A55 visual check remains outstanding. No phone access occurred.
- No commits, pushes, merges, PRs, tags, publication, cleanup or unrelated
  source changes were made. The worktree's pre-existing dirty state remains.
  Stage 2 remains incomplete due to Work Block 1; Stage 3 is unauthorized.

The passing FAST/INTEGRATION audit rows bind source SHA-256
`fa4c8a01fefca114d3098d0f47e3e81e7937c08d5ce7a93319f345b3bbec4160` at
`a908c96-dirty`.

This update supersedes the earlier widget-specific statements below that no
emulator attempt had occurred. Full context/task boundaries remain in the
existing Stage 2 handoff.

## Latest host verification and testing APK — 2026-11-14

After the screenshot-driven widget and guided-tour fixes:

- FAST PASS, 15:20:01 UTC: `GRADLE_USER_HOME=/tmp/gould-015-gradle-home
  scripts/verify-fast.sh --tests 'WidgetProjectionTest'`. Kotlin compile,
  focused JVM test and all FAST static checks passed. Source SHA-256
  `48c382c65cd1f921e23a2b5a2224372a7cc9a453a4c7b55c1b45c6281e25d160`.
- INTEGRATION PASS, 15:23:00 UTC, 57 s: `GRADLE_USER_HOME=/tmp/gould-015-gradle-home
  scripts/verify-integration.sh`. Full JVM suite, lint and
  `assembleDebugAndroidTest` compilation passed. No instrumentation executed.
- RELEASE host gate PASS, 15:23:31 UTC, 17 s:
  `GRADLE_USER_HOME=/tmp/gould-015-gradle-home scripts/verify-release.sh`.
  Mandatory clean host build/unit/lint/androidTest compilation and six policy
  checks passed. Native instrumentation and release-specific security/upgrade
  tests were not run.
- Release-variant testing APK PASS, build/lint command:
  `GRADLE_USER_HOME=/tmp/gould-015-gradle-home ./gradlew -I
  docs/releases/goldberg-0.11-stable-signing.init.gradle :app:clean
  :app:assembleRelease :app:lintRelease --no-daemon --max-workers=1
  --no-parallel` (1m40s). Artifact:
  `app/build/outputs/apk/release/app-release.apk`; package
  `com.opus.piano`, version `0.15.0-dev` (code 17), minSdk 30, targetSdk 37,
  `debuggable=false`, 8,971,626 bytes. ZIP test and APK v2 signature passed;
  signer certificate SHA-256 matches trusted identity
  `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.
  APK SHA-256:
  `e15b8876c8d1977723d047cf21834a07e0f434aa8fd7f9fc9999b43e72874a05`.
  Owner checklist updated. This testing artifact does not complete Stage 2 or
  authorize publication. No install or upgrade was run.
- `git diff --check` passed. Branch remains `feature/gould-0.15` at
  `a908c96d6c3fa2712b77be1ad1e8941a1ac2e9da`, with all prior dirty work
  preserved. Verification audit rows are bound to the dirty source above.

Stage 2 is **incomplete** because Work Block 1 remains unresolved. No automated
package generator or importer path connects the content-addressed bundle store
to `LocalCatalogueStore`. No approved redistribution-ready record set with
verified per-record sources/licences is present; planned GPL/CC terms are not
license authorization. Community submission/draft code, data and admin/BFF
systems were not retired. Persistent drafts remain user data and have no safe
export/recovery conversion; independent admin/operator use is not established.
Google identity is separately composed through `AppContainer.identity` and was
not changed, but its runtime flow was not tested in this host-only run. Preserve
schema 1, existing package format, separate Room databases, backups 5/8 and all
drafts; do not claim end-to-end catalogue distribution or backend retirement.
The exact evidence and screenshot mapping are in the continuation update below.

No emulator/ADB, owner phone, commit, push, merge, PR, tag, release publication,
or Stage 3 action was used. GitHub Issue/Project sync was not retried; earlier
checkpoint records the invalid configured token.

## Continuation update — screenshot review and widget layout (2026-11-14)

The owner reauthorized continuing the existing Stage 2 task and supplied five
screenshots in `~/Downloads/screen015/`. All five were inspected read-only:

- `Screenshot_20261114_161119_One UI Home.png`: resized weekly activity widget
  has seven very tall stretched day bars. `WidgetProjection.widgetHeatmapCellSize`
  now caps cell height relative to the day slot and uses a consistent gap;
  the host layout test covers narrow, wide, short and tall dimensions.
- `Screenshot_20261114_161128_Gould.png`: Activity goal/reward and week cells
  are centered or intentionally start-aligned as designed; no alignment defect
  was confirmed in this capture, so no unrelated Activity redesign was made.
- `Screenshot_20261114_161136_Gould.png`: Home statistic labels, chart and
  recent-session rows use their intended centered/left/right alignments. Long
  session titles remain exposed by their existing detail interaction; no
  screenshot-specific code defect was confirmed.
- `Screenshot_20261114_161146_Gould.png`: recently unlocked badges are a
  horizontally scrollable card row; the clipped next card is intentional and
  remains tappable for full detail. Collection grid titles already use shared
  measured slots and centered text; no additional change was justified.
- `Screenshot_20261114_161229_Gould.png`: at this phone width, the Czech tour
  primary action wrapped its final character. Guided-tour actions now stack at
  narrow widths or font scales above 1.3, preserving full labels and touch
  targets.

The screenshots were not modified, copied into the repository, or used as
physical-device acceptance evidence. Focused FAST passed on the changed source
after the environment retry: `GRADLE_USER_HOME=/tmp/gould-015-gradle-home
scripts/verify-fast.sh --tests 'WidgetProjectionTest'`, 2026-11-14 15:20:01 UTC.
Kotlin compilation, `WidgetProjectionTest`, and all FAST static checks passed.
The first sandbox attempts failed before Gradle startup (wildcard-IP lock
service); the escalated run with a combined class-filter expression compiled
but matched no tests; the corrected class-only run passed. All attempts remain
in the audit. No emulator or phone was used.

Stage 2 remains in progress. Catalogue generation-to-install is still not
implemented. The exact blocker is absence of verified record-level source and
license metadata / an approved redistributable input set; the owner also has
not separately authorized publishing license metadata. Community drafts and
submission UI remain preserved because `community-v1/drafts` is persistent
user-owned state and no safe export/recovery migration is implemented. Admin
and BFF components remain because the repository does not establish their
operational use or that they serve only the retired community path. Existing
Google identity remains separately wired through `AppContainer.identity`;
no identity code was changed. Do not claim Work Block 1 complete or use an APK
built before these source changes as current.

No commit, push, merge, PR, tag, GitHub publication, Stage 3 work, or emulator
operation occurred. The owner requests a meaningful logical commit history
only after Stage 2 completion and explicit commit authorization.

## Latest continuation — owner master directive

The original Stage 2 batches and host acceptance recorded below remain
historical evidence for their recorded source hash. The owner's current master
directive adds two remaining Stage 2 work blocks; Stage 2 is now **in progress**
and the prior release-variant APK is stale for source changed in this
continuation. Stage 3 remains unauthorized. No commit, push, merge, tag, PR or
publication was performed.

- Branch remains `feature/gould-0.15`; worktree remains dirty with the owner's
  pre-existing changes preserved.
- Work Block 2 changes: `DiscoverScreen` measures the tallest recent-card title
  at the current locale/font scale and reserves the same two-line slot for
  every visible card. Weekly widgets use a horizontal summary/chart layout only
  when both dimensions meet the wide-layout threshold and font scale is at
  most 1.5; otherwise the existing compact layout is used. Full recent titles
  remain available from the existing card detail action.
- Verification: `GRADLE_USER_HOME=/tmp/gould-015-gradle-home
  scripts/verify-fast.sh --tests 'WidgetProjectionTest'` passed at
  `a908c96-dirty`, 2026-11-14 14:57:01 UTC (19 s): `compileDebugKotlin`,
  focused `WidgetProjectionTest`, localization, migrations, backup fixtures,
  history paging and UI-literal checks. Source hash
  `a62fd1233558d70e280b758cf761232c0a4397226beb7a0141091b6e8588d4a9`;
  tooling hash `2158e911ef0e4c39e700df81ca04739dd1827919a3caf31692639dda82add02b`.
  Two failed startup attempts are retained in the audit: first the default
  Gradle home was read-only; then the writable cache hit the sandbox wildcard-IP
  preflight. The reviewed retry passed. No emulator, UI screenshot/render or
  physical-device check ran.
- Catalogue evidence: schema 1 is exported at
  `app/schemas/com.opus.piano.data.catalogue.CatalogueDatabase/1.json`.
  `BundleStore` and `BundleJobRunner` stage/promote generic content-addressed
  files, but repository search found no path installing a generated catalogue
  database into `LocalCatalogueStore`. The format/package importer must be
  resolved before claiming a complete distribution pipeline. No schema or
  package format was changed.
- Licensing evidence: local commit `b0d7c4b` is the exact PR #40 source/license
  proposal diff. It is explicitly a draft, not approval. The PR discussion
  could not be fetched because GitHub was unreachable from this environment.
  Open Opus's official site states that its data is public domain, but individual
  record provenance and component-level rights still need to be preserved and
  validated. No catalogue data was imported or license metadata published.
- Backend/data evidence: `community-v1/drafts` is persistent app-private user
  state; removing its UI/storage before a draft export/recovery strategy would
  violate the preservation rule. Admin evaluation found the legacy UI client
  and canonical protocol are distinct, and the repository does not establish
  active deployment/operator use. Preserve those components and BFF operational
  artifacts until that ambiguity is resolved; remove no user data.
- Remaining next actions are listed in the two-block continuation section of
  `STAGE-CHECKLIST.md`. Do not start Stage 3 or use the prior signed APK as a
  current candidate.

## Current state

- Stage 1 is complete. Stage 2 implementation and automated host acceptance
  are complete. Stage 3 remains unauthorized and has not started.
- Worktree: `.worktrees/gould-0.15`, branch `feature/gould-0.15`, based on
  `origin/main` at `a908c96d6c3fa2712b77be1ad1e8941a1ac2e9da`. At checkpoint
  time the Stage 2 changes are still uncommitted; the verification audit binds
  the dirty source state by its source hash. No reset, clean, merge, push, or
  publication was performed.
- Binding owner decisions: successful weeks require sessions AND minutes;
  admin systems are evaluated only; the planned licenses are GPL-3.0-or-later
  for application code and CC-BY-SA-4.0 for documentation/original prose. This
  planning decision does not authorize application or publication. Rights and
  provenance verification is a blocking prerequisite, followed by separate
  explicit authorization; no license files/metadata/assets were changed.
- Physical-device policy: owner testing is on a real device, primarily Samsung
  Galaxy A55. Emulator/AVD availability is not an acceptance prerequisite.
  Agents do not access the owner's phone. See `DEVICE-TEST-CHECKLIST.md`.

## Verification on current worktree

- FAST PASS, 2026-11-14 12:46:21 UTC: compile, JVM unit suite and static
  localization, migration v1–13, backup, history and UI-literal checks. An
  initial sandbox preflight at 12:46:09 could not start Gradle because it could
  not determine a wildcard IP; the authorized retry passed. Both audit rows are
  retained.
- INTEGRATION PASS, 2026-11-14 12:46:29 UTC: full JVM suite, lint and
  androidTest compilation. Valid Gradle cache reused. No instrumentation ran.
- RELEASE PASS, 2026-11-14 12:52:34 UTC, 8 seconds: mandatory clean host gate,
  APK build, unit tests, lint, androidTest compilation and all six policy
  checks. 39 tasks executed and 46 reused valid task-cache outputs. No
  instrumentation ran. Audit identity: `a908c96-dirty`; source SHA-256
  `df25621711d8f40b9919f22c3fc1a7a6e52da641920752cb975ae6b52a97906b`;
  verification-tools SHA-256
  `2158e911ef0e4c39e700df81ca04739dd1827919a3caf31692639dda82add02b`.
  Exact audit rows are in `verification-audit.jsonl`.
- `git diff --check` passed after the documentation and policy updates.

## Stage 2 physical-device testing APK

- Installable release variant: `app/build/outputs/apk/release/app-release.apk`
- Built with the existing signing initialization script:
  `docs/releases/goldberg-0.11-stable-signing.init.gradle`; no signing key was
  generated or changed.
- Build and release lint command:
  `./gradlew -I docs/releases/goldberg-0.11-stable-signing.init.gradle :app:clean :app:assembleRelease :app:lintRelease --no-daemon --max-workers=1 --no-parallel`
  — PASS, 2026-11-14, 1m51s.
- SHA-256: `7f41a70c4c089a1f1712e045d61a85d5c5cc64593e2d30ec776b2dcaff8c653c`.
- Verified metadata: package `com.opus.piano`, versionName `0.15.0-dev`,
  versionCode 17, minSdk 30, targetSdk 37, `debuggable=false`.
- `apksigner verify --verbose --print-certs` passed with APK Signature Scheme
  v2. Signer certificate SHA-256
  `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`, matching
  the trusted published 0.14.0 fingerprint. ZIP integrity check passed; size
  8,970,574 bytes.
- No installation or upgrade was performed. Owner testing steps and backup /
  stop conditions remain in `DEVICE-TEST-CHECKLIST.md`.

## Not executed / not claimed

- Android instrumentation and Compose UI tests: source compiled, not executed.
- Any Galaxy A55 testing, fresh install, 0.14.0 in-place update, data migration,
  restore, accessibility, theme, locale, widget or process-recreation result:
  not performed by the agent; checklist prepared for owner execution.
- Release-specific security/upgrade gates, public Wiki/Project synchronization
  and release publication: not run or authorized. This signed APK is solely the
  owner testing artifact, not a public release.
- No screenshots were inspected or generated in this continuation.

## Implementation and decisions

- The central weekly predicate requires both configured sessions and minutes.
- `ActivityRepository.snapshot()` is read-only; reward evaluation is on the
  serialized refresh path and reward/claim operations are idempotent.
- Widget invalidation includes manual activity, activity goals/weeks/rewards,
  cosmetics, practice sessions and relevant profile tables.
- AppContainer wiring, six localized tour topics, additive Room 12→13 schema,
  backup v8 compatibility and UI/localization documentation are implemented;
  corresponding host/static checks passed.
- Admin review recommends preserving both systems: source evidence does not
  establish actual operator usage. The owner's planned code/docs license split
  is recorded in `DECISIONS.md` and `LICENSE-RECOMMENDATION.md`; copyright
  ownership/provenance verification and separate application authorization
  remain pending. GPL corresponding source must be available for every
  distributed binary. Google ID/Play Services F-Droid eligibility and the
  MusicBrainz CC BY-NC-SA exclusion remain separate follow-ups.

## External synchronization

The local checklist, decision record and checkpoint are current. GitHub Issue,
Project, branch push and Wiki sync remain pending: the configured `gh` token was
reported invalid on the preceding inspection. It was not retried and no
credentials were exposed. No PR, merge, tag, release or public promotion was
created. Restore GitHub CLI authentication only when remote synchronization is
needed; the invalid token does not block local Stage 2 implementation or host
acceptance.

## Latest update — independent dump-key fingerprint search (2026-11-15)

Searched the exact full fingerprint and short ID against external search results,
including Debian/package references. No trustworthy independent institutional
confirmation of `D5E63B4BDCCE195642948684B8FC2375C777580F` was found. ReadTheDocs
repeats MusicBrainz docs; MetaBrainz GitHub instructions give only the short ID;
MBS-14311 is a MetaBrainz-hosted ticket with reporter-submitted evidence.
Keyservers distribute keys but do not authenticate their owners, so no public
key was retrieved or imported. Following the stop condition, no new GPG
verification was attempted. The last recorded result remains failure with
`Can't check signature: No public key`; normal-policy verification has not
succeeded. No dump archive was downloaded or processed and no contact was made.

Provenance investigation details and source classifications are in
`CATALOGUE-PROVENANCE-POLICY.md`. Current source commit: `a908c96d6c3fa2712b77be1ad1e8941a1ac2e9da`,
branch `feature/gould-0.15`. Next step: resume only if an independently
trustworthy publication confirming the complete fingerprint is located; then
retrieve/import in isolated GPG home, compare the complete fingerprint, and
test the held signatures under default GPG policy.

## Latest update — owner-approved trust anchor; retrieval blocked (2026-11-15)

Owner approved trusting the full fingerprint in MusicBrainz's own docs:
`D5E63B4BDCCE195642948684B8FC2375C777580F`. This is owner acceptance, not
independent institutional authentication. In fresh GPG home
`/tmp/gould015-keycheck.Z1b4N0` (GnuPG 2.4.9), `gpg --recv-keys C777580F` failed
because `/usr/bin/dirmngr` is unavailable (`keyserver receive failed: No
dirmngr`). Direct HTTPS key retrieval failed DNS resolution; the web fetch of
the same Ubuntu keyserver endpoint was inaccessible. No key was imported,
fingerprint comparison was not reached, and no signature verification was
attempted. This is an environment retrieval failure, not a cryptographic
rejection. No overrides/settings were changed.

A 3,120,558,080-byte `mbdump.tar.bz2` was already present in the existing
inspection directory before this attempt; it was not opened or processed, and
no archive was downloaded in this continuation. Detailed evidence is in
`CATALOGUE-PROVENANCE-POLICY.md`. Next step: use an available supported key
retrieval path in an isolated GPG home, verify the exact fingerprint, and only
then test held detached signatures with default policy. Stop if GPG rejects
either.

## Latest update — catalogue search/loading repair verification (2026-11-15)

Current commit remains `a908c96d6c3fa2712b77be1ad1e8941a1ac2e9da` on
`feature/gould-0.15`; existing unrelated work and diagnostic artifacts remain
preserved. No commit, push, merge, tag, PR, or publication occurred.

- Root defects found in code: personal-only identities copied by the legacy
  repertoire bridge were eligible as active catalogue-search entities; and
  `DiscoverRepository.local()` translated catalogue initialization/access
  failure into an empty successful page. Personal identities are now
  quarantined unless independently supported by approved public title data;
  unavailable local catalogue access propagates its cause into the existing
  error/retry path. Library receives the same Discover repository and can
  render its actual public records. The device-side reason for the observed
  prolonged Library skeleton was not measured.
- Added Android regression source for the real bundled DB through the local
  repository, Chopin/Bach/Mozart/Beethoven (including Chopin works and accent /
  partial matching), Discover and Library rendering, empty and failure paths,
  personal-data separation, Kanye West online-only results, offline cache, and
  MBID deduplication. These Android tests compile but were not executed.
- `scripts/verify-release.sh` passed on the final source at 2026-11-15
  13:55:47 UTC (34 s): clean assembleDebug, app JVM suite (327 tests, 0
  failures/errors/skips), Android-test compilation, lintDebug and seven policy
  checks. The curated catalogue static check reports 100 composers, 105,032
  works and 105,421 relationships. A direct host inspection of the bundled
  SQLite asset confirms the Chopin alias/FTS record; this is not evidence that
  the Android repository or UI executed successfully.
- Fresh local testing APK rebuilt afterward with the existing trusted signing
  configuration; `:app:assembleRelease :app:lintRelease` passed. Path
  `app/build/outputs/apk/release/app-release.apk`; 97,910,513 bytes; SHA-256
  `2ec57be70330372904ffc88045aecce2ade99cd80c449a609562194b1957550c`.
  Package `com.opus.piano`, version `0.15.0-dev`, code 17, min SDK 30, target
  SDK 37; APK signature verifies, non-debuggable manifest, signer certificate
  SHA-256 `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.
  `check_curated_apk.py` and compatible-certificate upgrade check pass. This is
  an owner-testing artifact, not a published release.
- Runtime acceptance remains open. Instrumentation was not run, no real Kanye
  West request/cache-offline journey was executed in this app, and no Galaxy
  A55 was accessed. Automatic review rejected ADB device enumeration because
  it could reach the owner's phone; do not retry or route around that
  restriction. Existing checkpoint records emulator boot failure. Owner
  runtime verification is required before claiming the requested repository
  and UI acceptance.

## Latest update — previous catalogue fix failed on owner device (2026-11-15)

Owner reports no observable change on the Samsung Galaxy A55: Discover
`chopin` remains empty and Library `Z katalogu` remains on skeletons. Treat the
previous catalogue fix as failed; no success claim stands. Current branch and
source commit remain `feature/gould-0.15` / `a908c96d6c3fa2712b77be1ad1e8941a1ac2e9da`.
The last local APK remains version `0.15.0-dev` code 17, 97,910,513 bytes,
SHA-256 `2ec57be70330372904ffc88045aecce2ade99cd80c449a609562194b1957550c`,
signed with certificate `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.
Installed artifact identity is unknown; owner is away from the ADB computer.
No device was accessed and no APK was built.

Source trace: Discover and Library both enter `DiscoverRepository`, which calls
`LocalCatalogueStore.initialize()` then `prepareLocal()` to install the bundled
catalogue and synchronize legacy cache. Search SQL filters to active public
entities. `DiscoverScreen` catches failures into a generic UI error without
logging. `SharedLibraryCatalogue` catches exceptions and clears `loading` in a
`finally`; a persistent skeleton therefore indicates the screen call did not
finish or the installed UI/code differs from this source. This is source
reasoning, not a measured runtime cause. None of these paths currently emits
structured per-layer catalogue counters.

The non-destructive installed-artifact and logcat procedure is in
`CATALOGUE-RUNTIME-DIAGNOSTICS.md`. First obtain installed version/codePath,
pull the installed base APK, compare SHA-256 and signer to the local artifact,
and capture existing SQLite/process errors. Runtime counter evidence remains
unavailable. Do not change behavior or build an APK until the actual installed
binary and first failing runtime boundary are established. Preserve all
existing device data and logcat history.

## Latest update — Library Repertoire/Catalogue tabs (2026-11-15)

Implemented the owner-requested Library UI redesign on `feature/gould-0.15` at
`a908c96d6c3fa2712b77be1ad1e8941a1ac2e9da`; existing worktree changes were
preserved. The Repertoire tab renders only personal `LibraryState.pieces` and
retains sort, edit, delete, and manual add. Its empty state directs to Catalogue.
The Catalogue tab embeds the existing Discover search/repository, including
curated offline and consented MusicBrainz online search, loading/error/empty /
provider states. The embedded public Catalogue hides Discover's personal
“Available offline” section. Existing catalogue detail add/open-existing behavior
and `CatalogueRepertoire` identity/idempotency logic are reused. No database,
backup, provenance, rate-limit, or catalogue architecture changes were made.

- Added Compose regression source covering personal/public separation, Chopin
  composer and work display, empty/loading/error/retry states with an offline
  provider, plus source for idempotent add and preserving the public record when
  a repertoire entry is deleted.
- Final `scripts/verify-release.sh` passed at 2026-11-15 18:07:42 UTC in 49 s:
  327 JVM tests, 0 failures/errors/skips; AndroidTest sources compiled; lint and
  seven policy checks passed. Instrumentation was not executed.
- Final `:app:assembleRelease :app:lintRelease` passed afterward. APK:
  `app/build/outputs/apk/release/app-release.apk`, 97,894,273 bytes, SHA-256
  `1399fd74cf90a562c766aeb1dc0a16ed28fa46a8f8c0abd209d7b582e0738905`, package
  `com.opus.piano`, version `0.15.0-dev` / code 17, non-debuggable; certificate
  SHA-256 `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.
  `check_curated_apk.py` and compatible-certificate upgrade check passed.
- The previous Discover/catalogue initialization failure on the Galaxy A55 is
  still unresolved. No physical device or emulator was used. This UI task is
  not runtime evidence for catalogue search/loading. See
  `CATALOGUE-RUNTIME-DIAGNOSTICS.md` for the pending owner procedure.
- No commit, push, merge, tag, PR, release publication, or external data change
  occurred. Previous build APKs/reports/test results were copied to unique
  `.gould-015-diagnostics` directories before clean gates.

## Latest update — Catalogue performance investigation (2026-11-15)

The reported recurring delay had a concrete source-level cause: each Discover
search re-ran `LocalCatalogueStore.initialize()` and the application preparation
callback, whose `LegacyCatalogueImport.synchronize()` re-walks legacy cache,
composer and work tables. `DiscoverRepository` now serializes preparation,
reuses it after successful completion, keeps incomplete identity batches
retryable, and propagates initialization errors so the next request can retry.
Bundled installation and search SQL remain unchanged. The catalog's prebuilt
FTS4 virtual table is present and used by query plans; pages remain at 40 items
with existing paging. Existing provider debounce, cancellation, cache,
provenance and rate limiting remain intact.

The Library switcher now fills the available width with two equal Material
primary tabs, centered wrapping labels and a 64 dp minimum height. An
instrumented layout assertion covers equal bounds, adjacency and touch height.

Host Python/SQLite measurements against the 283,987,968-byte shipped database:
decompression 421.7 ms, SHA-256 77.6 ms, SQLite integrity 893.2 ms, foreign-key
check 174.5 ms. Typed warm SQL query repeat medians were Chopin 0.085 ms, Bach
0.182 ms, Mozart 0.144 ms, Beethoven 0.078 ms and work-title Ballade 0.149 ms.
Query plans used `alias_fts` virtual table index 3 and entity/redirect indexes,
not a full entity-table scan. These are host measurements, not Galaxy timing;
the pre-change app path had no timing instrumentation, so no Android before/
after speedup is established. Raw reports are in
`.gould-015-diagnostics/catalogue-performance-20261115/`.

Final source gate: `scripts/verify-release.sh` PASS at
`2026-11-15T18:50:42Z`, 110 s; 327 JVM tests passed with zero failures,
errors or skips; lint and all seven policy checks passed; AndroidTest sources
compiled. New instrumented actual-catalogue timings, search/paging assertions,
and tab layout checks were not executed. No emulator or physical device was
accessed. The gate produced only its debug APK (not a signed, non-debuggable
release/testing APK); its current identity is recorded in
`CATALOGUE-RUNTIME-DIAGNOSTICS.md`. No separate test APK, commit, or external
update was created.

**Next:** when the owner can run the app on the Samsung Galaxy A55, capture
`GouldCataloguePerf` logcat timings without clearing prior logs, exercise a
first launch and repeated Chopin/Bach/Mozart/Beethoven/work searches, then run
consented MusicBrainz separately. Compare first visible frame and filter/history
phases with the captured search durations. The device procedure and phase names
are documented in `CATALOGUE-PERFORMANCE-INVESTIGATION.md`.

## Owner testing Release APK — 2026-11-15

- Built without cleaning using
  `./gradlew -I docs/releases/goldberg-0.11-stable-signing.init.gradle :app:assembleRelease --no-daemon --max-workers=1 --no-parallel`;
  final attempt exited 0 in 1m24s. The first attempt stopped at Room/KSP because
  the default `/tmp` quota was full; the successful retry set
  `JAVA_TOOL_OPTIONS=-Djava.io.tmpdir=$PWD/.gould-015-diagnostics/gradle-tmp/java-temp-release-owner-20261115`.
- APK: `app/build/outputs/apk/release/app-release.apk`, 97,910,657 bytes,
  SHA-256 `b6563a569027dde289f09ce311fea905c0dc3b45cb56419a76d4f584088e4412`.
  Android SDK `apkanalyzer` reports package `com.opus.piano`, version
  `0.15.0-dev`, code 17 and `debuggable=false`; `apksigner` v2 verification and
  ZIP integrity passed. Signer SHA-256 is the expected
  `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.
- Build emitted Kotlin generic-type/API deprecation warnings, the native
  library strip warning for `libandroidx.graphics.path.so`, and Gradle 10
  deprecation notice. No source/config/version/signing change was made. No
  installation or Galaxy A55 acceptance was performed; catalogue runtime and
  performance remain unverified on device.

## Catalogue performance audit follow-up — 2026-11-15

Current branch remains `feature/gould-0.15`; HEAD remains
`a908c96d6c3fa2712b77be1ad1e8941a1ac2e9da` with existing unrelated work
preserved. Updated correlated `GouldCataloguePerf` events and a test for their
safe session/source/category fields. `automaticSearch()` carries one opaque
session ID across its local and online emissions; the Android test asserts this
behavior. No search text, repertoire content, payload, or credentials are
logged. Pipeline findings, ranked source evidence, UX recommendation, Rust
feasibility, owner A55 commands, and limits are in
`CATALOGUE-PERFORMANCE-INVESTIGATION.md`.

- FAST PASS: 2026-11-15 21:58:42 UTC, 6 s; final-source debug Kotlin compile,
  both focused diagnostic-context tests, and six fast static-check groups
  passed.
- INTEGRATION PASS: 21:59:25 UTC, 36 s; final-source suite **329 JVM tests**,
  zero failures/errors/skips; `lintDebug` and AndroidTest compilation passed.
  AndroidTest was compiled, not executed. Final-source Release Kotlin
  compilation and `lintRelease` passed in 40 s.
- Built and independently inspected the signed diagnostic Release APK after
  source verification: `app/build/outputs/apk/release/app-release.apk`,
  97,927,041 bytes, SHA-256
  `da2760795b835ad7c2f8ae2bd68e0428b336b92bb2d01184f6ce249e705fc54f`;
  package `com.opus.piano`, version `0.15.0-dev` (code 17), non-debuggable,
  valid v2 signature, expected signer
  `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`, and
  valid ZIP. Earlier APK outputs are retained under
  `.gould-015-diagnostics/catalogue-performance-20261115/`.
- The mandatory `scripts/verify-release.sh` was not run: it invokes
  `gradlew clean`, prohibited by the preservation instruction. A first APK
  packaging attempt hit a temporary extraction quota; retry with isolated temp
  storage succeeded. No ADB, emulator, or phone was accessed. Galaxy timings,
  installed identity and online timings remain unavailable; no performance
  improvement or device acceptance is claimed.

**Next:** when the owner is available, first record installed package/version,
APK SHA-256, and signer using the owner procedure in the performance report.
Use the verified diagnostic build to capture correlated timings for initial
Catalogue render, local/online result arrival, category switching, scrolling,
and detail return. Compare on the same Galaxy A55 before implementing query or
UI optimizations.

## Release authorization and provenance gate — 2026-11-16

The owner authorized the Gould 0.15.0 pre-release, including GPL-3.0-or-later
for application source and CC-BY-SA-4.0 for documentation/original prose. This
authorization does not resolve the separately documented provenance gate for
the bundled MusicBrainz-derived catalogue. `CATALOGUE-PROVENANCE-POLICY.md`
lines 89–114 records that the snapshot signing key and archive signatures were
not authenticated, no source records were reviewed, and production generation/
import approval was not given. `CATALOGUE-SELECTION.md` records the current APK
payload as 100 composers, 105,032 works, and 105,421 relationships generated
from that snapshot. Therefore publication and license metadata application are
blocked; no LICENSE files, release commit, merge, tag, push, or GitHub Release
were created.

Read-only confirmation: branch `feature/gould-0.15`, HEAD
`a908c96d6c3fa2712b77be1ad1e8941a1ac2e9da` (same as remote `main` at audit
time), existing changes remain uncommitted, and remote `v0.15.0` does not
exist. The requested candidate remains preserved at
`.gould-015-cancellation-build/app/outputs/apk/release/app-release.apk`:
97,931,121 bytes, SHA-256
`f3df006e36aa55fb940b5a938cc36737156c3ff77a5c960a9c5da10b53ade8ea`;
read-only `apksigner` verification confirms v2 and trusted certificate SHA-256
`53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`, package
`com.opus.piano`, version `0.15.0-dev` / code 17. This confirms artifact
identity only and does not clear source-data provenance.

The latest recorded source verification remains the 2026-11-16 run above:
331 app JVM tests and 13 activity-domain tests passed; Android test APKs
compiled but instrumentation was not executed; lintDebug, lintRelease, release
assembly and static checks passed. `git diff --check` passed in this audit.
No physical device was accessed by the agent. The owner-reported A55 acceptance
of the cancellation candidate is not attributed to a new artifact or used to
resolve the data provenance blocker.

**Required before resuming publication:** authenticate the MusicBrainz signing
key and verify the exact source snapshot/checksums/signatures; complete the
record/source-field provenance review and obtain explicit approval to ship the
derived production catalogue. Then rerun the release gates against the final
source/artifact and repeat the license-scope audit. No release publication
action is authorized until these gates are clear.

## MusicBrainz catalogue provenance verification — 2026-11-16

Provenance investigation completed with outcome **A — VERIFIED**, scoped to the
publisher's documented fingerprint-pinning model and GnuPG 2.4.9 default
policy. The exact official `20261114-002230` Core archive and signatures were
preserved in a new isolated evidence directory. Both detached signatures
verified; the independently calculated archive SHA-256 matches the signed
manifest. The generator, curation and deterministic packaging steps were
rerun into that directory and byte-match the preserved full DB, curated DB,
and bundled gzip asset. The audit identifies Core fields/tables and confirms
no supplementary dump input. This is not owner approval to distribute.

See `provenance-evidence/FINAL-PROVENANCE-REPORT.md` and
`provenance-evidence/evidence-manifest.json`. Limitations retained: GnuPG owner
trust is unknown (the key identity is corroborated by two official
publisher-controlled sources); MetaBrainz MBS-14311 remains unresolved for
legacy SHA-1 certificate interoperability; original transfer redirect logs are
missing; the importer does not fully implement PostgreSQL COPY backslash
unescaping, though no such escaped sequence was present in the curated text
fields audited. No application behavior or production catalogue was changed;
no Git integration or publication occurred.

## Final release verification — 2026-11-16

The owner approved production use of snapshot `20261114-002230`,
GPL-3.0-or-later for project-owned program source, CC-BY-SA-4.0 for
project-owned documentation/original prose, and the 0.15.0 pre-release
process. The project-wide and third-party license boundaries are documented
in `docs/LICENSES.md`; canonical texts are `LICENSE` and `LICENSE-DOCS`.

Host verification passed: app JVM **338/338**, activity-domain JVM **13/13**,
`lintDebug`, `lintRelease`, Android-test APK compilation, Release assembly,
UI literal checker, MusicBrainz catalogue checker, curated APK payload check,
and `git diff --check`. Instrumentation was compiled only, not executed. Exact
commands/results are in `RELEASE-VERIFICATION.md`.

**Publication is blocked before Git integration by two gates.** First, the
fresh APK is not byte-identical to the owner-tested candidate. Second, the
packaged Google ID and Google Play Services dependencies declare the Android
SDK License, and compatibility with the approved GPL application license was
not established; see `docs/LICENSES.md`. No GPL linking exception or system-
library assumption was invented. The owner-tested candidate
remains unchanged at 97,931,121 bytes, SHA-256
`f3df006e36aa55fb940b5a938cc36737156c3ff77a5c960a9c5da10b53ade8ea`. The
fresh isolated assembly is also 97,931,121 bytes and verifies with the same
package/version, trusted signer, and catalogue payload, but has SHA-256
`9dfce12a1e793c21af970526bb0f1118a5ab9bd5f31e45b20bdca8a17318a054`. All
418 ZIP entries and uncompressed data match, but the full APK is not
byte-identical. Under the strict release gate it is untested and cannot be
published; owner A55 testing of this exact fresh artifact is required.

No files were staged or committed; no push, PR, merge, tag, or GitHub Release
was made. README/changelog remain pointed at published 0.14.0. Next: owner
tests the exact fresh APK and obtains a definitive compatibility determination
for the SDK-license dependencies. After both gates clear, reverify the chosen
candidate before the already-authorized squash-merge and pre-release flow.

## Google dependency licensing audit — 2026-11-16

The previously unfinished licensing half of the final verification is now
documented in
`docs/planning/gould-0.15/GOOGLE-DEPENDENCY-LICENSING-AUDIT.md`. It verifies
the licence identity of every Google artifact in `releaseRuntimeClasspath`
(Android Software Development Kit License for all ten `com.google.*`
artifacts; Apache-2.0 for androidx.credentials and guava
`listenablefuture`), and classifies exactly what the owner-tested APK
(SHA-256 `f3df006e…ade8ea`) packages: **298 SDK-License Google classes from
seven artifacts** (`play-services-fido` 118, `-base` 69, `-basement` 56,
`-auth` 39, `-auth-blockstore` 8, `-tasks` 5, `googleid` 3;
`auth-api-phone`/`auth-base`/`identity-credentials` contribute zero), plus
one Apache-2.0 `ListenableFuture` class. A fresh offline dependency report
(exit 0) is preserved at
`.gould-015-release-final-verification/dependencies-report.txt`.

**Verdict: LEGAL REVIEW REQUIRED.** The GPLv3 System-Libraries exception does
not plausibly cover code packaged inside the APK, no Google-granted §7-style
linking permission exists in any inspected artifact or term, and whether
SDK §3.4 permits app bundling combined with GPLv3 §5(c) whole-work licensing
is a genuine legal-judgment question (Google's formal clarification request
at Issue Tracker 515367456 is still unanswered). This does **not** clear the
`docs/LICENSES.md` gate: publication stays blocked pending an owner-obtained
definitive determination or a separately approved removal/replacement of the
Google sign-in path. No licence text, owner decision, code or Gradle file
was changed; no git write operation occurred. The stage-checklist item at
`STAGE-CHECKLIST.md` L661 stays unchecked for the same reason.

## Release execution — 2026-11-16 (owner-authorized publication)

The owner explicitly authorized completing and publishing Gould 0.15 as a
GitHub prerelease **despite the unresolved Google SDK licensing questions**
and instructed that no permission be invented and the questions not be
represented as resolved. Recorded in `GOOGLE-DEPENDENCY-LICENSING-AUDIT.md`
§12 (addendum, findings preserved), `docs/LICENSES.md`,
`docs/releases/GOULD-0.15.md` and `STAGE-CHECKLIST.md` L661 (stays
unchecked).

**Artifact decision:** the published APK is the exact owner-tested artifact
`.gould-015-cancellation-build/app/outputs/apk/release/app-release.apk`
(97,931,121 bytes, SHA-256
`f3df006e36aa55fb940b5a938cc36737156c3ff77a5c960a9c5da10b53ade8ea`,
versionCode 17, versionName `0.15.0-dev`, cert `53eface4…d59e`, recorded
Galaxy A55 owner acceptance). The entry-identical fresh build
`9dfce12a…318a054` was **not** published because it was never owner-tested;
the pending "owner retests the fresh APK" gate is therefore superseded by
publishing the already-tested artifact. Google Sign-In code is unchanged in
0.15 (replacement deferred to Gould 0.16).

**RELEASE host gate executed 2026-11-16** — `scripts/verify-release.sh`
**PASS in 76 s** (audit `2026-11-16T12:16:10+00:00 RELEASE PASS 76s
@a908c96-dirty`; BUILD SUCCESSFUL in 1m14s):
- Exact mandatory policy command `clean assembleDebug
  testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon
  --max-workers=1` executed; `:app:testDebugUnitTest` **338 tests, 0
  failures, 0 errors, 0 skipped** (task FROM-CACHE — restored from the
  Gradle build cache for identical inputs after `clean`, not a fresh
  re-execution); `:app:lintDebug` executed PASS; `:app:assembleDebug` and
  `:app:assembleDebugAndroidTest` built (Android-test APK compiled,
  **instrumentation not executed**).
- All seven mandatory policy checks PASS (agent policy, localization,
  migrations v1→v13 + backup formats, backup fixtures, history paging, UI
  literals, curated catalogue incl. DB SHA-256 `2b4c67b4…0b428f`, 100
  composers / 105,032 works / 105,421 relationships, snapshot
  `20261114-002230`).
- Additional fast checks PASS in the same session: check_admin_retirement,
  check_publication_workflows, check_musicbrainz_catalogue, check_fixtures,
  test_publication_workflows, test_upgrade_table_expectations, stage6
  test_backup_store, check_curated_apk on the owner-tested APK, schemas
  unchanged, `git diff --check`. Logs under `.gould-015-verification-tmp/`.
- `activity-domain:jvmTest` is not part of the mandatory policy command; its
  13-test PASS result is from the earlier final-verification run in
  `RELEASE-VERIFICATION.md` (same source state).

Not executed: Android instrumentation (compiled only), emulator journeys,
owner device access. `/tmp` quota remains exhausted; all temp output used
`$PWD/.gould-015-verification-tmp`.

Next: commit the 0.15 work (feat + docs commits on `feature/gould-0.15`),
push, open the Gould 0.15 PR, watch `quality.yml`, merge with a **regular
merge commit** (owner: no squash, no rebase), tag `v0.15.0` at the merge
commit, publish the prerelease with `gould-0.15.0.apk`, verify everything,
then record the actual URLs/SHAs in a post-release docs commit on `main`.

## Publication record — 2026-11-16 (completed)

All steps executed and verified on 2026-11-16:

- **Commits on `feature/gould-0.15`:** `a2233f6`
  (`feat(gould-0.15): curated catalogue, Library/Artist redesign, guided
  tour, admin retirement`, 169 files) and `42eda13`
  (`docs(gould-0.15): release notes, licensing audit addendum, verification
  records`, 67 files), pushed to origin. Working tree clean; no
  `.gould-015-*` build trees, no `provenance-evidence/{raw,reproduction,
  gnupg}` material and no secrets committed (largest committed file:
  `app/src/main/assets/catalogue/musicbrainz-core-20261114/catalogue.dbz`,
  89,123,721 bytes, under GitHub's 100 MB limit).
- **PR:** [#53 — Gould 0.15](https://github.com/stokonn/Gould/pull/53).
  CI on the PR: `host-quality` **pass** (7m40s), `synthetic-postgres`
  **pass** (2m4s), `administration-safety` **pass** (11s); the push-triggered
  duplicates of the same three workflows also passed. PR state MERGED,
  mergeable CLEAN at merge time.
- **Merge:** **regular merge commit**
  `06282c5aad4887b472373e925e6ead01e24c4cba` ("Merge pull request #53 from
  stokonn/feature/gould-0.15"), exactly **2 parents** (`a908c96…` old main +
  `42eda13…` branch head) — no squash, no rebase; both feature commits are
  ancestors of `origin/main`.
- **Tag:** annotated `v0.15.0` (tag object `35efbf0027108edc51962c10edc9949b11cdd1ca`)
  → resolves to the merge commit `06282c5…`; pushed to origin.
- **Release:** [Gould 0.15.0](https://github.com/stokonn/Gould/releases/tag/v0.15.0)
  — title "Gould 0.15.0", `isPrerelease: true`, `isDraft: false`, target
  `main`, published `2026-11-16T12:35:11Z`.
- **Asset:** exactly one asset `gould-0.15.0.apk`, 97,931,121 bytes. It was
  first uploaded by gh as `app-release.apk` (the `#label` suffix was not
  applied by gh 2.97.0); the correctly named copy was uploaded and the
  misnamed asset (ID 754791140) deleted, leaving the conventional name.
  Downloaded back from the release: SHA-256
  `f3df006e36aa55fb940b5a938cc36737156c3ff77a5c960a9c5da10b53ade8ea`,
  `cmp`-byte-identical to the owner-tested APK, signer cert
  `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.
- **Release notes:** user-focused body covering features, backup/upgrade
  safety, API 30+ requirement, the transparent unresolved-licensing note
  (owner authorization; questions NOT resolved), verification summary and
  the APK SHA-256.

Not executed: Android instrumentation (compiled only), emulator journeys,
owner device access by the agent. The Google SDK licensing questions remain
**unresolved** (audit verdict LEGAL REVIEW REQUIRED); publication proceeded
under the owner's explicit authorization, recorded in audit §12 and
`docs/LICENSES.md`. This entry is recorded in the post-release documentation
commit on `main`.
