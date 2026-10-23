"""Guarded normal Git push + one append-only Issue31 checkpoint. No build/deploy/release."""
from pathlib import Path
import argparse,hashlib,json,re,subprocess,tempfile
from verified_evidence import verify_host_evidence
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
 manifest(ROOT,old/'verified-host-source-sha256.json')
 manifest(old/'run-8SWyGyB0',old/'verified-host-artifacts-sha256.json')
 manifest(ROOT,ROOT/'docs/planning/goldberg-0.11/stage-6/results/android-local/source-sha256.json')
 manifest(ROOT,EVIDENCE/'current-verification-tools-sha256.json')
 verify_host_evidence(ROOT)
 matrix=json.loads((EVIDENCE/'coverage.json').read_text())
 require(matrix['counts']=={'PASS':13,'PARTIAL':6,'NOT TESTED':2,'FAIL':1},'threat findings differ')
 require(matrix['rows'][-1]['localStatus']=='FAIL','T22 must remain an explicit finding')
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
 body=f'''## Stage 6.12 — LOCAL security verification PARTIAL; source published

Branch `{BRANCH}`; exact remote HEAD `{expected}` verified after a normal push.
Original audit source `{AUDIT}`; owner-tested revision `faf9ba3ec24e7098289c80d6e119519ccc21b95e`.
Current commit adds verified host evidence and reporting/publication tooling fixes only.

- [x] Audited all T1–T22; matrix links168 exact successful test references and reviewed assertions.
- [x] Owner Fedora wrapper27 focused +32 unit PASS; nested PG17.11 Auth50 +handlers87
  +schema34 +integration34 +unit32 PASS. **296 successful executions;264 tests
  counting the repeated32 suite once;0 failures/skips/cancellations.** Five checks PASS.
- [x] All62 at-run source hashes matched before tool edits; immutable raw-run hashes checked.
  Nested stdout matches its five TAP/check logs; cleanup log records container removal.
  Numeric exit sidecar absent in tested v1: success uses owner report/result/control flow.
- [x] Version2 runner handles missing/duplicate/truncated TAP summaries with structured
  failure JSON, not NoneType traceback. **10 reporting regressions PASS** separately.
  Full version2 PostgreSQL rerun unexecuted; service/test/SQL/Android behavior unchanged.
- [x] Verified46 backend source +15 original host artifact +15 Android source hashes.
  Original host237 and Android JVM214 remain preserved historical baselines.
- [x] Matrix local assessment:13 PASS,6 PARTIAL,2 NOT TESTED,1 FAIL. Local PASS includes
  the new verified owner SQL execution; it does not close [I].
- [x] Fresh host PostgreSQL executed successfully; earlier dependency/setup/EPERM runs retained.
  Agent live Podman inventory remains denied by read-only runtime configuration.
- [ ] T22 exact-status finding: composed unknown audit-edit route returns400 rather than required403.
  No mutation handler dispatched or privilege bypass demonstrated. Passing probe records400;
  it does not waive the403 gate. No API/contract behavior changed.
- [ ] T13 rendering; T14 DNS/redirect/rebinding/fetch protections; T15 HTTP/browser;
  T19 actual restore/RPO; T16/T20 runtime usability; T21 actual network privacy remain OPEN.

[Exact threat matrix](https://github.com/{REPO}/blob/{expected}/docs/planning/goldberg-0.11/stage-6/LOCAL-SECURITY-MATRIX.md)
and raw `stage-6/results/security-local/run-vQd6iGn5/` retain names, assertions, counts,
source manifests and preflight. Successful probes are not full security acceptance.

Backend published9d7150c and Android published2f1524e preserved. Owner-confirmed instrumentation
APK packaging is PASS; zero Android instrumentation bodies, runtime acceptance still pending.
Emulator recovery deferred; no emulator/physical phone operated. Stage5/7A/X10/7B untouched.
No hosted provisioning/configuration, OAuth/real data, paid resources, merge/tag/release or6.13–6.18.
Formal6.12 and all cloud/release gates remain UNCHECKED/PARTIAL. No dashboard edit.

Next: explicitly review/reconcile T22 status semantics and the remaining untested local
assertions. Preserve hosted/Android gates. Do not repeat already successful host suites
merely to restate these results; full version2 runner execution remains unverified.
Hosted [I] remains separately authorized. Historical comments/description preserved.

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
