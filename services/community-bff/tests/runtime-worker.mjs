// Local workerd test entrypoint only; no configuration or deployment path.
import pg from 'pg';
import {createFetchHandler} from '../fetch.mjs';
import {createBffAdapter} from '../adapter.mjs';
import {syntheticValidator} from '../../../spikes/stage6-api-v1-local/tests/venue.mjs';
import {requestConnections} from '../database.mjs';
const handle=createFetchHandler({createAdapter:async({env,signal})=>{
 if(!/^proto_http_[a-f0-9]+$/.test(env.SCHEMA)||!/^\d+$/.test(env.PG_PORT))throw Error('local fixture only');
 const scope=requestConnections({Client:pg.Client,signal,options:{host:'127.0.0.1',port:Number(env.PG_PORT),database:'schema_prototype',user:'schema_prototype',password:'',connectionTimeoutMillis:3000,query_timeout:5000}});
 const adapter=createBffAdapter({connect:scope.connect,schema:env.SCHEMA,cursorKey:Buffer.from('a'.repeat(64),'hex'),ipKey:Buffer.from('b'.repeat(64),'hex'),validator:syntheticValidator(),verifier:{verify:async()=>{throw Error('no runtime test login');}}});
 return {adapter,close:scope.close};
}});
export default {fetch:(request,env,ctx)=>handle(request,env,{remoteAddress:'127.0.0.1',waitUntil:p=>ctx.waitUntil(p)})};
