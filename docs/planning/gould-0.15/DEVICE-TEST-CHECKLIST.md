# Gould 0.15 — owner physical-device test checklist

**Who runs this:** owner, on a real Android device. Primary device: Samsung
Galaxy A55. No emulator is required. No device result is claimed by this
checklist; enter results only after observing them.

The widget repair was separately authorized for emulator QA. The configured
API 37 and API 36 AVDs repeatedly exited with code 139 before ADB connected;
no emulator widget render or font-scale result is claimed. The owner's Galaxy
A55 remains the final visual acceptance device.

## Artifact and data-safety notes

- Current internal Release-variant testing APK was built from the current dirty
  Stage 2 source on 2026-11-15 using:
  `GRADLE_USER_HOME=/tmp/goldberg-gradle-home ./gradlew -I
  docs/releases/goldberg-0.11-stable-signing.init.gradle :app:clean
  :app:assembleRelease :app:lintRelease --no-daemon --max-workers=1
  --no-parallel` — PASS. It is for owner testing and does not authorize release
  publication.
- The expected artifact path remains
  `app/build/outputs/apk/release/app-release.apk`, package `com.opus.piano`,
  versionName `0.15.0-dev`, versionCode 17, minSdk 30, targetSdk 37,
  `debuggable=false`; ZIP integrity and APK v2 signature verification passed.
  Trusted certificate SHA-256:
  `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.
  Independently re-read APK SHA-256:
  `221340a389bf39478277d5cbf7d36966fe9596e2bfcec13b09840ef882cbcb85`;
  size 8,780,414 bytes. APK v2 verification and ZIP integrity pass; release
  manifest omits `android:debuggable`. This is not a public release.
- Before a future signed upgrade or restore test, create and verify a backup.
  If Android reports any install/signature/version error, stop: **do not
  uninstall 0.14.0, clear its data, or bypass the error.**
- Fresh-install checks are only safe when there is no existing Gould package
  in that Android user/profile, or in a separate owner-approved isolated
  profile. Never remove an existing installation to make room.
- Before any future signed upgrade or restore test, create and verify a backup
  using the app's supported export flow. Keep the original backup unchanged.

## Record the run

- Date/time:
- Device/model and Android version:
- Gould APK version, package, and SHA-256:
- Install context (fresh/separate profile/existing data):
- Locale and theme:
- Result summary and evidence location (exclude private journal contents):

## Installation and data preservation

- [ ] Fresh install completes without an existing Gould installation being
      removed or overwritten.
- [ ] App launches, initializes the database, and reaches Home without a crash.
- [ ] Export and verify a Gould 0.14.0 backup before attempting an update.
- [ ] Install the Stage 2 APK over published Gould 0.14.0 without uninstalling;
      if installation is rejected, stop and record the exact Android error.
- [ ] After a successful update, confirm the existing profile, settings,
      repertoire, statistics, badge dates, showcase and unfinished/recovery
      data remain. Record observed migration behavior; do not infer it from
      successful installation alone.
- [ ] Confirm the Room 12→13 tour-state migration preserves pre-existing
      `local_app_state` and all unrelated tables/rows.

## Backup and restore

- [ ] Export a backup containing the supported profile, activity, settings,
      cosmetic and tour state; verify export completes and can be located.
- [ ] Restore into an isolated fresh test profile and confirm the data and
      tour-progress fields round-trip.
- [ ] Import an older supported backup (formats 1–7) and confirm existing
      destination tour progress is preserved according to the compatibility
      rule.
- [ ] Confirm recordings/audio are not included in portable backups and that
      the original backup remains unchanged after restore.

## Weekly goals, activity and rewards

- [ ] Set weekly session and minutes goals. Meet sessions but miss minutes:
      week is not successful, the goal label is not met, and no successful-week
      streak/reward is earned.
- [ ] Meet minutes but miss sessions: same unsuccessful result.
- [ ] Meet both goals: week succeeds consistently in Activity, Home/Profile,
      current streak, longest streak, Freeze progress and rewards.
- [ ] Confirm goal edits take effect on the next Monday and do not rewrite the
      current week.
- [ ] Verify current and longest streaks across successful, failed, Beta Week
      and Freeze-protected weeks.
- [ ] Verify Freeze consumption on a failed eligible week and successful-week
      Freeze accrual/cap behavior.
- [ ] Verify weekly reward eligibility after both goals are met, claim once,
      and confirm a repeated refresh/restart does not grant it twice.
- [ ] Review activity history across practice sessions and manual entries;
      confirm daily streaks and weekly totals include both sources correctly.
- [ ] Add, edit, and remove an allowed recent manual entry; confirm older dates
      are rejected and widgets refresh to the new data.

## Guided tour

- [ ] First-run experience presents one tour and does not show the removed
      legacy onboarding flow.
- [ ] Visit all six topics: Home, Activity, Streaks, Profile, Collection, and
      Settings. Verify contextual navigation targets the existing app screens.
- [ ] Skip the tour and confirm it stays skipped after app restart.
- [ ] Interrupt partway, rotate/recreate the process, and resume at the correct
      topic without losing saved progress.
- [ ] Finish the tour, then replay it from Settings → About.
- [ ] Confirm screen reader semantics announce the topic heading/progress and
      controls; the scrim does not trap navigation or hide essential controls.

## Localization, appearance and accessibility

- [ ] Switch to English and Czech; verify all six tour topics and changed UI
      strings are translated and correctly formatted.
- [ ] Confirm original work/composer names and user-entered text are unchanged.
- [ ] Check Ivory/light and Nocturne/dark themes for readable text, icons,
      chart colors, dialogs, cards and widget appearance.
- [ ] Increase Android font/display size to a large setting; verify tour cards,
      forms, Activity history and Settings scroll without clipped controls.
- [ ] Navigate with TalkBack and switch access; verify labels, focus order,
      touch targets and completion/skip controls.
- [ ] Confirm motion remains understandable with system animation scales
      reduced/disabled.
- [ ] In Discover's recent-items row, compare short and long English and Czech
      titles at default and enlarged font sizes; corresponding title slots
      should align, longer titles should ellipsize after two lines, and opening
      the item should expose its full title without hiding controls.
- [ ] At narrow phone widths and enlarged font sizes, confirm the guided-tour
      skip and continue actions stack without clipping or awkwardly wrapping
      localized labels.

## Widgets and lifecycle

- [ ] Add each supported practice/activity widget and verify initial data.
- [ ] Resize the weekly widget through narrow, wide, short and tall shapes.
      Confirm constrained sizes retain the compact composition, roomy sizes
      show the summary beside the heatmap/details, weekday initials remain
      proportionate, and bars/labels stay together without stretching or a
      large gap. Repeat with a large system font scale.
- [ ] Change practice and manual activity data; confirm widgets refresh and do
      not show stale zero values on a failed refresh.
- [ ] Restart Gould and verify profile, history, streaks, rewards, tour state
      and appearance persist.
- [ ] Recreate/kill the app process during tour and during ordinary use; verify
      persisted state recovers without automatically starting microphone
      capture or practice timing.
- [ ] Confirm normal practice completion remains idempotent and per-piece
      attribution/statistics remain intact.

## Results

Leave boxes unchecked until observed. Record pass/fail/not run, app version,
device/Android version and concise evidence. A failed item must include steps to
reproduce and whether user data was affected. Do not attach private journal,
backup, recording or account data to a public issue.
