import pg from 'pg';
import {createFetchHandler} from './fetch.mjs';
import {createBffAdapter} from './adapter.mjs';
import {privateDatabaseOptions,requestConnections} from './database.mjs';
import {configuredIdentityVerifier} from './identity.mjs';

/** Inert unless explicitly configured. The accepted verifier can be provided by
 * private RPC or by the fixed-origin Google transport. Both return only verified
 * issuer/subject/nonce. Sessions check nonce and derive live database roles. */
const handler=createFetchHandler({createAdapter:async({env,signal})=>{
 if(env.COMMUNITY_BFF_ENABLED!=='true'||!/^([0-9a-f]{64})$/.test(env.CURSOR_KEY??'')||!/^([0-9a-f]{64})$/.test(env.IP_KEY??''))throw new Error('BFF unavailable');
 const verifier=configuredIdentityVerifier(env);
 const scope=requestConnections({Client:pg.Client,options:privateDatabaseOptions(env),signal,onError:failure=>console.warn('database_connection_unavailable',failure)});
 try{
  const adapter=createBffAdapter({connect:scope.connect,schema:'goldberg_private',verifier,cursorKey:Buffer.from(env.CURSOR_KEY,'hex'),ipKey:Buffer.from(env.IP_KEY,'hex')});
  return {adapter,close:scope.close};
 }catch{await scope.close();throw new Error('BFF unavailable');}
}});
export default {fetch(request,env,ctx){
 if(env.GOOGLE_OAUTH_ENABLED!=='true'&&request.method==='POST'&&['/v1/session/challenge','/v1/session','/v1/session/refresh'].includes(new URL(request.url).pathname)){
  const requestId=crypto.randomUUID();
  return new Response(JSON.stringify({schemaVersion:1,error:{code:'service_unavailable',message:'Service unavailable',requestId}}),{status:503,headers:{'Content-Type':'application/json','Cache-Control':'no-store','X-Request-ID':requestId}});
 }
 return handler(request,env,{waitUntil:p=>ctx.waitUntil(p),remoteAddress:request.headers.get('CF-Connecting-IP')});
}};
