## Stage 7B coordinated acceptance continuation — 2026-10-23

**Current: IN PROGRESS; owner acceptance pending.** Scope: approved remaining
Admin visual/no-scroll/accessibility, sync/notification runtime, seeded upgrade and
contract/session verification. Initial HEAD/remote3dc8488; prior CI40023191770 at
3a4aac2 retained. Concrete matrix and running tasks: [OWNER-REVIEW.md](OWNER-REVIEW.md).

- [x] Recover exact state/pinned contracts/criteria; private PMP and Stage6 preserved.
- [x] Changed read-only local emulator5584 attempt: host SIGSEGV139 before ADB,
  zero app executions/screenshots. Use existing KVM CI; no unchanged retry.
- [x] Test-only production-screen render harness and controlled WorkManager/permission
  checks compile. Host assembly/JVM/lint gate PASS, 275 JVM/0 failures/errors/skips; six host scripts PASS. Default app networking/decisions remain disabled.
- [x] Source63f7e21 normal-pushed/read back; run40027522387 executing full suite.
- [x] Upgrade job failed before boot: corrupt API36 system-image ZIP; zero upgrade
  checks. Trusted baseline/release signer checks PASS; raw evidence retained.
- [x] Bounded image-install preflight synthetic command harness PASS; additional
  worker-delivery fixture source compiled/host gate PASS2m25s. Runtime pending.
- [ ] Execute source-bound CI and immutable-v0.10.1 same-runner upgrade; inspect
  actual Admin screenshots, document limitations and synchronize publication.
- [ ] G1–G9/G11 approved live integration BLOCKED; explicit owner acceptance pending.

This section supersedes older current-status text. Stage7A FINAL ACCEPTED; X10
NOT FIXED/NOT VERIFIED, deferred Gould0.12. No main merge/release/production activation.

---

## Stage 7B Android Admin — preserved implementation and verified CI (2026-10-23)

**CI recovery COMPLETE; Stage 7B acceptance remains IN PROGRESS.**
Purpose: preserve the interrupted Android Admin work and recover reproducible
verification. Scope: existing queue/review/conflict/audit/settings/sync, contract
fixtures, native tests, dispatch-only CI and durable evidence. Personal Room,
backup and practice systems have no recovery-session changes. Production Admin
networking and decisions remain disabled.

- [x] Preserve original cee9330 work in checkpoint0703cd8, with private PMP excluded.
- [x] Publish corrected source `3a4aac27e2eb8ae70716538d86acd374cdd38077`;
  ordinary push and exact remote SHA readback PASS.
- [x] [Run40023191770 SUCCESS](https://github.com/stokonn/Goldberg/actions/runs/40023191770),
  exact source binding/Ubuntu24.04/KVM/API36/emulator-5554/qemu1 verified.
- [x] Fresh full host gate SUCCESS7m41s:275 JVM tests,0 failures/errors/skips;
  lint0 errors/132 warnings/1 hint; six host scripts PASS,1050 EN/CZ resources.
- [x] Full connected task SUCCESS3m38s: XML182 tests,0 failures/errors,2 existing
  external-fixture skips; all4 Stage7bAdminRuntimeTest bodies PASS. No filters,
  added skips or weakened assertions. Console184/XML182 discrepancy retained;
  XML182 testcase elements are authoritative. Skip messages are empty in XML;
  unchanged source guards require external portrait/provider files not supplied.
- [x] Teardown removal verified in actual logcat. Same-built-APK install-r Success;
  MainActivity cold launch Status:ok. Both smoke screenshots inspected: Home with
  onboarding. These are not Admin/no-scroll/theme/font/accessibility acceptance.
- [x] Artifact12532680601 ZIP integrity and remote SHA256 digest verified:
  5c23ceb0ede9690a82934eae9fa87f75930839aa5859d85bc535bb7ac58ac525.
  Raw logs/XML/skip details/screenshots/file hashes retained in results/ci-run-40023191770.
- [x] Raw evidence retained byte-for-byte; narrow CRLF EOL allowance leaves normal
  source whitespace checks active. Old-source run40015851111 is historical only.
- [x] Issue31 dashboard/checkpoint and local handoffs/checklists synchronized;
  umbrella OPEN / Goldberg Development Project In Progress; private PMP unchanged.
- [ ] Admin visual/no-scroll/locale/font/accessibility, notification/WorkManager
  runtime, seeded upgrade, approved live contract/session integration and owner
  acceptance remain OPEN. G1–G9/G11 integration unresolved; G10 locally closed.

[Final CI checkpoint](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6374873953) · [Recovery record](SAFE-RECOVERY.md).
The final docs-only checkpoint contains this evidence; its exact SHA/readback is
recorded in Issue31 after publication. No source/runtime claim is transferred from
3a4aac2 to another commit. Next: execute the remaining Admin fixture screenshot/
no-scroll matrix on an explicitly identified emulator, retaining all other gates.
Stage7A final owner acceptance and X10 deferred Gould0.12 remain unchanged; Stage8
readability/accessibility/touch-target checks remain required. No physical device,
main merge, release, deployment or production activation.

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
