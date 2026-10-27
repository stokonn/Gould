import test from 'node:test';
import assert from 'node:assert/strict';
import {createFetchHandler} from '../fetch.mjs';
import {requestConnections,privateDatabaseOptions} from '../database.mjs';
import {identifier,migrate} from '../../../spikes/stage6-schema-pg/src/database.mjs';

test('Fetch uses real Request/Response, preserves receipts and emits bodyless 204',async()=>{
 let closed=0;
 const handle=createFetchHandler({createAdapter:async()=>({adapter:{invoke:async r=>({status:r.method==='DELETE'?204:200,headers:{'X-Request-ID':'receipt-id'},body:{path:r.path}})},close:async()=>{closed++;}})});
 const response=await handle(new Request('https://private.invalid/v1/admin/queue?q=%25'));
 assert.deepEqual(await response.json(),{path:'/v1/admin/queue?q=%25'});assert.equal(response.headers.get('x-request-id'),'receipt-id');
 const logout=await handle(new Request('https://private.invalid/v1/session',{method:'DELETE'}));assert.equal(logout.status,204);assert.equal(await logout.text(),'');assert.equal(closed,2);
});
test('body limit stops dispatch, including unknown-length streams',async()=>{
 let calls=0;const handle=createFetchHandler({maxBodyBytes:4,createAdapter:async()=>{calls++;throw Error('secret');}});
 for(const request of [new Request('https://private.invalid',{method:'POST',body:'12345'}),new Request('https://private.invalid',{method:'POST',body:'12345',headers:{'content-length':'5'}})]){const r=await handle(request);assert.equal(r.status,413);assert.equal((await r.text()).includes('secret'),false);}
 assert.equal(calls,0);
});
test('deadline aborts database work, closes late scope and redacts failure',async()=>{
 let signal,closed=0;const handle=createFetchHandler({timeoutMs:10,createAdapter:async o=>{signal=o.signal;await new Promise(r=>setTimeout(r,25));return {adapter:{invoke:async()=>{throw Error('password');}},close:async()=>{closed++;}};}});
 const r=await handle(new Request('https://private.invalid'));assert.equal(r.status,503);assert.ok(signal.aborted);assert.ok(!(await r.text()).includes('password'));await new Promise(r=>setTimeout(r,35));assert.equal(closed,1);
});
test('request connections close on failed connect, abort and repeated cleanup',async()=>{
 let ends=0;class Client{async connect(){}async end(){ends++;}}
 const controller=new AbortController(),scope=requestConnections({Client,options:{},signal:controller.signal});await scope.connect();controller.abort();await scope.close();assert.equal(ends,1);await assert.rejects(scope.connect());
 class Bad extends Client{async connect(){throw Error('password');}}
 const s=requestConnections({Client:Bad,options:{},signal:new AbortController().signal});await assert.rejects(s.connect(),e=>e.message==='database unavailable');assert.equal(ends,2);
});
test('TLS bindings require fixed provider host, CA and restricted user',()=>{
 const env={DB_HOST:'db.hlfsouvndjsbsaldttvi.supabase.co',EXPECTED_DB_HOST:'db.hlfsouvndjsbsaldttvi.supabase.co',DB_PORT:'5432',DB_USER:'goldberg_staging_probe',DB_PASSWORD:'test-only',DB_CA_PEM:'-----BEGIN CERTIFICATE-----'};
 const options=privateDatabaseOptions(env);assert.equal(options.ssl.rejectUnauthorized,true);assert.equal(options.ssl.servername,env.DB_HOST);
 for(const patch of [{DB_HOST:'evil.invalid'},{DB_USER:'postgres'},{DB_CA_PEM:''},{DB_PORT:'443'},{DB_PASSWORD:''}])assert.throws(()=>privateDatabaseOptions({...env,...patch}));
});
test('runtime identifier permits only canonical private schema and migration stays local',async()=>{
 assert.equal(identifier('goldberg_private'),'"goldberg_private"');assert.throws(()=>identifier('public'));assert.throws(()=>identifier('goldberg_private;DROP'));
 await assert.rejects(migrate({query:()=>{throw Error('must not query');}},'goldberg_private'),/prototype migrations only/);
});
test('stalled body deadline cancels stream before any database dispatch',async()=>{
 let cancelled=false,calls=0;
 const body=new ReadableStream({pull(){},cancel(){cancelled=true;}});
 const handle=createFetchHandler({timeoutMs:10,createAdapter:async()=>{calls++;throw Error('must not dispatch');}});
 const response=await handle(new Request('https://private.invalid/v1/submissions',{method:'POST',body,duplex:'half'}));
 assert.equal(response.status,503);await new Promise(r=>setTimeout(r,5));assert.equal(cancelled,true);assert.equal(calls,0);
});
test('caller cancellation aborts scoped work and oversized output is unavailable',async()=>{
 const controller=new AbortController();let signal,closed=0;
 const handle=createFetchHandler({timeoutMs:1000,createAdapter:async o=>{signal=o.signal;return {adapter:{invoke:async()=>new Promise(()=>{})},close:async()=>{closed++;}};}});
 const pending=handle(new Request('https://private.invalid',{signal:controller.signal}));await new Promise(r=>setTimeout(r,5));controller.abort();assert.equal((await pending).status,503);assert.equal(signal.aborted,true);assert.equal(closed,1);
 const bounded=createFetchHandler({createAdapter:async()=>({adapter:{invoke:async()=>({status:200,body:{value:'x'.repeat(1048576)}})}})});
 assert.equal((await bounded(new Request('https://private.invalid'))).status,503);
});
test('asynchronous pg socket errors retire the connection exactly once',async()=>{
 const {EventEmitter}=await import('node:events');let ends=0;
 class Client extends EventEmitter{async connect(){}async end(){ends++;}}
 const scope=requestConnections({Client,options:{},signal:new AbortController().signal});
 const c=await scope.connect();c.emit('error',Error('private socket detail'));await scope.close();assert.equal(ends,1);
});


test('connection diagnostics expose only bounded categories and never driver text or credentials',async()=>{
 const {connectionFailure}=await import('../database.mjs');
 const secret='postgres://administrator:PRIVATE-PASSWORD@host.invalid';
 assert.deepEqual(connectionFailure({code:'28P01',message:'password authentication failed '+secret}),{code:'28P01',reason:'authentication'});
 assert.deepEqual(connectionFailure({code:secret,message:'certificate failed '+secret}),{code:'UNKNOWN',reason:'certificate'});
 let diagnostic;
 class Bad {on(){}async connect(){throw {code:'ETIMEDOUT',message:secret};}async end(){}}
 const scope=requestConnections({Client:Bad,options:{},signal:new AbortController().signal,onError:r=>{diagnostic=r;throw Error(secret);}});
 await assert.rejects(scope.connect(),e=>e.message==='database unavailable');
 assert.deepEqual(diagnostic,{code:'ETIMEDOUT',reason:'timeout'});assert.ok(!JSON.stringify(diagnostic).includes(secret));
});


test('Hyperdrive transport accepts only the explicit private binding and never falls back',async()=>{
 const {runtimeDatabaseOptions}=await import('../database.mjs');
 const env={DB_TRANSPORT:'hyperdrive',CATALOGUE_DB:{connectionString:'postgresql://synthetic:synthetic@hyperdrive.local:5432/postgres'}};
 assert.equal(runtimeDatabaseOptions(env).connectionString,env.CATALOGUE_DB.connectionString);
 assert.ok(runtimeDatabaseOptions({...env,CATALOGUE_DB:{connectionString:'postgresql://synthetic:synthetic@proxy.hyperdrive.local/virtual?sslmode=disable'}}));
 for(const connectionString of ['postgresql://synthetic:synthetic@evil.invalid/postgres','postgresql://synthetic:synthetic@hyperdrive.local.evil.invalid/postgres','postgresql://synthetic:synthetic@hyperdrive.local/one/two','postgresql://synthetic:synthetic@hyperdrive.local/postgres?host=evil.invalid','invalid'])assert.throws(()=>runtimeDatabaseOptions({...env,CATALOGUE_DB:{connectionString}}));
 assert.throws(()=>runtimeDatabaseOptions({DB_TRANSPORT:'hyperdrive'}));assert.throws(()=>runtimeDatabaseOptions({DB_TRANSPORT:'unknown'}));
});


test('catalogue-only Worker rejects private and write routes before reading credentials or opening a database',async()=>{
 const {default:worker}=await import('../worker.mjs');
 const env=new Proxy({CATALOGUE_READ_ONLY:'true'},{get(target,key){if(key==='CATALOGUE_READ_ONLY')return target[key];throw Error('must not access private configuration');}});
 for(const path of ['/v1/session/challenge','/v1/session','/v1/session/refresh','/v1/admin/queue','/v1/submissions','/v1/catalogue/overlay/','/v1/catalogue/%6fverlay']){
  const r=await worker.fetch(new Request('https://catalogue.invalid'+path,{method:'POST',body:'private-input-must-not-be-reflected'}),env,{});assert.equal(r.status,404);assert.equal(r.headers.get('cache-control'),'no-store');assert.ok(!(await r.text()).includes('private-input'));
 }
 for(const path of ['/v1/catalogue/overlay','/v1/catalogue/changes']){
  const denied=await worker.fetch(new Request('https://catalogue.invalid'+path,{method:'POST'}),env,{});assert.equal(denied.status,405);assert.equal(denied.headers.get('allow'),'GET');
  const read=await worker.fetch(new Request('https://catalogue.invalid'+path),{CATALOGUE_READ_ONLY:'true'},{});assert.equal(read.status,503);
 }
});
