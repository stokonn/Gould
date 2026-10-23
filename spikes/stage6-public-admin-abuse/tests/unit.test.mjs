import {test} from 'node:test';
import assert from 'node:assert/strict';
import {randomBytes,randomUUID} from 'node:crypto';
import {Cursors,decimal} from '../src/cursor.mjs';
import {coarseAddress,AbuseProtection} from '../src/abuse.mjs';
import {display} from '../src/catalogue.mjs';
import {CommunityApi} from '../../stage6-handlers-local/src/local-api.mjs';
import {Validator} from '../../stage6-handlers-local/src/validation.mjs';
const key=randomBytes(32);
for(const value of ['0','9','10','9007199254740993','9223372036854775807'])test(`lossless int8 decimal ${value}`,()=>assert.equal(decimal(value),value));
for(const value of [1,9007199254740992,'01','-1','1e3','9223372036854775808',null])test(`invalid int8 ${value}`,()=>assert.throws(()=>decimal(value),e=>e.status===400));
test('signed cursors reject alteration, cross endpoint/filter and cross server key',()=>{
 const c=new Cursors(key),v={version:1,scope:'catalogue.overlay:work',high:'9223372036854775807',id:randomUUID()},token=c.sign(v);assert.deepEqual(c.read(token,v.scope),v);
 for(const t of [token.slice(0,-5)+'aaaaa','x',token+'.x',token.slice(1),'x'.repeat(2049)])assert.throws(()=>c.read(t,v.scope),e=>e.status===400);
 assert.throws(()=>c.read(token,'catalogue.changes:'));assert.throws(()=>new Cursors(randomBytes(32)).read(token,v.scope));
});
for(const [ip,coarse] of [
 ['198.51.100.1','198.51.100.0/24'],['198.51.100.254','198.51.100.0/24'],
 ['2001:db8:1:2::a','2001:0db8:0001:0002::/64'],['2001:0db8:0001:0002:ffff:ffff:ffff:ffff','2001:0db8:0001:0002::/64'],
 ['::ffff:198.51.100.1','198.51.100.0/24'],['::ffff:c633:6401','198.51.100.0/24']
])test('synthetic coarse grouping '+ip,()=>assert.equal(coarseAddress(ip),coarse));
for(const ip of ['198.51.100.01','unknown','2001:db8::1%zone','198.51.100.1, 198.51.100.2',undefined])test('invalid trusted peer '+ip,()=>assert.throws(()=>coarseAddress(ip),e=>e.status===400));
test('keyed hashes hide peer, preserve group, separate groups and key rotations',()=>{
 const a=new AbuseProtection({schema:'proto_unit',clock:()=>0,key}),b=new AbuseProtection({schema:'proto_unit',clock:()=>0,key:randomBytes(32)});
 assert.match(a.hash('198.51.100.1'),/^[0-9a-f]{64}$/);assert.equal(a.hash('198.51.100.1'),a.hash('198.51.100.2'));assert.notEqual(a.hash('198.51.100.1'),a.hash('198.51.101.1'));assert.notEqual(a.hash('198.51.100.1'),b.hash('198.51.100.1'));
});
test('public projection selects only safe display/provenance, retaining exact revision',()=>{
 const r={entity_id:randomUUID(),kind:'work',revision:'9223372036854775807',payload:{contributorNote:'PRIVATE',author_id:'PRIVATE',fields:[{field:'title',newValue:'Synthetic title',evidence:[{value:'PRIVATE'}]}]},source_evidence:[{license:'CC0-1.0',value:'PRIVATE'}],actor_id:'PRIVATE',reason:'PRIVATE'};
 const out=display(r);assert.equal(out.revision,r.revision);assert.equal(out.catalogueUuid,r.entity_id);assert.equal(out.displayTitle,'Synthetic title');assert.ok(!JSON.stringify(out).includes('PRIVATE'));assert.deepEqual(out.provenanceSummary,{licenses:['CC0-1.0'],sourceCount:1});
 assert.throws(()=>display({...r,kind:'practice'}));assert.throws(()=>display({...r,revision:'9223372036854775808'}));
});
test('adapter propagates truthful retry and safe failure envelope, never diagnostics',async()=>{
 const a=new AbuseProtection({schema:'proto_unit',clock:()=>0,key}),api=new CommunityApi({extensions:{limited:async()=>{throw a.limited(1.1);},unavailable:async()=>{throw new Error('PRIVATE backend state');}}});
 const r=await api.invoke({operation:'limited',body:{schemaVersion:1}});assert.equal(r.status,429);assert.equal(r.headers['Retry-After'],'2');assert.equal(r.body.error.retryAfterSeconds,2);
 const u=await api.invoke({operation:'unavailable',body:{schemaVersion:1}});assert.equal(u.status,503);assert.ok(!JSON.stringify(u).includes('PRIVATE'));assert.equal((await api.invoke({operation:'admin.audit.edit',body:{schemaVersion:1}})).status,400);
});
test('fail-closed evidence policy remains empty and no evidence fetch abstraction is added',()=>{
 assert.throws(()=>new Validator().payload({schemaVersion:1,target:{kind:'work',catalogueUuid:randomUUID()},changeKind:'new_metadata',intendedLicense:'CC0-1.0',fields:[]}),e=>e.status===400);
});

test('T18 independent audit verification rejects tamper, omission, sequence reordering and head mismatch',async()=>{
 const {verifyAudit,digest}=await import('../../stage6-handlers-local/src/audit.mjs');
 let prev='0'.repeat(64);const events=[];
 for(let i=1;i<=12;i++){
  const e={seq:String(i),actor_id:randomUUID(),action:'synthetic.action',target_kind:'principal',target_id:randomUUID(),target_revision:null,before_hash:null,after_hash:null,reason:null,created_at:'2030-01-01T00:00:00.000000Z',request_id:randomUUID(),prev_hash:prev};
  e.canonical_content='['+['goldberg-audit-v1',e.seq,e.actor_id,e.action,e.target_kind,e.target_id,e.target_revision,e.before_hash,e.after_hash,e.reason,e.created_at,e.request_id].map(v=>JSON.stringify(v)).join(', ')+']';
  e.this_hash=digest(prev+':'+e.canonical_content);events.push(e);prev=e.this_hash;
 }
 const v={version:1,events,head:{last_seq:'12',last_hash:prev}};assert.equal(verifyAudit(v),true);
 for(const mutate of [x=>x.events[1].action='tampered',x=>x.events.splice(3,1),x=>x.events.sort((a,b)=>a.seq.localeCompare(b.seq)),x=>x.head.last_hash='0'.repeat(64)]){const x=structuredClone(v);mutate(x);assert.throws(()=>verifyAudit(x));}
});
test('T20 unavailable PostgreSQL connector fails public read closed with safe 503',async()=>{
 const {CatalogueService}=await import('../src/catalogue.mjs');
 const catalogue=new CatalogueService({connect:async()=>{throw Error('PRIVATE storage unavailable');},schema:'proto_unavailable',cursors:new Cursors(key)});
 const api=new CommunityApi({extensions:{'catalogue.overlay':(_t,b)=>catalogue.read('catalogue.overlay',b)}});
 const r=await api.invoke({operation:'catalogue.overlay',body:{schemaVersion:1}});assert.equal(r.status,503);assert.ok(!JSON.stringify(r).includes('PRIVATE'));
});
