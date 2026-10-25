"""Independent metadata/hash verification; no accepted test bodies or network."""
from pathlib import Path
import hashlib,json,subprocess
BASE=Path(__file__).resolve().parent;ROOT=BASE.parents[1]
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
pg=BASE/'results/goldberg-private-probe-a65fcef971334fd8a22a891f3454a4fb'
components=BASE/'results/component-final-8o7WsQ'
for folder in [pg,components]:
 r=json.loads((folder/'result.json').read_text());assert r['status']=='PASS' and r['sourceUnchanged']
 for name,h in r['artifacts'].items():assert sha(folder/name)==h,name
 for name,h in r['sources'].items():assert sha(ROOT/name if name.startswith('spikes/') else BASE/name)==h,name
r=json.loads((pg/'result.json').read_text());assert r['count']==18 and len(set(r['checks']))==18
assert r['cleanup']=='removed-and-absence-verified' and r['remoteWrites']==0
r=json.loads((components/'result.json').read_text());assert r['logicalComponentTests']==8 and r['localWorkerdRequests']==5 and r['hostedRequests']==0
c=json.loads((BASE/'catalog-observation.json').read_text())
assert c['function']['guard_matches'] and c['private_schema_absent'] and c['pg17']
assert c['external_ssl_enforced'] is None and c['api_exposed_schemas_observed'] is None
priv=json.loads((BASE/'public-privilege-observation.json').read_text())
assert priv['executor_is_function_owner']
assert not priv['public_schema_create_granted_to_public'] and not priv['database_create_granted_to_public']
for name,r in json.loads((BASE/'candidate-audit.json').read_text())['candidateFiles'].items():
 assert r['byte_identical_to_accepted_head'] and sha(ROOT/name)==r['sha256']
 assert (ROOT/name).read_bytes()==subprocess.check_output(['git','show','c8b903bbab147b239683c21204f524f7201849f0:'+name],cwd=ROOT)
print('PASS:18 new SQL checks,8 new component groups,5 local workerd requests,source/raw hashes and read-only catalog evaluation;UNKNOWN fields retained')
