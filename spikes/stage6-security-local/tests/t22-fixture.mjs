// Synthetic scripted storage; no PostgreSQL/HTTP proof.
import {randomUUID,randomBytes} from 'node:crypto';
import {SessionService} from '../../stage6-auth-local/src/sessions.mjs';
import {Authorization} from '../../stage6-auth-local/src/authorization.mjs';
import {LocalApi} from '../../stage6-auth-local/src/local-api.mjs';
import {Idempotency} from '../../stage6-handlers-local/src/idempotency.mjs';
import {compose} from '../../stage6-public-admin-abuse/src/compose.mjs';
import {Validator} from '../../stage6-handlers-local/src/validation.mjs';
const now=Date.parse('2030-01-01T00:00:00Z');
function venue({role='administrator',state='active',expired=false,revoked=false,missing=false,unavailable=false,extraQuery,cursorKey}={}){
 const principalId=randomUUID(),familyId=randomUUID(),trace=[],queries=[],logs=[];
 const accessToken='gs_access_'+randomBytes(32).toString('base64url');
 const client={end:async()=>trace.push('end'),query:async(sql,args=[])=>{
  queries.push(sql);trace.push(sql.split(/\s+/).slice(0,4).join(' '));
  if(sql==='BEGIN'||sql==='COMMIT'||sql==='ROLLBACK'||sql.startsWith('SET LOCAL')||sql.includes('pg_advisory_xact_lock'))return {rows:[],rowCount:0};
  if(sql.includes('.sessions WHERE token_hash=')&&sql.startsWith('SELECT principal_id'))return {rows:missing?[]:[{principal_id:principalId,family_id:familyId}]};
  if(sql.includes('.principals WHERE id=ANY'))return {rows:[{id:principalId,state}]};
  if(sql.startsWith('SELECT * FROM')&&sql.includes('.auth_families WHERE'))return {rows:[{id:familyId,created_at:new Date(now-1000),expires_at:new Date(now+100000),revoked_at:revoked?new Date(now):null}]};
  if(sql.startsWith('SELECT id,issued_at,expires_at,revoked_at'))return {rows:[{id:randomUUID(),issued_at:new Date(now-1000),expires_at:new Date(expired?now:now+10000),revoked_at:revoked?new Date(now):null}]};
  if(sql.startsWith('SELECT role FROM'))return {rows:role?[{role}]:[]};
  if(extraQuery&&sql.includes('.idempotency_results'))return extraQuery(sql,args);
  if(sql.startsWith('SELECT request_hash,')||sql.includes('.idempotency_results WHERE'))return {rows:[],rowCount:0};
  if(sql==='SAVEPOINT idem_effects'||sql==='RELEASE SAVEPOINT idem_effects'||sql==='ROLLBACK TO SAVEPOINT idem_effects')return {rows:[]};
  if(extraQuery&&(sql.includes('.audit_head')||sql.includes('.audit_events')))return extraQuery(sql,args);
  if(sql.startsWith('SELECT last_seq::text,last_hash'))return {rows:[{last_seq:'0',last_hash:'0'.repeat(64)}]};
  if(sql.includes('FROM "proto_t22".audit_events'))return {rows:[]};
  if(extraQuery)return extraQuery(sql,args);
  throw Error('Unexpected SQL in scripted denial venue');
 }};
 const connect=async()=>{trace.push('connect');if(unavailable)throw Error('synthetic unavailable storage');return client;};
 const sessions=new SessionService({connect,schema:'proto_t22',verifier:{verify:async()=>{throw Error('not used');}},clock:()=>now,accessTtlMs:10000,refreshTtlMs:100000,challengeTtlMs:1000});
 const authorization=new Authorization({sessions,clock:()=>now});
 const idempotency=new Idempotency({schema:'proto_t22',clock:()=>now});
 const services=compose({connect,schema:'proto_t22',authorization,idempotency,validator:new Validator(),clock:()=>now,cursorKey:cursorKey??randomBytes(32),ipKey:randomBytes(32),logger:v=>logs.push(v)});
 const authApi=new LocalApi({sessions,authorization,logger:v=>logs.push(v)});
 return {principalId,accessToken,trace,queries,logs,...services,authApi};
}
const body=(v,extra={})=>({schemaVersion:1,principalId:v.principalId,role:'administrator',revoke:false,idempotencyKey:randomUUID(),...extra});
const invoke=(v,operation='admin.roles',b=body(v),token=v.accessToken)=>v.api.invoke({operation,body:b,...(token===undefined?{}:{accessToken:token})});

export {venue,body,invoke};
