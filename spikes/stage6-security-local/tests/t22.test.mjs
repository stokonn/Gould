// In-process status/dispatch investigation. Scripted SQL is NOT PostgreSQL execution.
import {test} from 'node:test';
import assert from 'node:assert/strict';
import {randomUUID} from 'node:crypto';
import {venue,body,invoke} from './t22-fixture.mjs';
function noProtectedWrites(v){
 // Idempotency expiry housekeeping can be attempted inside the transaction, then rolled back.
 const housekeeping=q=>q.startsWith('DELETE FROM "proto_t22".idempotency_results WHERE ');
 assert.ok(v.queries.every(q=>(!/^\s*(INSERT|UPDATE|DELETE|TRUNCATE|ALTER|DROP|CREATE)\b/.test(q)||housekeeping(q))&&!q.includes('.append_audit(')),JSON.stringify(v.queries));
 if(v.queries.some(housekeeping)){assert.ok(v.queries.includes('ROLLBACK'));assert.ok(!v.queries.includes('COMMIT'));}
}
for(const label of ['anonymous','malformed token','administrator'])test(`T22 audit edit ${label}: approved403 before auth SQL or handler dispatch`,async()=>{
 const v=venue(),token=label==='anonymous'?undefined:label==='malformed token'?'synthetic-invalid':v.accessToken;
 const request={operation:'admin.audit.edit',body:{schemaVersion:1},...(token?{accessToken:token}:{})};
 const r=await v.api.invoke(request);assert.equal(r.status,403);assert.equal(r.body.error.code,'forbidden');assert.deepEqual(v.trace,[]);assert.equal(v.logs.length,1);noProtectedWrites(v);
});
test('T22 same absent audit operation: Auth and composed adapters both403',async()=>{
 const v=venue(),request={operation:'admin.audit.edit',accessToken:v.accessToken,body:{schemaVersion:1}};
 assert.equal((await v.authApi.invoke(request)).status,403);assert.equal((await v.api.invoke(request)).status,403);assert.deepEqual(v.trace,[]);noProtectedWrites(v);
});
for(const operation of ['bootstrap.admin','roles.bootstrap','rpc','deployment','__proto__','constructor','toString'])test(`T22 absent operation ${operation} cannot dispatch via prototype`,async()=>{
 const v=venue();const r=await invoke(v,operation,{schemaVersion:1});assert.equal(r.status,403);assert.deepEqual(v.trace,[]);noProtectedWrites(v);
});
for(const [label,token,options,expected] of [
 ['missing',undefined,{},401],['malformed','bad',{},401],['unknown well formed',null,{missing:true},401],
 ['expired',null,{expired:true},401],['revoked',null,{revoked:true},401],['suspended',null,{state:'suspended'},403],
 ['contributor',null,{role:'contributor'},403],['moderator',null,{role:'moderator'},403],['no grants',null,{role:null},403],['unknown role',null,{role:'deployment_operator'},403]
])test(`T22 registered role route ${label} denies${expected} without privilege writes`,async()=>{
 const v=venue(options);const request={operation:'admin.roles',body:body(v)};
 if(label!=='missing')request.accessToken=token??v.accessToken;
 const r=await v.api.invoke(request);assert.equal(r.status,expected,JSON.stringify(r));noProtectedWrites(v);
 if(['missing','malformed'].includes(label))assert.deepEqual(v.trace,[]);
 if(['contributor','moderator','no grants','unknown role'].includes(label))assert.ok(v.queries.some(q=>q.startsWith('SELECT role FROM')));
 assert.ok(!v.logs.some(x=>JSON.stringify(x).includes(v.accessToken)));
});
for(const uppercase of [false,true])test(`T22 authenticated administrator self grant ${uppercase?'uppercase':'canonical'} UUID denies403 before mutation`,async()=>{
 const v=venue(),r=await invoke(v,'admin.roles',body(v,{principalId:uppercase?v.principalId.toUpperCase():v.principalId}));
 assert.equal(r.status,403,JSON.stringify(r));assert.ok(v.queries.some(q=>q.startsWith('SELECT role FROM')));noProtectedWrites(v);assert.ok(v.queries.includes('ROLLBACK'));assert.ok(!v.queries.includes('COMMIT'));
});
for(const [label,change] of [['forged actor',b=>b.actor=randomUUID()],['schemaVersion',b=>b.schemaVersion=2],['string revoke',b=>b.revoke='false'],['bad principal UUID',b=>b.principalId='bad'],['mixed suspend fields',b=>b.suspended=true]])test(`T22 malformed role request ${label} fails400 before authentication`,async()=>{
 const v=venue();const b=body(v);change(b);const r=await v.api.invoke({operation:'admin.roles',body:b});assert.equal(r.status,400);assert.deepEqual(v.trace,[]);noProtectedWrites(v);
});
test('T22 unsupported operator role denied400 after auth, no grant/audit SQL',async()=>{
 const v=venue();assert.equal((await invoke(v,'admin.roles',body(v,{principalId:randomUUID(),role:'deployment_operator'}))).status,400);noProtectedWrites(v);
});
test('T22 known audit read authenticates and authorizes; admin only read SQL',async()=>{
 for(const [role,expected] of [['contributor',403],['moderator',403],['administrator',200]]){
  const v=venue({role}),r=await invoke(v,'admin.audit',{schemaVersion:1});assert.equal(r.status,expected,JSON.stringify(r));noProtectedWrites(v);
  if(expected===200)assert.deepEqual(r.body.items,[]);
 }
});
test('T22 unavailable auth storage gives safe503, no role/audit mutation',async()=>{
 const v=venue({unavailable:true});const r=await invoke(v);assert.equal(r.status,503);assert.ok(!JSON.stringify(r).includes('synthetic unavailable'));noProtectedWrites(v);
});
test('T22 approved array operation rejects400 before registered route authentication',async()=>{
 const v=venue();const r=await v.api.invoke({operation:['admin.roles'],body:body(v)});assert.equal(r.status,400);assert.deepEqual(v.trace,[]);noProtectedWrites(v);
});
test('T22 malformed outer envelope bounded body and getters deny400 before any SQL',async()=>{
 const v=venue(),getter={};Object.defineProperty(getter,'x',{enumerable:true,get(){throw Error('must not invoke');}});
 for(const request of [{operation:'admin.audit.edit',body:{schemaVersion:1},actor:v.principalId},{operation:'admin.roles',body:getter},{operation:'admin.roles',body:{x:'x'.repeat(16385)}}])assert.equal((await v.api.invoke(request)).status,400);
 assert.deepEqual(v.trace,[]);noProtectedWrites(v);
});

for(const [label,operation] of [['missing',undefined],['null',null],['empty',''],['object',{}],['array',[]]])test(`T22 malformed operation ${label} denies400 without dispatch`,async()=>{const v=venue();const r=await v.api.invoke({operation,body:{schemaVersion:1}});assert.equal(r.status,400);assert.deepEqual(v.trace,[]);noProtectedWrites(v);});
