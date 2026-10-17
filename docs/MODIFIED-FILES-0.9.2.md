# Goldberg 0.9.2 development files

Local implementation inventory; unrelated RELEASE_NOTES.md is excluded and preserved.

- `.cursor/rules/goldberg.mdc`
- `.github/copilot-instructions.md`
- `.github/workflows/quality.yml`
- `AGENTS.md`
- `CHANGELOG.md`
- `CLAUDE.md`
- `GEMINI.md`
- `README.md`
- `app/build.gradle.kts`
- `app/schemas/com.opus.piano.data.OpusDatabase/7.json`
- `app/src/androidTest/assets/backups/goldberg-0.9.1-format2.json`
- `app/src/androidTest/java/com/opus/piano/AppLanguageTest.kt`
- `app/src/androidTest/java/com/opus/piano/CatalogDatabaseTest.kt`
- `app/src/androidTest/java/com/opus/piano/ExperiencePersistenceTest.kt`
- `app/src/androidTest/java/com/opus/piano/Goldberg08MigrationTest.kt`
- `app/src/androidTest/java/com/opus/piano/GoldbergPersistenceTest.kt`
- `app/src/androidTest/java/com/opus/piano/GoogleIdentityPersistenceTest.kt`
- `app/src/androidTest/java/com/opus/piano/NavigationRegressionTest.kt`
- `app/src/androidTest/java/com/opus/piano/PracticeRecoveryTest.kt`
- `app/src/androidTest/java/com/opus/piano/Quality092PersistenceTest.kt`
- `app/src/androidTest/java/com/opus/piano/SmartCachePersistenceTest.kt`
- `app/src/main/assets/legal/third-party-notices.md`
- `app/src/main/java/com/opus/piano/OpusApplication.kt`
- `app/src/main/java/com/opus/piano/data/BackupRepository.kt`
- `app/src/main/java/com/opus/piano/data/CatalogRepository.kt`
- `app/src/main/java/com/opus/piano/data/DailyQuotes.kt`
- `app/src/main/java/com/opus/piano/data/OpusDatabase.kt`
- `app/src/main/java/com/opus/piano/data/PersonalData.kt`
- `app/src/main/java/com/opus/piano/data/PracticeData.kt`
- `app/src/main/java/com/opus/piano/data/PracticeRepository.kt`
- `app/src/main/java/com/opus/piano/data/WikimediaProfile.kt`
- `app/src/main/java/com/opus/piano/identity/GoogleIdentity.kt`
- `app/src/main/java/com/opus/piano/practice/PracticeCoordinator.kt`
- `app/src/main/java/com/opus/piano/practice/PracticePersistence.kt`
- `app/src/main/java/com/opus/piano/preferences/MediaPreferences.kt`
- `app/src/main/java/com/opus/piano/ui/BadgesScreens.kt`
- `app/src/main/java/com/opus/piano/ui/BalancedHome.kt`
- `app/src/main/java/com/opus/piano/ui/CatalogScreens.kt`
- `app/src/main/java/com/opus/piano/ui/CatalogViewModel.kt`
- `app/src/main/java/com/opus/piano/ui/ComposerPortrait.kt`
- `app/src/main/java/com/opus/piano/ui/ComposerScreen.kt`
- `app/src/main/java/com/opus/piano/ui/CompositionScreen.kt`
- `app/src/main/java/com/opus/piano/ui/CustomizePanel.kt`
- `app/src/main/java/com/opus/piano/ui/EditorialComponents.kt`
- `app/src/main/java/com/opus/piano/ui/HarpsichordScreen.kt`
- `app/src/main/java/com/opus/piano/ui/JournalComponents.kt`
- `app/src/main/java/com/opus/piano/ui/LegalScreen.kt`
- `app/src/main/java/com/opus/piano/ui/OnboardingScreen.kt`
- `app/src/main/java/com/opus/piano/ui/OpusApp.kt`
- `app/src/main/java/com/opus/piano/ui/PracticeHistoryScreen.kt`
- `app/src/main/java/com/opus/piano/ui/PracticeReviewScreen.kt`
- `app/src/main/java/com/opus/piano/ui/PracticeScreen.kt`
- `app/src/main/java/com/opus/piano/ui/ProfileScreen.kt`
- `app/src/main/java/com/opus/piano/ui/ProfileViewModel.kt`
- `app/src/main/java/com/opus/piano/ui/RecordingsScreen.kt`
- `app/src/main/java/com/opus/piano/ui/RepertoireScreen.kt`
- `app/src/main/java/com/opus/piano/ui/SettingsScreen.kt`
- `app/src/main/java/com/opus/piano/ui/Theme.kt`
- `app/src/main/java/com/opus/piano/ui/TuxScreen.kt`
- `app/src/main/res/values-cs/strings.xml`
- `app/src/main/res/values/strings.xml`
- `app/src/test/java/com/opus/piano/GoogleIdentityPolicyTest.kt`
- `app/src/test/java/com/opus/piano/PracticeEngineTest.kt`
- `app/src/test/java/com/opus/piano/PracticeFailureTest.kt`
- `app/src/test/java/com/opus/piano/Quality092Test.kt`
- `docs/GITHUB-TRACKING-0.9.2.json`
- `docs/GOOGLE-IDENTITY-SETUP.md`
- `docs/MODIFIED-FILES-0.9.2.md`
- `docs/PLAN-0.9.2.md`
- `docs/QUOTATIONS.md`
- `docs/VERIFICATION-0.9.2.md`
- `docs/wiki/Changelog-and-Roadmap.md`
- `docs/wiki/Credits-and-Licenses.md`
- `docs/wiki/Developer-Guide.md`
- `docs/wiki/FAQ-and-Troubleshooting.md`
- `docs/wiki/Getting-Started.md`
- `docs/wiki/Home.md`
- `docs/wiki/Settings-and-Customization.md`
- `docs/wiki/User-Guide.md`
- `docs/wiki/_Sidebar.md`
- `docs/wiki/images/bach-keyboard-development.png`
- `docs/wiki/images/home-development.png`
- `docs/wiki/images/profile-czech-development.png`
- `docs/wiki/images/tux-development.png`
- `scripts/check_agent_policy.py`
- `scripts/check_backup_fixtures.py`
- `scripts/check_migrations.py`
- `scripts/verify_quality_apk.py`

## Retained resumption evidence and tooling

- `docs/MANUAL-VERIFICATION-0.9.2.md`: owner-reported sign-in and individually unverified Google behaviors.
- `docs/wiki/Verification.md`: published verification matrix and current candidate status.
- `docs/verification/0.9.2/`: final logs, the deliberately failing avatar regression, screenshots actually inspected, candidate/source hashes and Project-view API evidence; see its README.
- `scripts/emulator_ui.py`: explicit emulator-only UI capture/focus/long-press helper; rejects physical-device serials.
- `scripts/measure-startup.sh`: emulator-only startup measurement guard.

During resumption, application-code edits were limited to Home's fallback for existing Free Practice history and extraction/correction of bounded Google-avatar decoding, plus the Android regression test. The existing IME fix was inspected and verified without further inset changes. Room schema, migration and backup implementation were not modified again during resumption.

Final owner-verification/release review updates only documentation and retained evidence: all six Google scenarios are now PASS — owner-reported manual verification; docs/RELEASE-0.9.2.md contains the release notes and limitations. No application code changed after the 70-JVM/31-Android final run.
