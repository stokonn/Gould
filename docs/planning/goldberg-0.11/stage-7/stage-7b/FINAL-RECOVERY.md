# Stage 7B final recovery — October 24, 2026

Status: **IN PROGRESS — owner acceptance pending**. This file supersedes earlier
pending-run summaries; historical evidence and checkpoints remain preserved.

Purpose: recover the interrupted CI #24 investigation, correct the fixture flow,
and independently verify the exact corrected source before owner acceptance.
Scope: Stage7bAcceptanceTest, retained CI evidence, verification and tracking docs.
No production code, personal database, migration, backup or practice changes.

## Implementation and acceptance checklist

- [x] Recover branch/status and existing Issue #31, milestone 4, releases and
  Goldberg Development Project 2 (In Progress). Initial HEAD/remote exactly
  `dc60e8379114b44f758e0bb5184a0b6b83d7a465` on
  `feature/goldberg-0.11-stage-7b`. Eight modified tracked files, private PMP and
  historical untracked evidence preserved. Private-file hash snapshot retained
  locally; PMP is never staged.
- [x] Read canonical policy, maintenance, AI/stage/recovery checkpoints and latest
  Issue comments. Resume-checkpoint skill found no additional session checkpoint.
- [x] Independently inventory CI #24 (`40083355927`) using existing ZIPs in
  `/tmp/opencode/run-40083355927`; **no artifacts downloaded again**. Both ZIP
  digests match fresh GitHub artifact metadata; ZIP CRC integrity passes.
  Retained-file inventory: 424 files before this report plus all **732 original ZIP member hashes**, including complete raw
  upgrade extraction. See `results/ci-run-40083355927/independent-inventory.json`
  and `independent-files-sha256.json`.
- [x] Verify failed-run JUnit: **187 tests, 1 failure, 0 errors, 2 existing skips**.
  Matrix fails only at four standard 1.0 locale/theme configurations; retains
  170 PNG/tree pairs, not a completed acceptance matrix. Actual inspected
  standard-en-Light failure screenshot shows revision 2 awaiting acknowledgement;
  small-cs-Dark-2.0 Queue row screenshot shows reachable complete sub_1 card.
- [x] Independently verify old-source seeded upgrade: all 12 personal collections
  equal, 37 sessions, preferences unchanged per result; SQLite schema 8→10,
  integrity_check=ok and no foreign-key violations in both raw databases.
  This is temporary same-runner debug signing, not release-signed installation.
- [x] Review existing test correction: Queue row first loads revision 1; queue
  fixture then advances to revision 2. Production correctly blocks prepare while
  reReview=true. Test now waits for verified row detail, asserts re-review, clicks
  revision acknowledgement and completion, waits for verified/non-re-review, then
  retains all decision confirmation assertions. Four added acknowledgement
  captures increase expected complete matrix from 258 to **262 pairs**.
- [x] Focused command PASS 13s: `ANDROID_HOME=/home/stokonn/Android/Sdk ./gradlew
  testDebugUnitTest --tests 'com.opus.piano.admin.AdminReviewTest'
  assembleDebugAndroidTest --offline --no-daemon --max-workers=1` (1 executed,
  57 up-to-date tasks). Instrumentation compiled only; no local emulator run.
- [x] Seven existing validation scripts PASS: admin contracts, agent policy,
  backups, history paging, localization (1050 EN/CZ), migrations and Stage3B v5.
- [x] Mandatory clean local gate PASS **3m58s**, 84 tasks (82 executed/2 up-to-date): `ANDROID_HOME=/home/stokonn/Android/Sdk ./gradlew clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --offline --no-daemon --max-workers=1`. Fresh **275 JVM tests /36 suites /0 failures/errors/skips**, lint **0 errors/119 warnings/1 hint**, instrumentation compilation only. Logs, XML, source manifest and APK identity retained in `results/final-recovery-2026-10-24/`.
- [x] APK identity/signing/ZIP checks PASS: `com.opus.piano`, existing development 0.10.1/code11, SHA-256 `93ae9ec88472e4f07dc5328980786e9ce252887ccbac507c0034195cd9b51961`, trusted signer `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`. Production source unchanged; APK hash is identical to the previous clean production build.
- [ ] Commit/push only verified correction/evidence/docs; verify remote SHA.
- [ ] Dispatch full stage7b-acceptance exactly once at corrected source SHA.
- [ ] Independently inspect exact-SHA JUnit, 262 required screenshot/tree pairs,
  32 dashboard configurations, Queue/Audit row reachability, native accessibility
  focus, actual row-open/Back, notification/sync and seeded upgrade evidence.
- [ ] Synchronize Issue #31 description AND checkpoint comments, Project,
  maintainable documentation and owner report with executed results.
- [ ] Explicit owner acceptance. Manual emulator TalkBack/full navigation,
  platform permission revocation and live G1–G9/G11 remain open/unexecuted;
  passing synthetic CI does not close them.

## Durable checkpoint

Initial branch/commit above; clean host gate and APK identity PASS. Next: publish narrow test-only correction, dispatch once,
then inspect complete raw acceptance artifacts. Known local emulator host crash is
not retried. No phone, other-worktree edits, production activation, main merge or
release. Stage 7B is not marked complete.

created by stokonn with love <3
