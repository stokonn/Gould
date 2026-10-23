import {test,before,after} from 'node:test';
import assert from 'node:assert/strict';
import {randomUUID,randomBytes} from 'node:crypto';
import {localClient,identifier} from '../../stage6-schema-pg/src/database.mjs';
import {SessionService} from '../../stage6-auth-local/src/sessions.mjs';
import {IdentityVerifier} from '../../stage6-auth-local/src/identity.mjs';
import {Authorization} from '../../stage6-auth-local/src/authorization.mjs';
import {FakeClock,FakeJwks,makeKey,credential,issuer,audience} from '../../stage6-auth-local/tests/fixtures.mjs';
import {Validator} from '../../stage6-handlers-local/src/validation.mjs';
import {Idempotency} from '../../stage6-handlers-local/src/idempotency.mjs';
import {exportAudit,verifyAudit} from '../../stage6-handlers-local/src/audit.mjs';
import {migrateOperations} from '../src/storage.mjs';
import {compose} from '../src/compose.mjs';
let db,key;const schemas=[];
const policy={fields:{work:{title:{type:'text',maxBytes:2048}}},intendedLicenses:['CC0-1.0'],sources:[{type:'reference',value:'Explicitly permitted Goldberg authored synthetic test fixture',license:'CC0-1.0',kind:'work',fields:['title'],intendedLicenses:['CC0-1.0']}]};
const payload=(id=randomUUID(),title='Synthetic permitted title')=>({schemaVersion:1,target:{kind:'work',catalogueUuid:id},changeKind:'new_metadata',intendedLicense:'CC0-1.0',contributorNote:'PRIVATE moderation note',fields:[{field:'title',newValue:title,evidence:[{type:'reference',value:policy.sources[0].value,license:'CC0-1.0'}]}]});
async function connect(){const c=localClient();await c.connect();return c;}
before(async()=>{db=await connect();key=await makeKey('operations-synthetic-key');});
after(async()=>{if(db){await db.query('ROLLBACK');for(const s of schemas)await db.query(`DROP SCHEMA ${identifier(s)} CASCADE`);await db.end();}});
async function user(v,subject,roles){const ch=await v.sessions.challenge(),u=await v.sessions.login({credential:await credential(key,v.clock,{subject,nonce:ch.nonce}),challenge:ch.challenge});for(const role of roles)await db.query(`INSERT INTO ${v.q}.role_grants(id,principal_id,role,granted_by,granted_at) VALUES($1,$2,$3,$2,$4)`,[randomUUID(),u.principalId,role,new Date(v.clock.now())]);return u;}
async function venue(limits={daily:100,pending:100,ipLimit:200}){
 const schema='proto_'+randomUUID().replaceAll('-',''),q=identifier(schema);schemas.push(schema);await migrateOperations(db,schema);
 const clock=new FakeClock(),verifier=new IdentityVerifier({issuer,audience,clock:clock.now,fetchJwks:new FakeJwks([key.jwk]).fetch});
 const sessions=new SessionService({connect,schema,verifier,clock:clock.now,accessTtlMs:172800000,refreshTtlMs:259200000,challengeTtlMs:60000});
 const authorization=new Authorization({sessions,clock:clock.now}),idempotency=new Idempotency({schema,clock:clock.now});let fail;
 const onBoundary=async(n,c)=>{if(n===fail)throw Error('synthetic fault');if(fail==='deferred'&&n==='feed')await c.query(`INSERT INTO ${q}.published_entities(id,kind,revision,status) VALUES($1,'work',1,'published')`,[randomUUID()]);};
 const composed=compose({connect,schema,authorization,idempotency,validator:new Validator(policy),clock:clock.now,cursorKey:randomBytes(32),ipKey:randomBytes(32),limits,onBoundary});
 const v={schema,q,clock,sessions,authorization,idempotency,...composed,setFail:n=>{fail=n;}};
 await db.query(`UPDATE ${q}.intake_control SET enabled=true`);
 v.author=await user(v,'author',['contributor']);v.other=await user(v,'other',['contributor']);v.mod=await user(v,'mod',['moderator']);v.admin=await user(v,'admin',['administrator']);v.admin2=await user(v,'admin2',['administrator']);return v;
}
const request=(extra={})=>({schemaVersion:1,idempotencyKey:randomUUID(),...extra});
const call=(v,u,operation,body={schemaVersion:1},ip='198.51.100.1')=>v.api.invoke({operation,accessToken:u?.accessToken,body},{remoteAddress:ip});
async function create(v,id=randomUUID(),expectedRevision=0,u=v.author){const r=await call(v,u,'submission.create',request({payload:payload(id),expectedRevision}));assert.equal(r.status,201,JSON.stringify(r));return r.body;}
async function publish(v,id=randomUUID(),expectedRevision=0){const s=await create(v,id,expectedRevision);assert.equal((await call(v,v.mod,'moderation.open',request({id:s.id,expectedRevision,expectedSubmissionRevision:1}))).status,200);const r=await call(v,v.mod,'moderation.decide',request({id:s.id,expectedRevision,expectedSubmissionRevision:1,decision:'approve'}));assert.equal(r.status,201,JSON.stringify(r));return r.body;}
async function adminBody(v,id,extra={}){const e=(await db.query(`SELECT revision::text FROM ${v.q}.published_entities WHERE id=$1`,[id])).rows[0],f=(await db.query(`SELECT seq::text FROM ${v.q}.change_feed WHERE entity_id=$1 ORDER BY change_feed.seq DESC LIMIT 1`,[id])).rows[0];return request({id,expectedRevision:e.revision,expectedEventSeq:f.seq,reason:'PRIVATE synthetic moderation reason',...extra});}
async function snapshot(v){const out={};for(const t of ['published_entities','published_revisions','change_feed','audit_events','audit_head','idempotency_results','submissions','submission_revisions','role_grants','principals','abuse_limits'])out[t]=(await db.query(`SELECT row_to_json(t)::text AS r FROM ${v.q}.${t} t ORDER BY row_to_json(t)::text`)).rows;return out;}
async function chain(v){assert.equal(verifyAudit(await exportAudit(db,v.schema)),true);}

test('004 migration repeat application is checksum verified and defaults intake off',async()=>{const v=await venue();await migrateOperations(db,v.schema);await db.query(`UPDATE ${v.q}.intake_control SET enabled=false`);assert.equal((await call(v,v.author,'submission.create',request({payload:payload(),expectedRevision:0}))).status,503);});
test('public only approved current metadata, no private note, identity, audit or pending record',async()=>{
 const v=await venue();await create(v);const p=await publish(v);for(const op of ['catalogue.overlay','catalogue.changes']){const r=await call(v,null,op);assert.equal(r.status,200);assert.equal(r.body.items.length,1);const json=JSON.stringify(r);for(const secret of ['PRIVATE',v.author.principalId,v.mod.principalId,'actor_id','audit_seq','contributorNote','source_evidence','canonical_content'])assert.ok(!json.includes(secret));assert.equal(r.body.items[0].catalogueUuid,p.entityId);}
});
test('quarantine immediate exclusion even with older page cursor; immutable disclosure-safe tombstone',async()=>{
 const v=await venue();for(let i=0;i<21;i++)await publish(v);const page=await call(v,null,'catalogue.overlay');assert.equal(page.body.items.length,20);assert.ok(page.body.nextCursor);
 const remaining=(await db.query(`SELECT id FROM ${v.q}.published_entities WHERE id<>ALL($1::uuid[])`,[page.body.items.map(i=>i.catalogueUuid)])).rows[0].id;
 const body=await adminBody(v,remaining);assert.equal((await call(v,v.admin,'admin.quarantine',body)).status,200);const next=await call(v,null,'catalogue.overlay',{schemaVersion:1,cursor:page.body.nextCursor});assert.equal(next.body.items.length,0);
 const feed=await call(v,null,'catalogue.changes');const page2=await call(v,null,'catalogue.changes',{schemaVersion:1,cursor:feed.body.nextCursor});assert.equal(page2.body.items.at(-1).event,'quarantine');assert.ok(!JSON.stringify(page2).includes('PRIVATE'));await chain(v);
});
test('supersession, rollback, later publication use immutable monotonic revision allocation',async()=>{
 const v=await venue(),p=await publish(v);await publish(v,p.entityId,1);const before=(await db.query(`SELECT * FROM ${v.q}.published_revisions ORDER BY revision`)).rows;
 const b=await adminBody(v,p.entityId,{targetRevision:'1'});assert.equal((await call(v,v.admin,'admin.rollback',b)).status,200);assert.equal((await call(v,null,'catalogue.overlay')).body.items[0].revision,'1');
 assert.deepEqual((await db.query(`SELECT * FROM ${v.q}.published_revisions ORDER BY revision`)).rows,before);assert.equal((await publish(v,p.entityId,1)).revision,3);
 const f=await call(v,null,'catalogue.changes');assert.deepEqual(f.body.items.map(i=>i.event),['publication','supersession','rollback','supersession']);await chain(v);
});
test('numeric feed order beyond nine events, equal timestamps, multipage highwater and concurrent writes',async()=>{
 const v=await venue();await db.query(`ALTER TABLE ${v.q}.change_feed ALTER COLUMN created_at SET DEFAULT '2030-01-01T00:00:00Z'::timestamptz`);for(let i=0;i<23;i++)await publish(v);const first=await call(v,null,'catalogue.changes');assert.equal(first.body.items.length,20);
 await Promise.all([publish(v),publish(v)]);const second=await call(v,null,'catalogue.changes',{schemaVersion:1,cursor:first.body.nextCursor});assert.equal(second.body.items.length,3);
 const seq=[...first.body.items,...second.body.items].map(i=>BigInt(i.sequence));assert.deepEqual(seq,[...seq].sort((a,b)=>a<b?-1:1));assert.equal(new Set(seq).size,23);
 const polled=await call(v,null,'catalogue.changes',{schemaVersion:1,cursor:second.body.pollCursor});assert.equal(polled.body.items.length,2);const empty=await call(v,null,'catalogue.changes',{schemaVersion:1,cursor:polled.body.pollCursor});assert.equal(empty.body.items.length,0);
});
test('cursor tampering, cross kind, invalid kind, page size and privilege fields fail 400',async()=>{
 const v=await venue();for(const body of [{schemaVersion:1,kind:'practice'},{schemaVersion:1,cursor:'tampered'},{schemaVersion:1,limit:21},{schemaVersion:1,actor:randomUUID()}])assert.equal((await call(v,null,'catalogue.overlay',body)).status,400);
});
for(const op of ['admin.quarantine','admin.rollback'])test(op+' live default-deny, reason and exact expected state',async()=>{
 const v=await venue(),p=await publish(v);await publish(v,p.entityId,1);const b=await adminBody(v,p.entityId,op.endsWith('rollback')?{targetRevision:'1'}:{});
 for(const u of [v.author,v.mod])assert.equal((await call(v,u,op,b)).status,403);assert.equal((await call(v,null,op,b)).status,401);
 for(const bad of [{...b,reason:''},{...b,actor:v.admin.principalId},{...b,expectedRevision:2}])assert.equal((await call(v,v.admin,op,bad)).status,400);
 assert.equal((await call(v,v.admin,op,{...b,expectedRevision:'1'})).status,409);assert.equal((await call(v,v.admin,op,{...b,expectedEventSeq:'1'})).status,409);
});
for(const op of ['admin.quarantine','admin.rollback'])test(op+' replay, mismatch and concurrent distinct-key decisions',async()=>{
 const v=await venue(),p=await publish(v);await publish(v,p.entityId,1);const b=await adminBody(v,p.entityId,op.endsWith('rollback')?{targetRevision:'1'}:{});
 const pair=await Promise.all([call(v,v.admin,op,b),call(v,v.admin,op,b)]);assert.deepEqual(pair.map(r=>r.status),[200,200]);assert.equal(pair[0].body.requestId,pair[1].body.requestId);
 assert.equal((await call(v,v.admin,op,{...b,reason:'different'})).status,422);await chain(v);
 const w=await venue(),p2=await publish(w);await publish(w,p2.entityId,1);const b2=await adminBody(w,p2.entityId,op.endsWith('rollback')?{targetRevision:'1'}:{});
 const raced=await Promise.all([call(w,w.admin,op,b2),call(w,w.admin2,op,{...b2,idempotencyKey:randomUUID()})]);assert.deepEqual(raced.map(r=>r.status).sort(),[200,409]);await chain(w);
});
for(const op of ['admin.quarantine','admin.rollback'])for(const boundary of ['locked','projection','audit','feed','deferred'])test(`${op} T17 atomic rollback at ${boundary}`,async()=>{
 const v=await venue(),p=await publish(v);await publish(v,p.entityId,1);const b=await adminBody(v,p.entityId,op.endsWith('rollback')?{targetRevision:'1'}:{}),prior=await snapshot(v);v.setFail(boundary);
 assert.equal((await call(v,v.admin,op,b)).status,503);assert.deepEqual(await snapshot(v),prior);v.setFail(null);assert.equal((await call(v,v.admin,op,b)).status,200);await chain(v);
});
test('role/suspension integration reuses live Auth, denies escalation, self grant and revoked replay',async()=>{
 const v=await venue(),grant=request({principalId:v.other.principalId,role:'moderator',revoke:false});assert.equal((await call(v,v.mod,'admin.roles',grant)).status,403);assert.equal((await call(v,v.admin,'admin.roles',{...grant,principalId:v.admin.principalId})).status,403);
 assert.equal((await call(v,v.admin,'admin.roles',grant)).status,200);const snap=await snapshot(v);assert.equal((await call(v,v.admin,'admin.roles',grant)).status,200);assert.deepEqual(await snapshot(v),snap);
 assert.equal((await call(v,v.admin,'admin.roles',request({principalId:v.other.principalId,role:'moderator',revoke:true}))).status,200);assert.equal((await call(v,v.other,'moderation.queue')).status,403);
 const suspend=request({principalId:v.other.principalId,suspended:true});assert.equal((await call(v,v.admin,'admin.suspend',suspend)).status,200);assert.equal((await call(v,v.other,'submission.mine')).status,403);
 assert.equal((await call(v,v.admin,'admin.suspend',request({principalId:v.other.principalId,suspended:false}))).status,200);assert.equal((await call(v,v.other,'submission.mine')).status,200);
 await call(v,v.admin2,'admin.roles',request({principalId:v.admin.principalId,role:'administrator',revoke:true}));assert.equal((await call(v,v.admin,'admin.roles',grant)).status,403);await chain(v);
});
test('principal operation fault rolls role and audit/idempotency back together',async()=>{const v=await venue(),prior=await snapshot(v);v.setFail('principal');assert.equal((await call(v,v.admin,'admin.roles',request({principalId:v.other.principalId,role:'moderator',revoke:false}))).status,503);assert.deepEqual(await snapshot(v),prior);});
test('admin audit paginates numerically with hash head and has no mutation endpoint',async()=>{
 const v=await venue();for(let i=0;i<7;i++)await publish(v);for(const u of [null,v.author,v.mod])assert.equal((await call(v,u,'admin.audit')).status,u?403:401);
 let cursor,events=[],head;do{const r=await call(v,v.admin,'admin.audit',{schemaVersion:1,...(cursor?{cursor}:{})});assert.equal(r.status,200);head=r.body.head;events.push(...r.body.items);cursor=r.body.nextCursor;}while(cursor);
 assert.equal(verifyAudit({version:1,events,head}),true);assert.equal((await call(v,v.admin,'admin.audit.edit',request())).status,400);
 for(const table of ['published_revisions','audit_events','change_feed'])await assert.rejects(db.query(`DELETE FROM ${v.q}.${table}`),e=>e.code==='55000');
});
test('daily limit atomic last slot, duplicate replay, mismatch and pending quota',async()=>{
 const v=await venue({daily:1,pending:10,ipLimit:20}),b=request({payload:payload(),expectedRevision:0});const pair=await Promise.all([call(v,v.author,'submission.create',b),call(v,v.author,'submission.create',b)]);assert.deepEqual(pair.map(r=>r.status),[201,201]);
 const denied=await call(v,v.author,'submission.create',request({payload:payload(),expectedRevision:0}));assert.equal(denied.status,429);assert.ok(Number(denied.headers['Retry-After'])>0);
 assert.equal((await call(v,v.author,'submission.create',{...b,expectedRevision:1})).status,422);
 const w=await venue({daily:1,pending:10,ipLimit:20}),r=await Promise.all([createAttempt(w),createAttempt(w)]);assert.deepEqual(r.map(x=>x.status).sort(),[201,429]);
 const p=await venue({daily:10,pending:1,ipLimit:20});await create(p);assert.equal((await createAttempt(p)).status,429);await chain(v);
});
const createAttempt=(v,u=v.author,ip='198.51.100.1')=>call(v,u,'submission.create',request({payload:payload(),expectedRevision:0}),ip);
test('coarse IP last slot across different accounts, hashed storage and no raw diagnostics',async()=>{
 const v=await venue({daily:5,pending:10,ipLimit:1});const r=await Promise.all([createAttempt(v,v.author,'198.51.100.1'),createAttempt(v,v.other,'198.51.100.254')]);assert.deepEqual(r.map(x=>x.status).sort(),[201,429]);
 const rows=(await db.query(`SELECT * FROM ${v.q}.abuse_limits`)).rows;assert.equal(rows.length,2);assert.match(rows.find(r=>r.scope==='coarse_ip').scope_key,/^[0-9a-f]{64}$/);assert.ok(!JSON.stringify(rows).includes('198.51.100.'));assert.equal((await createAttempt(v,v.other,'198.51.101.1')).status,201);
});
for(const boundary of ['principal','coarse_ip'])test('quota rollback on '+boundary+' counter fault',async()=>{const v=await venue(),b=request({payload:payload(),expectedRevision:0}),prior=await snapshot(v);v.setFail(boundary);assert.equal((await call(v,v.author,'submission.create',b)).status,503);assert.deepEqual(await snapshot(v),prior);v.setFail(null);assert.equal((await call(v,v.author,'submission.create',b)).status,201);});
test('kill switch truthful 503, committed replay and public/private reads stay available',async()=>{
 const v=await venue(),b=request({payload:payload(),expectedRevision:0});assert.equal((await call(v,v.author,'submission.create',b)).status,201);await db.query(`UPDATE ${v.q}.intake_control SET enabled=false`);const prior=await snapshot(v);assert.equal((await createAttempt(v)).status,503);assert.equal((await call(v,v.author,'submission.create',b)).status,201);assert.equal((await call(v,null,'catalogue.overlay')).status,200);assert.equal((await call(v,v.author,'submission.mine')).status,200);assert.deepEqual(await snapshot(v),prior);
});
test('quota table unavailable is 503 without work loss; recovery permits same key',async()=>{
 const v=await venue(),b=request({payload:payload(),expectedRevision:0}),prior=await snapshot(v);await db.query(`ALTER TABLE ${v.q}.abuse_limits RENAME TO unavailable_limits`);assert.equal((await call(v,v.author,'submission.create',b)).status,503);await db.query(`ALTER TABLE ${v.q}.unavailable_limits RENAME TO abuse_limits`);assert.deepEqual(await snapshot(v),prior);assert.equal((await call(v,v.author,'submission.create',b)).status,201);
});

test('BIGINT feed sequence boundaries remain lossless and numerically ordered',async()=>{
 const v=await venue();await db.query(`ALTER TABLE ${v.q}.change_feed ALTER COLUMN seq RESTART WITH 9223372036854775806`);
 await publish(v);await publish(v);const feed=await call(v,null,'catalogue.changes');assert.deepEqual(feed.body.items.map(i=>i.sequence),['9223372036854775806','9223372036854775807']);
});
test('synthetic daily reset and coarse retention do not erase pending/audit history',async()=>{
 const v=await venue({daily:1,pending:10,ipLimit:1,ipWindowMs:1000,retentionMs:1000});await create(v);assert.equal((await createAttempt(v)).status,429);
 v.clock.advance(86400000);assert.equal((await createAttempt(v)).status,201);
 assert.equal((await db.query(`SELECT count(*)::int n FROM ${v.q}.submissions`)).rows[0].n,2);
 assert.equal((await db.query(`SELECT count(*)::int n FROM ${v.q}.abuse_limits WHERE scope='coarse_ip'`)).rows[0].n,1);await chain(v);
});
test('quota boundary exactly resets the IP window without recounting prior idempotency replay',async()=>{
 const v=await venue({daily:5,pending:10,ipLimit:1,ipWindowMs:1000,retentionMs:1000}),b=request({payload:payload(),expectedRevision:0});
 assert.equal((await call(v,v.author,'submission.create',b)).status,201);v.clock.advance(999);const deny=await createAttempt(v);assert.equal(deny.status,429);assert.equal(deny.headers['Retry-After'],'1');
 v.clock.advance(1);assert.equal((await call(v,v.author,'submission.create',b)).status,201);assert.equal((await createAttempt(v)).status,201);
});
test('idempotency insert failure rolls quota, submission and audit back together',async()=>{
 const v=await venue(),b=request({payload:payload(),expectedRevision:0}),prior=await snapshot(v);
 await db.query(`CREATE FUNCTION ${v.q}.synthetic_fail_result() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'synthetic fault'; END $$; CREATE TRIGGER synthetic_fail_result BEFORE INSERT ON ${v.q}.idempotency_results FOR EACH ROW EXECUTE FUNCTION ${v.q}.synthetic_fail_result()`);
 assert.equal((await call(v,v.author,'submission.create',b)).status,503);assert.deepEqual(await snapshot(v),prior);
 await db.query(`DROP TRIGGER synthetic_fail_result ON ${v.q}.idempotency_results`);assert.equal((await call(v,v.author,'submission.create',b)).status,201);
});
