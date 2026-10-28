# Gould 0.12 — Android Home Screen Widgets | approved product direction, implementation requirements

**Status:** These widgets are a REQUIRED part of the complete Gould 0.12 scope. This document supersedes any older prompt/package that omitted them. **One complete 0.12 development stage**, internal checkpoints permitted.

## Scope and owner-selected variants

1. **Quick Practice — option A.** A glanceable, direct-to-Studio launcher for practice from the user's Android home screen. Small form can show logo/clear shortcut; larger form shows the actual selected/latest relevant piece or last known draft state, with meaningful accessible label. If no piece exists, show Free Practice or a clear neutral empty state, not invented content. Tapping must route intelligently, but **must not immediately start a microphone, recording, timer, or create a new session**. The user confirms starting/resuming in Studio.
2. **Weekly Activity — option C.** An attractive seven-day graph and credited practice totals. Week is **Monday–Sunday (option A)**, in the user's locale/time zone; distinguish days with zero activity from unavailable/stale data. Pull numbers from the existing verified practice ledger / summary, not wallpaper dummy data, wall-clock elapsed time or recorded audio length. Small size shows essential total/status; larger sizes include a readable chart and relevant verified stats without clutter or duplicating data.
3. **Intelligent Open — option B.** Contextual navigation on widget tap: active practice → current Studio session; durable paused/uncompleted draft → Studio with safe Resume/Review choice; otherwise → Studio ready state. If a particular piece is known and persisted, open the appropriate piece context without starting a session. Preserve existing state and pending recovery; no silent discard, duplicate start or permission bypass.
4. **Adaptive Sizes — option B.** Support practical launcher sizes, e.g. 1×1, 2×1, 2×2 and 4×2 (actual launcher cell sizes vary). Avoid declaring all variants pixel-identical. Use responsive layout and text/icon simplification; do not render illegible charts at 1×1. Work with Android widget resizing and density. Enforce accessible font scaling, safe clipping, touch targets and meaningful accessibility descriptions.
5. **Stale/error handling — option B.** Work offline. Store/reuse the last *verified persisted* non-sensitive display snapshot where appropriate. If state cannot be refreshed, show a subtle **last-updated / stale indicator** and safe tap path; never fabricate fresh numbers, show blank/zero as truth when unavailable, or silently start services. Refresh on relevant database/active-session changes with efficient bounded scheduling; update when locale/time zone and day/week change, without excessive battery drain.

## Design and app compatibility

- Match existing Gould 0.11 Editorial design, Ivory/Nocturne and current semantic user accent. Respect actual supported appearance setting values, launcher dark/light constraints and visual contrast. Mockups are illustrative, not exact pixels or assets.
- Keep actual five bottom destinations Home / Library / Discover / Studio / Profile. Widgets are Android home-screen surfaces, **not a new in-app bottom tab**.
- App belongs in an appropriate **Education** app classification where actually supported by distribution metadata; do NOT confuse Play Store/app category with Android `AppWidgetProviderInfo.widgetCategory`, whose valid values are launcher/home/other host categories. Inspect existing app settings/manifest before changing classification. Do not invent unsupported manifest flags.
- Implement using the current Android project and compatible supported framework/tooling. Inspect whether Jetpack Glance or platform `AppWidgetProvider` best fits existing build, support minSdk 26 and target SDK 36, and avoid unnecessary heavy dependencies.
- No network permission or backend calls for widget data. No direct exposure of private note text, audio, identities, backend credentials, or private files in widget RemoteViews/updates. Widgets never request microphone permission or create recordings.
- Use `PendingIntent` with safe flags and navigation/deeplinks compatible with the existing `OpusApp` and `PracticeCoordinator`. Handle stale widget intents, missing extras, reboot/process death and multiple widgets independently. Do not use a secondary timer or duplicate practice-credit calculation.
- Cosmetic widget samples may display made-up pieces or times. **Never hardcode them** into real app UI, and do not bundle design screenshots into the APK.

## Validation and acceptance

- [ ] Quick Practice 1×1 and 2×1/4×1 open correct Studio screen (ready, active, paused/recoverable), no silent microphone/time/recording activity.
- [ ] Weekly Activity 2×2 and 4×2 show seven verified values Monday–Sunday with accurate credited time, timezone rollover, zero/empty states and CZ/EN labels.
- [ ] UI behaves sensibly on small/large launchers, resizing, system font 1.0×/1.5×/2×, Ivory/Nocturne and user accent changes.
- [ ] Offline after process death/reboot; backed-up/restored journal integrity untouched; no spurious data reset or session duplication.
- [ ] Refresh on genuine data change and date rollover without frequent unnecessary wakeups; check performance/battery implications.
- [ ] Stale snapshot is explicitly labeled with a meaningful last-updated or retry state; never disguised as current.
- [ ] Automated state/navigation/unit tests plus actual Android API 36 emulator widget add/resize/tap/screenshots. Do not infer physical-device behavior; owner phone testing is forbidden to the agent.
- [ ] Compare real screenshot results to the approved 0.11 app and visual inspiration. Include widget shots in the owner's visual sign-off gate before release.

## Reference images

- `references/10_widget_board_overview.png` — high-level widget designs (Quick Practice / Weekly Activity / Intelligent Open / adaptive sizes).
- `references/11_quick_practice_widget.png`, `12_weekly_activity_widget.png`, `13_intelligent_open_widget.png`, `14_adaptive_widgets.png` — separate cropped references for legibility. These are **not** actual app widget screenshots; some visible numbers/labels are invented and should not be shipped.

created by stokonn with love <3
