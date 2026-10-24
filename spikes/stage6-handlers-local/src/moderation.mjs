import {randomUUID} from 'node:crypto';
import {forbidden,notFound,invalid,uuid} from '../../stage6-auth-local/src/errors.mjs';
import {bounded,version,revision,incrementRevision,text} from './validation.mjs';
import {conflict} from './idempotency.mjs';
import {transition} from './events.mjs';
export class ModerationService {
 constructor({authorization,idempotency,validator,onBoundary=async()=>{}}){Object.assign(this,{authorization,idempotency,validator,onBoundary,q:authorization.q});}
 async act(token,operation,body,requestId=randomUUID()){
  bounded(body);body=structuredClone(body);version(body,['schemaVersion','id','expectedRevision','expectedSubmissionRevision','idempotencyKey',...(operation==='moderation.decide'?['decision','reason']:[])]);
  uuid(body.id);revision(body.expectedRevision);revision(body.expectedSubmissionRevision,{positive:true});
  if(operation==='moderation.decide'){
   if(!['approve','reject','request_changes'].includes(body.decision))throw invalid();
   if(body.reason!==undefined)text(body.reason);if(body.decision!=='approve'&&!body.reason)throw invalid();
  }else if(operation!=='moderation.open')throw invalid();
  return this.authorization.require(token,operation==='moderation.open'?'queue.read':'decide',(c,ctx)=>this.idempotency.run(c,ctx.principalId,operation,body,async()=>{
   const {rows:[s]}=await c.query(`SELECT * FROM ${this.q}.submissions WHERE id=$1 FOR UPDATE`,[body.id]);
   if(!s)throw notFound();if(s.author_id===ctx.principalId)throw forbidden();
   if(s.current_revision!==String(body.expectedSubmissionRevision)||s.expected_revision!==String(body.expectedRevision))throw conflict();
   if(operation==='moderation.open'){
    if(s.status!=='submitted')throw conflict();
    await transition(c,this.q,{actor:ctx.principalId,id:s.id,revision:s.current_revision,from:s.status,to:'in_review',requestId});
    return {status:200,body:{requestId,id:s.id,status:'in_review'}};
   }
   if(s.status!=='in_review')throw conflict();
   const {rows:[snap]}=await c.query(`SELECT payload,this_hash FROM ${this.q}.submission_revisions WHERE submission_id=$1 AND revision=$2`,[s.id,s.current_revision]);
   if(body.decision!=='approve'){
    const to=body.decision==='reject'?'rejected':'changes_requested';
    await transition(c,this.q,{actor:ctx.principalId,id:s.id,revision:s.current_revision,from:s.status,to,reason:body.reason,requestId,hash:snap.this_hash});
    return {status:200,body:{requestId,id:s.id,status:to}};
   }
   this.validator.payload(snap.payload);
   if(!s.catalogue_uuid)throw conflict();
   if(s.mapping_id){const {rows:[m]}=await c.query(`SELECT state,catalogue_uuid FROM ${this.q}.identity_mappings WHERE id=$1 FOR SHARE`,[s.mapping_id]);if(!m||m.state!=='resolved'||m.catalogue_uuid!==s.catalogue_uuid)throw conflict();}
   const entity=s.catalogue_uuid;
   // Advisory lock serializes absent rows too; every publisher takes it before the entity row/head.
   await c.query('SELECT pg_advisory_xact_lock(hashtextextended($1,0))',[`entity:${this.idempotency.schema}:${entity}`]);
   const {rows:[current]}=await c.query(`SELECT * FROM ${this.q}.published_entities WHERE id=$1 FOR UPDATE`,[entity]);
   if((current?.revision??'0')!==String(body.expectedRevision)||(current&&(current.kind!==s.target_kind||current.status!=='published')))throw conflict();
   const maximum=(await c.query(`SELECT coalesce(max(revision),0)::text AS n FROM ${this.q}.published_revisions WHERE entity_id=$1`,[entity])).rows[0].n;
   // Checked full-range int8 increment: BigInt arithmetic only, decimal-string
   // persistence; overflow beyond signed int8 maximum is a conflict, never rounding.
   const nextRevision=incrementRevision(maximum);
   await this.onBoundary('locked',c);
   if(!current)await c.query(`INSERT INTO ${this.q}.published_entities(id,kind,revision,status) VALUES($1,$2,$3,'published')`,[entity,s.target_kind,nextRevision]);
   await this.onBoundary('entity',c);
   await c.query(`INSERT INTO ${this.q}.published_revisions(entity_id,revision,submission_id,submission_revision,actor_id,payload,source_evidence) VALUES($1,$2,$3,$4,$5,$6,jsonb_path_query_array($6::jsonb,'$.fields[*].evidence[*]'))`,[entity,nextRevision,s.id,s.current_revision,ctx.principalId,snap.payload]);
   await this.onBoundary('snapshot',c);
   await c.query(`UPDATE ${this.q}.published_entities SET revision=$2 WHERE id=$1`,[entity,nextRevision]);
   await this.onBoundary('projection',c);
   await transition(c,this.q,{actor:ctx.principalId,id:s.id,revision:s.current_revision,from:'in_review',to:'approved',reason:body.reason,requestId,hash:snap.this_hash});
   await this.onBoundary('approved',c);
   const seq=await transition(c,this.q,{actor:ctx.principalId,id:s.id,revision:s.current_revision,from:'approved',to:'published',requestId,hash:snap.this_hash});
   await this.onBoundary('audit',c);
   await c.query(`INSERT INTO ${this.q}.change_feed(entity_id,revision,status,audit_seq) VALUES($1,$2,'published',$3)`,[entity,nextRevision,seq]);
   await this.onBoundary('feed',c);
   return {status:201,body:{requestId,id:s.id,status:'published',entityId:entity,revision:nextRevision}};
  }));
 }
}
