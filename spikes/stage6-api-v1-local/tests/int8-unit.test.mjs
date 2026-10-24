// Scripted boundary coverage where a real submission chain would require >2^53
// immutable predecessor rows. Real PG chain tests keep every constraint enabled.
import {test} from 'node:test';
import assert from 'node:assert/strict';
import {randomUUID} from 'node:crypto';
import {revision,incrementRevision} from '../../stage6-handlers-local/src/validation.mjs';
import {SubmissionService} from '../../stage6-handlers-local/src/submissions.mjs';
const MAX='9223372036854775807';

test('INT8 unit: entity nonnegative and submission positive bounds reject unsafe coercion',()=>{
 for(const value of [0,'0',1,'1','9007199254740993',MAX])assert.equal(revision(value),value);
 for(const value of [0,'0'])assert.throws(()=>revision(value,{positive:true}),e=>e.status===400);
 for(const value of [-1,'-1',Number.MAX_SAFE_INTEGER+1,1.5,null,true,1n,'01','+1','1.0','1e2',' 1','9223372036854775808'])assert.throws(()=>revision(value),e=>e.status===400);
 assert.equal(revision(MAX,{positive:true}),MAX);
});

test('INT8 unit: shared checked increment crosses exact odd values and reaches int8 maximum',()=>{
 for(const [a,b] of [['0','1'],['9007199254740991','9007199254740992'],['9007199254740993','9007199254740994'],['9223372036854775806',MAX]])assert.equal(incrementRevision(a),b);
 assert.throws(()=>incrementRevision(MAX),e=>e.status===409&&e.code==='conflict');
});

function service(current){
 const id=randomUUID(),actor=randomUUID(),target={kind:'work',catalogueUuid:randomUUID()},queries=[];
 const c={query:async(sql,args)=>{
  queries.push({sql,args});
  if(sql.startsWith('SELECT * FROM'))return {rows:[{id,author_id:actor,status:'changes_requested',current_revision:current}]};
  if(sql.startsWith('SELECT payload'))return {rows:[{payload:{target},this_hash:'1'.repeat(64)}]};
  if(sql.includes('.submission_revisions('))return {rows:[{this_hash:'2'.repeat(64)}]};
  if(sql.includes('.append_audit('))return {rows:[{seq:'1'}]};
  return {rows:[]};
 }};
 const s=new SubmissionService({authorization:{q:'"proto_scripted_int8"',require:async(_token,_cap,fn)=>fn(c,{principalId:actor})},
  idempotency:{run:async(_c,_p,_op,_body,fn)=>fn()},validator:{payload:()=>{}}});
 return {s,queries,body:{schemaVersion:1,id,payload:{target},expectedRevision:MAX,expectedSubmissionRevision:current,idempotencyKey:randomUUID()}};
}

test('INT8 scripted submission service: above-safe revision persisted and audited as exact decimal strings',async()=>{
 const {s,queries,body}=service('9007199254740993');
 const r=await s.revise('synthetic',body);assert.equal(r.body.revision,'9007199254740994');
 assert.equal(queries.find(q=>q.sql.includes('.submission_revisions(')).args[1],r.body.revision);
 assert.equal(queries.find(q=>q.sql.includes('.append_audit(')).args[3],r.body.revision);
 assert.equal(queries.find(q=>q.sql.includes('.review_events(')).args[2],r.body.revision);
 assert.equal(queries.find(q=>q.sql.includes('SET status=')).args[2],r.body.revision);
});

test('INT8 scripted submission service: maximum revision conflicts before any write',async()=>{
 const {s,queries,body}=service(MAX);
 await assert.rejects(s.revise('synthetic',body),e=>e.status===409);
 assert.ok(queries.every(q=>q.sql.startsWith('SELECT')));
});
