# Goldberg 0.11 — verified Stable release

[Goldberg 0.11 Stable](https://github.com/stokonn/Goldberg/releases/tag/v0.11) is a signed, non-debuggable com.opus.piano APK,version0.11/code13. The annotatedv0.11tag points to ab7d66d; its195application/build inputs match the accepted a7bb2aa source. Main history is preserved.

APK SHA256: `290a15e7b15da701b8f0a40e98bf8b7c1f7cc00ce487504dcb6d4ab2e11b40d6`. Trusted signer: `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`. GitHub asset digest and re-downloaded APK bytes match. Signed release lint/package/version/ZIP/notice/resource checks pass.

348JVM passed;quality40491886948 passed. Initial full API36 run217cases had204passes,2subsequently corrected failures and11documented external/probe skips. Focused40491890436 then passed all6affected cases;200unaffected native passes are reused. This is not a claim of a new217case run. Seeded0.10.1schema8→10 offline/network/restart acceptance preserves every original row/preference and37sessions,with integrity/FK pass. CI uses disposable debug rebuilds;release certificate compatibility is checked independently.

Nine actual final frames were inspected across both themes,EN/CZ,standard/small and1x/2x. Three sharedLibraryframes are component renders. Small2x primary action requires scrolling;latercreditsframes show it fully. Navigation labels retain existing wrapping. No physical-phone or newAPI26 execution is claimed.

[Detailed evidence and limitations](https://github.com/stokonn/Goldberg/blob/main/docs/releases/GOLDBERG-0.11.md). Public catalogue API10/10security checks passed once;its projection remains empty. BackendOAuth/community writes/private-data uploads stay disabled.

created by stokonn with love <3

<details><summary>Preserved earlier verification history</summary>

# Current verification

See [Goldberg 0.10.1 verification](Verification-0.10.1) for the current release. Prior results below remain historical evidence.

# Verification — Goldberg 0.9.2

Application acceptance review passes. The normal [Goldberg 0.9.2 release](https://github.com/stokonn/Goldberg/releases/tag/v0.9.2) uses the verified compatible APK. A build alone was not used as release acceptance.

## Owner-reported manual Google results

On 2026-10-17, the owner personally tested the latest installed build on a Samsung Galaxy A55 and individually confirmed these scenarios. **Codex did not perform the physical-device tests. No exact installed APK digest or device logs were supplied.** Do not infer unrelated authentication, networking, migration or UI results.

| Scenario | Result | Reported observation |
| --- | --- | --- |
| Google display-name import | PASS — owner-reported manual verification | Account name retrieved and displayed |
| Google profile-picture import | PASS — owner-reported manual verification | Imported after required media consent |
| Manual nickname preservation | PASS — owner-reported manual verification | Customized nickname not overwritten |
| Authentication persistence | PASS — owner-reported manual verification | Signed-in state retained after full close/reopen |
| Sign-out and local data preservation | PASS — owner-reported manual verification | Sign-out worked; existing local data remained intact |
| Google account switching | PASS — owner-reported manual verification | Another account selected without losing existing local data |

The owner previously reported successful real sign-in and an in-place installation. These statements are not automated tests and are not emulator results.

## Automated and emulator evidence

- 70 JVM tests and 31 actually executed Android instrumentation tests passed on the unchanged final application source.
- Host migrations (schemas 1–6 into 7), historical backup formats, history paging, 755 CZ/EN resources and canonical agent-policy/Wiki checks pass.
- Lint: zero errors, 57 reviewed warnings; warnings are not claimed resolved.
- Seeded 0.9.1-to-0.9.2 emulator upgrade retained the tested 37 sessions, two pieces, draft, timing/parts/intervals, profile and existing badge date.
- Actual rendered screens were inspected in light/dark, CZ/EN and representative 360dp/130% font layouts. Keyboard focus in Practice/review/history/recording editors passes without duplicate inset space.
- Cache/consent/failed-download/quote-offline fixtures pass. Live Wi-Fi, offline cached content, cellular denial without consent and cellular completion after consent were separately checked.
- Final offline Google provider shows its network error; Back returns with localized cancellation and unchanged local test data. Offline catalogue fallback and online retry pass.

The new avatar regression initially exposed division by zero, then passed after sample-size initialization. Actual small/large Android bitmap decoding is separate from the owner's authenticated picture-import result. Home correctly shows recorded Free Practice when no recent repertoire piece exists.

Screenshots are actual emulator captures with controlled test data. Original design mockups were unavailable; inspection used the written approved specification. The full screen/theme/font/hardware cross-product, audible keyboard playback and physical-device Smart Practice screen-off/battery behavior are not certified. Claude/Gemini/Cursor/Copilot runtime loading was not tested; instruction syntax/references were checked. The permanent Project remains private to authorized accounts; Board layout/Status grouping are verified.

## Artifact

Package com.opus.piano; version 0.9.2; code 9. Archive, supplied icon resources and actual signing compatibility against the trusted published 0.9.1 APK pass. The existing compatible Android Debug certificate is retained, not a new production signing identity.

APK SHA-256: `7470fe7b477678946040df452813fc47708489883543bb3ea57bb2148bab4e7f`.

Signing SHA-256: `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.

[Home](Home) · [Developer Guide](Developer-Guide) · [Settings](Settings-and-Customization)

## Goldberg 0.10

See [Goldberg 0.10 Verification](Verification-0.10) for the release build, automated checks, screenshots and owner-reported Google results. The 0.9.2 verification above remains historical and release-specific.

</details>
