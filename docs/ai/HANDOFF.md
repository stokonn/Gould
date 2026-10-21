# Goldberg 0.11 — Stage 5 HANDOFF (fresh-session resume instructions)

> **SUPERSESSION NOTE (2026-10-21, Stage 6 planning branch):** the Stage 5 content
> below is a frozen historical checkpoint from before Stage 5 completion. **Stage 5 is
> now COMPLETE and owner-approved (2026-10-21)** — the resume steps below are obsolete
> and must not be executed; the authoritative Stage 5 record lives on
> `feature/goldberg-0.11-stage-5` (completion commit `736f6d3`, head `32cf5d9`) and the
> canonical `STAGE-CHECKLIST.md`. See the additive note at the end of this file.

> Created 2026-10-20; updated at the end of the third resume session (desktop machine).
> Stage 5 is **in progress, incomplete**. The branch HEAD is a checkpoint commit on top of
> `54f2b45`, pushed to origin as an owner-authorized IN-PROGRESS checkpoint. Nothing was
> merged, tagged or published. **Tasks 5.10/5.11 (instrumentation + emulator acceptance)
> are the only Stage 5 tasks still open** — they are blocked on this desktop by a known
> upstream emulator bug and must be executed on the **notebook's** working API 36 emulator.

## Read order (in this order, before touching code)

1. `AGENTS.md` — permanent policy (data safety, no physical phone, publication gates).
2. `docs/ai/STATE.md` — current state, executed evidence, emulator blocker record.
3. `docs/ai/VERIFICATION.md` — executed vs. not-executed ledger.
4. `docs/ai/DECISIONS.md` — owner decisions, incl. approved F1/F2 resolutions.
5. `docs/planning/goldberg-0.11/STAGE-CHECKLIST.md` — Stage 5 section (authoritative list).
6. GitHub Issue #31 — latest checkpoint comment (do not duplicate issues).

## What the checkpoint contains (all prior work preserved)

- Everything from the previous checkpoint (`c61cf12`): F1/F2 fixes, compile-green
  `Stage5BundleJobRunnerTest`/`Stage5BundleWorkerTest`, recovery docs.
- This session's additions: `docs/ai/*` + STAGE-CHECKLIST updated with the **desktop
  emulator blocker** (Google Issue #566807521) and the emulator-independent host
  verification (Gradle host gate + all five `scripts/check_*.py`).
- `.pmp/`, `.pmp.yml`, `RELEASE_NOTES.md` and
  `docs/planning/goldberg-0.11/stage-4/checkpoint-archive/` are private/unrelated —
  never commit them.

## Verified state at this checkpoint (executed — see VERIFICATION.md for exact rows)

- `:app:assembleDebug :app:assembleDebugAndroidTest` **BUILD SUCCESSFUL** (desktop, Java 25
  host, 1m08s, 0 errors). Fresh APKs:
  `app-debug.apk` 32,388,569 B sha256 `44222c59b89ed63041d1e105f074857f5ca3c2f93efef4a9ed0eea115dd0a157`;
  `app-debug-androidTest.apk` 1,331,040 B sha256
  `6030e528474334ae31f0514c0f818aeeb68dcf3f9b2ac35e453762d1e5ba8f2d` (Oct 20 21:13).
- Host gate (`clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug`)
  + `scripts/check_*.py` — results recorded in VERIFICATION.md (all executed on the
  desktop this session).
- **Zero instrumentation tests executed anywhere** (emulator blocker below).

## Emulator blocker on the desktop (why 5.10/5.11 were not done here)

Every headless emulator boot on the desktop (Fedora kernel `7.2.10-200.fc44`) dies with
`Killed by SIGSEGV` after `WARNING: cannnot unmap ptr … protected range …`, on both
AVDs (`Medium_Phone_API_37.0` playstore and fresh `stage5-api36` google_apis API 36) and
both emulator versions (37.2.12 SDK + 37.3.4 canary side-by-side), across
swiftshader/auto(lavapipe) GPU modes. This is upstream bug **Google Issue Tracker
#566807521**; unfixed as of 37.3.4. Details in `docs/ai/STATE.md`.

## Exact resume steps for tasks 5.10/5.11 — run on the NOTEBOOK

The notebook has the previously working toolchain (emulator 37.2.12 + API 36 google_apis
x86_64 image + AVD `stage5-api36`, AVD home redirected). On the notebook:

1. Confirm the tree is the pushed Stage 5 branch at the checkpoint commit:
   `git status` (only `.pmp/`, `.pmp.yml`, `RELEASE_NOTES.md`,
   `stage-4/checkpoint-archive/` untracked), `git log --oneline -2`.
2. Rebuild **fresh** APKs there (the notebook's build outputs are from an older source
   state — do NOT reuse them):
   `./gradlew --no-daemon --max-workers=1 :app:assembleDebug :app:assembleDebugAndroidTest`
   and verify timestamps/hashes before installing (the stale-APK lesson from the battery-
   emergency session).
3. Boot the notebook emulator (AVD home redirect is mandatory there):
   `ANDROID_AVD_HOME=/home/stokonn/.config/.android/avd "$ANDROID_HOME/emulator/emulator"
   -avd stage5-api36 -port 5580 -no-window -no-audio -no-boot-anim -gpu swiftshader_indirect
   -no-snapshot -memory 2048 &`
   Keep the process alive across tool calls (managed/background shell, not a bare nohup —
   the desktop session lost nohup'd processes). Wait for
   `adb -s emulator-5580 shell getprop sys.boot_completed` = 1 and verify
   `ro.kernel.qemu`=1. **Every ADB call must use `-s emulator-5580`; never a physical phone.**
4. Install the fresh APKs explicitly:
   `adb -s emulator-5580 install -r app/build/outputs/apk/debug/app-debug.apk`
   `adb -s emulator-5580 install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`
5. **Task 5.10** — run instrumentation explicitly, per Stage5 class with real counts:
   - `adb -s emulator-5580 shell am instrument -w -e class com.opus.piano.Stage5BundleStoreTest com.opus.piano.test/androidx.test.runner.AndroidJUnitRunner`
   - `adb -s emulator-5580 shell am instrument -w -e class com.opus.piano.Stage5BundleJobRunnerTest com.opus.piano.test/androidx.test.runner.AndroidJUnitRunner`
   - `adb -s emulator-5580 shell am instrument -w -e class com.opus.piano.Stage5BundleWorkerTest com.opus.piano.test/androidx.test.runner.AndroidJUnitRunner`
   Record exact OK/Failure counts. Investigate any failure against the F1/F2 contracts
   (DECISIONS.md); never weaken assertions.
6. **Task 5.11** — emulator acceptance matrix on the same emulator: Wi-Fi→mobile
   transition, Data Saver, consent revocation mid-stream, real process death at
   boundaries, low disk, seeded in-place upgrade from trusted 0.10.1, disk/network cost
   measurements. Stage 4 helpers: `scripts/emulator_ui.py`,
   `scripts/verify_emulator_upgrade.py`, `scripts/measure-startup.sh`.
7. **Task 5.13** — update `docs/ai/*` + STAGE-CHECKLIST with executed evidence, commit,
   push to the Stage 5 branch, sync Issue #31 + Project. **Do not mark 5.10–5.13 complete
   without executed evidence. Do not start Stage 6; no tag/merge/release.**
8. PMP: local `.pmp/`/`.pmp.yml` on the desktop were **preserved as-is, never
   reinitialized**. Leave them private.

Alternative 1 (CI, current primary path): `.github/workflows/stage5-emulator.yml`
(`workflow_dispatch` only, KVM-accelerated `ubuntu-24.04`, SHA-pinned, revised this
session — signing-resolution, state-asserted network/Data Saver checks, DB integrity
after process death, source-SHA binding) runs the full connected suite for 5.10 and the
CI-executable 5.11 subset. It is committed on this branch (`1ef15bf` + CI-activation
revision) and proposed for `main` via **draft PR #32 (workflow-only, not merged)**.
After the owner approves: merge PR #32, then dispatch the workflow **against
`feature/goldberg-0.11-stage-5`** from the Actions tab (the workflow depends on this
branch's helper scripts; `main`'s copy is older). Consent revocation mid-stream, low
disk, screenshot inspection and real bundle-transfer costs remain notebook/manual work
either way.

Alternative 2: if an upstream emulator fix for #566807521 ships (watch the issue), the
desktop's `stage5-api36` AVD + emulator 37.3.4 side-by-side install are already prepared
(`~/Android/Sdk/emulator-37.3.4/emulator -avd stage5-api36 -port 5580 …`).

## Hard prohibitions (unchanged)

No physical phone; no implicit device selection; no merge/tag/release/APK publication
without separate authorization; no weakened test assertions; no fabricated results; no
Stage 5 "complete" claim; no Stage 6 start. Never commit `.pmp/`, `.pmp.yml`,
`RELEASE_NOTES.md` or the stage-4 checkpoint archive.

## Environment

Desktop: Fedora, kernel `7.2.10-200.fc44.x86_64`, Java 25, 15 GiB RAM, Android SDK at
`~/Android/Sdk` (android-36 platform + build-tools 36.0.0 + cmdline-tools 12.0 +
emulator 37.2.12 and side-by-side 37.3.4), AVDs in `~/.android/avd`. Gradle 9.3.1,
`org.gradle.jvmargs=-Xmx3g`; always `--no-daemon --max-workers=1`. `/tmp` is a small
tmpfs — keep large archives in `~/.cache`.
Notebook (for 5.10/5.11): Arch Linux, Java 17, emulator 37.2.12, AVD `stage5-api36` with
`ANDROID_AVD_HOME=/home/stokonn/.config/.android/avd`.

## Additive note — Stage 6 planning branch (2026-10-21, separate agent, docs only)

Added on the isolated `docs/goldberg-0.11-stage-6-planning` branch only; the Stage 5
branch/tree was not modified by this work. Stage 6 (community backend) **planning
documentation** is prepared at `docs/planning/goldberg-0.11/stage-6/` (10 documents +
additive STAGE-CHECKLIST/STAGES/DECISIONS/HANDOFF updates), committed and pushed on
that branch.

**Update (2026-10-21, same day — owner decision review completed):** the owner settled
the Stage 6 design decisions. Supabase Free + strict 0 CZK + graceful denial/fail-closed
(D4a/D4b), Frankfurt region (D5c), general-audience moderation with minimal data
(D5f/D5g), in-app Android Admin (D8a) and console bootstrap with MFA (D8c) are
**approved**; the Web Admin BFF (D8b) is **approved conditionally** on proving its
zero-cost fit before implementation; D5a is a provisional preference with **no
supplied contact** and D5b/D5d details plus D5e/D5f legal reviews are **deferred to
pre-launch gates**. Full outcomes: `docs/planning/goldberg-0.11/stage-6/OPEN-DECISIONS.md`
and `docs/ai/DECISIONS.md`.

**Stage 5 reconciliation:** this branch was cut before Stage 5 completion, so its
copies of STAGE-CHECKLIST/STATE/HANDOFF carried stale "Stage 5 in progress" text. The
canonical checklist on this branch now records **Stage 5 ✅ complete, owner-approved
2026-10-21** (CI runs 39557624554 + 39576173002, 12/12 screenshots; real-CDN transfer
measurements preserved as a future integration dependency) and points to the Stage 5
branch as the authoritative record. **Stage 5 was not reopened, rerun or modified** —
no Stage 5 code or evidence was touched.

**Stage 6 implementation is still NOT started and remains blocked** on a separate
implementation instruction, the D8b zero-cost proof (new task 6.0f), the remaining D5
pre-launch gates, and separately authorized provisioning (6.0d) / OAuth console setup
(6.0e). Nothing above in the Stage 5 handoff changes.

**Addendum (2026-10-21, Stage 6 C2/C3 session — docs/planning only):** conditions
C1–C3 of the 6.0f gate now read: **C1 approved** (architecture level);
**C2 executed → CONDITIONAL PASS** (isolated local `workerd` spike on
`spike/goldberg-0.11-c2-bff`, final commit `7bc6976`, spike code commit
`d2ce77b`: 19/19 security, P1–P7 executed, kernel CPU **2.03 ms/invocation**;
per-invocation production CPU **unverified**; §12 production-runtime
verification **owner-gated**; spike code remains isolated/unmerged);
**C3 documentary review complete**
(`docs/planning/goldberg-0.11/stage-6/C3-EDGE-PRIVACY-REVIEW.md`: conditional
privacy viability at 0 CZK, no compliance claim, legal/operator questions open —
controller/operator identity incl. the **minor-owner legal-capacity question is
now blocking for 6.0d provisioning**). **Still open: C2 §12 production
verification and C4 (terms re-verification). Task 6.0f stays UNCHECKED.**
Resume order for a fresh Stage 6 session: `AGENTS.md` →
`docs/planning/goldberg-0.11/stage-6/CHECKLIST.md` (progress log) →
`Q1-OPERATOR-READINESS.md` + `C4-PREPROVISIONING-CHECKLIST.md` →
`C3-EDGE-PRIVACY-REVIEW.md` §11 (pre-provisioning checklist) → GitHub Issue #31
latest checkpoint.

**Addendum (2026-10-21, Stage 6 Q1/C4 session — docs/planning only):** the two
remaining documentation conditions are now *prepared*, neither *closed*:
**Q1 research** (`stage-6/Q1-OPERATOR-READINESS.md`) records verified contractual
facts (no published minimum account age on either provider's current terms;
authority-to-bind warranties; CF SSSA §2.2.1(a); Czech Civil Code §§30–33; Google
age rule), the professional-review question set, and **operator Options A/B/C with
no option selected** — **D5b remains legally unresolved and blocking for 6.0d
provisioning**; no identity/contact data was requested or recorded, no legal
conclusion was drawn. **C4 pre-provisioning checklist**
(`stage-6/C4-PREPROVISIONING-CHECKLIST.md`) pins the current source versions with
access dates (incl. the dated Workers Logs → Observability pricing change of
2026-12-01 and the Supabase Free "no automatic backups" fact) and provides the
§D runbook; **the final C4 verification must still run immediately before any
separately authorized provisioning — C4 stays OPEN, 6.0f stays UNCHECKED.**
Retrieval failures recorded honestly: EUR-Lex GDPR text (0 bytes ×2) and the
Czech MoJ official English Civil Code PDF (TLS error). **Next step for a fresh
agent:** read the CHECKLIST progress log, then await the owner's decisions
(professional review of the Q1 §4 questions → choose Option A/B/C) — do not
provision, do not implement, do not touch 6.0e.

**Addendum (2026-10-21, Stage 6 cloud-readiness session — docs/planning only):**
the owner chose the **cloud architecture as the intended deployment path**
(Cloudflare Workers Free same-origin static + `/v1` BFF; Supabase Free
PostgreSQL/Auth in `eu-central-1` Frankfurt; permanent 0 CZK; no paid plans,
chargeable trials, upgrades or unapproved billing) **instead of the temporary
local-only Option C** — Option C is now only the current gated posture (nothing
provisioned, nothing may be until gates pass), not the intended end state.
Instruction authorized **documentation and verification planning only**: no
accounts, credentials, deployments, endpoints or code. The earlier "owner is a
minor" premise is **reclassified as an unverified assumption, not a verified
current fact**; no age/identity/family detail may be requested or recorded, and
**D5b stays legally unresolved and blocking for 6.0d** (account registration ≠
compliance; eligibility, legal capacity, authority-to-bind and the GDPR
controller role require professional verification — Q1 §4 unchanged). New
deliverable: `stage-6/CLOUD-READINESS-PLAN.md` — (a) reconciled assumptions;
(b) the **6.0f ↔ provisioning gate-ordering** conflict analyzed with an
explicit minimal sequence (documentary verification → contractual readiness →
owner-authorized *limited private verification* provisioning → day-of C4 →
isolated synthetic-data production-runtime C2 verification → 6.0f accept/
reject → separate implementation authorization → later launch gates) and
alternatives **ORD-A/B/C left pending as an owner decision**; (c) the exact
future private-cloud verification runbook **CR-0…CR-8** (account readiness,
MFA/secrets, no-billing verification, Frankfurt region selection, minimal
private test infra, CPU-per-invocation/cold-start/Supabase-latency/quota/
fail-closed/logging measurements, mandatory recorded rollback+cleanup —
synthetic data only); (d) Free-tier usage estimates (0 CZK, no billing method)
and binding stop/rollback conditions. **Next step for a fresh agent:** read
`stage-6/CLOUD-READINESS-PLAN.md` §9 (pending owner decisions) and the CHECKLIST
progress log, then **await the owner's gate-ordering decision (ORD-A/B/C) and
the D5b professional-review routing — do not provision, do not create accounts,
do not implement, do not touch 6.0e.** 6.0f stays unchecked; Stage 6
implementation stays NOT STARTED; Stage 5 remains COMPLETE.

created by stokonn with love <3
