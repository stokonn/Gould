# Gould 0.13 development release-readiness checklist

This is a development checklist only. It does not authorize signing,
publication, tagging, or merging into `main`.

## Verified

- [x] Application identity remains `com.opus.piano`; database remains `opus.db`;
  schema remains v11.
- [x] Development metadata is versionName `0.13-dev`, stable-property metadata
  is `0.13`, and versionCode is `15`.
- [x] R8/resource-shrunk unsigned artifact is 6,492,506 bytes; optimized
  runtime journeys passed on disposable API 36 emulator evidence.
- [x] Recording metadata is loaded in page batches; association previews are
  capped in SQL at six rows per recording.
- [x] Refresh paths clear cached details and reload after capture changes,
  recovery, deletion, and manual retry.
- [x] Focused host FAST, static checks, Android-test compilation, and synthetic
  bounded-query benchmark passed.
- [x] Profiling closed with negative evidence: retained CPU samples are
  RenderThread/emulator dominated; no additional app-owned optimization is
  justified by the bounded Room benchmark and focused native regression.
- [x] RELEASE host gate passed at frozen development source `14882d3`:
  `clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug`
  plus five policy checks, 248 seconds; audit row recorded in
  `verification-audit.jsonl`.
- [x] Final-SHA CI run `41149284775` passed at `6dbd2b8`: full native job
  recorded 271 tests, 0 failures, 0 errors and 13 skipped; acceptance job
  passed seeded upgrade/data-preservation, Data Saver, restart and database
  integrity checks.
- [x] Stable-metadata unsigned R8 artifact preflight passed in 289 seconds:
  package `com.opus.piano`, versionName `0.13`, versionCode `15`, minSdk 26,
  targetSdk 36, ZIP integrity and non-debuggable release packaging.
- [x] Test-signed artifact quality preflight passed against published `v0.12`:
  APK metadata progression, trusted-fingerprint check, ZIP integrity and
  packaged launcher artwork/circle bounds. This is not production-signature
  evidence and does not prove a published signed upgrade.

## Open gates

- [x] Focused `StudioRecordingTest` instrumentation passed 8/8 with zero
  failures/errors/skips on disposable API 36 CI emulator `emulator-5554`,
  run [41146502208](https://github.com/stokonn/Gould/actions/runs/41146502208)
  at exact commit `e812b7c`.
- [x] Dedicated Compose/recomposition and recording-CPU profiling is closed
  with documented negative evidence; no app-owned hotspot was demonstrated.
- [ ] Production-signature compatibility and published-version in-place
  upgrade using the actual trusted production-signed 0.12 and 0.13 APKs.
- [ ] Final release artifact security/privacy/licensing review beyond the
  passed host checks, and visual/accessibility QA across the supported matrix.
- [ ] Owner approval for any production signing, tag, Stable publication, or
  `main` integration.

## Final status

| Area | Status | Evidence or limitation |
|---|---|---|
| Source identity and metadata | PASS | `com.opus.piano`, `opus.db`, schema 11, versionCode 15 |
| Focused recording regression | PASS | CI 41146502208, `StudioRecordingTest` 8/8 |
| Profiling and justified optimization | PASS | Negative evidence; no further app-owned hotspot |
| RELEASE host gate | PASS | 248 s at `14882d3`, audit recorded |
| Final 0.13 artifact preflight | PASS | Unsigned R8 APK, SHA-256 `9bfb5a3e…86c2f`; stable metadata and ZIP/artwork checks |
| Production signing and published upgrade | NOT RUN | Requires actual production-signed 0.13 APK and owner-authorized release action |
| CI native/data acceptance at final SHA | PASS | Run 41149284775; instrumentation 271/0/0/13 skipped; acceptance passed |
| Visual/accessibility matrix | NOT RUN | No final release visual/accessibility run exists |
| Security/privacy/licensing | PARTIAL | Permissions, legal assets, dependency surface and host fixtures checked; final artifact-specific review remains |
| Publication/main/tag | BLOCKED | Explicit owner authorization required |

created by stokonn with love <3
