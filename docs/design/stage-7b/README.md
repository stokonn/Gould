# Goldberg 0.11 — Stage 7B Android Admin visual handoff

**Status:** Visual direction approved by owner through provided examples; implementation now authorized separately in `START_HERE_PROMPT.txt`.

## Which reference takes priority?

1. `original-references/user-reference-dashboard.png` and `user-reference-re-review.png`: owner-provided screenshots of the previously approved simple dark UI direction. These images include external ChatGPT interface elements — **do not implement the chat chrome**.
2. `png/01...09`: nine separate, clean, dark, schematic implementation mockups aligned to the screenshots. These are illustrative and may not depict every backend state.
3. `svg/01...09`: editable vector source for those same nine mockups.
4. `DESIGN-SPEC.md`: authoritative constraints and screen-by-screen behavior.

Do not use the rejected ornate ivory/editorial mockups or earlier six-screen collage as a source of truth.

## Files

| PNG | Screen/state |
|---|---|
| `01_administration_dashboard.png` | Dashboard — 4 stats + 3 destinations, no scroll |
| `02_moderation_queue.png` | Search, filters, deferred section, compact rows, update pill |
| `03_proposal_detail.png` | Current vs proposed comparison |
| `04_approve_confirmation.png` | Approval confirmation |
| `05_reject_confirmation.png` | Rejection and required reason |
| `06_request_changes.png` | Information request |
| `07_guided_re_review.png` | Conflict recovery — most closely follows second original screenshot |
| `08_audit_log.png` | Role-scoped event timeline |
| `09_admin_settings.png` | Sync and notification preferences |

PNG output is 880×1840 pixels; SVG canvas is 440×920 design units. These are *visual references*, not exact device density or Android layout dimensions. Content and counts are fictional examples.

## Installation into project

Unzip at repository root. The `docs/design/stage-7b/` and `docs/ai/` paths are already structured for that purpose. Copy the contents of root `START_HERE_PROMPT.txt` into Codex/OpenCode, or ask it to read that file. Check existing repository documents before overwriting or merging any same-name files.

No source code, migrations, credentials, or deployable application files are included in this package.
