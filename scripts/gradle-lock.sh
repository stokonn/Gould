#!/usr/bin/env bash
# Serialize Gould Gradle workflows across worktrees on this host.
set -euo pipefail
[[ $# -gt 0 ]] || { echo "Usage: scripts/gradle-lock.sh <command> [args...]" >&2; exit 2; }
lock_dir="${GOULD_GRADLE_LOCK_DIR:-${XDG_CACHE_HOME:-$HOME/.cache}/gould}"
mkdir -p "$lock_dir"
export GOULD_GRADLE_LOCK_HELD=1
# --close keeps the lock out of child/daemon file descriptors. flock itself
# holds it until the workflow exits; an idle Gradle daemon cannot retain it.
status=0
flock --nonblock --conflict-exit-code 75 --close "$lock_dir/gradle.lock" "$@" || status=$?
if [[ "$status" -eq 75 ]]; then
  echo "Another Gould Gradle workflow holds the host lock; no new workflow started." >&2
fi
exit "$status"
