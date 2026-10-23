import {randomUUID} from 'node:crypto';
import {Authorization} from '../../stage6-auth-local/src/authorization.mjs';
import {uuid,invalid,notFound} from '../../stage6-auth-local/src/errors.mjs';
import {bounded,version,text} from '../../stage6-handlers-local/src/validation.mjs';
import {conflict} from '../../stage6-handlers-local/src/idempotency.mjs';
import {decimal} from './cursor.mjs';
export class AdminService {
 constructor({authorization,idempotency,cursors,onBoundary=async()=>{}}){Object.assign(this,{authorization,idempotency,cursors,onBoundary,q:authorization.q});}
 async entity(token,op,body,requestId=randomUUID()){
  bounded(body);body=structuredClone(body);version(body,['schemaVersion','id','expectedRevision','expectedEventSeq','targetRevision','reason','idempotencyKey']);uuid(body.id);decimal(body.expectedRevision,{zero:false});decimal(body.expectedEventSeq,{zero:false});text(body.reason);
  if(op==='admin.rollback')decimal(body.targetRevision,{zero:false});else if(op!=='admin.quarantine'||body.targetRevision!==undefined)throw invalid();
  return this.authorization.require(token,'quarantine',(c,ctx)=>this.idempotency.run(c,ctx.principalId,op,body,async()=>{
   await c.query('SELECT pg_advisory_xact_lock(hashtextextended($1,0))',[`entity:${this.idempotency.schema}:${body.id}`]);
   const {rows:[e]}=await c.query(`SELECT * FROM ${this.q}.published_entities WHERE id=$1 FOR UPDATE`,[body.id]);if(!e)throw notFound();
   const {rows:[latest]}=await c.query(`SELECT seq::text FROM ${this.q}.change_feed WHERE entity_id=$1 ORDER BY change_feed.seq DESC LIMIT 1`,[e.id]);
   if(e.revision!==body.expectedRevision||latest?.seq!==body.expectedEventSeq)throw conflict();
   await this.onBoundary('locked',c);
   const rollback=op==='admin.rollback';if(!rollback&&e.status!=='published')throw conflict();
   const target=rollback?body.targetRevision:e.revision;
   if(rollback&&BigInt(target)>=BigInt(e.revision))throw conflict();
   const {rows:[r]}=await c.query(`SELECT this_hash FROM ${this.q}.published_revisions WHERE entity_id=$1 AND revision=$2`,[e.id,target]);if(!r)throw conflict();
   const {rows:[before]}=await c.query(`SELECT this_hash FROM ${this.q}.published_revisions WHERE entity_id=$1 AND revision=$2`,[e.id,e.revision]);
   await c.query(`UPDATE ${this.q}.published_entities SET revision=$2,status=$3,reason=$4 WHERE id=$1`,[e.id,target,rollback?'published':'quarantined',body.reason]);
   await this.onBoundary('projection',c);
   const seq=(await c.query(`SELECT ${this.q}.append_audit($1,$2,'entity',$3,$4,$5,$6,$7,$8) AS seq`,[ctx.principalId,op,e.id,target,requestId,body.reason,before.this_hash,r.this_hash])).rows[0].seq;
   await this.onBoundary('audit',c);
   const feed=(await c.query(`INSERT INTO ${this.q}.change_feed(entity_id,revision,status,reason,audit_seq,event_kind) VALUES($1,$2,$3,$4,$5,$6) RETURNING seq::text`,[e.id,target,rollback?'published':'quarantined',body.reason,seq,rollback?'rollback':'quarantine'])).rows[0];
   await this.onBoundary('feed',c);
   return {status:200,body:{requestId,catalogueUuid:e.id,revision:target,status:rollback?'published':'quarantined',eventSequence:feed.seq}};
  }));
 }
 async principal(token,op,body,requestId=randomUUID()){
  bounded(body);body=structuredClone(body);version(body,['schemaVersion','principalId','role','revoke','suspended','idempotencyKey']);uuid(body.principalId);
  if(op==='admin.roles'){if(typeof body.revoke!=='boolean'||body.suspended!==undefined)throw invalid();}
  else if(op!=='admin.suspend'||typeof body.suspended!=='boolean'||body.role!==undefined||body.revoke!==undefined)throw invalid();
  // Existing Authorization methods retain all live checks, sorted principal locks, and audit semantics.
  // Insert idempotency within their existing withSession transaction, including their additional target locks.
  return this.authorization.require(token,'roles.manage',(c,ctx)=>this.idempotency.run(c,ctx.principalId,op,body,async()=>{
   const wrapped=Object.create(this.authorization.sessions);
   wrapped.withSession=(_access,fn)=>fn(c,ctx);
   const auth=new Authorization({sessions:wrapped,clock:this.authorization.clock});
   const result=op==='admin.roles'?await auth.changeRole(token,body,body.revoke,requestId):await auth.suspend(token,body,requestId);
   await this.onBoundary('principal',c);return {status:200,body:{requestId,...result}};
  }),{additionalPrincipals:[body.principalId]});
 }
 async audit(token,body){
  bounded(body);body=structuredClone(body);version(body,['schemaVersion','cursor']);const scope='admin.audit',cursor=body.cursor===undefined?null:this.cursors.read(body.cursor,scope);
  return this.authorization.require(token,'audit.read',async c=>{
   const head=(await c.query(`SELECT last_seq::text,last_hash FROM ${this.q}.audit_head WHERE singleton FOR SHARE`)).rows[0];
   const high=cursor?.high??head.last_seq,after=cursor?decimal(cursor.after):'0';
   const rows=(await c.query(`SELECT seq::text,prev_hash,this_hash,actor_id,action,target_kind,target_id,target_revision::text,before_hash,after_hash,reason,
    to_char(created_at AT TIME ZONE 'UTC','YYYY-MM-DD"T"HH24:MI:SS.US"Z"') AS created_at,request_id,canonical_content
    FROM ${this.q}.audit_events WHERE seq>$1::bigint AND seq<=$2::bigint ORDER BY audit_events.seq LIMIT 21`,[after,high])).rows;
   const more=rows.length>20,items=rows.slice(0,20),boundary=cursor?.head??head.last_hash;
   return {status:200,body:{items,head:{last_seq:high,last_hash:boundary},nextCursor:more?this.cursors.sign({version:1,scope,high,head:boundary,after:items.at(-1).seq}):null}};
  });
 }
}
