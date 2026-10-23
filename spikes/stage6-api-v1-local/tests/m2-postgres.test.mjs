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
after(async()=>{if(db){for(const s of schemas)await db.query(`DROP SCHEMA ${identifier(s)} CASCADE`);await db.end();}if(process.env.M2_DETAIL_PATH)writeFileSync(process.env.M2_DETAIL_PATH,JSON.stringify(detailCaptures,null,2)+'\n');});
async function venue(){
 const schema='proto_m2_'+randomUUID().replaceAll('-',''),q=identifier(schema);schemas.push(schema);await migrateAuth(db,schema);
 let now=Date.now(),fault;const clock=()=>now;
 const verifier={verify:async raw=>JSON.parse(raw)}; // Synthetic identity only, no production credential verifier.
 const sessions=new SessionService({connect,schema,verifier,clock,accessTtlMs:900000,refreshTtlMs:604800000,challengeTtlMs:300000});
 const authorization=new Authorization({sessions,clock}),validator=syntheticValidator();
 const submissions=new SubmissionService({authorization,validator,idempotency:new Idempotency({schema,clock})});
 const faultConnect=async()=>{const c=await connect(),query=c.query.bind(c);c.query=async(sql,args)=>{if(fault){const result=await fault(sql,args,c,query);if(result)return result;}return query(sql,args);};return c;};
 const factory=(options={})=>createLocalAdapter({localOnly:true,connect:faultConnect,schema,verifier,clock,validator,...options});
 let adapter=factory();
 const send=async(u,method,path,body)=>{const r=await adapter.invoke({method,path,headers:{...(u?{Authorization:'Bearer '+u.accessToken}:{}),...(body?{'Content-Type':'application/json'}:{})},...(body?{body:JSON.stringify(body)}:{})});if(method==='GET'&&r.status===200&&r.body?.submissionId)detailCaptures.push(r.body);return r;};
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

test('PG detail reflects persisted submitted/open/published; exact DTO fields, no read effects',async()=>{
 const v=await venue(),s=await create(v,{open:false}),prior=await snapshot(v);let r=await detail(v,s);assert.equal(r.status,200,JSON.stringify(r));
 assert.equal(r.body.status,'submitted');assert.equal(r.body.expectedEntityRevision,'0');assert.equal(r.body.submissionRevision,'1');assert.equal(r.body.targetState,'new');assert.deepEqual(await snapshot(v),prior);
 assert.deepEqual(Object.keys(r.body).sort(),['schemaVersion','requestId','submissionId','authorId','status','kind','targetEntityId','targetState','expectedEntityRevision','submissionRevision','createdAt','updatedAt','revisions','reviews'].sort());
 assert.equal((await v.send(v.mod,'POST',s.path+'/open',body())).status,200);assert.equal((await detail(v,s)).body.status,'in_review');assert.equal((await decide(v,s)).status,201);
 r=await detail(v,s);assert.equal(r.body.status,'published');assert.equal(r.body.submissionRevision,'1');assert.equal(r.body.expectedEntityRevision,'0');assert.equal(r.body.reviews.items.length,3);assert.equal(r.body.revisions.items.length,1);assert.ok(!JSON.stringify(r).includes('source_evidence'));await chain(v);
});
test('PG independent entity7/proposal3 publishes8; stale expectations conflict and refetch stays authoritative',async()=>{
 const v=await venue();let s=await create(v);assert.equal((await decide(v,s)).status,201);
 // Seven real publications, not scripted SQL effects.
 for(let i=1;i<7;i++){s=await create(v,{entity:s.entity,expected:i});assert.equal((await decide(v,s,body({decision:'approve',expectedEntityRevision:String(i)}))).status,201);}
 s=await create(v,{entity:s.entity,expected:7});
 for(let rev=1;rev<3;rev++){
  assert.equal((await decide(v,s,body({decision:'request_changes',reason:'Synthetic revision requested',expectedEntityRevision:'7',expectedSubmissionRevision:String(rev)}))).status,200);
  await v.submissions.revise(v.author.accessToken,{schemaVersion:1,id:s.id,payload:payload(s.entity),expectedRevision:7,expectedSubmissionRevision:rev,idempotencyKey:randomUUID()});
 }
 assert.equal((await decide(v,s,body({decision:'approve',expectedEntityRevision:'7',expectedSubmissionRevision:'2'}))).status,409);
 const d=await detail(v,s);assert.equal(d.body.expectedEntityRevision,'7');assert.equal(d.body.submissionRevision,'3');
 const r=await decide(v,s,body({decision:'approve',expectedEntityRevision:'7',expectedSubmissionRevision:'3'}));assert.equal(r.status,201);assert.equal(r.body.publishedEntityRevision,'8');assert.equal((await detail(v,s)).body.status,'published');await chain(v);
});
test('PG changed live entity baseline cannot be waived by refetch/reconfirmation',async()=>{
 const v=await venue(),s=await create(v),other=await create(v,{entity:s.entity});assert.equal((await decide(v,other)).status,201);const prior=await snapshot(v);
 assert.equal((await decide(v,s)).status,409);assert.equal((await detail(v,s)).body.expectedEntityRevision,'0');assert.equal((await decide(v,s,body({decision:'approve',expectedEntityRevision:'1'}))).status,409);assert.deepEqual(await snapshot(v),prior);
});
test('PG live authorization, revocation, suspension and account isolation before detail or replay',async()=>{
 const v=await venue(),s=await create(v),b=body({decision:'approve'});assert.equal((await decide(v,s,b)).status,201);
 for(const p of [s.path,'/v1/admin/submissions/'+randomUUID()])for(const u of [v.author,v.other])assert.equal((await v.send(u,'GET',p)).status,403);
 assert.equal((await detail(v,s,null)).status,401);assert.equal((await v.send(v.mod,'GET','/v1/admin/submissions/'+randomUUID())).status,404);
 assert.equal((await decide(v,s,b,v.other)).status,403);
 await db.query(`UPDATE ${v.q}.role_grants SET revoked_at=clock_timestamp() WHERE principal_id=$1`,[v.mod.principalId]);assert.equal((await detail(v,s)).status,403);assert.equal((await decide(v,s,b)).status,403);
 await v.sessions.logout(v.mod2.accessToken);assert.equal((await detail(v,s,v.mod2)).status,401);
 await db.query(`UPDATE ${v.q}.principals SET state='suspended' WHERE id=$1`,[v.author.principalId]);assert.equal((await detail(v,s,v.author)).status,403);
});
test('PG exact receipt replay after state change/fresh adapter; changed body422; no second effects',async()=>{
 const v=await venue(),s=await create(v),b=body({decision:'approve'}),r=await decide(v,s,b);assert.equal(r.status,201);const prior=await snapshot(v);
 v.fresh();const replay=await decide(v,s,b);assert.deepEqual(replay,r);assert.deepEqual(await snapshot(v),prior);
 assert.equal((await decide(v,s,{...b,decision:'reject',reason:'Changed intent'})).status,422);
 const c=await connect();try{const stored=(await c.query(`SELECT response,http_status FROM ${v.q}.idempotency_results WHERE principal_id=$1 AND key=$2`,[v.mod.principalId,b.idempotencyKey])).rows[0];assert.deepEqual(stored.response,r.body);assert.equal(stored.http_status,r.status);}finally{await c.end();}
 await chain(v);
});
for(const decision of ['reject','request_changes'])test('PG '+decision+'200 exact receipt and persisted detail',async()=>{
 const v=await venue(),s=await create(v),b=body({decision,reason:'Synthetic reason'}),r=await decide(v,s,b);assert.equal(r.status,200);assert.equal((await detail(v,s)).body.status,decision==='reject'?'rejected':'changes_requested');assert.deepEqual(await decide(v,s,b),r);await chain(v);
});
for(const boundary of ['projection','receipt','deferred'])test('PG atomic rollback of effects/audit/receipt at '+boundary,async()=>{
 const v=await venue(),s=await create(v),b=body({decision:'approve'}),prior=await snapshot(v);
 v.setFault(async(sql,args,c,query)=>{
  if(boundary==='projection'&&sql.startsWith('SELECT expected_revision::text'))throw Error('Synthetic projection fault');
  if(boundary==='receipt'&&sql.startsWith('INSERT INTO')&&sql.includes('.idempotency_results('))throw Error('Synthetic receipt fault');
  if(boundary==='deferred'&&sql==='COMMIT')await query(`INSERT INTO ${v.q}.published_entities(id,kind,revision,status) VALUES($1,'work',1,'published')`,[randomUUID()]);
 });
 assert.equal((await decide(v,s,b)).status,503);v.setFault(null);assert.deepEqual(await snapshot(v),prior);assert.equal((await decide(v,s,b)).status,201);await chain(v);
});
test('PG concurrent competing moderators: one201 and one409, coherent atomic chain',async()=>{
 const v=await venue(),s=await create(v);const results=await Promise.all([decide(v,s),decide(v,s,body({decision:'approve'}),v.mod2)]);assert.deepEqual(results.map(r=>r.status).sort(),[201,409]);assert.equal(await count(v,'published_revisions'),1);assert.equal(await count(v,'change_feed'),1);await chain(v);
});
test('PG simultaneous same-key requests exact replay; changed-body same-key race422',async()=>{
 const v=await venue(),s=await create(v),b=body({decision:'approve'});const rs=await Promise.all([decide(v,s,b),decide(v,s,b)]);assert.deepEqual(rs[0],rs[1]);assert.equal(rs[0].status,201);assert.equal(await count(v,'published_revisions'),1);
 const t=await create(v),key=randomUUID(),race=await Promise.all([decide(v,t,body({idempotencyKey:key,decision:'reject',reason:'Synthetic reason'})),decide(v,t,body({idempotencyKey:key,decision:'request_changes',reason:'Synthetic reason'}))]);assert.deepEqual(race.map(r=>r.status).sort(),[200,422]);await chain(v);
});
test('PG receipt retention exactly24h from completion; before/at/after expiry under controlled clock',async()=>{
 const v=await venue(),s=await create(v),b=body({decision:'approve'});const start=v.clock();let moved=false;
 v.setFault(async sql=>{if(!moved&&sql.startsWith('SELECT expected_revision::text')){moved=true;v.advance(4567);}});
 const r=await decide(v,s,b);assert.equal(r.status,201);v.setFault(null);
 const stored=(await db.query(`SELECT created_at,expires_at FROM ${v.q}.idempotency_results WHERE principal_id=$1 AND key=$2`,[v.mod.principalId,b.idempotencyKey])).rows[0];assert.equal(+stored.created_at,start+4567);assert.equal(+stored.expires_at,+stored.created_at+86400000);
 v.advance(86400000-1);v.mod=await v.user('moderator');assert.deepEqual(await decide(v,s,b),r);
 v.advance(1);assert.equal((await decide(v,s,b)).status,409);v.advance(1);assert.equal((await decide(v,s,b)).status,409);
 // Failed expired replay rolls back its cleanup; old expired row is not resurrected as valid.
 assert.equal((await db.query(`SELECT response FROM ${v.q}.idempotency_results WHERE key=$1`,[b.idempotencyKey])).rows[0].response.requestId,r.body.requestId);await chain(v);
});
test('PG lossless full-int8 reads and UTC microseconds; writes stay fail closed',async()=>{
 const v=await venue(),entity=randomUUID(),sid=randomUUID(),max='9223372036854775807';
 await db.query('BEGIN');try{
  await db.query(`INSERT INTO ${v.q}.submissions(id,author_id,target_kind,catalogue_uuid,expected_revision,current_revision,created_at,updated_at) VALUES($1,$2,'work',$3,$4,1,'2030-01-01T12:00:00.123456Z','2030-01-01T12:00:00.123457Z')`,[sid,v.author.principalId,entity,max]);
  await db.query(`INSERT INTO ${v.q}.submission_revisions(submission_id,revision,payload,prev_hash,created_at) VALUES($1,1,$2,repeat('0',64),'2030-01-01T12:00:00.999999Z')`,[sid,payload(entity)]);await db.query('COMMIT');
 }catch(e){await db.query('ROLLBACK');throw e;}
 const s={id:sid,path:'/v1/admin/submissions/'+sid};const r=await detail(v,s);assert.equal(r.status,200,JSON.stringify(r));assert.equal(r.body.expectedEntityRevision,max);assert.equal(r.body.submissionRevision,'1');assert.equal(r.body.createdAt,'2030-01-01T12:00:00.123456Z');assert.equal(r.body.updatedAt,'2030-01-01T12:00:00.123457Z');assert.equal(r.body.revisions.items[0].createdAt,'2030-01-01T12:00:00.999999Z');
 assert.equal((await decide(v,s,body({decision:'approve',expectedEntityRevision:max,expectedSubmissionRevision:max}))).status,503);
});
test('PG empty real policy denies payload disclosure; strict detail routing and no body/query',async()=>{
 const v=await venue(),s=await create(v);v.fresh({validator:undefined});assert.equal((await detail(v,s)).status,503);
 for(const path of [s.path+'?reviewCursor=abc',s.path+'?x=1','/v1/admin/submissions/not-a-uuid'])assert.equal((await v.send(v.mod,'GET',path)).status,400);
 assert.equal((await v.send(v.mod,'GET',s.path,{schemaVersion:1})).status,400);assert.equal((await v.send(v.mod,'POST',s.path,{})).status,405);assert.equal((await v.send(v.mod,'GET',s.path+'/')).status,404);
});
test('PG detail coherent snapshot during a concurrent committed decision',async()=>{
 const v=await venue(),s=await create(v);let entered,release;const seen=new Promise(r=>entered=r),hold=new Promise(r=>release=r);
 // Hold the executed content snapshot before returning it while a different moderator commits.
 v.setFault(async(sql,args,c,query)=>{if(sql.startsWith('SELECT s.id,s.author_id')){const result=await query(sql,args);entered();await hold;return result;}});
 const pending=detail(v,s);await seen;
 // Different moderator avoids same-principal session lock; real commit after snapshot acquisition.
 assert.equal((await decide(v,s,body({decision:'approve'}),v.mod2)).status,201);release();
 const r=await pending;assert.equal(r.status,200);assert.equal(r.body.status,'in_review');assert.equal(r.body.reviews.items.at(-1).to,'in_review');v.setFault(null);assert.equal((await detail(v,s)).body.status,'published');await chain(v);
});

test('PG bounded history numeric order and overflow unavailable without truncation or mutation',async()=>{
 const v=await venue(),s=await create(v);
 for(let rev=1;rev<=9;rev++){
  assert.equal((await decide(v,s,body({decision:'request_changes',reason:'Synthetic revision',expectedSubmissionRevision:String(rev)}))).status,200);
  await v.submissions.revise(v.author.accessToken,{schemaVersion:1,id:s.id,payload:payload(s.entity),expectedRevision:0,expectedSubmissionRevision:rev,idempotencyKey:randomUUID()});
 }
 const d=await detail(v,s);assert.equal(d.status,200,JSON.stringify(d));assert.equal(d.body.revisions.items.length,10);assert.equal(d.body.revisions.items[0].submissionRevision,'10');assert.equal(d.body.reviews.items.length,19);
 const seqs=d.body.reviews.items.map(r=>BigInt(r.auditSeq));assert.deepEqual(seqs,[...seqs].sort((a,b)=>a<b?-1:1));
 assert.equal((await decide(v,s,body({decision:'request_changes',reason:'Synthetic revision',expectedSubmissionRevision:'10'}))).status,200);
 await v.submissions.revise(v.author.accessToken,{schemaVersion:1,id:s.id,payload:payload(s.entity),expectedRevision:0,expectedSubmissionRevision:10,idempotencyKey:randomUUID()});
 const prior=await snapshot(v);assert.equal((await detail(v,s)).status,503);assert.deepEqual(await snapshot(v),prior);await chain(v);
});
test('PG revoked session denies exact receipt replay; another reviewer key namespace independent',async()=>{
 const v=await venue(),s=await create(v),b=body({decision:'approve'});assert.equal((await decide(v,s,b)).status,201);
 assert.equal((await decide(v,s,b,v.mod2)).status,409);await v.sessions.logout(v.mod.accessToken);assert.equal((await decide(v,s,b)).status,401);assert.equal((await detail(v,s)).status,401);
});
test('PG unsupported stored fields fail closed and missing entity never rewrites recorded baseline',async()=>{
 const v=await venue(),s=await create(v,{expected:7});const d=await detail(v,s);assert.equal(d.status,200);assert.equal(d.body.expectedEntityRevision,'7');assert.equal(d.body.targetState,'existing');assert.equal((await decide(v,s,body({decision:'approve',expectedEntityRevision:'7'}))).status,409);
 const bad=payload(randomUUID());bad.fields[0].newValue='Never disclose unapproved text';bad.fields[0].field='unapproved';
 const sub=randomUUID();await db.query('BEGIN');try{await db.query(`INSERT INTO ${v.q}.submissions(id,author_id,target_kind,catalogue_uuid,expected_revision,current_revision) VALUES($1,$2,'work',$3,0,1)`,[sub,v.author.principalId,bad.target.catalogueUuid]);await db.query(`INSERT INTO ${v.q}.submission_revisions(submission_id,revision,payload,prev_hash) VALUES($1,1,$2,repeat('0',64))`,[sub,bad]);await db.query('COMMIT');}catch(e){await db.query('ROLLBACK');throw e;}
 const r=await detail(v,{path:'/v1/admin/submissions/'+sub});assert.equal(r.status,503);assert.ok(!JSON.stringify(r).includes('Never disclose'));
});
