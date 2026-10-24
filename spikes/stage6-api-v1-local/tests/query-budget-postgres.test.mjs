// EXPLAIN actual bounded read queries on disposable, constraint-valid synthetic data.
import {test} from 'node:test';
import assert from 'node:assert/strict';
import {writeFileSync} from 'node:fs';
import {venue,get,payload} from './pg-http-venue.mjs';
import {randomUUID} from 'node:crypto';

test('QUERY-PG measured5000-row queue/audit/stats plans preserve caps and timeout without speculative indexes',async t=>{
 const v=await venue(t),n=5000,p=payload(randomUUID());
 await v.db.query('BEGIN');try{
  await v.db.query(`INSERT INTO ${v.q}.submissions(id,author_id,target_kind,catalogue_uuid,expected_revision,current_revision) SELECT id,$1,'work',id,0,1 FROM (SELECT gen_random_uuid() AS id FROM generate_series(1,$2)) x`,[v.author.principalId,n]);
  await v.db.query(`INSERT INTO ${v.q}.submission_revisions(submission_id,revision,payload,prev_hash) SELECT id,1,jsonb_set($1::jsonb,'{target,catalogueUuid}',to_jsonb(id::text)),repeat('0',64) FROM ${v.q}.submissions`,[p]);
  await v.db.query(`SELECT ${v.q}.append_audit($1,'submission.created','submission',s.id,1,gen_random_uuid(),NULL,NULL,r.this_hash) FROM ${v.q}.submissions s JOIN ${v.q}.submission_revisions r ON r.submission_id=s.id`,[v.admin.principalId]);
  await v.db.query('COMMIT');
 }catch(e){await v.db.query('ROLLBACK');throw e;}
 for(const name of ['submissions','submission_revisions','audit_events'])await v.db.query(`ANALYZE ${v.q}.${name}`);
 const plans=[];let label;
 v.setFault(async(sql,args,_c,query)=>{
  if(sql.includes('SELECT s.id,s.author_id')||sql.startsWith('SELECT seq::text,')||sql.includes('(SELECT last_seq::text FROM')){
   assert.equal((await query('SHOW statement_timeout')).rows[0].statement_timeout,'5s');
   const start=process.hrtime.bigint(),plan=await query('EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON) '+sql,args);
   plans.push({label,sql,args,statementTimeout:'5s',datasetSubmissions:n,datasetCreatedAudits:n,wallMs:Number(process.hrtime.bigint()-start)/1e6,explain:plan.rows[0]['QUERY PLAN']});
  }
 });
 label='queue-default';const first=await get(v,'/v1/admin/queue?limit=20');assert.equal(first.status,200);assert.equal(first.body.items.length,20);assert.ok(first.body.nextCursor);
 label='queue-keyset';assert.equal((await get(v,'/v1/admin/queue?limit=20&cursor='+first.body.nextCursor)).body.items.length,20);
 label='queue-filtered';assert.equal((await get(v,'/v1/admin/queue?kind=work&status=submitted&q=WoRk&limit=20')).body.items.length,20);
 label='audit-filtered';const audit=await get(v,'/v1/admin/audit?action=submission.created&q=submission&limit=20');assert.equal(audit.body.items.length,20);
 label='stats-exact';const stats=await get(v,'/v1/admin/stats');assert.equal(stats.body.pending,String(n));assert.equal(stats.body.state,'exact');
 v.setFault(null);
 function nodes(p){return [p,...(p.Plans??[]).flatMap(nodes)];}
 for(const p of plans.filter(x=>x.label.startsWith('queue'))){
  const revisionScans=nodes(p.explain[0].Plan).filter(n=>n['Relation Name']==='submission_revisions');
  assert.ok(revisionScans.length>0);assert.ok(revisionScans.every(n=>n['Actual Rows']*n['Actual Loops']<=21),'bounded page must not scan5000 revision payloads');
 }
 assert.deepEqual(plans.map(x=>x.label),['queue-default','queue-keyset','queue-filtered','audit-filtered','stats-exact']);
 if(process.env.QUERY_PLAN_CAPTURES)writeFileSync(process.env.QUERY_PLAN_CAPTURES,JSON.stringify({syntheticOnly:true,indexChanges:false,datasetSubmissions:n,plans},null,2)+'\n');
});
