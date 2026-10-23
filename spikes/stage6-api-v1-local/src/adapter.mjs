// Provisional LOCAL /v1 representation only: deliberately no listener, URL or deploy entry point.
import {randomUUID} from 'node:crypto';
import {LocalApi} from '../../stage6-auth-local/src/local-api.mjs';
import {SessionService,protect} from '../../stage6-auth-local/src/sessions.mjs';
import {Authorization} from '../../stage6-auth-local/src/authorization.mjs';
import {transaction} from '../../stage6-auth-local/src/storage.mjs';
import {ApiError,invalid,unauthenticated,unavailable} from '../../stage6-auth-local/src/errors.mjs';
import {CommunityApi} from '../../stage6-handlers-local/src/local-api.mjs';
import {ModerationService} from '../../stage6-handlers-local/src/moderation.mjs';
import {PublicReceipts} from './receipts.mjs';
import {Validator,text} from '../../stage6-handlers-local/src/validation.mjs';
import {object,secret,id,decimal,localRevision,readRevision,timestamp,parseBody} from './wire.mjs';
const caps=new Set(['submit','submission.read.own','submission.revise.own','submission.withdraw.own','queue.read','decide','quarantine','audit.read','roles.manage']);
const safeMessages={400:['validation_failed','Invalid request'],401:['unauthenticated','Authentication required'],403:['forbidden','Operation not permitted'],404:['not_found','Resource not found'],405:['method_not_allowed','Method not allowed'],409:['conflict','Revision or state conflict'],422:['idempotency_mismatch','Key already used for a different request'],503:['service_unavailable','Service unavailable']};
const output=(fn,...args)=>{try{return fn(...args);}catch{throw unavailable();}};
const known=new Map([['/v1/session/challenge',['POST']],['/v1/session',['POST','GET','DELETE']],['/v1/session/refresh',['POST']],['/v1/me/capabilities',['GET']]]);
export function createLocalAdapter({localOnly,connect,schema,verifier,clock,validator=new Validator()}){
 if(localOnly!==true||typeof clock!=='function')throw new TypeError('explicit local-only composition and clock required');
 const sessions=new SessionService({connect,schema,verifier,clock,accessTtlMs:900000,refreshTtlMs:604800000,challengeTtlMs:300000});
 const authorization=new Authorization({sessions,clock});const idempotency=new PublicReceipts({schema,clock});
 return new PublicAdapter({sessions,authorization,idempotency,validator,clock});
}
export class PublicAdapter {
 constructor({sessions,authorization,idempotency,validator,clock}){Object.assign(this,{sessions,authorization,idempotency,validator,clock});this.auth=new LocalApi({sessions,authorization});}
 async invoke(request){
  let requestId=randomUUID(),allow;
  const response=(status,body)=>({status,headers:{'X-Request-ID':requestId,'Cache-Control':'no-store',...(status===204?{}:{'Content-Type':'application/json'}),...(allow?{Allow:allow}: {})},body});
  try{
   object(request,['method','path','headers','body'],['method','path']);
   const {method,path}=request;
   if(typeof method!=='string'||!/^[A-Z]+$/.test(method)||typeof path!=='string'||!path.startsWith('/')||path.includes('#'))throw invalid();
   // No supported operation takes query parameters. Reject rather than discard them.
   if(path.includes('?'))throw invalid();
   const match=/^\/v1\/admin\/submissions\/([^/]+)\/(open|decisions)$/.exec(path);
   const methods=match?['POST']:known.get(path);
   if(!methods)throw new ApiError(404,'not_found','Resource not found');
   if(!methods.includes(method)){allow=methods.join(', ');throw new ApiError(405,'method_not_allowed','Method not allowed');}
   const headers=request.headers??{};object(headers,Object.keys(headers));const normalized=Object.create(null);
   for(const [name,value] of Object.entries(headers)){const key=name.toLowerCase();if(!/^[A-Za-z][A-Za-z0-9-]*$/.test(name)||Object.hasOwn(normalized,key)||typeof value!=='string'||/[\r\n]/.test(value))throw invalid();normalized[key]=value;}
   let body;
   if(method==='POST'){
    if(normalized['content-type']!=='application/json')throw invalid();body=parseBody(request.body);
    if(body?.schemaVersion!==1)throw invalid();
   }else if(request.body!==undefined)throw invalid();
   const token=()=>{const v=normalized.authorization;if(typeof v!=='string'||!/^Bearer [A-Za-z0-9_-]+$/.test(v))throw unauthenticated();return v.slice(7);};
   const finishAuth=async(operation,b={},accessToken)=>{
    const result=await this.auth.invoke({operation,body:b,...(accessToken?{accessToken}:{})});
    requestId=output(id,result.body?.requestId??requestId);
    if(result.status>=400){const e=result.body.error;throw new ApiError(result.status,e.code,e.message);}
    return result;
   };
   const dto=(fields)=>({schemaVersion:1,requestId,...fields});
   if(match){
    const submissionId=id(match[1]),opening=match[2]==='open';
    const fields=['schemaVersion','expectedEntityRevision','expectedSubmissionRevision','idempotencyKey'];
    object(body,[...fields,...(opening?[]:['decision','reason'])],[...fields,...(opening?[]:['decision'])]);
    decimal(body.expectedEntityRevision);decimal(body.expectedSubmissionRevision,{positive:true});id(body.idempotencyKey);
    if(!opening){if(!['approve','reject','request_changes'].includes(body.decision))throw invalid();if(Object.hasOwn(body,'reason'))text(body.reason);if(body.decision!=='approve'&&!body.reason)throw invalid();}
    const accessToken=token(),operation=opening?'moderation.open':'moderation.decide';
    // Auth first on valid-but-unsupported int8 writes; never round into the older safe-number domain.
    if([body.expectedEntityRevision,body.expectedSubmissionRevision].some(v=>BigInt(v)>BigInt(Number.MAX_SAFE_INTEGER))){await this.authorization.require(accessToken,opening?'queue.read':'decide',async()=>{throw unavailable();});}
    const publicBody=structuredClone(body),localBody={schemaVersion:1,id:submissionId,expectedRevision:localRevision(body.expectedEntityRevision),expectedSubmissionRevision:localRevision(body.expectedSubmissionRevision,{positive:true}),idempotencyKey:body.idempotencyKey,...(opening?{}:{decision:body.decision,...(Object.hasOwn(body,'reason')?{reason:body.reason}:{})})};
    // Per-request bridge: public identity and complete public result are persisted by the existing
    // idempotency engine, inside the same live-authorized transaction as the domain mutation.
    const bridge={run:(c,principal,_operation,_body,fn)=>this.idempotency.run(c,principal,`public-local:POST:${path}`,publicBody,async()=>{
     const result=await fn();
     const {rows:[state]}=await c.query(`SELECT expected_revision::text,current_revision::text FROM ${this.authorization.q}.submissions WHERE id=$1`,[submissionId]);
     if(!state)throw unavailable();
     const receipt={schemaVersion:1,requestId:output(id,result.body.requestId),submissionId:output(id,result.body.id),status:result.body.status,submissionRevision:readRevision(state.current_revision,{positive:true})};
     if(opening){if(result.status!==200||receipt.status!=='in_review')throw unavailable();receipt.expectedEntityRevision=readRevision(state.expected_revision);}
     else{receipt.decision=publicBody.decision;if(receipt.decision==='approve'){if(result.status!==201||receipt.status!=='published')throw unavailable();receipt.entityId=output(id,result.body.entityId);receipt.publishedEntityRevision=readRevision(result.body.revision,{positive:true});}else if(result.status!==200||receipt.status!==(receipt.decision==='reject'?'rejected':'changes_requested'))throw unavailable();}
     return {status:result.status,body:receipt};
    })};
    const moderation=new ModerationService({authorization:this.authorization,idempotency:bridge,validator:this.validator});
    const api=new CommunityApi({moderation});
    const result=await api.invoke({operation,accessToken,body:localBody});
    requestId=output(id,result.body.requestId);
    if(result.status>=400){const e=result.body.error;throw new ApiError(result.status,e.code,e.message);}
    return response(result.status,result.body);
   }
   if(path==='/v1/session/challenge'){
    object(body,['schemaVersion']);const result=await finishAuth('challenge');
    const {rows:[stored]}=await transaction(this.sessions.connect,c=>c.query(`SELECT expires_at FROM ${this.sessions.q}.auth_challenges WHERE token_hash=$1`,[protect('challenge',result.body.challenge)]));
    if(!stored)throw unavailable();return response(201,dto({challenge:output(secret,result.body.challenge),nonce:output(secret,result.body.nonce),expiresAt:output(timestamp,stored.expires_at,{authMilliseconds:true})}));
   }
   if(method==='POST'){
    const login=path==='/v1/session';object(body,['schemaVersion',...(login?['credential','challenge']:['refreshToken'])]);
    const b=login?{credential:secret(body.credential),challenge:secret(body.challenge)}:{refreshToken:secret(body.refreshToken)};
    const result=await finishAuth(login?'login':'refresh',b);
    const context=await this.sessions.withSession(result.body.accessToken,async(c,ctx)=>{
     const {rows:[family]}=await c.query(`SELECT expires_at FROM ${this.sessions.q}.auth_families WHERE id=$1 AND principal_id=$2`,[ctx.familyId,ctx.principalId]);if(!family)throw unavailable();return {...ctx,refreshExpiresAt:family.expires_at};
    });
    if(context.principalId!==result.body.principalId)throw unavailable();
    return response(login?201:200,dto({principalId:output(id,context.principalId),sessionId:output(id,context.sessionId),accessToken:output(secret,result.body.accessToken),refreshToken:output(secret,result.body.refreshToken),expiresAt:output(timestamp,context.expiresAt,{authMilliseconds:true}),refreshExpiresAt:output(timestamp,context.refreshExpiresAt,{authMilliseconds:true})}));
   }
   const accessToken=token();
   // LocalApi deliberately drops the body (including its generated ID) on204.
   // Pass this request's ID directly to the same session service so audit/header correlate.
   if(method==='DELETE'){await this.sessions.logout(accessToken,requestId);return response(204,null);}
   if(path==='/v1/session'){
    const result=await finishAuth('session',{},accessToken);
    return response(200,dto({principalId:output(id,result.body.principalId),sessionId:output(id,result.body.sessionId),expiresAt:output(timestamp,result.body.expiresAt,{authMilliseconds:true})}));
   }
   const context=await this.sessions.withSession(accessToken,async(c,ctx)=>{
    const bound={q:this.sessions.q,withSession:(_token,fn)=>fn(c,ctx)};
    const authorization=new Authorization({sessions:bound,clock:this.clock});
    return {...ctx,capabilities:await authorization.capabilities(accessToken)};
   });
   if(!Array.isArray(context.capabilities)||context.capabilities.some(x=>!caps.has(x))||new Set(context.capabilities).size!==context.capabilities.length)throw unavailable();
   return response(200,dto({principalId:output(id,context.principalId),sessionId:output(id,context.sessionId),capabilities:context.capabilities}));
  }catch(error){
   const e=error instanceof ApiError&&safeMessages[error.status]?.[0]===error.code?error:unavailable();
   return response(e.status,{schemaVersion:1,error:{code:safeMessages[e.status][0],message:safeMessages[e.status][1],requestId}});
  }
 }
}
