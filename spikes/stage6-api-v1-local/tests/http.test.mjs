import test from 'node:test';
import assert from 'node:assert/strict';
import {createLocalHttpServer,listenLocalHttp} from '../src/http.mjs';
import {venue} from './venue.mjs';

async function running(){
 const v=await venue();
 const server=createLocalHttpServer({adapter:v.adapter});
 const base=await listenLocalHttp(server);
 return {v,base,close:()=>new Promise(resolve=>server.close(resolve))};
}
async function json(response){return response.status===204?null:response.json();}

test('loopback HTTP preserves session DTOs, authorization and 204 logout',async t=>{
 const {v,base,close}=await running();t.after(close);
 assert.equal(new URL(base).hostname,'127.0.0.1');
 let response=await fetch(`${base}/v1/session/challenge`,{method:'POST',headers:{'content-type':'application/json'},body:'{"schemaVersion":1}'});
 assert.equal(response.status,201);const challenge=await response.json();
 const credential=JSON.stringify({issuer:'https://identity.synthetic.invalid',audience:'synthetic-local',subject:'reviewer',nonce:challenge.nonce,iat:v.now(),exp:v.now()+120000});
 response=await fetch(`${base}/v1/session`,{method:'POST',headers:{'content-type':'application/json'},body:JSON.stringify({schemaVersion:1,credential,challenge:challenge.challenge})});
 assert.equal(response.status,201);const session=await response.json();
 assert.equal(session.principalId,'00000000-0000-4000-8000-000000000001');
 response=await fetch(`${base}/v1/me/capabilities`,{headers:{authorization:`Bearer ${session.accessToken}`}});
 assert.equal(response.status,200);assert.deepEqual(new Set((await response.json()).capabilities),new Set(['queue.read','decide','quarantine','audit.read','roles.manage']));
 for(const endpoint of ['queue','audit','stats']){
  response=await fetch(`${base}/v1/admin/${endpoint}`,{headers:{authorization:`Bearer ${session.accessToken}`}});
  assert.equal(response.status,503);assert.equal((await response.json()).error.code,'service_unavailable');
 }
 response=await fetch(`${base}/v1/session`,{method:'DELETE',headers:{authorization:`Bearer ${session.accessToken}`}});
 assert.equal(response.status,204);assert.equal(await response.text(),'');
});

test('loopback transport rejects duplicate JSON, oversized bodies and unsupported methods',async t=>{
 const {base,close}=await running();t.after(close);
 let response=await fetch(`${base}/v1/session/challenge`,{method:'POST',headers:{'content-type':'application/json'},body:'{"schemaVersion":1,"schemaVersion":1}'});
 assert.equal(response.status,400);assert.equal((await response.json()).error.code,'validation_failed');
 response=await fetch(`${base}/v1/admin/queue?status=in_review&status=submitted`);
 assert.equal(response.status,400);
 response=await fetch(`${base}/v1/session/challenge`,{method:'POST',headers:{'content-type':'application/json','content-length':'20000'},body:'x'.repeat(20000)});
 assert.equal(response.status,413);
 response=await fetch(`${base}/v1/session/challenge`,{method:'PUT'});
 assert.equal(response.status,405);assert.equal((await response.json()).error.code,'method_not_allowed');
});
