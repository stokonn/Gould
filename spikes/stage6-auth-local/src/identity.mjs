import {jwtVerify,createLocalJWKSet,decodeProtectedHeader} from 'jose';
import {unauthenticated,unavailable,readClock} from './errors.mjs';
// Fetcher is trusted configuration, never a URL from a token header. No network transport here.
export class IdentityVerifier {
 #cache;#freshUntil=0;#inflight;#lastUnknown=-Infinity;
 constructor({issuer,audience,fetchJwks,clock=Date.now,algorithms=['RS256'],timeoutMs=500,maxAgeSeconds=300,skewSeconds=0}){
  if(typeof issuer!=='string'||!issuer||typeof audience!=='string'||!audience||typeof fetchJwks!=='function'||typeof clock!=='function'||
    !Array.isArray(algorithms)||!algorithms.length||algorithms.some(a=>!['RS256','ES256'].includes(a))||
    !Number.isInteger(timeoutMs)||timeoutMs<1||timeoutMs>5000||!Number.isInteger(maxAgeSeconds)||maxAgeSeconds<1||maxAgeSeconds>3600||
    !Number.isInteger(skewSeconds)||skewSeconds<0||skewSeconds>60)throw new TypeError('invalid verifier configuration');
  Object.assign(this,{issuer,audience,fetchJwks,clock:()=>readClock(clock),algorithms:[...algorithms],timeoutMs,maxAgeSeconds,skewSeconds});
 }
 async #load(){
  if(this.#inflight)return this.#inflight;
  this.#inflight=(async()=>{
   const controller=new AbortController();let timer;
   try{
    const result=await Promise.race([Promise.resolve().then(()=>this.fetchJwks({signal:controller.signal})),new Promise((_,reject)=>{timer=setTimeout(()=>{controller.abort();reject(unavailable());},this.timeoutMs);})]);
    if(!result||!Number.isInteger(result.maxAgeSeconds)||result.maxAgeSeconds<0||!result.jwks||!Array.isArray(result.jwks.keys)||
      !result.jwks.keys.length||result.jwks.keys.length>16||JSON.stringify(result.jwks).length>65536||
      new Set(result.jwks.keys.map(k=>k?.kid)).size!==result.jwks.keys.length||
      result.jwks.keys.some(k=>!k||typeof k.kid!=='string'||!k.kid||k.kid.length>128||'d' in k||'k' in k))throw unavailable();
    this.#cache=createLocalJWKSet(structuredClone(result.jwks));
    this.#freshUntil=this.clock()+Math.min(result.maxAgeSeconds,this.maxAgeSeconds)*1000;
   }catch{this.#freshUntil=0;throw unavailable();}finally{clearTimeout(timer);}
  })();
  try{return await this.#inflight;}finally{this.#inflight=undefined;}
 }
 async #key(header,token){
  const refreshed=!this.#cache||this.clock()>=this.#freshUntil;
  if(refreshed)await this.#load();
  try{return await this.#cache(header,token);}catch(error){
   if(error.code!=='ERR_JWKS_NO_MATCHING_KEY'||refreshed||this.clock()-this.#lastUnknown<1000)throw error;
   this.#lastUnknown=this.clock();await this.#load();return this.#cache(header,token);
  }
 }
 async verify(token){
  if(typeof token!=='string'||token.length>8192||token.split('.').length!==3)throw unauthenticated();
  try{
   const header=decodeProtectedHeader(token);
   if(!this.algorithms.includes(header.alg)||typeof header.kid!=='string'||!header.kid||header.kid.length>128||header.jku||header.jwk||header.x5u)throw unauthenticated();
   const {payload}=await jwtVerify(token,(h,t)=>this.#key(h,t),{issuer:this.issuer,audience:this.audience,algorithms:this.algorithms,
    typ:'JWT',requiredClaims:['iss','sub','aud','iat','exp','nonce'],clockTolerance:this.skewSeconds,currentDate:new Date(this.clock()),maxTokenAge:this.maxAgeSeconds});
   const now=Math.floor(this.clock()/1000);
   if(payload.aud!==this.audience||typeof payload.sub!=='string'||!payload.sub||payload.sub.length>255||typeof payload.nonce!=='string'||payload.nonce.length>256||
    !Number.isSafeInteger(payload.iat)||!Number.isSafeInteger(payload.exp)||payload.iat>now+this.skewSeconds||payload.exp<=payload.iat)throw unauthenticated();
   return Object.freeze({issuer:payload.iss,subject:payload.sub,nonce:payload.nonce});
  }catch(e){if(e.status===503)throw e;throw unauthenticated();}
 }
}
