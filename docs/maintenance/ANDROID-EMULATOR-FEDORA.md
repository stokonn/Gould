# Fedora Android Emulator Recovery

Updated: 2026-11-13. This record covers local API 37 runtime verification for
Gould 0.14. It describes host evidence and an emulator-only workaround; it does
not change Fedora security policy, install packages, or touch personal AVD data.

## Diagnosis

The two failing QEMU processes were recorded by `systemd-coredump`:

| PID | Time (CET) | Emulator | Result |
|---:|---|---|---|
| 28556 | 2026-11-13 19:21:18 | `gould014-api37-clean-1112`, `-gpu swiftshader_indirect` | QEMU `RenderThread`, SIGSEGV / `SEGV_ACCERR`, core 303.3 MiB |
| 29776 | 2026-11-13 19:28:03 | same AVD and renderer, read-only/no-snapshot | QEMU `RenderThread`, SIGSEGV / `SEGV_ACCERR`, core 279.4 MiB |

For both crashes, Fedora's audit journal recorded SELinux `execheap` AVC
denials against that `RenderThread` immediately before SIGSEGV. SELinux was
Enforcing during the successful recovery run as well. This ties exit 139 to the
host renderer's executable-heap path; it is not evidence of an application or
instrumentation crash. The core's stack was stripped and only resolved to the
faulting thread, so no deeper source symbol is claimed.

The retained host cores are in `/var/lib/systemd/coredump/` and are indexed by
`coredumpctl info 28556 29776`; the audit/journal evidence is read with
`journalctl --since '2026-11-13 19:20:00' --until '2026-11-13 19:30:00'`.
`/tmp/gould014-emulator.log` contains the failing launch's final emulator
startup lines (the fault occurred in the host QEMU process, so Android guest
tombstones are not the crash record).

There was a separate AVD defect: `/home/stokonn/.android/avd/`
`gould014-api37-clean-1112.ini` contained `target=android-0`, although its
`config.ini` points to Android 37.0 Google Play. This AVD was created by older
cmdline-tools 12.0, which mishandled the dotted API level. Existing investigation in
the Gould 0.14 checklist reproduced that behavior and confirmed that
cmdline-tools 23.0 generates `target=android-37.0`; the mismatched AVD was kept
untouched. The sibling `gould014-api37-gapi` AVD has matching `.ini` and
`config.ini` targets (`android-37.0`) and uses the API 37 Google APIs x86_64
image.

## Host and SDK facts

- Fedora 45, kernel `7.3.3-300.fc45.x86_64`; Intel Core Ultra 5 235T, x86_64,
  VT-x exposed.
- On the host (outside the agent sandbox), `/dev/kvm` is `crw-rw-rw-`,
  `root:kvm`; the current user can read/write it. `emulator -accel-check`
  reports `KVM (version 12) is installed and usable`. The sandbox namespace
  hides `/dev/kvm` and blocks ADB's local server socket; that is a sandbox
  boundary, not a Fedora KVM setup defect.
- Android Emulator 37.2.12 (build 16428233) is the SDK default used here;
  side-by-side 37.3.4 is installed. Platform-tools is 37.0.1 and cmdline-tools
  `latest` is 23.0.
- Installed images include API 37.0 Google APIs x86_64 revision 7 and Google
  Play x86_64 revision 6; API 36 Google APIs x86_64 revision 7; and older API
  26 Google APIs. The accepted AVD is Pixel 6 / API 37.0 / Google APIs,
  4096 MiB RAM, 4 x86_64 cores, 1080x2400 at 420 dpi. It remains the existing
  disposable `gould014-api37-gapi` AVD.
- Host memory was 30 GiB total / 17 GiB available during verification, swap
  8 GiB, filesystem free space 539 GiB, `/tmp` free space 14 GiB, open-file
  limit 4096. Resource exhaustion was not present.
- The failed command was headless (`-no-window -no-audio -no-boot-anim`) with
  `swiftshader_indirect`. Recovery uses `swangle`; emulator logs identify its
  renderer as ANGLE over SwiftShader. No Vulkan/driver packages were added.
- Existing AVDs were inventoried and retained. No ADB `wait-for-device` process
  was left behind. No app release APK was signed or produced.

## Fix and reproducible procedure

Use the valid API 37 Google APIs AVD and `swangle`; do not relax SELinux or
change Fedora-wide policy. The repeatable gate is:

```sh
scripts/run-gould014-emulator-verification.sh
```

The script fails closed unless cmdline-tools 23.0, matching API 37 AVD targets,
the expected system image, readable/writable KVM, and an idle emulator/ADB
device inventory are present. It explicitly selects one emulator serial,
starts headlessly with `-gpu swangle -read-only -no-snapshot-load
-no-snapshot-save`, polls rather than spawning `adb wait-for-device`, and
requires all of:

- the explicit serial is online and remains online across five checks;
- `sys.boot_completed=1`;
- Android's `mount` service is published;
- `ro.kernel.qemu=1` and API level 37.

It installs the debug APK, checks that `MainActivity` reaches the resumed state,
runs `AppLanguageTest` as the instrumentation smoke, then the focused
repository/reward, v11 migration, widget, and responsive accessibility tests.
It uses the emulator console to shut down on both pass and failure. Evidence goes
to `/tmp/gould014-emulator-verification/` by default. Override
`GOULD014_RESULTS_DIR`, `GOULD014_AVD`, or `GOULD014_TEST_CLASSES` only when a
matching disposable API 37 AVD and exact emulator-only test classes are known.
The script refuses to kill unrelated emulators or ADB devices. If it refuses
due to a stale instance, stop only the identified disposable emulator with
`adb -s emulator-NNNN emu kill`, wait for its QEMU process to exit, then retry.

To create a replacement disposable AVD if the accepted one becomes invalid,
first confirm the image is already installed, use cmdline-tools 23.0, and put
`ANDROID_AVD_HOME` in a new directory under `/tmp`; preserve existing AVDs. For
this host, the exact API 37 Google APIs replacement command is:

```sh
sdk=/home/stokonn/Android/Sdk
avd_home_for_gould=/tmp/gould014-api37-avds
mkdir -p "$avd_home_for_gould"
ANDROID_HOME="$sdk" ANDROID_AVD_HOME="$avd_home_for_gould" \
  "$sdk/cmdline-tools/latest/bin/avdmanager" create avd \
  --name gould014-api37-gapi-fresh \
  --package 'system-images;android-37.0;google_apis;x86_64' --device pixel_6
sed -i -e 's/^hw.ramSize=.*/hw.ramSize=4096M/' \
  -e 's/^hw.cpu.ncore=.*/hw.cpu.ncore=4/' \
  "$avd_home_for_gould/gould014-api37-gapi-fresh.avd/config.ini"
grep '^target=android-37.0$' "$avd_home_for_gould/gould014-api37-gapi-fresh.ini"
grep '^target=android-37.0$' "$avd_home_for_gould/gould014-api37-gapi-fresh.avd/config.ini"
ANDROID_HOME="$sdk" ANDROID_AVD_HOME="$avd_home_for_gould" \
  GOULD014_AVD=gould014-api37-gapi-fresh \
  scripts/run-gould014-emulator-verification.sh
```

Both target checks must print a match before launch. The runner selects
`swangle`, headless operation, and a temporary read-only AVD overlay. Do not
install packages or alter host security settings as part of recovery.

Troubleshooting: if the runner reports no `/dev/kvm`, use host (not sandbox)
`stat /dev/kvm`, `test -r /dev/kvm`, `test -w /dev/kvm`, and
`$sdk/emulator/emulator -accel-check`. If ADB reports an occupied device, inspect
`adb devices -l` and QEMU PIDs, then stop only the identified disposable
instance. If exit 139 returns, inspect the new PID with `coredumpctl info PID`
and matching audit records for `execheap`; retain the core and do not make SELinux
permissive or add an unscoped policy rule.

## Recovery verification

- The accepted `gould014-api37-gapi` AVD started on explicit `emulator-5590`
  with `-gpu swangle`; it registered with ADB, reported API 37,
  `sys.boot_completed=1`, and `service check mount: found`, and stayed online
  during instrumentation while guest SELinux reported Enforcing.
- Focused instrumentation first exposed three incorrect test expectations:
  a prior-Sunday manual entry was counted in the current week's totals, a
  reward was expected before its week closed, and 60 seconds was expected as
  60 minutes. Those fixtures were corrected to assert the documented week and
  minute semantics. Added a Room-reopen regression for owned-only cosmetic
  Apply/loadout persistence.
- Final end-to-end run of
  `GOULD014_RESULTS_DIR=/tmp/gould014-emulator-verification-final scripts/run-gould014-emulator-verification.sh`
  on the same API 37 image: the `AppLanguageTest` instrumentation smoke passed
  first (1/1); the following focused batch passed **21 tests with 0
  failures/errors/skips in 1m35s**
  (`ActivityRepositoryTest` 8, `ActivityMigrationTest` 1, `WidgetTest` 9,
  `Stage8cUiRegressionTest` 3). This includes perfect-week
  eligibility/idempotency across reopen, cosmetics ownership/apply/reopen,
  populated v11 migration preservation, RemoteViews rendering across sizes and
  font scales, and responsive navigation/accessibility checks across 32
  configurations.
- Debug app installed; `MainActivity` cold-started successfully in the final
  run (`Status: ok`, 874 ms). UI helper navigated to Activity. Inspected
  `/tmp/gould014-api37-ui/home-ready.png` and `activity.png`; Activity, Beta Week,
  weekly overview, goals action, and year heatmap rendered. The Activity
  accessibility node was located by its visible label.
- No crash or new SELinux `execheap` denial occurred in the controlled
  `swangle` startup/instrumentation run. The earlier SIGSEGV cores remain as
  evidence; they were not deleted. The managed script shut the device down;
  afterward `adb devices -l` had no devices and no QEMU/ADB wait process remained.

Remaining 0.14 work is tracked in
[`../planning/gould-0.14/STAGE-CHECKLIST.md`](../planning/gould-0.14/STAGE-CHECKLIST.md).
This local recovery did not run the full instrumentation suite, signed build,
in-place upgrade, actual launcher placement/resize journey, or release gates.
Cosmetic ownership/apply was verified at the Room repository boundary; direct
interaction with an owned cosmetic in the production collection screen remains
part of visual acceptance.
No commit, push, PR update, merge, tag, or publication was performed.

created by stokonn with love <3
