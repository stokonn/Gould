# Run 39557624554 — attempt 6 (@ `44645fa`, headSha verified) — **FULLY GREEN**

**Dispatched:** 2026-10-21 (owner-approved after attempt 5's documented diagnosis).
**Completed:** 2026-10-21 08:58 UTC.
**Conclusion: `success` — both jobs PASSED.** The first fully green Stage 5 CI run.
**Branch/commit:** `feature/goldberg-0.11-stage-5` @ `44645fa`
(`44645fa5c18865e192a733d04dbe3107dab7628d`), verified three ways: dispatch headSha,
`acceptance/source-commit.txt`, `instrumentation/source-commit.txt` (all identical).

## 5.10 instrumentation — PASSED (third consecutive green)

- **Tests: 177, Failures: 0, Errors: 0, Skipped: 2** on the API 36 emulator
  (`instrumentation-counts.txt`; full per-test XML `instrumentation-TEST-results.xml`).
- Confirms stability of the run-4 fixes across three independent emulator runs.

## 5.11 acceptance subset — PASSED IN FULL; every section executed for the first time

### (a) Python upgrade verification (unchanged from run 5's green)

- **`offlineVerified: true`** — offline reached at verify-loop attempt 1
  (`connectivity-offline-attempt-01.txt` = `Active default network: none`), held
  **through the entire upgrade** (`connectivity-final.txt` = `none`,
  `offline-mobile-data-setting.txt` = `0`).
- Seeded v0.10.1 → candidate in-place upgrade green: `baselineSchemaVerified: 8`,
  `personalSchema: 10`, `originalRowsUnchanged: true`, `preferencesUnchanged: true`,
  37 sessions, UUID bridge 3 identities / 113 anchors, `foreignKeys: "pass"`.
- Signing green: trusted release certificate `53eface4…141d59e` matched the downloaded
  0.10.1 release APK; baseline = candidate debug certificate `750a7aaf…e5e7c` (this
  runner's keystore); all three `apksigner` exit codes 0.

### (b) Wi-Fi→mobile default-network transition — FIRST EXECUTION, PASSED

The fixed parser (run-5 fix, `44645fa`) read the API 36 format correctly
(`network-evidence.txt`):

- Restore → **`active default network: 101`** (Wi-Fi, `nc{[ Transports: WIFI`).
- `svc wifi disable` → **`active default network: 100`** (cellular fallback).
- **`OK: default network transitioned (active default network: 101 -> active default network: 100)`** —
  the numeric IDs differ, satisfying the change assertion (and identifying the two
  distinct networks explicitly, stronger than the ID-change check alone).
- Wi-Fi restored for the remainder of the run.

### (b) Data Saver — FIRST EXECUTION, PASSED (state readback + dumpsys)

- Enable: `cmd netpolicy get restrict-background` → `Restrict background status:
  enabled`; `dumpsys netpolicy` → `Restrict background: true`
  (`netpolicy-enabled.txt`, 1 344 lines).
- Disable: readback → `disabled`; dumpsys → `Restrict background: false`
  (`netpolicy-disabled.txt`, 1 341 lines).
- **`OK: Data Saver toggled with verified state readback`**

### (c) Process death + cold restart — FIRST EXECUTION, PASSED

- `am force-stop` confirmed dead (`pidof` empty), relaunched, `pidof` non-empty;
  `run-as … ls databases` shows `opus.db`, `opus.db-shm`, `opus.db-wal`
  (`process-death-db.txt`).

### (d) Post-restart database integrity — FIRST EXECUTION, PASSED

- Pulled `db-after-restart/opus.db` (preserved in this bundle):
  **`PRAGMA integrity_check: ok`**, **`PRAGMA foreign_key_check violations: none`**,
  **`PRAGMA user_version: 10`** (`process-death-integrity.txt`).

### Startup measurement — FIRST EXECUTION, PASSED

- Workflow-level cold launches (`startup-measurement.txt`): `LaunchState: COLD`,
  `TotalTime` = 2262 / 2408 / 2587 / 2445 / 2004 ms.
- Python-level matched samples (`result.json`): baseline
  `[4765, 5204, 4774, 4908, 4019] ms` vs candidate
  `[2423, 2436, 1997, 2323, 2049] ms` — candidate consistently ≈2× faster on cold
  process after initialization (script's own caveat recorded: small emulator samples,
  not install/start-to-usable or a device guarantee).

## What this run closed from the run-5 diagnosis

- The section-(b) parser format mismatch (`ni type:` → `nc Transports:`) — proven fixed
  by real execution: the transition assertion **passed on the emulator**, not just in
  local parser checks.
- The swallowed deadline-FATAL hygiene defect — not exercised on a green path; the
  stderr change is in effect (would surface on any future deadline).
- The "never-executed sections" gap: **all of (b) (transition + Data Saver), (c), (d)
  and startup measurement have now executed on a real CI emulator and passed.**

## Evidence

- Curated bundle (this directory, 28 files, 1.2 MB): full-run step log + step-13 log,
  both source-commits, counts + per-test XML, signing set, the complete connectivity
  chain (offline attempt/established/pre-upgrade/final + `connectivity-dump` +
  `network-evidence`), `netpolicy-enabled/disabled`, `process-death-db/integrity`,
  `startup-measurement`, `db-after-restart/opus.db`, `upgrade/result.json` + before/
  after JSON + appearance XML, the fixed parser as run (`assert_default_network.py`),
  disk usage.
- Large logs **retained in GitHub artifacts (30-day retention), not committed**:
  acceptance `logcat.txt` (11 MB, SHA-256
  `1d29f99c4606ff57d85e29635d35cbbead72f1fde9d6b8700ca6e7bbb770cdba`),
  instrumentation `logcat.txt` (9.7 MB, SHA-256
  `c7c47039d0409c1e7abffa2c7db151a798f51394e488f2beb782a2702f630d9b`).
- Run: <https://github.com/stokonn/Goldberg/actions/runs/39557624554>

## Status

**The mandatory CI verification for Stage 5 tasks 5.10/5.11 is now green end-to-end.**
Stage 5 remains **IN PROGRESS** per the owner's standing rule until every mandatory
verification — including the notebook/manual-only items (consent revocation mid-stream,
low disk, screenshots, real bundle-transfer costs) tracked in 5.12/5.13 — passes. No
merge/tag/release/APK publication performed; Stage 6/7 untouched.
