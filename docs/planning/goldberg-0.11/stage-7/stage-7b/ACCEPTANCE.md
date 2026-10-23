## Stage 7B recovered work and native verification — 2026-10-23

Stage7B remains IN PROGRESS; acceptance incomplete. Preservation checkpoint
`0703cd8ec40a86e6f92e7fd9cf172c5921e1e90e` ordinary-pushed/read back.
Current verified CI source `de792fc06f0e8d8ef898d4d2254489783e3310a5`,
[run40013193851](https://github.com/stokonn/Goldberg/actions/runs/40013193851):
full host build/JVM/lint and six scripts PASS; **275 JVM tests,0 failures/errors/skips**;
lint0 errors/132 warnings/1 hint. API36 emulator-5554/qemu1: connected task SUCCESS
3m22s; XML **182 tests,0 failures/errors,2 pre-existing Stage4 external-fixture skips**;
all **4 Stage7bAdminRuntimeTest cases PASS**. XML has182 testcase elements; Gradle's
console prints184, so XML counts remain authoritative. No tests filtered/weakened.

Workflow conclusion FAILURE after tests: UTP removes the installed app; post-suite
smoke launch fails with Activity class missing. Launcher screenshot inspected;
it is not Admin/app UI evidence. Narrow fix restores the same built APK with
explicit emulator-targeted install-r before smoke; syntax and synthetic host
success/fatal-install-failure harness PASS. Corrected smoke runtime pending.

Native context-wrapper isolation fix071ea93 is compiled and now runtime-verified.
Initial source/prior evidence/PMP preserved;1187 protected files unchanged and one
intentional native-test correction (original retained in0703cd8). Personal Room,
backup, practice and production application source unchanged during recovery.
Production networking/decisions DISABLED; G1–G9/G11 integration and Admin visual/
accessibility/WorkManager/notification/seeded-upgrade/owner acceptance remain open.
Stage7A finally owner-accepted; X10 unfixed/unverified and deferred Gould0.12;
Stage8 material readability/accessibility/touch-target checks remain required.
See [SAFE-RECOVERY.md](SAFE-RECOVERY.md) for purpose, scope, tasks, evidence and continuation.
Issue31 OPEN / Goldberg Development In Progress; final checkpoint sync pending.

---

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

---

## Retained acceptance matrix and historical packet

# Stage 7B — verification and owner acceptance preparation

Status: **IN PROGRESS. No owner acceptance, release or merge approval claimed.**
Branch: `feature/goldberg-0.11-stage-7b`, base/HEAD `cee9330`, uncommitted continuation.
Read [CHECKLIST.md](CHECKLIST.md) for current executed test counts and blockers.

## Implemented scope and limits

- Queue: retained service, bounded 8-page cursor walks/20 display hydrations,
  dedup/retry, debounced exhaustive-or-unavailable text search, explicit ID lookup,
  five filters, personal Deferred/Re-review markers, preserved navigation context,
  opportunistic update detection and manual apply. Incomplete scans never claim full
  search/count coverage. No cursor snapshot guarantee exists in legacy C6.
- Review: read-only proposed fields and full nested evidence/license/note details,
  complete supplied revision/event timeline, contextual confirmations, required
  reject/request text, fresh role + revision checks and server-result validation.
  Smart next runs only after confirmed success. Current catalogue before-values
  are **unavailable (G4)**; previous proposal revisions are never mislabeled.
- Conflict: entry/foreground/pre-confirm checks, explicit re-review acknowledgement
  bound to latest revision, field-level old/new and evidence-only diff from the prior
  proposal retained in memory, all supplied history,
  incomplete-history notice, advisory after three conflicts. No automatic resubmit.
- Uncertain outcomes: only explicit replay of identical in-memory body/key. Leaving
  for a different proposal cannot discard that pending result. Process death clears
  sensitive state; durable same-key reconciliation with reason bodies is not claimed.
- Audit: immutable read projection, chronological cursor pages and dedup, My actions /
  Moderation / today dashboard shortcuts, explicit fetched-pages-only search scope.
- Sync: separate opt-ins for local notifications and approximate 3h WorkManager
  checks, both OFF. Unmetered Wi-Fi default; mobile/metered requires a confirmation.
  Per-account/endpoint preferences and work scope; fresh role checks around complete
  bounded scans; digest-only baseline, generic private notification, platform
  notification permission, logout/account-switch cancellation. No push/poll timer.
- Admin-only scoped black/charcoal theme with selected sans family; no global 7A
  typography change. Dashboard remains exactly 4 tiles + 3 destinations, fixed
  layout without scrolling at font scale ≤1.3. This is **not screenshot evidence**.
- **G5/G6:** no approved live admin session integration/frozen dual-revision HTTP
  contract. Production decisions are explicitly disabled in OpusApplication;
  fixture-driven decision behavior is not a live backend integration pass.

## Acceptance matrix

| Gate | Current evidence/status | Required next verification |
|---|---|---|
| Queue paging/dedup/search/update application | JVM regression suite; current count in checklist | Emulator interaction with shared frozen server fixtures |
| Role denial/revocation, self-review, stale confirmation | JVM fixtures | Real integration against agreed Stage 6 adapter, including logout/account lifecycle |
| Atomic server decisions/idempotency | Android same-key/response-safety JVM fixtures only | Stage 6 atomic transaction and HTTP replay proof; freeze G6 first |
| Conflict/re-review | JVM fixtures | Render changed old/new fields and missing/intermediate-history cases |
| Catalogue comparison | BLOCKED G4 | Verified current field values + published/entity revision from server |
| Live sign-in/refresh | BLOCKED G5 | Approved credential exchange, account binding, expiry/refresh/revocation fixtures |
| Audit/consent policy | JVM fixtures | WorkManager execution, metered transition, permission denial, account switch/reboot |
| Dashboard no-scroll | BLOCKED emulator SIGSEGV | Small and standard supported displays, both app themes, EN/CZ, normal font scales with system bars/bottom navigation |
| Other rendered admin screens | BLOCKED emulator SIGSEGV | Queue, Details, three Confirmations, Re-review, Audit, Settings vs all nine references |
| Accessibility | UNEXECUTED | 48dp targets, TalkBack order/labels, keyboard/insets, larger fonts 1.5/2.0, reduced motion |
| Existing app regression | JVM/host checks only in this continuation | Profile/Repertoire/Practice navigation and seeded in-place upgrade; preserve personal data |
| Stage 7A X10 | OPEN, unchanged | Owner-performed Samsung check under the existing separate gate |
| Owner visual/functional acceptance | PENDING | Review actual screenshots and executed matrix; explicit owner acceptance |

## Reproducible host commands

```sh
./gradlew assembleDebug testDebugUnitTest assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1
python3 scripts/check_agent_policy.py
python3 scripts/check_backup_fixtures.py
python3 scripts/check_history_paging.py
python3 scripts/check_localization.py
python3 scripts/check_migrations.py
git diff --check
```

Gradle requires approved local socket access on this host; a sandboxed wildcard-IP
startup failure is not an application test failure. Instrumentation compilation is
never instrumentation execution. No fresh emulator attempts were made during this
continuation; retained SIGSEGV evidence is in CHECKLIST.md. Original images and nine
individual mockups were inspected; **zero new actual UI screenshots** were captured.

## Owner review packet and exact next actions

1. Stage 6: answer G1–G6 in CONTRACT-AUDIT.md; freeze shared wire fixtures without
   deploying or changing existing Stage 6 gates. Coordinate only through Issue #31.
2. Adapt the single Android facade to the frozen session/capability/queue/detail/
   open/dual-revision/audit contract and test it before enabling any live decisions.
   Do not merge another session's uncommitted tree or guess a wire mapping.
3. Obtain an allowed working emulator venue. Identify its `emulator-NNNN` serial,
   verify `ro.kernel.qemu=1`, and explicitly target that serial for every ADB call.
   Run instrumented tests and capture source-bound screenshots with size, density,
   font scale, locale, app theme and SHA-256 manifest. Never use a physical phone.
4. Execute all unchecked rows above. Preserve initial failures and blockers. Compare
   actual renders with `docs/design/stage-7b/original-references/` and the nine PNGs.
   The dashboard must fit without clipping, unreadable text or normal-scale scroll.
5. Update Issue #31 description + append-only checkpoint, CHECKLIST, root
   STAGE-CHECKLIST and docs/ai with exact source/artifact identity and results.
   Ask for owner acceptance of this concrete evidence only after it exists.

No production backend deployment, real-data collection, main merge, release or
physical-device operation is authorized by this packet.
