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

## Open gates

- [x] Focused `StudioRecordingTest` instrumentation passed 8/8 with zero
  failures/errors/skips on disposable API 36 CI emulator `emulator-5554`,
  run [41146502208](https://github.com/stokonn/Gould/actions/runs/41146502208)
  at exact commit `e812b7c`.
- [x] Dedicated Compose/recomposition and recording-CPU profiling is closed
  with documented negative evidence; no app-owned hotspot was demonstrated.
- [ ] Release-only verification: non-debuggable 0.13/code15 artifact, trusted
  production-signature compatibility, APK/ZIP/resource gates, published-version
  in-place upgrade, security/privacy/licensing checks, full native execution,
  and visual/accessibility QA.
- [ ] Required CI evidence at the final frozen SHA. The focused native result
  at `e812b7c` remains valid for unchanged app/build source; later commits are
  documentation/audit-only.
- [ ] Owner approval for any production signing, tag, Stable publication, or
  `main` integration.

## Final status

| Area | Status | Evidence or limitation |
|---|---|---|
| Source identity and metadata | PASS | `com.opus.piano`, `opus.db`, schema 11, versionCode 15 |
| Focused recording regression | PASS | CI 41146502208, `StudioRecordingTest` 8/8 |
| Profiling and justified optimization | PASS | Negative evidence; no further app-owned hotspot |
| RELEASE host gate | PASS | 248 s at `14882d3`, audit recorded |
| Final 0.13 release artifact and trusted signing | NOT RUN | Requires separate production release authorization |
| Upgrade/security/privacy/licensing release gates | NOT RUN | Final release artifact/source freeze gates |
| Full final-SHA native/visual CI | NOT RUN | Focused native evidence is source-bound to unchanged app code |
| Publication/main/tag | BLOCKED | Explicit owner authorization required |

created by stokonn with love <3
