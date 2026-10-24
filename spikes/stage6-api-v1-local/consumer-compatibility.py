"""Read-only Stage7B source pins, exact static gaps and selected real Kotlin replay."""
from pathlib import Path
import hashlib,json,subprocess,sys,tempfile
ROOT=Path(__file__).resolve().parents[2];BASE=Path(__file__).resolve().parent
PKG=ROOT/'contracts/admin/shared/v1'
CONSUMER=Path('/home/stokonn/Projects/Goldberg-Stage7B')
COMMIT='0887d3fe33cf5d49d46e14e1de95168f08a493a2'
ADMIN='app/src/main/java/com/opus/piano/admin/'
SELECTED=['AdminApi.kt','AdminModels.kt','AdminWireValues.kt','AdminStatsService.kt']

def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def blob(name):return subprocess.check_output(['git','show',COMMIT+':'+name],cwd=ROOT)
def inventory():
 names=subprocess.check_output(['git','ls-tree','-r','--name-only',COMMIT,'--',ADMIN],cwd=ROOT,text=True).splitlines()
 names += ['app/src/main/java/com/opus/piano/OpusApplication.kt']
 return {n:hashlib.sha256(blob(n)).hexdigest() for n in sorted(names)}
def check():
 manifest=json.loads((PKG/'consumer-source.json').read_text())
 assert manifest['commit']==COMMIT and manifest['files']==inventory()
 for name,digest in manifest['files'].items():assert sha(CONSUMER/name)==digest,'consumer source changed since inspection: '+name
 def text(name):return blob(ADMIN+name).decode()
 facts={
  'dual-revisions-missing':'.put("expectedRevision", expectedRevision)' in text('AdminModels.kt') and '.put("expectedSubmissionRevision"' not in text('AdminModels.kt'),
  'revision-int-range':'value <= Int.MAX_VALUE' in text('AdminWireValues.kt'),
  'legacy-queue-fields':'id = o.optString("id")' in text('AdminModels.kt') and 'owner = o.optString("owner")' in text('AdminModels.kt'),
  'legacy-audit-ts':'unsignedLong(o, "ts")' in text('AdminModels.kt'),
  'logout-requires-json':'request("DELETE", "$V1/session", null).map { it.optBoolean("revoked") }' in text('AdminApi.kt'),
  'refresh-api-missing':'suspend fun refresh(' not in text('AdminApi.kt') and 'suspend fun challenge(' not in text('AdminApi.kt'),
  'comparison-open-api-missing':'suspend fun comparison(' not in text('AdminApi.kt') and 'suspend fun open(' not in text('AdminApi.kt'),
  'stats-legacy-zone-actions':'ZoneId.systemDefault()' in text('AdminStatsService.kt') and '"decision.approve" -> approved++' in text('AdminStatsService.kt'),
  'logout-clear-after-io':'return api.signOut()\n        } finally {\n            session.clear()' in text('AdminRepository.kt'),
  'conflict-never-retry':'never silently retry' in text('AdminReviewService.kt') and 'reReview = true' in text('AdminReviewService.kt'),
  'account-response-guard':'token() != requestToken' in text('AdminApi.kt'),
  'g10-disabled':'fixtureTransportOverride ?: DisabledAdminTransport' in text('AdminApi.kt') and 'decisionsEnabled = false' in blob('app/src/main/java/com/opus/piano/OpusApplication.kt').decode(),
 }
 assert all(facts.values()),[k for k,v in facts.items() if not v]
 print('PASS: pinned read-only consumer source and '+str(len(facts))+' actual static contract/boundary facts; source inspection is not Android runtime acceptance.')
 return facts

def replay(captures,out):
 facts=check();cache=Path.home()/'.gradle/caches/modules-2/files-2.1'
 def jar(group,module,version):
  matches=list((cache/group/module/version).glob('*/*.jar'));assert len(matches)==1,(group,module,version)
  return matches[0]
 compiler=[jar('org.jetbrains.kotlin','kotlin-compiler-embeddable','2.2.10'),jar('org.jetbrains.kotlin','kotlin-stdlib','2.2.10'),jar('org.jetbrains.kotlin','kotlin-script-runtime','2.2.10'),jar('org.jetbrains.kotlin','kotlin-reflect','1.6.10'),jar('org.jetbrains.kotlinx','kotlinx-coroutines-core-jvm','1.8.0'),jar('org.jetbrains','annotations','13.0')]
 runtime=[jar('org.jetbrains.kotlin','kotlin-stdlib','2.2.10'),jar('org.jetbrains.kotlinx','kotlinx-coroutines-core-jvm','1.10.2'),jar('org.json','json','20240303'),jar('org.jetbrains','annotations','13.0')]
 before={str(p):sha(p) for p in set(compiler+runtime)}
 with tempfile.TemporaryDirectory(prefix='goldberg-consumer-jvm-') as tmp:
  tmp=Path(tmp);sources=[]
  for name in SELECTED:
   target=tmp/name;target.write_bytes(blob(ADMIN+name));sources.append(str(target))
  output=tmp/'consumer.jar'
  cmd=['java','-cp',':'.join(map(str,compiler)),'org.jetbrains.kotlin.cli.jvm.K2JVMCompiler','-no-stdlib','-no-reflect','-jvm-target','17','-classpath',':'.join(map(str,runtime)),'-d',str(output),*sources,str(BASE/'tests/ConsumerCompatibilityProbe.kt')]
  built=subprocess.run(cmd,capture_output=True,text=True);(out.parent/'kotlin-compile.log').write_text(built.stdout+built.stderr)
  if built.returncode:raise RuntimeError('Kotlin compile failed; see kotlin-compile.log')
  executed=subprocess.run(['java','-cp',str(output)+':'+':'.join(map(str,runtime)),'com.opus.piano.admin.ConsumerCompatibilityProbeKt',str(captures),str(PKG/'consumer-cases.json')],capture_output=True,text=True)
  (out.parent/'kotlin-runtime.log').write_text(executed.stdout+executed.stderr)
  if executed.returncode:raise RuntimeError('Kotlin replay failed; see kotlin-runtime.log')
  result=json.loads(executed.stdout)
 assert before=={str(p):sha(p) for p in set(compiler+runtime)},'JVM dependencies changed during execution'
 check();result.update({'consumerCommit':COMMIT,'staticFacts':facts,'dependencySha256':before,'androidRuntimeExecuted':False})
 expected=json.loads((PKG/'consumer-cases.json').read_text())['cases']
 assert {c['id'] for c in result['cases']}=={c['id'] for c in expected} and result['executedCases']==len(expected)
 out.write_text(json.dumps(result,indent=2)+'\n');print('PASS: '+str(result['executedCases'])+' actual Kotlin JVM consumer compatibility cases; no Android runtime/network activation.')

if __name__=='__main__':
 if sys.argv[1:]==['--capture-source']:
  (PKG/'consumer-source.json').write_text(json.dumps({'commit':COMMIT,'branch':'feature/goldberg-0.11-stage-7b','worktreeInspectedReadOnly':str(CONSUMER),'files':inventory()},indent=2)+'\n');check()
 elif len(sys.argv)==4 and sys.argv[1]=='--replay':replay(Path(sys.argv[2]),Path(sys.argv[3]))
 elif len(sys.argv)==1:check()
 else:raise SystemExit('usage: consumer-compatibility.py [--capture-source | --replay CAPTURES OUTPUT]')
