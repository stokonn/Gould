## Host evidence committed; remote publication pending

Host verification evidence commitc4673c25163e8210807d0b85d94cfd4fd30933b9 records
owner-tested1277bed with214/214 JVM tests, lint0 errors/109 warnings, trusted-signed
debug APK and precise coverage/runtime limitations. Application/test source is
unchanged from the tested revision. Normal exact-SHA push ofc4673c2 was attempted
and failed before authentication: github.com could not resolve. Connected GitHub
read confirmed the integration branch absent; Issue31 has68 comments and no new
Android checkpoint. No force-push or dashboard write was attempted.

A narrowly scoped host publisher is prepared at
.gradle/community-publication/publish-host.sh. It checks a clean exact branch/HEAD,
ancestry, unchanged tested source,214 XML results, lint counts, source/artifact
hashes and the preserved APK, then pushes normally, verifies remote SHA and appends
one factual partial checkpoint with readback/history checks. It never deploys or
uploads the APK. The milestone remains runtime-PARTIAL even after source publication.

Exact next compilation (permitted host, no full JVM/lint rerun needed):
`./gradlew --no-daemon :app:assembleDebugAndroidTest --stacktrace`.
Then use a functioning named disposable emulator for5 native tests, existing Room/
backup migration and seeded-upgrade checks, actual process recreation/cold startup
and contributor UI/theme/font/CZ/EN acceptance. Current executor cannot bind sockets;
no active emulator observed, and Stage7B separately reports host qemu SIGSEGV. Native
source-only compilation has passed, but there is no Android test APK or runtime pass.
Do not use a physical phone or start6.12–6.14. All formal gates remain unchanged.

## Android host Gradle verification confirmed — 2026-10-23

**JVM/build/lint VERIFIED; Android runtime acceptance still PENDING.**
Branch implement/goldberg-0.11-stage-6-10-11-local; owner-tested HEAD
1277bedf5056e794a94566e828ffecb4b14338a3 (implementation6e4a627). Before this
recording pass, working tree was clean and all15 recorded source hashes matched.
Owner executed on permitted Fedora host:
`./gradlew --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug --stacktrace`
and reported BUILD SUCCESSFUL. Agent inspected the actual generated outputs;
original console transcript was not supplied, so no compiler-warning count is
invented. No application/test source change was needed after this green run.

- [x]31 actual JUnit XML suites: **214 PASS,0 failures/errors/skips**; includes
 40 CommunityRepositoryTest tests and174 existing regressions. These40 are included
 in214, not an additional execution total. Full app/contributor UI Kotlin compiled.
- [x] LintDebug: **0 errors/fatal,109 warnings**. Category counts and raw XML/TXT/HTML
 retained. No warnings located in new data/community or CommunityDraftPanel files;
 SettingsScreen has an existing UseKtx suggestion on an unchanged expression.
- [x] Debug APK assembled, agent signature/badging inspected. ApplicationId
 com.opus.piano, inherited version0.10.1/code11, min26/target36. Trusted signing
 SHA25653eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e matches.
 APK SHA2568e30d783d223b7fc9b48c780edf6a3f5f24624aa016a77c3312d6f4149a0751d;
 preserved privately under .gradle/community-host-artifacts/1277bed/app-debug.apk.
 APK is not committed, uploaded or released.
- [x] Packaged manifest and data-extraction XML independently inspected: allowBackup
 false/fullBackupContent false; all cloud/device-transfer domains excluded. This is
 structural policy proof, not an executed Android restore/Keystore test.
- [x] Coverage mapped to exact executed XML test names for privacy/DTOs, file drafts,
 independent JVM restart, account isolation, error/retry/conflict/rate limits, cache,
 disabled transport and source-level backup/startup assertions. No network-path
 privacy assertion or Android process-death inference from JVM tests.
- [x] Direct standalone compilation of CommunityLocalInstrumentedTest.kt against
 the host-built app classes, SDK36 and cached AndroidX PASS. **0 Android test bodies
 executed**; no Gradle androidTest APK packaging claim.
- [x] Agent reran five repository checks: all PASS, including host DDL migrations
 1→10..9→10 and portable backup fixtures. No Room/personal/backup source change.
- [ ] Gradle :app:assembleDebugAndroidTest, packaging/install of test APK,5 new
 Android Keystore/tamper/backup-flag/filesystem/disabled-boundary tests.
- [ ] Existing Room/backup native migration and seeded upgrade verification;
 actual Android process death/recreation, cold startup, contributor UI/theme/font/
 CZ/EN screenshots and user-visible draft/error states. No physical phone use.
- [ ] Normal branch push/exact remote-SHA verification and Issue31 checkpoint.

Evidence copied before build outputs can be overwritten:
stage-6/results/android-local/host-confirmed-1277bed/ contains31 JUnit XML files,
raw lint reports, coverage.json, verification.json, source/artifact hashes and
APK/policy/direct-instrumentation-compile checks. Source identity relies on owner's
exact tested-revision report, initially clean tree and matching recorded hashes;
manual host command did not capture a contemporaneous source manifest. No claim
of signed execution attestation. Earlier blocked runs/failure logs remain intact.

Runtime venue: fresh executor loopback-bind probe returns EPERM; no active emulator
observed. Stage7B checkpoint6359455362 separately reports host qemu SIGSEGV. No
repeat crashing launch, AVD wipe, phone use or sandbox bypass. Existing granular
policy forbids host escalation prompts. A functioning permitted emulator venue is
required; shared Stage5/7 emulator state/worktrees must remain preserved.

All formal6.10/6.11/cloud/release gates remain unchecked; overall milestone remains
PARTIAL pending runtime acceptance. No real OAuth/endpoint/production upload,
Supabase/Cloudflare change, main merge, release or6.12–6.14 start. Next action:
compile test APK on permitted host, then execute scoped tests and native migration/
upgrade/UI acceptance on a named disposable emulator; publish this factual partial
checkpoint after normal source push and exact remote readback.

## Earlier source/recovery checkpoints (historical)

## Concurrent Stage7B checkpoint observed after source commit

Fresh Issue31 read:68 comments, all67 original comment bodies preserved. New
checkpoint6359455362 reports independent Stage7B source49680da/cee9330 published,
host Gradle204 PASS/lint/APK/androidTest compilation, and qemu SIGSEGV preventing
emulator acceptance. These are that checkpoint's reported results, not Android
6.10–6.11 results; they do not turn this branch's blocked Gradle/native checks green.
The earlier source checkpoint's67-comment count was correct when recorded.

No Stage7B code/worktree/Issue content modified or merged. Its independent Admin
session/transport foundation may require shared session/credential reconciliation
when branches are explicitly integrated; no incompatible live auth exists here,
because contributor production transport/enrollment remain disabled. Its requested
stats/queue/audit-search contract additions are outside6.10–6.11 scope and have not
been invented or implemented. Host emulator verification needs a functioning
permitted emulator venue; do not repeatedly launch the documented crashing host
emulator or use the owner's physical phone. Local candidate remains PARTIAL with
40 direct JVM tests passing, full Android acceptance pending, publication unstarted.

## Android partial source saved — publication and acceptance pending

Implementation candidate commit6e4a627eab72c5c2056151c122556d5442417d91 on
implement/goldberg-0.11-stage-6-10-11-local, descendant of verified backend9d7150c.
Direct Kotlin/JUnit40 PASS and five host checks PASS; final captured source run is
stage-6/results/android-local/jvm-ofm366xf. Core/credential Kotlin was compiled;
full Android UI/build/lint and5 runtime tests remain unexecuted. Formal6.10/6.11
are unchecked and this local Android milestone is PARTIAL.

Git shell read failed resolving github.com; connected GitHub confirms the new
branch is absent. No unverified Android candidate was pushed. Issue31 remains at67
comments; no new checkpoint/dashboard edit. Prepared host verification is
scripts/verify-community-android-local.sh; optional EMULATOR_SERIAL must explicitly
name a disposable confirmed emulator. Host publication instructions and an exact
partial checkpoint are saved under ignored .gradle/community-publication/; publish
only after appropriate host verification and a normal exact-SHA remote readback.

The source commit retained raw log trailing blank lines. A scoped .gitattributes
rule disables blank-at-eof checks only for .log artifacts, preserving their raw
bytes; source/document whitespace checks remain enabled. No test failure was hidden.
Host execution approval was automatically rejected because sandbox_approval:false
prevents escalation prompts. No bypass, real network API, deployment, real account,
physical phone, main merge or release action. Next action is permitted-host Android
verification of this source, followed by emulator acceptance; not a new milestone.

# Local Android 6.10–6.11 — PARTIAL implementation, 2026-10-23

Branch `implement/goldberg-0.11-stage-6-10-11-local`, exact backend base
`9d7150c5b3f20880fe4d587d9c3bfd65a0f612ce`. Source checkpoint is the containing
implementation commit. Backend source publication and Issue31 checkpoint6358952579
were independently read;67 comments at start. No changes to backend source/evidence,
Stage5/Stage7A/Stage7B worktrees, personal practice/schema/backup/profile/OAuth code.

Implemented Android source:

- Typed catalogue UUID/kind/external identity/revision, contribution payload and
  create/revise/withdraw commands; explicit JSON field selection, nested deny-unknown
  decoder, exact schemaVersion,16KiB/field/evidence bounds and safe write integers.
  Public overlay/change DTOs preserve full BIGINT strings and numeric sequence order.
  Real source/field/license acceptance remains server-owned/default-deny; no universal
  licensing policy is inferred. Freeform contribution text is not guaranteed free of
  personal prose. No existing practice entity is accepted by the request codec.
- CommunityRepository with disabled production transport (no endpoint/HTTP factory),
  explicit synthetic fixture transport, distinct availability/error states and no
  automatic publishing. Google profile identity grants no community authorization.
- Private no-backup local draft files, stable UUIDs, cross-instance file locking,
  optimistic versions and atomic fsync/rename publication; incomplete local drafts
  supported. No deletion/overwriting of unrelated drafts or damaged data. Separate
  guest/principal namespaces; no automatic guest/account adoption or account transfer.
- Exact wire request/key persisted before an attempt; same logical request retries
  use that key, including process restart and failed receipt save. Attempted drafts
  cannot be edited in place; a reviewed new logical request requires explicit copy,
  keeping original content.409/422/400/404 block resend;429 honors server time and
  missing/invalid Retry-After does not permit immediate resend. Failed attempts retain
  draft content. Caller cancellation and sign-out during a fixture call are safe.
- AndroidKeyStore AES-GCM credential adapter under noBackupFilesDir, redacted token
  representation, local clearing on community sign-out. No real enrollment/provider
  integration exists. Android backup disabled and all transfer domains excluded;
  portable export code/preferences remain unchanged. Android runtime encryption/
  backup checks await the instrumentation venue; compilation is not runtime proof.
- Small Settings → Data & Storage → Local contribution drafts subpage, existing
  navigation/components/theme,29 new EN/CZ strings. Saved draft editing, preview,
  unavailable/error labels, safe retry state and explicit community credential clear.
  Remote submit is disabled in the actual app. UI does not offer fake publication.
  Public fixtures are used by tests only, not by OpusApplication or user catalogue.
- Lazy application composition; no community work in onCreate, workers, practice or
  recovery startup. Saved overlay snapshots mark stale/synthetic/time explicitly.
  A fresh or recreated repository never fetches/uploads until an explicit method call.

Executed verification:

| Venue | Actual result |
|---|---|
| Direct cached Kotlin2.2.10 compiler + real JUnit4.13.2 | **40 tests PASS,0 failures**, jvm-final-signout-io.log and jvm-ofm366xf/junit.log |
| Core/credential Kotlin compilation against SDK36 android.jar | PASS as part of direct invocation; Android credential operations not executed |
| Five scripts/check_*.py | all PASS; separate logs/host-checks.json |
| Localization |951 EN/CZ entries, matching placeholders/plurals/locales |
| Existing Room host migration checks |1→10 through9→10 PASS; existing schema unchanged |
| Existing portable backup fixtures |all three formats retain original fields through schema10 |
| Gradle compileDebugKotlin attempt | FAILED before tasks: wildcard IP unavailable creating FileLockContentionHandler |
| Scoped permitted-host verification request | Automatically rejected: sandbox_approval:false; no escalation prompt available |
| Full Gradle JVM regressions / lint / debug APK / androidTest compilation | **UNEXECUTED**, not counted as pass |
|5 new Android instrumentation tests | Written, **not compiled/executed** |
| Emulator UI/startup/upgrade/screenshots | **UNEXECUTED**, no physical device accessed |
| Actual network privacy assertion / real OAuth/hosted integration | **UNEXECUTED**, production transport disabled |

Direct tests use actual filesystem persistence and an actual separate JVM process
for restart recovery. They exercise serializer/privacy smuggling, scalar/version/
UUID/BIGINT/URL/body bounds, creation/edit/CAS/write faults/corrupt-record retention,
account isolation/sign-out, expired/denied sessions, retry body/key persistence,
409/422/no-resend, all error statuses, Retry-After dates/windows/missing values,
cancellation/timeouts/sign-out during a call, uncertain receipt save, disabled
transport/no implicit calls and stale public cache. Test backup/startup source checks
are source-level only, not Android restore or cold-start measurements.

Initial direct run:31/32 PASS,1 failed due a new test comparing boxed Integer2000
with nullable Long2000. Corrected the fixture assertion to2000L; original historical
repository tests were not modified. Subsequent32/35/37/38/final40 runs PASS.
All raw failures/logs and tested-source manifests retained. Scripts/community-local/
verify-jvm.py reproduces the direct compiler venue; it is not a substitute for Gradle.

No Room migration was needed. Drafts/credentials live in separate no-backup files;
OpusDatabase, all schema exports and backup/practice/Google identity code are
byte-identical to9d7150c. The chosen owner-specified backend baseline predates six
later Stage5 file fixes; those are preserved on32cf5d9, not merged into this branch.
Stage7A576f0d2 and Stage7B local49680da are untouched. Later branch integration must
retain those independent fixes; no superseding Stage5/7 acceptance is claimed.

Remaining gates/limitations:

1. Run `bash scripts/verify-community-android-local.sh` in a permitted host. It
   records source hashes before execution, checks them afterward, runs full Gradle
   JVM/build/androidTest compilation/lint and five repository checks. Fix any genuine
   compiler/runtime failures before local milestone acceptance.
2. Use only a disposable emulator with synthetic data. Optional
   `EMULATOR_SERIAL=emulator-NNNN` runs5 new instrumentation checks after confirming
   ro.kernel.qemu=1. Execute relevant existing Room/backup instrumentation separately,
   seeded restart/upgrade and contributor UI inspection in both themes/CZ/EN/font sizes.
   No emulator rendering has been inspected in this session.
3. AndroidKeyStore/backup exclusion and app cold-start behavior need runtime proof.
   Guest/account migration, real enrollment and endpoint/contract freeze remain later
   separately authorized integration work. Save edits locally before previewing;
   unsaved form state uses rememberSaveable, not a claimed hard-kill autosave.
4. Commit/publication of this partial branch does not mark formal6.10/6.11 complete.
   All cloud, release and threat-model gates remain unchanged. No deploy/main merge,
   tag/APK release, production users/secrets or subsequent milestone started.

Exact next action: host verification of this existing implementation; do not restart
or rerun full Gradle inside the socket-denied sandbox. After acceptance of6.10–6.11,
recommend separately authorized6.12–6.14 security/integration verification.

Final direct venue jvm-ofm366xf captured seven Kotlin source/test hashes before
compilation and checked them unchanged afterward;40 tests passed. SDK credential
source was compiled, not executed. Current source manifest also covers UI/native
test/runner resources, which remain subject to the blocked Gradle/Android venue.
