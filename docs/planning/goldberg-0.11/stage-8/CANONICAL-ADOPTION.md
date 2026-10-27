# Canonical Stage 6 client adoption in isolated Stage 8 integration

Status:LOCALLY VERIFIED SERVICE INTEGRATION;UI adoption andwhole-stage acceptance
remain PARTIAL. Branch integrate/goldberg-0.11-canonical-client is based on42f6897.
The accepted Stage7A/7B anddirty Stage8 worktrees were preserved without edits.
Current owner instruction authorizes this isolated source combination;earlier
preparation-only restrictions remain historical.

Purpose/scope:adopt the exact Stage6 canonical codec/client/audit verifier plus an
application-scoped repository andthe existing Android transport seam. No personal
Room/schema/migration/journal/practice changes,dependencies,network activation or
UI changes. Contract loading/decoding is off main;tokens/uncertain intent remain
in memory;process recreation requires fresh authentication. Production uses the
existing DisabledAdminTransport regardless of a persisted endpoint.

- [x] Recover reviewed integration sequence andinventory legacy consumers.
- [x] Preserve accepted legacyUI,Keystore,marker/settings andexisting fixture corpus.
- [x] Add exact canonical shared schema/codec/session/review/audit implementation.
- [x] Bind canonical application repository via suspend lazy factory andAndroid
  transport abstraction;boundedUTF8/no redirects/no endpoint switching.
- [x] Synchronize generation/cache guards;clear sensitive state before logout I/O;
  live capabilities,dual revisions,explicit confirmation anduncertain exact replay.
- [x]302 JVM tests PASS/0failures/errors/skips,including20canonical and7repository
  tests. First4derived-fixture identity mismatches retained in first-attempt/;
  corrected by binding test capabilities to the same token principal/session.
- [x] Debug APK,testAPK compilation andlint0errors132warnings1hint PASS atreviewed
  source. Final source/raw/hash receipt in results/canonical-integration-2026-10-25.
- [x] Actual Kotlin service→loopbackHTTP→BFF→PostgreSQL integration executed1PASS
  at final reviewed source: Stage6 services/community-bff/results/local-f0323084bea14437995a58a3e368e715/result.json.
  This executes challenge/login/caps/queue/detail/Comparison/open/confirmed reject/
  stats/audit/bodyless logout,not only captured-fixture replay.
- [ ] Execute new4native canonical application tests plus existing full suite on
  emulatorCI;local host emulator crashed before testing.No physical-device use.
- [ ] Adapt accepted legacy UI/service models to canonical decimal/dual revisions,
  Comparison/serverstats/audit semantics. No lossyInt conversion orlegacy fallback.
- [ ] Final accepted UI/account/Keystore lifecycle/seeded-upgrade combined acceptance.
- [ ] Public version/OAuth/real policies andhosted integration;owner acceptance.

Normal host gate:
./gradlew testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1

Actual HTTP/PG integration compiles its separate source set only with
-PgoldbergCanonicalIntegration=true;the Stage6 disposable runner supplies
SCHEMA_PG_PORT andGOLDBERG_BFF_SOURCE. This test-only loopback transport is never
packaged/selected in the application. No skipped external test is counted as pass.

No screenshot inspection was required/performed:existing UI was not changed.
API26 image absent;API36 nativeCI is the supported fallback.Local emulator logs
retain disk-capacity failure andtwohostSIGSEGVs;no local native success inferred.
Current APK remains development0.10.1/code11/com.opus.piano;trusted certificate
53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e verified.
No release,main merge,tag,public service orpractice data synchronization.

created by stokonn with love <3
