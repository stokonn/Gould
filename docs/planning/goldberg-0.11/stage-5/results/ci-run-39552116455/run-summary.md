# Run 39552116455 — attempt 5 (@ `6da1021`, headSha verified)

**Dispatched:** 2026-10-21 (owner-approved "fix + verify + dispatch attempt 5" in the
recovery session). **Completed:** 2026-10-21 08:05 UTC.
**Conclusion:** `failure` — 5.10 **SUCCESS**, 5.11 failed at the never-executed
section (b) host-side parser (see root cause below).
**Branch/commit:** `feature/goldberg-0.11-stage-5` @ `6da1021`
(`6da1021c3af8b9071fe113d08fa6809abec37efc`), verified three ways: dispatch headSha,
`acceptance/source-commit.txt` (162 B, both jobs identical format), instrumentation
source-commit.

## 5.10 instrumentation — EXECUTED AND PASSED (second consecutive green)

- **Tests: 177, Failures: 0, Errors: 0, Skipped: 2** on the API 36 emulator
  (`instrumentation-counts.txt`; full per-test XML:
  `instrumentation-TEST-results.xml`, SHA-256
  `eee30ad8…98a141`).
- Confirms the run-4 result was stable: every Stage 1–4 suite + the fixed Stage 5
  bundle suites pass on-device.

## 5.11 acceptance subset — python upgrade validation PASSED IN FULL; failure in the
host-side Wi-Fi→mobile transition parser

### What PASSED (first complete 5.11 python verification ever)

`upgrade/result.json` (script exited past its final assertion — all sections wrote):

- **`offlineVerified: true` — the attempt-5 offline fix WORKED**: offline reached at
  verify-loop **attempt 1** (`connectivity-offline-attempt-01.txt`: `Active default
  network: none`; `settings put global mobile_data 0` applied before telephony came
  up), held **through the entire upgrade** (`connectivity-final.txt`: `Active default
  network: none`), with `offline-mobile-data-setting.txt` = `0`. No airplane-mode
  escalation needed on this run.
- Seeded v0.10.1 → candidate in-place upgrade: `baselineSchemaVerified: 8`,
  `personalSchema: 10`, **`originalRowsUnchanged: true`, `preferencesUnchanged:
  true`**, 37 sessions, notes/profile/badges/repertoire/recovery archive/draft bytes
  retained, `foreignKeys: "pass"`, UUID bridge 3 identities / 113 anchors.
- Step-13 log line: `PASS: exact persistent-table equality after 0.10.1 upgrade; 37
  sessions, notes, profile, badges, repertoire, recovery archive, draft bytes and
  preferences retained` (06:03:19.749Z).
- **Startup measurement EXECUTED for the first time** (`--startup-runs`): baseline
  `[4441, 4864, 5140, 4599, 3578] ms` vs candidate `[2470, 2080, 2630, 2363, 2236] ms`
  (cold process after initialization; script's own caveat: small emulator samples, not
  install/start-to-usable or a device guarantee).
- Signing evidence passed again (trusted release cert `53eface4…` compatible;
  baseline = candidate = runner debug key `47aebcfd…`; `apksigner-exit-codes.txt`).

### Root cause of the 5.11 failure — PROVEN from artifacts (not inferred)

Section (b) (Wi-Fi→mobile default-network transition — **first real execution ever**;
all earlier attempts died at/before the python assertion) failed at its very first
`wait_for_transport WIFI`:

- The host-side parser `assert_default_network.py` (workflow heredoc) extracts the
  transport from the NAI block's `ni{...}` field via `type: (\w+)` — a legacy
  dumpsys format. **The API 36 emulator prints no `type:` token there**: the real
  format (preserved in `connectivity-dump.txt`) is
  `NetworkAgentInfo{network{101} … ni{WIFI CONNECTED extra: } … nc{[ Transports:
  WIFI Capabilities: … ]}` — the transport lives only in the `nc{[ Transports: …]}`
  capabilities section.
- **The emulator itself behaved correctly.** The 14-iteration loop evidence
  (`network-evidence.txt`) shows iteration 1 saw `Active default network: 'none'`
  (Wi-Fi still reconnecting — correct retry behavior) and iterations 2–14 all saw a
  numeric active default whose NAI block contained `WIFI CONNECTED extra: `; the
  last loop dump (`connectivity-dump.txt`) shows `Active default network: 101` with
  `nc{[ Transports: WIFI … VALIDATED …]}` on wlan0. **Wi-Fi reconnected within ~4–8 s
  and became the validated active default network; only the parser could not read
  the transport token.**
- The script then died at exactly the 60 s deadline (step-13 log:
  `##[error]The process '/usr/bin/sh' failed with exit code 1` at 06:04:21.7Z, 60 s
  after `== expect Wi-Fi as active default network ==` at 06:03:19.9Z). Exit path:
  `wait_for_transport` returned 1 → `set -e` on the `WIFI_NET="$(…)"` assignment.
- **Not an application defect** (the app was not even running during this section);
  **not a data issue** (the full upgrade validation had already passed); a host-side
  workflow parser/format mismatch that could only surface now, because section (b)
  had never executed on a real emulator before.
- Secondary evidence-hygiene defect found in the same triage: the deadline message
  `FATAL: active default network never reached transport WIFI within 60s` is echoed
  to the function's stdout, which is swallowed by the `WIFI_NET="$(…)"` command
  substitution — it appears in **neither** the step log nor the evidence file. To be
  fixed alongside the parser (echo to stderr).

### Sections that still never executed

(c) process-death + cold restart, (d) post-restart DB integrity — the script died in
(b) before them. (b)'s Data Saver toggles and the Wi-Fi→mobile *transition assertion*
also remain unexecuted (only the Wi-Fi-restore half of (b) ran).

## Designed fix (documented here BEFORE implementation; same gate discipline)

1. `assert_default_network.py`: read the transport from `nc{\[\s*Transports:\s*(\w+)`
   (canonical NetworkCapabilities enumeration, present on API 36), mapping the
   section's expected names `WIFI→WIFI`, `MOBILE→CELLULAR`; keep the legacy
   `ni{…} type:` path as a fallback for older formats. **Assertion semantics
   unchanged**: active default network must be numeric AND its NAI must declare the
   expected transport. No check removed, no retry shortened.
2. `wait_for_transport`: echo the deadline FATAL to `>&2` so it escapes the command
   substitution and reaches the step log.
3. Verification plan (all local, host-emulator blocker unchanged): YAML parse +
   `bash -n` on both materialized scripts; run the **fixed parser against the REAL
   run-5 dumps** (`connectivity-dump.txt` → WIFI expectation must now pass; a
   cellular-active variant → MOBILE/CELLULAR must pass and WIFI must still fail;
   a synthetic legacy-format block → fallback path must pass).

## Evidence

- Curated bundle (this directory, 21 files, 720 KB): step logs (whole run + step 13),
  both source-commits, counts + TEST XML, signing set, the complete connectivity
  evidence chain (`connectivity-offline-attempt-01` → `-established` → `-pre-upgrade`
  → `-final`, `offline-mobile-data-setting`, `connectivity-dump`, `network-evidence`),
  `upgrade/result.json` + before/after JSON + appearance XML, the failing parser as
  run (`assert_default_network.py`), disk usage.
- Large logs **retained in GitHub artifacts (30-day retention), not committed**:
  acceptance `logcat.txt` (11 MB, SHA-256 `78f41131dbacbc5ccf40e8d0c165f3e15276f8458
  64cb71e33471ceda10f2758`), instrumentation `logcat.txt` (14 MB, SHA-256
  `0edfc483a024940940791ac060b5957ccc27659957c3156bccbb4b69abc4ce5f`).
- Run: <https://github.com/stokonn/Goldberg/actions/runs/39552116455>

## Status

Stage 5 IN PROGRESS. No assertion weakened; no app code changed by the fix
(workflow-only). A sixth dispatch requires a locally verified fix + owner approval
(per the standing diagnose-and-document-first rule).
