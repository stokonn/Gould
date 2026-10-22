import {test,before,after} from 'node:test';
import assert from 'node:assert/strict';
import {randomUUID,randomBytes} from 'node:crypto';
import {SignJWT} from 'jose';
import {localClient,migrate} from '../../stage6-schema-pg/src/database.mjs';
import {exportAudit,verifyAudit} from '../../stage6-schema-pg/src/audit.mjs';
import {migrateAuth,identifier,transaction} from '../src/storage.mjs';
import {IdentityVerifier} from '../src/identity.mjs';
import {SessionService,protect} from '../src/sessions.mjs';
import {Authorization} from '../src/authorization.mjs';
import {LocalApi} from '../src/local-api.mjs';
import {FakeClock,FakeJwks,makeKey,credential,issuer,audience} from './fixtures.mjs';
let admin,key1,key2;const schemas=[];
async function connect(){const c=localClient();await c.connect();return c;}
async function venue({extension=true}={}){
 const s='proto_'+randomUUID().replaceAll('-',''),q=identifier(s);schemas.push(s);
 if(extension)await migrateAuth(admin,s);else await migrate(admin,s);
 const clock=new FakeClock(),provider=new FakeJwks([key1.jwk]),logs=[];
 const verifier=new IdentityVerifier({issuer,audience,clock:clock.now,fetchJwks:provider.fetch,timeoutMs:50});
 const sessions=new SessionService({connect,schema:s,verifier,clock:clock.now,accessTtlMs:900000,refreshTtlMs:3600000,challengeTtlMs:60000});
 const authorization=new Authorization({sessions,clock:clock.now});const api=new LocalApi({sessions,authorization,logger:e=>logs.push(e)});
 return {s,q,clock,provider,verifier,sessions,authorization,api,logs};
}
async function user(v,subject='synthetic-user',roles=[]){
 const challenge=await v.sessions.challenge();const jwt=await credential(key1,v.clock,{subject,nonce:challenge.nonce});
 const session=await v.sessions.login({credential:jwt,challenge:challenge.challenge});
 if(roles.length){const operator=randomUUID();await admin.query(`INSERT INTO ${v.q}.principals(id,iss,sub,created_at) VALUES($1,'https://bootstrap.synthetic.invalid',$2,$3)`,[operator,randomUUID(),new Date(v.clock.now())]);
 for(const role of roles)await admin.query(`INSERT INTO ${v.q}.role_grants(id,principal_id,role,granted_by,granted_at) VALUES($1,$2,$3,$4,$5)`,[randomUUID(),session.principalId,role,operator,new Date(v.clock.now())]);}
 return {...session,jwt,challenge};
}
async function denied(promise,status){await assert.rejects(promise,e=>e.status===status);}
async function snapshot(v){const result={};for(const table of ['principals','auth_families','sessions','auth_refresh','role_grants','audit_events','audit_head'])result[table]=(await admin.query(`SELECT row_to_json(t)::text AS row FROM ${v.q}.${table} t ORDER BY row_to_json(t)::text`)).rows;return result;}
async function failAudit(v,on=true){if(on)await admin.query(`CREATE FUNCTION ${v.q}.test_audit_failure() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'synthetic failure with credential-like text'; END; $$; CREATE TRIGGER zz_test_fail BEFORE INSERT ON ${v.q}.audit_events FOR EACH ROW EXECUTE FUNCTION ${v.q}.test_audit_failure()`);else await admin.query(`DROP TRIGGER zz_test_fail ON ${v.q}.audit_events; DROP FUNCTION ${v.q}.test_audit_failure()`);}
async function submission(v,author){const id=randomUUID();await admin.query('BEGIN');try{await admin.query(`INSERT INTO ${v.q}.submissions(id,author_id,target_kind,catalogue_uuid,expected_revision,current_revision) VALUES($1,$2,'work',$3,0,1)`,[id,author,randomUUID()]);await admin.query(`INSERT INTO ${v.q}.submission_revisions(submission_id,revision,payload,prev_hash) VALUES($1,1,'{}',repeat('0',64))`,[id]);await admin.query('COMMIT');}catch(e){await admin.query('ROLLBACK');throw e;}return id;}
before(async()=>{admin=await connect();key1=await makeKey('synthetic-key-1');key2=await makeKey('synthetic-key-2');console.log('Auth venue:',(await admin.query('SELECT version() AS v')).rows[0].v);});
after(async()=>{if(admin){await admin.query('ROLLBACK');for(const s of schemas)await admin.query(`DROP SCHEMA IF EXISTS ${identifier(s)} CASCADE`);await admin.end();}});

test('Auth extension upgrades seeded base sessions and repeats without altering original migration checksums',async()=>{
 const v=await venue({extension:false}),id=randomUUID(),family=randomUUID(),session=randomUUID();
 await admin.query(`INSERT INTO ${v.q}.principals(id,iss,sub) VALUES($1,'https://seed.synthetic.invalid','seed')`,[id]);
 await admin.query(`INSERT INTO ${v.q}.sessions(id,principal_id,family_id,token_hash,issued_at,expires_at) VALUES($1,$2,$3,repeat('a',64),now(),now()+interval '1 hour')`,[session,id,family]);
 const before=(await admin.query(`SELECT * FROM ${v.q}.schema_migrations ORDER BY version`)).rows;
 await migrateAuth(admin,v.s);await migrateAuth(admin,v.s);assert.deepEqual((await admin.query(`SELECT * FROM ${v.q}.schema_migrations ORDER BY version`)).rows,before);
 assert.equal((await admin.query(`SELECT id FROM ${v.q}.auth_families`)).rows[0].id,family);assert.equal((await admin.query(`SELECT id FROM ${v.q}.sessions`)).rows[0].id,session);
});
test('incompatible cross-principal seeded family fails Auth migration atomically, preserving base data',async()=>{
 const v=await venue({extension:false}),family=randomUUID();for(let i=0;i<2;i++){const id=randomUUID();await admin.query(`INSERT INTO ${v.q}.principals(id,iss,sub) VALUES($1,'https://seed.synthetic.invalid',$2)`,[id,'seed-'+i]);await admin.query(`INSERT INTO ${v.q}.sessions(id,principal_id,family_id,token_hash,issued_at,expires_at) VALUES($1,$2,$3,$4,now(),now()+interval '1 hour')`,[randomUUID(),id,family,String(i).repeat(64)]);}
 await assert.rejects(migrateAuth(admin,v.s),e=>e.code==='23505');assert.equal((await admin.query(`SELECT count(*)::int AS n FROM ${v.q}.sessions`)).rows[0].n,2);
 assert.equal((await admin.query('SELECT to_regclass($1) AS name',[`${v.s}.auth_families`])).rows[0].name,null);
});
test('strict verified identity maps by issuer/subject; email/name/role claims grant no authority',async()=>{
 const v=await venue(),a=await user(v,'stable');const ch=await v.sessions.challenge();const jwt=await credential(key1,v.clock,{subject:'stable',nonce:ch.nonce,claims:{email:'not-stored@synthetic.invalid',name:'not stored',roles:['administrator']}});
 const b=await v.sessions.login({credential:jwt,challenge:ch.challenge});assert.equal(a.principalId,b.principalId);
 assert.equal((await admin.query(`SELECT count(*)::int AS n FROM ${v.q}.principals WHERE iss=$1 AND sub='stable'`,[issuer])).rows[0].n,1);
 assert.deepEqual(await v.authorization.capabilities(b.accessToken),[]);assert.equal((await v.api.invoke({operation:'audit.read',accessToken:b.accessToken})).status,403);
 const p=(await admin.query(`SELECT row_to_json(t)::text AS value FROM ${v.q}.principals t WHERE id=$1`,[a.principalId])).rows[0].value;assert.ok(!p.includes('not-stored'));
});
for(const [label,claims] of [
 ['wrong issuer',{iss:'https://other.synthetic.invalid'}],['wrong audience',{aud:'other-client'}],['expired',{exp:1893455999}],
 ['future iat',{iat:1893456061}],['missing expiry',{exp:undefined}],['missing subject',{sub:undefined}],['empty subject',{sub:''}],
 ['missing audience',{aud:undefined}],['missing nonce',{nonce:undefined}],['non-numeric expiry',{exp:'later'}],['expiry before issued',{exp:1893455999}],
 ['old credential',{iat:1893455600,exp:1893456200}]
])test(`T1/T4 strict claims reject ${label} with 401`,async()=>{
 const v=await venue();await denied(v.verifier.verify(await credential(key1,v.clock,{claims})),401);
 assert.equal((await admin.query(`SELECT count(*)::int AS n FROM ${v.q}.principals`)).rows[0].n,0);
});
test('T2 invalid signature, malformed/no token, none and HMAC confusion fail closed',async()=>{
 const v=await venue(),valid=await credential(key1,v.clock);const parts=valid.split('.');parts[2]=(parts[2][0]==='A'?'B':'A')+parts[2].slice(1);
 for(const bad of [undefined,null,'','bad','a.b.c',parts.join('.')])await denied(v.verifier.verify(bad),401);
 const claims={iss:issuer,aud:audience,sub:'synthetic',iat:1893456000,exp:1893456120,nonce:'synthetic'};
 const hs=await new SignJWT(claims).setProtectedHeader({alg:'HS256',kid:key1.kid,typ:'JWT'}).sign(randomBytes(32));await denied(v.verifier.verify(hs),401);
 const none=Buffer.from(JSON.stringify({alg:'none',kid:key1.kid,typ:'JWT'})).toString('base64url')+'.'+Buffer.from(JSON.stringify(claims)).toString('base64url')+'.';await denied(v.verifier.verify(none),401);
});
test('T2 unknown kid refreshes once; rotation succeeds; attacker URLs are never fetched',async()=>{
 const v=await venue();await v.verifier.verify(await credential(key1,v.clock));assert.equal(v.provider.calls,1);
 v.provider.keys=[key1.jwk,key2.jwk];await v.verifier.verify(await credential(key2,v.clock));assert.equal(v.provider.calls,2);
 await denied(v.verifier.verify(await credential(key2,v.clock,{header:{kid:'unknown'}})),401);assert.equal(v.provider.calls,2);
 await denied(v.verifier.verify(await credential(key1,v.clock,{header:{jku:'https://attacker.invalid/jwks'}})),401);assert.equal(v.provider.calls,2);
});
test('T2 bounded cache expiry and unavailable provider never fall back to stale keys',async()=>{
 const v=await venue(),jwt=await credential(key1,v.clock);await v.verifier.verify(jwt);v.provider.available=false;
 await v.verifier.verify(jwt);assert.equal(v.provider.calls,1);v.clock.advance(2000);await denied(v.verifier.verify(jwt),503);
 v.provider.available=true;await v.verifier.verify(jwt);assert.equal(v.provider.calls,3);
});
test('T2 cold unavailable/hanging provider, ambiguous/private keys and malformed JWKS deny',async()=>{
 const v=await venue();v.provider.available=false;await denied(v.verifier.verify(await credential(key1,v.clock)),503);
 v.provider.available=true;v.provider.hang=true;await denied(v.verifier.verify(await credential(key1,v.clock)),503);v.provider.hang=false;
 for(const keys of [[],[key1.jwk,key1.jwk],[{...key1.jwk,d:'private-not-allowed'}]]){const bad=new IdentityVerifier({issuer,audience,clock:v.clock.now,fetchJwks:async()=>({jwks:{keys},maxAgeSeconds:10})});await denied(bad.verify(await credential(key1,v.clock)),503);}
});
test('T2 concurrent cold verification coalesces one bounded JWKS request',async()=>{
 const v=await venue(),jwt=await credential(key1,v.clock);await Promise.all(Array.from({length:8},()=>v.verifier.verify(jwt)));assert.equal(v.provider.calls,1);
});
test('T3 nonce binding, expired challenge and credential replay cannot mint a second session',async()=>{
 const v=await venue(),ch=await v.sessions.challenge(),jwt=await credential(key1,v.clock,{nonce:ch.nonce});
 await denied(v.sessions.login({credential:await credential(key1,v.clock,{nonce:'wrong'}),challenge:ch.challenge}),401);
 await v.sessions.login({credential:jwt,challenge:ch.challenge});await denied(v.sessions.login({credential:jwt,challenge:ch.challenge}),401);
 const next=await v.sessions.challenge();await denied(v.sessions.login({credential:jwt,challenge:next.challenge}),401);
 v.clock.advance(60000);await denied(v.sessions.login({credential:await credential(key1,v.clock,{nonce:next.nonce}),challenge:next.challenge}),401);
 assert.equal((await admin.query(`SELECT count(*)::int AS n FROM ${v.q}.sessions`)).rows[0].n,1);
});
test('T3 concurrent identical credential exchange consumes challenge exactly once',async()=>{
 const v=await venue(),ch=await v.sessions.challenge(),args={credential:await credential(key1,v.clock,{nonce:ch.nonce}),challenge:ch.challenge};
 const results=await Promise.allSettled([v.sessions.login(args),v.sessions.login(args)]);assert.equal(results.filter(r=>r.status==='fulfilled').length,1);assert.equal(results.find(r=>r.status==='rejected').reason.status,401);
 assert.equal((await admin.query(`SELECT count(*)::int AS n FROM ${v.q}.sessions`)).rows[0].n,1);
});
test('tokens are CSPRNG opaque, distinct by purpose; only hashes persisted, no raw credentials/nonce',async()=>{
 const v=await venue(),a=await user(v);assert.match(a.accessToken,/^gs_access_[A-Za-z0-9_-]{43}$/);assert.match(a.refreshToken,/^gs_refresh_[A-Za-z0-9_-]{43}$/);assert.notEqual(a.accessToken,a.refreshToken);
 const persisted=JSON.stringify(await snapshot(v))+JSON.stringify((await admin.query(`SELECT * FROM ${v.q}.auth_challenges`)).rows);
 for(const secret of [a.accessToken,a.refreshToken,a.jwt,a.challenge.challenge])assert.ok(!persisted.includes(secret));
 assert.equal((await admin.query(`SELECT token_hash FROM ${v.q}.sessions`)).rows[0].token_hash,protect('access',a.accessToken));
 await denied(v.sessions.authenticate(a.refreshToken),401);await denied(v.sessions.refresh(a.accessToken),401);
});
test('T1 exact access expiry rejects; refresh can rotate before absolute family expiry',async()=>{
 const v=await venue(),a=await user(v);await v.sessions.authenticate(a.accessToken);v.clock.advance(900000);await denied(v.sessions.authenticate(a.accessToken),401);
 const b=await v.sessions.refresh(a.refreshToken);await v.sessions.authenticate(b.accessToken);v.clock.advance(2700000);await denied(v.sessions.refresh(b.refreshToken),401);await denied(v.sessions.authenticate(b.accessToken),401);
});
test('T3 refresh rotation revokes old access; replay commits family revocation and audit',async()=>{
 const v=await venue(),a=await user(v),b=await v.sessions.refresh(a.refreshToken);await denied(v.sessions.authenticate(a.accessToken),401);await v.sessions.authenticate(b.accessToken);
 await denied(v.sessions.refresh(a.refreshToken),401);await denied(v.sessions.authenticate(b.accessToken),401);await denied(v.sessions.refresh(b.refreshToken),401);
 assert.equal((await admin.query(`SELECT count(*)::int AS n FROM ${v.q}.audit_events WHERE action='session.refresh_replay'`)).rows[0].n,1);assert.equal(verifyAudit(await exportAudit(admin,v.s)),true);
});
test('T3 concurrent refresh has one winner and replay revokes even its newly issued access',async()=>{
 const v=await venue(),a=await user(v);const result=await Promise.allSettled([v.sessions.refresh(a.refreshToken),v.sessions.refresh(a.refreshToken)]);
 assert.equal(result.filter(r=>r.status==='fulfilled').length,1);assert.equal(result.find(r=>r.status==='rejected').reason.status,401);
 await denied(v.sessions.authenticate(result.find(r=>r.status==='fulfilled').value.accessToken),401);assert.equal((await admin.query(`SELECT count(*)::int AS n FROM ${v.q}.sessions`)).rows[0].n,2);
});
test('T1 logout is idempotent and family-scoped; other sign-in/account sessions remain isolated',async()=>{
 const v=await venue(),a=await user(v,'one'),b=await user(v,'one'),other=await user(v,'two');assert.equal(a.principalId,b.principalId);assert.notEqual(a.principalId,other.principalId);
 await v.sessions.logout(a.accessToken);await v.sessions.logout(a.accessToken);await denied(v.sessions.authenticate(a.accessToken),401);await denied(v.sessions.refresh(a.refreshToken),401);await v.sessions.authenticate(b.accessToken);await v.sessions.authenticate(other.accessToken);
 assert.equal((await v.api.invoke({operation:'logout',accessToken:a.accessToken})).status,204);
});
test('T5 valid token from suspended/deleted principal gets 403, expired/revoked token 401',async()=>{
 const v=await venue(),a=await user(v,'actor',['moderator']);
 for(const state of ['suspended','deleted']){await admin.query(`UPDATE ${v.q}.principals SET state=$2 WHERE id=$1`,[a.principalId,state]);await denied(v.sessions.authenticate(a.accessToken),403);await denied(v.authorization.require(a.accessToken,'queue.read'),403);}
 await admin.query(`UPDATE ${v.q}.principals SET state='active' WHERE id=$1`,[a.principalId]);await v.sessions.logout(a.accessToken);await denied(v.sessions.authenticate(a.accessToken),401);
});
test('T5 role revocation takes effect on next request despite a still-live session',async()=>{
 const v=await venue(),a=await user(v,'mod',['moderator']),manager=await user(v,'admin',['administrator']);
 await v.authorization.require(a.accessToken,'queue.read');await v.authorization.changeRole(manager.accessToken,{principalId:a.principalId,role:'moderator'},true);
 await v.sessions.authenticate(a.accessToken);await denied(v.authorization.require(a.accessToken,'queue.read'),403);
});
test('T6 default deny, empty roles, unknown capability and JWT role injection',async()=>{
 const v=await venue(),a=await user(v);assert.deepEqual(await v.authorization.capabilities(a.accessToken),[]);
 for(const cap of ['submit','queue.read','roles.manage','deployment.manage','unknown'])await denied(v.authorization.require(a.accessToken,cap),403);
 assert.equal((await v.api.invoke({operation:'roles.grant',accessToken:a.accessToken,body:{principalId:a.principalId,role:'administrator'}})).status,403);
});
test('T6 strict allowlist rejects forged role/author/status in protected resource requests',async()=>{
 const v=await venue(),a=await user(v,'contributor',['contributor']),id=await submission(v,a.principalId),before=await snapshot(v);
 for(const extra of [{role:'administrator'},{author_id:randomUUID()},{status:'published'},{capabilities:['roles.manage']}]){
  const result=await v.api.invoke({operation:'submission.read',accessToken:a.accessToken,body:{id,...extra}});assert.equal(result.status,400);assert.equal(result.body.error.code,'validation_failed');}
 assert.deepEqual(await snapshot(v),before);
 assert.throws(()=>Authorization.contributorBody({fields:[],roles:['administrator']},['fields']),e=>e.status===400);
});
test('T7/T8 ownership in query hides other private submissions and denies cross-user writes',async()=>{
 const v=await venue(),a=await user(v,'one',['contributor']),b=await user(v,'two',['contributor']),id=await submission(v,a.principalId);
 assert.equal((await v.authorization.submission(a.accessToken,id)).author_id,a.principalId);
 await denied(v.authorization.submission(b.accessToken,id),404);await denied(v.authorization.submission(b.accessToken,id,'revise'),404);
 await denied(v.authorization.submission(b.accessToken,randomUUID()),404);assert.equal((await admin.query(`SELECT status FROM ${v.q}.submissions WHERE id=$1`,[id])).rows[0].status,'submitted');
});
test('T8/T22 moderator/admin capabilities and actor != author enforced server-side',async()=>{
 const v=await venue(),a=await user(v,'contributor',['contributor']),m=await user(v,'moderator',['moderator']),manager=await user(v,'admin',['administrator']),id=await submission(v,a.principalId);
 await v.authorization.submission(m.accessToken,id,'decide');await v.authorization.submission(manager.accessToken,id,'decide');await denied(v.authorization.submission(a.accessToken,id,'decide'),403);
 const own=await submission(v,m.principalId);await denied(v.authorization.submission(m.accessToken,own,'decide'),403);await denied(v.authorization.require(m.accessToken,'roles.manage'),403);await denied(v.authorization.require(m.accessToken,'audit.read'),403);
});
test('T22 admin cannot self-grant; operator capability/role stays outside application roles',async()=>{
 const v=await venue(),manager=await user(v,'admin',['administrator']),target=await user(v,'other');const before=await snapshot(v);
 await denied(v.authorization.changeRole(manager.accessToken,{principalId:manager.principalId,role:'moderator'}),403);
 await denied(v.authorization.require(manager.accessToken,'deployment.manage'),403);
 await denied(v.authorization.changeRole(manager.accessToken,{principalId:target.principalId,role:'deployment_operator'}),400);assert.deepEqual(await snapshot(v),before);
 await v.authorization.changeRole(manager.accessToken,{principalId:target.principalId,role:'contributor'});assert.ok((await v.authorization.capabilities(target.accessToken)).includes('submit'));
});
test('T5 administrator suspension is immediate, audited and cannot resurrect deleted principal',async()=>{
 const v=await venue(),manager=await user(v,'admin',['administrator']),target=await user(v,'target',['contributor']);
 await v.authorization.suspend(manager.accessToken,{principalId:target.principalId,suspended:true});await denied(v.sessions.authenticate(target.accessToken),403);
 await v.authorization.suspend(manager.accessToken,{principalId:target.principalId,suspended:false});await v.sessions.authenticate(target.accessToken);
 await admin.query(`UPDATE ${v.q}.principals SET state='deleted' WHERE id=$1`,[target.principalId]);await denied(v.authorization.suspend(manager.accessToken,{principalId:target.principalId,suspended:false}),404);
});
test('T9/T22 public database role has no private-table or privileged-function access',async()=>{
 const v=await venue(),u=await user(v),role='proto_anon_'+randomUUID().replaceAll('-','');await admin.query(`CREATE ROLE ${role} NOLOGIN`);
 try{await admin.query(`GRANT USAGE ON SCHEMA ${v.q} TO ${role}`);await admin.query(`SET ROLE ${role}`);
  for(const table of ['submissions','sessions','role_grants','audit_events','auth_families','auth_refresh','auth_challenges'])await assert.rejects(admin.query(`SELECT * FROM ${v.q}.${table}`),e=>e.code==='42501');
  await assert.rejects(admin.query(`SELECT ${v.q}.append_audit($1,'attack','principal',$1,NULL,$2)`,[u.principalId,randomUUID()]),e=>e.code==='42501');
  await assert.rejects(admin.query(`UPDATE ${v.q}.audit_events SET reason='attack'`),e=>e.code==='42501');
 }finally{await admin.query('RESET ROLE');await admin.query(`DROP OWNED BY ${role}`);await admin.query(`DROP ROLE ${role}`);}
});
test('T9/T22 local transport exposes no audit mutation or arbitrary RPC, even to admin',async()=>{
 const v=await venue(),a=await user(v,'admin',['administrator']);for(const operation of ['audit.update','rpc','deployment','sessions.list','roles.list'])assert.equal((await v.api.invoke({operation,accessToken:a.accessToken,body:{}})).status,403);
 for(const operation of ['session','audit.read'])assert.equal((await v.api.invoke({operation})).status,401);
 assert.equal(verifyAudit(await exportAudit(admin,v.s)),true);
});
test('login audit failure rolls back identity/challenge/family/session atomically, then same credential retries',async()=>{
 const v=await venue(),ch=await v.sessions.challenge(),args={credential:await credential(key1,v.clock,{nonce:ch.nonce}),challenge:ch.challenge};await failAudit(v);
 await denied(v.sessions.login(args),503);assert.equal((await admin.query(`SELECT count(*)::int AS n FROM ${v.q}.principals`)).rows[0].n,0);assert.equal((await admin.query(`SELECT consumed_at FROM ${v.q}.auth_challenges`)).rows[0].consumed_at,null);
 for(const table of ['auth_families','sessions','auth_refresh','audit_events'])assert.equal((await admin.query(`SELECT count(*)::int AS n FROM ${v.q}.${table}`)).rows[0].n,0);
 await failAudit(v,false);await v.sessions.login(args);assert.equal(verifyAudit(await exportAudit(admin,v.s)),true);
});
test('refresh audit failure rolls back consumption/revocation/new tokens and preserves old session',async()=>{
 const v=await venue(),a=await user(v),before=await snapshot(v);await failAudit(v);await denied(v.sessions.refresh(a.refreshToken),503);assert.deepEqual(await snapshot(v),before);await v.sessions.authenticate(a.accessToken);await failAudit(v,false);await v.sessions.refresh(a.refreshToken);
});
test('logout and admin role changes roll back when mandatory audit append fails',async()=>{
 const v=await venue(),manager=await user(v,'admin',['administrator']),target=await user(v,'other');const before=await snapshot(v);await failAudit(v);
 await denied(v.sessions.logout(target.accessToken),503);await denied(v.authorization.changeRole(manager.accessToken,{principalId:target.principalId,role:'moderator'}),503);assert.deepEqual(await snapshot(v),before);await failAudit(v,false);await v.sessions.authenticate(target.accessToken);
});
test('secret-safe error envelope/logs distinguish 401/403/400/503 without request/token/SQL leaks',async()=>{
 const v=await venue(),a=await user(v,'sensitive-subject',['contributor']);
 const cases=[{operation:'session',accessToken:'secret-malformed-token'},{operation:'audit.read',accessToken:a.accessToken},{operation:'session',accessToken:a.accessToken,body:{role:'administrator'}}];
 const responses=[];for(const request of cases)responses.push(await v.api.invoke(request));assert.deepEqual(responses.map(r=>r.status),[401,403,400]);
 const broken=new SessionService({connect:async()=>{throw new Error('database password=secret fake SQL');},schema:v.s,verifier:v.verifier,clock:v.clock.now,accessTtlMs:100,refreshTtlMs:1000,challengeTtlMs:100});
 const api=new LocalApi({sessions:broken,authorization:v.authorization,logger:e=>v.logs.push(e)});responses.push(await api.invoke({operation:'session',accessToken:a.accessToken}));assert.equal(responses[3].status,503);
 const emitted=JSON.stringify({logs:v.logs,responses});for(const secret of [a.accessToken,a.refreshToken,a.jwt,'secret-malformed-token','password=','fake SQL','sensitive-subject'])assert.ok(!emitted.includes(secret),secret);
 for(const r of responses){assert.equal(r.body.requestId,r.body.error.requestId);assert.equal(typeof r.body.error.code,'string');}
});

test('self-grant and self-suspension cannot bypass guards using uppercase UUID spellings',async()=>{
 const v=await venue(),a=await user(v,'admin',['administrator']);
 await denied(v.authorization.changeRole(a.accessToken,{principalId:a.principalId.toUpperCase(),role:'contributor'}),403);
 await denied(v.authorization.suspend(a.accessToken,{principalId:a.principalId.toUpperCase(),suspended:true}),403);
});
test('issuer/subject identity isolation includes identical subject under another configured issuer',async()=>{
 const v=await venue(),a=await user(v,'same-subject'),otherIssuer='https://other-provider.synthetic.invalid';
 const verifier=new IdentityVerifier({issuer:otherIssuer,audience,fetchJwks:v.provider.fetch,clock:v.clock.now});
 const sessions=new SessionService({connect,schema:v.s,verifier,clock:v.clock.now,accessTtlMs:900000,refreshTtlMs:3600000,challengeTtlMs:60000});
 const ch=await sessions.challenge(),jwt=await credential(key1,v.clock,{subject:'same-subject',nonce:ch.nonce,claims:{iss:otherIssuer}});
 const b=await sessions.login({credential:jwt,challenge:ch.challenge});assert.notEqual(a.principalId,b.principalId);
 assert.equal((await admin.query(`SELECT count(*)::int AS n FROM ${v.q}.principals WHERE sub='same-subject'`)).rows[0].n,2);
});
test('strict audience is the exact configured value, not an unexpected multi-audience envelope',async()=>{
 const v=await venue();await denied(v.verifier.verify(await credential(key1,v.clock,{claims:{aud:[audience,'other-client']}})),401);
});
test('clock rollback cannot activate sessions or refresh records before issuance',async()=>{
 const v=await venue(),a=await user(v);v.clock.advance(-1000);await denied(v.sessions.authenticate(a.accessToken),401);await denied(v.sessions.refresh(a.refreshToken),401);
});
test('configured ES256 verification uses public EC keys and still rejects RSA/HS algorithm substitutions',async()=>{
 const {generateKeyPair,exportJWK}=await import('jose');const ec=await generateKeyPair('ES256'),jwk={...await exportJWK(ec.publicKey),kid:'synthetic-ec',alg:'ES256',use:'sig'};
 const clock=new FakeClock(),provider=new FakeJwks([jwk]);const verifier=new IdentityVerifier({issuer,audience,algorithms:['ES256'],clock:clock.now,fetchJwks:provider.fetch});
 const token=await credential({...ec,kid:jwk.kid},clock,{header:{alg:'ES256'}});assert.equal((await verifier.verify(token)).subject,'synthetic-subject');
 await denied(verifier.verify(await credential(key1,clock)),401);
});
test('concurrent identity creation from separate challenges converges to one principal and two isolated families',async()=>{
 const v=await venue(),a=await v.sessions.challenge(),b=await v.sessions.challenge();
 const results=await Promise.all([v.sessions.login({credential:await credential(key1,v.clock,{subject:'same',nonce:a.nonce}),challenge:a.challenge}),v.sessions.login({credential:await credential(key1,v.clock,{subject:'same',nonce:b.nonce}),challenge:b.challenge})]);
 assert.equal(results[0].principalId,results[1].principalId);assert.notEqual(results[0].accessToken,results[1].accessToken);assert.equal((await admin.query(`SELECT count(*)::int AS n FROM ${v.q}.auth_families`)).rows[0].n,2);
});
test('malformed credential and provider outage create no identity/session or consumed challenge',async()=>{
 const v=await venue(),ch=await v.sessions.challenge();v.provider.available=false;
 const result=await v.api.invoke({operation:'login',body:{credential:await credential(key1,v.clock,{nonce:ch.nonce}),challenge:ch.challenge}});assert.equal(result.status,503);
 assert.equal((await admin.query(`SELECT count(*)::int AS n FROM ${v.q}.principals`)).rows[0].n,0);assert.equal((await admin.query(`SELECT consumed_at FROM ${v.q}.auth_challenges`)).rows[0].consumed_at,null);
 v.provider.available=true;assert.equal((await v.api.invoke({operation:'login',body:{credential:'malformed',challenge:ch.challenge}})).status,401);
});

test('unavailable/malformed clock is a 503 denial, never a bypass of expiry or JWT dates',async()=>{
 const v=await venue(),a=await user(v,'user',['contributor']),jwt=await credential(key1,v.clock);v.clock.ms=NaN;
 await denied(v.sessions.authenticate(a.accessToken),503);await denied(v.sessions.refresh(a.refreshToken),503);await denied(v.verifier.verify(jwt),503);
 assert.equal((await v.api.invoke({operation:'capabilities',accessToken:a.accessToken})).status,503);
});
test('future-dated role grants are not effective before their server timestamp',async()=>{
 const v=await venue(),a=await user(v,'mod',['moderator']);
 // Separate test-only SQL fixture, not an application role-history rewrite path.
 await admin.query(`INSERT INTO ${v.q}.role_grants(id,principal_id,role,granted_by,granted_at) VALUES($1,$2,'administrator',$3,$4)`,[randomUUID(),a.principalId,(await user(v,'grantor')).principalId,new Date(v.clock.now()+1000)]);
 await denied(v.authorization.require(a.accessToken,'roles.manage'),403);v.clock.advance(1000);await v.authorization.require(a.accessToken,'roles.manage');
});
