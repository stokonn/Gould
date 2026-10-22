import {randomBytes,randomUUID,createHash} from 'node:crypto';
import {transaction,identifier} from './storage.mjs';
import {resolvePrincipal} from './principals.mjs';
import {unauthenticated,forbidden,uuid,readClock} from './errors.mjs';
export const protect=(kind,value)=>createHash('sha256').update(`goldberg:${kind}:`).update(value).digest('hex');
const opaque=kind=>`gs_${kind}_${randomBytes(32).toString('base64url')}`;
function token(value,kind){if(typeof value!=='string'||!new RegExp(`^gs_${kind}_[A-Za-z0-9_-]{43}$`).test(value))throw unauthenticated();return protect(kind,value);}
export class SessionService {
 constructor({connect,schema,verifier,clock=Date.now,accessTtlMs,refreshTtlMs,challengeTtlMs}){
  if(typeof connect!=='function'||typeof clock!=='function'||!verifier||[accessTtlMs,refreshTtlMs,challengeTtlMs].some(v=>!Number.isSafeInteger(v)||v<=0)||refreshTtlMs<accessTtlMs)throw new TypeError('explicit session security lifetimes required');
  Object.assign(this,{connect,q:identifier(schema),verifier,clock:()=>readClock(clock),accessTtlMs,refreshTtlMs,challengeTtlMs});
 }
 async challenge(){const challenge=opaque('challenge'),now=new Date(this.clock());
  await transaction(this.connect,c=>c.query(`INSERT INTO ${this.q}.auth_challenges(token_hash,created_at,expires_at) VALUES($1,$2,$3)`,[token(challenge,'challenge'),now,new Date(+now+this.challengeTtlMs)]));return {challenge,nonce:challenge};
 }
 async #audit(c,actor,action,requestId){await c.query(`SELECT ${this.q}.append_audit($1,$2,'principal',$1,NULL,$3)`,[actor,action,requestId]);}
 async #mint(c,principalId,familyId,familyExpires,predecessorId,now){
  const accessToken=opaque('access'),refreshToken=opaque('refresh'),sessionId=randomUUID();
  const expires=new Date(Math.min(+familyExpires,+now+this.accessTtlMs));
  await c.query(`INSERT INTO ${this.q}.sessions(id,principal_id,family_id,predecessor_id,token_hash,issued_at,expires_at)
    VALUES($1,$2,$3,$4,$5,$6,$7)`,[sessionId,principalId,familyId,predecessorId,token(accessToken,'access'),now,expires]);
  await c.query(`INSERT INTO ${this.q}.auth_refresh(token_hash,session_id,principal_id,family_id,created_at,expires_at) VALUES($1,$2,$3,$4,$5,$6)`,[token(refreshToken,'refresh'),sessionId,principalId,familyId,now,familyExpires]);
  return {accessToken,refreshToken,principalId,expiresAt:expires.toISOString()};
 }
 async login({credential,challenge},requestId=randomUUID()){
  const hash=token(challenge,'challenge'),identity=await this.verifier.verify(credential);
  if(identity.nonce!==challenge)throw unauthenticated();
  return transaction(this.connect,async c=>{
   const now=new Date(this.clock()),principalId=await resolvePrincipal(c,this.q,identity,now);
   const consumed=await c.query(`UPDATE ${this.q}.auth_challenges SET consumed_at=$2 WHERE token_hash=$1 AND consumed_at IS NULL AND expires_at>$2 RETURNING token_hash`,[hash,now]);
   if(consumed.rowCount!==1)throw unauthenticated();
   const familyId=randomUUID(),familyExpires=new Date(+now+this.refreshTtlMs);
   await c.query(`INSERT INTO ${this.q}.auth_families(id,principal_id,created_at,expires_at) VALUES($1,$2,$3,$4)`,[familyId,principalId,now,familyExpires]);
   const result=await this.#mint(c,principalId,familyId,familyExpires,null,now);
   await this.#audit(c,principalId,'session.created',requestId);return result;
  });
 }
 async #lock(c,principalId,familyId,additional=[]){
  const ids=[...new Set([principalId,...additional.map(uuid)])].sort();
  const {rows:principals}=await c.query(`SELECT id,state FROM ${this.q}.principals WHERE id=ANY($1::uuid[]) ORDER BY id FOR UPDATE`,[ids]);
  const {rows:[family]}=await c.query(`SELECT * FROM ${this.q}.auth_families WHERE id=$1 AND principal_id=$2 FOR UPDATE`,[familyId,principalId]);
  return {principal:principals.find(p=>p.id===principalId),family};
 }
 async #revoke(c,principalId,familyId,now,action,requestId){
  await c.query(`UPDATE ${this.q}.auth_families SET revoked_at=$2 WHERE id=$1 AND revoked_at IS NULL`,[familyId,now]);
  await c.query(`UPDATE ${this.q}.sessions SET revoked_at=$2 WHERE family_id=$1 AND revoked_at IS NULL`,[familyId,now]);
  await this.#audit(c,principalId,action,requestId);
 }
 async refresh(refreshToken,requestId=randomUUID()){
  const hash=token(refreshToken,'refresh');
  const result=await transaction(this.connect,async c=>{
   const {rows:[initial]}=await c.query(`SELECT principal_id,family_id FROM ${this.q}.auth_refresh WHERE token_hash=$1`,[hash]);
   if(!initial)throw unauthenticated();
   const {principal,family}=await this.#lock(c,initial.principal_id,initial.family_id),now=new Date(this.clock());
   if(!family||family.revoked_at||family.created_at>now||family.expires_at<=now)throw unauthenticated();
   if(!principal||principal.state!=='active')throw forbidden();
   const {rows:[refresh]}=await c.query(`SELECT * FROM ${this.q}.auth_refresh WHERE token_hash=$1 FOR UPDATE`,[hash]);
   if(!refresh||refresh.created_at>now||refresh.expires_at<=now)throw unauthenticated();
   if(refresh.consumed_at){await this.#revoke(c,principal.id,family.id,now,'session.refresh_replay',requestId);return {replayed:true};}
   const {rows:[session]}=await c.query(`SELECT revoked_at FROM ${this.q}.sessions WHERE id=$1`,[refresh.session_id]);
   if(!session||session.revoked_at)throw unauthenticated();
   await c.query(`UPDATE ${this.q}.auth_refresh SET consumed_at=$2 WHERE token_hash=$1`,[hash,now]);
   await c.query(`UPDATE ${this.q}.sessions SET revoked_at=$2 WHERE id=$1 AND revoked_at IS NULL`,[refresh.session_id,now]);
   const next=await this.#mint(c,principal.id,family.id,family.expires_at,refresh.session_id,now);
   await this.#audit(c,principal.id,'session.rotated',requestId);return next;
  });
  if(result.replayed)throw unauthenticated();return result;
 }
 async withSession(accessToken,fn,{additionalPrincipals=[]}={}){
  const hash=token(accessToken,'access');
  return transaction(this.connect,async c=>{
   const {rows:[initial]}=await c.query(`SELECT principal_id,family_id FROM ${this.q}.sessions WHERE token_hash=$1`,[hash]);
   if(!initial)throw unauthenticated();
   const {principal,family}=await this.#lock(c,initial.principal_id,initial.family_id,additionalPrincipals),now=new Date(this.clock());
   const {rows:[session]}=await c.query(`SELECT id,issued_at,expires_at,revoked_at FROM ${this.q}.sessions WHERE token_hash=$1`,[hash]);
   if(!session||session.revoked_at||session.issued_at>now||session.expires_at<=now||!family||family.revoked_at||family.created_at>now||family.expires_at<=now)throw unauthenticated();
   if(!principal||principal.state!=='active')throw forbidden();
   return fn(c,Object.freeze({principalId:principal.id,sessionId:session.id,familyId:family.id,expiresAt:session.expires_at.toISOString()}));
  });
 }
 async authenticate(accessToken){return this.withSession(accessToken,async(_c,context)=>context);}
 async logout(accessToken,requestId=randomUUID()){
  const hash=token(accessToken,'access');
  await transaction(this.connect,async c=>{
   const {rows:[initial]}=await c.query(`SELECT principal_id,family_id FROM ${this.q}.sessions WHERE token_hash=$1`,[hash]);
   if(!initial)return;
   const {family}=await this.#lock(c,initial.principal_id,initial.family_id);
   if(family&&!family.revoked_at)await this.#revoke(c,initial.principal_id,family.id,new Date(this.clock()),'session.logout',requestId);
  });
 }
}
