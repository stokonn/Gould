import test from 'node:test';import assert from 'node:assert/strict';
import caller from './local-caller.mjs';
const token='a'.repeat(64); // Synthetic fixture only.
test('loopback caller requires authentication and forwards only fixed request through service binding',async()=>{
 let calls=0;
 const env={STAGING_PROBE_ENABLED:'true',PRIVATE_VERIFICATION_TOKEN:token,
  PROBE:{fetch:async r=>{calls++;assert.equal(r.url,'https://private.invalid/__private_staging/probe');assert.equal(r.method,'POST');assert.equal(r.headers.get('Authorization'),'Bearer '+token);return new Response(null,{status:200});}}};
 for(const [url,auth,status] of [['http://public.invalid/probe',true,404],['http://127.0.0.1/probe',false,403],['http://127.0.0.1/probe?x=y',true,404],['http://127.0.0.1/probe',true,200]]){
  const r=await caller.fetch(new Request(url,{method:'POST',headers:auth?{Authorization:'Bearer '+token}:{}}),env);assert.equal(r.status,status);
 }
 assert.equal(calls,1);
 assert.equal((await caller.fetch(new Request('http://127.0.0.1/probe',{method:'POST'}),{})).status,503);
 const failing={...env,PROBE:{fetch:async()=>{throw Error('private transport error');}}};
 const failed=await caller.fetch(new Request('http://127.0.0.1/probe',{method:'POST',headers:{Authorization:'Bearer '+token}}),failing);
 assert.equal(failed.status,503);assert.equal(await failed.text(),'');
});
