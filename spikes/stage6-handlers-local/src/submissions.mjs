import {randomUUID} from 'node:crypto';
import {notFound,invalid,uuid,allowObject} from '../../stage6-auth-local/src/errors.mjs';
import {bounded,version,revision,incrementRevision,text} from './validation.mjs';
import {conflict,canonical} from './idempotency.mjs';
import {audit,transition} from './events.mjs';
export class SubmissionService {
 constructor({authorization,idempotency,validator}){Object.assign(this,{authorization,idempotency,validator,q:authorization.q});}
 async create(token,body,requestId=randomUUID()){
  bounded(body);body=structuredClone(body);version(body,['schemaVersion','payload','expectedRevision','idempotencyKey']);this.validator.payload(body.payload);revision(body.expectedRevision);
  return this.authorization.require(token,'submit',(c,ctx)=>this.idempotency.run(c,ctx.principalId,'submission.create',body,async()=>{
   const p=body.payload,t=p.target,id=randomUUID();let mapping=null;
   if(t.externalIdentity){const e=t.externalIdentity;
    // Namespace uniqueness preserves unresolved identity; never infer a merge from a UUID supplied alongside it.
    const r=await c.query(`INSERT INTO ${this.q}.identity_mappings(id,provider,provider_entity_type,external_id,state) VALUES($1,$2,$3,$4,'needs_review') ON CONFLICT(provider,provider_entity_type,external_id) DO UPDATE SET external_id=EXCLUDED.external_id RETURNING id,state,catalogue_uuid`,[randomUUID(),e.provider,e.providerKind,e.externalId]);
    mapping=r.rows[0];if(mapping.state==='resolved'&&mapping.catalogue_uuid!==t.catalogueUuid)throw conflict();
   }
   await c.query(`INSERT INTO ${this.q}.submissions(id,author_id,target_kind,catalogue_uuid,mapping_id,expected_revision,current_revision) VALUES($1,$2,$3,$4,$5,$6,1)`,[id,ctx.principalId,t.kind,t.catalogueUuid??null,mapping?.id??null,String(body.expectedRevision)]);
   const {rows:[snap]}=await c.query(`INSERT INTO ${this.q}.submission_revisions(submission_id,revision,payload,prev_hash) VALUES($1,1,$2,repeat('0',64)) RETURNING this_hash`,[id,p]);
   await audit(c,this.q,{actor:ctx.principalId,id,revision:1,requestId,action:'submission.created',after:snap.this_hash});
   return {status:201,body:{requestId,id,revision:1,status:'submitted',identityState:mapping?.state??'catalogue'}};
  }));
 }
 async locked(c,ctx,id){
  const {rows:[s]}=await c.query(`SELECT * FROM ${this.q}.submissions WHERE id=$1 AND author_id=$2 FOR UPDATE`,[id,ctx.principalId]);if(!s)throw notFound();return s;
 }
 async revise(token,body,requestId=randomUUID()){
  bounded(body);body=structuredClone(body);version(body,['schemaVersion','id','payload','expectedRevision','expectedSubmissionRevision','idempotencyKey']);uuid(body.id);revision(body.expectedRevision);revision(body.expectedSubmissionRevision,{positive:true});this.validator.payload(body.payload);
  return this.authorization.require(token,'submission.revise.own',(c,ctx)=>this.idempotency.run(c,ctx.principalId,'submission.revise',body,async()=>{
   const s=await this.locked(c,ctx,body.id);
   if(s.status!=='changes_requested'||s.current_revision!==String(body.expectedSubmissionRevision))throw conflict();
   const {rows:[prior]}=await c.query(`SELECT payload,this_hash FROM ${this.q}.submission_revisions WHERE submission_id=$1 AND revision=$2`,[s.id,s.current_revision]);
   if(canonical(prior.payload.target)!==canonical(body.payload.target))throw invalid();
   // Checked full-range int8 increment: BigInt arithmetic only; overflow beyond
   // signed int8 maximum is a conflict, never a rounded Number.
   const next=incrementRevision(s.current_revision);
   const {rows:[snap]}=await c.query(`INSERT INTO ${this.q}.submission_revisions(submission_id,revision,payload,prev_hash) VALUES($1,$2,$3,$4) RETURNING this_hash`,[s.id,next,body.payload,prior.this_hash]);
   await c.query(`UPDATE ${this.q}.submissions SET expected_revision=$2 WHERE id=$1`,[s.id,body.expectedRevision]);
   await transition(c,this.q,{actor:ctx.principalId,id:s.id,revision:next,from:s.status,to:'in_review',requestId,hash:snap.this_hash});
   return {status:201,body:{requestId,id:s.id,revision:next,status:'in_review'}};
  }));
 }
 async withdraw(token,body,requestId=randomUUID()){
  bounded(body);body=structuredClone(body);version(body,['schemaVersion','id','expectedRevision','expectedSubmissionRevision','reason','idempotencyKey']);uuid(body.id);revision(body.expectedRevision);revision(body.expectedSubmissionRevision,{positive:true});if(body.reason!==undefined)text(body.reason);
  return this.authorization.require(token,'submission.withdraw.own',(c,ctx)=>this.idempotency.run(c,ctx.principalId,'submission.withdraw',body,async()=>{
   const s=await this.locked(c,ctx,body.id);if(!['submitted','in_review'].includes(s.status)||s.current_revision!==String(body.expectedSubmissionRevision)||s.expected_revision!==String(body.expectedRevision))throw conflict();
   await transition(c,this.q,{actor:ctx.principalId,id:s.id,revision:s.current_revision,from:s.status,to:'withdrawn',reason:body.reason,requestId});return {status:200,body:{requestId,id:s.id,status:'withdrawn'}};
  }));
 }
 async read(token,body){
  bounded(body);body=structuredClone(body);version(body,['schemaVersion','id']);uuid(body.id);
  // Existing guard derives current capabilities and applies ownership in SQL. Fetch stays in the same session transaction below.
  return this.authorization.sessions.withSession(token,async(c,ctx)=>{
   const {rows:[s]}=await c.query(`SELECT s.* FROM ${this.q}.submissions s WHERE s.id=$1 AND ((s.author_id=$2 AND EXISTS(SELECT 1 FROM ${this.q}.role_grants WHERE principal_id=$2 AND role='contributor' AND revoked_at IS NULL AND granted_at<=$3)) OR EXISTS(SELECT 1 FROM ${this.q}.role_grants WHERE principal_id=$2 AND role IN ('moderator','administrator') AND revoked_at IS NULL AND granted_at<=$3))`,[body.id,ctx.principalId,new Date(this.authorization.clock())]);
   if(!s)throw notFound();
   const revisions=(await c.query(`SELECT revision::text,payload,prev_hash,this_hash FROM ${this.q}.submission_revisions WHERE submission_id=$1 ORDER BY submission_revisions.revision`,[s.id])).rows;
   const reviews=(await c.query(`SELECT from_status,to_status,reason,audit_seq::text FROM ${this.q}.review_events WHERE submission_id=$1 ORDER BY audit_seq`,[s.id])).rows;
   return {status:200,body:{submission:s,revisions,reviews}};
  });
 }
 async list(token,body,queue=false){
  bounded(body);body=structuredClone(body);version(body,['schemaVersion','cursor']);let cursor=body.cursor??null;
  if(cursor){allowObject(cursor,['createdAt','id']);uuid(cursor.id);if(typeof cursor.createdAt!=='string'||!/^\d{4}-\d\d-\d\dT\d\d:\d\d:\d\d\.\d{6}Z$/.test(cursor.createdAt)||!Number.isFinite(Date.parse(cursor.createdAt)))throw invalid();}
  return this.authorization.require(token,queue?'queue.read':'submit',async(c,ctx)=>{
   const rows=(await c.query(`SELECT id,target_kind,status,current_revision::text,to_char(created_at AT TIME ZONE 'UTC','YYYY-MM-DD"T"HH24:MI:SS.US"Z"') AS "createdAt" FROM ${this.q}.submissions WHERE ${queue?"status IN ('submitted','in_review','changes_requested')":'author_id=$1'} ${queue?'AND $1::uuid IS NOT NULL':''} AND ($2::timestamptz IS NULL OR (created_at,id)>($2::timestamptz,$3::uuid)) ORDER BY created_at,id LIMIT 21`,[ctx.principalId,cursor?.createdAt??null,cursor?.id??null])).rows;
   const more=rows.length>20,items=rows.slice(0,20),last=items.at(-1);return {status:200,body:{items,nextCursor:more?{createdAt:last.createdAt,id:last.id}:null}};
  });
 }
}
