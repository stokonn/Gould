#!/usr/bin/env python3
"""Append one compact JSON line per verification run to the Gould audit trail.

Usage: verify-audit.py <tier> <status> <duration_seconds> <detail>
Tier: FAST | INTEGRATION | RELEASE. Status: PASS | FAIL | SKIP | PARTIAL.
The audit file is committed documentation of actually executed checks.
"""
import datetime
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
entry = {
    "time": datetime.datetime.now(datetime.timezone.utc).isoformat(timespec="seconds"),
    "tier": tier,
    "status": status,
    "seconds": int(duration),
    "commit": sha + ("-dirty" if dirty else ""),
    "detail": detail,
}
audit.parent.mkdir(parents=True, exist_ok=True)
with audit.open("a") as handle:
    handle.write(json.dumps(entry, ensure_ascii=True) + "\n")
print(f"audit: {audit.relative_to(root)} <- {entry['time']} {tier} {status} {duration}s @{entry['commit']}")
