# Gould 0.15.0 final verification — publication blocked

**Status:** requested host checks passed. Publication is **BLOCKED** by two
independent gates: (1) a fresh Release assembly is not byte-identical to the
already owner-tested APK and requires owner retesting; (2) redistribution
compatibility of packaged Google ID/Play Services components under their
Android SDK License with the approved GPL application license was not
established. See `docs/LICENSES.md`.

## Source and policy audit

- Branch at audit: `feature/gould-0.15`, HEAD
  `a908c96d6c3fa2712b77be1ad1e8941a1ac2e9da`; remote `origin/main` was the same
  commit. No `v0.15.0` tag, release, or Gould 0.15 PR existed at inspection.
- The full intended implementation remains in the local feature worktree.
- Owner approved GPL-3.0-or-later for project-owned program source,
  CC-BY-SA-4.0 for project-owned documentation/original prose, and the exact
  verified MusicBrainz Core catalogue. License and asset boundaries are in
  `docs/LICENSES.md`; canonical texts are `LICENSE` and `LICENSE-DOCS`.
- A material Google identity dependency compatibility question remains:
  `googleid:1.1.1` and `play-services-auth:21.1.1` declare the Android SDK
  License, are packaged in the APK, and no explicit GPL-compatible permission
  or confirmed System Library exception was established. No legal conclusion
  is invented; this blocks distribution pending a definitive determination.
- No application source/resources/catalogue/version metadata were changed in
  this final verification and licensing documentation batch. No APK license
  asset was added.

## Executed checks

The first Gradle invocation lacked its isolated init file because the write to
`/tmp` failed from quota exhaustion. It was interrupted before Release
assembly, and is not counted. A debug manifest report under the default
ignored build tree was regenerated, then compared byte-for-byte with preserved
copies in both signed build trees; all three SHA-256 values were
`098d26a2224b2f2a4430247368521136f760e6eebd312efe8051e913f3e83632`. No APK
or source file was overwritten by that interrupted run.

Successful Gradle verification used offline mode, one worker, isolated
workspace build/schema/temp paths, and no clean task:

```text
JAVA_TOOL_OPTIONS="-Djava.io.tmpdir=$PWD/.gould-015-release-final-verification/tmp" \
GRADLE_USER_HOME=/home/stokonn/.gradle ./gradlew --offline --no-daemon \
  --max-workers=1 --console=plain \
  -I .gould-015-release-final-verification.init.gradle \
  -I docs/releases/goldberg-0.11-stable-signing.init.gradle \
  :app:testDebugUnitTest :activity-domain:jvmTest \
  :app:lintDebug :app:lintRelease :app:assembleDebugAndroidTest \
  :app:assembleRelease
```

Gradle result: **BUILD SUCCESSFUL**. Fresh JVM test execution was forced:

```text
JAVA_TOOL_OPTIONS="-Djava.io.tmpdir=$PWD/.gould-015-release-final-verification/tmp" \
GRADLE_USER_HOME=/home/stokonn/.gradle ./gradlew --offline --no-daemon \
  --max-workers=1 --console=plain \
  -I .gould-015-release-final-verification.init.gradle \
  :app:testDebugUnitTest :activity-domain:jvmTest --rerun-tasks
```

- `:app:testDebugUnitTest`: **338 tests, 0 failures, 0 errors, 0 skipped**.
- `:activity-domain:jvmTest`: **13 tests, 0 failures, 0 errors, 0 skipped**.
- `:app:lintDebug`: PASS.
- `:app:lintRelease`: PASS.
- `:app:assembleDebugAndroidTest`: PASS (compiled only; instrumentation was
  **not executed**).
- `:app:assembleRelease`: PASS (signed Release APK assembled).
- `python3 scripts/check_ui_literals.py`: PASS; 7 audited legacy literals,
  no new raw UI strings.
- `python3 scripts/check_musicbrainz_catalogue.py`: PASS; 100 composers,
  105,032 works, 105,421 relationships; DB SHA-256
  `2b4c67b414dd985f7caa4e6e08efddba685debe731156a6df30ea4f6dbadce5`.
- `python3 scripts/check_curated_apk.py
  .gould-015-cancellation-build/app/outputs/apk/release/app-release.apk`:
  PASS; expected catalogue asset only, decoded payload correct, no full dump.
- `git diff --check`: see final check result in the current release checkpoint.

The owner has tested the previous cancellation-fix APK on a Samsung Galaxy
A55. That owner-reported acceptance does not apply to the fresh APK below.
Instrumentation was compiled but not executed, and the agent did not access
the owner's device.

## Exact APK comparison

Protected owner-tested APK:

- Path: `.gould-015-cancellation-build/app/outputs/apk/release/app-release.apk`
- Size: 97,931,121 bytes.
- SHA-256: `f3df006e36aa55fb940b5a938cc36737156c3ff77a5c960a9c5da10b53ade8ea`.

Fresh isolated Release assembly:

- Path: `.gould-015-release-final-verification/app/outputs/apk/release/app-release.apk`
- Size: 97,931,121 bytes.
- SHA-256: `9dfce12a1e793c21af970526bb0f1118a5ab9bd5f31e45b20bdca8a17318a054`.

Both report package `com.opus.piano`, version `0.15.0-dev`, code `17`,
`debuggable=false`, valid APK Signature Scheme v2, and certificate SHA-256
`53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`. All 418
ZIP entry names and uncompressed contents match, including the catalogue
payload. Complete APK hashes differ in the signed APK/container bytes. The
fresh APK is therefore **not owner-tested** and must not be published.

## GitHub and publication state

No files were staged or committed, no branch was pushed, no PR was created or
merged, and no tag or release was created. The README/changelog remain pointed
at published 0.14.0. Release notes are prepared but marked unpublished. The
source archive, full/reproduced databases, temporary build outputs, signing
configuration, and diagnostic artifacts remain local and are excluded from
Git.

Next step: owner tests the exact fresh APK above on the Galaxy A55. Then
resolve the Google dependency license question. Only after both gates are
cleared, reverify the chosen file and proceed with the already authorized
merge, tag, and GitHub pre-release sequence.

---

## Publication addendum — 2026-11-16 (owner-authorized)

**Status change:** the owner explicitly authorized completing and publishing
Gould 0.15 as a GitHub prerelease **despite the unresolved Google SDK
licensing questions**, with the instruction not to invent permissions or
claim the questions are resolved. This supersedes the two gates above as
follows, without altering any finding in this document or in
`GOOGLE-DEPENDENCY-LICENSING-AUDIT.md`:

- **APK-retest gate — superseded.** The published artifact is the exact
  **owner-tested** APK (`.gould-015-cancellation-build/.../app-release.apk`,
  97,931,121 bytes, SHA-256
  `f3df006e36aa55fb940b5a938cc36737156c3ff77a5c960a9c5da10b53ade8ea`).
  The fresh `9dfce12a…318a054` build remains unpublished and not
  owner-tested; publishing the already-tested artifact removes the need for
  its retest.
- **License gate — NOT resolved.** The audit verdict *LEGAL REVIEW REQUIRED*
  stands. The owner's authorization is a release decision for Gould's own
  code only; it resolves nothing about Google's code, grants no permission
  and is recorded transparently in `docs/LICENSES.md`, audit §12 and the
  release notes.
- **Merge method — changed by owner instruction.** The Git integration uses a
  **regular merge commit** (explicitly no squash, no rebase), preserving the
  original commit history. This supersedes the earlier "squash-merge" wording
  above.

**RELEASE host gate executed 2026-11-16** (before commit): the exact
mandatory policy command via `scripts/verify-release.sh` **PASS in 76 s**
(audit `2026-11-16T12:16:10+00:00 RELEASE PASS 76s @a908c96-dirty`):
`:app:testDebugUnitTest` 338 tests / 0 failures / 0 errors / 0 skipped
(FROM-CACHE restore for identical inputs after `clean`), `lintDebug`
executed PASS, `assembleDebug` + `assembleDebugAndroidTest` built
(instrumentation **compiled, not executed**), all seven mandatory policy
checks PASS. Fast checks (10 policy scripts, 4 unit suites, curated-APK
payload on the published artifact, schema immutability, `git diff --check`)
all PASS; logs in `.gould-015-verification-tmp/`.

**Publication record** (PR URL, merge commit, tag, prerelease URL, asset
digests, final verification) is appended after execution in the post-release
documentation commit on `main`, mirroring the 0.14.0 practice.
