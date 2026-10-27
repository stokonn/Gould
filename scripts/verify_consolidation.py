"""Run final host gates without publishing, changing credentials or using ADB."""
from pathlib import Path
import hashlib
import json
import os
import subprocess
import sys
import uuid
import xml.etree.ElementTree as ET


def fingerprint(root):
    names = subprocess.check_output(["git", "ls-files", "-z"], cwd=root).split(b"\0")
    return {
        os.fsdecode(name): hashlib.sha256((root / os.fsdecode(name)).read_bytes()).hexdigest()
        for name in names if name
    }


def main():
    root = Path(__file__).resolve().parents[1]
    output = root / ".gradle/final-consolidation" / uuid.uuid4().hex
    output.mkdir(parents=True, mode=0o700)
    report = {"commit": subprocess.check_output(["git", "rev-parse", "HEAD"], cwd=root, text=True).strip(),
              "source_hashes": fingerprint(root), "checks": [], "accepted": False}
    env = os.environ.copy()
    env["ANDROID_HOME"] = env.get("ANDROID_HOME") or env.get("ANDROID_SDK_ROOT") or str(Path.home() / "Android/Sdk")
    checks = [
        ("android-host", ["./gradlew", "clean", "assembleDebug", "testDebugUnitTest", "assembleDebugAndroidTest", "lintDebug", "--no-daemon", "--max-workers=1"]),
        *[(name, [sys.executable, "scripts/" + name + ".py"]) for name in
          ["check_migrations", "check_backup_fixtures", "check_localization", "check_history_paging", "check_agent_policy"]],
        ("v5-fixtures", [sys.executable, "-c", "import sys,runpy;from pathlib import Path;sys.path.insert(0,str(Path('docs/planning/goldberg-0.11/stage-3b').resolve()));m=runpy.run_path('docs/planning/goldberg-0.11/stage-3b/check_fixtures.py');[m['check'](m['OUT']/n) for n in ['full-v5.json','minimal-v5.json','unknown-v5.json']]"]),
        ("encrypted-backups", [sys.executable, "-m", "unittest", "discover", "-s", "scripts/stage6", "-p", "test_backup_store.py"]),
        ("private-runtime", [sys.executable, "-m", "unittest", "discover", "-s", "scripts/stage6", "-p", "test_private_runtime.py"]),
        ("historical-evidence", [sys.executable, "scripts/stage6/verify_evidence.py"]),
        *[("npm-" + Path(package).name, ["npm", "ci", "--prefix", package, "--ignore-scripts", "--no-audit", "--no-fund"])
          for package in ["spikes/stage6-schema-pg", "spikes/stage6-auth-local", "services/community-bff"]],
        ("backend-postgres-workerd", [sys.executable, "services/community-bff/run-local.py"]),
        ("backend-android-http", [sys.executable, "services/community-bff/run-local.py", "--client-worktree", str(root), "--client-only"]),
        ("fail-closed-bundle", ["node", "services/community-bff/build-local.mjs"]),
        ("debug-artifact", [sys.executable, "scripts/stage8/independent/audit_artifact.py", "--apk", "app/build/outputs/apk/debug/app-debug.apk", "--output", str(output / "artifact.json")]),
        ("schema-unchanged", ["git", "diff", "--exit-code", "--", "app/schemas"]),
        ("git-integrity", ["git", "fsck", "--full"]),
    ]
    try:
        for name, command in checks:
            print("Running " + name, flush=True)
            log = output / (name + ".log")
            with log.open("w") as stream:
                result = subprocess.run(command, cwd=root, env=env, stdout=stream, stderr=subprocess.STDOUT)
            report["checks"].append({"name": name, "argv": command, "exit_code": result.returncode, "log": log.name})
            if result.returncode:
                raise RuntimeError(name + " failed; inspect " + str(log))
            if name == "android-host":
                suites = list((root / "app/build/test-results/testDebugUnitTest").glob("TEST-*.xml"))
                assert suites, "No fresh JVM test results"
                counts = {key: sum(int(ET.parse(p).getroot().get(key, "0")) for p in suites)
                          for key in ["tests", "failures", "errors", "skipped"]}
                report["jvm"] = counts
                assert counts["tests"] > 0 and counts["failures"] == counts["errors"] == counts["skipped"] == 0, counts
                # The subsequent focused HTTP test replaces Gradle's XML outputs.
                for path in suites:
                    (output / path.name).write_bytes(path.read_bytes())
        report["source_unchanged"] = report["source_hashes"] == fingerprint(root)
        assert report["source_unchanged"], "Tracked source changed during verification"
        report["accepted"] = True
    except (OSError, RuntimeError, AssertionError) as error:
        report["error"] = str(error)
        print(str(error), file=sys.stderr)
    finally:
        (output / "report.json").write_text(json.dumps(report, indent=2) + "\n")
        print("Report: " + str(output / "report.json"), flush=True)
    return 0 if report["accepted"] else 1


if __name__ == "__main__":
    raise SystemExit(main())
