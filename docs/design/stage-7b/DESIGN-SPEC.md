# Stage 7B — Approved minimal dark UI design specification

## Visual language

- The **user's two original screenshots** establish the visual direction: almost black background, quiet charcoal panels, subtle rounded borders, generous but controlled negative space, no decoration for its own sake.
- Tokens in mockups (illustrative, not a mandate to override existing app tokens): background `#050505`; card `#242424`; secondary surface `#171717`; hairline border `#292929`; main text `#F6F6F6`; muted text `#A6A6A6`.
- Use limited accent colors only for meaningful semantic statuses: pending muted amber, needs info muted blue, approval green, rejection red, changed revision warning.
- The visual examples use a casual rounded/handwritten-style font to approximate the screenshots; use an appropriately legible project-approved font and preserve accessibility. **Do not package or copy font binaries**. Typography must not compromise small-screen readability.
- Rounded rectangles, minimal outline icons, readable labels, quiet separators. Avoid cream editorial theme, serif headings, gradients, shadows, stock images, oversized decorative icons or ornamental music graphics.
- Preserve the established Goldberg application header/navigation rather than implementing ChatGPT's surrounding controls visible in original screenshots.

## 01 — Dashboard

- Four 2×2 statistic tiles: Pending, Needs info, Approved today, Rejected today. Tiles are deep links to relevant server-verified Queue/Audit filters.
- Three destinations: Moderation Queue, Audit Log, Admin Settings. Queue row may include a small Resume #ID/Continue line if a safe account-scoped marker exists.
- Role label, compact section label, last-updated and manual refresh are allowed if they fit.
- **No vertical/horizontal scrolling** on ordinary supported screens/font scales. No clipped text, hidden buttons or unreadably small font. Test actual safe areas, insets and screenshots on small and standard emulator sizes. Extreme accessibility text sizes may use an accessible fallback layout instead of clipping.
- No separate Deferred section/card on Dashboard. Do not add unrelated KPIs or navigation.

## 02 — Moderation Queue

- Search across all server pages; All/Pending/Needs info filters, Re-review and Deferred as personal overlays.
- Compact rows with title, composer, ID/revision, status, right chevron. Default oldest waiting first, stable server cursor.
- Collapsed personal Deferred section; persistent markers are NOT server moderation states.
- Floating update pill only after detecting a change opportunistically; no continuous polling. Do not cover list content or controls.
- Queue is allowed to scroll; preserve scroll anchor/filter/search on return.

## 03 — Proposal Detail

- Read-only comparison of current and proposed values. Highlight changed fields subtly (color plus textual/semantic indicator).
- Tabs/sections for Comparison, Details, History and Evidence. All current source/evidence must be accessible, not just changed snippets.
- Decisions unavailable until latest revision and permissions are verified.

## 04–06 — Confirmation flows

- Separate Approve, Reject, Request Changes confirmation states, each with clear action summary and cancel option.
- Reject requires a reason. Request Changes requires a clear request and valid server transition. No bulk approvals.
- Never treat local confirmation as successful moderation; require confirmed atomic server response, revision and authorization.

## 07 — Guided Re-review

- Strong match to the second owner screenshot: "Re-review required" notice, updated fields in charcoal cards, old text struck through, current value and updated source.
- "I have reviewed revision N" acknowledgement. Complete re-review disabled until conditions satisfied. This acknowledgement does not replace server-side revision validation.
- Show intermediate history if backend supports it. If missing, be transparent and require full review.

## 08 — Audit Log

- Compact chronological list, role-scoped filters, server search/pagination, no client mutation of immutable audit records.

## 09 — Admin Settings

- Compact settings for opt-in local notifications and optional approximate 3-hour WorkManager sync (OFF by default); Wi-Fi-only default, mobile data requires explicit permission.
- No Android-side role management or destructive database controls.

## Safety and implementation notes

- These PNGs are **static illustrations** with invented demo proposal names, IDs and counts. Do not hardcode them in the app.
- Follow actual existing app architecture and verified Stage 6 API contracts; stop/report blockers rather than fabricating server behavior.
- Use Android resources, Compose/components and semantic accessibility labels as appropriate to the actual codebase.
- Stage 7A X10 is still open and blocks final release. Stage 7B implementation permission does not authorize a release or production backend deployment.
