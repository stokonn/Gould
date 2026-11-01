# Gould 0.13 README screenshot capture — owner-approved emulator runbook

Status: **capture pending**; these are instructions, not screenshots or execution evidence.

## Goal

Replace the README's archived development-preview images with **real English Gould 0.13 pre-release emulator screenshots**. Do not redesign, retouch, generate, or imply that an older screenshot represents the current release. The publication remains `v0.13` **pre-release**, not Stable.

## Preparation

1. Read `AGENTS.md`, `docs/ai/HANDOFF.md`, `docs/planning/gould-0.13/RELEASE-READINESS.md`, `docs/PRIVACY.md`, and `README.md`.
2. Use an **independent disposable emulator** (Android API 36 preferred) with a recorded explicit serial matching `emulator-NNNN`. Never select a physical device; pass `-s "$SERIAL"` on every ADB command. Inspect `adb devices -l`, `getprop ro.kernel.qemu` and `ro.build.version.sdk` before any installation or state change.
3. Install the **official published `Gould-v0.13.apk`** into that fresh emulator, not a main debug build. Verify exact APK SHA-256 against `Gould-v0.13.apk.sha256`, signer against the accepted release identity, and package `com.opus.piano`, `versionName=0.13`, `versionCode=15`. Do not overwrite a mismatched or non-disposable installation. If the release artifact cannot be downloaded/verified, stop and report.
4. Use only **synthetic** data: illustrative public-domain classical pieces, plausible practice sessions and badges earned from their actual conditions. No real profile photo, login, private practice journal, microphone audio, support mailbox, token, account, notification content or personal identity. Recording UI may be shown only if a fixture take is safely generated in the disposable emulator; never fake playback.
5. Set application language to **English**, with normal font/display scale, and theme to Ivory or Nocturne as appropriate. No debug overlays, keyboard, popup, toast, error state or clipped UI. Do not enable network-dependent features solely for screenshots.

## Required images (all verified against the installed 0.13 binary)

Save PNG images under `docs/wiki/images/gould-0.13/`:
- `home-en-ivory.png`
- `repertoire-en-ivory.png`
- `practice-en-nocturne.png` — show actual session state only if safe; otherwise use non-running Studio screen
- `profile-en-ivory.png`
- `achievements-en-nocturne.png`
- `studio-previous-en-nocturne.png` — only where the user-visible saved-take state is actually reproducible
- Optional: `home-en-nocturne.png`, `settings-en-ivory.png`, `widget-quick-practice-en.png`, `widget-weekly-activity-en.png`.

Use a consistent portrait resolution and clean status bar. Sample command (replace the serial after verifying it is an emulator):

```sh
SERIAL=emulator-5554
adb -s "$SERIAL" shell getprop ro.kernel.qemu  # must return 1
adb -s "$SERIAL" shell dumpsys package com.opus.piano | grep -E 'versionCode=|versionName='
adb -s "$SERIAL" exec-out screencap -p > docs/wiki/images/gould-0.13/home-en-ivory.png
```

For each image, inspect it **visually** and record: filename, installed APK SHA-256, screenshot SHA-256, emulator serial/API, locale/theme, capture source and whether data is synthetic. Use a separate concise `CAPTURE-EVIDENCE.md` in the same screenshot directory; do not write personal IDs or secrets.

## README update gate

Only **after** verified PNG files have been committed:
- Replace the README `A look inside` images with an English three-column Home / Practice Studio / Profile table, linking to the new files.
- Replace its development-preview disclaimer with `Screenshots captured from Gould 0.13 pre-release on an Android emulator (API XX), using fictional demo data.` when proven.
- Keep older screenshots and all old verification evidence. Do not rename older files as newer captures.
- Add optional screenshots elsewhere only when individually verified.
- Verify Markdown image paths actually exist; preserve accurate release classification and installation warnings.

## QA and publication

Run a narrowly scoped README image-path check, a sensitive-data review, image dimensions/hash audit and visual accessibility review. No application source/build/settings/database changes should be needed. Capture and README update should be a separate small commit from the backend architecture decision.

Stop if unable to verify the APK, launch the emulator, reproduce an exact screen or establish screenshot provenance. **Never create AI-generated screenshots as a substitute.**
