# Stage 7B review instructions and evidence limits

Read OWNER-REVIEW.md first. Stage7B acceptance is pending; no production activation or release is offered.

## Review sequence

1. Check the tested commit and CI source-commit.txt; later documentation-only commits do not change the executable source. Never substitute prior Home/onboarding smoke for Admin renders.
2. Review small/standard dashboard renders at font1.0/1.3, both EN/CZ and Light/Dark parent themes. There must be four statistic tiles, three destinations and reachable refresh without scrolling. Admin itself intentionally stays dark. Compare the approved original dashboard and nine schematic references; do not copy ChatGPT chrome or invented counts.
3. Inspect Queue, Proposal, Audit and Settings renders, all three confirmation states and guided re-review. Fixtures are clearly synthetic. The comparison must explicitly say current catalogue values unavailable; old proposal text is not a catalogue baseline. Missing revision2 in the re-review fixture must be disclosed.
4. Inspect font1.5/2.0: scroll fallback is allowed; controls must remain reachable. Static screenshots and exported accessibility trees do not establish TalkBack announcement order, touch exploration, keyboard navigation, complete scroll reachability or full real OpusApp navigation. Keep those matrix items open unless separately executed.
5. Review loading/empty/error states. A captured indeterminate animation frame does not prove the complete lifecycle. Check runtime logs/domain assertions for cancellation, retry and stale-response safety.
6. Read the upgrade result separately from fixture UI. The approved CI procedure initializes immutable v0.10.1 schema8 with synthetic personal records, installs the candidate in place using the same runner debug key, compares persistent tables/preferences and schema10 integrity. The trusted released APK digest/certificate is checked independently. The CI rebuild is not the trusted release binary and cannot prove release-signed installation. Baseline v0.10.1 has no Stage7B Admin data; native store recreation is separate limited persistence evidence.
7. Read controlled WorkManager tests as scheduling/constraint/cancel/early-guard evidence only. No real three-hour wait, reboot delivery, successful changed-scan notice or live backend is implied. Permission grant/denial/revocation, metered runtime transitions and positive notification delivery require their own observed assertions.
8. Review G1–G9/G11 dependencies in CONTRACT-AUDIT.md. bcc7796 stays the pinned corpus authority; Stage6 M2/M3 progress does not freeze public HTTP/session/current-value integration. Keep network and decisions disabled.

## Smallest remaining actions by dependency

- Local host emulator: changed renderer still SIGSEGV before boot. Use existing verified CI KVM venue; never retry the same failed local configuration unchanged or use a phone.
- Render/accessibility failures: retain exact failing screenshots/trees; fix only demonstrated approved-scope defects, rerun affected checks and bind final runtime to the new source.
- Missing accessibility/manual interaction evidence: test the actual final Admin screens with TalkBack, keyboard/insets, scroll reachability and supported reduced-motion settings on an identified emulator; retain results without claiming screenshot-only proof.
- Positive sync/delivery: use an explicitly controlled test venue that can inject a complete authorized queue and change/revocation/lifecycle events into the existing worker; verify notifications under platform permission transitions. Never enable production transport to clear this gate.
- Upgrade failure/setup blocker: retain raw signer/installer/upgrade diagnostics; fix only the demonstrated infrastructure or migration defect. Never uninstall to bypass a mismatch or fabricate a baseline.
- Live integration: Stage6/owner must freeze approved versioned public DTO/session/principal/dual-revision/current-value/audit/validation rules. Then authorize and verify one adapter locally. No cloud or real-account activation follows from local success.
- Owner acceptance: review this complete evidence packet and explicitly accept the documented final scope/limitations. No request to approve unverified behavior is implied.

Stage7A remains FINAL ACCEPTED; X10 NOT FIXED/NOT VERIFIED, deferred to Gould0.12. Stage8 readability/accessibility/touch-target gates remain required.
