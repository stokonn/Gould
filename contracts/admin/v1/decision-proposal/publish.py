"""Publish only verified contract planning: normal push, append dashboard, one checkpoint."""
from pathlib import Path
import argparse, json, re, subprocess, tempfile
import verify
ROOT = verify.ROOT
BRANCH = 'verify/goldberg-0.11-stage-6-12-local'
REPO = 'stokonn/Goldberg'
BASELINE = '19f5642192148051a7b4eb5bc5efb1090192e3c0'
ALLOWED_DOCS = {
 'docs/planning/goldberg-0.11/stage-6/API-V1-DECISION-PROPOSAL.md',
 'docs/planning/goldberg-0.11/stage-6/STAGE7B-CONTRACT-READINESS.md',
 'docs/planning/goldberg-0.11/stage-6/HANDOFF-STAGE7.md',
 'docs/planning/goldberg-0.11/stage-6/CHECKLIST.md',
 'docs/planning/goldberg-0.11/STAGE-CHECKLIST.md',
 'docs/ai/STATE.md','docs/ai/HANDOFF.md','docs/ai/VERIFICATION.md',
 'docs/ai/API-V1-PROPOSAL-PUBLICATION.md',
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
 return f'''## Stage 6 → Stage 7B — PROPOSED API v1 decision package

Branch `{BRANCH}`, planning commit `{expected}`; baseline `{BASELINE}` preserved.
Purpose: prepare owner decisions and synthetic DTO fixtures, with no live HTTP or
privileged action activation. [Decision package](https://github.com/{REPO}/blob/{expected}/docs/planning/goldberg-0.11/stage-6/API-V1-DECISION-PROPOSAL.md).

- [x] Prepared G1 authoritative snapshot stats; G2 queue/search metadata; G3 scoped
  audit filters; G4 authorized current comparison; G5 explicit session/principal/
  rotation/logout; G6 explicit open/dual revisions/replay/conflicts; G7 versioned
  binding; G8 decimal-string int8; G9 canonical audit/counting; G11 UTF-8/policy limits.
- [x] 61 authored synthetic cases (37 positive / 24 negative), 15 verifier
  regressions PASS; eight authored protocol scenes checked; existing 41 primary / 29 comparison fixtures preserved and
  verified; 11 existing local contract tests PASS; agent policy and whitespace PASS.
  No HTTP/PG/Android execution or activation from this package.
- [x] G10 remains CLOSED. Prior 345 successful host executions / 313 tests / 205
  PG-backed bodies retained as historical verified baseline; not rerun.
- [ ] Owner O1–O8 decisions: lifecycle/logout/version mapping, lifetimes, dual
  revisions/receipt semantics, wire/int8/cursors/version compatibility, statistics
  timezone/window, search scope, exact field/source/license policy, future authorization.
- [ ] G1–G9/G11 integration BLOCKED; production auth/network/actions inactive;
  6.12 PARTIAL; 6.17 NOT FROZEN; 6.13–6.18 implementation not performed.
- [ ] Browser/fetch/restore/privacy/hosted/Android runtime acceptance and existing
  Q1/C4/zero-cost/legal gates remain OPEN. X10 OPEN; Stage8 pending.

No Stage7B edits, device operations, cloud provisioning, OAuth integration, merge,
deploy or release. Next: owner reviews O1–O8, then separately authorizes approved
implementation and shared integration tests. Stop at owner decision gate.

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
 marker=f'<!-- stage6-api-v1-proposal:{expected} -->'
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
 result={'sha':expected,'checkpoint':f'https://github.com/{REPO}/issues/31#issuecomment-{cid}','status':'PROPOSED; owner gate pending'}
 (receipt/'published.json').write_text(json.dumps(result,indent=2)+'\n'); print(json.dumps(result,indent=2))

if __name__=='__main__': main()
