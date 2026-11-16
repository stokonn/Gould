# Gould 0.15 catalogue runtime diagnosis

Status: pending owner access to the Galaxy A55 and its ADB computer. The
mandatory source verification produced the debug APK recorded below, but no APK
has been installed or inspected on the phone. The previous code fix is
considered failed; this document makes no runtime-success claim.

## 1. Identify the installed artifact before changing anything

Run from a Bash shell with Android Platform Tools installed. These commands do
not uninstall, clear app data, or erase existing logcat history.

```bash
set -eu
PKG=com.opus.piano
adb devices -l
adb shell dumpsys package "$PKG" | grep -E 'versionCode=|versionName=|codePath=|firstInstallTime|lastUpdateTime'
REMOTE_APK="$(adb shell pm path "$PKG" | tr -d '\r' | sed -n 's/^package://p' | grep '/base.apk$' | head -n 1)"
test -n "$REMOTE_APK"
adb pull "$REMOTE_APK" /tmp/gould-015-installed-base.apk
sha256sum /tmp/gould-015-installed-base.apk
SDK_ROOT="${ANDROID_SDK_ROOT:-$ANDROID_HOME}"
APKSIGNER="$(find "$SDK_ROOT/build-tools" -type f -name apksigner | sort -V | tail -n 1)"
"$APKSIGNER" verify --print-certs /tmp/gould-015-installed-base.apk
```

Compare the pulled APK SHA-256 with the current local debug artifact hash:
`c62e68249c5ee50612cb09006abdc56b7b82dbc2c492ba13561a4031a08f3a7f`.
Local path: `app/build/outputs/apk/debug/app-debug.apk`; 162,260,883 bytes;
package `com.opus.piano`, version `0.15.0-dev`, versionCode 17. It is signed
with the Android debug certificate (SHA-256
`53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`), so it
is not the trusted-signed, non-debuggable owner testing APK and must not be
treated as one. A matching version and certificate alone do not prove the
binary is identical; compare the pulled APK hash.
Expected package/version are `com.opus.piano`, `0.15.0-dev`, versionCode 17;
expected signing certificate SHA-256 is
`53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`.
A matching version and certificate alone do not prove the binary is identical;
compare the pulled APK hash.

If `adb devices -l` does not show the phone as `device`, stop and resolve the
USB authorization/Platform Tools issue separately. Do not uninstall or clear
app storage. Share the version lines, pulled APK hash, signer fingerprint, and
ADB state.

## 2. Preserve a logcat capture of the existing failure

The previous APK contains no structured per-layer catalogue counters, so this
capture can only reveal existing Android/SQLite exceptions. It does not replace
the instrumented trace required for acceptance.

```bash
adb logcat -v threadtime -T 5m > /tmp/gould-015-catalogue-logcat.txt
```

With that command running, open Gould, reproduce Discover search for `chopin`,
then open Library → `Z katalogu`; stop capture with Ctrl+C. Do not run
`adb logcat -c`. Share only lines around Gould process errors or SQLite errors;
redact account/device identifiers and unrelated personal content.

## 3. Diagnostic source trace still required

After installed-artifact identity and available exception evidence are reviewed,
add narrowly scoped structured events and counters around: Discover input and
search key; repository entry; initialize/install stages; catalogue file path,
existence and byte length; SQLite open/schema/integrity outcome; normalized
query hash, filter and SQL statement/bind-count; composer/work query counts;
page mapping; Discover emissions and Compose row count; Library loading start,
completion, error and render count. Log no raw query, repertoire title,
composer text from personal data, identity, account, or backup values. Include
exception class and a correlation ID while allowing cancellation to propagate.

The current source includes structured phase timings tagged
`GouldCataloguePerf`, including an explicit initialization outcome and first
Catalogue landing render. There are no Galaxy A55 logcat captures in the
preserved diagnostics, so those checkpoints cannot yet be observed. The owner
is away from the ADB computer; do not claim runtime acceptance or a performance
improvement. Before producing an owner testing APK, compare the installed
artifact, collect a trace, and establish the first runtime failure boundary.
