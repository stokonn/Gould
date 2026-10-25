import worker from './worker.bundle.mjs'; import assert from 'node:assert/strict'; let count=0;
for(const method of ['GET','POST','PUT','DELETE','OPTIONS','HEAD']) for(const path of ['/','/v1/me','/v1/admin/queue','/health','/oauth/callback']) {
 const r=await worker.fetch(new Request('https://synthetic.invalid'+path,{method}),new Proxy({},{get(){throw Error('secret access')}}));
 assert.equal(r.status,503); assert.equal(r.headers.get('cache-control'),'no-store');assert.equal(r.headers.get('set-cookie'),null);assert.equal(r.headers.get('access-control-allow-origin'),null);assert.deepEqual(await r.json(),{error:'private_staging_disabled'});count++;
} console.log('PASS: '+count+' bundled Worker request cases');