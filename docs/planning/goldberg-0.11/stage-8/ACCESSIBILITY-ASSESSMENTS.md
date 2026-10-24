# Stage 8 — deferred accessibility and layout assessments

Status: **assessment records PREPARED; release assessments NOT EXECUTED; all three findings NOT FIXED**. Owner explicitly deferred them to Stage8 release-readiness assessment when accepting Stage7B. Stage7 Android client/synthetic acceptance remains COMPLETE.

Inputs: [accepted Stage7B report](../stage-7/stage-7b/FINAL-OWNER-REPORT.md), [October24 raw observations and hashes](../stage-7/stage-7b/results/manual-acceptance-2026-10-24/), CI25's262 screenshot/tree pairs and [retained owner decision](https://github.com/stokonn/Goldberg/issues/31#issuecomment-6388393525). No new emulator execution, listening, contrast measurement or product fix occurred during8A.

## A8-1 — duplicate Admin Settings TalkBack focus stops

**Observed, NOT FIXED; release disposition OPEN.** October24 CZ/Dark/font1.3 on360×640dp with actual TalkBack16.0.0.738667889: positions3/4,5/6,7/8 focus each TextView label and then its named checkable control. Controls were reachable, named and stateful. See original `cs-settings-focus-observed.json`, `cs-settings-focus-3.png`, `-4.png`, `-8.png`. Duplicate traversal is an observed usability defect; no state-loss or inaccessible-toggle defect is inferred.

Read-only source assessment: `app/src/main/java/com/opus/piano/ui/admin/AdminSettingsScreen.kt`, AdminSwitch, renders separate Text and Switch siblings; Switch has adminToggleSemantics(label,checked,Role.Switch). This is consistent with the retained duplicate stops; it is not a new runtime pass or a proven remediation.

- [ ] Reproduce on full real NavHost with actual TalkBack in EN/CZ, both themes, font1.0/1.3 and large-font representatives; retain service events/tree/screenshots.
- [ ] Assess whether one coherent named/role/state/action stop can serve each row while retaining touch target, keyboard/native focus, checked state, disabled behavior and exactly-once action. Do not blindly clear label/control semantics.
- [ ] If a fix is separately authorized, verify notifications/background/mobile-consent dialogs and toggles remain reachable and correct; compare retained before/after traversal and focused controls.
- [ ] Record release severity and owner disposition: fix verified, explicitly accepted release limitation, or BLOCK. Stage7 deferral alone is not Stage8 release approval.

## A8-2 — large-font bottom-tab truncation

**Observed, NOT FIXED; release disposition OPEN.** Existing CI25 and October24 Czech dark1.3 samples retain bottom-navigation ellipsis; larger-font CI matrix covers visible extremes. Programmatic accessible names and prior reachability do not prove complete visual labels or an exhaustive TalkBack configuration matrix.

Read-only source assessment: `app/src/main/java/com/opus/piano/ui/OpusApp.kt` uses NavigationBarItem label Text with maxLines=1/ellipsis and full navLabel semantics. This explains intended truncation behavior; it does not settle readability or authorize changing established tabs/navigation/appearance.

- [ ] Assess all actual tabs at narrow/representative/larger sizes, EN/CZ, both themes and fonts1.0/1.3/1.5/2.0, using the actual current NavHost and insets.
- [ ] Record full/ellipsized labels, touch areas, semantic names, selected state and real navigation/Back/focus reachability. Distinguish visual and assistive results.
- [ ] Review permitted responsive layout options without dropping navigation destinations or settings; any production change requires separately authorized scope and appropriate evidence.
- [ ] Obtain explicit Stage8 release disposition; do not mark fixed from existing accessible labels or owner Stage7 acceptance.

## A8-3 — scaffold/system-bar contrast finding

**Historical scaffold finding, NOT FIXED; product-wide impact UNVERIFIED; release disposition OPEN.** Prior CI Admin scaffold captures exposed dark-parent status contrast; representative October24 real NavHost CZ/Dark1.3 sample showed readable light icons. That sample does not prove the scaffold finding fixed or all theme/window/lifecycle combinations correct.

Read-only source assessment: MainActivity updates light status/navigation-bar flags from the outer OpusTheme background luminance; Stage7bAcceptanceTest renders some screens within a test OpusTheme/AdminTheme scaffold rather than full OpusApp navigation. Different theme/window contexts are a plausible contributor, not an established universal production defect or a resolution.

- [ ] Reproduce real MainActivity/NavHost versus test scaffold with identical device/theme/font/system-bar settings; record which surface/flags/screens differ.
- [ ] Measure/record foreground and background in the actual affected status/navigation bars, with screenshots and settings; assess against the project's applicable accessibility/design criteria without inventing a measured ratio.
- [ ] Cover system/app Light/Dark combinations, route transitions and lifecycle restoration; keep scaffold-only evidence distinct from production evidence.
- [ ] Record whether app, harness or documentation needs a separately authorized correction, or an explicit release limitation; retain original finding and owner disposition.

## Coverage that remains UNVERIFIED

Audible speech quality/pronunciation was not listened to; actual TTS installation is not proof. Subjective motion comfort and frame timing were not assessed. Full accessibility/font/keyboard/configuration coverage beyond recorded representative samples is unverified. Real three-hour/reboot notification delivery was not executed. The prior zero-animation/permission-revocation observations remain valid within their bounded scope. No physical phone is authorized or required by this preparation. X10 belongs to its existing Gould0.12 deferral and is not marked fixed or silently reclassified into these three findings.

created by stokonn with love <3
