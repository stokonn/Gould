// Local synthetic security probes. No listener, external fetcher, real account or SQL fixture.
import {test,before} from 'node:test';
import assert from 'node:assert/strict';
import {randomUUID,randomBytes} from 'node:crypto';
import {createRequire} from 'node:module';
const {SignJWT}=await import(createRequire(new URL('../../stage6-auth-local/package.json',import.meta.url)).resolve('jose'));
import {IdentityVerifier} from '../../stage6-auth-local/src/identity.mjs';
import {FakeClock,FakeJwks,makeKey,credential,issuer,audience} from '../../stage6-auth-local/tests/fixtures.mjs';
import {LocalApi} from '../../stage6-auth-local/src/local-api.mjs';
import {unauthenticated,forbidden} from '../../stage6-auth-local/src/errors.mjs';
import {Validator} from '../../stage6-handlers-local/src/validation.mjs';
import {CommunityApi} from '../../stage6-handlers-local/src/local-api.mjs';
import {compose} from '../../stage6-public-admin-abuse/src/compose.mjs';
let key,other;
before(async()=>{key=await makeKey('security-synthetic-1');other=await makeKey('security-synthetic-2');});
const venue=()=>{const clock=new FakeClock(),provider=new FakeJwks([key.jwk]);return {clock,provider,verifier:new IdentityVerifier({issuer,audience,clock:clock.now,fetchJwks:provider.fetch,timeoutMs:25})};};
const deny=(promise,status)=>assert.rejects(promise,e=>e.status===status);

for(const [label,claims] of [
 ['expired',{exp:1893456000}],['future iat',{iat:1893456001}],['wrong issuer',{iss:issuer+'/'}],
 ['wrong audience',{aud:audience+'-other'}],['multi audience',{aud:[audience,'other']}],
 ['missing nonce',{nonce:undefined}]
])test(`T1/T4 independent verifier rejects ${label}`,async()=>{
 const v=venue();await deny(v.verifier.verify(await credential(key,v.clock,{claims})),401);
});
for(const [name,value] of [['jku','https://attacker.synthetic.invalid/keys'],['x5u','https://attacker.synthetic.invalid/cert'],['jwk',{kty:'oct',k:'synthetic'}]]){
 test(`T2 untrusted ${name} header rejected before trusted resolver invocation`,async()=>{
  const v=venue();await deny(v.verifier.verify(await credential(key,v.clock,{header:{[name]:value}})),401);assert.equal(v.provider.calls,0);
 });
}
test('T2 none HMAC and forged signatures deny without successful identity',async()=>{
 const v=venue(),good=await credential(key,v.clock),parts=good.split('.');
 parts[2]=(parts[2][0]==='A'?'B':'A')+parts[2].slice(1);await deny(v.verifier.verify(parts.join('.')),401);
 const claims={iss:issuer,aud:audience,sub:'synthetic',iat:1893456000,exp:1893456120,nonce:'synthetic'};
 const none=Buffer.from(JSON.stringify({alg:'none',kid:key.kid,typ:'JWT'})).toString('base64url')+'.'+Buffer.from(JSON.stringify(claims)).toString('base64url')+'.';
 await deny(v.verifier.verify(none),401);
 await deny(v.verifier.verify(await new SignJWT(claims).setProtectedHeader({alg:'HS256',kid:key.kid,typ:'JWT'}).sign(randomBytes(32))),401);
});
test('T2 header cache cap exact expiry fails closed then recovers with rotated keys',async()=>{
 const v=venue();v.provider.maxAgeSeconds=3600;
 const verifier=new IdentityVerifier({issuer,audience,clock:v.clock.now,fetchJwks:v.provider.fetch,maxAgeSeconds:1});
 const jwt=await credential(key,v.clock);await verifier.verify(jwt);v.provider.available=false;v.clock.advance(999);await verifier.verify(jwt);
 v.clock.advance(1);await deny(verifier.verify(jwt),503);assert.equal(v.provider.calls,2);
 v.provider.available=true;v.provider.keys=[other.jwk];await verifier.verify(await credential(other,v.clock));assert.equal(v.provider.calls,3);
 await deny(verifier.verify(jwt),401);
});
test('T2 zero max age cannot reuse keys on the next verification',async()=>{
 const v=venue();v.provider.maxAgeSeconds=0;const jwt=await credential(key,v.clock);await v.verifier.verify(jwt);
 v.provider.available=false;await deny(v.verifier.verify(jwt),503);assert.equal(v.provider.calls,2);
});
test('T2 unknown kid flood bounded and later rotation eligible at exactly one second',async()=>{
 const v=venue();await v.verifier.verify(await credential(key,v.clock));
 const unknown=await credential(other,v.clock);await deny(v.verifier.verify(unknown),401);assert.equal(v.provider.calls,2);
 for(let i=0;i<8;i++)await deny(v.verifier.verify(await credential(other,v.clock,{header:{kid:'unknown-'+i}})),401);
 assert.equal(v.provider.calls,2);v.provider.keys=[key.jwk,other.jwk];v.clock.advance(999);await deny(v.verifier.verify(unknown),401);
 v.clock.advance(1);await v.verifier.verify(unknown);assert.equal(v.provider.calls,3);
});
test('T2 parallel cold resolver outage coalesces and never returns identity',async()=>{
 const v=venue();v.provider.available=false;const jwt=await credential(key,v.clock);
 const r=await Promise.allSettled(Array.from({length:12},()=>v.verifier.verify(jwt)));
 assert.equal(v.provider.calls,1);assert.ok(r.every(x=>x.status==='rejected'&&x.reason.status===503));
 v.provider.available=true;assert.equal((await v.verifier.verify(jwt)).subject,'synthetic-subject');
});
test('T1 unavailable clock is safe 503 and does not invoke resolver',async()=>{
 const v=venue(),jwt=await credential(key,v.clock);v.clock.ms=NaN;await deny(v.verifier.verify(jwt),503);assert.equal(v.provider.calls,0);
});
test('T1 denial envelope and structured log contain no supplied credential or exception text',async()=>{
 const access='synthetic-token-marker',logs=[];
 const api=new LocalApi({sessions:{authenticate:async()=>{throw Object.assign(unauthenticated(),{stack:access});}},authorization:{},logger:r=>logs.push(r)});
 const r=await api.invoke({operation:'session',accessToken:access});assert.equal(r.status,401);
 assert.deepEqual(Object.keys(logs[0]).sort(),['code','event','requestId','status']);assert.equal(logs[0].event,'request.denied');assert.equal(logs[0].requestId,r.body.requestId);
 assert.ok(!JSON.stringify({r,logs}).includes(access));
});

const source={type:'reference',value:'Goldberg authored synthetic security fixture',license:'CC0-1.0',kind:'work',fields:['title'],intendedLicenses:['CC0-1.0']};
const validator=new Validator({fields:{work:{title:{type:'text',maxBytes:2048}}},intendedLicenses:['CC0-1.0'],sources:[source]});
const payload=()=>({schemaVersion:1,target:{kind:'work',catalogueUuid:randomUUID()},changeKind:'new_metadata',intendedLicense:'CC0-1.0',fields:[{field:'title',newValue:'Synthetic title',evidence:[{type:'reference',value:source.value,license:source.license}]}]});
for(const field of ['journal','practiceSessions','personalNotes','microphoneRecordings','backup','privateRepertoireHistory','profile']){
 test(`T21 server rejects private ${field} at every selected object boundary`,()=>{
  for(const position of ['root','target','field','evidence']){const p=payload();({root:p,target:p.target,field:p.fields[0],evidence:p.fields[0].evidence[0]})[position][field]='synthetic-private';assert.throws(()=>validator.payload(p),e=>e.status===400);}
 });
}
test('T13 malformed object getters nesting and size fail before any handler dispatch',async()=>{
 let calls=0;const api=new CommunityApi({extensions:{probe:async()=>{calls++;return {status:200,body:{}};}}});
 let deep={};for(let i=0;i<9;i++)deep={x:deep};
 const getter={};Object.defineProperty(getter,'x',{enumerable:true,get(){throw Error('getter must never execute');}});
 const cycle={};cycle.x=cycle;
 for(const body of [deep,getter,cycle,{x:'x'.repeat(16385)}])assert.equal((await api.invoke({operation:'probe',body})).status,400);
 assert.equal(calls,0);
});
test('T14 empty policy refuses synthetic evidence and URL policy is not an SSRF fetch pass',()=>{
 assert.throws(()=>new Validator().payload(payload()),e=>e.status===400);
 for(const value of ['https://169.254.169.254/source','https://localhost/source','https://10.0.0.1/source','https://[::1]/source','javascript:alert(1)','data:text/plain,x']){
  const p=payload();p.fields[0].evidence[0]={type:'source_url',value,license:'CC0-1.0'};assert.throws(()=>validator.payload(p),e=>e.status===400);
 }
});
test('T22 approved composed unknown audit mutation denies403 with no dispatch',async()=>{
 let calls=0;const api=new CommunityApi({extensions:{'admin.audit':async()=>{calls++;return {status:200,body:{}};}}});
 for(const accessToken of [undefined,'synthetic-admin-token']){
  const r=await api.invoke({operation:'admin.audit.edit',accessToken,body:{schemaVersion:1}});assert.equal(r.status,403);assert.equal(r.body.error.code,'forbidden');
 }
 assert.equal(calls,0); // Approved absent-operation403; no audit handler is exposed.
});
test('T9/T22 configured admin role route defers to live authorization and never opens storage when denied',async()=>{
 let connects=0,checks=0;
 const authorization={q:'"proto_security"',sessions:{},require:async(_token,cap)=>{checks++;assert.equal(cap,'roles.manage');throw forbidden();}};
 const v=compose({connect:async()=>{connects++;throw Error('must not connect');},schema:'proto_security',authorization,idempotency:{},validator,clock:()=>1893456000000,cursorKey:randomBytes(32),ipKey:randomBytes(32)});
 const r=await v.api.invoke({operation:'admin.roles',body:{schemaVersion:1,principalId:randomUUID(),role:'administrator',revoke:false,idempotencyKey:randomUUID()}});
 assert.equal(r.status,403);assert.equal(checks,1);assert.equal(connects,0); // Injected authorization; real SQL/self-grant proof remains in prior host tests.
});
