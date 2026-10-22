import {randomUUID} from 'node:crypto';
import {forbidden,notFound,invalid,uuid,allowObject,readClock} from './errors.mjs';
const ROLE_CAPABILITIES=Object.freeze({
 contributor:['submit','submission.read.own','submission.revise.own','submission.withdraw.own'],
 moderator:['queue.read','decide'],
 administrator:['queue.read','decide','roles.manage','quarantine','audit.read']
});
export class Authorization {
 constructor({sessions,clock=Date.now}){this.sessions=sessions;this.clock=()=>readClock(clock);this.q=sessions.q;}
 async #capabilities(c,principalId){
  const {rows}=await c.query(`SELECT role FROM ${this.q}.role_grants WHERE principal_id=$1 AND revoked_at IS NULL AND granted_at<=$2`,[principalId,new Date(this.clock())]);
  if(rows.some(r=>!Object.hasOwn(ROLE_CAPABILITIES,r.role)))throw forbidden();
  return [...new Set(rows.flatMap(r=>ROLE_CAPABILITIES[r.role]))].sort();
 }
 async capabilities(accessToken){return this.sessions.withSession(accessToken,(c,ctx)=>this.#capabilities(c,ctx.principalId));}
 async require(accessToken,capability,fn=async(_c,ctx)=>({principalId:ctx.principalId}),options){
  return this.sessions.withSession(accessToken,async(c,ctx)=>{
   const caps=await this.#capabilities(c,ctx.principalId);
   if(!caps.includes(capability))throw forbidden();return fn(c,ctx);
  },options);
 }
 async submission(accessToken,id,mode='read'){
  id=uuid(id);if(!['read','revise','withdraw','decide'].includes(mode))throw forbidden();
  return this.sessions.withSession(accessToken,async(c,ctx)=>{
   const caps=await this.#capabilities(c,ctx.principalId);
   const ownCapability=`submission.${mode}.own`;
   const canReview=mode==='read'?caps.includes('queue.read'):mode==='decide'&&caps.includes('decide');
   if(!canReview&&!caps.includes(ownCapability))throw forbidden();
   // Existence/ownership is part of the query. Unprivileged users never receive another author's row.
   const {rows:[row]}=await c.query(`SELECT id,author_id,status,current_revision::text FROM ${this.q}.submissions WHERE id=$1 AND ($2::boolean OR author_id=$3)`,[id,canReview,ctx.principalId]);
   if(!row)throw notFound();
   if(mode==='decide'&&row.author_id===ctx.principalId)throw forbidden();
   return row;
  });
 }
 async changeRole(accessToken,{principalId,role},revoke=false,requestId=randomUUID()){
  principalId=uuid(principalId);if(!Object.hasOwn(ROLE_CAPABILITIES,role))throw invalid();
  return this.require(accessToken,'roles.manage',async(c,ctx)=>{
   if(ctx.principalId===principalId)throw forbidden();
   const {rows:[target]}=await c.query(`SELECT id FROM ${this.q}.principals WHERE id=$1`,[principalId]);if(!target)throw notFound();
   const now=new Date(this.clock());let changed;
   if(revoke)changed=await c.query(`UPDATE ${this.q}.role_grants SET revoked_at=$3 WHERE principal_id=$1 AND role=$2 AND revoked_at IS NULL RETURNING id`,[principalId,role,now]);
   else changed=await c.query(`INSERT INTO ${this.q}.role_grants(id,principal_id,role,granted_by,granted_at) VALUES($1,$2,$3,$4,$5)
     ON CONFLICT(principal_id,role) WHERE revoked_at IS NULL DO NOTHING RETURNING id`,[randomUUID(),principalId,role,ctx.principalId,now]);
   if(changed.rowCount)await c.query(`SELECT ${this.q}.append_audit($1,$2,'principal',$3,NULL,$4)`,[ctx.principalId,revoke?'role.revoked':'role.granted',principalId,requestId]);
   return {changed:changed.rowCount>0};
  },{additionalPrincipals:[principalId]});
 }
 async suspend(accessToken,{principalId,suspended},requestId=randomUUID()){
  principalId=uuid(principalId);if(typeof suspended!=='boolean')throw invalid();
  return this.require(accessToken,'roles.manage',async(c,ctx)=>{
   if(ctx.principalId===principalId)throw forbidden();
   const result=await c.query(`UPDATE ${this.q}.principals SET state=$2 WHERE id=$1 AND state<>'deleted' RETURNING id`,[principalId,suspended?'suspended':'active']);
   if(!result.rowCount)throw notFound();
   await c.query(`SELECT ${this.q}.append_audit($1,$2,'principal',$3,NULL,$4)`,[ctx.principalId,suspended?'principal.suspended':'principal.unsuspended',principalId,requestId]);
   return {changed:true};
  },{additionalPrincipals:[principalId]});
 }
 // Guard reusable by future request handlers; it never silently strips elevated fields.
 static contributorBody(body,allowed){allowObject(body,allowed);return body;}
}
