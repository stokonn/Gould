# Stage 6 — Readiness continuation, 2026-10-22

Purpose: recover the interrupted checkpoint, narrow documentary gaps and prepare
future 6.1 requirements while preserving every approval gate. Documentation only.
Scope: Stage 6 planning and docs/ai; no application, database, spike or infrastructure changes.

## Session checklist

- [x] Inspect branches/worktrees, planning `b3edc2a`, spike `c21e550`, Issue #31 and latest checkpoint `6348121556`.
- [x] Preserve completed experimental results without rerunning the suites.
- [x] Prepare Stage 6-only Issue description diff and obtain explicit owner approval.
- [x] Apply approved Issue description update and verify exact body by readback.
- [x] Review public official subprocessor, transfer, pricing and GDPR sources.
- [x] Prepare F1–F5 requirements and synchronize local checkpoint documents.
- [x] Run documentation/policy checks and record actual results.
- [x] Publish the prepared session checkpoint to Issue #31 (owner-authorized; comment 6349066370).
- [ ] Finalize verified documentation commit/push and append remote evidence.

Completion: local documentary work VERIFIED; checkpoint posted with owner approval;
verified documentation commit/push authorized and in progress. This checklist concerns documentation work only; it never
checks off 6.0f or 6.1–6.18. See the final verification section to be recorded below.

## Recovered evidence and remote status

Planning branch `docs/goldberg-0.11-stage-6-planning` = `b3edc2a`; isolated spike
`spike/goldberg-0.11-stage-6-contract-pg` = `c21e550`. Previous results are complete:
PG 123/123; SQLite 79 passed, 0 failed, 14 PG-only skips (93 total); FOR UPDATE
20/20 and SERIALIZABLE 20/20. They are local experimental evidence, never a Supabase
or production result. No rerun was needed. Diffing original faithful sources and
`results/pg/` between `e37ed00` and `c21e550` produced no differences.

[Issue #31](https://github.com/stokonn/Goldberg/issues/31) is OPEN, milestone 4
Goldberg 0.11 is OPEN. [Goldberg Development](https://github.com/users/stokonn/projects/2)
item is In Progress (read-only verification). Latest inspected release is v0.10.1.
The interrupted Issue description sync was pending. The owner explicitly approved
the proposed Stage 6-only edit; it now references `b3edc2a`, `c21e550` and
[checkpoint 6348121556](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6348121556).
All other Issue content and all comments were preserved; exact approved-body readback passed.
Connector writes failed HTTP 403 “Resource not accessible by integration”; existing
authenticated `gh api` applied the approved edit successfully, without authentication changes.

Local continuation uses an isolated checkout at
`/home/stokonn/Projects/Opus/.gradle/stage6-continuation`, branch
`docs/goldberg-0.11-stage-6-readiness`, base `b3edc2a`. Existing worktrees and the
owner’s untracked RELEASE_NOTES.md are untouched. Verified documentation commit/push
explicitly authorized by the owner’s finalization instruction; evidence to be appended.

## Documentary findings (public reads on 2026-10-22)

These are incremental findings; C4 §D steps 1–9 remain unchecked. No console,
billing configuration, subscription, account or resource was changed.

| Area | Executed review and evidence | Still required |
|---|---|---|
| SB-P9 / C3 Q8 | Current [Supabase PDF](https://supabase.com/legal/subprocessor-list/October-12-2026.pdf), dated October 12, 2026: both pages parsed as text (22 entries). Examples: AWS hosting; Cloudflare hosting/CDN; Google hosting; Braintrust/Sentry tracing; Supabase US/SG support. | Map applicable services and data categories to each recipient and processing location, assess safeguards, subscribe to updates owner-side and monitor the DPA’s objection window. Country column identifies entities, not proof of actual processing location. PDF retrieval gap narrowed; full review remains open. |
| CF-P10 / C3 Q4 | [Cloudflare list](https://www.cloudflare.com/gdpr/subprocessors/cloudflare-services/), dated October 1, 2025, lists Google and Oracle for the Developer Platform and group affiliates for support. | Confirm which apply to plain Workers Free, data categories, actual locations and support access; notification monitoring. Product-wide list does not prove a Frankfurt-only path. |
| SB-P8 / international transfers | [Supabase residency FAQ](https://supabase.com/legal/privacy-resources/data-residency-and-transfers-faq), v1 October 6, 2026, separates regional core hosting from account, support, telemetry and CDN processing; refers to SCCs for Singapore/US. | Controller review of the actual flow, recipient roles, SCC applicability and supplementary safeguards; no EU-only or compliance claim. |
| SB-P10 / C3 Q5 | [Trust Center](https://trust.supabase.io/) public page opened. Accessible text contains profile/certification information; no TIA document or Free-plan access entitlement was exposed. | Owner obtains access or provider clarification privately, then qualified reviewer assesses TIA. No request submitted, NDA accepted or contact sent. Status now “opened; TIA not retrieved,” superseding “unopened.” |
| DPA notification windows | [Supabase DPA](https://supabase.com/legal/customer-resources/data-processing-addendum) §6.3: at least 30-day proposed-change notice for subscribed customers, objection within 5 days. [Cloudflare DPA](https://www.cloudflare.com/cloudflare-customer-dpa/) §5.2: objection within 10 days. | Owner assigns monitoring responsibility and subscribes only under separate instruction; no subscription made this session. Recheck versions/day-of. |
| X-2 transfer landscape | [European Commission EU–US page](https://commission.europa.eu/law/law-topic/data-protection/international-dimension-data-protection/eu-us-data-transfers_en) still describes adequacy for participating US companies. | Current legal-status review and recipient certification/scope check at decision time; this page alone is not exhaustive legal/court monitoring and does not establish either vendor’s certification. SCC/TIA assessment remains required for applicable non-adequacy transfers. |
| X-6 official GDPR | EUR-Lex ELI, CELEX HTML and CELEX PDF routes returned a JavaScript anti-bot page, not Regulation text. An EDPS page also returned 403. | Obtain authentic Regulation text for professional review; do not substitute a search snippet or claim article verification. X-6 remains open. Historical five failed attempts remain preserved; this session’s failures are additional, with a different observed response. |
| X-7 | No new MoJ translation retrieval attempted. | If needed by reviewer, obtain official English translation; do not block all work solely on translation when authentic Czech law is available. |

The PDF was reviewed through extracted text; no application or PDF screenshots were
visually inspected. No full third-party document is copied into the repository.

## Zero-cost verification still needed

The owner’s verified first-hand observations remain attributed to the owner:
Workers Free active with no payment method; Supabase Personal Free organization
with zero projects; Frankfurt selectable in the creation form. No agent console
verification is implied. Supabase billing/payment status remains unverified.

[Supabase pricing](https://supabase.com/pricing) still lists Free at $0, 500 MB DB,
5 GB egress, 5 GB cached egress, 1 GB storage, 50k MAU, two active projects and
one-week inactivity pausing. [Billing FAQ](https://supabase.com/docs/guides/platform/billing-faq)
confirms organization-level subscriptions and Free restrictions after quota excess;
project allowance also depends on Owner/Admin memberships, so zero projects in
one organization alone does not prove all account-wide allowance is available.
No paid plan/spend-cap workaround is acceptable. No project is to be created now.

[Workers pricing](https://developers.cloudflare.com/workers/platform/pricing/)
still lists Free runtime limits. [Observability pricing](https://developers.cloudflare.com/observability/pricing/)
announces a December 1, 2026 transition: Free ingestion 0.5 GB/day, seven-day
retention, additional ingestion unavailable. These future terms are not today’s
active retention evidence. Legacy Log Explorer has separate pricing and must not
be enabled as a zero-cost assumption. Recheck day-of and on/after December 1.

Remaining account/configuration evidence: current organization/plan and no payment
method/add-ons/trials/chargeable services; MFA/privileged access; plain Workers
logging configuration; Supabase SSL/RLS; and an existing free encrypted export
target. Free automatic backups are not included; daily export/restore and actual
RPO remain unverified operational requirements. Retention for provider-controlled
edge metadata and dashboard/security logs remains unresolved. Do not infer absence
of retention from disabled application logs or inability to inspect billing.

## Prioritized blockers and owner actions

1. **P0 — Q1/D5b:** route the existing Q1-PROFESSIONAL-REVIEW-BRIEF.md (13 questions,
   C-1…C-6 confirmations) to an appropriate professional. Record a role-based outcome
   privately, then explicit owner decision in the register; Q1-D remains preference
   only. No private identifying details belong in GitHub. This blocks provisioning.
2. **P1 — documentary C4:** obtain TIA/Free-access clarification and exact retention
   answers; map applicable subprocessors/transfers with the reviewer; retrieve
   authentic GDPR. Agent can continue public research; owner must handle private
   access/decisions and any provider contact. No contacts sent this session.
3. **P1 — account zero-cost evidence:** owner confirms Supabase payment/billing
   posture and applicable project allowance without creating a project, adding a
   payment method or changing plans; later refresh both providers’ Free/no-add-on
   evidence on the authorized verification day. Keep identifiers out of public docs.
4. **P2 — gated private verification:** only after legal/operator readiness, obtain
   separate scoped authorization under existing ORD-A, execute fresh C4 and the
   synthetic-data runtime/hosted transaction checks, document cleanup and costs.
   C2 §12 remains OPEN; this continuation authorizes none of those actions.
5. **P3 — owner acceptance and later implementation:** assess evidence and explicitly
   accept/reject 6.0f, then separately authorize tasks 6.1–6.18. Future F1–F5 design
   requirements are in [POSTGRESQL-DESIGN-REQUIREMENTS.md](POSTGRESQL-DESIGN-REQUIREMENTS.md).
   Retention/contact/notices and restore proof remain later pre-collection gates.

**Unchanged:** Q1/D5b unresolved and blocking; C4 OPEN; C2 §12 OPEN; 6.0f
UNCHECKED; 6.1–6.18 UNCHECKED; 6.0d/6.0e NOT AUTHORIZED; ORD-A sequencing
only; permanent 0 CZK/month. Stage 6 production implementation NOT STARTED.

created by stokonn with love <3


## Executed verification (this continuation)

- All five existing host scripts exit 0: `python3 scripts/check_agent_policy.py`,
  `check_backup_fixtures.py` (3 formats), `check_history_paging.py` (106 sessions,
  4 pages), `check_localization.py` (922 EN/CZ resources), `check_migrations.py`
  (v1–9 to v10 DDL). These are host checks, not Android migration execution.
- `git diff --check` passed. Documentation links, unchanged approval checkboxes
  and unchanged non-Stage-6 sections verified by a local inspection script.
- Original faithful PG sources/evidence unchanged between e37ed00 and c21e550;
  recorded TAP/summary inspected, no spike suite rerun.
- No Gradle/JVM/lint/APK build, emulator/instrumentation, hosted runtime,
  production migration, restore or billing test executed: documentation-only scope.
  No APK/signing/hash artifact or screenshots produced/inspected this session.
- No merge, release or tag. Existing Git identity not changed. Documentation
  commit/push authorized separately during finalization; evidence to be appended.

created by stokonn with love <3


## Exact documentation file set

- docs/ai/STATE.md
- docs/ai/HANDOFF.md
- docs/ai/VERIFICATION.md
- docs/ai/STAGE6-ISSUE-CHECKPOINT-DRAFT.md (new; posted snapshot preserved)
- docs/planning/goldberg-0.11/STAGE-CHECKLIST.md (Stage 6 entry only)
- docs/planning/goldberg-0.11/stage-6/C4-PREPROVISIONING-CHECKLIST.md
- docs/planning/goldberg-0.11/stage-6/CHECKLIST.md
- docs/planning/goldberg-0.11/stage-6/DATA-MODEL.md
- docs/planning/goldberg-0.11/stage-6/Q1-PROFESSIONAL-REVIEW-BRIEF.md
- docs/planning/goldberg-0.11/stage-6/README.md
- docs/planning/goldberg-0.11/stage-6/READINESS-CONTINUATION.md (new)
- docs/planning/goldberg-0.11/stage-6/POSTGRESQL-DESIGN-REQUIREMENTS.md (new)


## Finalization checklist (owner instruction, 2026-10-22)

- [x] Post prepared continuation checkpoint to existing Issue #31 (6349066370).
- [x] Review all 12 documentation changes; preserve technical gates and other worktrees.
- [x] Re-run the five existing host checks and documentation scope/link/checkbox checks.
- [ ] Commit/push only verified Stage 6 documentation and verify remote identity.
- [ ] Prepare a concise Issue dashboard from latest Stage 1–8 checkpoints, with link checks and before/after counts.
- [ ] Obtain owner approval of the final dashboard before replacing the Issue description.

Issue cleanup is editorial only; no technical approval gate or stage acceptance changes.

Finalization pre-commit verification: all five host scripts re-executed and PASS
(same counts as above); documentation review/link/scope/unchanged-checkbox check
PASS; git diff --check PASS. No code/spike/emulator reruns. Owner Git identity
read from existing configuration, not changed. Publication evidence follows after push.
