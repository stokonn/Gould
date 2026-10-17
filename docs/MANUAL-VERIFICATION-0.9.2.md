# Goldberg 0.9.2 — user-reported manual verification

Sources: the owner's explicit reports in the development conversation on 2026-10-16 (installation/sign-in) and 2026-10-17 (six additional scenarios). This is user-reported evidence, not a Codex observation or physical-device test performed by Codex.

The owner reports installing Goldberg 0.9.2 on a Samsung Galaxy A55 using `adb install -r` and successfully authenticating with Google Sign-In. No APK digest, screenshot, account identifier or device log was supplied with the report; do not invent that evidence or infer additional tested behavior.

| Check | Status | Evidence |
| --- | --- | --- |
| In-place APK installation command completed | User-reported success | Owner reports successful `adb install -r` |
| Real Google authentication | User-reported manual PASS | Owner explicitly confirms authentication succeeded |
| Google display-name import | PASS — owner-reported manual verification | Google account name retrieved and displayed |
| Google profile-picture import | PASS — owner-reported manual verification | Imported after required media download consent |
| Manual nickname preservation | PASS — owner-reported manual verification | Customized nickname not overwritten by Google data |
| Authentication persistence | PASS — owner-reported manual verification | Account remained signed in after fully closing/reopening |
| Sign-out and local data preservation | PASS — owner-reported manual verification | Sign-out worked; existing local data remained intact |
| Google account switching | PASS — owner-reported manual verification | Another account selected without losing existing local data |
| Actual user-data retention after phone upgrade | UNVERIFIED | Installation success does not establish data retention; seeded emulator upgrade has separate evidence |

Codex must not interact with the owner's phone to fill these gaps. Do not broaden the successful-authentication claim to other account or upgrade behaviors. The six explicitly reported Google scenarios now satisfy their manual acceptance checks. Do not infer unrelated authentication, networking, migration or UI results. Review all other release gates independently before publication. Host/emulator evidence is recorded separately in VERIFICATION-0.9.2.md.
