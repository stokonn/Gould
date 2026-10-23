"""Guarded normal Git push + one append-only Issue31 checkpoint. No build/deploy/release."""
from pathlib import Path
import argparse,hashlib,json,re,subprocess,tempfile
from verified_evidence import verify_host_evidence,verify_historical_sources,APPROVED_ROUTING_EDITS
from verify_contract import check as verify_contract
from tap_report import parse_tap
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
 verify_contract(check_client_worktree=False)
 current=EVIDENCE/'t22-investigation'
 for name,count in [('verified-t22.tap',38),('verified-contract.tap',11),('verified-focused.tap',27),('verified-unit.tap',32)]:parse_tap(current/name,count,True)
 report=(current/'verified-report.log').read_text();require('Ran 11 tests' in report and '\nOK\n' in report,'current reporting regressions differ')
 require(json.loads((current/'OWNER-APPROVAL.json').read_text())['approval']=='EXPLICIT_OWNER_APPROVED','owner decision not recorded')
 matrix=json.loads((EVIDENCE/'coverage.json').read_text())
 require(matrix['counts']=={'PASS':13,'PARTIAL':7,'NOT TESTED':2},'threat findings differ')
 require(matrix['rows'][-1]['localStatus']=='PARTIAL','T22 must remain pending real PostgreSQL/HTTP verification')
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
 body=f'''## Stage 6.12 — owner-approved T22 correction and Stage7B contract handoff (PARTIAL)

Branch `{BRANCH}`; exact remote HEAD `{expected}` verified after normal push.
Published baseline e7dc7ea and checkpoint6369071469 preserved; no history rewrite.

- [x] Reproduced old composed400 before auth/dispatch versus Auth adapter403.
  Owner explicitly approved: malformed/non-string/empty operation400; well-formed
  unregistered audit/bootstrap operation403 without dispatch; registered missing/
  invalid session401; capability/self-grant403. No privileged endpoint added.
- [x] Applied approved local patch;38 focused real-service/scripted-SQL checks PASS.
  Current27 security +32 unit +11 shared-contract +11 reporting regressions PASS:
  **119 executed local tests,0 failed/skipped/cancelled in final runs.** Earlier
  harness failures retained; no protected role/audit mutation observed. This is not
  actual HTTP or current PostgreSQL execution.
- [x] Owner-confirmed prior PG17.11 baseline296 executions/264 tests counting the
  repeated32 suite once preserved. It is pre-patch evidence, not a new regression
  run against the changed routing assertions. Original manifests checked via pinned
  history for explicitly approved changes; current code/corpus hashes recorded.
- [x] T22 now PARTIAL after explicit justified owner decision and local checks;
  original FAIL evidence preserved. Matrix13 local PASS/7 PARTIAL/2 NOT TESTED.
- [x] Stage6→7B handoff compares normative design/current backend/actual Android/
  unapproved proposals acrossG1–G11. G10 remains closed on Android (read-only static
  compatibility); G1–G9/G11 blocked for real integration. No Stage7B files changed.
- [x] Shared versioned corpus:30 actual in-process/scripted-storage captures +11
  inactive proposals +29 preserved authored Stage7B comparison scenes. All synthetic,
  no HTTP binding, no API freeze. Exact provenance and fixture hashes recorded.
- [ ] Updated real PostgreSQL suites not executed here: socket preflightEPERM,
  zero database bodies. Re-run new assertions on permitted local host.
- [ ] G1–G4 server functionality andG5–G9/G11 contract/implementation decisions;
  hosted/browser/fetch/restore/network-privacy/Android-runtime acceptance OPEN.

[Contract readiness](https://github.com/{REPO}/blob/{expected}/docs/planning/goldberg-0.11/stage-6/STAGE7B-CONTRACT-READINESS.md)
[Shared fixtures](https://github.com/{REPO}/tree/{expected}/contracts/admin/v1)
[Threat matrix](https://github.com/{REPO}/blob/{expected}/docs/planning/goldberg-0.11/stage-6/LOCAL-SECURITY-MATRIX.md)

Production networking and privileged Android actions remain disabled. Formal6.12
PARTIAL;6.17 freeze NOT completed. No6.13–6.18, hosted/OAuth/real-data/paid/merge/tag/
release/emulator/phone operations. Issue description and historical comments preserved.
Next: permitted local PostgreSQL rerun, owner review of exact shared DTO/binding/
revision/session/validation decisions; no dialect guessing or live activation.

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
