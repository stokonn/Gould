# Goldberg 0.9.2 — retained verification evidence

Captured on 2026-10-17 using an isolated API-37 emulator, explicitly targeted as `emulator-5580`. No owner-phone commands were executed. These are controlled test data, not owner journal records.

- `resume-gradle.log`: initial resumed host build/test/lint pass; source before the subsequent Home/avatar corrections.
- `resume-instrumentation.log`: initial resumed 30-test Android pass.
- `avatar-regression-before-fix.log`: intentionally failing regression confirming the avatar decoder's division by zero. This is not a passing result.
- `screens/keyboard-en-light.png`, `keyboard-cs-dark.png`: focused Practice notes, controls and keyboard; duplicated blank inset area absent.
- `screens/review-keyboard-cs-dark.png`, `history-editor-keyboard-cs-dark.png`: nested review fields remain visible; long forms scroll.
- `screens/recording-editor-keyboard-en-light.png`, `recording-editor-keyboard-cs-dark.png`: focused performer field and dialog Save/Cancel controls above the keyboard.
- `screens/history-compact-cs-dark.png`, `history-review-cs-dark.png`: saved two-second manual test session, compact row and full review.
- `screens/home-free-practice-fixed-en-light.png`: corrected recent-session fallback after Free Practice, recorded duration and real seven-day graph.
- `screens/home-empty-light.png`: genuinely empty test journal, zero bars remain absent.
- `screens/profile-cs-light.png`, `profile-empty-dark.png`: local signed-out profile, compact statistics and optional Google prompt.
- `screens/appearance-cs-light.png`, `appearance-cs-dark.png`, `data-storage-cs-light.png`: localized appearance and data-management controls.
- `screens/piece-detail-cs-dark.png`, `piece-editor-cs-dark.png`, `repertoire-cs-dark.png`: actual piece detail/editor and pinned repertoire control layout.
- `screens/about-cs-dark.png`, `tux-cs-dark.png`, `harpsichord-cs-dark.png`: actual About activation, terminal signature and connected chromatic keyboard. Audible playback unverified.
- `screens/resume-settled.png`: onboarding. `resume-initial.png` is only a transient splash and is not accepted screen evidence.
- `screens/*360-font130*`: 360dp width, 130% font scale, Czech/dark Home/Profile/Practice. The Home capture preceded the corrected Free Practice fallback; retain it as discovery evidence, not the final fixed state.

Screenshots were inspected against the written design specification. Original visual mockups are unavailable. The full cross-product of all screen/theme/font/network states and authenticated Google-account screens is not claimed verified. See ../../VERIFICATION-0.9.2.md and ../../MANUAL-VERIFICATION-0.9.2.md for limitations and release gates.

Final candidate evidence: `final-gradle.log` (70 JVM tests, zero failures, lint zero errors/57 warnings), `final-instrumentation.log` (31 executed tests, all passed), `final-host-checks.log`, `final-apk.log`, `candidate.json` and `source-manifest.sha256`. The final avatar decoder supersedes the deliberately failing red-regression build. `project-views.json` verifies the actual private Project Board/Status grouping.

Final release review adds `release-google-offline-settled.png` (actual provider network error), `release-google-offline-return.png` (localized cancellation, local profile intact), offline badge/catalogue captures, successful catalogue retry and composer-detail rendering. These were inspected. `release-google-offline-flow.png` is transitional, not settled-error evidence. Six newly reported Google scenarios are separately attributed in docs/MANUAL-VERIFICATION-0.9.2.md; they are not emulator tests.
