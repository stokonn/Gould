## Stage 7B preserved checkpoint and CI continuation — 2026-10-23

**Stage7B IN PROGRESS; acceptance incomplete.** Source/evidence checkpoint
`0703cd8ec40a86e6f92e7fd9cf172c5921e1e90e` committed and ordinary-pushed; exact
remote SHA readback PASS. Initial cee9330 +26 modified tracked /125 publishable new
files preserved; private PMP excluded. Fresh JVM **275/0/0/0**,36 suites,12s;
assembly/lint gate SUCCESS (mostly cached), six host scripts and syntax checks PASS.
No app-source/contract/private PMP/prior-evidence bytes changed during recovery.

[API36 CI run40011091868](https://github.com/stokonn/Goldberg/actions/runs/40011091868)
FAILED before build/tests at this exact SHA: emulator executable absent during
pre-build diagnostics (exit127). Zero test bodies; no application failure. Narrow
verified fix explicitly installs emulator before diagnostics. Corrected commit
`3e04e2b716435684a0d4d88a71a4a59407201ada` pushed with exact remote SHA readback;
[run40011450671](https://github.com/stokonn/Goldberg/actions/runs/40011450671)
FAILED before build/tests: installed emulator lacks libpulse.so.0 (exit127).
Second narrow CI fix installs libpulse0 before diagnostics. Commit
`5d3f0ccd03411666ff741ccc894bc85ad98cd280` pushed and remotely verified;
[run40011713646](https://github.com/stokonn/Goldberg/actions/runs/40011713646)
passed dependency/KVM diagnostics and is building. Instrumentation execution pending. Issue31 dashboard updated (connector403; authenticated gh succeeded),
umbrella OPEN / Project In Progress; final chronological checkpoint awaits results.
See [SAFE-RECOVERY.md](SAFE-RECOVERY.md) for scope/tasks/audit/commands/artifact identity.

Stage7A finally owner-accepted; X10 NOT FIXED/NOT VERIFIED, owner-deferred Gould0.12.
Stage8 material readability/accessibility/touch targets remain required. Existing
historical summaries below are superseded for current status/authorization only.
Production networking/decisions DISABLED; G1–G9/G11 integration and Admin visual/
accessibility/WorkManager/notification/upgrade/owner acceptance remain pending.

Native test-only isolation fix found by inspection: return the wrapper itself from
applicationContext so stores and tamper assertions use the same disposable prefs.
All assertions retained; compilation SUCCESS17s. Corrected runtime still pending.
Original test preserved in0703cd8; application/contracts/PMP/prior evidence unchanged.

---

# Goldberg 0.11 — Stage 7B Android Admin — IMPLEMENTATION CHECKLIST

**Branch:** `feature/goldberg-0.11-stage-7b` (based on Stage 7A `576f0d2`) ·
**Started:** 2026-10-23 · **Owner authorization:** local Stage 7B Android implementation
authorized subject to all backend/security/cloud/release gates. NOT authorization for
production backend deployment, merge, release, or physical-device testing.

**Separation of duties (2026-10-23):** another agent handles Stage 6 backend. This
branch/worktree owns **Stage 7B Android** and the **Stage 7A X10** status tracking only (no new X10 investigation authorized in the Codex takeover).
Do not overwrite, merge or reset the other agent's work. Verify actual API contracts
before integration; never rely on or alter another session's uncommitted Stage 6 changes.

**Non-negotiable:** Administration Dashboard fits ONE normal supported screen with NO
vertical/horizontal scrolling. Accessible scroll fallback permitted only at extreme
font scaling; never clip controls or shrink text to unreadable sizes.

## Current Codex continuation (2026-10-23)

7B.4 queue and Android-side 7B.5–7B.7 implementation are in place; host verification
and acceptance preparation are recorded below. **No final stage acceptance** while
screenshots/instrumentation/live contracts remain blocked. Production moderation
writes are disabled pending the G6 dual-revision contract freeze. See
[ACCEPTANCE.md](ACCEPTANCE.md) and the latest Codex checkpoint at the end of this file.
Stage 6 coordination: [Issue #31 comment 6362661598](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6362661598).

## Historical OpenCode checkpoint (2026-10-23) — 7B.1 ✅ · 7B.2 ✅ · 7B.3 🟧 (emulator BLOCKED) · 7B.4–7B.8 then pending

- **Branch/commit:** `feature/goldberg-0.11-stage-7b`, based on Stage 7A `576f0d2`;
  commits `49680da` (7B.1+7B.2) and this 7B.3 checkpoint (see `git log`).
- **Done:** 7B.1 contract audit (G1/G2/G3 + owner decisions); 7B.2 foundation
  (`admin/` package: models, contract client + transport seam, Keystore session,
  trust policy, markers, repository) — **204 unit tests 0 failures, lint PASS,
  5 check scripts PASS, EN/CZ 993 keys PASS, debug APK
  SHA-256 `7fef49b1900643f4e4ec68ddd405ac493ca51a60a351c3162c83794fa2d3f19c`**;
  7B.3 dashboard + Profile entry + stats derivation + destination routes (implemented,
  JVM-verified).
- **Blockers:** (1) **Host emulator is unusable — qemu SIGSEGV on every launch**
  (3 crashes today, systemd-coredump SIGSEGV, matches upstream Google #566807521 from
  Stage 5) ⇒ scroll-free screenshot gate, instrumentation execution and all visual
  verification are **BLOCKED, not passed**; (2) no deployed Stage 6 backend anywhere ⇒
  every admin screen correctly shows honest not-configured/unavailable states;
  live-data verification awaits Stage 6 (6.0d/6.0e, not authorized).
- **X10 (Stage 7A text alignment): UNCHANGED — still OPEN**, real-device-only,
  not reproduced, no code shipped, no claim of resolution (see cross-cutting section).
- **Exact next steps for a new agent:** (a) retry emulator when the host upstream
  SIGSEGV is worked around (e.g. CI workflow like `.github/workflows/stage5-emulator.yml`,
  a different host/AVD, or an emulator version fix) and execute the 7B.3 scroll-free
  screenshot gate; (b) continue 7B.4 Moderation Queue (search per owner decision,
  detail hydration, update pill, scroll/filter restoration); then 7B.5 detail +
  decisions, 7B.6 conflict, 7B.7 audit/notifications/sync, 7B.8 verification.
  Keep Issue #31 updated at every milestone; no release/deployment without approval.

## Stage 7B.1 — Contract and readiness audit — ✅ COMPLETE

- [x] Inspect Stage 6 normative contract, C6 spike, C2 BFF spike, cloud reality.
- [x] Produce contract matrix + recorded gaps G1/G2/G3 (no invented endpoints).
- [x] Obtain owner decisions for the two open forks (stats derivation, search bounds).
- Evidence: [CONTRACT-AUDIT.md](CONTRACT-AUDIT.md). Owner answers 2026-10-23:
  exact-or-unavailable stats (never partial-as-total, `/v1/admin/stats` = durable fix);
  ID search via verified detail endpoint, deep search only if fully exhaustible within
  safe bounds, never silently omit matches, server-side search = Stage 6 dependency.

## Stage 7B.2 — Security/data foundation

- [x] `admin/AdminModels.kt` — contract-shaped data classes + org.json codecs (queue
      item, submission detail, review event, audit event, decision request/result).
      Unknown fields tolerated; no invented fields; payload bodies passed through raw.
- [x] `admin/AdminApi.kt` — single contract-shaped client (capabilities, queue, detail,
      decide, audit); base URL empty by default → honest `NotConfigured` without any
      network call; https-only/default-port/no-userinfo; bounded bodies (512 KiB);
      no redirects; injectable `AdminTransport` seam (mirrors `bundleConnectOverride`)
      + production `HttpsAdminTransport`.
- [x] Error taxonomy mapping (400/401/403/404/409/422/429/503) → typed `AdminFailure`
      results (`NotConfigured`, `InvalidEndpoint`, `Unreachable`, `Validation`,
      `Unauthorized`, `Forbidden`, `NotFound`, `Conflict`, `RateLimited`,
      `ServerError`, `InvalidPayload`); no partial success with hidden errors.
- [x] `admin/AdminSessionStore.kt` — interface + `KeystoreAdminSessionStore` (AES-GCM
      via AndroidKeyStore, ciphertext in private prefs; backup already fully excluded
      by `allowBackup=false`; Keystore failure degrades to clear, never plaintext) +
      in-memory fake for tests.
- [x] `admin/AdminTrustPolicy.kt` — 10-minute NON-sliding trust window (fixed at fresh
      grant, proven never to extend without re-grant); invalidated on logout/
      revocation; in-memory → every process restart begins untrusted.
- [x] `admin/AdminMarkers.kt` — account-scoped DEFERRED + RE_REVIEW markers (ids only,
      non-sensitive); persist until confirmed non-pending server status or manual
      removal; cleared per-account only; never bled across accounts.
- [x] Role gate wired: `GET /v1/me/capabilities` client verified (tested); Profile →
      Administration entry + dashboard role chip wired (server always enforces
      regardless; entry visibility rule documented in `AdminViewModel.entryVisible`).
- [x] Strings `stage7b_*` in both `values/strings.xml` and `values-cs/strings.xml`
      (993 keys, EN/CZ parity + placeholder check PASS via `scripts/check_localization.py`).
- [x] JVM tests for codecs, error mapping, trust window, markers —
      **21 `AdminFoundationTest` cases green; full suite 195/195 green**
      (`./gradlew testDebugUnitTest`, 2026-10-23).

## Stage 7B.3 — Dashboard (scroll-free hard gate) — 🟧 IMPLEMENTED, EMULATOR GATE BLOCKED

- [x] `ui/admin/AdminDashboardScreen.kt` — exactly 4 stat tiles (Pending, Needs info,
      Approved today, Rejected today) + 3 destinations (Moderation Queue, Audit Log,
      Admin Settings). Resume #ID = optional 2nd line INSIDE Moderation Queue row only
      (deterministic account-scoped marker; empty while no markers exist).
- [x] Role label (server-derived chip), section label, last-updated, manual refresh,
      no polling; first-load placeholder is "…", never a fabricated count.
- [x] Stats source: owner-approved exact-or-unavailable derivation (`AdminStatsService`,
      gap G1): complete bounded walks only (≤8 queue pages + ≤8 audit pages per manual
      refresh); incomplete walk, upstream failure or missing config ⇒ explicit
      per-tile unavailable state with reason text; `GET /v1/admin/stats` remains the
      durable Stage 6 fix.
- [x] Role-gated Profile → Administration entry (`entryVisible` rule: hidden only after
      a verified no-surface answer or explicit 403; convenience only — server enforces);
      no extra bottom tab (`selectedTab` maps admin routes to Profile).
- [x] Destination routes registered (`admin`, `admin/queue?filter=`, `admin/audit`,
      `admin/settings`) with real first-pass server-backed Queue/Audit/Settings
      screens so deep links resolve end-to-end; their full stage requirements
      continue in 7B.4/7B.7/7B.8.
- [ ] **Scroll-free on normal supported screens (HARD GATE — NOT VERIFIED):** requires
      emulator screenshots (small + standard sizes, both themes, normal font scale).
      **BLOCKED 2026-10-23:** every emulator launch on this host dies with qemu
      SIGSEGV (see Verification below). Implementation uses a fixed-height layout
      with bottom-anchored footer and no `verticalScroll` at font scale ≤1.3
      (`SCROLL_FALLBACK_FONT_SCALE`), but this is a code-level property only — the
      visual gate is NOT passed until screenshots exist.

### 7B.3 verification (executed 2026-10-23, host only)

| Check | Result |
|---|---|
| `./gradlew testDebugUnitTest` | **PASS — 204 tests, 0 failures, 0 errors** (25 `AdminFoundationTest` + 6 `AdminStatsTest` incl. partial-walk-never-total, not-configured, per-tile failure mapping, contract-only query params, trust-window semantics, marker scoping) |
| `./gradlew lintDebug` | **PASS** (no fatal findings) |
| `./gradlew assembleDebug` | **PASS** — `app/build/outputs/apk/debug/app-debug.apk`, SHA-256 `7fef49b1900643f4e4ec68ddd405ac493ca51a60a351c3162c83794fa2d3f19c` (0 source files newer than APK) |
| `./gradlew assembleDebugAndroidTest` | **PASS — instrumentation COMPILED only, NOT executed** |
| `scripts/check_*.py` (5 scripts) | **PASS** all |
| EN/CZ parity | **PASS — 993 keys**, matching placeholders |
| Emulator screenshots / instrumentation | **BLOCKED — not executed** (host emulator SIGSEGV) |
| Visual comparison vs `docs/design/stage-7b/` mockups | **BLOCKED** (needs screenshots) |

**Emulator blocker evidence (2026-10-23):** bounded recovery performed — adb server
restarted (`kill-server`/`start-server`, daemon healthy, 0 devices), `adb connect
127.0.0.1:5555/5557` → connection refused (qemu dead), startup logs inspected.
Three launch attempts (Medium_Phone_API_37.0 ×2 — one GUI-capable, one headless
`-no-window -gpu swiftshader_indirect`; stage5-api36 ×1 headless swiftshader with
`-no-snapshot`) all died during early boot; systemd-coredump records **SIGSEGV in
`qemu-system-x86_64`** for PIDs 1187010 (08:42:06), 1256430 (08:43:35) and
1321999 (08:56:40 CEST), plus one on 2026-10-22 23:35 — 39 historical qemu coredumps
on this host. Consistent with the upstream emulator SIGSEGV already recorded in the
Stage 5 STATE.md (Google Issue #566807521). A first-run crash-consent dialog was also
suspected of blocking boot; clearing the crash-report cache did not prevent the
subsequent SIGSEGV. **No AVD data was wiped; no physical device was used.**

## Stage 7B.4 — Moderation Queue — HOST-VERIFIED; RENDERED ACCEPTANCE PENDING

- [x] Compact server-backed rows hydrated via verified detail endpoint; honest ID-only fallback.
- [x] All/Pending/Needs info + personal Re-review/Deferred filters and row labels.
- [x] Debounced ID search; exhaustive bounded title/composer/type scan or explicit
  server-search-required state. No silent partial search matches.
- [x] Cursor paging/dedup/retry, stable server order and repeated-cursor bounds.
- [x] Opportunistic update pill, including revision/status/removal changes;
  failed/partial apply preserves rows and pill; no polling.
- [x] Collapsed Deferred section, account-scoped markers and query/filter/scroll
  preservation on detail navigation. Original incomplete service continued in place.
- [ ] Actual rendered interactions, foreground/account lifecycle and full backend
  integration: BLOCKED/UNEXECUTED. Host tests do not pass visual acceptance.

## Stage 7B.5 — Proposal detail + decisions — FIXTURE-VERIFIED; LIVE CONTRACT BLOCKED

- [x] Read-only proposed fields, supplied revision/request timeline and full nested
  evidence/source/license/note content. Never label a prior proposal as current catalogue.
- [x] Contextual Approve/Reject/Request Changes confirmations; required reject/request
  text; fresh role + revision checks; matching atomic response required for success.
- [x] Same-key/body explicit replay on uncertain outcomes; pending action blocks a new
  decision/proposal. Smart next only after confirmed success (fixture tests only).
- [ ] Actual before/after catalogue comparison: G4 lacks verified current values.
- [ ] Live decisions/session integration: G5/G6 BLOCKED. Production writes explicitly
  disabled; no invented dual-revision mapping/open endpoint.
- [ ] Rendered detail/confirmations and live server acceptance (emulator/backend blocked).

## Stage 7B.6 — Conflict recovery — HOST IMPLEMENTATION; VISUAL/INTEGRATION PENDING

- [x] Entry/foreground/pre-confirm validation; stale revision/status invalidates
  confirmation. Background lock invalidates in-flight verification; no automatic resubmit.
- [x] Prior reviewed proposal retained in memory, all supplied intermediate snapshots,
  missing-history warning and acknowledgement tied to the latest verified revision.
- [x] Stability advisory after three conflicts; account/revocation clearing and
  cancellation/idempotency regressions pass on JVM fixtures.
- [x] Field-level old/new proposal diff, including additions/removals and evidence-only
  changes; old value struck through, current value and updated evidence shown.
- [ ] Rendered diff/catch-up/accessibility and equivalence to mockup 07 require
  emulator verification; not accepted from host compilation alone.
- [ ] Live atomic dual-revision conflict proof: G6 BLOCKED.

## Stage 7B.7 — Audit / notifications / sync — HOST IMPLEMENTATION; RUNTIME PENDING

- [x] Role-scoped immutable audit projection, chronological paging/dedup, fetched-page
  search/My actions/Moderation filters and today-specific dashboard shortcuts.
- [x] Separate notification/background opt-ins OFF; approximate 3h WorkManager;
  unmetered Wi-Fi default, explicit mobile/metered confirmation and platform permission.
- [x] Complete bounded queue digest only; no notification from failed/partial walks,
  generic private notification, role checks around scans, scope/account/logout guards.
- [x] No privileged APK credentials, no persisted sensitive review drafts, no offline
  decision queue; role trust uses monotonic fixed 10-minute windows.
- [ ] WorkManager/notification/permission/network/reboot execution and live audit
  integration are UNEXECUTED; host policy tests are not runtime acceptance.

## Stage 7B.8 — Verification and owner acceptance

- [x] Build + JVM tests + lint + `scripts/check_*.py` — final Codex checkpoint below.
- [ ] Emulator screenshots of Dashboard/Queue/Detail/Confirmations/Re-review/Audit/
      Settings vs mockups; accessibility, insets, dynamic type, dark/light, targets.
- [ ] Regression: Profile, Repertoire, Practice, existing data + upgrade unaffected.
- [ ] Dashboard scroll-free gate re-verified.
- [ ] Owner acceptance (separate gate; not claimed by implementation).

---

## Cross-cutting: Stage 7A X10 (text alignment, real-device) — 🚧 INVESTIGATED, BLOCKED ON VERIFICATION

- [x] Locate original X10 report (Stage 7A CHECKPOINT 7A.16/7A.17 + UI-ACCEPTANCE-MATRIX
      X10 + Issue #31). **Reported symptom:** inconsistent text alignment on the owner's
      **physical Samsung device** after round 3.
- [x] Attempt reproduction. **RESULT: cannot reproduce on emulator** — documented, not a
      shortcut: round-3 checkpoint `d7c0aef` and acceptance entry 7A.17 both record that
      emulator renders cannot reproduce/close this real-device difference; it appears
      only under Samsung font metrics. Agent is also forbidden from the owner's phone.
- [ ] **BLOCKED:** the mandated "verify fix with emulator screenshots" step is not
      achievable for a real-device-only defect on any permitted device. Making an
      unverified speculative change to shared global typography would risk regressing
      every screen and could not be honestly claimed as a fix.
- Candidate root cause to CONFIRM with the owner (not yet changed): Samsung One UI
  substitutes its own system sans/serif and applies a per-app "Font size / Display"
  scale that Compose maps partly to fontPadding and line-height; the round-3
  `includeFontPadding=false` fix assumed AOSP line metrics. If the residual X10 is
  line-height vs Samsung-font ascent/descent mismatch, the smallest safe fix is a
  per-style `lineHeight`/`baseline` trim — but this REQUIRES a real-device render to
  confirm and cannot be validated on emulator.
- **X10 stays OPEN.** Two unblock paths for the owner: perform the Samsung capture/check themselves and report the result. Agents must
  not operate the physical device; any targeted fix requires its own authorization. No change shipped; no claim of resolution.

---

created by stokonn with love <3

## Codex takeover checkpoint — 2026-10-23 (7B.4 host milestone)

Recovered `cee9330` on `feature/goldberg-0.11-stage-7b`; only pre-existing
uncommitted file was `AdminQueueService.kt`. Its original bytes are retained at
`/tmp/goldberg-7b-AdminQueueService-opencode.kt`. No Stage 6 tree changed.

- [x] Wire retained queue service, debounced search, all five filters, collapsed
  Deferred section, scroll/filter/query context and manual update action.
- [x] Remove obsolete FULL_DEPTH documentation; every walk is bounded (8 pages,
  20 display hydrations, 8 unknown-marker hydrations). Larger queue/search limitation
  is visible; no extra server parameters.
- [x] Fix duplicate IDs within/across pages, update baselines (unloaded rows are not
  new), revision/status/removal detection, repeated cursors and failed/partial
  refresh preserving existing rows + pending pill. Explicit ID lookup via detail.
- [x] `:app:compileDebugKotlin :app:testDebugUnitTest --no-daemon --max-workers=1`
  PASS (34 s); all 12 new AdminQueueTest cases PASS. Existing baseline also PASS.
  Gradle requires sandbox escalation for local socket creation; initial sandboxed
  attempt failed before compilation (wildcard IP), not an application failure.
- [ ] Queue rendered UI, lifecycle/account revocation stress and live backend checks
  still pending; emulator blocker unchanged. Stage 7B.4 not finally accepted.

Next: implement/test detail/decisions and conflict recovery, then audit/consent-safe
sync and acceptance preparation. No production deploy, physical device, merge or
release. Stage 7A X10 stays OPEN.

## Codex verified milestone and handoff — 2026-10-23

**Branch:** `feature/goldberg-0.11-stage-7b`; **HEAD:** `cee9330f82cf2e57dcdc66a63d94b7a4e3749888` +
uncommitted continuation. No commit/push, merge, release or deployment. Stage 6
working trees untouched; only immutable committed objects inspected. Original
OpenCode untracked service preserved at
`/home/stokonn/.cache/goldberg-stage7b/AdminQueueService-opencode-2026-10-23.kt`,
SHA-256 `83b2ac4ee50cf1d6ce917ea54103a5e6bf0e89f2603875fbc4bee3877ce5f2d0`.

**Implementation:** queue service recovered/wired and corrected; proposal fields,
evidence and history; explicit moderation confirmations and same-key uncertain
replay; role/revision/background guards; guided field-level re-review including
source-only changes; chronological audit; opt-in consent-safe WorkManager/local
notifications. Admin dark sans surface is scoped to Admin, with 4 tiles + 3
Dashboard destinations and no normal-scale scroll implementation. Personal Room
schemas, backup formats, journal and practice code unchanged.

**Executed final source verification:**

- `./gradlew assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1`
  **BUILD SUCCESSFUL, 2m41s**, 83 tasks (31 executed, 52 up-to-date).
- **251 JVM tests / 0 failures / 0 errors / 0 skipped**; 47 new cases:
  Queue 12, Review 26, Audit/Sync 9. Full report XMLs retained.
- Lint **0 fatal/errors, 132 warnings** (plus a state-boxing hint); Admin warnings
  are SharedPreferences KTX suggestions, not suppressed. Prior overlapping lint
  runs produced an empty incremental report; the final explicit report above
  supersedes them. No clean-warning claim.
- Five `scripts/check_*.py` **PASS**: policy, backup fixtures, history paging,
  localization (**1050 EN/CZ resources**) and migrations v1–9→10 DDL.
- `git diff --check` PASS. Source hashes retained; **0 source files newer than APK**.
- APK: `app/build/outputs/apk/debug/app-debug.apk`, **24,140,673 bytes**, SHA-256
  `d23a98b25dc5da8069a04d629d04408bf2135929872ef624c803910d0c03f8c6`. Package `com.opus.piano`, unchanged version **0.10.1/code 11**;
  this is a development artifact, not a published 0.11 release. Signature verified:
  `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.
- Instrumentation **compiled only; NOT executed**. No actual UI screenshots,
  accessibility/TalkBack, WorkManager runtime or seeded upgrade executed here.
  All 11 supplied reference images were inspected; they are not runtime evidence.
- Intermediate compile failures (missing localized Cancel key, recursive type
  inference at principal scope) are preserved in results; fixed before final pass.

**Blockers / not accepted:** 7B.3 screenshot/no-scroll and all rendered UI checks
remain **BLOCKED** by the retained emulator SIGSEGV (no new boot attempts).
7A X10 remains **OPEN**. G1/G2/G3 server queries, G4 catalogue before-values,
G5 live session and **G6 dual entity/submission revision HTTP contract** remain
Stage 6 dependencies. Production decisions are **disabled by default and explicitly
in OpusApplication**; C6 fixture tests are not a backend integration pass. The
current Stage 6 local candidate at `2f1524e` is not a frozen/deployed HTTP API.

**Tracking:** Issue #31 remains OPEN; Goldberg Development Project status read back
**In Progress**. Queue milestone [6362355770](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6362355770);
Stage 6 coordination [6362661598](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6362661598).
Connector writes returned 403; existing authenticated `gh` used successfully.

**Exact next steps:** read CONTRACT-AUDIT and ACCEPTANCE; agree shared Stage 6
session/capability/queue/detail/open/dual-revision/audit fixtures, adapt the single
Android facade, then obtain an allowed emulator venue and execute every unchecked
visual/runtime/upgrade gate. Never enable production decisions by guessing a wire
mapping, operate a physical phone, or edit Stage 6's worktree. Owner acceptance,
merge and release remain separate gates. Preserve all uncommitted continuation.

Evidence: [results/codex-2026-10-23](results/codex-2026-10-23/), including source
manifest, test XMLs, final log, actual lint XML, signing/badging and artifact identity.

Final GitHub checkpoint read back and dashboard synchronized: [6362810877](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6362810877). Issue OPEN, Project In Progress; all historical comments retained.


## Stage 7B contract preparation — recovered verified checkpoint (2026-10-23)

**Current status: local preparation verified; Stage 7B acceptance remains IN PROGRESS / BLOCKED.**
Branch `feature/goldberg-0.11-stage-7b`, HEAD `cee9330f82cf2e57dcdc66a63d94b7a4e3749888`
plus preserved uncommitted continuation. No application code was changed by this
recovery session. Stage 6 trees untouched; no commit/push, merge, release or deploy.
`.pmp/` and `.pmp.yml` remain private/untracked. This entry supersedes older current-status
summaries below; historical checkpoints and earlier counts remain audit history.

**Purpose/scope:** finish the interrupted API contract integration preparation by
recovering source/evidence, retaining supported Android safety changes, and making
contract decisions explicit without inventing or activating a backend mapping.

- [x] Read policy, PMP sequence-3 audit checkpoint, AI handoffs, Stage 7B checklist /
  acceptance, latest Issue #31 comments, branch/status/diffs and existing evidence.
- [x] Recover immutable `bcc7796` audit and `faf9ba3` comparison; 29 synthetic cases
  and 15 pinned hashes. Normative design, legacy C6, local candidate and inactive
  mapping proposal stay separate. No Stage 6 backend tests executed here.
- [x] Retain already implemented application-disabled transport even with a saved
  endpoint/token; independent decision disable; strict capability principal/string
  validation, exact legacy Int revisions and Long audit sequences, cursor type
  rejection, typed malformed-payload errors. No candidate dialect adapter enabled.
- [x] Preserve recovered successful Gradle outputs: 36 suites / **275 JVM tests,
  0 failures/errors/skips**, including **24 AdminContractIntegrationTest cases**.
  `./gradlew testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug
  --no-daemon --max-workers=1` completed in 2m26s; retained follow-up 13s pass.
  These are **prior-session executions inspected now**, not new JVM executions.
  Initial two capability-test failures remain preserved and are superseded by
  the passing reports after their pre-interruption fix.
- [x] Lint recovered: **0 errors/fatal, 132 warnings, 1 hint**. Instrumentation APK
  compiled/assembled only; **0 instrumentation bodies executed**.
- [x] Recovery session executed all six host scripts: contract fixtures/provenance,
  agent policy, backup formats 1–3, history paging, localization (1050 EN/CZ), and
  migrations v1–9→10 DDL; all PASS. `git diff --check` PASS.
- [x] Recovery session verified APK signing, badging, ZIP integrity and exclusion of
  test corpus. APK `app/build/outputs/apk/debug/app-debug.apk`, 24,140,673 bytes,
  SHA-256 `6976d76e9bd936cd5b45e26f1910f86c3105863a8fe2bbf6f1269aaafc7e269b`;
  `com.opus.piano`, development version **0.10.1/code 11**; trusted signer
  `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.
  No application source file newer than APK; test-only corpus files are newer.
  Source manifest reconstructed on recovery, not contemporaneous build/CI proof.
- [x] Recover bounded emulator investigation: read-only `stage5-api36`, explicit
  intended serial `emulator-5582`, no snapshot/wipe; **SIGSEGV / exit -11 after
  21.68s**. This is host setup failure, not application evidence. No new launch,
  ADB operation or physical device use in recovery; no screenshots inspected.
- [ ] Stage 6/owner contract approval and actual Android/backend integration.
- [ ] Functioning permitted emulator; rendered/no-scroll screenshots, accessibility,
  WorkManager/notification runtime, seeded upgrade and owner acceptance.

**Unresolved G1–G6:** G1 server stats; G2 server queue search/display labels;
G3 audit server filtering/search; G4 verified current catalogue before-values;
G5 live credential/session/principal lifecycle; G6 explicit open + two revision
expectations and result/conflict/replay HTTP semantics. All remain OPEN. Existing
bounded exact-or-unavailable/client-side fallbacks do not close server gaps.
Additional G7 public DTO/cursor/HTTP binding, G8 numeric representation, G9 audit
vocabulary/time windows and G11 validation/reason/license policy remain OPEN.
**G10 local Android production networking boundary is closed**; this is no backend
approval. Production networking and moderation decisions remain DISABLED. X10 OPEN.
Personal database/schema/backup/practice data unchanged.

**Next milestone:** obtain explicit approval of versioned shared public
session/capability/queue/detail/open/dual-revision/audit/error/validation fixtures;
then implement one approved adapter and run isolated integration tests. Obtain a
working allowed emulator venue before executing the remaining acceptance matrix.
Do not select a dialect heuristically, default missing expectations, revive
networking from an endpoint setting or retry the known crashing emulator unchanged.

Evidence: `docs/planning/goldberg-0.11/stage-7/stage-7b/results/contract-preparation-2026-10-23/`
(recovered raw logs/XML, failure history, emulator diagnostics, source hashes,
artifact identity and recovery host checks). [Recovery Issue checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6368949572). Final PMP checkpoint is read through
`pmp resume` / `pmp history`; native PMP test-count evidence is not fabricated from
recovered JUnit outputs. Issue #31 remains OPEN; Project remains In Progress.
