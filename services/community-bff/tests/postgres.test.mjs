import test from 'node:test';
import assert from 'node:assert/strict';
import {randomUUID} from 'node:crypto';
import {venue,intent,payload} from '../../../spikes/stage6-api-v1-local/tests/pg-http-venue.mjs';
import {migrateOperations} from '../../../spikes/stage6-public-admin-abuse/src/storage.mjs';
import {createBffAdapter} from '../adapter.mjs';
import {createFetchHandler} from '../fetch.mjs';

async function setup(t){
 const v=await venue(t,{adapterFactory:createBffAdapter,migrate:migrateOperations});
 // Explicit synthetic-only policy and disposable schema; never hosted intake.
 await v.db.query(`UPDATE ${v.q}.intake_control SET enabled=true`);
 const handle=createFetchHandler({createAdapter:async()=>({adapter:v.adapter()})});
 v.fetch=async(user,method,path,body)=>{const r=await handle(new Request('https://private.invalid'+path,{method,headers:{...(user?{authorization:'Bearer '+user.accessToken}:{}),...(body?{'content-type':'application/json'}:{})},...(body?{body:JSON.stringify(body)}:{})}),{}, {remoteAddress:'127.0.0.1'});return {status:r.status,body:await r.json(),headers:r.headers};};
 v.contribute=(user=v.author,extra={})=>v.fetch(user,'POST','/v1/submissions',{schemaVersion:1,payload:payload(randomUUID()),expectedRevision:'0',idempotencyKey:randomUUID(),...extra});return v;
}
test('Fetch → canonical BFF → PostgreSQL contribution, immutable revisions and exact replay',async t=>{
 const v=await setup(t),body={schemaVersion:1,payload:payload(randomUUID()),expectedRevision:'0',idempotencyKey:randomUUID()};
 const first=await v.fetch(v.author,'POST','/v1/submissions',body);assert.equal(first.status,201,JSON.stringify(first));assert.equal(first.body.revision,'1');
 const replay=await v.fetch(v.author,'POST','/v1/submissions',body);assert.deepEqual(replay,first);
 assert.equal((await v.fetch(v.author,'POST','/v1/submissions',{...body,expectedRevision:'1'})).status,422);
 const read=await v.fetch(v.author,'GET','/v1/submissions/'+first.body.id);assert.equal(read.status,200);assert.equal(read.body.revisions.length,1);assert.ok(!JSON.stringify(read.body).includes('author_id'));
 assert.equal((await v.fetch(v.other,'GET','/v1/submissions/'+first.body.id)).status,404);
 const mine=await v.fetch(v.author,'GET','/v1/submissions/mine');assert.equal(mine.status,200);assert.equal(mine.body.items.length,1);
});
test('Fetch BFF default-deny authorization, strict JSON, quotas and kill switch',async t=>{
 const v=await setup(t);assert.equal((await v.contribute(null)).status,401);
 assert.equal((await v.contribute(v.mod)).status,403);
 assert.equal((await v.contribute(v.author,{expectedRevision:0})).status,400);
 assert.equal((await v.contribute(v.author,{authorId:v.other.principalId})).status,400);
 for(let n=0;n<5;n++)assert.equal((await v.contribute()).status,201);
 const limited=await v.contribute();assert.equal(limited.status,429);assert.ok(Number(limited.headers.get('retry-after'))>0);
 await v.db.query(`UPDATE ${v.q}.intake_control SET enabled=false`);
 const s=await v.create({open:true});const result=await v.fetch(v.mod,'POST',s.path+'/decisions',intent({decision:'approve'}));assert.equal(result.status,503);
 assert.equal((await v.db.query(`SELECT count(*) FROM ${v.q}.published_entities`)).rows[0].count,'0');
});
test('Fetch BFF canonical moderation and public feed have actual PG effects',async t=>{
 const v=await setup(t),s=await v.create({open:true});
 // Fixture policy uses syntheticTitle, which the public title projection denies.
 const approved=await v.fetch(v.mod,'POST',s.path+'/decisions',intent({decision:'approve'}));assert.equal(approved.status,201,JSON.stringify(approved));assert.equal(approved.body.publishedEntityRevision,'1');
 const feed=await v.fetch(null,'GET','/v1/catalogue/changes');assert.equal(feed.status,200);assert.equal(feed.body.items.length,1);assert.equal(feed.body.items[0].revision,'1');assert.ok(feed.body.pollCursor);
 const denied=await v.fetch(v.author,'POST',`/v1/admin/entities/${s.entity}/quarantine`,{schemaVersion:1,expectedRevision:'1',expectedEventSeq:'1',reason:'Synthetic reason',idempotencyKey:randomUUID()});assert.equal(denied.status,403);
 const quarantine=await v.fetch(v.admin,'POST',`/v1/admin/entities/${s.entity}/quarantine`,{schemaVersion:1,expectedRevision:'1',expectedEventSeq:'1',reason:'Synthetic reason',idempotencyKey:randomUUID()});assert.equal(quarantine.status,200,JSON.stringify(quarantine));
 assert.equal((await v.fetch(null,'GET','/v1/catalogue/changes')).body.items.at(-1).status,'quarantined');
});
test('Fetch BFF request-changes, immutable revise, reopen and withdrawal lifecycle',async t=>{
 const v=await setup(t),s=await v.create({open:true});
 assert.equal((await v.fetch(v.mod,'POST',s.path+'/decisions',intent({decision:'request_changes',reason:'Synthetic correction'}))).status,200);
 const changed=payload(s.entity);changed.fields[0].newValue='Synthetic revision two';
 const revised=await v.fetch(v.author,'POST',`/v1/submissions/${s.id}/revisions`,{schemaVersion:1,payload:changed,expectedRevision:'0',expectedSubmissionRevision:'1',idempotencyKey:randomUUID()});
 assert.equal(revised.status,201,JSON.stringify(revised));assert.equal(revised.body.revision,'2');
 const read=await v.fetch(v.author,'GET','/v1/submissions/'+s.id);assert.equal(read.body.revisions.length,2);assert.equal(read.body.revisions[0].payload.fields[0].newValue,'Synthetic work');
 const withdrawal=await v.fetch(v.author,'POST',`/v1/submissions/${s.id}/withdraw`,{schemaVersion:1,expectedRevision:'0',expectedSubmissionRevision:'2',reason:'Synthetic withdrawal',idempotencyKey:randomUUID()});assert.equal(withdrawal.status,200);
 assert.equal((await v.fetch(v.author,'GET','/v1/submissions/'+s.id)).body.submission.status,'withdrawn');
});
test('Fetch BFF live role grants/revocation and suspension use server authorization',async t=>{
 const v=await setup(t),path=`/v1/admin/principals/${v.other.principalId}`;
 assert.equal((await v.fetch(v.other,'GET','/v1/admin/queue')).status,403);
 const grant={schemaVersion:1,role:'moderator',revoke:false,idempotencyKey:randomUUID()};
 assert.equal((await v.fetch(v.admin,'POST',path+'/roles',grant)).status,200);
 assert.equal((await v.fetch(v.other,'GET','/v1/admin/queue')).status,200);
 assert.equal((await v.fetch(v.other,'POST',path+'/roles',{...grant,role:'administrator',idempotencyKey:randomUUID()})).status,403);
 assert.equal((await v.fetch(v.admin,'POST',path+'/roles',{...grant,revoke:true,idempotencyKey:randomUUID()})).status,200);
 assert.equal((await v.fetch(v.other,'GET','/v1/admin/queue')).status,403);
 assert.equal((await v.fetch(v.admin,'POST',path+'/suspend',{schemaVersion:1,suspended:true,idempotencyKey:randomUUID()})).status,200);
 assert.equal((await v.fetch(v.other,'GET','/v1/session')).status,403);
});
test('Fetch BFF public overlay exposes only approved projection and rollback follows feed sequence',async t=>{
 const v=await setup(t),validator=v.adapter().core.validator;
 // Explicit disposable synthetic policy; no production policy is modified.
 validator.fields.work.title={type:'text',maxBytes:2048};validator.sources[0].fields.push('title');
 const entity=randomUUID();
 const submit=async(expected,title)=>{
  const p=payload(entity);p.fields[0].field='title';p.fields[0].newValue=title;
  const created=await v.contribute(v.author,{payload:p,expectedRevision:String(expected)});assert.equal(created.status,201);
  const path='/v1/admin/submissions/'+created.body.id;
  assert.equal((await v.fetch(v.mod,'POST',path+'/open',intent({expectedEntityRevision:String(expected)}))).status,200);
  assert.equal((await v.fetch(v.mod,'POST',path+'/decisions',intent({decision:'approve',expectedEntityRevision:String(expected)}))).status,201);
 };
 await submit(0,'Synthetic first');await submit(1,'Synthetic second');
 const page=await v.fetch(null,'GET','/v1/catalogue/overlay?kind=work');assert.equal(page.status,200);assert.equal(page.body.items[0].displayTitle,'Synthetic second');assert.ok(!JSON.stringify(page.body).includes('author'));
 const rollback=await v.fetch(v.admin,'POST',`/v1/admin/entities/${entity}/rollback`,{schemaVersion:1,expectedRevision:'2',expectedEventSeq:'2',targetRevision:'1',reason:'Synthetic rollback',idempotencyKey:randomUUID()});assert.equal(rollback.status,200,JSON.stringify(rollback));
 assert.equal((await v.fetch(null,'GET','/v1/catalogue/overlay')).body.items[0].displayTitle,'Synthetic first');
});
test('Fetch BFF cursor boundary is bounded, principal-bound and expires',async t=>{
 const v=await setup(t);for(let i=0;i<21;i++)await v.create();
 const first=await v.fetch(v.author,'GET','/v1/submissions/mine');assert.equal(first.body.items.length,20);assert.equal(typeof first.body.nextCursor,'string');
 const path='/v1/submissions/mine?cursor='+encodeURIComponent(first.body.nextCursor);
 assert.equal((await v.fetch(v.author,'GET',path)).body.items.length,1);
 assert.equal((await v.fetch(v.other,'GET',path)).status,400);
 v.advance(900000);
 const refreshed=await v.fetch(null,'POST','/v1/session/refresh',{schemaVersion:1,refreshToken:v.author.refreshToken});assert.equal(refreshed.status,200);
 assert.equal((await v.fetch(refreshed.body,'GET',path)).status,400);
 assert.equal((await v.fetch(null,'GET','/v1/catalogue/changes?cursor=forged')).status,400);
});

test('restricted non-owner BFF role executes sessions, contribution/review/publication and denies DDL/ledger/intake edits',async t=>{
 const {runtimeAccessSql,runtimePrivileges}=await import('../runtime-access.mjs');
 const {connect}=await import('../../../spikes/stage6-api-v1-local/tests/pg-http-venue.mjs');
 const v=await setup(t),role='goldberg_bff_'+randomUUID().replaceAll('-','');
 await v.db.query(`CREATE ROLE "${role}" LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS`);
 t.after(async()=>{const c=await connect();try{await c.query(`DROP ROLE "${role}"`);}finally{await c.end();}});
 await v.db.query(runtimeAccessSql({schema:v.schema,role}));
 const {default:pg}=await import('pg');
 const login=async user=>{const c=new pg.Client({host:'127.0.0.1',port:Number(process.env.SCHEMA_PG_PORT??54339),database:'schema_prototype',user,password:'',connectionTimeoutMillis:3000});await c.connect();return c;};
 const restricted=async()=>{const c=await login(role);const query=c.query.bind(c);c.query=async(...args)=>{try{return await query(...args);}catch(e){t.diagnostic("synthetic runtime SQL: "+e.code+" "+e.message);throw e;}};return c;};
 await v.fresh({connect:restricted});
 // setup's Fetch closure resolves the current adapter after fresh().
 const ch=await v.fetch(null,'POST','/v1/session/challenge',{schemaVersion:1});assert.equal(ch.status,201);
 const signed=await v.fetch(null,'POST','/v1/session',{schemaVersion:1,challenge:ch.body.challenge,credential:JSON.stringify({issuer:'https://synthetic.invalid',audience:'local-test',subject:'restricted-runtime',nonce:ch.body.nonce,iat:v.clock(),exp:v.clock()+120000})});assert.equal(signed.status,201,JSON.stringify(signed));
 const created=await v.contribute();assert.equal(created.status,201,JSON.stringify(created));
 const path='/v1/admin/submissions/'+created.body.id;
 assert.equal((await v.fetch(v.other,'GET',path)).status,403);
 assert.equal((await v.fetch(v.mod,'POST',path+'/open',intent())).status,200);
 const decision=intent({decision:'approve'});
 const approved=await v.fetch(v.mod,'POST',path+'/decisions',decision);assert.equal(approved.status,201,JSON.stringify(approved));
 assert.deepEqual(await v.fetch(v.mod,'POST',path+'/decisions',decision),approved);
 assert.equal((await v.fetch(null,'GET','/v1/catalogue/changes')).body.items.length,1);
 assert.equal((await v.fetch(v.other,'GET','/v1/submissions/'+created.body.id)).status,404);
 const refresh=await v.fetch(null,'POST','/v1/session/refresh',{schemaVersion:1,refreshToken:signed.body.refreshToken});assert.equal(refresh.status,200);
 const c=await restricted();try{
  for(const sql of [`CREATE TABLE ${v.q}.forbidden(id int)`,`ALTER TABLE ${v.q}.principals DISABLE ROW LEVEL SECURITY`,`TRUNCATE ${v.q}.audit_events`,`DELETE FROM ${v.q}.audit_events`,`UPDATE ${v.q}.intake_control SET enabled=true`,`SELECT * FROM ${v.q}.schema_migrations`,`SELECT * FROM ${v.q}.auth_migrations`,`SELECT * FROM ${v.q}.operations_migrations`])await assert.rejects(c.query(sql),e=>e.code==='42501',sql);
  const {rows:[flags]}=await c.query('SELECT rolsuper,rolbypassrls,rolcreaterole FROM pg_roles WHERE rolname=current_user');assert.deepEqual(flags,{rolsuper:false,rolbypassrls:false,rolcreaterole:false});
  const {rows:[n]}=await c.query('SELECT count(*) FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace WHERE n.nspname=$1 AND c.relkind=\'r\' AND c.relrowsecurity',[v.schema]);assert.equal(Number(n.count),Object.keys(runtimePrivileges).length);
 }finally{await c.end();}
 // Policies remain tied to the server role: table ACL alone grants no rows to a
 // different client role. Revoke runtime membership and no user token can add it.
 const outsider='goldberg_bff_'+randomUUID().replaceAll('-','');await v.db.query(`CREATE ROLE "${outsider}" LOGIN`);
 try{
  await v.db.query(`GRANT USAGE ON SCHEMA ${v.q} TO "${outsider}"; GRANT SELECT ON ${v.q}.principals TO "${outsider}"`);
  const o=await login(outsider);try{assert.equal((await o.query(`SELECT * FROM ${v.q}.principals`)).rowCount,0);await assert.rejects(o.query(`SET ROLE "${role}"`),e=>e.code==='42501');}finally{await o.end();}
 }finally{await v.db.query(`DROP OWNED BY "${outsider}"; DROP ROLE "${outsider}"`);}
});

test('runtime access plan rejects unsafe identifiers and elevated roles atomically',async t=>{
 const {runtimeAccessSql}=await import('../runtime-access.mjs');
 assert.throws(()=>runtimeAccessSql({schema:'public'}));assert.throws(()=>runtimeAccessSql({role:'postgres'}));assert.throws(()=>runtimeAccessSql({role:'goldberg_bff_a;drop'}));
 const v=await setup(t),role='goldberg_bff_'+randomUUID().replaceAll('-','');
 await v.db.query(`CREATE ROLE "${role}" NOLOGIN BYPASSRLS`);
 try{await assert.rejects(v.db.query(runtimeAccessSql({schema:v.schema,role})),/unprivileged/);await v.db.query('ROLLBACK');assert.equal((await v.db.query('SELECT count(*) FROM pg_policy WHERE polrelid=$1::regclass',[v.schema+'.principals'])).rows[0].count,'0');}
 finally{await v.db.query('ROLLBACK');await v.db.query(`DROP ROLE "${role}"`);}
});
