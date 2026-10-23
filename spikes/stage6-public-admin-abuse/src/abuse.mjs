import {createHmac} from 'node:crypto';
import {isIP} from 'node:net';
import {ApiError,invalid,unavailable,readClock} from '../../stage6-auth-local/src/errors.mjs';
import {identifier} from '../../stage6-schema-pg/src/database.mjs';
import {SubmissionService} from '../../stage6-handlers-local/src/submissions.mjs';
import {ModerationService} from '../../stage6-handlers-local/src/moderation.mjs';
export function coarseAddress(ip){
 if(typeof ip!=='string'||ip.includes('%')||!isIP(ip))throw invalid();
 if(isIP(ip)===4)return ip.split('.').slice(0,3).join('.')+'.0/24';
 let v=ip.toLowerCase();
 if(v.includes('.')){const at=v.lastIndexOf(':'),b=v.slice(at+1).split('.').map(Number);v=v.slice(0,at+1)+((b[0]<<8)|b[1]).toString(16)+':'+((b[2]<<8)|b[3]).toString(16);}
 const halves=v.split('::'),left=halves[0]?halves[0].split(':'):[],right=halves[1]?halves[1].split(':'):[];
 const parts=halves.length===2?[...left,...Array(8-left.length-right.length).fill('0'),...right]:left;
 const words=parts.map(p=>parseInt(p,16));
 if(words.slice(0,5).every(w=>w===0)&&words[5]===65535)return [words[6]>>8,words[6]&255,words[7]>>8].join('.')+'.0/24';
 return words.slice(0,4).map(w=>w.toString(16).padStart(4,'0')).join(':')+'::/64';
}
export class AbuseProtection {
 constructor({schema,clock,key,daily=5,pending=10,ipLimit=20,ipWindowMs=3600000,retentionMs=7200000,onBoundary=async()=>{}}){
  if(!Buffer.isBuffer(key)||key.length<32||[daily,pending,ipLimit,ipWindowMs,retentionMs].some(v=>!Number.isSafeInteger(v)||v<1)||retentionMs<ipWindowMs||retentionMs>86400000)throw new TypeError('explicit bounded local limits/key required');
  Object.assign(this,{q:identifier(schema),clock:()=>readClock(clock),key:Buffer.from(key),daily,pending,ipLimit,ipWindowMs,retentionMs,onBoundary});
 }
 hash(ip){return createHmac('sha256',this.key).update('goldberg-coarse-ip-v1:'+coarseAddress(ip)).digest('hex');}
 async gate(c){const {rows:[state]}=await c.query(`SELECT enabled FROM ${this.q}.intake_control WHERE singleton FOR SHARE`);if(state?.enabled!==true)throw unavailable();}
 limited(seconds){const e=new ApiError(429,'rate_limited','Submission limit reached');e.retryAfter=Math.max(1,Math.ceil(seconds));return e;}
 async prune(c){
  await c.query(`DELETE FROM ${this.q}.abuse_limits WHERE scope='coarse_ip' AND window_end<=$1`,[new Date(this.clock()-this.retentionMs)]);
 }
 async consume(c,principal,ip){
  await this.gate(c);const now=this.clock(),day=Math.floor(now/86400000)*86400000,window=Math.floor(now/this.ipWindowMs)*this.ipWindowMs;
  // Principal already locked by SessionService; account quotas and pending count cannot race.
  const pending=(await c.query(`SELECT count(*)::text AS n FROM ${this.q}.submissions WHERE author_id=$1 AND status IN ('submitted','in_review','changes_requested')`,[principal])).rows[0].n;
  if(BigInt(pending)>=BigInt(this.pending))throw this.limited(60);
  const hash=this.hash(ip);
  // Serialize across accounts in one coarse group, including expiry cleanup.
  await c.query('SELECT pg_advisory_xact_lock(hashtextextended($1,0))',[`abuse-ip:${this.q}:${hash}`]);
  await this.prune(c);
  for(const [scope,key,start,end,limit,pid] of [['principal',principal,day,day+86400000,this.daily,principal],['coarse_ip',hash,window,window+this.ipWindowMs,this.ipLimit,null]]){
   const result=await c.query(`INSERT INTO ${this.q}.abuse_limits(scope,scope_key,principal_id,window_start,window_end,count) VALUES($1,$2,$3,$4,$5,1)
    ON CONFLICT(scope,scope_key,window_start) DO UPDATE SET count=${this.q}.abuse_limits.count+1 WHERE ${this.q}.abuse_limits.count<$6::bigint RETURNING count::text`,[scope,key,pid,new Date(start),new Date(end),String(limit)]);
   if(!result.rowCount)throw this.limited((end-now)/1000);
   await this.onBoundary(scope,c);
  }
 }
}
// Decorate the existing transaction/idempotency boundary; do not duplicate submission/auth storage.
export class ProtectedSubmissions extends SubmissionService {
 constructor(options){super(options);this.abuse=options.abuse;}
 async create(token,body,requestId,transport={}){
  const remoteAddress=transport.remoteAddress;
  const base=this.idempotency,service=Object.assign(Object.create(Object.getPrototypeOf(this)),this,{idempotency:{run:(c,p,op,b,fn)=>base.run(c,p,op,b,async()=>{await this.abuse.consume(c,p,remoteAddress);return fn();})}});
  return SubmissionService.prototype.create.call(service,token,body,requestId);
 }
 async revise(token,body,requestId){
  const base=this.idempotency,service=Object.assign(Object.create(Object.getPrototypeOf(this)),this,{idempotency:{run:(c,p,op,b,fn)=>base.run(c,p,op,b,async()=>{await this.abuse.gate(c);return fn();})}});
  return SubmissionService.prototype.revise.call(service,token,body,requestId);
 }
}
export class ProtectedModeration extends ModerationService {
 constructor(options){super(options);this.abuse=options.abuse;}
 async act(token,op,body,requestId){
  const base=this.idempotency,decorated=Object.create(base);decorated.run=(c,p,o,b,fn)=>base.run(c,p,o,b,async()=>{if(o==='moderation.decide'&&b.decision==='approve')await this.abuse.gate(c);return fn();});
  return ModerationService.prototype.act.call(Object.assign(Object.create(Object.getPrototypeOf(this)),this,{idempotency:decorated}),token,op,body,requestId);
 }
}
