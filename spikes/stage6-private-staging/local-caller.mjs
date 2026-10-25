// Loopback-only development caller; authenticated remote service-binding transport.
// No real network transport/session is started by importing this module.
import {equalSecret} from './probe-handler.mjs';
export default {
/** @param {Request} request @param {{STAGING_PROBE_ENABLED?:string,PRIVATE_VERIFICATION_TOKEN?:string,PROBE:{fetch(r:Request):Promise<Response>}}} env */
async fetch(request,env){
 const u=new URL(request.url);
 if(!['127.0.0.1','localhost','[::1]'].includes(u.hostname) || request.method!=='POST' || u.pathname!=='/probe' || u.search)
  return new Response(null,{status:404});
 const token=env.PRIVATE_VERIFICATION_TOKEN;
 if(env.STAGING_PROBE_ENABLED!=='true' || typeof token!=='string' || !/^[a-f0-9]{64}$/.test(token))
  return new Response(null,{status:503});
 try {
 const auth=request.headers.get('Authorization') ?? '';
 if(auth.length!==71 || !auth.startsWith('Bearer ')
   || !await equalSecret(token,auth.slice(7),globalThis.crypto.subtle))
  return new Response(null,{status:403});
 // Service binding fetch does not expose the target via an Internet URL.
 return await env.PROBE.fetch(new Request('https://private.invalid/__private_staging/probe',{
  method:'POST',headers:{Authorization:'Bearer '+token},
 }));
 } catch { return new Response(null,{status:503}); }
}};
