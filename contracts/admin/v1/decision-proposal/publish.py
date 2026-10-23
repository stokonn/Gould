"""Publish only verified contract planning: normal push, append dashboard, one checkpoint."""
from pathlib import Path
import argparse, json, re, subprocess, tempfile
import verify
ROOT = verify.ROOT
BRANCH = 'verify/goldberg-0.11-stage-6-12-local'
REPO = 'stokonn/Goldberg'
BASELINE = 'fefa6e83c2a7b9c4c12660ff1542ab086356de88'
ALLOWED_DOCS = {
 'docs/planning/goldberg-0.11/stage-6/API-V1-DECISION-PROPOSAL.md',
 'docs/planning/goldberg-0.11/stage-6/STAGE7B-CONTRACT-READINESS.md',
 'docs/planning/goldberg-0.11/stage-6/HANDOFF-STAGE7.md',
 'docs/planning/goldberg-0.11/stage-6/CHECKLIST.md',
 'docs/planning/goldberg-0.11/STAGE-CHECKLIST.md',
 'docs/ai/STATE.md','docs/ai/HANDOFF.md','docs/ai/VERIFICATION.md',
 'docs/ai/API-V1-PROPOSAL-PUBLICATION.md',
 'docs/ai/DECISIONS.md',
 'docs/planning/goldberg-0.11/stage-6/OPEN-DECISIONS.md',
 'docs/planning/goldberg-0.11/stage-6/API-V1-OWNER-DECISIONS-2026-10-23.md',
 'docs/planning/goldberg-0.11/stage-6/STAGE7B-APPROVED-DESIGN-READINESS.md',
}

def command(*args):
 return subprocess.check_output(args,cwd=ROOT,text=True).strip()

def require(ok, message):
 if not ok: raise RuntimeError(message)

def local_guards(expected):
 require(re.fullmatch('[0-9a-f]{40}',expected),'exact 40-character SHA required')
 require(command('git','branch','--show-current')==BRANCH,'wrong branch')
 require(command('git','rev-parse','HEAD')==expected,'HEAD differs')
 require(not command('git','status','--porcelain=v1'),'working tree must be clean')
 require(command('git','remote','get-url','origin') in
         ['https://github.com/stokonn/Goldberg.git','git@github.com:stokonn/Goldberg.git'],'unexpected origin')
 command('git','merge-base','--is-ancestor',BASELINE,expected)
 for name in command('git','diff','--name-only',BASELINE,expected).splitlines():
  require(name in ALLOWED_DOCS or name.startswith('contracts/admin/v1/decision-proposal/'),
          'non-planning change: '+name)
 verify.check()
 subprocess.run(['python3','-m','unittest','discover','-s','contracts/admin/v1/decision-proposal','-p','test_*.py'],cwd=ROOT,check=True)
 subprocess.run(['git','diff','--check',BASELINE,expected],cwd=ROOT,check=True)

def remote():
 lines=command('git','ls-remote','--heads','origin','refs/heads/'+BRANCH).splitlines()
 require(len(lines)==1,'remote branch missing or ambiguous')
 return lines[0].split()[0]

def comments():
 pages=json.loads(command('gh','api','--paginate','--slurp',f'repos/{REPO}/issues/31/comments'))
 return {str(c['id']):c['body'] for page in pages for c in page}

def payload_text(expected):
 return f'''## API v1 — owner design approval O1–O8 recorded (implementation BLOCKED)

Branch `{BRANCH}`, exact planning commit `{expected}`; published proposal `{BASELINE}`
and checkpoint6371392069 preserved. [Dated decision record](https://github.com/{REPO}/blob/{expected}/docs/planning/goldberg-0.11/stage-6/API-V1-OWNER-DECISIONS-2026-10-23.md).

- [x] O1 explicit session/version mapper,204 logout and no persisted secret-bearing
  offline queue; no unconfirmed revocation claim. O2 access15m/family7d/challenge5m/
  skew≤60s, atomic/single-flight refresh and replay-family revocation approved by design.
- [x] O3 explicit open/independent revisions,200/201 typed results,409 refetch/reconfirm,
  live authorization and24h exact receipts approved. O4 decimal-string int8, UTC precision,
  DTO projections, scoped signed cursors15m and public404/405 approved AS DESIGN.
- [x] O5 Europe/Prague server civil-day statistics, UTC transport, DST/coherent snapshot,
  approval counted once, incomplete totals unavailable. O6 NFC, case-insensitive,
  diacritic-sensitive literal bounded search approved AS DESIGN.
- [x] O7 safety structure/1000 UTF-8-byte reason/current projection/strict allowlisting
  PARTIALLY APPROVED BY DESIGN. O8 documentation/fixture verification sequence approved.
- [x] Offline verification:61 original DTO scenes (37 positive/24 negative),8 authored
  protocol scenes,4 Prague civil/DST windows,10 CZ/EN/literal search cases;18 verifier
  regressions PASS. Original41 primary/29 comparison fixtures and provenance PASS;
  agent policy and whitespace PASS. These are planning checks, not server/runtime proof.
- [ ] `/v1` requires confirming no previously approved/deployed incompatible consumer;
  otherwise prepare `/v2` for separate owner approval. Query budgets await local measurement.
- [ ] Concrete real-world source/field/license/attribution/evidence policy remains empty/
  default-deny, unapproved. Synthetic policies grant no permission; no legal clearance.
- [ ] G1–G9/G11 integration BLOCKED; G10 CLOSED;6.12 PARTIAL/T22 LOCAL PASS;
  prior345 executions/313 tests/205 PG-backed bodies preserved, not rerun;6.17 NOT FROZEN.

No backend/Android implementation, HTTP/auth/network/action activation, formal6.13–6.18,
Stage7B tree edits, devices, cloud/OAuth production, merge, deployment or release.
Next: separate policy/version/budget decisions and explicit implementation authorization;
all remaining security/hosted/runtime/zero-cost/legal gates unchanged. Stage7B acceptance
BLOCKED, X10 OPEN, Stage8 pending. Prior proposal/checkpoints preserved as history.

created by stokonn with love <3
'''


def api_write(method, path, data):
 with tempfile.NamedTemporaryFile('w',suffix='.json') as f:
  json.dump(data,f); f.flush()
  return json.loads(command('gh','api','--method',method,path,'--input',f.name))

def main():
 parser=argparse.ArgumentParser(description=__doc__)
 parser.add_argument('--expected-head',required=True)
 parser.add_argument('--verify-only',action='store_true')
 args=parser.parse_args(); expected=args.expected_head
 local_guards(expected)
 if args.verify_only:
  print('PASS: exact clean planning-only SHA and offline checks; no publication'); return
 previous=remote()
 if previous!=expected:
  command('git','fetch','--no-tags','origin','refs/heads/'+BRANCH)
  command('git','merge-base','--is-ancestor',previous,expected)
 local_guards(expected)
 command('git','push','origin',expected+':refs/heads/'+BRANCH)
 require(remote()==expected,'remote SHA differs; no checkpoint')
 issue=json.loads(command('gh','api',f'repos/{REPO}/issues/31'))
 history=comments(); body=payload_text(expected)
 marker=f'<!-- stage6-api-v1-design-approval:{expected} -->'
 matches=[cid for cid,text in history.items() if marker in text]
 require(len(matches)<=1,'duplicate marker; inspect')
 require(issue['state']=='open','issue unexpectedly closed; inspect')
 # Preserve full pre-update description locally and append a current section only.
 receipt=ROOT/'.gradle/api-v1-proposal-publication'; receipt.mkdir(parents=True,exist_ok=True)
 (receipt/'issue-before.json').write_text(json.dumps(issue,indent=2)+'\n')
 section=body+'\n'+marker+'\n'
 if marker not in issue['body']:
  fresh=json.loads(command('gh','api',f'repos/{REPO}/issues/31'))
  require(fresh['body']==issue['body'],'concurrent dashboard change; rerun after inspection')
  updated=api_write('PATCH',f'repos/{REPO}/issues/31',{'body':issue['body']+'\n\n'+section})
  require(updated['body']==issue['body']+'\n\n'+section,'dashboard readback differs')
 if matches:
  cid=matches[0]
 else:
  posted=api_write('POST',f'repos/{REPO}/issues/31/comments',{'body':section}); cid=str(posted['id'])
 current=comments()
 require(all(current.get(k)==v for k,v in history.items()),'historical comments differ; inspect')
 require(sum(marker in v for v in current.values())==1,'checkpoint marker missing/duplicated')
 require(marker in current.get(cid,''),'checkpoint readback missing')
 final=json.loads(command('gh','api',f'repos/{REPO}/issues/31'))
 require(final['body'].startswith(issue['body']) and marker in final['body'],'dashboard changed; inspect')
 require(remote()==expected,'remote moved; inspect')
 result={'sha':expected,'checkpoint':f'https://github.com/{REPO}/issues/31#issuecomment-{cid}','status':'APPROVED DESIGN WITH QUALIFICATIONS; implementation and integration blocked'}
 (receipt/'published.json').write_text(json.dumps(result,indent=2)+'\n'); print(json.dumps(result,indent=2))

if __name__=='__main__': main()
