import pg from 'pg';
import {createFetchHandler} from './fetch.mjs';
import {createBffAdapter} from './adapter.mjs';
import {privateDatabaseOptions,requestConnections} from './database.mjs';

/** Inert unless explicitly configured. No production identity transport is
 * invented: binding must implement the accepted verifier via private service
 * RPC. It returns issuer/subject/nonce, never roles. The session service checks
 * nonce and derives live roles from PostgreSQL itself. */
const handler=createFetchHandler({createAdapter:async({env,signal})=>{
 if(env.COMMUNITY_BFF_ENABLED!=='true'||!env.IDENTITY_VERIFIER||typeof env.IDENTITY_VERIFIER.verify!=='function'||!/^([0-9a-f]{64})$/.test(env.CURSOR_KEY??'')||!/^([0-9a-f]{64})$/.test(env.IP_KEY??''))throw new Error('BFF unavailable');
 const scope=requestConnections({Client:pg.Client,options:privateDatabaseOptions(env),signal});
 try{
  const adapter=createBffAdapter({connect:scope.connect,schema:'goldberg_private',verifier:env.IDENTITY_VERIFIER,cursorKey:Buffer.from(env.CURSOR_KEY,'hex'),ipKey:Buffer.from(env.IP_KEY,'hex')});
  return {adapter,close:scope.close};
 }catch{await scope.close();throw new Error('BFF unavailable');}
}});
export default {fetch(request,env,ctx){
 return handler(request,env,{waitUntil:p=>ctx.waitUntil(p),remoteAddress:request.headers.get('CF-Connecting-IP')});
}};
