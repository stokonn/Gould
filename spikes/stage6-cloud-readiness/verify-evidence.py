"""Independently check the final local result against current sources/artifacts."""
from pathlib import Path
import hashlib, json, argparse
from prepare import BASE, ROOT, bundle

parser=argparse.ArgumentParser()
parser.add_argument('--run',type=Path,help='Result directory to verify against current sources')
args=parser.parse_args()
run = args.run or BASE/'results'/json.loads((BASE/'results/CURRENT.json').read_text())['run']
run=run.resolve()
assert run.parent==(BASE/'results').resolve(), 'result must be a direct child of readiness results'
result = json.loads((run/'result.json').read_text())
assert result['status']=='PASS' and result['count']==20
assert len(set(result['checks']))==20
assert result['cleanup']=='removed-and-absence-verified'
assert result['sourceUnchanged'] and result['syntheticOnly'] and result['remoteMutations']==0
for name, digest in result['sources'].items():
    assert hashlib.sha256((ROOT/name).read_bytes()).hexdigest()==digest, name
for name, digest in result['artifacts'].items():
    assert hashlib.sha256((run/name).read_bytes()).hexdigest()==digest, name
assert (BASE/'private-schema.candidate.sql').read_text()==bundle()
print('PASS: final20-check result, all current source/raw artifact hashes and generated SQL')
