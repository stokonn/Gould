import {test,before,after} from 'node:test';
import assert from 'node:assert/strict';
import {randomUUID} from 'node:crypto';
import {localClient,identifier} from '../../stage6-schema-pg/src/database.mjs';
import {exportAudit,verifyAudit} from '../src/audit.mjs';
import {insertResult} from '../../stage6-schema-pg/src/transactions.mjs';
import {migrateAuth} from '../../stage6-auth-local/src/storage.mjs';
import {IdentityVerifier} from '../../stage6-auth-local/src/identity.mjs';
import {SessionService} from '../../stage6-auth-local/src/sessions.mjs';
import {Authorization} from '../../stage6-auth-local/src/authorization.mjs';
import {FakeClock,FakeJwks,makeKey,credential,issuer,audience} from '../../stage6-auth-local/tests/fixtures.mjs';
import {Validator,bounded,safeUrl} from '../src/validation.mjs';
import {Idempotency,canonical} from '../src/idempotency.mjs';
import {SubmissionService} from '../src/submissions.mjs';
import {ModerationService} from '../src/moderation.mjs';
import {CommunityApi} from '../src/local-api.mjs';
let db,key;const schemas=[];
const fieldNames=Array.from({length:20},(_,i)=>i===0?'title':`fixtureField${i}`);
const policy={fields:{work:Object.fromEntries(fieldNames.map(n=>[n,{type:'text',maxBytes:2048}]))},intendedLicenses:['CC0-1.0'],sources:[{type:'source_url',value:'https://evidence.synthetic.invalid/source',license:'CC0-1.0',kind:'work',fields:fieldNames,intendedLicenses:['CC0-1.0']},{type:'reference',value:'Goldberg authored synthetic fixture',license:'CC0-1.0',kind:'work',fields:fieldNames,intendedLicenses:['CC0-1.0']}]};
const payload=(entity=randomUUID())=>({schemaVersion:1,target:{kind:'work',catalogueUuid:entity},changeKind:'new_metadata',fields:[{field:'title',newValue:'Synthetic catalogue title',evidence:[{type:'source_url',value:policy.sources[0].value,license:'CC0-1.0'}]}],intendedLicense:'CC0-1.0'});
async function connect(){const c=localClient();await c.connect();return c;}
async function venue(){
 const schema='proto_'+randomUUID().replaceAll('-',''),q=identifier(schema);schemas.push(schema);await migrateAuth(db,schema);
 const clock=new FakeClock(),provider=new FakeJwks([key.jwk]);
 const verifier=new IdentityVerifier({issuer,audience,clock:clock.now,fetchJwks:provider.fetch});
 const sessions=new SessionService({connect,schema,verifier,clock:clock.now,accessTtlMs:172800000,refreshTtlMs:259200000,challengeTtlMs:60000});
 const authorization=new Authorization({sessions,clock:clock.now}),idempotency=new Idempotency({schema,clock:clock.now}),validator=new Validator(policy);
 const submissions=new SubmissionService({authorization,idempotency,validator});let fail=null;
 const moderation=new ModerationService({authorization,idempotency,validator,onBoundary:async(name,c)=>{if(fail===name)throw new Error('synthetic boundary fault');if(fail==='sql'&&name==='snapshot')await c.query('SELECT missing_column FROM pg_catalog.pg_class');}});
 const logs=[],api=new CommunityApi({submissions,moderation,logger:v=>logs.push(v)});
 const v={schema,q,clock,sessions,authorization,idempotency,validator,submissions,moderation,api,logs,setFail:value=>{fail=value;}};
 v.author=await user(v,'author',['contributor']);v.other=await user(v,'other',['contributor']);v.mod=await user(v,'moderator',['moderator']);v.mod2=await user(v,'moderator-2',['moderator']);return v;
}
async function user(v,subject,roles){const ch=await v.sessions.challenge();const u=await v.sessions.login({credential:await credential(key,v.clock,{subject,nonce:ch.nonce}),challenge:ch.challenge});for(const role of roles)await db.query(`INSERT INTO ${v.q}.role_grants(id,principal_id,role,granted_by,granted_at) VALUES($1,$2,$3,$2,$4)`,[randomUUID(),u.principalId,role,new Date(v.clock.now())]);return u;}
async function call(v,u,operation,body){return v.api.invoke({operation,accessToken:u?.accessToken,body});}
async function create(v,p=payload(),u=v.author,extra={}){const r=await call(v,u,'submission.create',{schemaVersion:1,payload:p,expectedRevision:0,idempotencyKey:randomUUID(),...extra});assert.equal(r.status,201,JSON.stringify(r));return r.body;}
const decisionBody=(id,extra={})=>({schemaVersion:1,id,expectedRevision:0,expectedSubmissionRevision:1,idempotencyKey:randomUUID(),...extra});
async function open(v,id,expectedRevision=0){const r=await call(v,v.mod,'moderation.open',decisionBody(id,{expectedRevision}));assert.equal(r.status,200,JSON.stringify(r));}
async function decide(v,id,decision='approve',extra={},u=v.mod){return call(v,u,'moderation.decide',decisionBody(id,{decision,...(decision==='approve'?{}:{reason:'Synthetic review reason'}),...extra}));}
async function count(v,t){return (await db.query(`SELECT count(*)::int AS n FROM ${v.q}.${t}`)).rows[0].n;}
async function snapshot(v){const out={};for(const t of ['submissions','submission_revisions','published_entities','published_revisions','review_events','audit_events','audit_head','change_feed','idempotency_results','identity_mappings'])out[t]=(await db.query(`SELECT row_to_json(t)::text AS row FROM ${v.q}.${t} t ORDER BY row_to_json(t)::text`)).rows;return out;}
async function chain(v){assert.equal(verifyAudit(await exportAudit(db,v.schema)),true);}
before(async()=>{db=await connect();key=await makeKey('handlers-synthetic-key');});
after(async()=>{if(db){await db.query('ROLLBACK');for(const s of schemas)await db.query(`DROP SCHEMA ${identifier(s)} CASCADE`);await db.end();}});

test('authenticated initial submission has server owner, immutable revision, hash and request-bound audit',async()=>{
 const v=await venue(),p=payload(),s=await create(v,p);const r=await call(v,v.author,'submission.read',{schemaVersion:1,id:s.id});assert.equal(r.status,200);assert.equal(r.body.submission.author_id,v.author.principalId);assert.deepEqual(r.body.revisions[0].payload,p);assert.equal(await count(v,'published_entities'),0);
 const a=(await db.query(`SELECT request_id,after_hash FROM ${v.q}.audit_events WHERE target_id=$1`,[s.id])).rows[0];assert.equal(a.request_id,s.requestId);assert.equal(a.after_hash,r.body.revisions[0].this_hash);
 await assert.rejects(db.query(`UPDATE ${v.q}.submission_revisions SET payload='{}' WHERE submission_id=$1`,[s.id]),e=>e.code==='55000');await chain(v);
});
for(const [name,mutate] of [
 ['unknown top field',p=>p.elevated=true],['nested role',p=>p.target.role='moderator'],['field author',p=>p.fields[0].author=randomUUID()],['evidence approval',p=>p.fields[0].evidence[0].approved=true],['external elevated',p=>{p.target.externalIdentity={provider:'wikidata',providerKind:'work',externalId:'Q42',role:'admin'};}],
 ['missing schema version',p=>delete p.schemaVersion],['wrong schema version',p=>p.schemaVersion=2],['HTML',p=>p.fields[0].newValue='<script>alert(1)</script>'],['encoded HTML',p=>p.fields[0].newValue='&lt;script&gt;'],['object value',p=>p.fields[0].newValue={notes:'private'}],['oversize string',p=>p.fields[0].newValue='x'.repeat(2049)],['bad UUID',p=>p.target.catalogueUuid='123'],['wrong kind',p=>p.target.kind='practice'],['unknown change',p=>p.changeKind='publish'],['no identity',p=>delete p.target.catalogueUuid],['unknown field name',p=>p.fields[0].field='notes'],['missing license',p=>delete p.fields[0].evidence[0].license],['unknown license',p=>p.fields[0].evidence[0].license='unknown'],['rejected license',p=>p.fields[0].evidence[0].license='CC-BY-NC-SA-3.0'],['missing evidence',p=>p.fields[0].evidence=[]],['missing intended license',p=>delete p.intendedLicense],['unreviewed source',p=>p.fields[0].evidence[0].value='https://evidence.synthetic.invalid/unknown'],['external kind mismatch',p=>p.target.externalIdentity={provider:'wikidata',providerKind:'artist',externalId:'Q42'}]
])test(`T6/T13 validation rejects ${name} without effects`,async()=>{const v=await venue(),p=payload();mutate(p);const prior=await snapshot(v);const r=await call(v,v.author,'submission.create',{schemaVersion:1,payload:p,expectedRevision:0,idempotencyKey:randomUUID()});assert.equal(r.status,400);assert.deepEqual(await snapshot(v),prior);});
for(const name of ['journal','notes','microphone','backup','sessions','repertoire','profile'])test(`T21 rejects personal ${name} at every object level`,async()=>{
 const v=await venue();for(const where of ['root','target','field','evidence']){const p=payload(),obj={root:p,target:p.target,field:p.fields[0],evidence:p.fields[0].evidence[0]}[where];obj[name]={data:'synthetic private'};assert.equal((await call(v,v.author,'submission.create',{schemaVersion:1,payload:p,expectedRevision:0,idempotencyKey:randomUUID()})).status,400);}assert.equal(await count(v,'submissions'),0);
});
for(const url of ['javascript:alert(1)','data:text/plain,x','http://evidence.synthetic.invalid/source','https://localhost/source','https://127.0.0.1/source','https://127.1/source','https://0x7f000001/source','https://10.0.0.1/source','https://172.16.0.1/source','https://192.168.1.1/source','https://169.254.169.254/source','https://[::1]/source','https://[::ffff:7f00:1]/source','https://user:pass@evidence.synthetic.invalid/source','https://evidence.synthetic.invalid:444/source','https://evidence.synthetic.invalid./source','https://evidence.synthetic.invalid/source#private','https://evidence.synthetic.invalid\\@localhost/source','https://evil.invalid/source','https://evidence.synthetic.invalid/'+ 'x'.repeat(2048)])test(`T14 refuses unsafe/unreviewed URL ${url.slice(0,80)}`,()=>{assert.throws(()=>new Validator(policy).payload({...payload(),fields:[{field:'title',newValue:'x',evidence:[{type:'source_url',value:url,license:'CC0-1.0'}]}]}),e=>e.status===400);});

test('boundary checks: 20 fields, 10 URLs, 2 KiB strings, 16 KiB exact request, nesting/cycle/getters',()=>{
 const validator=new Validator(policy),p=payload();p.fields=fieldNames.map(field=>({field,newValue:'x',evidence:[{type:'reference',value:policy.sources[1].value,license:'CC0-1.0'}]}));validator.payload(p);p.fields.push(structuredClone(p.fields[0]));assert.throws(()=>validator.payload(p));
 const b=payload();b.fields[0].evidence=Array.from({length:10},()=>structuredClone(b.fields[0].evidence[0]));validator.payload(b);b.fields[0].evidence.push(b.fields[0].evidence[0]);assert.throws(()=>validator.payload(b));
 const raw={x:''};raw.x='x'.repeat(16384-Buffer.byteLength(JSON.stringify(raw)));bounded(raw);raw.x+='x';assert.throws(()=>bounded(raw));
 let deep={};for(let i=0;i<9;i++)deep={x:deep};assert.throws(()=>bounded(deep));const cycle={};cycle.x=cycle;assert.throws(()=>bounded(cycle));assert.throws(()=>bounded({get x(){throw new Error('must not invoke');}}),e=>e.status===400);
 const max=payload();max.fields[0].newValue='x'.repeat(2048);validator.payload(max);assert.throws(()=>new Validator().payload(payload()),e=>e.status===400);
 const u='https://evidence.synthetic.invalid/';safeUrl(u+'a'.repeat(2048-u.length),['evidence.synthetic.invalid']);assert.throws(()=>safeUrl(u+'a'.repeat(2049-u.length),['evidence.synthetic.invalid']));
});

test('T7/T8 owner-only private reads/revisions/withdrawals hide another contributor and absent IDs equally',async()=>{
 const v=await venue(),s=await create(v);for(const id of [s.id,randomUUID()]){
  assert.equal((await call(v,v.other,'submission.read',{schemaVersion:1,id})).status,404);
  assert.equal((await call(v,v.other,'submission.revise',decisionBody(id,{payload:payload()}))).status,404);
  assert.equal((await call(v,v.other,'submission.withdraw',decisionBody(id))).status,404);
 }const mine=await call(v,v.other,'submission.mine',{schemaVersion:1});assert.deepEqual(mine.body.items,[]);assert.equal((await call(v,v.author,'moderation.queue',{schemaVersion:1})).status,403);assert.equal((await call(v,v.mod,'submission.read',{schemaVersion:1,id:s.id})).status,200);
});
test('canonical lifecycle request_changes → author revision keeps original immutable snapshot and audit',async()=>{
 const v=await venue(),p=payload(),s=await create(v,p);assert.equal((await call(v,v.author,'submission.revise',decisionBody(s.id,{payload:p}))).status,409);await open(v,s.id);assert.equal((await decide(v,s.id,'request_changes')).status,200);
 const p2=structuredClone(p);p2.fields[0].newValue='Revised synthetic title';const r=await call(v,v.author,'submission.revise',decisionBody(s.id,{payload:p2}));assert.equal(r.status,201,JSON.stringify(r));
 const history=await call(v,v.author,'submission.read',{schemaVersion:1,id:s.id});assert.equal(history.body.revisions.length,2);assert.deepEqual(history.body.revisions[0].payload,p);assert.deepEqual(history.body.revisions[1].payload,p2);assert.equal(history.body.revisions[1].prev_hash,history.body.revisions[0].this_hash);
 assert.equal((await decide(v,s.id,'approve',{expectedSubmissionRevision:1})).status,409);assert.equal((await decide(v,s.id,'approve',{expectedSubmissionRevision:2})).status,201);await chain(v);
});
for(const decision of ['approve','reject','request_changes'])test(`atomic ${decision} transition and review/audit coherence`,async()=>{
 const v=await venue(),s=await create(v);await open(v,s.id);const r=await decide(v,s.id,decision);assert.equal(r.status,decision==='approve'?201:200,JSON.stringify(r));
 assert.equal(await count(v,'review_events'),decision==='approve'?3:2);assert.equal(await count(v,'published_entities'),decision==='approve'?1:0);assert.equal(await count(v,'change_feed'),decision==='approve'?1:0);
 const bad=(await db.query(`SELECT r.id FROM ${v.q}.review_events r JOIN ${v.q}.audit_events a ON a.seq=r.audit_seq WHERE a.actor_id<>r.actor_id OR a.target_id<>r.submission_id OR a.target_revision<>r.submission_revision`)).rows;assert.equal(bad.length,0);
 assert.equal((await decide(v,s.id,'reject')).status,409);await chain(v);
});
test('withdraw author submitted/in_review only; terminal and changes_requested deny without silent deletion',async()=>{
 const v=await venue(),s=await create(v);assert.equal((await call(v,v.author,'submission.withdraw',decisionBody(s.id,{reason:'Synthetic withdrawal'}))).status,200);assert.equal((await call(v,v.author,'submission.withdraw',decisionBody(s.id))).status,409);assert.equal(await count(v,'submission_revisions'),1);
 const s2=await create(v);await open(v,s2.id);assert.equal((await call(v,v.author,'submission.withdraw',decisionBody(s2.id))).status,200);
 const s3=await create(v);await open(v,s3.id);await decide(v,s3.id,'request_changes');assert.equal((await call(v,v.author,'submission.withdraw',decisionBody(s3.id))).status,409);await chain(v);
});
test('T6 self-approval, forged role/author/status, revoked moderator and suspended identity deny',async()=>{
 const v=await venue(),s=await create(v);await db.query(`INSERT INTO ${v.q}.role_grants(id,principal_id,role,granted_by,granted_at) VALUES($1,$2,'moderator',$2,$3)`,[randomUUID(),v.author.principalId,new Date(v.clock.now())]);assert.equal((await call(v,v.author,'moderation.open',decisionBody(s.id))).status,403);await open(v,s.id);assert.equal((await decide(v,s.id,'approve',{},v.author)).status,403);
 for(const name of ['role','author','status'])assert.equal((await decide(v,s.id,'approve',{[name]:'administrator'})).status,400);
 await db.query(`UPDATE ${v.q}.role_grants SET revoked_at=$2 WHERE principal_id=$1`,[v.mod.principalId,new Date(v.clock.now())]);assert.equal((await decide(v,s.id)).status,403);
 await db.query(`UPDATE ${v.q}.principals SET state='suspended' WHERE id=$1`,[v.mod2.principalId]);assert.equal((await decide(v,s.id,'approve',{},v.mod2)).status,403);
});
test('unresolved external identity uniqueness persists needs_review, including paired catalogue UUID; no auto-merge/publication',async()=>{
 const v=await venue(),p=payload();delete p.target.catalogueUuid;p.target.externalIdentity={provider:'wikidata',providerKind:'work',externalId:'Q42'};
 const a=await create(v,p),b=await create(v,p);assert.equal(await count(v,'identity_mappings'),1);await open(v,a.id);assert.equal((await decide(v,a.id)).status,409);
 p.target.catalogueUuid=randomUUID();const c=await create(v,p);await open(v,c.id);assert.equal((await decide(v,c.id)).status,409);const m=(await db.query(`SELECT * FROM ${v.q}.identity_mappings`)).rows[0];assert.equal(m.state,'needs_review');assert.equal(m.catalogue_uuid,null);assert.equal(await count(v,'published_entities'),0);
});
test('T10 two moderators race on one submission: exactly [201,409] and coherent publication/feed',async()=>{
 const v=await venue(),s=await create(v);await open(v,s.id);const rs=await Promise.all([decide(v,s.id),decide(v,s.id,'approve',{},v.mod2)]);assert.deepEqual(rs.map(r=>r.status).sort(),[201,409]);assert.equal(await count(v,'published_revisions'),1);assert.equal(await count(v,'change_feed'),1);await chain(v);
});
for(const existing of [false,true])test(`T10 distinct submissions same ${existing?'existing':'absent'} entity: one winner, stale loser 409`,async()=>{
 const v=await venue(),p=payload();let expected=0;if(existing){const seed=await create(v,p);await open(v,seed.id);assert.equal((await decide(v,seed.id)).status,201);expected=1;}
 const a=await create(v,p,v.author,{expectedRevision:expected}),b=await create(v,p,v.other,{expectedRevision:expected});await open(v,a.id,expected);await open(v,b.id,expected);
 const rs=await Promise.all([decide(v,a.id,'approve',{expectedRevision:expected}),decide(v,b.id,'approve',{expectedRevision:expected},v.mod2)]);assert.deepEqual(rs.map(r=>r.status).sort(),[201,409]);assert.equal(await count(v,'published_revisions'),expected+1);
 const projection=(await db.query(`SELECT e.kind,e.revision::text,r.payload,r.source_evidence FROM ${v.q}.published_entities e JOIN ${v.q}.published_revisions r ON r.entity_id=e.id AND r.revision=e.revision`)).rows[0];assert.equal(projection.revision,String(expected+1));assert.deepEqual(projection.payload,p);assert.deepEqual(projection.source_evidence,p.fields[0].evidence);await chain(v);
});
test('T11 same-key concurrent create and moderation replay exact stored response; different-key loser conflicts',async()=>{
 const v=await venue(),body={schemaVersion:1,payload:payload(),expectedRevision:0,idempotencyKey:randomUUID()};
 const rs=await Promise.all([call(v,v.author,'submission.create',body),call(v,v.author,'submission.create',JSON.parse(canonical(body)))]);assert.deepEqual(rs[0],rs[1]);assert.equal(rs[0].status,201);assert.equal(await count(v,'submissions'),1);
 await open(v,rs[0].body.id);const d=decisionBody(rs[0].body.id,{decision:'approve'}),approvals=await Promise.all([call(v,v.mod,'moderation.decide',d),call(v,v.mod,'moderation.decide',d)]);assert.deepEqual(approvals[0],approvals[1]);assert.equal(approvals[0].status,201);assert.equal(await count(v,'published_revisions'),1);assert.equal((await decide(v,rs[0].body.id)).status,409);
 await chain(v);
});
test('T12 same key different payload/operation gives 422; principals cannot see/reuse stored results',async()=>{
 const v=await venue(),p=payload(),key=randomUUID(),body={schemaVersion:1,payload:p,expectedRevision:0,idempotencyKey:key};const a=await call(v,v.author,'submission.create',body);assert.equal(a.status,201);const changed=structuredClone(body);changed.payload.fields[0].newValue='Other';assert.equal((await call(v,v.author,'submission.create',changed)).status,422);
 assert.equal((await call(v,v.author,'submission.withdraw',decisionBody(a.body.id,{idempotencyKey:key}))).status,422);
 const b=await call(v,v.other,'submission.create',body);assert.equal(b.status,201);assert.notEqual(a.body.id,b.body.id);assert.equal(await count(v,'submissions'),2);
 assert.equal((await call(v,v.author,'submission.create',{...body,idempotencyKey:'bad'})).status,400);
});
test('expiry minimum 24h: replay just before, reuse exactly at expiry; original records retained',async()=>{
 const v=await venue(),body={schemaVersion:1,payload:payload(),expectedRevision:0,idempotencyKey:randomUUID()},a=await call(v,v.author,'submission.create',body);
 v.clock.advance(86400000-1);assert.deepEqual(await call(v,v.author,'submission.create',body),a);v.clock.advance(1);const b=await call(v,v.author,'submission.create',body);assert.equal(b.status,201);assert.notEqual(b.body.id,a.body.id);assert.equal(await count(v,'submissions'),2);
 const times=(await db.query(`SELECT extract(epoch from expires_at-created_at)::int AS seconds FROM ${v.q}.idempotency_results`)).rows;assert.ok(times.every(t=>t.seconds>=86400));
});
for(const boundary of ['locked','entity','snapshot','projection','approved','audit','feed','sql'])test(`T17 full rollback and same-key retry after ${boundary} fault`,async()=>{
 const v=await venue(),s=await create(v);await open(v,s.id);const prior=await snapshot(v),body=decisionBody(s.id,{decision:'approve'});v.setFail(boundary);assert.equal((await call(v,v.mod,'moderation.decide',body)).status,503);assert.deepEqual(await snapshot(v),prior);v.setFail(null);assert.equal((await call(v,v.mod,'moderation.decide',body)).status,201);await chain(v);
});
test('T17 idempotency insert and deferred commit failures rollback all effects and allow same-key retry',async()=>{
 const v=await venue(),s=await create(v);await open(v,s.id);const body=decisionBody(s.id,{decision:'approve'}),prior=await snapshot(v);
 for(const deferred of [false,true]){
 await db.query(`CREATE FUNCTION ${v.q}.fixture_fail() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'synthetic store/commit failure'; END; $$`);
 await db.query(deferred?`CREATE CONSTRAINT TRIGGER fixture_failure AFTER INSERT ON ${v.q}.idempotency_results DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION ${v.q}.fixture_fail()`:`CREATE TRIGGER fixture_failure BEFORE INSERT ON ${v.q}.idempotency_results FOR EACH ROW EXECUTE FUNCTION ${v.q}.fixture_fail()`);
 assert.equal((await call(v,v.mod,'moderation.decide',body)).status,503);assert.deepEqual(await snapshot(v),prior);await db.query(`DROP TRIGGER fixture_failure ON ${v.q}.idempotency_results; DROP FUNCTION ${v.q}.fixture_fail()`);
 }assert.equal((await call(v,v.mod,'moderation.decide',body)).status,201);await chain(v);
});
test('database lock timeout returns safe 503; rollback releases locks and permits retry',async()=>{
 const v=await venue(),s=await create(v);await open(v,s.id);const blocker=await connect();await blocker.query('BEGIN');await blocker.query(`SELECT id FROM ${v.q}.submissions WHERE id=$1 FOR UPDATE`,[s.id]);const body=decisionBody(s.id,{decision:'approve'});
 try{const r=await call(v,v.mod,'moderation.decide',body);assert.equal(r.status,503);assert.ok(!JSON.stringify(r).includes('SELECT'));}finally{await blocker.query('ROLLBACK');await blocker.end();}
 assert.equal((await call(v,v.mod,'moderation.decide',body)).status,201);await chain(v);
});
test('T18 immutable publication/audit/review/feed; exported chain tamper detected; no audit mutation adapter',async()=>{
 const v=await venue(),s=await create(v);await open(v,s.id);assert.equal((await decide(v,s.id)).status,201);
 for(const t of ['published_revisions','audit_events','review_events','change_feed'])await assert.rejects(db.query(`DELETE FROM ${v.q}.${t}`),e=>e.code==='55000');
 const exported=await exportAudit(db,v.schema);verifyAudit(exported);exported.events[0].action='forged';assert.throws(()=>verifyAudit(exported));assert.equal((await call(v,v.mod,'audit.edit',{schemaVersion:1})).status,400);await chain(v);
});
test('malformed, expired and revoked sessions safely reject actual handlers with 401 and sanitized logs',async()=>{
 const v=await venue(),s=await create(v),body=decisionBody(s.id,{decision:'approve'});await open(v,s.id);
 for(const accessToken of [undefined,'bad','gs_access_'+'a'.repeat(43)])assert.equal((await v.api.invoke({operation:'moderation.decide',accessToken,body})).status,401);
 await v.sessions.logout(v.mod.accessToken);assert.equal((await call(v,v.mod,'moderation.decide',body)).status,401);v.clock.advance(172800000);assert.equal((await call(v,v.mod2,'moderation.decide',body)).status,401);
 assert.ok(!JSON.stringify(v.logs).includes(v.mod.accessToken));assert.equal(await count(v,'published_entities'),0);
});
test('validation rejects unknown envelope/schema/request fields; safe 400/401/403/404/409/422/503 taxonomy covered',async()=>{
 const v=await venue();assert.equal((await v.api.invoke({operation:'submission.create',accessToken:v.author.accessToken,body:{schemaVersion:1},role:'admin'})).status,400);
 const body={schemaVersion:1,payload:payload(),expectedRevision:0,idempotencyKey:randomUUID()};for(const version of [undefined,'1',0,2])assert.equal((await call(v,v.author,'submission.create',{...body,schemaVersion:version})).status,400);
 const first=await call(v,v.author,'submission.create',body);assert.equal(first.status,201);await v.sessions.logout(v.author.accessToken);assert.equal((await call(v,v.author,'submission.create',body)).status,401);
});
test('bounded queue/mine pagination returns 20 then remainder with microsecond stable cursor, no private payload leakage',async()=>{
 const v=await venue();for(let i=0;i<22;i++)await create(v);
 const page=await call(v,v.author,'submission.mine',{schemaVersion:1});assert.equal(page.body.items.length,20);assert.ok(page.body.nextCursor);const next=await call(v,v.author,'submission.mine',{schemaVersion:1,cursor:page.body.nextCursor});assert.equal(next.body.items.length,2);assert.equal(next.body.nextCursor,null);
 assert.equal(new Set([...page.body.items,...next.body.items].map(i=>i.id)).size,22);
 const queue=await call(v,v.mod,'moderation.queue',{schemaVersion:1});assert.equal(queue.body.items.length,20);assert.ok(queue.body.items.every(i=>!Object.hasOwn(i,'author_id')&&!Object.hasOwn(i,'payload')));
});
test('constraint-specific PostgreSQL recovery does not translate unrelated unique/SQL errors to conflict',async()=>{
 const v=await venue(),c=await connect(),args={principal:v.author.principalId,key:randomUUID(),hash:'a'.repeat(64),body:{requestId:randomUUID()},now:new Date(v.clock.now())};
 try{await c.query('BEGIN');assert.equal((await insertResult(c,v.schema,args)).stored,true);await c.query('COMMIT');await c.query('BEGIN');const replay=await insertResult(c,v.schema,args);assert.equal(replay.stored,false);assert.deepEqual(replay.body,args.body);await c.query('COMMIT');
 await c.query('BEGIN');await assert.rejects(insertResult(c,v.schema,{...args,key:randomUUID(),hash:'bad'}),e=>e.code==='23514');await c.query('ROLLBACK');}finally{await c.end();}
});

test('reusable service snapshots caller data before awaiting authorization; actor remains session-derived',async()=>{
 const v=await venue(),p=payload(),body={schemaVersion:1,payload:p,expectedRevision:0,idempotencyKey:randomUUID()};const before=structuredClone(p);
 const pending=v.submissions.create(v.author.accessToken,body);body.payload.fields[0].newValue='Changed by caller during await';const result=await pending;assert.equal(result.status,201);
 const stored=(await db.query(`SELECT payload FROM ${v.q}.submission_revisions WHERE submission_id=$1`,[result.body.id])).rows[0].payload;assert.deepEqual(stored,before);
});
test('read role revocation is immediate; owner retains data but cannot bypass current capabilities',async()=>{
 const v=await venue(),s=await create(v);await db.query(`UPDATE ${v.q}.role_grants SET revoked_at=$2 WHERE principal_id=$1`,[v.author.principalId,new Date(v.clock.now())]);assert.equal((await call(v,v.author,'submission.read',{schemaVersion:1,id:s.id})).status,404);assert.equal((await call(v,v.author,'submission.mine',{schemaVersion:1})).status,403);assert.equal(await count(v,'submission_revisions'),1);
});
test('exact idempotency collision recovery: unrelated unique violation remains SQL error',async()=>{
 const v=await venue(),c=await connect(),args={principal:v.author.principalId,key:randomUUID(),hash:'b'.repeat(64),body:{requestId:randomUUID()},now:new Date(v.clock.now())};
 await db.query(`CREATE UNIQUE INDEX fixture_other_unique ON ${v.q}.idempotency_results(request_hash)`);
 try{await c.query('BEGIN');await insertResult(c,v.schema,args);await c.query('COMMIT');await c.query('BEGIN');await assert.rejects(insertResult(c,v.schema,{...args,key:randomUUID()}),e=>e.code==='23505'&&e.constraint==='fixture_other_unique');await c.query('ROLLBACK');}finally{await c.end();}
});
test('schemaVersion/unknown object/string/array value and sparse evidence boundaries fail closed',async()=>{
 const v=await venue();for(const change of [p=>p.fields[0].evidence[0].type='proxy',p=>p.fields[0].evidence[0].value={url:policy.sources[0].value},p=>p.fields[0].newValue=['notes'],p=>p.fields=[],p=>p.target.externalIdentity={provider:'musicbrainz',providerKind:'work',externalId:'not-an-id'},p=>p.fields[0].evidence=[null],p=>p.fields[0].newValue='\u0000']){const p=payload();change(p);assert.equal((await call(v,v.author,'submission.create',{schemaVersion:1,payload:p,expectedRevision:0,idempotencyKey:randomUUID()})).status,400);}
});
test('stale expected entity/submission revisions, malformed reasons and retargeting have safe errors',async()=>{
 const v=await venue(),p=payload(),s=await create(v,p);assert.equal((await call(v,v.mod,'moderation.open',decisionBody(s.id,{expectedRevision:1}))).status,409);await open(v,s.id);
 for(const changes of [{expectedRevision:1},{expectedSubmissionRevision:2}])assert.equal((await decide(v,s.id,'approve',changes)).status,409);
 assert.equal((await decide(v,s.id,'reject',{reason:''})).status,400);await decide(v,s.id,'request_changes');const p2=payload();assert.equal((await call(v,v.author,'submission.revise',decisionBody(s.id,{payload:p2}))).status,400);assert.equal(await count(v,'submission_revisions'),1);
});
test('audit export sorts int8 numerically beyond nine and detects an omitted row/head tamper',async()=>{
 const v=await venue();for(let i=0;i<12;i++)await create(v);const a=await exportAudit(db,v.schema);assert.ok(a.events.length>9);assert.equal(verifyAudit(a),true);const missing=structuredClone(a);missing.events.splice(4,1);assert.throws(()=>verifyAudit(missing));a.head.last_hash='0'.repeat(64);assert.throws(()=>verifyAudit(a));
});
