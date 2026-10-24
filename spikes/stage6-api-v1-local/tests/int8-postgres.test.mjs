// Actual HTTP listener → adapter → real PG17: checked full-range int8 mutations.
// BigInt/decimal-string arithmetic only; no mutation value rounds through Number.
import {test} from 'node:test';
import assert from 'node:assert/strict';
import {randomUUID,createHash} from 'node:crypto';
import {venue,get,decide,intent,payload,snapshot} from './pg-http-venue.mjs';
import {exportAudit,verifyAudit} from '../../stage6-handlers-local/src/audit.mjs';

const MAX='9223372036854775807';
const MAX_SAFE=String(Number.MAX_SAFE_INTEGER); // 9007199254740991
const ABOVE_SAFE='9007199254740992'; // MAX_SAFE+1, still inside int8
const equalStatus=(r,status)=>assert.equal(r.status,status,JSON.stringify(r.body));

// Seed a submission in_review whose expected entity revision and a published
// entity head sit at an arbitrary full-range revision, satisfying every schema
// guard (separate publication attribution, payload/evidence/hash checks).
// Stored submission revisions cannot start above 1 (revision chain guard), so
// full-range submission-revision coverage exercises exact comparisons instead.
async function seedEntityHead(v,{entityHead}){
 const entity=randomUUID(),sid=randomUUID(),p=payload(entity),attributionPayload=payload(entity);
 const attribution=await v.submissions.create(v.author.accessToken,{schemaVersion:1,payload:attributionPayload,expectedRevision:0,idempotencyKey:randomUUID()});
 await v.db.query('BEGIN');
 try{
  await v.db.query(`INSERT INTO ${v.q}.published_entities(id,kind,revision,status) VALUES($1,'work',$2,'published')`,[entity,entityHead]);
  await v.db.query(`INSERT INTO ${v.q}.published_revisions(entity_id,revision,submission_id,submission_revision,actor_id,payload,source_evidence) VALUES($1,$2,$3,1,$4,$5,jsonb_path_query_array($5::jsonb,'$.fields[*].evidence[*]'))`,[entity,entityHead,attribution.body.id,v.mod.principalId,attributionPayload]);
  // Target submission: direct initial insert (status submitted/current_revision 1), then open through HTTP.
  await v.db.query(`INSERT INTO ${v.q}.submissions(id,author_id,target_kind,catalogue_uuid,expected_revision,current_revision) VALUES($1,$2,'work',$3,$4,1)`,[sid,v.author.principalId,entity,entityHead]);
  await v.db.query(`INSERT INTO ${v.q}.submission_revisions(submission_id,revision,payload,prev_hash) VALUES($1,1,$2,repeat('0',64))`,[sid,p]);
  await v.db.query('COMMIT');
 }catch(e){await v.db.query('ROLLBACK');throw e;}
 const s={id:sid,entity,path:'/v1/admin/submissions/'+sid};
 equalStatus(await v.send(v.mod,'POST',s.path+'/open',intent({expectedEntityRevision:entityHead,expectedSubmissionRevision:'1'})),200);
 return s;
}

test('INT8-PG entity increment crosses the safe-integer boundary losslessly end to end',async t=>{
 const v=await venue(t),s=await seedEntityHead(v,{entityHead:MAX_SAFE});
 const first=await decide(v,s,{expectedEntityRevision:MAX_SAFE,expectedSubmissionRevision:'1'});
 equalStatus(first,201);
 assert.equal(first.body.publishedEntityRevision,ABOVE_SAFE); // exact decimal string, never Number-rounded
 const stored=await v.db.query(`SELECT e.revision::text AS entity,r.revision::text AS rev FROM ${v.q}.published_entities e JOIN ${v.q}.published_revisions r ON r.entity_id=e.id AND r.revision=e.revision WHERE e.id=$1`,[s.entity]);
 assert.equal(stored.rows[0].entity,ABOVE_SAFE);assert.equal(stored.rows[0].rev,ABOVE_SAFE);
 const feed=(await v.db.query(`SELECT revision::text AS r FROM ${v.q}.change_feed WHERE entity_id=$1`,[s.entity])).rows;
 assert.equal(feed.at(-1).r,ABOVE_SAFE);
 assert.equal(verifyAudit(await exportAudit(v.db,v.schema)),true);
});

test('INT8-PG same-key replay of a full-range approval is byte-exact and effect-free',async t=>{
 const v=await venue(t),s=await seedEntityHead(v,{entityHead:MAX_SAFE});
 const b=intent({decision:'approve',expectedEntityRevision:MAX_SAFE,expectedSubmissionRevision:'1'});
 const first=await v.send(v.admin,'POST',s.path+'/decisions',b);equalStatus(first,201);
 assert.equal(first.body.publishedEntityRevision,ABOVE_SAFE);
 const prior=await snapshot(v);await v.fresh();
 const replay=await v.send(v.admin,'POST',s.path+'/decisions',b);
 assert.equal(replay.raw,first.raw);assert.deepEqual(await snapshot(v),prior);
 // Changed intent under the same key is a mismatch, not a second publication.
 equalStatus(await v.send(v.admin,'POST',s.path+'/decisions',{...b,expectedEntityRevision:ABOVE_SAFE}),422);
 assert.deepEqual(await snapshot(v),prior);
});

test('INT8-PG entity at signed int8 maximum refuses overflow with conflict and zero effects',async t=>{
 const v=await venue(t),s=await seedEntityHead(v,{entityHead:MAX}),before=await snapshot(v);
 const r=await decide(v,s,{expectedEntityRevision:MAX,expectedSubmissionRevision:'1'});equalStatus(r,409);
 assert.deepEqual(await snapshot(v),before);
 // The submission is untouched and still decidable with a non-overflowing expectation is impossible here:
 // the only consistent expectation is MAX, which cannot increment. Nothing was published.
 assert.equal((await v.db.query(`SELECT count(*)::text AS n FROM ${v.q}.published_revisions WHERE entity_id=$1`,[s.entity])).rows[0].n,'1');
 assert.equal(verifyAudit(await exportAudit(v.db,v.schema)),true);
});

test('INT8-PG concurrent full-range approvals serialize to exactly one publication',async t=>{
 const v=await venue(t),s=await seedEntityHead(v,{entityHead:MAX_SAFE});
 const b=intent({decision:'approve',expectedEntityRevision:MAX_SAFE,expectedSubmissionRevision:'1'});
 const rs=await Promise.all([v.send(v.admin,'POST',s.path+'/decisions',b),v.send(v.mod,'POST',s.path+'/decisions',b)]);
 assert.deepEqual(rs.map(r=>r.status).sort(),[201,409]);
 const ok=rs.find(r=>r.status===201);assert.equal(ok.body.publishedEntityRevision,ABOVE_SAFE);
 assert.equal((await v.db.query(`SELECT count(*)::text AS n FROM ${v.q}.published_revisions WHERE entity_id=$1`,[s.entity])).rows[0].n,'2'); // attribution + one publication
 assert.equal((await v.db.query(`SELECT count(*)::text AS n FROM ${v.q}.change_feed WHERE entity_id=$1`,[s.entity])).rows[0].n,'1');
 assert.equal(verifyAudit(await exportAudit(v.db,v.schema)),true);
});

test('INT8-PG storage fault during full-range approval rolls back every effect; unchanged retry recovers',async t=>{
 const v=await venue(t),s=await seedEntityHead(v,{entityHead:MAX_SAFE});
 const b=intent({decision:'approve',expectedEntityRevision:MAX_SAFE,expectedSubmissionRevision:'1'}),before=await snapshot(v);
 let fired=false;
 v.setFault(async sql=>{if(sql.startsWith('INSERT INTO')&&sql.includes('.change_feed(')){fired=true;throw Error('private SQL fault must not escape');}});
 const failed=await v.send(v.admin,'POST',s.path+'/decisions',b);equalStatus(failed,503);assert.ok(fired);assert.ok(!failed.raw.includes('private SQL'));
 v.setFault(null);assert.deepEqual(await snapshot(v),before);
 const ok=await v.send(v.admin,'POST',s.path+'/decisions',b);equalStatus(ok,201);assert.equal(ok.body.publishedEntityRevision,ABOVE_SAFE);
});

test('INT8-PG minimum valid revisions approve a new entity at exactly 1',async t=>{
 const v=await venue(t),s=await v.create({open:true}); // expected 0 / submission 1
 const r=await decide(v,s,{expectedEntityRevision:'0',expectedSubmissionRevision:'1'});
 equalStatus(r,201);assert.equal(r.body.publishedEntityRevision,'1');assert.equal(r.body.submissionRevision,'1');
});

test('INT8-PG malformed, underflow and above-max revision writes are rejected without dispatch effects',async t=>{
 const v=await venue(t),s=await seedEntityHead(v,{entityHead:MAX}),before=await snapshot(v);
 for(const bad of [
  {expectedEntityRevision:'-1'},
  {expectedEntityRevision:'00'},
  {expectedEntityRevision:'9223372036854775808'},
  {expectedEntityRevision:'1e2'},
  {expectedEntityRevision:9007199254740993}, // unsafe JS Number literal, not a string
  {expectedSubmissionRevision:'0'}, // submission revision must be positive
  {expectedSubmissionRevision:'-9223372036854775808'},
  {expectedSubmissionRevision:'9223372036854775808'}
 ])equalStatus(await decide(v,s,{decision:'approve',...bad}),400);
 assert.deepEqual(await snapshot(v),before);
});

test('INT8-PG stale full-range expectations conflict exactly; correct values proceed',async t=>{
 const v=await venue(t),s=await seedEntityHead(v,{entityHead:MAX});
 // Off-by-one above MAX_SAFE must not alias through rounding.
 equalStatus(await decide(v,s,{expectedEntityRevision:'9223372036854775806',expectedSubmissionRevision:'1'}),409);
 equalStatus(await decide(v,s,{expectedEntityRevision:MAX,expectedSubmissionRevision:'2'}),409);
 equalStatus(await decide(v,s,{expectedEntityRevision:MAX,expectedSubmissionRevision:'9007199254740993'}),409);
 // The correct full-range expectation matches, then overflows at increment: distinct 409 with no effects.
 const before=await snapshot(v);equalStatus(await decide(v,s,{expectedEntityRevision:MAX,expectedSubmissionRevision:'1'}),409);
 assert.deepEqual(await snapshot(v),before);
});

test('INT8-PG queue, detail and comparison read full-range revisions back exactly',async t=>{
 const v=await venue(t),s=await seedEntityHead(v,{entityHead:MAX});
 const q=await get(v,'/v1/admin/queue?q='+s.id);equalStatus(q,200);
 assert.equal(q.body.items[0].expectedEntityRevision,MAX);assert.equal(q.body.items[0].submissionRevision,'1');
 const d=await get(v,s.path);equalStatus(d,200);assert.equal(d.body.expectedEntityRevision,MAX);assert.equal(d.body.submissionRevision,'1');
 const c=await get(v,'/v1/admin/entities/'+s.entity+'/comparison?submissionId='+s.id);equalStatus(c,200);
 assert.equal(c.body.state,'available');assert.equal(c.body.entityRevision,MAX);
 const a=await get(v,'/v1/admin/audit?subjectId='+s.id);equalStatus(a,200);
 assert.ok(a.body.items.length>=1);assert.ok(a.body.items.every(x=>x.subject.revision===null||/^[0-9]+$/.test(x.subject.revision)));
 // Audit self-hash chain remains intact around full-range subjects.
 let prev='0'.repeat(64);const all=await get(v,'/v1/admin/audit?limit=20');
 for(const e of all.body.items){assert.equal(e.prevHash,prev);assert.equal(createHash('sha256').update(prev+':'+e.canonicalContent).digest('hex'),e.thisHash);prev=e.thisHash;}
 assert.equal(verifyAudit(await exportAudit(v.db,v.schema)),true);
});

test('INT8-PG full-range request_changes then author revision chain stays exact',async t=>{
 const v=await venue(t),s=await seedEntityHead(v,{entityHead:MAX});
 // Full-range entity head: request_changes validates both revisions but never publishes.
 const rc=await decide(v,s,{decision:'request_changes',reason:'Synthetic full-range rework request',expectedEntityRevision:MAX,expectedSubmissionRevision:'1'});
 equalStatus(rc,200);
 let d=await get(v,s.path);equalStatus(d,200);assert.equal(d.body.status,'changes_requested');assert.equal(d.body.submissionRevision,'1');assert.equal(d.body.expectedEntityRevision,MAX);
 // Author revision: BigInt increment 1→2, exact decimal-string persistence (never a rounded Number).
 const revised=await v.submissions.revise(v.author.accessToken,{schemaVersion:1,id:s.id,payload:payload(s.entity),expectedRevision:MAX,expectedSubmissionRevision:'1',idempotencyKey:randomUUID()});
 assert.equal(revised.status,201,JSON.stringify(revised.body));assert.equal(revised.body.revision,'2');
 assert.equal((await v.db.query(`SELECT current_revision::text AS n FROM ${v.q}.submissions WHERE id=$1`,[s.id])).rows[0].n,'2');
 // Stale and above-safe submission expectations on the chained revision conflict exactly.
 equalStatus(await decide(v,s,{decision:'reject',reason:'Stale full-range expectation',expectedEntityRevision:MAX,expectedSubmissionRevision:'1'}),409);
 equalStatus(await decide(v,s,{decision:'reject',reason:'Above-safe expectation',expectedEntityRevision:MAX,expectedSubmissionRevision:'9007199254740993'}),409);
 // Correct full-range chain values proceed through the reject path.
 equalStatus(await decide(v,s,{decision:'reject',reason:'Synthetic full-range rejection',expectedEntityRevision:MAX,expectedSubmissionRevision:'2'}),200);
 d=await get(v,s.path);equalStatus(d,200);assert.equal(d.body.status,'rejected');assert.equal(d.body.submissionRevision,'2');
 assert.equal(d.body.revisions.items[0].submissionRevision,'2');assert.equal(d.body.revisions.items.length,2);
 assert.equal(d.body.reviews.items.at(-1).submissionRevision,'2');
 assert.equal(verifyAudit(await exportAudit(v.db,v.schema)),true);
});

test('INT8-PG exact odd above-safe entity revision increments without aliasing',async t=>{
 const v=await venue(t),head='9007199254740993',s=await seedEntityHead(v,{entityHead:head});
 const before=await snapshot(v);
 equalStatus(await decide(v,s,{expectedEntityRevision:'9007199254740992'}),409);
 assert.deepEqual(await snapshot(v),before);
 const r=await decide(v,s,{expectedEntityRevision:head});equalStatus(r,201);
 assert.equal(r.body.publishedEntityRevision,'9007199254740994');
 assert.equal(verifyAudit(await exportAudit(v.db,v.schema)),true);
});

test('INT8-PG publication can reach maximum exactly, then rejects the next increment',async t=>{
 const v=await venue(t),head='9223372036854775806',s=await seedEntityHead(v,{entityHead:head});
 const r=await decide(v,s,{expectedEntityRevision:head});equalStatus(r,201);assert.equal(r.body.publishedEntityRevision,MAX);
 assert.equal((await v.db.query(`SELECT revision::text AS r FROM ${v.q}.published_entities WHERE id=$1`,[s.entity])).rows[0].r,MAX);
 const next=await v.create({entity:s.entity,expected:MAX,open:true}),before=await snapshot(v);
 equalStatus(await decide(v,next,{expectedEntityRevision:MAX}),409);
 assert.deepEqual(await snapshot(v),before);
 assert.equal(verifyAudit(await exportAudit(v.db,v.schema)),true);
});

test('INT8-PG concurrent same-principal full-range identical intent replays exactly once',async t=>{
 const v=await venue(t),s=await seedEntityHead(v,{entityHead:'9007199254740993'});
 const b=intent({decision:'approve',expectedEntityRevision:'9007199254740993'});
 const rs=await Promise.all([v.send(v.admin,'POST',s.path+'/decisions',b),v.send(v.admin,'POST',s.path+'/decisions',b)]);
 assert.deepEqual(rs.map(r=>r.status),[201,201]);assert.equal(rs[0].raw,rs[1].raw);
 assert.equal((await v.db.query(`SELECT count(*)::text AS n FROM ${v.q}.change_feed WHERE entity_id=$1`,[s.entity])).rows[0].n,'1');
 assert.equal(verifyAudit(await exportAudit(v.db,v.schema)),true);
});
