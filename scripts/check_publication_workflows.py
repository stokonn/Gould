#!/usr/bin/env python3
"""Fail closed if repository workflows gain automatic publication authority."""
from pathlib import Path
import re
import sys

WORKFLOWS = Path(__file__).resolve().parents[1] / ".github/workflows"


def violations(text: str, path: str = "workflow.yml") -> list[str]:
    problems = []
    # Every checked-in workflow is verification-only and explicitly least-privileged.
    permissions = re.search(r"(?m)^permissions:\s*\n((?:^[ \t]+[^\n]*\n?)*)", text)
    if not permissions or not re.search(r"(?m)^\s+contents:\s*read\s*$", permissions.group(1)):
        problems.append(f"{path}: explicit read-only contents permission is required")
    if re.search(r"(?im)^\s*(?:contents|packages|actions|id-token):\s*write\s*$|^\s*permissions:\s*write-all\s*$", text):
        problems.append(f"{path}: workflow grants write permission")
    if re.search(r"(?im)^\s*pull_request_target:", text):
        problems.append(f"{path}: pull_request_target is not permitted")
    if re.search(r"\bsecrets\.", text):
        problems.append(f"{path}: workflow must not reference secrets")
    for checkout in re.finditer(r"(?im)^\s*-\s*uses:\s*actions/checkout@[^\n]*", text):
        next_step = re.search(r"(?im)^\s*-\s*(?:uses|name):", text[checkout.end():])
        end = checkout.end() + next_step.start() if next_step else len(text)
        if not re.search(r"(?im)^\s+persist-credentials:\s*false\s*$", text[checkout.end():end]):
            problems.append(f"{path}: checkout must not persist credentials")
    if re.search(r"(?im)^\s*-?\s*uses:\s*[^\n]*(?:softprops/action-gh-release|ncipollo/release-action|marvinpinto/action-automatic-releases)", text):
        problems.append(f"{path}: release publication action is not permitted")
    if re.search(r"(?i)(?:gh\s+release\s+create|gradle\s+[^\n]*publish|npm\s+publish)", text):
        problems.append(f"{path}: publication command is not permitted")
    return problems


def main() -> int:
    errors = []
    for path in sorted(WORKFLOWS.glob("*.yml")) + sorted(WORKFLOWS.glob("*.yaml")):
        errors.extend(violations(path.read_text(encoding="utf-8"), str(path.relative_to(WORKFLOWS.parent.parent))))
    if errors:
        print("\n".join(errors), file=sys.stderr)
        return 1
    print("Publication workflow policy passed: explicit read-only permissions, no secrets/persisted checkout credentials, and no release actions or commands.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
