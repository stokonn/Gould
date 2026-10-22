# Goldberg 0.11 — Stage 5 VERIFICATION ledger

> **NOTE (2026-10-21, Stage 6 planning branch):** frozen historical ledger from before
> Stage 5 completion. Stage 5 is COMPLETE and owner-approved (2026-10-21); the final
> executed/not-executed record (incl. runs 39557624554 and 39576173002) lives on
> `feature/goldberg-0.11-stage-5`. Rows below are retained unchanged as historical
> evidence — no row was edited, added or removed here.

## Stage 6 documentation sessions (2026-10-21 — planning branch; no application code)

| # | Action | Result |
|---|---|---|
| 1 | C2 spike execution (separate owner authorization; isolated branch `spike/goldberg-0.11-c2-bff`, loopback-only, synthetic identities, no accounts/deployments) | **EXECUTED → CONDITIONAL PASS**: security **19/19**, performance **P1–P7 executed**, kernel-measured average CPU **2.03 ms/invocation** (flat under injected latency; 10 ms Workers Free ceiling re-verified from official pricing docs). Per-invocation production CPU **NOT verified** (local upper-bound argument only); §12 production-runtime verification **not executed — owner-gated**. Evidence: `spikes/bff-c2/results/SUMMARY.md` (spike branch, final commit `7bc6976`). |
| 2 | C3 edge-privacy documentary review (Cloudflare + Supabase + EU institutional sources; all accessed 2026-10-21) | **EXECUTED (documentation)** — `stage-6/C3-EDGE-PRIVACY-REVIEW.md`: 18 primary sources recorded with titles/access dates; conditional-viability verdict; no compliance claim. Not executed: any provisioning-time verification (§11 checklist), the Supabase subprocessor PDF parse, the Supabase TIA retrieval, any legal review. |
| 3 | C2/C3 status reconciliation across Stage 6 + `docs/ai/*` docs | **EXECUTED** — stale "C2 UNEXECUTED" current-state statements updated or superseded-noted (historical specs retained verbatim under supersession headers); C1/C2/C3/C4 statuses consistent everywhere; 6.0f still unchecked. |
| 4 | Q1 operator-readiness research (Cloudflare/Supabase/Google terms + Czech Civil Code; all accessed 2026-10-21) | **EXECUTED (documentation)** — `stage-6/Q1-OPERATOR-READINESS.md`: verified facts with sources (no published minimum account age on either provider; authority-to-bind warranties; CF SSSA §2.2.1(a); Czech CC §§30–33; Google age rule), Options A/B/C documented **without selection**, professional-review question set. **NOT executed / failed:** GDPR official text (EUR-Lex CELEX:32016R0679) retrieval — **0 bytes, 2 attempts** (environment-blocked; recorded as X-6); Czech MoJ official English Civil Code PDF — **curl exit 60, TLS failure** (authentic Czech text used instead); any account-signup observation; any legal review. **No legal conclusion drawn; D5b unchanged (unresolved/blocking).** |
| 5 | C4 pre-provisioning checklist preparation (documentation only) | **EXECUTED (documentation)** — `stage-6/C4-PREPROVISIONING-CHECKLIST.md`: per-item URLs, version/date stamps (CF SSSA 2025-09-12, CF DPA v6.4, CF pricing upd. 2026-10-02 w/ Workers Logs→Observability change 2026-12-01, Supabase ToS v2/2026-05-06, Supabase DPA v1/2026-08-01, Free-tier facts incl. 1 h auth / 1 day API-DB log retention and no automatic backups), verified-2026-10-21 vs must-reverify columns + §D provisioning-day runbook. **NOT executed:** the final C4 verification itself (stays OPEN), subprocessor PDF parse (SB-P9), TIA check (SB-P10), any console/API session with either provider, any account creation. |
| 6 | Cloud-readiness session (documentation-only): operator-decision reconciliation + gate-ordering analysis + future verification runbook | **EXECUTED (documentation)** — new `stage-6/CLOUD-READINESS-PLAN.md` (cloud architecture = intended deployment path instead of temporary local-only Option C; "minor owner" premise reclassified as an **unverified assumption**; 6.0f ↔ provisioning conflict analyzed with sequence S1–S8 and alternatives **ORD-A/B/C left pending as an owner decision**; private-cloud verification runbook CR-0…CR-8, synthetic data only; 0 CZK estimates; stop/rollback conditions); synchronized updates in `stage-6/CHECKLIST.md`, `OPEN-DECISIONS.md` §6, `Q1-OPERATOR-READINESS.md` (reconciliation note), `C3-EDGE-PRIVACY-REVIEW.md` (reconciliation note), `BFF-FEASIBILITY.md` §8, `README.md`, `STAGE-CHECKLIST.md` Stage 6, `docs/ai/*`. **NOT executed:** any account creation, provisioning, deployment, OAuth configuration, C4 final verification, C2 §12 production measurement, legal review. **No gate closed; 6.0f stays unchecked; D5b unchanged (unresolved/blocking).** |
| 7 | ORD-A approval session (documentation-only): record the owner's ORD-A sequencing decision and reconcile gates | **EXECUTED (documentation)** — `stage-6/CLOUD-READINESS-PLAN.md` updated: §3 rewritten as the **approved 11-step sequence** (ORD-A, supersession-marked), §4 replaced with **ORD-A APPROVED 2026-10-21 (sequencing only; ORD-B/ORD-C not selected)** + the reconciled **gate status table** (§4.2) + preserved not-selected alternatives (§4.3); new **ORD-A readiness checklist** in §5 (aligned with CR-0…CR-8: resolved Q1/D5b, confirmed authority to accept contracts, separate temp-resource approval, day-of C4, CF Workers Free only, Supabase Free `eu-central-1`, no billing/paid features, MFA, minimal temporary protected resources, synthetic data only, no public launch, CPU/cold-start/latency/quota/fail-closed measurements, secret-safe minimized logging, mandatory deletion + credential revocation, recorded cleanup evidence, immediate stop on blockers); owner-facing Q1 §4 question list (§9.1, identity-free); synchronized updates in `stage-6/CHECKLIST.md` (6.0f + progress log), `OPEN-DECISIONS.md` §6, `README.md`, `STAGE-CHECKLIST.md` Stage 6, `docs/ai/{STATE,HANDOFF,DECISIONS}.md`. **NOT executed:** any account creation, provisioning, deployment (incl. test Worker), OAuth configuration, credential access, C4 final verification, C2 §12 production measurement, legal review, money spending. **No gate closed; 6.0f stays unchecked; Q1/D5b, C2 §12, C4 stay open; 6.0d/6.0e stay NOT AUTHORIZED.** |
| 8 | Q1 natural-person operator reassessment (documentation-only): re-verify provider terms + Czech/EU sources, add candidate Q1-D, build the A/B/C/D decision matrix, record the free/non-commercial product direction | **EXECUTED (documentation)** — `stage-6/Q1-OPERATOR-READINESS.md` rewritten: REASSESSMENT NOTE + corrected standing constraints, re-verified §§30–37, new §1.6 (Czech business-status/trade-licensing: Civil Code §§15/18/420–421, Trade Act 455/1991 §2 + §3(1)(b)), §1.7 **PD-1 product direction** (free/non-commercial; no monetization approved or implemented — owner statement), §1.8 **QD-1…QD-10 findings classification**, §2 extended with Q2.9–Q2.14, §4 extended with **Q4.9–Q4.13**, §5 **Option D** (legally capable natural person in own name, candidate/not approved), **§5.5 A/B/C/D matrix**, §5.6 technical/non-legal recommendation (evaluate D first after review), §10 phased GDPR controller readiness; capacity framed purely as an **eligibility precondition, no personal facts**. Sources accessed 2026-10-21: Cloudflare SSSA (full text re-read), Supabase ToS at canonical `https://supabase.com/terms` (old `/company/terms` → 404), Czech Civil Code verze 18 (zakonyprolidi), Trade Act via e-Sbírka/zakonyprolidi/MPO, GDPR Art. 2/4/8 via **unofficial** gdpr-info.eu mirror. Synchronized: `stage-6/{README,CHECKLIST,OPEN-DECISIONS,CLOUD-READINESS-PLAN,C4-PREPROVISIONING-CHECKLIST}.md`, `STAGE-CHECKLIST.md` Stage 6, `docs/ai/{STATE,HANDOFF,DECISIONS,VERIFICATION}.md`. **NOT executed / failed:** official EUR-Lex GDPR retrieval — **0 bytes ×5 total** (environment-blocked; X-6 still ❌), any legal/professional review, any account-signup observation, any account creation, provisioning, deployment, OAuth configuration, implementation, money spending. **No option selected; no legal conclusion; no GDPR compliance claim; D5b unchanged (unresolved/blocking); 6.0f stays unchecked; 6.0d/6.0e stay NOT AUTHORIZED.** |
| 9 | Q1 professional-review handoff brief (documentation-only): compile the identity-free routing package with primary-source references, neutral A/B/C/D framing, official Czech-DPA + European Commission guidance, and the pre-resolution confirmation checklist | **EXECUTED (documentation)** — new `stage-6/Q1-PROFESSIONAL-REVIEW-BRIEF.md`: all **13 questions Q4.1–Q4.13 preserved verbatim** from Q1 §4 with per-question primary-source citations; neutral A/B/C/D table with **Q1-D explicitly a candidate, not approved**; §3 verified law (GDPR Art. 2/4(7)/8 via non-official mirror — official EUR-Lex still not retrieved, X-6; Czech CC verze 18; Trade Act 455/1991) vs **official guidance fetched 2026-10-22** — ÚOOÚ *Základní příručka* (https://uoou.gov.cz/verejnost/zakladni-prirucka-k-ochrane-udaju: controller responsibility + demonstrable compliance, risk-based self-analysis, legal form not decisive, cloud provider as typical processor, Art. 28(3) written contract, 1-month rights deadline, 72-hour breach rule), ÚOOÚ *Desatero* announcement (small controllers/živnostníci guidance), ÚOOÚ consultation practice, EC *Application of the GDPR* (household exception wording, "(paid or for free)" scope, Art. 4(7) role tests, SME/risk-based thresholds) and EC *Obligations* (Arts. 12–14/25/32/33–35/37–39) — each quoted with access date; §6 **C-1…C-6 confirmations required before Q1/D5b can be resolved**; §7 output format; §8 standing restrictions. Synchronized: stage-6 `README.md`, `OPEN-DECISIONS.md` (D5b row), `CHECKLIST.md` (progress log), `STAGE-CHECKLIST.md` Stage 6, `docs/ai/{STATE,HANDOFF,VERIFICATION}.md`. **NOT executed:** any legal/professional review (the brief only prepares for it), any personal-data request or record, any account creation, provisioning, deployment, OAuth, credentials, implementation, real user data, monetization, Stage 7 work, money spending. **No option selected; no approval simulated or claimed; D5b UNRESOLVED/blocking for 6.0d; C4 OPEN; 6.0f UNCHECKED; 6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing-only; 0 CZK unchanged.** |

**No application source, test source, Gradle or dependency change was made in
any Stage 6 session. No credentials, personal data or contact details were
introduced.**

Honest executed vs not-executed record. Only rows marked **EXECUTED** were actually run.

## Executed — CI activation session (2026-10-20, fourth session, desktop host)

Phase A/B preparation for KVM CI verification (no CI run, no emulator run, no
instrumentation executed this session — nothing is claimed beyond these host-side checks):

| # | Command / action | Result |
|---|---|---|
| 1 | `apksigner verify --print-certs` (build-tools 36.0.0) on the downloaded trusted `Goldberg-v0.10.1.apk` | **EXECUTED** — APK sha256 `854bbfebf617ec920df60d181a12e0dcbd2fed10052f2479903a78a290cef312` (matches expected); signer cert SHA-256 `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e` = the trusted certificate (DN `C=US, O=Android, CN=Android Debug`). Proves a CI-runner debug-signed candidate **cannot** be installed in place over the release-signed baseline (different keystore) → workflow revised to rebuild the v0.10.1 baseline with the same runner debug keystore; production credentials stay out of CI. |
| 2 | AOSP source inspection of API 36 network APIs (`NetworkPolicyManagerShellCommand`, `NetworkPolicyManagerService.dump`, `ConnectivityService`, `NetworkAgentInfo.toString`) | **EXECUTED** — found the previous workflow used a **non-existent command** `cmd netpolicy set data_saver enable` on API 36; correct form is `set restrict-background true\|false` with `get restrict-background` → `Restrict background status: enabled\|disabled`; `dumpsys netpolicy` reports `Restrict background: true\|false`; default-network transport is assertable via `Active default network: <id>` + the id's `NetworkAgentInfo … ni{type: WIFI\|MOBILE …}` block. |
| 3 | `git ls-remote --tags` verification of all five SHA-pinned actions | **EXECUTED** — checkout `11d5960…`=v4.4.0, setup-java `cf277c6…`=v4.9.1, setup-android `9fc6c4e…`=v3.2.2, android-emulator-runner `a421e43…`=v2.38.0, upload-artifact `ea165f8…`=v4.6.2 — every pin resolves to its claimed release tag. |
| 4 | Workflow revision + full local validation (`yaml.safe_load`; `bash -n` on all 12 run/emulator scripts; `py_compile` + functional tests of both embedded Python snippets) | **EXECUTED** — all pass. The default-network parser was unit-tested against AOSP-format dumps: WIFI→OK, MOBILE→OK, transport-mismatch→FATAL, `none`→FATAL (one parser bug found and fixed during this: indented `NetworkAgentInfo` lines). |
| 5 | Exclusion scan: `@Ignore` in `app/src/androidTest`, `testInstrumentationRunnerArguments`/`excludeClasses`/`managedDevices` in Gradle | **EXECUTED** — **0 matches**: the CI `connectedDebugAndroidTest` runs all 42 androidTest classes with no filters, exclusions or skips. |
| 6 | v0.10.1 tag build-config inspection (`git show v0.10.1:…`) | **EXECUTED** — Java 17, compileSdk 36, Gradle 9.3.1, AGP 9.1.0, versionName `0.10.1`, versionCode 11 → the tag rebuild will work with the same CI toolchain and satisfies `verify_emulator_upgrade.py --baseline-version 0.10.1 --baseline-schema 8`. |
| 7 | `origin/main` helper-script inspection | **EXECUTED** — main's `verify_emulator_upgrade.py` is an older revision without the `--baseline-schema/--expected-schema/--offline/--startup-runs` flags → the workflow must be dispatched against `feature/goldberg-0.11-stage-5` (documented in the workflow header). |
| 8 | CI-only branch + draft PR creation (worktree `/tmp/opencode/goldberg-ci-only` off `origin/main`, branch `ci/stage5-emulator-verification`) | **EXECUTED** — commit `9870d99`; `git diff origin/main --stat` = **1 file** (`.github/workflows/stage5-emulator.yml`, +379); draft [PR #32](https://github.com/stokonn/Goldberg/pull/32) opened, **not merged**; `gh pr diff --name-only` confirms the single workflow file. Stage 5 working tree and all untracked files (`.pmp/`, `.pmp.yml`, `RELEASE_NOTES.md`, stage-4 checkpoint archive) untouched. |

## Not executed — CI activation session (no claim)

- **Any GitHub Actions run** — the workflow has never been dispatched; PR #32 awaits the
  owner's Phase C approval (merge + manual dispatch are gated).
- Any emulator run, any `Stage5*Test` instrumentation, any 5.11 acceptance item — all
  still pending the CI run (or notebook); tasks 5.10/5.11 remain open.
- The strengthened Data Saver/Wi-Fi assertions were validated against AOSP source and
  synthetic dumps only; their first real-device-format execution will be the CI run
  (a format surprise there would be diagnosed, never weakened away).

## Executed — third resume session (2026-10-20, desktop machine)

| # | Command | Result |
|---|---|---|
| 1 | `./gradlew --no-daemon --max-workers=1 :app:assembleDebug :app:assembleDebugAndroidTest` | **BUILD SUCCESSFUL**, 1m08s (Java 25 host); only known nullable-`listFiles()` warnings. Fresh `app-debug.apk` 32,388,569 B sha256 `44222c59…a157`; `app-debug-androidTest.apk` 1,331,040 B sha256 `6030e528…a8f2d` (Oct 20 21:13). |
| 2 | `sdkmanager "system-images;android-36;google_apis;x86_64"` (cmdline-tools 12.0 installed first) | Image r07 installed. |
| 3 | `avdmanager create avd -n stage5-api36 -k system-images;android-36;google_apis;x86_64 -d pixel_6` | AVD created (`~/.android/avd/stage5-api36`). |
| 4 | Emulator boots (explicit `-port 5580` / serial `emulator-5580`, headless): API 37 + swiftshader ×2 (37.2.12); API 37 + auto/lavapipe (37.2.12, ran 13 min, guest never booted, adb offline); API 36 + swiftshader (37.2.12); API 36 + auto/lavapipe (37.2.12); API 36 + swiftshader (37.3.4 canary) | **FAILED — `Killed by SIGSEGV`** on every completed attempt after `WARNING: cannnot unmap ptr … protected range …`. Known upstream bug (Google #566807521), Fedora kernel 7.2.10; unfixed in 37.3.4. **No emulator runs; no instrumentation executed.** |
| 5 | `python3 scripts/check_agent_policy.py` | **PASS** (exit 0): canonical policy, thin agent references, Cursor frontmatter, local Wiki navigation; runtime agent loading not tested. |
| 6 | `python3 scripts/check_backup_fixtures.py` | **PASS** (exit 0): 0.6.0-f1 / 0.9.1-f2 / 0.9.2-f3 fixtures → schema 10, all portable fields retained. |
| 7 | `python3 scripts/check_history_paging.py` | **PASS** (exit 0): 106 sessions / 4 pages + legacy association cases. |
| 8 | `python3 scripts/check_localization.py` | **PASS** (exit 0): 922 EN/CZ resources, placeholders, Czech plurals. |
| 9 | `python3 scripts/check_migrations.py` | **PASS** (exit 0): v5–v9 → v10 DDL, legacy data retained; bridge allocation requires Android tests (noted, not run). |

Host gate executed this session (2026-10-20, desktop):

| # | Command | Result |
|---|---|---|
| 10 | `./gradlew --no-daemon --max-workers=1 clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug` | **BUILD SUCCESSFUL**, 3m24s, 83/84 tasks executed. `testDebugUnitTest` **174 tests / 0 failures / 0 errors / 0 skipped** (30 classes). Lint **0 errors / 110 warnings** (2 in Stage 5 offline code: `UsableSpace`, `UseKtx`; rest pre-existing). Fresh clean-build `app-debug.apk` 22,508,542 B sha256 `b6ad01206d24b74e371edd16b9aec26d720f41afca8412557f1f7b21e3fb88ff` — **same byte size as the notebook checkpoint APK** (22,508,542 B), cross-machine source-identity evidence. `app-debug-androidTest.apk` 988,287 B (clean build). Note: the earlier incremental build (row 1) produced different-size APKs; the clean-build artifacts supersede them as the authoritative Stage 5 host-gate outputs. |
| 11 | GUI-mode emulator attempt (owner-requested): `DISPLAY=:0 XAUTHORITY=… .mutter-Xwaylandauth.* emulator -avd stage5-api36 -port 5580 -gpu auto` (no `-no-window`), emulator 37.2.12 | First launch aborted before QEMU (missing X authority in tool shell). Relaunch with session XAUTHORITY: Qt window created on the GNOME desktop, then **`Killed by SIGSEGV`** after the protected-range warning → **GUI mode also fails; no ADB device ever appeared.** All local emulator retries stopped per owner instruction. |
| 12 | CI preparation: verified GitHub-hosted runner KVM support + `reactivecircus/android-emulator-runner` v2.38.0 (pinned `a421e43855164a8197daf9d8d40fe71c6996bb0d`); wrote `.github/workflows/stage5-emulator.yml` (workflow_dispatch only, SHA-pinned actions, explicit emulator-NNNN + `ro.kernel.qemu=1` guards); `yaml.safe_load` validation; `py_compile` on `verify_emulator_upgrade.py`/`emulator_ui.py`; `bash -n` on `measure-startup.sh`; trusted baseline 0.10.1 APK digest `854bbfebf617ec920df60d181a12e0dcbd2fed10052f2479903a78a290cef312` recorded from the GitHub release API | **Workflow prepared locally; later committed+pushed as `1ef15bf` and revised in the CI-activation session (see the top section) — no CI results claimed.** |

## Not executed — third session (no claim)

- All `Stage5*Test` instrumentation (task 5.10) and the emulator acceptance matrix
  (task 5.11) — blocked by the desktop emulator bug (row 4, GUI mode too per row 11);
  execution paths are the notebook (per `docs/ai/HANDOFF.md`) or the prepared CI
  workflow (row 12) — neither has produced results yet. F1/F2 runtime proof still
  pending that run.
- `scripts/emulator_ui.py` / `verify_emulator_upgrade.py` / `measure-startup.sh` (need a
  booted emulator).

## Executed — second resume session (2026-10-20, battery-emergency stop)

| # | Command | Result |
|---|---|---|
| 1 | `./gradlew --no-daemon --max-workers=1 :app:assembleDebugAndroidTest` (after L324 fix) | **BUILD SUCCESSFUL**, 2m02s, 0 errors; known nullable-`listFiles()` warnings only. |
| 2 | `./gradlew --no-daemon --max-workers=1 :app:testDebugUnitTest --rerun` | **174 tests / 0 failures / 0 errors / 0 skipped** (30 classes, fresh). |
| 3 | `./gradlew --no-daemon --max-workers=1 :app:testDebugUnitTest :app:assembleDebugAndroidTest` (after F1 retarget + F2 `installBlob` production fix) | **BUILD SUCCESSFUL**, 2m10s; fresh JVM reports 19:23 → **174 / 0 / 0 / 0**. |
| 4 | `sdkmanager "emulator" "system-images;android-36;google_apis;x86_64"` | Installed emulator 37.2.12.0 + system image r07. |
| 5 | `avdmanager create avd -n stage5-api36 …` (needs `ANDROID_AVD_HOME=/home/stokonn/.config/.android/avd` for the emulator binary) | AVD created. |
| 6 | Emulator boot `… -avd stage5-api36 -port 5580 -no-window …` | **emulator-5580** booted; API 36 / Android 16 / `ro.kernel.qemu=1`; boot completed ~82s. |
| 7 | `adb -s emulator-5580 install -r` (app + androidTest APK) | Both `Success` — **but app-debug.apk was the stale Oct 16 build at that moment**. |
| 8 | `./gradlew --no-daemon --max-workers=1 :app:assembleDebug` | **BUILD SUCCESSFUL**, 5m44s → fresh `app-debug.apk` Oct 20 19:45, 22,508,542 bytes. **Not yet reinstalled.** |
| 9 | `adb -s emulator-5580 emu kill` | `OK: killing emulator` — clean stop at battery warning. |

## Executed — first resume session (2026-10-20)

| # | Command | Result |
|---|---|---|
| 1 | `:app:assembleDebugAndroidTest` (after runner-test fixes, before worker test) | BUILD SUCCESSFUL 4m54s, 0 errors. |
| 2 | `:app:testDebugUnitTest` | UP-TO-DATE → 0 tests re-executed (superseded by row 2 above). |

## Last previously executed evidence (prior session, per STAGE-CHECKLIST)

- Full host gate `clean assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug`:
  BUILD SUCCESSFUL, **146 tests / 0 failures**, lint 0 errors (109 warnings).
- After tasks 5.5–5.9: `testDebugUnitTest` **174 tests / 0 failures** (now reproduced fresh).

## Not executed this session (no claim)

- **All instrumentation tests** (`Stage5BundleStoreTest`, `Stage5BundleJobRunnerTest`,
  `Stage5BundleWorkerTest`) — emulator was booted and identified (emulator-5580) but the
  battery emergency stopped the session **before any test execution** (tasks 5.10/5.11 open).
- `lintDebug`, `assembleDebug` was run (row 8) but the full gate + `scripts/check_*.py`
  (migrations/backup/localization/paging/policy) — task 5.12 not run.
- Emulator acceptance matrix (Wi-Fi→mobile, Data Saver, consent revocation mid-stream, real
  process death, low disk, seeded in-place upgrade from trusted 0.10.1, cost measurements).
- F1/F2 fixes are compile-verified + JVM-regression-free; their **runtime** proof requires the
  instrumentation run in 5.10 (pending).

## Warning/error inventory at checkpoint

- androidTest compile: **0 errors**; warnings: 7+ nullable `listFiles()` receivers (runner
  test), 1 pre-existing `Stage3bBackupTest` reified-type warning.
- Installed-on-emulator app APK is stale (Oct 16) — reinstall fresh APKs before running tests.

## Executed — Stage 6 C6 contract spike (2026-10-22, isolated branch, local-only)

Evidence links below point at the stable spike commit
[`d2a95d4`](https://github.com/stokonn/Goldberg/tree/d2a95d4b83c634aacb4f7f74432f2bd8980cf218/spikes/contract-c6)
because the executable `spikes/contract-c6/` directory is intentionally not
part of this planning branch (code isolation preserved).

| # | Command | Result |
|---|---|---|
| C1 | `node tools/execute.mjs` (in `spikes/contract-c6/`) | **PASS — 79 tests / 79 pass / 0 fail**, exit 0, run `2026-10-22T06:40:20Z`→`06:40:23Z`, Node v22.23.1; static isolation scan: 17 files, **0 forbidden findings** (no http/net/dns/fetch/undici/wrangler surface; `child_process` only for the in-repo race test). Raw: [tap.txt](https://github.com/stokonn/Goldberg/blob/d2a95d4b83c634aacb4f7f74432f2bd8980cf218/spikes/contract-c6/results/raw/tap.txt), [run.json](https://github.com/stokonn/Goldberg/blob/d2a95d4b83c634aacb4f7f74432f2bd8980cf218/spikes/contract-c6/results/raw/run.json), summary-data.json (same directory). |
| C2 | `node --test tests/*.test.mjs` (run 1, debugging) | **69/79 pass, 10 fail** — all root-caused and preserved: 1 real server bug (uncaught idempotency `HttpError` → now 422 response), 6 test-side bugs (invalid revision flow; session TTL vs 24 h fake-clock jumps; wrong error-envelope requestId path; `GENESIS` first-link hash), 2 SSRF gaps (WHATWG hex-form v4-mapped `::ffff:7f00:1`; non-4-part numeric host) — see [SUMMARY.md §1 failure log](https://github.com/stokonn/Goldberg/blob/d2a95d4b83c634aacb4f7f74432f2bd8980cf218/spikes/contract-c6/results/SUMMARY.md). |
| C3 | `node --test tests/*.test.mjs` (run 2, after fixes) | **79/79 pass, 0 fail**, exit 0. |

Suite coverage executed: V1 validation (16), V2 authz (14), V3 existence (9),
V4 moderation (10), V5 idempotency (7), V6 failures (7), V7 SSRF-static (8),
V8 audit chain (6), V9 multi-process race (10 trials + 1 hygiene = 12 tests).

**Not executed / no claim:** C2 §12 production runtime, all [I]-venue rows, real
Postgres isolation, HTTP/JWT/CSRF/CORS (C2 scope), CPU measurement (C2),
tasks 6.1–6.18, Android/emulator checks (untouched), lint (no app code).
`node:sqlite` ExperimentalWarning recorded honestly in TAP. No integration or
production readiness is claimed; 6.0f stays UNCHECKED.

## Executed — Stage 6 Cloudflare MCP readiness verification (2026-10-22, read-only)

Owner-instructed, read-only session against the official Cloudflare MCP server
(OAuth, read-only permissions). **No cloud resource created/modified/deleted;
no OAuth permission, billing or account setting changed; no credential
accessed or recorded; no write scope exercised.** Non-identifying facts only
(no email, account ID or token in any repository file).

| # | Read-only call | Result |
|---|---|---|
| M1 | `GET /user` (MCP connection check) | **EXECUTED — HTTP 200, success.** Connection authenticates via OAuth. |
| M2 | `GET /user/tokens/verify` | **EXECUTED — 401 "Invalid API Token".** **Expected, not a defect:** that endpoint validates API tokens, not OAuth bearer sessions (M1 proves the OAuth session works). |
| M3 | `GET /accounts` | **EXECUTED — 1 standard account**, created 2026-10-22, not suspended, no pro/business/enterprise zones, account-level 2FA-enforcement flag off. |
| M4 | `GET /user` (account fields) | **EXECUTED — user-level two-factor authentication ENABLED** (D8c-relevant when actionable). |
| M5 | `GET /user/subscriptions` | **EXECUTED — 403, "Authentication error": no OAuth scope covers this endpoint.** **Billing state is NOT inspectable via this MCP connection; inaccessible billing is explicitly NOT evidence of zero costs** — the 0 CZK posture is enforced by the C4/ORD-A gates, never inferred from a tool limitation. |
| M6 | `GET /accounts/{id}/workers/scripts` | **EXECUTED — 0 Worker scripts.** |
| M7 | `GET /accounts/{id}/workers/subdomain` | **EXECUTED — 404, code 10007:** workers.dev subdomain **not yet created** (auto-created only on the first Workers & Pages dashboard visit — not performed). |
| M8 | `GET /zones` (filtered by account) | **EXECUTED — 0 zones** (no domains on the account). |
| M9 | `GET /accounts/{id}/d1/database` | **EXECUTED — 0 D1 databases.** |
| M10 | `GET /accounts/{id}/storage/kv/namespaces` | **EXECUTED — 0 KV namespaces.** |
| M11 | `GET /accounts/{id}/r2/buckets` | **EXECUTED — 403, code 10042:** R2 **not enabled** on the account (dashboard enablement required; not performed — R2 is not needed by the approved architecture). |

**Read-scope evidence:** the Workers R2 *read* scope is present on the
connection (error text of M11 confirms it); billing endpoints are refused for
lack of any OAuth scope (M5). Read-only posture therefore verified from
observed behavior, not assumed.

**Not executed / no claim:** any write/create/update/delete call of any kind;
any billing inspection, change or interpretation; any provisioning,
deployment, Wrangler action or OAuth scope change; C4 final verification
(stays OPEN); C2 §12 production-runtime measurement (stays OPEN — no Worker
deployed); any Supabase inspection (out of MCP scope — Supabase remains
unverified/unprovisioned from this side); legal/professional review.
**No gate closed: Q1/D5b UNRESOLVED (the existing account does NOT resolve
it); 6.0f UNCHECKED; 6.0d/6.0e NOT AUTHORIZED; 6.1–6.18 unchecked; ORD-A
sequencing-only; Stage 7 untouched; 0 CZK/month unchanged.**

## C6-PG spike — executed verification record (2026-10-22)

- **Executor (executed):** `node tools/execute-pg.mjs` with
  `C6_PG_URL=postgres://c6spike@127.0.0.1:54329/c6spike` on branch
  `spike/goldberg-0.11-stage-6-contract-pg` @ `f7bef8e` (evidence committed at
  `e37ed00`), Node v22.23.1. Exit 0.
- **PostgreSQL venue (real engine):** `tests/` v1–v10 → **86 tests, 86 pass,
  0 fail, 0 skipped** — PostgreSQL **17.11** (podman `c6-pg`, removed after
  capture), `default_transaction_isolation = read committed`, host port
  `127.0.0.1:54329` (loopback only, trust auth), synthetic data, per-test
  schemas. Raw TAP:
  [tap-postgres.txt](https://github.com/stokonn/Goldberg/blob/e37ed00b9205ce22e9e8ec494651e900708ee1e3/spikes/contract-c6/results/pg/raw/tap-postgres.txt).
- **SQLite post-port regression venue:** **86 tests, 79 pass, 0 fail,
  7 skipped** (v10 is PostgreSQL-only; the skips are honest). Raw TAP:
  [tap-sqlite-postport.txt](https://github.com/stokonn/Goldberg/blob/e37ed00b9205ce22e9e8ec494651e900708ee1e3/spikes/contract-c6/results/pg/raw/tap-sqlite-postport.txt).
  Baseline comparison: the original C6 run at `d2a95d4` was 79/79 — no
  regression introduced by the async port.
- **Static isolation scan:** 20 files, 0 forbidden findings (only socket use:
  `pg` driver → loopback instance).
- **Observed race outcomes (not asserted away):** v9 SQLite 10/10
  `[201,409]`; v9 PostgreSQL 10/10 `[201,500]`/`[500,201]` (10/10 T10-invariant
  deviations), plus deterministic ISO-1..ISO-3 probes in v10 — findings
  F1–F5 written up in
  [results/SUMMARY-PG.md](https://github.com/stokonn/Goldberg/blob/e37ed00b9205ce22e9e8ec494651e900708ee1e3/spikes/contract-c6/results/SUMMARY-PG.md).
- **Not executed / not claimed:** Supabase-hosted (PostgREST) runs — blocked
  by D5b/Q1 + C4 + ORD-A steps 4–6; production implementation tests
  (6.1–6.18); HTTP/CSRF/CORS layer (C2 scope); instrumentation on any device
  (no emulator/device work in this spike); any [I]-venue row.
- **Cleanup:** container `c6-pg` stopped and removed after evidence capture;
  `.scratch/` is gitignored; no cloud resources, no billing, no user data.

**Gates unchanged: 6.0f UNCHECKED; 6.1–6.18 unchecked; Q1/D5b UNRESOLVED
(Q1-D preference only); C4 OPEN; C2 §12 OPEN; ORD-A sequencing-only;
0 CZK/month; Stage 7 untouched; Stage 5 remains COMPLETE.**

## Executed — Stage 6 C4 preliminary verification & documentation reconciliation (2026-10-22, documentation-only)

**Method:** public document fetches only (webfetch/curl). No console login, no
MCP/API billing call, no resource created, no payment method inspected, no
account setting touched. Reconciliation target: the 2026-10-21 source set in
the C4 checklist §E.

| # | Source | Result (accessed 2026-10-22) |
|---|---|---|
| V-1 | Cloudflare Workers pricing | **Workers Free available** — 100k req/day, 10 ms CPU/invocation, static assets free & unlimited; "Last updated Oct 2, 2026" = unchanged; Workers Logs Free 200k events/day, 3-day retention; "Beginning December 1, 2026 …" announcement unchanged |
| V-2 | Cloudflare Observability pricing (**NEW source, not in §E**) | **"Last updated Oct 15, 2026":** Free = 0.5 GB/day ingestion + 7-day retention included, **additional ingestion unavailable on Free** (hard stop, resumes 00:00 UTC), storage not separately metered; automatic Free/Pro/Business migration **2026-12-01**; Log Explorer separate $1/GB, 30-day pool → **preliminary 0-CZK read; re-verify day-of and ≥ 2026-12-01** |
| V-3 | Cloudflare Self-Serve Subscription Agreement | "Last Updated September 12, 2025" — unchanged |
| V-4 | Cloudflare Customer DPA | "Version 6.4 … effective April 3, 2026" — unchanged |
| V-5 | Cloudflare Workers limits | dateModified 2026-10-12 — unchanged; fail-closed error-routing code **1027** present |
| V-6 | Cloudflare subprocessors page | "Last Updated: October 1, 2025" — unchanged |
| V-7 | Cloudflare Website Terms | **date not re-extractable this session** (client-rendered) — honest gap; last verified Effective 2025-08-01 on 2026-10-21; remains ⚠ day-of |
| V-8 | Supabase pricing | **Free available** — 500 MB DB (shared CPU) / 5 GB egress / 5 GB cached / 1 GB storage / 50k MAU / 2 projects / 1 GB log ingestion / audit 1 h / API+DB logs 1 day / unlimited API requests / pause 1 week; **Automatic backups "Not included" on Free** |
| V-9 | Supabase backups docs | Daily backups Pro (7 d) / Team (14 d) / Enterprise; official guidance: "free tier plan projects regularly export their data using the Supabase CLI `db dump` command and maintain off-site backups" |
| V-10 | Supabase billing FAQ | Free quota exceed → notification → grace period → **"service restrictions will apply"**: project pause, database read-only, disabled new launches/transfers, **402 for all API requests**; no-prior-notice only for suspected abuse |
| V-11 | Supabase regions | **`eu-central-1` "Central EU (Frankfurt)" listed under Specific regions**; "Region selection is a data-location control, not proof of regulatory compliance" unchanged |
| V-12 | Supabase DPA | "Version 1 — August 1, 2026" — unchanged |
| V-13 | Supabase Terms of Service | **version badge not re-renderable this session** — honest gap; last verified "Version 2 — May 6, 2026" on 2026-10-21; remains ⚠ day-of |
| V-14 | Supabase subprocessor list page | "Updated October 12, 2026" / "Updated June 1, 2026" — unchanged; **PDF still not parsed (SB-P9 ❌)** |
| V-15 | Issue #31 MCP checkpoint `6343122203` (prior session, re-read) | read-only MCP auth verified (`GET /user` 200); 1 CF account created 2026-10-22, 0 zones / 0 Workers; billing refused 403 — **explicitly not zero-cost evidence** |

- **Recorded from owner instruction (not agent-verified):** both provider
  accounts now exist (Cloudflare corroborated by V-15; Supabase existence
  from the owner's statement only); **Q1-D = owner's preferred structure
  subject to eligibility verification** (preference only, `78fece3`).
- **Not executed / not claimed:** console sessions; billing/payment-method
  inspection; subprocessor PDF parse; TIA/Trust Center access; official GDPR
  retrieval (X-6); MoJ translation (X-7); in-flow region selection; any
  provisioning, deployment, account change, OAuth change; **C4 closure** (§D
  steps 1–9 all unchecked); any Stage 7 work.

**Gates unchanged: C4 OPEN; 6.0f UNCHECKED; 6.1–6.18 unchecked; Q1/D5b
UNRESOLVED (Q1-D preference only, subject to eligibility verification); C2 §12
OPEN; 6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing-only; 0 CZK/month; Stage 7
untouched; Stage 5 remains COMPLETE.**

## C6-PG concurrency hardening — executed verification record (2026-10-22)

- **Executor (executed):** `node tools/execute-hardening.mjs` with
  `C6_PG_URL=postgres://c6spike@127.0.0.1:54329/c6spike` on branch
  `spike/goldberg-0.11-stage-6-contract-pg` (code `d8e82d5`, evidence
  `c21e550`), Node v22.23.1, PostgreSQL 17.11 (podman `c6-pg`, rootless,
  loopback-only, `read committed`, removed after capture). Exit 0.
- **PostgreSQL hardening venue:** v1–v11 → **123 tests, 123 pass, 0 fail,
  0 skipped**. Raw TAP:
  [tap-postgres-hardening.txt](https://github.com/stokonn/Goldberg/blob/c21e550ceb5ec7a42317a4f2fe02e6d1225a2cab/spikes/contract-c6/results/pg-hardening/raw/tap-postgres-hardening.txt).
- **SQLite regression venue (faithful server only):** **93 tests, 79 pass,
  0 fail, 14 skipped** (v10 + v11 are PostgreSQL-only — honest skips; v1–v9
  unchanged). Raw TAP:
  [tap-sqlite-regression.txt](https://github.com/stokonn/Goldberg/blob/c21e550ceb5ec7a42317a4f2fe02e6d1225a2cab/spikes/contract-c6/results/pg-hardening/raw/tap-sqlite-regression.txt).
- **v11 hardening suite (PG-only, 37 tests, 7/7 pass):** in-process races
  (both strategies × new/pre-existing entity) `[201,409]` with exact
  one-decision counts and coherent chain; **repeated 20-trial multi-process
  barrier race: 20/20 `[201,409]` per strategy** (for-update telemetry all
  zero; serializable 21× `40001`, 0 exhausted); contract/negative behavior
  (401/403/400/409/503, kill switch, fault-injection rollback, concurrent
  same-key `[201,409]`); **deterministic F3 store race** (23505 → stored
  replay, exactly 1 row, 422 negative); **F4 scale run** (20 concurrent
  decisions: all 201, audit seqs strictly consecutive, chain coherent;
  for-update 0 retries, serializable 218× `40001` with maxAttempts=64).
- **Static isolation scan:** 23 files, 0 forbidden findings (`child_process`
  confined to the race tests + child tools; only socket use: `pg` driver →
  loopback). Original `results/pg/` evidence verified untouched
  (`git status`: only new files added).
- **Not executed / not claimed:** any Supabase-hosted (PostgREST) run —
  blocked by 6.0f + D5b/Q1 + C4 + ORD-A steps 4–6; production implementation
  (6.1–6.18); any device/emulator work; any merge/deploy/release.
- **Cleanup:** container `c6-pg` stopped and removed after evidence capture.

**Gates unchanged: 6.0f UNCHECKED; 6.1–6.18 unchecked; Q1/D5b UNRESOLVED
(Q1-D preference only); C4 OPEN; C2 §12 OPEN; ORD-A sequencing-only;
0 CZK/month; Stage 7 untouched; Stage 5 remains COMPLETE.**

## Owner-attributed C4 console observations recorded (2026-10-22, NOT agent-executed)

- **Recorded from owner instruction (manual, first-hand, never agent-verified):**
  (a) Cloudflare Workers Free **active with no payment method attached**;
  (b) a Supabase **"Personal Free" organization exists with zero projects**
  (no project created); (c) **Frankfurt (`eu-central-1`) is selectable in the
  Supabase project-creation form**. Recorded as §F.5 (N-11–N-13) of
  `stage-6/C4-PREPROVISIONING-CHECKLIST.md`.
- **Not executed / not claimed:** any agent console session, billing or
  payment-method inspection (MCP has no billing scope — inaccessible billing
  is explicitly not zero-cost evidence), project creation, resource
  modification; any screenshot/console export stored in GitHub (none —
  privacy rule); any §D step closure. Supabase billing/payment status
  remains unverified; SB-P7/§D step 6 remains ⬜ (in-flow selectability
  partially informed, not closed).

**Gates unchanged: C4 OPEN (§D steps 1–9 unchecked); 6.0f UNCHECKED;
6.1–6.18 unchecked; Q1/D5b UNRESOLVED and blocking; C2 §12 OPEN; 6.0d/6.0e
NOT AUTHORIZED; ORD-A sequencing-only; 0 CZK/month; Stage 7 untouched;
Stage 5 remains COMPLETE.**
