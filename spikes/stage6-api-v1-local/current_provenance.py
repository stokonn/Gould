"""Shared inventory and immutable historical checks for current local acceptance."""
from pathlib import Path
import hashlib
import json
import subprocess

ROOT = Path(__file__).resolve().parents[2]
BASE = Path(__file__).resolve().parent
HIST = ROOT / 'contracts/admin/v1/source-provenance.json'
META = ROOT / 'contracts/admin/v1/decision-proposal/metadata.json'
CUR = HIST.parent / 'current-provenance/current-source-provenance.json'
# Last design checkpoint: all metadata pins (including original handler sources)
# match this commit. metadata.baseline is the earlier source-only checkpoint.
HISTORICAL_SNAPSHOT = '6dd3136269ec7f5932c8c4b3689fb065d4db9105'
APPROVED_HISTORICAL_DRIFT = {
    'spikes/stage6-handlers-local/src/moderation.mjs',
    'spikes/stage6-handlers-local/src/submissions.mjs',
    'spikes/stage6-handlers-local/src/validation.mjs',
}


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def set_hash(files):
    return hashlib.sha256(json.dumps(files, sort_keys=True, separators=(',', ':')).encode()).hexdigest()


def historical_blob(path, revision=HISTORICAL_SNAPSHOT):
    return subprocess.check_output(['git', 'show', f'{revision}:{path}'], cwd=ROOT)


def historical_integrity():
    # Compare the metadata itself to an explicit immutable Git object, then every
    # frozen artifact on disk. Evolved handlers are checked against their historical
    # Git snapshot, never declared a current-file PASS of the old pin.
    assert META.read_bytes() == historical_blob(str(META.relative_to(ROOT))), 'historical metadata changed'
    meta = json.loads(META.read_text())
    hist = json.loads(HIST.read_text())
    checked = {}
    for group in ('files', 'baselineFiles'):
        for name, digest in meta[group].items():
            assert hashlib.sha256(historical_blob(name)).hexdigest() == digest, f'historical snapshot mismatch: {name}'
            if group == 'baselineFiles':
                assert hashlib.sha256(historical_blob(name, meta['baseline'])).hexdigest() == digest, name
            if name not in APPROVED_HISTORICAL_DRIFT:
                assert sha(ROOT / name) == digest, f'frozen artifact changed: {name}'
            checked[name] = digest
    assert hist['sourceSetHash'] == set_hash(hist['files'])
    assert hist['fixtureSetHash'] == set_hash(hist['fixtureFiles'])
    for name, digest in hist['files'].items():
        assert hashlib.sha256(historical_blob(name)).hexdigest() == digest, name
        if name not in APPROVED_HISTORICAL_DRIFT:
            assert sha(ROOT / name) == digest, f'unapproved historical drift: {name}'
    for name, digest in hist['fixtureFiles'].items():
        assert sha(HIST.parent / name) == digest, f'frozen fixture changed: {name}'
    return meta, hist, checked


def source_files():
    """All executable implementation, tests, runners, validators and input fixtures.

    The generated manifest and output evidence are excluded to avoid self-hashing.
    Their hashes are recorded separately by the execution runner.
    """
    paths = set()
    for folder in ['stage6-api-v1-local', 'stage6-schema-pg', 'stage6-auth-local',
                   'stage6-handlers-local', 'stage6-public-admin-abuse']:
        root = ROOT / 'spikes' / folder
        for sub in ['src', 'migrations', 'tests']:
            paths.update(p for p in (root / sub).rglob('*') if p.is_file()
                         and p.suffix in {'.mjs', '.sql', '.py', '.json'})
        paths.update(p for p in root.glob('*.py'))
        paths.update(p for p in root.glob('package*.json'))
    security = ROOT / 'spikes/stage6-security-local'
    paths.update(security.glob('*.py'))
    raw_index=ROOT/'docs/planning/goldberg-0.11/stage-6/results/security-local/post-t22-run-artifacts-sha256.json'
    paths.add(raw_index)
    paths.update(ROOT/n for n in json.loads(raw_index.read_text()))
    paths.add(ROOT/'docs/planning/goldberg-0.11/stage-6/results/security-local/run-csaqQFXV/focused.tap')
    paths.update(p for p in (security / 'tests').rglob('*') if p.is_file()
                 and p.suffix in {'.py', '.mjs', '.json'})
    paths.update(p for p in (ROOT / 'contracts/admin/v1').rglob('*') if p.is_file()
                 and '__pycache__' not in p.parts and 'current-provenance' not in p.parts)
    paths.update(ROOT / 'scripts' / n for n in ['check_agent_policy.py',
                 'check_backup_fixtures.py', 'check_stage6_acl_proposal.py'])
    # Explicitly include external source inputs used by the policy/backup/ACL gates.
    paths.update(ROOT / n for n in ['AGENTS.md', 'CLAUDE.md', 'GEMINI.md',
                 '.cursor/rules/goldberg.mdc', '.github/copilot-instructions.md',
                 'docs/planning/goldberg-0.11/stage-6/ACL-MIGRATION-PROPOSAL.md',
                 'app/src/main/java/com/opus/piano/data/OpusDatabase.kt',
                 'app/src/main/java/com/opus/piano/data/PersonalCatalogueBridge.kt'])
    paths.update((ROOT / 'docs/wiki').glob('*.md'))
    paths.update((ROOT / 'app/src/androidTest/assets/backups').glob('*.json'))
    paths.update((ROOT / 'app/schemas/com.opus.piano.data.OpusDatabase').glob('*.json'))
    for name in ['spikes/stage6-schema-pg/acl-proposal',
                 'docs/planning/goldberg-0.11/stage-3b/fixtures']:
        paths.update(p for p in (ROOT / name).rglob('*') if p.is_file())
    return {str(p.relative_to(ROOT)): sha(p) for p in sorted(paths)}


# Test selection is derived from actual source files/package configuration. Counts
# come from validated executed TAP bodies/plan/summaries, never hand-edited totals.
def tap_suites():
    api = json.loads((BASE / 'package.json').read_text())['scripts']['test'].split()
    assert api[:2] == ['node', '--test']
    api_files = ['spikes/stage6-api-v1-local/' + n for n in api[2:]]
    suites = {
        'http-postgres.tap': ['spikes/stage6-api-v1-local/tests/http-postgres.test.mjs'],
        'int8-postgres.tap': ['spikes/stage6-api-v1-local/tests/int8-postgres.test.mjs'],
        'auth.tap': ['spikes/stage6-auth-local/tests/auth.test.mjs'],
        'schema.tap': ['spikes/stage6-schema-pg/tests/schema.test.mjs'],
        'handlers.tap': ['spikes/stage6-handlers-local/tests/handlers.test.mjs'],
        'operations-pg.tap': ['spikes/stage6-public-admin-abuse/tests/postgres.test.mjs'],
        'operations-unit.tap': ['spikes/stage6-public-admin-abuse/tests/unit.test.mjs'],
        'postgres.tap': ['spikes/stage6-api-v1-local/tests/m3-postgres.test.mjs'],
        'm2-postgres.tap': ['spikes/stage6-api-v1-local/tests/m2-postgres.test.mjs'],
        'api.tap': api_files,
        't22.tap': ['spikes/stage6-security-local/tests/t22.test.mjs'],
    }
    for files in suites.values():
        assert files and all((ROOT / f).is_file() for f in files)
    return suites
