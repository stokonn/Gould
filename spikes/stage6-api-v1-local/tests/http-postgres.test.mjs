// Actual HTTP listener → adapter → real PG17; never scripted SQL or skip fallback.
import {test} from 'node:test';
import assert from 'node:assert/strict';
import {randomUUID,createHash} from 'node:crypto';
import {venue,get,decide,intent,payload,snapshot} from './pg-http-venue.mjs';
import {exportAudit,verifyAudit} from '../../stage6-handlers-local/src/audit.mjs';
import {pragueWindow} from '../src/statistics.mjs';

const equalStatus=(r,status)=>assert.equal(r.status,status,JSON.stringify(r.body));
test('HTTP-PG challenge/login/session/capabilities/refresh/logout and account isolation',async t=>{
 const v=await venue(t),s=await get(v,'/v1/session');equalStatus(s,200);assert.equal(s.body.principalId,v.admin.principalId);assert.equal(s.body.sessionId,v.admin.sessionId);
 const caps=await get(v,'/v1/me/capabilities');assert.equal(caps.body.principalId,v.admin.principalId);assert.ok(caps.body.capabilities.includes('audit.read'));
 const rotated=await v.send(null,'POST','/v1/session/refresh',{schemaVersion:1,refreshToken:v.admin.refreshToken});equalStatus(rotated,200);assert.notEqual(rotated.body.sessionId,v.admin.sessionId);
 equalStatus(await get(v,'/v1/session',v.admin),401);equalStatus(await get(v,'/v1/session',rotated.body),200);
 const logout=await v.send(rotated.body,'DELETE','/v1/session');equalStatus(logout,204);assert.equal(logout.raw,'');equalStatus(await get(v,'/v1/session',rotated.body),401);
 equalStatus(await get(v,'/v1/session',v.other),200);
});
test('HTTP-PG concurrent refresh rotates once and replay revokes family before or after response',async t=>{
 const v=await venue(t),u=await v.signIn('race');const b={schemaVersion:1,refreshToken:u.refreshToken};
 const results=await Promise.all([v.send(null,'POST','/v1/session/refresh',b),v.send(null,'POST','/v1/session/refresh',b)]);
 assert.ok(results.every(r=>[200,401].includes(r.status)));assert.ok(results.some(r=>r.status===401));
 for(const r of results.filter(r=>r.status===200))equalStatus(await get(v,'/v1/session',r.body),401);
 const events=(await v.db.query(`SELECT action FROM ${v.q}.audit_events WHERE actor_id=$1`,[u.principalId])).rows.map(x=>x.action);assert.equal(events.filter(x=>x==='session.rotated').length,1);assert.equal(events.filter(x=>x==='session.refresh_replay').length,1);
 equalStatus(await get(v,'/v1/session',v.other),200);
});
test('HTTP-PG expired challenge, bad identity, expired access, and absolute family expiry',async t=>{
 const v=await venue(t);const ch=await v.send(null,'POST','/v1/session/challenge',{schemaVersion:1});v.advance(300000);
 equalStatus(await v.send(null,'POST','/v1/session',{schemaVersion:1,challenge:ch.body.challenge,credential:JSON.stringify({issuer:'https://synthetic.invalid',audience:'local-test',subject:'late',nonce:ch.body.nonce,iat:v.clock(),exp:v.clock()+1000})}),401);
 v.advance(600000);equalStatus(await get(v,'/v1/session'),401);
 v.advance(604800000);equalStatus(await v.send(null,'POST','/v1/session/refresh',{schemaVersion:1,refreshToken:v.admin.refreshToken}),401);
});
test('HTTP-PG queue exact safe DTO, metadata null, evidence count and no private payload',async t=>{
 const v=await venue(t),s=await v.create();const r=await get(v,'/v1/admin/queue');equalStatus(r,200);assert.equal(r.body.items.length,1);
 assert.deepEqual(r.body.items[0],{submissionId:s.id,authorId:v.author.principalId,status:'submitted',kind:'work',createdAt:r.body.items[0].createdAt,updatedAt:r.body.items[0].updatedAt,expectedEntityRevision:'0',submissionRevision:'1',evidenceCount:'1',display:{title:null,composer:null}});
 assert.equal(r.body.coverage,'page');assert.equal(r.body.searchScope,'id_title_composer_kind');assert.ok(!r.raw.includes('Synthetic reference'));
 await v.fresh({validator:undefined});equalStatus(await get(v,'/v1/admin/queue'),200); // Safe metadata is independent of disclosure policy.
});
test('HTTP-PG queue filters precede limit; UUID/kind search; percent underscore literal',async t=>{
 const v=await venue(t);for(let i=0;i<3;i++)await v.create();const s=await v.create({open:true});
 for(const query of ['status=in_review&limit=1','q='+s.id,'q=WoRk&status=in_review','kind=work&status=in_review']){
  const r=await get(v,'/v1/admin/queue?'+query);equalStatus(r,200);assert.equal(r.body.items.length,1);assert.equal(r.body.items[0].submissionId,s.id);
 }
 for(const q of ['%','_','Synthetic work','wórk'])assert.equal((await get(v,'/v1/admin/queue?q='+encodeURIComponent(q))).body.items.length,0);
});
test('HTTP-PG queue microsecond keyset pages have no duplicates and survive adapter restart',async t=>{
 const v=await venue(t);for(let i=0;i<5;i++)await v.create();
 const first=await get(v,'/v1/admin/queue?limit=2');assert.ok(first.body.nextCursor);await v.fresh();
 const second=await get(v,'/v1/admin/queue?limit=2&cursor='+first.body.nextCursor);
 const third=await get(v,'/v1/admin/queue?limit=2&cursor='+second.body.nextCursor);
 assert.equal(third.body.nextCursor,null);const ids=[...first.body.items,...second.body.items,...third.body.items].map(x=>x.submissionId);
 assert.equal(new Set(ids).size,5);assert.equal(ids.length,5);
});
test('HTTP-PG cursor tamper, scope, caller, filters and exact 15-minute expiry fail closed',async t=>{
 const v=await venue(t);for(let i=0;i<2;i++)await v.create();const first=await get(v,'/v1/admin/queue?limit=1'),cursor=first.body.nextCursor;
 for(const path of ['/v1/admin/queue?limit=1&status=submitted&cursor='+cursor,'/v1/admin/audit?cursor='+cursor,'/v1/admin/queue?cursor='+cursor.slice(0,-4)+'AAAA'])equalStatus(await get(v,path),400);
 equalStatus(await get(v,'/v1/admin/queue?cursor='+cursor,v.mod),400);
 v.advance(899999);const refreshed=await v.send(null,'POST','/v1/session/refresh',{schemaVersion:1,refreshToken:v.admin.refreshToken});equalStatus(refreshed,200);
 equalStatus(await get(v,'/v1/admin/queue?cursor='+cursor,refreshed.body),200);
 v.advance(1);equalStatus(await get(v,'/v1/admin/queue?cursor='+cursor,refreshed.body),400);
});
test('HTTP-PG audit exact canonical DTO, fixed snapshot head and independent hash chain',async t=>{
 const v=await venue(t),s=await v.create({open:true});equalStatus(await decide(v,s),201);
 let cursor,high,head,items=[];do{
  const r=await get(v,'/v1/admin/audit?limit=2'+(cursor?'&cursor='+cursor:''));equalStatus(r,200);assert.equal(r.body.coverage,'chain');
  high??=r.body.highwater;head??=r.body.headHash;assert.equal(r.body.highwater,high);assert.equal(r.body.headHash,head);items.push(...r.body.items);cursor=r.body.nextCursor;
  if(items.length===2)await v.create();
 }while(cursor);
 assert.equal(String(items.length),high);let prev='0'.repeat(64);
 for(const e of items){assert.equal(e.prevHash,prev);assert.equal(createHash('sha256').update(prev+':'+e.canonicalContent).digest('hex'),e.thisHash);prev=e.thisHash;}
 assert.equal(prev,head);assert.ok(BigInt((await get(v,'/v1/admin/audit')).body.highwater)>BigInt(high));
 assert.equal(verifyAudit(await exportAudit(v.db,v.schema)),true);
});
test('HTTP-PG audit AND filters, actual action vocabulary, time bounds and literal CZ/NFC search',async t=>{
 const v=await venue(t),s=await v.create({open:true});equalStatus(await decide(v,s,{decision:'reject',reason:'Český %_ důvod'}),200);
 const all=await get(v,'/v1/admin/audit?action=in_review-%3Erejected');equalStatus(all,200);const e=all.body.items[0];assert.equal(e.subject.id,s.id);
 const q=new URLSearchParams({action:e.action,actorId:v.admin.principalId,subjectId:s.id,from:e.createdAt,q:'c\u030ceský %_',limit:'1'});
 const r=await get(v,'/v1/admin/audit?'+q);equalStatus(r,200);assert.equal(r.body.coverage,'filtered');assert.equal(r.body.items.length,1);
 for(const query of ['q=cesky','q=Synthetic','to='+e.createdAt,'actorId='+v.other.principalId])assert.equal((await get(v,'/v1/admin/audit?action=in_review-%3Erejected&'+query)).body.items.length,0);
 equalStatus(await get(v,'/v1/admin/audit?action=submitted-%3Ein_review'),200);
});
test('HTTP-PG exact stats count approval once and use database Prague day',async t=>{
 const v=await venue(t);await v.create();await v.create({open:true});const info=await v.create({open:true}),ok=await v.create({open:true}),no=await v.create({open:true});
 equalStatus(await decide(v,info,{decision:'request_changes',reason:'Synthetic details'}),200);equalStatus(await decide(v,ok),201);equalStatus(await decide(v,no,{decision:'reject',reason:'Synthetic rejection'}),200);
 const r=await get(v,'/v1/admin/stats');equalStatus(r,200);assert.equal(r.body.state,'exact');assert.equal(r.body.pending,'2');assert.equal(r.body.needsInfo,'1');assert.equal(r.body.approvedInWindow,'1');assert.equal(r.body.rejectedInWindow,'1');
 const db=await v.db.query(`SELECT to_char(clock_timestamp() AT TIME ZONE 'Europe/Prague','YYYY-MM-DD') AS day`);const {elapsedHours:_,...window}=pragueWindow(db.rows[0].day);assert.deepEqual(r.body.window,window);
 assert.equal(r.body.auditHighwater,(await get(v,'/v1/admin/audit')).body.highwater);
});
test('HTTP-PG stats snapshot remains coherent during concurrent publication',async t=>{
 const v=await venue(t),s=await v.create({open:true});const before=await get(v,'/v1/admin/stats');let fired=false;
 v.setFault(async sql=>{if(!fired&&sql.includes("(SELECT last_seq::text")){fired=true;equalStatus(await decide(v,s,{},v.mod),201);}});
 const during=await get(v,'/v1/admin/stats');equalStatus(during,200);assert.equal(during.body.pending,before.body.pending);assert.equal(during.body.approvedInWindow,before.body.approvedInWindow);assert.equal(during.body.auditHighwater,before.body.auditHighwater);
 v.setFault(null);const after=await get(v,'/v1/admin/stats');assert.equal(after.body.pending,'0');assert.equal(after.body.approvedInWindow,'1');
});
test('HTTP-PG dual revisions, open, comparison, publication and exact persisted replay',async t=>{
 const v=await venue(t),s=await v.create(),b=intent();equalStatus(await v.send(v.admin,'POST',s.path+'/open',b),200);
 const d=await get(v,s.path);equalStatus(d,200);assert.equal(d.body.expectedEntityRevision,'0');assert.equal(d.body.submissionRevision,'1');
 equalStatus(await get(v,'/v1/admin/entities/'+s.entity+'/comparison?submissionId='+s.id),200);
 const decision=intent({decision:'approve'}),first=await v.send(v.admin,'POST',s.path+'/decisions',decision);equalStatus(first,201);await v.fresh();
 const prior=await snapshot(v),replay=await v.send(v.admin,'POST',s.path+'/decisions',decision);assert.equal(replay.raw,first.raw);assert.deepEqual(await snapshot(v),prior);
 equalStatus(await v.send(v.admin,'POST',s.path+'/decisions',{...decision,reason:'Changed intent'}),422);
 equalStatus(await get(v,s.path),200);
 const current=await get(v,'/v1/admin/entities/'+s.entity+'/comparison?submissionId='+s.id);equalStatus(current,200);assert.equal(current.body.state,'available');assert.equal(current.body.entityRevision,'1');
});
test('HTTP-PG stale entity/proposal conflicts and revised proposal requires explicit new intent',async t=>{
 const v=await venue(t),s=await v.create({open:true});equalStatus(await decide(v,s,{decision:'request_changes',reason:'Revise'}),200);
 // Author revision endpoint is not in the approved public adapter: fixture uses native service.
 await v.submissions.revise(v.author.accessToken,{schemaVersion:1,id:s.id,payload:payload(s.entity),expectedRevision:0,expectedSubmissionRevision:1,idempotencyKey:randomUUID()});
 const before=await snapshot(v);equalStatus(await decide(v,s),409);equalStatus(await decide(v,s,{expectedEntityRevision:'1',expectedSubmissionRevision:'2'}),409);assert.deepEqual(await snapshot(v),before);
 assert.equal((await get(v,s.path)).body.status,'in_review');
 equalStatus(await decide(v,s,{expectedSubmissionRevision:'2'}),201);
});
test('HTTP-PG same-key concurrency exact replay and competing moderators one publication',async t=>{
 const v=await venue(t),s=await v.create({open:true}),b=intent({decision:'approve'});
 const rs=await Promise.all([v.send(v.admin,'POST',s.path+'/decisions',b),v.send(v.admin,'POST',s.path+'/decisions',b)]);rs.forEach(r=>equalStatus(r,201));assert.equal(rs[0].raw,rs[1].raw);
 const other=await v.create({open:true}),race=await Promise.all([decide(v,other),decide(v,other,{},v.mod)]);assert.deepEqual(race.map(r=>r.status).sort(),[201,409]);
 assert.equal((await v.db.query(`SELECT count(*)::text AS n FROM ${v.q}.published_revisions`)).rows[0].n,'2');assert.equal(verifyAudit(await exportAudit(v.db,v.schema)),true);
});
test('HTTP-PG SQL failure rolls back domain, audit and receipt; unchanged intent retry recovers',async t=>{
 const v=await venue(t),s=await v.create({open:true}),b=intent({decision:'approve'}),before=await snapshot(v);let fired=false;
 v.setFault(async sql=>{if(sql.startsWith('INSERT INTO')&&sql.includes('.idempotency_results(')){fired=true;throw Error('private SQL fault must not escape');}});
 const failed=await v.send(v.admin,'POST',s.path+'/decisions',b);equalStatus(failed,503);assert.ok(fired);assert.ok(!failed.raw.includes('private SQL'));v.setFault(null);assert.deepEqual(await snapshot(v),before);
 equalStatus(await v.send(v.admin,'POST',s.path+'/decisions',b),201);
});
test('HTTP-PG read timeout is explicit unavailable and next request recovers without mutation',async t=>{
 const v=await venue(t);await v.create();const before=await snapshot(v);
 for(const [path,needle] of [['queue','SELECT s.id,s.author_id'],['audit','SELECT last_seq::text,last_hash'],['stats','(SELECT last_seq::text FROM']]){
  v.setFault(async(sql,_args,_c,query)=>{if(sql.includes(needle)){await query("SET LOCAL statement_timeout='1ms'");await query('SELECT pg_sleep(0.02)');}});
  equalStatus(await get(v,'/v1/admin/'+path),503);v.setFault(null);equalStatus(await get(v,'/v1/admin/'+path),200);assert.deepEqual(await snapshot(v),before);
 }
});
test('HTTP-PG live role revocation, suspension, self-review and foreign account isolation',async t=>{
 const v=await venue(t),s=await v.create({open:true});
 for(const path of ['/v1/admin/queue','/v1/admin/audit','/v1/admin/stats']){equalStatus(await get(v,path,v.other),403);equalStatus(await get(v,path,null),401);}
 equalStatus(await get(v,'/v1/admin/audit',v.mod),403);equalStatus(await get(v,'/v1/admin/stats',v.mod),403);
 await v.db.query(`INSERT INTO ${v.q}.role_grants(id,principal_id,role,granted_by,granted_at) VALUES($1,$2,'moderator',$3,$4)`,[randomUUID(),v.author.principalId,v.admin.principalId,new Date(v.clock())]);
 equalStatus(await decide(v,s,{},v.author),403);
 await v.adapter().authorization.changeRole(v.admin.accessToken,{principalId:v.mod.principalId,role:'moderator'},true);
 equalStatus(await get(v,'/v1/admin/queue',v.mod),403);
 await v.adapter().authorization.suspend(v.admin.accessToken,{principalId:v.other.principalId,suspended:true});equalStatus(await get(v,'/v1/session',v.other),403);
});
test('HTTP-PG malformed queries and unsafe int8 writes fail closed; safe errors hide SQL',async t=>{
 const v=await venue(t),s=await v.create({open:true}),before=await snapshot(v);
 for(const path of ['/v1/admin/queue?limit=21','/v1/admin/queue?status=submitted&status=in_review','/v1/admin/audit?from=2030-02-30T00%3A00%3A00.000000Z','/v1/admin/audit?actorId=bad','/v1/admin/stats?limit=1','/v1/admin/queue?__proto__=bad'])equalStatus(await get(v,path),400);
 equalStatus(await decide(v,s,{expectedEntityRevision:'9223372036854775807'}),503);equalStatus(await decide(v,s,{expectedEntityRevision:'9223372036854775808'}),400);equalStatus(await decide(v,s,{expectedSubmissionRevision:'01'}),400);
 assert.deepEqual(await snapshot(v),before);
});
test('HTTP-PG Prague DST windows equal PostgreSQL civil boundaries, exclusive end includes no next day',async t=>{
 const v=await venue(t);
 for(const day of ['2026-03-29','2026-10-25','2026-10-24','2026-01-01']){
  const w=pragueWindow(day),r=await v.db.query(`SELECT
   to_char($1::date::timestamp AT TIME ZONE 'Europe/Prague' AT TIME ZONE 'UTC','YYYY-MM-DD"T"HH24:MI:SS.US"Z"') AS start,
   to_char(($1::date+1)::timestamp AT TIME ZONE 'Europe/Prague' AT TIME ZONE 'UTC','YYYY-MM-DD"T"HH24:MI:SS.US"Z"') AS finish,
   count(*) FILTER (WHERE t>=$2::timestamptz AND t<$3::timestamptz)::text AS n
   FROM unnest(ARRAY[$2::timestamptz-interval '1 microsecond',$2::timestamptz,$3::timestamptz-interval '1 microsecond',$3::timestamptz]) t`,[day,w.startInclusive,w.endExclusive]);
  assert.equal(r.rows[0].start,w.startInclusive);assert.equal(r.rows[0].finish,w.endExclusive);assert.equal(r.rows[0].n,'2');
 }
});
test('HTTP-PG int8 queue baseline read preserves max storage precision without Number coercion',async t=>{
 const v=await venue(t),sid=randomUUID(),entity=randomUUID(),p=payload(entity);
 await v.db.query('BEGIN');
 try{
  await v.db.query(`INSERT INTO ${v.q}.submissions(id,author_id,target_kind,catalogue_uuid,expected_revision,current_revision) VALUES($1,$2,'work',$3,$4,1)`,[sid,v.author.principalId,entity,'9223372036854775807']);
  // Same initial revision insertion as native service, preserving all schema guards.
  await v.db.query(`INSERT INTO ${v.q}.submission_revisions(submission_id,revision,payload,prev_hash) VALUES($1,1,$2,repeat('0',64))`,[sid,p]);
  await v.db.query('COMMIT');
 }catch(e){await v.db.query('ROLLBACK');throw e;}
 const r=await get(v,'/v1/admin/queue?q='+sid);equalStatus(r,200);assert.equal(r.body.items[0].expectedEntityRevision,'9223372036854775807');
});
test('HTTP-PG receipt expiry boundaries, live authorization before replay and principal key isolation',async t=>{
 const v=await venue(t),s=await v.create({open:true}),b=intent({decision:'reject',reason:'Synthetic bounded rejection'});
 const first=await v.send(v.admin,'POST',s.path+'/decisions',b);equalStatus(first,200);
 equalStatus(await v.send(v.other,'POST',s.path+'/decisions',b),403);
 v.advance(86400000-1);const renewed=await v.signIn('admin');
 const prior=await snapshot(v);const replay=await v.send(renewed,'POST',s.path+'/decisions',b);equalStatus(replay,200);assert.equal(replay.raw,first.raw);assert.deepEqual(await snapshot(v),prior);
 v.advance(1);equalStatus(await v.send(renewed,'POST',s.path+'/decisions',b),409);assert.deepEqual(await snapshot(v),prior);
 v.advance(1);equalStatus(await v.send(renewed,'POST',s.path+'/decisions',b),409);
 // Revoke through a separately granted administrator; no grant invented by HTTP.
 const otherAdmin=await v.signIn('other-admin','administrator');
 await v.adapter().authorization.changeRole(otherAdmin.accessToken,{principalId:v.admin.principalId,role:'administrator'},true);
 equalStatus(await v.send(renewed,'POST',s.path+'/decisions',b),403);
});
test('HTTP-PG audit cursor binds all filters and original highwater even with transition substring search',async t=>{
 const v=await venue(t);for(let i=0;i<3;i++)await v.create({open:true});
 const first=await get(v,'/v1/admin/audit?q=-%3E&limit=1');equalStatus(first,200);assert.ok(first.body.nextCursor);
 const next=await get(v,'/v1/admin/audit?q=-%3E&limit=1&cursor='+first.body.nextCursor);equalStatus(next,200);assert.equal(next.body.highwater,first.body.highwater);
 equalStatus(await get(v,'/v1/admin/audit?cursor='+first.body.nextCursor),400);
 equalStatus(await get(v,'/v1/admin/audit?q=-%3E&actorId='+v.admin.principalId+'&cursor='+first.body.nextCursor),400);
});
test('HTTP-PG default-deny policy prevents publication and private detail disclosure without mutation',async t=>{
 const v=await venue(t),s=await v.create({open:true});await v.fresh({validator:undefined});const before=await snapshot(v);
 equalStatus(await get(v,s.path),503);equalStatus(await decide(v,s),400);assert.deepEqual(await snapshot(v),before);
 equalStatus(await get(v,'/v1/admin/queue'),200);
});
test('HTTP-PG revoked moderator cannot replay an earlier valid decision receipt',async t=>{
 const v=await venue(t),s=await v.create({open:true}),b=intent({decision:'reject',reason:'Synthetic decision'});
 equalStatus(await v.send(v.mod,'POST',s.path+'/decisions',b),200);
 await v.adapter().authorization.changeRole(v.admin.accessToken,{principalId:v.mod.principalId,role:'moderator'},true);
 const before=await snapshot(v);equalStatus(await v.send(v.mod,'POST',s.path+'/decisions',b),403);assert.deepEqual(await snapshot(v),before);
});
