# Stage 6.10–6.11 — bounded emulator recovery and runtime verification

Prepared 2026-10-23. **Procedure only; no emulator launch or runtime pass executed
by the agent.** Existing host builds and publication have succeeded and must not be
repeated as recovery steps. Overall runtime/UI acceptance remains pending.

## Confirmed state and uncertainty

- Owner host: Fedora 44, Emulator 37.2.12, KVM available and usable; AVDs
  `stage5-api36` and `Medium_Phone_API_37.0`; no device connected via ADB.
- Repeated SIGSEGV in `qemu-system-x86_64-headless`; inspected command included
  `-gpu swiftshader_indirect`; last owner-inspected coredump PID 1321999.
- Exact crash cause is **unconfirmed**. Do not attribute it to KVM, Fedora,
  headless mode, the system image or GPU flag without corresponding evidence.
- Google's [graphics guidance](https://developer.android.com/studio/run/emulator-acceleration)
  lists `software` as a supported software-rendering selection and marks
  `swiftshader_indirect` deprecated since 36.4.9. Installed `emulator -help-gpu`
  also lists `software`. Deprecation alone does not establish crash causality.
- [Command-line documentation](https://developer.android.com/studio/run/emulator-commandline)
  explains separate AVD data directories and `-no-snapshot` (no Quick Boot load/save).
  This plan uses a fresh data directory and preserves both existing AVDs.
- [37.2.12 release notes](https://developer.android.com/studio/releases/emulator)
  establish the version; they do not establish the cause of this host crash.

## 1. Preserve and review existing evidence; do not boot yet

Use the already collected version/KVM/AVD/ADB diagnostics. Retain existing boot logs,
command line, signal, executable build ID and coredump metadata in an ignored local
folder. If PID 1321999 metadata was not saved, a single read-only
`coredumpctl info 1321999 --no-pager` capture is appropriate. Request a symbolized
stack with frame arguments suppressed if needed. Keep raw cores/memory private;
review/sanitize any excerpts before placing them in repository evidence.

No SDK upgrade/downgrade, library replacement, permissions/SELinux change, global
ADB server restart, AVD reset, snapshot deletion or userdata copying is part of this
procedure. Do not boot either existing AVD again under the known crashing settings.
If there is no permitted desktop display or local socket access, stop and select a
functioning permitted emulator venue; do not run these commands inside the denied
agent sandbox or substitute a physical phone.

## 2. At most one fresh, isolated desktop smoke trial

This is a recovery trial, not a causal experiment: a fresh AVD and renderer/display
configuration differ from the previous crash. Success would establish a usable new
venue, not explain or fix the original crash.

On the permitted Fedora desktop, first ensure ports 5590/5591 are unused and the
already installed API 36 image exists. Use a new registry/data directory under this
worktree's ignored `.gradle`; no existing AVD configuration is edited. Stop if the
profile/image is unavailable or a tool asks to install/download anything.

```bash
set -euo pipefail
cd /tmp/goldberg-stage6-android-local
sdk=/home/stokonn/Android/Sdk
work="$PWD/.gradle/community-emulator-recovery"
serial=emulator-5590
mkdir -p "$work/avds"
test ! -e "$work/trial.started"
test ! -e "$work/device.avd"
test -n "${DISPLAY:-}${WAYLAND_DISPLAY:-}"
test -f "$sdk/system-images/android-36/google_apis/x86_64/package.xml"
python3 - <<'PYPORTS'
import socket
from contextlib import ExitStack
with ExitStack() as stack:
    for port in (5590,5591):
        probe=stack.enter_context(socket.socket(socket.AF_INET,socket.SOCK_STREAM))
        probe.bind(('127.0.0.1',port))
PYPORTS
printf 'no\n' | ANDROID_AVD_HOME="$work/avds" \
  "$sdk/cmdline-tools/latest/bin/avdmanager" create avd \
  --name GoldbergStage610Recovery \
  --package 'system-images;android-36;google_apis;x86_64' \
  --device pixel_6 --path "$work/device.avd" > "$work/create.log" 2>&1
test -f "$work/avds/GoldbergStage610Recovery.ini"
touch "$work/trial.started"
ANDROID_AVD_HOME="$work/avds" "$sdk/emulator/emulator" \
  -avd GoldbergStage610Recovery -port 5590 -accel on \
  -gpu software -no-snapshot -no-audio -memory 2048 -cores 2 -verbose \
  > "$work/boot.log" 2>&1 &
emu_pid=$!
printf '%s\n' "$emu_pid" > "$work/launcher.pid"
```

The command is windowed; no `-no-window` or `swiftshader_indirect` is used. Emulator
binary selection remains tool-controlled. Leave the single-trial marker in place.
Check the recorded process and selected serial for up to about 180 seconds; polling
is allowed, **restarting the emulator is not**. Every ADB command must contain
`-s "$serial"`. A bounded boot probe is `timeout 5s "$sdk/platform-tools/adb"
-s "$serial" shell getprop sys.boot_completed`; require `1` before installing.

If the launcher exits, a new SIGSEGV occurs, boot stalls or ADB never connects:
stop this trial, retain the new log and selected core metadata, and perform no
second renderer/AVD/acceleration retry under this plan. For a still-running owned
trial, close its window or use only the selected serial's `emu kill`. If ADB is
unreachable, send TERM only to the recorded launcher PID after confirming its
command line/ownership. Never kill all emulator/qemu processes. Preserve the fresh
trial directory for diagnosis; existing AVDs and their userdata remain untouched.
The next action after failure is stack-based diagnosis or a separately selected
working emulator host, not speculative repeated launches.

## 3. Verify only after this isolated emulator is stable

The host already produced the required APKs. **Do not rebuild them or rerun the
publication script.** Verify their recorded hashes/signatures, then confirm the
selected device is this emulator before installing synthetic test artifacts:

```bash
set -euo pipefail
cd /tmp/goldberg-stage6-android-local
python3 - <<'PYAPKS'
from pathlib import Path
import hashlib
expected={
    ".gradle/community-host-artifacts/1277bed/app-debug.apk": "8e30d783d223b7fc9b48c780edf6a3f5f24624aa016a77c3312d6f4149a0751d",
    ".gradle/community-host-artifacts/2f1524e/app-debug-androidTest.apk": "519dcf93d767e9a8fa5fade656f3361fe0f5d332e6e833603d8c66e75c60305e",
}
for name,want in expected.items():
    assert hashlib.sha256(Path(name).read_bytes()).hexdigest()==want,name
PYAPKS
adb_bin=/home/stokonn/Android/Sdk/platform-tools/adb
serial=emulator-5590
work=/tmp/goldberg-stage6-android-local/.gradle/community-emulator-recovery
test "$("$adb_bin" -s "$serial" shell getprop ro.kernel.qemu | tr -d '\r')" = 1
test "$("$adb_bin" -s "$serial" shell getprop sys.boot_completed | tr -d '\r')" = 1
test "$("$adb_bin" -s "$serial" emu avd name | sed -n '1p' | tr -d '\r')" = GoldbergStage610Recovery
"$adb_bin" -s "$serial" install -r \
  /tmp/goldberg-stage6-android-local/.gradle/community-host-artifacts/1277bed/app-debug.apk
"$adb_bin" -s "$serial" install -r \
  /tmp/goldberg-stage6-android-local/.gradle/community-host-artifacts/2f1524e/app-debug-androidTest.apk
"$adb_bin" -s "$serial" shell am instrument -w -r \
  -e class com.opus.piano.CommunityLocalInstrumentedTest \
  com.opus.piano.test/androidx.test.runner.AndroidJUnitRunner \
  > "$work/community-instrumentation.log" 2>&1
```

Read the full runner output and exact executed/failed/skipped counts; shell exit 0
alone is insufficient. Expected new test count is 5. Do not replace signing
mismatch recovery with uninstall, clear-data or a wipe. No real Google/service
account is enrolled and community production transport remains disabled.

After the five native tests actually pass, run relevant existing native Room/backup
migration tests against this same disposable device and verify a seeded upgrade.
Then inspect the contributor Settings subpage using synthetic draft content:
create/save/reopen, explicit preview, truthful disabled submit, error/retry states,
account-context behavior through synthetic seams, rotation and actual force-stop/
relaunch persistence, and cold startup into local practice with community unavailable.
Check Ivory/Nocturne, CZ/EN, representative sizes/font scales and record screenshots.
Keep unexercisable fixture-only/account cases pending rather than enabling real auth.
No physical device or real user data is used.

## Acceptance and stop rules

The successful JVM/build/lint/packaging/publication results remain valid throughout
this procedure. Record runtime evidence separately and retain all earlier failure
logs/comments. Native packaging is PASS; native execution currently remains 0.
Formal 6.10/6.11 stay unchecked until required runtime/UI acceptance is actually
satisfied. A boot-only result is not app acceptance; five passing native tests do
not substitute for Android process-death/upgrade/UI checks. No 6.12–6.14, merge,
deployment, hosted changes or release is authorized by this procedure.

created by stokonn with love <3
