import {test,before,after} from 'node:test';
import assert from 'node:assert/strict';
import {randomUUID} from 'node:crypto';
import {localClient,migrate,identifier,migrationFiles,safeInt8} from '../src/database.mjs';
import {appendAudit,publishSynthetic,transition,insertResult,Conflict,Mismatch} from '../src/transactions.mjs';
import {exportAudit,verifyAudit,digest} from '../src/audit.mjs';
const clients=[],schemas=[];
async function connect(){const c=localClient();await c.connect();clients.push(c);return c;}
async function venue(through=2){const c=await connect(),s='proto_'+randomUUID().replaceAll('-','');schemas.push(s);await migrate(c,s,{through});return {c,s,q:identifier(s)};}
const payload={schemaVersion:1,target:{kind:'work'},changeKind:'correction',fields:[{field:'title',newValue:'Synthetic work',evidence:[{type:'source_url',value:'https://example.invalid/synthetic',license:'CC0'}]}],intendedLicense:'CC0'};
async function seed(v){
  const {c,q}=v,author=randomUUID(),actor=randomUUID(),operator=randomUUID();
  for(const [id,sub] of [[author,'synthetic-author'],[actor,'synthetic-moderator'],[operator,'synthetic-operator']])
    await c.query(`INSERT INTO ${q}.principals(id,iss,sub) VALUES($1,'https://synthetic.invalid',$2)`,[id,sub]);
  await c.query(`INSERT INTO ${q}.role_grants(id,principal_id,role,granted_by) VALUES($1,$2,'moderator',$3)`,[randomUUID(),actor,operator]);
  return {author,actor,operator};
}
async function submission(v,ids,{entity=randomUUID(),reviewed=true}={}){
  const {c,q,s}=v,id=randomUUID();await c.query('BEGIN');
  try {
    await c.query(`INSERT INTO ${q}.submissions(id,author_id,target_kind,catalogue_uuid,expected_revision,current_revision) VALUES($1,$2,'work',$3,0,1)`,[id,ids.author,entity]);
    await c.query(`INSERT INTO ${q}.submission_revisions(submission_id,revision,payload,prev_hash) VALUES($1,1,$2,repeat('0',64))`,[id,payload]);
    if(reviewed) await transition(c,s,{submission:id,revision:1,actor:ids.actor,from:'submitted',to:'in_review'});
    await c.query('COMMIT');return {id,entity};
  }catch(e){await c.query('ROLLBACK');throw e;}
}
function decision(ids,sub,extra={}){return {submission:sub.id,actor:ids.actor,key:randomUUID(),hash:digest('synthetic request'),expectedSubmissionRevision:1,expectedEntityRevision:0,...extra};}
async function sqlstate(c,sql,params,code){await assert.rejects(c.query(sql,params),e=>e.code===code);}
async function counts(v){const names=['published_entities','published_revisions','review_events','audit_events','change_feed','idempotency_results'];const out={};for(const t of names)out[t]=(await v.c.query(`SELECT count(*)::text AS n FROM ${v.q}.${t}`)).rows[0].n;out.head=(await v.c.query(`SELECT last_seq::text,last_hash FROM ${v.q}.audit_head`)).rows;out.subs=(await v.c.query(`SELECT id,status,current_revision::text FROM ${v.q}.submissions ORDER BY id`)).rows;return out;}
before(async()=>{const c=await connect();console.log('PostgreSQL version:',(await c.query('SELECT version() AS v')).rows[0].v);});
after(async()=>{for(const c of clients)await c.query('ROLLBACK').catch(()=>{});const c=clients[0];for(const s of schemas)await c.query(`DROP SCHEMA IF EXISTS ${identifier(s)} CASCADE`);for(const x of clients)await x.end();});

test('empty PostgreSQL 17 migrations create all entities and a pre-existing genesis head',async()=>{
 const v=await venue();const tables=(await v.c.query('SELECT tablename FROM pg_tables WHERE schemaname=$1',[v.s])).rows.map(r=>r.tablename);
 for(const name of ['principals','role_grants','sessions','submissions','submission_revisions','review_events','published_entities','published_revisions','identity_mappings','audit_events','audit_head','abuse_limits','idempotency_results','change_feed','schema_migrations']) assert.ok(tables.includes(name),name);
 assert.deepEqual((await v.c.query(`SELECT last_seq::text,last_hash FROM ${v.q}.audit_head`)).rows,[{last_seq:'0',last_hash:'0'.repeat(64)}]);
});
test('seeded v1 to v2 migration preserves principals, roles and quota accounting',async()=>{
 const v=await venue(1),ids=await seed(v);
 await v.c.query(`INSERT INTO ${v.q}.abuse_limits VALUES('principal',$1::uuid::text,$1::uuid,now(),now()+interval '1 day',3)`,[ids.author]);
 await migrate(v.c,v.s);assert.equal((await v.c.query(`SELECT count(*)::int AS n FROM ${v.q}.principals`)).rows[0].n,3);
 assert.equal((await v.c.query(`SELECT count::text FROM ${v.q}.abuse_limits`)).rows[0].count,'3');
 assert.equal((await v.c.query(`SELECT count(*)::int AS n FROM ${v.q}.role_grants`)).rows[0].n,1);
 await submission(v,ids);
});
test('repeat and concurrent migrations are idempotent and checksum drift fails closed',async()=>{
 const v=await venue(),other=await connect(),ids=await seed(v);await submission(v,ids);const before=await counts(v);
 await Promise.all([migrate(v.c,v.s),migrate(other,v.s)]);assert.deepEqual(await counts(v),before);
 const files=await migrationFiles();files[0].sql+='\n-- drift';await assert.rejects(migrate(v.c,v.s,{files}),/checksum/);
 assert.deepEqual(await counts(v),before);
});
test('failed migration rolls back DDL and its ledger entry, preserving seeded v1',async()=>{
 const v=await venue(1);await seed(v);const files=await migrationFiles();files[1].sql+='\nSELECT missing_migration_function();';
 await assert.rejects(migrate(v.c,v.s,{files}),e=>e.code==='42883');
 assert.equal((await v.c.query(`SELECT count(*)::int AS n FROM ${v.q}.schema_migrations`)).rows[0].n,1);
 assert.equal((await v.c.query(`SELECT count(*)::int AS n FROM ${v.q}.principals`)).rows[0].n,3);
 await migrate(v.c,v.s);assert.equal((await v.c.query(`SELECT count(*)::int AS n FROM ${v.q}.schema_migrations`)).rows[0].n,2);
});
test('principal, external identity and active-role uniqueness; explicit soft revocation history',async()=>{
 const v=await venue(),ids=await seed(v);await sqlstate(v.c,`INSERT INTO ${v.q}.principals(id,iss,sub) VALUES($1,'https://synthetic.invalid','synthetic-author')`,[randomUUID()],'23505');
 const mapping=randomUUID();await v.c.query(`INSERT INTO ${v.q}.identity_mappings VALUES($1,'synthetic','work','id-1',NULL,'needs_review')`,[mapping]);
 await sqlstate(v.c,`INSERT INTO ${v.q}.identity_mappings VALUES($1,'synthetic','work','id-1',NULL,'needs_review')`,[randomUUID()],'23505');
 await sqlstate(v.c,`INSERT INTO ${v.q}.role_grants(id,principal_id,role,granted_by) VALUES($1,$2,'moderator',$3)`,[randomUUID(),ids.actor,ids.operator],'23505');
 await v.c.query(`UPDATE ${v.q}.role_grants SET revoked_at=clock_timestamp() WHERE principal_id=$1`,[ids.actor]);
 await v.c.query(`INSERT INTO ${v.q}.role_grants(id,principal_id,role,granted_by) VALUES($1,$2,'moderator',$3)`,[randomUUID(),ids.actor,ids.operator]);
 assert.equal((await v.c.query(`SELECT count(*)::int AS n FROM ${v.q}.role_grants`)).rows[0].n,2);
});
test('orphan FKs for identity, actor, mapping, session, revision, review, audit and publication reject',async()=>{
 const v=await venue(),ids=await seed(v),missing=randomUUID();
 await sqlstate(v.c,`INSERT INTO ${v.q}.role_grants(id,principal_id,role,granted_by) VALUES($1,$2,'moderator',$3)`,[randomUUID(),missing,ids.operator],'23503');
 await sqlstate(v.c,`INSERT INTO ${v.q}.sessions(id,principal_id,family_id,token_hash,issued_at,expires_at) VALUES($1,$2,$3,$4,now(),now()+interval '1 hour')`,[randomUUID(),missing,randomUUID(),digest('session')],'23503');
 await sqlstate(v.c,`INSERT INTO ${v.q}.submissions(id,author_id,target_kind,mapping_id,expected_revision,current_revision) VALUES($1,$2,'work',$3,0,1)`,[randomUUID(),ids.author,missing],'23503');
 await sqlstate(v.c,`INSERT INTO ${v.q}.submission_revisions(submission_id,revision,payload,prev_hash) VALUES($1,1,$2,repeat('0',64))`,[missing,payload],'23503');
 const sub=await submission(v,ids);await sqlstate(v.c,`INSERT INTO ${v.q}.review_events(id,submission_id,submission_revision,actor_id,from_status,to_status,audit_seq) VALUES($1,$2,1,$3,'in_review','approved',999)`,[randomUUID(),sub.id,ids.actor],'23503');
 await assert.rejects(appendAudit(v.c,v.s,{actor:ids.actor,target:missing}),e=>e.code==='23503');
 await sqlstate(v.c,`INSERT INTO ${v.q}.change_feed(entity_id,revision,status,audit_seq) VALUES($1,1,'published',1)`,[missing],'23503');
 await sqlstate(v.c,`DELETE FROM ${v.q}.principals WHERE id=$1`,[ids.author],'23503');
});
test('deferred current-revision FK permits atomic creation but rejects dangling pointers at COMMIT',async()=>{
 const v=await venue(),ids=await seed(v);await v.c.query('BEGIN');
 await v.c.query(`INSERT INTO ${v.q}.submissions(id,author_id,target_kind,catalogue_uuid,expected_revision,current_revision) VALUES($1,$2,'work',$3,0,1)`,[randomUUID(),ids.author,randomUUID()]);
 await sqlstate(v.c,'COMMIT',[],'23503');await v.c.query('ROLLBACK');assert.equal((await v.c.query(`SELECT count(*)::int AS n FROM ${v.q}.submissions`)).rows[0].n,0);
 await submission(v,ids);
});
test('session rotation family/ownership, expiry and quota checks are physical constraints',async()=>{
 const v=await venue(),ids=await seed(v),id=randomUUID(),family=randomUUID();
 await v.c.query(`INSERT INTO ${v.q}.sessions(id,principal_id,family_id,token_hash,issued_at,expires_at) VALUES($1,$2,$3,$4,now(),now()+interval '1 hour')`,[id,ids.author,family,digest('one')]);
 await sqlstate(v.c,`INSERT INTO ${v.q}.sessions(id,principal_id,family_id,predecessor_id,token_hash,issued_at,expires_at) VALUES($1,$2,$3,$4,$5,now(),now()+interval '1 hour')`,[randomUUID(),ids.actor,family,id,digest('two')],'23503');
 await sqlstate(v.c,`INSERT INTO ${v.q}.sessions(id,principal_id,family_id,token_hash,issued_at,expires_at) VALUES($1,$2,$3,$4,now(),now())`,[randomUUID(),ids.author,family,digest('three')],'23514');
 await sqlstate(v.c,`INSERT INTO ${v.q}.abuse_limits VALUES('principal',$1::uuid::text,$1::uuid,now(),now()+interval '1 hour',-1)`,[ids.author],'23514');
});
test('revision chain requires contiguous versions and matching previous/content hashes',async()=>{
 const v=await venue(),ids=await seed(v),sub=await submission(v,ids);
 const [{this_hash:hash}]=(await v.c.query(`SELECT this_hash FROM ${v.q}.submission_revisions`)).rows;
 await sqlstate(v.c,`INSERT INTO ${v.q}.submission_revisions(submission_id,revision,payload,prev_hash) VALUES($1,3,$2,$3)`,[sub.id,payload,hash],'23514');
 await sqlstate(v.c,`INSERT INTO ${v.q}.submission_revisions(submission_id,revision,payload,prev_hash) VALUES($1,2,$2,$3)`,[sub.id,payload,'f'.repeat(64)],'23514');
 await sqlstate(v.c,`INSERT INTO ${v.q}.submission_revisions(submission_id,revision,payload,prev_hash,this_hash) VALUES($1,2,$2,$3,$4)`,[sub.id,payload,hash,'f'.repeat(64)],'23514');
 const snapshot=await v.c.query(`SELECT encode(sha256(convert_to(jsonb_build_array(submission_id::text,revision::text,payload,prev_hash)::text,'UTF8')),'hex')=this_hash AS valid FROM ${v.q}.submission_revisions`);assert.equal(snapshot.rows[0].valid,true);
});
test('only documented server transitions; changes_requested creates a new immutable revision',async()=>{
 const v=await venue(),ids=await seed(v),sub=await submission(v,ids);
 for(const state of ['draft','returned','published'])await sqlstate(v.c,`UPDATE ${v.q}.submissions SET status=$2 WHERE id=$1`,[sub.id,state],'23514');
 await v.c.query('BEGIN');await transition(v.c,v.s,{submission:sub.id,revision:1,actor:ids.actor,from:'in_review',to:'changes_requested',reason:'synthetic correction'});await v.c.query('COMMIT');
 await v.c.query('BEGIN');const hash=(await v.c.query(`SELECT this_hash FROM ${v.q}.submission_revisions WHERE submission_id=$1`,[sub.id])).rows[0].this_hash;
 await v.c.query(`INSERT INTO ${v.q}.submission_revisions(submission_id,revision,payload,prev_hash) VALUES($1,2,$2,$3)`,[sub.id,payload,hash]);
 await transition(v.c,v.s,{submission:sub.id,revision:2,actor:ids.author,from:'changes_requested',to:'in_review'});await v.c.query('COMMIT');
 assert.equal((await v.c.query(`SELECT count(*)::int AS n FROM ${v.q}.submission_revisions`)).rows[0].n,2);
});
test('state transition without matching audit/review cannot commit',async()=>{
 const v=await venue(),ids=await seed(v),sub=await submission(v,ids,{reviewed:false});
 await sqlstate(v.c,`UPDATE ${v.q}.submissions SET status='in_review' WHERE id=$1`,[sub.id],'23514');
 assert.equal((await v.c.query(`SELECT status FROM ${v.q}.submissions`)).rows[0].status,'submitted');
});
test('immutable revision, published revision, review, audit and feed reject UPDATE/DELETE/TRUNCATE',async()=>{
 const v=await venue(),ids=await seed(v),sub=await submission(v,ids);await publishSynthetic(v.c,v.s,decision(ids,sub));
 for(const table of ['submission_revisions','published_revisions','review_events','audit_events','change_feed']){
  const column=table==='review_events'||table==='audit_events'||table==='change_feed'?'reason':table==='submission_revisions'?'this_hash':'this_hash';
  await sqlstate(v.c,`UPDATE ${v.q}.${table} SET ${column}=${column}`,[],'55000');
  await sqlstate(v.c,`DELETE FROM ${v.q}.${table}`,[],'55000');
  await sqlstate(v.c,`TRUNCATE ${v.q}.${table} CASCADE`,[],'55000');
 }
 await sqlstate(v.c,`TRUNCATE ${v.q}.audit_events CASCADE`,[],'55000');
 await sqlstate(v.c,`DELETE FROM ${v.q}.audit_head`,[],'55000');
 await sqlstate(v.c,`UPDATE ${v.q}.audit_head SET last_seq=last_seq+1`,[],'23514');
});
test('publication snapshot, target kind and actor-author checks reject invalid publication',async()=>{
 const v=await venue(),ids=await seed(v),sub=await submission(v,ids);await assert.rejects(publishSynthetic(v.c,v.s,decision(ids,sub,{actor:ids.author})),/authorized/);
 await v.c.query('BEGIN');await v.c.query(`INSERT INTO ${v.q}.published_entities VALUES($1,'work',1,'published',NULL)`,[sub.entity]);
 await sqlstate(v.c,`INSERT INTO ${v.q}.published_revisions(entity_id,revision,submission_id,submission_revision,actor_id,payload,source_evidence) VALUES($1,1,$2,1,$3,$4,'[]')`,[sub.entity,sub.id,ids.author,payload],'23514');await v.c.query('ROLLBACK');
 await v.c.query('BEGIN');await v.c.query(`INSERT INTO ${v.q}.published_entities VALUES($1,'work',1,'published',NULL)`,[sub.entity]);
 await sqlstate(v.c,`INSERT INTO ${v.q}.published_revisions(entity_id,revision,submission_id,submission_revision,actor_id,payload,source_evidence) VALUES($1,1,$2,1,$3,'{}','[]')`,[sub.entity,sub.id,ids.actor],'23514');await v.c.query('ROLLBACK');
});
for(const boundary of ['locked','publication','approved','audit','feed','idempotency'])test(`atomic publication rollback after ${boundary} leaves all tables/head/key unchanged`,async()=>{
 const v=await venue(),ids=await seed(v),sub=await submission(v,ids),args=decision(ids,sub),before=await counts(v);
 await assert.rejects(publishSynthetic(v.c,v.s,{...args,failAt:boundary}),new RegExp(`injected:${boundary}`));assert.deepEqual(await counts(v),before);
 const result=await publishSynthetic(v.c,v.s,args);assert.equal(result.status,201);assert.equal(verifyAudit(await exportAudit(v.c,v.s)),true);
});
test('stored response replay, mismatch 422, principal isolation and >=24-hour expiry/reuse',async()=>{
 const v=await venue(),ids=await seed(v),sub=await submission(v,ids),now=new Date('2030-01-01T00:00:00Z'),args=decision(ids,sub,{now});
 const first=await publishSynthetic(v.c,v.s,args),before=await counts(v);assert.equal((await publishSynthetic(v.c,v.s,args)).replayed,true);assert.deepEqual(await counts(v),before);
 await assert.rejects(publishSynthetic(v.c,v.s,{...args,hash:digest('different')}),e=>e instanceof Mismatch && e.status===422);
 await v.c.query('BEGIN');await insertResult(v.c,v.s,{principal:ids.author,key:args.key,hash:args.hash,body:{synthetic:true},now});await v.c.query('COMMIT');
 await sqlstate(v.c,`INSERT INTO ${v.q}.idempotency_results VALUES($1,$2,$3,201,'{}',$4,$4::timestamptz+interval '23 hours')`,[ids.author,randomUUID(),args.hash,now],'23514');
 const nextSub=await submission(v,ids);const replay=await publishSynthetic(v.c,v.s,decision(ids,nextSub,{key:args.key,now:new Date('2030-01-01T23:59:59Z')}));assert.deepEqual(replay.body,first.body);
 const next=await publishSynthetic(v.c,v.s,decision(ids,nextSub,{key:args.key,now:new Date('2030-01-02T00:00:00Z')}));assert.equal(next.replayed,false);assert.notEqual(next.body.entityId,first.body.entityId);
});
test('F3 new-schema unique collision recovers transaction using exact constraint/savepoint',async()=>{
 const v=await venue(),ids=await seed(v),other=await connect(),key=randomUUID(),args={principal:ids.actor,key,hash:digest('race'),body:{ok:true},now:new Date('2030-01-01Z')};
 await v.c.query('BEGIN');await other.query('BEGIN');const a=await insertResult(v.c,v.s,args);assert.equal(a.stored,true);
 const pending=insertResult(other,v.s,args);await v.c.query('COMMIT');const b=await pending;assert.equal(b.stored,false);assert.deepEqual(b.body,a.body);await other.query('COMMIT');
 assert.equal((await v.c.query(`SELECT count(*)::int AS n FROM ${v.q}.idempotency_results`)).rows[0].n,1);
 await other.query('BEGIN');await assert.rejects(insertResult(other,v.s,{...args,hash:digest('mismatch')}),e=>e.status===422);await other.query('ROLLBACK');
 await v.c.query('BEGIN');await assert.rejects(insertResult(v.c,v.s,{...args,principal:randomUUID(),key:randomUUID()}),e=>e.code==='23503');await v.c.query('ROLLBACK');
});
test('F1 physical-schema same-submission contention is serialized; loser is ordinary conflict',async()=>{
 const v=await venue(),ids=await seed(v),sub=await submission(v,ids),other=await connect();const args=decision(ids,sub);
 const result=await Promise.allSettled([publishSynthetic(v.c,v.s,args),publishSynthetic(other,v.s,{...args,key:randomUUID()})]);
 assert.equal(result.filter(r=>r.status==='fulfilled').length,1);assert.ok(result.find(r=>r.status==='rejected').reason instanceof Conflict);
 assert.equal((await v.c.query(`SELECT count(*)::int AS n FROM ${v.q}.published_revisions`)).rows[0].n,1);
});
test('F1 new regression: different submissions for same absent entity compare target revision atomically',async()=>{
 const v=await venue(),ids=await seed(v),entity=randomUUID(),a=await submission(v,ids,{entity}),b=await submission(v,ids,{entity}),other=await connect();
 const result=await Promise.allSettled([publishSynthetic(v.c,v.s,decision(ids,a)),publishSynthetic(other,v.s,decision(ids,b))]);
 assert.equal(result.filter(r=>r.status==='fulfilled').length,1);assert.ok(result.find(r=>r.status==='rejected').reason instanceof Conflict);
 assert.equal((await v.c.query(`SELECT count(*)::int AS n FROM ${v.q}.published_entities`)).rows[0].n,1);
 assert.equal((await v.c.query(`SELECT count(*)::int AS n FROM ${v.q}.published_revisions`)).rows[0].n,1);assert.equal(verifyAudit(await exportAudit(v.c,v.s)),true);
});
test('F4 physical audit triggers serialize independent appenders, rollback head, export and detect tampering',async()=>{
 const v=await venue(),ids=await seed(v),others=await Promise.all(Array.from({length:4},connect));
 await Promise.all(others.map(async(c)=>{await c.query('BEGIN');await appendAudit(c,v.s,{actor:ids.actor,kind:'principal',target:ids.author,revision:null,reason:'Synthetic "quotes", Č and newline\n'});await c.query('COMMIT');}));
 const exported=await exportAudit(v.c,v.s);assert.equal(exported.head.last_seq,'4');assert.equal(verifyAudit(JSON.parse(JSON.stringify(exported))),true);
 for(const alter of [s=>s.events[1].reason='tampered',s=>s.events.splice(1,1),s=>s.events.pop(),s=>s.events.reverse(),s=>s.head.last_hash='f'.repeat(64)]){const copy=structuredClone(exported);alter(copy);assert.throws(()=>verifyAudit(copy));}
 const before=await counts(v);await v.c.query('BEGIN');await appendAudit(v.c,v.s,{actor:ids.actor,kind:'principal',target:ids.author,revision:null});await v.c.query('ROLLBACK');assert.deepEqual(await counts(v),before);
});
test('F5 driver-default int8, prepared statements and safe JSON boundaries are lossless or rejected',async()=>{
 const v=await venue();for(const value of ['-9223372036854775808','-9007199254740992','-9007199254740991','0','9007199254740991','9007199254740992','9223372036854775807']){
  const {rows:[row]}=await v.c.query({name:'int8-boundary',text:'SELECT $1::bigint AS value',values:[value]});assert.equal(row.value,value);assert.equal(typeof row.value,'string');
  if(BigInt(value)>=BigInt(Number.MIN_SAFE_INTEGER)&&BigInt(value)<=BigInt(Number.MAX_SAFE_INTEGER))assert.equal(safeInt8(row.value),Number(value));else assert.throws(()=>safeInt8(row.value),RangeError);
 }
 assert.throws(()=>safeInt8(9007199254740992),TypeError);assert.throws(()=>safeInt8('9223372036854775808'),RangeError);
 assert.throws(()=>identifier('public; DROP SCHEMA public'),/prototype schema/);
});
test('F5 unsafe next entity revision fails before write and leaves transaction consistent',async()=>{
 const v=await venue(),ids=await seed(v),a=await submission(v,ids);await v.c.query('BEGIN');
 await v.c.query(`INSERT INTO ${v.q}.published_entities VALUES($1,'work',9007199254740991,'published',NULL)`,[a.entity]);
 await v.c.query(`INSERT INTO ${v.q}.published_revisions(entity_id,revision,submission_id,submission_revision,actor_id,payload,source_evidence) VALUES($1,9007199254740991,$2,1,$3,$4,jsonb_path_query_array($4::jsonb,'$.fields[*].evidence[*]'))`,[a.entity,a.id,ids.actor,payload]);await v.c.query('COMMIT');
 const b=await submission(v,ids,{entity:a.entity}),before=await counts(v);
 await assert.rejects(publishSynthetic(v.c,v.s,decision(ids,b,{expectedEntityRevision:'9007199254740991'})),RangeError);assert.deepEqual(await counts(v),before);
});
test('restricted local role cannot mutate history/head or invoke ungranted definer functions',async()=>{
 const v=await venue(),ids=await seed(v),role='proto_runtime_'+randomUUID().replaceAll('-','');
 await v.c.query(`CREATE ROLE ${role} NOLOGIN`);
 try{
  await v.c.query(`GRANT USAGE ON SCHEMA ${v.q} TO ${role}`);await v.c.query(`GRANT SELECT ON ALL TABLES IN SCHEMA ${v.q} TO ${role}`);
  await v.c.query(`GRANT EXECUTE ON FUNCTION ${v.q}.append_audit(uuid,text,text,uuid,bigint,uuid,text,text,text) TO ${role}`);
  await v.c.query(`SET ROLE ${role}`);await appendAudit(v.c,v.s,{actor:ids.actor,kind:'principal',target:ids.author,revision:null});
  await sqlstate(v.c,`UPDATE ${v.q}.audit_head SET last_seq=0`,[],'42501');await sqlstate(v.c,`DELETE FROM ${v.q}.audit_events`,[],'42501');
  await sqlstate(v.c,`SELECT ${v.q}.audit_content(NULL::${v.q}.audit_events)`,[],'42501');
 }finally{await v.c.query('RESET ROLE');await v.c.query(`DROP OWNED BY ${role}`);await v.c.query(`DROP ROLE ${role}`);}
 assert.equal(verifyAudit(await exportAudit(v.c,v.s)),true);
});


test('different submissions for an existing entity serialize target revision comparison',async()=>{
 const v=await venue(),ids=await seed(v),first=await submission(v,ids);await publishSynthetic(v.c,v.s,decision(ids,first));
 const a=await submission(v,ids,{entity:first.entity}),b=await submission(v,ids,{entity:first.entity}),other=await connect();
 const results=await Promise.allSettled([publishSynthetic(v.c,v.s,decision(ids,a,{expectedEntityRevision:1})),publishSynthetic(other,v.s,decision(ids,b,{expectedEntityRevision:1}))]);
 assert.equal(results.filter(r=>r.status==='fulfilled').length,1);assert.ok(results.find(r=>r.status==='rejected').reason instanceof Conflict);
 assert.equal((await v.c.query(`SELECT revision::text FROM ${v.q}.published_entities`)).rows[0].revision,'2');assert.equal(verifyAudit(await exportAudit(v.c,v.s)),true);
});
test('concurrent identical-key local publication returns one stored effect and replay',async()=>{
 const v=await venue(),ids=await seed(v),sub=await submission(v,ids),args=decision(ids,sub),other=await connect();
 const results=await Promise.all([publishSynthetic(v.c,v.s,args),publishSynthetic(other,v.s,args)]);
 assert.deepEqual(results[0].body,results[1].body);assert.equal(results.filter(r=>r.replayed).length,1);
 assert.equal((await v.c.query(`SELECT count(*)::int AS n FROM ${v.q}.published_revisions`)).rows[0].n,1);
});
test('non-idempotency uniqueness failure is not converted to replay; savepoint keeps transaction usable',async()=>{
 const v=await venue(),ids=await seed(v),hash=digest('unique-only-regression');
 await v.c.query(`ALTER TABLE ${v.q}.idempotency_results ADD CONSTRAINT synthetic_other_unique UNIQUE(request_hash)`);
 await v.c.query('BEGIN');await insertResult(v.c,v.s,{principal:ids.actor,key:randomUUID(),hash,body:{ok:true}});await v.c.query('COMMIT');
 await v.c.query('BEGIN');await assert.rejects(insertResult(v.c,v.s,{principal:ids.actor,key:randomUUID(),hash,body:{ok:true}}),e=>e.code==='23505'&&e.constraint==='synthetic_other_unique');
 assert.equal((await v.c.query('SELECT 1 AS n')).rows[0].n,1);await v.c.query('ROLLBACK');
});
test('role and session identifiers/history cannot be rewritten; active revocation remains possible',async()=>{
 const v=await venue(),ids=await seed(v);
 await sqlstate(v.c,`UPDATE ${v.q}.role_grants SET role='administrator'`,[],'55000');
 await sqlstate(v.c,`DELETE FROM ${v.q}.role_grants`,[],'55000');
 const id=randomUUID();await v.c.query(`INSERT INTO ${v.q}.sessions(id,principal_id,family_id,token_hash,issued_at,expires_at) VALUES($1,$2,$3,$4,now(),now()+interval '1 hour')`,[id,ids.author,randomUUID(),digest('fixed-session')]);
 await sqlstate(v.c,`UPDATE ${v.q}.sessions SET token_hash=$1`,[digest('replaced')],'55000');
 await v.c.query(`UPDATE ${v.q}.sessions SET revoked_at=clock_timestamp() WHERE id=$1`,[id]);
 await sqlstate(v.c,`UPDATE ${v.q}.sessions SET revoked_at=NULL`,[],'55000');
});


test('new immutable revision without atomic header advancement cannot commit',async()=>{
 const v=await venue(),ids=await seed(v),sub=await submission(v,ids);
 await v.c.query('BEGIN');await transition(v.c,v.s,{submission:sub.id,revision:1,actor:ids.actor,from:'in_review',to:'changes_requested',reason:'synthetic'});await v.c.query('COMMIT');
 const {this_hash:hash}=(await v.c.query(`SELECT this_hash FROM ${v.q}.submission_revisions`)).rows[0];
 await v.c.query('BEGIN');await v.c.query(`INSERT INTO ${v.q}.submission_revisions(submission_id,revision,payload,prev_hash) VALUES($1,2,$2,$3)`,[sub.id,payload,hash]);
 await sqlstate(v.c,'COMMIT',[],'23514');await v.c.query('ROLLBACK');
 assert.equal((await v.c.query(`SELECT count(*)::int AS n FROM ${v.q}.submission_revisions`)).rows[0].n,1);
});
test('typed existing target FK and publication kind/evidence cannot disagree',async()=>{
 const v=await venue(),ids=await seed(v),sub=await submission(v,ids);await publishSynthetic(v.c,v.s,decision(ids,sub));
 await sqlstate(v.c,`INSERT INTO ${v.q}.submissions(id,author_id,target_kind,catalogue_uuid,target_entity_id,expected_revision,current_revision) VALUES($1,$2,'artist',$3,$3,1,1)`,[randomUUID(),ids.author,sub.entity],'23503');
 const next=await submission(v,ids);await v.c.query('BEGIN');await v.c.query(`INSERT INTO ${v.q}.published_entities VALUES($1,'artist',1,'published',NULL)`,[next.entity]);
 await sqlstate(v.c,`INSERT INTO ${v.q}.published_revisions(entity_id,revision,submission_id,submission_revision,actor_id,payload,source_evidence) VALUES($1,1,$2,1,$3,$4,jsonb_path_query_array($4::jsonb,'$.fields[*].evidence[*]'))`,[next.entity,next.id,ids.actor,payload],'23514');await v.c.query('ROLLBACK');
 await v.c.query('BEGIN');await v.c.query(`INSERT INTO ${v.q}.published_entities VALUES($1,'work',1,'published',NULL)`,[next.entity]);
 await sqlstate(v.c,`INSERT INTO ${v.q}.published_revisions(entity_id,revision,submission_id,submission_revision,actor_id,payload,source_evidence) VALUES($1,1,$2,1,$3,$4,'[]')`,[next.entity,next.id,ids.actor,payload],'23514');await v.c.query('ROLLBACK');
});
test('bounded lock wait preserves native SQLSTATE and rolls back rather than hiding it as conflict',async()=>{
 const v=await venue(),ids=await seed(v),sub=await submission(v,ids),other=await connect(),before=await counts(v);
 await other.query('BEGIN');await other.query(`SELECT 1 FROM ${v.q}.submissions WHERE id=$1 FOR UPDATE`,[sub.id]);
 try {await assert.rejects(publishSynthetic(v.c,v.s,decision(ids,sub)),e=>e.code==='55P03' && !(e instanceof Conflict));}
 finally {await other.query('ROLLBACK');}
 assert.deepEqual(await counts(v),before);assert.equal((await publishSynthetic(v.c,v.s,decision(ids,sub))).status,201);
});
