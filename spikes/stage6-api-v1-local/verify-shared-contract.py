"""Independent shared fixture/consumer/query-plan verification; no Android activation."""
from pathlib import Path
import collections,hashlib,importlib.util,json,math,sys
ROOT=Path(__file__).resolve().parents[2];PKG=ROOT/'contracts/admin/shared/v1'
spec=importlib.util.spec_from_file_location('approved_dto',ROOT/'contracts/admin/v1/decision-proposal/verify.py')
DTO=importlib.util.module_from_spec(spec);spec.loader.exec_module(DTO)
DEFS=DTO.read(ROOT/'contracts/admin/v1/decision-proposal/dto.schema.json')['$defs']
def response(kind,status,value):
 if kind=='EmptyBody':assert status==204 and value is None
 else:DTO.validate(value,DEFS[kind],DEFS,response=True)
def verify(run):
 definition=json.loads((PKG/'scenarios.json').read_text());data=json.loads((run/'shared-contract-captures.json').read_text())
 cases={c['id']:c for c in definition['cases']};captured={c['id']:c for c in data['captures']}
 assert len(captured)==len(data['captures'])==len(cases) and captured.keys()==cases.keys()
 assert definition['publicVersionSelected'] is False
 for id,c in captured.items():
  assert c['origin']=='actual-loopback-http-postgresql' and (c['status'],c['dto'])==(cases[id]['status'],cases[id]['dto'])
  response(c['dto'],c['status'],c['response'])
  assert len(c['rawSha256'])==64 and (c['bodyBytes']==0)==(c['status']==204)
 assert len(data['transportFailures'])==2 and sorted(x['status'] for x in data['transportFailures'])==[200,201]
 assert all('destroyed after HTTP headers' in x['mechanism'] for x in data['transportFailures'])
 kotlin=json.loads((run/'consumer-results.json').read_text());expected=json.loads((PKG/'consumer-cases.json').read_text())['cases']
 actual={c['id']:c for c in kotlin['cases']};assert len(actual)==kotlin['executedCases']==len(expected)
 for c in expected:
  assert actual[c['id']]['actualOutcome']==c['expectedOutcome'] and actual[c['id']]['classification']==c['classification']
 assert kotlin['androidRuntimeExecuted'] is False and all(kotlin['staticFacts'].values())
 for p,h in kotlin['dependencySha256'].items():assert hashlib.sha256(Path(p).read_bytes()).hexdigest()==h,p
 plans=json.loads((run/'query-plans.json').read_text());assert plans['syntheticOnly'] and plans['indexChanges'] is False and plans['datasetSubmissions']==5000
 assert [p['label'] for p in plans['plans']]==['queue-default','queue-keyset','queue-filtered','audit-filtered','stats-exact']
 summary=[]
 for p in plans['plans']:
  assert p['statementTimeout']=='5s' and p['datasetSubmissions']==5000
  plan=p['explain'][0];assert plan['Execution Time']>=0 and math.isfinite(plan['Execution Time'])
  if p['label']!='stats-exact':assert p['args'][-1]==21 and plan['Plan']['Actual Rows']<=21
  if p['label'].startswith('queue'):
   def nodes(n):return [n]+[z for child in n.get('Plans',[]) for z in nodes(child)]
   scans=[n for n in nodes(plan['Plan']) if n.get('Relation Name')=='submission_revisions']
   assert scans and all(n['Actual Rows']*n['Actual Loops']<=21 for n in scans), 'revision work exceeds bounded page'
  summary.append({'label':p['label'],'planningMs':plan['Planning Time'],'executionMs':plan['Execution Time'],'root':plan['Plan']['Node Type']})
 matrix=json.loads((PKG/'compatibility-matrix.json').read_text())['rows']
 assert len(matrix)==len({row['id'] for row in matrix})
 allowed={'VERIFIED COMPATIBLE','VERIFIED INCOMPATIBLE','NOT EXECUTED','BLOCKED BY OWNER/POLICY','NOT APPLICABLE'}
 for row in matrix:
  assert row['result'] in allowed and all(row[k] for k in ['consumerExpectation','normativeDesign','verificationMechanism','remediation'])
  if row.get('actualBackendFixture'):assert row['actualBackendFixture'] in cases
  if row.get('probeId'):assert row['result']==actual[row['probeId']]['classification']
  if row.get('staticFact'):assert kotlin['staticFacts'][row['staticFact']] is True
 version=json.loads((PKG/'version-evidence.json').read_text())
 assert version['publicVersionSelected'] is False and version['ownerVersionDecisionRequired'] and version['unknownExternalOrDeployedConsumerState'] and version['confirmedGlobalConsumerAbsence'] is False
 assert version['consumerCommit']==kotlin['consumerCommit']
 contract=json.loads((PKG/'contract.json').read_text());assert contract['publicApiVersionSelected'] is False
 assert contract['normativeSchema']=='../../v1/decision-proposal/dto.schema.json'
 print('PASS: '+str(len(matrix))+' compatibility rows: '+json.dumps(dict(collections.Counter(x['result'] for x in matrix)))+'; external/public-version gate remains open.')
 print(f'PASS: {len(captured)} maintained real HTTP/PG fixture captures;2 actual dropped response streams; schema expectations match.')
 print('PASS: '+str(len(actual))+' actual Kotlin JVM replay cases: '+json.dumps(dict(collections.Counter(x['classification'] for x in actual.values())))+'; Android runtime not executed.')
 print('PASS: synthetic5000-row actual-query EXPLAIN/caps/5s timeout evidence: '+json.dumps(summary))
if __name__=='__main__':
 if len(sys.argv)==4 and sys.argv[1]=='--response':
  kind,status=sys.argv[2],int(sys.argv[3]);response(kind,status,json.load(sys.stdin))
 elif len(sys.argv)==2:verify(Path(sys.argv[1]))
 else:raise SystemExit('usage: verify-shared-contract.py RUN | --response DTO STATUS (JSON stdin)')
