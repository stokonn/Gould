// Synthetic fixture services seed data; every send/signIn call is real HTTP + PG.
import assert from 'node:assert/strict';
import {after} from 'node:test';
import {writeFileSync} from 'node:fs';
const captures=[];
after(()=>{if(process.env.HTTP_PG_CAPTURES)writeFileSync(process.env.HTTP_PG_CAPTURES,JSON.stringify(captures,null,2)+'\n');});
import {randomUUID,randomBytes} from 'node:crypto';
import {localClient,identifier} from '../../stage6-schema-pg/src/database.mjs';
import {migrateAuth} from '../../stage6-auth-local/src/storage.mjs';
import {SubmissionService} from '../../stage6-handlers-local/src/submissions.mjs';
import {Idempotency} from '../../stage6-handlers-local/src/idempotency.mjs';
import {createLocalAdapter} from '../src/adapter.mjs';
import {createLocalHttpServer,listenLocalHttp} from '../src/http.mjs';
import {syntheticValidator} from './venue.mjs';
export const connect=async()=>{const c=localClient();await c.connect();return c;};
export const intent=(extra={})=>({schemaVersion:1,expectedEntityRevision:'0',expectedSubmissionRevision:'1',idempotencyKey:randomUUID(),...extra});
export const payload=entity=>({schemaVersion:1,target:{kind:'work',catalogueUuid:entity},changeKind:'new_metadata',fields:[{field:'syntheticTitle',newValue:'Synthetic work',evidence:[{type:'reference',value:'Synthetic reference only',license:'SYNTHETIC-NOT-APPROVED'}]}],intendedLicense:'SYNTHETIC-NOT-APPROVED'});
export async function venue(t){
 const db=await connect(),schema='proto_http_'+randomUUID().replaceAll('-',''),q=identifier(schema);
 const servers=[];t.after(async()=>{for(const server of servers){server.closeAllConnections();await new Promise(r=>server.close(r));}await db.query(`DROP SCHEMA ${q} CASCADE`);await db.end();});
 await migrateAuth(db,schema);
 let now=Date.now(),fault;const clock=()=>now,cursorKey=randomBytes(32);
 const verifier={verify:async raw=>{
  const x=JSON.parse(raw);
  if(x.issuer!=='https://synthetic.invalid'||x.audience!=='local-test'||typeof x.subject!=='string'||x.exp<=now||x.iat>now+60000)throw Error('invalid synthetic identity');
  return x;
 }};
 const faultConnect=async()=>{const c=await connect(),query=c.query.bind(c);c.query=async(sql,args)=>{if(fault)await fault(sql,args,c,query);return query(sql,args);};return c;};
 const factory=(options={})=>createLocalAdapter({localOnly:true,connect:faultConnect,schema,verifier,clock,cursorKey,validator:syntheticValidator(),...options});
 let adapter=factory();
 const start=async a=>{const server=createLocalHttpServer({adapter:a});servers.push(server);return listenLocalHttp(server);};
 let base=await start(adapter);
 const send=async(u,method,path,body)=>{
  const response=await fetch(base+path,{method,headers:{...(u?{authorization:'Bearer '+u.accessToken}:{}),...(body!==undefined?{'content-type':'application/json'}:{})},...(body!==undefined?{body:JSON.stringify(body)}:{})});
  const raw=await response.text(),b=raw?JSON.parse(raw):null;
  if(response.status===200&&method==='GET'){
   const dto=path.startsWith('/v1/admin/queue')?'QueuePage':path.startsWith('/v1/admin/audit')?'AuditPage':path.startsWith('/v1/admin/stats')?'Stats':null;
   if(dto)captures.push({dto,value:b});
  }
  if(b?.requestId??b?.error?.requestId)assert.equal(response.headers.get('x-request-id'),b.requestId??b.error.requestId);
  return {status:response.status,body:b,raw,headers:response.headers};
 };
 const signIn=async(subject,role)=>{
  const ch=await send(null,'POST','/v1/session/challenge',{schemaVersion:1});assert.equal(ch.status,201,JSON.stringify(ch.body));
  const u=await send(null,'POST','/v1/session',{schemaVersion:1,challenge:ch.body.challenge,credential:JSON.stringify({issuer:'https://synthetic.invalid',audience:'local-test',subject,nonce:ch.body.nonce,iat:now,exp:now+120000})});assert.equal(u.status,201,JSON.stringify(u.body));
  if(role)await db.query(`INSERT INTO ${q}.role_grants(id,principal_id,role,granted_by,granted_at) VALUES($1,$2,$3,$2,$4)`,[randomUUID(),u.body.principalId,role,new Date(now)]);
  return u.body;
 };
 const v={db,schema,q,send,signIn,url:()=>base,clock,advance:ms=>now+=ms,setFault:f=>fault=f,
  fresh:async options=>{adapter=factory(options);base=await start(adapter);},adapter:()=>adapter};
 v.author=await signIn('author','contributor');v.admin=await signIn('admin','administrator');v.mod=await signIn('mod','moderator');v.other=await signIn('other','contributor');
 v.submissions=new SubmissionService({authorization:adapter.authorization,validator:syntheticValidator(),idempotency:new Idempotency({schema,clock})});
 v.create=async({entity=randomUUID(),expected=0,open=false}={})=>{
  const result=await v.submissions.create(v.author.accessToken,{schemaVersion:1,payload:payload(entity),expectedRevision:expected,idempotencyKey:randomUUID()});
  const s={...result.body,entity,path:'/v1/admin/submissions/'+result.body.id};
  if(open)assert.equal((await send(v.mod,'POST',s.path+'/open',intent({expectedEntityRevision:String(expected)}))).status,200);
  return s;
 };
 return v;
}
export const get=(v,path,u=v.admin)=>v.send(u,'GET',path);
export const decide=(v,s,extra={},u=v.admin)=>v.send(u,'POST',s.path+'/decisions',intent({decision:'approve',...extra}));
export async function snapshot(v){const out={};for(const table of ['submissions','submission_revisions','published_entities','published_revisions','review_events','audit_events','audit_head','change_feed','idempotency_results'])out[table]=(await v.db.query(`SELECT row_to_json(t)::text AS r FROM ${v.q}.${table} t ORDER BY row_to_json(t)::text`)).rows;return out;}
