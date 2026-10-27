#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
./gradlew assembleDebug --no-daemon --max-workers=1
python3 - <<'PY'
import hashlib
import json
from pathlib import Path
import re

directory = Path("app/build/outputs/apk/debug")
metadata = json.loads((directory / "output-metadata.json").read_text())
elements = metadata["elements"]
if len(elements) != 1:
    raise SystemExit("Expected one debug APK; inspect split outputs before naming an artifact")
version = elements[0]["versionName"]
if not re.fullmatch(r"[0-9]+\.[0-9]+(?:\.[0-9]+)?(?:[.-][A-Za-z0-9.]+)?", version):
    raise SystemExit("Invalid APK version")
filename = elements[0]["outputFile"]
if Path(filename).name != filename:
    raise SystemExit("Expected an APK basename")
source = directory / filename
target = directory / f"Gould-v{version}.apk"
if source.is_symlink() or target.is_symlink():
    raise SystemExit("Refusing symlink artifact paths")
payload = source.read_bytes()
if target.exists():
    if hashlib.sha256(target.read_bytes()).digest() != hashlib.sha256(payload).digest():
        raise SystemExit("Refusing to overwrite an existing different Gould artifact")
else:
    with target.open("xb") as output:
        output.write(payload)
print(f"APK: {target.resolve()}")
print(f"SHA-256: {hashlib.sha256(payload).hexdigest()}")
PY
