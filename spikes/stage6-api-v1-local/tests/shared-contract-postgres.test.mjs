// Maintained canonical fixtures produced by actual localhost HTTP → PostgreSQL.
import {execFileSync} from 'node:child_process';
import {test,after} from 'node:test';
import assert from 'node:assert/strict';
import {randomUUID,createHash} from 'node:crypto';
import {request as httpRequest} from 'node:http';
import {readFileSync,writeFileSync} from 'node:fs';
import {venue,get,decide,intent,payload,snapshot} from './pg-http-venue.mjs';
import {ContractConsumer} from '../../../contracts/admin/shared/v1/consumer-reference.mjs';
import {pragueWindow} from '../src/statistics.mjs';
const definitions=JSON.parse(readFileSync(new URL('../../../contracts/admin/shared/v1/scenarios.json',import.meta.url)));
const specs=new Map(definitions.cases.map(x=>[x.id,x])),captures=[],transportFailures=[];
const scrub=value=>JSON.parse(JSON.stringify(value,(key,v)=>['accessToken','refreshToken','credential','challenge','nonce','authorization'].includes(key)?'SYNTHETIC_REDACTED_TOKEN':v));
async function record(v,id,u,method,path,body,headers={}){
 const spec=specs.get(id);assert.ok(spec,'unregistered fixture '+id);assert.ok(!captures.some(x=>x.id===id),'duplicate fixture '+id);
 const r=await fetch(v.url()+path,{method,headers:{...(u?{authorization:'Bearer '+u.accessToken}:{}),...(body===undefined?{}:{'content-type':'application/json'}),...headers},...(body===undefined?{}:{body:JSON.stringify(body)})});
 const raw=await r.text(),value=raw?JSON.parse(raw):null;
 assert.equal(r.status,spec.status,id+': '+raw);assert.equal(r.headers.get('cache-control'),'no-store');
 assert.equal(r.headers.get('x-request-id'),value?.requestId??value?.error?.requestId??r.headers.get('x-request-id'));
 execFileSync('python3',[new URL('../verify-shared-contract.py',import.meta.url).pathname,'--response',spec.dto,String(r.status)],{input:JSON.stringify(value)});
 captures.push({id,origin:'actual-loopback-http-postgresql',method,path,request:scrub(body??null),status:r.status,dto:spec.dto,
  response:scrub(value),redaction:'synthetic token fields scrubbed after raw DTO validation; other fields unchanged',rawSha256:createHash('sha256').update(raw).digest('hex'),bodyBytes:Buffer.byteLength(raw)});
 return {status:r.status,body:value,raw,headers:r.headers};
}
after(()=>{if(process.env.SHARED_CONTRACT_CAPTURES)writeFileSync(process.env.SHARED_CONTRACT_CAPTURES,JSON.stringify({packageVersion:1,captures,transportFailures},null,2)+'\n');});
const r=(v,id,u,path)=>record(v,id,u,'GET',path);
async function dropped(v,u,path,body){
 return new Promise((resolve,reject)=>{
  const data=JSON.stringify(body),req=httpRequest(v.url()+path,{method:'POST',headers:{'content-type':'application/json',...(u?{authorization:'Bearer '+u.accessToken}:{}),'content-length':Buffer.byteLength(data)}},res=>{
   transportFailures.push({path,status:res.statusCode,mechanism:'actual client response stream destroyed after HTTP headers, before consuming body'});
   res.destroy();resolve(res.statusCode);
  });req.on('error',reject);req.end(data);
 });
}

test('SHARED-PG session challenge, identity/session binding, access expiry and absolute family expiry',async t=>{
 const v=await venue(t),ch=await record(v,'session.challenge',null,'POST','/v1/session/challenge',{schemaVersion:1});
 const login=await record(v,'session.login',null,'POST','/v1/session',{schemaVersion:1,challenge:ch.body.challenge,credential:JSON.stringify({issuer:'https://synthetic.invalid',audience:'local-test',subject:'shared-login',nonce:ch.body.nonce,iat:v.clock(),exp:v.clock()+120000})});
 const u=login.body,s=await r(v,'session.binding',u,'/v1/session');assert.equal(s.body.principalId,u.principalId);assert.equal(s.body.sessionId,u.sessionId);
 await r(v,'session.capabilities',v.admin,'/v1/me/capabilities');
 v.advance(900000);await r(v,'session.access-expired',u,'/v1/session');
 v.advance(604800000);await record(v,'session.family-expired',null,'POST','/v1/session/refresh',{schemaVersion:1,refreshToken:u.refreshToken});
});
test('SHARED-PG refresh rotation, exact account binding, replay revokes the entire family',async t=>{
 const v=await venue(t),u=await v.signIn('refresh-fixture');
 const next=await record(v,'session.refresh',null,'POST','/v1/session/refresh',{schemaVersion:1,refreshToken:u.refreshToken});
 assert.equal(next.body.principalId,u.principalId);assert.notEqual(next.body.sessionId,u.sessionId);
 await r(v,'session.old-access-revoked',u,'/v1/session');
 await record(v,'session.refresh-replay',null,'POST','/v1/session/refresh',{schemaVersion:1,refreshToken:u.refreshToken});
 await r(v,'session.family-revoked',next.body,'/v1/session');await r(v,'session.other-account',v.other,'/v1/session');
});
test('SHARED-PG reference single-flight refresh and stale account response rejection',async t=>{
 const v=await venue(t);let refreshCalls=0;
 const c=new ContractConsumer(async(u,m,p,b)=>{if(p.endsWith('/refresh'))refreshCalls++;return v.send(u,m,p,b);});c.authenticate(v.admin);
 const sub=await v.create({open:true}),oldReview=await c.review(sub.id);
 const [a,b]=await Promise.all([c.refresh(),c.refresh()]);assert.equal(refreshCalls,1);assert.equal(a.sessionId,b.sessionId);await c.session();
 await assert.rejects(c.confirm(oldReview,'approve',undefined,{acknowledged:true}),e=>e.code==='explicit_reconfirmation_required');
 let release;const gate=new Promise(resolve=>release=resolve);
 const slow=new ContractConsumer(async(u,m,p,b)=>{const response=await v.send(u,m,p,b);await gate;return response;});slow.authenticate(v.other);
 const pending=slow.session();slow.authenticate(v.author);release();await assert.rejects(pending,e=>e.code==='account_changed');assert.equal(slow.tokens.principalId,v.author.principalId);
});
test('SHARED-PG actual lost refresh response requires fresh authentication; never resubmit consumed refresh',async t=>{
 const v=await venue(t),u=await v.signIn('lost-refresh');
 const c=new ContractConsumer(async(_u,_m,path,b)=>{assert.equal(await dropped(v,null,path,b),200);throw Error('response lost');});c.authenticate(u);
 await assert.rejects(c.refresh());assert.equal(c.tokens,null);
 await record(v,'session.lost-refresh-replay',null,'POST','/v1/session/refresh',{schemaVersion:1,refreshToken:u.refreshToken});
 await r(v,'session.lost-refresh-old-access',u,'/v1/session');
 const fresh=await v.signIn('lost-refresh');assert.equal(fresh.principalId,u.principalId);assert.equal((await get(v,'/v1/session',fresh)).status,200);
});
test('SHARED-PG logout204, repeated logout, revoked access and immediate offline local clearing',async t=>{
 const v=await venue(t);const out=await record(v,'session.logout',v.admin,'DELETE','/v1/session');assert.equal(out.raw,'');assert.equal(out.headers.get('content-type'),null);
 await record(v,'session.logout-repeat',v.admin,'DELETE','/v1/session');await r(v,'session.after-logout',v.admin,'/v1/session');
 let release;const gate=new Promise(resolve=>release=resolve);
 const c=new ContractConsumer(async()=>{await gate;throw Error('offline');});c.authenticate(v.other);const pending=c.logout();assert.equal(c.tokens,null);assert.equal(c.pending,null);release();assert.deepEqual(await pending,{locallyCleared:true,revocationConfirmed:false});
});
test('SHARED-PG role revocation, suspended principals and cross-role denial',async t=>{
 const v=await venue(t);await r(v,'isolation.contributor-queue',v.other,'/v1/admin/queue');
 await v.db.query(`UPDATE ${v.q}.role_grants SET revoked_at=clock_timestamp() WHERE principal_id=$1`,[v.mod.principalId]);await r(v,'isolation.revoked-role',v.mod,'/v1/admin/queue');
 await v.db.query(`UPDATE ${v.q}.principals SET state='suspended' WHERE id=$1`,[v.admin.principalId]);await r(v,'isolation.suspended',v.admin,'/v1/session');
});
test('SHARED-PG canonical open/detail/compare/approve and exact receipt replay within24h',async t=>{
 const v=await venue(t),s=await v.create();await record(v,'moderation.open',v.admin,'POST',s.path+'/open',intent());
 await r(v,'moderation.detail',v.admin,s.path);const absent=await r(v,'comparison.absent',v.admin,'/v1/admin/entities/'+s.entity+'/comparison?submissionId='+s.id);assert.equal(absent.body.reason,'absent');
 const b=intent({decision:'approve'}),first=await record(v,'moderation.approve',v.admin,'POST',s.path+'/decisions',b),before=await snapshot(v);
 await v.fresh();const replay=await record(v,'moderation.replay',v.admin,'POST',s.path+'/decisions',b);assert.equal(first.raw,replay.raw);assert.deepEqual(await snapshot(v),before);
 await record(v,'moderation.mismatch',v.admin,'POST',s.path+'/decisions',{...b,reason:'Changed intent'});
 v.advance(86400000);const fresh=await v.signIn('admin');await record(v,'moderation.expired-receipt',fresh,'POST',s.path+'/decisions',b);
});
test('SHARED-PG reject/request_changes typed200 results and explicit legacy-body rejection',async t=>{
 const v=await venue(t),s=await v.create({open:true}),other=await v.create({open:true});
 await record(v,'moderation.reject',v.admin,'POST',s.path+'/decisions',intent({decision:'reject',reason:'Synthetic rejection'}));
 await record(v,'moderation.request-changes',v.admin,'POST',other.path+'/decisions',intent({decision:'request_changes',reason:'Synthetic clarification'}));
 await record(v,'wire.legacy-decision',v.admin,'POST',other.path+'/decisions',{schemaVersion:1,decision:'approve',expectedRevision:1,idempotencyKey:randomUUID()});
});
test('SHARED-PG generic409 requires refetch and explicit acknowledged fresh review; no automatic decision retry',async t=>{
 const v=await venue(t),s=await v.create({open:true});let decisionCalls=0;
 const c=new ContractConsumer(async(u,m,p,b)=>{if(p.endsWith('/decisions'))decisionCalls++;return v.send(u,m,p,b);});c.authenticate(v.admin);
 const initial=await c.review(s.id);await assert.rejects(c.confirm(initial,'approve'),e=>e.code==='explicit_reconfirmation_required');assert.equal(decisionCalls,0);
 await decide(v,s,{decision:'request_changes',reason:'Revise'});
 await v.submissions.revise(v.author.accessToken,{schemaVersion:1,id:s.id,payload:payload(s.entity),expectedRevision:0,expectedSubmissionRevision:1,idempotencyKey:randomUUID()});
 await assert.rejects(c.confirm(initial,'approve',undefined,{acknowledged:true}),e=>e.code==='refetch_and_reconfirm');assert.equal(decisionCalls,0);
 await record(v,'moderation.conflict',v.admin,'POST',s.path+'/decisions',intent({decision:'approve'}));
 const detail=await r(v,'moderation.refetch',v.admin,s.path);assert.equal(detail.body.submissionRevision,'2');
 const fresh=await c.review(s.id);assert.equal((await c.confirm(fresh,'approve',undefined,{acknowledged:true})).status,201);assert.equal(decisionCalls,1);
});
test('SHARED-PG consumer409 after preflight is not silently retried; revoked queued intent is cancelled',async t=>{
 const v=await venue(t),s=await v.create({open:true});let decisions=0;
 const c=new ContractConsumer(async(u,m,p,b)=>{if(p.endsWith('/decisions')){decisions++;await decide(v,s,{decision:'reject',reason:'Concurrent moderator'},v.mod);}return v.send(u,m,p,b);});c.authenticate(v.admin);
 await assert.rejects(c.confirm(await c.review(s.id),'approve',undefined,{acknowledged:true}),e=>e.code==='refetch_and_reconfirm');assert.equal(decisions,1);assert.equal(c.pending,null);
 const other=await v.create({open:true}),d=new ContractConsumer((u,m,p,b)=>v.send(u,m,p,b));d.authenticate(v.admin);
 d.pending={epoch:d.epoch,id:other.id,body:intent({decision:'approve'})};await v.db.query(`UPDATE ${v.q}.role_grants SET revoked_at=clock_timestamp() WHERE principal_id=$1`,[v.admin.principalId]);
 await assert.rejects(d.retryPending(),e=>e.code==='capability_revoked');assert.equal(d.pending,null);assert.equal((await get(v,other.path,v.mod)).body.status,'in_review');
});
test('SHARED-PG concurrent moderator decisions serialize and full response loss reconciles same exact intent',async t=>{
 const v=await venue(t),s=await v.create({open:true});
 const race=await Promise.all([decide(v,s),decide(v,s,{},v.mod)]);assert.deepEqual(race.map(x=>x.status).sort(),[201,409]);
 const other=await v.create({open:true});let first=true;
 const c=new ContractConsumer(async(u,m,path,body)=>{
  if(path.endsWith('/decisions')){
   if(first){first=false;assert.equal(await dropped(v,u,path,body),201);throw Error('lost decision response');}
   return record(v,'moderation.lost-response-replay',u,m,path,body);
  }
  return v.send(u,m,path,body);
 });c.authenticate(v.admin);
 await assert.rejects(c.confirm(await c.review(other.id),'approve',undefined,{acknowledged:true}));assert.ok(c.pending);
 const original=JSON.stringify(c.pending.body);await c.refresh();assert.equal(JSON.stringify(c.pending.body),original);
 const before=await snapshot(v);assert.equal((await c.retryPending()).status,201);assert.equal(c.pending,null);assert.deepEqual(await snapshot(v),before);
 const stored=(await v.db.query(`SELECT response FROM ${v.q}.idempotency_results WHERE key=$1`,[JSON.parse(original).idempotencyKey])).rows[0];assert.equal(stored.response.status,'published');
});
test('SHARED-PG bounded queue filters, null metadata and signed scope-bound pagination',async t=>{
 const v=await venue(t);for(let i=0;i<4;i++)await v.create();
 const first=await r(v,'queue.first',v.admin,'/v1/admin/queue?limit=2'),cursor=first.body.nextCursor;
 assert.deepEqual(first.body.items[0].display,{title:null,composer:null});
 const second=await r(v,'queue.next',v.admin,'/v1/admin/queue?limit=2&cursor='+cursor);assert.equal(new Set([...first.body.items,...second.body.items].map(x=>x.submissionId)).size,4);
 await r(v,'queue.filtered',v.admin,'/v1/admin/queue?kind=work&status=submitted&q=WoRk&limit=1');
 await r(v,'queue.cursor-tamper',v.admin,'/v1/admin/queue?cursor='+cursor.slice(0,-4)+'AAAA');await r(v,'queue.cursor-account',v.mod,'/v1/admin/queue?cursor='+cursor);
 await r(v,'queue.cursor-filter',v.admin,'/v1/admin/queue?status=submitted&cursor='+cursor);await r(v,'queue.legacy-cursor',v.admin,'/v1/admin/queue?cursor=2030-01-01');
 v.advance(900000);const u=await v.signIn('admin');await r(v,'queue.cursor-expired',u,'/v1/admin/queue?cursor='+cursor);
});
test('SHARED-PG audit immutable highwater, chain verification and scoped cursors',async t=>{
 const v=await venue(t),s=await v.create({open:true});
 const first=await r(v,'audit.first',v.admin,'/v1/admin/audit?limit=2'),head=first.body.headHash,high=first.body.highwater;
 await decide(v,s);let page=first.body,events=[...page.items];
 while(page.nextCursor){const x=await get(v,'/v1/admin/audit?limit=2&cursor='+page.nextCursor);assert.equal(x.body.highwater,high);assert.equal(x.body.headHash,head);events.push(...x.body.items);page=x.body;}
 let prev='0'.repeat(64);for(const e of events){assert.equal(e.prevHash,prev);assert.equal(createHash('sha256').update(prev+':'+e.canonicalContent).digest('hex'),e.thisHash);prev=e.thisHash;}assert.equal(prev,head);
 await r(v,'audit.filtered',v.admin,'/v1/admin/audit?action=in_review-%3Eapproved&subjectId='+s.id);
 await r(v,'audit.cursor-scope',v.admin,'/v1/admin/queue?cursor='+first.body.nextCursor);
});
test('SHARED-PG coherent exact statistics during concurrent publication and Prague DST SQL parity',async t=>{
 const v=await venue(t),s=await v.create({open:true});const before=await r(v,'stats.before',v.admin,'/v1/admin/stats');let fired=false;
 v.setFault(async sql=>{if(!fired&&sql.includes('(SELECT last_seq::text')){fired=true;assert.equal((await decide(v,s,{},v.mod)).status,201);}});
 const during=await r(v,'stats.concurrent',v.admin,'/v1/admin/stats');assert.equal(during.body.auditHighwater,before.body.auditHighwater);assert.equal(during.body.pending,before.body.pending);v.setFault(null);
 const after=await r(v,'stats.after',v.admin,'/v1/admin/stats');assert.equal(after.body.approvedInWindow,'1');assert.equal(after.body.pending,'0');
 for(const day of ['2026-03-29','2026-10-25']){
  const w=pragueWindow(day),row=(await v.db.query(`SELECT extract(epoch FROM (($1::date+1)::timestamp AT TIME ZONE 'Europe/Prague')-($1::date::timestamp AT TIME ZONE 'Europe/Prague'))/3600 AS hours`,[day])).rows[0];assert.equal(Number(row.hours),w.elapsedHours);
 }
});
test('SHARED-PG source-policy default-deny and backend outage/timeout are honest unavailable states',async t=>{
 const v=await venue(t),s=await v.create({open:true});await decide(v,s);await v.fresh({validator:undefined});
 await r(v,'policy.detail-denied',v.admin,s.path);const cmp=await r(v,'policy.comparison-unavailable',v.admin,'/v1/admin/entities/'+s.entity+'/comparison?submissionId='+s.id);assert.equal(cmp.body.reason,'projection_unavailable');
 const before=await snapshot(v);v.setFault(async sql=>{if(sql.includes('SELECT s.id,s.author_id'))throw Error('private SQL must not leak');});
 const out=await r(v,'error.outage',v.admin,'/v1/admin/queue');assert.ok(!out.raw.includes('private SQL'));v.setFault(null);assert.deepEqual(await snapshot(v),before);
 v.setFault(async(sql,_args,_c,query)=>{if(sql.includes('SELECT s.id,s.author_id')){await query("SET LOCAL statement_timeout='1ms'");await query('SELECT pg_sleep(0.02)');}});
 await r(v,'error.timeout',v.admin,'/v1/admin/queue');v.setFault(null);await r(v,'queue.recovered',v.admin,'/v1/admin/queue');
 await r(v,'error.not-found',v.admin,'/v1/admin/submissions/'+randomUUID());
});
test('SHARED-PG exact wire boundaries, unknown fields, supported UTF8 media type and unsafe legacy representations',async t=>{
 const v=await venue(t),s=await v.create({open:true});
 for(const [id,extra] of [['wire.numeric',{expectedEntityRevision:1}],['wire.submission-zero',{expectedSubmissionRevision:'0'}],['wire.overflow',{expectedEntityRevision:'9223372036854775808'}],['wire.underflow',{expectedEntityRevision:'-1'}],['wire.unknown',{unexpected:true}]])await record(v,id,v.admin,'POST',s.path+'/decisions',intent({decision:'approve',...extra}));
 await record(v,'wire.utf8-media',null,'POST','/v1/session/challenge',{schemaVersion:1},{'content-type':'application/json; charset=utf-8'});
 await record(v,'wire.wrong-charset',null,'POST','/v1/session/challenge',{schemaVersion:1},{'content-type':'application/json; charset=utf-16'});
 await record(v,'wire.unknown-login',null,'POST','/v1/session',{schemaVersion:1,credential:'synthetic',challenge:'synthetic',principalId:v.admin.principalId});
 await r(v,'wire.unknown-query',v.admin,'/v1/admin/queue?legacyLimit=20');
});

test('SHARED-PG maximum int8 publication round trip stays exact and next increment conflicts',async t=>{
 const v=await venue(t),entity=randomUUID(),p=payload(entity),head='9223372036854775806';
 const attribution=await v.submissions.create(v.author.accessToken,{schemaVersion:1,payload:p,expectedRevision:0,idempotencyKey:randomUUID()});
 await v.db.query('BEGIN');try{
  await v.db.query(`INSERT INTO ${v.q}.published_entities(id,kind,revision,status) VALUES($1,'work',$2,'published')`,[entity,head]);
  await v.db.query(`INSERT INTO ${v.q}.published_revisions(entity_id,revision,submission_id,submission_revision,actor_id,payload,source_evidence) VALUES($1,$2,$3,1,$4,$5,jsonb_path_query_array($5::jsonb,'$.fields[*].evidence[*]'))`,[entity,head,attribution.body.id,v.mod.principalId,p]);
  await v.db.query('COMMIT');
 }catch(e){await v.db.query('ROLLBACK');throw e;}
 const s=await v.create({entity,expected:head,open:true});const pub=await record(v,'wire.int8-max-result',v.admin,'POST',s.path+'/decisions',intent({decision:'approve',expectedEntityRevision:head}));assert.equal(pub.body.publishedEntityRevision,'9223372036854775807');
 const read=await r(v,'wire.int8-max-read',v.admin,'/v1/admin/entities/'+entity+'/comparison?submissionId='+s.id);assert.equal(read.body.entityRevision,'9223372036854775807');
 const next=await v.create({entity,expected:'9223372036854775807',open:true}),before=await snapshot(v);
 await record(v,'wire.int8-max-overflow',v.admin,'POST',next.path+'/decisions',intent({decision:'approve',expectedEntityRevision:'9223372036854775807'}));assert.deepEqual(await snapshot(v),before);
});

test('SHARED-PG observed privilege loss invalidates acknowledgements even after a later role regrant',async t=>{
 const v=await venue(t),s=await v.create({open:true});let posts=0;
 const c=new ContractConsumer((u,m,p,b)=>{if(p.endsWith('/decisions'))posts++;return v.send(u,m,p,b);});c.authenticate(v.admin);const old=await c.review(s.id);
 await v.db.query(`UPDATE ${v.q}.role_grants SET revoked_at=clock_timestamp() WHERE principal_id=$1`,[v.admin.principalId]);
 await assert.rejects(c.checked('GET','/v1/admin/queue'),e=>e.code==='capability_revoked');
 await v.db.query(`INSERT INTO ${v.q}.role_grants(id,principal_id,role,granted_by,granted_at) VALUES($1,$2,'administrator',$2,$3)`,[randomUUID(),v.admin.principalId,new Date(v.clock())]);
 await assert.rejects(c.confirm(old,'approve',undefined,{acknowledged:true}),e=>e.code==='explicit_reconfirmation_required');assert.equal(posts,0);
 assert.equal((await c.confirm(await c.review(s.id),'approve',undefined,{acknowledged:true})).status,201);assert.equal(posts,1);
});
test('SHARED-PG reference refuses approval without verified existing current values or when recorded baseline needs rebase',async t=>{
 const v=await venue(t);let posts=0;const c=new ContractConsumer((u,m,p,b)=>{if(p.endsWith('/decisions'))posts++;return v.send(u,m,p,b);});c.authenticate(v.admin);
 const absent=await v.create({expected:'1',open:true});await assert.rejects(c.confirm(await c.review(absent.id),'approve',undefined,{acknowledged:true}),e=>e.code==='comparison_unavailable');assert.equal(posts,0);
 const published=await v.create({open:true});assert.equal((await decide(v,published)).status,201);
 const stale=await v.create({entity:published.entity,expected:'0',open:true});await assert.rejects(c.confirm(await c.review(stale.id),'approve',undefined,{acknowledged:true}),e=>e.code==='rebase_required');assert.equal(posts,0);assert.equal(c.pending,null);
});
test('SHARED-PG queue keyset is explicitly a live page walk, not a fabricated cross-request snapshot',async t=>{
 const v=await venue(t);for(let i=0;i<3;i++)await v.create();
 const first=await r(v,'queue.live-first',v.admin,'/v1/admin/queue?limit=1');assert.equal(first.body.coverage,'page');
 const later=await v.create();let page=first.body,ids=page.items.map(x=>x.submissionId);
 const next=await r(v,'queue.live-next',v.admin,'/v1/admin/queue?limit=1&cursor='+page.nextCursor);page=next.body;ids.push(...page.items.map(x=>x.submissionId));
 while(page.nextCursor){page=(await get(v,'/v1/admin/queue?limit=1&cursor='+page.nextCursor)).body;ids.push(...page.items.map(x=>x.submissionId));}
 assert.equal(new Set(ids).size,4);assert.ok(ids.includes(later.id));
});
