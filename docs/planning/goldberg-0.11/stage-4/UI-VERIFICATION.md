# Stage4 UI inspection — partial WIP evidence

Actual emulator captures only; no mockups or physical-device verification. Synthetic37-session upgrade fixture and local personal snapshots; no real provider identity certified. API26,480×800 at160dpi, established Slate/Outline/Compact appearance settings. These are specific inspected states, not full UI acceptance.

## Original9daadb3 verification APK

APK SHA256 `67594aadc1deac61c70bfed954203a4d3eaf8388526a7da83c7bde044e70f861`.

- [EN Nocturne Discover](results/screenshots/api26-en-nocturne-discover.png): offline banner, unchecked online action, local content/categories and five tabs render. The original legacy Library label is visible.
- [EN Ivory Discover](results/screenshots/api26-en-ivory-discover.png): same native layout/theme controls; existing local snapshot names are retained.
- [EN Ivory work](results/screenshots/api26-en-ivory-work.png): captured a misleading unconditional verified-source notice for an observed personal snapshot. This finding was fixed in the UI candidate below.
- [EN1.5× with keyboard](results/screenshots/api26-en-ivory-large-discover.png): keyboard occupies navigation space; this is a focused-input state, not evidence of five visible tabs.
- [EN1.5× keyboard dismissed](results/screenshots/api26-en-ivory-large-tabs.png): actual large-font layout inspection, retained separately.

## Corrected UI candidate

APK SHA256 `1dd7e3339f40998b3880c5fc3d2c07f9cb2c2ecad0b396d9365fe7d12737f2ee`. Only application changes after9daadb3 are CZ/EN Repertoire/Repertoár labels and conditional reviewed-versus-observed identity notice. Targeted API26 regression29/29 passed; no unchanged expensive JVM/performance repeat was needed for these presentation fixes. Installed APK hash matched the local artifact and RECORD_AUDIO app-ops reported `No operations.`

- [CZ Ivory Discover](results/screenshots/api26-ui-fixed-cs-ivory-discover.png): all five localized tabs including Repertoár, wrapping privacy text, correct local/offline state. The snackbar truthfully preserves the unreadable seeded recovery payload.
- [CZ Ivory local work](results/screenshots/api26-ui-fixed-cs-ivory-work.png): observed snapshot explicitly requires verification; no add-to-repertoire action or invented source assertions. Back returned to Discover.
- [CZ1.5× five-tab layout](results/screenshots/api26-ui-fixed-cs-large-tabs.png): all five tab labels/touch controls remain visible, category chips wrap to another row, content scrolls under the fixed bottom bar. No clipping of tab labels observed in this capture.

Settings theme and Czech selection were performed through the real app UI after upgrade preference comparisons had passed. Raw unfinished recovery was not discarded or converted into practice credit. One combined UI-helper sequence initially failed to find Settings after font resizing; a separately inspected settled Profile/Settings navigation succeeded. This is not a passing automated UI suite assertion.

## Remaining acceptance

Further API37 and final-candidate EN/Ivory states; artist/album/release/movement/arrangement and accepted verified-provider details; loading/partial/rate-limit/retry and network transitions; correction document picker and portrait credits/render; all existing appearance preferences,2× fonts/smaller widths, screen-reader focus/touch targets, rotation/process-death navigation restoration. Numeric legacy artist fallback `87` appears in local snapshot landing content; improve its presentation without inventing an artist name. Search work subtypes need clearer movement/arrangement/medley result labeling. Do not claim full accessibility, all multilingual states or Stage4 visual acceptance from these captures.

created by stokonn with love <3

## Current API37 inspected state

[API37 EN Nocturne Discover](results/screenshots/api37-ui-en-nocturne-discover.png), current1dd7e3… APK: five corrected labels, offline fallback, unchecked online checkbox, source snapshot names retained, visible no-data-loss recovery snackbar. Captured after105-entry suite and four real process-death/restart cases passed. API37 installed hash matched; RECORD_AUDIO app-op mode was `ignore`. This adds one inspected large-screen state, not full UI acceptance.
