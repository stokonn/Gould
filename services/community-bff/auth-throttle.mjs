import {createHmac} from 'node:crypto';
import {transaction,identifier} from '../../spikes/stage6-auth-local/src/storage.mjs';
import {ApiError,readClock} from '../../spikes/stage6-auth-local/src/errors.mjs';
import {coarseAddress} from '../../spikes/stage6-public-admin-abuse/src/abuse.mjs';

/** One shared challenge/login/refresh budget, including failed credentials.
 * No raw IP or token is stored. Missing transport identity shares a fail-closed
 * bucket rather than allowing callers to bypass throttling with absent metadata.
 * Uses the existing disposable abuse counters; no migration or background job. */
export class AuthThrottle {
 constructor({connect,schema,key,clock=Date.now,limit=30}){
  if(typeof connect!=='function'||!Buffer.isBuffer(key)||key.length<32||!Number.isSafeInteger(limit)||limit<1||limit>120)throw new TypeError('bounded authentication throttle required');
  Object.assign(this,{connect,q:identifier(schema),key:Buffer.from(key),clock:()=>readClock(clock),limit});
 }
 async consume(remoteAddress){
  const address=remoteAddress===undefined?'unattributed':coarseAddress(remoteAddress);
  const hash=createHmac('sha256',this.key).update('goldberg-auth-throttle-v1:'+address).digest('hex');
  const now=this.clock(),start=Math.floor(now/60000)*60000,end=start+60000;
  const allowed=await transaction(this.connect,async c=>{
   await c.query(`DELETE FROM ${this.q}.abuse_limits WHERE scope='coarse_ip' AND window_end<=$1`,[new Date(now-7200000)]);
   const result=await c.query(`INSERT INTO ${this.q}.abuse_limits(scope,scope_key,principal_id,window_start,window_end,count)
    VALUES('coarse_ip',$1,NULL,$2,$3,1) ON CONFLICT(scope,scope_key,window_start)
    DO UPDATE SET count=${this.q}.abuse_limits.count+1 WHERE ${this.q}.abuse_limits.count<$4::bigint RETURNING count`,[hash,new Date(start),new Date(end),String(this.limit)]);
   return result.rowCount===1;
  });
  if(!allowed){const e=new ApiError(429,'rate_limited','Authentication limit reached');e.retryAfter=Math.max(1,Math.ceil((end-now)/1000));throw e;}
 }
}
