import {IdentityVerifier} from '../../spikes/stage6-auth-local/src/identity.mjs';
import {unavailable} from '../../spikes/stage6-auth-local/src/errors.mjs';

// Configuration fixes the origin. JWT headers and request parameters cannot
// select a key URL. The accepted verifier owns rotation, single-flight and TTL.
export const GOOGLE_JWKS_URL='https://www.googleapis.com/oauth2/v3/certs';
export function googleIdentityVerifier({audience,fetcher=fetch,clock=Date.now}) {
 if(typeof audience!=='string'||!/^[A-Za-z0-9_-]{1,200}\.apps\.googleusercontent\.com$/.test(audience)||typeof fetcher!=='function')throw new TypeError('Google client ID required');
 return new IdentityVerifier({issuer:'https://accounts.google.com',audience,clock,algorithms:['RS256'],skewSeconds:60,timeoutMs:3000,maxAgeSeconds:300,
  fetchJwks:async({signal})=>{
   let reader;
   try{
    const response=await fetcher(GOOGLE_JWKS_URL,{signal,redirect:'error',headers:{Accept:'application/json'}});
    reader=response.body?.getReader();
    if(response.status!==200||!/^application\/json(?:\s*;|$)/i.test(response.headers.get('content-type')??''))throw unavailable();
    const length=response.headers.get('content-length');
    if(length!==null&&(!/^\d+$/.test(length)||Number(length)>65536))throw unavailable();
    if(!reader)throw unavailable();
    const chunks=[];let size=0;
    while(true){const {value,done}=await reader.read();if(done)break;size+=value.byteLength;if(size>65536)throw unavailable();chunks.push(value);}
    const jwks=JSON.parse(new TextDecoder('utf-8',{fatal:true}).decode(Buffer.concat(chunks)));
    const cache=response.headers.get('cache-control')??'';
    const match=/(?:^|,)\s*max-age=(\d+)\s*(?:,|$)/i.exec(cache);
    const age=Number(response.headers.get('age')??0);
    const maxAgeSeconds=/(?:^|,)\s*(?:no-store|no-cache)\s*(?:,|$)/i.test(cache)||!Number.isSafeInteger(age)||age<0?0:Math.max(0,Math.min(300,Number(match?.[1]??0)-age));
    return {jwks,maxAgeSeconds};
   }catch{throw unavailable();}
   finally{await reader?.cancel().catch(()=>{});}
  }});
}
