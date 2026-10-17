# Google profile identity — configuration and verification

The owner supplied the public Web client ID `355619842126-042mfov62elelsi87maol41oe8pva3el.apps.googleusercontent.com` and configured an Android OAuth client for `com.opus.piano`. Credential Manager and Google ID are integrated for optional local profile identity. No OAuth client secret, token persistence, cloud backup or practice synchronization is introduced. For the 0.10 candidate, the owner reports successful sign-in, display-name/avatar import after consent, preservation of manual nickname/avatar overrides, restart persistence, sign-out/account switching and local practice-data preservation on a Samsung Galaxy A55. This is owner-reported manual evidence, not Codex testing. The Google Cloud Console settings and signing registration were not independently inspected.

## OAuth configuration notes

1. The owner has supplied the public Web OAuth client ID and reports creating the Android OAuth client for `com.opus.piano`; successful owner-reported 0.10 sign-in is documented above. The owner should maintain the consent screen audience/test-user configuration in their Google Cloud project.
2. The intended Android signing SHA-1 is `4F:BB:35:7F:14:74:0D:B3:6F:CE:48:67:27:50:75:57:D4:E3:C4:20`. The trusted certificate SHA-256 is `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`. The final APK fingerprint is checked locally; Cloud Console registration cannot be independently inspected here. Never rotate the key or uninstall to work around OAuth/signature problems.
3. The Web client ID is public and configured in the Android Credential Manager Google ID-token request. Never put a client secret in the APK or Git.
4. Do not introduce Firebase/cloud synchronization solely for profile identity. Where token authenticity must be trusted beyond local UI, validate it with a trusted backend; decoding a JWT is not verification.

## Required behavior for integration

- Optional sign-in only; cancellation/failure leaves all local data unchanged. The app is fully usable signed out.
- After a valid result, ask before replacing an existing manually chosen avatar or nickname. Import the account's display name/avatar as identity defaults; keep explicit manual overrides separate. No persistent connected-status card on Profile.
- Settings exposes account management/sign-out. Sign-out clears the credential session/account binding, not the local journal, manually chosen nickname/avatar, favorites or biography. Account switches must not overwrite existing manual choices without confirmation.
- Download a Google avatar only with network/media consent; never treat sign-in as metered-image consent. Do not log tokens, account emails or identifiers unnecessarily.
- Codex's emulator flow reached Google account entry but was not authenticated because the emulator had no test account configured; no credentials were entered. Do not call this an authentication pass. Separately, the six owner-observed 0.10 physical-device results listed above are `PASS — owner-reported manual verification`; they were not run by Codex.

Official implementation references (review again when wiring the real client):
- [Credential Manager and Sign in with Google](https://developer.android.com/identity/sign-in/credential-manager-siwg)
- [Verify Google ID tokens](https://developers.google.com/identity/gsi/web/guides/verify-google-id-token)

Codex must not test on the owner’s phone. Successful real authentication is user-reported manual verification. On 2026-10-17 the owner individually confirmed display-name/avatar import, nickname preservation, authenticated restart, sign-out/local data preservation and account switching as PASS — owner-reported manual verification. See MANUAL-VERIFICATION-0.9.2.md.

## Implemented local behavior

The sign-in button requests a Google credential explicitly; startup never launches authentication. A random nonce accompanies each request. Credential Manager returns the Google provider credential, which is parsed with Google ID. The token is not logged, stored or used to authorize any remote Goldberg service. Any future protected backend must verify it server-side.

A legacy non-default nickname is treated as manually chosen. Explicit nickname edits always override later Google updates. An existing local avatar is preserved; an empty avatar can import the account picture only from HTTPS googleusercontent.com with media consent, bounded download/decode and Room persistence. Removing/selecting an avatar prevents later automatic replacement. Imported avatars remain local after sign-out. An unavailable picture does not invalidate sign-in.

Settings offers sign-out; sign out then sign in to choose another account. Provider-state clearing must succeed before the local account binding is removed. Restart restores the local identity choice without claiming renewed online authentication. Google identity/device consent is excluded from portable journal backups and Android backup is disabled.

## Signing registration

The published 0.9.2 APK and Goldberg 0.10 candidate use the same compatible certificate under the repository signing configuration. Register SHA-1 `4F:BB:35:7F:14:74:0D:B3:6F:CE:48:67:27:50:75:57:D4:E3:C4:20` for package `com.opus.piano` in the same Cloud project as the Web client. No additional Android client is needed unless a future release uses a different certificate; register that actual certificate before distributing it. The app does not have access to verify your Google Cloud Console registrations.


## Avatar decoder regression

An emulator regression found and corrected a zero sample-size division before image decode. The import uses the tested bounded decoder; this local bitmap test does not verify Google-hosted image retrieval or an authenticated account's avatar. The owner subsequently confirmed authenticated avatar import after media consent; that manual result remains separate from the decoder test.
