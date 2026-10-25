import pg from 'pg';
import {createFetchHandler} from './fetch.mjs';
import {createBffAdapter} from './adapter.mjs';
import {privateDatabaseOptions,requestConnections} from './database.mjs';
import {googleIdentityVerifier} from './identity.mjs';

const verifiers=new WeakMap();
function identity(env){
 if(env.IDENTITY_VERIFIER&&typeof env.IDENTITY_VERIFIER.verify==='function')return env.IDENTITY_VERIFIER;
 if(!verifiers.has(env))verifiers.set(env,googleIdentityVerifier({audience:env.GOOGLE_CLIENT_ID}));
 return verifiers.get(env);
}

/** Inert unless explicitly configured. The accepted verifier can be provided by
 * private RPC or by the fixed-origin Google transport. Both return only verified
 * issuer/subject/nonce. Sessions check nonce and derive live database roles. */
const handler=createFetchHandler({createAdapter:async({env,signal})=>{
 if(env.COMMUNITY_BFF_ENABLED!=='true'||!/^([0-9a-f]{64})$/.test(env.CURSOR_KEY??'')||!/^([0-9a-f]{64})$/.test(env.IP_KEY??''))throw new Error('BFF unavailable');
 const verifier=identity(env);
 const scope=requestConnections({Client:pg.Client,options:privateDatabaseOptions(env),signal});
 try{
  const adapter=createBffAdapter({connect:scope.connect,schema:'goldberg_private',verifier,cursorKey:Buffer.from(env.CURSOR_KEY,'hex'),ipKey:Buffer.from(env.IP_KEY,'hex')});
  return {adapter,close:scope.close};
 }catch{await scope.close();throw new Error('BFF unavailable');}
}});
export default {fetch(request,env,ctx){
 return handler(request,env,{waitUntil:p=>ctx.waitUntil(p),remoteAddress:request.headers.get('CF-Connecting-IP')});
}};
