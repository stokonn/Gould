# Proposed owner authorization — local-only formal tasks 6.1–6.3

**DRAFT FOR OWNER REVIEW — NOT APPROVED OR IN FORCE.** Prepared 2026-10-22.
This text is the proposed amendment; the existing gate policy remains unchanged.

> I authorize adoption of the reviewed PostgreSQL prototype and formal local-only
> implementation of tasks 6.1–6.3 in the exact scope of LOCAL-CORE-ADOPTION.md on
> an isolated branch, using disposable PostgreSQL 17, synthetic identities, fake
> clock/JWKS and ephemeral test keys only. Reuse the tested prototype/evidence;
> do not implement a new backend architecture or deploy a service.
>
> For this local-only subset, I approve a narrow exception to the rule requiring
> 6.0f acceptance before formal implementation. It may proceed while 6.0f remains
> OPEN/UNCHECKED. Amend the corresponding CHECKLIST 6.0f/D8b, OPEN-DECISIONS D8b,
> CLOUD-READINESS-PLAN §2–4/step 10 and POSTGRESQL-DESIGN-REQUIREMENTS references
> consistently for this subset only. This does not select ORD-B or waive C2 §12.
>
> Q1/D5b, C4, C2 §12 and 6.0f remain open with their existing evidence requirements.
> ORD-A's cloud sequencing and prerequisites remain binding. 6.0d/6.0e and real-user
> collection remain unauthorized. No hosted Supabase changes, provisioning,
> deployment, OAuth, production credentials, billing, paid service, Stage 7 work,
> merge, tag or release. Permanent hosting budget stays 0 CZK/month.
>
> Authorize only actual local 6.1–6.3 implementation and verification milestones;
> do not check global tasks or integration acceptance on local evidence alone.
> 6.4–6.18 require their existing prerequisites and separate authorization.
> Report exact source/test evidence and any compatibility/policy boundary before
> proceeding beyond this scope. The approved API remains unchanged unless I also
> approve the separately prepared revision-state patch after compatibility review.

The owner can approve this exact scoped text or provide a correction. Recording
this draft does not itself authorize implementation or alter an approval gate.

created by stokonn with love <3
