// Actual PostgreSQL only. No skip/fallback or scripted SQL acceptance.
import {test,before,after} from 'node:test';
import assert from 'node:assert/strict';
import {randomUUID} from 'node:crypto';
import {writeFileSync} from 'node:fs';
import {localClient,identifier} from '../../stage6-schema-pg/src/database.mjs';
import {migrateAuth} from '../../stage6-auth-local/src/storage.mjs';
import {SessionService} from '../../stage6-auth-local/src/sessions.mjs';
import {Authorization} from '../../stage6-auth-local/src/authorization.mjs';
import {SubmissionService} from '../../stage6-handlers-local/src/submissions.mjs';
import {Idempotency} from '../../stage6-handlers-local/src/idempotency.mjs';
import {exportAudit,verifyAudit} from '../../stage6-handlers-local/src/audit.mjs';
import {createLocalAdapter} from '../src/adapter.mjs';
import {syntheticValidator} from './venue.mjs';
let db;const schemas=[],detailCaptures=[];
const connect=async()=>{const c=localClient();await c.connect();return c;};
before(async()=>{db=await connect();console.log('ACTUAL POSTGRESQL '+(await db.query('SELECT version() AS v')).rows[0].v);});
after(async()=>{if(db){for(const s of schemas)await db.query(`DROP SCHEMA ${identifier(s)} CASCADE`);await db.end();}if(process.env.M3_COMPARISON_PATH)writeFileSync(process.env.M3_COMPARISON_PATH,JSON.stringify(detailCaptures,null,2)+'\n');});
async function venue(){
 const schema='proto_m3_'+randomUUID().replaceAll('-',''),q=identifier(schema);schemas.push(schema);await migrateAuth(db,schema);
 let now=Date.now(),fault;const clock=()=>now;
 const verifier={verify:async raw=>JSON.parse(raw)}; // Synthetic identity only, no production credential verifier.
 const sessions=new SessionService({connect,schema,verifier,clock,accessTtlMs:900000,refreshTtlMs:604800000,challengeTtlMs:300000});
 const authorization=new Authorization({sessions,clock}),validator=syntheticValidator();
 const submissions=new SubmissionService({authorization,validator,idempotency:new Idempotency({schema,clock})});
 const faultConnect=async()=>{const c=await connect(),query=c.query.bind(c);c.query=async(sql,args)=>{if(fault){const result=await fault(sql,args,c,query);if(result)return result;}return query(sql,args);};return c;};
 const factory=(options={})=>createLocalAdapter({localOnly:true,connect:faultConnect,schema,verifier,clock,validator,...options});
 let adapter=factory();
 const send=async(u,method,path,body)=>{const r=await adapter.invoke({method,path,headers:{...(u?{Authorization:'Bearer '+u.accessToken}:{}),...(body?{'Content-Type':'application/json'}:{})},...(body?{body:JSON.stringify(body)}:{})});if(method==='GET'&&r.status===200&&r.body?.submissionId&&path.includes('/comparison?'))detailCaptures.push(r.body);return r;};
 const user=async(subject,role)=>{const ch=await sessions.challenge(),u=await sessions.login({challenge:ch.challenge,credential:JSON.stringify({issuer:'https://synthetic.invalid',subject,nonce:ch.nonce})});if(role)await db.query(`INSERT INTO ${q}.role_grants(id,principal_id,role,granted_by,granted_at) VALUES($1,$2,$3,$2,$4)`,[randomUUID(),u.principalId,role,new Date(now)]);return u;};
 const v={schema,q,sessions,authorization,submissions,send,user,clock,advance:ms=>now+=ms,setFault:f=>fault=f,fresh:opts=>adapter=factory(opts)};
 v.author=await user('author','contributor');v.mod=await user('moderator','moderator');v.mod2=await user('moderator2','moderator');v.other=await user('other','contributor');return v;
}
const body=(extra={})=>({schemaVersion:1,expectedEntityRevision:'0',expectedSubmissionRevision:'1',idempotencyKey:randomUUID(),...extra});
function payload(entity){return {schemaVersion:1,target:{kind:'work',catalogueUuid:entity},changeKind:'new_metadata',fields:[{field:'syntheticTitle',newValue:'Synthetic work',evidence:[{type:'reference',value:'Synthetic reference only',license:'SYNTHETIC-NOT-APPROVED'}]}],intendedLicense:'SYNTHETIC-NOT-APPROVED'};}
async function create(v,{entity=randomUUID(),expected=0,open=true}={}){
 const s=(await v.submissions.create(v.author.accessToken,{schemaVersion:1,payload:payload(entity),expectedRevision:expected,idempotencyKey:randomUUID()})).body;
 s.path='/v1/admin/submissions/'+s.id;s.entity=entity;
 if(open)assert.equal((await v.send(v.mod,'POST',s.path+'/open',body({expectedEntityRevision:String(expected)}))).status,200);return s;
}
const decide=(v,s,b=body({decision:'approve'}),u=v.mod)=>v.send(u,'POST',s.path+'/decisions',b);
const detail=(v,s,u=v.mod)=>v.send(u,'GET',s.path);
async function snapshot(v){const out={};for(const t of ['submissions','submission_revisions','published_entities','published_revisions','review_events','audit_events','audit_head','change_feed','idempotency_results'])out[t]=(await db.query(`SELECT row_to_json(t)::text AS r FROM ${v.q}.${t} t ORDER BY row_to_json(t)::text`)).rows;return out;}
async function count(v,t){return (await db.query(`SELECT count(*)::int AS n FROM ${v.q}.${t}`)).rows[0].n;}
async function chain(v){assert.equal(verifyAudit(await exportAudit(db,v.schema)),true);}

const comparison=(v,s,u=v.mod,entity=s.entity)=>v.send(u,'GET','/v1/admin/entities/'+entity+'/comparison?submissionId='+s.id);
async function published(v){const s=await create(v);assert.equal((await decide(v,s)).status,201);return s;}
test('PG Comparison exact live DTO, matching baseline, Detail unchanged and no read mutations',async()=>{
 const v=await venue(),first=await published(v),s=await create(v,{entity:first.entity,expected:1});const prior=await snapshot(v);
 const r=await comparison(v,s);assert.equal(r.status,200,JSON.stringify(r));assert.equal(r.body.state,'available');assert.equal(r.body.entityRevision,'1');
 assert.deepEqual(Object.keys(r.body).sort(),['schemaVersion','requestId','submissionId','entityId','state','entityRevision','entityStatus','payloadHash','fields'].sort());
 assert.deepEqual(r.body.fields,[{field:'syntheticTitle',value:'Synthetic work',evidence:[{type:'reference',value:'Synthetic reference only',license:'SYNTHETIC-NOT-APPROVED'}]}]);
 assert.equal(r.body.entityStatus,'published');assert.match(r.body.payloadHash,/^[0-9a-f]{64}$/);
 const d=await detail(v,s);assert.equal(d.body.expectedEntityRevision,'1');assert.equal(d.body.submissionRevision,'1');assert.deepEqual(await snapshot(v),prior);
});
test('PG changed live head independent of recorded baseline; stale decision no side effects',async()=>{
 const v=await venue(),first=await published(v),s=await create(v,{entity:first.entity,expected:1}),other=await create(v,{entity:first.entity,expected:1});
 assert.equal((await comparison(v,s)).body.entityRevision,'1');assert.equal((await decide(v,other,body({decision:'approve',expectedEntityRevision:'1'}))).status,201);
 const r=await comparison(v,s);assert.equal(r.body.entityRevision,'2');assert.equal((await detail(v,s)).body.expectedEntityRevision,'1');const prior=await snapshot(v);
 assert.equal((await decide(v,s,body({decision:'approve',expectedEntityRevision:'1'}))).status,409);
 assert.equal((await decide(v,s,body({decision:'approve',expectedEntityRevision:'2'}))).status,409);assert.deepEqual(await snapshot(v),prior);
});
test('PG proposal changes independently; stale proposal decision conflicts with no effects',async()=>{
 const v=await venue(),first=await published(v),s=await create(v,{entity:first.entity,expected:1});const old=await comparison(v,s);
 assert.equal((await decide(v,s,body({decision:'request_changes',reason:'Synthetic revision',expectedEntityRevision:'1'}))).status,200);
 const p=payload(s.entity);p.fields[0].newValue='Different proposal';await v.submissions.revise(v.author.accessToken,{schemaVersion:1,id:s.id,payload:p,expectedRevision:1,expectedSubmissionRevision:1,idempotencyKey:randomUUID()});
 const r=await comparison(v,s);assert.equal(r.body.entityRevision,old.body.entityRevision);assert.deepEqual(r.body.fields,old.body.fields);assert.equal(r.body.payloadHash,old.body.payloadHash);
 assert.equal((await detail(v,s)).body.submissionRevision,'2');const prior=await snapshot(v);
 assert.equal((await decide(v,s,body({decision:'approve',expectedEntityRevision:'1'}))).status,409);assert.deepEqual(await snapshot(v),prior);
});
test('PG coherent Comparison during concurrent committed publication with different values',async()=>{
 const v=await venue(),first=await published(v),s=await create(v,{entity:first.entity,expected:1}),other=await create(v,{entity:first.entity,expected:1,open:false});
 // Revise only through normal service/state flow.
 assert.equal((await v.send(v.mod2,'POST',other.path+'/open',body({expectedEntityRevision:'1'}))).status,200);
 assert.equal((await decide(v,other,body({decision:'request_changes',reason:'Synthetic change',expectedEntityRevision:'1'}),v.mod2)).status,200);
 const p=payload(s.entity);p.fields[0].newValue='New live value';await v.submissions.revise(v.author.accessToken,{schemaVersion:1,id:other.id,payload:p,expectedRevision:1,expectedSubmissionRevision:1,idempotencyKey:randomUUID()});
 const before=(await comparison(v,s)).body;let enter,release;const seen=new Promise(r=>enter=r),hold=new Promise(r=>release=r);
 v.setFault(async(sql,args,c,query)=>{if(sql.startsWith('SELECT s.catalogue_uuid')){const result=await query(sql,args);enter();await hold;return result;}});
 const pending=comparison(v,s);await seen;
 try{assert.equal((await decide(v,other,body({decision:'approve',expectedEntityRevision:'1',expectedSubmissionRevision:'2'}),v.mod2)).status,201);}finally{release();}
 const old=await pending;v.setFault(null);assert.equal(old.body.entityRevision,'1');assert.deepEqual(old.body.fields,before.fields);assert.equal(old.body.payloadHash,before.payloadHash);
 const now=await comparison(v,s);assert.equal(now.body.entityRevision,'2');assert.equal(now.body.fields[0].value,'New live value');assert.notEqual(now.body.payloadHash,before.payloadHash);await chain(v);
});
test('PG missing target absent; missing submission/wrong entity denied404 without mutations',async()=>{
 const v=await venue(),s=await create(v);let prior=await snapshot(v);assert.equal((await comparison(v,s)).body.reason,'absent');
 assert.equal((await comparison(v,s,v.mod,randomUUID())).status,404);assert.equal((await comparison(v,{...s,id:randomUUID()})).status,404);assert.deepEqual(await snapshot(v),prior);
});
test('PG live Comparison authorization hiding, account isolation, role/session revocation',async()=>{
 const v=await venue(),s=await published(v);
 for(const u of [v.author,v.other])for(const id of [s.id,randomUUID()])assert.equal((await comparison(v,{...s,id},u)).status,404);
 assert.equal((await comparison(v,s,null)).status,401);
 await db.query(`UPDATE ${v.q}.role_grants SET revoked_at=clock_timestamp() WHERE principal_id=$1`,[v.mod.principalId]);assert.equal((await comparison(v,s)).status,404);
 await v.sessions.logout(v.mod2.accessToken);assert.equal((await comparison(v,s,v.mod2)).status,401);
});
test('PG default-deny policy and rejected stored field never expose live values',async()=>{
 const v=await venue(),s=await published(v);v.fresh({validator:undefined});const prior=await snapshot(v);
 let r=await comparison(v,s);assert.equal(r.body.reason,'projection_unavailable');assert.deepEqual(Object.keys(r.body).sort(),['schemaVersion','requestId','submissionId','entityId','state','reason'].sort());assert.ok(!JSON.stringify(r).includes('Synthetic work'));assert.deepEqual(await snapshot(v),prior);
 // A different trusted fixture policy excludes the field already stored in PG.
 const policy=syntheticValidator();delete policy.fields.work.syntheticTitle;v.fresh({validator:policy});r=await comparison(v,s);assert.equal(r.body.reason,'projection_unavailable');
});
test('PG unsupported kind projection unavailable without proposed-value fallback',async()=>{
 const v=await venue(),s=await published(v),policy=syntheticValidator();policy.fields={artist:policy.fields.work};v.fresh({validator:policy});
 assert.equal((await comparison(v,s)).body.reason,'projection_unavailable');
});
test('PG exact int8 live read above JS precision; full-range writes compare losslessly',async()=>{
 const v=await venue(),s=await published(v),max='9223372036854775807';
 // Isolated synthetic initial published-head fixture; all FK/hash/evidence guards remain.
 // This exercises adapter reads only, not a full-int8 domain publication.
 const entity=randomUUID();await db.query('BEGIN');try{
 await db.query(`INSERT INTO ${v.q}.published_entities(id,kind,revision,status) VALUES($1,'work',$2,'published')`,[entity,max]);
 const p=payload(entity);
 // A separate submitted revision is required by unique publication attribution.
 const submitted=await v.submissions.create(v.author.accessToken,{schemaVersion:1,payload:p,expectedRevision:0,idempotencyKey:randomUUID()});
 await db.query(`INSERT INTO ${v.q}.published_revisions(entity_id,revision,submission_id,submission_revision,actor_id,payload,source_evidence) VALUES($1,$2,$3,1,$4,$5,jsonb_path_query_array($5::jsonb,'$.fields[*].evidence[*]'))`,[entity,max,submitted.body.id,v.mod.principalId,p]);await db.query('COMMIT');
 const target=await create(v,{entity,expected:0,open:false});const r=await comparison(v,target);assert.equal(r.body.entityRevision,max);assert.equal(r.body.fields[0].value,'Synthetic work');
 }catch(e){await db.query('ROLLBACK');throw e;}
});
test('PG strict Comparison query/body/method and no extra DTO timestamps',async()=>{
 const v=await venue(),s=await create(v),path='/v1/admin/entities/'+s.entity+'/comparison';
 for(const suffix of ['', '?x=1','?submissionId='+s.id+'&submissionId='+s.id,'?submissionId='+s.id+'&extra=1','?submissionId=bad'])assert.equal((await v.send(v.mod,'GET',path+suffix)).status,400);
 assert.equal((await v.send(v.mod,'GET',path+'?submissionId='+s.id,{})).status,400);
 const r=await v.send(v.mod,'POST',path+'?submissionId='+s.id,{});assert.equal(r.status,405);assert.equal(r.headers.Allow,'GET');
});
test('PG Comparison policy overflow refuses all values rather than truncating',async()=>{
 const v=await venue(),s=await published(v),policy=syntheticValidator();policy.fields.work.syntheticTitle.maxBytes=3;v.fresh({validator:policy});const prior=await snapshot(v);
 assert.equal((await comparison(v,s)).body.reason,'projection_unavailable');assert.deepEqual(await snapshot(v),prior);
});
test('PG suspended account loses Comparison access immediately',async()=>{
 const v=await venue(),s=await published(v);assert.equal((await comparison(v,s)).body.state,'available');
 await db.query(`UPDATE ${v.q}.principals SET state='suspended' WHERE id=$1`,[v.mod.principalId]);assert.equal((await comparison(v,s)).status,404);
});
test('PG unresolved mapping returns unmapped union; no inferred entity values',async()=>{
 const v=await venue(),p=payload(randomUUID());delete p.target.catalogueUuid;p.target.externalIdentity={provider:'wikidata',providerKind:'work',externalId:'Q123'};
 const s=await v.submissions.create(v.author.accessToken,{schemaVersion:1,payload:p,expectedRevision:0,idempotencyKey:randomUUID()});const prior=await snapshot(v);
 const r=await comparison(v,{id:s.body.id,entity:randomUUID()});assert.equal(r.status,200);assert.equal(r.body.reason,'unmapped');assert.equal(r.body.entityId,null);assert.deepEqual(await snapshot(v),prior);
});
async function storedFixture(v,transform){
 const entity=randomUUID(),sid=randomUUID(),p=payload(entity);transform(p);
 await db.query('BEGIN');try{
 await db.query(`INSERT INTO ${v.q}.submissions(id,author_id,target_kind,catalogue_uuid,expected_revision,current_revision) VALUES($1,$2,$3,$4,0,1)`,[sid,v.author.principalId,p.target.kind,entity]);
 await db.query(`INSERT INTO ${v.q}.submission_revisions(submission_id,revision,payload,prev_hash) VALUES($1,1,$2,repeat('0',64))`,[sid,p]);
 await db.query(`INSERT INTO ${v.q}.published_entities(id,kind,revision,status) VALUES($1,$2,1,'published')`,[entity,p.target.kind]);
 await db.query(`INSERT INTO ${v.q}.published_revisions(entity_id,revision,submission_id,submission_revision,actor_id,payload,source_evidence) VALUES($1,1,$2,1,$3,$4,jsonb_path_query_array($4::jsonb,'$.fields[*].evidence[*]'))`,[entity,sid,v.mod.principalId,p]);await db.query('COMMIT');
 }catch(e){await db.query('ROLLBACK');throw e;}
 return {id:sid,entity};
}
test('PG actual unapproved stored fields and nested private provenance rejected',async()=>{
 const v=await venue();
 for(const transform of [p=>p.fields[0].privateProvenance='PRIVATE-NEVER-DISCLOSE',p=>p.fields[0].evidence[0].privateActor='PRIVATE-NEVER-DISCLOSE',p=>p.fields[0].field='unapproved']){
 const s=await storedFixture(v,transform),prior=await snapshot(v),r=await comparison(v,s);assert.equal(r.body.reason,'projection_unavailable');assert.ok(!JSON.stringify(r).includes('PRIVATE-NEVER-DISCLOSE'));assert.deepEqual(await snapshot(v),prior);
 }
});
test('PG actual unsupported entity kind and >20 stored fields refuse whole projection',async()=>{
 const v=await venue();for(const transform of [p=>p.target.kind='unsupported_synthetic',p=>p.fields=Array.from({length:21},(_,i)=>({...p.fields[0],field:'synthetic'+i}))]){
 const s=await storedFixture(v,transform),r=await comparison(v,s);assert.equal(r.body.reason,'projection_unavailable');assert.ok(!Object.hasOwn(r.body,'fields'));
 }
});
test('PG deletion of published target is restricted; missing head uses absent without proposal fallback',async()=>{
 const v=await venue(),s=await published(v),prior=await snapshot(v);
 await assert.rejects(db.query(`DELETE FROM ${v.q}.published_entities WHERE id=$1`,[s.entity]),e=>e.code==='23503');assert.deepEqual(await snapshot(v),prior);
 const missing=await create(v);const r=await comparison(v,missing);assert.equal(r.body.reason,'absent');assert.ok(!Object.hasOwn(r.body,'fields'));
});
