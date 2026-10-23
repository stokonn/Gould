"""Evidence index: exact successful TAP/JUnit names, assertion review, no inferred runtime passes."""
from pathlib import Path
import collections,fnmatch,hashlib,json,re,xml.etree.ElementTree as ET
from tap_report import parse_tap
from verified_evidence import verify_historical_sources,APPROVED_ROUTING_EDITS
ROOT=Path(__file__).resolve().parents[2]
BASE=ROOT/'docs/planning/goldberg-0.11/stage-6/results/security-local'
HOST='spikes/stage6-public-admin-abuse/results/run-TEmZzZQg'
RUN='docs/planning/goldberg-0.11/stage-6/results/security-local/run-x0nDSTXd'
files={
 'auth':('spikes/stage6-auth-local/tests/auth.test.mjs',HOST+'/auth-regression.tap','fresh owner Fedora PostgreSQL17.11'),
 'handlers':('spikes/stage6-handlers-local/tests/handlers.test.mjs',HOST+'/handlers-regression.tap','fresh owner Fedora PostgreSQL17.11'),
 'schema':('spikes/stage6-schema-pg/tests/schema.test.mjs',HOST+'/schema-regression.tap','fresh owner Fedora PostgreSQL17.11'),
 'pg':('spikes/stage6-public-admin-abuse/tests/postgres.test.mjs',HOST+'/postgres.tap','fresh owner Fedora PostgreSQL17.11'),
 'unit':('spikes/stage6-public-admin-abuse/tests/unit.test.mjs',RUN+'/unit.tap','fresh owner Fedora security wrapper'),
 'focused':('spikes/stage6-security-local/tests/security.test.mjs',RUN+'/focused.tap','fresh owner Fedora security wrapper'),
 't22':('spikes/stage6-security-local/tests/t22.test.mjs',RUN+'/t22.tap','fresh Node real service + scripted SQL; no PostgreSQL/HTTP'),
 'android':('app/src/test/java/com/opus/piano/community/CommunityRepositoryTest.kt','docs/planning/goldberg-0.11/stage-6/results/android-local/host-confirmed-1277bed/TEST-com.opus.piano.community.CommunityRepositoryTest.xml','prior owner Fedora JVM; no Android runtime')}
inventory={}
for suite,(source,log,venue) in files.items():
 text=(ROOT/log).read_text()
 if suite=='android':
  doc=ET.fromstring(text);assert doc.get('failures')=='0' and doc.get('errors')=='0'
  names=[e.get('name') for e in doc.findall('testcase') if e.find('failure') is None and e.find('error') is None and e.find('skipped') is None]
 else:
  assert not re.search(r'^not ok|failureType:|hookFailed',text,re.M),log
  names=re.findall(r'^ok \d+ - (.+)$',text,re.M)
  counts=parse_tap(ROOT/log,require_success=True)
  assert len(names)==counts['pass'],log
 inventory[suite]=[dict(file=source,test=name,log=log,venue=venue) for name in names]

def select(suite,pattern):
 result=[r for r in inventory[suite] if fnmatch.fnmatchcase(r['test'],pattern)]
 assert result,(suite,pattern)
 return result

def refs(*pairs):return [r for suite,pattern in pairs for r in select(suite,pattern)]
rows=[]
def row(n,title,status,covered,gaps,integration,evidence):
 rows.append(dict(threat=f'T{n}',scenario=title,localStatus=status,coveredAssertions=covered,missingAssertionsOrEvidence=gaps,requiresHostedIntegration=integration,remainingIntegration='OPEN' if integration else 'No hosted venue required by original row; listed unsupported/local checks remain OPEN',evidence=evidence))
row(1,'Expired session/access token','PASS','Exact access expiry rejects; actual handlers return401, publish nothing and omit token from logs. New generic denial probe asserts request.denied event and correlation ID.','No real HTTP/hosted session/logging check. Fresh owner SQL execution confirmed; no hosted expiry/logging execution.',True,refs(('auth','T1 exact access expiry*'),('handlers','malformed, expired and revoked*'),('focused','T1 denial envelope*'),('focused','T1/T4 independent verifier rejects expired')))
row(2,'Signature, kid, algorithms and JWKS cache/rotation','PASS','Signature/none/HS confusion rejected401; fake provider bounded/cold outage yields safe503; concurrent refresh coalesces; header cap, zero-age, exact expiry and unknown-kid cooldown verified with synthetic clock. Attacker jku/x5u/jwk rejected before resolver invocation.','Real JWKS rotation, transport/DNS and deployed cache window untested. Provider outage503 is deliberate fail-closed behavior, not a successful identity.',True,refs(('auth','T2*'),('focused','T2*')))
row(3,'Credential/refresh replay','PASS','Nonce mismatch/expiry/replay cannot mint second session; concurrent credential consumption has one winner. Refresh reuse commits family revocation/audit; even concurrent winner access is revoked.','Fresh owner local SQL execution; no Google/hosted replay venue.',True,refs(('auth','T3*')))
row(4,'Wrong audience or issuer','PASS','Exact issuer/audience, unexpected multi-audience and required claims rejected401 with real local JWT crypto; fresh SQL suite confirms no principal rows.','Hosted provider audience/issuer integration OPEN.',True,refs(('auth','T1/T4 strict claims reject wrong*'),('auth','strict audience is the exact*'),('focused','T1/T4 independent verifier rejects *audience'),('focused','T1/T4 independent verifier rejects wrong issuer')))
row(5,'Revoked moderator/suspended principal','PASS','Still-live sessions lose privileged capabilities on next request; suspension403 and role revocation403; integration wrappers use live authorization including replay.','Fresh owner SQL execution confirmed. No wall-clock production propagation claim.',False,refs(('auth','T5*'),('pg','role/suspension integration*'),('handlers','read role revocation is immediate*')))
row(6,'Role injection/mass assignment','PASS','Forged role/author/status rejected400 with exact snapshot equality; nested unknown fields rejected; role claims grant no authority; no self-approval.','Fresh owner SQL execution confirmed; no wire-layer deserialization/HTTP claim.',False,refs(('handlers','T6/T13 validation rejects unknown top field without effects'),('handlers','T6/T13 validation rejects nested role without effects'),('handlers','T6 self-approval*'),('auth','strict verified identity maps*'),('auth','T6 strict allowlist rejects*')))
row(7,'Guess another contributor pending ID','PASS','Other-owner and absent IDs both404 on private reads; own list empty; contributor queue denied.','Evidence is local in-process/SQL; no HTTP listener tested.',False,refs(('handlers','T7/T8 owner-only*'),('auth','T7/T8 ownership in query*')))
row(8,'Cross-user modification','PASS','Other-owner revise/withdraw both404; no revision/status effect; ownership SQL query hides rows.','No deployed PATCH route; local service operation is tested.',False,refs(('handlers','T7/T8 owner-only*'),('auth','T7/T8 ownership in query*')))
row(9,'REST/RPC privilege escalation','PARTIAL','Fresh restricted SQL roles cannot read private tables, mutate audit/head or invoke ungranted functions. SQL functions pin search_path=pg_catalog and revoke PUBLIC execution (source review). Local arbitrary RPC denied by Auth adapter. New composed admin route defers to injected authorization.','No real REST/RPC listener, hosted Supabase RLS/anonymous JWT or deployed function identity. SQL mocks are not hosted tests. The historical400/403 difference is corrected by an explicit owner-approved local patch; post-patch local PostgreSQL assertions now pass; HTTP/RPC binding remains untested, see T22.',True,refs(('auth','T9/T22 public database role*'),('auth','T9/T22 local transport*'),('schema','restricted local role*'),('focused','T9/T22 configured admin role*')))
row(10,'Concurrent moderator decisions','PASS','Two moderators exactly201/409; one immutable publication/feed. Same absent/existing entity contention compares revisions atomically. Admin quarantine/rollback distinct-key races exactly200/409.','Fresh owner PostgreSQL concurrency execution confirmed. No production load inference.',False,refs(('handlers','T10*'),('pg','admin.* replay, mismatch and concurrent distinct-key decisions')))
row(11,'Identical idempotent retry','PASS','Concurrent identical create/moderation replay stored response with one submission/publication; successful retries do not consume quota again.','Fresh owner SQL execution confirmed; no network-loss wire replay tested.',False,refs(('handlers','T11 same-key*'),('pg','daily limit atomic last slot*'),('pg','kill switch truthful*')))
row(12,'Key reused with changed body','PASS','Same key different payload/operation422; principals cannot reuse another stored result. Admin same-key changed reason422.','No HTTP transport integration; fresh owner PostgreSQL evidence confirmed.',False,refs(('handlers','T12 same key*'),('pg','admin.* replay, mismatch and concurrent distinct-key decisions')))
row(13,'Script/URL/oversize/unknown/deep input','PARTIAL','HTML/encoded HTML and unknown fields rejected400 with unchanged SQL snapshots. Node/string/byte/nesting/getter/cycle limits enforced before dispatch; no handler called in new probe.','Actual Android/browser rendering/escaping unexecuted. Compose Text source review cannot substitute runtime rendering. No HTTP parser-size limit assertion.',False,refs(('handlers','T6/T13 validation rejects HTML without effects'),('handlers','T6/T13 validation rejects encoded HTML without effects'),('handlers','boundary checks:*'),('focused','T13 malformed object*')))
row(14,'Evidence SSRF/private IP/rebinding/redirect','PARTIAL','Unsafe literal/private/metadata/local and non-reviewed URLs refused; empty policy denies even synthetic input. Evidence is stored only; no fetcher/proxy introduced.','No DNS resolve-then-check, rebinding, redirect revalidation, real fetch size/time tests. URL syntax/allowlist validation DOES NOT pass full T14. No unrestricted fetching may be enabled.',False,refs(('handlers','T14 refuses*'),('focused','T14 empty policy*'),('unit','fail-closed evidence policy*')))
row(15,'Browser CSRF/CORS','NOT TESTED','No current browser session/cookie/HTTP path exists. Older isolated C2 spike is not execution of this composed adapter.','No current CSRF mismatch403, SameSite=Strict cookie, Origin/CORS or browser scenario execution. Both current local HTTP and hosted/browser paths OPEN.',True,[])
row(16,'Principal/distributed IP quota exhaustion','PARTIAL','Daily/pending limits, principal last-slot and cross-account coarse-IP races; hashes/grouping; exact windows;429 Retry-After; kill503; storage failure503; rollback/retry counts; janitor preserves submissions/audit.','Android/Admin actual runtime usability and deployed distributed-IP/edge quota exhaustion untested. Key/retention fixtures synthetic, not legal retention policy.',True,refs(('pg','daily limit atomic last slot*'),('pg','coarse IP last slot*'),('pg','quota rollback on *'),('pg','quota boundary exactly*'),('pg','kill switch truthful*'),('pg','quota table unavailable*'),('pg','idempotency insert failure rolls quota*'),('pg','independent janitor prunes*'),('unit','keyed hashes*'),('unit','retention scheduler*')))
row(17,'Partial transaction failure','PASS','Approval fault boundaries locked/entity/snapshot/projection/approved/audit/feed/sql; idempotency insertion and deferred commit failure; exact snapshot equality then same-key success. Admin quarantine/rollback locked/projection/audit/feed/deferred; principal/quota faults atomic.','Fresh owner PostgreSQL fault-injection run confirmed. No claim of provider failure injection.',False,refs(('handlers','T17*'),('pg','admin.* T17 atomic rollback at *'),('pg','principal operation fault*'),('pg','quota rollback on *'),('schema','atomic publication rollback after *')))
row(18,'Audit tamper/update/delete','PASS','SQL immutable UPDATE/DELETE/TRUNCATE and restricted-role guards; exported copy modification/omission/order/head mismatch rejected by independent recomputation, including >9 sequences. No audit mutation handler dispatched.','Operator console/provider path [I] OPEN. Chain proves tamper evidence relative to trusted head, not protection from privileged full-history replacement. T22 records the historical mismatch and approved local correction; post-patch local PostgreSQL rerun now verified.',True,refs(('schema','immutable revision, published revision*'),('schema','F4 physical audit triggers*'),('handlers','T18 immutable publication*'),('handlers','audit export sorts int8*'),('unit','T18 independent audit*')))
row(19,'Export and isolated restore/RPO','NOT TESTED','Fresh F4 verifies an exported JSON audit copy; it does NOT restore a database.','No full fresh-empty-database restore, table/count/sample comparison, restored chain or RPO execution. Local and provider [I] OPEN; do not start 6.14 export/restore implementation here.',True,refs(('schema','F4 physical audit triggers*')))
row(20,'Unavailable/paused/quota backend graceful behavior','PARTIAL','Backend safe503 and kill-switch read availability; JVM fake transport retains drafts, error taxonomy, explicit stale cache and disabled production transport without startup fetch.','No Android cold startup/process death/UI or Admin runtime, no deployed pause/exhaustion. JVM file-store recreation is not Android runtime. Emulator recovery explicitly deferred.',True,refs(('unit','T20 unavailable PostgreSQL connector*'),('pg','kill switch truthful*'),('pg','quota table unavailable*'),('android','everyServerErrorRetainsDraftAndMapsDistinctly'),('android','disabledProductionBoundaryNeverSendsAndRetainsDraft'),('android','repositoryConstructionAndLocalEditingDoNotUploadOrFetch'),('android','cachedOverlayIsExplicitlyStaleSyntheticAndSurvivesRestart')))
row(21,'Personal-data isolation','PARTIAL','Server private fields rejected at root/target/field/evidence; fresh SQL no submissions; Kotlin explicit nested DTO allowlists and credential redaction executed in JVM fixtures; fresh additional server private-field probes.','Actual network-layer assertion absent: production transport disabled. Zero personal rows leaving device cannot be claimed from DTO/fixture assertions. Android/runtime venue deferred; no real data needed.',False,refs(('handlers','T21 rejects personal*'),('focused','T21 server rejects*'),('android','dtoUsesExplicitNestedAllowlists'),('android','privateObjectSmugglingFailsAtEveryPayloadLevel'),('android','credentialsAreRedactedAndNotInFixtureCallsOrPersistedDrafts')))
row(22,'Bootstrap self-grant/audit self-edit','PASS','LOCAL PASS under explicit owner-approved clarification. Post-patch PG17.11 Auth tests deny self-grant/case bypass and anonymous SQL/table/function escalation; handlers and admin audit tests explicitly assert403 on missing audit-edit routes and physical immutable history remains protected. Host38 real-service/scripted-SQL cases cover absent operations403/no dispatch, malformed operations400, registered auth401/capability403, safe503 and no protected writes. All executed at verified6d0abad source.','Local in-process scope only. No HTTP listener, browser, hosted bootstrap/console or Android path executed. Original400-versus403 FAIL, approval, patch and earlier PARTIAL checkpoints remain historical; no public integration or formal Stage6 acceptance implied.',False,refs(('auth','T22 admin cannot self-grant*'),('auth','T9/T22 public database role*'),('auth','T9/T22 local transport*'),('auth','self-grant and self-suspension*'),('handlers','T18 immutable publication*'),('schema','immutable revision, published revision*'),('pg','role/suspension integration*'),('pg','admin audit paginates*'),('t22','*')))

assert len(rows)==22 and [r['threat'] for r in rows]==[f'T{i}' for i in range(1,23)]
# Verify original signed-in-source baselines instead of silently regenerating their historical manifests.
old=ROOT/'spikes/stage6-public-admin-abuse/results'
source=json.loads((old/'verified-host-source-sha256.json').read_text())
verify_historical_sources(ROOT,old/'verified-host-source-sha256.json','9d7150c5b3f20880fe4d587d9c3bfd65a0f612ce',APPROVED_ROUTING_EDITS)
artifacts=json.loads((old/'verified-host-artifacts-sha256.json').read_text())
for name,sha in artifacts.items():assert hashlib.sha256((old/'run-8SWyGyB0'/name).read_bytes()).hexdigest()==sha,name
android=json.loads((ROOT/'docs/planning/goldberg-0.11/stage-6/results/android-local/source-sha256.json').read_text())
for name,sha in android.items():assert hashlib.sha256((ROOT/name).read_bytes()).hexdigest()==sha,name
summary=dict(collections.Counter(r['localStatus'] for r in rows))
doc=dict(overall='PARTIAL; formal 6.12 unchecked',sourceBase='6d0abad3decf0b724c0e00f1b4e270bf8d244fba',backendPublished='9d7150c5b3f20880fe4d587d9c3bfd65a0f612ce',androidPublished='2f1524e4083a98a64d5fa9790f3cdc002d1f8237',verifiedBaselineSourceHashes=len(source),verifiedBaselineArtifactHashes=len(artifacts),verifiedAndroidSourceHashes=len(android),counts=summary,interpretation='PASS refers only to reviewed local assertions with exact executed evidence; historical owner executions are not fresh reruns or full [I] acceptance. Probe PASS is not threat PASS.',rows=rows)
(BASE/'coverage.json').write_text(json.dumps(doc,indent=2)+'\n')
(BASE/'test-inventory.json').write_text(json.dumps(inventory,indent=2)+'\n')
md='''# Stage 6.12 — local threat-coverage matrix

**Overall PARTIAL; formal6.12 remains unchecked.** T22 LOCAL PASS under the explicit
owner-approved local routing clarification. Browser/HTTP/hosted/SSRF-fetch/restore/
network-privacy/Android-runtime portions remain OPEN. No6.13–6.18 or6.17 freeze.

Owner-tested source `6d0abad3decf0b724c0e00f1b4e270bf8d244fba`, version3 runner:
`results/security-local/run-x0nDSTXd/` matches nested PG17.11
`spikes/stage6-public-admin-abuse/results/run-TEmZzZQg/` exactly by stdout linkage.
Outer108 PASS (27 focused +32 unit +38 T22 +11 contract); nested237 PASS
(50 Auth +87 handler +34 schema +34 integration +32 unit).
**345 successful executions;313 tests counting repeated32 unit suite once.**
There are205 PostgreSQL-backed bodies; the other140 executions are socket-free
Node tests (including repeated units). Zero failures/skips/cancellations, all five
checks PASS in both runners, explicit nested exit0,82 source hashes verified.

Cleanup log records removal of goldberg-public-admin-abuse-3645015, matching its
uniquely named disposable runner. This is retained removal evidence, not a new live
agent Podman inventory check. Earlier296 pre-patch executions, all FAIL/PARTIAL
snapshots, immutable raw logs and prior manifests are preserved.

T22 now satisfies the explicitly approved local acceptance cases with current
routing models AND physical PostgreSQL assertions; this does not turn a model
case into a database case. Shared contract fixtures remain captures over scripted
storage or inactive proposals, never HTTP/PostgreSQL integration proof. Prior Android
JVM214 and reporting-tool runs are separate retained evidence, not this host run.

Historical46 backend hashes remain Git-verifiable for approved routing/test changes;
original15 artifacts/15 Android-source hashes unchanged. Current82-source snapshot
has no production/service/fixture changes during this reconciliation; only explicitly
identified evidence/publication/documentation metadata may change afterward.

[Coverage](results/security-local/coverage.json) and
[test inventory](results/security-local/test-inventory.json) identify exact successful
file/test/log/venue references. Current local statuses: '''+json.dumps(summary)+'''.

| Threat | Local status | Hosted [I] remaining | Assertions / gaps |
|---|---|---|---|
'''
for r in rows:
 md+='| '+r['threat']+' — '+r['scenario']+' | '+r['localStatus']+' | '+('OPEN' if r['requiresHostedIntegration'] else 'Not required by original row; other gaps below')+' | '+r['coveredAssertions'].replace('|','/')+' **Missing:** '+r['missingAssertionsOrEvidence'].replace('|','/')+' |\n'
md+='\n## Exact executed evidence by threat\n\n'
for r in rows:
 md+='### '+r['threat']+' — '+r['localStatus']+'\n\n'
 if not r['evidence']:md+='No current candidate test executed for this principal scenario.\n'
 for e in r['evidence']:
  md+=f"- `{e['file']}` — **{e['test']}**; {e['venue']}; `{e['log']}`.\n"
md+='\n## Remaining work / exact next action\n\n1. Preserve the successful version3 run and immutable new/old source/artifact records.\n   Do not repeat it merely to restate this verified evidence.\n2. T22 is LOCAL PASS under the approved clarification and current routing/physical\n   PostgreSQL assertions. HTTP binding, hosted bootstrap and actual clients untested.\n3. Keep T13 rendering, T14 real fetch protections, T15 browser/HTTP, T19 actual\n   restore/RPO, T16/T20 client runtime and T21 network assertions OPEN.\n4. G10 remains closed on Android; G1-G9/G11 remain blocked for real integration.\n   No wire dialect, endpoint, revision conversion or API freeze is approved here.\n5. Overall6.12 remains PARTIAL; no6.13–6.18, provisioning, merge/tag/deploy/release.\n\ncreated by stokonn with love <3\n'

(ROOT/'docs/planning/goldberg-0.11/stage-6/LOCAL-SECURITY-MATRIX.md').write_text(md)
print(json.dumps({'matrixRows':len(rows),'counts':summary,'sourceHashes':len(source),'artifactHashes':len(artifacts),'androidSourceHashes':len(android),'evidenceReferences':sum(len(r['evidence']) for r in rows)},indent=2))
