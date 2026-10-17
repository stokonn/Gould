# Google profile identity — configuration and verification

The owner supplied the real public Web client ID `355619842126-042mfov62elelsi87maol41oe8pva3el.apps.googleusercontent.com` and reports an Android client registered for `com.opus.piano`. Credential Manager and Google ID are integrated for optional local profile identity. No OAuth secret, token persistence, cloud backup or practice synchronization is introduced. The owner now reports successful real Google authentication on a Samsung Galaxy A55 following an in-place install. This is user-reported manual evidence, not testing performed by Codex. Cloud Console settings were not independently inspected.

## Configuration the owner must provide

1. Create/select a Google Cloud project and configure its OAuth consent screen, application name, support email, authorized domain/privacy information, audience and test users as applicable. Enable Google account identity for the intended audience.
2. Create an Android OAuth client for package `com.opus.piano`. Register the SHA-1 certificate fingerprint of the compatible signing key: `4F:BB:35:7F:14:74:0D:B3:6F:CE:48:67:27:50:75:57:D4:E3:C4:20`. Verify it independently with apksigner against the actual APK. The trusted SHA-256 is `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`. Never create/rotate a key or uninstall an existing app to work around OAuth/signature problems.
3. Create the Web OAuth client ID required by Android Credential Manager's Sign in with Google ID-token request. Supply the real public server/client ID through an appropriate build configuration. A client ID is public; a client secret must never enter the APK or Git.
4. Confirm that the selected Android and Web clients belong to the intended project. Configure test accounts and publish consent appropriately when ready. Supply the real client ID, project identity and authorized signing confirmation to implementation.
5. Complete integration with the supported Android Credential Manager + Google ID libraries, inspect the returned Google credential type and reject malformed responses. Do not introduce Firebase/cloud synchronization solely for profile identity. Where token authenticity must be trusted beyond local UI, validate the token with a trusted backend; decoding a JWT is not verification.

## Required behavior for integration

- Optional sign-in only; cancellation/failure leaves all local data unchanged. The app is fully usable signed out.
- After a valid result, ask before replacing an existing manually chosen avatar or nickname. Import the account's display name/avatar as identity defaults; keep explicit manual overrides separate. No persistent connected-status card on Profile.
- Settings exposes account management/sign-out. Sign-out clears the credential session/account binding, not the local journal, manually chosen nickname/avatar, favorites or biography. Account switches must not overwrite existing manual choices without confirmation.
- Download a Google avatar only with network/media consent; never treat sign-in as metered-image consent. Do not log tokens, account emails or identifiers unnecessarily.
- Verify sign-in, cancellation, failure, sign-out, avatar/name defaults, manual overrides, restart, account switching and signed-out operation on an emulator with Google Play services after real client setup. Only individually observed provider behaviors may be reported as passed; current evidence is separated in MANUAL-VERIFICATION-0.9.2.md.

Official implementation references (review again when wiring the real client):
- [Credential Manager and Sign in with Google](https://developer.android.com/identity/sign-in/credential-manager-siwg)
- [Verify Google ID tokens](https://developers.google.com/identity/gsi/web/guides/verify-google-id-token)

Codex must not test on the owner’s phone. Successful real authentication is user-reported manual verification. On 2026-10-17 the owner individually confirmed display-name/avatar import, nickname preservation, authenticated restart, sign-out/local data preservation and account switching as PASS — owner-reported manual verification. See MANUAL-VERIFICATION-0.9.2.md.

## Implemented local behavior

The sign-in button requests a Google credential explicitly; startup never launches authentication. A random nonce accompanies each request. Credential Manager returns the Google provider credential, which is parsed with Google ID. The token is not logged, stored or used to authorize any remote Goldberg service. Any future protected backend must verify it server-side.

A legacy non-default nickname is treated as manually chosen. Explicit nickname edits always override later Google updates. An existing local avatar is preserved; an empty avatar can import the account picture only from HTTPS googleusercontent.com with media consent, bounded download/decode and Room persistence. Removing/selecting an avatar prevents later automatic replacement. Imported avatars remain local after sign-out. An unavailable picture does not invalidate sign-in.

Settings offers sign-out; sign out then sign in to choose another account. Provider-state clearing must succeed before the local account binding is removed. Restart restores the local identity choice without claiming renewed online authentication. Google identity/device consent is excluded from portable journal backups and Android backup is disabled.

## Signing registration

Both the existing published 0.9.1 APK and the local debug candidate use the same compatible certificate when built with the current signing configuration. Register SHA-1 `4F:BB:35:7F:14:74:0D:B3:6F:CE:48:67:27:50:75:57:D4:E3:C4:20` for package `com.opus.piano` in the same Cloud project as the Web client. No additional Android client is needed unless a future release uses a different certificate; register that actual certificate before distributing it. The app does not have access to verify your Google Cloud Console registrations.


## Avatar decoder regression

An emulator regression found and corrected a zero sample-size division before image decode. The import uses the tested bounded decoder; this local bitmap test does not verify Google-hosted image retrieval or an authenticated account's avatar. The owner subsequently confirmed authenticated avatar import after media consent; that manual result remains separate from the decoder test.
