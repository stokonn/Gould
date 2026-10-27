# Contract/session verification disposition — acceptance continuation

Purpose: classify all available Android contract/session evidence without replacing immutable authority or activating an integration. Scope: existing legacy Admin facade, shared comparison corpus and current parser/session/data-safety tests. No Stage6 worktree, schema or contract changes.

## Executed locally before source63f7e21 dispatch

- [x] `python3 scripts/check_admin_contract_fixtures.py`:29 authored cases and15 immutable bcc779685d79e99451796b3fa45657b3bca8dd99 source hashes PASS.
- [x] Full JVM suite275/0 failures/errors/skips. Its Admin subset is101 cases: Foundation24, Stats6, Queue12, Review26, AuditSync9, ContractIntegration24. Raw per-case XML remains in the prior CI; current case inventory is in results/acceptance-preparation-2026-10-23/admin-jvm-coverage.json. Counts are subsets, not additional tests.
- [x] ContractIntegration24 verifies separated authorities/inactive mapping; default disabled transport even with saved endpoint/credentials; explicit fixture seam; legacy-supported queue/detail; unbound candidate principals, structured cursors and missing proposal revisions rejected; exact BIGINT/Long and legacy revision bounds; typed malformed payloads; opaque cursor preservation; no invented conflict fields; replay identity and unequal entity/proposal/published revisions; no double-counting candidate transitions.
- [x] Review26 includes explicit confirmations, required reasons, stale revision/state/capability/self-decision protection, same-key uncertain replay, conflict/no automatic resubmit, background lock, cancellation after a write, account switch during requests, cancelled sign-out clearing local sensitive state and evidence-only diffs.
- [x] AuditSync9 covers OFF defaults, unmetered Wi-Fi/mobile-consent policy, notification opt-in plus permission, incomplete/failed snapshots yielding no signature, digest changes/removals, paged audit dedup/scope and failed reload retaining rows.
- [x] Queue12 covers bounded hydration/search, dedup, update pill, failed/partial apply retaining rows, explicit ID lookup, terminal marker clearing only after confirmation and clear-state behavior.
- [x] Native four-case result at prior source3a4aac2/CI40023191770 remains valid for unchanged Keystore/marker/JSON/disabled-transport implementation; it is not evidence for new source execution. New CI results must be read separately.

## Unresolved integration authority

- [ ] G1 stats completeness/day semantics; G2 queue search/labels; G3 audit server search/filter; G4 verified current catalogue values.
- [ ] G5 approved credential exchange/session/principal binding, refresh/revocation/account lifecycle; no production OAuth or real account used.
- [ ] G6 open/dual-revision/current-result/replay semantics; G7 frozen public DTO/cursor/HTTP; G8 representation/ranges; G9 audit vocabulary/time/head windows; G11 reason/field/evidence/source/license validation policy.
- [x] G10 Android production boundary remains CLOSED: disabled networking and independent disabled decisions. Synthetic repository construction does not change OpusApplication defaults.

Stage6 M2 completion6374687079 and M3 authorization6374949198 are coordination context only. Neither freezes public v1, approves this client's adapter or authorizes policy activation. Do not import working-tree changes, silently advance source-provenance.json or treat in-process PostgreSQL/service success as Android HTTP integration.

Smallest next action: Stage6/owner publishes an explicitly approved versioned client-facing contract covering these gaps, then one bounded local adapter/session integration venue can be tested. Until then C2 is BLOCKED; local fixture/domain success is PASS only in its named venue. Stage7B and Stage7 remain incomplete.
