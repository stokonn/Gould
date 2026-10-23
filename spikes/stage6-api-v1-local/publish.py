"""Guarded ordinary source push and one factual Issue31 M1 checkpoint; never deploys."""
from pathlib import Path
import argparse,json,re,subprocess,tempfile
import verify
ROOT=verify.ROOT
BRANCH='feature/goldberg-0.11-stage-6-api-v1-local'
REPO='stokonn/Goldberg'
BASELINE=verify.BASELINE
DOCS={'docs/planning/goldberg-0.11/STAGE-CHECKLIST.md','docs/planning/goldberg-0.11/stage-6/CHECKLIST.md','docs/planning/goldberg-0.11/stage-6/API-V1-LOCAL-M1.md','docs/ai/STATE.md','docs/ai/HANDOFF.md','docs/ai/VERIFICATION.md','docs/ai/STAGE6-API-M1-PUBLICATION.md'}

def cmd(*args):return subprocess.check_output(args,cwd=ROOT,text=True).strip()
def require(ok,message):
 if not ok:raise RuntimeError(message)
def local(expected):
 require(re.fullmatch('[0-9a-f]{40}',expected),'exact reviewed 40-character SHA required')
 require(cmd('git','branch','--show-current')==BRANCH,'wrong branch')
 require(cmd('git','rev-parse','HEAD')==expected,'HEAD differs')
 require(not cmd('git','status','--porcelain=v1'),'dirty worktree; do not publish')
 require(cmd('git','remote','get-url','origin') in ['https://github.com/stokonn/Goldberg.git','git@github.com:stokonn/Goldberg.git'],'unexpected origin')
 cmd('git','merge-base','--is-ancestor',BASELINE,expected)
 for path in cmd('git','diff','--name-only',BASELINE,expected).splitlines():
  require(path in DOCS or path=='.gitignore' or path.startswith('spikes/stage6-api-v1-local/'),'out-of-scope change: '+path)
 prior=subprocess.check_output(['git','show',BASELINE+':.gitignore'],cwd=ROOT)
 cache_ignore=b'# Isolated API adapter tests reuse verified local dependency caches (never committed).\n/spikes/stage6-schema-pg/node_modules\n/spikes/stage6-auth-local/node_modules\n'
 require((ROOT/'.gitignore').read_bytes()==prior+cache_ignore,'unexpected ignore changes')
 verify.check();cmd('git','diff','--check',BASELINE,expected)

def remote():
 lines=cmd('git','ls-remote','--heads','origin','refs/heads/'+BRANCH).splitlines()
 require(len(lines)<=1,'ambiguous branch');return lines[0].split()[0] if lines else None

def history():
 pages=json.loads(cmd('gh','api','--paginate','--slurp',f'repos/{REPO}/issues/31/comments'))
 return {str(c['id']):c['body'] for page in pages for c in page}

def payload(expected):
 return f'''## Stage 6 API v1 LOCAL implementation — Milestone1 (PARTIAL integration)

Branch `{BRANCH}`, source commit `{expected}`, exact approved baseline `{BASELINE}`.
[Local adapter and evidence](https://github.com/{REPO}/tree/{expected}/spikes/stage6-api-v1-local).

- [x] Isolated local in-process request/response adapter: strict routes/version/body,
  explicit AuthLocalApi/SessionService/CommunityApi mappings, safe errors,204 logout,
  correlated IDs and exact DTO projections. No listener or Android wiring.
- [x] Session challenge/exchange/session/capabilities/refresh/logout executable locally;
 15m access/7d absolute family/5m challenge, live authorization/revocation; controlled
  RSA/JWKS exchange tested with maximum60s provider skew.
- [x] Explicit open and approve/reject/request_changes with independent entity/proposal
  expectations;200/201/generic409, exact public receipts inside domain transaction,
  expiry24h from completion, changed intent422, authorization before replay.
- [x] Decimal int8 reads/strict range rejection, microsecond timestamps and scoped signed
  cursor codec15m. Unsupported full-int8 writes503 after live auth; safe-number increment
  exhaustion retains domain409. No narrowing/rounding or full-int8-write claim.
- [x] Executed106 adapter/wire tests (real services on scripted SQL + scalar/routing
  units, including27 request fixtures/all8 protocol scenes/controlled RSA),38 relevant
  T22 regressions and18 planning-verifier tests PASS. Before/after/current source hashes,
  raw TAP and policy/provenance checks recorded. Zero failures/skips/cancellations.
- [ ] PostgreSQL preflight EPERM;0 PG bodies. No HTTP/browser/Android/runtime proof.
  Prior345 executions/313 tests/205 PG-backed bodies preserved, not rerun.
- [ ] Latest detail/refetch DTO, full-int8 domain writes, actual PostgreSQL/public HTTP
  verification, client single-flight/account/204 integration remain OPEN.
- [ ] Public `/v1` compatibility unconfirmed: local/provisional only, no silent `/v2`.
  G1–G4 unimplemented; all real policies empty/default-deny; no production OAuth/cloud.

G10 CLOSED; G1–G9/G11 integration acceptance BLOCKED;6.12 PARTIAL/T22 LOCAL PASS;
6.17 NOT FROZEN; no formal6.13–6.18, Stage7B edits, devices, hosted/browser/production,
merge/deploy/release. Stage7A FINAL ACCEPTED; X10 unfixed/unverified, owner-deferred
Gould0.12; Stage8 readability/accessibility/touch-target gates retained.
Next smallest local milestone: bounded authoritative latest-submission detail/refetch
projection plus permitted disposable-PG verification of adapter/receipts; preserve all
remaining version/policy/activation gates. Original security branch/evidence untouched.

created by stokonn with love <3
'''

def write(method,path,body):
 with tempfile.NamedTemporaryFile('w',suffix='.json') as f:
  json.dump(body,f);f.flush();return json.loads(cmd('gh','api','--method',method,path,'--input',f.name))

def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('--expected-head',required=True);p.add_argument('--verify-only',action='store_true');a=p.parse_args();expected=a.expected_head
 local(expected)
 if a.verify_only:print('PASS: exact clean M1 SHA/scope/executed evidence; no publication');return
 previous=remote()
 if previous and previous!=expected:
  cmd('git','fetch','--no-tags','origin','refs/heads/'+BRANCH);cmd('git','merge-base','--is-ancestor',previous,expected)
 local(expected);cmd('git','push','origin',expected+':refs/heads/'+BRANCH)
 require(remote()==expected,'remote SHA differs; no checkpoint')
 issue=json.loads(cmd('gh','api',f'repos/{REPO}/issues/31'));before=history()
 marker=f'<!-- stage6-api-v1-local-m1:{expected} -->';matches=[k for k,b in before.items() if marker in b];require(len(matches)<=1,'duplicate marker; inspect')
 section=payload(expected)+'\nRemote SHA verified after ordinary push.\n'+marker+'\n'
 receipt=ROOT/'.gradle/stage6-api-m1-publication';receipt.mkdir(parents=True,exist_ok=True);(receipt/'issue-before.json').write_text(json.dumps(issue,indent=2)+'\n')
 require(issue['state']=='open','issue unexpectedly closed')
 if marker not in issue['body']:
  fresh=json.loads(cmd('gh','api',f'repos/{REPO}/issues/31'));require(fresh['body']==issue['body'],'concurrent description edit; inspect')
  result=write('PATCH',f'repos/{REPO}/issues/31',{'body':issue['body']+'\n\n'+section});require(result['body']==issue['body']+'\n\n'+section,'dashboard differs')
 if matches:cid=matches[0]
 else:cid=str(write('POST',f'repos/{REPO}/issues/31/comments',{'body':section})['id'])
 after=history();require(all(after.get(k)==v for k,v in before.items()),'prior comments changed; inspect');require(sum(marker in b for b in after.values())==1 and marker in after.get(cid,''),'checkpoint readback differs')
 final=json.loads(cmd('gh','api',f'repos/{REPO}/issues/31'));require(final['body'].startswith(issue['body']) and marker in final['body'],'description changed; inspect');require(remote()==expected,'remote moved; inspect')
 result={'sha':expected,'checkpoint':f'https://github.com/{REPO}/issues/31#issuecomment-{cid}','status':'LOCAL M1 only; integration PARTIAL/BLOCKED; version provisional'};(receipt/'published.json').write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result,indent=2))
if __name__=='__main__':main()
