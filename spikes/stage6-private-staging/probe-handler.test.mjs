import test from 'node:test';
import assert from 'node:assert/strict';
import {webcrypto} from 'node:crypto';
import {createProbeHandler} from './probe-handler.mjs';
const token='a'.repeat(64); // Synthetic fixture, never a hosted credential.
const request=(auth='Bearer '+token,path='/__private_staging/probe',method='POST') =>
 new Request('https://synthetic.invalid'+path,{method,headers:auth?{Authorization:auth}:{}});

test('disabled by default and malformed configuration never touches adapter',async()=>{
 let calls=0;const f=createProbeHandler({readProbe:async()=>{calls++;return true;},subtle:webcrypto.subtle});
 for(const env of [{},{STAGING_PROBE_ENABLED:'TRUE'},{STAGING_PROBE_ENABLED:'true'},
   {STAGING_PROBE_ENABLED:'true',PRIVATE_VERIFICATION_TOKEN:'bad'}]) {
  assert.equal((await f(request(),env)).status,503);
 }
 assert.equal(calls,0);
});
test('missing/wrong/oversized credentials and alternate routes deny before database',async()=>{
 let calls=0;const f=createProbeHandler({readProbe:async()=>{calls++;return true;},subtle:webcrypto.subtle});
 const env={STAGING_PROBE_ENABLED:'true',PRIVATE_VERIFICATION_TOKEN:token};
 for(const auth of ['', 'Bearer '+'b'.repeat(64),'Bearer '+token+'x','Basic '+token]) assert.equal((await f(request(auth),env)).status,403);
 for(const req of [request(undefined,'/v1/me'),request(undefined,'/__private_staging/probe?token=x'),request(undefined,undefined,'GET')]) assert.equal((await f(req,env)).status,404);
 assert.equal(calls,0);
});
test('authenticated synthetic success; no credential,cookie,CORS or response caching',async()=>{
 let calls=0;const f=createProbeHandler({readProbe:async()=>{calls++;return true;},subtle:webcrypto.subtle});
 const r=await f(request(),{STAGING_PROBE_ENABLED:'true',PRIVATE_VERIFICATION_TOKEN:token});
 assert.equal(r.status,200);assert.deepEqual(await r.json(),{synthetic:true,database:'reachable'});
 assert.equal(r.headers.get('cache-control'),'no-store');assert.equal(r.headers.get('set-cookie'),null);
 assert.equal(r.headers.get('access-control-allow-origin'),null);assert.equal(calls,1);
});
test('database denial/outage/timeout and cryptographic error fail closed without leaking error text',async()=>{
 const env={STAGING_PROBE_ENABLED:'true',PRIVATE_VERIFICATION_TOKEN:token};
 for(const readProbe of [async()=>false,async()=>{throw Error('secret SQL/credentials');}]) {
  const r=await createProbeHandler({readProbe,subtle:webcrypto.subtle})(request(),env);
  assert.equal(r.status,503);assert.deepEqual(await r.json(),{error:'probe_unavailable'});
 }
 let called=false;const r=await createProbeHandler({readProbe:async()=>{called=true;},subtle:{generateKey:async()=>{throw Error('crypto failure');}}})(request(),env);
 assert.equal(r.status,503);assert.equal(called,false);
});
