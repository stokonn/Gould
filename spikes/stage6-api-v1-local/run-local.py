"""Focused local-only tests and contemporaneous source binding; never starts a DB/listener."""
from pathlib import Path
import hashlib,json,platform,socket,subprocess,sys,tempfile
ROOT=Path(__file__).resolve().parents[2]
BASE=Path(__file__).resolve().parent

def sources():
 paths=[p for folder in ['stage6-api-v1-local','stage6-auth-local','stage6-handlers-local','stage6-schema-pg','stage6-public-admin-abuse'] for p in (ROOT/'spikes'/folder/'src').glob('*.mjs')]
 paths+=list((BASE/'tests').glob('*.mjs'))+list(BASE.glob('*.py'))+[BASE/'package.json']
 paths += [ROOT/'spikes/stage6-security-local/tests'/n for n in ['t22.test.mjs','t22-fixture.mjs']]
 paths += [ROOT/'spikes'/f/'package-lock.json' for f in ['stage6-auth-local','stage6-schema-pg']]
 paths += list((ROOT/'contracts/admin/v1/decision-proposal').glob('*.json'))
 return {str(p.relative_to(ROOT)):hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(set(paths))}

def main():
 run=Path(tempfile.mkdtemp(prefix='run-',dir=BASE/'results'))
 before=sources();(run/'source-before.json').write_text(json.dumps(before,indent=2)+'\n')
 try:
  with socket.socket() as probe:probe.bind(('127.0.0.1',0));probe.listen(1)
  preflight={'localTcp':'available','databaseBodiesExecuted':0,'note':'This focused runner never starts PostgreSQL; an available socket is not DB proof.'}
 except OSError as error:
  preflight={'localTcp':'blocked','errno':error.errno,'error':error.strerror,'databaseBodiesExecuted':0}
 (run/'postgres-preflight.json').write_text(json.dumps(preflight,indent=2)+'\n')
 commands=[('adapter.tap',['node','spikes/stage6-api-v1-local/tests/adapter.test.mjs']),('t22.tap',['node','spikes/stage6-security-local/tests/t22.test.mjs']),('design.log',['python3','contracts/admin/v1/decision-proposal/verify.py']),('design-tests.log',['python3','-m','unittest','discover','-s','contracts/admin/v1/decision-proposal','-p','test_*.py']),('original-contract.log',['python3','spikes/stage6-security-local/verify_contract.py']),('agent-policy.log',['python3','scripts/check_agent_policy.py']),('whitespace.log',['git','diff','--check'])]
 results=[]
 for name,command in commands:
  p=subprocess.run(command,cwd=ROOT,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True)
  (run/name).write_text(p.stdout);results.append({'log':name,'command':command,'exitCode':p.returncode})
 after=sources();(run/'source-after.json').write_text(json.dumps(after,indent=2)+'\n')
 result={'classification':'LOCAL_IN_PROCESS_SCRIPTED_SQL_AND_UNIT_ONLY','sourceUnchanged':before==after,'commands':results,'node':subprocess.check_output(['node','--version'],text=True).strip(),'python':platform.python_version(),'postgres':preflight,'httpRequestsExecuted':0,'androidBodiesExecuted':0,'prior345Rerun':False}
 (run/'result.json').write_text(json.dumps(result,indent=2)+'\n')
 (BASE/'results/LATEST.json').write_text(json.dumps({'run':str(run.relative_to(ROOT))},indent=2)+'\n')
 print(json.dumps({'run':str(run.relative_to(ROOT)),'commandsPassed':all(x['exitCode']==0 for x in results),'sourceUnchanged':before==after,'postgres':preflight},indent=2))
 return 0 if before==after and all(x['exitCode']==0 for x in results) else 1
if __name__=='__main__':sys.exit(main())
