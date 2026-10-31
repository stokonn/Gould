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

## Open gates

- [x] Focused `StudioRecordingTest` instrumentation passed 8/8 with zero
  failures/errors/skips on disposable API 36 CI emulator `emulator-5554`,
  run [41146502208](https://github.com/stokonn/Gould/actions/runs/41146502208)
  at exact commit `e812b7c`.
- [x] Dedicated Compose/recomposition and recording-CPU profiling is closed
  with documented negative evidence; no app-owned hotspot was demonstrated.
- [ ] Final frozen-source RELEASE verification: clean policy build, full native
  execution, visual/accessibility QA, artifact/signing/upgrade/security/privacy/
  licensing gates, and required CI at the final SHA.
- [ ] Owner approval for any production signing, tag, Stable publication, or
  `main` integration.

created by stokonn with love <3
