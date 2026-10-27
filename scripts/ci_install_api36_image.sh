#!/usr/bin/env bash
# Bounded retry of sdkmanager's corrupt/incomplete CDN archive failure. This only
# installs the approved API36 image; no app/test assertion is skipped or weakened.
set -euo pipefail
mkdir -p stage5-ci-results
IMAGE='system-images;android-36;google_apis;x86_64'
IMAGE_ROOT="${ANDROID_HOME:?}/system-images/android-36/google_apis/x86_64"
for attempt in 1 2 3; do
  if sdkmanager --install "$IMAGE" > "stage5-ci-results/image-install-$attempt.log" 2>&1; then
    cat "stage5-ci-results/image-install-$attempt.log"
    test -s "$IMAGE_ROOT/system.img"
    test -s "$IMAGE_ROOT/source.properties"
    cp "$IMAGE_ROOT/source.properties" stage5-ci-results/image-source.properties
    echo "API36 system image ready (attempt $attempt)"
    exit 0
  fi
  cat "stage5-ci-results/image-install-$attempt.log"
  echo "API36 image installation failed on attempt $attempt" >&2
  # Let sdkmanager manage its own temporary/cache files. Never delete a shared SDK.
done
echo 'API36 image unavailable after three attempts; no emulator/test body executed' >&2
exit 1
