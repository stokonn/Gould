// State-bearing SCRIPTED SQL, not PostgreSQL. Real Auth/Authorization/Moderation/Idempotency
// methods execute unchanged. Global transaction serialization is a test-harness device,
// NOT evidence for database locking, constraints, durability or concurrent PG transactions.
import {randomUUID} from 'node:crypto';
import {createLocalAdapter} from '../src/adapter.mjs';
import {Validator} from '../../stage6-handlers-local/src/validation.mjs';
import {unauthenticated} from '../../stage6-auth-local/src/errors.mjs';
export const ID={admin:'00000000-0000-4000-8000-000000000001',author:'00000000-0000-4000-8000-000000000002',sub:'00000000-0000-4000-8000-000000000003',entity:'00000000-0000-4000-8000-000000000004'};
const issuer='https://identity.synthetic.invalid';
export function syntheticValidator(){return new Validator({fields:{work:{syntheticTitle:{type:'text',maxBytes:2048}}},intendedLicenses:['SYNTHETIC-NOT-APPROVED'],sources:[{type:'reference',value:'Synthetic reference only',license:'SYNTHETIC-NOT-APPROVED',kind:'work',fields:['syntheticTitle'],intendedLicenses:['SYNTHETIC-NOT-APPROVED']}]});}
export class ScriptedDatabase {
 constructor(){this.trace=[];this.tail=Promise.resolve();this.state={principals:{[ID.admin]:{id:ID.admin,iss:issuer,sub:'reviewer',state:'active'}},roles:{[ID.admin]:['administrator']},challenges:{},families:{},sessions:{},refresh:{},submissions:{},snapshots:{},entities:{},published:[],reviews:[],audit:[],feed:[],receipts:{}};}
 connect=async()=>{
  let release,snapshot;const saves=new Map();
  return {end:async()=>{},query:async(sql,args=[])=>{
   this.trace.push(sql);
   if(sql==='BEGIN'){const previous=this.tail;this.tail=new Promise(r=>release=r);await previous;snapshot=structuredClone(this.state);return {rows:[]};}
   if(sql==='COMMIT'){release();return {rows:[]};}
   if(sql==='ROLLBACK'){this.state=snapshot;release();return {rows:[]};}
   if(sql.startsWith('SAVEPOINT ')){saves.set(sql.slice(10),structuredClone(this.state));return {rows:[]};}
   if(sql.startsWith('ROLLBACK TO SAVEPOINT ')){this.state=structuredClone(saves.get(sql.slice(22)));return {rows:[]};}
   if(sql.startsWith('RELEASE SAVEPOINT ')){saves.delete(sql.slice(18));return {rows:[]};}
   if(sql.startsWith('SET LOCAL')||sql.includes('pg_advisory_xact_lock'))return {rows:[]};
   if(this.fail?.(sql))throw Error('synthetic storage fault');
   return this.query(sql,args);
  }};
 };
 query(sql,a){
  const s=this.state,row=x=>({rows:x?[structuredClone(x)]:[],rowCount:x?1:0}),rows=x=>({rows:structuredClone(x),rowCount:x.length});
  if(sql.startsWith('INSERT INTO')&&sql.includes('.auth_challenges(')){s.challenges[a[0]]={token_hash:a[0],created_at:a[1],expires_at:a[2],consumed_at:null};return row();}
  if(sql.startsWith('SELECT expires_at')&&sql.includes('.auth_challenges'))return row(s.challenges[a[0]]);
  if(sql.startsWith('INSERT INTO')&&sql.includes('.principals(')){if(!Object.values(s.principals).some(x=>x.iss===a[1]&&x.sub===a[2]))s.principals[a[0]]={id:a[0],iss:a[1],sub:a[2],state:'active'};return row();}
  if(sql.startsWith('SELECT id,state')&&sql.includes('WHERE iss='))return row(Object.values(s.principals).find(x=>x.iss===a[0]&&x.sub===a[1]));
  if(sql.startsWith('UPDATE')&&sql.includes('.auth_challenges SET consumed_at')){const x=s.challenges[a[0]];if(!x||x.consumed_at||x.expires_at<=a[1])return row();x.consumed_at=a[1];return row({token_hash:a[0]});}
  if(sql.startsWith('INSERT INTO')&&sql.includes('.auth_families(')){s.families[a[0]]={id:a[0],principal_id:a[1],created_at:a[2],expires_at:a[3],revoked_at:null};return row();}
  if(sql.startsWith('INSERT INTO')&&sql.includes('.sessions(')){s.sessions[a[4]]={id:a[0],principal_id:a[1],family_id:a[2],predecessor_id:a[3],token_hash:a[4],issued_at:a[5],expires_at:a[6],revoked_at:null};return row();}
  if(sql.startsWith('INSERT INTO')&&sql.includes('.auth_refresh(')){s.refresh[a[0]]={token_hash:a[0],session_id:a[1],principal_id:a[2],family_id:a[3],created_at:a[4],expires_at:a[5],consumed_at:null};return row();}
  if(sql.startsWith('SELECT principal_id,family_id FROM')&&sql.includes('.sessions WHERE'))return row(s.sessions[a[0]]);
  if(sql.startsWith('SELECT principal_id,family_id FROM')&&sql.includes('.auth_refresh WHERE'))return row(s.refresh[a[0]]);
  if(sql.startsWith('SELECT id,state')&&sql.includes('id=ANY'))return rows(a[0].map(id=>s.principals[id]).filter(Boolean));
  if(sql.startsWith('SELECT * FROM')&&sql.includes('.auth_families WHERE')){const f=s.families[a[0]];return row(f?.principal_id===a[1]?f:null);}
  if(sql.startsWith('SELECT expires_at')&&sql.includes('.auth_families WHERE')){const f=s.families[a[0]];return row(f?.principal_id===a[1]?f:null);}
  if(sql.startsWith('SELECT id,issued_at,expires_at,revoked_at'))return row(s.sessions[a[0]]);
  if(sql.startsWith('SELECT * FROM')&&sql.includes('.auth_refresh WHERE'))return row(s.refresh[a[0]]);
  if(sql.startsWith('SELECT revoked_at FROM')&&sql.includes('.sessions WHERE id='))return row(Object.values(s.sessions).find(x=>x.id===a[0]));
  if(sql.startsWith('UPDATE')&&sql.includes('.auth_refresh SET consumed_at')){s.refresh[a[0]].consumed_at=a[1];return row();}
  if(sql.startsWith('UPDATE')&&sql.includes('.sessions SET revoked_at')){for(const x of Object.values(s.sessions))if((sql.includes('WHERE family_id=')?x.family_id:x.id)===a[0])x.revoked_at??=a[1];return row();}
  if(sql.startsWith('UPDATE')&&sql.includes('.auth_families SET revoked_at')){s.families[a[0]].revoked_at??=a[1];return row();}
  if(sql.startsWith('SELECT role FROM'))return rows((s.roles[a[0]]??[]).map(role=>({role})));
  if(sql.includes('.append_audit(')){const event={seq:String(s.audit.length+1),actorId:a[0],action:a[1],requestId:sql.includes("'principal'")?a[2]:a[4]};s.audit.push(event);return row({seq:event.seq});}
  if(sql.startsWith('SELECT * FROM')&&sql.includes('.idempotency_results WHERE'))return row(s.receipts[a[0]+':'+a[1]]);
  if(sql.startsWith('DELETE FROM')&&sql.includes('.idempotency_results')){const key=a[0]+':'+a[1];if(s.receipts[key]?.expires_at<=a[2])delete s.receipts[key];return row();}
  if(sql.startsWith('INSERT INTO')&&sql.includes('.idempotency_results(')){const key=a[0]+':'+a[1];if(s.receipts[key])throw Object.assign(Error('synthetic duplicate'),{code:'23505',constraint:'idempotency_results_pkey'});s.receipts[key]={request_hash:a[2],http_status:a[3],response:structuredClone(a[4]),created_at:a[5],expires_at:new Date(+a[5]+86400000)};return row();}
  if(sql.startsWith('SELECT * FROM')&&sql.includes('.submissions WHERE'))return row(s.submissions[a[0]]);
  if(sql.startsWith('SELECT expected_revision::text,current_revision::text'))return row(s.submissions[a[0]]);
  if(sql.startsWith('SELECT payload,this_hash FROM'))return row(s.snapshots[a[0]+':'+a[1]]);
  if(sql.startsWith('INSERT INTO')&&sql.includes('.review_events(')){s.reviews.push({submission_id:a[1],submission_revision:String(a[2]),from:a[4],to:a[5]});return row();}
  if(sql.startsWith('UPDATE')&&sql.includes('.submissions SET status=')){Object.assign(s.submissions[a[0]],{status:a[1],current_revision:String(a[2])});return row();}
  if(sql.startsWith('SELECT * FROM')&&sql.includes('.published_entities WHERE'))return row(s.entities[a[0]]);
  if(sql.startsWith('SELECT coalesce(max(revision),0)::text')){const revisions=s.published.filter(x=>x.entity_id===a[0]).map(x=>BigInt(x.revision));return row({n:String(revisions.reduce((x,y)=>x>y?x:y,0n))});}
  if(sql.startsWith('INSERT INTO')&&sql.includes('.published_entities(')){s.entities[a[0]]={id:a[0],kind:a[1],revision:String(a[2]),status:'published'};return row();}
  if(sql.startsWith('INSERT INTO')&&sql.includes('.published_revisions(')){s.published.push({entity_id:a[0],revision:String(a[1]),submission_id:a[2],payload:structuredClone(a[5])});return row();}
  if(sql.startsWith('UPDATE')&&sql.includes('.published_entities SET revision=')){s.entities[a[0]].revision=String(a[1]);return row();}
  if(sql.startsWith('INSERT INTO')&&sql.includes('.change_feed(')){s.feed.push({entity_id:a[0],revision:String(a[1]),status:a[2],audit_seq:a[3]});return row();}
  throw Error('Unexpected scripted SQL: '+sql.slice(0,120));
 }
 seed({status='in_review',entity='7',proposal='3',author=ID.author,currentEntity=entity}={}){
  const s=this.state;s.submissions[ID.sub]={id:ID.sub,author_id:author,status,current_revision:proposal,expected_revision:entity,catalogue_uuid:ID.entity,target_kind:'work',mapping_id:null};
  const payload={schemaVersion:1,target:{kind:'work',catalogueUuid:ID.entity},changeKind:'correction',fields:[{field:'syntheticTitle',newValue:'Synthetic work',evidence:[{type:'reference',value:'Synthetic reference only',license:'SYNTHETIC-NOT-APPROVED'}]}],intendedLicense:'SYNTHETIC-NOT-APPROVED'};
  s.snapshots[ID.sub+':'+proposal]={payload,this_hash:'1'.repeat(64)};
  if(currentEntity!=='0'){s.entities[ID.entity]={id:ID.entity,kind:'work',revision:currentEntity,status:'published'};s.published=[{entity_id:ID.entity,revision:currentEntity}];}
 }
}
export async function venue({policy=true,login=true}={}){
 const db=new ScriptedDatabase();let now=Date.parse('2030-01-01T12:00:00.123Z');
 const verifier={verify:async credential=>{let c;try{c=JSON.parse(credential);}catch{throw unauthenticated();}if(c.issuer!==issuer||c.audience!=='synthetic-local'||typeof c.subject!=='string'||c.exp<=now||c.iat>now+60000)throw unauthenticated();return {issuer,subject:c.subject,nonce:c.nonce};}};
 const adapter=createLocalAdapter({localOnly:true,connect:db.connect,schema:'proto_api_m1',verifier,clock:()=>now,...(policy?{validator:syntheticValidator()}:{})});
 const send=(method,path,body,token)=>adapter.invoke({method,path,headers:{...(body!==undefined?{'Content-Type':'application/json'}:{}),...(token?{Authorization:'Bearer '+token}:{})},...(body!==undefined?{body:JSON.stringify(body)}:{})});
 const signIn=async(subject='reviewer')=>{const challenge=await send('POST','/v1/session/challenge',{schemaVersion:1});const credential=JSON.stringify({issuer,audience:'synthetic-local',subject,nonce:challenge.body.nonce,iat:now,exp:now+120000});return send('POST','/v1/session',{schemaVersion:1,credential,challenge:challenge.body.challenge});};
 const result=login?await signIn():null;
 return {adapter,db,send,signIn,session:result?.body,loginStatus:result?.status,now:()=>now,advance:ms=>now+=ms};
}
export const intent=(extra={})=>({schemaVersion:1,expectedEntityRevision:'7',expectedSubmissionRevision:'3',idempotencyKey:randomUUID(),...extra});
export const decisionPath=`/v1/admin/submissions/${ID.sub}/decisions`;
export const openPath=`/v1/admin/submissions/${ID.sub}/open`;
