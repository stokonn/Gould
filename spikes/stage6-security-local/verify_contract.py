"""Local fixture/provenance verification. No network, PostgreSQL, Android or freeze."""
from pathlib import Path
import hashlib,json,subprocess
ROOT=Path(__file__).resolve().parents[2];BASE=ROOT/'contracts/admin/v1'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def check(check_client_worktree=True):
 corpus=json.loads((BASE/'fixtures.json').read_text());readiness=json.loads((BASE/'readiness.json').read_text());provenance=json.loads((BASE/'source-provenance.json').read_text())
 assert provenance['fixtureSetHash']==hashlib.sha256(json.dumps(provenance['fixtureFiles'],sort_keys=True,separators=(',',':')).encode()).hexdigest()
 assert provenance['sourceSetHash']==hashlib.sha256(json.dumps(provenance['files'],sort_keys=True,separators=(',',':')).encode()).hexdigest()
 assert corpus['frozen'] is False and corpus['httpApproved'] is False and corpus['active'] is False
 assert readiness['frozen'] is False and readiness['httpApproved'] is False
 assert len(corpus['cases'])==41 and len({c['id'] for c in corpus['cases']})==41
 assert sum(c['classification']=='actual-local-capture' for c in corpus['cases'])==30
 assert sum(c['classification']=='proposal-unapproved' for c in corpus['cases'])==11
 for c in corpus['cases']:
  assert c['synthetic'] is True and c['httpBinding'] is None
  if c['classification']=='proposal-unapproved':assert c['active'] is False and c['requiresApproval'] is True
  else:assert c['verification']['executed'] is True and c['verification']['postgres'] is False and c['verification']['http'] is False
 assert [r['id'] for r in readiness['dependencies']]==[f'G{i}' for i in range(1,12)]
 for row in readiness['dependencies']:
  assert all(row[k] for k in ['normative','actualLocalBackend','androidExpectation','mismatchOrMissing','requiredSharedFixtures','proposedResolution','approvalRequirement','verificationStatus'])
 assert readiness['dependencies'][9]['verificationStatus']=='CLOSED_ANDROID_STATIC_COMPATIBILITY_CONFIRMED'
 assert all(r['verificationStatus']=='BLOCKED' for r in readiness['dependencies'] if r['id']!='G10')
 for name,h in provenance['files'].items():assert sha(ROOT/name)==h,name
 for name,h in provenance['fixtureFiles'].items():assert sha(BASE/name)==h,name
 old=json.loads((BASE/'stage7b-comparison/source-provenance.json').read_text())
 for name,h in old['files'].items():assert hashlib.sha256(subprocess.check_output(['git','show',f"{old['commit']}:{name}"],cwd=ROOT)).hexdigest()==h,name
 client=json.loads((BASE/'stage7b-readonly-source-sha256.json').read_text());source=Path(client['worktree'])
 # Read only: never run a client build or update its index/configuration.
 if check_client_worktree:
  for name,h in client['files'].items():assert sha(source/name)==h,'inspected Stage7B bytes changed since read: '+name
  assert 'fixtureTransportOverride ?: DisabledAdminTransport' in (source/'app/src/main/java/com/opus/piano/admin/AdminApi.kt').read_text()
  assert 'decisionsEnabled = false' in (source/'app/src/main/java/com/opus/piano/OpusApplication.kt').read_text()
 approval=json.loads((ROOT/'docs/planning/goldberg-0.11/stage-6/results/security-local/t22-investigation/OWNER-APPROVAL.json').read_text());assert approval['approval']=='EXPLICIT_OWNER_APPROVED'
 print(('Pinned client provenance only; external worktree not consulted. ' if not check_client_worktree else 'Read-only current client bytes checked. ')+ 'PASS:41 primary cases (30 actual in-process/scripted-storage captures,11 inactive proposals),29 preserved authored cases; G1-G11 matrix; source/corpus/client hashes; G10 remains closed. No HTTP/PG/Android/freeze acceptance.')
if __name__=='__main__':check()
