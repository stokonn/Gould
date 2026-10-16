#!/usr/bin/env bash
set -euo pipefail
# Required publication gate: compare actual APK certificates, never infer from signing configuration.
[[ $# -eq 2 ]] || { echo 'Usage: verify-upgrade-signing.sh published.apk candidate.apk' >&2; exit 2; }
sdk_path="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
[[ -n "$sdk_path" ]] || { echo 'Set ANDROID_HOME to the installed SDK' >&2; exit 2; }
signer="$sdk_path/build-tools/36.0.0/apksigner"
certificate() { "$signer" verify --print-certs "$1" | sed -n 's/^Signer #1 certificate SHA-256 digest: //p'; }
baseline_cert="$(certificate "$1")"
candidate_cert="$(certificate "$2")"
[[ -n "$baseline_cert" && -n "$candidate_cert" ]] || { echo 'Unable to verify APK certificates' >&2; exit 1; }
if [[ "$baseline_cert" != "$candidate_cert" ]]; then
    printf 'BLOCKED: incompatible signing certificates\nPublished: %s\nCandidate: %s\n' "$baseline_cert" "$candidate_cert" >&2
    exit 1
fi
printf 'PASS: matching APK signing certificate %s\n' "$candidate_cert"
