# Goldberg 0.11 — Stage 7B Android Admin — IMPLEMENTATION CHECKLIST

**Branch:** `feature/goldberg-0.11-stage-7b` (based on Stage 7A `576f0d2`) ·
**Started:** 2026-10-23 · **Owner authorization:** local Stage 7B Android implementation
authorized subject to all backend/security/cloud/release gates. NOT authorization for
production backend deployment, merge, release, or physical-device testing.

**Separation of duties (2026-10-23):** another agent handles Stage 6 backend. This
branch/worktree owns **Stage 7B Android** and the **Stage 7A X10** investigation only.
Do not overwrite, merge or reset the other agent's work. Verify actual API contracts
before integration; assume no uncommitted Stage 6 changes exist.

**Non-negotiable:** Administration Dashboard fits ONE normal supported screen with NO
vertical/horizontal scrolling. Accessible scroll fallback permitted only at extreme
font scaling; never clip controls or shrink text to unreadable sizes.

## Session status checkpoint (2026-10-23) — 7B.1 ✅ · 7B.2 ✅ · 7B.3 🟧 (emulator BLOCKED) · 7B.4–7B.8 pending

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

## Stage 7B.4 — Moderation Queue

- [ ] Compact rows (title, composer, ID/revision, status, chevron); server-backed list.
- [ ] Filters All/Pending/Needs info + Re-review + Deferred (personal overlays).
- [ ] Search: ID via verified detail endpoint; deep (title/composer/type) only if fully
      exhaustible within safe bounds, else explicit "requires server search" state.
- [ ] Cursor pagination (~20 provisional), stable sort, dedup, contextual empty/error.
- [ ] Floating update pill after opportunistic change detection (no polling).
- [ ] Collapsed Deferred section; safe context restoration (scroll/filter/search).

## Stage 7B.5 — Proposal detail + decisions

- [ ] Read-only before/after Smart Comparison; full evidence/sources; revision +
      information-request timeline; always latest verified server revision.
- [ ] Approve / Reject (reason required) / Request Changes (→ `changes_requested`)
      via explicit contextual confirmation; no bulk approve; Smart Auto-Next only
      after confirmed server success.
- [ ] Never treat local confirmation as success; require atomic server response.

## Stage 7B.6 — Conflict recovery

- [ ] Validate on entry/resume/pre-confirmation; expected revision/state + atomic
      server checks; inline validation blocks decisions until success.
- [ ] Highlighted old/new diff, guided re-review, catch-up across intermediate
      revisions when available; never auto-resubmit stale decisions.
- [ ] Non-blocking stability advisory after ~3 confirmed conflicts (provisional).

## Stage 7B.7 — Audit / notifications / sync

- [ ] Role-scoped audit log (immutable from client), chronological, search/filter over
      fetched pages, pagination, integrated timeline.
- [ ] Optional in-app/local notifications (opt-in). Optional ~3h WorkManager check
      OFF by default, Wi-Fi-only default, mobile data only with explicit consent; no
      push, no continuous polling.
- [ ] No privileged credentials in APK; no sensitive persistent review drafts; no
      unverified offline moderation decisions.

## Stage 7B.8 — Verification and owner acceptance

- [ ] Build + JVM tests + lint + `scripts/check_*.py`.
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
- **X10 stays OPEN.** Two unblock paths for the owner: (a) authorize an agent-assisted
  capture on the Samsung device, or (b) perform the check themselves after a targeted
  candidate fix. No change shipped; no claim of resolution.

---

created by stokonn with love <3
