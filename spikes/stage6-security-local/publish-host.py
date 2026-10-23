"""Guarded normal Git push + one append-only Issue31 checkpoint. No build/deploy/release."""
from pathlib import Path
import argparse,hashlib,json,re,subprocess,tempfile
from verified_evidence import verify_host_evidence,verify_historical_sources,APPROVED_ROUTING_EDITS
from verify_contract import check as verify_contract
from tap_report import parse_tap
from post_t22_evidence import verify as verify_post_t22
ROOT=Path(__file__).resolve().parents[2]
BRANCH='verify/goldberg-0.11-stage-6-12-local'
REPO='stokonn/Goldberg'
AUDIT='ebd813509c046c3b6119e2aed953f270ec7d8d92'
EVIDENCE=ROOT/'docs/planning/goldberg-0.11/stage-6/results/security-local'

def command(*args):return subprocess.check_output(args,cwd=ROOT,text=True).strip()
def require(condition,message):
 if not condition:raise RuntimeError(message)
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def manifest(base,path):
 for name,value in json.loads(path.read_text()).items():require(sha(base/name)==value,'hash mismatch: '+name)
def verify(expected):
 require(bool(re.fullmatch('[0-9a-f]{40}',expected)),'exact 40-character expected SHA required')
 require(command('git','branch','--show-current')==BRANCH,'wrong branch')
 require(command('git','rev-parse','HEAD')==expected,'HEAD differs from explicitly reviewed SHA')
 require(not command('git','status','--porcelain=v1'),'working tree must be clean')
 require(command('git','remote','get-url','origin') in ['https://github.com/stokonn/Goldberg.git','git@github.com:stokonn/Goldberg.git'],'unexpected origin')
 for ancestor in [AUDIT,'9d7150c5b3f20880fe4d587d9c3bfd65a0f612ce','2f1524e4083a98a64d5fa9790f3cdc002d1f8237']:
  command('git','merge-base','--is-ancestor',ancestor,expected)
 old=ROOT/'spikes/stage6-public-admin-abuse/results'
 verify_historical_sources(ROOT,old/'verified-host-source-sha256.json','9d7150c5b3f20880fe4d587d9c3bfd65a0f612ce',APPROVED_ROUTING_EDITS)
 manifest(old/'run-8SWyGyB0',old/'verified-host-artifacts-sha256.json')
 manifest(ROOT,ROOT/'docs/planning/goldberg-0.11/stage-6/results/android-local/source-sha256.json')
 manifest(ROOT,EVIDENCE/'current-verification-tools-sha256.json')
 verify_host_evidence(ROOT)
 verify_post_t22(ROOT)
 tool_log=(EVIDENCE/'post-t22-tools/unittest-final.log').read_text();require('Ran 17 tests' in tool_log and '\nOK\n' in tool_log,'post-T22 verifier regressions differ')
 verify_contract(check_client_worktree=False)
 current=EVIDENCE/'t22-investigation'
 for name,count in [('verified-t22.tap',38),('verified-contract.tap',11),('verified-focused.tap',27),('verified-unit.tap',32)]:parse_tap(current/name,count,True)
 report=(current/'verified-report.log').read_text();require('Ran 11 tests' in report and '\nOK\n' in report,'current reporting regressions differ')
 require(json.loads((current/'OWNER-APPROVAL.json').read_text())['approval']=='EXPLICIT_OWNER_APPROVED','owner decision not recorded')
 matrix=json.loads((EVIDENCE/'coverage.json').read_text())
 require(matrix['counts']=={'PASS':14,'PARTIAL':6,'NOT TESTED':2},'threat findings differ')
 require(matrix['rows'][-1]['localStatus']=='PASS','T22 local status must match post-patch executed evidence')
 require(not command('git','diff','bcc779685d79e99451796b3fa45657b3bca8dd99',expected,'--','app','scripts'),'unexpected Android/repository script changes')

def comments():
 pages=json.loads(command('gh','api','--paginate','--slurp',f'repos/{REPO}/issues/31/comments'))
 return {str(c['id']):c['body'] for page in pages for c in page}
def remote():
 lines=command('git','ls-remote','--heads','origin',f'refs/heads/{BRANCH}').splitlines()
 require(len(lines)<=1,'ambiguous remote ref')
 return lines[0].split()[0] if lines else None

def main():
 parser=argparse.ArgumentParser(description=__doc__)
 parser.add_argument('--expected-head',required=True)
 parser.add_argument('--verify-only',action='store_true',help='local read-only guards; no remote calls or publication')
 args=parser.parse_args();verify(args.expected_head)
 if args.verify_only:
  print('PASS: exact clean local revision, ancestry, evidence/source hashes and PARTIAL findings; no network/publication executed');return
 expected=args.expected_head
 prior=remote()
 if prior and prior!=expected:
  command('git','fetch','--no-tags','origin',f'refs/heads/{BRANCH}')
  command('git','merge-base','--is-ancestor',prior,expected)
 verify(expected)
 command('git','push','origin',f'{expected}:refs/heads/{BRANCH}')
 require(remote()==expected,'remote SHA readback differs; no checkpoint posted')
 before=json.loads(command('gh','api',f'repos/{REPO}/issues/31'))
 history=comments()
 marker=f'<!-- stage6-security-local-published:{expected} -->'
 matches=[key for key,body in history.items() if marker in body]
 require(len(matches)<=1,'duplicate marker already exists; inspect without creating another')
 body=f'''## Stage 6.12 — post-T22 Fedora v3 verified; T22 LOCAL PASS, overall PARTIAL

Branch `{BRANCH}`; exact remote HEAD `{expected}` verified after normal push.
Owner-tested published source `6d0abad3decf0b724c0e00f1b4e270bf8d244fba` preserved.

- [x] Outer run-x0nDSTXd:27 focused +32 unit +38 T22 +11 contract =108 PASS.
- [x] Matching nested PG17.11 run-TEmZzZQg:50 Auth +87 handler +34 schema
  +34 integration +32 unit =237 PASS. **345 successful executions;313 tests
  counting the repeated32 unit suite once;205 PostgreSQL-backed bodies.**
  Zero failures/skips/cancellations across all nine TAP files.
- [x] Explicit nested exit0; outer summaryErrors=[], checksOrTestsFailed=false,
  postgresBlocked=false, sourceUnchanged=true; all five checks PASS in both runners.
  Exact stdout linkage,82 at-run source hashes and31 immutable raw artifacts verified.
- [x] Removal log names goldberg-public-admin-abuse-3645015; container ID andPG17.11
  version retained. This is removal/exit evidence, not a new live inventory check.
- [x] T22 LOCAL PASS under explicit owner-approved400/401/403 clarification:
  physical handler/admin assertions require403 for absent audit mutation; live
  self-grant/revocation and restricted SQL/audit immutability tests pass.38 focused
  service/scripted-SQL cases remain clearly separate from database-backed tests.
- [x] Original T22 FAIL, all pre-patch296/264 evidence and prior checkpoints retained.
  Current matrix14 local PASS/6 PARTIAL/2 NOT TESTED; overall6.12 stays PARTIAL.
- [x] Stage7B readiness updated: G10 remains closed by pinned Android boundary;
  G1–G9/G11 remain BLOCKED for real integration. Current local backend proof does
  not approve shared HTTP DTOs, session/revision mapping, server features or6.17 freeze.
- [ ] Browser/CORS/CSRF, DNS/redirect/rebinding fetch protections, actual restore/RPO,
  network privacy, Android runtime and all applicable hosted checks remain OPEN.

[Exact matrix](https://github.com/{REPO}/blob/{expected}/docs/planning/goldberg-0.11/stage-6/LOCAL-SECURITY-MATRIX.md)
[Readiness](https://github.com/{REPO}/blob/{expected}/docs/planning/goldberg-0.11/stage-6/STAGE7B-CONTRACT-READINESS.md)
Raw runs and post-t22-independent-verification.json are retained under stage6 evidence.
Seventeen additional evidence/reporting regressions PASS separately from345 host executions.
Shared captures remain scripted-storage/fixture-only where labeled; no HTTP venue executed.

Production networking/actions disabled. No Stage7B edits, emulator/phone, hosted/OAuth/
real-data/paid/merge/tag/release or6.13–6.18 work. No dashboard edit; prior comments
and description preserved. Next: explicit G1–G9/G11 decisions/authorized implementation,
remaining local acceptance, separately authorized integration. No repeat of the successful
v3 PostgreSQL run merely to restate these facts.

created by stokonn with love <3
{marker}
'''
 if matches:comment_id=matches[0]
 else:
  with tempfile.NamedTemporaryFile('w',suffix='.json') as request:
   json.dump({'body':body},request);request.flush()
   posted=json.loads(command('gh','api','--method','POST',f'repos/{REPO}/issues/31/comments','--input',request.name));comment_id=str(posted['id'])
 after=json.loads(command('gh','api',f'repos/{REPO}/issues/31'))
 current=comments()
 require(before['body']==after['body'],'Issue description changed concurrently; inspect, never rewrite')
 require(all(current.get(k)==v for k,v in history.items()),'historical comments changed concurrently; inspect, never rewrite')
 require(marker in current.get(comment_id,''),'checkpoint readback missing')
 require(remote()==expected,'remote moved after publication; inspect')
 record={'remoteSha':expected,'checkpoint':f'https://github.com/{REPO}/issues/31#issuecomment-{comment_id}','priorCommentsPreserved':len(history),'status':'PARTIAL; local findings and integration gaps remain OPEN'}
 out=ROOT/'.gradle/stage612-publication';out.mkdir(parents=True,exist_ok=True)
 (out/'published-host.json').write_text(json.dumps(record,indent=2)+'\n');print(json.dumps(record,indent=2))

if __name__=='__main__':main()
