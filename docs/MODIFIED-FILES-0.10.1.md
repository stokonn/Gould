# Goldberg 0.10.1 modified files

Compared with verified starting main 6bde1ad. Implementation commit: 56851cb5da30d9e823f6374562f7773e96a35e46; final privacy documentation follows in a separate documentation commit.

Architecture: existing application-scoped PracticeCoordinator and repositories reused. Completion publishes a saved session ID after persistence; historical edits reuse untimed links and existing note storage. No schema, backup format, database identity, application identity or signing change. UI extends existing Compose/theme components.

- .gitignore
- CHANGELOG.md
- README.md
- app/build.gradle.kts
- app/src/main/assets/legal/privacy.md
- app/src/main/java/com/opus/piano/data/JournalEdits.kt
- app/src/main/java/com/opus/piano/practice/PracticeCoordinator.kt
- app/src/main/java/com/opus/piano/ui/BalancedHome.kt
- app/src/main/java/com/opus/piano/ui/CustomizePanel.kt
- app/src/main/java/com/opus/piano/ui/LegalScreen.kt
- app/src/main/java/com/opus/piano/ui/OpusApp.kt
- app/src/main/java/com/opus/piano/ui/PracticeReviewScreen.kt
- app/src/main/java/com/opus/piano/ui/PracticeScreen.kt
- app/src/main/java/com/opus/piano/ui/ProfileScreen.kt
- app/src/main/java/com/opus/piano/ui/RepertoireScreen.kt
- app/src/main/java/com/opus/piano/ui/SettingsScreen.kt
- app/src/main/java/com/opus/piano/ui/TuxScreen.kt
- app/src/main/res/values-cs/strings.xml
- app/src/main/res/values/strings.xml
- app/src/test/java/com/opus/piano/Polish101Test.kt
- app/src/test/java/com/opus/piano/PracticeFailureTest.kt
- docs/BRANCH-CLEANUP-0.10.1.md
- docs/GITHUB-TRACKING-0.10.1.json
- docs/LEGAL-AUDIT-0.10.1.md
- docs/MODIFIED-FILES-0.10.1.md
- docs/NAMING-AUDIT-0.10.1.md
- docs/PLAN-0.10.1.md
- docs/PRIVACY.md
- docs/RELEASE-0.10.1.md
- docs/ROADMAP-0.11-DATABASE.md
- docs/TESTED-SOURCE-0.10.1.json
- docs/VERIFICATION-0.10.1.md
- docs/design/goldberg-0.10.1/APPROVAL_MATRIX.md
- docs/design/goldberg-0.10.1/CODEX_TASK.md
- docs/design/goldberg-0.10.1/README.md
- docs/design/goldberg-0.10.1/manifest.json
- docs/wiki/Changelog-and-Roadmap.md
- docs/wiki/Home.md
- docs/wiki/Settings-and-Customization.md
- docs/wiki/User-Guide.md
- docs/wiki/Verification-0.10.1.md
- docs/wiki/Verification.md
- docs/wiki/images/goldberg-0.10.1-about.png
- docs/wiki/images/goldberg-0.10.1-appearance.png
- docs/wiki/images/goldberg-0.10.1-profile.png
- docs/wiki/images/goldberg-0.10.1-tux.png
- scripts/emulator_ui.py
- scripts/verify_emulator_upgrade.py
