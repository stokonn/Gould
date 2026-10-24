"""Execute frozen verification only on its actual immutable historical snapshot.

No worktree checkout/reset or changes to historical validators, metadata or evidence.
The temporary tree contains exact Git blobs for the original source/fixture pins.
"""
import json
import subprocess
import tempfile
import sys
from pathlib import Path
from current_provenance import (ROOT, META, HIST, HISTORICAL_SNAPSHOT,
                                historical_blob, historical_integrity)


def main(security=False):
    meta, hist, checked = historical_integrity()
    names = set(checked) | {str(META.relative_to(ROOT))}
    names.update(hist['files'])
    names.update(str((HIST.parent / p).relative_to(ROOT)) for p in hist['fixtureFiles'])
    names.update(['spikes/stage6-security-local/verify_contract.py',
                  'docs/planning/goldberg-0.11/stage-6/results/security-local/t22-investigation/OWNER-APPROVAL.json'])
    if security:
        # Preserve the historical 345/313/205 security-evidence verifier and its
        # 17 regressions. Its source pin is intentionally strict on evolved code.
        prefix = 'docs/planning/goldberg-0.11/stage-6/results/security-local/'
        artifacts = prefix + 'post-t22-run-artifacts-sha256.json'
        pins = json.loads(historical_blob(artifacts))
        source_manifest = prefix + 'run-x0nDSTXd/source-sha256.json'
        names.update(pins)
        names.update([artifacts, source_manifest, prefix + 'run-csaqQFXV/focused.tap'])
        names.update(json.loads(historical_blob(source_manifest)))
        tracked = subprocess.check_output(['git', 'ls-tree', '-r', '--name-only', HISTORICAL_SNAPSHOT,
                                          '--', 'spikes/stage6-security-local'], cwd=ROOT, text=True).splitlines()
        names.update(n for n in tracked if n.endswith('.py'))
        # The current retained artifacts must match too; snapshot execution alone
        # would not detect someone editing a local historical raw log.
        assert (ROOT / artifacts).read_bytes() == historical_blob(artifacts)
        import hashlib
        for name, digest in pins.items():
            assert hashlib.sha256((ROOT / name).read_bytes()).hexdigest() == digest, name
    with tempfile.TemporaryDirectory(prefix='goldberg-historical-') as folder:
        tree = Path(folder)
        for name in sorted(names):
            target = tree / name
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(historical_blob(name))
        # Git object lookups are read-only and refer to the original repository.
        gitdir = subprocess.check_output(['git', 'rev-parse', '--absolute-git-dir'], cwd=ROOT, text=True).strip()
        (tree / '.git').write_text('gitdir: ' + gitdir + '\n')
        print(f'Historical snapshot {HISTORICAL_SNAPSHOT}; source baseline {meta["baseline"]}', flush=True)
        if security:
            subprocess.run(['python3', '-m', 'unittest', 'discover', '-s',
                            'spikes/stage6-security-local/tests', '-p', 'test_*.py'],
                           cwd=tree, check=True)
            print('PASS: historical security-evidence regressions on original source; '
                  'historical raw artifacts unchanged. Prior345 executions not rerun.')
            return
        # Frozen verify.check() includes the immutable-source comparison unchanged.
        subprocess.run(['python3', '-m', 'unittest', 'discover', '-s',
                        'contracts/admin/v1/decision-proposal', '-p', 'test_*.py'],
                       cwd=tree, check=True)
        subprocess.run(['python3', '-c',
                        "import runpy; runpy.run_path('spikes/stage6-security-local/verify_contract.py')['check'](check_client_worktree=False)"],
                       cwd=tree, check=True)
    print(f'PASS: frozen {meta["verifierTests"]}-test historical suite on original Git source; '
          'no current-file historical pin PASS claimed.')


if __name__ == '__main__':
    assert sys.argv[1:] in ([], ['--security'])
    main(security=sys.argv[1:] == ['--security'])
