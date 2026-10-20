# Stage 6 — Privacy, licensing and open-decision register (D4 / D5 / D8)

**All decisions below are UNRESOLVED owner calls.** They formalize the D4/D5/D8 rows
of the canonical register in [STAGES.md](../STAGES.md) and the reconciliation in
[stage-2/READINESS.md](../stage-2/READINESS.md). Recommendations are proposals from
planning, **not approvals**. No identity, contact details, controller information or
credentials are invented here. **No legal compliance (incl. GDPR) is claimed or
inferred** — free hosting is not proof of compliance (CLOUD.md), and the
privacy/terms decisions below must be settled by the owner before public collection.

## 1. Decision D4 — backend and free-tier acceptance

| # | Question | Alternatives & trade-offs | Proposal (not approved) | Blocked until decided |
|---|---|---|---|---|
| D4a | Which backend? | **Supabase Free** — managed auth/RLS/transactions, least custom security code; vendor/config risk. **Workers+D1** — no inactivity pause, edge latency; all auth/session/authorization custom, CPU-budget risk. **Defer/no backend** — contributions stay local-only; honest zero-risk option. | Supabase Free moderation-only pilot (ARCHITECTURE.md §3). Building both to postpone the choice is prohibited. | All server implementation and provisioning. |
| D4b | Accept free-tier suspension/quotas? | **Accept graceful-denial model** — Supabase pausing, daily exhaustion, no SLA; app degrades truthfully. **Reject pausing** — pushes toward Workers (unproven) or no backend. | Accept graceful denial + kill switch; publish no SLA. | Provisioning; truthful in-app "unavailable" copy. |

## 2. Decision D5 — community operation, privacy and licensing

| # | Question | Alternatives & trade-offs | Proposal (not approved) | Blocked until decided |
|---|---|---|---|---|
| D5a | Who is the operator/controller contact? | Named owner contact in privacy notice / pseudonymous contact route / no public collection at all. | **Owner must supply or explicitly choose a route — cannot be invented by planning.** | Any public collection, OAuth console setup, privacy notice publication. |
| D5b | Privacy & data-processing responsibilities | Owner-as-controller using provider DPA; document subprocessors/transfers; minimal fields (iss+sub, timestamps). | Owner-as-controller posture; subprocessor/region review at provisioning; privacy notice content drafted only after D5a/D5c. | Privacy notice; collection start. |
| D5c | Processing region & transfers | Pin EU region (Supabase supports region choice; Workers is global-edge — data at rest location differs). | Prefer EU data residency if Supabase is chosen; document edge-processing caveat. | Provisioning. |
| D5d | Account deletion & retention | (a) Pseudonymize identity, keep approved attribution; (b) full deletion incl. attribution where license permits; (c) fixed retention period then purge pending/rejected data. Retention of rejected submissions is a separate sub-choice (proposed: purge rejected payloads after N days, keep audit skeleton). | (a) for approved contributions (license integrity) with per-case override; pending/rejected purged after a short window. Exact numbers need owner review. | `DELETE /v1/me` implementation; retention jobs; terms text. |
| D5e | Contribution terms & licensing | Two-track: CC0 structured facts + separately labelled compatible license for contributor-authored text (existing COMMUNITY-ADMIN.md proposal) vs single blanket license. Cannot relicense copied Wikipedia prose by ticking a box; contributor warrants rights. | Two-track licensing as proposed; final license identifiers chosen with owner review. | Submission terms UI/API; moderation checklist. |
| D5f | Community audience & age safeguards | General audience (no age data, no child-directed features) / 16+ / all-ages with moderation. Each affects terms wording, not features. | General audience, no age data collected; terms reviewed by owner. | Terms publication; public collection. |
| D5g | Moderator/admin authorization | Owner-only moderation / owner + named moderators via explicit grants. Escalation route = owner. | Owner-only initially; grants owner-controlled, fully audited. | Role seeding; queue launch. |

## 3. Decision D8 — Admin packaging and bootstrap

| # | Question | Alternatives & trade-offs | Proposal (not approved) | Blocked until decided |
|---|---|---|---|---|
| D8a | Android Admin packaging | In-app role-gated destination (reuses Compose components, one APK, hidden-but-server-enforced) vs separate Admin APK (isolation, second signing/release pipeline cost). | In-app gated destination; security never relies on hidden UI (AUTH.md §6). | Stage 7 Part B Android work. |
| D8b | Web Admin auth/session | BFF same-origin HttpOnly cookie + CSRF (strong XSS posture, tiny backend sessions) vs static SPA + managed auth tokens (simpler host, weaker storage guarantees, heavier XSS testing). | BFF (AUTH.md §5). | Stage 7 Part B web work. |
| D8c | Admin bootstrap & recovery | Out-of-band console grant + documented recovery (AUTH.md §7) vs pre-created trusted credentials (prohibited — inventing credentials) vs emailed recovery flows (needs D5a contact, new dependency). | Out-of-band grant; recovery = repeat grant procedure. | Admin endpoint activation. |

## 4. Pre-collection gate (all must be true before the first real user is collected)

- [ ] D4a/D4b decided; backend provisioned under a separate owner instruction.
- [ ] D5a–D5g decided; privacy notice, controller contact and contribution terms
      drafted and **published before collection** (Stage 6 acceptance, STAGES.md).
- [ ] D8a–D8c decided.
- [ ] THREAT-MODEL.md [I]-venue rows executed for the chosen backend.
- [ ] Export + isolated restore drill passed (ZERO-COST-OPS.md §4).
- [ ] Owner explicitly authorizes opening the service to real contributors.

An unanswered decision blocks only its dependent tasks (STAGES.md standing rule);
it never blocks unrelated local work.

created by stokonn with love <3
