# Goldberg signing and upgrade gate

Keep application ID com.opus.piano and database opus.db. Never uninstall or clear existing app data to bypass signing mismatch.

Actual published v0.7.0 asset Goldberg_v0.7.0.apk was downloaded from stokonn/Opus for this implementation audit. Its package metadata is 0.7.0/code 5, min API 26, target API 36. Its certificate SHA-256 is:

53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e

The available build signing identity currently produces that same fingerprint. Older 0.7 development documents described a different host key and are historical, not authoritative for the published artifact. Always compare actual APKs; build OS, file names and package names do not establish upgrade compatibility.

Before distribution compare the final candidate with authentic published 0.6.1 and 0.7 artifacts using scripts/verify-upgrade-signing.sh, verify signatures/package/version/code/checksum, then test an in-place upgrade on a dedicated populated test installation. Certificate equality alone does not prove runtime data preservation.

Future builds must explicitly retain the verified signing identity. Keep the existing compatible private key outside Git and back it up encrypted in independent locations. Do not print passwords, embed credentials in scripts, generate a replacement release identity or rotate certificates without a separately approved migration. The explicitly authorized 0.8 beta waives runtime verification as a publication gate only; missing automated checks or signing compatibility still block publication. Runtime upgrades/data preservation remain publicly unverified. No key was generated, replaced or committed by this work.
