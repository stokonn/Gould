#!/usr/bin/env python3
"""Append one compact JSON line per verification run to the Gould audit trail.

Usage: verify-audit.py <tier> <status> <duration_seconds> <detail>
Tier: FAST | INTEGRATION | RELEASE. Status: PASS | FAIL | SKIP | PARTIAL.
The audit file is committed documentation of actually executed checks.
"""
import datetime
import hashlib
import json
import subprocess
import sys
from pathlib import Path

root = Path(__file__).resolve().parents[1]
audit = root / "docs/planning/gould-0.13/verification-audit.jsonl"

tier, status, duration, detail = sys.argv[1:5]
assert tier in {"FAST", "INTEGRATION", "RELEASE"}, tier
assert status in {"PASS", "FAIL", "SKIP", "PARTIAL"}, status
sha = subprocess.check_output(["git", "rev-parse", "--short", "HEAD"], cwd=root, text=True).strip()
dirty = bool(subprocess.check_output(["git", "status", "--porcelain"], cwd=root, text=True).strip())


def fingerprint(pathspecs):
    paths = sorted(set(subprocess.check_output(
        ["git", "ls-files", "--cached", "--others", "--exclude-standard", "--", *pathspecs],
        cwd=root, text=True).splitlines()))
    digest = hashlib.sha256()
    count = 0
    for name in paths:
        path = root / name
        if path.is_file():
            digest.update(name.encode() + b"\0" + hashlib.sha256(path.read_bytes()).digest())
            count += 1
    return digest.hexdigest(), count


source_sha, source_files = fingerprint([
    "app/src/main", "app/schemas", "app/build.gradle.kts", "app/proguard-rules.pro",
    "build.gradle.kts", "settings.gradle.kts", "gradle.properties", "gradle", "gradlew", "gradlew.bat",
])
tools_sha, _ = fingerprint([
    "scripts/verify-*.sh", "scripts/verify-audit.py", "scripts/gradle-lock.sh", "scripts/build-optimized-local.sh",
])
entry = {
    "time": datetime.datetime.now(datetime.timezone.utc).isoformat(timespec="seconds"),
    "tier": tier,
    "status": status,
    "seconds": int(duration),
    "commit": sha + ("-dirty" if dirty else ""),
    "detail": detail,
    "source_sha256": source_sha,
    "source_files": source_files,
    "verification_tools_sha256": tools_sha,
}
audit.parent.mkdir(parents=True, exist_ok=True)
with audit.open("a") as handle:
    handle.write(json.dumps(entry, ensure_ascii=True) + "\n")
print(f"audit: {audit.relative_to(root)} <- {entry['time']} {tier} {status} {duration}s @{entry['commit']}")
