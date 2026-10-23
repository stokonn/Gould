// Actual service output over deliberately scripted storage; no HTTP/PostgreSQL acceptance.
import {writeFile} from 'node:fs/promises';
import {randomUUID} from 'node:crypto';
import {venue,body,invoke} from './tests/t22-fixture.mjs';
import {AbuseProtection} from '../stage6-public-admin-abuse/src/abuse.mjs';
import {CommunityApi} from '../stage6-handlers-local/src/local-api.mjs';
import {Validator} from '../stage6-handlers-local/src/validation.mjs';
import {decimal} from '../stage6-public-admin-abuse/src/cursor.mjs';
import {display} from '../stage6-public-admin-abuse/src/catalogue.mjs';
const uuid=n=>'00000000-0000-4000-8000-'+String(n).padStart(12,'0');
const requestId=uuid(99),cases=[];
function normalize(value){const ids=new Map();let n=1000;const walk=x=>{if(Array.isArray(x))return x.map(walk);if(x&&typeof x==='object')return Object.fromEntries(Object.entries(x).map(([k,v])=>[k,k==='requestId'?requestId:walk(v)]));if(typeof x==='string'&&/^[a-f0-9-]{36}$/i.test(x)&&!x.startsWith('00000000-0000-4000-8000-')){if(!ids.has(x))ids.set(x,uuid(n++));return ids.get(x);}return x;};return walk(value);}
async function capture(id,operation,requestBody,run,scope='real in-process adapter/service + scripted SQL'){
 const response=normalize(await run());cases.push({id,classification:'actual-local-capture',synthetic:true,operation,httpBinding:null,requestBody,scope,verification:{executed:true,postgres:false,http:false,identifiers:'dynamic response UUIDs normalized; no credentials captured'},response});return response;
}
let v=venue();await capture('actual-capabilities','capabilities',{},()=>v.authApi.invoke({operation:'capabilities',accessToken:v.accessToken,body:{}}));
v=venue();await capture('actual-auth-schemaVersion-rejected','capabilities',{schemaVersion:1},()=>v.authApi.invoke({operation:'capabilities',accessToken:v.accessToken,body:{schemaVersion:1}}));
v=venue();await capture('actual-session','session',{},()=>v.authApi.invoke({operation:'session',accessToken:v.accessToken,body:{}}));
v=venue({revoked:true});await capture('actual-logout-204','logout',{},()=>v.authApi.invoke({operation:'logout',accessToken:v.accessToken,body:{}}));
for(const [id,operation,b,opts,token] of [
 ['actual-error-400','admin.roles',{schemaVersion:1}, {},null],
 ['actual-error-401','admin.roles',null,{},undefined],
 ['actual-error-403','admin.audit.edit',{schemaVersion:1},{},null],
 ['actual-role-revoked','admin.roles',null,{role:null},null],
 ['actual-service-unavailable','admin.roles',null,{unavailable:true},null],
 ['actual-queue-filter-rejected','moderation.queue',{schemaVersion:1,status:'submitted'},{},null],
 ['actual-audit-search-rejected','admin.audit',{schemaVersion:1,search:'synthetic'},{},null]
]){v=venue(opts);const req=b??body(v);await capture(id,operation,normalize(req),()=>v.api.invoke({operation,body:req,...(token===undefined?{}:{accessToken:v.accessToken})}));}
v=venue();await capture('actual-admin-audit-empty','admin.audit',{schemaVersion:1},()=>invoke(v,'admin.audit',{schemaVersion:1}));
let rows=Array.from({length:21},(_,i)=>({id:uuid(i+1),target_kind:'work',status:'submitted',current_revision:'1',createdAt:'2030-01-01T00:00:00.000001Z'}));
v=venue({extraQuery:async sql=>{if(sql.startsWith('SELECT id,target_kind'))return {rows};throw Error('unhandled capture SQL');}});
await capture('actual-queue-page','moderation.queue',{schemaVersion:1},()=>invoke(v,'moderation.queue',{schemaVersion:1}));
rows=[];await capture('actual-queue-empty','moderation.queue',{schemaVersion:1},()=>invoke(v,'moderation.queue',{schemaVersion:1}));
const evidence={type:'reference',value:'Goldberg authored synthetic contract fixture',license:'CC0-1.0'};
const payload={schemaVersion:1,target:{kind:'work',catalogueUuid:uuid(7)},changeKind:'correction',intendedLicense:'CC0-1.0',fields:[{field:'title',newValue:'Synthetic after',evidence:[evidence]}]};
const policy={fields:{work:{title:{type:'text',maxBytes:2048}}},intendedLicenses:['CC0-1.0'],sources:[{...evidence,kind:'work',fields:['title'],intendedLicenses:['CC0-1.0']}]};
function moderationVenue({status='in_review',current='3',expected='7',absent=false}={}){
 const s={id:uuid(2),author_id:uuid(6),target_kind:'work',catalogue_uuid:uuid(7),mapping_id:null,target_entity_id:uuid(7),expected_revision:expected,status,current_revision:current,created_at:'2030-01-01T00:00:00.000Z',updated_at:'2030-01-01T00:00:00.000Z'};
 let stored=null,seq=10;
 const v=venue({extraQuery:async(sql,args)=>{
  if(sql.includes('.idempotency_results')){if(sql.startsWith('SELECT'))return {rows:stored?[stored]:[]};if(sql.startsWith('INSERT'))stored={request_hash:args[2],http_status:args[3],response:args[4],expires_at:new Date('2030-01-02T00:00:00Z')};return {rows:[],rowCount:1};}
  if(sql.includes('SELECT enabled'))return {rows:[{enabled:true}]};
  if(sql.startsWith('SELECT s.*')||sql.startsWith('SELECT * FROM')&&sql.includes('.submissions WHERE'))return {rows:absent?[]:[structuredClone(s)]};
  if(sql.includes('.submission_revisions WHERE'))return {rows:[{revision:current,payload,this_hash:'a'.repeat(64),prev_hash:'0'.repeat(64)}]};
  if(sql.includes('.review_events WHERE'))return {rows:[{from_status:'submitted',to_status:'in_review',reason:null,audit_seq:'10'}]};
  if(sql.includes('.published_entities WHERE'))return {rows:[{id:uuid(7),kind:'work',revision:expected,status:'published'}]};
  if(sql.includes('coalesce(max(revision),0)'))return {rows:[{n:expected}]};
  if(sql.includes('.append_audit('))return {rows:[{seq:String(++seq)}]};
  if(sql.startsWith('UPDATE')&&sql.includes('.submissions'))s.status=args[1];
  if(sql.startsWith('INSERT')||sql.startsWith('UPDATE')||sql.startsWith('RELEASE')||sql.startsWith('SAVEPOINT'))return {rows:[],rowCount:1};
  throw Error('unhandled moderation capture SQL: '+sql);
 }});
 // Capture replay with the actual idempotency engine and explicit stored-result SQL seam.
 v.moderation.validator=new Validator(policy);
 return v;
}
v=moderationVenue();await capture('actual-detail','submission.read',{schemaVersion:1,id:uuid(2)},()=>invoke(v,'submission.read',{schemaVersion:1,id:uuid(2)}));
v=moderationVenue({absent:true});await capture('actual-error-404','submission.read',{schemaVersion:1,id:uuid(2)},()=>invoke(v,'submission.read',{schemaVersion:1,id:uuid(2)}));
for(const decision of ['approve','reject','request_changes']){
 v=moderationVenue();const req={schemaVersion:1,id:uuid(2),expectedRevision:7,expectedSubmissionRevision:3,idempotencyKey:uuid(8),decision,...(decision==='approve'?{}:{reason:'Synthetic reason'})};
 await capture('actual-decision-'+decision,'moderation.decide',req,()=>invoke(v,'moderation.decide',req));
 if(decision==='approve'){await capture('actual-approve-replay','moderation.decide',req,()=>invoke(v,'moderation.decide',req));cases.at(-1).replayOf='actual-decision-approve';await capture('actual-error-422','moderation.decide',{...req,reason:'Changed synthetic body'},()=>invoke(v,'moderation.decide',{...req,reason:'Changed synthetic body'}));}
}
v=moderationVenue({status:'submitted',current:'1',expected:'0'});const open={schemaVersion:1,id:uuid(2),expectedRevision:0,expectedSubmissionRevision:1,idempotencyKey:uuid(8)};
await capture('actual-open','moderation.open',open,()=>invoke(v,'moderation.open',open));
v=moderationVenue();const stale={...open,expectedRevision:6,expectedSubmissionRevision:3,decision:'approve'};
await capture('actual-error-409','moderation.decide',stale,()=>invoke(v,'moderation.decide',stale));
// Error factories are real, but injected effects test serialization only; no quota/SQL claim.
const abuse=new AbuseProtection({schema:'proto_fixture',clock:()=>0,key:Buffer.alloc(32,7)});
const quotaApi=new CommunityApi({submissions:{create:async()=>{throw abuse.limited(1.1);}}});await capture('actual-error-429','submission.create',{schemaVersion:1},()=>quotaApi.invoke({operation:'submission.create',body:{schemaVersion:1}}),'actual adapter/AbuseProtection error serialization; effect injected, no quota storage execution');
const {digest,verifyAudit}=await import('../stage6-handlers-local/src/audit.mjs');
const auditRows=[];let previous='0'.repeat(64);
for(let i=1;i<=21;i++){
 const e={seq:String(i),actor_id:uuid(3),action:'submitted->in_review',target_kind:'submission',target_id:uuid(2),target_revision:'3',before_hash:null,after_hash:null,reason:null,created_at:'2030-01-01T00:00:00.000000Z',request_id:uuid(99),prev_hash:previous};
 e.canonical_content='['+['goldberg-audit-v1',e.seq,e.actor_id,e.action,e.target_kind,e.target_id,e.target_revision,e.before_hash,e.after_hash,e.reason,e.created_at,e.request_id].map(x=>JSON.stringify(x)).join(', ')+']';e.this_hash=digest(previous+':'+e.canonical_content);previous=e.this_hash;auditRows.push(e);
}
const head={last_seq:'21',last_hash:previous};
v=venue({cursorKey:Buffer.alloc(32,7),extraQuery:async(sql,args)=>sql.includes('.audit_head')?{rows:[head]}:{rows:auditRows.filter(e=>BigInt(e.seq)>BigInt(args[0])&&BigInt(e.seq)<=BigInt(args[1]))}});
const first=await capture('actual-audit-page-1','admin.audit',{schemaVersion:1},()=>invoke(v,'admin.audit',{schemaVersion:1}));
const second=await capture('actual-audit-page-2','admin.audit',{schemaVersion:1,cursor:first.body.nextCursor},()=>invoke(v,'admin.audit',{schemaVersion:1,cursor:first.body.nextCursor}));
if(!verifyAudit({version:1,events:[...first.body.items,...second.body.items],head:first.body.head}))throw Error('synthetic chain verification failed');
for(const revision of ['7','8']){
 const row={entity_id:uuid(7),kind:'work',revision,payload:{fields:[{field:'title',newValue:revision==='7'?'Synthetic before':'Synthetic after'}]},source_evidence:[evidence]};
 cases.push({id:'actual-public-display-'+revision,classification:'actual-local-capture',synthetic:true,operation:'display',httpBinding:null,scope:'actual public projection function over authored row; not current entity lookup',verification:{executed:true,postgres:false,http:false},value:display(row)});
}
for(const value of ['9007199254740993','9223372036854775807'])cases.push({id:'actual-bigint-'+value,classification:'actual-local-capture',synthetic:true,operation:'decimal',httpBinding:null,scope:'actual lossless decimal primitive; not full audit chain',verification:{executed:true,postgres:false,http:false},value:decimal(value)});
const proposals=[
 {id:'proposal-stats-snapshot',gap:'G1',proposal:{snapshotId:'synthetic',computedAt:'2030-01-01T00:00:00Z',window:{start:'2030-01-01T00:00:00Z',end:'2030-01-02T00:00:00Z',timezone:'OWNER_DECISION_REQUIRED'},counts:{pending:2,needsInfo:1,approved:1,rejected:0},complete:true}},
 {id:'proposal-queue-search',gap:'G2',proposal:{search:'synthetic',status:['submitted'],fields:['title','composer','targetKind'],cursorBinding:'filter and snapshot digest required'}},
 {id:'proposal-audit-search',gap:'G3',proposal:{action:['approved->published'],query:'synthetic',cursorBinding:'signed filter/snapshot scope required'}},
 {id:'proposal-current-entity-values',gap:'G4',proposal:{catalogueUuid:uuid(7),revision:'7',status:'published',fields:{title:'Synthetic before'},authority:'explicit admin current projection required; not earlier proposal'}},
 {id:'proposal-session-http-lifecycle',gap:'G5',proposal:{httpBinding:null,sourceOperations:['challenge','login','refresh','logout'],loginBody:{credential:'SYNTHETIC_NOT_A_REAL_CREDENTIAL',challenge:'SYNTHETIC_CHALLENGE'},tokenResponseFields:['accessToken','refreshToken','principalId','expiresAt'],refreshReplay:'revoke family and audit; exact lifecycle binding approval required'}},
 {id:'proposal-session-principal-binding',gap:'G5',proposal:{principalId:uuid(3),capabilities:['decide'],requires:'authenticated principal source and token lifecycle binding approval'}},
 {id:'proposal-dual-revision-http',gap:'G6',proposal:{expectedRevision:7,expectedSubmissionRevision:3,publishedEntityRevision:8,missingExpectation:'block; never default'}},
 {id:'proposal-public-binding',gap:'G7',proposal:{httpRoute:null,approval:'all method/path/DTO bindings remain unapproved'}},
 {id:'proposal-revision-representation',gap:'G8',proposal:{writes:'owner must select explicit compatible representation/range',auditReads:'decimal strings through Long.MAX_VALUE'}},
 {id:'proposal-statistical-vocabulary',gap:'G9',proposal:{oneApproval:'choose exactly one transition; never count both in_review->approved and approved->published',timezone:'owner decision'}},
 {id:'proposal-validation-policy',gap:'G11',proposal:{reasonMaxUtf8Bytes:1000,reasonRequired:['reject','request_changes','quarantine','rollback'],realEvidencePolicy:'default-deny pending approval'}}
].map(p=>({...p,classification:'proposal-unapproved',synthetic:true,active:false,requiresApproval:true,httpBinding:null}));
await writeFile('contracts/admin/v1/fixtures.json',JSON.stringify({schemaVersion:1,fixtureSet:'admin-v1-local-audit-2026-10-23-r1',synthetic:true,frozen:false,active:false,httpApproved:false,approval:'local comparison/captures only; not6.17 freeze',imports:['stage7b-comparison/fixtures.json'],cases:[...cases,...proposals]},null,2)+'\n');
console.log(JSON.stringify({captures:cases.length,proposals:proposals.length,statuses:[...new Set(cases.filter(c=>c.response).map(c=>c.response.status))].sort()}));
