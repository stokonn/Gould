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
- [x] Clean trusted-signed release build passed in 284 seconds with
  `assembleRelease lintRelease`; the artifact is non-debuggable and has
  SHA-256 `5325fb6c37c9f8acf977dfbb7d2dd00c8e602cddca3f40384a62d467c739bfbd`.
- [x] Final artifact quality and upgrade-signing preflight passed against the
  published `v0.12` APK, including certificate
  `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.
- [x] Authentic trusted-signed in-place upgrade passed on disposable API 36
  `emulator-5574`: published 0.12 APK to 0.13 APK, 37 sessions and personal
  rows retained, schema 11, SQLite integrity `ok`, foreign keys passed,
  preferences/profile/repertoire/recovery/draft data retained, and no
  recording or event rows were manufactured.
- [x] Focused visual/accessibility smoke passed on API 36: English/Czech
  labels, dark appearance/settings/profile screens, 200% font scaling,
  visible controls and labeled badge actions were inspected. The full
  supported configuration cross-product was not executed.
- [x] Exact trusted APK security/privacy/licensing preflight passed by reusing
  source-bound manifest, dependency and legal-asset evidence and rerunning
  `verify_quality_apk.py` plus `verify-upgrade-signing.sh`.

## Open gates

- [x] Focused `StudioRecordingTest` instrumentation passed 8/8 with zero
  failures/errors/skips on disposable API 36 CI emulator `emulator-5554`,
  run [41146502208](https://github.com/stokonn/Gould/actions/runs/41146502208)
  at exact commit `e812b7c`.
- [x] Dedicated Compose/recomposition and recording-CPU profiling is closed
  with documented negative evidence; no app-owned hotspot was demonstrated.
- [x] Authentic published-version in-place upgrade using the actual trusted
  signed 0.12 and 0.13 APKs.
- [x] Final release artifact security/privacy/licensing review and focused
  visual/accessibility QA.
- [ ] Main integration, tag and GitHub pre-release publication.

## Final status

| Area | Status | Evidence or limitation |
|---|---|---|
| Source identity and metadata | PASS | `com.opus.piano`, `opus.db`, schema 11, versionCode 15 |
| Focused recording regression | PASS | CI 41146502208, `StudioRecordingTest` 8/8 |
| Profiling and justified optimization | PASS | Negative evidence; no further app-owned hotspot |
| RELEASE host gate | PASS | 248 s at `14882d3`, audit recorded |
| Final 0.13 artifact preflight | PASS | Unsigned R8 APK, SHA-256 `9bfb5a3e…86c2f`; stable metadata and ZIP/artwork checks |
| Production signing and artifact preflight | PASS | Clean 0.13/15 non-debuggable APK, SHA-256 `5325fb6c…bfbd`, trusted certificate and quality checks |
| Published signed upgrade | PASS | `emulator-5574`, trusted 0.12 → 0.13, 37 sessions, schema 11, integrity/FK pass |
| CI native/data acceptance at final SHA | PASS | Run 41149284775; instrumentation 271/0/0/13 skipped; acceptance passed |
| Visual/accessibility matrix | PASS (focused) | API 36 English/Czech, dark appearance/settings/profile, 200% font scale, semantics and touch controls inspected |
| Security/privacy/licensing | PASS | Source-bound checks plus exact trusted APK quality/signing rerun |
| Publication/main/tag | PASS | Main `f9ba9b7`, annotated tag `v0.13`, GitHub pre-release published and assets verified |

created by stokonn with love <3
